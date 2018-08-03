/*
 * File    : JNDIConnectionBeanProvider.java
 * Created : 10-nov-2005 19:28
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * This class is a special {@link edu.xtec.util.db.ConnectionBeanProvider} that obtains
 * the {@link java.sql.Connection} objects from a JNDI datasource. In order to
 * obtain objects of this class, the static method <CODE>getConnectionBeanProvider</CODE> of
 * <CODE>ConnectionBeanProvider</CODE> must be called, passing the text <CODE>JNDI</CODE> as
 * value of the <CODE>dbDriver</CODE> parameter, and the JNDI name of the datasource
 * as the value of <CODE>dbServer</CODE> param.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class JNDIConnectionBeanProvider extends ConnectionBeanProvider{
    
    /**
     * Key used for the <CODE>dbContext</CODE> property
     */
    public static final String DB_CONTEXT="dbContext";
    /**
     * String "JNDI", used to indicate <CODE>ConnectionBeanProvider</CODE> that an
     * object of this class is required.
     */
    public static final String JNDI="JNDI";
    
    /**
     * Object used to obtain valid {@link java.sql.Connection} objects.
     */
    protected DataSource ds;
    /**
     * Context where to look for the indicated JNDI resource. In <a
     * href="http://tomcat.apache.org/">Apache Tomcat</a>, this value should be
     * <CODE>java:comp/env</CODE>.
     */
    protected String dbContext;
    private int totalCBRequests;
    private Date started;
    private Date lastUse;
    private String logFileString;
    private PrintWriter log;
    private int debugLevel;
    private int totalStatements;
    private String lastRequest;
    
    /** Creates a new instance of JNDIConnectionBeanProvider */
    public JNDIConnectionBeanProvider() {
    }
    
    /**
     * Main initialization function, called immediatelly after constructor by
     * getConnectionBeanProvider functions.
     * @param map Collection of key - value pairs that must specify the JNDI Datasource name and
     * context params.
     * @throws Exception Throwed if the DataSource can't be
     * instantiated.
     */
    @Override
    protected void setUp(Map<String, String> map) throws Exception{
        
        super.setUp(map);
        started=new Date();
        dbDriver=JNDI;
        if(dbServer==null)
            throw new Exception("JNDI datasource name not specified!");
        dbContext = getValue(map, DB_CONTEXT, null);
        debugLevel = Integer.parseInt(getValue(map, "dbDebugLevel", "2"));
        
        if(debugLevel>0){
            try{
                logFileString = getValue(map, "dbLogFile", null);
                if(logFileString!=null){
                    File f=new File(logFileString);
                    if(!f.isAbsolute()){
                        f=new File(System.getProperty("user.home"));
                        f=new File(f, logFileString);
                        logFileString=f.getAbsolutePath();
                    }
                    boolean logAppend = Boolean.valueOf(getValue(map, "dbLogAppend", "true")).booleanValue();
                    log = new PrintWriter(new FileOutputStream(logFileString, logAppend),true);
                }
            } catch(IOException ioex){
                System.err.println(new Date().toString()+" - Error creating log file for JNDIConnectionProvider - "+ioex);
            }
        }
        
        Context ctx=new InitialContext();
        if(dbContext!=null && dbContext.trim().length()>0)
            ctx=(Context)ctx.lookup(dbContext);
        
        ds=(DataSource)ctx.lookup(dbServer);
        
        if(log!=null){
            log.println("-----------------------------------------");
            log.println(started);
            log.println("Starting JNDIConnectionBeanProvider");
            log.println("dbContext = " + dbContext);
            log.println("dbServer = " + dbServer);
            log.println("Context = " + ctx);
            log.println("Datasource = " + ds);
            log.println("-----------------------------------------");
        }
    }
    
    /** Provides information about the current state of this ConnectionBeanProvider.
     * @return Information string, formatted in HTML.
     */
    @Override
    public String getInfo(){
        StringBuilder sb=new StringBuilder();
        sb.append("<b>JNDIConnectionBeanProvider ").append(hashCode()).append("</b><br>\n");
        sb.append(super.getInfo());
        sb.append("started: ").append(started).append("<br>\n");
        sb.append("dbContext: ").append(dbContext).append("<br>\n");
        sb.append("Total requests: ").append(totalCBRequests).append("<br>\n");
        sb.append("Total statements: ").append(totalStatements).append("<br>\n");
        sb.append("Last use: ").append(lastUse).append("<br>\n");
        sb.append("Last request: ").append(lastRequest).append("<br>\n");
        return sb.toString();
    }
    
    /**
     * Performs cleanup
     */
    protected void destroy() {
    }
    
    /** This method must be called when the obtained ConnectionBean is no longer needed,
     * usualy inside the <I>finally</I> block of a <I>try - catch</I> statement.
     * @param conn The ConnectionBean object to be disposed
     * @return A descriptive String, useful only for debug purposes.
     */
    public String freeConnectionBean(ConnectionBean conn) {
        if(conn!=null){
            try{
                lastRequest=conn.getLastStatement();
                totalStatements+=conn.getNumStatements();
                conn.closeConnection();
            } catch(Exception ex){
                if(log!=null)
                    log.println(new Date().toString() + " Unable to close DB connection: "+ex);
            }
        }
        return "";
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
    public ConnectionBean getConnectionBean() {
        ConnectionBean result=null;
        try{
            if(ds!=null){
                Connection con=ds.getConnection();
                result=new ConnectionBean(con, mapStatements);
                totalCBRequests++;
                lastUse=new Date();
            }
            if(log!=null && debugLevel>2)
                log.println(new Date().toString()+" - connection request");
        } catch(Exception ex){
            if(log!=null)
                log.println(new Date().toString() + " Unable to get DB connection: "+ex.getMessage());
        }
        return result;
    }
}
