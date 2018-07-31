/*
 * File    : ExtendedJDialog.java
 * Created : 04-jun-2002 12:30
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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * This class is just a {@link javax.swing.JDialog} with a {@link java.awt.event.WindowAdapter} that
 * checks for focus lost. It also provides an utility method to place the dialog window centered
 * over another swing object.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ExtendedJDialog extends JDialog {

  private WindowAdapter wa = null;
  private boolean waConnected = false;

  /** Creates new JDialog */
  public ExtendedJDialog(Frame owner, String title, boolean modal) {
    super(owner, title, modal);
    createComponentEvents();
  }

  public ExtendedJDialog(Component owner, String title, boolean modal) {
    this(JOptionPane.getFrameForComponent(owner), title, modal);
  }

  public ExtendedJDialog(Dialog owner, String title, boolean modal) {
    super(owner, title, modal);
    createComponentEvents();
  }

  protected void createComponentEvents() {
    /*
     * Removed due to problems with MAC OSX
     *
    wa=new java.awt.event.WindowAdapter(){
        public void windowDeactivated(java.awt.event.WindowEvent e){
            requestFocus();
        }
    };
     */
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      if (!waConnected && wa != null) {
        addWindowListener(wa);
        waConnected = true;
      }
    } else {
      if (waConnected && wa != null) {
        removeWindowListener(wa);
        waConnected = false;
      }
    }
    super.setVisible(b);
  }

  public void centerOver(Component parent) {
    if (parent != null)
      setLocation((parent.getWidth() - getWidth()) / 2, (parent.getHeight() - getHeight()) / 2);
    setLocationRelativeTo(parent);
  }
}
