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

import java.lang.Class;

public class Format implements java.lang.Cloneable, java.io.Serializable {

    public static final int NOT_SPECIFIED = -1;
    public static final int TRUE = 1;
    public static final int FALSE = 0;
    
    protected String encoding;

    public static final Class intArray = (new int[0]).getClass();
    public static final Class shortArray = (new short[0]).getClass();
    public static final Class byteArray = (new byte[0]).getClass();
    public static final Class formatArray = (new Format[0]).getClass();    
    protected Class dataType = byteArray;
    protected Class clz = getClass();
	
    public Format(String encoding) {}
    public Format(String encoding, Class dataType) {}
    public String getEncoding() {return null;}
    public Class getDataType() {return null;}
    public boolean equals(Object format) {return false;}
    public boolean matches(Format format) {return false;}
    public Format intersects(Format other) {return null;}
    public boolean isSameEncoding(Format other) {return false;}
    public boolean isSameEncoding(String encoding) {return false;}
    public Format relax() {return null;}
    public Object clone() {return null;}
    protected void copy(Format f) {}
}
