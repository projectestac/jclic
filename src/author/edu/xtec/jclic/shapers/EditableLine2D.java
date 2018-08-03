/*
 * File    : EditableLine2D.java
 * Created : 25-feb-2002 09:29
 * By      : allastar
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

package edu.xtec.jclic.shapers;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author allastar
 * @version 1.0
 */
public class EditableLine2D extends Line2D.Double implements EditableShape {

  private boolean selected = false;
  private int border = 0; // Border indicates if the last clicked point was the first or the second
  private int selectedBorder = -1;

  /** Creates new EditableLine2D */
  public EditableLine2D(double x1, double y1, double x2, double y2) {
    super(x1, y1, x2, y2);
  }

  public EditableLine2D(Point2D p1, Point2D p2) {
    super(p1, p2);
  }

  public boolean isSelected() {
    return selected;
  }

  public void drawBorders(java.awt.Graphics g) {
    g.drawRect((int) x1 - (EditableShapeConstants.selectLength / 2),
        (int) y1 - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
        EditableShapeConstants.selectLength);
    g.drawRect((int) x2 - (EditableShapeConstants.selectLength / 2),
        (int) y2 - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
        EditableShapeConstants.selectLength);
    g.setColor(EditableShapeConstants.SELECTED_BORDER_COLOR);
    if (selectedBorder == 1) {
      g.fillRect((int) x1 - (EditableShapeConstants.selectLength / 2),
          (int) y1 - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    } else if (selectedBorder == 2) {
      g.fillRect((int) x2 - (EditableShapeConstants.selectLength / 2),
          (int) y2 - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    }
  }

  public boolean hasClickedBorder(double x, double y, boolean needSelected) {
    boolean hasClicked = false;
    if (!needSelected || selected) {
      Rectangle r1 = new Rectangle((int) x1 - (EditableShapeConstants.selectLength / 2),
          (int) y1 - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
      Rectangle r2 = new Rectangle((int) x2 - (EditableShapeConstants.selectLength / 2),
          (int) y2 - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
      if (r1.contains(x, y)) {
        border = 1;
        hasClicked = true;
      } else if (r2.contains(x, y)) {
        border = 2;
        hasClicked = true;
      }
    }
    return hasClicked;
  }

  public void aproximateNearestBorder(double x, double y) {
    if (getP1().distance(x, y) < getP2().distance(x, y)) {
      x1 = x;
      y1 = y;
    } else {
      x2 = x;
      y2 = y;
    }
  }

  public java.awt.geom.Point2D getNearestBorder(double x, double y) {
    return (getP1().distance(x, y) < getP2().distance(x, y)) ? getP1() : getP2();
  }

  public void setSelected(boolean b) {
    selected = b;
  }

  public void paintWithColor(java.awt.Graphics g, int drawingMode, java.awt.Color c) {
    g.setColor(EditableShapeConstants.BORDER_COLOR);
    drawBorders(g);
    if (selected && drawingMode != PolygonDrawPanel.MOVING) {
      paintSelection(g);
    } else if (drawingMode == PolygonDrawPanel.NEW_POINT) {
      g.setColor(Color.red);
      drawBorders(g);
      g.setColor(c);
    } else
      g.setColor(c);
    g.drawLine((int) getX1(), (int) getY1(), (int) getX2(), (int) getY2());
  }

  public void paint(java.awt.Graphics g, int drawingMode) {
    paintWithColor(g, drawingMode, EditableShapeConstants.defaultColor);
  }

  public void paintSelection(java.awt.Graphics g) {
    g.setColor(Color.black);
    drawBorders(g);
    g.setColor(EditableShapeConstants.selectedColor);
  }

  public void changeBorder(double x, double y) {
    if (border == 1) {
      x1 = x;
      y1 = y;
    } else {
      x2 = x;
      y2 = y;
    }
  }

  public double distanceTo(double x, double y) {
    return ptSegDist(x, y);
  }

  public boolean isInto(java.awt.geom.Rectangle2D r) {
    return r.intersectsLine(this);
  }

  public void move(double incX, double incY) {
    x1 += incX;
    x2 += incX;
    y1 += incY;
    y2 += incY;
  }

  public void transform(java.awt.geom.AffineTransform aTransf) {
    Point2D p1 = getP1();
    Point2D p2 = getP2();
    aTransf.transform(p1, p1);
    aTransf.transform(p2, p2);
    setLine(p1, p2);
  }

  public EditableShape[] divide(double x, double y) {
    EditableShape[] newShapes = new EditableShape[2];
    Point2D p = new Point2D.Double(x, y);
    newShapes[0] = new EditableLine2D(p, getP2());
    newShapes[1] = new EditableLine2D(getP1(), p);
    return newShapes;
  }

  public boolean isAdjacentTo(java.awt.geom.Point2D p) {
    if (getP1().equals(p))
      return true;
    else if (getP2().equals(p)) {
      setLine(getP2(), getP1());
      return true;
    } else
      return false;
  }

  public java.awt.geom.Point2D getEndPoint() {
    return getP2();
  }

  public java.awt.geom.Point2D getInitialPoint() {
    return getP1();
  }

  public Object clone() {
    EditableLine2D el = new EditableLine2D(getInitialPoint(), getEndPoint());
    el.setSelected(isSelected());
    return el;
  }

  public java.awt.geom.Point2D[] getBorders() {
    return new Point2D[] { getP1(), getP2() };
  }

  public void selectBorder(double x, double y) {
    Point2D p = new Point2D.Double(x, y);
    if (getP1().equals(p))
      selectedBorder = 1;
    else if (getP2().equals(p))
      selectedBorder = 2;
    else
      selectedBorder = -1;
  }

  public void deselectBorder() {
    selectedBorder = -1;
  }

  public boolean hasSelectedBorder() {
    return (selectedBorder != -1);
  }

  public java.awt.geom.Point2D getNotSelectedBorder() {
    if (selectedBorder == 1)
      return getP2();
    else
      return getP1();
  }
}
