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

import java.util.Enumeration;
import javax.servlet.ServletContext;

public interface HttpSession {

    public long getCreationTime();
    public String getId();
    public long getLastAccessedTime();
    public ServletContext getServletContext();
    public void setMaxInactiveInterval(int interval);
    public int getMaxInactiveInterval();
    public HttpSessionContext getSessionContext();
    public Object getAttribute(String name);
    public Object getValue(String name);
    public Enumeration getAttributeNames();
    public String[] getValueNames();
    public void setAttribute(String name, Object value);
    public void putValue(String name, Object value);
    public void removeAttribute(String name);
    public void removeValue(String name);
    public void invalidate();
    public boolean isNew();
}

