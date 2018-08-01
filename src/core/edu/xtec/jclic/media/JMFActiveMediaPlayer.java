/*
 * File    : JMFActiveMediaPlayer.java
 * Created : 02-may-2001 10:56
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

import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.jclic.boxes.ActiveBox;
import edu.xtec.util.ExtendedByteArrayInputStream;
import edu.xtec.util.StreamIO;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.media.Player;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class JMFActiveMediaPlayer extends ActiveMediaPlayer implements javax.media.ControllerListener {

  public static Sequencer sequencer = null;

  javax.media.Player player;
  javax.media.protocol.DataSource dataSource;
  boolean midi;
  ByteArrayInputStream midiIs;

  /** Creates new JMFActiveMediaPlayer */
  public JMFActiveMediaPlayer(edu.xtec.jclic.media.MediaContent mc, MediaBag mb, PlayStation ps) {
    super(mc, mb, ps);
    player = null;
    dataSource = null;
    midi = false;
    midiIs = null;
    if (!useAudioBuffer) {
      try {
        switch (mc.mediaType) {
        case MediaContent.PLAY_MIDI:
          midi = true;
          InputStream is = mb.getInputStream(mc.mediaFileName);
          if (is instanceof ByteArrayInputStream)
            midiIs = (ByteArrayInputStream) is;
          else
            midiIs = new ExtendedByteArrayInputStream(StreamIO.readInputStream(is), mc.mediaFileName);
          break;

        default:
          Object source = mb.getMediaDataSource(mc.mediaFileName);
          if (source instanceof ExtendedByteArrayInputStream) {
            dataSource = new ByteDataSource((ExtendedByteArrayInputStream) source);
          } else if (source instanceof String) {
            javax.media.MediaLocator ml = new javax.media.MediaLocator((String) source);
            dataSource = javax.media.Manager.createDataSource(ml);
          }
        }
      } catch (Exception ex) {
        System.err.println("Error reading media \"" + mc.mediaFileName + "\":\n" + ex);
      }
    }
  }

  public AudioBuffer createAudioBuffer(int seconds) throws Exception {
    return new JavaSoundAudioBuffer(mc.length);
  }

  public static void closeMidiSequencer() {
    if (sequencer != null) {
      if (sequencer.isRunning())
        sequencer.stop();
      if (sequencer.isOpen())
        sequencer.close();
    }
  }

  public void realize() {
    if (!useAudioBuffer) {
      try {
        if (midi) {
          if (sequencer == null)
            sequencer = MidiSystem.getSequencer();
        } else {
          if (player == null && dataSource != null) {
            player = javax.media.Manager.createPlayer(dataSource);
            if (player != null)
              player.addControllerListener(this);
          }
          if (player != null && player.getState() < Player.Realized)
            player.realize();
        }
      } catch (Exception e) {
        System.err.println("Error realizing media \"" + mc.mediaFileName + "\"\n" + e);
      }
    }
  }

  @Override
  protected void playNow(ActiveBox setBx) {
    if (useAudioBuffer)
      super.playNow(setBx);
    else {
      try {
        if (midi) {
          if (sequencer == null)
            realize();
          if (sequencer != null && midiIs != null) {
            if (sequencer.isRunning())
              sequencer.stop();
            if (!sequencer.isOpen())
              sequencer.open();
            midiIs.reset();
            sequencer.setSequence(midiIs);
            sequencer.stop();
            setTimeRanges();
            sequencer.start();
          }
        } else {
          if (player == null && dataSource != null)
            realize();
          if (player != null) {
            boolean retry = false;
            if (mc.mediaType == MediaContent.PLAY_VIDEO)
              linkTo(setBx);
            if (player.getState() >= Player.Realized) {
              setTimeRanges();
              attachVisualComponent();
            } else {
              retry = true;
            }
            player.start();
            if (retry) {
              if (player.getState() >= Player.Realized) {
                attachVisualComponent();
              }
            }
          }
        }
      } catch (Exception e) {
        System.err.println("Error playing media \"" + mc.mediaFileName + "\":\n" + e);
      }
    }
  }

  @Override
  public void stop() {
    super.stop();
    if (!useAudioBuffer) {
      try {
        if (midi && sequencer != null) {
          closeMidiSequencer();
        } else if (player != null && player.getState() == Player.Started)
          player.stop();
      } catch (Exception e) {
        System.err.println("Error stopping media \"" + mc.mediaFileName + "\":\n" + e);
      }
    }
  }

  @Override
  public void clear() {
    super.clear();
    if (!useAudioBuffer) {
      try {
        if (midi) {
          if (sequencer != null) {
            sequencer.close();
            sequencer = null;
          }
          if (midiIs != null) {
            midiIs.close();
            midiIs = null;
          }
        } else if (player != null) {
          destroyVisualComponent();
          player.close();
        }
      } catch (Exception e) {
        System.err.println("Error closing media \"" + mc.mediaFileName + "\":\n" + e);
      }
    }
  }

  public void controllerUpdate(javax.media.ControllerEvent event) {
    if (player == null)
      return;
    try {
      if (event instanceof javax.media.RealizeCompleteEvent) {
        setTimeRanges();
        attachVisualComponent();
        if (player != null)
          player.prefetch();
      } else if (event instanceof javax.media.EndOfMediaEvent) {
        if (mc.loop) {
          player.setMediaTime(new javax.media.Time(0));
          player.start();
        }
      } else if (event instanceof javax.media.ControllerErrorEvent) {
        ps.setSystemMessage(ps.getMsg("msg_error_playing_media"), "");
        System.err.println("Controller error event:\n" + ((javax.media.ControllerErrorEvent) event).getMessage());
        clear();
      } else if (event instanceof javax.media.ControllerClosedEvent) {
        visualComponent = null;
        destroyVisualComponent();
        player.deallocate();
        player = null;
      }
    } catch (Exception e) {
      System.err.println("Controller event update error for \"" + mc.mediaFileName + "\":\n" + e);
    }
  }

  protected void setTimeRanges() {
    if (useAudioBuffer)
      return;
    try {
      if (midi && sequencer != null) {
        sequencer.setTickPosition(0L);
      } else if (player != null && player.getState() >= Player.Realized) {
        if (mc.from != -1)
          player.setMediaTime(new javax.media.Time(1000000L * mc.from));
        else
          player.setMediaTime(new javax.media.Time(0L));

        if (mc.to != -1)
          player.setStopTime(new javax.media.Time(1000000L * mc.to));
      }
    } catch (Exception e) {
      System.err.println("Error setting time ranges for \"" + mc.mediaFileName + "\":\n" + e);
    }
  }

  protected Component getVisualComponent() {
    if (player == null || player.getState() < javax.media.Player.Realized)
      return null;
    else
      return player.getVisualComponent();
  }
}
