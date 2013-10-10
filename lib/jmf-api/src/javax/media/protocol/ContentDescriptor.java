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

import javax.media.Format;

public class ContentDescriptor extends Format {
    static public final String RAW	 	= "raw";
    static public final String RAW_RTP          = "raw.rtp";
    static public final String MIXED		= "application.mixed-data";
    static public final String CONTENT_UNKNOWN 	= "UnknownContent";
    public String getContentType() {return null;}
    public ContentDescriptor(String cdName) {super(cdName);}
    static final public  String mimeTypeToPackageName(String mimeType) {return null;}
}
