/*
 * File    : ActiveBagContentKit.java
 * Created : 04-mar-2004 10:55
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

package edu.xtec.jclic.automation;

import edu.xtec.jclic.boxes.ActiveBagContent;

/**
 * This class is used by {@link edu.xtec.jclic.Activity} objects to interact
 * with {@link edu.xtec.jclic.automation.AutoContentProvider} objects.
 * Activities that use {@link edu.xtec.jclic.boxes.ActiveBagContent} objects,
 * like puzzles or associations, use ActiveBagContentKits to pass all its
 * containers (up to three) in a single argument when calling the
 * <CODE>generateContent</CODE> method of <CODE>AutoContentProvider</CODE>.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 */
public class ActiveBagContentKit {

  public int nRows;
  public int nCols;
  public ActiveBagContent[] content;
  public boolean useIds;

  /** Creates a new instance of ActiveBagContentKit */
  public ActiveBagContentKit(int nRows, int nCols, ActiveBagContent[] content, boolean useIds) {
    this.nRows = nRows;
    this.nCols = nCols;
    this.content = content;
    this.useIds = useIds;
  }

  /**
   * Activities should implement this interface when its contents are formed
   * basically by {@link edu.xtec.jclic.boxes.ActiveBagContent} objects, in order
   * to let {@link edu.xtec.jclic.automation.AutoContentProvider} objects to check
   * whether they are compatible with them.
   */
  public interface Compatible {
  };
}
