/*
 * File    : MediaPreviewPanel.java
 * Created : 17-feb-2004 13:32
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2018 Francesc Busquets & Departament
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

package edu.xtec.jclic.bags;

import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.beans.ImgPanel;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.boxes.JPanelActiveBox;
import edu.xtec.jclic.media.MediaContent;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.util.Options;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class MediaPreviewPanel extends JPanel {

  public static final int PREFERRED_CMP_WIDTH = 600;
  public static final int PREFERRED_CMP_HEIGHT = 400;
  public static final String SAMPLE_TEXT = "ABCDEFGHIJ\nKLMNOPQRST\nUVWXYZ\nabcdefghij\nklmnopqrst\nuvwxyz\n0123456789\n";

  MediaBagElementEditor mbed;
  Options options;
  PlayStation ps;
  int mediaType;

  /** Creates a new instance of MediaPreviewPanel */
  public MediaPreviewPanel(MediaBagElementEditor mbed, Options options) {
    super();
    this.mbed = mbed;
    this.options = options;
    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
    if (mbed != null) {
      JComponent cmp = null;
      mediaType = Utils.getFileType(mbed.getMediaBagElement().getFileName());
      switch (mediaType) {
      case Utils.TYPE_IMAGE:
        cmp = buildImgComponent();
        break;
      case Utils.TYPE_AUDIO:
      case Utils.TYPE_MIDI:
      case Utils.TYPE_ANIM:
      case Utils.TYPE_VIDEO:
        cmp = buildMediaComponent();
        break;
      case Utils.TYPE_FONT:
        cmp = buildFontComponent();
        break;
      default:
        break;
      }
      if (cmp != null)
        add(cmp);
    }
  }

  public void end() {
    if (ps != null)
      ps.stopMedia(1);
  }

  protected JComponent buildImgComponent() {
    Image img = null;
    JComponent result = null;
    try {
      img = mbed.getMediaBagElement().prepareAndGetImage(mbed.getMediaBag().getProject().getFileSystem());
    } catch (Exception ex) {
      System.err.println("Error loading image " + mbed.getMediaBagElement().getFileName());
    }
    if (img != null) {
      result = new JScrollPane(new ImgPanel(new ImageIcon(img)));
      result.setPreferredSize(new Dimension(PREFERRED_CMP_WIDTH, PREFERRED_CMP_HEIGHT));
    }
    return result;
  }

  protected JComponent buildFontComponent() {
    Object o = mbed.getMediaBagElement().getData();
    JTextArea textArea = new JTextArea();

    if (o instanceof Font) {
      textArea.setFont(((Font) o).deriveFont(Font.PLAIN, 48));
      textArea.setText(SAMPLE_TEXT);
    } else {
      textArea.setText(options.getMsg("edit_media_font_error"));
    }
    return new JScrollPane(textArea);
  }

  protected JComponent buildMediaComponent() {
    ActiveBoxContent abc = new ActiveBoxContent();
    abc.mediaContent = new MediaContent();
    int mt = MediaContent.UNKNOWN;
    switch (mediaType) {
    case Utils.TYPE_AUDIO:
      mt = MediaContent.PLAY_AUDIO;
      break;
    case Utils.TYPE_MIDI:
      mt = MediaContent.PLAY_MIDI;
      break;
    case Utils.TYPE_ANIM:
    case Utils.TYPE_VIDEO:
      mt = MediaContent.PLAY_VIDEO;
      break;
    }
    abc.mediaContent.mediaType = mt;
    abc.mediaContent.mediaFileName = mbed.getMediaBagElement().getFileName();
    abc.mediaContent.stretch = true;
    abc.mediaContent.free = false;
    abc.dimension = new Dimension(PREFERRED_CMP_WIDTH, PREFERRED_CMP_HEIGHT);
    JPanelActiveBox jpab = new JPanelActiveBox(null, null, this);
    JClicProjectEditor prjed = (JClicProjectEditor) mbed.getFirstParent(JClicProjectEditor.class);
    if (prjed != null && prjed.getTestPlayerContainer() != null) {
      ps = prjed.getTestPlayerContainer().getTestPlayer();
      jpab.setPlayStation(ps);
    }
    jpab.setActiveBoxContent(abc);
    try {
      abc.realizeContent(mbed.getMediaBag());
    } catch (Exception ex) {
      System.err.println("Error: " + ex);
    }
    JScrollPane result = new JScrollPane(jpab);
    result.setPreferredSize(new Dimension(PREFERRED_CMP_WIDTH, PREFERRED_CMP_HEIGHT));
    return result;
  }
}
