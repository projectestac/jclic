/*
 * File    : ProjectSettingsEditor.java
 * Created : 27-feb-2004 11:39
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

package edu.xtec.jclic.project;

import edu.xtec.jclic.edit.Editor;
import edu.xtec.jclic.edit.EditorPanel;
import edu.xtec.util.Options;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ProjectSettingsEditor extends Editor{
    
    /** Creates a new instance of ProjectSettingsEditor */
    public ProjectSettingsEditor(ProjectSettings ps) {
        super(ps);
    }
    
    protected void createChildren() {
    }
    
    public EditorPanel createEditorPanel(Options options) {
        return new ProjectSettingsEditorPanel(options);
    }
    
    public Class getEditorPanelClass() {
        return ProjectSettingsEditorPanel.class;
    }
    
    public ProjectSettings getProjectSettings(){
        return (ProjectSettings)getFirstObject(ProjectSettings.class);
    }            
    
    public JClicProjectEditor getProjectEditor(){
        return (JClicProjectEditor)getFirstParent(JClicProjectEditor.class);
    }    
    
}
