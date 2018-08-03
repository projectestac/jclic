/*
 * File    : EditableShapeConstants.java
 * Created : 28-feb-2002 11:33
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

/**
 * @author allastar
 * @version
 */
public class EditableShapeConstants {
    public static int selectLength = 8;
    public static double scaleXFactor = 1.1;
    public static double scaleYFactor = 1.1;
    public static int gridWidth = -1; // -1->Without grid
    public static java.awt.Color selectedColor = java.awt.Color.blue;
    public static java.awt.Color defaultColor = java.awt.Color.black;
    public static java.awt.Color gridColor = java.awt.Color.lightGray;
    public static java.awt.Color selectingAreaColor = java.awt.Color.darkGray;
    public static java.awt.Color lightColor = java.awt.Color.lightGray;
    public static java.awt.Color BORDER_COLOR = java.awt.Color.black;
    public static java.awt.Color DRAWN_BORDER_COLOR = java.awt.Color.black;
    public static java.awt.Color CUT_COLOR = java.awt.Color.red;
    public static java.awt.Color ACTIVE_COLOR = new java.awt.Color(128, 128, 255); // When the polygon can be modified
    public static java.awt.Color SELECTED_BORDER_COLOR = java.awt.Color.orange; // When there is a point selected
    public static java.awt.Color movingColor = java.awt.Color.green;
    public static java.awt.Color inactiveFillColor = new java.awt.Color(240, 240, 240);
    public static java.awt.Color inactiveBorderColor = new java.awt.Color(100, 100, 100);
    public static boolean fillDrawn = true;
    public static boolean pointsOnGrid = false; // magnet
    public static boolean showDrawnPoints = true;
}
