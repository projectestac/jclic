/*
 * File    : QT61Tools.java
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

import edu.xtec.util.ExtendedByteArrayInputStream;
import edu.xtec.util.StreamIO;
import java.io.File;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class QT61Tools extends Object {

  private static QT61Lock lock;

  public QT61Tools() throws Exception {
    if (lock == null) {
      lock = new QT61Lock();
    }
  }

  protected class QT61Lock extends Object {
    protected QT61Lock() throws Exception {
      if (!quicktime.QTSession.isInitialized()) {
        quicktime.QTSession.open();
      }
    }

    @Override
    protected void finalize() throws Throwable {
      quicktime.QTSession.close();
      super.finalize();
    }
  }

  public static quicktime.std.movies.Movie getMovie(Object source, boolean midi) throws Exception {
    quicktime.std.movies.Movie movie;
    quicktime.util.QTHandle handle = null;
    String sourceName = "";
    if (source instanceof ExtendedByteArrayInputStream) {
      ExtendedByteArrayInputStream eias = (ExtendedByteArrayInputStream) source;
      handle = new quicktime.util.QTHandle(eias.getBuffer());
      sourceName = eias.getName();
    } else if (source instanceof File) {
      sourceName = ((File) source).getName();
      if (midi) {
        handle = new quicktime.util.QTHandle(StreamIO.readFile((File) source));
      } else {
        handle = new quicktime.std.movies.media.DataRef(new quicktime.io.QTFile((File) source));
      }
    } else if (source instanceof String) {
      sourceName = (String) source;
      if (midi) {
        handle = new quicktime.util.QTHandle(StreamIO.readFile(new File(sourceName)));
      } else {
        handle = new quicktime.std.movies.media.DataRef(sourceName);
      }
    }

    if (midi) {
      quicktime.std.qtcomponents.MovieImporter movieimporter = new quicktime.std.qtcomponents.MovieImporter(
          quicktime.util.QTUtils.toOSType("Midi"));
      movie = new quicktime.std.movies.Movie(1);
      movie.setDefaultDataRef(new quicktime.std.movies.media.DataRef(new quicktime.util.QTHandle()));
      movieimporter.fromHandle(handle, movie, null, 0, 1);
    } else {
      quicktime.std.movies.media.DataRef dr;
      if (handle instanceof quicktime.std.movies.media.DataRef)
        dr = (quicktime.std.movies.media.DataRef) handle;
      else {
        String extension = sourceName.substring(sourceName.lastIndexOf('.'));
        dr = new quicktime.std.movies.media.DataRef(handle, quicktime.std.StdQTConstants.kDataRefFileExtensionTag,
            extension);
      }
      movie = quicktime.std.movies.Movie.fromDataRef(dr, 1);
    }

    return movie;
  }

  public static quicktime.app.view.MoviePlayer getPlayer(Object source) throws Exception {
    quicktime.std.movies.Movie mv = getMovie(source, false);
    return new quicktime.app.view.MoviePlayer(mv);
  }
}
