/*
 * File    : ActiveBoxBag.java
 * Created : 13-dec-2000 12:09
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

import java.awt.geom.Point2D;
import javax.swing.JComponent;

/**
 * This class is a special case of {@link edu.xtec.jclic.boxes.BoxBag}, containing only {@link
 * edu.xtec.jclic.boxes.ActiveBox} objects. In addition to the members and methods of <CODE>BoxBag
 * </CODE>, it implements specific methods to deal with {@link
 * edu.xtec.jclic.boxes.ActiveBagContent} objects and with other specific members of {@link
 * edu.xtec.jclic.boxes.ActiveBox}, like its "ids" (<CODE>idOrder</CODE>, <CODE>idLoc</CODE> and
 * <CODE>idAss</CODE>).
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class ActiveBoxBag extends BoxBag implements Cloneable {

  /** Creates new ActiveBoxBag */
  public ActiveBoxBag(AbstractBox parent, JComponent container, BoxBase boxBase) {
    super(parent, container, boxBase);
  }

  public final void addActiveBox(ActiveBox bx) {
    bx.idLoc = cells.size();
    bx.idOrder = bx.idLoc;
    super.addBox(bx);
  }

  public ActiveBox getActiveBox(int idLoc) {
    return (ActiveBox) super.getBox(idLoc);
  }

  public ActiveBox getBackgroundActiveBox() {
    return (ActiveBox) getBackgroundBox();
  }

  public void setContent(ActiveBagContent abc) {
    setContent(abc, null);
  }

  public void setContent(ActiveBagContent abc, ActiveBagContent altAbc) {
    setContent(abc, altAbc, 0, 0, getNumCells());
  }

  public void setContent(
      ActiveBagContent abc, ActiveBagContent altAbc, int fromIndex, int toCell, int numCells) {
    ActiveBox bx;
    for (int i = 0; i < numCells; i++) {
      bx = getActiveBox(toCell + i);
      bx.setContent(abc, fromIndex + i);
      bx.setAlternative(false);
      if (altAbc != null) bx.setAltContent(altAbc, fromIndex + i);
    }
    if (abc.backgroundContent != null && (bx = getBackgroundActiveBox()) != null) {
      bx.setContent(abc.backgroundContent);
      if (/*abc.bb!=null && */ abc.bb != bx.getBoxBaseX()) bx.setBoxBase(abc.bb);
    }
  }

  public ActiveBox findActiveBox(Point2D p) {
    return (ActiveBox) super.findBox(p);
  }

  public void clearAll() {
    for (int i = 0; i < cells.size(); i++) getActiveBox(i).clear();
    if (backgroundBox != null) getBackgroundActiveBox().clear();
  }

  public int countCellsAtPlace() {
    int cellsAtPlace = 0;
    for (int i = 0; i < cells.size(); i++) if (getActiveBox(i).isAtPlace()) cellsAtPlace++;
    return cellsAtPlace;
  }

  public ActiveBox getActiveBoxWithIdLoc(int idLoc) {
    ActiveBox bx;
    for (int i = 0; i < cells.size(); i++) {
      if ((bx = getActiveBox(i)).idLoc == idLoc) return bx;
    }
    return null;
  }

  public boolean cellIsAtEquivalentPlace(ActiveBox bx, boolean checkCase) {
    return bx.isAtPlace() || bx.isEquivalent(getActiveBoxWithIdLoc(bx.idOrder), checkCase);
  }

  public int countCellsAtEquivalentPlace(boolean checkCase) {
    int cellsAtPlace = 0;
    for (int i = 0; i < cells.size(); i++) {
      if (cellIsAtEquivalentPlace(getActiveBox(i), checkCase)) cellsAtPlace++;
    }
    return cellsAtPlace;
  }

  public int countCellsWithIdAss(int idAss) {
    int n = 0;
    for (int i = 0; i < cells.size(); i++) {
      if (getActiveBox(i).idAss == idAss) n++;
    }
    return n;
  }

  @Override
  public int countInactiveCells() {
    int n = 0;
    for (int i = 0; i < cells.size(); i++) {
      if (getActiveBox(i).isInactive()) n++;
    }
    return n;
  }

  public void setDefaultIdAss() {
    for (int i = 0; i < cells.size(); i++) getActiveBox(i).setDefaultIdAss();
  }

  public void scrambleCells(int times, boolean fitInArea) {

    int nc = cells.size();
    if (nc >= 2) {
      Point2D[] pos = new Point2D[nc];
      int[] idLoc = new int[nc];
      for (int i = 0; i < nc; i++) {
        ActiveBox bx = getActiveBox(i);
        pos[i] = new Point2D.Double();
        pos[i].setLocation(bx.getLocation());
        idLoc[i] = bx.idLoc;
      }

      Point2D p = new Point2D.Double();
      int j;
      for (int i = 0; i < times; i++) {
        int r1 = (int) (Math.random() * nc);
        int r2 = (int) (Math.random() * nc);
        if (r1 != r2) {
          p.setLocation(pos[r1]);
          pos[r1].setLocation(pos[r2]);
          pos[r2].setLocation(p);
          j = idLoc[r1];
          idLoc[r1] = idLoc[r2];
          idLoc[r2] = j;
        }
      }

      double maxX = x + width;
      double maxY = y + height;
      for (int i = 0; i < nc; i++) {
        ActiveBox bx = getActiveBox(i);
        double px = pos[i].getX();
        double py = pos[i].getY();
        if (fitInArea) {
          px = Math.min(Math.max(px, x), maxX - bx.width);
          py = Math.min(Math.max(py, y), maxY - bx.height);
        }
        bx.setLocation(px, py);
        bx.idLoc = idLoc[i];
      }
    }
  }

  public void resetIds() {
    for (int i = 0; i < cells.size(); i++) {
      ActiveBox bx = (ActiveBox) cells.get(i);
      if (bx != null) {
        bx.idOrder = i;
        bx.idAss = i;
        bx.idLoc = i;
      }
    }
  }

  private static final int NOT_USED = -12345;

  public int getNextItem(int currentItem) {
    return getNextItem(currentItem, NOT_USED);
  }

  public int getNextItem(int currentItem, int idAssValid) {
    int i;
    for (i = currentItem + 1; i < cells.size(); i++) {
      ActiveBox bx = (ActiveBox) cells.get(i);
      if (bx == null) break;
      if (idAssValid != NOT_USED) {
        if (idAssValid == bx.idAss) break;
      } else if (bx.idAss >= 0) break;
    }
    return i;
  }
}
