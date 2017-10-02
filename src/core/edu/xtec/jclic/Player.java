/*
 * File    : Player.java
 * Created : 18-dec-2000 10:21
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

import edu.xtec.jclic.bags.ActivityBagElement;
import edu.xtec.jclic.bags.ActivitySequenceElement;
import edu.xtec.jclic.bags.JumpInfo;
import edu.xtec.jclic.boxes.ActiveBox;
import edu.xtec.jclic.boxes.ActiveBoxContent;
import edu.xtec.jclic.boxes.BoxBase;
import edu.xtec.jclic.boxes.BoxConnector;
import edu.xtec.jclic.boxes.Counter;
import edu.xtec.jclic.clic3.Clic3;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.fileSystem.ZipFileSystem;
import edu.xtec.jclic.media.ActiveMediaBag;
import edu.xtec.jclic.media.ActiveMediaPlayer;
import edu.xtec.jclic.media.CheckMediaSystem;
import edu.xtec.jclic.media.EventSounds;
import edu.xtec.jclic.media.JavaSoundAudioBuffer;
import edu.xtec.jclic.media.MediaContent;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.report.Reporter;
import edu.xtec.jclic.skins.AboutWindow;
import edu.xtec.jclic.skins.Skin;
import edu.xtec.util.BrowserLauncher;
import edu.xtec.util.Html;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import edu.xtec.util.ResourceManager;
import edu.xtec.util.StrUtils;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


/**
 * <CODE>Player</CODE> is one of the the main classes of the JClic system. It implements the
 * {@link edu.xtec.jclic.PlayStation} interface, so it can read and play JClic
 * projects from files or streams. In order to allow activities to run,
 * <CODE>Player</CODE> provides them of all the necessary resources: media bags
 * (to load and realize images and other media contents), sequence control,
 * report system management, user interface (loading and management of skins),
 * display of system messages, etc.
 * Player is also a {@link edu.xtec.jclic.RunnableComponent}, so it can be
 * embedded in applets, frames and other containers.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.10
 */
public class Player extends JPanel implements Constants, RunnableComponent, PlayStation, ActionListener {
    
    // check fonts
    //private static final Font CHECK_ARIAL=FontCheck.checkSystemFont("Arial", "arial2.ttf");
    
    // static fields
    /**
     * Name of the resource corresponding to the default skin of JClic.
     */
    public static final String DEFAULT_SKIN="@default.xml";
    
    /**
     * Name of the default reporter used by JClic.
     */
    public static final String DEFAULT_REPORTER="Reporter";
    
    /**
     * List of the names of the internal resources corresponding to the default
     * event sounds used by JClic.
     */
    public static final String[] DEFAULT_EVENT_SOUNDS={
        
        //
        // CHANGED 01-Mar-2013: Use sounds in WAV format to avoid openJDK errors
        //
        /*START*/ "sounds/start.wav",
        /*CLICK*/ "sounds/click.wav",
        /*ACTION_ERROR*/ "sounds/action_error.wav",
        /*ACTION_OK*/ "sounds/action_ok.wav",
        /*FINISHED_ERROR*/ "sounds/finished_error.wav",
        /*FINISHED_OK*/ "sounds/finished_ok.wav"
        
    };
    
    /**
     * List of the names of the internal resources corresponding to the default icons
     * associated to the basic actions defined in {@link edu.xtec.jclic.Constants}.
     */
    public static final String[] ACTION_ICONS={
        "icons/prev.gif",
        "icons/next.gif",
        "icons/return.gif",
        "icons/reset.gif",
        "icons/info_small.gif",
        "icons/help.gif",
        "icons/audio_on.gif",
        "icons/logo_button.gif"
    };
    
    /**
     * The default name of the application (JClic)
     */
    public static final String DEFAULT_APP_NAME="JClic";
    
    /**
     * Array containing the {@link javax.swing.Action} objects used by the player.
     */
    protected Action[] actions;
    
    /**
     * The main {@link edu.xtec.util.Messages} object.
     */
    protected Messages messages;
    
    /**
     * The {@link edu.xtec.jclic.project.JClicProject} currently hosted by the
     * {@code Player}.
     */
    protected JClicProject project;
    
    /**
     * The UI element (<CODE>Panel</CODE>)of the <CODE>Activity</CODE> currently
     * running in the <CODE>Player</CODE>.
     */
    protected Activity.Panel actPanel;
    
    /**
     * This object manages a list with the names of all the activities
     * currently played by the user in this <CODE>Player</CODE>.
     */
    protected PlayerHistory history;
    
    /**
     * Current {@link edu.xtec.jclic.skins.Skin} of the <CODE>Player</CODE>.
     */
    protected Skin skin;
    
    /**
     * Default skin of this <CODE>Player</CODE>. Users can override the
     * <CODE>DEFAULT_SKIN</CODE> setting and choose another Skin to be used by
     * default in JClic.
     */
    protected Skin defaultSkin;
    
    /**
     * Bag of realized media objects, ready to play.
     */
    protected ActiveMediaBag activeMediaBag;
    
    /**
     * Current reporter used by this <CODE>Player</CODE>.
     */
    protected Reporter reporter;
    
    /**
     * Current set of system sonds used in this <CODE>Player</CODE>.
     */
    protected EventSounds eventSounds;
    
    /**
     * Main <CODE>Timer</CODE>, used to feed the time conter. This timer generates an
     * <CODE>ActionPerformed</CODE> event every second.
     */
    protected Timer timer;
    
    /**
     * This flag indicates if the <CODE>Player</CODE> must play the sounds (including
     * system sounds) and other media contents of the activities.
     */
    protected boolean audioEnabled=true;
    
    /**
     * This flag indicates if the program must write verbose info to the system console.
     */
    protected boolean trace=false;
    
    /**
     * This flag indicates if the navigation buttons (<I>go to next activity</I> and
     * <I>go back</I>) are enabled o disabled.
     */
    protected boolean navButtonsDisabled=false;
    
    /**
     * When this flag is <CODE>true</CODE>, the navigation buttons are always enabled,
     * despite of the indications made by the activities or the sequence control system.
     * Used only for debugging projects with complicated sequence chaining.
     */
    protected boolean navButtonsAlways=false;
    
    /**
     * The main name of the application in wich this <CODE>Player</CODE> is running.
     */
    protected String appName=DEFAULT_APP_NAME;
    
