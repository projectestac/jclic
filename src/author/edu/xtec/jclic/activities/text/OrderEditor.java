/*
 * File    : OrderEditor.java
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

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class OrderEditor extends TextActivityBaseEditor {

  /** Creates a new instance of OrderEditor */
  public OrderEditor(Order act) {
    super(act);
  }

  protected static HashMap<Options, OrderCheckPanel> panelMap =
      new HashMap<Options, OrderCheckPanel>(1);

  @Override
  protected void editCheckOptions(Options options, Component parent) {
    Order ord = (Order) getTextActivity();
    if (ord != null) {
      OrderCheckPanel checkPanel = panelMap.get(options);
      if (checkPanel == null) {
        checkPanel = new OrderCheckPanel(options);
        panelMap.put(options, checkPanel);
      }
      checkPanel.setOptions(ord);
      if (options.getMessages().showInputDlg(parent, checkPanel, "edit_text_act_check_title")) {
        checkPanel.collectData(ord);
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
    Order ord = (Order) getActivity();
    int t = ord.type;
    boolean b = ord.amongParagraphs;
    boolean result = OrderTypePanel.editOrder(ord, options, parent);
    if (result) {
      if (ord.type != t && ord.tad.tmb.size() > 0) {
        if (options
                .getMessages()
                .showQuestionDlg(parent, "edit_text_act_warnChangeType", null, "yn")
            == Messages.YES) {
          ord.tad.clearAllTargets();
        } else {
          result = false;
          ord.type = t;
          ord.amongParagraphs = b;
        }
      }
    }

    if (result) {
      ord.tad.targetType =
          (ord.type == Order.ORDER_PARAGRAPHS)
              ? TextActivityDocument.TT_PARAGRAPH
              : TextActivityDocument.TT_WORD;
      setModified(true);
    }

    return result;
  }
}
