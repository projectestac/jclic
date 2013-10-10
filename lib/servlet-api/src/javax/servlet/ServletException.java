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

public class ServletException extends Exception {

    public ServletException() {
	super();
    }
    
    public ServletException(String message) {
	super(message);
    }
    
    public ServletException(String message, Throwable rootCause) {
	super(message, rootCause);
    }

    public ServletException(Throwable rootCause) {
	super(rootCause);
    }
  
    public Throwable getRootCause() {
	return null;
    }
}
