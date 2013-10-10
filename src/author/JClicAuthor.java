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

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public abstract class JClicAuthor {
    
    public static final int INSTANCE_PORT=5874;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SingleInstanceJFrame jcp=new SingleInstanceJFrame(
        "edu.xtec.jclic.AuthorSingleFrame", args, 
        "JClic author", "icons/logo_author.png", 
        "icons/miniauthor.png",
        INSTANCE_PORT);
        if(jcp.isArmed())
            //jcp.show();
            jcp.setVisible(true);
        else
            System.exit(0);
    }    
}
