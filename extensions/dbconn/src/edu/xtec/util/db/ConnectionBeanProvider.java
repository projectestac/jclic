/*
 * File    : ConnectionBeanProvider.java
 * Created : 21-oct-2003 18:18
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

package edu.xtec.util.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Classes that extended this abstract class provide {@link edu.xtec.util.db.ConnectionBean}
 * objects to clients. It also provides a static method to create ConnectionBeanProvider objects
 * based on different parameters, and stores a map containing all the created
 * objects, in order to reuse them between different instances or applications
 * sharing the same JVM.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public abstract class ConnectionBeanProvider {
    
    /** Tag used in properties to identify the JDBC driver used in the connections 
     * to the database.
     */
    public static final String DB_DRIVER="dbDriver";
    /** Tag used in properties to identify the server used in connections */
    public static final String DB_SERVER="dbServer";
    /** Tag used in properties to identify the login name used in connections. */
    public static final String DB_LOGIN="dbLogin";
    /** Tag used in properties to identify the password used in connections. */
    public static final String DB_PASSWORD="dbPassword";
    /**
     * Tag used in properties to determine if the {@link edu.xtec.util.db.ConnectionBean}
     * objects created must store and reuse {@link java.sql.PreparedStatement} objects.
     */
    public static final String MAP_STATEMENTS="dbMapStatements";
    
    /** Tag used for <I>true</I> */
    public static final String TRUE = "true";
    
    /** Tag used for <I>false</I> */
    public static final String  FALSE="false";
        
    /**
     * Name of the JDBC driver used in Connection objects
     */
    protected String dbDriver;
    
    /**
     * Name of the JDBC url used in Connection objects
     */
    protected String dbServer;
    
    /** Name of the user used in Connection objects. */
    protected String dbLogin;
    
    /**
     * Password used in JDBC Connection objects.
     */
    protected String dbPassword;
    
    /**
     * When true, the {@link edu.xtec.util.db.ConnectionBean} objects created will store and reuse
     * prepared statements.
     */
    protected boolean mapStatements;
    
    private int useCount;
    private static Map<String, ConnectionBeanProvider> cbProviders;
    
    /**
     * Main initialization function. Must be called immediatelly after constructor by
     * getConnectionBeanProvider functions.
     * @param map Collection of key - value pairs that must specify the driver, url, login and
     * password of the just created ConnectionBeanProvider.
     * @throws Exception Throwed if dbDriver does not contain a valid driver name, or if it can't be
     * instantiated.
     */
    protected void setUp(Map<String, String> map) throws Exception{
        dbDriver = getValue(map, DB_DRIVER, null);
        dbServer = getValue(map, DB_SERVER, null);
        dbLogin  = getValue(map, DB_LOGIN, null);
        dbPassword = getValue(map, DB_PASSWORD, null);
        mapStatements = !FALSE.equals(getValue(map, MAP_STATEMENTS, TRUE));
        useCount=0;
    }
    
    /** This is the main function that all ConnectionbeanProvider objects must
     * implement.<P>
     * <B>Important:</B> You must ever call FreeConnectionBean after the use of the ConnectionBean
     * object. Typical inmplementation use a try - catch - finally statement block in
     * order to ensure that all ConnectionBean objects will be properly disposed after
     * use.<P>
     * Example:<P>
     * <PRE>
     * ConectionBeanProvider cbp;
     * java.util.Properties prop=new Java.util.Properties();
     * // ...
     * // ... fill-up properties with values for dbDriver, dbServer, dbLogin, etc.
     * // ...
     * cbp=ConnectionBeanProvider.getConnectionBeanProvider(map);
     * ConnectionBean cb=cbp.getConnectionBean();
     * try {
     *  // ... use of the ConnectionBean object
     * } catch(Exception ex){
     *  // ... process possible exceptions done while database access
     * } finally {
     *  // Very important: free the ConnectionBean object:
     *  cbp.freeConnectionBean(cb);
     * }
     * </PRE>
     * @return A ready-to-use ConnectionBean. Remember to return it by calling
     * FreeConnectionBean.
     */
    public abstract ConnectionBean getConnectionBean();
    
    /** This method must be called when the obtained ConnectionBean is no longer needed,
     * usualy inside the <I>finally</I> block of a <I>try - catch</I> statement.
     * @param conn The ConnectionBean object to be disposed
     * @return A descriptive String, useful only for debug purposes.
     */
    public abstract String freeConnectionBean(ConnectionBean conn);
    
    /**
     * This method will be called before finalization. Implementations of
     * <CODE>ConnectionBeanProvider</CODE> must perform cleanup operations on all opened
     * connections.
     */
    protected abstract void destroy();
    
    /**
     * Provides information about the current state of this <CODE>ConnectionBeanProvider</CODE>.
     * @return Information string, formatted in HTML.
     */
    public String getInfo(){
        StringBuilder sb=new StringBuilder();
        sb.append(DB_DRIVER).append(": ").append(dbDriver).append("<br>\n");
        sb.append(DB_SERVER).append(": ").append(dbServer).append("<br>\n");
        sb.append(DB_LOGIN).append(": ").append(dbLogin).append("<br>\n");
        sb.append(MAP_STATEMENTS).append(": ").append(mapStatements).append("<br>\n");
        sb.append("Use count:").append(useCount).append("<br>\n");
        return sb.substring(0);
    }
    
    /**
     * Provides information about all the <CODE>ConnectionBeanProvider</CODE> objects currently
     * existing.
     * @return Information string, formatted in HTML.
     */
    public static String getGlobalInfo(){
        StringBuilder sb=new StringBuilder();
        if(cbProviders==null)
            cbProviders=new HashMap<String, ConnectionBeanProvider>(2);
        sb.append("<b>").append(cbProviders.size()).append(" ConnectionBeanProviders created</b><br>\n");
        for(String key : cbProviders.keySet()){        
            ConnectionBeanProvider cbp=cbProviders.get(key);
            sb.append("<hr>\n");
            sb.append(cbp.getInfo());
        }
        return sb.substring(0);
    }
    
    /**
     * Method used to build a ConnectionBeanProvider. The current
     * implementations returns a {@link edu.xtec.util.db.SingleConnectionBeanProvider} if <CODE>mapStatements</CODE>
     * is <I>false</I>. Otherwise, it checks the value of <CODE>dbDriverName</CODE> to return a
     * {@link PooledConnectionBeanProvider} or a {@link
     * edu.xtec.util.db.JNDIConnectionBeanProvider}.
     * @param pooled Determines the type of ConnectionbeanProvider to be created: pooled or simple.
     * @param dbDriver Name of the JDBC driver to be used. When this param is "JNDI", a
     * {@link edu.xtec.util.db.JNDIConnectionBeanProvider} will be created.
     * @param dbServer Path to the JDBC source or JNDI datasource to be used.
     * @param dbLogin User name to be used in JDB connections.
     * @param dbPassword Password to be used in JDBC connections.
     * @param mapStatements When <i>true</i>, the created {@link edu.xtec.util.db.ConnectionBean} 
     * objects will reuse PreparedStatements.
     * @throws Exception If something goes wrong.
     * @return The ConnectionBean object.
     */
    public static ConnectionBeanProvider getConnectionBeanProvider(boolean pooled,
    String dbDriver, String dbServer,  String dbLogin, String dbPassword,
    boolean mapStatements) throws Exception{
        Map<String, String> map=new HashMap<String, String>();
        map.put(DB_DRIVER, dbDriver);
        map.put(DB_SERVER, dbServer);
        map.put(DB_LOGIN, dbLogin);
        map.put(DB_PASSWORD, dbPassword);
        map.put(MAP_STATEMENTS, mapStatements ? TRUE : FALSE);
        return getConnectionBeanProvider(pooled, map);
    }
    
    /**
     * This method calls the many-params version of getConnectionBeanProvider(),
     * extracting the needed parameters from the map.
     * @param map Collection containing all the key - value pairs needed to create the
     * ConnectionBeanProvider object.
     * @param pooled Determines the type of <CODE>ConnectionbeanProvider</CODE> to be created: <I>pooled</I> or <I>simple</I>.
     * @throws Exception If something goes wrong.
     * @return The requested ConnectionBeanProvider object.
     */
    public static ConnectionBeanProvider getConnectionBeanProvider(boolean pooled, Map<String, String> map) throws Exception{
        ConnectionBeanProvider result=null;
        String staticDbDriver=map.get(DB_DRIVER);
        String staticDbServer=map.get(DB_SERVER);
        String staticDbLogin=map.get(DB_LOGIN);        
        StringBuilder kb=new StringBuilder();
        kb.append(staticDbDriver).append("@");
        kb.append(staticDbServer).append("@");
        kb.append(staticDbLogin).append("@");
        kb.append(pooled ? "POOLED" : "DIRECT");
        String ks=kb.substring(0);
        
        if(cbProviders==null)
            cbProviders=new HashMap<String, ConnectionBeanProvider>(2);
        else
            result = cbProviders.get(ks);
        
        /*
        Iterator it=cbProviders.keySet().iterator();
        while(it.hasNext()){
            Object o=it.next();
            if(o!=null && o.equals(ks)){
                result=(ConnectionBeanProvider)cbProviders.get(o);
                break;
            }
        }
        */
        
        if(result==null){
            if(JNDIConnectionBeanProvider.JNDI.equals(staticDbDriver))
                result=new JNDIConnectionBeanProvider();
            else if(pooled)
                result=new PooledConnectionBeanProvider();
            else
                result=new SingleConnectionBeanProvider();
            result.setUp(map);
            cbProviders.put(ks, result);
        }
        result.useCount++;
        result.readDBSettings(map);
        return result;
    }
    
    /** Use this method when a ConectionbeanProvider is no longer needed.
     * ConnectionBeanProvider objects are stored into a static collection, so they can
     * be reused when multiple classes (or multiple instances of the same class) need
     * them. The ConnectionBeanProvider object will be destroyed only when a internal
     * counter decreases to zero.
     * @param cbp The ConnectionBeanProvider to be released.
     */
    public static void freeConnectionBeanProvider(ConnectionBeanProvider cbp){
        if(cbp!=null && cbProviders!=null){
            for (String s : cbProviders.keySet()) {
                ConnectionBeanProvider o=cbProviders.get(s);
                if(o!=null && o.equals(cbp) && --cbp.useCount<=0){
                    cbp.destroy();
                    cbProviders.remove(s);
                    break;
                }
            }
        }
    }
    
    /**
     * Useful method used to retrieve a value from a Map using a default value when not
     * found, or when found with <I>null</I> value.
     * @param map The map where to search for
     * @param key The key to be searched
     * @param defaultValue Value returned when not found or null
     * @return The String associated with the specified key, or the defaultValue if not found.
     */
    protected String getValue(Map<String, String> map, String key, String defaultValue){
        String result=map.get(key);
        if(result==null || result.trim().length()==0)
            result=defaultValue;
        return result;
    }

    /**
     * Key for the name of the database table that contains key-value pairs of settings
     */
    public static final String SETTINGS_TABLE_KEY="SETTINGS_TABLE";
    /**
     * Default name for the settings table
     */
    public static final String DEFAULT_SETTINGS_TABLE = "SETTINGS";
    /**
     * All the settings with this value will be read from the database
     */
    public static final String READ_FROM_DATABASE="READ_FROM_DATABASE";

    /**
     * This function looks inside a map for entries with the value "READ_FROM_DATABASE".
     * All settings with this text will be replaced with the value stored in a
     * table names "SETTINGS" (or with the name specified in the map with the key
     * "SETTINGS_TABLE"). This table must contain at least two fields: "SETTING_KEY"
     * and "SETTING_VALUE".
     * @param map The map to be processed
     * @throws Exception If the database is not accessible.
     */
    public void readDBSettings(Map<String, String> map) throws Exception{

        Exception exception=null;
        String settingsTable=getValue(map, SETTINGS_TABLE_KEY, DEFAULT_SETTINGS_TABLE);
        if(settingsTable!=null && map.containsValue(READ_FROM_DATABASE)){
            edu.xtec.util.db.ConnectionBean con=null;
            PreparedStatement stmt=null;
            ResultSet rs=null;
            try{
                con=getConnectionBean();
                String sql= "SELECT SETTING_VALUE"+
                            " FROM "+settingsTable+
                            " WHERE SETTING_KEY=?";
                stmt=con.getPreparedStatement(sql);
                for (String key : map.keySet()) {
                    String value=map.get(key);
                    if(READ_FROM_DATABASE.equals(value)){
                        stmt.setString(1, key);
                        rs=stmt.executeQuery();
                        if(rs.next()){
                            value=rs.getString(1);
                            map.put(key, value);
                        }
                    }
                }
            } catch(Exception ex) {
                exception=ex;
            } finally {
                if(rs!=null)
                    rs.close();
                if(stmt!=null && con!=null)
                    con.closeStatement(stmt);
                if(con!=null)
                    freeConnectionBean(con);
            }
        }

        if(exception!=null)
            throw exception;
    }

}
