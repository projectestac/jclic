/*
 * File    : MenuElement.java
 * Created : 21-may-2002 9:28
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

import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import javax.swing.ImageIcon;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class MenuElement implements Editable, Cloneable, Domable{

    public String caption;
    public String icon;
    public String projectPath;
    public String sequence;
    public String description;
    
    public MenuElement(){
        icon=DEFAULT_ICON;
    }
    
    public static final String ELEMENT_NAME="menuElement";
    public static final String CAPTION="caption", ICON="icon", PATH="path", SEQUENCE="sequence", DESCRIPTION="description";
    public static final String RETURN_TAG="@RETURN";
    public static final String DEFAULT_ICON="@ico00.png", DEFAULT_FOLDER_ICON="@icofolder.png";
    public static final int MAX_ICON_WIDTH=32, MAX_ICON_HEIGHT=32;
    
    public org.jdom.Element getJDomElement(){
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        
        if(caption!=null)
            e.setAttribute(CAPTION, caption);
        if(icon!=null && !icon.equals(DEFAULT_ICON))
            e.setAttribute(ICON, icon);
        if(projectPath!=null)
            e.setAttribute(PATH, projectPath);
        if(sequence!=null)
            e.setAttribute(SEQUENCE, sequence);
        if(description!=null)
            e.setAttribute(DESCRIPTION, description);
        return e;
    }
    
    public ImageIcon getIcon(MediaBag mb){
        ImageIcon result=null;
        if(icon.startsWith("@")){
            String fn=icon.substring(1);
            // ico files converted to png
            if(fn.endsWith(".gif") && fn.startsWith("ico"))
                fn=fn.substring(0, fn.length()-4)+".png";
            result=edu.xtec.util.ResourceManager.getImageIcon("icons/"+fn);
        }
        else{
            try{
                result=new ImageIcon(mb.getImageElement(icon).getImage());
                result.setDescription(icon);
            } catch(Exception ex){
                System.err.println("Error reading image "+icon+"\n"+ex);
            }
        }
        
        if(result==null)
            result=edu.xtec.util.ResourceManager.getImageIcon("icons/ico00.png");
        
        return result;
    }
    
    public static MenuElement getMenuElement(org.jdom.Element e) throws Exception{
        JDomUtility.checkName(e, ELEMENT_NAME);
        MenuElement me=new MenuElement();
        me.setProperties(e, null);
        return me;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        JDomUtility.checkName(e, ELEMENT_NAME);
        caption=JDomUtility.getStringAttr(e, CAPTION, caption, false);
        sequence=JDomUtility.getStringAttr(e, SEQUENCE, sequence, false);
        description=JDomUtility.getStringAttr(e, DESCRIPTION, description, false);
        projectPath=FileSystem.stdFn(JDomUtility.getStringAttr(e, PATH, projectPath, false));
        icon=projectPath==null ? DEFAULT_FOLDER_ICON : DEFAULT_ICON;
        icon=FileSystem.stdFn(JDomUtility.getStringAttr(e, ICON, icon, false));
    }
    
    public Editor getEditor(Editor parent){
        return Editor.createEditor(getClass().getName()+"Editor", this, parent);
    }        
    
}
