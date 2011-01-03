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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class DigitalAssetVO implements BaseEntityVO, Cloneable
{

  	private java.lang.Integer digitalAssetId;
    private java.lang.String assetFileName;
    private java.lang.String assetKey;
    private java.lang.String assetFilePath;
    private java.lang.String assetContentType;
    private java.lang.Integer assetFileSize;
    

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
	public Integer getId() 
	{
		return getDigitalAssetId();
	}
	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	
	public ConstraintExceptionBuffer validate() 
	{ 
		return null;
	}
	  
    public java.lang.Integer getDigitalAssetId()
    {
        return this.digitalAssetId;
    }
                
    public void setDigitalAssetId(java.lang.Integer digitalAssetId)
    {
        this.digitalAssetId = digitalAssetId;
    }
    
    public java.lang.String getAssetFileName()
    {
        return this.assetFileName;
    }
                
    public void setAssetFileName(java.lang.String assetFileName)
    {
        this.assetFileName = assetFileName;
    }
    
    public java.lang.String getAssetKey()
    {
        return this.assetKey;
    }
                
    public void setAssetKey(java.lang.String assetKey)
    {
        this.assetKey = assetKey;
    }

    public java.lang.String getAssetFilePath()
    {
        return this.assetFilePath;
    }
                
    public void setAssetFilePath(java.lang.String assetFilePath)
    {
        this.assetFilePath = assetFilePath;
    }
    
    public java.lang.String getAssetContentType()
    {
        return this.assetContentType;
    }
                
    public void setAssetContentType(java.lang.String assetContentType)
    {
        this.assetContentType = assetContentType;
    }
    
    public java.lang.Integer getAssetFileSize()
    {
        return this.assetFileSize;
    }
                
    public void setAssetFileSize(java.lang.Integer assetFileSize)
    {
        this.assetFileSize = assetFileSize;
    }
    
	public DigitalAssetVO createCopy() throws Exception
	{
		return (DigitalAssetVO)this.clone();
	}

}
        
