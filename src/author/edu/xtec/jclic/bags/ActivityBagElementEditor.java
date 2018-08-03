/*
 * File    : ActivityBagElementEditor.java
 * Created : 22-apr-2003 16:29
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

package edu.xtec.jclic.bags;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.ActivityEditor;
import edu.xtec.jclic.TestPlayerContainer;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.edit.EditorAction;
import edu.xtec.jclic.edit.EditorPanel;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.util.Options;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ActivityBagElementEditor extends Editor {

  public static ImageIcon icon;
  public static EditorAction testActivityAction;
  public static boolean actionsCreated;

  /** Creates a new instance of ActivityBagElementEditor */
  public ActivityBagElementEditor(ActivityBagElement abe) {
    super(abe);
  }

  protected void createChildren() {
  }

  public EditorPanel createEditorPanel(Options options) {
    ActivityEditor ae = getActivityEditor();
    return (ae == null ? null : ae.createEditorPanel(options));
  }

  public Class getEditorPanelClass() {
    ActivityEditor ae = getActivityEditor();
    return (ae == null ? null : ae.getEditorPanelClass());
  }

  public ActivityBagElement getActivityBagElement() {
    return (ActivityBagElement) getUserObject();
  }

  public ActivityEditor getActivityEditor() {
    ActivityEditor ae = getChildCount() > 0 ? (ActivityEditor) getFirstChild() : null;
    if (ae == null) {
      try {
        ActivityBagElement abel = getActivityBagElement();
        JClicProject prj = ((ActivityBagEditor) getEditorParent()).getActivityBag().getProject();
        Activity act = Activity.getActivity(abel.getData(), prj);
        ae = (ActivityEditor) act.getEditor(this);
      } catch (Exception ex) {
        System.err.println("Error loading activity!\n");
        ex.printStackTrace(System.err);
      }
    }
    return ae;
  }

  public void changeActivityClass(String newClassName) throws Exception {
    ((ActivityBagEditor) getEditorParent()).changeActivityClass(this, newClassName);
  }

  public void forgetActivityEditor() {
    if (getChildCount() > 0) {
      ActivityEditor ae = (ActivityEditor) getFirstChild();
      if (ae.isModified())
        ae.saveData();
      removeAllChildren();
    }
  }

  public static Icon getIcon() {
    if (icon == null)
      icon = edu.xtec.util.ResourceManager.getImageIcon("icons/miniclic.png");
    return icon;
  }

  @Override
  public javax.swing.Icon getIcon(boolean leaf, boolean expanded) {
    return getIcon();
  }

  @Override
  public String toString() {
    return getActivityBagElement().getName();
  }

  @Override
  public boolean canBeParentOf(Editor e) {
    return (e instanceof ActivityEditor);
  }

  @Override
  public boolean canBeSiblingOf(Editor e) {
    return (e instanceof ActivityBagElementEditor);
  }

  @Override
  protected boolean canClone() {
    return true;
  }

  @Override
  protected Editor getClone() throws Exception {
    ActivityBagElement abe = (ActivityBagElement) getActivityBagElement().clone();
    return abe.getEditor(null);
  }

  protected void setActionsFlag() {
    allowDelete = true;
    allowCut = true;
    allowCopy = true;
    allowPaste = true;
  }

  @Override
  protected boolean delete(boolean changeSelection) {
    ActivityEditor aed = getActivityEditor();
    String activityName = aed.getActivity().name;
    JClicProjectEditor projectEditor = aed.getProjectEditor();
    boolean result = super.delete(changeSelection);
    if (result) {
      projectEditor.getActivitySequenceEditor().removeElementsWith(activityName);
    }
    return result;
  }

  @Override
  public void setActionsOwner() {
    setActionsFlag();
    super.setActionsOwner();
    if (actionsCreated) {
      testActivityAction.setActionOwner(this);
      ActivityBagEditor.newActivityBagElementAction.setActionOwner(this);
      ActivityBagEditor.copyActivityAttributesAction.setActionOwner(this);
    }
  }

  @Override
  public void clearActionsOwner() {
    super.clearActionsOwner();
    testActivityAction.setActionOwner(null);
    ActivityBagEditor.newActivityBagElementAction.setActionOwner(getEditorParent());
    ActivityBagEditor.copyActivityAttributesAction.setActionOwner(null);
  }

  public void testActivity() {
    ActivityEditor ae = getActivityEditor();
    ae.collectData();
    JClicProjectEditor pe = ae.getProjectEditor();
    if (pe != null) {
      TestPlayerContainer tpc = pe.getTestPlayerContainer();
      if (tpc != null && tpc.getTestPlayer() != null) {
        String activityName = ae.getActivity().name;
        ActivitySequenceEditor ased = pe.getActivitySequenceEditor();
        if (ased.getActivitySequence().getElementByActivityName(activityName) == null) {
          ased.createNewSequenceElement(activityName, null, -1);
        }
        pe.collectData();
        pe.getProject().skin = null;
        pe.getProject().realize(null, tpc.getTestPlayer());
        tpc.getTestPlayer().load(null, null, activityName, null);
        tpc.test();
      }
    }
  }

  public static void createActions(Options options) {
    createBasicActions(options);
    if (!actionsCreated) {
      testActivityAction = new EditorAction("edit_act_testAction", "icons/play.gif", "edit_act_testAction_tooltip",
          options) {
        protected void doAction(Editor e) {
          if (e instanceof ActivityBagElementEditor) {
            ((ActivityBagElementEditor) e).testActivity();
          } else if (e instanceof ActivitySequenceElementEditor) {
            ((ActivitySequenceElementEditor) e).testActivity();
          }
        }
      };
      actionsCreated = true;
    }
  }

  public boolean nameChanged(int type, String oldName, String newName) {
    boolean result = false;
    boolean hasChild = getChildCount() > 0;
    ActivityEditor ae = getActivityEditor();
    if (ae != null)
      result = ae.nameChanged(type, oldName, newName);
    if (!hasChild)
      forgetActivityEditor();
    return result;
  }
}
