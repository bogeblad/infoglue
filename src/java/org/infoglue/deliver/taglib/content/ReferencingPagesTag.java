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

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class ReferencingPagesTag extends ComponentLogicTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private Integer contentId;
	private Integer maxPages = new Integer(50);
	private Boolean excludeCurrentPage = new Boolean(false);

	public int doEndTag() throws JspException
    {
        setResultAttribute(getController().getReferencingPages(contentId, maxPages.intValue(), excludeCurrentPage));
	    
	    return EVAL_PAGE;
    }
    
    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("ReferencingPagesTag", "contentId", contentId);
    }

    public void setMaxPages(String maxPages) throws JspException
    {
        this.maxPages = evaluateInteger("ReferencingPagesTag", "maxPages", maxPages);
    }

    public void setExcludeCurrentPage(String excludeCurrentPage) throws JspException
    {
        this.excludeCurrentPage = (Boolean)evaluate("ReferencingPagesTag", "excludeCurrentPage", excludeCurrentPage, Boolean.class);
    }
}
