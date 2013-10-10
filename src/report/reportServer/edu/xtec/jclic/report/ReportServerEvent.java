/*
 * File    : ReportServerEvent.java
 * Created : 06-sep-2002 13:19
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

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class ReportServerEvent extends Object{
    
    public static final int SYSTEM=0, SOCKET=1, DB=2;
    public static final int CONNECT=0, DISCONNECT=1, START=2, STOP=3, MSG=4, ERROR=5, CONNECTION=6;
    public int type;
    public int action;
    public String msg;
    public Socket socket;
    
    public ReportServerEvent(int type, String msg, Socket socket, int action){
        this.type=type;
        this.msg=msg;
        this.socket=socket;
        this.action=action;
    }
        
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder(100);        
        sb.append(type==SYSTEM ? "SYSTEM " : type==SOCKET ? "" : "DB ");
        String sAct="";
        switch(action){
            case CONNECT: sAct="CONNECT"; break;
            case DISCONNECT: sAct="DISCONNECT"; break;
            case START: sAct="START"; break;
            case STOP: sAct="STOP"; break;
            case ERROR: sAct="ERROR"; break;
            case CONNECTION: sAct=msg==null ? "CONNECTION" : ""; break;
        }
        sb.append(sAct).append(" ");
        if(socket!=null) sb.append(socket.getInetAddress().getHostAddress()).append(":").append(socket.getPort()).append(" ");
        if(msg!=null) sb.append(msg);
        return sb.substring(0);
    }
}
