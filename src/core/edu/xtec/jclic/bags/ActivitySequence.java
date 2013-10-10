/*
 * File    : ActivitySequence.java
 * Created : 19-dec-2000 16:35
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

package edu.xtec.jclic.bags;

import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.report.Reporter;
import edu.xtec.jclic.report.SequenceReg;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class stores the definition of the sequence of activities related to a
 * specific {@link edu.xtec.jclic.project.JClicProject}. The sequence are formed by
 * an ordered list of objects of type {@link edu.xtec.jclic.bags.ActivitySequenceElement},
 * internally stored in a {@link java.util.ArrayList}. It stores also a transient pointer
 * to a current element, and provides several methods useful to deal with sequences.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */

public class ActivitySequence extends Object implements Editable, Domable{
    
    private int currentAct;
    private List<ActivitySequenceElement> elements;
    protected JClicProject project;
    
    /** Creates new ActivitySequence */
    public ActivitySequence(JClicProject project){
        this.project=project;
        elements=new ArrayList<ActivitySequenceElement>(20);
        currentAct=-1;
    }
    
    public Editor getEditor(Editor parent){
        return Editor.createEditor(getClass().getName()+"Editor", this, parent);
    }
    
    public JClicProject getProject(){
        return project;
    }
    
    public void add(ActivitySequenceElement ase){
        elements.add(ase);
        if(elements.size()==1){
            currentAct=0;
            if(ase.getTag()==null)
                ase.setTag("start");
        }
    }
    
    public void insertElementAt(ActivitySequenceElement ase, int index){
        elements.add(index, ase);
        //elements.insertElementAt(ase, index);
    }
    
    public void remove(ActivitySequenceElement ase){
        if(elements.contains(ase)){
            if(elements.indexOf(ase)==currentAct)
                currentAct=-1;
            elements.remove(ase);
        }
    }
    
    public int getSize(){
        return elements.size();
    }
    
    public static final String ELEMENT_NAME="sequence";
    
    public org.jdom.Element getJDomElement(){
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        for(int i=0; i<elements.size(); i++){
            e.addContent(getElement(i, false).getJDomElement());
        }
        return e;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        JDomUtility.checkName(e, ELEMENT_NAME);
        Iterator it=e.getChildren(ActivitySequenceElement.ELEMENT_NAME).iterator();
        while(it.hasNext()){
            add(ActivitySequenceElement.getActivitySequenceElement((org.jdom.Element)it.next()));
        }
    }
    
    public void clear(){
        currentAct=-1;
        elements.clear();
    }
    
    public int getElementIndex(ActivitySequenceElement ase){
        return ase==null ? -1 : elements.indexOf(ase);
    }
    
    public boolean checkAllElements() throws Exception{
        boolean result=true;
        for(int i=0; i<elements.size(); i++)
            if(project.activityBag.getElement(getElement(i, false).getActivityName())==null)
                result=false;
        return result;
    }
    
    public ActivitySequenceElement getElement(int n, boolean updateCurrentAct){
        ActivitySequenceElement result;
        try{
            result=elements.get(n);
        } catch(Exception ex){
            result=null;
        }
        if(result!=null && updateCurrentAct)
            currentAct=n;
        return result;
    }
    
    public ActivitySequenceElement getElementByTag(String tag, boolean updateCurrentAct){
        int i;
        int k=elements.size();
        if (k<1 || tag==null)
            return null;
        
        String normalizedTag=FileSystem.stdFn(tag);
        ActivitySequenceElement ase=null;
        for(i=0; i<k; i++){
            ase=getElement(i, false);
            if(ase!=null && ase.getTag()!=null && ase.getTag().equals(normalizedTag))
                break;
        }
        
        if(i==k)
            ase=null;
        else
            if(updateCurrentAct)
                currentAct=i;
        
        return ase;
    }
    
