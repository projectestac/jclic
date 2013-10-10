/*
 * File    : Counter.java
 * Created : 02-apr-2001 17:12
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

package edu.xtec.jclic.boxes;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import javax.swing.JComponent;


/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class Counter extends AbstractBox {
    
    int value;
    int countDown;
    boolean enabled;
    Image img;
    Dimension dSize;
    Point origin;
    
    /** Creates new Counter */
    public Counter(AbstractBox parent, JComponent container, Rectangle2D r, BoxBase boxBase) {
        super(parent, container, boxBase);
        img=null;
        value=0;
        enabled=false;
        setBounds(r);
        countDown=0;
    }
    
    public void setEnabled(boolean bEnabled){
        enabled=bEnabled;
        repaint();
    }
    
    public boolean isEnabled(){
        return enabled;
    }
    
    public void setCountDown(int maxValue){
        countDown=maxValue;
        repaint();
    }
    
    public void setSource(Image setImg, Point setOrigin, Dimension setDigitSize){
        img=setImg;
        origin=setOrigin;
        dSize=setDigitSize;
        repaint();
    }
    
    public void incValue(){
        value++;
        if(enabled) 
            repaint();
        //  paintImmediatelly();
    }
    
    public void setValue(int newValue){
        value=newValue;
        if(enabled) 
            repaint();
    }
    
    public int getValue(){
        return value;
    }
    
    public boolean updateContent(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io){
        int i, j, w, d, valr;
        boolean k;
        double marginW=(width-3*dSize.width)/2;
        double marginH=(height-dSize.height)/2;
        
        // Todo: implement text mode
        if(img==null) 
            return false;
        
        valr=value;
        if(countDown>0)
            valr=Math.max(0, countDown-value);
        
        valr=Math.min(999, valr);
        
        for(k=false, i=0, j=100; i<3; i++, j/=10){
            if(!enabled) d=1;
            else{
                if((w=(valr/j)%10)!=0){
                    k=true;
                    d=11-w;
                }
                else 
                    d=(k==true || i==2 ? 11:1);
            }
            
            g2.drawImage(img, (int)(x+marginW+dSize.width*i), (int)(y+marginH),
            (int)(x+marginW+dSize.width*(i+1)), (int)(y+marginH+dSize.height),
            origin.x, origin.y+dSize.height*d,
            origin.x+dSize.width, origin.y+dSize.height*(d+1), io);
        }        
        return true;
    }
}