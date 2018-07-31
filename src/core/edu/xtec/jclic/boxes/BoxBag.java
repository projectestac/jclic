/*
 * File    : BoxBag.java
 * Created : 10-sep-2001 10:13
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

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.Constants;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 * A BoxBag is a class derived from {@link edu.xtec.jclic.boxes.AbstractBox} that contains a
 * collection of "boxes" (objects also derived from AbstractBox). The boxes are stores into a
 * protected {@link java.util.ArrayList}. The class implements methods to add, remove and retrieve
 * boxes, and to manage some of its properties like visibility, status, location and size.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.23
 */
public class BoxBag extends AbstractBox implements Cloneable, Resizable {

  protected ArrayList<AbstractBox> cells = new ArrayList<AbstractBox>(12);
  protected Rectangle2D preferredBounds = new Rectangle2D.Double();
  protected AbstractBox backgroundBox = null;

  /** Creates new ActiveBoxBag */
  public BoxBag(AbstractBox parent, JComponent container, BoxBase boxBase) {
    super(parent, container, boxBase);
    preferredBounds.setRect(getBounds());
  }

  @Override
  public Object clone() {
    BoxBag dBB = (BoxBag) super.clone();
    dBB.preferredBounds = (Rectangle2D) preferredBounds.clone();
    dBB.cells = new ArrayList<AbstractBox>();
    for (int i = 0; i < cells.size(); i++) dBB.cells.add((AbstractBox) getBox(i).clone());
    if (backgroundBox != null) {
      dBB.backgroundBox = (AbstractBox) backgroundBox.clone();
      dBB.backgroundBox.setParent(dBB);
    }
    return dBB;
  }

  @Override
  public void setContainer(JComponent newContainer) {
    super.setContainer(newContainer);
    // for(int i=0; i<cells.size(); i++)
    //    getBox(i).setContainer(newContainer);
    // if(backgroundBox!=null)
    //    backgroundBox.setContainer(newContainer);
  }

  @Override
  public void end() {
    for (int i = 0; i < cells.size(); i++) getBox(i).end();
    if (backgroundBox != null) {
      backgroundBox.end();
      backgroundBox = null;
    }
    cells.clear();
    super.end();
  }

  public Dimension getPreferredSize() {
    return preferredBounds.getBounds().getSize();
  }

  public Dimension getMinimumSize() {
    Dimension d = getPreferredSize();
    return new Dimension(
        Math.max(Constants.MIN_CELL_SIZE, d.width), Math.max(Constants.MIN_CELL_SIZE, d.height));
  }

  public Dimension getScaledSize(double scale) {
    Dimension d = getPreferredSize();
    return new Dimension((int) (scale * d.width), (int) (scale * d.height));
  }

  public void ensureCapacity(int n) {
    cells.ensureCapacity(n);
  }

  public void addBox(AbstractBox bx) {
    cells.add(bx);
    bx.setParent(this);
    if (cells.size() == 1) {
      super.setBounds(bx);
    } else {
      add(bx);
    }
    preferredBounds.setRect(getBounds());
  }

  public int boxIndex(Object bx) {
    return bx == null ? -1 : cells.indexOf(bx);
  }

  public AbstractBox getBox(int id) {
    return (id < 0 || id >= cells.size()) ? null : (AbstractBox) cells.get(id);
  }

  public AbstractBox getBackgroundBox() {
    return backgroundBox;
  }

  public void setBackgroundBox(AbstractBox bx) {
    backgroundBox = bx;
    if (backgroundBox != null) {
      add(backgroundBox);
      backgroundBox.setParent(this);
    }
    preferredBounds.setRect(getBounds());
  }

  public void recalcSize() {
    Rectangle2D r = new Rectangle2D.Double(x, y, 0, 0);
    if (backgroundBox != null) r.add(backgroundBox);
    for (int i = 0; i < cells.size(); i++) r.add((AbstractBox) (cells.get(i)));
    preferredBounds.setRect(r);
    x = r.getX();
    y = r.getY();
    width = r.getWidth();
    height = r.getHeight();
  }

  public int getNumCells() {
    return cells.size();
  }

