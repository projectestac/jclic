/*
 * File    : MediaContent.java
 * Created : 25-apr-2001 15:33
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

package edu.xtec.jclic.media;

import edu.xtec.jclic.*;
import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.*;
import java.awt.Point;
import java.util.Map;


/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class MediaContent extends Object implements Cloneable, Domable {

    public static final int UNKNOWN=0, PLAY_AUDIO=1,
    PLAY_VIDEO=2, PLAY_MIDI=3, PLAY_CDAUDIO=4,
    RECORD_AUDIO=5, PLAY_RECORDED_AUDIO=6,
    RUN_CLIC_ACTIVITY=7, RUN_CLIC_PACKAGE=8,
    RUN_EXTERNAL=9, URL=10, EXIT=11, RETURN=12, NUM_MEDIA_TYPES=13;
    
    public static final String[] mediaName={
            "UNKNOWN", "PLAY_AUDIO", "PLAY_VIDEO", "PLAY_MIDI", "PLAY_CDAUDIO",
            "RECORD_AUDIO", "PLAY_RECORDED_AUDIO", "RUN_CLIC_ACTIVITY",
            "RUN_CLIC_PACKAGE", "RUN_EXTERNAL", "URL", "EXIT", "RETURN"
        };
    public static final int FROM_BOX=0, FROM_WINDOW=1, FROM_FRAME=2;
    public static final String[] fromName={"BOX", "WINDOW", "FRAME"};

    public int mediaType=UNKNOWN;
    public int level=1;
    public String mediaFileName=null;
    public String externalParam=null;
    public int from=-1;
    public int to=-1;
    public int length=3;
    public String cdFrom=null;
    public String cdTo=null;
    public int recBuffer=0;
    //public boolean fixedAspectRatio=true;
    public boolean stretch=false;
    public boolean free=false;
    public Point absLocation=null;
    public int absLocationFrom=FROM_BOX;
    public boolean loop=false;
    public boolean catchMouseEvents=false;
    public boolean autoStart=false;

    /** Creates new MediaContent */
    public MediaContent() {
    }
    
    public static final String ELEMENT_NAME="media";
    protected static final String
      TYPE="type", LEVEL="level", FILE="file", PARAMS="params", FROM="from", TO="to",
      CDFROM="cdFrom", CDTO="cdTo", BUFFER="buffer", LENGTH="length", STRETCH="stretch",
      //RETAIN_ASPECT="retainAspect",
      FREE="free", PX="px", PY="py", PFROM="pFrom", LOOP="loop", CATCH_MOUSE="catchMouseEvents",
      AUTOSTART="autoStart";
    
    public org.jdom.Element getJDomElement(){
        
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        
        e.setAttribute(TYPE, mediaName[mediaType]);
        e.setAttribute(LEVEL, Integer.toString(level));
        if(mediaFileName!=null) e.setAttribute(FILE, mediaFileName);
        if(externalParam!=null) e.setAttribute(PARAMS, externalParam);
        if(from!=-1) e.setAttribute(FROM, Integer.toString(from));
        if(to!=-1) e.setAttribute(TO, Integer.toString(to));
        if(cdFrom!=null) e.setAttribute(CDFROM, cdFrom);
        if(cdTo!=null) e.setAttribute(CDTO, cdTo);
        if(mediaType==RECORD_AUDIO || mediaType==PLAY_RECORDED_AUDIO){
            e.setAttribute(BUFFER, Integer.toString(recBuffer));
            if(mediaType==RECORD_AUDIO) e.setAttribute(LENGTH, Integer.toString(length));
        }
        if(mediaType==PLAY_VIDEO){
            //e.setAttribute(RETAIN_ASPECT, JDomUtility.boolString(fixedAspectRatio));
            e.setAttribute(STRETCH, JDomUtility.boolString(stretch));
            e.setAttribute(FREE, JDomUtility.boolString(free));
            if(catchMouseEvents)
                e.setAttribute(CATCH_MOUSE, JDomUtility.boolString(catchMouseEvents));
            if(absLocation!=null){
                e.setAttribute(PX, Integer.toString(absLocation.x));
                e.setAttribute(PY, Integer.toString(absLocation.y));
                e.setAttribute(PFROM, fromName[absLocationFrom]);
            }
        }
        if(loop) e.setAttribute(LOOP, JDomUtility.boolString(loop));
        if(autoStart) e.setAttribute(AUTOSTART, JDomUtility.boolString(autoStart));
        return e;
    }
    
    public static MediaContent getMediaContent(org.jdom.Element e) throws Exception{
        
        MediaContent mc=new MediaContent();
        mc.setProperties(e, null);
        return mc;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        
        JDomUtility.checkName(e, ELEMENT_NAME);
        
        mediaType=JDomUtility.getStrIndexAttr(e, TYPE, mediaName, -1);
        if(mediaType==-1)
            throw new IllegalArgumentException("Unknown media type: "+mediaName);
        level=JDomUtility.getIntAttr(e, LEVEL, level);
        mediaFileName=FileSystem.stdFn(e.getAttributeValue(FILE));
        externalParam=e.getAttributeValue(PARAMS);
        from=JDomUtility.getIntAttr(e, FROM, from);
        to=JDomUtility.getIntAttr(e, TO, to);
        cdFrom=e.getAttributeValue(CDFROM);
        cdTo=e.getAttributeValue(CDTO);
        recBuffer=JDomUtility.getIntAttr(e, BUFFER, recBuffer);
        length=JDomUtility.getIntAttr(e, LENGTH, length);
        stretch=JDomUtility.getBoolAttr(e, STRETCH, stretch);
        // bad identifier in beta 0
        //mc.fixedAspectRatio=JDomUtility.getBoolAttr(e, RETAIN_ASPECT, mc.fixedAspectRatio);
        stretch=JDomUtility.getBoolAttr(e, "retainAspect", stretch);
        // --------
        free=JDomUtility.getBoolAttr(e, FREE, free);
        absLocationFrom=JDomUtility.getStrIndexAttr(e, PFROM, fromName, absLocationFrom);
        absLocation=JDomUtility.getPointAttr(e, PX, PY, absLocation);
        loop=JDomUtility.getBoolAttr(e, LOOP, loop);
        catchMouseEvents=JDomUtility.getBoolAttr(e, CATCH_MOUSE, catchMouseEvents);
        autoStart=JDomUtility.getBoolAttr(e, AUTOSTART, autoStart);
    }
    
    public static void listReferences(org.jdom.Element e, Map<String,String> map){
        if(e!=null){
            String s=e.getAttributeValue(FILE);
            String p=e.getAttributeValue(PARAMS);
            int k=0;
            if(s!=null && s.length()>0){
                int type;
                try{
                    type=JDomUtility.getStrIndexAttr(e, TYPE, mediaName, -1);
                } catch(Exception ex){
                    System.err.println("error:\n"+ex);
                    return;
                }
                switch(type){
                    case PLAY_AUDIO:
                    case PLAY_VIDEO:
                    case PLAY_MIDI:
                        map.put(s, Constants.MEDIA_OBJECT);
                        break;
                    case RUN_CLIC_ACTIVITY:
                        k=1;
                    case RUN_CLIC_PACKAGE:
                        map.put(s, 
                        p!=null ? Constants.EXTERNAL_OBJECT : 
                            k>0 ? Constants.ACTIVITY_OBJECT : 
                                Constants.SEQUENCE_OBJECT);
                        if(p!=null)
                            map.put(p, Constants.EXTERNAL_OBJECT);                                
                        break;
                    case RUN_EXTERNAL:                        
                        map.put((p==null ? s : s+" "+p), Constants.EXTERNAL_OBJECT);
                        break;
                    case URL:
                        map.put(s, Constants.URL_OBJECT);
                        break;
                }
            }
        }
    }
    
    public String getIconName(){
        String s="unknown.gif";
        switch(mediaType){
            case PLAY_AUDIO:
                s="icowave.png";
                break;
            case PLAY_MIDI:
                s="icomidi.png";
                break;
            case PLAY_VIDEO:
                s="icomci.png";
                break;
            case RETURN:
            case RUN_CLIC_ACTIVITY:
            case RUN_CLIC_PACKAGE:
                s="ico00.png";
                break;
            case RECORD_AUDIO:
                s="icorec.png";
                break;
            case PLAY_RECORDED_AUDIO:
                s="icorplay.png";
                break;
            case EXIT:
                s="icoexit.png";
                break;
            case URL:
            case RUN_EXTERNAL:
                s="icoexe.png";
                break;
            default:
                break;
        }
        return s;
    }
    
    public void registerContentTo(MediaBag mb) throws Exception{
        if(mediaFileName!=null){
            switch(mediaType){
                case PLAY_AUDIO:
                case PLAY_MIDI:
                case PLAY_VIDEO:
                    mb.registerElement(mediaFileName, null);
                    break;
                    
                default:
                    break;
            }
        }
    }
    
    public boolean isEquivalent(MediaContent mc){
       return mc!=null &&
           mediaType==mc.mediaType &&
           StrUtils.compareStringsIgnoreCase(mediaFileName, mc.mediaFileName) &&
           StrUtils.compareStringsIgnoreCase(externalParam, mc.externalParam) &&
           from==mc.from &&
           to==mc.to &&
           StrUtils.compareStringsIgnoreCase(cdFrom, mc.cdFrom) &&
           StrUtils.compareStringsIgnoreCase(cdTo, mc.cdTo) &&
           recBuffer==mc.recBuffer &&
           stretch==mc.stretch &&
           free==mc.free &&
           StrUtils.compareObjects(absLocation, mc.absLocation) &&
           absLocationFrom==mc.absLocationFrom &&
           loop==mc.loop &&
           level==mc.level;
    }
    
    public String getDescription(){
        StringBuilder result=new StringBuilder();
        result.append(mediaName[mediaType]);
        if(mediaFileName!=null){
            result.append(" ").append(mediaFileName);
            if(from>=0) result.append(" ").append("from:").append(from);
            if(to>=0) result.append(" ").append("to:").append(to);            
        }
        else if(externalParam!=null){
            result.append(" ").append(externalParam);
        }
        return result.substring(0);
    }
    
    @Override
    public Object clone(){
        MediaContent mc=null;
        try{
            mc=(MediaContent)super.clone();
            if(absLocation!=null)
                mc.absLocation=(Point)absLocation.clone();            
        } catch(Exception ex){
            System.err.println("Unexpected error cloning MediaContent!");
        }
        return mc;
    }
    
}