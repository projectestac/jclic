/*
 * File    : ActionReg.java
 * Created : 11-jul-2001 9:01
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

import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class ActionReg extends Object implements java.io.Serializable, Domable {

  String type;
  String source;
  String dest;
  long time;
  boolean isOk;

  /** Creates new ActionReg */
  public ActionReg(String setType, String setSource, String setDest, boolean ok) {
    type = setType;
    source = setSource;
    dest = setDest;
    isOk = ok;
    time = System.currentTimeMillis();
  }

  public ActionReg(org.jdom.Element e) throws Exception {
    setProperties(e, null);
  }

  public static final String ELEMENT_NAME = "action";
  public static final String TYPE = "type", SOURCE = "source", DEST = "dest", TIME = "time", OK = "ok";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    JDomUtility.setStringAttr(e, TYPE, type, false);
    JDomUtility.setStringAttr(e, SOURCE, source, false);
    JDomUtility.setStringAttr(e, DEST, dest, false);
    e.setAttribute(OK, JDomUtility.boolString(isOk));
    e.setAttribute(TIME, Long.toString(time));
    return e;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    type = JDomUtility.getStringAttr(e, TYPE, type, false);
    source = JDomUtility.getStringAttr(e, SOURCE, source, false);
    dest = JDomUtility.getStringAttr(e, DEST, dest, false);
    isOk = JDomUtility.getBoolAttr(e, OK, isOk);
    time = JDomUtility.getLongAttr(e, TIME, time);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(type).append("(OK:").append(isOk).append(")");
    if (source != null)
      result.append(" SOURCE:").append(source);
    if (dest != null)
      result.append(" DEST:").append(dest);
    return result.substring(0);
  }
}
