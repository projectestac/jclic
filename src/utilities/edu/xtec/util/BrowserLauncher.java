/*
 * File    : BrowserLauncher.java
 * Created : 02-jul-2002 18:22
 * Remaked:  01-Feb-2011 17:30
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Old version was taken from: Eric Albert's BrowserLauncher class (ejalbert@cs.stanford.edu)
 * New version from BareBonesBrowserLauncher, by Dem Pilafian enhanced by Williem van Engen
 * Adapted by Francesc Busquets
 *
 * @author Eric Albert (ejalbert@cs.stanford.edu) modified by Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */

package edu.xtec.util;

import java.awt.Component;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import javax.swing.JOptionPane;

/** Class to open the system's default web browser.
 * <p>
 * Until Java 1.6, there was no standard way to open an internet page with the
 * user's default web browser. This class aims to be a simple just-works solution
 * to this. It tries to use Java 1.6's method first, and falls back to running
 * an executable (which requires full system permissions, of course), which is
 * different for each platform.
 * <p>
 * This code is an adaptation of
 * <a href="http://www.centerkey.com/java/browser/">http://www.centerkey.com/java/browser/</a>
 */
public class BrowserLauncher {


    /** Added by fbusquets - command that launches the user's preferred browser **/
    private static String preferredBrowser;
    /** Added by fbusquets - Key used to store browser settings in properties files **/
    public static final String BROWSER="browser";


    private static final String errMsg = "Error attempting to launch web browser";

    /** Open a URL
     *
     * @param url {@link URL} to open
     * @param parent parent component for error message dialog
     */
    public static void openURL(URL url, Component parent) {
	openURL(url.toExternalForm(), parent);
    }

    /** Open a string URL
     *
     * @param surl URL to open (as String)
     * @param parent parent component for error message dialog
     */
    //@SuppressWarnings("unchecked") // to support older java compilers
    public static void openURL(String surl, Component parent) {
	// Try java desktop API first (new in Java 1.6)
	// basically: java.awt.Desktop.getDesktop().browse(new URI(url));

        // Replace whitespaces with the URL-encoded expression "%20"
        surl=StrUtils.replace(surl, " ", "%20");

	try {
	    Class desktop = Class.forName("java.awt.Desktop");
	    Method getDesktop = desktop.getDeclaredMethod("getDesktop", new Class[] {});
	    Object desktopInstance = getDesktop.invoke(null, new Object[] {});
	    Method browse = desktop.getDeclaredMethod("browse", new Class[] {URI.class});
	    URI uri = new URI(surl);
	    //logger.fine("Using Java Desktop API to open URL '"+url+"'");
	    browse.invoke(desktopInstance, new Object[] {uri});
	    return;
	} catch(Exception e) { }

	// Failed, resort to executing the browser manually
	String osName = System.getProperty("os.name");
	try {
	    // Mac OS has special Java class
	    if (osName.startsWith("Mac OS")) {
		Class fileMgr = Class.forName("com.apple.eio.FileManager");
		Method openURL = fileMgr.getDeclaredMethod("openURL",
			new Class[] {String.class});
		//logger.fine("Using "+fileMgr+" to open URL '"+url+"'");
		openURL.invoke(null, new Object[] {surl});
		return;
	    }

	    String[] cmd;

	    // Windows execs url.dll
	    if (osName.startsWith("Windows")) {
		cmd = new String[] { "rundll32", "url.dll,FileProtocolHandler", surl };

	    // else assume unix/linux: call one of the available browsers
	    } else {
		String[] browsers = {
                        // user's Preferred browser (will be skipped if null or empty):
                        preferredBrowser,
			// Freedesktop, http://portland.freedesktop.org/xdg-utils-1.0/xdg-open.html
			"xdg-open",
			// Debian
			"sensible-browser",
			// Otherwise call browsers directly
			"chromium", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
		String browser = null;
		for (int count = 0; count < browsers.length && browser == null; count++)
		    if (browsers[count]!=null && browsers[count].length()>0 && Runtime.getRuntime().exec(
			    new String[] {"which", browsers[count]}).waitFor() == 0)
			browser = browsers[count];

		if (browser == null) {
		    //logger.warning("No web browser found");
		    throw new Exception("Could not find web browser");
		}

		cmd = new String[] { browser, surl };

	    }

	    if (Runtime.getRuntime().exec(cmd).waitFor() !=0 )
		throw new Exception("Error opening page: "+surl);
	}
	catch (Exception e) {
	    JOptionPane.showMessageDialog(parent, errMsg + ":\n" + e.getLocalizedMessage());
	}
    }

    /** Open a URL
     * <p>
     * This is equal to {@link #openURL(URL, Component) openURL(url, null)}
     * so any error dialog will have no parent.
     *
     * @param url {@link URL} to open
     */
    public static void openURL(URL url) {
	openURL(url, null);
    }

    /** Open a string URL
     * <p>
     * This is equal to {@link #openURL(String, Component) openURL(surl, null)}
     * so any error dialog will have no parent.
     *
     * @param surl URL to open (as String)
     */
    public static void openURL(String surl) {
	openURL(surl, null);
    }

    /**
     * Added by fbusquets
     * Sets the path to the user's perferred browser.
     * @param browserCmd The path to the browser to be used by BrowserLaunched
     */
    public static void setPreferredBrowser(String browserCmd){
        preferredBrowser=StrUtils.nullableString(browserCmd);
    }

    /**
     * Added by fbusquets
     * Gets the path to the user's preferred browser.
     * @param defaultValue The default choice, used only if preferred browser not set.
     * @returns The command line corresponding to the user's preferred browser
     */
    public static String getPreferredBrowser(String defaultValue){
        return StrUtils.secureString(preferredBrowser, defaultValue);
    }

}
