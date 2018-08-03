/*
 * File    : AutoContentProvider.java
 * Created : 06-may-2001 18:12
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

package edu.xtec.jclic.automation;

import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ResourceBridge;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This abstract class is the base for all the classes that provide contents to
 * JClic activities, usually based on random values. Activities linked to a
 * <CODE>AutoContentProvider</CODE> object rely on it to build its contents on
 * every start.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public abstract class AutoContentProvider extends Object implements Domable, Editable {

  public static final String ELEMENT_NAME = "automation";
  public static final String BASE_CLASS = "edu.xtec.jclic.automation.", BASE_CLASS_TAG = "@";

  public AutoContentProvider() {
  }

  public abstract void setProperties(org.jdom.Element e, Object aux) throws Exception;

  public boolean setClic3Properties(byte[] data) {
    return false;
  }

  public static AutoContentProvider getAutoContentProvider(org.jdom.Element e) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    String className = JDomUtility.getClassName(e);

    AutoContentProvider acp = getAutoContentProvider(className);
    if (acp != null)
      acp.setProperties(e, null);
    return acp;
  }

  public static AutoContentProvider getAutoContentProvider(String className) throws Exception {
    AutoContentProvider acp = null;
    Class providerClass = getAutoContentProviderClass(className);
    return (AutoContentProvider) providerClass.newInstance();
  }

  public static Class getAutoContentProviderClass(String className) throws Exception {
    if (className.startsWith(BASE_CLASS_TAG))
      className = BASE_CLASS + className.substring(1);
    Class providerClass = Class.forName(className);
    return providerClass;
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(JDomUtility.CLASS, getShortClassName());
    return e;
  }

  public String getShortClassName() {
    String result = getClass().getName();
    if (result.startsWith(BASE_CLASS))
      result = BASE_CLASS_TAG + result.substring(BASE_CLASS.length());
    return result;
  }

  // Added new method with parameter "FileSystem", in order to allow access to
  // external files
  public void init(ResourceBridge rb, FileSystem fs) {
    init(rb);
  }

  public void init(ResourceBridge rb) {
  }

  public abstract boolean generateContent(Object kit, ResourceBridge rb);

  public Editor getEditor(Editor parent) {
    Editor result = null;
    String s = getClass().getName() + "Editor";
    try {
      Class.forName(s);
    } catch (ClassNotFoundException ex) {
      s = "edu.xtec.jclic.automation.AutoContentProviderEditor";
    }
    return Editor.createEditor(s, this, parent);
  }

  private static final Class[] ARRAY_OF_CLASS = new Class[] { Class.class };

  public static boolean checkClient(String contentProviderClassName, Class clientClass) {
    boolean result = false;
    try {
      Class<?> contentProviderClass = getAutoContentProviderClass(contentProviderClassName);
      Method method = contentProviderClass.getMethod("checkClient", ARRAY_OF_CLASS);
      if (method != null) {
        Object o = method.invoke((Object) null, (Object[]) new Class[] { clientClass });
        if (o instanceof Boolean) {
          result = ((Boolean) o).booleanValue();
        }
      }
    } catch (Exception ex) {
      System.err.println("Error checking class:\n" + ex);
    }
    return result;
  }

  public static void listReferences(org.jdom.Element e, Map<String, String> map) throws Exception {
    AutoContentProvider acp = getAutoContentProvider(e);
    if (acp != null)
      acp.innerListReferences(map);
  }

  public void innerListReferences(Map<String, String> map) throws Exception {
    // default implementation does nothing
  }
}
