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

package javax.media.protocol;

import javax.media.*;
import javax.media.Duration;
import java.io.IOException;
import java.net.*;

abstract public class DataSource implements Controls, Duration {

    MediaLocator sourceLocator;
	
    public DataSource() {}
    public DataSource(MediaLocator source) {}
    public void setLocator(MediaLocator source) {}
    public MediaLocator getLocator() {return null;}
    protected void initCheck() {}
    public abstract String getContentType();
    public abstract void connect() throws IOException; 
    public abstract void disconnect();
    public abstract void start() throws IOException;
    public abstract void stop() throws IOException;

}
