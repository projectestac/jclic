/*
 * File    : TextActivityDocument.java
 * Created : 28-may-2001 10:35
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

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.clic3.*;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.StreamIO;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.text.*;


/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class TextActivityDocument extends DefaultStyledDocument {
    
    public static final int NUM_TABS=30;
    public static final int DEFAULT_TAB=12;
    public static final Color DEFAULT_TARGET_COLOR=Color.blue;
    public static final Color DEFAULT_TARGET_ERROR_COLOR=Color.red;
    public static final String TARGET="target", TARGET_ERROR="targetError", FILL="fill";
    public static final int TT_FREE=0, TT_CHAR=1, TT_WORD=2, TT_PARAGRAPH=3;
    
    protected int tabSpc=DEFAULT_TAB;
    protected int lastBoxId=0;
    protected StyleContext styleContext;
    public TargetMarkerBag tmb;
    // added
    public ActiveBagContent boxesContent;
    public ActiveBagContent popupsContent;
    //--
    protected int targetType=TT_FREE;
    
    /** Creates new TextActivityDocument */
    public TextActivityDocument(StyleContext sc) {
        super(sc);
        styleContext=sc;
        checkStyleContext(styleContext);
        tmb=new TargetMarkerBag();
        boxesContent=new ActiveBagContent(1,1);
        popupsContent=new ActiveBagContent(1,1);
    }
    
    public static void checkStyleContext(StyleContext sc){
        Style targetStyle=sc.getStyle(TARGET);
        if(targetStyle==null){            
            targetStyle=sc.addStyle(TARGET, sc.getStyle(StyleContext.DEFAULT_STYLE));
            StyleConstants.setForeground(targetStyle, DEFAULT_TARGET_COLOR);
            targetStyle.addAttribute(TARGET, Boolean.TRUE);
        }
        Style targetErrorStyle=sc.getStyle(TARGET_ERROR);
        if(targetErrorStyle==null){
            targetErrorStyle=sc.addStyle(TARGET_ERROR, targetStyle);
            StyleConstants.setForeground(targetErrorStyle, DEFAULT_TARGET_ERROR_COLOR);
        }
    }
    
    public void readClic3Data(Clic3Activity c3a, TextActivityBase tab) throws Exception{
        
        BoxBase bb=c3a.getBoxBase(0);
        boxesContent.setBoxBase(c3a.getBoxBase(1));
        popupsContent.setBoxBase(c3a.getBoxBase(3));
        
        Style regular=boxBaseToStyledDocument(bb, this);
        
        setTabSpc(c3a.tabSpc);
        
        Style targetStyle=getStyle(TARGET);
        StyleConstants.setBackground(targetStyle, c3a.colorUsuari[1]);
        StyleConstants.setForeground(targetStyle, c3a.colorUsuari[0]);
        
        Style targetError=getStyle(TARGET_ERROR);
        StyleConstants.setBackground(targetError, c3a.colorError[1]);
        StyleConstants.setForeground(targetError, c3a.colorError[0]);
        
        boolean multiTarget=(c3a.puzMode==Clic3.FORATS);
        StringBuilder sb=new StringBuilder();
        sb.append(Clic3.CHBLOCK).append(Clic3.CHINC);
        StringTokenizer st=new StringTokenizer(c3a.txBase, sb.substring(0), true);
        boolean intoBlock=false;
        boolean intoTarget=false;
        TargetMarker tm=null;
        int targetElementCount=0;
        int k=0;
        //used for popups in targets
        int w=0, h=0;
        boolean leftAlign=false, onlyPlay=false;
        while(st.hasMoreTokens() || intoTarget){
            String t;
            try{
                t=st.nextToken();
            } catch(java.util.NoSuchElementException ex){
                t=new String(new char[]{Clic3.CHINC});
                targetElementCount=6;
            }
            if(t.charAt(0)==Clic3.CHBLOCK)
                intoBlock=intoBlock ? false : true;
            else if(t.charAt(0)==Clic3.CHINC){
                if(multiTarget){
                    targetElementCount++;
                    if(targetElementCount==1){
                        // create target
                        tm=new TargetMarker(this);
                        tm.target=new TextTarget();
                        tm.begOffset=getLength();
                    }
                    
                    if(targetElementCount>6){
                        targetElementCount=0;
                        intoTarget=false;
                        if(tm!=null){
                          tm.endOffset=getLength();
                          tmb.add(tm);
                        }
                    } else{
                        intoTarget=true;
                    }
                }
                else{
                    intoTarget=intoTarget ? false : true;
                    if(intoTarget){
                        tm=new TargetMarker(this);
                        tm.begOffset=getLength();
                    }
                    else if(tm!=null){
                        tm.endOffset=getLength();
                        tmb.add(tm);
                    }
                }
            }
            else if(intoBlock){
                ActiveBoxContent ab=new ActiveBoxContent();
                StringTokenizer stb=new StringTokenizer(t, ",");
                w=Integer.parseInt(stb.nextToken());
                h=Integer.parseInt(stb.nextToken());
                ab.setDimension(new Dimension(w, h));
                ab.setBorder((Integer.parseInt(stb.nextToken()))!=0);
                boolean b=((Integer.parseInt(stb.nextToken()))!=0);
                ab.txtAlign[0]=b ? JDomUtility.ALIGN_LEFT : JDomUtility.ALIGN_MIDDLE;
                ab.imgAlign[0]=ab.txtAlign[0];
                c3a.setActiveBoxTextContent(ab, stb.nextToken("").substring(1));
                
                //ab.setBoxBase(tab.boxesContent.bb);
                ab.setBoxBase(boxesContent.bb);
                
                ab.userData=getEndPosition();
                
                boxesContent.addActiveBoxContent(ab);
                
                JPanelActiveBox jpab=new JPanelActiveBox(null, null, tab.getProject().getBridge().getComponent());
                jpab.setAlignmentY(0.9f);
                jpab.setActiveBoxContent(ab);
                MutableAttributeSet attr=new SimpleAttributeSet(regular);
                StyleConstants.setComponent(attr, jpab);
                insertString(getLength(), " ", attr);
            }
            else if(intoTarget){
                if(targetElementCount==0)
                    insertString(getLength(), t, getStyle(TARGET));
                else switch(targetElementCount){
                    case 1:
                        StringTokenizer stx=new StringTokenizer(t, ",");
                        if (tm != null) {
                            tm.target.iniChar = stx.nextToken().charAt(0);
                            tm.target.isList = (stx.nextToken().compareTo("1") == 0);
                            tm.target.numIniChars = Integer.parseInt(stx.nextToken());
                            tm.target.maxLenResp = Integer.parseInt(stx.nextToken());
                            tm.target.infoMode = Integer.parseInt(stx.nextToken());
                            int v = Integer.parseInt(stx.nextToken());
                            tm.target.popupDelay = v & 0xFF;
                            tm.target.popupMaxTime = (v & 0xFF00) >> 8;
                        }
                        w=Integer.parseInt(stx.nextToken());
                        h=Integer.parseInt(stx.nextToken());
                        int flags=Integer.parseInt(stx.nextToken());
                        leftAlign=((flags&1)==1);
                        onlyPlay=((flags&2)==2);
                        break;
                    case 2:
                        if(tm!=null && t.length()>0)
                            tm.target.setAnswer(t);
                        break;
                    case 3:
                        StringTokenizer sty=new StringTokenizer(t, "\n\r");
                        ArrayList<String> al=new ArrayList<String>();
                        while(sty.hasMoreTokens()){
                            String s=sty.nextToken();
                            if(s.length()>0)
                                al.add(s);
                        }
                        if(tm!=null && al.size()>0){
                            tm.target.options=new String[al.size()];
                            for(int i=0; i<al.size(); i++){
                                tm.target.options[i]=(String)(al.get(i));
                            }
                        }
                        break;
                    case 4:
                        if(tm!=null && t.length()>0)
                            tm.target.iniText=t;
                        break;
                    case 5:
                        if(tm!=null && t.length()>0){
                            tm.target.popupContent=new ActiveBoxContent();
                            tm.target.popupContent.setDimension(new Dimension(w, h));
                            c3a.setActiveBoxTextContent(tm.target.popupContent, t);
                            
                            tm.target.popupContent.setBoxBase(popupsContent.bb);
                            
                            tm.target.popupContent.txtAlign[0]=leftAlign ? JDomUtility.ALIGN_LEFT : JDomUtility.ALIGN_MIDDLE;
                            tm.target.popupContent.imgAlign[0]=tm.target.popupContent.txtAlign[0];
                            tm.target.onlyPlay=onlyPlay;
                            
                            popupsContent.addActiveBoxContent(tm.target.popupContent);
                        }
                        break;
                    case 6:
                        insertString(getLength(), t, getStyle(TARGET));
                        break;
                }
            }
            else{
                insertString(getLength(), t, getStyle(StyleContext.DEFAULT_STYLE));
            }
        }
        tmb.setPositions();
    }
    
    public static JPanelActiveBox insertBox(ActiveBoxContent ab, int atPos, TextActivityDocument doc, TextActivityBase tab, AttributeSet atr) throws Exception{
        
        if(atr==null)
            atr=doc.getStyle(StyleContext.DEFAULT_STYLE);
        
        if(atPos<0)
            atPos=doc.getLength();
        
        doc.boxesContent.addActiveBoxContent(ab);
                
        JPanelActiveBox jpab=new JPanelActiveBox(null, ab.bb, tab.getProject().getBridge().getComponent());
        jpab.setAlignmentY(0.9f);
        jpab.setActiveBoxContent(ab);
        SimpleAttributeSet satr=new SimpleAttributeSet(atr);
        StyleConstants.setComponent(satr, jpab);
        doc.insertString(atPos, " ", satr);
        return jpab;
    }
    
    public static Style boxBaseToStyledDocument(BoxBase bb, StyledDocument sd){
        Style style=sd.getStyle(StyleContext.DEFAULT_STYLE);
        boxBaseToStyle(bb, style);
        return style;
    }
    
    public static void boxBaseToStyle(BoxBase bb, Style st){
        StyleConstants.setFontFamily(st, bb.getFont().getFamily());
        StyleConstants.setFontSize(st, bb.getFont().getSize());
        StyleConstants.setBold(st, bb.getFont().isBold());
        StyleConstants.setItalic(st, bb.getFont().isItalic());
        StyleConstants.setBackground(st, bb.backColor);
        StyleConstants.setForeground(st, bb.textColor);
    }
    
    public void setTabSpc(int newTabSpc){
        tabSpc=newTabSpc;
        setStyledDocumentTabSpc(tabSpc, this, styleContext);
    }
    
    protected static AttributeSet getAttributes(TextActivityDocument doc, AttributeSet a, org.jdom.Element e) throws Exception {
        
        int nAttributes=e.getAttributes().size();
        
        if(nAttributes<1)
            return a;
        
        String parentStyle=e.getAttributeValue(STYLE);
        Style style=(parentStyle!=null ? doc.getStyle(parentStyle) : null);
        if(style!=null && nAttributes==1)
            return style;
        
        MutableAttributeSet atr=new SimpleAttributeSet();
        atr.setResolveParent(style!=null ? style : a);
        
        fillAttributes(atr, e);
        
        return atr;
    }
    
    protected static void fillAttributes(MutableAttributeSet a, org.jdom.Element e) throws Exception {
        java.util.List atrList=e.getAttributes();
        
        for(int i=0; i<atrList.size(); i++){
            org.jdom.Attribute atr=(org.jdom.Attribute)atrList.get(i);
            String atrName=atr.getName();
            if(atrName.equals(StyleConstants.FontFamily.toString()))
                StyleConstants.setFontFamily(a, edu.xtec.util.FontCheck.getValidFontFamilyName(atr.getValue()));
            else if(atrName.equals(StyleConstants.FontSize.toString()))
                StyleConstants.setFontSize(a, atr.getIntValue());
            else if(atrName.equals(StyleConstants.Bold.toString()))
                StyleConstants.setBold(a, atr.getBooleanValue());
            else if(atrName.equals(StyleConstants.Italic.toString()))
                StyleConstants.setItalic(a, atr.getBooleanValue());
            else if(atrName.equals(StyleConstants.Background.toString()))
                StyleConstants.setBackground(a, JDomUtility.stringToColor(atr.getValue()));
            else if(atrName.equals(StyleConstants.Foreground.toString()))
                StyleConstants.setForeground(a, JDomUtility.stringToColor(atr.getValue()));
            // other attributes:
            else if(atrName.equals(TARGET))
                a.addAttribute(atrName, atr.getBooleanValue());
            // Paragraph attributes:
            else if(atrName.equals(StyleConstants.BidiLevel.toString()))
                StyleConstants.setBidiLevel(a, atr.getIntValue());
            else if(atrName.equals(StyleConstants.Alignment.toString()))
                StyleConstants.setAlignment(a, atr.getIntValue());
        }
    }
    
    protected static void addStyle(TextActivityDocument doc, org.jdom.Element e) throws Exception {
        JDomUtility.checkName(e, STYLE);
        Style s;
        String styleName=e.getAttributeValue(NAME);
        
        s=doc.getStyle(styleName);
        if(s==null){
            String baseName=JDomUtility.getStringAttr(e, BASE, StyleContext.DEFAULT_STYLE, false);
            Style base=doc.getStyle(baseName);
            s=doc.addStyle(styleName, base);
        }
        
        fillAttributes(s, e);
    }
    
    public static void setStyledDocumentTabSpc(int tab, StyledDocument sd, StyleContext sc){
        
        Style regular= sd.getStyle(StyleContext.DEFAULT_STYLE);
        
        FontMetrics fm=sc.getFontMetrics(sd.getFont(regular));
        float sep=fm.charWidth(' ')*tab;
        TabStop[] tabs=new TabStop[NUM_TABS];
        for(int i=0; i<30; i++){
            tabs[i]=new TabStop(sep*(i+1));
        }
        StyleConstants.setTabSet(regular, new TabSet(tabs));
        sd.setParagraphAttributes(0, sd.getLength(), regular, true);
        
        String tabStr=Integer.toString(tab);
        Object o=regular.getAttribute(TABSPC);
        if(o==null || !tabStr.equals(o)){
            if(o!=null)
                regular.removeAttribute(TABSPC);
            regular.addAttribute(TABSPC, tabStr);
        }
    }
    
    public int getTabSpc(){
        return tabSpc;
    }
    
    
    public static StyleContext copyStylesFrom(StyleContext src, StyleContext dest,
    boolean fontFace, boolean fontSize,
    boolean style, boolean colour, boolean targetColour, boolean errorColour){
        
        boolean result=false;
        StyleContext clon;
        try{
            clon=(StyleContext)StreamIO.cloneObject(dest);
        } catch(Exception ex){
            System.err.println("Error cloning StyleContext:\n"+ex);
            return src;
        }
        
        Style mainStyle=clon.getStyle(StyleContext.DEFAULT_STYLE);
        Style srcMainStyle=src.getStyle(StyleContext.DEFAULT_STYLE);
        
        if(fontFace){
            String font=StyleConstants.getFontFamily(srcMainStyle);
            if(font!=null && font.length()>0 && !font.equals(StyleConstants.getFontFamily(mainStyle))){
                StyleConstants.setFontFamily(mainStyle, font);
                result=true;
            }
        }
        if(fontSize){
            int size=StyleConstants.getFontSize(srcMainStyle);
            if(size>0 && size!=StyleConstants.getFontSize(mainStyle)){
                StyleConstants.setFontSize(mainStyle, size);
                result=true;
            }
        }
        if(colour){
            Color color=StyleConstants.getForeground(srcMainStyle);
            if(color!=null && !color.equals(StyleConstants.getForeground(mainStyle))){
                StyleConstants.setForeground(mainStyle, color);
                result=true;
            }
            
            color=StyleConstants.getBackground(srcMainStyle);
            if(color!=null && !color.equals(StyleConstants.getBackground(mainStyle))){
                StyleConstants.setBackground(mainStyle, color);
                result=true;
            }
        }
        if(style){
            boolean b=StyleConstants.isBold(srcMainStyle);
            if(b!=StyleConstants.isBold(mainStyle)){
                StyleConstants.setBold(mainStyle, b);
                result=true;
            }
            
            b=StyleConstants.isItalic(srcMainStyle);
            if(b!=StyleConstants.isItalic(mainStyle)){
                StyleConstants.setItalic(mainStyle, b);
                result=true;
            }
            
            b=StyleConstants.isUnderline(srcMainStyle);
            if(b!=StyleConstants.isUnderline(mainStyle)){
                StyleConstants.setUnderline(mainStyle, b);            
                result=true;
            }
        }
        
        if(targetColour){
            Style targetStyle=clon.getStyle(TARGET);
            Style srcTargetStyle=src.getStyle(TARGET);
            if(targetStyle!=null && srcTargetStyle!=null){
                Color color=StyleConstants.getForeground(srcTargetStyle);
                if(color!=null && !color.equals(StyleConstants.getForeground(targetStyle))){
                    StyleConstants.setForeground(targetStyle, color);
                    result=true;
                }
                
                color=StyleConstants.getBackground(srcTargetStyle);
                if(color!=null && !color.equals(StyleConstants.getBackground(targetStyle))){
                    StyleConstants.setBackground(targetStyle, color);
                    result=true;
                }                
            }
        }
        
        if(errorColour){
            Style errorStyle=clon.getStyle(TARGET_ERROR);
            Style srcErrorStyle=src.getStyle(TARGET_ERROR);
            if(errorStyle!=null && srcErrorStyle!=null){
                Color color=StyleConstants.getForeground(srcErrorStyle);
                if(color!=null && !color.equals(StyleConstants.getForeground(errorStyle))){
                    StyleConstants.setForeground(errorStyle, color);
                    result=true;
                }
                
                color=StyleConstants.getBackground(srcErrorStyle);
                if(color!=null && !color.equals(StyleConstants.getBackground(errorStyle))){
                    StyleConstants.setBackground(errorStyle, color);
                    result=true;
                }                
            }
        }       
        return result ? clon : dest;
    }
    
    public static final String ELEMENT_NAME="document";
    public static final String STYLE="style", TABSPC="tabWidth",
    TEXT="text", P="p", BASE="base", NAME="name";
    
    public org.jdom.Element getJDomElement() throws Exception{
        return getJDomElement(styleContext);
    }
    
    public org.jdom.Element getJDomElement(StyleContext sc) throws Exception{
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        tmb.setPositions();
        addStylesToElement(e, sc, tabSpc);
        e.addContent(getJDomElement(getDefaultRootElement()));
        return e;
    }
    
    public static void addStylesToElement(org.jdom.Element e, StyleContext sc, int tabSpc) throws Exception{
        java.util.Enumeration<?> styleNamesEnum=sc.getStyleNames();
        // use Stack for reverse order
        java.util.Stack<String> styleNames=new java.util.Stack<String>();
        while(styleNamesEnum.hasMoreElements())
            styleNames.push((String)styleNamesEnum.nextElement());
        
        while(!styleNames.empty()){
            org.jdom.Element s=new org.jdom.Element(STYLE);
            String styleName=(String)styleNames.pop();
            s.setAttribute(NAME, styleName);
            Style style=sc.getStyle(styleName);
            Style parent=(Style)style.getAttribute(AttributeSet.ResolveAttribute);
            if(parent!=null)
                s.setAttribute(BASE, parent.getName());
            setJDomElementAttributes(s, style, true);
            if(styleName.equals(StyleContext.DEFAULT_STYLE) && s.getAttribute(TABSPC)==null)
                s.setAttribute(TABSPC, Integer.toString(tabSpc));
            e.addContent(s);
        }
    }
    
    public org.jdom.Element getJDomElement(Element element) throws Exception{
        org.jdom.Element e;
        String elementName=element.getName();
        AttributeSet atr=element.getAttributes();
        String style=(String)(atr.getAttribute(AttributeSet.NameAttribute));
        
        if(elementName.equals("component")){
            Object cmp=atr.getAttribute(StyleConstants.ComponentAttribute);
            if(cmp!=null && cmp instanceof JPanelActiveBox){
                JPanelActiveBox jpab=(JPanelActiveBox)cmp;
                return jpab.getActiveBox().getContent().getJDomElement();
            }
        }
        else if(TARGET.equals(style)){
            elementName=TARGET;
            TargetMarker tm=tmb.getElementByOffset(element.getStartOffset(), true);
            if(tm!=null){
                TextTarget tt=tm.target;
                if(tt!=null){
                    e=tt.getJDomElement();
                    e.addContent(new org.jdom.Element(TEXT).setText(getElementText(element)));
                    return e;
                }
            }
        }
        
        if(elementName.equals("content")) elementName=TEXT;
        else if(elementName.equals("paragraph")) elementName=P;
        
        e=new org.jdom.Element(elementName);
        
        if(style==null || !atr.isEqual(getStyle(style))){
            setJDomElementAttributes(e, atr, elementName.equals(P));
        } else{
            if(!StyleContext.DEFAULT_STYLE.equals(style) && !TARGET.equals(style))
                e.setAttribute(STYLE, style);
        }
        
        int count=element.getElementCount();
        if(count==0){
            String s=getElementText(element);
            if(s!=null && s.length()>0)
                e.setText(getElementText(element));
            else{
                if(elementName.equals(TEXT)) return null;
            }            
        } else{
            for(int i=0; i<count; i++){
                org.jdom.Element child=getJDomElement(element.getElement(i));
                if(child!=null)
                    e.addContent(child);
            }
        }
        return e;
    }
    
    protected static String getElementText(Element element) throws Exception{
        String s=new String();
        int offset=element.getStartOffset();
        int length=element.getEndOffset()-offset;
        if(length>0)
            s=element.getDocument().getText(offset, length);
        return edu.xtec.util.StrUtils.replace(s, "\n", "");
    }
        
    protected static void setJDomElementAttributes(org.jdom.Element e, AttributeSet atr, boolean isParagraph){
        java.util.Enumeration atrNames=atr.getAttributeNames();
        if(atrNames!=null){
            while(atrNames.hasMoreElements()){
                Object attribute=atrNames.nextElement();
                if(attribute!=null && atr.isDefined(attribute) && (isParagraph || !(attribute instanceof AttributeSet.ParagraphAttribute))){
                    JDomUtility.addGenericAttribute(e, attribute.toString(), atr.getAttribute(attribute));
                }
            }
        }
    }
    
    public static TextActivityDocument getTextActivityDocument(org.jdom.Element e, TextActivityBase tab) throws Exception{
        TextActivityDocument doc=new TextActivityDocument(tab.styleContext);
        org.jdom.Element child, child2;
        
        JDomUtility.checkName(e, ELEMENT_NAME);
        
        java.util.List styleList=e.getChildren(STYLE);
        if(!styleList.isEmpty()){
            Style s;
            String styleName, styleParent;
            for(int i=0; i<styleList.size(); i++){
                child=(org.jdom.Element)styleList.get(i);
                styleName=child.getAttributeValue(NAME);
                styleParent=JDomUtility.getStringAttr(child, BASE, StyleContext.DEFAULT_STYLE, false);
                boolean isDefault=styleName.equals(StyleContext.DEFAULT_STYLE);
                if(isDefault)
                    s=doc.getStyle(styleName);
                else
                    s=doc.addStyle(styleName, doc.getStyle(styleParent));
                fillAttributes(s, child);
                if(isDefault)
                    doc.setTabSpc(JDomUtility.getIntAttr(child, TABSPC, DEFAULT_TAB));
            }
        }
        
        child=e.getChild(doc.getDefaultRootElement().getName());
        if(child!=null){
            processChilds(child.getChildren(), doc, tab, doc.getStyle(StyleContext.DEFAULT_STYLE));
            doc.tmb.setPositions();
        }
        
        return doc;
    }
    
    private static void processChilds(java.util.List childs, TextActivityDocument doc, TextActivityBase tab, AttributeSet atr) throws Exception{
        org.jdom.Element child;
        AttributeSet atrBase=atr;
        
        for(int i=0; i<childs.size(); i++){
            atr=atrBase;
            org.jdom.Element e=(org.jdom.Element)childs.get(i);
            String elementName=e.getName();
            if(elementName.equals(P)){
                int p=doc.getLength();
                atr=getAttributes(doc, atrBase, e);
                processChilds(e.getChildren(), doc, tab, atr);
                if(i<childs.size()-1)
                    doc.insertString(doc.getLength(), "\n", atr);
                //if(p>=0)
                doc.setParagraphAttributes(p, doc.getLength()-p, atr, true);
            }
            else if(elementName.equals(TEXT)){
                doc.insertString(doc.getLength(), e.getText(), getAttributes(doc, atrBase, e));
            }
            else if(elementName.equals(TARGET)){
                atr=doc.getStyle(TARGET);
                TargetMarker tm=new TargetMarker(doc);
                tm.begOffset=doc.getLength();
                if(!e.getChildren().isEmpty()){
                    tm.target=TextTarget.getTextTarget(e, tab.getProject().mediaBag);
                    processChilds(e.getChildren(TEXT), doc, tab, atr);
                } else{
                    doc.insertString(doc.getLength(), e.getText(), atr);
                }
                tm.endOffset=doc.getLength();
                doc.tmb.add(tm);
            }
            else if(elementName.equals(ActiveBoxContent.ELEMENT_NAME)){
                ActiveBoxContent ab=ActiveBoxContent.getActiveBoxContent(e, tab.getProject().mediaBag);
                
                //tab.boxesContent.addActiveBoxContent(ab);
                doc.boxesContent.addActiveBoxContent(ab);
                
                JPanelActiveBox jpab=new JPanelActiveBox(null, ab.bb, tab.getProject().getBridge().getComponent());
                jpab.setAlignmentY(0.9f);
                jpab.setActiveBoxContent(ab);
                SimpleAttributeSet satr=new SimpleAttributeSet(atr);
                StyleConstants.setComponent(satr, jpab);
                doc.insertString(doc.getLength(), " ", satr);
            }
        }
    }
    
    public TextActivityDocument cloneDoc(TextActivityDocument dest, boolean hideTargets, boolean initTargets, boolean hideTargetStyle) throws Exception{
        tmb.updateOffsets();
        TextActivityDocument d=dest;
        if(d==null)
            d=new TextActivityDocument(styleContext);
        
        AttributeSet s, s2;
        s=getStyle(StyleContext.DEFAULT_STYLE);
        s2=null;
        d.setParagraphAttributes(0, d.getLength(), s, true);
        d.boxesContent=boxesContent;
        d.popupsContent=popupsContent;
        
        int ip=0;
        int lp=getLength();
        char[] currentChars=getText(0, lp).toCharArray();
        while(ip<lp){
            Element ep=getParagraphElement(ip);
            int dOffset=d.getLength();
            
            TargetMarker tm=null;
            TargetMarker tmx=null;
            MutableAttributeSet attr=null;
            String si=null;
            int i=ip;
            int j=i;
            // Bug: getEndOffset() fails!
            //int l=ep.getEndOffset()+1;
            int l=ip;
            while(l<currentChars.length && currentChars[l]!='\n')
                l++;
            l++;
            s=getCharacterElement(i).getAttributes();
            s2=null;
            while(i<l){
                for(j=i+1; j<l; j++){
                    s2=getCharacterElement(j).getAttributes();
                    if(s!=null && !s.isEqual(s2)) break;
                }
                attr=null;
                si=null;
                tm=tmb.getElementByOffset(i, false);
                if(tm!=null){
                    tmx=new TargetMarker(d);
                    tmx.begOffset=d.getLength();
                    tmx.target=tm.target;
                    if(tm.target!=null && initTargets){
                        if(tm.target.isList){
                            attr=new SimpleAttributeSet(s);
                            StyleConstants.setComponent(attr, tm.target.buildCombo(s, getStyle(TARGET_ERROR)));
                            si=" ";
                        } else{
                            if(tm.target.iniText!=null){
                                si=tm.target.iniText;
                            } else{
                                si=tm.target.getFillString();
                                attr=new SimpleAttributeSet(s);
                                attr.addAttribute(FILL, Boolean.TRUE);
                            }
                        }
                    }
                    else if(hideTargets){
                        si=new String();
                    }
                    else if(hideTargetStyle){
                        attr=styleContext.getStyle(StyleContext.DEFAULT_STYLE);
                    }
                }
                if(si==null){
                    try{
                        si=getText(i, j-i);
                    } catch(Exception ex){}
                }
                
                if(si!=null && si.length()>0){
                    d.insertString(d.getLength(), si, attr!=null ? attr : s);
                }
                
                if(tmx!=null && tm!=null){
                    tmx.endOffset=d.getLength();
                    d.tmb.add(tmx);
                }
                i=j;
                s=s2;
            }
            
            int lpd=d.getLength()-dOffset;
            if(lpd>0){
                d.setParagraphAttributes(dOffset, lpd, ep.getAttributes(), true);
            }
            // bug: getEndOffset fails!
            //ip=ep.getEndOffset()+1;
            ip=l;
        }
        
        d.tmb.setPositions();
        return d;
    }
        
    public static Font attributesToFont(AttributeSet s){
        int style=(StyleConstants.isBold(s) ? Font.BOLD : 0) | (StyleConstants.isItalic(s) ? Font.ITALIC : 0);
        return new Font(StyleConstants.getFontFamily(s), style ,StyleConstants.getFontSize(s));
    }
    
    
    public JPanelActiveBox[] getPanelBoxes(){
        ArrayList<JPanelActiveBox> v=new ArrayList<JPanelActiveBox>();
        for(int i=0; i<getLength(); i++){
            Component c=StyleConstants.getComponent(getCharacterElement(i).getAttributes());
            if(c!=null && c instanceof JPanelActiveBox)
                v.add((JPanelActiveBox)c);
        }        
        return v.toArray(new JPanelActiveBox[v.size()]);        
    }
    
    public void attachTo(JTextComponent tc, Activity.Panel parent){
        for(JPanelActiveBox jpab : getPanelBoxes()){
            jpab.notifyMouseEventsTo(tc);
            jpab.setPanelParent(parent);
        }
    }
    
    public static boolean checkBooleanAttribute(AttributeSet atr, Object key){
        Object o=atr.getAttribute(key);
        return(o!=null && o.equals(Boolean.TRUE));
    }
    
    public boolean checkBooleanAttribute(int offset, Object key){
        Element e=getCharacterElement(offset);
        return e!=null && checkBooleanAttribute(e.getAttributes(), key);
    }
    
    public MutableAttributeSet getTargetAttributeSet(){
        return new SimpleAttributeSet(styleContext.getStyle(TARGET));
    }
    
    public MutableAttributeSet getFillAttributeSet(){
        MutableAttributeSet attr=getTargetAttributeSet();
        attr.addAttribute(FILL, Boolean.TRUE);
        return attr;
    }
    
    public void applyStyleToTarget(TargetMarker tm, String style, boolean invertColors, boolean replace){
        if(style==null)
            style=StyleContext.DEFAULT_STYLE;
        MutableAttributeSet attr=new SimpleAttributeSet(styleContext.getStyle(style));
        if(invertColors){
            Color bg=StyleConstants.getBackground(attr);
            Color fore=StyleConstants.getForeground(attr);
            StyleConstants.setBackground(attr, fore);
            StyleConstants.setForeground(attr, bg);
        }
        tm.updateOffsets();
        setCharacterAttributes(tm.begOffset, tm.getLength(), attr, replace);
    }
    
    public void clearAllTargets(){
        tmb.removeUnattachedElements();
        Iterator it=tmb.iterator();
        while(it.hasNext()){
            TargetMarker tm=(TargetMarker)it.next();
            applyStyleToTarget(tm, null, false, true);
            it.remove();
        }
    }

    public org.jdom.Element getJDomElementWithoutStyles() throws Exception{
        org.jdom.Element mainElement=getJDomElement();
        clearElementStyles(mainElement);
        return mainElement;
    }

    protected void clearElementStyles(org.jdom.Element e) {
        String s=e.getName();
        if(P.equals(s) || TEXT.equals(s)){
            java.util.HashSet<org.jdom.Attribute> set=new java.util.HashSet<org.jdom.Attribute>();
            Iterator it=e.getAttributes().iterator();
            while(it.hasNext()){
                org.jdom.Attribute at=(org.jdom.Attribute)it.next();
                if(!TEXT.equals(at.getName()))
                    set.add(at);
            }
            for(org.jdom.Attribute atr : set)
                e.removeAttribute(atr);
        }
        Iterator it=e.getChildren().iterator();
        while(it.hasNext())
            clearElementStyles((org.jdom.Element)it.next());
    }

    
    /** Getter for property targetType.
     * @return Value of property targetType.
     */
    public int getTargetType() {
        return targetType;
    }
    
    /** Setter for property targetType.
     * @param targetType New value of property targetType.
     */
    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }
    
}
