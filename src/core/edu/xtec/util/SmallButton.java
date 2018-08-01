/*
 * File    : SmallButton.java
 * Created : 09-jun-2002 20:19
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

import java.awt.event.*;
import javax.swing.Action;
import javax.swing.JButton;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class SmallButton extends JButton implements MouseListener {

  SmallButtonBorder smb;

  public SmallButton(Action act) {
    super(act);
    setText(null);

    setToolTipText((String) act.getValue(javax.swing.Action.SHORT_DESCRIPTION));

    addMouseListener(this);
    setRequestFocusEnabled(false);
    smb = new SmallButtonBorder(getBorder(), false);
    setBorder(smb);
    setOpaque(false);
  }

  @Override
  public float getAlignmentY() {
    return 0.5f;
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
    if (isEnabled())
      smb.borderOn = true;
    setOpaque(true);
    repaint();
  }

  public void mouseExited(MouseEvent e) {
    smb.borderOn = false;
    setOpaque(false);
    repaint();
  }
}
