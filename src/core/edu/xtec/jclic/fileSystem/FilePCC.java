/*
 * File    : FilePCC.java
 * Created : 12-jul-2000 12:30
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

import edu.xtec.util.ResourceBridge;
import java.io.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class FilePCC extends PCCFileSystem {

  RandomAccessFile raf;

  public FilePCC(String rootPath, String fName, ResourceBridge rb) throws Exception {
    super(rootPath, fName, rb);
    open();
  }

  @Override
  protected void open() throws Exception {
    if (raf == null) {
      DataInputStream dis = super.getDataInputStream(pccName);
      initPCC(dis);
      dis.close();
      raf = new RandomAccessFile(sysFn(getCanonicalNameOf(root + pccName)), "r");
    }
  }

  protected byte[] getBytes(int entryNum) throws IOException {
    long fileSize = fe[entryNum].size;
    byte[] b = new byte[(int) fileSize];
    raf.seek(fe[entryNum].offset);
    for (long k = 0; k < fileSize; ) k += raf.read(b, (int) k, (int) (fileSize - k));
    return b;
  }

  @Override
  protected void changeBase(String newRoot, String newFileName) throws Exception {
    if (raf != null) throw new Exception("Unable to change base fileName: FileSystem is open!");
    super.changeBase(newRoot, newFileName);
    pccName = getCanonicalNameOf(newFileName);
  }

  @Override
  public void close() {
    if (raf != null) {
      try {
        raf.close();
      } catch (Exception ex) {
        // eat exception
      }
      raf = null;
    }
    super.close();
  }
}
