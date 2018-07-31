/*
 * File    : JMFAudioPlayer.java
 * Created : 25-may-2002 19:27
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
import edu.xtec.util.ExtendedByteArrayInputStream;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class JMFAudioPlayer implements AudioPlayer {

  protected javax.media.Player player;
  public static final javax.media.Time zeroTime = new javax.media.Time(0L);

  /** Creates new JMFAudioPlayer */
  public JMFAudioPlayer() {
    player = null;
  }

  public boolean setDataSource(Object source) throws Exception {
    close();

    if (source instanceof ExtendedByteArrayInputStream) {
      player =
          javax.media.Manager.createPlayer(
              new ByteDataSource((ExtendedByteArrayInputStream) source));
    } else if (source instanceof String) {
      javax.media.MediaLocator ml = new javax.media.MediaLocator((String) source);
      javax.media.protocol.DataSource dataSource = javax.media.Manager.createDataSource(ml);
      player = javax.media.Manager.createPlayer(dataSource);
    }

    return player != null;
  }

  public void realize(String fileName, MediaBag mediaBag) throws Exception {
    if (fileName != null) setDataSource(mediaBag.getMediaDataSource(fileName));
    if (player != null) player.realize();
  }

  public void close() {
    if (player != null) {
      player.close();
      player = null;
    }
  }

  public void play() {
    try {
      stop();
      player.setMediaTime(zeroTime);
      player.start();
    } catch (Exception ex) {
      System.err.println("Error playing system sound:\n" + ex);
    }
  }

  public void stop() {
    if (player != null && player.getState() >= javax.media.Player.Started) player.stop();
  }
}
