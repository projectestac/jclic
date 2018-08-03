/*
 * File    : Domable.java
 * Created : 24-mar-2003 15:26
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

package edu.xtec.util;

/**
 * This interface applies to all classes that can serialize and de-serialize its
 * data into a XML {@link org.jdom.Element} of type
 * <a href="http://www.jdom.org">JDOM</a>. The <CODE>getJDomElement
 * </CODE> method stores the current non-transient data into a JDOM element, and
 * <CODE>setProperties
 * </CODE> reads the data. <CODE>Domable</CODE> classes should implement also a
 * static method that builds new objects from a provided Element.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public interface Domable {

  org.jdom.Element getJDomElement();

  void setProperties(org.jdom.Element e, Object aux) throws Exception;
}
