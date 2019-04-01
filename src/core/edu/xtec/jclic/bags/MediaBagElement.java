/*
 * File    : MediaBagElement.java
 * Created : 19-dec-2000 15:36
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
package edu.xtec.jclic.bags;

import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import javax.swing.ImageIcon;
import net.sf.image4j.codec.bmp.BMPDecoder;
import net.sf.image4j.codec.ico.ICODecoder;

/**
 * <CODE>MediaBagElements</CODE> are the members of
 * {@link edu.xtec.jclic.bags.MediaBag} objects. Media elements have a name, a
 * reference to a file (the <CODE>fileName</CODE>) and, when initialized, a
 * <CODE>data</CODE> field containing the raw content of the media. They have
 * also a flag indicating if the data must be saved into the
 * {@link edu.xtec.jclic.project.JClicProject} file or must be mantained as a
 * single reference to a external file.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class MediaBagElement extends Object implements Editable, Domable, Comparable {

  private String name;
  private String fileName;
  private int usageCount;
  private Object data;
  public boolean projectFlag;
  public boolean saveFlag;
  public boolean animated;
  private boolean hasThumb;
  private boolean isGif;

  // 10-Aug-2015
  // Added to allow renaming of media files
  private String metadata;
  public String normalizedFileName;

  /** Creates new MediaBagElement */
  public MediaBagElement(String fileName) {
    this(fileName, null);
  }

  public MediaBagElement(String fileName, Object data) {
    this(fileName, data, fileName);
  }

  public MediaBagElement(String fileName, Object data, String name) {
    setName(name);
    setFileName(fileName);
    setData(data);
    usageCount = 0;
    animated = false;
    saveFlag = true;
  }

  public static final String ELEMENT_NAME = "media";
  public static final String FILE = "file", NAME = "name", SAVE = "save", USAGE = "usage", ANIMATED = "animated";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(NAME, name);
    e.setAttribute(FILE, fileName);
    if (!saveFlag) {
      e.setAttribute(SAVE, JDomUtility.BOOL_STR[JDomUtility.FALSE]);
    }
    if (usageCount > 0) {
      e.setAttribute(USAGE, Integer.toString(usageCount));
    }
    if (isGif) {
      e.setAttribute(ANIMATED, JDomUtility.BOOL_STR[animated ? JDomUtility.TRUE : JDomUtility.FALSE]);
    }
    return e;
  }

  protected static MediaBagElement getMediaBagElement(org.jdom.Element e) throws Exception {
    MediaBagElement mb = new MediaBagElement("NONAME", null);
    mb.setProperties(e, null);
    return mb;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    setName(JDomUtility.getStringAttr(e, NAME, name, false));
    setFileName(JDomUtility.getStringAttr(e, FILE, fileName, false));
    saveFlag = JDomUtility.getBoolAttr(e, SAVE, true);
    usageCount = JDomUtility.getIntAttr(e, USAGE, usageCount);
    animated = JDomUtility.getBoolAttr(e, ANIMATED, false);
  }

  @Override
  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

  public boolean isEmpty() {
    return data == null;
  }

  public void setFileName(String sName) {
    fileName = FileSystem.stdFn(sName);
    data = null;
    animated = false;
    isGif = fileName.toLowerCase().endsWith(".gif");
  }

  public void justSetFileName(String sName) {
    fileName = FileSystem.stdFn(sName);
  }
  
  public String getFileName() {
    return fileName;
  }

  public void setMetaData(String meta) {
    metadata = meta;
  }

  public String getMetaData() {
    return metadata;
  }

  public boolean isImage() {
    return Utils.getFileType(fileName) == Utils.TYPE_IMAGE;
  }

  public void setData(Object sData) {
    data = sData;
    // CHANGED: 28/Apr/2016 - Don't clear animated flag!
    // animated=false;
    if (data != null && data instanceof Image) {
      Toolkit.getDefaultToolkit().prepareImage((Image) data, -1, -1, null);
    }
  }

  public void clearData() {
    if (data != null && !(data instanceof java.awt.Font)) {
      if (data instanceof Image) {
        ((Image) data).flush();
      }
      setData(null);
    }
  }

  public Object getData() {
    return data;
  }

  public boolean isInternal() {
    return (data != null);
  }

  public void incUsageCount() {
    usageCount++;
  }

  public void decUsageCount() {
    usageCount--;
  }

  public boolean isUsed() {
    return usageCount <= 0;
  }

  @Override
  protected void finalize() throws Throwable {
    Image img = getImage();
    if (img != null) {
      img.flush();
    }
    data = null;
    super.finalize();
  }

  public boolean prepareImage(FileSystem fs) throws Exception {
    boolean result = false;
    if (isImage()) {
      if (data == null || !(data instanceof Image)) {
        String fn = fileName.toLowerCase();
        if (fn.endsWith(".bmp")) {
          BufferedImage img = BMPDecoder.read(fs.getInputStream(fileName));
          setData(Toolkit.getDefaultToolkit().createImage(img.getSource()));
        } else if (fn.endsWith(".ico")) {
          java.util.List list = ICODecoder.read(fs.getInputStream(fileName));
          int maxs = 0;
          BufferedImage img = null;
          int listSize = list.size();
          for (int i = 0; i < listSize; i++) {
            BufferedImage imgtmp = (BufferedImage) list.get(i);
            int s = imgtmp.getWidth() * imgtmp.getHeight();
            if (s >= maxs) {
              img = imgtmp;
              maxs = s;
            }
          }
          if (img != null) {
            setData(Toolkit.getDefaultToolkit().createImage(img.getSource()));
          }
        } else {
          setData(fs.getImageFile(fileName));
        }
      }
      result = true;
    }
    return result;
  }

  private static int imgReadyFlag = (ImageObserver.WIDTH | ImageObserver.HEIGHT);

  public Image getImage() throws Exception {
    int imgStatus = 0;
    if (data == null || !(data instanceof Image)) {
      return null;
    }
    if (!animated) {
      while (true) {
        imgStatus = Toolkit.getDefaultToolkit().checkImage((Image) data, -1, -1, null);
        if ((imgStatus & (ImageObserver.ERROR | ImageObserver.ABORT)) != 0) {
          System.err.println("Error loading " + getName() + " - Toolkit.checkImage returned status: " + imgStatus);
          data = null;
          break;
        } else if ((imgStatus & imgReadyFlag) == imgReadyFlag) {
          break;
        }
        Thread.sleep(50);
      }
    }

    if (data != null && (imgStatus & ImageObserver.FRAMEBITS) != 0) {
      animated = true;
    }

    return (Image) data;
  }

  public Image prepareAndGetImage(FileSystem fs) throws Exception {
    Image img = getImage();
    if (img == null && data == null) {
      if (prepareImage(fs)) {
        img = getImage();
      }
    }
    return img;
  }

  public long getFileSize(FileSystem fs) {
    long result = 0;
    if (fileName != null) {
      try {
        result = fs.getFileLength(fileName);
      } catch (IOException ex) {
        System.err.println("Error recovering the file size of \"" + fileName + "\": " + ex.getMessage());
      }
    }
    return result;
  }

  public ImageIcon getThumbNail(int maxWidth, int maxHeight, FileSystem fs) {
    ImageIcon result = null;
    try {
      Image img = prepareAndGetImage(fs);
      if (img == null) {
        img = Utils.getFileIcon(fileName).getImage();
      }
      if (img != null && maxWidth > 0 && maxHeight > 0) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w > 0 && h > 0 && (w > maxWidth || h > maxHeight)) {
          double f = Math.min((double) maxWidth / w, (double) maxHeight / h);
          img = img.getScaledInstance((int) (f * w), (int) (f * h), Image.SCALE_SMOOTH);
        }
        result = new ImageIcon(img);
        hasThumb = true;
      }
    } catch (Exception ex) {
      System.err.println("Error reading image:\n" + ex);
    }
    return result;
  }

  public boolean isWaitingForImage() {
    if (hasThumb == true || animated == true || data == null || !(data instanceof Image)) {
      return false;
    }
    int state = Toolkit.getDefaultToolkit().checkImage((Image) data, -1, -1, null);
    if ((state & ImageObserver.FRAMEBITS) != 0) {
      animated = true;
    }
    boolean result = (state & (ImageObserver.ALLBITS | ImageObserver.FRAMEBITS)) == 0;
    return result;
  }

  /**
   * Getter for property name.
   *
   * @return Value of property name.
   */
  public java.lang.String getName() {
    return name;
  }

  /**
   * Setter for property name.
   *
   * @param name New value of property name.
   */
  public void setName(String name) {
    this.name = FileSystem.stdFn(name);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int compareTo(Object obj) {
    int result = -1;
    if (getName() != null && obj instanceof MediaBagElement) {
      result = getName().compareTo(((MediaBagElement) obj).getName());
    }
    return result;
  }
}
