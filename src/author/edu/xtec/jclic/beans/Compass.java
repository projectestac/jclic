/*
 * File    : Compass.java
 * Created : 04-nov-2002 09:36
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

package edu.xtec.jclic.beans;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class Compass extends JPanel {
    
    public static final String PROP_DIRECTION = "direction";
    
    public static final String[] BT_NAMES={"nw", "n", "ne", "w", "c", "e", "sw", "s", "se"};
    public static final java.awt.Dimension BT_DIMENSION=new java.awt.Dimension(16, 16);
    
    private JToggleButton[] buttons;    
    private int direction;
    
    /** Creates new Compass */
    public Compass() {
        super(new java.awt.GridLayout(3, 3));
        setOpaque(false);
        direction=-1;        
        ActionListener lst=new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                String c=ev.getActionCommand();
                for(int i=0; i<9; i++){
                    if(BT_NAMES[i].equals(c)){
                        setDirection(i);
                        break;
                    }
                }
            }
        };        
        ButtonGroup bg=new ButtonGroup();
        buttons=new JToggleButton[9];    
        for(int i=0; i<9; i++){
            JToggleButton btn=new JToggleButton(new ImageIcon(getClass().getResource("/edu/xtec/resources/icons/"+BT_NAMES[i]+".gif")));
            btn.setPreferredSize(BT_DIMENSION);
            bg.add(btn);
            btn.setActionCommand(BT_NAMES[i]);
            btn.addActionListener(lst);
            btn.setFocusPainted(false);
            add(btn);
            buttons[i]=btn;
        }
        
    }
    
    public int getDirection() {
        return direction;
    }
    
    public void setDirection(int value) {
        int oldValue = direction;        
        direction = value;        
        firePropertyChange(PROP_DIRECTION, oldValue, direction);
        if(value>=0){
            if(buttons[value]!=null && !buttons[value].isSelected())
                buttons[value].setSelected(true);
        }
        else
            if(oldValue>=0)
                buttons[oldValue].setSelected(false);
    }
    
    public int[] getDoubleDirection(){
        int[] result=new int[2];
        int v=(direction<0 ? 4 : direction);
        result[0]=v%3;
        result[1]=v/3;
        return result;
    }
    
    public void setDoubleDirection(int[] value){
        if(value==null || value.length!=2 || value[0]<0 || value[1]<0)
            setDirection(-1);
        else
            setDirection(3*value[1]+value[0]);
    }
        
}
