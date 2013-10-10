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

import java.net.*;

public class MediaLocator implements java.io.Serializable {
    public MediaLocator(URL url) {}
    public MediaLocator(String locatorString) {}
    public URL getURL() throws MalformedURLException {return null;}
    public String getProtocol() {return null;}
    public String getRemainder() {return null;}
    public String toExternalForm() {return null;}
}
