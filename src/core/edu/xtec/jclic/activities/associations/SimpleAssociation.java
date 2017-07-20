/*
 * File    : SimpleAssociation.java
 * Created : 20-apr-2001 17:40
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
import edu.xtec.jclic.clic3.Clic3Activity;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Iterator;

/**
 * This activity has two {@link edu.xtec.jclic.bags.MediaBag} objects with the same number of elements.
 * The elements are linked one to one (first A to first B, second A to second B,
 * etc).
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 * @see Activity
 */
public class SimpleAssociation extends Activity implements ActiveBagContentKit.Compatible {
    
    // for internal use
    boolean useIdAss;
    
    /**
     * Creates new simpleAssociation
     * @param project The project to wich this activity belongs.
     */
    public SimpleAssociation(JClicProject project) {
        super(project);
        boxGridPos=AB;
        scramble[0]=true;
        scramble[1]=true;
        abc=new ActiveBagContent[3];
        //for(int i=0; i<3; i++)
        //    abc[i]=null;
        useIdAss=false;
    }
    
    /**
     * Initialisation method for newly created activities.
     */    
    @Override
    public void initNew(){
        super.initNew();
        abc[0]=ActiveBagContent.initNew(3, 2, 'A');
        abc[1]=ActiveBagContent.initNew(3, 2, '1');
        //abc[2]=ActiveBagContent.initNew(3, 2, 'a');
    }
    
    /**
     * Creates a JDom Element that represents the current object properties, in order
     * to allow it to be stored into a XML files.
     * @return A JDom element representing the activity properties.
     */    
    @Override
    public org.jdom.Element getJDomElement(){
        
        org.jdom.Element ex;
        
        if(abc[0]==null || abc[1]==null)
            return null;
        
        org.jdom.Element e=super.getJDomElement();
        
        e.addContent(abc[0].getJDomElement().setAttribute(ID, PRIMARY));
        e.addContent(abc[1].getJDomElement().setAttribute(ID, SECONDARY));
        if(abc[2]!=null)
            e.addContent(abc[2].getJDomElement().setAttribute(ID, SOLVED_PRIMARY));
        
        ex=new org.jdom.Element(SCRAMBLE);
        ex.setAttribute(TIMES, Integer.toString(shuffles));
        ex.setAttribute(PRIMARY, JDomUtility.boolString(scramble[0]));
        ex.setAttribute(SECONDARY, JDomUtility.boolString(scramble[1]));
        e.addContent(ex);
        
        ex=new org.jdom.Element(LAYOUT);
        ex.setAttribute(POSITION, LAYOUT_NAMES[boxGridPos]);
        e.addContent(ex);
        
        return e;
    }
    
    /**
     * Applies to this object the properties previously stored into a JDom Element
     * usually created by means of a call to the {@link #getJDomElement()} method).
     * @param e The JDom element cointaining the data.
     * @param aux Auxiliary object. Unused here, but has to be {@link edu.xtec.util.Domable}.
     * @throws Exception If something goes wrong.
     */    
    @Override
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        
        org.jdom.Element child;
        super.setProperties(e, aux);
        ActiveBagContent bag;
        abc[2]=null;
        
        Iterator itr = e.getChildren(ActiveBagContent.ELEMENT_NAME).iterator();
        while (itr.hasNext()){
            child=((org.jdom.Element)itr.next());
            bag=ActiveBagContent.getActiveBagContent(child, project.mediaBag);
            String id=JDomUtility.getStringAttr(child, ID, PRIMARY, false);
            if(PRIMARY.equals(id))
                abc[0]=bag;
            else if(SECONDARY.equals(id))
                abc[1]=bag;
            else if(SOLVED_PRIMARY.equals(id))
                abc[2]=bag;
        }
        if(abc[0]==null || abc[1]==null)
            throw new IllegalArgumentException("Association without contents!");
        
        if((child=e.getChild(SCRAMBLE))!=null){
            shuffles=JDomUtility.getIntAttr(child, TIMES, shuffles);
            scramble[0]=JDomUtility.getBoolAttr(child, PRIMARY, scramble[0]);
            scramble[1]=JDomUtility.getBoolAttr(child, SECONDARY, scramble[1]);
        }
        
