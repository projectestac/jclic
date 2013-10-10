/*
 * File    : MediaBagEditor.java
 * Created : 17-sep-2002 16:30
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
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.fileSystem.ZipFileSystem;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.ProgressDialog;
import edu.xtec.util.StrUtils;
import edu.xtec.util.StreamIO;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class MediaBagEditor extends Editor{

    public static final int DEFAULT_IMG_MAX_WIDTH=800;
    public static final int DEFAULT_IMG_MAX_HEIGHT=600;
    public static final int IMG_MIN_SIZE=10;
    public static final String RESIZED="resized";

    //public static EditorAction newMediaAction;
    public static boolean actionsCreated;
    private boolean initializing;
    public static EditorAction newMediaBagElementAction, updateAllMediaAction, exportAllMediaAction;
    private static Icon icon;
    protected static int imgMaxWidth=DEFAULT_IMG_MAX_WIDTH;
    protected static int imgMaxHeight=DEFAULT_IMG_MAX_HEIGHT;
    
    /** Creates a new instance of MediaBagEditor */
    public MediaBagEditor(MediaBag mb) {
        super(mb);
    }
    
    public Options getOptions(){
        return getMediaBag().getProject().getBridge().getOptions();
    }
    
    protected void createChildren() {
        initializing=true;
        Iterator it=getMediaBag().getElementsByName().iterator();
        while(it.hasNext()){
            ((MediaBagElement)it.next()).getEditor(this);
        }
        initializing=false;
    }
    
    public EditorPanel createEditorPanel(Options options) {
        return new MediaBagMultiEditorPanel(options);
    }
    
    public Class getEditorPanelClass() {
        return MediaBagMultiEditorPanel.class;
    }
    
    @Override
    public String getTitleKey(){
        return "edit_media";
    }
    
    public MediaBag getMediaBag(){
        return (MediaBag)getUserObject();
    }
    
    public JClicProjectEditor getProjectEditor(){
        return (JClicProjectEditor)getFirstParent(JClicProjectEditor.class);
    }    
            
    public static String[] selectMediaFiles(MediaBag mediaBag, Options options, Component dlgOwner, int fileFilters, String defaultFile){
        String[] result;
        FileSystem fs=mediaBag.getProject().getFileSystem();
        //int[] filters=new int[]{fileFilters};
        //if(fileFilters<0){
        int[] filters=new int[]{Utils.ALL_IMAGES_FF, Utils.ALL_SOUNDS_FF, Utils.MIDI_FF,
        Utils.ALL_VIDEO_FF, Utils.ALL_ANIM_FF, Utils.FONTS_FF, Utils.SKINS_FF,
        Utils.ALL_MEDIA_FF};
        //}
        result=fs.chooseFiles(defaultFile, false, filters, options, "edit_find_media", dlgOwner, true, true);
        if(result!=null){
            for(int i=0; i<result.length; i++){
                if(result[i]!=null)
                    result[i]=result[i].replace(File.separatorChar, '/');
            }
        }
        return result;
    }
    
    public String getValidMediaName(String suggestedName){
        String name=suggestedName;
        int i=1;
        while(getMediaBag().getElement(name)!=null){
            name=name+"_"+i++;
        }
        return name;
    }
    
    public MediaBagElementEditor addMediaBagElement(MediaBagElement mbe){
        MediaBagElementEditor result=null;
        if(getMediaBag().addElement(mbe)){
            result=(MediaBagElementEditor)mbe.getEditor(this);
            fireEditorDataChanged(null);
        }
        return result;
    }
    
    public MediaBagElementEditor[] createNewMediaBagElements(Options options, Component dlgOwner, int fileFilters){
        MediaBagElementEditor[] result=null;
        Messages msg=options.getMessages();
        MediaBag mb=getMediaBag();
        FileSystem fs=mb.getProject().getFileSystem();
        String[] fNames=selectMediaFiles(getMediaBag(), options, dlgOwner, fileFilters, null);
        if(fNames!=null){
            List<MediaBagElementEditor> v=new ArrayList<MediaBagElementEditor>();
            for(int i=0; i<fNames.length; i++){
                if(mb.getElementByFileName(fNames[i])!=null){
                    msg.showAlert(dlgOwner, new String[]{
                        msg.get("edit_media_exists_1"),
                        fNames[i],
                        msg.get("edit_media_exists_2")
                    });
                }
                else{
                    MediaBagElement mbe=new MediaBagElement(fNames[i], null, getValidMediaName(fNames[i]));
                    boolean cancel=false;

                    // Check image size
                    if(mbe.isImage()){
                        Image img=null;
                        try {
                            img=mbe.prepareAndGetImage(fs);
                            if(img==null)
                                throw new Exception("invalid image!");
                        } catch (Exception ex){
                            msg.showErrorWarning(dlgOwner, "err_reading_data", fNames[i], ex, null);
                            cancel=true;
                        }

                        if (img != null) {
                            int imgWidth = img.getWidth(null);
                            int imgHeight = img.getHeight(null);

                            if ((imgWidth > imgMaxWidth || imgHeight > imgMaxHeight)) {
                                int answer = msg.showQuestionDlgObj(
                                        dlgOwner,
                                        new String[]{StrUtils.replace(
                                            msg.get("img_resize_prompt"),
                                            "%s",
                                            new String[]{
                                                fNames[i],
                                                "" + imgWidth + "x" + imgHeight,
                                                "" + imgMaxWidth + "x" + imgMaxHeight
                                            })},
                                        "CONFIRM",
                                        "ync");

                                if (answer == Messages.CANCEL) {
                                    cancel = true;
                                } else if (answer == Messages.YES) {

                                    // TODO: Process can be long. Display wait cursor

                                    String baseName = fNames[i];
                                    int lastDot = baseName.lastIndexOf(".");
                                    if (lastDot > 0) {
                                        baseName = baseName.substring(0, lastDot);
                                    }

                                    String newName = baseName + "-" + RESIZED + ".jpg";
                                    File destFile = new File(fs.getFullFileNamePath(newName));
                                    int counter = 0;
                                    while (destFile.exists()) {
                                        newName = baseName + "-" + RESIZED + Integer.toString(++counter) + ".jpg";
                                        destFile = new File(fs.getFullFileNamePath(newName));
                                    }

                                    try {
                                        javax.swing.ImageIcon resizedImg = mbe.getThumbNail(imgMaxWidth, imgMaxHeight, fs);
                                        BufferedImage bimg=Utils.toBufferedImage(resizedImg.getImage(), Color.white, null);
                                        javax.imageio.ImageIO.write(bimg, "jpg", destFile);
                                        fNames[i] = newName;
                                        mbe = new MediaBagElement(fNames[i], null, getValidMediaName(fNames[i]));
                                    } catch (Exception ex) {
                                        cancel=true;
                                        msg.showErrorWarning(dlgOwner, "err_file_save", ex);
                                    }

                                }
                            }
                        }
                    }

                    if(!cancel && mb.addElement(mbe)){
                        v.add((MediaBagElementEditor)mbe.getEditor(this));
                        //fireEditorDataChanged(null);
                    }
                }
            }
            if(v.size()>0){
                result=v.toArray(new MediaBagElementEditor[v.size()]);
                fireEditorDataChanged(null);
            }
        }
        return result;
    }
    
    public void updateAllElements(JComponent parent){        
        final ProgressDialog progressDialog=new ProgressDialog(parent, getOptions());
        edu.xtec.util.SwingWorker sw=new edu.xtec.util.SwingWorker(){
            
            @Override
            public Object construct(){
                FileSystem fs=getMediaBag().getProject().getFileSystem();
                ZipFileSystem zfs=(fs instanceof ZipFileSystem) ? (ZipFileSystem)fs : null;
                int count=0;
                progressDialog.setProgressMax(getChildCount());
                progressDialog.setProgressValue(count);
                
                Enumeration en=children();
                while(en.hasMoreElements() && !isCancelled()){
                    MediaBagElementEditor mbeled=(MediaBagElementEditor)en.nextElement();
                    String fName=mbeled.getMediaBagElement().getFileName();
                    progressDialog.setFileLabel(fName);
                    if(zfs!=null){
                        ZipFileSystem.ExtendedZipEntry ze=zfs.getEntry(fName);
                        if(ze!=null && zfs.fileExists(fName)){
                            ze.ignore=true;
                        }
                    }
                    mbeled.updateContent(null);
                    progressDialog.setProgressValue(++count);
                }
                setModified(true);
                progressDialog.setFileLabel(null);
                fireEditorDataChanged(null);                
                return null;
            }
            
            @Override
            public void finished(){
                progressDialog.setVisible(false);
            }
        };
        progressDialog.start("edit_media_refreshAll", "edit_media_refreshAll_working", sw, true, true, false);
    }
    
    public void exportAllElements(final JComponent parent){
        final Options options=getOptions();
        final ProgressDialog progressDialog=new ProgressDialog(parent, options);
        edu.xtec.util.SwingWorker sw=new edu.xtec.util.SwingWorker(){

            @Override            
            public Object construct(){
                FileSystem fs=getMediaBag().getProject().getFileSystem();
                ZipFileSystem zfs=(fs instanceof ZipFileSystem) ? (ZipFileSystem)fs : null;
                String dlgMsg=options.getMessages().get("filesystem_copyFile");
                int count=0;
                progressDialog.setProgressMax(getChildCount());
                progressDialog.setProgressValue(count);
                Enumeration en=children();
                boolean overwriteAll=false;
                boolean overwriteNone=false;
                while(en.hasMoreElements() && !isCancelled()){
                    MediaBagElementEditor mbeled=(MediaBagElementEditor)en.nextElement();
                    String fName=mbeled.getMediaBagElement().getFileName();
                    if(zfs!=null){
                        ZipFileSystem.ExtendedZipEntry ze=zfs.getEntry(fName);
                        if(ze!=null && !ze.ignore){
                            progressDialog.setFileLabel(fName);
                            String fNameDest=zfs.getFullFileNamePath(fName);
                            File fileDest=new File(fNameDest);
                            boolean prompt=true;
                            if(fileDest.exists()){
                                if(overwriteNone)
                                    continue;
                                else if(!overwriteAll){
                                    boolean next=false;
                                    boolean cancel=false;
                                    switch(options.getMessages().confirmOverwriteFile(parent, fileDest, "yYnNc")){
                                        case Messages.NO_TO_ALL:
                                            overwriteNone=true;
                                        case Messages.NO:
                                            next=true;
                                            break;
                                        case Messages.YES_TO_ALL:
                                            overwriteAll=true;
                                        case Messages.YES:
                                            break;
                                        default:
                                            cancel=true;
                                    }
                                    if(next)
                                        continue;
                                    else if(cancel)
                                        break;
                                }
                            }
                            try{
                                OutputStream os=zfs.createSecureFileOutputStream(fNameDest);
                                //int len=(int)zfs.getFileLength(fName);
                                InputStream is=zfs.getInputStream(fName);
                                StreamIO.writeStreamTo(is, os);
                                //StreamIO.writeStreamDlg(is, os, len, dlgMsg, progressDialog, options);
                            } catch(Exception ex){
                                options.getMessages().showErrorWarning(progressDialog, "FILE_ERR_SAVING", ex);
                                break;
                            }
                        }
                        progressDialog.setProgressValue(++count);
                    }
                }
                progressDialog.setFileLabel(null);
                return null;
            }
            
            @Override            
            public void finished(){
                progressDialog.setVisible(false);
            }
        };
        progressDialog.start("edit_media_refreshAll", "edit_media_refreshAll_working", sw, true, true, false);
    }

    @Override
    public void setActionsOwner(){
        allowDelete = allowCut = allowCopy = allowPaste = false;
        super.setActionsOwner();
        if(actionsCreated){
            newMediaBagElementAction.setActionOwner(this);
            FileSystem fs=getMediaBag().getProject().getFileSystem();
            boolean isZipFileSystem=(fs instanceof ZipFileSystem);
            updateAllMediaAction.setActionOwner(isZipFileSystem ? this : null);
            exportAllMediaAction.setActionOwner(isZipFileSystem ? this : null);
        }
    }
    
    @Override
    public void clearActionsOwner(){
        super.clearActionsOwner();
        if(actionsCreated){
            newMediaBagElementAction.setActionOwner(null);
            updateAllMediaAction.setActionOwner(null);
            exportAllMediaAction.setActionOwner(null);
        }
    }
    
    
    public static void createActions(Options opt){
        createBasicActions(opt);
        if(!actionsCreated){
            newMediaBagElementAction=new EditorAction("edit_media_new", "icons/media_new.gif", "edit_media_new_tooltip", opt){
                protected void doAction(Editor e){
                    Editor ch=null;
                    if(e instanceof MediaBagElementEditor){
                        ch=e;
                        e=e.getEditorParent();
                    }
                    if(e instanceof MediaBagEditor){
                        EditorPanel ep=getEditorPanelSrc();
                        MediaBagMultiEditorPanel mbep=null;
                        if(ep instanceof MediaBagMultiEditorPanel)
                            mbep=(MediaBagMultiEditorPanel)ep;
                        int filters=(mbep==null ? -1 : mbep.getFilters());
                        MediaBagElementEditor[] mbed=((MediaBagEditor)e).createNewMediaBagElements(options, getComponentSrc(), filters);
                        if(mbed!=null && mbed.length>0 && mbep!=null){
                            mbep.setSelected(mbed[0]);
                        }
                    }
                }
            };
            updateAllMediaAction=new EditorAction("edit_media_refreshAll", "icons/reset_all.gif", "edit_media_refreshAll_tooltip", opt){
                protected void doAction(Editor e){
                    if(e instanceof MediaBagEditor){
                        ((MediaBagEditor)e).updateAllElements(getJComponentSrc());
                    }
                }
            };
            exportAllMediaAction=new EditorAction("edit_media_exportAll", "icons/file_save_all.gif", "edit_media_exportAll_tooltip", opt){
                protected void doAction(Editor e){
                    if(e instanceof MediaBagEditor){
                        ((MediaBagEditor)e).exportAllElements(getJComponentSrc());
                    }
                }
            };
            actionsCreated=true;
        }
    }
    
    public MediaBagElementEditor getChildFor(MediaBagElement mbe){
        MediaBagElementEditor result=null;
        if(mbe!=null){
            Enumeration en=children();
            while(en.hasMoreElements()){
                Editor e=(Editor)en.nextElement();
                if(e.getUserObject()==mbe){
                    result=(MediaBagElementEditor)e;
                    break;
                }
            }
        }
        return result;
    }
    
    public List<MediaBagElementEditor> getChildrenList(int filters){
        List<MediaBagElementEditor> v=new ArrayList<MediaBagElementEditor>();
        Iterator it=getMediaBag().getElementsByName().iterator();
        String[] extFilters=Utils.getFileFilterExtensions(filters);
        while(it.hasNext()){
            MediaBagElement mbe=(MediaBagElement)it.next();
            int i=0;
            String s=mbe.getFileName().toLowerCase();
            if(extFilters!=null)
                while(i<extFilters.length && !s.endsWith(extFilters[i]))
                    i++;
            
            if(extFilters==null || i<extFilters.length){
                MediaBagElementEditor mbed=getChildFor(mbe);
                if(mbed!=null)
                    v.add(mbed);
            }
        }
        return v;
    }
    
    public boolean nameChanged(int type, String oldName, String newName){
        boolean result=false;
        if((type & Constants.T_MEDIA)!=0){
            Enumeration e=children();
            while(e.hasMoreElements())
                result|=((MediaBagElementEditor)e.nextElement()).nameChanged(type, oldName, newName);
        }
        return result;
    }
    
    public static Icon getIcon(){
        if(icon==null)
            icon=edu.xtec.util.ResourceManager.getImageIcon("icons/media_bag.gif");
        return icon;
    }
    
    public int checkOrphanElements(Options options, Component parent, boolean prompt){
        int result=Messages.YES;
        List<MediaBagElementEditor> v=new ArrayList<MediaBagElementEditor>();
        Enumeration en=children();
        while(en.hasMoreElements()){
            MediaBagElementEditor mbed=(MediaBagElementEditor)en.nextElement();
            if(mbed.listReferences().isEmpty())
                v.add(mbed);
        }
        if(!v.isEmpty()){
            boolean doIt=!prompt;
            if(!doIt){
                //result=options.getMessages().showQuestionDlg(parent, "edit_project_orphanMedia", null, true);
                Object[] object=new Object[]{
                    options.getMsg("edit_project_orphanMedia"),
                    v.size()>10 ? (Object)(new javax.swing.JScrollPane(new javax.swing.JList(v.toArray()))) : (Object)v,
                    options.getMsg("edit_project_orphanMedia_prompt"),
                };
                result=options.getMessages().showQuestionDlgObj(parent, object, "edit_project_orphanMedia_title", "ync");
                doIt=(result==Messages.YES);
            }
            if(doIt){
                Iterator<MediaBagElementEditor> it=v.iterator();
                while(it.hasNext()){
                    it.next().delete(true);
                }
            }
        }
        return result;
    }

    public static int getImgMaxWidth(){
        return imgMaxWidth;
    }

    public static void setImgMaxWidth(int width){
        imgMaxWidth=Math.max(IMG_MIN_SIZE, width);
    }

    public static int getImgMaxHeight(){
        return imgMaxHeight;
    }

    public static void setImgMaxHeight(int height){
        imgMaxHeight=Math.max(IMG_MIN_SIZE, height);
    }
    
}
