/*
 * File    : ProjectLibraryEditor.java
 * Created : 05-jun-2002 17:12
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

package edu.xtec.jclic.project;

import edu.xtec.jclic.activities.panels.*;
import edu.xtec.jclic.edit.*;
import edu.xtec.util.Options;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ProjectLibraryEditor extends Editor {

  public static ImageIcon icon;

  /** Creates new ProjectLibraryEditor */
  public ProjectLibraryEditor(ProjectLibrary pl) {
    super(pl);
  }

  protected void createChildren() {
    ProjectLibrary pl = getProjectLibrary();
    Menu m = pl.getRootMenu();
    if (m != null) m.getEditor(this);
  }

  public Class getEditorPanelClass() {
    return ProjectLibraryEditorPanel.class;
  }

  public EditorPanel createEditorPanel(Options options) {
    return new ProjectLibraryEditorPanel(options);
  }

  public ProjectLibrary getProjectLibrary() {
    return (ProjectLibrary) getUserObject();
  }

  public static Icon getIcon() {
    if (icon == null) icon = edu.xtec.util.ResourceManager.getImageIcon("icons/database.gif");
    return icon;
  }

  @Override
  public javax.swing.Icon getIcon(boolean leaf, boolean expanded) {
    return getIcon();
  }

  @Override
  public String toString() {
    return getProjectLibrary().settings.title;
  }

  protected void saveMenus(TreeNode e) {
    if (e instanceof MenuEditor)
      getProjectLibrary().activityBag.addActivity(((MenuEditor) e).getMenu());
    for (int i = 0; i < e.getChildCount(); i++)
      if (e.getChildAt(i) instanceof MenuEditor) saveMenus(e.getChildAt(i));
  }

  @Override
  public void setActionsOwner() {
    // no actions
  }

  @Override
  public void clearActionsOwner() {
    super.clearActionsOwner();
    // no actions to clear
  }

  public static void createActions(Options options) {
    createBasicActions(options);
  }
}
