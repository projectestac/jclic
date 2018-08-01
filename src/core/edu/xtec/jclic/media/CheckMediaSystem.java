/*
 * File    : CheckMediaSystem.java
 * Created : 08-jul-2002 23:06
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
import edu.xtec.util.Check;
import edu.xtec.util.Options;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public final class CheckMediaSystem {

  public static final String QTLOCK = "QTLOCK";
  static final String BUNDLE = "messages.CheckMediaSystemMessages";
  public static Object qtLock = null;
  public static Object qt61Lock = null;

  public static boolean check(Options options, boolean showWarning) {

    boolean result = false;

    boolean qt = (qtLock != null);
    boolean qt61 = (qt61Lock != null);

    boolean checkQT = (options.getBoolean(Options.MAC) && !options.getBoolean(Options.ARCH64BIT))
        || Constants.TRUE.equalsIgnoreCase(System.getProperty("FORCE_USE_QT"));

    if (checkQT && !qt61) {
      try {
        if (CheckMediaSystem.class.getResource("/quicktime/app/view/QTFactory.class") != null) {
          Class.forName("quicktime.app.view.QTFactory");
          qt61 = true;
          qt = false;
        }
      } catch (Exception ex) {
        // No quicktime 6.1 available
      }
    }
    options.put(Constants.QT61, qt61);
    options.put(Constants.QT, qt);

    boolean jmf = false;
    try {
      if (CheckMediaSystem.class.getResource("/javax/media/Player.class") != null) {
        Class.forName("javax.media.Player");
        jmf = true;
      }
    } catch (Exception ex) {
      // no JMF available
    }
    options.put(Constants.JMF, jmf);

    String mediaSystem = (String) options.get(Constants.MEDIA_SYSTEM);
    if (mediaSystem != null) {
      if (mediaSystem.equals(Constants.QT)) {
        if (qt61)
          mediaSystem = Constants.QT61;
        else if (!qt)
          mediaSystem = null;
      } else if (mediaSystem.equals(Constants.JMF)) {
        if (!jmf)
          mediaSystem = null;
      } else
        mediaSystem = null;
    }
    if (mediaSystem == null)
      mediaSystem = qt61 ? Constants.QT61 : /* qt ? Constants.QT : */ jmf ? Constants.JMF : null;

    if (mediaSystem != null && mediaSystem.equals(Constants.QT61) && qt61Lock == null) {
      try {
        qt61Lock = new edu.xtec.jclic.media.QT61Tools();
        options.put(QTLOCK, qt61Lock);
      } catch (Exception ex) {
        options.getMessages().showErrorWarning(options.getMainComponent(), "media_qt_error_initializing", ex);
        mediaSystem = jmf ? Constants.JMF : null;
      }
    }

    options.put(Constants.MEDIA_SYSTEM, mediaSystem);

    if (showWarning && mediaSystem == null)
      warn(options);

    return mediaSystem != null;
  }

  public static void warn(Options options) {
    if (!options.getBoolean(Constants.NO_MEDIASYSTEM_WARN)) {
      Check.showUrlPane(options, "media_check_url");
      options.putBoolean(Constants.NO_MEDIASYSTEM_WARN, true);
    }
  }
}
