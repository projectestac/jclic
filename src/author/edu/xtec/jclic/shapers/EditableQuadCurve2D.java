/*
 * File    : EditableQuadCurve2D.java
 * Created : 26-feb-2002 11:35
 * By      : allastar
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

package edu.xtec.jclic.shapers;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author allastar
 * @version 1.0
 */
public class EditableQuadCurve2D extends QuadCurve2D.Double implements EditableShape {

  private boolean selected = false;
  private int border = -1;
  private int selectedBorder = -1;

  /** Creates new EditableQuadCurve2D */
  public EditableQuadCurve2D(QuadCurve2D curve) {
    super();
    setCurve(curve);
  }

  public EditableQuadCurve2D(
      double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
    super(x1, y1, ctrlx, ctrly, x2, y2);
  }

  public boolean isSelected() {
    return selected;
  }

  public boolean isInto(java.awt.geom.Rectangle2D r) {
    return (r.contains(getP1()) || r.contains(getP2()) || r.contains(getCtrlPt()));
  }

  public void drawBorders(java.awt.Graphics g) {
    if (getP1().getX() != -1)
      g.drawRect(
          (int) getP1().getX() - (EditableShapeConstants.selectLength / 2),
          (int) getP1().getY() - (EditableShapeConstants.selectLength / 2),
          EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    if (getP2().getX() != -1)
      g.drawRect(
          (int) getP2().getX() - (EditableShapeConstants.selectLength / 2),
          (int) getP2().getY() - (EditableShapeConstants.selectLength / 2),
          EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    if (getCtrlPt().getX() != -1)
      g.fillRect(
          (int) getCtrlPt().getX() - (EditableShapeConstants.selectLength / 2),
          (int) getCtrlPt().getY() - (EditableShapeConstants.selectLength / 2),
          EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    g.setColor(EditableShapeConstants.SELECTED_BORDER_COLOR);
    if (selectedBorder == 1) {
      g.fillRect(
          (int) getP1().getX() - (EditableShapeConstants.selectLength / 2),
          (int) getP1().getY() - (EditableShapeConstants.selectLength / 2),
          EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    } else if (selectedBorder == 2) {
      g.fillRect(
          (int) getP2().getX() - (EditableShapeConstants.selectLength / 2),
          (int) getP2().getY() - (EditableShapeConstants.selectLength / 2),
          EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    }
  }

  public boolean hasClickedBorder(double x, double y, boolean needSelected) {
    boolean hasClicked = false;
    if (!needSelected || selected) {
      Rectangle r1 =
          new Rectangle(
              (int) getP1().getX() - (EditableShapeConstants.selectLength / 2),
              (int) getP1().getY() - (EditableShapeConstants.selectLength / 2),
              EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength);
      Rectangle r2 =
          new Rectangle(
              (int) getP2().getX() - (EditableShapeConstants.selectLength / 2),
              (int) getP2().getY() - (EditableShapeConstants.selectLength / 2),
              EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength);
      Rectangle r3 =
          new Rectangle(
              (int) getCtrlPt().getX() - (EditableShapeConstants.selectLength / 2),
              (int) getCtrlPt().getY() - (EditableShapeConstants.selectLength / 2),
              EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength);
      if (r1.contains(x, y)) {
        border = 1;
        hasClicked = true;
      } else if (r2.contains(x, y)) {
        border = 2;
        hasClicked = true;
      } else if (r3.contains(x, y)) {
        border = 3;
        hasClicked = true;
      }
    }
    return hasClicked;
  }

  public void aproximateNearestBorder(double x, double y) {
    // getNearestBorder(x,y).setLocation(x,y);
    if (hasClickedBorder(x, y, false)) changeBorder(x, y);
  }

  public java.awt.geom.Point2D getNearestBorder(double x, double y) {
    Point2D p1;
    double d1 = getP1().distance(x, y);
    double d2 = getP2().distance(x, y);
    double d3 = getCtrlPt().distance(x, y);
    if (d1 < d2) p1 = getP1();
    else {
      p1 = getP2();
      d1 = d2;
    }
    if (d3 < d1) p1 = getCtrlPt();
    return p1;
  }

  public void setSelected(boolean b) {
    selected = b;
  }

  public void changeBorder(double x, double y) {
    Point2D p = new Point2D.Double(x, y);
    if (border == 1) {
      setCurve(p, getCtrlPt(), getP2());
    } else if (border == 2) {
      setCurve(getP1(), getCtrlPt(), p);
    } else { // border==3
      setCurve(getP1(), p, getP2());
    }
  }

  public double distanceTo(double x, double y) {
    double d1 = getNearestBorder(x, y).distance(x, y);
    double d2 = (new Line2D.Double(getP1(), getP2())).ptSegDist(x, y);
    return Math.min(d1, d2);
  }

  public void paintWithColor(java.awt.Graphics g, int drawingMode, java.awt.Color c) {
    g.setColor(EditableShapeConstants.BORDER_COLOR);
    drawBorders(g);
    if (selected) {
      paintSelection(g);
    } else if (drawingMode == PolygonDrawPanel.NEW_POINT) {
      g.setColor(Color.red);
      drawBorders(g);
      g.setColor(c);
    } else g.setColor(c);
    ((Graphics2D) g).draw(this);
  }

  public void paint(java.awt.Graphics g, int drawingMode) {
    paintWithColor(g, drawingMode, EditableShapeConstants.defaultColor);
  }

  public void paintSelection(java.awt.Graphics g) {
    g.setColor(Color.black);
    drawBorders(g);
    g.setColor(EditableShapeConstants.selectedColor);
  }

  public void move(double incX, double incY) {
    setCurve(
        getP1().getX() + incX,
        getP1().getY() + incY,
        getCtrlX() + incX,
        getCtrlY() + incY,
        getP2().getX() + incX,
        getP2().getY() + incY);
  }

  public void transform(java.awt.geom.AffineTransform aTransf) {
    Point2D p1 = getP1();
    Point2D p2 = getCtrlPt();
    Point2D p3 = getP2();
    aTransf.transform(p1, p1);
    aTransf.transform(p2, p2);
    aTransf.transform(p3, p3);
    setCurve(p1, p2, p3);
  }

  public EditableShape[] divide(double x, double y) {
    EditableShape[] newShapes = new EditableShape[2];
    QuadCurve2D left = new QuadCurve2D.Double();
    QuadCurve2D right = new QuadCurve2D.Double();
    subdivide(left, right);
    newShapes[0] = new EditableQuadCurve2D(left);
    newShapes[1] = new EditableQuadCurve2D(right);
    //// setCurve(left);
    return newShapes;
  }

  public boolean isAdjacentTo(java.awt.geom.Point2D p) {
    if (getP1().equals(p)) return true;
    else if (getP2().equals(p)) {
      setCurve(getP2(), getCtrlPt(), getP1());
      return true;
    } else return false;
  }

  public java.awt.geom.Point2D getEndPoint() {
    return getP2();
  }

  public java.awt.geom.Point2D getInitialPoint() {
    return getP1();
  }

  public Object clone() {
    return new EditableQuadCurve2D(this);
  }

  public java.awt.geom.Point2D[] getBorders() {
    return new Point2D[] {getP1(), getP2(), getCtrlPt()};
  }

  public void selectBorder(double x, double y) {
    Point2D p = new Point2D.Double(x, y);
    if (getP1().equals(p)) selectedBorder = 1;
    else if (getP2().equals(p)) selectedBorder = 2;
    else selectedBorder = -1;
  }

  public void deselectBorder() {
    selectedBorder = -1;
  }

  public boolean hasSelectedBorder() {
    return (selectedBorder != -1);
  }

  public java.awt.geom.Point2D getNotSelectedBorder() {
    if (selectedBorder == 1) return getP2();
    else return getP1();
  }
}
