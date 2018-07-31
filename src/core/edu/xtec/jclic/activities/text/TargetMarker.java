/*
 * File    : TargetMarker.java
 * Created : 04-jun-2001 19:47
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

package edu.xtec.jclic.activities.text;

import javax.swing.text.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class TargetMarker extends java.lang.Object {

  public int begOffset, endOffset;
  public Position begPos, endPos;
  public TextTarget target;
  public TextActivityDocument doc;
  public boolean hasFocus;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("h: ").append(hashCode());
    sb.append(" [").append(begOffset).append("-").append(endOffset).append("]");
    sb.append(" target: ").append(target);
    return sb.substring(0);
  }

  /** Creates new TargetMarker */
  public TargetMarker(TextActivityDocument document) {
    begOffset = 0;
    endOffset = 0;
    begPos = null;
    endPos = null;
    target = null;
    doc = document;
    hasFocus = false;
  }

  public void reset() {
    hasFocus = false;
    if (target != null) target.reset();
  }

  public void setPositions() {
    try {
      begPos = doc.createPosition(begOffset);
      endPos = doc.createPosition(endOffset);
    } catch (Exception e) {
      System.err.println("Target marker error:\n" + e);
    }
  }

  public void requestFocus(TextActivityBase.Panel tabp) {
    hasFocus = true;
    if (target != null) target.requestFocus(tabp, this);
  }

  public void lostFocus(TextActivityBase.Panel tabp) {
    hasFocus = false;
    if (target != null) target.lostFocus(tabp, this);
  }

  public void updateOffsets() {
    if (begPos != null) begOffset = begPos.getOffset();
    if (endPos != null) endOffset = endPos.getOffset();
  }

  public java.awt.Rectangle getBegRect(JTextComponent pane) {
    if (begPos != null) {
      begOffset = begPos.getOffset();
      try {
        return pane.modelToView(begOffset);
      } catch (Exception ex) {
        System.err.println("Error calling modelToView:\n" + ex);
      }
    }
    return null;
  }

  public String getCurrentText() {
    String result = null;
    if (target != null && target.comboList != null) {
      result = (String) target.comboList.getSelectedItem();
    } else {
      try {
        result = doc.getText(begPos.getOffset(), endPos.getOffset() - begPos.getOffset());
      } catch (Exception e) {
        // eat exception
      }
    }
    return result == null ? new String() : result;
  }

  public String getCurrentText(String boolAttrToExclude) {
    if (boolAttrToExclude == null || (target != null && target.comboList != null))
      return getCurrentText();

    updateOffsets();
    StringBuilder stb = new StringBuilder();
    for (int i = begOffset; i < endOffset; i++) {
      if (!doc.checkBooleanAttribute(i, boolAttrToExclude)) {
        try {
          stb.append(doc.getText(i, 1));
        } catch (Exception e) {
          System.err.println("Error getting text answer:\n" + e);
          break;
        }
      }
    }
    return stb.substring(0);
  }

  public boolean checkText(Evaluator ev) {
    if (target == null) return false;
    return target.checkText(getCurrentText(), ev);
  }

  public boolean contains(int offset, boolean includeEndPos) {
    updateOffsets();
    return offset >= begOffset && (includeEndPos ? (offset <= endOffset) : (offset < endOffset));
  }

  public int getLength() {
    return endOffset - begOffset;
  }

  public int getParagraphBegOffset() {
    updateOffsets();
    return doc.getParagraphElement(begOffset).getStartOffset();
  }
}
