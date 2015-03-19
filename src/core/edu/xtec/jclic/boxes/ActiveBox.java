/*
 * File    : ActiveBox.java
 * Created : 12-dec-2000 17:05
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

import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.media.ActiveMediaPlayer;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceBridge;
import edu.xtec.util.StrUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.text.AttributedString;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;


/**
 * Objects of this class are widely used in JClic activities: cells in puzzles and
 * associations, messages and other objects are active boxes. The specific
 * content, size and location of <CODE>ActiveBox</CODE> is determined by its
 * {@link edu.xtec.jclic.boxes.ActiveBoxContent} members. Most ActiveBoxes have only
 * one content, but some of them can have a secondary or "alternative" content, indicated
 * by the <CODE>altContent</CODE> member. This content is used only when the <CODE>alternative</CODE>
 * flag of the <CODE>ActiveBox</CODE> is on.
 * Active boxes can host video and interactive media content (specified in the mediaContent member of
 * the {@link edu.xtec.jclic.boxes.ActiveBoxContent}) through the <CODE>hostedMediaPlayer</CODE> member.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class ActiveBox extends AbstractBox implements Cloneable {
    
    public static boolean compressImages=true;
    public static boolean USE_TRANSFORM=false;
    
    public int idOrder, idLoc, idAss;
    protected ActiveBoxContent content;
    protected ActiveBoxContent altContent;
    public boolean hasHostedComponent;
    protected ActiveMediaPlayer hostedMediaPlayer;
    
    
    /** Creates new ActiveBox */
    public ActiveBox(AbstractBox parent, JComponent container, BoxBase boxBase) {
        super(parent, container, boxBase);
        clear();
        idLoc=-1;
        idOrder=-1;
        idAss=-1;
        hasHostedComponent=false;
    }
    
    public ActiveBox(AbstractBox parent, JComponent container, int setIdLoc, 
                     Rectangle2D r, BoxBase boxBase){
        super(parent, container, boxBase);
        clear();
        idLoc=setIdLoc;
        //idAss=-1;
        setBounds(r);
    }
    
    public void setHostedMediaPlayer(ActiveMediaPlayer amp){
        ActiveMediaPlayer old=hostedMediaPlayer;
        hostedMediaPlayer=amp;
        if(old!=null && old!=amp)
            old.linkTo(null);
    }
    
    public ActiveBoxContent getCurrentContent(){
        return isAlternative() ? altContent : content;
    }
    
    public ActiveBoxContent getContent(){
        if(content==null)
            setContent(new ActiveBoxContent());
        return content;
    }
    
    public void clear(){
        content=null;
        altContent=null;
        idOrder=-1;
        setInactive(true);
        if(!hasHostedComponent)
            setHostedComponent(null);
        setHostedMediaPlayer(null);
    }
    
    public boolean isEquivalent(ActiveBox bx, boolean checkCase){
        return bx!=null &&
        content!=null &&
        content.isEquivalent(bx.content, checkCase);
    }
    
    public boolean isCurrentContentEquivalent(ActiveBox bx, boolean checkCase){
        return bx!=null &&
        getCurrentContent()!=null &&
        getCurrentContent().isEquivalent(bx.getCurrentContent(), checkCase);
    }
    
    public void exchangeLocation(ActiveBox bx){
        Point2D.Double pt=new Point2D.Double(x, y);
        int idLoc0=idLoc;
        setLocation(bx.getLocation());
        bx.setLocation(pt);
        idLoc=bx.idLoc;
        bx.idLoc=idLoc0;
    }
    
    public void copyContent(ActiveBox bx){
        idOrder=bx.idOrder;
        idAss=bx.idAss;
        content=bx.content;
        altContent=bx.altContent;
        if(content!=null){
            if(content.bb!=null)
                setBoxBase(content.bb);
            if(content.border!=null && bx.hasBorder()!=content.border.booleanValue())
                setBorder(content.border.booleanValue());
            if(content.img!=null && content.animated)
                Utils.refreshAnimatedImage(content.img);
        }
        setInactive(bx.isInactive());
        setInverted(bx.isInverted());
        setAlternative(bx.isAlternative());
        setHostedComponent(bx.getHostedComponent());
        hasHostedComponent=bx.hasHostedComponent;
        setHostedMediaPlayer(bx.hostedMediaPlayer);
        if(hostedMediaPlayer!=null)
            hostedMediaPlayer.setVisualComponentVisible(!isInactive() && isVisible());
    }
    
    public void exchangeContent(ActiveBox bx){
        ActiveBox bx0=new ActiveBox(getParent(), getContainerX(), getBoxBaseX());
        bx0.copyContent(this);
        copyContent(bx);
        bx.copyContent(bx0);
    }
    
    public void setTextContent(String tx){
        // only plain text!
        if(tx==null) tx="";
        if(content==null) content=new ActiveBoxContent();
        content.rawText=tx;
        content.text=tx;
        content.mediaContent=null;
        content.img=null;
        
        setHostedComponent(null);
        setInactive(false);
        checkHostedComponent();
        setHostedMediaPlayer(null);
    }
    
    public void setIdOrder(int newIdOrder){
        idOrder=newIdOrder;
    }
    
    public void setIdAss(int newIdAss){
        idAss=newIdAss;
    }
    
    public void setDefaultIdAss(){
        idAss=(content==null ? -1 : content.id);
    }
    
    public boolean isAtPlace(){
        return idOrder==idLoc;
    }
    
    public void setContent(ActiveBoxContent abc){
        setHostedComponent(null);
        setHostedMediaPlayer(null);
        content=abc;
        if(content!=null) {
            if(content.bb!=getBoxBaseX())
                setBoxBase(content.bb);
            if(content.border!=null && hasBorder()!=content.border.booleanValue())
                setBorder(content.border.booleanValue());
            setInactive(false);
            if(abc.img!=null && abc.animated)
                Utils.refreshAnimatedImage(abc.img);
            checkHostedComponent();
            checkAutoStartMedia();
        }
        else 
            clear();
    }
    
    public void checkAutoStartMedia(){
        ActiveBoxContent cnt=getContent();
        if(cnt!=null && cnt.mediaContent!=null 
           && cnt.mediaContent.autoStart==true && cnt.amp!=null){
            final ActiveMediaPlayer amp=cnt.amp;
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    amp.play(ActiveBox.this);
                }
            });
        }
    }
    
    public void setAltContent(ActiveBoxContent abc){
        altContent=abc;
        checkHostedComponent();
        if(isAlternative() && hostedMediaPlayer!=null)
            setHostedMediaPlayer(null);
    }
    
    public void setCurrentContent(ActiveBoxContent abc){
        if(isAlternative())
            setAltContent(abc);
        else
            setContent(abc);
        repaint();
    }
    
    public void setContent(ActiveBagContent abc, int i){
        if(i<0)
            i=idOrder;
        if(abc==null || i>=abc.getNumCells())
            return;
        if(abc.bb!=getBoxBaseX())
            setBoxBase(abc.bb);
        setContent(abc.getActiveBoxContent(i));
    }
    
    public void setAltContent(ActiveBagContent abc, int i){
        if(i<0) i=idOrder;
        if(abc==null || abc.isEmpty() || i>abc.getNumCells()) 
            return;
        setAltContent(abc.getActiveBoxContent(i));
    }
    
    public boolean switchToAlt(ResourceBridge rb){
        if(isAlternative() || altContent==null || altContent.isEmpty()) 
            return false;
        setHostedComponent(null);
        setHostedMediaPlayer(null);
        setAlternative(true);
        checkHostedComponent();
        checkAutoStartMedia();
        return true;
    }
    
    @Override
    protected void checkHostedComponent(){
        if(hasHostedComponent) 
            return;
        ActiveBoxContent abc= getCurrentContent();
        BoxBase bb=getBoxBaseResolve();
        Component jc=null;
        if(!isInactive() && abc!=null && abc.htmlText!=null){
            String s=abc.htmlText;
            if(abc.innerHtmlText!=null){
                Color backColor=isInactive() ? bb.inactiveColor : isInverted() ? bb.textColor : bb.backColor;
                Color foreColor=isInverted() ? bb.backColor : isAlternative() ? bb.alternativeColor : bb.textColor;
                Font f=bb.getOriginalFont();
                s="<html>"+
                "<body bgcolor=\"#"+ Integer.toHexString(backColor.getRGB()&0xFFFFFF) + "\">"+
                "<div style=\""+
                "background: #"+Integer.toHexString(backColor.getRGB()&0xFFFFFF)+"; "+
                "color: #"+Integer.toHexString(foreColor.getRGB()&0xFFFFFF)+"; "+
                "font-family: "+f.getFontName()+"; "+
                "font-size: "+f.getSize()+"pt; "+
                (f.isBold() ? "font-weight: bold;" : "") +
                "text-align: "+ (abc.txtAlign[0]==JDomUtility.ALIGN_LEFT ? "left" : abc.txtAlign[0]==JDomUtility.ALIGN_RIGHT ? "right" : "center") + "; " +
                "width: 100%; "+
                "\">"+
                abc.innerHtmlText +
                "</div>"+
                "</body></html>";
            }
            jc=getHostedComponent();
            if(jc!=null && jc instanceof JLabel){
                ((JLabel)jc).setText(s);
                return;
            }
            else{
                jc=new JLabel(s);
            }
        }
        setHostedComponent(jc);
    }
    
    public boolean updateContent(Graphics2D g2, Rectangle dirtyRegion, ImageObserver io){
        
        ActiveBoxContent abc=getCurrentContent();
        BoxBase bb=getBoxBaseResolve();
        
        if(isInactive() || abc==null || width<2 || height<2) 
            return true;
        
        Rectangle2D.Double imgRect=null;
        
        if(abc.img!=null){
            if(abc.imgClip!=null){
                Rectangle r=abc.imgClip.getBounds();
                
                /*
                 * We have two methods to draw:
                 *  # Using AffineTransform
                 *      * Compatible with Mac OS-X 10.1, JRE 1.3.1 rev 1.
                 *      * Possibly more slow ?
                 *
                 *  # Using Graphics.drawImage
                 *     * Doesn't work with Mac OSX 10.1 Java 1.3.1 rev. 1
                 *
                 *  Comment one of the two, or apply method 1 only to objects of class:
                 *  com.apple.mrj.internal.awt.graphics.VImage
                 */
                
                // Method 1
                if(USE_TRANSFORM){
                    if(r.width>0 && r.height>0){
                        AffineTransform at;
                        if(r.width!=width || r.height!=height){
                            double sx=width/r.width;
                            double sy=height/r.height;
                            at=AffineTransform.getScaleInstance(sx, sy);
                            at.translate(x/sx-r.x, y/sy-r.y);
                        }
                        else
                            at=AffineTransform.getTranslateInstance(x-r.x, y-r.y);
                        g2.drawImage(abc.img, at, io);
                    }
                }
                else{
                    // Method 2
                    g2.drawImage(abc.img, (int)x, (int)y,
                    (int)(x+width), (int)(y+height),
                    (int)r.x, (int)r.y,
                    (int)(r.x+r.width), (int)(r.y+r.height),
                    io);
                }
            }
            else {
                double imgw, imgh;
                boolean compress=false;
                if((imgw=abc.img.getWidth(io))==0) imgw=width;
                if((imgh=abc.img.getHeight(io))==0) imgh=height;
                double scale=1.0;
                if(compressImages==true && (width>0 && height>0) && (imgw>width || imgh>height)){
                    scale=Math.min(width/imgw, height/imgh);
                    imgw*=scale;
                    imgh*=scale;
                    compress=true;
                }
                double xs = (abc.imgAlign[0]==JDomUtility.ALIGN_LEFT 
                             ? 0 
                             : abc.imgAlign[0]==JDomUtility.ALIGN_RIGHT ? width-imgw : (width-imgw)/2);
                double ys = (abc.imgAlign[1]==JDomUtility.ALIGN_TOP 
                             ? 0 
                             : abc.imgAlign[1]==JDomUtility.ALIGN_BOTTOM ? height-imgh : (height-imgh)/2);
                if(compress){
                    if(USE_TRANSFORM){
                        // Method 1:
                        AffineTransform at=AffineTransform.getScaleInstance(scale, scale);
                        at.translate((x+xs)/scale, (y+ys)/scale);
                        g2.drawImage(abc.img, at, io);
                    }
                    else{
                        // Method 2:
                        g2.drawImage(abc.img, (int)(x+xs), (int)(y+ys), (int)imgw, (int)imgh, io);
                    }
                }
                else
                    g2.drawImage(abc.img, (int)(x+xs), (int)(y+ys), io);
                
                if(abc.avoidOverlapping && abc.text!=null /*&& !JDomUtility.isDefaultAlign(abc.imgAlign)*/)
                    imgRect=new Rectangle2D.Double(Math.max(0.0,xs), Math.max(0.0,ys), 
                                                   Math.min(width, imgw), Math.min(height, imgh));
            }
        }
        if(abc.text!=null && abc.text.length()>0){
            double px=this.x;
            double py=this.y;
            double pWidth=this.width;
            double pHeight=this.height;
            if(imgRect!=null){
                double[] prx=new double[]{0, imgRect.x, imgRect.x+imgRect.width, pWidth};
                double[] pry=new double[]{0, imgRect.y, imgRect.y+imgRect.height, pHeight};
                Rectangle2D.Double[] rr=new Rectangle2D.Double[]{
                    new Rectangle2D.Double(prx[0], pry[0], prx[3], pry[1]),
                    new Rectangle2D.Double(prx[0], pry[2], prx[3], pry[3]-pry[2]),
                    new Rectangle2D.Double(prx[0], pry[0], prx[1], pry[3]),
                    new Rectangle2D.Double(prx[2], pry[0], prx[3]-prx[2], pry[3])};
                    Rectangle2D.Double rmax=rr[0];
                    double maxSurface=rmax.width*rmax.height;
                    for(int i=1; i<rr.length; i++){
                        double s=rr[i].width*rr[i].height;
                        if(s>maxSurface-1){
                            if(Math.abs(s-maxSurface)<=1){
                                boolean b=false;
                                switch(i){
                                    case 1:
                                        b=(abc.txtAlign[1]==JDomUtility.ALIGN_BOTTOM);
                                        break;
                                    case 2:
                                        b=(abc.txtAlign[0]==JDomUtility.ALIGN_LEFT);
                                        break;
                                    case 3:
                                        b=(abc.txtAlign[0]==JDomUtility.ALIGN_RIGHT);
                                        break;
                                }
                                if(!b)
                                    continue;
                            }
                            maxSurface=s;
                            rmax=rr[i];
                        }
                    }
                    px+=rmax.x;
                    py+=rmax.y;
                    pWidth=rmax.width;
                    pHeight=rmax.height;                    
            }
            double w, wtl, h, hl, dx, dy;
            double ww=Math.max(5.0, pWidth-2*bb.textMargin);
            AttributedString atext;
            TextLayout layout;
            int rt;
            FontRenderContext frc=g2.getFontRenderContext();
            LineBreakMeasurer lbm;
            int maxLines=StrUtils.countSpaces(abc.text)+1;
            
            for(;;){
                int i;
                atext=new AttributedString(abc.text);
                atext.addAttribute(TextAttribute.FONT, bb.getFont());
                lbm=new LineBreakMeasurer(atext.getIterator(), frc);
                w=0; h=0; hl=0;
                lbm.setPosition(0);
                rt=abc.text.indexOf('\n');
                for(i=0; lbm.getPosition()<abc.text.length() && h<pHeight ;i++){                    
                    if(rt>lbm.getPosition()){
                        layout=lbm.nextLayout((float)ww, rt, false);
                        rt=abc.text.indexOf('\n', lbm.getPosition());
                    }
                    else
                        layout=lbm.nextLayout((float)ww);
                    
                    if(layout==null) break;
                    wtl=layout.getVisibleAdvance();
                    if(wtl>w) w=wtl;
                    hl=layout.getLeading();
                    h+= layout.getAscent()+layout.getDescent()+hl;
                }
                h-=hl;
                if((h<=pHeight && i<=maxLines) || bb.reduceFont()==false)
                    break;
                JComponent jc=getContainerResolve();
                if(jc!=null)
                    RepaintManager.currentManager(jc).markCompletelyDirty(jc);
            }
            dy= py + (abc.txtAlign[1]==JDomUtility.ALIGN_TOP 
                     ? 0 
                     : abc.txtAlign[1]==JDomUtility.ALIGN_BOTTOM 
                       ? pHeight-h : (pHeight-h)/2);
            g2.setColor(isInverted() 
                        ? bb.backColor 
                        : isAlternative() 
                          ? bb.alternativeColor 
                          : bb.textColor);
            lbm.setPosition(0);
            rt=abc.text.indexOf('\n');
            h=0;
            while(lbm.getPosition()<abc.text.length() && h<pHeight){
                
                if(rt>lbm.getPosition()){
                    layout=lbm.nextLayout((float)ww, rt, false);
                    rt=abc.text.indexOf('\n', lbm.getPosition());
                }
                else
                    layout=lbm.nextLayout((float)ww);
                
                if(layout==null) break;
                wtl=layout.getVisibleAdvance();
                dx= px + bb.textMargin + 
                    (abc.txtAlign[0]==JDomUtility.ALIGN_LEFT 
                     ? 0 
                     : abc.txtAlign[0]==JDomUtility.ALIGN_RIGHT 
                       ? ww-wtl 
                       : (ww-wtl)/2);

                // 21-dec-1009
                // Removed: right-to-left writting is automatically computed
                // by TextLayout!
                //
                //if(!layout.isLeftToRight()){
                //    dx-=wtl;
                //}
                h+=layout.getAscent();
                if(bb.shadow){
                    g2.setColor(bb.shadowColor);
                    layout.draw(g2, (float)(dx+bb.getDynFontSize()/10), (float)(dy+h+bb.getDynFontSize()/10));
                    g2.setColor(isInverted() 
                                ? bb.backColor 
                                : isAlternative() 
                                  ? bb.alternativeColor 
                                  : bb.textColor);
                }
                layout.draw(g2, (float)dx, (float)(dy+h));
                h+=layout.getDescent()+layout.getLeading();
            }
        }
        return true;
    }
    
    public boolean playMedia(PlayStation ps){
        ActiveBoxContent abc=getCurrentContent();
        // ORIGINAL
        /*
        if(abc!=null && abc.mediaContent!=null){
            ac.playMedia(abc.mediaContent, this);
            return true;
        }
         */
        // MODIFIED TO REPAINT ANIMATED GIFS
        if(abc!=null){
            if(abc.animated){
                Utils.refreshAnimatedImage(abc.img);
                repaint();
            }
            if(abc.mediaContent!=null){
                ps.playMedia(abc.mediaContent, this);
                return true;
            }
        }
        return false;
    }
    
    public String getDescription(){
        return content==null ? "" : content.getDescription();
    }
    
    @Override
    public void setBounds(Rectangle2D r){
        super.setBounds(r);
        if(hostedMediaPlayer!=null)
            hostedMediaPlayer.checkVisualComponentBounds(this);
    }
    
    @Override
    public void end(){
        clear();
        super.end();
    }
    
    public static void checkOptions(Options options){
        if(!options.getBoolean(Options.JAVA14) && options.getBoolean(Options.MAC)){
            USE_TRANSFORM=true;
        }
    }
}
