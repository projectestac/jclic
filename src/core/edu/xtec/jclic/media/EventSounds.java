/*
 * File    : EventSounds.java
 * Created : 19-sep-2001 16:13
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

package edu.xtec.jclic.media;

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Options;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class EventSounds extends Object implements Domable, Cloneable {

  public static boolean globalEnabled = true;

  public static final String ELEMENT_NAME = "eventSounds";
  public static final String ENABLED = "enabled";
  public static final int START = 0, CLICK = 1, ACTION_ERROR = 2, ACTION_OK = 3, FINISHED_ERROR = 4, FINISHED_OK = 5,
      NUM_EVENTS = 6;
  public static final String[] EVENT_NAMES = { "start", "click", "actionError", "actionOk", "finishedError",
      "finishedOk" };
  protected EventSounds parent;
  protected EventSoundsElement[] elements;
  protected int enabled;

  /** Creates new EventSounds */
  public EventSounds(EventSounds setParent) {
    elements = new EventSoundsElement[NUM_EVENTS];
    enabled = JDomUtility.DEFAULT;
    parent = setParent;
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    org.jdom.Element child;
    boolean empty = true;

    if (enabled != JDomUtility.DEFAULT) {
      e.setAttribute(ENABLED, JDomUtility.triStateString(enabled));
      empty = false;
    }
    for (EventSoundsElement element : elements) {
      if (element != null) {
        child = element.getJDomElement();
        if (child != null) {
          e.addContent(child);
          empty = false;
        }
      }
    }
    return empty ? null : e;
  }

  public static EventSounds getEventSounds(org.jdom.Element e) throws Exception {

    EventSounds ev = new EventSounds(null);
    ev.setProperties(e, null);
    return ev;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    org.jdom.Element child;
    enabled = JDomUtility.getTriStateAttr(e, ENABLED, enabled);
    Iterator it = e.getChildren().iterator();
    while (it.hasNext()) {
      EventSoundsElement ese = EventSoundsElement.getEventSoundsElement((org.jdom.Element) it.next());
      if (ese != null) {
        int index = JDomUtility.getStrIndexAttr(ese.getId(), EVENT_NAMES, -1);
        if (index >= 0 && index < NUM_EVENTS)
          elements[index] = ese;
      }
    }
  }

  public static void listReferences(org.jdom.Element e, Map<String, String> map) {
    org.jdom.Element child;
    // Correction of bug #83 (Custom event sounds ignored in media dependency check)
    Iterator it = e.getChildren(EventSoundsElement.ELEMENT_NAME).iterator();
    while (it.hasNext()) {
      if ((child = (org.jdom.Element) it.next()) != null) {
        String s = child.getAttributeValue(EventSoundsElement.FILE);
        if (s != null)
          map.put(s, Constants.MEDIA_OBJECT);
      }
    }
  }

  public HashMap getReferences() {
    HashMap<String, String> result = new HashMap<String, String>();
    org.jdom.Element e = getJDomElement();
    if (e != null)
      listReferences(e, result);
    return result;
  }

  public void setParent(EventSounds p) {
    parent = p;
  }

  public boolean realize(Options options, MediaBag mediaBag) {
    Exception ext = null;
    try {
      for (EventSoundsElement element : elements) {
        if (element != null)
          element.realize(options, mediaBag);
      }
    } catch (Exception ex) {
      System.err.println("Error realizing event sound:\n" + ex);
      ex.printStackTrace(System.out);
      ext = ex;
    }
    return (ext == null);
  }

  public void setDataSource(int event, Object source, Options options) throws Exception {
    if (event < 0 || event >= NUM_EVENTS)
      return;
    if (elements[event] == null) {
      elements[event] = new EventSoundsElement(EVENT_NAMES[event]);
    }
    elements[event].setDataSource(source, options);
  }

  public EventSoundsElement getElement(int event) {
    if (event < 0 || event >= NUM_EVENTS)
      return null;
    return elements[event];
  }

  public EventSoundsElement createElement(int event) {
    if (event < 0 || event >= NUM_EVENTS)
      return null;
    if (elements[event] == null)
      elements[event] = new EventSoundsElement(EVENT_NAMES[event]);
    return elements[event];
  }

  public int getEnabledChain(int event) {
    int st = enabled;
    if (st == JDomUtility.DEFAULT) {
      if (event >= 0 && event < NUM_EVENTS)
        if (elements[event] == null)
          st = (parent == null ? st : parent.getEnabledChain(event));
        else {
          st = elements[event].getEnabled();
          if (st == JDomUtility.DEFAULT && parent != null)
            st = parent.getEnabledChain(event);
        }
    }
    return st;
  }

  public EventSoundsElement getElementChain(int event) {
    EventSoundsElement ese;
    if (event < 0 || event >= NUM_EVENTS)
      return null;
    ese = elements[event];
    if (ese == null && parent != null)
      return parent.getElementChain(event);
    return ese;
  }

  public void playNow(int event) {
    if (!globalEnabled)
      return;
    EventSoundsElement ese = null;
    if (getEnabledChain(event) != JDomUtility.FALSE && (ese = getElementChain(event)) != null)
      ese.play();
  }

  public void play(int event) {
    if (!globalEnabled)
      return;
    EventSoundsElement ese = null;
    if (getEnabledChain(event) != JDomUtility.FALSE && (ese = getElementChain(event)) != null) {
      final EventSoundsElement evs = ese;
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          evs.play();
        }
      });
    }
  }

  public void close() {
    for (int i = 0; i < NUM_EVENTS; i++)
      if (elements[i] != null) {
        elements[i].close();
        elements[i] = null;
      }
    parent = null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  @Override
  public Object clone() {
    EventSounds ev = new EventSounds(parent);
    for (int i = 0; i < elements.length; i++) {
      if (elements[i] != null)
        ev.elements[i] = (EventSoundsElement) elements[i].clone();
    }
    ev.enabled = enabled;
    return ev;
  }

  /**
   * Getter for property enabled.
   *
   * @return Value of property enabled.
   */
  public int getEnabled() {
    return enabled;
  }

  /**
   * Setter for property enabled.
   *
   * @param enabled New value of property enabled.
   */
  public void setEnabled(int enabled) {
    this.enabled = enabled;
  }
}
