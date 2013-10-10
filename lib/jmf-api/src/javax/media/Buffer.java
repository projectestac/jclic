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

public class Buffer {
    public final static int FLAG_EOM = (1 << 0);
    public final static int FLAG_DISCARD = (1 << 1);
    public final static int FLAG_SILENCE = (1 << 2);
    public final static int FLAG_SID = (1 << 3);
    public final static int FLAG_KEY_FRAME = (1 << 4);
    public final static int FLAG_NO_DROP = (1 << 5);
    public final static int FLAG_NO_WAIT = (1 << 6);
    public final static int FLAG_NO_SYNC = (FLAG_NO_DROP | FLAG_NO_WAIT);
    public final static int FLAG_SYSTEM_TIME = (1 << 7);
    public final static int FLAG_RELATIVE_TIME = (1 << 8);
    public final static int FLAG_FLUSH = (1 << 9);
    public final static int FLAG_SYSTEM_MARKER = (1 << 10);
    public final static int FLAG_RTP_MARKER = (1 << 11);
    public final static int FLAG_RTP_TIME = (1 << 12);
    public final static int FLAG_BUF_OVERFLOWN = (1 << 13);
    public final static int FLAG_BUF_UNDERFLOWN = (1 << 14);
    public final static int FLAG_LIVE_DATA = (1 << 15);
    public final static long TIME_UNKNOWN = -1L;
    public final static long SEQUENCE_UNKNOWN = Long.MAX_VALUE - 1;
    public Format getFormat() {return null;}
    public void setFormat(Format format){}
    public int getFlags() {return 0;}
    public void setFlags(int flags) {}
    public boolean isEOM() {return false;}
    public void setEOM(boolean eom) {}
    public boolean isDiscard() {return false;}
    public void setDiscard(boolean discard) {}
    public Object getData() {return null;}
    public void setData(Object data) {}
    public Object getHeader() {return null;}
    public void setHeader(Object header) {}
    public int getLength() {return 0;}
    public void setLength(int length){}
    public int getOffset() {return 0;}
    public void setOffset(int offset) {}
    public long getTimeStamp() {return 0L;}
    public void setTimeStamp(long timeStamp) {}
    public long getDuration() {return 0L;}
    public void setDuration(long duration) {}
    public void setSequenceNumber(long number) {}
    public long getSequenceNumber() {return 0L;}
    public void copy(Buffer buffer) {}
    public void copy(Buffer buffer, boolean swapData) {}
    public Object clone() {return null;}
}

