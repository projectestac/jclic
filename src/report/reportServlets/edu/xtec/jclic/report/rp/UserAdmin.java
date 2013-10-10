/*
 * File    : UserAdmin.java
 * Created : 19-feb-2003 16:06
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

import edu.xtec.jclic.report.GroupData;
import edu.xtec.jclic.report.UserData;
import edu.xtec.util.StrUtils;
import java.util.ResourceBundle;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.09
 */
public class UserAdmin extends Basic{
    
    public static String TITLE="user_admin_title";
    public static String URL="userAdmin";
    
    public static final String GROUP="group", ID="id", NAME="name", ICON="icon",
    USER="user";
    
    // 22-mai-06: Added password fields
    public static final String PASS="pass", PASSC="passc";
    // ---
    
    public static final int USER_MENU=0,
    EDIT=1, EDIT_UPDATE=2,
    CREATE=3, CREATE_UPDATE=4,
    DELETE=5, DELETE_UPDATE=6,
    CLEAR=7, CLEAR_UPDATE=8, ERR=9;
    
    public static final String[] ACTIONS={"",
    "edit", "editUpd",
    "create", "createUpd",
    "del", "delUpd",
    "clear", "clearUpd"};
    
    protected int page;
    protected DateManager dm;
    protected GroupData gd;
    protected UserData ud;
    
    protected boolean err=true;
    protected String returnUrl;
    
    public String getTitle(ResourceBundle bundle) {
        /*
        StringBuilder sb=new StringBuilder(bundle.getString(TITLE));
        if(ud!=null && ud.text!=null && ud.text.length()>0)
            sb.append(": ").append(filter(ud.text));
        return sb.toString();
         */
        return bundle.getString(TITLE);
    }
    
    public String getUrl() {
        return urlParam(URL, LANG, lang);
    }
    
    public String getUrl(int action){
        StringBuilder sb=new StringBuilder(200);
        sb.append(URL);
        urlParamSb(sb, LANG, lang, true);
        urlParamSb(sb, ACTION, ACTIONS[action], false);
        if(ud!=null)
            urlParamSb(sb, USER, ud.getId(), false);
        else if(gd!=null)
            urlParamSb(sb, GROUP, gd.getId(), false);            
        return sb.substring(0);
    }
    
    @Override
    public boolean init() throws Exception{
        if(!super.init())
            return false;
        
        String uId=getParamNotNull(USER);
        if(uId.length()>0)
            ud=bridge.getUserData(uId);
        
        String gId=getParamNotNull(GROUP);
        if(ud!=null)
            gId=ud.groupId;
        
        if(gId.length()>0)
            gd=bridge.getGroupData(gId);
        
        page=getAction(ACTIONS, USER_MENU);
        
        if(page==CLEAR || page==CLEAR_UPDATE){
            dm=new DateManager(this);
            if(!dm.init())
                return false;
        }
        
        switch(page){
            case EDIT_UPDATE:
                edit();
                break;
            case CREATE_UPDATE:
                create();
                break;
            case DELETE_UPDATE:
                delete();
                break;
            case CLEAR_UPDATE:
                clear();
                break;
        }
        
        return true;
    }
    
    protected void edit() throws Exception{
        returnUrl=getUrl(USER_MENU);
        errMsg=getMsg("db_error")+"<BR>";
        String name=getParamNotNull(NAME).trim();
        
        // 22-mai-06: Added password fields
        String pass=StrUtils.nullableString(getParam(PASS));
        String passc=StrUtils.nullableString(getParam(PASSC));
        // ---
        
        if(name.length()==0)
            errMsg=errMsg+getMsg("user_admin_invalid_name");
        // 22-mai-06: Added password fields
        else if(!StrUtils.compareObjects(pass, passc))
            errMsg=errMsg+getMsg("user_admin_err_bad_pw");
        // ---
        else{
            ud.setText(name);
            
            // 22-mai-06: Added password fields
            ud.pwd=(pass==null ? null : edu.xtec.util.Encryption.Encrypt(pass));                        
            // ---
            
            ud.setIconUrl(getParamNotNull(ICON).trim());
            try{
                bridge.updateUser(ud, false);
                page=USER_MENU;
            } catch(Exception ex){
                errMsg=errMsg+ex.getLocalizedMessage();
            }
        }                      
    }
    
    protected String getGroupUrl(){
        StringBuilder sb=new StringBuilder(300);
        sb.append(GroupAdmin.URL);
        urlParamSb(sb, LANG, lang, true);
        urlParamSb(sb, ACTION, GroupAdmin.ACTIONS[GroupAdmin.GROUP_MENU], false);
        urlParamSb(sb, GroupAdmin.GROUP, gd.getId(), false);
        return sb.substring(0);
    }
    
