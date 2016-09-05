/*
 * File    : LibraryManager.java
 * Created : 04-jun-2002 11:13
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

import edu.xtec.jclic.*;
import edu.xtec.jclic.activities.panels.Menu;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Messages;
import edu.xtec.util.PersistentSettings;
import edu.xtec.util.ResourceBridge;
import edu.xtec.util.ResourceManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.10.04
 */
public class LibraryManager implements Domable {

  protected DefaultListModel libraries;
  protected PlayerSettings settings;
  protected boolean modified;
  public boolean autoRun;
  public static final String AUTO_RUN = "autoRun";
  public static final String ELEMENT_NAME = "libraryManager";
  private static final String MSG_ID = "libraryManager_";
  public static final String PROJECT_LIBRARIES = "project.libraries";

  /**
   * Creates new LibraryManager
   */
  public LibraryManager(PlayerSettings settings) {
    this.settings = settings;
    libraries = new DefaultListModel();
    modified = false;
    autoRun = true;
  }

  public static LibraryManager getLibraryManager(PlayerSettings st, org.jdom.Element e) throws Exception {
    LibraryManager lm = new LibraryManager(st);
    lm.setProperties(e, null);
    return lm;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    autoRun = JDomUtility.getBoolAttr(e, AUTO_RUN, autoRun);

    String[][] sysLibs = getSystemLibraries();

    if (sysLibs != null) {
      for (int i = 0; i < sysLibs.length; i++) {
        LibraryManagerElement lme = new LibraryManagerElement(sysLibs[i][0], sysLibs[i][1], settings.rb.getOptions());
        lme.setSystemLib(true);
        libraries.addElement(lme);
      }
    }

    Iterator it = e.getChildren(LibraryManagerElement.ELEMENT_NAME).iterator();
    while (it.hasNext()) {
      libraries.addElement(LibraryManagerElement.getLibraryManagerElement((org.jdom.Element) it.next(), settings.rb.getOptions()));
    }
  }

  public void addNewLibrary(String path, String title) throws Exception {
    File f = new File(path);
    if (f.exists()) {
      ProjectLibrary pl = ProjectLibrary.loadProjectLibrary(path, settings.rb);
      title = pl.settings.title;
    } else {
      if (title == null) {
        title = settings.rb.getMsg("libraryManager_mainLibraryTitle");
      }
      ProjectLibrary pl = ProjectLibrary.createNewProjectLibrary(settings.rb, settings.fileSystem);
      pl.settings.title = title;
      pl.save(path);
    }
    libraries.addElement(new LibraryManagerElement(title, path, settings.rb.getOptions()));
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(AUTO_RUN, JDomUtility.boolString(autoRun));
    for (int i = 0; i < libraries.size(); i++) {
      LibraryManagerElement lme = (LibraryManagerElement) libraries.get(i);
      if (!lme.isSystemLib()) {
        e.addContent(lme.getJDomElement());
      }
    }
    return e;
  }

  public boolean isEmpty() {
    return libraries.isEmpty();
  }

  public ProjectLibrary getAutoStartProjectLibrary() throws Exception {
    ProjectLibrary result = null;
    if (autoRun && !isEmpty()) {
      if (libraries.size() > 1) {
        result = selectProjectLibrary(false, false);
      } else {
        result = ProjectLibrary.loadProjectLibrary(((LibraryManagerElement) libraries.get(0)).path, settings.rb);
        if (result != null) {
          Menu m = result.getRootMenu();
          if (m == null || m.getMenuElementCount() < 1) {
            result = null;
          }
        }
      }
    }
    return result;
  }

  public ProjectLibrary selectProjectLibrary(boolean allowEdit, boolean selectOnlyEditable) throws Exception {
    ProjectLibrary result = null;
    if (!libraries.isEmpty() || allowEdit) {
      LibraryDialog plDlg = new LibraryDialog(allowEdit, selectOnlyEditable);
      plDlg.setVisible(true);
      if (plDlg.result != null) {
        result = ProjectLibrary.loadProjectLibrary(plDlg.result.path, settings.rb);
      }
    }
    return result;
  }

