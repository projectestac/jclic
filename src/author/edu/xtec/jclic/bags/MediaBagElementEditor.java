/*
 * File    : MediaBagElementEditor.java
 * Created : 02-feb-2004 13:46
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
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.edit.EditorAction;
import edu.xtec.jclic.edit.EditorPanel;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class MediaBagElementEditor extends Editor{
    
    private static boolean actionsCreated ;
    private static Icon icon;
    public static EditorAction testMediaBagElementAction;
//    public static EditorAction newMediaBagElementAction;
    
    /** Creates a new instance of MediaBagElementEditor */
    public MediaBagElementEditor(MediaBagElement el) {
        super(el);
    }
    
    protected void createChildren() {
    }
    
    public Class getEditorPanelClass() {
        return MediaBagElementEditorPanel.class;
    }
    
    public EditorPanel createEditorPanel(Options options) {
        return new MediaBagElementEditorPanel(options);
    }
    
    public MediaBagElement getMediaBagElement(){
        return (MediaBagElement)getFirstObject(MediaBagElement.class);
    }
    
    public MediaBag getMediaBag(){
        MediaBag result=null;
        if(getEditorParent() instanceof MediaBagEditor){
            result=((MediaBagEditor)getParent()).getMediaBag();
        }
        return result;
    }
    
    public String getName(){
        return getMediaBagElement().getName();
    }
    
    private static MediaPreviewPanel previewPanel=null;
    public void testMedia(java.awt.Component parent, Options options){        
        if(previewPanel==null){
            previewPanel=new MediaPreviewPanel(this, options);
            options.getMessages().showInputDlg(parent, previewPanel, "edit_media_preview_tooltip", null);
            //JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(parent), previewPanel, "preview", javax.swing.JOptionPane.PLAIN_MESSAGE);
            previewPanel.end();
            previewPanel=null;
        }
    }
    
    public boolean rename(String newName, Editor.EditorListener agent, Messages msg){
        String oldName=getName();
        String errMsg=null;
        newName=newName.trim();
        Component parentComponent=(agent instanceof Component) ? (Component)agent : null;
        boolean result=false;
        
        if(oldName.equals(newName)){
            // do nothing           
        }
        else if(newName.length()<1)
            errMsg="edit_media_rename_invalid";
        else if(getMediaBag().getElement(newName)!=null)
            errMsg="edit_media_rename_exists";
        else{
            result=getProjectEditor().nameChanged(
            Utils.T_CODES[Utils.getFileType(getMediaBagElement().getFileName())],
            oldName, newName);
            //getMediaBagElement().setName(newName);            
            //getMediaBag().fireMediaNameChanged(Constants.MEDIA_OBJECT, newName, oldName);
            
            //if(result)
            //    fireEditorDataChanged(agent);
        }
        
        if(errMsg!=null && msg!=null && parentComponent!=null){
            msg.showAlert(parentComponent, errMsg);            
        }
        return result;
    }
    
    public void updateContent(Editor.EditorListener agent){
        getMediaBagElement().setData(null);
        fireEditorDataChanged(/*agent*/null);
    }
    
    public boolean changeFileName(String newFileName, Editor.EditorListener agent, Messages msg){
        boolean result=false;
        String errMsg=null;
        String oldFileName=getMediaBagElement().getFileName();
        
        FileSystem fs=getMediaBag().getProject().getFileSystem();
        newFileName=FileSystem.stdFn(newFileName.trim());
        Component parentComponent=(agent instanceof Component) ? (Component)agent : null;
        
        if(oldFileName.equals(newFileName)){
            // do nothing           
        }
        else if(Utils.getFileType(oldFileName)!=Utils.getFileType(newFileName)){
            errMsg="edit_media_chfile_different";            
        }
        else if(newFileName.length()<1)
            errMsg="edit_media_chfile_invalid";
        else if(getMediaBag().getElementByFileName(newFileName)!=null){
            if(msg!=null && parentComponent!=null){            
                msg.showAlert(parentComponent, new String[]{
                    msg.get("edit_media_exists_1"),
                    newFileName,
                    msg.get("edit_media_exists_2")
                });
            }
        }
        else{
            getMediaBagElement().setFileName(newFileName);
            result=true;
            fireEditorDataChanged(agent);
        }
        
        if(errMsg!=null && msg!=null && parentComponent!=null){
            msg.showAlert(parentComponent, errMsg);            
        }        
        return result;
    }
        
    @Override
    public String toString(){
        return getName();
    }
    
    protected void saveData(){
    }
        
    @Override
    protected boolean delete(boolean changeSelection){        
        MediaBagEditor mbe=(MediaBagEditor)getEditorParent();                                
        boolean result=super.delete(changeSelection);
        if(result && mbe!=null){
            result=mbe.getMediaBag().removeElement(getMediaBagElement());
            mbe.fireEditorDataChanged(null);
        }
        return result;
    }
    
    @Override
    protected boolean canClone(){
        return false;
    }
    
    protected void setActionsFlag(){
        // ATENCIO:
        allowDelete = true;
        allowCut = false;
        allowCopy = false;
        allowPaste=false;
    }
    
    @Override
    public void setActionsOwner(){        
        setActionsFlag();
        super.setActionsOwner();
        
        if(actionsCreated){
            testMediaBagElementAction.setActionOwner(this);
            //newMediaBagElementAction.setActionOwner(this);            
        }
    }
    
    @Override
    public void clearActionsOwner(){
        super.clearActionsOwner();
        if(actionsCreated){
            //newMediaBagElementAction.setActionOwner(null);
            testMediaBagElementAction.setActionOwner(null);
        }
    }
    
    public static Icon getIcon(){
        if(icon==null)
            icon=edu.xtec.util.ResourceManager.getImageIcon("icons/movie.gif");
        return icon;
    }
    
    @Override
    public Icon getIcon(boolean leaf, boolean expanded){
        //return leaf ? getIcon() : null;
        return getIcon();
    }
    
    @Override
    public boolean canBeParentOf(Editor e){
        return false;
    }
    
    @Override
    public boolean canBeSiblingOf(Editor e){
        return (e instanceof MediaBagElementEditor);
    }
    
    public JClicProjectEditor getProjectEditor(){
        return (JClicProjectEditor)getFirstParent(JClicProjectEditor.class);
    }
    
    
    public static void createActions(Options opt){
        createBasicActions(opt);
        if(!actionsCreated){
            testMediaBagElementAction=new EditorAction("edit_media_preview", "icons/media_view.gif", "edit_media_preview_tooltip", opt){
                protected void doAction(Editor e){
                    EditorPanel ep=getEditorPanelSrc();
                    if(ep!=null && e instanceof MediaBagElementEditor){
                        //MediaBagElement mbe=((MediaBagElementEditor)e).getMediaBagElement();
                        ((MediaBagElementEditor)e).testMedia(ep, ep.getOptions());
                    }
                }
            };                        
            actionsCreated=true;
        }
    }
    
    public String getDescription(Options options){
        MediaBagElement mbe=getMediaBagElement();
        StringBuilder sb=new StringBuilder();
        String fileName=mbe.getFileName();
        if(fileName!=null){
            int type=Utils.getFileType(fileName);
            sb.append(options.getMsg(Utils.TYPE_CODES[type]));
            if(type==Utils.TYPE_IMAGE){
                if(mbe.animated)
                    sb.append(" ").append(options.getMsg("ftype_animated"));
                try{
                    Image img=mbe.getImage();
                    if(img!=null){
                        sb.append(" (").append(img.getWidth(null)).append("x").append(img.getHeight(null)).append(")");
                    }
                } catch(Exception ex){
                    sb.append(" - ").append(options.getMsg("ERROR"));
                    System.err.println("Error reading image "+fileName);
                }
            }
        }
        return sb.substring(0);
    }
    
    public long getFileSize(){
        MediaBagElement mbe=getMediaBagElement();
        long result=-1L;
        if(mbe!=null){
            String fileName=mbe.getFileName();
            if(fileName!=null && getMediaBag()!=null){
                try{
                    result=getMediaBag().getProject().getFileSystem().getFileLength(fileName);
                } catch(Exception ex){
                    System.err.println("ERROR getting the size of "+fileName);
                }
            }
        }
        return result;
    }
    
    public boolean nameChanged(int type, String oldName, String newName){
        boolean result=false;
        if((type & Constants.T_MEDIA)!=0 && oldName.equals(getMediaBagElement().getName())){
            getMediaBagElement().setName(newName);
            setModified(true);
            result=true;
        }
        return result;
    }
    
    public List<String> listReferences(){
        List<String> result=null;
        MediaBag mb=getMediaBag();
        if(mb!=null){
            Map<String, String> hm=new HashMap<String, String>();
            mb.listReferencesTo(getName(), Constants.MEDIA_OBJECT, hm);
            result=new ArrayList<String>(hm.keySet());
        }
        allowDelete=(result==null || result.isEmpty());
        deleteAction.setEnabled(allowDelete);
        return result;        
    }
    
}
