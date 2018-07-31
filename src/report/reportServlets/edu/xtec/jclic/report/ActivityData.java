/*
 * File    : ActivityData.java
 * Created : 07-feb-2003 17:01
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

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ActivityData implements java.io.Serializable {

  public String project, activityName, id;
  public int time, actions, score, qualification;
  public boolean solved;

  /** Creates a new instance of ActivityData */
  public ActivityData(
      String project,
      String activityName,
      String id,
      int time,
      int actions,
      int score,
      boolean solved,
      int qualification) {
    this.project = project != null ? project.trim() : null;
    this.activityName = activityName != null ? activityName.trim() : null;
    this.id = id != null ? id.trim() : null;
    this.time = time;
    this.actions = actions;
    this.score = score;
    this.solved = solved;
    this.qualification = qualification;
  }

  public int percentSolved() {
    int result = 0;
    if (actions > 0) result = (score * 100) / actions;
    return result;
  }
}
