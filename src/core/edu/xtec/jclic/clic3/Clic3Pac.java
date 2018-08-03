/*
 * File    : Clic3Pac.java
 * Created : 08-nov-2000 20:25
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

package edu.xtec.jclic.clic3;

import edu.xtec.jclic.fileSystem.FileSystem;

/**
 * This class encapsulates a Clic 3.0 package of activities (.pac file), and
 * provides methods to import it from a data stream and to import it into a
 * {@link edu.xtec.jclic.project.JClicProject}.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class Clic3Pac extends Clic3 {

  public String rPath;
  public String fileName;
  public String fileDesc;
  public String[] acts;
  public int nActs;
  public int act;
  public boolean loaded, modified;
  public boolean autoPass, noPassButtons, noDiskButton, noExitButton;
  public int lapToPass;
  public boolean def, sup, inf, supTFlag, infTFlag;
  public String chDef, chSup, chInf;
  public int infP, infT, supP, supT;

  /** Creates new Clic3Pac */
  public Clic3Pac() {
    acts = null;
    chDef = chSup = chInf = null;
    clear();
    rPath = BLANK;
  }

  public void clear() {
    int i;

    acts = null;
    fileName = BLANK;
    fileDesc = BLANK;
    nActs = 0;
    act = 0;
    autoPass = false;
    noPassButtons = false;
    noDiskButton = false;
    noExitButton = false;
    lapToPass = 0;
    loaded = false;
    modified = false;
    def = sup = inf = supTFlag = infTFlag = false;
    infT = supT = 0;
    infP = 25;
    supP = 75;
    chDef = null;
    chSup = null;
    chInf = null;
  }

  public boolean load(String name, byte[] data) {
    String[] txt;
    String fn;
    String str;
    int nLin, fLine, ver;
    boolean result = false;
    int i, boolParms;

    fileName = FileSystem.getCanonicalNameOf(validFileName(name), false);
    rPath = FileSystem.getPathPartOf(fileName);
    fLine = 0;

    if (getExt(fileName) == EXT_PAC && (txt = dataToArray(data)) != null && (nLin = txt.length) > 0) {
      ver = parseIntX(txt[fLine++]);
      if (ver > 1000)
        ver -= 1000;
      if (ver > 107 && ver <= CLICVER) {
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

        if (ver >= 113) {
          boolParms = parseIntX(txt[fLine++]);
          autoPass = (boolParms & 0x0001) != 0;
          noPassButtons = (boolParms & 0x0002) != 0;
          noDiskButton = (boolParms & 0x0004) != 0;
          noExitButton = (boolParms & 0x0008) != 0;
          lapToPass = parseIntX(txt[fLine++]);

          boolParms = parseIntX(txt[fLine++]);
          def = (boolParms & 0x0001) != 0;
          sup = (boolParms & 0x0002) != 0;
          inf = (boolParms & 0x0004) != 0;
          supTFlag = (boolParms & 0x0008) != 0;
          infTFlag = (boolParms & 0x0010) != 0;
          str = txt[fLine++];
          if (str.length() > 0)
            chDef = rPath + validFileName(str);

          str = txt[fLine++];
          if (str.length() > 0)
            chSup = rPath + validFileName(str);

          str = txt[fLine++];
          if (str.length() > 0)
            chInf = rPath + validFileName(str);

          supP = parseIntX(txt[fLine++]);
          supT = parseIntX(txt[fLine++]);
          infP = parseIntX(txt[fLine++]);
          infT = parseIntX(txt[fLine++]);
        }
      }
      if (fLine >= nLin || ver > CLICVER || (nActs = nLin - fLine) < 1 || (acts = new String[nActs]) == null
          || copyArray(acts, 0, txt, fLine, nActs, true) == false)
        result = false;
      else
        result = true;

      if (!result) {
        clear();
      } else {
        for (int l = 0; l < acts.length; l++)
          acts[l] = rPath + validFileName(acts[l]);
        loaded = true;
      }
    } else
      clear();

    return result;
  }
}
