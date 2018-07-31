/*
 * File    : EditableEllipse2D.java
 * Created : 28-feb-2002 09:45
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
public class EditableEllipse2D extends EditableRectangle implements Cloneable {

  public EditableEllipse2D(Point2D ini, Point2D end) {
    super(ini, end);
  }

  public EditableEllipse2D(int x, int y, int width, int height) {
    super(x, y, width, height);
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
    g.drawOval((int) getX(), (int) getY(), (int) getWidth(), (int) getHeight());
  }

  public boolean contains(double x, double y) {
    return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight()).contains(x, y);
  }

  public boolean contains(double x, double y, double w, double h) {
    return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight()).contains(x, y, w, h);
  }

  public PathIterator getPathIterator(AffineTransform at) {
    return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight()).getPathIterator(at);
  }

  public boolean intersects(double x, double y, double w, double h) {
    return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight()).intersects(x, y, w, h);
  }

  public EditableShape[] divide(double x, double y, boolean b) {
    return divide(x, y);
  }

  public EditableShape[] divide(
      double x, double y) { // The ellipse will be converted to an array of Bezier curves
    EditableShape[] shapes = new EditableShape[10];
    int currentShape = 0;
    double currentX = 0, currentY = 0;
    PathIterator pi = getPathIterator(new AffineTransform());
    while (!pi.isDone()) {
      double[] points = new double[6];
      int type = pi.currentSegment(points);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          currentX = points[0];
          currentY = points[1];
          break;
        case PathIterator.SEG_LINETO:
          shapes[currentShape] = new EditableLine2D(currentX, currentY, points[0], points[1]);
          currentShape++;
          currentX = points[0];
          currentY = points[1];
          break;
        case PathIterator.SEG_CUBICTO:
          shapes[currentShape] =
              new EditableCubicCurve2D(
                  currentX, currentY, points[0], points[1], points[2], points[3], points[4],
                  points[5]);
          currentShape++;
          currentX = points[4];
          currentY = points[5];
          break;
        case PathIterator.SEG_QUADTO:
          shapes[currentShape] =
              new EditableQuadCurve2D(
                  currentX, currentY, points[0], points[1], points[2], points[3]);
          currentShape++;
          currentX = points[2];
          currentY = points[3];
          break;
      }
      pi.next();
    }
    return shapes;
  }

  public Ellipse2D getEllipse() {
    return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight());
  }

  public Object clone() {
    return new EditableEllipse2D(
        getLocation(),
        new Point2D.Double(getLocation().getX() + getWidth(), getLocation().getY() + getHeight()));
  }
}
