/*
 * File    : ActivitySequenceEditor.java
 * Created : 08-apr-2003 12:51
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

package edu.xtec.jclic.bags;

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.edit.EditorAction;
import edu.xtec.jclic.edit.EditorPanel;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.util.ListComboModel;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.StrUtils;
import java.awt.Component;
import java.util.Enumeration;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.tree.MutableTreeNode;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ActivitySequenceEditor extends Editor {

  public static ImageIcon icon;
  protected DefaultListModel<Object> tagList;
  private boolean initializing;
  public static boolean actionsCreated;
  public static EditorAction newActivitySequenceElementAction;

  /** Creates a new instance of ActivitySequenceEditor */
  public ActivitySequenceEditor(ActivitySequence ac) {
    super(ac);
  }

  protected void createChildren() {
    initializing = true;
    ActivitySequence as = getActivitySequence();
    int n = as.getSize();
    for (int i = 0; i < n; i++)
      as.getElement(i, false).getEditor(this);
    initializing = false;
  }

  public EditorPanel createEditorPanel(Options options) {
    return new ActivitySequenceEditorPanel(options);
  }

  public Class getEditorPanelClass() {
    return ActivitySequenceEditorPanel.class;
  }

  @Override
  public String getTitleKey() {
    return "edit_sequences";
  }

  public ActivitySequence getActivitySequence() {
    return (ActivitySequence) getUserObject();
  }

  public static Icon getIcon() {
    if (icon == null)
      icon = edu.xtec.util.ResourceManager.getImageIcon("icons/sequence.gif");
    return icon;
  }

  @Override
  public javax.swing.Icon getIcon(boolean leaf, boolean expanded) {
    return getIcon();
  }

  @Override
  public String toString() {
    return "Activity sequence";
  }

  public Options getOptions() {
    return getActivitySequence().getProject().getBridge().getOptions();
  }

  public JClicProjectEditor getProjectEditor() {
    return (JClicProjectEditor) getFirstParent(JClicProjectEditor.class);
  }

  public void removeElementsWith(String activityName) {
    String act = StrUtils.nullableString(activityName);
    if (act != null) {
      act = FileSystem.stdFn(act);
      int i = 0;
      int numElements = getChildCount();
      while (i < numElements) {
        numElements = getChildCount();
        for (i = 0; i < numElements; i++) {
          ActivitySequenceElementEditor asee = (ActivitySequenceElementEditor) getChildAt(i);
          if (act.equals(asee.getActivitySequenceElement().getActivityName())) {
            remove(i);
            break;
          }
        }
      }
    }
  }

  public boolean createNewSequenceElement(int index, boolean prompt, Component dlgParent) {

    String act = null, tag = null;
    Messages msg = getOptions().getMessages();

    if (prompt) {
      ListModel lm = getProjectEditor().getActivityBagEditor().getListModel();
      if (lm.getSize() == 0) {
        msg.showAlert(dlgParent, "edit_seq_newElement_error_emptyList");
        return false;
      }
      JComboBox actCombo = new JComboBox<Object>(
          new ListComboModel(getProjectEditor().getActivityBagEditor().getListModel()));
      actCombo.setToolTipText(msg.get("edit_seq_activity_tooltip"));
      JTextField tagField = new JTextField();
      tagField.setToolTipText(msg.get("edit_seq_tag_tooltip"));
      JComponent[] prompt_objects = new JComponent[] { actCombo, tagField };
      String[] prompt_keys = new String[] { "edit_seq_activity", "edit_seq_tag" };
      String[] prompt_msg = new String[] { "edit_seq_newElement_msg" };

      if (!msg.showInputDlg(dlgParent, prompt_msg, prompt_keys, prompt_objects, "edit_seq_newElement"))
        return false;
      act = StrUtils.nullableString(actCombo.getSelectedItem());
      tag = StrUtils.nullableString(tagField.getText());
    }

    if (act == null) {
      msg.showAlert(dlgParent, "edit_seq_newElement_error_noAct");
      return false;
    }

    return createNewSequenceElement(act, tag, index);
  }

  public boolean createNewSequenceElement(String actName, String tag, int index) {

    ActivitySequenceElement ase = new ActivitySequenceElement(actName);
    if (tag != null)
      ase.setTag(tag);

    ActivitySequenceElementEditor aseed = (ActivitySequenceElementEditor) ase.getEditor(null);

    if (index < 0)
      index = getChildCount();
    else
      index = Math.min(index, getChildCount());

    return insertEditor(aseed, true, index, true);
  }

  public ListModel<Object> getTagList() {
    if (tagList == null) {
      tagList = new DefaultListModel<Object>();
      Enumeration en = children();
      while (en.hasMoreElements()) {
        ActivitySequenceElementEditor asee = (ActivitySequenceElementEditor) en.nextElement();
        String tag = StrUtils.nullableString(asee.getActivitySequenceElement().getTag());
        if (tag != null)
          tagList.addElement(tag);
      }
    }
    return tagList;
  }

  public String getTag(String tag) {
    String result = null;
    getTagList();
    Enumeration en = tagList.elements();
    while (en.hasMoreElements() && result == null) {
      String s = (String) en.nextElement();
      if (s.equals(tag))
        result = s;
    }
    return result;
  }

  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    super.insert(newChild, childIndex);
    if (!initializing) {
      ActivitySequenceElementEditor asee = (ActivitySequenceElementEditor) newChild;
      getActivitySequence().insertElementAt(asee.getActivitySequenceElement(), childIndex);
      if (tagList != null) {
        String tag = asee.getTag();
        if (tag != null && getTag(tag) == null) {
          tagList.addElement(tag);
        }
      }
    }
  }

  @Override
  public void remove(int childIndex) {
    ActivitySequenceElementEditor asee = (ActivitySequenceElementEditor) getChildAt(childIndex);
    super.remove(childIndex);
    getActivitySequence().remove(asee.getActivitySequenceElement());
    if (tagList != null && asee.getTag() != null) {
      String s = getTag(asee.getTag());
      if (s != null)
        tagList.removeElement(s);
    }
  }

  public boolean nameChanged(int type, String oldName, String newName) {
    boolean result = false;
    if ((type & (Constants.T_ACTIVITY | Constants.T_SEQUENCE)) != 0) {
      Enumeration en = children();
      while (en.hasMoreElements())
        result |= ((ActivitySequenceElementEditor) en.nextElement()).nameChanged(type, oldName, newName);
    }
    return result;
  }

  @Override
  public void setActionsOwner() {
    allowDelete = allowCut = allowCopy = allowPaste = false;
    super.setActionsOwner();
    if (actionsCreated) {
      newActivitySequenceElementAction.setActionOwner(this);
    }
  }

  @Override
  public void clearActionsOwner() {
    super.clearActionsOwner();
    if (actionsCreated) {
      newActivitySequenceElementAction.setActionOwner(null);
    }
  }

  public static void createActions(Options options) {
    createBasicActions(options);
    if (!actionsCreated) {
      newActivitySequenceElementAction = new EditorAction("edit_seq_newElement", "icons/sequence_new.gif",
          "edit_seq_newElement_tooltip", options) {
        protected void doAction(Editor e) {
          Editor ch = null;
          if (e instanceof ActivitySequenceElementEditor) {
            ch = e;
            e = e.getEditorParent();
          }

          if (e instanceof ActivitySequenceEditor)
            ((ActivitySequenceEditor) e).createNewSequenceElement(e.getNearestIndex(ch, true), true, getComponentSrc());
        }
      };
      actionsCreated = true;
    }
  }
}
