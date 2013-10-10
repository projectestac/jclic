/*
 * File    : CtrlPanel.java
 * Created : 10-mar-2004 16:48
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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This class is a special {@link javax.swing.JPanel}, designed to edit the
 * properties of a specific object. It has a boolean flag useful
 * to signal if the edited object has been modified, and implements several
 * listeners (a {@link java.beans.PropertyChangeListener}, a
 * {@link javax.swing.event.DocumentListener} and a {@link java.awt.event.ActionListener}).
 * This listeners can be hooked to child controls in order to vehiculate a single way
 * to handle events that can cause changes to the edited object.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class CtrlPanel extends JPanel implements PropertyChangeListener, 
                                                 ActionListener, DocumentListener {
    
    private boolean modified;
    private static int initializing;
    private MouseAdapter mouseAdapter;
    
    public boolean eventPerformed(EventObject eventObject){
        return genericEvent(eventObject);
    }
    
    public boolean documentChangePerformed(DocumentEvent documentEvent){
        return genericEvent(documentEvent);
    }
    
    public boolean genericEvent(Object event){
        return true;
    }
    
    public final void actionPerformed(ActionEvent actionEvent) {
        if(!isInitializing() && eventPerformed(actionEvent))
            modified=true;
    }
    
    public final void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if(!isInitializing() && eventPerformed(propertyChangeEvent))
            modified=true;
    }
    
    public final void changedUpdate(DocumentEvent documentEvent) {
        if(!isInitializing() && documentChangePerformed(documentEvent))
            modified=true;
    }
    
    public final void insertUpdate(DocumentEvent documentEvent) {
        if(!isInitializing() && documentChangePerformed(documentEvent))
            modified=true;
    }
    
    public final void removeUpdate(DocumentEvent documentEvent) {
        if(!isInitializing() && documentChangePerformed(documentEvent))
            modified=true;
    }
    
    public MouseListener getMouseClickListener(){
        if(mouseAdapter==null){
            mouseAdapter=new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if(!isInitializing() && eventPerformed(evt))
                        modified=true;
                }
            };
        }
        return mouseAdapter;
    }
    
    /** Getter for property modified.
     * @return Value of property modified.
     *
     */
    public final boolean isModified() {
        return findModified(this);
    }
    
    /** Setter for property modified.
     * @param modified New value of property modified.
     *
     */
    public final void setModified(boolean value) {
        if(value)
            modified=true;
        else
            clearModified(this);
    }
    
    private static void clearModified(Container cnt){
        if(cnt instanceof CtrlPanel){
            ((CtrlPanel)cnt).modified=false;
        }
        for(Component ch : cnt.getComponents()){
            if(ch instanceof Container)
                clearModified((Container)ch);
        }
    }
    
    private static boolean findModified(Container cnt){
        boolean result = cnt instanceof CtrlPanel ? ((CtrlPanel)cnt).modified : false;
        if(!result){
            for(Component ch : cnt.getComponents()){
                if(ch instanceof Container)
                    if((result=findModified((Container)ch))==true)
                        break;
            }
        }
        return result;
    }
    
    /** Getter for property initializing.
     * @return Value of property initializing.
     *
     */
    public final boolean isInitializing() {
        return initializing!=0;
    }
    
    /** Setter for property initializing.
     * @param initializing New value of property initializing.
     *
     */
    public final void setInitializing(boolean value) {
        initializing += value ? 1 : -1;
    }
    
    public Container getAWTAncestor(Class<?> ancestorClass){
        Container result=null;
        Container c=getParent();
        while(c!=null){
            if(ancestorClass.isAssignableFrom(c.getClass())){
                result=c;
                break;
            }
            c=c.getParent();
        }
        return result;
    }
}
