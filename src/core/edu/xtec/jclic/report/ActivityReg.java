/*
 * File    : ActivityReg.java
 * Created : 11-jul-2001 9:03
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

package edu.xtec.jclic.report;

import edu.xtec.jclic.*;
import edu.xtec.util.Domable;
import edu.xtec.util.Html;
import edu.xtec.util.JDomUtility;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.08
 */
public class ActivityReg extends Object implements java.io.Serializable, Domable{
    
    String name;
    String code;
    long startTime;
    long totalTime;
    List<ActionReg> actions;
    boolean solved;
    ActionReg lastAction;
    int score;
    int minActions;
    boolean closed;
    boolean reportActions;
    int numActions;
    
    /** Creates new ActivityReg */
    public ActivityReg(Activity act) {
        name=act.name;
        code=act.code;
        actions=new ArrayList<ActionReg>();
        startTime=System.currentTimeMillis();
        totalTime=0;
        solved=false;
        score=0;
        lastAction=null;
        closed=false;
        minActions=act.getMinNumActions();
        reportActions=act.reportActions;
        numActions=0;
    }
    
    public ActivityReg(org.jdom.Element e) throws Exception{
        actions=new ArrayList<ActionReg>();
        setProperties(e, null);
    }
    
    public static final String ELEMENT_NAME="activity";
    public static final String NAME="name", CODE="code", 
    START="start", TIME="time", SOLVED="solved", SCORE="score",
    MIN_ACTIONS="minActions", CLOSED="closed", 
    REPORT_ACTIONS="reportActions", ACTIONS="actions";
    
    public org.jdom.Element getJDomElement(){
        org.jdom.Element e=new org.jdom.Element(ELEMENT_NAME);
        JDomUtility.setStringAttr(e, NAME, name, false);
        JDomUtility.setStringAttr(e, CODE, code, false);
        JDomUtility.setStringAttr(e, START, Long.toString(startTime), false);
        JDomUtility.setStringAttr(e, TIME, Long.toString(totalTime), false);
        e.setAttribute(SOLVED, JDomUtility.boolString(solved));
        JDomUtility.setStringAttr(e, SCORE, Integer.toString(score), false);
        JDomUtility.setStringAttr(e, MIN_ACTIONS, Integer.toString(minActions), false);
        if(!closed)
            e.setAttribute(CLOSED, JDomUtility.BOOL_STR[JDomUtility.FALSE]);
        if(reportActions)
            e.setAttribute(REPORT_ACTIONS, JDomUtility.BOOL_STR[JDomUtility.TRUE]);
        JDomUtility.setStringAttr(e, ACTIONS, Integer.toString(numActions), false);
        Iterator<ActionReg> it=actions.iterator();
        while(it.hasNext())
            e.addContent(it.next().getJDomElement());
        return e;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        JDomUtility.checkName(e, ELEMENT_NAME);
        name=JDomUtility.getStringAttr(e, NAME, name, false);
        code=JDomUtility.getStringAttr(e, CODE, code, false);
        startTime=JDomUtility.getLongAttr(e, START, startTime);
        totalTime=JDomUtility.getLongAttr(e, TIME, totalTime);
        solved=JDomUtility.getBoolAttr(e, SOLVED, solved);
        score=JDomUtility.getIntAttr(e, SCORE, score);
        minActions=JDomUtility.getIntAttr(e, MIN_ACTIONS, minActions);
        closed=JDomUtility.getBoolAttr(e, CLOSED, true);
        reportActions=JDomUtility.getBoolAttr(e, REPORT_ACTIONS, false);
        numActions=JDomUtility.getIntAttr(e, ACTIONS, numActions);               
        Iterator it=e.getChildren(ActionReg.ELEMENT_NAME).iterator();
        while(it.hasNext()){
            lastAction=new ActionReg((org.jdom.Element)it.next());
            actions.add(lastAction);
        }        
    }
    
    private static final String okTd="BGCOLOR=\"#90FF90\"";
    private static final String badTd="BGCOLOR=\"#FF9090\"";    
    public String toHtmlString(edu.xtec.util.Messages msg, String firstTd){
        Html html=new Html(300);
        html.tr(true);
        if(firstTd!=null)
            html.append(firstTd);
        html.td(name, false);
        if(closed){
            html.td(msg.get(solved ? "YES":"NO"), Html.CENTER, true, solved ? okTd:badTd);
            html.td(msg.getNumber(numActions), Html.RIGHT, false, null);
            html.td(msg.getPercent(getPrecision()), Html.RIGHT, false, null);
            html.td(msg.getHmsTime(totalTime), Html.RIGHT, false, null);
        }
        else
            html.append("<TD COLSPAN=\"4\" ALIGN=\"center\">").append(msg.get("report_not_finished")).td(false);
        html.tr(false);
        return html.toString();
    }
    
    public void newAction(String type, String source, String dest, boolean ok){
        if(!closed){
            lastAction=new ActionReg(type, source, dest, ok);
            actions.add(lastAction);
        }
    }
    
    public ActionReg getActionReg(int index){
        return index>=actions.size() ? null : actions.get(index);
    }
    
    public void closeActivity(){
        if(!closed){
            if(lastAction!=null){
                totalTime=lastAction.time-startTime;
            } else{
                totalTime=System.currentTimeMillis()-startTime;
            }
            closed=true;
        }
    }
    
    public int getPrecision(){
        int result=0;
        if(closed && minActions>0 && numActions>0){
            if(solved){
                if(numActions<minActions) result=100;
                else result=(minActions*100)/numActions;
            } else{
                result=100*(score*score)/(minActions*numActions);
            }
        }
        return result;        
    }
    
    public void endActivity(int setScore, int setNumActions, boolean isSolved){
        if(!closed){
            solved=isSolved;
            numActions=setNumActions;
            score=setScore;
            closeActivity();
        }
    }
    
    @Override
    public String toString(){
        StringBuilder result=new StringBuilder("ACTIVITY: ");
        result.append(name);
        return result.substring(0);
    }
}
