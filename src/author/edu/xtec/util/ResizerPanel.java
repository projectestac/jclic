/*
 * File    : ResizerPanel.java
 * Created : 05-jun-2003 10:15
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

package edu.xtec.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class ResizerPanel extends JPanel {

  public static final String DEFAULT_TEXTURE = "textures/diagonal.gif";
  TexturePaint tp;

  public ResizerPanel() {
    this(DEFAULT_TEXTURE);
  }

  public ResizerPanel(String texture) {
    super();
    ImageIcon img = edu.xtec.util.ResourceManager.getImageIcon(texture);
    int w = img.getIconWidth();
    int h = img.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D big = bi.createGraphics();
    big.setBackground(getBackground());
    big.drawImage(img.getImage(), 0, 0, getBackground(), null);
    Rectangle r = new Rectangle(0, 0, w, h);
    tp = new TexturePaint(bi, r);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setPaint(tp);
    g2.fill(g2.getClipBounds());
  }
}
