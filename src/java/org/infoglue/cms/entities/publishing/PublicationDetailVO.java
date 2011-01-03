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

package org.infoglue.cms.entities.publishing;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;

public class PublicationDetailVO implements BaseEntityVO
{ 
	public static final Integer UNDEFINED        = new Integer(0);
	public static final Integer PUBLISH          = new Integer(1);
	public static final Integer PUBLISH_DENIED   = new Integer(2);
	public static final Integer UNPUBLISH_LATEST = new Integer(3);
	public static final Integer UNPUBLISH_DENIED = new Integer(4);
	
	
    private java.lang.Integer publicationDetailId;
    private java.lang.String name			 = "";
    private java.lang.String description	 = "";
    private java.lang.String entityClass	 = "";
    private java.lang.Integer entityId		 = null;
    private java.util.Date creationDateTime  = DateHelper.getSecondPreciseDate();
  	private java.lang.Integer typeId         = UNDEFINED;
  	private String creator 					 = null; 
  	  
	/**
	 * The constructor for the object. Empty now.
	 */
	  	
  	public PublicationDetailVO()
  	{
  	}
  	  	
    public java.lang.Integer getPublicationDetailId()
    {
        return this.publicationDetailId;
    }
    
    public void setPublicationDetailId(java.lang.Integer publicationDetailId)
    {
        this.publicationDetailId = publicationDetailId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;
    }

    public java.lang.String getDescription()
    {
        return this.description;
    }
                
    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }

    public java.lang.String getEntityClass()
    {
        return this.entityClass;
    }
            
    public void setEntityClass(java.lang.String entityClass)
    {
        this.entityClass = entityClass;
    }

    public java.lang.Integer getEntityId()
    {
        return entityId;
    }
            
    public void setEntityId(java.lang.Integer entityId)
    {
        this.entityId = entityId;
    }
    
    public java.lang.Integer getTypeId()
    {
        return this.typeId;
    }
    
    public void setTypeId(java.lang.Integer typeId)
    {
        this.typeId = typeId;
    }
    
    public java.util.Date getCreationDateTime()
    {
        return this.creationDateTime;
    }
                
    public void setCreationDateTime(java.util.Date creationDateTime)
    {
        this.creationDateTime = creationDateTime;
    }
    
	public String getCreator()
	{
		return this.creator;
	}

	public void setCreator(String creator)
	{
		this.creator = creator;
	}
                	

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return getPublicationDetailId();
	}	

	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{ 
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		return ceb;
	}	


}
        
