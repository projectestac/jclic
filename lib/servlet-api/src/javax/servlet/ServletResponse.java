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
import java.io.PrintWriter;
import java.util.Locale;
 
public interface ServletResponse {
    public String getCharacterEncoding();
    public String getContentType();
    public ServletOutputStream getOutputStream() throws IOException;
    public PrintWriter getWriter() throws IOException;
    public void setCharacterEncoding(String charset);
    public void setContentLength(int len);
    public void setContentType(String type);
    public void setBufferSize(int size);
    public int getBufferSize();
    public void flushBuffer() throws IOException;
    public void resetBuffer();
    public boolean isCommitted();
    public void reset();
    public void setLocale(Locale loc);
    public Locale getLocale();
}





