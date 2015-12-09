/*
 * File    : ProjectSettings.java
 * Created : 09-may-2002 15:57
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

import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.util.Domable;
import edu.xtec.util.Html;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.StrUtils;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ProjectSettings implements Editable, Domable {

  public String title;
  public String iconFileName;
  public String description;
  public String descriptors;
  public String area;
  public String level;
  public Locale locale;
  public String[] languages;
  public Author[] authors;
  public Organization[] organizations;
  public Revision[] revisions;
  public String skinFileName;
  public EventSounds eventSounds;

  public static String UNTITLED = "untitled";
  public static String ELEMENT_NAME = "settings";
  public static String TITLE = "title", LOCALE = "locale", LANGUAGE = "language", DESCRIPTION = "description", DESCRIPTORS = "descriptors",
          SKIN = "skin", FILE = "file", AREA = "area", LEVEL = "level", ICON = "icon";

  /**
   * Creates new ProjectSettings
   */
  public ProjectSettings() {
    title = UNTITLED;
    description = null;
    area = null;
    level = null;
    descriptors = null;
    locale = null;
    languages = null;
    authors = null;
    organizations = null;
    revisions = new Revision[]{new Revision(new Date(), "created")};
    eventSounds = new EventSounds(null);
    skinFileName = null;
    iconFileName = null;
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    org.jdom.Element child;

    e.addContent(new org.jdom.Element(TITLE).setText(title));

    if (revisions != null) {
      for (Revision revision : revisions) {
        e.addContent(revision.getJDomElement());
      }
    }

    if (authors != null) {
      for (Author author : authors) {
        e.addContent(author.getJDomElement());
      }
    }

    if (organizations != null) {
      for (Organization organization : organizations) {
        e.addContent(organization.getJDomElement());
      }
    }

    if (languages != null) {
      for (String language : languages) {
        e.addContent(new org.jdom.Element(LANGUAGE).setText(language));
      }
    }

    if (locale != null) {
      StringBuilder sb = new StringBuilder(locale.getLanguage());
      if (locale.getCountry() != null && locale.getCountry().length() > 0) {
        sb.append('-').append(locale.getCountry());
        if (locale.getVariant() != null && locale.getVariant().length() > 0) {
          sb.append('-').append(locale.getVariant());
        }
      }
      e.setAttribute(LOCALE, sb.substring(0));
    }

    if (description != null) {
      JDomUtility.addParagraphs(e, DESCRIPTION, description);
    }

    child = new org.jdom.Element(DESCRIPTORS);
    if (area != null) {
      child.setAttribute(AREA, area);
    }

    if (level != null) {
      child.setAttribute(LEVEL, level);
    }

    if (descriptors != null) {
      child.setText(descriptors);
    }

    e.addContent(child);

    if ((child = eventSounds.getJDomElement()) != null) {
      e.addContent(child);
    }

    if (skinFileName != null) {
      child = new org.jdom.Element(SKIN);
      child.setAttribute(FILE, skinFileName);
      //if(skin!=null && skin.name!=null && skin.name.length()>0)
      //    child2.setAttribute(NAME, skin.name);
      e.addContent(child);
    }

    if (iconFileName != null) {
      child = new org.jdom.Element(ICON);
      child.setAttribute(FILE, iconFileName);
      //if(skin!=null && skin.name!=null && skin.name.length()>0)
      //    child2.setAttribute(NAME, skin.name);
      e.addContent(child);
    }

    return e;
  }

  public static ProjectSettings getProjectSettings(org.jdom.Element e) throws Exception {
    ProjectSettings st = new ProjectSettings();
    st.setProperties(e, null);
    return st;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {

    JDomUtility.checkName(e, ELEMENT_NAME);
    org.jdom.Element child;
    Iterator itr;
    String s;

    if ((child = e.getChild(TITLE)) != null) {
      title = child.getText();
    }

    ArrayList<Revision> alr = new ArrayList<Revision>();
    itr = e.getChildren(Revision.ELEMENT_NAME).iterator();
    while (itr.hasNext()) {
      alr.add(Revision.getRevision((org.jdom.Element) itr.next()));
    }
    if (!alr.isEmpty()) {
      revisions = alr.toArray(new Revision[alr.size()]);
    }

    ArrayList<Author> alau = new ArrayList<Author>();
    itr = e.getChildren(Author.ELEMENT_NAME).iterator();
    while (itr.hasNext()) {
      alau.add(Author.getAuthor((org.jdom.Element) itr.next()));
    }
    if (!alau.isEmpty()) {
      authors = alau.toArray(new Author[alau.size()]);
    }

    ArrayList<Organization> alo = new ArrayList<Organization>();
    itr = e.getChildren(Organization.ELEMENT_NAME).iterator();
    while (itr.hasNext()) {
      alo.add(Organization.getOrganization((org.jdom.Element) itr.next()));
    }
    if (!alo.isEmpty()) {
      organizations = alo.toArray(new Organization[alo.size()]);
    }

    ArrayList<String> all = new ArrayList<String>();
    itr = e.getChildren(LANGUAGE).iterator();
    while (itr.hasNext()) {
      all.add(((org.jdom.Element) itr.next()).getText());
    }
    if (!all.isEmpty()) {
      languages = all.toArray(new String[all.size()]);
    }

    if ((s = JDomUtility.getStringAttr(e, LOCALE, null, false)) != null) {
      StringTokenizer stk = new StringTokenizer(s, "-");
      String l = null, c = null, v = null;
      if (stk.hasMoreTokens()) {
        l = stk.nextToken();
      }
      if (stk.hasMoreTokens()) {
        c = stk.nextToken();
      }
      if (stk.hasMoreTokens()) {
        v = stk.nextToken();
      }
      if (l != null && c != null) {
        if (v != null) {
          locale = new Locale(l, c, v);
        } else {
          locale = new Locale(l, c);
        }
      }
    }

    description = JDomUtility.getParagraphs(e.getChild(DESCRIPTION));

    if ((child = e.getChild(DESCRIPTORS)) != null) {
      // check for old format
      if (!child.getChildren(JDomUtility.P).isEmpty()) {
        descriptors = JDomUtility.getParagraphs(e.getChild(DESCRIPTORS));
        descriptors = edu.xtec.util.StrUtils.replace(descriptors, "\n", ", ");
      } else {
        descriptors = StrUtils.nullableString(child.getTextNormalize());
      }
      area = JDomUtility.getStringAttr(child, AREA, area, false);
      level = JDomUtility.getStringAttr(child, LEVEL, level, false);
    }

    if ((child = e.getChild(EventSounds.ELEMENT_NAME)) != null) {
      eventSounds = EventSounds.getEventSounds(child);
    }

    if ((child = e.getChild(SKIN)) != null) {
      skinFileName = JDomUtility.getStringAttr(child, FILE, skinFileName, false);
      //if(jcp.skinFileName!=null && jcp.skinFileName.length()>0)
      //    jcp.skin=jcp.mediaBag.getSkinElement(jcp.skinFileName);
      //else
      //    jcp.skinFileName=null;
    }

    if ((child = e.getChild(ICON)) != null) {
      iconFileName = JDomUtility.getStringAttr(child, FILE, iconFileName, false);
      //if(jcp.skinFileName!=null && jcp.skinFileName.length()>0)
      //    jcp.skin=jcp.mediaBag.getSkinElement(jcp.skinFileName);
      //else
      //    jcp.skinFileName=null;
    }
  }

  public String toHtmlString(edu.xtec.util.Messages msg) {
    String msgBase = "about_window_lb_";
    Html html = new Html(1000);

    //html.doubleCell(msg.get(msgBase+"project"), true, name, true);
    html.doubleCell(msg.get(msgBase + "project"), true, title, true);

    if (area != null) {
      html.doubleCell(msg.get(msgBase + "area"), true, area, false);
    }

    if (level != null) {
      html.doubleCell(msg.get(msgBase + "level"), true, level, false);
    }

    if (authors != null) {
      int k = authors.length;
      html.tr(true).td(msg.get(msgBase + "author" + (k > 1 ? "s" : "")), true).td(true);
      for (int i = 0; i < k; i++) {
        if (authors[i] != null) {
          html.append(authors[i].toHtmlString(msg)).br();
        }
      }
      html.td(false).tr(false);
    }

    if (organizations != null) {
      int k = organizations.length;
      html.tr(true).td(msg.get(msgBase + "organization" + (k > 1 ? "s" : "")), true).td(true);
      for (int i = 0; i < organizations.length; i++) {
        if (i > 0) {
          html.nbsp().br();
        }
        if (organizations[i] != null) {
          html.append(organizations[i].toHtmlString(msg)).br();
        }
      }
      html.td(false).tr(false);
    }

    if (revisions != null) {
      html.tr(true).td(msg.get(msgBase + "history"), true).td(true);
      for (int i = 0; i < revisions.length; i++) {
        if (i > 0) {
          html.nbsp().br();
        }
        if (revisions[i] != null) {
          html.append(revisions[i].toHtmlString(msg)).br();
        }
      }
      html.td(false).tr(false);
    }

    if (languages != null) {
      int k = languages.length;
      html.tr(true).td(msg.get(msgBase + "language" + (k > 1 ? "s" : "")), true).td(true);
      for (int i = 0; i < k; i++) {
        if (languages[i] != null) {
          html.append(languages[i]).sp();
        }
      }
      html.td(false).tr(false);
    }

    if (description != null) {
      html.doubleCell(msg.get(msgBase + "description"), true, description, false);
    }

    if (descriptors != null) {
      html.doubleCell(msg.get(msgBase + "descriptors"), true, descriptors, false);
    }

    return Html.table(html.toString(), null, 1, 5, -1, null, false);
  }

  public JSONObject toJSON(edu.xtec.util.Messages msg) throws JSONException {
    JSONObject json = new JSONObject();

    json.put("title", title);
    
    if (authors!=null && authors.length > 0) {
      StringBuilder sb = new StringBuilder();
      for (Author a : authors) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(a.toString());
      }
      json.put("author", sb.toString());
    }

    if (organizations != null && organizations.length > 0) {
      StringBuilder sb = new StringBuilder();
      for (Organization o : organizations) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(o.toString());
      }
      json.put("school", sb.toString());
    }

    if(revisions!=null && revisions.length > 0)
      json.put("date", msg.getShortDateStr(revisions[0].date));

    Locale locale = msg.getLocale();
    String langCode = msg.getLocale().getLanguage();
    boolean langCodeSet = false;
    if (languages != null && languages.length > 0) {
      String langNames = "";
      for (String lang : languages) {

        String code = null;
        int p = lang.lastIndexOf('(');
        int q = lang.lastIndexOf(')');
        if (p > 0 && q > p) {
          code = lang.substring(p + 1, q);
        } else {
          code = (String) edu.xtec.util.Messages.getNamesToCodes().get(lang.toLowerCase());
        }

        if (code != null) {
          if (!langCodeSet) {
            langCode = code;
            locale = new Locale(code);
            langCodeSet = true;
          }
          json.append("langCodes", code);

          if (langNames.length() > 0) {
            langNames += ", ";
          }
          langNames += (new Locale(code)).getDisplayName(locale);
        }
      }
      if (langNames.length() > 0) {
        json.put("languages", (new JSONObject()).put(langCode, langNames));
      }
    }

    if (area != null) {
      json.put("areas", (new JSONObject()).put(langCode, area));
    }

    if (level != null) {
      json.put("levels", (new JSONObject()).put(langCode, level));
    }

    if (description != null) {
      json.put("description", (new JSONObject()).put(langCode, description));
    }

    json.append("meta_langs", langCode);

    return json;
  }

  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

}
