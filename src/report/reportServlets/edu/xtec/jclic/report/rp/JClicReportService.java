/*
 * File    : JClicReportService.java
 * Created : 26-mar-2003 16:30
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

import edu.xtec.jclic.report.ActionReg;
import edu.xtec.jclic.report.ActivityReg;
import edu.xtec.jclic.report.BasicJDBCBridge;
import edu.xtec.jclic.report.GroupData;
import edu.xtec.jclic.report.ReportServerEventMaker;
import edu.xtec.jclic.report.TCPReportBean;
import edu.xtec.jclic.report.UserData;
import edu.xtec.util.Domable;
import edu.xtec.util.JDomUtility;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version(13.09.17)
 */
public class JClicReportService extends ReportsRequestProcessor{
    
    TCPReportBean request=null, response=null;
    public static ReportServerEventMaker eventMaker=new ReportServerEventMaker();
    boolean trustClientDateTime=false;

    @Override
    public boolean init() throws Exception{
        if(!super.init())
            return false;
        
        InputStream is = getInputStream();
        
        if (is != null) {
          if (prop != null) {
            trustClientDateTime = "true".equalsIgnoreCase((String) prop.get(BasicJDBCBridge.TRUST_CLIENT_DATETIME));
          }

          org.jdom.Document doc = JDomUtility.getSAXBuilder().build(is);
          request = new TCPReportBean(doc.getRootElement());
          response = processRequest(request);
        }
        else
          response = new TCPReportBean();
        
        return true;
    }
    
    @Override
    public boolean usesWriter(){
        return false;
    }
    
    @Override
    public boolean wantsInputStream(){
        return true;
    }
    
    @Override
    public void process(java.io.OutputStream out) throws Exception{
        JDomUtility.saveDocument(out, response.getJDomElement());
    };
        
    @Override
    public void header(List<String[]> v){
        super.header(v);
        v.add(new String[]{CONTENT_TYPE, "text/xml"});
        // 8-Jun-16: Added support for Cross-Domain requests
        // 12-Jun-16: Support also Preflight requests sent via OPTIONS method
        // TODO: Allow to set specific values for CORS
        v.add(new String[]{EXTRA, "Access-Control-Allow-Origin", "*"});
        v.add(new String[]{EXTRA, "Access-Control-Allow-Methods", "POST, OPTIONS"});        
        v.add(new String[]{EXTRA, "Access-Control-Allow-Headers", "Content-Type"});
        v.add(new String[]{EXTRA, "Access-Control-Allow-Max-Age", "1728000"});
    }
    
    private TCPReportBean processRequest(TCPReportBean bean) throws Exception{
        
        TCPReportBean result=new TCPReportBean(TCPReportBean.ERROR);
        
        String id=bean.getId();        
        if(TCPReportBean.MULTIPLE.equals(id)){
            Domable[] beans=bean.getData();
            for(int i=0; i<beans.length; i++)
                result=processRequest((TCPReportBean)beans[i]);
        }
        else if(TCPReportBean.ADD_SESSION.equals(id)){
            String sessionId=bridge.addSession
            (bean.getParam(TCPReportBean.USER)
            ,trustClientDateTime ? 
               Long.parseLong(bean.getParam(TCPReportBean.TIME))
               : System.currentTimeMillis()
            ,bean.getParam(TCPReportBean.PROJECT)
            ,bean.getParam(TCPReportBean.CODE)
            ,bean.getParam(TCPReportBean.KEY)
            ,bean.getParam(TCPReportBean.CONTEXT));
            result.setParam(TCPReportBean.SESSION, sessionId);
            result.setId(TCPReportBean.ADD_SESSION);
        }
        else if(TCPReportBean.ADD_ACTIVITY.equals(id)){
            ActivityReg ar=(ActivityReg)bean.getSingleData();
            int actId=Integer.parseInt(bean.getParam(TCPReportBean.NUM));
            String sessionId=bean.getParam(TCPReportBean.SESSION);
            
            int actIdR=bridge.addActivity(actId, sessionId, ar);
            
            ActionReg actReg;
            for(int arc=0; (actReg=ar.getActionReg(arc))!=null; arc++)
                bridge.addAction(actId, sessionId, arc, actReg);
            
            result.setParam(TCPReportBean.ACTIVITY, Integer.toString(actIdR));
            result.setId(TCPReportBean.ADD_ACTIVITY);
        }
        else if(TCPReportBean.NEW_GROUP.equals(id)){
            String groupId=bridge.newGroup((GroupData)bean.getSingleData());
            result.setParam(TCPReportBean.GROUP, groupId);
            result.setId(TCPReportBean.NEW_GROUP);
        }
        else if(TCPReportBean.NEW_USER.equals(id)){
            String userId=bridge.newUser((UserData)bean.getSingleData());
            result.setParam(TCPReportBean.USER, userId);
            result.setId(TCPReportBean.NEW_USER);
        }
        else if(TCPReportBean.GET_USER_DATA.equals(id)){
            result.setData(bridge.getUserData(bean.getParam(TCPReportBean.USER)));
            result.setId(TCPReportBean.GET_USER_DATA);
        }
        else if(TCPReportBean.GET_GROUP_DATA.equals(id)){
            result.setData(bridge.getGroupData(bean.getParam(TCPReportBean.GROUP)));
            result.setId(TCPReportBean.GET_GROUP_DATA);
        }
        else if(TCPReportBean.GET_PROPERTY.equals(id)){
            String k=bean.getParam(TCPReportBean.KEY);
            String dv=bean.getParam(TCPReportBean.DEFAULT);
            result.setParam(TCPReportBean.RESULT, bridge.getProperty(k,dv));
            result.setId(TCPReportBean.GET_PROPERTY);
        }
        else if(TCPReportBean.GET_PROPERTIES.equals(id)){
            Map<String, String> map=bridge.getProperties();
            for (String key : map.keySet()) {
                result.setParam(key, map.get(key));
            }
            result.setId(TCPReportBean.GET_PROPERTIES);
        }
        else if(TCPReportBean.GET_GROUPS.equals(id)){
            List<GroupData> v=bridge.getGroups();
            result.setData((GroupData[])v.toArray(new GroupData[v.size()]));
            result.setId(TCPReportBean.GET_GROUPS);
        }
        else if(TCPReportBean.GET_USERS.equals(id)){
            List<UserData> v=bridge.getUsers(bean.getParam(TCPReportBean.GROUP));
            result.setData((UserData[])v.toArray(new UserData[v.size()]));
            result.setId(TCPReportBean.GET_USERS);
        }
        else{
            //fireReportServerEvent(ReportServerEvent.DB, "unknow command", socket, ReportServerEvent.ERROR);
            result.setParam(TCPReportBean.ERROR, "unknown command");
        }
        
        // Mirar qus el que es reporta!!!
        eventMaker.fireReportServerSystemEvent(id);
        
        return result;
    }
    
    
}
