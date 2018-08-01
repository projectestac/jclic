/*
 * File    : AnimatedActiveBox.java
 * Created : 10-jan-2002 15:41
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

package edu.xtec.jclic.boxes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * This class is currently used only in some {@link edu.xtec.jclic.skins.Skin}
 * classes to animate progress bars. It takes a collection of images (stored as
 * elements of an {@link edu.xtec.jclic.boxes.ActiveBagContent}) that are used
 * as "frames" of a picture, and displayed one after other. It uses a
 * {@link java.util.Timer} to generate the events that cause the rotation of
 * frames.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class AnimatedActiveBox extends ActiveBox implements ActionListener {

  protected ActiveBagContent abc = null;
  protected ActiveBagContent altAbc = null;

  protected Timer timer = null;
  protected int delay = MIN_DELAY;
  protected boolean running = false;
  protected int currentFrame = 0;
  private boolean waitingForFirstFrame = false;
  protected long startTime = 0L;
  protected int startDelay = 0;

  public static final int MIN_DELAY = 50;

  /** Creates new AnimatedActiveBox */
  public AnimatedActiveBox(AbstractBox parent, JComponent container, BoxBase boxBase) {
    super(parent, container, boxBase);
  }

  public AnimatedActiveBox(AbstractBox parent, JComponent container, int setIdLoc, Rectangle2D r, BoxBase boxBase) {
    super(parent, container, setIdLoc, r, boxBase);
  }

  public void setStartDelay(int startDelay) {
    this.startDelay = startDelay;
  }

  public void setDelay(int delay) {
    this.delay = delay;
  }

  @Override
  public void clear() {
    super.clear();
  }

  public void setContent(ActiveBagContent abc) {
    this.abc = abc;
    if (abc != null && !abc.isEmpty())
      setContent(abc.getActiveBoxContent(0));
  }

  public void setAltContent(ActiveBagContent abc) {
    this.altAbc = abc;
  }

  private void buildTimer() {
    if (timer != null) {
      timer.stop();
      timer = null;
    }
    if (delay >= MIN_DELAY) {
      timer = new Timer(delay, this);
      timer.setRepeats(true);
      timer.setCoalesce(true);
    }
  }

  public void start() {
    stop(false);
    if (timer == null) {
      buildTimer();
    }
    if (timer != null) {
      startTime = System.currentTimeMillis();
      waitingForFirstFrame = false;
      if (!timer.isRunning())
        timer.start();
    }
  }

  public void stop(boolean toFirstFrame) {
    if (timer != null && timer.isRunning()) {
      if (toFirstFrame)
        waitingForFirstFrame = true;
      else {
        waitingForFirstFrame = false;
        timer.stop();
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    ActiveBagContent c = isAlternative() ? altAbc : abc;
    if (c != null && c.getNumCells() > 0) {
      currentFrame++;
      currentFrame %= c.getNumCells();
      if (currentFrame == 0 && waitingForFirstFrame) {
        waitingForFirstFrame = false;
        timer.stop();
      } else {
        if (isAlternative())
          setAltContent(c, currentFrame);
        else
          setContent(c, currentFrame);
      }
      if (!isVisible() && startDelay > 0 && (System.currentTimeMillis() - startTime) >= startDelay)
        setVisible(true);
    }
  }
}
