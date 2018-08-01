/*
 * File    : EditorPanel.java
 * Created : 05-jun-2002 10:30
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

package edu.xtec.jclic.edit;

import edu.xtec.jclic.edit.Editor.EditorListener;
import edu.xtec.util.CtrlPanel;
import edu.xtec.util.Options;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * This class is a {@link edu.xtec.util.CtrlPanel} specialized to deal with
 * {@link edu.xtec.jclic.edit.Editor} objects. Every implementation of editor
 * panel will have specific methods to deal with a specific type of
 * <CODE>Editor</CODE> objects (wich, in turn, will be designed for a specific
 * type of data objects).
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public abstract class EditorPanel extends CtrlPanel implements FocusListener, EditorListener {

  protected Editor editor;
  protected Options options;

  public static final Color titleBgColor = Color.orange;
  public static final Color titleForeColor = Color.blue;
  public static final Color panelBgColor = new Color(255, 255, 204);
  public static final Border titleBorder = new EmptyBorder(new Insets(6, 4, 6, 4));
  public static final Border panelBorder = new EmptyBorder(0, 0, 0, 0);

  public JLabel northComponent;
  public Component southComponent;

  /** Creates new EditPanel */
  public EditorPanel(Options options) {
    super();
    this.options = options;
  }

  public Options getOptions() {
    return options;
  }

  protected void postInit(int preferredLabelWidth, boolean withTitleBar, boolean withToolBar) {
    setBorder(panelBorder);
    if (withTitleBar || withToolBar) {
      JPanel northPanel = new JPanel(new BorderLayout());
      if (withTitleBar) {
        northComponent = createTitleLabel(preferredLabelWidth);
        northPanel.add(northComponent, BorderLayout.NORTH);
      }
      if (withToolBar) {
        southComponent = createToolBar();
        northPanel.add(southComponent, BorderLayout.SOUTH);
      }
      add(northPanel, BorderLayout.NORTH);
    }
  }

  public abstract boolean checkIfEditorValid(Editor e);

  public final void clear() {
    setModified(false);
  }

  public final void fill() {
    setInitializing(true);
    fillData();
    setInitializing(false);
    setModified(false);
    setEnabled(editor != null);
  }

  protected abstract void fillData();

  public final void save() {
    if (editor != null && isModified()) {
      saveData();
      editor.fireEditorDataChanged(this);
      setModified(false);
    }
  }

  protected abstract void saveData();

  public void attachEditor(Editor e, boolean saveChanges) {
    if (e == editor) {
      if (editor != null && saveChanges && isModified())
        editor.setModified(true);
      fill();
    } else {
      if (editor != null)
        removeEditor(saveChanges);

      if (checkIfEditorValid(e)) {
        setEditor(e);
        editor.addEditorListener(this);
        fill();
      }
    }
  }

  public Editor getEditor() {
    return editor;
  }

  protected synchronized void setEditor(Editor e) {
    if (editor != null)
      editor.clearActionsOwner();
    editor = e;
    if (e != null)
      e.setActionsOwner();
  }

  public void removeEditor(boolean saveChanges) {
    if (editor != null) {
      editor.removeEditorListener(this);
      if (saveChanges)
        save();
      setEditor(null);
      fill();
    }
  }

  public void editorDataChanged(Editor e) {
    if (e != null && e.equals(getEditor()))
      fillData();
  }

  protected Icon getIcon() {
    return null;
  }

  protected String getTitle() {
    return "";
  }

  protected JLabel createTitleLabel(int preferredWidth) {
    JLabel result = new JLabel(getTitle());
    result.setHorizontalAlignment(SwingConstants.CENTER);
    if (getIcon() != null) {
      result.setIcon(getIcon());
      result.setIconTextGap(10);
    }
    result.setBackground(titleBgColor);
    result.setForeground(titleForeColor);
    result.setOpaque(true);
    result.setBorder(titleBorder);
    result.validate();
    Dimension d = result.getPreferredSize();
    result.setPreferredSize(new Dimension(Math.max(d.width, preferredWidth), d.height));
    result.setMinimumSize(result.getPreferredSize());
    return result;
  }

  protected void addActionsTo(Container cnt) {
  }

  protected JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    addActionsTo(toolBar);
    return toolBar;
  }

  protected String checkEmptyString(String src, boolean trim, String msgCodeDefault) {
    String result = src;
    if (result != null && trim)
      result = result.trim();
    if (result == null || result.length() == 0)
      result = options.getMsg(msgCodeDefault);
    return result;
  }

  public boolean showDialog(Editor e, String titleKey, Component cmp, boolean removeActionButtons) {

    if (removeActionButtons && southComponent != null)
      remove(southComponent);

    attachEditor(e, false);

    EditDialog dlg = new EditDialog(options, titleKey, JOptionPane.getFrameForComponent(cmp));
    dlg.getContentPane().add(this, BorderLayout.CENTER);

    if (northComponent != null) {
      Dimension d = dlg.getContentPane().getPreferredSize();
      d.width = northComponent.getPreferredSize().width;
      setPreferredSize(d);
    }

    dlg.showDialog();
    removeEditor(dlg.result);
    return dlg.result;
  }

  public void focusGained(FocusEvent focusEvent) {
    if (getEditor() != null)
      getEditor().setActionsOwner();
    else
      Editor.clearBasicActionsOwner();
  }

  public void focusLost(FocusEvent focusEvent) {
    // do nothing!
  }
}
