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

public class FormEntryAssetVO implements BaseEntityVO
{
    private java.lang.Integer formEntryAssetId;
    private java.lang.Integer formEntryId;
    private java.lang.String fileName;
	private java.lang.String assetKey;
    private java.lang.String contentType;
    private java.lang.Integer fileSize;
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getFormEntryAssetIdId();
	}

    public java.lang.Integer getFormEntryAssetIdId()
    {
        return this.formEntryAssetId;
    }
                
	public java.lang.Integer getFormEntryAssetId()
	{
		return formEntryAssetId;
	}

	public void setFormEntryAssetId(java.lang.Integer formEntryAssetId)
	{
		this.formEntryAssetId = formEntryAssetId;
	}

	public java.lang.Integer getFormEntryId()
	{
		return formEntryId;
	}

	public void setFormEntryId(java.lang.Integer formEntryId)
	{
		this.formEntryId = formEntryId;
	}

    public java.lang.String getFileName()
	{
		return fileName;
	}

	public void setFileName(java.lang.String fileName)
	{
		this.fileName = fileName;
	}

	public java.lang.String getAssetKey()
	{
		return assetKey;
	}

	public void setAssetKey(java.lang.String assetKey)
	{
		this.assetKey = assetKey;
	}

	public java.lang.String getContentType()
	{
		return contentType;
	}

	public void setContentType(java.lang.String contentType)
	{
		this.contentType = contentType;
	}

	public java.lang.Integer getFileSize()
	{
		return fileSize;
	}

	public void setFileSize(java.lang.Integer fileSize)
	{
		this.fileSize = fileSize;
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
        
