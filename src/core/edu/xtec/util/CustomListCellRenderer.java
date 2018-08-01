/*
 * File    : CustomListCellRenderer.java
 * Created : 09-apr-2003 18:27
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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * This class is a {@link javax.swing.ListCellRenderer} that can use an array of
 * strings and another array of icons to display its content based on the value
 * of the <CODE>index</CODE> param passed to
 * <CODE>getListCellRendererComponent</CODE>.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class CustomListCellRenderer extends JLabel implements ListCellRenderer<Object> {

  protected String[] labels = null;
  protected Icon[] icons = null;

  public CustomListCellRenderer() {
    this(null, null);
  }

  /** Creates a new instance of CustomListCellRenderer */
  public CustomListCellRenderer(String[] labels, Icon[] icons) {
    setObjects(labels, icons);
    setOpaque(true);
    if (labels == null) {
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }
    setHorizontalAlignment(JLabel.LEFT);
  }

  public void setObjects(String[] labels, Icon[] icons) {
    this.labels = labels;
    this.icons = icons;
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

    String s = "";
    if (value instanceof String)
      s = (String) value;
    else if (labels != null && index >= 0 && index < labels.length)
      s = labels[index];

    Icon icon = null;
    if (value instanceof Icon)
      icon = (Icon) value;
    else if (icons != null && index >= 0 && index < icons.length)
      icon = icons[index];
    else if (icons != null && labels != null && index < 0 && value != null) {
      for (int i = 0; i < labels.length; i++) {
        if (value.equals(labels[i])) {
          if (i < icons.length)
            icon = icons[i];
          break;
        }
      }
    }

    setText(s);
    setIcon(icon);

    return this;
  }
}
