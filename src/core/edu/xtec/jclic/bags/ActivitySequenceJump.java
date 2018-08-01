/*
 * File    : ActivitySequenceJump.java
 * Created : 19-dec-2000 16:27
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

import edu.xtec.util.JDomUtility;
import java.util.Map;

/**
 * This is a special case of {@link edu.xtec.jclic.bags.JumpInfo}, used only in
 * {@link edu.xtec.jclic.bags.ActivitySequenceElement} objects. Sequence
 * elements contain two ActivitySequenceJump objects: one to be processed when
 * the user clicks on the "next" button (or when the activity finishes, if in
 * automatic mode), and the other one related to the "prev" button.
 * ActivitySequenceJump objects define a default jump or action, but can have up
 * to two {@link edu.xtec.jclic.bags.ConditionalJumpInfo} objects, used to
 * define alternative jumps when the obtained score or the time used to solve
 * the activities are below or over specific values.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ActivitySequenceJump extends JumpInfo implements Cloneable {

  public ConditionalJumpInfo upperJump;
  public ConditionalJumpInfo lowerJump;

  /** Creates new ActivitySequenceJump */
  public ActivitySequenceJump(int action) {
    this(action, null);
  }

  public ActivitySequenceJump(int action, String sequence) {
    super(action, sequence);
    upperJump = null;
    lowerJump = null;
  }

  public static final String ID = "id", NAME = "name", UPPER = "upper", LOWER = "lower";

  public org.jdom.Element getJDomElement(String id) {
    org.jdom.Element e, child;

    e = super.getJDomElement();
    e.setAttribute(ID, id);
    if (upperJump != null) {
      child = upperJump.getJDomElement();
      child.setAttribute(ID, UPPER);
      e.addContent(child);
    }
    if (lowerJump != null) {
      child = lowerJump.getJDomElement();
      child.setAttribute(ID, LOWER);
      e.addContent(child);
    }
    return e;
  }

  public static ActivitySequenceJump getActivitySequenceJump(org.jdom.Element e) throws Exception {
    ActivitySequenceJump asj = new ActivitySequenceJump(STOP);
    asj.setProperties(e, null);
    return asj;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);

    org.jdom.Element child;

    // check for old version data (Jclic beta 0.4 or lower)
    String s = e.getAttributeValue(NAME);
    if (s == null) {
      super.setProperties(e, aux);
      if ((child = JDomUtility.getChildWithId(e, ELEMENT_NAME, UPPER)) != null)
        upperJump = ConditionalJumpInfo.getConditionalJumpInfo(child);
      if ((child = JDomUtility.getChildWithId(e, ELEMENT_NAME, LOWER)) != null)
        lowerJump = ConditionalJumpInfo.getConditionalJumpInfo(child);
    } else {
      // OLD VERSION DATA
      if (!s.equals(actions[STOP])) {
        if (s.equals(actions[EXIT]))
          action = EXIT;
        else {
          action = JUMP;
          sequence = s;
        }
      }

      if ((child = e.getChild(UPPER)) != null) {
        int v = JDomUtility.getIntAttr(child, ConditionalJumpInfo.THRESHOLD, 0);
        int t = JDomUtility.getIntAttr(child, ConditionalJumpInfo.TIME, -1);
        String sq = child.getText();
        upperJump = new ConditionalJumpInfo(JUMP, sq, v, t);
      }
      if ((child = e.getChild(LOWER)) != null) {
        int v = JDomUtility.getIntAttr(child, ConditionalJumpInfo.THRESHOLD, 0);
        int t = JDomUtility.getIntAttr(child, ConditionalJumpInfo.TIME, -1);
        String sq = child.getText();
        lowerJump = new ConditionalJumpInfo(JUMP, sq, v, t);
      }
    }
  }

  public void setConditionalJump(ConditionalJumpInfo jump, boolean upper) {
    if (upper)
      upperJump = jump;
    else
      lowerJump = jump;
  }

  public JumpInfo resolveJump(int rating, int time) {
    if (rating < 0 || time < 0)
      return this;
    if (upperJump != null && rating > upperJump.threshold && (upperJump.time <= 0 || time < upperJump.time))
      return upperJump;
    if (lowerJump != null && (rating < lowerJump.threshold || (lowerJump.time > 0 && time > lowerJump.time)))
      return lowerJump;
    return this;
  }

  @Override
  public void listReferences(String type, Map<String, String> map) {
    super.listReferences(type, map);
    if (upperJump != null)
      upperJump.listReferences(type, map);
    if (lowerJump != null)
      lowerJump.listReferences(type, map);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    ActivitySequenceJump result = (ActivitySequenceJump) super.clone();
    if (upperJump != null)
      result.upperJump = (ConditionalJumpInfo) upperJump.clone();
    if (lowerJump != null)
      result.lowerJump = (ConditionalJumpInfo) lowerJump.clone();
    return result;
  }
}
