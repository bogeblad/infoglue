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


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.FormEntry;
import org.infoglue.cms.entities.management.FormEntryAsset;
import org.infoglue.cms.entities.management.FormEntryAssetVO;
import org.infoglue.cms.entities.management.FormEntryValue;
import org.infoglue.cms.entities.management.FormEntryValueVO;
import org.infoglue.cms.entities.management.Redirect;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


public class FormEntryAssetImpl implements FormEntryAsset
{
    private FormEntryAssetVO valueObject = new FormEntryAssetVO();
    private FormEntry formEntry;
	private java.io.InputStream assetBlob;
	private byte[] assetBytes = null;

	public Integer getId()
	{
		return this.getFormEntryAssetId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	     
    
    public FormEntryAssetVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(FormEntryAssetVO valueObject)
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
		setValueObject((FormEntryAssetVO) valueObject);
	}

	public Integer getFormEntryAssetId()
	{
		return valueObject.getFormEntryAssetId();
	}

	public String getFileName()
	{
		return valueObject.getFileName();
	}

	public String getAssetKey()
	{
		return valueObject.getAssetKey();
	}

	public String getContentType()
	{
		return valueObject.getContentType();
	}

	public Integer getFileSize()
	{
		return valueObject.getFileSize();
	}

	public void setFormEntryAssetId(Integer formEntryAssetId)
	{
		valueObject.setFormEntryAssetId(formEntryAssetId);
	}

	public void setFileName(String fileName)
	{
		valueObject.setFileName(fileName);
	}

	public void setAssetKey(String assetKey)
	{
		valueObject.setAssetKey(assetKey);
	}

	public void setContentType(String contentType)
	{
		valueObject.setContentType(contentType);
	}

	public void setFileSize(Integer fileSize)
	{
		valueObject.setFileSize(fileSize);
	}
	
	public FormEntry getFormEntry()
	{
		return formEntry;
	}

	public void setFormEntry(FormEntry formEntry)
	{
		this.formEntry = formEntry;
	}
	
    public void setAssetBlob(java.io.InputStream assetBlob)
    {
        this.assetBlob = assetBlob;
    }

    public synchronized InputStream getAssetBlob()
    {
        InputStream inputStream = null;
        if(this.assetBytes != null)
        {
            inputStream = new ByteArrayInputStream(assetBytes);            
        }
        else
        {
            inputStream = assetBlob;
        }
        
        return inputStream;
    }
    
	public void setBytes(byte[] bytes)
	{
	    setAssetBlob(new ByteArrayInputStream(bytes));
	}

	
	public byte[] getBytes()
	{
		if(this.assetBytes == null)
		{
			try
			{
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				
				BufferedInputStream bis = new BufferedInputStream(getAssetBlob());

				int character;
				while ((character = bis.read()) != -1)
				{
					byteArrayOutputStream.write(character);
				}
				byteArrayOutputStream.flush();
		
				bis.close();
				byteArrayOutputStream.close();
				
				this.assetBytes = byteArrayOutputStream.toByteArray();
			}
			catch(Exception e)
			{
				System.out.println("The asset with id:" + this.getId() + " had no assetBlob or an error occurred when we tried to get it:" + e.getMessage());
			}
		}
				
		return this.assetBytes;
	}

}        
