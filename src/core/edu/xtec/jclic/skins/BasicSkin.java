/*
 * File    : BasicSkin.java
 * Created : 10-oct-2001 18:19
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

package edu.xtec.jclic.skins;

import edu.xtec.jclic.*;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.misc.*;
import edu.xtec.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class BasicSkin extends Skin {
    
    BufferedImage img;
    Color fillColor;
    int initiated;
    Rectangle frame;
    int leftSlicer, rightSlicer, topSlicer, bottomSlicer;
    Rectangle playerRect;
    Rectangle msgRect;
    Rectangle [] msgAreaRect = new Rectangle[NUM_MSG_AREAS];
    Color textColor;
    Rectangle [] buttonsRect = new Rectangle[Constants.NUM_ACTIONS];
    Rectangle [][] countersRect = new Rectangle[Constants.NUM_COUNTERS][];
    ActiveBox[] countersLabel = new ActiveBox[Constants.NUM_COUNTERS];
    AbstractBox[] boxes = new AbstractBox[NUM_BOXES];
    JProgressBar progressBar;
    Rectangle progressBarRect;
    Rectangle progressAnimationRect;
    AnimatedActiveBox progressAnimation;
    boolean hideProgressBar, hideProgressAnim;
    
    static final int NUM_BOXES=2*Constants.NUM_COUNTERS+5;
    
    /** Creates new BasicSkin */
    protected BasicSkin() {
        super();
        for(int i=0; i<Constants.NUM_COUNTERS; i++)
            countersRect[i]=new Rectangle[2];
        progressBar=null;
        progressAnimation=null;
        hideProgressBar=true; hideProgressAnim=true;
        initiated=0;
    }
    
    public static final String IMAGE="image", PREFERRED_SIZE="preferredSize",
    FRAME="frame", PLAYER="player", FILL="fill",
    SLICER="slicer", MESSAGES="messages", STATUS_BAR="statusBar",
    SETTINGS="settings", STYLE="style", FOREGROUND="foreground", BACKGROUND="background",
    MSG_AREA="msgArea", BORDER="border",
    BUTTONS="buttons", BUTTON="button", ACTIVE="active", OVER="over", DISABLED="disabled",
    POS="pos", SOURCE="source", COUNTERS="counters", COUNTER="counter", LABEL="label",
    DIGITS="digits", TOGGLE="toggle",
    PROGRESS_BAR="progressBar", AUTO_HIDE="autoHide", SHOW_PERCENT="showPercent",
    PROGRESS_ANIMATION="progressAnimation", FRAMES="frames", DELAY="delay", STEP="step";
    
    protected void setProperties(org.jdom.Element e, FileSystem fs) throws Exception{
        org.jdom.Element child, child2, child3, child4;
        BoxBase bb;
        
        setPreferredSize(JDomUtility.getDimension(e, PREFERRED_SIZE, new Dimension(640, 480)));
        
        fillColor=JDomUtility.getColorByPoint(e, FILL, img, Color.white);
        String imageFile=JDomUtility.getStringAttr(e, IMAGE, null, true);
        if(imageFile==null || imageFile.length()==0)
            throw new Exception("BasicSkin properties without image name!");
        if(fs==null){
            //ImageIcon imgIcon=new ImageIcon(getClass().getResource(imageFile));
            ImageIcon imgIcon=ResourceManager.getImageIcon(RESOURCE_FOLDER+"/"+imageFile);
            img=Utils.toBufferedImage(imgIcon.getImage(), fillColor, this);
        }
        else{
            ImageIcon imgi=new ImageIcon(fs.getBytes(imageFile));
            img=Utils.toBufferedImage(imgi.getImage(), fillColor, this);
        }
        // re-check color (may be specified by point)
        fillColor=JDomUtility.getColorByPoint(e, FILL, img, Color.gray);
        setBackground(fillColor);
        
        frame=JDomUtility.getRectangle(e, FRAME, null);
        setMinimumSize(frame.getSize());
        
        child=e.getChild(SLICER);
        leftSlicer=JDomUtility.getIntAttr(child, JDomUtility.LEFT, 0);
        rightSlicer=JDomUtility.getIntAttr(child, JDomUtility.RIGHT, 0);
        topSlicer=JDomUtility.getIntAttr(child, JDomUtility.TOP, 0);
        bottomSlicer=JDomUtility.getIntAttr(child, JDomUtility.BOTTOM, 0);
        
        playerRect=JDomUtility.getRectangle(e, PLAYER, null);
        
        msgRect=JDomUtility.getRectangle(e, MESSAGES, null);
        bb=new BoxBase();
        bb.transparent=true;
        msgBox=new ActiveBox(null, this, 0, msgRect, bb);
        
        if((child=e.getChild(PROGRESS_BAR))!=null){
            hideProgressBar=JDomUtility.getBoolAttr(child, AUTO_HIDE, true);
            BoxBase bbProgres=getBoxBase(child, null);
            progressBarRect=JDomUtility.getRectangle(child, null, null);
            if(progressBarRect!=null){
                progressBar=new JProgressBar();
                BoxBase bbProgress=getBoxBase(child, null);
                progressBar.setBackground(bbProgress.backColor);
                progressBar.setForeground(bbProgress.textColor);
                progressBar.setFont(bbProgress.getFont());
                //progressBar.setBorderPainted(JDomUtility.getBoolAttr(child, BORDER, true));
                progressBar.setStringPainted(JDomUtility.getBoolAttr(child, SHOW_PERCENT, true));
                progressBar.setOpaque(false);
                progressBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
                add(progressBar);
                progressBar.setVisible(!hideProgressBar);
                hasProgress=true;
            }
        }
        
        if((child=e.getChild(PROGRESS_ANIMATION))!=null){
            Dimension d=JDomUtility.getDimension(child, null, null);
            Point source=JDomUtility.getPoint(child, SOURCE, null);
            Point pos=JDomUtility.getPoint(child, POS, null);
            int frames=JDomUtility.getIntAttr(child, FRAMES, 0);
            int delay=JDomUtility.getIntAttr(child, DELAY, 0);
            int direction=JDomUtility.getDirection(child, JDomUtility.DIRECTION_DOWN);
            hideProgressAnim=JDomUtility.getBoolAttr(child, AUTO_HIDE, true);
            if(d!=null && source!=null && frames>0 && delay>=AnimatedActiveBox.MIN_DELAY){
                int dx, dy;
                switch(direction){
                    case JDomUtility.DIRECTION_RIGHT:
                        dx=1; dy=0;
                        break;
                    case JDomUtility.DIRECTION_LEFT:
                        dx=-1; dy=0;
                        break;
                    case JDomUtility.DIRECTION_UP:
                        dx=0; dy=-1;
                        break;
                    default:
                        dx=0; dy=1;
                }
                int sx=JDomUtility.getIntAttr(child, STEP, d.width);
                int sy=JDomUtility.getIntAttr(child, STEP, d.height);
                progressAnimationRect=new Rectangle(pos, d);
                progressAnimation=new AnimatedActiveBox(null, this, 0, progressAnimationRect, getBoxBase(child, null));
                progressAnimation.setDelay(delay);
                ActiveBagContent abc=new ActiveBagContent(1, frames);
                for(int i=0; i<frames; i++){
                    ActiveBoxContent ab=new ActiveBoxContent();
                    ab.setImgContent(img, new Rectangle(source.x+(dx*i*sx), source.y+(dy*i*sy), d.width, d.height));
                    abc.addActiveBoxContent(ab);
                }
                progressAnimation.setContent(abc);
                
                progressAnimation.setVisible(!hideProgressAnim);
            }
        }
        
        if((child=e.getChild(STATUS_BAR))!=null){
            BoxBase bbMsgDef=getBoxBase(child, null);
            textColor=bbMsgDef.textColor;
            for(int i=0; i<NUM_MSG_AREAS; i++)
                if((child2=JDomUtility.getChildWithId(child, MSG_AREA, msgAreaNames[i]))!=null){
                    msgAreaRect[i]=JDomUtility.getRectangle(child2, null, null);
                    bb=getBoxBase(child2, bbMsgDef);
                    bb.textMargin=0;
                    msgArea[i]=new ActiveBox(null, this, 0, msgAreaRect[i], bb);
                    ActiveBoxContent abc=new ActiveBoxContent();
                    abc.setBoxBase(bb);
                    abc.setTxtAlign(JDomUtility.getAlignment(child2, null, null));                    
                    msgArea[i].setContent(abc);
                }
        }
        
        if((child=e.getChild(BUTTONS))!=null){
            child2=child.getChild(SETTINGS);
            Dimension dDef=JDomUtility.getDimension(child2, null, new Dimension());
            Point activeDef=JDomUtility.getOffset(child2, ACTIVE, null);
            Point overDef=JDomUtility.getOffset(child2, OVER, null);
            Point disabledDef=JDomUtility.getOffset(child2, DISABLED, null);
            for(int i=0; i<Constants.NUM_ACTIONS; i++)
                if((child2=JDomUtility.getChildWithId(child, BUTTON, Constants.ACTION_NAME[i]))!=null){
                    Point pos=JDomUtility.getPoint(child2, POS, null);
                    boolean toggle=JDomUtility.getBoolAttr(child2, TOGGLE, false);
                    if(pos!=null){
                        child3=child2.getChild(SETTINGS);
                        Dimension d=JDomUtility.getDimension(child3, null, dDef);
                        buttonsRect[i]=new Rectangle(pos, d);
                        Point source=JDomUtility.getPoint(child2, SOURCE, null);
                        Point active=JDomUtility.getOffset(child3, ACTIVE, activeDef);
                        Point over=JDomUtility.getOffset(child3, OVER, overDef);
                        Point disabled=JDomUtility.getOffset(child3, DISABLED, disabledDef);
                        buttons[i]=createButton(i, d, source, active, over, disabled, toggle);
                    }
                }
        }
        
        if((child=e.getChild(COUNTERS))!=null){
            child2=child.getChild(SETTINGS);
            Dimension dCounterDef=JDomUtility.getDimension(child2, COUNTER, new Dimension());
            Dimension dLabelDef=JDomUtility.getDimension(child2, LABEL, new Dimension());
            //int[] dCounterAlign=JDomUtility.getAlignment(child2, COUNTER, null);
            int[] dLabelAlign=JDomUtility.getAlignment(child2, LABEL, null);
            
            BoxBase bbCountersDef=getBoxBase(child, null);
            
            child2=child.getChild(DIGITS);
            Dimension dSizeDef=JDomUtility.getDimension(child2, null, null);
            Point dOriginDef=JDomUtility.getPoint(child2, SOURCE, null);
            
            for(int i=0; i<Constants.NUM_COUNTERS; i++)
                if((child2=JDomUtility.getChildWithId(child, COUNTER, Constants.counterNames[i]))!=null){
                    child3=child.getChild(SETTINGS);
                    bb=getBoxBase(child2, bbCountersDef);
                    bb.textMargin=0;
                    Point counterPos=JDomUtility.getPoint(child2, COUNTER, null);
                    if(counterPos!=null){
                        Dimension dCounter=JDomUtility.getDimension(child3, COUNTER, dCounterDef);
                        countersRect[i][0]=new Rectangle(counterPos, dCounter);
                        child4=child2.getChild(DIGITS);
                        Dimension dSize=JDomUtility.getDimension(child4, null, dSizeDef);
                        Point dOrigin=JDomUtility.getPoint(child4, SOURCE, dOriginDef);
                        counters[i]=new Counter(null, this, countersRect[i][0], bb);
                        counters[i].setSource(img, dOrigin, dSize);
                    }
                    Point labelPos=JDomUtility.getPoint(child2, LABEL, null);
                    if(labelPos!=null){
                        Dimension dLabel=JDomUtility.getDimension(child3, LABEL, dLabelDef);
                        countersRect[i][1]=new Rectangle(labelPos, dLabel);
                        countersLabel[i]=new ActiveBox(null, this, 0, countersRect[i][1], bb);
                        ActiveBoxContent abc=new ActiveBoxContent();
                        abc.setBoxBase(bb);
                        abc.setTxtAlign(JDomUtility.getAlignment(child2, LABEL, dLabelAlign));
                        abc.setTextContent(ps.getMsg("label_"+Constants.counterNames[i]));
                        countersLabel[i].setContent(abc);
                    }
                }
        }
        
        int c, l, k=0;
        for(c=0; c<Constants.NUM_COUNTERS; c++){
            boxes[k++]=counters[c];
            boxes[k++]=countersLabel[c];
        }
        for(l=0; l<NUM_MSG_AREAS; l++){
            boxes[k++]=msgArea[l];
        }
        boxes[k++]=msgBox;
        boxes[k++]=progressAnimation;
        initiated=1;
    }
    
    protected BoxBase getBoxBase(org.jdom.Element e, BoxBase defaultValue) throws Exception{
        org.jdom.Element child;
        if(e==null || (child=e.getChild(STYLE))==null) return defaultValue;
        BoxBase bb=
        defaultValue==null
        ? new BoxBase()
        : BoxBase.getBoxBase(defaultValue.getJDomElement());
        org.jdom.Element child2=child.getChild(JDomUtility.FONT);
        if(child2!=null)
            bb.setFont(JDomUtility.elementToFont(child2));
        bb.textColor=JDomUtility.getColorByPoint(child, FOREGROUND, img, bb.textColor);
        bb.backColor=JDomUtility.getColorByPoint(child, BACKGROUND, img, bb.backColor);
        if(bb.backColor==BoxBase.DEFAULT_BACK_COLOR)
            bb.transparent=true;
        else
            bb.transparent=JDomUtility.getBoolAttr(child, BoxBase.TRANSPARENT, bb.transparent);
        bb.textMargin=JDomUtility.getIntAttr(child, BoxBase.MARGIN, bb.textMargin);
        
        return bb;
    }
    
    @Override
    public void doLayout(){
        if(initiated<1){
            invalidate();
            return;
        }
        super.doLayout();
        
        if(player!=null)
            player.setBounds(translateRect(playerRect));
        
        if(progressBar!=null)
            progressBar.setBounds(translateRect(progressBarRect));
        
        for(int i=0; i<buttons.length; i++)
            if(buttons[i]!=null)
                buttons[i].setBounds(translateRect(buttonsRect[i]));
        
        for(int i=0; i<Constants.NUM_COUNTERS; i++){
            if(counters[i]!=null)
                counters[i].setLocation(translatePoint(countersRect[i][0].getLocation()));
            if(countersLabel[i]!=null)
                countersLabel[i].setLocation(translatePoint(countersRect[i][1].getLocation()));
        }
        
        if(msgBox!=null)
            msgBox.setBounds(translateRect(msgRect));
        
        if(progressAnimation!=null)
            progressAnimation.setBounds(translateRect(progressAnimationRect));
        
        for(int i=0; i<NUM_MSG_AREAS; i++)
            if(msgArea[i]!=null)
                msgArea[i].setBounds(translateRect(msgAreaRect[i]));
        
        if(player!=null)
            initiated=2;
    }
    
    @Override
    public void startAnimation(){
        if(progressAnimation!=null){
            if(hideProgressAnim)
                progressAnimation.setStartDelay(DEFAULT_PROGRESS_WAKE_ON);
            //progressAnimation.setVisible(true);
            progressAnimation.start();
        }
    }
    
    @Override
    public void stopAnimation(){
        if(progressAnimation!=null){
            if(hideProgressAnim)
                progressAnimation.setVisible(false);
            progressAnimation.stop(false);
        }
    }
    
    public void render(Graphics2D g2, Rectangle clip){
        if(player==null || initiated<2)
            return;
        
        if(!readyToPaint)
            readyToPaint=true;
        
        //super.paintComponent(g);
        
        if(counters!=null)
            for(int i=0; i<Constants.NUM_COUNTERS; i++){
                if(counters[i]!=null && counters[i].getBounds().equals(clip)){
                    Utils.drawImage(g2, img, counters[i].getBounds(), countersRect[i][0], this);
                    counters[i].update(g2, clip, this);
                    return;
                }
            }
        
        
        drawSlicedFrame(g2,
        getBounds(),
        frame,
        img,
        leftSlicer, rightSlicer, topSlicer, bottomSlicer);
        
        if(boxes!=null)
            for(int i=0; i<NUM_BOXES; i++){
                if(boxes[i]!=null){
                    boxes[i].update(g2, clip, this);
                    if(boxes[i].getBounds().equals(clip)){
                        return;
                    }
                }
            }
    }
    
    @Override
    public void setLocale(java.util.Locale l){
        super.setLocale(l);
        if(ps!=null && countersLabel!=null)
            for(int i=0; i<Constants.NUM_COUNTERS; i++)
                if(countersLabel[i]!=null)
                    countersLabel[i].setTextContent(ps.getMsg("label_"+Constants.counterNames[i]));
    }
    
    protected AbstractButton createButton(int buttonId, Dimension d, Point source, Point active, Point over, Point disabled, boolean toggle){
        
        final AbstractButton button;
        Action action=ps.getAction(buttonId);
        
        if(source!=null){
            ImageIcon icon;
            icon=new ImageIcon(img.getSubimage(source.x, source.y, d.width, d.height));
            if(!toggle)
                //button=new JButton(icon);
                button=new JButton(action);
            else
                //button=new JToggleButton(icon);
                button=new JToggleButton(action);
            
            button.setIcon(icon);
            button.setText(null);
            
            if(active!=null){
                icon=new ImageIcon(img.getSubimage(source.x+active.x, source.y+active.y, d.width, d.height));
                if(!toggle)
                    button.setPressedIcon(icon);
                else
                    button.setSelectedIcon(icon);
            }
            if(over!=null){
                icon=new ImageIcon(img.getSubimage(source.x+over.x, source.y+over.y, d.width, d.height));
                button.setRolloverIcon(icon);
            }
            if(disabled!=null){
                icon=new ImageIcon(img.getSubimage(source.x+disabled.x, source.y+disabled.y, d.width, d.height));
                button.setDisabledIcon(icon);
            }
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBorder(new EmptyBorder(0,0,0,0));
            button.setContentAreaFilled(false);
            button.setMargin(new Insets(0,0,0,0));
        }
        else{
            button=new JButton(action);
            //button=new JButton(ac.getMsg("button_"+s+"_caption"));
        }
        
        //button.setToolTipText(ac.getMsg("button_"+s+"_tooltip"));
        button.setPreferredSize(buttonsRect[buttonId].getSize());
        button.setVisible(true);
        //button.setEnabled(false);
        //button.setActionCommand(s);
        if(toggle){
            Object o=action.getValue(AbstractAction.DEFAULT);
            button.setSelected(o!=null && o.equals(Boolean.FALSE));
            action.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent evt){
                    if(evt.getPropertyName().equals("selected")){
                        button.setSelected(evt.getNewValue().equals(Boolean.FALSE));
                    }
                }
            });
        }
        add(button);
        return button;
    }
    
    Point translatePoint(Point pt){
        Rectangle bounds=getBounds();
        Dimension extra=new Dimension(bounds.width-frame.width, bounds.height-frame.height);
        Point result=new Point(pt);
        if(pt.x>rightSlicer) result.x+=extra.width;
        if(pt.y>bottomSlicer) result.y+=extra.height;
        return result;
    }
    
    Rectangle translateRect(Rectangle r){
        Rectangle bounds=getBounds();
        Dimension extra=new Dimension(bounds.width-frame.width, bounds.height-frame.height);
        Rectangle result=new Rectangle(r.x, r.y, r.width, r.height);
        if(r.x>rightSlicer){
            result.x+=extra.width;
        }
        else{
            if(r.x+r.width>leftSlicer) result.width+=extra.width;
        }
        if(r.y>bottomSlicer){
            result.y+=extra.height;
        }
        else{
            if(r.y+r.height>topSlicer) result.height+=extra.height;
        }
        return result;
    }
    
    @Override
    public void setProgressMax(int max){
        super.setProgressMax(max);
        if(progressBar!=null)
            progressBar.setMaximum(max);
    }
    
    @Override
    public void setProgressValue(int value){
        super.setProgressValue(value);
        if(progressBar!=null){
            progressBar.setValue(value);
            if(!progressBar.isVisible() && (System.currentTimeMillis()-progressStartTime)>=DEFAULT_PROGRESS_WAKE_ON)
                progressBar.setVisible(true);
        }
    }
    
    @Override
    public void endProgress(){
        super.endProgress();
        if(progressBar!=null && hideProgressBar==true)
            progressBar.setVisible(false);
    }
    
}