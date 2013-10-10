/*
 * File    : TextGridContent.java
 * Created : 04-oct-2001 12:30
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

import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.awt.Color;
import java.util.Iterator;


/**
 * This class encapsulates the content of {@link edu.xtec.jclic.boxes.TextGridContent} objects.
 * It implements methds to set and retrieve individual characters on the grid, and to
 * serialize and de-serialize the content from/to XML objects. It also contains information
 * about the desired size and graphic proprties (font, colors, etc.) of the grid.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class TextGridContent extends Object implements Domable{
    
    public static final char DEFAULT_WILD='*';
    public static final String DEFAULT_RANDOM_CHARS="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    public int ncw=1, nch=1;
    public double w=TextGrid.DEFAULT_CELL_SIZE;
    public double h=TextGrid.DEFAULT_CELL_SIZE;
    public boolean border=false;
    public BoxBase bb=null;
    public String[] text=new String[1];
    public char wild=DEFAULT_WILD;
    public String randomChars=DEFAULT_RANDOM_CHARS;
    
    /** Creates new TextGridContent */
    public TextGridContent() {
    }
    
    public static TextGridContent initNew(int ncw, int nch, char firstChar){
        TextGridContent result=new TextGridContent();
        result.ncw=ncw;
        result.nch=nch;
        result.text=new String[nch];
        StringBuilder sb=new StringBuilder();
        for(int i=0; i<nch; i++){
            sb.setLength(0);
            for(int j=0; j<ncw; j++)
                sb.append(firstChar++);
            result.text[i]=sb.toString();
        }
        result.bb=new BoxBase();
        result.bb.backColor=Color.white;
        result.border=true;
        return result;
    }
    
    
    public static final String ELEMENT_NAME="textGrid";
    public static final String ROWS="rows", COLUMNS="columns",
    CELL_WIDTH="cellWidth", CELL_HEIGHT="cellHeight",
    BORDER="border", ROW="row", TEXT="text", WILD="wild", RANDOM_CHARS="randomChars";
    
    public org.jdom.Element getJDomElement(){
        
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        org.jdom.Element child;
        
        e.setAttribute(ROWS, Integer.toString(ncw));
        e.setAttribute(COLUMNS, Integer.toString(nch));
        e.setAttribute(CELL_WIDTH, Double.toString(w));
        e.setAttribute(CELL_HEIGHT, Double.toString(h));
        e.setAttribute(BORDER, JDomUtility.boolString(border));
        if(wild!=DEFAULT_WILD)
            e.setAttribute(WILD, String.copyValueOf(new char[]{wild}));
            
        if(!DEFAULT_RANDOM_CHARS.equals(randomChars))
            e.setAttribute(RANDOM_CHARS, randomChars);
            
        if(bb!=null)
            e.addContent(bb.getJDomElement());
            
        child=new org.jdom.Element(TEXT);
        for(int i=0; i<nch; i++){
            if(i<text.length)
                child.addContent(new org.jdom.Element(ROW).setText(text[i]));
        }
        e.addContent(child);
            
        return e;
    }
    
    public static TextGridContent getTextGridContent(org.jdom.Element e) throws Exception{
        TextGridContent tgc=new TextGridContent();
        tgc.setProperties(e, null);
        return tgc;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        org.jdom.Element child, child2;
        JDomUtility.checkName(e, ELEMENT_NAME);
        ncw=JDomUtility.getIntAttr(e, ROWS, ncw);
        nch=JDomUtility.getIntAttr(e, COLUMNS, nch);
        w=JDomUtility.getDoubleAttr(e, CELL_WIDTH, w);
        h=JDomUtility.getDoubleAttr(e, CELL_HEIGHT, h);
        if(ncw<1 || nch<1 || w<1 || h<1)
            throw new IllegalArgumentException("Invalid TextGridContent attributes!");
        
        String s=e.getAttributeValue(WILD);
        if(s!=null && s.length()>0)
            wild=s.charAt(0);
        
        randomChars=JDomUtility.getStringAttr(e, RANDOM_CHARS, randomChars, false);
        
        border=JDomUtility.getBoolAttr(e, BORDER, border);
        
        if((child=e.getChild(BoxBase.ELEMENT_NAME))!=null)
            bb=BoxBase.getBoxBase(child);
        
        if((child=e.getChild(TEXT))!=null){
            text=new String[nch];
            Iterator itr=child.getChildren(ROW).iterator();
            int i=0;
            while(itr.hasNext()){
                child2=((org.jdom.Element)itr.next());
                text[i++]=child2.getText();
            }
            for(; i<nch; i++)
                text[i]="";
        }
    }
    
    public void completeText(){
        char[] emptyLineChars=new char[ncw];
        for(int i=0; i<ncw; i++)
            emptyLineChars[i]=wild;
        
        String[] result=new String[nch];
        for(int i=0; i<nch; i++){
            StringBuilder sb=new StringBuilder();
            if(i<text.length){
                char[] txch=text[i].toCharArray();
                sb.append(txch, 0, Math.min(txch.length, ncw));
                int l=sb.length();
                if(l<ncw)
                    sb.append(emptyLineChars, 0, ncw-l);
                result[i]=sb.substring(0);
            }
            else
                result[i]=String.copyValueOf(emptyLineChars);
        }
        text=result;
    }
    
    public int countWildChars(){
        int result=0;
        completeText();
        if(text!=null)
            for(int y=0; y<nch; y++)
                for(int x=0; x<ncw; x++)
                    if(text[y].charAt(x)==wild)
                        result++;
        return result;
    }
    
    public int getNumChars(){
        return ncw*nch;
    }
    
    public void setCharAt(int x, int y, char ch){
        if(x>=0 && x<ncw && y>=0 && y<nch){
            StringBuilder sb=new StringBuilder(text[y]);
            sb.setCharAt(x, ch);
            text[y]=sb.substring(0);
        }
    }
    
    public void copyStyleTo(TextGridContent tgc){
        if(tgc!=null){
            tgc.w=w;
            tgc.h=h;
            tgc.border=border;
            tgc.bb=bb;
        }
    }
}
