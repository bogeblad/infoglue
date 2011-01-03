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

import java.net.URLEncoder;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.TreeViewAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.treeservice.ss.ContentNodeSupplier;
import org.infoglue.cms.treeservice.ss.ContentNodeVersionSupplier;
import org.infoglue.cms.util.CmsPropertyHandler;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.INodeSupplier;

public class ViewContentToolMenuHtmlAction extends TreeViewAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewContentToolMenuHtmlAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer repositoryId;
	private String showVersions;
	private String showLeafs = "yes";
	private String treeMode = "classic";
	private Integer select = -1;
	private BaseNode rootNode = null;
	private String[] allowedContentTypeIds = null;
	private String bodyClass;
		
	/* Experiment 2003-09-11 TODO:
	 * Provide a list of content-type definition, so that
	 * we can populate a contenxt menu, to support the
	 * creation of new contents.
	 * 
	 */
	
	
	public String doBindingView() throws Exception
	{
		super.doExecute();
        
		return "bindingView";
	}

	public String doBindingViewV3() throws Exception
	{
		super.doExecute();
        
		return "bindingViewV3";
	}

	/**
	 * @see org.infoglue.cms.applications.common.actions.TreeViewAbstractAction#getNodeSupplier()
	 */
	protected INodeSupplier getNodeSupplier() throws Exception, org.infoglue.cms.exception.SystemException
	{
		if(getRepositoryId() == null  || getRepositoryId().intValue() < 1)
			return null;
				
		if (this.showVersions == null || this.showVersions.equals("")) 
		{
			this.showVersions = (String)getRequest().getSession().getAttribute("htmlTreeShowVersions");
		} 
		else 
		{
			getRequest().getSession().setAttribute("htmlTreeShowVersions", this.showVersions);
		}
		
		/*		
		Cookie[] cookies = getRequest().getCookies();
	    if(cookies != null)
			for (int i=0; i < cookies.length; i++)
				if (cookies[i].getName().compareTo("showversions") == 0)
					if (cookies[i].getValue().compareTo("yes") == 0)
					{
						setShowVersions("yes");
						return new ContentNodeVersionSupplier(getRepositoryId(), this.getInfoGluePrincipal().getName());
					}
					*/		
		
		INodeSupplier sup = null;
		if (this.showVersions != null && this.showVersions.equalsIgnoreCase("yes")) 
		{
			sup = new ContentNodeVersionSupplier(getRepositoryId(), this.getInfoGluePrincipal().getName());
        } 
		else 
		{
			ContentNodeSupplier contentNodeSupplier = new ContentNodeSupplier(getRepositoryId(), this.getInfoGluePrincipal());
			contentNodeSupplier.setShowLeafs(showLeafs.compareTo("yes")==0);
			contentNodeSupplier.setAllowedContentTypeIds(allowedContentTypeIds);
			sup = contentNodeSupplier;
        }
		
		String treeMode = CmsPropertyHandler.getTreeMode(); 
		if(treeMode != null) setTreeMode(treeMode);
		

		rootNode = sup.getRootNode();
		return sup;
	}

	public List getAvailableLanguages() throws Exception
	{
		return RepositoryLanguageController.getController().getRepositoryLanguageVOListWithRepositoryId(this.repositoryId);
	}

	public List getContentTypeDefinitions() throws Exception
	{
		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
	}      

	/**
	 * Returns the repositoryId.
	 * @return Integer
	 */
	public Integer getRepositoryId()
	{
	    if(this.repositoryId == null)
	    {
	        try
	        {
		        List repositoryVOList = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
		        if(repositoryVOList != null && repositoryVOList.size() > 0)
		        {
		            this.repositoryId = ((RepositoryVO)repositoryVOList.get(0)).getId();
		        }
	        }
	        catch(Exception e)
	        {
	            logger.error("Could not fetch the master repository for the principal:" + e.getMessage(), e);
	        }
	    }
	        
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
	 * Returns the showVersions.
	 * @return String
	 */
	public String getShowVersions()
	{
		return showVersions;
	}

	/**
	 * Sets the showVersions.
	 * @param showVersions The showVersions to set
	 */
	public void setShowVersions(String showVersions)
	{
		this.showVersions = showVersions;
	}

	/**
	 * Returns the showLeafs.
	 * @return String
	 */
	public String getShowLeafs()
	{
		return showLeafs;
	}

	/**
	 * Sets the showLeafs.
	 * @param showLeafs The showLeafs to set
	 */
	public void setShowLeafs(String showLeafs)
	{
		this.showLeafs = showLeafs;
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

    public BaseNode getRootNode()
    {
        return rootNode;
    }
    
    public void setRootNode(BaseNode rootNode)
    {
        this.rootNode = rootNode;
    }
	
    public String getTreeMode() 
	{
		return treeMode;
	}
	
	public void setTreeMode(String treeMode) 
	{
		this.treeMode = treeMode;
	}
	
    public String[] getAllowedContentTypeIds()
    {
        return allowedContentTypeIds;
    }
    
    public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
    {
        this.allowedContentTypeIds = allowedContentTypeIds;
    }
    
    public String getAllowedContentTypeIdsAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i<allowedContentTypeIds.length; i++)
        {
            if(i > 0)
                sb.append("&");
            
            sb.append("allowedContentTypeIds=" + URLEncoder.encode(allowedContentTypeIds[i], "UTF-8"));
        }

        return sb.toString();
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
