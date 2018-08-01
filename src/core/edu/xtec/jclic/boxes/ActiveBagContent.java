/*
 * File    : ActiveBagContent.java
 * Created : 27-apr-2001 17:11
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
import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.jclic.bags.MediaBagElement;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.shapers.Rectangular;
import edu.xtec.jclic.shapers.Shaper;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * This class stores a collection of
 * {@link edu.xtec.jclic.boxes.ActiveBoxContent} objects, currently in a
 * {@link java.util.ArrayList}, and provides methods to manage it. The two main
 * members of <CODE>ActiveBagContent</CODE> are the
 * {@link edu.xtec.jclic.shapers.Shaper}, responsible of determining the
 * position and shape of each {@link edu.xtec.jclic.boxes.ActiveBox} based on
 * it, and the {@link edu.xtec.jclic.boxes.BoxBase}, that provides a common
 * visual style.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ActiveBagContent extends Object implements Domable {

  public Image img;
  public String imgName;
  public int ncw, nch;
  public double w, h;
  public boolean border;
  public BoxBase bb;
  protected Shaper shaper;
  protected ActiveBoxContent backgroundContent;

  protected ArrayList<ActiveBoxContent> activeBoxContentArray = new ArrayList<ActiveBoxContent>(12);
  public int defaultIdValue = -1;

  /** Creates new ActiveBagContent */
  private ActiveBagContent() {
  }

  public ActiveBagContent(int ncw, int nch) {
    this.ncw = Math.max(1, ncw);
    this.nch = Math.max(1, nch);
    w = Activity.DEFAULT_GRID_ELEMENT_SIZE;
    h = Activity.DEFAULT_GRID_ELEMENT_SIZE;
  }

  public static ActiveBagContent initNew(int ncw, int nch, int firstChar) {
    return initNew(ncw, nch, firstChar, false, false, 50, 30);
  };

  public static ActiveBagContent initNew(int ncw, int nch, int firstChar, boolean withIds, boolean withItems, int w,
      int h) {
    ActiveBagContent result = new ActiveBagContent(ncw, nch);
    result.w = w;
    result.h = h;
    result.setBoxBase(new BoxBase());
    result.border = true;
    char[] ch = null;
    if (firstChar > 0) {
      ch = new char[] { (char) firstChar };
    }
    for (int i = 0; i < nch; i++) {
      for (int j = 0; j < ncw; j++) {
        ActiveBoxContent ab = new ActiveBoxContent();
        if (ch != null) {
          ab.setTextContent("");
          ch[0]++;
        } else {
          ab.setTextContent("");
        }
        if (withIds) {
          if (withItems) {
            ab.id = j;
            ab.item = i;
          } else {
            ab.id = i * ncw + j;
          }
        }
        result.addActiveBoxContent(ab);
      }
    }
    return result;
  }

  public void checkCells() {
    int shaperCells = getShaper().getNumCells();
    int existingCells = activeBoxContentArray.size();
    if (existingCells > shaperCells) {
      while (activeBoxContentArray.size() > shaperCells) {
        activeBoxContentArray.remove(activeBoxContentArray.size() - 1);
      }
    } else if (shaperCells > existingCells) {
      for (int i = existingCells; i < shaperCells; i++) {
        ActiveBoxContent ab = getActiveBoxContent(i);
        ab.id = defaultIdValue;
      }
    }
  }

  public static final String ELEMENT_NAME = "cells";
  public static final String ROWS = "rows", COLUMNS = "columns", COLS = "cols";
  public static final String CELL_WIDTH = "cellWidth", CELL_HEIGHT = "cellHeight", BORDER = "border", IMAGE = "image",
      IDS = "ids";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    if (ncw > 0 || nch > 0) {
      e.setAttribute(ROWS, Integer.toString(nch));
      e.setAttribute(COLS, Integer.toString(ncw));
    }
    if (imgName == null && (w != 0 || h != 0)) {
      e.setAttribute(CELL_WIDTH, Double.toString(w));
      e.setAttribute(CELL_HEIGHT, Double.toString(h));
    }
    e.setAttribute(BORDER, JDomUtility.boolString(border));
    if (imgName != null) {
      e.setAttribute(IMAGE, imgName);
    }

    if (bb != null) {
      e.addContent(bb.getJDomElement());
    }

    if (shaper != null) {
      e.addContent(shaper.getJDomElement());
    }

    switch (testCellContents()) {
    case ActiveBoxContent.EMPTY_CELL:
      // write nothing
      break;

    case ActiveBoxContent.ONLY_ID:
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < activeBoxContentArray.size(); i++)
        sb.append(getActiveBoxContent(i).id).append(" ");
      e.addContent(new org.jdom.Element(IDS).addContent(sb.substring(0).trim()));
      break;

    default:
      for (int i = 0; i < activeBoxContentArray.size(); i++)
        e.addContent(getActiveBoxContent(i).getJDomElement());
    }
    return e;
  }

  public int testCellContents() {
    int result = ActiveBoxContent.EMPTY_CELL;
    for (int i = 0; i < activeBoxContentArray.size(); i++) {
      int r = getActiveBoxContent(i).testCellContents();
      if (r > result) {
        result = r;
        if (r > ActiveBoxContent.ONLY_ID)
          break;
      }
    }
    return result;
  }

  public static ActiveBagContent getActiveBagContent(org.jdom.Element e, MediaBag mediaBag) throws Exception {

    ActiveBagContent abc = new ActiveBagContent();
    abc.setProperties(e, mediaBag);
    return abc;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    MediaBag mediaBag = (MediaBag) aux;

    org.jdom.Element child;

    // Bug in JClic beta 1: "columns" is number of rows, and "rows" is number of
    // columns.
    // Corrected in beta 2: If "cols" is specified, "rows" are rows and "cols" are
    // columns.
    int k = JDomUtility.getIntAttr(e, COLS, -1);
    if (k >= 0) {
      ncw = k;
      nch = JDomUtility.getIntAttr(e, ROWS, nch);
    } else {
      ncw = JDomUtility.getIntAttr(e, ROWS, ncw);
      nch = JDomUtility.getIntAttr(e, COLUMNS, nch);
    }
    activeBoxContentArray.ensureCapacity(Math.max(12, ncw * nch));
    w = JDomUtility.getDoubleAttr(e, CELL_WIDTH, w);
    h = JDomUtility.getDoubleAttr(e, CELL_HEIGHT, h);
    border = JDomUtility.getBoolAttr(e, BORDER, border);
    imgName = FileSystem.stdFn(e.getAttributeValue(IMAGE));
    if ((child = e.getChild(BoxBase.ELEMENT_NAME)) != null)
      setBoxBase(BoxBase.getBoxBase(child));
    if ((child = e.getChild(Shaper.ELEMENT_NAME)) != null)
      setShaper(Shaper.getShaper(child));

    Iterator itr = e.getChildren(ActiveBoxContent.ELEMENT_NAME).iterator();
    if (itr.hasNext())
      while (itr.hasNext()) {
        child = ((org.jdom.Element) itr.next());
        addActiveBoxContent(ActiveBoxContent.getActiveBoxContent(child, mediaBag));
      }
    else {
      child = e.getChild(IDS);
      int[] v;
      if (child != null)
        if ((v = JDomUtility.stringToIntArray(child.getText())) != null)
          for (int i = 0; i < v.length; i++)
            getActiveBoxContent(i).id = v[i];
    }

    if (imgName != null) {
      if (mediaBag != null && mediaBag.getProject().getBridge() != null)
        setImgContent(mediaBag.getImageElement(imgName), true);
    }

    // Todo: check for empty-content
    int n = activeBoxContentArray.size();
    if (n > 0) {
      boolean empty = true;
      for (int i = 0; i < n; i++) {
        ActiveBoxContent bxc = getActiveBoxContent(i);
        if (bxc.id != -1 || bxc.item != -1 || !bxc.isEmpty()) {
          empty = false;
          break;
        }
      }
      if (empty) {
        for (int i = 0; i < n; i++)
          getActiveBoxContent(i).id = i;
      }
    }
  }

  public static void listReferences(org.jdom.Element e, Map<String, String> map) {
    String s = e.getAttributeValue(IMAGE);
    if (s != null)
      map.put(s, Constants.MEDIA_OBJECT);
    Iterator itr = e.getChildren(ActiveBoxContent.ELEMENT_NAME).iterator();
    while (itr.hasNext())
      ActiveBoxContent.listReferences((org.jdom.Element) itr.next(), map);
  }

  public Shaper getShaper() {
    if (shaper == null)
      setShaper(new Rectangular(ncw, nch));
    return shaper;
  }

  public void setShaper(Shaper sh) {
    shaper = sh;
  }

  public void addActiveBoxContent(ActiveBoxContent ab) {
    activeBoxContentArray.add(ab);
    if (ncw == 0 || nch == 0) {
      ncw = 1;
      nch = 1;
    }
  }

  public ActiveBoxContent getActiveBoxContent(int i) {
    if (i >= activeBoxContentArray.size()) {
      for (int j = activeBoxContentArray.size(); j <= i; j++)
        activeBoxContentArray.add(new ActiveBoxContent());
    }
    return (ActiveBoxContent) activeBoxContentArray.get(i);
  }

  public ActiveBoxContent getActiveBoxContentWith(int id, int item) {
    ActiveBoxContent result = null;
    for (int i = 0; i < activeBoxContentArray.size(); i++) {
      ActiveBoxContent abxcnt = (ActiveBoxContent) activeBoxContentArray.get(i);
      if (abxcnt.id == id && abxcnt.item == item) {
        result = abxcnt;
        break;
      }
    }
    return result;
  }

  public void deleteActiveBoxContentWith(int id, int item) {
    ActiveBoxContent abc = getActiveBoxContentWith(id, item);
    if (abc != null) {
      activeBoxContentArray.remove(abc);
      for (int i = 0; i < activeBoxContentArray.size(); i++) {
        abc = (ActiveBoxContent) activeBoxContentArray.get(i);
        if (abc.id == id && abc.item > item)
          abc.item--;
      }
    }
  }

  public void insertActiveBoxContentWith(int id, int item) {
    ActiveBoxContent abc = getActiveBoxContentWith(id, item);
    int index = activeBoxContentArray.indexOf(abc);
    for (int i = 0; i < activeBoxContentArray.size(); i++) {
      abc = (ActiveBoxContent) activeBoxContentArray.get(i);
      if (abc.id == id && abc.item >= item)
        abc.item++;
    }
    abc = new ActiveBoxContent();
    abc.id = id;
    abc.item = item;
    activeBoxContentArray.add(index + 1, abc);
  }

  public int indexOf(ActiveBoxContent cnt) {
    int result = -1;
    for (int i = 0; i < activeBoxContentArray.size(); i++) {
      if (activeBoxContentArray.get(i) == cnt) {
        result = i;
        break;
      }
    }
    return result;
  }

  public void setActiveBoxContentAt(ActiveBoxContent ab, int index) {
    if (index >= 0) {
      getActiveBoxContent(index);
      activeBoxContentArray.set(index, ab);
    }
  }

  public void setImgContent(MediaBagElement mbe, boolean roundSizes) throws Exception {
    setImgContent(mbe, getShaper(), roundSizes);
  }

  public void setImgContent(MediaBagElement mbe, Shaper sh) throws Exception {
    setImgContent(mbe, sh, false);
  }

  public void setImgContent(MediaBagElement mbe, Shaper sh, boolean roundSizes) throws Exception {
    setShaper(sh);
    ncw = shaper.getNumColumns();
    nch = shaper.getNumRows();
    if (mbe != null) {
      img = mbe.getImage();
      imgName = mbe.getName();
      w = -1;
      h = -1;
      while (true) {
        w = ((double) img.getWidth(null)) / ncw;
        h = ((double) img.getHeight(null)) / nch;
        if (w >= 0 && h >= 0)
          break;
        Thread.sleep(50);
      }
      if (roundSizes) {
        w = (double) ((int) w);
        h = (double) ((int) h);
      }

      if (w < 1 || h < 1)
        throw new Exception("Invalid image!");
    } else {
      img = null;
      imgName = null;
      w = Math.max(w, 10);
      h = Math.max(h, 10);
    }

    Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w * ncw, h * nch);
    activeBoxContentArray.ensureCapacity(shaper.getNumCells());
    for (int i = 0; i < shaper.getNumCells(); i++) {
      getActiveBoxContent(i).setImgContent(img, shaper.getShape(i, r));
    }
    if (shaper.hasRemainder()) {
      backgroundContent = new ActiveBoxContent();
      backgroundContent.setImgContent(img, shaper.getRemainderShape(r));
    }
  }

  public void setTextContent(String[] txt, int setNcw, int setNch) {
    ncw = Math.max(1, setNcw);
    nch = Math.max(1, setNch);
    int n = ncw * nch;
    activeBoxContentArray.ensureCapacity(n);
    for (int i = 0; i < n; i++) {
      getActiveBoxContent(i).setTextContent(((i >= txt.length || txt[i] == null) ? "" : txt[i]));
    }
  }

  public boolean hasImg() {
    return img != null;
  }

  public double getTotalWidth() {
    return w * ncw;
  }

  public double getTotalHeight() {
    return h * nch;
  }

  public int getNumCells() {
    return activeBoxContentArray.size();
  }

  public boolean isEmpty() {
    return activeBoxContentArray.isEmpty();
  }

  public void prepareMedia(PlayStation ps) {
    for (int i = 0; i < activeBoxContentArray.size(); i++) {
      getActiveBoxContent(i).prepareMedia(ps);
    }
    if (img != null && ps.getOptions().getBoolean(Constants.PRE_DRAW_IMAGES)) {
      Graphics g = ps.getComponent().getGraphics();
      g.drawImage(img, 0, 0, 0, 0, ps.getComponent());
    }
  }

  public void setBoxBase(BoxBase boxBase) {
    bb = boxBase;
  }

  public void setIds(int[] ids) {
    for (int i = 0; i < activeBoxContentArray.size(); i++)
      if (i < ids.length)
        getActiveBoxContent(i).id = ids[i];
  }

  public void setAllIdsTo(int id) {
    for (int i = 0; i < activeBoxContentArray.size(); i++)
      getActiveBoxContent(i).id = id;
  }

  public void avoidAllIdsNull(int maxId) {
    boolean allIdsNull = true;
    int numCells = activeBoxContentArray.size();
    for (int i = 0; i < numCells; i++) {
      if (getActiveBoxContent(i).id != -1) {
        allIdsNull = false;
        break;
      }
    }
    if (allIdsNull) {
      maxId = Math.max(1, maxId);
      for (int i = 0; i < numCells; i++) {
        getActiveBoxContent(i).id = i % maxId;
      }
    }
  }

  public void copyStyleTo(ActiveBagContent abc) {
    if (abc != null) {
      abc.setBoxBase(bb);
      abc.border = border;
    }
  }
}
