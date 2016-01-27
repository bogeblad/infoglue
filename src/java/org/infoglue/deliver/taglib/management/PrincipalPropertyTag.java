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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.management.impl.simple.GroupPropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.RolePropertiesImpl;
import org.infoglue.cms.entities.management.impl.simple.UserPropertiesImpl;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class PrincipalPropertyTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String userName;
	private InfoGluePrincipal principal;
	private String attributeName;
	private Integer languageId 		= null;
    private boolean defeatCaches	= false;

    public PrincipalPropertyTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		if(languageId == null)
    		languageId = getController().getLanguageId();
    	
        //Here we store the defeat caches setting for later reset
        boolean previousDefeatCaches = getController().getDeliveryContext().getDefeatCaches();
		try
		{
	        Map<Class, List<Object>> entities = new HashMap<Class, List<Object>>();
	        entities.put(UserPropertiesImpl.class, Collections.EMPTY_LIST);
	        entities.put(GroupPropertiesImpl.class, Collections.EMPTY_LIST);
	        entities.put(RolePropertiesImpl.class, Collections.EMPTY_LIST);
	        getController().getDeliveryContext().setDefeatCaches(defeatCaches, entities);

		    if(userName != null && !userName.equals(""))
		    {
		        setResultAttribute(this.getController().getPrincipalPropertyValue(getController().getPrincipal(userName), attributeName, languageId));
		    }
		    else if(principal != null)
		    {
	            setResultAttribute(getController().getPrincipalPropertyValue(principal, attributeName, languageId));
		    }
		    else
		    {
		    	setResultAttribute(getController().getPrincipalPropertyValue(attributeName, languageId));
		    }
		}
		finally
		{
	        //Resetting the defeatcaches setting
	        getController().getDeliveryContext().setDefeatCaches(previousDefeatCaches, new HashMap<Class, List<Object>>());
	        languageId 		= null;
	        userName 		= null;
	        principal 		= null;
		    defeatCaches 	= false;
		}
	    	
        languageId 		= null;
        userName 		= null;
        principal 		= null;
	    defeatCaches 	= false;

        return EVAL_PAGE;
    }

    public void setUserName(final String userName) throws JspException
    {
        this.userName = evaluateString("principal", "userName", userName);
    }

    public void setPrincipal(final String principalString) throws JspException
    {
        this.principal = (InfoGluePrincipal)evaluate("principal", "principal", principalString, InfoGluePrincipal.class);
    }

    public void setPrincipalObject(final InfoGluePrincipal principal) throws JspException
    {
        this.principal = principal;
    }

    public void setAttributeName(final String attributeName) throws JspException
    {
        this.attributeName = evaluateString("principal", "attributeName", attributeName);
    }

    public void setLanguageId(final String languageIdString) throws JspException
    {
 	   this.languageId = this.evaluateInteger("principal", "languageId", languageIdString);
    }
    
    public void setDefeatCaches(final String defeatCaches) throws JspException
    {
        this.defeatCaches = (Boolean)evaluate("contentAttribute", "defeatCaches", defeatCaches, Boolean.class);
    }

}
