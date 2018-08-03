/*
 * File    : UserReport.java
 * Created : 25-jan-2003 18:58
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
public class UserReport extends AbstractServlet {

  protected RequestProcessor createRP() throws Exception {
    return new edu.xtec.jclic.report.rp.UserReport();
  }
}
