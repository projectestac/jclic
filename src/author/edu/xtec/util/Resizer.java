/*
 * File    : Resizer.java
 * Created : 08-mai-2003 14:42
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

import edu.xtec.jclic.Constants;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.JComponent;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class Resizer implements MouseListener, MouseMotionListener {

  JComponent jc;
  Rectangle r;
  Cursor defaultCursor;
  boolean doGetBounds;
  boolean dragging;
  boolean dragCursorX, dragCursorY;
  ResizerListener rl;
  Dimension minSize;
  Dimension maxSize;
  boolean editable;
  boolean enabled;

  public static final int MARGIN = 5;

  /** Creates a new instance of Resizer */
  public Resizer(JComponent jc, Rectangle r, boolean editable, ResizerListener rl) {
    this.jc = jc;
    setRect(r);
    this.editable = editable;
    if (editable) defaultCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    this.rl = rl;
    jc.addMouseListener(this);
    jc.addMouseMotionListener(this);
    minSize = new Dimension(Constants.MIN_CELL_SIZE, Constants.MIN_CELL_SIZE);
    maxSize = new Dimension(jc.getPreferredSize());
    setEnabled(true);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    jc.setCursor(defaultCursor);
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setMinSize(Dimension d) {
    minSize = d;
  }

  public void setMaxSize(Dimension d) {
    maxSize = d;
  }

  public void setRect(Rectangle r) {
    this.r = r;
    doGetBounds = (r == null);
    if (doGetBounds) this.r = jc.getBounds();
  }

  public void mouseClicked(MouseEvent mouseEvent) {
    if (enabled && editable && rl != null && r.contains(mouseEvent.getPoint())) {
      rl.editObject(r, jc, mouseEvent.getPoint());
    }
  }

  public void mouseDragged(MouseEvent mouseEvent) {
    if (enabled && dragging) resizeByDrag(mouseEvent.getPoint());
  }

  public void mouseEntered(MouseEvent mouseEvent) {}

  public void mouseExited(MouseEvent mouseEvent) {}

  public void mouseMoved(MouseEvent mouseEvent) {
    if (!dragging && enabled) {
      Point p = mouseEvent.getPoint();
      Cursor newCursor = defaultCursor;
      if (doGetBounds) r = jc.getBounds();
      dragCursorX =
          Math.abs(p.x - r.x - r.width) < MARGIN && p.y >= r.y && p.y < (r.y + r.height + MARGIN);
      dragCursorY =
          Math.abs(p.y - r.y - r.height) < MARGIN && p.x >= r.x && p.x < (r.x + r.width + MARGIN);
      if (dragCursorX && dragCursorY)
        newCursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
      else if (dragCursorY) newCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
      else if (dragCursorX) newCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
      jc.setCursor(newCursor);
    } else if (dragging) mouseDragged(mouseEvent);
  }

  public void mousePressed(MouseEvent mouseEvent) {
    if (enabled && (dragCursorX || dragCursorY)) {
      dragging = true;
      resizeByDrag(mouseEvent.getPoint());
    }
  }

  public void mouseReleased(MouseEvent mouseEvent) {
    if (enabled && dragging) {
      resizeByDrag(mouseEvent.getPoint());
      dragging = false;
    }
  }

  private void resizeByDrag(Point p) {
    int w = r.width;
    int h = r.height;
    if (dragCursorX) w = Math.min(maxSize.width, Math.max(minSize.width, p.x - r.x));
    if (dragCursorY) h = Math.min(maxSize.height, Math.max(minSize.height, p.y - r.y));
    r = new Rectangle(r.x, r.y, w, h);
    if (rl != null) rl.resizeObjectTo(r, jc);
  }

  public JComponent getComponent() {
    return jc;
  }

  public interface ResizerListener {
    public void resizeObjectTo(Rectangle r, JComponent jc);

    public void editObject(Rectangle r, JComponent jc, Point pt);
  }
}
