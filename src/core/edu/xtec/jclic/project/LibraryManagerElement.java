/*
 * File    : LibraryManagerElement.java
 * Created : 17-jun-2002 18:04
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

import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Options;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class LibraryManagerElement implements Domable {

  protected String name;
  protected String path;
  protected Options options;
  protected boolean exists;
  protected boolean editable;
  protected boolean isUrl;
  protected boolean systemLib;

  public static final String ELEMENT_NAME = "library";
  public static final String NAME = "name", PATH = "path";

  public LibraryManagerElement(Options options) {
    this.options = options;
    name = options.getMsg("UNNAMED");
    path = null;
    exists = false;
    editable = false;
    isUrl = false;
    systemLib = false;
  }

  public LibraryManagerElement(String name, String path, Options options) {
    this.name = name;
    this.path = path;
    this.options = options;
    checkAttributes();
  }

  public static LibraryManagerElement getLibraryManagerElement(org.jdom.Element e, Options options)
      throws Exception {
    LibraryManagerElement lme = new LibraryManagerElement(options);
    lme.setProperties(e, null);
    return lme;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    name = JDomUtility.getStringAttr(e, NAME, name, false);
    path = JDomUtility.getStringAttr(e, PATH, path, false);
    checkAttributes();
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(NAME, name);
    e.setAttribute(PATH, path);
    return e;
  }

  public javax.swing.Icon getIcon() {
    String base = "icons/database";

    if (exists) {
      if (!editable) base = base + "_locked";
    } else base = base + "_unavailable";

    return edu.xtec.util.ResourceManager.getImageIcon(base + ".gif");
  }

  protected void checkAttributes() {
    exists = false;
    editable = false;
    isUrl = false;
    if (path != null) {
      if (path.startsWith("http:") || path.startsWith("https:")) {
        isUrl = true;
        try {
          java.net.URL url = new java.net.URL(path.replace(" ", "%20"));
          java.net.URLConnection con = url.openConnection();
          exists = (con != null);
        } catch (Exception ex) {
          //
        }
      } else {
        java.io.File file = new java.io.File(path);
        exists = file.exists() && !file.isDirectory() && file.canRead();
        if (exists) editable = file.canWrite();
      }
    }
  }

  public void setSystemLib(boolean setting) {
    systemLib = setting;
  }

  public boolean isSystemLib() {
    return systemLib;
  }

  @Override
  public String toString() {
    return name;
  }
}
