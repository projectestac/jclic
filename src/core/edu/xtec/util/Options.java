/*
 * File    : Options.java
 * Created : 02-jul-2002 00:20
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

import java.applet.Applet;
import java.awt.Component;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class Options extends HashMap<String, Object> {

  public static final String TRUE = "true", FALSE = "false";
  public static final Boolean BTRUE = true, BFALSE = false;
  public static final String MAC = "Mac",
      WIN = "Windows",
      JAVA131 = "java131",
      JAVA14 = "java14",
      JAVA141 = "java141",
      ARCH64BIT = "arch64bit";
  public static final String MAIN_PARENT_COMPONENT = "mainParentComponent", APPLET = "applet";
  public static final String LANGUAGE_BY_PARAM = "languageByParam";

  protected static final String[] TRANSIENT_KEYS = {
    MAIN_PARENT_COMPONENT,
    APPLET,
    LANGUAGE_BY_PARAM,
    MAC,
    WIN,
    JAVA14,
    JAVA131,
    JAVA141,
    ARCH64BIT,
    Messages.MESSAGES
  };

  public Options() {
    init();
  }

  public Options(Map<String, Object> t) {
    super(t);
    init();
  }

  public Options(Component cmp) {
    init();
    if (cmp != null) setMainComponent(cmp);
  }

  protected void init() {
    String ver = System.getProperty("java.version");
    if (ver != null && ver.compareTo("1.3.1") >= 0) put(JAVA131, true);

    if (ver != null && ver.compareTo("1.4.0") >= 0) put(JAVA14, true);

    if (ver != null && ver.compareTo("1.4.1") >= 0) put(JAVA141, true);

    String s = System.getProperty("os.name").toLowerCase();
    // String s=System.getProperty("java.vendor");
    if (s != null) {
      if (s.indexOf("mac") >= 0) {
        put(MAC, true);
        put(LFUtil.LOOK_AND_FEEL, LFUtil.SYSTEM);
      } else if (s.toLowerCase().indexOf("win") >= 0) put(WIN, true);
    }
    s = System.getProperty("sun.arch.data.model");
    if ("64".equals(s)) put(ARCH64BIT, true);
  }

  public Properties toProperties() {
    Properties prop = new Properties();
    Iterator it = keySet().iterator();
    while (it.hasNext()) {
      Object k = it.next();
      if (k != null && k instanceof String) {
        int i;
        for (i = 0; i < TRANSIENT_KEYS.length; i++) if (k.equals(TRANSIENT_KEYS[i])) break;
        if (i == TRANSIENT_KEYS.length) {
          Object v = get((String) k);
          if (v != null) prop.setProperty((String) k, v.toString());
        }
      }
    }
    return prop;
  }

  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    boolean result = defaultValue;
    Object r = get(key);
    if (r != null) {
      if (r instanceof Boolean) result = ((Boolean) r).booleanValue();
      else if (r instanceof String) result = ((String) r).equalsIgnoreCase(TRUE);
      else if (r instanceof Integer) result = ((Integer) r).intValue() != 0;
    }
    return result;
  }

  public void putBoolean(String key, boolean value) {
    put(key, value ? BTRUE : BFALSE);
  }

  public String getString(String key) {
    return (String) get(key);
  }

  public String getString(String key, String defaultValue) {
    return StrUtils.secureString(get(key), defaultValue);
  }

  public int getInt(String key, int defaultValue) {
    int result = defaultValue;
    Object r = get(key);
    if (r != null) {
      if (r instanceof Integer) result = ((Integer) r).intValue();
      else if (r instanceof String) {
        try {
          result = Integer.parseInt((String) r);
        } catch (NumberFormatException ex) {
          result = defaultValue;
        }
      }
    }
    return result;
  }

  public Messages getMessages() {
    return getMessages(null);
  }

  public Messages getMessages(String bundle) {
    return Messages.getMessages(this, bundle);
  }

  public String getMsg(String key) {
    return getMessages(null).get(key);
  }

  public Component getMainComponent() {
    return (Component) get(MAIN_PARENT_COMPONENT);
  }

  public Applet getApplet() {
    return (Applet) get(APPLET);
  }

  public void setMainComponent(Component cmp) {
    put(MAIN_PARENT_COMPONENT, cmp);
    if (cmp instanceof Applet) put(APPLET, cmp);
  }

  public void setLookAndFeel() {
    String s = getString(LFUtil.LOOK_AND_FEEL);
    if (s != null) LFUtil.setLookAndFeel(s, getMainComponent());
  }

  public void syncProperties(Map src, boolean preserveExistingValues) {
    Iterator it = src.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      if (!preserveExistingValues || !containsKey(entry.getKey().toString()))
        put(entry.getKey().toString(), entry.getValue());
    }
  }

  public static Map<String, Object> strToMap(
      String values, Map<String, Object> map, String delim, char equals, boolean nullsAllowed) {
    if (values != null && values.length() >= 0) {
      java.util.StringTokenizer st = new java.util.StringTokenizer(values, delim);
      while (st.hasMoreTokens()) {
        String e = st.nextToken();
        int i = e.indexOf(equals);
        if (i > 0) {
          String key = e.substring(0, i);
          String value = e.substring(i + 1);
          if (key != null && key.length() > 0 && (nullsAllowed || values.length() > 0))
            map.put(key, value);
        }
      }
    }
    return map;
  }

  public static String getString(Map map, String key, String defaultValue) {
    String result = (String) map.get(key);
    return result == null ? defaultValue : result;
  }

  public static java.awt.Window getWindowForComponent(Component parentComponent) {
    if (parentComponent == null) return null;
    if (parentComponent instanceof java.awt.Window) return (java.awt.Window) parentComponent;
    return getWindowForComponent(parentComponent.getParent());
  }

  public static Map<String, String> toStringMap(Properties prop) {
    Map<String, String> map = new HashMap<String, String>();
    Enumeration keys = prop.propertyNames();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      String value = prop.getProperty(key);
      map.put(key, value);
    }
    return map;
  }
}
