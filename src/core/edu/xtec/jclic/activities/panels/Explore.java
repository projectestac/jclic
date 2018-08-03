/*
 * File    : Explore.java
 * Created : 27-apr-2001 12:49
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

package edu.xtec.jclic.activities.panels;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.bags.ActivitySequenceElement;
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
public class Explore extends Activity implements ActiveBagContentKit.Compatible {

  /** Creates new Explore */
  public Explore(JClicProject project) {
    super(project);
    boxGridPos = AB;
    abc = new ActiveBagContent[2];
    // for(int i=0; i<2; i++)
    // abc[i]=null;
    scramble[0] = false;
    bTimeCounter = bScoreCounter = bActionsCounter = false;
    includeInReports = false;
    reportActions = false;
  }

  @Override
  public void initNew() {
    super.initNew();
    abc[0] = ActiveBagContent.initNew(3, 2, '1', true, false, 50, 30);
    abc[1] = ActiveBagContent.initNew(1, 6, 'A');
  }

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element ex;

    if (abc[0] == null || abc[1] == null)
      return null;

    org.jdom.Element e = super.getJDomElement();

    e.addContent(abc[0].getJDomElement().setAttribute(ID, PRIMARY));
    e.addContent(abc[1].getJDomElement().setAttribute(ID, SECONDARY));

    ex = new org.jdom.Element(SCRAMBLE);
    {
      ex.setAttribute(TIMES, Integer.toString(shuffles));
      ex.setAttribute(PRIMARY, JDomUtility.boolString(scramble[0]));
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
    if (abc[0] == null || abc[1] == null)
      throw new IllegalArgumentException("Explore activity without content!");

    if ((child = e.getChild(SCRAMBLE)) != null) {
      shuffles = JDomUtility.getIntAttr(child, TIMES, shuffles);
      scramble[0] = JDomUtility.getBoolAttr(child, PRIMARY, scramble[0]);
    }

    if ((child = e.getChild(LAYOUT)) != null)
      boxGridPos = JDomUtility.getStrIndexAttr(child, POSITION, LAYOUT_NAMES, boxGridPos);
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    for (int i = 0; i < 2; i++) {
      abc[i] = c3a.createActiveBagContent(i);
      abc[i].setBoxBase(c3a.getBoxBase(i));
    }
    // Clic3 bug
    abc[1].border = false;
    abc[0].setIds(c3a.ass);
    boxGridPos = c3a.graPos;
    // Clic3 bug:
    // scramble=c3a.bar[0];
    scramble[0] = false;

    bScoreCounter = bActionsCounter = false;
    includeInReports = false;
    reportActions = false;
  }

  @Override
  public boolean mustPauseSequence() {
    return true;
  }

  public int getMinNumActions() {
    return 0;
  }

  @Override
  public boolean hasRandom() {
    return true;
  }

  public Activity.Panel getActivityPanel(PlayStation ps) {
    return new Panel(ps);
  }

  class Panel extends Activity.Panel {

    ActiveBoxBag[] bg = new ActiveBoxBag[2];

    protected Panel(PlayStation ps) {
      super(ps);
      for (int i = 0; i < 2; i++)
        bg[i] = null;
    }

    public void clear() {
      for (int i = 0; i < 2; i++)
        if (bg[i] != null) {
          bg[i].end();
          bg[i] = null;
        }
    }

    @Override
    public void buildVisualComponents() throws Exception {

      if (firstRun)
        super.buildVisualComponents();

      clear();

      if (abc[0] != null && abc[1] != null) {
        // consider the use of assIds
        if (acp != null)
          acp.generateContent(new ActiveBagContentKit(abc[0].nch, abc[0].ncw, abc, true), ps);

        bg[0] = ActiveBoxGrid.createEmptyGrid(null, this, margin, margin, abc[0]);
        // Clic3 behavior!!!
        double w = abc[1].w;
        if (boxGridPos == AUB || boxGridPos == BUA)
          w = abc[0].getTotalWidth();
        bg[1] = new ActiveBoxGrid(null, this, margin, margin, w, abc[1].h, new edu.xtec.jclic.shapers.Rectangular(1, 1),
            abc[1].bb);

        bg[0].setContent(abc[0]);
        bg[0].setDefaultIdAss();
        bg[1].getActiveBox(0).setInactive(false);
        bg[0].setVisible(true);
        bg[1].setVisible(true);
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
      if (bg[0] != null && bg[1] != null) {
        if (scramble[0])
          shuffle(new ActiveBoxBag[] { bg[0] }, true, true);
        if (useOrder)
          currentItem = bg[0].getNextItem(-1);
        playing = true;
      }
    }

    public void render(Graphics2D g2, Rectangle dirtyRegion) {
      for (int i = 0; i < 2; i++)
        if (bg[i] != null)
          bg[i].update(g2, dirtyRegion, this);
    }

    public Dimension setDimension(Dimension preferredMaxSize) {
      if (bg[0] == null || bg[1] == null || getSize().equals(preferredMaxSize))
        return preferredMaxSize;
      return BoxBag.layoutDouble(preferredMaxSize, bg[0], bg[1], boxGridPos, margin);
    }

    @Override
    public void processMouse(MouseEvent e) {
      ActiveBox bx1, bx2;
      boolean m = false;

      if (playing)
        switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
          ps.stopMedia(1);
          if ((bx1 = bg[0].findActiveBox(e.getPoint())) != null) {
            bx2 = bg[1].getActiveBox(0);
            if (bx1.idAss != -1 && (!useOrder || bx1.idOrder == currentItem)) {
              bx2.setContent(abc[1], bx1.idAss);
              if (!bx2.playMedia(ps) && !bx1.playMedia(ps))
                playEvent(EventSounds.CLICK);
              if (useOrder)
                currentItem = bg[0].getNextItem(currentItem);
              ps.reportNewAction(getActivity(), ACTION_SELECT, bx1.getDescription(), bx2.getDescription(), true, 0);

              // Clic 3.0 behavior:
              if (bx1.idAss >= 0 && bx1.idAss < abc[1].getNumCells()) {
                ActivitySequenceElement ase = project.activitySequence.getCurrentAct();
                if (ase != null && project.activitySequence.hasNextAct(true) && ase.delay > 0
                    && (project.activitySequence.getNavButtonsFlag() & ActivitySequenceElement.NAV_FWD) == 0) {
                  finishActivity(true);
                }
              }
            } else {
              bx2.clear();
              bx2.setInactive(false);
            }
          }
          break;
        }
    }
  }
}
