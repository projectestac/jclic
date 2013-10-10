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

public interface Controller extends Clock, Duration {
    public final static Time LATENCY_UNKNOWN = new Time(Long.MAX_VALUE);
    public final static int Unrealized = 100;
    public final static int Realizing = 200;
    public final static int Realized = 300;
    public final static int Prefetching = 400;
    public final static int Prefetched = 500;
    public final static int Started = 600;
    public int getState();
    public int getTargetState();
    public void realize();
    public void prefetch();
    public void deallocate();
    public void close();
    public Time getStartLatency();
    public Control[] getControls();
    public Control getControl(String forName);
    public void addControllerListener(ControllerListener listener);
    public void removeControllerListener(ControllerListener listener);
}
