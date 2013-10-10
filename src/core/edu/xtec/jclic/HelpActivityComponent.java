/*
 * File    : HelpActivityComponent.java
 * Created : 16-aug-2001 1:32
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

package edu.xtec.jclic;

import edu.xtec.jclic.boxes.AbstractBox;
import edu.xtec.jclic.boxes.ActiveBox;
import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * JClic activities have the pottibility to show a dialog window containig an
 * object with help contents. Activities should call
 * the <CODE>ShowHelp</CODE> method of {@link edu.xtec.jclic.PlayStation} to
 * make this help window appear. The abstract class <CODE>HelpActivityComponent</CODE>
 * can be used as a base class for this kind of help objects.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.23
 */
public abstract class HelpActivityComponent extends JPanel implements ListSelectionListener{
    
    /**
     * Key used to store the preferred location property.
     */
    public static final String PREFERRED_LOCATION="prefLoc";
    /**
     * The {@link edu.xtec.jclic.Activity.Panel} that this object belongs to.
     */
    public Activity.Panel ap;
    /**
     * The currently selected box in the <CODE>HelpActivityComponent</CODE>, if any.
     */
    public AbstractBox markedBox;
    public boolean markedBoxWasVisible;
    
    /** Creates new HelpActivityComponent */
    public HelpActivityComponent(Activity.Panel setAct) {
        super();
        ap=setAct;
        markedBox=null;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        putClientProperty(PREFERRED_LOCATION, new Point(ap.getLocationOnScreen()));
    }
    
    public void init(){}
    public void end(){
        unmarkBox();
    }
    
    public abstract void render(Graphics2D g2, Rectangle dirtyRegion);
    
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        RenderingHints rh=g2.getRenderingHints();
        g2.setRenderingHints(Constants.DEFAULT_RENDERING_HINTS);
        super.paintComponent(g2);
        render(g2, g2.getClipBounds());
        g2.setRenderingHints(rh);
    }
    
    public void unmarkBox(){
        ap.ps.stopMedia(1);
        if(markedBox!=null){
            markedBox.setMarked(!markedBox.isMarked());
            markedBox.setInverted(!markedBox.isInverted());
            markedBox.setVisible(markedBoxWasVisible);
            markedBox=null;
        }
    }
    
    public void markBox(AbstractBox bx, boolean play){
        unmarkBox();
        markedBox=bx;
        if(bx!=null){
            markedBoxWasVisible=bx.isVisible();
            if(play && bx instanceof ActiveBox) ((ActiveBox)bx).playMedia(ap.ps);
            bx.setMarked(!bx.isMarked());
            bx.setInverted(!bx.isInverted());
            bx.setVisible(true);
        }
    }
    
    public void processMouse(MouseEvent p1){}
    
    @Override
    protected void processEvent(AWTEvent e){
        if(e instanceof MouseEvent)
            processMouse((MouseEvent)e);
        super.processEvent(e);
    }
    
    public void valueChanged(ListSelectionEvent ev) {
    }
    
    @Override
    public void doLayout(){
    }
    
}
