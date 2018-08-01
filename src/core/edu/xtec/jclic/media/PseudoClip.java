/*
 * File    : PseudoClip.java
 * Created : 04-aug-2004 16:10
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

import javax.sound.sampled.AudioFormat;

/** @author Francesc Busquets (fbusquets@xtec.cat) */
public interface PseudoClip {
  // Methods from Clip
  public void loop(int count) throws Exception;

  public void setFramePosition(int frames);

  public void setLoopPoints(int start, int end);

  // Methods from DataLine
  public AudioFormat getFormat();

  public boolean isActive();

  public boolean isRunning();

  public void start() throws Exception;

  public void stop();

  // Methods from Line
  public void close();

  public boolean isOpen();

  // Special methods
  public void open() throws Exception;
}
