/*
 * File    : CrossWord.java
 * Created : 04-oct-2001 19:53
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

package edu.xtec.jclic.activities.textGrid;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JToggleButton;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class CrossWord extends Activity implements ActiveBagContentKit.Compatible {

  public static final int NO_ADVANCE = 0, ADVANCE_RIGHT = 1, ADVANCE_DOWN = 2;
  public static final int LABEL_WIDTH = 40;

  public boolean wildTransparent;
  public boolean upperCase;
  public boolean checkCase;

  /** Creates new CrossWords */
  public CrossWord(JClicProject project) {
    super(project);
    boxGridPos = AB;
    abc = new ActiveBagContent[2];
    wildTransparent = false;
    upperCase = true;
    checkCase = true;
  }

  @Override
  public void initNew() {
    super.initNew();
    abc[0] = ActiveBagContent.initNew(3, 1, -1, true, true, 200, 60);
    abc[1] = ActiveBagContent.initNew(3, 1, -1, true, true, 200, 60);
    tgc = TextGridContent.initNew(3, 3, 'A');
  }

  public static final String ACROSS_CLUES = "acrossClues", DOWN_CLUES = "downClues",
      WILD_TRANSPARENT = "wildTransparent", UPPERCASE = "upperCase", CHECK_CASE = "checkCase";

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element child;

    if (tgc == null || abc[0] == null || abc[1] == null)
      return null;

    org.jdom.Element e = super.getJDomElement();

    e.addContent(tgc.getJDomElement());
    e.addContent(abc[0].getJDomElement().setAttribute(ID, ACROSS_CLUES));
    e.addContent(abc[1].getJDomElement().setAttribute(ID, DOWN_CLUES));
    child = new org.jdom.Element(LAYOUT);
    child.setAttribute(POSITION, LAYOUT_NAMES[boxGridPos]);
    if (wildTransparent)
      child.setAttribute(WILD_TRANSPARENT, JDomUtility.boolString(wildTransparent));
    if (!upperCase)
      child.setAttribute(UPPERCASE, JDomUtility.boolString(upperCase));
    if (!checkCase)
      child.setAttribute(CHECK_CASE, JDomUtility.boolString(checkCase));
    e.addContent(child);

    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    org.jdom.Element child;

    super.setProperties(e, aux);

    if ((child = e.getChild(TextGridContent.ELEMENT_NAME)) == null)
      throw new IllegalArgumentException("CrossWord without TextGrid");
    tgc = TextGridContent.getTextGridContent(child);

    java.util.Iterator it = e.getChildren(ActiveBagContent.ELEMENT_NAME).iterator();
    while (it.hasNext()) {
      child = (org.jdom.Element) it.next();
      ActiveBagContent bc = ActiveBagContent.getActiveBagContent(child, project.mediaBag);
      String id = JDomUtility.getStringAttr(child, ID, null, false);
      if (ACROSS_CLUES.equals(id))
        abc[0] = bc;
      else if (DOWN_CLUES.equals(id))
        abc[1] = bc;
      else
        throw new IllegalArgumentException("Unknown clues: " + id);
    }
    if (abc[0] == null || abc[1] == null)
      throw new IllegalArgumentException("CrossWord without H or V clues!");

    if ((child = e.getChild(LAYOUT)) != null) {
      boxGridPos = JDomUtility.getStrIndexAttr(child, POSITION, LAYOUT_NAMES, boxGridPos);
      wildTransparent = JDomUtility.getBoolAttr(child, WILD_TRANSPARENT, wildTransparent);
      upperCase = JDomUtility.getBoolAttr(child, UPPERCASE, upperCase);
      checkCase = JDomUtility.getBoolAttr(child, CHECK_CASE, checkCase);
    }
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    boxGridPos = c3a.graPos;
    tgc = new TextGridContent();
    tgc.nch = c3a.nctxh;
    tgc.ncw = c3a.nctxw;
    tgc.w = c3a.txtCW;
    tgc.h = c3a.txtCH;
    tgc.border = true;
    tgc.text = c3a.graTxt;
    tgc.bb = c3a.getBoxBase(0);

    for (int k = 0; k < 2; k++) {
      abc[k] = new ActiveBagContent(1, 1);
      int i, j, l;
      for (i = 0, l = 0; i < c3a.tags[k].length; i++) {
        j = 0;
        java.util.StringTokenizer st = new java.util.StringTokenizer(c3a.tags[k][i], ";");
        while (st.hasMoreElements()) {
          ActiveBoxContent bc = abc[k].getActiveBoxContent(l++);
          c3a.setActiveBoxTextContent(bc, st.nextToken());
          bc.id = i;
          bc.item = j++;
        }
      }
      abc[k].bb = c3a.getBoxBase(1);
      abc[k].ncw = 1;
      abc[k].nch = i;
      abc[k].w = 200;
      abc[k].h = 75;
    }
  }

  public int getMinNumActions() {
    return (tgc == null ? 0 : tgc.getNumChars() - tgc.countWildChars());
  }

  @Override
  public boolean helpSolutionAllowed() {
    return false;
  }

  @Override
  public boolean needsKeyboard() {
    return true;
  }

  public Activity.Panel getActivityPanel(PlayStation ps) {
    return new Panel(ps);
  }

  class Panel extends Activity.Panel implements ActionListener, FocusListener {
    TextGrid grid;
    BoxBag bb;
    int advance;
    int numLetters;
    ActiveBox hClue, vClue;
    JToggleButton hClueBtn, vClueBtn;

    protected Panel(PlayStation ps) {
      super(ps);
      grid = null;
      bb = null;
      advance = NO_ADVANCE;
      numLetters = 0;
      addFocusListener(this);
    }

    public void clear() {
      if (grid != null) {
        grid.end();
        grid = null;
      }
      if (bb != null) {
        bb.end();
        bb = null;
      }
    }

    private BoxBag createBoxBag(int n) {
      BoxBag bxb = new BoxBag(null, this, null);
      SimpleBox sb = new SimpleBox(bxb, null, null);
      sb.setBounds(0, 0, LABEL_WIDTH, abc[n].h);
      sb.setBorder(true);
      JToggleButton tgbtn = new JToggleButton(
          edu.xtec.util.ResourceManager.getImageIcon(n == 0 ? "buttons/textright.png" : "buttons/textdown.png"));
      tgbtn.addActionListener(this);
      javax.swing.border.Border border = tgbtn.getBorder();
      sb.setHostedComponent(tgbtn);
      tgbtn.setBorder(border);
      bxb.addBox(sb);

      ActiveBox ab = new ActiveBox(bxb, null, n,
          new java.awt.geom.Rectangle2D.Double(LABEL_WIDTH, 0, abc[n].w, abc[n].h), null);
      bxb.addBox(ab);
      bxb.setBoxBase(abc[n].bb);

      if (n == 0) {
        hClue = ab;
        hClueBtn = tgbtn;
      } else {
        vClue = ab;
        vClueBtn = tgbtn;
      }
      return bxb;
    }

    @Override
    public void buildVisualComponents() throws Exception {

      if (firstRun)
        super.buildVisualComponents();

      clear();

      if (acp != null && abc != null)
        acp.generateContent(new ActiveBagContentKit(0, 0, abc, false), ps);

      if (tgc != null) {
        grid = TextGrid.createEmptyGrid(null, this, margin, margin, tgc, wildTransparent);

        bb = new BoxBag(null, this, null);

        BoxBag bxbh = createBoxBag(0);
        BoxBag bxbv = createBoxBag(1);
        if (boxGridPos == AUB || boxGridPos == BUA)
          bxbv.setLocation(bxbh.getWidth() + margin, 0);
        else
          bxbv.setLocation(0, bxbh.getHeight() + margin);
        bb.addBox(bxbh);
        bb.addBox(bxbv);

        grid.setVisible(true);
        bb.setVisible(true);
        invalidate();
      }
    }

    @Override
    public void initActivity() throws Exception {
      super.initActivity();

      if (!firstRun)
        buildVisualComponents();
      else
        firstRun = false;

      setAndPlayMsg(MAIN, EventSounds.START);
      if (grid != null) {
        grid.setChars(tgc.text);
        numLetters = getMinNumActions();
        grid.setCellAttributes(true, true);
        grid.setCursorEnabled(true);
        setCursorAt(0, 0);
        advance = ADVANCE_RIGHT;
        hClueBtn.setSelected(true);
        requestFocus();
        playing = true;
      }
    }

    public int getCurrentScore() {
      return grid == null ? 0 : grid.countCoincidences(checkCase);
    }

    public void render(Graphics2D g2, Rectangle dirtyRegion) {
      if (grid != null)
        grid.update(g2, dirtyRegion, this);
      if (bb != null)
        bb.update(g2, dirtyRegion, this);
    }

    public Dimension setDimension(Dimension preferredMaxSize) {
      if (grid == null || bb == null || getSize().equals(preferredMaxSize))
        return preferredMaxSize;
      else
        return BoxBag.layoutDouble(preferredMaxSize, grid, bb, boxGridPos, margin);
    }

    @Override
    public void processMouse(MouseEvent e) {
      Point p = e.getPoint();

      if (playing)
        switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
          ps.stopMedia(1);
          if (grid.contains(p)) {
            Point pt = grid.getLogicalCoords(p);
            if (pt != null) {
              setCursorAt(pt.x, pt.y);
            }
          } else if (hClue.contains(p))
            hClue.playMedia(ps);
          else if (vClue.contains(p))
            vClue.playMedia(ps);
          break;
        }
    }

    protected void moveCursor(int dx, int dy) {
      if (grid != null) {
        grid.moveCursor(dx, dy, true);
        cursorPosChanged();
      }
    }

    protected void setCursorAt(int x, int y) {
      grid.setCursorAt(x, y, true);
      cursorPosChanged();
    }

    protected void cursorPosChanged() {
      Point pt = grid.getCursor();
      if (pt != null && bb != null) {
        Point items = grid.getItemFor(pt.x, pt.y);
        if (items != null) {
          hClue.setContent(abc[0].getActiveBoxContentWith(pt.y, items.x));
          vClue.setContent(abc[1].getActiveBoxContentWith(pt.x, items.y));
        }
      }
    }

    @Override
    public void processKey(KeyEvent e) {
      if (playing && grid != null)
        switch (e.getID()) {
        case KeyEvent.KEY_TYPED:
          Point cur = grid.getCursor();
          char ch = e.getKeyChar();
          if (ch >= 0 && cur != null) {
            if (upperCase)
              ch = Character.toUpperCase(ch);
            grid.setCharAt(cur.x, cur.y, ch);
            boolean ok = grid.isCellOk(cur.x, cur.y, checkCase);
            int r = getCurrentScore();
            ps.reportNewAction(getActivity(), ACTION_WRITE, String.copyValueOf(new char[] { ch }),
                "X:" + cur.x + " Y:" + cur.y, ok, r);
            if (r == numLetters) {
              grid.setCursorEnabled(false);
              grid.stopCursorBlink();
              finishActivity(true);
            } else {
              playEvent(EventSounds.CLICK);
              if (advance == ADVANCE_RIGHT)
                moveCursor(1, 0);
              else if (advance == ADVANCE_DOWN)
                moveCursor(0, 1);
            }
          }
          break;

        case KeyEvent.KEY_PRESSED:
          int dx = 0, dy = 0;
          switch (e.getKeyCode()) {
          case KeyEvent.VK_RIGHT:
            dx = 1;
            break;
          case KeyEvent.VK_LEFT:
            dx = -1;
            break;
          case KeyEvent.VK_DOWN:
            dy = 1;
            break;
          case KeyEvent.VK_UP:
            dy = -1;
            break;
          }
          if (dx != 0 || dy != 0)
            moveCursor(dx, dy);
          break;
        }
    }

    public void focusGained(FocusEvent e) {
      if (playing && grid != null)
        grid.startCursorBlink();
    }

    public void focusLost(FocusEvent e) {
      if (grid != null)
        grid.stopCursorBlink();
    }

    public void actionPerformed(ActionEvent ev) {
      advance = NO_ADVANCE;
      if (ev.getSource().equals(hClueBtn)) {
        if (hClueBtn.isSelected())
          advance = ADVANCE_RIGHT;
        vClueBtn.setSelected(false);
      } else if (ev.getSource().equals(vClueBtn)) {
        if (vClueBtn.isSelected())
          advance = ADVANCE_DOWN;
        hClueBtn.setSelected(false);
      }
      requestFocus();
    }
  }
}
