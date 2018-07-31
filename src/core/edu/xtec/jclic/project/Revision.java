/*
 * File    : Revision.java
 * Created : 13-jul-2001 11:33
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class Revision extends Object implements Domable {

  public Date date;
  public Author[] authors;
  public String description;
  public String comments;

  /** Creates new Revision */
  public Revision() {
    date = new Date();
    description = new String();
    comments = null;
  }

  public Revision(Date setDate, String setDescription) {
    date = setDate;
    description = setDescription;
    // by=null;
    authors = null;
    comments = null;
  }

  public static final String ELEMENT_NAME = "revision";
  public static final String DATE = "date", /*BY="by",*/
      DESCRIPTION = "description",
      COMMENTS = "comments";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(DESCRIPTION, description);
    e.setAttribute(DATE, JDomUtility.dateToStringShortUS(date));
    if (comments != null) JDomUtility.addParagraphs(e, COMMENTS, comments);
    if (authors != null) for (Author author : authors) e.addContent(author.getJDomElement());
    return e;
  }

  public static Revision getRevision(org.jdom.Element e) throws Exception {
    Revision r = new Revision();
    r.setProperties(e, null);
    return r;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);

    description = JDomUtility.getStringAttr(e, DESCRIPTION, description, true);
    date = JDomUtility.getDateAttrShortUS(e, DATE, date);
    comments = JDomUtility.getParagraphs(e.getChild(COMMENTS));

    ArrayList<Author> al = new ArrayList<Author>();
    Iterator itr = e.getChildren(Author.ELEMENT_NAME).iterator();
    while (itr.hasNext()) al.add(Author.getAuthor((org.jdom.Element) itr.next()));
    if (!al.isEmpty()) authors = al.toArray(new Author[al.size()]);
  }

  public String toHtmlString(edu.xtec.util.Messages msg) {
    Html html = new Html(300);
    html.bold(msg.getShortDateStr(date));
    if (description != null) html.append("<B>: ").appendParagraphs(description).append("</B>");
    if (authors != null) for (Author author : authors) html.br().append(author.toHtmlString(msg));
    if (comments != null && comments.length() > 0) html.br().appendParagraphs(comments);
    return html.toString();
  }

  @Override
  public String toString() {
    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
    StringBuilder sb = new StringBuilder();
    sb.append(df.format(date)).append(" - ");
    if (description != null && description.length() > 0) sb.append(description);
    return sb.substring(0);
  }
}
