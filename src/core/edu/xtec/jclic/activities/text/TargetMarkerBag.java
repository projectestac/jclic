/*
 * File    : TargetMarkerBag.java
 * Created : 04-jun-2001 19:54
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

import java.util.Iterator;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class TargetMarkerBag extends java.util.ArrayList<TargetMarker> {

  private TargetMarker currentTarget;

  /** Creates new TargetMarkerBag */
  public TargetMarkerBag() {
    super(10);
    currentTarget = null;
  }

  @Override
  public boolean add(TargetMarker tm) {
    int i;
    /*
    if (!(obj instanceof TargetMarker)) {
        return false;
    }
    TargetMarker tm = (TargetMarker) obj;
    */
    for (i = 0; i < size(); i++) {
      if (getElement(i).begOffset >= tm.begOffset) break;
    }
    if (i == size()) {
      return super.add(tm);
    }
    super.add(i, tm);
    return true;
  }

  public TargetMarker getElement(int n) {
    try {
      return get(n);
    } catch (Exception e) {
      return null;
    }
  }

  public void setParentPane(TextActivityPane parent) {
    for (int i = 0; i < size(); i++) {
      TargetMarker tm = getElement(i);
      if (tm.target != null) tm.target.setParentPane(parent);
    }
  }

  public void setPositions() {
    for (int i = 0; i < size(); i++) getElement(i).setPositions();
  }

  public void updateOffsets() {
    for (int i = 0; i < size(); i++) getElement(i).updateOffsets();
  }

  public void removeUnattachedElements() {
    Iterator it = iterator();
    while (it.hasNext()) {
      TargetMarker tm = (TargetMarker) it.next();
      tm.updateOffsets();
      if (tm.begOffset < 0 || tm.endOffset < 0 || tm.begOffset == tm.endOffset) it.remove();
    }
  }

  public int checkTargets(Evaluator ev) {
    int result = 0;
    for (int i = 0; i < size(); i++) {
      if (getElement(i).checkText(ev)) result++;
    }
    return result;
  }

  public int countSolvedTargets() {
    int result = 0;
    for (int i = 0; i < size(); i++) {
      TextTarget tt = getElement(i).target;
      if (tt != null && tt.targetStatus == TextTarget.SOLVED) result++;
    }
    return result;
  }

  public void reset() {
    for (int i = 0; i < size(); i++) getElement(i).reset();
  }

  public TargetMarker getElementByOffset(int offset, boolean includeEndPos) {
    for (int i = 0; i < size(); i++) {
      TargetMarker tm = getElement(i);
      if (tm.contains(offset, includeEndPos)) return tm;
    }
    return null;
  }

  public TargetMarker getNearestElement(int offset, boolean searchForward) {
    TargetMarker prev = null, next = null;
    for (int i = 0; i < size(); i++) {
      TargetMarker tm = getElement(i);

      if (tm.begPos.getOffset() <= offset) {
        prev = tm;
      }

      if (tm.endPos.getOffset() >= offset) {
        next = tm;
        break;
      }
    }
    return searchForward ? next : prev;
  }

  public void setCurrentTarget(TargetMarker tm, TextActivityBase.Panel tabp) {
    if (currentTarget != null /* && currentTarget!=tm*/) {
      currentTarget.lostFocus(tabp);
    }

    if (tm != null && contains(tm)) {
      currentTarget = tm;
      if (currentTarget.target != null) {
        currentTarget.target.setModified(false);
      }
      currentTarget.requestFocus(tabp);
    } else {
      currentTarget = null;
    }
  }

  public TargetMarker getCurrentTarget() {
    return currentTarget;
  }

  public TargetMarker getElement(TextTarget tt) {
    for (int i = 0; i < size(); i++) {
      TargetMarker tm = getElement(i);
      if (tm.target == tt) return tm;
    }
    return null;
  }

  public TargetMarker getNextTarget(TargetMarker tm) {
    return getElement(Math.min(indexOf(tm == null ? currentTarget : tm) + 1, size() - 1));
  }

  public TargetMarker getPrevTarget(TargetMarker tm) {
    return getElement(Math.max(indexOf(tm == null ? currentTarget : tm) - 1, 0));
  }

  public void swapTargets(TargetMarker tm1, TargetMarker tm2) {
    TextActivityDocument doc = tm1.doc;
    if (doc != tm2.doc) {
      System.err.println("Error: unable to swap among different documents");
      return;
    }
    try {
      tm1.updateOffsets();
      String tx1 = doc.getText(tm1.begOffset, tm1.getLength());
      tm2.updateOffsets();
      String tx2 = doc.getText(tm2.begOffset, tm2.getLength());
      doc.remove(tm1.begOffset, tx1.length());
      doc.insertString(tm1.begOffset, tx2, doc.getTargetAttributeSet());
      tm1.endOffset = tm1.begOffset + tx2.length();
      tm1.setPositions();
      tm2.updateOffsets();
      doc.remove(tm2.begOffset, tx2.length());
      doc.insertString(tm2.begOffset, tx1, doc.getTargetAttributeSet());
      tm2.endOffset = tm2.begOffset + tx1.length();
      tm2.setPositions();
    } catch (Exception ex) {
      System.err.println("Error: unable to swap target texts:\n" + ex);
    }
    updateOffsets();
  }

  public int[] getParagragraphOffsets() {
    if (size() == 0) return null;

    int[] pOffsets = new int[size()];
    for (int i = 0; i < size(); i++) pOffsets[i] = getElement(i).getParagraphBegOffset();

    return pOffsets;
  }
}
