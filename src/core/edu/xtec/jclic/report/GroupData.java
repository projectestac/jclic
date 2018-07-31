/*
 * File    : GroupData.java
 * Created : 17-feb-2003 9:46
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

package edu.xtec.jclic.report;

import edu.xtec.util.CompoundObject;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;

/** @author Francesc Busquets (fbusquets@xtec.cat) */
public class GroupData extends CompoundObject implements Domable {

  public String description;

  /** Creates a new instance of GroupData */
  public GroupData(String id, String name, String iconUrl, String description) {
    super(id, name, iconUrl);
    this.description = description;
  }

  public GroupData(org.jdom.Element e) throws Exception {
    setProperties(e, null);
  }

  public static final String ELEMENT_NAME = "group";
  public static final String ID = "id", NAME = "name", ICON = "icon", DESCRIPTION = "description";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    JDomUtility.setStringAttr(e, ID, id, false);
    JDomUtility.setStringAttr(e, NAME, text, false);
    JDomUtility.setStringAttr(e, ICON, iconUrl, false);
    JDomUtility.setStringAttr(e, DESCRIPTION, description, false);
    return e;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    id = JDomUtility.getStringAttr(e, ID, null, false);
    text = JDomUtility.getStringAttr(e, NAME, null, false);
    iconUrl = JDomUtility.getStringAttr(e, ICON, null, false);
    description = JDomUtility.getStringAttr(e, DESCRIPTION, null, false);
  }
}