  @Override
  public void setBorder(boolean newVal) {
    for (int i = 0; i < cells.size(); i++) getBox(i).setBorder(newVal);
  }

  @Override
  public void setVisible(boolean newVal) {
    for (int i = 0; i < cells.size(); i++) getBox(i).setVisible(newVal);
    // if(backgroundBox!=null)
    //    backgroundBox.setVisible(newVal);
  }

  @Override
  public void setAlternative(boolean newVal) {
    super.setAlternative(newVal);
    for (int i = 0; i < cells.size(); i++) getBox(i).setAlternative(newVal);
  }

  @Override
  public void setBoxBase(BoxBase setBb) {
    super.setBoxBase(setBb);
    // if(cells!=null)
    //    for(int i=0; i<cells.size(); i++)
    //        getBox(i).setBoxBase(setBb);
    // if(backgroundBox!=null)
    //    backgroundBox.setBoxBase(setBb);
  }

  @Override
  public void setBounds(Rectangle2D r) {

    // if(width>0 && height>0 && (x!=newX || y!=newY || width!=newWidth || height!=newHeight)){
    if (!r.isEmpty() && !r.equals(this)) {
      double scaleW = r.getWidth() / width;
      double scaleH = r.getHeight() / height;
      double dx = r.getX() - x;
      double dy = r.getY() - y;
      for (int i = 0; i < cells.size(); i++) {
        AbstractBox bx = getBox(i);
        Point2D.Double p = new Point2D.Double(bx.x - x, bx.y - y);
        bx.setBounds(
            dx + x + scaleW * p.x, dy + y + scaleH * p.y, scaleW * bx.width, scaleH * bx.height);
      }
      if (backgroundBox != null) {
        AbstractBox bx = backgroundBox;
        Point2D.Double p = new Point2D.Double(bx.x - x, bx.y - y);
        bx.setBounds(
            dx + x + scaleW * p.x, dy + y + scaleH * p.y, scaleW * bx.width, scaleH * bx.height);
      }
    }
    super.setBounds(r);
  }

  @Override
  public boolean update(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io) {

    if (isEmpty() || !isVisible() || isTemporaryHidden()) return false;
    if (!intersects(dirtyRegion)) return false;

    if (backgroundBox != null) backgroundBox.update(g2, dirtyRegion, io);

    AbstractBox bx;
    for (int i = 0; i < cells.size(); i++)
      if (!((bx = getBox(i)).isMarked())) bx.update(g2, dirtyRegion, io);
    for (int i = 0; i < cells.size(); i++)
      if ((bx = getBox(i)).isMarked()) bx.update(g2, dirtyRegion, io);
    return true;
  }

  public boolean updateContent(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io) {
    return true;
  }

  public AbstractBox findBox(Point2D p) {
    for (int i = cells.size() - 1; i >= 0; i--) {
      AbstractBox bx = getBox(i);
      if (bx.isVisible() && bx.contains(p)) return bx;
    }
    return null;
  }

  public int countInactiveCells() {
    int n = 0;
    for (int i = 0; i < cells.size(); i++) {
      if (getBox(i).isInactive()) n++;
    }
    return n;
  }

  public static Dimension layoutSingle(Dimension preferredMaxSize, Resizable rs, int margin) {

    // Avoid exceptions when rs is null
    if (rs == null) return preferredMaxSize;

    // optimal dimension
    Dimension d = rs.getPreferredSize();
    // minimal dimension
    Dimension minSize = rs.getMinimumSize();
    // maximal dimension
    Dimension maxSize = preferredMaxSize;
    // remove margins
    maxSize.width -= 2 * margin;
    maxSize.height -= 2 * margin;
    // correct maxSize if less than minSize
    if (minSize.width > maxSize.width || minSize.height > maxSize.height) {
      maxSize = minSize;
    }
    // compute scale factor
    double scale = 1;
    if (d.width > maxSize.width) {
      scale = (double) maxSize.width / d.width;
    }
    if ((scale * d.height) > maxSize.height) {
      scale = (double) maxSize.height / d.height;
    }
    // resize bg
    d = rs.getScaledSize(scale);
    rs.setBounds(margin, margin, d.width, d.height);
    // restore margins
    d.width += 2 * margin;
    d.height += 2 * margin;

    return d;
  }

