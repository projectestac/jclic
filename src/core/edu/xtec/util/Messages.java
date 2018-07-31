/*
 * File    : Messages.java
 * Created : 14-sep-2001 11:22
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.Collator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.10.29
 */
public class Messages {

  public static final String LANGUAGE = "language", COUNTRY = "country", VARIANT = "variant";
  public static final String BASIC_BUNDLE = "messages.BasicMessages";
  public static final String MESSAGES = "messages";
  public static final String ERROR = "ERROR", WARNING = "WARNING";

  public static final int OK = 0; // o
  public static final int YES = 1; // y
  public static final int RETRY = 2; // r
  public static final int NO = 3; // n
  public static final int IGNORE = 4; // i
  public static final int CANCEL = 5; // c
  public static final int YES_TO_ALL = 6; // Y
  public static final int NO_TO_ALL = 7; // N
  private static final int NUM_BUTTONS = 8;

  private static final String BTN_KEYS = "oyrnicYN";
  private static final String[] BTN_CODES = {
    "OK", "YES", "RETRY", "NO", "IGNORE", "CANCEL", "YES_TO_ALL", "NO_TO_ALL"
  };
  private String[] dlgButtons;

  public static final int MAX_PASSWORD_LENGTH = 24;
  private Locale currentLocale;
  private MultiBundle messages;
  private Collator collator;
  private java.text.NumberFormat numberFormat;
  private java.text.NumberFormat percentFormat;

  public static final String OPTIONS_DELIMITER = ",";

  public static final String[] KNOWN_LANGS = {
    "anglès",
    "arabic",
    "araucanian",
    "basque",
    "català",
    "catalán",
    "chinese",
    "english",
    "espanyol",
    "español",
    "esperanto",
    "euskara",
    "francés",
    "francès",
    "french",
    "gallego",
    "german",
    "greek",
    "inglés",
    "italian",
    "latin",
    "occità",
    "occitan",
    "portuguès",
    "romanian",
    "spanish",
    "swedish",
    "vasco"
  };
  public static final String[] KNOWN_LANG_CODES = {
    "en", "ar", "arn", "eu", "ca", "ca", "ch", "en", "es", "es", "eo", "eu", "fr", "fr", "fr", "gl",
    "de", "gr", "en", "it", "la", "oc", "oc", "pt", "ro", "es", "sv", "eu"
  };

  public Messages(String bundle) {
    init(bundle, null, null, null);
  }

  public Messages(String bundle, String options) {
    StringTokenizer st = new StringTokenizer(options, OPTIONS_DELIMITER);
    init(
        bundle,
        st.hasMoreTokens() ? st.nextToken() : null,
        st.hasMoreTokens() ? st.nextToken() : null,
        st.hasMoreTokens() ? st.nextToken() : null);
  }

  public Messages(String bundle, java.util.HashMap options) {
    init(
        bundle,
        (String) options.get(LANGUAGE),
        (String) options.get(COUNTRY),
        (String) options.get(VARIANT));
  }

  public Messages(String bundle, String language, String country, String variant) {
    init(bundle, language, country, variant);
  }

