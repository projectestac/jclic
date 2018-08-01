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

import edu.xtec.util.Options;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author jlagares
 *         <p>
 *         TODO: Allow different keyboard layouts TODO: Normalize the creation
 *         of icons
 */
public class TFormKeyboard extends JFrame {

  JPanel panelPlaphoons;
  public static final int CELLS_X = 15, CELLS_Y = 5;
  public static final int DEFAULT_POS_X = 100, DEFAULT_POS_Y = 100;
  public static final int DEFAULT_WIDTH = 445, DEFAULT_HEIGHT = 160;
  public static final float FONT_SIZE_INCREMENT = 4.0F;
  int cellWidth;
  int cellHeight;
  JLabel keys[][] = new JLabel[CELLS_X][CELLS_Y];
  boolean full[][] = new boolean[CELLS_X][CELLS_Y];
  int frameBorder = 1;
  int frameSpan = 2;
  int span = frameSpan;
  boolean isShiftLock = false;
  boolean isShift = false;
  int iIsShift, nIsShift;
  boolean isHorizontalScanning = false;
  int xPos = 0;
  int yPos = 0;
  FressaFunctions fressa;
  Font font;
  Font boldFont;

  public TFormKeyboard(Options options, FressaFunctions fressa) {

    this.fressa = fressa;

    setResizable(true);
    setLocation(DEFAULT_POS_X, DEFAULT_POS_Y);
    setTitle(options.getMsg("acc_keyboardTitle"));
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    panelPlaphoons = new JPanel();
    panelPlaphoons.setBackground(Color.white);
    panelPlaphoons.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    add(panelPlaphoons, BorderLayout.CENTER);
    panelPlaphoons.setLayout(null);
    setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

    font = (new JLabel()).getFont();
    font = font.deriveFont(font.getSize2D() + FONT_SIZE_INCREMENT);
    boldFont = font.deriveFont(Font.BOLD);

    java.awt.event.MouseAdapter mAdapter = new java.awt.event.MouseAdapter() {

      @Override
      public void mouseReleased(java.awt.event.MouseEvent me) {
        if (me.getButton() == java.awt.event.MouseEvent.BUTTON1) {
          plafonsOnClick(me);
        }
      }
    };

    for (int i = 0; i < CELLS_X; i++) {
      for (int n = 0; n < CELLS_Y; n++) {
        full[i][n] = false;
        keys[i][n] = new JLabel();

        if (i > 0) {
          keys[i][n].setBorder(BorderFactory.createLineBorder(Color.black, frameBorder));
        }
        keys[i][n].setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        keys[i][n].setFont(font);

        panelPlaphoons.add(keys[i][n]);
        keys[i][n].addMouseListener(mAdapter);
      }
    }

    lowercase();
    setAlwaysOnTop(true);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height));
    calcFrameSize();

    addComponentListener(new java.awt.event.ComponentAdapter() {

      @Override
      public void componentResized(java.awt.event.ComponentEvent evt) {
        calcFrameSize();
      }
    });
  }

  void sendKey(int t) {
    if (fressa.robot != null) {
      fressa.robot.keyPress(t);
      fressa.robot.keyRelease(t);
    }
    isShiftFalse();
  }

  void sendShiftKey(int t) {
    if (fressa.robot != null) {
      fressa.robot.keyPress(KeyEvent.VK_SHIFT);
      fressa.robot.keyPress(t);
      fressa.robot.keyRelease(t);
      fressa.robot.keyRelease(KeyEvent.VK_SHIFT);
    }
    isShiftFalse();
  }

  void isShiftFalse() {
    if (isShift) {
      isShift = false;
      // WARNING: possible ArrayOutOfBoundsException:
      keys[iIsShift][nIsShift].setForeground(Color.black);
      // WARNING: possible unnecessary repaint of all panel:
      if (isShiftLock) {
        uppercase();
      } else {
        lowercase();
      }
    }
  }

  public void changeScanningPosition() {
    if (isHorizontalScanning) {
      xPos++;
      if (xPos >= CELLS_X) {
        isHorizontalScanning = false;
        xPos = 0;
        yPos = 0;
      } else {
        // WARNING: possible outOfBoundsException and NullPointerException:
        while ((keys[xPos][yPos].getText().equals("")) && (!full[xPos][yPos])) {
          xPos++;
          if (xPos >= CELLS_X) {
            isHorizontalScanning = false;
            xPos = 0;
            yPos = 0;
            break;
          }
        }
      }
    } else {
      yPos = (yPos + 1) % CELLS_Y;
    }

    int x = keys[xPos][yPos].getLocationOnScreen().x + cellWidth / 2;
    int y = keys[xPos][yPos].getLocationOnScreen().y + cellHeight / 2;
    mouseMove(x, y);

    if (fressa.readLabels && !keys[xPos][yPos].getText().equals("")) {
      fressa.readText(keys[xPos][yPos].getText());
    }
  }

  public void doClick() {
    if (fressa.robot != null) {
      fressa.robot.mousePress(InputEvent.BUTTON1_MASK);
      fressa.robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }
  }

  public void mouseMove(int x, int y) {
    if (fressa.robot != null) {
      fressa.robot.mouseMove(x, y);
    }
  }

  public void plafonsOnClick(java.awt.event.MouseEvent evt) {

    // WARNING: Too much "returns"
    int ic = -1;
    int nc = -1;

    if (fressa.actPanel == null) {
      return;
    }

    // WARNINIG: Use SwingUtilities.InvokeLater instead
    fressa.actPanel.requestFocus();
    int wait = 0, maxWait = 10;
    while (!fressa.actPanel.hasFocus() && ++wait <= maxWait) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException ex) {
        System.err.println("Error while waiting to transfer focus. " + ex.getMessage());
        break;
      }
    }
    if (!fressa.actPanel.hasFocus()) {
      System.err.println("Accessible keyboard: Error transferring focus!");
      return;
    }

    for (int i = 0; i < CELLS_X; i++) {
      for (int n = 0; n < CELLS_Y; n++) {
        if (evt.getComponent().equals(keys[i][n])) {
          ic = i;
          nc = n;
          break;
        }
      }
    }

    if ((ic == -1) && (nc == -1)) {
      return;
    }

    fressa.mustDisableScanTimer = true;
    if (ic == 0) {
      isHorizontalScanning = true;
      xPos = 0;
      changeScanningPosition();
      return;
    } else {
      isHorizontalScanning = false;
      xPos = 0;
      yPos = -1;
    }
    if ((nc == 0) && (ic == 14)) {
      sendKey(KeyEvent.VK_BACK_SPACE);
    } else if (keys[ic][nc].getText().equals("tab")) {
      if (isShift) {
        sendShiftKey(KeyEvent.VK_TAB);
      } else {
        sendKey(KeyEvent.VK_TAB);
      }
    } else if ((((nc == 1) || (nc == 2)) && (ic == 14)) || ((nc == 0) && (ic == 1))) {
      sendKey(KeyEvent.VK_ENTER);
    } else if ((nc == 4) && (ic == 1)) {
      sendKey(KeyEvent.VK_SPACE);
    } else if ((nc == 4) && (ic == 2)) {
      sendKey(KeyEvent.VK_DELETE);
    } else if ((nc == 4) && (ic == 3)) {
      sendKey(KeyEvent.VK_LEFT);
    } else if ((nc == 4) && (ic == 4)) {
      sendKey(KeyEvent.VK_RIGHT);
    } else if ((nc == 4) && (ic == 5)) {
      sendKey(KeyEvent.VK_DOWN);
    } else if ((nc == 4) && (ic == 6)) {
      sendKey(KeyEvent.VK_UP);
    } else if (keys[ic][nc].getText().equals("º")) {
    } else if (keys[ic][nc].getText().equals("1")) {
      sendKey(KeyEvent.VK_1);
    } else if (keys[ic][nc].getText().equals("2")) {
      sendKey(KeyEvent.VK_2);
    } else if (keys[ic][nc].getText().equals("3")) {
      sendKey(KeyEvent.VK_3);
    } else if (keys[ic][nc].getText().equals("4")) {
      sendKey(KeyEvent.VK_4);
    } else if (keys[ic][nc].getText().equals("5")) {
      sendKey(KeyEvent.VK_5);
    } else if (keys[ic][nc].getText().equals("6")) {
      sendKey(KeyEvent.VK_6);
    } else if (keys[ic][nc].getText().equals("7")) {
      sendKey(KeyEvent.VK_7);
    } else if (keys[ic][nc].getText().equals("8")) {
      sendKey(KeyEvent.VK_8);
    } else if (keys[ic][nc].getText().equals("9")) {
      sendKey(KeyEvent.VK_9);
    } else if (keys[ic][nc].getText().equals("0")) {
      sendKey(KeyEvent.VK_0);
    } else if (keys[ic][nc].getText().equals("'")) {
      sendKey(KeyEvent.VK_QUOTE);
    } else if (keys[ic][nc].getText().equals("¡")) {
      sendKey(KeyEvent.VK_INVERTED_EXCLAMATION_MARK);
    } else if (keys[ic][nc].getText().equals("`")) {
      sendKey(KeyEvent.VK_DEAD_GRAVE);
    } else if (keys[ic][nc].getText().equals("+")) {
      sendKey(KeyEvent.VK_PLUS);
    } else if (keys[ic][nc].getText().equals("ñ")) {

      if (fressa.robot != null) {
        fressa.robot.keyPress(KeyEvent.VK_ALT);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD6);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD6);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD4);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD4);
        fressa.robot.keyRelease(KeyEvent.VK_ALT);
      }
      isShiftFalse();

    } else if (keys[ic][nc].getText().equals("´")) {
      sendKey(KeyEvent.VK_DEAD_ACUTE);
    } else if (keys[ic][nc].getText().equals("ç")) {
      if (fressa.robot != null) {
        fressa.robot.keyPress(KeyEvent.VK_ALT);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD3);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD3);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD5);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD5);
        fressa.robot.keyRelease(KeyEvent.VK_ALT);
      }
      isShiftFalse();
    } else if (keys[ic][nc].getText().equals(",")) {
      sendKey(KeyEvent.VK_COMMA);
    } else if (keys[ic][nc].getText().equals(".")) {
      sendKey(keys[ic][nc].getText().codePointAt(0));
    } else if (keys[ic][nc].getText().equals("-")) {
      sendKey(KeyEvent.VK_MINUS);
    } else if (keys[ic][nc].getText().equals("<")) {
      sendKey(KeyEvent.VK_LESS);

    } else if (keys[ic][nc].getText().equals("ª")) {

    } else if (keys[ic][nc].getText().equals("!")) {
      sendShiftKey(KeyEvent.VK_1);
    } else if (keys[ic][nc].getText().equals("\"")) {
      sendShiftKey(KeyEvent.VK_2);
    } else if (keys[ic][nc].getText().equals("·")) {
      sendShiftKey(KeyEvent.VK_3);
    } else if (keys[ic][nc].getText().equals("$")) {
      sendShiftKey(KeyEvent.VK_4);
    } else if (keys[ic][nc].getText().equals("%")) {
      sendShiftKey(KeyEvent.VK_5);
    } else if (keys[ic][nc].getText().equals("&")) {
      sendShiftKey(KeyEvent.VK_6);
    } else if (keys[ic][nc].getText().equals("/")) {
      sendKey(KeyEvent.VK_DIVIDE);
    } else if (keys[ic][nc].getText().equals("(")) {
      sendShiftKey(KeyEvent.VK_8);
    } else if (keys[ic][nc].getText().equals(")")) {
      sendShiftKey(KeyEvent.VK_9);
    } else if (keys[ic][nc].getText().equals("=")) {
      sendShiftKey(KeyEvent.VK_0);
    } else if (keys[ic][nc].getText().equals("?")) {
      sendShiftKey(KeyEvent.VK_QUOTE);
    } else if (keys[ic][nc].getText().equals("¿")) {
      sendShiftKey(KeyEvent.VK_INVERTED_EXCLAMATION_MARK);
    } else if (keys[ic][nc].getText().equals("^")) {
      // sendKey(KeyEvent.VK_CIRCUMFLEX);
      sendShiftKey(KeyEvent.VK_DEAD_GRAVE);
    } else if (keys[ic][nc].getText().equals("*")) {
      sendShiftKey(KeyEvent.VK_PLUS);
    } else if (keys[ic][nc].getText().equals("¨")) {
      // sendShiftKey(KeyEvent.VK_DEAD_DIAERESIS);
      sendShiftKey(KeyEvent.VK_DEAD_ACUTE);
    } else if (keys[ic][nc].getText().equals("Ç")) {
      // sendShiftKey("ç".codePointAt(0));
      if (fressa.robot != null) {
        fressa.robot.keyPress(KeyEvent.VK_ALT);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD2);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD2);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD8);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD8);
        fressa.robot.keyRelease(KeyEvent.VK_ALT);
      }
      isShiftFalse();
    } else if (keys[ic][nc].getText().equals("Ñ")) {
      // sendShiftKey("ç".codePointAt(0));
      if (fressa.robot != null) {
        fressa.robot.keyPress(KeyEvent.VK_ALT);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD1);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD6);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD6);
        fressa.robot.keyPress(KeyEvent.VK_NUMPAD5);
        fressa.robot.keyRelease(KeyEvent.VK_NUMPAD5);
        fressa.robot.keyRelease(KeyEvent.VK_ALT);
      }
      isShiftFalse();
    } else if (keys[ic][nc].getText().equals(">")) {
      sendShiftKey(KeyEvent.VK_LESS);
    } else if (keys[ic][nc].getText().equals(";")) {
      sendShiftKey(KeyEvent.VK_COMMA);
    } else if (keys[ic][nc].getText().equals(":")) {
      sendShiftKey(".".codePointAt(0));
    } else if (keys[ic][nc].getText().equals("_")) {
      sendShiftKey(KeyEvent.VK_MINUS);
    } else if (keys[ic][nc].getText().equals("co")) {
      if (fressa.robot != null && fressa.actPanelRectangleMiddleBottom != null) {
        mouseMove(fressa.actPanelRectangleMiddleBottom.x, fressa.actPanelRectangleMiddleBottom.y);
        doClick();
      }
    } else if (keys[ic][nc].getText().equals("<>")) {
      fressa.currentZone = 0;
      fressa.place = 0;
    } else if (keys[ic][nc].getText().equals("BM")) {
      isShiftLock = !isShiftLock;
      if (isShift) {
        isShift = false;
        keys[iIsShift][nIsShift].setForeground(new java.awt.Color(0, 0, 0));
      }
      if (isShiftLock) {
        uppercase();
        // keys[ic][nc].setBackground(new java.awt.Color(127,127,127));
        keys[ic][nc].setForeground(new java.awt.Color(127, 127, 127));
      } else {
        lowercase();
        keys[ic][nc].setForeground(new java.awt.Color(0, 0, 0));
      }
    } else if (keys[ic][nc].getText().equals("shi")) {
      isShift = !isShift;
      iIsShift = ic;
      nIsShift = nc;
      if (isShift) {
        if (isShiftLock) {
          lowercase();
        } else {
          uppercase();
        }
        keys[ic][nc].setForeground(new java.awt.Color(127, 127, 127));
      } else {
        if (isShiftLock) {
          uppercase();
        } else {
          lowercase();
        }
        keys[ic][nc].setForeground(new java.awt.Color(0, 0, 0));
      }
    } else {
      if (!keys[ic][nc].getText().equals("")) {
        if (!keys[ic][nc].getText().equals(keys[ic][nc].getText().toUpperCase())) {
          sendKey(keys[ic][nc].getText().toUpperCase().codePointAt(0));
        } else {
          sendShiftKey(keys[ic][nc].getText().toUpperCase().codePointAt(0));
        }
      }
    }
    fressa.mustDisableScanTimer = false;
  }

  public void calcFrameSize() {
    cellWidth = panelPlaphoons.getWidth() / CELLS_X;
    cellHeight = panelPlaphoons.getHeight() / CELLS_Y;
    span = frameBorder + frameSpan;
    for (int i = 0; i < CELLS_X; i++) {
      for (int n = 0; n < CELLS_Y; n++) {
        keys[i][n].setVisible(false);
        keys[i][n].setBounds(frameSpan + cellWidth * i, frameSpan + cellHeight * n, cellWidth - frameSpan,
            cellHeight - frameSpan);
        keys[i][n].setVisible(true);
      }
    }
  }

  void lowercase() {
    int i = 1;
    int n = 0;
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("return.gif")));
    full[i][n] = true;
    i++;
    keys[i][n].setText("1");
    full[i][n] = true;
    i++;
    keys[i][n].setText("2");
    full[i][n] = true;
    i++;
    keys[i][n].setText("3");
    full[i][n] = true;
    i++;
    keys[i][n].setText("4");
    full[i][n] = true;
    i++;
    keys[i][n].setText("5");
    full[i][n] = true;
    i++;
    keys[i][n].setText("6");
    full[i][n] = true;
    i++;
    keys[i][n].setText("7");
    full[i][n] = true;
    i++;
    keys[i][n].setText("8");
    full[i][n] = true;
    i++;
    keys[i][n].setText("9");
    full[i][n] = true;
    i++;
    keys[i][n].setText("0");
    full[i][n] = true;
    i++;
    keys[i][n].setText("'");
    full[i][n] = true;
    i++;
    keys[i][n].setText("¡");
    full[i][n] = true;
    i++;
    // keys[i][n].setText("bac");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("bacspace.gif")));
    full[i][n] = true;
    i++;
    n++;
    i = 1;
    keys[i][n].setText("tab");
    full[i][n] = true;
    i++;
    keys[i][n].setText("q");
    full[i][n] = true;
    i++;
    keys[i][n].setText("w");
    full[i][n] = true;
    i++;
    keys[i][n].setText("e");
    full[i][n] = true;
    i++;
    keys[i][n].setText("r");
    full[i][n] = true;
    i++;
    keys[i][n].setText("t");
    full[i][n] = true;
    i++;
    keys[i][n].setText("y");
    full[i][n] = true;
    i++;
    keys[i][n].setText("u");
    full[i][n] = true;
    i++;
    keys[i][n].setText("i");
    full[i][n] = true;
    i++;
    keys[i][n].setText("o");
    full[i][n] = true;
    i++;
    keys[i][n].setText("p");
    full[i][n] = true;
    i++;
    keys[i][n].setText("`");
    full[i][n] = true;
    i++;
    keys[i][n].setText("+");
    full[i][n] = true;
    i++;
    // keys[i][n].setText("Ret");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("return.gif")));
    full[i][n] = true;
    i++;
    n++;
    i = 1;
    keys[i][n].setText("BM");
    full[i][n] = true;
    i++;
    keys[i][n].setText("a");
    full[i][n] = true;
    i++;
    keys[i][n].setText("s");
    full[i][n] = true;
    i++;
    keys[i][n].setText("d");
    full[i][n] = true;
    i++;
    keys[i][n].setText("f");
    full[i][n] = true;
    i++;
    keys[i][n].setText("g");
    full[i][n] = true;
    i++;
    keys[i][n].setText("h");
    full[i][n] = true;
    i++;
    keys[i][n].setText("j");
    full[i][n] = true;
    i++;
    keys[i][n].setText("k");
    full[i][n] = true;
    i++;
    keys[i][n].setText("l");
    full[i][n] = true;
    i++;
    keys[i][n].setText("ñ");
    full[i][n] = true;
    // keys[i][n].setText("");
    i++;
    keys[i][n].setText("´");
    full[i][n] = true;
    i++;
    keys[i][n].setText("ç");
    full[i][n] = true;
    // keys[i][n].setText("");
    i++;
    // keys[i][n].setText("Ret");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("return.gif")));
    full[i][n] = true;
    i++;
    n++;
    i = 1;
    keys[i][n].setText("shi");
    full[i][n] = true;
    i++;
    keys[i][n].setText("<");
    full[i][n] = true;
    i++;
    keys[i][n].setText("z");
    full[i][n] = true;
    i++;
    keys[i][n].setText("x");
    full[i][n] = true;
    i++;
    keys[i][n].setText("c");
    full[i][n] = true;
    i++;
    keys[i][n].setText("v");
    full[i][n] = true;
    i++;
    keys[i][n].setText("b");
    full[i][n] = true;
    i++;
    keys[i][n].setText("n");
    full[i][n] = true;
    i++;
    keys[i][n].setText("m");
    full[i][n] = true;
    i++;
    keys[i][n].setText(",");
    full[i][n] = true;
    i++;
    keys[i][n].setText(".");
    full[i][n] = true;
    i++;
    keys[i][n].setText("-");
    full[i][n] = true;
    i++;
    // keys[i][n].setText("shi");
    i++;
    // keys[i][n].setText("A");
    n++;
    i = 1;
    // keys[i][n].setText("esp");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("espai.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("sup");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("delete.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("<-");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("esquerra.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("->");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("dreta.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("|b");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("baixa.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("|p");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("puja.gif")));
    full[i][n] = true;
    i++;
    keys[i][n].setText("<>");
    // keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource(
    // "canviactivitatfletxes.gif" )));
    full[i][n] = true;
    Font f = keys[i][n].getFont();
    keys[i][n].setFont(new Font(f.getFontName(), Font.BOLD, f.getSize()));
    i++;
    keys[i][n].setText("co");
    full[i][n] = true;
    f = keys[i][n].getFont();
    keys[i][n].setFont(new Font(f.getFontName(), Font.BOLD, f.getSize()));
    i++;
    // keys[i][n].setText(".->");
    keys[i][n].setText("");
    f = keys[i][n].getFont();
    keys[i][n].setFont(new Font(f.getFontName(), Font.BOLD, f.getSize()));
    i++;
  }

  void uppercase() {
    int i = 1;
    int n = 0;
    // keys[i][n].setText("ª");
    // keys[i][n].setText("");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("return.gif")));
    full[i][n] = true;
    i++;
    keys[i][n].setText("!");
    full[i][n] = true;
    i++;
    keys[i][n].setText("\"");
    full[i][n] = true;
    i++;
    keys[i][n].setText("·");
    full[i][n] = true;
    i++;
    keys[i][n].setText("$");
    full[i][n] = true;
    i++;
    keys[i][n].setText("%");
    full[i][n] = true;
    i++;
    keys[i][n].setText("&");
    full[i][n] = true;
    i++;
    keys[i][n].setText("/");
    full[i][n] = true;
    i++;
    keys[i][n].setText("(");
    full[i][n] = true;
    i++;
    keys[i][n].setText(")");
    full[i][n] = true;
    i++;
    keys[i][n].setText("=");
    full[i][n] = true;
    i++;
    keys[i][n].setText("?");
    full[i][n] = true;
    i++;
    keys[i][n].setText("¿");
    full[i][n] = true;
    i++;
    // keys[i][n].setText("bac");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("bacspace.gif")));
    full[i][n] = true;
    i++;
    n++;
    i = 1;
    keys[i][n].setText("tab");
    full[i][n] = true;
    i++;
    keys[i][n].setText("Q");
    full[i][n] = true;
    i++;
    keys[i][n].setText("W");
    full[i][n] = true;
    i++;
    keys[i][n].setText("E");
    full[i][n] = true;
    i++;
    keys[i][n].setText("R");
    full[i][n] = true;
    i++;
    keys[i][n].setText("T");
    full[i][n] = true;
    i++;
    keys[i][n].setText("Y");
    full[i][n] = true;
    i++;
    keys[i][n].setText("U");
    full[i][n] = true;
    i++;
    keys[i][n].setText("I");
    full[i][n] = true;
    i++;
    keys[i][n].setText("O");
    full[i][n] = true;
    i++;
    keys[i][n].setText("P");
    full[i][n] = true;
    i++;
    keys[i][n].setText("^");
    full[i][n] = true;
    i++;
    keys[i][n].setText("*");
    full[i][n] = true;
    i++;
    // keys[i][n].setText("Ret");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("return.gif")));
    full[i][n] = true;
    i++;
    n++;
    i = 1;
    keys[i][n].setText("BM");
    full[i][n] = true;
    i++;
    keys[i][n].setText("A");
    full[i][n] = true;
    i++;
    keys[i][n].setText("S");
    full[i][n] = true;
    i++;
    keys[i][n].setText("D");
    full[i][n] = true;
    i++;
    keys[i][n].setText("F");
    full[i][n] = true;
    i++;
    keys[i][n].setText("G");
    full[i][n] = true;
    i++;
    keys[i][n].setText("H");
    full[i][n] = true;
    i++;
    keys[i][n].setText("J");
    full[i][n] = true;
    i++;
    keys[i][n].setText("K");
    full[i][n] = true;
    i++;
    keys[i][n].setText("L");
    full[i][n] = true;
    i++;
    keys[i][n].setText("Ñ");
    full[i][n] = true;
    // keys[i][n].setText("");
    i++;
    keys[i][n].setText("¨");
    full[i][n] = true;
    i++;
    keys[i][n].setText("Ç");
    full[i][n] = true;
    // keys[i][n].setText("");
    i++;
    // keys[i][n].setText("Ret");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("return.gif")));
    full[i][n] = true;
    i++;
    n++;
    i = 1;
    keys[i][n].setText("shi");
    full[i][n] = true;
    i++;
    keys[i][n].setText(">");
    full[i][n] = true;
    i++;
    keys[i][n].setText("Z");
    full[i][n] = true;
    i++;
    keys[i][n].setText("X");
    full[i][n] = true;
    i++;
    keys[i][n].setText("C");
    full[i][n] = true;
    i++;
    keys[i][n].setText("V");
    full[i][n] = true;
    i++;
    keys[i][n].setText("B");
    full[i][n] = true;
    i++;
    keys[i][n].setText("N");
    full[i][n] = true;
    i++;
    keys[i][n].setText("M");
    full[i][n] = true;
    i++;
    keys[i][n].setText(";");
    full[i][n] = true;
    i++;
    keys[i][n].setText(":");
    full[i][n] = true;
    i++;
    keys[i][n].setText("_");
    full[i][n] = true;
    i++;
    // keys[i][n].setText("shi");
    i++;
    n++;
    i = 1;
    // keys[i][n].setText("esp");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("espai.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("sup");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("delete.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("<-");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("esquerra.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("->");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("dreta.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("|b");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("baixa.gif")));
    full[i][n] = true;
    i++;
    // keys[i][n].setText("|p");
    keys[i][n].setIcon(new javax.swing.ImageIcon(this.getClass().getResource("puja.gif")));
    full[i][n] = true;
    i++;
    keys[i][n].setText("<>");
    full[i][n] = true;
    Font f = keys[i][n].getFont();
    keys[i][n].setFont(boldFont);
    i++;
    keys[i][n].setText("co");
    full[i][n] = true;
    f = keys[i][n].getFont();
    keys[i][n].setFont(boldFont);
    i++;
    // keys[i][n].setText(".->");
    keys[i][n].setText("");
    f = keys[i][n].getFont();
    keys[i][n].setFont(boldFont);
    i++;
  }
}
