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

package org.infoglue.cms.applications.contenttool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;

/**
 * This action removes a content from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteDigitalAssetAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private Integer digitalAssetId;
	private String entity;
	private Integer entityId;
	private String returnAddress;
	
	
	protected String doExecute() throws Exception 
	{
	    DigitalAssetController.getController().delete(digitalAssetId, entity, entityId);
		
	    this.getResponse().sendRedirect(returnAddress);
	    
	    return NONE;
	}
	
    public Integer getDigitalAssetId()
    {
        return digitalAssetId;
    }
    
    public void setDigitalAssetId(Integer digitalAssetId)
    {
        this.digitalAssetId = digitalAssetId;
    }
    
    public String getEntity()
    {
        return entity;
    }
    
    public void setEntity(String entity)
    {
        this.entity = entity;
    }
    
    public Integer getEntityId()
    {
        return entityId;
    }
    
    public void setEntityId(Integer entityId)
    {
        this.entityId = entityId;
    }

    public String getReturnAddress()
    {
        return returnAddress;
    }
    
    public void setReturnAddress(String returnAddress)
    {
        this.returnAddress = returnAddress;
    }
}
