/*
 * File    : JDBCReporter.java
 * Created : 12-jul-2001 18:56
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

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.Messages;
import edu.xtec.util.db.ConnectionBeanProvider;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.13
 */
public class JDBCReporter extends Reporter {

  protected String currentSessionId;
  protected int actCount;
  protected ActivityReg lastActivity;
  protected BasicJDBCBridge bridge;

  public static final String DRIVER = "driver", URL = "url", SYSTEM_USER = "system_user", SYSTEM_PWD = "system_pwd";

  /** Creates new ODBCReporter */
  public JDBCReporter() {
    super();
    bridge = null;
    currentSessionId = null;
    lastActivity = null;
    actCount = 0;
  }

  @Override
  public void end() {
    super.end();
    reportActivity();
    if (bridge != null) {
      bridge.end();
      bridge = null;
    }
  }

  @Override
  public void init(HashMap properties, Component parent, Messages msg) throws Exception {
    super.init(properties, parent, msg);
    boolean success = false;
    try {
      bridge = null;
      String driver = (String) properties.get(DRIVER);
      String url = (String) properties.get(URL);
      description = "JDBC " + url;
      String system_user = (String) properties.get(SYSTEM_USER);
      String system_pwd = (String) properties.get(SYSTEM_PWD);
      String tablePrefix = (String) properties.get(BasicJDBCBridge.TABLE_PREFIX_KEY);
      boolean createTables = !"false".equalsIgnoreCase((String) properties.get(BasicJDBCBridge.CREATE_TABLES_KEY));
      ConnectionBeanProvider cbp = ConnectionBeanProvider.getConnectionBeanProvider(false, driver, url, system_user,
          system_pwd, true);
      bridge = new BasicJDBCBridge(cbp, createTables, tablePrefix);
      if (userId == null)
        userId = promptUserId(parent, msg);
      if (userId == null)
        success = true;
    } catch (Exception ex) {
      msg.showErrorWarning(parent, "report_err_init", description, ex, null);
    }
    if (!success) {
      if (bridge != null) {
        bridge.end();
        bridge = null;
      }
      description = description + " (" + msg.get("report_not_connected") + ")";
      initiated = false;
    }
  }

  @Override
  public String getProperty(String key, String defaultValue) throws Exception {
    checkBridge();
    return bridge.getProperty(key, defaultValue);
  }

  @Override
  public List<GroupData> getGroups() throws Exception {
    checkBridge();
    return bridge.getGroups();
  }

  @Override
  public List<UserData> getUsers(String groupId) throws Exception {
    checkBridge();
    return bridge.getUsers(groupId);
  }

  @Override
  public String newGroup(GroupData gd) throws Exception {
    checkBridge();
    return bridge.newGroup(gd);
  }

  @Override
  public String newUser(UserData ud) throws Exception {
    checkBridge();
    return bridge.newUser(ud);
  }

  @Override
  public UserData getUserData(String userId) throws Exception {
    checkBridge();
    return bridge.getUserData(userId);
  }

  @Override
  public GroupData getGroupData(String groupId) throws Exception {
    checkBridge();
    return bridge.getGroupData(groupId);
  }

  protected void checkBridge() throws Exception {
    if (bridge == null)
      throw new Exception("Not connected!");
  }

  @Override
  public void newSession(JClicProject jcp, Component parent, Messages msg) {
    super.newSession(jcp, parent, msg);

    if (bridge == null || userId == null)
      return;

    reportActivity();
    currentSessionId = null;
  }

  public void createDBSession() {
    if (bridge == null || userId == null)
      return;
    try {
      actCount = 0;
      currentSessionId = bridge.addSession(userId, currentSession.timeMillis, currentSession.projectName,
          currentSession.code, sessionKey, sessionContext);
    } catch (Exception ex) {
      currentSessionId = null;
      bridge.end();
      bridge = null;
      initiated = false;
      System.err.println("Error creating report of session:\n" + ex);
    }
  }

  protected void reportActivity() {
    if (lastActivity != null && bridge != null) {
      if (!lastActivity.closed)
        lastActivity.closeActivity();
      if (currentSessionId == null)
        createDBSession();
      if (currentSessionId != null) {
        try {
          int actId;
          ActionReg ar;
          actId = bridge.addActivity(actCount++, currentSessionId, lastActivity.name, lastActivity.numActions,
              lastActivity.score, lastActivity.solved, lastActivity.getPrecision(),
              (int) (lastActivity.totalTime / 1000), lastActivity.code);

          for (int arc = 0; (ar = lastActivity.getActionReg(arc)) != null; arc++)
            bridge.addAction(actId, currentSessionId, arc, ar.type, ar.source, ar.dest, ar.isOk);

        } catch (Exception ex) {
          bridge.end();
          bridge = null;
          initiated = false;
          System.err.println("Error reporting activity:\n" + ex);
        }
      }
    }
    if (currentSession != null && currentSession.currentSequence != null
        && currentSession.currentSequence.currentActivity != lastActivity) {
      lastActivity = currentSession.currentSequence.currentActivity;
    } else
      lastActivity = null;
  }

  @Override
  public void newActivity(Activity act) {
    super.newActivity(act);
    reportActivity();
  }
}
