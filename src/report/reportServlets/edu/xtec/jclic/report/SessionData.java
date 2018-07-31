/*
 * File    : SessionData.java
 * Created : 05-feb-2003 13:09
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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class SessionData extends Object implements java.io.Serializable {

  public String id;
  public Date date;
  public String project;
  public int numActs;
  public int actsSolved;
  public int totalPrec;
  public int totalTime;
  public int sessionCount;
  public Set<String> users;
  public List<ActivityData> actData;

  /** Creates a new instance of SessionData */
  public SessionData(
      String id,
      String usr,
      String project,
      Date date,
      int numActs,
      int actsSolved,
      int totalPrec,
      int totalTime) {
    this.id = id != null ? id.trim() : null;
    users = new HashSet<String>(1);
    users.add(usr != null ? usr.trim() : null);
    this.project = project != null ? project.trim() : null;
    this.date = date;
    this.numActs = numActs;
    this.actsSolved = actsSolved;
    this.totalPrec = totalPrec;
    this.totalTime = Math.max(0, totalTime);
    sessionCount = 1;
  }

  public void acumula(SessionData d) {
    actsSolved += d.actsSolved;
    totalPrec += d.totalPrec;
    numActs += d.numActs;
    totalTime += d.totalTime;
    users.addAll(d.users);
    if (project != null && !project.equals(d.project)) project = "";
    id = "";
    sessionCount += d.sessionCount;
    if (actData == null) actData = d.actData;
    else if (d.actData != null) actData.addAll(d.actData);
  }

  public boolean sameDate(SessionData d) {
    return date != null && d != null && date.equals(d.date);
  }

  public int percentSolved() {
    return numActs > 0 ? (100 * actsSolved) / numActs : 0;
  }

  public int percentPrec() {
    return numActs > 0 ? totalPrec / numActs : 0;
  }

  public String getUsr() {
    String result = "";
    if (users.size() > 1) result = "*";
    else {
      Iterator it = users.iterator();
      if (it.hasNext()) result = (String) it.next();
    }
    return result;
  }
}
