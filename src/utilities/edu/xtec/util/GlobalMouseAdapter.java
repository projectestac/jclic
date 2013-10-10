/*
 * File    : GlobalMouseAdapter.java
 * Created : 24-jan-2002 13:20
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
import java.awt.event.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class GlobalMouseAdapter implements MouseListener, MouseMotionListener{
    
    protected Component parent;

    /** Creates new GlobalMouseAdapter */
    public GlobalMouseAdapter(Component parent) {
        this.parent=parent;
    }
    
    public void attachTo(Component cmp, boolean catchMotion){
        cmp.addMouseListener(this);
        if(catchMotion)
            cmp.addMouseMotionListener(this);        
    }
    
    public Component getParent(){
        return parent;
    }
    
    public void setParent(Component parent){
        this.parent=parent;
    }
    
    public void detach(Component cmp){
        if(cmp!=null){
            cmp.removeMouseListener(this);
            cmp.removeMouseMotionListener(this);
        }
    }
    
    protected void processEvent(MouseEvent e){
        java.awt.Point pt=e.getComponent().getLocation();
        e.translatePoint(pt.x, pt.y);
        parent.dispatchEvent(e);
    }

    public void mouseDragged(java.awt.event.MouseEvent e) {
        processEvent(e);
    }
    
    public void mouseExited(java.awt.event.MouseEvent e) {
        processEvent(e);
    }
    
    public void mouseReleased(java.awt.event.MouseEvent e) {
        processEvent(e);
    }
    
    public void mouseMoved(java.awt.event.MouseEvent e) {
        processEvent(e);
    }
    
    public void mousePressed(java.awt.event.MouseEvent e) {
        processEvent(e);
    }
    
    public void mouseClicked(java.awt.event.MouseEvent e) {
        processEvent(e);
    }
    
    public void mouseEntered(java.awt.event.MouseEvent e) {
        processEvent(e);
    }    
}
