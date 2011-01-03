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

package org.infoglue.cms.entities.content.impl.simple;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;

public class DigitalAssetImpl implements DigitalAsset
{
    private DigitalAssetVO valueObject = new DigitalAssetVO();
	private byte[] assetBytes = null;
	private java.util.Collection contentVersions;
	private java.util.Collection userProperties;
	private java.util.Collection roleProperties;
	private java.util.Collection groupProperties;
	private java.io.InputStream assetBlob;
	private boolean assetBlobRead = false;
	    
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
		setValueObject((DigitalAssetVO) valueObject);
	}
 
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getId()
	 */
	public Integer getId() 
	{
		return getDigitalAssetId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
		
    public DigitalAssetVO getValueObject()
    {
        return this.valueObject;
    }
        
    public void setValueObject(DigitalAssetVO valueObject)
    {
        this.valueObject = valueObject;
    }   
  	
    public java.lang.Integer getDigitalAssetId()
    {
        return this.valueObject.getDigitalAssetId();
    }
            
    public void setDigitalAssetId(java.lang.Integer digitalAssetId)
    {
        this.valueObject.setDigitalAssetId(digitalAssetId);
    }
      
    public java.lang.String getAssetFileName()
    {
        return this.valueObject.getAssetFileName();
    }
            
    public void setAssetFileName(java.lang.String assetFileName)
    {
        this.valueObject.setAssetFileName(assetFileName);
    }

    public java.lang.String getAssetKey()
    {
        return this.valueObject.getAssetKey();
    }
            
    public void setAssetKey(java.lang.String assetKey)
    {
        this.valueObject.setAssetKey(assetKey);
    }
      
    public java.lang.String getAssetFilePath()
    {
        return this.valueObject.getAssetFilePath();
    }
            
    public void setAssetFilePath(java.lang.String assetFilePath)
    {
        this.valueObject.setAssetFilePath(assetFilePath);
    }
      
    public java.lang.String getAssetContentType()
    {
        return this.valueObject.getAssetContentType();
    }
            
    public void setAssetContentType(java.lang.String assetContentType)
    {
        this.valueObject.setAssetContentType(assetContentType);
    }
      
    public java.lang.Integer getAssetFileSize()
    {
        return this.valueObject.getAssetFileSize();
    }
            
    public void setAssetFileSize(java.lang.Integer assetFileSize)
    {
        this.valueObject.setAssetFileSize(assetFileSize);
    }
      
    public java.util.Collection getContentVersions()
    {
        return this.contentVersions;
    }
            
    public void setContentVersions (java.util.Collection contentVersions)
    {
        this.contentVersions = contentVersions;
    }
    
    public java.util.Collection getUserProperties()
    {
        return this.userProperties;
    }
    
    public void setUserProperties(java.util.Collection userProperties)
    {
        this.userProperties = userProperties;
    }

    public java.util.Collection getRoleProperties()
    {
        return this.roleProperties;
    }
    
    public void setRoleProperties(java.util.Collection roleProperties)
    {
        this.roleProperties = roleProperties;
    }

    public java.util.Collection getGroupProperties()
    {
        return this.groupProperties;
    }
    
    public void setGroupProperties(java.util.Collection groupProperties)
    {
        this.groupProperties = groupProperties;
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
        
        assetBlobRead = true;
        
        return inputStream;
    }
    
	public void setAssetBytes(byte[] bytes)
	{
	    setAssetBlob(new ByteArrayInputStream(bytes));
	}

	
	public byte[] getAssetBytes()
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
				Thread.dumpStack();
			}
		}
				
		return this.assetBytes;
	}

	public boolean getIsAssetBlobRead() 
	{
		return assetBlobRead;
	}
}        
