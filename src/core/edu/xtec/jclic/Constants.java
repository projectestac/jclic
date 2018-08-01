/*
 * File    : Constants.java
 * Created : 31-jul-2002 09:35
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

/**
 * Definition of some global constants widely used in JClic.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.10.02
 */
public interface Constants {

    /**
     * Path to the bundle of <I>.properties</I> files with the basic JClic messages
     */
    public static final String DEFAULT_BUNDLE = "messages.JClicMessages";

    /** Path to the basic JClic settings */
    public static final String COMMON_SETTINGS = "commonSettings";

    /** Path to the JClic logo */
    public static final String LOGO_ICON = "icons/logo.png";

    /** Name of the application (JClic) */
    public static final String PROGRAM = "JClic";

    // String constants used in applet parameters and common properties

    /** Key for the parameter <CODE>cookie</CODE>, used in applets */
    public static final String COOKIE = "cookie";

    /**
     * Key for the parameter <CODE>exitUrl</CODE>, used in applets to specify the
     * URL where the browser should navigate when JClic executes an
     * <CODE>exit</CODE> command.
     */
    public static final String EXIT_URL = "exitUrl";

    /**
     * Key for the parameter <CODE>urlBase</CODE>, used in applets to specify the
     * absolute base URL used to locate other files.
     */
    public static final String URL_BASE = "urlBase";

    /**
     * Key for the parameter <CODE>infoUrlFrame</CODE>, used in applets to specify
     * the name of the browser frame or window where the <CODE>info</CODE> content
     * of the activities will be displayed.
     */
    public static final String INFO_URL_FRAME = "infoUrlFrame";

    /**
     * Key for the boolean parameter <CODE>compressImages</CODE>. When
     * <CODE>false</CODE>, images larger than the cell will be cropped. Otherwise,
     * JClic will compress it to fit into the cell.
     */
    public static final String COMPRESS_IMAGES = "compressImages";

    /**
     * Key for the boolean parameter 'preDrawImages', used in past versions to
     * activate a workaround to a Java graphics bug.
     */
    public static final String PRE_DRAW_IMAGES = "preDrawImages";

    /**
     * Key for the parameter 'skin', used to specify the
     * {@link edu.xtec.jclic.skins.Skin} to be used.
     */
    public static final String SKIN = "skin";

    /**
     * Key for the parameter 'reporterClass', used to specify the type of
     * {@link edu.xtec.jclic.report.Reporter} to be used.
     */
    public static final String REPORTER_CLASS = "reporter";

    /**
     * Key for the parameter 'reporterParams', used to pass specific parametres to
     * the reporter.
     */
    public static final String REPORTER_PARAMS = "reporterParams";

    /**
     * Key for the boolean parameter 'systemSounds'. When <CODE>false</CODE>, no
     * system sounds are played.
     */
    public static final String SYSTEM_SOUNDS = "systemSounds";

    /**
     * Key for the boolean parameter 'audioEnabled'. When <CODE>false</CODE>, no
     * sounds or other media are played.
     */
    public static final String AUDIO_ENABLED = "audioEnabled";

    /**
     * Key for the boolean parameter 'navButtonsAlways'. When <CODE>true</CODE>, the
     * navigation buttons (<I>next activity</I> and <I>previous activity</I>) are
     * always displayed.
     */
    public static final String NAV_BUTTONS_ALWAYS = "navButtonsAlways";

    /**
     * Key for the boolean parameter 'trace'. When <CODE>true</CODE>, debug messages
     * are displayed in the console.
     */
    public static final String TRACE = "trace";

    /**
     * Key for the boolean parameter 'myurl'. Used by applets to know its
     * DocumentBase since Oracle has broken this funcionality for local filesystems
     * in Java 1.7.40.
     */
    public static final String MYURL = "myurl";

    /** String used to represent the boolean value <CODE>true</CODE>. */
    public static final String TRUE = "true";

    /** String used to represent the boolean value <CODE>false</CODE>. */
    public static final String FALSE = "false";

    /** String used to represent the <CODE>default</CODE> value */
    public static final String DEFAULT = "default";

    /** String used to represent the <CODE>enabled</CODE> value */
    public static final String ENABLED = "enabled";

    /** String used to represent the <CODE>id</CODE> value */
    public static final String ID = "id";

    /**
     * String used to represent the QuickTime for Java (pre-6.1) multimedia system
     */
    public static final String QT = "QuickTime";

    /**
     * String used to represent the QuickTime for Java (6.1 or later) multimedia
     * system
     */
    public static final String QT61 = "QuickTime 6.1";

    /** String used to represent the Java Media Framework system */
    public static final String JMF = "Java Media Framework";

    /**
     * Key for the runtime property <CODE>mediaSystem</CODE>. Possible values are
     * <CODE>JMF</CODE>, <CODE>QT</CODE>, <CODE>QT61</CODE> or <CODE>null</CODE>.
     */
    public static final String MEDIA_SYSTEM = "mediaSystem";

