/*
 * File    : Clic3.java
 * Created : 30-oct-2000 20:45
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

package edu.xtec.jclic.clic3;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.bags.ActivityBag;
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.bags.ActivitySequenceJump;
import edu.xtec.jclic.bags.ConditionalJumpInfo;
import edu.xtec.jclic.bags.JumpInfo;
import edu.xtec.jclic.bags.MediaBagElement;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.fileSystem.PCCFileSystem;
import edu.xtec.jclic.project.JClicProject;
import java.awt.Color;
import java.util.ArrayList;

/**
 * Constants and miscellaneous functions useful to read Clic 3.0 files and
 * import it into JClic projects.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public abstract class Clic3 {

  // CONSTANTS OF CLIC 3.0
  //
  public static final int CLICVER = 120;
  public static final String CLICSTR = "CLIC120";

  protected static final String BLANK = "";
  protected static final String COMMA = ",";

  protected static final Color DEFAULT_BACK_COLOR = new Color(0xC0, 0xC0, 0xC0);
  protected static final Color DEFAULT_COLOR_USUARI_0 = Color.blue;
  protected static final Color DEFAULT_COLOR_USUARI_1 = DEFAULT_BACK_COLOR;
  protected static final Color DEFAULT_COLOR_ERROR_0 = Color.white;
  protected static final Color DEFAULT_COLOR_ERROR_1 = Color.red;
  protected static final TripleColor DEFAULT_TRIPLE_COLOR = new TripleColor(Color.white, Color.black, Color.gray,
      false);

  // Extensions
  public static final String extName[] = { "*.BMP", "*.GIF", "*.TXT", "*.PUZ", "*.SOP", "*.ASS", "*.CRW", "*.TXA",
      "*.PAC", "*.PCC", "*.P?C", "*.HLP", "*.MVB", "*.EXE", "*.MID", "*.WAV", "*.JPG", "*.PNG", BLANK };
  public static final int EXT_BMP = 0, EXT_GIF = 1, EXT_TXT = 2, EXT_PUZ = 3, EXT_SOP = 4, EXT_ASS = 5, EXT_CRW = 6,
      EXT_TXA = 7, EXT_PAC = 8, EXT_PCC = 9, EXT_PCG = 10, EXT_HLP = 11, EXT_MVB = 12, EXT_EXE = 13, EXT_MID = 14,
      EXT_WAV = 15, EXT_JPG = 16, EXT_PNG = 17, UNKNOWN_EXT = 18, NO_EXT = -1;

  protected static final int MAXCW = 20, MAXCH = 20;
  protected static final int MAXGW = 21, MAXGH = 10;

  // PlayModes
  public static final int NONE = 0, PUZZLE = 1, SOPA = 2, ASSOCIA = 3, CREUATS = 4, TEXTACT = 5;

  // PuzModes
  public static final int INTERC = 0, FORAT = 1, DOBLE = 2, MEMORY = 3;

  // AssModes
  public static final int NORMAL = 0, ESPECIAL = 1, EXPLORA = 2, INFO = 3, IDENTIFICA = 4, ESCRIU = 5;

  // TxActModes
  public static final int FORATS = 0, COMPTEXT = 1, IDLLETRES = 2, IDPARAULES = 3, BPARAULES = 4, BPARAGRAFS = 5;

  // GraPositions
  public static final int AB = 0, BA = 1, AUB = 2, BUA = 3;
  public static final int DEF_TXTCASW = 140, DEF_TXTCASH = 100, DEF_CICLES = 30;
  public static final int NEGRE = 160;
  public static final char COMODI = '*';
  public static final char SEPARADOR = ';';
  public static final byte ENDDESC_BYTE = -121; // 0x87
  // public static final char ENDDESC = '\u0000';
  public static final char ENDDESC = '\u2021';
  public static final char CHBLOCK = '\u008E';
  public static final char CHINC = '\u008F';
  public static final char SEP = '\u0090';

  /*
   * Constants used in Clic 3.0 not needed in JClic
   *
   * WM_USERIDS WM_PLAYW=WM_USER, WM_HELPW, WM_CBNEXT, WM_CBPREV, WM_CBGETFOCUS,
   * WM_NOUINTENT};
   *
   * enum USERMESSAGES {PLAYW_RESIZE, PLAYW_ENDGAME, PLAYW_INTENT, PLAYW_ENCERTS,
   * PLAYW_WAITMEIN, PLAYW_SETMESS, PLAYW_SETMESSPLAY, HELPW_ITEMSEL,
   * HELPW_MOUSE};
   *
   * public static final int WDIGIT = 13, HDIGIT = 23; public static final int
   * MARGE = 6; public static final int BOTOW = 63, BOTOW2 = 30, BOTOH = 39;
   *
   * public static final int DIGITSW = 39, DIGITSH = 23;
   *
   * public static final int CTRLH = 39; // era 41 public static final int CTRLW =
   * 450;
   *
   * public static final int CLW = (3*MARGE+BOTOW+CTRLW); public static final int
   * CLH = (6*BOTOH+7*MARGE);
   *
   * public static final int PLWW = CTRLW; public static final int PLWH =
   * (CLH-CTRLH-3*MARGE);
   *
   * public static final int LMAXRFTXT = 512;
   *
   * String resources (to be revised!) public static final int IDS_ERRFMT = 1;
   *
   * Common variables
   *
   * public static int passos, passos0; public static int gAjVis, encertsNow;
   * public static boolean quick, quick0, arrossega, arrossega0, sonor0,
   * autoCursor, cursorGran; public static boolean doReportFile, drf0, exitDirect;
   * public static boolean helponhelp, menus; public static boolean allowNewUsr,
   * allowNewGrp, showUserReports; public static int caretVis, caretX, caretY,
   * caretW, caretH, autoCursorDelay; public static final Font logFont1 = new
   * Font("Arial", 0, 12); public static int gruixfil;
   *
   * public static String clicPath = ""; public static String ediText = ""; public
   * static String ediGraf = ""; public static String originalPath = ""; public
   * static String playerId = ""; public static String iniFile = ""; public static
   * String dbFile = ""; public static String originalDbFile = ""; public static
   * String fileName0 = "";
   *
   * extern char cdUnit; public static String lastExt = "   ";
   */

  public static int getExt(String fileName) {
    int begExt;
    String ext;
    int i;

    if (fileName == null || fileName.length() == 0)
      return UNKNOWN_EXT;

    if ((begExt = fileName.indexOf(' ')) < 0)
      begExt = fileName.length() - 1;

    for (; begExt > 0; begExt--)
      if (fileName.charAt(begExt) == '.')
        break;

    if (begExt == 0 || fileName.length() - begExt < 4)
      return UNKNOWN_EXT;

    ext = "*" + fileName.substring(begExt, begExt + 4).toUpperCase();
    for (i = 0; i < UNKNOWN_EXT; i++)
      if (ext.equals(extName[i]))
        break;

    if (i == EXT_JPG || i == EXT_PNG)
      i = EXT_GIF;

    return i;
  }

  public static boolean isClic3Extension(int ext) {

    return ext == EXT_PUZ || ext == EXT_ASS || ext == EXT_SOP || ext == EXT_CRW || ext == EXT_TXA;
  }

  public static int parseIntX(String s) {
    return parseIntX(s, 10);
  }

  public static int parseIntX(String s, int radix) {
    int v;
    try {
      v = Integer.parseInt(s, radix);
    } catch (NumberFormatException e) {
      v = 0;
    }
    return v;
  }

  protected static int correctColor(int color) {
    return (color & 0xFF) << 16 | (color & 0xFF00) | (color & 0xFF0000) >> 16;
  }

  private static final String[] strArray = { "FFFFFF", "000000", "808080", "C0C0C0", "0000FF", "00FF00", "FF0000" };
  private static final Color[] colorArray = { Color.white, Color.black, Color.gray, DEFAULT_BACK_COLOR, Color.red,
      Color.green, Color.blue };

  protected static Color strToColor(String st) {
    if (st == null)
      return Color.black;
    if (st.length() > 6)
      st = st.substring(st.length() - 6);
    for (int i = 0; i < strArray.length; i++)
      if (st.equals(strArray[i]))
        return colorArray[i];

    return new Color(correctColor(parseIntX(st, 16)));
  }

  public static boolean copyArray(String[] dest, int indest, String[] source, int insource, int e, boolean upper) {
    int i, d, s;

    for (i = 0, d = indest, s = insource; i < e; i++, d++, s++) {
      if ((dest[d] = source[s]) == null)
        break;
      if (upper)
        dest[d] = dest[d].toUpperCase();
    }
    return (i == e);
  }

  public static void readPccFile(JClicProject project) throws Exception {
    String name;
    byte[] bytes;
    MediaBagElement mbe;
    int i, j;

    if (!(project.getFileSystem() instanceof PCCFileSystem))
      throw new Exception("FileSystem is not PCCFileSystem!");

    PCCFileSystem pccfs = (PCCFileSystem) project.getFileSystem();

    for (i = 0; i < pccfs.numFiles; i++) {
      name = pccfs.getEntryName(i);

      switch (getExt(name)) {
      case EXT_PAC:
        addPacToSequence(project, name);
        break;

      case EXT_ASS:
      case EXT_PUZ:
      case EXT_SOP:
      case EXT_CRW:
      case EXT_TXA:
        addActivityToBag(project, name /* , pccfs */);
        break;

      default:
        // do nothing;
      }
    }
  }

  private static int gcCounter = 0;

  public static void addActivityToBag(JClicProject project, String name) throws Exception {

    ActivityBag ab = project.activityBag;

    String normalizedName = FileSystem.stdFn(name);

    if (ab.getElementByName(normalizedName) != null)
      return;

    Clic3Activity c3a = new Clic3Activity(project);
    try {
      c3a.load(normalizedName, project.getFileSystem().getBytes(normalizedName));
    } catch (Exception e) {
      System.err.println("Error loading " + normalizedName + "\n" + e);
      throw e;
    }

    switch (c3a.actMode) {
    case ASSOCIA:
      switch (c3a.puzMode) {
      case INFO:
        c3a.className = "edu.xtec.jclic.activities.panels.InformationScreen";
        break;
      case NORMAL:
        c3a.className = "edu.xtec.jclic.activities.associations.SimpleAssociation";
        break;
      case ESPECIAL:
        c3a.className = "edu.xtec.jclic.activities.associations.ComplexAssociation";
        break;
      case IDENTIFICA:
        c3a.className = "edu.xtec.jclic.activities.panels.Identify";
        break;
      case EXPLORA:
        c3a.className = "edu.xtec.jclic.activities.panels.Explore";
        break;
      case ESCRIU:
        c3a.className = "edu.xtec.jclic.activities.text.WrittenAnswer";
        break;
      }
      break;

    case PUZZLE:
      switch (c3a.puzMode) {
      case INTERC:
        c3a.className = "edu.xtec.jclic.activities.puzzles.ExchangePuzzle";
        break;
      case DOBLE:
        c3a.className = "edu.xtec.jclic.activities.puzzles.DoublePuzzle";
        break;
      case MEMORY:
        c3a.className = "edu.xtec.jclic.activities.memory.MemoryGame";
        break;
      case FORAT:
        c3a.className = "edu.xtec.jclic.activities.puzzles.HolePuzzle";
        break;
      }
      break;

    case SOPA:
      c3a.className = "edu.xtec.jclic.activities.textGrid.WordSearch";
      break;

    case CREUATS:
      c3a.className = "edu.xtec.jclic.activities.textGrid.CrossWord";
      break;

    case TEXTACT:
      switch (c3a.puzMode) {
      case FORATS:
        c3a.className = "edu.xtec.jclic.activities.text.FillInBlanks";
        break;
      case COMPTEXT:
        c3a.className = "edu.xtec.jclic.activities.text.Complete";
        break;
      case IDLLETRES:
      case IDPARAULES:
        c3a.className = "edu.xtec.jclic.activities.text.Identify";
        break;
      case BPARAULES:
      case BPARAGRAFS:
        c3a.className = "edu.xtec.jclic.activities.text.Order";
        break;
      }
      break;

    default:
      throw new IllegalArgumentException("Unknown Clic3 activity!");
    }

    // WARNING:

    ab.addActivity(Activity.getActivity(c3a, project));
    // clear memory
    project.mediaBag.clearData();
    // force a gc every 50 activities
    if ((++gcCounter % 50) == 0)
      System.gc();
  }

  public static void addPacToSequence(JClicProject project, String name) throws Exception {

    String normalizedName = FileSystem.stdFn(name);

    if (project.activitySequence.getElementByTag(normalizedName, false) != null)
      return;

    FileSystem fs = project.getFileSystem();

    Clic3Pac c3p = new Clic3Pac();

    try {
      c3p.load(normalizedName, fs.getBytes(normalizedName));
    } catch (Exception e) {
      System.err.println("Error loading " + normalizedName + "\n" + e);
      throw e;
    }

    for (int i = 0; i < c3p.nActs; i++) {
      ActivitySequenceElement ase = new ActivitySequenceElement(c3p.acts[i], c3p.lapToPass,
          c3p.noPassButtons ? ActivitySequenceElement.NAV_NONE : ActivitySequenceElement.NAV_BOTH);
      if (i == 0) {
        ase.backJump = new ActivitySequenceJump(JumpInfo.STOP);
        ase.setTag(normalizedName);
        ase.setDescription(c3p.fileDesc != null && c3p.fileDesc.length() > 0 ? c3p.fileDesc : null);
      }
      if (i == c3p.nActs - 1) {
        ActivitySequenceJump asj = new ActivitySequenceJump(JumpInfo.STOP);
        if (c3p.def == true && c3p.chDef != null && getExt(c3p.chDef) == EXT_PAC) {
          asj.action = JumpInfo.JUMP;
          asj.sequence = fs.getCanonicalNameOf(c3p.chDef);
          if (c3p.sup) {
            asj.setConditionalJump(new ConditionalJumpInfo(JumpInfo.JUMP, fs.getCanonicalNameOf(c3p.chSup), c3p.supP,
                c3p.supTFlag ? c3p.supT : -1), true);
          }
          if (c3p.inf) {
            asj.setConditionalJump(new ConditionalJumpInfo(JumpInfo.JUMP, fs.getCanonicalNameOf(c3p.chInf), c3p.infP,
                c3p.infTFlag ? c3p.infT : -1), false);
          }
        }
        ase.fwdJump = asj;
      }
      project.activitySequence.add(ase);
    }
  }

  public static final String ansiChars = "\u20ac\u0081\u201a\u0192\u201e\u2026\u2020\u2021\u02c6\u2030\u0160\u2039\u0152"
      + "\u008d\u008e\u008f\u0090\u2018\u2019\u201c\u201d\u2022\u2013\u2014\u02dc\u2122"
      + "\u0161\u203a\u0153\u009d\u009e\u0178";
  public static final String convertFileChars = "e__f______s_c_z__________ts_o_zy_icl_y_s_ca___r_o_23__p__10_423_aaaaaaa"
      + "ceeeeiiiidnooooox0uuuuy_baaaaaaaceeeeiiiionooooo_0uuuuy_y";

  // 0x80 is Euro: 20ac
  // bytes used:
  // ENDDESC 0x87 ('\u2021')
  // CHBLOCK 0x8E (not used)
  // CHINC 0x8F (not used)
  // SEP ? 0x90 (not used)

  public static String bufferToString(byte[] buffer) {
    if (buffer == null || buffer.length == 0)
      return BLANK;
    char[] result = new char[buffer.length];
    int v;
    for (int i = 0; i < buffer.length; i++) {
      v = buffer[i] & 0x00FF;
      if (v < 128 || v >= 160)
        // warning: direct map from int to char
        result[i] = (char) v;
      else
        result[i] = ansiChars.charAt(v - 128);
    }
    return new String(result);
  }

  public static String[] dataToArray(byte[] data) {
    ArrayList<String> v = new ArrayList<String>();
    int i, j, k, l, l2;
    byte[] buffer;
    String[] array;
    int nLines = 0;

    l = data.length;
    for (i = 0, j = 0; i < l;) {
      for (; j < l; j++)
        if (data[j] == 0x0D || data[j] == 0x0A)
          break;
      l2 = j - i;
      if (l2 > 0) {
        buffer = new byte[l2];
        for (k = 0; k < l2; k++)
          buffer[k] = data[i + k];
        v.add(bufferToString(buffer));
      } else
        v.add(BLANK);
      nLines++;
      if (j < l && data[++j] == 0x0A)
        j++;
      i = j;
    }

    if (nLines > 0) {
      array = new String[nLines];
      try {
        array = v.toArray(array);
      } catch (ArrayStoreException e) {
        array = null;
      }
    } else
      array = null;

    return array;
  }

  public static final String validFileChars = ".:\\_!~0123456789abcdefghijklmnopqrstuvwxyz";

  public static String validFileName(String name, boolean strict, boolean remember) {
    if (name == null)
      return null;
    StringBuilder sb = new StringBuilder();
    for (char ch : name.toCharArray()) {
      if (validFileChars.indexOf(ch, strict ? 3 : 0) < 0) {
        if (ch >= 'A' && ch <= 'Z')
          ch = Character.toLowerCase(ch);
        else if (ch == '/')
          ch = '\\';
        else {
          int k = ansiChars.indexOf(ch);
          if (k >= 0)
            ch = convertFileChars.charAt(k);
          else
            ch = '_';
        }
      }
      sb.append(ch);
    }
    String result = sb.substring(0);
    if (remember && !result.equals(name)) {
      FileSystem.altFileNames.put(result, name);
    }
    return result;
  }

  public static String validFileName(String name) {
    return validFileName(name, false, true);
  }

  public static String pacNameToLowerCase(String s) {
    String result = s;
    if (s != null) {
      String l = s.toLowerCase();
      if (l.endsWith(".pac") || l.endsWith(".pcc"))
        result = l;
    }
    return result;
  }
}
