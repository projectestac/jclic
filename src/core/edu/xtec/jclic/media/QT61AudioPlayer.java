/*
 * File    : QT61AudioPlayer.java
 * Created : 19-sep-2003 11:01
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

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class QT61AudioPlayer implements AudioPlayer {

  quicktime.app.view.MoviePlayer player;

  /** Creates new QT61AudioPlayer */
  public QT61AudioPlayer() {
    player = null;
  }

  public void play() {
    if (player != null) {
      stop();
      try {
        player.setTime(0);
        quicktime.app.time.TaskAllMovies.addMovieAndStart();
        player.getMovie().setActive(true);
        player.setRate(1.0F);
      } catch (Exception ex) {
        System.err.println("QT Exception:\n" + ex);
      }
    }
  }

  public boolean setDataSource(Object source) throws Exception {
    close();
    player = QT61Tools.getPlayer(source);
    return player != null;
  }

  public void stop() {
    if (player != null) {
      try {
        player.setRate(0);
        player.getMovie().setActive(false);
        quicktime.app.time.TaskAllMovies.removeMovie();
      } catch (Exception ex) {
        System.err.println("QT Error:\n" + ex);
      }
    }
  }

  public void close() {
    if (player != null) {
      stop();
      if (player != null) {
        try {
          player.getMovie().disposeQTObject();
        } catch (Exception ex) {
          System.err.println("QT Error:\n" + ex);
        }
      }
      player = null;
    }
  }

  public void realize(String fileName, MediaBag mediaBag) throws Exception {
    if (fileName != null) {
      setDataSource(mediaBag.getMediaDataSource(fileName));
    }
  }
}