    Timer delayedTimer;
    Action delayedAction;
    Object currentConstraints;
    int[] counterVal=new int[NUM_COUNTERS];
    Cursor[] cursors=new Cursor[3];
    Options options;
    Image splashImg;
    Point bgImageOrigin=new Point();
    private edu.xtec.util.SwingWorker worker=null;
    
    /**
     * Creates a new <CODE>Player</CODE> object.
     * @param options Options object to be used in the initialization process
     */
    public Player(Options options) {
        this(options, null);
    }
    
    /**
     * Creates a new <CODE>Player</CODE> object that will be initially loaded with a specific
     * <CODE>JClicProject</CODE>.
     * @param options Options object to be used in the initialization process
     * @param project JClic project to load (can be <CODE>null</CODE>)
     */
    public Player(Options options, JClicProject project) {
        this.options=options;
        this.project=project;
        init();
    }
    
    /**
     * Main initialization process, called once by constructors. Subclasses of
     * <CODE>Player</CODE> should override this method to initialize additional members.
     */
    protected void init(){
        options.setLookAndFeel();
        CheckMediaSystem.check(options, false);
        setPreferredSize(new Dimension(600, 400));
        setLayout(null);
        Utils.checkRenderingHints(options);
        BoxConnector.checkOptions(options);
        ActiveBox.checkOptions(options);
        for(int i=0; i<NUM_COUNTERS; i++)
            counterVal[i]=0;
        setMessages();
        buildActions();
        setActionsText();
        history=new PlayerHistory(this);
        trace=options.getBoolean(TRACE);
        ActiveBox.compressImages=options.getBoolean(COMPRESS_IMAGES, true);
        audioEnabled=options.getBoolean(AUDIO_ENABLED, true);
        navButtonsAlways=options.getBoolean(NAV_BUTTONS_ALWAYS, false);
        setProject(project);
        activeMediaBag=new ActiveMediaBag();
        initSkin();
        setSkin(skin);
        setSystemMessage(getMessages().get("msg_initializing"), null);
        setWaitCursor(true);
        createCursors();
        createEventSounds();
        initTimers();
        splashImg=ResourceManager.getImageIcon(LOGO_ICON).getImage();
        if(skin!=null && skin.hasMemMonitor())
            skin.setMem(Runtime.getRuntime().freeMemory());
        setWaitCursor(false);
        setWindowTitle();
        setSystemMessage(getMessages().get("msg_ready"), null);
    }
    
    /**
     * Starts the player, loading a specific project if specified. This
     * method is defined in the {@link RunnableComponent} interface.
     * @param fullPath Full path to the JClic project file to be loaded. Can be
     * <I>null</I>.
     * @param sequence Optional parameter, used only when <CODE>fullPath</CODE>
     * is not <I>null</I>. It
     * indicates the sequence where the to start. It's also possible to indicate
     * a string representation of a number "N". In this case, the player will
     * start with the activity indicated by the Nth element of the main sequence
     * of the project.
     * @return <CODE>true</CODE> if the player starts successfully.
     * <CODE>false</CODE> otherwise.
     */
    public boolean start(String fullPath, String sequence){
        initReporter();
        if(fullPath!=null)
            return load(fullPath, sequence);
        else
            return false;
    }
    
    /**
     * This method is called when the container gains the focus for the first
     * time or when losts it. Not used in <CODE>Player</CODE>.
     */
    public void activate() {
    }
    
    /**
     * Instructs the RunnableComponent to stop working.
     */
    public void stop(){
        stopMedia(-1);
    }
    
    /**
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            end();
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Executes miscellaneous finalization routines.
     */
    public void end(){
        if(worker!=null){
            worker.interrupt();
            worker=null;
        }
        stopMedia();
        closeHelpWindow();
        if(actPanel!=null){
            actPanel.end();
            remove(actPanel);
            actPanel=null;
        }
        if(eventSounds!=null){
            eventSounds.close();
            eventSounds=null;
        }
        if(project!=null){
            project.end();
            project=null;
        }
        if(activeMediaBag!=null)
            activeMediaBag.removeAll();
        if(reporter!=null){
            reporter.end();
            reporter=null;
        }
    }
    
    /**
     * Creates and initializes the members of the {@link cursors} array.
     */
    protected void createCursors(){
        try{
            Toolkit tk=Toolkit.getDefaultToolkit();
            
            cursors[HAND_CURSOR]=tk.createCustomCursor(
            ResourceManager.getImageIcon("cursors/hand.gif").getImage(),
            new Point(8,0), "hand");
            
            cursors[OK_CURSOR]=tk.createCustomCursor(
            ResourceManager.getImageIcon("cursors/ok.gif").getImage(),
            new Point(0,0), "ok");
            
            cursors[REC_CURSOR]=tk.createCustomCursor(
            ResourceManager.getImageIcon("cursors/micro.gif").getImage(),
            new Point(15,3), "record");
            
        }catch(Exception e){
            System.err.println("Error creating cursor:\n"+e);
        }
    }
    
    /**
     * Creates the {@link eventSounds} member and initializes the sound system.
     */
    protected void createEventSounds(){
        
        // Workaround for a JavaSound bug in Mac OS X
        if(options.getBoolean(Options.MAC)){
            try{
                JavaSoundAudioBuffer.initialize();
            } catch(Exception ex){
                System.err.println("Error initializing AudioBuffer lines:\n"+ex);
            }
        }
        // end of workaround
        
        eventSounds=new EventSounds(null);
        try{
            for(int i=0; i<DEFAULT_EVENT_SOUNDS.length; i++){
                String s=DEFAULT_EVENT_SOUNDS[i];
                eventSounds.setDataSource(i, ResourceManager.getResourceAsByteArray(s), options);
            }
            eventSounds.realize(options, project.mediaBag);
        } catch(Exception ex){
            System.err.println("Error reading system sound:\""+ex);
        }
        EventSounds.globalEnabled=options.getBoolean(SYSTEM_SOUNDS, true);
    }
    
    /**
     * Creates and initializes the {@link reporter} member.
     */
    protected void initReporter(){
        if(reporter!=null){
            reporter.end();
            reporter=null;
        }
        String reporterClassName=StrUtils.secureString(options.getString(REPORTER_CLASS), DEFAULT_REPORTER);
        try{
            reporter=Reporter.getReporter(reporterClassName,
            options.getString(REPORTER_PARAMS), this, messages);
        } catch(Exception ex){
            reporter=null;
            messages.showErrorWarning(this, "report_err_creating", reporterClassName, ex, null);
        }
    }
    
