/*
 * File    : GradientButton.java
 * Created : 27-sep-2002 11:36
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

package edu.xtec.jclic.beans;

import edu.xtec.jclic.misc.Gradient;
import edu.xtec.jclic.misc.GradientEditor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.JButton;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class GradientButton extends NullableObject {

  public static final String PROP_GRADIENT = "gradient";

  /** Creates a new instance of GradientPanel */
  public GradientButton() {
    super();
  }

  @Override
  protected String getObjectType() {
    return PROP_GRADIENT;
  }

  public Gradient getGradient() {
    return (Gradient) getObject();
  }

  public void setGradient(Gradient g) {
    setObject(g);
  }

  @Override
  protected AbstractButton buildButton() {
    return new JButton() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Gradient gradient = getGradient();
        if (gradient != null) {
          Rectangle r = new Rectangle(0, 0, getWidth(), getHeight());
          r.grow(-3, -3);
          Graphics2D g2 = (Graphics2D) g;
          RenderingHints rh = g2.getRenderingHints();
          g2.setRenderingHints(edu.xtec.jclic.Constants.DEFAULT_RENDERING_HINTS);
          gradient.paint(g2, r);
          g2.setRenderingHints(rh);
        }
      }
    };
  }

  @Override
  protected Object createObject() {
    return new Gradient();
  }

  @Override
  protected Object editObject(Object o) {
    Gradient g = (Gradient) (o == null ? createObject() : o);
    return GradientEditor.getGradient(g, this, options);
  }
}