    public ActivitySequenceElement[] getElements(){
        return elements.toArray(new ActivitySequenceElement[elements.size()]);
        //ActivitySequenceElement[] result=new ActivitySequenceElement[elements.size()];
        //elements.copyInto(result);
        //return result;
    }
    
    public boolean hasNextAct(boolean hasReturn){
        boolean result=false;
        ActivitySequenceElement ase=getCurrentAct();
        if(ase!=null){
            if(ase.fwdJump==null)
                result=true;
            else switch(ase.fwdJump.action){
                case JumpInfo.STOP:
                    break;
                case JumpInfo.RETURN:
                    result=(hasReturn);
                    break;
                default:
                    result=true;
            }
        }
        return result;
    }
    
    public boolean hasPrevAct(boolean hasReturn){
        boolean result=false;
        ActivitySequenceElement ase=getCurrentAct();
        if(ase!=null){
            if(ase.backJump==null)
                result=true;
            else switch(ase.backJump.action){
                case JumpInfo.STOP:
                    break;
                case JumpInfo.RETURN:
                    result=hasReturn;
                    break;
                default:
                    result=true;
            }
        }
        return result;
    }
    
    public int getNavButtonsFlag(){
        int flag=ActivitySequenceElement.NAV_NONE;
        ActivitySequenceElement ase=getCurrentAct();
        if(ase!=null)
            flag=ase.navButtons;
        return flag;
    }
    
    public JumpInfo getJump(boolean back, Reporter rep){
        ActivitySequenceElement ase;
        JumpInfo result=null;
        if((ase=getCurrentAct())!=null){
            ActivitySequenceJump asj=(back ? ase.backJump : ase.fwdJump);
            if(asj==null){
                int i=currentAct+(back ? -1 : 1);
                if(i>=elements.size() || i<0)
                    i=0;
                result=new JumpInfo(JumpInfo.JUMP, i);
            }
            else{
                int rating=-1;
                int time=-1;
                if(rep!=null){
                    SequenceReg.Info info=rep.getCurrentSequenceInfo();
                    if(info!=null){
                        rating=(int)(info.tScore);
                        time=(int)(info.tTime/1000);
                    }
                }
                result=asj.resolveJump(rating, time);
            }
        }
        return result;
    }
    
    public ActivitySequenceElement getCurrentAct(){
        return getElement(currentAct, false);
    }
    
    public int getCurrentActNum(){
        return currentAct;
    }
    
    public String getSequenceForElement(int e){
        String s=null;
        if(e>=0 && e<elements.size())
            for(int i=e; i>=0; i--)
                if((s=getElement(i, false).getTag())!=null)
                    break;
        return s;
    }
    
    public ActivitySequenceElement getElementByActivityName(String activityName){
        ActivitySequenceElement result=null;
        if(activityName!=null){
            for(int i=0; i<elements.size(); i++){
                ActivitySequenceElement ase=getElement(i, false);
                if(ase.getActivityName().equalsIgnoreCase(activityName)){
                    result=ase;
                    break;
                }
            }
        }
        return  result;
    }
    
    public boolean checkCurrentActivity(String name){
        ActivitySequenceElement ase=getCurrentAct();
        if(ase==null || !ase.getActivityName().equalsIgnoreCase(name)){
            for(int i=0; i<elements.size(); i++){
                if(getElement(i, false).getActivityName().equalsIgnoreCase(name)){
                    currentAct=i;
                    return false;
                }
            }
            ase=new ActivitySequenceElement(name, 0, ActivitySequenceElement.NAV_BOTH);
            ase.fwdJump=new ActivitySequenceJump(JumpInfo.STOP);
            ase.backJump=new ActivitySequenceJump(JumpInfo.STOP);
            elements.add(ase);
            currentAct=elements.size()-1;
            return false;
        }
        return true;
    }
    
    public void listReferences(String type, Map<String, String> map) {
        for(int i=0; i<elements.size(); i++)
            getElement(i, false).listReferences(type, map);
    }
}