/*
 * File    : Menu.java
 * Created : 21.may-2002 9:28
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
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.media.*;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class Menu extends Activity implements Editable {
    
    protected ArrayList<MenuElement> menuElements;
    
    public static final int MAX_LABEL_LENGTH=30;
    
    
    /** Creates new Menu */
    public Menu(JClicProject project) {
        super(project);
        includeInReports=false;
        reportActions=false;
        bActionsCounter=false;
        bScoreCounter=false;
        bTimeCounter=false;
        menuElements=new ArrayList<MenuElement>();
    }
    
    @Override
    public String getPublicName(){
        return description;
    }
    
    @Override
    public org.jdom.Element getJDomElement(){
        org.jdom.Element e=super.getJDomElement();
        for(MenuElement el : menuElements)
            e.addContent(el.getJDomElement());
        return e;
    }
    
    @Override
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        super.setProperties(e, aux);
        includeInReports=false;
        java.util.Iterator it=e.getChildren(MenuElement.ELEMENT_NAME).iterator();
        while(it.hasNext())
            menuElements.add(MenuElement.getMenuElement((org.jdom.Element)it.next()));
    }
    
    public int getMenuElementCount(){
        return menuElements.size();
    }
    
    public MenuElement getMenuElement(int n){
        if(n<0 || n>=menuElements.size())
            return null;
        return (MenuElement)menuElements.get(n);
    }
    
    public void addMenuElement(MenuElement me){
        menuElements.add(me);
    }
    
    public int getMinNumActions() {
        return 0;
    }
    
    @Override
    public Editor getEditor(Editor parent){
        return Editor.createEditor(getClass().getName()+"Editor", this, parent);
    }
    
    public Activity.Panel getActivityPanel(PlayStation ps) {
        return new Panel(ps);
    }
    
    class Panel extends Activity.Panel implements ActionListener {
        
        VFlowScrollPane scrollPane;
        JPanel panel;
        
        protected Panel(PlayStation ps){
            super(ps);
        }
        
        @Override
        public void buildVisualComponents() throws Exception{
            super.buildVisualComponents();
            panel=new JPanel();
            panel.setBackground(activityBgColor);
            panel.setLayout(new java.awt.FlowLayout());
            scrollPane=new VFlowScrollPane(panel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setBorder(BorderFactory.createLineBorder(Color.darkGray, 1));
            addButtons();
            add(scrollPane);
        }
        
        public void addButtons(){
            panel.removeAll();
            for(int i=0; i<menuElements.size(); i++){
                MenuElement me=getMenuElement(i);
                JButton button=new JButton();
                //button.setPreferredSize(BUTTON_PREF_SIZE);
                
                button.setIcon(me.getIcon(project.mediaBag));
                
                if(me.caption!=null)
                    button.setText(StrUtils.getShortExpression(me.caption, MAX_LABEL_LENGTH));
                
                if(me.description!=null)
                    button.setToolTipText(me.description);
                else if(me.caption!=null)
                    button.setToolTipText(me.caption);
                
                button.setHorizontalTextPosition(SwingConstants.CENTER);
                button.setVerticalTextPosition(SwingConstants.BOTTOM);
                
                button.setActionCommand(Integer.toString(i));
                button.addActionListener(this);
                
                panel.add(button);
            }
        }
        
        @Override
        public void doLayout(){
            scrollPane.setBounds(0, 0, getWidth(), getHeight());
        }
        
        public void clear() {
        }
        
        public Dimension setDimension(Dimension maxSize) {
            return maxSize;
        }
        
        @Override
        public void initActivity() throws Exception{
            super.initActivity();
            
            if(!firstRun) buildVisualComponents();
            firstRun=false;
            
            setAndPlayMsg(MAIN, EventSounds.START);
            //ps.setMsg(messages[MAIN]);
            //ps.playMsg();
            //if(messages[MAIN]==null || messages[MAIN].mediaContent==null)
            //    playEvent(EventSounds.START);
            playing=true;
        }
        
        public void render(Graphics2D g2, Rectangle dirtyRegion) {
        }
        
        public void actionPerformed(ActionEvent ae) {
            int i;
            try{
                i=Integer.parseInt(ae.getActionCommand());
            } catch(NumberFormatException e){
                return;
            }
            if(i>=0){
                MenuElement me=getMenuElement(i);
                if(me!=null && (me.projectPath!=null || me.sequence!=null)){
                    MediaContent mc=new MediaContent();
                    if(MenuElement.RETURN_TAG.equals(me.sequence)){
                        mc.mediaType=MediaContent.RETURN;
                    }
                    else{
                        mc.mediaType=MediaContent.RUN_CLIC_PACKAGE;
                        mc.mediaFileName=me.sequence;
                        mc.externalParam=me.projectPath;
                    }
                    ps.playMedia(mc, null);
                }
            }
        }        
    }
    
}
