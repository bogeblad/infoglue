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

package org.infoglue.cms.applications.databeans;

/**
 * This bean represents a Asset Key definition. Used mostly by the content type definition editor.
 * 
 * @author Mattias Bogeblad
 */

public class AssetKeyDefinition
{
    private String assetKey;
    private Boolean isMandatory;
    private Boolean inherited = false;
    private String description;
    private Integer maximumSize;
    private String allowedContentTypes;
    private String imageWidth;
    private String imageHeight;
    private String assetUploadTransformationsSettings;
    
	public String getAssetKey()
    {
        return assetKey;
    }
    public void setAssetKey(String assetKey)
    {
        this.assetKey = assetKey;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getImageHeight()
    {
    	if(imageHeight == null)
    		return "*";
		return imageHeight;
    }
    public void setImageHeight(String imageHeight)
    {
        this.imageHeight = imageHeight;
    }
    public String getImageWidth()
    {
    	if(imageWidth == null)
    		return "*";
		return imageWidth;
    }
    public void setImageWidth(String imageWidth)
    {
        this.imageWidth = imageWidth;
    }
    public Integer getMaximumSize()
    {
        return maximumSize;
    }
    public void setMaximumSize(Integer maximumSize)
    {
        this.maximumSize = maximumSize;
    }
    public String getAllowedContentTypes()
    {
        return allowedContentTypes;
    }
    public void setAllowedContentTypes(String allowedContentTypes)
    {
        this.allowedContentTypes = allowedContentTypes;
    }
	public Boolean getIsMandatory()
	{
		return isMandatory;
	}
	public void setIsMandatory(Boolean isMandatory)
	{
		this.isMandatory = isMandatory;
	}
	public Boolean getInherited()
	{
		return inherited;
	}
	public void setInherited(Boolean inherited)
	{
		this.inherited = inherited;
	}
    public String getAssetUploadTransformationsSettings()
	{
		return assetUploadTransformationsSettings;
	}
	public void setAssetUploadTransformationsSettings(String assetUploadTransformationsSettings)
	{
		this.assetUploadTransformationsSettings = assetUploadTransformationsSettings;
	}

}
