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
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class GroupPropertyTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String groupName;
	private InfoGlueGroup group;
	private String propertyName;
	private Integer languageId = null;
	private Boolean useLanguageFallback = true;
	
    public GroupPropertyTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		if(languageId == null)
    		languageId = getController().getLanguageId();
    	
	    if(groupName != null && !groupName.equals(""))
	    {
	        setResultAttribute(this.getController().getGroupPropertyValue(this.getController().getGroup(groupName), propertyName, languageId, useLanguageFallback));
	    }
	    else if(group != null)
	    {
            setResultAttribute(getController().getGroupPropertyValue(group, propertyName, languageId, useLanguageFallback));
	    }
	    else
	    {
	    	throw new JspException("Must set either groupName or group attribute.");
	    }

	    languageId = null;
        groupName = null;
        group = null;
        propertyName = null;
        useLanguageFallback = true;
        
        return EVAL_PAGE;
    }

    public void setGroupName(final String groupName) throws JspException
    {
        this.groupName = evaluateString("group", "groupName", groupName);
    }

    public void setGroup(final String principalString) throws JspException
    {
        this.group = (InfoGlueGroup)evaluate("group", "group", principalString, InfoGlueGroup.class);
    }

    public void setPropertyName(final String propertyName) throws JspException
    {
        this.propertyName = evaluateString("group", "propertyName", propertyName);
    }

    public void setLanguageId(final String languageIdString) throws JspException
    {
 	   this.languageId = this.evaluateInteger("group", "languageId", languageIdString);
    }

    public void setUseLanguageFallback(final String useLanguageFallback) throws JspException
    {
 	   this.useLanguageFallback = (Boolean)this.evaluate("group", "useLanguageFallback", useLanguageFallback, Boolean.class);
    }
}
