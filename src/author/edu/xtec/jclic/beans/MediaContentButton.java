/*
 * File    : MediaContentButton.java
 * Created : 23-dec-2002 16:26
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

package edu.xtec.jclic.beans;

import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.jclic.media.MediaContent;
import edu.xtec.jclic.media.MediaContentEditor;
import edu.xtec.util.ResourceManager;
import javax.swing.UIManager;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class MediaContentButton extends NullableObject {

  public static final String PROP_MEDIA_CONTENT = "mediaContent";
  public static final String[] MEDIA_ICONS = { "icons/unknown_small.gif", "icons/audio_on.gif", "icons/movie.gif",
      "icons/music.gif", "icons/cdaudio.gif", "icons/speak.gif", "icons/play_speaked.gif", "icons/jump_to_activity.gif",
      "icons/jump_to_sequence.gif", "icons/run_external.gif", "icons/html_doc.gif", "icons/cancel.gif",
      "icons/return.gif" };

  MediaBagEditor mbe;

  /** Creates a new instance of MediaContentButton */
  public MediaContentButton() {
    super();
  }

  @Override
  protected String getObjectType() {
    return PROP_MEDIA_CONTENT;
  }

  public MediaContent getMediaContent() {
    return (MediaContent) getObject();
  }

  public void setMediaContent(MediaContent mc) {
    setObject(mc);
  }

  public void setMediaBagEditor(MediaBagEditor mbe) {
    this.mbe = mbe;
  }

  @Override
  public void setObject(Object value) {
    super.setObject(value);
    if (nullValue || object == null) {
      button.setBackground(UIManager.getColor("Button.background"));
      button.setForeground(UIManager.getColor("Button.foreground"));
      button.setIcon(null);
    } else {
      MediaContent mc = (MediaContent) value;
      String imgName = "unknown_small.gif";
      if (mc.mediaType >= 0 && mc.mediaType < MEDIA_ICONS.length)
        imgName = MEDIA_ICONS[mc.mediaType];

      button.setIcon(ResourceManager.getImageIcon(imgName));
    }
  }

  @Override
  protected Object createObject() {
    return new MediaContent();
  }

  @Override
  protected Object editObject(Object o) {
    MediaContent mc = (MediaContent) (o == null ? createObject() : o);
    return MediaContentEditor.getMediaContent(mc, this, options, mbe);
  }
}
