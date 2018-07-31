/*
 * File    : BasicJDBCBridge.java
 * Created : 16-jul-2001 19:00
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

import edu.xtec.util.StrUtils;
import edu.xtec.util.db.ConnectionBean;
import edu.xtec.util.db.ConnectionBeanProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.23
 */
public class BasicJDBCBridge extends Object {

  public static final String DEFAULT_ODBC_BRIDGE = "sun.jdbc.odbc.JdbcOdbcDriver";
  public static final String DEFAULT_DB = "jdbc:odbc:JClicUserReports";
  public static final String TABLE_PREFIX_KEY = "tablePrefix";
  public static final String CREATE_TABLES_KEY = "createTables";
  public static final String TRUST_CLIENT_DATETIME = "trustClientDateTime";

  public static final int OTHER = 0;
  public static final int MYSQL = 1;
  public static final int ORACLE = 2;
  public static final int ACCESS = 3;

  protected String driver;
  protected String url;
  public String DBMSName;
  public int DBMSType;
  protected ConnectionBeanProvider cbp;
  protected boolean createTables;
  protected String tablePrefix;

  /** Creates new BasicJDBCBridge */
  // public BasicJDBCBridge(String setDriver, String setUrl, String systemUser, String systemPwd)
  // throws Exception{
  public BasicJDBCBridge(ConnectionBeanProvider cbp, boolean createTables, String tablePrefix)
      throws Exception {
    this.cbp = cbp;
    this.createTables = createTables;
    this.tablePrefix = (tablePrefix == null ? "" : tablePrefix);
    if (createTables) checkTables();
  }

  public String getTableName(String tableName) {
    return getTableName(tableName, null);
  }

  public String getTableName(String tableName, String alias) {
    StringBuilder result = new StringBuilder();
    if (tablePrefix != null && tablePrefix.length() > 0) result.append(tablePrefix);
    result.append(tableName);
    if (alias != null) result.append(" ").append(alias);
    return result.substring(0);
  }

  public ConnectionBeanProvider getConnectionBeanProvider() {
    return cbp;
  }

  public static final Object[][] TABLE_DEFS = {
    {
      "SETTINGS",
      new String[] {
        "SETTING_KEY VARCHAR(255) NOT NULL",
        "SETTING_VALUE VARCHAR(255)",
        "PRIMARY KEY (SETTING_KEY)"
      }
    },
    {
      "GROUPS",
      new String[] {
        "GROUP_ID VARCHAR(50) NOT NULL",
        "GROUP_NAME VARCHAR(80) NOT NULL",
        "GROUP_DESCRIPTION VARCHAR(255)",
        "GROUP_ICON VARCHAR(255)",
        "GROUP_CODE VARCHAR(50)",
        "GROUP_KEYWORDS VARCHAR(255)",
        "PRIMARY KEY (GROUP_ID)"
      }
    },
    {
      "USERS",
      new String[] {
        "USER_ID VARCHAR(50) NOT NULL",
        "GROUP_ID VARCHAR(50) NOT NULL",
        "USER_NAME VARCHAR(80) NOT NULL",
        "USER_PWD VARCHAR(255)",
        "USER_ICON VARCHAR(255)",
        "USER_CODE VARCHAR(50)",
        "USER_KEYWORDS VARCHAR(255)",
        "PRIMARY KEY (USER_ID)"
      }
    },
    {
      "SESSIONS",
      new String[] {
        "SESSION_ID VARCHAR(50) NOT NULL",
        "USER_ID VARCHAR(50) NOT NULL",
        "SESSION_DATETIME TIMESTAMP NOT NULL",
        "PROJECT_NAME VARCHAR(100) NOT NULL",
        "SESSION_KEY VARCHAR(50)",
        "SESSION_CODE VARCHAR(50)",
        "SESSION_CONTEXT VARCHAR(50)",
        "PRIMARY KEY (SESSION_ID)"
      }
    },
    {
      "ACTIVITIES",
      new String[] {
        "SESSION_ID VARCHAR(50) NOT NULL",
        "ACTIVITY_ID INTEGER(5) NOT NULL",
        "ACTIVITY_NAME VARCHAR(50) NOT NULL",
        "NUM_ACTIONS INTEGER(4)",
        "SCORE INTEGER(4)",
        "ACTIVITY_SOLVED INTEGER(1)",
        "QUALIFICATION INTEGER(3)",
        "TOTAL_TIME INTEGER(5)",
        "ACTIVITY_CODE VARCHAR(50)",
        "PRIMARY KEY (SESSION_ID,ACTIVITY_ID)"
      }
    },
    {
      "ACTIONS",
      new String[] {
        "SESSION_ID VARCHAR(50) NOT NULL",
        "ACTIVITY_ID INTEGER(5) NOT NULL",
        "ACTION_ID INTEGER(4) NOT NULL",
        "ACTION_TYPE VARCHAR(20) NOT NULL",
        "ACTION_SOURCE VARCHAR(255)",
        "ACTION_DEST VARCHAR(255)",
        "ACTION_OK INTEGER(1)",
        "PRIMARY KEY (SESSION_ID,ACTIVITY_ID,ACTION_ID)"
      }
    }
  };

