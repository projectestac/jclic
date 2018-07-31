/*
 * File    : MenuEditor.java
 * Created : 05-jun-2002 17:01
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

package edu.xtec.jclic.activities.panels;

import edu.xtec.jclic.*;
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.project.*;
import edu.xtec.util.Options;
import edu.xtec.util.StrUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class MenuEditor extends Editor {

  protected static ImageIcon icon;
  public static EditorAction newMenuElementAction, newMenuAction;
  public static boolean actionsCreated = false;
  protected ProjectLibrary projectLibrary;

  /** Creates new MenuEditor */
  public MenuEditor(Menu menu) {
    super(menu);
  }

  protected void createChildren() {
    Menu m = getMenu();
    ProjectLibrary pl = getProjectLibrary();
    if (pl != null && !m.menuElements.isEmpty()) {
      for (int i = 0; i < m.getMenuElementCount(); i++) {
        MenuElement me = m.getMenuElement(i);
        if (me.projectPath == null
            && me.sequence != null
            && !MenuElement.RETURN_TAG.equals(me.sequence)) {
          try {
            Activity act =
                Activity.getActivity(pl.activityBag.getElement(me.sequence).getData(), pl);
            if (act instanceof Menu) ((Menu) act).getEditor(this);
          } catch (Exception ex) {
            System.err.println("Error reading activity:\n" + ex);
          }
        } else me.getEditor(this);
      }
    }
  }

  public Class getEditorPanelClass() {
    return MenuEditorPanel.class;
  }

  public EditorPanel createEditorPanel(Options options) {
    return new MenuEditorPanel(options);
  }

  public Menu getMenu() {
    return (Menu) getUserObject();
  }

  public Options getOptions() {
    return getMenu().getProject().getBridge().getOptions();
  }

  protected void saveData() {
    ProjectLibrary pl = getProjectLibrary();
    if (pl != null) pl.activityBag.addActivity(getMenu());
  }

  public ProjectLibrary getProjectLibrary() {
    if (projectLibrary == null) {
      Editor p = getEditorParent();
      if (p != null) {
        if (p instanceof ProjectLibraryEditor)
          projectLibrary = ((ProjectLibraryEditor) p).getProjectLibrary();
        else if (p instanceof MenuEditor) projectLibrary = ((MenuEditor) p).getProjectLibrary();
      }
    }
    return projectLibrary;
  }

  @Override
  public String toString() {
    return getMenu().description;
  }

  @Override
  protected boolean canClone() {
    return true;
  }

  @Override
  protected Editor getClone() throws Exception {
    Menu menu = (Menu) getMenu().duplicate();
    menu.name = Long.toString(System.currentTimeMillis());
    MenuEditor me = (MenuEditor) menu.getEditor(null);
    me.projectLibrary = getProjectLibrary();
    me.createChildren();
    return me;
  }

  @Override
  public void setActionsOwner() {
    Editor e = getEditorParent();
    allowDelete = allowCut = (e != null && !(e instanceof ProjectLibraryEditor));
    allowDelete = allowCut && getChildCount() == 0;
    allowCopy = allowPaste = true;
    super.setActionsOwner();
    newMenuElementAction.setActionOwner(this);
    newMenuAction.setActionOwner(e != null ? this : null);
  }

  @Override
  public void clearActionsOwner() {
    super.clearActionsOwner();
    newMenuAction.setActionOwner(null);
    newMenuElementAction.setActionOwner(null);
  }

  public static Icon getIcon() {
    if (icon == null) icon = edu.xtec.util.ResourceManager.getImageIcon("icons/file_open.gif");
    return icon;
  }

  public void setDescription(String description) {
    Menu m = getMenu();
    m.description = StrUtils.secureString(description, getOptions().getMsg("UNNAMED"));

    ActiveBoxContent[] messages = m.getMessages();
    if (messages[Activity.MAIN] == null) {
      messages[Activity.MAIN] = new ActiveBoxContent();
      messages[Activity.MAIN].setBoxBase(new BoxBase());
    }
    messages[Activity.MAIN].setTextContent(m.description);

    MenuElement me = checkParentMenuElementRef(false, false, false, 0);
    if (me != null) me.caption = m.description;
  }

  protected MenuElement checkParentMenuElementRef(
      boolean remove, boolean create, boolean move, int index) {
    MenuElement result = null;
    Menu m = getMenu();
    if (getEditorParent() instanceof MenuEditor) {
      Menu pm = ((MenuEditor) getEditorParent()).getMenu();
      for (int i = 0; i < pm.getMenuElementCount(); i++) {
        MenuElement me = pm.getMenuElement(i);
        if (me.projectPath == null && me.sequence != null && me.sequence.equals(m.name)) {
          result = me;
          break;
        }
      }
      if (result != null && (remove || move)) {
        pm.menuElements.remove(result);
        if (move) pm.menuElements.add(index, result);
      } else if (result == null && create) {
        index = Math.max(0, Math.min(index, m.getMenuElementCount()));
        result = new MenuElement();
        result.sequence = m.name;
        result.caption = m.description;
        pm.menuElements.add(index, result);
      }
    }
    return result;
  }

  @Override
  public boolean moveUp(boolean updateSelection) {
    boolean result = super.moveUp(updateSelection);
    if (result) checkParentMenuElementRef(false, false, true, getParent().getIndex(this));
    return result;
  }

  @Override
  public boolean moveDown(boolean updateSelection) {
    boolean result = super.moveDown(updateSelection);
    if (result) checkParentMenuElementRef(false, false, true, getParent().getIndex(this));
    return result;
  }

  @Override
  public boolean insertEditor(Editor e, boolean asChild, int index, boolean updateSelection) {
    boolean result = false;
    if (!asChild) result = super.insertEditor(e, asChild, index, updateSelection);
    else {
      if (e instanceof MenuEditor) {
        ProjectLibrary pl = getProjectLibrary();
        if (pl != null) {
          Menu m = ((MenuEditor) e).getMenu();
          // find last non-Menu index
          int i;
          for (i = 0; i < getChildCount(); i++) if (!(getChildAt(i) instanceof MenuEditor)) break;

          if (index < 0) index = i;
          else index = Math.min(index, i);

          pl.activityBag.addActivity(m);
          pl.activitySequence.add(new ActivitySequenceElement(m.name, true));
          MenuEditor med = (MenuEditor) m.getEditor(this);
          med.checkParentMenuElementRef(false, true, false, index);
          result = super.insertEditor(med, true, index, updateSelection);
        }
      } else if (e instanceof MenuElementEditor) {
        MenuElement me = ((MenuElementEditor) e).getMenuElement();
        if (index < 0) index = getChildCount();
        else index = Math.min(index, getChildCount());

        getMenu().menuElements.add(index, me);
        result = super.insertEditor(me.getEditor(this), true, index, updateSelection);
      }
    }
    return result;
  }

  public boolean createNewMenu(int index, boolean prompt, Component dlgParent) {
    boolean result = false;
    ProjectLibrary pl = getProjectLibrary();

    if (pl != null) {
      JTree ct = getCurrentTree();
      TreePath savePath = null;

      if (ct != null) savePath = ct.getSelectionPath();

      Menu nm = new Menu(pl);
      nm.name = Long.toString(System.currentTimeMillis());

      MenuEditor med = (MenuEditor) nm.getEditor(null);
      med.setDescription(getOptions().getMsg("menu_newMenuName"));

      if (prompt) {
        result =
            med.createEditorPanel(getOptions())
                .showDialog(med, "menu_newMenuElement_caption", dlgParent, true);
      } else {
        result = true;
      }

      if (result) {
        result = insertEditor(med, true, index, true);
      } else if (savePath != null && ct != null) {
        ct.clearSelection();
        ct.setSelectionPath(savePath);
      }
    }
    return result;
  }

  public boolean createNewMenuElement(int index, boolean prompt, Component dlgParent) {

    MenuElement me = new MenuElement();
    boolean result;
    TreePath savePath = null;
    JTree ct = getCurrentTree();
    MenuElementEditor mee;

    me.caption = getOptions().getMsg("menu_newMenuElementName");

    if (ct != null) savePath = ct.getSelectionPath();

    mee = (MenuElementEditor) me.getEditor(null);
    mee.projectLibrary = getProjectLibrary();
    mee.createChildren();

    if (prompt) {
      result =
          mee.createEditorPanel(getOptions())
              .showDialog(mee, "menu_newMenuElement_caption", dlgParent, true);
    } else {
      result = true;
    }

    if (index < 0) index = getChildCount();
    else index = Math.min(index, getChildCount());

    if (result) {
      result = insertEditor(mee, true, index, true);
    } else if (savePath != null && ct != null) {
      ct.clearSelection();
      ct.setSelectionPath(savePath);
    }
    return result;
  }

  @Override
  public boolean delete(boolean updateSelection) {
    boolean result = false;
    ProjectLibrary pl = getProjectLibrary();
    String name = getMenu().name;
    Editor saveParent = getEditorParent();
    if (name != null && (result = super.delete(updateSelection)) == true) {
      parent = saveParent;
      if (pl != null) {
        ActivitySequenceElement ase = pl.activitySequence.getElementByTag(name, false);
        if (ase != null) pl.activitySequence.remove(ase);
        pl.activityBag.removeElementByName(name);
      }

      checkParentMenuElementRef(true, false, false, 0);

      parent = null;
    }
    return result;
  }

  @Override
  public boolean canBeParentOf(Editor e) {
    return (e instanceof MenuElementEditor) || (e instanceof MenuEditor);
  }

  @Override
  public boolean canBeSiblingOf(Editor e) {
    if (getEditorParent() instanceof MenuEditor) return canBeParentOf(e);
    else return (e instanceof MenuElementEditor);
  }

  public static void createActions(Options options) {
    createBasicActions(options);
    if (!actionsCreated) {

      newMenuElementAction =
          new EditorAction(
              "menu_newMenuElement_caption",
              "icons/new_miniclic.png",
              "menu_newMenuElement_tooltip",
              options) {
            protected void doAction(Editor e) {
              Editor ch = null;
              if (e instanceof MenuElementEditor) {
                ch = e;
                e = e.getEditorParent();
              }

              if (e instanceof MenuEditor)
                ((MenuEditor) e)
                    .createNewMenuElement(e.getNearestIndex(ch, true), true, getComponentSrc());
            }
          };

      newMenuAction =
          new EditorAction(
              "menu_newMenu_caption", "icons/new_folder.gif", "menu_newMenu_tooltip", options) {
            protected void doAction(Editor e) {
              if (e instanceof MenuElementEditor) e = e.getEditorParent();
              if (e instanceof MenuEditor)
                ((MenuEditor) e).createNewMenu(-1, true, getComponentSrc());
            }
          };

      actionsCreated = true;
    }
  }
}
