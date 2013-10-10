/*
 * This is just an INCOMPLETE, EMPTY and NO-OPERATIONAL implementation of the 
 * QuickTime(TM) for Java library, based on the public API available at: 
 * http://developer.apple.com/documentation/Java/Reference/1.4.1/Java141API_QTJ
 *
 * The information contained in this file is used only at compile-time to make 
 * possible the complete build process of JClic without external non-free 
 * dependencies. 
 *
 * A full operational version of the library is available at:
 * http://developer.apple.com/quicktime/qtjava
 */

package quicktime.std.movies;

import quicktime.QTObject;
import quicktime.QTException;
import quicktime.std.StdQTException;
import quicktime.std.movies.media.DataRef;
import quicktime.std.movies.TimeInfo;

public final class Movie extends QTObject
{
    public static Movie fromDataRef(DataRef dataref, int i) throws QTException {return null;}
    public Movie(int i) throws QTException {}
    public void setActive(boolean flag) throws StdQTException {}
    public void setTimeScale(int i) throws StdQTException {}
    public void setActiveSegment(TimeInfo timeinfo) throws StdQTException {}
    public void setDefaultDataRef(DataRef dataref) throws StdQTException {}
}
