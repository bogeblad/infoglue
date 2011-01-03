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

import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * Tag for org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController.getHasContentVersionInState(<String>, <Sring>, <boolean>);
 */
public class HasContentVersionInStateTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3258135773294113587L;

	private Integer contentId;
	private Integer languageId;
	private Integer stateId;
    
    public int doEndTag() throws JspException
    {
		produceResult(getHasContentVersionInState());
    
		this.contentId = null;
		this.languageId = null;
		this.stateId = null;
	    
		return EVAL_PAGE;
    }

	private Boolean getHasContentVersionInState() throws JspException
	{	
		if(this.languageId == null)
			this.languageId = getController().getLanguageId();

	    return this.getController().getHasContentVersionInState(this.contentId, this.languageId, this.stateId);
	}
	
	public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("HasContentVersionInState", "contentId", contentId);
    }

    public void setLanguageId(String languageId) throws JspException
    {
        this.languageId = evaluateInteger("HasContentVersionInState", "languageId", languageId);
    }

    public void setStateId(String stateId) throws JspException
    {
        this.stateId = evaluateInteger("HasContentVersionInState", "stateId", stateId);
    }

}