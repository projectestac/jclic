/*
 * File    : ExtendedPlayer.java
 * Created : 25-oct-2001 10:10
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

import edu.xtec.jclic.accessibility.FressaFunctions;
import edu.xtec.jclic.fileSystem.*;
import edu.xtec.jclic.misc.Utils;
import edu.xtec.jclic.project.*;
import edu.xtec.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.29
 */
public class ExtendedPlayer extends Player {
    
    JFrame debugFrame=null;
    public static final String MESSAGES_BUNDLE="messages.PlayerMessages";
    protected int recentFilesOffset;
    protected JMenuBar menuBar;
    protected JMenu fileMenu, recentFilesMenu, activityMenu, toolsMenu, helpMenu;
    protected PlayerSettings settings;
    protected FressaFunctions fressa;

    
    /** Creates new ExtendedPlayer */
    public ExtendedPlayer(Options options){
        super(options);
    }
    
    @Override
    protected void init(){
        //settings=PlayerSettings.loadPlayerSettings(this, options);
        settings=PlayerSettings.loadPlayerSettings(this);
        super.init();
        settings.checkLibrary();
        if(settings.fressaEnabled)
            fressa=new FressaFunctions(options);
    }
    
    protected void doInstall(String fileName){
        if(settings.promptPassword(this, new String[]{"install_info_description", "settings_passwordRequired"})){
            try{                
                ProjectInstallerDlg pi=ProjectInstallerDlg.getProjectInstallerDlg(this, settings.libraryManager, fileName);
                if(pi!=null){
                    pi.setVisible(true);
                    if(pi.result!=null && !pi.cancel){
                        if(pi.launchNow && pi.pathToMainProject!=null)
                            load(pi.pathToMainProject, null);
                        else
                            launchProjectLibrary(pi.result);
                    }
                }
            } catch(Exception ex){
                System.err.println("Error installing: "+ex.getMessage());
            }
        }
    }
    
    @Override
    public boolean start(String fullPath, String sequence){
        boolean result=super.start(fullPath, sequence);
        if(!result){
            try{
                ProjectLibrary pl=settings.libraryManager.getAutoStartProjectLibrary();
                if(pl!=null)
                    launchProjectLibrary(pl);
            } catch(Exception ex){
                System.err.println("Error loading autoStart project library!\n"+ex);
            }
        }
        return result;
    }
            
    @Override
    public boolean load(String fullPath, String sequence){
        boolean result=false;
        if(fullPath!=null && sequence==null && fullPath.endsWith(ProjectInstaller.INSTALLER_EXTENSION))
            doInstall(fullPath);
        else
            result=super.load(fullPath, sequence);            
        return result;
    }
    
    @Override
    public Messages setMessages(){
        super.setMessages();
        messages.addBundle(MESSAGES_BUNDLE);
        return messages;
    }
    
    @Override
    protected FileSystem createFileSystem(){
        return settings.fileSystem;
    }
    
    protected void createMenu(){                
        
        menuBar=new JMenuBar();        
        
        fileMenu=new JMenu(messages.get("m_File"));
        fileMenu.setMnemonic(messages.get("m_File_Mnemonic").charAt(0));
        fileMenu.add(new KJMenuItem(getAction(ACTION_OPEN_FILE)));
        fileMenu.add(new KJMenuItem(getAction(ACTION_OPEN_URL)));
        menuBar.add(fileMenu);
        
        activityMenu=new JMenu(messages.get("m_Activity"));
        activityMenu.setMnemonic(messages.get("m_Activity_Mnemonic").charAt(0));
        activityMenu.add(new KJMenuItem(getAction(ACTION_NEXT)));
        activityMenu.add(new KJMenuItem(getAction(ACTION_PREV)));
        activityMenu.add(new KJMenuItem(getAction(ACTION_RETURN)));
        activityMenu.add(new KJMenuItem(getAction(ACTION_RESET)));
        activityMenu.addSeparator();
        activityMenu.add(new KJMenuItem(getAction(ACTION_INFO)));
        activityMenu.add(new KJMenuItem(getAction(ACTION_HLP)));
        activityMenu.add(new KJMenuItem(getAction(ACTION_REPORTS)));
        activityMenu.addSeparator();
        activityMenu.add(new KJMenuItem(getAction(ACTION_AUDIO)));
        menuBar.add(activityMenu);
        
        toolsMenu=new JMenu(messages.get("m_Tools"));
        toolsMenu.setMnemonic(messages.get("m_Tools_Mnemonic").charAt(0));
        toolsMenu.add(new KJMenuItem(getAction(ACTION_LIBRARIES)));
        toolsMenu.add(new KJMenuItem(getAction(ACTION_SETTINGS)));
        menuBar.add(toolsMenu);
        
        helpMenu=new JMenu(messages.get("m_Help"));
        helpMenu.setMnemonic(messages.get("m_Help_Mnemonic").charAt(0));
        helpMenu.add(new KJMenuItem(getAction(ACTION_ABOUT)));
        menuBar.add(helpMenu);        
    }
    
