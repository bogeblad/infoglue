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

import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class SiteNodeTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3258135773294113587L;

	private Integer siteNodeId;
	private Integer targetSiteNodeId;
	private String propertyName;
	private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;

    public SiteNodeTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
		produceResult(getSiteNode());
        return EVAL_PAGE;
    }

	private SiteNodeVO getSiteNode() throws JspException
	{
	    if(this.siteNodeId != null)
	        return this.getController().getSiteNode(this.siteNodeId);
	    else if(this.propertyName != null)
	    {
	        if(this.targetSiteNodeId != null)
	            return this.getComponentLogic().getBoundSiteNode(targetSiteNodeId, propertyName, useInheritance);
	        else
	            return this.getComponentLogic().getBoundSiteNode(propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
	    }
	    else if(this.getController().getSiteNodeId() != null && this.getController().getSiteNodeId().intValue() > -1)
	        return this.getController().getSiteNode();
	    else
	        return null;
	}
	
    public void setSiteNodeId(String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("siteNode", "siteNodeId", siteNodeId);;
    }

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("siteNode", "propertyName", propertyName);
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

    public void setTargetSiteNodeId(String targetSiteNodeId) throws JspException
    {
        this.targetSiteNodeId = evaluateInteger("siteNode", "targetSiteNodeId", targetSiteNodeId);
    }

}