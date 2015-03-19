/*
 * File    : ThumbsExplorerPanel.java
 * Created : 11-feb-2004 11:44
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

import edu.xtec.util.LFUtil;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class is a {@link javax.swing.JPanel} that contains a set of
 * {@link edu.xtec.jclic.boxes.ThumbsExplorerPanel.ThumbElement} objects. Elements are
 * displayed into a grid of rectangular cells of same size, distributed from left to
 * right and top to bottom. It provides methods to select a specific ThumbElement, and
 * has a {@link javax.swing.event.EventListenerList} used to notify observers about
 * selection changes.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.13
 */
public class ThumbsExplorerPanel extends JPanel {
    
    public static final int DEFAULT_THUMB_WIDTH=90;
    public static final int DEFAULT_THUMB_HEIGHT=90;
    public static final int DEFAULT_THUMB_MARGIN=14;
    public static final int DEFAULT_TEXT_HEIGHT=14;
    public static final int DEFAULT_THUMB_INTERNAL_MARGIN=2;
    
    protected static BasicStroke BORDER_STROKE=new BasicStroke(0.2F);
    protected int th_width, th_height, th_margin, th_textHeight, th_int_margin;
    protected Font font;
    
    private List<ThumbElement> elements;
    private ThumbElement current;
    private int elementsPerRow;
    
    private EventListenerList listenersList;
    public Dimension maxThumbSize;
    public Dimension boxSize;
    
    /** Creates a new instance of ThumbsExplorerPanel  */
    public ThumbsExplorerPanel() {
        setLayout(null);
        elements=new ArrayList<ThumbElement>();
        listenersList=new EventListenerList();
        setSizes(DEFAULT_THUMB_WIDTH, DEFAULT_THUMB_HEIGHT,
        DEFAULT_THUMB_MARGIN, DEFAULT_TEXT_HEIGHT, DEFAULT_THUMB_INTERNAL_MARGIN);
        setPreferredSize(new Dimension(3*boxSize.width, boxSize.height));
        setFont(new Font("Dialog", Font.PLAIN, 12));
        setBackground(LFUtil.getColor("Table.background",  Color.lightGray));
        elementsPerRow=1;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }
    
    public void setSizes(int th_width, int th_height, int th_margin, int th_textHeight, int th_int_margin){
        this.th_width=Math.max(10, th_width);
        this.th_height=Math.max(10, th_height);
        this.th_margin=Math.max(5, th_margin);
        this.th_textHeight=Math.max(10, th_textHeight);
        this.th_int_margin=Math.max(0, th_int_margin);
        boxSize=new Dimension(th_width+2*th_margin, th_height+th_textHeight+2*th_margin);
        maxThumbSize=new Dimension(th_width-2*th_int_margin, th_height-2*th_int_margin);
        for(int i=0; i<elements.size(); i++){
            getThumbElement(i).sizeImage();
        }
        invalidate();
    }
    
    public void resizeTo(int desiredWidth){
        int numElements=elements.size();
        int w=Math.max(boxSize.width, desiredWidth);
        int bpr=w/boxSize.width;
        int numRows=(numElements/bpr)+1;
        int h=numRows*boxSize.height;
        setPreferredSize(new Dimension(w, h));
        revalidate();
        repaint();
    }
    
    public ThumbElement getThumbElement(int p){
        return elements.get(p);
    }
    
    public ThumbElement[] getThumbElements(){
        return elements.toArray(new ThumbElement[elements.size()]);
    }
    
    public ThumbElement getElementFor(Object object){
        ThumbElement result=null;
        for(int i=0; i<elements.size(); i++){
            if(getThumbElement(i).getUserObject()==object){
                result=getThumbElement(i);
                break;
            }
        }
        return result;
    }
    
    public int getThumbElementCount(){
        return elements.size();
    }
    
    public void removeThumbElementAt(int p){
        if(elements!=null){
            ThumbElement o=elements.get(p);
            if(o!=null){
                if(o==current)
                    current=null;
                elements.remove(p);
                invalidate();
            }
        }
    }
    
    public void removeAllThumbElements(){
        elements.clear();
        current=null;
        invalidate();
        repaint();
    }
    
    public ThumbElement addThumbElement(int p, Object userObject, ImageIcon img, String text){
        ThumbElement th=new ThumbElement(userObject, img, text);
        if(p>=0)
            elements.add(p, th);
        else
            elements.add(th);
        invalidate();
        return th;
    }
    
