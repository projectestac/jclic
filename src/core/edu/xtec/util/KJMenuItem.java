/*
 * File    : KJMenuItem.java
 * Created : 17-jul-2002 18:13
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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class KJMenuItem extends JMenuItem {

  /** Creates a new instance of KJMenuItem */
  public KJMenuItem(Action a) {
    super(a);
  }

  @Override
  protected void configurePropertiesFromAction(Action a) {
    setText((a != null ? (String) a.getValue(Action.NAME) : null));
    setIcon((a != null ? (Icon) a.getValue(Action.SMALL_ICON) : null));
    setEnabled((a != null ? a.isEnabled() : true));
    if (a != null) {
      Object o = a.getValue(Action.ACCELERATOR_KEY);
      if (o != null && o instanceof KeyStroke)
        setAccelerator((KeyStroke) o);
      o = a.getValue(Action.MNEMONIC_KEY);
      if (o != null && o instanceof Integer)
        setMnemonic(((Integer) o).intValue());
    }
  }
}
