/*
 * File    : ReportServerConstants.java
 * Created : 21-feb-2003 10:40
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

package edu.xtec.jclic;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public interface ReportServerConstants {

  public static final String TRUE = "true", FALSE = "false";

  public static final String CFG_FILE = "jclicReports.properties";

  public static final String LANGUAGE = "language",
      LOOK = "lookAndFeel",
      TOOLTIPS = "tooltips",
      HTTP_VERBOSE = "http_verbose";

  public static final String SESSION_LIFETIME = "session_lifetime";

  public static final String HTTP_PORT = "http_port",
      HTTP_TIMEOUT = "http_timeout",
      HTTP_AUTOSTART = "http_autostart",
      HTTP_LOGFILE = "http_logFile";
  public static final String GRAPH_WIDTH = "graph_width",
      GRAPH_DIST_WIDTH = "graph_dist_width",
      GRAPH_HEIGHT = "graph_height",
      GRAPH_HEADER_HEIGHT = "graph_header_height",
      GRAPH_MARGIN = "graph_margin",
      GRAPH_COLOR_BG = "graph_color_bg",
      GRAPH_COLOR_TEXT = "graph_color_text",
      GRAPH_COLOR_HEADER_BG = "graph_color_header_bg",
      GRAPH_COLOR_HEADER_TEXT = "graph_color_header_text",
      GRAPH_COLOR_BORDER = "graph_color_border",
      GRAPH_COLOR_V1 = "graph_color_v1",
      GRAPH_COLOR_V2 = "graph_color_v2",
      GRAPH_COLOR_DIST = "graph_color_dist",
      GRAPH_COLOR_ALERT = "graph_color_alert",
      GRAPH_STROKE_WIDTH = "graph_stroke_width";
  public static final String GRAPH_FONT_FAMILY = "graph_font_family",
      GRAPH_FONT_SIZE = "graph_font_size";
  public static final String GRAPH_MARGIN_X = "graph_margin_x",
      GRAPH_MARGIN_Y = "graph_margin_y",
      GRAPH_DIV_Y = "graph_div_y",
      GRAPH_MAX_COLS = "graph_max_cols",
      GRAPH_MARGIN_DIST_X = "graph_margin_dist_x",
      GRAPH_MARGIN_DIST_Y = "graph_margin_dist_y";
}
