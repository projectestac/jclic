/*
 * File    : ProjectFileUtils.java
 * Created : 10-aug-2015 09:00
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
package edu.xtec.jclic.project;

import edu.xtec.jclic.bags.ActivityBagElement;
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.bags.JumpInfo;
import edu.xtec.jclic.bags.MediaBagElement;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.fileSystem.FileZip;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ResourceBridge;
import edu.xtec.util.StreamIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.json.JSONObject;

/**
 * Miscellaneous utilities to process ".jclic", ".jclic.zip" and ".scorm.zip"
 * files, normalizing media file names, avoiding links to other "zip" files and
 * extracting contents to a given folder.
 *
 * @author fbusquet
 */
public class ProjectFileUtils implements ResourceBridge {

  FileSystem fileSystem;
  String fullFilePath;
  String projectName;
  String jclicFileName;
  String plainProjectName;
  String basePath;
  String relativePath;
  JClicProject project;
  public boolean isScorm;

  // Interruption flag
  public static boolean interrupt = false;

  // Offsets in "counters" array
  public static int NUM_ACTS = 0, NUM_MEDIA = 1, TOTAL_FILESIZE = 2;

  /**
   * Builds a ProjectFileUtils object, initializing a @link{JClicProject}
   *
   * @param fileName - Relative or absolute path to the ".jclic" or ".jclic.zip"
   *                 file to be processed
   * @param basePath - Base path of this project. Relative paths are based on this
   *                 one. When null, the parent folder of fileName will be used.
   * @throws Exception
   */
  public ProjectFileUtils(String fileName, String basePath) throws Exception {

    fullFilePath = new File(fileName).getCanonicalPath();
    isScorm = fullFilePath.endsWith(Utils.EXT_SCORM_ZIP);
    boolean isZip = isScorm || fullFilePath.endsWith(Utils.EXT_JCLIC_ZIP);
    if (!isZip && !fullFilePath.endsWith(".jclic")) {
      throw new Exception("File " + fileName + " is not a JClic project file!");
    }

    File fullFilePathFile = new File(fullFilePath);
    String fileBase = fullFilePathFile.getParent();
    this.basePath = (basePath == null ? fileBase : basePath);
    relativePath = this.basePath.equals(fileBase) ? "" : fileBase.substring(this.basePath.length() + 1);

    fileSystem = FileSystem.createFileSystem(fullFilePath, this);
    String file = fullFilePathFile.getName();
    JSONObject json = null;
    if (isZip && !isScorm) {
      file = ((FileZip) fileSystem).getZipName();
      jclicFileName = file.substring(0, file.lastIndexOf("."));

      String[] projects = ((FileZip) fileSystem).getEntries(".jclic");
      if (projects == null) {
        throw new Exception("File " + fullFilePath + " does not contain any jclic project");
      }
      projectName = projects[0];
    } else if (isScorm) {
      if (fileSystem.fileExists("project.json")) {
        json = new JSONObject(new String(fileSystem.getBytes("project.json")));
        projectName = json.optString("mainFile", null);
        jclicFileName = projectName;
      }
      if (projectName == null) {
        throw new Exception("Invalid JClic SCORM file: " + fullFilePath);
      }
    } else {
      jclicFileName = projectName = file;
    }

    plainProjectName = FileSystem.getValidFileName(projectName.substring(0, projectName.lastIndexOf(".")));

    org.jdom.Document doc = fileSystem.getXMLDocument(projectName);
    project = JClicProject.getJClicProject(doc.getRootElement(), this, fileSystem, file);
    if (json != null) {
      project.readJSON(json, false);
    }
  }

