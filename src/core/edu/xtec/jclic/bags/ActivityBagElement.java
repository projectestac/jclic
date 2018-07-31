/*
 * File    : ActivityBagElement.java
 * Created : 19-dec-2000 15:52
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

package edu.xtec.jclic.bags;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.edit.Editable;
import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.fileSystem.FileSystem;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores a XML {@link org.jdom.Element} that defines an {@link edu.xtec.jclic.Activity}.
 * It stores also a {@link java.util.HashMap} with references of other objects to this activity, and
 * implements some useful methods to directly retrieve some properites of the related Activity, like
 * its name. ActivityBagElements are usually stored into {@link edu.xtec.jclic.bags.ActivityBag}
 * objects.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class ActivityBagElement extends Object implements Editable, Cloneable {

  private org.jdom.Element element;
  private Map<String, String> references;

  /** Creates new ActivityBagElement */
  public ActivityBagElement(org.jdom.Element e) {
    setData(e);
  }

  public String getName() {
    return FileSystem.stdFn(element.getAttributeValue(Activity.NAME));
  }

  public void setData(org.jdom.Element e) {
    element = e;
    references = null;
  }

  public org.jdom.Element getData() {
    return element;
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * Getter for property dependences.
   *
   * @return Value of property dependences.
   */
  public Map<String, String> getReferences() {
    if (references == null && element != null) {
      references = new HashMap<String, String>();
      Activity.listReferences(element, references);
    }
    return references;
  }

  public Editor getEditor(Editor parent) {
    return Editor.createEditor(getClass().getName() + "Editor", this, parent);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    ActivityBagElement result = (ActivityBagElement) super.clone();
    result.references = null;
    result.element = (org.jdom.Element) element.clone();
    return result;
  }
}
