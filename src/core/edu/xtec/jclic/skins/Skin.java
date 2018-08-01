/*
 * File    : Skin.java
 * Created : 11-oct-2001 16:09
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

package edu.xtec.jclic.skins;

import edu.xtec.jclic.*;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public abstract class Skin extends JPanel implements ProgressInputStream.ProgressInputStreamListener {

  public static final int DEFAULT_PROGRESS_WAKE_ON = 1000;
  public static final String RESOURCE_FOLDER = "skins";
  public static final String RESOURCE_LIST_FILE = "listskins.properties";

  public static final int MAIN = 0, AUX = 1, MEM = 2, NUM_MSG_AREAS = 3;
  public static final String[] msgAreaNames = { "main", "aux", "mem" };

  public String name;
  public String fileName;
  protected AbstractButton[] buttons = new AbstractButton[Constants.NUM_ACTIONS];
  protected Counter[] counters = new Counter[Constants.NUM_COUNTERS];
  protected ActiveBox[] msgArea = new ActiveBox[NUM_MSG_AREAS];

  protected Component player;
  protected PlayStation ps;
  protected ActiveBox msgBox;
  long mem;
  private static int waitCursorCount;
  protected boolean readyToPaint;
  public JDialog currentHelpWindow, currentAboutWindow;
  protected int progressMax, progress;
  protected boolean hasProgress, progressActive;
  protected long progressStartTime = 0L;

  protected static HashSet<Skin> skinStack = new HashSet<Skin>();

  /** Creates new Skin */
  protected Skin() {
    super();
    name = null;
    fileName = null;
    setEnabled(false);
    readyToPaint = false;
    setLayout(null);
    player = null;
    ps = null;
    currentHelpWindow = null;
    currentAboutWindow = null;
    progress = 0;
    progressMax = 100;
    progressActive = false;
    hasProgress = false;
    skinStack.add(this);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
  }

  public void emptySkinStack() {
    skinStack = new HashSet<Skin>();
  }

  public void attach(Component setPlayer) {
    if (player != null)
      detach();
    player = setPlayer;
    add(player);
    /*
     * if(player instanceof ActionListener) for(int i=0; i<NUM_BUTTONS; i++)
     * if(buttons[i]!=null) buttons[i].addActionListener((ActionListener)player);
     */
    setWaitCursor();
    setEnabled(true);
    revalidate();
  }

  public void detach() {
    if (player != null) {
      remove(player);
      player = null;
    }
    if (currentHelpWindow != null)
      currentHelpWindow.setVisible(false);
    if (currentAboutWindow != null)
      currentAboutWindow.setVisible(false);
    setEnabled(false);
  }

  public static final String ELEMENT_NAME = "skin";
  public static final String INTERNAL_SKIN_PREFIX = "@";
  public static final String NAME = "name";

  public static Skin getSkin(String skinName, FileSystem fs, PlayStation ps) throws Exception {
    Iterator it = skinStack.iterator();
    while (it.hasNext()) {
      Skin s = (Skin) it.next();
      if (s != null && skinName.equals(s.fileName) && ps.equals(s.ps))
        return s;
    }

    org.jdom.Element e;
    if (skinName.startsWith(INTERNAL_SKIN_PREFIX)) {
      e = FileSystem
          .getXMLDocument(ResourceManager
              .getResourceAsStream(RESOURCE_FOLDER + "/" + skinName.substring(INTERNAL_SKIN_PREFIX.length())))
          .getRootElement();
      fs = null;
    } else
      e = fs.getXMLDocument(skinName).getRootElement();

    JDomUtility.checkName(e, ELEMENT_NAME);
    Class skinClass = Class.forName(JDomUtility.getClassName(e));
    Skin sk = (Skin) skinClass.newInstance();
    sk.name = e.getAttributeValue(NAME);
    sk.fileName = skinName;
    sk.ps = ps;
    sk.setProperties(e, fs);
    return sk;
  }

  protected abstract void setProperties(org.jdom.Element e, FileSystem fs) throws Exception;

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    RenderingHints rh = g2.getRenderingHints();
    if (player != null) {
      g2.setRenderingHints(Constants.DEFAULT_RENDERING_HINTS);
    }
    render(g2, g2.getClipBounds());
    if (rh != null)
      g2.setRenderingHints(rh);
  }

  public abstract void render(Graphics2D g2, Rectangle clip);

  @Override
  protected void processEvent(AWTEvent e) {
    if (ps != null && e instanceof MouseEvent) {
      if (e.getID() == MouseEvent.MOUSE_CLICKED) {
        Iterator it = getActiveBoxes();
        while (it.hasNext()) {
          ActiveBox ab = (ActiveBox) it.next();
          if (ab.contains(((MouseEvent) e).getPoint()) && ab.getContent().mediaContent != null) {
            ps.stopMedia(ab.getContent().mediaContent.level);
            ab.playMedia(ps);
          }
        }
      }
    }
    super.processEvent(e);
  }

  protected Iterator<ActiveBox> getActiveBoxes() {

    ArrayList<ActiveBox> v = new ArrayList<ActiveBox>();

    if (msgBox != null)
      v.add(msgBox);

    return v.iterator();
  }

  public void setProgressMax(int max) {
    progressMax = max;
  }

  public void setProgressValue(int value) {
    progress = value;
  }

  public void setProgressName(String name) {
    setSystemMessage(null, name == null ? null : ps.getMsg("LOADING_FILE") + " " + FileSystem.getFileNameOf(name));
  }

  public void startProgress(String name) {
    progressStartTime = System.currentTimeMillis();
    setProgressValue(0);
    progressActive = true;
  }

  public void endProgress() {
    progressActive = false;
  }

  public InputStream getProgressInputStream(InputStream is, int expectedLength, String fName) {
    InputStream result;
    if (hasProgress && !progressActive) {
      ProgressInputStream pi = new ProgressInputStream(is, expectedLength, fName);
      pi.addProgressInputStreamListener(this);
      result = pi;
    } else {
      result = is;
    }
    return result;
  }

  public ActiveBox getMsgBox() {
    return msgBox;
  }

  public Counter getCounter(int counterId) {
    return ((counterId < 0 || counterId >= Constants.NUM_COUNTERS) ? null : counters[counterId]);
  }

  public void enableCounter(int counterId, boolean bEnabled) {
    Counter counter = getCounter(counterId);
    if (counter != null)
      counter.setEnabled(bEnabled);
  }

  public void resetAllCounters(boolean bEnabled) {
    for (int i = 0; i < Constants.NUM_COUNTERS; i++)
      if (counters[i] != null) {
        counters[i].setValue(0);
        counters[i].setCountDown(0);
        counters[i].setEnabled(bEnabled);
      }
  }

  public AbstractButton getButton(int buttonId) {
    return ((buttonId < 0 || buttonId >= Constants.NUM_ACTIONS) ? null : buttons[buttonId]);
  }

  /*
   * public void enableButton(int buttonId, boolean state){ AbstractButton
   * button=getButton(buttonId); if(button!=null) button.setEnabled(state); }
   */

  public Object[] getCurrentSettings() {
    Object[] settings = new Object[4];
    settings[0] = buttons;
    settings[1] = msgBox;
    settings[2] = counters;
    settings[3] = new Integer(waitCursorCount);
    return settings;
  }

  public void setCurrentSettings(Object[] settings) {
    if (settings.length > 0 && settings[0] != null) {
      AbstractButton[] buttonSettings = (AbstractButton[]) settings[0];
      for (int i = 0; i < Constants.NUM_ACTIONS; i++) {
        if (i < buttonSettings.length && buttonSettings[i] != null && buttons[i] != null) {
          buttons[i].setVisible(buttonSettings[i].isVisible());
        }
      }
    }
    if (settings.length > 1 && settings[1] != null && msgBox != null) {
      ActiveBox abSettings = (ActiveBox) settings[1];
      msgBox.copyContent(abSettings);
      msgBox.setBoxBase((abSettings.getBoxBaseResolve()));
    }

    if (settings.length > 2 && settings[2] != null && counters != null) {
      Counter[] counterSettings = (Counter[]) settings[2];
      for (int i = 0; i < Constants.NUM_COUNTERS; i++) {
        if (i < counterSettings.length && counterSettings[i] != null && counters[i] != null) {
          counters[i].setEnabled(counterSettings[i].isEnabled());
          counters[i].setValue(counterSettings[i].getValue());
        }
      }
    }
    if (settings.length > 3 && settings[3] != null) {
      waitCursorCount = ((Integer) settings[3]).intValue();
      setWaitCursor();
    }
  }

  protected void drawSlicedFrame(Graphics g, Rectangle dest, Rectangle source, Image img, int leftSlicer,
      int rightSlicer, int topSlicer, int bottomSlicer) {
    Rectangle rs = new Rectangle();
    Rectangle rd = new Rectangle();

    // first row
    rs.setBounds(source.x, source.y, leftSlicer, topSlicer);
    rd.setBounds(dest.x, dest.y, rs.width, rs.height);
    Utils.drawImage(g, img, rd, rs, this);

    rs.x += leftSlicer;
    rs.width = rightSlicer - leftSlicer;
    rd.x += leftSlicer;
    rd.width = dest.width - leftSlicer - (source.width - rightSlicer);
    Utils.tileImage(g, img, rd, rs, this);

    rs.x = source.x + rightSlicer;
    rs.width = source.width - rightSlicer;
    rd.x = dest.x + dest.width - (source.width - rightSlicer);
    rd.width = rs.width;
    Utils.drawImage(g, img, rd, rs, this);

    // second row
    rs.setBounds(source.x, source.y + topSlicer, leftSlicer, bottomSlicer - topSlicer);
    rd.setBounds(dest.x, dest.y + topSlicer, leftSlicer, dest.height - topSlicer - (source.height - bottomSlicer));
    Utils.tileImage(g, img, rd, rs, this);

    rs.x += leftSlicer;
    rs.width = rightSlicer - leftSlicer;
    rd.x += leftSlicer;
    rd.width = dest.width - leftSlicer - (source.width - rightSlicer);
    Utils.tileImage(g, img, rd, rs, this);

    rs.x = source.x + rightSlicer;
    rs.width = source.width - rightSlicer;
    rd.x = dest.x + dest.width - (source.width - rightSlicer);
    rd.width = rs.width;
    Utils.tileImage(g, img, rd, rs, this);

    // third row
    rs.setBounds(source.x, source.y + bottomSlicer, leftSlicer, source.height - bottomSlicer);
    rd.setBounds(dest.x, dest.y + dest.height - (source.height - bottomSlicer), rs.width, rs.height);
    Utils.drawImage(g, img, rd, rs, this);

    rs.x += leftSlicer;
    rs.width = rightSlicer - leftSlicer;
    rd.x += leftSlicer;
    rd.width = dest.width - leftSlicer - (source.width - rightSlicer);
    Utils.tileImage(g, img, rd, rs, this);

    rs.x = source.x + rightSlicer;
    rs.width = source.width - rightSlicer;
    rd.x = dest.x + dest.width - (source.width - rightSlicer);
    rd.width = rs.width;
    Utils.drawImage(g, img, rd, rs, this);
  }

  public boolean hasMemMonitor() {
    return msgArea[MEM] != null;
  }

  public void setMem(long newMem) {
    mem = newMem;
    if (msgArea[MEM] != null) {
      String s = new StringBuilder(Long.toString(mem / 1000)).append(" Kb").substring(0);
      msgArea[MEM].setTextContent(s);
    }
  }

  public void setSystemMessage(String msg1, String msg2) {
    if (/* !isEnabled() || */ !isVisible())
      return;
    if (msgArea[MAIN] != null && msg1 != null) {
      msgArea[MAIN].setTextContent(msg1);
    }
    if (msgArea[AUX] != null) {
      msgArea[AUX].setTextContent(msg2 == null ? "" : msg2);
    }
  }

  public void startAnimation() {
  }

  public void stopAnimation() {
  }

  public void setWaitCursor(boolean status) {
    if (status) {
      waitCursorCount++;
    } else {
      waitCursorCount--;
      if (waitCursorCount < 0) {
        waitCursorCount = 0;
      }
    }
    setWaitCursor();
  }

  public void setWaitCursor() {
    setCursor(
        waitCursorCount > 0 ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : /* Cursor.getDefaultCursor() */ null);
  }

  public void showHelp(JComponent solution, String msg) {
    currentHelpWindow = new HelpWindow(this, solution, msg);
    currentHelpWindow.setVisible(true);
    currentHelpWindow = null;
    requestFocus();
  }

  class HelpWindow extends ExtendedJDialog {

    HelpWindow(Component parent, JComponent solution, String msg) {
      super(parent, ps.getMsg("help_window_caption"), true);
      getContentPane().setLayout(new BorderLayout());
      if (solution != null)
        getContentPane().add(solution, BorderLayout.CENTER);
      else {
        JLabel lb = new JLabel(msg == null || msg.trim().length() == 0 ? "?" : msg.trim());
        getContentPane().add(lb, BorderLayout.NORTH);
      }
      JButton btClose = new JButton(ps.getMsg("help_window_close_button"),
          ResourceManager.getImageIcon("icons/exit_small.gif"));
      btClose.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setVisible(false);
        }
      });
      getContentPane().add(btClose, BorderLayout.SOUTH);

      pack();
      Point p = null;
      if (solution != null) {
        p = (Point) solution.getClientProperty(HelpActivityComponent.PREFERRED_LOCATION);
        if (p != null) {
          p = new Point(p);
          int dx = (getWidth() - getContentPane().getWidth()) / 2;
          int dy = getHeight() - getContentPane().getHeight() - dx;
          p.translate(-dx, -dy);
        }
      }
      if (p == null)
        p = parent.getLocationOnScreen();
      setLocation(p);
    }
  }

  public AboutWindow buildAboutWindow() {
    return new AboutWindow(this, ps, new Dimension(500, 400));
  }

  public void showAboutWindow(AboutWindow aw) {
    currentAboutWindow = aw;
    currentAboutWindow.setVisible(true);
    currentAboutWindow = null;
    requestFocus();
  }

  @Override
  public void requestFocus() {
    if (player != null)
      player.requestFocus();
  }

  public static String[] getSystemSkinList(boolean withEmptyEntry) {
    ArrayList<String> v = new ArrayList<String>();
    java.util.Properties prop = new java.util.Properties();
    try {
      prop.load(ResourceManager.getResourceAsStream(RESOURCE_FOLDER + "/" + RESOURCE_LIST_FILE));
    } catch (Exception e) {
      System.err.println(
          "Unable to open " + ResourceManager.RESOURCE_ROOT + RESOURCE_FOLDER + "/" + RESOURCE_LIST_FILE + ":\n" + e);
    }
    if (withEmptyEntry)
      v.add("");
    for (java.util.Enumeration e = prop.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String value = "@" + prop.getProperty(key).trim();
      v.add(value);
    }
    if (v.size() > 0)
      return (String[]) v.toArray(new String[v.size()]);
    else
      return new String[0];
  }
}
