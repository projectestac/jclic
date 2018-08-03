/*
 * File    : Editable.java
 * Created : 05-jun-2002 17:25
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

package edu.xtec.jclic.edit;

/**
 * Classes that implement this interface have always an associated
 * {@link edu.xtec.jclic.edit.Editor}. The interface has one single method to
 * implement: <CODE>getEditor
 * </CODE>.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public interface Editable {
  /**
   * Returns the {@link edu.xtec.jclic.edit.Editor} associated to this object.
   *
   * @param parent Editors can have a parent editor. Childs should communicate its
   *               changes to parents, and changes in parents should affect its
   *               childs. This parameter can be <I>null</I> in stand-alone
   *               objects.
   * @return An Editor for this object.
   */
  public Editor getEditor(Editor parent);
}
