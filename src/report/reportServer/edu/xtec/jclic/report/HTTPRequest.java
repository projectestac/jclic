/*
 * File    : HTTPRequest.java
 * Created : 13-feb-2003 15:19
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

import edu.xtec.servlet.RequestProcessor;
import edu.xtec.util.Html;
import edu.xtec.util.StrUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class provides methods and encapsulates the data involved in a
 * transaction using the HTTP protocol. It provides basic methods to accept an
 * incoming conenction throught an open {@link java.net.Socket}, read the
 * cookies and parameters passed by the client, read the data passed into the
 * request header and leave a prepared {@link java.io.OutputStream} and a
 * {@link java.io.PrintWriter} for sending back the response to the client.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class HTTPRequest {

  public Socket socket;
  public BufferedReader in;
  public OutputStream os;
  public PrintWriter pw;
  public Map<String, Object> cookies = new HashMap<String, Object>();
  public Map<String, Object> params = new HashMap<String, Object>();
  public String method;
  public String urlBase;
  public String protocol;
  public boolean commited;
  public String firstLine;
  public InputStream inputStream;

  ResponseHead head = new ResponseHead();

  public static final int OK = 200, BAD_REQUEST = 400, MOVED_PERM = 301, FOUND = 302, NOT_FOUND = 404,
      SERVER_ERROR = 500;
  public static final String MIME_HTML = "text/html";

  /** Creates a new instance of HTTPRequest */
  public HTTPRequest(Socket socket) throws Exception {

    this.socket = socket;
    buildStreams();

    int cl = -1;
    try {
      firstLine = in.readLine();
      if (firstLine == null) {
        while (in.ready())
          in.readLine();
        return;
      }
      StringTokenizer st = new StringTokenizer(firstLine, " ");
      method = st.nextToken();
      String s = st.nextToken();
      int i = s.indexOf('?');
      if (i < 0)
        urlBase = s;
      else {
        urlBase = s.substring(0, i);
        processParamsLine(s.substring(i + 1), params, '&', '=', true);
      }
      protocol = st.nextToken();
      if (protocol == null || !protocol.equals("HTTP/1.0") && !protocol.equals("HTTP/1.1")) {
        error(BAD_REQUEST, null);
        throw new Exception("Bad request!");
      }
    } catch (Exception ex) {
      error(BAD_REQUEST, null);
      throw ex;
    }

    while (true) {
      String line = StrUtils.nullableString(in.readLine());
      if (line == null)
        break;
      else if (line.toLowerCase().startsWith("cookie:")) {
        int k = line.indexOf(' ');
        if (k > 0)
          processParamsLine(line.substring(k + 1), cookies, ';', '=', false);
      } else if (line.toLowerCase().startsWith("content-length:")) {
        cl = Integer.parseInt(line.substring(16));
      }
    }

    if (cl >= 0) {
      char[] buf = new char[cl];
      int k = in.read(buf);
      // Corrected a bug that added trailing zeroes to the resulting line
      // Now the "copyValueOf" takes care of the number of readed "chars",
      // not the estimated length in bytes
      // String line=String.copyValueOf(buf);
      String line = String.copyValueOf(buf, 0, k);
      inputStream = new java.io.ByteArrayInputStream(line.getBytes());
      if (!line.startsWith("<"))
        processParamsLine(line, params, '&', '=', true);
    } else {
      while (in.ready()) {
        String line = StrUtils.nullableString(in.readLine());
        if (line == null)
          break;
        processParamsLine(line, params, '&', '=', true);
      }
    }
  }

  private void buildStreams() throws java.io.IOException {
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    os = new BufferedOutputStream(socket.getOutputStream());
    pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), RequestProcessor.CHARSET));
  }

  private void buildStreamsDebug(final java.io.PrintStream log) throws java.io.IOException {
    in = new BufferedReader(new InputStreamReader(socket.getInputStream())) {
      @Override
      public String readLine() throws java.io.IOException {
        String result = super.readLine();
        if (result != null) {
          log.println("< " + result);
        }
        return result;
      }

      @Override
      public int read(char buf[]) throws java.io.IOException {
        int result = super.read(buf);
        if (result > 0)
          log.println("< " + new String(buf, 0, result));
        return result;
      }
    };
    os = new BufferedOutputStream(socket.getOutputStream()) {
      @Override
      public synchronized void write(byte b[], int off, int len) throws java.io.IOException {
        if (b != null) {
          log.println("> " + new String(b, off, len));
        }
        super.write(b, off, len);
      }
    };
    pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), RequestProcessor.CHARSET)) {
      @Override
      public void write(String s, int off, int len) {
        log.println("> " + s);
        super.write(s, off, len);
      }
    };
  }

  public static void processParamsLine(String txt, Map<String, Object> map, char sep, char equalSign, boolean arrays) {
    if (txt != null && txt.length() > 0 && map != null) {
      StringTokenizer st = new StringTokenizer(txt, String.valueOf(sep));
      while (st.hasMoreTokens()) {
        String s = st.nextToken().trim();
        String key, value = null;
        int k = s.indexOf(equalSign);
        if (k > 0) {
          key = Html.decode(s.substring(0, k).replace('+', ' '));
          if (k < s.length() - 1)
            value = Html.decode(s.substring(k + 1).replace('+', ' '));
        } else
          key = Html.decode(s.replace('+', ' '));

        if (arrays) {
          String[] vArray = (String[]) map.get(key);
          if (vArray == null) {
            vArray = new String[] { value };
          } else {
            String[] v2 = new String[vArray.length + 1];
            int i = 0;
            for (; i < vArray.length; i++)
              v2[i] = vArray[i];
            v2[i] = value;
            vArray = v2;
          }
          map.put(key, vArray);
        } else {
          map.put(key, value);
        }
      }
    }
  }

  public final void error(int code, String msg) throws Exception {
    String err;
    switch (code) {
    case BAD_REQUEST:
      err = "Bad Request";
      break;
    case MOVED_PERM:
      err = "Moved permanently";
      break;
    case NOT_FOUND:
      err = "File not found";
      break;
    case SERVER_ERROR:
      err = "Server error";
      break;
    default:
      err = "Undefined error";
    }
    head.code = code;
    head.title = err;
    head.write();
    StringBuilder sb = new StringBuilder(100);
    sb.append("<B>").append(err).append("</B>\n<BR>&nbsp;<BR>\n");
    if (msg != null)
      sb.append(msg);
    minimalPage("ERROR: " + err, sb.substring(0));
  }

  public void redirect(String url) throws Exception {
    head.contentType = null;
    head.code = MOVED_PERM;
    head.title = "Moved permanently";
    StringBuilder sb = new StringBuilder(100);
    sb.append("http://");
    String serverAddress = socket.getLocalAddress().getHostAddress();
    if (serverAddress == null || "0.0.0.0".equals(serverAddress))
      serverAddress = java.net.InetAddress.getLocalHost().getHostAddress();
    sb.append(serverAddress);
    sb.append(":").append(socket.getLocalPort()).append("/").append(url);

    String fullUrl = sb.substring(0);
    sb.setLength(0);
    sb.append("Location: ").append(fullUrl);
    head.extra = sb.substring(0);
    head.write();
    sb.setLength(0);
    sb.append("redirected to: <A HREF=\"").append(fullUrl).append("\">").append(fullUrl).append("</A>");
    minimalPage("redirect", sb.substring(0));
  }

  public class ResponseHead {
    public String contentType;
    public int code, contentLength;
    public String title, extra;
    public boolean cache;
    public boolean commited;

    public ResponseHead() {
      contentType = MIME_HTML;
      code = OK;
      cache = true;
      commited = false;
      contentLength = -1;
    }

    public void write() {

      StringBuilder sb = new StringBuilder(200);
      sb.append("HTTP/1.0 ");
      sb.append(code).append(" ").append(title == null ? "OK" : title);
      pw.println(sb.substring(0));

      if (extra != null)
        pw.println(extra);

      if (contentType != null) {
        sb.setLength(0);
        pw.println(sb.append("Content-Type: ").append(contentType).substring(0));
      }

      sb.setLength(0);
      pw.println(sb.append("Date: ").append(RequestProcessor.httpDate(new java.util.Date())).substring(0));
      pw.println("Server: JClicHttpServer 1.0");
      if (!cache) {
        pw.println("Pragma: no-cache"); // HTTP/1.0
        pw.println("Cache-Control: no-cache"); // HTTP/1.1
        pw.println("Expires: 0");
      }

      if (cookies.size() > 0) {
        sb.setLength(0);
        sb.append("Set-Cookie: ");
        Iterator it = cookies.keySet().iterator();
        boolean first = true;
        while (it.hasNext()) {
          String key = (String) it.next();
          if (!first)
            sb.append(";");
          else
            first = false;
          sb.append(key).append("=").append(cookies.get(key));
        }
        pw.println(sb.substring(0));
      }

      if (contentLength > 0) {
        sb.setLength(0);
        pw.println(sb.append("Content-Length: ").append(contentLength).substring(0));
      }

      pw.println("");
      pw.flush();
      commited = true;
    }
  }

  public void minimalPage(String title, String text) throws Exception {
    StringBuilder sb = new StringBuilder(1024);
    sb.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
    sb.append("<HTTP>\n<HEAD>\n");
    if (title != null)
      sb.append("<TITLE>").append(title).append("</TITLE>\n");
    sb.append("</HEAD>\n<BODY>\n");
    sb.append("<H2>").append(title).append("</H2>\n");
    sb.append(text);
    sb.append("\n</BODY>\n</HTML>");
    pw.println(sb.substring(0));
    os.flush();
    commited = true;
  }
}
