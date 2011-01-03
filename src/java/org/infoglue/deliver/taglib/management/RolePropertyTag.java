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
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class RolePropertyTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String roleName;
	private InfoGlueRole role;
	private String propertyName;
	private Integer languageId = null;
	private Boolean useLanguageFallback = true;
	
    public RolePropertyTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		if(languageId == null)
    		languageId = getController().getLanguageId();
    	
	    if(roleName != null && !roleName.equals(""))
	    {
	        setResultAttribute(this.getController().getRolePropertyValue(this.getController().getRole(roleName), propertyName, languageId, useLanguageFallback));
	    }
	    else if(role != null)
	    {
            setResultAttribute(getController().getRolePropertyValue(role, propertyName, languageId, useLanguageFallback));
	    }
	    else
	    {
	    	throw new JspException("Must set either roleName or role attribute.");
	    }

	    languageId = null;
        roleName = null;
        role = null;
        propertyName = null;
        useLanguageFallback = true;
        
        return EVAL_PAGE;
    }

    public void setRoleName(final String roleName) throws JspException
    {
        this.roleName = evaluateString("role", "roleName", roleName);
    }

    public void setRole(final String role) throws JspException
    {
        this.role = (InfoGlueRole)evaluate("role", "role", role, InfoGlueRole.class);
    }

    public void setPropertyName(final String propertyName) throws JspException
    {
        this.propertyName = evaluateString("role", "propertyName", propertyName);
    }

    public void setLanguageId(final String languageIdString) throws JspException
    {
 	   this.languageId = this.evaluateInteger("role", "languageId", languageIdString);
    }

    public void setUseLanguageFallback(final String useLanguageFallback) throws JspException
    {
 	   this.useLanguageFallback = (Boolean)this.evaluate("role", "useLanguageFallback", useLanguageFallback, Boolean.class);
    }
}
