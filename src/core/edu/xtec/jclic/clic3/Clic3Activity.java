/*
 * File    : Clic3Activity.java
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

import edu.xtec.jclic.boxes.ActiveBagContent;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.media.MediaContent;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.shapers.HolesMaker;
import edu.xtec.jclic.shapers.Rectangular;
import edu.xtec.jclic.shapers.Shaper;
import edu.xtec.util.FontCheck;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.StrUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.StringTokenizer;

/**
 * This class encapsulates the main properties of a Clic 3.0 activity, and
 * provides methods to load it from a data stream and to convert it to a JClic
 * {@link edu.xtec.jclic.Activity}.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class Clic3Activity extends Clic3 {

  public JClicProject project;

  public static final int TOO_MUCH_CELLS = 14;
  private static final Boolean BF = false;

  public String rPath = null;
  public String className = null;
  public String fileName;
  public String[] fileNameCont = new String[3];
  public int actMode, puzMode, graPos;
  public int ncw, nch;
  public int nctxw, nctxh;
  public int txtCW, txtCH, txtCW2, txtCH2;
  public int[] ntags = new int[2];
  public int[] cont = new int[3];
  public boolean[] bar = new boolean[2];
  public boolean[] delim = new boolean[2];
  public boolean stretch;
  public String initMess;
  public String endMess;
  public String fileDesc;
  public String custHelpFile;
  public String hlpTopic;
  public String[][] tags = new String[2][];
  public String[] graTxt;
  public int[] nLines = new int[3];
  public int[] ass = new int[MAXCW * MAXCH];
  public boolean mAss, invAss, shHelp, shPuz, sol;
  public boolean okToNext, btCorregir;
  public boolean avCont, avMaj, avAcc, avDblSpc, avPunt, avLletra, brPar, avNoSalta;
  public boolean noBV, shDisk, shPrint, shPorta, barraAmunt, noAv;
  public boolean custHlp, tileBmp, pwTransp, pwrp;
  public int pwrx, pwry;
  public int avPrevHelp, avTimePH, tabSpc;
  public int avScope, avMaxScope;
  public Font[] logF = new Font[4];
  public TripleColor[] colors = new TripleColor[4];
  public Color[] colorFons = new Color[2];
  public Color[] colorUsuari = new Color[2];
  public Color[] colorError = new Color[2];
  public boolean useDLL;
  public String rgDLL;
  public byte[] dllOptions;
  public boolean[] comptadors = new boolean[3];
  public boolean marcs;
  public int btTipus;
  public String txBase;
  public String txBtCorregir;
  public String txPrev;
  public String bmpFons;
  public String initMessPrev;
  public String actd;

  /** Creates new Clic3Activity */
  public Clic3Activity(JClicProject project) {
    this.project = project;
    clear();
    rPath = BLANK;
  }

  public void clear() {
    int i;

    actMode = NONE;
    puzMode = INTERC;
    graPos = AB;
    ncw = nch = nctxw = nctxh = 0;
    txtCW = txtCW2 = DEF_TXTCASW;
    txtCH = txtCH2 = DEF_TXTCASH;
    stretch = false;
    btCorregir = true;
    avCont = true;
    avAcc = true;
    avMaj = true;
    brPar = false;
    avDblSpc = false;
    avPunt = true;
    avLletra = true;
    avNoSalta = true;
    avScope = 3;
    avMaxScope = 6;

    fileName = initMess = endMess = fileDesc = custHelpFile = hlpTopic = BLANK;
    for (i = 0; i < (MAXCW * MAXCH); i++)
      ass[i] = -2;
    mAss = false;
    invAss = false;
    shHelp = true;
    shPuz = true;
    shDisk = true;
    shPrint = false;
    shPorta = true;
    custHlp = false;
    noBV = false;
    barraAmunt = false;
    okToNext = false;
    noAv = false;
    tabSpc = 16;

    for (i = 0; i < 3; i++) {
      fileNameCont[i] = BLANK;
      cont[i] = EXT_TXT;
      nLines[i] = 0;
      comptadors[i] = true;
    }

    for (i = 0; i < 2; i++) {
      ntags[i] = 0;
      tags[i] = null;
      bar[i] = true;
      delim[i] = true;
      colorFons[i] = DEFAULT_BACK_COLOR;
    }

    colorUsuari[0] = DEFAULT_COLOR_USUARI_0;
    colorUsuari[1] = DEFAULT_COLOR_USUARI_1;
    colorError[0] = DEFAULT_COLOR_ERROR_0;
    colorError[1] = DEFAULT_COLOR_ERROR_1;

    graTxt = null;
    for (i = 0; i < 4; i++) {
      logF[i] = BoxBase.getDefaultFont();
      colors[i] = (TripleColor) DEFAULT_TRIPLE_COLOR.clone();
    }

    colors[2].backColor = DEFAULT_BACK_COLOR;

    marcs = true;

    useDLL = false;
    dllOptions = null;
    rgDLL = BLANK;

    txBase = txBtCorregir = txPrev = bmpFons = initMessPrev = BLANK;
    avPrevHelp = 0;
    avTimePH = 30;
    btTipus = 1;
    tileBmp = true;
    pwTransp = false;
    pwrp = false;
    pwrx = pwry = 0;
  }

  Font stringToFont(String s) {
    int lfHeight, lfWeight, lfItalic;
    String lfFaceName;
    float size;
    int style;
    StringTokenizer st;

    st = new StringTokenizer(s, COMMA);
    lfHeight = parseIntX(st.nextToken());
    for (int i = 0; i < 3; i++)
      st.nextToken();
    lfWeight = parseIntX(st.nextToken());
    lfItalic = parseIntX(st.nextToken());
    for (int i = 0; i < 7; i++)
      st.nextToken();
    lfFaceName = st.nextToken().trim();

    size = (lfHeight * 10) / 12;
    if (size < BoxBase.MIN_FONT_SIZE)
      size = BoxBase.MIN_FONT_SIZE;
    style = 0;
    if (lfWeight >= 700)
      style |= Font.BOLD;
    if (lfItalic != 0)
      style |= Font.ITALIC;
    if ("system".equalsIgnoreCase(lfFaceName)) {
      lfFaceName = "Arial";
      size = 13;
      style |= Font.BOLD;
    }

    return FontCheck.getValidFont(lfFaceName, style, (int) size);
  }

  TripleColor stringToTripleColor(String str) {
    TripleColor c = new TripleColor();
    StringTokenizer st;
    String tx;

    st = new StringTokenizer(str, COMMA);

    c.backColor = strToColor(st.nextToken());
    c.textColor = strToColor(st.nextToken());
    c.shadowColor = strToColor(st.nextToken());
    c.shadow = (st.nextToken().compareTo("1") == 0);

    return c;
  }

  public boolean load(String name, byte[] data) {
    String[] txt;
    StringBuilder tmptx = new StringBuilder();
    String fn = BLANK;
    int i, l, tipus, nLin, fLine, ver, ncas, k;
    boolean result = false;
    int boolParms = 0;

    fileName = FileSystem.getCanonicalNameOf(validFileName(name), false);
    rPath = FileSystem.getPathPartOf(fileName);

    fLine = 0;
    nLin = 0;
    ver = 0;

    tipus = getExt(fileName);

    if (isClic3Extension(tipus) && (txt = dataToArray(data)) != null && (nLin = txt.length) > 0) {
      if (nLin > 2 && (ver = (ver = parseIntX(txt[fLine++])) > 1000 ? ver - 1000 : ver) >= 100 && ver <= CLICVER) {
        if (ver > 107) {
          StringBuilder sb = new StringBuilder();
          while (fLine < nLin) {
            if (txt[fLine].length() > 0 && txt[fLine].charAt(0) == ENDDESC) {
              fLine++;
              break;
            }
            if (sb.length() > 0)
              sb.append("\r\n");
            sb.append(txt[fLine++]);
          }
          fileDesc = sb.substring(0);
        }

        if (ver > 100) {
          for (i = 0; i < (ver < 118 ? 1 : 3); i++) {
            logF[i] = stringToFont(txt[fLine++]);
            if (ver >= 118)
              colors[i] = stringToTripleColor(txt[fLine++]);
          }
          if (ver < 118)
            for (i = 1; i < 4; i++)
              logF[i] = logF[0];
        }

        if (ver > 118) {
          StringTokenizer st = new StringTokenizer(txt[fLine++], COMMA);
          comptadors[1] = (parseIntX(st.nextToken()) != 0);
          comptadors[2] = (parseIntX(st.nextToken()) != 0);
          comptadors[0] = (parseIntX(st.nextToken()) != 0);
          marcs = (parseIntX(st.nextToken()) != 0);
          colorFons[0] = strToColor(st.nextToken());
          colorFons[1] = strToColor(st.nextToken());

          st.nextToken();
          st.nextToken();

          btTipus = parseIntX(st.nextToken());
          pwrx = parseIntX(st.nextToken());
          pwry = parseIntX(st.nextToken());
        } else if (ver == 118) {
          StringTokenizer st = new StringTokenizer(txt[fLine++], COMMA);
          comptadors[0] = (parseIntX(st.nextToken()) != 0);
          comptadors[1] = (parseIntX(st.nextToken()) != 0);
          comptadors[2] = (parseIntX(st.nextToken()) != 0);
          marcs = (parseIntX(st.nextToken()) != 0);
          colorFons[0] = strToColor(st.nextToken());
          colorFons[1] = strToColor(st.nextToken());
          st.nextToken();
          st.nextToken();
        }

        if (ver < 119)
          btTipus = 0;

        if (ver >= 119)
          if (txt[fLine++].length() > 0)
            bmpFons = FileSystem.getCanonicalNameOf(rPath + validFileName(txt[fLine - 1]), BF);

        initMess = txt[fLine++];
        endMess = txt[fLine++];

        if (ver > 102) {
          boolParms = parseIntX(txt[fLine++]);
          delim[0] = (boolParms & 0x0001) != 0;
          delim[1] = (boolParms & 0x0002) != 0;
        }

        if (ver > 105) {
          StringTokenizer st = new StringTokenizer(txt[fLine++], ";");
          txtCW = parseIntX(st.nextToken());
          if (st.hasMoreTokens())
            txtCW2 = parseIntX(st.nextToken());
          else
            txtCW2 = txtCW;

          st = new StringTokenizer(txt[fLine++], ";");
          txtCH = parseIntX(st.nextToken());
          if (st.hasMoreTokens())
            txtCH2 = parseIntX(st.nextToken());
          else
            txtCH2 = txtCH;
        } else {
          txtCW = txtCW2 = DEF_TXTCASW;
          txtCH = txtCH2 = DEF_TXTCASH;
        }

        if (ver < 118 && (tipus == EXT_SOP || tipus == EXT_CRW)) {
          txtCW = 20;
          txtCH = 20;
        }

        shHelp = (boolParms & 0x0004) == 0;
        graPos = (boolParms & (0x0018)) >> 3;
        shPuz = (boolParms & 0x0020) == 0;
        noBV = (boolParms & 0x0040) != 0;
        if (ver >= 118) {
          shDisk = (boolParms & 0x0080) != 0;
          shPorta = (boolParms & 0x0100) != 0;
          barraAmunt = (boolParms & 0x0200) != 0;
        }

        if (ver >= 119) {
          invAss = (boolParms & 0x400) != 0;
          tileBmp = (boolParms & 0x800) != 0;
          pwTransp = (boolParms & 0x1000) != 0;
          pwrp = (boolParms & 0x2000) != 0;
          noAv = (boolParms & 0x4000) != 0;
          shPrint = (boolParms & 0x8000) != 0;
        }

        if (ver > 108) {
          custHlp = (parseIntX(txt[fLine++]) != 0);
          // ADD RPATH?
          hlpTopic = validFileName(txt[fLine++]);
          custHelpFile = validFileName(txt[fLine++]);
        }

        if (ver > 114) {
          useDLL = (parseIntX(txt[fLine++]) != 0);
          rgDLL = validFileName(txt[fLine++]);
          dllOptions = StrUtils.extractByteSeq(data, fLine++, (byte) 0, ENDDESC_BYTE);
        }

        switch (tipus) {
        case EXT_PUZ:
          actMode = PUZZLE;
          if (nLin < fLine + 4)
            break;
          fileNameCont[0] = FileSystem.getCanonicalNameOf(rPath + validFileName(txt[fLine++]), BF);
          if ((cont[0] = getExt(fileNameCont[0])) > EXT_TXT || (ncw = parseIntX(txt[fLine++])) < 1
              || (nch = parseIntX(txt[fLine++])) < 1 || ncw > MAXCW || nch > MAXCH || (ncw == 1 && nch == 1)
              || (puzMode = parseIntX(txt[fLine++])) < INTERC || puzMode > MEMORY)
            break;
          stretch = (nLin > fLine && parseIntX(txt[fLine++]) == 1);
          bar[0] = bar[1] = true;
          if (ver < 120 && puzMode == MEMORY)
            graPos = AUB;
          result = true;
          break;

        case EXT_ASS:
          actMode = ASSOCIA;
          if (nLin < fLine + 7)
            break;
          if (ver > 109)
            puzMode = parseIntX(txt[fLine++]);
          if ((ncw = parseIntX(txt[fLine++])) < 1 || (nch = parseIntX(txt[fLine++])) < 1 || ncw > MAXCW || nch > MAXCH)
            break;

          fileNameCont[0] = FileSystem.getCanonicalNameOf(rPath + validFileName(txt[fLine++]), BF);
          if ((cont[0] = getExt(fileNameCont[0])) > EXT_TXT)
            break;

          fileNameCont[1] = FileSystem.getCanonicalNameOf(rPath + validFileName(txt[fLine++]), BF);
          if (puzMode != INFO && puzMode != IDENTIFICA && (cont[1] = getExt(fileNameCont[1])) > EXT_TXT)
            break;

          bar[0] = (parseIntX(txt[fLine++]) == 1);
          bar[1] = (parseIntX(txt[fLine++]) == 1);
          if (puzMode == IDENTIFICA || puzMode == ESCRIU)
            bar[1] = false;
          else if (puzMode == EXPLORA || puzMode == INFO)
            bar[0] = bar[1] = false;
          stretch = true;

          if (ver > 106) {
            if (sol = (parseIntX(txt[fLine++]) != 0)) {
              fileNameCont[2] = FileSystem.getCanonicalNameOf(rPath + validFileName(txt[fLine++]), BF);
              if ((cont[2] = getExt(fileNameCont[2])) > EXT_TXT)
                break;
            }
          }

          if (ver > 101) {
            if (nLin < fLine + 1)
              break;
            mAss = (parseIntX(txt[fLine++]) == 1);
            if (mAss) {
              if (puzMode == NORMAL)
                puzMode = ESPECIAL;
              ncas = ncw * nch;
              if (nLin < fLine + ncas + 2)
                break;
              if ((nctxw = parseIntX(txt[fLine++])) < 1 || (nctxh = parseIntX(txt[fLine++])) < 1 || nctxw > MAXCW
                  || nctxh > MAXCH)
                break;
              for (i = 0; i < ncas; i++)
                ass[i] = parseIntX(txt[fLine++]);
            }
          }

          if (puzMode == INFO || puzMode == EXPLORA)
            noAv = true;
          result = true;
          break;

        case EXT_SOP:
          actMode = SOPA;
          if (nLin < fLine + 7)
            break;

          fileNameCont[0] = txt[fLine++];
          if (fileNameCont[0].length() > 0 && fileNameCont[0].charAt(0) == '*') {
            bar[0] = false;
            fileNameCont[0] = BLANK;
            cont[0] = EXT_BMP;
          } else {
            fileNameCont[0] = FileSystem.getCanonicalNameOf(rPath + validFileName(fileNameCont[0]), BF);
            bar[0] = true;
          }

          if ((bar[0] && (cont[0] = getExt(fileNameCont[0])) > EXT_TXT) || (ncw = parseIntX(txt[fLine++])) < 1
              || (nch = parseIntX(txt[fLine++])) < 1 || ncw > MAXCW || nch > MAXCH || (ncw == 1 && nch == 1)
              || (nctxw = parseIntX(txt[fLine++])) < 1 || (nctxh = parseIntX(txt[fLine++])) < 1 || nctxw > MAXGW
              || nctxh > MAXGH || (nctxw == 1 && nctxh == 1) || (ntags[0] = parseIntX(txt[fLine++])) < 1
              || nLin < (nctxh + ntags[0] + fLine) || (tags[0] = new String[ntags[0]]) == null
              || (graTxt = new String[nctxh]) == null || copyArray(graTxt, 0, txt, fLine, nctxh, true) == false
              || copyArray(tags[0], 0, txt, fLine + nctxh, ntags[0], true) == false)
            break;
          result = true;
          stretch = true;
          break;

        case EXT_CRW:
          actMode = CREUATS;
          if (nLin < fLine + 6)
            break;
          if ((nctxw = parseIntX(txt[fLine++])) < 1 || (nctxh = parseIntX(txt[fLine++])) < 1 || nctxw > MAXGW
              || nctxh > MAXGH || (nctxw == 1 && nctxh == 1)
              || nLin < (fLine + nctxh + (ntags[0] = nctxh) + (ntags[1] = nctxw))
              || (graTxt = new String[nctxh]) == null || (tags[0] = new String[ntags[0]]) == null
              || (tags[1] = new String[ntags[1]]) == null || copyArray(graTxt, 0, txt, fLine, nctxh, true) == false
              || copyArray(tags[0], 0, txt, fLine + nctxh, ntags[0], false) == false
              || copyArray(tags[1], 0, txt, fLine + nctxh + ntags[0], ntags[1], false) == false)
            break;

          bar[0] = bar[1] = false;
          ncw = 1;
          nch = 4;
          stretch = false;
          result = true;
          break;

        case EXT_TXA:
          actMode = TEXTACT;
          logF[3] = stringToFont(txt[fLine++]);
          colors[3] = stringToTripleColor(txt[fLine++]);
          StringTokenizer st = new StringTokenizer(txt[fLine++], COMMA);
          colorUsuari[0] = strToColor(st.nextToken());
          colorUsuari[1] = strToColor(st.nextToken());
          colorError[0] = strToColor(st.nextToken());
          colorError[1] = strToColor(st.nextToken());

          st = new StringTokenizer(txt[fLine++], COMMA);
          puzMode = parseIntX(st.nextToken());
          boolParms = parseIntX(st.nextToken());
          avScope = parseIntX(st.nextToken());
          avMaxScope = parseIntX(st.nextToken());
          avPrevHelp = parseIntX(st.nextToken());
          avTimePH = parseIntX(st.nextToken());
          tabSpc = parseIntX(st.nextToken());

          okToNext = (boolParms & 0x0001) != 0;
          btCorregir = (boolParms & 0x0002) != 0;
          avCont = (boolParms & 0x0004) != 0;
          avMaj = (boolParms & 0x0008) != 0;
          avAcc = (boolParms & 0x0010) != 0;
          brPar = (boolParms & 0x0020) != 0;
          avDblSpc = (boolParms & 0x0040) != 0;
          avPunt = (boolParms & 0x0080) != 0;
          avLletra = (boolParms & 0x0100) != 0;
          avNoSalta = (boolParms & 0x0200) != 0;
          txBtCorregir = txt[fLine++];
          for (k = 0; k < 3; k++) {
            tmptx.setLength(0);
            while (fLine < nLin) {
              if (txt[fLine].length() > 0 && txt[fLine].charAt(0) == ENDDESC) {
                fLine++;
                break;
              }
              if (tmptx.length() > 0)
                tmptx.append("\n");
              tmptx.append(txt[fLine++]);
            }
            switch (k) {
            case 0:
              txBase = tmptx.substring(0);
              break;
            case 1:
              txPrev = tmptx.substring(0);
              break;
            case 2:
              initMessPrev = tmptx.substring(0);
              break;
            }
          }
          if (btCorregir == false && (puzMode != FORATS || avCont == false))
            noAv = true;

          result = (txBase != null);

        default:
          break;
        }
      }

      if (!result) {
        clear();
      }
    } else
      clear();
    return result;
  }

  public BoxBase getBoxBase(int i) {
    // we have only 4 box bases
    if (i > 3)
      return null;

    BoxBase bb = new BoxBase();
    if (!colors[i].backColor.equals(BoxBase.DEFAULT_BACK_COLOR))
      bb.backColor = colors[i].backColor;
    if (!colors[i].textColor.equals(BoxBase.DEFAULT_TEXT_COLOR))
      bb.textColor = colors[i].textColor;
    if (!colors[i].shadowColor.equals(Color.gray)) {
      if (colors[i].shadow)
        bb.shadowColor = colors[i].shadowColor;
      bb.inactiveColor = colors[i].shadowColor;
      bb.alternativeColor = colors[i].shadowColor;
    }
    bb.shadow = colors[i].shadow;
    if (!logF[i].equals(BoxBase.getDefaultFont()))
      bb.setFont(logF[i]);
    return bb;
  }

  protected ActiveBagContent[] abcCopy = new ActiveBagContent[3];

  public ActiveBagContent createActiveBagContent(int num) throws Exception {
    int cw, ch;
    double w, h;
    ActiveBagContent abc = null;
    boolean specialShape = false;
    boolean noAutoZones = false;
    boolean singleCells = false;
    boolean border;

    if (num < 0 || num >= 3 || fileNameCont[num] == null)
      return abc;

    if (num == 0 || num == 2) {
      cw = ncw;
      ch = nch;
      w = txtCW;
      h = txtCH;
      border = delim[0];
    } else {
      cw = nctxw;
      ch = nctxh;
      w = txtCW2;
      h = txtCH2;
      border = delim[1];
    }

    abc = new ActiveBagContent(cw, ch);
    abc.border = border;

    Shaper sh = null;
    if (fileDesc != null) {
      if (fileDesc.indexOf("noAutoZones") >= 0)
        noAutoZones = true;
      if (fileDesc.indexOf("singleCells") >= 0)
        singleCells = true;
      int i = fileDesc.indexOf("SHAPER:");
      if (i >= 0) {
        StringTokenizer st = new StringTokenizer(fileDesc.substring(i + 7), "|");
        if (st.hasMoreTokens()) {
          org.jdom.Element e = new org.jdom.Element(Shaper.ELEMENT_NAME);
          e.setAttribute(JDomUtility.CLASS, "@" + st.nextToken());
          e.setAttribute(Shaper.COLS, Integer.toString(cw));
          e.setAttribute(Shaper.ROWS, Integer.toString(ch));
          while (st.hasMoreTokens()) {
            String attribute, value;
            attribute = st.nextToken();
            if (st.hasMoreTokens()) {
              value = st.nextToken();
              e.setAttribute(attribute, value);
            }
          }
          try {
            sh = Shaper.getShaper(e);
          } catch (Exception ex) {
            sh = null;
          }
        }
      }
    }

    if (sh == null)
      sh = new Rectangular(cw, ch);
    else
      specialShape = true;

    abc.setShaper(sh);

    if (fileNameCont[num].endsWith(".txt")) {
      String[] content = dataToArray(project.getFileSystem().getBytes(fileNameCont[num]));
      if (content == null) {
        content = new String[cw * ch];
        for (int z = 0; z < cw * ch; z++)
          content[z] = new String();
      }
      setActiveBagTextContent(abc, content, /* rb, */ cw, ch, w, h);
    } else {
      if (!noAutoZones && mAss && !specialShape && cw * ch > TOO_MUCH_CELLS && bar[num & 1] == false) {
        if (num == 2) {
          if (abcCopy[0] != null) {
            abc.setShaper(abcCopy[0].getShaper());
            abc.ncw = abcCopy[0].ncw;
            abc.nch = abcCopy[0].nch;
          }
        } else {
          int[] ids = new int[cw * ch];
          if (num == 0)
            System.arraycopy(ass, 0, ids, 0, cw * ch);
          else {
            for (int i = 0; i < cw * ch; i++)
              ids[i] = -1;
            int k = 0;
            int m = Math.min(ncw * nch, ass.length);
            for (int i = 0; i < m; i++) {
              int j = ass[i];
              if (j >= 0) {
                if (ids[j] == -1) {
                  ids[j] = k;
                  ass[i] = k;
                  k++;
                } else
                  ass[i] = ids[j];
              }
            }
          }
          boolean skipOnes = (actMode == ASSOCIA && puzMode == IDENTIFICA);
          HolesMaker hsm = new HolesMaker(cw, ch, ids, num != 1, skipOnes, singleCells);
          abc.setShaper(hsm.getShaper());
          if (num == 0)
            ass = hsm.getIds();
        }
      }
      abc.setImgContent(project.mediaBag.getImageElement(fileNameCont[num]), true);
    }
    abcCopy[num] = abc;
    return abc;
  }

  public void setActiveBoxTextContent(ActiveBoxContent abc, String tx) throws Exception {
    int i, j;
    String sub, subc;
    String txPrev2;

    abc.mediaContent = null;
    abc.img = null;
    abc.imgName = null;
    abc.rawText = tx;
    txPrev2 = tx;
    while ((i = txPrev2.indexOf('{')) >= 0) {
      j = txPrev2.indexOf('}', i);
      if (j < 0)
        break;
      sub = txPrev2.substring(i + 1, j);
      txPrev2 = txPrev2.substring(0, i) + txPrev2.substring(j + 1);
      sub = sub.trim();
      subc = sub.toLowerCase();
      if (subc.endsWith(".gif") || subc.endsWith(".jpg") || subc.endsWith(".bmp") || subc.endsWith(".png")) {
        abc.imgName = FileSystem.getCanonicalNameOf(rPath + validFileName(sub), BF);
      } else {
        if (abc.mediaContent == null)
          abc.mediaContent = new MediaContent();

        try {
          StringTokenizer st = new StringTokenizer(subc, " ");
          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("stretch"))
              abc.mediaContent.stretch = true;
            else if (token.equals("free"))
              abc.mediaContent.free = true;
            else if (token.equals("pos")) {
              abc.mediaContent.absLocation = new Point();
              abc.mediaContent.absLocation.x = Integer.parseInt(st.nextToken());
              abc.mediaContent.absLocation.y = Integer.parseInt(st.nextToken());
              int k = Integer.parseInt(st.nextToken());
              abc.mediaContent.absLocationFrom = (k == 0 ? MediaContent.FROM_BOX
                  : (k == 1 ? MediaContent.FROM_WINDOW : MediaContent.FROM_FRAME));
            } else if (token.equals("to")) {
              if (abc.mediaContent.mediaType == MediaContent.PLAY_CDAUDIO)
                abc.mediaContent.cdTo = st.nextToken();
              else
                abc.mediaContent.to = Integer.parseInt(st.nextToken());
            } else if (token.equals("from"))
              abc.mediaContent.from = Integer.parseInt(st.nextToken());
            else if (token.equals("exit"))
              abc.mediaContent.mediaType = MediaContent.EXIT;
            else if (token.length() == 4 && token.startsWith("rec")) {
              abc.mediaContent.recBuffer = Integer.parseInt(token.substring(3));
              abc.mediaContent.mediaType = MediaContent.RECORD_AUDIO;
              if (st.hasMoreTokens()) {
                token = st.nextToken();
                if (token.equals("play"))
                  abc.mediaContent.mediaType = MediaContent.PLAY_RECORDED_AUDIO;
                else
                  abc.mediaContent.length = Integer.parseInt(token);
              }
            } else if (token.endsWith(".wav") || token.endsWith(".mp3") || token.endsWith(".ogg")
                || token.endsWith(".au") || token.endsWith(".ra")) {
              abc.mediaContent.mediaType = MediaContent.PLAY_AUDIO;
              abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
            } else if (token.endsWith(".mid")) {
              abc.mediaContent.mediaType = MediaContent.PLAY_MIDI;
              abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
              // Compute MIDI pos!!!
            } else if (token.endsWith(".avi") || token.endsWith(".mpg") || token.endsWith(".swf")) {
              abc.mediaContent.mediaType = MediaContent.PLAY_VIDEO;
              abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
            } else if (token.endsWith(".ass") || token.endsWith(".puz") || token.endsWith(".sop")
                || token.endsWith(".crw") || token.endsWith(".txa")) {
              abc.mediaContent.mediaType = MediaContent.RUN_CLIC_ACTIVITY;
              abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
            } else if (token.endsWith(".pac") || token.endsWith(".pcc")) {
              abc.mediaContent.mediaType = MediaContent.RUN_CLIC_PACKAGE;
              abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
            } else if (token.endsWith(".exe") || token.endsWith(".com") || token.endsWith(".bat")) {
              abc.mediaContent.mediaType = MediaContent.RUN_EXTERNAL;
              // AVOID ABSOLUTE PATH
              abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
              abc.mediaContent.externalParam = BLANK;
              while (st.hasMoreTokens()) {
                // CHECK RPATH
                abc.mediaContent.externalParam = abc.mediaContent.externalParam + " " + st.nextToken();
              }
              abc.mediaContent.externalParam = abc.mediaContent.externalParam.trim();
            } else if (token.endsWith(".htm") || token.endsWith(".html") || token.startsWith("http:")) {
              abc.mediaContent.mediaType = MediaContent.URL;
              if (FileSystem.isStrUrl(token))
                abc.mediaContent.mediaFileName = token;
              else
                abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
            } else if (token.indexOf(".") >= 0) {
              abc.mediaContent.mediaType = MediaContent.UNKNOWN;
              abc.mediaContent.mediaFileName = FileSystem.getCanonicalNameOf(rPath + validFileName(token), BF);
            } else if (token.indexOf(":") >= 0) {
              abc.mediaContent.mediaType = MediaContent.PLAY_CDAUDIO;
              abc.mediaContent.cdFrom = token;
            } else
              System.err.println("Unknown media command: " + token);
          }
        } catch (Exception e) {
          System.err.println("Error parsing media string \"" + sub + "\"\n" + e);
        }
      }
    }
    abc.text = txPrev2;
    abc.realizeContent(project.mediaBag);
  }

  public void setActiveBagTextContent(ActiveBagContent abc, String[] setText, int setNcw, int setNch, double setW,
      double setH) throws Exception {
    setActiveBagTextContent(abc, setText, setNcw, setNch);
    abc.w = setW;
    abc.h = setH;
  }

  public void setActiveBagTextContent(ActiveBagContent abc, String[] txt, int setNcw, int setNch) throws Exception {
    abc.img = null;
    abc.imgName = null;
    abc.ncw = Math.max(1, setNcw);
    abc.nch = Math.max(1, setNch);
    int n = abc.ncw * abc.nch;
    for (int i = 0; i < n; i++)
      setActiveBoxTextContent(abc.getActiveBoxContent(i), ((i >= txt.length || txt[i] == null) ? BLANK : txt[i]));
  }
}
