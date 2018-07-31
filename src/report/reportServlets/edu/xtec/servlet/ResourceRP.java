/*
 * File    : ResourceRP.java
 * Created : 23-jan-2003 10:42
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

package edu.xtec.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class ResourceRP extends RequestProcessor {

  protected String objectName;
  protected byte[] objectData;
  public static final String ID = "id", LAST_MODIFIED = "Last-Modified";
  public static final String RESOURCE_BASE = "html/";

  protected static Map<String, Object> objects = new HashMap<String, Object>();
  protected static String dateStr = httpDate(new java.util.Date());

  /** Creates a new instance of ResourceRP */
  public ResourceRP() {
    super();
  }

  @Override
  public boolean noCache() {
    return false;
  }

  @Override
  public boolean init() throws Exception {
    objectName = getParam(ID);
    if (objectName != null) {
      objectData = (byte[]) objects.get(objectName);
      if (objectData == null) {
        try {
          StringBuilder sb = new StringBuilder(RESOURCE_BASE);
          sb.append(objectName);
          objectData = edu.xtec.util.ResourceManager.getResourceBytes(sb.substring(0));
          objects.put(objectName, objectData);
        } catch (Exception ex) {
          errCode = HTTP_NOT_FOUND;
          errMsg = objectName + " not found!";
        }
      }
    } else {
      errCode = HTTP_BAD_REQUEST;
      errMsg = "Unespecified object id!";
    }
    return true;
  };

  @Override
  public void header(List<String[]> v) {
    super.header(v);
    if (objectData != null) {
      String ct = null;
      if (objectName.endsWith(".gif")) ct = "image/gif";
      else if (objectName.endsWith(".jpg")) ct = "text/jpg";
      else if (objectName.endsWith(".css")) ct = "text/css";
      if (ct != null) {
        v.add(new String[] {CONTENT_TYPE, ct});
      }
      v.add(new String[] {CONTENT_LENGTH, Integer.toString(objectData.length)});
      v.add(new String[] {EXTRA, LAST_MODIFIED, dateStr});
    }
  }

  @Override
  public boolean usesWriter() {
    return false;
  }

  @Override
  public void process(java.io.OutputStream out) throws Exception {
    if (objectData != null) out.write(objectData, 0, objectData.length);
  }
}