  public static final String[][] DEFAULT_SETTINGS = {
    {"ALLOW_CREATE_GROUPS", "false"},
    {"ALLOW_CREATE_USERS", "false"},
    {"SHOW_GROUP_LIST", "true"},
    {"SHOW_USER_LIST", "true"},
    {"USER_TABLES", "true"},
    {"TIME_LAP", "10"}
  };

  protected void checkTables() throws Exception {
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    try {
      Connection con = cb.getConnection();
      java.sql.DatabaseMetaData dbmd = con.getMetaData();
      DBMSName = dbmd.getDatabaseProductName();
      if (DBMSName != null) {
        DBMSName = DBMSName.toLowerCase();
        if (DBMSName.indexOf("oracle") >= 0) DBMSType = ORACLE;
        else if (DBMSName.indexOf("mysql") >= 0) DBMSType = MYSQL;
        else if (DBMSName.indexOf("access") >= 0) DBMSType = ACCESS;
        else DBMSType = OTHER;
      }
      for (int i = 0; i < TABLE_DEFS.length; i++) {
        String tName = getTableName((String) TABLE_DEFS[i][0]);
        ResultSet rsx = dbmd.getTables(null, null, tName, null);
        boolean tableExists = rsx.next();
        rsx.close();
        if (!tableExists) {
          Statement stmt = con.createStatement();
          StringBuilder sb = new StringBuilder("CREATE TABLE ");
          sb.append(tName).append("(");
          String[] fields = (String[]) TABLE_DEFS[i][1];
          for (String sf : fields) {
            if (DBMSType == ACCESS) {
              // Change INTEGER(x) for INTEGER
              int intP = sf.indexOf("INTEGER(");
              if (intP > 0) {
                int intP2 = sf.indexOf(')', intP);
                sf =
                    sf.substring(0, intP + 7)
                        + (intP2 > 0 && sf.length() > intP2 + 1 ? sf.substring(intP2 + 1) : "");
              }
            } else if (DBMSType == ORACLE) {
              // Change TIMESTAMP for DATE
              int intP = sf.indexOf("TIMESTAMP");
              if (intP > 0) {
                sf =
                    sf.substring(0, intP)
                        + "DATE"
                        + (sf.length() > intP + 9 ? sf.substring(intP + 9) : "");
              }
              intP = sf.indexOf("INTEGER");
              if (intP > 0) {
                sf = sf.substring(0, intP) + "NUMBER" + sf.substring(intP + 7);
              }
            }
            sb.append(sf).append(",");
          }
          sb.deleteCharAt(sb.length() - 1);
          sb.append(")");
          // System.out.println("CREATING TABLE "+tName);
          stmt.executeUpdate(sb.substring(0));
          if (i == 0) {
            for (String[] setting : DEFAULT_SETTINGS) {
              sb.setLength(0);
              sb.append("INSERT INTO ")
                  .append(tName)
                  .append(" VALUES(")
                  .append("'")
                  .append(setting[0])
                  .append("','")
                  .append(setting[1])
                  .append("')");
              stmt.executeUpdate(sb.substring(0));
            }
          }
          stmt.close();
        }
      }
    } catch (Exception e) {
      ex = e;
    } finally {
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
  }

  public void end() {
    if (cbp != null) ConnectionBeanProvider.freeConnectionBeanProvider(cbp);
    // cbp.destroy();
    cbp = null;
  }

  @Override
  protected void finalize() throws Throwable {
    end();
    super.finalize();
  }

  public String getProperty(String key, String defaultValue) throws Exception {
    String result = defaultValue;
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt =
          cb.getPreparedStatement(
              "SELECT SETTING_VALUE FROM " + getTableName("SETTINGS") + " WHERE SETTING_KEY=?");
      stmt.setString(1, key);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) result = trimStr(rs.getString(1));
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return result;
  }

