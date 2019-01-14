/*
 * File    : AuthorSingleFrame.java
 * Created : 16-sep-2002 10:01
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
package edu.xtec.jclic;

import edu.xtec.jclic.bags.*;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.clic3.Clic3;
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.fileSystem.*;
import edu.xtec.jclic.media.*;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.jclic.project.*;
import edu.xtec.jclic.skins.AboutWindow;
import edu.xtec.util.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import org.json.JSONObject;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class AuthorSingleFrame extends JPanel
    implements ResourceBridge, TestPlayerContainer, RunnableComponent, Constants, ActionListener {

  protected Options options;
  protected Messages messages;
  protected JClicProject project;
  protected JFrame debugFrame = null;
  protected int recentFilesOffset;
  protected JMenuBar menuBar;
  protected JMenu fileMenu, recentFilesMenu, toolsMenu, helpMenu, editMenu, insertMenu, viewMenu;
  protected AuthorSettings settings;
  protected Player player;
  protected JTabbedPane tabbedPane;

  protected Action[] actions;
  protected Action[] projectActions;
  private edu.xtec.util.SwingWorker worker = null;
  protected JDialog playerDlg;
  protected JClicProjectEditor projectEditor;
  protected EditorPanel mediaBagEditorPanel;
  protected EditorPanel activityBagEditorPanel;
  protected EditorPanel activitySequenceEditorPanel;
  protected EditorPanel projectSettingsEditorPanel;
  protected ProgressDialog progressDialog;

  public static final String MESSAGES_BUNDLE = "messages.AuthorMessages";

  /** Creates a new instance of Author */
  public AuthorSingleFrame(Options options) {
    this.options = options;
    setLayout(new BorderLayout());
    init();
  }

  protected final void init() {
    settings = AuthorSettings.loadAuthorSettings(this);
    options.setLookAndFeel();
    Utils.checkRenderingHints(options);
    CheckMediaSystem.check(options, false);
    setMessages();
    buildActions();
    setActionsText();
    progressDialog = new ProgressDialog(this, options);
    ActiveBox.compressImages = options.getBoolean(COMPRESS_IMAGES, true);
    createFrames();
  }

  public void activate() {
    focusListener.focusGained(null);
  }

  // Methods inherited from ResourceBridge interface:
  public java.io.InputStream getProgressInputStream(java.io.InputStream is, int expectedLength, String name) {
    return progressDialog == null ? is : progressDialog.getProgressInputStream(is, expectedLength, name);
  }

  public edu.xtec.util.Options getOptions() {
    return options;
  }

  public String getMsg(String key) {
    return messages.get(key);
  }

  public javax.swing.JComponent getComponent() {
    return this;
  }

  // Methods inherited from interface RunnableComponent:
  public void addTo(javax.swing.RootPaneContainer cont, Object constraints) {
    cont.setContentPane(this);
    checkMenu(false);
  }

  public boolean start(String fullPath, String sequence) {
    boolean result = false;
    if (fullPath != null) {
      result = load(fullPath, sequence);
    }
    return result;
  }

  public void stop() {
  }

  public void end() {
  }

  public Messages setMessages() {
    messages = Messages.getMessages(options, DEFAULT_BUNDLE);
    messages.addBundle(COMMON_SETTINGS);
    messages.addBundle(ExtendedPlayer.MESSAGES_BUNDLE);
    messages.addBundle(MESSAGES_BUNDLE);
    setLocale(messages.getLocale());
    return messages;
  }

  public boolean load(String fullPath, String sequence) {
    load(fullPath, sequence, null);
    return true;
  }

  public void load(final String sFullPath, final String sSequence, final String sActivity) {

    if (worker != null) {
      return;
    }

    if (sFullPath != null && project != null) {
      if (project != null) {
        project.mediaBag.clearData();
      }
      if (player != null) {
        player.activeMediaBag.removeAll();
      }
      System.gc();
    }

    worker = new edu.xtec.util.SwingWorker() {

      ActivityBagElement abe = null;
      Exception exception = null;
      AuthorSingleFrame thisAuthor = AuthorSingleFrame.this;
      String fullPath = null;
      String sequence = null;
      String activityName = null;
      boolean imported = false;

      @Override
      public Object construct() {

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
          fullPath = Clic3.pacNameToLowerCase(sFullPath);
          sequence = Clic3.pacNameToLowerCase(sSequence);
          activityName = sActivity;
          boolean prompted = false;

          while (!isCancelled()) {
            String dn = fullPath != null ? fullPath : sequence != null ? sequence : activityName;
            progressDialog.setText(messages.get("msg_loading") + " " + dn);

            FileSystem fileSystem = project == null ? createFileSystem() : project.getFileSystem();

            // Step 1: load project
            if (fullPath != null) {
              if (fileSystem != null) {
                fullPath = fileSystem.getUrl(fullPath);
                if (fullPath.startsWith("file://")) {
                  fullPath = fullPath.substring(7);
                } else if (fullPath.indexOf('?') > 0) {
                  fullPath = fullPath.substring(0, fullPath.indexOf('?'));
                }
              }
              if (sequence == null) {
                sequence = "0";
              }

              String projectName = null;
              JSONObject json = null;
              if (fullPath.endsWith(Utils.EXT_JCLIC_ZIP)) {
                fileSystem = FileSystem.createFileSystem(fullPath, thisAuthor);
                String[] projects = ((ZipFileSystem) fileSystem).getEntries(".jclic");
                if (projects == null) {
                  throw new Exception("File " + fullPath + " does not contain any jclic project");
                }
                projectName = projects[0];
              } else if (fullPath.endsWith(Utils.EXT_SCORM_ZIP)) {
                fileSystem = FileSystem.createFileSystem(fullPath, thisAuthor);
                if (fileSystem.fileExists("project.json")) {
                  json = new JSONObject(new String(fileSystem.getBytes("project.json")));
                  projectName = json.optString("mainFile", null);
                }
                if (projectName == null) {
                  throw new Exception("Invalid JClic SCORM file: " + fullPath);
                }
              } else {
                fileSystem = new FileSystem(FileSystem.getPathPartOf(fullPath), thisAuthor);
                projectName = FileSystem.getFileNameOf(fullPath);
                if (fileSystem.fileExists("project.json"))
                  json = new JSONObject(new String(fileSystem.getBytes("project.json")));
              }

              if (projectName.endsWith(".jclic")) {
                org.jdom.Document doc = fileSystem.getXMLDocument(projectName);
                System.gc();
                JClicProject prj = JClicProject.getJClicProject(doc.getRootElement(), thisAuthor, fileSystem, fullPath);
                if (json != null) {
                  prj.readJSON(json, false);
                }
                setProject(prj);
              } else {
                sequence = projectName;
                setProject(new JClicProject(thisAuthor, fileSystem, fullPath));
              }
            }

            // load sequence
            if (sequence != null) {
              ActivitySequenceElement ase = null;
              String seqName = FileSystem.stdFn(sequence);
              // get existing sequence by name
              if (project != null) {
                ase = project.activitySequence.getElementByTag(seqName, true);
              }
              // get existing sequence by number
              if (ase == null && project != null) {
                int i = StrUtils.getAbsIntValueOf(seqName);
                if (i >= 0) {
                  ase = project.activitySequence.getElement(i, true);
                }
              }
              // load new sequence (only with Clic3 files)
              if (ase == null && project != null) {
                boolean firstPac = (project.activitySequence.getSize() == 0);
                boolean isPcc = seqName.endsWith(".pcc");
                boolean isPac = seqName.endsWith(".pac");
                if (isPcc || isPac) {
                  imported = true;
                  if (isPcc) {
                    String path = fileSystem.root + seqName;
                    fileSystem = FileSystem.createFileSystem(path, thisAuthor);
                    if (firstPac) {
                      project.setFileSystem(fileSystem);
                      project.setFullPath(path);
                    } else {
                      setProject(new JClicProject(thisAuthor, fileSystem, path));
                    }
                    firstPac = true;
                    Clic3.readPccFile(project);
                    ase = project.activitySequence.getCurrentAct();
                  } else if (isPac) {
                    Clic3.addPacToSequence(project, seqName);
                    ase = project.activitySequence.getElementByTag(seqName, true);
                  }

                  if (firstPac) {
                    project.setName(seqName);
                  }
                }
              }

              if (ase != null) {
                activityName = ase.getActivityName();
              }
            }

            // load activity
            if (activityName != null && project != null) {
              String actName = FileSystem.stdFn(activityName);
              abe = project.activityBag.getElement(actName);
            }

            fullPath = null;
            sequence = null;
            activityName = null;

            if (project != null) {
              project.activitySequence.checkAllElements();
            }

            if (project != null) {
              Map<String, String> hm = new HashMap<String, String>();
              project.activityBag.listReferences(SEQUENCE_OBJECT, hm);
              project.activityBag.listReferences(ACTIVITY_OBJECT, hm);
              project.activitySequence.listReferences(null, hm);
              java.util.Iterator it = hm.keySet().iterator();
              while (it.hasNext() && !isCancelled()) {
                String s = (String) it.next();
                if (SEQUENCE_OBJECT.equals(hm.get(s))) {
                  if (project.activitySequence.getElementByTag(s, false) == null) {
                    sequence = Clic3.pacNameToLowerCase(s);
                    break;
                  }
                } else if (!project.activityBag.activityExists(s)) {
                  activityName = s;
                  break;
                }
              }
            }

            if (fullPath == null && sequence == null && activityName == null) {
              break;
            } else {
              if (!prompted) {
                System.out.println("FullPath: "+fullPath+" - sequence: "+sequence+" - activityName: "+activityName);
                prompted = (messages.showQuestionDlg(progressDialog, "msg_prompt_loadReferences", null,
                    "yn") == Messages.YES);
                if (!prompted) {
                  break;
                }
              }
              abe = null;
            }
          }
          if (project != null && !isCancelled()) {
            project.mediaBag.waitForAllImages();
          }
        } catch (Exception ex) {
          exception = ex;
          System.err.println("Exception:" + ex);
        }

        attachProject();
        if (imported && projectEditor != null) {
          projectEditor.setModified(true);
        }
        return abe;
      }

      @Override
      public void finished() {

        progressDialog.setVisible(false);

        setCursor(null);

        if (worker == null || exception != null) {
          List<Object> v = new ArrayList<Object>();
          if (fullPath != null) {
            v.add(fullPath);
          }
          if (sequence != null) {
            v.add(sequence);
          }
          if (activityName != null) {
            v.add(activityName);
          }

          messages.showErrorWarning(thisAuthor, "err_reading_data", v, exception, null);
        }
        // unlock events
        worker = null;
        setEnabled(true);
      }
    };

    // finalization process
    setCursor(null);
    // lock events
    setEnabled(false);

    // Done by progressDialog!
    progressDialog.start("WORKING", "msg_loading_project", worker, true, true, false);
  }

  protected void setProject(JClicProject p) {
    if (project != null) {
      project.end();
    }
    project = p;
    setFrameTitle(null);
    checkActions();
  }

  protected void setFrameTitle(String s) {
    java.awt.Frame fr = JOptionPane.getFrameForComponent(AuthorSingleFrame.this);
    if (fr != null) {
      StringBuilder sb = new StringBuilder("JClic author");
      if (s != null && s.length() > 0) {
        sb.append(" - ").append(s);
      } else if (project != null) {
        sb.append(" - ").append(project.getName());
      }
      fr.setTitle(sb.substring(0));
    }
  }

  protected void checkActions() {
    if (projectActions != null) {
      for (int i = 0; i < projectActions.length; i++) {
        projectActions[i].setEnabled(project != null);
      }
    }
  }

  protected FileSystem createFileSystem() {
    return settings.fileSystem;
  }

  protected void createMenu() {

    Editor.createBasicActions(options);
    MediaBagEditor.createActions(options);
    ActivityBagEditor.createBasicActions(options);
    ActivitySequenceEditor.createActions(options);

    menuBar = new JMenuBar();

    fileMenu = new JMenu(messages.get("m_File"));
    fileMenu.setMnemonic(messages.get("m_File_Mnemonic").charAt(0));
    fileMenu.add(new KJMenuItem(getAction(ACTION_NEW_PROJECT)));
    fileMenu.addSeparator();
    fileMenu.add(new KJMenuItem(getAction(ACTION_OPEN_FILE)));
    fileMenu.add(new KJMenuItem(getAction(ACTION_OPEN_URL)));
    fileMenu.addSeparator();
    fileMenu.add(new KJMenuItem(getAction(ACTION_SAVE_FILE)));
    fileMenu.add(new KJMenuItem(getAction(ACTION_SAVE_FILE_AS)));
    fileMenu.addSeparator();
    fileMenu.add(new KJMenuItem(getAction(ACTION_EXPORT_HTML5)));
    menuBar.add(fileMenu);

    editMenu = new JMenu(messages.get("m_Edit"));
    editMenu.setMnemonic(messages.get("m_Edit_Mnemonic").charAt(0));
    editMenu.add(new KJMenuItem(Editor.cutAction));
    editMenu.add(new KJMenuItem(Editor.copyAction));
    editMenu.add(new KJMenuItem(Editor.pasteAction));
    editMenu.add(new KJMenuItem(Editor.deleteAction));
    editMenu.addSeparator();
    editMenu.add(ActivityBagEditor.copyActivityAttributesAction);
    editMenu.addSeparator();
    editMenu.add(new KJMenuItem(Editor.moveUpAction));
    editMenu.add(new KJMenuItem(Editor.moveDownAction));
    menuBar.add(editMenu);

    insertMenu = new JMenu(messages.get("m_Insert"));
    insertMenu.setMnemonic(messages.get("m_Insert_Mnemonic").charAt(0));
    insertMenu.add(MediaBagEditor.newMediaBagElementAction);
    insertMenu.add(ActivityBagEditor.newActivityBagElementAction);
    insertMenu.add(ActivitySequenceEditor.newActivitySequenceElementAction);
    insertMenu.addSeparator();
    insertMenu.add(new KJMenuItem(getAction(ACTION_IMPORT_ACTIVITIES)));
    menuBar.add(insertMenu);

    toolsMenu = new JMenu(messages.get("m_Tools"));
    toolsMenu.setMnemonic(messages.get("m_Tools_Mnemonic").charAt(0));
    toolsMenu.add(new KJMenuItem(getAction(ACTION_SETTINGS)));
    toolsMenu.addSeparator();
    toolsMenu.add(new KJMenuItem(getAction(ACTION_DOCTREE)));
    toolsMenu.addSeparator();
    toolsMenu.add(new KJMenuItem(getAction(ACTION_CREATE_INSTALLER)));
    menuBar.add(toolsMenu);

    viewMenu = new JMenu(messages.get("m_View"));
    viewMenu.setMnemonic(messages.get("m_View_Mnemonic").charAt(0));
    viewMenu.add(new KJMenuItem(getAction(ACTION_EDIT_PROJECT)));
    viewMenu.add(new KJMenuItem(getAction(ACTION_EDIT_MEDIA)));
    viewMenu.add(new KJMenuItem(getAction(ACTION_EDIT_ACTIVITIES)));
    viewMenu.add(new KJMenuItem(getAction(ACTION_EDIT_SEQ)));
    viewMenu.addSeparator();
    viewMenu.add(ActivityBagElementEditor.testActivityAction);
    viewMenu.add(MediaBagElementEditor.testMediaBagElementAction);
    menuBar.add(viewMenu);

    helpMenu = new JMenu(messages.get("m_Help"));
    helpMenu.setMnemonic(messages.get("m_Help_Mnemonic").charAt(0));
    helpMenu.add(new KJMenuItem(getAction(ACTION_ABOUT)));
    menuBar.add(helpMenu);
  }

  protected void postCreateMenu() {
    fileMenu.addSeparator();
    fileMenu.add(new KJMenuItem(getAction(ACTION_EXIT)));
    fileMenu.addSeparator();
    recentFilesMenu = fileMenu;
    recentFilesOffset = fileMenu.getItemCount();
    updateRecentFilesMenu();
  }

  protected void checkMenu(boolean recreate) {
    JRootPane rp = getRootPane();
    if (rp == null) {
      return;
    }

    if (recreate || rp.getJMenuBar() == null) {
      if (recreate || menuBar == null) {
        createMenu();
        postCreateMenu();
      }
      rp.setJMenuBar(menuBar);
      rp.revalidate();
    }
  }

  protected void updateRecentFilesMenu() {
    if (recentFilesMenu != null && recentFilesOffset >= 0) {
      JMenuItem jmi;
      int itemsToRemove = recentFilesMenu.getItemCount() - recentFilesOffset;
      for (int i = 0; i < itemsToRemove; i++) {
        recentFilesMenu.remove(recentFilesOffset);
      }
      for (int i = 0; i < PlayerSettings.MAX_RECENT; i++) {
        if (settings.recentFiles[i] != null) {
          String s = settings.recentFiles[i];
          int k = s.lastIndexOf('\\');
          if (k < 0) {
            k = s.lastIndexOf('/');
          }
          if (k >= 0) {
            s = s.substring(k + 1);
          }
          createMenuItem(recentFilesMenu, Integer.toString(i + 1) + ". " + s, "recent" + i, true,
              KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, ActionEvent.ALT_MASK));
        }
      }
    }
  }

  JMenuItem createMenuItem(JComponent parent, String text, String actionCommand, boolean mnemonic,
      KeyStroke accelerator) {
    JMenuItem jmi = new JMenuItem(text);
    if (actionCommand != null) {
      jmi.setActionCommand(actionCommand);
      jmi.addActionListener(this);
    }
    if (mnemonic) {
      jmi.setMnemonic(jmi.getText().charAt(0));
    }
    if (accelerator != null) {
      jmi.setAccelerator(accelerator);
    }
    parent.add(jmi);
    return jmi;
  }

  public static final int ACTION_OPEN_FILE = 0, ACTION_OPEN_URL = 1, ACTION_SAVE_FILE = 2, ACTION_SAVE_FILE_AS = 3,
      ACTION_EXIT = 4, ACTION_SETTINGS = 5, ACTION_DOCTREE = 6, ACTION_ABOUT = 7, ACTION_NEW_PROJECT = 8,
      ACTION_EDIT_PROJECT = 9, ACTION_EDIT_MEDIA = 10, ACTION_EDIT_ACTIVITIES = 11, ACTION_EDIT_SEQ = 12,
      ACTION_CREATE_INSTALLER = 13, ACTION_IMPORT_ACTIVITIES = 14, ACTION_EXPORT_HTML5 = 15, AUTHOR_NUM_ACTIONS = 16;

  public static final String[] ACTION_NAMES = { "openFile", "openUrl", "saveFile", "saveFileAs", "exit", "settings",
      "docTree", "helpAbout", "newProject", "editProject", "editMedia", "editActivities", "editSeq", "createInstaller",
      "importActivities", "exportProject" };
  public static final String[] ACTION_ICONS = { "icons/file_open.gif", "icons/world.gif", "icons/file_save.gif",
      "icons/file_save_as.gif", "icons/exit_small.gif", "icons/settings.gif", "icons/tree.gif", "icons/help.gif",
      "icons/project_new.gif", "icons/project_settings.gif", "icons/media_bag.gif", "icons/miniclic.png",
      "icons/sequence.gif", "icons/installer.gif", "icons/import_act.png", "icons/html_doc.gif" };

  protected int getNumActions() {
    return AUTHOR_NUM_ACTIONS;
  }

  protected void buildActions() {

    actions = new Action[getNumActions()];

    actions[ACTION_OPEN_FILE] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (checkSaveChanges(false)) {
          int[] filters = { Utils.ALL_CLIC_FF, Utils.INSTALL_FF, Utils.ALL_JCLIC_SCORM_FF };
          FileSystem fs = settings.fileSystem;
          String result = fs.chooseFile(null, false, filters, options, null, AuthorSingleFrame.this, false);
          if (result != null) {
            String fileName = fs.getFullFileNamePath(result);
            if (load(fileName, null)) {
              addRecentFile(fileName);
            }
          }
        }
      }
    };

    actions[ACTION_OPEN_URL] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (checkSaveChanges(false)) {
          String url = messages.showInputDlg(AuthorSingleFrame.this, "URL_OPEN", "URL", "http://", "URL_OPEN", false);
          if (url != null) {
            url = url.trim();
            if (url.startsWith("http://http://")) {
              url = url.substring(7);
            }
            if (url.length() > 0 && !url.equals("http://")) {
              if (load(url, null)) {
                addRecentFile(url);
              }
            }
          }
        }
      }
    };

    actions[ACTION_SAVE_FILE] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        saveFile(false);
      }
    };

    actions[ACTION_SAVE_FILE_AS] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        saveFile(true);
      }
    };

    actions[ACTION_EXIT] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        exit();
      }
    };

    actions[ACTION_SETTINGS] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        String currentLook = options.getString(LFUtil.LOOK_AND_FEEL);
        String currentLanguage = options.getString(Messages.LANGUAGE);
        String currentCountry = options.getString(Messages.COUNTRY);
        String currentVariant = options.getString(Messages.VARIANT);
        String currentMediaSystem = settings.mediaSystem;
        if (settings.edit(AuthorSingleFrame.this)) {
          settings.save();
          options.syncProperties(settings.getProperties(), false);
          boolean recreateMenu = false;
          if (!settings.lookAndFeel.equals(currentLook)) {
            options.setLookAndFeel();
            recreateMenu = true;
          }
          if (settings.language != null && (!StrUtils.compareObjects(settings.language, currentLanguage)
              || !StrUtils.compareObjects(settings.country, currentCountry)
              || !StrUtils.compareObjects(settings.variant, currentVariant))) {
            setMessages();
            recreateMenu = true;
          }
          if (recreateMenu) {
            checkMenu(true);
          }
          if (!currentMediaSystem.equals(settings.mediaSystem)) {
            options.put(MEDIA_SYSTEM, settings.mediaSystem);
            edu.xtec.jclic.media.CheckMediaSystem.check(options, false);
          }
        }
      }
    };

    actions[ACTION_DOCTREE] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (project != null) {
          org.jdom.Element e = JDomTreePanel.editElement(AuthorSingleFrame.this, project.getJDomElement(),
              AuthorSingleFrame.this, project.getFileSystem(), project.getName(), "edit_docTree_title");
          if (e != null) {
            try {
              String fullPath = project.getFullPath();
              JClicProject prj = JClicProject.getJClicProject(e, AuthorSingleFrame.this,
                  FileSystem.createFileSystem(fullPath, AuthorSingleFrame.this), fullPath);
              if (prj != null) {
                setProject(prj);
                attachProject();
                projectEditor.setModified(true);
              }
            } catch (Exception ex) {
              messages.showErrorWarning(AuthorSingleFrame.this, "edit_tree_badFormat", ex);
            }
          }
        }
      }
    };

    actions[ACTION_IMPORT_ACTIVITIES] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (project != null) {
          int[] filters = { Utils.ALL_JCLIC_FF };
          FileSystem fs = settings.fileSystem;
          String result = fs.chooseFile(null, false, filters, options, "import_selectProject", AuthorSingleFrame.this,
              false);

          if (result != null) {
            String fullPath = fs.getFullFileNamePath(result);
            if ((new File(project.getFileSystem().getFullRoot())).equals(new File(fullPath))) {
              messages.showAlert(AuthorSingleFrame.this, "import_warn_samefile");
            } else {
              doImportActivities(fullPath);
            }
          }
        }
      }
    };

    actions[ACTION_ABOUT] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        AboutWindow aw = new AboutWindow(AuthorSingleFrame.this, AuthorSingleFrame.this, new Dimension(500, 400));
        try {
          aw.buildAboutTab("JClic author", getMsg("AUTHOR_VERSION"), "logo_author_small.png", null, null, null, null);
          aw.buildStandardTab(aw.getHtmlSystemInfo(), "about_window_systemInfo", "about_window_lb_system",
              "icons/system_small.gif");
          aw.setVisible(true);
        } catch (Exception ex) {
          System.err.println("Error building about window: " + ex);
        }
      }
    };

    actions[ACTION_NEW_PROJECT] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (checkSaveChanges(false)) {
          JClicProject prj = NewProjectDlg.prompt(AuthorSingleFrame.this, AuthorSingleFrame.this, createFileSystem());
          if (prj != null) {
            setProject(prj);
            attachProject();
          }
        }
      }
    };

    actions[ACTION_EXPORT_HTML5] = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        if (checkSaveChanges(project.isScorm)) {
          String inputPath = project.getFileSystem().getFullFileNamePath("");
          String[] folders = ExportToJSDlg.prompt(AuthorSingleFrame.this, AuthorSingleFrame.this, inputPath,
              AuthorSingleFrame.this.settings.rootExportPath, AuthorSingleFrame.this.settings.rootScormPath);
          if (folders != null) {
            try {
              boolean exportAll = folders[2].equals("true");
              String scormFile = folders[3];
              if (!exportAll) {
                inputPath = project.getFileSystem().getFullRoot();
              }
              String fullPath = (new File(project.getFullPath())).getCanonicalPath();
              String mainFile = project.getFileSystem().getRelativeFileNamePath(fullPath);
              if (mainFile.endsWith(".zip")) {
                mainFile = mainFile.substring(0, mainFile.length() - 4);
              }
              ExportTaskDlg.doTask(AuthorSingleFrame.this, settings, AuthorSingleFrame.this, inputPath, folders[0],
                  mainFile, project, exportAll, scormFile);
            } catch (IOException ex) {
              System.err.println("Error: " + ex);
            }
          }
        }
      }
    };

    actions[ACTION_EDIT_PROJECT] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (tabbedPane != null) {
          tabbedPane.setSelectedComponent(projectSettingsEditorPanel);
        }
      }
    };

    actions[ACTION_EDIT_MEDIA] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (tabbedPane != null) {
          tabbedPane.setSelectedComponent(mediaBagEditorPanel);
        }
      }
    };

    actions[ACTION_EDIT_ACTIVITIES] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (tabbedPane != null) {
          tabbedPane.setSelectedComponent(activityBagEditorPanel);
        }
      }
    };

    actions[ACTION_EDIT_SEQ] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (tabbedPane != null) {
          tabbedPane.setSelectedComponent(activitySequenceEditorPanel);
        }
      }
    };

    /*
     * actions[ACTION_CREATE_HTML] = new AbstractAction() { public void
     * actionPerformed(ActionEvent ev) { if (checkSaveChanges(project.isScorm)) {
     * AppletHtmlCreator.createHtml(project, settings, AuthorSingleFrame.this); } }
     * };
     */

    actions[ACTION_CREATE_INSTALLER] = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        if (checkSaveChanges(project.isScorm)) {
          ProjectInstallerEditPanel.createInstaller(project, AuthorSingleFrame.this);
        }
      }
    };

    projectActions = new Action[] { actions[ACTION_SAVE_FILE], actions[ACTION_SAVE_FILE_AS], actions[ACTION_DOCTREE],
        actions[ACTION_EDIT_SEQ], actions[ACTION_EDIT_ACTIVITIES], actions[ACTION_EDIT_MEDIA],
        actions[ACTION_EDIT_PROJECT], actions[ACTION_CREATE_INSTALLER], actions[ACTION_IMPORT_ACTIVITIES],
        actions[ACTION_EXPORT_HTML5] };

    checkActions();
  }

  protected boolean saveFile(boolean saveAs) {
    boolean ok = false;
    if (project != null && projectEditor.checkProject(options, this, true)) {
      int[] filters = { Utils.JCLIC_ZIP_FF };
      FileSystem fs = project.getFileSystem();
      String path = StrUtils.secureString(project.getFullPath(), project.getName() + Utils.EXT_JCLIC_ZIP);
      ;
      if (project.isScorm) {
        if (messages.showQuestionDlg(this, "warn_save_scorm_file", "CONFIRM", "yn") != Messages.YES)
          return false;
        saveAs = true;
        path = settings.fileSystem
            .getFullFileNamePath(project.getName() + File.separator + project.getName() + Utils.EXT_JCLIC_ZIP);
      }
      String pLower = path.toLowerCase();
      if (!pLower.endsWith(Utils.EXT_JCLIC_ZIP)) {
        if (pLower.endsWith(".jclic")) {
          path = path + ".zip";
        } else if (pLower.endsWith(".pac") || pLower.endsWith(".pcc")) {
          int dot = path.lastIndexOf('.');
          if (dot >= 1) {
            path = path.substring(0, dot);
          }
          path = path + Utils.EXT_JCLIC_ZIP;
        }
      }

      String result;
      File f = new File(path);
      if (project.isScorm) {
        // Create folder if not exists
        try {
          f.getParentFile().mkdir();
        } catch (Exception ex) {
          messages.showErrorWarning(this, "err_unable_to_create_folder", ex);
          return false;
        }
      }
      if (!saveAs && f.exists() && f.canWrite()) {
        result = path;
      } else {
        result = fs.chooseFile(path, true, filters, options, null, AuthorSingleFrame.this, false);
      }
      if (result != null) {
        ok = doSaveFile(result);
        setFrameTitle(null);
      }
    }
    return ok;
  }

  static boolean saveResult;

  protected boolean doSaveFile(final String fName) {

    if (worker != null) {
      return false;
    }

    saveResult = false;

    worker = new edu.xtec.util.SwingWorker() {
      Exception exception = null;
      AuthorSingleFrame thisAuthor = AuthorSingleFrame.this;

      @Override
      public Object construct() {
        Object o = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
          projectEditor.saveProject(fName);
          saveResult = true;
          project.isScorm = false;
          addRecentFile(project.getFullPath());
          o = true;
        } catch (Exception ex) {
          exception = ex;
        }
        return o;
      }

      @Override
      public void finished() {
        progressDialog.setVisible(false);
        setCursor(null);
        if (worker == null || exception != null) {
          messages.showErrorWarning(thisAuthor, "FILE_ERR_SAVING", fName, exception, null);
        }
        // unlock events
        worker = null;
        setEnabled(true);
      }
    };

    setCursor(null);
    setEnabled(false);
    progressDialog.start("WORKING", "msg_saving_project", worker, true, false, true);
    return saveResult;
  }

  protected void doImportActivities(final String fullPath) {

    if (worker != null) {
      return;
    }

    System.gc();

    worker = new edu.xtec.util.SwingWorker() {
      Exception exception = null;
      AuthorSingleFrame thisAuthor = AuthorSingleFrame.this;

      @Override
      public Object construct() {
        Object o = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
          JClicProject jcp = JClicProject.getJClicProject(thisAuthor, fullPath, progressDialog);
          if (jcp != null) {
            progressDialog.setText(messages.get("import_importing"));

            // Select activities to import
            JClicProjectEditor jcped = (JClicProjectEditor) jcp.getEditor(null);
            javax.swing.JList<Object> actList = new javax.swing.JList<Object>(jcp.activityBag.getElements());
            javax.swing.JScrollPane pane = new javax.swing.JScrollPane(actList);
            pane.setPreferredSize(new java.awt.Dimension(300, 300));
            if (messages.showInputDlg(thisAuthor, new String[] { "import_selectActivities" }, new String[] {},
                new JComponent[] { pane }, "action_importActivities_caption")
                && actList.getSelectedIndices().length > 0) {

              progressDialog.setText(messages.get("import_checkdep"));
              Object[] selection = actList.getSelectedValues();

              // check media elements to import
              List<String> mediaNames = new ArrayList<String>();
              for (int i = 0; i < selection.length; i++) {
                ActivityBagElement abe = (ActivityBagElement) selection[i];
                Map map = abe.getReferences();
                Iterator it = map.keySet().iterator();
                while (it.hasNext()) {
                  String media = (String) it.next();
                  Object mediaType = map.get(media);
                  if (Constants.MEDIA_OBJECT.equals(mediaType) && !mediaNames.contains(media)) {
                    mediaNames.add(media);
                  }

                  // Todo: process also objects of type Constants.EXTERNAL_OBJECT
                }
              }

              // get the list of media bag elements
              ArrayList<MediaBagElement> mediaBagElements = new ArrayList<MediaBagElement>();
              for (int i = 0; i < mediaNames.size(); i++) {
                MediaBagElement mb = jcp.mediaBag.getElement((String) mediaNames.get(i));
                if (mb != null) {
                  mediaBagElements.add(mb);
                }
              }

              // check for repeated media names and import media
              for (MediaBagElement mbe : mediaBagElements) {
                String name = mbe.getName();
                String rootName = name;
                progressDialog.setText(messages.get("import_impmedia") + " " + name);
                int prefix = 0;
                while (project.mediaBag.getElement(name) != null) {
                  name = "i0" + Integer.toString(++prefix) + "-" + rootName;
                }
                String fName = mbe.getFileName();
                String rootFName = fName;
                prefix = 0;
                while (project.getFileSystem().fileExists(fName)) {
                  fName = "i0" + Integer.toString(++prefix) + "-" + rootFName;
                }
                File outFile = new File(project.getFileSystem().getFullFileNamePath(fName));
                outFile.getParentFile().mkdirs();
                java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
                edu.xtec.util.StreamIO.writeStreamTo(jcp.mediaBag.getInputStream(mbe.getName()), fos);

                if (!name.equals(rootName)) {
                  jcped.getActivityBagEditor().nameChanged(Constants.T_MEDIA, rootName, name);
                  mbe.setName(name);
                }

                if (!fName.equals(rootFName)) {
                  mbe.setFileName(fName);
                }

                mbe.setData(null);
                projectEditor.getMediaBagEditor().addMediaBagElement(mbe);
              }

              // Import activities
              for (int i = 0; i < selection.length; i++) {
                ActivityBagElement abe = (ActivityBagElement) selection[i];
                String actName = abe.toString();
                String rootName = actName;
                progressDialog.setText(messages.get("import_impact") + " " + actName);
                int prefix = 0;
                while (project.activityBag.getElementByName(actName) != null) {
                  actName = "i0" + Integer.toString(++prefix) + rootName;
                }
                if (!actName.equals(rootName)) {
                  jcped.nameChanged(Constants.T_ACTIVITY, rootName, actName);
                }

                ActivityBagElementEditor abeed = (ActivityBagElementEditor) abe.getEditor(null);
                projectEditor.getActivityBagEditor().insertEditor(abeed, true, -1, false);
              }

              o = true;
            }
          }

        } catch (Exception ex) {
          exception = ex;
        }
        return o;
      }

      @Override
      public void finished() {
        progressDialog.setVisible(false);
        setCursor(null);
        if (worker == null || exception != null) {
          messages.showErrorWarning(thisAuthor, "FILE_ERR_OPENING", fullPath, exception, null);
        }
        // unlock events
        worker = null;
        setEnabled(true);
      }
    };

    setCursor(null);
    setEnabled(false);
    progressDialog.start("WORKING", "import_importing", worker, true, false, true);
  }

  protected void setActionsText() {
    if (actions != null) {
      for (int i = 0; i < actions.length; i++) {
        if (actions[i] != null) {
          String s = messages.get("action_" + getActionName(i) + "_caption");
          if (!s.equals(actions[i].getValue(Action.NAME))) {
            actions[i].putValue(Action.NAME, s);
          }
          s = messages.get("action_" + getActionName(i) + "_tooltip");
          if (!s.equals(actions[i].getValue(Action.SHORT_DESCRIPTION))) {
            actions[i].putValue(Action.SHORT_DESCRIPTION, s);
          }
          s = messages.get("action_" + getActionName(i) + "_keys");
          if (s != null && s.length() == 2) {
            actions[i].putValue(Action.MNEMONIC_KEY, new Integer(s.charAt(0)));
            if (s.charAt(1) != '*') {
              actions[i].putValue(Action.ACCELERATOR_KEY,
                  KeyStroke.getKeyStroke((int) s.charAt(1), KeyEvent.CTRL_MASK));
            }
          }
          Icon icon = getActionIcon(i);
          if (icon != null && !icon.equals(actions[i].getValue(Action.SMALL_ICON))) {
            actions[i].putValue(Action.SMALL_ICON, icon);
          }
        }
      }
    }
  }

  protected String getActionName(int actionId) {
    if (actionId < 0 || actionId >= ACTION_NAMES.length) {
      return null;
    }
    return ACTION_NAMES[actionId];
  }

  protected Icon getActionIcon(int actionId) {
    if (actionId < 0 || actionId >= ACTION_ICONS.length) {
      return null;
    }
    return ResourceManager.getImageIcon(ACTION_ICONS[actionId]);
  }

  public Action getAction(int id) {
    if (actions == null || id < 0 || id >= actions.length) {
      return null;
    }
    return actions[id];
  }

  protected boolean processActionEvent(String ac) {
    if (ac.startsWith("recent") && ac.length() > 6) {
      try {
        int i = Integer.parseInt(ac.substring(6));
        if (i >= 0 && i < PlayerSettings.MAX_RECENT && settings.recentFiles[i] != null && checkSaveChanges(false)) {
          load(settings.recentFiles[i], null);
        }
      } catch (Exception ex) {
        System.err.println("invalid command: " + ac);
      }
    } else {
      return !isEnabled();
    }

    return true;
  }

  protected void addRecentFile(String fName) {
    settings.addRecentFile(fName);
    updateRecentFilesMenu();
    settings.save();
  }

  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    String cmd = actionEvent.getActionCommand();
    if (cmd != null) {
      processActionEvent(cmd);
    }
  }

  public void exit() {
    if (checkSaveChanges(false)) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (options.getApplet() == null) {
            try {
              end();
              java.awt.Frame fr = JOptionPane.getFrameForComponent(AuthorSingleFrame.this);
              if (fr != null) {
                fr.dispose();
              } else {
                System.exit(0);
              }
            } catch (Exception ex) {
              System.err.println("Unable to exit!\n" + ex);
            }
          }
        }
      });
    }
  }

  protected void attachProject() {
    lastFocusedPanel = null;
    if (projectSettingsEditorPanel != null && mediaBagEditorPanel != null && activityBagEditorPanel != null
        && activitySequenceEditorPanel != null) {
      if (project != null) {
        project.activityBag.sortByName();
      }
      projectEditor = project == null ? null : (JClicProjectEditor) project.getEditor(null);
      projectSettingsEditorPanel.attachEditor(projectEditor == null ? null : projectEditor.getProjectSettingsEditor(),
          true);
      mediaBagEditorPanel.attachEditor(projectEditor == null ? null : projectEditor.getMediaBagEditor(), true);
      activityBagEditorPanel.attachEditor(projectEditor == null ? null : projectEditor.getActivityBagEditor(), true);
      activitySequenceEditorPanel.attachEditor(projectEditor == null ? null : projectEditor.getActivitySequenceEditor(),
          true);
      if (project != null) {
        player.setProject(project);
        player.getHistory().clearHistory();
      }
      // 22-Jan-2013: JClicAuthor hangs when an I/O error occurs
      if (projectEditor != null) {
        projectEditor.setTestPlayerContainer(this);
      }
    }
    actions[ACTION_DOCTREE].setEnabled(project != null);
  }

  private EditorPanel lastFocusedPanel;

  protected void checkTabbedPaneFocus(boolean focusLost) {
    if (tabbedPane != null) {
      Object o = tabbedPane.getSelectedComponent();
      if (!focusLost && (o instanceof EditorPanel)) {
        if (lastFocusedPanel != null) {
          lastFocusedPanel.focusLost(null);
        }
        lastFocusedPanel = (EditorPanel) o;
        lastFocusedPanel.focusGained(null);
      } else {
        Editor.clearBasicActionsOwner();
      }
    }
  }

  protected java.awt.event.FocusListener focusListener = new java.awt.event.FocusListener() {
    public void focusGained(java.awt.event.FocusEvent ev) {
      if (playerDlg != null && playerDlg.isShowing()) {
        playerDlg.requestFocus();
      } else {
        checkTabbedPaneFocus(false);
      }
    }

    public void focusLost(java.awt.event.FocusEvent ev) {
      checkTabbedPaneFocus(true);
    }
  };

  protected void createFrames() {

    tabbedPane = new JTabbedPane();

    tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent ev) {
        checkTabbedPaneFocus(false);
      }
    });

    JClicProject newProject = new JClicProject(this, new FileSystem(this), "");

    projectEditor = (JClicProjectEditor) newProject.getEditor(null);

    ProjectSettingsEditor psed = projectEditor.getProjectSettingsEditor();
    projectSettingsEditorPanel = psed.createEditorPanel(options);
    tabbedPane.addTab(messages.get("edit_project"), JClicProjectEditor.getIcon(), projectSettingsEditorPanel,
        messages.get("edit_project_tooltip"));

    mediaBagEditorPanel = new MediaBagMultiEditorPanel(options);
    tabbedPane.addTab(messages.get("edit_media"), MediaBagEditor.getIcon(), mediaBagEditorPanel,
        messages.get("edit_media_tooltip"));

    ActivityBagEditor abe = projectEditor.getActivityBagEditor();
    activityBagEditorPanel = abe.createEditorPanel(options);
    tabbedPane.addTab(messages.get("edit_activities"), ActivityEditor.getIcon(), activityBagEditorPanel,
        messages.get("edit_activities_tooltip"));

    activitySequenceEditorPanel = projectEditor.getActivitySequenceEditor().createEditorPanel(options);
    tabbedPane.addTab(messages.get("edit_sequences"), ActivitySequenceEditor.getIcon(), activitySequenceEditorPanel,
        messages.get("edit_sequences_tooltip"));

    playerDlg = new JDialog(JOptionPane.getFrameForComponent(this), "test player", true);
    player = new Player(options, newProject);
    player.getHistory().setTestMode(true);
    player.appName = "JClic test player";
    player.addTo(playerDlg, null);
    playerDlg.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        player.closeHelpWindow();
        player.removeActivity();
      }
    });
    playerDlg.pack();

    add(tabbedPane, BorderLayout.CENTER);
  }

  public Player getTestPlayer() {
    return player;
  }

  public void test() {
    if (playerDlg != null && player != null) {
      player.initReporter();
      if (player.reporter != null && project != null) {
        player.reporter.newSession(project, player, messages);
      }
      playerDlg.setVisible(true);
    }
  }

  public boolean newInstanceRequest(final String param1, final String param2) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Frame frame = JOptionPane.getFrameForComponent(AuthorSingleFrame.this);
        if (frame != null) {
          frame.toFront();
        }
        messages.showAlert(AuthorSingleFrame.this, new String[] { "new instance request", param1, param2 });
      }
    });
    return true;
  }

  protected boolean checkSaveChanges(boolean force) {
    boolean result = true;
    if (projectEditor != null) {
      projectEditor.collectData();
      if (force || projectEditor.isModified()) {
        switch (messages.showQuestionDlg(this, "warn_project_modified", "CONFIRM", "ync")) {
        case Messages.YES:
          result = saveFile(false);
          break;
        case Messages.CANCEL:
          result = false;
          break;
        case Messages.NO:
          result = !force;
          break;
        }
      }
    }
    return result;
  }

  public boolean windowCloseRequested() {
    return checkSaveChanges(false);
  }

  public void displayUrl(String url, boolean inFrame) {
    try {
      edu.xtec.util.BrowserLauncher.openURL(url);
    } catch (Exception ex) {
      System.err.println("Unable to invoque URL " + url + "\n" + ex);
    }
  }

  public void editActivity(String activityName) {
    if (tabbedPane != null && activityBagEditorPanel != null) {
      if (((ActivityBagEditorPanel) activityBagEditorPanel).editActivity(activityName)) {
        tabbedPane.setSelectedComponent(activityBagEditorPanel);
      }
    }
  }
}
