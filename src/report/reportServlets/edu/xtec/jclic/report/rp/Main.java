/*
 * File    : Main.java
 * Created : 23-jan-2003 11:43
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
public class Main extends Basic {

    public static String TITLE = "main_title";
    public static String URL = "main";

    public String getTitle(ResourceBundle bundle) {
        return bundle.getString(TITLE);
    }

    public String getUrl() {
        return URL;
    }

    @Override
    public void body(java.io.PrintWriter out) throws Exception {
        super.body(out);
        standardHeader(out, getMsg("main_title"), "");
        StringBuilder sb = new StringBuilder(3000);
        sb.append("<ul class=\"menu\">\n");
        if (bridge.hasUserTables()) {
            sb.append("<li>")
                    .append(linkTo(urlParam(GroupAdmin.URL, LANG, lang), bundle.getString(GroupAdmin.TITLE), null))
                    .append("</li>\n");
            sb.append("<li>")
                    .append(linkTo(urlParam(GroupReport.URL, LANG, lang), bundle.getString(GroupReport.TITLE), null))
                    .append("</li>\n");
            sb.append("<li>")
                    .append(linkTo(urlParam(UserReport.URL, LANG, lang), bundle.getString(UserReport.TITLE), null))
                    .append("</li>\n");
        }
        sb.append("<li>").append(linkTo(urlParam(ActReport.URL, LANG, lang), bundle.getString(ActReport.TITLE), null))
                .append("</li>\n");
        sb.append("<li>").append(linkTo(urlParam(DbAdmin.URL, LANG, lang), bundle.getString(DbAdmin.TITLE), null))
                .append("</li>\n");
        sb.append("</ul>");
        out.println(sb.substring(0));
    };
}