  public Map<String, String> getProperties() throws Exception {
    Map<String, String> result = new HashMap<String, String>();
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement("SELECT * FROM " + getTableName("SETTINGS"));
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        result.put(rs.getString("SETTING_KEY"), rs.getString("SETTING_VALUE"));
      }
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return result;
  }

  public List<GroupData> getGroups() throws Exception {
    ArrayList<GroupData> result = new ArrayList<GroupData>();
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt =
          cb.getPreparedStatement(
              "SELECT GROUP_ID,GROUP_NAME,GROUP_ICON,GROUP_DESCRIPTION"
                  + " FROM "
                  + getTableName("GROUPS"));
      // ResultSet rs=stmt.executeQuery("SELECT GROUP_NAME,GROUP_ICON,GROUP_ID FROM groups");
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        String[] s = new String[4];
        // Object[] v=new Object[3];
        for (int i = 0; i < 3; i++) {
          // for(int i=0; i<3; i++){
          s[i] = trimStr(rs.getString(i + 1));
          // String s=trimStr(rs.getString(i+1));
          // if(s!=null)
          //    v[i]=s.trim();
        }
        // result.add(v);
        result.add(new GroupData(s[0], s[1], s[2], s[3]));
      }
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return result;
  }

  public List<UserData> getUsers(String groupId) throws Exception {
    ArrayList<UserData> result = new ArrayList<UserData>();
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      StringBuilder sb = new StringBuilder("SELECT USER_NAME,USER_ICON,USER_ID,USER_PWD,GROUP_ID");
      sb.append(" FROM ").append(getTableName("USERS"));
      if (groupId != null) sb.append(" WHERE GROUP_ID=?");
      stmt = cb.getPreparedStatement(sb.substring(0));
      if (groupId != null) stmt.setString(1, groupId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        String[] s = new String[5];
        // Object[] v=new Object[4];
        for (int i = 0; i < 5; i++) {
          s[i] = trimStr(rs.getString(i + 1));
          // String s=trimStr(rs.getString(i+1));
          // if(s!=null)
          //    v[i]=s.trim();
        }
        result.add(new UserData(s[2], s[0], s[1], s[3], s[4]));
        // result.add(v);
      }
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw (ex);
    }
    return result;
  }

  public UserData getUserData(String userId) throws Exception {
    UserData result = null;
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt =
          cb.getPreparedStatement(
              "SELECT *" + " FROM " + getTableName("USERS") + " WHERE USER_ID=?");
      stmt.setString(1, userId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        result =
            new UserData(
                trimStr(rs.getString("USER_ID")),
                trimStr(rs.getString("USER_NAME")),
                trimStr(rs.getString("USER_ICON")),
                trimStr(rs.getString("USER_PWD")),
                trimStr(rs.getString("GROUP_ID")));
      }
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw (ex);
    }
    return result;
  }

  public GroupData getGroupData(String groupId) throws Exception {
    GroupData result = null;
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt =
          cb.getPreparedStatement(
              "SELECT *" + " FROM " + getTableName("GROUPS") + " WHERE GROUP_ID=?");
      stmt.setString(1, groupId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        result =
            new GroupData(
                trimStr(rs.getString("GROUP_ID")),
                trimStr(rs.getString("GROUP_NAME")),
                trimStr(rs.getString("GROUP_ICON")),
                trimStr(rs.getString("GROUP_DESCRIPTION")));
      }
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return result;
  }

  public String addSession(
      String userId,
      long time,
      String projectName,
      String sessionCode,
      String sessionKey,
      String sessionContext)
      throws Exception {
    String sessionId = null;
    Exception ex = null;
    if (userId == null) throw new Exception("Invalid UserId");
    if (projectName == null) throw new Exception("Invalid ProjectName");
    sessionId = StrUtils.limitStrLen(userId, 36).concat("_").concat(Long.toString(time));
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt =
          cb.getPreparedStatement(
              "INSERT INTO "
                  + getTableName("SESSIONS")
                  + " (SESSION_ID,USER_ID,SESSION_DATETIME,PROJECT_NAME,SESSION_KEY,SESSION_CODE,SESSION_CONTEXT)"
                  + " VALUES(?,?,?,?,?,?,?)");
      stmt.setString(1, sessionId);
      stmt.setString(2, userId);
      stmt.setTimestamp(3, new Timestamp(time));
      stmt.setString(4, StrUtils.limitStrLen(projectName, 100));
      stmt.setString(5, StrUtils.limitStrLen(sessionKey, 50));
      stmt.setString(6, StrUtils.limitStrLen(sessionCode, 50));
      stmt.setString(7, StrUtils.limitStrLen(sessionContext, 50));
      stmt.executeUpdate();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return sessionId;
  }

  public int addActivity(int activityId, String sessionId, ActivityReg ar) throws Exception {
    return addActivity(
        activityId,
        sessionId,
        ar.name,
        ar.numActions,
        ar.score,
        ar.solved,
        ar.getPrecision(),
        (int) (ar.totalTime / 1000),
        ar.code);
  }

  public int addActivity(
      int activityId,
      String sessionId,
      String name,
      int actions,
      int score,
      boolean solved,
      int precision,
      int time,
      String activityCode)
      throws Exception {
    if (sessionId == null) throw new Exception("Invalid sessionId");
    if (name == null) throw new Exception("Invalid activity name");
    int actId = activityId;
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt =
          cb.getPreparedStatement(
              "INSERT INTO "
                  + getTableName("ACTIVITIES")
                  + " (SESSION_ID,ACTIVITY_ID,ACTIVITY_NAME,NUM_ACTIONS,SCORE,ACTIVITY_SOLVED,QUALIFICATION,TOTAL_TIME,ACTIVITY_CODE)"
                  + " VALUES(?,?,?,?,?,?,?,?,?)");
      stmt.setString(1, sessionId);
      stmt.setInt(2, activityId);
      stmt.setString(3, StrUtils.limitStrLen(name, 50));
      stmt.setInt(4, actions);
      stmt.setInt(5, score);
      stmt.setInt(6, solved ? 1 : 0);
      stmt.setInt(7, precision);
      stmt.setInt(8, time);
      stmt.setString(9, StrUtils.limitStrLen(activityCode, 50));
      stmt.executeUpdate();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return actId;
  }

  public int addAction(int activityId, String sessionId, int actionId, ActionReg actReg)
      throws Exception {
    return addAction(
        activityId, sessionId, actionId, actReg.type, actReg.source, actReg.dest, actReg.isOk);
  }

  public int addAction(
      int activityId,
      String sessionId,
      int actionId,
      String type,
      String source,
      String dest,
      boolean isOk)
      throws Exception {

    if (sessionId == null) throw new Exception("Invalid sessionId");
    if (type == null) throw new Exception("Invalid action type");
    int actId = actionId;
    Exception ex = null;

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt =
          cb.getPreparedStatement(
              "INSERT INTO "
                  + getTableName("ACTIONS")
                  + " (SESSION_ID,ACTIVITY_ID,ACTION_ID,ACTION_TYPE,ACTION_SOURCE,ACTION_DEST,ACTION_OK)"
                  + " VALUES(?,?,?,?,?,?,?)");
      stmt.setString(1, sessionId);
      stmt.setInt(2, activityId);
      stmt.setInt(3, actionId);
      stmt.setString(4, type);
      stmt.setString(5, StrUtils.limitStrLen(source, 255));
      stmt.setString(6, StrUtils.limitStrLen(dest, 255));
      stmt.setInt(7, isOk ? 1 : 0);
      stmt.executeUpdate();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return actId;
  }

  public String newGroup(GroupData gd) throws Exception {
    Exception ex = null;
    StringBuilder sb =
        new StringBuilder("INSERT INTO " + getTableName("GROUPS") + " (GROUP_ID,GROUP_NAME");
    int n = 0;
    if (gd.hasIcon()) {
      sb.append(",GROUP_ICON");
      n++;
    }
    if (gd.description != null && gd.description.length() > 0) {
      sb.append(",GROUP_DESCRIPTION");
      n++;
    }
    sb.append(") VALUES(?,?");
    for (int i = 0; i < n; i++) sb.append(",?");
    sb.append(")");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      n = 1;
      stmt.setString(n++, StrUtils.limitStrLen(gd.getId(), 50));
      stmt.setString(n++, StrUtils.limitStrLen(gd.getText(), 80));
      if (gd.hasIcon()) stmt.setString(n++, StrUtils.limitStrLen(gd.getIconUrl(), 255));
      if (gd.description != null && gd.description.length() > 0)
        stmt.setString(n++, StrUtils.limitStrLen(gd.description, 255));
      stmt.executeUpdate();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return gd.getId();
  }

  public String newUser(UserData ud) throws Exception {
    Exception ex = null;
    StringBuilder sb =
        new StringBuilder("INSERT INTO " + getTableName("USERS") + " (GROUP_ID,USER_ID,USER_NAME");
    int n = 0;
    if (ud.hasIcon()) {
      sb.append(",USER_ICON");
      n++;
    }
    if (ud.pwd != null && ud.pwd.length() > 0) {
      sb.append(",USER_PWD");
      n++;
    }
    sb.append(") VALUES(?,?,?");
    for (int i = 0; i < n; i++) sb.append(",?");
    sb.append(")");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      n = 1;
      stmt.setString(n++, ud.groupId);
      stmt.setString(n++, StrUtils.limitStrLen(ud.getId(), 50));
      stmt.setString(n++, StrUtils.limitStrLen(ud.getText(), 80));
      if (ud.hasIcon()) stmt.setString(n++, StrUtils.limitStrLen(ud.getIconUrl(), 255));
      if (ud.pwd != null && ud.pwd.length() > 0)
        stmt.setString(n++, StrUtils.limitStrLen(ud.pwd, 255));
      stmt.executeUpdate();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null) throw ex;
    }
    return ud.getId();
  }

  public static String trimStr(String s) {
    String result = s;
    if (result != null) result = result.trim();
    return result;
  }
}
