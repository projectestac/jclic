/*
 * File    : GroupAdmin.java
 * Created : 14-feb-2003 15:53
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

import edu.xtec.jclic.report.GroupData;
import edu.xtec.jclic.report.UserData;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public class GroupAdmin extends Basic {

  public static String TITLE = "group_admin_title";
  public static String URL = "groupAdmin";

  public static final String GROUP = "group", ID = "id", NAME = "name", ICON = "icon";
  public static final int MENU = 0,
      GROUP_MENU = 1,
      EDIT = 2,
      EDIT_UPDATE = 3,
      CREATE = 4,
      CREATE_UPDATE = 5,
      DELETE = 6,
      DELETE_UPDATE = 7,
      CLEAR = 8,
      CLEAR_UPDATE = 9,
      ERR = 10;

  public static final String[] ACTIONS = {
    "", "group", "edit", "editUpd", "create", "createUpd", "del", "delUpd", "clear", "clearUpd"
  };

  protected int page;
  protected DateManager dm;
  protected List<GroupData> groups;
  protected List<UserData> users;
  protected GroupData gd;

  protected String returnUrl;

  public String getTitle(ResourceBundle bundle) {
    StringBuilder sb = new StringBuilder(200);
    sb.append(bundle.getString(TITLE));
    if (gd != null && gd.getText() != null && gd.getText().length() > 0)
      sb.append(": ").append(filter(gd.getText()));
    return sb.substring(0);
  }

  public String getUrl() {
    return urlParam(URL, LANG, lang);
  }

  public String getUrl(int action) {
    StringBuilder sb = new StringBuilder(300);
    sb.append(URL);
    urlParamSb(sb, LANG, lang, true);
    urlParamSb(sb, ACTION, ACTIONS[action], false);
    if (gd != null) urlParamSb(sb, GROUP, gd.getId(), false);
    return sb.substring(0);
  }

  @Override
  public boolean init() throws Exception {
    if (!super.init()) return false;

    String gId = getParamNotNull(GROUP);
    if (gId.length() > 0) gd = bridge.getGroupData(gId);

    page = getAction(ACTIONS, MENU);

    if (page == CLEAR || page == CLEAR_UPDATE) {
      dm = new DateManager(this);
      if (!dm.init()) return false;
    }

    switch (page) {
      case EDIT_UPDATE:
        edit();
        break;
      case CREATE_UPDATE:
        create();
        break;
      case DELETE_UPDATE:
        delete();
        break;
      case CLEAR_UPDATE:
        clear();
        break;
    }

    if (page == MENU) groups = bridge.getGroups();
    else if (page == GROUP_MENU && gd != null) users = bridge.getUsers(gd.getId());

    return true;
  }

  protected void edit() throws Exception {
    returnUrl = getUrl(GROUP_MENU);
    errMsg = getMsg("db_error") + "<BR>";
    String name = getParamNotNull(NAME).trim();
    if (name.length() == 0) errMsg = errMsg + getMsg("group_admin_invalid_name");
    else {
      gd.setText(name);
      gd.setIconUrl(getParamNotNull(ICON).trim());
      try {
        bridge.updateGroup(gd, false);
        page = GROUP_MENU;
      } catch (Exception ex) {
        errMsg = errMsg + ex.getLocalizedMessage();
      }
    }
  }

  protected void create() throws Exception {
    returnUrl = getUrl(MENU);
    errMsg = getMsg("db_error") + "<BR>";
    String name = getParamNotNull(NAME).trim();
    String id = getParamNotNull(ID).trim();
    String icon = getParamNotNull(ICON).trim();
    if (name.length() == 0) errMsg = errMsg + getMsg("group_admin_invalid_name");
    else if (id.length() == 0) errMsg = errMsg + getMsg("group_admin_invalid_id");
    else {
      gd = bridge.getGroupData(id);
      if (gd != null) {
        returnUrl = urlParam(urlParam(getUrl(CREATE), NAME, name), ICON, icon);
        errMsg = errMsg + getMsg("group_admin_id_already_exists");
      } else {
        try {
          gd = new GroupData(id, name, icon, null);
          bridge.updateGroup(gd, true);
          page = GROUP_MENU;
        } catch (Exception ex) {
          gd = null;
          errMsg = errMsg + ex.getLocalizedMessage();
        }
      }
    }
  }

  protected void delete() throws Exception {
    returnUrl = getUrl(GROUP_MENU);
    try {
      bridge.deleteGroup(gd.getId());
      gd = null;
      page = MENU;
    } catch (Exception ex) {
      returnUrl = getUrl(GROUP_MENU);
      errMsg = getMsg("db_error") + "<BR>" + ex.getLocalizedMessage();
    }
  }

  protected void clear() throws Exception {
    returnUrl = getUrl(GROUP_MENU);
    try {
      bridge.clearGroupReportData(gd.getId(), dm.dFrom, dm.dTo);
      page = GROUP_MENU;
    } catch (Exception ex) {
      errMsg = getMsg("db_error") + "<BR>" + ex.getLocalizedMessage();
    }
  }

  @Override
  public void head(java.io.PrintWriter out) throws Exception {
    super.head(out);
    if (page == CLEAR) {
      StringBuilder sb = new StringBuilder(200);
      dm.writeDateScript(sb);
      out.println(sb.substring(0));
    }
  }

  @Override
  public void body(java.io.PrintWriter out) throws Exception {

    super.body(out);
    StringBuilder sb = new StringBuilder(2000);
    StringBuilder sb2 = new StringBuilder(500);
    Iterator it;
    boolean flag = false;

    // decidir titol segons pagina

    sb.append(linkTo(urlParam(Main.URL, LANG, lang), bundle.getString(Main.TITLE), null));
    if (page != MENU) sb.append(" | ").append(linkTo(getUrl(), getMsg(TITLE), null));

    standardHeader(out, filter(getTitle(bundle)), sb.substring(0));
    sb.setLength(0);

    if (gd == null && page != CREATE && page != MENU) {
      page = ERR;
      if (errMsg == null) errMsg = getMsg("bad_data");
    }

    switch (page) {
      case MENU:
        sb.append("<table class=\"tblA\" width=400>\n");
        sb.append("<tr><th>").append(getMsg("group_admin_groups")).append("</th></tr>\n");
        it = groups.iterator();
        while (it.hasNext()) {
          GroupData gdata = (GroupData) it.next();
          String url = urlParam(getUrl(GROUP_MENU), GROUP, gdata.getId());
          sb.append("<tr><td><a href=\"").append(url).append("\">").append(filter(gdata.getText()));
          sb.append("</a></td></tr>\n");
        }
        sb.append("</table>\n");
        sb.append("<br clear=\"all\">\n");
        sb.append("<form>");
        sb.append(buttonTo(getUrl(CREATE), getMsg("group_admin_new_button"), null));
        sb.append("</form>");
        break;

      case GROUP_MENU:
        sb.append("<form class=\"info\">\n");
        sb.append("<p><strong>")
            .append(getMsg("group_admin_id"))
            .append("</strong> ")
            .append(filter(gd.getId()))
            .append("</p>\n");
        sb.append("<p><strong>")
            .append(getMsg("group_admin_name"))
            .append("</strong> ")
            .append(filter(gd.getText()))
            .append("</p>\n");
        sb.append("<p><strong>").append(getMsg("group_admin_icon")).append(" ");
        if (gd.getIconUrl() != null && gd.getIconUrl().length() > 0) {
          sb.append("<img src=\"").append(filter(gd.getIconUrl())).append("\" title=\"");
          sb.append(filter(gd.getIconUrl())).append("\">");
        } else sb.append("---");
        sb.append("</p>\n");
        sb.append("<p>");
        sb.append(buttonTo(getUrl(EDIT), getMsg("group_admin_edit_button"), null));
        sb.append(buttonTo(getUrl(DELETE), getMsg("group_admin_delete_button"), null));
        sb.append(buttonTo(getUrl(CLEAR), getMsg("group_admin_clear_button"), null));
        sb.append("</p>\n");
        sb.append("</form>\n<br clear=\"all\">\n");
        if (users != null && users.size() > 0) {
          sb.append("<table class=\"tblA\" width=500>\n");
          sb.append("<tr><th>").append(getMsg("group_admin_users")).append("</th></tr>\n");
          it = users.iterator();
          while (it.hasNext()) {
            UserData ud = (UserData) it.next();
            sb2.setLength(0);
            sb2.append(UserAdmin.URL);
            urlParamSb(sb2, LANG, lang, true);
            urlParamSb(sb2, GROUP, gd.getId(), false);
            urlParamSb(sb2, UserAdmin.USER, ud.getId(), false);
            sb.append("<tr><td><a href=\"")
                .append(sb2.substring(0))
                .append("\">")
                .append(filter(ud.getText()));
            sb.append("</a></td></tr>\n");
          }
          sb.append("</table>\n");
          sb.append("<br clear=\"all\">");

        } else {
          sb.append("<p><strong>").append(getMsg("group_admin_no_users")).append("</strong></p>\n");
        }
        sb.append("<form>");
        sb2.setLength(0);
        sb2.append(UserAdmin.URL);
        urlParamSb(sb2, LANG, lang, true);
        urlParamSb(sb2, ACTION, UserAdmin.ACTIONS[UserAdmin.CREATE], false);
        urlParamSb(sb2, GROUP, gd.getId(), false);
        sb.append(buttonTo(sb2.substring(0), getMsg("group_admin_create_user_button"), null));
        sb.append("</form>\n");
        break;

      case EDIT:
        flag = true;
      case CREATE:
        String id = (gd != null ? gd.getId() : getParamNotNull(ID).trim());
        String name = (gd != null ? gd.getText() : getParamNotNull(NAME).trim());
        String icon = (gd != null ? gd.getIconUrl() : getParamNotNull(ICON).trim());
        sb.append("<form class=\"inputForm\" method=\"post\" action=\"")
            .append(getUrl(flag ? EDIT_UPDATE : CREATE_UPDATE))
            .append("\">\n");

        sb.append("<p><strong>").append(getMsg("group_admin_id")).append("</strong> ");
        sb.append("<input name=\"")
            .append(ID)
            .append("\" value=\"")
            .append(filter(id))
            .append("\" size=40");
        if (flag) sb.append(" readonly");
        sb.append("></p>\n");
        // if(!flag)
        //    sb.append("<input name=\"").append(ID).append("\"
        // value=\"").append(filter(id)).append("\" size=\"40\">");
        // else
        //    sb.append(filter(id==null ? "" : id));
        // sb.append("</p>\n");

        sb.append("<p><strong>").append(getMsg("group_admin_name")).append("</strong> ");
        sb.append("<input name=\"")
            .append(NAME)
            .append("\" value=\"")
            .append(filter(name))
            .append("\" size=40></p>\n");

        sb.append("<p><strong>").append(getMsg("group_admin_icon")).append("</strong> ");
        sb.append("<input name=\"")
            .append(ICON)
            .append("\" value=\"")
            .append(filter(icon))
            .append("\" size=40></p>\n");

        sb.append("<p><input type=\"submit\" value=\"").append(getMsg("submit")).append("\"> ");
        sb.append(buttonTo(getUrl(flag ? GROUP_MENU : MENU), getMsg("cancel"), null));
        sb.append("</p>\n");
        sb.append("</form>\n");
        sb.append("<br clear=\"all\">\n");
        break;

      case DELETE:
        sb.append("<p><strong>")
            .append(getMsg("group_admin_delete_group"))
            .append(" \"")
            .append(filter(gd.getText()))
            .append("\"</strong></p>\n");
        sb.append("<p>").append(getMsg("group_admin_delete_group_explain")).append("</p>\n");
        sb.append("<p>").append(getMsg("report_areyousure")).append("</p>\n");
        sb.append("<form method=\"post\" action=\"").append(getUrl(DELETE_UPDATE)).append("\">\n");
        sb.append("<p><input type=\"submit\" value=\"")
            .append(getMsg("YES"))
            .append("\" width=50>\n");
        sb.append(buttonTo(getUrl(GROUP_MENU), getMsg("NOT"), " width=50"));
        sb.append("</p>\n");
        sb.append("</form>\n");
        break;

      case CLEAR:
        sb.append("<p><strong>")
            .append(getMsg("group_admin_clear_group"))
            .append(" \"")
            .append(filter(gd.getText()))
            .append("\"</strong></p>\n");
        sb.append("<p>").append(getMsg("group_admin_clear_group_explain")).append("</p>\n");
        sb.append("<form class=\"inputForm\" action=\"")
            .append(getUrl(CLEAR_UPDATE))
            .append("\" method=\"post\" name=\"")
            .append(MAIN_FORM)
            .append("\">\n");
        dm.writeHiddenFields(sb);
        sb2.setLength(0);
        sb2.append("document.").append(MAIN_FORM).append(".submit()");
        dm.zonaData(sb, buttonAction(sb2.substring(0), getMsg("db_clear_reports_date"), null));
        sb.append("</form>\n");
        sb.append("<br clear=\"all\">\n");
        break;

      default:
        sb.append("<p><strong>").append(getMsg("error")).append("</strong></p>\n");
        if (errMsg != null) {
          sb.append("<p>").append(errMsg).append("</p>\n");
        }
        if (returnUrl != null) {
          sb.append("<p><a href=\"")
              .append(returnUrl)
              .append("\">")
              .append(getMsg("return"))
              .append("</a></p>\n");
        }
    }
    out.println(sb.substring(0));
  };
}
