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

import java.util.List;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This tag returns all languages available for the current repository minus any disables languages for this page.
 * @author Mattias Bogeblad
 *
 */

public class SiteNodeLanguagesTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3258135773294113587L;

	private Integer siteNodeId;
    
    public SiteNodeLanguagesTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
		produceResult(getPageLanguages());
        return EVAL_PAGE;
    }

	private List getPageLanguages() throws JspException
	{
	    if(this.siteNodeId != null)
	        return this.getController().getPageLanguages(this.siteNodeId);
	    else
	    	return this.getController().getPageLanguages();
	}
	
    public void setSiteNodeId(String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("SiteNodeLanguagesTag", "siteNodeId", siteNodeId);
    }

}