    /**
     * Creates and initializes the {@link defaultSkin} member.
     */
    protected void initSkin(){
        String s="";
        try{
            FileSystem fsSk=null;
            s=options.getString(SKIN);
            if(s==null)
                s=DEFAULT_SKIN;
            else if(!s.startsWith(Skin.INTERNAL_SKIN_PREFIX)){
                fsSk=new FileSystem(FileSystem.getPathPartOf(s), this);
                s=FileSystem.getFileNameOf(s);
            }
            defaultSkin=Skin.getSkin(s, fsSk, this);
            actions[ACTION_REPORTS].setEnabled(true);
            //actions[ACTION_AUDIO].setEnabled(options.get(MEDIA_SYSTEM)!=null);
            actions[ACTION_AUDIO].setEnabled(true);
        }
        catch(Exception ex){
            System.err.println("Error creating skin \""+s+"\":\n"+ex);
        }
    }
    
    /**
     * Creates and initializes the members {@link timer}, {@link delayedTimer} and
     * {@link delayedAction}
     */
    protected void initTimers(){
        timer=new Timer(1000, this);
        delayedTimer=new Timer(1000, this);
        delayedTimer.setRepeats(false);
        delayedAction=null;
    }
    
    /**
     * If open, closes the help dialog window.
     */
    public void closeHelpWindow(){
        if(skin!=null){
            if(skin.currentHelpWindow!=null)
                skin.currentHelpWindow.setVisible(false);
            if(skin.currentAboutWindow!=null)
                skin.currentAboutWindow.setVisible(false);
        }
    }
    
    /**
     * Creates and initializes the {@link messages} member.
     * @return The <CODE>messages</CODE> member.
     */
    protected Messages setMessages(){
        messages=Messages.getMessages(options, DEFAULT_BUNDLE);
        messages.addBundle(COMMON_SETTINGS);
        setLocale(messages.getLocale());
        Locale.setDefault(messages.getLocale());
        setActionsText();
        if(skin!=null){
            skin.setLocale(messages.getLocale());
        }
        return messages;
    }
    
    public Component getTopComponent(){
        if(skin!=null)
            return skin;
        return this;
    }
    
    public Skin getSkin(){
        return skin;
    }
    
    /**
     *
     * @param newSkin
     */
    public void setSkin(Skin newSkin){
        if(newSkin==null)
            newSkin=defaultSkin;
        
        if(newSkin!=null && !newSkin.equals(skin)){
            Container top=null;
            Object [] currentSkinSettings=null;
            
            if(skin!=null){
                currentSkinSettings=skin.getCurrentSettings();
                skin.detach();
                top=skin.getParent();
                top.remove(skin);
            }
            
            newSkin.attach(this);
            skin=newSkin;
            
            if(top!=null){
                RootPaneContainer rpc=null;
                while(top!=null && rpc==null){
                    if(top instanceof RootPaneContainer)
                        rpc=(RootPaneContainer)top;
                    else
                        top=top.getParent();
                }
                
                if(rpc!=null){
                    addTo(rpc, currentConstraints);
                    //top.invalidate();
                    top.validate();
                    top.repaint();
                }
            }
            
            if(currentSkinSettings!=null && skin!=null)
                skin.setCurrentSettings(currentSkinSettings);
        }
    }
    
    public void addTo(RootPaneContainer cont, Object constraints){
        currentConstraints=constraints;
        if(constraints==null){
            cont.getContentPane().add(getTopComponent());
        }
        else{
            cont.getContentPane().add(getTopComponent(), constraints);
        }
    }
    
    protected FileSystem createFileSystem(){
        return new FileSystem(this);
    }
    
    protected void setProject(JClicProject p) {
        if(project!=null){
            if(project!=p)
                project.end();
            removeActivity();
        }
        project=(p!=null ? p : new JClicProject(this, createFileSystem(), null));
        project.realize(eventSounds, this);
        if(project.skin!=null)
            defaultSkin=project.skin;
    }
    
    public boolean load(String fullPath, String sequence){
        load(fullPath, sequence, null, null);
        return true;
    }
    
