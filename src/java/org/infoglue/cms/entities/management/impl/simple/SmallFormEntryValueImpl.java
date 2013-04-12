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
import org.infoglue.cms.entities.management.FormEntry;
import org.infoglue.cms.entities.management.FormEntryValue;
import org.infoglue.cms.entities.management.FormEntryValueVO;
import org.infoglue.cms.entities.management.Redirect;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class SmallFormEntryValueImpl extends FormEntryValueImpl
{
    private FormEntryValueVO valueObject = new FormEntryValueVO();
    
	public Integer getId()
	{
		return this.getFormEntryValueId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	     
    
    public FormEntryValueVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(FormEntryValueVO valueObject)
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
		setValueObject((FormEntryValueVO) valueObject);
	}

	public Integer getFormEntryValueId()
	{
		return valueObject.getFormEntryValueId();
	}

	public String getName()
	{
		return valueObject.getName();
	}

	public String getValue()
	{
		return valueObject.getValue();
	}

	public void setFormEntryValueId(Integer formEntryValueId) throws SystemException
	{
		valueObject.setFormEntryValueId(formEntryValueId);
	}

	public void setName(String name) throws ConstraintException
	{
		valueObject.setName(name);
	}

	public void setValue(String value) throws ConstraintException
	{
		valueObject.setValue(value);
	}

	public Integer getFormEntryId()
	{
		return valueObject.getFormEntryId();
	}

	public void setFormEntryId(Integer formEntryId)
	{
		this.valueObject.setFormEntryId(formEntryId);
	}
}        
