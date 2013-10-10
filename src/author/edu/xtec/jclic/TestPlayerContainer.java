/*
 * File    : TestPlayerContainer.java
 * Created : 20-jan-2004 16:14
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

package edu.xtec.jclic;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public interface TestPlayerContainer {
    
    public Player getTestPlayer();
    public void test();
    public void editActivity(String activityName);
    
}
