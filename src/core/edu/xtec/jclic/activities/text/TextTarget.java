/*
 * File    : TextTarget.java
 * Created : 31-may-2001 16:55
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

import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.MediaContent;
import edu.xtec.util.JDomUtility;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class TextTarget extends java.lang.Object{
    
    //enum TIPUS_INCOGNITA {ESCRITA, LLISTA};
    public static final int NO_INFO=0, INFO_ALWAYS=1, INFO_ON_ERROR=2, INFO_ON_DEMAND=3;
    public static final String[] infoTypes={"no_info", "always", "onError", "onDemand"};
    public static final int NOT_EDITED=0, EDITED=1, SOLVED=2, WITH_ERROR=3;
    public static final int INFO_LEFTALIGN=1, INFO_ONLYPLAY=2;
    
    public boolean isList;
    public int numIniChars;
    public char iniChar;
    public int maxLenResp;
    public String[] answer;
    public String[] options;
    public String iniText;
    public int infoMode;
    public ActiveBoxContent popupContent;
    public int popupDelay;
    public int popupMaxTime;
    public boolean onlyPlay;
    
    // TRANSIENT PROPERTIES
    protected TargetCombo comboList;
    public int targetStatus;
    private boolean flagModified;
    TextActivityPane parentPane;
    
    /** Creates new TextTarget */
    public TextTarget() {
        isList=false;
        numIniChars=1;
        iniChar='_';
        maxLenResp=0;
        answer=null;
        options=null;
        iniText=null;
        infoMode=NO_INFO;
        popupContent=null;
        popupDelay=0;
        popupMaxTime=0;
        onlyPlay=false;
        comboList=null;
        targetStatus=NOT_EDITED;
        flagModified=false;
    }
    
    public TextTarget(Document doc, int x0, int x1){
        this();
        if(x1<x0){
            int v=x0;
            x0=x1;
            x1=v;
        }
        String s;
        try{
            s=doc.getText(x0, x1-x0);
        } catch(BadLocationException ex){
            // should not occur!
            s="";
        }
        numIniChars=s.length();
        answer=new String[]{s};
        maxLenResp=numIniChars;
    }
    
    public void reset(){
        targetStatus=NOT_EDITED;
        flagModified=false;
        if(comboList!=null)
            comboList.checkColors();
    }
    
    public static final String ELEMENT_NAME="target";
    public static final String ANSWER="answer",
    OPTION_LIST="optionList", OPTION="option",
    RESPONSE="response", FILL="fill", INI_LEN="length", MAX_LEN="maxLength", INI_TEXT="show",
    INFO="info", MODE="mode", DELAY="delay", MAX_TIME="maxTime";
    
    public org.jdom.Element getJDomElement(){
        
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        org.jdom.Element child;
        if(answer!=null){
            for(String ans : answer)
                e.addContent((new org.jdom.Element(ANSWER)).addContent(ans));
        }
        if(isList){
            child=new org.jdom.Element(OPTION_LIST);
            if(options!=null){
                for(String opt : options)
                    child.addContent((new org.jdom.Element(OPTION)).addContent(opt));
            }
            e.addContent(child);
        } else{
            child=new org.jdom.Element(RESPONSE);
            child.setAttribute(FILL, new String(new char[]{iniChar}));
            child.setAttribute(INI_LEN, Integer.toString(numIniChars));
            child.setAttribute(MAX_LEN, Integer.toString(maxLenResp));
            if(iniText!=null && iniText.length()>0) child.setAttribute(INI_TEXT, iniText);
            e.addContent(child);
        }
        
        if(popupContent!=null && infoMode!=NO_INFO){
            child=new org.jdom.Element(INFO);
            if(infoMode!=INFO_ALWAYS) child.setAttribute(MODE, infoTypes[infoMode]);
            if(popupDelay>0) child.setAttribute(DELAY, Integer.toString(popupDelay));
            if(popupMaxTime>0) child.setAttribute(MAX_TIME, Integer.toString(popupMaxTime));
            if(onlyPlay && popupContent.mediaContent!=null){
                child.addContent(popupContent.mediaContent.getJDomElement());
            } else{
                child.addContent(popupContent.getJDomElement());
            }
            e.addContent(child);
        }        
        return e;
    }
    
    public static TextTarget getTextTarget(org.jdom.Element e, MediaBag mediaBag) throws Exception{
        TextTarget tt=new TextTarget();
        tt.setProperties(e, mediaBag);
        return tt;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{        
        JDomUtility.checkName(e, ELEMENT_NAME);
        MediaBag mediaBag=(MediaBag)aux;
        org.jdom.Element child, child2;
        
        java.util.List answerLst=e.getChildren(ANSWER);
        if(!answerLst.isEmpty()){
            answer=new String[answerLst.size()];
            for(int i=0; i<answer.length; i++)
                answer[i]=((org.jdom.Element)(answerLst.get(i))).getText();
        }
        if((child=e.getChild(OPTION_LIST))!=null){
            java.util.List optionLst=child.getChildren(OPTION);
            if(!optionLst.isEmpty()){
                isList=true;
                options=new String[optionLst.size()];
                for(int i=0; i<options.length; i++)
                    options[i]=((org.jdom.Element)(optionLst.get(i))).getText();
            }
        }
        if((child=e.getChild(RESPONSE))!=null){
            String s=new String(new char[]{iniChar});
            s=JDomUtility.getStringAttr(child, FILL, s, true);
            if(s.length()>0) iniChar=s.charAt(0);
            numIniChars=JDomUtility.getIntAttr(child, INI_LEN, numIniChars);
            maxLenResp=JDomUtility.getIntAttr(child, MAX_LEN, maxLenResp);
            iniText=child.getAttributeValue(INI_TEXT);
        }
        if((child=e.getChild(INFO))!=null){
            infoMode=JDomUtility.getStrIndexAttr(child, MODE, infoTypes, INFO_ALWAYS);
            popupDelay=JDomUtility.getIntAttr(child, DELAY, popupDelay);
            popupMaxTime=JDomUtility.getIntAttr(child, MAX_TIME, popupMaxTime);
            if((child2=child.getChild(MediaContent.ELEMENT_NAME))!=null){
                onlyPlay=true;
                popupContent=new ActiveBoxContent();
                popupContent.mediaContent=MediaContent.getMediaContent(child2);
            }
            else if((child2=child.getChild(ActiveBoxContent.ELEMENT_NAME))!=null){
                popupContent=ActiveBoxContent.getActiveBoxContent(child2, mediaBag);
            }
        }        
    }
    
    protected void setParentPane(TextActivityPane pane){
        parentPane=pane;
    }
    
    public void setAnswer(String text){
        java.util.StringTokenizer st=new java.util.StringTokenizer(text, "|");
        int numItems=st.countTokens();
        if(numItems<=0){
            answer=null;
        } else{
            answer=new String[numItems];
            for(int i=0; i<numItems; i++){
                answer[i]=st.nextToken();
            }
        }
    }
    
    public String getAnswers(){
        StringBuilder sb=new StringBuilder();
        if(answer!=null)
            for(int i=0; i<answer.length; i++){
                if(i>0)
                    sb.append("|");
                sb.append(answer[i]);
            }
        return sb.substring(0);
    }
    
    public boolean checkText(String txt, Evaluator ev){
        if(answer==null || txt==null)
            return false;
        boolean ok=ev.checkText(txt, answer);
        targetStatus=ok ? SOLVED : WITH_ERROR;
        return ok;
    }
    
    public JComboBox buildCombo(AttributeSet targetAttr, AttributeSet errorAttr){
        if(!isList || options==null) return null;
        comboList=new TargetCombo(options, targetAttr, errorAttr);
        if(iniText!=null)
            comboList.setSelectedItem(iniText);
        return comboList;
    }
    
    public String getFillString(){
        return getFillString(numIniChars);
    }
    
    public String getFillString(int length){
        StringBuilder s=new StringBuilder();
        for(int i=0; i<length; i++){
            s.append(iniChar);
        }
        return s.substring(0);
    }
    
    public void requestFocus(TextActivityBase.Panel tabp, TargetMarker tm){
        if(comboList!=null){
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    comboList.requestFocus();
                    //comboList.setPopupVisible(true);
                }
            });
            
            //if(tab instanceof FillInBlanks)
            //    parent=(FillInBlanks.FillInBlanksPane)tab.pane;
        }
        else
            tabp.requestFocus();
        if(popupContent!=null && (infoMode==INFO_ALWAYS || (infoMode==INFO_ON_ERROR && targetStatus==WITH_ERROR)))
            tabp.popupTimer.setUp(this, tm);
    }
    
    public void checkPopup(TextActivityBase.Panel tabp, TargetMarker tm, boolean f1Key){
        if(popupContent!=null){
            boolean show=false;
            if(infoMode==INFO_ON_ERROR)
                show=(targetStatus==WITH_ERROR);
            else if(infoMode==INFO_ON_DEMAND)
                show=f1Key;
            
            if(show)
                tabp.popupTimer.setUp(this, tm);
            else
                tabp.popupTimer.stopPopup();
        }
        
    }
    
    protected Point getPopupLocation(TextActivityBase.Panel tabp, TargetMarker tm){
        if(popupContent==null || onlyPlay)
            return null;
        
        Rectangle r=tm.getBegRect(tabp.pane);
        if(r==null)
            return null;
        
        Point pt=r.getLocation();
        pt.y-=popupContent.dimension.height;
        if(pt.y<0)
            pt.y=r.y+r.height;
        
        if(pt.x+popupContent.dimension.width > tabp.pane.getWidth()){
            pt.x-=(tabp.pane.getWidth()-pt.x+popupContent.dimension.width);
            if(pt.x<0)
                pt.x=0;
        }
        return pt;
    }
    
    public void adjustPopupLocation(TextActivityBase.Panel tabp, TargetMarker tm){
        Point pt=getPopupLocation(tabp, tm);
        if(pt!=null)
            tabp.pane.bx.setLocation(pt);
    }
    
    public void lostFocus(TextActivityBase.Panel tabp, TargetMarker tm){
        if(comboList!=null && parentPane!=null){
            parentPane.repaint();
        }
        if(popupContent!=null)
            tabp.popupTimer.stopPopup();
    }

    public class TargetCombo extends JComboBox{
        
        Color bgColor, foreColor;
        Color errBgColor, errForeColor;
        Font rFont;
        TargetComboCellRenderer cellRenderer;
        JTextField txEditor;
        AbstractAction showListAction, fwdAction, bkAction;
        
        public TargetCombo(String[] items, AttributeSet targetAttr, AttributeSet errorAttr){
            super(items);
            setSelectedIndex(-1);
            showListAction=new AbstractAction("showList"){
                public void actionPerformed(ActionEvent e){
                    setPopupVisible(true);
                }
            };
            fwdAction=new AbstractAction("forward"){
                public void actionPerformed(ActionEvent e){
                    if(parentPane!=null){
                        Action act=parentPane.getActionMap().get("next-target");
                        if(act!=null)
                            act.actionPerformed(e);
                    }
                }
            };
            bkAction=new AbstractAction("backwards"){
                public void actionPerformed(ActionEvent e){
                    if(parentPane!=null){
                        Action act=parentPane.getActionMap().get("prev-target");
                        if(act!=null)
                            act.actionPerformed(e);
                    }
                }
            };
                                    
            rFont=TextActivityDocument.attributesToFont(targetAttr);
            setFont(rFont);
            bgColor=StyleConstants.getBackground(targetAttr);
            foreColor=StyleConstants.getForeground(targetAttr);
            errBgColor=StyleConstants.getBackground(errorAttr);
            errForeColor=StyleConstants.getForeground(errorAttr);
            setCursor(Cursor.getDefaultCursor());
            cellRenderer=new edu.xtec.jclic.activities.text.TextTarget.TargetCombo.TargetComboCellRenderer();
            setRenderer(cellRenderer);
            
            Dimension d=getPreferredSize();
            setPreferredSize(new Dimension((int)d.getWidth()+rFont.getSize()/2, (int)d.getHeight()));
            
            setMaximumSize(getPreferredSize());
            setEditable(true);
            Component c=getEditor().getEditorComponent();
            if(c instanceof JTextField)
                txEditor=(JTextField)c;
            else
                txEditor=new JTextField();
            txEditor.setEnabled(false);
            checkColors();
            setAlignmentY(0.8f); 
            setKActions();            
        }
        
        protected void setKActions(){
            registerKeyboardAction(showListAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
            registerKeyboardAction(showListAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
            txEditor.registerKeyboardAction(showListAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
            txEditor.registerKeyboardAction(showListAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
            
            registerKeyboardAction(fwdAction, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            registerKeyboardAction(fwdAction, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            registerKeyboardAction(fwdAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            
            registerKeyboardAction(bkAction, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            registerKeyboardAction(bkAction, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }
        
        public void checkColors(){
            if(targetStatus==WITH_ERROR){
                txEditor.setBackground(errBgColor);
                txEditor.setForeground(errForeColor);
                txEditor.setDisabledTextColor(errForeColor);
            }
            else{
                txEditor.setBackground(bgColor);
                txEditor.setForeground(foreColor);
                txEditor.setDisabledTextColor(foreColor);
            }
        }
        
        class TargetComboCellRenderer extends JLabel implements ListCellRenderer {
            
            public TargetComboCellRenderer() {
                setOpaque(true);
                setFont(rFont);
            }
            
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                setBackground(isSelected ? foreColor : bgColor);
                setForeground(isSelected ? bgColor : foreColor);
                setText(value==null ? "" : value.toString());
                return this;
            }            
        }
        
        @Override
        public void setSelectedItem(Object o){
            super.setSelectedItem(o);
            if(o!=null && parentPane!=null){
                setModified(true);
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        if(!isPopupVisible()){
                            parentPane.targetChanged(TextTarget.this);
                        }
                    }
                });
            }
        }        
    }
    
    public void setModified(boolean value){
        flagModified=value;
        if(value==true && targetStatus==NOT_EDITED)
            targetStatus=EDITED;
    }
    
    public boolean isModified(){
        return flagModified;
    }
    
    
    public static class PopupTimer extends javax.swing.Timer{
        
        TextTarget target;
        boolean action;
        TextActivityBase.Panel tabp;
        TargetMarker tm;
        
        public PopupTimer(TextActivityBase.Panel tabp){
            super(1000, null);
            this.tabp=tabp;
            setRepeats(false);
            target=null;
        }
        
        public void setUp(TextTarget setTarget, TargetMarker setTm){
            stop();
            target=setTarget;
            tm=setTm;
            action=true;
            if(target!=null && target.popupContent!=null){
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                if(target.popupDelay>0){
                    //setDelay(target.popupDelay*1000);
                    setInitialDelay(target.popupDelay*1000);
                    setRepeats(false);
                    start();
                }
                else
                    startPopup();
                    }});
            }
        }
        
        public void startPopup(){
            stop();
            if(target!=null && target.popupContent!=null){
                if(!target.onlyPlay){
                    Point pt=target.getPopupLocation(tabp, tm);
                    if(pt!=null)
                        tabp.pane.enableActiveBox(target.popupContent, pt);
                }
                if(target.popupContent.mediaContent!=null)
                    tabp.ps.playMedia(target.popupContent.mediaContent, tabp.pane.bx);
                if(target.popupMaxTime>0){
                    action=false;
                    //setDelay(target.popupMaxTime*1000);
                    setInitialDelay(target.popupMaxTime*1000);
                    setRepeats(false);
                    start();
                }
            }
        }
        
        public void stopPopup(){
            stop();
            if(target!=null && target.popupContent!=null){
                tabp.ps.stopMedia(1);
                tabp.pane.disableActiveBox();
            }
        }
        
        @Override
        protected void fireActionPerformed(java.awt.event.ActionEvent e){
            if(action==true)
                startPopup();
            else
                stopPopup();
        }
    }
}
