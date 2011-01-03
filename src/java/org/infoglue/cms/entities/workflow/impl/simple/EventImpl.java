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

package org.infoglue.cms.entities.workflow.impl.simple;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.workflow.Event;
import org.infoglue.cms.entities.workflow.EventVO;

public class EventImpl implements Event
{
    private EventVO valueObject = new EventVO();
    private org.infoglue.cms.entities.management.impl.simple.RepositoryImpl repository;
     
    public EventVO getValueObject()
    {
        return this.valueObject;
    }
        
	public Object getIdAsObject()
	{
		return getId();
	}

    public void setValueObject(EventVO valueObject)
    {
        this.valueObject = valueObject;
    }     
    
    public java.lang.Integer getEventId()
    {
        return this.valueObject.getEventId();
    }
            
    public void setEventId(java.lang.Integer eventId)
    {
        this.valueObject.setEventId(eventId);
    }
      
    public java.lang.String getName()
    {
        return this.valueObject.getName();
    }
            
    public void setName(java.lang.String name)
    {
        this.valueObject.setName(name);
    }

    public java.lang.String getDescription()
    {
        return this.valueObject.getDescription();
    }
            
    public void setDescription(java.lang.String description)
    {
        this.valueObject.setDescription(description);
    }

    public java.lang.String getEntityClass()
    {
        return this.valueObject.getEntityClass();
    }
            
    public void setEntityClass(java.lang.String entityClass)
    {
        this.valueObject.setEntityClass(entityClass);
    }

    public java.lang.Integer getEntityId()
    {
        return this.valueObject.getEntityId();
    }
            
    public void setEntityId(java.lang.Integer entityId)
    {
        this.valueObject.setEntityId(entityId);
    }

    public java.lang.Integer getTypeId()
    {
        return this.valueObject.getTypeId();
    }
            
    public void setTypeId(java.lang.Integer typeId)
    {
        this.valueObject.setTypeId(typeId);
    }
      
    public java.util.Date getCreationDateTime()
    {
        return this.valueObject.getCreationDateTime();
    }
            
    public void setCreationDateTime(java.util.Date creationDateTime)
    {
        this.valueObject.setCreationDateTime(creationDateTime);
    }
             
    public String getCreator()
    {
        return this.valueObject.getCreator();
    }
            
    public void setCreator (String creator)
    {
        this.valueObject.setCreator(creator);
    }

    public org.infoglue.cms.entities.management.impl.simple.RepositoryImpl getRepository()
    {
        return this.repository;
    }
            
    public void setRepository (org.infoglue.cms.entities.management.impl.simple.RepositoryImpl repository)
    {
        this.repository = repository;
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
		setValueObject((EventVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getEventId();
	}

  }        
