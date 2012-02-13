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

package org.infoglue.cms.applications.structuretool.actions;

import org.infoglue.cms.applications.common.actions.TreeViewAbstractAction;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.treeservice.ss.SiteNodeNodeSupplier;
import org.infoglue.cms.util.CmsPropertyHandler;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.INodeSupplier;

public class ViewStructureToolMenuHtmlAction extends TreeViewAbstractAction
{
	private static final long serialVersionUID = 1L;

	private Integer repositoryId;
	private Integer select = -1;
	private String treeMode = "classic";
	private BaseNode rootNode = null; 
	
	public String doBindingView() throws SystemException
	{
		super.doExecute();
        
		return "bindingView";
	}

	public String doBindingViewV3() throws SystemException
	{
		super.doExecute();
        
		return "bindingViewV3";
	}

	/**
	 * @see org.infoglue.cms.applications.common.actions.TreeViewAbstractAction#getNodeSupplier()
	 */
	protected INodeSupplier getNodeSupplier() throws SystemException
	{
		if(getRepositoryId() == null  || getRepositoryId().intValue() < 1)
			this.repositoryId = super.getRepositoryId();
		
		String treeMode = CmsPropertyHandler.getTreeMode(); 
		if(treeMode != null) setTreeMode(treeMode);
		SiteNodeNodeSupplier sup = new SiteNodeNodeSupplier(getRepositoryId(), this.getInfoGluePrincipal());
		rootNode = sup.getRootNode();
	    return sup;
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
	 * Sets the repositoryId.
	 * @param repositoryId The repositoryId to set
	 */
	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	/**
	 * Returns the select.
	 * @return Integer
	 */
	public Integer getSelect()
	{
		return select;
	}

	/**
	 * Sets the select.
	 * @param select The select to set
	 */
	public void setSelect(Integer select)
	{
		this.select = select;
	}

	public String getTreeMode() {
		return treeMode;
	}
	public void setTreeMode(String treeMode) {
		this.treeMode = treeMode;
	}
	public BaseNode getRootNode() {
		return rootNode;
	}
	public void setRootNode(BaseNode rootNode) {
		this.rootNode = rootNode;
	}
}
