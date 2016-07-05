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

/**
 * This class implements the action class for viewServerNode.
 * The use-case lets the user see all information about a specific site/serverNode.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewServerNodeAction extends InfoGlueAbstractAction
{ 
	private static final long serialVersionUID = 1L;

    private ServerNodeVO serverNodeVO;

    public ViewServerNodeAction()
    {
        this(new ServerNodeVO());
    }
    
    public ViewServerNodeAction(ServerNodeVO serverNodeVO)
    {
        this.serverNodeVO = serverNodeVO;
    }
    
    protected void initialize(Integer serverNodeId) throws Exception
    {
        serverNodeVO = ServerNodeController.getController().getServerNodeVOWithId(serverNodeId);
    } 

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doExecute() throws Exception
    {
    	if(getServerNodeId() == null || getServerNodeId().intValue() == -1)
    		return "redirectToList";
    		
        this.initialize(getServerNodeId());

        return "success";
    }
          
    public java.lang.Integer getServerNodeId()
    {
        return this.serverNodeVO.getServerNodeId();
    }
        
    public void setServerNodeId(java.lang.Integer serverNodeId) throws Exception
    {
        this.serverNodeVO.setServerNodeId(serverNodeId);
    }

    public java.lang.String getName()
    {
        return this.serverNodeVO.getName();
    }

    public java.lang.String getDescription()
    {
        return this.serverNodeVO.getDescription();
    }

    public java.lang.String getDnsName()
    {
        return this.serverNodeVO.getDnsName();
    }

}
