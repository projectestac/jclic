/*
 * File    : Operator.java
 * Created : 08-mar-2004 11:24
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

package edu.xtec.jclic.automation.arith;

import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;

/**
 * Utility class used by {@link edu.xtec.jclic.automation.arith.Arith} to
 * encapsulate the properties and methods related to the members of the
 * operations.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class Operator implements Domable {

  protected static final int MAX_VALUE = 100000000;
  protected static final int WZERO = 1, WONE = 2, WMINUSONE = 4;
  protected static final int NLIMITS = 26;
  protected static final int[] LIMITS = { 0, -9999, -1000, -999, -100, -99, -50, -25, -20, -10, -9, -5, -1, 0, 1, 5, 9,
      10, 20, 25, 50, 99, 100, 999, 1000, 9999 };
  protected static final int DEFAULT_LIMIT = 13;
  protected static final int LIM0 = 13;
  protected static final int LIM10 = 17;
  protected static final int LIMI25 = 7;
  protected static final int LIMS25 = 19;
  protected static final int NOLIM = 25;
  protected static final String[] LIM_CH = { "x", "-9999", "-1000", "-999", "-100", "-99", "-50", "-25", "-20", "-10",
      "-9", "-5", "-1", "0", "1", "5", "9", "10", "20", "25", "50", "99", "100", "999", "1000", "9999" };
  protected static final int NUMLST = 20;

  int limInf;
  int limSup;
  int numDec;
  boolean wZero, wOne, wMinusOne;
  int fromList;
  int[] lst = new int[NUMLST];
  protected boolean fromBlank;

  static final String ELEMENT_NAME = "operand", DECIMALS = "decimals", VALUES = "values", FROM = "from", TO = "to",
      INCLUDE = "include", ZERO = "zero", ONE = "one", MINUSONE = "minusOne";

  public Operator() {
    limInf = LIM0;
    limSup = LIM10;
    numDec = 0;
    wZero = false;
    wOne = false;
    wMinusOne = false;
    fromList = 0;
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    if (numDec > 0)
      e.setAttribute(DECIMALS, Integer.toString(numDec));
    if (fromList > 0) {
      e.setAttribute(VALUES, JDomUtility.intArrayToString(lst, fromList));
    } else {
      e.setAttribute(FROM, LIM_CH[limInf]);
      e.setAttribute(TO, LIM_CH[limSup]);
      if (wZero || wOne || wMinusOne) {
        org.jdom.Element ei = new org.jdom.Element(INCLUDE);
        ei.setAttribute(ZERO, JDomUtility.boolString(wZero));
        ei.setAttribute(ONE, JDomUtility.boolString(wOne));
        ei.setAttribute(MINUSONE, JDomUtility.boolString(wMinusOne));
        e.addContent(ei);
      }
    }
    return e;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);

    numDec = JDomUtility.getIntAttr(e, DECIMALS, numDec);
    String s = e.getAttributeValue(VALUES);
    if (s != null) {
      int[] v = JDomUtility.stringToIntArray(s);
      fromList = v.length;
      System.arraycopy(v, 0, lst, 0, fromList);
    } else {
      limInf = JDomUtility.getStrIndexAttr(e, FROM, LIM_CH, limInf);
      limSup = JDomUtility.getStrIndexAttr(e, TO, LIM_CH, limSup);
      org.jdom.Element child = e.getChild(INCLUDE);
      if (child != null) {
        wZero = JDomUtility.getBoolAttr(child, ZERO, wZero);
        wOne = JDomUtility.getBoolAttr(child, ONE, wOne);
        wMinusOne = JDomUtility.getBoolAttr(child, MINUSONE, wMinusOne);
      }
    }
  }

  public int setClic3Properties(byte[] ops, int p) {
    int v;
    int i, lb, hb;
    int arithVer = 0;

    if ((limInf = ops[p++] & 0x7F) == 0) {
      fromBlank = true;
      limInf = LIM0;
    }
    limSup = ((i = ops[p++] & 0x7F) == 0 ? LIM10 : i);
    numDec = ops[p++] & 0x3;
    v = ops[p++] & 0x7F;
    wZero = ((v & WZERO) != 0);
    wOne = ((v & WONE) != 0);
    wMinusOne = ((v & WMINUSONE) != 0);

    fromList = ops[p++] & 0x7F;

    for (i = 0; i < NUMLST; i++) {
      lb = ops[p++] & 0x7F;
      v = ops[p++] & 0x7F;
      hb = v & 0x3F;
      lst[i] = lb + hb * 128;
      if ((v & 0x40) != 0)
        lst[i] *= -1;
    }
    return p;
  }

  protected static int adjustLimVer(int l) {
    if (l >= LIMI25)
      l++;
    if (l >= LIMS25)
      l++;
    return l;
  }
}
