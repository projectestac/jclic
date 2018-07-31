/*
 * File    : ActivityEditorPanel.java
 * Created : 10-jun-2002 10:15
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

package edu.xtec.jclic;

import edu.xtec.jclic.activities.text.TextActivityEditorTextPanel;
import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.jclic.boxes.ActiveBagContentEditor;
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.Options;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class ActivityEditorPanel extends EditorPanel {

  List<ActivityEditorInternalPanel> internalPanels;
  String currentActivityClass;
  Map<String, ActivityEditorInternalPanel> panelStore;
  public static final String SINGLE = "SINGLE", // puzzles and info
      SINGLE_ALT = "SINGLE_ALT", // memory
      SINGLE_BOOL_ALT = "SINGLE_BOOL_ALT", // identify
      SINGLE_LAYOUT = "SINGLE_LAYOUT", // double puzzle
      DOUBLE_ALT = "DOUBLE_ALT", // simple ass.
      DOUBLE_REL = "DOUBLE_REL", // explore
      DOUBLE_REL_ALT = "DOUBLE_REL_ALT", // complex ass. written answ.
      TEXTGRID_ALT = "TEXTGRID_ALT", // word search
      TEXTGRID_CRW = "TEXTGRID_CRW", // crossword
      TEXT_BASE = "TEXT_BASE";

  /** Creates new form MenuEditorPanel */
  public ActivityEditorPanel(Options options) {
    super(options);
    internalPanels = new ArrayList<ActivityEditorInternalPanel>(4);
    panelStore = new HashMap<String, ActivityEditorInternalPanel>();
    // ActivityBagElementEditor.createActions(options);
    initComponents();
    // addInternalPanel(new ActivityEditorDescPanel(options, this));
    postInit(250, false, false);
    setEnabled(false);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    Iterator<ActivityEditorInternalPanel> it = internalPanels.iterator();
    while (it.hasNext()) {
      it.next().setEnabled(enabled);
    }
  }

  public void addInternalPanel(ActivityEditorInternalPanel panel, String title, String tooltip) {
    if (title == null) title = panel.getTitle();
    if (tooltip == null) tooltip = panel.getTooltip();
    tabbedPane.addTab(title, panel.getIcon(), panel, tooltip);
    internalPanels.add(panel);
  }

  public void addInternalPanel(String storeKey, String title, String tooltip) {
    ActivityEditorInternalPanel panel = (ActivityEditorInternalPanel) panelStore.get(storeKey);
    if (panel == null) {
      if (SINGLE.equals(storeKey))
        panelStore.put(
            storeKey, (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.SINGLE)));
      else if (SINGLE_LAYOUT.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.SINGLE_LAYOUT)));
      else if (SINGLE_ALT.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.SINGLE_ALT)));
      else if (SINGLE_BOOL_ALT.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.SINGLE_BOOL_ALT)));
      else if (DOUBLE_ALT.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.DOUBLE_ALT)));
      else if (DOUBLE_REL.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.DOUBLE_REL)));
      else if (DOUBLE_REL_ALT.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.DOUBLE_REL_ALT)));
      else if (TEXTGRID_ALT.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.TEXTGRID_ALT)));
      else if (TEXTGRID_CRW.equals(storeKey))
        panelStore.put(
            storeKey,
            (panel = new ActiveBagContentEditor(this, ActiveBagContentEditor.TEXTGRID_CRW)));
      else if (TEXT_BASE.equals(storeKey))
        panelStore.put(storeKey, (panel = new TextActivityEditorTextPanel(this)));
    }
    if (panel != null) addInternalPanel(panel, title, tooltip);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  private void initComponents() { // GEN-BEGIN:initComponents
    mainPanel = new javax.swing.JPanel();
    tabbedPane = new javax.swing.JTabbedPane();

    setLayout(new java.awt.BorderLayout());

    setToolTipText(options.getMsg("edit_act_descriptionTab_tooltip"));
    mainPanel.setLayout(new java.awt.BorderLayout());

    mainPanel.add(tabbedPane, java.awt.BorderLayout.CENTER);

    add(mainPanel, java.awt.BorderLayout.CENTER);
  } // GEN-END:initComponents

  @Override
  protected synchronized void setEditor(Editor e) {
    // System.out.println("SET EDITOR");
    if (e instanceof ActivityEditor) {
      ActivityEditor ae = (ActivityEditor) e;

      MediaBagEditor mbe = ae.getMediaBagEditor();
      if (mbe != null && Utils.lowMemoryCondition()) mbe.getMediaBag().clearData();

      String s = ae.getActivity().getClass().getName();
      if (!s.equals(currentActivityClass)) {
        int cs = Math.max(tabbedPane.getSelectedIndex(), 0);
        // tabbedPane.setSelectedIndex(0);
        int c = tabbedPane.getTabCount();
        for (int i = c - 1; i >= ActivityEditor.COMMON_PANELS; i--) {
          // internalPanels.remove(i);
          // tabbedPane.remove(i);
          tabbedPane.remove((Component) internalPanels.remove(i));
        }

        ae.createPanels(this);
        final int csx = Math.min(cs, internalPanels.size() - 1);
        // tabbedPane.setSelectedIndex(csx);
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {
              public void run() {
                if (internalPanels.size() >= csx) tabbedPane.setSelectedIndex(csx);
              }
            });
      }
      currentActivityClass = s;
    }
    super.setEditor(e);
  }

  public boolean checkIfEditorValid(Editor e) {
    return e instanceof ActivityEditor;
  }

  protected ActivityEditor getActivityEditor() {
    return (ActivityEditor) editor;
  }

  protected Activity getActivity() {
    if (editor == null) return null;
    else return getActivityEditor().getActivity();
  }

  /*
  public void clear() {
      super.clear();
      Iterator it=internalPanels.iterator();
      while(it.hasNext())
          ((ActivityEditorInternalPanel)it.next()).clear();
  }
   */

  public void fillData() {
    Activity act = getActivity();
    if (act != null) {
      Iterator<ActivityEditorInternalPanel> it = internalPanels.iterator();
      while (it.hasNext()) it.next().fillData();
    }
  }

  public void saveData() {
    Activity act = getActivity();
    if (act != null) {
      Iterator<ActivityEditorInternalPanel> it = internalPanels.iterator();
      while (it.hasNext()) it.next().saveData();
      getActivityEditor().saveData();
    }
  }

  @Override
  protected javax.swing.Icon getIcon() {
    return ActivityEditor.getIcon();
  }

  @Override
  protected String getTitle() {
    return "activity";
    // return options.getMsg("menu_dlg_title");
  }

  @Override
  public void focusLost(java.awt.event.FocusEvent focusEvent) {
    if (editor != null) editor.collectData();
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel mainPanel;
  private javax.swing.JTabbedPane tabbedPane;
  // End of variables declaration//GEN-END:variables

}
