/*
 * File    : PlayerSettings.java
 * Created : 19-jun-2002 10:22
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
package edu.xtec.jclic;

import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.project.LibraryManager;
import edu.xtec.util.BrowserLauncher;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.LFUtil;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceBridge;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class PlayerSettings extends Object implements Cloneable, edu.xtec.jclic.Constants, Domable {

  public ResourceBridge rb;
  public LibraryManager libraryManager;
  public String language, country, variant;
  public String reporterClass, reporterParams;
  public boolean reporterEnabled;
  public boolean soundEnabled, systemSounds;
  public String lookAndFeel;
  public String skin;
  public Map<String, String> misc;
  public String rootPath;
  public String mediaSystem;
  public FileSystem fileSystem;
  public static int MAX_RECENT = 8;
  public String[] recentFiles;
  public String preferredBrowser;
  protected String password;
  protected boolean passwordConfirmed;
  public String cfgFile;
  public boolean readOnly;
  protected static String defaultCfgFile = null;
  public static final String PROJECTS_PATH = "projects", CFG_FILE = "jclic.cfg", DEFAULT_SKIN = "@default.xml",
      DEFAULT_REPORTER = "TCPReporter", DEFAULT_REPORTER_PARAMS = "path=localhost:9000";
  public boolean fressaEnabled;

  /** Creates new PlayerSettings */
  public PlayerSettings(ResourceBridge rb, String fromPath /* , Options options */) {
    this.rb = rb;
    password = null;
    misc = new HashMap<String, String>();
    reporterEnabled = false;
    reporterClass = DEFAULT_REPORTER;
    reporterParams = DEFAULT_REPORTER_PARAMS;
    soundEnabled = true;
    systemSounds = true;
    lookAndFeel = edu.xtec.util.LFUtil.DEFAULT;
    preferredBrowser = "";
    skin = DEFAULT_SKIN;
    recentFiles = new String[MAX_RECENT];
    mediaSystem = DEFAULT;
    readOnly = false;
    rootPath = System.getProperty("user.home");
    cfgFile = (fromPath == null ? getDefaultCfgFile(rb.getOptions()) : fromPath);
    fileSystem = new FileSystem(rb);
    if (FileSystem.isStrUrl(cfgFile)) {
      readOnly = true;
    } else {
      File f = new File(cfgFile);
      readOnly = !f.canWrite();
    }
    libraryManager = new LibraryManager(this);
    passwordConfirmed = false;
  }

  public static final String ELEMENT_NAME = "JClicSettings", REPORTER = "reporter", CLASS = "class", PARAMS = "params",
      SOUND = "sound", SYSTEM = "system", MISC = "misc", PATHS = "paths", PATH = "path", ROOT = "root",
      RECENT_FILES = "recentFiles", FILE = "file", PASSWORD = "password";

  public static String getDefaultCfgFile(Options options) {
    if (defaultCfgFile == null) {
      defaultCfgFile = edu.xtec.util.PersistentSettings.getFilePath(PROGRAM, CFG_FILE, options, true);
    }
    return defaultCfgFile;
  }

  public static PlayerSettings loadPlayerSettings(ResourceBridge rb) {
    PlayerSettings result = null;

    String cfgFile = rb.getOptions().getString(ELEMENT_NAME);

    if (cfgFile != null) {
      try {
        result = new PlayerSettings(rb, cfgFile);
        result.loadSettings(result.fileSystem.getXMLDocument(cfgFile).getRootElement());
      } catch (Exception ex) {
        result = null;
        System.err.println("Unable to read settings from " + cfgFile + "\n" + ex);
      }
    }
    if (result == null) {
      cfgFile = getDefaultCfgFile(rb.getOptions());
      result = new PlayerSettings(rb, cfgFile);
      try {
        result.loadSettings(result.fileSystem.getXMLDocument(cfgFile).getRootElement());
      } catch (Exception ex) {
        result.initNewSettings();
      }
    }

    rb.getOptions().syncProperties(result.getProperties(), true);
    if (!rb.getOptions().getBoolean(Options.LANGUAGE_BY_PARAM) && result.language != null) {
      rb.getOptions().put(Messages.LANGUAGE, result.language);
      if (result.country != null) {
        rb.getOptions().put(Messages.COUNTRY, result.country);
      }
      if (result.variant != null) {
        rb.getOptions().put(Messages.VARIANT, result.variant);
      }
    }
    return result;
  }

  private void initNewSettings() {
    try {
      Messages msg = rb.getOptions().getMessages();
      if (language == null) {
        language = msg.getLocale().getLanguage();
        country = msg.getLocale().getCountry();
        variant = msg.getLocale().getVariant();
      }
      readOnly = false;
      File f = new File(new File(cfgFile).getParentFile(), PROJECTS_PATH);
      f.mkdirs();
      rootPath = f.getAbsolutePath();
      fileSystem = new FileSystem(rootPath, rb);
    } catch (Exception ex) {
      System.err.println("Unable to create new settings!\n" + ex);
    }
  }

  public void checkLibrary() {
    if (!readOnly && libraryManager.isEmpty() && rootPath != null && !FileSystem.isStrUrl(rootPath)) {
      File f = new File(new File(rootPath), "library.jclic");
      try {
        libraryManager.addNewLibrary(f.getAbsolutePath(), null);
        save();
      } catch (Exception ex) {
        System.err.println("Unable to create new project library!\n" + ex);
      }
    }
  }

  public void save() {
    if (!readOnly) {
      try {
        FileOutputStream fos = fileSystem.createSecureFileOutputStream(cfgFile);
        JDomUtility.saveDocument(fos, getJDomElement());
        fos.close();
      } catch (Exception ex) {
        System.err.println("unable to save settings in " + cfgFile + ": " + ex.getMessage());
      }
    }
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    org.jdom.Element child, child2;

    e.addContent(libraryManager.getJDomElement());

    if (password != null) {
      e.setAttribute(PASSWORD, edu.xtec.util.Encryption.Encrypt(password));
    }

    if (language != null) {
      child = new org.jdom.Element(Messages.LANGUAGE);
      child.setAttribute(ID, language);
      if (country != null) {
        child.setAttribute(Messages.COUNTRY, country);
      }
      if (variant != null) {
        child.setAttribute(Messages.VARIANT, variant);
      }
      e.addContent(child);
    }

    child = new org.jdom.Element(PATHS);
    child2 = new org.jdom.Element(PATH);
    child2.setAttribute(ID, ROOT);
    child2.setAttribute(PATH, rootPath);
    child.addContent(child2);
    e.addContent(child);

    child = new org.jdom.Element(REPORTER);
    child.setAttribute(ENABLED, JDomUtility.boolString(reporterEnabled));
    child.setAttribute(CLASS, reporterClass);
    child.setAttribute(PARAMS, reporterParams);
    e.addContent(child);

    child = new org.jdom.Element(SOUND);
    child.setAttribute(ENABLED, JDomUtility.boolString(soundEnabled));
    child.setAttribute(SYSTEM, JDomUtility.boolString(systemSounds));
    child.setAttribute(MEDIA_SYSTEM, mediaSystem);
    e.addContent(child);

    child = new org.jdom.Element(LFUtil.LOOK_AND_FEEL);
    child.setAttribute(ID, lookAndFeel);
    e.addContent(child);

    child = new org.jdom.Element(BrowserLauncher.BROWSER);
    child.setAttribute(ID, preferredBrowser);
    e.addContent(child);

    child = new org.jdom.Element(SKIN);
    child.setAttribute(ID, skin);
    e.addContent(child);

    if (!misc.isEmpty()) {
      child = new org.jdom.Element(MISC);
      for (java.util.Map.Entry me : misc.entrySet()) {
        String k = (String) me.getKey();
        Object v = me.getValue();
        if (k != null && v != null && v instanceof String) {
          child.setAttribute(k, (String) v);
        }
      }
      e.addContent(child);
    }

    child = new org.jdom.Element(RECENT_FILES);
    for (int i = 0; i < MAX_RECENT; i++) {
      if (recentFiles[i] != null) {
        child2 = new org.jdom.Element(FILE);
        child2.setAttribute(PATH, recentFiles[i]);
        child.addContent(child2);
      }
    }
    e.addContent(child);

    return e;
  }

  public void loadSettings(org.jdom.Element e) throws Exception {
    setProperties(e, null);
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    org.jdom.Element child, child2;

    password = JDomUtility.getStringAttr(e, PASSWORD, password, false);
    if (password != null) {
      password = edu.xtec.util.Encryption.Decrypt(password);
    }

    if ((child = e.getChild(LibraryManager.ELEMENT_NAME)) != null) {
      libraryManager = LibraryManager.getLibraryManager(this, child);
    }
    if ((child = e.getChild(PATHS)) != null) {
      String rp = null;
      Iterator it = child.getChildren(PATH).iterator();
      while (it.hasNext()) {
        child2 = (org.jdom.Element) it.next();
        if (ROOT.equals(child2.getAttributeValue(ID))) {
          rp = JDomUtility.getStringAttr(child2, PATH, rootPath, false);
        }
      }
      if (rp != null) {
        rootPath = rp;
        if (!FileSystem.isStrUrl(rootPath)) {
          fileSystem = new FileSystem(rootPath, rb);
        }
      }
    }
    if ((child = e.getChild(Messages.LANGUAGE)) != null) {
      language = JDomUtility.getStringAttr(child, ID, language, false);
      country = JDomUtility.getStringAttr(child, Messages.COUNTRY, country, false);
      variant = JDomUtility.getStringAttr(child, Messages.VARIANT, variant, false);
    }
    if ((child = e.getChild(REPORTER)) != null) {
      reporterEnabled = JDomUtility.getBoolAttr(child, ENABLED, reporterEnabled);
      reporterClass = JDomUtility.getStringAttr(child, CLASS, reporterClass, false);
      reporterParams = JDomUtility.getStringAttr(child, PARAMS, reporterParams, false);
    }
    if ((child = e.getChild(SOUND)) != null) {
      soundEnabled = JDomUtility.getBoolAttr(child, ENABLED, soundEnabled);
      systemSounds = JDomUtility.getBoolAttr(child, SYSTEM, systemSounds);
      mediaSystem = JDomUtility.getStringAttr(child, MEDIA_SYSTEM, mediaSystem, false);
    }
    if ((child = e.getChild(LFUtil.LOOK_AND_FEEL)) != null) {
      lookAndFeel = JDomUtility.getStringAttr(child, ID, lookAndFeel, false);
    }
    if ((child = e.getChild(BrowserLauncher.BROWSER)) != null) {
      preferredBrowser = JDomUtility.getStringAttr(child, ID, preferredBrowser, false);
      BrowserLauncher.setPreferredBrowser(preferredBrowser);
    }
    if ((child = e.getChild(SKIN)) != null) {
      skin = JDomUtility.getStringAttr(child, ID, skin, false);
    }
    if ((child = e.getChild(MISC)) != null) {
      java.util.Iterator it = child.getAttributes().iterator();
      while (it.hasNext()) {
        org.jdom.Attribute a = (org.jdom.Attribute) it.next();
        misc.put(a.getName(), a.getValue());
      }
    }
    if ((child = e.getChild(RECENT_FILES)) != null) {
      java.util.Iterator it = child.getChildren(FILE).iterator();
      for (int i = 0; i < MAX_RECENT; i++) {
        if (it.hasNext()) {
          recentFiles[i] = JDomUtility.getStringAttr(((org.jdom.Element) it.next()), PATH, null, false);
        } else {
          recentFiles[i] = null;
        }
      }
    }
  }

  public Messages getMessages() {
    return rb.getOptions().getMessages();
  }

  public Map getProperties() {
    HashMap<String, Object> prop = new HashMap<String, Object>();

    String t = TRUE;
    String f = FALSE;

    if (language != null) {
      prop.put(Messages.LANGUAGE, language);
      prop.put(Messages.COUNTRY, country);
      prop.put(Messages.VARIANT, variant);
    }

    if (reporterEnabled) {
      prop.put(REPORTER_CLASS, reporterClass);
      prop.put(REPORTER_PARAMS, reporterParams);
    }
    prop.put(SYSTEM_SOUNDS, systemSounds ? t : f);
    prop.put(AUDIO_ENABLED, soundEnabled ? t : f);
    prop.put(LFUtil.LOOK_AND_FEEL, lookAndFeel);
    prop.put(BrowserLauncher.BROWSER, preferredBrowser);
    prop.put(MEDIA_SYSTEM, mediaSystem);
    prop.put(SKIN, skin);
    prop.putAll(misc);
    return prop;
  }

  public boolean edit(Component parent) {
    boolean result = false;
    if (promptPassword(parent, null)) {
      PlayerSettingsDlg dlg = new PlayerSettingsDlg(this, parent);
      dlg.setVisible(true);
      result = dlg.result;
      if (result) {
        BrowserLauncher.setPreferredBrowser(preferredBrowser);
      }
    }
    return result;
  }

  public boolean promptPassword(Component parent, String[] msgKeys) {
    boolean result = (passwordConfirmed || password == null || password.length() == 0);
    while (!result) {
      if (msgKeys == null) {
        msgKeys = new String[] { "settings_passwordRequired" };
      }
      String r = getMessages().showInputDlg(parent, msgKeys, null, null, null, true);
      if (r == null) {
        break;
      }
      result = password.equals(r);
      if (!result) {
        getMessages().showAlert(parent, "PASSWORD_INCORRECT");
      } else {
        passwordConfirmed = true;
      }
    }
    return result;
  }

  public void addRecentFile(String fName) {
    if (fName != null) {
      String[] recentBak = recentFiles;
      recentFiles = new String[MAX_RECENT];
      recentFiles[0] = fName.trim();
      for (int i = 0, j = 1; i < MAX_RECENT && j < MAX_RECENT; i++) {
        if (recentBak[i] != null && !recentBak[i].equals(fName)) {
          recentFiles[j++] = recentBak[i];
        }
      }
    }
  }
}
