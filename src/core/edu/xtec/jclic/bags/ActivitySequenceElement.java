/*
 * File    : ActivitySequenceElement.java
 * Created : 19-dec-2000 16:18
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
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.util.Iterator;
import java.util.Map;

/**
 * This class defines a specific point into a JClic sequence of activities: what
 * activity must run at this point, what to do or where to jump when the
 * activity finishes, the behavior of the "next" and "prev" buttons, etc. It can
 * also have a "tag", used to refer to this point of the sequence with a unique
 * name. <CODE>ActivitySequenceElements</CODE> are always stored into
 * {@link edu.xtec.jclic.bags.ActivitySequence} objects.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ActivitySequenceElement extends Object implements Editable, Domable, Cloneable {

  private String tag;
  private String description;
  private String activityName;
  public ActivitySequenceJump fwdJump;
  public ActivitySequenceJump backJump;
  public int navButtons;
  public int delay;

  /** Creates new ActivitySequenceElement */
  public ActivitySequenceElement(String activityName) {
    this(activityName, false);
  }

  public ActivitySequenceElement(String activityName, boolean singleActivity) {
    this(activityName, 0, NAV_BOTH);
    if (singleActivity) {
      setTag(getActivityName());
      fwdJump = new ActivitySequenceJump(JumpInfo.STOP);
      backJump = new ActivitySequenceJump(JumpInfo.RETURN);
    }
  }

  public ActivitySequenceElement(String activityName, int delay, int navButtons) {
    setActivityName(activityName);
    this.delay = delay;
    tag = null;
    description = null;
    fwdJump = null;
    backJump = null;
    this.navButtons = navButtons;
  }

  public static final String ELEMENT_NAME = "item", NAME = "name", ID = "id", DESCRIPTION = "description",
      DELAY = "delay", FORWARD = "forward", BACK = "back", NAV_BUTTONS = "navButtons";
  public static final int NAV_NONE = 0, NAV_FWD = 1, NAV_BACK = 2, NAV_BOTH = 3;
  public static final String[] NAV_BUTTONS_TAG = { "none", "fwd", "back", "both" };

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    if (tag != null) {
      e.setAttribute(ID, tag);
      if (description != null)
        JDomUtility.addParagraphs(e, DESCRIPTION, description);
    }
    e.setAttribute(NAME, activityName);
    if (delay != 0)
      e.setAttribute(DELAY, Integer.toString(delay));
    if (fwdJump != null)
      e.addContent(fwdJump.getJDomElement(FORWARD));
    if (backJump != null)
      e.addContent(backJump.getJDomElement(BACK));
    if (navButtons != NAV_BOTH)
      e.setAttribute(NAV_BUTTONS, NAV_BUTTONS_TAG[navButtons]);
    return e;
  }

  public static ActivitySequenceElement getActivitySequenceElement(org.jdom.Element e) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    ActivitySequenceElement ase = new ActivitySequenceElement("", 0, NAV_BOTH);
    ase.setProperties(e, null);
    return ase;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    org.jdom.Element child;
    setActivityName(e.getAttributeValue(NAME));
    delay = JDomUtility.getIntAttr(e, DELAY, 0);
    setTag(JDomUtility.getStringAttr(e, ID, tag, false));
    description = JDomUtility.getParagraphs(e.getChild(DESCRIPTION));
    Iterator it = e.getChildren(ActivitySequenceJump.ELEMENT_NAME).iterator();
    while (it.hasNext()) {
      child = (org.jdom.Element) it.next();
      String id = child.getAttributeValue(ActivitySequenceJump.ID);
      ActivitySequenceJump asj = ActivitySequenceJump.getActivitySequenceJump(child);
      if (FORWARD.equals(id))
        fwdJump = asj;
      else if (BACK.equals(id))
        backJump = asj;
    }
    navButtons = JDomUtility.getStrIndexAttr(e, NAV_BUTTONS, NAV_BUTTONS_TAG, NAV_BOTH);
  }

  public void setTag(String newTag) {
    tag = FileSystem.stdFn(newTag);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTag() {
    return tag;
  }

  public String getDescription() {
    return description;
  }

  public void setActivityName(String sActivityName) {
    activityName = FileSystem.stdFn(sActivityName);
  }

  public String getActivityName() {
    return activityName;
  }

  public void listReferences(String type, Map<String, String> map) {
    if (activityName != null && (type == null || type.equals(Constants.ACTIVITY_OBJECT)))
      map.put(activityName, Constants.ACTIVITY_OBJECT);
    if (tag != null && (type == null || type.equals(Constants.SEQUENCE_OBJECT)))
      map.put(tag, Constants.SEQUENCE_OBJECT);
    if (backJump != null)
      backJump.listReferences(type, map);
    if (fwdJump != null)
      fwdJump.listReferences(type, map);
  }

  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    ActivitySequenceElement result = (ActivitySequenceElement) super.clone();
    if (fwdJump != null)
      result.fwdJump = (ActivitySequenceJump) fwdJump.clone();
    if (backJump != null)
      result.backJump = (ActivitySequenceJump) backJump.clone();
    return result;
  }
}
