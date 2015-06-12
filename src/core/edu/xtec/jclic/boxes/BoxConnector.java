/*
 * File    : BoxConector.java
 * Created : 14-dec-2000 10:40
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

import edu.xtec.util.Options;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import javax.swing.JComponent;

/**
 * <CODE>BoxConnector</CODE> allows users to visually connect two {@link edu.xtec.jclic.boxes.ActiveBox}
 * objects in a {@link edu.xtec.jclic.Activity.Panel}. There are two modes of operation:
 * drawing a line between an origin point (usually the point where the user clicks on)
 * and a destination point, or dragging the box from one location to another. The lines can
 * have arrows at its ending.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class BoxConnector extends Object {
    
    public static final float LINE_WIDTH=1.5F;
    public static final BasicStroke BASIC_STROKE=new BasicStroke(LINE_WIDTH);
    public static final Color DEFAULT_LINE_COLOR=Color.black;
    public static final Color DEFAULT_XOR_COLOR=Color.white;
    public static final double ARROW_ANGLE=Math.PI/6;
    public static final double ARROW_L=10.0;
    
    public static boolean USE_XOR=true;
    public static boolean GROW_BUG=false;
    
    public Point2D origin;
    public Point2D dest;
    public boolean arrow;
    public boolean active=false;
    public boolean linePainted=false;
    public double arrow_l=ARROW_L;
    public double arrow_angle=ARROW_ANGLE;
    public Color lineColor=DEFAULT_LINE_COLOR;
    public Color xorColor=DEFAULT_XOR_COLOR;
    Point2D relativePos;
    ActiveBox bx;
    JComponent parent;
    public float line_width=LINE_WIDTH;
    
    
    /** Creates new BoxConnector */
    public BoxConnector(JComponent setParent) {
        parent=setParent;
        origin=new Point2D.Double();
        dest=new Point2D.Double();
        arrow=false;
        active=false;
        linePainted=false;
        relativePos=new Point2D.Double();
    }
    
    public boolean update(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io){
        if(!active) return false;
        if(bx!=null){
            bx.setTemporaryHidden(false);
            bx.update(g2, dirtyRegion, io);
            bx.setTemporaryHidden(true);
        }
        else{
            drawLine(g2);
            linePainted=true;
        }
        return true;
    }
    
    public void drawLine(Graphics2D g2){
        if(active)
            drawLine(g2, origin, dest, arrow, lineColor, xorColor,
            arrow_l, arrow_angle, line_width);
    }
    
    public void moveBy(double dx, double dy){
        moveTo(new Point2D.Double(dest.getX()+dx, dest.getY()+dy));
    }
    
    public void moveTo(Point2D p){
        moveTo(p, false);
    }
    
    public void moveTo(Point2D p, boolean forcePaint){
        Rectangle clipRect;
        
        if(!active || (forcePaint==false && dest.equals(p)))
            return;
        
        if(bx!=null){
            clipRect=new Rectangle(
            (int)(p.getX()-relativePos.getX()),
            (int)(p.getY()-relativePos.getY()),
            (int)bx.width,
            (int)bx.height);
            clipRect.add(bx);
            bx.setLocation(new Point2D.Double(p.getX()-relativePos.getX(), 
                                              p.getY()-relativePos.getY()));
        }
        else{
            if(forcePaint || !USE_XOR){
                clipRect=new Rectangle((int)origin.getX(), (int)origin.getY(), 0, 0);
                clipRect.add(p);
                clipRect.add(dest);
                dest.setLocation(p);
            }
            else{
                Graphics2D g2=(Graphics2D)parent.getGraphics();
                if(linePainted){
                    drawLine(g2);
                }
                dest.setLocation(p);
                drawLine(g2);
                linePainted=true;
                return;
            }
        }
        growRect(clipRect, arrow ? (int)arrow_l : 1, arrow ? (int)arrow_l : 1);
        parent.repaint(clipRect);
    }
    
    public void begin(Point2D p){
        if(active) 
            end();
        origin.setLocation(p);
        dest.setLocation(p);
        linePainted=false;
        active=true;
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    public void begin(Point2D p, ActiveBox setBox){
        begin(p);
        bx=setBox;
        relativePos.setLocation(p.getX()-bx.x, p.getY()-bx.y);
        bx.setTemporaryHidden(true);
        Rectangle r = new Rectangle(bx.getBounds());
        growRect(r, 1, 1);
        linePainted=false;
        parent.repaint(r);
    }
    
    public ActiveBox getBox(){
        return bx;
    }
    
    public void end(){
        if(!active) return;
        if(bx!=null){
            Rectangle r = new Rectangle(bx.getBounds());
            growRect(r, 1, 1);
            parent.repaint(r);
            bx.setLocation(origin.getX()-relativePos.getX(), 
                           origin.getY()-relativePos.getY());
            bx.setTemporaryHidden(false);
            r.setBounds(bx.getBounds());
            growRect(r, 1, 1);
            parent.repaint(r);
            bx=null;
            relativePos.setLocation(0, 0);
        }
        else {
            moveTo(dest, true);
        }
        active=false;
        linePainted=false;
        parent.setCursor(null);
    }
    
    public static void drawLine(Graphics2D g2, Point2D origin, Point2D dest, boolean arrow){
        drawLine(g2, origin, dest, arrow, DEFAULT_LINE_COLOR, DEFAULT_XOR_COLOR, 
                 ARROW_L, ARROW_ANGLE, LINE_WIDTH);
    }
    
    public static void drawLine(Graphics2D g2, Point2D origin, Point2D dest, 
                                boolean arrow, Color color, Color xorColor, 
                                double arrow_l, double arrowAngle, float strokeWidth){
        Stroke oldStroke=g2.getStroke();
        Object oldStrokeHint=g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g2.setColor(color);
        if(USE_XOR && xorColor!=null)
            g2.setXORMode(xorColor);
        g2.setStroke(strokeWidth==LINE_WIDTH ? BASIC_STROKE : new BasicStroke(strokeWidth));
        g2.drawLine((int)origin.getX(), (int)origin.getY(), (int)dest.getX(), (int)dest.getY());
        if(arrow){
            double beta= Math.atan2(origin.getY()-dest.getY(), dest.getX()-origin.getX());
            Point2D arp=new Point2D.Double(dest.getX()-arrow_l*Math.cos(beta+arrowAngle), 
                                           dest.getY()+arrow_l*Math.sin(beta+arrowAngle));
            g2.drawLine((int)dest.getX(), (int)dest.getY(), (int)arp.getX(), (int)arp.getY());
            arp.setLocation(dest.getX()-arrow_l*Math.cos(beta-arrowAngle), 
                            dest.getY()+arrow_l*Math.sin(beta-arrowAngle));
            g2.drawLine((int)dest.getX(), (int)dest.getY(), (int)arp.getX(), (int)arp.getY());
        }
        if(USE_XOR && xorColor!=null)
            g2.setPaintMode();
        g2.setStroke(oldStroke);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldStrokeHint);
    }
    
    public static void checkOptions(Options options){
        if(options.getBoolean(Options.JAVA141) && options.getBoolean(Options.MAC)){
            USE_XOR=false;
        }
    }
    
    public static void growRect(Rectangle r, int h, int w){
        if(GROW_BUG){
            r.x-=w;
            r.width+=2*w;
            r.y-=h;
            r.height+=2*h;
        }
        else{
            r.grow(h, w);
        }
    }
        
    public static Color getXORColor(Color src){
        return getXORColor(src, Color.white);
    }
    
    public static Color getXORColor(Color src, Color against){
        return new Color(src.getRGB()^against.getRGB());
    }
}