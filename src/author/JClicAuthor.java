/*
 * File    : JClicAuthor.java
 * Created : 17-sep-2002 10:41
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

import edu.xtec.jclic.SingleInstanceJFrame;
import edu.xtec.jclic.project.ProjectFileUtils;
import edu.xtec.util.StrUtils;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public abstract class JClicAuthor {

  public static final int INSTANCE_PORT = 5874;

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    
    int p = StrUtils.getIndexOf("-processZip", args);
    if (p >= 0 && args.length > p + 1) {
      try {
        ProjectFileUtils pfu = new ProjectFileUtils(args[p + 1]);
        pfu.normalizeFileNames();
        pfu.avoidZipLinks();
        if (args.length > p + 2 && !args[p + 2].startsWith("-")) {
          pfu.saveTo(args[p + 2]);
        }
      } catch (Exception ex) {
        System.err.println("Error processing ZIP file: " + ex.getMessage());
      }
      return;
    }

    p = StrUtils.getIndexOf("-processZipFolder", args);
    if (p >= 0 && args.length > p + 2) {
      try {
        ProjectFileUtils.processFolder(args[p+1], args[p+2]);
      } catch (Exception ex) {
        System.err.println("Error processing ZIP file: " + ex.getMessage());
      }
      return;
    }
    
    SingleInstanceJFrame jcp = new SingleInstanceJFrame(
            "edu.xtec.jclic.AuthorSingleFrame", args,
            "JClic author", "icons/logo_author.png",
            "icons/miniauthor.png",
            INSTANCE_PORT);
    if (jcp.isArmed()) //jcp.show();
    {
      jcp.setVisible(true);
    } else {
      System.exit(0);
    }
  }
}
