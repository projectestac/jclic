/*
 * File    : PersistentSettings.java
 * Created : 28-jun-2002 16:20
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

package edu.xtec.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class PersistentSettings {

  public static final String BASE = ".edu.xtec.properties";
  public static final String BASE_OLD = "edu.xtec.properties";
  public static final String BUNDLE = "messages.PersistentPathsMessages";
  public static final String BASE_PREFS_NODE = "/edu/xtec";
  public static final String JCLIC_PREFS_NODE = "/edu/xtec/jclic";

  public static Preferences userPrefs = Preferences.userRoot().node(BASE_PREFS_NODE);
  public static Preferences systemPrefs = Preferences.systemRoot().node(JCLIC_PREFS_NODE);

  // Gets basic preferences for "edu.xtec" programs.
  private static Properties getProperties() throws Exception {

    Properties prop = new Properties();

    // Try to read from new "Preferences" object
    String[] keys = userPrefs.keys();
    if (keys.length == 0) {
      // No keys left. Try to load from previous settings
      StringBuilder sb = new StringBuilder(300);
      sb.append(System.getProperty("user.dir"));
      sb.append(File.pathSeparator);
      sb.append(System.getProperty("user.home"));
      sb.append(File.pathSeparator);
      sb.append(System.getProperty("java.home")).append(File.separator).append("lib");
      sb.append(File.pathSeparator).append(System.getProperty("java.ext.dirs"));

      File baseFile = null;
      StringTokenizer st = new StringTokenizer(sb.substring(0), File.pathSeparator);
      while (baseFile == null && st.hasMoreTokens()) {
        String s = st.nextToken();
        File dir = new File(s);
        if (dir.exists() && dir.isDirectory()) {
          File testFile = new File(dir, BASE);
          if (testFile.canRead()) {
            baseFile = testFile;
          } else {
            testFile = new File(dir, BASE_OLD);
            if (testFile.canRead()) {
              baseFile = testFile;
            }
          }
        }
      }

      if (baseFile != null) {
        // Found old settings
        // Read it and store into "userPrefs"
        InputStream is = new FileInputStream(baseFile);
        prop.load(is);
        is.close();
        saveSettings(prop);
      }
    } else {
      // Read current settings from "userPrefs"
      for (String k : keys) {
        prop.put(k, userPrefs.get(k, ""));
      }
    }
    return prop;
  }

  public static File getBasePathTo(String programName, Options options) throws Exception {

    Properties prop = getProperties();

    String path = (String) prop.get(programName);
    if (path != null) {
      File d = new File(path);
      if (!d.exists() || !d.isDirectory()) {
        path = null;
      }
    }
    if (path == null) {
      // "Program Files" path is no longer used as default for storing data
      // instead, we will use always {user.home}/programName
      String sPath = System.getProperty("user.home") + File.separator + programName;
      Messages messages = getMessages(options, BUNDLE);
      String msg = messages.get("cl_alert");
      int i = msg.indexOf('$');
      if (i >= 0) {
        msg = msg.substring(0, i) + programName + msg.substring(i + 1);
      }
      boolean done = false;
      File d = new File(sPath);
      while (!done) {
        String result = (String) JOptionPane.showInputDialog(options.getMainComponent(), msg,
            messages.get("cl_prompt_title"), JOptionPane.QUESTION_MESSAGE, null, null, sPath);

        if (result == null)
          throw new Exception("bad user input!");

        d = new File(result);
        try {
          if (!d.exists() || !d.isDirectory())
            d.mkdirs();
          done = d.exists() && d.isDirectory();
        } catch (Exception ex) {
          messages.showErrorWarning(options.getMainComponent(), "cl_err_unableToCreateDir", ex);
        }
      }

      path = d.getAbsolutePath();
      prop.setProperty(programName, path);
      saveSettings(prop);
    }
    return new File(path);
  }

  private static void saveSettings(Properties prop) throws Exception {

    Enumeration keys = prop.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      String value = prop.getProperty(key, "");
      userPrefs.put(key, value);
    }
    userPrefs.flush();
  }

  public static Messages getMessages(Options options, String bundle) throws Exception {
    Messages msg = (Messages) options.get(Messages.MESSAGES);
    if (msg == null) {
      boolean mustSave = false;
      String l = (String) options.get(Messages.LANGUAGE);
      Properties prop = null;
      if (l == null) {
        prop = getProperties();
        l = prop.getProperty(Messages.LANGUAGE);
        if (l != null)
          options.put(Messages.LANGUAGE, l);
        else
          mustSave = true;
      } else {
        options.put(Options.LANGUAGE_BY_PARAM, Options.BTRUE);
      }
      msg = Messages.getMessages(options, bundle);
      if (mustSave && prop != null) {
        prop.setProperty(Messages.LANGUAGE, (String) options.get(Messages.LANGUAGE));
        saveSettings(prop);
      }
    } else if (bundle != null)
      msg.addBundle(bundle);

    return msg;
  }

  public static String getFilePath(String programName, String fileName, Options options, boolean createFile) {
    String result = null;
    File base;
    try {
      base = getBasePathTo(programName, options);
    } catch (Exception ex) {
      base = new File(System.getProperty("user.home"));
    }

    File testFile = new File(base, fileName);

    if (testFile.canRead())
      result = testFile.getAbsolutePath();
    else if (createFile)
      try {
        FileOutputStream fos = new FileOutputStream(testFile);
        fos.close();
        testFile.delete();
      } catch (Exception ex) {
        // Unable to create file in selected dir
        // It will be created on {user.dir}
        testFile = new File(fileName);
      } finally {
        result = testFile.getAbsolutePath();
      }
    return result;
  }
}