    protected void postCreateMenu(){                        
        fileMenu.addSeparator();
        fileMenu.add(new KJMenuItem(getAction(ACTION_EXIT)));
        fileMenu.addSeparator();
        recentFilesMenu=fileMenu;
        recentFilesOffset=fileMenu.getItemCount();
        updateRecentFilesMenu();        
    }
    
    @Override
    public void addTo(RootPaneContainer cont, Object constraints){
        super.addTo(cont, constraints);
        checkMenu(false);
    }
    
    protected void checkMenu(boolean recreate){
        JRootPane rp=getRootPane();
        if(rp==null)
            return;
        
        if(recreate || rp.getJMenuBar()==null){
            if(recreate || menuBar==null){                
                createMenu();
                postCreateMenu();
            }
            rp.setJMenuBar(menuBar);
            rp.revalidate();
        }
    }
    
    protected void updateRecentFilesMenu(){
        if(recentFilesMenu!=null && recentFilesOffset>=0){
            JMenuItem jmi;
            int itemsToRemove=recentFilesMenu.getItemCount()-recentFilesOffset;
            for(int i=0; i<itemsToRemove; i++)
                recentFilesMenu.remove(recentFilesOffset);
            for(int i=0; i<PlayerSettings.MAX_RECENT; i++){
                if(settings.recentFiles[i]!=null){
                    String s=settings.recentFiles[i];
                    int k=s.lastIndexOf('\\');
                    if(k<0)
                        k=s.lastIndexOf('/');
                    if(k>=0)
                        s=s.substring(k+1);
                    createMenuItem(recentFilesMenu, Integer.toString(i+1)+". "+s, "recent"+i, true, KeyStroke.getKeyStroke(KeyEvent.VK_1+i, ActionEvent.ALT_MASK));
                }
            }            
        }
    }
    
    JMenuItem createMenuItem(JComponent parent, String text, String actionCommand, boolean mnemonic, KeyStroke accelerator){
        JMenuItem jmi=new JMenuItem(text);
        if(actionCommand!=null){
            jmi.setActionCommand(actionCommand);
            jmi.addActionListener(this);
        }
        if(mnemonic)
            jmi.setMnemonic(jmi.getText().charAt(0));
        if(accelerator!=null)
            jmi.setAccelerator(accelerator);
        parent.add(jmi);
        return jmi;
    }
    
    public static final int 
    ACTION_OPEN_FILE=NUM_ACTIONS, 
    ACTION_OPEN_URL=NUM_ACTIONS+1, 
    ACTION_EXIT=NUM_ACTIONS+2, 
    ACTION_SETTINGS=NUM_ACTIONS+3, 
    ACTION_LIBRARIES=NUM_ACTIONS+4, 
    ACTION_ABOUT=NUM_ACTIONS+5, 
    NUM_ACTIONS_EXT=NUM_ACTIONS+6;
    
    public static final String[] ACTION_NAME_EXT={"openFile", "openUrl", "exit", "settings", "libraries", "helpAbout"};
    public static final String[] ACTION_ICONS_EXT={
        "icons/file_open.gif",
        "icons/world.gif",
        "icons/exit_small.gif",
        "icons/settings.gif",
        "icons/database.gif",
        "icons/help.gif"
    };
        
    @Override
    protected int getNumActions(){
        return NUM_ACTIONS_EXT;
    }
    
