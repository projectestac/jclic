/*
 * File    : CompleteEditor.java
 * Created : 10-oct-2002 15:40
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

package edu.xtec.jclic.activities.text;

import edu.xtec.util.Options;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class CompleteEditor extends TextActivityBaseEditor {

  /** Creates a new instance of SimpleAssociationEditor */
  public CompleteEditor(Complete act) {
    super(act);
  }

  protected static Map<Options, CompleteCheckPanel> panelMap = new HashMap<Options, CompleteCheckPanel>(1);

  @Override
  protected void editCheckOptions(Options options, Component parent) {
    Complete cmp = (Complete) getTextActivity();
    if (cmp != null) {
      CompleteCheckPanel checkPanel = panelMap.get(options);
      if (checkPanel == null) {
        checkPanel = new CompleteCheckPanel(options);
        panelMap.put(options, checkPanel);
      }
      checkPanel.setOptions(cmp);
      if (options.getMessages().showInputDlg(parent, checkPanel, "edit_text_act_check_title")) {
        checkPanel.collectData(cmp);
        setModified(true);
      }
    }
  }
}