    public ThumbElement addThumbElement(Object userObject, ImageIcon img, String text){
        return addThumbElement(-1, userObject, img, text);
    }
    
    /** Getter for property current.
     * @return Value of property current.
     *
     */
    public ThumbElement getCurrent() {
        return current;
    }
    
    public Object getCurrentObject(){
        return current==null ? null : current.getUserObject();
    }
    
    /** Setter for property current.
     * @param th New value of {@link #current}.
     *
     */
    public void setCurrent(ThumbElement th) {
        if(current!=null){
            Rectangle r=getRectFor(getIndexOf(current));
            if(r!=null)
                repaint(r);
        }
        if(th!=null && elements.contains(th)){
            current = th;
            Rectangle r=getRectFor(getIndexOf(current));
            if(r!=null)
                repaint(r);
            scrollRectToVisible(r);
        }
        else
            current=null;
    }
    
    public void checkCurrentVisibility() {
        if(current!=null){
            Rectangle r=getRectFor(getIndexOf(current));
            if(r!=null)
                scrollRectToVisible(r);
        }
    }
    
    public void setCurrentObject(Object o){
        ThumbElement th=null;
        for(int i=0; i<elements.size(); i++){
            if(getThumbElement(i).getUserObject()==o){
                th=getThumbElement(i);
                break;
            }
        }
        setCurrent(th);
    }
    
    /** Getter for property font.
     * @return Value of property font.
     *
     */
    @Override
    public Font getFont() {
        return font;
    }
    
    /** Setter for property font.
     * @param font New value of property font.
     *
     */
    @Override
    public void setFont(Font font) {
        this.font = font;
        repaint();
    }
    
    public int getIndexOf(ThumbElement th){
        return th==null ? -1 : elements.indexOf(th);
    }
    
    public Rectangle getRectFor(int p){
        Rectangle r=null;
        if(p>=0 && p<elements.size()){
            r=new Rectangle(boxSize);
            r.x=(p%elementsPerRow)*r.width;
            r.y=(p/elementsPerRow)*r.height;
        }
        return r;
    }
    
    @Override
    public void doLayout(){
        Dimension d=getPreferredSize();
        int w=Math.max(d.width, boxSize.width);
        elementsPerRow=w/boxSize.width;
        /*
         * ELIMINATED DUE TO
         * RECURSIVE CALLS IN 1.4
         * TEST IN 1.3.1
         *
        int nRows=elements.size()/elementsPerRow+1;
        int h=nRows*boxSize.height;
        if(getHeight()!=h)
            setBounds(getX(), getY(), w, h);
         */
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        Rectangle clip=g2.getClipBounds();
        Rectangle r=new Rectangle(boxSize);
        for(int i=0; i<elements.size(); i++){
            r.x=(i%elementsPerRow)*r.width;
            r.y=(i/elementsPerRow)*r.height;
            if(r.intersects(clip)){
                getThumbElement(i).paint(g2, r);
            }
        }
    }
    
    
    public void addActionListener(ActionListener l) {
        listenersList.add(ActionListener.class, l);
    }
    
    public void removeActionListener(ActionListener l) {
        listenersList.remove(ActionListener.class, l);
    }
    
    protected void fireActionPerformed() {
        ActionEvent actionEvent=new ActionEvent(current, ActionEvent.ACTION_PERFORMED, "");
        Object[] listeners = listenersList.getListeners(ActionListener.class);
        for (Object lst : listeners) {
            ((ActionListener)lst).actionPerformed(actionEvent);
        }
    }
    
    public void addListSelectionListener(ListSelectionListener l){
        listenersList.add(ListSelectionListener.class, l);
    }
    
    public void removeListSelectionListener(ListSelectionListener l) {
        listenersList.remove(ListSelectionListener.class, l);
    }
    
    protected void fireSelectionChanged(int index) {
        ListSelectionEvent listEvent=new ListSelectionEvent(current, index, index, false);
        Object[] listeners = listenersList.getListeners(ListSelectionListener.class);
        for (Object lst : listeners) {
            ((ListSelectionListener)lst).valueChanged(listEvent);
        }
    }
    
