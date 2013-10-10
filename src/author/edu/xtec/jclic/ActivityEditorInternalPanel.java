/*
 * File    : ActivityEditorInternalPanel.java
 * Created : 30-sep-2002 09:41
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

import edu.xtec.util.Options;

/**
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 1.0
 */
public abstract class ActivityEditorInternalPanel extends edu.xtec.util.CtrlPanel {
    
    protected Options options;
    protected ActivityEditorPanel parent;
    
    /** Creates a new instance of ActivityEditorInternalPane */
    public ActivityEditorInternalPanel(ActivityEditorPanel parent) {
        this.parent=parent;
        this.options=parent.getOptions();
        //setOpaque(false);
    }
    
    public Options getOptions(){
        return options;
    };    
    
    public ActivityEditor getActivityEditor(){
        return parent.getActivityEditor();
    }
    
    public Activity getActivity(){
        ActivityEditor ae=getActivityEditor();
        return ae==null ? null : ae.getActivity();
    }
    
    protected abstract void fillData();
    
    protected abstract void saveData();
    
    protected abstract javax.swing.Icon getIcon();
    
    protected abstract String getTitle();    
    
    protected abstract String getTooltip();

    // A ELIMINAR:
    protected final void resetPanel(java.util.EventObject ev){
    }    
    protected final void clear(){
    };    
}
