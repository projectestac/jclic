/*
 * File    : Basic.java
 * Created : 23-jan-2003 17:41
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

package edu.xtec.jclic.report.rp;

import edu.xtec.jclic.report.*;
import edu.xtec.util.CompoundObject;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public abstract class Basic extends ReportsRequestProcessor {
        
    // Messages
    public static final String BUNDLE="edu.xtec.resources.messages.reportMessages";
    public static final String LANG="lang";
    
    public static final long DEFAULT_SESSION_LIFETIME=20L;
    
    // Parameters
    public static final String ACTION="action", PWD="pwd", RETRY="retry",
    ON="on", MAIN_FORM="mainForm";
    
    // Authentication cookie
    public static final String AUTH="AUTH";
    
    protected static Map<String, Object[]> localeObjects=new HashMap<String, Object[]>();
    protected static Map<String, Long> sessions=new HashMap<String, Long>();
    
    // Non-static variables
    protected boolean retry;
    protected String auth;
    protected ResourceBundle bundle;
    protected String[] months;
    protected DateFormat shortDateFormat, veryShortDateFormat;
    protected NumberFormat numberFormat;
    protected String lang;
    
    private static String[] F_NUMBERS;
    public static String[] getFormattedNumbers(){
        if(F_NUMBERS==null){
            java.text.DecimalFormat df=new java.text.DecimalFormat("00");
            List<String> v=new ArrayList<String>(100);
            for(int i=0; i<100; i++)
                v.add(df.format(i));
            F_NUMBERS=(String[])v.toArray(new String[100]);
        }
        return F_NUMBERS;
    }
    
    public abstract String getTitle(ResourceBundle bundle);
    public abstract String getUrl();
    
    public String getMsg(String key){
        return filter(bundle.getString(key));
    }
    
    @Override
    public boolean init() throws Exception{
        
        if(!super.init())
            return false;
        
        lang=getParam(LANG);
        if(lang==null || lang.length()!=2)
            lang=prop.getProperty("language", Locale.getDefault().getLanguage());
        else
            lang=lang.toLowerCase();
        
        Object[] obj=localeObjects.get(lang);
        if(obj!=null){
            bundle=(ResourceBundle)obj[0];
            months=(String[])obj[1];
            shortDateFormat=(DateFormat)obj[2];
            veryShortDateFormat=(DateFormat)obj[3];
            numberFormat=(NumberFormat)obj[4];
        }
        else{
            Locale l=new Locale(lang, "");
            bundle=ResourceBundle.getBundle(BUNDLE, l);
            if(bundle==null)
                throw new Exception("Internal error!");
            months=new String[12];
            for(int i=0; i<12; i++)
                months[i]=bundle.getString("month_"+(i+1));
            shortDateFormat=DateFormat.getDateInstance(DateFormat.SHORT, l);
            veryShortDateFormat=new SimpleDateFormat("dd/MM", l);
            //numberFormat=NumberFormat.getIntegerInstance(l);
            numberFormat=NumberFormat.getInstance(l);
            obj=new Object[]{bundle, months, 
            shortDateFormat, veryShortDateFormat, numberFormat};
            localeObjects.put(lang, obj);
        }
                
        checkAuth();
        if(auth!=null)
            setCookie(AUTH, auth);
        return auth!=null;
    }
    
    protected boolean checkAuth(){
       
       auth=getCookie(AUTH);
       if(auth!=null){
           Long timeCheck=sessions.get(auth);
           if(timeCheck!=null){
               long lifeTime;
               try{
                   lifeTime=Long.parseLong(prop.getProperty(SESSION_LIFETIME, Long.toString(DEFAULT_SESSION_LIFETIME)));
               } catch(NumberFormatException ex){
                   lifeTime=DEFAULT_SESSION_LIFETIME;
               }
               lifeTime=Math.max(1, lifeTime);
               if(timeCheck.longValue()<System.currentTimeMillis()-lifeTime*60*1000){
                   auth=null;
                   sessions.remove(auth);
               }
               else
                   sessions.put(auth, new Long(System.currentTimeMillis()));
           }
           else
               auth=null;
       }        
        
       if(auth==null){
            boolean ok=false;
            try{
                String sPwd=bridge.getProperty("PASSWORD", null);
                if (sPwd!=null && sPwd.length()>0){ //PASSWORD field exists, Maybe null
                    String sTypedPwd=getParam(PWD);
                    if(sTypedPwd!=null && sPwd.equals(edu.xtec.util.Encryption.Encrypt(sTypedPwd)))
                        ok=true;
                    else
                        retry=true;
                }
                else
                    ok=true;
            } catch(Exception ex){
                ok=false;
            }
            
            if(ok){
                char[] key=new char[12];
                for(int i=0; i<12; i++){
                    char k=(char)(Math.random()*36);
                    if(k<10)
                        key[i]=(char)('0'+k);
                    else
                        key[i]=(char)('A'+k-10);
                }
                auth=new String(key);
                sessions.put(auth, new Long(System.currentTimeMillis()));
            }
        }        
        return auth!=null;
    }
    
    @Override
    public void header(List<String[]> v){
        super.header(v);
        if(!checkAuth()){
            v.add(new String[]{REDIRECT, urlParam(Login.URL, RETRY, retry ? TRUE:FALSE)});
        }
    }
    
    @Override
    public void head(java.io.PrintWriter out) throws Exception{
        super.head(out);
        title(bundle.getString("jclic_reports"), getTitle(bundle), out);
        linkStyle(resourceUrl("basic.css"), resourceUrl("basic_print.css"), out);
    };
    
    public void standardHeader(java.io.PrintWriter out, String title, String menu) throws Exception{
        StringBuilder sb=new StringBuilder(300);
        sb.append("<h1>").append(getMsg("jclic_reports")).append("<br>").append(title).append("</h1>\n");
        sb.append("<p class=\"topMenu\">").append(menu).append("</p>");
        out.println(sb.substring(0));
    }
    
    protected boolean getBoolParam(String paramStr, String expectedValue){
        boolean result=false;
        String s=getParam(paramStr);
        if(s!=null && s.length()>0){
            if(expectedValue==null)
                result=true;
            else
                result=expectedValue.toLowerCase().equals(s.trim().toLowerCase());
        }
        return result;
    }
    
    public static String linkTo(String url, String text, String style){
        StringBuilder sb=new StringBuilder(url.length()+text.length()+200);
        sb.append("<a href=\"");
        // was "escape(url)"
        sb.append(url).append("\"");
        if(style!=null)
            sb.append(" class=\"").append(style).append("\"");
        sb.append(">");
        sb.append(filter(text));
        sb.append("</a>");
        return sb.substring(0);
    }
    
    public static String buttonAction(String action, String text, String extra){
        StringBuilder sb=new StringBuilder(text.length()+200);
        sb.append("<input type=\"button\" value=\"").append(filter(text)).append("\"");
        if(action!=null)
            sb.append(" onClick=\"").append(action).append("\"");
        if(extra!=null)
            sb.append(" ").append(extra);
        sb.append(" id=\"noPrint\">");
        return sb.substring(0);
    }
    
    public static String buttonTo(String url, String text, String extra){
        return buttonAction("window.location.href='"+escape(url)+"'", text, extra);
    }
    
    protected static String urlParam(String url, String key, String value){
        return urlParamSb(new StringBuilder(url.length()+100).append(url), key, value, url.indexOf('?')<0).substring(0);
    }
    
    protected static StringBuilder urlParamSb(StringBuilder sb, String key, String value, boolean first){
        if(value!=null && value.length()>0){
            sb.append(first ? "?" : "&");
            sb.append(key).append("=").append(ReportUtils.urlEncode(value, false, false));
        }
        return sb;
    }
    
    protected int getAction(String[] actions, int defaultAction){
        String action=getParam(ACTION);
        int result=defaultAction;
        if(action!=null){
            action=action.trim();
            for(int i=0; i<actions.length; i++){
                if(actions[i].equals(action)){
                    result=i;
                    break;
                }
            }
        }
        return result;
    }

    public String[][] vectorToArray(List v, boolean isCompoundObject){
        return vectorToArray(v, isCompoundObject, null, null);
    }
    
    public String[][] vectorToArray(List v, boolean isCompoundObject, String wildCardKey, String wildCardMsg){
        String[][] result=null;
        if(wildCardKey!=null || (v!=null && v.size()>0)){
            int l=(v!=null ? v.size() : 0);
            result=new String[l+(wildCardKey!=null ? 1 : 0)][];
            int k=0;
            if(wildCardKey!=null){
                String s=wildCardKey;
                if(wildCardMsg!=null)
                    s=getMsg(wildCardMsg);
                result[k++]=new String[]{wildCardKey, s};
            }
            if(v!=null){
                for(int i=0; i<l; i++){
                    String sKey, sValue;
                    if(isCompoundObject){
                        CompoundObject co=(CompoundObject)v.get(i);
                        sKey=co.getId();
                        sValue=co.getText();
                    }
                    else{
                        sKey=((String)v.get(i)).trim();
                        sValue=sKey;
                    }
                    result[k++]=new String[]{sKey, sValue};
                }
            }
        }
        return result;
    }        
}
