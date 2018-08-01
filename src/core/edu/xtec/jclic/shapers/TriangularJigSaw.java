/*
 * File    : TriangularJigSaw.java
 * Created : 14-may-2001 13:51
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

package edu.xtec.jclic.shapers;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class TriangularJigSaw extends JigSaw {

  /** Creates new TriangularJigSaw */
  public TriangularJigSaw(int nx, int ny) {
    super(nx, ny);
  }

  @Override
  protected void hLine(ShapeData sd, int type, double x, double y, double w, double h, boolean inv) {
    int kx = inv ? -1 : 1;
    int ky = (type == 1 ? 1 : -1);

    if (type == 0) {
      sd.lineTo(x + w * kx, y);
    } else {
      double x0 = x + ((w - w * baseWidthFactor) / 2) * kx;
      double wb = w * baseWidthFactor * kx;
      sd.lineTo(x0, y);
      double hb = (h * toothHeightFactor) * ky;
      sd.lineTo(x0 + wb / 2, y + hb);
      sd.lineTo(x0 + wb, y);
      sd.lineTo(x + w * kx, y);
    }
  }

  @Override
  protected void vLine(ShapeData sd, int type, double x, double y, double w, double h, boolean inv) {
    int ky = inv ? -1 : 1;
    int kx = (type == 1 ? 1 : -1);

    if (type == 0) {
      sd.lineTo(x, y + h * ky);
    } else {
      double y0 = y + ((h - h * baseWidthFactor) / 2) * ky;
      double hb = h * baseWidthFactor * ky;
      sd.lineTo(x, y0);
      double wb = w * toothHeightFactor * kx;
      sd.lineTo(x + wb, y0 + hb / 2);
      sd.lineTo(x, y0 + hb);
      sd.lineTo(x, y + h * ky);
    }
  }
}
