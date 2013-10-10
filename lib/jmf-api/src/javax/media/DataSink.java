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

import java.io.IOException;

public interface DataSink extends MediaHandler, Controls {
    public void setOutputLocator(MediaLocator output);
    public MediaLocator getOutputLocator();
    public void start() throws IOException;
    public void stop() throws IOException;
    public void open() throws IOException, SecurityException; 
    public void close();
    public String getContentType();
    public void addDataSinkListener(javax.media.datasink.DataSinkListener listener);
    public void removeDataSinkListener(javax.media.datasink.DataSinkListener listener);
}

