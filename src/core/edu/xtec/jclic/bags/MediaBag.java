/*
 * File    : MediaBag.java
 * Created : 19-dec-2000 15:49
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
package edu.xtec.jclic.bags;

import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.skins.Skin;
import edu.xtec.util.Domable;
import edu.xtec.util.ExtendedByteArrayInputStream;
import edu.xtec.util.FontCheck;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.StreamIO;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class stores and manages all the media components (images, sounds,
 * animations, video, MIDI files, etc.) needed to run the activities of a
 * {@link edu.xtec.jclic.project.JClicProject}. The main member of the class is
 * a {@link java.util.ArrayList} that stores
 * {@link edu.xtec.jclic.bags.MediaBagElement} objects. It defines also a
 * {@link edu.xtec.jclic.bags.MediaBag.Listener} interface to allow other
 * objects to be informed about changes in the media collection.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class MediaBag extends Object implements Editable, Domable, StreamIO.InputStreamProvider {

  /**
   * The project this <CODE>MediaBag</CODE> belongs to
   */
  protected JClicProject project;
  /**
   * List containing all the {@link edu.xtec.jclic.bags.MediaBagElement} objects
   * of this <CODE>MediaBag</CODE>.
   */
  protected List<MediaBagElement> elements;
  protected Set<Listener> listeners;

  /**
   * Creates new MediaBag
   */
  public MediaBag(JClicProject project) {
    this.project = project;
    elements = new ArrayList<MediaBagElement>(30);
    listeners = new HashSet<Listener>(1);
  }

  public JClicProject getProject() {
    return project;
  }

  public static String ELEMENT_NAME = "mediaBag";

  /**
   * Provides a copy of the elements List
   *
   * @return a List (currently an ArrayList) of the elements
   */
  public List<MediaBagElement> getElements() {
    return new ArrayList<MediaBagElement>(elements);
  }

  public void clear() {
    elements.clear();
  }

  public List<MediaBagElement> getElementsByName() {
    List<MediaBagElement> v = getElements();
    Collections.sort(v, new Comparator<MediaBagElement>() {
      @Override
      public int compare(MediaBagElement o1, MediaBagElement o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
    return v;
  }

  public List<MediaBagElement> getElementsByType() {
    final StringBuilder sb1 = new StringBuilder(200);
    final StringBuilder sb2 = new StringBuilder(200);
    List<MediaBagElement> v = getElements();
    Collections.sort(v, new Comparator<MediaBagElement>() {
      @Override
      public int compare(MediaBagElement o1, MediaBagElement o2) {
        sb1.setLength(0);
        String fName = o1.getFileName();
        String name = o1.getName();
        int dot = fName.lastIndexOf('.');
        sb1.append(dot > 0 ? fName.substring(dot) : ".zzz");
        sb1.append(name);

        sb2.setLength(0);
        fName = o2.getFileName();
        name = o2.getName();
        dot = fName.lastIndexOf('.');
        sb2.append(dot > 0 ? fName.substring(dot) : ".zzz");
        sb2.append(name);

        return sb1.substring(0).compareToIgnoreCase(sb2.substring(0));
      }
    });
    return v;
  }

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    Iterator<MediaBagElement> it = getElementsByType().iterator();
    while (it.hasNext()) {
      e.addContent(it.next().getJDomElement());
    }
    return e;
  }

  public void clearData() {
    Iterator<MediaBagElement> it = elements.iterator();
    while (it.hasNext()) {
      it.next().clearData();
    }
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    Iterator it = e.getChildren(MediaBagElement.ELEMENT_NAME).iterator();
    while (it.hasNext()) {
      elements.add(MediaBagElement.getMediaBagElement((org.jdom.Element) it.next()));
    }
  }

  public boolean addElement(MediaBagElement mbe) {
    boolean result = mbe != null && (getElement(mbe.getName()) == null);
    if (result) {
      elements.add(mbe);
    }
    return result;
  }

  public MediaBagElement getElement(String name) {
    MediaBagElement result = null;
    if (name != null) {
      for (int i = 0; i < elements.size(); i++) {
        MediaBagElement mbe = elements.get(i);
        if (name.equals(mbe.getName())) {
          result = mbe;
          break;
        }
      }
    }
    return result;
  }

  public MediaBagElement getElementByFileName(String fileName) {
    MediaBagElement result = null;
    if (fileName != null) {
      for (int i = 0; i < elements.size(); i++) {
        MediaBagElement mbe = elements.get(i);
        if (fileName.equals(mbe.getFileName())) {
          result = mbe;
          break;
        }
      }
    }
    return result;
  }

  public MediaBagElement registerElement(String name, String fileName) {
    MediaBagElement result = getElement(name);
    if (result == null) {
      result = new MediaBagElement(FileSystem.stdFn(fileName == null ? name : fileName), null, name);
      elements.add(result);
    }
    return result;
  }

  public boolean removeElement(MediaBagElement mbe) {
    return elements.remove(mbe);
  }

  public Object getMediaDataSource(String name) throws Exception {
    Object result = null;
    String normalizedName = project.getFileSystem().getCanonicalNameOf(name);
    MediaBagElement mbe = registerElement(normalizedName, null);
    if (mbe != null) {
      if (mbe.getData() != null) {
        if (mbe.getData() instanceof ExtendedByteArrayInputStream) {
          result = ((ExtendedByteArrayInputStream) mbe.getData()).duplicate();
        } else {
          result = mbe.getData();
        }
      } else {
        result = project.getFileSystem().getMediaDataSource(mbe.getFileName());
        mbe.setData(result);
      }
    }
    return result;
  }

  @Override
  public InputStream getInputStream(String name) throws Exception {
    InputStream result = null;
    String normalizedName = project.getFileSystem().getCanonicalNameOf(name);
    MediaBagElement mbe = registerElement(normalizedName, null);
    if (mbe != null) {
      if (mbe.getData() instanceof ExtendedByteArrayInputStream) {
        ExtendedByteArrayInputStream ebais = (ExtendedByteArrayInputStream) mbe.getData();
        mbe.setData(ebais);
        result = (ebais).duplicate();
      } else {
        result = project.getFileSystem().getInputStream(mbe.getFileName());
        if (result instanceof ExtendedByteArrayInputStream) {
          mbe.setData(result);
        }
      }
    }
    return result;
  }

  public MediaBagElement getImageElement(String name) throws Exception {
    MediaBagElement result;
    String normalizedName = project.getFileSystem().getCanonicalNameOf(name);
    result = registerElement(normalizedName, null);
    if (result != null) {
      result = result.prepareImage(project.getFileSystem()) ? result : null;
    }
    return result;
  }

  public void buildFonts() {

    // count currently empty font elements
    Map<String, MediaBagElement> fonts = new HashMap<String, MediaBagElement>();
    Iterator<MediaBagElement> it = elements.iterator();
    while (it.hasNext()) {
      MediaBagElement mbe = it.next();
      String name = mbe.getName();
      if (name != null && mbe.getData() == null && mbe.getFileName().endsWith(".ttf")) {
        fonts.put(name, mbe);
      }
    }
    if (!fonts.isEmpty()) {
      String[] fontList = FontCheck.getFontList(false);
      int nFontsList = fontList.length;
      Iterator<String> itf = fonts.keySet().iterator();
      while (itf.hasNext()) {
        String name = itf.next();
        int i = 0;
        for (; i < nFontsList; i++) {
          if (fontList[i].equalsIgnoreCase(name)) {
            break;
          }
        }
        if (i == nFontsList) {
          MediaBagElement mbe = fonts.get(name);
          try {
            mbe.setData(FontCheck.buildNewFont(mbe.getFileName(), this, name));
          } catch (Exception ex) {
            System.err.println("Unable to create font:\n" + ex);
          }
        }
      }
    }
  }

  public Skin getSkinElement(String name, PlayStation ps) {
    if (name.startsWith(Skin.INTERNAL_SKIN_PREFIX)) {
      try {
        return Skin.getSkin(name, project.getFileSystem(), ps);
      } catch (Exception e) {
        System.err.println("Error loading skin \"" + name + "\":\n" + e);
      }
    } else {
      String normalizedName = project.getFileSystem().getCanonicalNameOf(name);
      MediaBagElement mbe = registerElement(normalizedName, null);
      if (mbe != null) {
        if (mbe.getData() == null || !(mbe.getData() instanceof Skin)) {
          try {
            Skin sk = Skin.getSkin(mbe.getFileName(), project.getFileSystem(), ps);
            mbe.setData(sk);
          } catch (Exception e) {
            System.err.println("Error loading skin \"" + mbe.getFileName() + "\":\n" + e);
          }
        }
        return (Skin) mbe.getData();
      }
    }
    return null;
  }

  public boolean isWaitingForImages() {
    Iterator<MediaBagElement> it = elements.iterator();
    while (it.hasNext()) {
      MediaBagElement mbe = it.next();
      if (mbe != null && mbe.isWaitingForImage()) {
        return true;
      }
    }
    return false;
  }

  public void waitForAllImages() {
    while (isWaitingForImages()) {
      try {
        Thread.sleep(100);
      } catch (Exception ex) {
        System.err.println("error waiting for images!\n" + ex);
        return;
      }
    }
  }

  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

  public interface Listener {

    public void listReferences(String type, Map<String, String> map);

    public void listReferencesTo(String name, String type, Map<String, String> map);
  }

  public void addListener(Listener lst) {
    if (!listeners.contains(lst)) {
      listeners.add(lst);
    }
  }

  public void removeListener(Listener lst) {
    listeners.remove(lst);
  }

  public void listReferencesTo(String name, String type, Map<String, String> map) {
    Iterator<Listener> it = listeners.iterator();
    while (it.hasNext()) {
      it.next().listReferencesTo(name, type, map);
    }
  }

  public void setNormalizedFileNames() {

    HashSet<String> currentNames = new HashSet<String>();
    Iterator<MediaBagElement> it = elements.iterator();
    while (it.hasNext()) {

      MediaBagElement mbe = it.next();
      String fn = mbe.getFileName();

      String fnv = FileSystem.getValidFileName(fn);
      // Avoid filenames starting with a dot
      if (fnv.charAt(0) == '.') {
        fnv = "_" + fnv;
      }
      if (!fnv.equals(fn)) {
        String fn0 = fnv;
        int n = 0;
        while (currentNames.contains(fnv)) {
          fnv = Integer.toString(n++) + fn0;
        }
        mbe.normalizedFileName = fnv;
      }
      currentNames.add(fnv);
    }
  }

}
