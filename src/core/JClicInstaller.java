/*
 * File    : JClicInstaller.java
 * Created : 01-dec-2000 17:40
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

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.ExtendedPlayer;
import edu.xtec.jclic.PlayerSettings;
import edu.xtec.jclic.SingleInstanceJFrame;
import edu.xtec.jclic.project.ProjectInstallerDlg;
import edu.xtec.util.BasicResourceBridge;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.PersistentSettings;

/*
 * JClicInstaller.java
 *
 * Created on 20 / febrer / 2004, 16:21
 */
/** @author Francesc Busquets (fbusquets@xtec.cat) */
public abstract class JClicInstaller {

  /** @param args the command line arguments */
  public static void main(String[] args) {
    boolean exit = true;
    try {
      Options options = new Options((java.awt.Component) null);
      Messages messages = PersistentSettings.getMessages(options, Constants.DEFAULT_BUNDLE);
      messages.addBundle(Constants.COMMON_SETTINGS);
      messages.addBundle(ExtendedPlayer.MESSAGES_BUNDLE);
      String installer = SingleInstanceJFrame.loadArgs(args, options);
      if (installer == null) {
        System.err.println("Error: no installer file specified!");
      } else if (!installer.endsWith(".jclic.inst")) {
        System.err.println(
            "Error: "
                + installer
                + " isn't a JClic package install script.\nJClic package install scripts end always with .jclic.inst");
      } else {
        BasicResourceBridge rb = new BasicResourceBridge(options);
        PlayerSettings settings = PlayerSettings.loadPlayerSettings(rb);
        if (settings.promptPassword(null, null)) {
          settings.checkLibrary();
          ProjectInstallerDlg pi =
              ProjectInstallerDlg.getProjectInstallerDlg(null, settings.libraryManager, installer);
          if (pi != null) {
            pi.setVisible(true);
            if (!pi.cancel && pi.launchNow && pi.pathToMainProject != null) {
              JClicPlayer.main(new String[] {pi.pathToMainProject});
              exit = false;
            }
          }
        }
      }
    } catch (Exception ex) {
      System.err.println("Error installing:\n" + ex);
    }
    if (exit) {
      System.exit(0);
    }
  }
}
