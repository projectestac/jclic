/*
 * File    : EditorTreePanel.java
 * Created : 05-jun-2002 17:49
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

package edu.xtec.jclic.edit;

import edu.xtec.util.Options;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class EditorTreePanel extends JPanel implements TreeSelectionListener{
    
    Options options;
    public Editor currentItem;
    
    protected Map<Class,EditorPanel> editPanels;
    protected boolean onlySelect;
    protected Class selectable;
    protected EditorPanel currentPanel;
    protected JTree tree;
    protected JPanel edit;
    protected Editor root;
    
    /** Creates new EditorTreePanel */
    public EditorTreePanel(Editor root, Options options, boolean onlySelect, Class selectable) {
        super(new BorderLayout());
        this.options=options;
        this.onlySelect=onlySelect;
        this.selectable=selectable;
        this.root=root;
        editPanels=new HashMap<Class,EditorPanel>();
        init();
    }
    
    public JTree getTree(){
        return tree;
    }
    
    public void setRootEditor(Editor root){
        this.root=root;
        tree.setModel(root.getTreeModel());
        root.setCurrentTree(tree);
    }
    
    protected void init(){        
        tree=root.createJTree();
        if(onlySelect && selectable!=null){
            tree.setSelectionModel(new DefaultTreeSelectionModel(){
                @Override
                public void setSelectionPath(TreePath path){
                    Object o=path.getLastPathComponent();
                    if(o instanceof Editor){
                        Object u=((Editor)o).getUserObject();
                        if(selectable.isInstance(u)){
                            super.setSelectionPath(path);
                            return;
                        }
                    }
                    resetRowSelection();
                }
            });
        }
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
                
        if(onlySelect){
            JScrollPane scroll=new JScrollPane(tree);
            scroll.setPreferredSize(new java.awt.Dimension(250, 300));
            add(scroll, BorderLayout.CENTER);
        } else{
            edit=new JPanel();
            edit.setLayout(new java.awt.BorderLayout());
            edit.setPreferredSize(new java.awt.Dimension(250, 300));
            JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new JScrollPane(tree), edit);
            split.setResizeWeight(1);
            split.setPreferredSize(new java.awt.Dimension(520, 300));
            add(split, BorderLayout.CENTER);
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    tree.setSelectionInterval(0, 0);
                }
            });
        }
        
        /*
        add(new JScrollPane(tree), BorderLayout.CENTER);
        if(!onlySelect){
            edit=new JPanel();
            edit.setLayout(new java.awt.BorderLayout());
            edit.setPreferredSize(new java.awt.Dimension(200, 100));
            //edit.setMinimumSize(new java.awt.Dimension(250, 100));
            //edit.setMaximumSize(new java.awt.Dimension(250, 999));
            //edit.setPreferredSize(new java.awt.Dimension(250, 200));
            add(edit, BorderLayout.EAST);
        }
         */
    }
    
    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        currentItem=null;
        //DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        Editor node = (Editor)tree.getLastSelectedPathComponent();
        if (node == null) return;
        //currentItem=(Editor)node.getUserObject();
        currentItem=node;
        currentItemChanged();
    }
    
    protected void currentItemChanged(){
        if(edit!=null){
            if(currentPanel!=null){
                currentPanel.removeEditor(true);
            }
            Class pc=currentItem.getEditorPanelClass();
            EditorPanel ep=editPanels.get(pc);
            if(ep==null){
                ep=currentItem.createEditorPanel(options);
                editPanels.put(pc, ep);
            }
            ep.attachEditor(currentItem, true);
            if(currentPanel!=ep){
                if(currentPanel!=null)
                    edit.remove(currentPanel);
                edit.add(ep, BorderLayout.CENTER);
                edit.revalidate();
                edit.repaint();
            }
            currentPanel=ep;
        }
    }
    
    public EditorPanel getCurrentPanel(){
        return currentPanel;
    }
}
