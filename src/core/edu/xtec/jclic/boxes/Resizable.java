/*
 * File    : Resizable.java
 * Created : 03-oct-2001 20:17
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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * This interface applies to all the objects that have "bounds" (location, witdh and height) and can
 * be resized or moved.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public interface Resizable {
  public Rectangle getBounds();

  public Rectangle2D getBounds2D();

  public Dimension getPreferredSize();

  public Dimension getMinimumSize();

  public Dimension getScaledSize(double scale);

  public void setBounds(Rectangle2D r);

  public void setBounds(double x, double y, double width, double height);
}
