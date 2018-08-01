/*
 * File    : AbstractServlet.java
 * Created : 23-jan-2003 12:03
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

package edu.xtec.servlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public abstract class AbstractServlet extends HttpServlet {

  public AbstractServlet() {
    super();
    RequestProcessor.setDirectResources(true);
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    Enumeration en = config.getInitParameterNames();
    if (en.hasMoreElements()) {
      Map<String, Object> map = new HashMap<String, Object>();
      while (en.hasMoreElements()) {
        String key = (String) en.nextElement();
        String value = (String) config.getInitParameter(key);
      }
      RequestProcessor.config(map);
    }
  }

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    RequestProcessor rp = null;
    try {
      rp = createRP();
      if (rp.wantsInputStream())
        rp.setInputStream(request.getInputStream());
      else {
        Map<String, Object> v = new HashMap<String, Object>();
        Map parameters = request.getParameterMap();
        for (Object k : parameters.keySet()) {
          v.put((String) k, parameters.get(k));
        }
        rp.setParams(v);
      }
      Cookie[] ck = request.getCookies();
      if (ck != null)
        for (int i = 0; i < ck.length; i++)
          rp.setCookie(ck[i].getName(), ck[i].getValue());
      Enumeration en = request.getHeaderNames();
      Map<String, Object> map = new HashMap<String, Object>();
      while (en.hasMoreElements()) {
        String name = (String) en.nextElement();
        Enumeration enh = request.getHeaders(name);
        List<String> v = new ArrayList<String>();
        while (enh.hasMoreElements())
          v.add((String) enh.nextElement());
        map.put(name.toLowerCase(), v);
      }
      rp.setHeaders(map);
      rp.init();
      if (rp.noCache()) {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "0");
      }
      List<String[]> v = new ArrayList<String[]>();
      rp.header(v);
      if (!v.isEmpty()) {
        Iterator it = v.iterator();
        while (it.hasNext() && !response.isCommitted()) {
          String[] h = (String[]) it.next();
          if (h[0].equals(RequestProcessor.ERROR)) {
            int code = Integer.parseInt(h[1]);
            response.sendError(code, h[2]);
          }
          if (h[0].equals(RequestProcessor.REDIRECT)) {
            response.sendRedirect(h[1]);
            break;
          } else if (h[0].equals(RequestProcessor.CONTENT_TYPE))
            response.setContentType(h[1]);
          else if (h[0].equals(RequestProcessor.CONTENT_LENGTH))
            response.setContentLength(Integer.parseInt(h[1]));
          else if (h[0].equals(RequestProcessor.EXTRA))
            response.setHeader(h[1], h[2]);
          else if (h[0].equals(RequestProcessor.COOKIE))
            response.addCookie(new Cookie(h[1], h[2]));
        }
      }
      if (!response.isCommitted()) {
        if (rp.usesWriter()) {
          java.io.PrintWriter pw = response.getWriter();
          rp.process(pw);
          try {
            pw.flush();
            pw.close();
          } catch (Exception ex) {
            // ...
          }
        } else {
          java.io.OutputStream os = response.getOutputStream();
          rp.process(os);
          try {
            os.flush();
            os.close();
          } catch (Exception ex) {
            // ...
          }
        }
      }
      rp.end();
      rp = null;
    } catch (Exception ex) {
      int errCode = rp != null && rp.errCode >= 0 ? rp.errCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      String errMsg = rp != null && rp.errMsg != null ? rp.errMsg : ex.getMessage();
      if (!response.isCommitted()) {
        response.sendError(errCode, errMsg);
      }
      System.err.println("ERROR " + errCode + ": " + errMsg);
      ex.printStackTrace(System.err);
    } finally {
      if (rp != null)
        rp.end();
    }
  }

  protected abstract RequestProcessor createRP() throws Exception;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    processRequest(request, response);
  }
}
