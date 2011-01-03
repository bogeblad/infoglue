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

import org.infoglue.deliver.taglib.TemplateControllerTag;

public class PageUrlAfterLanguageChangeTag extends TemplateControllerTag
{
	private static final long serialVersionUID = 4050485595074016051L;
	
	private String languageCode;
	
    public int doEndTag() throws JspException
    {
        produceResult(this.getController().getPageUrlAfterLanguageChange(languageCode));
        return EVAL_PAGE;
    }

	public void setLanguageCode(final String languageCode) throws JspException
    {
        this.languageCode = evaluateString("pageUrl", "languageCode", languageCode);
    }
}
