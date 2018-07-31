/*
 * File    : PlayerHistory.java
 * Created : 11-jan-2002 15:31
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

import edu.xtec.jclic.bags.ActivitySequence;
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.bags.JumpInfo;
import java.util.Stack;

/**
 * PlayerHistory uses a {@link java.util.Stack} object to store the list of projects and activities
 * done by the user. This class allows {@link edu.xtec.jclic.Player} objects to rewind a sequence or
 * go back to a caller menu.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class PlayerHistory {

  /** The <CODE>Player</CODE> this <CODE>PlayerHistory</CODE> belongs to */
  protected Player player;
  /**
   * This is the main member of the class. <CODE>PlayerHistory</CODE> puts and retrieves on it
   * information about the proects and activities done by the current user.
   */
  protected Stack<HistoryElement> sequenceStack;
  /**
   * When in test mode (for instance, in the <CODE>Player</CODE> used by JClic author to preview
   * activities), jumps are only simulated.
   */
  protected boolean testMode;

  /**
   * Creates a new PlayerHistory
   *
   * @param player The <CODE>Player</CODE> this <CODE>PlayerHistory</CODE> belongs to
   */
  public PlayerHistory(Player player) {
    this.player = player;
    sequenceStack = new Stack<HistoryElement>();
  }

  /**
   * Counts the number of history elements stored in the stack
   *
   * @return The number of stored elements
   */
  public int storedElementsCount() {
    return sequenceStack.size();
  }

  /** Removes all the elements from the history stack */
  public void clearHistory() {
    sequenceStack.clear();
  }

  /** <CODE>PlayerHistory</CODE> uses this inner class to store history elements. */
  protected class HistoryElement {
    String projectPath;
    String sequence;
    int activity;

    HistoryElement(String path, String seq, int act) {
      projectPath = path;
      sequence = seq;
      activity = act;
    }
  }

  /**
   * Adds the current <CODE>Player</CODE>'s project and activity to the top of the history stack.
   */
  public void push() {
    if (player.project != null && player.project.getFullPath() != null) {
      ActivitySequence ase = player.project.activitySequence;
      int act = ase.getCurrentActNum();
      if (act >= 0) {
        if (!sequenceStack.isEmpty()) {
          HistoryElement last = sequenceStack.peek();
          if (last.projectPath.equals(player.project.getFullPath()) && last.activity == act) return;
        }
        sequenceStack.push(
            new HistoryElement(player.project.getFullPath(), ase.getSequenceForElement(act), act));
      }
    }
  }

  /**
   * Retrieves the history element placed at the top of the stack (if any) and makes the <CODE>
   * Player</CODE> to load it. The obtained effect is to "rewind" or "go back", usually to a caller
   * menu or activity.
   *
   * @return The current implementation of this method always returns <CODE>true</CODE>.
   */
  public boolean pop() {
    // todo: check return value
    if (!sequenceStack.isEmpty()) {
      HistoryElement e = sequenceStack.pop();
      if (e.projectPath.equals(player.project.getFullPath()))
        player.load(null, Integer.toString(e.activity), null, null);
      else {
        if (testMode && e.projectPath != null && e.projectPath.length() > 0) {
          player
              .getMessages()
              .showAlert(player, new String[] {player.getMsg("test_alert_jump_to"), e.projectPath});
        } else {
          player.load(e.projectPath, Integer.toString(e.activity), null, null);
        }
      }
    }
    return true;
  }

  /**
   * Processes the provided <CODE>JumpInfo</CODE> object, instructing the <CODE>Player</CODE> to go
   * back, stop or jump to another point in the sequence.
   *
   * @param ji The <CODE>JumpInfo</CODE> object to be processed
   * @param allowReturn When this param is <CODE>true</CODE>, the jump instructed by the <CODE>
   *     JumpInfo</CODE> (if any) will be recorded, in order to make possible to go back to the
   *     current activity.
   * @return <CODE>true</CODE> if the jump can be processed without errors. <CODE>false</CODE>
   *     otherwise.
   */
  public boolean processJump(JumpInfo ji, boolean allowReturn) {
    boolean result = false;
    if (ji != null && player.project != null) {
      switch (ji.action) {
        case JumpInfo.STOP:
          break;
        case JumpInfo.RETURN:
          result = pop();
          break;
        case JumpInfo.EXIT:
          if (testMode) {
            player.getMessages().showAlert(player, "test_alert_exit");
          } else player.exit(ji.sequence);
          break;
        case JumpInfo.JUMP:
          if (ji.sequence == null && ji.projectPath == null) {
            ActivitySequenceElement ase =
                player.project.activitySequence.getElement(ji.actNum, true);
            if (ase != null) {
              if (allowReturn) push();
              // jcp.activitySequence.setCurrentAct(ji.actNum);
              // result=loadActivity(ase.getActivityName());
              player.load(null, null, ase.getActivityName(), null);
              result = true;
            }
          } else {
            if (testMode && ji.projectPath != null && ji.projectPath.length() > 0) {
              player
                  .getMessages()
                  .showAlert(
                      player, new String[] {player.getMsg("test_alert_jump_to"), ji.projectPath});
            } else result = jumpToSequence(ji.sequence, ji.projectPath, allowReturn);
          }
          break;
      }
    }
    return result;
  }

  private boolean jumpToSequence(String sequence, String path, boolean allowReturn) {
    if (sequence == null && path == null) return false;
    if (path == null) path = player.project.getFullPath();
    if (!sequenceStack.isEmpty()) {
      HistoryElement e = sequenceStack.peek();
      if (sequence != null && path.equals(e.projectPath)) {
        boolean same = sequence.equals(e.sequence);
        if (path.equals(player.project.getFullPath())) {
          ActivitySequenceElement ase =
              player.project.activitySequence.getElement(e.activity, false);
          same = (ase != null && sequence.equals(ase.getTag()));
        }
        if (same) return pop();
      }
    }
    if (allowReturn) push();
    if (path.equals(player.project.getFullPath())) player.load(null, sequence, null, null);
    else player.load(path, sequence, null, null);
    return true;
  }

  /**
   * Getter for property testMode.
   *
   * @return Value of property testMode.
   */
  public boolean isTestMode() {
    return testMode;
  }

  /**
   * Setter for property testMode.
   *
   * @param testMode New value of property testMode.
   */
  public void setTestMode(boolean testMode) {
    this.testMode = testMode;
  }
}
