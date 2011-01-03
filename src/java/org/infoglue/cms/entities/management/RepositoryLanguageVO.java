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

package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class RepositoryLanguageVO  implements BaseEntityVO
{

    private java.lang.Integer repositoryLanguageId;
    private java.lang.Boolean isPublished;
    private java.lang.Integer sortOrder = new Integer(0);
    private Integer repositoryId;
    private Integer languageId;
     
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return getRepositoryLanguageId();
	}
  
  
    public java.lang.Integer getRepositoryLanguageId()
    {
        return this.repositoryLanguageId;
    }
                
    public void setRepositoryLanguageId(java.lang.Integer repositoryLanguageId) throws SystemException
    {
        this.repositoryLanguageId = repositoryLanguageId;
    }
    
    public java.lang.Boolean getIsPublished()
    {
        return this.isPublished;
    }
                
    public void setIsPublished(java.lang.Boolean isPublished) throws ConstraintException
    {
        this.isPublished = isPublished;
    }
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
		return new ConstraintExceptionBuffer();
	}
  
	public Integer getLanguageId()
	{
		return languageId;
	}

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public void setLanguageId(Integer integer)
	{
		languageId = integer;
	}

	public void setRepositoryId(Integer integer)
	{
		repositoryId = integer;
	}

    public java.lang.Integer getSortOrder()
    {
        return sortOrder;
    }
    
    public void setSortOrder(java.lang.Integer order)
    {
        this.sortOrder = order;
    }
}
        
