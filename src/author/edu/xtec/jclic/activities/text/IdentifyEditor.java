/*
 * File    : IdentifyEditor.java
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

import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class IdentifyEditor extends TextActivityBaseEditor {

  /** Creates a new instance of IdentifyEditor */
  public IdentifyEditor(Identify act) {
    super(act);
  }

  protected static Map<Options, CheckOptionsPanel> panelMap = new HashMap<Options, CheckOptionsPanel>(1);

  @Override
  protected void editCheckOptions(Options options, Component parent) {
    Identify ident = (Identify) getTextActivity();
    if (ident != null) {
      CheckOptionsPanel checkPanel = panelMap.get(options);
      if (checkPanel == null) {
        checkPanel = new CheckOptionsPanel(options);
        panelMap.put(options, checkPanel);
      }
      checkPanel.setOptions(ident);
      if (options.getMessages().showInputDlg(parent, checkPanel, "edit_text_act_check_title")) {
        checkPanel.collectData(ident);
        setModified(true);
      }
    }
  }

  @Override
  protected boolean hasType() {
    return true;
  }

  @Override
  protected boolean editType(Options options, Component parent) {
    Identify ident = (Identify) getActivity();
    int t = ident.type;
    boolean result = IdentifyTypePanel.editIdentify(ident, options, parent);
    if (result) {
      if (ident.type != t && ident.tad.tmb.size() > 0) {
        if (options.getMessages().showQuestionDlg(parent, "edit_text_act_warnChangeType", null, "yn") == Messages.YES) {
          ident.tad.clearAllTargets();
        } else {
          result = false;
          ident.type = t;
        }
      }
    }

    if (result) {
      ident.tad.targetType = (ident.type == Identify.IDENTIFY_WORDS) ? TextActivityDocument.TT_WORD
          : TextActivityDocument.TT_CHAR;
      setModified(true);
    }

    return result;
  }
}
