/*
 * File    : ReportUtils.java
 * Created : 05-feb-2003 17:17
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

package edu.xtec.jclic.report;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public abstract class ReportUtils {
        

    public static final SimpleDateFormat SDF=new SimpleDateFormat("yyyy-MM-dd");
    
    // Converteix dates en format "yyyy/MM/dd" a objectes Date
    public static Date strToDate(String s) throws Exception{
        return strToDate(s, false);
    }
    
    public static Date strToDate(String s, boolean atMidnight) throws Exception{        
        GregorianCalendar gc=null;
        if(s.indexOf('/')>0 || s.indexOf('-')>0){
            StringTokenizer st=new StringTokenizer(s, "/- ");
            gc=new GregorianCalendar(
            Integer.parseInt(st.nextToken()),
            Integer.parseInt(st.nextToken())-1,
            Integer.parseInt(st.nextToken()),
            atMidnight ? 23 : 0,
            atMidnight ? 59 : 0,
            atMidnight ? 59 : 0);
        } 
        else if(s.length()>=8){
            gc=new GregorianCalendar(
            Integer.parseInt(s.substring(0, 4)),
            Integer.parseInt(s.substring(4, 6))-1,
            Integer.parseInt(s.substring(6, 8)),
            atMidnight ? 23 : 0,
            atMidnight ? 59 : 0,
            atMidnight ? 59 : 0);
        }
        else
            throw new Exception("Bad date: "+s);
        
        return gc.getTime();
    }
    
    public static String dateToStr(Date d) throws Exception{
        return SDF.format(d);
    }
    
    
    public static final String ALPHANUM_CHARS="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; 
    public static final String VALID_URL_CHARS="-_.!~*'()";
    
    public static String urlEncode(String src, boolean strict, boolean spaces){
        if(src==null || src.length()<1)
            return src;
        
        int len=src.length();
        StringBuilder sb=new StringBuilder(src.length()*2);
        for(int i=0; i<len; i++){            
            char ch=src.charAt(i);
            if(ALPHANUM_CHARS.indexOf(ch)>=0 
            || (!strict && VALID_URL_CHARS.indexOf(ch)>=0)
            || (spaces && ch==' '))
                sb.append(ch);
            else{
                String s=Integer.toHexString(ch);
                if(ch<16)
                    sb.append("%0").append(s);
                else if(ch>255)
                    sb.append("%26%23").append(s).append("%3B");
                else
                    sb.append("%").append(s);
            }
        }
        return sb.substring(0);        
    }        
}
