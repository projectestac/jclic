/*
 * File    : ActiveBoxContent.java
 * Created : 02-may-2001 15:29
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

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.PlayStation;
import edu.xtec.jclic.bags.MediaBag;
import edu.xtec.jclic.bags.MediaBagElement;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.media.ActiveMediaPlayer;
import edu.xtec.jclic.media.MediaContent;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ResourceManager;
import edu.xtec.util.StrUtils;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Map;

/**
 * This class defines a content that can be displayed by {@link edu.xtec.jclic.boxes.ActiveBox}
 * objects. This content can be a text, an image, a fragment of an image or a
 * combination of text and images. The style (colours, font and size, etc.) can be specified
 * in a {@link edu.xtec.jclic.boxes.BoxBase} object. It stores also information about
 * the optimal size and location of the <CODE>ActiveBox</CODE>.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ActiveBoxContent extends Object implements Cloneable, Domable{
    
    public BoxBase bb=null;
    public Dimension dimension=null;
    public Boolean border=null;
    public String text=null;
    public String imgName=null;
    public Shape imgClip=null;
    public MediaContent mediaContent=null;
    public int[] imgAlign=new int[]{JDomUtility.ALIGN_MIDDLE,JDomUtility.ALIGN_MIDDLE};
    public int[] txtAlign=new int[]{JDomUtility.ALIGN_MIDDLE,JDomUtility.ALIGN_MIDDLE};
    public boolean avoidOverlapping=false;
    public int id=-1;
    public int item=-1;
    
    public Image img=null;
    
    // static fields
    protected static ActiveBoxContent EMPTY_CONTENT;
    
    // transient properties
    public Object userData=null;
    public String rawText=null;
    public String htmlText=null;
    public String innerHtmlText=null;
    public boolean animated=false;
    public ActiveMediaPlayer amp=null;
    
    
    protected void copyRawDataFrom(ActiveBoxContent src){
        bb=src.bb;
        dimension=src.dimension;
        border=src.border;
        text=src.text;
        imgName=src.imgName;
        imgClip=src.imgClip;
        mediaContent=src.mediaContent;
        imgAlign=src.imgAlign;
        txtAlign=src.txtAlign;
        avoidOverlapping=src.avoidOverlapping;
        id=src.id;
        item=src.item;
        img=src.img;
        userData=src.userData;
        rawText=src.rawText;
        htmlText=src.htmlText;
        innerHtmlText=src.innerHtmlText;
        animated=src.animated;
        amp=src.amp;
    }
    
    /** Creates new ActiveBoxContent */
    public ActiveBoxContent() {
    }
    
    public static ActiveBoxContent getEmptyContent(){
        if(EMPTY_CONTENT==null)
            EMPTY_CONTENT=new ActiveBoxContent();
        return EMPTY_CONTENT;
    }
    
    public static final String ELEMENT_NAME="cell";
    protected static final String
    ID="id", ITEM="item",
    WIDTH="width", HEIGHT="height", BORDER="border",
    IMAGE="image", TXTALIGN="txtAlign", IMGALIGN="imgAlign",
    AVOID_OVERLAPPING="avoidOverlapping";
    
    public org.jdom.Element getJDomElement(){
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        
        if(id!=-1)
            e.setAttribute(ID, Integer.toString(id));
        
        if(item!=-1)
            e.setAttribute(ITEM, Integer.toString(item));
        
        JDomUtility.setAlignProp(e, TXTALIGN, txtAlign, true);
        JDomUtility.setAlignProp(e, IMGALIGN, imgAlign, true);
        if(avoidOverlapping)
            e.setAttribute(AVOID_OVERLAPPING, JDomUtility.boolString(avoidOverlapping));
        
        if(dimension!=null){
            e.setAttribute(WIDTH, Integer.toString(dimension.width));
            e.setAttribute(HEIGHT, Integer.toString(dimension.height));
        }
        if(border!=null)
            e.setAttribute(BORDER, JDomUtility.boolString(border.booleanValue()));
        
        if(imgName!=null)
            e.setAttribute(IMAGE, imgName);
        
        if(bb!=null)
            e.addContent(bb.getJDomElement());
        
        if(mediaContent!=null)
            e.addContent(mediaContent.getJDomElement());
        
        if(text!=null)
            JDomUtility.setParagraphs(e, text);
        
        return e;
    }
    
    public static final int EMPTY_CELL=0, ONLY_ID=1, HAS_CONTENT=2;
    public int testCellContents(){
        if(JDomUtility.isDefaultAlign(txtAlign)
        && JDomUtility.isDefaultAlign(imgAlign)
        && !avoidOverlapping
        && dimension==null
        && border==null && imgName==null && bb==null && mediaContent==null
        && (text==null || text.length()==0)){
            return ((id==-1 && item==-1) ? EMPTY_CELL : ONLY_ID);
        }
        return HAS_CONTENT;
    }
    
    public static ActiveBoxContent getActiveBoxContent(org.jdom.Element e, MediaBag mediaBag) throws Exception{
        
        ActiveBoxContent abc=new ActiveBoxContent();
        abc.setProperties(e, mediaBag);
        return abc;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        
        JDomUtility.checkName(e, ELEMENT_NAME);
        MediaBag mediaBag=(MediaBag)aux;
        
        org.jdom.Element child;
        
        id=JDomUtility.getIntAttr(e, ID, id);
        item=JDomUtility.getIntAttr(e, ITEM, item);
        if(e.getAttribute(JDomUtility.HALIGN)!=null || e.getAttribute(JDomUtility.VALIGN)!=null){
            // old version!!!
            txtAlign[0]=JDomUtility.getHAlign(e, txtAlign[0]);
            txtAlign[1]=JDomUtility.getVAlign(e, txtAlign[1]);
            imgAlign[0]=txtAlign[0];
            imgAlign[1]=txtAlign[1];
        }
        else{
            txtAlign=JDomUtility.getAlignProp(e, TXTALIGN, txtAlign);
            imgAlign=JDomUtility.getAlignProp(e, IMGALIGN, imgAlign);
        }
        avoidOverlapping=JDomUtility.getBoolAttr(e, AVOID_OVERLAPPING, avoidOverlapping);
        
        dimension=JDomUtility.getDimensionAttr(e, WIDTH, HEIGHT, dimension);
        border=JDomUtility.getBooleanAttr(e, BORDER, border);
        imgName=FileSystem.stdFn(e.getAttributeValue(IMAGE));
        if((child=e.getChild(BoxBase.ELEMENT_NAME))!=null)
            setBoxBase(BoxBase.getBoxBase(child));
        if((child=e.getChild(MediaContent.ELEMENT_NAME))!=null)
            mediaContent=MediaContent.getMediaContent(child);
        setTextContent(JDomUtility.getParagraphs(e));
        
        if(mediaBag!=null)
            realizeContent(mediaBag);
    }
    
    public static void listReferences(org.jdom.Element e, Map<String,String> map){
        if(e!=null){
            String s=e.getAttributeValue(IMAGE);
            if(s!=null && s.length()>0)
                map.put(s, Constants.MEDIA_OBJECT);
            org.jdom.Element child=e.getChild(MediaContent.ELEMENT_NAME);
            if(child!=null)
                MediaContent.listReferences(child, map);
        }
    }
    
    @Override
    public Object clone(){
        ActiveBoxContent abc=null;
        try{
            abc=(ActiveBoxContent)super.clone();
            abc.txtAlign=new int[]{txtAlign[0], txtAlign[1]};
            abc.imgAlign=new int[]{imgAlign[0], imgAlign[1]};
        } catch(Exception ex){
            System.err.println("Unexpected error cloning ActiveBoxContent!");
        }
        return abc;
    }
    
    public void realizeContent(MediaBag mediaBag) throws Exception{
        //todo: check global img and imgclip
        //img=null;
        //animated=false;
        if(imgName!=null){
            MediaBagElement mbe=mediaBag.getImageElement(imgName);
            if(mbe!=null){
                img=mbe.getImage();
                animated=mbe.animated;
            }
        }
        if(mediaContent!=null){
            mediaContent.registerContentTo(mediaBag);
            if(img==null && (text==null || text.length()==0)){
                String s=mediaContent.getIconName();
                img=ResourceManager.getImageIcon("icons/"+s).getImage();
            }
        }
        checkHtmlText(mediaBag);
    }
    
    public boolean isEmpty(){
        return (text==null && img==null);
    }
    
    public boolean isEquivalent(ActiveBoxContent abc, boolean checkCase){
        if(abc==this) return true;
        boolean result=false;
        if(abc!=null){
            if(isEmpty() && abc.isEmpty())
                result=(id==abc.id);
            else
                result=(text==null
                        ? abc.text==null
                        : (checkCase ? text.equals(abc.text) : text.equalsIgnoreCase(abc.text)))
                && (mediaContent==null
                    ? abc.mediaContent==null
                    : mediaContent.isEquivalent(abc.mediaContent))
                && (img == abc.img)
                && (imgClip==null ? abc.imgClip==null : imgClip.equals(abc.imgClip));
        }
        return result;
    }
    
    @Override
    public boolean equals(Object o){
        boolean result=(o==this);
        if(!result && o!=null && o instanceof ActiveBoxContent){
            ActiveBoxContent abc=(ActiveBoxContent)o;
            result = isEquivalent(abc, true)
            && ((bb==null && abc.bb==null) || (bb!=null && bb.equals(abc.bb)))
            && txtAlign[0]==abc.txtAlign[0] && txtAlign[1]==abc.txtAlign[1]
            && imgAlign[0]==abc.imgAlign[0] && imgAlign[1]==abc.imgAlign[1]
            && avoidOverlapping==abc.avoidOverlapping;
        }
        return result;
    }
    
    public void setTextContent(String tx){
        // only plain text!
        if(tx!=null){
            rawText=tx;
            text=tx;
            checkHtmlText(null);
        }
        else{
            rawText=null;
            text=null;
            htmlText=null;
            innerHtmlText=null;
        }
    }
    
    protected void checkHtmlText(MediaBag mediaBag){
        htmlText=null;
        innerHtmlText=null;
        
        if(text!=null && text.substring(0).toLowerCase().startsWith("<html>")){
            htmlText=text;
            if(mediaBag!=null){
                FileSystem fs=mediaBag.getProject().getFileSystem();
                String path=fs.root;
                if(!fs.isUrlBased())
                    path="file:"+FileSystem.sysFn(path);
                htmlText=StrUtils.replace(htmlText, "SRC=\"", "SRC=\""+path);
                htmlText=StrUtils.replace(htmlText, "src=\"", "src=\""+path);
            }
            String s=htmlText.substring(0).toLowerCase();
            if(s.indexOf("<body")==-1){
                int s2=s.indexOf("</html>");
                if(s2>=0){
                    innerHtmlText=htmlText.substring(6, s2);
                }
            }
        }
    }
    
    public void setImgContent(Image setImg, Shape setImgClip){
        img=setImg;
        imgName=null;
        imgClip=setImgClip;
    }
    
    public void setImgAlign(int[] align){
        imgAlign[0]=align[0];
        imgAlign[1]=align[1];
    }
    
    public void setImgAlign(int h, int v){
        imgAlign[0]=h;
        imgAlign[1]=v;
    }
    
    public void setTxtAlign(int[] align){
        txtAlign[0]=align[0];
        txtAlign[1]=align[1];
    }
    
    public void setTxtAlign(int h, int v){
        txtAlign[0]=h;
        txtAlign[1]=v;
    }
    
    public void prepareMedia(PlayStation ps){
        if(mediaContent!=null)
            amp=ps.getActiveMediaPlayer(mediaContent);
        else
            amp=null;
    }
    
    public void setBoxBase(BoxBase boxBase){
        bb=boxBase;
    }
    
    public void setDimension(Dimension d){
        dimension=d;
    }
    
    public Dimension getDimension(){
        return dimension;
    }
    
    public void setBorder(boolean b){
        border=b;
    }
    
    public void copyStyleTo(ActiveBoxContent abc){
        if(abc!=null){
            abc.setBoxBase(bb);
            if(border!=null)
                abc.setBorder(border.booleanValue());
        }
    }
    
    public String getDescription(){
        StringBuilder result=new StringBuilder();
        if(text!=null && text.length()>0) result.append(text);
        else if(imgName!=null)
            result.append("IMG:").append(imgName);
        else if(imgClip!=null){
            Rectangle r=imgClip.getBounds();
            result.append("[").append(r.x).append(",").append(r.y).append(",");
            result.append(r.width).append(",").append(r.height).append("]");
        }
        
        if(mediaContent!=null){
            if(result.length()>0) result.append(' ');
            result.append(mediaContent.getDescription());
        }
        return result.substring(0);
    }
}