  public static Messages getMessages(HashMap<String, Object> options, String bundle) {
    Messages msg = (Messages) options.get(MESSAGES);
    if (msg == null) {
      String language = (String) options.get(LANGUAGE);
      if (language == null) {
        JOptionPane pane =
            new JOptionPane(
                "Please select your language:",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        // pane.setSelectionValues(DESCRIPTIVE_LANGUAGE_CODES);
        pane.setSelectionValues(getDescriptiveLanguageCodes(null));
        pane.setWantsInput(true);
        String initialSelection = getDescriptiveLanguageCode(Locale.getDefault().getLanguage());
        pane.setInitialSelectionValue(initialSelection);
        showDlg((Component) options.get(Options.MAIN_PARENT_COMPONENT), pane, "Language selecion");
        String sel = (String) pane.getInputValue();
        if (sel == null) {
          sel = initialSelection;
        }
        options.put(LANGUAGE, getLanguageFromDescriptive(sel));
      }
      msg = new Messages(bundle, options);
      options.put(MESSAGES, msg);
      Locale.setDefault(msg.getLocale());
    } else if (bundle != null) {
      msg.setLocale(options);
      msg.addBundle(bundle);
    }
    return msg;
  }

  private void init(String bundle, String language, String country, String variant) {
    setLocale(language, country, variant);
    addBundle(bundle);
    addBundle(BASIC_BUNDLE);
    getDlgButtons(true);
  }

  public void setLocale(java.util.HashMap options) {
    setLocale(
        (String) options.get(LANGUAGE),
        (String) options.get(COUNTRY),
        (String) options.get(VARIANT));
  }

  public void setLocale(String language, String country, String variant) {
    Locale l;
    if (country == null) {
      country = "";
    }
    if (language == null || language.length() == 0) {
      l = (currentLocale == null ? Locale.getDefault() : currentLocale);
    } else if (variant == null || variant.length() == 0) {
      l = new Locale(language, country);
    } else {
      l = new Locale(language, country, variant);
    }
    if (!l.equals(currentLocale)) {
      currentLocale = l;
      numberFormat = java.text.NumberFormat.getInstance(currentLocale);
      percentFormat = java.text.NumberFormat.getPercentInstance(currentLocale);
      collator = null;
      if (messages != null) {
        messages.setLocale(currentLocale);
        getDlgButtons(true);
      }
    }
  }

  public String[] getDlgButtons(boolean update) {
    if (update || dlgButtons == null) {
      dlgButtons = new String[NUM_BUTTONS];
      for (int i = 0; i < NUM_BUTTONS; i++) {
        dlgButtons[i] = get(BTN_CODES[i]);
      }
    }
    return dlgButtons;
  }

  public void addBundle(String bundle) {
    if (currentLocale != null && bundle != null) {
      try {
        java.util.ResourceBundle b = ResourceManager.getBundle(bundle, currentLocale);
        if (messages == null) {
          messages = new MultiBundle(b, bundle, currentLocale);
        } else {
          messages.addBundle(b, bundle, currentLocale);
        }
      } catch (Exception ex) {
        System.err.println("unable to build messagesBundle: " + bundle);
        System.err.println(ex);
      }
    }
  }

  public String getShortDateStr(Date date) {
    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, currentLocale);
    return df.format(date);
  }

