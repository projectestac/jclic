/*
 * File    : JClicProjctEditor.java
 * Created : 27-sep-2002 15:52
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

import edu.xtec.jclic.TestPlayerContainer;
import edu.xtec.jclic.bags.ActivityBagEditor;
import edu.xtec.jclic.bags.ActivitySequenceEditor;
import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.edit.EditorPanel;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class JClicProjectEditor extends Editor {

  protected static ImageIcon icon;

  ActivityBagEditor abe;
  ActivitySequenceEditor ase;
  MediaBagEditor mbe;
  ProjectSettingsEditor pse;
  TestPlayerContainer testPlayerContainer;

  /** Creates a new instance of JClicProjectEditor */
  public JClicProjectEditor(JClicProject project) {
    super(project);
  }

  protected void createChildren() {
    JClicProject jcp = getProject();
    jcp.mediaBag.addListener(jcp.activityBag);
    abe = (ActivityBagEditor) jcp.activityBag.getEditor(this);
    ase = (ActivitySequenceEditor) jcp.activitySequence.getEditor(this);
    mbe = (MediaBagEditor) jcp.mediaBag.getEditor(this);
    pse = (ProjectSettingsEditor) jcp.settings.getEditor(this);
  }

  public EditorPanel createEditorPanel(Options options) {
    // todo: create editor panel
    return null;
  }

  public Class getEditorPanelClass() {
    // todo: create panel class
    return null;
  }

  public JClicProject getProject() {
    return (JClicProject) getFirstObject(JClicProject.class);
  }

  public ActivityBagEditor getActivityBagEditor() {
    return abe;
  }

  public ActivitySequenceEditor getActivitySequenceEditor() {
    return ase;
  }

  public MediaBagEditor getMediaBagEditor() {
    return mbe;
  }

  public ProjectSettingsEditor getProjectSettingsEditor() {
    return pse;
  }

  public void setTestPlayerContainer(TestPlayerContainer tpc) {
    testPlayerContainer = tpc;
  }

  public TestPlayerContainer getTestPlayerContainer() {
    return testPlayerContainer;
  }

  public boolean nameChanged(int type, String oldName, String newName) {
    boolean result = getActivityBagEditor().nameChanged(type, oldName, newName)
        | getActivitySequenceEditor().nameChanged(type, oldName, newName)
        | getMediaBagEditor().nameChanged(type, oldName, newName);
    return result;
  }

  public static Icon getIcon() {
    if (icon == null)
      icon = edu.xtec.util.ResourceManager.getImageIcon("icons/project_settings.gif");
    return icon;
  }

  public void saveProject(String fileName) throws Exception {
    collectData();
    getProject().setName(fileName);
    getProject().saveProject(fileName);
    setModified(false);
  }

  public boolean checkProject(Options options, Component parent, boolean prompt) {
    collectData();
    return getActivityBagEditor().checkOrphanElements(options, parent, prompt) != Messages.CANCEL
        && getMediaBagEditor().checkOrphanElements(options, parent, prompt) != Messages.CANCEL;
  }
}
