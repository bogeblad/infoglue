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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;

//import com.frovi.ss.Tree.BaseNode;
//import com.frovi.ss.Tree.INodeSupplier;

public class ViewContentToolBoundListAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewContentToolBoundListAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer repositoryId;
	private Integer siteNodeId;
	private String anchorId;
	private String articleTitle;
	private Map<Integer, String> contentList;
	private Integer select = -1;
	private String[] allowedContentTypeIds = null;
	private String bodyClass;
	private boolean binding = false;
	
	public String doBindingView() throws Exception
	{
		setBinding(true);
		
		//super.doExecute();
        
		return "bindingView";
	}

	@Override
	public String doExecute() throws Exception
	{
		SiteNodeVersionVO latestSiteNodeVersion = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getLatestActiveSiteNodeVersionVO(siteNodeId);
		if(latestSiteNodeVersion != null) {
			List<Object> referencedObjects = RegistryController.getController().getReferencedObjects(SiteNodeVersion.class.getName(), latestSiteNodeVersion.getSiteNodeVersionId().toString(), true);
			
			if(!referencedObjects.isEmpty()) {
				// List of CTD-ID for the content types that you can have anchor links to.
				// To add more content types, just add the CTD-ID for them to the array list
				List<Integer> ctdIdList = new ArrayList<Integer>();
				ContentTypeDefinitionVO ctd = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Article");
				ctdIdList.add(ctd.getContentTypeDefinitionId());
				contentList = new HashMap<Integer, String>();
				
				// Build list of contents that can be linked to
				for (Object obj : referencedObjects) {
					setArticleTitle("");
					if(obj instanceof ContentVO) {
						ContentVO contentObj = (ContentVO) obj;
						if(ctdIdList.contains(contentObj.getContentTypeDefinitionId())) {
							ContentVersionVO cvObj = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentObj.getId());
							setArticleTitle(ContentVersionController.getContentVersionController().getAttributeValue(cvObj.getContentVersionId(), "Title", true));
							contentList.put(contentObj.getContentId(), contentObj.getName() + (getArticleTitle().length() > 0 ? " (" + getArticleTitle() + ")" : ""));
						}
					}
				}
			}
		}
		return "success";
	}

	public Map<Integer, String> getContentList() {
		return contentList;
	}

	public void setContentList(Map<Integer, String> contentList) {
		this.contentList = contentList;
	}

	public List getAvailableLanguages() throws Exception
	{
		return LanguageController.getController().getLanguageVOList(this.repositoryId);
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

    /** 
				Returns true if this is a binding action.
    */
    public boolean isBinding()
    {
    	return binding;
    }
    
    /** 
				Set if this is a binding action.
     */
    public void setBinding(boolean binding)
    {
    	this.binding = binding;
    }

	public Integer getSiteNodeId() {
		return siteNodeId;
	}

	public void setSiteNodeId(Integer siteNodeId) {
		this.siteNodeId = siteNodeId;
	}

	public String getAnchorId() {
		return anchorId;
	}

	public void setAnchorId(String anchorId) {
		this.anchorId = anchorId;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
}
