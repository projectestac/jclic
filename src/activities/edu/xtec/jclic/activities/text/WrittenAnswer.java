/*
 * File    : WrittenAnswer.java
 * Created : 09-may-2001 11:15
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

package edu.xtec.jclic.activities.text;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class WrittenAnswer extends Activity implements ActiveBagContentKit.Compatible {
    
    int nonAssignedCells;
    boolean useIdAss;
    
    /** Creates new WrittenAnswer */
    public WrittenAnswer(JClicProject project) {
        super(project);
        boxGridPos=AB;
        abc=new ActiveBagContent[3];
        //for(int i=0; i<3; i++)
        //    abc[i]=null;
        scramble[0]=false;
        
        nonAssignedCells=0;
        invAss=false;
        useIdAss=true;
    }
    
    @Override
    public void initNew(){
        super.initNew();
        abc[0]=ActiveBagContent.initNew(3, 2, 'A');
        abc[1]=ActiveBagContent.initNew(3, 2, 'A');
    }
    
    protected static final String ANSWERS="answers", INVERSE="inverse";
    
    @Override
    public org.jdom.Element getJDomElement(){
        org.jdom.Element ex;
        
        if(abc[0]==null || abc[1]==null) return null;
        
        org.jdom.Element e=super.getJDomElement();
        
        e.addContent(abc[0].getJDomElement().setAttribute(ID, PRIMARY));
        e.addContent(abc[1].getJDomElement().setAttribute(ID, ANSWERS));
        if(abc[2]!=null)
            e.addContent(abc[2].getJDomElement().setAttribute(ID, SOLVED_PRIMARY));
        
        ex=new org.jdom.Element(SCRAMBLE);{
            ex.setAttribute(TIMES, Integer.toString(shuffles));
            ex.setAttribute(PRIMARY, JDomUtility.boolString(scramble[0]));
            e.addContent(ex);
        }
        
        ex=new org.jdom.Element(LAYOUT);
        ex.setAttribute(POSITION, LAYOUT_NAMES[boxGridPos]);
        e.addContent(ex);
        
        if(invAss)
            e.setAttribute(INVERSE, JDomUtility.boolString(invAss));
        
        return e;
    }
    
    @Override
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        org.jdom.Element child;
        super.setProperties(e, aux);
        ActiveBagContent bag;
        abc[2]=null;
        java.util.Iterator itr = e.getChildren(ActiveBagContent.ELEMENT_NAME).iterator();
        while (itr.hasNext()){
            child=((org.jdom.Element)itr.next());
            bag=ActiveBagContent.getActiveBagContent(child, project.mediaBag);
            String id=JDomUtility.getStringAttr(child, ID, PRIMARY, false);
            if(PRIMARY.equals(id)) 
                abc[0]=bag;
            else if(ANSWERS.equals(id)) 
                abc[1]=bag;
            else if(SOLVED_PRIMARY.equals(id)) 
                abc[2]=bag;
        }
        if(abc[0]==null || abc[1]==null)
            throw new IllegalArgumentException("WrittenAnswer without content!");
        
        if((child=e.getChild(SCRAMBLE))!=null){
            shuffles=JDomUtility.getIntAttr(child, TIMES, shuffles);
            scramble[0]=JDomUtility.getBoolAttr(child, PRIMARY, scramble[0]);
        }
        
        if((child=e.getChild(LAYOUT))!=null)
            boxGridPos=JDomUtility.getStrIndexAttr(child, POSITION, LAYOUT_NAMES, boxGridPos);
        
        invAss=JDomUtility.getBoolAttr(e, INVERSE, invAss);
        
        abc[0].avoidAllIdsNull(abc[1].getNumCells());
        
    }
    
    @Override
    public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception{
        super.setProperties(c3a);
        // Clic3 problem
        c3a.nctxw=c3a.ncw;
        c3a.nctxh=c3a.nch;
        boxGridPos=c3a.graPos;
        for(int i=0; i<2; i++){
            abc[i]=c3a.createActiveBagContent(i);
            abc[i].setBoxBase(c3a.getBoxBase(i));
        }
        scramble[0]=c3a.bar[0];
        abc[2]=c3a.sol ? c3a.createActiveBagContent(2) : null;
        
        abc[0].avoidAllIdsNull(abc[1].getNumCells());
    }
    
    public int getMinNumActions(){
        if(abc[0]==null || abc[1]==null) 
            return 0;        
        if(invAss) 
            return abc[1].getNumCells();
        else 
            return abc[0].getNumCells()-nonAssignedCells;
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
    public boolean needsKeyboard(){
        return true;
    }

    
    public Activity.Panel getActivityPanel(PlayStation ps) {
        return new Panel(ps);
    }
    
    class Panel extends Activity.Panel implements java.awt.event.ActionListener {
        
        JTextField textField;
        ActiveBoxBag[] bg=new ActiveBoxBag[2];
        int currentCell;
        boolean[] invAssCheck;
        
        protected Panel(PlayStation ps){
            super(ps);
            for(int i=0; i<2; i++)
                bg[i]=null;
            currentCell=-1;
            textField=null;
            //<<
            invAssCheck=null;
            //>>
        }
        
        public void clear(){
            for(int i=0; i<2; i++)
                if(bg[i]!=null){
                    bg[i].end();
                    bg[i]=null;
                }
            textField=null;
        }
        
        @Override
        public void buildVisualComponents() throws Exception{
            
            if(firstRun)
                super.buildVisualComponents();
            
            ActiveBox bx;
            currentCell=-1;
            
            clear();
            
            if(abc[0]!=null && abc[1]!=null){
                
                if(acp!=null)
                    acp.generateContent(new ActiveBagContentKit(abc[0].nch, abc[0].ncw, abc, true), ps);

                if(invAss){
                    invAssCheck=new boolean[abc[1].getNumCells()];
                    for(int i=0; i<invAssCheck.length; i++)
                        invAssCheck[i]=false;
                }
                
                bg[0]=ActiveBoxGrid.createEmptyGrid(null, this, margin, margin, abc[0]);
                // Clic3 behavior!!!
                double w=abc[1].w;
                if(boxGridPos==AUB || boxGridPos==BUA)
                    w=abc[0].getTotalWidth();
                bg[1]=new ActiveBoxGrid(null, this, margin, margin, w, abc[1].h, new edu.xtec.jclic.shapers.Rectangular(1, 1), abc[1].bb);
                
                textField=new JTextField(200);
                textField.setHorizontalAlignment(JTextField.CENTER);
                bx=bg[1].getActiveBox(0);
                bx.setInactive(false);
                bx.hasHostedComponent=true;
                bx.setHostedComponent(textField);
                textField.addActionListener(this);
                
                bg[0].setContent(abc[0], abc[2]);
                currentCell=0;
                
                bg[0].setDefaultIdAss();
                nonAssignedCells=0;
                for(int i=0; i<bg[0].getNumCells(); i++){
                    bx=bg[0].getActiveBox(i);
                    if(bx.idAss==-1){
                        nonAssignedCells++;
                        bx.switchToAlt(ps);
                    }
                }
                
                bg[0].setVisible(true);
                bg[1].setVisible(true);
                invalidate();
            }
        }
        
        @Override
        public void initActivity() throws Exception{
            super.initActivity();
            
            if(!firstRun)
                buildVisualComponents();
            else firstRun=false;
            
            //setCounters(0, 0, 0);
            setAndPlayMsg(MAIN, EventSounds.START);
            //ps.setMsg(messages[MAIN]);
            if(bg[0]!=null && bg[1]!=null){
                if(scramble[0])
                    shuffle(new ActiveBoxBag[] {bg[0]}, true, true);
                    //ps.playMsg();
                    //if(messages[MAIN]==null || messages[MAIN].mediaContent==null)
                    //    playEvent(EventSounds.START);
                    if(useOrder)
                        currentItem=bg[0].getNextItem(-1);
                    playing=true;
                    setCurrentCell(0);
            }
        }
        
        public void render(Graphics2D g2, Rectangle dirtyRegion) {
            for(int i=0; i<2; i++){
                if(bg[i]!=null) 
                    bg[i].update(g2, dirtyRegion, this);
            }
        }
        
        public Dimension setDimension(Dimension preferredMaxSize){
            return bg[0]==null || bg[1]==null || getSize().equals(preferredMaxSize)
            ? preferredMaxSize
            : BoxBag.layoutDouble(preferredMaxSize, bg[0], bg[1], boxGridPos, margin);
        }
        
        private boolean checkInvAss(){
            if(invAss==false || invAssCheck==null)
                return false;
            for(boolean b : invAssCheck)
                if(!b)
                    return false;
            return true;
        }
        
        private void setCurrentCell(int i){
            ActiveBox bx;
            boolean m=false;
            
            if(!playing) return;
            if(currentCell!=-1){
                boolean ok=false;
                bx=bg[0].getActiveBoxWithIdLoc(currentCell);
                String src=bx.getDescription();                
                bx.setMarked(false);
                //<<
                //String txCheck=abc[1].getActiveBoxContent(bx.idOrder).text;
                int id=bx.idAss;
                String txCheck = (id>=0 ? abc[1].getActiveBoxContent(id).text : "");
                //>>
                String txAnswer=textField.getText().trim();
                if(edu.xtec.util.StrUtils.compareMultipleOptions(txAnswer, txCheck, false)){
                    ok=true;
                    bx.idAss=-1;
                    
                    // 29-mai-2007
                    // When in multiple-answer, fill-in textField with
                    // the first valid option:
                    if(txCheck.indexOf('|')>=0)
                        textField.setText((new java.util.StringTokenizer(txCheck, "|")).nextToken());
                    
                    if(abc[2]!=null){
                        bx.switchToAlt(ps);
                        m=bx.playMedia(ps);                        
                    }
                    else
                        bx.clear();
                    if(invAss && id>=0 && id<invAssCheck.length){
                        invAssCheck[id]=true;
                    }
                    if(useOrder)
                        currentItem=bg[0].getNextItem(currentItem);
                }
                
                int cellsPlaced=bg[0].countCellsWithIdAss(-1);
                
                if(txAnswer.length()>0){
                    ps.reportNewAction(getActivity(), ACTION_WRITE, src, txAnswer, ok, cellsPlaced);
                }
                if(ok && (checkInvAss() || cellsPlaced==bg[0].getNumCells())){
                    finishActivity(true);
                    textField.setEnabled(false);
                    return;
                }
                else if(!m && txAnswer.length()>0)
                    playEvent(ok ? EventSounds.ACTION_OK : EventSounds.ACTION_ERROR);
            }
            
            if(useOrder)
                bx=(ActiveBox)bg[0].getBox(currentItem);
            else
                bx=bg[0].getActiveBoxWithIdLoc(i);
            if(bx==null || bx.idAss==-1){
                for(int j=0; j<bg[0].getNumCells(); j++){
                    bx=bg[0].getActiveBoxWithIdLoc(j);
                    if(bx.idAss!=-1) break;
                }
                if(bx!=null && bx.idAss==-1){
                    // error ?
                    finishActivity(false);
                    textField.setEnabled(false);
                    return;
                }
            }
            
            // 29 - mai -2007
            // Draw border only if it has more than one cell
            if(bg[0].getNumCells()>1 && bx!=null)
                bx.setMarked(true);
            if(bx!=null)
                currentCell=bx.idLoc;
            textField.setText("");
            textField.requestFocus();
            
            if(bx!=null)
                bx.playMedia(ps);
        }
        
        @Override
        public void requestFocus(){
            if(playing && textField!=null)
                textField.requestFocus();
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e){
            if(playing && e.getSource()==textField && currentCell!=-1)
                setCurrentCell(currentCell);
        }
        
        @Override
        public void processMouse(MouseEvent e){
            ActiveBox bx;
            boolean m=false;
            
            if(playing) switch(e.getID()){
                case MouseEvent.MOUSE_PRESSED:
                    ps.stopMedia(1);
                    if((bx=bg[0].findActiveBox(e.getPoint()))!=null){
                        if(bx.getContent()!=null && bx.getContent().mediaContent==null)
                            playEvent(EventSounds.CLICK);
                        setCurrentCell(bx.idLoc);
                    }
                    break;
            }
        }
        
        @Override
        public void showHelp(){
            if(!helpWindowAllowed() || bg[0]==null) return;
            
            HelpActivityComponent hac=null;
            if(showSolution){
                hac=new HelpActivityComponent(this){
                    ActiveBoxBag abb=null;
                    String currentResponse="";
                    int cellsPlaced=bg[0].countCellsWithIdAss(-1);
                    public void render(Graphics2D g2, Rectangle dirtyRegion){
                        if(abb!=null) abb.update(g2, dirtyRegion, this);
                    }
                    @Override
                    public void init(){
                        currentResponse=textField.getText();
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
                    public void end(){
                        super.end();
                        textField.setText(currentResponse);
                    }
                    @Override
                    public void processMouse(MouseEvent e){
                        ActiveBox bx;
                        if(abb!=null) switch(e.getID()){
                            case MouseEvent.MOUSE_PRESSED:
                                bx=abb.findActiveBox(e.getPoint());
                                if(bx!=null){
                                    boolean m=bx.playMedia(ps);
                                    String s=abc[1].getActiveBoxContent(bx.idOrder).text;
                                    if(s!=null)
                                        textField.setText(s.replace('|', ' '));
                                    ps.reportNewAction(getActivity(), ACTION_HELP, bx.getDescription(), null, false, cellsPlaced);
                                    if(!m)
                                        playEvent(EventSounds.CLICK);
                                }
                                break;
                            case MouseEvent.MOUSE_RELEASED:
                                unmarkBox();
                                textField.setText("");
                                break;
                        }
                    }
                };
                hac.init();
            }
            if(ps.showHelp(hac, helpMsg))
                ps.reportNewAction(getActivity(), ACTION_HELP, null, null, false, bg[0].countCellsWithIdAss(-1));
                
            if(hac!=null)
                hac.end();
        }        
    }
}
