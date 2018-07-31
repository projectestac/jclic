/*
 * File    : ReportsRequestProcessor.java
 * Created : 27-mar-2003 17:11
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

import static edu.xtec.jclic.ReportServerConstants.CFG_FILE;

import edu.xtec.jclic.ReportServerConstants;
import edu.xtec.jclic.report.BasicJDBCBridge;
import edu.xtec.jclic.report.ReportServerJDBCBridge;
import edu.xtec.servlet.RequestProcessor;
import edu.xtec.util.Options;
import edu.xtec.util.db.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public abstract class ReportsRequestProcessor extends RequestProcessor
    implements ReportServerConstants {

  protected static ReportServerJDBCBridge bridge;
  protected static Properties prop;

  public static synchronized void setProperties(Properties prop) {
    if (prop != null) {
      if (bridge != null) {
        bridge.end();
        bridge = null;
      }
    }
    ReportsRequestProcessor.prop = prop;
  }

  public static synchronized Properties getProperties() {
    if (prop == null) prop = new Properties();
    return prop;
  }

  public static synchronized void loadProperties(String file) throws Exception {
    if (prop == null) {
      prop = new Properties();
      prop.load(ReportServerConstants.class.getResourceAsStream(CFG_FILE));
      loadLocalProperties(file);
    }
  }

  public static synchronized void loadLocalProperties(String file) throws Exception {
    if (prop == null) prop = new Properties();
    if (file == null) file = CFG_FILE;
    if (file != null) {
      File f = new File(file);
      if (!f.isAbsolute()) f = new File(System.getProperty("user.home"), file);
      if (f.exists()) {
        FileInputStream is = new FileInputStream(f);
        prop.load(is);
        is.close();
      }
    }
  }

  public static synchronized void initJDBCBridge(String propFile) throws Exception {
    if (prop == null) loadProperties(propFile);
    if (bridge == null) {
      ConnectionBeanProvider cbp =
          ConnectionBeanProvider.getConnectionBeanProvider(true, Options.toStringMap(prop));
      boolean createTables =
          !"false".equalsIgnoreCase((String) prop.get(BasicJDBCBridge.CREATE_TABLES_KEY));
      String tablePrefix = (String) prop.get(BasicJDBCBridge.TABLE_PREFIX_KEY);
      bridge = new ReportServerJDBCBridge(cbp, createTables, tablePrefix);
    }
  }

  @Override
  public boolean init() throws Exception {

    if (!super.init()) return false;

    if (bridge == null) initJDBCBridge(null);

    return true;
  }
}
