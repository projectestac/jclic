/*
 * File    : ActiveMediaBag.java
 * Created : 02-may-2001 11:28
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
import java.util.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ActiveMediaBag extends HashSet<ActiveMediaPlayer> {

    /** Creates new ActiveMediaBag */
    public ActiveMediaBag() {
    }
    
    public ActiveMediaPlayer createActiveMediaPlayer(MediaContent mc, MediaBag mb, PlayStation ps){
        ActiveMediaPlayer amp=null;
        switch(mc.mediaType){
            case MediaContent.RECORD_AUDIO:
                if(mc.length<=0 || mc.length>=AudioBuffer.MAX_RECORD_LENGTH)
                    break;
            case MediaContent.PLAY_RECORDED_AUDIO:
                if(mc.recBuffer<0 || mc.recBuffer>=ActiveMediaPlayer.AUDIO_BUFFERS)
                    break;
            case MediaContent.PLAY_AUDIO:
            case MediaContent.PLAY_MIDI:
            case MediaContent.PLAY_VIDEO:
                amp=ActiveMediaPlayer.createActiveMediaPlayer(mc, mb, ps);
                break;
        }
        if(amp!=null)
            add(amp);
        return amp;
    }
    
    public ActiveMediaPlayer getActiveMediaPlayer(MediaContent mc, MediaBag mb, PlayStation ps){
        ActiveMediaPlayer amp=null;
        for (Iterator i = iterator(); i.hasNext(); ){
            amp=(ActiveMediaPlayer)i.next();
            if(amp.getMediaContent()==mc || amp.getMediaContent().isEquivalent(mc))
                break;
            amp=null;
        }
        if(amp==null)
            amp=createActiveMediaPlayer(mc, mb, ps);
        return amp;
    }
    
    public void removeActiveMediaPlayer(MediaContent mc){
        ActiveMediaPlayer amp=null;
        for (Iterator i = iterator(); i.hasNext(); ){
            amp=(ActiveMediaPlayer)i.next();
            if(amp.getMediaContent()==mc) break;
            amp=null;
        }
        if(amp!=null){
            amp.clear();
            remove(amp);
        }
    }
        
    public void realizeAll(){
        for (Iterator<ActiveMediaPlayer> i = iterator(); i.hasNext(); ){
            ActiveMediaPlayer amp=i.next();
            amp.realize();
        }
    }
    
    public void stopAll(){
        stopAll(-1);
    }
    
    public void stopAll(int level){
        for (Iterator i = iterator(); i.hasNext(); ){
            ActiveMediaPlayer amp=(ActiveMediaPlayer)i.next();
            if(level==-1 || amp.getMediaContent().level<=level) amp.stop();
        }
    }
    
    public void removeAll(){
        for (Iterator i = iterator(); i.hasNext(); ){
            ActiveMediaPlayer amp=(ActiveMediaPlayer)i.next();
            amp.clear();
        }
        clear();
        ActiveMediaPlayer.clearAllAudioBuffers();
    }    
}