    protected void create() throws Exception{
        returnUrl=getGroupUrl();
        errMsg=getMsg("db_error")+"<BR>";
        String name=getParamNotNull(NAME).trim();
        String id=getParamNotNull(ID).trim();
        String icon=getParamNotNull(ICON).trim();
        
        // 22-mai-06: Added password fields
        String pass=StrUtils.nullableString(getParam(PASS));
        String passc=StrUtils.nullableString(getParam(PASSC));
        // ---
        
        if(name.length()==0)
            errMsg=errMsg+getMsg("user_admin_invalid_name");
        else if(id.length()==0)
            errMsg=errMsg+getMsg("user_admin_invalid_id");
        // 22-mai-06: Added password fields
        else if(!StrUtils.compareObjects(pass, passc))
            errMsg=errMsg+getMsg("user_admin_err_bad_pw");
        // ---
        else{
            ud=bridge.getUserData(id);
            if(ud!=null){
                returnUrl=urlParam(urlParam(getUrl(CREATE), NAME, name), ICON, icon);
                errMsg=errMsg+getMsg("user_admin_id_already_exists");
            }
            else{
                try{
                    ud=new UserData(id, name, icon, null, gd.getId());
                    
                    // 22-mai-06: Added password fields
                    ud.pwd=(pass==null ? null : edu.xtec.util.Encryption.Encrypt(pass));                        
                    // ---
                    
                    bridge.updateUser(ud, true);
                    page=USER_MENU;
                } catch(Exception ex){
                    ud=null;
                    errMsg=errMsg+ex.getLocalizedMessage();
                }
            }
        }
    }
    
    protected void delete() throws Exception{
        returnUrl=getUrl(USER_MENU);
        try{
            bridge.deleteUser(ud.getId());
            ud=null;
            errMsg=getMsg("user_admin_user_deleted");
            err=false;
            returnUrl=getGroupUrl();
        } catch(Exception ex){
            returnUrl=getUrl(USER_MENU);
            errMsg=getMsg("db_error")+"<BR>"+ex.getLocalizedMessage();
        }
    }
    
    protected void clear() throws Exception{
        returnUrl=getUrl(USER_MENU);
        try{
            bridge.clearUserReportData(ud.getId(), dm.dFrom, dm.dTo);
            page=USER_MENU;
        } catch(Exception ex){
            errMsg=getMsg("db_error")+"<BR>"+ex.getLocalizedMessage();
        }
    }
    
    @Override
    public void head(java.io.PrintWriter out) throws Exception{
        super.head(out);
        if(page==CLEAR){
            StringBuilder sb=new StringBuilder(300);
            dm.writeDateScript(sb);
            out.println(sb.substring(0));
        }
    }
    
