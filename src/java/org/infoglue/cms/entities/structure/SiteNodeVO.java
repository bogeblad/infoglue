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

package org.infoglue.cms.entities.structure;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class SiteNodeVO implements BaseEntityVO
{

    private Integer siteNodeId 	= null;
    private String name			= "";
    private Date publishDateTime 	= new Date();
    private Date expireDateTime  	= new Date();
    private Boolean isBranch		= new Boolean(false);             
    private Boolean isDeleted 		= new Boolean(false);
    private Integer repositoryId 	= null;    
  	private Integer siteNodeTypeDefinitionId = null;  
  	private Integer childCount;
  	private Integer sortOrder;
  	private Boolean isHidden 		= new Boolean(false);
  	
  	private String creatorName;
	private Integer metaInfoContentId 	= new Integer(-1);
	
	private Integer parentSiteNodeId 	= null;

  	//Used if an application wants to add more properties to this item... used for performance reasons.
  	private Map extraProperties = new Hashtable();
  	
  	private static SimpleTimeZone stmz = new SimpleTimeZone(-8 * 60 * 60 * 1000, "GMT");


	public SiteNodeVO()
  	{
  		//Initilizing the expireDateTime... 
  		Calendar calendar = Calendar.getInstance(stmz);
  		
  		int years = 50;
  		try
  		{
	  		String numberOfYears = CmsPropertyHandler.getDefaultNumberOfYearsBeforeExpire();
	  		if(numberOfYears != null && !numberOfYears.equals(""))
	  			years = new Integer(numberOfYears).intValue();
  		}
  		catch (Throwable t) 
  		{}
  		
  		calendar.add(Calendar.YEAR, years);
  		expireDateTime = calendar.getTime();
  	}
  	
	/**
	 * Returns the childCount.
	 * @return Integer
	 */
	public Integer getChildCount()
	{
		return childCount;
	}

	/**
	 * Sets the childCount.
	 * @param childCount The childCount to set
	 */
	public void setChildCount(Integer childCount)
	{
		this.childCount = childCount;
	}
  
    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeId;
    }
                
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
        this.siteNodeId = siteNodeId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setRepositoryId(java.lang.Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryId;
    }

    public void setSiteNodeTypeDefinitionId(java.lang.Integer siteNodeTypeDefinitionId)
    {
        this.siteNodeTypeDefinitionId = siteNodeTypeDefinitionId;
    }
    
    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.siteNodeTypeDefinitionId;
    }
    
    public void setName(java.lang.String name)
    {
        this.name = name;
    }
    
    public java.util.Date getPublishDateTime()
    {
        return this.publishDateTime;
    }
                
    public void setPublishDateTime(java.util.Date publishDateTime)
    {
        this.publishDateTime = publishDateTime;
    }
    
    public java.util.Date getExpireDateTime()
    {
        return this.expireDateTime;
    }
                
    public void setExpireDateTime(java.util.Date expireDateTime)
    {
        this.expireDateTime = expireDateTime;
    }
    
    public java.lang.Boolean getIsBranch()
    {
    	return this.isBranch;
	}
    
    public void setIsBranch(java.lang.Boolean isBranch)
	{
		this.isBranch = isBranch;
	}
	
    public Integer getMetaInfoContentId()
    {
        return metaInfoContentId;
    }
    
    public void setMetaInfoContentId(Integer metaInfoContentId)
    {
        this.metaInfoContentId = metaInfoContentId;
    }
    
    public Boolean getIsDeleted()
    {
    	return this.isDeleted;
	}
    
    public void setIsDeleted(Boolean isDeleted)
	{
		this.isDeleted = isDeleted;
	}

    public Boolean getIsHidden()
    {
    	return this.isHidden;
	}
    
    public void setIsHidden(Boolean isHidden)
	{
		this.isHidden = isHidden;
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return getSiteNodeId();
	}
	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{ 
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 		ValidatorFactory.createStringValidator("SiteNode.name", true, 2, 100).validate(name, ceb);
 		
 		if(this.publishDateTime.after(this.expireDateTime))
			ceb.add(new ConstraintException("SiteNode.publishDateTime", "308"));
		
		return ceb;
	}	
	          
	/**
	 * Returns the creatorName.
	 * @return String
	 */
	public String getCreatorName()
	{
		return creatorName;
	}

	/**
	 * Sets the creatorName.
	 * @param creatorName The creatorName to set
	 */
	public void setCreatorName(String creatorName)
	{
		this.creatorName = creatorName;
	}

    public Map getExtraProperties()
    {
        return extraProperties;
    }

	public Integer getParentSiteNodeId()
	{
		return parentSiteNodeId;
	}

	public void setParentSiteNodeId(Integer parentSiteNodeId)
	{
		this.parentSiteNodeId = parentSiteNodeId;
	}

	public Integer getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder)
	{
		this.sortOrder = sortOrder;
	}

}
        
