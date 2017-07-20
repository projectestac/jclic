/*
 * File    : ComplexAssociation.java
 * Created : 23-apr-2001 9:57
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

package edu.xtec.jclic.activities.associations;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import java.awt.event.MouseEvent;


/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class ComplexAssociation extends SimpleAssociation {
    
    int nonAssignedCells;
    
    /** Creates new ComplexAssociation */
    public ComplexAssociation(JClicProject project) {
        super(project);
        
        nonAssignedCells=0;
        invAss=false;
        useIdAss=true;
    }
    
    @Override
    public void initNew(){
        super.initNew();
        abc[0]=ActiveBagContent.initNew(3, 2, 'A', true, false, 50, 30);
        abc[1]=ActiveBagContent.initNew(3, 2, '1');
        //abc[2]=ActiveBagContent.initNew(3, 2, 'a');
    }
    
    protected static final String INVERSE="inverse";
    
    @Override
    public org.jdom.Element getJDomElement(){
        
        org.jdom.Element e=super.getJDomElement();
        
        if(e!=null)
            if(invAss) e.setAttribute(INVERSE, JDomUtility.boolString(invAss));
        
        return e;
    }
    
    @Override
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        super.setProperties(e, aux);
        invAss=JDomUtility.getBoolAttr(e, INVERSE, invAss);
        abc[0].avoidAllIdsNull(abc[1].getNumCells());
    }
    
    @Override
    public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception{
        super.setProperties(c3a);
        invAss=c3a.invAss;
        abc[0].setIds(c3a.ass);
    }
    
    @Override
    public int getMinNumActions(){
        if(abc[0]==null || abc[1]==null) return 0;
        if(invAss) return abc[1].getNumCells();
        else return abc[0].getNumCells()-nonAssignedCells;
    }
    
    @Override
    public Activity.Panel getActivityPanel(PlayStation ps) {
        return new Panel(ps);
    }
        
    class Panel extends SimpleAssociation.Panel {
        
        boolean [] invAssCheck;
        
        protected Panel(PlayStation ps){
            super(ps);
            invAssCheck=null;
        }
        
        @Override
        public void buildVisualComponents() throws Exception{
            
            if(firstRun) super.buildVisualComponents();
            
            clear();
            
            if(abc[0]!=null && abc[1]!=null){
                
                if(acp!=null)
                    acp.generateContent(new ActiveBagContentKit(abc[0].nch, abc[0].ncw, abc, true), ps);
                
                if(invAss){
                    invAssCheck=new boolean[abc[1].getNumCells()];
                    for(int i=0; i<invAssCheck.length; i++) invAssCheck[i]=false;
                }
                
                for(int i=0; i<2; i++)
                    bg[i]=ActiveBoxGrid.createEmptyGrid(null, this, margin, margin, abc[i]);
                
                bg[0].setContent(abc[0], abc[2]);
                bg[1].setContent(abc[1]);
                
                bg[0].setDefaultIdAss();
                nonAssignedCells=0;
                for(int i=0; i<bg[0].getNumCells(); i++){
                    ActiveBox bx=bg[0].getActiveBox(i);
                    if(bx.idAss==-1){
                        nonAssignedCells++;
                        bx.switchToAlt(ps);
                    }
                }
                
                for(int i=0; i<2; i++){
                    bg[i].setVisible(true);
                }
                invalidate();
            }
        }
        
        private boolean checkInvAss(){
            int i;
            if(invAss==false || invAssCheck==null) return false;
            for(i=0; i<invAssCheck.length; i++) if(!invAssCheck[i]) break;
            return i==invAssCheck.length;
        }
        
        @Override
        public void processMouse(MouseEvent e){
            ActiveBox bx1, bx2;
            java.awt.Point p=e.getPoint();
            boolean m=false;
            
            if(playing) switch(e.getID()){
                case MouseEvent.MOUSE_PRESSED:
                    ps.stopMedia(1);
                    if(bc.active){
                        boolean clickOnBg0=false;
                        bc.end();
                        if((bx1=bg[0].findActiveBox(bc.origin))!=null){
                            bx2=bg[1].findActiveBox(/*bc.dest*/p);
                        }
                        else if((bx2=bg[1].findActiveBox(bc.origin))!=null){
                            bx1=bg[0].findActiveBox(/*bc.dest*/p);
                            clickOnBg0=true;
                        }
                        if(bx1!=null && bx2!=null && bx1.idAss!=-1 && !bx2.isInactive()){
                            boolean ok=false;
                            String src=bx1.getDescription();
                            String dest=bx2.getDescription();
                            //ac.incCounterValue(ActivityContainer.ACTIONS_COUNTER);
                            if(bx1.idAss==bx2.idOrder ||
                            bx2.getContent().isEquivalent(abc[1].getActiveBoxContent(bx1.idAss), true)){
                                ok=true;
                                bx1.setIdAss(-1);
                                if(abc[2]!=null){
                                    bx1.switchToAlt(ps);
                                    m|=bx1.playMedia(ps);
                                }
                                else{
                                    if(clickOnBg0) m|=bx1.playMedia(ps);
                                    else m|=bx2.playMedia(ps);
                                    bx1.clear();
                                }
                                if(invAss){
                                    invAssCheck[bx2.idOrder]=true;
                                    bx2.clear();
                                }
                                if(useOrder)
                                    currentItem=bg[0].getNextItem(currentItem);
                            }
                            int cellsOk=bg[0].countCellsWithIdAss(-1);
                            ps.reportNewAction(getActivity(), ACTION_MATCH, src, dest, ok, cellsOk-nonAssignedCells);
                            if(ok && (checkInvAss() || cellsOk==bg[0].getNumCells())) finishActivity(true);
                            else if(!m) playEvent(ok ? EventSounds.ACTION_OK : EventSounds.ACTION_ERROR);
                        }
                        else if((clickOnBg0 && bg[0].contains(p)) || (!clickOnBg0 && bg[1].contains(p))){
                            // click on grid, out of cell
                            String src=(bx1!=null ? bx1.getDescription() : bx2!=null ? bx2.getDescription() : "null");
                            ps.reportNewAction(getActivity(), ACTION_MATCH, src, "null", false, bg[1].countCellsWithIdAss(-1));
                            playEvent(EventSounds.ACTION_ERROR);
                        }
                    }
                    else{
                        //if(((bx1=bg[0].findActiveBox(p))!=null && bx1.idAss!=-1) ||
                        //(bx1=bg[1].findActiveBox(p))!=null){
                        if(((bx1=bg[0].findActiveBox(p))!=null && bx1.idAss!=-1 && (!useOrder || bx1.idOrder==currentItem))
                        ||(!useOrder && (bx1=bg[1].findActiveBox(p))!=null)){
                            if(dragCells)
                                bc.begin(p, bx1);
                            else
                                bc.begin(p);
                            m|=bx1.playMedia(ps);
                            if(!m) playEvent(EventSounds.CLICK);
                        }
                    }
                    break;
                    
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED:
                    if(bc.active){
                        bc.moveTo(p);
                    }
                    break;
            }
        }
    }
}
