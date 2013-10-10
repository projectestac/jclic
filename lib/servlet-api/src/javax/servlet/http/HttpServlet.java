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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public abstract class HttpServlet extends GenericServlet
    implements java.io.Serializable
{

    public HttpServlet() { }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
    {}

    protected long getLastModified(HttpServletRequest req) {
	return -1;
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
    {}
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
    {}

    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
    {}

    protected void doDelete(HttpServletRequest req,
			    HttpServletResponse resp)
	throws ServletException, IOException
    {}
    
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
    {}
    
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) 
	throws ServletException, IOException
    {}		

    protected void service(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
    {}

    public void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException
    {}
}
