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
import org.infoglue.cms.util.validators.ValidatorFactory;

public class InterceptionPointVO  implements BaseEntityVO
{

    private java.lang.Integer interceptionPointId;
	private java.lang.String category;
    private java.lang.String name;
    private java.lang.String description;
    private java.lang.Boolean usesExtraDataForAccessControl;

    public InterceptionPointVO()
    {
    }

    public InterceptionPointVO(String category, String name, String description, boolean usesExtraDataForAccessControl)
    {
        this.category = category;
        this.name = name;
        this.description = description;
        this.usesExtraDataForAccessControl = new Boolean(usesExtraDataForAccessControl);
    }
    
    public java.lang.Integer getInterceptionPointId()
    {
        return this.interceptionPointId;
    }
                
    public void setInterceptionPointId(java.lang.Integer interceptionPointId)
    {
        this.interceptionPointId = interceptionPointId;
    }
    
	public java.lang.String getCategory()
	{
		return this.category;
	}
                
	public void setCategory(java.lang.String category)
	{
		this.category = category;
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
    
	public java.lang.Boolean getUsesExtraDataForAccessControl()
	{
		return usesExtraDataForAccessControl;
	}

	public void setUsesExtraDataForAccessControl(java.lang.Boolean usesExtraDataForAccessControl)
	{
		this.usesExtraDataForAccessControl = usesExtraDataForAccessControl;
	}


	public String toString()
	{  
		return getCategory() + ":" + getName();
	}
   
	public Integer getId() 
	{
		return this.getInterceptionPointId();
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
		ValidatorFactory.createStringValidator("InterceptionPoint.name", true, 2, 100).validate(name, ceb);
		ValidatorFactory.createStringValidator("InterceptionPoint.category", true, 2, 30).validate(category, ceb);
		ValidatorFactory.createStringValidator("InterceptionPoint.description", true, 2, 100).validate(description, ceb);
		
    	return ceb;
	}
    

}
        
