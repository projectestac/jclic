
/*
 * File    : JClicPlayer.java
 * Created : 01-dec-2000 12:34
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

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public abstract class JClicPlayer {

  public static final int INSTANCE_PORT = 5872;

  /** @param args the command line arguments */
  public static void main(String args[]) {
    SingleInstanceJFrame jcp = new SingleInstanceJFrame("edu.xtec.jclic.ExtendedPlayer", args, "JClic player",
        "icons/logo.png",
        "icons/miniclic.png", INSTANCE_PORT);
    if (jcp.isArmed())
      jcp.setVisible(true);
    else
      System.exit(0);
  }
}
