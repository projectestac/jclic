/*
 * File    : MultiBundle.java
 * Created : 28-jan-2002 11:42
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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class MultiBundle {

  java.util.ArrayList<ResourceBundleEx> bundles;

  public MultiBundle(ResourceBundle mainBundle, String resource, Locale l) {
    bundles = new java.util.ArrayList<ResourceBundleEx>(1);
    bundles.add(new ResourceBundleEx(mainBundle, resource, l));
  }

  public void addBundle(ResourceBundle bundle, String resource, Locale l) {
    for (int i = 0; i < bundles.size(); i++)
      if (((ResourceBundleEx) bundles.get(i)).resource.equals(resource)) return;
    bundles.add(new ResourceBundleEx(bundle, resource, l));
  }

  public void setLocale(Locale l) {
    for (int i = 0; i < bundles.size(); i++) (bundles.get(i)).setLocale(l);
  }

  public String getString(String key) {
    String result = key;
    for (int i = 0; i < bundles.size(); i++) {
      try {
        result = bundles.get(i).bundle.getString(key);
        return result;
      } catch (Exception ex) {
        // do nothing
      }
    }

    System.err.println("Unable to find resource message: [" + result + "]");
    return result;
  }

  class ResourceBundleEx {
    ResourceBundle bundle = null;
    String resource = null;
    Locale locale = null;

    ResourceBundleEx(ResourceBundle bundle, String resource, Locale locale) {
      this.resource = resource;
      this.locale = locale;
      this.bundle = bundle;
    }

    void setLocale(Locale l) {
      if (!locale.equals(l)) {
        try {
          bundle = ResourceManager.getBundle(resource, l);
          locale = l;
        } catch (Exception ex) {
          System.err.println("unable to build messagesBundle: " + resource);
          System.err.println(ex);
        }
      }
    }
  }
}
