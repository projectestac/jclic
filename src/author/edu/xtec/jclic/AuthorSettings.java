/*
 * File    : AuthorSettings.java
 * Created : 18-sep-2002 09:10
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

import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.BrowserLauncher;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.LFUtil;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceBridge;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class AuthorSettings implements edu.xtec.jclic.Constants {

  public ResourceBridge rb;
  public String language, country, variant;
  public String lookAndFeel;
  public String preferredBrowser;
  public String rootPath;
  public String rootExportPath;
  public String rootScormPath;
  public String mediaSystem;
  public FileSystem fileSystem;
  public String appletCodeBase;
  public String appletWidth, appletHeight;
  public String jsCodeBase;
  public Color appletBgColor;
  public int imgMaxWidth;
  public int imgMaxHeight;
  public Map<String, Object> misc;

  public static final int MAX_RECENT = 8;
  public String[] recentFiles;

  public String cfgFile;
  public boolean readOnly;

  protected static String defaultCfgFile = null;

  public static final String PROJECTS_PATH = "projects", EXPORT_PATH = "export", SCORM_PATH = "scorm",
      CFG_FILE = "jclic_author.cfg";

  public static final String DEFAULT_APPLET_CODEBASE = "http://clic.xtec.cat/dist/jclic";
  public static final String DEFAULT_APPLET_WIDTH = "700";
  public static final String DEFAULT_APPLET_HEIGHT = "450";
  public static final Color DEFAULT_APPLET_BGCOLOR = Color.white;

  /** Revert codebase to clic.xtec.cat */
  public static final String DEFAULT_JS_CODEBASE = "https://clic.xtec.cat/dist/jclic.js/jclic.min.js";

  /** Creates new AuthorSettings */
  public AuthorSettings(ResourceBridge rb, String fromPath) {
    this.rb = rb;
    misc = new HashMap<String, Object>();
    lookAndFeel = edu.xtec.util.LFUtil.SYSTEM;
    preferredBrowser = "";
    recentFiles = new String[MAX_RECENT];
    mediaSystem = DEFAULT;
    readOnly = false;
    rootExportPath = rootScormPath = rootPath = System.getProperty("user.home");
    cfgFile = (fromPath == null ? getDefaultCfgFile(rb.getOptions()) : fromPath);
    fileSystem = new FileSystem(rb);
    if (FileSystem.isStrUrl(cfgFile)) {
      readOnly = true;
    } else {
      File f = new File(cfgFile);
      readOnly = !f.canWrite();
    }
    appletCodeBase = DEFAULT_APPLET_CODEBASE;
    appletWidth = DEFAULT_APPLET_WIDTH;
    appletHeight = DEFAULT_APPLET_HEIGHT;
    appletBgColor = DEFAULT_APPLET_BGCOLOR;

    jsCodeBase = DEFAULT_JS_CODEBASE;

    imgMaxWidth = MediaBagEditor.DEFAULT_IMG_MAX_WIDTH;
    imgMaxHeight = MediaBagEditor.DEFAULT_IMG_MAX_HEIGHT;
  }

  public static final String ELEMENT_NAME = "JClicAuthorSettings", APPLET = "applet", CODEBASE = "codebase",
      JSCODEBASE = "JScodebase", BGCOLOR = "bgcolor", IMGMAXSIZE = "imgMaxSize", WIDTH = "width", HEIGHT = "height",
      SOUND = "sound", SYSTEM = "system", MISC = "misc", PATHS = "paths", PATH = "path", ROOT = "root",
      EXPORT = "export", SCORM = "scorm", RECENT_FILES = "recentFiles", FILE = "file";

  public static String getDefaultCfgFile(Options options) {
    if (defaultCfgFile == null)
      defaultCfgFile = edu.xtec.util.PersistentSettings.getFilePath(PROGRAM, CFG_FILE, options, true);
    return defaultCfgFile;
  }

  public static AuthorSettings loadAuthorSettings(ResourceBridge rb) {

    AuthorSettings result = null;

    String cfgFile = rb.getOptions().getString(ELEMENT_NAME);

    if (cfgFile != null) {
      try {
        result = new AuthorSettings(rb, cfgFile);
        result.loadSettings(result.fileSystem.getXMLDocument(cfgFile).getRootElement());
      } catch (Exception ex) {
        result = null;
        System.err.println("Unable to read settings from " + cfgFile + "\n" + ex);
      }
    }
    if (result == null) {
      cfgFile = getDefaultCfgFile(rb.getOptions());
      result = new AuthorSettings(rb, cfgFile);
      try {
        result.loadSettings(result.fileSystem.getXMLDocument(cfgFile).getRootElement());
      } catch (Exception ex) {
        result.initNewSettings();
      }
    }

    Options options = rb.getOptions();
    options.syncProperties(result.getProperties(), true);
    if (!options.getBoolean(Options.LANGUAGE_BY_PARAM) && result.language != null) {
      options.put(Messages.LANGUAGE, result.language);
      if (result.country != null)
        options.put(Messages.COUNTRY, result.country);
      if (result.variant != null)
        options.put(Messages.VARIANT, result.variant);
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

      f = new File(new File(cfgFile).getParentFile(), EXPORT_PATH);
      rootExportPath = f.getAbsolutePath();

      f = new File(new File(cfgFile).getParentFile(), SCORM_PATH);
      rootScormPath = f.getAbsolutePath();

      fileSystem = new FileSystem(rootPath, rb);
      imgMaxWidth = MediaBagEditor.getImgMaxWidth();
      imgMaxHeight = MediaBagEditor.getImgMaxHeight();
    } catch (Exception ex) {
      System.err.println("Unable to create new settings!\n" + ex);
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

    if (language != null) {
      child = new org.jdom.Element(Messages.LANGUAGE);
      child.setAttribute(ID, language);
      if (country != null)
        child.setAttribute(Messages.COUNTRY, country);
      if (variant != null)
        child.setAttribute(Messages.VARIANT, variant);
      e.addContent(child);
    }

    child = new org.jdom.Element(PATHS);

    child2 = new org.jdom.Element(PATH);
    child2.setAttribute(ID, ROOT);
    child2.setAttribute(PATH, rootPath);
    child.addContent(child2);

    child2 = new org.jdom.Element(PATH);
    child2.setAttribute(ID, EXPORT);
    child2.setAttribute(PATH, rootExportPath);
    child.addContent(child2);

    child2 = new org.jdom.Element(PATH);
    child2.setAttribute(ID, SCORM);
    child2.setAttribute(PATH, rootScormPath);
    child.addContent(child2);

    e.addContent(child);

    child = new org.jdom.Element(APPLET);
    child.setAttribute(CODEBASE, appletCodeBase);
    child.setAttribute(WIDTH, appletWidth);
    child.setAttribute(HEIGHT, appletHeight);
    child.setAttribute(BGCOLOR, JDomUtility.colorToString(appletBgColor));
    child.setAttribute(JSCODEBASE, jsCodeBase);
    e.addContent(child);

    child = new org.jdom.Element(SOUND);
    child.setAttribute(MEDIA_SYSTEM, mediaSystem);
    e.addContent(child);

    child = new org.jdom.Element(LFUtil.LOOK_AND_FEEL);
    child.setAttribute(ID, lookAndFeel);
    e.addContent(child);

    child = new org.jdom.Element(BrowserLauncher.BROWSER);
    child.setAttribute(ID, preferredBrowser);
    e.addContent(child);

    child = new org.jdom.Element(IMGMAXSIZE);
    child.setAttribute(WIDTH, Integer.toString(imgMaxWidth));
    child.setAttribute(HEIGHT, Integer.toString(imgMaxHeight));
    e.addContent(child);

    if (!misc.isEmpty()) {
      child = new org.jdom.Element(MISC);
      for (java.util.Map.Entry me : misc.entrySet()) {
        String k = (String) me.getKey();
        Object v = me.getValue();
        if (k != null && v != null && v instanceof String)
          child.setAttribute(k, (String) v);
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

  protected void loadSettings(org.jdom.Element e) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    org.jdom.Element child, child2;

    if ((child = e.getChild(PATHS)) != null) {
      String rp = null, rpx = null, rps = null;
      Iterator it = child.getChildren(PATH).iterator();
      while (it.hasNext()) {
        child2 = (org.jdom.Element) it.next();
        if (ROOT.equals(child2.getAttributeValue(ID))) {
          rp = JDomUtility.getStringAttr(child2, PATH, rootPath, false);
        } else if (EXPORT.equals(child2.getAttributeValue(ID))) {
          rpx = JDomUtility.getStringAttr(child2, PATH, rootExportPath, false);
        } else if (SCORM.equals(child2.getAttributeValue(ID))) {
          rps = JDomUtility.getStringAttr(child2, PATH, rootScormPath, false);
        }
      }
      if (rp != null) {
        rootPath = rp;
        if (!FileSystem.isStrUrl(rootPath))
          fileSystem = new FileSystem(rootPath, rb);
      }
      if (rpx != null) {
        rootExportPath = rpx;
      } else {
        if (!FileSystem.isStrUrl(rootPath)) {
          File f = new File(rootPath);
          if (f.getName().compareToIgnoreCase(PROJECTS_PATH) == 0 && f.getParent() != null) {
            rootExportPath = (new File(f.getParentFile(), EXPORT_PATH)).getPath();
          } else {
            rootExportPath = (new File(f, EXPORT_PATH)).getPath();
          }
        }
      }
      if (rps != null) {
        rootScormPath = rps;
      } else {
        if (!FileSystem.isStrUrl(rootPath)) {
          File f = new File(rootPath);
          if (f.getName().compareToIgnoreCase(PROJECTS_PATH) == 0 && f.getParent() != null) {
            rootScormPath = (new File(f.getParentFile(), SCORM_PATH)).getPath();
          } else {
            rootScormPath = (new File(f, SCORM_PATH)).getPath();
          }
        }
      }
    }

    if ((child = e.getChild(APPLET)) != null) {
      appletCodeBase = JDomUtility.getStringAttr(child, CODEBASE, DEFAULT_APPLET_CODEBASE, false);
      appletWidth = JDomUtility.getStringAttr(child, WIDTH, DEFAULT_APPLET_WIDTH, false);
      appletHeight = JDomUtility.getStringAttr(child, HEIGHT, DEFAULT_APPLET_HEIGHT, false);
      appletBgColor = JDomUtility.getColorAttr(child, BGCOLOR, DEFAULT_APPLET_BGCOLOR);
      jsCodeBase = JDomUtility.getStringAttr(child, JSCODEBASE, DEFAULT_JS_CODEBASE, false);
    }

    if ((child = e.getChild(Messages.LANGUAGE)) != null) {
      language = JDomUtility.getStringAttr(child, ID, language, false);
      country = JDomUtility.getStringAttr(child, Messages.COUNTRY, country, false);
      variant = JDomUtility.getStringAttr(child, Messages.VARIANT, variant, false);
    }

    if ((child = e.getChild(SOUND)) != null) {
      mediaSystem = JDomUtility.getStringAttr(child, MEDIA_SYSTEM, mediaSystem, false);
    }

    if ((child = e.getChild(LFUtil.LOOK_AND_FEEL)) != null) {
      lookAndFeel = JDomUtility.getStringAttr(child, ID, lookAndFeel, false);
    }

    if ((child = e.getChild(BrowserLauncher.BROWSER)) != null) {
      preferredBrowser = JDomUtility.getStringAttr(child, ID, preferredBrowser, false);
      BrowserLauncher.setPreferredBrowser(preferredBrowser);
    }

    if ((child = e.getChild(IMGMAXSIZE)) != null) {
      imgMaxWidth = JDomUtility.getIntAttr(child, WIDTH, MediaBagEditor.DEFAULT_IMG_MAX_WIDTH);
      imgMaxHeight = JDomUtility.getIntAttr(child, HEIGHT, MediaBagEditor.DEFAULT_IMG_MAX_HEIGHT);
      MediaBagEditor.setImgMaxWidth(imgMaxWidth);
      MediaBagEditor.setImgMaxHeight(imgMaxHeight);
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
        if (it.hasNext())
          recentFiles[i] = JDomUtility.getStringAttr(((org.jdom.Element) it.next()), PATH, null, false);
        else
          recentFiles[i] = null;
      }
    }
  }

  public Map<String, Object> getProperties() {
    Map<String, Object> prop = new HashMap<String, Object>();

    String t = TRUE;
    String f = FALSE;

    if (language != null) {
      prop.put(Messages.LANGUAGE, language);
      prop.put(Messages.COUNTRY, country);
      prop.put(Messages.VARIANT, variant);
    }

    prop.put(LFUtil.LOOK_AND_FEEL, lookAndFeel);
    prop.put(MEDIA_SYSTEM, mediaSystem);
    prop.putAll(misc);
    return prop;
  }

  public boolean edit(Component parent) {
    AuthorSettingsDlg dlg = new AuthorSettingsDlg(this, parent);
    dlg.setVisible(true);
    if (dlg.result) {
      BrowserLauncher.setPreferredBrowser(preferredBrowser);
      MediaBagEditor.setImgMaxWidth(imgMaxWidth);
      MediaBagEditor.setImgMaxHeight(imgMaxHeight);
    }
    return dlg.result;
  }

  public void addRecentFile(String fName) {
    String[] recentBak = recentFiles;
    recentFiles = new String[MAX_RECENT];
    recentFiles[0] = fName;
    for (int i = 0, j = 1; i < MAX_RECENT && j < MAX_RECENT; i++) {
      if (recentBak[i] != null && !recentBak[i].equals(fName)) {
        recentFiles[j++] = recentBak[i];
      }
    }
  }
}
