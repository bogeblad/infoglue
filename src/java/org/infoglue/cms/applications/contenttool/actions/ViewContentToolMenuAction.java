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

/**
 * This class implements the action class for the menu in the content tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewContentToolMenuAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
    private Integer repositoryId;
    private String tree;
    private String showVersions;
    private String path;
    
    public void setRepositoryId(Integer repositoryId)
    {
    	if (this.showVersions == null || this.showVersions.equals("")) 
			this.showVersions = (String)getRequest().getSession().getAttribute("htmlTreeShowVersions");
		else 
			getRequest().getSession().setAttribute("htmlTreeShowVersions", this.showVersions);
		
    	if(repositoryId != null)
		{
	   		getHttpSession().setAttribute("repositoryId", repositoryId);
		}
		
    	this.repositoryId = repositoryId;
    }

    public Integer getRepositoryId()
    {
    	return this.repositoryId;
    }
    
    public String doExecute() throws Exception
    {
    	getResponse().setHeader("Cache-Control","no-cache"); 
    	getResponse().setHeader("Pragma","no-cache");
    	getResponse().setDateHeader ("Expires", 0);

    	return "success";
    }

    public String doV3() throws Exception
    {
    	getResponse().setHeader("Cache-Control","no-cache"); 
    	getResponse().setHeader("Pragma","no-cache");
    	getResponse().setDateHeader ("Expires", 0);

    	return "successV3";
    }

	/**
	 * Returns the tree.
	 * @return String
	 */
	public String getTree()
	{
		return tree;
	}

	/**
	 * Sets the tree.
	 * @param tree The tree to set
	 */
	public void setTree(String tree)
	{
		this.tree = tree;
	}

	public String getShowVersions() 
	{
		return showVersions;
	}
	
	public void setShowVersions(String showVersions) 
	{
		this.showVersions = showVersions;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}
	
}
