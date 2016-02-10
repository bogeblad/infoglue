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

import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.exolab.castor.jdo.Database;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicURLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.ComponentLogic;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.jfree.util.Log;
import org.netbeans.lib.cvsclient.commandLine.command.log;

public class PageUrlTag extends ComponentLogicTag
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4050485595074016051L;
	
	private String propertyName;
	private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;
    private boolean forceHTTPProtocol = false;
    private boolean includeLanguageId = true;
    private boolean isDecorated = true;
	private Integer siteNodeId;
	private Integer languageId;
	private Integer contentId = new Integer(-1);
	private String operatingMode;
	
	private String extraParameters;
	
	public PageUrlTag() 
	{
		super();
	}

    public int doEndTag() throws JspException
    {
        produceResult(getPageUrl());
        
        this.propertyName = null;
        this.useInheritance = true;
        this.useRepositoryInheritance = true;
        this.useStructureInheritance = true;
        this.languageId = null;
        this.siteNodeId = null;
        this.contentId = null;
        this.extraParameters = null;
        this.includeLanguageId = true;
        this.operatingMode = null;
        this.isDecorated = true;
        
        return EVAL_PAGE;
    }

	private String getPageUrl() throws JspTagException
	{
	    if(this.languageId == null)
	        this.languageId = getController().getLanguageId();
	    String url = "";
	   
	    if(this.propertyName != null) {
	    	ComponentLogic componentLogic = getController().getComponentLogic();
	 		Map property = componentLogic.getInheritedComponentProperty(componentLogic.getInfoGlueComponent(), propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
	 		siteNodeId = componentLogic.getSiteNodeId(property);
	 		if(siteNodeId == null) {
	 			return url;
	 		}
	    }
	    
	    if (operatingMode == null) {
	    	url = getController().getPageUrl(siteNodeId, languageId, includeLanguageId, contentId);
	    } else{
	    	if (operatingMode.equalsIgnoreCase("3") && isDecorated == true) {
	    		/* live pages can not med combined with decorated mode */
		    	isDecorated = false;
	    	}
	    	DeliveryContext dc = getController().getDeliveryContext();
	    	String tempOperatingMode = dc.getOperatingMode();
	    	dc.setOperatingMode(operatingMode);
	    	try {
	    		url = getController().getPageUrl(siteNodeId, languageId, includeLanguageId, -1, operatingMode, isDecorated);
	    	} finally {
		    	/* restoring operatingMode */
		    	dc.setOperatingMode(tempOperatingMode);	
	    	}
	    }
	    if (forceHTTPProtocol || CmsPropertyHandler.getForceHTTPProtocol()) {
	    	url = url.replaceFirst("https:", "http:");
	    }

	    return url;
	}

	public void setSiteNodeId(final String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("pageUrl", "siteNodeId", siteNodeId);
    }
	public void setForceHTTPProtocol(final String forceHTTPProtocol) throws JspException
    {
        this.forceHTTPProtocol = evaluateBoolean("pageUrl", "forceHTTPProtocol", forceHTTPProtocol);
    }
	
    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("pageUrl", "languageId", languageId);
    }
    
    public void setOperatingMode(final String operatingMode) throws JspException
    {
        this.operatingMode = evaluateString("pageUrl", "operatingMode", operatingMode);
    }
    
    public void setContentId(final String contentId) throws JspException
    {
        this.contentId = evaluateInteger("pageUrl", "contentId", contentId);
    }
    
    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("pageUrl", "propertyName", propertyName);
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

    public void setExtraParameters(String extraParameters)
    {
        this.extraParameters = extraParameters;
    }
    
	public void setIncludeLanguageId(final String includeLanguageId) throws JspException
    {
        this.includeLanguageId = evaluateBoolean("pageUrlAfterLanguageChange", "includeLanguageId", includeLanguageId);
    }
	
	public void setIsDecorated(final String isDecorated) throws JspException
    {
        this.isDecorated = evaluateBoolean("pageUrlAfterLanguageChange", "isDecorated", isDecorated);
    }
}
