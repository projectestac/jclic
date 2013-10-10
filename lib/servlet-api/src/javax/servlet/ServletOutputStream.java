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

import java.io.OutputStream;
import java.io.IOException;
import java.io.CharConversionException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract class ServletOutputStream extends OutputStream {

    protected ServletOutputStream() { }
    public void print(String s) throws IOException {}
    public void print(boolean b) throws IOException {}
    public void print(char c) throws IOException {}
    public void print(int i) throws IOException {}
    public void print(long l) throws IOException {}
    public void print(float f) throws IOException {}
    public void print(double d) throws IOException {}
    public void println() throws IOException {}
    public void println(String s) throws IOException {}
    public void println(boolean b) throws IOException {}
    public void println(char c) throws IOException {}
    public void println(int i) throws IOException {}
    public void println(long l) throws IOException {}
    public void println(float f) throws IOException {}
    public void println(double d) throws IOException {}
}
