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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class QualifyerVO implements BaseEntityVO
{
	private java.lang.Integer qualifyerId;
    private java.lang.String name;
    private java.lang.String value;
	private java.lang.String path;
    private java.lang.Integer sortOrder;
     
    public java.lang.Integer getQualifyerId()
    {
        return this.qualifyerId;
    }
                
    public void setQualifyerId(java.lang.Integer qualifyerId)
    {
        this.qualifyerId = qualifyerId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;
    }
    
    public java.lang.String getValue()
    {
        return this.value;
    }
                
    public void setValue(java.lang.String value)
    {
        this.value = value;
    }
    
    public java.lang.Integer getSortOrder()
    {
        return this.sortOrder;
    }
                
    public void setSortOrder(java.lang.Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

	public String getPath()
	{
		return this.path;
	}

	public String getShortPath()
	{
		return this.path.substring(this.path.lastIndexOf("/") + 1);
	}
			
	public void setPath(String path)
	{
		this.path = path;
	}

    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return getQualifyerId();
	}
	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{ 
		return null;
	}

}
        
