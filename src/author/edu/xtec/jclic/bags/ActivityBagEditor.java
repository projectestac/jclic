/*
 * File    : ActivityBagEditor.java
 * Created : 19-sep-2002 09:53
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

import edu.xtec.jclic.*;
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.MutableTreeNode;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class ActivityBagEditor extends Editor {

  public static ImageIcon icon;
  public static EditorAction newActivityBagElementAction;
  public static EditorAction copyActivityAttributesAction;
  public static boolean actionsCreated;
  private boolean initializing;
  private static NewActivityPanel newActivityPanel = null;

  /** Creates a new instance of ActivityBagEditor */
  public ActivityBagEditor(ActivityBag ab) {
    super(ab);
  }

  protected void createChildren() {
    initializing = true;
    ActivityBag ab = getActivityBag();
    if (ab != null) {
      int s = ab.size();
      for (int i = 0; i < s; i++) {
        ab.elementAt(i).getEditor(this);
      }
    }
    initializing = false;
  }

  public EditorPanel createEditorPanel(Options options) {
    return new ActivityBagEditorPanel(options);
  }

  public Class getEditorPanelClass() {
    return ActivityBagEditorPanel.class;
  }

  @Override
  public String getTitleKey() {
    return "edit_activities";
  }

  public ActivityBag getActivityBag() {
    return (ActivityBag) getUserObject();
  }

  public static Icon getIcon() {
    if (icon == null)
      icon = edu.xtec.util.ResourceManager.getImageIcon("icons/database.gif");
    return icon;
  }

  @Override
  public javax.swing.Icon getIcon(boolean leaf, boolean expanded) {
    return getIcon();
  }

  @Override
  public String toString() {
    return "Activity bag";
  }

  public Options getOptions() {
    return getActivityBag().getProject().getBridge().getOptions();
  }

  public JClicProjectEditor getProjectEditor() {
    return (JClicProjectEditor) getFirstParent(JClicProjectEditor.class);
  }

  public boolean createNewActivityBagElement(int index, Component dlgParent) {
    String actClassName = null, actName = null;

    // Modified: 14-09-2010 - suggested by Camille Manoury
    // On errors (empty class name, empty or repeated activity name...) prompt again
    while (true) {

      if (newActivityPanel == null)
        newActivityPanel = new NewActivityPanel(getOptions());
      else
        newActivityPanel.setActivityName(null);

      if (!getOptions().getMessages().showInputDlg(dlgParent, newActivityPanel, "edit_act_newActivity"))
        return false;

      actClassName = newActivityPanel.gectActivityClassName();
      if (actClassName == null) {
        getOptions().getMessages().showAlert(dlgParent, "edit_act_newActivity_error_noAct");
        continue;
      }

      actName = newActivityPanel.getActivityName();
      if (actName == null) {
        getOptions().getMessages().showAlert(dlgParent, "edit_act_newActivity_error_noName");
        continue;
      }

      if (getActivityBag().getElementIndex(actName) >= 0) {
        getOptions().getMessages().showAlert(dlgParent, "edit_media_rename_exists");
        continue;
      }

      break;
    }

    Activity act;
    try {
      act = Activity.getActivity(actClassName, getProjectEditor().getProject());
      act.name = actName;
    } catch (Exception ex) {
      getOptions().getMessages().showErrorWarning(dlgParent, "edit_act_newActivity_error_creating", ex);
      return false;
    }

    org.jdom.Element e = act.getJDomElement();
    if (e == null) {
      getOptions().getMessages().showErrorWarning(dlgParent, "edit_act_newActivity_error_creating", null);
      return false;
    }

    ActivityBagElement abe = new ActivityBagElement(act.getJDomElement());

    ActivityBagElementEditor abeed = (ActivityBagElementEditor) abe.getEditor(null);

    if (index < 0)
      index = getChildCount();
    else
      index = Math.min(index, getChildCount());

    return insertEditor(abeed, true, index, true);
  }

  public void changeActivityClass(ActivityBagElementEditor abed, String newClassName) throws Exception {
    abed.collectData();
    ActivityBagElement abe = abed.getActivityBagElement();
    abed.forgetActivityEditor();
    Activity act = Activity.getActivity(newClassName, getProjectEditor().getProject());
    act.setProperties(abe.getData(), null);
    abe.setData(act.getJDomElement());
    setModified(true);
  }

  @Override
  public void setActionsOwner() {
    allowDelete = allowCut = allowCopy = allowPaste = false;
    super.setActionsOwner();
    if (actionsCreated) {
      newActivityBagElementAction.setActionOwner(this);
    }
  }

  @Override
  public void clearActionsOwner() {
    super.clearActionsOwner();
    if (actionsCreated) {
      newActivityBagElementAction.setActionOwner(null);
    }
  }

  public static void createActions(Options options) {
    createBasicActions(options);
    if (!actionsCreated) {
      newActivityBagElementAction = new EditorAction("edit_act_newActivity", "icons/new_miniclic.png",
          "edit_act_newActivity_tooltip", options) {
        protected void doAction(Editor e) {
          Editor ch = null;
          if (e instanceof ActivityBagElementEditor) {
            ch = e;
            e = e.getEditorParent();
          }
          if (e instanceof ActivityBagEditor)
            ((ActivityBagEditor) e).createNewActivityBagElement(e.getNearestIndex(ch, true), getComponentSrc());
        }
      };
      copyActivityAttributesAction = new EditorAction("edit_act_copyAttributes", "icons/copy_properties.gif",
          "edit_act_copyAttributes_tooltip", options) {
        protected void doAction(Editor e) {
          Editor ch = null;
          if (e instanceof ActivityBagElementEditor) {
            CopyActivityAttributesPanel catrp = new CopyActivityAttributesPanel(this.options,
                (ActivityBagElementEditor) e);
            catrp.showDialog(getComponentSrc());
          }
        }
      };
      actionsCreated = true;
    }
  }

  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    super.insert(newChild, childIndex);
    if (!initializing) {
      ActivityBagElement abe = ((ActivityBagElementEditor) newChild).getActivityBagElement();
      if (getActivityBag().getElementIndex(abe.getName()) >= 0) {
        String name = abe.getName();
        int i = name.length() - 1;
        while (i >= 0 && Character.isDigit(name.charAt(i)))
          i--;
        name = name.substring(0, i + 1);
        if (!name.endsWith("_"))
          name = name + "_";
        int suffix = 2;
        while (getActivityBag().getElementIndex(name + suffix) >= 0)
          suffix++;
        abe.getData().setAttribute(Activity.NAME, name + suffix);
      }
      getActivityBag().insertElementAt(abe, childIndex);
    }
  }

  @Override
  public void remove(int childIndex) {
    super.remove(childIndex);
    getActivityBag().removeElementAt(childIndex);
  }

  public boolean nameChanged(int type, String oldName, String newName) {
    boolean result = false;
    Enumeration en = children();
    while (en.hasMoreElements())
      result |= ((ActivityBagElementEditor) en.nextElement()).nameChanged(type, oldName, newName);
    return result;
  }

  public int checkOrphanElements(Options options, Component parent, boolean prompt) {
    int result = Messages.YES;
    List<String> v = new ArrayList<String>();
    ActivityBagElement[] abel = getActivityBag().getElements();
    ActivitySequenceEditor ased = getProjectEditor().getActivitySequenceEditor();
    ActivitySequenceElement[] asel = ased.getActivitySequence().getElements();
    for (int i = 0; i < abel.length; i++) {
      String actName = abel[i].getName();
      int j;
      for (j = 0; j < asel.length; j++) {
        if (asel[j].getActivityName().equals(actName))
          break;
      }
      if (j == asel.length) {
        v.add(actName);
      }
    }
    if (!v.isEmpty()) {
      boolean doIt = !prompt;
      if (!doIt) {
        Object[] object = new Object[] { options.getMsg("edit_project_orphanActivities"),
            v.size() > 10 ? (Object) (new javax.swing.JScrollPane(new javax.swing.JList<Object>(v.toArray())))
                : (Object) v,
            options.getMsg("edit_project_orphanActivities_prompt"), };
        result = options.getMessages().showQuestionDlgObj(parent, object, "edit_project_orphanActivities_title", "ync");
        doIt = (result == Messages.YES);
      }
      if (doIt) {
        Iterator<String> it = v.iterator();
        while (it.hasNext()) {
          ased.createNewSequenceElement(it.next(), null, -1);
        }
      }
    }
    return result;
  }
}
