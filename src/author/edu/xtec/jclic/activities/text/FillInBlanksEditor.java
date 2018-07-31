/*
 * File    : FillInBlanksEditor.java
 * Created : 10-oct-2002 15:40
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

import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class FillInBlanksEditor extends TextActivityBaseEditor {

  /** Creates a new instance of FillInBlanksEditor */
  public FillInBlanksEditor(FillInBlanks act) {
    super(act);
  }

  @Override
  protected TargetMarker createNewTarget(int ifrom, int ito, Options options, Component parent) {
    TargetMarker tm = super.createNewTarget(ifrom, ito, options, parent);
    if (tm != null) {
      tm.target = new TextTarget(getTextActivity().tad, from, to);
      if (options != null && parent != null && !editTarget(from, options, parent)) {
        deleteTarget(tm, null, null);
      }
    }
    return tm;
  }

  @Override
  protected boolean deleteTarget(TargetMarker tm, Options options, Component parent) {
    int d = 0;
    if (options != null)
      d =
          options
              .getMessages()
              .showQuestionDlg(parent, "edit_text_act_warnDeleteTarget", null, "yn");
    return d == Messages.YES ? super.deleteTarget(tm, options, parent) : false;
  }

  @Override
  protected boolean canEditTarget() {
    return true;
  }

  protected static Map<Options, FillInBlanksCheckPanel> panelMap =
      new HashMap<Options, FillInBlanksCheckPanel>(1);

  @Override
  protected void editCheckOptions(Options options, Component parent) {
    FillInBlanks fib = (FillInBlanks) getTextActivity();
    if (fib != null) {
      FillInBlanksCheckPanel checkPanel = panelMap.get(options);
      if (checkPanel == null) {
        checkPanel = new FillInBlanksCheckPanel(options);
        panelMap.put(options, checkPanel);
      }
      checkPanel.setOptions(fib);
      if (options.getMessages().showInputDlg(parent, checkPanel, "edit_text_act_check_title")) {
        checkPanel.collectData(fib);
        setModified(true);
      }
    }
  }
}
