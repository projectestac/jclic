/*
 * File    : HttpReportServer.java
 * Created : 13-feb-2003 14:47
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2005 Francesc Busquets & Departament
 * d'Educacio de la Generalitat de Catalunya
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (see the LICENSE file).
 */

package edu.xtec.jclic.report;

import edu.xtec.jclic.report.rp.*;
import edu.xtec.servlet.RequestProcessor;
import edu.xtec.servlet.ResourceRP;
import edu.xtec.util.Messages;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class HTTPReportServer extends ReportServerEventMaker {

  public static final int DEFAULT_PORT = 9000, DEFAULT_TIMEOUT = 1200;

  protected static final String[] URI = {
    "/login",
    "/main",
    "/dbAdmin",
    "/userReport",
    "/actReport",
    "/img",
    "/groupReport",
    "/groupAdmin",
    "/userAdmin",
    "/resource",
    "/JClicReportService"
  };

  protected static final Class[] CLS = {
    Login.class,
    Main.class,
    DbAdmin.class,
    UserReport.class,
    ActReport.class,
    Img.class,
    GroupReport.class,
    GroupAdmin.class,
    UserAdmin.class,
    ResourceRP.class,
    JClicReportService.class
  };

  HttpThread httpThread;

  edu.xtec.util.Messages messages;

  /** Creates a new instance of HttpReportServer */
  public HTTPReportServer(Messages messages) {
    this.messages = messages;
    httpThread = null;

    RequestProcessor.setDirectResources(true);
  }

  public boolean startServer(int port, int timeOut) {
    if (serverRunning()) {
      fireReportServerSystemEvent(
          messages.get("manage_server_already_started"), ReportServerEvent.ERROR);
      return false;
    }

    try {
      httpThread = new HttpThread(port, timeOut);
      httpThread.start();
    } catch (Exception e) {
      fireReportServerSystemEvent(e.toString(), ReportServerEvent.ERROR);
      return false;
    }
    return true;
  }

  public boolean stopServer() {
    if (!serverRunning()) {
      fireReportServerSystemEvent("Server is not started!", ReportServerEvent.ERROR);
      return false;
    }
    httpThread.stopServer();
    while (serverRunning()) {
      Thread.yield();
    }
    return true;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      if (serverRunning()) stopServer();
    } finally {
      super.finalize();
    }
  }

  public boolean serverRunning() {
    return httpThread != null;
  }

  protected class HttpThread extends Thread {

    boolean running = false;
    ServerSocket ss = null;
    int socketTimeOut = 0;

    HttpThread(int port, int timeOut) throws Exception {
      ss = new ServerSocket(port);
      fireReportServerSystemEvent(Integer.toString(port), ReportServerEvent.START);
      socketTimeOut = timeOut;
      running = false;
    }

    @Override
    public void run() {
      try {
        running = true;
        ss.setSoTimeout(1000);
        while (running) {
          try {
            Socket socket = ss.accept();
            ClientConnection cc = new ClientConnection(socket);
            cc.start();
          } catch (InterruptedIOException ioex) {
            // Timeout. start again...
          } catch (Exception ex) {
            fireReportServerSystemEvent(ex.toString(), ReportServerEvent.ERROR);
            running = false;
          }
        }
        ss.close();
        fireReportServerSystemEvent(null, ReportServerEvent.STOP);
      } catch (IOException e) {
        fireReportServerSystemEvent(e.toString(), ReportServerEvent.ERROR);
      }
      running = false;
      httpThread = null;
    }

    public void stopServer() {
      running = false;
    }

    protected class ClientConnection extends Thread {

      Socket socket = null;
      int id = 0;

      ClientConnection(Socket socket) {
        this.socket = socket;
        // fireReportServerSocketEvent(socket, null, ReportServerEvent.CONNECT);
        // fireReportServerSocketEvent(socket, null, ReportServerEvent.CONNECTION);
      }

      @Override
      public void run() {
        RequestProcessor rp = null;
        HTTPRequest re = null;
        try {

          re = new HTTPRequest(socket);
          fireReportServerSocketEvent(socket, re.firstLine, ReportServerEvent.CONNECTION);

          String url = re.urlBase;
          if (url == null || url.length() == 0 || url.equals("/")) url = URI[0];

          Class<?> cl = null;
          for (int i = 0; i < URI.length; i++) {
            if (URI[i].equals(url)) {
              cl = CLS[i];
              break;
            }
          }

          int p = 0;
          if (cl == null
              && re.urlBase != null
              && (p = re.urlBase.lastIndexOf('/')) >= 0
              && p < re.urlBase.length() - 1) {
            String s = re.urlBase.substring(p + 1);
            re.params.put(ResourceRP.ID, new String[] {s});
            // System.out.println(s);
            cl = ResourceRP.class;
          }

          if (cl == null) {
            re.error(HTTPRequest.NOT_FOUND, null);
          } else {
            rp = (RequestProcessor) cl.newInstance();
            rp.setParams(re.params);
            if (rp.wantsInputStream()) rp.setInputStream(re.inputStream);
            for (String key : re.cookies.keySet()) {
              rp.setCookie(key, (String) re.cookies.get(key));
            }

            rp.init();
            re.head.cache = !rp.noCache();
            List<String[]> v = new ArrayList<String[]>();
            rp.header(v);

            if (!v.isEmpty()) {
              Iterator it = v.iterator();
              while (it.hasNext() && !re.head.commited) {
                String[] h = (String[]) it.next();
                if (h[0].equals(RequestProcessor.ERROR)) {
                  re.error(Integer.parseInt(h[1]), h[2]);
                } else if (h[0].equals(RequestProcessor.REDIRECT)) {
                  re.redirect(h[1]);
                  break;
                } else if (h[0].equals(RequestProcessor.CONTENT_TYPE)) re.head.contentType = h[1];
                else if (h[0].equals(RequestProcessor.CONTENT_LENGTH))
                  re.head.contentLength = Integer.parseInt(h[1]);
                else if (h[0].equals(RequestProcessor.EXTRA)) {
                  StringBuilder sb = new StringBuilder(100);
                  if (re.head.extra != null) sb.append(re.head.extra).append("\r\n");
                  sb.append(h[1]).append(": ").append(h[2]);
                  re.head.extra = sb.substring(0);
                } else if (h[0].equals(RequestProcessor.COOKIE)) re.cookies.put(h[1], h[2]);
              }
            }
            if (!re.head.commited) {
              re.head.write();
              if (rp.usesWriter()) {
                rp.process(re.pw);
                re.pw.flush();
                re.pw.close();
              } else {
                rp.process(re.os);
                re.os.flush();
                re.os.close();
              }
              re.commited = true;
            }
            rp.end();
            rp = null;
          }
        } catch (Exception ex) {
          int errCode = (rp != null && rp.errCode >= 0) ? rp.errCode : HTTPRequest.SERVER_ERROR;
          String errMsg = (rp != null && rp.errMsg != null) ? rp.errMsg : ex.getMessage();
          if (re != null && !re.head.commited) {
            try {
              re.error(errCode, errMsg);
            } catch (Exception ex2) {
              System.err.println("Unable to report error due to\n" + ex2);
            }
          }
          fireReportServerSocketEvent(socket, ex.toString(), ReportServerEvent.ERROR);
          System.err.println("ERROR " + errCode + ": " + errMsg);
          ex.printStackTrace(System.err);
        } finally {
          if (rp != null) rp.end();
          try {
            socket.close();
          } catch (Exception ex) {
            System.err.println("Unable to close socket due to:\n" + ex);
          }
        }
      }
    }
  }
}
