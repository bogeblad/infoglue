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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.webservices.elements.RemoteAttachment;
import org.infoglue.deliver.taglib.AbstractTag;
import org.infoglue.deliver.taglib.content.ContentParameterTag;
import org.infoglue.deliver.taglib.content.ContentVersionParameterInterface;

/**
 * This class implements support for adding access rights to a content
 */
public class AccessRightParameterTag extends AbstractTag //implements ContentVersionParameterInterface 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The access right object.
	 */
	private Map accessRight = new HashMap();
	
	
	/**
	 * Default constructor. 
	 */
	public AccessRightParameterTag()
	{
		super();
	}

	/**
	 * Initializes the parameters to make it accessible for the children tags (if any).
	 * 
	 * @return indication of whether to evaluate the body or not.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doStartTag() throws JspException 
	{
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Adds a parameter with the specified name and value to the parameters
	 * of the parent tag.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		addAccessRight();
		
		accessRight = new HashMap();
		
		return EVAL_PAGE;
    }
	
	/**
	 * Adds the content version to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addAccessRight() throws JspException
	{
		AbstractTag parent = (ContentParameterTag) findAncestorWithClass(this, ContentParameterTag.class);
		if(parent == null)
		{
			parent = (RemoteAccessRightsServiceTag) findAncestorWithClass(this, RemoteAccessRightsServiceTag.class);
			if(parent == null)
			{
				throw new JspTagException("AccessRightParameterTag must have a ContentParameterTag ancestor.");
			}
			else
				((RemoteAccessRightsServiceTag) parent).addAccessRight(accessRight);
		}
		else
			((ContentParameterTag) parent).addAccessRight(accessRight);
	}

	/**
	 * Adds the access right attribute to the access right Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addAccessRightAttribute(String name, String value) throws JspException
	{
	    Map accessRightAttributes = (Map)this.accessRight.get("accessRightAttributes");
	    if(accessRightAttributes == null)
	    {
	    	accessRightAttributes = new HashMap();
	        this.accessRight.put("accessRightAttributes", accessRightAttributes);
	    }

	    accessRightAttributes.put(name, value);
	}

	
	/**
	 * Adds a accessRightRole attribute to the accessRight map.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addAccessRightRole(String roleName)
	{
	    List accessRightRoles = (List)this.accessRight.get("accessRightRoles");
	    if(accessRightRoles == null)
	    {
	    	accessRightRoles = new ArrayList();
	        this.accessRight.put("accessRightRoles", accessRightRoles);
	    }

	    accessRightRoles.add(roleName);
	}

	/**
	 * Adds a accessRightGroup attribute to the accessRight map.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addAccessRightGroup(String groupName)
	{
	    List accessRightGroups = (List)this.accessRight.get("accessRightGroups");
	    if(accessRightGroups == null)
	    {
	    	accessRightGroups = new ArrayList();
	        this.accessRight.put("accessRightGroups", accessRightGroups);
	    }

	    accessRightGroups.add(groupName);
	}

	/**
	 * Adds a accessRightUser attribute to the accessRight map.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addAccessRightUser(String userName)
	{
	    List accessRightUsers = (List)this.accessRight.get("accessRightUsers");
	    if(accessRightUsers == null)
	    {
	    	accessRightUsers = new ArrayList();
	        this.accessRight.put("accessRightUsers", accessRightUsers);
	    }

	    accessRightUsers.add(userName);
	}
	
	/**
	 * 
	 */
	public void setParameters(final String parameters) throws JspException
	{
	    this.accessRight.put("parameters", evaluateString("AccessRightParameterTag", "parameters", parameters));
	}

	/**
	 * 
	 */
	public void setInterceptionPointName(final String interceptionPointName) throws JspException
	{
	    this.accessRight.put("interceptionPointName", evaluateString("AccessRightParameterTag", "interceptionPointName", interceptionPointName));
	}

	/**
	 * 
	 */
	public void setClearOldAccessRights(final String clearOldAccessRights) throws JspException
	{
	    this.accessRight.put("clearOldAccessRights", evaluateString("AccessRightParameterTag", "clearOldAccessRights", clearOldAccessRights));
	}

}
