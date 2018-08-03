/*
 * File    : FillInBlanks.java
 * Created : 01-jun-2001 8:57
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

package edu.xtec.jclic.activities.text;

import edu.xtec.jclic.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.Actions;
import edu.xtec.util.JDomUtility;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class FillInBlanks extends TextActivityBase {

  protected boolean autoJump;
  protected boolean forceOkToAdvance;
  protected Evaluator ev;

  /** Creates new FillInBlanks */
  public FillInBlanks(JClicProject project) {
    super(project);
    autoJump = false;
    forceOkToAdvance = false;
    ev = new ComplexEvaluator(project);
  }

  public static final String AUTO_JUMP = "autoJump", FORCE_OK_TO_ADVANCE = "forceOkToAdvance";

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();
    if (autoJump)
      e.setAttribute(AUTO_JUMP, JDomUtility.boolString(autoJump));
    if (forceOkToAdvance)
      e.setAttribute(FORCE_OK_TO_ADVANCE, JDomUtility.boolString(forceOkToAdvance));
    e.addContent(ev.getJDomElement());
    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    super.setProperties(e, aux);
    ev = Evaluator.getEvaluator(e.getChild(Evaluator.ELEMENT_NAME), project);
    autoJump = JDomUtility.getBoolAttr(e, AUTO_JUMP, autoJump);
    forceOkToAdvance = JDomUtility.getBoolAttr(e, FORCE_OK_TO_ADVANCE, forceOkToAdvance);
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    ((ComplexEvaluator) ev).setProperties(c3a);
    forceOkToAdvance = c3a.okToNext;
    autoJump = !c3a.avNoSalta;
    hasCheckButton = !c3a.avCont;
    // evalOnTheFly=c3a.avCont;
    // if(evalOnTheFly)
    // hasCheckButton=false;
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

    boolean locked;
    TextActivityDocument playDoc = null;

    protected Panel(PlayStation ps) {
      super(ps);
      locked = true;
    }

    @Override
    protected void initDocument() throws Exception {
      if (tad != null) {
        playing = false;
        tad.tmb.setCurrentTarget(null, this);
        tad.tmb.reset();
        playDoc = new TextActivityDocument(styleContext);
        tad.cloneDoc(playDoc, false, true, false);
        pane.setStyledDocument(playDoc);
        playDoc.attachTo(pane, FillInBlanks.Panel.this);
        tad.tmb.setParentPane(pane);
        pane.setEnabled(true);
        if (playDoc.tmb.size() > 0) {
          pane.setEditable(true);
          pane.requestFocus();
          pane.getCaret().setVisible(true);
          setCaretPos(0);
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
      FillInBlanksPane fp = new FillInBlanksPane();
      fp.setActions();
      return fp;
    }

    class FillInBlanksPane extends TextActivityPane {

      protected FillInBlanksPane() {
        super(FillInBlanks.Panel.this);
      }

      @Override
      public boolean processMouse(MouseEvent e) {
        if (super.processMouse(e) && e.getID() == MouseEvent.MOUSE_PRESSED && playing && !locked) {
          int p = this.viewToModel(e.getPoint());
          if (p >= 0)
            setCaretPos(p);
        }
        return false;
      }

      protected void removeChar(int offset) throws BadLocationException {
        TargetMarker tm = playDoc.tmb.getCurrentTarget();
        if (tm == null || !tm.contains(offset, false)) {
          playEvent(EventSounds.ACTION_ERROR);
          return;
        }
        boolean lastChar = (tm.getLength() == 1);
        playDoc.remove(offset, 1);
        tm.target.setModified(true);
        if (lastChar) {
          playDoc.insertString(tm.begOffset, tm.target.getFillString(1), playDoc.getFillAttributeSet());
          getCaret().setDot(tm.begOffset);
        } else {
          tm.endOffset--;
          getCaret().setDot(offset);
        }
        tm.setPositions();
        playDoc.tmb.updateOffsets();
      }

      protected void removeCharCatch(int offset) {
        try {
          removeChar(offset);
        } catch (BadLocationException ex) {
          System.err.println("Text activity error:\n" + ex);
        }
      }

      protected void insertChar(int offset, char ch) throws BadLocationException {
        TargetMarker tm = playDoc.tmb.getCurrentTarget();
        if (tm == null || !tm.contains(offset, true) || ch < 0x20)
          return;

        int firstFill = getFirstFillChar(tm);
        if (firstFill >= 0) {
          playDoc.remove(firstFill, 1);
          if (offset > firstFill)
            offset--;
        } else if (tm.getLength() >= tm.target.maxLenResp) {
          playEvent(EventSounds.ACTION_ERROR);
          return;
        }

        playDoc.insertString(offset, new String(new char[] { ch }), playDoc.getTargetAttributeSet());
        tm.target.setModified(true);

        if (firstFill < 0)
          tm.endOffset++;
        tm.setPositions();
        playDoc.tmb.updateOffsets();

        if (autoJump && tm.getCurrentText(TextActivityDocument.FILL).length() >= tm.target.maxLenResp) {
          getCaret().setDot(goToTarget(playDoc.tmb.getNextTarget(tm), -1));
        } else {
          getCaret().setDot(offset + 1);
        }
      }

      protected void insertCharCatch(int offset, char ch) {
        try {
          insertChar(offset, ch);
        } catch (BadLocationException ex) {
          System.err.println("Text activity error:\n" + ex);
        }
      }

      protected int getFirstFillChar(TargetMarker tm) {
        for (int i = tm.begOffset; i < tm.endOffset; i++)
          if (playDoc.checkBooleanAttribute(i, TextActivityDocument.FILL))
            return i;
        return -1;
      }

      boolean readyForActions() {
        return playing && !locked && isEditable() && isEnabled();
      }

      // Actions
      AbstractAction defaultKeyTypedAction = new AbstractAction(DefaultEditorKit.defaultKeyTypedAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            String content = e.getActionCommand();
            int mod = e.getModifiers();
            if ((content != null) && (content.length() > 0)
                && ((mod & ActionEvent.ALT_MASK) == (mod & ActionEvent.CTRL_MASK))) {
              char c = content.charAt(0);
              if ((c >= 0x20) && (c != 0x7F)) {
                insertCharCatch(getCaret().getDot(), c);
              }
            }
          }
        }
      };

      AbstractAction forwardAction = new AbstractAction(DefaultEditorKit.forwardAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            int k = getCaret().getDot();
            setCaretPos(k + 1, k, true);
          }
        }
      };

      AbstractAction backwardAction = new AbstractAction(DefaultEditorKit.backwardAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            int k = getCaret().getDot();
            setCaretPos(k - 1, k, false);
          }
        }
      };

      AbstractAction nextTargetAction = new AbstractAction("next-target") {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            getCaret().setDot(goToTarget(playDoc.tmb.getNextTarget(null), -1));
          }
        }
      };

      AbstractAction prevTargetAction = new AbstractAction("prev-target") {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            getCaret().setDot(goToTarget(playDoc.tmb.getPrevTarget(null), -1));
          }
        }
      };

      AbstractAction insertBreakAction = new AbstractAction(DefaultEditorKit.insertBreakAction) {
        public void actionPerformed(ActionEvent e) {
          insertTabAction.actionPerformed(e);
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

      AbstractAction deletePrevCharAction = new AbstractAction(DefaultEditorKit.deletePrevCharAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            removeCharCatch(getCaret().getDot() - 1);
          }
        }
      };

      AbstractAction deleteNextCharAction = new AbstractAction(DefaultEditorKit.deleteNextCharAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            removeCharCatch(getCaret().getDot());
          }
        }
      };

      AbstractAction beginWordAction = new AbstractAction(DefaultEditorKit.beginWordAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            TargetMarker tm = playDoc.tmb.getCurrentTarget();
            if (tm != null)
              setCaretPos(tm.begOffset);
          }
        }
      };

      AbstractAction endWordAction = new AbstractAction(DefaultEditorKit.endWordAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            TargetMarker tm = playDoc.tmb.getCurrentTarget();
            if (tm != null)
              setCaretPos(tm.endOffset);
          }
        }
      };

      AbstractAction beginAction = new AbstractAction(DefaultEditorKit.beginAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            if ((e.getModifiers() & ActionEvent.CTRL_MASK) != 0)
              setCaretPos(goToTarget(playDoc.tmb.getElement(0), -1));
            else
              beginWordAction.actionPerformed(e);
          }
        }
      };

      AbstractAction endAction = new AbstractAction(DefaultEditorKit.endAction) {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            if ((e.getModifiers() & ActionEvent.CTRL_MASK) != 0)
              setCaretPos(goToTarget(playDoc.tmb.getElement(playDoc.tmb.size() - 1), -1));
            else
              endWordAction.actionPerformed(e);
          }
        }
      };

      AbstractAction showHelpAction = new AbstractAction("show-help") {
        public void actionPerformed(ActionEvent e) {
          if (readyForActions()) {
            TargetMarker tm = playDoc.tmb.getCurrentTarget();
            if (tm != null && tm.target != null && tm.target.popupContent != null
                && tm.target.infoMode == TextTarget.INFO_ON_DEMAND)
              tm.target.checkPopup(Panel.this, tm, true);
          }
        }
      };

      Action kitUpAction = null;
      AbstractAction upAction = new AbstractAction(DefaultEditorKit.upAction) {
        public void actionPerformed(ActionEvent e) {
          if (kitUpAction != null && readyForActions()) {
            int bkPos = getCaret().getDot();
            kitUpAction.actionPerformed(e);
            int newPos = getCaret().getDot();
            getCaret().setDot(bkPos);
            setCaretPos(newPos, bkPos, false);
          }
        }
      };

      Action kitDownAction = null;
      AbstractAction downAction = new AbstractAction(DefaultEditorKit.downAction) {
        public void actionPerformed(ActionEvent e) {
          if (kitDownAction != null && readyForActions()) {
            int bkPos = getCaret().getDot();
            kitDownAction.actionPerformed(e);
            int newPos = getCaret().getDot();
            getCaret().setDot(bkPos);
            setCaretPos(newPos, bkPos, true);
          }
        }
      };

      protected void setActions() {

        kitUpAction = getActionMap().get(DefaultEditorKit.upAction);
        kitDownAction = getActionMap().get(DefaultEditorKit.downAction);

        java.util.Map<String, Object[]> actionKeys = Actions.getActionKeys(this);
        ActionMap am = new ActionMap();
        am.setParent(getActionMap());
        setActionMap(am);
        // Originals:
        Actions.mapTraceAction(this, actionKeys, DefaultEditorKit.beepAction);
        Actions.mapTraceAction(this, actionKeys, "requestFocus");
        Actions.mapTraceAction(this, actionKeys, "toggle-componentOrientation");

        // Derivats:
        Actions.mapAction(this, actionKeys, defaultKeyTypedAction);
        Actions.mapAction(this, actionKeys, insertBreakAction);
        Actions.mapAction(this, actionKeys, insertTabAction);
        Actions.mapAction(this, actionKeys, deletePrevCharAction);
        Actions.mapAction(this, actionKeys, deleteNextCharAction);
        Actions.mapAction(this, actionKeys, forwardAction);
        Actions.mapAction(this, actionKeys, backwardAction);
        Actions.mapAction(this, actionKeys, beginWordAction);
        Actions.mapAction(this, actionKeys, endWordAction);
        Actions.mapAction(this, actionKeys, beginAction);
        Actions.mapAction(this, actionKeys, endAction);
        Actions.mapAction(this, actionKeys, upAction);
        Actions.mapAction(this, actionKeys, downAction);

        Actions.mapAction(this, actionKeys, showHelpAction);

        Actions.mapAction(this, actionKeys, nextTargetAction);
        Actions.mapAction(this, actionKeys, prevTargetAction);

        Actions.mapAction(this, actionKeys, nextTargetAction, DefaultEditorKit.nextWordAction);
        Actions.mapAction(this, actionKeys, prevTargetAction, DefaultEditorKit.previousWordAction);

        Actions.mapAction(this, actionKeys, nextTargetAction, DefaultEditorKit.endParagraphAction);
        Actions.mapAction(this, actionKeys, prevTargetAction, DefaultEditorKit.beginParagraphAction);

        Actions.mapAction(this, actionKeys, nextTargetAction, DefaultEditorKit.beginLineAction);
        Actions.mapAction(this, actionKeys, prevTargetAction, DefaultEditorKit.endLineAction);

        Actions.mapAction(this, actionKeys, nextTargetAction, DefaultEditorKit.pageDownAction);
        Actions.mapAction(this, actionKeys, prevTargetAction, DefaultEditorKit.pageUpAction);

        am.setParent(null);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, java.awt.Event.SHIFT_MASK),
            insertTabAction.getValue(Action.NAME));
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0),
            showHelpAction.getValue(Action.NAME));
      }

      @Override
      protected void targetChanged(TextTarget tt) {
        if (readyForActions()) {
          TargetMarker tm = playDoc.tmb.getElement(tt);
          if (tm != null) {
            TargetMarker currentTm = playDoc.tmb.getCurrentTarget();
            goToTarget(tm, -1);
            if (currentTm != tm && !hasCheckButton) {
              tm.target.setModified(true);
              goToTarget(tm, -1);
            }
          }
        }
      }
    }

    protected boolean evalTarget(TargetMarker tm) {
      if (tm.target == null)
        return false;
      String src = tm.getCurrentText(TextActivityDocument.FILL);
      if (src == null || src.length() < 1)
        return false;
      byte[] result = ev.evalText(src, tm.target.answer);
      boolean ok = Evaluator.isOk(result);
      tm.target.targetStatus = ok ? TextTarget.SOLVED : TextTarget.WITH_ERROR;
      markTarget(tm, result);
      return ok;
    }

    protected void markTarget(TargetMarker tm, byte[] attributes) {
      if (tm.target.comboList != null) {
        tm.target.comboList.checkColors();
        tm.target.comboList.repaint();
        return;
      }
      int bgTarget = tm.begPos.getOffset();
      int endTarget = tm.endPos.getOffset();
      int p = 0;
      for (int i = bgTarget; i < endTarget && p < attributes.length; i++) {
        if (!playDoc.checkBooleanAttribute(i, TextActivityDocument.FILL)) {
          playDoc.setCharacterAttributes(i, 1,
              styleContext.getStyle(
                  attributes[p] == Evaluator.FLAG_OK ? TextActivityDocument.TARGET : TextActivityDocument.TARGET_ERROR),
              false);
          p++;
        }
      }
    }

    protected void setCaretPos(int offset) {
      setCaretPos(offset, pane.getCaret().getDot(), true);
    }

    protected void setCaretPos(int offset, int currentOffset, boolean searchForward) {
      int check = checkCaretPos(offset, searchForward);
      if (check == -1)
        pane.setEditable(false);
      else if (check != currentOffset)
        pane.setCaretPosition(check);
    }

    protected int checkCaretPos(int offset, boolean searchForward) {
      TargetMarkerBag tmb = playDoc.tmb;
      TargetMarker tm = tmb.getElementByOffset(offset, true);
      int p = offset;
      if (tm == null) {
        tm = tmb.getNearestElement(offset, searchForward);
        if (tm == null)
          tm = tmb.getNearestElement(offset, !searchForward);

        if (tm == null) {
          pane.setEditable(false);
          locked = true;
          return -1;
        } else {
          if (searchForward)
            p = tm.begPos.getOffset();
          else
            p = tm.endPos.getOffset();
        }
      }

      if (tmb.getCurrentTarget() != tm || tm.target.comboList != null)
        p = goToTarget(tm, p);

      return p;
    }

    protected int goToTarget(TargetMarker tm, int offset) {
      int p = offset;
      TargetMarkerBag tmb = playDoc.tmb;
      if (tmb.getCurrentTarget() != null && !hasCheckButton) {
        TargetMarker currentTm = tmb.getCurrentTarget();
        if (!hasCheckButton && currentTm != null && (currentTm.target.isModified() || forceOkToAdvance)) {
          boolean ok = evalTarget(currentTm);
          if (!ok)
            currentTm.target.checkPopup(this, currentTm, false);
          int solvedTargets = tmb.countSolvedTargets();
          ps.reportNewAction(getActivity(), ACTION_WRITE, currentTm.getCurrentText(TextActivityDocument.FILL),
              currentTm.target.getAnswers(), ok, solvedTargets);
          if (ok && solvedTargets == tmb.size()) {
            finishActivity(true);
          } else {
            playEvent(ok ? EventSounds.ACTION_OK : EventSounds.ACTION_ERROR);
          }

          if (forceOkToAdvance && !ok) {
            tm = currentTm;
            tmb.setCurrentTarget(tm, this);
            return tm.begPos.getOffset();
          }
        }
      }
      if (p < 0)
        p = tm.begPos.getOffset();
      if (!tm.contains(p, true))
        p = tm.begOffset;

      tmb.setCurrentTarget(tm, this);
      return p;
    }

    @Override
    protected void doCheck(boolean fromButton) {
      if (playDoc == null || locked)
        return;

      TargetMarkerBag tmb = playDoc.tmb;
      if (tmb.getCurrentTarget() != null && !hasCheckButton) {
        goToTarget(tmb.getCurrentTarget(), -1);
        return;
      }
      int solvedTargets = 0;
      for (int i = 0; i < tmb.size(); i++) {
        TargetMarker tm = tmb.getElement(i);
        if (tm != null && tm.target.targetStatus != TextTarget.NOT_EDITED) {
          boolean ok = evalTarget(tm);
          solvedTargets = tmb.countSolvedTargets();
          ps.reportNewAction(getActivity(), ACTION_WRITE, tm.getCurrentText(TextActivityDocument.FILL),
              tm.target.getAnswers(), ok, solvedTargets);
        }
      }
      if (solvedTargets != tmb.size()) {
        if (fromButton) {
          playEvent(EventSounds.FINISHED_ERROR);

          if (tmb.getCurrentTarget() != null)
            goToTarget(tmb.getCurrentTarget(), -1);
        }
      } else
        finishActivity(true);
    }

    @Override
    public void finishActivity(boolean result) {
      pane.setEditable(false);
      pane.setEnabled(false);
      if (playDoc != null && playDoc.tmb.getCurrentTarget() != null)
        ;
      playDoc.tmb.getCurrentTarget().lostFocus(this);
      super.finishActivity(result);
    }
  }
}
