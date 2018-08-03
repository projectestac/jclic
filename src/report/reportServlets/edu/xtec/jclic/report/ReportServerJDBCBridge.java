/*
 * File    : ReportServerJDBCBridge.java
 * Created : 11-feb-2003 15:52
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2018 Francesc Busquets & Departament
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
import java.util.Calendar;
import java.util.List;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.23
 */
public class ReportServerJDBCBridge extends BasicJDBCBridge {

  public static final String[] KCC = { "SESSION_KEY", "SESSION_CODE", "SESSION_CONTEXT" };

  public String GROUP_SESSION_DATE_STRING = null;
  public String ORDER_QUALIFICATION_1_STRING = null;
  public String ORDER_QUALIFICATION_2_STRING = null;

  public ReportServerJDBCBridge(ConnectionBeanProvider cbp, boolean createTables, String tablePrefix) throws Exception {
    super(cbp, createTables, tablePrefix);
    ConnectionBean cb = cbp.getConnectionBean();

    // Changed 20-Dec-2010: Set default behavior to MySQL instead of MS-Access
    // TODO: Check code with PostgreSQL
    switch (DBMSType) {
    case ACCESS:
      GROUP_SESSION_DATE_STRING = "Format(s.SESSION_DATETIME,'yyyy/mm/dd')";
      ORDER_QUALIFICATION_1_STRING = "sum(QUALIFICATION)/count(*)";
      ORDER_QUALIFICATION_2_STRING = ORDER_QUALIFICATION_1_STRING;
      break;
    case ORACLE:
      GROUP_SESSION_DATE_STRING = "trunc(s.SESSION_DATETIME)";
      ORDER_QUALIFICATION_1_STRING = "sum(QUALIFICATION)/count(*) AS qf";
      ORDER_QUALIFICATION_2_STRING = "qf";
      break;
    case MYSQL:
    default: // MYSQL & Other
      GROUP_SESSION_DATE_STRING = "s.SESSION_DATETIME";
      ORDER_QUALIFICATION_1_STRING = "sum(QUALIFICATION)/count(*) AS qf"; // std as in Oracle
      ORDER_QUALIFICATION_2_STRING = "qf";
      break;
    }
  }

  private static java.util.Date FIRST_DATE = null;

  public static java.util.Date getFirstDate() {
    if (FIRST_DATE == null) {
      Calendar c = Calendar.getInstance();
      c.set(1992, 0, 1);
      FIRST_DATE = c.getTime();
    }
    return FIRST_DATE;
  }

