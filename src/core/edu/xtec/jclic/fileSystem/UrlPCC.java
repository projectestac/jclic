/*
 * File    : UrlPCC.java
 * Created : 12-jul-2000 12:36
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
public class UrlPCC extends PCCFileSystem {

  byte[] pccBytes = null;

  public UrlPCC(String rootPath, String fName, ResourceBridge rb) throws Exception {
    super(rootPath, fName, rb);
    pccBytes = super.getBytes(pccName);
    ByteArrayInputStream bais = new ByteArrayInputStream(pccBytes);
    DataInputStream dis = new DataInputStream(bais);
    initPCC(dis);
    dis.close();
  }

  protected byte[] getBytes(int entryNum) throws IOException {
    long fileSize = fe[entryNum].size;
    long offset = fe[entryNum].offset;
    byte[] b = new byte[(int) fileSize];
    if (fileSize > 0)
      System.arraycopy(pccBytes, (int) offset, b, 0, (int) fileSize);
    return b;
  }

  @Override
  public void close() {
    pccBytes = null;
    super.close();
  }
}
