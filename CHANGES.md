## Release notes for JClic (https://projectestac.github.io/jclic)
=================================================================

###Changes in 0.3.2.10 (05-Oct-2018)
---------------------------------------------------------------------------------------
- Check core components in delayed event handlers
- Process HTTP redirects also when protocol changes (from HTTP to HTTPS)
- Linux: Avoid incorrect keyboard layout settings caused by the 'LANGUAGE' environment variable
- Updated "launchApplet" script

###Changes in 0.3.2.9 (03-Aug-2018)
---------------------------------------------------------------------------------------
- Check ZIP file integrity when saving projects
- Write license tags to `project.json` in HTML5 export
- Enable 'level' in URL media type (opens in _BLANK)
- HTML5 export includes information about number of activities, media objects and size
- Updated Catalan translation
- Updated Java code formatting criteria

###Changes in 0.3.2.8 (12-Mar-2018)
---------------------------------------------------------------------------------------
- New license chooser in JClic Author. Allows to choose between different Creative Commons models, or set it to "other" (to be detailed in `decription`)
- JClic Author allows now to set custom icons for HTML export in three sizes: 16x16 (favicon), 72x72 and 192x192
- New fields in jclic files: `license`, `icon16`, `icon72` and `icon192` (see jclic.xsd)

###Changes in 0.3.2.7 (01-Feb-2018)
---------------------------------------------------------------------------------------
- JClic Author "refresh media" button will not act on elements where the original file does not exist
- The maximum recording time in "record audio" media elements has been increased to 3' (180")

###Changes in 0.3.2.6 (29-Oct-2017)
---------------------------------------------------------------------------------------
- Corrected an error found when parsing the new area and level descriptors
- Corrected an error found in JClic Author blocking exit without saving changes

###Changes in 0.3.2.4 (09-Oct-2017)
---------------------------------------------------------------------------------------
- Java 7 required
- New fields in jclic files (see jclic.xsd):
  - `meta-langs`: Language codes for wich a "description" is available. In packages published on the ClicZone, descriptions are usually provided in English (en), Catalan (ca) and Spanish (es).
  - `descriptions`: Since now, there was an unique "description" field. Now there is a collection of descriptions in several languages.
  - `cover` and `thumb`: Cover and thumbnail images, used to display the project in JClic libraries.
- JClic Author can now import files previously exported to ".scorm.zip".
- When opening a project, both in JClic player and author, meta-data is loaded from file "project.json" (if available).
- Export to HTML5 has been optimized, looking for files with same name but different content placed in different jclic.zip files on same folder.
- Re-organization of menus in JClic Author: now "Export to HTML5" has been moved fo "File" (from they old location in "Tools")

###Changes in 0.3.2.1 (05-Sep-2016)
---------------------------------------------------------------------------------------
- Reverted the change of the URL of jclic.min.js in HTML5 export, now pointing again to clic.xtec.cat

