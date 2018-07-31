/*
 * File    : DateManager.java
 * Created : 14-feb-2003 16:38
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

import edu.xtec.jclic.report.ReportUtils;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public class DateManager {

  public static final String FROM = "from", TO = "to";
  // public static final String FROM_FORM="fromForm", TO_FORM="toForm";

  protected Date dFrom, dTo, firstDate, today;
  private GregorianCalendar calendar;

  protected Basic rp;

  /** Creates a new instance of DateManager */
  public DateManager(Basic rp) {
    this.rp = rp;
  }

  public boolean init() throws Exception {

    firstDate = Basic.bridge.getMinSessionDate();
    today = new Date();
    if (firstDate == null || firstDate.compareTo(today) > 0) firstDate = today;
    dFrom = rp.getDateParam(FROM, firstDate, false);
    dTo = rp.getDateParam(TO, today, true);
    if (dFrom.compareTo(dTo) > 0) dFrom = dTo;

    System.out.println("From: " + dFrom + " To: " + dTo);
    return true;
  }

  public GregorianCalendar getCalendar() {
    if (calendar == null) calendar = new GregorianCalendar();
    return calendar;
  }

  public void writeHiddenFields(StringBuilder sb) throws Exception {
    sb.append("<input type=\"hidden\" name=\"")
        .append(FROM)
        .append("\" value=\"")
        .append(ReportUtils.dateToStr(dFrom))
        .append("\">\n");
    sb.append("<input type=\"hidden\" name=\"")
        .append(TO)
        .append("\" value=\"")
        .append(ReportUtils.dateToStr(dTo))
        .append("\">\n");
  }

  public void writeDateScript(StringBuilder sb) {
    sb.append("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("<!--\n");
    sb.append(" function updateDateFrom(){\n");
    sb.append("     mainForm.from.value=\n");
    sb.append("       \"\"+mainForm.yearFrom.options[mainForm.yearFrom.selectedIndex].value+\n");
    sb.append("       \"-\"+mainForm.monthFrom.options[mainForm.monthFrom.selectedIndex].value+\n");
    sb.append("       \"-\"+mainForm.dayFrom.options[mainForm.dayFrom.selectedIndex].value;\n");
    sb.append(" };\n");
    sb.append(" function updateDateTo(){\n");
    sb.append("     mainForm.to.value=\n");
    sb.append("       \"\"+mainForm.yearTo.options[mainForm.yearTo.selectedIndex].value+\n");
    sb.append("       \"-\"+mainForm.monthTo.options[mainForm.monthTo.selectedIndex].value+\n");
    sb.append("       \"-\"+mainForm.dayTo.options[mainForm.dayTo.selectedIndex].value;\n");
    sb.append(" };\n");
    sb.append("//-->\n");
    sb.append("</script>\n");
  }

  protected void zonaData(StringBuilder sb, String button) {
    liniaData(sb, true);
    liniaData(sb, false);
    if (button != null) {
      sb.append("<p>").append(button).append("</p>");
    }
  }

  protected void liniaData(StringBuilder sb, boolean bFrom) {
    String[] n = Basic.getFormattedNumbers();
    String msgKey = bFrom ? "report_from" : "report_to";
    // String formName = bFrom ? FROM_FORM : TO_FORM;
    // String type = bFrom ? FROM : TO;
    String type = bFrom ? "From" : "To";

    String ctrlName = Basic.MAIN_FORM + "." + type;
    // String actionStr=" onChange=updateDate"+formName+"()";
    String actionStr = "\" onChange=updateDate" + type + "()";
    GregorianCalendar c = getCalendar();
    c.setTime(firstDate);
    int firstYear = c.get(GregorianCalendar.YEAR);
    c.setTime(today);
    int currentYear = c.get(GregorianCalendar.YEAR);
    c.setTime(bFrom ? dFrom : dTo);

    sb.append("<p><strong>").append(Basic.toNbsp(rp.getMsg(msgKey))).append("</strong> ");
    sb.append("<select name=\"day")
        .append(type)
        .append("\"  onChange=\"updateDate")
        .append(type)
        .append("()\">\n");
    int x = c.get(GregorianCalendar.DAY_OF_MONTH);
    for (int i = 1; i <= 31; i++) {
      sb.append("<option");
      if (i == x) sb.append(" selected");
      sb.append(" value=\"").append(n[i]).append("\">").append(n[i]).append("</option>\n");
    }
    sb.append("</select> <select name=\"month")
        .append(type)
        .append("\"  onChange=\"updateDate")
        .append(type)
        .append("()\">\n");
    x = c.get(GregorianCalendar.MONTH);
    for (int i = 0; i < 12; i++) {
      sb.append("<option");
      if (i == x) sb.append(" selected");
      sb.append(" value=\"")
          .append(n[i + 1])
          .append("\">")
          .append(Basic.filter(rp.months[i]))
          .append("</option>\n");
    }
    sb.append("</select> <select name=\"year")
        .append(type)
        .append("\"  onChange=\"updateDate")
        .append(type)
        .append("()\">\n");
    x = c.get(GregorianCalendar.YEAR);
    for (int i = firstYear; i <= currentYear; i++) {
      sb.append("<option");
      if (i == x) sb.append(" selected");
      sb.append(" value=\"").append(i).append("\">").append(i).append("</option>\n");
    }
    sb.append("</select></p>\n");
  }
}
