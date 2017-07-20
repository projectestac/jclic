/*
 * File    : SingleInstanceJFrame.java
 * Created : 19-feb-2004 14:01
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

import edu.xtec.util.Check;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceManager;
import edu.xtec.util.StrUtils;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class SingleInstanceJFrame extends javax.swing.JFrame implements Constants{
    
    public static final String OK="OK", CANCEL="CANCEL";
    public static final int DEFAULT_TIMEOUT=1000;
    
    RunnableComponent rc;
    javax.swing.JLabel splashLabel;
    String project;
    Options options;
    boolean trace;
    String playerClass;
    SocketThread socketThread;
    boolean initializing;
    boolean armed;    
        
    /** Creates a new instance of SingleInstanceJFrame */
    public SingleInstanceJFrame(String playerClass, String[] args, String windowTitle, String logoIcon, String frameIcon, int port) {
        
        initializing=true;        
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.playerClass=playerClass;        
        options=new Options(this);
        project=loadArgs(args, options);
        
        if(!checkOtherInstance(port)){
            build(playerClass, args, windowTitle, frameIcon, logoIcon);
            armed=true;
        }        
    }
    
    public boolean isArmed(){
        return armed;
    }
    
    public static String loadArgs(String[] args, Options options){
        String result=null;
        for(String arg : args){
            if(arg!=null && arg.length()>0){
                if(arg.startsWith("-")){
                    String key, value=null;
                    int k=arg.indexOf('=');
                    if(k>0){
                        key=arg.substring(1, k);
                        value=arg.substring(k+1);
                    }
                    else
                        key=arg.substring(1);
                    options.put(key, value);
                    //System.out.println(key+" is "+value);
                }
                else
                    result=arg;
            }
        }
        return result;
    }
            
    protected void build(String playerClass, String[] args, String windowTitle, String frameIcon, String logoIcon) {
        
        trace=options.getBoolean(TRACE);
        if(trace){
            for(String arg : args)
                System.out.println(arg);
        }
        
        initComponents();
        setTitle(windowTitle);
        if(frameIcon!=null)
            setIconImage(ResourceManager.getImageIcon(frameIcon).getImage());
        
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        int scrW=(int)screenSize.getWidth();
        int scrH=(int)screenSize.getHeight();        
        
        splashLabel = new javax.swing.JLabel(" ", ResourceManager.getImageIcon(logoIcon), javax.swing.SwingConstants.CENTER);
        splashLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        splashLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        splashLabel.setBackground(BG_COLOR);
        splashLabel.setOpaque(true);
        //splashLabel.setPreferredSize(new Dimension(scrW-40, scrH-80));
        
        getContentPane().add(splashLabel, java.awt.BorderLayout.CENTER);        
        pack();        
        setBounds((scrW-getWidth())/2, (scrH-getHeight())/3, scrW-40, scrH-80);
        setLocation((scrW-getWidth())/2, (scrH-getHeight())/3);
        
        if(Check.checkSignature(options, true))
            init();
        else
            System.exit(0);

    }

    protected void init(){        
        
        final javax.swing.RootPaneContainer rpc=this;        
        
        edu.xtec.util.SwingWorker sw=new edu.xtec.util.SwingWorker(){
            
            final StringBuilder sb = new StringBuilder();
            
            @Override
            public Object construct(){
                try{                    
                    // build messages
                    Messages messages=edu.xtec.util.PersistentSettings.getMessages(options, DEFAULT_BUNDLE);
                    messages.addBundle(COMMON_SETTINGS);
                    
                    if(splashLabel!=null)
                        splashLabel.setText(messages.get("LOADING"));
                                        
                    Class<?> c=Class.forName(playerClass);
                    java.lang.reflect.Constructor cons=c.getConstructor(new Class[]{edu.xtec.util.Options.class});
                    rc=(RunnableComponent)cons.newInstance(new Object[]{options});
                } catch(Exception ex){
                    sb.append("ERROR: Unable to start!\n").append(ex);
                    ex.printStackTrace(System.err);
                }
                return rc;
            }
            
            @Override
            public void finished(){
                if(getValue()==null){
                    // no player build!
                    if(splashLabel!=null){
                        String s=sb.substring(0);
                        splashLabel.setText(s);
                        System.err.println(s);
                    }
                }
                else{
                    // remove label and place player
                    getContentPane().removeAll();
                    splashLabel=null;
                    //rc.addTo(getContentPane(), java.awt.BorderLayout.CENTER);
                    rc.addTo(rpc, java.awt.BorderLayout.CENTER);
                    getRootPane().revalidate();
                    // load project
                    javax.swing.SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            rc.start(project, null);
                            initializing=false;
                        }
                    });
                }
            }
        };
        
        if(trace)
            System.out.println(">>> initializing...");
        
        // launch swingWorker
        sw.start();        
    }
    
    private void initComponents() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                if(rc==null || rc.windowCloseRequested()){
                    if(socketThread!=null)
                        socketThread.stopSocketThread();
                    if(rc!=null){
                        rc.end();
                        rc=null;
                    }
                    dispose();
                }
            }
            
            @Override
            public void windowActivated(java.awt.event.WindowEvent evt) {
                if(rc!=null)
                    rc.activate();
            }
            
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        }
        );
    }
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        if(socketThread!=null)
            socketThread.stopSocketThread();
        if(rc!=null){
            rc.end();
            rc=null;
        }
        while(socketThread!=null){
            Thread.yield();
        }        
        System.exit(0);
    }
    
    protected boolean checkOtherInstance(int port){
        boolean result=false;
        try{
            socketThread=new SocketThread(port, DEFAULT_TIMEOUT);
        } catch(Exception ex){
            // socket already created!
            // eat exception
        }
        if(socketThread==null){
            try{
                Socket socket=new Socket(InetAddress.getLocalHost(), port);
                BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                pw.println(playerClass);
                pw.println(StrUtils.secureString(project));
                pw.println("");
                pw.flush();
                String response=in.readLine();
                result=OK.equals(response);
                socket.close();
            }
            catch(Exception ex){
                System.err.println("Socket error: "+ex);
            }
        }
        else{
            result=false;
            socketThread.start();
        }
        return result;        
    }
            
    protected class SocketThread extends Thread{
        
        boolean running;
        boolean inService;
        boolean forceSocketClose;
        ServerSocket ss;
        int socketTimeOut;
        
        SocketThread(int port, int timeOut) throws IOException{
            ss=new ServerSocket(port);
            socketTimeOut=timeOut;
            running=false;
        }
        
        @Override
        public void run(){
            try{
                running=true;
                ss.setSoTimeout(1000);
                while(running){
                    try{
                        inService=false;
                        Socket socket=ss.accept();
                        inService=true;
                        BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                        String skPlayerClass=StrUtils.secureString(in.readLine());
                        String skArg1=StrUtils.nullableString(in.readLine());
                        String skArg2=StrUtils.nullableString(in.readLine());                        
                        
                        boolean result=rc!=null 
                        && !initializing
                        && skPlayerClass.equals(playerClass) 
                        && skArg1!=null;
                        
                        pw.println(result ? OK : CANCEL);                        
                        pw.flush();
                        socket.close();
                        if(result)
                            rc.newInstanceRequest(skArg1, skArg2);
                    }catch(InterruptedIOException ioex){
                        // Timeout. start again...
                    }catch(Exception ex){
                        if(!forceSocketClose)
                            System.err.println("Socket error: "+ex);
                        running=false;
                    }
                }
                forceSocketClose=true;
                ss.close();                
            }
            catch (IOException ex){
                if(!forceSocketClose)
                    System.err.println("Server socket error: "+ex);
            }
            running=false;
            inService=false;
            socketThread=null;
        }
        
        public void stopSocketThread(){
            if(inService){
                running=false;
            } else {
                try{
                    forceSocketClose=true;
                    ss.close();
                } catch(IOException ex){
                    // eat exception
                } finally{
                    socketThread=null;
                    running=false;
                }
            }
        }
    }    
}
