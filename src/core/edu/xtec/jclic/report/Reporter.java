/*
 * File    : Reporter.java
 * Created : 09-jul-2001 17:04
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

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.report.SessionReg.Info;
import edu.xtec.util.CompoundListCellRenderer;
import edu.xtec.util.Encryption;
import edu.xtec.util.Html;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.StrUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * This class implements the basic operations related with the process of the results obtained by
 * users playing JClic activities. These operations include users identification, compilation of
 * data coming from the activities, storage of this data for a posterior use and presentation of
 * summarized results.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class Reporter extends Object {

  String userId;
  String sessionKey;
  String sessionContext;
  String groupCodeFilter;
  String userCodeFilter;
  String description;
  Date started;
  List<SessionReg> sessions;
  SessionReg currentSession;
  /** <I>true</I> if the system was successfully initiated, <I>false</I> otherwise */
  public boolean initiated;
  /**
   * <I>true</I> when the system is connected to a database with user's data. When <I>false</I>, a
   * generic user id will be used.
   */
  protected Boolean bUserBased;

  public static final String ALLOW_CREATE_GROUPS = "ALLOW_CREATE_GROUPS",
      ALLOW_CREATE_USERS = "ALLOW_CREATE_USERS",
      SHOW_GROUP_LIST = "SHOW_GROUP_LIST",
      SHOW_USER_LIST = "SHOW_USER_LIST",
      USER_TABLES = "USER_TABLES",
      TIME_LAP = "TIME_LAP";

  /** Creates new Reporter */
  public Reporter() {
    sessions = new CopyOnWriteArrayList<SessionReg>();
    started = new Date();
    initiated = false;
  }

  /**
   * Method use to retrieve specific properties of the reports system
   *
   * @param key Property key
   * @param defaultValue Default value for this key
   * @throws Exception Throwed when the system was unable to read the specified property
   * @return The value related to this property
   */
  public String getProperty(String key, String defaultValue) throws Exception {
    return defaultValue;
  }

  /**
   * Retrieves boolean properties of the reports system
   *
   * @param key
   * @param defaultValue
   * @throws Exception
   * @return
   */
  public boolean getBooleanProperty(String key, boolean defaultValue) throws Exception {
    String s = getProperty(key, new Boolean(defaultValue).toString());
    return key == null ? defaultValue : Boolean.valueOf(s).booleanValue();
  }

  public List<GroupData> getGroups() throws Exception {
    return null;
  }

  public List<UserData> getUsers(String groupId) throws Exception {
    return null;
  }

  public UserData getUserData(String userId) throws Exception {
    return null;
  }

  public GroupData getGroupData(String groupId) throws Exception {
    return null;
  }

  public boolean userBased() throws Exception {
    if (bUserBased == null) bUserBased = getBooleanProperty(USER_TABLES, true);
    return bUserBased.booleanValue();
  }

  protected GroupData promptForNewGroup(Component parent, Messages msg) throws Exception {
    JTextField gName = new JTextField(20);
    JTextField gId = new JTextField(20);
    JComponent[] jc = new JComponent[] {gName, gId};
    String[] ps = new String[] {"report_name_prompt", "report_id_prompt"};
    String name;
    String id;
    GroupData result = null;
    while (result == null) {
      boolean dlgResult =
          msg.showInputDlg(
              parent, new String[] {"report_new_group_data"}, ps, jc, "report_new_group");
      if (!dlgResult) return null;
      name = StrUtils.nullableString(gName.getText());
      id = StrUtils.nullableString(gId.getText());
      if (id == null) msg.showAlert(parent, "report_err_bad_id");
      else if (getGroupData(id) != null) msg.showAlert(parent, "report_err_duplicate_id");
      else if (name == null) msg.showAlert(parent, "report_err_bad_name");
      else {
        result = new GroupData(id, name, null, null);
        result.setId(newGroup(result));
      }
    }
    return result;
  }

  protected UserData promptForNewUser(Component parent, Messages msg, String groupId)
      throws Exception {

    if (groupId == null) {
      groupId = promptGroupId(parent, msg);
      if (groupId == null) return null;
    }
    JTextField gName = new JTextField(20);
    JTextField gId = new JTextField(20);
    JPasswordField pwf1 = new JPasswordField(Messages.MAX_PASSWORD_LENGTH);
    JPasswordField pwf2 = new JPasswordField(Messages.MAX_PASSWORD_LENGTH);
    JComponent[] jc = new JComponent[] {gName, gId, pwf1, pwf2};
    String[] ps =
        new String[] {
          "report_name_prompt", "report_id_prompt", "report_pw_prompt", "report_pw_prompt_confirm"
        };
    String name;
    String id;
    String pw;
    UserData result = null;
    while (result == null) {
      if (!msg.showInputDlg(
          parent, new String[] {"report_new_user_data"}, ps, jc, "report_new_user")) return null;

      name = StrUtils.nullableString(gName.getText());
      id = StrUtils.nullableString(gId.getText());
      pw = StrUtils.nullableString(String.copyValueOf(pwf1.getPassword()));
      boolean pwOk = true;
      if (pw != null) {
        pwOk = pw.equals(StrUtils.nullableString(String.copyValueOf(pwf2.getPassword())));
        pw = Encryption.Encrypt(pw);
      }
      if (!pwOk) msg.showAlert(parent, "report_err_bad_pwd");
      else if (id == null) msg.showAlert(parent, "report_err_bad_id");
      else if (getUserData(id) != null) msg.showAlert(parent, "report_err_duplicate_id");
      else if (name == null) msg.showAlert(parent, "report_err_bad_name");
      else {
        result = new UserData(id, name, null, pw, groupId);
        result.setId(newUser(result));
      }
    }
    return result;
  }

  protected String promptGroupId(Component parent, final Messages msg) throws Exception {
    String groupId = null;
    if (getGroups().isEmpty()) {
      String s = msg.get("report_generic_group_name");
      newGroup(new GroupData(s, s, null, null));
      if (getGroups().isEmpty()) return groupId;
    }
    final List<GroupData> vg = getGroups();
    final JList<Object> list = new JList<Object>();
    list.setCellRenderer(new CompoundListCellRenderer());
    list.setListData(vg.toArray());
    list.setSelectedValue(vg.get(0), false);
    JScrollPane listScroll = new JScrollPane(list);
    JComponent[] jc;
    if (getBooleanProperty(ALLOW_CREATE_GROUPS, false)) {
      JButton btn = new JButton(msg.get("report_new_group"));
      btn.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              Component cmp = null;
              if (evt.getSource() != null && evt.getSource() instanceof Component)
                cmp = (Component) evt.getSource();
              try {
                GroupData gd = promptForNewGroup(cmp, msg);
                if (gd != null) {
                  vg.add(gd);
                  list.setListData(vg.toArray());
                  list.setSelectedValue(gd, true);
                }
              } catch (Exception ex) {
                msg.showErrorWarning(cmp, "report_err_creating_group", ex);
              }
            }
          });
      jc = new JComponent[] {listScroll, btn};
    } else {
      jc = new JComponent[] {listScroll};
    }

    if (msg.showInputDlg(
        parent, new String[] {"report_grouplist_title"}, null, jc, "report_ident_user")) {
      GroupData gd = (GroupData) list.getSelectedValue();
      if (gd != null) {
        groupId = gd.getId();
      }
    }
    return groupId;
  }

  public String promptUserId(Component parent, final Messages msg) throws Exception {
    if (!userBased()) throw new Exception("No users defined in the database!");

    boolean cancel = false;
    int tries = 0;

    while (userId == null && !cancel && tries++ < 3) {
      if (getBooleanProperty(SHOW_USER_LIST, true)) {
        String gi = null;
        if (getBooleanProperty(SHOW_GROUP_LIST, true)) {
          gi = promptGroupId(parent, msg);
          if (gi == null) return null;
        }
        final String groupId = gi;
        final List<UserData> v = getUsers(groupId);
        boolean allow_create_users = getBooleanProperty(ALLOW_CREATE_USERS, false);
        if (v.isEmpty() && !allow_create_users) {
          msg.showErrorWarning(
              parent,
              groupId == null ? "report_err_no_users" : "report_err_no_users_in_group",
              null);
          break;
        }
        final JList<Object> list = new JList<Object>();
        list.setCellRenderer(new CompoundListCellRenderer());
        list.setListData(v.toArray());
        if (!v.isEmpty()) list.setSelectedValue(v.get(0), false);
        JScrollPane listScroll = new JScrollPane(list);
        JComponent[] jc;
        if (allow_create_users) {
          JButton btn = new JButton(msg.get("report_new_user"));
          btn.setToolTipText(msg.get("report_new_user_tooltip"));
          btn.addActionListener(
              new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                  Component cmp = null;
                  if (evt.getSource() != null && evt.getSource() instanceof Component)
                    cmp = (Component) evt.getSource();
                  try {
                    UserData ud = promptForNewUser(cmp, msg, groupId);
                    if (ud != null) {
                      v.add(ud);
                      list.setListData(v.toArray());
                      list.setSelectedValue(ud, true);
                    }
                  } catch (Exception ex) {
                    msg.showErrorWarning(cmp, "report_err_creating_user", ex);
                  }
                }
              });
          jc = new JComponent[] {listScroll, btn};
        } else {
          jc = new JComponent[] {listScroll};
        }

        if (!msg.showInputDlg(
            parent, new String[] {"report_userlist_title"}, null, jc, "report_ident_user")) break;

        UserData ud = (UserData) list.getSelectedValue();
        if (ud == null) break;

        if (ud.pwd != null && ud.pwd.length() > 0) {
          String pwd = Encryption.Decrypt(ud.pwd);
          String inputPwd =
              msg.showInputDlg(
                  parent,
                  "report_user_has_pwd",
                  "report_pw_prompt",
                  null,
                  "report_ident_user",
                  true);

          // 22-mai-06: Modified to avoid null password entries
          // Old version:
          /*
          if(inputPwd==null)
              break;
          else if(inputPwd.equals(pwd))
              userId=ud.getId();
          else
              msg.showErrorWarning(parent, "report_err_invalid_user", null);
           */
          // New version:
          if (pwd.equals(inputPwd)) userId = ud.getId();
          else msg.showErrorWarning(parent, "report_err_invalid_user", null);
        } else userId = ud.getId();
      } else {
        JTextField textField = new JTextField();
        JPasswordField pwdField = new JPasswordField(Messages.MAX_PASSWORD_LENGTH);
        if (!msg.showInputDlg(
            parent,
            new String[] {"report_select_user"},
            new String[] {"report_id_prompt", "report_pw_prompt"},
            new JComponent[] {textField, pwdField},
            "report_ident_user")) break;

        String s = StrUtils.nullableString(textField.getText());
        if (s == null) break;
        UserData ud = getUserData(s);

        boolean userOk = false;
        if (ud != null) {
          String uPwd = StrUtils.nullableString(ud.pwd);
          if (uPwd == null
              || Encryption.Decrypt(uPwd).equals(String.copyValueOf(pwdField.getPassword())))
            userOk = true;
        }
        if (!userOk) msg.showErrorWarning(parent, "report_err_invalid_user", null);
        else userId = ud == null ? "" : ud.getId();
      }
    }
    return userId;
  }

  public String toHtmlString(Messages msg) {
    String prefix = "report_";
    Html html = new Html(3000);
    Html tb;

    tb = new Html(3000);
    tb.doubleCell(msg.get(prefix + "started"), true, msg.getShortDateTimeStr(started), false);
    tb.doubleCell(
        msg.get(prefix + "system"),
        true,
        description == null ? msg.get(prefix + "system_standard") : description,
        false);
    if (userId != null) tb.doubleCell(msg.get(prefix + "user"), true, userId, false);

    int numSessions = 0;
    int numSequences = 0;
    int nActivities = 0;
    int nActSolved = 0;
    int nActScore = 0;
    int nActions = 0;
    int percentSolved = 0;
    long tScore = 0;
    long tTime = 0L;
    Iterator<SessionReg> it = sessions.iterator();
    while (it.hasNext()) {
      Info inf = it.next().getInfo(true);
      if (inf.numSequences > 0) {
        numSessions++;
        numSequences += inf.numSequences;
        if (inf.nActivities > 0) {
          nActivities += inf.nActivities;
          nActSolved += inf.nActSolved;
          nActions += inf.nActions;
          if (inf.nActScore > 0) {
            tScore += (inf.tScore * inf.nActScore);
            nActScore += inf.nActScore;
          }
          tTime += inf.tTime;
        }
      }
    }
    if (numSequences > 0) {
      if (numSessions > 1)
        tb.doubleCell(msg.get(prefix + "num_projects"), true, msg.getNumber(numSessions), false);
      tb.doubleCell(msg.get(prefix + "num_sequences"), true, msg.getNumber(numSequences), false);
      tb.doubleCell(msg.get(prefix + "num_activities"), true, msg.getNumber(nActivities), false);
      if (nActivities > 0) {
        tb.doubleCell(
            msg.get(prefix + "num_activities_solved"),
            true,
            msg.getNumber(nActSolved) + " (" + msg.getPercent(nActSolved * 100 / nActivities) + ")",
            false);
        if (nActScore > 0)
          tb.doubleCell(
              msg.get(prefix + "global_score"), true, msg.getPercent(tScore / nActScore), false);
        tb.doubleCell(msg.get(prefix + "total_time"), true, msg.getHmsTime(tTime), false);
        tb.doubleCell(msg.get(prefix + "num_actions"), true, msg.getNumber(nActions), false);
      }
      html.append(Html.table(tb.toString(), null, 0, 2, -1, null, false)).append(Html.NBSP);

      StringBuilder tbs = new StringBuilder();
      it = sessions.iterator();
      while (it.hasNext()) {
        SessionReg sr = it.next();
        if (sr.getInfo(false).numSequences > 0)
          tbs.append(sr.toHtmlString(msg, false, numSessions > 1));
      }
      html.append(Html.table(tbs.substring(0), null, 1, 2, -1, null, false));
    } else {
      html.append(Html.table(tb.toString(), null, 0, 2, -1, null, false));
      html.br().bold(msg.get(prefix + "no_activities"));
    }

    return html.toString();
  }

  public static final String ELEMENT_NAME = "reporter";
  public static final String USER_ID = "user",
      KEY = "key",
      CONTEXT = "context",
      GROUP_CODE_FILTER = "groupCodeFilter",
      USER_CODE_FILTER = "userCodeFilter";

  public void init(HashMap properties, Component parent, Messages msg) throws Exception {
    userId = (String) properties.get(USER_ID);
    sessionKey = (String) properties.get(KEY);
    sessionContext = (String) properties.get(CONTEXT);
    groupCodeFilter = (String) properties.get(GROUP_CODE_FILTER);
    userCodeFilter = (String) properties.get(USER_CODE_FILTER);
    initiated = true;
  }

  public static Reporter getReporter(HashMap properties, Component parent, Messages msg)
      throws Exception {
    String className;
    if (properties == null)
      throw new IllegalArgumentException("Null properties passed to \"getReporter\"");
    if ((className = (String) properties.get(JDomUtility.CLASS)) == null)
      throw new IllegalArgumentException(
          "Properties passed to \"getReporter\" with null class name");
    Class reporterClass = Class.forName(className);
    Reporter rep = (Reporter) reporterClass.newInstance();
    rep.init(properties, parent, msg);
    return rep;
  }

  public static Reporter getReporter(
      String className, String strProperties, Component parent, Messages msg) throws Exception {
    if (className == null || className.length() == 0)
      throw new IllegalArgumentException(
          "Properties passed to \"getReporter\" with null class name");

    if (className.indexOf('.') < 0) {
      className = "edu.xtec.jclic.report." + className;
    }
    HashMap<String, Object> properties = new HashMap<String, Object>();
    properties.put(JDomUtility.CLASS, className);
    Options.strToMap(strProperties, properties, ";", '=', false);
    return getReporter(properties, parent, msg);
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      end();
    } finally {
      super.finalize();
    }
  }

  public void end() {
    endSession();
  }

  public void endSequence() {
    if (currentSession != null) currentSession.endSequence();
  }

  public void endSession() {
    endSequence();
    currentSession = null;
  }

  public String newGroup(GroupData gd) throws Exception {
    throw new Exception("No database!");
  }

  public String newUser(UserData ud) throws Exception {
    throw new Exception("No database!");
  }

  public void newSession(JClicProject jcp, Component parent, Messages msg) {
    endSession();
    currentSession = new SessionReg(jcp);
    sessions.add(currentSession);
  }

  public void newSequence(ActivitySequenceElement ase) {
    if (currentSession != null) currentSession.newSequence(ase);
  }

  public void newActivity(Activity act) {
    if (currentSession != null) currentSession.newActivity(act);
  }

  public void endActivity(int score, int numActions, boolean solved) {
    if (currentSession != null) currentSession.endActivity(score, numActions, solved);
  }

  public void newAction(String type, String source, String dest, boolean ok) {
    if (currentSession != null) currentSession.newAction(type, source, dest, ok);
  }

  public edu.xtec.jclic.report.SequenceReg.Info getCurrentSequenceInfo() {
    return currentSession == null ? null : currentSession.getCurrentSequenceInfo();
  }

  public String getCurrentSequenceTag() {
    return currentSession == null ? null : currentSession.getCurrentSequenceTag();
  }
}
