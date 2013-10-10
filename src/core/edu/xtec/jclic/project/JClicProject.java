/*
 * File    : JClicProject.java
 * Created : 16-aug-2001 1:17
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

package edu.xtec.jclic.project;

import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.bags.*;
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.fileSystem.ZipFileSystem;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.skins.Skin;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ProgressDialog;
import edu.xtec.util.ResourceBridge;
import edu.xtec.util.StrUtils;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */


public class JClicProject extends Object implements Editable, Domable {
    
    public static String CURRENT_VERSION="0.1.3";
    
    protected ResourceBridge bridge;
    protected FileSystem fileSystem;
    public ActivityBag activityBag;
    public ActivitySequence activitySequence;
    public MediaBag mediaBag;
    public Skin skin;
    
    protected String name;
    public String version;
    public ProjectSettings settings;
    public String type;
    
    /** Optional code used in reports for filtering queries by activity
     */
    public String code;
    
    protected String fullPath;
        
    public static String TYPE="type";
    
    /** Creates new JClicProject */
    public JClicProject(ResourceBridge bridge, FileSystem fileSystem, String fullPath) {
        this.fileSystem=fileSystem;
        this.bridge=bridge;
        this.fullPath = (fullPath==null ? "" : fullPath);
        version=CURRENT_VERSION;
        settings=new ProjectSettings();
        settings.title=bridge.getMsg("UNNAMED");
        setName(settings.title);
        type=null;
        
        activityBag=new ActivityBag(this);
        activitySequence=new ActivitySequence(this);
        mediaBag=new MediaBag(this/*, fileSystem*/);
        skin=null;        
    }
    
    public static String ELEMENT_NAME="JClicProject";
    public static String VERSION="version", NAME="name", CODE="code";
        
    public org.jdom.Element getJDomElement(){
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
                        
        //if(settings.title==ProjectSettings.UNTITLED && name!=NO_NAME)
        //    settings.title=name;
        
        e.setAttribute(NAME, name);
        e.setAttribute(VERSION, CURRENT_VERSION);
        if(type!=null)
            e.setAttribute(TYPE, type);
        if(code!=null)
            e.setAttribute(CODE, code);
        e.addContent(settings.getJDomElement());        
        e.addContent(activitySequence.getJDomElement());        
        e.addContent(activityBag.getJDomElement());        
        e.addContent(mediaBag.getJDomElement());
        
        return e;
    }
    
    //public void load(org.jdom.Element e) throws Exception{
    //    setProperties(e, null);
    //}
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        JDomUtility.checkName(e, ELEMENT_NAME);
        
        org.jdom.Element child;
        
        name=JDomUtility.getStringAttr(e, NAME, name, false);
        version=JDomUtility.getStringAttr(e, VERSION, version, false);
        type=JDomUtility.getStringAttr(e, TYPE, type, false);
        code=JDomUtility.getStringAttr(e, CODE, code, false);
        
        if((child=e.getChild(ProjectSettings.ELEMENT_NAME))!=null)
            settings=ProjectSettings.getProjectSettings(child);            
        
        //if(settings.title==ProjectSettings.UNTITLED && name!=NO_NAME)
        //    settings.title=name;
                
