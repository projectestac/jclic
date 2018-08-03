/*
 * File    : ColorButton.java
 * Created : 26-sep-2002 13:38
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

import edu.xtec.util.Options;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import javax.swing.JButton;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ColorButton extends JButton {

  Options options;
  public static final String PROP_COLOR = "color";
  public static final int BT_PREF_WIDTH = 31, BT_PREF_HEIGHT = 21;
  public static final int SAMPLE_WIDTH = 21, SAMPLE_HEIGHT = 14;
  Color color;

  /** Creates a new instance of ColorButton */
  public ColorButton() {
    super();
    setPreferredSize(new java.awt.Dimension(BT_PREF_WIDTH, BT_PREF_HEIGHT));
    setMinimumSize(getPreferredSize());
    setMaximumSize(getPreferredSize());
    setColor(Color.lightGray);
  }

  public void setOptions(Options options) {
    this.options = options;
  }

  @Override
  protected void fireActionPerformed(ActionEvent event) {
    if (options != null) {
      Color c = AlphaColorChooserPanel.chooseColor(options, this, getColor());
      if (c != null)
        changeColor(c);
    }
    super.fireActionPerformed(event);
  }

  public void setColor(Color c) {
    color = c;
    repaint();
  }

  public Color getColor() {
    return color;
  }

  public void changeColor(Color c) {
    Color oldColor = getColor();
    setColor(c);
    if (!oldColor.equals(c))
      firePropertyChange(PROP_COLOR, oldColor, c);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    java.awt.Rectangle r = new java.awt.Rectangle(3, 3, getWidth() - 6, getHeight() - 6);
    g.setColor(color);
    g.fillRect(r.x, r.y, r.width, r.height);
  }
}
