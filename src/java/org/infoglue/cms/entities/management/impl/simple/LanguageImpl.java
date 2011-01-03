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
		 
import java.util.ArrayList;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;

public class LanguageImpl implements Language
{
    private LanguageVO valueObject = new LanguageVO();
    private java.util.Collection repositoryLanguages = new ArrayList();
        
	public Integer getId()
	{
		return this.getLanguageId();
	}

	public Object getIdAsObject()
	{
		return getId();
	}

	public String toString()
	{
		return this.valueObject.toString();
	}

    public LanguageVO getValueObject()
    {
        return this.valueObject;
    }

        
    public void setValueObject(LanguageVO valueObject)
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
		setValueObject((LanguageVO) valueObject);
	}
    
    public java.lang.Integer getLanguageId()
    {
        return this.valueObject.getLanguageId();
    }
            
    public void setLanguageId(java.lang.Integer languageId)
    {
        this.valueObject.setLanguageId(languageId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name) throws ConstraintException
    {
        this.valueObject.setName(name);
    }
      
    public java.lang.String getLanguageCode()
    {
        return this.valueObject.getLanguageCode();
    }
            
	public void setLanguageCode(java.lang.String languageCode) throws ConstraintException
	{
		this.valueObject.setLanguageCode(languageCode);
	}

    public void setCharset(java.lang.String charset) throws ConstraintException
    {
        this.valueObject.setCharset(charset);
    }

	public java.lang.String getCharset()
	{
		return this.valueObject.getCharset();
	}
                  
    public java.util.Collection getRepositoryLanguages()
    {
        return this.repositoryLanguages;
    }
            
    public void setRepositoryLanguages (java.util.Collection repositoryLanguages)
    {
        this.repositoryLanguages = repositoryLanguages;
    }
  }        
