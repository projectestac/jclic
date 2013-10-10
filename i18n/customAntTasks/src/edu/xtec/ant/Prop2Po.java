/*
 * Prop2Po.java
 *
 */

package edu.xtec.ant;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
import java.util.Vector;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.taskdefs.ExecTask;
import java.util.Enumeration;
import java.io.IOException;

/**
 * @author fbusquet
 */
public class Prop2Po extends Task {
    
    // Original source code
    
    /**
     * The name of the properties file.
     */
    protected File inputFile;

    /**
     * The .properties template file.
     */
    protected File templateFile;

    protected File outputFile;
    protected File backupFile;

    protected File destDir;
    protected File backupDir;

    protected boolean verbose;
    protected boolean makePot;
    protected boolean preserveTimestamps;

    /**
     * the filesets of the .properties to convert
     */
    protected Vector filesets = new Vector();

    protected FileUtils fileUtils = FileUtils.newFileUtils();

    /**
     * set the preserveTimestamps value
     */
    public void setPreserveTimestamps(boolean preserveTimestamps) {
        this.preserveTimestamps = preserveTimestamps;
    }

    /**
     * the .properties file to convert; required
     */
    public void setInputFile(final File inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * template location; required
     */
    public void setTemplateFile(final File templateFile) {
        this.templateFile = templateFile;
    }

    /**
     * name of exported PO file; optional
     */
    public void setOutputFile(final File outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * name of the backup file; optional
     */
    public void setBackupFile(final File backupFile) {
        this.backupFile = backupFile;
    }

    /**
     * Sets the destination directory.
     * @param destDir the destination directory
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Sets the backup directory.
     * @param backupDir the backup directory
     */
    public void setBackupDir(File backupDir) {
        this.backupDir = backupDir;
    }

    /**
     * export a POT template
     * ; optional: default false
     */
    public void setMakePot(final boolean makePot) {
        this.makePot = makePot;
    }

    /**
     * Adds a set of files to convert
     */
    public void addFileset(final FileSet set) {
        filesets.addElement(set);
    }

    // Completely rewrited
    
    /** 
     * Convert the files
     */
    public void execute() throws BuildException {
        if (null == inputFile && null == filesets) {
            throw new BuildException("a properties file must be set through inputFile attribute "
                                     + "or nested filesets");
        }
        if (null != inputFile) {
            doOneFile(inputFile, outputFile, backupFile);
            return;
        } else {
            //Assume null != filesets

            // deal with the filesets
            for (int i = 0; i < filesets.size(); i++) {
                FileSet fs = (FileSet) filesets.elementAt(i);
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                String[] inputFiles = ds.getIncludedFiles();
                for (int j = 0; j < inputFiles.length; j++) {
                    doOneFile(new File(fs.getDir(getProject()), inputFiles[j]), null, null);
                }
            }
        }
    }

    /**
     * convert one file
     */
    private void doOneFile(File inputFile, File outputFile, File backupFile) 
        throws BuildException {
        if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_1)) {
            throw new BuildException("The Prop2Po task is only available on "
                                     + "JDK versions 1.2 or greater");
        }

        if(outputFile==null){
            if(destDir==null){
               destDir=inputFile.getParentFile();
            }

            String justFName=inputFile.getName();
            int lastDot=justFName.lastIndexOf('.');
            if(lastDot>0)
              justFName=justFName.substring(0, lastDot);
            String ext=makePot ? ".pot" : ".po";
            outputFile=new File(destDir, justFName+ext);
        }

        if(backupFile==null && backupDir!=null){
            backupFile=new File(backupDir, outputFile.getName());
            if(backupFile.equals(outputFile))
              backupFile=null;
        }

        if (isUpToDate(inputFile, outputFile)) {
          return;
        }

        final ExecTask cmd = (ExecTask) getProject().createTask("exec");
        cmd.setExecutable("prop2po");

        if (makePot) {
            cmd.createArg().setValue("-P");
        }

        if (null != templateFile) {
            cmd.createArg().setValue("--template="+templateFile.toString());
        }

        if (null != inputFile) {
            cmd.createArg().setValue(inputFile.toString());
        }

        if (null != outputFile) {
            cmd.createArg().setValue(outputFile.toString());
        }

        log("Converting properties: " + inputFile.getAbsolutePath());
        cmd.setFailonerror(true);
        cmd.setTaskName(getTaskName());
        cmd.execute();

        if(preserveTimestamps){
           outputFile.setLastModified(inputFile.lastModified());
        }

        if(backupFile!=null) {
           log("Creating backup of "+outputFile.getName()+" in "+backupFile.getParent().toString());
           try{
              fileUtils.copyFile(outputFile, backupFile, null, true, true);
           } catch(Exception ex){
              throw new BuildException(ex);
           }
        }
    }
    
    protected boolean isUpToDate(File inputFile, File outputFile) {
        if (null == inputFile) {
            return false;
        }

        if (null != outputFile) {

            if (!inputFile.exists()) {
              return false;
            }
            if (!outputFile.exists()) {
              return false;
            }
            if (inputFile.equals(outputFile)) {
              return false;
            }
            if (outputFile.lastModified() >= inputFile.lastModified()) {
                return true;
            }
        }

        return false;
    }
}