  public static Dimension layoutDouble(
      Dimension desiredMaxSize, Resizable rsA, Resizable rsB, int boxGridPos, int margin) {
    // number of horizontally and vertically grids
    boolean isHLayout = false;
    int nbh = 1, nbv = 1;
    switch (boxGridPos) {
      case Activity.AB:
      case Activity.BA:
        nbh = 2;
        nbv = 1;
        isHLayout = true;
        break;
      case Activity.AUB:
      case Activity.BUA:
        nbh = 1;
        nbv = 2;
        isHLayout = false;
        break;
    }
    Rectangle2D ra = rsA.getBounds2D();
    Rectangle2D rb = rsB.getBounds2D();
    // optimal dimensions
    Dimension da = rsA.getPreferredSize();
    Dimension db = rsB.getPreferredSize();
    Dimension d =
        new Dimension(
            isHLayout ? da.width + db.width : Math.max(da.width, db.width),
            isHLayout ? Math.max(da.height, db.height) : da.height + db.height);
    // minimal dimensions
    Dimension minSizeA = rsA.getMinimumSize();
    Dimension minSizeB = rsB.getMinimumSize();
    Dimension minSize =
        new Dimension(
            isHLayout ? minSizeA.width + minSizeB.width : Math.max(minSizeA.width, minSizeB.width),
            isHLayout
                ? Math.max(minSizeA.height, minSizeB.height)
                : minSizeA.height + minSizeB.height);
    // maximal dimension
    Dimension maxSize = desiredMaxSize;
    // remove margins
    maxSize.width -= (1 + nbh) * margin;
    maxSize.height -= (1 + nbv) * margin;
    // correct maxSize if less than minSize
    if (minSize.width > maxSize.width || minSize.height > maxSize.height) {
      maxSize.setSize(minSize);
    }
    // compute scale factor
    double scale = 1;
    if (d.width > maxSize.width) {
      scale = (double) maxSize.width / d.width;
    }
    if ((scale * d.height) > maxSize.height) {
      scale = (double) maxSize.height / d.height;
    }
    // correct possible minimal infractions
    // ...
    // resize
    da = rsA.getScaledSize(scale);
    db = rsB.getScaledSize(scale);

    // margins to center one box relatove to the other
    int dah, dav, dbh, dbv;
    dah = db.width > da.width ? (db.width - da.width) / 2 : 0;
    dbh = da.width > db.width ? (da.width - db.width) / 2 : 0;
    dav = db.height > da.height ? (db.height - da.height) / 2 : 0;
    dbv = da.height > db.height ? (da.height - db.height) / 2 : 0;

    switch (boxGridPos) {
      case Activity.AB:
        rsA.setBounds(margin, margin + dav, da.width, da.height);
        rsB.setBounds(2 * margin + da.width, margin + dbv, db.width, db.height);
        break;
      case Activity.BA:
        rsB.setBounds(margin, margin + dbv, db.width, db.height);
        rsA.setBounds(2 * margin + db.width, margin + dav, da.width, da.height);
        break;
      case Activity.AUB:
        rsA.setBounds(margin + dah, margin, da.width, da.height);
        rsB.setBounds(margin + dbh, 2 * margin + da.height, db.width, db.height);
        break;
      case Activity.BUA:
        rsB.setBounds(margin + dbh, margin, db.width, db.height);
        rsA.setBounds(margin + dah, 2 * margin + db.height, da.width, da.height);
        break;
      default:
        rsA.setBounds(
            (int) (margin + scale * ra.getX()),
            (int) (margin + scale * ra.getY()),
            da.width,
            da.height);
        rsB.setBounds(
            (int) (margin + scale * rb.getX()),
            (int) (margin + scale * rb.getY()),
            da.width,
            da.height);
        break;
    }

    // recompute d adding margins
    Rectangle r = new Rectangle(rsA.getBounds());
    r.add(rsB.getBounds());
    d.width = r.width + 2 * margin;
    d.height = r.height + 2 * margin;

    return d;
  }
}
