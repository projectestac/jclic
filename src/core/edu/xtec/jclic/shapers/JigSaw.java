/*
 * File    : JigSaw.java
 * Created : 10-may-2001 18:31
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

import edu.xtec.util.JDomUtility;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class JigSaw extends Shaper {

  protected double baseWidthFactor;
  protected double toothHeightFactor;
  protected boolean randomLines;

  public JigSaw(int nx, int ny) {
    super(nx, ny);
    baseWidthFactor = getDefaultBaseWidthFactor();
    toothHeightFactor = getDefaultToothHeightFactor();
    randomLines = false;
  }

  protected int getBaseFactor() {
    return 50;
  }

  @Override
  public String getEditorPanelClassName() {
    return getClass().getPackage().getName() + ".JigSawEditorPanel";
  }

  protected double getDefaultBaseWidthFactor() {
    return 1f / 3;
  }

  protected double getDefaultToothHeightFactor() {
    return 1f / 6;
  }

  protected static final String BASE_WIDTH_FACTOR = "baseWidthFactor", TOOTH_HEIGHT_FACTOR = "toothHeightFactor",
      RANDOM_LINES = "randomLines";

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();
    if (baseWidthFactor != getDefaultBaseWidthFactor()) {
      e.setAttribute(BASE_WIDTH_FACTOR, Double.toString(baseWidthFactor));
    }
    if (toothHeightFactor != getDefaultToothHeightFactor()) {
      e.setAttribute(TOOTH_HEIGHT_FACTOR, Double.toString(toothHeightFactor));
    }
    if (randomLines)
      e.setAttribute(RANDOM_LINES, JDomUtility.boolString(randomLines));
    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    initiated = false;
    baseWidthFactor = JDomUtility.getDoubleAttr(e, BASE_WIDTH_FACTOR, baseWidthFactor);
    toothHeightFactor = JDomUtility.getDoubleAttr(e, TOOTH_HEIGHT_FACTOR, toothHeightFactor);
    randomLines = JDomUtility.getBoolAttr(e, RANDOM_LINES, randomLines);
  }

  protected void buildShapes() {

    int[][] hLineType = new int[nRows + 1][nCols + 1];
    int[][] vLineType = new int[nRows + 1][nCols + 1];
    for (int row = 0; row < nRows; row++) {
      for (int col = 0; col < nCols; col++) {
        if (row == 0) {
          hLineType[row][col] = 0;
        } else {
          hLineType[row][col] = 1 + ((randomLines ? (int) (Math.random() * 9) : row + col) % 2);
        }
        if (col == 0) {
          vLineType[row][col] = 0;
        } else {
          vLineType[row][col] = 1 + ((randomLines ? (int) (Math.random() * 9) : col + row + 1) % 2);
        }
        if (col == nCols - 1)
          vLineType[row][col + 1] = 0;
        if (row == nRows - 1)
          hLineType[row + 1][col] = 0;
      }
    }

    double w = WIDTH / nCols;
    double h = HEIGHT / nRows;

    for (int r = 0; r < nRows; r++) {
      for (int c = 0; c < nCols; c++) {
        double x = w * c;
        double y = h * r;
        ShapeData sd = shapeData[r * nCols + c];
        sd.moveTo(x, y);
        hLine(sd, hLineType[r][c], x + 0, y + 0, w, h, false);
        vLine(sd, vLineType[r][c + 1], x + w, y + 0, w, h, false);
        hLine(sd, hLineType[r + 1][c], x + w, y + h, w, h, true);
        vLine(sd, vLineType[r][c], x + 0, y + h, w, h, true);
        sd.closePath();
      }
    }
    initiated = true;
  }

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
      sd.lineTo(x0, y + hb);
      sd.lineTo(x0 + wb, y + hb);
      sd.lineTo(x0 + wb, y);
      sd.lineTo(x + w * kx, y);
    }
  }

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
      sd.lineTo(x + wb, y0);
      sd.lineTo(x + wb, y0 + hb);
      sd.lineTo(x, y0 + hb);
      sd.lineTo(x, y + h * ky);
    }
  }
}
