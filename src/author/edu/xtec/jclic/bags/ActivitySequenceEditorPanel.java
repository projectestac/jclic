/*
 * File    : ActivitySequenceEditorPanel.java
 * Created : 10-jun-2002 16:36
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

import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.edit.EditorPanel;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceManager;
import edu.xtec.util.SmallButton;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.FocusEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ActivitySequenceEditorPanel extends EditorPanel implements ListSelectionListener, ListDataListener {

  public static final int ICON_WIDTH = 20;

  public ActivitySequenceElementEditor currentItem;
  protected SeqTableModel tableModel;

  /** Creates new form ProjectLibraryEditorPanel */
  public ActivitySequenceEditorPanel(Options options) {
    super(options);
    tableModel = new SeqTableModel();
    ActivitySequenceEditor.createActions(options);
    ActivitySequenceElementEditor.createActions(options);
    initComponents();
    initTable();
    postInit(250, false, true);
    setEnabled(false);
  }

  private void initTable() {
    TableColumn column;
    for (int i = 2; i < 4; i++) {
      column = seqTable.getColumnModel().getColumn(i);
      column.setMaxWidth(ICON_WIDTH);
      column.setPreferredWidth(ICON_WIDTH);
      column.setWidth(ICON_WIDTH);
      column.setHeaderRenderer(headerIcoCellRenderer);
    }
    seqTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    seqTable.getSelectionModel().addListSelectionListener(ActivitySequenceEditorPanel.this);
  }

  private TableCellRenderer headerIcoCellRenderer = new TableCellRenderer() {
    JLabel lb;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      if (lb == null) {
        lb = new JLabel();
        Object o = UIManager.get("TableHeader.background");
        if (o instanceof Color)
          lb.setBackground((Color) o);
        o = UIManager.get("TableHeader.cellBorder");
        if (o instanceof Border)
          lb.setBorder((Border) o);
        o = UIManager.get("TableHeader.font");
        if (o instanceof Font)
          lb.setFont((Font) o);
        o = UIManager.get("TableHeader.foreground");
        if (o instanceof Color)
          lb.setForeground((Color) o);
      }
      if ("prev".equals(value)) {
        lb.setText("");
        lb.setIcon(ResourceManager.getImageIcon("icons/prev.gif"));
        lb.setToolTipText(options.getMsg("action_prev_tooltip"));
      } else if ("next".equals(value)) {
        lb.setText("");
        lb.setIcon(ResourceManager.getImageIcon("icons/next.gif"));
        lb.setToolTipText(options.getMsg("action_next_tooltip"));
      }
      return lb;
    }
  };

  protected void currentItemChanged() {
    if (!isInitializing()) {
      ActivitySequenceElementEditorPanel asep = (ActivitySequenceElementEditorPanel) edit;
      asep.attachEditor(currentItem, true);
    }
  }

  public void valueChanged(ListSelectionEvent ev) {

    if (ev.getValueIsAdjusting() || getActivitySequenceEditor() == null)
      return;

    ActivitySequenceElementEditorPanel asep = (ActivitySequenceElementEditorPanel) edit;

    int row = seqTable.getSelectionModel().getAnchorSelectionIndex();
    if (row >= 0 && row < getActivitySequenceEditor().getChildCount()) {
      currentItem = (ActivitySequenceElementEditor) getActivitySequenceEditor().getChildAt(row);
    } else
      currentItem = null;
    currentItemChanged();
  }

  public void contentsChanged(ListDataEvent e) {
    if (tableModel != null)
      tableModel.fireTableDataChanged();
  }

  public void intervalAdded(ListDataEvent e) {
    if (tableModel != null)
      tableModel.fireTableDataChanged();
  }

  public void intervalRemoved(ListDataEvent e) {
    if (tableModel != null)
      tableModel.fireTableDataChanged();
  }

  @Override
  public void focusGained(FocusEvent focusEvent) {
    ((ActivitySequenceElementEditorPanel) edit).focusGained(focusEvent);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  private void initComponents() { // GEN-BEGIN:initComponents
    javax.swing.JScrollPane scroll;

    split = new javax.swing.JSplitPane();
    scroll = new javax.swing.JScrollPane();
    seqTable = new ActivitySequenceTable(tableModel);
    edit = new ActivitySequenceElementEditorPanel(options);

    setLayout(new java.awt.BorderLayout());

    scroll.setMinimumSize(new java.awt.Dimension(300, 50));
    scroll.setViewportView(seqTable);

    split.setLeftComponent(scroll);

    split.setRightComponent(edit);

    add(split, java.awt.BorderLayout.CENTER);
  } // GEN-END:initComponents

  public boolean checkIfEditorValid(Editor e) {
    return e instanceof ActivitySequenceEditor;
  }

  protected ActivitySequence getActivitySequence() {
    if (editor == null)
      return null;
    else
      return ((ActivitySequenceEditor) editor).getActivitySequence();
  }

  /*
   * public void clear() { super.clear(); ActivitySequenceElementEditorPanel
   * asep=(ActivitySequenceElementEditorPanel)edit; asep.removeEditor(true);
   * ActivitySequenceEditor ased=getActivitySequenceEditor(); if(ased!=null)
   * ased.setListSelectionModel(null); }
   */

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    edit.setEnabled(enabled);
    seqTable.setEnabled(enabled);
  }

  public void fillData() {
    ActivitySequenceEditor ased = getActivitySequenceEditor();
    if (ased == null) {
      ActivitySequenceElementEditorPanel asep = (ActivitySequenceElementEditorPanel) edit;
      asep.removeEditor(true);
    } else {
      ased.setListSelectionModel(seqTable.getSelectionModel());
      if (ased.getChildCount() > 0) {
        seqTable.getSelectionModel().setSelectionInterval(0, 0);
        ActivitySequenceElementEditorPanel asep = (ActivitySequenceElementEditorPanel) edit;
        asep.attachEditor((Editor) ased.getChildAt(0), true);
      }
      tableModel.fireTableDataChanged();
    }
  }

  public void saveData() {
    ActivitySequence as = getActivitySequence();
  }

  /*
   * public boolean isModified(){ boolean result=super.isModified(); if(!result){
   * ActivitySequenceElementEditorPanel
   * asep=(ActivitySequenceElementEditorPanel)edit; result=asep.isModified(); }
   * return result; }
   */

  @Override
  protected Icon getIcon() {
    return ActivitySequenceEditor.getIcon();
  }

  @Override
  protected String getTitle() {
    return "Activity sequence";
  }

  public ActivitySequenceEditor getActivitySequenceEditor() {
    return (ActivitySequenceEditor) getEditor();
  }

  @Override
  protected void setEditor(Editor e) {
    if (getEditor() != null)
      getEditor().getListModel().removeListDataListener(this);
    super.setEditor(e);
    if (getEditor() != null)
      getEditor().getListModel().addListDataListener(this);
  }

  @Override
  protected void addActionsTo(Container cnt) {
    cnt.add(new SmallButton(ActivitySequenceEditor.newActivitySequenceElementAction));
    cnt.add(new SmallButton(ActivityBagElementEditor.testActivityAction));
    cnt.add(new SmallButton(Editor.moveUpAction));
    cnt.add(new SmallButton(Editor.moveDownAction));
    cnt.add(new SmallButton(Editor.copyAction));
    cnt.add(new SmallButton(Editor.cutAction));
    cnt.add(new SmallButton(Editor.pasteAction));
    cnt.add(new SmallButton(Editor.deleteAction));
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel edit;
  protected javax.swing.JTable seqTable;
  private javax.swing.JSplitPane split;
  // End of variables declaration//GEN-END:variables

  class SeqTableModel extends AbstractTableModel {

    public boolean drawDivider(int row) {
      boolean result = false;
      ActivitySequenceEditor ase = getActivitySequenceEditor();
      if (ase != null) {
        ActivitySequenceElementEditor ased1 = (ActivitySequenceElementEditor) ase.getChildAt(row);
        ActivitySequenceElementEditor ased2 = (row == ase.getChildCount() - 1) ? null
            : (ActivitySequenceElementEditor) ase.getChildAt(row + 1);
        result = (ased1 != null && ased1.getActivitySequenceElement().fwdJump != null)
            || (ased2 != null && ased2.getActivitySequenceElement().backJump != null);
      }
      return result;
    }

    public int getRowCount() {
      Editor e = getEditor();
      return e == null ? 0 : e.getChildCount();
    }

    public int getColumnCount() {
      return 4;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
      if (columnIndex == 2 || columnIndex == 3)
        return ImageIcon.class;
      else
        return java.lang.String.class;
    }

    public Object getValueAt(int row, int column) {
      Object result = null;
      ActivitySequenceEditor ase = getActivitySequenceEditor();
      if (ase != null) {
        ActivitySequenceElementEditor ased = (ActivitySequenceElementEditor) ase.getChildAt(row);
        if (ased != null) {
          switch (column) {
          case 0:
            result = ased.getTag();
            break;

          case 1:
            result = ased.getActivitySequenceElement().getActivityName();
            break;

          case 2:
          case 3:
            result = ActivitySequenceElementEditor.getElementIcon(ased.getActivitySequenceElement(), column == 3);
            break;
          /*
           * case 4: result=ActivitySequenceElementEditor.getElementJumpDescription(asel);
           * break;
           */
          }
        }
      }
      return result;
    }

    @Override
    public String getColumnName(int column) {
      String result = "";
      switch (column) {
      case 0:
        result = options.getMsg("edit_seq_tag_header");
        break;
      case 1:
        result = options.getMsg("edit_seq_activity_header");
        break;
      case 2:
        result = "prev";
        break;
      case 3:
        result = "next";
        break;
      }
      return result;
    }
  }
}
