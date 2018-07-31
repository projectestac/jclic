/*
 * File    : JPanelActiveBox.java
 * Created : 29-may-2001 17:34
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

import edu.xtec.jclic.Activity.Panel;
import edu.xtec.jclic.Constants;
import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.misc.Utils;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * This class is a {@link javax.swing.JPanel} tha contains a single {@link
 * edu.xtec.jclic.boxes.ActiveBox}. It is used when the ActiveBox must be integrated in a complex
 * Swing container (for example, to place ActiveBoxes into {@link javax.swing.JTextPane} objects),
 * or when the active content of the ActiveBox needs to be reresented into a swing panel (for
 * example, in boxes with HTML content or with boxes that have standard multimedia controllers).
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class JPanelActiveBox extends JPanel {

  Panel parentActivityPanel = null;
  PlayStation ps;
  public ActiveBox ab;
  public boolean catchMouseEvents;
  Component mouseListener = null;
  ImageObserver io;

  /** Creates new JPanelActiveBox */
  public JPanelActiveBox(AbstractBox parent, BoxBase boxBase, ImageObserver io) {
    super();
    this.io = io;
    if (io instanceof Panel) {
      parentActivityPanel = (Panel) io;
      ps = parentActivityPanel.getPs();
    } else if (io instanceof PlayStation) {
      ps = (PlayStation) io;
    }
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    setBorder(BorderFactory.createEmptyBorder());
    ab = new ActiveBox(parent, this, boxBase);
    catchMouseEvents = true;
  }

  public void notifyMouseEventsTo(Component cmp) {
    mouseListener = cmp;
  }

  public void setPanelParent(Panel parent) {
    parentActivityPanel = parent;
    ps = (parent == null ? null : parent.getPs());
    repaint();
  }

  public void setPlayStation(PlayStation ps) {
    this.ps = ps;
  }

  @Override
  protected void processEvent(AWTEvent e) {
    if (catchMouseEvents && e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      if (e.getID() == MouseEvent.MOUSE_PRESSED
          && ab != null
          && ps != null
          && (parentActivityPanel == null || parentActivityPanel.isPlaying())) {
        ps.stopMedia(1);
        ab.playMedia(ps);
      }
      if (mouseListener != null
          && (ab == null
              || ab.getContent().mediaContent == null
              || ab.getContent().mediaContent.catchMouseEvents == false)) {
        Point bkPt = me.getPoint();
        Point pt = Utils.mapPointTo(this, bkPt, mouseListener);
        me.translatePoint(pt.x - bkPt.x, pt.y - bkPt.y);
        mouseListener.dispatchEvent(e);
      }
      me.consume();
      return;
    }
    super.processEvent(e);
  }

  public JPanel setActiveBoxContent(ActiveBoxContent abc) {
    if (abc == null) abc = new ActiveBoxContent();
    if (abc.dimension != null) ab.setBounds(0, 0, abc.dimension.width, abc.dimension.height);
    ab.setContent(abc);
    if (abc.mediaContent != null) {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    } else {
      setCursor(null);
    }
    adjustSize();
    return this;
  }

  public ActiveBoxContent getActiveBoxContent() {
    return ab.getContent();
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    ab.setBounds(0, 0, width, height);
  }

  public ActiveBox getActiveBox() {
    return ab;
  }

  public void adjustSize() {
    if (ab != null) {
      Dimension size = ab.getBorderBounds().getSize();
      setPreferredSize(size);
      setMaximumSize(size);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;

    RenderingHints rh = g2.getRenderingHints();
    g2.setRenderingHints(Constants.DEFAULT_RENDERING_HINTS);

    if (ab == null) {
      super.paintComponent(g2);
    } else {
      while (true) {
        BoxBase.flagFontReduced = false;
        ab.update(g2, g2.getClipBounds(), io);
        if (!BoxBase.flagFontReduced) break;
      }
    }
    g2.setRenderingHints(rh);
  }
}
