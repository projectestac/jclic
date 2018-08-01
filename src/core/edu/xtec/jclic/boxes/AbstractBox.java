/*
 * File    : AbstractBox.java
 * Created : 12-dec-2000 10:55
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

package edu.xtec.jclic.boxes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

/**
 * This abstract class is the base for most graphic components of JClic. It
 * describes an {@link java.awt.geom.Area} (a rectangle by default) with some
 * special properties that determine how it must be drawn on screen. Some types
 * of boxes can act as containers for other boxes, establishing a hierarchy of
 * dependences. Box objects are always placed into a
 * {@link javax.swing.JComponent}.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public abstract class AbstractBox extends Rectangle2D.Double implements Cloneable {

  private AbstractBox parent;
  private JComponent container;
  private BoxBase boxBase;
  protected boolean border;
  protected Shape shape;
  protected boolean specialShape;
  private boolean visible;
  private boolean temporaryHidden;
  private boolean inactive;
  private boolean inverted;
  private boolean alternative;
  private boolean marked;
  private boolean focused;
  private Component hostedComponent;

  public AbstractBox(AbstractBox parent, JComponent container, BoxBase boxBase) {
    this.parent = parent;
    this.container = container;
    shape = this;
    specialShape = false;
    this.boxBase = boxBase;
    visible = true;
  }

  @Override
  public Object clone() {
    AbstractBox bx = (AbstractBox) super.clone();
    if (specialShape)
      bx.shape = new Area(shape);
    else
      bx.shape = bx;
    if (hostedComponent != null) {
      if (hostedComponent instanceof JLabel) {
        JLabel lb = (JLabel) hostedComponent;
        bx.hostedComponent = new JLabel(lb.getText(), lb.getIcon(), lb.getHorizontalAlignment());
        JComponent jc = bx.getContainerResolve();
        if (jc != null)
          jc.add(hostedComponent);
      } else
        bx.hostedComponent = null;
    }
    return bx;
  }

  public void setParent(AbstractBox parent) {
    this.parent = parent;
  }

  public AbstractBox getParent() {
    return parent;
  }

  public void end() {
    setHostedComponent(null);
  }

  @Override
  public void finalize() throws Throwable {
    end();
    super.finalize();
  }

  public void setContainer(JComponent newContainer) {
    container = newContainer;
    if (hostedComponent != null) {
      if (hostedComponent.getParent() != null) {
        hostedComponent.getParent().remove(hostedComponent);
      }
      if (container != null)
        container.add(hostedComponent);
    }
  }

  public JComponent getContainerX() {
    return container;
  }

  public JComponent getContainerResolve() {
    AbstractBox ab = this;
    while (ab.container == null && ab.parent != null)
      ab = ab.parent;
    return ab.container;
  }

  public void setBoxBase(BoxBase boxBase) {
    this.boxBase = boxBase;
    repaint();
    setHostedComponentColors();
    setHostedComponentBorder();
  }

  public BoxBase getBoxBaseResolve() {
    AbstractBox ab = this;
    while (ab.boxBase == null && ab.parent != null)
      ab = ab.parent;
    return ab.boxBase == null ? BoxBase.DEFAULT_BOX_BASE : ab.boxBase;
  }

  public BoxBase getBoxBaseX() {
    return boxBase;
  }

  public void setHostedComponent(Component jc) {
    if (hostedComponent != null) {
      if (hostedComponent.getParent() != null) {
        hostedComponent.getParent().remove(hostedComponent);
      }
    }
    hostedComponent = jc;
    if (hostedComponent != null) {
      hostedComponent.setVisible(false);
      JComponent cmp = getContainerResolve();
      if (cmp != null)
        cmp.add(jc);
      setHostedComponentColors();
      setHostedComponentBorder();
      setHostedComponentBounds();
      hostedComponent.setVisible(visible);
    }
  }

  public Component getHostedComponent() {
    return hostedComponent;
  }

  public void setShape(Shape setSh) {
    shape = setSh;
    specialShape = true;
    repaint();
    super.setRect(shape.getBounds2D());
    setHostedComponentBounds();
    repaint();
  }

  public Shape getShape() {
    return shape;
  }

  @Override
  public boolean contains(Point2D p) {
    return shape == this ? super.contains(p) : shape.contains(p);
  }

  public boolean update(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io) {

    if (hostedComponent != null)
      return false;
    if (isEmpty() || !isVisible() || isTemporaryHidden())
      return false;
    if (dirtyRegion != null && !shape.intersects(dirtyRegion))
      return false;

    Shape saveClip = new Area(g2.getClip());
    Area clip = new Area(saveClip);
    clip.intersect(new Area(shape));
    g2.setClip(clip);

    BoxBase bb = getBoxBaseResolve();
    if (!bb.transparent) {
      if (bb.bgGradient == null || bb.bgGradient.hasTransparency()) {
        g2.setColor(inactive ? bb.inactiveColor : inverted ? bb.textColor : bb.backColor);
        g2.fill(shape);
        g2.setColor(Color.black);
      }
      if (bb.bgGradient != null)
        bb.bgGradient.paint(g2, shape);
    }
    updateContent(g2, dirtyRegion, io);

    g2.setClip(saveClip);

    drawBorder(g2);
    return true;
  }

  public abstract boolean updateContent(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io);

  protected void drawBorder(Graphics2D g2) {
    if (border || marked || focused) {
      BoxBase bb = getBoxBaseResolve();
      g2.setColor(bb.borderColor);
      g2.setStroke((marked || focused) ? bb.getMarker() : bb.getBorder());
      if (marked || focused)
        g2.setXORMode(Color.white);
      g2.draw(shape);
      if (marked || focused)
        g2.setPaintMode();
      g2.setColor(Color.black);
      g2.setStroke(BoxBase.DEFAULT_STROKE);
    }
  }

  public Rectangle getBorderBounds() {
    if (!border && !marked && !focused)
      return getBounds();
    BoxBase bb = getBoxBaseResolve();
    Stroke strk = (marked || focused) ? bb.getMarker() : bb.getBorder();
    return strk.createStrokedShape(shape).getBounds();
  }

  public boolean hasBorder() {
    return border;
  }

  public void setBorder(boolean newVal) {
    if (!newVal)
      repaint();
    border = newVal;
    setHostedComponentBorder();
    if (newVal)
      repaint();
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean newVal) {
    visible = newVal;
    if (hostedComponent != null)
      hostedComponent.setVisible(newVal);
    repaint();
  }

  public boolean isTemporaryHidden() {
    return temporaryHidden;
  }

  public void setTemporaryHidden(boolean newVal) {
    temporaryHidden = newVal;
  }

  public boolean isInactive() {
    return inactive;
  }

  public void setInactive(boolean newVal) {
    inactive = newVal;
    checkHostedComponent();
    setHostedComponentColors();
    repaint();
  }

  public boolean isInverted() {
    return inverted;
  }

  public void setInverted(boolean newVal) {
    inverted = newVal;
    checkHostedComponent();
    setHostedComponentColors();
    repaint();
  }

  public boolean isMarked() {
    return marked;
  }

  public void setMarked(boolean newVal) {
    if (!newVal)
      repaint();
    marked = newVal;
    checkHostedComponent();
    setHostedComponentColors();
    if (newVal)
      repaint();
  }

  public boolean isFocused() {
    return focused;
  }

  public void setFocused(boolean newVal) {
    if (!newVal)
      repaint();
    focused = newVal;
    checkHostedComponent();
    setHostedComponentColors();
    if (newVal)
      repaint();
  }

  public boolean isAlternative() {
    return alternative;
  }

  public void setAlternative(boolean newVal) {
    alternative = newVal;
    checkHostedComponent();
    setHostedComponentColors();
    repaint();
  }

  private void setHostedComponentColors() {
    if (hostedComponent != null) {
      BoxBase bb = getBoxBaseResolve();
      hostedComponent.setFont(bb.getFont());
      hostedComponent.setBackground(inactive ? bb.inactiveColor : inverted ? bb.textColor : bb.backColor);
      hostedComponent.setForeground(inverted ? bb.backColor : alternative ? bb.alternativeColor : bb.textColor);
      if (hostedComponent instanceof JComponent) {
        ((JComponent) hostedComponent).setOpaque(true);
        if (hostedComponent instanceof JTextComponent)
          ((JTextComponent) hostedComponent).setCaretColor(bb.textColor);
      }
    }
  }

  private void setHostedComponentBorder() {
    if (hostedComponent != null && hostedComponent instanceof JComponent) {
      ((JComponent) hostedComponent)
          .setBorder(border ? BorderFactory.createLineBorder(getBoxBaseResolve().borderColor, 1)
              : BorderFactory.createEmptyBorder());
    }
  }

  private void setHostedComponentBounds() {
    if (hostedComponent != null) {
      Rectangle r = getBounds();
      hostedComponent.setBounds(r);
      if (hostedComponent instanceof JComponent)
        ((JComponent) hostedComponent).setPreferredSize(r.getSize());
    }
  }

  public void repaint() {
    JComponent jc = getContainerResolve();
    if (jc != null)
      jc.repaint(getBorderBounds());
    if (hostedComponent != null)
      hostedComponent.repaint();
  }

  public void setBounds(Rectangle2D r) {
    if (r.equals((Rectangle2D) this))
      return;

    if (specialShape) {
      AffineTransform tx = null;
      if (getWidth() != r.getWidth() || getHeight() != r.getHeight()) {
        tx = AffineTransform.getTranslateInstance(-getX(), -getY());
        shape = tx.createTransformedShape(shape);
        tx = AffineTransform.getScaleInstance(r.getWidth() / getWidth(), r.getHeight() / getHeight());
        shape = tx.createTransformedShape(shape);
        tx = AffineTransform.getTranslateInstance(getX(), getY());
        shape = tx.createTransformedShape(shape);
      }
      if (getX() != r.getX() || getY() != r.getY()) {
        tx = AffineTransform.getTranslateInstance(r.getX() - getX(), r.getY() - getY());
        shape = tx.createTransformedShape(shape);
      }
      if (tx != null) {
        setShape(shape);
      }
    } else {
      repaint();
      setRect(r);
      repaint();
    }
    setHostedComponentBounds();
  }

  public void setBounds(double newX, double newY, double newWidth, double newHeight) {
    setBounds(new Double(newX, newY, newWidth, newHeight));
    setHostedComponentBounds();
  }

  public void setLocation(Point2D p) {
    setBounds(p.getX(), p.getY(), width, height);
  }

  public void setLocation(double newX, double newY) {
    setBounds(newX, newY, width, height);
  }

  public void translate(double dx, double dy) {
    setBounds(x + dx, y + dy, width, height);
  }

  public void setSize(Dimension2D d) {
    setBounds(x, y, d.getWidth(), d.getHeight());
  }

  public void setSize(double newWidth, double newHeight) {
    setBounds(x, y, newWidth, newHeight);
  }

  public Point2D getLocation() {
    return new java.awt.geom.Point2D.Double(x, y);
  }

  protected void checkHostedComponent() {
    // to be overrided
  }

  public Point2D getAbsoluteLocation() {
    Point2D result = null;
    JComponent jc = getContainerResolve();
    if (jc != null) {
      result = new java.awt.Point(jc.getLocationOnScreen());
      result.setLocation(result.getX() + getX(), result.getY() + getY());
    }
    return result;
  }

  public Point2D getAbsoulteMiddleLocation() {
    Point2D result = getAbsoluteLocation();
    if (result != null) {
      result.setLocation(result.getX() + getWidth() / 2, result.getY() + getHeight() / 2);
    }
    return result;
  }
}
