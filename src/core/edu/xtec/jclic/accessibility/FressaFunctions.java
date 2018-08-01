/*
 * JClic accessibility
 * FressaFunctionsClass.java
 * Created on august / 2009
 *
 * @author Jordi Lagares Roset "jlagares@xtec.cat - www.lagares.org"
 * amb el suport del Departament d'Educacio de la Generalitat de Catalunya
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
package edu.xtec.jclic.accessibility;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.skins.Skin;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import java.awt.AWTEvent;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

/** @author jlagares */
public class FressaFunctions {

  // Constants
  public static final String BUNDLE = "messages.AccessibilityMessages";
  public static final String FRESSA_VERSION = "v: alfa 99 - 23/07/2010";
  public static final int DEFAULT_TIMER_SPAN = 12;
  public static final int MAX_SCANNING_ZONES = 3;
  public static final int BUTTONS_ZONE = 0;
  public static final int PUZZLE_ZONE = 1;
  public static final int ASSOCIATION_ZONE = 2;
  public static final int MAX_POINTS_PER_ZONE = 1000;
  public static final Rectangle INITIAL_FORM_EXCHANGE_POS = new Rectangle(100, 100, 45, 30);
  public static final int SCAN_ACTIVATION_DELAY = 5000;
  public static final int SCAN_ACTIVATION_CHECK_SPAN = 1000;
  public static final int SWAYING_ACTIVATION_DELAY = 400;
  public static final int SWAYING_SPAN = 100;
  public static final int SWAYING_AMOUNT = 2;
  // Static variables
  public static boolean noHandCursor = false;
  public static boolean forceRectangularShapes = false;
  // Global settings
  public boolean scanIncludingArrowButtons = true;
  public boolean withChangeZoneButton = false;
  public boolean scanArrowsAtEndOfActivity = false;
  public boolean scanFlagButton = false;
  public boolean jumpWellPlaced = true;
  public boolean showKeyboard = true;
  public boolean changeZoneIfNoClick = false;
  public boolean autoScanOnStart = false;
  public boolean directedScanOnStart = false;
  public boolean autoAutoScan = false;
  public boolean weAreAtTheStart = true;
  public int scanTimerSpan = DEFAULT_TIMER_SPAN;
  // TFormKeyboard settings
  public boolean mustDisableScanTimer = false;
  public boolean isScanning = false;
  public boolean isAutoScanning = false;
  public boolean isDirectedScanning = false;
  // Transient properties
  // Needed to know where the buttons are placed
  Skin skin;
  Activity.Panel actPanel;
  String activityType;
  // Scanning points
  boolean isFirstScreen = false;
  int place;
  int numberOfZones;
  int currentZone;
  int px[][] = new int[MAX_SCANNING_ZONES][MAX_POINTS_PER_ZONE];
  int py[][] = new int[MAX_SCANNING_ZONES][MAX_POINTS_PER_ZONE];
  int zonePoints[] = new int[MAX_SCANNING_ZONES];
  // In case we need to read the contents of the cells
  public String sll[][] = new String[MAX_SCANNING_ZONES][MAX_POINTS_PER_ZONE];
  int clickNumber = 1;
  boolean activityFinished = false;
  TFormExchange formExchange;
  TFormKeyboard formKeyboard;
  boolean activityWithKeyboard = false;
  // JClic
  Rectangle actPanelRectangle;
  Rectangle oldActPanelRectangle;
  Point actPanelRectangleMiddleBottom;
  // Voice synthesis
  boolean withVoice = true;
  boolean readLabels = false;
  // *** >> public FressaVoice fressaVoice;
  // Timers
  Timer scanTimer;
  Timer autoScanActivationTimer;
  long tickCountTime;
  int cursorPosX;
  int cursorPosY;
  // Swaying
  boolean withSwaying = false;
  int swayingCounter = 0;
  int swayingCursorX;
  int swayingCursorY;
  Timer swayingTimer;
  Timer directedScanStartSwayingTimer;
  // Robot
  Robot robot;
  // Messages
  Messages msg;
  // Keys for storing options
  public static final String SCAN_TIMER_SPAN = "scanTimerSpan", SCAN_INCLUDING_ARROWS = "scanIncludingArrows",
      CHANGE_ZONE_BUTTON = "changeZoneButton", SCAN_ARROWS_AT_END = "scanArrowsAtEnd", SCAN_FLAG = "scanFlag",
      JUMP_WELL_PLACED = "jumpWellPlaced", SHOW_KEYBOARD = "showKeyboard",
      CHANGE_ZONE_IF_NO_CLICK = "changeZoneIfNoClick", AUTO_SCAN_ON_START = "autoScanOnStart",
      DIRECTED_SCAN_ON_START = "directedScanOnStart", AUTO_AUTO_SCAN = "autoAutoScan", SWAYING = "swaying",
      NO_HAND_CURSOR = "noHandCursor", FORCE_RECTANGLES = "forceRectangles", READ_LABELS = "readLabels",
      WITH_VOICE = "withVoice";

