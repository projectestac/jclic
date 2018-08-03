/*
 * File    : BasicResourceBridge.java
 * Created : 20-feb-2004 16:08
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

package edu.xtec.util;

import java.io.InputStream;
import javax.swing.JComponent;

/**
 * This class is the most basic implementation of edu.xtec.util.ResourceBridge.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class BasicResourceBridge implements ResourceBridge {

  Options options;

  /** Creates a new instance of BasicResourceBridge */
  public BasicResourceBridge(Options options) {
    this.options = options;
  }

  public JComponent getComponent() {
    return (JComponent) options.getMainComponent();
  }

  public String getMsg(String key) {
    return options.getMessages().get(key);
  }

  public edu.xtec.util.Options getOptions() {
    return options;
  }

  public InputStream getProgressInputStream(InputStream is, int expectedLength, String name) {
    return new ProgressInputStream(is, expectedLength, name);
  }

  public void displayUrl(String url, boolean inFrame) {
  }
}
