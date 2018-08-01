/*
 * File    : SequenceReg.java
 * Created : 11-jul-2001 9:08
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
import edu.xtec.util.Html;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.13
 */
public class SequenceReg extends Object implements java.io.Serializable {

  String name;
  String description;
  List<ActivityReg> activities;
  ActivityReg currentActivity;
  long totalTime;
  boolean closed;
  protected transient Info info;

  /** Creates new SequenceReg */
  public SequenceReg(ActivitySequenceElement ase) {
    name = ase.getTag();
    description = ase.getDescription();
    activities = new CopyOnWriteArrayList<ActivityReg>();
    currentActivity = null;
    totalTime = 0;
    closed = false;
    info = new Info();
  }

  public String toHtmlString(edu.xtec.util.Messages msg) {
    Html html = new Html(3000);
    String fh = new Html(200).td(name, Html.LEFT, false, "ROWSPAN=\"" + msg.getNumber(activities.size()) + "\"")
        .toString();
    Iterator<ActivityReg> it = activities.iterator();
    while (it.hasNext()) {
      html.append(it.next().toHtmlString(msg, fh));
      fh = null;
    }
    return html.toString();
  }

  public Info getInfo(boolean recalc) {
    if (recalc)
      info.recalc();
    return info;
  }

  public class Info {
    public int nActivities, nActClosed, nActSolved, nActScore, percentSolved, nActions;
    public long tScore, tTime;

    protected Info() {
      clear();
    }

    protected void clear() {
      nActivities = nActClosed = nActSolved = nActScore = percentSolved = nActions = 0;
      tScore = tTime = 0L;
    }

    public void recalc() {
      clear();
      nActivities = activities.size();
      if (nActivities > 0) {
        Iterator<ActivityReg> it = activities.iterator();
        while (it.hasNext()) {
          ActivityReg ar = it.next();
          if (ar.closed) {
            nActClosed++;
            tTime += ar.totalTime;
            nActions += ar.numActions;
            if (ar.solved)
              nActSolved++;
            int r = ar.getPrecision();
            if (r >= 0) {
              tScore += r;
              nActScore++;
            }
          }
        }
        if (nActClosed > 0)
          percentSolved = (nActSolved * 100) / nActClosed;
        if (nActScore > 0)
          tScore /= nActScore;
      }
    }
  }

  void newActivity(Activity act) {
    if (!closed) {
      currentActivity = new ActivityReg(act);
      activities.add(currentActivity);
    }
  }

  void newAction(String type, String source, String dest, boolean ok) {
    if (currentActivity != null) {
      currentActivity.newAction(type, source, dest, ok);
    }
  }

  void endActivity(int score, int numActions, boolean solved) {
    if (currentActivity != null)
      currentActivity.endActivity(score, numActions, solved);
  }

  void endSequence() {
    if (currentActivity != null && !activities.isEmpty()) {
      if (!currentActivity.closed)
        currentActivity.closeActivity();
      ActivityReg firstActivity = activities.get(0);
      totalTime = currentActivity.startTime + currentActivity.totalTime - firstActivity.startTime;
    }
  }

  @Override
  public String toString() {
    return new StringBuilder("SEQUENCE: ").append(name).substring(0);
  }
}
