/*
 * File    : EventSoundsButton.java
 * Created : 27-sep-2002 15:34
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2018 Francesc Busquets & Departament
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

package edu.xtec.jclic.beans;

import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.media.EventSoundsEditorPanel;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ResourceManager;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class EventSoundsButton extends NullableObject {

  public static final String PROP_EVSND_NAME = "eventSounds";

  MediaBagEditor mbe;
  EventSounds evs = new EventSounds(null);

  /** Creates a new instance of ImgPanel */
  public EventSoundsButton() {
    super();
    button.setIcon(ResourceManager.getImageIcon("icons/audio_off.gif"));
  }

  @Override
  protected String getObjectType() {
    return PROP_EVSND_NAME;
  }

  public void setMediaBagEditor(MediaBagEditor mbe) {
    this.mbe = mbe;
  }

  public EventSounds getEventSounds() {
    evs.setEnabled(getObject() == null ? JDomUtility.FALSE : JDomUtility.DEFAULT);
    return evs;
  }

  public void setEventSounds(EventSounds evs) {
    if (evs == null)
      evs = new EventSounds(null);
    this.evs = evs;
    setObject(evs.getEnabled() == JDomUtility.FALSE ? null : evs);
  }

  @Override
  public void setObject(Object value) {
    boolean preStatus = (object != null);
    super.setObject(value);
    boolean status = (object != null);
    if (preStatus != status) {
      button.setIcon(ResourceManager.getImageIcon(status ? "icons/audio_on.gif" : "icons/audio_off.gif"));
    }
    if (value instanceof EventSounds)
      evs = (EventSounds) value;
    evs.setEnabled(status ? JDomUtility.DEFAULT : JDomUtility.FALSE);
  }

  @Override
  protected Object createObject() {
    evs.setEnabled(JDomUtility.DEFAULT);
    return evs;
  }

  @Override
  protected Object editObject(Object o) {
    if (options == null || mbe == null)
      return null;
    return EventSoundsEditorPanel.getEventSounds(evs, options, this, mbe);
  }
}
