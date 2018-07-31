/*
 * File    : Check.java
 * Created : 09-jul-2002 10:06
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2005 Francesc Busquets & Departament
 * d'Educacio de la Generalitat de Catalunya
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (see the LICENSE file).
 */

package edu.xtec.util;

import java.awt.Dimension;
import java.net.URL;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * This class is useful to display a warning message when a specific requirement is not satisfied.
 * Examples of unsatisfied requirements are the ability to instantiate a specific class, to access
 * to a file or resource, or to have specific permissions. The message can be stored into an HTML
 * resource, and will be displayed in a {@link javax.swing.JEditorPane}. Users will be able to
 * follow the links included in the HTML document.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class Check {

  private static final String DLG_KEY = "CHECK_DIALOG";

  private Check() {}

  public static void showUrlPane(final Options options, String urlKey) {
    final Messages msg = options.getMessages();
    HyperlinkListener hlst =
        new HyperlinkListener() {
          public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              final URL sUrl = e.getURL();
              SwingUtilities.invokeLater(
                  new Runnable() {
                    public void run() {
                      try {
                        if (options.getApplet() != null)
                          options.getApplet().getAppletContext().showDocument(sUrl);
                        else BrowserLauncher.openURL(sUrl.toExternalForm());
                      } catch (Exception ex) {
                        msg.showErrorWarning(
                            (JDialog) options.get(DLG_KEY), "URL_LAUNCH_ERROR", ex);
                      }
                    }
                  });
            }
          }
        };

    JScrollPane scroll;
    try {
      URL url = ResourceManager.getResource(msg.get(urlKey));
      JEditorPane ep = new JEditorPane(url);
      ep.setEditable(false);
      ep.addHyperlinkListener(hlst);
      scroll = new JScrollPane(ep);
      scroll.setPreferredSize(new Dimension(500, 400));
    } catch (Exception ex) {
      msg.showErrorWarning(options.getMainComponent(), "URL_ERROR", ex);
      return;
    }

    JOptionPane pane = new JOptionPane(new Object[] {scroll}, JOptionPane.WARNING_MESSAGE);
    JDialog dialog = pane.createDialog(options.getMainComponent(), msg.get(Messages.WARNING));
    options.put(DLG_KEY, dialog);
    dialog.setVisible(true);
    options.remove(DLG_KEY);
  }

  public static boolean checkSignature(Options options, boolean showWarning) {
    boolean result = false;
    try {
      System.getProperty("java.class.path");
      result = true;
    } catch (SecurityException ex) {
      if (showWarning) showUrlPane(options, "CHECK_SIGNATURE_URL");
    }
    return result;
  }
}
