/*
 * File    : Author.java
 * Created : 13-jul-2001 11:07
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

package edu.xtec.jclic.project;

import edu.xtec.util.Domable;
import edu.xtec.util.Html;
import edu.xtec.util.JDomUtility;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class Author extends Object implements Domable{
    
    public String name;
    public String mail;
    public String url;
    public String organization;
    public String comments;
    public String rol;
    
    /** Creates new Author */
    public Author() {
        name=new String();
        mail=null;
        url=null;
        organization=null;
        comments=null;
        rol=null;
    }
    
    public static final String ELEMENT_NAME="author";
    public static final String NAME="name", MAIL="mail", URL="url",
    ORGANIZATION="organization", COMMENTS="comments", ROL="rol";
    
    public org.jdom.Element getJDomElement(){
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        e.setAttribute(NAME, name);
        if(mail!=null) e.setAttribute(MAIL, mail);
        if(url!=null) e.setAttribute(URL, url);
        if(rol!=null) e.setAttribute(ROL, rol);
        if(organization!=null) e.setAttribute(ORGANIZATION, organization);
        if(comments!=null) JDomUtility.addParagraphs(e, COMMENTS, comments);
        return e;
    }
    
    public static Author getAuthor(org.jdom.Element e) throws Exception{
        
        JDomUtility.checkName(e, ELEMENT_NAME);
        
        Author a=new Author();
        a.name=JDomUtility.getStringAttr(e, NAME, a.name, true);
        a.mail=JDomUtility.getStringAttr(e, MAIL, a.mail, false);
        a.url=JDomUtility.getStringAttr(e, URL, a.url, false);
        a.rol=JDomUtility.getStringAttr(e, ROL, a.rol, false);
        a.organization=JDomUtility.getStringAttr(e, ORGANIZATION, a.organization, false);
        a.comments=JDomUtility.getParagraphs(e.getChild(COMMENTS));
        return a;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        
        JDomUtility.checkName(e, ELEMENT_NAME);
        name=JDomUtility.getStringAttr(e, NAME, name, true);
        mail=JDomUtility.getStringAttr(e, MAIL, mail, false);
        url=JDomUtility.getStringAttr(e, URL, url, false);
        rol=JDomUtility.getStringAttr(e, ROL, rol, false);
        organization=JDomUtility.getStringAttr(e, ORGANIZATION, organization, false);
        comments=JDomUtility.getParagraphs(e.getChild(COMMENTS));
    }
    
    public String toHtmlString(edu.xtec.util.Messages msg){
        Html html=new Html(500);
        if(rol!=null)
            html.append(rol).append(": ");
        html.append(name);
        if(mail!=null && mail.length()>0)
            html.sp().mailTo(mail, true);
        if(url!=null && url.length()>0)
            html.br().linkTo(url, null);
        if(organization!=null && organization.length()>0)
            html.br().appendParagraphs(organization);
        if(comments!=null && comments.length()>0)
            html.br().appendParagraphs(comments);
        return html.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder(name);
        if(rol!=null && rol.length()>0)
            sb.append(" (").append(rol).append(")");
        return sb.substring(0);
    }
    
}
