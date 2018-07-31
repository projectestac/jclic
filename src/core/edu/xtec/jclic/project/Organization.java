/*
 * File    : Organization.java
 * Created : 13-jul-2001 11:21
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
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class Organization extends Object implements Domable {

  public String name;
  public String mail;
  public String url;
  public String address;
  public String pc;
  public String city;
  public String country;
  public String state;
  public String comments;

  /** Creates new Organization */
  public Organization() {
    name = new String();
    mail = null;
    url = null;
    address = null;
    pc = null;
    city = null;
    country = null;
    state = null;
    comments = null;
  }

  public static final String ELEMENT_NAME = "organization";
  public static final String NAME = "name",
      MAIL = "mail",
      URL = "url",
      ADDRESS = "address",
      PC = "pc",
      CITY = "city",
      COUNTRY = "country",
      STATE = "state",
      COMMENTS = "comments";

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    e.setAttribute(NAME, name);
    if (mail != null) e.setAttribute(MAIL, mail);
    if (url != null) e.setAttribute(URL, url);
    if (address != null) e.setAttribute(ADDRESS, address);
    if (pc != null) e.setAttribute(PC, pc);
    if (city != null) e.setAttribute(CITY, city);
    if (country != null) e.setAttribute(COUNTRY, country);
    if (state != null) e.setAttribute(STATE, state);
    if (comments != null) JDomUtility.addParagraphs(e, COMMENTS, comments);
    return e;
  }

  public static Organization getOrganization(org.jdom.Element e) throws Exception {
    Organization o = new Organization();
    o.setProperties(e, null);
    return o;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    JDomUtility.checkName(e, ELEMENT_NAME);
    name = JDomUtility.getStringAttr(e, NAME, name, true);
    mail = JDomUtility.getStringAttr(e, MAIL, mail, false);
    url = JDomUtility.getStringAttr(e, URL, url, false);
    address = JDomUtility.getStringAttr(e, ADDRESS, address, false);
    pc = JDomUtility.getStringAttr(e, PC, pc, false);
    city = JDomUtility.getStringAttr(e, CITY, city, false);
    state = JDomUtility.getStringAttr(e, STATE, state, false);
    country = JDomUtility.getStringAttr(e, COUNTRY, country, false);
    comments = JDomUtility.getParagraphs(e.getChild(COMMENTS));
  }

  public String toHtmlString(edu.xtec.util.Messages msg) {
    Html html = new Html(300);
    html.append(name);
    if (mail != null && mail.length() > 0) html.sp().mailTo(mail, true);
    if (url != null && url.length() > 0) html.br().linkTo(url, null);
    if (address != null) html.br().appendParagraphs(address);
    if (pc != null || city != null) {
      html.br();
      if (pc != null) html.append(pc).nbsp();
      if (city != null) html.appendParagraphs(city);
    }
    if (state != null) html.br().appendParagraphs(state);
    if (country != null) html.br().appendParagraphs(country);
    if (comments != null) html.br().appendParagraphs(comments);
    return html.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    if (city != null && city.length() > 0) sb.append(" (").append(city).append(")");
    return sb.toString();
  }

  public static Organization fromString(String str) {
    Organization result = new Organization();
    str = str.trim();
    int p = str.indexOf(" (");
    if (p > 0) {
      result.name = str.substring(0, p);
      result.city = str.substring(p + 2, str.length() - 1);
    } else result.name = str;
    return result;
  }
}
