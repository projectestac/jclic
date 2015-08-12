/*
 * File    : FileSystem.java
 * Created : 20-jun-2000 9:45
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

import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.*;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import java.util.List;
import javax.swing.JFileChooser;

/**
 * Base class for Clic filesystems.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class FileSystem extends Object {

  public static final String FS = "/", WINFS = "\\";
  public static final char FSCH = '/', WINFSCH = '\\';
  protected static FileChooserForFiles fileChooser;
  //private static final String SEP_FILE_BAK=File.separator+"..";
  private static final String FS_BAK = FS + "..";
  public static java.util.HashMap<String, String> altFileNames = new java.util.HashMap<String, String>();

  public String root;
  protected boolean isURL = false;
  protected Boolean ISURL = isURL;
  protected ResourceBridge rb = null;

  public FileSystem(ResourceBridge rb) {
    root = "";
    this.rb = rb;
  }

  public FileSystem(String rootPath, ResourceBridge rb) {
    root = stdFn(rootPath);
    this.rb = rb;
    if (root == null) {
      root = "";
    }
    if (root.length() > 0) {
      if (isStrUrl(root)) {
        isURL = true;
        ISURL = isURL;
        //if(root.indexOf('\\')>=0)
        //    root=root.replace('\\', '/');
        //if(!root.endsWith("/")) root = root + "/";
        if (!root.endsWith(FS)) {
          root = root + FS;
        }
        root = getCanonicalNameOf(root);
      } else {
        File f = new File(sysFn(root));
        String saveRoot = root;
        try {
          root = stdFn(f.getCanonicalPath());
        } catch (Exception e) {
          root = saveRoot;
        }
        //if(!root.endsWith(File.separator))
        //    root=root+File.separator;
        if (!root.endsWith(FS)) {
          root = root + FS;
        }
      }
    }
  }

  public FileSystem duplicate() throws Exception {
    return createFileSystem(root, rb);
  }

  protected void changeBase(String newRoot, String newFileName) throws Exception {
    File f = new File(sysFn(newRoot));
    String saveRoot = root;
    try {
      root = stdFn(f.getCanonicalPath());
    } catch (Exception e) {
      root = saveRoot;
    }
    if (!root.endsWith(FS)) {
      root = root + FS;
    }
  }

  public static String stdFn(String s) {
    return s == null ? s : s.replace(WINFSCH, FSCH);
  }

  public static String sysFn(String s) {
    String result = s;
    if (result != null) {
      result = stdFn(result).replace(FSCH, File.separatorChar);
      if (result.indexOf("%20") >= 0) {
        result = StrUtils.replace(result, "%20", " ");
      }
    }
    return result;
  }

  public static FileSystem createFileSystem(String rootPath, String fileName, ResourceBridge rb) throws Exception {
    if (fileName == null) {
      return new FileSystem(rootPath, rb);
    } else if (fileName.endsWith(".pcc")) {
      return PCCFileSystem.createPCCFileSystem(rootPath, fileName, rb);
    } else if (fileName.endsWith(".zip")) {
      return ZipFileSystem.createZipFileSystem(rootPath, fileName, rb);
    } else {
      throw new Exception("unknown format " + fileName);
    }
  }

  public static FileSystem createFileSystem(String fullPath, ResourceBridge rb) throws Exception {
    fullPath = getCanonicalNameOf(fullPath, null);
    String fileName = null;
    //String rootPath=getPathOf(fullPath);
    String rootPath = getPathPartOf(fullPath);
    if (fullPath.endsWith(".pcc") || fullPath.endsWith(".zip")) {
      fileName = getFileNameOf(fullPath);
    }
    return createFileSystem(rootPath, fileName, rb);
  }

  public String getFullFileNamePath(String fName) {
    if (fName == null || fName.length() == 0) {
      return root.length() > 0 ? root.substring(0, root.length() - 1) : root;
    }
    String result = getCanonicalNameOf(fName);
    if (!isURL) {
      File f = new File(sysFn(result));
      if (!f.isAbsolute()) {
        result = getCanonicalNameOf(root + result);
      }
    } else {
      if (!isStrUrl(result)) {
        result = getCanonicalNameOf(root + result);
      }
    }
    return result;
  }

  public String getRelativeFileNamePath(String fName) {
    String s = stdFn(fName);
    if (s == null
            || s.length() < root.length()
            || !s.substring(0, root.length()).equalsIgnoreCase(root)) {
      return s;
    } else {
      return s.substring(root.length());
    }
  }

  public String getFullRoot() {
    return root;
  }

  public boolean isUrlBased() {
    return isURL;
  }

  public static boolean isStrUrl(String s) {
    return s != null && (s.startsWith("http:") || s.startsWith("https:") || s.startsWith("ftp:") || s.startsWith("mailto:"));
  }

  public String getUrl(String fileName) {
    String s = stdFn(fileName);
    if (s == null || isStrUrl(s) || s.startsWith("file:")) {
      return s;
    }

    if (!(s.charAt(1) == ':') && !s.startsWith(FS)) {
      s = getFullFileNamePath(s);
    }

    if (isURL) {
      return getCanonicalNameOf(s, ISURL);
    } else {
      return "file://" + sysFn(getCanonicalNameOf(s, ISURL));
    }
  }

  public String getCanonicalNameOf(String fileName) {
    return getCanonicalNameOf(fileName, ISURL);
  }

  public static String getCanonicalNameOf(String fileName, Boolean isUrl) {

    String fn = stdFn(fileName);
    boolean flagUrl = (isUrl != null ? isUrl.booleanValue() : isStrUrl(fn));

    //char sep= flagUrl ? '/' : File.separatorChar;
    //String sepStr= flagUrl ? "/" : File.separator;
    //String fn=normalizePath(fileName, sep);
    String prefix = "";
    int cut = -1;

    if (fn.startsWith("file:")) {
      //fileName=fileName.substring(5);            
      fn = fn.substring(5);
    }

    if (isStrUrl(fn)) {
      int k = fn.indexOf('@');
      if (k < 0) {
        k = 7;
      }
      cut = fn.indexOf(FSCH, k);
      //cut=fn.indexOf(sep, k);
    } else if (fn.length() > 2 && fn.charAt(1) == ':') {
      cut = (fn.charAt(2) == FSCH ? 2 : 1);
    } //else if(fileName.startsWith("\\\\")){
    else if (fn.startsWith("//")) {
      int i = fn.indexOf(FSCH, 2);
      cut = fn.indexOf(FSCH, i + 1);
    } else if (fn.startsWith(FS)) {
      cut = 0;
    }

    if (cut >= 0) {
      prefix = fn.substring(0, cut + 1);
      fn = fn.substring(cut + 1);
    }

    // interceptar '\...'
    //String back= flagUrl ? "/.." : SEP_FILE_BAK;
    int r;
    while ((r = fn.indexOf(FS_BAK)) >= 0) {
      int p;
      for (p = r - 1; p >= 0; p--) {
        if (fn.charAt(p) == FSCH) {
          break;
        }
      }
      StringBuilder newfn = new StringBuilder();
      if (p >= 0) {
        newfn.append(fn.substring(0, p + 1));
      }
      if (r + 4 < fn.length()) {
        newfn.append(fn.substring(r + 4));
      }
      fn = newfn.substring(0);
    }

    return prefix + fn;
  }

  public static String getPathPartOf(String fullPath) {
    String s = stdFn(fullPath);
    int i = s.lastIndexOf(FS);
    //if((i=fullPath.lastIndexOf('\\'))<0) i=fullPath.lastIndexOf('/');
    return i < 0 ? "" : s.substring(0, i + 1);
  }

  public static String getFileNameOf(String fullPath) {
    String s = stdFn(fullPath);
    int i = s.lastIndexOf(FS);
    //if((i=fullPath.lastIndexOf('\\'))<0)
    //    i=fullPath.lastIndexOf('/');
    return i < 0 ? s : s.substring(i + 1);
  }

  /*
   public static String normalizePath(String path){
   return normalizePath(path, File.separatorChar);
   }
    
   public static String normalizePathToURL(String path){
   return normalizePath(path, '/');
   }
   */
  /*
   public static String normalizePath(String path, char newSeparator){
   if(path==null) return null;
   String result=new String(path);
   if(newSeparator!='\\') result=result.replace('\\', newSeparator);
   if(newSeparator!='/') result=result.replace('/', newSeparator);
   return result;
   }
   */
  public byte[] getBytes(String fileName) throws IOException {
    return StreamIO.readInputStream(getInputStream(fileName));
  }

  public Image getImageFile(String fName) throws Exception {
    return Toolkit.getDefaultToolkit().createImage(getBytes(fName));
  }

  public long getFileLength(String fName) throws IOException {
    long length;
    if (isURL) {
      // Updated 04-Aug-2014 to solve GitHub issue #5
      // https://github.com/projectestac/jclic/issues/5
      URL url = new URL(getFullFileNamePath(fName).replace(" ", "%20"));
      URLConnection c = url.openConnection();
      length = c.getContentLength();
    } else {
      File f = new File(sysFn(getFullFileNamePath(fName)));
      length = f.length();
    }
    return length;
  }

  public boolean fileExists(String fName) {
    boolean result;
    try {
      if (isURL) {
        // Updated 04-Aug-2014 to solve GitHub issue #5
        // https://github.com/projectestac/jclic/issues/5
        URL url = new URL(getFullFileNamePath(fName).replace(" ", "%20"));
        URLConnection c = url.openConnection();
        result = (c.getContentLength() > 0);
      } else {
        File f = new File(sysFn(getFullFileNamePath(fName)));
        result = f.exists();
      }
    } catch (Exception ex) {
      // eat exception
      result = false;
    }
    return result;
  }

  public InputStream getInputStream(String fName) throws IOException {
    InputStream result;
    int length;
    if (isURL) {
      // Updated 04-Aug-2014 to solve GitHub issue #5
      // https://github.com/projectestac/jclic/issues/5
      URL url = new URL(getFullFileNamePath(fName).replace(" ", "%20"));
      URLConnection c = url.openConnection();
      length = c.getContentLength();
      result = c.getInputStream();
    } else {
      //File f=new File(getCanonicalNameOf(root + fName));
      File f = new File(sysFn(getFullFileNamePath(fName)));
      if (!f.exists()) {
        String alt = (String) altFileNames.get(fName);
        if (alt != null) {
          f = new File(sysFn(getFullFileNamePath(alt)));
        }
      }
      length = (int) f.length();
      result = new FileInputStream(f);
    }

    if (result != null && rb != null) {
      result = rb.getProgressInputStream(result, length, fName);
    }

    return result;
  }

  public Object getMediaDataSource(String fName) throws Exception {
    if (isURL) {
      return getExtendedByteArrayInputStream(fName);
    }

    return new StringBuilder("file:").append(getFullFileNamePath(fName)).substring(0);
  }

  public edu.xtec.util.ExtendedByteArrayInputStream getExtendedByteArrayInputStream(String fName) throws Exception {
    return new edu.xtec.util.ExtendedByteArrayInputStream(getBytes(fName), fName);
  }

  public static org.jdom.Document getXMLDocument(InputStream is) throws Exception {
    org.jdom.Document doc = JDomUtility.getSAXBuilder().build(is);
    edu.xtec.util.JDomUtility.clearNewLineElements(doc.getRootElement());
    return doc;
  }

  public org.jdom.Document getXMLDocument(String fName) throws Exception {
    org.jdom.Document doc = buildDoc(fName, JDomUtility.getSAXBuilder());
    edu.xtec.util.JDomUtility.clearNewLineElements(doc.getRootElement());
    return doc;
  }

  protected org.jdom.Document buildDoc(String fName, org.jdom.input.SAXBuilder builder) throws Exception {
    return builder.build(getInputStream(fName));
  }

  public void close() {
    // nothing to do
  }

  protected void open() throws Exception {
    // nothing to do
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      close();
    } finally {
      super.finalize();
    }
  }

  public static FileChooserForFiles getFileChooser(String root) {
    if (fileChooser == null) {
      fileChooser = new FileChooserForFiles();
      if (root != null) {
        fileChooser.setCurrentDirectory(new File(sysFn(root)));
      }
    }
    return fileChooser;
  }

  /*
   public String chooseFile(String defaultValue, boolean save, int[] filters, edu.xtec.util.Options options, String titleKey, java.awt.Component dlgOwner, boolean proposeMove){
   String result=null;
   FileChooserForFiles chooser;
   if(options!=null){
   Messages msg=options.getMessages();
   if(isURL){
   if(save){
   msg.showErrorWarning(dlgOwner, "filesystem_saveURLerror", null);
   }
   else{
   result=msg.showInputDlg(dlgOwner, "filesystem_enterURL", "URL", "http://", titleKey!=null ? titleKey : "filesystem_openURL", false);
   }
   }
   else if((chooser=getFileChooser())!=null){
   chooser.setCurrentDirectory(new File(sysFn(root)));
   chooser.setDialogType(save ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);                
   chooser.setDialogTitle(msg.get(titleKey!=null ? titleKey : save ? "FILE_SAVE" : "FILE_OPEN"));
   chooser.resetChoosableFileFilters();
   if(filters!=null){
   chooser.setAcceptAllFileFilterUsed(false);
   for(int i=0; i<filters.length; i++){
   if(i==filters.length-1)
   chooser.setFileFilter(Utils.getFileFilter(filters[i], msg));
   else
   chooser.addChoosableFileFilter(Utils.getFileFilter(filters[i], msg));
   }
   }
   else
   chooser.setAcceptAllFileFilterUsed(true);
                
   String s=StrUtils.nullableString(defaultValue);
   boolean dummyFile=false;
   if(s==null){
   s=".";
   dummyFile=true;
   }   
   chooser.directSetSelectedFile(new File(sysFn(getFullFileNamePath(s))));
   if(dummyFile)
   chooser.directSetSelectedFile(null);
                
   int retVal;
   boolean done=false;
   while(!done){
   if(save)
   retVal = chooser.showSaveDialog(dlgOwner);
   else
   retVal = chooser.showOpenDialog(dlgOwner);
                    
   if(retVal == JFileChooser.APPROVE_OPTION) {
   File f=chooser.getSelectedFile().getAbsoluteFile();
   result=getRelativeFileNamePath(stdFn(f.getAbsolutePath()));
   if(save){                            
   FileFilter filter=chooser.getFileFilter();
   if(filter instanceof SimpleFileFilter){
   f=((SimpleFileFilter)filter).checkFileExtension(f);
   result=getRelativeFileNamePath(stdFn(f.getAbsolutePath()));
   }
   done=msg.confirmOverwriteFile(dlgOwner, f);
   }
   else{
   done=msg.confirmReadableFile(dlgOwner, f);
   if(done 
   && proposeMove 
   && root.length()>0 
   && result.indexOf(FS)>=0
   && msg.showQuestionDlgObj(dlgOwner, new String[]
   { msg.get("filesystem_copyToRoot_1")+" "+result,
   msg.get("filesystem_copyToRoot_2"),
   msg.get("filesystem_copyToRoot_3"),
   msg.get("filesystem_copyToRoot_4"),
   }, "CONFIRM")){
   String name=stdFn(f.getName());
   File destFile=new File(sysFn(getFullFileNamePath(name)));
   if(msg.confirmOverwriteFile(dlgOwner, destFile)){
   try{
   OutputStream os=createSecureFileOutputStream(name); 
   InputStream is=getInputStream(result);
   if(StreamIO.writeStreamDlg(is, os, (int)f.length(), msg.get("filesystem_copyFile"), dlgOwner, options))
   result=name;
   else
   if(destFile.exists())
   destFile.delete();
   } catch(Exception ex){
   msg.showErrorWarning(dlgOwner, "ERROR", ex);
   }
   }
   }
   }
   }
   else{
   result=null;
   done=true;
   }
   }
   }
   }
   return result;
   }
   */
  public String chooseFile(String defaultValue, boolean save, int[] filters, Options options, String titleKey, Component dlgOwner, boolean proposeMove) {
    String result = null;
    String[] files = chooseFiles(defaultValue, save, filters, options, titleKey, dlgOwner, proposeMove, false);
    if (files != null && files.length > 0) {
      result = files[0];
    }
    return result;
  }

  public String[] chooseFiles(String defaultValue, boolean save, int[] filters, Options options, String titleKey, Component dlgOwner, boolean proposeMove, boolean multiSelection) {
    String[] result = null;
    FileChooserForFiles chooser;
    if (save) {
      multiSelection = false;
    }
    if (options != null) {
      Messages msg = options.getMessages();
      if (isURL) {
        if (save) {
          msg.showErrorWarning(dlgOwner, "filesystem_saveURLerror", null);
        } else {
          String s = msg.showInputDlg(dlgOwner, "filesystem_enterURL", "URL", "http://", titleKey != null ? titleKey : "filesystem_openURL", false);
          if (s != null) {
            result = new String[]{s};
          }
        }
      } else if ((chooser = getFileChooser(root)) != null) {
        chooser.setApproveButtonToolTipText(msg.get(save ? "FILE_SAVE_TOOLTIP" : "FILE_OPEN_TOOLTIP"));
        chooser.setDialogType(save ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
        chooser.setApproveButtonText(msg.get(save ? "SAVE" : "OPEN"));
        chooser.setMultiSelectionEnabled(multiSelection);
        //chooser.setCurrentDirectory(new File(sysFn(root)));
        chooser.setDialogTitle(msg.get(titleKey != null ? titleKey : save ? "FILE_SAVE" : "FILE_OPEN"));
        chooser.resetChoosableFileFilters();
        if (filters != null) {
          chooser.setAcceptAllFileFilterUsed(false);
          for (int i = 0; i < filters.length; i++) {
            if (i == filters.length - 1) {
              chooser.setFileFilter(Utils.getFileFilter(filters[i], msg));
            } else {
              chooser.addChoosableFileFilter(Utils.getFileFilter(filters[i], msg));
            }
          }
        } else {
          chooser.setAcceptAllFileFilterUsed(true);
        }

        String s = StrUtils.nullableString(defaultValue);
        boolean dummyFile = false;
        if (s == null) {
          s = ".";
          dummyFile = true;
        }
        chooser.directSetSelectedFile(new File(sysFn(getFullFileNamePath(s))));
        if (dummyFile) {
          chooser.directSetSelectedFile(null);
        }

        int retVal;
        boolean done = false;
        while (!done) {
          if (save) {
            retVal = chooser.showSaveDialog(dlgOwner);
          } else {
            retVal = chooser.showOpenDialog(dlgOwner);
          }

          if (retVal == JFileChooser.APPROVE_OPTION) {
            File[] files = multiSelection
                    ? chooser.getSelectedFiles()
                    : new File[]{chooser.getSelectedFile()};
            result = new String[files.length];
            for (int i = 0; i < files.length; i++) {
              File f = files[i].getAbsoluteFile();
              result[i] = getRelativeFileNamePath(stdFn(f.getAbsolutePath()));
              if (save) {
                javax.swing.filechooser.FileFilter filter = chooser.getFileFilter();
                if (filter instanceof SimpleFileFilter) {
                  f = ((SimpleFileFilter) filter).checkFileExtension(f);
                  result[i] = getRelativeFileNamePath(stdFn(f.getAbsolutePath()));
                }
                done = (msg.confirmOverwriteFile(dlgOwner, f, "yn") == Messages.YES);
              } else {
                done = msg.confirmReadableFile(dlgOwner, f);
                if (done
                        && proposeMove
                        && root.length() > 0
                        && result[i].indexOf(FS) >= 0
                        && msg.showQuestionDlgObj(dlgOwner, new String[]{msg.get("filesystem_copyToRoot_1") + " " + result[i],
                          msg.get("filesystem_copyToRoot_2"),
                          msg.get("filesystem_copyToRoot_3"),
                          msg.get("filesystem_copyToRoot_4"),}, "CONFIRM", "yn") == Messages.YES) {
                  String name = stdFn(f.getName());
                  File destFile = new File(sysFn(getFullFileNamePath(name)));
                  if (msg.confirmOverwriteFile(dlgOwner, destFile, "yn") == Messages.YES) {
                    try {
                      OutputStream os = createSecureFileOutputStream(name);
                      InputStream is = getInputStream(result[i]);
                      if (StreamIO.writeStreamDlg(is, os, (int) f.length(), msg.get("filesystem_copyFile"), dlgOwner, options)) {
                        result[i] = name;
                      } else if (destFile.exists()) {
                        destFile.delete();
                      }
                    } catch (Exception ex) {
                      msg.showErrorWarning(dlgOwner, "ERROR", ex);
                    }
                  }
                }
              }
              if (!done) {
                break;
              }
            }
          } else {
            result = null;
            done = true;
          }
        }
      }
    }
    return result;
  }

  class SecureFileOutputStream extends FileOutputStream {

    boolean closed;
    File tempFile;
    File destFile;

    /**
     * Creates new SecureFileOutputStream
     */
    private SecureFileOutputStream(File tempFile, File destFile) throws FileNotFoundException {
      super(tempFile);
      this.tempFile = tempFile;
      this.destFile = destFile;
      closed = false;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (!closed) {
        closed = true;
        if (destFile != null) {
          boolean isCurrentFs = getFullRoot().equals(stdFn(destFile.getAbsolutePath()));
          if (isCurrentFs) {
            FileSystem.this.close();
          }
          if (destFile.exists()) {
            destFile.delete();
          }
          boolean renamed = tempFile.renameTo(destFile);
          if (!renamed) {
            System.err.println("WARNING: Unable to rename " + tempFile + " to " + destFile.getName());
          }
          if (isCurrentFs) {
            try {
              if (!renamed) {
                // Try to continue using the temp file as base
                changeBase(tempFile.getParent(), tempFile.getName());
              }
              FileSystem.this.open();
            } catch (Exception ex) {
              throw new IOException(ex.getMessage());
            }
          }
        }
      }
    }
  }

  public FileOutputStream createSecureFileOutputStream(String fileName) throws IOException {
    FileOutputStream result;
    File file = new File(sysFn(getFullFileNamePath(fileName)));

    // 27-Sept-2006: Create parent directories if they don't exists
    file.getParentFile().mkdirs();
        // ---------

    // Fatal errrors detected probably caused by bad info on file.exists
    // Try to use SecureFileOutputStream always
    // 
    //if(file.exists()){
    File tmp = File.createTempFile("tmp", ".tmp", file.getParentFile());
    result = new SecureFileOutputStream(tmp, file);
    //} else{
    //    result=new FileOutputStream(file);
    //}
    return result;
  }

  // 16-Mar-2015: Added maxRecursion and maxFiles to prevent hangs in large file systems
  public static void exploreFiles(String prefix, File f, List<String> v, char pathSep, FileFilter filter, int maxRecursion, int maxFiles) {
    File[] files = filter == null ? f.listFiles() : f.listFiles(filter);
    StringBuilder sb = new StringBuilder();
    for (File file : files) {
      sb.setLength(0);
      if (file.isDirectory()) {
        if (maxRecursion != 0) {
          if (prefix != null) {
            sb.append(prefix);
          }
          sb.append(file.getName()).append(pathSep);
          exploreFiles(sb.substring(0), file, v, pathSep, filter, maxRecursion - 1, maxFiles);
        }
      } else {
        if (prefix != null) {
          sb.append(prefix);
        }
        v.add(sb.append(file.getName()).substring(0));
      }
      if (maxFiles > 0 && v.size() >= maxFiles)
        break;
    }
  }

  // Modified 10-Aug-2015
  // Allow only plain ASCII characters, dot, numbers and underscore
  // private static final String FNAME_VALID_CHARS = "_!~0123456789abcdefghijklmnopqrstuvwxyz";
  private static final String FNAME_VALID_CHARS = "_.0123456789abcdefghijklmnopqrstuvwxyz";
  
  // Modified 26-jul0-2006
  // Scope of character conversion limited to the basic ANSI (Latin1) set
  public static final String FNAME_CONVERTIBLE_CHARS = "\u00e1\u00e0\u00e4\u00e2\u00e3\u00e9\u00e8\u00eb\u00ea\u00ed\u00ec\u00ef\u00ee\u00f3\u00f2\u00f6\u00f4\u00f5\u00fa\u00f9\u00fc\u00fb\u00f1\u00e7\u20ac\u00ba\u00aa\u00e5\u00e6\u00f8\u00fd\u00fe\u00ff\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba\u03bb\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c2\u03c3\u03c4\u03c5\u03c6\u03c7\u03c8\u03c9";
  private static final String FNAME_EQUIVALENT_CHARS = "aaaaaeeeeiiiiooooouuuunceoaaaoypyabgdezhtjklmnkoprsstufxpo";

  public static String getValidFileName(String fn) {
    String result = null;
    if (fn != null) {
      StringBuilder sb = new StringBuilder();
      for (char ch : fn.toCharArray()) {
        ch = Character.toLowerCase(ch);
        if (FNAME_VALID_CHARS.indexOf(ch) < 0) {
          int p = FNAME_CONVERTIBLE_CHARS.indexOf(ch);
          if (p >= 0) {
            ch = FNAME_EQUIVALENT_CHARS.charAt(p);
          } else {
            ch = '_';
          }
        }
        sb.append(ch);
      }
      result = sb.substring(0);
    }
    return result;
  }

}
