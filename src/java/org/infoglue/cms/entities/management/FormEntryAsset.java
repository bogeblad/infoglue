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

import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

public interface FormEntryAsset extends IBaseEntity
{	  
  	public Integer getId();
       
    public FormEntryAssetVO getValueObject();
    
    public void setValueObject(FormEntryAssetVO valueObject);
    
    public java.lang.Integer getFormEntryAssetId();
    
    public void setFormEntryAssetId(java.lang.Integer formEntryAssetId);
    
    public java.lang.String getFileName();
    
    public void setFileName(java.lang.String fileName);
    
    public java.lang.String getAssetKey();
    
    public void setAssetKey(java.lang.String assetKey);

    public java.lang.String getContentType();
    
    public void setContentType(java.lang.String contentType);

    public java.lang.Integer getFileSize();
    
    public void setFileSize(java.lang.Integer fileSize);

    public FormEntry getFormEntry();
    
    public void setFormEntry(FormEntry formEntry);

    public void setAssetBlob(java.io.InputStream blob);

    public java.io.InputStream getAssetBlob();

	public void setBytes(byte[] bytes);

	public byte[] getBytes();

}
