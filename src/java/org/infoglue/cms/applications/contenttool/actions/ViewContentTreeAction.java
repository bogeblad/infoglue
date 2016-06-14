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

import javax.servlet.http.Cookie;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the action class for the menu in the content tool.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewContentTreeAction extends InfoGlueAbstractAction implements ViewContentTreeActionInterface
{
	private static final long serialVersionUID = 1L;
	
    private Integer repositoryId;
    private Integer contentId;
    private String tree;
    private String hideLeafs;
    private String bodyClass;
    
    public void setRepositoryId(Integer repositoryId)
    {
    	this.repositoryId = repositoryId;
    }

    public Integer getRepositoryId()
    {
    	return this.repositoryId;
    }
    
    public void setContentId(Integer contentId)
    {
    	this.contentId = contentId;
    }

	public Integer getContentId()
	{
		return this.contentId;
	}    

    public void setHideLeafs(String hideLeafs)
    {
    	this.hideLeafs = hideLeafs;
    }

	public String getHideLeafs()
	{
		return this.hideLeafs;
	}    
    
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
				{
					if (cookies[i].getName().compareTo("tree") == 0)
						setTree(cookies[i].getValue());
				}
		}

		// If that fails, try global properties for default tree
		if (tree == null)
			setTree(CmsPropertyHandler.getTree());

		// Still no tree, force applet version
		if (tree == null)
			setTree("applet");
    	
        return "success";
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

    public String getBodyClass()
    {
        return bodyClass;
    }
    
    public void setBodyClass(String bodyClass)
    {
        this.bodyClass = bodyClass;
    }
}
