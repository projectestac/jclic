/*
 * File    : FontCheck.java
 * Created : 15-mar-2002 11:33
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
package edu.xtec.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class FontCheck {

  public static final String DEFAULT_FONT_NAME = "default";
  public static final Font DEFAULT_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 17);
  public static final String TMP_FONT_PREFIX = "tmp_font_";
  private static final Map<Object, Font> systemFonts = new HashMap<Object, Font>(12);
  private static String[] fontList;
  public static final String[] fontSizes = new String[] { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22",
      "24", "26", "28", "36", "48", "72" };

  private FontCheck() {
  }

  public static String[] getFontList(boolean reload) {
    if (fontList == null || reload) {
      fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }
    return fontList;
  }

  public static boolean checkFont(Font font) {

    boolean result = false;
    if (font != null) {
      FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
      TextLayout layout = new TextLayout("AB", font, frc);
      result = layout.getBounds().getWidth() > 1;
    }
    return result;
  }

  public static boolean checkFontFamilyName(Object family) {
    return family != null && family instanceof String && checkFont(new Font((String) family, Font.PLAIN, 17));
  }

  public static Font getValidFont(String family, int style, int size) {
    Font f = new Font(family, style, size);
    if (!checkFont(f)) {
      Font fontBase = (Font) systemFonts.get(family.toLowerCase());
      if (fontBase == null) {
        fontBase = DEFAULT_FONT;
      }
      f = fontBase.deriveFont(style, size);
    }
    return f;
  }

  public static String getValidFontFamilyName(Object family) {
    Font f = systemFonts.get(family instanceof String ? ((String) family).toLowerCase() : family);
    if (f != null) {
      return f.getFamily();
    }
    return checkFontFamilyName(family) ? (String) family : DEFAULT_FONT_NAME;
  }

  public static Font checkSystemFont(String fontName, String fontFileName) {
    String fnLower = fontName.toLowerCase();
    Font f = (Font) systemFonts.get(fnLower);
    if (f == null) {
      f = new Font(fontName, Font.PLAIN, 17);
      if (!checkFont(f) || !fontName.toLowerCase().equals(f.getFamily().toLowerCase())) {
        try {
          f = buildNewFont(fontFileName, ResourceManager.STREAM_PROVIDER, "fonts/" + fontFileName);
          if (checkFont(f)) {
            systemFonts.put(fnLower, f);
          } else {
            f = DEFAULT_FONT;
          }
        } catch (Exception ex) {
          System.err.println("Unable to build font " + fontName + "\n:" + ex);
        }
      }
    }
    return f;
  }

  public static Font buildNewFont(String fileName, StreamIO.InputStreamProvider isp, String resourceName)
      throws Exception {

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Font font = Font.createFont(Font.TRUETYPE_FONT, isp.getInputStream(resourceName));
    if (ge != null && font != null) {
      ge.registerFont(font);
      font = getValidFont(font.getName(), Font.PLAIN, 1);
    }
    return font;
  }
}
