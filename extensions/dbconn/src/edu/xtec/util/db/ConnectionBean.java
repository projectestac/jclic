/*
 * File    : ConnectionBean.java
 * Created : 21-oct-2003 18:11
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores an SQL {@link java.sql.Connection} and provides methods to
 * obtain {@link java.sql.PreparedStatement} objects from it.
 * <CODE>ConnectionBean</CODE> can also store a collection containing all the
 * <CODE>PreparedStatement</CODE> objects, allowing client applications to reuse them.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.16
 */
public class ConnectionBean {
    
    private Connection con;
    private Map<String, PreparedStatement> statements;
    private Date born;
    private Date lastUsed;    
    private String lastStatement;
    private Date closed;
    /**
     * This field is used by {@link edu.xtec.util.db.ConnectionBeanProvider} to count
     * the number of times a ConectionBean has been used.
     */    
    public int usageCount;
    
    /**
     * Creates a new instance of ConnectionBean
     * @param con {@link java.sql.Connection} object in wich this ConnectionBean will be based.
     * @param mapStatements When set to <I>true</I>, PreparedStatement objects will be stored and reused.
     */
    public ConnectionBean(Connection con, boolean mapStatements) {
        this.con=con;
        if(mapStatements)
            statements=new HashMap<String, PreparedStatement>();
        born=new Date();
    }
    
    /**
     * Provides information about the current state of the ConnectionBean.
     * @return Information string, formatted in HTML.
     */
    public String getInfo(){
        StringBuilder sb=new StringBuilder();
        sb.append("ConnectionBean ").append(hashCode());
        if(statements!=null){
            sb.append(" (").append(statements.size()).append(" statements)");        
        }
        sb.append("<br>\n");
        sb.append("Created: ").append(born).append("<br>\n");
        sb.append("Usage count: ").append(usageCount).append("<br>\n");
        if(lastUsed==null)
            sb.append("Not yet used!<br>\n");
        else{
            sb.append("Last used: ").append(lastUsed).append("<br>\n");
            sb.append("Last statement: ").append(lastStatement).append("<br>\n");
        }
        if(closed!=null){
            sb.append("Closed: ").append(closed).append("<br>\n");
        }
        return sb.toString();
    }
        
    /**
     * Provides direct access to the {@link java.sql.Connection} object.
     * @return The {@link java.sql.Connection} object used in this ConnectionBean.
     */    
    public Connection getConnection(){
        lastUsed=new Date();
        return con;
    }
    
    /**
     * Creates a PreparedStatement based on the current connection and the provided SQL expression.<BR>
     * <B>Important:</B> Never call the <B>close()</B> method on Prepared statements obtained
     * from <CODE>ConnectionBean</CODE> objects created with the <CODE>mapStatements</CODE> option.
     * @param sql The SQL statement.
     * @throws SQLException If something goes wrong
     * @return The {@link java.sql.PreparedStatement} object.<BR>
     */    
    public PreparedStatement getPreparedStatement(String sql) throws SQLException{
        if(closed!=null)
            throw new SQLException("Connection closed!");
        lastUsed=new Date();
        lastStatement=sql;
        PreparedStatement stmt=(statements==null) ? null : (PreparedStatement)statements.get(sql);
        if(stmt==null){
            stmt=con.prepareStatement(sql);
            if(statements!=null)
                statements.put(sql, stmt);
        }
        else{
            try{
                // try - catch because
                // odbc.jdbc driver throws NullPoiterException on 
                // PreparedStatement.clearParameters
                //
                stmt.clearParameters();
            } catch(Exception ex){
                // eat exception
            }
        }
        return stmt;
    }
    
    /**
     * Creates a PreparedStatement based on the current connection and the provided SQL expression.<BR>
     * <B>Important:</B> Never call the <B>close()</B> method on Prepared statements obtained
     * from <CODE>ConnectionBean</CODE> objects created with the <CODE>mapStatements</CODE> option.
     * @return The {@link java.sql.PreparedStatement} object.<BR>
     * @param resultSetType A resultSet type. See {@link java.sql.ResultSet}.
     * @param resultSetConcurrency A concurrency type. See {@link java.sql.ResultSet}.
     * @param sql The SQL sentence of the PreparedStatemnt.
     * @throws SQLException If something goes wrong
     */    
    public PreparedStatement getPreparedStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException{
        if(closed!=null)
            throw new SQLException("Connection closed!");
        lastUsed=new Date();
        lastStatement=sql;
        PreparedStatement stmt=(statements==null) ? null : (PreparedStatement)statements.get(sql);
        if(stmt==null){
            stmt=con.prepareStatement(sql, resultSetType, resultSetConcurrency);
            if(statements!=null)
                statements.put(sql, stmt);
        }
        else{
            try{
                // try - catch because
                // odbc.jdbc driver throws NullPoiterException on PreparedStatement.clearParameters
                //
                stmt.clearParameters();
            } catch(Exception ex){
                // eat exception
            }
        }
        return stmt;
    }
    

    /**
     * Closes a {@link java.sql.PreparedStatement} object supplied by a previous call
     * to <CODE>getPreparedStatement()</CODE>. Applications should always call this method
     * when the <CODE>PreparedStatement</CODE> is no longer needed, and never directly
     * call <CODE>close()</CODE> in the <CODE>PreparedStatement</CODE>.
     * @param stmt The <CODE>PreparedStatement</CODE> to be closed.
     */    
    public void closeStatement(PreparedStatement stmt){
        closeStatement(stmt, false);
    }
    
    /**
     * <CODE>ConnectionBeans</CODE> created with the <CODE>mapStatements</CODE> tag set
     * to <I>true</I> can reuse {@link java.sql.PreparedStatement} objects between calls.
     * This method is functionally identical to <CODE>closeStatement(PreparedStatement)</CODE>, but
     * has a param that allows to specify if the statement must be really closed.
     * @param stmt The <CODE>PreparedStatement</CODE> to be closed.
     * @param forceClose When <I>true</I>, the statement will be effectively closed.
     */    
    public void closeStatement(PreparedStatement stmt, boolean forceClose){
        if(stmt!=null){
            if(forceClose || statements==null){
                try{
                    stmt.close();
                } catch(Exception ex){
                    System.err.println("Error closing statement: "+stmt);
                }
            }
            if(statements!=null && forceClose){
                for(String s : statements.keySet()){
                    PreparedStatement ps = statements.get(s);
                    if(ps!=null && ps==stmt){
                        statements.remove(s);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Clears all the stored {@link java.sql.PreparedStatement} objects (if any) and closes
     * the {@link java.sql.Connection}.
     * @throws SQLException If something goes wrong.
     */    
    public void closeConnection() throws SQLException{
        if(closed==null){
            closed=new Date();
            if(statements!=null){
                for(String s : statements.keySet()){                    
                    PreparedStatement stmt=statements.get(s);
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Exception ex) {
                            // Eat Exception
                        }
                    }
                }
                statements.clear();
            }
            con.close();
        }
    }
    
    /**
     * Getter for property lastStatement.
     * @return Value of property lastStatement.
     */
    public java.lang.String getLastStatement() {
        return lastStatement;
    }
    
    /**
     * Returns the number of {@link java.sql.PreparedStatement} objects stored by this
     * <CODE>ConnectionBean</CODE>. Used only in connection beans created with the <CODE>mapStatements</CODE>
     * param set to <CODE>true</CODE>.
     * @return The number of stored statements.
     */    
    public int getNumStatements(){
        return statements==null ? 0 : statements.size();
    }        
}