    @Override
    public void body(java.io.PrintWriter out) throws Exception{
        
        super.body(out);
        StringBuilder sb=new StringBuilder(3000);
        StringBuilder sb2=new StringBuilder(500);
        boolean flag=false;
        
        sb.append(linkTo(urlParam(Main.URL, LANG, lang), bundle.getString(Main.TITLE), null));
        sb.append(" | ");
        sb.append(linkTo(urlParam(GroupAdmin.URL, LANG, lang), bundle.getString(GroupAdmin.TITLE), null));
        if(gd!=null){
            sb.append(" | ");
            sb.append(linkTo(getGroupUrl(), gd.getText(), null));
        }
        if(page!=USER_MENU && ud!=null)
            sb.append(" | ").append(linkTo(getUrl(USER_MENU), ud.getText(), null));
        
        standardHeader(out, filter(getTitle(bundle)), sb.substring(0));
        sb.setLength(0);
        
        if(ud==null && page!=CREATE && page!=DELETE_UPDATE){
            page=ERR;
            if(errMsg==null)
                errMsg=getMsg("bad_data");
            err=true;
        }
        
        switch(page){
            
            case USER_MENU:
                sb.append("<form class=\"info\">\n");
                sb.append("<p><strong>").append(getMsg("user_admin_id")).append("</strong> ").append(filter(ud.getId())).append("</p>\n");
                sb.append("<p><strong>").append(getMsg("user_admin_name")).append("</strong> ").append(filter(ud.getText())).append("</p>\n");
                
                // 22-mai-06: Added password fields
                sb.append("<p><strong>").append(getMsg("user_admin_pw_prompt")).append("</strong> ").append(ud.pwd==null ? "" : "***").append("</p>\n");
                // ---
                
                sb.append("<p><strong>").append(getMsg("user_admin_icon")).append("</strong> ");
                if(ud.getIconUrl()!=null && ud.getIconUrl().length()>0){
                    sb.append("<img src=\"").append(filter(ud.getIconUrl())).append("\"");
                    sb.append(" title=\"").append(filter(ud.getIconUrl())).append("\">");
                }
                else
                    sb.append("---\n");
                sb.append("</p>\n");
                sb.append("<p>");
                sb.append(buttonTo(getUrl(EDIT), getMsg("user_admin_edit_button"), null));
                sb.append(buttonTo(getUrl(DELETE), getMsg("user_admin_delete_button"), null));
                sb.append(buttonTo(getUrl(CLEAR), getMsg("user_admin_clear_button"), null));
                sb.append("</p>\n");
                sb.append("</form>\n");
                sb.append("<br clear=\"all\">\n");
                break;
                
            case EDIT:
                flag=true;
            case CREATE:
                String id=(ud!=null ? ud.getId() : getParamNotNull(ID).trim());
                String name=(ud!=null ? ud.getText() : getParamNotNull(NAME).trim());
                String icon=(ud!=null ? ud.getIconUrl() : getParamNotNull(ICON).trim());

                // 22-mai-06: Added password fields                
                String pass=(ud!=null ? edu.xtec.util.Encryption.Decrypt(ud.pwd) : getParam(PASS));
                // ---
                
                sb.append("<form class=\"inputForm\" method=\"post\" action=\"").append(getUrl(flag ? EDIT_UPDATE : CREATE_UPDATE)).append("\">\n");
                sb.append("<p><strong>").append(getMsg("user_admin_id")).append("</strong> ");
                sb.append("<input name=\"").append(ID).append("\" value=\"").append(filter(id)).append("\" size=40 ");
                if(flag)
                    sb.append("readonly");
                sb.append(">\n");
                
                sb.append("<p><strong>").append(getMsg("user_admin_name")).append("</strong> ");
                sb.append("<input name=\"").append(NAME).append("\" value=\"").append(filter(name)).append("\" size=40></p>\n");

                // 22-mai-06: Added password fields                
                sb.append("<p><strong>").append(getMsg("user_admin_pw_prompt")).append("</strong> ");
                sb.append("<input name=\"").append(PASS).append("\" type=\"password\" value=\"").append(pass==null ? "" : filter(pass)).append("\" size=20><br>\n");
                sb.append("<strong>").append(getMsg("user_admin_pw_prompt_confirm")).append("</strong> ");
                sb.append("<input name=\"").append(PASSC).append("\" type=\"password\" value=\"").append(pass==null ? "" : filter(pass)).append("\" size=20></p>\n");
                // ---
                
                sb.append("<p><strong>").append(getMsg("user_admin_icon")).append("</strong> ");
                sb.append("<input name=\"").append(ICON).append("\" value=\"").append(filter(icon)).append("\" size=40></p>\n");
                sb.append("<p><input type=\"submit\" value=\"").append(getMsg("submit")).append("\"> ");
                sb.append(buttonTo(flag ? getUrl(USER_MENU) : getGroupUrl(), getMsg("cancel"), null)).append("</p>\n");
                sb.append("</form>\n");
                sb.append("<br clear=\"all\">\n");
                break;
                
            case DELETE:
                sb.append("<p><strong>").append(getMsg("user_admin_delete_user")).append(" \"").append(filter(ud.getText())).append("\"</strong></p>\n");
                sb.append("<p>").append(getMsg("user_admin_delete_user_explain")).append("</p>\n");
                sb.append("<p>").append(getMsg("report_areyousure")).append("</p>\n");
                sb.append("<form method=\"post\" action=\"").append(getUrl(DELETE_UPDATE)).append("\">\n");
                sb.append("<p><input type=\"submit\" value=\"").append(getMsg("YES")).append("\" width=50> ");
                sb.append(buttonTo(getUrl(USER_MENU), getMsg("NOT"), "width=50"));
                sb.append("</p>\n");
                sb.append("</form>\n");
                break;
                
            case CLEAR:
                sb.append("<p><strong>").append(getMsg("user_admin_clear_user")).append(" \"").append(filter(ud.getText())).append("\"</strong></p>\n");
                sb.append("<p>").append(getMsg("user_admin_clear_user_explain")).append("</p>\n");
                sb.append("<form class=\"inputForm\" action=\"").append(getUrl(CLEAR_UPDATE)).append("\" method=\"post\" name=\"").append(MAIN_FORM).append("\">\n");
                dm.writeHiddenFields(sb);
                sb2.setLength(0);
                sb2.append("document.").append(MAIN_FORM).append(".submit()");
                dm.zonaData(sb, buttonAction(sb2.substring(0), getMsg("db_clear_reports_date"), null));
                sb.append("</form>\n");
                sb.append("<br clear=\"all\">\n");
                break;
                
            default:
                sb.append("<p><strong>").append(getMsg(err ? "error" : "success")).append("</strong></p>\n");
                if(errMsg!=null)
                    sb.append("<p>").append(errMsg).append("</p>\n");
                if(returnUrl!=null)
                    sb.append("<p><a href=\"").append(returnUrl).append("\">").append(getMsg("return")).append("</a></p>\n");
        }
        out.println(sb.substring(0));
    };
}
