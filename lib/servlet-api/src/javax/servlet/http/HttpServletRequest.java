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

import javax.servlet.ServletRequest;
import java.util.Enumeration;

public interface HttpServletRequest extends ServletRequest {

    public static final String BASIC_AUTH = "BASIC";
    public static final String FORM_AUTH = "FORM";
    public static final String CLIENT_CERT_AUTH = "CLIENT_CERT";
    public static final String DIGEST_AUTH = "DIGEST";

    public String getAuthType();
    public Cookie[] getCookies();
    public long getDateHeader(String name);
    public String getHeader(String name); 
    public Enumeration getHeaders(String name); 
    public Enumeration getHeaderNames();
    public int getIntHeader(String name);
    public String getMethod();
    public String getPathInfo();
    public String getPathTranslated();
    public String getContextPath();
    public String getQueryString();
    public String getRemoteUser();
    public boolean isUserInRole(String role);
    public java.security.Principal getUserPrincipal();
    public String getRequestedSessionId();
    public String getRequestURI();
    public StringBuffer getRequestURL();
    public String getServletPath();
    public HttpSession getSession(boolean create);
    public HttpSession getSession();
    public boolean isRequestedSessionIdValid();
    public boolean isRequestedSessionIdFromCookie();
    public boolean isRequestedSessionIdFromURL();
    public boolean isRequestedSessionIdFromUrl();
}