  /**
   * Normalizes the file names of the media bag, restricting it to URL-safe
   * characters.
   *
   * @param ps       - The @link{PrintStream} where progress messages will be
   *                 outputed. Can be null.
   * @param fileList - The list of currently exported files, used to avoid
   *                 duplicates
   * @throws java.lang.InterruptedException
   */
  public void normalizeFileNames(PrintStream ps, Collection<String> fileList) throws InterruptedException {

    HashSet<String> currentNames = new HashSet<String>();
    Iterator<MediaBagElement> it = project.mediaBag.getElements().iterator();
    while (it.hasNext()) {

      boolean renamed = false;

      if (interrupt) {
        interrupt = false;
        throw new InterruptedException();
      }

      MediaBagElement mbe = it.next();

      String fn = mbe.getFileName();
      mbe.setMetaData(fn);
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
        renamed = true;
      }

      // Avoid duplicate filenames
      if (fileList != null && fileList.contains(getRelativeFn(fnv))) {
        int p = fnv.lastIndexOf(".");
        fnv = fnv.substring(0, p) + "-" + plainProjectName + fnv.substring(p);
        renamed = true;
      }

      if (renamed && ps != null) {
        mbe.justSetFileName(fnv);
        ps.println("Renaming \"" + fn + "\" as \"" + fnv + "\"");
      }

      currentNames.add(fnv);
    }
  }

  /**
   * Searchs for links to ".jclic.zip" files in @link{ActiveBox} and
   *
   * @link{JumpInfo} objects, and redirects it to ".jclic" files
   * @param ps - The @link{PrintStream} where progress messages will be outputed.
   *           Can be null.
   * @throws java.lang.InterruptedException
   */
  public void avoidZipLinks(PrintStream ps) throws InterruptedException {
    // Scan Activity elements
    for (ActivityBagElement ab : project.activityBag.getElements()) {
      if (interrupt) {
        interrupt = false;
        throw new InterruptedException();
      }
      avoidZipLinksInElement(ab.getData(), ps);
    }

    for (ActivitySequenceElement ase : project.activitySequence.getElements()) {

      if (interrupt) {
        interrupt = false;
        throw new InterruptedException();
      }

      if (ase.fwdJump != null) {
        avoidZipLinksInJumpInfo(ase.fwdJump, ps);
        avoidZipLinksInJumpInfo(ase.fwdJump.upperJump, ps);
        avoidZipLinksInJumpInfo(ase.fwdJump.lowerJump, ps);
      }
      if (ase.backJump != null) {
        avoidZipLinksInJumpInfo(ase.backJump, ps);
        avoidZipLinksInJumpInfo(ase.backJump.upperJump, ps);
        avoidZipLinksInJumpInfo(ase.backJump.lowerJump, ps);
      }
    }
  }

  /**
   * Searchs for ".jclic.zip" links in JumpInfo elements, changing it to links to
   * plain ".jclic" files.
   *
   * @param ji - The JumpInfo to scan for links
   * @param ps - The @link{PrintStream} where progress messages will be outputed.
   *           Can be null.
   * @throws java.lang.InterruptedException
   */
  public void avoidZipLinksInJumpInfo(JumpInfo ji, PrintStream ps) throws InterruptedException {
    if (ji != null && ji.projectPath != null && ji.projectPath.endsWith(Utils.EXT_JCLIC_ZIP)) {
      String p = ji.projectPath;
      String pv = p.substring(0, p.length() - 4);
      ji.projectPath = pv;
      if (ps != null) {
        ps.println("Changing sequence link from \"" + p + "\" to \"" + pv + "\"");
      }
    }
  }

  /**
   * Searchs for links to ".jclic.zip" files in the given JDOM element. This
   * method makes recursive calls on all the child elements of the provided
   * starting point.
   *
   * @param el - The org.jdom.Element to scan for links
   * @param ps - The @link{PrintStream} where progress messages will be outputed.
   *           Can be null.
   * @throws java.lang.InterruptedException
   */
  public void avoidZipLinksInElement(org.jdom.Element el, PrintStream ps) throws InterruptedException {
    if (el.getAttribute("params") != null || (el.getName().equals("menuElement") && el.getAttribute("path") != null)) {
      String attr = el.getName().equals("menuElement") ? "path" : "params";
      String p = el.getAttributeValue(attr);
      if (p != null && p.endsWith(Utils.EXT_JCLIC_ZIP)) {
        String pv = p.substring(0, p.length() - 4);
        if (ps != null) {
          ps.println("Changing media link from \"" + p + "\" to \"" + pv + "\"");
        }
        el.setAttribute(attr, pv);
      }
    }
    Iterator it = el.getChildren().iterator();
    while (it.hasNext()) {

      if (interrupt) {
        interrupt = false;
        throw new InterruptedException();
      }

      avoidZipLinksInElement((org.jdom.Element) it.next(), ps);
    }
  }

  public String getRelativeFn(String fName) {
    return relativePath.length() > 0 ? relativePath + '/' + fName : fName;
  }

  /**
   * Saves the JClic project and all its contents in plain format (not zipped)
   * into the specified path
   *
   * @param path     - The path where the project will be saved
   * @param fileList - Dynamic list containing relative paths of all exported
   *                 files
   * @param ps       - The @link{PrintStream} where progress messages will be
   *                 outputed. Can be null.
   * @throws Exception
   * @throws           java.lang.InterruptedException
   */
  public void saveTo(String path, Collection<String> fileList, PrintStream ps, long[] counters)
      throws Exception, InterruptedException {

    counters[NUM_ACTS] += project.activityBag.size();
    counters[NUM_MEDIA] += project.mediaBag.getElements().size();

    File outPath = new File(path);
    path = outPath.getCanonicalPath();

    // Check outPath exists and is writtable
    if (!outPath.exists()) {
      outPath.mkdirs();
    }

    if (!outPath.isDirectory() || !outPath.canWrite()) {
      throw new Exception("Unable to write to: \"" + path + "\"");
    }

    // Export media fileList
    Iterator<MediaBagElement> it = project.mediaBag.getElements().iterator();
    while (it.hasNext()) {

      if (interrupt) {
        interrupt = false;
        throw new InterruptedException();
      }

      MediaBagElement mbe = it.next();
      String fn = mbe.getMetaData();
      if (fn == null) {
        fn = mbe.getFileName();
      }

      InputStream is = fileSystem.getInputStream(fn);
      File outFile = new File(outPath, mbe.getFileName());
      FileOutputStream fos = new FileOutputStream(outFile);
      if (ps != null) {
        ps.println("Extracting " + fn + " to " + outFile.getCanonicalPath());
      }
      counters[TOTAL_FILESIZE] += StreamIO.writeStreamTo(is, fos);

      if (fileList != null) {
        fileList.add(getRelativeFn(outFile.getName()));
      }
    }

    // Save ".jclic" file
    org.jdom.Document doc = project.getDocument();

    File outFile = new File(outPath, jclicFileName);
    FileOutputStream fos = new FileOutputStream(outFile);
    if (ps != null) {
      ps.println("Saving project to: " + outFile.getCanonicalPath());
    }
    JDomUtility.saveDocument(fos, doc);
    counters[TOTAL_FILESIZE] += fos.getChannel().size();
    fos.close();

    // Save ".jclic.js" file
    String jsFileName = jclicFileName + ".js";
    outFile = new File(outPath, jsFileName);
    fos = new FileOutputStream(outFile);
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
    if (ps != null) {
      ps.println("Saving project to: " + outFile.getCanonicalPath());
    }

    org.jdom.output.XMLOutputter xmlOutputter = new org.jdom.output.XMLOutputter();
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    xmlOutputter.output(doc, bas);
    JSONObject json = new JSONObject();
    json.put("xml", bas.toString("UTF-8"));

    String sequence = json.toString();
    sequence = sequence.substring(8, sequence.length() - 2);

    pw.println(
        "if(JClicObject){JClicObject.projectFiles[\"" + getRelativeFn(jclicFileName) + "\"]=\"" + sequence + "\";}");
    pw.flush();
    counters[TOTAL_FILESIZE] += fos.getChannel().size();
    pw.close();

    if (fileList != null) {
      fileList.add(getRelativeFn(jclicFileName));
      fileList.add(getRelativeFn(jsFileName));
    }

    if (ps != null) {
      ps.println("Done processing: " + fullFilePath);
    }
  }

  public static void processSingleFile(String sourceFile, String destPath, Collection<String> fileList, PrintStream ps,
      long[] counters) throws Exception, InterruptedException {
    processSingleFile(sourceFile, destPath, null, fileList, ps, counters);
  }

  public static void processSingleFile(String sourceFile, String destPath, String basePath, Collection<String> fileList,
      PrintStream ps, long[] counters) throws Exception, InterruptedException {
    ProjectFileUtils prjFU = new ProjectFileUtils(sourceFile, basePath);
    prjFU.normalizeFileNames(ps, fileList);
    prjFU.avoidZipLinks(ps);
    prjFU.saveTo(destPath, fileList, ps, counters);
  }

  public static void processRootFolder(String sourcePath, String destPath, Collection<String> fileList, PrintStream ps,
      long[] counters) throws Exception, InterruptedException {
    String basePath = (new File(sourcePath)).getCanonicalPath();
    processFolder(sourcePath, destPath, basePath, fileList, ps, counters);
  }

  public static void processFolder(String sourcePath, String destPath, String basePath, Collection<String> fileList,
      PrintStream ps, long[] counters) throws Exception, InterruptedException {

    File src = new File(sourcePath);
    Collection<String> thisFolderList = (fileList == null ? new ArrayList<String>() : fileList);

    if (!src.isDirectory() || !src.canRead()) {
      throw new Exception("Source directory \"" + sourcePath + "\" does not exist, not a directory or not readable");
    }

    if (ps != null) {
      ps.println("Writting all jclic.zip files in \"" + src.getCanonicalPath() + "\" to \"" + destPath + "\"");
    }

    File dest = new File(destPath);

    File[] jclicFiles = src.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        String lowerName = name.toLowerCase();
        return lowerName.endsWith(Utils.EXT_JCLIC_ZIP) || lowerName.endsWith(Utils.EXT_SCORM_ZIP)
            || lowerName.endsWith(Utils.EXT_JCLIC);
      }
    });

    for (File f : jclicFiles) {

      if (interrupt) {
        interrupt = false;
        throw new InterruptedException();
      }

      if (ps != null) {
        ps.println("\nProcessing file: " + f.getAbsolutePath());
      }

      processSingleFile(f.getAbsolutePath(), dest.getAbsolutePath(), basePath, thisFolderList, ps, counters);
    }

    // Force garbage collection
    jclicFiles = null;
    System.gc();

    // Process subdirectories
    File[] subDirs = src.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return new File(dir, name).isDirectory();
      }
    });

    for (File f : subDirs) {

      if (interrupt) {
        interrupt = false;
        throw new InterruptedException();
      }

      ProjectFileUtils.processFolder(new File(src, f.getName()).getCanonicalPath(),
          new File(dest, f.getName()).getCanonicalPath(), basePath, fileList, ps, counters);
    }

    // Force garbage collection
    subDirs = null;
    System.gc();
  }

  // Void implementation of "ResourceBridge" methods:
  @Override
  public java.io.InputStream getProgressInputStream(java.io.InputStream is, int expectedLength, String name) {
    return is;
  }

  @Override
  public edu.xtec.util.Options getOptions() {
    return null;
  }

  @Override
  public String getMsg(String key) {
    return key;
  }

  @Override
  public javax.swing.JComponent getComponent() {
    return null;
  }

  @Override
  public void displayUrl(String url, boolean inFrame) {
    throw new UnsupportedOperationException("Not supported");
  }
}
