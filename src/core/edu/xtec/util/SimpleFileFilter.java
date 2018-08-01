/*
 * File    : SimpleFileFilter.java
 * Created : 01-feb-2001 18:07
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

package edu.xtec.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class SimpleFileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {

  private String m_description = null;
  private String[] m_extension = null;

  public SimpleFileFilter(String extension, String description) {
    this(new String[] { extension }, description);
  }

  public SimpleFileFilter(String[] extension, String description) {
    m_description = description;
    m_extension = extension;
    if (m_extension != null)
      for (int i = 0; i < m_extension.length; i++)
        if (!m_extension[i].startsWith("."))
          m_extension[i] = "." + m_extension[i];
  }

  public String getDescription() {
    return m_description;
  }

  public boolean accept(File f) {
    boolean result = false;
    if (f == null)
      result = false;
    else if (f.isDirectory())
      result = true;
    else if (m_extension != null) {
      String s = f.getName().toLowerCase();
      for (String ex : m_extension)
        if (ex.equals(".*") || s.endsWith(ex)) {
          result = true;
          break;
        }
    }
    return result;
  }

  public FilenameFilter getFilenameFilter() {
    return new FilenameFilter() {
      public boolean accept(File f, String name) {
        return SimpleFileFilter.this.accept(new File(f, name));
      }
    };
  }

  public File checkFileExtension(File f) {
    File result = f;
    if (!accept(result)) {
      for (String ex : m_extension)
        if (!ex.equals(".*"))
          result = new File(f.getAbsolutePath() + ex);
    }
    return result;
  }
}
