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

package quicktime.std.movies.media;

import quicktime.QTException;
import quicktime.util.QTHandle;
import quicktime.util.QTHandleRef;
import quicktime.io.QTFile;

public final class DataRef extends QTHandle
{
    public DataRef(String s) throws QTException{}
    public DataRef(QTFile qtfile) throws QTException {}
    public DataRef(QTHandleRef qthandleref) throws QTException {}
    public DataRef(QTHandleRef qthandleref, int i, String s) throws QTException {}
}