###Changes in 0.3.2.0 (15-Jul-2016)
---------------------------------------------------------------------------------------
- Add CORS headers in JClic Reports Server to allow direct communication with JClic.js running on browsers
- A SCORM package can be created in HTML5 export. This kind of files will be used in the new version of the [JClic module for Moodle](https://moodle.org/plugins/mod_jclic).

###Changes in 0.3.1.1 (28-Apr-2016)
---------------------------------------------------------------------------------------
- Write `animated` flag in MediaBag elements (needed for JClic.js in order to optimize the detection of animated GIFs)
- Changed the URL of jclic.min.js in HTML5 export, now pointing to the [JClic project in JSDelivr](http://www.jsdelivr.com/projects/jclic.js). The new URL is now: `https://cdn.jsdelivr.net/jclic.js/latest/jclic.min.js`

###Changes in 0.3.1.0 (09-Dec-2015)
---------------------------------------------------------------------------------------
- New option in JClicAuthor: Export to HTML5 using [jclic.js](http://projectestac.github.io/jclic.js).
- _Export to HTML5_ generates also a `project.json` file suitable for activity repositories created with _JClic Repo_ (see [https://github.com/projectestac/jclic-repo])
- JClic needs now Java 1.6 or later
- Correction of GitHub issue #5, related to URLs pointing to filenames containing whitespaces
- Added `maxRecursion` and `maxFiles` to `FileSystem.exploreFiles` to prevent hangs in large file systems

###Changes in 0.2.3.4 PPA (13-16 Dec-2013)
---------------------------------------------------------------------------------------
- Added a `dist/linux` directory, containing files needed to build PPA Debian packages: man files, bin launchers, desktop files, SVG icons and misc /debian control files.
- No modifications have been made on the source code of JClic, but `build.xml` has been updated to indicate UTF-8 as default encoding in javac ant task.

###Changes in 0.2.3.4 (29-Oct-2013)
---------------------------------------------------------------------------------------
- Correction of bugs affecting JClic Author: the program freezes when saving after making edits in the fields of the "Project" tab.
- Correction of a bug of Windows uninstaller: the program was not completely uninstalled because one file (reports.ico) was not removed

###Changes in 0.2.3.3 (28-Oct-2013)
---------------------------------------------------------------------------------------
- The official JClic JAR files released in http://clic.xtec.cat are now signed with a new certificate issued by the Catalan Agency of Certification "CATCert" (http://catcert.cat). This has no effect in the source code, but requires a small change in the version number to allow refreshing of Java Caché.

###Changes in 0.2.3.2 (07-Oct-2013)
---------------------------------------------------------------------------------------
- Implementation of "JClic System Libraries": System admins can now define libraries of JClic projects as a “System setting” for all users of a specific computer. The system libraries can be managed by command line, invoking: `java -cp jclic.jar edu.xtec.jclic.project.LibraryManager -[option] [name] [path]`
- JClic source code moved to [https://github.com/projectestac/jclic]

###Changes in 0.2.3.0 (23-Sep-2013)
---------------------------------------------------------------------------------------
- JClic needs now Java 1.5 or later to run, because some accessibility functions are only available from
- this version upwards. Previous JClic versions were targeted to Java 1.4.
- Correction of bug 1249: Incorrect reporting of activity results when the maximum number of attempts is achieved.
- Correction of bug 1414: Error in JClic applet with https protocol
- Correction of bug 64: Help window of text activities display in disabled colours
- Correction of bug 53: Saving existing activity with new name
- Correction of bug 172: The deletion of all activities of a project blocks JClic author
- Implementation of feature request #1591: Allow to replay the sound hosted in the initial message
- Changes in ReportServerJDBCBridge to set default behavior to MySQL instead of MSAccess
- Changed BrowserLauncher to BareBonesBrowserLauncher to solve a problem with browser detection in MacOS
- Support for Venetian (vec) and improvements to other languages.
- Image4j (BMP import) updated to version 0.7
- Catch unexpected exceptions when using PulseAudioClip in IcedTea
- New "protocol" parameter allowing JClicReports to run in https
- Changed logos and references of the Catalan Ministry of Education "Departament d'Educació" to "Departament d'Ensenyament" (main sponsor of the JClic project)
- Initial integration into the source code of the "Fressa" accessibility features, developed by Jordi Lagares. Some of this features will be operational in the next major release of JClic.
- Correction of bug: JClicAuthor hangs opening a file when an I/O error occurs
- Updated `build.xml`
- Event sounds converted to 16-bit PCM WAV format (instead of MP3) to avoid problems with OpenJDK
- Custom fonts now are rendered when JClic runs on Java 1,6 or later
- Java platform update: JClic requires Java 1.5 or later
- Many source files updated to be compliant with Java Generics
- Correction of bug: JClicReports hangs when activity or project name exceeds its maximum field length in database.

###Changes in 0.2.1.0 (25-Jan-2010)
---------------------------------------------------------------------------------------
- New and updated translations
- Windows installer: Added the parameter "-Xmx128m" into the command line of the JClic Author shortcut.
- Java WebStart: Added the parameter "max-heap-size=128m" to the JClic Author JNLP script.
- JClic AUthor: Memory management has been improved, specially in JClic AUthor, to avoid unexpected crashes. Now the java.awt.Image objects stored in the MediaBag are often cleaned, and reloaded only when needed. JClic player and Applet have also improved memory check and garbage collection.
- Fixed bug 129-326: Arabic and other right-to-left writing languages are now rendered correctly.
- JClic Author: Large images can be automatically scaled-down when imported into the media library. The maximum size is set by default to 800 x 600 pixels, but this setting can be changed in user's preferences.
- JClic Author: The cell properties editor allows to directly import images and other files into the media library (improvement suggested by Camille Manoury)
- JClic Author: When creating new activities, prompt again errors like empty or repeated name (improvement suggested by Camille Manoury)

###Changes in 0.2.0.6 (25-Feb-2009)
---------------------------------------------------------------------------------------
- New and upgraded translations
- Support for ISO-639-2 three-letter language codes, like Asturian (ast). Now this languages can be declared in src/utilities/edu/xtec/util/Messages.java in order to be listed in JClic settings dialog.
- Initial implementation of new accessibility features that make use of java.awt.Robot
- New button "Clear text styles" in JClic Author, to facilitate the cleaning of manually applied character attributes
- Unified base domain of external URLs to "clic.xtec.cat" instead of "clic.xtec.net". In the past, we have a merge of both.
- Changed the behavior of links of type "Go to activity". Now this links will preserve the user's navigation history, allowing to return and go back.
- Development tools: Forms are now edited with NetBeans 6.5
- ReportServer uses now the server's date and time to store session data, instead of the one reported by clients.
- Windows installer: now in 14 languages, built with NSIS-Unicode

###Changes in 0.2.0.5 (16-Jun-2008)
---------------------------------------------------------------------------------------
- Updated Basque and Russian translations

###Changes in 0.2.0.4 (11-Jun-2008)
---------------------------------------------------------------------------------------
This is a maintenance release.
- Added translations to new languages: Italian (full) and Russian (not yet finished)
- JClicAuthor can now run as an applet. Tha file dist/jclic/author.html contains an example of this modality.

###Changes in 0.2.0.3 (03-Apr-2008)
---------------------------------------------------------------------------------------
- Support for QuickTime 6.0 has been suppressed in JClic. Users of Mac OSX can still make use of QuickTime for Java 6.1 or later.
- JClic runs now on Java 1.4 or later. JRE 1.3.1 is no longer supported. This avoids the need of provide specific modules for the basic Java XML API (JAXP, DOM and Xalan), because they are already included in the Java 1.4 specification. JDOM 1.0 still remains as a external module needed by JClic.
- Changes in the source code, in order to make it fully compilable without external non-free dependencies: The binary files QTJava.zip, jclicjmf.jar and servlet.jar are no longer distributed with the source code of JClic, because of licensing issues. In order to make possible the build process, empty and partial stub implementations of QuickTime for Java, Java Media Framework and JSR-154 (Servlet 2.4) APIs have been included in the "lib" directory. All this stub implementations of the libraries are used only at compile time, and are not included in the binary files obtained as a result of the build process of JClic.
- ANT extensions are no longer needed to build JClic. The password for the key store file (needed to digitally sign the JAR files) is now provided through a include call to the file ~/.ant-global.properties.
- JDOM is now compiled without X-Path functions. This features, not used by JClic, require binary JAR files at compile-time.
- The file jmfhandlers.jar, used to avoid unnecessary http connections to the server during the initialization of Java Media Framework in applets, has been suppressed to avoid copyright conflicts.
- The GIF Encoder library by Rana Bhattacharyya, and the BMP importer by J.Osbaldeston, have been removed. Now the graphics presented in JClic Reports are dynamically generated in PNG format by the Java ImageIO library (included in Java 1.4). The import of BMP files is done by means of the Image4j library (http://image4j.sourceforge.net).
- Important changes have been done in the final packaging of JClic. Since this version, only three JAR files will be generated:
 * `jclic.jar` - Contains all the ingredients needed to run JClic Player and JClic Applet
 * `jclicauthor.jar` - To be used in conjunction with jclic.jar. Contains JClic Author.
 * `jclicreports.jar` - Also to be used with jclic.jar. Contains the stand-alone version of the JClic Reports Server.
- In addition to this three files, `jclicreports.war` and `jclicreports.ear` provide the Servlet version of JClic Reports Server.
- The build.xml file has been changed in many ways to reflect all this changes in the source code structure.
- Added two new skins: "Mini", for embedding JClic projects in blogs and other web pages with small space available, and "Empty", without buttons, counters nor message boxes.
- The cell marker is drawn only when there are more than one cell to solve. In multiple-answer mode, the first valid option is always displayed when the activity is finished, even if another valid option was entered (useful for natural spelling exercises).
- Applets can now handle project installer scripts (files with extension ".jclic.inst"). This allows to perform "one-click" installations of remote JClic projects without using Java WebStart. To use this feature, edit your project with JClicAuthor, generate the installer script (Menu "Tools-Create project installer...") and generate the applet ("Tools - Create web page..."). Then edit the "index.htm" file created in the last step, find the line that starts with "writePlugin" and change the project's file name ("yourproject.jclic.zip") by the installer one ("yourproject.jclic.inst").
- New translation system hosted in [Launchpad.net](https://translations.launchpad.net/jclic) and new translations: Basque, Greek, Danish and Turkish.

###Changes in 0.1.2.2 (23-Feb-2007)
---------------------------------------------------------------------------------------
- Corrected bug #41: "Conditional jumps between sequence elements are not always working as expected". Conditional jumps now are working also in the test window of JClic Author. This will simplify the design and testing of projects that make use of this feature.
- Corrected a bug in the algorithm that computes the global score.
- Corrected some minor mistakes detected in the German translation of JClic.

###Changes in 0.1.2.1 (23-Oct-2006)
---------------------------------------------------------------------------------------
Three main changes have been done:
- Feature request #68: In the reports system, the 'content-type' header of the HTTP responses is now "text/xml". This facilitates the integration with PHP-based applications, like Moodle. See http://projectes.lafarga.cat/projects/jclicmoodle for more information about a project that makes use of this feature.
- A new automation module, called "TagReplace", has been created. This module allows to substitute designed tags written in text elements by its associated values, indicated by means of an external text file or URL.
- Feature request #45: JClic Author can now transfer activities between different JClic projects. The "Import activities" feature allows to add to the current project activities taken from another one.

###Changes in 0.1.2.0 (22-May-2006)
---------------------------------------------------------------------------------------
This is primarily a bug fix release.
The following bugs have been fixed:
- 38: Data lost when editing crosswords. Was caused by a cleanup of excessive cells, incorrectly applied to Crossword activities.
- 51: No way to set dragCells property
- 52: No way to set UseOrder property. Now it's possible to set/unset this boolean properties from JClic author. The check boxes are in the "Options" tab of the Activity editor.
- 42: Changes to the parameters of conditional jumps are not retained. Now the edition of this properties, located in the "Sequences" tab of JClic author, work as expected.
- 83: Custom event sounds ignored in media dependency check. Now custom event sounds are correctly handled by JClic author, both when specified for the whole project or for a specific activity. In addition, changes to the project global skin are also shown in the test activity window.
- 103: User's password not saved in JClic reports. The "edit user" and "create user" forms have been modified, so now the password can be set in JClic Reports when creating a new user, and modified or erased later.
- 46: Windows installer create icons only for the current user. Now the installer defaults to create shortcuts and icons for all users.
