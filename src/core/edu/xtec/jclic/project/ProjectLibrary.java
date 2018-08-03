/*
 * File    : ProjectLibrary.java
 * Created : 04-jun-2002 10:37
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

package edu.xtec.jclic.project;

import edu.xtec.jclic.*;
import edu.xtec.jclic.activities.panels.*;
import edu.xtec.jclic.bags.*;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.ResourceBridge;
import edu.xtec.util.ResourceManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import javax.swing.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ProjectLibrary extends JClicProject implements Editable {

  public static final String LIBRARY_TYPE = "library";
  public static final String MSG_ID = "library_";

  /** Creates new ProjectLibrary */
  private ProjectLibrary(ResourceBridge rb, FileSystem fileSystem, String fullPath) {
    super(rb, fileSystem, fullPath);
    type = LIBRARY_TYPE;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    type = null;
    super.setProperties(e, aux);
    if (!LIBRARY_TYPE.equals(type))
      throw new Exception(bridge.getMsg("library_badFormat"));
  }

  public static ProjectLibrary createNewProjectLibrary(ResourceBridge rb, FileSystem fileSystem) {
    ProjectLibrary pl = new ProjectLibrary(rb, fileSystem, null);
    pl.settings.title = rb.getMsg("library_newLibraryName");
    Menu m = new Menu(pl);
    m.name = "main";
    String description = rb.getMsg("library_mainMenu");
    m.description = description;
    ActiveBoxContent[] messages = m.getMessages();
    messages[Activity.MAIN] = new ActiveBoxContent();
    messages[Activity.MAIN].setBoxBase(new BoxBase());
    messages[Activity.MAIN].setTextContent(description);
    pl.activityBag.addActivity(m);
    pl.activitySequence.add(new ActivitySequenceElement(m.name, true));
    return pl;
  }

  public static ProjectLibrary getProjectLibrary(org.jdom.Element e, ResourceBridge rb, FileSystem fileSystem,
      String fullPath) throws Exception {
    ProjectLibrary pl = new ProjectLibrary(rb, fileSystem, fullPath);
    pl.setProperties(e, null);
    return pl;
  }

  public static ProjectLibrary loadProjectLibrary(String fullPath, ResourceBridge rb) throws Exception {
    FileSystem fs = FileSystem.createFileSystem(fullPath, rb);
    org.jdom.Document doc = fs.getXMLDocument(FileSystem.getFileNameOf(fullPath));
    ProjectLibrary pl = new ProjectLibrary(rb, fs, fullPath);
    pl.setProperties(doc.getRootElement(), null);
    return pl;
  }

  public void save(String path) throws Exception {
    if (path == null)
      path = fullPath;
    FileOutputStream fos = fileSystem.createSecureFileOutputStream(path);
    saveDocument(fos);
    fos.close();
  }

  @Override
  public String toString() {
    return settings.title;
  }

  @Override
  public String getPublicName() {
    return settings.title;
  }

  public Menu getRootMenu() {
    Menu result = null;
    if (activitySequence.getSize() > 0) {
      try {
        ActivitySequenceElement ase = activitySequence.getElement(0, false);
        if (ase != null) {
          Activity act = Activity.getActivity(activityBag.getElement(ase.getActivityName()).getData(), this);
          if (act instanceof Menu) {
            result = (Menu) act;
          }
        }
      } catch (Exception ex) {
        System.err.println("Error reading project library!\n" + ex);
      }
    }
    return result;
  }

  @Override
  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

  public boolean editProjectLibrary(Object parent) {
    ProjectLibraryDialog pld;
    if (parent instanceof JDialog)
      pld = new ProjectLibraryDialog(true, true, (JDialog) parent);
    else
      pld = new ProjectLibraryDialog(true, true);
    pld.setVisible(true);
    return pld.accept;
  }

  protected JComponent getRbComponent() {
    if (bridge == null)
      return null;
    return bridge.getComponent();
  }

  protected String getRbMessage(String msg) {
    if (bridge == null) {
      return "";
    }
    return bridge.getMsg(msg);
  }

  class ProjectLibraryDialog extends edu.xtec.util.ExtendedJDialog {

    ProjectLibraryEditor pled;
    EditorTreePanel etp;
    Menu result = null;
    boolean accept = false;
    boolean allowEdit, allowNewMenu;
    Action selectAction, cancelAction;

    ProjectLibraryDialog(boolean allowNewMenu, boolean allowEdit, JDialog parent) {
      super(parent, bridge.getMsg(MSG_ID + "caption"), true);
      this.allowEdit = allowEdit;
      this.allowNewMenu = allowNewMenu;
      init();
    }

    ProjectLibraryDialog(boolean allowNewMenu, boolean allowEdit) {
      super(getRbComponent(), getRbMessage(MSG_ID + "caption"), true);
      this.allowEdit = allowEdit;
      this.allowNewMenu = allowNewMenu;
      init();
    }

    protected void init() {
      buildActions();
      pled = (ProjectLibraryEditor) getEditor(null);
      etp = new EditorTreePanel(pled, bridge.getOptions(), !allowEdit, (allowEdit ? null : Menu.class)) {
        @Override
        protected void currentItemChanged() {
          if (!allowEdit)
            selectAction.setEnabled(currentItem != null);
          super.currentItemChanged();
        }
      };
      getContentPane().add(etp, BorderLayout.CENTER);
      JPanel buttonsPanel = new JPanel();
      buttonsPanel.add(new JButton(selectAction));
      buttonsPanel.add(new JButton(cancelAction));
      /*
       * if(allowNewMenu || allowEdit){ buttonsPanel.add(new
       * JButton(newFolderAction)); }
       */
      getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
      pack();
      centerOver(bridge.getComponent());
    }

    void buildActions() {
      String s = allowEdit ? "select_caption_ok" : "select_caption";
      selectAction = new AbstractAction(bridge.getMsg(MSG_ID + s),
          ResourceManager.getImageIcon(allowEdit ? "icons/commit_changes.gif" : "icons/file_open.gif")) {
        public void actionPerformed(ActionEvent ev) {
          if (allowEdit && etp.getCurrentPanel() != null) {
            etp.getCurrentPanel().save();
            if (pled.isModified()) {
              pled.saveMenus(pled);
              try {
                save(null);
              } catch (Exception ex) {
                bridge.getOptions().getMessages().showErrorWarning(getParent(), "FILE_ERR_SAVING", fullPath, ex, null);
              }
            }
          }
          if (etp.currentItem != null && etp.currentItem instanceof MenuEditor) {
            result = ((MenuEditor) etp.currentItem).getMenu();
          } else {
            result = null;
            if (!allowEdit)
              return;
          }
          accept = true;
          setVisible(false);
        }
      };
      selectAction.putValue(AbstractAction.SHORT_DESCRIPTION, bridge.getMsg(MSG_ID + s + "_tooltip"));
      if (!allowEdit)
        selectAction.setEnabled(false);

      cancelAction = new AbstractAction(bridge.getMsg(MSG_ID + "cancel_caption"),
          ResourceManager.getImageIcon("icons/cancel.gif")) {
        public void actionPerformed(ActionEvent ev) {
          result = null;
          accept = false;
          setVisible(false);
        }
      };
      cancelAction.putValue(AbstractAction.SHORT_DESCRIPTION, bridge.getMsg(MSG_ID + "cancel_tooltip"));
    }
  }
}
