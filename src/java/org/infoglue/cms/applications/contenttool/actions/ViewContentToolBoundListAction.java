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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ViewContentToolBoundListAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewContentToolBoundListAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer repositoryId;
	private Integer siteNodeId;
	private String anchorId;
	private Map<Integer, String> contentList;
	private String[] allowedContentTypeIds = null;
	private Integer selectedLanguage = 0;
	

	@Override
	public String doExecute() throws Exception
	{
		logUserActionInfo(getClass(), "doExecute");
		
		if(this.siteNodeId != null && this.siteNodeId > 0)
		{
			try
			{
				String[] slotNames = CmsPropertyHandler.getSlotNamesForContentListing().split(","); // komponentplats 2/slot 2
				String[] contentNames = CmsPropertyHandler.getContentNamesForContentListing().split(","); // Article
				String[] attributeNames = CmsPropertyHandler.getTitleAttributesForContentListing().split(","); // Title
				logger.info("Number of slot names in application settings: " + slotNames.length);
				// If there are stored application settings for listing of contents on site nodes, try to list matching content for this site node
				// Only do this if there is at least one slot name with length > 0 
				if( (slotNames != null && slotNames.length > 0 && slotNames[0].length() > 0) && 
						(contentNames != null && contentNames.length > 0) && 
						(attributeNames != null && attributeNames.length > 0) && 
						(contentNames.length >= slotNames.length && attributeNames.length >= slotNames.length) )
				{
					contentList = new HashMap<Integer, String>();
					String contentName;
					LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(this.repositoryId);
					logger.info("Selected language for sitenode: " + this.selectedLanguage);
					if (this.selectedLanguage == 0)
					{
						List<LanguageVO> enabledLangs = SiteNodeController.getController().getEnabledLanguageVOListForSiteNode(this.siteNodeId);
						this.selectedLanguage = enabledLangs.get(0).getLanguageId();
						logger.info("Re-set selected language for sitenode (first enabled language): " + this.selectedLanguage);
					}
					SiteNodeVersionVO latestSiteNodeVersion = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getLatestActiveSiteNodeVersionVO(this.siteNodeId);
					
					if(latestSiteNodeVersion != null) {
						// Get the XML data for this site node
						String componentXML = null;
						SiteNodeVO siteNode = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId);
						ContentVersionVO metaInfo = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNode.getMetaInfoContentId(), masterLanguage.getLanguageId());
						componentXML = ContentVersionController.getContentVersionController().getAttributeValue(metaInfo.getId(), "ComponentStructure", false);
						logger.info("componentXML:" + componentXML);
						
						Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
						// Loop over the application settings and get the content we are interested in from the XML for each set of 3 settings
						for(int j = 0; j < slotNames.length; j++)
						{
							String xpath = "//component[@name='" + slotNames[j].trim() + "']/properties/property[@name='" + contentNames[j].trim() + "']/binding";
							NodeList components = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), xpath);
							int NrOfComponents = components.getLength();
							logger.info("Number of sitenode contents: " + NrOfComponents + " (slot name: " + slotNames[j] + " - content name: " + contentNames[j] +")");
							
							if (components.getLength() > 0)
							{
								for (int i=0; i<NrOfComponents; i++)
								{
									contentName = "";
									Element elem = (Element) components.item(i);
									int contentId = Integer.parseInt(elem.getAttribute("entityId"));
									contentName = ContentController.getContentController().getContentAttribute(contentId, selectedLanguage, attributeNames[j].trim());
									ContentVO contentObj = ContentController.getContentController().getContentVOWithId(contentId);
									if(contentObj != null)
									{
										contentList.put(contentId, contentObj.getName() + (contentName.length() > 0 ? " (" + contentName + ")" : ""));
									}
								}
							}
						}
							
					}
				}
			}
			catch(Exception e)
			{
				logger.info("Error when listing sitenode content:" + e.getMessage(), e);
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
	
    public String[] getAllowedContentTypeIds()
    {
        return allowedContentTypeIds;
    }
    
    public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
    {
        this.allowedContentTypeIds = allowedContentTypeIds;
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

	public Integer getSelectedLanguage() {
		return selectedLanguage;
	}

	public void setSelectedLanguage(Integer selectedLanguage) {
		this.selectedLanguage = selectedLanguage;
	}
}
