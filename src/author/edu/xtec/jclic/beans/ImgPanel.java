/*
 * File    : ImgPanel.java
 * Created : 17-feb-2004 14:57
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

import java.awt.Dimension;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ImgPanel extends JLabel {

  private boolean missingPicture = false;

  public ImgPanel(Image img) {
    this(new ImageIcon(img));
  }

  public ImgPanel(ImageIcon i) {
    super(i);
    if (i == null) {
      missingPicture = true;
      setText("No picture found.");
      setHorizontalAlignment(CENTER);
      setOpaque(true);
    }

    setAutoscrolls(true); // enable synthetic drag events
  }

  @Override
  public Dimension getPreferredSize() {
    if (missingPicture) {
      return new Dimension(320, 480);
    } else {
      return super.getPreferredSize();
    }
  }
}
