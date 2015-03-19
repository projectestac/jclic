/*
 * File    : LFUtil.java
 * Created : 24-oct-2001 21:22
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.SwingUtilities;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public abstract class LFUtil {
    
    /** Key for Options
     */
    public static final String LOOK_AND_FEEL="lookAndFeel";
    
    /** Default look &amp; feel name
     */
    public static final String DEFAULT="default";
    /** System look &amp; feel name
     */
    public static final String SYSTEM="system";
    /** Metal look &amp; feel name
     */
    public static final String METAL="metal";
    /** Motif look &amp; feel name
     */
    public static final String MOTIF="motif";
    /** Windows look &amp; feel name
     */
    public static final String WINDOWS="windows";
    
    public static final String[] VALUES={DEFAULT, SYSTEM, METAL, MOTIF};
    
    /** Sets the app look &amp; feel
     * @param friendlyName Look &amp; feel name. If null, empty or not recognized this function does nohing.
     */
    public static void setLookAndFeel(String friendlyName, Component rootComponent){
        if(friendlyName!=null){
            try{
                if(friendlyName.equals(DEFAULT))
                    setLookAndFeel(null, javax.swing.UIManager.getCrossPlatformLookAndFeelClassName(), rootComponent);
                else if(friendlyName.equals(SYSTEM))
                    setLookAndFeel(null, javax.swing.UIManager.getSystemLookAndFeelClassName(), rootComponent);
                else if(friendlyName.equals(METAL))
                    setLookAndFeel("javax.swing.plaf", "metal.MetalLookAndFeel", rootComponent);
                else if(friendlyName.equals(MOTIF))
                    setLookAndFeel("com.sun.java.swing.plaf", "motif.MotifLookAndFeel", rootComponent);
                else if(friendlyName.equals(WINDOWS))
                    setLookAndFeel("com.sun.java.swing.plaf", "windows.WindowsLookAndFeel", rootComponent);
            }
            catch(Exception ex){
                System.err.println("unable to set lookAndFeel to: \""+friendlyName+"\"\n"+ex);
            }
        }
    }
    
    private static void setLookAndFeel(String prefix, String className, Component rootComponent) throws Exception{
        String cl=(prefix!=null && prefix.length()>0) ? prefix+"."+className : className;
        javax.swing.UIManager.setLookAndFeel(cl);
        if(rootComponent!=null)
            SwingUtilities.updateComponentTreeUI(rootComponent);
    }

    public static Color getSysColor(String key, Color defaultValue){
        Color result=javax.swing.UIManager.getColor(key);
        return result==null ? defaultValue : result;
    }
    
    public static Color getColor(String key, Color defaultValue){
        Color result=defaultValue;
        Object o=javax.swing.UIManager.get(key);
        if(o!=null && o instanceof Color)
            result=(Color)o;
        return result;            
    }
    
    public static Font getFont(String key, Font defaultValue){
        Font result=defaultValue;
        Object o=javax.swing.UIManager.get(key);
        if(o!=null && o instanceof Font)
            result=(Font)o;
        return result;            
    }
    
}
