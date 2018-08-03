/*
 * File    : Editor.java
 * Created : 04-jun-2002 16:30
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
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.SystemColor;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * This generic class allows to modify the content and properties of an
 * associated {@link edu.xtec.jclic.edit.Editable} object. Editors provide
 * methods to register listeners that will be informed about changes occurred in
 * its associated data object. The class extends
 * {@link javax.swing.tree.DefaultMutableTreeNode} in order to make easy to
 * implement a tree of dependences between editor classes.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public abstract class Editor extends DefaultMutableTreeNode {

  protected HashSet<EditorListener> listeners;
  protected DefaultTreeModel treeModel;
  protected JTree currentTree;
  protected ListSelectionModel listSelectionModel;
  protected LModel listModel;
  private boolean modified;
  public static EditorAction moveUpAction, moveDownAction, copyAction, cutAction, pasteAction, deleteAction;
  protected static Editor clip;
  protected static boolean clipCutted;

  protected Editor(Object data) {
    super(data);
    listeners = new HashSet<EditorListener>();
  }

  public static Editor createEditor(String className, Object data, Editor parent) {
    Editor result = null;
    try {
      Class cl = Class.forName(className);
      Constructor[] constructors = cl.getDeclaredConstructors();
      Constructor cn = null;
      for (Constructor constr : constructors) {
        Class[] parameters = constr.getParameterTypes();
        if (parameters != null && parameters.length == 1 && parameters[0].isInstance(data)) {
          cn = constr;
          break;
        }
      }

      if (cn == null)
        throw new Exception();

      result = (Editor) cn.newInstance(new Object[] { data });
      if (result == null)
        throw new Exception();
      if (parent != null)
        parent.add(result);
      result.createChildren();
    } catch (Exception ex) {
      System.err.println("Unable to create " + className + " for " + data + "\n" + ex);
    }
    return result;
  }

  protected abstract void createChildren();

  public String getTitleKey() {
    return "edit_data";
  }

  public boolean isModified() {
    return modified;
  }

  public void setModified(boolean modified) {
    this.modified = modified;
    if (modified) {
      Editor ed = getEditorParent();
      if (ed != null)
        ed.setModified(true);
    } else {
      Enumeration en = children();
      while (en.hasMoreElements())
        ((Editor) en.nextElement()).setModified(false);
    }
  }

  public DefaultTreeModel getTreeModel() {
    if (isRoot()) {
      if (treeModel == null)
        setTreeModel(new DefaultTreeModel(this));
      return treeModel;
    }
    return (treeModel != null ? treeModel : getEditorParent().getTreeModel());
  }

  public void setTreeModel(DefaultTreeModel treeModel) {
    this.treeModel = treeModel;
  }

  public void setCurrentTree(JTree currentTree) {
    this.currentTree = currentTree;
  }

  public JTree getCurrentTree() {
    return currentTree != null ? currentTree : isRoot() ? null : getEditorParent().getCurrentTree();
  }

  public JTree createJTree() {
    setCurrentTree(new JTree(getTreeModel()));
    currentTree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
          boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof Editor) {
          Icon icon = ((Editor) value).getIcon(leaf, expanded);
          if (icon != null)
            setIcon(icon);
          if (clip == value && clipCutted) {
            setForeground(SystemColor.textInactiveText);
          }
        }
        return this;
      }
    });
    return currentTree;
  }

  public AbstractListModel<Object> getListModel() {
    if (listModel == null) {
      listModel = new LModel();
    }
    return listModel;
  }

  protected class LModel extends AbstractListModel<Object> {
    public Object getElementAt(int index) {
      return (index >= 0 && index < getChildCount()) ? getChildAt(index) : null;
    }

    public int getSize() {
      return getChildCount();
    }

    @Override
    public void fireIntervalAdded(Object src, int index0, int index1) {
      super.fireIntervalAdded(src, index0, index1);
    }

    @Override
    public void fireIntervalRemoved(Object src, int index0, int index1) {
      super.fireIntervalRemoved(src, index0, index1);
    }

    @Override
    public void fireContentsChanged(Object src, int index0, int index1) {
      super.fireContentsChanged(src, index0, index1);
    }
  }

  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    super.insert(newChild, childIndex);
    if (listModel != null)
      listModel.fireIntervalAdded(listModel, childIndex, childIndex);
  }

  @Override
  public void remove(int childIndex) {
    super.remove(childIndex);
    if (listModel != null)
      listModel.fireIntervalRemoved(listModel, childIndex, childIndex);
  }

  public void select() {
    JTree tree = getCurrentTree();
    if (tree != null)
      tree.getSelectionModel().setSelectionPath(new TreePath(getPath()));
    if (getListSelectionModel() != null) {
      Editor p = getEditorParent();
      if (p != null) {
        int index = p.getIndex(this);
        if (index >= 0)
          getListSelectionModel().setSelectionInterval(index, index);
      }
    }
  }

  public void reselect() {
    JTree tree = getCurrentTree();
    if (tree != null) {
      tree.getSelectionModel().clearSelection();
      tree.getSelectionModel().setSelectionPath(new TreePath(getPath()));
    }
    if (getListSelectionModel() != null) {
      Editor p = getEditorParent();
      if (p != null) {
        int index = p.getIndex(this);
        if (index >= 0) {
          getListSelectionModel().removeIndexInterval(index, index);
          getListSelectionModel().setSelectionInterval(index, index);
        }
      }
    }
  }

  public int getNearestIndex(Editor fromChild, boolean down) {
    int result = down ? getChildCount() : 0;
    if (fromChild != null) {
      int p = getIndex(fromChild);
      if (p >= 0)
        result = p + (down ? 1 : 0);
    }
    return result;
  }

  protected boolean canClone() {
    return false;
  }

  protected Editor getClone() throws Exception {
    return null;
  }

  protected boolean delete(boolean changeSelection) {
    boolean result = false;
    Editor p = getEditorParent();
    if (p != null) {
      p.setModified(true);
      if (clip == this)
        setClip(null, false);
      int index = p.getIndex(this);
      if (index == p.getChildCount() - 1)
        index--;
      getTreeModel().removeNodeFromParent(this);
      if (changeSelection) {
        Editor sel = index >= 0 ? (Editor) p.getChildAt(index) : p;
        if (sel != null) {
          sel.select();
        }
      }
      // Added 03-Feb-2011
      // Correct bug 172: when an editor loses its last element,
      // set their parent owner of the basic actions
      if (index < 0)
        p.setActionsOwner();

      result = true;
    }
    return result;
  }

  public boolean moveToIndex(int index, boolean updateSelection) {
    boolean result = false;
    DefaultTreeModel model = getTreeModel();
    Editor p = getEditorParent();
    if (p != null && model != null) {
      index = Math.min(Math.max(0, index), p.getChildCount());
      if (index != p.getIndex(this)) {
        p.setModified(true);
        model.removeNodeFromParent(this);
        model.insertNodeInto(this, p, index);
        result = true;
        if (updateSelection)
          select();
      }
    }
    return result;
  }

  public boolean moveUp(boolean updateSelection) {
    boolean result = false;
    Editor p = getEditorParent();
    if (p != null) {
      int index = parent.getIndex(this);
      result = moveToIndex(index - 1, updateSelection);
    }
    return result;
  }

  public boolean moveDown(boolean updateSelection) {
    boolean result = false;
    Editor p = getEditorParent();
    if (p != null) {
      int index = parent.getIndex(this);
      result = moveToIndex(index + 1, updateSelection);
    }
    return result;
  }

  public boolean copy() {
    boolean result = false;
    if (allowCopy && canClone()) {
      setClip(this, false);
      result = true;
    }
    return result;
  }

  protected static void setClip(Editor e, boolean cutted) {
    if (clip != null) {
      Editor c = clip;
      clip = null;
      clipCutted = false;
      c.getTreeModel().nodeChanged(c);
    }
    clip = e;
    clipCutted = cutted;
    if (clip != null)
      clip.getTreeModel().nodeChanged(clip);
  }

  public boolean cut() {
    boolean result = false;
    if (allowCut) {
      setClip(this, true);
      result = true;
    }
    return result;
  }

  public boolean canBeParentOf(Editor e) {
    return !(getClass().isInstance(e));
  }

  public boolean canBeSiblingOf(Editor e) {
    return true;
  }

  public boolean insertEditor(Editor e, boolean asChild, int index, boolean updateSelection) {
    boolean result = false;
    if (e != null) {
      if (asChild) {
        setModified(true);
        if (index < 0)
          index = getChildCount();
        getTreeModel().insertNodeInto(e, this, index);
        result = true;
        if (updateSelection)
          e.select();
      } else {
        Editor p = getEditorParent();
        if (p != null) {
          p.setModified(true);
          if (index < 0)
            index = p.getIndex(this);
          result = p.insertEditor(e, true, index, updateSelection);
        }
      }
    }
    return result;
  }

  protected boolean canPasteHere() {
    return allowPaste && clip != null && (!clipCutted || clip != this) && (clipCutted || clip.canClone())
        && (canBeParentOf(clip) || (canBeSiblingOf(clip) && getEditorParent() != null));
  }

  public boolean paste(boolean updateSelection) {
    boolean result = false;
    if (canPasteHere()) {
      Editor c = clip;
      if (clipCutted) {
        clip.delete(false);
      } else {
        try {
          c = clip.getClone();
        } catch (Exception ex) {
          System.err.println("Unable to clone " + clip + "\n" + ex);
          return false;
        }
      }

      result = insertEditor(c, canBeParentOf(c), -1, updateSelection);

      if (result) {
        setClip(clipCutted ? null : c, false);
        if (updateSelection) {
          c.select();
        }
      }
    }
    return result;
  }

  public Icon getIcon(boolean leaf, boolean expanded) {
    return null;
  }

  public Editor getEditorParent() {
    return (Editor) getParent();
  }

  public Editor getFirstParent(Class cl) {
    Editor result = getEditorParent();
    if (result != null && !cl.isInstance(result))
      result = result.getFirstParent(cl);
    return result;
  }

  public Editor getFirstChild(Class cl) {
    Editor result = null;
    Enumeration en = children();
    while (en.hasMoreElements()) {
      Editor ed = (Editor) en.nextElement();
      if (cl.isInstance(ed)) {
        result = ed;
        break;
      }
    }
    return result;
  }

  public Object getFirstObject(Class cl) {
    Object result = getUserObject();
    if ((result == null || !cl.isInstance(result)) && !isRoot())
      result = getEditorParent().getFirstObject(cl);
    return result;
  }

  public abstract Class getEditorPanelClass();

  public abstract EditorPanel createEditorPanel(Options options);

  @Override
  public String toString() {
    return "generic Editor component";
  }

  public Editor getChildByName(String name) {
    Editor result = null;
    if (name != null) {
      Enumeration en = children();
      while (result == null && en.hasMoreElements()) {
        Editor e = (Editor) en.nextElement();
        if (name.equals(e.toString()))
          result = e;
      }
    }
    return result;
  }

  public static void createBasicActions(Options options) {
    if (!basicActionsCreated) {
      moveUpAction = new EditorAction("editor_moveUp", "icons/up.gif", "editor_moveUp_tooltip", options) {
        protected void doAction(Editor e) {
          e.moveUp(true);
        }
      };
      moveDownAction = new EditorAction("editor_moveDown", "icons/down.gif", "editor_moveDown_tooltip", options) {
        protected void doAction(Editor e) {
          e.moveDown(true);
        }
      };
      copyAction = new EditorAction("COPY", "icons/copy.gif", "COPY", options) {
        protected void doAction(Editor e) {
          e.copy();
        }
      };
      cutAction = new EditorAction("CUT", "icons/cut.gif", "CUT", options) {
        protected void doAction(Editor e) {
          e.cut();
        }
      };
      pasteAction = new EditorAction("PASTE", "icons/paste.gif", "PASTE", options) {
        protected void doAction(Editor e) {
          e.paste(true);
        }
      };
      deleteAction = new EditorAction("DELETE", "icons/delete.gif", "DELETE", options) {
        protected void doAction(Editor e) {
          e.delete(true);
        }
      };
      basicActionsCreated = true;
    }
  }

  protected static boolean basicActionsCreated = false;
  protected boolean restrictMoveToSameType = true;
  protected boolean allowCopy = false, allowCut = false, allowPaste = false, allowDelete = false;

  public static void clearBasicActionsOwner() {
    if (basicActionsCreated) {
      moveUpAction.setActionOwner(null);
      moveDownAction.setActionOwner(null);
      copyAction.setActionOwner(null);
      cutAction.setActionOwner(null);
      pasteAction.setActionOwner(null);
      deleteAction.setActionOwner(null);
    }
  }

  public void setActionsOwner() {
    if (basicActionsCreated) {
      boolean eUp = false, eDown = false;
      Editor p = getEditorParent();
      if (p != null) {
        int i = p.getIndex(this);
        eUp = i > 0;
        eDown = i < p.getChildCount() - 1;
        if (restrictMoveToSameType) {
          eUp = eUp && getClass().isInstance(p.getChildAt(i - 1));
          eDown = eDown && getClass().isInstance(p.getChildAt(i + 1));
        }
      }
      moveUpAction.setActionOwner(eUp ? this : null);
      moveDownAction.setActionOwner(eDown ? this : null);

      copyAction.setActionOwner(allowCopy && canClone() ? this : null);
      cutAction.setActionOwner(allowCut ? this : null);
      pasteAction.setActionOwner(canPasteHere() ? this : null);
      deleteAction.setActionOwner(allowDelete ? this : null);
    }
  }

  public void clearActionsOwner() {
    clearBasicActionsOwner();
  }

  protected static Component findParentForDlg(AWTEvent ev) {
    if (ev != null && ev.getSource() instanceof Component)
      return (Component) ev.getSource();
    else
      return null;
  }

  public interface EditorListener {
    public void editorDataChanged(Editor e);
  }

  public void addEditorListener(EditorListener ls) {
    listeners.add(ls);
  }

  public void removeEditorListener(EditorListener ls) {
    listeners.remove(ls);
  }

  public void fireEditorDataChanged(EditorListener agent) {
    setModified(true);
    DefaultTreeModel tm = getTreeModel();
    if (tm != null)
      tm.nodeChanged(this);

    Iterator it = listeners.iterator();
    while (it.hasNext()) {
      EditorListener ls = (EditorListener) it.next();
      if (ls != agent)
        ls.editorDataChanged(this);
    }
  }

  public void collectData() {
    Iterator it = listeners.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      if (o instanceof EditorPanel) {
        ((EditorPanel) o).save();
        break;
      }
    }
    for (int i = 0; i < getChildCount(); i++) {
      TreeNode tn = getChildAt(i);
      if (tn instanceof Editor) {
        ((Editor) tn).collectData();
      }
    }
  }

  /**
   * Getter for property listSelectionModel.
   *
   * @return Value of property listSelectionModel.
   */
  public ListSelectionModel getListSelectionModel() {
    ListSelectionModel result = listSelectionModel;
    if (result == null && getEditorParent() != null)
      result = getEditorParent().getListSelectionModel();
    return result;
  }

  /**
   * Setter for property listSelectionModel.
   *
   * @param listSelectionModel New value of property listSelectionModel.
   */
  public void setListSelectionModel(ListSelectionModel listSelectionModel) {
    this.listSelectionModel = listSelectionModel;
  }

  public boolean editData(Component parent, Options options) {
    boolean result = false;
    EditorPanel panel = createEditorPanel(options);
    if (panel != null) {
      panel.attachEditor(this, false);
      result = options.getMessages().showInputDlg(parent, panel, getTitleKey());
      panel.attachEditor(null, result);
      if (result)
        setModified(true);
    }
    return result;
  }
}
