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
import javax.media.control.TrackControl;

public interface Processor extends Player {
    public final static int Configuring = 140;
    public final static int Configured = 180;
    public void configure();
    public TrackControl[] getTrackControls() throws NotConfiguredError;
    public ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError;
    public ContentDescriptor setContentDescriptor(ContentDescriptor outputContentDescriptor) throws NotConfiguredError ;
    public ContentDescriptor getContentDescriptor() throws NotConfiguredError;
    public DataSource getDataOutput() throws NotRealizedError;    
}