  public int getNumLibraries() {
    return libraries.getSize();
  }

  public LibraryManagerElement locateNewProjectLibrary(String path) {

    LibraryManagerElement result = null;
    ProjectLibrary pl;
    ResourceBridge rb = settings.rb;

    if (path == null) {
      int[] filters = {Utils.JCLIC_FF};
      String s = settings.fileSystem.chooseFile(settings.rootPath + File.separator + ".", false, filters, rb.getOptions(), null, rb.getComponent(), false);
      if (s != null) {
        path = settings.fileSystem.getFullFileNamePath(s);
      }
    }

    if (path != null && !elementExists(path, true)) {
      try {
        pl = ProjectLibrary.loadProjectLibrary(path, rb);
        result = new LibraryManagerElement(pl.settings.title, path, rb.getOptions());
      } catch (Exception ex) {
        settings.rb.getOptions().getMessages().showErrorWarning(rb.getComponent(), "FILE_ERR_READING", path, ex, null);
      }
    }
    return result;
  }

  public LibraryManagerElement createNewProjectLibrary(String path, String name) {

    LibraryManagerElement result = null;
    ResourceBridge rb = settings.rb;

    // Unused param "name"
    // if(name==null)
    //    name=rb.getMsg("library_newLibraryName");
    if (path == null) {
      int[] filters = {Utils.JCLIC_FF};
      String s = settings.fileSystem.chooseFile(settings.rootPath + File.separator + "library.jclic", true, filters, rb.getOptions(), null, rb.getComponent(), false);
      if (s != null) {
        path = settings.fileSystem.getFullFileNamePath(s);
      }
    }

    if (path != null && !elementExists(path, true)) {
      ProjectLibrary pl = ProjectLibrary.createNewProjectLibrary(rb, settings.fileSystem);
      try {
        pl.save(path);
        result = new LibraryManagerElement(pl.settings.title, path, rb.getOptions());
      } catch (Exception ex) {
        settings.rb.getOptions().getMessages().showErrorWarning(rb.getComponent(), "FILE_ERR_SAVING", path, ex, null);
      }
    }
    return result;
  }

  public boolean elementExists(String path, boolean warn) {
    boolean result = false;
    for (int i = 0; i < libraries.size(); i++) {
      LibraryManagerElement lme = (LibraryManagerElement) libraries.get(i);
      if (path.equals(settings.fileSystem.getFullFileNamePath(lme.path))) {
        result = true;
        if (warn) {
          settings.rb.getOptions().getMessages().showAlert(settings.rb.getComponent(), "libraryManager_new_exists");
        }
        break;
      }
    }
    return result;
  }

  public LibraryPane getLibraryPane(boolean allowEdit, boolean selectOnlyEditable) {
    return new LibraryPane(allowEdit, selectOnlyEditable);
  }

  public class LibraryPane extends JPanel implements ListSelectionListener {

    Action editAction, newLibraryAction, deleteAction;
    JList list;
    boolean allowEdit, onlyEditable;
    LibraryManagerElement current = null;

