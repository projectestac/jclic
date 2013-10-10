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

import java.lang.reflect.*;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import javax.media.protocol.*;
import javax.media.control.*;
import javax.media.*;

public final class Manager {

    private static String VERSION = "Not implemented!!";
    public static final int MAX_SECURITY = 1;
    public static final int  CACHING = 2;
    public static final int  LIGHTWEIGHT_RENDERER = 3;
    public static final int  PLUGIN_PLAYER = 4;    
    public final static String UNKNOWN_CONTENT_NAME = "unknown";

    private Manager() {}
    public static String getVersion() {return VERSION;}
    public static Player createPlayer(URL sourceURL) throws IOException, NoPlayerException {return null;}
    public static Player createPlayer(MediaLocator sourceLocator) throws IOException, NoPlayerException {return null;}
    public static Player createPlayer(DataSource source) throws IOException, NoPlayerException {return null;}
    public static Player createRealizedPlayer(URL sourceURL) throws IOException, NoPlayerException, CannotRealizeException {return null;}
    public static Player createRealizedPlayer(MediaLocator ml) throws IOException, NoPlayerException, CannotRealizeException {return null;}
    public static Player createRealizedPlayer(DataSource source) throws IOException, NoPlayerException, CannotRealizeException {return null;}
    public static Processor createProcessor(URL sourceURL) throws IOException, NoProcessorException {return null;}
    public static Processor createProcessor(MediaLocator sourceLocator) throws IOException, NoProcessorException {return null;}
    public static Processor createProcessor(DataSource source) throws IOException, NoProcessorException {return null;}
    public static Processor createRealizedProcessor(ProcessorModel model) throws IOException, NoProcessorException, CannotRealizeException {return null;}
    public static DataSource createDataSource(URL sourceURL) throws IOException, NoDataSourceException  {return null;}
    static public DataSource createDataSource(MediaLocator sourceLocator) throws IOException, NoDataSourceException {return null;}
    static public DataSource createMergingDataSource(DataSource[] sources) throws IncompatibleSourceException {return null;}
    static public DataSource createCloneableDataSource(DataSource source) {return null;}
    static public DataSink createDataSink(DataSource datasource, MediaLocator destLocator) throws NoDataSinkException {return null;}
    public static String getCacheDirectory() {return null;}
    public static void setHint(int hint, Object value) {}
    public static Object getHint(int hint) {return null;}
    static final int DONE = 0;
    static final int SUCCESS = 1;
    static public Vector getDataSourceList(String protocolName) {return null;}
    static public Vector getHandlerClassList(String contentName) {return null;}
    static public Vector getProcessorClassList(String contentName) {return null;}
    static Vector buildClassList(Vector prefixList, String name) {return null;}
    static Vector getContentPrefixList() {return null;}
    static Vector getProtocolPrefixList() {return null;}
}

