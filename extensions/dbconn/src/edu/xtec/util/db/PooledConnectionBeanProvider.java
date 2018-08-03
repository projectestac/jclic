/*
 * File    : PooledConnectionBeanProvider.java
 * Created : 21-oct-2003 20:13
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2018 Francesc Busquets & Departament
 * d'Educacio de la Generalitat de Catalunya
 *
 * Based on DbConnectionBeanBroker.
 * version 1.0.13 3/12/02
 * by Marc A. Mnich
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/** Implementation of {@link ConnectionBeanProvider} that uses a pool of 
 * {@link edu.xtec.util.db.ConnectionBean} objects.
 * Based on DbConnectionBroker 1.0.13, by Marc A. Mnich
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class PooledConnectionBeanProvider extends ConnectionBeanProvider implements Runnable{
    
    // Additional parameter names
    /** Key for minimum number of connections to start with. Default is 1. */
    public static final String MIN_CONNS = "dbMinConns";
    private static final String DEFAULT_MIN_CONNS = "1";
    /** Key for maximum number of connections in dynamic pool. Default is 3 */
    public static final String MAX_CONNS = "dbMaxConns";
    private static final String DEFAULT_MAX_CONNS = "3";
    /** Key for absolute path name for log file. e.g. 'c:/temp/mylog.log'. Default is 'pooledConnectionBean.log' in user's dir */
    public static final String LOG_FILE="dbLogFile";
    private static final String DEFAULT_LOG_FILE="connectionPool.log";
    /** Key for time in days between connection resets. (Reset does a basic cleanup). Default is 1.0 */
    public static final String MAX_CONN_TIME="dbMaxConnDays";
    private static final String DEFAULT_MAX_CONN_TIME="1.0";
    /** Key for append to logfile. Default is true.*/
    public static final String LOG_APPEND="dbLogAppend";
    private static final String DEFAULT_LOG_APPEND="true";
    /** Key for max time a connection can be checked out before being recycled. Zero value turns option off. Default is 60.*/
    public static final String MAX_CHECKOUT_SECONDS="dbMaxCheckoutSeconds";
    private static final String DEFAULT_MAX_CHECKOUT_SECONDS="60";
    /** Key for level of debug messages output to the log file.  0 -> no messages, 1 -> Errors, 2 -> Warnings, 3 -> Information. Default is 2. */
    public static final String DEBUG_LEVEL="dbDebugLevel";
    private static final String DEFAULT_DEBUG_LEVEL="2";
    
    private Thread runner;
    
    private ConnectionBean[] connPool;
    private int[] connStatus;
    
    private long[] connLockTime, connCreateDate;
    private String[] connID;
    private String logFileString, logPIDFileString;
    private int currConnections, connLast, minConns, maxConns, maxConnMSec,
    maxCheckoutSeconds, debugLevel;
    
    //available: set to false on destroy, checked by getConnection()
    private boolean available=true;
    
    private PrintWriter log;
    private SQLWarning currSQLWarning;
    private String pid;
    
    /**
     * Number of times this ConnectionBean provider has been used since its creation.
     */    
    public int globalUsageCount;
    
    /** Main initialization function, called immediatelly after constructor by
     * getConnectionBeanProvider functions.
     * @param map Collection of key - value pairs that must specify the driver, url, login and
     * password of the just created ConnectionBeanProvider.
     * @throws Exception Throwed if dbDriver does not contain a valid driver name, or if it can't be
     * instantiated.
     */
    @Override
    protected void setUp(Map<String, String> map) throws Exception {
        
        super.setUp(map);
        if(dbDriver==null || dbDriver.length()==0)
            throw new Exception("Parameter dbDriver is null!");
        Class.forName(dbDriver);        
        if(dbServer==null || dbServer.length()==0)
            throw new Exception("Parameter dbServer is null!");
        
        minConns = Math.max(1, Integer.parseInt(getValue(map, MIN_CONNS, DEFAULT_MIN_CONNS)));
        maxConns = Math.max(minConns, Math.min(15, Integer.parseInt(getValue(map, MAX_CONNS, DEFAULT_MAX_CONNS))));
        logFileString = getValue(map, LOG_FILE, DEFAULT_LOG_FILE);
        double maxConnTime = new Double(getValue(map, MAX_CONN_TIME, DEFAULT_MAX_CONN_TIME)).doubleValue();
        boolean logAppend = Boolean.valueOf(getValue(map, LOG_APPEND, DEFAULT_LOG_APPEND)).booleanValue();
        maxCheckoutSeconds = Integer.parseInt(getValue(map, MAX_CHECKOUT_SECONDS, DEFAULT_MAX_CHECKOUT_SECONDS));
        debugLevel = Integer.parseInt(getValue(map, DEBUG_LEVEL, DEFAULT_DEBUG_LEVEL));
        
        connPool = new ConnectionBean[maxConns];
        connStatus = new int[maxConns];
        connLockTime = new long[maxConns];
        connCreateDate = new long[maxConns];
        connID = new String[maxConns];
        currConnections = minConns;
        
        File f=new File(logFileString);
        if(!f.isAbsolute()){
            f=new File(System.getProperty("user.home"));
            f=new File(f, logFileString);
            logFileString=f.getAbsolutePath();
        }
        
        logPIDFileString=logFileString+".pid";
        maxConnMSec = (int)(maxConnTime * 86400000.0);  //86400 sec/day
        if(maxConnMSec < 30000) {  // Recycle no less than 30 seconds.
            maxConnMSec = 30000;
        }
        
        if(debugLevel>0){
            try {
                log = new PrintWriter(new FileOutputStream(logFileString, logAppend),true);
                
                // Can't open the requested file. Open the default file.
            } catch (IOException e1) {
                try {
                    log = new PrintWriter(new FileOutputStream("DBConn_" +
                    System.currentTimeMillis() + ".log",
                    logAppend),true);
                } catch (IOException e2) {
                    throw new IOException("Can't open any log file");
                }
            }
        }
        
        // Write the pid file (used to clean up dead/broken connection)
        SimpleDateFormat formatter
        = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss a zzz");
        Date nowc = new Date();
        pid = formatter.format(nowc);
        
        BufferedWriter pidout = new BufferedWriter(new
        FileWriter(logPIDFileString));
        pidout.write(pid);
        pidout.close();
        
        if(log!=null){
            log.println("-----------------------------------------");
            log.println(new Date());
            log.println("Starting DbConnectionBeanBroker Version 1.0.13:");
            log.println("dbDriver = " + dbDriver);
            log.println("dbServer = " + dbServer);
            log.println("dbLogin = " + dbLogin);
            log.println("log file = " + logFileString);
            log.println("minconnections = " + minConns);
            log.println("maxconnections = " + maxConns);
            log.println("Total refresh interval = " + maxConnTime + " days");
            log.println("logAppend = " + logAppend);
            log.println("maxCheckoutSeconds = " + maxCheckoutSeconds);
            log.println("debugLevel = " + debugLevel);
            log.println("mapStatements = " + mapStatements);
            log.println("-----------------------------------------");
        }
        
        // Initialize the pool of connections with the mininum connections:
        // Problems creating connections may be caused during reboot when the
        //    servlet is started before the database is ready.  Handle this
        //    by waiting and trying again.  The loop allows 5 minutes for
        //    db reboot.
        boolean connectionsSucceeded=false;
        Exception sqlEx=null;
        //int dbLoop=20;
        int dbLoop=3;
        
        try {
            for(int i=1; i < dbLoop; i++) {
                try {
                    for(int j=0; j < currConnections; j++) {
                        createConn(j);
                    }
                    connectionsSucceeded=true;
                    break;
                } catch (SQLException e){
                    sqlEx=e;
                    if(log!=null && debugLevel > 0) {
                        StringBuilder sb=new StringBuilder();
                        sb.append(new Date()).append(" ->Attempt (").append(i);
                        sb.append(" of ").append(dbLoop).append(") failed to create new connections set at startup:\n");
                        sb.append(e).append("\n");
                        sb.append("Will try again in 15 seconds...");                        
                        log.println(sb.substring(0));
                    }
                    try { Thread.sleep(15000); }
                    catch(InterruptedException e1) {}
                }
            }
            if(!connectionsSucceeded) { // All attempts at connecting to db exhausted
                if(log!=null && debugLevel > 0) {
                    log.println("\r\nAll attempts at connecting to Database exhausted");
                }
                if(sqlEx==null)
                    sqlEx=new IOException("Unable to connect to Database");
                throw sqlEx;
            }
        } catch (Exception e) {
            throw e;
            //throw new IOException();
        }
        
        // Fire up the background housekeeping thread
        
        runner = new Thread(this);
        runner.start();
        
    }//End DbConnectionBeanBroker()
    
    
    /**
     * Housekeeping thread.  Runs in the background with low CPU overhead.
     * Connections are checked for warnings and closure and are periodically
     * restarted.
     * This thread is a catchall for corrupted
     * connections and prevents the buildup of open cursors. (Open cursors
     * result when the application fails to close a Statement).
     * This method acts as fault tolerance for bad connection/statement programming.
     */
    public void run() {
        boolean forever = true;
        Statement stmt=null;
        String currCatalog=null;
        long maxCheckoutMillis = maxCheckoutSeconds * 1000;
        
        
        while(forever) {
            
            /*
             * CHECK OF PID DISABLED
             *
            FileReader fr=null;
            BufferedReader in=null;
            // Make sure the log file is the one this instance opened
            // If not, clean it up!
            try {
                fr=new FileReader(logPIDFileString);
                in = new BufferedReader(fr);
                String curr_pid = in.readLine();
                if(curr_pid.equals(pid)) {
                    //log.println("They match = " + curr_pid);
                } else {
                    //log.println("No match = " + curr_pid);
                    if(log!=null)
                        log.close();
                    
                    // Close all connections silently - they are definitely dead.
                    for(int i=0; i < currConnections; i++) {
                        try {
                            connPool[i].closeConnection();
                        } catch (SQLException e1) {} // ignore
                    }
                    // Returning from the run() method kills the thread
                    return;
                }
                
                // moved to "finally"
                //in.close();
                
            } catch (IOException e1) {
                if(log!=null){
                    log.print(new Date().toString() + "Can't read the file for pid info: " + logPIDFileString + " - ");
                    log.println(e1.getMessage());
                }
            } finally{
                try{
                    if(in!=null)
                        in.close();
                    if(fr!=null)
                        fr.close();
                    in=null;
                    fr=null;
                } catch(IOException ex){
                    // ignore exceptions on close
                }
            }
             */
            
            // Get any Warnings on connections and print to event file
            for(int i=0; i < currConnections; i++) {
                try {
                    currSQLWarning = connPool[i].getConnection().getWarnings();
                    if(currSQLWarning != null) {
                        if(log!=null && debugLevel > 1) {
                            log.println(new Date().toString()+" - Warnings on connection " +
                            String.valueOf(i) + " " + currSQLWarning);
                        }
                        connPool[i].getConnection().clearWarnings();
                    }
                } catch(SQLException e) {
                    if(log!=null && debugLevel > 1) {
                        log.println("Cannot access Warnings: " + e);
                    }
                }
                
            }
            
            for(int i=0; i < currConnections; i++) { // Do for each connection
                long age = System.currentTimeMillis() - connCreateDate[i];
                
                
                try {  // Test the connection with createStatement call
                    synchronized(connStatus) {
                        if(connStatus[i] > 0) { // In use, catch it next time!
                            
                            // Check the time it's been checked out and recycle
                            long timeInUse = System.currentTimeMillis() - connLockTime[i];
                            if(log!=null && debugLevel > 2) {
                                log.println(new Date().toString()+" - Warning. Connection " + i +
                                " in use for " + timeInUse +
                                " ms");
                            }
                            if(maxCheckoutMillis != 0) {
                                if(timeInUse > maxCheckoutMillis) {
                                    if(log!=null && debugLevel > 1) {
                                        log.println(new Date().toString()+" Warning. Connection " +
                                        i + " failed to be returned in time.  Recycling...");
                                    }
                                    throw new SQLException();
                                }
                            }
                            
                            continue;
                        }
                        connStatus[i] = 2; // Take offline (2 indicates housekeeping lock)
                    }
                    
                    
                    if(age > maxConnMSec) {  // Force a reset at the max conn time
                        throw new SQLException();
                    }
                    
                    stmt = connPool[i].getConnection().createStatement();
                    connStatus[i] = 0;  // Connection is O.K.
                    //log.println("Connection confirmed for conn = " +
                    //             String.valueOf(i));
                    
                    // Some DBs return an object even if DB is shut down
                    if(connPool[i].getConnection().isClosed()) {
                        throw new SQLException();
                    }
                    
                    
                    // Connection has a problem, restart it
                } catch(SQLException e) {
                    
                    if(log!=null && debugLevel > 1) {
                        log.println(new Date().toString() +
                        " ***** Recycling connection " +
                        String.valueOf(i) + ":");
                    }
                    
                    try {
                        connPool[i].closeConnection();
                    } catch(SQLException e0) {
                        if(log!=null && debugLevel > 0) {
                            log.println(new Date().toString()+" - Error! Can't close connection! Might have been closed already. Trying to recycle anyway... (" + e0 + ")");
                        }
                    }
                    
                    try {
                        createConn(i);
                    } catch(SQLException e1) {
                        if(log!=null && debugLevel > 0) {
                            log.println(new Date().toString()+" - Failed to create connection: " + e1);
                        }
                        connStatus[i] = 0;  // Can't open, try again next time
                    }
                } finally {
                    try{
                        if(stmt != null) {
                            stmt.close();
                        }
                    } catch(SQLException e1){
                        System.err.println(e1.getMessage());
                    }
                }                
            }
            
            try { 
                Thread.sleep(20000); // Wait 20 seconds for next cycle
            }  
            
            catch(InterruptedException e) {
                // Returning from the run method sets the internal
                // flag referenced by Thread.isAlive() to false.
                // This is required because we don't use stop() to
                // shutdown this thread.
                return;
            }            
        }
        
    } // End run
    
    /** This method hands out the connections in round-robin order.
     * This prevents a faulty connection from locking
     * up an application entirely.  A browser 'refresh' will
     * get the next connection while the faulty
     * connection is cleaned up by the housekeeping thread.
     *
     * If the min number of threads are ever exhausted, new
     * threads are added up the the max thread count.
     * Finally, if all threads are in use, this method waits
     * 2 seconds and tries again, up to ten times.  After that, it
     * returns a null.
     * @return The ConnectionBean object, ready to be used. Remember to free it using
     * freeConnectionBean, as explained in {@link
     * ConnectionBeanProvider#freeConnectionBean} ConnectionBeanProvider.
     */
    public ConnectionBean getConnectionBean() {
        
        ConnectionBean conn=null;
        
        if(available){
            boolean gotOne = false;
            
            for(int outerloop=1; outerloop<=10; outerloop++) {
                
                try  {
                    int loop=0;
                    int roundRobin = connLast + 1;
                    if(roundRobin >= currConnections) 
                        roundRobin=0;
                    
                    do {
                        synchronized(connStatus) {
                            if((connStatus[roundRobin] < 1) &&
                            (!connPool[roundRobin].getConnection().isClosed())) {
                                conn = connPool[roundRobin];
                                connStatus[roundRobin]=1;
                                connLockTime[roundRobin] = System.currentTimeMillis();
                                connLast = roundRobin;
                                gotOne = true;
                                break;
                            } else {
                                loop++;
                                roundRobin++;
                                if(roundRobin >= currConnections)
                                    roundRobin=0;
                            }
                        }
                    }
                    while((gotOne==false)&&(loop < currConnections));
                    
                }
                catch (SQLException e1) {
                    if(log!=null)
                        log.println(new Date().toString()+" - Error: " + e1);
                }
                
                if(gotOne) {
                    break;
                } else {
                    synchronized(this) {  // Add new connections to the pool
                        if(currConnections < maxConns) {
                            
                            try {
                                createConn(currConnections);
                                currConnections++;
                            } catch(SQLException e) {
                                if(log!=null && debugLevel > 0) {
                                    log.println(new Date().toString()+" - Error: Unable to create new connection: " + e);
                                }
                            }
                        }
                    }
                    
                    
                    try { 
                        Thread.sleep(2000); 
                    } catch(InterruptedException e) {
                    }
                    
                    if(log!=null && debugLevel > 0) {
                        log.println(new Date().toString()+" --> Connections Exhausted!  Will wait and try again in loop " +
                        String.valueOf(outerloop));
                    }
                }
                
            } // End of try 10 times loop
            
        } else {
            if(log!=null && debugLevel > 0) {
                log.println(new Date().toString()+" - Unsuccessful getConnection() request during destroy()");
            }
        } // End if(available)
        
        if(log!=null && debugLevel > 2) {
            log.println(new Date().toString()+" - Handing out connection " + idOfConnection(conn));
        }
        
        if(conn!=null)
            conn.usageCount++;
        
        return conn;
        
    }
    
    /** Returns the local JDBC ID for a connection.
     * @param conn The ConnectionBean owner if the Connection.
     * @return The JDBC ID.
     */
    public int idOfConnection(ConnectionBean conn) {
        int match;
        String tag;
        
        try {
            tag = conn.getConnection().toString();
        }
        catch (NullPointerException e1) {
            tag = "none";
        }
        
        match=-1;
        
        for(int i=0; i< currConnections; i++) {
            if(connID[i].equals(tag)) {
                match = i;
                break;
            }
        }
        return match;
    }
    
    /** Frees a connection.  Replaces connection back into the main pool for
     * reuse.
     * @param conn The ConnectionBean to be released.
     * @return A String useful only for debug purposes.
     */
    public String freeConnectionBean(ConnectionBean conn) {
        StringBuilder res=new StringBuilder();
        
        int thisconn = idOfConnection(conn);
        if(thisconn >= 0) {
            connStatus[thisconn]=0;
            res.append("freed ").append(conn.getConnection().toString());
            //log.println("Freed connection " + String.valueOf(thisconn) +
            //            " normal exit: ");
        } else {
            if(log!=null && debugLevel > 0) {
                log.println(new Date().toString()+" --> Error: Could not free connection!!!");
            }
        }        
        
        return res.substring(0);
        
    }
    
    /** Returns the age of a connection -- the time since it was handed out to
     * an application.
     * @param conn The ConnectionBean to be examined.
     * @return The age of the Connection, measured in milliseconds.
     */
    public long getAge(ConnectionBean conn) { // Returns the age of the connection in millisec.
        int thisconn = idOfConnection(conn);
        return System.currentTimeMillis() - connLockTime[thisconn];
    }
    
    private void createConn(int i) throws SQLException {
        
        if(connPool[i]!=null){
            globalUsageCount+=connPool[i].usageCount;            
        }
        
        Date now = new Date();
        
        try {
            Class.forName(dbDriver);
            
            connPool[i] = new ConnectionBean(DriverManager.getConnection
            (dbServer,dbLogin,dbPassword), mapStatements);
            
            connStatus[i]=0;
            connID[i]=connPool[i].getConnection().toString();
            connLockTime[i]=0;
            connCreateDate[i] =  now.getTime();
        } catch (ClassNotFoundException e2) {
            if(log!=null && debugLevel > 0) {
                log.println(now.toString()+" - Error creating connection: " + e2);
            }
        }
        if(log!=null)
            log.println(now.toString() + "  Opening connection " + String.valueOf(i) +
            " " + connPool[i].getConnection().toString() + ":");
    }
    
    /**
     * Shuts down the housekeeping thread and closes all connections
     * in the pool. Call this method from the destroy() method of the servlet.
     */
    
    /**
     * Multi-phase shutdown.  having following sequence:
     * <OL>
     * <LI><code>getConnection()</code> will refuse to return connections.
     * <LI>The housekeeping thread is shut down.<br>
     *    Up to the time of <code>millis</code> milliseconds after shutdown of
     *    the housekeeping thread, <code>freeConnection()</code> can still be
     *    called to return used connections.
     * <LI>After <code>millis</code> milliseconds after the shutdown of the
     *    housekeeping thread, all connections in the pool are closed.
     * <LI>If any connections were in use while being closed then a
     *    <code>SQLException</code> is thrown.
     * <LI>The log is closed.
     * </OL><br>
     * Call this method from a servlet destroy() method.
     *
     * @param      millis   the time to wait in milliseconds.
     * @exception  SQLException if connections were in use after
     * <code>millis</code>.
     */
    public void destroy(int millis) throws SQLException {
        
        // Checking for invalid negative arguments is not necessary,
        // Thread.join() does this already in runner.join().
        
        // Stop issuing connections
        available=false;
        
        // Shut down the background housekeeping thread
        runner.interrupt();
        
        // Wait until the housekeeping thread has died.
        try { runner.join(millis); }
        catch(InterruptedException e){} // ignore
        
        // The housekeeping thread could still be running
        // (e.g. if millis is too small). This case is ignored.
        // At worst, this method will throw an exception with the
        // clear indication that the timeout was too short.
        
        long startTime=System.currentTimeMillis();
        
        // Wait for freeConnection() to return any connections
        // that are still used at this time.
        int useCount;
        while((useCount=getUseCount())>0 && System.currentTimeMillis() - startTime <=  millis) {
            try { Thread.sleep(500); }
            catch(InterruptedException e) {} // ignore
        }
        
        // Close all connections, whether safe or not
        for(int i=0; i < currConnections; i++) {
            try {
                connPool[i].closeConnection();
            } catch (SQLException e1) {
                if(log!=null && debugLevel > 0) {
                    log.println(new Date().toString()+" - Cannot close connections on Destroy");
                }
            }
        }
        
        if(useCount > 0) {
            //bt-test successful
            String msg=new Date().toString()+" - Unsafe shutdown: Had to close "+useCount+
            " active DB connections after "+millis+"ms";
            if(log!=null){
                log.println(msg);
                // Close all open files
                log.close();
            }
            // Throwing following Exception is essential because servlet authors
            // are likely to have their own error logging requirements.
            throw new SQLException(msg);
        }
        
        // Close all open files
        if(log!=null)
            log.close();
        
    }//End destroy()
    
    
    /**
     * Less safe shutdown.  Uses default timeout value.
     * This method simply calls the <code>destroy()</code> method
     * with a <code>millis</code>
     * value of 10000 (10 seconds) and ignores <code>SQLException</code>
     * thrown by that method.
     * @see     #destroy(int)
     */
    protected void destroy() {
        try {
            destroy(10000);
        }
        catch(SQLException e) {}
    }
    
    
    
    /** Returns the number of connections in use.
     * @return The nomber of ConnectionBean objects in use.
     */
    // This method could be reduced to return a counter that is
    // maintained by all methods that update connStatus.
    // However, it is more efficient to do it this way because:
    // Updating the counter would put an additional burden on the most
    // frequently used methods; in comparison, this method is
    // rarely used (although essential).
    public int getUseCount() {
        int useCount=0;
        synchronized(connStatus) {
            for(int i=0; i < currConnections; i++) {
                if(connStatus[i] > 0) { // In use
                    useCount++;
                }
            }
        }
        return useCount;
    }
    
    /** Returns the number of connections in the dynamic pool.
     * @return The number of ConnectionBean objects created.
     */
    public int getSize() {
        return currConnections;
    }
    
    /** Provides information about the current state of this ConnectionBeanProvider.
     * @return Information string, formatted in HTML.
     */
    @Override
    public String getInfo(){
        int totalUsageCount=globalUsageCount;
        StringBuilder sb=new StringBuilder();
        sb.append("<b>PooledConnectionBeanProvider ").append(hashCode()).append("</b><br>\n")
        .append(super.getInfo())
        .append("PID: ").append(pid).append("<br>\n")
        .append("LogFileString: ").append(logFileString).append("<br>\n")
        .append("currConnections: ").append(currConnections).append("<br>\n")
        .append("connLast: ").append(connLast).append("<br>\n")
        .append("minConns: ").append(minConns).append("<br>\n")
        .append("maxConns: ").append(maxConns).append("<br>\n")
        .append("maxConnMSec: ").append(maxConnMSec).append("<br>\n")
        .append("maxCheckoutSeconds: ").append(maxCheckoutSeconds).append("<br>\n")
        .append("debugLevel: ").append(debugLevel).append("<br>\n")
        .append("CURRENT CONNECTIONS:<br>\n")
        .append("<hr>\n");
        for(int i=0; i<maxConns; i++){
            ConnectionBean cb=connPool[i];
            if(cb==null)
                sb.append("Empty ConnectionBean<br>\n");
            else{
                sb.append("Id: ").append(connID[i]).append("<br>\n")
                .append("Status: ").append(connStatus[i]).append("<br>\n")
                .append("LockTime: ").append(connLockTime[i]).append("<br>\n")
                .append("CreateDate: ").append(new Date(connCreateDate[i])).append("<br>\n")
                .append("------------\n")
                .append(cb.getInfo());
                totalUsageCount+=cb.usageCount;
            }
            sb.append("<hr>\n");
        }
        sb.append("TOTAL STATEMENTS USED: ").append(totalUsageCount).append("<br>\n");
        return sb.substring(0);
    }
    
}