  public FressaFunctions(Options options) {

    msg = options.getMessages(BUNDLE);

    try {
      robot = new Robot();
    } catch (java.awt.AWTException ex) {
      System.err.println("Unable to create java.awt.Robot: " + ex.getMessage());
    }

    formExchange = new TFormExchange(INITIAL_FORM_EXCHANGE_POS, msg.get("acc_exchangeBtnChar"));
    formKeyboard = new TFormKeyboard(options, this);

    noHandCursor = options.getBoolean(NO_HAND_CURSOR, false);
    forceRectangularShapes = options.getBoolean(FORCE_RECTANGLES, false);

    scanTimerSpan = options.getInt(SCAN_TIMER_SPAN, DEFAULT_TIMER_SPAN);
    scanIncludingArrowButtons = options.getBoolean(SCAN_INCLUDING_ARROWS, true);
    withChangeZoneButton = options.getBoolean(CHANGE_ZONE_BUTTON, false);
    scanArrowsAtEndOfActivity = options.getBoolean(SCAN_ARROWS_AT_END, false);
    scanFlagButton = options.getBoolean(SCAN_FLAG, false);
    jumpWellPlaced = options.getBoolean(JUMP_WELL_PLACED, true);
    showKeyboard = options.getBoolean(SHOW_KEYBOARD, true);
    changeZoneIfNoClick = options.getBoolean(CHANGE_ZONE_IF_NO_CLICK, false);
    autoScanOnStart = options.getBoolean(AUTO_SCAN_ON_START, false);
    directedScanOnStart = options.getBoolean(DIRECTED_SCAN_ON_START, false);
    autoAutoScan = options.getBoolean(AUTO_AUTO_SCAN, false);
    withSwaying = options.getBoolean(SWAYING, false);
    readLabels = options.getBoolean(READ_LABELS, false);
    withVoice = options.getBoolean(WITH_VOICE, false);

    if (withVoice) {
      /*
       * fressaVoice=new FressaVoice(); fressaVoice.start();
       * readText(msg.get("acc_voiceGreeting")); withVoice=fressaVoice.xxxxrunning;
       */
    }
  }

  public void startScanning() {

    if (actPanel != null) {
      zonePoints[0] = -1;
      zonePoints[1] = -1;
      zonePoints[2] = -1;
      if (withChangeZoneButton && formExchange != null) {
        formExchange.setVisible(true);
      }
      place = 0;
      isFirstScreen = true;
      isScanning = true;
      isAutoScanning = false;
      isDirectedScanning = false;

      actPanel.calcScanPoints();

      // WARNING: isScanning was just initialized, so it always will be "true":
      if (formKeyboard != null && activityWithKeyboard && showKeyboard && isScanning) {
        formKeyboard.isHorizontalScanning = false;
        formKeyboard.xPos = 0;
        formKeyboard.yPos = -1;
        formKeyboard.setVisible(true);
        formKeyboard.calcFrameSize();
        numberOfZones = 1;
        clickNumber = 1;
        currentZone = 1;
      }
    }
  }

  public void stopScanning() {
    isScanning = false;
    isAutoScanning = false;
    isDirectedScanning = false;
    if (formExchange != null) {
      formExchange.setVisible(false);
    }
    if (formKeyboard != null) {
      formKeyboard.setVisible(false);
    }
  }

  public void initActivity(Activity.Panel actPanel) {

    this.actPanel = actPanel;

    if (formKeyboard.isVisible()) {
      formKeyboard.setVisible(false);
    }

    activityWithKeyboard = false;

    activityType = actPanel.getActivity().getShortClassName();

    // Clear data
    // WARNING: Avoid the use of fixed-length arrays!
    for (int i = 0; i < MAX_SCANNING_ZONES; i++) {
      zonePoints[i] = -1;
      for (int n = 0; n < MAX_POINTS_PER_ZONE; n++) {
        px[i][n] = 0;
        py[i][n] = 0;
        sll[i][n] = "";
      }
    }

    currentZone = 0; // Start scanning only buttons
    place = 0;
    oldActPanelRectangle.setBounds(0, 0, 0, 0);
    activityFinished = false;
    activityWithKeyboard = actPanel.getActivity().needsKeyboard();

    if (formKeyboard != null && activityWithKeyboard && showKeyboard && isScanning) {
      formKeyboard.isHorizontalScanning = false;
      formKeyboard.xPos = 0;
      formKeyboard.yPos = -1;
      formKeyboard.setVisible(true);
      formKeyboard.calcFrameSize();
      numberOfZones = 1;
      clickNumber = 1;
      currentZone = 1;
    }

    actPanel.calcScanPoints();

    if (autoAutoScan) {
      if (autoScanActivationTimer == null) {
        enableAutoScanTimer();
      }
    } else if (weAreAtTheStart) {
      if (autoScanOnStart) {
        enableAutoScan();
      } else if (directedScanOnStart) {
        enableDirectedScan();
      }
    }

    weAreAtTheStart = false;

    if (isAutoScanning) {
      startScanTimer();
    }
  }

