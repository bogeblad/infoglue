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

package org.infoglue.deliver.taglib.content;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This is an attempt to make an TagLib for attempts to get a AssetThumbnailUrl:s from a content referenced by a component
 * in a JSP.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <infoglue:component.AssetThumbnailUrl propertyName="Logotype" assetKey="logotype" width="100" height="100"/>
 *
 * @author Mattias Bogeblad
 */

public class AssetThumbnailUrlTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3978145452350648625L;

	private Integer digitalAssetId;
	private Integer contentId;
	private String propertyName;
    private String assetKey;
    private int width;
    private int height;
    private boolean useInheritance = true;
    private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;

    public AssetThumbnailUrlTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
		try 
		{
			if(digitalAssetId != null)
			{
                produceResult(getController().getAssetThumbnailUrlForAssetWithId(digitalAssetId, width, height));
			}
			else if(contentId != null)
            {
	            if(assetKey != null)
	                produceResult(getController().getAssetThumbnailUrl(contentId, assetKey, width, height));
	            else
	                produceResult(getController().getAssetThumbnailUrl(contentId, width, height));    
            }
            else if(propertyName != null)
            {
				if(assetKey != null)
					produceResult(getComponentLogic().getAssetThumbnailUrl(propertyName, assetKey, width, height, useInheritance, useRepositoryInheritance, useStructureInheritance));
				else
					produceResult(getComponentLogic().getAssetThumbnailUrl(propertyName, width, height, useInheritance, useRepositoryInheritance, useStructureInheritance));
            }
            else
            {
	            if(assetKey != null)
	                produceResult(getController().getAssetThumbnailUrl(getController().getComponentContentId(), assetKey, width, height));
	            else
	                produceResult(getController().getAssetThumbnailUrl(getController().getComponentContentId(), width, height));    
            }
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			throw new JspTagException("ComponentLogic.getAssetThumbnailUrl error: " + e.getMessage());
		}
        return EVAL_PAGE;
    }

    public void setAssetKey(String assetKey)
    {
        this.assetKey = assetKey;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public void setDigitalAssetId(String digitalAssetId) throws JspException
    {
        this.digitalAssetId = evaluateInteger("assetThumbnailUrl", "digitalAssetId", digitalAssetId);
    }

    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("assetThumbnailUrl", "contentId", contentId);
    }

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("assetThumbnailUrl", "propertyName", propertyName);
    }
    
    public void setUseInheritance(boolean useInheritance)
    {
        this.useInheritance = useInheritance;
    }

    public void setUseRepositoryInheritance(boolean useRepositoryInheritance)
    {
        this.useRepositoryInheritance = useRepositoryInheritance;
    }

    public void setUseStructureInheritance(boolean useStructureInheritance)
    {
        this.useStructureInheritance = useStructureInheritance;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}