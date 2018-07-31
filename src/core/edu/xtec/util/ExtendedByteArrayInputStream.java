/*
 * File    : ExtendedByteArrayInputStream.java
 * Created : 21-sep-2001 10:14
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

import java.io.ByteArrayInputStream;

/**
 * This class extends {@link java.io.ByteArrayInputStream} in two ways: it adds a "name" member, and
 * gives read-only acces to its protected members <CODE>pos</CODE>, <CODE>mark</CODE> and <CODE>
 * count</CODE>.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ExtendedByteArrayInputStream extends ByteArrayInputStream {

  protected String m_name;

  /** Creates new ExtendedByteArrayInputStream */
  public ExtendedByteArrayInputStream(byte[] buffer, String name) {
    super(buffer);
    m_name = name;
  }

  public ExtendedByteArrayInputStream(byte[] buffer, int offset, int length, String name) {
    super(buffer, offset, length);
    m_name = name;
  }

  public ExtendedByteArrayInputStream duplicate() {
    return new ExtendedByteArrayInputStream(buf, m_name);
  }

  public String getName() {
    return m_name;
  }

  public int getPos() {
    return pos;
  }

  public int getMark() {
    return mark;
  }

  public int getCount() {
    return count;
  }

  public boolean eosReached() {
    return pos >= count;
  }

  public long seek(long param) {
    pos = 0;
    return skip(param);
  }

  public byte[] getBuffer() {
    return buf;
  }

  @Override
  public boolean markSupported() {
    return true;
  }
}
