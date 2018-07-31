/*
 * File    : SessionReg.java
 * Created : 29-jan-2002 11:06
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

package edu.xtec.jclic.report;

import edu.xtec.jclic.Activity;
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.Html;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public class SessionReg implements java.io.Serializable {

  List<SequenceReg> sequences;
  SequenceReg currentSequence = null;
  public java.util.Date started;
  protected long timeMillis;
  String projectName;
  protected transient Info info;
  String code;

  /** Creates new SessionReg */
  public SessionReg(JClicProject jcp) {
    this(jcp.getName(), jcp.code);
  }

  public SessionReg(String projectName, String code) {
    sequences = new CopyOnWriteArrayList<SequenceReg>();
    currentSequence = null;
    started = new java.util.Date();
    timeMillis = System.currentTimeMillis();
    this.projectName = projectName;
    this.code = code;
    info = new Info();
  }

  public String toHtmlString(
      edu.xtec.util.Messages msg, boolean recalcInfo, boolean writeProjectName) {
    String prefix = "report_";
    Html html = new Html(3000);
    if (recalcInfo) info.recalc();

    if (info.numSequences > 0) {
      if (writeProjectName) {
        html.append("<TR STYLE=\"")
            .append(msg.get("about_window_html_style_table_header2"))
            .append("\">");
        html.append("<TD COLSPAN=\"6\">");
        html.bold(msg.get(prefix + "project") + Html.NBSP + projectName);
        html.td(false).tr(false);
      }
      html.append("<TR STYLE=\"")
          .append(msg.get("about_window_html_style_table_header"))
          .append("\">");
      html.td(msg.get(prefix + "lb_sequence"), Html.CENTER, true, null);
      html.td(msg.get(prefix + "lb_activity"), Html.CENTER, true, null);
      html.td(msg.get(prefix + "lb_solved"), Html.CENTER, true, null);
      html.td(msg.get(prefix + "lb_actions"), Html.CENTER, true, null);
      html.td(msg.get(prefix + "lb_score"), Html.CENTER, true, null);
      html.td(msg.get(prefix + "lb_time"), Html.CENTER, true, null);
      html.tr(false);
      Iterator<SequenceReg> it = sequences.iterator();
      while (it.hasNext()) html.append(it.next().toHtmlString(msg));

      html.append("<TR STYLE=\"")
          .append(msg.get("about_window_html_style_table_totals"))
          .append("\">");
      html.td(msg.get(prefix + "lb_totals"), Html.LEFT, true, null);
      html.td(msg.getNumber(info.nActivities), Html.RIGHT, true, null);
      html.td(
          msg.getNumber(info.nActSolved) + " (" + msg.getPercent(info.percentSolved) + ")",
          Html.RIGHT,
          true,
          null);
      html.td(msg.getNumber(info.nActions), Html.RIGHT, true, null);
      html.td(msg.getPercent(info.tScore), Html.RIGHT, true, null);
      html.td(msg.getHmsTime(info.tTime), Html.RIGHT, true, null);
      html.tr(false);
    }

    return html.toString();
  }

  public Info getInfo(boolean recalc) {
    if (recalc) info.recalc();
    return info;
  }

  public class Info {
    public int numSequences, nActivities, nActSolved, nActScore, percentSolved, nActions;
    public long tScore, tTime;

    protected Info() {
      clear();
    }

    protected void clear() {
      numSequences = nActivities = nActSolved = nActScore = percentSolved = nActions = 0;
      tScore = tTime = 0L;
    }

    public void recalc() {
      clear();
      Iterator<SequenceReg> it = sequences.iterator();
      while (it.hasNext()) {
        SequenceReg.Info sri = it.next().getInfo(true);
        if (sri.nActivities > 0) {
          numSequences++;
          if (sri.nActClosed > 0) {
            nActivities += sri.nActClosed;
            nActions += sri.nActions;
            if (sri.nActScore > 0) {
              nActScore += sri.nActScore;
              tScore += (sri.tScore * sri.nActScore);
            }
            tTime += sri.tTime;
            nActSolved += sri.nActSolved;
          }
        }
        //
        // 20-Feb-2006 - Correction of bug #41
        // This code must be executed after the "for" iteration:
        // if(nActScore>0)
        //     tScore/=nActScore;
        // if(nActivities>0)
        //     percentSolved=(nActSolved*100)/nActivities;
      }
      if (nActScore > 0) tScore /= nActScore;
      if (nActivities > 0) percentSolved = (nActSolved * 100) / nActivities;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    end();
    super.finalize();
  }

  public void end() {
    endSequence();
  }

  public void endSequence() {
    if (currentSequence != null && currentSequence.totalTime == 0) currentSequence.endSequence();
    currentSequence = null;
  }

  public void newSequence(ActivitySequenceElement ase) {
    endSequence();
    currentSequence = new SequenceReg(ase);
    sequences.add(currentSequence);
  }

  public void newActivity(Activity act) {
    if (currentSequence != null) currentSequence.newActivity(act);
  }

  public void endActivity(int score, int numActions, boolean solved) {
    if (currentSequence != null) currentSequence.endActivity(score, numActions, solved);
  }

  public void newAction(String type, String source, String dest, boolean ok) {
    if (currentSequence != null) currentSequence.newAction(type, source, dest, ok);
  }

  public String getCurrentSequenceTag() {
    if (currentSequence == null) return null;
    return currentSequence.name;
  }

  public SequenceReg.Info getCurrentSequenceInfo() {
    return currentSequence == null ? null : currentSequence.getInfo(true);
  }
}
