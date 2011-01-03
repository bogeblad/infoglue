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

import javax.servlet.jsp.JspException;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * Tag for org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController.getContentAttribute(<String>, <Sring>, <boolean>);
 */
public class ContentTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3258135773294113587L;

	private Integer siteNodeId;
	private Integer contentId;
	private String propertyName;
    private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;

    //Section is for if you search for content by path
    private String path = null;
    private Integer repositoryId = null;
    private String repositoryName = null;
    
    public ContentTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
		produceResult(getContent());
    
		this.siteNodeId = null;
		this.contentId = null;
		this.propertyName = null;
		this.useInheritance = true;
		this.useRepositoryInheritance = true;
		this.useStructureInheritance = true;
		this.path = null;
		this.repositoryId = null;
		this.repositoryName = null;
	    
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
	            return this.getComponentLogic().getBoundContent(siteNodeId, propertyName, useInheritance);
	        else
	            return this.getComponentLogic().getBoundContent(propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
	    }
	    else if(path != null)
	    {
	    	if(this.repositoryName != null && repositoryId == null)
	    	{
	    		RepositoryVO repositoryVO = getController().getRepositoryWithName(repositoryName);
	    		if(repositoryVO != null)
	    			this.repositoryId = repositoryVO.getId();
	    	}
	    	
	    	if(repositoryId == null)
	    		repositoryId = this.getController().getRepositoryId();
	    	
	    	return getController().getContentWithPath(repositoryId, path);
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

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("content", "propertyName", propertyName);
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

    public void setSiteNodeId(String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("content", "siteNodeId", siteNodeId);
    }

    public void setRepositoryId(String repositoryId) throws JspException
    {
        this.repositoryId = evaluateInteger("content", "repositoryId", repositoryId);
    }

    public void setRepositoryName(String repositoryName) throws JspException
    {
        this.repositoryName = evaluateString("content", "repositoryName", repositoryName);
    }

    public void setPath(String path) throws JspException
    {
        this.path = evaluateString("content", "path", path);
    }

}