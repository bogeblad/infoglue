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
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class RegistryVO implements BaseEntityVO
{
    public final static Integer INLINE_LINK 				= new Integer(0);
    public final static Integer INLINE_ASSET 				= new Integer(1);
    public final static Integer PAGE_COMPONENT 				= new Integer(2);
    public final static Integer PAGE_COMPONENT_BINDING 		= new Integer(3);
    public final static Integer PAGE_BINDING 				= new Integer(4);
    public final static Integer INLINE_SITE_NODE_RELATION	= new Integer(5);
    public final static Integer INLINE_CONTENT_RELATION		= new Integer(6);
    
    private Integer registryId;
    private String entityName;
    private String entityId;
    private Integer referenceType;
    private String referencingEntityName;
    private String referencingEntityId;
    private String referencingEntityCompletingName;
    private String referencingEntityCompletingId;
        
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getRegistryId();
	}

    public Integer getRegistryId()
    {
        return registryId;
    }
    
    public void setRegistryId(Integer registryId)
    {
        this.registryId = registryId;
    }

    public String getEntityId()
    {
        return entityId;
    }
    
    public void setEntityId(String entityId)
    {
        this.entityId = entityId;
    }
    
    public String getEntityName()
    {
        return entityName;
    }
    
    public void setEntityName(String entityName)
    {
        this.entityName = entityName;
    }
    
    public Integer getReferenceType()
    {
        return referenceType;
    }
    
    public void setReferenceType(Integer referenceType)
    {
        this.referenceType = referenceType;
    }
    
    public String getReferencingEntityId()
    {
        return referencingEntityId;
    }
    
    public void setReferencingEntityId(String referencingEntityId)
    {
        this.referencingEntityId = referencingEntityId;
    }
    
    public String getReferencingEntityName()
    {
        return referencingEntityName;
    }
    
    public void setReferencingEntityName(String referencingEntityName)
    {
        this.referencingEntityName = referencingEntityName;
    }

    public String getReferencingEntityCompletingId()
    {
        return referencingEntityCompletingId;
    }

    public void setReferencingEntityCompletingId(String referencingEntityCompletingId)
    {
        this.referencingEntityCompletingId = referencingEntityCompletingId;
    }
    
    public String getReferencingEntityCompletingName()
    {
        return referencingEntityCompletingName;
    }
    
    public void setReferencingEntityCompletingName(String referencingEntityCompletingName)
    {
        this.referencingEntityCompletingName = referencingEntityCompletingName;
    }

	public String toString()
	{  
		return entityName;
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
        
