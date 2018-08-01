/*
 * File    : TagReplace.java
 * Created : 30-sep-2006 11:00
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2006 Francesc Busquets & Departament
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

package edu.xtec.jclic.automation.tagreplace;

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.activities.text.TargetMarker;
import edu.xtec.jclic.activities.text.TextActivityDocument;
import edu.xtec.jclic.activities.text.TextTarget;
import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.automation.AutoContentProvider;
import edu.xtec.jclic.automation.TextActivityContentKit;
import edu.xtec.jclic.boxes.ActiveBagContent;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ResourceBridge;
import edu.xtec.util.StrUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class TagReplace extends AutoContentProvider {

  public static final String DEFAULT_TAG_START = "[";
  public static final String DEFAULT_TAG_END = "]";
  public static final String DEFAULT_CHARSET = "UTF8";

  public String tagStart;
  public String tagEnd;
  public String mapFileName;
  public String fileCharset;
  private HashMap<String, String> map;

  public static final String TAG_START = "tagStart", TAG_END = "tagEnd", MAP_FN = "mapFn", MAP_CHARSET = "charset";

  /** Creates a new instance of TagReplace */
  public TagReplace() {
    tagStart = DEFAULT_TAG_START;
    tagEnd = DEFAULT_TAG_END;
    fileCharset = DEFAULT_CHARSET;
  }

  @Override
  public org.jdom.Element getJDomElement() {

    org.jdom.Element e = super.getJDomElement();
    e.setAttribute(TAG_START, tagStart);
    e.setAttribute(TAG_END, tagEnd);
    if (mapFileName != null) {
      e.setAttribute(MAP_FN, mapFileName);
      e.setAttribute(MAP_CHARSET, fileCharset);
    }
    return e;
  }

  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    tagStart = JDomUtility.getStringAttr(e, TAG_START, DEFAULT_TAG_START, false);
    tagEnd = JDomUtility.getStringAttr(e, TAG_END, DEFAULT_TAG_END, false);
    mapFileName = JDomUtility.getStringAttr(e, MAP_FN, null, false);
    fileCharset = JDomUtility.getStringAttr(e, MAP_CHARSET, DEFAULT_CHARSET, false);
    map = null;
  }

  public static boolean checkClient(Class cl) {
    return ActiveBagContentKit.Compatible.class.isAssignableFrom(cl)
        || TextActivityContentKit.Compatible.class.isAssignableFrom(cl);
  }

  public boolean generateContent(Object kit, ResourceBridge rb) {
    boolean result = false;
    if (kit instanceof ActiveBagContentKit) {
      ActiveBagContentKit k = (ActiveBagContentKit) kit;
      result = generateContent(k.nRows, k.nCols, k.content, k.useIds, rb);
    } else if (kit instanceof TextActivityContentKit) {
      result = generateContent((TextActivityContentKit) kit);
    }
    return result;
  }

  protected boolean generateContent(TextActivityContentKit k) {
    boolean result = false;
    if (k.tad != null) {
      try {
        k.checkButtonText = filter(k.checkButtonText);
        k.prevScreenText = filter(k.prevScreenText);
        filterDoc(k.tad);
        result = true;
      } catch (BadLocationException ex) {
        System.err.println("Error processing text document: " + ex);
        return false;
      }
    }
    return result;
  }

  protected boolean generateContent(int nRows, int nCols, ActiveBagContent[] content, boolean useIds,
      ResourceBridge rb) {
    if (content == null || content.length < 1 || rb == null)
      return false;

    for (ActiveBagContent abc : content)
      filterActiveBagContent(abc);

    return true;
  }

  protected void filterActiveBagContent(ActiveBagContent abc) {
    if (abc != null) {
      for (int j = 0; j < abc.getNumCells(); j++) {
        filterActiveBoxContent(abc.getActiveBoxContent(j));
      }
    }
  }

  protected void filterActiveBoxContent(ActiveBoxContent abx) {
    if (abx != null) {
      abx.text = filter(abx.text);
      abx.imgName = filter(abx.imgName);
      if (abx.mediaContent != null) {
        abx.mediaContent.mediaFileName = filter(abx.mediaContent.mediaFileName);
      }
    }
  }

  protected List<Object[]> locateTags(String src) {
    ArrayList<Object[]> result = new ArrayList<Object[]>();
    if (src != null && map != null) {
      int p = 0;
      int pStart;
      int pEnd;
      while ((pStart = src.indexOf(tagStart, p)) >= 0) {
        pEnd = src.indexOf(tagEnd, pStart + tagStart.length());
        if (pEnd <= pStart)
          break;
        Object[] tagMark = new Object[4];
        tagMark[0] = new Integer(pStart);
        tagMark[1] = new Integer(pEnd + tagEnd.length() - pStart);
        String key = src.substring(pStart + tagStart.length(), pEnd);
        String value = map.get(key);
        if (value != null) {
          tagMark[2] = value;
          // tagMark[3] is reserved
          result.add(tagMark);
        }
        p = pEnd + tagEnd.length();
      }
    }
    return result;
  }

  protected String filter(String src) {
    String result = src;
    if (src != null) {
      StringBuilder sb = new StringBuilder();
      List<Object[]> tagMarks = locateTags(src);
      int p = 0;
      for (Object[] tagMark : tagMarks) {
        int pStart = ((Integer) tagMark[0]).intValue();
        int tagLength = ((Integer) tagMark[1]).intValue();
        String value = (String) tagMark[2];
        sb.append(src.substring(p, pStart));
        sb.append(value);
        p = pStart + tagLength;
      }
      sb.append(src.substring(p));
      result = sb.substring(0);
    }
    return result;
  }

  protected void filter(String[] strArray) {
    if (strArray != null) {
      for (int i = 0; i < strArray.length; i++) {
        strArray[i] = filter(strArray[i]);
      }
    }
  }

  protected void filterDoc(TextActivityDocument tad) throws BadLocationException {
    if (tad != null) {
      String src = tad.getText(0, tad.getLength());
      List<Object[]> tagMarks = locateTags(src);
      int s = tagMarks.size();
      for (int i = 0; i < s; i++) {
        Object[] tagMark = (Object[]) tagMarks.get(i);
        tagMark[3] = tad.createPosition(((Integer) tagMark[0]).intValue());
      }
      for (int i = 0; i < s; i++) {
        Object[] tagMark = (Object[]) tagMarks.get(i);
        Position position = (Position) tagMark[3];
        int pos = position.getOffset();
        int tagLength = ((Integer) tagMark[1]).intValue();
        String value = (String) tagMark[2];
        AttributeSet as = tad.getCharacterElement(pos).getAttributes();
        tad.getLogicalStyle(pos);
        tad.insertString(pos, value, as);
        tad.remove(pos + value.length(), tagLength);
      }
      filterActiveBagContent(tad.boxesContent);
      filterActiveBagContent(tad.popupsContent);
      if (tad.tmb != null) {
        for (TargetMarker tm : tad.tmb) {
          TextTarget target = tm.target;
          if (target != null) {
            target.iniText = filter(target.iniText);
            filter(target.answer);
            filter(target.options);
            filterActiveBoxContent(target.popupContent);
          }
        }
      }
    }
  }

  @Override
  public void init(ResourceBridge rb, FileSystem fs) {
    if (fs != null && mapFileName != null) {
      try {
        InputStream is = fs.getInputStream(mapFileName);
        if (is != null) {
          BufferedReader bfr = new BufferedReader(new InputStreamReader(is, fileCharset));
          map = new HashMap<String, String>();
          String line;
          while ((line = bfr.readLine()) != null) {
            int p = line.indexOf('=');
            if (p > 0) {
              String key = StrUtils.secureString(line.substring(0, p)).trim();
              String value = StrUtils.secureString(line.substring(p + 1)).trim();
              if (key.length() > 0)
                map.put(key, value);
            }
          }
        }
      } catch (Exception ex) {
        System.err.println("Error initializing TagReplace: " + ex);
      }
    }
  }

  @Override
  public void innerListReferences(Map<String, String> map) throws Exception {
    if (mapFileName != null)
      map.put(mapFileName, Constants.EXTERNAL_OBJECT);
  }
}
