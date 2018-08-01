/*
 * File    : Activity.java
 * Created : 16-aug-2001 1:05
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

package edu.xtec.jclic;

import edu.xtec.jclic.automation.AutoContentProvider;
import edu.xtec.jclic.bags.MediaBagElement;
import edu.xtec.jclic.boxes.ActiveBagContent;
import edu.xtec.jclic.boxes.ActiveBoxBag;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.jclic.boxes.BoxConnector;
import edu.xtec.jclic.boxes.TextGridContent;
import edu.xtec.jclic.clic3.Clic3Activity;
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.misc.Gradient;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.skins.Skin;
import edu.xtec.util.Domable;
import edu.xtec.util.Html;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ResourceBridge;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * <CODE>Activity</CODE> is the abstract base class for JClic activities. It
 * defines also the inner class {@link edu.xtec.jclic.Activity.Panel}, wich is
 * responsible of the user interaction with the activity content. Activities
 * should extend both <CODE>Activity</CODE> and <CODE>Activity.Panel
 * </CODE> classes in order to become fully operative. JClic stores activities
 * in memory as {@link org.jdom.Element} objects. So, all non-transient data
 * must be stored to and retrieved from JDom elements.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public abstract class Activity extends Object implements Constants, Editable, Domable {

  /** The {@link JClicProject} this activity belongs to. */
  protected JClicProject project;

  // public fields
  /** The activity name */
  public String name = DEFAULT_NAME;

  /** Code used in reports to filter queries. Default is <I>null</I>. */
  public String code;

  /** Description of the activity. */
  public String description;

  /** Space, measured in pixels, between the activity components. */
  public int margin = DEFAULT_MARGIN;

  /** Background color of the activity panel. */
  public Color bgColor = DEFAULT_BG_COLOR;

  /** Gradient used to draw the background of the activity panel. */
  public Gradient bgGradient;

  /** Whether the bgImage (if any) has to be tiled across the panel background. */
  public boolean tiledBgImg;

  /**
   * Filename of the image painted in the panel background. Default is
   * <I>null</I>.
   */
  public String bgImageFile;

  /** Whether to draw a border around the activity panel. */
  public boolean border = true;

  /**
   * Whether to place the activity panel at the point specified by
   * {@link absolutePosition}, or leave it centered in the main window of the
   * player.
   */
  public boolean absolutePositioned;

  /** Position of the activity panel into the {@link edu.xtec.jclic.Player}. */
  public Point absolutePosition;

  /** Whether to generate usage reports. */
  public boolean includeInReports = true;

  /**
   * Whether to send action events to the {@link edu.xtec.jclic.report.Reporter}.
   */
  public boolean reportActions;

  /** Whether to have a help window or not. */
  public boolean helpWindow;

  /** Whether to show the solution on the help window. */
  public boolean showSolution;

  /**
   * Message to show in the help window when {@link showSolution} is
   * <CODE>false</CODE>.
   */
  public String helpMsg;

  /**
   * Specific set of {@link EventSounds} used in the activity. The default is
   * <CODE>null</CODE>, meaning to use the {@link edu.xtec.jclic.Player} sounds.
   */
  public EventSounds eventSounds = new EventSounds(null);

  /**
   * Wheter the activity must be solved in a specific order. The default is
   * <CODE>false</CODE>.
   */
  public boolean useOrder;

  /**
   * Wheter the cells of the activity will be dragged across the screen. When
   * <CODE>false</CODE>, a line will be painted to link elements.
   */
  public boolean dragCells;

  /**
   * File name of the Skin used by the activity. The default value is null,
   * meaning that the activity will use the skin specified for the project.
   */
  public String skinFileName;
  /**
   * Maximum amount of time (seconds) to solve the activity. The default value is
   * <CODE>0</CODE>, meaning unlimited time.
   */
  public int maxTime;
  /**
   * Whether the time counter should display a countdown when {@link #maxTime}
   * &gt; 0.
   */
  public boolean countDownTime;
  /**
   * Maximum number of actions allowed to solve the activity. The default value is
   * <CODE>0</CODE>, meaning unlimited actions.
   */
  public int maxActions;
  /**
   * Whether the actions counter should display a countdown when
   * {@link #maxActions} &gt; 0.
   */
  public boolean countDownActions;
  /**
   * String with the URL to be displayed when the user clicks on the <I>info</I>
   * button. Default is <CODE>null</CODE>.
   */
  public String infoUrl;
  /**
   * System command to be executed when the user clicks the <I>info</I> button.
   * Default is <CODE>
   * null</CODE>. Applets have this function disabled.
   */
  public String infoCmd;

  // protected fields
  /**
   * String labels corresponding to the four identifiers of JClic messages:
   * PREVIOUS, MAIN, END and END_ERROR.
   */
  public static final String[] MSG_TYPE = { "previous", "initial", "final", "finalError" };

  /**
   * Identifier of the message displayed before the activity starts. Used only in
   * certain types of activities.
   */
  public static final int PREVIOUS = 0;

  /**
   * Identifier of the main message, displayed as long as the activity is playing.
   */
  public static final int MAIN = 1;

  /**
   * Identifier of the message displayed when the user successfully ends the
   * activity.
   */
  public static final int END = 2;

  /**
   * Identifier of the message displayed when the user exceeds the maximum amount
   * of time or actions allowed.
   *
   * @see maxTime
   * @see maxActions
   */
  public static final int END_ERROR = 3;

  /** Number of message types. */
  public static final int NUM_MSG = 4;

  protected ActiveBoxContent[] messages = new ActiveBoxContent[NUM_MSG];
  protected Dimension windowSize = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  protected boolean transparentBg;
  protected Color activityBgColor = DEFAULT_BG_COLOR;
  protected Gradient activityBgGradient;
  protected boolean bTimeCounter = true;
  protected boolean bScoreCounter = true;
  protected boolean bActionsCounter = true;
  protected int shuffles = DEFAULT_SHUFFLES;
  protected AutoContentProvider acp;

  // fields used only by certain activity types
  public ActiveBagContent[] abc;
  public TextGridContent tgc;
  public int boxGridPos;
  public boolean[] scramble = new boolean[2];
  public boolean invAss;

  // static fields

  // layout constants
  public static final int AB = 0, BA = 1, AUB = 2, BUA = 3;
  public static final String[] LAYOUT_NAMES = { "AB", "BA", "AUB", "BUA" };

  public static final int DEFAULT_WIDTH = 400;
  public static final int DEFAULT_HEIGHT = 300;
  public static final int MINIMUM_WIDTH = 40;
  public static final int MINIMUM_HEIGHT = 40;
  public static final String DEFAULT_NAME = "---";
  public static final int DEFAULT_MARGIN = 8;
  public static final int DEFAULT_SHUFFLES = 31;
  public static final int DEFAULT_GRID_ELEMENT_SIZE = 20;
  public static final Color DEFAULT_BG_COLOR = Color.lightGray;

  // Constants for actions
  public static final String ACTION_MATCH = "MATCH", ACTION_PLACE = "PLACE", ACTION_WRITE = "WRITE",
      ACTION_SELECT = "SELECT", ACTION_HELP = "HELP";

  // Constants for XML fields
  public static final String ELEMENT_NAME = "activity", NAME = "name";
  public static final String BASE_CLASS = "edu.xtec.jclic.activities.", BASE_CLASS_TAG = "@";
  // Already defined in edu.xtec.jclic.Constants:
  // public static final String ID="id", SKIN="skin";
  public static final String CODE = "code", DESCRIPTION = "description", MESSAGES = "messages", TYPE = "type",
      SETTINGS = "settings", LAYOUT = "layout", MARGIN = "margin", CONTAINER = "container", BGCOLOR = "bgColor",
      IMAGE = "image", TILED = "tiled", COUNTERS = "counters", TIME = "time", ACTIONS = "actions", SCORE = "score",
      WINDOW = "window", TRANSPARENT = "transparent", BORDER = "border", POSITION = "position", X = "x", Y = "y",
      SIZE = "size", WIDTH = "width", HEIGHT = "height", PRIMARY = "primary", SECONDARY = "secondary",
      SOLVED_PRIMARY = "solvedPrimary", SOLVED_SECONDARY = "solved_secondary", GRID = "grid", ROW = "row",
      CLUES = "clues", CLUE = "clue", RANDOM_CHARS = "random_chars", SCRAMBLE = "scramble", TIMES = "times",
      REPORT = "report", REPORT_ACTIONS = "reportActions", HELP_WINDOW = "helpWindow",
      HELP_SHOW_SOLUTION = "showSolution", USE_ORDER = "useOrder", DRAG_CELLS = "dragCells", FILE = "file",
      MAX_TIME = "maxTime", COUNT_DOWN_TIME = "countDownTime", MAX_ACTIONS = "maxActions",
      COUNT_DOWN_ACTIONS = "countDownActions", INFO_URL = "infoUrl", INFO_CMD = "infoCmd";

  public static final String[][] COMPATIBLE_ACTIVITIES = new String[][] {
      new String[] { "@puzzles.DoublePuzzle", "@puzzles.ExchangePuzzle", "@puzzles.HolePuzzle", "@memory.MemoryGame",
          "@associations.SimpleAssociation", "@associations.ComplexAssociation", "@panels.Explore", "@panels.Identify",
          "@panels.InformationScreen", "@text.WrittenAnswer" },
      new String[] { "@text.FillInBlanks", "@text.Identify", "@text.Order", "@text.Complete", } };

  /** Creates new edu.xtec.jclicActivity */
  public Activity(JClicProject project) {
    this.project = project;
  }

  public void initNew() {
    name = project.getBridge().getMsg("UNNAMED");
  }

  public String getPublicName() {
    return name;
  }

  public JClicProject getProject() {
    return project;
  }

  public Editor getEditor(Editor parent) {
    Editor result = null;
    String s = getClass().getName() + "Editor";
    try {
      Class.forName(s);
    } catch (ClassNotFoundException ex) {
      s = "edu.xtec.jclic.ActivityEditor";
    }
    return Editor.createEditor(s, this, parent);
  }

  public String getShortClassName() {
    String s = getClass().getName();
    if (s.startsWith(BASE_CLASS))
      s = BASE_CLASS_TAG + s.substring(BASE_CLASS.length());
    return s;
  }

  public String[] getSimilarActivityClasses() {
    String[] result = null;
    String cl = getShortClassName();
    for (String[] classGroup : COMPATIBLE_ACTIVITIES) {
      for (String s : classGroup) {
        if (cl.equals(s)) {
          result = classGroup;
          break;
        }
      }
    }
    if (result == null) {
      result = new String[] { cl };
    }
    return result;
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element child, child2, child3;
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);

    e.setAttribute(JDomUtility.CLASS, getShortClassName());
    e.setAttribute(NAME, name);
    if (code != null)
      e.setAttribute(CODE, code);
    if (description != null)
      JDomUtility.addParagraphs(e, DESCRIPTION, description);

    child = new org.jdom.Element(MESSAGES);
    for (int i = 0; i < NUM_MSG; i++)
      if (messages[i] != null)
        child.addContent(messages[i].getJDomElement().setAttribute(TYPE, MSG_TYPE[i]));
    e.addContent(child);

    child = new org.jdom.Element(SETTINGS);

    child.setAttribute(MARGIN, Integer.toString(margin));

    if (infoUrl != null && infoUrl.length() > 0)
      child.setAttribute(INFO_URL, infoUrl);
    else if (infoCmd != null && infoCmd.length() > 0)
      child.setAttribute(INFO_CMD, infoCmd);

    if (useOrder)
      child.setAttribute(USE_ORDER, JDomUtility.boolString(useOrder));
    if (dragCells)
      child.setAttribute(DRAG_CELLS, JDomUtility.boolString(dragCells));
    if (maxTime > 0) {
      child.setAttribute(MAX_TIME, Integer.toString(maxTime));
      child.setAttribute(COUNT_DOWN_TIME, JDomUtility.boolString(countDownTime));
    }
    if (maxActions > 0) {
      child.setAttribute(MAX_ACTIONS, Integer.toString(maxActions));
      child.setAttribute(COUNT_DOWN_ACTIONS, JDomUtility.boolString(countDownActions));
    }
    child.setAttribute(REPORT, JDomUtility.boolString(includeInReports));
    if (includeInReports)
      child.setAttribute(REPORT_ACTIONS, JDomUtility.boolString(reportActions));
    if (helpWindow) {
      child2 = new org.jdom.Element(HELP_WINDOW);
      boolean hsa = helpSolutionAllowed();
      if (hsa)
        child2.setAttribute(HELP_SHOW_SOLUTION, JDomUtility.boolString(showSolution));
      if (helpMsg != null && (!hsa || !showSolution))
        JDomUtility.setParagraphs(child2, helpMsg);
      child.addContent(child2);
    }
    child2 = new org.jdom.Element(CONTAINER);
    child2.setAttribute(BGCOLOR, JDomUtility.colorToString(bgColor));
    if (bgGradient != null)
      child2.addContent(bgGradient.getJDomElement());

    if (bgImageFile != null && bgImageFile.length() > 0) {
      child3 = new org.jdom.Element(IMAGE);
      child3.setAttribute(NAME, bgImageFile);
      child3.setAttribute(TILED, JDomUtility.boolString(tiledBgImg));
      child2.addContent(child3);
    }

    child3 = new org.jdom.Element(COUNTERS);
    child3.setAttribute(TIME, JDomUtility.boolString(bTimeCounter));
    child3.setAttribute(ACTIONS, JDomUtility.boolString(bActionsCounter));
    child3.setAttribute(SCORE, JDomUtility.boolString(bScoreCounter));
    child2.addContent(child3);

    child.addContent(child2);

    child2 = new org.jdom.Element(WINDOW);
    child2.setAttribute(BGCOLOR, JDomUtility.colorToString(activityBgColor));
    if (activityBgGradient != null)
      child2.addContent(activityBgGradient.getJDomElement());
    if (transparentBg)
      child2.setAttribute(TRANSPARENT, JDomUtility.boolString(transparentBg));
    child2.setAttribute(BORDER, JDomUtility.boolString(border));

    if (absolutePositioned && absolutePosition != null) {
      child3 = new org.jdom.Element(POSITION);
      child3.setAttribute(X, Integer.toString(absolutePosition.x));
      child3.setAttribute(Y, Integer.toString(absolutePosition.y));
      child2.addContent(child3);
    }

    if (windowSize.width != DEFAULT_WIDTH || windowSize.height != DEFAULT_HEIGHT) {
      child3 = new org.jdom.Element(SIZE);
      child3.setAttribute(WIDTH, Integer.toString(windowSize.width));
      child3.setAttribute(HEIGHT, Integer.toString(windowSize.height));
      child2.addContent(child3);
    }
    child.addContent(child2);

    if ((child2 = eventSounds.getJDomElement()) != null)
      child.addContent(child2);

    if (skinFileName != null) {
      child2 = new org.jdom.Element(SKIN);
      child2.setAttribute(FILE, skinFileName);
      child.addContent(child2);
    }

    e.addContent(child);

    if (acp != null)
      e.addContent(acp.getJDomElement());

    return e;
  }

  public static Activity getActivity(Object o, JClicProject project) throws Exception {
    Activity act;
    org.jdom.Element e = null;
    Clic3Activity c3a = null;
    String className;

    if (o instanceof org.jdom.Element) {
      e = (org.jdom.Element) o;
      JDomUtility.checkName(e, ELEMENT_NAME);
      className = JDomUtility.getClassName(e);
    } else if (o instanceof Clic3Activity) {
      c3a = (Clic3Activity) o;
      className = c3a.className;
    } else if (o instanceof String) {
      className = (String) o;
    } else
      throw new Exception("unknown data!!");

    Class<?> activityClass;
    Constructor<?> con;
    Class<?>[] cparams = { JClicProject.class };
    Object[] initArgs = { project };
    if (className.startsWith(BASE_CLASS_TAG))
      className = BASE_CLASS + className.substring(1);
    activityClass = Class.forName(className);
    con = activityClass.getConstructor(cparams);
    act = (Activity) con.newInstance(initArgs);
    if (e != null)
      act.setProperties(e, null);
    else if (c3a != null)
      act.setProperties(c3a);
    else
      act.initNew();
    return act;
  }

  public Activity duplicate() throws Exception {
    return Activity.getActivity(getJDomElement(), project);
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    org.jdom.Element child, child2, child3;

    JDomUtility.checkName(e, ELEMENT_NAME);

    name = JDomUtility.getStringAttr(e, NAME, name, false);
    code = JDomUtility.getStringAttr(e, CODE, code, false);

    if ((child = e.getChild(DESCRIPTION)) != null)
      description = JDomUtility.getParagraphs(child);

    if ((child = e.getChild(MESSAGES)) != null) {
      Iterator itr = child.getChildren(ActiveBoxContent.ELEMENT_NAME).iterator();
      while (itr.hasNext()) {
        child2 = ((org.jdom.Element) itr.next());
        int i = JDomUtility.getStrIndexAttr(child2, TYPE, MSG_TYPE, -1);
        if (i >= 0)
          messages[i] = ActiveBoxContent.getActiveBoxContent(child2, project.mediaBag);
      }
    }

    if ((child = e.getChild(SETTINGS)) != null) {
      margin = JDomUtility.getIntAttr(child, MARGIN, margin);
      infoUrl = JDomUtility.getStringAttr(child, INFO_URL, infoUrl, false);
      if (infoUrl == null)
        infoCmd = JDomUtility.getStringAttr(child, INFO_CMD, infoCmd, false);
      useOrder = JDomUtility.getBoolAttr(child, USE_ORDER, useOrder);
      dragCells = JDomUtility.getBoolAttr(child, DRAG_CELLS, dragCells);
      maxTime = JDomUtility.getIntAttr(child, MAX_TIME, maxTime);
      if (maxTime > 0)
        countDownTime = JDomUtility.getBoolAttr(child, COUNT_DOWN_TIME, countDownTime);
      maxActions = JDomUtility.getIntAttr(child, MAX_ACTIONS, maxActions);
      if (maxActions > 0)
        countDownActions = JDomUtility.getBoolAttr(child, COUNT_DOWN_ACTIONS, countDownActions);
      includeInReports = JDomUtility.getBoolAttr(child, REPORT, includeInReports);
      if (includeInReports)
        reportActions = JDomUtility.getBoolAttr(child, REPORT_ACTIONS, reportActions);
      else
        reportActions = false;
      if ((child2 = child.getChild(HELP_WINDOW)) != null) {
        if (helpSolutionAllowed()) {
          showSolution = JDomUtility.getBoolAttr(child2, HELP_SHOW_SOLUTION, showSolution);
        }
        if (!showSolution)
          helpMsg = JDomUtility.getParagraphs(child2);
        helpWindow = helpMsg != null || showSolution;
      }
      if ((child2 = child.getChild(CONTAINER)) != null) {
        bgColor = JDomUtility.getColorAttr(child2, BGCOLOR, bgColor);
        if ((child3 = child2.getChild(Gradient.ELEMENT_NAME)) != null)
          bgGradient = Gradient.getGradient(child3);
        if ((child3 = child2.getChild(IMAGE)) != null) {
          bgImageFile = child3.getAttributeValue(NAME);
          tiledBgImg = JDomUtility.getBoolAttr(child3, TILED, tiledBgImg);
        }
        if ((child3 = child2.getChild(COUNTERS)) != null) {
          bTimeCounter = JDomUtility.getBoolAttr(child3, TIME, bTimeCounter);
          bActionsCounter = JDomUtility.getBoolAttr(child3, ACTIONS, bActionsCounter);
          bScoreCounter = JDomUtility.getBoolAttr(child3, SCORE, bScoreCounter);
          // Check for old version bug
          String v = project.version;
          if (v != null && v.compareTo("0.1.1") <= 0) {
            boolean b = bScoreCounter;
            bScoreCounter = bTimeCounter;
            bTimeCounter = bActionsCounter;
            bActionsCounter = b;
          }
        }
      }
      if ((child2 = child.getChild(WINDOW)) != null) {
        activityBgColor = JDomUtility.getColorAttr(child2, BGCOLOR, activityBgColor);
        if ((child3 = child2.getChild(Gradient.ELEMENT_NAME)) != null)
          activityBgGradient = Gradient.getGradient(child3);
        transparentBg = JDomUtility.getBoolAttr(child2, TRANSPARENT, transparentBg);
        border = JDomUtility.getBoolAttr(child2, BORDER, border);
        if ((child3 = child2.getChild(POSITION)) != null) {
          absolutePositioned = true;
          absolutePosition = JDomUtility.getPointAttr(child3, X, Y, absolutePosition);
        }
        if ((child3 = child2.getChild(SIZE)) != null) {
          windowSize = JDomUtility.getDimensionAttr(child3, WIDTH, HEIGHT, windowSize);
        }
      }
      if ((child2 = child.getChild(EventSounds.ELEMENT_NAME)) != null)
        eventSounds = EventSounds.getEventSounds(child2);

      if ((child2 = child.getChild(SKIN)) != null) {
        skinFileName = JDomUtility.getStringAttr(child2, FILE, skinFileName, false);

        /*
         * if(skinFileName!=null && skinFileName.length()>0){ if(project.getBridge()
         * instanceof PlayStation) skin=project.mediaBag.getSkinElement(skinFileName,
         * (PlayStation)project.getBridge()); } else skinFileName=null;
         */
      }
    }

    if ((child = e.getChild(AutoContentProvider.ELEMENT_NAME)) != null) {
      acp = AutoContentProvider.getAutoContentProvider(child);
    }
  }

  public void setProperties(Clic3Activity c3a) throws Exception {
    name = c3a.fileName;
    if (c3a.fileDesc.length() > 0)
      description = c3a.fileDesc;
    BoxBase bbMessage = c3a.getBoxBase(2);
    for (int i = MAIN; i < END_ERROR; i++) {
      messages[i] = new ActiveBoxContent();
      messages[i].setBoxBase(bbMessage);
      c3a.setActiveBoxTextContent(messages[i], i == MAIN ? c3a.initMess : c3a.endMess);
      if (messages[i].mediaContent != null)
        messages[i].mediaContent.level = 2;
    }

    absolutePositioned = c3a.pwrp;
    absolutePosition = new Point(c3a.pwrx, c3a.pwry);
    transparentBg = c3a.pwTransp;
    activityBgColor = c3a.colorFons[1];
    border = c3a.marcs;
    bgImageFile = c3a.bmpFons;
    if (bgImageFile != null && bgImageFile.length() > 0)
      project.mediaBag.getImageElement(bgImageFile);
    tiledBgImg = c3a.tileBmp;
    bgColor = c3a.colorFons[0];

    bTimeCounter = c3a.comptadors[0];
    bScoreCounter = c3a.comptadors[1];
    bActionsCounter = c3a.comptadors[2];

    includeInReports = !c3a.noAv;

    if (c3a.custHlp == true && c3a.custHelpFile != null && c3a.custHelpFile.length() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(c3a.custHelpFile);
      if (c3a.hlpTopic != null && c3a.hlpTopic.length() > 0)
        sb.append(" ").append(c3a.hlpTopic);
      String s = sb.substring(0);
      if (s.indexOf(".htm") > 0) {
        StringTokenizer st = new StringTokenizer(s, " ");
        while (st.hasMoreTokens()) {
          String r = st.nextToken();
          if (r.indexOf(".htm") > 0) {
            infoUrl = r;
            break;
          }
        }
      } else {
        if (c3a.custHelpFile.toLowerCase().indexOf(".hlp") > 0)
          infoCmd = "winhelp.exe " + c3a.custHelpFile;
        else
          infoCmd = s;
      }
    }

    if (helpSolutionAllowed())
      showSolution = c3a.shHelp;
    if (showSolution)
      helpWindow = true;

    if (c3a.useDLL) {
      if (c3a.rgDLL.equalsIgnoreCase("arith2.dll")) {
        org.jdom.Element e = new org.jdom.Element(AutoContentProvider.ELEMENT_NAME);
        e.setAttribute(JDomUtility.CLASS, "edu.xtec.jclic.automation.arith.Arith");
        try {
          acp = AutoContentProvider.getAutoContentProvider(e);
          if (acp != null)
            acp.setClic3Properties(c3a.dllOptions);
        } catch (Exception ex) {
          System.err.println("Unable to start arith2:\n" + ex);
        }
      }
    }
  }

  public static void listReferences(org.jdom.Element e, Map<String, String> map) {
    org.jdom.Element child, child2, child3;

    if ((child = e.getChild(MESSAGES)) != null) {
      Iterator itr = child.getChildren(ActiveBoxContent.ELEMENT_NAME).iterator();
      while (itr.hasNext())
        ActiveBoxContent.listReferences((org.jdom.Element) itr.next(), map);
    }

    if ((child = e.getChild(SETTINGS)) != null) {
      if ((child2 = child.getChild(CONTAINER)) != null) {
        if ((child3 = child2.getChild(IMAGE)) != null) {
          map.put(child3.getAttributeValue(NAME), Constants.MEDIA_OBJECT);
        }
      }
      if ((child2 = child.getChild(EventSounds.ELEMENT_NAME)) != null)
        EventSounds.listReferences(child2, map);

      if ((child2 = child.getChild(SKIN)) != null) {
        String skfn = JDomUtility.getStringAttr(child2, FILE, null, false);
        if (skfn != null && !skfn.startsWith(Skin.INTERNAL_SKIN_PREFIX))
          map.put(skfn, Constants.SKIN_OBJECT);
        // TODO: check skin dependences...
      }
    }

    Iterator itr = e.getChildren(ActiveBagContent.ELEMENT_NAME).iterator();
    while (itr.hasNext())
      ActiveBagContent.listReferences((org.jdom.Element) itr.next(), map);

    if ((child = e.getChild("document")) != null) {
      innerListReferences(child, map);
    }

    if ((child = e.getChild(AutoContentProvider.ELEMENT_NAME)) != null) {
      try {
        AutoContentProvider.listReferences(child, map);
      } catch (Exception ex) {
        System.err.println("Error checking AutoContentProvider: " + ex);
      }
    }
  }

  public static void innerListReferences(org.jdom.Element e, Map<String, String> map) {
    Iterator it = e.getChildren(ActiveBoxContent.ELEMENT_NAME).iterator();
    while (it.hasNext())
      ActiveBoxContent.listReferences((org.jdom.Element) it.next(), map);

    it = e.getChildren().iterator();
    while (it.hasNext()) {
      org.jdom.Element child = (org.jdom.Element) it.next();
      if (!ActiveBoxContent.ELEMENT_NAME.equals(child.getName()))
        innerListReferences(child, map);
    }
  }

  public ActiveBoxContent[] getMessages() {
    return messages;
  }

  public String toHtmlString(ResourceBridge rb) {
    Html html = new Html(1000);

    html.doubleCell(rb.getMsg("about_window_lb_activity"), true, name, true);

    if (description != null)
      html.doubleCell(rb.getMsg("about_window_lb_description"), true, description, false);

    return Html.table(html.toString(), null, 1, 5, -1, null, false);
  }

  // 02-oct-2006: Param "ResourceBridge" removed (can be obtained from "project"),
  // Changed the call to "init"
  public void initAutoContentProvider() {
    if (acp != null)
      acp.init(project.getBridge(), project.getFileSystem());
  }

  public boolean prepareMedia(PlayStation ps) {
    if (eventSounds != null)
      eventSounds.realize(ps.getOptions(), project.mediaBag);
    for (ActiveBoxContent msg : messages)
      if (msg != null)
        msg.prepareMedia(ps);
    if (abc != null)
      for (ActiveBagContent b : abc)
        if (b != null)
          b.prepareMedia(ps);
    return true;
  }

  protected Activity getActivity() {
    return this;
  }

  public abstract Activity.Panel getActivityPanel(PlayStation ps);

  public boolean helpSolutionAllowed() {
    return false;
  }

  public boolean helpWindowAllowed() {
    return helpWindow && ((helpSolutionAllowed() && showSolution) || helpMsg != null);
  }

  public abstract int getMinNumActions();

  public boolean mustPauseSequence() {
    return getMinNumActions() != 0;
  }

  public boolean canReinit() {
    return true;
  }

  public boolean hasInfo() {
    return ((infoUrl != null && infoUrl.length() > 0) || (infoCmd != null && infoCmd.length() > 0));
  }

  public boolean hasRandom() {
    return false;
  }

  public boolean shuffleAlways() {
    return false;
  }

  public boolean needsKeyboard() {
    return false;
  }

  public void end() {
    if (eventSounds != null) {
      eventSounds.close();
      eventSounds = null;
    }
    clear();
  }

  public void clear() {
  }

  @Override
  public void finalize() throws Throwable {
    try {
      end();
    } finally {
      super.finalize();
    }
  }

  /**
   * Getter for property windowSize.
   *
   * @return Value of property windowSize.
   */
  public Dimension getWindowSize() {
    return new Dimension(windowSize);
  }

  /**
   * Setter for property windowSize.
   *
   * @param windowSize New value of property windowSize.
   */
  public void setWindowSize(Dimension windowSize) {
    this.windowSize = new Dimension(windowSize);
  }

  public abstract class Panel extends JPanel {

    public Skin skin = null;
    public boolean solved = false;
    public Image bgImage = null;
    protected boolean playing = false;
    protected boolean firstRun = true;
    protected int currentItem = 0;
    protected BoxConnector bc = null;
    public PlayStation ps;

    /** Creates new ActivityPanel */
    protected Panel(PlayStation ps) {
      super();
      this.ps = ps;
      setMinimumSize(new Dimension(100, 100));
      setPreferredSize(new Dimension(500, 400));
      enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

      // 02-oct-2006: Added a call to initAutoContentProvider
      initAutoContentProvider();
    }

    public Activity getActivity() {
      return Activity.this;
    }

    public PlayStation getPs() {
      return ps;
    }

    public void buildVisualComponents() throws Exception {
      playing = false;

      skin = null;
      if (skinFileName != null && skinFileName.length() > 0)
        skin = project.mediaBag.getSkinElement(skinFileName, ps);

      bgImage = null;
      if (bgImageFile != null && bgImageFile.length() > 0) {
        MediaBagElement mbe = project.mediaBag.getImageElement(bgImageFile);
        bgImage = mbe.getImage();
        bgImageFile = mbe.getName();
      }
      setBackground(activityBgColor);
      if (transparentBg) {
        setOpaque(false);
      }
      setBorder(border ? BorderFactory.createBevelBorder(BevelBorder.RAISED) : BorderFactory.createEmptyBorder());

      invalidate();
    }

    protected void playEvent(int event) {
      if (eventSounds != null)
        eventSounds.play(event);
    }

    public void initActivity() throws Exception {
      if (playing) {
        playing = false;
        ps.reportEndActivity(Activity.this, solved);
      }
      solved = false;
      ps.reportNewActivity(Activity.this, 0);
      enableCounters();
    }

    public void startActivity() throws Exception {
    }

    public void showHelp() {
    }

    public abstract void render(Graphics2D g2, Rectangle dirtyRegion);

    public abstract Dimension setDimension(Dimension maxSize);

    public void processMouse(MouseEvent e) {
    }

    public void processKey(KeyEvent e) {
    }

    public boolean isPlaying() {
      return playing;
    }

    public final void fitTo(Rectangle proposed, Rectangle bounds) {
      Point origin = new Point(0, 0);
      if (absolutePositioned && absolutePosition != null) {
        origin.x = Math.max(0, absolutePosition.x + proposed.x);
        origin.y = Math.max(0, absolutePosition.y + proposed.y);
        proposed.width -= absolutePosition.x;
        proposed.height -= absolutePosition.y;
      }
      Dimension d = setDimension(new Dimension(Math.max(2 * margin + Activity.MINIMUM_WIDTH, proposed.width),
          Math.max(2 * margin + Activity.MINIMUM_HEIGHT, proposed.height)));
      if (!absolutePositioned) {
        origin.setLocation(Math.max(0, proposed.x + (proposed.width - d.width) / 2),
            Math.max(0, proposed.y + (proposed.height - d.height) / 2));
      }
      if (origin.x + d.width > bounds.width)
        origin.x = Math.max(0, bounds.width - d.width);
      if (origin.y + d.height > bounds.height)
        origin.y = Math.max(0, bounds.height - d.height);
      setBounds(origin.x, origin.y, d.width, d.height);
    }

    @Override
    public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      RenderingHints rh = g2.getRenderingHints();
      g2.setRenderingHints(DEFAULT_RENDERING_HINTS);
      if (!transparentBg) {
        if (activityBgGradient == null || activityBgGradient.hasTransparency())
          super.paintComponent(g2);
        if (activityBgGradient != null)
          activityBgGradient.paint(g2, new Rectangle(0, 0, getWidth(), getHeight()));
      }
      while (true) {
        BoxBase.flagFontReduced = false;
        render(g2, g2.getClipBounds());
        if (!BoxBase.flagFontReduced)
          break;
      }
      g2.setRenderingHints(rh);
    }

    public void forceFinishActivity() {
    }

    public void finishActivity(boolean result) {
      playing = false;
      solved = result;

      if (bc != null)
        bc.end();

      if (result) {
        setAndPlayMsg(END, EventSounds.FINISHED_OK);
      } else {
        setAndPlayMsg(END_ERROR, EventSounds.FINISHED_ERROR);
      }
      ps.activityFinished(solved);
      ps.reportEndActivity(Activity.this, solved);
    }

    protected void setAndPlayMsg(int msgCode, int eventSoundsCode) {
      ps.setMsg(messages[msgCode]);
      if (messages[msgCode] == null || messages[msgCode].mediaContent == null)
        playEvent(eventSoundsCode);
      else
        ps.playMsg();
    }

    public void end() {
      forceFinishActivity();
      if (playing) {
        if (bc != null)
          bc.end();
        ps.reportEndActivity(Activity.this, solved);
        playing = false;
        solved = false;
      }
      clear();
    }

    public abstract void clear();

    @Override
    public void finalize() throws Throwable {
      try {
        end();
      } finally {
        super.finalize();
      }
    }

    protected void enableCounters() {
      enableCounters(bTimeCounter, bScoreCounter, bActionsCounter);
    }

    protected void enableCounters(boolean eTime, boolean eScore, boolean eActions) {
      ps.setCounterEnabled(TIME_COUNTER, eTime);
      if (countDownTime)
        ps.setCountDown(TIME_COUNTER, maxTime);
      ps.setCounterEnabled(SCORE_COUNTER, eScore);
      ps.setCounterEnabled(ACTIONS_COUNTER, eActions);
      if (countDownActions)
        ps.setCountDown(ACTIONS_COUNTER, maxActions);
    }

    @Override
    public void doLayout() {
    }

    protected void shuffle(ActiveBoxBag[] bg, boolean visible, boolean fitInArea) {

      int steps = shuffles;

      int i = shuffles;
      while (i > 0) {
        int k = i > steps ? steps : i;
        for (ActiveBoxBag abb : bg)
          if (abb != null)
            abb.scrambleCells(k, fitInArea);
        i -= steps;
      }
    }

    @Override
    protected void processEvent(AWTEvent e) {
      if (playing && e instanceof MouseEvent) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED && !hasFocus())
          requestFocus();

        // Modified 05-Aug-2010:
        // Medium button must not be processed when "fressa" enabled
        if (e.getID() != MouseEvent.BUTTON3 || ps.getFressa() == null)
          processMouse((MouseEvent) e);
      } else if (playing && e instanceof KeyEvent)
        processKey((KeyEvent) e);

      super.processEvent(e);
    }

    public void calcScanPoints() {
      if (ps.getFressa() != null) {
        ps.getFressa().calcScanPoints();
      }
    }

    public void calcInactivePoints() {
      // does nothing by default
    }

    public void nextScanPosition() {
      if (ps.getFressa() != null) {
        ps.getFressa().nextScanPosition();
      }
    }

    public void clickLeftButton() {
      if (ps.getFressa() != null) {
        ps.getFressa().clickLeftButton();
        if (ps.getFressa().jumpWellPlaced)
          calcInactivePoints();
      }
    }
  }
}
