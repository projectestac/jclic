/*
 * File    : AutoContentProviderEditor.java
 * Created : 04-mar-2004 10:32
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2018 Francesc Busquets & Departament
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

package edu.xtec.jclic.automation;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.util.Options;
import edu.xtec.util.TripleString;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public abstract class AutoContentProviderEditor extends Editor {

  protected static ImageIcon icon;

  /** Creates a new instance of AutoContentProviderEditor */
  public AutoContentProviderEditor(AutoContentProvider acp) {
    super(acp);
  }

  protected void createChildren() {
  }

  public AutoContentProvider getAutoContentProvider() {
    return (AutoContentProvider) userObject;
  }

  @Override
  public String getTitleKey() {
    return "edit_acp";
  }

  public static Icon getIcon() {
    if (icon == null)
      icon = edu.xtec.util.ResourceManager.getImageIcon("icons/miniclic.png");
    return icon;
  }

  public static final String SYSTEM_LIST = "automation.contentproviders";

  public static List<TripleString> getSystemContentProvidersList(Options options) {
    List<TripleString> result;
    try {
      result = TripleString.getTripleList(SYSTEM_LIST, options, true, true, true);
    } catch (Exception ex) {
      System.err.println("Error reading list of content providers!\n" + ex);
      result = new ArrayList<TripleString>();
    }
    return result;
  }

  public Activity getActivity() {
    return (Activity) getFirstObject(Activity.class);
  }
}
