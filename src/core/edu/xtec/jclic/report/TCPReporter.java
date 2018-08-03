/*
 * File    : TCPReporter.java
 * Created : 17-jul-2001 9:39
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
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Messages;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Timer;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class TCPReporter extends Reporter {

  protected String currentSessionId;
  protected ActivityReg lastActivity;
  protected int actCount;
  protected URL serviceUrl;
  protected HashMap<String, String> dbProperties;
  protected List<TCPReportBean> tasks;
  protected Component parent;
  protected Messages msg;
  protected Timer timer;
  protected int timerLap;

  public static final String SERVER_PATH = "path", DEFAULT_SERVER_PATH = "localhost:9000", SERVER_PROTOCOL = "protocol",
      DEFAULT_SERVER_PROTOCOL = "http", SERVER_SERVICE = "service", DEFAULT_SERVER_SERVICE = "/JClicReportService",
      TIMER_LAP = "lap";

  public static final int DEFAULT_TIMER_LAP = 20;

  /** Creates new TCPReporter */
  public TCPReporter() {
    super();
    currentSessionId = null;
    lastActivity = null;
    actCount = 0;
    serviceUrl = null;
    tasks = new ArrayList<TCPReportBean>();
    timerLap = DEFAULT_TIMER_LAP;
  }

  protected synchronized void flushTasks() {
    if (!tasks.isEmpty() && serviceUrl != null) {
      TCPReportBean bean = new TCPReportBean(TCPReportBean.MULTIPLE);
      TCPReportBean[] items;
      items = tasks.toArray(new TCPReportBean[tasks.size()]);
      for (TCPReportBean item : items)
        bean.addElement(item.getJDomElement());
      if (transaction(bean) != null) {
        for (TCPReportBean item : items)
          tasks.remove(item);
      }
    }
  }

  @Override
  public void end() {
    super.end();
    reportActivity();
    flushTasks();
    stopReporting();
  }

  protected void checkUrl() throws Exception {
    if (serviceUrl == null)
      throw new Exception("Service not available!!");
  }

  protected TCPReportBean transaction(String key, Domable[] data) {
    return transaction(new TCPReportBean(key, data));
  }

  protected TCPReportBean transaction(TCPReportBean request) {
    if (serviceUrl == null)
      return null;
    TCPReportBean result = null;
    boolean loop = true;
    while (result == null && loop) {
      try {
        HttpURLConnection con = (HttpURLConnection) serviceUrl.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Content-type", "text/xml");
        OutputStream out = con.getOutputStream();
        JDomUtility.saveDocument(out, request.getJDomElement());
        out.flush();
        out.close();
        InputStream in = con.getInputStream();
        org.jdom.Document doc = JDomUtility.getSAXBuilder().build(con.getInputStream());
        result = new TCPReportBean(doc.getRootElement());
      } catch (Exception ex) {
        if (msg != null) {
          int r = msg.showErrorWarning(parent, "report_err_connect", ex, "ric");
          switch (r) {
          case Messages.RETRY:
            break;
          case Messages.IGNORE:
            loop = false;
            break;
          default:
            stopReporting();
            loop = false;
            break;
          }
        } else {
          stopReporting();
          loop = false;
        }
      }
    }
    return result;
  }

  protected void stopReporting() {
    if (serviceUrl != null) {
      serviceUrl = null;
      if (description != null && msg != null)
        description = description + " (" + msg.get("report_not_connected") + ")";
    }
    if (timer != null && timer.isRunning()) {
      timer.stop();
    }
    initiated = false;
  }

  @Override
  public void init(HashMap properties, Component parent, Messages msg) throws Exception {
    this.parent = parent;
    this.msg = msg;
    boolean success = false;
    try {
      super.init(properties, parent, msg);
      String serverPath = (String) properties.get(SERVER_PATH);
      if (serverPath == null)
        serverPath = DEFAULT_SERVER_PATH;
      description = "TCP/IP " + serverPath;
      String serverService = (String) properties.get(SERVER_SERVICE);
      if (serverService == null)
        serverService = DEFAULT_SERVER_SERVICE;
      if (serverPath.length() < 1 || serverService.length() < 1)
        throw new Exception("Bad server specification!");
      if (!serverService.startsWith("/"))
        serverService += "/";
      String serverProtocol = (String) properties.get(SERVER_PROTOCOL);
      if (serverProtocol == null)
        serverProtocol = DEFAULT_SERVER_PROTOCOL;

      serviceUrl = new URL(serverProtocol + "://" + serverPath + serverService);

      if (userId == null)
        userId = promptUserId(parent, msg);

      if (userId != null)
        success = true;

    } catch (Exception ex) {
      msg.showErrorWarning(parent, "report_err_init", description, ex, null);
    }
    if (success) {
      String tl = getProperty(TIME_LAP, Integer.toString(timerLap));
      timerLap = Math.min(300, Math.max(1, Integer.parseInt(tl)));
      timer = new Timer(timerLap * 1000, new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          flushTasks();
        }
      });
      timer.setRepeats(true);
      timer.start();
    } else {
      stopReporting();
    }
  }

  @Override
  public void newSession(JClicProject jcp, Component parent, Messages msg) {
    super.newSession(jcp, parent, msg);
    if (serviceUrl == null)
      return;

    if (userId == null) {
      try {
        userId = promptUserId(parent, msg);
      } catch (Exception ex) {
        msg.showErrorWarning(parent, "error_getting_user", ex);
      }
    }

    if (userId != null) {
      currentSessionId = null;
    }
  }

  public void createDBSession() {
    if (initiated && userId != null) {
      flushTasks();
      currentSessionId = null;
      actCount = 0;
      TCPReportBean bean = new TCPReportBean(TCPReportBean.ADD_SESSION);
      bean.setParam(TCPReportBean.PROJECT, currentSession.projectName);
      bean.setParam(TCPReportBean.TIME, Long.toString(currentSession.timeMillis));
      bean.setParam(TCPReportBean.CODE, currentSession.code);
      bean.setParam(TCPReportBean.USER, userId);
      bean.setParam(TCPReportBean.KEY, sessionKey);
      bean.setParam(TCPReportBean.CONTEXT, sessionContext);
      bean = transaction(bean);
      if (bean != null)
        currentSessionId = bean.getParam(TCPReportBean.SESSION);
      if (currentSessionId == null)
        stopReporting();
    }
  }

  protected void reportActivity() {
    if (lastActivity != null) {
      if (!lastActivity.closed)
        lastActivity.closeActivity();
      if (currentSessionId == null)
        createDBSession();
      if (currentSessionId != null) {
        TCPReportBean bean = new TCPReportBean(TCPReportBean.ADD_ACTIVITY);
        bean.setParam(TCPReportBean.SESSION, currentSessionId);
        bean.setParam(TCPReportBean.NUM, Integer.toString(actCount++));
        bean.setData(lastActivity);
        tasks.add(bean);
      }
    }
    if (currentSession != null && currentSession.currentSequence != null
        && currentSession.currentSequence.currentActivity != lastActivity) {
      lastActivity = currentSession.currentSequence.currentActivity;
    } else
      lastActivity = null;
  }

  @Override
  public String getProperty(String key, String defaultValue) throws Exception {
    if (dbProperties == null) {
      dbProperties = new HashMap<String, String>();
      TCPReportBean bean = transaction(new TCPReportBean(TCPReportBean.GET_PROPERTIES));
      if (bean == null)
        return defaultValue;
      dbProperties.putAll(bean.getParams());
    }
    String result = (String) dbProperties.get(key);
    if (result == null)
      result = defaultValue;
    return result;
  }

  @Override
  public List<GroupData> getGroups() throws Exception {
    TCPReportBean bean = transaction(TCPReportBean.GET_GROUPS, null);
    if (bean == null)
      return new ArrayList<GroupData>();
    Domable[] data = bean.getData();
    ArrayList<GroupData> result = new ArrayList<GroupData>(data.length);
    for (Domable d : data)
      if (d instanceof GroupData)
        result.add((GroupData) d);
    return result;
  }

  @Override
  public List<UserData> getUsers(String groupId) throws Exception {
    TCPReportBean bean = new TCPReportBean(TCPReportBean.GET_USERS);
    bean.setParam(TCPReportBean.GROUP, groupId);
    bean = transaction(bean);
    if (bean == null)
      return new ArrayList<UserData>();
    Domable[] data = bean.getData();
    ArrayList<UserData> result = new ArrayList<UserData>(data.length);
    for (Domable d : data)
      if (d instanceof UserData)
        result.add((UserData) d);
    return result;
  }

  @Override
  public UserData getUserData(String userId) throws Exception {
    UserData result = null;
    TCPReportBean bean = new TCPReportBean(TCPReportBean.GET_USER_DATA);
    bean.setParam(TCPReportBean.USER, userId);
    if ((bean = transaction(bean)) != null)
      result = (UserData) bean.getSingleData();
    return result;
  }

  @Override
  public GroupData getGroupData(String groupId) throws Exception {
    GroupData result = null;
    TCPReportBean bean = new TCPReportBean(TCPReportBean.GET_GROUP_DATA);
    bean.setParam(TCPReportBean.GROUP, groupId);
    if ((bean = transaction(bean)) != null)
      result = (GroupData) bean.getSingleData();
    return result;
  }

  @Override
  public String newGroup(GroupData gd) throws Exception {
    String result = null;
    TCPReportBean bean = new TCPReportBean(TCPReportBean.NEW_GROUP);
    bean.setData(gd);
    if ((bean = transaction(bean)) != null)
      result = (String) bean.getParam(TCPReportBean.GROUP);
    return result;
  }

  @Override
  public String newUser(UserData ud) throws Exception {
    String result = null;
    TCPReportBean bean = new TCPReportBean(TCPReportBean.NEW_USER);
    bean.setData(ud);
    if ((bean = transaction(bean)) != null)
      result = (String) bean.getParam(TCPReportBean.USER);
    return result;
  }

  @Override
  public void newActivity(Activity act) {
    super.newActivity(act);
    reportActivity();
  }
}
