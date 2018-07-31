/*
 * File    : ByteDataSource.java
 * Created : 20-sep-2001 9:14
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

package edu.xtec.jclic.media;

import edu.xtec.util.ExtendedByteArrayInputStream;
import edu.xtec.util.StreamIO;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ByteDataSource extends javax.media.protocol.PullDataSource {

  protected ByteSourceStream[] pss;
  protected javax.media.protocol.ContentDescriptor contentType;
  protected String fName;

  public ByteDataSource(ExtendedByteArrayInputStream is) throws IOException {
    super();
    this.pss = new ByteSourceStream[1];
    init(is);
  }

  public ByteDataSource(InputStream is, String name) throws IOException {
    super();
    this.pss = new ByteSourceStream[1];
    if (is instanceof ExtendedByteArrayInputStream) init((ExtendedByteArrayInputStream) is);
    else init(new ExtendedByteArrayInputStream(StreamIO.readInputStream(is), name));
  }

  public ByteDataSource(byte[] src, String name) throws IOException {
    super();
    this.pss = new ByteSourceStream[1];
    init(new ExtendedByteArrayInputStream(src, name));
  }

  private void init(ExtendedByteArrayInputStream is) throws IOException {
    contentType = getContentDescriptor(is.getName());
    pss[0] = new ByteSourceStream(is, contentType);
    fName = null;
  }

  private ByteDataSource() {
    this.pss = new ByteSourceStream[1];
  }

  public ByteDataSource duplicate() throws IOException {
    ByteDataSource result = new ByteDataSource();
    result.contentType = contentType;
    result.fName = fName;
    result.pss[0] = ((ByteSourceStream) pss[0]).duplicate();
    return result;
  }

  public ExtendedByteArrayInputStream getInputStream() {
    if (pss[0] != null) return pss[0].inputStream;
    else return null;
  }

  public String getFName() {
    return fName;
  }

  public void setFName(String name) {
    fName = name;
  }

  public java.lang.Object[] getControls() {
    return null;
  }

  public void connect() throws IOException {}

  public void disconnect() {}

  public void start() throws IOException {}

  public javax.media.Time getDuration() {
    return javax.media.Duration.DURATION_UNKNOWN;
  }

  public void stop() throws IOException {}

  public java.lang.Object getControl(java.lang.String str) {
    return null;
  }

  public java.lang.String getContentType() {
    return contentType.toString();
  }

  public javax.media.protocol.PullSourceStream[] getStreams() {
    return pss;
  }

  static Method getMimeTypeMethod;

  static {
    try {
      Class<?> cl = null;
      // 03-Apr-2008: Perform a previous check of the class as resource in order to avoid
      // uncatchable exceptions in applets
      if (ByteDataSource.class.getResource("/com/sun/media/MimeManager.class") != null
          && (cl = Class.forName("com.sun.media.MimeManager")) != null) {
        getMimeTypeMethod = cl.getMethod("getMimeType", new Class[] {String.class});
      }
    } catch (Exception ex) {
      // no com.sun classes available!
    }
  }

  public javax.media.protocol.ContentDescriptor getContentDescriptor(String fName) {
    String mimeType = null;
    if (fName != null) {
      int p = fName.lastIndexOf('.') + 1;
      String ext = (p > 0 && p < fName.length() ? fName.substring(p) : fName).toLowerCase();
      // mimeType=com.sun.media.MimeManager.getMimeType(ext);
      if (getMimeTypeMethod != null) {
        try {
          mimeType = (String) getMimeTypeMethod.invoke(null, new Object[] {ext});
        } catch (Exception ex) {
          //
        }
      }
    }
    if (mimeType == null) {
      mimeType = "unknown";
    }
    // Todo: implement some standard mime types...
    return new javax.media.protocol.ContentDescriptor(
        javax.media.protocol.ContentDescriptor.mimeTypeToPackageName(mimeType));
  }

  class ByteSourceStream
      implements javax.media.protocol.PullSourceStream, javax.media.protocol.Seekable {

    protected ExtendedByteArrayInputStream inputStream;
    javax.media.protocol.ContentDescriptor contentType;

    public ByteSourceStream(
        ExtendedByteArrayInputStream in, javax.media.protocol.ContentDescriptor type)
        throws IOException {
      inputStream = in;
      contentType = type;
    }

    public ByteSourceStream duplicate() throws IOException {
      return new ByteSourceStream(inputStream.duplicate(), contentType);
    }

    public javax.media.protocol.ContentDescriptor getContentDescriptor() {
      return contentType;
    }

    public long getContentLength() {
      return inputStream.getCount();
    }

    public boolean willReadBlock() {
      return inputStream.eosReached();
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
      return inputStream.read(buffer, offset, length);
    }

    public void close() throws IOException {
      inputStream.close();
    }

    public boolean endOfStream() {
      return inputStream.eosReached();
    }

    public Object[] getControls() {
      return new Object[0];
    }

    public Object getControl(String controlName) {
      return null;
    }

    public boolean isRandomAccess() {
      return true;
    }

    public long seek(long param) {
      try {
        return inputStream.seek(param);
      } catch (Exception ex) {
        return 0L;
      }
    }

    public long tell() {
      return inputStream.getPos();
    }
  }
}
