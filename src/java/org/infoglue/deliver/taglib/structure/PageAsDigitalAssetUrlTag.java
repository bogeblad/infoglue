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
package org.infoglue.deliver.taglib.structure;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * 
 * @author mattias
 *
 */
public class PageAsDigitalAssetUrlTag extends ComponentLogicTag
{
	/**
	 * This method calls an page and stores it as an digitalAsset - that way one can avoid having to 
	 * serve javascript-files and css-files through InfoGlue. Not suitable for use if you have very dynamic
	 * css:es or scripts which includes logic depending on user info etc.. mostly usable if you have a static css
	 * or controls it on the pageCache parameters.
	 */

	private static final long serialVersionUID = 4050485595074016051L;
	
	private String propertyName;
	private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;
    private String fileSuffix = "";
	private boolean cacheUrl = true;
	
	private Integer siteNodeId;
	private Integer languageId;
	private Integer contentId = new Integer(-1);

	private String extraParameters;
	
	public PageAsDigitalAssetUrlTag() 
	{
		super();
	}

    public int doEndTag() throws JspException
    {
        produceResult(getPageAsDigitalAssetUrl());
        return EVAL_PAGE;
    }

	private String getPageAsDigitalAssetUrl() throws JspException
	{
	    if(this.languageId == null)
	        this.languageId = getController().getDeliveryContext().getLanguageId();
	    
	    if(this.propertyName != null)
	        return getComponentLogic().getPageAsDigitalAssetUrl(propertyName, languageId, contentId, useInheritance, fileSuffix, cacheUrl, useRepositoryInheritance, useStructureInheritance);
	    else
	        return getController().getPageAsDigitalAssetUrl(siteNodeId, languageId, contentId, fileSuffix, cacheUrl);
	}

	public void setSiteNodeId(final String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("pageAsDigitalAssetUrl", "siteNodeId", siteNodeId);
    }

    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("pageAsDigitalAssetUrl", "languageId", languageId);
    }

    public void setContentId(final String contentId) throws JspException
    {
        this.contentId = evaluateInteger("pageAsDigitalAssetUrl", "contentId", contentId);
    }
    
    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("pageAsDigitalAssetUrl", "propertyName", propertyName);
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

    public void setFileSuffix(String fileSuffix) throws JspException
    {
        this.fileSuffix = evaluateString("pageAsDigitalAssetUrl", "fileSuffix", fileSuffix);;
    }

    public void setExtraParameters(String extraParameters)
    {
        this.extraParameters = extraParameters;
    }
    
    public void setCacheUrl(boolean cacheUrl)
    {
        this.cacheUrl = cacheUrl;
    }

}
