/*
 * File    : TripleString.java
 * Created : 28-apr-2003 10:14
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

package edu.xtec.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class TripleString extends Object implements Comparable<TripleString>{
    
    public static final int ELEMENTS=3;
    public static final int NAME=0, CLASS=1, DESCRIPTION=2;
    public static final char SEP='|';
    
    String[] str;
    
    /** Creates a new instance of TripleString */
    public TripleString(String name, String className, String description) {
        str=new String[ELEMENTS];
        str[NAME]=name;
        str[CLASS]=className;
        str[DESCRIPTION]=description;
    }
    
    public TripleString(String className, String nameAndDescription){
        str=new String[ELEMENTS];
        str[CLASS]=className;
        if(nameAndDescription!=null){
            nameAndDescription=nameAndDescription.trim();
            int p=nameAndDescription.indexOf(SEP);
            if(p>=0){
                str[NAME]=nameAndDescription.substring(0, p);
                str[DESCRIPTION]=nameAndDescription.substring(p+1);
            } else{
                str[NAME]=nameAndDescription;
            }
        }
    }
    
    public String getStr(int index){
        return (index>=0 && index<ELEMENTS) ? str[index] : null;
    }
    
    public void setStr(int index, String s){
        if(index>=0 && index<ELEMENTS)
            str[index] =s;
    }
    
    @Override
    public String toString(){
        return str[NAME];
    }
    
    public String getDescription(){
        return str[DESCRIPTION];
    }
    
    public String getClassName(){
        return str[CLASS];
    }
    
    @Override
    public boolean equals(Object obj){
        boolean result=false;
        if(obj!=null){
            result=obj.equals(str[NAME]);
        }
        return result;
    }
    
    public static int getFirstItemWithClass(List<TripleString> tripleListObjects, String className){
        int result=-1;
        if(className!=null && tripleListObjects!=null && !tripleListObjects.isEmpty()){
            for(int i=0; i<tripleListObjects.size(); i++){
                TripleString ts=tripleListObjects.get(i);
                if(className.equals(ts.getClassName())){
                    result=i;
                    break;
                }
            }
        }
        return result;
    }
    
    public static List<TripleString> getTripleList(String bundlePath, Options options, boolean includeEmpty, boolean sorted, boolean lookInUserDir) throws Exception{
        java.util.ResourceBundle bundle=ResourceManager.getBundle(bundlePath, options.getMessages().getLocale());
        java.util.Enumeration keys=bundle.getKeys();
        List<TripleString> result=new ArrayList<TripleString>();
        while(keys.hasMoreElements()){
            String key=((String)keys.nextElement()).trim();
            String str=bundle.getString(key);
            if(str!=null)
                result.add(new TripleString(key, str));            
        }
        if(lookInUserDir){
            String bundleName=bundlePath;
            int k=bundleName.lastIndexOf('.');
            if(k>0)
                bundleName=bundleName.substring(k+1);
            File file=new File(System.getProperty("user.home"), bundleName+".properties");
            if(file.exists()){
                Properties prop=new Properties();
                prop.load(new FileInputStream(file));
                Iterator it=prop.keySet().iterator();
                while(it.hasNext()){
                    String key=(String)it.next();
                    String str=prop.getProperty(key);
                    if(str!=null)
                        result.add(new TripleString(key, str));            
                }
            }
        }
        
        if(sorted)
            java.util.Collections.sort(result);        
        if(includeEmpty){
            result.add(0, new TripleString(options.getMsg("NONE"), null, null));
        }
        return result;
    }
    
    public int compareTo(TripleString obj) {
        //String s1=(obj instanceof TripleString) ? ((TripleString)obj).getClassName() : obj==null ? "" : obj.toString();
        return getClassName().compareTo(obj.getClassName());
    }
    
}
