/*
 * File    : CheckBoxTreeObject.java
 * Created : 15-jul-2004 14:14
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

package edu.xtec.jclic.beans;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class CheckBoxTreeObject extends DefaultMutableTreeNode {

  protected String label;
  protected boolean selected;

  /** Creates a new instance of CheckBoxTreeObject */
  public CheckBoxTreeObject(String label, boolean selected) {
    super();
    this.label = label;
    this.selected = selected;
  }

  /** Creates a new instance of CheckBoxTreeObject */
  public CheckBoxTreeObject() {
    this("", false);
  }

  public void switchSelected() {
    selected = !selected;
  }

  @Override
  public String toString() {
    return label == null ? "" : label;
  }

  /**
   * Getter for property selecetd.
   *
   * @return Value of property selected.
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * Setter for property selected.
   *
   * @param selected New value of property selected.
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  /**
   * Getter for property label.
   *
   * @return Value of property label.
   */
  public java.lang.String getLabel() {
    return label;
  }

  /**
   * Setter for property label.
   *
   * @param label New value of property label.
   */
  public void setLabel(java.lang.String label) {
    this.label = label;
  }
}
