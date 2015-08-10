/*
 * File    : ProjectFileUtils.java
 * Created : 10-aug-2015 09:00
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
package edu.xtec.jclic.project;

import edu.xtec.jclic.bags.ActivityBagElement;
import edu.xtec.jclic.bags.MediaBagElement;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.fileSystem.FileZip;
import edu.xtec.util.ResourceBridge;
import edu.xtec.util.StreamIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Miscellaneous utilities to process ".jclic.zip" files, normalizing media file
 * names, avoiding links to other "zip" files and extracting contents to a given
 * folder.
 *
 * @author fbusquet
 */
public class ProjectFileUtils implements ResourceBridge {

  FileZip zipFS;
  String zipName;
  String projectName;
  String newFileName;
  String[] entries;
  JClicProject project;

  public ProjectFileUtils(String fileName) throws Exception {

    zipFS = (FileZip) FileSystem.createFileSystem(fileName, this);

    entries = zipFS.getEntries(null);

    String[] projects = zipFS.getEntries(".jclic");
    if (projects == null) {
      throw new Exception("File " + fileName + " does not contain any jclic project");
    }
    projectName = projects[0];

    org.jdom.Document doc = zipFS.getXMLDocument(projectName);
    project = JClicProject.getJClicProject(doc.getRootElement(), this, zipFS, fileName);

    String s = zipFS.getZipName();
    newFileName = s.substring(0, s.lastIndexOf("."));

    System.out.println("Processing \"" + fileName + "\"");
  }

  public void clear() {

    if (project != null) {
      project.end();
      project = null;
    }

    if (zipFS != null) {
      zipFS.close();
      zipFS = null;
    }
  }

  @Override
  public void finalize() throws Throwable {
    clear();
    super.finalize();
  }

  /**
   * Normalizes the file names of the media bag, restricting it to URL-safe
   * characters.
   */
  public void normalizeFileNames() {

    HashSet<String> currentNames = new HashSet<String>();
    Iterator<MediaBagElement> it = project.mediaBag.getElements().iterator();
    while (it.hasNext()) {
      MediaBagElement mbe = it.next();
      if (!mbe.saveFlag) {
        System.out.println("Warning: File \"" + mbe.getFileName() + "\" is not part of \"" + zipFS.getFullRoot() + "\"");
      } else {
        String fn = mbe.getFileName();
        mbe.setMetaData(fn);
        String fnv = FileSystem.getValidFileName(fn);
        if (!fnv.equals(fn)) {
          String fn0 = fnv;
          int n = 0;
          while (currentNames.contains(fnv)) {
            fnv = Integer.toString(n++) + fn0;
          }
          System.out.println("Renaming \"" + fn + "\" as \"" + fnv + "\"");
          mbe.setFileName(fnv);
        }
        currentNames.add(fnv);
      }
    }
  }

  /**
   * Searchs for links to ".jclic.zip" files in ActiveBox and JumpInfo objects,
   * and redirects it to ".jclic" files
   */
  public void avoidZipLinks() {
    // Scan Activity elements
    for (ActivityBagElement ab : project.activityBag.getElements()) {
      avoidZipLinksInElement(ab.getData());
    };
  }

  /**
   *
   * Searchs for links to ".jclic.zip" files in the given JDOM element. This
   * method makes recursive calls on all the child elements of the provided
   * starting point.
   *
   * @param el - The org.jdom.Element to analyze
   */
  public void avoidZipLinksInElement(org.jdom.Element el) {
    String n = el.getName();

    if (("media".equals(n) && el.getAttribute("params") != null)
            || ("jump".equals(n) && el.getAttribute("project") != null)) {

      String attrName = "media".equals(n) ? "params" : "project";

      String p = el.getAttributeValue(attrName);
      if (p != null && p.endsWith(".jclic.zip")) {
        String pv = p.substring(0, p.length() - 4);
        System.out.println("Changing " + n + " link from \"" + p + "\" to \"" + pv + "\"");
        el.setAttribute(attrName, pv);
      }
    }
    Iterator it = el.getChildren().iterator();
    while (it.hasNext()) {
      avoidZipLinksInElement((org.jdom.Element) it.next());
    }
  }

  /**
   * Saves the JClic project and all its contents in plain format (not zipped)
   * into the specified path
   *
   * @param path - The path where the project will be saved
   * @throws Exception
   */
  public void saveTo(String path) throws Exception {

    File outPath = new File(path);

    // Check outPath exists and is writtable
    if (!outPath.exists()) {
      outPath.mkdirs();
    }

    if (!outPath.isDirectory() || !outPath.canWrite()) {
      throw new Exception("Unable to write to: \"" + path + "\"");
    }

    // Export media files
    Iterator<MediaBagElement> it = project.mediaBag.getElements().iterator();
    while (it.hasNext()) {
      MediaBagElement mbe = it.next();
      if (mbe.saveFlag) {

        String fn = mbe.getMetaData();
        if (fn == null) {
          fn = mbe.getFileName();
        }

        InputStream is = zipFS.getInputStream(fn);
        File outFile = new File(outPath, mbe.getFileName());
        FileOutputStream fos = new FileOutputStream(outFile);
        System.out.println("Extracting \"" + fn + "\" to \"" + outFile.getPath() + "\"");
        StreamIO.writeStreamTo(is, fos);
      }
    }

    // Save ".jclic" file
    File outFile = new File(outPath, newFileName);
    FileOutputStream fos = new FileOutputStream(outFile);
    System.out.println("Saving project to \"" + outFile.getPath() + "\"");
    project.saveDocument(fos);
    fos.close();

    System.out.println("Done processing \"" + zipFS.getFullRoot() + "\"");
  }

  public static void processFolder(String sourcePath, String destPath) throws Exception {

    File src = new File(sourcePath);

    if (!src.isDirectory() || !src.canRead()) {
      throw new Exception("Source file does not exist, or is not readable");
    }

    System.out.println("Cleaning and exporting jclic.zip files from \"" + sourcePath + "\" to \"" + destPath + "\"");

    File dest = new File(destPath);

    File[] jclicZipFiles = src.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".jclic.zip");
      }
    });

    for (File f : jclicZipFiles) {
       ProjectFileUtils prjFU = new ProjectFileUtils(f.getAbsolutePath());
       prjFU.normalizeFileNames();
       prjFU.avoidZipLinks();
       prjFU.saveTo(dest.getAbsolutePath());
    }

    // Process subdirectories
    File[] subDirs = src.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return new File(dir, name).isDirectory();
      }
    });

    for (File f : subDirs) {
      ProjectFileUtils.processFolder(
              new File(src, f.getName()).getAbsolutePath(),
              new File(dest, f.getName()).getAbsolutePath());
    }

  }

  // 
  //
  // Void implementation of "ResourceBridge" methods:
  //
  public java.io.InputStream getProgressInputStream(java.io.InputStream is, int expectedLength, String name) {
    return is;
  }

  public edu.xtec.util.Options getOptions() {
    return null;
  }

  public String getMsg(String key) {
    return key;
  }

  public javax.swing.JComponent getComponent() {
    return null;
  }

  public void displayUrl(String url, boolean inFrame) {
    throw new UnsupportedOperationException("Not supported");
  }
}
