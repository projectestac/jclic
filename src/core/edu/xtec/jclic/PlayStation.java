/*
 * File    : PlayStation.java
 * Created : 16-sep-2002 12:31
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

/**
 * A <CODE>PlayStation</CODE> is a container for activities. All the classes
 * capables of showing and running JClic activities must implement this
 * interface. It describes the necessary methods to make JClic activities work.
 * These methods are called by the activities to get resources, play media or
 * notify events.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 */
public interface PlayStation extends edu.xtec.util.ResourceBridge {

    /**
     * The action corresponding to this index. The indexes of actions are defined as
     * constants in <CODE>edu.xtec.jclic.Constants</CODE>.
     *
     * @param id The index for the requested action.
     * @return The action coresponding to this index, or null if it does not exist.
     */
    public javax.swing.Action getAction(int id);

    /**
     * Sets the content of the messages area of the player.
     *
     * @param abc The content to show in the message box.
     */
    public void setMsg(edu.xtec.jclic.boxes.ActiveBoxContent abc);

    /**
     * Performs or executes the media associated with the current content of the
     * player's message box, if any.
     */
    public void playMsg();

    /**
     * Sets the text of the system messages, usually displayed in the status bar of
     * the player. There are two system messages in JClic: The main message, that
     * describes the error or action being performed, and the secondary message,
     * used to complement the main information with filenames or other details.
     *
     * @param msg1 Text of the main message.
     * @param msg2 Text of the secondary message.
     */
    public void setSystemMessage(String msg1, String msg2);

    /**
     * Enables or disables the wait cursor (hourglass). This method uses a counter
     * that increments or decrements as called with <CODE>true</CODE> or
     * <CODE>false</CODE> params. The hourglass is maintained while the counter
     * remains &gt;0.
     *
     * @param state To enable or disable the wait cursor.
     */
    public void setWaitCursor(boolean state);

    /**
     * Obtains the <CODE>Cursor</CODE> resource of the specified type. Identifiers
     * for all the available cursors are defined in
     * <CODE>edu.xtec.jclic.Constants</CODE>.
     *
     * @param type The type of cursor requested (hand, stop, record...)
     * @return The {@link java.awt.Cursor} resource, or null if it does no exist.
     */
    public java.awt.Cursor getCustomCursor(int type);

    /**
     * Provides an appropiated <CODE>MediaPlayer</CODE> suitable for a specific
     * media.
     *
     * @param mediaContent The <CODE>MediaContent</CODE> to be used in this
     *                     <CODE>MediaPlayer</CODE>.
     * @return A <CODE>MediaPlayer</CODE> useful for this <CODE>MediaContent</CODE>.
     */
    public edu.xtec.jclic.media.ActiveMediaPlayer getActiveMediaPlayer(edu.xtec.jclic.media.MediaContent mediaContent);

    /**
     * Plays, performs or executes a MediaContent. If the media has graphical
     * content, the specified ActiveBox will be used to display it.
     *
     * @param mediaContent   The <CODE>MediaContent</CODE> to be performed.
     * @param mediaPlacement The <CODE>ActiveBox</CODE> where the graphical content
     *                       of the media (if any) will be displayed.
     */
    public void playMedia(edu.xtec.jclic.media.MediaContent mediaContent,
            edu.xtec.jclic.boxes.ActiveBox mediaPlacement);

    /**
     * Stops all the media content currently playing that has a priority level equal
     * or below the specified.
     *
     * @param level The priority level under wich all media contents will be halted,
     *              or -1 to stop all media.
     */
    public void stopMedia(int level);

    /**
     * Enables or disables the specified counter.
     *
     * @param counterId The identifier of the counter to enable or disable, as
     *                  specified in <CODE>
     *     edu.xtec.jclic.Constants</CODE>.
     * @param bEnabled  <CODE>true</CODE> to enable the counter, <CODE>false</CODE>
     *                  otherwise.
     */
    public void setCounterEnabled(int counterId, boolean bEnabled);

