/*
 * File    : TCPReportBean.java
 * Created : 17-feb-2003 13:06
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
import edu.xtec.util.DomableBean;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class TCPReportBean extends DomableBean {

  public static final String GET_PROPERTY = "get property", GET_PROPERTIES = "get_properties",
      ADD_SESSION = "add session", ADD_ACTIVITY = "add activity", GET_GROUPS = "get groups", GET_USERS = "get users",
      GET_USER_DATA = "get user data", GET_GROUP_DATA = "get group data", NEW_GROUP = "new group",
      NEW_USER = "new user", MULTIPLE = "multiple", UNKNOWN_KEY = "unknown";

  public static final String ERROR = "error";

  public static final Class[] DOMABLES = { TCPReportBean.class, GroupData.class, UserData.class, ActivityReg.class };

  public static final String[] DOMABLE_NAMES = { TCPReportBean.ELEMENT_NAME, GroupData.ELEMENT_NAME,
      UserData.ELEMENT_NAME, ActivityReg.ELEMENT_NAME };

  public static final String SESSION = "session", USER = "user", KEY = "key", CONTEXT = "context", TIME = "time",
      PROJECT = "project", CODE = "code", NUM = "num", DEFAULT = "default", RESULT = "result", GROUP = "group",
      ACTIVITY = "activity";

  /** Creates a new instance of TCPReportBean */
  public TCPReportBean() {
    super(ERROR);
  }

  public TCPReportBean(String id) {
    super(id);
  }

  public TCPReportBean(String id, Domable[] data) {
    super(id);
    setData(data);
  }

  public TCPReportBean(org.jdom.Element e) throws Exception {
    super(ERROR);
    setProperties(e, null);
  }

  public Domable[] getData() throws Exception {
    org.jdom.Element[] elements = getElements();
    ArrayList<Domable> v = new ArrayList<Domable>(elements.length);
    for (org.jdom.Element element : elements) {
      String name = element.getName();
      for (int j = 0; j < DOMABLES.length; j++) {
        if (name.equals(DOMABLE_NAMES[j])) {
          Class<?> cl = DOMABLES[j];
          Constructor cons = cl.getConstructor(new Class[] { org.jdom.Element.class });
          v.add((Domable) cons.newInstance(new Object[] { element }));
          break;
        }
      }
    }
    return v.toArray(new Domable[v.size()]);
  }

  public void setData(Domable[] data) {
    if (data != null)
      for (Domable d : data)
        setData(d);
  }

  public void setData(Domable data) {
    if (data != null)
      addElement(data.getJDomElement());
  }

  public Domable getSingleData() throws Exception {
    Domable[] dm = getData();
    return (dm == null || dm.length < 1) ? null : dm[0];
  }
}
