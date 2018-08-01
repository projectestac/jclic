/*
 * File    : UserReport.java
 * Created : 25-jan-2003 18:52
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

import edu.xtec.jclic.report.SessionData;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class UserReport extends Report {

  public static String TITLE = "user_report_title";
  public static String URL = "userReport";
  List<SessionData> sessionList;

  public String getTitle(ResourceBundle bundle) {
    return bundle.getString(TITLE);
  }

  public String getUrl() {
    return URL;
  }

  @Override
  public boolean init() throws Exception {

    type = USR;

    if (!super.init())
      return false;

    projects = bridge.getProjList(userId, dm.dFrom, dm.dTo, kcc);

    sessionList = getSessionList();

    if (session == null && sessionList != null && sessionList.size() > 0) {
      session = new Object[sessionList.size()];
      for (int i = 0; i < sessionList.size(); i++)
        session[i] = FALSE;
    }

    if (session != null && session.length > 0) {
      for (int i = 0; i < session.length; i++) {
        if (TRUE.equals(session[i])) {
          SessionData sd = (SessionData) sessionList.get(i);
          if (sd != null && sd.id != null && sd.id.length() > 0)
            sd.actData = bridge.getPacSessionList(null, null, sd.id, dm.dFrom, dm.dTo, kcc);
        }
      }
    }

    return true;
  }

  @Override
  public void head(java.io.PrintWriter out) throws Exception {
    super.head(out);
    StringBuilder sb = new StringBuilder(200);
    writeSessionScript(sb);
    out.println(sb.substring(0));
  }

  @Override
  public void body(java.io.PrintWriter out) throws Exception {

    super.body(out);
    StringBuilder sb = new StringBuilder(3000);

    sb.append("<div class=\"inputForm\">\n");
    zona(sb, "report_group", GROUP, true, opcioDefecte, vectorToArray(groups, true), groupId, isEditable, 180);
    zona(sb, "report_user", USER, true, opcioDefecte, vectorToArray(users, true), userId, isEditable, 180);
    zona(sb, "report_project", PROJECT, true, opcioDefecte,
        vectorToArray(projects, false, WILDCARD, "report_all_projects"), projectName, isEditable, 180);
    sb.append("</div>\n");

    zonaParams(sb);

    sb.append("<div class=\"inputForm\">\n");
    zonaData(sb);
    sb.append("</div>\n");
    sb.append("</form>\n");
    sb.append("<br clear=\"all\">\n");

    sb.append("<p>\n");
    if (sessionList.isEmpty()) {
      sb.append(getMsg("report_no_data")).append("\n</p>\n");
    } else {
      grafic(sb, Img.USER_GRAPH, false, true);
      sb.append("\n");
      grafic(sb, Img.USER_GRAPH, true, true);
      sb.append("\n</p>\n");

      resumGlobal(sb, sessionList, "tblA", "float: left; margin-right: 10px;");

      llistaSessions(sb, sessionList, true, "tblA", null);
    }

    out.println(sb.substring(0));
  }
}
