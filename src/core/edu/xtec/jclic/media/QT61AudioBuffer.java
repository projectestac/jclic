/*
 * File    : QT61AudioBuffer.java
 * Created : 19-sep-2003 11:01
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

import java.io.File;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class QT61AudioBuffer extends AudioBuffer {

  protected File file;
  protected quicktime.std.sg.SequenceGrabber mGrabber;
  protected quicktime.std.sg.SGSoundChannel mAudio;
  protected quicktime.app.view.MoviePlayer player;
  protected Thread recordThread;
  protected boolean initialized;

  /** Creates new QTAudioBuffer */
  public QT61AudioBuffer(int seconds) throws Exception {
    super(seconds);
    initialized = false;
  }

  public void play() throws Exception {
    stop();
    if (initialized) {
      if (player == null && file != null) {
        player = QT61Tools.getPlayer(file);
      }
      if (player != null) {
        player.setTime(0);
        quicktime.app.time.TaskAllMovies.addMovieAndStart();
        player.getMovie().setActive(true);
        player.setRate(1.0F);
      }
    }
  }

  protected void clear() {
    stop();
    if (file != null) {
      try {
        file.delete();
      } catch (Exception ex) {
        System.err.println("ERROR: Unable to delete file " + file.getPath() + "\n" + ex);
      }
      file = null;
    }
  }

  public void stop() {
    try {
      if (player != null) {
        player.setRate(0);
        player.getMovie().setActive(false);
        quicktime.app.time.TaskAllMovies.removeMovie();
      }
      for (int i = 0; i < 10; i++) {
        if (mGrabber != null && mGrabber.isRecordMode()) {
          mGrabber.stop();
          Thread.sleep(50);
        } else
          break;
      }
    } catch (Exception ex) {
      System.err.println("QuickTime recording error at STOP:\n" + ex);
    }
  }

  protected void record() throws Exception {
    stop();
    if (player != null) {
      try {
        player.getMovie().disposeQTObject();
      } catch (Exception ex) {
        System.err.println("QuickTime error: unable to release player\n" + ex);
      }
      player = null;
      initialized = false;
    }
    if (file != null) {
      file.delete();
      file = null;
    }
    file = File.createTempFile("rec", ".tmp");
    file.deleteOnExit();

    mGrabber = new quicktime.std.sg.SequenceGrabber();
    mAudio = new quicktime.std.sg.SGSoundChannel(mGrabber);
    mAudio.setUsage(quicktime.std.StdQTConstants.seqGrabRecord);
    mAudio.setSoundInputRate(22050);
    mAudio.setSoundInputParameters(16, 1, 0);

    mGrabber.setDataOutput(new quicktime.io.QTFile(file), quicktime.std.StdQTConstants.seqGrabToDisk);
    mGrabber.setMaximumRecordTime(m_seconds * 60);
    mGrabber.prepare(false, true);
    activeAudioBuffer = this;
    recordThread = new Thread("JClic sound record") {
      @Override
      public void run() {
        try {
          while (mGrabber.isRecordMode() && mGrabber.idleMore()) {
            mGrabber.idle();
            Thread.sleep(20);
          }
          if (mGrabber.isRecordMode())
            mGrabber.stop();
          recordThread = null;
          if (mAudio != null) {
            mGrabber.disposeChannel(mAudio);
            mAudio = null;
          }
          mGrabber.release();
          mGrabber = null;
          initialized = true;
          activeAudioBuffer = null;
          hideRecordingCursor();
        } catch (Exception ex) {
          System.err.println(ex.getMessage());
        }
      }
    };

    mGrabber.startRecord();
    recordThread.start();
  }
}
