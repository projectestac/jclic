/*
 * File    : ClipWrapper.java
 * Created : 04-aug-2004 16:20
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

import edu.xtec.jclic.bags.MediaBag;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

/** @author Francesc Busquets (fbusquets@xtec.cat) */
public class ClipWrapper implements PseudoClip {

  private Clip clip;
  private AudioInputStream ais;

  /** Creates a new instance of ClipWrapper */
  private ClipWrapper(AudioInputStream ais, Clip clip) {
    this.clip = clip;
    this.ais = ais;
  }

  public static ClipWrapper getClipWrapper(MediaBag mb, String mediaFileName) throws Exception {
    ClipWrapper result = null;
    JavaSoundAudioPlayer jsap = new JavaSoundAudioPlayer();
    jsap.setDataSource(mb.getMediaDataSource(mediaFileName));
    AudioInputStream ais = jsap.ais;
    Clip clip = jsap.getClip();
    return (ais == null || clip == null) ? null : new ClipWrapper(ais, clip);
  }

  public void close() {
    clip.close();
  }

  public javax.sound.sampled.AudioFormat getFormat() {
    return clip.getFormat();
  }

  public boolean isActive() {
    return clip.isActive();
  }

  public boolean isOpen() {
    return clip.isOpen();
  }

  public boolean isRunning() {
    return clip.isRunning();
  }

  public void loop(int count) throws Exception {
    clip.loop(count);
  }

  public void open() throws Exception {
    clip.open(ais);
  }

  public void setFramePosition(int frames) {
    clip.setFramePosition(frames);
  }

  public void setLoopPoints(int start, int end) {
    clip.setLoopPoints(start, end);
  }

  public void start() throws Exception {
    clip.start();
  }

  public void stop() {
    clip.stop();
  }
}
