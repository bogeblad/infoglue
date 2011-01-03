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
 * <infoglue:component.assets propertyName="Global images"/>
 *
 * @author Mattias Bogeblad
 */

public class AssetUrlsTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3546080250652931383L;

	private Integer contentId;
	private String propertyName;
    private boolean useInheritance = true;
    private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;

    public AssetUrlsTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
        try
        {
        	if(contentId != null)
               	produceResult(getController().getAssetUrls(contentId));                    
        	else
        		produceResult(getComponentLogic().getAssetUrls(propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance));                    
        }
        catch(Exception e)
        {
            throw new JspTagException("ComponentLogic.getAssetUrls Error: " + e.getMessage());
        }
        
        this.contentId = null;
        this.propertyName = null;
        this.useInheritance = true;
        this.useRepositoryInheritance = true;
        this.useStructureInheritance = true;
        
        return EVAL_PAGE;
    }

    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("assets", "contentId", contentId);
    }

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("assets", "propertyName", propertyName);
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

}