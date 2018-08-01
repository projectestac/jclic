/*
 * File    : Login.java
 * Created : 23-jan-2003 12:36
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

package edu.xtec.jclic.report.rp;

import java.util.ResourceBundle;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public class Login extends Basic {

  public static String TITLE = "login_title";
  public static String URL = "login";

  public Login() {
    super();
  }

  public String getTitle(ResourceBundle bundle) {
    return bundle.getString(TITLE);
  }

  public String getUrl() {
    return URL;
  }

  @Override
  protected boolean checkAuth() {
    return true;
  }

  @Override
  public void body(java.io.PrintWriter out) throws Exception {
    super.body(out);
    standardHeader(out, getMsg("login_title"), "");
    StringBuilder sb = new StringBuilder(2000);
    if (TRUE.equals(getParam(RETRY)))
      sb.append("<p><strong>").append(getMsg("login_incorrect")).append("</strong></p>\n");
    sb.append("<p>").append(getMsg("login_desc")).append("</p>\n");
    sb.append("<form class=\"inputForm\" action=\"main\" method=\"post\">\n");
    sb.append("<input type=\"hidden\" name=\"")
        .append(LANG)
        .append("\" value=\"")
        .append(lang)
        .append("\">\n");
    sb.append("<p>")
        .append(getMsg("login_pwd"))
        .append(" <input type=\"password\" length=\"20\" name=\"")
        .append(PWD)
        .append("\"></p>\n");
    sb.append("<p><input type=\"submit\" value=\"").append(getMsg("submit")).append("\"></p>\n");
    sb.append("</form>");
    out.println(sb.substring(0));
  };
}
