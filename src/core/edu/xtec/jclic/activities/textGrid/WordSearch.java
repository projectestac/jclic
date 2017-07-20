/*
 * File    : WordSearch.java
 * Created : 09-sep-2001 19:24
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

package edu.xtec.jclic.activities.textGrid;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class WordSearch extends Activity implements ActiveBagContentKit.Compatible{
    
    String[] clues;
    int [] clueItems;
    
    /** Creates new WordSearch*/
    public WordSearch(JClicProject project)  {
        super(project);
        boxGridPos=AB;
        abc=new ActiveBagContent[1];
        clues=null;
        clueItems=null;
    }
    
    @Override
    public void initNew(){
        super.initNew();
        clues=new String[0];
        clueItems=new int[0];
        tgc=TextGridContent.initNew(3, 3, 'A');
    }
    
    @Override
    public org.jdom.Element getJDomElement(){
        org.jdom.Element ex, ex2;
        
        if(clues==null || tgc==null) return null;
        
        org.jdom.Element e=super.getJDomElement();
        
        e.addContent(tgc.getJDomElement());
        
        ex=new org.jdom.Element(CLUES);
        for(int i=0; i<clues.length; i++){
            ex2=new org.jdom.Element(CLUE);
            ex2.setAttribute(ID, Integer.toString(clueItems[i]));
            ex2.setText(clues[i]);
            ex.addContent(ex2);
        }
        e.addContent(ex);
        
        if(abc[0]!=null){
            e.addContent(abc[0].getJDomElement().setAttribute(ID, SECONDARY));
            ex=new org.jdom.Element(LAYOUT);
            ex.setAttribute(POSITION, LAYOUT_NAMES[boxGridPos]);
            e.addContent(ex);
            
            if(scramble[0]){
                ex=new org.jdom.Element(SCRAMBLE);
                ex.setAttribute(TIMES, Integer.toString(shuffles));
                ex.setAttribute(SECONDARY, JDomUtility.boolString(scramble[0]));
                e.addContent(ex);
            }
        }
        return e;
    }
    
    @Override
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        org.jdom.Element child, child2;
        
        super.setProperties(e, aux);
        
        if((child=e.getChild(TextGridContent.ELEMENT_NAME))==null)
            throw new IllegalArgumentException("WordSearch without TextGridContent!");
        
        tgc=TextGridContent.getTextGridContent(child);
        
        if((child=e.getChild(CLUES))==null)
            throw new IllegalArgumentException("WordSearch without clues!");
        
        java.util.List cluesList=child.getChildren(CLUE);
        int numClues=cluesList.size();
        clues=new String[numClues];
        clueItems=new int[numClues];
        for(int i=0; i<numClues; i++){
            child2=((org.jdom.Element)cluesList.get(i));
            clueItems[i]=JDomUtility.getIntAttr(child2, ID, i);
            clues[i]=child2.getText();
        }
        
        child=e.getChild(ActiveBagContent.ELEMENT_NAME);
        if(child!=null){
            if(!SECONDARY.equals(child.getAttributeValue(ID)))
                throw new IllegalArgumentException("WordSearch expects only \"secondary\" BagContent!");
            abc[0]=ActiveBagContent.getActiveBagContent(child, project.mediaBag);
            
            if((child=e.getChild(SCRAMBLE))!=null){
                shuffles=JDomUtility.getIntAttr(child, TIMES, shuffles);
                scramble[0]=JDomUtility.getBoolAttr(child, SECONDARY, scramble[0]);
            }
            else{
                scramble[0]=false;
            }
        }
        else abc[0]=null;
        
        if((child=e.getChild(LAYOUT))!=null)
            boxGridPos=JDomUtility.getStrIndexAttr(child, POSITION, LAYOUT_NAMES, boxGridPos);
        
    }
    
    @Override
    public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception{
        super.setProperties(c3a);
        boxGridPos=c3a.graPos;
        tgc=new TextGridContent();
        tgc.nch=c3a.nctxh;
        tgc.ncw=c3a.nctxw;
        tgc.w=c3a.txtCW;
        tgc.h=c3a.txtCH;
        tgc.border=c3a.delim[1];
        tgc.text=c3a.graTxt;
        clues=c3a.tags[0];
        clueItems=new int[clues.length];
        for(int i=0; i<clues.length; i++)
            clueItems[i]=i;
        tgc.bb=c3a.getBoxBase(0);
        
        // read second grid
        if(c3a.bar[0]){
            // clic3 problem
            c3a.txtCW=c3a.txtCW2;
            c3a.txtCH=c3a.txtCH2;
            // ---
            abc[0]=c3a.createActiveBagContent(0);
            abc[0].setBoxBase(c3a.getBoxBase(1));
        }
        else abc[0]=null;
    }
    
    public int getMinNumActions(){
        return clues.length;
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
    
    /** Getter for property clues.
     * @return Value of property clues.
     *
     */
    public String[] getClues() {
        return clues;
    }
    
    /** Setter for property clues.
     * @param cl New value of property "clues"
     * @param clItems New value of property "clueItems"
     *
     */
    public void setClues(String[] cl, int[] clItems) {
        clues = cl;
        if(clues==null)
            clues=new String[0];
        clueItems=clItems;
        if(clueItems==null || clueItems.length!=clues.length){
            clueItems=new int[clues.length];
            for(int i=0; i<clueItems.length; i++)
                clueItems[i]=i;
        }
        
    }
    
    class Panel extends Activity.Panel{
        TextGrid grid;
        ActiveBoxBag bgAlt;
        boolean [] resolvedClues;
        
        protected Panel(PlayStation ps){
            super(ps);
            bc=new BoxConnector(this);
            resolvedClues=new boolean[clues.length];
            grid=null;
            bgAlt=null;
        }
        
        public void clear(){
            if(grid!=null){grid.end();grid=null;}
            if(bgAlt!=null){bgAlt.end();bgAlt=null;}
        }
        
        @Override
        public void buildVisualComponents() throws Exception{
            
            if(firstRun) super.buildVisualComponents();
            
            clear();
            
            if(acp!=null && abc!=null)
                acp.generateContent(new ActiveBagContentKit(0, 0, abc, false), ps);            

            if(tgc!=null){
                grid=TextGrid.createEmptyGrid(null, this, margin, margin, tgc, false);
                //if(acp!=null) acp.generateContent(abc[0].nch, abc[0].ncw, abc, false, ac);
                
                if(abc[0]!=null)
                    bgAlt=ActiveBoxGrid.createEmptyGrid(null, this, margin, margin, abc[0]);
                
                grid.setVisible(true);
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
            if(grid!=null){
                grid.setChars(tgc.text);
                grid.randomize();
                grid.setAllCellsAttribute(TextGrid.INVERTED, false);
                
                for(int i=0; i<clueItems.length; i++)
                    resolvedClues[i]=false;
                
                if(bgAlt!=null){
                    bgAlt.setContent(abc[0]);
                    if(scramble[0])
                        shuffle(new ActiveBoxBag[]{bgAlt}, true, true);
                        bgAlt.setVisible(false);
                }
                
                //ps.playMsg();
                //if(messages[MAIN]==null || messages[MAIN].mediaContent==null)
                //    playEvent(EventSounds.START);
                playing=true;
            }
        }
        
        public int getCurrentScore(){
            int result=0;
            if(clues!=null)
                for(int i=0; i<clues.length; i++)
                    if(resolvedClues[i]) result++;
            return result;
        }
        
        public void render(Graphics2D g2, Rectangle dirtyRegion) {
            if(grid!=null) grid.update(g2, dirtyRegion, this);
            if(bgAlt!=null) bgAlt.update(g2, dirtyRegion, this);
            if(bc.active) bc.update(g2, dirtyRegion, this);
        }
        
        public Dimension setDimension(Dimension preferredMaxSize){
            if(grid==null || getSize().equals(preferredMaxSize))
                return preferredMaxSize;
            if(bgAlt!=null)
                return BoxBag.layoutDouble(preferredMaxSize, grid, bgAlt, boxGridPos, margin);
            else
                return BoxBag.layoutSingle(preferredMaxSize, grid, margin);
        }
        
        @Override
        public void processMouse(MouseEvent e){
            Point pt1, pt2;
            Point p=e.getPoint();
            boolean m=false;
            
            if(playing) switch(e.getID()){
                case MouseEvent.MOUSE_PRESSED:
                    ps.stopMedia(1);
                    if(bc.active){
                        bc.end();
                        pt1=grid.getLogicalCoords(bc.origin);
                        pt2=grid.getLogicalCoords(bc.dest);
                        if(pt1!=null && pt2!=null){
                            String s=grid.getStringBetween(pt1.x, pt1.y, pt2.x, pt2.y);
                            if(s!=null && s.length()>0){
                                boolean ok=false;
                                boolean repeated=false;
                                int c;
                                for(c=0; c<clues.length; c++)
                                    if(s.equals(clues[c])) {ok=true; break;}
                                if(ok && !(repeated=resolvedClues[c])){
                                    resolvedClues[c]=true;
                                    grid.setAttributeBetween(pt1.x, pt1.y, pt2.x, pt2.y, TextGrid.INVERTED, true);
                                    if(bgAlt!=null){
                                        int k=clueItems[c];
                                        if(k>=0 && k<bgAlt.getNumCells()){
                                            //ActiveBox bx=bgAlt.getActiveBoxWithIdLoc(clueItems[c]);
                                            ActiveBox bx=bgAlt.getActiveBox(clueItems[c]);
                                            if(bx!=null){
                                                bx.setVisible(true);
                                                m=bx.playMedia(ps);
                                            }
                                        }
                                    }
                                }
                                if(!repeated){
                                    int r=getCurrentScore();
                                    ps.reportNewAction(getActivity(), ACTION_SELECT, s, null, ok, r);
                                    if(r==clues.length)
                                        finishActivity(true);
                                    else if(!m)
                                        playEvent(ok ? EventSounds.ACTION_OK : EventSounds.ACTION_ERROR);
                                }
                                else if(!ok && !m)
                                    playEvent(EventSounds.ACTION_ERROR);
                            }
                            else playEvent(EventSounds.ACTION_ERROR);
                        }
                    }
                    else{
                        if(grid.contains(p)){
                            playEvent(EventSounds.CLICK);
                            bc.begin(p);
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
        
        @Override
        public void showHelp(){
            if(!helpWindowAllowed() || grid==null) return;
            
            HelpActivityComponent hac=null;
            if(showSolution){
                hac=new HelpActivityComponent(this){
                    JScrollPane scrollPane=null;
                    JList cluesList=null;
                    public void render(Graphics2D g2, Rectangle dirtyRegion){
                        // do nothing
                    }
                    @Override
                    public void init(){
                        cluesList=new JList(clues);
                        cluesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                        cluesList.addListSelectionListener(this);
                        scrollPane=new JScrollPane(cluesList);
                        add(scrollPane);
                        Dimension size=grid.getBounds().getSize();
                        scrollPane.setBounds(DEFAULT_MARGIN, DEFAULT_MARGIN, size.width, size.height);
                        size.width+=2*DEFAULT_MARGIN;
                        size.height+=2*DEFAULT_MARGIN;
                        setPreferredSize(size);
                        setMaximumSize(size);
                        setMinimumSize(size);
                        Point p=(Point)getClientProperty(HelpActivityComponent.PREFERRED_LOCATION);
                        if(p!=null) p.translate((int)grid.x-DEFAULT_MARGIN, (int)grid.y-DEFAULT_MARGIN);
                    }
                    @Override
                    public void doLayout(){
                        Rectangle r=getBounds();
                        if(scrollPane!=null)
                            scrollPane.setBounds(DEFAULT_MARGIN, DEFAULT_MARGIN, r.width-2*DEFAULT_MARGIN, r.height-2*DEFAULT_MARGIN);
                    }
                    @Override
                    public void valueChanged(javax.swing.event.ListSelectionEvent ev) {
                        if(bgAlt!=null && !ev.getValueIsAdjusting()){
                            int i=cluesList.getSelectedIndex();
                            if(i<0){
                                unmarkBox();
                            } else{
                                playEvent(EventSounds.CLICK);
                                markBox(bgAlt.getActiveBox(clueItems[i]), true);
                            }
                        }
                    }
                };
                hac.init();
            }
            if(ps.showHelp(hac, helpMsg))
                ps.reportNewAction(getActivity(), ACTION_HELP, null, null, false, getCurrentScore());
            if(hac!=null)
                hac.end();
        }        
    }    
}
