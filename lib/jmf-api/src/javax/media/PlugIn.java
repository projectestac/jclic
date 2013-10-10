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

import javax.media.Controls;

public interface PlugIn extends Controls {
    public static final int BUFFER_PROCESSED_OK = 0;
    public static final int BUFFER_PROCESSED_FAILED = 1 << 0;
    public static final int INPUT_BUFFER_NOT_CONSUMED = 1 << 1;
    public static final int OUTPUT_BUFFER_NOT_FILLED  = 1 << 2;
    public static final int PLUGIN_TERMINATED = 1 << 3;
    public String getName();
    public void open() throws ResourceUnavailableException;
    public void close();
    public void reset();
}


