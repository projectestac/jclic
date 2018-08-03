/*
 * File    : Html.java
 * Created : 05-dec-2001 12:28
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
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
import java.io.PrintStream;
import java.util.StringTokenizer;

public class Html extends Object {

  public static final String SP = " ";
  public static final String NBSP = "&nbsp;";
  public static final String BR = "\n<BR>\n";
  public static final String P = "\n<P>\n";
  public static final String Q = "\"";
  public static final int LEFT = 0, CENTER = 1, RIGHT = 2;
  public static final String[] ALIGN = { "", "ALIGN=\"center\"", "ALIGN=\"right\"" };

  protected StringBuilder sb;

  public Html(int bufSize) {
    sb = new StringBuilder(bufSize);
  }

  public Html(StringBuilder sb) {
    this.sb = sb;
  }

  @Override
  public String toString() {
    return sb.substring(0);
  }

  public Html append(String str) {
    sb.append(str == null ? "" : str);
    return this;
  }

  public Html appendParagraphs(String source) {
    StringTokenizer st = new StringTokenizer(source, "\n");
    boolean first = true;
    while (st.hasMoreTokens()) {
      if (!first)
        sb.append(BR);
      else
        first = false;
      sb.append(st.nextToken());
    }
    return this;
  }

  public Html mailTo(String mail, boolean parenthesis) {
    if (parenthesis)
      sb.append("(");
    sb.append("<A HREF=\"mailto:").append(mail).append("\">").append(mail).append("</A>");
    if (parenthesis)
      sb.append(")");
    return this;
  }

  public Html linkTo(String url, String text) {
    sb.append("<A HREF=\"").append(url).append("\">");
    sb.append(text == null ? url : text);
    sb.append("</A>");
    return this;
  }

  public Html bold(String text) {
    sb.append("<B>").append(text).append("</B>");
    return this;
  }

  public Html td(boolean init) {
    sb.append(init ? "<TD VALIGN=\"top\">" : "</TD>\n");
    return this;
  }

  public Html td(String text) {
    return td(text, false);
  }

  public Html td(String text, int align, boolean bold, String tdParams) {
    sb.append("<TD VALIGN=\"top\" ");
    if (align > LEFT)
      sb.append(SP).append(ALIGN[align]);
    if (tdParams != null)
      sb.append(SP).append(tdParams);
    sb.append(">");
    if (bold)
      bold(text);
    else
      sb.append(text);
    sb.append("</TD>\n");
    return this;
  }

  public Html td(String text, boolean bld) {
    sb.append("<TD VALIGN=\"top\">");
    if (bld)
      bold(text);
    else
      sb.append(text);
    sb.append("</TD>\n");
    return this;
  }

  public Html tr(boolean init) {
    return append(init ? "<TR>" : "</TR>\n");
  }

  public Html br() {
    return append(BR);
  }

  public Html p() {
    return append(P);
  }

  public Html sp() {
    return append(SP);
  }

  public Html nbsp() {
    return append(NBSP);
  }

  public Html doubleCell(String tx1, boolean b1, String tx2, boolean b2) {
    tr(true);
    td(true);
    if (b1)
      sb.append("<B>");
    appendParagraphs(tx1);
    if (b1)
      sb.append("</B>");
    td(false);
    td(true);
    if (b2)
      sb.append("<B>");
    appendParagraphs(tx2);
    if (b2)
      sb.append("</B>");
    td(false);
    tr(false);
    return this;
  }

  public static String table(String content, String width, int border, int cellPadding, int cellSpacing, String style,
      boolean simpleCell) {
    int v = content != null ? content.length() : 100;
    StringBuilder sbs = new StringBuilder(v + 200);
    sbs.append("<TABLE ");
    if (width != null)
      sbs.append(" WIDTH=\"").append(width).append(Q);
    if (border >= 0)
      sbs.append(" BORDER=\"").append(border).append(Q);
    if (cellPadding >= 0)
      sbs.append(" CELLPADDING=\"").append(cellPadding).append(Q);
    if (cellSpacing >= 0)
      sbs.append(" CELLSPACING=\"").append(cellSpacing).append(Q);
    if (style != null)
      sbs.append(" STYLE=\"").append(style).append(Q);
    sbs.append(">\n");
    if (simpleCell)
      sbs.append("<TR><TD>");
    sbs.append(content);
    if (simpleCell)
      sbs.append("</TD></TR>");
    sbs.append("\n</TABLE>");
    return sbs.substring(0);
  }

  public static String getHtmlDoc(String content, String bgColor) {
    int v = content != null ? content.length() : 100;
    StringBuilder sbs = new StringBuilder(v + 100);
    sbs.append("<HTML>\n");
    sbs.append("<BODY");
    if (bgColor != null)
      sbs.append(" BGCOLOR=\"#").append(bgColor).append(Q);
    sbs.append(">\n");
    sbs.append(content);
    sbs.append("</BODY></HTML>");
    return sbs.substring(0);
  }

  public static String quote(String txt) {
    StringBuilder sbs = new StringBuilder(txt.length() * 3);
    sbs.append("'");
    sbs.append(edu.xtec.util.StrUtils.replace(
        edu.xtec.util.StrUtils.replace(edu.xtec.util.StrUtils.replace(txt, "'", "&#39;"), "\"", "&quot;"), "`",
        "&rsquo;"));
    sbs.append("'");
    return sbs.substring(0);
  }

  public static final String validChars = " -_.*0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  public static String encode(String src) {
    if (src == null || src.length() < 1)
      return src;

    int len = src.length();
    StringBuilder sbs = new StringBuilder(len * 2);
    for (int i = 0; i < len; i++) {
      char ch = src.charAt(i);
      if (validChars.indexOf(ch) >= 0)
        sbs.append(ch);
      else if (ch < 256)
        sbs.append("%").append(Integer.toHexString(ch));
      else
        sbs.append("%26%23").append(Integer.toHexString(ch)).append("%3B");
    }
    return sbs.substring(0);
  }

  public static String decode(String src) {
    if (src == null || src.length() < 1)
      return src;
    int len = src.length();
    StringBuilder sbs = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char ch = src.charAt(i);
      if (ch == '%') {
        sbs.append((char) Integer.parseInt(src.substring(i + 1, i + 3), 16));
        i += 2;
      } else
        sbs.append(ch);
    }
    String s = sbs.substring(0);
    len = s.length();
    sbs.setLength(0);
    int j = 0;
    for (int i = 0; i < len; i++) {
      char ch = s.charAt(i);
      if (ch == '&' && (j = s.indexOf(';', i)) > i + 2) {
        String v = s.substring(i + 1, j).toLowerCase();
        if (v.length() > 2 && v.charAt(0) == '#') {
          int k;
          if (v.charAt(1) == 'x')
            k = Integer.parseInt(v.substring(2), 16);
          else
            k = Integer.parseInt(v.substring(1));
          sbs.append((char) k);
        } else
          sbs.append('?');
        i = j;
      } else
        sbs.append(ch);
    }
    return sbs.substring(0);
  }

  // to be deprecated!!
  public static void processParamsLine(String txt, java.util.Map<String, String> map, char sep, char equalSign) {
    if (txt != null && txt.length() > 0 && map != null) {
      StringTokenizer st = new StringTokenizer(txt, String.valueOf(sep));
      while (st.hasMoreTokens()) {
        String s = st.nextToken().trim();
        String key, value = null;
        int k = s.indexOf(equalSign);
        if (k > 0) {
          key = decode(s.substring(0, k).replace('+', ' '));
          if (k < s.length() - 1)
            value = decode(s.substring(k + 1).replace('+', ' '));
        } else
          key = decode(s.replace('+', ' '));
        map.put(key, value);
      }
    }
  }

  public static void writeScriptConfirm(PrintStream pOut) {
    pOut.println("<SCRIPT language=\"javascript\">");
    pOut.println("function confirmAction(msg){");
    pOut.println("   var agree=confirm(msg);");
    pOut.println("   if (agree)");
    pOut.println("     return true;");
    pOut.println("   else");
    pOut.println("     return false;");
    pOut.println("}");
    pOut.println("</SCRIPT>");
  }
}
