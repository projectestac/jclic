/*
 * File    : Actions.java
 * Created : 18-dec-2001 10:48
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

package edu.xtec.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;

/**
 * Miscellaneous utilities for managing {@link javax.swing.Action} objects.
 *
 * <p>EXAMPLES:
 *
 * <p>Creation of a new action, with reference to a original key: <CODE>
 *  AbstractAction beginAction=new AbstractAction(DefaultEditorKit.beginAction){
 *    public void actionPerformed(ActionEvent e){
 *    // ...
 *  }
 * };
 * </CODE> Creation of a new action, with a new key: <CODE>
 * AbstractAction prevTargetAction=new AbstractAction("prev-target"){
 *  public void actionPerformed(ActionEvent e){
 *    // ...
 *  }
 * };
 * </CODE> Action that references an existing one: <CODE>
 * Action kitUpAction=null;
 * AbstractAction upAction=new AbstractAction(DefaultEditorKit.upAction){
 *  public void actionPerformed(ActionEvent e){
 *    if(kitUpAction!=null){
 *      // .... pre-action
 *    kitUpAction.actionPerformed(e);
 *      // .... post-action
 *    }
 *  }
 * };
 * </CODE> Actions mapping: <CODE>
 * protected void setActions(){
 *  // get existing actions:
 *  kitUpAction=getActionMap().get(DefaultEditorKit.upAction);
 *
 *  // actionKeys init:
 *  java.util.HashMap actionKeys=Actions.getActionKeys(this);
 *
 *  // build new ActionMap:
 *  ActionMap am=new ActionMap();
 *  am.setParent(getActionMap());
 *  setActionMap(am);
 *
 *  //Actions derived to trace (only for debug purposes):
 *  Actions.mapTraceAction(this, actionKeys, DefaultEditorKit.readOnlyAction);
 *  Actions.mapTraceAction(this, actionKeys, DefaultEditorKit.writableAction);
 *  Actions.mapTraceAction(this, actionKeys, "requestFocus");
 *  Actions.mapTraceAction(this, actionKeys, "toggle-componentOrientation");
 *
 *  //Actions to disable:
 *  Actions.mapNullAction(this, actionKeys, DefaultEditorKit.beepAction);
 *
 *  //New actions:
 *  Actions.mapAction(this, actionKeys, beginAction);
 *  Actions.mapAction(this, actionKeys, upAction);
 *
 *  //Original actions mapped to other methods:
 *  Actions.mapAction(this, actionKeys, beginAction, DefaultEditorKit.previousWordAction);
 *  Actions.mapAction(this, actionKeys, prevTargetAction, DefaultEditorKit.beginParagraphAction);
 *
 *  //Uncomment this line only if you want to hide all other actions:
 *  //am.setParent(null);
 *  //
 *
 *  // Assign keystrokes to a new action:
 *  getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, java.awt.Event.SHIFT_MASK), prevTargetAction.getValue(Action.NAME));
 * }
 * </CODE>
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public abstract class Actions {

  /**
   * Collects the keys of all the actions linked to a specific {@link javax.swing.JComponent} and
   * groups them in arrays by action names.
   *
   * @param jc The JComponent with the actions linked to.
   * @return A HashMap formed by pairs of action names (key) and arrays of action keys (value).
   *     Usually each action name has only one action key associated to it, and the value of the
   *     pair is an object array with only one string, but this is not an imperative: several
   *     actions can be associated to a single name.
   */
  public static Map<String, Object[]> getActionKeys(JComponent jc) {
    Map<String, Object[]> result = new HashMap<String, Object[]>();
    ActionMap am = jc.getActionMap();
    for (Object amk : am.allKeys()) {
      Action act = am.get(amk);
      Object o = act.getValue(Action.NAME);
      if (o == null) o = "";
      String name = o.toString();
      Object[] keys = result.get(name);
      if (keys == null) keys = new Object[] {amk};
      else {
        Object[] k2 = new Object[keys.length + 1];
        int j;
        for (j = 0; j < keys.length; j++) k2[j] = keys[j];
        k2[j] = amk;
        keys = k2;
      }
      result.put(name, keys);
    }
    return result;
  }

  /**
   * This function is for debug purposes only. Prints out the
   *
   * @param actionKeys
   * @param am
   */
  public static void dumpActionKeys(Map<String, Object[]> actionKeys, ActionMap am) {
    for (String key : actionKeys.keySet()) {
      System.out.println(key + ":");
      for (Object val : (Object[]) actionKeys.get(key))
        System.out.println(" - " + val + " >> " + am.get(val));
    }
  }

  public static void mapAction(JComponent jc, Map<String, Object[]> actionKeys, Action act) {
    if (actionKeys == null) {
      actionKeys = getActionKeys(jc);
    }
    if (jc != null && act != null && actionKeys != null) {
      ActionMap am = jc.getActionMap();
      String name = (String) act.getValue(Action.NAME);
      Object[] keys = actionKeys.get(name);
      boolean replaced = false;
      if (keys != null) {
        for (Object key : keys) {
          am.put(key, act);
          if (key.equals(name)) {
            replaced = true;
          }
        }
      }
      if (!replaced) {
        am.put(name, act);
      }
    }
  }

  public static void mapAction(
      JComponent jc, Map<String, Object[]> actionKeys, Action act, String key) {
    if (actionKeys == null) {
      actionKeys = getActionKeys(jc);
    }
    if (jc != null && actionKeys != null) {
      ActionMap am = jc.getActionMap();
      Object[] keys = actionKeys.get(key);
      boolean replaced = false;
      if (keys != null) {
        for (Object k : keys) {
          am.put(k, act);
          if (k.equals(key)) {
            replaced = true;
          }
        }
      }
      if (!replaced) {
        am.put(key, act);
      }
    }
  }

  public static class NullAction extends AbstractAction {
    public NullAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      // do nothing
    }
  }

  public static void mapNullAction(JComponent jc, Map<String, Object[]> actionKeys, String s) {
    mapAction(jc, actionKeys, new edu.xtec.util.Actions.NullAction(s));
  }

  public static class TraceAction extends AbstractAction {
    Action bkAction = null;
    Object bkKey = null;

    public TraceAction(Action act, Object key) {
      super((String) (key instanceof String ? key : ""));
      bkAction = act;
      bkKey = key;
    }

    public void actionPerformed(ActionEvent e) {
      System.out.println(
          "TRACE: " + (bkAction != null ? bkAction.getValue(Action.NAME) : "") + " " + bkKey);
      if (bkAction != null) bkAction.actionPerformed(e);
    }
  }

  public static void mapTraceAction(JComponent jc, Map<String, Object[]> actionKeys, String s) {
    mapAction(jc, actionKeys, new edu.xtec.util.Actions.TraceAction(jc.getActionMap().get(s), s));
  }

  public static void traceHierarchy(Component cmp, int level) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++) sb.append(" ");
    sb.append(cmp);
    System.out.println(sb.substring(0));
    if (cmp instanceof Container) {
      for (Component cmp2 : ((Container) cmp).getComponents()) traceHierarchy(cmp2, level + 1);
    }
  }
}
