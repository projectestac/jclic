/*
 * File    : JClicAuthotApplet.java
 * Created : 27-Apr-2008 21:00
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2008 Francesc Busquets & Departament
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

import edu.xtec.jclic.RunnableComponent;
import edu.xtec.jclic.media.CheckMediaSystem;
import edu.xtec.util.Check;
import edu.xtec.util.LFUtil;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceManager;
import java.net.URL;
import javax.swing.RootPaneContainer;

/** JClic Author applet. Shows a splash screen at startup and loads
 * a {@link AuthorSingleFrame} in a separate process.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public class JClicAuthorApplet extends javax.swing.JApplet implements edu.xtec.jclic.Constants {
    
    private Options options;
    private RunnableComponent rc;
    private String activityPack, sequence;
    private boolean initiated;
    private boolean trace;
    private javax.swing.JLabel splashLabel;
    private boolean isInstaller;
    
    // useful constants
    private static final String HTTP="http:", FILE="file:";
    private static final String STRING="string", BOOL="boolean", URL="url";
    
    // parameters
    private static final String ACTIVITY_PACK="activityPack";
    private static final String SEQUENCE="sequence";
    private static final int NUM_PRIVATE_PARAMS=2;
    private static final String[][] pInfo={
        {ACTIVITY_PACK,                 STRING, "absolute or relative URL of the JClic project to load"},
        {SEQUENCE,                      STRING, "optional project's starting sequence name"},
        {Messages.LANGUAGE,             STRING, "two-char language code"},
        {Messages.COUNTRY,              STRING, "two-char country code"},
        {Messages.VARIANT,              STRING, "locale variant code"},
        {SKIN,              STRING, "skin to be used"},
        {COOKIE,            STRING, "optional session cookie value. currently not used"},
        {REPORTER_CLASS,    STRING, "reporter class name"},
        {REPORTER_PARAMS,   STRING, "reporter parameters"},
        {EXIT_URL,          URL,    "URL where to redirect navigation at end"},
        {INFO_URL_FRAME,    STRING, "optional frame where to display info documents. If unespecified, _BLANK will be used"},
        {SYSTEM_SOUNDS,     BOOL,   "to play or not system sounds"},
        {COMPRESS_IMAGES,   BOOL,   "to compress or not images in cells"},
        {LFUtil.LOOK_AND_FEEL, STRING, "look & feel to use"},
        {AUDIO_ENABLED,     BOOL,   "to have audio enabled or not"},
        {MEDIA_SYSTEM,      STRING, "preferred multimedia system: 'JMF' for Java Media Framework or 'QT' for QuickTime"},
        {TRACE,             BOOL,   "show debug messages in console"}
    };
    
    /** Creates new JClicAuthorApplet */
    public JClicAuthorApplet() {
        super();
        trace=false;
        rc=null;
        initiated=false;
        activityPack=null;
        sequence=null;
        options=new Options(this);
        splashLabel = new javax.swing.JLabel(" ", ResourceManager.getImageIcon("icons/logo_author.png"), javax.swing.SwingConstants.CENTER);
        splashLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        splashLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        splashLabel.setBackground(BG_COLOR);
        splashLabel.setOpaque(true);
        getContentPane().add(splashLabel, java.awt.BorderLayout.CENTER);
    }
    
    @Override
    public void init() {
        // init only once by applet instance
        if(initiated)
            return;
        
        initiated=true;
        
        // get "trace" flag
        trace=TRUE.equals(getParameter(TRACE));
        
        final StringBuilder sb=new StringBuilder();
        final RootPaneContainer rpc=this;
        
        edu.xtec.util.SwingWorker sw=new edu.xtec.util.SwingWorker(){
            @Override
            public Object construct(){
                // get parameters
                for(int i=NUM_PRIVATE_PARAMS; i<pInfo.length; i++){
                    options.put(pInfo[i][0], getParameter(pInfo[i][0]));
                    if(trace)
                        System.out.println(">>> param "+pInfo[i][0]+" is "+getParameter(pInfo[i][0]));
                }
                
                Messages messages;
                try{
                    messages=edu.xtec.util.PersistentSettings.getMessages(options, DEFAULT_BUNDLE);
                } catch(Exception ex){
                    System.err.println("Unable to get default user messages language!\n"+ex);
                    messages=Messages.getMessages(options, DEFAULT_BUNDLE);
                }
                messages.addBundle(COMMON_SETTINGS);
                
                if(splashLabel!=null)
                    splashLabel.setText(messages.get("LOADING"));
                
                if(!Check.checkSignature(options, true))
                    return null;
                
                CheckMediaSystem.check(options, false);
                

                // build player
                try{

                    rc=new edu.xtec.jclic.AuthorSingleFrame(options);

                } catch(Exception ex){
                    sb.append("ERROR: ").append(ex);
                }
                return rc;
            }
            
            @Override
            public void finished(){
                if(getValue()==null){
                    if(isInstaller && options.getString(EXIT_URL)!=null){
                        try{
                            String url=options.getString(EXIT_URL);
                            if(url.indexOf(':')<0) // no protocol, assume "file://"
                                url="file://"+url;
                            getAppletContext().showDocument(new URL(url));
                        } catch(Exception ex){
                            sb.append("ERROR: ").append(ex);
                        }
                    }                                        
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
                            rc.start(activityPack, sequence);
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
    
    @Override
    public void start(){
        if(trace)
            System.out.println(">>> applet started");
    }
    
    @Override
    public void stop(){
        if(rc!=null)
            rc.stop();
        if(trace)
            System.out.println(">>> applet stopped");
    }
    
    @Override
    public void destroy(){
        if(rc!=null){
            if(trace)
                System.out.println(">>> destroying applet...");
            getContentPane().removeAll();
            rc.end();
            rc=null;
            initiated=false;
        }
        if(trace)
            System.out.println(">>> applet destroyed");
    }
    
    @Override
    public String getAppletInfo(){
        return "JClic Author applet";
    }
    
    @Override
    public String[][] getParameterInfo(){
        return pInfo;
    }
}
