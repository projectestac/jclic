/*
 * File    : ActiveBagContentPreviewPanel.java
 * Created : 10-oct-2002 10:57
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

package edu.xtec.jclic.boxes;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.Constants;
import edu.xtec.jclic.activities.textGrid.CrossWord;
import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.util.ResizerPanel;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ActiveBagContentPreviewPanel extends ResizerPanel implements FocusListener {

  public static final int MARGIN = 10;

  AbstractBox[] bg = new AbstractBox[2];
  ActiveBoxBag[] abg = new ActiveBoxBag[2];
  TextGrid[] grid = new TextGrid[2];
  ActiveBagContent[] abc = new ActiveBagContent[2];
  ActiveBagContent[] altAbc = new ActiveBagContent[2];
  ActiveBagContent[][] allAbc = new ActiveBagContent[][] { abc, altAbc };
  TextGridContent[] tgc = new TextGridContent[2];
  MediaBagEditor mbe;
  boolean[] allowResize = new boolean[2];
  boolean dragCursorX = false, dragCursorY = false;
  boolean dragging = false;
  int margin = MARGIN;
  private int cgrid = 0;
  int boxGridPos = Activity.AB;
  ActiveBagContentEditor parent;
  int editMode;
  int currentLine, highlightLine;
  BoxConnector bc;
  boolean showAllArrows = true;
  boolean crossWord = false;
  ActiveBox hClue, vClue;
  Color softLineColor = Color.blue;
  Color activeLineColor = Color.red;
  Color arrowColor = BoxConnector.DEFAULT_XOR_COLOR;
  public static final int EDIT_GRIDS = 0, EDIT_LINKS = 1, EDIT_BOOL = 2;
  public static final float ARROW_WIDTH = 2.0F;

  /** Creates a new instance of ActiveBagContentPreviewPanel */
  public ActiveBagContentPreviewPanel(ActiveBagContentEditor parent, boolean isCrossWord) {
    this.parent = parent;
    crossWord = isCrossWord;
    bc = new BoxConnector(this);
    bc.arrow = true;
    bc.line_width = ARROW_WIDTH;
    bc.lineColor = activeLineColor;
    currentLine = -1;
    highlightLine = -1;
    editMode = EDIT_GRIDS;
    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    addFocusListener(this);
  }

  /*
   * ATENCIO: DEPRECATED A Java 1.4!!
   *
   * public boolean isFocusTraversable(){ if(grid[0]!=null || grid[1]!=null)
   * return true; return super.isFocusTraversable(); }
   */

  public AbstractBox getAbstractBox(int index) {
    return bg[index];
  }

  public boolean isCrossWord() {
    return crossWord;
  }

  public void setActiveBagContent(int index, ActiveBagContent abcx, ActiveBagContent altAbcx, TextGridContent tgcx) {
    abc[index] = abcx;
    altAbc[index] = altAbcx;
    tgc[index] = tgcx;
    bg[index] = null;
    grid[index] = null;
    abg[index] = null;
    if (tgc[index] != null) {
      grid[index] = TextGrid.createEmptyGrid(null, this, margin, margin, tgc[index], false);
      bg[index] = grid[index];
      grid[index].setChars(tgc[index].text);
      if (crossWord)
        grid[index].setCellAttributes(true, false);
      grid[index].setCursorAt(0, 0, false);
      grid[index].setCursorEnabled(true);
      checkCursor(false);
      allowResize[index] = true;
    } else if (crossWord && index == 1 && abc[index] != null && altAbc[index] != null) {
      abg[1] = new ActiveBoxBag(null, this, abc[1].bb);
      bg[1] = abg[1];
      ActiveBox ab = new ActiveBox(abg[1], this, null);
      ActiveBoxContent abct = new ActiveBoxContent();
      abct.setImgContent(edu.xtec.util.ResourceManager.getImageIcon("buttons/textright.png").getImage(), null);
      ab.setContent(abct);
      abg[1].addActiveBox(ab);
      hClue = new ActiveBox(abg[1], this, null);
      abg[1].addActiveBox(hClue);
      ab = new ActiveBox(abg[1], this, null);
      abct = new ActiveBoxContent();
      abct.setImgContent(edu.xtec.util.ResourceManager.getImageIcon("buttons/textdown.png").getImage(), null);
      ab.setContent(abct);
      abg[1].addActiveBox(ab);
      vClue = new ActiveBox(abg[1], this, null);
      abg[1].addActiveBox(vClue);
      abg[1].setBorder(true);
      resizeCrossWordPanel((int) abc[1].w, (int) abc[1].h);
      cursorPosChanged();
      allowResize[1] = true;
    } else if (abc[index] != null) {
      abg[index] = ActiveBoxGrid.createEmptyGrid(null, this, margin, margin, abc[index]);
      bg[index] = abg[index];
      abg[index].setContent(abc[index], altAbc[index]);
      allowResize[index] = (abc[index].img == null);
    }

    if (bg[index] != null)
      bg[index].setVisible(true);
    /*
     * if(index==0 && bg[index]!=null && abcx!=null){
     * activeLineColor=bg[index].getBoxBaseResolve().textColor;
     * bc.lineColor=activeLineColor; }
     */
    if (bc.active)
      bc.end();
    currentLine = -1;
    highlightLine = -1;
    dragCursorX = false;
    dragCursorY = false;
    setCursor(null);
    revalidate();
    BoxBase.resetAllFonts();
    repaint();
  }

  protected void resizeCrossWordPanel(int w, int h) {
    if (crossWord && abg[1] != null && abg[1].getNumCells() == 4) {
      boolean rowDist = (boxGridPos == Activity.AUB || boxGridPos == Activity.BUA);
      int x = (int) (abg[1].getX());
      int y = (int) (abg[1].getY());

      ActiveBox ab = abg[1].getActiveBox(0);
      ab.setBounds(x, y, CrossWord.LABEL_WIDTH, h);
      ab = abg[1].getActiveBox(1);
      ab.setBounds(x + CrossWord.LABEL_WIDTH, y, w, h);
      int x0 = rowDist ? x + CrossWord.LABEL_WIDTH + w : x;
      int y0 = rowDist ? y : y + h;

      ab = abg[1].getActiveBox(2);
      ab.setBounds(x0, y0, CrossWord.LABEL_WIDTH, h);
      ab = abg[1].getActiveBox(3);
      ab.setBounds(x0 + CrossWord.LABEL_WIDTH, y0, w, h);

      abg[1].recalcSize();
      /*
       * abg[1].width = rowDist ? (2*CrossWord.LABEL_WIDTH)+2*w :
       * CrossWord.LABEL_WIDTH+w; abg[1].height = rowDist ? h : 2*h;
       */
    }
  }

  @Override
  public void doLayout() {
    if (bg[0] != null) {
      if (bg[1] != null)
        BoxBag.layoutDouble(getSize(), (Resizable) bg[0], (Resizable) bg[1], boxGridPos, margin);
      else
        BoxBag.layoutSingle(getSize(), (Resizable) bg[0], margin);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (bg[0] != null) {
      Graphics2D g2 = (Graphics2D) g;
      RenderingHints rh = g2.getRenderingHints();
      g2.setRenderingHints(Constants.DEFAULT_RENDERING_HINTS);
      while (true) {
        BoxBase.flagFontReduced = false;
        for (int i = 0; i < 2; i++)
          if (bg[i] != null)
            bg[i].update(g2, g2.getClipBounds(), this);
        if (!BoxBase.flagFontReduced)
          break;
      }

      if (editMode == EDIT_LINKS && abg[1] != null && abc[0] != null) {
        for (int i = 0; i < abc[0].getNumCells(); i++) {
          if (showAllArrows && i != currentLine && i != highlightLine)
            drawArrow(g2, i);
        }
        if (highlightLine >= 0)
          drawArrow(g2, highlightLine);

        bc.update(g2, g2.getClipBounds(), this);
      }
      g2.setRenderingHints(rh);
    }
  }

  private void drawArrow(Graphics2D g2, int i) {
    if (abg[0] == null || abg[1] == null)
      return;
    ActiveBox bx = abg[0].getActiveBox(i);
    int id = abc[0].getActiveBoxContent(i).id;
    if (id >= 0 && id < abc[1].getNumCells()) {
      ActiveBox bx2 = abg[1].getActiveBox(id);
      if (bx != null && bx2 != null) {
        BoxConnector.drawLine(g2, new Point2D.Double(bx.getX() + bx.getWidth() / 2, bx.getY() + bx.getHeight() / 2),
            new Point2D.Double(bx2.getX() + bx2.getWidth() / 2, bx2.getY() + bx2.getHeight() / 2), true,
            (i == highlightLine ? activeLineColor : softLineColor), arrowColor, BoxConnector.ARROW_L,
            BoxConnector.ARROW_ANGLE, (i == highlightLine ? ARROW_WIDTH : BoxConnector.LINE_WIDTH));
      }
    }
  }

  public void setMediaBagEditor(MediaBagEditor mbe) {
    this.mbe = mbe;
  }

  @Override
  protected void processEvent(AWTEvent e) {
    boolean consumed = false;
    if (bg[0] != null) {
      if (e instanceof MouseEvent) {
        switch (editMode) {
        case EDIT_GRIDS:
          consumed = processMouseGrids((MouseEvent) e);
          break;
        case EDIT_LINKS:
          consumed = processMouseLinks((MouseEvent) e);
          break;
        case EDIT_BOOL:
          consumed = processMouseBool((MouseEvent) e);
          break;
        }
      } else if (e instanceof KeyEvent && (grid[0] != null || grid[1] != null))
        consumed = processKey((KeyEvent) e);
    }
    if (!consumed)
      super.processEvent(e);
  }

  private boolean processMouseGrids(MouseEvent ev) {
    boolean consumed = false;
    Point p = ev.getPoint();
    int id = ev.getID();
    int index = cgrid;
    if (!dragging && (id == MouseEvent.MOUSE_MOVED || id == MouseEvent.MOUSE_PRESSED)) {
      if (bg[0] != null && bg[0].contains(p))
        index = 0;
      else if (bg[1] != null && bg[1].contains(p))
        index = 1;
      else
        index = -1;
    }

    AbstractBox xbox = index >= 0 ? bg[index] : null;
    ActiveBoxBag xbg = (abg[0] == xbox ? abg[0] : (abg[1] == xbox ? abg[1] : null));
    TextGrid xgrid = (grid[0] == xbox ? grid[0] : (grid[1] == xbox ? grid[1] : null));

    switch (ev.getID()) {
    case MouseEvent.MOUSE_MOVED:
      if (!dragging) {
        Cursor newCursor = null;
        if (xbox != null && allowResize[index]) {
          dragCursorX = Math.abs(p.x - xbox.x - xbox.width) < 5 && p.y >= xbox.y && p.y < (xbox.y + xbox.height + 5);
          dragCursorY = Math.abs(p.y - xbox.y - xbox.height) < 5 && p.x >= xbox.x && p.x < (xbox.x + xbox.width + 5);
          if (dragCursorX && dragCursorY)
            newCursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
          else if (dragCursorY)
            newCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
          else if (dragCursorX)
            newCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
        }
        if (newCursor == null && xbg != null && xbg.findActiveBox(p) != null)
          newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

        setCursor(newCursor);
      }
      break;

    case MouseEvent.MOUSE_PRESSED:
      if (xbox != null) {
        setPanelSelected(index);
        if (dragCursorX || dragCursorY) {
          dragging = true;
          resizeByDrag(p, false);
        } else if (xgrid != null) {
          if (xgrid.contains(p)) {
            Point pt = xgrid.getLogicalCoords(p);
            if (pt != null) {
              setCursorAt(pt.x, pt.y);
            }
          }
        }
        consumed = true;
      }
      break;

    case MouseEvent.MOUSE_RELEASED:
      if (xbox != null) {
        if (dragging) {
          resizeByDrag(p, true);
          dragging = false;
        } else if (xbg != null) {
          ActiveBox ab = xbg.findActiveBox(p);
          if (ab != null) {
            ActiveBoxContent abxcnt = ab.getCurrentContent();
            int group = -1;
            int bag = -1;
            int item = -1;
            for (int i = 0; i < allAbc.length && item < 0; i++) {
              for (int j = 0; j < allAbc[i].length && item < 0; j++) {
                if (allAbc[i][j] != null) {
                  item = allAbc[i][j].indexOf(abxcnt);
                  if (item >= 0) {
                    bag = j;
                    group = i;
                  }
                }
              }
            }
            if (item >= 0) {
              ActiveBoxContent abxc = ActiveBoxContentEditor.getActiveBoxContent(abxcnt, this, parent.getOptions(), mbe,
                  ab);
              if (abxc != null) {
                allAbc[group][bag].setActiveBoxContentAt(abxc, item);
                setModified(true);
                if (!ab.isAlternative()) {
                  ab.setContent(abxc);
                } else {
                  ab.setAltContent(abxc);
                }
                BoxBase.resetAllFonts();
                repaint();
              }
            }
          }
        }
        consumed = true;
      }
      break;

    case MouseEvent.MOUSE_DRAGGED:
      if (xbox != null && dragging) {
        resizeByDrag(p, false);
        consumed = true;
      }
      break;
    }
    return consumed;
  }

  protected boolean processMouseLinks(MouseEvent ev) {
    boolean consumed = false;
    if (abg[0] != null && abg[1] != null) {
      Point pt = ev.getPoint();
      switch (ev.getID()) {
      case MouseEvent.MOUSE_DRAGGED:
      case MouseEvent.MOUSE_MOVED:
        if (bc.active)
          bc.moveTo(pt);
        else {
          int newLine = -1;
          ActiveBox bx = abg[0].findActiveBox(pt);
          if (bx != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            newLine = bx.idOrder;
          } else
            setCursor(null);

          if (newLine != highlightLine) {
            highlightLine = newLine;
            repaint();
          }
        }
        consumed = true;
        break;

      case MouseEvent.MOUSE_PRESSED:
        if (bc.active) {
          bc.end();
          ActiveBox bx = abg[1].findActiveBox(bc.dest);
          ActiveBoxContent bc0 = abc[0].getActiveBoxContent(currentLine);
          bc0.id = (bx == null ? -1 : bx.idOrder);
          highlightLine = currentLine;
          currentLine = -1;
          setModified(true);
          repaint();
        } else {
          ActiveBox bx = abg[0].findActiveBox(pt);
          if (bx != null) {
            highlightLine = -1;
            currentLine = bx.idOrder;
            repaint();
            bc.begin(pt);
          }
        }
        consumed = true;
        break;
      }
    }
    return consumed;
  }

  private void resizeByDrag(Point p, boolean finish) {
    if (cgrid >= 0 && bg[cgrid] != null) {
      int ncw = 0, nch = 0;
      if (abg[cgrid] == bg[cgrid] && abc[cgrid] != null) {
        if (crossWord) {
          ncw = 1;
          nch = 1;
        } else {
          ncw = abc[cgrid].ncw;
          nch = abc[cgrid].nch;
        }
      } else if (grid[cgrid] == bg[cgrid] && tgc[cgrid] != null) {
        ncw = tgc[cgrid].ncw;
        nch = tgc[cgrid].nch;
      }
      if (ncw > 0 && nch > 0) {
        int w = (int) (bg[cgrid].width / ncw);
        int h = (int) (bg[cgrid].height / nch);
        if (dragCursorX)
          w = (int) ((p.x - bg[cgrid].x) / ncw);
        if (dragCursorY)
          h = (int) ((p.y - bg[cgrid].y) / nch);
        if (crossWord && cgrid == 1) {
          if (boxGridPos == Activity.AUB || boxGridPos == Activity.BUA) {
            w = (w - 2 * 40) / 2;
          } else {
            w = w - 40;
            h = h / 2;
          }
        }
        if (w > 10 && h > 10) {
          doResize(cgrid, w, h, finish);
          if (finish && crossWord)
            cursorPosChanged();
        }
      }
    }

    /*
     * if(cgrid>=0 && bg[cgrid]!=null && ((abc[cgrid]!=null && abc[cgrid].ncw>0 &&
     * abc[cgrid].nch>0) || (tgc[cgrid]!=null && tgc[cgrid].ncw>0 &&
     * tgc[cgrid].nch>0))){ int ncw = tgc[cgrid]!=null ? tgc[cgrid].ncw :
     * abc[cgrid].ncw; int nch = tgc[cgrid]!=null ? tgc[cgrid].nch : abc[cgrid].nch;
     * int w=(int)(bg[cgrid].width/ncw); int h=(int)(bg[cgrid].height/nch);
     * if(dragCursorX) w=(int)((p.x-bg[cgrid].x)/ncw); if(dragCursorY)
     * h=(int)((p.y-bg[cgrid].y)/nch); if(w>10 && h>10){ doResize(cgrid, w, h,
     * finish); } }
     */

  }

  protected void doResize(int index, int w, int h, boolean finish) {
    AbstractBox xbox = (index >= 0 ? bg[index] : null);
    if (xbox != null) {
      setModified(true);
      if (tgc[index] != null) {
        tgc[index].w = w;
        tgc[index].h = h;
      } else if (abc[index] != null) {
        abc[index].w = w;
        abc[index].h = h;
      }

      if (crossWord && index == 1) {
        altAbc[index].w = w;
        altAbc[index].h = h;
        resizeCrossWordPanel(w, h);
      } else {
        int ncw = tgc[index] != null ? tgc[index].ncw : abc[index].ncw;
        int nch = tgc[index] != null ? tgc[index].nch : abc[index].nch;
        Rectangle r = new Rectangle((int) bg[index].getX(), (int) bg[index].getY(), w * ncw, h * nch);
        xbox.setBounds(r);
      }
      if (finish) {
        /*
         * setActiveBagContent(index, abc[index], altAbc[index], tgc[index]);
         * ActiveBoxBag xbg=(xbox==abg[0] ? abg[0] : (xbox==abg[1] ? abg[1] : null));
         * if(xbg!=null && xbg.isAlternative()) xbg.setAlternative(true);
         */

        boolean xbgAlt;
        ActiveBoxBag xbg = (xbox == abg[0] ? abg[0] : (xbox == abg[1] ? abg[1] : null));
        xbgAlt = xbg != null && xbg.isAlternative();
        setActiveBagContent(index, abc[index], altAbc[index], tgc[index]);
        if (xbgAlt && abg[index] != null)
          abg[index].setAlternative(true);
      }
      parent.resized(index);
    }
    repaint();
  }

  protected boolean processMouseBool(MouseEvent ev) {
    boolean consumed = false;
    if (abg[0] != null) {
      ActiveBox bx = abg[0].findActiveBox(ev.getPoint());
      switch (ev.getID()) {
      case MouseEvent.MOUSE_MOVED:
        setCursor(bx != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null);
        consumed = true;
        break;

      case MouseEvent.MOUSE_RELEASED:
        if (bx != null) {
          int v = bx.getContent().id;
          v = v > 0 ? 0 : 1;
          bx.getContent().id = v;
          bx.setInverted(v > 0);
          bx.setMarked(v > 0);
          setModified(true);
          consumed = true;
        }
        break;
      }
    }
    return consumed;
  }

  public void setModified(boolean modified) {
    parent.setModified(modified);
  }

  /**
   * Getter for property boxGridPos.
   *
   * @return Value of property boxGridPos.
   */
  public int getBoxGridPos() {
    return boxGridPos;
  }

  /**
   * Setter for property boxGridPos.
   *
   * @param boxGridPos New value of property boxGridPos.
   */
  public void setBoxGridPos(int boxGridPos) {
    this.boxGridPos = boxGridPos;
    if (crossWord && abc[1] != null) {
      resizeCrossWordPanel((int) abc[1].w, (int) abc[1].h);
    }
    BoxBase.resetAllFonts();
    revalidate();
  }

  /**
   * Getter for property editMode.
   *
   * @return Value of property editMode.
   */
  public int getEditMode() {
    return editMode;
  }

  /**
   * Setter for property editMode.
   *
   * @param editMode New value of property editMode.
   */
  public void setEditMode(int editMode) {
    boolean checkBool = (editMode == EDIT_BOOL || this.editMode == EDIT_BOOL);
    this.editMode = editMode;
    if (checkBool && abg[0] != null) {
      for (int i = 0; i < abg[0].getNumCells(); i++) {
        ActiveBox bx = abg[0].getActiveBox(i);
        if (bx != null) {
          bx.setInverted(editMode == EDIT_BOOL ? bx.getContent().id > 0 : false);
          bx.setMarked(editMode == EDIT_BOOL ? bx.getContent().id > 0 : false);
        }
      }
    }
    repaint();
  }

  /**
   * Getter for property showAllArrows.
   *
   * @return Value of property showAllArrows.
   */
  public boolean isShowAllArrows() {
    return showAllArrows;
  }

  /**
   * Setter for property showAllArrows.
   *
   * @param showAllArrows New value of property showAllArrows.
   */
  public void setShowAllArrows(boolean showAllArrows) {
    this.showAllArrows = showAllArrows;
    repaint();
  }

  protected void moveCursor(int dx, int dy) {
    if (cgrid >= 0 && grid[cgrid] != null) {
      grid[cgrid].moveCursor(dx, dy, false);
      cursorPosChanged();
    }
  }

  protected void setCursorAt(int x, int y) {
    if (cgrid >= 0 && grid[cgrid] != null)
      grid[cgrid].setCursorAt(x, y, false);
    cursorPosChanged();
  }

  protected void cursorPosChanged() {
    if (crossWord && grid[0] != null && hClue != null && vClue != null) {
      Point pt = grid[0].getCursor();
      if (pt != null) {
        Point items = grid[0].getItemFor(pt.x, pt.y);
        if (items != null) {
          ActiveBoxContent abxc = abc[1].getActiveBoxContentWith(pt.y, items.x);
          if (abxc == null) {
            abc[1].insertActiveBoxContentWith(pt.y, items.x);
            abxc = abc[1].getActiveBoxContentWith(pt.y, items.x);
          }
          hClue.setContent(abxc);

          abxc = altAbc[1].getActiveBoxContentWith(pt.x, items.y);
          if (abxc == null) {
            altAbc[1].insertActiveBoxContentWith(pt.x, items.y);
            abxc = altAbc[1].getActiveBoxContentWith(pt.x, items.y);
          }
          vClue.setContent(abxc);
        }
      }
    }
  }

  public boolean processKey(KeyEvent e) {
    boolean consumed = false;
    if (cgrid >= 0 && grid[cgrid] != null && tgc[cgrid] != null) {
      int dx = 0, dy = 0;
      boolean delete = false, moveFirst = false;
      Point cur = grid[cgrid].getCursor();
      char ch1 = 0;

      switch (e.getID()) {
      case KeyEvent.KEY_PRESSED:
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
        case KeyEvent.VK_BACK_SPACE:
          dx = -1;
          delete = true;
          moveFirst = true;
          break;
        case KeyEvent.VK_DELETE:
          delete = true;
          break;
        }
        break;

      case KeyEvent.KEY_TYPED:
        char ch0 = e.getKeyChar();
        int kk = e.getKeyCode();
        if (cur != null) {
          dx = 1;
          if (Character.isLetterOrDigit(ch0))
            ch1 = Character.toUpperCase(ch0);
          else if (ch0 == tgc[cgrid].wild || Character.isSpaceChar(ch0))
            delete = true;
          else
            dx = 0;
        }
        break;
      }
      if (moveFirst && (dx != 0 || dy != 0)) {
        moveCursor(dx, dy);
        cur = grid[cgrid].getCursor();
        consumed = true;
      }
      if (delete)
        ch1 = tgc[cgrid].wild;
      if (ch1 != 0 && cur != null) {
        if (crossWord) {
          checkWildChanges(cur, ch1);
        }
        grid[cgrid].setCharAt(cur.x, cur.y, ch1);
        tgc[cgrid].setCharAt(cur.x, cur.y, ch1);
        setModified(true);
        consumed = true;
      }
      if (!moveFirst && (dx != 0 || dy != 0)) {
        moveCursor(dx, dy);
        consumed = true;
      }
    }
    return consumed;
  }

  protected void checkWildChanges(Point pt, char ch) {
    if (!crossWord)
      return;

    char oldCh = grid[cgrid].getCharAt(pt.x, pt.y);
    boolean deleteWild = (oldCh == tgc[cgrid].wild && ch != oldCh);
    boolean addWild = (!deleteWild && ch == tgc[cgrid].wild && ch != oldCh);
    if (addWild || deleteWild) {
      Point items = grid[cgrid].getItemFor(pt.x, pt.y);
      if (items != null) {
        boolean intoWhitesH = grid[cgrid].isIntoWhites(pt, true);
        boolean intoWhitesV = grid[cgrid].isIntoWhites(pt, false);
        boolean intoBlacksH = grid[cgrid].isIntoBlacks(pt, true);
        boolean intoBlacksV = grid[cgrid].isIntoBlacks(pt, false);
        if (deleteWild) {
          if (intoWhitesH) {
            abc[1].deleteActiveBoxContentWith(pt.y, items.x);
          } else if (intoBlacksH) {
            abc[1].insertActiveBoxContentWith(pt.y, items.x);
          }
          if (intoWhitesV) {
            altAbc[1].deleteActiveBoxContentWith(pt.x, items.y);
          } else if (intoBlacksV) {
            altAbc[1].insertActiveBoxContentWith(pt.x, items.y);
          }
        } else {
          if (intoWhitesH) {
            abc[1].insertActiveBoxContentWith(pt.y, items.x);
          } else if (intoBlacksH) {
            abc[1].deleteActiveBoxContentWith(pt.y, items.x);
          }
          if (intoWhitesV) {
            altAbc[1].insertActiveBoxContentWith(pt.x, items.y);
          } else if (intoBlacksV) {
            altAbc[1].deleteActiveBoxContentWith(pt.x, items.y);
          }
        }
        grid[cgrid].setCellLocked(pt.x, pt.y, addWild);
      }
    }
  }

  public void focusGained(FocusEvent e) {
    checkCursor(false);
  }

  public void focusLost(FocusEvent e) {
    checkCursor(true);
  }

  public void checkCursor(boolean forceLost) {
    for (int i = 0; i < 2; i++) {
      if (grid[i] != null) {
        if (cgrid == i && !forceLost && hasFocus())
          grid[i].startCursorBlink();
        else
          grid[i].stopCursorBlink();
      }
    }
  }

  private void setPanelSelected(int index) {
    cgrid = index;
    parent.panelSelected(cgrid);
    if (!hasFocus())
      requestFocus();
    else
      checkCursor(false);
  }
}
