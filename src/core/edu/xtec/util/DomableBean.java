/*
 * File    : DomableBean.java
 * Created : 25-mar-2003 11:38
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class has a collection of XML {@link org.jdom.Element} objects, among with a set of
 * properties represented by key-value pairs, and a string identifer.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.14
 */
public class DomableBean extends Object implements Domable, Serializable {

  public static final String ELEMENT_NAME = "bean",
      ID = "id",
      PARAM = "param",
      NAME = "name",
      VALUE = "value";

  private String id;
  private Map<String, String> params;
  private List<org.jdom.Element> elements;

  /** Creates a new instance of DomableBean */
  public DomableBean() {}

  public DomableBean(String id) {
    setId(id);
  }

  private List<org.jdom.Element> chkElements() {
    if (elements == null) elements = new ArrayList<org.jdom.Element>();
    return elements;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    if (id != null) e.setAttribute(ID, id);
    if (params != null) {
      Iterator<String> it = params.keySet().iterator();
      while (it.hasNext()) {
        String key = it.next();
        String value = params.get(key);
        if (value != null) {
          org.jdom.Element child = new org.jdom.Element(PARAM);
          child.setAttribute(NAME, key);
          child.setAttribute(VALUE, value);
          e.addContent(child);
        }
      }
    }
    if (elements != null) {
      Iterator it = elements.iterator();
      while (it.hasNext()) {
        e.addContent(((org.jdom.Element) it.next()).detach());
      }
    }
    return e;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    setId(e.getAttributeValue(ID));
    List children = e.getChildren();
    Iterator it = children.iterator();
    while (it.hasNext()) {
      org.jdom.Element child = (org.jdom.Element) it.next();
      if (PARAM.equals(child.getName())) {
        getParams()
            .put(
                JDomUtility.getStringAttr(child, NAME, "DEFAULT", false),
                JDomUtility.getStringAttr(child, VALUE, "", false));
      } else {
        chkElements().add(child);
      }
    }
  }

  public static DomableBean getDomableBean(org.jdom.Element e) throws Exception {
    DomableBean db = new DomableBean(null);
    db.setProperties(e, null);
    return db;
  }

  public void setParam(String key, String value) {
    if (key != null) getParams().put(key, value);
  }

  public String getParam(String key) {
    return key == null ? null : (String) getParams().get(key);
  }

  public Map<String, String> getParams() {
    if (params == null) params = new HashMap<String, String>();
    return params;
  }

  public void addElement(org.jdom.Element e) {
    chkElements().add(e);
  }

  public org.jdom.Element[] getElements() {
    org.jdom.Element[] result;
    if (elements != null) result = elements.toArray(new org.jdom.Element[elements.size()]);
    else result = new org.jdom.Element[0];
    return result;
  }
}
