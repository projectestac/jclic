/*
 * Po2Prop.java
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
public class Po2Prop extends Task {
    
    // Original source code
    
    /**
     * The name of the po file.
     */
    protected File inputFile;

    protected File checkAgainstFile;
    protected File checkAgainstDir;

    /**
     * The template file.
     */
    protected File templateFile;

    protected File outputFile;

    protected File destDir;

    protected boolean verbose;

    protected boolean preserveTimestamps;

    /**
     * the filesets of the .po to convert
     */
    protected Vector filesets = new Vector();

    protected FileUtils fileUtils = FileUtils.newFileUtils();

    /**
     * the .po file to convert; required
     */
    public void setInputFile(final File inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * set the preserveTimestamps value
     */
    public void setPreserveTimestamps(boolean preserveTimestamps) {
        this.preserveTimestamps = preserveTimestamps;
    }

    /**
     * the .po file to check against; optional
     */
    public void setCheckAgainstFile(final File checkAgainstFile) {
        this.checkAgainstFile = checkAgainstFile;
    }

    /**
     * the .directory containing .po files to check against
     */
    public void setCheckAgainstDir(final File checkAgainstDir) {
        this.checkAgainstDir = checkAgainstDir;
    }

    /**
     * template location; required
     */
    public void setTemplateFile(final File templateFile) {
        this.templateFile = templateFile;
    }

    /**
     * name of exported .properties file; optional
     */
    public void setOutputFile(final File outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Sets the destination directory.
     * @param destDir the destination directory
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }


    /**
     * Adds a set of files to convert
     */
    public void addFileset(final FileSet set) {
        filesets.addElement(set);
    }

    /** 
     * Convert the files
     */
    public void execute() throws BuildException {
        if (null == inputFile && null == filesets) {
            throw new BuildException("a .po file must be set through inputFile attribute "
                                     + "or nested filesets");
        }
        if (null != inputFile) {
            doOneFile(inputFile, outputFile, checkAgainstFile);
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
    private void doOneFile(File inputFile, File outputFile, File checkAgainstFile) 
        throws BuildException {
        if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_1)) {
            throw new BuildException("The Po2Prop task is only available on "
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
            outputFile=new File(destDir, justFName+".properties");
        }

        if(checkAgainstFile==null && checkAgainstDir!=null && inputFile!=null){
            checkAgainstFile=new File(checkAgainstDir, inputFile.getName());
        }

        if (isUpToDate(inputFile, outputFile, checkAgainstFile)) {
          return;
        }

        final ExecTask cmd = (ExecTask) getProject().createTask("exec");
        cmd.setExecutable("po2prop");

        if (null != templateFile) {
            cmd.createArg().setValue("--template="+templateFile.toString());
        }

        if (null != inputFile) {
            cmd.createArg().setValue("--input="+inputFile.toString());
        }

        if (null != outputFile) {
            cmd.createArg().setValue("--output="+outputFile.toString());
        }

        log("Converting properties: " + inputFile.getAbsolutePath());
        cmd.setFailonerror(true);
        cmd.setTaskName(getTaskName());
        cmd.execute();

       if(preserveTimestamps){
           outputFile.setLastModified(inputFile.lastModified());
       }
    }
    
    protected boolean isUpToDate(File inputFile, File outputFile, File checkAgainstFile) throws BuildException {
        if (null == inputFile) {
            return false;
        }

        if (null != outputFile) {

            if (!inputFile.exists()) {
              return false;
            }

            if (null!=checkAgainstFile && checkAgainstFile.exists()){
                if(checkAgainstFile.lastModified() >= inputFile.lastModified()){
                   return true;
                } else{
                   try {
                     if(fileUtils.contentEquals(inputFile, checkAgainstFile, true))
                       return true;
                   } catch(Exception ex){
                       throw new BuildException(ex);
                   }
                }
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
