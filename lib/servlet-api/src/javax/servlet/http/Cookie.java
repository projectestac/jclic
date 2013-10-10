/*
 * This is just an INCOMPLETE, EMPTY and NO-OPERATIONAL implementation of 
 * JSR-154 (Servlet 2.4), based on the public specification available at: 
 * http://jcp.org/aboutJava/communityprocess/final/jsr154/index.html
 * 
 * The files contained in this directory tree are used only at compile-time to make 
 * possible the build process of JClic without external dependencies. They will not be 
 * compiled, packaged nor included in the binary release of JClic obtained as a result 
 * of this build process.
 * 
 * Full operational versions of this library are available at:
 * http://java.sun.com/products/servlet/reference/api/index.html
 */

package javax.servlet.http;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Cookie implements Cloneable {
    public Cookie(String name, String value) {}

    public void setComment(String purpose) {}
    
    public String getComment() {
	return null;
    }
    
    public void setDomain(String pattern) {}
    
    public String getDomain() {
	return null;
    }

    public void setMaxAge(int expiry) {}

    public int getMaxAge() {
	return -1;
    }
    
    public void setPath(String uri) {}

    public String getPath() {
	return null;
    }

    public void setSecure(boolean flag) {}

    public boolean getSecure() {
	return false;
    }

    public String getName() {
	return null;
    }

    public void setValue(String newValue) {}

    public String getValue() {
	return null;
    }

    public int getVersion() {
	return -1;
    }

    public void setVersion(int v) {}

    public Object clone() {
       return null;
    }
}

