/*
 * File    : EventSoundsElement.java
 * Created : 26-sep-2001 12:40
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

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class EventSoundsElement extends Object implements Domable, Cloneable {

  protected String fileName;
  protected int enabled;
  protected AudioPlayer player;
  protected String id;

  /** Creates new EventSoundsElement */
  public EventSoundsElement(String id) {
    this.id = id;
    enabled = JDomUtility.DEFAULT;
    fileName = null;
    player = null;
  }

  public String getId() {
    return id;
  }

  public static final String ELEMENT_NAME = "sound", ID = "id", FILE = "file";

  public org.jdom.Element getJDomElement() {
    if (id == null || (enabled == JDomUtility.DEFAULT && fileName == null)) return null;
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(ID, id);
    if (enabled != JDomUtility.DEFAULT)
      e.setAttribute(EventSounds.ENABLED, JDomUtility.triStateString(enabled));
    if (fileName != null) e.setAttribute(FILE, fileName);
    return e;
  }

  public static EventSoundsElement getEventSoundsElement(org.jdom.Element e) throws Exception {
    String id = JDomUtility.getStringAttr(e, ID, e.getName(), false);
    if (id == null || id.length() < 1 || ELEMENT_NAME.equals(id))
      throw new Exception("Invalid event sound element id: " + id);
    EventSoundsElement ev = new EventSoundsElement(id);
    ev.setProperties(e, null);
    return ev;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    enabled = JDomUtility.getTriStateAttr(e, EventSounds.ENABLED, enabled);
    fileName = JDomUtility.getStringAttr(e, FILE, fileName, false);
  }

  public void setFileName(String fName) {
    if (player != null) player.close();
    fileName = fName;
  }

  public String getFileName() {
    return fileName;
  }

  public void setEnabled(int v) {
    if (JDomUtility.checkTriState(v)) {
      enabled = v;
      if (player != null) player.stop();
    }
  }

  public int getEnabled() {
    return enabled;
  }

  private void buildPlayer(Options options) {
    close();
    String s = options.getString(Constants.MEDIA_SYSTEM);
    try {
      Class c = Class.forName("edu.xtec.jclic.media.JavaSoundAudioPlayer");
      player = (AudioPlayer) c.newInstance();
    } catch (Exception ex) {
      System.err.println("Error building audio player:\n" + ex);
    }
  }

  public boolean setDataSource(Object source, Options options) throws Exception {
    buildPlayer(options);
    return player == null ? false : player.setDataSource(source);
  }

  public void realize(Options options, MediaBag mediaBag) throws Exception {
    if (player == null) buildPlayer(options);
    player.realize(fileName, mediaBag);
  }

  public void close() {
    if (player != null) {
      player.close();
      player = null;
    }
  }

  public void play() {
    if (player != null
        && enabled != edu.xtec.util.JDomUtility.FALSE
        && EventSounds.globalEnabled == true) player.play();
  }

  public void stop() {
    if (player != null) player.stop();
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  @Override
  public Object clone() {
    EventSoundsElement evs = new EventSoundsElement(id);
    evs.fileName = fileName;
    evs.enabled = enabled;
    evs.player = player;
    return evs;
  }
}