        if((child=e.getChild(LAYOUT))!=null)
            boxGridPos=JDomUtility.getStrIndexAttr(child, POSITION, LAYOUT_NAMES, boxGridPos);
        
    }
    
    @Override
    public void setProperties(Clic3Activity c3a) throws Exception{
        
        super.setProperties(c3a);
        // Clic3 problem
        if(getClass().getSuperclass()==Activity.class){
            c3a.nctxw=c3a.ncw;
            c3a.nctxh=c3a.nch;
        }
        // -----
        boxGridPos=c3a.graPos;
        for(int i=0; i<2; i++){
            abc[i]=c3a.createActiveBagContent(i);
            abc[i].setBoxBase(c3a.getBoxBase(i));
            scramble[i]=c3a.bar[i];
        }
        abc[2]=c3a.sol ? c3a.createActiveBagContent(2) : null;
    }
    
    public int getMinNumActions(){
        return abc[0]==null ? 0 : abc[0].getNumCells();
    }
    
    @Override
    public boolean helpSolutionAllowed(){
        return true;
    }
    
    @Override
    public boolean hasRandom(){
        return true;
    }
    
    public Activity.Panel getActivityPanel(PlayStation ps) {
        return new Panel(ps);
    }
    
    class Panel extends Activity.Panel {
        
        ActiveBoxBag[] bg=new ActiveBoxBag[2];
        
        protected Panel(PlayStation ps){
            super(ps);
            bc=new BoxConnector(this);
            for(int i=0; i<2; i++){
                bg[i]=null;
            }
            currentItem=0;
        }
        
        public void clear(){
            for(int i=0; i<2; i++)
                if(bg[i]!=null){
                    bg[i].end();
                    bg[i]=null;
                }
        }
        
        @Override
        public void buildVisualComponents() throws Exception {
            
            if(firstRun) super.buildVisualComponents();
            
            clear();
            
            if(abc[0]!=null && abc[1]!=null){
                
                if(acp!=null)
                    acp.generateContent(new ActiveBagContentKit(abc[0].nch, abc[0].ncw, abc, false), ps);
                
                for(int i=0; i<2; i++)
                    bg[i]=ActiveBoxGrid.createEmptyGrid(null, this, margin, margin, abc[i]);
                
                for(int i=0; i<2; i++){
                    bg[i].setContent(abc[i], i==0 ? abc[2] : null);
                    bg[i].setVisible(true);
                }
                invalidate();
            }
        }
        
        @Override
        public void initActivity() throws Exception{
            
            super.initActivity();
            
            if(!firstRun)
                buildVisualComponents();
            else
                firstRun=false;
            
            //ps.setMsg(messages[MAIN]);
            
            setAndPlayMsg(MAIN, EventSounds.START);
            
            if(bg[0]!=null && bg[1]!=null){
                if(scramble[0] || scramble[1])
                    shuffle(new ActiveBoxBag[]{scramble[0] ? bg[0]:null, scramble[1] ? bg[1]:null}, true, true);
                    //ps.playMsg();
                    //if(messages[MAIN]==null || messages[MAIN].mediaContent==null)
                    //    playEvent(EventSounds.START);
                    if(useOrder)
                        currentItem=bg[0].getNextItem(-1);
                    playing=true;
            }
        }
        
        public void render(Graphics2D g2, Rectangle dirtyRegion) {
            for(int i=0; i<2; i++)
                if(bg[i]!=null)
                    bg[i].update(g2, dirtyRegion, this);
            
            if(bc.active)
                bc.update(g2, dirtyRegion, this);
        }
        
        public Dimension setDimension(Dimension preferredMaxSize){
            if(bg[0]==null || bg[1]==null || getSize().equals(preferredMaxSize))
                return preferredMaxSize;
            return BoxBag.layoutDouble(preferredMaxSize, bg[0], bg[1], boxGridPos, margin);
        }
        
        @Override
        public void processMouse(MouseEvent e){
            
            ActiveBox bx1, bx2;
            Point p=e.getPoint();
            boolean m=false;
            
            if(playing) switch(e.getID()){
                case MouseEvent.MOUSE_PRESSED:
                    ps.stopMedia(1);
                    if(bc.active){
                        boolean clickOnBg0=false;
                        bc.end();
                        if((bx1=bg[0].findActiveBox(bc.origin))!=null)
                            bx2=bg[1].findActiveBox(/*bc.dest*/p);
                        else if((bx2=bg[1].findActiveBox(bc.origin))!=null){
                            bx1=bg[0].findActiveBox(/*bc.dest*/p);
                            clickOnBg0=true;
                        }
                        if(bx1!=null && bx2!=null && bx1.idAss!=-1 && bx2.idAss!=-1){
                            boolean ok=false;
                            String src=bx1.getDescription();
                            String dest=bx2.getDescription();
                            if(bx1.idOrder==bx2.idOrder
                            ||(bx2.getContent().isEquivalent(abc[1].getActiveBoxContent(bx1.idOrder), true))){
                                ok=true;
                                bx1.setIdAss(-1);
                                bx2.setIdAss(-1);
                                if(abc[2]!=null){
                                    bx1.switchToAlt(ps);
                                    m|=bx1.playMedia(ps);
                                }
                                else{
                                    if(clickOnBg0) m|=bx1.playMedia(ps);
                                    else m|=bx2.playMedia(ps);
                                    bx1.clear();
                                }
                                bx2.clear();
                                if(useOrder)
                                    currentItem=bg[0].getNextItem(currentItem);
                            }
                            int cellsPlaced=bg[1].countCellsWithIdAss(-1);
                            ps.reportNewAction(getActivity(), ACTION_MATCH, src, dest, ok, cellsPlaced);
                            if(ok && cellsPlaced==bg[1].getNumCells())
                                finishActivity(true);
                            else if(!m)
                                playEvent(ok ? EventSounds.ACTION_OK : EventSounds.ACTION_ERROR);
                        }
                    }
                    else{
                        if(((bx1=bg[0].findActiveBox(p))!=null && (!useOrder || bx1.idOrder==currentItem))
                        ||(!useOrder && (bx1=bg[1].findActiveBox(p))!=null) && bx1.idAss!=-1){
                            if(dragCells)
                                bc.begin(p, bx1);
                            else
                                bc.begin(p);
                            m=bx1.playMedia(ps);
                            if(!m) playEvent(EventSounds.CLICK);
                        }
                    }
                    break;
                    
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED:
                    if(bc.active)
                        bc.moveTo(p);
                    break;
            }
        }
        
        @Override
        public void showHelp(){
            
            if(!helpWindowAllowed() || bg[0]==null)
                return;
            
            HelpActivityComponent hac=null;
            if(showSolution){
                hac=new HelpActivityComponent(this){
                    ActiveBoxBag abb=null;
                    int cellsPlaced=bg[1].countCellsWithIdAss(-1);
                    public void render(Graphics2D g2, Rectangle dirtyRegion){
                        if(abb!=null) abb.update(g2, dirtyRegion, this);
                    }
                    @Override
                    public void init(){
                        abb=(ActiveBoxBag)bg[0].clone();
                        abb.setContainer(this);
                        Dimension size=abb.getBounds().getSize();
                        abb.setBounds(DEFAULT_MARGIN, DEFAULT_MARGIN, size.width, size.height);
                        size.width+=2*DEFAULT_MARGIN;
                        size.height+=2*DEFAULT_MARGIN;
                        setPreferredSize(size);
                        setMaximumSize(size);
                        setMinimumSize(size);
                        Point p=(Point)getClientProperty(HelpActivityComponent.PREFERRED_LOCATION);
                        if(p!=null)
                            p.translate((int)bg[0].x-DEFAULT_MARGIN, (int)bg[0].y-DEFAULT_MARGIN);
                    }
                    @Override
                    public void processMouse(MouseEvent e){
                        ActiveBox bx;
                        boolean m;
                        if(abb!=null) switch(e.getID()){
                            case MouseEvent.MOUSE_PRESSED:
                                bx=abb.findActiveBox(e.getPoint());
                                if(bx!=null){
                                    m=bx.playMedia(ps);
                                    if(!m) playEvent(EventSounds.CLICK);
                                    if(bx.idAss>=0){
                                        ActiveBox bxSolution=bg[1].getActiveBox(useIdAss ? bx.idAss : bx.idOrder);
                                        markBox(bxSolution, false);
                                        if(bxSolution!=null)
                                            ps.reportNewAction(getActivity(), ACTION_HELP, bx.getDescription(), bxSolution.getDescription(), false, cellsPlaced);
                                    }
                                }
                                break;
                            case MouseEvent.MOUSE_RELEASED:
                                unmarkBox();
                                break;
                        }
                    }
                };
                hac.init();
            }
            if(ps.showHelp(hac, helpMsg))
                ps.reportNewAction(getActivity(), ACTION_HELP, null, null, false, bg[1].countCellsWithIdAss(-1));
            
            if(hac!=null)
                hac.end();
        }                
    }
}
