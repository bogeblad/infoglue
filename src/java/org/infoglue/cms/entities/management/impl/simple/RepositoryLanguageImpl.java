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

package org.infoglue.cms.entities.management.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.RepositoryLanguageVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class RepositoryLanguageImpl implements RepositoryLanguage
{
    private RepositoryLanguageVO valueObject = new RepositoryLanguageVO();
  
	  /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getRepositoryLanguageId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

    public RepositoryLanguageVO getValueObject()
    {
        return this.valueObject;
    }

    public void setValueObject(RepositoryLanguageVO valueObject)
    {
        this.valueObject = valueObject;
    }   
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
		setValueObject((RepositoryLanguageVO) valueObject);
	}

    private org.infoglue.cms.entities.management.Repository repository;
    private org.infoglue.cms.entities.management.Language language;
  
    
    public java.lang.Integer getRepositoryLanguageId()
    {
        return this.valueObject.getRepositoryLanguageId();
    }
            
    public void setRepositoryLanguageId(java.lang.Integer repositoryLanguageId) throws SystemException
    {
        this.valueObject.setRepositoryLanguageId(repositoryLanguageId);
    }
      
    public java.lang.Boolean getIsPublished()
    {
        return this.valueObject.getIsPublished();
    }
            
    public void setIsPublished(java.lang.Boolean isPublished) throws ConstraintException
    {
        this.valueObject.setIsPublished(isPublished);
    }
          
    public java.lang.Integer getSortOrder()
    {
        return this.valueObject.getSortOrder();
    }
    
    public void setSortOrder(java.lang.Integer sortOrder) throws ConstraintException
    {
        this.valueObject.setSortOrder(sortOrder);
    }

    public org.infoglue.cms.entities.management.Repository getRepository()
    {
        return this.repository;
    }
            
    public void setRepository (org.infoglue.cms.entities.management.Repository repository)
    {
        this.repository = repository;
        this.valueObject.setRepositoryId(repository.getId());
    }

    public org.infoglue.cms.entities.management.Language getLanguage()
    {
        return this.language;
    }
            
    public void setLanguage (org.infoglue.cms.entities.management.Language language)
    {
        this.language = language;
		this.valueObject.setLanguageId(language.getId());
    }
      
}        
