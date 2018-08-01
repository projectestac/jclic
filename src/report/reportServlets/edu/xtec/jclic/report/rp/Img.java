/*
 * File    : Img.java
 * Created : 03-feb-2003 16:46
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

package edu.xtec.jclic.report.rp;

import static edu.xtec.servlet.RequestProcessor.CONTENT_TYPE;

import edu.xtec.jclic.report.ActivityData;
import edu.xtec.jclic.report.SessionData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class Img extends BasicReport {

  public static final String URL = "img";

  public static final String TYPE = "type", HEADER = "header", GRAPH = "graph", TEXT = "text", DIST = "dist",
      WIDTH = "w", HEIGHT = "h";
  public static final String USER_GRAPH = "userGraph", GROUP_GRAPH = "groupGraph", PROJECT_GRAPH = "projectGraph";
  public static int DEFAULT_WIDTH = 0, DIST_WIDTH, DEFAULT_HEIGHT, DEFAULT_HEADER_HEIGHT, MRG;
  public static Color BG_COLOR, TEXT_COLOR, BORDER_COLOR, HEADER_BG_COLOR, HEADER_TEXT_COLOR, V1_COLOR, V2_COLOR,
      DIST_COLOR, ALERT_COLOR;
  public static Stroke THIN_STROKE, BOLD_STROKE;
  public static Font STD_FONT, BOLD_FONT, ALERT_FONT;
  public static int MARGE_X, MARGE_Y, NUM_DIVISIONS_Y, MAX_COLS;
  public static int M_X_DIST, M_Y_DIST;
  boolean withHeader, isDist;
  int width, height;
  String titleKey;

  static {
    if (DEFAULT_WIDTH == 0) {
      try {
        loadSettings(null);
      } catch (Exception ex) {
      }
    }
  }

  public Img() throws Exception {
    super();
  }

  public String getTitle(ResourceBundle bundle) {
    return "";
  }

  public String getUrl() {
    return URL;
  }

  @Override
  public boolean noCache() {
    return false;
  }

  public static void loadSettings(String file) throws Exception {
    loadProperties(file);
    DEFAULT_WIDTH = Integer.parseInt(prop.getProperty(GRAPH_WIDTH, "440"));
    DIST_WIDTH = Integer.parseInt(prop.getProperty(GRAPH_DIST_WIDTH, "192"));
    DEFAULT_HEIGHT = Integer.parseInt(prop.getProperty(GRAPH_HEIGHT, "155"));
    DEFAULT_HEADER_HEIGHT = Integer.parseInt(prop.getProperty(GRAPH_HEADER_HEIGHT, "25"));
    MRG = Integer.parseInt(prop.getProperty(GRAPH_MARGIN, "8"));
    BG_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_BG, "008080"), 16));
    TEXT_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_TEXT, "FFFFFF"), 16));
    HEADER_BG_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_HEADER_BG, "008080"), 16));
    HEADER_TEXT_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_HEADER_TEXT, "FFFFFF"), 16));
    BORDER_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_BORDER, "000000"), 16));
    V1_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_V1, "00FF00"), 16));
    V2_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_V2, "0000FF"), 16));
    DIST_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_DIST, "0000FF"), 16));
    ALERT_COLOR = new Color(Integer.parseInt(prop.getProperty(GRAPH_COLOR_ALERT, "FF0000"), 16));
    BOLD_STROKE = new BasicStroke(Float.parseFloat(prop.getProperty(GRAPH_STROKE_WIDTH, "3.0")));
    THIN_STROKE = new BasicStroke(1.0f);

    try {
      String fontFamily = prop.getProperty(GRAPH_FONT_FAMILY, "Dialog");
      int fontSize = Integer.parseInt(prop.getProperty(GRAPH_FONT_SIZE, "11"));
      STD_FONT = new Font(fontFamily, Font.PLAIN, fontSize);
      BOLD_FONT = new Font(fontFamily, Font.BOLD, fontSize);
      ALERT_FONT = new Font(fontFamily, Font.BOLD, 24);
    } catch (Exception ex) {
      // no fonts!!!
    }

    MARGE_X = Integer.parseInt(prop.getProperty(GRAPH_MARGIN_X, "50"));
    MARGE_Y = Integer.parseInt(prop.getProperty(GRAPH_MARGIN_Y, "20"));
    NUM_DIVISIONS_Y = Integer.parseInt(prop.getProperty(GRAPH_DIV_Y, "4"));
    MAX_COLS = Integer.parseInt(prop.getProperty(GRAPH_MAX_COLS, "10"));
    M_X_DIST = Integer.parseInt(prop.getProperty(GRAPH_MARGIN_DIST_X, "15"));
    M_Y_DIST = Integer.parseInt(prop.getProperty(GRAPH_MARGIN_DIST_Y, "20"));
  }

  @Override
  public boolean init() throws Exception {

    if (!super.init())
      return false;

    withHeader = getBoolParam(HEADER, TRUE);
    isDist = getBoolParam(DIST, TRUE);
    type = UNKNOWN;
    String s = getParam(TYPE);
    if (USER_GRAPH.equals(s)) {
      type = USR;
      titleKey = "report_user_evolution";
    } else if (GROUP_GRAPH.equals(s)) {
      type = GRP;
      titleKey = "report_group_evolution";
    } else if (PROJECT_GRAPH.equals(s)) {
      type = PRJ;
      titleKey = "report_project_evolution";
    } else {
      type = UNKNOWN;
      errCode = HTTP_BAD_REQUEST;
      throw new Exception();
    }

    if (isDist)
      titleKey = "report_result_distribution";

    width = getIntParam(WIDTH, isDist ? DIST_WIDTH : DEFAULT_WIDTH);
    height = getIntParam(HEIGHT, DEFAULT_HEIGHT);

    if (width <= 0 || height <= 0) {
      System.err.println("EP!!");
    }

    return true;
  };

  @Override
  public void header(List<String[]> v) {
    v.add(new String[] { CONTENT_TYPE, "image/png" });
  }

  @Override
  public boolean usesWriter() {
    return false;
  }

  @Override
  public void process(java.io.OutputStream out) throws Exception {

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) bi.getGraphics();

    int h = withHeader ? DEFAULT_HEADER_HEIGHT : 0;

    Rectangle allRect = new Rectangle(0, 0, width, height);
    Rectangle hr = new Rectangle(0, 0, width, h);
    Rectangle gr = new Rectangle(0, h, width, height - h);

    g2.setColor(BG_COLOR);
    g2.fill(allRect);
    if (withHeader) {
      g2.setColor(HEADER_BG_COLOR);
      g2.fill(hr);
    }
    g2.setColor(BORDER_COLOR);
    g2.draw(new Rectangle(0, 0, width - 1, height - 1));

    if (withHeader) {
      drawHeader(g2, hr);
      g2.setColor(BORDER_COLOR);
      g2.drawLine(0, h, width, h);
    }

    if (isDist)
      dibuixaDistribucio(g2, getSessionList(), gr);
    else
      dibuixaImatge(g2, getSessionList(), gr);

    g2.dispose();
    ImageIO.write(bi, "png", out);
  }

  protected void drawHeader(Graphics2D g2, Rectangle box) throws Exception {

    FontMetrics fm = g2.getFontMetrics(STD_FONT);

    int fh = fm.getHeight();
    int rs = fh / 2;
    int dy = (box.height - fh) / 2;
    int dyr = (box.height - rs) / 2;

    if (!isDist) {
      String var1 = bundle.getString("report_global_precision");
      String var2 = bundle.getString("report_solved_activities");
      int lvar1 = fm.stringWidth(var1);
      int lvar2 = fm.stringWidth(var2);
      Rectangle r1 = new Rectangle(box.x + box.width - MRG - lvar2 - 6 - rs - 2 * MRG - lvar1 - 6 - fh, box.y + dyr, rs,
          rs);
      Rectangle r2 = new Rectangle(box.x + box.width - MRG - lvar2 - 6 - rs, box.y + dyr, rs, rs);
      g2.setColor(V1_COLOR);
      g2.fill(r1);
      g2.setColor(V2_COLOR);
      g2.fill(r2);
      g2.setColor(BORDER_COLOR);
      g2.draw(r1);
      g2.draw(r2);
      g2.setFont(STD_FONT);
      g2.setColor(HEADER_TEXT_COLOR);
      g2.drawString(var1, box.x + r1.x + rs + 6, box.y + dy + fm.getAscent());
      g2.drawString(var2, box.x + r2.x + rs + 6, box.y + dy + fm.getAscent());
    }

    g2.setColor(HEADER_TEXT_COLOR);
    g2.setFont(BOLD_FONT);
    g2.drawString(bundle.getString(titleKey), box.x + MRG, box.y + dy + fm.getAscent());
  }

  public void dibuixaImatge(Graphics2D g2, List<SessionData> v, Rectangle box) throws Exception {

    g2.setColor(TEXT_COLOR);
    g2.setStroke(THIN_STROKE);
    int gWidth = box.width - (2 * MARGE_X);
    int gHeight = box.height - (2 * MARGE_Y);
    g2.drawRect(box.x + MARGE_X, box.y + MARGE_Y, gWidth, gHeight);
    if (v.size() > 0) {
      float lDivY = gHeight / NUM_DIVISIONS_Y;
      for (int i = 1; i < NUM_DIVISIONS_Y; i++) { // Posem les linies horitzontals
        int y = (int) (MARGE_Y + (i * lDivY));
        g2.drawLine(box.x + MARGE_X, box.y + y, box.x + MARGE_X + gWidth, box.y + y);
      }
      g2.setFont(STD_FONT);
      FontMetrics fm = g2.getFontMetrics();
      for (int i = 0; i <= NUM_DIVISIONS_Y; i++) { // Posem els valors horitzontals en %
        String num = Integer.toString((100 - (i * (100 / NUM_DIVISIONS_Y))));
        int l = fm.stringWidth(num);
        g2.drawString(num, box.x + MARGE_X - 5 - l, box.y + MARGE_Y + (int) (lDivY * i) + 3);
      }

      int numArgs = v.size();
      if (numArgs == 1) { // Hem de replicar el punt
        v.add(v.get(0));
        numArgs++;
      }
      int columnesArgs = numArgs - 1;
      int numColumnesX = Math.min(MAX_COLS, columnesArgs); // nombre de columnes que s'acabara mostrant.
      float longColumnaX = gWidth / numColumnesX; // longitud horitzontal de cadascuna de les columnes
      float increment = (float) columnesArgs / (float) numColumnesX;
      int[] puntsDePas = new int[numColumnesX + 2];
      int i = 0;
      for (float f = 0.0f; f <= columnesArgs + 1; f += increment)
        puntsDePas[i++] = Math.round(f);

      int[] abscises = new int[numArgs];
      int j = 0; // j indicara quantes columnes completes hem fet
      for (int k = 0; k <= columnesArgs; k++) {
        if (puntsDePas[j] == k) { // mostrem punt i columna
          abscises[k] = (int) (MARGE_X + (j++ * longColumnaX));
        } else {
          int properPunt = puntsDePas[j];
          int anteriorPunt = puntsDePas[j - 1];
          float x = (k - anteriorPunt) * (longColumnaX / (properPunt - anteriorPunt));
          abscises[k] = (int) (MARGE_X + ((j - 1) * longColumnaX) + x);
        }
      }
      j = 0;
      Date dataFinal = null;
      boolean validat = true; // De moment mostrem les dades, quan trobem una a la que no te acces pararem.
      for (i = 0; i < columnesArgs && validat; i++) {
        SessionData sd1 = v.get(i);
        SessionData sd2 = v.get(i + 1);

        if (puntsDePas[j] == i) { // mostrem punt i columna
          g2.setColor(TEXT_COLOR);
          g2.setStroke(THIN_STROKE);
          g2.drawLine(box.x + abscises[i], box.y + MARGE_Y, box.x + abscises[i], box.y + MARGE_Y + gHeight);
        }
        g2.setStroke(BOLD_STROKE);
        g2.setColor(V2_COLOR);
        g2.drawLine(box.x + abscises[i], (int) (box.y + MARGE_Y + gHeight - ((sd1.percentSolved() * gHeight) / 100)),
            box.x + abscises[i + 1], (int) (box.y + MARGE_Y + gHeight - ((sd2.percentSolved() * gHeight) / 100)));
        g2.setColor(V1_COLOR);
        g2.drawLine(box.x + abscises[i], (int) (box.y + MARGE_Y + gHeight - ((sd1.percentPrec() * gHeight) / 100)),
            box.x + abscises[i + 1], (int) (box.y + MARGE_Y + gHeight - ((sd2.percentPrec() * gHeight) / 100)));
        if (puntsDePas[j] == i) { // mostrem punt i columna
          g2.setColor(TEXT_COLOR);
          g2.drawString(veryShortDateFormat.format(sd1.date), box.x + abscises[i], box.y + MARGE_Y + gHeight + 15);
          j++;
        }
        dataFinal = sd2.date;
      }
      g2.setColor(TEXT_COLOR);
      g2.drawString(veryShortDateFormat.format(dataFinal), box.x + abscises[i], box.y + MARGE_Y + gHeight + 15);
    } else {
      String noDades = bundle.getString("report_no_data");
      g2.setColor(ALERT_COLOR);
      g2.setFont(ALERT_FONT);
      Rectangle r = ALERT_FONT.getStringBounds(noDades, g2.getFontRenderContext()).getBounds();
      g2.drawString(noDades, box.x + MARGE_X + (gWidth - r.width) / 2, box.y + MARGE_Y + gHeight / 2 + r.height / 4);
    }
  }

  public static final int N_DIST_ELEMENTS = 5;

  public void dibuixaDistribucio(Graphics2D g2, List v, Rectangle box) {

    int[] dist = new int[N_DIST_ELEMENTS];
    Iterator it = v.iterator();
    int vc = 100 / N_DIST_ELEMENTS;
    while (it.hasNext()) {
      SessionData sd = (SessionData) it.next();
      if (sd.actData != null && sd.actData.size() > 0) {
        Iterator it2 = sd.actData.iterator();
        while (it.hasNext()) {
          ActivityData ad = (ActivityData) it.next();
          dist[Math.min(99, ad.qualification) / vc]++;
        }
      } else {
        dist[Math.min(99, sd.percentPrec()) / vc] += sd.numActs;
      }
    }

    int leftMargin = M_X_DIST;
    int topMargin = M_Y_DIST;

    g2.setColor(TEXT_COLOR);
    g2.setStroke(THIN_STROKE);
    g2.setFont(STD_FONT);
    float max = 0.0f, maxOrdenades = 0.0f, maxDivisio = 0.0f;

    boolean data = false;
    for (int i = 0; i < dist.length && data == false; i++)
      data = (dist[i] > 0);

    if (data) {
      max = dist[0];
      for (int i = 1; i < dist.length; i++)
        max = Math.max(max, dist[i]);
      maxOrdenades = getMaximOrdenades(max, false);
      maxDivisio = getMaximOrdenades(max, true);
      String sNum = formatNumber(maxDivisio);
      leftMargin += g2.getFontMetrics().stringWidth(sNum);
    }

    int gWidth = box.width - leftMargin - 10; // 10 pixels de marge per la dreta
    int gHeight = box.height - (2 * topMargin);
    g2.drawRect(box.x + leftMargin, box.y + topMargin, gWidth, gHeight);

    if (data) {
      int numDivisionsY = 0;
      for (float f = max; f > 0.01; f -= maxDivisio)
        numDivisionsY++;

      if (numDivisionsY > 0) {
        float longdivisioY = gHeight / numDivisionsY;
        for (int i = 1; i < numDivisionsY; i++) { // Posem les linies horitzontals
          g2.drawLine(box.x + leftMargin, box.y + topMargin + (int) (i * longdivisioY), box.x + leftMargin + gWidth,
              box.y + topMargin + (int) (i * longdivisioY));
        }
        for (int i = 0; i <= numDivisionsY; i++) { // Posem els valors horitzontals en %
          float num = (maxDivisio * numDivisionsY) - (i * maxDivisio);
          String sNum = formatNumber(num);
          g2.drawString(sNum, box.x + leftMargin - 5 - (g2.getFontMetrics().stringWidth(sNum)),
              box.y + topMargin + (int) (longdivisioY * i) + 3);
        }
      }

      float longColumnaX = gWidth / N_DIST_ELEMENTS; // longitud horitzontal de cadascuna de les columnes
      for (int i = 0; i < N_DIST_ELEMENTS; i++) {
        g2.setColor(TEXT_COLOR);
        g2.setStroke(THIN_STROKE);
        g2.drawLine(box.x + leftMargin + (int) (i * longColumnaX), box.y + topMargin,
            box.x + leftMargin + (int) (i * longColumnaX), box.y + topMargin + gHeight);
        StringBuilder sb = new StringBuilder(Integer.toString(i * 20));
        sb.append("%");
        g2.drawString(sb.substring(0), box.x + leftMargin + (int) (i * longColumnaX), box.y + topMargin + gHeight + 15);
        g2.setColor(DIST_COLOR);
        int alt = ((dist[i] * 100 / (int) (maxDivisio * numDivisionsY) * gHeight) - 1) / 100;
        int ample = (i == (N_DIST_ELEMENTS - 1)) ? (int) (gWidth - (longColumnaX * i) - 1) : (int) (longColumnaX - 1);
        g2.fillRect(box.x + leftMargin + (int) (i * longColumnaX) + 1, box.y + topMargin + gHeight - alt, ample, alt);
      }
    } else {
      String noDades = bundle.getString("report_no_data");
      g2.setColor(ALERT_COLOR);
      g2.setFont(STD_FONT);
      Rectangle r = STD_FONT.getStringBounds(noDades, g2.getFontRenderContext()).getBounds();
      g2.drawString(noDades, box.x + leftMargin + (gWidth - r.width) / 2,
          box.y + topMargin + gHeight / 2 + r.height / 4);
    }
  }

  public static float getMaximOrdenades(float maxim, boolean divisio) {
    float f = maxim * 100;
    if (divisio)
      f /= 6;
    int i = (int) f; // enter entre 0 i 9 de mes a l'esquerra de f.
    int xifresTretes = 0;
    while (i > 9) {
      i = (int) f;
      f = f / 10;
      xifresTretes++;
    }
    if (i == 1)
      i = 2;
    else if (i > 1 && i < 5)
      i = 5;
    else
      i = 10;
    for (int k = 1; k < xifresTretes; k++)
      i *= 10;
    return ((float) i) / 100;
  }

  private static DecimalFormat DF = null;

  protected static String formatNumber(float n) {
    if (DF == null) {
      DF = new DecimalFormat();
      DF.setMaximumFractionDigits(1);
      DF.setGroupingUsed(false);
    }
    return DF.format(n);
  }
}
