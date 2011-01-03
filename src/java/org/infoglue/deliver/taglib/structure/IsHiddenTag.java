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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.structuretool.actions.CreateSiteNodeAction;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class IsHiddenTag extends ComponentLogicTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

    private final static Logger logger = Logger.getLogger(IsHiddenTag.class.getName());

	private Integer siteNodeId;
	
    public IsHiddenTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		boolean isHidden = false;
		try
		{
			SiteNodeVersionVO svvo 	= NodeDeliveryController.getNodeDeliveryController(this.getController().getSiteNodeId(), this.getController().getLanguageId(), this.getController().getContentId()).getLatestActiveSiteNodeVersionVO(this.getController().getDatabase(), siteNodeId);
			isHidden 		= svvo.getIsHidden();
		}
		catch (Exception e) 
		{
			logger.warn("Exception in IsHiddenTag:" + e.getMessage());
		}
		
		setResultAttribute(isHidden);
        
		this.siteNodeId = null;
		
		return EVAL_PAGE;
    }

    public void setSiteNodeId(String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("IsHiddenTag", "siteNodeId", siteNodeId);
    }

}
