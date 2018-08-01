/*
 * File    : Identify.java
 * Created : 24-oct-2001 17:03
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
import edu.xtec.util.JDomUtility;
import java.awt.AWTEvent;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class Identify extends TextActivityBase {

  public static final int IDENTIFY_WORDS = 0, IDENTIFY_CHARS = 1;
  protected int type;

  /** Creates new Identify */
  public Identify(JClicProject project) {
    super(project);
    // evalOnTheFly=false;
    setType(IDENTIFY_WORDS);
    hasCheckButton = true;
    checkButtonText = "";
  }

  public static final String[] TYPES = { "identifyWords", "identifyChars" };

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();
    e.setAttribute(TYPE, TYPES[type]);
    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    super.setProperties(e, aux);
    setType(JDomUtility.getStrIndexAttr(e, TYPE, TYPES, type));
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    setType(c3a.puzMode == edu.xtec.jclic.clic3.Clic3.IDPARAULES ? IDENTIFY_WORDS : IDENTIFY_CHARS);
  }

  @Override
  public Activity.Panel getActivityPanel(PlayStation ps) {
    return new Panel(ps);
  }

  /**
   * Getter for property type.
   *
   * @return Value of property type.
   */
  public int getType() {
    return type;
  }

  /**
   * Setter for property type.
   *
   * @param type New value of property type.
   */
  public void setType(int type) {
    this.type = (type == IDENTIFY_CHARS ? IDENTIFY_CHARS : IDENTIFY_WORDS);
    tad.setTargetType(type == IDENTIFY_CHARS ? TextActivityDocument.TT_CHAR : TextActivityDocument.TT_WORD);
  }

  class Panel extends TextActivityBase.Panel {

    TextActivityDocument playDoc;
    int nActions;
    int score;
    boolean[] validChars;
    boolean[] marks;
    boolean[] targets;

    protected Panel(PlayStation ps) {
      super(ps);
      playDoc = null;
      nActions = 0;
      score = 0;
      validChars = null;
      marks = null;
      targets = null;
    }

    @Override
    protected void initDocument() throws Exception {
      if (tad != null) {
        playing = false;
        playDoc = new TextActivityDocument(styleContext);
        tad.cloneDoc(playDoc, false, false, true);
        if (!buildCheckArrays())
          return;
        pane.setStyledDocument(playDoc);
        playDoc.attachTo(pane, Identify.Panel.this);
        pane.setEditable(false);
        pane.requestFocus();
        nActions = 0;
        score = 0;
      }
    }

    private boolean buildCheckArrays() {
      if (playDoc == null)
        return false;
      int l = playDoc.getLength();
      if (l > 0) {
        char[] chars;
        try {
          chars = playDoc.getText(0, l).toCharArray();
        } catch (BadLocationException ex) {
          playDoc = null;
          return false;
        }
        validChars = new boolean[l];
        marks = new boolean[l];
        targets = new boolean[l];
        for (int i = 0; i < l; i++) {
          marks[i] = false;
          targets[i] = false;
          validChars[i] = Character.isLetterOrDigit(chars[i]);
        }
        for (int i = 0; i < playDoc.tmb.size(); i++) {
          TargetMarker tm = playDoc.tmb.getElement(i);
          for (int j = tm.begOffset; j < tm.endOffset; j++)
            targets[j] = true;
        }
      } else {
        validChars = new boolean[1];
        marks = new boolean[1];
        targets = new boolean[1];
      }
      return true;
    }

    @Override
    protected TextActivityPane buildPane() {
      IdentifyPane p = new IdentifyPane();
      p.setActions();
      return p;
    }

    class IdentifyPane extends TextActivityPane {

      protected IdentifyPane() {
        super(Identify.Panel.this);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
      }

      @Override
      public boolean processMouse(MouseEvent e) {
        if (!super.processMouse(e) || !playing || playDoc == null)
          return false;

        if (e.getID() == MouseEvent.MOUSE_PRESSED)
          select(viewToModel(e.getPoint()));

        return false;
      }

      protected void select(int p) {
        if (p < 0 || p >= marks.length) {
          playEvent(EventSounds.ACTION_ERROR);
          return;
        }

        nActions++;
        boolean marked = marks[p];
        int l = 1;

        TargetMarker tm = playDoc.tmb.getElementByOffset(p, false);
        if (tm != null) {
          p = tm.begOffset;
          l = tm.getLength();
        } else {
          if (!validChars[p]) {
            playEvent(EventSounds.ACTION_ERROR);
            return;
          }
          if (type == IDENTIFY_WORDS) {
            int it = p;
            while (it > 0 && validChars[it] == true)
              it--;
            int p0 = it + 1;
            it = p;
            p = p0;
            while (it < validChars.length && validChars[it] == true)
              it++;
            l = it - p0;
          }
        }

        playDoc.setCharacterAttributes(p, l,
            styleContext.getStyle(marked ? StyleContext.DEFAULT_STYLE : TextActivityDocument.TARGET), true);

        for (int i = 0; i < l; i++)
          marks[p + i] = !marked;

        if (!hasCheckButton) {
          TargetMarkerBag tmb = playDoc.tmb;
          boolean ok = (tm != null && marks[p] == true);
          boolean finishedOk = checkScore();
          try {
            tabp.ps.reportNewAction(Identify.this, ACTION_SELECT, playDoc.getText(p, l), (marked ? "true" : "false"),
                ok, score);
          } catch (Exception ex) {
            // should never done
          }
          if (finishedOk) {
            finishActivity(true);
            return;
          }
        }
        playEvent(EventSounds.CLICK);
      }

      protected void setActions() {
        java.util.Map<String, Object[]> actionKeys = Actions.getActionKeys(this);
        ActionMap am = new ActionMap();
        am.setParent(getActionMap());
        setActionMap(am);
        // Originals:
        Actions.mapTraceAction(this, actionKeys, DefaultEditorKit.beepAction);
        Actions.mapTraceAction(this, actionKeys, "requestFocus");
        Actions.mapTraceAction(this, actionKeys, "toggle-componentOrientation");
        am.setParent(null);
      }
    }

    protected boolean checkScore() {
      score = 0;
      for (int i = 0; i < playDoc.tmb.size(); i++) {
        if (marks[playDoc.tmb.getElement(i).begOffset] == true)
          score++;
      }

      for (int i = 0; i < targets.length; i++)
        if (marks[i] && !targets[i])
          return false;

      return (score == playDoc.tmb.size());
    }

    @Override
    protected void doCheck(boolean fromButton) {
      if (playDoc == null || playDoc.tmb.size() == 0)
        return;
      TargetMarkerBag tmb = playDoc.tmb;
      int i, j;
      playDoc.setCharacterAttributes(0, marks.length, styleContext.getStyle(StyleContext.DEFAULT_STYLE), true);
      for (i = 0; i < marks.length; i++) {
        if (marks[i]) {
          for (j = i; j < marks.length; j++)
            if (!marks[j])
              break;
          playDoc.setCharacterAttributes(i, j - i, styleContext.getStyle(TextActivityDocument.TARGET_ERROR), true);
          i = j;
        }
      }
      for (i = 0; i < tmb.size(); i++) {
        TargetMarker tm = tmb.getElement(i);
        playDoc.applyStyleToTarget(tm, marks[tm.begOffset] ? TextActivityDocument.TARGET : StyleContext.DEFAULT_STYLE,
            false, true);
      }
      boolean finishedOk = checkScore();
      ps.setCounterValue(SCORE_COUNTER, score);
      ps.setCounterValue(ACTIONS_COUNTER, nActions);
      if (!finishedOk) {
        if (fromButton) {
          playEvent(EventSounds.FINISHED_ERROR);
          pane.requestFocus();
        }
      } else
        finishActivity(true);
    }

    @Override
    public void finishActivity(boolean result) {
      pane.setEnabled(false);
      super.finishActivity(result);
    }
  }
}
