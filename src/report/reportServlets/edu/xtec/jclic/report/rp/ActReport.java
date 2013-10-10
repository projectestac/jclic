/*
 * File    : ActReport.java
 * Created : 12-feb-2003 12:56
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
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class ActReport extends Report{
    
    public static String TITLE="act_report_title";
    public static String URL="actReport";
    List<String> activities;
    List<SessionData> sessionList;
    
    public String getTitle(ResourceBundle bundle){
        return bundle.getString(TITLE);
    }
    
    public String getUrl(){
        return URL;
    }
    
    @Override
    public boolean init() throws Exception{

        type=PRJ;
                
        if(!super.init())
            return false;
        
        projects=bridge.getProjList(null, dm.dFrom, dm.dTo, kcc);
        
        //projectList=vToArray(
        //bridge.getProjList(null, dm.dFrom, dm.dTo, kcc),
        //null, null);
                
        if((projectName==null || projectName.length()==0 || projectName.equals(WILDCARD)) && projects!=null && projects.size()>0){
            projectName=(String)projects.get(0);
            activityName=WILDCARD;
        }
        
        activities=bridge.getActList(projectName, dm.dFrom, dm.dTo, kcc);
        
        /*
        activityList=vToArray(
        bridge.getActList(projectName, dm.dFrom, dm.dTo, kcc),
        WILDCARD, "report_all_activities");
         */
        
        sessionList=getSessionList();
        
        return true;
    }
    
    @Override
    public void body(java.io.PrintWriter out) throws Exception{
        
        super.body(out);
        StringBuilder sb=new StringBuilder(3000);
        
        sb.append("<div class=\"inputForm\">\n");        
        zona(sb, "report_project", PROJECT, true, opcioDefecte, 
        vectorToArray(projects, false), projectName, isEditable, 180);        
        zona(sb, "report_activity", ACTIVITY, true, opcioDefecte, 
        vectorToArray(activities, false, WILDCARD, "report_all_activities"), 
        activityName, isEditable, 180);        
        sb.append("</div>\n");
        
        zonaParams(sb);

        sb.append("<div class=\"inputForm\">\n");
        zonaData(sb);        
        sb.append("</div>\n");
        sb.append("</form>\n");
        sb.append("<br clear=\"all\">\n");
        
        sb.append("<p>\n");
        if(sessionList.isEmpty()){
            sb.append(getMsg("report_no_data")).append("\n</p>\n");
        }
        else{
            grafic(sb, Img.PROJECT_GRAPH, false, true);
            sb.append("\n");
            grafic(sb, Img.PROJECT_GRAPH, true, true);
            sb.append("\n</p>\n");
            
            resumGlobal(sb, sessionList, "tblA", "float: left; margin-right: 10px;");
            
            llistaSessions(sb, sessionList, false, "tblA", null);
        }
        out.println(sb.substring(0));
    }
    
}
