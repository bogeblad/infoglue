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

import java.util.Collection;
import java.util.Date;

import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

public interface FormEntry extends IBaseEntity
{
  	public Integer getId();
       
    public FormEntryVO getValueObject();
    
    public void setValueObject(FormEntryVO valueObject);
    
    public java.lang.Integer getFormEntryId();
    
    public void setFormEntryId(java.lang.Integer formEntryId) throws SystemException;
    
    public java.lang.Integer getFormContentId();
    
    public void setFormContentId(java.lang.Integer formContentId) throws SystemException;

    public java.lang.String getOriginAddress();
    
    public void setOriginAddress(java.lang.String originAddress) throws ConstraintException;
    
    public java.lang.String getFormName();
    
    public void setFormName(java.lang.String formName) throws ConstraintException;

    public java.lang.String getUserIP();
    
    public void setUserIP(java.lang.String userIP) throws ConstraintException;

    public java.lang.String getUserAgent();
    
    public void setUserAgent(java.lang.String userAgent) throws ConstraintException;

	public String getUserName();

	public void setUserName(String userName);

	public Date getRegistrationDateTime();

	public void setRegistrationDateTime(Date registrationDateTime);

    public Collection getFormEntryValues();
    
    public void setFormEntryValues(Collection formEntryValues) throws SystemException;

    public Collection getFormEntryAssets();

    public void setFormEntryAssets(Collection formEntryAssets) throws SystemException;

}