    public void load(final String sFullPath, final String sSequence,
    final String sActivity, final ActivityBagElement sAbe){
        
        if(worker!=null){
            return;
        }
        
        worker=new edu.xtec.util.SwingWorker(){
            Activity.Panel actp;
            Exception exception=null;
            Player thisPlayer=Player.this;
            
            @Override
            public Object construct(){
                
                if(skin!=null)
                    skin.startAnimation();
                
                setWaitCursor(true);
                
                String fullPath=Clic3.pacNameToLowerCase(sFullPath);
                String sequence=Clic3.pacNameToLowerCase(sSequence);
                Activity act=null;
                String activityName=sActivity;
                ActivityBagElement abe=sAbe;
                FileSystem fileSystem=project.getFileSystem();
                
                try{
                    // Step 1: load or create project and set a valid value for "sequence"
                    if(fullPath!=null){
                        setSystemMessage(messages.get("msg_loading_project"),
                        FileSystem.getFileNameOf(fullPath));
                        if(sequence==null)
                            sequence="0";
                        
                        // Check fileSystem and projectName
                        if(fileSystem!=null){
                            fullPath=fileSystem.getUrl(fullPath);
                            if(fullPath.startsWith("file://"))
                                fullPath=fullPath.substring(7);
                            // Added 03-Feb-2011
                            // Remove trailing parameters of URLs
                            else if(fullPath.indexOf('?')>0)
                                fullPath=fullPath.substring(0, fullPath.indexOf('?'));
                            // ----------
                        }

                        String projectName;
                        if(fullPath.endsWith(Utils.EXT_JCLIC_ZIP) || fullPath.endsWith(Utils.EXT_SCORM_ZIP)){
                            fileSystem=FileSystem.createFileSystem(fullPath, thisPlayer);
                            String[] projects=((ZipFileSystem)fileSystem).getEntries(".jclic");
                            if(projects==null)
                                throw new Exception("File "+fullPath+" does not contain any jclic project");
                            projectName=projects[0];
                        }
                        else{
                            fileSystem=new FileSystem(FileSystem.getPathPartOf(fullPath), thisPlayer);
                            projectName=FileSystem.getFileNameOf(fullPath);
                        }
                        
                        // Set project
                        if(projectName.endsWith(".jclic")){
                            org.jdom.Document doc=fileSystem.getXMLDocument(projectName);
                            setProject(JClicProject.getJClicProject(doc.getRootElement(),
                            thisPlayer, fileSystem, fullPath));
                            if(reporter!=null)
                                reporter.newSession(project, thisPlayer, messages);
                        }
                        else{
                            sequence=projectName;
                            setProject(new JClicProject(thisPlayer, fileSystem, fullPath));
                        }
                    }
                    
                    // Step 2: load ActivitySequenceElement ase
                    if(sequence!=null){
                        String seqName=FileSystem.stdFn(sequence);
                        setSystemMessage(messages.get("msg_loading_project"), FileSystem.getFileNameOf(seqName));
                        
                        navButtonsDisabled=false;
                        ActivitySequenceElement ase=project.activitySequence.getElementByTag(seqName, true);
                        
                        // if sequence does no exists, get existing sequence by number
                        if(ase==null){
                            int i=StrUtils.getAbsIntValueOf(seqName);
                            if(i>=0)
                                ase=project.activitySequence.getElement(i, true);
                        }
                        
                        // at this point, if ase==null the sequence was not found in project.
                        // try load new sequence (only with Clic3 files)
                        if(ase==null){
                            boolean firstPac=(project.activitySequence.getSize()==0);
                            boolean isPcc=seqName.endsWith(".pcc");
                            boolean isPac=seqName.endsWith(".pac");
                            if(isPcc || isPac){
                                if(isPcc){
                                    String path=fileSystem.root+seqName;
                                    fileSystem=FileSystem.createFileSystem(path, thisPlayer);
                                    if(firstPac){
                                        project.setFileSystem(fileSystem);
                                        project.setFullPath(path);
                                    }
                                    else
                                        setProject(new JClicProject(thisPlayer, fileSystem, path));
                                    firstPac=true;
                                    Clic3.readPccFile(project);
                                    ase=project.activitySequence.getCurrentAct();
                                }
                                else if(isPac){
                                    Clic3.addPacToSequence(project, seqName);
                                    ase=project.activitySequence.getElementByTag(seqName, true);
                                }
                                
                                if(firstPac){
                                    project.setName(seqName);
                                    if(reporter!=null)
                                        reporter.newSession(project, thisPlayer, messages);
                                }
                            }
                        }
                        
                        if(ase!=null){
                            if(reporter!=null)
                                reporter.newSequence(ase);
                            activityName=ase.getActivityName();
                        }
                    }
                    
                    // step 3: load ActivityBagElement abe
                    if(activityName!=null){
                        String actName=FileSystem.stdFn(activityName);
                        abe=project.activityBag.getElement(actName);
                    }
                    
                    // step 4: load Activity act
                    if(abe!=null){
                        setSystemMessage(messages.get("msg_loading_activity"), abe.getName());
                        act=Activity.getActivity(abe.getData(), project);
                    }
                    
                    // step 5: Load activity
                    if(act!=null){
                        setSystemMessage(null, messages.get("msg_preparing_media"));
                        if(project.settings.eventSounds!=null)
                            act.eventSounds.setParent(project.settings.eventSounds);
                        project.mediaBag.waitForAllImages();
                        act.prepareMedia(thisPlayer);
                        activeMediaBag.realizeAll();
                        if(abe!=null)
                            project.activitySequence.checkCurrentActivity(abe.getName());
                        setSystemMessage(null, messages.get("msg_initializing"));
                        actp=act.getActivityPanel(thisPlayer);
                        actp.buildVisualComponents();
                    }
                }
                catch(Exception ex){
                    exception=ex;
                    if(project==null)
                        setProject(null);
                    //act=null;
                    actp=null;
                }
                return actp;
            }
            
            @Override
            public void finished(){
                
                setWaitCursor(false);
                
                if(actPanel!=null){
                    actPanel.end();
                    remove(actPanel);
                    actPanel=null;
                    setCounterValue(TIME_COUNTER, 0);
                }
                
                if(actp!=null && worker!=null){
                    // moved to thread
                    setBackgroundSettings(actp.getActivity());
                    add(actPanel=actp);
                    actPanel.setCursor(null);
                    splashImg=null;
                    
                    // set skin
                    if(skin!=null)
                        skin.resetAllCounters(false);
                    
                    if(actp.skin!=null)
                        setSkin(actp.skin);
                    else if(project.skin!=null)
                        setSkin(project.skin);
                    else
                        setSkin(defaultSkin);
                    
                    if(skin!=null){
                        boolean hasReturn=(history.storedElementsCount()>0);
                        int navBtnFlag
                        = navButtonsAlways ? ActivitySequenceElement.NAV_BOTH
                        : navButtonsDisabled ? ActivitySequenceElement.NAV_NONE
                        : project.activitySequence.getNavButtonsFlag();
                        
                        if(actions!=null){
                            actions[ACTION_NEXT].setEnabled((navBtnFlag & ActivitySequenceElement.NAV_FWD)!=0
                            && project.activitySequence.hasNextAct(hasReturn));
                            actions[ACTION_PREV].setEnabled((navBtnFlag & ActivitySequenceElement.NAV_BACK)!=0
                            && project.activitySequence.hasPrevAct(hasReturn));
                            actions[ACTION_RETURN].setEnabled(history.storedElementsCount()>0);
                            actions[ACTION_HLP].setEnabled(actp.getActivity().helpWindowAllowed());
                            actions[ACTION_RESET].setEnabled(actp.getActivity().canReinit());
                            actions[ACTION_INFO].setEnabled(actp.getActivity().hasInfo());
                        }
                    }
                    // place activity on screen
                    setSystemMessage(messages.get("msg_ready"), null);
                    initActivity();
                }
                else if(exception!=null){
                    String sType=null;
                    List<Object> v=new ArrayList<Object>();
                    if(sFullPath!=null){
                        v.add(sFullPath);
                        sType="msg_error_loading_project";
                    }
                    if(sSequence!=null){
                        v.add(sSequence);
                        if(sType==null)
                            sType="msg_error_loading_sequence";
                    }
                    if(sActivity!=null){
                        v.add(sActivity);
                        if(sType==null)
                            sType="msg_error_loading_activity";
                    }
                    if(sAbe!=null){
                        v.add(sAbe.getName());
                        if(sType==null)
                            sType="msg_error_loading_activity";
                    }
                    if(sType==null)
                        sType=Messages.ERROR;
                    
                    setSystemMessage(messages.get(sType), null);
                    messages.showErrorWarning(thisPlayer, "err_reading_data", v, exception, null);
                    
                    validate();
                }
                else{
                    setSystemMessage(messages.get("msg_ready"), null);
                }
                
                // unlock events
                setWindowTitle();
                worker=null;
                if(skin!=null){
                    skin.stopAnimation();
                    skin.setEnabled(true);
                }
                setEnabled(true);
            }
        };
        
        // Main thread, after SwingWorker was build:
        forceFinishActivity();
        if(skin!=null)
            skin.setEnabled(false);
        setEnabled(false);
        worker.start();
    }
    
