/*
 * File    : ProjectInstaller.java
 * Created : 05-aug-2002 15:12
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

package edu.xtec.jclic.project;

import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.14
 */
public class ProjectInstaller implements Domable {

  public static final String SEP = ";", ITEM_SEP = ",", EQUAL_SEP = ":", FILES = "files";
  public static final String ITEM_NAMES = "itemNames", ITEM_PROJECTS = "itemProjects", ITEM_ICONS = "itemIcons",
      ITEM_DESCRIPTIONS = "itemDescriptions";

  public static final String ELEMENT_NAME = "JClicInstall";
  public static final String TITLE = "title", AUTHORS = "authors", FROM = "from";
  public static final String FILE = "file", SRC = "src", FOLDER = "folder";
  public static final String SHORTCUT = "shortcut", ICON = "icon", PROJECT = "project", TEXT = "text",
      DESCRIPTION = "description";
  public static final String INSTALLER_EXTENSION = ".jclic.inst";

  public String fName;
  public String from;
  public String baseFolder;
  public String projectTitle;
  public String authors;
  public List<String> files;
  public List<IconItem> iconItems;

  /** Creates a new instance of ProjectInstaller */
  public ProjectInstaller() {
    files = new ArrayList<String>();
    iconItems = new ArrayList<IconItem>();
  }

  public org.jdom.Element getJDomElement() {
    return getJDomElement(true);
  }

  public org.jdom.Element getJDomElement(boolean includeFrom) {

    org.jdom.Element child;

    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    JDomUtility.setStringAttr(e, TITLE, projectTitle, false);
    JDomUtility.setStringAttr(e, AUTHORS, authors, false);
    JDomUtility.setStringAttr(e, FOLDER, baseFolder, false);
    if (includeFrom)
      JDomUtility.setStringAttr(e, FROM, from, false);
    if (files != null) {
      for (int i = 0; i < files.size(); i++) {
        child = new org.jdom.Element(FILE);
        child.setAttribute(SRC, getFile(i));
        e.addContent(child);
      }
    }
    if (iconItems != null) {
      for (IconItem ii : iconItems) {
        child = new org.jdom.Element(SHORTCUT);
        JDomUtility.setStringAttr(child, PROJECT, ii.project, false);
        JDomUtility.setStringAttr(child, TEXT, ii.text, false);
        JDomUtility.setStringAttr(child, DESCRIPTION, ii.description, false);
        JDomUtility.setStringAttr(child, ICON, ii.icon, false);
        e.addContent(child);
      }
    }
    return e;
  }

  public static ProjectInstaller getProjectInstaller(String fileName) throws Exception {
    InputStream is;
    boolean isUrl = FileSystem.isStrUrl(fileName);
    URL url = null;
    File f = null;
    if (isUrl) {
      url = new URL(fileName);
      is = url.openStream();
    } else {
      f = new File(fileName);
      is = new FileInputStream(f);
    }
    org.jdom.Document doc = JDomUtility.getSAXBuilder().build(is);
    edu.xtec.util.JDomUtility.clearNewLineElements(doc.getRootElement());
    is.close();
    ProjectInstaller result = getProjectInstaller(doc.getRootElement());

    String from = null;
    String fName = null;
    if (isUrl && url != null) {
      String s0 = url.toExternalForm();
      int k = s0.lastIndexOf('/');
      if (k < 0)
        throw new Exception("Unable to get install store path from " + s0);
      from = s0.substring(0, k);
      fName = s0.substring(k + 1);
    } else if (f != null) {
      from = f.getParent();
      fName = f.getName();
    }

    if (from != null)
      result.from = from;
    if (fName != null)
      result.fName = fName;

    return result;
  }

  public static ProjectInstaller getProjectInstaller(org.jdom.Element e) throws Exception {
    ProjectInstaller pi = new ProjectInstaller();
    pi.setProperties(e, null);
    return pi;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    org.jdom.Element child;

    JDomUtility.checkName(e, ELEMENT_NAME);
    projectTitle = JDomUtility.getStringAttr(e, TITLE, projectTitle, false);
    authors = JDomUtility.getStringAttr(e, AUTHORS, authors, false);
    baseFolder = JDomUtility.getStringAttr(e, FOLDER, baseFolder, false);
    from = JDomUtility.getStringAttr(e, FROM, from, false);

    Iterator it = e.getChildren(FILE).iterator();
    while (it.hasNext()) {
      child = (org.jdom.Element) it.next();
      addFile(JDomUtility.getStringAttr(child, SRC, null, false));
    }

    it = e.getChildren(SHORTCUT).iterator();
    while (it.hasNext()) {
      child = (org.jdom.Element) it.next();
      IconItem ii = createIconItem();
      ii.project = JDomUtility.getStringAttr(child, PROJECT, ii.project, false);
      ii.text = JDomUtility.getStringAttr(child, TEXT, ii.text, false);
      ii.description = JDomUtility.getStringAttr(child, DESCRIPTION, ii.description, false);
      ii.icon = JDomUtility.getStringAttr(child, ICON, ii.icon, false);
    }
  }

  public void addFile(String s) {
    if (s != null && !files.contains(s))
      files.add(s);
  }

  public String getFile(int i) {
    if (files != null && i < files.size())
      return (String) files.get(i);
    else
      return null;
  }

  public IconItem getIconItem(int i) {
    if (iconItems != null && i < iconItems.size())
      return (IconItem) iconItems.get(i);
    else
      return null;
  }

  public IconItem createIconItem() {
    IconItem result = new IconItem();
    iconItems.add(result);
    return result;
  }

  public class IconItem {
    protected String text;
    protected String description;
    protected String project;
    protected String icon;

    @Override
    public String toString() {
      return (project == null ? "--" : project);
    }

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {
      this.description = description;
    }

    /**
     * Getter for property icon.
     *
     * @return Value of property icon.
     */
    public String getIcon() {
      return icon;
    }

    /**
     * Setter for property icon.
     *
     * @param icon New value of property icon.
     */
    public void setIcon(String icon) {
      this.icon = icon;
    }

    /**
     * Getter for property project.
     *
     * @return Value of property project.
     */
    public String getProject() {
      return project;
    }

    /**
     * Setter for property project.
     *
     * @param project New value of property project.
     */
    public void setProject(String project) {
      this.project = project;
    }

    /**
     * Getter for property text.
     *
     * @return Value of property text.
     */
    public String getText() {
      return text;
    }

    /**
     * Setter for property text.
     *
     * @param text New value of property text.
     */
    public void setText(String text) {
      this.text = text;
    }
  }
}
