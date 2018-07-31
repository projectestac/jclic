/*
 * File    : HolesMaker.java
 * Created : 22-oct-2001 12:00
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

import java.awt.Shape;
import java.awt.geom.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class HolesMaker {

  Holes h;
  int nx, ny;
  boolean[][] flagDone;
  int nIds;
  int[][] ids;
  int[] newIds;
  Rectangle2D.Double r;
  boolean skipOnes;
  boolean singleCells;

  public HolesMaker(
      int setNx, int setNy, int[] setIds, boolean useIds, boolean skipOnes, boolean singleCells) {
    this.skipOnes = skipOnes;
    this.singleCells = singleCells;
    nx = setNx;
    ny = setNy;
    h = new Holes(nx, ny);
    flagDone = new boolean[nx][ny];
    ids = new int[nx][ny];
    for (int y = 0; y < ny; y++)
      for (int x = 0; x < nx; x++) {
        flagDone[x][y] = false;
        ids[x][y] = setIds[y * nx + x];
      }

    nIds = 0;
    newIds = new int[nx * ny];
    h.scaleW = nx;
    h.scaleH = ny;
    r = new Rectangle2D.Double(0, 0, 1, 1);
    ShapeData[] shapeDataX = new ShapeData[nx * ny];
    int j = 0;
    for (int y = 0; y < ny; y++)
      for (int x = 0; x < nx; x++) {
        if (!flagDone[x][y] && ids[x][y] >= 0) {
          Area a = scan(null, x, y);
          Shape sh = a;
          if (a.isRectangular()) sh = a.getBounds2D();
          shapeDataX[j] = ShapeData.getShapeData(sh, null);
          shapeDataX[j].scaleTo(nx, ny);
          j++;
        }
      }
    if (!useIds) {
      int maxId = 0;
      for (int i = 0; i < j; i++) if (newIds[i] > maxId) maxId = newIds[i];
      int nCells = maxId + 1;
      h.nCells = nCells;
      h.shapeData = new ShapeData[nCells];
      for (int i = 0; i < nCells; i++) {
        int k;
        for (k = 0; k < j; k++) if (newIds[k] == i) break;
        if (k == j) {
          // should not happen!
          h.shapeData[i] = shapeDataX[0];
          System.err.println("HolesMaker error buliding shape for cell: " + i);
        } else h.shapeData[i] = shapeDataX[k];
      }
    } else {
      h.nCells = j;
      h.shapeData = new ShapeData[j];
      System.arraycopy(shapeDataX, 0, h.shapeData, 0, j);
    }
    h.nCols = nx;
    h.nRows = ny;
    h.enclosingShapeData = ShapeData.getShapeData(new Rectangle2D.Double(0, 0, 1, 1), null);
  }

  public Holes getShaper() {
    return h;
  }

  public int[] getIds() {
    int[] result = new int[nIds];
    System.arraycopy(newIds, 0, result, 0, nIds);
    return result;
  }

  protected Area scan(Area a, int xx, int y) {
    int id = ids[xx][y];
    for (int x = xx; x < nx; x++) {
      if (flagDone[x][y] || ids[x][y] != id) break;
      flagDone[x][y] = true;
      r.x = x;
      r.y = y;
      if (a == null) {
        a = new Area(r);
        newIds[nIds++] = id;
      } else a.add(new Area(r));

      if (singleCells || (skipOnes && id == 1)) break;

      if (x > 0 && !flagDone[x - 1][y] && ids[x - 1][y] == id) scan(a, x - 1, y);
      if (y > 0 && !flagDone[x][y - 1] && ids[x][y - 1] == id) scan(a, x, y - 1);
      if (y < (ny - 1) && !flagDone[x][y + 1] && ids[x][y + 1] == id) scan(a, x, y + 1);
    }
    return a;
  }
}