  public void startSwayingTimer() {
    if (swayingTimer == null) {
      swayingTimer = new Timer(SWAYING_SPAN, new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          changeSwayingPos();
        }
      });
      swayingCounter = 0;
      Point p = MouseInfo.getPointerInfo().getLocation();
      swayingCursorX = p.x;
      swayingCursorY = p.y;
      swayingTimer.start();
    }

    if (!swayingTimer.isRunning()) {
      swayingTimer.start();
    }
  }

  public void stopSwayingTimer() {
    if (swayingTimer != null) {
      swayingCounter = 0;
      changeSwayingPos();
      swayingTimer.stop();
      swayingTimer = null;
    }
  }

  public void changeSwayingPos() {

    int dx = 0;
    int dy = 0;

    switch (swayingCounter) {
    case 0:
      dx = -1;
      dy = -1;
      break;
    case 1:
      dx = 1;
      dy = -1;
      break;
    case 2:
      dx = 1;
      dy = 1;
      break;
    case 3:
      dx = -1;
      dy = 1;
      break;
    }

    swayingCounter = ++swayingCounter % 4;

    if (robot != null) {
      robot.mouseMove(swayingCursorX + dx * SWAYING_AMOUNT, swayingCursorY + dy * SWAYING_AMOUNT);
    }
  }

  public void startDirectedScanSwayingTimer() {
    if (swayingTimer == null && directedScanStartSwayingTimer == null) {
      directedScanStartSwayingTimer = new Timer(SWAYING_ACTIVATION_DELAY, new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          startSwayingTimer();
          directedScanStartSwayingTimer = null;
        }
      });
      directedScanStartSwayingTimer.setRepeats(false);
      directedScanStartSwayingTimer.start();
    }
  }

  public void disableScanning() {
    if (withSwaying) {
      stopSwayingTimer();
    }
    stopScanning();

    stopScanTimer();
    Toolkit.getDefaultToolkit().removeAWTEventListener(autoScanMouseListener);
    Toolkit.getDefaultToolkit().removeAWTEventListener(mouseDirectedScanListener);
    Toolkit.getDefaultToolkit().removeAWTEventListener(directedScanKeyboardListener);
    if (autoAutoScan) {
      Point p = MouseInfo.getPointerInfo().getLocation();
      cursorPosX = p.x;
      cursorPosY = p.y;
      tickCountTime = System.currentTimeMillis();
    }
  }

  public void enableAutoScan() {
    disableScanning();
    startScanning();
    isAutoScanning = true;
    startScanTimer();
    Toolkit.getDefaultToolkit().addAWTEventListener(autoScanMouseListener, AWTEvent.MOUSE_EVENT_MASK);
  }

  public void startScanTimer() {
    if (scanTimer == null) {
      scanTimer = new Timer(scanTimerSpan * 100, new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          if (withSwaying) {
            stopSwayingTimer();
          }
          actPanel.nextScanPosition();
          if (withSwaying) {
            startSwayingTimer();
          }
        }
      });
      scanTimer.start();
    }
  }

  public void stopScanTimer() {
    if (scanTimer != null) {
      scanTimer.stop();
      scanTimer = null;
    }
  }

  private AWTEventListener autoScanMouseListener = new AWTEventListener() {

    public void eventDispatched(AWTEvent event) {
      if (isAutoScanning && event instanceof MouseEvent) {
        MouseEvent me = (MouseEvent) event;
        if (me.getButton() == MouseEvent.BUTTON1) {
          if (withSwaying) {
            stopSwayingTimer();
          }

          // WARNING: Is this really needed?
          // if(event.getID()==MouseEvent.MOUSE_RELEASED) {
          // actPanel.ClickLeftButton();
          // }
        } else if (me.getButton() == MouseEvent.BUTTON3) {
          if (withSwaying) {
            stopSwayingTimer();
          }
          stopScanning();
          stopScanTimer();
          Toolkit.getDefaultToolkit().removeAWTEventListener(autoScanMouseListener);
        }
      }
    }
  };

  public void enableAutoScanTimer() {
    if (autoAutoScan && (autoScanActivationTimer == null || !autoScanActivationTimer.isRunning())) {
      Point p = MouseInfo.getPointerInfo().getLocation();
      cursorPosX = p.x;
      cursorPosY = p.y;
      tickCountTime = System.currentTimeMillis();
      autoScanActivationTimer = new Timer(SCAN_ACTIVATION_CHECK_SPAN, new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          if (!isScanning) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            int x = p.x;
            int y = p.y;
            if ((x != cursorPosX) || (y != cursorPosY)) {
              cursorPosX = x;
              cursorPosY = y;
              tickCountTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - tickCountTime > SCAN_ACTIVATION_DELAY) {
              enableAutoScan();
            }
          }
        }
      });
      autoScanActivationTimer.start();
    }
  }

  // WARNING: Why not recicle timers instead of destroying and creating it every
  // time?
  public void disableAutoScanTimer() {
    if (autoScanActivationTimer != null) {
      autoScanActivationTimer.stop();
      autoScanActivationTimer = null;
    }
  }

  public void enableDirectedScan() {
    disableScanning();
    startScanning();
    isDirectedScanning = true;
    if (withSwaying) {
      stopSwayingTimer();
    }
    actPanel.nextScanPosition();
    if (withSwaying) {
      stopSwayingTimer();
    }
    Toolkit.getDefaultToolkit().addAWTEventListener(mouseDirectedScanListener, AWTEvent.MOUSE_EVENT_MASK);
    Toolkit.getDefaultToolkit().addAWTEventListener(directedScanKeyboardListener, AWTEvent.KEY_EVENT_MASK);
  }

  private AWTEventListener mouseDirectedScanListener = new AWTEventListener() {

    public void eventDispatched(AWTEvent event) {
      if (isDirectedScanning) {
        MouseEvent me = (MouseEvent) event;
        if (me.getButton() == MouseEvent.BUTTON1) {
          if (me.getID() == MouseEvent.MOUSE_RELEASED) {
            if (withSwaying) {
              stopSwayingTimer();
            }
            actPanel.clickLeftButton();
            if (withSwaying) {
              startDirectedScanSwayingTimer();
            }
          }
        } else if (me.getButton() == MouseEvent.BUTTON3) {
          if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            if (withSwaying) {
              stopSwayingTimer();
            }
            actPanel.nextScanPosition();
            if (withSwaying) {
              startSwayingTimer();
            }
          }
        }
      }
    }
  };
  private AWTEventListener directedScanKeyboardListener = new AWTEventListener() {

    public void eventDispatched(AWTEvent event) {
      KeyEvent ke = (KeyEvent) event;
      if (ke.getID() == KeyEvent.KEY_TYPED) {
        if (ke.getKeyChar() == KeyEvent.VK_ESCAPE) {
          disableScanning();
        } else {
          // JOptionPane.showMessageDialog(null,ke.getKeyText(ke.getKeyChar()),"JAVA
          // PLAPHOONS",JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }
  };

  public void clickLeftButton() {
    if (withChangeZoneButton) {
      if (activityWithKeyboard && showKeyboard && isScanning) {
        if (currentZone == 0) {
          formKeyboard.isHorizontalScanning = false;
          formKeyboard.xPos = 0;
          formKeyboard.yPos = -1;
          formKeyboard.setVisible(true);
          formKeyboard.calcFrameSize();
          numberOfZones = 1;
          clickNumber = 1;
          currentZone = 1;
        }
        formExchange.changeBgColor(currentZone);
        return;
      }

      if (changeZoneIfNoClick && (activityType.equals("@associations.SimpleAssociation")
          || activityType.equals("@associations.ComplexAssociation"))) {
        if (currentZone == 0 || currentZone == 2) {
          if (place > zonePoints[currentZone]) {
            if (currentZone == 0 && numberOfZones > 0) {
              currentZone = 1;
              place = 0;
            } else if (currentZone == 2) {
              currentZone = 0;
              place = 0;
            }
            formExchange.changeBgColor(currentZone);
            return;
          }
        }
      } else {

        if (currentZone == 0 || currentZone == 1) {
          if (place > zonePoints[currentZone]) {
            if (currentZone == 0 && numberOfZones > 0) {
              currentZone = 1;
              place = 0;
            } else if (currentZone == 1) {
              currentZone = 0;
              place = 0;
            }
            formExchange.changeBgColor(currentZone);
            return;
          }
        }
      }
    }
    if (numberOfZones == 1) {
      if (currentZone == 1) {
        if (!activityType.equals("@panels.InformationScreen") && !activityType.equals("@panels.Explore")
            && !activityType.equals("@panels.Identify") && !activityType.equals("@textGrid.WordSearch")) {
          place = 0;
        }
      }
    } else if (numberOfZones == 2) {
      if (currentZone == 1) {
        currentZone = 2;
        place = 0;
      } else if (currentZone == 2) {
        currentZone = 1;
        place = 0;
      }
    }
  }

  // provisional
  public void readText(String txt) {
  }

  public void calcScanPoints() {
  }

  public void activityFinished() {
  }

  public void nextScanPosition() {
  }
}
