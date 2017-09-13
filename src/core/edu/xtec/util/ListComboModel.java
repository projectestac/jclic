/*
 * File    : ListComboModel.java
 * Created : 10-apr-2003 18:11
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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ListComboModel extends Object implements ComboBoxModel<Object>{
    
    private ListModel<Object> model;
    private Object current;
    
    /** Creates a new instance of ListComboModel */
    public ListComboModel(ListModel<Object> model) {
        this.model=model;
    }
    
    public ListComboModel() {
        this(new DefaultListModel<Object>());
    }
    
    public ListModel<Object> getListModel(){
        return model;
    }
    
    public Object getSelectedItem(){
        return current;
    }
    
    public void setSelectedItem(Object anItem){
        current=anItem;
    }
    
    public void addListDataListener(ListDataListener l){
        model.addListDataListener(l);
    }
    
    public Object getElementAt(int index){
        return index<0 ? null : model.getElementAt(index);
    }
    
    public int getSize() {
        return model.getSize();
    }
    
    public void removeListDataListener(ListDataListener l) {
        model.removeListDataListener(l);
    }
}
