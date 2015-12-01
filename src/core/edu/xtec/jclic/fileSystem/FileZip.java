/*
 * File    : FileZip.java
 * Created : 25-sep-2001 10:53
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
import edu.xtec.util.StreamIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class FileZip extends ZipFileSystem{

    protected ZipFile zip;
    
    /** Creates new ZipFileSystem */
    public FileZip(String rootPath, String fName, ResourceBridge rb) throws Exception{
        super(rootPath, fName, rb);
        open();
    }
    
    @Override
    protected void open() throws Exception{
        if(zip==null){
            //zip=new ZipFile(sysFn(root+zipName));
            zip=new ZipFile(sysFn(root+zipName));
            Enumeration en=zip.entries();
            ArrayList<FileZipEntry> v=new ArrayList<FileZipEntry>();
            while(en.hasMoreElements())
                v.add(new FileZipEntry((ZipEntry)en.nextElement()));
            entries=v.toArray(new FileZipEntry[v.size()]);
        }
    }
    
    protected class FileZipEntry extends ExtendedZipEntry{
        FileZipEntry(ZipEntry entry){
            super(entry);
        }
        public byte[] getBytes() throws IOException{
            return StreamIO.readInputStream(getInputStream());
        }        
        public InputStream getInputStream() throws IOException{
            InputStream is=zip.getInputStream(this);
            if(rb!=null)
                is=rb.getProgressInputStream(is, (int)getSize(), getName());
            return is;
        }
    }
        
    @Override
    public void close(){
        if(zip!=null){
            try{
                zip.close();
            }catch(Exception ex){
                // eat exception
            }            
            zip=null;
        }
        super.close();
    }    
    
    @Override
    protected void changeBase(String newRoot, String newFileName) throws Exception{
        if(zip!=null)
            throw new Exception("Unable to change base fileName: FileSystem is open!");
        super.changeBase(newRoot, newFileName);
        zipName = getCanonicalNameOf(newFileName);
        entries=null;
        loaded=false;        
    }
}
