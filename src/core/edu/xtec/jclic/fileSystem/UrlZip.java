/*
 * File    : UrlZip.java
 * Created : 25-sep-2001 10:54
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
import java.util.ArrayList;
import java.util.zip.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class UrlZip extends ZipFileSystem {

  /** Creates new ZipFileSystem */
  public UrlZip(String rootPath, String fName, ResourceBridge rb) throws Exception {
    super(rootPath, fName, rb);
    ZipInputStream zis = new ZipInputStream(super.getInputStream(fName));
    ArrayList<UrlZipEntry> v = new ArrayList<UrlZipEntry>();
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = zis.read(buffer, 0, 1024)) > 0) baos.write(buffer, 0, bytesRead);
      v.add(new UrlZipEntry(entry, baos.toByteArray()));
      zis.closeEntry();
    }
    zis.close();
    entries = v.toArray(new UrlZipEntry[v.size()]);
  }

  protected class UrlZipEntry extends ExtendedZipEntry {
    byte[] data;

    UrlZipEntry(ZipEntry entry) {
      super(entry);
      data = null;
    }

    UrlZipEntry(ZipEntry entry, byte[] setData) {
      super(entry);
      data = setData;
    }

    public byte[] getBytes() throws IOException {
      return data;
    }

    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(data);
    }
  }
}
