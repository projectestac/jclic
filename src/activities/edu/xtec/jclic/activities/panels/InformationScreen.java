/*
 * File    : InformationScreen.java
 * Created : 22-dec-2000 13:04
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

package edu.xtec.jclic.activities.panels;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class InformationScreen extends Activity implements ActiveBagContentKit.Compatible {
    
    /** Creates new InformationScreen */
    public InformationScreen(JClicProject project) {
        super(project);
        abc=new ActiveBagContent[1];
        includeInReports=false;
        reportActions=false;
    }
    
    @Override
    public void initNew(){
        super.initNew();
        abc[0]=ActiveBagContent.initNew(1, 1, 'A');
    }
    
    @Override
    public org.jdom.Element getJDomElement(){
        if(abc[0]==null) return null;
        org.jdom.Element e=super.getJDomElement();
        e.addContent(abc[0].getJDomElement());
        return e;
    }
    
    @Override
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        super.setProperties(e, aux);
        org.jdom.Element child=e.getChild(ActiveBagContent.ELEMENT_NAME);
        if(child!=null){
            abc[0]=ActiveBagContent.getActiveBagContent(child, project.mediaBag);
        }
        if(abc[0]==null)
            throw new IllegalArgumentException("InformationScreen without contents");
    }
    
    @Override
    public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception{
        super.setProperties(c3a);
        abc[0]=c3a.createActiveBagContent(0);
        abc[0].setBoxBase(c3a.getBoxBase(0));
        bTimeCounter=bScoreCounter=bActionsCounter=false;
        includeInReports=false;
        reportActions=false;
    }
    
    public int getMinNumActions(){
        return 0;
    }
    
    @Override
    public boolean hasRandom(){
        return true;
    }
    
    public Activity.Panel getActivityPanel(PlayStation ps) {
        return new Panel(ps);
    }
    
    class Panel extends Activity.Panel {
        
        ActiveBoxBag bg;
        
        protected Panel(PlayStation ps){
            super(ps);
            bg=null;
        }
        
        public void clear(){
            if(bg!=null){
                bg.end();
                bg=null;
            }
        }
        
        @Override
        public void buildVisualComponents() throws Exception{
            
            if(firstRun) super.buildVisualComponents();
            
            clear();
            
            if(abc[0]!=null){
                if(acp!=null)
                    acp.generateContent(new ActiveBagContentKit(abc[0].nch, abc[0].ncw, abc, false), ps);
                bg=ActiveBoxGrid.createEmptyGrid(null, this, margin, margin, abc[0]);
                bg.setContent(abc[0]);
                bg.setVisible(true);
                invalidate();
            }
        }
        
        @Override
        public void initActivity() throws Exception{
            super.initActivity();
            
            if(!firstRun) buildVisualComponents();
            else firstRun=false;
            
            setAndPlayMsg(MAIN, EventSounds.START);
            //ps.setMsg(messages[MAIN]);
            if(bg!=null){
                //ps.playMsg();
                //if(messages[MAIN]==null || messages[MAIN].mediaContent==null)
                //    playEvent(EventSounds.START);
                playing=true;
            }
        }
        
        public void render(Graphics2D g2, Rectangle dirtyRegion) {
            if(bg!=null)
                bg.update(g2, dirtyRegion, this);
        }
        
        public Dimension setDimension(Dimension preferredMaxSize){
            if(getSize().equals(preferredMaxSize)) return preferredMaxSize;
            return BoxBag.layoutSingle(preferredMaxSize, bg, margin);
        }
        
        @Override
        public void processMouse(MouseEvent e){
            ActiveBox bx;
            if(playing && e.getID()==MouseEvent.MOUSE_PRESSED){
                ps.stopMedia(1);
                if((bx=bg.findActiveBox(e.getPoint()))!=null){
                    if(!bx.playMedia(ps))
                        playEvent(EventSounds.CLICK);
                }
            }
        }       
    }    
}