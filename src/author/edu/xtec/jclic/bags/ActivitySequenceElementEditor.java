/*
 * File    : ActivitySequenceElementEditor.java
 * Created : 08-apr-2003 16:46
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

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.TestPlayerContainer;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.edit.EditorPanel;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceManager;
import edu.xtec.util.StrUtils;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public class ActivitySequenceElementEditor extends Editor{
    
    protected static ImageIcon icon;
    public static boolean actionsCreated=false;
    protected ActivitySequence activitySequence;
    //public static EditorAction testActivityAction;    
    //public static EditorAction newActivitySequenceElementAction;    
    
    /** Creates a new instance of ActivitySequenceElementEditor */
    public ActivitySequenceElementEditor(ActivitySequenceElement el) {
        super(el);
    }
    
    protected void createChildren() {
    }
    
    public Class getEditorPanelClass() {
        return ActivitySequenceElementEditorPanel.class;
    }
    
    public EditorPanel createEditorPanel(Options options) {
        return new ActivitySequenceElementEditorPanel(options);
    }
    
    public ActivitySequenceElement getActivitySequenceElement(){
        return (ActivitySequenceElement)getFirstObject(ActivitySequenceElement.class);
    }
    
    public String getTag(){
        ActivitySequenceElement ase=getActivitySequenceElement();
        return StrUtils.nullableString(ase.getTag());
    }
    
    protected void saveData(){        
    }
    
    /*
    protected boolean delete(boolean changeSelection){
        ActivitySequence as=getActivitySequenceElement().getParent();
        boolean result=super.delete(changeSelection);
        if(result && as!=null)
            as.remove(getActivitySequenceElement());
        return result;
    }
     */
    
    @Override
    protected boolean canClone(){
        return true;
    }
    
    @Override
    protected Editor getClone() throws Exception{
        ActivitySequenceElement asel=(ActivitySequenceElement)getActivitySequenceElement().clone();
        return asel.getEditor(null);
    }
    
    protected void setActionsFlag(){
        allowDelete = true;        
        allowCut = true;
        allowCopy = true;
        allowPaste=true;
    }
    
    @Override
    public void setActionsOwner(){        
        
        setActionsFlag();
        super.setActionsOwner();
        
        /*
        if(basicActionsCreated){
            setActionsFlag();
            boolean eUp=false, eDown=false;
            ActivitySequence as=getActivitySequenceElement().getParent();
            if(as!=null){
                int i=as.getElementIndex(getActivitySequenceElement());
                eUp=i>0;
                eDown=i<as.getSize()-1;
            }
            moveUpAction.setActionOwner(eUp ? this : null);
            moveDownAction.setActionOwner(eDown ? this : null);
            copyAction.setActionOwner(allowCopy && canClone() ? this : null);
            cutAction.setActionOwner(allowCut ? this : null);
            pasteAction.setActionOwner(canPasteHere() ? this : null);            
            deleteAction.setActionOwner(allowDelete ? this : null);            
        }
         */ 
        
        if(actionsCreated){
            ActivityBagElementEditor.testActivityAction.setActionOwner(this);
            ActivitySequenceEditor.newActivitySequenceElementAction.setActionOwner(this);
        }
    }
    
    @Override
    public void clearActionsOwner(){
        super.clearActionsOwner();
        ActivityBagElementEditor.testActivityAction.setActionOwner(null);
        ActivitySequenceEditor.newActivitySequenceElementAction.setActionOwner(getEditorParent());
    }
    
    public static Icon getIcon(){
        if(icon==null)
            icon=edu.xtec.util.ResourceManager.getImageIcon("icons/miniclic.png");
        return icon;
    }
    
    @Override
    public Icon getIcon(boolean leaf, boolean expanded){
        //return leaf ? getIcon() : null;
        return getIcon();
    }    
    
    @Override
    public boolean canBeParentOf(Editor e){
        return (e instanceof ActivitySequenceEditor);
    }
    
    @Override
    public boolean canBeSiblingOf(Editor e){
        return (e instanceof ActivitySequenceElementEditor);
    }        
    
    public JClicProjectEditor getProjectEditor(){
        return (JClicProjectEditor)getFirstParent(JClicProjectEditor.class);
    }
    
    public void testActivity(){
        collectData();
        JClicProjectEditor pe=getProjectEditor();                        
        int item=getParent().getIndex(this);
        if(pe!=null && item>=0){
            TestPlayerContainer tpc=pe.getTestPlayerContainer();
            if(tpc!=null && tpc.getTestPlayer()!=null){
                tpc.getTestPlayer().load(null, Integer.toString(item), null, null);
                tpc.test();
            }
        }
    }
    
    public static void createActions(Options options){
        ActivityBagElementEditor.createActions(options);
        actionsCreated=true;
    }        

    /*
    public boolean moveUp(boolean updateSelection){
        boolean result=super.moveUp(updateSelection);
        if(result){
            ActivitySequenceElement ase=getActivitySequenceElement();
            ActivitySequence as=ase.getParent();
            if(as!=null){
                int index=as.getElementIndex(ase);
                if(index>0){
                    as.remove(ase);
                    as.insertElementAt(ase, index-1);
                    if(updateSelection)
                        select();
                }
            }
        }
        return result;
    }
    
    public boolean moveDown(boolean updateSelection){
        boolean result=super.moveDown(updateSelection);
        if(result){
            ActivitySequenceElement ase=getActivitySequenceElement();
            ActivitySequence as=ase.getParent();
            if(as!=null && ase!=null){
                int index=as.getElementIndex(ase);
                if(index<as.getSize()-1){
                    as.remove(ase);
                    as.insertElementAt(ase, index+1);
                    if(updateSelection)
                        select();
                }
            }
        }
        return result;
    }
     */   
    
    public static Icon getElementIcon(ActivitySequenceElement ase, boolean fwd){
        String iconName = fwd ? "seq_next" : "seq_prev";
        ActivitySequenceJump asj=fwd ? ase.fwdJump : ase.backJump;
        if(asj!=null){
            if(asj.action==JumpInfo.STOP)
                iconName+="_stop";
            else if(asj.action==JumpInfo.JUMP)
                iconName+="_jump";
            else if(asj.action==JumpInfo.RETURN)
                iconName+="_return";
            else if(asj.action==JumpInfo.EXIT)
                iconName="seq_exit";
        }
        return ResourceManager.getImageIcon("icons/"+iconName+".gif");
    }
    
    public static String getElementJumpDescription(ActivitySequenceElement ase, boolean fwd){
        String result=null;
        ActivitySequenceJump asj = fwd ? ase.fwdJump : ase.backJump;
        if(asj!=null){
            result=StrUtils.secureString(asj.sequence);
            if(asj.projectPath!=null)
                result=result+" ("+asj.projectPath+")";
        }
        return result;
    }
    
    public static String getElementJumpDescription(ActivitySequenceElement ase){
        String result=null;
        if(ase.fwdJump!=null || ase.backJump!=null){
            result=getElementJumpDescription(ase, true);
            String r2=getElementJumpDescription(ase, false);
            if(r2!=null && r2.length()>0){
                StringBuilder sb=new StringBuilder(100);
                if(result!=null && result.length()>0)
                    sb.append(result).append(" ");
                sb.append("* ").append(r2);
                result=sb.substring(0);
            }
        }
        return result;
    }
    
    public boolean nameChanged(int type, String oldName, String newName){
        boolean result=false;
        ActivitySequenceElement ase=getActivitySequenceElement();
        if((type & Constants.T_ACTIVITY)!=0 && oldName.equals(ase.getActivityName())){
            ase.setActivityName(newName);
            setModified(true);
            result=true;
        }
        if((type & Constants.T_SEQUENCE)!=0){
            if(oldName.equals(ase.getTag())){
                ase.setTag(newName);
                result=true;
            }
            if(ase.fwdJump!=null){
                result|=jumpInfoNameChanged(ase.fwdJump, oldName, newName)
                |jumpInfoNameChanged(ase.fwdJump.upperJump, oldName, newName)
                |jumpInfoNameChanged(ase.fwdJump.lowerJump, oldName, newName);
            }
            if(ase.backJump!=null){
                result|=jumpInfoNameChanged(ase.backJump, oldName, newName)
                |jumpInfoNameChanged(ase.backJump.upperJump, oldName, newName)
                |jumpInfoNameChanged(ase.backJump.lowerJump, oldName, newName);
            }
            if(result)
                setModified(true);            
        }   
        return result;
    }
    
    private boolean jumpInfoNameChanged(JumpInfo ji, String oldName, String newName){
        boolean result=false;
        if(ji!=null && ji.action==JumpInfo.JUMP && ji.projectPath==null && oldName.equals(ji.sequence)){
            ji.sequence=newName;
            result=true;
        }
        return result;
    }
    
    
}
