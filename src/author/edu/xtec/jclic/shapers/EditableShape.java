/*
 * File    : EditableShape.java
 * Created : 25-feb-2002 08:55
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

/**
 * This interface defines a {@link java.awt.Shape} that implements specific
 * methods to be modified (edited) in a graphic environment
 *
 * @author allastar
 * @version 1.0
 */
public interface EditableShape extends java.awt.Shape {

  /**
   * Selects/unselects the shape
   *
   * @param b The state (selected/unselected) of the shape
   */
  public void setSelected(boolean b);

  /**
   * Checks if the shape is selected
   *
   * @return <CODE>true</CODE> if the shape is selected, <CODE>false</CODE>
   *         otherwise.
   */
  public boolean isSelected();

  /**
   * Draws the shape with the default colour
   *
   * @param g           The graphic environment where to draw the shape
   * @param drawingMode The drawing mode. Squares are drawed in red when this
   *                    value is ShapeGenPanel.NEW_POINT
   */
  public void paint(java.awt.Graphics g, int drawingMode);

  /**
   * Draws the shape with the indicated color.
   *
   * @param g           The graphic environment where to draw the shape
   * @param drawingMode The drawing mode. Squares are drawed in red when this
   *                    value is ShapeGenPanel.NEW_POINT
   * @param c           The color to be used to draw the shape
   */
  public void paintWithColor(java.awt.Graphics g, int drawingMode, java.awt.Color c);

  /**
   * Draws squares in black and the remainder objects in the selection color
   * (EditableShapeConstants.selectedColor)
   *
   * @param g The graphic environment where to draw the shape
   */
  public void paintSelection(java.awt.Graphics g);

  /**
   * Draws the shape with dragable sides
   *
   * @param g The graphic environment where to draw the shape
   */
  public void drawBorders(java.awt.Graphics g);

  /**
   * Checks if the specified point lies within a side of the shape
   *
   * @param x            The horizontal co-ordinate of the point to check
   * @param y            The vertical co-ordinate of the point to check
   * @param needSelected When the value of this param is <CODE>true</CODE>, the
   *                     side must be selected.
   * @return Returns <CODE>true</CODE> when a side of the shape is near the point
   *         defined by x and y.
   */
  public boolean hasClickedBorder(double x, double y, boolean needSelected);

  /**
   * Selects the side of the shape that lies with the specified point (if any).
   *
   * @param x The horizontal co-ordinate of the point to check
   * @param y The vertical co-ordinate of the point to check
   */
  public void selectBorder(double x, double y);

  /**
   * Rerturns the starting point of the side of the shape nearest to a specific
   * point.
   *
   * @param x The horizontal co-ordinate of the point
   * @param y The vertical co-ordinate of the point
   * @return The starting point of the side nearest to the point
   */
  public java.awt.geom.Point2D getNearestBorder(double x, double y);

  /**
   * Moves towards a specific point the side of the shape that is located nearest
   * it. .
   *
   * @param x The horizontal co-ordinate of the point
   * @param y The vertival co-ordinate of the point
   */
  public void aproximateNearestBorder(double x, double y);

  /**
   * Computes the distance between a supplied point and the nearest limit of the
   * shape.
   *
   * @param x The horizontal co-ordinate of the point
   * @param y The vertical co-ordinate of the point
   * @return The distance between the point and the shape
   */
  public double distanceTo(double x, double y);

  /**
   * Checks if the shape is into the limits of a specific
   * {@link java.awt.Rectangle}.
   *
   * @param r The rectangle to be checked
   * @return <CODE>true</CODE> if the shape intersects with the rectangle,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isInto(java.awt.geom.Rectangle2D r);

  /**
   * Changes the co-ordinates of the previously marked corner to (x, y)
   *
   * @param x The horizontal co-ordinate of the point
   * @param y The vertical co-ordinate of the point
   */
  public void changeBorder(double x, double y);

  /** Unselects the selected border of the shape, if any. */
  public void deselectBorder();

  /**
   * Gets the starting point of the first non-selected side of the shape
   *
   * @return The starting point of the side
   */
  public java.awt.geom.Point2D getNotSelectedBorder();

  /**
   * Checks if the shape currently has a selected side.
   *
   * @return <CODE>true</CODE> if the shape has a selected side,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean hasSelectedBorder();

  /**
   * Applies an affine transformation (rotation, trnaslation, scale...) to the
   * shape
   *
   * @param aTransf The affine transformation to be applied
   */
  public void transform(java.awt.geom.AffineTransform aTransf);

  /**
   * Divides the shape taking (x,y) as the "significant" point of the division.
   *
   * @param x The horizontal co-ordinate of the point
   * @param y The vertical co-ordinate of the point
   * @return An array with the two shapes resulting of the division.
   */
  public EditableShape[] divide(double x, double y);

  /**
   * Checks if the shape is adjacent to a specific point.
   *
   * @param p The point to be checked
   * @return <CODE>true</CODE> if the shape is adjacent to the point,
   *         <CODE>false</CODE> otherwise
   */
  public boolean isAdjacentTo(java.awt.geom.Point2D p);

  /**
   * Returns the point of the shape that acts as "starting" point. It is needed to
   * establish the path that draws the shape.
   *
   * @return The starting point of the shape
   */
  public java.awt.geom.Point2D getInitialPoint();

  /**
   * Gets the point of the shape that is considered as "ending" point.
   *
   * @return The ending point of the shape.
   */
  public java.awt.geom.Point2D getEndPoint();

  /**
   * Gets an array with all the corners and other special points of the shape.
   *
   * @return The array of the points used to build the path that draws the shape.
   */
  public java.awt.geom.Point2D[] getBorders();

  /**
   * Makes a copy of the shape.
   *
   * @return The copy of the object
   */
  public Object clone();
}
