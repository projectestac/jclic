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

import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;

public abstract class GenericServlet 
    implements Servlet, ServletConfig, java.io.Serializable
{
    public GenericServlet() { }

    public void destroy() {}
    
    public String getInitParameter(String name) {
        return null;
    }
    
    public Enumeration getInitParameterNames() {
        return null;
    }   
    
    public ServletConfig getServletConfig() {
	return null;
    }
    
    public ServletContext getServletContext() {
        return null;
    }

    public String getServletInfo() {
	return null;
    }

    public void init(ServletConfig config) throws ServletException {}
    public void init() throws ServletException {}    
    public void log(String msg) {}
    public void log(String message, Throwable t) {}
    public abstract void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException;

    public String getServletName() {
        return null;
    }
}
