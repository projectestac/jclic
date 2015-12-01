/*
 * File    : ZipFileSystem.java
 * Created : 24-sep-2001 19:48
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

package edu.xtec.jclic.fileSystem;

import edu.xtec.util.ResourceBridge;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public abstract class ZipFileSystem extends FileSystem {
    
    String zipName;
    protected boolean loaded;
    ExtendedZipEntry[] entries;

    public ZipFileSystem(ResourceBridge rb) {
        super("", rb);
        entries=null;
        zipName=null;
        loaded=false;
    }
    
    public ZipFileSystem(String rootPath, String zipFileName, ResourceBridge rb) {
        super(rootPath, rb);
        zipName = getCanonicalNameOf(zipFileName);
        entries=null;
        loaded=false;
    }
    
    public static ZipFileSystem createZipFileSystem(String rootPath, String zipFileName, ResourceBridge rb) throws Exception {
        if (isStrUrl(rootPath))
            return new UrlZip(rootPath, zipFileName, rb);
        else
            return new FileZip(rootPath, zipFileName, rb);
    }
    
    @Override
    public String getFullRoot(){
        return root+zipName;
    }
    
    public String getZipName(){
      return zipName;
    }
    
    public abstract class ExtendedZipEntry extends ZipEntry{
        public boolean ignore;
        ExtendedZipEntry(ZipEntry entry){
            super(entry);
        }
        public abstract byte[] getBytes() throws IOException;
        public abstract InputStream getInputStream() throws IOException;
        @Override
        public String getName(){
            return stdFn(super.getName());
        }
    }
    
    public ExtendedZipEntry getEntry(String fName){
        if(fName==null || fName.length()==0 || entries==null) return null;
        String name=getCanonicalNameOf(fName);
        for(ExtendedZipEntry eze : entries)
            if(!eze.ignore && eze.getName().equals(name))
                return eze;
        return null;
    }    
            
    @Override
    public byte[] getBytes(String fileName) throws IOException {
        //System.out.println("requesting "+fileName);
        ExtendedZipEntry entry;
        if((entry=getEntry(fileName))==null)
            return super.getBytes(fileName);        
        else return entry.getBytes();
    }
    
    @Override
    public boolean fileExists(String fName){
        return super.fileExists(fName);
    }
    
    @Override
    public long getFileLength(String fileName) throws IOException{
        ExtendedZipEntry entry;
        if((entry=getEntry(fileName))==null)
            return super.getFileLength(fileName);        
        else return entry.getSize();
    }
    
    @Override
    public Image getImageFile(String fName) throws Exception {        
        ExtendedZipEntry entry;    
        if((entry=getEntry(fName))==null)
            return super.getImageFile(fName);
        
        return Toolkit.getDefaultToolkit().createImage(entry.getBytes());
    }
    
    @Override
    public InputStream getInputStream(String fName) throws IOException{
        ExtendedZipEntry entry;        
        if((entry=getEntry(fName))==null)
            return super.getInputStream(fName);
        
        return entry.getInputStream();
    }
    
    @Override
    public Object getMediaDataSource(String fName) throws Exception{
        ExtendedZipEntry entry;        
        if((entry=getEntry(fName))==null)
            return super.getMediaDataSource(fName);
        
        return new edu.xtec.util.ExtendedByteArrayInputStream(entry.getBytes(), fName);
    }
    
    public String[] getEntries(String ext) throws Exception{
        String[] result=null;
        if(entries!=null && entries.length>0){
            ArrayList<String> v=new ArrayList<String>();
            for(ExtendedZipEntry eze : entries){
                String entryName=eze.getName();
                if(ext==null || entryName.endsWith(ext))
                    v.add(entryName);
            }
            if(!v.isEmpty())
                result=v.toArray(new String[v.size()]);
        }
        return result;
    }
    
    @Override
    public void close(){
        entries=null;
        super.close();
    }    
}
