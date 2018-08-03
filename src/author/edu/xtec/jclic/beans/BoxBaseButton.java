/*
 * File    : BoxBaseButton.java
 * Created : 12-oct-2002 18:51
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

package edu.xtec.jclic.beans;

import edu.xtec.jclic.boxes.AbstractBox;
import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.jclic.boxes.BoxBaseEditor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.AbstractButton;
import javax.swing.JButton;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class BoxBaseButton extends NullableObject {

  public static final String PROP_BOX_BASE = "boxBase";
  public static final String SAMPLE_STR = "abc";

  AbstractBox preview;

  /** Creates a new instance of BoxBasePanel */
  /*
   * public BoxBaseButton(Options options, String tooltipKey) { super(options,
   * tooltipKey); }
   */
  public BoxBaseButton() {
    super();
  }

  @Override
  protected String getObjectType() {
    return PROP_BOX_BASE;
  }

  public BoxBase getBoxBase() {
    return (BoxBase) getObject();
  }

  public void setBoxBase(BoxBase bb) {
    setObject(bb);
  }

  @Override
  public void setObject(Object value) {
    super.setObject(value);
    /*
     * if(nullValue || object==null){
     * button.setBackground(UIManager.getColor("Button.background"));
     * button.setForeground(UIManager.getColor("Button.foreground"));
     * button.setText(""); } else{ BoxBase bb=(BoxBase)value;
     * button.setBackground(bb.backColor); button.setForeground(bb.textColor);
     * button.setText("S"); }
     */
  }

  @Override
  protected AbstractButton buildButton() {
    return new JButton() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BoxBase bb = getBoxBase();
        if (bb != null) {
          java.awt.Rectangle r = new java.awt.Rectangle(0, 0, getWidth(), getHeight());
          r.grow(-3, -3);
          g.setColor(bb.backColor);
          g.fillRect(r.x, r.y, r.width, r.height);
          g.setColor(bb.textColor);
          FontMetrics fm = g.getFontMetrics();
          int y = r.y + r.height - (r.height - fm.getAscent()) / 2 - fm.getDescent();
          int x = r.x + (r.width - fm.stringWidth(SAMPLE_STR)) / 2;
          g.drawString(SAMPLE_STR, x, y);
        }
      }
    };
  }

  public void setPreview(AbstractBox preview) {
    this.preview = preview;
  }

  @Override
  protected Object createObject() {
    return new BoxBase();
  }

  @Override
  protected Object editObject(Object o) {
    BoxBase bb = (BoxBase) (o == null ? createObject() : o);
    return BoxBaseEditor.getBoxBase(bb, this, options, preview);
  }
}
