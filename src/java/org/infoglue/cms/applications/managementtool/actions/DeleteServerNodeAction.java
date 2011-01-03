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

package org.infoglue.cms.applications.managementtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.entities.management.ServerNodeVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a serverNode from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteServerNodeAction extends InfoGlueAbstractAction
{
	private ServerNodeVO serverNodeVO;
	private Integer serverNodeId;
	
	public DeleteServerNodeAction()
	{
		this(new ServerNodeVO());
	}

	public DeleteServerNodeAction(ServerNodeVO serverNodeVO) 
	{
		this.serverNodeVO = serverNodeVO;
	}
	
	protected String doExecute() throws ConstraintException, Exception 
	{
	    this.serverNodeVO.setServerNodeId(this.getServerNodeId());
		ServerNodeController.getController().delete(this.serverNodeVO, this.getInfoGluePrincipal());
		return "success";
	}
	
	public void setServerNodeId(Integer serverNodeId) throws SystemException
	{
		this.serverNodeVO.setServerNodeId(serverNodeId);	
	}

    public java.lang.Integer getServerNodeId()
    {
        return this.serverNodeVO.getServerNodeId();
    }
        
	
}
