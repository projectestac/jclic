/*
 * File    : Encryption.java
 * Created : 03-jul-2001 09:51
 * By      : allastar
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

/**
 * Utilities to encrypt and decrypt strings using simple methods, just to avoid
 * write passwords in plain text in data and configuration files. Do not use it
 * as a secure cryptographic system!
 *
 * @author Albert Llastarri (allastar@xtec.cat)
 * @version 13.08.08
 */
public final class Encryption {

  private static final String BLANK = "___blank___##";

  public static String Encrypt(String txt) {
    if (txt == null || txt.length() == 0)
      txt = BLANK;

    String result = null;
    try {
      result = codify(txt);
    } catch (Exception ex) {
      System.err.println("Error encripting text!");
    }
    return result;
  }

  public static String Decrypt(String txt) {

    if (txt == null || txt.length() == 0)
      return null;

    String s = decodify(txt);
    if (BLANK.equals(s))
      s = new String();

    return s;
  }

  private static char hexCharArrayToChar(char[] cA, int fromIndex) {
    char[] hex = new char[4];
    int n = 0;
    for (int i = 0; i <= 3; i++) {
      int j = Character.digit(cA[fromIndex + i], 16);
      n = (n * 16) + j;
    }
    return (char) n;
  }

  private static char[] charToHexCharArray(char c) {
    char[] hex = new char[4];
    int j = (int) c;
    for (int i = 3; i >= 0; i--) {
      hex[i] = Character.forDigit(j % 16, 16);
      j /= 16;
    }
    return hex;
  }

  private static char[] intToHexCharArray(int c) {
    char[] hex = new char[2];
    int j = (int) c;
    for (int i = 1; i >= 0; i--) {
      hex[i] = Character.forDigit(j % 16, 16);
      j /= 16;
    }
    return hex;
  }

  private static int hexCharArrayToInt(char[] cA, int fromIndex) {
    int n = 0;
    for (int i = 0; i <= 1; i++) {
      int j = Character.digit(cA[fromIndex + i], 16);
      n = (n * 16) + j;
    }
    return n;
  }

  private static StringBuilder compressZeros(char[] cA) {
    int total = 0;
    StringBuilder sb = new StringBuilder(cA.length);
    int[] zeros = new int[(cA.length + 7) / 8]; // it will be better to use bytes, but
    // then it takes the first bit as a sign
    int j;
    for (j = 0; total < cA.length; j++) {
      char b = 0;
      for (int i = 0; i <= 7; i++) {
        b <<= 1;
        if (total < cA.length) {
          if (cA[total] == '0')
            b += 1;
          else
            sb.append(cA[total]);
        }
        total++;
      }
      zeros[j] = (int) b;
    }
    return codifyZerosField(zeros, j).append(sb.substring(0));
  }

  private static StringBuilder codifyZerosField(int[] zeros, int length) {
    String hexZeros = codifyToHex(zeros, length); // hexZeros size is always odd
    StringBuilder codified = new StringBuilder();
    if (hexZeros.length() > 1) {
      char c1 = hexZeros.charAt(0);
      char c2 = hexZeros.charAt(1);
      int num = 1;
      int currentChar = 2;
      while (currentChar < hexZeros.length()) {
        if (c1 == hexZeros.charAt(currentChar) && c2 == hexZeros.charAt(currentChar + 1) && num < 32)
          num++;
        else { // New sequence
          codified.append(Character.forDigit(num, 32));
          codified.append(c1);
          codified.append(c2);
          num = 1;
          c1 = hexZeros.charAt(currentChar);
          c2 = hexZeros.charAt(currentChar + 1);
        }
        currentChar += 2;
      }
      codified.append(Character.forDigit(num, 32));
      codified.append(c1);
      codified.append(c2);
      codified.append("0");
    }
    return codified;
  }

