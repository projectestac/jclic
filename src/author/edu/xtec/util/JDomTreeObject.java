/*
 * File    : JDomTreeObject.java
 * Created : 05-dec-2002 11:19
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

import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public class JDomTreeObject extends AbstractTableModel{
    
    org.jdom.Element element;
    Options options;
    
    JDomTreeObject(org.jdom.Element element, Options options){
        super();
        this.element=element;
        this.options=options;
    }
    
    @Override
    public String getColumnName(int column){
        return options.getMsg(column==0 ? "XML_ATTRIBUTE" : "XML_VALUE");
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return (columnIndex!=0);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        if(aValue==null)
            return;
        if(rowIndex==0){
            if(element.getChildren().isEmpty())
                element.setText(aValue.toString());
            else
                options.getMessages().showAlert(options.getMainComponent(), "XML_NOT_EDITABLE");
        }
        else{
            org.jdom.Attribute atr=(org.jdom.Attribute)element.getAttributes().get(rowIndex-1);
            atr.setValue(aValue.toString());
        }
    }
    
    public int getRowCount(){
        return 1+element.getAttributes().size();
    }
    
    public int getColumnCount(){
        return 2;
    }
    
    public Object getValueAt(int row, int column){
        if(row==0){
            return (column==0 ? options.getMsg("XML_TEXT") : element.getText());
        }        
        org.jdom.Attribute atr=(org.jdom.Attribute)element.getAttributes().get(row-1);
        return(column==0 ? atr.getName() : atr.getValue());
    }
    
    @Override
    public String toString(){
        org.jdom.Attribute atr;
        StringBuilder result=new StringBuilder(element.getName());
        if((atr=element.getAttribute(JDomUtility.NAME))!=null)
            result.append(" ").append(atr.getValue());
        if((atr=element.getAttribute(JDomUtility.ID))!=null)
            result.append(" - ").append(atr.getValue());
        if((atr=element.getAttribute(JDomUtility.TYPE))!=null)
            result.append(" - ").append(atr.getValue());
        return result.substring(0);
    }
    
    public static DefaultMutableTreeNode processNode(DefaultMutableTreeNode parent, org.jdom.Element element, Options options){
        JDomTreeObject te=new JDomTreeObject(element, options);
        DefaultMutableTreeNode node=new DefaultMutableTreeNode(te);
        Iterator iter =element.getChildren().iterator();
        while(iter.hasNext()){
            processNode(node, (org.jdom.Element)iter.next(), options);
        }
        if(parent!=null)
            parent.add(node);
        return node;
    }
    
    /** Getter for property element.
     * @return Value of property element.
     */
    public org.jdom.Element getElement() {
        return element;
    }
    
}
