/*
 * File    : ActivityEditor.java
 * Created : 18-sep-2002 17:28
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

package edu.xtec.jclic;

import edu.xtec.jclic.automation.AutoContentProviderEditor;
import edu.xtec.jclic.bags.*;
import edu.xtec.jclic.boxes.ActiveBagContentEditor;
import edu.xtec.jclic.boxes.ActiveBoxContentEditor;
import edu.xtec.jclic.edit.*;
import edu.xtec.jclic.media.EventSoundsEditorPanel;
import edu.xtec.jclic.project.JClicProjectEditor;
import edu.xtec.jclic.skins.Skin;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.TripleString;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class ActivityEditor extends Editor {

  protected static ImageIcon icon;

  /** Creates a new instance of ActivityEditor */
  public ActivityEditor(Activity act) {
    super(act);
  }

  protected void createChildren() {
    Activity act = getActivity();
    if (act.acp != null)
      act.acp.getEditor(this);
  }

  public AutoContentProviderEditor getAutoContentProviderEditor() {
    return (AutoContentProviderEditor) getFirstChild(AutoContentProviderEditor.class);
  }

  public Class getEditorPanelClass() {
    return ActivityEditorPanel.class;
  }

  protected static final int COMMON_PANELS = 3;

  public synchronized EditorPanel createEditorPanel(Options options) {
    ActivityEditorPanel panel = new ActivityEditorPanel(options);
    panel.addInternalPanel(new ActivityEditorOptionsPanel(panel), null, null);
    panel.addInternalPanel(new ActivityEditorFramePanel(panel), null, null);
    panel.addInternalPanel(new ActivityEditorMsgPanel(panel), null, null);
    createPanels(panel);
    return panel;
  }

  @Override
  public String getTitleKey() {
    return "edit_act";
  }

  protected void createPanels(ActivityEditorPanel panel) {
  }

  public Activity getActivity() {
    return (Activity) getFirstObject(Activity.class);
  }

  public JClicProjectEditor getProjectEditor() {
    return (JClicProjectEditor) getFirstParent(JClicProjectEditor.class);
  }

  public MediaBagEditor getMediaBagEditor() {
    JClicProjectEditor jcpe = getProjectEditor();
    return jcpe == null ? null : jcpe.getMediaBagEditor();
  }

  public Options getOptions() {
    return getActivity().getProject().getBridge().getOptions();
  }

  public ActivityBagElementEditor getActivityBagElementEditor() {
    return (ActivityBagElementEditor) getFirstParent(ActivityBagElementEditor.class);
  }

  public ActivityBagEditor getActivityBagEditor() {
    return (ActivityBagEditor) getFirstParent(ActivityBagEditor.class);
  }

  public boolean rename(String newName, Component parent, Messages msg) {
    String oldName = getActivity().name;
    String errMsg = null;
    newName = newName.trim();
    boolean result = false;

    if (oldName.equals(newName)) {
      // do nothing
    } else if (newName.length() < 1)
      errMsg = "edit_media_rename_invalid";
    else if (getActivityBagEditor().getActivityBag().getElementByName(newName) != null)
      errMsg = "edit_media_rename_exists";
    else {
      result = getProjectEditor().nameChanged(Constants.T_ACTIVITY, oldName, newName);
    }

    if (errMsg != null && msg != null && parent != null) {
      msg.showAlert(parent, errMsg);
    }
    return result;
  }

  public synchronized void saveData() {
    ActivityBagElementEditor abee = getActivityBagElementEditor();
    if (abee != null) {
      ActivityBagElement abe = abee.getActivityBagElement();
      String name = abe.getName();
      abe.setData(getActivity().getJDomElement());
      if (!abe.getName().equals(name)) {
        // FIRE NAME CHANGE
      }
      if (isModified())
        abee.setModified(true);
    }
  }

  @Override
  public String toString() {
    return getActivity().name;
  }

  @Override
  public void setActionsOwner() {
    if (getEditorParent() != null)
      getEditorParent().setActionsOwner();
  }

  public static Icon getIcon() {
    if (icon == null)
      icon = edu.xtec.util.ResourceManager.getImageIcon("icons/miniclic.png");
    return icon;
  }

  @Override
  public Icon getIcon(boolean leaf, boolean expanded) {
    return getIcon();
  }

  static final BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
      new float[] { 5.0f }, 0.0f);

  public void drawPreview(Graphics2D g2, Rectangle bounds, int margin) {
    Rectangle r = new Rectangle(bounds.x + margin, bounds.y + margin, bounds.width - 2 * margin,
        bounds.height - 2 * margin);
    g2.setXORMode(Color.white);
    Stroke str = g2.getStroke();
    g2.setStroke(dashedStroke);
    g2.draw(r);
    g2.setPaintMode();
    g2.setStroke(str);
  }

  public boolean nameChanged(int type, String oldName, String newName) {
    boolean result = false;
    Activity act = getActivity();

    if ((type & Constants.T_ACTIVITY) != 0 && oldName.equals(act.name)) {
      act.name = newName;
      result = true;
    }

    for (int i = 0; i < act.messages.length; i++) {
      if (act.messages[i] != null)
        result |= ActiveBoxContentEditor.nameChanged(act.messages[i], type, oldName, newName);
    }

    if ((type & Constants.T_IMAGE) != 0 && oldName.equals(act.bgImageFile)) {
      act.bgImageFile = newName;
      result = true;
    }

    if (act.eventSounds != null && (type & (Constants.T_AUDIO | Constants.T_MIDI)) != 0) {
      result |= EventSoundsEditorPanel.nameChanged(act.eventSounds, type, oldName, newName);
    }

    if ((type & Constants.T_XML) != 0 && oldName.equals(act.skinFileName)
        && !oldName.startsWith(Skin.INTERNAL_SKIN_PREFIX)) {
      act.skinFileName = newName;
      result = true;
      // Explore skin dependences...
    }

    if (act.abc != null) {
      for (int i = 0; i < act.abc.length; i++) {
        if (act.abc[i] != null)
          result |= ActiveBagContentEditor.nameChanged(act.abc[i], type, oldName, newName);
      }
    }

    if (result) {
      setModified(true);
    }

    return result;
  }

  public static final String SYSTEM_LIST = "activities.listactivities";

  public static List<TripleString> getSystemActivityList(Options options) {
    List<TripleString> result;
    try {
      result = TripleString.getTripleList(SYSTEM_LIST, options, false, true, true);
    } catch (Exception ex) {
      System.err.println("Error reading list of activities!\n" + ex);
      result = new ArrayList<TripleString>();
    }
    return result;
  }
}
