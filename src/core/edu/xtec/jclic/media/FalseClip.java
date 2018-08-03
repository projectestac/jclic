/*
 * File    : FalseClip.java
 * Created : 04-aug-2004 16:26
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2018 Francesc Busquets & Departament
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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class FalseClip implements PseudoClip {

  private AudioInputStream ais;
  private AudioFormat af;
  private MediaBag mb;
  private String mediaFileName;
  private PlayThread playThread;
  private int loopCount;

  /** Creates a new instance of FalseClip */
  private FalseClip(MediaBag mb, String mediaFileName) throws Exception {
    this.mb = mb;
    this.mediaFileName = mediaFileName;
    af = getAudioInputStream().getFormat();
  }

  public static FalseClip getFalseClip(MediaBag mb, String mediaFileName) throws Exception {
    return new FalseClip(mb, mediaFileName);
  }

  private AudioInputStream getAudioInputStream() throws Exception {
    if (ais == null) {
      JavaSoundAudioPlayer jsap = new JavaSoundAudioPlayer();
      jsap.setDataSource(mb.getMediaDataSource(mediaFileName));
      ais = jsap.ais;
      if (ais == null)
        throw new Exception("Unable to open audio data!");
    }
    return ais;
  }

  public void close() {
    stop();
  }

  public javax.sound.sampled.AudioFormat getFormat() {
    return af;
  }

  public boolean isActive() {
    return (playThread != null && playThread.isAlive());
  }

  public boolean isOpen() {
    return ais != null;
  }

  public boolean isRunning() {
    return isActive();
  }

  public void loop(int count) throws Exception {
    start();
    loopCount = count;
  }

  public void open() throws Exception {
    getAudioInputStream();
  }

  public void setFramePosition(int frames) {
    // ignore
  }

  public void setLoopPoints(int start, int end) {
    // ignore
  }

  public void start() throws Exception {
    if (playThread == null) {
      loopCount = 0;
      playThread = new PlayThread();
      playThread.start();
    }
  }

  public void stop() {
    if (playThread != null) {
      if (playThread.isAlive()) {
        playThread.running = false;
        while (playThread != null) {
          Thread.yield();
        }
      } else {
        playThread = null;
        ais = null;
      }
    }
  }

  class PlayThread extends Thread {

    public boolean running;
    byte[] buf = new byte[JavaSoundAudioPlayer.INTERNAL_BUFFER_SIZE];
    SourceDataLine line = null;

    public PlayThread() throws Exception {
      running = false;
      getAudioInputStream();
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
      line = (SourceDataLine) AudioSystem.getLine(info);
      if (line != null)
        line.open(af);
    }

    @Override
    public void run() {
      if (line != null) {
        running = true;
        int nBytesRead = 0;
        try {
          line.start();
          while (running && nBytesRead >= 0) {
            nBytesRead = ais.read(buf, 0, buf.length);
            if (nBytesRead >= 0) {
              int nBytesWritten = line.write(buf, 0, nBytesRead);
            }
            Thread.yield();
          }
          if (running)
            line.drain();
          else
            line.stop();
        } catch (Exception ex) {
          System.err.println("JavaSound playing error:\n" + ex);
        }
        line.close();
      }
      ais = null;
      playThread = null;
      if (running && loopCount != 0) {
        if (loopCount > 0)
          loopCount--;
        try {
          int lc = loopCount;
          FalseClip.this.start();
          loopCount = lc;
        } catch (Exception ex) {
          System.err.println("Error looping sound: " + ex);
        }
      }
      running = false;
    }
  }
}
