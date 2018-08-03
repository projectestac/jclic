/*
 * File    : Resource.java
 * Created : 23-jan-2003 13:13
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

package edu.xtec.jclic.report.servlet;

import edu.xtec.servlet.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class Resource extends AbstractServlet {

  protected RequestProcessor createRP() throws Exception {
    return new ResourceRP();
  }
}
