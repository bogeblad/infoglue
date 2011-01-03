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

package org.infoglue.cms.entities.content;

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface DigitalAsset extends IBaseEntity
{
        
    public DigitalAssetVO getValueObject();
    
    public void setValueObject(DigitalAssetVO valueObject);

    
    public java.lang.Integer getDigitalAssetId();
    
    public void setDigitalAssetId(java.lang.Integer digitalAssetId);
    
    public java.lang.String getAssetFileName();
    
    public void setAssetFileName(java.lang.String assetFileName);
    
    public java.lang.String getAssetKey();
    
    public void setAssetKey(java.lang.String assetKey);

    public java.lang.String getAssetFilePath();
    
    public void setAssetFilePath(java.lang.String assetFilePath);
    
    public java.lang.String getAssetContentType();
    
    public void setAssetContentType(java.lang.String assetContentType);
    
    public java.lang.Integer getAssetFileSize();
    
    public void setAssetFileSize(java.lang.Integer assetFileSize);
 
    public java.util.Collection getContentVersions();
    
    public void setContentVersions(java.util.Collection contentVersions);

    public java.util.Collection getUserProperties();
    
    public void setUserProperties(java.util.Collection userProperties);

    public java.util.Collection getRoleProperties();
    
    public void setRoleProperties(java.util.Collection roleProperties);

    public java.util.Collection getGroupProperties();
    
    public void setGroupProperties(java.util.Collection groupProperties);

    public void setAssetBlob(java.io.InputStream blob);

    public java.io.InputStream getAssetBlob();

	public void setAssetBytes(byte[] bytes);

	public byte[] getAssetBytes();

	public boolean getIsAssetBlobRead();
}
