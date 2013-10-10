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
 
package javax.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public interface ServletContext {
    public String getContextPath();
    public ServletContext getContext(String uripath);
    public int getMajorVersion();
    public int getMinorVersion();
    public String getMimeType(String file);
    public Set getResourcePaths(String path);
    public URL getResource(String path) throws MalformedURLException;
    public InputStream getResourceAsStream(String path);
    public RequestDispatcher getRequestDispatcher(String path);
    public RequestDispatcher getNamedDispatcher(String name);
    public Servlet getServlet(String name) throws ServletException;
    public Enumeration getServlets();
    public Enumeration getServletNames();
    public void log(String msg);
    public void log(Exception exception, String msg);
    public void log(String message, Throwable throwable);
    public String getRealPath(String path);
    public String getServerInfo();
    public String getInitParameter(String name);
    public Enumeration getInitParameterNames();
    public Object getAttribute(String name);
    public Enumeration getAttributeNames();
    public void setAttribute(String name, Object object);
    public void removeAttribute(String name);
    public String getServletContextName();
}
