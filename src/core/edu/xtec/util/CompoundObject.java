/*
 * File    : CompoundObject.java
 * Created : 17-feb-2003 10:12
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

package edu.xtec.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * This class encapsulates three fields: a text <CODE>identifier</CODE>, a <CODE>label</CODE> and an
 * <CODE>icon</CODE>. The icon is always an external resource, accessible through an URL.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class CompoundObject implements Serializable {

  protected String id = null;
  protected String text = null;
  protected String iconUrl = null;
  private transient ImageIcon icon = null;

  /** Creates a new instance of CompoundObject */
  protected CompoundObject() {}

  /** Creates a new instance of CompoundObject */
  public CompoundObject(String id, String text, String iconUrl) {
    this.id = id;
    this.text = text;
    this.iconUrl = iconUrl;
  }

  public boolean hasIcon() {
    return iconUrl != null && iconUrl.length() > 0;
  }

  public ImageIcon getIcon() {
    if (icon == null && iconUrl != null && iconUrl.length() > 0) {
      String s = iconUrl;
      try {
        // Modified 18/Jan/2010
        // Try a more generic URL protocol detection
        //
        // if(!s.startsWith("http:"))
        //    s="file:/"+s;
        URL url;
        try {
          url = new URL(s);
        } catch (MalformedURLException mex) {
          url = new URL("file://" + s);
        }
        icon = new ImageIcon(url);
      } catch (Exception ex) {
        System.err.println("Unable to read image " + iconUrl + "\n" + ex);
      }
    }
    return icon;
  }

  public void clearIcon() {
    icon = null;
  }

  /**
   * Getter for property id.
   *
   * @return Value of property id.
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * Setter for property id.
   *
   * @param id New value of property id.
   */
  public void setId(java.lang.String id) {
    this.id = id;
  }

  /**
   * Getter for property text.
   *
   * @return Value of property text.
   */
  public java.lang.String getText() {
    return text;
  }

  /**
   * Setter for property text.
   *
   * @param text New value of property text.
   */
  public void setText(java.lang.String text) {
    this.text = text;
  }

  /**
   * Getter for property iconUrl.
   *
   * @return Value of property iconUrl.
   */
  public java.lang.String getIconUrl() {
    return iconUrl;
  }

  /**
   * Setter for property iconUrl.
   *
   * @param iconUrl New value of property iconUrl.
   */
  public void setIconUrl(java.lang.String iconUrl) {
    this.iconUrl = iconUrl;
  }
}
