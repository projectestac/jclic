/*
 * File    : BoxBase.java
 * Created : 12-dec-2000 11:28
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

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.misc.Gradient;
import edu.xtec.util.Domable;
import edu.xtec.util.FontCheck;
import edu.xtec.util.JDomUtility;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * This class contains all the main visual attributes needed to draw {@link
 * edu.xtec.jclic.boxes.AbstractBox} objects: background and foreground color and gradient, colors
 * for special states (inactive, alternative, disabled...), mrgins, fonts, strokes for borders, etc.
 * Objects derived from <CODE>AbstractBox</CODE> can have inheritance: boxes that act as
 * "containers" of other boxes (like {@link edu.xtec.jclic.boxes.BoxBag}). Most of the attributes of
 * <CODE>BoxBase</CODE> can be <I>null</I>, meaning that the value of the ancestor, or a default
 * value if the box has no ancestors, must be taken.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class BoxBase extends Object implements Cloneable, Domable {

  private Font originalFont;
  public Color backColor;
  public Gradient bgGradient;
  public Color textColor;
  public Color shadowColor;
  public Color borderColor;
  public Color inactiveColor;
  public Color alternativeColor;
  public boolean shadow;
  public boolean transparent;
  public int textMargin;
  protected BasicStroke borderStroke, markerStroke;

  private Font font;
  private float dynFontSize;
  private static final float REDUCE_FONT_STEP = 1;
  public static final float MIN_FONT_SIZE = 8;
  private static int resetAllFontsCounter = 0;
  public static boolean flagFontReduced = false;
  private int resetFontCounter;
  public static final Stroke DEFAULT_STROKE = new BasicStroke(1);
  public static final Color DEFAULT_BACK_COLOR = Color.lightGray;
  public static final Color DEFAULT_TEXT_COLOR = Color.black;
  public static final Color DEFAULT_SHADOW_COLOR = Color.gray;
  public static final Color DEFAULT_INACTIVE_COLOR = Color.gray;
  public static final Color DEFAULT_ALTERNATIVE_COLOR = Color.gray;
  public static final Color DEFAULT_BORDER_COLOR = Color.black;
  public static final float DEFAULT_BORDER_STROKE_WIDTH = 0.75f;
  public static final BasicStroke DEFAULT_BORDER_STROKE =
      new BasicStroke(DEFAULT_BORDER_STROKE_WIDTH);
  public static final float DEFAULT_MARKER_STROKE_WIDTH = 2.75f;
  public static final BasicStroke DEFAULT_MARKER_STROKE =
      new BasicStroke(DEFAULT_MARKER_STROKE_WIDTH);
  // default font will be build on first call to getDefaultFont();
  private static Font DEFAULT_FONT = null;

  public static BoxBase DEFAULT_BOX_BASE = new BoxBase();

  /** Creates new BoxBase */
  public BoxBase() {
    // setFont(new Font(null));
    setFont(getDefaultFont());
    dynFontSize = font.getSize2D();
    backColor = DEFAULT_BACK_COLOR;
    bgGradient = null;
    textColor = DEFAULT_TEXT_COLOR;
    shadowColor = DEFAULT_SHADOW_COLOR;
    inactiveColor = DEFAULT_INACTIVE_COLOR;
    alternativeColor = DEFAULT_ALTERNATIVE_COLOR;
    borderColor = DEFAULT_BORDER_COLOR;
    shadow = false;
    transparent = false;
    textMargin = Constants.AC_MARGIN;
    resetFontCounter = resetAllFontsCounter;
    borderStroke = DEFAULT_BORDER_STROKE;
    markerStroke = DEFAULT_MARKER_STROKE;
  }

  @Override
  public Object clone() {
    BoxBase result = null;
    try {
      result = (BoxBase) super.clone();
      if (bgGradient != null) result.bgGradient = (Gradient) bgGradient.clone();
      result.setFont(originalFont);
    } catch (Exception ex) {
      System.err.println("Unexpected error cloning BoxBase!");
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;

    boolean result = (o == this);
    if (!result && o instanceof BoxBase) {
      BoxBase bb = (BoxBase) o;
      result =
          originalFont.equals(bb.originalFont)
              && backColor.equals(bb.backColor)
              && ((bgGradient == null && bb.bgGradient == null)
                  || (bgGradient != null && bgGradient.equals(bb.bgGradient)))
              && textColor.equals(bb.textColor)
              && shadowColor.equals(bb.shadowColor)
              && borderColor.equals(bb.borderColor)
              && inactiveColor.equals(bb.inactiveColor)
              && alternativeColor.equals(bb.alternativeColor)
              && shadow == bb.shadow
              && transparent == bb.transparent
              && textMargin == bb.textMargin
              && borderStroke.equals(bb.borderStroke)
              && markerStroke.equals(bb.markerStroke);
    }
    return result;
  }

  public static Font getDefaultFont() {
    if (DEFAULT_FONT == null) DEFAULT_FONT = FontCheck.getValidFont("Arial", Font.PLAIN, 17);
    return DEFAULT_FONT;
  }

  public static final String ELEMENT_NAME = "style";
  public static final String COLOR = "color",
      FOREGROUND = "foreground",
      BACKGROUND = "background",
      SHADOW = "shadow",
      INACTIVE = "inactive",
      ALTERNATIVE = "alternative",
      BORDER = "border",
      TRANSPARENT = "transparent",
      MARGIN = "margin",
      BORDER_STROKE = "borderStroke",
      MARKER_STROKE = "markerStroke";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);

    // if(originalFont!=null) e.addContent(JDomUtility.fontToElement(originalFont));
    if (!getDefaultFont().equals(originalFont))
      e.addContent(JDomUtility.fontToElement(originalFont));

    org.jdom.Element ce = new org.jdom.Element(COLOR);
    if (!textColor.equals(DEFAULT_TEXT_COLOR))
      ce.setAttribute(FOREGROUND, JDomUtility.colorToString(textColor));
    if (!backColor.equals(DEFAULT_BACK_COLOR))
      ce.setAttribute(BACKGROUND, JDomUtility.colorToString(backColor));
    if (!shadowColor.equals(DEFAULT_SHADOW_COLOR))
      ce.setAttribute(SHADOW, JDomUtility.colorToString(shadowColor));
    if (!inactiveColor.equals(DEFAULT_INACTIVE_COLOR))
      ce.setAttribute(INACTIVE, JDomUtility.colorToString(inactiveColor));
    if (!alternativeColor.equals(DEFAULT_ALTERNATIVE_COLOR))
      ce.setAttribute(ALTERNATIVE, JDomUtility.colorToString(alternativeColor));
    if (!borderColor.equals(DEFAULT_BORDER_COLOR))
      ce.setAttribute(BORDER, JDomUtility.colorToString(borderColor));
    if (!ce.getAttributes().isEmpty()) e.addContent(ce);

    if (bgGradient != null) e.addContent(bgGradient.getJDomElement());

    if (shadow) e.setAttribute(SHADOW, JDomUtility.boolString(shadow));
    if (transparent) e.setAttribute(TRANSPARENT, JDomUtility.boolString(transparent));
    if (textMargin != Constants.AC_MARGIN) e.setAttribute(MARGIN, Integer.toString(textMargin));
    if (borderStroke.getLineWidth() != DEFAULT_BORDER_STROKE_WIDTH)
      e.setAttribute(BORDER_STROKE, Float.toString(borderStroke.getLineWidth()));
    if (markerStroke.getLineWidth() != DEFAULT_MARKER_STROKE_WIDTH)
      e.setAttribute(MARKER_STROKE, Float.toString(markerStroke.getLineWidth()));

    return e;
  }

  public static BoxBase getBoxBase(org.jdom.Element e) throws Exception {
    BoxBase bb = new BoxBase();
    bb.setProperties(e, null);
    return bb;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    org.jdom.Element child;
    String s;

    if ((child = e.getChild(JDomUtility.FONT)) != null) {
      setFont(JDomUtility.elementToFont(child));
    }
    if ((child = e.getChild(COLOR)) != null) {
      textColor = JDomUtility.getColorAttr(child, FOREGROUND, textColor);
      backColor = JDomUtility.getColorAttr(child, BACKGROUND, backColor);
      shadowColor = JDomUtility.getColorAttr(child, SHADOW, shadowColor);
      inactiveColor = JDomUtility.getColorAttr(child, INACTIVE, inactiveColor);
      alternativeColor = JDomUtility.getColorAttr(child, ALTERNATIVE, alternativeColor);
      borderColor = JDomUtility.getColorAttr(child, BORDER, borderColor);
    }
    shadow = JDomUtility.getBoolAttr(e, SHADOW, shadow);
    transparent = JDomUtility.getBoolAttr(e, TRANSPARENT, transparent);
    textMargin = JDomUtility.getIntAttr(e, MARGIN, textMargin);
    if ((s = e.getAttributeValue(BORDER_STROKE)) != null) setBorderWidth(Float.parseFloat(s));
    if ((s = e.getAttributeValue(MARKER_STROKE)) != null) setMarkerWidth(Float.parseFloat(s));

    if ((child = e.getChild(Gradient.ELEMENT_NAME)) != null)
      bgGradient = Gradient.getGradient(child);
  }

  public Stroke getBorder() {
    return borderStroke;
  }

  public float getBorderWidth() {
    return borderStroke.getLineWidth();
  }

  public void setBorderWidth(float w) {
    borderStroke = new BasicStroke(w);
  }

  public Stroke getMarker() {
    return markerStroke;
  }

  public float getMarkerWidth() {
    return markerStroke.getLineWidth();
  }

  public void setMarkerWidth(float w) {
    markerStroke = new BasicStroke(w);
  }

  public static void resetAllFonts() {
    resetAllFontsCounter++;
  }

  public void setFont(Font newFont) {
    if (newFont != null) {
      font = newFont;
      dynFontSize = font.getSize();
      originalFont = font.deriveFont(new AffineTransform());
    }
  }

  public void resetFont() {
    resetFontCounter = resetAllFontsCounter - 1;
  }

  public Font getFont() {
    if (resetFontCounter < resetAllFontsCounter) {
      resetFontCounter = resetAllFontsCounter;
      font = originalFont.deriveFont(new AffineTransform());
      dynFontSize = font.getSize();
    }
    return font;
  }

  public Font getOriginalFont() {
    return originalFont;
  }

  public float getDynFontSize() {
    return dynFontSize;
  }

  public boolean reduceFont() {
    if (dynFontSize <= MIN_FONT_SIZE) return false;
    flagFontReduced = true;
    dynFontSize -= REDUCE_FONT_STEP;
    font = font.deriveFont(dynFontSize);
    return true;
  }
}
