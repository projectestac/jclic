/*
 * File    : TextGrid.java
 * Created : 10-sep-2001 13:08
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

import edu.xtec.util.StrUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.ImageObserver;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.Timer;

/**
 * This class is a special {@link edu.xtec.jclic.boxes.ActiveBox} that displays a grid
 * of single characters. It is used in activities like crosswords and scrambled letters.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class TextGrid extends AbstractBox implements Cloneable, Resizable, ActionListener{
    
    int nRows, nCols;
    char [][] chars;
    char [][] answers;
    int [][] attributes;
    double cellWidth;
    double cellHeight;
    Rectangle2D preferredBounds=new Double();
    public char wild=TextGridContent.DEFAULT_WILD;
    String randomChars=TextGridContent.DEFAULT_RANDOM_CHARS;
    boolean cursorEnabled;
    boolean useCursor;
    Point cursor=new Point();
    boolean cursorBlink;
    Timer cursorTimer;
    boolean wildTransparent;
        
    public static final int MIN_CELL_SIZE=12, DEFAULT_CELL_SIZE=20, MIN_INTERNAL_MARGIN=2;
    public static final int NORMAL=0, INVERTED=1, HIDDEN=2, LOCKED=4, MARKED=8, TRANSPARENT=16;
    
    /** Creates new TextGridBox */
    
    public TextGrid(AbstractBox parent, JComponent container, double setX, double setY, 
                    int setNcw, int setNch, double setCellW, double setCellH, 
                    BoxBase boxBase, boolean setBorder){
        super(parent, container, boxBase);
        x=setX;
        y=setY;
        nCols=Math.max(1, setNcw);
        nRows=Math.max(1, setNch);
        cellWidth=Math.max(setCellW, MIN_CELL_SIZE);
        cellHeight=Math.max(setCellH, MIN_CELL_SIZE);
        width=cellWidth*nCols;
        height=cellHeight*nRows;
        chars=new char[nRows][nCols];
        attributes=new int[nRows][nCols];
        preferredBounds.setRect(getBounds());
        setBorder(setBorder);
        cursorTimer=new Timer(500, this);
        cursorTimer.setRepeats(true);
        cursorEnabled=false;
        useCursor=false;
        wildTransparent=false;
        answers=null;
    }
    
    public static TextGrid createEmptyGrid(AbstractBox parent, JComponent container, 
                                           double setX, double setY, TextGridContent tgc, 
                                           boolean wildTransparent){
        TextGrid result=new TextGrid(parent, container, setX, setY, 
                                     tgc.ncw, tgc.nch, tgc.w, tgc.h, tgc.bb, tgc.border);
        result.wild=tgc.wild;
        result.randomChars=tgc.randomChars;
        result.wildTransparent=wildTransparent;
        return result;
    }
    
    public void setChars(String[] text){
        answers=new char[nRows][nCols];
        for(int py=0; py<nRows; py++){
            String line = py<text.length ? text[py] : null;
            for(int px=0; px<nCols; px++){
                chars[py][px]=(line==null || px>=line.length()) ? ' ' : line.charAt(px);
                answers[py][px]=chars[py][px];
            }
        }
        repaint();
    }
    
    public void randomize(){
        for(int py=0; py<nRows; py++)
            for(int px=0; px<nCols; px++)
                if(chars[py][px]==wild)
                    chars[py][px]=randomChars.charAt((int)(Math.random()*randomChars.length()));
        repaint();
    }
    
    public void setCellAttributes(boolean lockWild, boolean clearChars){
        int atr=LOCKED;
        if(wildTransparent)
            atr|=TRANSPARENT;
        else
            atr|=INVERTED|HIDDEN;
        for(int py=0; py<nRows; py++)
            for(int px=0; px<nCols; px++)
                if(lockWild && chars[py][px]==wild)
                    attributes[py][px]=atr;
                else{
                    attributes[py][px]=NORMAL;
                    if(clearChars)
                        chars[py][px]=' ';
                }
        repaint();
    }
    
    public void setCellLocked(int px, int py, boolean locked){
        if(px>=0 && px<nCols && py>=0 && py<nRows){
            attributes[py][px] = locked 
            ? LOCKED | (wildTransparent ? TRANSPARENT : INVERTED|HIDDEN) 
            : NORMAL;
        }
    }
    
    public Point getItemFor(int rx, int ry){
        if(!isValidCell(rx, ry))
            return null;
        Point point=new Point();
        boolean inBlack=false, startCount=false;
        for(int px=0; px<rx; px++){
            if((attributes[ry][px]&LOCKED)!=0){
                if(!inBlack){
                    if(startCount) 
                        point.x++;
                    inBlack=true;
                }
            } else{
                startCount=true;
                inBlack=false;
            }
        }
        inBlack=false; 
        startCount=false;
        for(int py=0; py<ry; py++){
            if((attributes[py][rx]&LOCKED)!=0){
                if(!inBlack){
                    if(startCount) 
                        point.y++;
                    inBlack=true;
                }
            } else{
                startCount=true;
                inBlack=false;
            }
        }
        return point;
    }
    
    public void setCursorEnabled(boolean status){
        cursorEnabled=status;
        if(status==true)
            startCursorBlink();
        else
            stopCursorBlink();
    }
    
    public void startCursorBlink(){
        if(useCursor && cursorEnabled && cursorTimer!=null && !cursorTimer.isRunning()){
            blink(1);
            cursorTimer.start();
        }
    }
    
    public void stopCursorBlink(){
        if(cursorTimer!=null && cursorTimer.isRunning()){
            cursorTimer.stop();
            blink(-1);
        }
    }
    
    public void moveCursor(int dx, int dy, boolean skipLocked){
        if(useCursor){
            Point point=findNextCellWithAttr(cursor.x, cursor.y, 
                                             skipLocked ? LOCKED : NORMAL, 
                                             dx, dy, false);
            if(!cursor.equals(point))
                setCursorAt(point.x, point.y, skipLocked);
        }
    }
    
    public Point findFreeCell(Point from, int dx, int dy){
        Point result=null;
        if(from!=null && (dx!=0 || dy!=0)){
            Point scan=new Point(from);
            while(result==null){
                scan.x+=dx;
                scan.y+=dy;
                if(scan.x<0 || scan.x>=nCols || scan.y<0 || scan.y>=nRows)
                    break;
                if(!getCellAttribute(scan.x, scan.y, LOCKED))
                    result=scan;
            }
        }
        return result;
    }
    
    public boolean isIntoBlacks(Point pt, boolean checkHorizontal){
        boolean result;
        if(checkHorizontal){
            result=(pt.x<=0 || getCellAttribute(pt.x-1, pt.y, LOCKED))
            && (pt.x>=nCols-1 || getCellAttribute(pt.x+1, pt.y, LOCKED));
        }
        else{
            result=(pt.y<=0 || getCellAttribute(pt.x, pt.y-1, LOCKED))
            && (pt.y>=nRows-1 || getCellAttribute(pt.x, pt.y+1, LOCKED));            
        }
        return result;
    }
    
    public boolean isIntoWhites(Point pt, boolean checkHorizontal){
        boolean result;
        if(checkHorizontal){
            result=(pt.x>0 && !getCellAttribute(pt.x-1, pt.y, LOCKED))
            && (pt.x<nCols-1 && !getCellAttribute(pt.x+1, pt.y, LOCKED));
        }
        else{
            result=(pt.y>0 && !getCellAttribute(pt.x, pt.y-1, LOCKED))
            && (pt.y<nRows-1 && !getCellAttribute(pt.x, pt.y+1, LOCKED));            
        }
        return result;
    }
    
    public Point findNextCellWithAttr(int startX, int startY, int attr, 
                                      int dx, int dy, boolean attrState){
        Point point=new Point(startX+dx, startY+dy);
        while(true){
            if(point.x<0){
                point.x=nCols-1; 
                if(point.y>0)
                    point.y--; 
                else 
                    point.y=nRows-1;
            }
            else if(point.x>=nCols){
                point.x=0; 
                if(point.y<nRows-1)
                    point.y++; 
                else 
                    point.y=0;
            }
            if(point.y<0){
                point.y=nRows-1; 
                if(point.x>0)
                    point.x--; 
                else 
                    point.x=nCols-1;
            }
            else if(point.y>=nRows){
                point.y=0; 
                if(point.x<nCols-1)
                    point.x++; 
                else 
                    point.x=0;
            }
            if((point.x==startX && point.y==startY) || 
                getCellAttribute(point.x, point.y, attr)==attrState)
                break;
            point.x+=dx;
            point.y+=dy;
        }
        return point;
    }
    
    public void setCursorAt(int px, int py, boolean skipLocked){
        stopCursorBlink();
        if(isValidCell(px, py)){
            cursor.x=px; cursor.y=py;
            useCursor=true;
            if(skipLocked && getCellAttribute(px, py, LOCKED)){
                moveCursor(1, 0, skipLocked);
            }
            else{
                if(cursorEnabled)
                    startCursorBlink();
            }
        }
    }
    
    public void setUseCursor(boolean value){
        useCursor=value;
    }
    
    public Point getCursor(){
        return cursor;
    }
    
    public int countCharsLike(char ch){
        int result=0;
        for(int py=0; py<nRows; py++)
            for(int px=0; px<nCols; px++)
                if(chars[py][px]==ch)
                    result++;
        return result;
    }
    
    public int getNumCells(){
        return nRows*nCols;
    }
    
    public int countCoincidences(boolean checkCase){
        int result=0;
        if(answers!=null)
            for(int py=0; py<nRows; py++)
                for(int px=0; px<nCols; px++)
                    if(isCellOk(px, py, checkCase))
                        result++;
        return result;
    }
    
    public boolean isCellOk(int px, int py, boolean checkCase){
        boolean result=false;
        if(isValidCell(px, py)){
            char ch=chars[py][px];
            if(ch!=wild){
                char ch2=answers[py][px];
                if(ch==ch2 ||
                   (!checkCase && Character.toUpperCase(ch)==Character.toUpperCase(ch2)))
                    result=true;
            }
        }
        return result;
    }
    
    public Point getLogicalCoords(Point2D devicePoint){
        if(!contains(devicePoint)) 
            return null;
        int px=(int)((devicePoint.getX()-getX())/cellWidth);
        int py=(int)((devicePoint.getY()-getY())/cellHeight);
        if(isValidCell(px, py)){
            return new Point(px, py);
        }
        else 
            return null;
    }
    
    public boolean isValidCell(int px, int py){
        return px<nCols && py<nRows && px>=0 && py>=0;
    }
    
    public void setCharAt(int px, int py, char ch){
        if(isValidCell(px, py)){
            chars[py][px]=ch;
            repaintCell(px, py);
        }
    }
    
    public char getCharAt(int px, int py){
        if(isValidCell(px, py))
            return chars[py][px];
        else 
            return ' ';
    }
    
    public String getStringBetween(int x0, int y0, int x1, int y1){
        StringBuilder sb=new StringBuilder();
        if(isValidCell(x0, y0) && isValidCell(x1, y1)){
            int dx=x1-x0;
            int dy=y1-y0;
            if(dx==0 || dy==0 || Math.abs(dx)==Math.abs(dy)){
                int steps=Math.max(Math.abs(dx), Math.abs(dy));
                if(steps>0){
                    dx/=steps;
                    dy/=steps;
                }
                for(int i=0; i<=steps; i++)
                    sb.append(getCharAt(x0+dx*i, y0+dy*i));
            }
        }
        return sb.substring(0);
    }
    
    public void setAttributeBetween(int x0, int y0, int x1, int y1, int attribute, boolean value){
        if(isValidCell(x0, y0) && isValidCell(x1, y1)){
            int dx=x1-x0;
            int dy=y1-y0;
            if(dx==0 || dy==0 || Math.abs(dx)==Math.abs(dy)){
                int steps=Math.max(Math.abs(dx), Math.abs(dy));
                if(steps>0){
                    dx/=steps;
                    dy/=steps;
                }
                for(int i=0; i<=steps; i++)
                    setAttribute(x0+dx*i, y0+dy*i, attribute, value);
            }
        }
    }
    
    public void setAttribute(int px, int py, int attribute, boolean state){
        if(isValidCell(px, py)){
            if(attribute==MARKED && !state) 
                repaintCell(px, py);
            attributes[py][px]&=~attribute;
            attributes[py][px]|=(state ? attribute : 0);
            if(attribute!=MARKED || state) 
                repaintCell(px, py);
        }
    }
    
    public void setAllCellsAttribute(int attribute, boolean state){
        for(int py=0; py<nRows; py++)
            for(int px=0; px<nCols; px++)
                setAttribute(px, py, attribute, state);
    }
    
    public boolean getCellAttribute(int px, int py, int attribute){
        if(isValidCell(px, py))
            return (attributes[py][px]&attribute)!=0;
        else 
            return false;
    }
    
    public Rectangle2D getCellRect(int px, int py){
        return new Double(getX()+px*cellWidth, getY()+py*cellHeight, cellWidth, cellHeight);
    }
    
    public Rectangle getCellBorderBounds(int px, int py){
        boolean isMarked=getCellAttribute(px, py, MARKED);
        if(!border && !isMarked)
            return getCellRect(px, py).getBounds();
        BoxBase bb=getBoxBaseResolve();
        Stroke strk=isMarked ? bb.getMarker() : bb.getBorder();
        return  strk.createStrokedShape(getCellRect(px, py)).getBounds();
    }
    
    public void repaintCell(int px, int py){
        JComponent jc=getContainerResolve();
        if(jc!=null)
            jc.repaint(getCellBorderBounds(px, py));
    }
    
    @Override
    public Object clone(){
        TextGrid tgb=(TextGrid)super.clone();
        tgb.nRows=nRows;
        tgb.nCols=nCols;
        tgb.chars=new char[nRows][nCols];
        tgb.attributes=new int[nRows][nCols];
        for(int i=0; i<nRows; i++){
            System.arraycopy(chars[i], 0, tgb.chars[i], 0, nCols);
            System.arraycopy(attributes[i], 0, tgb.attributes[i], 0, nCols);
        }
        tgb.cellWidth=cellWidth;
        tgb.cellHeight=cellHeight;
        tgb.preferredBounds=(Rectangle2D)preferredBounds.clone();
        return tgb;
    }
    
    public Dimension getPreferredSize(){
        return preferredBounds.getBounds().getSize();
    }
    
    public Dimension getMinimumSize(){
        return new Dimension(MIN_CELL_SIZE * nCols, MIN_CELL_SIZE * nRows);
    }
    
    public Dimension getScaledSize(double scale){
        return new Dimension(StrUtils.roundTo(scale*preferredBounds.getWidth(), nCols), 
                             StrUtils.roundTo(scale*preferredBounds.getHeight(), nRows));
    }
    
    @Override
    public void setBounds(Rectangle2D r){
        super.setBounds(r);
        cellWidth=width/nCols;
        cellHeight=height/nRows;
    }
    
    @Override
    public boolean update(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io){
        if(isEmpty() || !isVisible() || isTemporaryHidden()) 
            return false;
        if(dirtyRegion!=null && !shape.intersects(dirtyRegion)) 
            return false;
        
        updateContent(g2, dirtyRegion, io);
        
        return true;
    }
    
    public boolean updateContent(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io){
        
        FontRenderContext frc=g2.getFontRenderContext();
        BoxBase bb=getBoxBaseResolve();
        
        // test font size
        FontMetrics fm=g2.getFontMetrics(bb.getFont());
        boolean resize=false;
        while(true){
            if(fm.charWidth('W')<=cellWidth-2*MIN_INTERNAL_MARGIN && 
               fm.getAscent()+fm.getDescent()<=cellHeight-2*MIN_INTERNAL_MARGIN)
                break;
            if(bb.reduceFont()==false)
                break;
            resize=true;
            fm=g2.getFontMetrics(bb.getFont());
        }
        if(resize){
            JComponent jc=getContainerResolve();
            if(jc!=null)
                RepaintManager.currentManager(jc).markCompletelyDirty(jc);
            return true;
        }
        
        char[] ch=new char[1];
        int attr;
        boolean isMarked, isInverted, isCursor;
        Rectangle2D boxBounds;
        double dx, dy;
        double ry=(cellHeight-fm.getDescent()+fm.getAscent())/2;
        
        for(int py=0; py<nRows; py++){
            for(int px=0; px<nCols; px++){
                Rectangle bxr=getCellBorderBounds(px, py);
                if(bxr.intersects(dirtyRegion)){
                    attr=attributes[py][px];
                    if((attr&TRANSPARENT)==0){
                        isInverted=(attr&INVERTED)!=0;
                        isMarked=(attr&MARKED)!=0;
                        isCursor=(useCursor && cursor.x==px && cursor.y==py);
                        boxBounds=getCellRect(px, py);
                        g2.setColor((isCursor && cursorBlink) ? 
                                    bb.inactiveColor : 
                                    isInverted ? bb.textColor : bb.backColor);
                        g2.fill(boxBounds);
                        g2.setColor(Color.black);
                        if((attr&HIDDEN)==0){
                            ch[0]=chars[py][px];
                            if(ch[0]!=0){
                                dx=boxBounds.getX()+(cellWidth-fm.charWidth(ch[0]))/2;
                                dy=boxBounds.getY()+ry;
                                GlyphVector gv=bb.getFont().createGlyphVector(frc, ch);
                                if(bb.shadow){
                                    g2.setColor(bb.shadowColor);
                                    g2.drawGlyphVector(gv, (float)(dx+bb.getDynFontSize()/10), 
                                                           (float)(dy+bb.getDynFontSize()/10));
                                }
                                g2.setColor(isInverted ? 
                                            bb.backColor : 
                                            isAlternative() ? bb.alternativeColor : bb.textColor);
                                g2.drawGlyphVector(gv, (float)dx, (float)dy);
                            }
                        }
                        if(border || isMarked){
                            g2.setColor(bb.borderColor);
                            g2.setStroke(isMarked ? bb.getMarker() : bb.getBorder());
                            if(isMarked) 
                                g2.setXORMode(Color.white);
                            g2.draw(boxBounds);
                            if(isMarked) 
                                g2.setPaintMode();
                            g2.setStroke(BoxBase.DEFAULT_STROKE);
                        }
                        g2.setColor(Color.black);
                    }
                }
            }
        }
        return true;
    }
    
    public void actionPerformed(ActionEvent ev){
        blink(0);
    }
    
    protected synchronized void blink(int status){
        if(useCursor){
            cursorBlink = status==1 ? true : status==-1 ? false : !cursorBlink;
            repaintCell(cursor.x, cursor.y);
        }
    }
    
    @Override
    public void end(){
        if(cursorTimer!=null){
            cursorTimer.stop();
            cursorTimer=null;
        }
    }
    
    public void finalize() throws Throwable{
        try {
            end();
        } finally {
            super.finalize();
        }
    }
}
