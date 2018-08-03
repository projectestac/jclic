/*
 * File    : SingleConnectionBeanProvider.java
 * Created : 21-oct-2003 19:12
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

package edu.xtec.util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

/** A single ConnectionBeanProvider, that uses only one single connection to the
 * database, in a single ConnectionBean.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class SingleConnectionBeanProvider extends ConnectionBeanProvider{
    
    private Connection con;
    private ConnectionBean conBean;
    
    /** Main initialization function, called immediatelly after constructor by
     * getConnectionBeanProvider functions.
     * @param map Collection of key - value pairs that must specify the driver, url, login and
     * password of the just created ConnectionBeanProvider.
     * @throws Exception Throwed if dbDriver does not contain a valid driver name, or if it can't be
     * instantiated.
     */    
    @Override
    protected void setUp(Map<String, String> map) throws Exception{
        super.setUp(map);
        if(dbDriver==null || dbDriver.length()==0)
            throw new Exception("Parameter dbDriver is null!");
        Class.forName(dbDriver);        
        if(dbServer==null || dbServer.length()==0)
            throw new Exception("Parameter dbServer is null!");
        if(dbLogin!=null && dbLogin.length()>0){
            con=DriverManager.getConnection(dbServer, dbLogin, dbPassword==null ? "":dbPassword);
        } else{
            con=DriverManager.getConnection(dbServer);
        }        
        conBean=new ConnectionBean(con, mapStatements);        
    }
    
    /** Closes the jdbc connection and frees the unique ConnectionBean. */    
    protected void destroy() {
        if(conBean!=null)
            conBean=null;
        if(con!=null){
            try{
                con.close();
            } catch(Exception ex){
            }
            con=null;
        }
    }
    
    /** Provides information about the current state of this ConnectionBeanProvider.
     * @return Information string, formatted in HTML.
    */
    @Override
    public String getInfo(){
        StringBuilder sb=new StringBuilder();
        sb.append("<b>SingleConnectionBeanProvider ").append(hashCode()).append("</b><br>\n")
        .append(super.getInfo())
        .append(conBean.getInfo());
        return sb.toString();
    }
    
    /** This method does nothing in this implementation of ConnectionBeanProvider,
     * because only one ConnectionBean is reused for all getConnectionBean
     * calls.
     * @param conn The Connectionbean to be released.
     * @return A zero-length string.
     */    
    public String freeConnectionBean(ConnectionBean conn) {
        // Do nothing
        return "";
    }
    
    /** Returns the ConnectionBean object.
     * @return The ConnectionBean object.
     */    
    public ConnectionBean getConnectionBean() {
        return conBean;
    }
    
}
