/*
 * JClic accessibility
 * TFormExchange.java
 * Created on august / 2009
 *
 * @author Jordi Lagares Roset "jlagares@xtec.cat - www.lagares.org"
 * amb el suport del Departament d'Educacio de la Generalitat de Catalunya
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
package edu.xtec.jclic.accessibility;

import java.awt.Color;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author jlagares
 */
public class TFormExchange extends JFrame {

    public JButton exchangeBtn;
    public static final Color DEFAULT_COLOR_A = Color.BLUE;
    public static final Color DEFAULT_COLOR_B = Color.RED;
    Color colorA = DEFAULT_COLOR_A;
    Color colorB = DEFAULT_COLOR_B;

    public TFormExchange(Rectangle r, String btnText) {
        this(r.x, r.y, r.width, r.height, btnText);
    }

    public TFormExchange(int x, int y, int dx, int dy, String btnText) {
        this(x, y, dx, dy, btnText, DEFAULT_COLOR_A, DEFAULT_COLOR_B);
    }

    public TFormExchange(int x, int y, int dx, int dy, String btnText, Color c1, Color c2) {
        setUndecorated(true);
        setResizable(false);
        setLocation(x, y);
        setSize(dx, dy);
        colorA = c1;
        colorB = c2;
        exchangeBtn = new JButton();
        exchangeBtn.setText(btnText);
        exchangeBtn.setBackground(colorA);
        getContentPane().add(exchangeBtn);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
    }

    public void changeBgColor(int curZone) {
        exchangeBtn.setBackground(curZone == 0 ? colorA : colorB);
    }
}