  public String getShortDateTimeStr(Date date) {
    DateFormat df =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, currentLocale);
    return df.format(date);
  }

  public Date parseShortDate(String str) {
    Date result = null;
    if (str != null) {
      try {
        result = DateFormat.getDateInstance(DateFormat.SHORT, currentLocale).parse(str);
      } catch (java.text.ParseException ex) {
        // bad data!
      }
    }
    return result;
  }

  public String get(String group, String key) {
    return get(new StringBuilder(group).append(key).substring(0));
  }

  public static String get(HashMap<String, Object> options, String group, String key) {
    return getMessages(options, null).get(group, key);
  }

  public String get(String key) {
    return messages == null ? key : messages.getString(key);
  }

  public static String get(HashMap<String, Object> options, String key) {
    return getMessages(options, null).get(key);
  }

  public String getAllowNull(String key) {
    return key == null ? "" : get(key);
  }

  public String get(String key1, String s, String key2) {
    StringBuilder sb = new StringBuilder(getAllowNull(key1));
    sb.append(" ").append(s != null ? s : "").append(" ").append(getAllowNull(key2));
    return sb.substring(0);
  }

  public Locale getLocale() {
    return currentLocale;
  }

  public Collator getCollator() {
    if (collator == null) {
      collator = Collator.getInstance(currentLocale);
    }
    return collator;
  }

  protected String[] parseButtons(String btnCodes) {
    btnCodes = StrUtils.secureString(btnCodes, "o");
    String[] result = new String[btnCodes.length()];
    String[] dlg = getDlgButtons(false);
    for (int i = 0; i < btnCodes.length(); i++) {
      int k = Math.max(BTN_KEYS.indexOf(btnCodes.charAt(i)), 0);
      result[i] = dlg[k];
    }
    return result;
  }

  protected JButton[] parseJButtons(String btnCodes) {
    btnCodes = StrUtils.secureString(btnCodes, "o");
    JButton[] result = new JButton[btnCodes.length()];
    String[] dlg = getDlgButtons(false);
    for (int i = 0; i < btnCodes.length(); i++) {
      int k = Math.max(BTN_KEYS.indexOf(btnCodes.charAt(i)), 0);
      result[i] = new JButton(dlg[k]);
      result[i].setActionCommand(BTN_CODES[k]);
      // result[i].setDefaultCapable(k==OK || k==YES);
    }
    return result;
  }

  public int showQuestionDlg(Component parent, String key, String titleKey, String buttons) {
    return showQuestionDlgObj(parent, get(key), titleKey, buttons);
  }

  public int showQuestionDlgObj(Component parent, Object msg, String titleKey, String buttons) {
    NarrowOptionPane pane =
        new NarrowOptionPane(
            60,
            msg,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            parseButtons(buttons));
    String title = get(StrUtils.secureString(titleKey, "QUESTION"));
    return getFeedback(parent, pane, title);
  }

  public String showInputDlg(
      Component parent,
      String msgKey,
      String shortPromptKey,
      String initialValue,
      String titleKey,
      boolean isPassword) {
    String[] msgKeys = null;
    if (msgKey != null) {
      msgKeys = new String[] {msgKey};
    }
    return showInputDlg(parent, msgKeys, shortPromptKey, initialValue, titleKey, isPassword);
  }

  public String showInputDlg(
      Component parent,
      String[] msgKeys,
      String shortPromptKey,
      String initialValue,
      String titleKey,
      boolean isPassword) {
    String result = null;
    JTextField textField;
    if (isPassword) {
      textField = new JPasswordField(MAX_PASSWORD_LENGTH);
      if (shortPromptKey == null) {
        shortPromptKey = "PASSWORD";
      }
    } else {
      textField = new JTextField(MAX_PASSWORD_LENGTH);
    }

    if (initialValue != null) {
      textField.setText(initialValue);
    }

    if (showInputDlg(
        parent, msgKeys, new String[] {shortPromptKey}, new JComponent[] {textField}, titleKey)) {
      if (isPassword) {
        char[] pwch = ((JPasswordField) textField).getPassword();
        if (pwch != null && pwch.length > 0) {
          result = String.copyValueOf(pwch);
        }
      } else {
        result = textField.getText();
      }
    }
    return result;
  }

  public boolean showInputDlg(
      Component parent,
      String[] msgKeys,
      String[] shortPromptKeys,
      JComponent[] promptObjects,
      String titleKey) {

    ArrayList<Object> v = new ArrayList<Object>();

    if (msgKeys != null) {
      for (String msgKey : msgKeys) {
        v.add(get(msgKey));
      }
    }

    if (promptObjects != null) {
      if (shortPromptKeys == null) {
        for (JComponent jc : promptObjects) {
          v.add(jc);
        }
        // v.addAll(Arrays.asList(promptObjects));
      } else {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(3, 3, 3, 3);

        JPanel panel = new JPanel(gridBag);
        for (int i = 0; i < promptObjects.length; i++) {
          if (shortPromptKeys.length > i) {
            JLabel lb = new JLabel(get(shortPromptKeys[i]));
            lb.setLabelFor(promptObjects[i]);
            lb.setHorizontalAlignment(SwingConstants.LEFT);
            c.gridwidth = GridBagConstraints.RELATIVE;
            gridBag.setConstraints(lb, c);
            panel.add(lb);
          }
          c.gridwidth = GridBagConstraints.REMAINDER;
          gridBag.setConstraints(promptObjects[i], c);
          panel.add(promptObjects[i]);
        }
        v.add(panel);
      }
    }
    String title = (titleKey != null ? get(titleKey) : "");

    NarrowOptionPane pane =
        new NarrowOptionPane(
            60,
            v.toArray(),
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            parseButtons("oc"));
    return getFeedback(parent, pane, title) == OK;
  }

  public boolean showInputDlg(Component parent, JComponent mainComponent, String titleKey) {
    return showInputDlg(parent, mainComponent, titleKey, "oc");
  }

  public boolean showInputDlg(
      Component parent, JComponent mainComponent, String titleKey, String buttons) {
    return showInputDlg(parent, mainComponent, titleKey, buttons, false);
  }

  public boolean showInputDlg(
      Component parent,
      JComponent mainComponent,
      String titleKey,
      String buttons,
      boolean centerOnParent) {

    InputDlg dlg = new InputDlg(parent, titleKey, buttons, mainComponent, centerOnParent);
    return dlg.getFeedback() == OK;

    // String title= (titleKey!=null ? get(titleKey) : "");
    // NarrowOptionPane pane=new NarrowOptionPane(60, mainComponent, JOptionPane.PLAIN_MESSAGE,
    // JOptionPane.DEFAULT_OPTION, null, parseButtons(buttons));
    // return getFeedback(parent, pane, title)==OK;
  }

  class InputDlg extends JDialog implements java.awt.event.ActionListener {

    int result = CANCEL;
    Component parent;
    boolean centerOnParent;

    InputDlg(
        Component parent,
        String titleKey,
        String buttons,
        JComponent mainComponent,
        boolean centerOnParent) {
      // 26-jan-06 - Modified to solve bug #73, reported by Jorda Polo
      // Compile error in gcj 4.0.3:
      // "Can't reference 'this' before the superclass constructor has been called."
      // OLD CODE:
      // super(JOptionPane.getFrameForComponent(parent), titleKey!=null ? get(titleKey) : "", true);
      // NEW CODE:
      // Split in two steps:
      // 1 - call super with 'owner' and 'modal' parameters
      // 2 - if 'titleKey' not null, set title
      super(JOptionPane.getFrameForComponent(parent), true);
      if (titleKey != null) {
        setTitle(get(titleKey));
      }
      // --------

      this.parent = parent;
      this.centerOnParent = centerOnParent;

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      getContentPane().setLayout(new BorderLayout(10, 10));
      if (mainComponent != null) {
        getContentPane().add(mainComponent, BorderLayout.CENTER);
      }
      JButton[] btn = parseJButtons(buttons);
      JPanel btnPanel = new JPanel();
      JButton defaultBtn = null;
      for (JButton b : btn) {
        btnPanel.add(b);
        b.addActionListener(this);
        String cmd = b.getActionCommand();
        if (defaultBtn == null && (BTN_CODES[OK].equals(cmd) || BTN_CODES[YES].equals(cmd))) {
          defaultBtn = b;
        }
      }
      getContentPane().add(btnPanel, BorderLayout.SOUTH);
      if (defaultBtn != null) {
        getRootPane().setDefaultButton(defaultBtn);
      }
    }

    public void actionPerformed(java.awt.event.ActionEvent ev) {
      if (ev != null) {
        String cmd = ev.getActionCommand();
        for (int i = 0; i < BTN_CODES.length; i++) {
          if (BTN_CODES[i].equals(cmd)) {
            result = i;
            break;
          }
        }
        setVisible(false);
      }
    }

    public int getFeedback() {
      pack();
      Component cmp = centerOnParent ? parent : JOptionPane.getFrameForComponent(parent);
      if (cmp != null) {
        int pw = cmp.getWidth();
        int ph = cmp.getHeight();
        setLocation((pw - getWidth()) / 2, (ph - getHeight()) / 2);
        setLocationRelativeTo(cmp);
      }
      if (!showDlg(this)) {
        result = CANCEL;
      }
      return result;
    }
  }

  public void showAlert(Component parent, String key) {
    showAlert(parent, new String[] {get(key)});
  }

  public void showAlert(Component parent, String[] msg) {
    System.err.println("Warning:");
    for (String s : msg) {
      System.err.println(s);
    }
    NarrowOptionPane pane =
        new NarrowOptionPane(
            60,
            msg,
            JOptionPane.WARNING_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            parseButtons(null));
    getFeedback(parent, pane, get(WARNING));
  }

  public int showErrorWarning(Component parent, String key, Exception ex) {
    return showErrorWarning(parent, key, (List<Object>) null, ex, null);
  }

  public int showErrorWarning(Component parent, String key, Exception ex, String buttons) {
    return showErrorWarning(parent, key, (List<Object>) null, ex, buttons);
  }

  public int showErrorWarning(
      Component parent, String key, String value, Exception ex, String buttons) {
    List<Object> v = new ArrayList<Object>();
    if (value != null) {
      v.add(value);
    }
    return showErrorWarning(parent, key, v, ex, buttons);
  }

  public int showErrorWarning(
      Component parent, String key, List<Object> values, Exception ex, String buttons) {
    if (key == null) {
      key = ERROR;
    }
    List<Object> v = new ArrayList<Object>();
    String mainMsg = get(key);
    System.err.println(mainMsg);
    v.add(mainMsg);
    if (values != null) {
      Iterator it = values.iterator();
      while (it.hasNext()) {
        Object o = it.next();
        if (o != null) {
          v.add(o);
          System.err.println(o);
        }
      }
    }
    if (ex != null) {
      String s = ex.getLocalizedMessage();
      if (s != null) {
        v.add(s);
      } else {
        v.add(ex.toString());
      }
      System.err.println(s);
      ex.printStackTrace(System.err);
    }

    NarrowOptionPane pane =
        new NarrowOptionPane(
            60,
            v.toArray(),
            JOptionPane.ERROR_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            parseButtons(buttons));

    return getFeedback(parent, pane, get(ERROR));
  }

  public boolean confirmReadableFile(Component parent, File f) {
    boolean result = f.canRead();
    if (!result) {
      showAlert(
          parent,
          new String[] {
            get("FILE_BEG"),
            quote(f.getAbsolutePath()),
            get(f.exists() ? "FILE_NOT READABLE" : "FILE_NOT_EXIST")
          });
    }
    return result;
  }

  public int confirmOverwriteFile(Component parent, File f, String buttons) {
    int result = YES;
    if (f.exists()) {
      boolean dir = f.isDirectory();
      List<Object> v = new ArrayList<Object>();
      v.add(get(dir ? "FILE_DIR_BEG" : "FILE_BEG"));
      v.add(quote(f.getAbsolutePath()));
      boolean readOnly = !f.canWrite();
      if (readOnly) {
        v.add(get("FILE_READONLY"));
      } else {
        v.add(get("FILE_EXISTS"));
        v.add(get(dir ? "FILE_OVERWRITE_DIR_PROMPT" : "FILE_OVERWRITE_PROMPT"));
      }
      if (readOnly) {
        showErrorWarning(parent, ERROR, v, null, null);
        result = CANCEL;
      } else {
        NarrowOptionPane pane =
            new NarrowOptionPane(
                60,
                v.toArray(),
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                parseButtons(buttons));
        result = getFeedback(parent, pane, get("CONFIRM"));
      }
    }
    return result;
  }

  private static boolean showDlg(Component parent, JOptionPane pane, String title) {
    if (parent != null && !(parent instanceof java.awt.Frame)) {
      parent = JOptionPane.getFrameForComponent(parent);
    }
    JDialog dialog = pane.createDialog(parent, title);
    pane.selectInitialValue();
    return showDlg(dialog);
  }

  public static boolean showDlg(final JDialog dialog) {
    if (SwingUtilities.isEventDispatchThread()) // dialog.show();
    {
      dialog.setVisible(true);
    } else {
      try {
        SwingUtilities.invokeAndWait(
            new Runnable() {
              public void run() {
                // dialog.show();
                dialog.setVisible(true);
              }
            });
      } catch (Exception ex) {
        System.err.println("Show dialog error: " + ex);
        return false;
      }
    }
    return true;
  }

  protected int getFeedback(Component parent, JOptionPane pane, String title) {
    int result = CANCEL;
    if (showDlg(parent, pane, title)) {
      Object selectedValue = pane.getValue();
      if (selectedValue != null) {
        for (int i = 0; i < NUM_BUTTONS; i++) {
          if (dlgButtons[i].equals(selectedValue)) {
            result = i;
            break;
          }
        }
      }
    }
    return result;
  }

  public static String quote(String text) {
    return new StringBuilder(" \"").append(text).append("\" ").substring(0);
  }

  public String kValue(long v) {
    StringBuilder sb = new StringBuilder(20);
    sb.append(numberFormat.format(v / 1024)).append(" Kb");
    return sb.substring(0);
  }

  public String fileSize(long size) {
    StringBuilder sb = new StringBuilder(20);
    int kb = (int) size / 1024;
    double mb = ((double) size) / (1024 * 1024);
    if (kb == 0) {
      sb.append(numberFormat.format(size)).append(" bytes");
    } else if (mb < 1.0) {
      sb.append(numberFormat.format(kb)).append(" Kb");
    } else {
      int digits = numberFormat.getMaximumFractionDigits();
      numberFormat.setMaximumFractionDigits(1);
      sb.append(numberFormat.format(mb)).append(" Mb");
      numberFormat.setMaximumFractionDigits(digits);
    }
    return sb.substring(0);
  }

  public String getNumber(long v) {
    return numberFormat.format(v);
  }

  public String getNumber(double v) {
    return numberFormat.format(v);
  }

  public String getPercent(long v) {
    return percentFormat.format(((double) v) / 100);
  }

  public String getPercent(double v) {
    return percentFormat.format(v);
  }

  public String getHmsTime(long milis) {
    long v = milis / 1000;
    if (v < 1) {
      v = 1;
    }
    StringBuilder sb = new StringBuilder(50);
    if (v >= 3600) {
      sb.append(v / 3600).append("h");
    }
    if (v >= 60) {
      sb.append((v % 3600) / 60).append("'");
    }
    sb.append(v % 60).append("\"");
    return sb.substring(0);
  }

  /* Since the Java specification does not include the ISO 639-2 three-letter
   * language codes, we provide this HashMap to support it. The list will be
   * expanded as new translations of JClic where created.
   */
  public static final HashMap<String, String> ISO_639_2_CODES = new HashMap<String, String>();

  static {
    ISO_639_2_CODES.put("ast", "asturianu");
    ISO_639_2_CODES.put("vec", "vèneto");
  }

  public static String getDescriptiveLanguageCode(String languageCode) {
    String result = null;
    if (languageCode != null) {
      result = (String) ISO_639_2_CODES.get(languageCode);
      if (result == null) {
        Locale lx = new Locale(languageCode, "");
        result = lx.getDisplayName(Locale.getDefault());
      }
      result = result + " (" + languageCode + ")";
    }
    return result;
  }

  public static String getLanguageFromDescriptive(String descriptive) {
    String result = null;
    int p = -1;
    if (descriptive != null
        && (descriptive = descriptive.trim()).length() > 4
        && (p = descriptive.lastIndexOf('(')) > 0) {
      result = descriptive.substring(p + 1, descriptive.length() - 1);
    }
    return result;
  }

  private static HashMap<String, String[]> descriptiveLanguageCodes =
      new HashMap<String, String[]>();

  public static String[] getDescriptiveLanguageCodes(Locale inLocale) {
    String key = (inLocale == null ? "null" : inLocale.toString());
    String[] result = descriptiveLanguageCodes.get(key);
    if (result == null) {
      Locale dl = Locale.getDefault();
      String[] lc = Locale.getISOLanguages();
      TreeMap<String, String> tree = new TreeMap<String, String>();
      for (String l : lc) {
        String s;
        // EXCEPTIONS:
        if (inLocale == null && l.equals("eu")) {
          s = "euskara";
        } else if (inLocale == null && l.equals("gl")) {
          s = "galego";
        } else {
          Locale lx = new Locale(l, "");
          s = lx.getDisplayName(inLocale == null ? lx : dl);
        }
        tree.put(l, s + " (" + l + ")");
      }
      for (String k : ISO_639_2_CODES.keySet()) {
        String s = (String) ISO_639_2_CODES.get(k);
        tree.put(k, s + " (" + k + ")");
      }
      result = new String[tree.size()];
      Iterator it = tree.values().iterator();
      int i = 0;
      while (it.hasNext()) {
        result[i++] = (String) it.next();
      }
      descriptiveLanguageCodes.put(key, result);
    }
    return result;
  }

  public String[] getDescriptiveLanguageCodes() {
    return getDescriptiveLanguageCodes(getLocale());
  }

  public static String getKnownLanguageCode(String language) {
    String result = null, lang = (language == null) ? "" : language.toLowerCase();
    for (int i = 0; i < KNOWN_LANGS.length; i++) {
      if (KNOWN_LANGS[i].equals(lang)) {
        result = KNOWN_LANG_CODES[i];
        break;
      }
    }
    return result;
  }

  private static HashMap<String, String> codesToNames;
  private static HashMap<String, String> namesToCodes;

  private static void buildLanguageMaps() {
    String[] dlc = getDescriptiveLanguageCodes(null);
    codesToNames = new HashMap<String, String>(dlc.length);
    namesToCodes = new HashMap<String, String>(dlc.length);
    for (String c : dlc) {
      // Allow country codes with more than two letters
      // int p=dlc[i].length()-5;
      int p = c.lastIndexOf('(') - 1;
      String name = c.substring(0, p).toLowerCase();
      // String code=dlc[i].substring(p+2, p+4);
      String code = c.substring(p + 2, c.length() - 1);
      codesToNames.put(code, name);
      namesToCodes.put(name, code);
    }
    /*
       java.util.Properties prop=new java.util.Properties();
       prop.putAll(codesToNames);
       try{
           prop.store(new java.io.FileOutputStream("language_codes.properties"), "ISO 639 language codes");
       } catch(Exception ex){
           System.err.println(ex);
       }
    */
  }

  public static HashMap getCodesToNames() {
    if (codesToNames == null) {
      buildLanguageMaps();
    }
    return codesToNames;
  }

  public static HashMap getNamesToCodes() {
    if (namesToCodes == null) {
      buildLanguageMaps();
    }
    return namesToCodes;
  }

  /*
   public static final String[] DESCRIPTIVE_LANGUAGE_CODES={
       "Afar (aa)",
       "Abkhazian (ab)",
       "Avestan (ae)",
       "Afrikaans (af)",
       "Amharic (am)",
       "Arabic (ar)",
       "Assamese (as)",
       "Aymara (ay)",
       "Azerbaijani (az)",
       "Bashkir (ba)",
       "Belarusian (be)",
       "Bulgarian (bg)",
       "Bihari (bh)",
       "Bislama (bi)",
       "Bengali (bn)",
       "Tibetan (bo)",
       "Breton (br)",
       "Bosnian (bs)",
       "Catal\u00E0 (ca)",
       "Chechen (ce)",
       "Chamorro (ch)",
       "Corsican (co)",
       "Czech (cs)",
       "Church Slavic (cu)",
       "Chuvash (cv)",
       "Welsh (cy)",
       "Danish (da)",
       "German (de)",
       "Dzongkha (dz)",
       "Greek, Modern (el)",
       "English (en)",
       "Esperanto (eo)",
       "Espa\u00F1ol (es)",
       "Estonian (et)",
       "Euskara (eu)",
       "Persian (fa)",
       "Finnish (fi)",
       "Fijian (fj)",
       "Faroese (fo)",
       "French (fr)",
       "Frisian (fy)",
       "Irish (ga)",
       "Gaelic (gd)",
       "Galego (gl)",
       "Guarani (gn)",
       "Gujarati (gu)",
       "Manx (gv)",
       "Hausa (ha)",
       "Hebrew (he)",
       "Hindi (hi)",
       "Hiri Motu (ho)",
       "Croatian (hr)",
       "Hungarian (hu)",
       "Armenian (hy)",
       "Herero (hz)",
       "Interlingua (ia)",
       "Indonesian (id)",
       "Interlingue (ie)",
       "Inupiaq (ik)",
       "Ido (io)",
       "Icelandic (is)",
       "Italian (it)",
       "Inuktitut (iu)",
       "Japanese (ja)",
       "Javanese (jv)",
       "Georgian (ka)",
       "Kikuyu (ki)",
       "Kwanyama (kj)",
       "Kazakh (kk)",
       "Kalaallisut (kl)",
       "Khmer (km)",
       "Kannada (kn)",
       "Korean (ko)",
       "Kashmiri (ks)",
       "Kurdish (ku)",
       "Komi (kv)",
       "Cornish (kw)",
       "Kirghiz (ky)",
       "Latin (la)",
       "Letzeburgesch (lb)",
       "Lingala (ln)",
       "Lao (lo)",
       "Lithuanian (lt)",
       "Latvian (lv)",
       "Malagasy (mg)",
       "Marshallese (mh)",
       "Maori (mi)",
       "Macedonian (mk)",
       "Malayalam (ml)",
       "Mongolian (mn)",
       "Moldavian (mo)",
       "Marathi (mr)",
       "Malay (ms)",
       "Maltese (mt)",
       "Burmese (my)",
       "Nauru (na)",
       "Norwegian Bokm\u00E5l (nb)",
       "North Ndebele (nd)",
       "Nepali (ne)",
       "Ndonga (ng)",
       "Dutch (nl)",
       "Norwegian Nynorsk (nn)",
       "Norwegian (no)",
       "South Ndebele (nr)",
       "Navajo (nv)",
       "Nyanja (ny)",
       "Occitan (oc)",
       "Oromo (om)",
       "Oriya (or)",
       "Ossetic (os)",
       "Panjabi (pa)",
       "Pali (pi)",
       "Polish (pl)",
       "Pushto (ps)",
       "Portuguese (pt)",
       "Quechua (qu)",
       "Raeto-Romance (rm)",
       "Rundi (rn)",
       "Romanian (ro)",
       "Russian (ru)",
       "Kinyarwanda (rw)",
       "Sanskrit (sa)",
       "Sardinian (sc)",
       "Sindhi (sd)",
       "Northern Sami (se)",
       "Sango (sg)",
       "Sinhalese (si)",
       "Slovak (sk)",
       "Slovenian (sl)",
       "Samoan (sm)",
       "Shona (sn)",
       "Somali (so)",
       "Albanian (sq)",
       "Serbian (sr)",
       "Swati (ss)",
       "Sotho (st)",
       "Sundanese (su)",
       "Swedish (sv)",
       "Swahili (sw)",
       "Tamil (ta)",
       "Telugu (te)",
       "Tajik (tg)",
       "Thai (th)",
       "Tigrinya (ti)",
       "Turkmen (tk)",
       "Tagalog (tl)",
       "Tswana (tn)",
       "Tonga (to)",
       "Turkish (tr)",
       "Tsonga (ts)",
       "Tatar (tt)",
       "Twi (tw)",
       "Tahitian (ty)",
       "Uighur (ug)",
       "Ukrainian (uk)",
       "Urdu (ur)",
       "Uzbek (uz)",
       "Vietnamese (vi)",
       "Volap\u00FCk (vo)",
       "Walloon (wa)",
       "Wolof (wo)",
       "Xhosa (xh)",
       "Yiddish (yi)",
       "Yoruba (yo)",
       "Zhuang (za)",
       "Chinese (zh)",
       "Zulu (zu)"
   };
  */
}
