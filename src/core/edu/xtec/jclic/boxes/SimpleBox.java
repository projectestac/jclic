/*
 * File    : SimpleBox.java
 * Created : 08-oct-2001 9:37
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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import javax.swing.JComponent;


/**
 * This is the mst simple implementation of {@link edu.xtec.jclic.boxes.AbstractBox}. It
 * does nor draws nothing.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class SimpleBox extends AbstractBox{
    
    /** Creates new SimpleBox */
    public SimpleBox(AbstractBox parent, JComponent container, BoxBase boxBase) {
        super(parent, container, boxBase);
    }
    
    @Override
    public boolean update(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io){
        return true;
    }
    
    public boolean updateContent(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io) {
        return true;
    }
}
