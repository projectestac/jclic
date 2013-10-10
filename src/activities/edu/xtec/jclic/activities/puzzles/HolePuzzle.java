/*
 * File    : HolePuzzle.java
 * Created : 13-sep-2001 19:52
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

package edu.xtec.jclic.activities.puzzles;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import java.awt.*;
import java.awt.event.MouseEvent;


/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class HolePuzzle extends Activity implements ActiveBagContentKit.Compatible {
    
    /** Creates new ExchangePuzzle */
    public HolePuzzle(JClicProject project) {
        super(project);
        abc=new ActiveBagContent[1];
        boxGridPos=AB;
    }
    
    @Override
    public void initNew(){
        super.initNew();
        abc[0]=ActiveBagContent.initNew(3, 2, 'A');
    }
    
    @Override
    public org.jdom.Element getJDomElement(){
        org.jdom.Element ex;
        if(abc[0]==null)
            return null;
        
        org.jdom.Element e=super.getJDomElement();
        
        e.addContent(abc[0].getJDomElement());
        
        ex=new org.jdom.Element(SCRAMBLE);{
            ex.setAttribute(TIMES, Integer.toString(shuffles));
            e.addContent(ex);
        }
        
        ex=new org.jdom.Element(LAYOUT);
        ex.setAttribute(POSITION, LAYOUT_NAMES[boxGridPos]);
        e.addContent(ex);
        
        return e;
    }
    
    @Override
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        org.jdom.Element child;
        super.setProperties(e, aux);
        child=e.getChild(ActiveBagContent.ELEMENT_NAME);
        if(child!=null)
            abc[0]=ActiveBagContent.getActiveBagContent(child, project.mediaBag);
        if(abc[0]==null/* || abc[0].ncw*abc[0].nch<2*/)
            throw new IllegalArgumentException("Puzzle without content!");        
        
        if((child=e.getChild(SCRAMBLE))!=null)
            shuffles=JDomUtility.getIntAttr(child, TIMES, shuffles);
        
        if((child=e.getChild(LAYOUT))!=null)
            boxGridPos=JDomUtility.getStrIndexAttr(child, POSITION, LAYOUT_NAMES, boxGridPos);
    }
    
    @Override
    public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception{
        super.setProperties(c3a);
        abc[0]=c3a.createActiveBagContent(0);
        abc[0].setBoxBase(c3a.getBoxBase(0));
        boxGridPos=c3a.graPos;
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
    
    @Override
    public boolean shuffleAlways(){
        return true;
    }    
    
    public Activity.Panel getActivityPanel(PlayStation ps) {
        return new Panel(ps);
    }
    
    class Panel extends Activity.Panel {
        
        ActiveBoxGrid bg, parkBg;
        int hiddenBoxIndex;
        ActiveBox hiddenBox;
        
        
        protected Panel(PlayStation ps){
            super(ps);
            bg=parkBg=null;
            hiddenBoxIndex=0;
            hiddenBox=null;
        }
        
        public void clear(){
            if(bg!=null){bg.end(); bg=null;}
            if(parkBg!=null){parkBg.end(); parkBg=null;}
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
                hiddenBoxIndex=(int)(Math.random()*bg.getNumCells());
                hiddenBox=bg.getActiveBox(hiddenBoxIndex);
                hiddenBox.setVisible(false);
                parkBg=new ActiveBoxGrid(null, this, margin, margin, hiddenBox.width, hiddenBox.height, new edu.xtec.jclic.shapers.Rectangular(1, 1), abc[0].bb);
                parkBg.setContent(abc[0], null, hiddenBoxIndex, 0, 1);
                parkBg.setBorder(bg.hasBorder());
                parkBg.setVisible(true);
                invalidate();
            }
        }
        
        @Override
        public void initActivity() throws Exception{
            super.initActivity();
            
            if(!firstRun) buildVisualComponents();
            firstRun=false;
            
            setAndPlayMsg(MAIN, EventSounds.START);
            //ps.setMsg(messages[MAIN]);
            if(bg!=null){
                java.util.Random random=new java.util.Random();
                if(shuffles%2!=1) shuffles++;
                for(int i=0; i<shuffles; i++){
                    Point pth=bg.getCoord(hiddenBox);
                    int v=(random.nextBoolean() ? 1 : -1);
                    if(random.nextBoolean()){
                        pth.x+=v;
                        if(pth.x<0 || pth.x>=bg.nCols)
                            pth.x-=2*v;
                    } else{
                        pth.y+=v;
                        if(pth.y<0 || pth.y>=bg.nRows)
                            pth.y-=2*v;
                    }
                    ActiveBox dstBx=bg.getActiveBoxWithIdLoc(pth.y*bg.nCols+pth.x);
                    if(dstBx!=null)
                        hiddenBox.exchangeLocation(dstBx);
                }
                ps.setCounterValue(SCORE_COUNTER, bg.countCellsAtEquivalentPlace(true));
                //ps.playMsg();
                //if(messages[MAIN]==null || messages[MAIN].mediaContent==null)
                //    playEvent(EventSounds.START);
                playing=true;
            }
        }
        
        public void render(Graphics2D g2, Rectangle dirtyRegion) {
            if(bg!=null)
                bg.update(g2, dirtyRegion, this);
            if(parkBg!=null)
                parkBg.update(g2, dirtyRegion, this);
        }
        
        public Dimension setDimension(Dimension preferredMaxSize){
            if(bg==null || parkBg==null || getSize().equals(preferredMaxSize))
                return preferredMaxSize;
            return BoxBag.layoutDouble(preferredMaxSize, bg, parkBg, boxGridPos, margin);
        }
        
        @Override
        public void processMouse(MouseEvent e){
            ActiveBox bx;
            Point p=e.getPoint();
            
            if(playing) switch(e.getID()){
                case MouseEvent.MOUSE_PRESSED:
                    ps.stopMedia(1);
                    bx=bg.findActiveBox(p);
                    if(bx!=null){
                        if(bx.isVisible()){
                            Point pt=bg.getCoordDist(bx, hiddenBox);
                            if(Math.abs(pt.x)+Math.abs(pt.y)==1){
                                boolean m=bx.playMedia(ps);
                                String src=bx.getDescription()+"("+bx.idOrder+")";
                                String dest="("+hiddenBox.idLoc+")";
                                bx.exchangeLocation(hiddenBox);
                                boolean ok=(bx.idOrder==bx.idLoc);
                                int cellsAtPlace=bg.countCellsAtEquivalentPlace(true);
                                ps.reportNewAction(getActivity(), ACTION_SELECT, src, dest, ok, cellsAtPlace);
                                if(ok && cellsAtPlace==bg.getNumCells()){
                                    hiddenBox.setVisible(true);
                                    parkBg.setVisible(false);
                                    finishActivity(true);
                                }
                                else
                                    if(!m) playEvent(EventSounds.CLICK);
                            }
                        }
                        else
                            playEvent(EventSounds.ACTION_ERROR);
                    }
                    repaint();
                    break;
            }
        }
        
        @Override
        public void showHelp(){
            if(!helpWindowAllowed() || bg==null) return;
            
            HelpActivityComponent hac=null;
            if(showSolution){
                hac=new HelpActivityComponent(this){
                    ActiveBoxBag abb=null;
                    public void render(Graphics2D g2, Rectangle dirtyRegion){
                        if(abb!=null) abb.update(g2, dirtyRegion, this);
                    }
                    
                    @Override
                    public void init(){
                        abb=ActiveBoxGrid.createEmptyGrid(null, this, DEFAULT_MARGIN, DEFAULT_MARGIN, abc[0]);
                        abb.setContent(abc[0]);
                        abb.setVisible(true);
                        Dimension size=bg.getBounds().getSize();
                        abb.setBounds(DEFAULT_MARGIN, DEFAULT_MARGIN, size.width, size.height);
                        size.width+=2*DEFAULT_MARGIN;
                        size.height+=2*DEFAULT_MARGIN;
                        setPreferredSize(size);
                        setMaximumSize(size);
                        setMinimumSize(size);
                        Point p=(Point)getClientProperty(HelpActivityComponent.PREFERRED_LOCATION);
                        if(p!=null)
                            p.translate((int)bg.x-DEFAULT_MARGIN, (int)bg.y-DEFAULT_MARGIN);
                    }
                    
                    @Override
                    public void processMouse(MouseEvent e){
                        // no mouse
                    }
                };
                hac.init();
            }
            if(ps.showHelp(hac, helpMsg))
                ps.reportNewAction(getActivity(), ACTION_HELP, null, null, false, bg.countCellsAtEquivalentPlace(true));
            if(hac!=null)
                hac.end();
        }
        
    }
}