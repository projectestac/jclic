/*
 * File    : Utils.java
 * Created : 06-feb-2001 18:30
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

package edu.xtec.jclic.misc;

import edu.xtec.jclic.Constants;
import edu.xtec.util.Options;
import edu.xtec.util.SimpleFileFilter;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;


/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public abstract class Utils implements Constants{
    
    public static final int
    JCLIC_FF=0, JCLIC_ZIP_FF=1, PAC_FF=2, PCC_FF=3,
    ALL_JCLIC_FF=4, ALL_CLIC_FF=5, ALL_JCLIC_CLIC_FF=6,
    INSTALL_FF=7, GIF_FF=8, JPG_FF=9, PNG_FF=10, ALL_IMAGES_FF=11,
    ALL_FF=12,
    ALL_SOUNDS_FF=13, MIDI_FF=14, ALL_VIDEO_FF=15, ALL_ANIM_FF=16,
    SKINS_FF=17, FONTS_FF=18, ALL_MEDIA_FF=19, ALL_MULTIMEDIA_FF=20,
    ALL_HTML_FF=21, TEXT_FF=22, NUM_FILE_FILTERS=23;
    
    private static SimpleFileFilter[] fileFilters=new SimpleFileFilter[NUM_FILE_FILTERS];
    
    public static final String EXT_JCLIC=".jclic", EXT_JCLIC_ZIP=".jclic.zip", EXT_SCORM_ZIP=".scorm.zip",
    EXT_PAC=".pac", EXT_PCC=".pcc", EXT_INSTALL=".jclic.inst";
    public static final String EXT_GIF=".gif", EXT_JPG=".jpg", EXT_PNG=".png", EXT_BMP=".bmp", EXT_ICO=".ico";
    public static final String EXT_WAV=".wav", EXT_AU=".au", EXT_MP3=".mp3", 
    EXT_AIFF=".aiff", EXT_MID=".mid", EXT_OGG=".ogg";
    public static final String EXT_AVI=".avi", EXT_MOV=".mov", EXT_MPEG=".mpeg";
    public static final String EXT_SWF=".swf", EXT_XML=".xml", EXT_TTF=".ttf", 
    EXT_HTM=".htm", EXT_HTML=".html", EXT_TXT=".txt", EXT_ALL=".*";
    
    public static final String[] EXT_ALL_JCLIC=new String[]{EXT_JCLIC, EXT_JCLIC_ZIP, EXT_SCORM_ZIP};
    public static final String[] EXT_ALL_CLIC=new String[]{EXT_PAC, EXT_PCC};
    public static final String[] EXT_ALL_JCLIC_CLIC=new String[]{EXT_JCLIC, EXT_JCLIC_ZIP, EXT_PAC, EXT_PCC};
    public static final String[] EXT_ALL_IMAGES=new String[]{EXT_GIF, EXT_JPG, EXT_PNG, EXT_BMP, EXT_ICO};
    public static final String[] EXT_ALL_SOUNDS=new String[]{EXT_WAV, EXT_AU, EXT_MP3, EXT_OGG, EXT_AIFF};
    public static final String[] EXT_ALL_VIDEO=new String[]{EXT_AVI, EXT_MOV, EXT_MPEG, EXT_SWF};
    public static final String[] EXT_ALL_ANIM=new String[]{EXT_SWF};
    public static final String[] EXT_ALL_MEDIA=new String[]{EXT_GIF, EXT_JPG, EXT_PNG, EXT_BMP, EXT_ICO,
    EXT_WAV, EXT_AU, EXT_MP3, EXT_OGG, EXT_AIFF, EXT_MID, EXT_AVI, EXT_MOV, EXT_MPEG, EXT_SWF, EXT_TTF, EXT_XML};
    public static final String[] EXT_ALL_MULTIMEDIA=new String[]{EXT_WAV, EXT_AU, EXT_MP3, EXT_OGG, EXT_AIFF, EXT_MID,
    EXT_AVI,EXT_MOV,EXT_MPEG, EXT_SWF};
    public static final String[] EXT_ALL_HTML=new String[]{EXT_HTM, EXT_HTML};
    
    public static final int TYPE_IMAGE=0, TYPE_AUDIO=1, TYPE_MIDI=2,
    TYPE_VIDEO=3, TYPE_ANIM=4, TYPE_XML=5, TYPE_FONT=6, TYPE_JCLIC=7, TYPE_CLIC=8,
    TYPE_INST=9, TYPE_TEXT=10, TYPE_UNKNOWN=11;
    
    public static final String[] TYPE_CODES={"ftype_image", "ftype_audio", "ftype_midi",
    "ftype_video", "ftype_anim", "ftype_xml", "ftype_font", "ftype_jclic", "ftype_clic",
    "ftype_inst", "ftype_text", "ftype_unknown"};
    
    public static final int[] T_CODES={
        Constants.T_IMAGE, Constants.T_AUDIO, Constants.T_MIDI,
        Constants.T_VIDEO, Constants.T_ANIM, Constants.T_XML, Constants.T_FONT, Constants.T_JCLIC, Constants.T_CLIC,
        Constants.T_INST, Constants.T_TEXT, Constants.T_UNKNOWN_MEDIA
    };
    
    public static int getFileType(String fileName){
        int result=TYPE_UNKNOWN;
        if(fileName!=null){
            int dot=fileName.lastIndexOf('.');
            if(dot>0){
                if(fileName.substring(0, dot).toLowerCase().endsWith(".jclic"))
                    dot-=6;
                String fn=fileName.substring(dot).toLowerCase();
                if(fn.equals(EXT_GIF) || fn.equals(EXT_JPG) || fn.equals(EXT_PNG) || fn.equals(EXT_BMP) || fn.equals(EXT_ICO))
                    result=TYPE_IMAGE;
                else if(fn.equals(EXT_WAV) || fn.equals(EXT_AU) || fn.equals(EXT_MP3) || fn.equals(EXT_OGG) || fn.equals(EXT_AIFF))
                    result=TYPE_AUDIO;
                else if(fn.equals(EXT_MID))
                    result=TYPE_MIDI;
                else if(fn.equals(EXT_AVI) || fn.equals(EXT_MOV) || fn.equals(EXT_MPEG))
                    result=TYPE_VIDEO;
                else if(fn.equals(EXT_SWF))
                    result=TYPE_ANIM;
                else if(fn.equals(EXT_XML))
                    result=TYPE_XML;
                else if(fn.equals(EXT_TTF))
                    result=TYPE_FONT;
                else if(fn.equals(EXT_JCLIC) || fn.equals(EXT_JCLIC_ZIP))
                    result=TYPE_JCLIC;
                else if(fn.equals(EXT_PAC) || fn.equals(EXT_PCC))
                    result=TYPE_CLIC;
                else if(fn.equals(EXT_INSTALL))
                    result=TYPE_INST;
                else if(fn.equals(EXT_TXT))
                    result=TYPE_TEXT;
            }
        }
        return result;
    }
    
    public static javax.swing.ImageIcon getFileIcon(String forFileName){
        String imgKey="icons/icounknown.png";
        switch(getFileType(forFileName)){
            case Utils.TYPE_AUDIO:
                imgKey="icons/icowave.png";
                break;
            case Utils.TYPE_MIDI:
                imgKey="icons/icomidi.png";
                break;
            case Utils.TYPE_ANIM:
            case Utils.TYPE_VIDEO:
                imgKey="icons/icomci.png";
                break;
            case Utils.TYPE_FONT:
                imgKey="icons/icottf.png";
                break;
            case Utils.TYPE_XML:
                imgKey="icons/icoxml.png";
                break;
        }
        return edu.xtec.util.ResourceManager.getImageIcon(imgKey);
    }
    
    public static int getFileFilterCode(String forFileName){
        int result=ALL_FF;
        switch(getFileType(forFileName)){
            case TYPE_IMAGE:
                result=ALL_IMAGES_FF;
                break;
            case TYPE_AUDIO:
                result=ALL_SOUNDS_FF;
                break;
            case TYPE_MIDI:
                result=MIDI_FF;
                break;
            case TYPE_VIDEO:
                result=ALL_VIDEO_FF;
                break;
            case TYPE_ANIM:
                result=ALL_ANIM_FF;
                break;
            case TYPE_XML:
                result=SKINS_FF;
                break;
            case TYPE_FONT:
                result=FONTS_FF;
                break;
            case TYPE_JCLIC:
                result=ALL_JCLIC_FF;
                break;
            case TYPE_CLIC:
                result=ALL_CLIC_FF;
                break;
            case TYPE_INST:
                result=INSTALL_FF;
                break;
            case TYPE_TEXT:
                result=TEXT_FF;
                break;                
        }
        return result;
            
    }    
    
    public static SimpleFileFilter getFileFilter(int fileFilterCode, edu.xtec.util.Messages msg){
        if(fileFilterCode<0 || fileFilterCode>=NUM_FILE_FILTERS)
            return null;
        if(fileFilters[fileFilterCode]==null){
            switch(fileFilterCode){
                case JCLIC_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_JCLIC, msg.get("filefilter_jclic"));
                    break;
                case JCLIC_ZIP_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_JCLIC_ZIP, msg.get("filefilter_jclic_zip"));
                    break;
                case PAC_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_PAC, msg.get("filefilter_pac"));
                    break;
                case PCC_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_PCC, msg.get("filefilter_pcc"));
                    break;
                case ALL_JCLIC_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_JCLIC, msg.get("filefilter_all_jclic"));
                    break;
                case ALL_CLIC_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_CLIC, msg.get("filefilter_all_clic"));
                    break;
                case ALL_JCLIC_CLIC_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_JCLIC_CLIC, msg.get("filefilter_all_jclic_clic"));
                    break;
                case INSTALL_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_INSTALL, msg.get("filefilter_install"));
                    break;
                case GIF_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_GIF, msg.get("filefilter_gif"));
                    break;
                case JPG_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_JPG, msg.get("filefilter_jpg"));
                    break;
                case PNG_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_PNG, msg.get("filefilter_png"));
                    break;
                case ALL_IMAGES_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_IMAGES, msg.get("filefilter_all_images"));
                    break;
                case ALL_SOUNDS_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_SOUNDS, msg.get("filefilter_all_sounds"));
                    break;
                case MIDI_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_MID, msg.get("filefilter_midi"));
                    break;
                case ALL_VIDEO_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_VIDEO, msg.get("filefilter_all_video"));
                    break;
                case ALL_ANIM_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_ANIM, msg.get("filefilter_all_anim"));
                    break;
                case SKINS_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_XML, msg.get("filefilter_skins"));
                    break;
                case FONTS_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_TTF, msg.get("filefilter_fonts"));
                    break;
                case ALL_MEDIA_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_MEDIA, msg.get("filefilter_all_media"));
                    break;                    
                case ALL_MULTIMEDIA_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_MULTIMEDIA, msg.get("filefilter_all_media"));
                    break;
                case ALL_HTML_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL_HTML, msg.get("filefilter_all_html"));
                    break;
                case TEXT_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_TXT, msg.get("filefilter_text"));
                    break;                    
                    
                case ALL_FF:
                    fileFilters[fileFilterCode]=new SimpleFileFilter(EXT_ALL, msg.get("filefilter_all"));
                    break;
            }
        }
        return fileFilters[fileFilterCode];
        
    }
    
    public static String[] getFileFilterExtensions(int fileFilterCode){
        String[] result=null;
        if(fileFilterCode>=0 && fileFilterCode<NUM_FILE_FILTERS){
            switch(fileFilterCode){
                case JCLIC_FF:
                    result=new String[]{EXT_JCLIC};
                    break;
                case JCLIC_ZIP_FF:
                    result=new String[]{EXT_JCLIC_ZIP};
                    break;
                case PAC_FF:
                    result=new String[]{EXT_PAC};
                    break;
                case PCC_FF:
                    result=new String[]{EXT_PCC};
                    break;
                case ALL_JCLIC_FF:
                    result=EXT_ALL_JCLIC;
                    break;
                case ALL_CLIC_FF:
                    result=EXT_ALL_CLIC;
                    break;
                case ALL_JCLIC_CLIC_FF:
                    result=EXT_ALL_JCLIC_CLIC;
                    break;
                case INSTALL_FF:
                    result=new String[]{EXT_INSTALL};
                    break;
                case GIF_FF:
                    result=new String[]{EXT_GIF};
                    break;
                case JPG_FF:
                    result=new String[]{EXT_JPG};
                    break;
                case PNG_FF:
                    result=new String[]{EXT_PNG};
                    break;
                case ALL_IMAGES_FF:
                    result=EXT_ALL_IMAGES;
                    break;
                case ALL_SOUNDS_FF:
                    result=EXT_ALL_SOUNDS;
                    break;
                case MIDI_FF:
                    result=new String[]{EXT_MID};
                    break;
                case ALL_VIDEO_FF:
                    result=EXT_ALL_VIDEO;
                    break;
                case ALL_ANIM_FF:
                    result=EXT_ALL_ANIM;
                    break;
                case SKINS_FF:
                    result=new String[]{EXT_XML};
                    break;
                case FONTS_FF:
                    result=new String[]{EXT_TTF};
                    break;                    
                case ALL_MEDIA_FF:
                    result=EXT_ALL_MEDIA;
                    break;                    
                case ALL_MULTIMEDIA_FF:
                    result=EXT_ALL_MULTIMEDIA;
                    break;
                case TEXT_FF:
                    result=new String[]{EXT_TXT};
                    break;                                        
            }
        }
        return result;
        
    }
    
    public static BufferedImage toBufferedImage(Image image, java.awt.Color bgColor, ImageObserver io) {
        int w=image.getWidth(io);
        int h=image.getHeight(io);
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, bgColor, io);
        g.dispose();
        return bufferedImage;
    }
    
    public static void drawImage(Graphics g, Image img, Rectangle dest, Rectangle source, ImageObserver io){
        if(g.getClip().intersects(dest)){
            g.drawImage(img, dest.x, dest.y, dest.x+dest.width, dest.y+dest.height, source.x, source.y, source.x+source.width, source.y+source.height, io);
        }
    }
    
    public static void tileImage(Graphics g, Image img, Rectangle dest, Rectangle source, ImageObserver io){
        if(g.getClip().intersects(dest)){
            int x, y;
            Area saveClip=new Area(g.getClip());
            Area newClip=new Area(saveClip);
            newClip.intersect(new Area(dest));
            g.setClip(newClip);
            Rectangle floatDest=new Rectangle(dest.x, dest.y, source.width, source.height);
            for(y=0; y<dest.height; y+=source.height){
                for(x=0; x<dest.width; x+=source.width){
                    floatDest.setLocation(dest.x+x, dest.y+y);
                    drawImage(g, img, floatDest, source, io);
                }
            }
            g.setClip(saveClip);
        }
    }
    
    public static Point mapPointTo(Component srcCmp, Point offset, Component destCmp){
        Point p=new Point();
        try{
            p=new Point(srcCmp.getLocationOnScreen());
        } catch(IllegalComponentStateException ex){
            // component is not visible!
        }
        p.x+=offset.x; p.y+=offset.y;
        Point pd=new Point();
        try{
            pd=destCmp.getLocationOnScreen();
        } catch(IllegalComponentStateException ex){
            // component is not visible!
        }
        p.x-=pd.x; p.y-=pd.y;
        return p;
    }
    
    
    public static void refreshAnimatedImage(Image img){
        if(img!=null && (java.awt.Toolkit.getDefaultToolkit().checkImage(img, -1, -1, null) & ImageObserver.ALLBITS)!=0){
            img.flush();
        }
    }
    
    public static void checkRenderingHints(Options options){
        if(DEFAULT_RENDERING_HINTS.isEmpty()){
            if(options==null)
                options=new Options();
            boolean mac=options.getBoolean(Options.MAC);
            boolean j14=options.getBoolean(Options.JAVA14);
            boolean win=options.getBoolean(Options.WIN);
            
            //DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
            
            if(win)
                DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            
            //DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
            
            //DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
            
            if(true)
                DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            if(win)
                DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            
            //DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            
            if(win)
                DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            
            
            if(win)
                DEFAULT_RENDERING_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
    }

    public static long getTotalFreeMemory(){
        Runtime r=Runtime.getRuntime();
        return r.maxMemory()-r.totalMemory()+r.freeMemory();
    }

    // Low memory under 4Mb of free RAM
    public static final long LOW_MEMORY_THRESHOLD=0x400000L;

    public static boolean lowMemoryCondition(){
        return getTotalFreeMemory()<LOW_MEMORY_THRESHOLD;
    }
    
}
