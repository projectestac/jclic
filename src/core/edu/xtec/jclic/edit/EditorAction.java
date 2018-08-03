/*
 * File    : EditorAction.java
 * Created : 14-jun-2002 20:38
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

package edu.xtec.jclic.edit;

import edu.xtec.util.Options;
import edu.xtec.util.ResourceManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * This abstract class provides a base to create {@link javax.swing.Action}
 * objects related to {@link edu.xtec.jclic.edit.Editor}s. Derived classes must
 * implement only the <CODE>doAction
 * </CODE> method, executing the required operations on the supplied
 * <CODE>Editor</CODE>.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public abstract class EditorAction extends AbstractAction {

  ActionEvent ev;
  Editor editor;
  public Options options;

  public EditorAction(String nameKey, String iconKey, String toolTipKey, Options options) {
    super(options.getMsg(nameKey), ResourceManager.getImageIcon(iconKey));
    this.options = options;
    if (toolTipKey != null)
      putValue(AbstractAction.SHORT_DESCRIPTION, options.getMsg(toolTipKey));

    String s = options.getMessages().get(nameKey + "_keys");
    if (s != null && s.length() >= 2 && !s.startsWith(nameKey)) {
      putValue(Action.MNEMONIC_KEY, new Integer(s.charAt(0)));
      if (s.charAt(1) != '*') {
        char ch = s.charAt(1);
        int key = (int) ch;
        int keyMod = KeyEvent.CTRL_MASK;
        if (ch == '#' && s.length() > 2) {
          try {
            int sep = s.indexOf('#', 2);
            String k;
            if (sep > 0) {
              keyMod = Integer.parseInt(s.substring(sep + 1));
              k = s.substring(2, sep);
            } else {
              k = s.substring(2);
            }
            key = Integer.parseInt(k);
          } catch (Exception ex) {
            System.err.println("Error initializing action keys\nBad expression: " + s);
          }
        }
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, keyMod));
      }
    }
    setEnabled(false);
  }

  public final void actionPerformed(ActionEvent ev) {
    this.ev = ev;
    if (editor != null)
      doAction(editor);
  }

  protected abstract void doAction(Editor e);

  protected Component getComponentSrc() {
    Component result = null;
    if (ev != null && ev.getSource() instanceof Component)
      result = (Component) ev.getSource();
    return result;
  }

  protected JComponent getJComponentSrc() {
    JComponent result = null;
    if (ev != null && ev.getSource() instanceof JComponent)
      result = (JComponent) ev.getSource();
    return result;
  }

  protected EditorPanel getEditorPanelSrc() {
    Component cmp = getComponentSrc();
    while (cmp != null) {
      if (cmp instanceof EditorPanel)
        break;
      cmp = cmp.getParent();
    }
    return (EditorPanel) cmp;
  }

  public void setActionOwner(Editor e) {
    editor = e;
    setEnabled(editor != null);
  }
}
