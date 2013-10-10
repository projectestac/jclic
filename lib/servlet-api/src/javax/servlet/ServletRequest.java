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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public interface ServletRequest {
    public Object getAttribute(String name);
    public Enumeration getAttributeNames();
    public String getCharacterEncoding();
    public void setCharacterEncoding(String env) throws java.io.UnsupportedEncodingException;
    public int getContentLength();
    public String getContentType();
    public ServletInputStream getInputStream() throws IOException; 
    public String getParameter(String name);
    public Enumeration getParameterNames();
    public String[] getParameterValues(String name);
    public Map getParameterMap();
    public String getProtocol();
    public String getScheme();
    public String getServerName();
    public int getServerPort();
    public BufferedReader getReader() throws IOException;
    public String getRemoteAddr();
    public String getRemoteHost();
    public void setAttribute(String name, Object o);
    public void removeAttribute(String name);
    public Locale getLocale();
    public Enumeration getLocales();
    public boolean isSecure();
    public RequestDispatcher getRequestDispatcher(String path);
    public String getRealPath(String path);
    public int getRemotePort();
    public String getLocalName();
    public String getLocalAddr();
    public int getLocalPort();

}