        //activitySequence.addElements(e.getChild(ActivitySequence.ELEMENT_NAME));        
        activitySequence.setProperties(e.getChild(ActivitySequence.ELEMENT_NAME), null);
        //activityBag.addActivities(e.getChild(ActivityBag.ELEMENT_NAME));        
        activityBag.setProperties(e.getChild(ActivityBag.ELEMENT_NAME), null);        
        if(version.compareTo("0.1.2")<=0)
            activityBag.sortByName();
        //mediaBag.addMediaElements(e.getChild(MediaBag.ELEMENT_NAME));        
        mediaBag.setProperties(e.getChild(MediaBag.ELEMENT_NAME), null);        
    }
    
    public static JClicProject getJClicProject(org.jdom.Element e, ResourceBridge rb, FileSystem fs, String fullPath) throws Exception{
        JClicProject jcp=new JClicProject(rb, fs, fullPath);
        //jcp.load(e);
        jcp.setProperties(e, null);
        return jcp;
    }
    
    public static JClicProject getJClicProject(ResourceBridge rb, String fullPath, ProgressDialog pd) throws Exception{
        
        JClicProject result=null;
        
        if(pd!=null)
            pd.setText(rb.getMsg("msg_loading")+" "+fullPath);
                        
        FileSystem fileSystem = FileSystem.createFileSystem(fullPath, rb);
        
        fullPath=fileSystem.getUrl(fullPath);
        if(fullPath.startsWith("file://"))
            fullPath=fullPath.substring(7);
                            
        String projectName;
        if(fullPath.endsWith(".jclic.zip")){
            fileSystem=FileSystem.createFileSystem(fullPath, rb);
            String[] projects=((ZipFileSystem)fileSystem).getEntries(".jclic");
            if(projects==null)
                throw new Exception("File "+fullPath+" does not contain any jclic project");
            projectName=projects[0];
        }
        else{
            fileSystem=new FileSystem(FileSystem.getPathPartOf(fullPath), rb);
            projectName=FileSystem.getFileNameOf(fullPath);
        }
                            
        if(projectName.endsWith(".jclic")){
            org.jdom.Document doc=fileSystem.getXMLDocument(projectName);
            System.gc();            
            result=getJClicProject(doc.getRootElement(), rb, fileSystem, fullPath);            
        }

        if(result!=null){
            result.mediaBag.waitForAllImages();
            //System.gc();
        }
        
        return result;        
    }
        
    
    public org.jdom.Document getDocument(){
        return new org.jdom.Document(getJDomElement());
    }
    
    public void saveDocument(OutputStream out) throws Exception{
        JDomUtility.saveDocument(out, getDocument());
    }
    
    public void saveZipDocument(OutputStream out, boolean includeMedia) throws Exception{
        ZipOutputStream zos=new ZipOutputStream(out);
        zos.putNextEntry(new ZipEntry(name+".jclic"));
        saveDocument(zos);
        zos.closeEntry();
        if(includeMedia){

            Set<String> set=Collections.synchronizedSet(new HashSet<String>());
            Iterator<MediaBagElement> mbit=mediaBag.getElements().iterator();
            while(mbit.hasNext()){
                MediaBagElement mbe=mbit.next();
                if(mbe.saveFlag){
                    String fName=mbe.getFileName();
                    if(fName!=null && fName.length()>0)
                        set.add(fName);
                }    
            }
            for (String fName : set) {
                zos.putNextEntry(new ZipEntry(fName));
                zos.write(fileSystem.getBytes(fName));
                zos.closeEntry();
            }
        }
        zos.close();
        out.close();
    }
    
    public void saveZipDocumentPreservingZipContents(OutputStream out, ZipFileSystem zfs) throws Exception{
        ZipOutputStream zos=new ZipOutputStream(out);
        zos.putNextEntry(new ZipEntry(name+".jclic"));
        saveDocument(zos);
        zos.closeEntry();
        String[] entries=zfs.getEntries(null);
        if(entries!=null){
            for(String entry : entries){
                if(!entry.endsWith(".jclic")){
                    zos.putNextEntry(new ZipEntry(entry));
                    zos.write(zfs.getBytes(entry));
                    zos.closeEntry();
                }                    
            }
        }        
        zos.close();
        out.close();
    }
    
    public void saveProject(String fileName) throws Exception{
        
        // Free discardable objects allocated in memory
        mediaBag.clearData();
        System.runFinalization();
        System.gc();
        // -------------

        String fn=fileSystem.getFullFileNamePath(fileName);        
        FileOutputStream fos=fileSystem.createSecureFileOutputStream(fn);
        saveZipDocument(fos, true);
        fos.close();
        setFileSystem(FileSystem.createFileSystem(fn, bridge));
        setFullPath(fn);
    }    
        
    public void setName(String newName){
        int p;
        name=StrUtils.secureString(newName, bridge.getMsg("UNNAMED"));
        name=(new File(name)).getName();
        if((p=name.indexOf('.'))>=0)
            name=name.substring(0, p);
        if(name.indexOf(' ')>=0)
            name=name.replace(' ', '_');
        if(name.length()<1) 
            name="NO_NAME";
    }
    
    public String getName(){
        return name;
    }
    
    public String getPublicName(){
        /*
        String result=name;
        if("noName".equals(name) || bridge.getMsg("UNNAMED").equals(name))
            result=settings.title;
        return result;
         */
        return settings.title;
    }
    
    public void realize(EventSounds parent, PlayStation ps) {
        
        if(skin==null && settings.skinFileName!=null && settings.skinFileName.length()>0)
            skin=mediaBag.getSkinElement(settings.skinFileName, ps);
        
        if(settings.eventSounds!=null){
            settings.eventSounds.setParent(parent);
            settings.eventSounds.realize(ps.getOptions(), mediaBag);
        }
        
        mediaBag.buildFonts();        
    }
    
    public void end(){
        if(settings.eventSounds!=null){
            settings.eventSounds.close();
            settings.eventSounds=null;
        }
        mediaBag.clearData();
        mediaBag.clear();
        fileSystem.close();
        System.gc();
    }
    
    @Override
    protected void finalize() throws Throwable{
        end();
        super.finalize();
    }        
    
    /** Getter for property resourceBridge.
     * @return Value of property resourceBridge.
     */
    public edu.xtec.util.ResourceBridge getBridge() {
        return bridge;
    }
    
    /** Setter for property resourceBridge.
     * @param resourceBridge New value of property resourceBridge.
     */
    public void setBridge(edu.xtec.util.ResourceBridge bridge) {
        this.bridge = bridge;
    }
    
    /** Getter for property fileSystem.
     * @return Value of property fileSystem.
     */
    public edu.xtec.jclic.fileSystem.FileSystem getFileSystem() {
        return fileSystem;
    }
    
    /** Setter for property fileSystem.
     * @param fileSystem New value of property fileSystem.
     */
    public void setFileSystem(edu.xtec.jclic.fileSystem.FileSystem fileSystem) {
        if(this.fileSystem!=null && this.fileSystem!=fileSystem)
            this.fileSystem.close();
        this.fileSystem = fileSystem;
    }
    
    /** Getter for property fullPath.
     * @return Value of property fullPath.
     */
    public java.lang.String getFullPath() {
        return fullPath;
    }
    
    /** Setter for property fullPath.
     * @param fullPath New value of property fullPath.
     */
    public void setFullPath(java.lang.String fullPath) {
        this.fullPath = fullPath;
    }
    
    public Editor getEditor(Editor parent) {
        return Editor.createEditor("edu.xtec.jclic.project.JClicProjectEditor", this, parent);
        //return Editor.createEditor(getClass().getName()+"Editor", this, parent);
    }
    
}
