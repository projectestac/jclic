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

import java.awt.Component;
import javax.media.protocol.DataSource;
import java.io.IOException;

public interface Player extends MediaHandler, Controller {
    public Component getVisualComponent();	
    public GainControl getGainControl();
    public Component getControlPanelComponent();
    public void start();
    public void addController(Controller newController)
	throws IncompatibleTimeBaseException;
    public void removeController(Controller oldController);
}
