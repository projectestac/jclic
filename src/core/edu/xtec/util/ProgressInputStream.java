/*
 * File    : ProgressInputStream.java
 * Created : 04-jul-2002 13:48
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ProgressInputStream extends FilterInputStream {

  private int nread;
  private int size;
  private boolean started;
  private List<ProgressInputStreamListener> listeners;
  private String name;

  public ProgressInputStream(InputStream in, int expectedLength, String name) {
    super(in);
    this.listeners = new ArrayList<ProgressInputStreamListener>();
    this.name = name;
    started = false;
    nread = 0;
    size = expectedLength;
    if (size <= 0) {
      try {
        size = in.available();
      } catch (IOException ioe) {
        size = 0;
      }
    }
  }

  public void addProgressInputStreamListener(ProgressInputStreamListener isl) {
    if (!listeners.contains(isl)) {
      listeners.add(isl);
    }
  }

  private void start() {
    if (!started) {
      notifyListeners(MAX, size);
      notifyListeners(VALUE, 0);
      notifyListeners(START, 0);
      started = true;
    }
  }

  @Override
  public int read() throws IOException {
    if (!started)
      start();
    int c = in.read();
    if (c >= 0)
      notifyListeners(VALUE, size);
    return c;
  }

  @Override
  public int read(byte b[]) throws IOException {
    if (!started)
      start();
    int nr = in.read(b);
    if (nr > 0)
      notifyListeners(VALUE, nread += nr);
    return nr;
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    if (!started)
      start();
    int nr = in.read(b, off, len);
    if (nr > 0)
      notifyListeners(VALUE, nread += nr);
    return nr;
  }

  @Override
  public long skip(long n) throws IOException {
    if (!started)
      start();
    long nr = in.skip(n);
    if (nr > 0)
      notifyListeners(VALUE, nread += nr);
    return nr;
  }

  @Override
  public void close() throws IOException {
    if (!started)
      start();
    in.close();
    notifyListeners(END, 0);
  }

  @Override
  public synchronized void reset() throws IOException {
    if (!started)
      start();
    in.reset();
    nread = size - in.available();
    notifyListeners(VALUE, nread);
  }

  protected static final int MAX = 0, VALUE = 1, START = 2, END = 3;

  private void notifyListeners(int action, int value) {
    for (ProgressInputStreamListener isl : listeners) {
      switch (action) {
      case MAX:
        isl.setProgressMax(value);
        break;
      case VALUE:
        isl.setProgressValue(value);
        break;
      case START:
        isl.startProgress(name);
        break;
      case END:
        isl.endProgress();
        break;
      default:
        break;
      }
    }
  }

  public interface ProgressInputStreamListener {
    void setProgressMax(int max);

    void setProgressValue(int value);

    void startProgress(String strName);

    void endProgress();
  }
}
