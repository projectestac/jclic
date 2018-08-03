/*
 * File    : FileChooserForFiles.java
 * Created : 18-apr-2002 16:43
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

import java.io.File;
import java.lang.reflect.Method;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.metal.MetalFileChooserUI;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class FileChooserForFiles extends JFileChooser {

  /** Creates new FileChooserForFiles */
  public FileChooserForFiles() {
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  public FileChooserForFiles(File file) {
    super(file);
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  public FileChooserForFiles(File currentDirectory, FileSystemView fsv) {
    super(currentDirectory, fsv);
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  public FileChooserForFiles(FileSystemView fsw) {
    super(fsw);
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  public FileChooserForFiles(String file) {
    super(file);
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  public FileChooserForFiles(String currentDirectory, FileSystemView fsv) {
    super(currentDirectory, fsv);
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  @Override
  public void setCurrentDirectory(final File dir) {
    final String oldFileName = getCurrentFileName();
    super.setCurrentDirectory(dir);

    // TODO: interpret oldFilename for multiselection
    if (oldFileName != null && oldFileName.indexOf('"') == -1) {
      if (getCurrentFileName() == null) {
        setSelectedFile(new File(dir, oldFileName));
      }
    }
  }

  static Class<?> WindowsFileChooserUIClass;
  static Method getFileNameMethod;

  static {
    try {
      if (FileChooserForFiles.class
          .getResource("/com/sun/java/swing/plaf/windows/WindowsFileChooserUI.class") != null) {
        WindowsFileChooserUIClass = Class.forName("com.sun.java.swing.plaf.windows.WindowsFileChooserUI");
        if (WindowsFileChooserUIClass != null) {
          getFileNameMethod = WindowsFileChooserUIClass.getMethod("getFileName", (Class[]) null);
        }
      }
    } catch (Exception ex) {
      // Class not found: ignore
    }
  }

  /** JDKBUG: get current selected filename. */
  private String getCurrentFileName() {
    final FileChooserUI fchui = getUI();
    String fileName;
    if (fchui instanceof MetalFileChooserUI) {
      fileName = ((MetalFileChooserUI) fchui).getFileName();
    } else if (WindowsFileChooserUIClass != null && WindowsFileChooserUIClass.isInstance(fchui)
        && getFileNameMethod != null) {
      try {
        fileName = (String) getFileNameMethod.invoke(fchui, (Object[]) null);
      } catch (Exception ex) {
        fileName = null;
      }
    } else {
      fileName = null;
    }
    return fileName == null || fileName.trim().length() == 0 ? null : fileName;
  }

  public void directSetSelectedFile(final File selectedFile) {
    super.setSelectedFile(selectedFile);
  }

  /** JDKBUG: avoid unsetting filename when a directory is selected. */
  @Override
  public void setSelectedFile(final File selectedFile) {
    if (selectedFile != null) {
      if ((selectedFile.isDirectory() && isDirectorySelectionEnabled())
          || (!selectedFile.isDirectory() && isFileSelectionEnabled())) {
        super.setSelectedFile(selectedFile);
      }
    }
  }
}