    @Override
    protected void processMouseEvent(MouseEvent ev){
        if(isEnabled() && ev.getID()==MouseEvent.MOUSE_RELEASED){
            int p=(ev.getX()/boxSize.width)+elementsPerRow*(ev.getY()/boxSize.height);
            if(p<elements.size()){
                if(ev.getClickCount()==1){
                    setCurrent(getThumbElement(p));
                    fireSelectionChanged(p);
                }
                else if(ev.getClickCount()==2/* && current==getThumbElement(p)*/){
                    fireActionPerformed();
                }
            }
        }
    }
    
    @Override
    protected void processKeyEvent(KeyEvent e){
    }
    
    public class ThumbElement {
        
        ImageIcon image;
        String text;
        Object userObject;
        
        /** Creates a new instance of ThumbElement */
        ThumbElement(Object userObject, ImageIcon image, String text) {
            setUserObject(userObject);
            setImage(image);
            setText(text);
        }
        
        /** Getter for property userObject.
         * @return Value of property userObject.
         *
         */
        public java.lang.Object getUserObject() {
            return userObject;
        }
        
        /** Setter for property userObject.
         * @param userObject New value of property userObject.
         *
         */
        public void setUserObject(java.lang.Object userObject) {
            this.userObject = userObject;
        }
        
        /** Getter for property text.
         * @return Value of property text.
         *
         */
        public java.lang.String getText() {
            return text;
        }
        
        /** Setter for property text.
         * @param text New value of property text.
         *
         */
        public void setText(java.lang.String text) {
            this.text = text;
        }
        
        /** Getter for property image.
         * @return Value of property image.
         *
         */
        public ImageIcon getImage() {
            return image;
        }
        
        /** Setter for property image.
         * @param image New value of property image.
         *
         */
        public void setImage(ImageIcon image) {
            this.image = image;
            if(image!=null)
                //image.setImageObserver(ThumbsExplorerPanel.this);
                sizeImage();
        }
        
        protected void sizeImage(){
            if(image!=null){
                int w=image.getIconWidth();
                int h=image.getIconHeight();
                if(w>maxThumbSize.width || h>maxThumbSize.height){
                    double f=Math.min((double)maxThumbSize.width/w, (double)maxThumbSize.height/h);
                    image=new ImageIcon(image.getImage().getScaledInstance((int)(f*w),
                    (int)(f*h),
                    Image.SCALE_SMOOTH));
                }
            }
        }
        
        protected void paint(Graphics2D g2, Rectangle r){
            
            g2.setBackground(LFUtil.getColor("Table.background", Color.white));
            g2.clearRect(r.x, r.y, r.width, r.height);
            
            if(current==this){
                Rectangle r2=new Rectangle(r.x+th_margin/2, r.y+th_margin/2,
                r.width-th_margin, r.height-th_margin);
                g2.setColor(LFUtil.getSysColor("activeCaption", Color.orange));
                g2.fill(r2);
                g2.setColor(LFUtil.getSysColor("activeCaptionBorder", Color.red));
                g2.draw(r2);
            }
            if(image!=null){
                image.paintIcon(ThumbsExplorerPanel.this, g2,
                r.x+((th_width+2*th_margin)-image.getIconWidth())/2,
                r.y+((th_height+2*th_margin)-image.getIconHeight())/2);
            }
            g2.setColor(LFUtil.getColor("Table.gridColor", Color.gray));
            g2.setStroke(BORDER_STROKE);
            g2.drawRect(r.x+th_margin, r.y+th_margin, th_width, th_height);
            if(text!=null && text.length()>0){
                if(current==this)
                    g2.setColor(LFUtil.getSysColor("activeCaptionText", Color.black));
                else
                    g2.setColor(LFUtil.getColor("Table.foreground", Color.black));
                FontRenderContext frc=g2.getFontRenderContext();
                Font f2=LFUtil.getFont("Table.font", font);
                TextLayout layout = new TextLayout(text, f2, frc);
                Rectangle2D txRect=layout.getBounds();
                if(layout.getBounds().getWidth()>th_width){
                    TextHitInfo hitInfo = layout.hitTestChar(th_width-20, 0);
                    int insPoint = hitInfo.getInsertionIndex();
                    if(insPoint>0){
                        layout=new TextLayout(text.substring(0, insPoint)+"...", f2, frc);
                        txRect=layout.getBounds();
                    }
                }
                layout.draw(g2,
                (float)r.x+th_margin+(th_width-(float)txRect.getWidth())/2,
                (float)r.y+2*th_margin+th_height);
            }
        }
    }
}