    @Override
    protected void buildActions(){
        super.buildActions();
        actions[ACTION_OPEN_FILE]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                FileSystem fs=project.getFileSystem();
                if(fs.isUrlBased())
                    fs=settings.fileSystem;
                int[] filters={Utils.ALL_CLIC_FF, Utils.INSTALL_FF, Utils.ALL_JCLIC_FF};                
                String result=fs.chooseFile(null, false, filters, options, null, ExtendedPlayer.this, false);
                if(result!=null){
                    String fileName=fs.getFullFileNamePath(result);
                    if(load(fileName, null))
                        addRecentFile(fileName);
                }
            }
        };        
        
        actions[ACTION_OPEN_URL]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                String url=messages.showInputDlg(ExtendedPlayer.this, "URL_OPEN", "URL", "http://", "URL_OPEN", false);
                if(url!=null){
                    url=url.trim();
                    // Check if the protocol was entered twice in the string
                    //if(url.startsWith("http://http://"))
                    if(url.indexOf("://", 7)>=0)
                        url=url.substring(7);
                    if(url.length()>0 && !url.equals("http://")){                        
                        if(load(url, null))
                            addRecentFile(url);
                    }
                }
            }
        };        
        
        actions[ACTION_EXIT]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                exit();
            }
        };        
        
        actions[ACTION_SETTINGS]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                String currentLook=options.getString(LFUtil.LOOK_AND_FEEL);
                String currentLanguage=options.getString(Messages.LANGUAGE);
                String currentCountry=options.getString(Messages.COUNTRY);
                String currentVariant=options.getString(Messages.VARIANT);                
                String currentSkin=options.getString(SKIN);
                String currentReporterClass=settings.reporterClass;
                String currentReporterParams=settings.reporterParams;
                boolean currentReporterEnabled=settings.reporterEnabled;
                String currentMediaSystem=settings.mediaSystem;
                if(settings.edit(ExtendedPlayer.this)){
                    settings.save();
                    options.syncProperties(settings.getProperties(), false);
                    if(!settings.skin.equals(currentSkin)){
                        initSkin();
                        setSkin(null);
                    }
                    else if(getSkin()!=null){
                        AbstractButton bt=getSkin().getButton(ACTION_AUDIO);
                        if(bt!=null)
                            bt.setSelected(!audioEnabled);
                    }
                    boolean recreateMenu=false;
                    if(!settings.lookAndFeel.equals(currentLook)){
                        options.setLookAndFeel();
                        recreateMenu=true;
                    }
                    if(settings.language!=null && 
                       (!StrUtils.compareObjects(settings.language, currentLanguage) ||
                        !StrUtils.compareObjects(settings.country, currentCountry) ||
                        !StrUtils.compareObjects(settings.variant, currentVariant))){
                        setMessages();
                        recreateMenu=true;
                    }
                    if(recreateMenu){
                        checkMenu(true);
                    }
                    if(settings.reporterEnabled &&
                    (!settings.reporterClass.equals(currentReporterClass) ||
                    !settings.reporterParams.equals(currentReporterParams))){
                        initReporter();
                    }
                    audioEnabled=settings.soundEnabled;
                    edu.xtec.jclic.media.EventSounds.globalEnabled=settings.systemSounds;
                    if(!currentMediaSystem.equals(settings.mediaSystem)){
                        options.put(MEDIA_SYSTEM, settings.mediaSystem);
                        edu.xtec.jclic.media.CheckMediaSystem.check(options, false);                        
                        createEventSounds();
                    }
                }
            }
        };        
        
        actions[ACTION_LIBRARIES]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                try{
                    ProjectLibrary pl=settings.libraryManager.selectProjectLibrary(true, false);
                    if(pl!=null)
                        launchProjectLibrary(pl);
                } catch(Exception ex){
                    messages.showErrorWarning(ExtendedPlayer.this, "error_launchLibrary", ex);
                }
            }
        };        
        
        actions[ACTION_ABOUT]=new AbstractAction(){
            public void actionPerformed(ActionEvent ev){
                showAbout(false);
            }
        };        
    }
    
    @Override
    protected String getActionName(int actionId){        
        if(actionId>=NUM_ACTIONS && actionId<NUM_ACTIONS_EXT)
            return ACTION_NAME_EXT[actionId-NUM_ACTIONS];
        return super.getActionName(actionId);
    }
    
    @Override
    protected Icon getActionIcon(int actionId){
        if(actionId>=NUM_ACTIONS && actionId<NUM_ACTIONS_EXT)
            return ResourceManager.getImageIcon(ACTION_ICONS_EXT[actionId-NUM_ACTIONS]);
        return super.getActionIcon(actionId);
    }
    
    @Override
    protected boolean processActionEvent(String ac){
        if(ac.startsWith("recent") && ac.length()>6){
            try{
                int i=Integer.parseInt(ac.substring(6));
                if(i>=0 && i<PlayerSettings.MAX_RECENT && settings.recentFiles[i]!=null)
                    load(settings.recentFiles[i], null);
            } catch(Exception ex){
                System.err.println("invalid command: "+ac);
            }            
        }
        else
            return super.processActionEvent(ac);
        
        return true;
    }
    
    protected void launchProjectLibrary(ProjectLibrary pl){
        if(pl!=null){            
            setProject(null);
            edu.xtec.jclic.media.MediaContent mc=new edu.xtec.jclic.media.MediaContent();
            mc.externalParam=pl.getFullPath();
            mc.mediaFileName=pl.activitySequence.getElement(0, false).getTag();            
            mc.mediaType=edu.xtec.jclic.media.MediaContent.RUN_CLIC_PACKAGE;
            playMedia(mc, null);
        }
    }
    
    protected void addRecentFile(String fName){
        settings.addRecentFile(fName);
        updateRecentFilesMenu();        
        settings.save();
    }


    /**
     * FressaFunctions offers special accessibility features
     * like atomatic scanning and voice synthesis.
     * @return The FressaFunctions object, or <CODE>null</CODE> if accessibility features are not enabled
     */
    @Override
    public edu.xtec.jclic.accessibility.FressaFunctions getFressa(){
        return fressa;
    }



}
