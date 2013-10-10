/*
 * File    : ActiveBoxGrid.java
 * Created : 11-jan-2001 16:42
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

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.shapers.Shaper;
import edu.xtec.util.StrUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JComponent;

/**
 * This class is a {@link edu.xtec.jclic.boxes.ActiveBoxBag} with constructors that
 * take an argument of type {@link edu.xtec.jclic.shapers.Shaper} to build
 * all its {@link edu.xtec.jclic.boxes.ActiveBox} elements. It also mantains info about
 * the number of "rows" and "columns", useful to compute appropiate (integer) values when
 * resizing the <CODE>ActiveBoxBag</CODE> and its <CODE>ActiveBox</CODE> children.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class ActiveBoxGrid extends ActiveBoxBag implements Cloneable{
    
    public int nCols;
    public int nRows;
    
    public ActiveBoxGrid(AbstractBox parent, JComponent container,
    double px, double py, double setWidth, double setHeight,
    Shaper sh, BoxBase boxBase) {
        
        super(parent, container, boxBase);
        
        nCols=sh.getNumColumns();
        nRows=sh.getNumRows();
        Rectangle r=new Rectangle((int)px, (int)py
                                  , ((int)(setWidth / nCols))*nCols
                                  , ((int)(setHeight / nRows))*nRows);
        
        ensureCapacity(sh.getNumCells());
        
        for(int i=0; i<sh.getNumCells(); i++){
            Shape tmpSh=sh.getShape(i, r);
            ActiveBox bx=new ActiveBox(this, null, i, tmpSh.getBounds2D(), null);
            if(!sh.rectangularShapes())
                bx.setShape(tmpSh);
            addActiveBox(bx);
        }
        
        if(sh.hasRemainder()){
            Shape tmpSh=sh.getRemainderShape(r);
            ActiveBox bx=new ActiveBox(this, null, 0, tmpSh.getBounds2D(), null);
            bx.setShape(tmpSh);
            setBackgroundBox(bx);
        }
    }
    
    @Override
    public Dimension getMinimumSize(){
        return new Dimension
        ( Constants.MIN_CELL_SIZE * nCols
        , Constants.MIN_CELL_SIZE * nRows);
    }
    
    @Override
    public Dimension getScaledSize(double scale){
        return new Dimension
        ( StrUtils.roundTo(scale*preferredBounds.getWidth(), nCols)
        , StrUtils.roundTo(scale*preferredBounds.getHeight(), nRows));
    }
    
    public static ActiveBoxGrid createEmptyGrid(AbstractBox parent, JComponent container,
                  double px, double py, ActiveBagContent abc, Shaper sh, BoxBase boxBase) {
        
        ActiveBoxGrid result=null;
        if(abc!=null){
            result=new ActiveBoxGrid
            ( parent, container, px, py
            , abc.getTotalWidth(),abc.getTotalHeight()
            , sh==null ? abc.getShaper():sh
            , boxBase==null ? abc.bb : boxBase);
            
            result.setBorder(abc.border);
        }
        return result;
    }
    
    public static ActiveBoxGrid createEmptyGrid(ActiveBox parent, JComponent container,
                  double px, double py, ActiveBagContent abc) {
        return createEmptyGrid(parent, container, px, py, abc, null, null);
    }
    
    public Point getCoord(ActiveBox bx){
        int py=bx.idLoc/nCols;
        int px=bx.idLoc%nCols;
        return new Point(px, py);
    }
    
    public Point getCoordDist(ActiveBox src, ActiveBox dest){
        Point ptSrc=getCoord(src);
        Point ptDest=getCoord(dest);
        return new Point(ptDest.x-ptSrc.x, ptDest.y-ptSrc.y);
    }
}