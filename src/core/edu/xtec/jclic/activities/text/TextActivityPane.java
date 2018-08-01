/*
 * File    : TextActivityPane.java
 * Created : 28-may-2001 17:19
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

package edu.xtec.jclic.activities.text;

import edu.xtec.jclic.*;
import edu.xtec.jclic.boxes.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.text.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class TextActivityPane extends javax.swing.JTextPane {

  TextActivityBase.Panel tabp;
  TextActivityBase tab;
  ActiveBox bx;

  /** Creates new TextActivityPane */
  public TextActivityPane(TextActivityBase.Panel tabp) {
    super();
    this.tabp = tabp;
    tab = (TextActivityBase) tabp.getActivity();
    bx = new ActiveBox(null, tabp, null);
    bx.setVisible(false);
    setEditorKit(new javax.swing.text.rtf.RTFEditorKit());
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
  }

  @Override
  protected void processEvent(AWTEvent e) {
    if (e instanceof MouseEvent) {
      if (!processMouse((MouseEvent) e)) {
        ((MouseEvent) e).consume();
        return;
      }
    }
    super.processEvent(e);
  }

  public boolean processMouse(MouseEvent e) {
    boolean result = true;
    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      if (tabp.showingPrevScreen) {
        tabp.ps.startActivity(tabp);
        result = false;
      } else if (tabp.isPlaying()) {
        if (bx.isVisible() && bx.contains(e.getPoint())) {
          if (bx.getContent().mediaContent != null)
            bx.playMedia(tabp.ps);
          result = false;
        }
      }
    }
    return result;
  }

  public void enableActiveBox(ActiveBoxContent abc, Point location) {
    if (abc.dimension != null) {
      bx.setContent(abc);
      bx.setBounds(new Rectangle(location, abc.dimension));
      bx.setVisible(true);
      bx.repaint();
    }
  }

  public void disableActiveBox() {
    bx.setVisible(false);
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    super.paint(g2);

    if (bx.isVisible()) {
      RenderingHints rh = g2.getRenderingHints();
      g2.setRenderingHints(Constants.DEFAULT_RENDERING_HINTS);
      while (true) {
        BoxBase.flagFontReduced = false;
        bx.update(g2, g2.getClipBounds(), this);
        if (!BoxBase.flagFontReduced)
          break;
      }
      g2.setRenderingHints(rh);
    }
  }

  @Override
  public void setStyledDocument(StyledDocument doc) {
    transferFocus();

    // JRE 1.4 workaround, part 1: Save a clean copy of the default style attributes
    Style defaultStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
    AttributeSet as = defaultStyle.copyAttributes();

    super.setStyledDocument(doc);

    // JRE 1.4 workaround, part 2: Restore lost attributes
    defaultStyle.addAttributes(as);

    setBackground(StyleConstants.getBackground(defaultStyle));
    Style st = tab.styleContext.getStyle(TextActivityDocument.TARGET);
    if (st != null)
      setCaretColor(StyleConstants.getForeground(st));
    requestFocus();
  }

  protected void targetChanged(TextTarget tt) {
  }
}
