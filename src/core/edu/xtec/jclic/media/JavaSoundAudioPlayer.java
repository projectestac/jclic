/*
 * File    : JavaSoundAudioPlayer.java
 * Created : 23-jun-2004 11:29
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
import edu.xtec.util.StreamIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.tritonus.applet.AppletMpegSPIWorkaround;
import org.tritonus.applet.AppletVorbisSPIWorkaround;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class JavaSoundAudioPlayer implements AudioPlayer {

  private Clip clip;
  protected AudioInputStream ais;
  protected boolean isMpeg;
  protected boolean isOgg;
  protected boolean isWav;

  static final int INTERNAL_BUFFER_SIZE = 1024;
  static final int CHECK_BUFFER_SIZE = 0x60;
  static final int BIT_SAMPLE_SIZE = 16;
  static final boolean BIG_ENDIAN = true;

  /** Creates a new instance of JavaSoundAudioPlayer */
  public JavaSoundAudioPlayer() {}

  public boolean setDataSource(Object source) throws Exception {

    close();

    InputStream is = null;
    javax.sound.sampled.AudioFileFormat m_audioFileFormat = null;

    if (source instanceof ExtendedByteArrayInputStream) {
      is =
          checkInputStream((InputStream) source, ((ExtendedByteArrayInputStream) source).getName());
    } else if (source instanceof InputStream) {
      is = checkInputStream((InputStream) source, null);
    } else if (source instanceof File) {
      is = checkInputStream(new java.io.FileInputStream((File) source), ((File) source).getName());
    } else {
      java.net.URL url = null;
      if (source instanceof java.net.URL) url = (java.net.URL) source;
      else if (source instanceof String) {
        url = new java.net.URL((String) source);
      }
      if (url != null) {
        is = checkInputStream(url.openStream(), source.toString());
      }
    }

    if (is != null) {
      if (isMpeg || (!isWav && !isOgg)) {
        try {
          m_audioFileFormat = AppletMpegSPIWorkaround.getAudioFileFormat(is);
          ais = AppletMpegSPIWorkaround.getAudioInputStream(is);
          isMpeg = true;
        } catch (IOException ex) {
          throw ex;
        } catch (UnsupportedAudioFileException ex) {
          isMpeg = false;
        }
      }
      if (isOgg || (!isMpeg && !isWav)) {
        try {
          m_audioFileFormat = AppletVorbisSPIWorkaround.getAudioFileFormat(is);
          ais = AppletVorbisSPIWorkaround.getAudioInputStream(is);
          isOgg = true;
        } catch (IOException ex) {
          throw ex;
        } catch (UnsupportedAudioFileException ex) {
          isOgg = false;
        }
      }
      if (isWav || (!isMpeg && !isOgg)) {
        m_audioFileFormat = AudioSystem.getAudioFileFormat(is);
        ais = AudioSystem.getAudioInputStream(is);
      }
    }

    if (ais != null) {
      AudioFormat af = ais.getFormat();
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, af, INTERNAL_BUFFER_SIZE);
      if (!AudioSystem.isLineSupported(info)) {
        AudioFormat sourceFormat = af;
        AudioFormat targetFormat =
            new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                BIT_SAMPLE_SIZE,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * (BIT_SAMPLE_SIZE / 8),
                sourceFormat.getSampleRate(),
                // sourceFormat.getFrameRate(),
                BIG_ENDIAN);

        if (isMpeg) ais = AppletMpegSPIWorkaround.getAudioInputStream(targetFormat, ais);
        else if (isOgg) ais = AppletVorbisSPIWorkaround.getAudioInputStream(targetFormat, ais);
        else ais = AudioSystem.getAudioInputStream(targetFormat, ais);
      }
    }
    return ais != null;
  }

  public Clip getClip() throws Exception {
    if (clip == null && ais != null) {
      clip =
          (Clip)
              AudioSystem.getLine(
                  new DataLine.Info(Clip.class, ais.getFormat(), INTERNAL_BUFFER_SIZE));
    }
    return clip;
  }

  public void realize(String fileName, MediaBag mediaBag) throws Exception {
    if (fileName != null) setDataSource(mediaBag.getMediaDataSource(fileName));
    if (ais != null && getClip() != null) {
      clip.open(ais);
    }
  }

  // Modified to avoid unexpected exceptions like
  // "IllegalStateException: line already closed"
  // launched by org.classpath.icedtea.pulseaudio.PulseAudioClip.close
  public void close() {
    if (clip != null && clip.isOpen()) {
      if (clip.isRunning()) clip.stop();
      clip.close();
    }
    clip = null;
    ais = null;
  }

  public void play() {
    try {
      stop();
      if (getClip() != null) {
        clip.setFramePosition(0);
        clip.start();
      }
    } catch (Exception ex) {
      System.err.println("Error playing sound:\n" + ex);
    }
  }

  public void stop() {
    if (clip != null && clip.isActive()) clip.stop();
  }

  protected InputStream checkInputStream(InputStream is, String name) throws Exception {
    String s = (name == null ? null : name.toLowerCase());
    if (s != null) {
      if (s.endsWith(".wav")) isWav = true;
      else if (s.endsWith(".ogg")) isOgg = true;
      else if (s.endsWith(".mp3")) isMpeg = true;
    }

    if (s == null || isWav) {
      byte[] data;
      if (!is.markSupported()) {
        data = StreamIO.readInputStream(is);
        is = new ByteArrayInputStream(data);
      }
      is.mark(CHECK_BUFFER_SIZE);
      byte[] b = new byte[CHECK_BUFFER_SIZE];
      is.read(b);
      is.reset();
      if (b[0x00] == 'R'
          && b[0x01] == 'I'
          && b[0x02] == 'F'
          && b[0x03] == 'F'
          && b[0x08] == 'W'
          && b[0x09] == 'A'
          && b[0x0A] == 'V'
          && b[0x0B] == 'E'
          && b[0x0C] == 'f'
          && b[0x0D] == 'm'
          && b[0x0E] == 't'
          && b[0x0F] == ' '
          && b[0x14] == 0x55
          && b[0x15] == 0x00) {
        for (int p = 0x11; p < CHECK_BUFFER_SIZE - 6; p++) {
          if (b[p] == 'd' && b[p + 1] == 'a' && b[p + 2] == 't' && b[p + 3] == 'a') {
            int offset = p + 4 + 4;
            is.skip(offset);
            isWav = false;
            isMpeg = true;
            break;
          }
        }
      }
    }
    return is;
  }
}
