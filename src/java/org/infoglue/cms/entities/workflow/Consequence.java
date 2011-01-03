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

package org.infoglue.cms.entities.workflow;

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface Consequence extends IBaseEntity
{
    public Integer getId();
     	    
    public ConsequenceVO getValueObject();
    
    public void setValueObject(ConsequenceVO valueObject);

    
    public java.lang.Integer getConsequenceId();
    
    public void setConsequenceId(java.lang.Integer consequenceId);
    
    public org.infoglue.cms.entities.workflow.impl.simple.ConsequenceDefinitionImpl getConsequenceDefinition();
    
    public void setConsequenceDefinition(org.infoglue.cms.entities.workflow.impl.simple.ConsequenceDefinitionImpl consequenceDefinition);
    
    public org.infoglue.cms.entities.workflow.impl.simple.ActionImpl getAction();
    
    public void setAction(org.infoglue.cms.entities.workflow.impl.simple.ActionImpl action);
        
}
