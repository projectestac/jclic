/*
 * File    : HolesEditorPanel.java
 * Created : 03-dec-2002 10:03
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

package edu.xtec.jclic.shapers;

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class HolesEditorPanel extends javax.swing.JPanel implements PointListener, ActionListener, PropertyChangeListener{
    
    public static final double ROTATE_UNIT=Math.PI/180*15;
    
    Options options;
    Holes shaper;
    Image img;
    Rectangle previewArea;
    BoxBase previewBb;
    ShapeListModel listModel;
    ListSelectionListener listSelectionListener;
    boolean modified;
    int currentShape;
    
    protected PolygonDrawPanel pdp;
    private boolean shapeDrawn=false;
    double xFactor=1, yFactor=1;
    double lastWidth=-1, lastHeight=-1;
    
    Dimension previewDim;//
    
    private boolean initializing;
    
    private int zoomFactor=1;
    public static final int MAX_ZOOM=10, MIN_ZOOM=-2;
    
    /** Creates new form JigSawEditor */
    public HolesEditorPanel(Options options, Holes shaper, Dimension previewDim, Image img, BoxBase previewBb) {
        this.options=options;
        this.shaper=shaper;
        this.img=img;
        this.previewDim=previewDim;
        
        initializing=true;
        
        //currentShape=-1;
        currentShape=shaper.getNumCells()+1;
        previewArea=new Rectangle(img==null ? previewDim : new Dimension(img.getWidth(this), img.getHeight(this)));
        
        //previewAreaStart=previewArea;
        this.previewBb=previewBb;
        initMembers();
        initComponents();
        customizeComponents();
        
        pdp=new PolygonDrawPanel(img==null ? (int)(previewDim.getWidth()):img.getWidth(this), img==null ? ((int)previewDim.getHeight()) :img.getHeight(this),this,(img==null));
        pdp.addPointListener(this);
        if (previewPanel!=null){
            ((PreviewPanel)previewPanel).vp.addMouseMotionListener(pdp);
            ((PreviewPanel)previewPanel).vp.addMouseListener(pdp);
        }
        initializing=false;
        
        shapeChanged();
    }
    
    @Override
    public void setCursor(java.awt.Cursor c){
        if (previewPanel!=null)
            ((PreviewPanel)previewPanel).vp.setCursor(c);
    }
    
    public void setCursor(java.awt.Cursor c, boolean onlyPreviewPanel){
        if (previewPanel!=null)
            ((PreviewPanel)previewPanel).vp.setCursor(c);
    }

    class ShapeListModel extends AbstractListModel {

        public int getSize() {
            return shaper.shapeData.length;
        }

        public Object getElementAt(int index) {
            ShapeData sd = shaper.shapeData[index];
            return (sd.comment == null || sd.comment.length() == 0) ?
                Integer.toString(index) : sd.comment;
        }

        public void switchShapes(int index1, int index2) {
            if (shaper != null
                    && index1 >= 0 && index1 < shaper.shapeData.length
                    && index2 >= 0 && index2 < shaper.shapeData.length) {

                /*
                pdp.deSelectAll();
                setCurrentShape(-1);
                ShapeData sd1 = shaper.shapeData[index1];
                ShapeData sd2 = shaper.shapeData[index2];
                shaper.shapeData[index1] = sd2;
                shaper.shapeData[index2] = sd1;
                setCurrentShape(index1);
                updateList();
                 *
                 */

                //shapeChanged();

                //listSelectionListener.valueChanged(new ListSelectionEvent(listModel, index1, index2, false));
            }
        }
    }

    private void initMembers(){

        listModel=new ShapeListModel();
        
        listSelectionListener=new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent ev){
                if(ev.getValueIsAdjusting())
                    return;
                int v=shapesList.getSelectedIndex();
                if(v!=currentShape && v>=0){
                    if (pdp.getNumShapes()>0) 
                        pdp.endPolygon(true,false,v);
                    if (v>=0 && getHoles().getShapeData(v)!=null)
                        tfName.setText(getHoles().getShapeData(v).comment);
                    else
                        tfName.setText("");
                    setCurrentShapeNoList(v);
                }
                else
                    tfName.setText("");

                upBtn.setEnabled(v>0);
                downBtn.setEnabled(v < shapesList.getModel().getSize()-1);
            }
        };
    }
    
    protected void confirmChanges(){
        pdp.endPolygon();
    }
    
    protected void customizeComponents(){
        
        btShowDrawnPoints.setSelected(EditableShapeConstants.showDrawnPoints);
        
        btGrid.setSelected(EditableShapeConstants.gridWidth!=-1);
        gridSizeEdit.setEnabled(btGrid.isSelected());
                
        btMagnet.setSelected(EditableShapeConstants.pointsOnGrid);
        magnetRadiusEdit.setEnabled(btMagnet.isSelected());
        
        btSelect.setSelected(true);
        
    }
    
    public void updateList(){
        initMembers();
        shapesList.setModel(listModel);
    }
    
    protected void setCurrentShape(int v){
        if (shapesList.getSelectedIndex()!=v)
            shapesList.setSelectedIndex(v);
        btDelete.setEnabled(pdp.getNumShapes()>0);
        updateTransformingButtons();
        currentShape=v;
    }
    
    protected void setCurrentShapeNoList(int v){
        currentShape=v;
        pdp.selectShape(v);
        btDelete.setEnabled(pdp.getNumShapes()>0);
        ((PreviewPanel)previewPanel).updateView();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgMode = new javax.swing.ButtonGroup();
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        javax.swing.JToolBar tbTop1 = new javax.swing.JToolBar();
        btShowDrawnPoints = new javax.swing.JToggleButton();
        btShowDrawnPoints.addActionListener(this);
        javax.swing.JSeparator sept1 = new JToolBar.Separator();
        btGrid = new javax.swing.JToggleButton();
        btGrid.addActionListener(this);
        gridSizeEdit = new edu.xtec.jclic.beans.SmallIntEditor();
        gridSizeEdit.addPropertyChangeListener(this);
        javax.swing.JSeparator sept2 = new JToolBar.Separator();
        btDelete = new javax.swing.JButton();
        btDelete.addActionListener(this);
        btCopy = new javax.swing.JButton();
        btCopy.addActionListener(this);
        btPaste = new javax.swing.JButton();
        btPaste.addActionListener(this);
        javax.swing.JSeparator sept3 = new JToolBar.Separator();
        btMagnet = new javax.swing.JToggleButton();
        btMagnet.addActionListener(this);
        magnetRadiusEdit = new edu.xtec.jclic.beans.SmallIntEditor();
        magnetRadiusEdit.addPropertyChangeListener(this);
        javax.swing.JSeparator sept4 = new JToolBar.Separator();
        javax.swing.JPanel zoomPanel = new javax.swing.JPanel();
        javax.swing.JLabel zoomLb = new javax.swing.JLabel();
        btZoomOut = new javax.swing.JButton();
        btZoomOut.addActionListener(this);
        lbZoom = new javax.swing.JTextField();
        btZoomIn = new javax.swing.JButton();
        btZoomIn.addActionListener(this);
        javax.swing.JLabel spacerTop = new javax.swing.JLabel();
        javax.swing.JSeparator sep0 = new javax.swing.JSeparator();
        javax.swing.JPanel leftPanel = new javax.swing.JPanel();
        javax.swing.JToolBar tbLeft1 = new javax.swing.JToolBar();
        btSelect = new javax.swing.JToggleButton();
        btSelect.addActionListener(this);
        btDivide = new javax.swing.JToggleButton();
        btDivide.addActionListener(this);
        javax.swing.JSeparator sep1 = new JToolBar.Separator();
        btRect = new javax.swing.JToggleButton();
        btRect.addActionListener(this);
        btEllipse = new javax.swing.JToggleButton();
        btEllipse.addActionListener(this);
        btPolygon = new javax.swing.JToggleButton();
        btPolygon.addActionListener(this);
        btLine = new javax.swing.JButton();
        btLine.addActionListener(this);
        btBezier = new javax.swing.JButton();
        btBezier.addActionListener(this);
        btQuad = new javax.swing.JButton();
        btQuad.addActionListener(this);
        javax.swing.JSeparator sep2 = new JToolBar.Separator();
        btExpand = new javax.swing.JButton();
        btExpand.addActionListener(this);
        btContract = new javax.swing.JButton();
        btContract.addActionListener(this);
        javax.swing.JSeparator sep4 = new JToolBar.Separator();
        btRotateRight = new javax.swing.JButton();
        btRotateRight.addActionListener(this);
        btRotateLeft = new javax.swing.JButton();
        btRotateLeft.addActionListener(this);
        javax.swing.JLabel spacerLeft = new javax.swing.JLabel();
        javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane();
        previewPanel = new PreviewPanel();
        previewPanel.addPropertyChangeListener(this);
        javax.swing.JPanel listPanel = new javax.swing.JPanel();
        orderBtnPanel = new javax.swing.JPanel();
        upBtn = new javax.swing.JButton();
        downBtn = new javax.swing.JButton();
        listScroll = new javax.swing.JScrollPane();
        shapesList = new javax.swing.JList();
        tfName = new javax.swing.JTextField();
        tfName.addActionListener(this);

        setLayout(new java.awt.GridBagLayout());

        topPanel.setLayout(new java.awt.GridBagLayout());

        tbTop1.setFloatable(false);

        btShowDrawnPoints.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/vertex_off.gif"))); // NOI18N
        btShowDrawnPoints.setToolTipText(options.getMsg("edit_shape_showPoints_tooltip"));
        btShowDrawnPoints.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/vertex_on.gif"))); // NOI18N
        tbTop1.add(btShowDrawnPoints);

        sept1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        tbTop1.add(sept1);

        btGrid.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/grid.gif"))); // NOI18N
        btGrid.setToolTipText(options.getMsg("edit_shape_hideGrid"));
        tbTop1.add(btGrid);

        gridSizeEdit.setToolTipText(options.getMsg("edit_shape_gridSize_tooltip"));
        gridSizeEdit.setEditColumns(2);
        gridSizeEdit.setMax(50);
        gridSizeEdit.setMin(5);
        gridSizeEdit.setValue(10);
        gridSizeEdit.setValues(new int[]{5, 10, 15, 20, 30, 50});
        tbTop1.add(gridSizeEdit);

        sept2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        tbTop1.add(sept2);

        btDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/delete.gif"))); // NOI18N
        btDelete.setToolTipText(options.getMsg("edit_shape_deletePoint"));
        btDelete.setMaximumSize(new java.awt.Dimension(32, 32));
        btDelete.setMinimumSize(new java.awt.Dimension(32, 32));
        tbTop1.add(btDelete);

        btCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/copy.gif"))); // NOI18N
        btCopy.setToolTipText(options.getMsg("COPY"));
        tbTop1.add(btCopy);

        btPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/paste.gif"))); // NOI18N
        btPaste.setToolTipText(options.getMsg("PASTE"));
        tbTop1.add(btPaste);

        sept3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        tbTop1.add(sept3);

        btMagnet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/imant.gif"))); // NOI18N
        btMagnet.setToolTipText(options.getMsg("edit_shape_aproxPoints"));
        tbTop1.add(btMagnet);

        magnetRadiusEdit.setToolTipText(options.getMsg("edit_shape_magnetRadius_tooltip"));
        magnetRadiusEdit.setEditColumns(2);
        magnetRadiusEdit.setMax(15);
        magnetRadiusEdit.setMin(1);
        magnetRadiusEdit.setValue(4);
        magnetRadiusEdit.setValues(new int[]{1, 2, 4, 6, 10, 15});
        tbTop1.add(magnetRadiusEdit);

        sept4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        tbTop1.add(sept4);

        zoomPanel.setOpaque(false);
        zoomPanel.setLayout(new javax.swing.BoxLayout(zoomPanel, javax.swing.BoxLayout.LINE_AXIS));

        zoomLb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/zoom.gif"))); // NOI18N
        zoomLb.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 3));
        zoomPanel.add(zoomLb);

        btZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/minus.gif"))); // NOI18N
        btZoomOut.setToolTipText(options.getMsg("edit_shape_zoomOut"));
        btZoomOut.setPreferredSize(new java.awt.Dimension(16, 16));
        zoomPanel.add(btZoomOut);

        lbZoom.setColumns(3);
        lbZoom.setEditable(false);
        lbZoom.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lbZoom.setText("1x");
        zoomPanel.add(lbZoom);

        btZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/plus.gif"))); // NOI18N
        btZoomIn.setToolTipText(options.getMsg("edit_shape_zoomIn"));
        btZoomIn.setPreferredSize(new java.awt.Dimension(16, 16));
        zoomPanel.add(btZoomIn);

        tbTop1.add(zoomPanel);

        topPanel.add(tbTop1, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        topPanel.add(spacerTop, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(topPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(sep0, gridBagConstraints);

        leftPanel.setLayout(new java.awt.GridBagLayout());

        tbLeft1.setFloatable(false);
        tbLeft1.setOrientation(1);

        bgMode.add(btSelect);
        btSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/selectionMode.gif"))); // NOI18N
        btSelect.setToolTipText(options.getMsg("edit_shape_select"));
        btSelect.setMaximumSize(new java.awt.Dimension(32, 32));
        btSelect.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btSelect);

        bgMode.add(btDivide);
        btDivide.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/add_point.gif"))); // NOI18N
        btDivide.setToolTipText(options.getMsg("edit_shape_addPoint"));
        btDivide.setMaximumSize(new java.awt.Dimension(32, 32));
        btDivide.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btDivide);
        tbLeft1.add(sep1);

        bgMode.add(btRect);
        btRect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/rect.gif"))); // NOI18N
        btRect.setToolTipText(options.getMsg("edit_shape_drawRect"));
        btRect.setMaximumSize(new java.awt.Dimension(32, 32));
        btRect.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btRect);

        bgMode.add(btEllipse);
        btEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/rodona.gif"))); // NOI18N
        btEllipse.setToolTipText(options.getMsg("edit_shape_drawEllipse"));
        btEllipse.setMaximumSize(new java.awt.Dimension(32, 32));
        btEllipse.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btEllipse);

        bgMode.add(btPolygon);
        btPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/pent.gif"))); // NOI18N
        btPolygon.setToolTipText(options.getMsg("edit_shape_drawPoly"));
        btPolygon.setMaximumSize(new java.awt.Dimension(32, 32));
        btPolygon.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btPolygon);

        btLine.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/line.gif"))); // NOI18N
        btLine.setToolTipText(options.getMsg("edit_shape_toLine"));
        btLine.setMaximumSize(new java.awt.Dimension(32, 32));
        btLine.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btLine);

        btBezier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/bezier.gif"))); // NOI18N
        btBezier.setToolTipText(options.getMsg("edit_shape_toBezier"));
        btBezier.setMaximumSize(new java.awt.Dimension(32, 32));
        btBezier.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btBezier);

        btQuad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/quad.gif"))); // NOI18N
        btQuad.setToolTipText(options.getMsg("edit_shape_toQuad"));
        btQuad.setMaximumSize(new java.awt.Dimension(32, 32));
        btQuad.setMinimumSize(new java.awt.Dimension(32, 32));
        tbLeft1.add(btQuad);
        tbLeft1.add(sep2);

        btExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/expand.gif"))); // NOI18N
        btExpand.setToolTipText(options.getMsg("edit_shape_expand"));
        tbLeft1.add(btExpand);

        btContract.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/contract.gif"))); // NOI18N
        btContract.setToolTipText(options.getMsg("edit_shape_contract"));
        tbLeft1.add(btContract);
        tbLeft1.add(sep4);

        btRotateRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/rotate_right.gif"))); // NOI18N
        btRotateRight.setToolTipText(options.getMsg("edit_shape_rRight"));
        tbLeft1.add(btRotateRight);

        btRotateLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/rotate_left.gif"))); // NOI18N
        btRotateLeft.setToolTipText(options.getMsg("edit_shape_rLeft"));
        tbLeft1.add(btRotateLeft);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        leftPanel.add(tbLeft1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        leftPanel.add(spacerLeft, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(leftPanel, gridBagConstraints);

        splitPane.setResizeWeight(1.0);

        previewPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        previewPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitPane.setLeftComponent(previewPanel);

        listPanel.setLayout(new java.awt.BorderLayout(10, 8));

        upBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/up.gif")));
        upBtn.setToolTipText(options.getMsg("edit_list_upBtn_tooltip"));
        upBtn.setEnabled(false);
        upBtn.setPreferredSize(new java.awt.Dimension(16, 16));
        upBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upBtnActionPerformed(evt);
            }
        });
        orderBtnPanel.add(upBtn);

        downBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/xtec/resources/icons/down.gif")));
        downBtn.setToolTipText(options.getMsg("edit_list_downBtn_tooltip"));
        downBtn.setEnabled(false);
        downBtn.setPreferredSize(new java.awt.Dimension(16, 16));
        downBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downBtnActionPerformed(evt);
            }
        });
        orderBtnPanel.add(downBtn);

        listPanel.add(orderBtnPanel, java.awt.BorderLayout.NORTH);

        listScroll.setToolTipText(options.getMsg("edit_shape_elements"));
        listScroll.setPreferredSize(new java.awt.Dimension(80, 100));

        shapesList.setModel(listModel);
        shapesList.addListSelectionListener(listSelectionListener);
        listScroll.setViewportView(shapesList);

        listPanel.add(listScroll, java.awt.BorderLayout.CENTER);

        tfName.setToolTipText(options.getMsg("edit_shape_elemName"));
        listPanel.add(tfName, java.awt.BorderLayout.SOUTH);

        splitPane.setRightComponent(listPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(splitPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void upBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upBtnActionPerformed

        if (listModel != null) {
            int sel = shapesList.getSelectedIndex();
            if (sel > 0 && sel < listModel.getSize() - 1) {
                listModel.switchShapes(sel, sel-1);
                shapesList.setSelectedIndex(sel - 1);
                modified = true;
                repaint(0);
            }
        }
    }//GEN-LAST:event_upBtnActionPerformed

    private void downBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downBtnActionPerformed

        if (listModel != null) {
            int sel = shapesList.getSelectedIndex();
            if (sel >= 0 && sel < listModel.getSize() - 2) {
                listModel.switchShapes(sel, sel+1);
                shapesList.setSelectedIndex(sel + 1);
                modified = true;
                repaint(0);
            }
        }
    }//GEN-LAST:event_downBtnActionPerformed
    
                
    private void setDrawingRectangleMode(){
        deselectAll();
        btRect.setSelected(true);
        pdp.setDrawingMode(PolygonDrawPanel.DRAWING_RECT);
        pdp.deSelectAll();
        repaint(0);
    }
    
    private void deselectAll(){
        pdp.cancelCurrentOperations();
        btSelect.setSelected(false);
        btRect.setSelected(false);
        btEllipse.setSelected(false);
        btPolygon.setSelected(false);
        btBezier.setSelected(false);
        btQuad.setSelected(false);
        btDivide.setSelected(false);
        //        btZoom.setSelected(false);
    }
    
    public void setDrawingMode(int drawingMode){
        pdp.setDrawingMode(drawingMode);
        switch (drawingMode){
            case PolygonDrawPanel.SELECTING:
                btSelect.setSelected(true);
                break;
            case PolygonDrawPanel.NEW_POINT:
                btDivide.setSelected(true);
                break;
        }
        btDelete.setEnabled(pdp.getNumShapes()>0);
    }
    
    public void shapeChanged() {
        if (pdp.getNumShapes()>0){ //Ja no podem crear un rectangle o el.lipse
            btDelete.setEnabled(true);
            shapeDrawn=true;
            repaint(0);
        }
        else {
            btDelete.setEnabled(pdp.hasSelectedPoint());
            clean();
        }
        if (!btDivide.isSelected()){
            pdp.setDrawingMode(PolygonDrawPanel.SELECTING);
            deselectAll();
            btSelect.setSelected(true);
        }
        else btSelect.setSelected(false);
        
        updateTransformingButtons();
    }
    
    protected void updateTransformingButtons(){
        List<EditableShape> v=pdp.getSelectedShapes();
        if (v.size()==1){
            EditableShape shape=v.get(0); //El te segur: size==1
            if (!(shape instanceof EditableRectangle)){
                btBezier.setEnabled(true);
                btQuad.setEnabled(true);
            }
            if (!(shape instanceof EditableEllipse2D))
                btLine.setEnabled(true);
        }
        else{
            btBezier.setEnabled(false);
            btQuad.setEnabled(false);
            btLine.setEnabled(false);
            repaint(0);
        }
    }
    
    public JComponent getPreviewPanel(){
        return previewPanel;
    }
    
    private void clean(){
        //pdp.deleteCurrent();
        pdp.deleteSelected(false);
        shapeDrawn=false;
        deselectAll();
        btRect.setEnabled(true);
        btEllipse.setEnabled(true);
        btPolygon.setEnabled(true);
        ////btDivide.setEnabled(false);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgMode;
    private javax.swing.JButton btBezier;
    private javax.swing.JButton btContract;
    private javax.swing.JButton btCopy;
    private javax.swing.JButton btDelete;
    private javax.swing.JToggleButton btDivide;
    private javax.swing.JToggleButton btEllipse;
    private javax.swing.JButton btExpand;
    private javax.swing.JToggleButton btGrid;
    private javax.swing.JButton btLine;
    private javax.swing.JToggleButton btMagnet;
    private javax.swing.JButton btPaste;
    private javax.swing.JToggleButton btPolygon;
    private javax.swing.JButton btQuad;
    private javax.swing.JToggleButton btRect;
    private javax.swing.JButton btRotateLeft;
    private javax.swing.JButton btRotateRight;
    private javax.swing.JToggleButton btSelect;
    private javax.swing.JToggleButton btShowDrawnPoints;
    private javax.swing.JButton btZoomIn;
    private javax.swing.JButton btZoomOut;
    private javax.swing.JButton downBtn;
    private edu.xtec.jclic.beans.SmallIntEditor gridSizeEdit;
    private javax.swing.JTextField lbZoom;
    private javax.swing.JScrollPane listScroll;
    private edu.xtec.jclic.beans.SmallIntEditor magnetRadiusEdit;
    private javax.swing.JPanel orderBtnPanel;
    private javax.swing.JScrollPane previewPanel;
    private javax.swing.JList shapesList;
    private javax.swing.JTextField tfName;
    private javax.swing.JButton upBtn;
    // End of variables declaration//GEN-END:variables
    
    public void updateView(){
        ((PreviewPanel)previewPanel).updateView();
    }
    
    class PreviewPanel extends JScrollPane{
        
        public VP vp;
        
        public PreviewPanel(){
            vp=new VP();
            setViewportView(vp);
            updateView();
        }
        
        public void updateView(){
            vp.updateView();
            //doLayout();////
        }
    }
    
    class VP extends JPanel{
        
        private int xBak=-1, yBak=-1, wBak=-1, hBak=-1;
        
        List<Shape> shapes;

        VP() {
            this.shapes = new ArrayList<Shape>();
        }
        
        public void updateView(){
            setSize(getSize());
            setPreferredSize(getSize());
            updateComponentsView();
        }
        
        public void updateComponentsView(){
            shapes.clear();
            for(int i=0; i<shaper.getNumCells(); i++){
                shapes.add(shaper.getShape(i, previewArea));
            }
            if (pdp!=null)
                pdp.updateView();
            super.updateUI();
            repaint();
            
        }
        
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            RenderingHints rh=g2.getRenderingHints();
            g2.setRenderingHints(Constants.DEFAULT_RENDERING_HINTS);
            
            Color defaultBgColor=g2.getBackground();
            Color defaultColor=g2.getColor();
            
            g2.setColor(previewBb.backColor);
            g2.fill(previewArea);
            g2.setBackground(previewBb.backColor);
            g2.setColor(previewBb.borderColor);
            Stroke defaultStroke=g2.getStroke();
            g2.setStroke(previewBb.getBorder());
            
            if(img!=null){
                g2.drawImage(img, previewArea.x, previewArea.y, previewArea.width, previewArea.height, this);
            }
            
            pdp.drawGrid(g,EditableShapeConstants.gridWidth);
            
            g2.setColor(Color.black);
            for(int i=0; i<shapes.size(); i++){
                if(i!=currentShape)
                    g2.draw((Shape)shapes.get(i));
            }
            
            g2.setColor(Color.red);
            pdp.paint(g2);
            ///%            drawBorder(g2,defaultBgColor);
            g2.setStroke(defaultStroke);
            g2.setColor(defaultColor);
            g2.setBackground(defaultBgColor);
            
            g2.setRenderingHints(rh);
        }
        
        protected void drawBorder(Graphics g, Color c){
            g.setColor(c);
            g.fillRect(0,0,(int)previewArea.getX(),getHeight());
        }
        
        @Override
        public void doLayout(){
            
            previewArea.x=(getBounds().width-previewArea.width)/2;
            previewArea.y=(getBounds().height-previewArea.height)/2;
            
            if (previewArea.x!=xBak || previewArea.y!=yBak || previewArea.width!=wBak || previewArea.height!=hBak){
                xBak=previewArea.x;
                yBak=previewArea.y;
                wBak=previewArea.width;
                hBak=previewArea.height;
                pdp.initDrawnBorders();
            }
            //previewArea.x=0;
            //previewArea.y=0;
            
            //updateView();
            updateComponentsView();
        }
        
        @Override
        public Dimension getSize(){
            return new Dimension(previewArea.x+(int)previewArea.getBounds().getWidth(),previewArea.y+(int)previewArea.getBounds().getHeight());
        }
    }
    
    public Holes getHoles(){
        return shaper;
    }
    
    public Rectangle getPreviewArea(){
        return previewArea;
    }
    
    public void setPreviewArea(Rectangle r){
        previewArea=r;
    }
    
    public int getNumShapes(){
        return shaper.getNumCells();
    }
    
    public static Shaper getShaper(Shaper initialShaper, Component parent, Options options, Dimension dim, Image img, BoxBase bb){
        //return HolesEditPanel.getShaper(initialShaper, parent, options, dim, img, bb);
        
        Messages msg=options.getMessages();
        if(initialShaper==null || !(initialShaper instanceof Holes))
            return null;
        
        Holes sh;
        try{
            sh=(Holes)initialShaper.clone();
        } catch(CloneNotSupportedException ex){
            msg.showErrorWarning(parent, "edit_act_shaper_err", ex);
            return null;
        }
        
        HolesEditorPanel he=new HolesEditorPanel(options, sh, dim, img, bb);
                
        boolean b=msg.showInputDlg(parent, he, "edit_act_shaper_properties");
        if(b)
            he.confirmChanges();
        return b ? sh : null;
        
    }
    
    public void updatePreviewArea(double xFactor, double yFactor){
        if (lastWidth!=-1){
            lastWidth*=xFactor;
            lastHeight*=yFactor;
        }
        else{
            lastWidth=previewArea.getWidth()*xFactor;
            lastHeight=previewArea.getHeight()*yFactor;
        }
        this.xFactor*=xFactor;
        this.yFactor*=yFactor;
        
        //previewArea=new Rectangle(new Point((int)(previewArea.x*xFactor),(int)(previewArea.y*yFactor)),new Dimension((int)(lastWidth), (int)(lastHeight)));
        previewArea=new Rectangle(new Dimension((int)(lastWidth), (int)(lastHeight)));
        
        //previewArea.x=(int)((((PreviewPanel)previewPanel).vp.getBounds().width-lastWidth)/2);
        //previewArea.y=(int)((((PreviewPanel)previewPanel).vp.getBounds().height-lastHeight)/2);
        
        //previewArea=new Rectangle(new Dimension((int)(previewArea.getWidth()*xFactor), (int)(previewArea.getHeight()*yFactor)));
        updateView();
    }
    
    public void incDrawingArea(double incWidth, double incHeight){
        modifyDrawingArea(previewArea.width+incWidth,previewArea.height+incHeight);
    }
    
    protected void modifyDrawingArea(double newWidth, double newHeight){
        if(newWidth>0 && newHeight>0){
            double xFactorMod=newWidth/previewArea.width;
            double yFactorMod=newHeight/previewArea.height;
            for(int i=0; i<shaper.getNumCells(); i++){
                ShapeData sd=shaper.getShapeData(i);
                sd.scaleTo(xFactorMod, yFactorMod);
            }
            previewArea.setSize((int)newWidth, (int)newHeight);
            previewDim.setSize(previewArea.getSize());
            
            shaper.scaleW=newWidth;
            shaper.scaleH=newHeight;
            
            updateView();
        }
    }
    
    public void pointMoved(java.awt.geom.Point2D p) {
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if(!initializing && actionEvent!=null){
            Object obj=actionEvent.getSource();
            if(obj==btDivide){
                deselectAll();
                requestFocus();
                btDivide.setSelected(true);
                pdp.setDrawingMode(PolygonDrawPanel.NEW_POINT);
                pdp.deSelectAll();
                repaint(0);
            }
            else if(obj==btDelete){
                clean();
                shapeChanged();
                repaint(0);
            }
            else if(obj==btEllipse){
                requestFocus();
                pdp.setDrawingMode(PolygonDrawPanel.DRAWING_ELLIPSE);
            }
            else if(obj==btRect){
                requestFocus();
                pdp.setDrawingMode(PolygonDrawPanel.DRAWING_RECT);
            }
            else if(obj==btLine){
                requestFocus();
                pdp.convertToLine();
                repaint(0);
            }
            else if(obj==btBezier){
                requestFocus();
                pdp.convertToBezier();
                repaint(0);
            }
            else if(obj==btQuad){
                requestFocus();
                pdp.convertToQuad();
                repaint(0);
            }
            else if(obj==btPolygon){
                requestFocus();
                pdp.setDrawingMode(PolygonDrawPanel.DRAWING_POLYGON);
            }
            else if(obj==btSelect){
                requestFocus();
                pdp.setDrawingMode(PolygonDrawPanel.SELECTING);
            }
            else if(obj==btShowDrawnPoints){
                EditableShapeConstants.showDrawnPoints=btShowDrawnPoints.isSelected();
                repaint(0);
            }
            else if(obj==btGrid){
                EditableShapeConstants.gridWidth=btGrid.isSelected() ? gridSizeEdit.getValue() : -1;
                gridSizeEdit.setEnabled(btGrid.isSelected());
                repaint();
            }
            else if(obj==btMagnet){
                EditableShapeConstants.pointsOnGrid=btMagnet.isSelected();
                magnetRadiusEdit.setEnabled(btMagnet.isSelected());
                repaint(0);
            }
            else if(obj==btRotateRight){
                pdp.rotate(ROTATE_UNIT, false, false);
            }
            else if(obj==btRotateLeft){
                pdp.rotate(-ROTATE_UNIT, false, false);
            }
            else if(obj==btExpand){
                pdp.scale(EditableShapeConstants.scaleXFactor,EditableShapeConstants.scaleYFactor,false,false);
            }
            else if(obj==btContract){
                if(EditableShapeConstants.scaleXFactor!=0 && EditableShapeConstants.scaleYFactor!=0)
                    pdp.scale(1/EditableShapeConstants.scaleXFactor,1/EditableShapeConstants.scaleYFactor,false,false);
            }
            else if(obj==btZoomIn){
                doZoom(true);
            }
            else if(obj==btZoomOut){
                doZoom(false);
            }
            else if(obj==btCopy){
                pdp.copy(false);
                updateView();
            }
            else if(obj==btPaste){
                pdp.endPolygon();
                pdp.paste();
                updateView();
            }
            else if(obj==tfName){
                ShapeData sd;
                if (currentShape>=0){
                    sd=getHoles().getShapeData(currentShape);
                    String txt=tfName.getText().trim();
                    //Potser caldria comprovar que no estigui repetit el nom
                    if (sd!=null && txt.length()>0) 
                        sd.comment=txt;
                    updateList();
                }                
            }
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        if(!initializing && ev!=null){
            Object obj=ev.getSource();
            if(obj==gridSizeEdit){
                int v=btGrid.isSelected() ? gridSizeEdit.getValue() : -1;
                if(v!=EditableShapeConstants.gridWidth){
                    EditableShapeConstants.gridWidth=v;
                    repaint();                
                }
            }
            else if(obj==magnetRadiusEdit){
                int v=magnetRadiusEdit.getValue()*2;                
                if(v!=EditableShapeConstants.selectLength){
                    EditableShapeConstants.selectLength=v;
                    pdp.initDrawnBorders();
                    repaint();
                }
            }
            else if(obj==previewPanel){
                repaint(0);
            }
        }        
    }
    
    protected void doZoom(boolean in){
        double zoomDelta=1.0;
        if(in){
            if(zoomFactor<MAX_ZOOM){
                if (zoomFactor>=1){
                    zoomDelta=((double)zoomFactor+1)/zoomFactor;
                }
                else{
                    double den=Math.abs(zoomFactor-2);
                    zoomDelta=(1/(den-1))/(1/den);
                }
                zoomFactor++;
            }
        }
        else{
            if(zoomFactor>MIN_ZOOM){
                if (zoomFactor>1){
                    zoomDelta=((double)zoomFactor-1)/zoomFactor;
                }
                else {
                    double den=Math.abs(zoomFactor-2);
                    zoomDelta=(1/(den+1))/(1/den);
                }
                zoomFactor--;
            }
        }
        if (zoomDelta!=1){
            pdp.endPolygon();
            updatePreviewArea(zoomDelta, zoomDelta);
        }
        btZoomIn.setEnabled(zoomFactor<MAX_ZOOM);
        btZoomOut.setEnabled(zoomFactor>MIN_ZOOM);
        String sFactor = (zoomFactor>=1) ? Integer.toString(zoomFactor) : ("1/"+Math.abs(zoomFactor-2));
        lbZoom.setText(sFactor+"x");        
    }
        
}
