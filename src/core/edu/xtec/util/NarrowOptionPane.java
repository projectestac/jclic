/*
 * File    : NarrowOptionPane.java
 * Created : 23-jan-2004 09:46
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

import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class NarrowOptionPane extends JOptionPane {

  public static final int DEFAULT_CH = 60;
  int maxCharactersPerLineCount;

  /** Creates a new instance of NarrowOptionPane */
  public NarrowOptionPane(int maxCharactersPerLineCount) {
    this(maxCharactersPerLineCount, "JOptionPane message");
  }

  public NarrowOptionPane(int maxCharactersPerLineCount, Object message) {
    this(maxCharactersPerLineCount, message, JOptionPane.PLAIN_MESSAGE);
  }

  public NarrowOptionPane(int maxCharactersPerLineCount, Object message, int messageType) {
    this(maxCharactersPerLineCount, message, messageType, JOptionPane.DEFAULT_OPTION);
  }

  public NarrowOptionPane(int maxCharactersPerLineCount, Object message, int messageType, int optionType) {
    this(maxCharactersPerLineCount, message, messageType, optionType, null);
  }

  public NarrowOptionPane(int maxCharactersPerLineCount, Object message, int messageType, int optionType, Icon icon) {
    this(maxCharactersPerLineCount, message, messageType, optionType, icon, null);
  }

  public NarrowOptionPane(int maxCharactersPerLineCount, Object message, int messageType, int optionType, Icon icon,
      Object[] options) {
    this(maxCharactersPerLineCount, message, messageType, optionType, icon, options, null);
  }

  public NarrowOptionPane(int maxCharactersPerLineCount, Object message, int messageType, int optionType, Icon icon,
      Object[] options, Object initialValue) {
    if ((message instanceof String) && ((String) message).trim().toLowerCase().startsWith("<html>")) {
      maxCharactersPerLineCount = Integer.MAX_VALUE;
    }
    this.maxCharactersPerLineCount = maxCharactersPerLineCount;
    setMessage(message);
    setOptions(options);
    setInitialValue(initialValue);
    setIcon(icon);
    setMessageType(messageType);
    setOptionType(optionType);
    value = JOptionPane.UNINITIALIZED_VALUE;
    inputValue = JOptionPane.UNINITIALIZED_VALUE;
    updateUI();
  }

  @Override
  public int getMaxCharactersPerLineCount() {
    return maxCharactersPerLineCount;
  }
}
