/*
 * File    : ComplexEvaluator.java
 * Created : 11-jun-2001 14:56
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

package edu.xtec.jclic.activities.text;

import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class ComplexEvaluator extends BasicEvaluator {

  public static final String DETAIL = "detail";

  public static final String CHECK_STEPS = "checkSteps";
  public static final int DEFAULT_CHECK_STEPS = 3;

  public static final String CHECK_SCOPE = "checkScope";
  public static final int DEFAULT_CHECK_SCOPE = 6;

  protected boolean detail;
  protected int checkSteps;
  protected int checkScope;

  /** Creates new ComplexEvaluator */
  public ComplexEvaluator(JClicProject project) {
    super(project);
    detail = true;
    checkSteps = DEFAULT_CHECK_STEPS;
    checkScope = DEFAULT_CHECK_SCOPE;
  }

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();
    if (!detail)
      e.setAttribute(DETAIL, JDomUtility.boolString(detail));
    if (checkSteps != DEFAULT_CHECK_STEPS)
      e.setAttribute(CHECK_STEPS, Integer.toString(checkSteps));
    if (checkScope != DEFAULT_CHECK_SCOPE)
      e.setAttribute(CHECK_SCOPE, Integer.toString(checkScope));

    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    super.setProperties(e, aux);
    detail = JDomUtility.getBoolAttr(e, DETAIL, true);
    checkSteps = JDomUtility.getIntAttr(e, CHECK_STEPS, DEFAULT_CHECK_STEPS);
    checkScope = JDomUtility.getIntAttr(e, CHECK_SCOPE, DEFAULT_CHECK_SCOPE);
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) {
    super.setProperties(c3a);
    detail = c3a.avLletra;
    checkSteps = c3a.avScope;
    checkScope = c3a.avMaxScope;
  }

  @Override
  protected void init() {
    super.init();
  }

  @Override
  public byte[] evalText(String text, String[] match) {
    if (!detail)
      return super.evalText(text, match);
    int[] numChecks = new int[match.length];
    int maxCheck = -1, maxCheckIndex = -1;
    byte[][] flags = new byte[match.length][];
    boolean[] skipped = new boolean[text.length()];
    String sText = getClearedText(text, skipped);
    for (int i = 0; i < match.length; i++) {
      String sMatch = getClearedText(match[i]);
      flags[i] = new byte[sText.length()];
      boolean ok = compareSegment(sText, sText.length(), match[i], match[i].length(), flags[i], false);
      numChecks[i] = countFlagsOk(flags[i]);
      if (ok) {
        maxCheckIndex = i;
        maxCheck = numChecks[i];
      }
    }

    if (maxCheckIndex == -1) {
      for (int i = 0; i < match.length; i++) {
        if (numChecks[i] > maxCheck) {
          maxCheck = numChecks[i];
          maxCheckIndex = i;
        }
      }
    }

    byte[] returnFlags = new byte[text.length()];
    for (int i = 0, k = 0; i < text.length(); i++) {
      if (skipped[i])
        returnFlags[i] = FLAG_OK;
      else
        returnFlags[i] = flags[maxCheckIndex][k++];
    }

    return returnFlags;
  }

  private int countFlagsOk(byte[] flags) {
    int r = 0;
    for (int i = 0; i < flags.length; i++)
      if (flags[i] == Evaluator.FLAG_OK)
        r++;
    return r;
  }

  private boolean compareSegment(String src, int ls, String ok, int lok, byte[] attr, boolean iterate) {
    int coinci;
    int is, iok, lastIs;
    boolean lastiok = true;
    boolean result = true;
    char chs, chok;

    coinci = 0;
    if (ls == 0 || lok == 0 || src == null || ok == null)
      return false;
    // chs=chok= iterate ? 0 : ' ';
    lastIs = 0;
    for (iok = 0, is = 0; is < ls; is++, iok++) {
      chs = src.charAt(is);
      lastIs = is;
      if (iok >= 0 && iok < lok)
        chok = ok.charAt(iok);
      else
        chok = 0;
      if (collator.equals(new String(new char[] { chs }), new String(new char[] { chok }))) {
        coinci++;
        attr[is] = Evaluator.FLAG_OK;
        lastiok = true;
      } else {
        result = false;
        attr[is] = Evaluator.FLAG_DEFAULT_ERROR;
        if (!iterate && lastiok && chok != 0 && checkSteps > 0 && checkScope > 0) {
          int lbloc = 2 * checkSteps + 1;
          int[] itcoinc = new int[lbloc];
          int i = 0, j, is2, iok2, ls2, lok2, jmax;
          for (j = 0; j < lbloc; j++) {
            itcoinc[j] = 0;
            i = iok + ((j + 1) / 2) * ((j & 1) != 0 ? 1 : -1);
            if (i >= lok)
              continue;
            is2 = (i < 0 ? is - i : is);
            if (is2 >= ls)
              continue;
            ls2 = (ls2 = ls - is2) > checkScope ? checkScope : ls2;
            iok2 = (i < 0 ? 0 : i);
            lok2 = (lok2 = lok - iok2) > checkScope ? checkScope : lok2;
            byte[] flags2 = new byte[src.length() - is2];
            boolean result2 = compareSegment(src.substring(is2), ls2, ok.substring(iok2), lok2, flags2, true);
            itcoinc[j] = countFlagsOk(flags2);
            if (result2)
              break;
          }
          if (j == lbloc) {
            jmax = checkSteps;
            for (j = 0; j < lbloc; j++)
              if (itcoinc[j] > itcoinc[jmax])
                jmax = j;
            i = iok + ((jmax + 1) / 2) * ((jmax & 1) != 0 ? 1 : -1);
          } else if (itcoinc[j] > 0)
            coinci++;

          iok = i;
          lastiok = false;
        }
      }
    }
    if (iok != lok) {
      result = false;
      attr[lastIs] = Evaluator.FLAG_DEFAULT_ERROR;
    }

    return result;
  }
}
