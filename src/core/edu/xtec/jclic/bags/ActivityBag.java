/*
 * File    : ActivityBag.java
 * Created : 19-dec-2000 16:09
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

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.Constants;
import edu.xtec.jclic.bags.MediaBag.Listener;
import edu.xtec.jclic.clic3.Clic3;
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class stores the complete collection of activities of a {@link
 * edu.xtec.jclic.project.JClicProject}. The collection is managed through a private {@link
 * java.util.ArrayList} of objects of type {@link edu.xtec.jclic.bags.ActivityBagElement}.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ActivityBag extends Object implements Editable, Domable, Listener {

  /** The project that this ActivityBag belongs to */
  protected JClicProject project;

  private List<ActivityBagElement> elements;

  /**
   * Creates new ActivityBag
   *
   * @param project The project this ActivityBag belongs to
   */
  public ActivityBag(JClicProject project) {
    elements = new ArrayList<ActivityBagElement>(20);
    this.project = project;
  }

  /**
   * Returns the project this ActiivityBag belongs to
   *
   * @return The project related to this ActivityBag.
   */
  public JClicProject getProject() {
    return project;
  }

  public int size() {
    return elements.size();
  }

  public ActivityBagElement elementAt(int index) throws ArrayIndexOutOfBoundsException {
    return elements.get(index);
    // return (ActivityBagElement)elements.elementAt(index);
  }

  public void insertElementAt(ActivityBagElement el, int index)
      throws ArrayIndexOutOfBoundsException {
    elements.add(index, el);
    // elements.insertElementAt(el, index);
  }

  public void removeElementAt(int index) throws ArrayIndexOutOfBoundsException {
    elements.remove(index);
    // elements.removeElementAt(index);
  }

  public void addElement(ActivityBagElement el) {
    elements.add(el);
    // elements.addElement(el);
  }

  public boolean removeElement(ActivityBagElement el) {
    return elements.remove(el);
    // return elements.removeElement(el);
  }

  public int getElementIndex(String name) {
    String s = FileSystem.stdFn(name);
    int result = -1, c = 0;
    Iterator<ActivityBagElement> it = elements.iterator();
    while (it.hasNext()) {
      if (it.next().getName().equals(s)) {
        result = c;
        break;
      }
      c++;
    }
    return result;
  }

  public ActivityBagElement getElementByName(String name) {
    int i = getElementIndex(name);
    return (i >= 0 ? elementAt(i) : null);
  }

  public ActivityBagElement getElement(String name) throws Exception {
    ActivityBagElement abe = getElementByName(name);
    if (abe == null) {
      if (Clic3.isClic3Extension(Clic3.getExt(name))) {
        Clic3.addActivityToBag(project, name);
        abe = getElementByName(name);
      }
    }
    return abe;
  }

  public boolean activityExists(String name) {
    return getElementByName(name) != null;
  }

  /** The name of the XML elements of type ActivityBag */
  public static final String ELEMENT_NAME = "activities";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    for (int i = 0; i < size(); i++) {
      e.addContent(elementAt(i).getData().detach());
    }
    return e;
  }

  public void addActivity(Activity act) {
    if (act != null) addJDomElement(act.getJDomElement());
  }

  public void addJDomElement(org.jdom.Element e) {
    if (e != null) {
      ActivityBagElement abe = getElementByName(e.getAttributeValue(Activity.NAME));
      if (abe != null) abe.setData(e);
      else addElement(new ActivityBagElement(e));
    }
  }

  public ActivityBagElement[] getElements() {
    return elements.toArray(new ActivityBagElement[size()]);
    // ActivityBagElement[] result=new ActivityBagElement[size()];
    // elements.copyInto(result);
    // return result;
  }

  public void sortByName() {
    Collections.sort(
        elements,
        new Comparator<ActivityBagElement>() {
          public int compare(ActivityBagElement o1, ActivityBagElement o2) {
            return o1.getName().compareTo(o2.getName());
          }
        });
  }

  public void sortByClassAndName() {
    final StringBuilder sb1 = new StringBuilder(200);
    final StringBuilder sb2 = new StringBuilder(200);
    Collections.sort(
        elements,
        new Comparator<ActivityBagElement>() {
          public int compare(ActivityBagElement o1, ActivityBagElement o2) {
            org.jdom.Element e1 = o1.getData();
            org.jdom.Element e2 = o2.getData();
            sb1.setLength(0);
            sb1.append(e1.getAttributeValue(JDomUtility.CLASS));
            sb1.append(e1.getAttributeValue(Activity.NAME));
            sb2.setLength(0);
            sb2.append(e2.getAttributeValue(JDomUtility.CLASS));
            sb2.append(e2.getAttributeValue(Activity.NAME));
            return sb1.substring(0).compareTo(sb2.substring(0));
          }
        });
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);

    org.jdom.Element child;
    List lst = e.getChildren(Activity.ELEMENT_NAME);
    // elements.ensureCapacity(lst.size());
    Iterator it = lst.iterator();
    while (it.hasNext()) {
      child = (org.jdom.Element) it.next();

      // Needed in JDOM B-8:
      it.remove();

      // Added class cast for JDOM 1.0:
      addJDomElement((org.jdom.Element) child.detach());
    }
  }

  public void removeElementByName(String name) {
    ActivityBagElement abe = getElementByName(name);
    if (abe != null) removeElement(abe);
  }

  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

  public void listReferencesTo(String name, String type, Map<String, String> map) {
    for (int i = 0; i < size(); i++) {
      ActivityBagElement abe = elementAt(i);
      Map dp = abe.getReferences();
      if (dp != null && dp.containsKey(name) && (type == null || type.equals(dp.get(name))))
        map.put(abe.getName(), Constants.ACTIVITY_OBJECT);
    }
    EventSounds evs = project.settings.eventSounds;
    if (evs != null) {
      HashMap dp = evs.getReferences();
      if (dp != null && dp.containsKey(name) && (type == null || type.equals(dp.get(name))))
        map.put("[" + project.getBridge().getMsg("edit_project") + "]", Constants.PROJECT_OBJECT);
    }

    if (project.settings.coverFileName != null) {
      MediaBagElement mbe = project.mediaBag.getElementByFileName(project.settings.coverFileName);
      if (mbe != null && name.equals(mbe.getName())) map.put(name, Constants.PROJECT_OBJECT);
    }

    if (project.settings.thumbnailFileName != null) {
      MediaBagElement mbe =
          project.mediaBag.getElementByFileName(project.settings.thumbnailFileName);
      if (mbe != null && name.equals(mbe.getName())) map.put(name, Constants.PROJECT_OBJECT);
    }

    if (project.settings.icon16 != null) {
      MediaBagElement mbe = project.mediaBag.getElementByFileName(project.settings.icon16);
      if (mbe != null && name.equals(mbe.getName())) map.put(name, Constants.PROJECT_OBJECT);
    }

    if (project.settings.icon72 != null) {
      MediaBagElement mbe = project.mediaBag.getElementByFileName(project.settings.icon72);
      if (mbe != null && name.equals(mbe.getName())) map.put(name, Constants.PROJECT_OBJECT);
    }

    if (project.settings.icon192 != null) {
      MediaBagElement mbe = project.mediaBag.getElementByFileName(project.settings.icon192);
      if (mbe != null && name.equals(mbe.getName())) map.put(name, Constants.PROJECT_OBJECT);
    }
  }

  @Override
  public void listReferences(String type, Map<String, String> map) {
    for (int i = 0; i < size(); i++) {
      ActivityBagElement abe = elementAt(i);
      if (type == null || type.equals(Constants.ACTIVITY_OBJECT))
        map.put(abe.getName(), Constants.ACTIVITY_OBJECT);
      Map<String, String> dp = abe.getReferences();
      if (dp != null) {
        if (type == null) map.putAll(dp);
        else {
          Iterator<String> it = dp.keySet().iterator();
          while (it.hasNext()) {
            String key = it.next();
            if (type.equals(dp.get(key))) map.put(key, type);
          }
        }
      }
    }
  }
}
