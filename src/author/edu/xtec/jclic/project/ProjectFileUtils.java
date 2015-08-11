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
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.bags.JumpInfo;
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
  String zipFilePath;
  String zipFileName;
  String projectName;
  String jclicFileName;
  String[] entries;
  JClicProject project;
  
  
  /**
   * Builds a ProjectFileUtils object, initializing a @link{JClicProject}
   * @param fileName - Relative or absolute path to the ".jclic.zip" file to be processed
   * @throws Exception 
   */
  public ProjectFileUtils(String fileName) throws Exception {
    
    zipFilePath = new File(fileName).getCanonicalPath();
    if(!zipFilePath.endsWith(".jclic.zip"))
      throw new Exception("File "+fileName+" is not a jclic.zip file!");
    
    zipFS = (FileZip) FileSystem.createFileSystem(zipFilePath, this);
    zipFileName = zipFS.getZipName();
    jclicFileName = zipFileName.substring(0, zipFileName.lastIndexOf("."));

    entries = zipFS.getEntries(null);

    String[] projects = zipFS.getEntries(".jclic");
    if (projects == null) {
      throw new Exception("File " + zipFilePath + " does not contain any jclic project");
    }
    projectName = projects[0];

    org.jdom.Document doc = zipFS.getXMLDocument(projectName);
    project = JClicProject.getJClicProject(doc.getRootElement(), this, zipFS, zipFileName);

    System.out.println("\nProcessing file: " + zipFilePath);
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
        System.out.println("WARINIG: File \"" + mbe.getFileName() + "\" is not part of \"" + zipFilePath + "\"");
      } else {
        String fn = mbe.getFileName();
        mbe.setMetaData(fn);
        String fnv = FileSystem.getValidFileName(fn);
        // Avoid filenames starting with a dot
        if(fnv.charAt(0)=='.')
          fnv = "_" + fnv;          
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
   * Searchs for links to ".jclic.zip" files in @link{ActiveBox} and @link{JumpInfo} objects,
   * and redirects it to ".jclic" files
   */
  public void avoidZipLinks() {
    // Scan Activity elements
    for (ActivityBagElement ab : project.activityBag.getElements()) {
      avoidZipLinksInElement(ab.getData());
    }

    for (ActivitySequenceElement ase : project.activitySequence.getElements()) {
      if (ase.fwdJump != null) {
        avoidZipLinksInJumpInfo(ase.fwdJump);
        avoidZipLinksInJumpInfo(ase.fwdJump.upperJump);
        avoidZipLinksInJumpInfo(ase.fwdJump.lowerJump);
      }
      if (ase.backJump != null) {
        avoidZipLinksInJumpInfo(ase.backJump);
        avoidZipLinksInJumpInfo(ase.backJump.upperJump);
        avoidZipLinksInJumpInfo(ase.backJump.lowerJump);
      }
    }
  }
  
  
  /**
   * Searchs for ".jclic.zip" links in JumpInfo elements, changing it to links
   * to plain ".jclic" files.
   * @param ji - The JumpInfo to scan for links
   */
  public void avoidZipLinksInJumpInfo(JumpInfo ji) {
    if (ji != null && ji.projectPath != null && ji.projectPath.endsWith(".jclic.zip")) {
      String p = ji.projectPath;
      String pv = p.substring(0, p.length() - 4);
      ji.projectPath = pv;
      System.out.println("Changing sequence link from \"" + p + "\" to \"" + pv + "\"");
    }
  }

  /**
   *
   * Searchs for links to ".jclic.zip" files in the given JDOM element. This
   * method makes recursive calls on all the child elements of the provided
   * starting point.
   *
   * @param el - The org.jdom.Element to scan for links
   */
  public void avoidZipLinksInElement(org.jdom.Element el) {
    if (el.getAttribute("params") != null) {
      String p = el.getAttributeValue("params");
      if (p != null && p.endsWith(".jclic.zip")) {
        String pv = p.substring(0, p.length() - 4);
        System.out.println("Changing media link from \"" + p + "\" to \"" + pv + "\"");
        el.setAttribute("params", pv);
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
    path = outPath.getCanonicalPath();

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
        System.out.println("Extracting " + fn + " to " + outFile.getCanonicalPath());
        StreamIO.writeStreamTo(is, fos);
      }
    }

    // Save ".jclic" file
    File outFile = new File(outPath, jclicFileName);
    FileOutputStream fos = new FileOutputStream(outFile);
    System.out.println("Saving project to: " + outFile.getCanonicalPath());
    project.saveDocument(fos);
    fos.close();

    System.out.println("Done processing: " + zipFilePath);
  }

  public static void processFolder(String sourcePath, String destPath) throws Exception {

    File src = new File(sourcePath);

    if (!src.isDirectory() || !src.canRead()) {
      throw new Exception("Source directory \""+sourcePath+"\" does not exist, not a directory or not readable");
    }

    System.out.println("Exporting all jclic.zip files in \"" + src.getCanonicalPath() + "\" to \"" + destPath + "\"");

    File dest = new File(destPath);

    File[] jclicZipFiles = src.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".jclic.zip");
      }
    });

    for (File f : jclicZipFiles) {
      try {
        ProjectFileUtils prjFU = new ProjectFileUtils(f.getAbsolutePath());
        prjFU.normalizeFileNames();
        prjFU.avoidZipLinks();
        prjFU.saveTo(dest.getAbsolutePath());
      } catch (Exception ex) {
        System.out.println("ERROR: "+ex.getMessage());
      }
    }    
    jclicZipFiles = null;
    System.gc();

    // Process subdirectories
    File[] subDirs = src.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return new File(dir, name).isDirectory();
      }
    });

    for (File f : subDirs) {
      ProjectFileUtils.processFolder(
              new File(src, f.getName()).getCanonicalPath(),
              new File(dest, f.getName()).getCanonicalPath());
    }
    subDirs = null;
    System.gc();
  }

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
