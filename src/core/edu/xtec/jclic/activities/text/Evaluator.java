/*
 * File    : Evaluator.java
 * Created : 07-jun-2001 13:36
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
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.text.Collator;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public abstract class Evaluator extends java.lang.Object implements Domable {

  public static final byte FLAG_OK = 0;
  public static final byte FLAG_DEFAULT_ERROR = 1;

  Collator collator;
  // Activity act;
  protected boolean initiated = false;

  /** Creates new Evaluator */
  public Evaluator(JClicProject project) {
    // act=activity;
    if (project.getBridge() != null)
      collator = project.getBridge().getOptions().getMessages().getCollator();
    else collator = Collator.getInstance();
  }

  public static final String ELEMENT_NAME = "evaluator";
  public static final String BASE_CLASS = "edu.xtec.jclic.activities.text.", BASE_CLASS_TAG = "@";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    String s = getClass().getName();
    if (s.startsWith(BASE_CLASS)) s = BASE_CLASS_TAG + s.substring(BASE_CLASS.length());
    e.setAttribute(JDomUtility.CLASS, s);
    return e;
  }

  public static Evaluator getEvaluator(org.jdom.Element e, JClicProject project) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    String className = JDomUtility.getClassName(e);
    if (className.startsWith(BASE_CLASS_TAG)) className = BASE_CLASS + className.substring(1);

    String s;
    Evaluator ev;
    Class<?> evaluatorClass;
    Class[] cparams = {JClicProject.class};
    Object[] initArgs = {project};
    evaluatorClass = Class.forName(className);
    java.lang.reflect.Constructor con = evaluatorClass.getConstructor(cparams);
    ev = (Evaluator) con.newInstance(initArgs);
    ev.setProperties(e, null);
    ev.init();
    return ev;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {}

  protected void init() {
    initiated = true;
  }

  public boolean checkText(String text, String[] match) {
    for (String m : match) {
      if (checkText(text, m)) return true;
    }
    return false;
  }

  public abstract boolean checkText(String text, String match);

  public abstract byte[] evalText(String text, String[] match);

  public byte[] evalText(String text, String match) {
    return evalText(text, new String[] {match});
  }

  public static boolean isOk(byte[] flags) {
    for (int fl : flags) if (fl != FLAG_OK) return false;
    return true;
  }
}
