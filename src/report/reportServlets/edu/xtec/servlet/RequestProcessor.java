/*
 * File    : RequestProcessor.java
 * Created : 23-jan-2003 10:31
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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public abstract class RequestProcessor {

  public static final String REDIRECT = "REDIRECT",
      CONTENT_TYPE = "CONTENT_TYPE",
      CONTENT_LENGTH = "CONTENT_LENGTH",
      COOKIE = "COOKIE",
      EXTRA = "EXTRA",
      ERROR = "ERROR";
  public static final int HTTP_NOT_FOUND = 404, HTTP_BAD_REQUEST = 400, HTTP_UNAUTHORIZED = 401;
  public static final String CHARSET = "ISO-8859-1";
  private Map<String, Object> params;
  private Map<String, Object> cookies;
  private Map<String, Object> headers;
  private InputStream inputStream;
  public int errCode;
  public String errMsg;
  private static boolean DIRECT_RESOURCES;
  protected static Map<String, Object> properties = new HashMap<String, Object>();

  /** Creates a new instance of RequestProcessor */
  public RequestProcessor() {
    params = new HashMap<String, Object>();
    cookies = new HashMap<String, Object>();
    headers = new HashMap<String, Object>();
    inputStream = null;
    errCode = -1;
    errMsg = null;
  }

  public static void config(Map<String, Object> prop) {
    if (prop != null) {
      properties.putAll(prop);
    }
  }

  public boolean init() throws Exception {
    return true;
  }

  public boolean usesWriter() {
    return true;
  }

  public boolean noCache() {
    return true;
  }

  public void process(java.io.OutputStream out) throws Exception {}

  public void process(java.io.PrintWriter out) throws Exception {
    startHead(out);
    head(out);
    endHead(out);
    startBody(out);
    body(out);
    endBody(out);
  }

  public void header(List<String[]> v) {
    if (cookies.size() > 0) {
      for (String key : cookies.keySet()) {
        String value = (String) cookies.get(key);
        v.add(new String[] {COOKIE, key, value});
      }
    }
  }

  public void end() {}

  public boolean wantsInputStream() {
    return false;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(InputStream is) {
    inputStream = is;
  }

  public int getIntParam(String name, int defaultValue) {
    int result = defaultValue;
    String s = getParam(name);
    if (s != null) {
      try {
        result = Integer.parseInt(s);
      } catch (NumberFormatException ex) {
      }
    }
    return result;
  }

  public Date getDateParam(String name, Date defaultValue, boolean atMidnight) {
    Date result = defaultValue;
    String s = getParam(name);
    if (s != null) {
      try {
        result = edu.xtec.jclic.report.ReportUtils.strToDate(s, atMidnight);
      } catch (Exception ex) {
      }
    }
    return result;
  }

  public String getParamNotNull(String name) {
    String result = getParam(name, 0);
    if (result == null) {
      result = "";
    }
    return result.trim();
  }

  public String getParam(String name) {
    return getParam(name, 0);
  }

  public Object[] getParams(String name) {
    return (Object[]) params.get(name);
  }

  public String getParam(String name, int index) {
    String result = null;
    Object[] array = (Object[]) params.get(name);
    if (index >= 0 && array != null && array.length > index) {
      result = (String) array[index];
    }
    return result;
  }

  public void setParam(String name, String value) {
    params.put(name, new String[] {value});
  }

  public void setParam(String name, String[] value) {
    params.put(name, value);
  }

  public void setParams(Map<String, Object> map) {
    // params.putAll(map);
    if (map != null) {
      for (String s : map.keySet()) {
        Object val = map.get(s);
        if (s != null) {
          if (val instanceof String) {
            setParam(s.toString(), (String) val);
          } else if (val instanceof String[]) {
            setParam(s.toString(), (String[]) val);
          }
        }
      }
    }
  }

  public String getCookie(String name) {
    return (String) cookies.get(name);
  }

  public void setCookie(String name, String value) {
    cookies.put(name, value);
  }

  public void setCookies(Map<String, Object> map) {
    cookies.putAll(map);
  }

  public void setHeaders(Map<String, Object> map) {
    headers.putAll(map);
  }

  public java.util.Vector getHeaders(String headerName) {
    return (java.util.Vector) headers.get(headerName.toLowerCase());
  }

  public String getHeader(String headerName) {
    String result = null;
    java.util.Vector v = getHeaders(headerName);
    if (v != null && !v.isEmpty()) {
      result = (String) v.get(0);
    }
    return result;
  }

  protected void startHead(java.io.PrintWriter out) {
    out.println("<html>");
    out.println("<head>");
    out.print("<meta http-equiv=\"Content-Type\" content=\"text/html; ");
    out.print(CHARSET);
    out.println("\">");
  }

  protected void head(java.io.PrintWriter out) throws Exception {}

  protected void endHead(java.io.PrintWriter out) {
    out.println("</head>");
  }

  protected void linkStyle(String fileScreen, String filePrint, java.io.PrintWriter out) {
    StringBuilder sb = new StringBuilder();
    sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    sb.append(fileScreen);
    if (filePrint != null) {
      sb.append("\" media=\"screen\">\n");
      sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
      sb.append(filePrint).append("\" media=\"print\">");
    } else {
      sb.append("\">");
    }

    out.println(sb.substring(0));
  }

  protected void linkScript(String file, java.io.PrintWriter out) {
    out.print("<script language=\"JavaScript\" src=\"");
    out.print(file);
    out.println("\" type=\"text/javascript\"></script>");
  }

  protected void writeScript(String text, java.io.PrintWriter out) {
    out.println("<script language=\"JavaScript\" type=\"text/javascript\">");
    out.println("<!--");
    out.println(text);
    out.println("// -->");
    out.println("</script>");
  }

  protected void title(String prefix, String title, java.io.PrintWriter out) {
    StringBuilder sb = new StringBuilder(200);
    sb.append("<title>");
    if (prefix != null) {
      sb.append(filter(prefix)).append(" - ");
    }
    sb.append(filter(title));
    sb.append("</title>");
    out.println(sb.substring(0));
  }

  protected void startBody(java.io.PrintWriter out) {
    out.println("<body>");
  }

  protected void body(java.io.PrintWriter out) throws Exception {}

  protected void endBody(java.io.PrintWriter out) {
    out.println("</body>");
    out.println("</html>");
  }

  public static String filter(String input) {
    String result = (input == null ? "" : input);
    if (input != null && input.length() > 0) {
      StringBuilder filtered = new StringBuilder(input.length());
      char c;
      for (int i = 0; i < input.length(); i++) {
        c = input.charAt(i);
        String s = null;
        switch (c) {
          case '<':
            s = "&lt;";
            break;
          case '>':
            s = "&gt;";
            break;
          case '"':
            s = "&quot;";
            break;
          case '&':
            s = "&amp;";
            break;
        }
        if (s != null) {
          filtered.append(s);
        } else {
          filtered.append(c);
        }
      }
      result = filtered.substring(0);
    }
    return result;
  }

  public static String escape(String input) {
    String result = input;
    if (input != null && input.length() > 0) {
      StringBuilder filtered = new StringBuilder(input.length());
      char c;
      for (int i = 0; i < input.length(); i++) {
        c = input.charAt(i);
        String s = null;
        switch (c) {
          case '\'':
            // s="&#39;"
            s = "\\'";
            break;
          case '\\':
            s = "\\\\";
            break;
        }
        if (s != null) {
          filtered.append(s);
        } else {
          filtered.append(c);
        }
      }
      result = filtered.substring(0);
    }
    return result;
  }

  public static String toNbsp(String src) {
    StringBuilder sb = new StringBuilder(src == null ? 0 : src.length() * 2);
    if (src != null) {
      StringTokenizer st = new StringTokenizer(src);
      if (st.hasMoreTokens()) {
        while (true) {
          sb.append(st.nextToken());
          if (st.hasMoreTokens()) {
            sb.append("&nbsp;");
          } else {
            break;
          }
        }
      }
    }
    return sb.substring(0);
  }

  public static void setDirectResources(boolean v) {
    DIRECT_RESOURCES = v;
  }

  protected static String resourceUrl(String resource) {
    String result = resource;
    if (!DIRECT_RESOURCES) {
      StringBuilder sb = new StringBuilder(50);
      sb.append("resource?id=").append(resource);
      result = sb.substring(0);
    }
    return result;
  }

  private static SimpleDateFormat HTTP_DF;

  public static String httpDate(Date date) {
    if (HTTP_DF == null) {
      HTTP_DF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
      HTTP_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    return HTTP_DF.format(date);
  }
}
