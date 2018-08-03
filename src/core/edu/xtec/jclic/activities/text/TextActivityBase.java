/*
 * File    : TextActivitybase.java
 * Created : 28-may-2001 10:19
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

package edu.xtec.jclic.activities.text;

import edu.xtec.jclic.*;
import edu.xtec.jclic.automation.TextActivityContentKit;
import edu.xtec.jclic.boxes.*;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.StrUtils;
import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.text.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class TextActivityBase extends Activity implements TextActivityContentKit.Compatible {

  protected TextActivityDocument tad;
  protected StyleContext styleContext;
  protected boolean hasCheckButton;
  protected String checkButtonText;
  protected boolean prevScreen;
  protected int prevScreenMaxTime;
  protected String prevScreenText;
  protected BoxBase prevScreenStyle;

  // transient properties
  protected StyledDocument prevScreenDocument;

  /** Creates new TextActivityBase */
  public TextActivityBase(JClicProject project) {
    super(project);
    styleContext = new StyleContext();
    MutableAttributeSet mas = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
    if (mas != null) {
      StyleConstants.setFontFamily(mas, BoxBase.getDefaultFont().getFamily());
      StyleConstants.setBackground(mas, Color.white);
    }
    tad = new TextActivityDocument(styleContext);

    hasCheckButton = false;
    checkButtonText = "";
    prevScreen = false;
    prevScreenMaxTime = 0;
    prevScreenText = null;
    prevScreenDocument = null;
    prevScreenStyle = new BoxBase();
  }

  public static final String CHECK_BUTTON = "checkButton", PREV_SCREEN = "prevScreen", TEXT = "text";

  @Override
  public org.jdom.Element getJDomElement() {
    org.jdom.Element e = super.getJDomElement();
    if (hasCheckButton)
      e.addContent(new org.jdom.Element(CHECK_BUTTON).setText(checkButtonText));
    if (prevScreen) {
      org.jdom.Element child = new org.jdom.Element(PREV_SCREEN);
      if (prevScreenMaxTime > 0)
        child.setAttribute(MAX_TIME, Integer.toString(prevScreenMaxTime));
      if (prevScreenText != null) {
        child.addContent(prevScreenStyle.getJDomElement());
        JDomUtility.setParagraphs(child, prevScreenText);
      }
      e.addContent(child);
    }
    try {
      e.addContent(tad.getJDomElement());
    } catch (Exception ex) {
      System.err.println("Error getting document contents!");
      return null;
    }
    return e;
  }

  @Override
  public void setProperties(org.jdom.Element e, Object aux) throws Exception {
    org.jdom.Element child;
    super.setProperties(e, aux);
    if ((child = e.getChild(CHECK_BUTTON)) != null) {
      hasCheckButton = true;
      checkButtonText = child.getText();
    }
    if ((child = e.getChild(PREV_SCREEN)) != null) {
      prevScreen = true;
      if ((prevScreenText = JDomUtility.getParagraphs(child)) != null) {
        prevScreenStyle = BoxBase.getBoxBase(child.getChild(BoxBase.ELEMENT_NAME));
        prevScreenDocument = new DefaultStyledDocument();
        TextActivityDocument.boxBaseToStyledDocument(prevScreenStyle, prevScreenDocument);
        prevScreenDocument.insertString(0, prevScreenText, null);
      }
      prevScreenMaxTime = JDomUtility.getIntAttr(child, MAX_TIME, prevScreenMaxTime);
    }
    setTextActivityDocument(e.getChild(TextActivityDocument.ELEMENT_NAME));
  }

  public void setTextActivityDocument(org.jdom.Element e) throws Exception {
    tad = TextActivityDocument.getTextActivityDocument(e, this);
  }

  @Override
  public void setProperties(edu.xtec.jclic.clic3.Clic3Activity c3a) throws Exception {
    super.setProperties(c3a);
    setWindowSize(new Dimension(c3a.txtCW, c3a.txtCH));
    hasCheckButton = c3a.btCorregir;
    checkButtonText = c3a.txBtCorregir;
    if (c3a.avPrevHelp > 0) {
      prevScreen = true;
      prevScreenMaxTime = c3a.avTimePH;
      if (c3a.avPrevHelp == 2) {
        prevScreenText = c3a.txPrev;
        prevScreenStyle = c3a.getBoxBase(3);
      }
      messages[PREVIOUS] = new ActiveBoxContent();
      messages[PREVIOUS].setBoxBase(messages[MAIN].bb);
      c3a.setActiveBoxTextContent(messages[PREVIOUS], c3a.initMessPrev);
    }
    tad.readClic3Data(c3a, this);
  }

  public void setStyleContext(StyleContext sc) {
    if (sc != null) {
      try {
        setTextActivityDocument(tad.getJDomElement(sc));
        styleContext = sc;
      } catch (Exception ex) {
        System.err.println("Error updating document styles:\n" + ex);
      }
    }
  }

  public StyleContext getStyleContext() {
    return styleContext;
  }

  protected Evaluator buildClic3Evaluator(edu.xtec.jclic.clic3.Clic3Activity c3a, boolean complex) throws Exception {
    org.jdom.Element e = new org.jdom.Element(Evaluator.ELEMENT_NAME);
    String evaluatorClassName = complex ? ComplexEvaluator.class.getName() : BasicEvaluator.class.getName();
    e.setAttribute(JDomUtility.CLASS, evaluatorClassName);
    e.setAttribute(BasicEvaluator.CHECK_CASE, JDomUtility.boolString(c3a.avMaj));
    e.setAttribute(BasicEvaluator.CHECK_ACCENTS, JDomUtility.boolString(c3a.avAcc));
    e.setAttribute(BasicEvaluator.CHECK_PUNCTUATION, JDomUtility.boolString(c3a.avPunt));
    e.setAttribute(BasicEvaluator.CHECK_DOUBLE_SPACES, JDomUtility.boolString(c3a.avDblSpc));
    if (complex) {
      e.setAttribute(ComplexEvaluator.DETAIL, JDomUtility.boolString(c3a.avLletra));
      e.setAttribute(ComplexEvaluator.CHECK_STEPS, Integer.toString(c3a.avScope));
      e.setAttribute(ComplexEvaluator.CHECK_SCOPE, Integer.toString(c3a.avMaxScope));
    }
    return Evaluator.getEvaluator(e, c3a.project);
  }

  public int getMinNumActions() {
    return (tad == null ? 0 : tad.tmb.size());
  }

  @Override
  public boolean prepareMedia(PlayStation ps) {
    if (!super.prepareMedia(ps))
      return false;

    if (tad != null) {
      tad.boxesContent.prepareMedia(ps);
      tad.popupsContent.prepareMedia(ps);
    }

    return true;
  }

  @Override
  public boolean helpSolutionAllowed() {
    return true;
  }

  public Activity.Panel getActivityPanel(PlayStation ps) {
    return new Panel(ps);
  }

  public class Panel extends Activity.Panel implements java.awt.event.ActionListener {

    JScrollPane scrollPane = null;
    JButton checkButton = null;
    TextActivityPane pane = null;
    boolean showingPrevScreen = false;
    javax.swing.Timer prevScreenTimer = null;
    TextTarget.PopupTimer popupTimer = null;

    protected Panel(PlayStation ps) {
      super(ps);
      popupTimer = new TextTarget.PopupTimer(this);
    }

    public void clear() {
      if (prevScreenTimer != null) {
        prevScreenTimer.stop();
        prevScreenTimer = null;
      }
    }

    @Override
    public void doLayout() {
      int w = getWidth(), h = getHeight();
      if (checkButton != null && checkButton.isVisible()) {
        int hb = checkButton.getPreferredSize().height;
        h -= hb;
        checkButton.setBounds(0, h, w, hb);
      }
      if (scrollPane != null && pane != null) {
        if (pane.getBounds().isEmpty())
          pane.setBounds(0, 0, w, h);
        scrollPane.setBounds(0, 0, w, h);
      }
    }

    @Override
    public void buildVisualComponents() throws Exception {

      super.buildVisualComponents();

      if (acp != null) {
        TextActivityContentKit kit = new TextActivityContentKit(tad, checkButtonText, prevScreenText);
        if (acp.generateContent(kit, ps)) {
          checkButtonText = kit.checkButtonText;
          prevScreenText = kit.prevScreenText;
        }
      }

      if (prevScreen == true && prevScreenText != null) {
        prevScreenDocument = new DefaultStyledDocument();
        TextActivityDocument.boxBaseToStyledDocument(prevScreenStyle, prevScreenDocument);
        try {
          prevScreenDocument.insertString(0, prevScreenText, null);
        } catch (Exception e) {
          System.err.println("Error displaying initial screen:\n" + e);
        }
      }

      pane = buildPane();

      scrollPane = new JScrollPane(pane);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      scrollPane.getVerticalScrollBar().setBorder(BorderFactory.createLineBorder(Color.darkGray, 1));
      add(scrollPane);

      if (hasCheckButton) {
        checkButton = new JButton(StrUtils.secureString(checkButtonText, " "));
        checkButton.setOpaque(false);
        checkButton.addActionListener(this);
        add(checkButton);
      }

      if (prevScreen && prevScreenMaxTime > 0) {
        prevScreenTimer = new javax.swing.Timer(1000 * prevScreenMaxTime, this);
        prevScreenTimer.setRepeats(false);
      }
    }

    protected TextActivityPane buildPane() {
      return new TextActivityPane(this);
    }

    @Override
    public void initActivity() throws Exception {
      if (prevScreen)
        preInitActivity();
      else
        startActivity();
    }

    @Override
    public void startActivity() throws Exception {
      super.initActivity();
      showingPrevScreen = false;
      setAndPlayMsg(MAIN, EventSounds.START);
      initDocument();
      if (checkButton != null)
        checkButton.setVisible(true);
      ps.playMsg();
      pane.requestFocus();
      playing = true;
    }

    public void preInitActivity() {
      if (messages[PREVIOUS] == null || prevScreen == false)
        return;
      showingPrevScreen = true;
      if (checkButton != null)
        checkButton.setVisible(false);
      enableCounters(true, false, false);
      ps.setCounterValue(TIME_COUNTER, 0);

      ps.setMsg(messages[PREVIOUS]);
      pane.setEditable(false);
      pane.setStyledDocument(prevScreenDocument != null ? prevScreenDocument : tad);
      if (prevScreenTimer != null) {
        ps.setCountDown(TIME_COUNTER, prevScreenMaxTime);
        prevScreenTimer.start();
      }
      ps.playMsg();
    }

    protected void initDocument() throws Exception {
      if (tad != null) {
        if (pane.getDocument() != tad)
          pane.setStyledDocument(tad);
        pane.setEnabled(true);
      }
    }

    public void render(Graphics2D g2, Rectangle dirtyRegion) {
    }

    @Override
    public void requestFocus() {
      if (playing && pane != null)
        pane.requestFocus();
    }

    public Dimension setDimension(Dimension desiredMaxSize) {
      return new Dimension(Math.min(desiredMaxSize.width, getWindowSize().width),
          Math.min(desiredMaxSize.height, getWindowSize().height));
    }

    @Override
    public void setCursor(Cursor cursor) {
      if (pane != null)
        pane.setCursor(cursor);
      super.setCursor(cursor);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (checkButton != null && e.getSource() == checkButton) {
        if (playing)
          doCheck(true);
      }
      if (prevScreenTimer != null && e.getSource() == prevScreenTimer && showingPrevScreen) {
        ps.startActivity(this);
      }
    }

    @Override
    public void forceFinishActivity() {
      if (playing)
        doCheck(false);
    }

    protected void doCheck(boolean fromButton) {
    }

    @Override
    public void showHelp() {
      if (!helpWindowAllowed() || pane == null)
        return;
      HelpActivityComponent hac = null;
      if (showSolution) {
        hac = new HelpActivityComponent(this) {
          TextActivityPane tap = null;
          JScrollPane jsp = null;

          public void render(Graphics2D g2, Rectangle dirtyRegion) {
          }

          @Override
          public void init() {
            tap = buildPane();
            try {
              // build new document
              tap.setStyledDocument(
                  TextActivityDocument.getTextActivityDocument(tad.getJDomElement(), TextActivityBase.this));
            } catch (Exception ex) {
              // reuse existing document (lost floating elements)
              tap.setStyledDocument(tad);
            }
            // Modified 04-Feb-2011
            // Correction of bug #64:
            // Leave the component not editable but enabled:
            tap.setEditable(false);
            // tap.setEnabled(false);
            // -------------------
            jsp = new JScrollPane(tap);
            jsp.setBorder(BorderFactory.createEmptyBorder());
            jsp.getVerticalScrollBar().setBorder(BorderFactory.createLineBorder(Color.darkGray, 1));
            add(jsp);
            Dimension size = scrollPane.getBounds().getSize();
            jsp.setBounds(0, 0, size.width, size.height);
            if (tap.getBounds().isEmpty())
              tap.setBounds(0, 0, size.width, size.height);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);
          }
        };
        hac.init();
      }
      if (ps.showHelp(hac, helpMsg))
        ps.reportNewAction(getActivity(), ACTION_HELP, null, null, false, -1);
      if (hac != null)
        hac.end();
    }
  }

  public TextActivityDocument getDocument() {
    return tad;
  }
}