    public void forceFinishActivity(){
        if(timer!=null){
            timer.stop();
            delayedTimer.stop();
            if(actPanel!=null){
                closeHelpWindow();
                actPanel.forceFinishActivity();
                stopMedia();
                activeMediaBag.removeAll();
                if(Utils.lowMemoryCondition()){
                    if(trace)
                        System.out.println(">>> LOW MEMORY! cleaning...");
                    project.mediaBag.clearData();
                    System.runFinalization();
                    System.gc();
                }
            }
            setCursor(null);
        }
    }
    
    public void removeActivity(){
        forceFinishActivity();
        if(actPanel!=null){
            actPanel.end();
            remove(actPanel);
            setMsg(null);
            setBackgroundSettings(null);
            actPanel=null;
        }
    }
    
    public void initActivity(){
        
        setWaitCursor(true);
        setCursor(null);
        timer.stop();
        delayedTimer.stop();
        setCounterValue(TIME_COUNTER, 0);
        stopMedia();
        try{
            if(actPanel!=null){
                actPanel.initActivity();
                timer.start();
                if(!actPanel.getActivity().mustPauseSequence())
                    startAutoPassTimer();
                if(getFressa()!=null)
                    getFressa().initActivity(actPanel);
                setSystemMessage(messages.get("msg_activity_running"), null);
            }
            if(skin!=null)
                skin.setMem(Runtime.getRuntime().freeMemory());
        } catch(Exception ex){
            messages.showErrorWarning(this, "msg_error_starting_activity", ex);
            setSystemMessage(messages.get("ERROR"), null);
        } finally{
            setWaitCursor(false);
            validate();
            repaint();//
        }
    }
    
    public void startActivity(Activity.Panel ap){
        setWaitCursor(true);
        try{
            ap.startActivity();
        }
        catch(Exception ex){
            messages.showErrorWarning(this, "msg_error_starting_activity", ex);
            setSystemMessage(messages.get("ERROR"), null);
        }
        finally{
            setWaitCursor(false);
        }
    }
    
    @Override
    public void doLayout(){
        if(trace)
            System.out.println(">>> layout!");
        if(actPanel!=null){
            BoxBase.resetAllFonts();
            Rectangle bounds=getBounds();
            Rectangle proposedRect=new Rectangle(AC_MARGIN, AC_MARGIN,
            bounds.width-2*AC_MARGIN, bounds.height-2*AC_MARGIN);
            if(actPanel.bgImage!=null && !actPanel.getActivity().tiledBgImg){
                bgImageOrigin.x=(getWidth()-actPanel.bgImage.getWidth(this))/2;
                bgImageOrigin.y=(getHeight()-actPanel.bgImage.getHeight(this))/2;
                if(actPanel.getActivity().absolutePositioned){
                    proposedRect.x=bgImageOrigin.x;
                    proposedRect.y=bgImageOrigin.y;
                    proposedRect.width-=(bgImageOrigin.x-AC_MARGIN);
                    proposedRect.height-=(bgImageOrigin.y-AC_MARGIN);
                    proposedRect.width=Math.min(proposedRect.width, bounds.width);
                    proposedRect.height=Math.min(proposedRect.height, bounds.height);
                }
            }
            actPanel.fitTo(proposedRect, bounds);
        }
    }
    
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        
        if(splashImg!=null){
            int x, y, imgW, imgH;
            g2.setColor(BG_COLOR);
            g2.fill(g2.getClip());
            imgW=splashImg.getWidth(this);
            imgH=splashImg.getHeight(this);
            x=(getBounds().width-imgW)/2;
            y=(getBounds().height-imgH)/2;
            g2.drawImage(splashImg, x, y, this);
            return;
        }
        
        Rectangle rBounds=new Rectangle(0, 0, getWidth(), getHeight());
        
        if(actPanel==null || actPanel.getActivity().bgGradient==null ||
        actPanel.getActivity().bgGradient.hasTransparency())
            super.paintComponent(g);
        
