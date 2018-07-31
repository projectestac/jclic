/*
 * File    : Report.java
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

import edu.xtec.jclic.report.ActivityData;
import edu.xtec.jclic.report.GroupData;
import edu.xtec.jclic.report.ReportUtils;
import edu.xtec.jclic.report.SessionData;
import edu.xtec.jclic.report.UserData;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public abstract class Report extends BasicReport {

  public static final int DEFAULT_SELECT_WIDTH = 180;

  public static final String SESSION = "session";

  protected boolean isEditable = true;
  protected boolean userTables = true;
  protected String change;
  protected String opcioDefecte;
  protected Object[] session;
  protected List<GroupData> groups;
  protected List<UserData> users;
  protected List projects;

  @Override
  public boolean init() throws Exception {

    if (!super.init()) return false;

    isEditable = !(FALSE.equalsIgnoreCase(getParamNotNull(EDIT)));
    userTables = bridge.hasUserTables();

    if (userTables) {
      groups = bridge.getGroups();
      if (groupId.length() > 0) users = bridge.getUsers(groupId);
    }

    session = getParams(SESSION);

    change = getParam(CHANGE);
    switch (GROUP.equals(change)
        ? 0
        : USER.equals(change)
            ? 1
            : DATE.equals(change)
                ? 2
                : PROJECT.equals(change) ? 3 : ACTIVITY.equals(change) ? 4 : 5) {
      case 0:
        if (users != null && users.size() > 0) userId = ((UserData) users.get(0)).getId();
        else userId = "";
      case 1:
        projectName = WILDCARD;
      case 2:
      case 3:
        session = null;
        activityName = WILDCARD;
      case 4:
      default:
        break;
    }

    return true;
  }

  @Override
  public void head(java.io.PrintWriter out) throws Exception {
    super.head(out);
    StringBuilder sb = new StringBuilder(300);
    writeGoScript(sb);
    dm.writeDateScript(sb);
    out.println(sb.substring(0));
  }

  @Override
  public void body(java.io.PrintWriter out) throws Exception {

    super.body(out);
    StringBuilder sb = new StringBuilder(2000);

    if (isEditable)
      sb.append(linkTo(urlParam(Main.URL, LANG, lang), bundle.getString(Main.TITLE), null));

    standardHeader(out, filter(getTitle(bundle)), sb.substring(0));

    sb.setLength(0);
    sb.append("<form action=\"")
        .append(getUrl())
        .append("\" method=\"post\" name=\"")
        .append(MAIN_FORM)
        .append("\">\n");
    sb.append("<input type=\"hidden\" name=\"")
        .append(CHANGE)
        .append("\" value=\"")
        .append(NEW)
        .append("\">\n");
    sb.append("<input type=\"hidden\" name=\"")
        .append(LANG)
        .append("\" value=\"")
        .append(lang)
        .append("\">\n");
    dm.writeHiddenFields(sb);
    if (session != null) {
      for (int i = 0; i <= session.length; i++) {
        sb.append("<input type=\"hidden\" name=\"").append(SESSION).append("\" value=\"");
        sb.append(i < session.length && session[i] != null ? (String) session[i] : FALSE)
            .append("\">\n");
      }
    }
    if (!isEditable)
      sb.append("<input type=\"hidden\" name=\"")
          .append(EDIT)
          .append("\" value=\"")
          .append(FALSE)
          .append("\">\n");

    out.println(sb.substring(0));
  }

  protected void writeGoScript(StringBuilder sb) {
    sb.append("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("<!--\n");
    sb.append(" function go(canvi){\n");
    sb.append("   document.")
        .append(MAIN_FORM)
        .append(".")
        .append(CHANGE)
        .append(".value=canvi;\n");
    sb.append("   document.").append(MAIN_FORM).append(".submit();\n");
    sb.append(" };\n");
    sb.append("//-->\n");
    sb.append("</script>\n");
  }

  protected void writeSessionScript(StringBuilder sb) {
    sb.append("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("<!--\n");
    sb.append(" function session(id,v){\n");
    sb.append("     document.")
        .append(MAIN_FORM)
        .append(".")
        .append(SESSION)
        .append("[id].value=v\n");
    sb.append("     go('").append(SESSION).append("');\n");
    sb.append(" };\n");
    sb.append("//-->\n");
    sb.append("</script>\n");
  }

  protected void llista(
      StringBuilder sb,
      String selectName,
      boolean submitOnChange,
      String defaultOption,
      String[][] values,
      String selectedValue,
      int width) {
    /* Mostra una llista desplegable (objecte SELECT) amb opcions
     */
    sb.append("<select name=\"").append(selectName);
    if (width > 0) sb.append("\" style=\"width:").append(width).append("px\"");
    sb.append(" size=\"1\"");
    if (submitOnChange) {
      sb.append(" onChange=\"go('").append(selectName).append("')\"");
    }
    sb.append(">\n");

    if (defaultOption != null)
      sb.append("<option>").append(filter(defaultOption)).append("</option>\n");

    if (values != null && values.length > 0) {
      for (int i = 0; i < values.length; i++) {
        sb.append("<option value=\"").append(filter(values[i][0])).append("\"");
        if (selectedValue != null && selectedValue.equals(values[i][0])) sb.append(" selected");
        sb.append(">").append(filter(values[i][1])).append("</option>\n");
      }
    } else {
      sb.append("<option>");
      for (int i = 0; i < 27; i++) sb.append("&nbsp;");
      sb.append("</option>\n");
    }
    sb.append("</select>\n");
  }

  protected void zona(
      StringBuilder sb,
      String descKey,
      String selectName,
      boolean submitOnChange,
      String defaultOption,
      String[][] values,
      String selectedValue,
      boolean showList,
      int width) {
    sb.append("<p>").append(filter(getMsg(descKey))).append("&nbsp;");
    if (showList && values != null)
      llista(sb, selectName, submitOnChange, defaultOption, values, selectedValue, width);
    else {
      sb.append("<input type=\"hidden\" name=\"").append(selectName).append("\"");
      if (selectedValue != null) sb.append(" value=\"").append(filter(selectedValue)).append("\"");
      sb.append("/>\n");
      if (selectedValue != null && values != null) {
        for (int i = 0; i < values.length; i++) {
          if (selectedValue.equals(values[i][0])) {
            sb.append(filter(values[i][1])).append("\n");
            break;
          }
        }
      }
    }
    sb.append("</p>\n");
  }

  protected void zonaData(StringBuilder sb) {

    StringBuilder sb2 = new StringBuilder(100);
    sb2.append("go('").append(DATE).append("')");
    dm.zonaData(sb, buttonAction(sb2.substring(0), getMsg("submit"), null));
  }

  public static final String[] KCC_LABELS = {"report_key", "report_code", "report_context"};

  protected void zonaParams(StringBuilder sb) {
    sb.append("<div class=\"inputForm\" id=\"compact\">\n");
    for (int i = 0; i < KCC.length; i++) {
      sb.append("<p>");
      // zonaParam(sb, KCC[i], kcc[i], KCC_LABELS[i], !isEditable);
      if (isEditable) sb.append(filter(getMsg(KCC_LABELS[i]))).append("&nbsp;");
      sb.append("<input name=\"").append(KCC[i]).append("\" size=\"8\"");
      if (kcc[i] != null) sb.append(" value=\"").append(filter(kcc[i])).append("\"");
      if (!isEditable) sb.append(" type=\"hidden\"");
      sb.append("></p>\n");
    }
    sb.append("</div>\n");
  }

  protected void grafic(StringBuilder sb, String type, boolean dist, boolean withHeader)
      throws Exception {

    int w = dist ? Img.DIST_WIDTH : Img.DEFAULT_WIDTH;
    int h = Img.DEFAULT_HEIGHT + Img.DEFAULT_HEADER_HEIGHT;

    StringBuilder urlG = new StringBuilder(300);
    urlG.append(Img.URL);
    urlParamSb(urlG, Img.TYPE, type, true);
    if (dist) urlParamSb(urlG, Img.DIST, TRUE, false);
    if (withHeader) urlParamSb(urlG, Img.HEADER, TRUE, false);

    urlParamSb(urlG, Img.WIDTH, Integer.toString(w), false);
    urlParamSb(urlG, Img.HEIGHT, Integer.toString(h), false);

    urlParamSb(urlG, PID, pageId, false);

    urlParamSb(urlG, USER, userId, false);
    urlParamSb(urlG, GROUP, groupId, false);
    if (projectName.length() > 0 && !projectName.equals(WILDCARD))
      urlParamSb(urlG, PROJECT, projectName, false);
    if (activityName.length() > 0 && !activityName.equals(WILDCARD))
      urlParamSb(urlG, ACTIVITY, activityName, false);
    for (int i = 0; i < KCC.length; i++) urlParamSb(urlG, KCC[i], kcc[i], false);
    urlParamSb(urlG, DateManager.FROM, ReportUtils.dateToStr(dm.dFrom), false);
    urlParamSb(urlG, DateManager.TO, ReportUtils.dateToStr(dm.dTo), false);

    sb.append("<img src=\"")
        .append(urlG)
        .append("\" width=\"")
        .append(w)
        .append("\" height=\"")
        .append(h)
        .append("\">");
  }

  protected void llistaSessions(
      StringBuilder sb, List<SessionData> v, boolean expandable, String className, String style)
      throws Exception {

    // String cl=" CLASS=\"T1\"";
    // String clh=" CLASS=\"T1header\"";

    // Html html=new Html(sb);
    int n = 0;

    sb.append("<table class=\"").append(className).append("\"");
    if (style != null) sb.append(" style=\"").append(style).append("");
    sb.append(">\n");

    sb.append("<tr>");
    sb.append("<th>").append(toNbsp(getMsg("report_header_date"))).append("</th>\n");
    n++;
    if (type == PRJ || type == GRP) {
      sb.append("<th>").append(toNbsp(getMsg("report_header_user"))).append("</th>\n");
      sb.append("<th>").append(toNbsp(getMsg("report_header_sessions"))).append("</th>\n");
      n += 2;
    }
    if (type == USR) {
      sb.append("<th colspan=2>").append(toNbsp(getMsg("report_header_project"))).append("</th>\n");
      n += 2;
    }
    sb.append("<th>").append(toNbsp(getMsg("report_header_numActs"))).append("</th>\n");
    sb.append("<th>").append(toNbsp(getMsg("report_header_actsSolved"))).append("</th>\n");
    sb.append("<th>").append(toNbsp(getMsg("report_header_time"))).append("</th>\n");
    sb.append("<th>").append(toNbsp(getMsg("report_header_prec"))).append("</th>\n");
    n += 4;
    sb.append("</tr>\n");
    // html.tr(false);

    StringBuilder sb2 = new StringBuilder(500);
    StringBuilder sb3 = new StringBuilder(100);
    int numSess = 0;
    Iterator<SessionData> it = v.iterator();
    while (it.hasNext()) {
      SessionData sd = it.next();
      sb.append("<tr>\n");
      sb2.setLength(0);
      boolean link = expandable && sd.id != null && sd.id.length() > 0;
      boolean status = link && sd.actData != null;
      if (expandable) {
        if (link) {
          sb2.append("<a href=\"javascript:session(")
              .append(numSess)
              .append(",'")
              .append(status ? FALSE : TRUE)
              .append("')\">");
          sb2.append("<img src=\"").append(resourceUrl(status ? "menys.gif" : "mes.gif"));
          sb2.append("\" width=\"9\" height=\"9\" border=\"0\" alt=\"");
          sb2.append(getMsg("report_session_detail_" + (status ? "hide" : "show")))
              .append("\" id=\"noPrint\">");
          sb2.append("</a>&nbsp;");
        } else sb2.append("&nbsp;&nbsp;");
      }
      sb2.append(shortDateFormat.format(sd.date));
      sb3.setLength(0);
      // sb3.append(cl);
      if (status)
        sb3.append(" valign=\"top\" rowspan=\"").append(sd.actData.size() + 2).append("\"");

      sb.append("<td align=\"right\"")
          .append(sb3.substring(0))
          .append(">")
          .append(sb2.substring(0))
          .append("</td>\n");
      // html.td(sb2.substring(0), Html.LEFT, false, sb3.substring(0));

      if (type == GRP || type == PRJ) {
        String s = sd.getUsr();
        if ("*".equals(s)) {
          sb2.setLength(0);
          sb2.append("(")
              .append(numberFormat.format(sd.users.size()))
              .append(" ")
              .append(bundle.getString("report_n_users"))
              .append(")");
          s = sb2.substring(0);
        }
        sb.append("<td align=\"right\">").append(filter(s)).append("</td>\n");
        // html.td(filter(s), Html.RIGHT, false, cl);
        sb.append("<td align=\"right\">")
            .append(numberFormat.format(sd.sessionCount))
            .append("</td>\n");
        // html.td(Integer.toString(sd.sessionCount), Html.RIGHT, false, cl);
      }
      if (type == USR) {
        sb.append("<td align=\"left\" colspan=2>").append(filter(sd.project)).append("</td>\n");
        // html.td(filter(sd.project), Html.LEFT, false, "COLSPAN=\"2\" "+cl);
      }
      sb.append("<td align=\"right\">").append(numberFormat.format(sd.numActs)).append("</td>\n");
      // html.td(Integer.toString(sd.numActs), Html.RIGHT, false, cl);
      sb.append("<td align=\"right\">")
          .append(numberFormat.format(sd.actsSolved))
          .append("&nbsp;(")
          .append(numberFormat.format(sd.percentSolved()))
          .append("%)</td>\n");
      // html.td(Integer.toString(sd.actsSolved)+"&nbsp;("+sd.percentSolved()+"%)", Html.RIGHT,
      // false, cl);
      sb.append("<td align=\"right\">").append(formatTime(sd.totalTime)).append("</td>\n");
      // html.td(formatTime(sd.totalTime), Html.RIGHT, false, cl);
      sb.append("<td align=\"right\">")
          .append(numberFormat.format(sd.percentPrec()))
          .append("%</td>\n");
      // html.td(Integer.toString(sd.percentPrec())+"%", Html.RIGHT, false, cl);
      sb.append("</tr>\n");
      // html.tr(false);
      if (status) {
        llistaActivitats(sb, sd.actData);
      }
      numSess++;
    }
    sb.append("</table>");
  }

  protected void llistaActivitats(StringBuilder sb, List<ActivityData> v) throws Exception {

    // String cl=" CLASS=\"T2\"";
    // String clh=" CLASS=\"T2header\"";
    // String clGreen=" CLASS=\"Tgreen\"";
    // String clRed=" CLASS=\"Tred\"";

    // Html html=new Html(sb);
    sb.append("<tr id=\"t2x\">\n");
    // html.tr(true);

    sb.append("<th>").append(toNbsp(getMsg("report_header_activity"))).append("</th>\n");
    // html.td(toNbsp(getMsg("report_header_activity")), Html.LEFT, true, clh);
    if (type == PRJ || type == GRP) {
      sb.append("<th>").append(toNbsp(getMsg("report_header_user"))).append("</th>\n");
      // html.td(toNbsp(getMsg("report_header_user")), Html.LEFT, true, clh);
    }
    if (type == GRP) {
      sb.append("<th>").append(toNbsp(getMsg("report_header_project"))).append("</th>\n");
      // html.td(toNbsp(getMsg("report_header_project")), Html.LEFT, true, clh);
    }
    sb.append("<th>").append(toNbsp(getMsg("report_header_solved"))).append("</th>\n");
    // html.td(toNbsp(getMsg("report_header_solved")), Html.RIGHT, true, clh);
    sb.append("<th>").append(toNbsp(getMsg("report_header_actions"))).append("</th>\n");
    // html.td(toNbsp(getMsg("report_header_actions")), Html.RIGHT, true, clh);
    sb.append("<th>").append(toNbsp(getMsg("report_header_score"))).append("</th>\n");
    // html.td(toNbsp(getMsg("report_header_score")), Html.RIGHT, true, clh);
    sb.append("<th>").append(toNbsp(getMsg("report_header_time"))).append("</th>\n");
    // html.td(toNbsp(getMsg("report_header_time")), Html.RIGHT, true, clh);
    sb.append("<th>").append(toNbsp(getMsg("report_header_prec"))).append("</th>\n");
    // html.td(toNbsp(getMsg("report_header_prec")), Html.RIGHT, true, clh);
    sb.append("</tr>\n");
    // html.tr(false);

    Iterator<ActivityData> it = v.iterator();
    while (it.hasNext()) {
      ActivityData ad = it.next();
      sb.append("<tr id=\"t2x\">\n");
      // html.tr(true);
      sb.append("<td>").append(filter(ad.activityName)).append("</td>\n");
      // html.td(filter(ad.activityName), Html.LEFT, false, cl);
      if (type == PRJ || type == GRP) {
        sb.append("<td></td>\n");
        // html.td("", Html.LEFT, false, cl);
      }
      if (type == GRP) {
        sb.append("<td>").append(filter(ad.project)).append("</td>\n");
        // html.td(filter(ad.project), Html.LEFT, false, cl);
      }
      sb.append("<td id=\"").append(ad.solved ? "green" : "red").append("\">");
      sb.append(getMsg(ad.solved ? "YES_SHORT" : "NOT_SHORT")).append("</td>\n");
      // html.td(getMsg(ad.solved ? "Y" : "N"), Html.CENTER, false, ad.solved ? clGreen : clRed);

      sb.append("<td align=\"right\">").append(numberFormat.format(ad.actions)).append("</td>\n");
      // html.td(Integer.toString(ad.actions), Html.RIGHT, false, cl);
      sb.append("<td align=\"right\">")
          .append(numberFormat.format(ad.score))
          .append("&nbsp;(")
          .append(numberFormat.format(ad.percentSolved()))
          .append("%)</td>\n");
      // html.td(Integer.toString(ad.score)+"&nbsp;("+ad.percentSolved()+"%)", Html.RIGHT, false,
      // cl);
      sb.append("<td align=\"right\">").append(formatTime(ad.time)).append("</td>\n");
      // html.td(formatTime(ad.time), Html.RIGHT, false, cl);
      sb.append("<td align=\"right\">")
          .append(numberFormat.format(ad.qualification))
          .append("%</td>\n");
      // html.td(Integer.toString(ad.qualification)+"%", Html.RIGHT, false, cl);
      sb.append("</tr>\n");
      // html.tr(false);
    }
  }

  protected void resumGlobal(StringBuilder sb, List<SessionData> v, String className, String style)
      throws Exception {

    int sess = 0, usrs, numProjects = 0, act_fetes = 0, act_res = 0, temps = 0, precisio = 0;

    if (!projectName.equals(WILDCARD)) numProjects = 1;
    else if (projects != null) {
      numProjects = projects.size();
      if (projects.size() > 0 && WILDCARD.equals(projects.get(0))) numProjects--;
    }

    Set<String> hs = new HashSet<String>();
    Iterator<SessionData> it = v.iterator();
    while (it.hasNext()) {
      SessionData sd = it.next();
      act_fetes += sd.numActs;
      act_res += sd.actsSolved;
      temps += sd.totalTime;
      precisio += sd.totalPrec;
      sess += sd.sessionCount;
      hs.addAll(sd.users);
    }
    usrs = hs.size();

    sb.append("<table class=\"").append(className).append("\"");
    if (style != null) sb.append(" style=\"").append(style).append("\"");
    sb.append(">\n");
    sb.append("<tr><th colspan=2>")
        .append(filter(getMsg("report_globalSummary")))
        .append("</th></tr>\n");
    if (usrs > 1) resumTD(sb, getMsg("report_users"), numberFormat.format(usrs));
    resumTD(sb, getMsg("report_sessions"), numberFormat.format(sess));
    if (type != PRJ) resumTD(sb, getMsg("report_projects"), numberFormat.format(numProjects));
    resumTD(sb, getMsg("report_actDone"), numberFormat.format(act_fetes));
    StringBuilder sb2 = new StringBuilder(200);
    sb2.append(numberFormat.format(act_res))
        .append(" (")
        .append(numberFormat.format(act_fetes == 0 ? 0 : 100 * act_res / act_fetes))
        .append("%)");
    resumTD(sb, getMsg("report_actSolved"), sb2.substring(0));
    resumTD(sb, getMsg("report_totalTime"), formatTime(temps));
    sb2.setLength(0);
    sb2.append(numberFormat.format(act_fetes == 0 ? 0 : precisio / act_fetes)).append("%");
    resumTD(sb, getMsg("report_globalPrec"), sb2.substring(0));
    sb.append("</table>\n");
  }

  protected void resumTD(StringBuilder sb, String td1, String td2) {
    sb.append("<tr><td>").append(toNbsp(td1));
    sb.append("</td><td align=\"right\">").append(toNbsp(td2)).append("</td></tr>\n");
  }

  public String formatTime(int secs) {
    secs = Math.max(0, secs);
    String[] n = getFormattedNumbers();
    int h = secs / (60 * 60);
    int m = (secs - (h * 60 * 60)) / 60;
    int s = secs % 60;
    StringBuilder sb = new StringBuilder(8);
    if (h > 0) sb.append(h).append(":");
    sb.append(n[m]).append(":").append(n[s]);
    return sb.substring(0);
  }
}
