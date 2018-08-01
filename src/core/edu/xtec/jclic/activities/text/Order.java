/*
 * File    : Order.java
 * Created : 24-oct-2001 17:04
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
public class Order extends TextActivityBase {

  public static final int ORDER_WORDS = 0, ORDER_PARAGRAPHS = 1;
  protected boolean amongParagraphs;
  protected int type;
  protected Evaluator ev;

  /** Creates new Order */
  public Order(JClicProject project) {
    super(project);
    amongParagraphs = false;
    setType(ORDER_WORDS);
    hasCheckButton = true;
    checkButtonText = "";
    ev = new BasicEvaluator(project);
  }

  public static final String[] TYPES = { "orderWords", "orderParagraphs" };
  public static final String AMONG_PARAGRAPHS = "amongParagraphs";

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();
    e.setAttribute(TYPE, TYPES[type]);
    if (amongParagraphs)
      e.setAttribute(AMONG_PARAGRAPHS, JDomUtility.boolString(amongParagraphs));
    e.addContent(ev.getJDomElement());
    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    super.setProperties(e, aux);
    setType(JDomUtility.getStrIndexAttr(e, TYPE, TYPES, type));
    if (type == ORDER_WORDS)
      amongParagraphs = JDomUtility.getBoolAttr(e, AMONG_PARAGRAPHS, amongParagraphs);
    ev = Evaluator.getEvaluator(e.getChild(Evaluator.ELEMENT_NAME), project);
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    ((BasicEvaluator) ev).setProperties(c3a);
    setType(c3a.puzMode == edu.xtec.jclic.clic3.Clic3.BPARAGRAFS ? ORDER_PARAGRAPHS : ORDER_WORDS);
    amongParagraphs = c3a.brPar;
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

  @Override
  public boolean hasRandom() {
    return true;
  }

  @Override
  public boolean shuffleAlways() {
    return true;
  }

  /**
   * Setter for property type.
   *
   * @param type New value of property type.
   */
  public void setType(int type) {
    this.type = (type == ORDER_WORDS ? ORDER_WORDS : ORDER_PARAGRAPHS);
    tad.setTargetType(type == ORDER_WORDS ? TextActivityDocument.TT_WORD : TextActivityDocument.TT_PARAGRAPH);
  }

  class Panel extends TextActivityBase.Panel {

    TextActivityDocument playDoc;
    int nActions;
    TargetMarker anchor, cursor;

    protected Panel(PlayStation ps) {
      super(ps);
      playDoc = null;
      nActions = 0;
    }

    @Override
    protected void initDocument() throws Exception {
      if (tad != null) {
        playing = false;
        playDoc = new TextActivityDocument(styleContext);
        tad.cloneDoc(playDoc, false, false, false);
        for (int i = 0; i < playDoc.tmb.size(); i++) {
          playDoc.tmb.getElement(i).target = new TextTarget();
          playDoc.tmb.getElement(i).target.answer = new String[] { playDoc.tmb.getElement(i).getCurrentText() };
        }
        if (playDoc.tmb.size() > 1) {
          for (int i = 0; i < 5; i++) {
            if (shuffle() != playDoc.tmb.size())
              break;
          }
        }
        pane.setStyledDocument(playDoc);
        playDoc.attachTo(pane, Order.Panel.this);
        pane.setEditable(false);
        pane.requestFocus();
        bc = new edu.xtec.jclic.boxes.BoxConnector(pane);
        nActions = 0;
      }
    }

    private int shuffle() {
      if (playDoc == null || playDoc.tmb.size() < 2)
        return 0;
      TargetMarkerBag tmb = playDoc.tmb;
      int k = tmb.size();
      int[] p = tmb.getParagragraphOffsets();
      int[] p2 = new int[k];
      int k2;
      for (k2 = 0; k2 < k; k2++)
        p2[k2] = k2;
      int t1;
      int t2 = 0;
      java.util.Random rnd = new java.util.Random();
      for (int i = 0; i < shuffles; i++) {
        t1 = rnd.nextInt(k);
        if (type == ORDER_WORDS && !amongParagraphs) {
          k2 = 0;
          for (int j = 0; j < k; j++) {
            if (j != t1 && p[j] == p[t1])
              p2[k2++] = j;
          }
        }
        if (k2 > 0) {
          for (int c = 0; c < 300; c++) {
            t2 = p2[rnd.nextInt(k2)];
            if (t2 != t1)
              break;
          }
          if (t1 != t2)
            tmb.swapTargets(tmb.getElement(t1), tmb.getElement(t2));
        }
      }
      return tmb.checkTargets(ev);
    }

    @Override
    protected TextActivityPane buildPane() {
      OrderPane p = new OrderPane();
      p.setActions();
      return p;
    }

    class OrderPane extends TextActivityPane {

      protected OrderPane() {
        super(Order.Panel.this);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
      }

      @Override
      public boolean processMouse(MouseEvent e) {
        if (!super.processMouse(e) || bc == null || !playing)
          return false;

        switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
          int p = viewToModel(e.getPoint());
          if (p >= 0) {
            playEvent(EventSounds.CLICK);
            TargetMarker tm = playDoc.tmb.getElementByOffset(p, false);
            if (tm != null) {
              setTargetCursor(tm);
              if (bc.active) {
                bc.end();
                if (cursor == anchor) {
                  setTargetAnchor(null);
                  setTargetCursor(tm);
                } else {
                  swapTargets(anchor, cursor);
                }
              } else {
                setTargetAnchor(tm);
                bc.begin(e.getPoint());
              }
            } else {
              bc.end();
              setTargetAnchor(null);
            }
          }
          break;

        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_DRAGGED:
          if (bc.active)
            bc.moveTo(e.getPoint());
          break;
        }

        return false;
      }

      public boolean initKeyAction() {
        if (!playing)
          return false;
        if (bc.active)
          bc.end();
        if (cursor == null) {
          setTargetCursor(playDoc.tmb.getElement(0));
          return false;
        }
        return true;
      }

      // Actions
      AbstractAction forwardAction = new AbstractAction(DefaultEditorKit.forwardAction) {
        public void actionPerformed(ActionEvent e) {
          nextTargetAction.actionPerformed(e);
        }
      };

      AbstractAction backwardAction = new AbstractAction(DefaultEditorKit.backwardAction) {
        public void actionPerformed(ActionEvent e) {
          prevTargetAction.actionPerformed(e);
        }
      };

      AbstractAction nextTargetAction = new AbstractAction("next-target") {
        public void actionPerformed(ActionEvent e) {
          if (initKeyAction()) {
            setTargetCursor(playDoc.tmb.getNextTarget(cursor));
          }
        }
      };

      AbstractAction prevTargetAction = new AbstractAction("prev-target") {
        public void actionPerformed(ActionEvent e) {
          if (initKeyAction()) {
            setTargetCursor(playDoc.tmb.getPrevTarget(cursor));
          }
        }
      };

      AbstractAction insertBreakAction = new AbstractAction(DefaultEditorKit.insertBreakAction) {
        public void actionPerformed(ActionEvent e) {
          if (initKeyAction()) {
            if (anchor == null)
              setTargetAnchor(cursor);
            else
              swapTargets(anchor, cursor);
          }
        }
      };

      AbstractAction insertTabAction = new AbstractAction(DefaultEditorKit.insertTabAction) {
        public void actionPerformed(ActionEvent e) {
          if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0)
            prevTargetAction.actionPerformed(e);
          else
            nextTargetAction.actionPerformed(e);
        }
      };

      AbstractAction beginAction = new AbstractAction(DefaultEditorKit.beginAction) {
        public void actionPerformed(ActionEvent e) {
          if (initKeyAction()) {
            setTargetCursor(playDoc.tmb.getElement(0));
          }
        }
      };

      AbstractAction endAction = new AbstractAction(DefaultEditorKit.endAction) {
        public void actionPerformed(ActionEvent e) {
          if (initKeyAction()) {
            setTargetCursor(playDoc.tmb.getElement(playDoc.tmb.size() - 1));
          }
        }
      };

      AbstractAction upAction = new AbstractAction(DefaultEditorKit.upAction) {
        public void actionPerformed(ActionEvent e) {
          prevTargetAction.actionPerformed(e);
        }
      };

      AbstractAction downAction = new AbstractAction(DefaultEditorKit.downAction) {
        public void actionPerformed(ActionEvent e) {
          nextTargetAction.actionPerformed(e);
        }
      };

      protected void setActions() {

        java.util.Map<String, Object[]> actionKeys = Actions.getActionKeys(this);
        ActionMap am = new ActionMap();
        am.setParent(getActionMap());
        setActionMap(am);
        // Originals:
        Actions.mapTraceAction(this, actionKeys, DefaultEditorKit.beepAction);
        Actions.mapTraceAction(this, actionKeys, "requestFocus");
        Actions.mapTraceAction(this, actionKeys, "toggle-componentOrientation");

        // Derived:
        Actions.mapAction(this, actionKeys, insertBreakAction);
        Actions.mapAction(this, actionKeys, insertTabAction);
        Actions.mapAction(this, actionKeys, forwardAction);
        Actions.mapAction(this, actionKeys, backwardAction);
        Actions.mapAction(this, actionKeys, beginAction);
        Actions.mapAction(this, actionKeys, endAction);
        Actions.mapAction(this, actionKeys, upAction);
        Actions.mapAction(this, actionKeys, downAction);

        Actions.mapAction(this, actionKeys, nextTargetAction);
        Actions.mapAction(this, actionKeys, prevTargetAction);

        am.setParent(null);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, java.awt.Event.SHIFT_MASK),
            insertTabAction.getValue(Action.NAME));
      }
    }

    protected void setTargetAnchor(TargetMarker tm) {
      if (anchor != null)
        playDoc.applyStyleToTarget(anchor, TextActivityDocument.TARGET, false, true);

      anchor = tm;
      if (anchor != null)
        playDoc.applyStyleToTarget(anchor, TextActivityDocument.TARGET, true, true);
    }

    protected void setTargetCursor(TargetMarker tm) {
      if (tm != null && tm == anchor) {
        cursor = tm;
        return;
      }
      if (cursor != null) {
        if (cursor != anchor)
          playDoc.applyStyleToTarget(cursor, TextActivityDocument.TARGET, false, true);
      }
      cursor = tm;
      if (cursor != null)
        playDoc.applyStyleToTarget(cursor, null, true, true);
    }

    protected void swapTargets(TargetMarker src, TargetMarker dest) {
      if (src == dest || !playing || playDoc == null)
        return;
      setTargetAnchor(null);
      TargetMarkerBag tmb = playDoc.tmb;
      tmb.swapTargets(src, dest);
      src.checkText(ev);
      boolean ok = dest.checkText(ev);
      nActions++;
      if (!hasCheckButton) {
        int solvedTargets = tmb.countSolvedTargets();
        ps.reportNewAction(getActivity(), ACTION_PLACE, src.getCurrentText(), Integer.toString(tmb.indexOf(dest)), ok,
            solvedTargets);
        if (ok && solvedTargets == tmb.size()) {
          finishActivity(true);
        } else {
          playEvent(ok ? EventSounds.ACTION_OK : EventSounds.ACTION_ERROR);
        }
      }
      setTargetCursor(dest);
    }

    @Override
    protected void doCheck(boolean fromButton) {
      if (playDoc == null || playDoc.tmb.size() == 0)
        return;

      TargetMarkerBag tmb = playDoc.tmb;

      if (bc.active)
        bc.end();

      for (int i = 0; i < tmb.size(); i++) {
        TargetMarker tm = tmb.getElement(i);
        playDoc.applyStyleToTarget(tm, tm.target.targetStatus == TextTarget.SOLVED ? TextActivityDocument.TARGET
            : TextActivityDocument.TARGET_ERROR, false, true);
      }
      int tagsSolved = tmb.countSolvedTargets();
      ps.setCounterValue(SCORE_COUNTER, tagsSolved);
      ps.setCounterValue(ACTIONS_COUNTER, nActions);
      if (tagsSolved != tmb.size()) {
        if (fromButton) {
          playEvent(EventSounds.FINISHED_ERROR);
          pane.requestFocus();
        }
      } else
        finishActivity(true);
    }

    @Override
    public void finishActivity(boolean result) {
      if (bc.active)
        bc.end();
      pane.setEnabled(false);
      super.finishActivity(result);
    }
  }
}
