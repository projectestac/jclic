/*
 * File    : Shaper.java
 * Created : 10-may-2001 16:26
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

import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Options;
import edu.xtec.util.TripleString;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public abstract class Shaper extends Object implements Cloneable, Domable {

  protected final double WIDTH = 1, HEIGHT = 1;
  protected static final String DELIM = "|";

  protected int nCols, nRows, nCells;
  protected ShapeData[] shapeData;
  protected boolean initiated = false;

  /** Creates new Shaper */
  public Shaper(int nx, int ny) {
    reset(nx, ny);
  }

  public void reset(int nCols, int nRows) {
    this.nCols = nCols;
    this.nRows = nRows;
    nCells = nRows * nCols;
    initiated = false;
    shapeData = new ShapeData[nCells];
    for (int i = 0; i < nCells; i++)
      shapeData[i] = new ShapeData();
  }

  public void reset() {
    reset(nCols, nRows);
  }

  public static final String ELEMENT_NAME = "shaper";
  public static final String COLS = "cols", ROWS = "rows";
  public static final String BASE_CLASS = "edu.xtec.jclic.shapers.", BASE_CLASS_TAG = "@";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(JDomUtility.CLASS, getClassName());
    e.setAttribute(COLS, Integer.toString(nCols));
    e.setAttribute(ROWS, Integer.toString(nRows));
    return e;
  }

  public String getClassName() {
    String result = getClass().getName();
    if (result.startsWith(BASE_CLASS))
      result = BASE_CLASS_TAG + result.substring(BASE_CLASS.length());
    return result;
  }

  public static Shaper getShaper(org.jdom.Element e) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    String className = JDomUtility.getClassName(e);
    int cw = JDomUtility.getIntAttr(e, COLS, 1);
    int ch = JDomUtility.getIntAttr(e, ROWS, 1);
    Shaper sh = createShaper(className, cw, ch);
    sh.setProperties(e, null);
    return sh;
  }

  public static Shaper createShaper(String className, int cw, int ch) throws Exception {

    if (className.startsWith(BASE_CLASS_TAG))
      className = BASE_CLASS + className.substring(1);

    Class<?> shaperClass;
    java.lang.reflect.Constructor con;
    Class[] cparams = { int.class, int.class };
    Object[] initArgs = { new Integer(cw), new Integer(ch) };
    shaperClass = Class.forName(className);
    con = shaperClass.getConstructor(cparams);
    return (Shaper) con.newInstance(initArgs);
  }

  protected abstract void buildShapes();

  public boolean rectangularShapes() {
    return false;
  }

  public String getEditorPanelClassName() {
    return null;
  }

  public Shaper edit(Component parent, Options options, Dimension dim, Image img, BoxBase bb) {
    Shaper result = null;
    if (getEditorPanelClassName() != null) {
      try {
        Class<?> cl = Class.forName(getEditorPanelClassName());
        java.lang.reflect.Method m = cl.getMethod("getShaper",
            new Class[] { Shaper.class, Component.class, Options.class, Dimension.class, Image.class, BoxBase.class });
        result = (Shaper) m.invoke(null, new Object[] { this, parent, options, dim, img, bb });
      } catch (Exception ex) {
        options.getMessages().showErrorWarning(parent, "edit_act_shaper_err", ex);
      }
    }
    return result;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
  }

  public Shape getShape(int n, Rectangle2D rect) {
    if (!initiated)
      buildShapes();
    if (n >= nCells || shapeData[n] == null)
      return null;
    return shapeData[n].getShape(rect);
  }

  public ShapeData getShapeData(int n) {
    return (n >= 0 && n < shapeData.length) ? shapeData[n] : null;
  }

  protected ShapeData getEnclosingShapeData() {
    ShapeData sh = new ShapeData();
    sh.moveTo(0, 0);
    sh.lineTo(1.0, 0);
    sh.lineTo(1.0, 1.0);
    sh.lineTo(0, 1.0);
    sh.closePath();
    return sh;
  }

  public boolean hasRemainder() {
    return false;
  }

  public Shape getRemainderShape(Rectangle2D rect) {
    if (!hasRemainder())
      return null;

    if (!initiated)
      buildShapes();

    GeneralPath gp = (GeneralPath) (getEnclosingShapeData().getShape(rect));
    gp.setWindingRule(GeneralPath.WIND_EVEN_ODD);
    for (int i = 0; i < nCells; i++) {
      if (shapeData[i] != null)
        gp.append(shapeData[i].getShape(rect), false);
    }
    return gp;
  }

  public int getNumRows() {
    return nRows;
  }

  public void setNumRows(int n) {
    if (n > 0 && n != nRows)
      reset(nCols, n);
  }

  public int getNumColumns() {
    return nCols;
  }

  public void setNumColumns(int n) {
    if (n > 0 && n != nCols)
      reset(nRows, n);
  }

  public int getNumCells() {
    return nCells;
  }

  public static final String SYSTEM_LIST = "shapers.listshapers";

  public static List getSystemShaperList(Options options) {
    List<TripleString> result = new ArrayList<TripleString>();
    try {
      result = TripleString.getTripleList(SYSTEM_LIST, options, false, true, true);
    } catch (Exception ex) {
      System.err.println("Error reading list of shapers!\n" + ex);
    }
    return result;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    Shaper clon = (Shaper) super.clone();
    clon.shapeData = (ShapeData[]) shapeData.clone();
    for (int i = 0; i < shapeData.length; i++) {
      if (shapeData[i] != null)
        clon.shapeData[i] = (ShapeData) shapeData[i].clone();
    }
    return clon;
  }
}
