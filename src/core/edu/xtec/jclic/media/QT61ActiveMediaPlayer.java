/*
 * File    : QT61ActiveMediaPlayer.java
 * Created : 19-sep-2003 11:02
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
import java.awt.Component;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class QT61ActiveMediaPlayer extends ActiveMediaPlayer {

  quicktime.app.view.MoviePlayer player;
  quicktime.app.view.QTComponent canvas;
  boolean realized;

  /** Creates new QT61ActiveMediaPlayer */
  public QT61ActiveMediaPlayer(edu.xtec.jclic.media.MediaContent mc, MediaBag mb, PlayStation ps) {
    super(mc, mb, ps);
    player = null;
    realized = false;
    if (!useAudioBuffer) {
      try {
        player = QT61Tools.getPlayer(mb.getMediaDataSource(mc.mediaFileName));
      } catch (Exception ex) {
        System.err.println("Error reading media \"" + mc.mediaFileName + "\":\n" + ex);
      }
    }
  }

  public AudioBuffer createAudioBuffer(int seconds) throws Exception {
    return new QT61AudioBuffer(mc.length);
  }

  public void realize() {
    if (!useAudioBuffer) {
      try {
        if (player != null) {
          attachVisualComponent();
          player.getMovie().setTimeScale(1000);
          setTimeRanges();
          realized = true;
        }
      } catch (Exception ex) {
        System.err.println("QuickTime exception:\n" + ex);
      }
    }
  }

  @Override
  protected void playNow(ActiveBox setBx) {
    if (useAudioBuffer)
      super.playNow(setBx);
    else if (player != null) {
      try {
        stop();
        if (!realized)
          realize();
        if (mc.mediaType == MediaContent.PLAY_VIDEO)
          linkTo(setBx);
        attachVisualComponent();
        player.setTime(Math.max(mc.from, 0));
        quicktime.app.time.TaskAllMovies.addMovieAndStart();
        player.getMovie().setActive(true);
        player.setRate(1.0F);
      } catch (Exception ex) {
        System.err.println("QuickTime Exception:\n" + ex);
      }
    }
  }

  @Override
  public void stop() {
    super.stop();
    if (!useAudioBuffer) {
      try {
        if (player != null) {
          player.setRate(0);
          player.getMovie().setActive(false);
          quicktime.app.time.TaskAllMovies.removeMovie();
        }
      } catch (Exception ex) {
        System.err.println("QuickTime Error:\n" + ex);
      }
    }
  }

  @Override
  public void clear() {
    super.clear();
    if (!useAudioBuffer) {
      try {
        if (player != null) {
          destroyVisualComponent();
          realized = false;
        }
      } catch (Exception ex) {
        System.err.println("QuickTime Error:\n" + ex);
      }
    }
  }

  protected void setTimeRanges() {
    if (useAudioBuffer || player == null)
      return;
    try {
      if (mc.from >= 0 || mc.to >= 0) {
        int from = Math.max(0, mc.from);
        int to = mc.to;
        if (to < 0) {
          to = player.getDuration();
        }
        player.setTime(Math.max(mc.from, 0));
        player.getMovie().setActiveSegment(new quicktime.std.movies.TimeInfo(from, to - from));
      }
    } catch (Exception ex) {
      System.err.println("QuickTime Error:\n" + ex);
    }
  }

  protected Component getVisualComponent() {
    if (player == null || mc.mediaType != MediaContent.PLAY_VIDEO)
      return null;
    if (canvas == null) {
      try {
        canvas = quicktime.app.view.QTFactory.makeQTComponent(player.getMovie());
      } catch (Exception ex) {
        System.err.println("Error building QTCanvas!\n" + ex);
      }
    }
    return (Component) canvas;
  }
}
