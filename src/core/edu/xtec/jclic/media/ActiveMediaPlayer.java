/*
 * File    : ActiveMediaPlayer.java
 * Created : 27-may-2002 18:43
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
import edu.xtec.jclic.boxes.ActiveBox;
import edu.xtec.util.GlobalMouseAdapter;
import edu.xtec.util.Options;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public abstract class ActiveMediaPlayer {
    
    public static final int AUDIO_BUFFERS=10;
    protected static final AudioBuffer[] audioBuffer=new AudioBuffer[AUDIO_BUFFERS];
    
    MediaContent mc;
    PlayStation ps;
    ActiveBox bx;
    Component visualComponent;
    GlobalMouseAdapter mouseAdapter;
    boolean useAudioBuffer;
    
    public static ActiveMediaPlayer createActiveMediaPlayer(MediaContent mc, MediaBag mb, PlayStation ps){
        ActiveMediaPlayer result=null;
        String playerClassName=null;
        String ms=ps.getOptions().getString(Constants.MEDIA_SYSTEM);
        
        switch(mc.mediaType){
            case MediaContent.RECORD_AUDIO:
            case MediaContent.PLAY_RECORDED_AUDIO:
                if(ps.getOptions().getBoolean(Options.MAC)){
                    if(Constants.QT61.equals(ms)){
                        playerClassName="edu.xtec.jclic.media.QT61ActiveMediaPlayer";
                        break;
                    }
					// 27-Nov-2007: QuickTime 6.0 is no longer supported in JClic					
                    //else if(Constants.QT.equals(ms)){
                    //    playerClassName="edu.xtec.jclic.media.QTActiveMediaPlayer";
                    //    break;
                    //}
                }
            case MediaContent.PLAY_AUDIO:
            case MediaContent.PLAY_MIDI:
                playerClassName="edu.xtec.jclic.media.JavaSoundActiveMediaPlayer";
                break;
            default:
                if(Constants.QT61.equals(ms))
                    playerClassName="edu.xtec.jclic.media.QT61ActiveMediaPlayer";
                //else if(Constants.QT.equals(ms))
                //    playerClassName="edu.xtec.jclic.media.QTActiveMediaPlayer";
                else if(Constants.JMF.equals(ms))
                    playerClassName="edu.xtec.jclic.media.JMFActiveMediaPlayer";
                else
                    CheckMediaSystem.warn(ps.getOptions());
                break;
        }
        
        if(playerClassName!=null){
            try{
                Class<?> c=Class.forName(playerClassName);
                java.lang.reflect.Constructor<?> cons=c.getConstructor(new Class<?>[]{edu.xtec.jclic.media.MediaContent.class, edu.xtec.jclic.bags.MediaBag.class, edu.xtec.jclic.PlayStation.class});
                result=(ActiveMediaPlayer)cons.newInstance(new Object[]{mc, mb, ps});
            } catch(Exception ex){
                System.err.println("Error building media player:\n"+ex);
            }
        }
        return result;
    }
    
    /** Creates new MediaContentPlayer */
    protected ActiveMediaPlayer(MediaContent mc, MediaBag mb, PlayStation ps) {
        this.mc=mc;
        this.ps=ps;
        bx=null;
        visualComponent=null;
        mouseAdapter=null;
        useAudioBuffer=false;
        try{
            switch(mc.mediaType){
                case MediaContent.RECORD_AUDIO:
                    clearAudioBuffer(mc.recBuffer);
                    audioBuffer[mc.recBuffer]=createAudioBuffer(mc.length);
                case MediaContent.PLAY_RECORDED_AUDIO:
                    useAudioBuffer=true;
                    break;
                default:
                    break;
            }
        } catch(Exception ex){
            System.err.println("Error:\n"+ex);
        }
    }
    
    public abstract AudioBuffer createAudioBuffer(int seconds) throws Exception;
    
    public abstract void realize();
    
    public void play(final edu.xtec.jclic.boxes.ActiveBox setBx){
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                stopAllAudioBuffers();
                playNow(setBx);
            }
        });
    }
    
    protected void playNow(edu.xtec.jclic.boxes.ActiveBox setBx){
        try{
            switch(mc.mediaType){
                case MediaContent.RECORD_AUDIO:
                    if(audioBuffer[mc.recBuffer]!=null){
                        audioBuffer[mc.recBuffer].record(ps, setBx);
                    }
                    break;
                case MediaContent.PLAY_RECORDED_AUDIO:
                    if(audioBuffer[mc.recBuffer]!=null)
                        audioBuffer[mc.recBuffer].play();
                    break;
                default:
                    break;
            }
        } catch(Exception e){
            System.err.println("Error playing media \""+mc.mediaFileName+"\":\n"+e);
        }
    }
    
    public void stop(){
        if(useAudioBuffer)
            stopAudioBuffer(mc.recBuffer);
    }
    
    @Override
    protected void finalize() throws Throwable{
        clear();
        super.finalize();
    }
    
    public void clear(){
        stop();
        if(useAudioBuffer)
            clearAudioBuffer(mc.recBuffer);
    }
    
    protected abstract void setTimeRanges();
    
    public static void clearAudioBuffer(int buffer){
        if(buffer>=0 && buffer<AUDIO_BUFFERS && audioBuffer[buffer]!=null){
            audioBuffer[buffer].clear();
            audioBuffer[buffer]=null;
        }
    }
    
    public static void clearAllAudioBuffers(){
        for(int i=0; i<AUDIO_BUFFERS; i++)
            clearAudioBuffer(i);
    }
    
    public static int countActiveBuffers(){
        int c=0;
        for(AudioBuffer ab : audioBuffer)
            if(ab!=null)
                c++;
        return c;
    }
    
    public static void stopAllAudioBuffers(){
        for(AudioBuffer ab : audioBuffer)
            if(ab!=null)
                ab.stop();
    }
    
    public static void stopAudioBuffer(int buffer){
        if(buffer>=0 && buffer<AUDIO_BUFFERS && audioBuffer[buffer]!=null)
            audioBuffer[buffer].stop();
    }
    
    public void checkVisualComponentBounds(ActiveBox bxi){
        if(visualComponent==null)
            return;
        
        Rectangle enclosingRect=new Rectangle();
        if(!mc.free)
            enclosingRect.setBounds(bxi.getBounds());
        else
            enclosingRect.setBounds(ps.getComponent().getBounds());
        
        Point offset=new Point();
        Dimension dim=new Dimension(visualComponent.getPreferredSize());
        if(mc.absLocation!=null){
            offset.setLocation(mc.absLocation);
            if(offset.x+dim.width>enclosingRect.width)
                offset.x=enclosingRect.width-dim.width;
            if(offset.y+dim.height>enclosingRect.height)
                offset.y=enclosingRect.height-dim.height;
        }
        if(mc.stretch){
            int extraW=enclosingRect.width-offset.x-dim.width;
            if(extraW<0){
                dim.width=enclosingRect.width-offset.x;
                extraW=0;
            }
            int extraH=enclosingRect.height-offset.y-dim.height;
            if(extraH<0){
                dim.height=enclosingRect.height-offset.y;
                extraH=0;
            }
            if(mc.absLocation==null){
                offset.x+=extraW/2;
                offset.y+=extraH/2;
            }
        }
        Rectangle vRect=new Rectangle(enclosingRect.x+offset.x, enclosingRect.y+offset.y, dim.width, dim.height);
        visualComponent.setSize(dim);
        visualComponent.setLocation(vRect.getLocation());
        visualComponent.setBounds(vRect);
    }
    
    public void setVisualComponentVisible(boolean state){
        if(visualComponent!=null)
            visualComponent.setVisible(state);
    }
    
    protected abstract java.awt.Component getVisualComponent();
    
    public void attachVisualComponent(){
        if(mc.mediaType!=MediaContent.PLAY_VIDEO || bx==null)
            return;
        
        visualComponent=getVisualComponent();
        if(visualComponent==null)
            return;
        visualComponent.setVisible(false);
        
        Container cnt=bx.getContainerResolve();
        if(mc.free)
            cnt=ps.getComponent();
        
        if(cnt!=visualComponent.getParent()){
            cnt.add(visualComponent);
        }
        
        if(mouseAdapter==null && mc.catchMouseEvents==false){
            mouseAdapter=new GlobalMouseAdapter(cnt);
            mouseAdapter.attachTo(visualComponent, true);
        }
        
        checkVisualComponentBounds(bx);
        //bx.addActiveBoxListener(this);
        visualComponent.setVisible(true);
    }
    
    protected void destroyVisualComponent(){
        if(mouseAdapter!=null){
            mouseAdapter.detach(visualComponent);
            mouseAdapter=null;
        }
        if(visualComponent!=null){
            visualComponent.setVisible(false);
            stop();
            visualComponent=null;
        }
    }
    
    public void linkTo(ActiveBox setBx){
        if(bx!=null && bx!=setBx){
            bx.setHostedMediaPlayer(null);
            destroyVisualComponent();
        }
        bx=setBx;
        if(bx!=null)
            bx.setHostedMediaPlayer(this);
        else{
            destroyVisualComponent();
        }
    }
    
    public MediaContent getMediaContent() {
        return mc;
    }
}
