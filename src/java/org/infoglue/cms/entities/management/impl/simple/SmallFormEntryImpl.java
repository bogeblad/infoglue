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
import java.util.Collection;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.FormEntry;
import org.infoglue.cms.entities.management.FormEntryVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class SmallFormEntryImpl extends FormEntryImpl
{
    private FormEntryVO valueObject = new FormEntryVO();
	
	public Integer getId()
	{
		return this.getFormEntryId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}

	public Integer getFormContentId()
	{
		return valueObject.getFormContentId();
	}

	public Integer getFormEntryId()
	{
		return valueObject.getFormEntryId();
	}

	public String getFormName()
	{
		return valueObject.getFormName();
	}

	public String getOriginAddress()
	{
		return valueObject.getOriginAddress();
	}

	public String getUserAgent()
	{
		return valueObject.getUserAgent();
	}

	public String getUserIP()
	{
		return valueObject.getUserIP();
	}

	public void setFormContentId(Integer formContentId) throws SystemException
	{
		valueObject.setFormContentId(formContentId);
	}

	public void setFormEntryId(Integer formEntryId) throws SystemException
	{
		valueObject.setFormEntryId(formEntryId);
	}

	public void setFormName(String formName) throws ConstraintException
	{
		valueObject.setFormName(formName);
	}

	public void setOriginAddress(String originAddress) throws ConstraintException
	{
		valueObject.setOriginAddress(originAddress);
	}

	public void setUserAgent(String userAgent) throws ConstraintException
	{
		valueObject.setUserAgent(userAgent);
	}

	public void setUserIP(String userIP) throws ConstraintException
	{
		valueObject.setUserIP(userIP);
	}

    public FormEntryVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(FormEntryVO valueObject)
    {
        this.valueObject = valueObject;
    }   

	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}

	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((FormEntryVO) valueObject);
	}

}        