    /**
     * Changes the mode of operation of the specified counter to count down,
     * starting by the indicated value and stopping at zero.
     *
     * @param counterId The identifier of the counter, as specified in
     *                  <CODE>edu.xtec.jclic.Constants
     *     </CODE>   .
     * @param maxValue  The value to initially assign to the counter.
     */
    public void setCountDown(int counterId, int maxValue);

    /**
     * Increments the value of the specified counter, or decrements it when in
     * <I>countDown</I> mode.
     *
     * @param counterId The identifier of the counter, as specified in
     *                  <CODE>edu.xtec.jclic.Constants
     *     </CODE>   .
     */
    public void incCounterValue(int counterId);

    /**
     * Sets the specified counter to a specific value.
     *
     * @param counterId The identifier of the counter, as specified in
     *                  <CODE>edu.xtec.jclic.Constants
     *     </CODE>   .
     * @param newValue  The value to be assigned to the counter.
     */
    public void setCounterValue(int counterId, int newValue);

    /**
     * Gets the current value of the specified counter.
     *
     * @param counterId The identifier of the counter, as specified in
     *                  <CODE>edu.xtec.jclic.Constants
     *     </CODE>   .
     * @return The current value of the conuter.
     */
    public int getCounterValue(int counterId);

    /**
     * Turns on the specified {@link edu.xtec.jclic.Activity.Panel}.
     *
     * @param actp The activity panel that will be started.
     */
    public void startActivity(Activity.Panel actp);

    /**
     * Activity panels use this method to notify Players that the user has finished
     * the current activity.
     *
     * @param completedOk <CODE>true</CODE> if the activity was successfully
     *                    finished.
     */
    public void activityFinished(boolean completedOk);

    /**
     * Notifies the player that the activity has been started.
     *
     * @param act          The {@link Activity} that has been started.
     * @param currentScore The score at the beggining of the activity (usually zero,
     *                     but can be different in some cases)
     */
    public void reportNewActivity(Activity act, int currentScore);

    /**
     * Notifies the {@link edu.xtec.jclic.report.Reporter} (if any) that an action
     * has been performed. This method is only used by activities that explicity
     * have the <CODE>reportActions</CODE> flag set.
     *
     * @param act          The activity in wich panel the action has been performed.
     * @param type         The type of action, as defined in {@link Activity}.
     * @param source       The description of the object (cell, word, letter...) in
     *                     wich the action has been performed.
     * @param dest         If the action involves more than one object, the
     *                     description of the second one.
     * @param ok           <CODE>true</CODE> if the action was successful,
     *                     <CODE>false</CODE> otherwise.
     * @param currentScore Score obtained just after the action.
     */
    public void reportNewAction(Activity act, String type, String source, String dest, boolean ok, int currentScore);

    /**
     * Notifies the {@link edu.xtec.jclic.report.Reporter} (if any) that an action
     * has been finished.
     *
     * @param act    The Activity yhat has been finished.
     * @param solved <CODE>true</CODE> if the activity was solved,
     *               <CODE>false</CODE> otherwise.
     */
    public void reportEndActivity(Activity act, boolean solved);

    /**
     * Instructs the Player to show a help dialog window displaying a content
     * associated with the current activity.
     *
     * @param hlpComponent The {@link javax.swing.JComponent} to be placed in the
     *                     help dialog, or <CODE>null</CODE> if no special content
     *                     must be used. Activities usually use
     *                     {@link edu.xtec.jclic.HelpActivityComponent} objects for
     *                     this param.
     * @param hlpMsg       A message to be displayed on the help window, usually
     *                     used in place of a specific JComponent. This parameter
     *                     can be <CODE>null</CODE>.
     * @return <CODE>true</CODE> if the user clicks on the <B>OK</B> button of the
     *         help dialog, <CODE>
     *     false</CODE> otherwise.
     */
    public boolean showHelp(javax.swing.JComponent hlpComponent, String hlpMsg);

    /**
     * FressaFunctions offers special accessibility features like atomatic scanning
     * and voice synthesis.
     *
     * @return The FressaFunctions object, or <CODE>null</CODE> if accessibility
     *         features are not enabled
     */
    public edu.xtec.jclic.accessibility.FressaFunctions getFressa();
}
