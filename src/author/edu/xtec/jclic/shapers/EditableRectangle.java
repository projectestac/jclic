/*
 * File    : EditableRectangle.java
 * Created : 26-feb-2002 13:32
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
public class EditableRectangle extends Rectangle2D.Double implements EditableShape, Cloneable {

  protected boolean selected = false;
  protected int border = -1;
  private int selectedBorder = -1;

  protected Point2D p1, p2, p3, p4;
  protected Point2D lastP1, lastP2;

  /** Creates new EditableRectangle */
  public EditableRectangle(Point2D ini, Point2D end) {
    super();
    setLimits(ini, end);
  }

  public EditableRectangle(int x, int y, int width, int height) {
    super();
    setLimits(x, y, width, height);
  }

  public void setLimits(int x, int y, int width, int height) {
    if (width < 0) {
      width *= -1;
      x -= width;
    }
    if (height < 0) {
      height *= -1;
      y -= height;
    }
    p1 = new Point2D.Double(x, y);
    p2 = new Point2D.Double(x + width, y + height);
    p3 = new Point2D.Double();
    p4 = new Point2D.Double();
    lastP1 = new Point2D.Double(1, 1);
    lastP2 = new Point2D.Double(1, 1);
    setRect(p1, p2);
  }

  public void setLimits(Point2D ini, Point2D end) {
    p1 = new Point2D.Double();
    p2 = new Point2D.Double();
    p3 = new Point2D.Double();
    p4 = new Point2D.Double();
    lastP1 = new Point2D.Double(1, 1);
    lastP2 = new Point2D.Double(1, 1);
    setRect(ini, end);
  }

  public EditableShape[] divide(double x, double y) { // Deixara de ser un Rectangle. Retornem un array amb 5 linies
    return divide(x, y, true);
  }

  public EditableShape[] divide(double x, double y, boolean newPoint) {
    // Retornem un array amb les noves linies del Rectangle. Si newPoint=true partim
    // la recta que conte (x,y)
    EditableShape[] lines = new EditableShape[5];
    lines[0] = new EditableLine2D(p1, p3);
    lines[1] = new EditableLine2D(p3, p2);
    lines[2] = new EditableLine2D(p2, p4);
    lines[3] = new EditableLine2D(p4, p1);
    if (newPoint) {
      double minDist = ((EditableLine2D) lines[0]).ptLineDistSq(x, y);
      short nearestLine = 0;
      for (short i = 1; i < 4; i++) {
        double dist = ((EditableLine2D) lines[i]).ptLineDistSq(x, y);
        if (dist < minDist) {
          minDist = dist;
          nearestLine = i;
        }
      }
      Point2D p = new Point2D.Double(x, y);
      Point2D p1 = ((EditableLine2D) lines[nearestLine]).getP1();
      Point2D p2 = ((EditableLine2D) lines[nearestLine]).getP2();
      lines[nearestLine] = new EditableLine2D(p1, p);
      lines[4] = new EditableLine2D(p, p2);
    }
    return lines;
  }

  public void setSelected(boolean b) {
    selected = b;
  }

  public double distanceTo(double x, double y) {
    Line2D l1 = new Line2D.Double(p1, p3);
    Line2D l2 = new Line2D.Double(p3, p2);
    Line2D l3 = new Line2D.Double(p2, p4);
    Line2D l4 = new Line2D.Double(p4, p1);
    double d1 = l1.ptLineDistSq(x, y);
    double d2 = l2.ptLineDistSq(x, y);
    double d3 = l3.ptLineDistSq(x, y);
    double d4 = l4.ptLineDistSq(x, y);
    double d5, d6;
    if (d1 < d2)
      d5 = d1;
    else
      d5 = d2;
    if (d3 < d4)
      d6 = d3;
    else
      d6 = d4;
    if (d5 < d6)
      return d5;
    else
      return d6;
  }

  public boolean hasClickedBorder(double x, double y, boolean needSelected) {
    boolean hasClicked = false;
    if (!needSelected || selected) {
      Rectangle r1 = new Rectangle((int) p1.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p1.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
      Rectangle r2 = new Rectangle((int) p2.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p2.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
      Rectangle r3 = new Rectangle((int) p3.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p3.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
      Rectangle r4 = new Rectangle((int) p4.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p4.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
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
      } else if (r4.contains(x, y)) {
        border = 4;
        hasClicked = true;
      }
    }
    return hasClicked;
  }

  public void paintSelection(java.awt.Graphics g) {
    g.setColor(Color.black);
    drawBorders(g);
    g.setColor(EditableShapeConstants.selectedColor);
  }

  public boolean isSelected() {
    return selected;
  }

  public void aproximateNearestBorder(double x, double y) {
    boolean b = hasClickedBorder(x, y, false); // Marca la cantonada mes propera
    changeBorder(x, y); // Posa la cantonada mes propera a (x,y) en la posicio (x,y)
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
    } else
      g.setColor(c);
    g.drawRect((int) getX(), (int) getY(), (int) getWidth(), (int) getHeight());
  }

  public void transform(java.awt.geom.AffineTransform aTransf) {
    aTransf.transform(p1, p1);
    aTransf.transform(p2, p2);
    setRect(p1, p2);
  }

  public boolean isAdjacentTo(java.awt.geom.Point2D p) {
    if (p.equals(p1) || p.equals(p2) || p.equals(p3) || p.equals(p4))
      return true;
    else
      return false;
  }

  public boolean isInto(java.awt.geom.Rectangle2D r) {
    return (r.intersects(this));
  }

  public java.awt.geom.Point2D getEndPoint() {
    return p2;
  }

  private void setRect(Point2D ini, Point2D end) {
    if (!(lastP1.equals(ini) && lastP2.equals(end))) {
      p1 = ini;
      p2 = end;
      p3.setLocation(p2.getX(), p1.getY());
      p4.setLocation(p1.getX(), p2.getY());
      double x = Math.min(p1.getX(), p2.getX());
      double y = Math.min(p1.getY(), p2.getY());
      double width = Math.abs(p2.getX() - p1.getX());
      double height = Math.abs(p2.getY() - p1.getY());
      setRect(x, y, width, height);
      lastP1 = (Point2D) p1.clone();
      lastP2 = (Point2D) p2.clone();
    }
  }

  public void changeBorder(double x, double y) {
    Point2D newPoint = new Point2D.Double(x, y);
    if (border == 1) {
      setRect(newPoint, p2);
    } else if (border == 2) {
      setRect(p1, newPoint);
    } else if (border == 3) {
      p1.setLocation(p1.getX(), newPoint.getY());
      p2.setLocation(newPoint.getX(), p2.getY());
      setRect(p1, p2);
    } else if (border == 4) {
      p1.setLocation(newPoint.getX(), p1.getY());
      p2.setLocation(p2.getX(), newPoint.getY());
      setRect(p1, p2);
    }
  }

  public void drawBorders(java.awt.Graphics g) {
    g.drawRect((int) p1.getX() - (EditableShapeConstants.selectLength / 2),
        (int) p1.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
        EditableShapeConstants.selectLength);
    g.drawRect((int) p2.getX() - (EditableShapeConstants.selectLength / 2),
        (int) p2.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
        EditableShapeConstants.selectLength);
    g.drawRect((int) p3.getX() - (EditableShapeConstants.selectLength / 2),
        (int) p3.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
        EditableShapeConstants.selectLength);
    g.drawRect((int) p4.getX() - (EditableShapeConstants.selectLength / 2),
        (int) p4.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
        EditableShapeConstants.selectLength);
    g.setColor(EditableShapeConstants.SELECTED_BORDER_COLOR);
    if (selectedBorder == 1) {
      g.fillRect((int) p1.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p1.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    } else if (selectedBorder == 2) {
      g.fillRect((int) p2.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p2.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    } else if (selectedBorder == 3) {
      g.fillRect((int) p3.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p3.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    } else if (selectedBorder == 4) {
      g.fillRect((int) p4.getX() - (EditableShapeConstants.selectLength / 2),
          (int) p4.getY() - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
          EditableShapeConstants.selectLength);
    }
  }

  public java.awt.geom.Point2D getInitialPoint() {
    return getLocation();
  }

  public void paint(java.awt.Graphics g, int drawingMode) {
    paintWithColor(g, drawingMode, EditableShapeConstants.defaultColor);
  }

  public java.awt.geom.Point2D getNearestBorder(double x, double y) {
    double d1 = p1.distance(x, y);
    double d2 = p2.distance(x, y);
    double d3 = p3.distance(x, y);
    double d4 = p4.distance(x, y);
    Point2D p5, p6;
    if (d1 < d2)
      p5 = p1;
    else
      p5 = p2;
    if (d3 < d4)
      p6 = p3;
    else
      p6 = p4;
    if (p5.distance(x, y) < p6.distance(x, y))
      return p5;
    else
      return p6;
  }

  public Point2D getLocation() {
    return new Point2D.Double(getX(), getY());
  }

  /*
   * public Rectangle2D getRectangle(){ return new
   * Rectangle2D.Double(getX(),getY(),getWidth(),getHeight()); }
   */

  public Object clone() {
    return new EditableRectangle(getLocation(),
        new Point2D.Double(getLocation().getX() + getWidth(), getLocation().getY() + getHeight()));
  }

  public java.awt.geom.Point2D[] getBorders() {
    return new Point2D[] { p1, p2, p3, p4 };
  }

  public void selectBorder(double x, double y) {
    Point2D p = new Point2D.Double(x, y);
    if (p1.equals(p))
      selectedBorder = 1;
    else if (p2.equals(p))
      selectedBorder = 2;
    else if (p3.equals(p))
      selectedBorder = 3;
    else if (p4.equals(p))
      selectedBorder = 4;
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
      return p1;
    else if (selectedBorder == 2)
      return p2;
    else if (selectedBorder == 3)
      return p3;
    else
      return p4;
  }
}
