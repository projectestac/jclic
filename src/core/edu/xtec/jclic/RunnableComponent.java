/*
 * File    : RunnableComponent.java
 * Created : 26-jun-2002 11:57
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
 * Interface used to represent complex objects that can be handled by containers
 * of type {@link edu.xtec.jclic.SingleInstanceJFrame}.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public interface RunnableComponent {

  /**
   * Instructs this <CODE>RunnableComponent</CODE> to be self-placed into the
   * specified {@link javax.swing.RootPaneContainer}.
   *
   * @param cont        The container in wich the component should be placed.
   * @param constraints The constraints used in the call to the <CODE>add</CODE>
   *                    method of the <CODE>Container</CODE>. This parameter can
   *                    be <CODE>null</CODE>.
   */
  public void addTo(javax.swing.RootPaneContainer cont, Object constraints);

  /**
   * Instructs the <CODE>RunnableComponent</CODE> to start working.
   *
   * @param param1 First parameter passed to the component. Can be
   *               <CODE>null</CODE>.
   * @param param2 Second parameter passed to the component. Can also be
   *               <CODE>null</CODE>.
   * @return <CODE>true</CODE> if the component has been turned on,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean start(String param1, String param2);

  /** Instructs the RunnableComponent to stop working. */
  public void stop();

  /**
   * Notifies the component that the main program will be halted soon, and all
   * resources must be freed.
   */
  public void end();

  /**
   * This method is called when the container gains the focus for the first time
   * or after have lost it.
   */
  public void activate();

  /**
   * A new instance of the <CODE>RunnableComponent</CODE> has been requested by
   * the user.
   *
   * @param param1 First parameter passed to the component. Can be
   *               <CODE>null</CODE>.
   * @param param2 Second parameter passed to the component. Can also be
   *               <CODE>null</CODE>.
   * @return <CODE>true</CODE> if the request was successfull, <CODE>false</CODE>
   *         otherwise.
   */
  public boolean newInstanceRequest(final String param1, final String param2);

  /**
   * Called by the main container when it receives a <CODE>windowClosing</CODE>
   * event.
   *
   * @return <CODE>true</CODE> if the component can be safely disposed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean windowCloseRequested();
}
