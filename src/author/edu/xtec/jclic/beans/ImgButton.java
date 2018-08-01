/*
 * File    : ImgButton.java
 * Created : 27-sep-2002 15:34
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

import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.jclic.bags.MediaBagSelector;
import edu.xtec.jclic.misc.Utils;
import java.awt.Rectangle;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ImgButton extends NullableObject {

  public static final String PROP_IMG_NAME = "imageName";

  MediaBagEditor mbe;

  /** Creates a new instance of ImgPanel */
  public ImgButton() {
    super();
  }

  @Override
  protected String getObjectType() {
    return PROP_IMG_NAME;
  }

  public void setMediaBagEditor(MediaBagEditor mbe) {
    this.mbe = mbe;
  }

  public String getImgName() {
    return (String) getObject();
  }

  public void setImgName(String name) {
    setObject(name);
  }

  @Override
  public void setObject(Object value) {
    super.setObject(value);
    if (nullValue || object == null || mbe == null)
      button.setIcon(null);
    else {
      try {
        Rectangle r = button.getVisibleRect();
        int w = r.width - 4;
        int h = r.height - 4;
        if (w > 0 && h > 0) {
          String s = (String) object;
          MediaBag mb = mbe.getMediaBag();
          button.setIcon(mb.getImageElement(s).getThumbNail(w, h, mb.getProject().getFileSystem()));
        }
      } catch (Exception ex) {
        System.err.println("Error reading image:\n" + ex);
      }
    }
  }

  @Override
  protected Object createObject() {
    return null;
  }

  @Override
  protected Object editObject(Object o) {
    if (options == null || mbe == null)
      return null;
    return MediaBagSelector.getMediaName((String) o, options, this, mbe, Utils.ALL_IMAGES_FF);
  }
}