    LibraryPane(boolean allowEdit, boolean selectOnlyEditable) {
      super(new BorderLayout());
      this.allowEdit = allowEdit;
      onlyEditable = selectOnlyEditable;
      buildActions();
      list = new JList(libraries);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener(this);
      if (list.getModel().getSize() > 0) {
        list.setSelectedIndex(0);
      }
      list.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if (value instanceof LibraryManagerElement) {
            LibraryManagerElement lme = (LibraryManagerElement) value;
            setIcon(lme.getIcon());
            if (!lme.exists || (!lme.editable && onlyEditable) || lme.isSystemLib()) {
              setForeground(java.awt.SystemColor.textInactiveText);
            }
          }
          return this;
        }
      });
      add(new JScrollPane(list), BorderLayout.CENTER);
      if (allowEdit) {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(new JButton(editAction));
        buttonsPanel.add(new JButton(newLibraryAction));
        buttonsPanel.add(new JButton(deleteAction));
        add(buttonsPanel, BorderLayout.SOUTH);
      }
    }

    void buildActions() {
      final edu.xtec.util.Options options = settings.rb.getOptions();
      editAction = new AbstractAction(
              options.getMsg(MSG_ID + "edit_caption"),
              ResourceManager.getImageIcon("icons/edit.gif")) {
        public void actionPerformed(ActionEvent ev) {
          LibraryManagerElement lme = (LibraryManagerElement) list.getSelectedValue();
          if (lme != null) {
            try {
              if (!settings.promptPassword(LibraryPane.this, null)) {
                return;
              }
              ProjectLibrary pl = ProjectLibrary.loadProjectLibrary(lme.path, settings.rb);
              if (pl.editProjectLibrary(LibraryPane.this)) {
                if (!pl.settings.title.equals(lme.name)) {
                  lme.name = pl.settings.title;
                  modified = true;
                  list.repaint();
                }
              }
            } catch (Exception ex) {
              System.err.println("Unable edit projectLibrary:\n" + ex);
            }
          }
        }
      };
      editAction.putValue(AbstractAction.SHORT_DESCRIPTION, options.getMsg(MSG_ID + "edit_tooltip"));
      editAction.setEnabled(false);

      deleteAction = new AbstractAction(
              options.getMsg(MSG_ID + "delete_caption"),
              ResourceManager.getImageIcon("icons/delete.gif")) {
        public void actionPerformed(ActionEvent ev) {
          LibraryManagerElement lme = (LibraryManagerElement) list.getSelectedValue();
          if (lme != null) {
            int currentIndex = list.getSelectedIndex();
            try {
              if (!settings.promptPassword(LibraryPane.this, null)
                      || !(options.getMessages().showQuestionDlg(null, MSG_ID + "delete_confirm", null, "yn") == Messages.YES)) {
                return;
              }
              libraries.removeElement(lme);
              modified = true;
              list.setSelectedIndex(Math.max(0, currentIndex - 1));
            } catch (Exception ex) {
              System.err.println("Unable to delete projectLibrary:\n" + ex);
            }
          }
        }
      };
      deleteAction.putValue(AbstractAction.SHORT_DESCRIPTION, options.getMsg(MSG_ID + "delete_tooltip"));
      deleteAction.setEnabled(false);

      newLibraryAction = new AbstractAction(
              options.getMsg(MSG_ID + "new_caption"),
              ResourceManager.getImageIcon("icons/database_new.gif")) {
        public void actionPerformed(ActionEvent ev) {
          if (!settings.promptPassword(LibraryPane.this, null)) {
            return;
          }

          /*
                     Object[] opcions = { options.getMsg(MSG_ID+"createNewLibrary"), options.getMsg(MSG_ID+"connectToLibrary"), options.getMsg("CANCEL")};
                     int n = JOptionPane.showOptionDialog(LibraryPane.this,
                     options.getMsg(MSG_ID+"new_prompt"),
                     null,
                     JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     opcions,
                     opcions[0]);

                     LibraryManagerElement lme=null;
                     if(n==JOptionPane.YES_OPTION)
                     lme=createNewProjectLibrary(null, null);
                     else if(n==JOptionPane.NO_OPTION)
                     lme=locateNewProjectLibrary(null);

           */
          LibraryManagerElement lme = NewLibraryDlg.getLibraryManagerElement(LibraryManager.this, LibraryPane.this);
          if (lme != null) {
            modified = true;
            libraries.addElement(lme);
            list.setSelectedValue(lme, true);
          }
        }
      };
      newLibraryAction.putValue(AbstractAction.SHORT_DESCRIPTION, options.getMsg(MSG_ID + "new_tooltip"));
    }

    public LibraryManagerElement getCurrentSelected() {
      return current;
    }

    public void addListSelectionListener(ListSelectionListener lst) {
      list.addListSelectionListener(lst);
    }

    public void valueChanged(ListSelectionEvent ev) {
      if (ev != null && !ev.getValueIsAdjusting()) {
        current = (LibraryManagerElement) list.getSelectedValue();
        editAction.setEnabled(current != null && current.editable);
        deleteAction.setEnabled(current != null && !current.isSystemLib());
        //list.setToolTipText(item==null ? null : item.settings.description);
      }
    }
  }

  class LibraryDialog extends edu.xtec.util.ExtendedJDialog implements ListSelectionListener {

    LibraryManagerElement result = null;
    Action selectAction, cancelAction;
    LibraryPane pane;
    boolean allowEdit, onlyEditable;

    LibraryDialog(boolean allowEdit, boolean selectOnlyEditable) {
      super(settings.rb.getComponent(), settings.rb.getMsg(MSG_ID + "caption"), true);
      this.allowEdit = allowEdit;
      onlyEditable = selectOnlyEditable;
      pane = new LibraryPane(allowEdit, selectOnlyEditable);
      pane.setOpaque(false);
      buildActions();
      pane.addListSelectionListener(this);
      getContentPane().add(pane, BorderLayout.CENTER);
      JPanel buttonsPanel = new JPanel();
      buttonsPanel.setOpaque(false);
      //buttonsPanel.add(new JButton(selectAction));
      JButton btSelect = new JButton(selectAction);
      btSelect.setDefaultCapable(true);
      buttonsPanel.add(btSelect);
      buttonsPanel.add(new JButton(cancelAction));
      getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
      getRootPane().setDefaultButton(btSelect);
      pack();
      centerOver(settings.rb.getComponent());
      pane.list.setSelectedIndex(0);
      valueChanged(new ListSelectionEvent(pane.list, 0, 0, false));
      valueChanged(null);
    }

    public void valueChanged(ListSelectionEvent ev) {
      if (ev != null && !ev.getValueIsAdjusting()) {
        LibraryManagerElement item = pane.getCurrentSelected();
        selectAction.setEnabled(item != null && item.exists && (!onlyEditable || item.editable));
      }
    }

    void closeDialog() {
      if (modified) {
        settings.save();
        modified = false;
      }
      setVisible(false);
    }

    void buildActions() {
      final edu.xtec.util.Options options = settings.rb.getOptions();
      selectAction = new AbstractAction(
              options.getMsg(MSG_ID + "select_caption"),
              ResourceManager.getImageIcon("icons/file_open.gif")) {
        public void actionPerformed(ActionEvent ev) {
          result = (LibraryManagerElement) pane.list.getSelectedValue();
          closeDialog();
        }
      };
      selectAction.putValue(AbstractAction.SHORT_DESCRIPTION, options.getMsg(MSG_ID + "select_tooltip"));
      selectAction.setEnabled(false);

      cancelAction = new AbstractAction(
              options.getMsg(MSG_ID + "cancel_caption"),
              ResourceManager.getImageIcon("icons/cancel.gif")) {
        public void actionPerformed(ActionEvent ev) {
          result = null;
          closeDialog();
        }
      };
      cancelAction.putValue(AbstractAction.SHORT_DESCRIPTION, options.getMsg(MSG_ID + "cancel_tooltip"));
    }
  }

  public static String[][] getSystemLibraries() {

    String[][] result = null;

    try {
      String projectLibraries = PersistentSettings.systemPrefs.get(PROJECT_LIBRARIES, "[]");
      JSONArray jsa = new JSONArray(projectLibraries);
      int numLibraries = jsa.length();
      if (numLibraries > 0) {
        result = new String[numLibraries][2];
        for (int i = 0; i < numLibraries; i++) {
          JSONObject jso = jsa.getJSONObject(i);
          if (jso != null) {
            result[i][0] = jso.getString("name");
            result[i][1] = jso.getString("path");
          }
          if (result[i][0] == null || result[i][1] == null) {
            throw new Exception("Invalid settings in JClic system libraries.");
          }
        }
      }
    } catch (Exception ex) {
      System.err.println("Error reading JClic system libraries: " + ex.getMessage());
      result = null;
    }

    return result;
  }

  public static void main(String[] args) {

    String command = null;
    String libName = null;
    String libPath = null;
    boolean silent = false;

    String[][] sysLibs = getSystemLibraries();

    boolean err = false;
    for (String s : args) {
      if(s.equals("-silent")) {
        silent = true;
      } else if (s.startsWith("-")) {
        if (command != null || s.length() == 1) {
          err = true;
        } else {
          command = s.substring(1);
        }
      } else if (libName == null) {
        libName = s;
      } else if (libPath == null) {
        libPath = s;
      } else {
        err = true;
      }
    }

    try {
      if (err || command == null) {
        printUsage();
      } else if (command.equals("add") && libName != null && libPath != null) {
        JSONArray jsa = new JSONArray();
        JSONObject jso;
        boolean done = false;
        if (sysLibs != null) {
          for (int i = 0; i < sysLibs.length; i++) {
            String name = sysLibs[i][0];
            String path = sysLibs[i][1];

            if (libName.equals(name)) {
              path = libPath;
              done = true;
            }
            jso = new JSONObject();
            jso.put("name", name);
            jso.put("path", path);
            jsa.put(jso);
          }
        }
        if (!done) {
          jso = new JSONObject();
          jso.put("name", libName);
          jso.put("path", libPath);
          jsa.put(jso);
        }

        try {
          PersistentSettings.systemPrefs.put(PROJECT_LIBRARIES, jsa.toString());
          PersistentSettings.systemPrefs.flush();
        } catch (Exception ex) {
          System.err.println("Unable to write system settings. Check your permissions!");
          System.err.println(ex.getMessage());
          err = true;
        }

      } else if (command.equals("remove") && libName != null) {
        JSONArray jsa = new JSONArray();
        JSONObject jso;
        boolean done = false;
        if (sysLibs != null) {
          for (int i = 0; i < sysLibs.length; i++) {
            String name = sysLibs[i][0];
            String path = sysLibs[i][1];

            if (libName.equals(name)) {
              done = true;
              continue;
            }

            jso = new JSONObject();
            jso.put("name", name);
            jso.put("path", path);
            jsa.put(jso);
          }
        }
        if (!done) {
          System.err.println("ERROR: Library \"" + libName + "\" does not exist!");
          err = true;
        } else {
          try {
            PersistentSettings.systemPrefs.put(PROJECT_LIBRARIES, jsa.toString());
            PersistentSettings.systemPrefs.flush();
          } catch (Exception ex) {
            System.err.println("Unable to write system settings. Check your permissions!");
            System.err.println(ex.getMessage());
            err = true;
          }
        }
      } else if (command.equals("list")) {
        if (sysLibs == null || sysLibs.length == 0) {
          if (!silent)
            System.out.println("There are no system libraries defined!");
        } else if (!silent) {
          for (int i = 0; i < sysLibs.length; i++) {
            System.out.println(sysLibs[i][0] + ": " + sysLibs[i][1]);
          }
        }
      } else {
        printUsage();
      }
    } catch (JSONException ex) {
      System.err.println("JSON Error: " + ex.getMessage());
      err = true;
    }

    System.exit(err ? -1 : 0);

  }

  protected static void printUsage() {
    System.out.println(
            "JClic System Libraries Manager\n"
            + "\n"
            + "usage: java -cp jclic.jar edu.xtec.jclic.project.LibraryManager -[option] [name] [path]\n"
            + "\n"
            + "Valid options are:\n"
            + "-add name path     Adds a new element to the list of system libraries.\n"
            + "                   'path' must point to a library.jclic file.\n"
            + "-remove name       Removes the library from the system list.\n"
            + "-list              Displays a list of the existing system libraries.\n"
            + "-silent            Supresses console output (except for errors).\n"
            + "-help              Displays this message.\n");
  }
}
