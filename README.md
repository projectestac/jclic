[JClic](http://clic.xtec.cat) - Player and authoring tool for educational activities
====================================================================================

JClic is a set of cross-platform Java applications useful for creating and carrying out different types of educational activities like puzzles, associations, text exercises or crosswords.

JClic is an open source project of the Department of Education of the Government of Catalonia. Teachers from different countries have contributed since 1995 to create a big repository of educational activities, shared under Creative Commons licenses in the project's main site: [http://clic.xtec.cat](http://clic.xtec.cat)

The current components of JClic are:

- **JClic Player**: Allows students to play with the activities and, optionally, track reports of their work in a local or remote database. The activities are organized into “projects” (files with extension .jclic.zip), and projects can be grouped into “Libraries”.
- **JClic Applet**: Variant of JClic Player that can run embedded in HTML documents. Currently not used because lack of Java Applet support in modern browsers.
- **JClic Author**: Visual tool used by teachers and authors to create or modify activities and projects.
- **JClic Reports**: Reporting tool designed to collect and display the results (time, tries, guesses, success...) achieved by the students while playing JClic activities.
- **JClic module for Moodle**: Is a parallel project specifically designed for embedding JClic activities into the Moodle Virtual Learning Environment. This project is available in: [https://github.com/projectestac/moodle-mod_jclic](https://github.com/projectestac/moodle-mod_jclic)

See INSTALL.txt for compilation instructions, and HACKING.txt if you want to set-up your own JClic development project with [Netbeans](http://netbeans.org).

JClic needs Java 1.7 or later (currently supported only in GNU/Linux, Windows and Mac OS X)

The new project **[JClic.js](http://projectestac.github.io/jclic.js/)** allows to play JClic activities in GNU/Linux, Mac, Windows, ChromeOS, Android, iOS...  and any operating system with a web browser supporting HTML5, without the need of Java.

