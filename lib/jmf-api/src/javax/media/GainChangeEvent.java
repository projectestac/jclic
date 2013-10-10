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

public class GainChangeEvent extends MediaEvent {
    public GainChangeEvent(Object o) {super(o);}
    //public GainChangeEvent(GainControl from, boolean mute, float dB, float level) {}
    public Object getSource() {return null;}
    public GainControl getSourceGainControl() {return null;}
    public float getDB() {return 0;}
    public float getLevel() {return 0;}
    public boolean getMute() {return false;}
}
