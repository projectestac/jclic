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

import edu.xtec.jclic.bags.MediaBagElement;
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.util.Domable;
import edu.xtec.util.Html;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.Messages;
import edu.xtec.util.StrUtils;
import java.util.*;
import org.json.JSONArray;
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
  // --- New fields added 25/07/2017
  public String coverFileName;
  public String thumbnailFileName;
  public String icon16, icon72, icon192;
  public Locale[] meta_langs;
  public String[] descriptions;
  public List<String> area_codes;
  public List<String> level_codes;
  public String[] lang_codes;
  public int license;

  public static String UNTITLED = "untitled";
  public static String ELEMENT_NAME = "settings";
  public static String TITLE = "title", LOCALE = "locale", LANGUAGE = "language", DESCRIPTION = "description",
          DESCRIPTORS = "descriptors", SKIN = "skin", FILE = "file", AREA = "area", AREA_CODES = "area-codes", LEVEL = "level", LEVEL_CODES = "level-codes",
          ICON = "icon", COVER = "cover", THUMB = "thumb", ICON16="icon16", ICON72="icon72", ICON192="icon192", META_LANGS = "meta_langs", DESCRIPTIONS = "descriptions", LICENSE = "license", TYPE = "type", URL = "url";

  public static String[] KNOWN_META_LANGS = {"ca", "es", "en"};

  @SuppressWarnings("unchecked")
  public static List<String>[] KNOWN_LEVEL_DESCS = (List<String>[]) new List[]{
    Arrays.asList(new String[]{"Infantil (3-6)", "Infantil (3-6)", "Kindergarten (3-6)"}),
    Arrays.asList(new String[]{"Primària (6-12)", "Primaria (6-12)", "Primary school (6-12)"}),
    Arrays.asList(new String[]{"Secundària (12-16)", "Secundaria (12-16)", "Secondary school (12-16)"}),
    Arrays.asList(new String[]{"Batxillerat (16-18)", "Bachillerato (16-18)", "High school (16-18)"})
  };
  public static String[] KNOWN_LEVEL_CODES = {"INF", "PRI", "SEC", "BTX"};

  @SuppressWarnings("unchecked")
  public static List<String>[] KNOWN_AREA_DESCS = (List<String>[]) new List[]{
    Arrays.asList(new String[]{"Llengües", "Lenguas", "Languages"}),
    Arrays.asList(new String[]{"Matemàtiques", "Matemáticas", "Mathematics"}),
    Arrays.asList(new String[]{"Ciències socials", "Ciencias sociales", "Social sciences"}),
    Arrays.asList(new String[]{"Ciències experimentals", "Ciencias experimentales", "Experimental sciences"}),
    Arrays.asList(new String[]{"Música", "Música", "Music"}),
    Arrays.asList(new String[]{"Visual i plàstica", "Plástica y visual", "Art & design"}),
    Arrays.asList(new String[]{"Educació física", "Educación física", "Physical education"}),
    Arrays.asList(new String[]{"Tecnologies", "Tecnología", "Design & technology"}),
    Arrays.asList(new String[]{"Diversos", "Diversos", "Miscellaneous"})
  };
  public static String[] KNOWN_AREA_CODES = {"lleng", "mat", "soc", "exp", "mus", "vip", "ef", "tec", "div"};

  public static String[] CC_LICENSES = {"by", "by-sa", "by-nd", "by-nc", "by-nc-sa", "by-nc-nd", "other"};
  public static int CC_BY = 0, CC_BY_SA = 1, CC_BY_ND = 2, CC_BY_NC = 3, CC_BY_NC_SA = 4, CC_BY_NC_ND = 5, OTHER = 6;
  public static String CC_URL = "https://creativecommons.org/licenses/%%CODE%%/4.0", OTHER_URL = "See 'description'";
  public static String[] LICENSE_DESC = {
    "Aquesta obra està sota una llicència de Creative Commons <a href=\"%%URL%%\">%%CODE%%</a>",
    "Esta obra está bajo una licencia de Creative Commons <a href=\"%%URL%%\">%%CODE%%</a>",
    "Licensed under a Creative Commons license <a href=\"%%URL%%\">%%CODE%%</a>"
  };
  public static String[] LICENSE_OTHER = {
    "Els termes de la llicència d'ús s'especifiquen a la descripció del projecte",
    "Los términos de la licencia de uso se especifican en la descripción del proyecto",
    "License terms are specified in project description"
  };

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
    // --- New fields added 25/07/2017
    coverFileName = null;
    icon16 = icon72 = icon192 = null;
    thumbnailFileName = null;
    meta_langs = new Locale[1];
    meta_langs[0] = Locale.getDefault();
    descriptions = new String[1];
    descriptions[0] = "";
    license = CC_BY_NC_SA;
  }

  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = new org.jdom.Element(ELEMENT_NAME);
    org.jdom.Element child, child2;

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
      /*
      StringBuilder sb = new StringBuilder(loc.getLanguage());
      if (loc.getCountry() != null && loc.getCountry().length() > 0) {
        sb.append('-').append(loc.getCountry());
        if (loc.getVariant() != null && loc.getVariant().length() > 0) {
          sb.append('-').append(loc.getVariant());
        }
      }
      e.setAttribute(LOCALE, sb.substring(0));
       */
      e.setAttribute(LOCALE, locale.toLanguageTag());
    }

    if (description != null) {
      JDomUtility.addParagraphs(e, DESCRIPTION, description);
    }

    child = new org.jdom.Element(DESCRIPTORS);
    if (area != null) {
      child.setAttribute(AREA, area);
    }

    if (area_codes != null && area_codes.size() > 0) {
      child.setAttribute(AREA_CODES, StrUtils.getEnumeration(area_codes));
    }

    if (level != null) {
      child.setAttribute(LEVEL, level);
    }

    if (level_codes != null && level_codes.size() > 0) {
      child.setAttribute(LEVEL_CODES, StrUtils.getEnumeration(level_codes));
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
      e.addContent(child);
    }

    if (iconFileName != null) {
      child = new org.jdom.Element(ICON);
      child.setAttribute(FILE, iconFileName);
      e.addContent(child);
    }

    if (coverFileName != null) {
      child = new org.jdom.Element(COVER);
      child.setAttribute(FILE, coverFileName);
      e.addContent(child);
    }

    if (thumbnailFileName != null) {
      child = new org.jdom.Element(THUMB);
      child.setAttribute(FILE, thumbnailFileName);
      e.addContent(child);
    }

    if (icon16 != null) {
      child = new org.jdom.Element(ICON16);
      child.setAttribute(FILE, icon16);
      e.addContent(child);
    }

    if (icon72 != null) {
      child = new org.jdom.Element(ICON72);
      child.setAttribute(FILE, icon72);
      e.addContent(child);
    }

    if (icon192 != null) {
      child = new org.jdom.Element(ICON192);
      child.setAttribute(FILE, icon192);
      e.addContent(child);
    }

    child = new org.jdom.Element(META_LANGS);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < meta_langs.length; i++) {
      sb.append(i > 0 ? "," : "").append(meta_langs[i].toLanguageTag());
    }
    child.setText(sb.substring(0));
    e.addContent(child);

    child = new org.jdom.Element(DESCRIPTIONS);
    for (int i = 0; i < meta_langs.length; i++) {
      child2 = JDomUtility.addParagraphs(child, DESCRIPTION, descriptions[i] == null ? "" : descriptions[i]);
      child2.setAttribute(LANGUAGE, meta_langs[i].toLanguageTag());
    }
    e.addContent(child);

    child = new org.jdom.Element(LICENSE);
    child.setAttribute(TYPE, CC_LICENSES[license]);
    child.setAttribute(URL, license < OTHER ? CC_URL.replace("%%CODE%%", CC_LICENSES[license]) : OTHER_URL);
    e.addContent(child);

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
    StringBuffer sb;
    StringTokenizer stk;
    ArrayList<String> al;

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
      for (int i = 0; i < languages.length; i++) {
        String lng = Messages.getLanguageFromDescriptive(languages[i]);
        Locale loc = Locale.forLanguageTag(lng == null ? languages[i] : lng);
        if (loc.getLanguage().equals("")) {
          lng = Messages.getKnownLanguageCode(languages[i]);
          loc = Locale.forLanguageTag(lng == null ? languages[i] : lng);
        }
        languages[i] = loc.toLanguageTag();
      }
    }

    if (languages != null && languages.length > 0 && languages[0].length() > 1 && !languages[0].equals("und")) {
      meta_langs[0] = Locale.forLanguageTag(languages[0]);
    }

    if ((s = JDomUtility.getStringAttr(e, LOCALE, null, false)) != null) {
      stk = new StringTokenizer(s, "-");
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
    if (description != null) {
      descriptions[0] = description;
    }

    if ((child = e.getChild(DESCRIPTORS)) != null) {
      // check for old format
      if (!child.getChildren(JDomUtility.P).isEmpty()) {
        descriptors = JDomUtility.getParagraphs(e.getChild(DESCRIPTORS));
        descriptors = edu.xtec.util.StrUtils.replace(descriptors, "\n", ", ");
      } else {
        descriptors = StrUtils.nullableString(child.getTextNormalize());
      }
      area = JDomUtility.getStringAttr(child, AREA, area, false);
      String ac = JDomUtility.getStringAttr(child, AREA_CODES, null, false);
      if (ac != null) {
        area_codes = StrUtils.enumerationToList(ac, ",");
      } else if (area != null) {
        sb = new StringBuffer();
        al = new ArrayList<String>();
        stk = new StringTokenizer(area, ",");
        while (stk.hasMoreElements()) {
          s = stk.nextToken().trim();
          int i = 0;
          for (; i < KNOWN_AREA_DESCS.length; i++) {
            if (KNOWN_AREA_DESCS[i].contains(s)) {
              al.add(KNOWN_AREA_CODES[i]);
              break;
            }
          }
          if (i == KNOWN_AREA_DESCS.length) {
            sb.append(sb.length() > 0 ? ", " : "").append(s);
          }
        }
        area = sb.length() > 0 ? sb.toString() : null;
        if (al.size() > 0) {
          area_codes = al;
        }
      }

      level = JDomUtility.getStringAttr(child, LEVEL, level, false);
      String lc = JDomUtility.getStringAttr(child, LEVEL_CODES, null, false);
      if (lc != null) {
        level_codes = StrUtils.enumerationToList(lc, ",");
      } else if (level != null) {
        sb = new StringBuffer();
        al = new ArrayList<String>();
        stk = new StringTokenizer(level, ",");
        while (stk.hasMoreElements()) {
          s = stk.nextToken().trim();
          int i = 0;
          for (; i < KNOWN_LEVEL_DESCS.length; i++) {
            if (KNOWN_LEVEL_DESCS[i].contains(s)) {
              al.add(KNOWN_LEVEL_CODES[i]);
              break;
            }
          }
          if (i == KNOWN_LEVEL_DESCS.length) {
            sb.append(sb.length() > 0 ? ", " : "").append(s);
          }
        }
        level = sb.length() > 0 ? sb.toString() : null;
        if (al.size() > 0) {
          level_codes = al;
        }
      }
    }

    if ((child = e.getChild(EventSounds.ELEMENT_NAME)) != null) {
      eventSounds = EventSounds.getEventSounds(child);
    }

    if ((child = e.getChild(SKIN)) != null) {
      skinFileName = JDomUtility.getStringAttr(child, FILE, skinFileName, false);
    }

    if ((child = e.getChild(ICON)) != null) {
      iconFileName = JDomUtility.getStringAttr(child, FILE, iconFileName, false);
    }

    if ((child = e.getChild(COVER)) != null) {
      coverFileName = JDomUtility.getStringAttr(child, FILE, coverFileName, false);
    }

    if ((child = e.getChild(THUMB)) != null) {
      thumbnailFileName = JDomUtility.getStringAttr(child, FILE, thumbnailFileName, false);
    }

    if ((child = e.getChild(ICON16)) != null) {
      icon16 = JDomUtility.getStringAttr(child, FILE, icon16, false);
    }

    if ((child = e.getChild(ICON72)) != null) {
      icon72 = JDomUtility.getStringAttr(child, FILE, icon72, false);
    }

    if ((child = e.getChild(ICON192)) != null) {
      icon192 = JDomUtility.getStringAttr(child, FILE, icon192, false);
    }
    
    if ((child = e.getChild(META_LANGS)) != null) {
      String[] ml = child.getTextNormalize().split(",");
      meta_langs = new Locale[ml.length];
      descriptions = new String[ml.length];
      for (int i = 0; i < ml.length; i++) {
        meta_langs[i] = Locale.forLanguageTag(ml[i]);
        descriptions[i] = (i == 0 && description != null) ? description : "";
      }
    }

    if ((child = e.getChild(DESCRIPTIONS)) != null) {
      @SuppressWarnings("unchecked")
      List<org.jdom.Element> descs = (List<org.jdom.Element>) child.getChildren(DESCRIPTION);
      descriptions = new String[Math.max(meta_langs.length, descs.size())];
      for (int i = 0; i < descs.size(); i++) {
        descriptions[i] = JDomUtility.getParagraphs(descs.get(i));
      }
    }

    if ((child = e.getChild(LICENSE)) != null) {
      license = JDomUtility.getStrIndexAttr(child, TYPE, CC_LICENSES, CC_BY_NC_SA);
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

  public void readJSON(JSONObject json, JClicProject parent, boolean preserve) throws Exception {

    // Read title
    if (json.has("title") && (!preserve || title.length() == 0)) {
      title = json.getString("title");
    }

    // Read authors
    if (json.has("author") && (!preserve || authors == null || authors.length == 0)) {
      String[] authStr = json.getString("author").split(", ");
      authors = new Author[authStr.length];
      for (int i = 0; i < authStr.length; i++) {
        authors[i] = Author.fromString(authStr[i]);
      }
    }

    // Read organizations
    if (json.has("school") && (!preserve || organizations == null || organizations.length == 0)) {
      String[] orgStr = json.getString("school").split(", ");
      organizations = new Organization[orgStr.length];
      for (int i = 0; i < orgStr.length; i++) {
        organizations[i] = Organization.fromString(orgStr[i]);
      }
    }

    // Load meta-languages
    if (json.has("meta_langs") && (!preserve || (meta_langs.length == 1 && meta_langs[0] == Locale.getDefault()))) {
      JSONArray langTags = json.getJSONArray("meta_langs");
      meta_langs = new Locale[langTags.length()];
      for (int i = 0; i < meta_langs.length; i++) {
        meta_langs[i] = new Locale(langTags.getString(i));
      }
    }

    // TODO: must we read levels, areas and languages from descriptive, localized tags? They are
    // already present in ProjectSettings
    // Load descriptions
    if (json.has("description") && (!preserve || (descriptions.length == 1 || descriptions[0].length() == 0))) {
      JSONObject descs = json.getJSONObject("description");
      descriptions = new String[meta_langs.length];
      for (int i = 0; i < meta_langs.length; i++) {
        descriptions[i] = descs.optString(meta_langs[i].toLanguageTag());
      }
      description = descriptions[0];
    }

    // Load cover and thumbnail
    if (json.has("cover") && (!preserve || coverFileName == null)) {
      coverFileName = json.getString("cover");
      parent.mediaBag.addElement(new MediaBagElement(coverFileName));
    }

    if (json.has("thumbnail") && (!preserve || thumbnailFileName == null)) {
      thumbnailFileName = json.getString("thumbnail");
      parent.mediaBag.addElement(new MediaBagElement(thumbnailFileName));
    }
    
    // Load icons if not defined
    if(icon16 == null) {
      icon16 = "favicon.ico";
      parent.mediaBag.addElement(new MediaBagElement(icon16));      
    }

    if(icon72 == null) {
      icon72 = "icon-72.png";
      parent.mediaBag.addElement(new MediaBagElement(icon72));
    }

    if(icon192 == null) {
      icon192 = "icon-192.png";
      parent.mediaBag.addElement(new MediaBagElement(icon192));
    }    
  }

  public JSONObject toJSON(edu.xtec.util.Messages msg) throws JSONException {

    // Initialize `langTags` with current codes in meta_langs
    int numLangs = meta_langs.length;
    String[] langTags = new String[numLangs];
    for (int i = 0; i < numLangs; i++) {
      langTags[i] = meta_langs[i].toLanguageTag();
    }

    // Prepare an empty JSON object to be filled in with current project settings
    JSONObject json = new JSONObject();

    // Fill in title
    json.put("title", title);

    // Fill in authors
    if (authors != null && authors.length > 0) {
      StringBuilder sb = new StringBuilder();
      for (Author a : authors) {
        StrUtils.addToEnum(sb, a.toString());
      }
      json.put("author", sb.toString());
    }

    // Fill in organizations
    if (organizations != null && organizations.length > 0) {
      StringBuilder sb = new StringBuilder();
      for (Organization o : organizations) {
        StrUtils.addToEnum(sb, o.toString());
      }
      json.put("school", sb.toString());
    }

    // Fill in last revision date
    if (revisions != null && revisions.length > 0) {
      json.put("date", msg.getShortDateStr(revisions[0].date));
    }

    // Fill in project languages (codes and names)
    if (languages != null && languages.length > 0) {
      String[] langDescs = new String[numLangs];

      for (String lang : languages) {

        String code = null;

        // Check for full language and code format, like in "Catalan (ca)"
        int p = lang.lastIndexOf('(');
        int q = lang.lastIndexOf(')');
        if (p > 0 && q > p) {
          code = lang.substring(p + 1, q);
        } else {
          // Check for descriptive language code, like in "Catalan"
          code = (String) edu.xtec.util.Messages.getNamesToCodes().get(lang.toLowerCase());
          if (code == null) // Fallblack: "lang" should be a language code
          {
            code = lang;
          }
        }

        // Append code
        json.append("langCodes", code);

        // Add language description for each meta_lang
        Locale loc = new Locale(code);
        for (int i = 0; i < numLangs; i++) {
          langDescs[i] = StrUtils.addToEnum(langDescs[i] == null ? "" : langDescs[i], loc.getDisplayName(meta_langs[i]));
        }
      }

      // Build a special object for language descriptions
      JSONObject jso = new JSONObject();
      for (int i = 0; i < numLangs; i++) {
        jso.put(langTags[i], langDescs[i]);
      }
      json.put("languages", jso);
    }

    // Fill in project subjects, both as codes and descriptive tags
    if (area_codes != null && area_codes.size() > 0) {

      // Fill in area codes
      json.put("areaCodes", new JSONArray(area_codes));

      // Fill in descriptive names
      JSONObject jso = new JSONObject();
      for (String lang : langTags) {
        String areaDescs = "";
        int p = StrUtils.getIndexOf(lang, KNOWN_META_LANGS);
        if (p >= 0) {
          StringBuilder sb = new StringBuilder();
          Iterator<String> it = area_codes.iterator();
          while (it.hasNext()) {
            String code = it.next();
            int j = StrUtils.getIndexOf(code, KNOWN_AREA_CODES);
            StrUtils.addToEnum(sb, j >= 0 ? KNOWN_AREA_DESCS[j].get(p) : code);
          }
          areaDescs = sb.toString();
        } else {
          areaDescs = StrUtils.getEnumeration(area_codes);
        }

        // Add additional subjects        
        if (area != null && area.length() > 0) {
          areaDescs = StrUtils.addToEnum(areaDescs, area);
        }

        jso.put(lang, areaDescs);
      }
      json.put("areas", jso);
    } else if (area != null) {
      JSONObject jso = new JSONObject();
      for (String lang : langTags) {
        jso.put(lang, area);
      }
      json.put("areas", jso);
    }

    // Fill in project educational levels, both as codes and descriptive names
    if (level_codes != null && level_codes.size() > 0) {
      // Fill in level codes
      json.put("levelCodes", new JSONArray(level_codes));

      JSONObject jso = new JSONObject();
      for (String lang : langTags) {
        String levelDescs = "";
        int p = StrUtils.getIndexOf(lang, KNOWN_META_LANGS);
        if (p >= 0) {
          StringBuilder sb = new StringBuilder();
          Iterator<String> it = level_codes.iterator();
          while (it.hasNext()) {
            String level = it.next();
            int j = StrUtils.getIndexOf(level, KNOWN_LEVEL_CODES);
            StrUtils.addToEnum(sb, j >= 0 ? KNOWN_LEVEL_DESCS[j].get(p) : level);
          }
          levelDescs = sb.toString();
        } else {
          levelDescs = StrUtils.getEnumeration(level_codes);
        }

        // Add additional levels
        if (level != null && level.length() > 0) {
          levelDescs = StrUtils.addToEnum(levelDescs, level);
        }
        jso.put(lang, levelDescs);
      }
      json.put("levels", jso);
    } else if (level != null) {
      JSONObject jso = new JSONObject();
      for (String lang : langTags) {
        jso.put(lang, level);
      }
      json.put("levels", jso);
    }

    // Fill in descriptions
    JSONObject jso = new JSONObject();
    for (int i = 0; i < numLangs && i < descriptions.length; i++) {
      if (descriptions[i] != null) {
        jso.put(langTags[i], descriptions[i]);
      }
    }
    json.put("description", jso);

    // Fill in license
    jso = new JSONObject();
    String url = license < OTHER ? CC_URL.replace("%%CODE%%", CC_LICENSES[license]) : OTHER_URL;
    for (int i = 0; i < numLangs; i++) {
      int p = StrUtils.getIndexOf(langTags[i], KNOWN_META_LANGS);
      if(p < 0)
        p = 2;
      String str = license < OTHER ? LICENSE_DESC[p].replace("%%URL%%", url).replace("%%CODE%%", CC_LICENSES[license].toUpperCase()) : LICENSE_OTHER[p];
      jso.put(langTags[i], str);
    }
    json.put("license", jso);
    
    // Fill in meta langs
    json.put("meta_langs", new JSONArray(langTags));
    
    // Fill in cover and thumbnail images
    if (coverFileName != null) {
      json.put("cover", coverFileName);
    }
    if (thumbnailFileName != null) {
      json.put("thumbnail", thumbnailFileName);
    }

    return json;
  }

  public static String[] getLicensesList(String otherTxt) {
    ArrayList<String> v = new ArrayList<String>();
    for (int i = 0; i < OTHER; i++) {
      v.add("Creative Commons " + CC_LICENSES[i].toUpperCase());
    }
    v.add(otherTxt);
    return (String[]) v.toArray(new String[v.size()]);
  }

  @Override
  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

}
