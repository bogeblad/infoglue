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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicURLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.jfree.util.Log;

public class CustomPageUrlTag extends ComponentLogicTag
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4050485595074016051L;

    private String customPath = "";
	private Integer siteNodeId;
	private Integer languageId;
	private String operatingMode = "3";
	
	private Integer contentId = new Integer(-1);

	public CustomPageUrlTag() 
	{
		super();
	}

    public int doEndTag() throws JspException
    {
        try {
			produceResult(getCustomPageUrl());
		} catch (Exception e) {
			Log.error("Could not get url");
		}
        

        this.languageId = null;
        this.siteNodeId = null;
        this.contentId = null;
        this.operatingMode = "";
        this.customPath = "";
        
        return EVAL_PAGE;
    }

	private String getCustomPageUrl() throws Exception
	{
	    if(this.languageId == null) {
	        this.languageId = getController().getLanguageId();
	    }
	    
	    if(this.siteNodeId == null) {
	    	this.siteNodeId = getController().getSiteNodeId();
	    }
	    if (!customPath.isEmpty() && !customPath.startsWith("/") ) {
	    	customPath = "/" + customPath;
	    }
	    DeliveryContext dc = getController().getDeliveryContext();
	    dc.setOperatingMode(operatingMode);
	    URLComposer basicURLComposer = BasicURLComposer.getURLComposer();
	    dc.setUseFullUrl(true);
	    return basicURLComposer.composePageUrl(getController().getDatabase(), getController().getPrincipal(), this.siteNodeId, this.languageId, contentId, customPath, dc);
	}

    public void setCustomPath(String customPath)
    {
        this.customPath = customPath;
    }
    public void setLanguageId(Integer languageId)
    {
        this.languageId = languageId;
    }    
    public void setSiteNodeId(Integer siteNodeId)
    {
        this.siteNodeId = siteNodeId;
    }   
    public void setOperatingMode(String operatingMode)
    {
        this.operatingMode = operatingMode;
    }
}
