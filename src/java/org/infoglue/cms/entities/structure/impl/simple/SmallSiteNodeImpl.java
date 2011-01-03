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

package org.infoglue.cms.entities.structure.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;

public class SmallSiteNodeImpl extends SiteNodeImpl
{
    private SiteNodeVO valueObject = new SiteNodeVO();
    //private SmallSiteNodeImpl parentSiteNode;
    //private Collection childSiteNodes = new ArrayList();
    //private Collection siteNodeVersions = new ArrayList();
    //private RepositoryImpl repository;
    //private SiteNodeTypeDefinitionImpl siteNodeTypeDefinition;

    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getVO()
	 */
	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#setVO(BaseEntityVO)
	 */
	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((SiteNodeVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getSiteNodeId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

    public SiteNodeVO getValueObject()
    {
        return this.valueObject;
    }

    public void setValueObject(SiteNodeVO valueObject)
    {
        this.valueObject = valueObject;
    }     
    
    public java.lang.Integer getSiteNodeId()
    {
        return this.valueObject.getSiteNodeId();
    }
            
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
        this.valueObject.setSiteNodeId(siteNodeId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }
    
    public java.lang.Boolean getIsBranch()
    {
    	return this.valueObject.getIsBranch();
	}
    
    public void setIsBranch(java.lang.Boolean isBranch)
	{
		this.valueObject.setIsBranch(isBranch);
	}
	  
    public java.util.Date getPublishDateTime()
    {
        return this.valueObject.getPublishDateTime();
    }
            
    public void setPublishDateTime(java.util.Date publishDateTime)
    {
        this.valueObject.setPublishDateTime(publishDateTime);
    }
      
    public java.util.Date getExpireDateTime()
    {
        return this.valueObject.getExpireDateTime();
    }
            
    public void setExpireDateTime(java.util.Date expireDateTime)
    {
        this.valueObject.setExpireDateTime(expireDateTime);
    }
     
    public Integer getMetaInfoContentId()
    {
        return this.valueObject.getMetaInfoContentId();
    }
    
    public void setMetaInfoContentId(Integer metaInfoContentId)
    {
        this.valueObject.setMetaInfoContentId(metaInfoContentId);
    }

    public Integer getRepositoryId()
    {
        return this.valueObject.getRepositoryId();
    }
    
    public void setRepositoryId(Integer repositoryId)
    {
        this.valueObject.setRepositoryId(repositoryId);
    }

    public Integer getSiteNodeTypeDefinitionId()
    {
        return this.valueObject.getSiteNodeTypeDefinitionId();
    }
    
    public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
    {
        this.valueObject.setSiteNodeTypeDefinitionId(siteNodeTypeDefinitionId);
    }

    /*
    public org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl getParentSiteNode()
    {
        return this.parentSiteNode;
    }
            
    public void setParentSiteNode (org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl parentSiteNode)
    {
        this.parentSiteNode = parentSiteNode;
    }
      
    public java.util.Collection getChildSiteNodes()
    {
        return this.childSiteNodes;
    }
            
    public void setChildSiteNodes (java.util.Collection childSiteNodes)
    {
        this.childSiteNodes = childSiteNodes;
        this.setChildCount(new Integer(childSiteNodes.size()) );        
    }
      
    public java.util.Collection getSiteNodeVersions()
    {
        return this.siteNodeVersions;
    }
            
    public void setSiteNodeVersions (java.util.Collection siteNodeVersions)
    {
        this.siteNodeVersions = siteNodeVersions;
    }
    */
      
    public String getCreator()
    {
        return this.valueObject.getCreatorName();
    }
            
    public void setCreator(String creator)
    {
        this.valueObject.setCreatorName(creator);
    }
    /*  
    public org.infoglue.cms.entities.management.impl.simple.RepositoryImpl getRepository()
    {
        return this.repository;
    }
            
    public void setRepository (org.infoglue.cms.entities.management.impl.simple.RepositoryImpl repository)
    {
        this.repository = repository;
		this.valueObject.setRepositoryId(repository.getRepositoryId());
    }
      
    public org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl getSiteNodeTypeDefinition()
    {
        return this.siteNodeTypeDefinition;
    }
            
    public void setSiteNodeTypeDefinition (org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl siteNodeTypeDefinition)
    {
    	this.siteNodeTypeDefinition = siteNodeTypeDefinition;
        if(siteNodeTypeDefinition != null)
			this.valueObject.setSiteNodeTypeDefinitionId(siteNodeTypeDefinition.getSiteNodeTypeDefinitionId());
    }
    */
    
	/**
	 * Returns the childCount.
	 * @return Integer
	 */
	public Integer getChildCount()
	{
		return this.valueObject.getChildCount();
	}

	/**
	 * Sets the childCount.
	 * @param childCount The childCount to set
	 */
	public void setChildCount(Integer childCount)
	{
		this.valueObject.setChildCount(childCount);
	}

	public void setParentSiteNodeId(Integer parentSiteNodeId)
	{
		this.valueObject.setParentSiteNodeId(parentSiteNodeId);
	}

	public Integer getParentSiteNodeId()
	{
		return this.valueObject.getParentSiteNodeId();
	}

}        
