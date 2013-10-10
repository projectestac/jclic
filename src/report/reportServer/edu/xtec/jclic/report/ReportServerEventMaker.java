/*
 * File    : ReportServerEventMaker.java
 * Created : 06-sep-2002 13:25
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

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class ReportServerEventMaker {
    
    protected List<Listener> listeners=new ArrayList<Listener>(1);
    
    public interface Listener {
        public void reportEventPerformed(ReportServerEvent ev);
    }
            
    /** Creates a new instance of ReportServerEventMaker */
    public ReportServerEventMaker() {
    }
    
    public void addListener(Listener ls){
        listeners.add(ls);
    }
    
    public void removeListener(Listener ls){
        listeners.remove(ls);
    }
    
    public void fireReportServerSystemEvent(String s){
        fireReportServerSystemEvent(s, ReportServerEvent.MSG);
    }
    
    public void fireReportServerSystemEvent(String s, int action){
        fireReportServerEvent(ReportServerEvent.SYSTEM, s, null, action);
    }
    
    public void fireReportServerSocketEvent(Socket socket, String msg){
        fireReportServerSocketEvent(socket, msg, ReportServerEvent.MSG);
    }
    
    public void fireReportServerSocketEvent(Socket socket, String msg, int action){
        fireReportServerEvent(ReportServerEvent.SOCKET, msg, socket, action);
    }
    
    public void fireReportServerEvent(int type, String msg, Socket socket, int action){
        ReportServerEvent ev=new ReportServerEvent(type, msg, socket, action);
        for(int i=0; i<listeners.size(); i++){
            ((Listener)listeners.get(i)).reportEventPerformed(ev);
        }
    }    
}
