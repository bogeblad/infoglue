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

import java.util.List;

import javax.servlet.http.Cookie;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.util.CmsPropertyHandler;


/**
 * This class implements the action class for the header in the siteNode tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewStructureToolHeaderAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;   
     
	private List repositories;
	private String tree;
	private Integer repositoryId;
    
    private String exp=""; // for html tree support to start expanded

    public String doExecute() throws Exception
    {
    	// Get / Set tree preferance
    	if (tree != null)
    	{
    		// This action was called with parameter to set tree preferance
    		// Add a cookie for the tree setting
    		Cookie t = new Cookie("tree", tree);
    		getResponse().addCookie(t);
    	}
    	else
		{
		// First try to get user cookie for tree-mode
    		Cookie[] cookies = getRequest().getCookies();
	        if(cookies != null)
				for (int i=0; i < cookies.length; i++)
					if (cookies[i].getName().compareTo("tree") == 0)
						setTree(cookies[i].getValue());
		}

		// If that fails, try global properties for default tree
		if (tree == null)
			setTree(CmsPropertyHandler.getTree());

		// Still no tree, force applet version
		if (tree == null)
			setTree("applet");
    	
    	this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
    	
        return "success";
    }
    
    public List getRepositories()
    {
    	return this.repositories;
    }
    
    public Integer getTopRepositoryId()
    {
    	if (repositoryId != null)
    		return repositoryId;
    	
    	if(this.repositories.size() > 0)
    	{
    		return ((RepositoryVO)this.repositories.get(0)).getRepositoryId();
    	}
    	
    	return null;
    }           
	/**
	 * Returns the repositoryId.
	 * @return Integer
	 */
	public Integer getRepositoryId() 
	{
		try
    	{
	    	if(this.repositoryId == null)
	    	{	
	    		this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
	    		
	    		if(this.repositoryId == null)
	    		{
		    		String prefferedRepositoryId = CmsPropertyHandler.getPreferredRepositoryId(this.getInfoGluePrincipal().getName());
		    		if(prefferedRepositoryId != null && prefferedRepositoryId.length() > 0)
		    		{
		    			this.repositoryId = new Integer(prefferedRepositoryId);
		    			getHttpSession().setAttribute("repositoryId", this.repositoryId);		
		    		}
		    	}
	    		
	    		if(this.repositoryId == null)
	    		{
		    		this.repositoryId = getTopRepositoryId();
		    		getHttpSession().setAttribute("repositoryId", this.repositoryId);		
		    	}
	    	}
    	}
    	catch(Exception e)
    	{
    	}
   
   		return repositoryId;
	}

	/**
	 * Returns the repositoryName.
	 * @return String
	 */
	public String getRepositoryName() 
	{
	    String repositoryName = "";
		try
    	{
		    Integer repositoryId = this.getRepositoryId();
		    repositoryName = RepositoryController.getController().getRepositoryVOWithId(repositoryId).getName();
    	}
    	catch(Exception e)
    	{
    	}
    	
		return repositoryName;
	}

	/**
	 * Returns the tree.
	 * @return String
	 */
	public String getTree() {
		return tree;
	}

	/**
	 * Sets the repositoryId.
	 * @param repositoryId The repositoryId to set
	 */
	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	/**
	 * Sets the tree.
	 * @param tree The tree to set
	 */
	public void setTree(String tree) {
		this.tree = tree;
	}

	/**
	 * Returns the exp.
	 * @return String
	 */
	public String getExp()
	{
		return exp;
	}

	/**
	 * Sets the exp.
	 * @param exp The exp to set
	 */
	public void setExp(String exp)
	{
		this.exp = exp;
	}
}
