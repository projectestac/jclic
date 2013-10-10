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

package quicktime.std.sg;

import quicktime.QTException;
import quicktime.std.StdQTException;
import quicktime.io.QTFile;

public final class SequenceGrabber
{
    public SequenceGrabber() throws QTException{}
    public boolean isRecordMode() throws StdQTException {return false;}
    public void stop() throws StdQTException {}
    public void setDataOutput(QTFile qtfile, int i) throws QTException {}
    public void setMaximumRecordTime(int i) throws StdQTException {}
    public void prepare(boolean flag, boolean flag1) throws StdQTException {}
    public void idle() throws StdQTException {}
    public boolean idleMore() throws StdQTException {return false;}
    public void disposeChannel(SGChannel sgchannel) throws StdQTException {}
    public void release() throws StdQTException {}
    public void startRecord() throws StdQTException {}
}
