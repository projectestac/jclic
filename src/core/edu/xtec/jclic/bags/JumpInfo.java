/*
 * File    : JumpInfo.java
 * Created : 04-jan-2002 16:52
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

package edu.xtec.jclic.bags;

import edu.xtec.jclic.Constants;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.util.Map;

/**
 * This class contains information about what the sequence manager of JClic must do in specific
 * circumstances: when an activity finishes, when the user clicks on the "next" and "prev" buttons,
 * or when a special active content is activated. Different kinds of actions are possible: to go
 * back to a previous point in the sequence, to exit the program, to stop (do nothing), to jump to a
 * specific point in the sequence of activities or to jump to another JClic project.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class JumpInfo implements Domable, Cloneable {

  public static final int JUMP = 0, STOP = 1, RETURN = 2, EXIT = 3;
  public int action;
  public String projectPath;
  public String sequence;
  public int actNum;

  /** Creates new JumpInfo */
  public JumpInfo(int action) {
    this(action, null);
  }

  public JumpInfo(int action, String sequence) {
    this.action = action;
    this.sequence = sequence;
    projectPath = null;
    actNum = -1;
  }

  public JumpInfo(int action, int actNum) {
    this.action = action;
    this.actNum = actNum;
    projectPath = null;
    sequence = null;
  }

  public static final String[] actions = {"JUMP", "STOP", "RETURN", "EXIT"};
  public static final String ELEMENT_NAME = "jump",
      ACTION = "action",
      TAG = "tag",
      PROJECT = "project";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    if (action != JUMP) e.setAttribute(ACTION, actions[action]);
    else {
      if (sequence != null) e.setAttribute(TAG, sequence);
      if (projectPath != null) e.setAttribute(PROJECT, projectPath);
    }
    return e;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);

    action = JDomUtility.getStrIndexAttr(e, ACTION, actions, JUMP);
    if (action == JUMP) {
      sequence = JDomUtility.getStringAttr(e, TAG, sequence, false);
      projectPath = JDomUtility.getStringAttr(e, PROJECT, projectPath, false);
    }
  }

  public void listReferences(String type, Map<String, String> map) {
    if (action == JUMP) {
      if (projectPath != null) {
        if (type == null || type.equals(Constants.EXTERNAL_OBJECT))
          map.put(projectPath, Constants.EXTERNAL_OBJECT);
      } else if (sequence != null) {
        if (type == null || type.equals(Constants.SEQUENCE_OBJECT))
          map.put(sequence, Constants.SEQUENCE_OBJECT);
      }
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
