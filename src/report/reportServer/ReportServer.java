/*
 * File    : ReportServer.java
 * Created : 10-aug-2001 13:52
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

import edu.xtec.jclic.ReportServerConstants;
import edu.xtec.jclic.report.*;
import edu.xtec.jclic.report.rp.JClicReportService;
import edu.xtec.jclic.skins.AboutWindow;
import edu.xtec.util.*;
import edu.xtec.util.db.ConnectionBeanProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.*;
import java.util.*;
import javax.swing.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class ReportServer extends javax.swing.JFrame
    implements ReportServerConstants, ResourceBridge {

  HTTPReportServer httpServer;

  // String driver;
  // String url;
  // String system_user;
  // String system_password;
  int httpPort;
  int httpTimeOut;
  Options options;
  Messages msg;
  boolean webVerbose;
  boolean webAutostart;
  java.awt.Image frameIcon;
  File cfgFile;
  rsListener webServerListener;
  java.io.PrintWriter logWeb;
  DateFormat dateFormat, timeFormat;

  public static final String MSG_BUNDLE = "messages.ReportServerMessages";

  // ** Creates new form ReportServer */
  public ReportServer(String[] args) {

    options = new Options(this);
    frameIcon = ResourceManager.getImageIcon("icons/reportServerIcon.png").getImage();

    if (args.length > 0 && args[0] != null) {
      cfgFile = new File(args[0]);
      if (!cfgFile.isAbsolute()) cfgFile = new File(System.getProperty("user.dir"), args[0]);
    }
    if (cfgFile == null || !cfgFile.exists())
      cfgFile =
          new File(
              edu.xtec.util.PersistentSettings.getFilePath(
                  edu.xtec.jclic.Constants.PROGRAM, CFG_FILE, options, true));

    Properties prop = new Properties();
    try {
      prop.load(ReportServerConstants.class.getResourceAsStream(CFG_FILE));
      if (cfgFile.exists()) {
        FileInputStream is = new FileInputStream(cfgFile);
        prop.load(is);
        is.close();
        String pwd = prop.getProperty(ConnectionBeanProvider.DB_PASSWORD);
        if (pwd != null && pwd.length() > 0)
          prop.setProperty(ConnectionBeanProvider.DB_PASSWORD, Encryption.Decrypt(pwd));
      }
      options.syncProperties(prop, false);
      loadSettings();
    } catch (Exception ex) {
      System.err.println("Error reading settings!\n" + ex);
    }

    dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

    httpServer = new HTTPReportServer(msg);

    initComponents();

    webServerListener = new rsListener(webMsgTextArea);
    webServerListener.writer = logWeb;
    httpServer.addListener(webServerListener);
    JClicReportService.eventMaker.addListener(webServerListener);

    pack();
    setVisible(true);
    if (webAutostart) startWebServer();
  }

  private void loadSettings() throws Exception {

    edu.xtec.jclic.report.rp.ReportsRequestProcessor.setProperties(options.toProperties());
    msg = edu.xtec.util.PersistentSettings.getMessages(options, MSG_BUNDLE);
    msg.addBundle(edu.xtec.jclic.Constants.DEFAULT_BUNDLE);
    msg.addBundle(edu.xtec.jclic.Constants.COMMON_SETTINGS);
    options.setLookAndFeel();
    // driver=options.getString(ConnectionBeanProvider.DB_DRIVER);
    // url=options.getString(ConnectionBeanProvider.DB_SERVER);
    // system_user=options.getString(ConnectionBeanProvider.DB_LOGIN);
    // system_password=options.getString(ConnectionBeanProvider.DB_PASSWORD);
    httpPort = options.getInt(HTTP_PORT, HTTPReportServer.DEFAULT_PORT);
    httpTimeOut = options.getInt(HTTP_TIMEOUT, HTTPReportServer.DEFAULT_TIMEOUT);
    ToolTipManager.sharedInstance().setEnabled(options.getBoolean(TOOLTIPS, true));
    webVerbose = options.getBoolean(HTTP_VERBOSE, true);
    webAutostart = options.getBoolean(HTTP_AUTOSTART, true);

    String preferredBrowser = options.getString(BrowserLauncher.BROWSER);
    if (preferredBrowser != null && preferredBrowser.length() > 0)
      edu.xtec.util.BrowserLauncher.setPreferredBrowser(preferredBrowser);

    try {
      String s = options.getString(HTTP_LOGFILE, null);
      if (s != null) {
        FileOutputStream fos = new FileOutputStream(s, true);
        logWeb = new java.io.PrintWriter(fos, true);
        if (webServerListener != null) webServerListener.writer = logWeb;
      }
    } catch (Exception ex) {
      msg.showErrorWarning(this, "error_opening_logs", ex);
    }
  }

  private void startWebServer() {

    if (httpServer.startServer(httpPort, httpTimeOut)) {
      startWebServerBtn.setEnabled(false);
      stopWebServerBtn.setEnabled(true);
      launchBrowserBtn.setEnabled(true);
    } else {
      msg.showErrorWarning(this, "error_serverCannotStart", null);
    }
  }

  void stopWebServer() {
    if (httpServer.serverRunning()) httpServer.stopServer();
    startWebServerBtn.setEnabled(true);
    launchBrowserBtn.setEnabled(false);
    stopWebServerBtn.setEnabled(false);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the FormEditor.
   */
  private void initComponents() { // GEN-BEGIN:initComponents
    java.awt.GridBagConstraints gridBagConstraints;

    webServerPane = new javax.swing.JPanel();
    btnWebPanel = new javax.swing.JPanel();
    startWebServerBtn = new javax.swing.JButton();
    stopWebServerBtn = new javax.swing.JButton();
    launchBrowserBtn = new javax.swing.JButton();
    webMsgPanel = new javax.swing.JPanel();
    webMsgScrollPane = new javax.swing.JScrollPane();
    webMsgTextArea = new javax.swing.JTextArea();
    webVerboseCheckBox = new javax.swing.JCheckBox();
    webClearTextBtn = new javax.swing.JButton();
    webCopyTextBtn = new javax.swing.JButton();
    btnPanel = new javax.swing.JPanel();
    settingsButton = new javax.swing.JButton();
    aboutBtn = new javax.swing.JButton();
    exitBtn = new javax.swing.JButton();

    setTitle(msg.get("form_title"));
    setIconImage(frameIcon);
    setName("");
    addWindowListener(
        new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent evt) {
            exitForm(evt);
          }
        });

    webServerPane.setLayout(new java.awt.GridBagLayout());

    btnWebPanel.setLayout(new java.awt.GridBagLayout());

    startWebServerBtn.setIcon(
        new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/run.gif")));
    startWebServerBtn.setToolTipText(msg.get("form_btn_start_tooltip"));
    startWebServerBtn.setText(msg.get("form_btn_start"));
    startWebServerBtn.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            startWebServerBtnActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(8, 2, 2, 2);
    btnWebPanel.add(startWebServerBtn, gridBagConstraints);

    stopWebServerBtn.setIcon(
        new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/cancel.gif")));
    stopWebServerBtn.setToolTipText(msg.get("form_btn_stop_tooltip"));
    stopWebServerBtn.setText(msg.get("form_btn_stop"));
    stopWebServerBtn.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            stopWebServerBtnActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(8, 2, 2, 2);
    btnWebPanel.add(stopWebServerBtn, gridBagConstraints);

    launchBrowserBtn.setIcon(
        new javax.swing.ImageIcon(
            getClass().getResource("/edu/xtec/resources/icons/browser_small.gif")));
    launchBrowserBtn.setText(msg.get("form_btn_show"));
    launchBrowserBtn.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            launchBrowserBtnActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(8, 2, 2, 2);
    btnWebPanel.add(launchBrowserBtn, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    webServerPane.add(btnWebPanel, gridBagConstraints);

    webMsgPanel.setLayout(new java.awt.GridBagLayout());

    webMsgPanel.setBorder(new javax.swing.border.TitledBorder(msg.get("form_messages_title")));
    webMsgPanel.setMinimumSize(new java.awt.Dimension(300, 200));
    webMsgPanel.setPreferredSize(new java.awt.Dimension(400, 200));
    webMsgTextArea.setEditable(false);
    webMsgScrollPane.setViewportView(webMsgTextArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    webMsgPanel.add(webMsgScrollPane, gridBagConstraints);

    webVerboseCheckBox.setToolTipText(msg.get("form_messages_verbose_tooltip"));
    webVerboseCheckBox.setSelected(webVerbose);
    webVerboseCheckBox.setText(msg.get("form_messages_verbose"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    webMsgPanel.add(webVerboseCheckBox, gridBagConstraints);

    webClearTextBtn.setIcon(
        new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/delete.gif")));
    webClearTextBtn.setToolTipText(msg.get("form_messages_clear_tooltip"));
    webClearTextBtn.setText(msg.get("form_messages_clear"));
    webClearTextBtn.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            webClearTextBtnActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    webMsgPanel.add(webClearTextBtn, gridBagConstraints);

    webCopyTextBtn.setIcon(
        new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/copy.gif")));
    webCopyTextBtn.setToolTipText(msg.get("form_messages_copy_tooltip"));
    webCopyTextBtn.setText(msg.get("form_messages_copy"));
    webCopyTextBtn.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            webCopyTextBtnActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    webMsgPanel.add(webCopyTextBtn, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    webServerPane.add(webMsgPanel, gridBagConstraints);

    getContentPane().add(webServerPane, java.awt.BorderLayout.CENTER);

    btnPanel.setLayout(new java.awt.GridBagLayout());

    settingsButton.setIcon(
        new javax.swing.ImageIcon(
            getClass().getResource("/edu/xtec/resources/icons/settings.gif")));
    settingsButton.setText(msg.get("settings_button"));
    settingsButton.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            settingsButtonActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    btnPanel.add(settingsButton, gridBagConstraints);

    aboutBtn.setIcon(
        new javax.swing.ImageIcon(
            getClass().getResource("/edu/xtec/resources/icons/about_small.gif")));
    aboutBtn.setText(msg.get("ABOUT"));
    aboutBtn.setToolTipText(msg.get("form_btn_exit_tooltip"));
    aboutBtn.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            aboutBtnActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    btnPanel.add(aboutBtn, gridBagConstraints);

    exitBtn.setIcon(
        new javax.swing.ImageIcon(
            getClass().getResource("/edu/xtec/resources/icons/exit_small.gif")));
    exitBtn.setText(msg.get("form_btn_exit"));
    exitBtn.setToolTipText(msg.get("form_btn_exit_tooltip"));
    exitBtn.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            exitBtnActionPerformed(evt);
          }
        });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    btnPanel.add(exitBtn, gridBagConstraints);

    getContentPane().add(btnPanel, java.awt.BorderLayout.SOUTH);
  } // GEN-END:initComponents

  private void aboutBtnActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_aboutBtnActionPerformed

    showAbout();
  } // GEN-LAST:event_aboutBtnActionPerformed

  private void webClearTextBtnActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_webClearTextBtnActionPerformed

    webMsgTextArea.setText("");
  } // GEN-LAST:event_webClearTextBtnActionPerformed

  private void webCopyTextBtnActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_webCopyTextBtnActionPerformed

    webMsgTextArea.selectAll();
    webMsgTextArea.copy();
    webMsgTextArea.select(0, 0);

    // Add your handling code here:
  } // GEN-LAST:event_webCopyTextBtnActionPerformed

  private void launchBrowserBtnActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_launchBrowserBtnActionPerformed

    try {
      java.net.URL page =
          new java.net.URL(
              "http", java.net.InetAddress.getLocalHost().getHostName(), httpPort, "/");
      edu.xtec.util.BrowserLauncher.openURL(page.toExternalForm());
    } catch (Exception ex) {
      msg.showErrorWarning(this, "error", ex);
    }
  } // GEN-LAST:event_launchBrowserBtnActionPerformed

  private void stopWebServerBtnActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_stopWebServerBtnActionPerformed

    stopWebServer();
  } // GEN-LAST:event_stopWebServerBtnActionPerformed

  private void startWebServerBtnActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_startWebServerBtnActionPerformed

    startWebServer();
  } // GEN-LAST:event_startWebServerBtnActionPerformed

  private void settingsButtonActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_settingsButtonActionPerformed

    ReportServerSettingsDlg dlg = new ReportServerSettingsDlg(this, true, options);
    dlg.setVisible(true);
    if (dlg.result) {
      if (cfgFile != null) {
        try {
          Properties prop = options.toProperties();
          String pwd = prop.getProperty(ConnectionBeanProvider.DB_PASSWORD, "");
          if (pwd != null && pwd.length() > 0)
            prop.put(ConnectionBeanProvider.DB_PASSWORD, Encryption.Encrypt(pwd));
          FileOutputStream os = new FileOutputStream(cfgFile);
          prop.store(os, "Jclic report server settings");
          os.close();
        } catch (Exception ex) {
          msg.showErrorWarning(this, "error_writting_settings", ex);
        }
        stopWebServer();

        if (logWeb != null) {
          logWeb.flush();
          logWeb.close();
          logWeb = null;
          webServerListener.writer = null;
        }

        try {
          loadSettings();
        } catch (Exception ex) {
          System.err.println("Error loading settings.\n" + ex);
        }

        if (webAutostart) startWebServer();
      }
    }
  } // GEN-LAST:event_settingsButtonActionPerformed

  private void exitBtnActionPerformed(
      java.awt.event.ActionEvent evt) { // GEN-FIRST:event_exitBtnActionPerformed

    exitForm(null);
  } // GEN-LAST:event_exitBtnActionPerformed

  /** Exit the Application */
  private void exitForm(java.awt.event.WindowEvent evt) { // GEN-FIRST:event_exitForm

    stopWebServer();
    if (logWeb != null) {
      logWeb.flush();
      logWeb.close();
      logWeb = null;
      webServerListener.writer = null;
    }
    System.exit(0);
  } // GEN-LAST:event_exitForm

  /** @param args the command line arguments */
  public static void main(String args[]) {
    // new ReportServer(args).show();
    new ReportServer(args).setVisible(true);
  }

  class rsListener implements ReportServerEventMaker.Listener {

    JTextArea textArea;
    java.io.PrintWriter writer;

    rsListener(JTextArea textArea) {
      this.textArea = textArea;
    }

    public void reportEventPerformed(ReportServerEvent ev) {
      if (webVerbose || ev.type == ReportServerEvent.SYSTEM) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(dateFormat.format(new Date()));
        sb.append(" ").append(ev.toString());
        if (writer != null) writer.println(sb.substring(0));
        textArea.append(sb.append("\n").substring(0));
      }
    }
  }

  // Interface ResourceBridge
  // ******************************************
  public java.io.InputStream getProgressInputStream(
      java.io.InputStream is, int expectedLength, String name) {
    return is;
  }

  public void displayUrl(String url, boolean inFrame) {
    try {
      edu.xtec.util.BrowserLauncher.openURL(url);
    } catch (Exception ex) {
      System.err.println("Error opening URL: " + url);
    }
  }

  public edu.xtec.util.Options getOptions() {
    return options;
  }

  public String getMsg(String key) {
    return msg.get(key);
  }

  public javax.swing.JComponent getComponent() {
    return getRootPane();
  }
  // ******************************************

  protected void showAbout() {
    AboutWindow aw = new AboutWindow(getComponent(), this, new java.awt.Dimension(500, 400));
    try {
      aw.buildAboutTab(
          "JClic Reports",
          getMsg("REPORTS_VERSION"),
          "logo_reports_small.png",
          null,
          null,
          null,
          null);
      aw.buildStandardTab(
          aw.getHtmlSystemInfo(),
          "about_window_systemInfo",
          "about_window_lb_system",
          "icons/system_small.gif");

      aw.setVisible(true);

    } catch (Exception ex) {
      System.err.println("Error building about window!\n" + ex);
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton aboutBtn;
  private javax.swing.JPanel btnPanel;
  private javax.swing.JPanel btnWebPanel;
  private javax.swing.JButton exitBtn;
  private javax.swing.JButton launchBrowserBtn;
  private javax.swing.JButton settingsButton;
  private javax.swing.JButton startWebServerBtn;
  private javax.swing.JButton stopWebServerBtn;
  private javax.swing.JButton webClearTextBtn;
  private javax.swing.JButton webCopyTextBtn;
  private javax.swing.JPanel webMsgPanel;
  private javax.swing.JScrollPane webMsgScrollPane;
  private javax.swing.JTextArea webMsgTextArea;
  private javax.swing.JPanel webServerPane;
  private javax.swing.JCheckBox webVerboseCheckBox;
  // End of variables declaration//GEN-END:variables

}
