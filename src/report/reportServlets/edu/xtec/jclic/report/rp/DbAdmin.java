/*
 * File    : DbAdmin.java
 * Created : 24-jan-2003 12:47
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

import edu.xtec.jclic.report.Reporter;
import edu.xtec.util.db.ConnectionBean;
import edu.xtec.util.db.ConnectionBeanProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class DbAdmin extends Basic {

  public static String TITLE = "db_admin_title";
  public static String URL = "dbAdmin";

  public static final int MENU = 0, EDIT_SETTINGS = 1, UPDATE_SETTINGS = 2, EDIT_PWD = 3, UPDATE_PWD = 4;

  public static final String[] ACTIONS = { "", "edit", "update", "editPwd", "updatePwd" };

  public static final String ALLOW_GROUP_CREATE = "agc", ALLOW_USER_CREATE = "auc", SHOW_GROUP_LIST = "shgl",
      SHOW_USER_LIST = "shul", USER_TABLES = "ut", TIME_LAP = "lap";

  public static final String[] DB_BOOL_KEYS = { Reporter.ALLOW_CREATE_GROUPS, Reporter.ALLOW_CREATE_USERS,
      Reporter.SHOW_GROUP_LIST, Reporter.SHOW_USER_LIST, Reporter.USER_TABLES };

  public static final String[] DB_BOOL_PARAMS = { ALLOW_GROUP_CREATE, ALLOW_USER_CREATE, SHOW_GROUP_LIST,
      SHOW_USER_LIST, USER_TABLES };

  public static final String[] DB_LITERAL_KEYS = { Reporter.TIME_LAP };
  public static final String[] DB_LITERAL_PARAMS = { TIME_LAP };
  public static final int[] DB_LITERAL_PARAMS_LENGTH = { 4 };

  public static final String PW_FIELD = "pw";

  protected int page;

  public String getTitle(ResourceBundle bundle) {
    return bundle.getString(TITLE);
  }

  public String getUrl() {
    return urlParam(URL, LANG, lang);
  }

  public String getUrl(int action) {
    return urlParam(getUrl(), ACTION, ACTIONS[action]);
  }

  @Override
  public boolean init() throws Exception {
    if (!super.init())
      return false;

    page = getAction(ACTIONS, MENU);
    switch (page) {
    case UPDATE_SETTINGS:
      updateSettings();
      break;
    case UPDATE_PWD:
      updatePwd();
      break;
    }
    return true;
  }

  private void updateSettings() throws Exception {
    Exception ex = null;
    boolean[] bValues = new boolean[DB_BOOL_PARAMS.length];
    for (int i = 0; i < DB_BOOL_PARAMS.length; i++)
      bValues[i] = getBoolParam(DB_BOOL_PARAMS[i], ON);

    String[] sValues = new String[DB_LITERAL_PARAMS.length];
    for (int i = 0; i < DB_LITERAL_PARAMS.length; i++)
      sValues[i] = getParam(DB_LITERAL_PARAMS[i]);

    ConnectionBeanProvider cbp = bridge.getConnectionBeanProvider();
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement updateStmt = null;
    PreparedStatement insertStmt = null;
    try {
      updateStmt = cb.getPreparedStatement(
          "UPDATE " + bridge.getTableName("SETTINGS") + " SET SETTING_VALUE=? WHERE SETTING_KEY=?");

      insertStmt = cb.getPreparedStatement(
          "INSERT INTO " + bridge.getTableName("SETTINGS") + "(SETTING_KEY,SETTING_VALUE) VALUES(?,?)");

      for (int i = 0; i < DB_BOOL_PARAMS.length; i++) {
        updateItem(DB_BOOL_KEYS[i], bValues[i] ? "true" : "false", updateStmt, insertStmt);
      }

      for (int i = 0; i < DB_LITERAL_PARAMS.length; i++) {
        updateItem(DB_LITERAL_KEYS[i], sValues[i], updateStmt, insertStmt);
      }
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(updateStmt);
      cb.closeStatement(insertStmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw (ex);
    }
  }

  private void updateItem(String sk, String sv, PreparedStatement updateStmt, PreparedStatement insertStmt)
      throws Exception {
    updateStmt.setString(1, sv);
    updateStmt.setString(2, sk);
    int iRecords = updateStmt.executeUpdate();
    if (iRecords != 1) {
      if (iRecords == 0) {
        insertStmt.setString(1, sk);
        insertStmt.setString(2, sv);
        iRecords = insertStmt.executeUpdate();
      }
      if (iRecords != 1)
        throw new Exception(bundle.getString("db_admin_settings_error"));
    }
  }

  private void updatePwd() throws Exception {
    Exception ex = null;
    String newPwd = getParam(PW_FIELD);
    String s = null;

    ConnectionBeanProvider cbp = bridge.getConnectionBeanProvider();
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb
          .getPreparedStatement("SELECT count(*) FROM " + bridge.getTableName("SETTINGS") + " WHERE SETTING_KEY=?");
      stmt.setString(1, "PASSWORD");
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        newPwd = (newPwd != null) ? edu.xtec.util.Encryption.Encrypt(newPwd) : null;
        int iCount = rs.getInt(1);
        PreparedStatement stmt2;
        if (iCount == 0) { // PASSWORD field don't exists
          stmt2 = cb.getPreparedStatement(
              "INSERT INTO " + bridge.getTableName("SETTINGS") + " (SETTING_KEY,SETTING_VALUE) VALUES(?,?)");
          stmt2.setString(1, "PASSWORD");
          stmt2.setString(2, newPwd);
        } else {
          stmt2 = cb.getPreparedStatement(
              "UPDATE " + bridge.getTableName("SETTINGS") + " SET SETTING_VALUE=? WHERE SETTING_KEY=?");
          stmt2.setString(1, newPwd);
          stmt2.setString(2, "PASSWORD");
        }
        stmt2.executeUpdate();
        cb.closeStatement(stmt2);
      }
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw (ex);
    }
  }

  @Override
  public void head(java.io.PrintWriter out) throws Exception {
    super.head(out);
    if (page == EDIT_PWD) {
      StringBuilder sb = new StringBuilder(500);
      writeEditPwdScript(sb);
      out.println(sb.substring(0));
    }
  }

  @Override
  public void body(java.io.PrintWriter out) throws Exception {

    super.body(out);
    String titleKey = (page == EDIT_SETTINGS ? "db_admin_edit_title"
        : page == EDIT_PWD ? "db_admin_pw_edit_title" : "db_admin_title");
    StringBuilder sb = new StringBuilder(3000);
    sb.append(linkTo(urlParam(Main.URL, LANG, lang), bundle.getString(Main.TITLE), null));
    if (page != MENU)
      sb.append(" | ").append(linkTo(getUrl(), getTitle(bundle), null));
    standardHeader(out, filter(getMsg(titleKey)), sb.substring(0));
    sb.setLength(0);

    switch (page) {
    case EDIT_SETTINGS:
      sb.append("<form action=\"").append(getUrl(UPDATE_SETTINGS)).append("\" method=\"post\">\n");
      sb.append("<div class=\"inputForm\">\n");
      writeAdminFields(sb, true);
      sb.append("<p><input type=\"submit\" value=\"").append(getMsg("submit")).append("\"></p>\n");
      sb.append("</div>\n");
      sb.append("</form>\n");
      break;

    case EDIT_PWD:
      writeEditPwd(sb);
      break;

    default:
      sb.append("<form>\n");
      sb.append("<div class=\"inputForm\">\n");
      writeAdminFields(sb, false);
      sb.append("<p>").append(buttonTo(getUrl(EDIT_SETTINGS), bundle.getString("db_admin_edit_btn"), null));
      sb.append("&nbsp;");
      sb.append(buttonTo(getUrl(EDIT_PWD), bundle.getString("db_admin_pw_edit_btn"), null)).append("</p>");
      sb.append("</div>\n");
      sb.append("</form>\n");
    }
    out.println(sb.substring(0));
  };

  private void writeEditPwdScript(StringBuilder sb) throws Exception {

    sb.append("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("<!--\n");
    sb.append(" function verifyPasswords(){\n");
    sb.append("  if (document.forms[0].").append(PW_FIELD).append("2.value==document.forms[0].").append(PW_FIELD)
        .append(".value) document.forms[0].submit();\n");
    sb.append("  else{\n");
    sb.append("    alert(\"").append(getMsg("db_admin_pw_noFit")).append("\");\n");
    sb.append("    event.returnValue=false;\n");
    sb.append("  }\n");
    sb.append(" }\n");
    sb.append("//-->\n");
    sb.append("</script>\n");
  }

  private void writeEditPwd(StringBuilder sb) throws Exception {

    sb.append("<form action=\"").append(urlParam(getUrl(), ACTION, ACTIONS[UPDATE_PWD]))
        .append("\" method=\"post\" onSubmit=\"verifyPasswords();\">\n");
    sb.append("<div class=\"inputForm\">\n");
    sb.append("<p>").append(getMsg("db_admin_pw_newPwd")).append(": ");
    sb.append("<input type=\"password\" length=\"40\" name=\"").append(PW_FIELD).append("\"></p>\n");
    sb.append("<p>").append(getMsg("db_admin_pw_verifyPwd")).append(": ");
    sb.append("<input type=\"password\" length=\"40\" name=\"").append(PW_FIELD).append("2\"></p>\n");
    sb.append("<p><input type=\"submit\" value=\"").append(getMsg("submit")).append("\">\n");
    sb.append("</div>\n");
    sb.append("</form>\n");
  }

  private void writeAdminFields(StringBuilder sb, boolean edit) throws Exception {

    Map<String, String> settings = bridge.getProperties();

    for (int i = 0; i < DB_BOOL_PARAMS.length; i++) {
      sb.append("<p>").append(filter(getMsg("db_admin_param_" + DB_BOOL_PARAMS[i])));
      sb.append(" <input type=\"checkbox\" name=\"").append(DB_BOOL_PARAMS[i]).append("\" value=\"").append(ON)
          .append("\"");
      if (!edit)
        sb.append(" disabled");
      String s = (String) settings.get(DB_BOOL_KEYS[i]);
      if (s != null && s.trim().toLowerCase().equals("true"))
        sb.append(" checked");
      sb.append("></p>\n");
    }
    for (int i = 0; i < DB_LITERAL_PARAMS.length; i++) {
      sb.append("<p>");
      sb.append(filter(getMsg("db_admin_param_" + DB_LITERAL_PARAMS[i])));
      sb.append(" <input name=\"").append(DB_LITERAL_PARAMS[i]).append("\"");
      String s = (String) settings.get(DB_LITERAL_KEYS[i]);
      if (s != null)
        sb.append(" value=\"").append(s).append("\"");
      if (!edit)
        sb.append(" disabled");
      sb.append(" size=\"").append(DB_LITERAL_PARAMS_LENGTH[i]).append("\">");
      sb.append("</p>\n");
    }
  }
}
