/*
 * File    : BasicReport.java
 * Created : 04-feb-2003 15:12
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

package edu.xtec.jclic.report.rp;

import edu.xtec.jclic.report.SessionData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public abstract class BasicReport extends Basic{
    
    public static final int USR=0, GRP=1, PRJ=2, UNKNOWN=-1;
    
    public static final String EDIT="edit", 
    CHANGE="change", NEW="new", GROUP="group", USER="user", PROJECT="project",
    ACTIVITY="activity", DATE="date", PID="pid";
    
    public static final String WILDCARD="-1";
    
    //public static final String FROM="from", TO="to";
    
    public static final String[] KCC={"sessionKey", "sessionCode", "sessionContext"};
    
    public static final File SDIR=new File(System.getProperty("java.io.tmpdir"));
    
    //protected Date dFrom, dTo, firstDate, today;
    protected String groupId;
    protected String userId;
    protected String projectName;
    protected String activityName;
    protected String[] kcc;
    protected int type=UNKNOWN;
    //private GregorianCalendar calendar;
    protected String pageId;
    protected DateManager dm;
    
    @Override
    public boolean init() throws Exception{
        if(!super.init())
            return false;
        
        dm=new DateManager(this);
        
        userId=getParamNotNull(USER);
        if(userId.length()>0 && (int)userId.charAt(0)==160)
            userId=userId.replace((char)160,' ');
        
        groupId = getParamNotNull(GROUP);
        
        projectName=getParamNotNull(PROJECT);
        
        activityName=getParamNotNull(ACTIVITY);

        kcc=new String[3];
        for(int i=0; i<KCC.length; i++)
            kcc[i]=getParam(KCC[i]);
        
        pageId=getParam(PID);
        if(pageId==null || pageId.length()==0)
            pageId=Long.toString(100000000L+(long)(Math.random()*100000000L));
        
        /*
        firstDate=bridge.getMinSessionDate();
        today=new Date();
        if(firstDate.compareTo(today)>0)
            firstDate=today;
        dFrom=getDateParam(FROM, firstDate, false);
        dTo=getDateParam(TO, today, true);
        if(dFrom.compareTo(dTo)>0)
            dFrom=dTo;
         */
        
        return dm.init();
    }
    
    protected List<SessionData> getSessionList() throws Exception{
        
        List<SessionData> v;
        
        /*
         * Removed serialization to File
         *
        File f=new File(SDIR, "report_"+pageId+".ser");
        if(f.exists()){
            FileInputStream in = new FileInputStream(f);
            ObjectInputStream s = new ObjectInputStream(in);
            v=(List<SessionData>)s.readObject();
        }
        else{
        */
            switch(type){
                case USR:
                    v=bridge.getInfoSessionUser(userId, projectName, dm.dFrom, dm.dTo, kcc, false);
                    break;
                case GRP:
                    v=bridge.getInfoSessionGroup(groupId, projectName, dm.dFrom, dm.dTo, kcc, true);
                    break;
                case PRJ:
                    v=bridge.getInfoSessionAct(projectName, activityName, dm.dFrom, dm.dTo, kcc, true);
                    break;
                default:
                    v=new ArrayList<SessionData>();
            }
         /*   
            FileOutputStream out = new FileOutputStream(f);
            ObjectOutputStream s = new ObjectOutputStream(out);
            s.writeObject(v);
            f.deleteOnExit();
        }
        */
            
        return v;
    }
    
    public String[][] vToArray(List<String> v, String wildCardKey, String wildCardMsg){
        String[][] result=null;
        if(wildCardKey!=null || (v!=null && v.size()>0)){
            int l=(v!=null ? v.size() : 0);
            result=new String[l+(wildCardKey!=null ? 1 : 0)][];
            int k=0;
            if(wildCardKey!=null){
                String s=wildCardKey;
                if(wildCardMsg!=null)
                    s=getMsg(wildCardMsg);
                result[k++]=new String[]{wildCardKey, s};
            }
            if(v!=null){
                for(String s : v){
                    s=s.trim();
                    result[k++]=new String[]{s, s};
                }
            }
        }
        return result;
    }
    
    public String[][] vToArray(List<Object[]> v, int keyIndex, int valueIndex){
        String[][] result=null;
        if(v!=null && v.size()>0){
            int l=v.size();
            result=new String[l][];
            for(int i=0; i<l; i++){
                Object[] o=(Object[])v.get(i);
                result[i]=new String[2];
                result[i][0]=((String)o[keyIndex]).trim();
                result[i][1]=((String)o[valueIndex]).trim();
            }
        }
        return result;        
    }
    
}
