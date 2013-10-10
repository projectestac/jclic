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

package quicktime.app.view;

import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;

public class MoviePlayer
{
    public MoviePlayer(Movie movie) throws StdQTException {}
    public Movie getMovie(){return null;}
    public void setTime(int i) throws StdQTException {}
    public int getDuration() throws StdQTException {return 0;}
    public void setRate(float f) throws StdQTException {}
}
