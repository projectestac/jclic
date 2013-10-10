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

public interface Clock {
    static public final Time RESET = new Time(Long.MAX_VALUE);
    public void setTimeBase(TimeBase master) throws IncompatibleTimeBaseException;
    public void syncStart(Time at);
    public void stop();
    public void setStopTime(Time stopTime);
    public Time getStopTime();
    public void setMediaTime(Time now);
    public Time getMediaTime();
    public long getMediaNanoseconds();
    public Time getSyncTime();
    public TimeBase getTimeBase();
    public Time mapToTimeBase(Time t) throws ClockStoppedException;
    public float getRate();
    public float setRate(float factor);
}
