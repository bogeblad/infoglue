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

package org.infoglue.deliver.taglib.content;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.providers.ContentDetailPageResolver;
import org.infoglue.deliver.applications.databeans.WebPage;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This class returns a WebPage containing info of which detail page this content is bound if any.
 * 
 * @author Mattias Bogeblad
 */

public class ContentDetailPageTag extends ComponentLogicTag 
{
    private final static Logger logger = Logger.getLogger(ContentDetailPageTag.class.getName());

	private static final long serialVersionUID = 4050206323348354355L;

	private Integer siteNodeId;
	private Integer contentId;
	private String propertyName;
    private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;
    private boolean escapeHTML = false;
    private boolean hideUnauthorizedPages = false;
    private boolean disableValidateBindingOnPage = false;
    private boolean disableFallBack = false;
    
	public int doEndTag() throws JspException
    {
        ContentVO contentVO = getContent();
        	
        if(contentVO != null)
        {
        	if(contentVO.getExtraProperties().get("detailSiteNodeId") != null)
			{
				Integer detailSiteNodeId = (Integer)contentVO.getExtraProperties().get("detailSiteNodeId");
				
				boolean isValid = false;
				if(!disableValidateBindingOnPage)
            	{
					List referencingSiteNodeVOList = getController().getReferencingPages(contentVO.getId(), 50, true);
            		Iterator referencingSiteNodeVOListIterator = referencingSiteNodeVOList.iterator();
            		while(referencingSiteNodeVOListIterator.hasNext())
            		{
            			SiteNodeVO detailSiteNodeVO = (SiteNodeVO)referencingSiteNodeVOListIterator.next();
            			if(detailSiteNodeVO.getId().equals(detailSiteNodeId))
            			{
            				isValid = true;
            				break;
            			}
            		}
            	}
				else
					isValid = true; 
				
				if(isValid)
				{
	            	WebPage webPage = getController().getPage(detailSiteNodeId, getController().getLanguageId(), new Integer(-1), escapeHTML, hideUnauthorizedPages);
	            	setResultAttribute(webPage);
				}
				else
				{
					List referencingSiteNodeVOList = getController().getReferencingPages(contentVO.getId(), 50, true);
					if(referencingSiteNodeVOList.size() == 1)
					{
						SiteNodeVO detailSiteNodeVO = (SiteNodeVO)referencingSiteNodeVOList.get(0);
						WebPage webPage = getController().getPage(detailSiteNodeVO.getId(), getController().getLanguageId(), new Integer(-1), escapeHTML, hideUnauthorizedPages);
						setResultAttribute(webPage);				
					}
					else if(!disableFallBack && referencingSiteNodeVOList.size() > 1)
					{
						SiteNodeVO detailSiteNodeVO = (SiteNodeVO)referencingSiteNodeVOList.get(0);
						WebPage webPage = getController().getPage(detailSiteNodeVO.getId(), getController().getLanguageId(), new Integer(-1), escapeHTML, hideUnauthorizedPages);
						setResultAttribute(webPage);				
					}
					else
					{
						setResultAttribute(null);
					}
				}
			}
			else
			{
				WebPage webPage = null;
				if(contentVO.getContentTypeDefinitionId() != null)
				{
					try
					{
						ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId(), getController().getDatabase());
						logger.info("contentTypeDefinitionVO:" + contentTypeDefinitionVO.getName());
						if(contentTypeDefinitionVO.getDetailPageResolverClass() != null && !contentTypeDefinitionVO.getDetailPageResolverClass().equals(""))
						{
							ContentDetailPageResolver cdpr = (ContentDetailPageResolver)loadExtensionClass(contentTypeDefinitionVO.getDetailPageResolverClass()).newInstance();;
							logger.info("cdpr:" + cdpr.getName());
							SiteNodeVO detailSiteNodeVO = cdpr.getDetailSiteNodeVO(getController().getPrincipal(), contentVO.getId(), contentTypeDefinitionVO.getDetailPageResolverData(), getController().getDatabase());
							logger.info("detailSiteNodeVO:" + detailSiteNodeVO.getId());
							webPage = getController().getPage(detailSiteNodeVO.getId(), getController().getLanguageId(), new Integer(-1), escapeHTML, hideUnauthorizedPages);
							logger.info("webPage:" + webPage.getSiteNodeId());
						}
					}
					catch (Exception e) 
					{
						logger.warn("Content with id:" + contentVO.getId() + " had a faulty content type:" + e.getMessage());
						e.printStackTrace();
					}
				}
				
				if(webPage == null)
				{
					List referencingSiteNodeVOList = getController().getReferencingPages(contentVO.getId(), 50, true);
					if(referencingSiteNodeVOList.size() == 1)
					{
						SiteNodeVO detailSiteNodeVO = (SiteNodeVO)referencingSiteNodeVOList.get(0);
						webPage = getController().getPage(detailSiteNodeVO.getId(), getController().getLanguageId(), new Integer(-1), escapeHTML, hideUnauthorizedPages);
					}
					else if(!disableFallBack && referencingSiteNodeVOList.size() > 1)
					{
						SiteNodeVO detailSiteNodeVO = (SiteNodeVO)referencingSiteNodeVOList.get(0);
						webPage = getController().getPage(detailSiteNodeVO.getId(), getController().getLanguageId(), new Integer(-1), escapeHTML, hideUnauthorizedPages);
					}
				}
	
				setResultAttribute(webPage);
			}
        }
        else
        {
        	setResultAttribute(null);
        }
        
	    return EVAL_PAGE;
    }
    
	private ContentVO getContent() throws JspException
	{	
	    if(this.contentId != null)
	    {
			if(this.contentId.intValue() < 1)
				return null;

	    	return this.getController().getContent(this.contentId);
	    }
	    else if(this.propertyName != null)
	    {
	        if(this.siteNodeId != null)
	            return this.getComponentLogic().getBoundContentWithDetailSiteNodeId(siteNodeId, propertyName, useInheritance);
	        else
	            return this.getComponentLogic().getBoundContentWithDetailSiteNodeId(propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
	    }
	    else if(this.getController().getContentId() != null && this.getController().getContentId().intValue() > -1)
	    {
	    	return this.getController().getContent();
	    }
	    else
	    {
	    	return null;
	    }
	}

    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("content", "contentId", contentId);
    }

    public void setSiteNodeId(String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("content", "siteNodeId", siteNodeId);
    }

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("ContentDetailPageUrlTag", "propertyName", propertyName);
    }

    public void setUseInheritance(boolean useInheritance)
    {
        this.useInheritance = useInheritance;
    }
    
    public void setUseRepositoryInheritance(boolean useRepositoryInheritance)
    {
        this.useRepositoryInheritance = useRepositoryInheritance;
    }

    public void setUseStructureInheritance(boolean useStructureInheritance)
    {
        this.useStructureInheritance = useStructureInheritance;
    }

	public void setEscapeHTML(boolean escapeHTML) 
	{
		this.escapeHTML = escapeHTML;
	}

	public void setHideUnauthorizedPages(boolean hideUnauthorizedPages) 
	{
		this.hideUnauthorizedPages = hideUnauthorizedPages;
	}

	public void setDisableValidateBindingOnPage(boolean disableValidateBindingOnPage) 
	{
		this.disableValidateBindingOnPage = disableValidateBindingOnPage;
	}

	public void setDisableFallBack(boolean disableFallBack) 
	{
		this.disableFallBack = disableFallBack;
	}

}