        if(actPanel!=null && (actPanel.getActivity().bgGradient!=null || actPanel.bgImage!=null)){
            RenderingHints rh=g2.getRenderingHints();
            g2.setRenderingHints(DEFAULT_RENDERING_HINTS);
            
            if(actPanel.getActivity().bgGradient!=null)
                actPanel.getActivity().bgGradient.paint(g2, rBounds);
            
            if(actPanel.bgImage!=null){
                Rectangle r=new Rectangle(0, 0, actPanel.bgImage.getWidth(this),
                actPanel.bgImage.getHeight(this));
                Rectangle gBounds=g2.getClipBounds();
                
                if(!actPanel.getActivity().tiledBgImg){
                    r.setLocation(bgImageOrigin);
                    if(r.intersects(gBounds)){
                        g2.drawImage(actPanel.bgImage, bgImageOrigin.x, bgImageOrigin.y, this);
                    }
                }
                else{
                    Utils.tileImage(g2, actPanel.bgImage, rBounds, r, this);
                }
            }
            g2.setRenderingHints(rh);
        }
    }
    
    // Methods inherited from interface ActionListener
    public void actionPerformed(ActionEvent e){
        String ac=null;
        if(timer!=null && e.getSource().equals(timer)){
            incCounterValue(TIME_COUNTER);
            if(actPanel!=null && actPanel.getActivity().maxTime>0 &&
            actPanel.isPlaying() &&
            counterVal[TIME_COUNTER]>=actPanel.getActivity().maxTime){
                actPanel.finishActivity(false);
            }
            return;
        }
        
        if(delayedTimer!=null && e.getSource().equals(delayedTimer)){
            delayedTimer.stop();
            if(delayedAction!=null){
                delayedAction.actionPerformed(null);
            }
        }
        
        if(ac==null && (ac=e.getActionCommand())==null)
            return;
        delayedAction=null;
        
        processActionEvent(ac);
    }
    
    protected int getNumActions(){
        return NUM_ACTIONS;
    }
    
    protected void buildActions(){
        
        actions=new Action[getNumActions()];
        
        actions[ACTION_NEXT]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                history.processJump(project.activitySequence.getJump(false, reporter), false);
            }
        };
        
        actions[ACTION_PREV]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                history.processJump(project.activitySequence.getJump(true, reporter), false);
            }
        };
        
        actions[ACTION_RETURN]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                history.pop();
            }
        };
        
        actions[ACTION_RESET]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                if(actPanel!=null && actPanel.getActivity().canReinit())
                    initActivity();
            }
        };
        
        actions[ACTION_HLP]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                if(actPanel!=null)
                    actPanel.showHelp();
            }
        };
        
        actions[ACTION_INFO]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){                
                if(actPanel!=null && actPanel.getActivity().hasInfo()){
                    if(actPanel.getActivity().infoUrl!=null){
                        displayUrl(actPanel.getActivity().infoUrl, true);
                    }
                    else if(actPanel.getActivity().infoCmd!=null){
                        runCmd(actPanel.getActivity().infoCmd);
                    }
                }
            }
        };
        
        actions[ACTION_REPORTS]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){                
                showAbout(true);
            }
        };
        
        actions[ACTION_AUDIO]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                Object vBack=getValue(AbstractAction.DEFAULT);
                audioEnabled=!audioEnabled;
                Object vNew=audioEnabled ? Boolean.TRUE : Boolean.FALSE;
                if(!audioEnabled){
                    stopMedia();
                    EventSounds.globalEnabled=false;
                }
                else{
                    EventSounds.globalEnabled=options.getBoolean(SYSTEM_SOUNDS, true);
                }
                putValue(AbstractAction.DEFAULT, vNew);
                if(changeSupport!=null){
                    PropertyChangeEvent evt=new PropertyChangeEvent(this, "selected", vBack, vNew);
                    changeSupport.firePropertyChange(evt);
                }
            }
        };
        actions[ACTION_AUDIO].putValue(AbstractAction.DEFAULT, audioEnabled ? Boolean.TRUE : Boolean.FALSE);
        
        for(int dynAct : DYNAMIC_ACTIONS){
            actions[dynAct].setEnabled(false);
        }
        actions[ACTION_AUDIO].setEnabled(true);
    }
    
    protected void setActionsText(){
        if(actions!=null){
            for(int i=0; i<actions.length; i++){
                if(actions[i]!=null){
                    String s=messages.get("action_"+getActionName(i)+"_caption");
                    if(!s.equals(actions[i].getValue(Action.NAME)))
                        actions[i].putValue(Action.NAME, s);
                    s=messages.get("action_"+getActionName(i)+"_tooltip");
                    if(!s.equals(actions[i].getValue(Action.SHORT_DESCRIPTION)))
                        actions[i].putValue(Action.SHORT_DESCRIPTION, s);
                    s=messages.get("action_"+getActionName(i)+"_keys");
                    if(s!=null && s.length()==2){
                        actions[i].putValue(Action.MNEMONIC_KEY, new Integer(s.charAt(0)));
                        char c=s.charAt(1);
                        int kk=-1;
                        if(c=='*'){
                            switch(i){
                                case ACTION_NEXT:
                                    kk=KeyEvent.VK_RIGHT;
                                    break;
                                    
                                case ACTION_PREV:
                                    kk=KeyEvent.VK_LEFT;
                                    break;
                                    
                                case ACTION_RETURN:
                                    kk=KeyEvent.VK_UP;
                                    break;
                                    
                                case ACTION_RESET:
                                    kk=KeyEvent.VK_ENTER;
                                    break;
                                    
                                default:
                                    break;
                            }
                        }
                        else
                            kk=(int)c;
                        
                        if(kk>=0)
                            actions[i].putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(kk, KeyEvent.CTRL_MASK));
                    }
                    Icon icon=getActionIcon(i);
                    if(icon!=null && !icon.equals(actions[i].getValue(Action.SMALL_ICON)))
                        actions[i].putValue(Action.SMALL_ICON, icon);
                }
            }
        }
    }
    
    protected String getActionName(int actionId){
        if(actionId<0 || actionId>=ACTION_NAME.length)
            return null;
        return ACTION_NAME[actionId];
    }
    
    protected Icon getActionIcon(int actionId){
        if(actionId<0 || actionId>=ACTION_ICONS.length)
            return null;
        return ResourceManager.getImageIcon(ACTION_ICONS[actionId]);
    }
    
    public Action getAction(int id) {
        if(actions==null || id<0 || id>=actions.length)
            return null;
        return actions[id];
    }
    
    protected boolean processActionEvent(String ac){
        return !isEnabled();
    }
    
    protected void showAbout(boolean selectReportPane){
        if(skin!=null){
            AboutWindow aw=skin.buildAboutWindow();
            try{
                aw.buildAboutTab("JClic", getMsg("JCLIC_VERSION"), null, null, null, null, null);
                aw.buildStandardTab(aw.getHtmlSystemInfo(),
                "about_window_systemInfo",
                "about_window_lb_system", "icons/system_small.gif");
                if(project!=null){
                    StringBuilder sb=new StringBuilder(4096);
                    sb.append(project.settings.toHtmlString(messages));
                    if(actPanel!=null){
                        sb.append(Html.BR).append(actPanel.getActivity().toHtmlString(this));
                    }
                    aw.buildStandardTab(sb.substring(0),
                    "about_window_projectInfo",
                    "about_window_lb_project", "icons/info_small.gif");
                }
                if(reporter!=null){
                    aw.buildStandardTab(reporter.toHtmlString(messages),
                    "about_window_reportInfo",
                    "about_window_lb_report", "icons/report_small.gif");
                    if(selectReportPane)
                        aw.getTabbedPane().setSelectedIndex(3);
                }
                skin.showAboutWindow(aw);
                
            } catch(Exception ex){
                System.err.println("Error building about window!\n"+ex);
            }
        }
    }
    
    // Methods inherited from interface ActivityContainer
    public void playMedia(final MediaContent mediaContent, final ActiveBox mediaPlacement) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                String s=mediaContent.mediaFileName;
                JumpInfo ji;
                
                switch(mediaContent.mediaType){
                    case MediaContent.RUN_CLIC_PACKAGE:
                        ji=new JumpInfo(JumpInfo.JUMP, FileSystem.stdFn(s));
                        ji.projectPath=mediaContent.externalParam;
                        history.processJump(ji, true);
                        break;
                    case MediaContent.RUN_CLIC_ACTIVITY:
                        history.push();
                        // ----- MODIFIED 24-Feb-2009
                        // Treat links to activity as links to sequence
                        // TODO: Check if this has secondary effects on existing JClic projects
                        // if(!navButtonsAlways)
                        //    navButtonsDisabled=true;
                        load(null, null, s, null);
                        break;
                    case MediaContent.RETURN:
                        history.pop();
                        break;
                    case MediaContent.EXIT:
                        String exitUrl=mediaContent.mediaFileName;
                        if(exitUrl!=null)
                            exitUrl=project.getFileSystem().getUrl(exitUrl);
                        ji=new JumpInfo(JumpInfo.EXIT, exitUrl);
                        history.processJump(ji, false);
                        break;
                    case MediaContent.RUN_EXTERNAL:
                        if(mediaContent.mediaFileName!=null){
                            StringBuilder sb=new StringBuilder(mediaContent.mediaFileName);
                            if(mediaContent.externalParam!=null)
                                sb.append(" ").append(mediaContent.externalParam);
                            runCmd(sb.substring(0));
                        }
                        break;
                    case MediaContent.URL:
                        if(mediaContent.mediaFileName!=null){
                            displayUrl(mediaContent.mediaFileName, true);
                        }
                        break;
                    case MediaContent.PLAY_AUDIO:
                    case MediaContent.PLAY_MIDI:
                    case MediaContent.PLAY_VIDEO:
                    case MediaContent.RECORD_AUDIO:
                    case MediaContent.PLAY_RECORDED_AUDIO:
                        if(audioEnabled){
                            ActiveMediaPlayer amp=activeMediaBag.getActiveMediaPlayer(mediaContent, project.mediaBag, Player.this);
                            if(amp!=null){
                                amp.play(mediaPlacement);
                            }
                        }
                        break;
                    default:
                        break;
                }
                
            }
        });
    }
    
    protected void runCmd(String cmd){
        if(options.get(Options.APPLET)!=null){
            messages.showAlert(this, "msg_warn_no_exec_in_applets");
            return;
        }
        
        try{
            Runtime.getRuntime().exec(cmd, null, new File(project.getFileSystem().root));
        } catch(Exception ex){
            messages.showErrorWarning(this, "msg_error_executing_external", cmd, ex, null);
        }
    }
    
    public void stopMedia(){
        stopMedia(-1);
    }
    
    public void stopMedia(int level){
        activeMediaBag.stopAll(level);
    }
    
    public void activityFinished(boolean completedOk) {
        closeHelpWindow();

        if(getFressa()!=null)
            getFressa().activityFinished();

        if(completedOk){
            setCursor(getCustomCursor(OK_CURSOR));
            actPanel.setCursor(null);
        }
        setSystemMessage(messages.get("msg_activity_finished"), null);
        timer.stop();
        startAutoPassTimer();
    }
    
    public void startAutoPassTimer(){
        ActivitySequenceElement ase=project.activitySequence.getCurrentAct();
        if(ase!=null && ase.delay>0 && !delayedTimer.isRunning() && !navButtonsDisabled){
            delayedAction=actions[ACTION_NEXT];
            delayedTimer.setInitialDelay(ase.delay * 1000);
            delayedTimer.start();
        }
    }
    
    protected void setBackgroundSettings(Activity act){
        setBackground(act!=null ? act.bgColor : Color.lightGray);
        bgImageOrigin.setLocation(0, 0);
        repaint();
    }
    
    public void setMsg(ActiveBoxContent abc) {
        ActiveBox ab=null;
        if(skin!=null)
            ab=skin.getMsgBox();
        if(ab!=null){
            ab.clear();
            ab.setContent(abc==null ? ActiveBoxContent.getEmptyContent() : abc);
        }
    }
    
    public void playMsg() {
        if(skin!=null && skin.getMsgBox()!=null){
            skin.getMsgBox().playMedia(this);
        }
    }
    
    public void incCounterValue(int counterId){
        counterVal[counterId]++;
        Counter c=null;
        if(skin!=null && (c=skin.getCounter(counterId))!=null)
            c.setValue(counterVal[counterId]);
        if(counterId==ACTIONS_COUNTER && actPanel!=null
        && actPanel.getActivity().maxActions>0 && actPanel.isPlaying()
        && counterVal[ACTIONS_COUNTER]>=actPanel.getActivity().maxActions){
            // 14-Mai-2010
            // Correction of bug 1249: Incorrect reporting of activity results when maximum number of attempts achieved
            // actPanel.finishActivity must be called when the action
            // has been completely processed.
            // The "solved" status should not be supposed to be always "false"

            //actPanel.finishActivity(false);
            SwingUtilities.invokeLater(
              new Runnable(){
                public void run(){
                    actPanel.finishActivity(actPanel.solved);
                }
            });
        }
    }
    
    public void setCountDown(int counterId, int maxValue){
        Counter c=null;
        if(skin!=null && (c=skin.getCounter(counterId))!=null)
            c.setCountDown(maxValue);
    }
    
    public void setCounterValue(int counterId, int newValue) {
        counterVal[counterId]=newValue;
        Counter c=null;
        if(skin!=null && (c=skin.getCounter(counterId))!=null)
            c.setValue(newValue);
    }
    
    public int getCounterValue(int counterId) {
        return counterVal[counterId];
    }
    
    public void setCounterEnabled(int counterId, boolean bEnabled) {
        if(skin!=null){
            skin.enableCounter(counterId, bEnabled);
            setCountDown(counterId, 0);
        }
    }
    
    public Messages getMessages(){
        return (messages==null ? setMessages() : messages);
    }
    
    public void setWaitCursor(boolean state) {
        if(skin!=null){
            skin.setWaitCursor(state);
        }
    }
    
    public void setSystemMessage(String msg1, String msg2) {
        
        if(skin!=null)
            skin.setSystemMessage(msg1, msg2);
        
        if(trace)
            System.out.println("MSG "+(msg1==null ? "" : msg1 + " ")+(msg2==null ? "" : msg2));
    }
    
    public JComponent getComponent(){
        return this;
    }
    
    public ActiveMediaPlayer getActiveMediaPlayer(MediaContent mediaContent){
        if(activeMediaBag!=null && mediaContent!=null)
            return activeMediaBag.getActiveMediaPlayer(mediaContent, project.mediaBag, this);
        else
            return null;
    }
    
    /** Gets the custom cursor corresponding to the indicated type
     * @param type Type of cursor.
     * @return The requested cursor, or the default system cursor if not found.
     */
    public Cursor getCustomCursor(int type) {
        if(type>=0 && type<cursors.length)
            return cursors[type];
        else
            return null;
    }
    
    public void reportNewActivity(Activity act, int currentScore){
        ActivitySequenceElement ase=project.activitySequence.getCurrentAct();
        if(reporter!=null){
            if(ase.getTag()!=null && !ase.getTag().equals(reporter.getCurrentSequenceTag()))
                reporter.newSequence(ase);
            if(act.includeInReports)
                reporter.newActivity(act);
        }
        setCounterValue(ACTIONS_COUNTER, 0);
        setCounterValue(SCORE_COUNTER, 0);
    }
    
    public void reportNewAction(Activity act, String type, String source,
    String dest, boolean ok, int currentScore){
        if(reporter!=null && act.includeInReports && act.reportActions)
            reporter.newAction(type, source, dest, ok);
        
        if(currentScore>=0){
            incCounterValue(ACTIONS_COUNTER);
            setCounterValue(SCORE_COUNTER, currentScore);
        }
    }
    
    public void reportEndActivity(Activity act, boolean solved){
        if(reporter!=null && act.includeInReports)
            reporter.endActivity(counterVal[SCORE_COUNTER], counterVal[ACTIONS_COUNTER], solved);
    }
    
    public boolean showHelp(JComponent hlpComponent, String hlpMsg) {
        if(skin!=null){
            skin.showHelp(hlpComponent, hlpMsg);
            return true;
        }
        return false;
    }
    
    public InputStream getProgressInputStream(InputStream is, int expectedLength, String name){
        if(skin!=null && is!=null && !(is instanceof ByteArrayInputStream)){
            is=skin.getProgressInputStream(is, expectedLength, name);
        }
        return is;
    }
    
    public Options getOptions(){
        return options;
    }
    
    public PlayerHistory getHistory(){
        return history;
    }
    
    public void displayUrl(String url, boolean inFrame){
        if(url!=null){
            url=project.getFileSystem().getUrl(url);
            try{
                displayUrl(new URL(url), inFrame);
            } catch(Exception ex){
                System.err.println("Unable to invoque URL "+url+"\n"+ex);
            }
        }
    }
    
    public void displayUrl(URL url, boolean inFrame){
        if(url==null) return;
        Applet applet=options.getApplet();
        try{
            // Modified 21-Feb-2011
            // getAppletContext().showDocument seems not working on a Mac
            // (tested with Firefox and Safari)
            // Instead, launch a new browser:
            if(applet!=null && !options.getBoolean(Options.MAC)){
                if(inFrame){
                    String frame=(String)options.get(INFO_URL_FRAME);
                    if(frame==null)
                        frame="_BLANK";
                    applet.getAppletContext().showDocument(url, frame);
                }
                else{
                    end();
                    applet.getAppletContext().showDocument(url);
                }
            }
            else{
                BrowserLauncher.openURL(url.toExternalForm());
            }
        }catch(Exception ex){
            System.err.println("Unable to invoque URL "+url+"\n"+ex);
        }
    }
    
    public void exit(){
        exit(null);
    }
    
    public void exit(String url){
        final String sUrl=(url==null ? options.getString(EXIT_URL) : url);
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                if(sUrl!=null){
                    displayUrl(sUrl, false);
                }
                if(options.getApplet()==null){
                    try{
                        end();
                        Frame fr=JOptionPane.getFrameForComponent(getTopComponent());
                        if(fr!=null)
                            fr.dispose();
                        else
                            System.exit(0);
                    } catch(Exception ex){
                        System.err.println("Unable to exit!\n"+ex);
                    }
                }
            }
        });
    }
    
    @Override
    public void requestFocus(){
        if(actPanel!=null)
            actPanel.requestFocus();
    }
    
    public String getMsg(String key) {
        return messages.get(key);
    }
    
    public void doAutoStart() {
    }
    
    public boolean newInstanceRequest(final String param1, final String param2) {
        boolean result=false;
        if(param1!=null){
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    Frame frame=JOptionPane.getFrameForComponent(Player.this);
                    if(frame!=null)
                        frame.toFront();
                    load(param1, param2);
                }
            });
            result=true;
        }
        return result;
    }
    
    public boolean windowCloseRequested() {
        return true;
    }
    
    public void setWindowTitle(String docTitle){
        Window w=Options.getWindowForComponent(this);
        if(w!=null){
            StringBuilder sb=new StringBuilder();
            String s=StrUtils.nullableString(docTitle);
            if(s!=null)
                sb.append(s).append(" - ");
            sb.append(appName);
            if(w instanceof Frame)
                ((Frame)w).setTitle(sb.substring(0));
            else if(w instanceof Dialog)
                ((Dialog)w).setTitle(sb.substring(0));
        }
    }
    
    public void setWindowTitle(){
        StringBuilder sb=new StringBuilder();
        String prjName=project==null ? null : StrUtils.nullableString(project.getPublicName());
        String actName=actPanel==null ? null : StrUtils.nullableString(actPanel.getActivity().getPublicName());
        if(actName!=null){
            sb.append(actName);
            if(prjName!=null)
                sb.append(" [");
        }
        if(prjName!=null){
            sb.append(prjName);
            if(actName!=null)
                sb.append("]");
        }
        setWindowTitle(sb.substring(0));
    }    
    
    /**
     * FressaFunctions offers special accessibility features
     * like atomatic scanning and voice synthesis.
     * @return The FressaFunctions object, or <CODE>null</CODE> if accessibility features are not enabled
     */
    public edu.xtec.jclic.accessibility.FressaFunctions getFressa(){
        return null;
    }


       
}
