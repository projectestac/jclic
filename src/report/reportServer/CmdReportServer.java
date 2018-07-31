/*
 * File    : CmdReportServer.java
 * Created : 16-jul-2001 20:23
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
import edu.xtec.util.Encryption;
import edu.xtec.util.Messages;
import edu.xtec.util.db.ConnectionBeanProvider;
import java.io.*;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class CmdReportServer implements ReportServerEventMaker.Listener, ReportServerConstants {

  static String iDriver, iUrl, iUser, iPwd;
  static int iHttpPort, iHttpTimeout;
  static boolean iCreateTables;
  static String iTablePrefix;
  DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

  public CmdReportServer(
      String driver,
      String url,
      String user,
      String pwd,
      int httpPort,
      int httpTimeOut,
      boolean createTables,
      String tablePrefix)
      throws Exception {
    showValidCommands();

    // ReportServerJDBCBridge bridge=new ReportServerJDBCBridge(driver, url, user, pwd);

    // ConnectionBeanProvider cbp=new PooledConnectionBeanProvider(driver, url, user, pwd, 2, 5,
    // "JClicConnectionPool.log", 1.0);
    // ConnectionBeanProvider cbp=new SingleConnectionBeanProvider(driver, url, user, pwd, true);

    ConnectionBeanProvider cbp =
        ConnectionBeanProvider.getConnectionBeanProvider(true, driver, url, user, pwd, true);
    ReportServerJDBCBridge bridge = new ReportServerJDBCBridge(cbp, createTables, tablePrefix);
    reportEventPerformed(
        new ReportServerEvent(ReportServerEvent.SYSTEM, url, null, ReportServerEvent.CONNECT));

    HTTPReportServer httpServer = new HTTPReportServer(new Messages(ReportServer.MSG_BUNDLE));
    httpServer.addListener(this);
    // httpServer.startServer(bridge, httpPort, timeOut);
    httpServer.startServer(httpPort, httpTimeOut);

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    boolean loop = true;
    String s = null;
    while (loop) {
      try {
        s = in.readLine();
        Thread.sleep(1000);
      } catch (Exception ex) {
        loop = false;
      }
      if (s != null) {
        if (s.equalsIgnoreCase("stop")) {
          httpServer.stopServer();
        } else if (s.equalsIgnoreCase("start")) {
          httpServer.startServer(httpPort, httpTimeOut);
        } else if (s.equalsIgnoreCase("exit")) {
          if (httpServer.serverRunning()) httpServer.stopServer();
          loop = false;
        } else {
          showValidCommands();
        }
      }
    }
    bridge.end();
    // bridge=null;
    reportEventPerformed(
        new ReportServerEvent(ReportServerEvent.SYSTEM, url, null, ReportServerEvent.DISCONNECT));
  }

  public static void main(String[] args) {

    String propFile = CFG_FILE;
    java.util.Properties prop;
    int i = 0;

    if (args.length > 0) {
      if (args[1].equals("-help")) {
        displayHelp();
        return;
      }
      if (!args[0].startsWith("-")) {
        propFile = args[0];
        i++;
      }
    }

    prop = new java.util.Properties();
    try {
      prop.load(ReportServerConstants.class.getResourceAsStream(CFG_FILE));
      if (!CFG_FILE.equals(propFile)) {
        prop.load(CmdReportServer.class.getResourceAsStream(propFile));
        String pwd = prop.getProperty(ConnectionBeanProvider.DB_PASSWORD);
        if (pwd != null && pwd.length() > 0)
          prop.setProperty(ConnectionBeanProvider.DB_PASSWORD, Encryption.Decrypt(pwd));
      }
    } catch (Exception ex) {
      System.err.println("ERROR: Invalid properties file name\n" + ex);
      displayHelp();
      return;
    }

    iDriver =
        prop.getProperty(
            ConnectionBeanProvider.DB_DRIVER,
            edu.xtec.jclic.report.BasicJDBCBridge.DEFAULT_ODBC_BRIDGE);
    iUrl =
        prop.getProperty(
            ConnectionBeanProvider.DB_SERVER, edu.xtec.jclic.report.BasicJDBCBridge.DEFAULT_DB);
    iUser = prop.getProperty(ConnectionBeanProvider.DB_LOGIN, null);
    iPwd = prop.getProperty(ConnectionBeanProvider.DB_PASSWORD, null);
    iHttpPort =
        Integer.parseInt(
            prop.getProperty(HTTP_PORT, Integer.toString(HTTPReportServer.DEFAULT_PORT)));
    iHttpTimeout =
        Integer.parseInt(
            prop.getProperty(HTTP_TIMEOUT, Integer.toString(HTTPReportServer.DEFAULT_TIMEOUT)));
    iTablePrefix = prop.getProperty(BasicJDBCBridge.TABLE_PREFIX_KEY, null);
    iCreateTables =
        "true".equalsIgnoreCase((prop.getProperty(BasicJDBCBridge.CREATE_TABLES_KEY, "true")));

    for (; i < args.length; i++) {
      boolean err = false;
      if (i < args.length - 1) {
        if (args[i].equals("-driver")) iDriver = args[++i];
        else if (args[i].equals("-url")) iUrl = args[++i];
        else if (args[i].equals("-user")) iUser = args[++i];
        else if (args[i].equals("-pwd")) iPwd = args[++i];
        else if (args[i].equals("-port")) iHttpPort = Integer.parseInt(args[++i]);
        else if (args[i].equals("-timeout")) iHttpTimeout = Integer.parseInt(args[++i]);
        else if (args[i].equals("-prefix")) iTablePrefix = args[++i];
        else if (args[i].equals("-create")) iCreateTables = "true".equalsIgnoreCase(args[++i]);
        else err = true;
      } else err = true;
      if (err) {
        System.err.println("Syntax error!");
        displayHelp();
        return;
      }
    }

    try {
      new CmdReportServer(
          iDriver, iUrl, iUser, iPwd, iHttpPort, iHttpTimeout, iCreateTables, iTablePrefix);
      System.err.println("Report Server closed");
    } catch (Exception ex) {
      System.err.println("Unable to start!\n" + ex);
    }
  }

  static void displayHelp() {
    System.out.println("Syntax: java CmdReportServer [-help] [fileName] [-option value]..");
    System.out.println("Switches:");
    System.out.println("  -help     displays this message");
    System.out.println("  fileName  get values from the specified fileName");
    System.out.println("            if ommitted, default values will be read");
    System.out.println("            from ReportServer.properties");
    System.out.println("Option-Value pairs:");
    System.out.println("  -driver   name of the JDBC driver");
    System.out.println("  -url      JDBC path to the database");
    System.out.println("  -user     system user name");
    System.out.println("  -pwd      system user password");
    System.out.println("  -port     HTTP port for foreign connections (default: 9000)");
    System.out.println("  -timeout  maximum number of seconds of socket inactivity");
    System.out.println("  -create   [true|false] automatic creation of tables (default: true)");
    System.out.println("  -prefix   prefix used in table names");
  }

  public void reportEventPerformed(ReportServerEvent ev) {
    System.out.println(dateFormat.format(new Date()) + " " + ev.toString());
  }

  void showValidCommands() {
    System.out.println("-----------------------------------------------------");
    System.out.println("Valid control commands:");
    System.out.println("stop - Close all connections and stop server");
    System.out.println("start - Start server");
    System.out.println("exit - Close all connections, stop server and exit");
    System.out.println("-----------------------------------------------------");
  }
}
