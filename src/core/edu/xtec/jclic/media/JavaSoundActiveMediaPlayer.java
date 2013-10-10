/*
 * File    : JavaSoundActiveMediaPlayer.java
 * Created : 16-jun-2004 15:59
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

import edu.xtec.util.ExtendedByteArrayInputStream;
import edu.xtec.util.StreamIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.*;
import javax.swing.Timer;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 */
public class JavaSoundActiveMediaPlayer extends ActiveMediaPlayer {
    
    PseudoClip clip;
    Timer timer;
    
    boolean midi;
    ByteArrayInputStream midiIs;
    public static javax.sound.midi.Sequencer sequencer=null;
    
    /** Creates a new instance of JavaSoundActiveMediaPlayer */
    public JavaSoundActiveMediaPlayer(edu.xtec.jclic.media.MediaContent mc, edu.xtec.jclic.bags.MediaBag mb, edu.xtec.jclic.PlayStation ps) {
        super(mc, mb, ps);
        InputStream is;
        midi=false;
        midiIs=null;
        if(!useAudioBuffer){
            try{
                switch(mc.mediaType){
                    case MediaContent.PLAY_MIDI:
                        midi=true;
                        is=mb.getInputStream(mc.mediaFileName);
                        if(is instanceof ByteArrayInputStream)
                            midiIs=(ByteArrayInputStream)is;
                        else
                            midiIs=new ExtendedByteArrayInputStream(StreamIO.readInputStream(is), mc.mediaFileName);
                        break;
                        
                    case MediaContent.PLAY_AUDIO:
                        if(mc.to>0 || mc.from>0)
                            clip=ClipWrapper.getClipWrapper(mb, mc.mediaFileName);
                        else
                            clip=FalseClip.getFalseClip(mb, mc.mediaFileName);
                        
                        if(clip !=null && !mc.loop && mc.to>Math.max(0, mc.from)){
                            timer=new Timer(mc.to-Math.max(0, mc.from), new ActionListener(){
                                public void actionPerformed(ActionEvent ev){
                                    if(clip!=null && clip.isRunning())
                                        clip.stop();                                            
                                }
                            });
                            timer.setCoalesce(false);
                            timer.setRepeats(false);
                        }                                                                        
                        break;
                        
                    default:
                        break;
                }
            } catch(Exception ex){
                System.err.println("Error reading media \""+mc.mediaFileName+"\":\n"+ex);
            }
        }
    }
    
    public AudioBuffer createAudioBuffer(int seconds) throws Exception{
        return new JavaSoundAudioBuffer(mc.length);
    }
    
    public static void closeMidiSequencer(){
        if(sequencer!=null){
            if(sequencer.isRunning())
                sequencer.stop();
            if(sequencer.isOpen())
                sequencer.close();
        }
    }
    
    public void realize(){
        if(!useAudioBuffer){
            try{
                if(midi){
                    if(sequencer==null)
                        sequencer=MidiSystem.getSequencer();
                }
                else{
                    if(clip!=null && !clip.isOpen()){
                        clip.open();
                    }
                }
            } catch(Exception e){
                System.err.println("Error realizing media \""+mc.mediaFileName+"\"\n"+e);
                //e.printStackTrace();
            }
        }
    }
    
    @Override
    protected void playNow(edu.xtec.jclic.boxes.ActiveBox setBx){
        if(useAudioBuffer)
            super.playNow(setBx);
        else{
            try{
                if(midi){
                    if(sequencer==null) realize();
                    if(sequencer!=null && midiIs!=null){
                        if(sequencer.isRunning())
                            sequencer.stop();
                        if(!sequencer.isOpen())
                            sequencer.open();
                        midiIs.reset();
                        sequencer.setSequence(midiIs);
                        sequencer.stop();
                        setTimeRanges();
                        sequencer.start();
                    }
                } else if(clip!=null){
                    if(!clip.isOpen())
                        realize();
                    setTimeRanges();
                    attachVisualComponent();
                    if(mc.loop){
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    }
                    else{
                        clip.start();
                        if(timer!=null)
                            timer.start();                    
                    }
                }
            } catch(Exception e){
                System.err.println("Error playing media \""+mc.mediaFileName+"\":\n"+e);
            }
        }
    }
    
    @Override
    public void stop() {
        super.stop();
        if(!useAudioBuffer){
            try{
                if(midi && sequencer!=null){
                    closeMidiSequencer();
                }
                else if (clip!=null && clip.isActive()){
                    if(timer!=null && timer.isRunning())
                        timer.stop();
                    clip.stop();
                }
            } catch(Exception e){
                System.err.println("Error stopping media \""+mc.mediaFileName+"\":\n"+e);
            }
        }
    }
    
    @Override
    public void clear(){
        super.clear();
        if(!useAudioBuffer){
            try{
                if(midi){
                    if(sequencer!=null){
                        sequencer.close();
                        sequencer=null;
                    }
                    if(midiIs!=null){
                        midiIs.close();
                        midiIs=null;
                    }
                }
                else if(clip!=null){
                    stop();
                    clip.close();
                    destroyVisualComponent();
                    clip=null;
                    timer=null;
                }
            } catch(Exception e){
                System.err.println("Error closing media \""+mc.mediaFileName+"\":\n"+e);
            }
        }
    }
    
    protected void setTimeRanges(){
        if(useAudioBuffer) return;
        try{
            if(midi && sequencer!=null){
                sequencer.setTickPosition(0L);
                /*
                if(mc.from!=-1){
                    //sequencer.setTickPosition(sequencer.getSequence().getResolution()*mc.from/2);
                } else
                    sequencer.setTickPosition(0L);
                //if(mc.to!=-1){
                //    sequencer.
                //}
                 */
            }
            else if(clip!=null && clip.isOpen()){
                if(mc.from>=0 || mc.to>=0){
                    int from=mc.from>0 ? (int)((clip.getFormat().getFrameRate()*mc.from)/1000) : 0;
                    int to=mc.to>=0 ? (int)((clip.getFormat().getFrameRate()*mc.to)/1000) : -1;
                    if(mc.loop)
                        clip.setLoopPoints(from, to);
                    clip.setFramePosition(from);
                } else{
                    clip.setFramePosition(0);
                }
            }
        } catch(Exception e){
            System.err.println("Error setting time ranges for \""+mc.mediaFileName+"\":\n"+e);
        }
    }
    
    protected java.awt.Component getVisualComponent(){
        return null;
    }
    
}
