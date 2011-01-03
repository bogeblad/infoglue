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
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class SiteNodeTypeDefinitionVO  implements BaseEntityVO
{

    private java.lang.Integer siteNodeTypeDefinitionId;
    private java.lang.String invokerClassName;
    private java.lang.String name;
    private java.lang.String description;
    
	public String toString()
	{  
		return getName();
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() {
		return getSiteNodeTypeDefinitionId();
	}

    public java.lang.Integer getSiteNodeTypeDefinitionId()
    {
        return this.siteNodeTypeDefinitionId;
    }
                
    public void setSiteNodeTypeDefinitionId(java.lang.Integer siteNodeTypeDefinitionId)
    {
        this.siteNodeTypeDefinitionId = siteNodeTypeDefinitionId;
    }
        
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name) throws ConstraintException
    {
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 		
    	ValidatorFactory.createStringValidator("SiteNodeTypeDefinition.name", true, 6, 30).validate(name, ceb);

 		ceb.throwIfNotEmpty();		
        this.name = name;

    }
    
    public java.lang.String getDescription()
    {
        return this.description;
    }
                
    public void setDescription(java.lang.String description) throws ConstraintException
    {
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	ValidatorFactory.createStringValidator("SiteNodeTypeDefinition.description", true, 6, 255).validate(description, ceb); 
 
 		ceb.throwIfNotEmpty();		
        this.description = description;

    }
    
    public java.lang.String getInvokerClassName()
    {
        return this.invokerClassName;
    }
                
    public void setInvokerClassName(java.lang.String invokerClassName) throws ConstraintException
    {
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	ValidatorFactory.createStringValidator("SiteNodeTypeDefinition.invokerClassName", true, 6, 100).validate(invokerClassName, ceb); 
 
 		ceb.throwIfNotEmpty();		
        this.invokerClassName = invokerClassName;

    }
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	if (name != null) ValidatorFactory.createStringValidator("SiteNodeTypeDefinition.name", true, 6, 30, true, SiteNodeTypeDefinitionImpl.class, this.getId(), null).validate(name, ceb);

		return ceb;
	}

}
        
