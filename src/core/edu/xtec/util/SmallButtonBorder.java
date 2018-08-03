/*
 * File    : SmallButtonBorder.java
 * Created : 14-apr-2004 17:11
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

package edu.xtec.util;

import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class SmallButtonBorder implements Border {

  boolean borderOn;
  Border m_border;

  public SmallButtonBorder(Border border, boolean borderOn) {
    m_border = border;
    this.borderOn = borderOn;
  }

  public SmallButtonBorder(JComponent jc, boolean borderOn) {
    this(jc.getBorder(), borderOn);
  }

  public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
    if (borderOn)
      m_border.paintBorder(c, g, x, y, width, height);
  }

  public java.awt.Insets getBorderInsets(java.awt.Component component) {
    java.awt.Insets i = (java.awt.Insets) m_border.getBorderInsets(component).clone();
    i.left = i.top;
    i.right = i.bottom;
    return i;
  }

  public boolean isBorderOpaque() {
    return m_border.isBorderOpaque();
  }
}
