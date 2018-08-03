/*
 * File    : PCCFileSystem.java
 * Created : 20-jun-2000 10:00
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

package edu.xtec.jclic.fileSystem;

import edu.xtec.jclic.clic3.Clic3;
import edu.xtec.util.ResourceBridge;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public abstract class PCCFileSystem extends FileSystem {

  String pccName;
  public String pccVersion;
  public String pccDescription;
  public int numFiles;
  public int baseOffset;
  PCCFileEntry[] fe;

  public PCCFileSystem(ResourceBridge rb) {
    super("", rb);
  }

  public PCCFileSystem(String rootPath, String pccFileName, ResourceBridge rb) {
    super(rootPath, rb);
    pccName = getCanonicalNameOf(pccFileName);
  }

  public static PCCFileSystem createPCCFileSystem(String rootPath, String pccFileName, ResourceBridge rb)
      throws Exception {
    if (isStrUrl(rootPath))
      return new UrlPCC(rootPath, pccFileName, rb);
    else
      return new FilePCC(rootPath, pccFileName, rb);
  }

  @Override
  public String getFullRoot() {
    return root + pccName;
  }

  protected class PCCFileEntry extends Object {
    String fileName;
    long size, offset;

    public PCCFileEntry(String fName, long o, long s) {
      fileName = stdFn(fName);
      size = s;
      offset = o;
    }
  }

  void initPCC(DataInputStream dis) throws Exception {
    if ((pccVersion = extractLineFromDIS(dis)) != null && pccVersion.compareTo("PCC00") == 0) {
      pccDescription = extractLineFromDIS(dis);
      numFiles = extractShortInt(dis);
      baseOffset = (int) extractShortLong(dis);
      fe = new PCCFileEntry[numFiles];
      for (int i = 0; i < numFiles; i++) {
        fe[i] = new PCCFileEntry(Clic3.validFileName(extractSzString(dis)), extractShortLong(dis),
            extractShortLong(dis));
        if (fe[i].fileName.length() == 0)
          break;
      }
    }
  }

  public String getEntryInfo(int n) {
    return (n < 0 || n >= numFiles) ? "OUT OF INDEX!" : fe[n].toString();
  }

  public String getEntryName(int n) {
    return (n < 0 || n >= numFiles) ? "OUT OF INDEX!" : fe[n].fileName;
  }

  public int getEntryNum(String fName) {
    int i = -1;
    String normalizedFName = stdFn(fName);

    if (numFiles > 0) {
      for (i = 0; i < numFiles; i++) {
        if (fe[i].fileName.equals(normalizedFName))
          break;
      }
    }
    if (i == numFiles && altFileNames.get(fName) != null) {
      // not found, but found in altFileNames!
      normalizedFName = stdFn((String) altFileNames.get(fName));
      for (i = 0; i < numFiles; i++) {
        if (fe[i].fileName.equals(normalizedFName))
          break;
      }
    }
    return (i == numFiles ? -1 : i);
  }

  protected abstract byte[] getBytes(int entryNum) throws IOException;

  @Override
  public byte[] getBytes(String fileName) throws IOException {
    int entryNum;
    if ((entryNum = getEntryNum(fileName)) == -1)
      return super.getBytes(fileName);
    return getBytes(entryNum);
  }

  @Override
  public long getFileLength(String fileName) throws IOException {
    int entryNum;
    return (entryNum = getEntryNum(fileName)) == -1 ? super.getFileLength(fileName) : fe[entryNum].size;
  }

  @Override
  public Image getImageFile(String fName) throws Exception {
    int entryNum;
    return (entryNum = getEntryNum(fName)) == -1 ? super.getImageFile(fName)
        : Toolkit.getDefaultToolkit().createImage(getBytes(entryNum));
  }

  public java.io.DataInputStream getDataInputStream(String fName) throws IOException {
    return new DataInputStream(super.getInputStream(fName));
  }

  public String extractLine(BufferedReader bfr) throws IOException {
    return bfr.readLine();
  }

  public String extractLineFromDIS(DataInputStream dis) throws IOException {
    StringBuilder str = new StringBuilder();
    boolean skipNext = false;
    while (true) {
      byte b = dis.readByte();
      if (skipNext)
        break;
      else if (b == 0x0D)
        skipNext = true;
      else
        str.append((char) b);
    }
    return str.substring(0);
  }

  public String extractSzString(BufferedReader bfr) throws IOException {
    StringBuilder str = new StringBuilder();
    char b[] = new char[1];
    while (true) {
      bfr.read(b, 0, 1);
      if (b[0] == 0)
        break;
      else
        str.append(b[0]);
    }
    return str.substring(0);
  }

  public String extractSzString(DataInputStream dis) throws IOException {
    StringBuilder str = new StringBuilder();
    while (true) {
      byte b = dis.readByte();
      if (b == 0)
        break;
      else
        str.append((char) b);
    }
    return str.substring(0);
  }

  public int extractShortInt(DataInputStream bfr) throws IOException {
    int bt = bfr.readUnsignedByte();
    int r = bt;
    bt = bfr.readUnsignedByte();
    r += (bt << 8);
    return r;
  }

  public long extractShortLong(DataInputStream bfr) throws IOException {
    long bt = bfr.readUnsignedByte();
    long r = bt;
    bt = bfr.readUnsignedByte();
    r += (bt << 8);
    bt = bfr.readUnsignedByte();
    r += (bt << 16);
    bt = bfr.readUnsignedByte();
    r += (bt << 24);
    return r;
  }

  @Override
  public void close() {
    fe = null;
    super.close();
  }
}
