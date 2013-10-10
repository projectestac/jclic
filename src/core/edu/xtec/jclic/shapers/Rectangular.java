/*
 * File    : Rectangular.java
 * Created : 10-may-2001 16:53
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

package edu.xtec.jclic.shapers;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class Rectangular extends Shaper {

    
    public Rectangular(int nx,int ny){
        super(nx, ny);
    }
    
    @Override
    public boolean rectangularShapes(){
        return true;
    }
    
    protected void buildShapes(){
        int r, c;
        double w=WIDTH/nCols;
        double h=HEIGHT/nRows;
        double x, y;        
        for(r=0; r<nRows; r++){
            for(c=0; c<nCols; c++){
                ShapeData sh=shapeData[r*nCols+c];
                x=c*w; y=r*h;
                sh.moveTo(x, y);
                sh.lineTo(x+w, y);
                sh.lineTo(x+w, y+h);
                sh.lineTo(x, y+h);
                sh.lineTo(x, y);
                sh.closePath();
            }
        }        
        initiated=true;
    }
}
