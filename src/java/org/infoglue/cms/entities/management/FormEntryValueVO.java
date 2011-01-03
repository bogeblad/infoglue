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

public class FormEntryValueVO implements BaseEntityVO
{
    private java.lang.Integer formEntryValueId;
    private java.lang.Integer formEntryId;
    private java.lang.String name;
    private java.lang.String value;
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getFormEntryValueIdId();
	}

    public java.lang.Integer getFormEntryValueIdId()
    {
        return this.formEntryValueId;
    }
                
	public java.lang.Integer getFormEntryValueId()
	{
		return formEntryValueId;
	}

	public void setFormEntryValueId(java.lang.Integer formEntryValueId)
	{
		this.formEntryValueId = formEntryValueId;
	}

	public java.lang.Integer getFormEntryId()
	{
		return formEntryId;
	}

	public void setFormEntryId(java.lang.Integer formEntryId)
	{
		this.formEntryId = formEntryId;
	}

	public java.lang.String getName()
	{
		return name;
	}

	public void setName(java.lang.String name)
	{
		this.name = name;
	}

	public java.lang.String getValue()
	{
		return value;
	}

	public void setValue(java.lang.String value)
	{
		this.value = value;
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
        
