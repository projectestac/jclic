/*
 * File    : ActiveBagContentEditor.java
 * Created : 09-oct-2002 17:10
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

package edu.xtec.jclic.boxes;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.ActivityEditorInternalPanel;
import edu.xtec.jclic.ActivityEditorPanel;
import edu.xtec.jclic.Constants;
import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.util.ResourceManager;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ActiveBagContentEditor extends ActivityEditorInternalPanel {

  JTabbedPane ctrlTab;
  ActiveBagContentPreviewPanel abcpp;
  ActiveBagContentControlPanel[] cp;
  TextGridContentControlPanel tgp;
  ActiveBagContentRelPanel rp;
  ActiveBagContentLayoutPanel lp;
  MediaBagEditor mediaBagEditor;
  boolean useRel = false;
  boolean useBoolRel = false;
  boolean crossWord = false;
  int relIndex = -1;

  public static final int SINGLE = 0, // puzzles and info
      SINGLE_ALT = 1, // memory
      SINGLE_BOOL_ALT = 2, // identify
      SINGLE_LAYOUT = 3, // double puzzle
      DOUBLE_ALT = 4, // simple ass.
      DOUBLE_REL = 5, // explore
      DOUBLE_REL_ALT = 6, // complex ass. written answ.
      TEXTGRID_ALT = 7, // wordsearch
      TEXTGRID_CRW = 8; // crossWord

  /** Creates new form ActiveBagContentEditor */
  public ActiveBagContentEditor(ActivityEditorPanel parent, int type) {
    super(parent);
    int altIndex = -1;
    int indexBis = -1;
    boolean showLayout = false;
    boolean useTextGrid = false;
    switch (type) {
    case SINGLE_BOOL_ALT:
      useBoolRel = true;
      altIndex = 1;
      break;
    case SINGLE_ALT:
      showLayout = true;
      altIndex = 1;
      break;
    case SINGLE_LAYOUT:
      showLayout = true;
    case SINGLE:
      break;
    case DOUBLE_REL_ALT:
      altIndex = 2;
    case DOUBLE_REL:
      useRel = true;
      indexBis = 1;
      showLayout = true;
      break;
    case DOUBLE_ALT:
      altIndex = 2;
      indexBis = 1;
      showLayout = true;
      break;
    case TEXTGRID_ALT:
      useTextGrid = true;
      indexBis = 0;
      showLayout = true;
      break;
    case TEXTGRID_CRW:
      useTextGrid = true;
      indexBis = 0;
      altIndex = 1;
      showLayout = true;
      crossWord = true;
      break;
    }

    initComponents();
    abcpp = (ActiveBagContentPreviewPanel) previewPanel;

    cp = new ActiveBagContentControlPanel[2];
    if (useTextGrid)
      tgp = new TextGridContentControlPanel(this, 0, !crossWord);
    else
      cp[0] = new ActiveBagContentControlPanel(this, 0, altIndex, 0, crossWord);

    if (indexBis >= 0) {
      cp[1] = new ActiveBagContentControlPanel(this, indexBis, crossWord ? 1 : -1, useTextGrid ? 1 : indexBis,
          crossWord);
    }

    if (cp[1] != null || useRel || useBoolRel || showLayout) {
      ctrlTab = new JTabbedPane();
      ctrlTab.addTab(options.getMsg("edit_act_grid" + (cp[1] == null ? "" : "_A")),
          ResourceManager.getImageIcon("icons/grid.gif"), getFirstComponent());
      if (cp[1] != null)
        ctrlTab.addTab(options.getMsg("edit_act_grid_B"), ResourceManager.getImageIcon("icons/grid.gif"), cp[1]);
      if (showLayout) {
        lp = new ActiveBagContentLayoutPanel(this);
        ctrlTab.addTab(options.getMsg("edit_act_grid_layout"), ResourceManager.getImageIcon("icons/grid_layout.gif"),
            lp);
      }
      if (useRel || useBoolRel) {
        relIndex = ctrlTab.getTabCount();
        rp = new ActiveBagContentRelPanel(this, useBoolRel);
        ctrlTab.addTab(options.getMsg("edit_act_grid_relationship"),
            ResourceManager.getImageIcon("icons/relationship.gif"), rp);
      }
      add(ctrlTab, BorderLayout.NORTH);
      ctrlTab.getModel().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          abcpp.setEditMode(ctrlTab.getSelectedIndex() == relIndex
              ? useBoolRel ? ActiveBagContentPreviewPanel.EDIT_BOOL : ActiveBagContentPreviewPanel.EDIT_LINKS
              : ActiveBagContentPreviewPanel.EDIT_GRIDS);
        }
      });
    } else
      add(getFirstComponent(), BorderLayout.NORTH);

    if (type == DOUBLE_ALT && cp[0] != null && cp[1] != null) {
      cp[0].setSyncpanel(cp[1]);
      cp[1].setSyncpanel(cp[0]);
    }
  }

  private JComponent getFirstComponent() {
    JComponent result = tgp;
    if (result == null)
      result = cp[0];
    return result;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  private void initComponents() { // GEN-BEGIN:initComponents
    previewScroll = new javax.swing.JScrollPane();
    previewPanel = new ActiveBagContentPreviewPanel(this, crossWord);

    setLayout(new java.awt.BorderLayout());

    previewPanel.setPreferredSize(new java.awt.Dimension(800, 600));
    previewScroll.setViewportView(previewPanel);

    add(previewScroll, java.awt.BorderLayout.CENTER);
  } // GEN-END:initComponents

  public void fillData() {
    saveBag = null;
    saveBagAlt = null;
    Activity act = getActivity();
    mediaBagEditor = act == null ? null : getActivityEditor().getMediaBagEditor();
    if (tgp != null)
      tgp.fillData(act);
    if (cp[0] != null)
      cp[0].fillData(act);
    if (cp[1] != null)
      cp[1].fillData(act);
    if (rp != null)
      rp.fillData(act);
    if (lp != null)
      lp.fillData(act);
    checkTabs();
    abcpp.setMediaBagEditor(mediaBagEditor);
    abcpp.setBoxGridPos(act == null ? Activity.AB : act.boxGridPos);
  }

  public void saveData() {
    Activity act = getActivity();
    if (act != null) {
      if (tgp != null)
        tgp.saveData(act);
      if (cp[0] != null)
        cp[0].saveData(act);
      if (cp[1] != null)
        cp[1].saveData(act);
      if (rp != null)
        rp.saveData(act);
      if (lp != null)
        lp.saveData(act);
    }
  }

  private ActiveBagContent saveBag, saveBagAlt;

  protected void enableGridB(boolean value) {
    Activity act = getActivity();
    if (cp[1] != null && act != null && act.abc != null) {
      int index = cp[1].index;
      if (value == false) {
        saveBag = cp[1].abc;
        saveBagAlt = cp[1].altAbc;
        act.abc[index] = null;
      } else {
        if (saveBag != null) {
          act.abc[index] = saveBag;
          if (cp[1].altIndex >= 0)
            act.abc[cp[1].altIndex] = saveBagAlt;
        } else {
          act.abc[index] = ActiveBagContent.initNew(2, 2, 'A');
        }
      }
      cp[1].setInitializing(true);
      cp[1].fillData(act);
      cp[1].setInitializing(false);
      setModified(true);
      checkTabs();
    }
  }

  protected void checkTabs() {
    Activity act = getActivity();
    if (act != null && cp[1] != null && lp != null && ctrlTab != null) {
      boolean visible = (cp[1].abc != null);
      if (!visible && ctrlTab.getTabCount() == 3) {
        ctrlTab.remove(lp);
        ctrlTab.remove(cp[1]);
      } else if (visible && ctrlTab.getTabCount() == 1) {
        ctrlTab.addTab(options.getMsg("edit_act_grid_B"), ResourceManager.getImageIcon("icons/grid.gif"), cp[1]);
        ctrlTab.addTab(options.getMsg("edit_act_grid_layout"), ResourceManager.getImageIcon("icons/grid_layout.gif"),
            lp);
      }
    }
  }

  /*
   * public boolean dataChanged(){ return false; }
   */

  protected javax.swing.Icon getIcon() {
    return edu.xtec.util.ResourceManager.getImageIcon("icons/panel.gif");
  }

  protected String getTitle() {
    return options.getMsg("edit_act_panels_panel");
  }

  protected String getTooltip() {
    return options.getMsg("edit_act_panels_panel_tooltip");
  }

  // only called by abcpp;
  protected void panelSelected(int panel) {
    if (ctrlTab != null && panel >= 0 && panel < ctrlTab.getTabCount())
      ctrlTab.setSelectedIndex(panel);
  }

  protected void resized(int panel) {
    if (panel >= 0 && (ctrlTab == null || panel < ctrlTab.getTabCount())) {
      if (panel == 0 && tgp != null)
        tgp.resized();
      else if (cp != null && panel < cp.length && cp[panel] != null)
        cp[panel].resized();
    }
  }

  public static boolean nameChanged(ActiveBagContent abc, int type, String oldName, String newName) {
    boolean result = false;

    if ((type & Constants.T_IMAGE) != 0 && oldName.equals(abc.imgName)) {
      abc.imgName = newName;
      result = true;
    }

    if (abc.backgroundContent != null)
      result |= ActiveBoxContentEditor.nameChanged(abc.backgroundContent, type, oldName, newName);

    for (int i = 0; i < abc.activeBoxContentArray.size(); i++)
      result |= ActiveBoxContentEditor.nameChanged(abc.getActiveBoxContent(i), type, oldName, newName);

    return result;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel previewPanel;
  private javax.swing.JScrollPane previewScroll;
  // End of variables declaration//GEN-END:variables

}
