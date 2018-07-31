/*
 * File    : BasicEvaluator.java
 * Created : 07-jun-2001 13:50
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
import java.text.Collator;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class BasicEvaluator extends Evaluator {

  public static final String CHECK_CASE = "checkCase";
  public static final String CHECK_ACCENTS = "checkAccents";
  public static final String CHECK_PUNCTUATION = "checkPunctuation";
  public static final String CHECK_DOUBLE_SPACES = "checkDoubleSpaces";
  public static final String PUNCTUATION = ".,;:";

  protected boolean checkCase;
  protected boolean checkAccents;
  protected boolean checkPunctuation;
  protected boolean checkDoubleSpaces;

  int strength;

  /** Creates new BasicEvaluator */
  public BasicEvaluator(JClicProject project) {
    super(project);
    checkCase = false;
    checkAccents = true;
    checkPunctuation = true;
    checkDoubleSpaces = false;
    init();
  }

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();

    if (checkCase) e.setAttribute(CHECK_CASE, JDomUtility.boolString(checkCase));
    if (!checkAccents) e.setAttribute(CHECK_ACCENTS, JDomUtility.boolString(checkAccents));
    if (!checkPunctuation)
      e.setAttribute(CHECK_PUNCTUATION, JDomUtility.boolString(checkPunctuation));
    if (checkDoubleSpaces)
      e.setAttribute(CHECK_DOUBLE_SPACES, JDomUtility.boolString(checkDoubleSpaces));

    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    super.setProperties(e, aux);
    checkCase = JDomUtility.getBoolAttr(e, CHECK_CASE, false);
    checkAccents = JDomUtility.getBoolAttr(e, CHECK_ACCENTS, true);
    checkPunctuation = JDomUtility.getBoolAttr(e, CHECK_PUNCTUATION, true);
    checkDoubleSpaces = JDomUtility.getBoolAttr(e, CHECK_DOUBLE_SPACES, false);
  }

  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) {
    checkCase = c3a.avMaj;
    checkAccents = c3a.avAcc;
    checkPunctuation = c3a.avPunt;
    checkDoubleSpaces = c3a.avDblSpc;
  }

  @Override
  protected void init() {
    super.init();
    strength = checkAccents ? checkCase ? Collator.TERTIARY : Collator.SECONDARY : Collator.PRIMARY;
    collator.setStrength(strength);
  }

  public boolean checkText(String text, String match) {
    return collator.equals(getClearedText(text), getClearedText(match));
  }

  public byte[] evalText(String text, String[] match) {
    byte[] flags = new byte[text.length()];
    boolean result = checkText(text, match);
    for (int i = 0; i < flags.length; i++) {
      flags[i] = result ? FLAG_OK : FLAG_DEFAULT_ERROR;
    }
    return flags;
  }

  protected String getClearedText(String src) {
    return getClearedText(src, null);
  }

  protected String getClearedText(String src, boolean[] skipped) {

    if (skipped == null) skipped = new boolean[src.length()];

    for (int i = 0; i < src.length(); i++) skipped[i] = false;

    if (checkPunctuation && checkDoubleSpaces) return src;

    StringBuilder sb = new StringBuilder();
    boolean wasSpace = false;
    for (int i = 0; i < src.length(); i++) {
      char ch = src.charAt(i);
      if (PUNCTUATION.indexOf(ch) >= 0 && !checkPunctuation) {
        if (!wasSpace) sb.append(' ');
        else skipped[i] = true;
        wasSpace = true;
      } else if (ch == ' ') {
        if (checkDoubleSpaces || !wasSpace) sb.append(ch);
        else skipped[i] = true;
        wasSpace = true;
      } else {
        wasSpace = false;
        sb.append(ch);
      }
    }
    return sb.substring(0);
  }
}
