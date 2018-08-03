/*
 * File    : JDomUtility.java
 * Created : 14-jun-2001 17:25
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
package edu.xtec.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class JDomUtility {

  private static org.jdom.input.SAXBuilder saxBuilder;
  private static org.jdom.output.XMLOutputter outputter;

  public static final String ID = "id",
      IMAGE = "image",
      NAME = "name",
      TYPE = "type",
      BGCOLOR = "bgcolor",
      FORECOLOR = "forecolor",
      MARGIN = "margin",
      BORDER = "border",
      POSITION = "position",
      X = "x",
      Y = "y",
      P = "p",
      CLASS = "class";

  private JDomUtility() {}

  public static boolean addGenericAttribute(org.jdom.Element e, String key, Object v) {
    if (v == null) {
      return false;
    }

    Class<?> cl = v.getClass();
    if (e.getAttribute(key) != null) {
      return false;
    }
    if (cl.isArray()) {
      return false;
    }

    if (cl.isAssignableFrom(String.class)
        || cl.isAssignableFrom(Integer.class)
        || cl.isAssignableFrom(Float.class)
        || cl.isAssignableFrom(Double.class)
        || cl.isAssignableFrom(Boolean.class)) {
      e.setAttribute(key, v.toString());
    } else if (cl.isAssignableFrom(Color.class)) {
      e.setAttribute(key, colorToString((Color) v));
    } else {
      return false;
    }

    return true;
  }

  // ----------------------------------------------------------------------
  // Font conversion functions
  // ----------------------------------------------------------------------
  public static final String FONT = "font";
  private static final String FAMILY = "family", SIZE = "size", BOLD = "bold", ITALIC = "italic";

  public static org.jdom.Element fontToElement(java.awt.Font font) {
    org.jdom.Element e = new org.jdom.Element(FONT);
    e.setAttribute(FAMILY, font.getFamily());
    e.setAttribute(SIZE, Integer.toString(font.getSize()));
    if (font.isBold()) {
      e.setAttribute(BOLD, boolString(font.isBold()));
    }
    if (font.isItalic()) {
      e.setAttribute(ITALIC, boolString(font.isItalic()));
    }
    return e;
  }

  public static Font elementToFont(org.jdom.Element e) throws Exception {
    checkName(e, FONT);
    String family = getStringAttr(e, FAMILY, "default", false);
    int size = getIntAttr(e, SIZE, 12);
    int style =
        (getBoolAttr(e, BOLD, false) ? Font.BOLD : 0)
            | (getBoolAttr(e, ITALIC, false) ? Font.ITALIC : 0);
    return FontCheck.getValidFont(family, style, size);
  }

  // ----------------------------------------------------------------------
  // Color conversion functions
  // ----------------------------------------------------------------------
  public static String colorToString(Color color) {
    // be careful with alpha : high bits
    String s = Long.toHexString(0x100000000L | color.getRGB()).toUpperCase();
    s = s.substring(s.length() - (color.getAlpha() == 0xFF ? 6 : 8));
    return "0x" + s;
  }

  public static final String[] HTML_COLOR_NAMES = {
    "Black", "Silver", "Gray", "White",
    "Maroon", "Red", "Purple", "Fuchsia",
    "Green", "Lime", "Olive", "Yellow",
    "Navy", "Blue", "Teal", "Aqua",
    "Pink", "Orange", "DarkGray"
  };

  public static final Color[] HTML_COLORS = {
    Color.black,
    Color.lightGray,
    Color.gray,
    Color.white,
    new Color(128, 0, 0),
    Color.red,
    new Color(128, 0, 128),
    Color.magenta,
    new Color(0, 128, 0),
    Color.green,
    new Color(128, 128, 0),
    Color.yellow,
    new Color(0, 0, 128),
    Color.blue,
    new Color(0, 128, 128),
    Color.cyan,
    Color.pink,
    Color.orange,
    Color.darkGray
  };

  public static Color stringToColor(String s) throws Exception {
    // Color c=Color.black;
    Color color = null;
    if (s.startsWith("0x")) {
      // c=Color.decode(s);
      long v = Long.decode(s).longValue();
      color = new Color((int) v, s.length() > 8 || v >= 0x1000000);
    } else {
      for (int i = 0; i < HTML_COLOR_NAMES.length; i++) {
        if (HTML_COLOR_NAMES[i].equalsIgnoreCase(s)) {
          color = HTML_COLORS[i];
          break;
        }
      }
    }
    if (color == null) {
      throw new Exception("Invalid color: " + s);
    }
    return color;
  }

  public static Color getColorAttr(org.jdom.Element e, String attr, Color defaultValue)
      throws Exception {
    String s = e.getAttributeValue(attr);
    return s == null ? defaultValue : stringToColor(s);
  }

  // ----------------------------------------------------------------------
  // conversion functions for awt objects
  // ----------------------------------------------------------------------
  public static final String LEFT = "left",
      RIGHT = "right",
      TOP = "top",
      BOTTOM = "bottom",
      WIDTH = "width",
      HEIGHT = "height",
      UP = "up",
      DOWN = "down";

  public static org.jdom.Element getChildWithId(org.jdom.Element e, String name, String id) {
    org.jdom.Element result;
    if (e != null && id != null && name != null) {
      Iterator it = e.getChildren(name).iterator();
      while (it.hasNext()) {
        result = (org.jdom.Element) it.next();
        if (id.equals(result.getAttributeValue(ID))) {
          return result;
        }
      }
    }
    return null;
  }

  public static final String RECTANGLE = "rectangle";

  public static Rectangle getRectangle(org.jdom.Element e, String id, Rectangle defaultValue) {
    if (id != null) {
      e = getChildWithId(e, RECTANGLE, id);
    } else if (e != null && !e.getName().equals(RECTANGLE)) {
      e = e.getChild(RECTANGLE);
    }
    if (e == null) {
      return defaultValue;
    }
    Rectangle r = (defaultValue == null ? new Rectangle() : new Rectangle(defaultValue));
    r.setBounds(
        getIntAttr(e, LEFT, r.x),
        getIntAttr(e, TOP, r.y),
        getIntAttr(e, WIDTH, r.width),
        getIntAttr(e, HEIGHT, r.height));
    return r;
  }

  public static final String POINT = "point";

  public static Point getPoint(org.jdom.Element e, String id, Point defaultValue) {
    if (id != null) {
      e = getChildWithId(e, POINT, id);
    } else if (e != null && !e.getName().equals(POINT)) {
      e = e.getChild(POINT);
    }
    if (e == null) {
      return defaultValue;
    }
    Point p = (defaultValue == null ? new Point() : new Point(defaultValue));
    p.setLocation(getIntAttr(e, LEFT, p.x), getIntAttr(e, TOP, p.y));
    return p;
  }

  public static final String DIMENSION = "dimension";

  public static Dimension getDimension(org.jdom.Element e, String id, Dimension defaultValue) {
    if (id != null) {
      e = getChildWithId(e, DIMENSION, id);
    } else if (e != null && !e.getName().equals(DIMENSION)) {
      e = e.getChild(DIMENSION);
    }
    if (e == null) {
      return defaultValue;
    }
    Dimension d = (defaultValue == null ? new Dimension() : new Dimension(defaultValue));
    d.setSize(getIntAttr(e, WIDTH, d.width), getIntAttr(e, HEIGHT, d.height));
    return d;
  }

  public static final String OFFSET = "offset";

  public static Point getOffset(org.jdom.Element e, String id, Point defaultValue) {
    if (id != null) {
      e = getChildWithId(e, OFFSET, id);
    } else if (e != null && !e.getName().equals(OFFSET)) {
      e = e.getChild(OFFSET);
    }
    if (e == null) {
      return defaultValue;
    }
    Point p = (defaultValue == null ? new Point() : new Point(defaultValue));
    p.setLocation(getIntAttr(e, RIGHT, p.x), getIntAttr(e, DOWN, p.y));
    return p;
  }

  public static final String COLOR = "color", VALUE = "value";

  public static Color getColorByPoint(
      org.jdom.Element e, String id, java.awt.image.BufferedImage img, Color defaultValue)
      throws Exception {
    if (id != null) {
      e = getChildWithId(e, COLOR, id);
    } else if (e != null && !e.getName().equals(COLOR)) {
      e = e.getChild(COLOR);
    }
    if (e == null) {
      return defaultValue;
    }
    Color result = getColorAttr(e, VALUE, defaultValue);
    if (img != null) {
      org.jdom.Element child = e.getChild(POINT);
      if (child != null) {
        Point pt = getPoint(child, null, new Point());
        result = new Color(img.getRGB(pt.x, pt.y));
      }
    }
    return result;
  }

  public static final String DIRECTION = "direction";
  public static final int DIRECTION_UP = 0,
      DIRECTION_DOWN = 1,
      DIRECTION_LEFT = 2,
      DIRECTION_RIGHT = 3;
  public static final String[] directionName = {UP, DOWN, LEFT, RIGHT};

  public static int getDirection(org.jdom.Element e, int defaultValue) throws Exception {
    if (e == null) {
      return defaultValue;
    }
    return getStrIndexAttr(e, DIRECTION, directionName, defaultValue);
  }

  public static final String ALIGNMENT = "alignment";
  public static final String HALIGN = "hAlign", VALIGN = "vAlign";
  public static final int ALIGN_LEFT = 0,
      ALIGN_TOP = 0,
      ALIGN_MIDDLE = 1,
      ALIGN_RIGHT = 2,
      ALIGN_BOTTOM = 2;
  public static final String[] hAlignName = {"left", "middle", "right"};
  public static final String[] vAlignName = {"top", "middle", "bottom"};
  public static final int[] DEFAULT_ALIGNMENT = {ALIGN_MIDDLE, ALIGN_MIDDLE};

  public static int getHAlign(org.jdom.Element e, int defaultValue) throws Exception {
    if (e == null) {
      return defaultValue;
    }
    return getStrIndexAttr(e, HALIGN, hAlignName, defaultValue);
  }

  public static int getVAlign(org.jdom.Element e, int defaultValue) throws Exception {
    if (e == null) {
      return defaultValue;
    }
    return getStrIndexAttr(e, VALIGN, vAlignName, defaultValue);
  }

  public static int[] getAlignment(org.jdom.Element e, String id, int[] defaultValue)
      throws Exception {
    if (defaultValue == null) {
      defaultValue = DEFAULT_ALIGNMENT;
    }
    if (id != null) {
      e = getChildWithId(e, ALIGNMENT, id);
    } else if (e != null && !e.getName().equals(ALIGNMENT)) {
      e = e.getChild(ALIGNMENT);
    }
    if (e == null) {
      return defaultValue;
    }
    int[] al = new int[2];
    al[0] = getHAlign(e, defaultValue == null ? defaultValue[0] : ALIGN_MIDDLE);
    al[1] = getVAlign(e, defaultValue == null ? defaultValue[1] : ALIGN_MIDDLE);
    return al;
  }

  public static int[] getAlignProp(org.jdom.Element e, String id, int[] defaultValue)
      throws Exception {
    int[] result = new int[2];
    if (defaultValue == null) {
      defaultValue = DEFAULT_ALIGNMENT;
    }
    result[0] = defaultValue[0];
    result[1] = defaultValue[1];
    if (id != null) {
      String s = e.getAttributeValue(id);
      if (s != null && s.length() > 0) {
        StringTokenizer st = new StringTokenizer(s, ",");
        if (st.hasMoreTokens()) {
          result[0] = getStrIndexAttr(st.nextToken(), hAlignName, defaultValue[0]);
        }
        if (st.hasMoreTokens()) {
          result[1] = getStrIndexAttr(st.nextToken(), vAlignName, defaultValue[1]);
        }
      }
    }
    return result;
  }

  public static void setAlignProp(
      org.jdom.Element e, String id, int[] align, boolean omitIfDefault) {
    if (e != null && id != null && align != null && align.length == 2) {
      if (!omitIfDefault || !isDefaultAlign(align)) {
        e.setAttribute(id, hAlignName[align[0]] + "," + vAlignName[align[1]]);
      }
    }
  }

  public static boolean isDefaultAlign(int[] align) {
    return (align != null && align[0] == ALIGN_MIDDLE && align[1] == ALIGN_MIDDLE);
  }

  // ----------------------------------------------------------------------
  // conversion functions for paragraphs of plain text
  // ----------------------------------------------------------------------
  public static org.jdom.Element addParagraphs(
      org.jdom.Element parent, String childName, String text) {
    org.jdom.Element result = null;
    if (text != null) {
      result = new org.jdom.Element(childName);
      setParagraphs(result, text);
      parent.addContent(result);
    }
    return result;
  }

  public static void setParagraphs(org.jdom.Element e, String text) {
    if (text != null) {
      StringTokenizer st = new StringTokenizer(text, "\n");
      while (st.hasMoreTokens()) {
        e.addContent(new org.jdom.Element(P).setText(st.nextToken()));
      }
    }
  }

  public static String getParagraphs(org.jdom.Element e) {
    StringBuilder sb = null;
    if (e != null) {
      java.util.Iterator itr = e.getChildren(P).iterator();
      while (itr.hasNext()) {
        String s = ((org.jdom.Element) itr.next()).getText();
        if (sb == null) {
          sb = new StringBuilder(s);
        } else {
          sb.append("\n").append(s);
        }
      }
    }
    return (sb == null ? null : sb.substring(0));
  }

  // ----------------------------------------------------------------------
  // conversion functions for arrays
  // ----------------------------------------------------------------------
  public static String intArrayToString(int[] v) {
    return intArrayToString(v, v.length);
  }

  public static String intArrayToString(int[] v, int numElements) {
    StringBuilder sb = new StringBuilder(numElements * 4);
    for (int i = 0; i < numElements; i++) {
      sb.append(v[i]).append(' ');
    }
    return sb.substring(0).trim();
  }

  public static int[] stringToIntArray(String s) throws Exception {
    StringTokenizer st = new StringTokenizer(s, " ");
    int numTokens = st.countTokens();
    int[] result = new int[numTokens];
    for (int i = 0; i < numTokens; i++) {
      result[i] = Integer.parseInt(st.nextToken());
    }
    return result;
  }

  // ----------------------------------------------------------------------
  // primitive types conversion functions
  // ----------------------------------------------------------------------
  // int
  public static int getIntAttr(org.jdom.Element e, String attr, int defaultValue) {
    String s = e.getAttributeValue(attr);
    int result = defaultValue;
    if (s != null) {
      result = Integer.parseInt(s);
    }
    return result;
  }

  // long
  public static long getLongAttr(org.jdom.Element e, String attr, long defaultValue) {
    String s = e.getAttributeValue(attr);
    long result = defaultValue;
    if (s != null) {
      result = Long.parseLong(s);
    }
    return result;
  }

  // triple state
  public static final String[] BOOL_STR = {"false", "true", "default"};
  public static final int FALSE = 0, TRUE = 1, DEFAULT = 2;

  public static String triStateString(int state) {
    if (state < 0 || state >= DEFAULT) {
      state = DEFAULT;
    }
    return BOOL_STR[state];
  }

  public static int getTriStateAttr(org.jdom.Element e, String attr, int defaultValue)
      throws Exception {
    return getStrIndexAttr(e, attr, BOOL_STR, defaultValue);
  }

  public static boolean checkTriState(int v) {
    return v >= FALSE && v <= DEFAULT;
  }

  // boolean and Boolean
  public static String boolString(boolean value) {
    return BOOL_STR[value ? TRUE : FALSE];
  }

  public static boolean getBoolAttr(org.jdom.Element e, String attr, boolean defaultValue) {
    String s = e.getAttributeValue(attr);
    boolean result = defaultValue;
    if (s != null) {
      if (s.equalsIgnoreCase(BOOL_STR[TRUE])) {
        result = true;
      } else if (s.equalsIgnoreCase(BOOL_STR[FALSE])) {
        result = false;
      } else {
        throw new NumberFormatException("invalid boolean: " + s);
      }
    }
    return result;
  }

  public static Boolean getBooleanAttr(org.jdom.Element e, String attr, Boolean defaultValue) {
    String s = e.getAttributeValue(attr);
    Boolean result = defaultValue;
    if (s != null) {
      if (s.equalsIgnoreCase(BOOL_STR[TRUE]) || s.equalsIgnoreCase(BOOL_STR[FALSE])) {
        result = Boolean.valueOf(s);
      } else {
        throw new NumberFormatException("invalid boolean: " + s);
      }
    }
    return result;
  }

  // float
  public static float getFloatAttr(org.jdom.Element e, String attr, float defaultValue) {
    String s = e.getAttributeValue(attr);
    float result = defaultValue;
    if (s != null) {
      result = Float.parseFloat(s);
    }
    return result;
  }

  // double
  public static double getDoubleAttr(org.jdom.Element e, String attr, double defaultValue) {
    String s = e.getAttributeValue(attr);
    double result = defaultValue;
    if (s != null) {
      result = Double.parseDouble(s);
    }
    return result;
  }

  // ----------------------------------------------------------------------
  // conversion functions for basic classes
  // ----------------------------------------------------------------------
  // String
  public static String getStringAttr(
      org.jdom.Element e, String attr, String defaultValue, boolean allowEmpty) {
    String s = e.getAttributeValue(attr);
    String result = defaultValue;
    if (s != null && (allowEmpty || s.length() > 0)) {
      result = s;
    }
    return result;
  }

  public static void setStringAttr(
      org.jdom.Element e, String key, String value, boolean allowEmpty) {
    String v = allowEmpty && (value == null || value.length() == 0) ? "" : value;
    if (key != null && v != null) {
      e.setAttribute(key, v);
    }
  }

  // String array index
  public static int getStrIndexAttr(
      org.jdom.Element e, String attr, String[] values, int defaultValue) throws Exception {
    return getStrIndexAttr(e.getAttributeValue(attr), values, defaultValue);
  }

  public static int getStrIndexAttr(String s, String[] values, int defaultValue) throws Exception {
    int result = defaultValue;
    if (s != null && s.length() > 0) {
      for (result = 0; result < values.length; result++) {
        if (s.equalsIgnoreCase(values[result])) {
          break;
        }
      }
      if (result == values.length) {
        throw new Exception("Unknown value: " + s);
      }
    }
    return result;
  }

  // Dimension
  private static final int rare = -18634527;

  public static Dimension getDimensionAttr(
      org.jdom.Element e, String atrW, String atrH, Dimension defaultValue) {
    Dimension result = defaultValue;
    int w = getIntAttr(e, atrW, rare);
    int h = getIntAttr(e, atrH, rare);
    if (w != rare && h != rare) {
      result = new Dimension(w, h);
    }
    return result;
  }

  // Point
  public static Point getPointAttr(
      org.jdom.Element e, String atrX, String atrY, Point defaultValue) {
    Point result = defaultValue;
    int x = getIntAttr(e, atrX, rare);
    int y = getIntAttr(e, atrY, rare);
    if (x != rare && y != rare) {
      result = new Point(x, y);
    }
    return result;
  }

  // Date
  public static String dateToStringShortUS(Date date) {
    return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(date);
  }

  public static Date getDateAttrShortUS(org.jdom.Element e, String attr, Date defaultValue)
      throws Exception {
    Date result = defaultValue;
    String s;
    if ((s = getStringAttr(e, attr, null, false)) != null) {
      result = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse(s);
    }
    return result;
  }

  public static final String NEWLINE = "\n", BLANK = "";

  public static void clearNewLineElements(org.jdom.Element e) {
    if (e != null) {
      List<org.jdom.Text> toRemove = null;
      boolean hasChildren = !e.getChildren().isEmpty();
      List content = e.getContent();
      for (Object o : content) {
        if (o instanceof org.jdom.Element) {
          clearNewLineElements((org.jdom.Element) o);
        } else if (hasChildren && o instanceof org.jdom.Text) {
          if (toRemove == null) {
            toRemove = new ArrayList<org.jdom.Text>();
          }
          toRemove.add((org.jdom.Text) o);
        }
      }
      if (toRemove != null) {
        for (org.jdom.Text o : toRemove) {
          content.remove(o);
        }
      }
    }
  }

  public static org.jdom.input.SAXBuilder getSAXBuilder() {
    if (saxBuilder == null) {
      /*
           try{
               saxBuilder=new org.jdom.input.SAXBuilder("org.apache.crimson.parser.XMLReaderImpl");
           } catch(Exception ex){
               saxBuilder=new org.jdom.input.SAXBuilder();
           }
      */
      saxBuilder = new org.jdom.input.SAXBuilder();
    }
    return saxBuilder;
  }

  public static void saveDocument(OutputStream out, org.jdom.Element rootElement) throws Exception {
    saveDocument(out, new org.jdom.Document(rootElement));
  }

  public static void saveDocument(OutputStream out, org.jdom.Document doc) throws Exception {
    getXMLOutputter().output(doc, out);
    out.flush();
  }

  public static org.jdom.output.XMLOutputter getXMLOutputter() {
    if (outputter == null) {
      org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
      format.setIndent(" ");
      format.setEncoding("UTF-8");
      format.setTextMode(org.jdom.output.Format.TextMode.PRESERVE);
      outputter = new org.jdom.output.XMLOutputter(format);
    }
    return outputter;
  }

  public static void checkName(org.jdom.Element e, String expectedName) throws Exception {
    if (e == null) {
      throw new org.jdom.JDOMException(
          "Null element passed as argument, expecting: \"" + expectedName + "\"");
    }
    if (!e.getName().equals(expectedName)) {
      throw new org.jdom.JDOMException(
          "Find element \"" + e.getName() + "\" while expecting \"" + expectedName + "\"");
    }
  }

  public static String getClassName(org.jdom.Element e) throws Exception {
    if (e == null) {
      throw new org.jdom.JDOMException("Element without class name!");
    }
    return e.getAttributeValue(CLASS);
  }
}
