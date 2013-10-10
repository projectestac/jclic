/*
 * This is just an INCOMPLETE, EMPTY and NO-OPERATIONAL implementation of the 
 * Java Media Framework library, based on the public API available at: 
 * http://java.sun.com/products/java-media/jmf/2.1.1/apidocs
 *
 * The information contained in this file is used only at compile-time to make 
 * possible the complete build process of JClic without external non-free 
 * dependencies. 
 *
 * A full operational version of the library is available at:
 * http://java.sun.com/products/java-media/jmf
 */

package javax.media;

public class Time implements java.io.Serializable {

    public final static long   	ONE_SECOND  = 1000000000L;
    public final static Time 	TIME_UNKNOWN = new Time(Long.MAX_VALUE - 1);
    private final static double	NANO_TO_SEC = 1.0E-9;

    public Time(long nanoseconds) {}
    public Time(double seconds) {}

    protected long secondsToNanoseconds(double seconds) {return (long)(seconds * ONE_SECOND);}
    public long	getNanoseconds() {return 0L;}
    public double getSeconds() {return 0D;}
}
