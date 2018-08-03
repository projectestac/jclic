/*
 * File    : SmallIntEditor.java
 * Created : 02-dec-2002 16:14
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

package edu.xtec.jclic.beans;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class SmallIntEditor extends JPanel implements ActionListener {

  public static final String PROP_VALUE = "value";
  private JButton plusButton, minusButton;
  private NumberField textFld;
  private int max, min, lastValue;
  private Toolkit toolkit;
  private NumberFormat integerFormatter;
  private int[] values;
  boolean editable;

  /** Creates a new instance of SmallIntEditor */
  public SmallIntEditor() {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setOpaque(false);
    editable = true;
    toolkit = Toolkit.getDefaultToolkit();
    integerFormatter = NumberFormat.getNumberInstance();
    integerFormatter.setParseIntegerOnly(true);
    max = 10;
    min = 0;
    plusButton = new JButton(new ImageIcon(getClass().getResource("/edu/xtec/resources/icons/plus.gif")));
    plusButton.setMnemonic('+');
    plusButton.addActionListener(this);
    plusButton.setPreferredSize(new java.awt.Dimension(16, 16));
    minusButton = new JButton(new ImageIcon(getClass().getResource("/edu/xtec/resources/icons/minus.gif")));
    minusButton.setMnemonic('-');
    minusButton.addActionListener(this);
    minusButton.setPreferredSize(new java.awt.Dimension(16, 16));
    textFld = new NumberField(4);
    textFld.setColumns(4);
    textFld.setHorizontalAlignment(JTextField.CENTER);
    textFld.setText("0");
    textFld.addActionListener(this);
    add(minusButton, BorderLayout.WEST);
    add(textFld, BorderLayout.CENTER);
    add(plusButton, BorderLayout.EAST);
  }

  @Override
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    textFld.setToolTipText(text);
    plusButton.setToolTipText(text);
    minusButton.setToolTipText(text);
  }

  public void actionPerformed(ActionEvent ev) {
    int oldValue = lastValue;
    boolean btPlus = (plusButton == ev.getSource());
    boolean btMinus = (minusButton == ev.getSource());
    int delta = btPlus ? 1 : btMinus ? -1 : 0;
    if (delta != 0 && values != null && values.length > 0) {
      int p = 0;
      while (p < values.length && oldValue > values[p])
        p++;
      if (delta < 0 || (p < values.length && values[p] == oldValue))
        p += delta;
      if (p >= 0 && p < values.length)
        delta = values[p] - oldValue;
      else
        delta = 0;
    }

    if (delta != 0) {
      setValue(getValue() + delta);
    }
    checkValueRanges();
    checkEnabled();
    lastValue = getValue();
    firePropertyChange(PROP_VALUE, oldValue, lastValue);
  }

  private void checkEnabled() {
    int value = getValue();
    minusButton.setEnabled(isEnabled() && value > min);
    plusButton.setEnabled(isEnabled() && value < max);
    textFld.setEnabled(isEnabled() && editable);
  }

  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    checkEnabled();
  }

  public int getValue() {
    int retVal = 0;
    String s = textFld.getText();
    if (s != null && s.length() > 0) {
      try {
        retVal = integerFormatter.parse(s).intValue();
      } catch (ParseException e) {
        // This should never happen because insertString allows
        // only properly formatted data to get in the field.
        toolkit.beep();
        System.err.println("Bad integer value " + s + ": " + e.getMessage());
      }
    }
    return correctValue(retVal);
  }

  public void setValue(int value) {
    lastValue = correctValue(value);
    textFld.setText(integerFormatter.format(lastValue));
  }

  public int correctValue(int value) {
    return Math.min(max, Math.max(min, value));
  }

  private void checkValueRanges() {
    int v = 0;
    try {
      v = integerFormatter.parse(textFld.getText()).intValue();
    } catch (ParseException e) {
    }
    if (v < min || v > max)
      setValue(correctValue(v));
  }

  public void setEditColumns(int columns) {
    textFld.setColumns(columns);
  }

  public int getEditColumns() {
    return textFld.getColumns();
  }

  /**
   * Getter for property max.
   *
   * @return Value of property max.
   */
  public int getMax() {
    return max;
  }

  /**
   * Setter for property max.
   *
   * @param max New value of property max.
   */
  public void setMax(int max) {
    this.max = max;
  }

  /**
   * Getter for property min.
   *
   * @return Value of property min.
   */
  public int getMin() {
    return min;
  }

  /**
   * Setter for property min.
   *
   * @param min New value of property min.
   */
  public void setMin(int min) {
    this.min = min;
  }

  /**
   * Getter for property values.
   *
   * @return Value of property values.
   */
  public int[] getValues() {
    return this.values;
  }

  /**
   * Setter for property values.
   *
   * @param values New value of property values.
   */
  public void setValues(int[] values) {
    this.values = values;
  }

  /**
   * Getter for property editable.
   *
   * @return Value of property editable.
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * Setter for property editable.
   *
   * @param editable New value of property editable.
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
    checkEnabled();
  }

  class NumberField extends JTextField {

    public NumberField(int columns) {
      super(columns);
    }

    @Override
    protected Document createDefaultModel() {
      return new WholeNumberDocument();
    }

    protected class WholeNumberDocument extends PlainDocument {
      @Override
      public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        char[] source = str.toCharArray();
        char[] result = new char[source.length];
        int j = 0;

        for (int i = 0; i < result.length; i++) {
          if (Character.isDigit(source[i]) || source[i] == '-')
            result[j++] = source[i];
          else {
            toolkit.beep();
          }
        }
        super.insertString(offs, new String(result, 0, j), a);
        checkEnabled();
      }

      @Override
      public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        checkEnabled();
      }
    }
  }
}
