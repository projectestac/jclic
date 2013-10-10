/*
 * File    : StrUtils.java
 * Created : 17-may-2004 19:50
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.23
 */
public abstract class StrUtils {
    
    public static void strToIntArray(String str, int [] intArray){
        if(str==null || intArray==null) return;
        int i=0;
        StringTokenizer st= new StringTokenizer(str, ",", false);
        while(st.hasMoreTokens() && i<intArray.length){
            try{
                intArray[i]=Integer.parseInt(st.nextToken().trim());
            } catch (Exception e){
            }
            i++;
        }
    }
    
    public static void strToPoint(String str, java.awt.Point pt){
        int [] values=new int[2];
        strToIntArray(str, values);
        pt.x=values[0];
        pt.y=values[1];
    }
    
    public static void strToDimension(String str, java.awt.Dimension d){
        int [] values=new int[2];
        strToIntArray(str, values);
        d.width=values[0];
        d.height=values[1];
    }
    
    public static void strToRect(String str, java.awt.Rectangle r){
        int [] values=new int[4];
        strToIntArray(str, values);
        r.setBounds(values[0], values[1], values[2], values[3]);
    }
    
    public static String[] strToStrArray(String source, String separator){
        if(source==null || separator==null) return null;
        StringTokenizer st=new StringTokenizer(source, separator, true);
        List<String> v=new ArrayList<String>();
        while(st.hasMoreTokens()){
            String s=st.nextToken();
            if(separator.equals(s)) v.add(new String());
            else{
                v.add(s);
                if(st.hasMoreTokens()) st.nextToken();
            }
        }
        return v.toArray(new String[v.size()]);        
    }
    
    public static String[] strToStrArrayNoNulls(String source, String separator) throws Exception{
        String[] result=strToStrArray(source, separator);
        if(result==null || result.length==0)
            throw new Exception("Invalid parameter: "+source);
        for(String s : result)
            if(s==null || s.length()==0)
                throw new Exception("Invalid parameter: "+source);
        return result;
    }
    
    public static byte[] extractByteSeq(byte[] data, int line, byte searchFor, byte changeTo) {
        
        byte[] result=null;
        int l=data.length;
        int k=0;
        int p0=0, p1=0;
        
        for(int i=0; i<l; i++){
            if(data[i]==0x0D){
                if(i<l && data[i+1]==0x0A){
                    p0=p1==0 ? p1 : p1+2;
                    p1=i;
                    if(k==line) break;
                    k++;
                }
            }
        }
        
        if(p1>p0){
            int j=p1-p0;
            result=new byte[j];
            for(int i=0; i<j; i++){
                result[i]=data[p0+i];
                if(result[i]==searchFor) result[i]=changeTo;
            }
        }
        
        return result;
    }
    
    public static int roundTo(double v, int n){
        return ((int)(v/n))*n;
    }
    
    public static int countSpaces(String tx){
        String t=tx.trim();
        int j=0;
        for(char ch : t.toCharArray()){
            if(ch==' ') j++;
        }
        return j;
    }
    
    public static boolean compareStringsIgnoreCase(String s1, String s2){
        if(s1==null && s2==null) return true;
        if(s1==null || s2==null) return false;
        return s1.compareToIgnoreCase(s2)==0;
    }
    
    public static boolean compareTrimStringsIgnoreCase(Object s1, Object s2){
        if(s1==null)
            s1="";
        if(s2==null)
            s2="";
        return s1.toString().trim().compareToIgnoreCase(s2.toString().trim())==0;
    }
    
    public static boolean compareObjects(Object o1, Object o2){
        if(o1==null && o2==null) return true;
        if(o1==null || o2==null) return false;
        return o1.equals(o2);
    }
    
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        if(str==null)
            str="";
        StringBuilder result = new StringBuilder();
        
        int e;
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            if(replace!=null)
                result.append(replace);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        return result.substring(0);
    }
    
    public static String replace(String str, String pattern, String [] replace) {
        int s = 0;
        if(str==null)
            str="";
        StringBuilder result = new StringBuilder();

        int e, i=0;
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            if(replace!=null)
                result.append(replace[i++ % replace.length]);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        return result.substring(0);
    }

    public static boolean compareMultipleOptions(String answer, String check, boolean checkCase){
        if(answer==null || answer.length()==0 || check==null || check.length()==0) return false;
        StringTokenizer st=new StringTokenizer(check, "|");
        while(st.hasMoreTokens()){
            if(checkCase ? st.nextToken().equals(answer)
            : st.nextToken().equalsIgnoreCase(answer)) return true;
        }
        return false;
    }
    
    public static String secureString(Object data){
        if(data==null) return new String();
        return data.toString();
    }
    
    public static String secureString(Object data, String defaultValue){
        String result=nullableString(data);
        return result==null ? defaultValue : result;
    }
    
    public static String nullableString(Object o){
        String result=null;
        if(o!=null){
            result=o.toString().trim();
            if(result.length()==0)
                result=null;
        }
        return result;
    }
    
    public static String trimEnding(String str){
        String result=str;
        if(result!=null){
            int p=result.length()-1;
            int p0=p;
            while(p>=0){
                char ch=result.charAt(p);
                if(ch==' ' || ch=='\n' || ch=='\r')
                    p--;
                else
                    break;
            }
            if(p!=p0){
                result=result.substring(0, p+1);
            }
        }
        return result;
    }
    
    public static String secureSQLString(String data){
        return replace(secureString(data), "'", "''");
    }
    
    public static int getAbsIntValueOf(String s){
        int result=-1;
        if(s!=null && s.length()>0){
            for(char ch : s.toCharArray()){
                if(ch<'0' || ch>'9')
                    return result;
            }
            try{
                result=Integer.parseInt(s);
            } catch(NumberFormatException ex){
                result=-1;
            }
        }
        return result;
    }
    
    public static String getShortExpression(String text, int maxLen){
        String result=secureString(text).trim();
        if(maxLen>0 && result.length()>maxLen){
            result=result.substring(0, maxLen);
            for(int p=maxLen-1; p>(2*maxLen/3); p--){
                if(Character.isSpaceChar(result.charAt(p))){
                    result=result.substring(0, p)+"...";
                    break;
                }
            }
        }
        return result;
    }
    
    public static String limitStrLen(String text, int maxLen){
        String result=text;
        
        if(result!=null && result.length()>maxLen){
            result=result.substring(0, maxLen);
        }
        
        return result;
    }
    
}
