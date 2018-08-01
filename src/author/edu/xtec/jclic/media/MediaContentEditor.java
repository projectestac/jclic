/*
 * File    : MediaContentEditor.java
 * Created : 23-dec-2002 11:58
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

package edu.xtec.jclic.media;

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.bags.MediaBagEditor;
import edu.xtec.jclic.bags.MediaBagSelector;
import edu.xtec.jclic.beans.MediaContentButton;
import edu.xtec.jclic.beans.SmallIntEditor;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceManager;
import edu.xtec.util.StrUtils;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class MediaContentEditor extends edu.xtec.util.CtrlPanel {

  MediaContent mc;
  Options options;
  MediaBagEditor mbe;
  public static final String PROP_MC = "mediaContent";
  private static final String[] LOCATION_FROM_MSG_CODES = { "box", "window", "frame" };
  List<String> locationFromNames;
  private JTextField[] textFields;

  /** Creates new form MediaContentEditor */
  public MediaContentEditor(Options options, MediaContent mc, MediaBagEditor mbe) {
    this.options = options;
    pre_init();
    initComponents();
    post_init();
    setMediaBagEditor(mbe);
    setMediaContent(mc);
  }

  private void pre_init() {
    locationFromNames = new ArrayList<String>();
    for (String code : LOCATION_FROM_MSG_CODES)
      locationFromNames.add(options.getMsg("edit_mc_location_from_" + code));
  }

  private void post_init() {
    textFields = new JTextField[] { fileTxt, externalTxt, fromTxt, toTxt, xTxt, yTxt };
    for (int i = 0; i < textFields.length; i++)
      textFields[i].getDocument().addDocumentListener(this);

    Enumeration en = mediaTypes.getElements();
    while (en.hasMoreElements()) {
      JToggleButton rb = (JToggleButton) en.nextElement();
      rb.addActionListener(this);
    }
  }

  public void setMediaBagEditor(MediaBagEditor mbe) {
    this.mbe = mbe;
  }

  private void checkAndFirePropertyChange(MediaContent oldMc) {
    MediaContent currentMc = getMc();
    boolean eq = ((oldMc == null && currentMc == null) || (oldMc != null && oldMc.equals(currentMc)));
    if (!eq)
      firePropertyChange(PROP_MC, oldMc, currentMc);
  }

  public void setMediaContent(MediaContent c) {

    if (c == null)
      c = new MediaContent();

    setInitializing(true);

    mc = (MediaContent) c.clone();

    Enumeration en = mediaTypes.getElements();
    JToggleButton rb = (JToggleButton) en.nextElement();
    int i = 0;
    for (; i < mc.mediaType; i++)
      rb = (JToggleButton) en.nextElement();
    rb.setSelected(true);

    levelEditor.setValue(mc.level);
    recBufferEditor.setValue(mc.recBuffer);
    fileTxt.setText(mc.mediaFileName);
    externalTxt.setText(mc.externalParam);
    fromTxt.setText(Integer.toString(mc.from));
    toTxt.setText(Integer.toString(mc.to));
    lengthEditor.setValue(mc.length);
    stretchChk.setSelected(mc.stretch);
    if (mc.absLocation != null)
      posAbsoluteRadio.setSelected(true);
    else if (mc.free)
      posWindowRadio.setSelected(true);
    else
      posCellRadio.setSelected(true);
    syncPointValues();
    loopChk.setSelected(mc.loop);
    autostartChk.setSelected(mc.autoStart);
    catchMouseChk.setSelected(mc.catchMouseEvents);
    checkEnabled();

    setInitializing(false);
  }

  public boolean check(Component parent) {
    boolean result = false;
    switch (mc.mediaType) {
    case MediaContent.PLAY_AUDIO:
    case MediaContent.PLAY_VIDEO:
    case MediaContent.PLAY_MIDI:
    case MediaContent.RUN_EXTERNAL:
    case MediaContent.URL:
    case MediaContent.RUN_CLIC_ACTIVITY:
      result = StrUtils.nullableString(mc.mediaFileName) != null;
      break;
    case MediaContent.RECORD_AUDIO:
    case MediaContent.PLAY_RECORDED_AUDIO:
      result = (mc.recBuffer >= 0 && mc.recBuffer < 10);
      break;
    case MediaContent.RUN_CLIC_PACKAGE:
      result = StrUtils.nullableString(mc.mediaFileName) != null || StrUtils.nullableString(mc.externalParam) != null;
      break;

    case MediaContent.EXIT:
    case MediaContent.RETURN:
      result = true;
      break;
    }
    if (!result)
      options.getMessages().showAlert(parent, "edit_mc_err_nullContent");
    return result;
  }

  public MediaContent getMc() {
    return mc;
  }

  public MediaContent getMcClone() {
    return mc == null ? null : (MediaContent) mc.clone();
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated
  // Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    mediaTypes = new javax.swing.ButtonGroup();
    posTypes = new javax.swing.ButtonGroup();
    mediaTypePanel = new javax.swing.JPanel();
    javax.swing.JToggleButton toggleType00 = new javax.swing.JToggleButton();
    toggleType01 = new javax.swing.JToggleButton();
    toggleType02 = new javax.swing.JToggleButton();
    toggleType03 = new javax.swing.JToggleButton();
    toggleType04 = new javax.swing.JToggleButton();
    toggleType05 = new javax.swing.JToggleButton();
    toggleType06 = new javax.swing.JToggleButton();
    toggleType07 = new javax.swing.JToggleButton();
    toggleType08 = new javax.swing.JToggleButton();
    toggleType09 = new javax.swing.JToggleButton();
    toggleType10 = new javax.swing.JToggleButton();
    toggleType11 = new javax.swing.JToggleButton();
    toggleType12 = new javax.swing.JToggleButton();
    mainPanel = new javax.swing.JPanel();
    lbLevel = new javax.swing.JLabel();
    levelEditor = new edu.xtec.jclic.beans.SmallIntEditor();
    loopChk = new javax.swing.JCheckBox();
    autostartChk = new javax.swing.JCheckBox();
    filePanel = new javax.swing.JPanel();
    javax.swing.JPanel filePanelCard0 = new javax.swing.JPanel();
    javax.swing.JPanel filePanelCard1 = new javax.swing.JPanel();
    lbFile = new javax.swing.JLabel();
    fileTxt = new javax.swing.JTextField();
    findFileBtn = new javax.swing.JButton();
    javax.swing.JPanel filePanelCard2 = new javax.swing.JPanel();
    lbRecbuffer = new javax.swing.JLabel();
    recBufferEditor = new edu.xtec.jclic.beans.SmallIntEditor();
    lbLength = new javax.swing.JLabel();
    lengthEditor = new edu.xtec.jclic.beans.SmallIntEditor();
    fragmentPanel = new javax.swing.JPanel();
    javax.swing.JPanel fragmentPanelCard0 = new javax.swing.JPanel();
    javax.swing.JPanel fragmentPanelCard1 = new javax.swing.JPanel();
    lbFrom = new javax.swing.JLabel();
    fromTxt = new javax.swing.JTextField();
    lbTo = new javax.swing.JLabel();
    toTxt = new javax.swing.JTextField();
    javax.swing.JPanel fragmentPanelCard2 = new javax.swing.JPanel();
    lbExternal = new javax.swing.JLabel();
    externalTxt = new javax.swing.JTextField();
    locationPanel = new javax.swing.JPanel();
    javax.swing.JPanel locationPanelCard0 = new javax.swing.JPanel();
    javax.swing.JPanel locationPanelCard1 = new javax.swing.JPanel();
    javax.swing.JPanel lpanel = new javax.swing.JPanel();
    posCellRadio = new javax.swing.JRadioButton();
    posWindowRadio = new javax.swing.JRadioButton();
    posAbsoluteRadio = new javax.swing.JRadioButton();
    lbX = new javax.swing.JLabel();
    xTxt = new javax.swing.JTextField();
    lbY = new javax.swing.JLabel();
    yTxt = new javax.swing.JTextField();
    lbLocFrom = new javax.swing.JLabel();
    locFromCombo = new javax.swing.JComboBox<Object>(locationFromNames.toArray());
    catchMouseChk = new javax.swing.JCheckBox();
    stretchChk = new javax.swing.JCheckBox();

    setBorder(javax.swing.BorderFactory.createEtchedBorder());
    setLayout(new java.awt.BorderLayout());

    mediaTypePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(options.getMsg("edit_mc_type")));
    mediaTypePanel.setLayout(new java.awt.GridBagLayout());

    mediaTypes.add(toggleType00);
    toggleType00.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[0]));
    toggleType00.setText(options.getMsg("edit_mc_type_unknown"));
    toggleType00.setToolTipText(options.getMsg("edit_mc_type_unknown"));
    toggleType00.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType00, gridBagConstraints);

    mediaTypes.add(toggleType01);
    toggleType01.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[1]));
    toggleType01.setText(options.getMsg("edit_mc_type_sound"));
    toggleType01.setToolTipText(options.getMsg("edit_mc_type_sound"));
    toggleType01.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType01, gridBagConstraints);

    mediaTypes.add(toggleType02);
    toggleType02.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[2]));
    toggleType02.setText(options.getMsg("edit_mc_type_video"));
    toggleType02.setToolTipText(options.getMsg("edit_mc_type_video"));
    toggleType02.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType02, gridBagConstraints);

    mediaTypes.add(toggleType03);
    toggleType03.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[3]));
    toggleType03.setText(options.getMsg("edit_mc_type_midi"));
    toggleType03.setToolTipText(options.getMsg("edit_mc_type_midi"));
    toggleType03.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType03, gridBagConstraints);

    mediaTypes.add(toggleType04);
    toggleType04.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[4]));
    toggleType04.setText(options.getMsg("edit_mc_type_cdaudio"));
    toggleType04.setToolTipText(options.getMsg("edit_mc_type_cdaudio"));
    toggleType04.setEnabled(false);
    toggleType04.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType04, gridBagConstraints);

    mediaTypes.add(toggleType05);
    toggleType05.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[5]));
    toggleType05.setText(options.getMsg("edit_mc_type_record"));
    toggleType05.setToolTipText(options.getMsg("edit_mc_type_record"));
    toggleType05.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType05, gridBagConstraints);

    mediaTypes.add(toggleType06);
    toggleType06.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[6]));
    toggleType06.setText(options.getMsg("edit_mc_type_play_recorded"));
    toggleType06.setToolTipText(options.getMsg("edit_mc_type_play_recorded"));
    toggleType06.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType06, gridBagConstraints);

    mediaTypes.add(toggleType07);
    toggleType07.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[7]));
    toggleType07.setText(options.getMsg("edit_mc_type_run_activity"));
    toggleType07.setToolTipText(options.getMsg("edit_mc_type_run_activity"));
    toggleType07.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType07, gridBagConstraints);

    mediaTypes.add(toggleType08);
    toggleType08.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[8]));
    toggleType08.setText(options.getMsg("edit_mc_type_run_sequence"));
    toggleType08.setToolTipText(options.getMsg("edit_mc_type_run_sequence"));
    toggleType08.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType08, gridBagConstraints);

    mediaTypes.add(toggleType09);
    toggleType09.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[9]));
    toggleType09.setText(options.getMsg("edit_mc_type_run_external"));
    toggleType09.setToolTipText(options.getMsg("edit_mc_type_run_external"));
    toggleType09.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType09, gridBagConstraints);

    mediaTypes.add(toggleType10);
    toggleType10.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[10]));
    toggleType10.setText(options.getMsg("edit_mc_type_url"));
    toggleType10.setToolTipText(options.getMsg("edit_mc_type_url"));
    toggleType10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType10, gridBagConstraints);

    mediaTypes.add(toggleType11);
    toggleType11.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[11]));
    toggleType11.setText(options.getMsg("edit_mc_type_exit"));
    toggleType11.setToolTipText(options.getMsg("edit_mc_type_exit"));
    toggleType11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType11, gridBagConstraints);

    mediaTypes.add(toggleType12);
    toggleType12.setIcon(ResourceManager.getImageIcon(MediaContentButton.MEDIA_ICONS[12]));
    toggleType12.setText(options.getMsg("edit_mc_type_return"));
    toggleType12.setToolTipText(options.getMsg("edit_mc_type_return"));
    toggleType12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    mediaTypePanel.add(toggleType12, gridBagConstraints);

    add(mediaTypePanel, java.awt.BorderLayout.WEST);

    mainPanel.setLayout(new java.awt.GridBagLayout());

    lbLevel.setText(options.getMsg("edit_mc_level"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    mainPanel.add(lbLevel, gridBagConstraints);

    levelEditor.addPropertyChangeListener(SmallIntEditor.PROP_VALUE, this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    mainPanel.add(levelEditor, gridBagConstraints);

    loopChk.setText(options.getMsg("edit_mc_loop"));
    loopChk.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    mainPanel.add(loopChk, gridBagConstraints);

    autostartChk.setText(options.getMsg("edit_mc_autostart"));
    autostartChk.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    mainPanel.add(autostartChk, gridBagConstraints);

    filePanel.setLayout(new java.awt.CardLayout());
    filePanel.add(filePanelCard0, "card0");

    filePanelCard1.setLayout(new java.awt.GridBagLayout());

    lbFile.setText(options.getMsg("edit_mc_file"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    filePanelCard1.add(lbFile, gridBagConstraints);

    fileTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    filePanelCard1.add(fileTxt, gridBagConstraints);

    findFileBtn.setText("...");
    findFileBtn.addActionListener(this);
    filePanelCard1.add(findFileBtn, new java.awt.GridBagConstraints());

    filePanel.add(filePanelCard1, "card1");

    filePanelCard2.setLayout(new java.awt.GridBagLayout());

    lbRecbuffer.setText(options.getMsg("edit_mc_recBuffer"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    filePanelCard2.add(lbRecbuffer, gridBagConstraints);

    recBufferEditor.addPropertyChangeListener(SmallIntEditor.PROP_VALUE, this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    filePanelCard2.add(recBufferEditor, gridBagConstraints);

    lbLength.setText(options.getMsg("edit_mc_length"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    filePanelCard2.add(lbLength, gridBagConstraints);

    lengthEditor.setMax(180);
    lengthEditor.setMin(1);
    lengthEditor.setValue(5);
    lengthEditor.addPropertyChangeListener(SmallIntEditor.PROP_VALUE, this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    filePanelCard2.add(lengthEditor, gridBagConstraints);

    filePanel.add(filePanelCard2, "card2");

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    mainPanel.add(filePanel, gridBagConstraints);

    fragmentPanel.setLayout(new java.awt.CardLayout());
    fragmentPanel.add(fragmentPanelCard0, "card0");

    fragmentPanelCard1.setLayout(new java.awt.GridBagLayout());

    lbFrom.setLabelFor(fromTxt);
    lbFrom.setText(options.getMsg("edit_mc_from"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    fragmentPanelCard1.add(lbFrom, gridBagConstraints);

    fromTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    fromTxt.setPreferredSize(new java.awt.Dimension(60, 21));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    fragmentPanelCard1.add(fromTxt, gridBagConstraints);

    lbTo.setLabelFor(toTxt);
    lbTo.setText(options.getMsg("edit_mc_to"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    fragmentPanelCard1.add(lbTo, gridBagConstraints);

    toTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    toTxt.setPreferredSize(new java.awt.Dimension(60, 21));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    fragmentPanelCard1.add(toTxt, gridBagConstraints);

    fragmentPanel.add(fragmentPanelCard1, "card1");

    fragmentPanelCard2.setLayout(new java.awt.GridBagLayout());

    lbExternal.setLabelFor(externalTxt);
    lbExternal.setText(options.getMsg("edit_mc_externalParam"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    fragmentPanelCard2.add(lbExternal, gridBagConstraints);

    externalTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    fragmentPanelCard2.add(externalTxt, gridBagConstraints);

    fragmentPanel.add(fragmentPanelCard2, "card2");

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    mainPanel.add(fragmentPanel, gridBagConstraints);

    locationPanel.setLayout(new java.awt.CardLayout());
    locationPanel.add(locationPanelCard0, "card0");

    locationPanelCard1.setLayout(new java.awt.GridBagLayout());

    lpanel.setBorder(javax.swing.BorderFactory.createTitledBorder(options.getMsg("edit_mc_location")));
    lpanel.setLayout(new java.awt.GridBagLayout());

    posTypes.add(posCellRadio);
    posCellRadio.setText(options.getMsg("edit_mc_pos_cell"));
    posCellRadio.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    lpanel.add(posCellRadio, gridBagConstraints);

    posTypes.add(posWindowRadio);
    posWindowRadio.setText(options.getMsg("edit_mc_pos_window"));
    posWindowRadio.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    lpanel.add(posWindowRadio, gridBagConstraints);

    posTypes.add(posAbsoluteRadio);
    posAbsoluteRadio.setText(options.getMsg("edit_mc_pos_absolute"));
    posAbsoluteRadio.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
    lpanel.add(posAbsoluteRadio, gridBagConstraints);

    lbX.setText(options.getMsg("edit_mc_location_x"));
    lbX.setLabelFor(xTxt);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    lpanel.add(lbX, gridBagConstraints);

    xTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    xTxt.setPreferredSize(new java.awt.Dimension(60, 21));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    lpanel.add(xTxt, gridBagConstraints);

    lbY.setText(options.getMsg("edit_mc_location_y"));
    lbY.setLabelFor(yTxt);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    lpanel.add(lbY, gridBagConstraints);

    yTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    yTxt.setPreferredSize(new java.awt.Dimension(60, 21));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    lpanel.add(yTxt, gridBagConstraints);

    lbLocFrom.setText(options.getMsg("edit_mc_location_from"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    lpanel.add(lbLocFrom, gridBagConstraints);

    locFromCombo.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    lpanel.add(locFromCombo, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    locationPanelCard1.add(lpanel, gridBagConstraints);

    catchMouseChk.setText(options.getMsg("edit_mc_catchmouse"));
    catchMouseChk.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    locationPanelCard1.add(catchMouseChk, gridBagConstraints);

    stretchChk.setText(options.getMsg("edit_mc_stretch"));
    stretchChk.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    locationPanelCard1.add(stretchChk, gridBagConstraints);

    locationPanel.add(locationPanelCard1, "card1");

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    mainPanel.add(locationPanel, gridBagConstraints);

    add(mainPanel, java.awt.BorderLayout.CENTER);
  } // </editor-fold>//GEN-END:initComponents

  @Override
  public boolean documentChangePerformed(DocumentEvent ev) {
    Document doc = ev.getDocument();
    int v = 0;
    for (; v < textFields.length; v++)
      if (textFields[v].getDocument() == doc)
        break;
    if (v < textFields.length)
      return eventPerformed(new EventObject(textFields[v]));
    return false;
  }

  @Override
  public boolean eventPerformed(EventObject ev) {
    if (ev != null && ev.getSource() != null) {
      setInitializing(true);
      MediaContent oldMc = getMcClone();
      Object src = ev.getSource();
      if (src == levelEditor)
        mc.level = levelEditor.getValue();
      else if (src == recBufferEditor)
        mc.recBuffer = recBufferEditor.getValue();
      else if (src == lengthEditor)
        mc.length = lengthEditor.getValue();
      else if (src == fileTxt) {
        String s = fileTxt.getText();
        mc.mediaFileName = (s.length() > 0 ? s : null);
      } else if (src == externalTxt) {
        String s = externalTxt.getText();
        mc.externalParam = (s.length() > 0 ? s : null);
      } else if (src == fromTxt)
        mc.from = getIntValue(fromTxt, -1);
      else if (src == toTxt)
        mc.to = getIntValue(toTxt, -1);
      else if (src == stretchChk)
        mc.stretch = stretchChk.isSelected();
      else if (src == posCellRadio) {
        mc.free = false;
        mc.absLocation = null;
        syncPointValues();
        checkEnabled();
      } else if (src == posWindowRadio) {
        mc.free = true;
        mc.absLocation = null;
        syncPointValues();
        checkEnabled();
      } else if (src == posAbsoluteRadio) {
        mc.free = false;
        mc.absLocation = new java.awt.Point();
        syncPointValues();
        checkEnabled();
      } else if (src == locFromCombo)
        mc.absLocationFrom = Math.max(0, locFromCombo.getSelectedIndex());
      else if (src == xTxt && mc.absLocation != null)
        mc.absLocation.x = getIntValue(xTxt, 0);
      else if (src == yTxt && mc.absLocation != null)
        mc.absLocation.y = getIntValue(yTxt, 0);
      else if (src == loopChk)
        mc.loop = loopChk.isSelected();
      else if (src == autostartChk)
        mc.autoStart = autostartChk.isSelected();
      else if (src == catchMouseChk)
        mc.catchMouseEvents = catchMouseChk.isSelected();
      else if (src == findFileBtn) {
        selectBtnPressed();
      } else if (src instanceof JToggleButton) {
        Enumeration en = mediaTypes.getElements();
        int v = -1, j = 0;
        while (en.hasMoreElements()) {
          JToggleButton rb = (JToggleButton) en.nextElement();
          if (rb == src) {
            v = j;
            break;
          }
          j++;
        }
        if (v >= 0) {
          int currentType = mc.mediaType;
          mc.mediaType = v;
          if (mc.mediaType != currentType) {
            MediaContent mc2 = new MediaContent();
            mc2.mediaType = mc.mediaType;
            setMediaContent(mc2);
          }
        }
      }
      checkAndFirePropertyChange(oldMc);
      setInitializing(false);
    }
    return true;
  }

  protected void selectBtnPressed() {
    String value = null;
    JList<Object> list = null;
    String dlgTitleKey = null;

    if (mc.mediaType == MediaContent.RUN_CLIC_ACTIVITY) {
      list = new JList<Object>(mbe.getProjectEditor().getActivityBagEditor().getListModel());
      dlgTitleKey = "edit_mc_activity_selection";
    } else if (mc.mediaType == MediaContent.RUN_CLIC_PACKAGE) {
      list = new JList<Object>(mbe.getProjectEditor().getActivitySequenceEditor().getTagList());
      dlgTitleKey = "edit_mc_sequence_selection";
    }

    if (list != null) {
      JScrollPane pane = new JScrollPane(list);
      if (options.getMessages().showInputDlg(this, pane, dlgTitleKey)) {
        Object o = list.getSelectedValue();
        if (o != null)
          value = o.toString();
      }
    } else {
      int filter = mc.mediaType == MediaContent.PLAY_AUDIO ? Utils.ALL_SOUNDS_FF
          : mc.mediaType == MediaContent.PLAY_MIDI ? Utils.MIDI_FF
              : mc.mediaType == MediaContent.PLAY_VIDEO ? Utils.ALL_VIDEO_FF : Utils.ALL_MULTIMEDIA_FF;
      value = MediaBagSelector.getMediaName(mc.mediaFileName, options, this, mbe, filter);
    }

    if (value != null) {
      mc.mediaFileName = value;
      fileTxt.setText(value);
    }
  }

  protected void checkEnabled() {
    boolean bLevel = true, bRecBuffer = false, bLength = false, bFile = false;
    boolean bExternal = false, bFrom = false, bTo = false, bStretch = false;
    boolean bLocation = false;
    boolean bLoop = false, bAutostart = true, bCatchMouse = false;

    int filePanelItem = 0, fragmentPanelItem = 0, locationPanelItem = 0;

    switch (mc.mediaType) {
    case MediaContent.PLAY_VIDEO:
      bStretch = true;
      bLocation = true;
      bCatchMouse = true;
      locationPanelItem = 1;
    case MediaContent.PLAY_AUDIO:
    case MediaContent.PLAY_MIDI:
      bFile = true;
      bFrom = true;
      bTo = true;
      bLoop = true;
      filePanelItem = 1;
      fragmentPanelItem = 1;
      break;
    case MediaContent.PLAY_CDAUDIO:
      break;
    case MediaContent.RECORD_AUDIO:
      bLength = true;
    case MediaContent.PLAY_RECORDED_AUDIO:
      bRecBuffer = true;
      filePanelItem = 2;
      break;
    case MediaContent.RUN_CLIC_ACTIVITY:
    case MediaContent.RUN_CLIC_PACKAGE:
    case MediaContent.RUN_EXTERNAL:
      bExternal = true;
    case MediaContent.URL:
      bFile = true;
      bLevel = false;
      filePanelItem = 1;
      fragmentPanelItem = 2;
      break;
    case MediaContent.EXIT:
    case MediaContent.RETURN:
      bLevel = false;
      break;
    default:
      bLevel = false;
      break;
    }

    levelEditor.setEnabled(bLevel);
    recBufferEditor.setEnabled(bRecBuffer);
    lengthEditor.setEnabled(bLength);
    fileTxt.setEnabled(bFile);
    findFileBtn.setEnabled(bFile);
    externalTxt.setEnabled(bExternal);
    fromTxt.setEnabled(bFrom);
    toTxt.setEnabled(bTo);
    stretchChk.setEnabled(bStretch);
    posCellRadio.setEnabled(bLocation);
    posWindowRadio.setEnabled(bLocation);
    posAbsoluteRadio.setEnabled(bLocation);
    locFromCombo.setEnabled(bLocation && mc.absLocation != null);
    xTxt.setEnabled(bLocation && mc.absLocation != null);
    yTxt.setEnabled(bLocation && mc.absLocation != null);
    loopChk.setEnabled(bLoop);
    autostartChk.setEnabled(bAutostart);
    catchMouseChk.setEnabled(bCatchMouse);

    CardLayout cl = (CardLayout) filePanel.getLayout();
    cl.show(filePanel, "card" + Integer.toString(filePanelItem));
    if (filePanelItem == 1) {
      String s = mc.mediaType == MediaContent.RUN_CLIC_ACTIVITY ? "edit_mc_activity"
          : mc.mediaType == MediaContent.RUN_CLIC_PACKAGE ? "edit_mc_sequence"
              : mc.mediaType == MediaContent.URL ? "edit_mc_url" : "edit_mc_file";
      lbFile.setText(options.getMsg(s));

      boolean b = (mc.mediaType != MediaContent.URL && mc.mediaType != MediaContent.RUN_EXTERNAL);
      findFileBtn.setVisible(b);
      filePanel.revalidate();
    }

    cl = (CardLayout) fragmentPanel.getLayout();
    cl.show(fragmentPanel, "card" + Integer.toString(fragmentPanelItem));
    if (fragmentPanelItem == 2) {
      String s = mc.mediaType == MediaContent.RUN_CLIC_ACTIVITY || mc.mediaType == MediaContent.RUN_CLIC_PACKAGE
          ? "edit_mc_project"
          : "edit_mc_externalParam";
      lbExternal.setText(options.getMsg(s));
      fragmentPanel.revalidate();
    }

    cl = (CardLayout) locationPanel.getLayout();
    cl.show(locationPanel, "card" + Integer.toString(locationPanelItem));
  }

  private void syncPointValues() {
    java.awt.Point p = mc.absLocation;
    if (p == null)
      p = new java.awt.Point();
    xTxt.setText(Integer.toString(p.x));
    yTxt.setText(Integer.toString(p.y));
    locFromCombo.setSelectedIndex(mc.absLocationFrom);
  }

  protected int getIntValue(JTextField txf, int defaultValue) {
    String s = txf.getText();
    int result = defaultValue;
    try {
      result = Integer.parseInt(s);
    } catch (Exception ex) {
    }
    return result;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox autostartChk;
  private javax.swing.JCheckBox catchMouseChk;
  private javax.swing.JTextField externalTxt;
  private javax.swing.JPanel filePanel;
  private javax.swing.JTextField fileTxt;
  private javax.swing.JButton findFileBtn;
  private javax.swing.JPanel fragmentPanel;
  private javax.swing.JTextField fromTxt;
  private javax.swing.JLabel lbExternal;
  private javax.swing.JLabel lbFile;
  private javax.swing.JLabel lbFrom;
  private javax.swing.JLabel lbLength;
  private javax.swing.JLabel lbLevel;
  private javax.swing.JLabel lbLocFrom;
  private javax.swing.JLabel lbRecbuffer;
  private javax.swing.JLabel lbTo;
  private javax.swing.JLabel lbX;
  private javax.swing.JLabel lbY;
  private edu.xtec.jclic.beans.SmallIntEditor lengthEditor;
  private edu.xtec.jclic.beans.SmallIntEditor levelEditor;
  private javax.swing.JComboBox<Object> locFromCombo;
  private javax.swing.JPanel locationPanel;
  private javax.swing.JCheckBox loopChk;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JPanel mediaTypePanel;
  private javax.swing.ButtonGroup mediaTypes;
  private javax.swing.JRadioButton posAbsoluteRadio;
  private javax.swing.JRadioButton posCellRadio;
  private javax.swing.ButtonGroup posTypes;
  private javax.swing.JRadioButton posWindowRadio;
  private edu.xtec.jclic.beans.SmallIntEditor recBufferEditor;
  private javax.swing.JCheckBox stretchChk;
  private javax.swing.JTextField toTxt;
  private javax.swing.JToggleButton toggleType01;
  private javax.swing.JToggleButton toggleType02;
  private javax.swing.JToggleButton toggleType03;
  private javax.swing.JToggleButton toggleType04;
  private javax.swing.JToggleButton toggleType05;
  private javax.swing.JToggleButton toggleType06;
  private javax.swing.JToggleButton toggleType07;
  private javax.swing.JToggleButton toggleType08;
  private javax.swing.JToggleButton toggleType09;
  private javax.swing.JToggleButton toggleType10;
  private javax.swing.JToggleButton toggleType11;
  private javax.swing.JToggleButton toggleType12;
  private javax.swing.JTextField xTxt;
  private javax.swing.JTextField yTxt;
  // End of variables declaration//GEN-END:variables

  private static Map<Options, MediaContentEditor> panels = new HashMap<Options, MediaContentEditor>();

  public static MediaContent getMediaContent(MediaContent initialMc, Component parent, Options options,
      MediaBagEditor mbe) {
    MediaContent result = null;
    Messages msg = options.getMessages();
    MediaContentEditor mce = panels.get(options);
    if (mce == null) {
      mce = new MediaContentEditor(options, initialMc, mbe);
      panels.put(options, mce);
    } else {
      mce.setMediaBagEditor(mbe);
      mce.setMediaContent(initialMc);
    }
    if (msg.showInputDlg(parent, mce, "edit_mc_title") && mce.check(parent))
      result = mce.getMc();
    return result;
  }

  public static boolean nameChanged(MediaContent mc, int type, String oldName, String newName) {
    boolean result = false;
    if ((type & (Constants.T_MEDIA | Constants.T_SEQUENCE | Constants.T_URL)) != 0
        && oldName.equals(mc.mediaFileName)) {
      mc.mediaFileName = newName;
      result = true;
    }
    return result;
  }
}
