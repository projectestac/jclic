/*
 * File    : ClassicJigSaw.java
 * Created : 13-may-2001 20:11
 * By      : fbusquets
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
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class ClassicJigSaw extends JigSaw {

  /** Creates new ClassicJigSaw */
  public ClassicJigSaw(int nx, int ny) {
    super(nx, ny);
  }

  @Override
  protected double getDefaultBaseWidthFactor() {
    return 3f / 4;
  }

  @Override
  protected double getDefaultToothHeightFactor() {
    return 3f / 5;
  }

  @Override
  protected int getBaseFactor() {
    return 85;
  }

  @Override
  protected void hLine(ShapeData sd, int type, double x, double y, double w, double h, boolean inv) {
    int kx = inv ? -1 : 1;
    int ky = (type == 1 ? 1 : -1);

    if (type == 0) {
      sd.lineTo(x + w * kx, y);
    } else {
      double x0 = x + ((w - w * baseWidthFactor) / 2) * kx;
      double wb = ((w * baseWidthFactor) / 12) * kx;
      sd.lineTo(x0, y);
      double hb = ((h * toothHeightFactor) * ky) / 8;
      sd.cubicTo(x0 + 4 * wb, y, x0 + 6 * wb, y - hb, x0 + 4 * wb, y - 3 * hb);
      sd.cubicTo(x0 + 2 * wb, y - 5 * hb, x0 + 10 * wb, y - 5 * hb, x0 + 8 * wb, y - 3 * hb);
      sd.cubicTo(x0 + 6 * wb, y - 1 * hb, x0 + 8 * wb, y, x0 + 12 * wb, y);
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
      double hb = ((h * baseWidthFactor) / 12) * ky;
      sd.lineTo(x, y0);
      double wb = ((w * toothHeightFactor) * kx) / 8;
      sd.cubicTo(x, y0 + 4 * hb, x - wb, y0 + 6 * hb, x - 3 * wb, y0 + 4 * hb);
      sd.cubicTo(x - 5 * wb, y0 + 2 * hb, x - 5 * wb, y0 + 10 * hb, x - 3 * wb, y0 + 8 * hb);
      sd.cubicTo(x - 1 * wb, y0 + 6 * hb, x, y0 + 8 * hb, x, y0 + 12 * hb);
      sd.lineTo(x, y + h * ky);
    }
  }
}