  public java.util.Date getMinSessionDate() throws Exception {
    Exception ex = null;
    java.util.Date d = getFirstDate();
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement pstmt = null;
    try {
      pstmt = cb.getPreparedStatement("SELECT min(SESSION_DATETIME)" + " FROM " + getTableName("SESSIONS"));
      ResultSet result = pstmt.executeQuery();
      if (result.next())
        d = result.getDate(1);
      result.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(pstmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return d;
  }

  private String getFilteringStringQuery(String[] kcc, boolean and) {
    StringBuilder sb = new StringBuilder(200);
    if (kcc != null) {
      for (int i = 0; i < KCC.length; i++) {
        if (kcc[i] != null && kcc[i].length() > 0) {
          sb.append(and ? " AND s." : " s.").append(KCC[i]).append("=?");
          and = true;
        }
      }
    }
    return sb.substring(0);
  }

  private int updateFiltering(PreparedStatement pstmt, String[] kcc, int n) throws SQLException {
    if (kcc != null)
      for (int i = 0; i < KCC.length; i++)
        if (kcc[i] != null && kcc[i].length() > 0)
          pstmt.setString(n++, kcc[i]);
    return n;
  }

  public List<SessionData> getInfoSessionUser(String userId, String projectName, java.util.Date dateFrom,
      java.util.Date dateTo, String[] kcc, boolean groupByDate) throws Exception {
    Exception ex = null;
    List<SessionData> vInfoSessions = new ArrayList<SessionData>();
    String proj = null;
    if (projectName != null && projectName.length() > 0 && !projectName.equals("-1"))
      proj = projectName;
    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT ").append(GROUP_SESSION_DATE_STRING).append(", s.SESSION_ID, s.PROJECT_NAME, count(*)")
        .append(", sum(QUALIFICATION), sum(a.ACTIVITY_SOLVED), sum(a.TOTAL_TIME)").append(" FROM ")
        .append(getTableName("SESSIONS", "s")).append(", ").append(getTableName("ACTIVITIES", "a")).append(",")
        .append(getTableName("USERS", "u")).append(" WHERE u.USER_ID=? AND s.USER_ID=u.USER_ID")
        .append(getFilteringStringQuery(kcc, true)).append(" AND s.SESSION_DATETIME>=? AND s.SESSION_DATETIME<=?")
        .append(" AND s.SESSION_ID=a.SESSION_ID");
    if (proj != null)
      sb.append(" AND s.PROJECT_NAME=?");
    sb.append(" GROUP BY s.SESSION_ID, s.PROJECT_NAME, ").append(GROUP_SESSION_DATE_STRING).append(" ORDER BY ")
        .append(GROUP_SESSION_DATE_STRING);

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement pstmt = null;
    try {
      pstmt = cb.getPreparedStatement(sb.substring(0));
      int n = 1;
      pstmt.setString(n++, userId);
      n = updateFiltering(pstmt, kcc, n);
      pstmt.setTimestamp(n++, new Timestamp(dateFrom.getTime()));
      pstmt.setTimestamp(n++, new Timestamp(dateTo.getTime()));
      if (proj != null)
        pstmt.setString(n++, proj);

      ResultSet rs = pstmt.executeQuery();
      SessionData sdx = null;
      while (rs.next()) {
        SessionData sd = new SessionData(rs.getString(2), // id
            userId, // user
            rs.getString(3), // projectName
            ReportUtils.strToDate(rs.getString(1)), // date
            rs.getInt(4), // numActs
            rs.getInt(6), // actsSolved
            rs.getInt(5), // totalPrec
            Math.max(0, rs.getInt(7))); // totalTime
        if (groupByDate) {
          if (sd.sameDate(sdx)) {
            if (sdx != null)
              sdx.acumula(sd);
          } else {
            if (sdx != null)
              vInfoSessions.add(sdx);
            sdx = sd;
          }
        } else
          vInfoSessions.add(sd);
      }
      if (sdx != null)
        vInfoSessions.add(sdx);
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(pstmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return vInfoSessions;
  }

  public List<SessionData> getInfoSessionGroup(String groupId, String projectName, java.util.Date dateFrom,
      java.util.Date dateTo, String[] kcc, boolean groupByDate) throws Exception {
    Exception ex = null;
    List<SessionData> vInfoSessions = new ArrayList<SessionData>();
    String proj = null;
    if (projectName != null && projectName.length() > 0 && !projectName.equals("-1"))
      proj = projectName;

    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT ").append(GROUP_SESSION_DATE_STRING).append(
        ", s.SESSION_ID, s.PROJECT_NAME, count(*), sum(QUALIFICATION), sum(a.ACTIVITY_SOLVED), sum(a.TOTAL_TIME), s.USER_ID")
        .append(" FROM ").append(getTableName("SESSIONS", "s")).append(", ").append(getTableName("ACTIVITIES", "a"))
        .append(", ").append(getTableName("USERS", "u")).append(" WHERE u.GROUP_ID=? AND s.USER_ID=u.USER_ID")
        .append(getFilteringStringQuery(kcc, true)).append(" AND s.SESSION_DATETIME>=? AND s.SESSION_DATETIME<=?")
        .append(" AND s.SESSION_ID=a.SESSION_ID");
    if (proj != null)
      sb.append(" AND s.PROJECT_NAME=?");
    sb.append(" GROUP BY s.SESSION_ID, s.USER_ID, s.PROJECT_NAME, ").append(GROUP_SESSION_DATE_STRING)
        .append(" ORDER BY ").append(GROUP_SESSION_DATE_STRING);

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement pstmt = null;
    try {
      pstmt = cb.getPreparedStatement(sb.substring(0));
      int n = 1;
      pstmt.setString(n++, groupId);
      n = updateFiltering(pstmt, kcc, n);
      pstmt.setTimestamp(n++, new Timestamp(dateFrom.getTime()));
      pstmt.setTimestamp(n++, new Timestamp(dateTo.getTime()));
      if (proj != null)
        pstmt.setString(n++, proj);

      ResultSet rs = pstmt.executeQuery();
      SessionData sdx = null;
      while (rs.next()) {
        SessionData sd = new SessionData(rs.getString(2), // id
            rs.getString(8), // user
            rs.getString(3), // projectName
            ReportUtils.strToDate(rs.getString(1)), // date
            rs.getInt(4), // numActs
            rs.getInt(6), // actsSolved
            rs.getInt(5), // totalPrec
            Math.max(0, rs.getInt(7))); // totalTime
        if (groupByDate) {
          if (sd.sameDate(sdx)) {
            if (sdx != null)
              sdx.acumula(sd);
          } else {
            if (sdx != null)
              vInfoSessions.add(sdx);
            sdx = sd;
          }
        } else
          vInfoSessions.add(sd);
      }
      if (sdx != null)
        vInfoSessions.add(sdx);
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(pstmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return vInfoSessions;
  }

  public List<SessionData> getInfoSessionAct(String projectName, String activityName, java.util.Date dateFrom,
      java.util.Date dateTo, String[] kcc, boolean groupByDate) throws Exception {
    Exception ex = null;
    List<SessionData> vInfoSessions = new ArrayList<SessionData>();
    String act = null;
    if (activityName != null && activityName.length() > 0 && !activityName.equals("-1"))
      act = activityName;

    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT ").append(GROUP_SESSION_DATE_STRING).append(
        ", s.SESSION_ID, s.PROJECT_NAME, count(*), sum(QUALIFICATION), sum(a.ACTIVITY_SOLVED), sum(a.TOTAL_TIME), s.USER_ID")
        .append(" FROM ").append(getTableName("SESSIONS", "s")).append(", ").append(getTableName("ACTIVITIES", "a"))
        .append(" WHERE s.PROJECT_NAME=?").append(getFilteringStringQuery(kcc, true))
        .append(" AND s.SESSION_DATETIME>=? AND s.SESSION_DATETIME<=?").append(" AND s.SESSION_ID=a.SESSION_ID");
    if (act != null)
      sb.append(" AND a.ACTIVITY_NAME=?");
    sb.append(" GROUP BY s.SESSION_ID, s.USER_ID, s.PROJECT_NAME, ").append(GROUP_SESSION_DATE_STRING)
        .append(" ORDER BY ").append(GROUP_SESSION_DATE_STRING);

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement pstmt = null;
    try {
      pstmt = cb.getPreparedStatement(sb.substring(0));
      int n = 1;
      pstmt.setString(n++, projectName);
      n = updateFiltering(pstmt, kcc, n);
      pstmt.setTimestamp(n++, new Timestamp(dateFrom.getTime()));
      pstmt.setTimestamp(n++, new Timestamp(dateTo.getTime()));
      if (act != null)
        pstmt.setString(n++, act);

      ResultSet rs = pstmt.executeQuery();
      SessionData sdx = null;
      while (rs.next()) {
        SessionData sd = new SessionData(rs.getString(2), // id
            rs.getString(8), // user
            rs.getString(3), // projectName
            ReportUtils.strToDate(rs.getString(1)), // date
            rs.getInt(4), // numActs
            rs.getInt(6), // actsSolved
            rs.getInt(5), // totalPrec
            Math.max(0, rs.getInt(7))); // totalTime
        if (groupByDate) {
          if (sd.sameDate(sdx)) {
            if (sdx != null)
              sdx.acumula(sd);
          } else {
            if (sdx != null)
              vInfoSessions.add(sdx);
            sdx = sd;
          }
        } else
          vInfoSessions.add(sd);
      }
      if (sdx != null)
        vInfoSessions.add(sdx);
      rs.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(pstmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return vInfoSessions;
  }

  public boolean hasUserTables() throws Exception {
    String s = getProperty("USER_TABLES", null);
    boolean result = s == null || !s.trim().toLowerCase().equals("false");
    return result;
  }

  public List<String> getProjList(String userId, java.util.Date dateFrom, java.util.Date dateTo, String[] kcc)
      throws Exception {
    Exception ex = null;
    List<String> pl = new ArrayList<String>();

    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT distinct(PROJECT_NAME)").append(" FROM ").append(getTableName("SESSIONS", "s"))
        .append(" WHERE s.SESSION_DATETIME>=? AND s.SESSION_DATETIME<=?");
    if (userId != null)
      sb.append(" AND s.USER_ID=?"); // When userID==null, get data from all users
    sb.append(getFilteringStringQuery(kcc, true)).append(" ORDER BY PROJECT_NAME");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      int n = 1;
      stmt.setTimestamp(n++, new Timestamp(dateFrom.getTime()));
      stmt.setTimestamp(n++, new Timestamp(dateTo.getTime()));
      if (userId != null)
        stmt.setString(n++, userId);
      updateFiltering(stmt, kcc, n);

      ResultSet result = stmt.executeQuery();
      while (result.next())
        pl.add(result.getString("PROJECT_NAME"));
      result.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return pl;
  }

  public List<String> getProjListGrup(String groupId, java.util.Date dateFrom, java.util.Date dateTo, String[] kcc)
      throws Exception {
    /*
     * Returns a ProjList containing the list of the names of all projects made by
     * the users of the group 'groupName' in the time interval indicated.
     * Information is only returned if validated==true or if the projects wher made
     * in sessions where sessionKey==pass.
     */
    Exception ex = null;
    List<String> pl = new ArrayList<String>();

    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT distinct(PROJECT_NAME)").append(" FROM ").append(getTableName("SESSIONS", "s")).append(", ")
        .append(getTableName("GROUPS", "g")).append(", ").append(getTableName("USERS", "u"))
        .append(" WHERE SESSION_DATETIME>=? AND SESSION_DATETIME<=?")
        .append(" AND u.GROUP_ID=g.GROUP_ID AND s.USER_ID=u.USER_ID");
    if (groupId != null)
      sb.append(" AND g.GROUP_ID=?"); // When groupId==null get data from all groups
    sb.append(getFilteringStringQuery(kcc, true)).append(" ORDER BY PROJECT_NAME");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      int n = 1;
      stmt.setTimestamp(n++, new Timestamp(dateFrom.getTime()));
      stmt.setTimestamp(n++, new Timestamp(dateTo.getTime()));
      if (groupId != null)
        stmt.setString(n++, groupId);
      updateFiltering(stmt, kcc, n);

      ResultSet result = stmt.executeQuery();
      while (result.next())
        pl.add(result.getString("PROJECT_NAME"));
      result.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return pl;
  }

  public List<String> getActList(String prj, java.util.Date dateFrom, java.util.Date dateTo, String[] kcc)
      throws Exception {
    Exception ex = null;
    List<String> pl = new ArrayList<String>();

    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT distinct(ACTIVITY_NAME)").append(" FROM ").append(getTableName("ACTIVITIES", "a")).append(", ")
        .append(getTableName("SESSIONS", "s")).append(" WHERE s.SESSION_DATETIME>=? AND s.SESSION_DATETIME<=?")
        .append(" AND a.SESSION_ID=s.SESSION_ID");
    if (prj != null)
      sb.append(" AND s.PROJECT_NAME=?");
    sb.append(getFilteringStringQuery(kcc, true)).append(" ORDER BY ACTIVITY_NAME");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      int n = 1;
      stmt.setTimestamp(n++, new Timestamp(dateFrom.getTime()));
      stmt.setTimestamp(n++, new Timestamp(dateTo.getTime()));
      if (prj != null)
        stmt.setString(n++, prj);
      updateFiltering(stmt, kcc, n);

      ResultSet result = stmt.executeQuery();
      while (result.next())
        pl.add(result.getString("ACTIVITY_NAME"));
      result.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return pl;
  }

  public List<ActivityData> getPacSessionList(String userId, String proj, String sessionId, java.util.Date dFrom,
      java.util.Date dTo, String[] kcc) throws Exception {
    /*
     * Returns a PacSessionList with all the info needed to show the table with
     * information about the activities made in the session 'sessionId'
     * corresponding to the user 'userId' in the indicated date interval . Data ara
     * only returned when validated==true or when the invloucrated sessions where
     * created with sessionKey==pass.
     */

    Exception ex = null;
    List<ActivityData> v = new ArrayList<ActivityData>();
    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT s.PROJECT_NAME, a.ACTIVITY_ID, a.ACTIVITY_NAME, a.NUM_ACTIONS")
        .append(" ,a.SCORE, a.ACTIVITY_SOLVED, a.QUALIFICATION, a.TOTAL_TIME").append(" FROM ")
        .append(getTableName("ACTIVITIES", "a")).append(", ").append(getTableName("SESSIONS", "s"))
        .append(" WHERE a.SESSION_ID=s.SESSION_ID");
    if (dFrom != null && dTo != null)
      sb.append(" AND SESSION_DATETIME>=? AND SESSION_DATETIME<=?");
    if (userId != null)
      sb.append(" AND s.USER_ID=?");
    sb.append(getFilteringStringQuery(kcc, true));
    if (sessionId != null)
      sb.append(" AND a.SESSION_ID=?");
    if (proj != null && !proj.equals("-1"))
      sb.append(" AND s.PROJECT_NAME=?");
    sb.append(" ORDER BY s.SESSION_DATETIME, a.ACTIVITY_ID");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      int n = 1;
      if (dFrom != null && dTo != null) {
        stmt.setTimestamp(n++, new Timestamp(dFrom.getTime()));
        stmt.setTimestamp(n++, new Timestamp(dTo.getTime()));
      }
      if (userId != null)
        stmt.setString(n++, userId);
      n = updateFiltering(stmt, kcc, n);
      if (sessionId != null)
        stmt.setString(n++, sessionId);
      if (proj != null && !proj.equals("-1"))
        stmt.setString(n++, proj);

      ResultSet result = stmt.executeQuery();
      while (result.next()) {
        ActivityData ad = new ActivityData(result.getString("PROJECT_NAME"), result.getString("ACTIVITY_NAME"),
            result.getString("ACTIVITY_ID"), Math.max(0, result.getInt("TOTAL_TIME")), result.getInt("NUM_ACTIONS"),
            result.getInt("SCORE"), result.getInt("ACTIVITY_SOLVED") != 0, result.getInt("QUALIFICATION"));
        v.add(ad);
      }
      result.close();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    return v;
  }

  public void updateUser(UserData ud, boolean create) throws Exception {
    Exception ex = null;
    boolean result = false;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      String tn = getTableName("USERS");
      stmt = cb.getPreparedStatement(
          create ? "INSERT INTO " + tn + " (USER_NAME,USER_ICON,USER_PWD,USER_ID,GROUP_ID) VALUES (?,?,?,?,?)"
              : "UPDATE " + tn + " SET USER_NAME=?, USER_ICON=?, USER_PWD=? WHERE USER_ID=?");
      stmt.setString(1, StrUtils.limitStrLen(ud.getText(), 80));
      if (ud.getIconUrl() != null && ud.getIconUrl().length() == 0)
        ud.setIconUrl(null);
      stmt.setString(2, StrUtils.limitStrLen(ud.getIconUrl(), 255));
      stmt.setString(3, StrUtils.limitStrLen(ud.pwd, 255));
      stmt.setString(4, StrUtils.limitStrLen(ud.getId(), 50));
      if (create)
        stmt.setString(5, ud.groupId);
      result = stmt.executeUpdate() > 0;
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    if (!result)
      throw new Exception("SQL \"UPDATE\" statement returns 0");
  }

  public void updateGroup(GroupData gd, boolean create) throws Exception {
    Exception ex = null;
    boolean result = false;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      String tn = getTableName("GROUPS");
      stmt = cb.getPreparedStatement(create ? "INSERT INTO " + tn + " (GROUP_NAME,GROUP_ICON,GROUP_ID) VALUES (?,?,?)"
          : "UPDATE " + tn + " SET GROUP_NAME=?, GROUP_ICON=? WHERE GROUP_ID=?");
      stmt.setString(1, StrUtils.limitStrLen(gd.getText(), 80));
      if (gd.getIconUrl() != null && gd.getIconUrl().length() == 0)
        gd.setIconUrl(null);
      stmt.setString(2, StrUtils.limitStrLen(gd.getIconUrl(), 255));
      stmt.setString(3, StrUtils.limitStrLen(gd.getId(), 50));
      result = stmt.executeUpdate() > 0;
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
    if (!result)
      throw new Exception("SQL \"UPDATE\" statement returns 0");
  }

  public void clearUserReportData(String userId, java.util.Date dFrom, java.util.Date dTo) throws Exception {
    Exception ex = null;
    StringBuilder sb = new StringBuilder(300);
    // Index for GROUP_ID into users. It will be faster with groups, but MySQL
    // doesn't have foreign keys...
    sb.append("SELECT SESSION_ID").append(" FROM ").append(getTableName("SESSIONS")).append(" WHERE USER_ID=?");
    if (dFrom != null && dTo != null)
      sb.append(" AND SESSION_DATETIME>=? AND SESSION_DATETIME<=?");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      stmt.setString(1, userId);
      if (dFrom != null && dTo != null) {
        stmt.setTimestamp(2, new Timestamp(dFrom.getTime()));
        stmt.setTimestamp(3, new Timestamp(dTo.getTime()));
      }
      ResultSet rs = stmt.executeQuery();
      while (rs.next())
        deleteSession(rs.getString(1));
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
  }

  public void clearGroupReportData(String groupId, java.util.Date dFrom, java.util.Date dTo) throws Exception {
    Exception ex = null;
    StringBuilder sb = new StringBuilder(300);
    sb.append("SELECT SESSION_ID").append(" FROM ").append(getTableName("SESSIONS", "s")).append(", ")
        .append(getTableName("USERS", "u")).append(" WHERE u.GROUP_ID=?").append(" AND u.USER_ID=s.USER_ID");
    if (dFrom != null && dTo != null)
      sb.append(" AND s.SESSION_DATETIME>=? AND s.SESSION_DATETIME<=?");

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement(sb.substring(0));
      stmt.setString(1, groupId);
      if (dFrom != null && dTo != null) {
        stmt.setTimestamp(2, new Timestamp(dFrom.getTime()));
        stmt.setTimestamp(3, new Timestamp(dTo.getTime()));
      }
      ResultSet rs = stmt.executeQuery();
      while (rs.next())
        deleteSession(rs.getString(1));
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
  }

  protected void deleteSession(String sessionId) throws Exception {
    Exception ex = null;
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      stmt = cb.getPreparedStatement("DELETE FROM " + getTableName("ACTIONS") + " WHERE SESSION_ID=?");
      stmt.setString(1, sessionId);
      stmt.executeUpdate();
      cb.closeStatement(stmt);

      stmt = cb.getPreparedStatement("DELETE FROM " + getTableName("ACTIVITIES") + " WHERE SESSION_ID=?");
      stmt.setString(1, sessionId);
      stmt.executeUpdate();
      cb.closeStatement(stmt);

      stmt = cb.getPreparedStatement("DELETE FROM " + getTableName("SESSIONS") + " WHERE SESSION_ID=?");
      stmt.setString(1, sessionId);
      stmt.executeUpdate();

    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
  }

  public void deleteUser(String userId) throws Exception {
    Exception ex = null;

    clearUserReportData(userId, null, null);

    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      cb.getConnection().commit();
      stmt = cb.getPreparedStatement("DELETE FROM " + getTableName("USERS") + " WHERE USER_ID=?");
      stmt.setString(1, userId);
      stmt.executeUpdate();
    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
  }

  public void deleteGroup(String groupId) throws Exception {
    Exception ex = null;
    clearGroupReportData(groupId, null, null);
    ConnectionBean cb = cbp.getConnectionBean();
    PreparedStatement stmt = null;
    try {
      cb.getConnection().commit();
      stmt = cb.getPreparedStatement("DELETE FROM " + getTableName("USERS") + " WHERE GROUP_ID=?");
      stmt.setString(1, groupId);
      stmt.executeUpdate();
      cb.closeStatement(stmt);

      cb.getConnection().commit();
      stmt = cb.getPreparedStatement("DELETE FROM " + getTableName("GROUPS") + " WHERE GROUP_ID=?");
      stmt.setString(1, groupId);
      stmt.executeUpdate();

    } catch (Exception e) {
      ex = e;
    } finally {
      cb.closeStatement(stmt);
      cbp.freeConnectionBean(cb);
      if (ex != null)
        throw ex;
    }
  }
}
