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

import org.infoglue.cms.applications.common.actions.TreeViewAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.treeservice.ss.ManagementToolNodeSupplier;

import com.frovi.ss.Tree.INodeSupplier;

/**
 * ViewManagementToolMenuHtmlAction.java
 * Created on 2002-okt-11 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 * 
 * This class implements the use case ViewManagementToolMenuHtmlAction.java
 * The class supplies action classes with methods
 * to carry out the use case
 */
public class ViewManagementToolMenuHtmlAction extends TreeViewAbstractAction
{
	private static final long serialVersionUID = 1L;

	private Integer repositoryId;
	private String name;
    private RepositoryVO repositoryVO;
	
	/**
	 * @see org.infoglue.cms.applications.common.actions.TreeViewAbstractAction#getNodeSupplier()
	 */
	protected INodeSupplier getNodeSupplier() throws Exception
	{
    	if(this.repositoryId != null && this.repositoryId.intValue() > 0)
    	{
	    	this.repositoryVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryId);
	    	this.setName(this.repositoryVO.getName());
    	}
		
		return new ManagementToolNodeSupplier(repositoryId, this.getInfoGluePrincipal());
	}


	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the repositoryId.
	 * @return Integer
	 */
	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Sets the repositoryId.
	 * @param repositoryId The repositoryId to set
	 */
	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

}
