/*
 * File    : VFlowScrollPane.java
 * Created : 28-sep-2004 09:24
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

package edu.xtec.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class VFlowScrollPane extends JScrollPane{
    
    JPanel panel;
    int panelHGap, panelVGap;
    
    public VFlowScrollPane(JPanel view){
        super(view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel=view;
        if(panel.getLayout() instanceof FlowLayout){
            panelHGap=((FlowLayout)panel.getLayout()).getHgap();
            panelVGap=((FlowLayout)panel.getLayout()).getVgap();
        }
    }
    
    @Override
    public void doLayout(){
        int n=panel.getComponentCount();
        if(n>0){
            Dimension minSize=getMinimumSize();
            Component lastCmp=panel.getComponent(n-1);
            int w=Math.max(minSize.width, getWidth()-getVerticalScrollBar().getWidth()-panelHGap);
            panel.setPreferredSize(new Dimension(w, 9999));
            super.doLayout();
            panel.setPreferredSize(new Dimension(w, lastCmp.getY()+lastCmp.getHeight()+panelVGap));
            invalidate();
        }
        super.doLayout();
    }
}
