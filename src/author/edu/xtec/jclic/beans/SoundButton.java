/*
 * File    : SoundButton.java
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
import edu.xtec.jclic.bags.MediaBagSelector;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.ResourceManager;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class SoundButton extends NullableObject {

  public static final String PROP_SND_NAME = "soundName";

  MediaBagEditor mbe;

  /** Creates a new instance of ImgPanel */
  public SoundButton() {
    super();
    button.setIcon(ResourceManager.getImageIcon("icons/audio_off.gif"));
  }

  @Override
  protected String getObjectType() {
    return PROP_SND_NAME;
  }

  public void setMediaBagEditor(MediaBagEditor mbe) {
    this.mbe = mbe;
  }

  public String getSoundName() {
    return (String) getObject();
  }

  public void setSoundName(String name) {
    setObject(name);
  }

  @Override
  public void setObject(Object value) {
    boolean preStatus = (object != null);
    super.setObject(value);
    boolean status = (object != null);
    if (preStatus != status)
      button.setIcon(ResourceManager.getImageIcon(status ? "icons/audio_on.gif" : "icons/audio_off.gif"));
  }

  @Override
  protected Object createObject() {
    return null;
  }

  @Override
  protected Object editObject(Object o) {
    if (options == null || mbe == null)
      return null;
    return MediaBagSelector.getMediaName((String) o, options, this, mbe, Utils.ALL_SOUNDS_FF);
  }
}
