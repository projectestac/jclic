/*
 * File    : AudioBuffer.java
 * Created : 17-sep-2001 13:47
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

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.boxes.ActiveBox;
import java.awt.Component;
import java.awt.Cursor;

/**
 * The abtract class <code>AudioBuffer</code> supplies sound recording and playing services. Audio
 * data is discarded when the <code>AudioBuffer</code> object is destroyed.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 * @see JavaSoundAudioBuffer
 */
public abstract class AudioBuffer {

  /** Default maximum length of recordings (in seconds). */
  public static final int MAX_RECORD_LENGTH = 180;

  /** Maximum recording length, in seconds. */
  protected int m_seconds;

  /** Cursor displayed when recording. */
  protected static Cursor recCursor;

  /**
   * Currently running <code>AudioBuffer</code> (static object, because only one can be running).
   */
  protected static AudioBuffer activeAudioBuffer;

  /** Owner of the active <code>AudioBuffer</code>. */
  protected static Component owner;

  /** Original <code>Cursor</code> used by the active <code>AudioBuffer</code>. */
  protected static Cursor backOwnerCursor;

  /** Main owner (usually a {@link java.awt.Frame}) of the active <code>AudioBuffer</code>. */
  protected static Component mainOwner;

  /** Original {@link java.awt.Cursor} of the main owner of the active <code>AudioBuffer</code>. */
  protected static Cursor backMainOwnerCursor;

  /**
   * Creates new AudioBuffer
   *
   * @param seconds Maximum amount of seconds allowed for recording
   * @throws Exception If something goes wrong...
   */
  public AudioBuffer(int seconds) throws Exception {
    if (seconds <= 0 || seconds > MAX_RECORD_LENGTH)
      throw new Exception(
          "Error: Audio buffer length can't exceed " + MAX_RECORD_LENGTH + " seconds");

    m_seconds = seconds;
  }

  /**
   * Plays the recorded audio data, if any.
   *
   * @throws Exception If something goes wrong
   */
  public abstract void play() throws Exception;

  /** Stops playing or recording, if running. */
  public abstract void stop();

  /** Performs cleanup of recorded audio data. */
  protected abstract void clear();

  /**
   * Performs explicit cleanup of recorded audio data before destroying the object.
   *
   * @throws Throwable Throwed by <CODE>Object#finalize</CODE>
   */
  @Override
  protected void finalize() throws Throwable {
    clear();
    super.finalize();
  }

  /**
   * Checks if the <CODE>AudioBuffer</CODE> is currently recording or playing sound.
   *
   * @return <CODE>true</CODE> if recording or playing, <CODE>false</CODE> otherwise.
   */
  public static boolean busy() {
    return activeAudioBuffer != null;
  }

  /**
   * Only one <CODE>AudioBuffer</CODE> can be "active", because the sound recording hardware cannot
   * be shared between processes. This method returns the currently active <CODE>AudioBuffer</CODE>,
   * if any.
   *
   * @return The currently active <CODE>AudioBuffer</CODE>, or <CODE>null</CODE> if none is active.
   */
  protected static AudioBuffer getActiveAudioBuffer() {
    return activeAudioBuffer;
  }

  /**
   * Starts sound recording with visual indications: the mouse cursor switchs a microphone.
   *
   * @param ps A valid {@link PlayStation}, used to retrieve the recording cursor image and to
   *     determine the main component associated to this <CODE>AudioBuffer</CODE>.
   * @param bx The {@link ActiveBox} associated to this recording. If <CODE>null</CODE>, the default
   *     {@link Component} for the provided {@link PlayStation} will be used.
   * @throws Exception If someting goes wrong
   */
  public void record(PlayStation ps, ActiveBox bx) throws Exception {
    forceStop();
    recCursor = ps.getCustomCursor(Constants.REC_CURSOR);
    owner = (bx == null ? null : bx.getContainerResolve());
    mainOwner = javax.swing.JOptionPane.getFrameForComponent(ps.getComponent());
    showRecordingCursor();
    record();
  }

  /**
   * Starts the recording of sound, without any visual indication. Subclasses of <CODE>AudioBuffer
   * </CODE> must implement this method.
   *
   * @throws Exception If something goes wrong.
   */
  protected abstract void record() throws Exception;

  /**
   * The usual way to stop the recording or playnig processes is to place a flag in their thread and
   * wait to next cycle. This method allows to force an abrupt end of this threads.
   *
   * @throws Exception If something goes wrong
   */
  protected static void forceStop() throws Exception {
    if (busy()) {
      getActiveAudioBuffer().stop();
      Thread.yield();
      if (busy()) throw new Exception("Unable to stop recorder!");
    }
  }

  /**
   * Displays the recording cursor. Prior to call this method, the associated Component must be
   * specified.
   */
  protected static void showRecordingCursor() {
    if (recCursor != null) {
      if (owner != null) {
        backOwnerCursor = owner.getCursor();
        if (backOwnerCursor == Cursor.getDefaultCursor()) backOwnerCursor = null;
        owner.setCursor(recCursor);
      }
      if (mainOwner != null) {
        backMainOwnerCursor = mainOwner.getCursor();
        if (backMainOwnerCursor == Cursor.getDefaultCursor()) backMainOwnerCursor = null;
        mainOwner.setCursor(recCursor);
      }
    }
  }

  /** Resets the mouse cursor to its original state. */
  protected static void hideRecordingCursor() {
    if (owner != null) {
      owner.setCursor(backOwnerCursor);
      owner = null;
    }
    if (mainOwner != null) {
      mainOwner.setCursor(backMainOwnerCursor);
      mainOwner = null;
    }
  }
}
