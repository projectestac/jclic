/*
 * File    : UserData.java
 * Created : 17-feb-2003 10:17
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
public class UserData extends CompoundObject implements Domable {

  public String groupId;
  public String pwd;

  /** Creates a new instance of UserData */
  public UserData(String id, String name, String iconUrl, String pwd, String groupId) {
    super(id, name, iconUrl);
    this.groupId = groupId;
    this.pwd = pwd;
  }

  public UserData(org.jdom.Element e) throws Exception {
    setProperties(e, null);
  }

  public static final String ELEMENT_NAME = "user";
  public static final String ID = "id", NAME = "name", ICON = "icon", PWD = "pwd", GROUP = "group";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    JDomUtility.setStringAttr(e, ID, id, false);
    JDomUtility.setStringAttr(e, NAME, text, false);
    JDomUtility.setStringAttr(e, ICON, iconUrl, false);
    JDomUtility.setStringAttr(e, PWD, pwd, false);
    JDomUtility.setStringAttr(e, GROUP, groupId, false);
    return e;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    id = JDomUtility.getStringAttr(e, ID, null, false);
    text = JDomUtility.getStringAttr(e, NAME, null, false);
    iconUrl = JDomUtility.getStringAttr(e, ICON, null, false);
    pwd = JDomUtility.getStringAttr(e, PWD, null, false);
    groupId = JDomUtility.getStringAttr(e, GROUP, null, false);
  }
}
