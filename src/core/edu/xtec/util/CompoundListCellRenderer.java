/*
 * File    : CompoundListCellRenderer.java
 * Created : 13-aug-2002 12:46
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

package edu.xtec.util;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class CompoundListCellRenderer extends JLabel implements ListCellRenderer<Object> {

  /** Creates a new instance of CompoundListCellRenderer */
  public CompoundListCellRenderer() {
    setOpaque(true);
    setHorizontalAlignment(CENTER);
    setVerticalAlignment(CENTER);
  }

  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }

    String text = "???";
    ImageIcon icon = null;

    if (value instanceof CompoundObject) {
      CompoundObject co = (CompoundObject) value;
      text = co.text;
      icon = co.getIcon();
    } else if (value != null)
      text = value.toString();

    setText(icon == null ? text : "");
    setIcon(icon);
    return this;
  }
}
