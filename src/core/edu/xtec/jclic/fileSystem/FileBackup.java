/*
 * File    : FileBackup.java
 * Created : 14-nov-2002 15:23
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

package edu.xtec.jclic.fileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** @author Francesc Busquets (fbusquets@xtec.cat) */
public class FileBackup {

  File file;
  File backup;
  List<File> createdDirs;
  static final String BAK_EXT = ".bak";

  /** Creates a new instance of FileBackup */
  public FileBackup(File file) throws java.io.IOException {
    this.file = file;
    if (file.exists() && file.isFile()) {
      backup = new File(file.getAbsolutePath() + BAK_EXT);
      if (backup.exists()) backup.delete();
      file.renameTo(backup);
    } else if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
      File f = file.getParentFile();
      createdDirs = new ArrayList<File>(2);
      while (f != null && !f.exists()) {
        createdDirs.add(f);
        f = f.getParentFile();
      }
      file.getParentFile().mkdirs();
    }
  }

  public void rollback() throws java.io.IOException {
    if (backup != null) {
      if (file.exists()) file.delete();
      backup.renameTo(file);
      backup = null;
    } else {
      file.delete();
      file = null;
      if (createdDirs != null) {
        for (int i = 0; i < createdDirs.size(); i++) {
          File f = createdDirs.get(i);
          if (f.isDirectory()) f.delete();
        }
        createdDirs = null;
      }
    }
  }

  public void cleanup() throws java.io.IOException {
    if (backup != null) {
      backup.delete();
      backup = null;
    }
  }

  /**
   * Getter for property file.
   *
   * @return Value of property file.
   */
  public java.io.File getFile() {
    return file;
  }

  /**
   * Getter for property backup.
   *
   * @return Value of property backup.
   */
  public java.io.File getBackup() {
    return backup;
  }
}
