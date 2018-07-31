/*
 * File    : Complete.java
 * Created : 24-oct-2001 17:01
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

import edu.xtec.jclic.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.Actions;
import edu.xtec.util.StrUtils;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class Complete extends TextActivityBase {

  Evaluator ev;

  /** Creates new Complete */
  public Complete(JClicProject project) {
    super(project);
    ev = new ComplexEvaluator(project);
    hasCheckButton = true;
  }

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();
    e.addContent(ev.getJDomElement());
    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    super.setProperties(e, aux);
    ev = Evaluator.getEvaluator(e.getChild(Evaluator.ELEMENT_NAME), project);
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    ((ComplexEvaluator) ev).setProperties(c3a);
    hasCheckButton = true;
  }

  @Override
  public boolean needsKeyboard() {
    return true;
  }

  @Override
  public Activity.Panel getActivityPanel(PlayStation ps) {
    return new Panel(ps);
  }

  class Panel extends TextActivityBase.Panel {

    TextActivityDocument playDoc = null;
    boolean locked;
    int nActions;

    protected Panel(PlayStation ps) {
      super(ps);
      locked = true;
      nActions = 0;
    }

    @Override
    protected void initDocument() throws Exception {
      nActions = 0;
      if (tad != null) {
        playing = false;
        playDoc = new TextActivityDocument(styleContext);
        tad.cloneDoc(playDoc, true, false, false);
        pane.setStyledDocument(playDoc);
        playDoc.attachTo(pane, Complete.Panel.this);
        pane.setEnabled(true);
        if (playDoc.tmb.size() > 0) {
          pane.setEditable(true);
          pane.requestFocus();
          pane.getCaret().setVisible(true);
          locked = false;
        } else {
          locked = true;
          pane.setEditable(false);
          pane.getCaret().setVisible(false);
        }
      }
    }

    @Override
    protected TextActivityPane buildPane() {
      // CompletePane cp=new CompletePane(this);
      CompletePane cp = new CompletePane();
      cp.setActions();
      return cp;
    }

    class CompletePane extends TextActivityPane {

      // public CompletePane(Complete act){
      protected CompletePane() {
        super(Complete.Panel.this);
      }

      @Override
      public boolean processMouse(MouseEvent e) {
        if (super.processMouse(e) && e.getID() == MouseEvent.MOUSE_PRESSED && playing && !locked)
          return true;

        return false;
      }

      protected void invalidateSelection() {
        int offset = getCaret().getDot();
        if (getCaret().getMark() != offset) getCaret().setDot(offset);
      }

      @Override
      protected void fireCaretUpdate(CaretEvent e) {
        invalidateSelection();
        super.fireCaretUpdate(e);
      }

      @Override
      public void replaceSelection(String content) {
        invalidateSelection();
        if (content != null && content.length() > 0) {
          char ch = content.charAt(0);
          int offset = getCaret().getDot();
          if (ch >= 0x20 && ch != 0x7F) {
            try {
              playDoc.insertString(
                  offset, new String(new char[] {ch}), playDoc.getTargetAttributeSet());
            } catch (BadLocationException ex) {
              System.err.println("Text activity error:\n" + ex);
            }
          }
        }
      }

      @Override
      public void cut() {
        invalidateSelection();
      }

      @Override
      public void paste() {
        invalidateSelection();
      }

      boolean readyForActions() {
        return playing && !locked && isEditable() && isEnabled();
      }

      // Actions
      Action kitDeletePrevCharAction = null;
      AbstractAction deletePrevCharAction =
          new AbstractAction(DefaultEditorKit.deletePrevCharAction) {
            public void actionPerformed(ActionEvent e) {
              if (readyForActions() && kitDeletePrevCharAction != null) {
                invalidateSelection();
                int offset = getCaret().getDot() - 1;
                if (offset > 0
                    && playDoc.checkBooleanAttribute(offset, TextActivityDocument.TARGET) == true) {
                  kitDeletePrevCharAction.actionPerformed(e);
                }
              }
            }
          };

      Action kitDeleteNextCharAction = null;
      AbstractAction deleteNextCharAction =
          new AbstractAction(DefaultEditorKit.deleteNextCharAction) {
            public void actionPerformed(ActionEvent e) {
              if (readyForActions() && kitDeleteNextCharAction != null) {
                invalidateSelection();
                int offset = getCaret().getDot();
                if (offset >= 0
                    && offset < playDoc.getLength()
                    && playDoc.checkBooleanAttribute(offset, TextActivityDocument.TARGET) == true) {
                  kitDeleteNextCharAction.actionPerformed(e);
                }
              }
            }
          };

      protected void setActions() {
        kitDeleteNextCharAction = getActionMap().get(DefaultEditorKit.deleteNextCharAction);
        kitDeletePrevCharAction = getActionMap().get(DefaultEditorKit.deletePrevCharAction);
        java.util.Map<String, Object[]> actionKeys = Actions.getActionKeys(this);
        ActionMap am = new ActionMap();
        am.setParent(getActionMap());
        setActionMap(am);
        Actions.mapAction(this, actionKeys, deletePrevCharAction);
        Actions.mapAction(this, actionKeys, deleteNextCharAction);
      }
    }

    @Override
    protected void doCheck(boolean fromButton) {
      if (playDoc == null || locked) return;

      String match;
      String current;
      try {
        match = StrUtils.trimEnding(tad.getText(0, tad.getLength()));
        current = StrUtils.trimEnding(playDoc.getText(0, playDoc.getLength()));
      } catch (BadLocationException e) {
        System.err.println("Error: unable to retrieve text:\n" + e);
        return;
      }

      byte[] result = ev.evalText(current, match);
      int score = 0;
      if (result != null) {
        int l = result.length;
        int i = 0;
        while (i < l) {
          while (i < l && !playDoc.checkBooleanAttribute(i, TextActivityDocument.TARGET)) i++;
          if (i < l) {
            nActions++;
            boolean actionOk = true;
            int j = i;
            while (i < l && playDoc.checkBooleanAttribute(i, TextActivityDocument.TARGET)) i++;
            playDoc.setCharacterAttributes(
                j, i - j, styleContext.getStyle(TextActivityDocument.TARGET), true);
            for (int k = j; k < i; k++) {
              if (result[k] != Evaluator.FLAG_OK) {
                actionOk = false;
                playDoc.setCharacterAttributes(
                    k, 1, styleContext.getStyle(TextActivityDocument.TARGET_ERROR), false);
              }
            }
            if (actionOk) score++;
          }
        }
      }

      ps.setCounterValue(SCORE_COUNTER, score);
      ps.setCounterValue(ACTIONS_COUNTER, nActions);

      if (Evaluator.isOk(result)) finishActivity(true);
      else if (fromButton) playEvent(EventSounds.FINISHED_ERROR);
    }

    @Override
    public void finishActivity(boolean result) {
      pane.setEditable(false);
      pane.setEnabled(false);
      super.finishActivity(result);
    }
  }
}
