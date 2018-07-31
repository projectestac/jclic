/*
 * File    : TripleColor.java
 * Created : 19-dec-2000 23:54
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

package edu.xtec.jclic.clic3;

import java.awt.Color;

/**
 * This class is just a data structure with three {@link java.awt.Color} objects, defining the
 * background, foreground and shadow colors used to draw cells, and a boolean that indicates if a
 * shadow should be painted under texts.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class TripleColor extends Object implements Cloneable {

  public Color backColor = Color.white;
  public Color textColor = Color.black;
  public Color shadowColor = Color.lightGray;
  public boolean shadow = false;

  public TripleColor(Color bak, Color text, Color shad, boolean sh) {
    backColor = bak;
    textColor = text;
    shadowColor = shad;
    shadow = sh;
  }

  public TripleColor() {}

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }
}
