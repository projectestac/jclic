/*
 * File    : NullableObject.java
 * Created : 04-nov-2002 15:30
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

package edu.xtec.jclic.beans;

import edu.xtec.util.Options;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class NullableObject extends JPanel implements ActionListener {

  public static final String PROP_CUSTOM_OBJECT = "customObject";

  protected Object object;
  protected JCheckBox check;
  protected boolean nullValue;
  protected AbstractButton button;
  protected Options options;
  protected boolean modified;

  public static final int PANEL_HEIGHT = 21, CHECK_WIDTH = 14, BUTTON_WIDTH = 31;
  public static final Dimension btDim = new Dimension(BUTTON_WIDTH, PANEL_HEIGHT);
  public static final Dimension checkDim = new Dimension(CHECK_WIDTH, PANEL_HEIGHT);
  public static final Dimension panelDim = new Dimension(BUTTON_WIDTH + CHECK_WIDTH, PANEL_HEIGHT);

  /** Creates new NullableObject */
  public NullableObject() {

    super(new java.awt.BorderLayout());
    setOpaque(false);
    nullValue = true;

    check = new JCheckBox();
    check.setPreferredSize(checkDim);
    check.setMinimumSize(checkDim);
    check.setFocusPainted(false);
    check.setSelected(false);
    add(check, java.awt.BorderLayout.WEST);

    button = buildButton();
    button.setPreferredSize(btDim);
    button.setMinimumSize(btDim);
    button.setFocusPainted(false);
    add(button, java.awt.BorderLayout.CENTER);

    check.addActionListener(this);
    button.addActionListener(this);
  }

  protected String getObjectType() {
    return PROP_CUSTOM_OBJECT;
  }

  protected Object createObject() {
    return new Object();
  }

  protected AbstractButton buildButton() {
    return new JButton();
  }

  public boolean isModified() {
    return modified;
  }

  public void setModified(boolean modified) {
    this.modified = modified;
  }

  protected Object editObject(Object o) {
    return o;
  };

  @Override
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    if (button != null)
      button.setToolTipText(text);
    if (check != null)
      check.setToolTipText(text);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (button != null)
      button.setEnabled(enabled);
    if (check != null)
      check.setEnabled(enabled);
  }

  @Override
  public void setName(String name) {
    if (check != null)
      check.setText(name);
  }

  public void setText(String text) {
    if (check != null)
      check.setText(text);
  }

  public void setOptions(Options options) {
    this.options = options;
  }

  public Options getOptions() {
    return options;
  }

  public Object getObject() {
    return nullValue ? null : object;
  }

  public void clear() {
    setObject((object = null));
  }

  public void setObject(Object value) {
    nullValue = (value == null);
    if (!nullValue)
      object = value;
    check.setSelected(!nullValue);
    button.repaint();
  }

  public void changeObject(Object o) {
    Object oldValue = getObject();
    setObject(o);
    Object newValue = getObject();
    if ((oldValue == null && newValue != null) || (oldValue != null && !oldValue.equals(newValue)))
      firePropertyChange(getObjectType(), oldValue, getObject());
  }

  public void setIcon(Icon icon) {
    button.setIcon(icon);
  }

  public Icon getIcon() {
    return button.getIcon();
  }

  public void actionPerformed(ActionEvent ev) {
    if (ev != null) {
      if (button.equals(ev.getSource())) {
        modified = true;
        Object o = editObject(object);
        if (o != null)
          changeObject(o);
      } else if (check.equals(ev.getSource())) {
        changeObject(check.isSelected() ? (object == null ? createObject() : object) : null);
        modified = true;
      }
    }
  }
}