  private static String decodifyZerosField(char[] cA) {
    StringBuilder sb = new StringBuilder();
    int num = Character.digit(cA[0], 32);
    int k = 0;
    int i;
    for (i = 0; num != 0; i++) {
      while (num > 0) {
        sb.append(cA[(i * 3) + 1]);
        sb.append(cA[(i * 3) + 2]);
        num--;
        k++;
      }
      if (cA.length > ((i * 3) + 3))
        num = Character.digit(cA[(i * 3) + 3], 32);
      else
        num = 0;
    }
    for (int j = (i * 3) + 1; j < cA.length; j++)
      sb.append(cA[j]);
    char c = Character.forDigit(k, 32);
    return c + sb.toString();
  }

  private static StringBuilder decompressZeros(char[] cA) {
    cA = decodifyZerosField(cA).toCharArray();
    int numBytesZeros = Character.digit(cA[0], 32);
    int iniNoZeros = (numBytesZeros * 2) + 1;
    boolean bFi = false;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numBytesZeros && !bFi; i++) {
      int zeros = hexCharArrayToInt(cA, 1 + (i * 2));
      String s = Integer.toBinaryString(zeros);
      while (s.length() < 8)
        s = "0" + s;
      for (int j = 0; j <= 7 && !bFi; j++) {
        if (s.charAt(j) == '1') {
          sb.append('0');
        } else if (iniNoZeros < cA.length) {
          sb.append(cA[iniNoZeros]);
          iniNoZeros++;
        } else
          bFi = true;
      }
    }
    return sb;
  }

  private static String codifyToHex(int[] bA, int length) {
    // char [] cA=s.toCharArray();
    char[] cA = new char[length * 2];
    int j = 0;
    for (int i = 0; i < length; i++) {
      char[] hex = intToHexCharArray(bA[i]);
      for (int k = 0; k < 2; k++) {
        cA[j] = hex[k];
        j++;
      }
    }
    String st = new String(cA);
    return st;
  }

  private static char[] codifyToHex(String s) {
    char[] cA = new char[s.length() * 4];
    int j = 0;
    for (int i = 0; i < s.length(); i++) {
      char[] hex = charToHexCharArray(s.charAt(i));
      for (int k = 0; k < 4; k++) {
        cA[j] = hex[k];
        j++;
      }
    }
    return cA;
  }

  private static String decodifyFromHex(StringBuilder sb1) {
    StringBuilder sb = new StringBuilder();
    char[] cA = sb1.toString().toCharArray();
    int j = 0;
    for (int i = 0; j < sb1.length(); i++) {
      char c = hexCharArrayToChar(cA, j);
      sb.append(c);
      j += 4;
    }
    return sb.toString();
  }

  private static char[] changeOrder(StringBuilder s) {
    int m = 0;
    int n = s.length() - 1;
    char[] cA = new char[s.length()]; // =s.toCharArray();
    for (int i = 0; i < s.length(); i++) {
      if ((i % 2) == 0) {
        cA[m] = s.charAt(i);
        m++;
      } else {
        cA[n] = s.charAt(i);
        n--;
      }
    }
    return cA;
  }

  private static char[] unchangeOrder(String s) {
    int m = 0;
    int n = s.length() - 1;
    char[] cA = new char[s.length()];
    for (int i = 0; i < s.length(); i++) {
      if ((i % 2) == 0) {
        cA[i] = s.charAt(m);
        m++;
      } else {
        cA[i] = s.charAt(n);
        n--;
      }
    }
    return cA;
  }

  static class TooLargePasswordException extends Exception {
    @Override
    public String toString() {
      return "Password mustn't contain over 24 characters!!!";
    }
  }

  private static String codify(String word) throws TooLargePasswordException {
    if (word.length() > 24)
      throw new TooLargePasswordException();
    return new String(changeOrder(compressZeros(codifyToHex(word))));
  }

  private static String decodify(String word) {
    try {
      return decodifyFromHex(decompressZeros(unchangeOrder(word)));
    } catch (Exception e) { // The supplied word was not codified using this system
      return "";
    }
  }
}
