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

import java.awt.Component;
import quicktime.QTException;
import quicktime.std.movies.Movie;

public interface QTComponent
{
    public abstract void setMovie(Movie movie) throws QTException;
    public abstract Movie getMovie();
    public abstract Component asComponent();
}
