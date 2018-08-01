/*
 * File    : ActiveBoxPreviewPanel.java
 * Created : 02-oct-2002 18:45
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

package edu.xtec.jclic.boxes;

import edu.xtec.jclic.Constants;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ActiveBoxPreviewPanel extends JPanel {

  ActiveBox ab;
  ActiveBoxContent abc;

  /** Creates a new instance of ActiveBoxPreviewPanel */
  public ActiveBoxPreviewPanel(AbstractBox parent) {
    super();
    ab = new ActiveBox(parent, this, null);
    abc = new ActiveBoxContent();
    ab.setContent(abc);
    ab.setAltContent(abc);
  }

  public void setActiveBoxContent(ActiveBoxContent b) {
    abc = (b == null ? new ActiveBoxContent() : b);
    ab.setContent(abc);
    ab.setAltContent(abc);
  }

  public ActiveBox getActiveBox() {
    return ab;
  }

  public ActiveBoxContent getActiveBoxContent() {
    return abc;
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    ab.setBounds(getVisibleRect());
    BoxBase.resetAllFonts();
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;

    super.paintComponent(g);

    RenderingHints rh = g2.getRenderingHints();
    g2.setRenderingHints(Constants.DEFAULT_RENDERING_HINTS);
    while (true) {
      BoxBase.flagFontReduced = false;
      ab.update(g2, g2.getClipBounds(), this);
      if (!BoxBase.flagFontReduced)
        break;
    }
    g2.setRenderingHints(rh);
  }
}
