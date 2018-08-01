/*
 * File    : ResourceManager.java
 * Created : 07-mar-2002 17:11
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public abstract class ResourceManager {

  public static final String RESOURCE_ROOT = "/edu/xtec/resources/";
  public static final String RESOURCE_CLASS_ROOT = "edu.xtec.resources.";
  public static final StreamIO.InputStreamProvider STREAM_PROVIDER = new StreamIO.InputStreamProvider() {
    public java.io.InputStream getInputStream(String resourceName) throws Exception {
      return getResourceAsStream(resourceName);
    }
  };
  public static final String DEFAULT_LOCALE = "en";
  private static Map<String, ImageIcon> icons = new HashMap<String, ImageIcon>();

  public static ImageIcon getImageIcon(String name) {
    ImageIcon result = (ImageIcon) icons.get(name);
    if (result == null) {
      try {
        result = new ImageIcon(getResource(name));
        String s = name;
        if (s.startsWith("icons/"))
          s = new StringBuilder("@").append(s.substring(6)).substring(0);
        result.setDescription(s);
        icons.put(name, result);
      } catch (Exception ex) {
        System.err.println("unable to get image " + name);
        System.err.println(ex);
      }
    }
    return result;
  }

  public static java.net.URL getResource(String name) throws Exception {
    java.net.URL result = ResourceManager.class.getResource(RESOURCE_ROOT + name);
    if (result == null)
      throw buildException(name);
    return result;
  }

  public static java.io.InputStream getResourceAsStream(String name) throws Exception {
    java.io.InputStream result = ResourceManager.class.getResourceAsStream(RESOURCE_ROOT + name);
    if (result == null)
      throw buildException(name);
    return result;
  }

  public static byte[] getResourceBytes(String name) throws Exception {
    return StreamIO.readInputStream(getResourceAsStream(name));
  }

  public static ExtendedByteArrayInputStream getResourceAsByteArray(String name) throws Exception {
    return new ExtendedByteArrayInputStream(getResourceBytes(name), name);
  }

  public static String getResourceText(String name, boolean useCRLF) throws Exception {

    String lineEnding = useCRLF ? "\r\n" : System.getProperty("line.separator");
    BufferedReader in = new BufferedReader(new InputStreamReader(getResourceAsStream(name)));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = in.readLine()) != null) {
      sb.append(line).append(lineEnding);
    }
    in.close();
    return sb.substring(0);
  }

  public static java.util.ResourceBundle getBundle(String name, java.util.Locale locale) throws Exception {
    if (locale != null && DEFAULT_LOCALE.equals(locale.getLanguage()))
      Locale.setDefault(locale);
    java.util.ResourceBundle result = java.util.ResourceBundle.getBundle(RESOURCE_CLASS_ROOT + name, locale);
    if (result == null)
      throw buildException(name);
    return result;
  }

  private static Exception buildException(String name) {
    return new Exception("Unable to load resource: " + name);
  }
}
