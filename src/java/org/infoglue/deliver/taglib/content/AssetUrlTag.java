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
 * This is an attempt to make an TagLib for attempts to get a AssetUrl:s from a content referenced by a component
 * in a JSP.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <infoglue:component.AssetUrl propertyName="Logotype" assetKey="logotype"/>
 *
 * @author Mattias Bogeblad
 */

public class AssetUrlTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3546080250652931383L;

	private Integer digitalAssetId;
	private Integer contentId;
	private String propertyName;
    private String assetKey;
    private boolean useInheritance = true;
    private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;
    private boolean useDownloadAction = false;

    public AssetUrlTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
    	boolean oldUseDownloadAction = this.getController().getDeliveryContext().getUseDownloadAction();
    	this.getController().getDeliveryContext().setUseDownloadAction(useDownloadAction);
    	
        try
        {
			if(digitalAssetId != null)
			{
                produceResult(getController().getAssetUrlForAssetWithId(digitalAssetId));
			}
			else if(contentId != null)
            {
	            if(assetKey != null)
	                produceResult(getController().getAssetUrl(contentId, assetKey));
	            else
	                produceResult(getController().getAssetUrl(contentId));    
            }
            else if(propertyName != null)
            {
	            if(assetKey != null)
	                produceResult(getComponentLogic().getAssetUrl(propertyName, assetKey, useInheritance, useRepositoryInheritance, useStructureInheritance));
	            else
	                produceResult(getComponentLogic().getAssetUrl(propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance));                    
            }
            else
            {
	            if(assetKey != null)
	                produceResult(getController().getAssetUrl(getController().getComponentContentId(), assetKey));
	            else
	                produceResult(getController().getAssetUrl(getController().getComponentContentId()));    
            }
        }
        catch(Exception e)
        {
            throw new JspTagException("ComponentLogic.getAssetUrl Error: " + e.getMessage());
        }
        
        this.getController().getDeliveryContext().setUseDownloadAction(oldUseDownloadAction);
        
        return EVAL_PAGE;
    }

    public void setDigitalAssetId(String digitalAssetId) throws JspException
    {
        this.digitalAssetId = evaluateInteger("assetThumbnailUrl", "digitalAssetId", digitalAssetId);
    }

    public void setAssetKey(String assetKey) throws JspException
    {
        this.assetKey = evaluateString("assetUrl", "assetKey", assetKey);
    }

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("assetUrl", "propertyName", propertyName);
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

    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("assetUrl", "contentId", contentId);
    }

	public void setUseDownloadAction(boolean useDownloadAction)
	{
		this.useDownloadAction = useDownloadAction;
	}
}