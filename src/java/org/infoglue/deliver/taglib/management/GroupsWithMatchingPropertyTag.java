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

package org.infoglue.deliver.taglib.management;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class GroupsWithMatchingPropertyTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String propertyName;
	private String propertyValue;
	private Boolean useLanguageFallback = true;
	private Integer languageId;
	
	public int doEndTag() throws JspException
    {
		if(languageId == null)
			languageId = getController().getLanguageId();
		
		setResultAttribute(getController().getGroupsByMatchingProperty(propertyName, propertyValue, languageId, useLanguageFallback));

		this.propertyName = null;
		this.propertyValue = null;
		this.useLanguageFallback = true;
		this.languageId = null;
		
	    return EVAL_PAGE;
    }

    public void setPropertyName(final String propertyName) throws JspException
    {
        this.propertyName = evaluateString("groupsWithMatchingProperty", "propertyName", propertyName);
    }
    
    public void setPropertyValue(final String propertyValue) throws JspException
    {
        this.propertyValue = evaluateString("groupsWithMatchingProperty", "propertyValue", propertyValue);
    }
    
    public void setUseLanguageFallback(final String useLanguageFallback) throws JspException
    {
        this.useLanguageFallback = (Boolean)evaluate("groupsWithMatchingProperty", "useLanguageFallback", useLanguageFallback, Boolean.class);
    }

    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("groupsWithMatchingProperty", "languageId", languageId);
    }
}
