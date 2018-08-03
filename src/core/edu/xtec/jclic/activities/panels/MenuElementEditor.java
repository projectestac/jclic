/*
 * File    : MenuElementEditor.java
 * Created : 05-jun-2002 16:14
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

package edu.xtec.jclic.activities.panels;

import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.jclic.project.ProjectLibrary;
import edu.xtec.util.Options;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class MenuElementEditor extends Editor {

  protected static ImageIcon icon;
  protected ImageIcon meIcon;
  protected ProjectLibrary projectLibrary;

  public static EditorAction findProjectAction, findIconAction;
  protected static boolean actionsCreated = false;

  /** Creates new EditorMenuElement */
  public MenuElementEditor(MenuElement me) {
    super(me);
  }

  protected void createChildren() {
    ProjectLibrary pl = getProjectLibrary();
    if (pl != null) {
      meIcon = getMenuElement().getIcon(pl.mediaBag);
    }
  }

  public MenuElement getMenuElement() {
    return (MenuElement) getUserObject();
  }

  public MenuEditor getMenuEditorParent() {
    MenuEditor result = null;
    if (getParent() != null && getParent() instanceof MenuEditor)
      result = (MenuEditor) getParent();
    return result;
  }

  public Class getEditorPanelClass() {
    return MenuElementEditorPanel.class;
  }

  public ProjectLibrary getProjectLibrary() {
    if (projectLibrary != null)
      return projectLibrary;
    MenuEditor me = getMenuEditorParent();
    if (me != null)
      projectLibrary = me.getProjectLibrary();
    return projectLibrary;
  }

  @Override
  public String toString() {
    return getMenuElement().caption;
  }

  public EditorPanel createEditorPanel(Options options) {
    return new MenuElementEditorPanel(options);
  }

  public static Icon getIcon() {
    if (icon == null)
      icon = edu.xtec.util.ResourceManager.getImageIcon("icons/miniclic.png");
    return icon;
  }

  @Override
  public javax.swing.Icon getIcon(boolean leaf, boolean expanded) {
    return leaf ? getIcon() : null;
  }

  @Override
  protected boolean canClone() {
    return true;
  }

  @Override
  protected Editor getClone() throws Exception {
    MenuElement me = MenuElement.getMenuElement(getMenuElement().getJDomElement());
    MenuElementEditor mee = (MenuElementEditor) me.getEditor(null);
    mee.projectLibrary = getProjectLibrary();
    mee.meIcon = meIcon;
    return mee;
  }

  @Override
  public void setActionsOwner() {
    allowCut = allowDelete = allowCopy = allowPaste = true;
    super.setActionsOwner();
    if (actionsCreated) {
      findProjectAction.setActionOwner(this);
      findIconAction.setActionOwner(this);
      boolean hasParent = (getMenuEditorParent() != null);
      MenuEditor.newMenuAction.setActionOwner(hasParent ? this : null);
      MenuEditor.newMenuElementAction.setActionOwner(hasParent ? this : null);
    }
  }

  @Override
  public void clearActionsOwner() {
    super.clearActionsOwner();
    if (actionsCreated) {
      findProjectAction.setActionOwner(null);
      findIconAction.setActionOwner(null);
      MenuEditor.newMenuAction.setActionOwner(null);
      MenuEditor.newMenuElementAction.setActionOwner(null);
    }
  }

  protected void syncParentIndex() {
    MenuEditor me = getMenuEditorParent();
    if (me != null) {
      me.getMenu().menuElements.remove(getMenuElement());
      me.getMenu().menuElements.add(me.getIndex(this), getMenuElement());
    }
  }

  @Override
  public boolean moveUp(boolean updateSelection) {
    boolean result = super.moveUp(updateSelection);
    if (result)
      syncParentIndex();
    return result;
  }

  @Override
  public boolean moveDown(boolean updateSelection) {
    boolean result = super.moveDown(updateSelection);
    if (result)
      syncParentIndex();
    return result;
  }

  @Override
  public boolean delete(boolean updateSelection) {
    boolean result = false;
    getProjectLibrary();
    MenuEditor me = getMenuEditorParent();
    if (me != null && (result = super.delete(updateSelection)) == true)
      me.getMenu().menuElements.remove(getMenuElement());
    return result;
  }

  @Override
  public boolean canBeParentOf(Editor e) {
    return false;
  }

  @Override
  public boolean canBeSiblingOf(Editor e) {
    return (e instanceof MenuElementEditor) || (e instanceof MenuEditor);
  }

  public void findProject(Options options, Component dlgOwner) {
    MenuElement me = getMenuElement();
    ProjectLibrary pl = getProjectLibrary();
    if (pl != null) {
      int[] filters = { Utils.ALL_CLIC_FF, Utils.ALL_JCLIC_FF };
      String result = pl.getFileSystem().chooseFile(me.projectPath, false, filters, options, "edit_find_file", dlgOwner,
          false);
      if (result != null) {
        me.projectPath = result;
        fireEditorDataChanged(null);
      }
    }
  }

  public void findIcon(Options options, Component dlgOwner) {
    MenuElement me = getMenuElement();
    ProjectLibrary pl = getProjectLibrary();
    if (pl != null) {
      int[] filters = { Utils.ALL_IMAGES_FF, Utils.GIF_FF };
      String s = me.icon;
      if (s != null && s.startsWith("@"))
        s = null;
      String result = pl.getFileSystem().chooseFile(s == null ? me.projectPath : s, false, filters, options,
          "edit_find_image", dlgOwner, false);
      if (result != null) {
        ImageIcon ii = null;
        try {
          ii = new ImageIcon(pl.getFileSystem().getImageFile(result));
          if (ii.getIconWidth() > MenuElement.MAX_ICON_WIDTH || ii.getIconHeight() > MenuElement.MAX_ICON_HEIGHT) {
            options.getMessages().showAlert(dlgOwner, "menuElement_err_iconTooLarge");
            ii = null;
          }
        } catch (Exception ex) {
          System.err.println("Error reading image " + result + "\n" + ex);
        }
        if (ii != null) {
          me.icon = result;
          meIcon = me.getIcon(pl.mediaBag);
          fireEditorDataChanged(null);
        }
      }
    }
  }

  public static void createActions(Options options) {
    createBasicActions(options);
    if (!actionsCreated) {
      findProjectAction = new EditorAction("edit_find_file", "icons/file_open.gif", "edit_find_file", options) {
        protected void doAction(Editor e) {
          if (e instanceof MenuElementEditor)
            ((MenuElementEditor) e).findProject(this.options, getComponentSrc());
        }
      };

      findIconAction = new EditorAction("edit_find_image", "icons/file_open.gif", "edit_find_image", options) {
        protected void doAction(Editor e) {
          if (e instanceof MenuElementEditor)
            ((MenuElementEditor) e).findIcon(this.options, getComponentSrc());
        }
      };
      actionsCreated = true;
    }
  }
}
