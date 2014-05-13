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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.contenttool.actions.ViewContentVersionAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the action class for the menu in the siteNode tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewStructureToolMenuAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private final static Logger logger = Logger.getLogger(ViewStructureToolMenuAction.class.getName());

    private Integer repositoryId;
    private String tree;
    private String path;

	public void setRepositoryId(Integer repositoryId)
    {
    	if(repositoryId != null)
		{
	   		getHttpSession().setAttribute("structureRepositoryId", repositoryId);
	   	}
		
    	this.repositoryId = repositoryId;
    }

    public Integer getRepositoryId()
    {
    	return this.repositoryId;
    }
    
    public String doExecute() throws Exception
    {
        return "success";
    }

    public String doV3() throws Exception
    {
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

	public String getShowComponentsFirst()
	{
	    return CmsPropertyHandler.getShowComponentsFirst();
	}
	
	public LanguageVO getMasterLanguageVO() throws Exception
	{
	    return LanguageController.getController().getMasterLanguage(repositoryId);
	}

	public String getFullPath()
	{
		String fullPath = path;
		try
		{
			if(path != null && !path.equals(""))
				fullPath = SiteNodeController.getController().getSiteNodeIdPath(new Integer(path));
		}
		catch (Exception e) 
		{
			logger.info("Error:" + e.getMessage(), e);
		}
		return fullPath;
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
