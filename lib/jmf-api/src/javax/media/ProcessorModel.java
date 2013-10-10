/*
 * This is just an INCOMPLETE, EMPTY and NO-OPERATIONAL implementation of the 
 * Java Media Framework library, based on the public API available at: 
 * http://java.sun.com/products/java-media/jmf/2.1.1/apidocs
 *
 * The information contained in this file is used only at compile-time to make 
 * possible the complete build process of JClic without external non-free 
 * dependencies. 
 *
 * A full operational version of the library is available at:
 * http://java.sun.com/products/java-media/jmf
 */

package javax.media;

import javax.media.protocol.*;

public class ProcessorModel {
    public ProcessorModel() {}
    public ProcessorModel(Format [] formats, ContentDescriptor outputContentDescriptor) {}
    public ProcessorModel(DataSource inputDataSource, Format [] formats, ContentDescriptor outputContentDescriptor) {}
    public ProcessorModel(MediaLocator inputLocator, Format [] formats, ContentDescriptor outputContentDescriptor) {}
    public int getTrackCount(int availableTrackCount) {return 0;}
    public Format getOutputTrackFormat(int tIndex) {return null;}
    public boolean isFormatAcceptable(int tIndex, Format tFormat) {return false;}
    public ContentDescriptor getContentDescriptor() {return null;}
    public javax.media.protocol.DataSource getInputDataSource() {return null;}
    public MediaLocator getInputLocator() {return null;}
}
