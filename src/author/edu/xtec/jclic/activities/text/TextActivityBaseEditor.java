/*
 * File    : TextActivityBaseEditor.java
 * Created : 10-oct-2002 15:40
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

import edu.xtec.jclic.ActivityEditor;
import edu.xtec.jclic.ActivityEditorPanel;
import edu.xtec.jclic.boxes.ActiveBagContentEditor;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.boxes.ActiveBoxContentEditor;
import edu.xtec.jclic.boxes.JPanelActiveBox;
import edu.xtec.util.Options;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class TextActivityBaseEditor extends ActivityEditor {

  /** Creates a new instance of TextActivityBaseEditor */
  public TextActivityBaseEditor(TextActivityBase act) {
    super(act);
  }

  @Override
  protected void createPanels(ActivityEditorPanel panel) {
    panel.addInternalPanel(ActivityEditorPanel.TEXT_BASE, null, null);
  }

  protected TextActivityBase getTextActivity() {
    return (TextActivityBase) getActivity();
  }

  protected JPanelActiveBox createNewBox(int pos, Options options, Component parent) {
    JPanelActiveBox result = null;
    TextActivityBase tab = getTextActivity();

    AttributeSet a = tab.tad.getCharacterElement(pos).getAttributes();
    if (a.isDefined(TextActivityDocument.TARGET)) {
      options.getMessages().showAlert(parent, "edit_text_act_err_cellInTarget");
      return null;
    }

    ActiveBoxContent ab = new ActiveBoxContent();
    ab.setDimension(new Dimension(100, 40));
    ab.setBoxBase(tab.tad.boxesContent.bb);
    ab = ActiveBoxContentEditor.getActiveBoxContent(ab, parent, options, getMediaBagEditor(), null);
    if (ab != null) {
      try {
        result = TextActivityDocument.insertBox(ab, pos, tab.tad, tab, null);
      } catch (Exception ex) {
        options.getMessages().showErrorWarning(parent, "ERROR", ex);
      }
    }
    return result;
  }

  protected int from, to;

  protected TargetMarker createNewTarget(int ifrom, int ito, Options options, Component parent) {
    TargetMarker tm = null;
    TextActivityBase tab = getTextActivity();
    if (tab != null) {
      from = ifrom;
      to = ito;
      if (!checkTargetSegment(options, parent))
        return null;
      tm = new TargetMarker(tab.tad);
      tm.begOffset = from;
      tm.endOffset = to;
      tm.setPositions();
      tab.tad.tmb.add(tm);
      tab.tad.applyStyleToTarget(tm, TextActivityDocument.TARGET, false, true);
    }
    return tm;
  }

  private boolean checkTargetSegment(Options options, Component parent) {
    String errMsg = null;
    if (getTextActivity() == null)
      return false;
    TextActivityDocument tad = getTextActivity().tad;
    String text;
    try {
      text = tad.getText(0, tad.getLength());
    } catch (Exception ex) {
      return false;
    }
    int len = text.length();
    from = Math.min(Math.max(0, from), Math.max(0, len - 1));
    to = Math.min(Math.max(from, to), len);

    switch (tad.getTargetType()) {
    case TextActivityDocument.TT_FREE:
      int fromBk = from;
      int toBk = to;
      while (from < len && Character.isWhitespace(text.charAt(from)))
        from++;
      while (to >= from && to > 0 && Character.isWhitespace(text.charAt(to - 1)))
        to--;
      if (from >= to) {
        from = fromBk;
        to = toBk;
      }
      break;
    case TextActivityDocument.TT_CHAR:
      while (from < len && Character.isWhitespace(text.charAt(from)))
        from++;
      to = Math.min(from + 1, len);
      break;
    case TextActivityDocument.TT_WORD:
      while (from >= 0 && !Character.isWhitespace(text.charAt(from)))
        from--;
      from++;
      to = from;
      while (to < len && !Character.isWhitespace(text.charAt(to)))
        to++;
      break;
    case TextActivityDocument.TT_PARAGRAPH:
      javax.swing.text.Element element = tad.getParagraphElement(from);
      if (element == null)
        to = from;
      else {
        from = Math.max(0, element.getStartOffset());
        to = Math.max(0, Math.min(text.length() - 1, element.getEndOffset() - 1));
        while (to > from && (text.charAt(to) == 0x0D || text.charAt(to) == 0x0A))
          to--;
        to++;
      }
      break;
    default:
      break;
    }

    if (to == from)
      errMsg = "edit_text_act_err_noSelection";
    else if (from > to)
      errMsg = "ERROR";
    else {
      try {
        for (int i = from; i < to; i++) {
          char ch = tad.getText(i, 1).charAt(0);
          if (ch == 0x0D || ch == 0x0A || ch == 0x08) {
            errMsg = "edit_text_act_err_badCharInSelection";
            break;
          }
          AttributeSet a = tad.getCharacterElement(i).getAttributes();
          if (a.isDefined(TextActivityDocument.TARGET)) {
            errMsg = "edit_text_act_err_nestedTargets";
            break;
          }
          if (a.getAttribute(StyleConstants.ComponentAttribute) != null) {
            errMsg = "edit_text_act_err_cellInTarget";
            break;
          }
        }
      } catch (BadLocationException ex) {
      }
    }

    if (errMsg != null && options != null)
      options.getMessages().showAlert(parent, errMsg);

    return errMsg == null;
  }

  protected boolean deleteTarget(TargetMarker tm, Options options, Component parent) {
    TextActivityBase tab = getTextActivity();
    if (tab != null) {
      tab.tad.tmb.remove(tm);
      tab.tad.applyStyleToTarget(tm, null, false, true);
    }
    return true;
  }

  protected boolean canEditTarget() {
    return false;
  }

  protected boolean editTarget(int pos, Options options, Component parent) {
    boolean result = false;
    TextActivityBase tab = getTextActivity();
    if (canEditTarget() && pos >= 0 && pos < tab.tad.getLength()) {
      TargetMarker tm = tab.tad.tmb.getElementByOffset(pos, true);
      if (tm != null && tm.target != null)
        result = TextTargetEditorPanel.editTextTarget(tm.target, options, getMediaBagEditor(), parent);
      if (result)
        setModified(true);
    }
    return result;
  }

  protected void editCheckOptions(Options options, Component parent) {
  }

  protected boolean hasType() {
    return false;
  }

  protected boolean editType(Options options, Component parent) {
    return false;
  }

  public boolean nameChanged(int type, String oldName, String newName) {
    boolean result = super.nameChanged(type, oldName, newName);

    result |= ActiveBagContentEditor.nameChanged(getTextActivity().tad.boxesContent, type, oldName, newName);

    result |= ActiveBagContentEditor.nameChanged(getTextActivity().tad.popupsContent, type, oldName, newName);

    if (result)
      setModified(true);

    return result;
  }
}
