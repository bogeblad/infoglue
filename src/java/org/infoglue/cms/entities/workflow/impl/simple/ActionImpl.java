/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.cms.entities.workflow.impl.simple;

import org.infoglue.cms.entities.workflow.Action;
import org.infoglue.cms.entities.workflow.ActionVO;

public class ActionImpl implements Action
{
    private ActionVO valueObject = new ActionVO();
     
    public ActionVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(ActionVO valueObject)
    {
        this.valueObject = valueObject;
    }   

        private org.infoglue.cms.entities.workflow.impl.simple.ActionDefinitionImpl actionDefinition;
        private org.infoglue.cms.entities.workflow.impl.simple.ActorImpl actor;
        private java.util.Collection consequences;
  
    
    public java.lang.Integer getActionId()
    {
        return this.valueObject.getActionId();
    }
            
    public void setActionId(java.lang.Integer actionId)
    {
        this.valueObject.setActionId(actionId);
    }
      
    public org.infoglue.cms.entities.workflow.impl.simple.ActionDefinitionImpl getActionDefinition()
    {
        return this.actionDefinition;
    }
            
    public void setActionDefinition (org.infoglue.cms.entities.workflow.impl.simple.ActionDefinitionImpl actionDefinition)
    {
        this.actionDefinition = actionDefinition;
    }
      
    public org.infoglue.cms.entities.workflow.impl.simple.ActorImpl getActor()
    {
        return this.actor;
    }
            
    public void setActor (org.infoglue.cms.entities.workflow.impl.simple.ActorImpl actor)
    {
        this.actor = actor;
    }
      
    public java.util.Collection getConsequences()
    {
        return this.consequences;
    }
            
    public void setConsequences (java.util.Collection consequences)
    {
        this.consequences = consequences;
    }
  }        
