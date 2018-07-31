/*
 * File    : MemoryGame.java
 * Created : 30-apr-2001 14:00
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

package edu.xtec.jclic.activities.memory;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class MemoryGame extends Activity implements ActiveBagContentKit.Compatible {

  /** Creates new MemoryGame */
  public MemoryGame(JClicProject project) {
    super(project);
    boxGridPos = AB;
    abc = new ActiveBagContent[2];
  }

  @Override
  public void initNew() {
    super.initNew();
    abc[0] = ActiveBagContent.initNew(3, 2, 'A');
  }

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element ex;

    if (abc[0] == null)
      return null;

    org.jdom.Element e = super.getJDomElement();

    e.addContent(abc[0].getJDomElement().setAttribute(ID, PRIMARY));
    if (abc[1] != null)
      e.addContent(abc[1].getJDomElement().setAttribute(ID, SECONDARY));

    ex = new org.jdom.Element(SCRAMBLE);
    {
      ex.setAttribute(TIMES, Integer.toString(shuffles));
      e.addContent(ex);
    }

    ex = new org.jdom.Element(LAYOUT);
    ex.setAttribute(POSITION, LAYOUT_NAMES[boxGridPos]);
    e.addContent(ex);

    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    org.jdom.Element child;
    super.setProperties(e, aux);
    ActiveBagContent bag;
    abc[1] = null;
    java.util.Iterator itr = e.getChildren(ActiveBagContent.ELEMENT_NAME).iterator();
    while (itr.hasNext()) {
      child = ((org.jdom.Element) itr.next());
      bag = ActiveBagContent.getActiveBagContent(child, project.mediaBag);
      String id = JDomUtility.getStringAttr(child, ID, PRIMARY, false);
      if (PRIMARY.equals(id))
        abc[0] = bag;
      else if (SECONDARY.equals(id))
        abc[1] = bag;
    }
    if (abc[0] == null)
      throw new IllegalArgumentException("Memory game without ActiveBagContent");

    if ((child = e.getChild(SCRAMBLE)) != null) {
      shuffles = JDomUtility.getIntAttr(child, TIMES, shuffles);
    }

    if ((child = e.getChild(LAYOUT)) != null)
      boxGridPos = JDomUtility.getStrIndexAttr(child, POSITION, LAYOUT_NAMES, boxGridPos);
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    abc[0] = c3a.createActiveBagContent(0);
    abc[0].setBoxBase(c3a.getBoxBase(0));
    boxGridPos = c3a.graPos;
  }

  public int getMinNumActions() {
    return abc[0] == null ? 0 : abc[0].getNumCells();
  }

  @Override
  public boolean hasRandom() {
    return true;
  }

  @Override
  public boolean shuffleAlways() {
    return true;
  }

  public Activity.Panel getActivityPanel(PlayStation ps) {
    return new Panel(ps);
  }

  class Panel extends Activity.Panel {

    ActiveBoxBag bg;

    protected Panel(PlayStation ps) {
      super(ps);
      bc = new BoxConnector(this);
    }

    public void clear() {
      if (bg != null) {
        bg.end();
        bg = null;
      }
    }

    @Override
    public void buildVisualComponents() throws Exception {

      if (firstRun)
        super.buildVisualComponents();

      clear();

      if (abc[0] != null) {

        if (acp != null) {
          ActiveBagContent[] abcPass = abc[1] == null ? new ActiveBagContent[] { abc[0] }
              : new ActiveBagContent[] { abc[0], abc[1] };

          acp.generateContent(new ActiveBagContentKit(abc[0].nch, abc[0].ncw, abcPass, false), ps);
        }

        int ncw = abc[0].ncw;
        int nch = abc[0].nch;
        if (boxGridPos == AB || boxGridPos == BA)
          ncw *= 2;
        else
          nch *= 2;

        bg = new ActiveBoxGrid(null, this, margin, margin, abc[0].w * ncw, abc[0].h * nch,
            new edu.xtec.jclic.shapers.Rectangular(ncw, nch), abc[0].bb);

        int nc = abc[0].getNumCells();
        bg.setBorder(abc[0].border);
        bg.setContent(abc[0], null, 0, 0, nc);
        bg.setContent((abc[1] != null ? abc[1] : abc[0]), null, 0, nc, nc);
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < nc; j++) {
            ActiveBox bx = bg.getActiveBox(i * nc + j);
            bx.setIdAss(j);
            bx.setInactive(true);
          }
        }
        bg.setVisible(true);
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
      if (bg != null) {
        shuffle(new ActiveBoxBag[] { bg }, false, true);
        playing = true;
      }
    }

    public void render(Graphics2D g2, Rectangle dirtyRegion) {
      if (bg != null)
        bg.update(g2, dirtyRegion, this);
      if (bc.active)
        bc.update(g2, dirtyRegion, this);
    }

    public Dimension setDimension(Dimension preferredMaxSize) {
      if (bg == null || getSize().equals(preferredMaxSize))
        return preferredMaxSize;
      return BoxBag.layoutSingle(preferredMaxSize, bg, margin);
    }

    @Override
    public void processMouse(MouseEvent e) {
      ActiveBox bx1, bx2;
      Point p = e.getPoint();
      boolean m;

      if (playing)
        switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
          ps.stopMedia(1);
          if (bc.active) {
            bc.end();
            if ((bx1 = bg.findActiveBox(bc.origin)) != null && (bx2 = bg.findActiveBox(/* bc.dest */ p)) != null
                && bx1.idAss != -1 && bx2.idAss != -1) {
              if (bx1 != bx2) {
                boolean ok = false;
                if (bx1.idAss == bx2.idAss || bx1.getContent().isEquivalent(bx2.getContent(), true)) {
                  ok = true;
                  bx1.setIdAss(-1);
                  bx2.setIdAss(-1);
                  bx1.setInactive(false);
                  bx2.setInactive(false);
                } else {
                  bx1.setInactive(true);
                  if (dragCells)
                    bx2.setInactive(true);
                  else {
                    bx2.setInactive(false);
                    bc.begin(p);
                  }
                }
                m = bx2.playMedia(ps);
                int cellsAtPlace = bg.countCellsWithIdAss(-1);
                ps.reportNewAction(getActivity(), ACTION_MATCH, bx1.getDescription(), bx2.getDescription(), ok,
                    cellsAtPlace / 2);
                if (ok && cellsAtPlace == bg.getNumCells())
                  finishActivity(true);
                else if (!m)
                  playEvent(ok ? EventSounds.ACTION_OK : EventSounds.ACTION_ERROR);
              } else {
                playEvent(EventSounds.CLICK);
                bx1.setInactive(true);
              }
            } else if (bx1 != null) {
              bx1.setInactive(true);
            }
            repaint();
          } else {
            if ((bx1 = bg.findActiveBox(p)) != null && bx1.idAss != -1) {
              if (dragCells)
                bc.begin(p, bx1);
              else
                bc.begin(p);
              m = bx1.playMedia(ps);
              if (!m)
                playEvent(EventSounds.CLICK);
              bx1.setInactive(false);
            }
          }
          break;

        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_DRAGGED:
          bc.moveTo(p);
          break;
        }
    }
  }
}
