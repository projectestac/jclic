/*
 * File    : ActivitySequenceTable.java
 * Created : 11-apr-2003 16:09
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Subclass of BasicTableUI
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ActivitySequenceTable extends JTable {

  public static final Color DIVIDER_COLOR = Color.red;
  public static final int DIVIDER_WIDTH = 1;

  private BasicStroke stroke;

  /** Creates a new instance of ActivitySequenceTable */
  public ActivitySequenceTable(TableModel model) {
    super(model);
    stroke = new BasicStroke((float) DIVIDER_WIDTH);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    ActivitySequenceEditorPanel.SeqTableModel model;
    if (getModel() instanceof ActivitySequenceEditorPanel.SeqTableModel)
      model = (ActivitySequenceEditorPanel.SeqTableModel) getModel();
    else return;

    if (getRowCount() <= 0 || getColumnCount() <= 0) {
      return;
    }

    Rectangle clip = g.getClipBounds();
    Point minLocation = clip.getLocation();
    Point maxLocation = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1);
    int rMin = rowAtPoint(minLocation);
    int rMax = rowAtPoint(maxLocation);
    // This should never happen.
    if (rMin == -1) {
      rMin = 0;
    }
    // If the table does not have enough rows to fill the view we'll get -1.
    // Replace this with the index of the last row.
    if (rMax == -1) {
      rMax = getRowCount() - 1;
    }
    int cMin = columnAtPoint(minLocation);
    int cMax = columnAtPoint(maxLocation);
    // This should never happen.
    if (cMin == -1) {
      cMin = 0;
    }
    // If the table does not have enough columns to fill the view we'll get -1.
    // Replace this with the index of the last column.
    if (cMax == -1) {
      cMax = getColumnCount() - 1;
    }

    // ***************

    // g.setColor(table.getGridColor());
    g.setColor(DIVIDER_COLOR);
    ((Graphics2D) g).setStroke(stroke);

    Rectangle minCell = getCellRect(rMin, cMin, true);
    Rectangle maxCell = getCellRect(rMax, cMax, true);

    int tableWidth = maxCell.x + maxCell.width;
    int y = minCell.y;

    for (int row = rMin; row <= rMax; row++) {
      int rh = getRowHeight(row);
      y += rh;
      if (model.drawDivider(row)) g.drawLine(0, y - 1, tableWidth - 1, y - 1);
    }
  }
}