    /**
     * Key for the parameter or runtime property 'noMediaSystemWarn', used to
     * indicate that users should not be warned about the absence of multimedia
     * extensions in their Java system.
     */
    public static final String NO_MEDIASYSTEM_WARN = "NoMediaSystemWarn";

    /**
     * Array of strings containing the values of all the valid multimedia extensions
     */
    public static final String[] MEDIA_SYSTEMS = { DEFAULT, JMF, QT };

    /** Default rendering hints used with {@link java.awt.Graphics2D} objects. */
    public static final java.awt.RenderingHints DEFAULT_RENDERING_HINTS = new java.awt.RenderingHints(null);

    /**
     * Index of the <CODE>hand</CODE> (standard) cursor in the
     * {@link edu.xtec.jclic.Player} member <CODE>cursors</CODE>.
     */
    public static final int HAND_CURSOR = 0;

    /**
     * Index of the <CODE>ok</CODE> cursor in the {@link edu.xtec.jclic.Player}
     * member <CODE>cursors
     * </CODE>.
     */
    public static final int OK_CURSOR = 1;

    // Constants for cursors
    /**
     * Index of the <CODE>record</CODE> (microphone) cursor in the
     * {@link edu.xtec.jclic.Player} member <CODE>cursors</CODE>.
     */
    public static final int REC_CURSOR = 2;

    /** Default color used to fill backgrounds of graphic objects. */
    public static final java.awt.Color BG_COLOR = new java.awt.Color(239, 247, 221);

    public static final int ACTION_PREV = 0, ACTION_NEXT = 1, ACTION_RETURN = 2, ACTION_RESET = 3, ACTION_INFO = 4,
            ACTION_HLP = 5, ACTION_AUDIO = 6, ACTION_REPORTS = 7, NUM_ACTIONS = 8;
    /**
     * Array that contains integers representing all the dynamic actions, useful for
     * operations involving the <CODE>actions</CODE> member of
     * {@link edu.xtec.jclic.Player}.
     */
    public static final int[] DYNAMIC_ACTIONS = { ACTION_PREV, ACTION_NEXT, ACTION_RETURN, ACTION_RESET, ACTION_INFO,
            ACTION_HLP };
    /**
     * Array with the names of all the dynamic actions used in
     * {@link edu.xtec.jclic.Player} objects.
     */
    public static final String[] ACTION_NAME = { "prev", "next", "return", "reset", "info", "help", "audio", "about" };

    /** Index of the <CODE>score</CODE> counter. */
    public static final int SCORE_COUNTER = 0;

    /** Index of the <CODE>actions</CODE> counter. */
    public static final int ACTIONS_COUNTER = 1;

    /** Index of the <CODE>time</CODE> counter. */
    public static final int TIME_COUNTER = 2;

    // Constants for counters
    /**
     * Number of counters used in JClic players (currently 3: <CODE>score</CODE>,
     * <CODE>actions</CODE> and <CODE>time</CODE>)
     */
    public static final int NUM_COUNTERS = 3;
    /**
     * Names used to represent the counters (<I>score</I>, <I>actions</I> and
     * <I>time</I>)
     */
    public static final String[] counterNames = { "score", "actions", "time" };

    /**
     * Minimal margin between the activity container internal frame and the activity
     * window bounds.
     */
    public static final int AC_MARGIN = 6;

    /** Minimal value for both with and height of cells. */
    public static final int MIN_CELL_SIZE = 10;

    /** Names used to represent different kinds of objects. */
    public static final String MEDIA_OBJECT = "media", SEQUENCE_OBJECT = "sequence", ACTIVITY_OBJECT = "activity",
            URL_OBJECT = "url", EXTERNAL_OBJECT = "external", SKIN_OBJECT = "skin", PROJECT_OBJECT = "project";

    /** Bit masks used to represent different types of resources. */
    public static final int T_ACTIVITY = 0x0001, T_SEQUENCE = 0x0002, T_URL = 0x0004, T_EXTERNAL = 0x0008,
            T_IMAGE = 0x0010, T_AUDIO = 0x0020, T_MIDI = 0x0040, T_VIDEO = 0x0080, T_ANIM = 0x0100, T_XML = 0x0200,
            T_FONT = 0x0400, T_JCLIC = 0x0800, T_CLIC = 0x1000, T_INST = 0x2000, T_UNKNOWN_MEDIA = 0x4000,
            T_TEXT = 0x8000;

    /** Generic value used to represent all the supported media types. */
    public static final int T_MEDIA = T_IMAGE | T_AUDIO | T_MIDI | T_VIDEO | T_ANIM | T_XML | T_FONT | T_UNKNOWN_MEDIA;
}
