/*
 * File    : StreamIO.java
 * Created : 06-feb-2001 18:30
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
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class StreamIO {

  private static boolean cancel = false;
  // public static final int DEFAULT_READ_STEP_SIZE=1024;
  public static final int DEFAULT_READ_STEP_SIZE = 4096;

  private StreamIO() {}

  public static void setCancel(boolean value) {
    cancel = value;
  }

  public interface InputStreamListener {

    void notify(InputStream in, int bytesRead);
  }

  public static byte[] readInputStream(InputStream is) throws IOException {
    return readInputStream(is, null, DEFAULT_READ_STEP_SIZE);
  }

  public static byte[] readInputStream(InputStream is, InputStreamListener lst, int stepSize)
      throws IOException {
    cancel = false;
    BufferedInputStream bufferedStream;
    if (is instanceof BufferedInputStream) {
      bufferedStream = (BufferedInputStream) is;
    } else {
      bufferedStream = new BufferedInputStream(is);
    }

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buffer = new byte[stepSize];
    while (!cancel) {
      // bytesRead=is.read(buffer);
      int bytesRead = bufferedStream.read(buffer);
      if (lst != null) {
        lst.notify(is, bytesRead);
      }
      if (bytesRead <= 0) {
        break;
      }
      os.write(buffer, 0, bytesRead);
      Thread.yield();
    }
    buffer = os.toByteArray();
    os.close();
    // is.close();
    bufferedStream.close();
    if (cancel) {
      throw new InterruptedIOException("Cancelled by user");
    }
    return buffer;
  }

  public static byte[] readFile(File file) throws IOException {
    return readFile(file, null, 0);
  }

  public static byte[] readFile(File file, InputStreamListener lst, int stepSize)
      throws IOException {
    cancel = false;
    long fileLength = file.length();
    if (
    /*fileLength<1 ||*/ fileLength > Integer.MAX_VALUE) {
      throw new IOException();
    }
    int intFileLength = (int) fileLength;
    byte[] result = new byte[intFileLength];
    BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
    int bytesRead = 0;
    for (int k = 0; k < fileLength && !cancel; k += bytesRead) {
      int bytesToRead = intFileLength - k;
      if (stepSize > 0 && stepSize < bytesToRead) {
        bytesToRead = stepSize;
      }
      bytesRead = fis.read(result, k, bytesToRead);
      if (bytesRead < 0) {
        break;
      }
      if (lst != null) {
        lst.notify(fis, bytesRead);
      }
      Thread.yield();
    }
    fis.close();
    if (cancel) {
      throw new InterruptedIOException("Cancelled by user");
    }
    return result;
  }

  public static byte[] getResourceBytes(Object caller, String packageName, String resourceName)
      throws IOException {
    return readInputStream(caller.getClass().getResourceAsStream(packageName + "/" + resourceName));
  }

  public static long writeStreamTo(InputStream is, OutputStream os) throws IOException {
    return writeStreamTo(is, os, null, DEFAULT_READ_STEP_SIZE);
  }

  public static long writeStreamTo(
      InputStream is, OutputStream os, InputStreamListener lst, int stepSize) throws IOException {
    cancel = false;
    long result = 0;
    BufferedInputStream bufferedStream;
    if (is instanceof BufferedInputStream) {
      bufferedStream = (BufferedInputStream) is;
    } else {
      bufferedStream = new BufferedInputStream(is);
    }

    int bytesRead;
    byte[] buf = new byte[stepSize];
    while (!cancel) {
      bytesRead = bufferedStream.read(buf, 0, stepSize);
      if (lst != null) {
        lst.notify(is, bytesRead);
      }
      if (bytesRead <= 0) {
        break;
      }
      os.write(buf, 0, bytesRead);
      result += bytesRead;
      Thread.yield();
    }
    bufferedStream.close();
    os.flush();
    os.close();
    if (cancel) {
      throw new InterruptedIOException("Cancelled by user");
    }
    return result;
  }

  public interface InputStreamProvider {

    java.io.InputStream getInputStream(String resourceName) throws Exception;
  }

  public static boolean writeStreamDlg(
      final InputStream is,
      final OutputStream os,
      int knownSize,
      String mainMsg,
      Component dlgOwner,
      Options options) {

    final Messages msg = options.getMessages();
    String title = msg.get("WRITING_FILE");
    JDialog dialog;
    if (dlgOwner instanceof java.awt.Dialog) {
      dialog = new JDialog((java.awt.Dialog) dlgOwner, title, true);
    } else if (dlgOwner != null) {
      dialog = new JDialog(JOptionPane.getFrameForComponent(dlgOwner), title, true);
    } else {
      dialog = new JDialog((JFrame) null, title, true);
    }

    if (mainMsg != null) {
      dialog.getContentPane().add(new JLabel(mainMsg));
    }

    final JProgressBar progress = new JProgressBar();

    if (knownSize > 0) {
      progress.setMinimum(0);
      progress.setMaximum(knownSize);
      progress.setIndeterminate(false);
    } else {
      progress.setIndeterminate(true);
    }

    InputStreamListener isl =
        new InputStreamListener() {
          public void notify(InputStream in, int bytesRead) {
            progress.setValue(bytesRead);
          }
        };
    dialog.getContentPane().add(progress);

    final InputStreamListener iis = isl;

    JButton cancelButton = new JButton(msg.get("CANCEL"));
    cancelButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            cancel = true;
          }
        });
    dialog.getContentPane().add(cancelButton);

    dialog.pack();

    final JDialog dlg = dialog;
    edu.xtec.util.SwingWorker sw =
        new edu.xtec.util.SwingWorker() {
          private boolean result = false;

          @Override
          public Object construct() {
            try {
              writeStreamTo(is, os, iis, DEFAULT_READ_STEP_SIZE);
              result = !cancel;
            } catch (Exception ex) {
              result = false;
              msg.showErrorWarning(dlg, "ERROR", ex);
            }
            dlg.setVisible(false);
            return result;
          }
        };
    sw.startLater();
    dialog.setVisible(true);
    return ((Boolean) sw.get()).booleanValue();
  }

  public static Object cloneObject(Serializable obj) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
    ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(obj);
    out.close();
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream in = new ObjectInputStream(bais);
    return in.readObject();
  }
}
