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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;common:parameter&gt; tag, which adds a parameter
 * to the parameters of the parent tag.
 *
 *  Note! This tag must have a &lt;common:urlBuilder&gt; ancestor.
 */
public class ContentParameterTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The contentVO object
	 */
	private Map contentMap = new HashMap();
		
	/**
	 * Default constructor. 
	 */
	public ContentParameterTag()
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
		addContentMap();
		
		contentMap = new HashMap();
		
		return EVAL_PAGE;
    }
	
	/**
	 * Adds the parameter to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addContentMap() throws JspException
	{
		final RemoteContentServiceTag parent = (RemoteContentServiceTag) findAncestorWithClass(this, RemoteContentServiceTag.class);
		if(parent == null)
		{
			throw new JspTagException("ContentParameterTag must have a RemoteContentServiceTag ancestor.");
		}

		((RemoteContentServiceTag) parent).addContentMap(contentMap);
	}
	
	/**
	 * Sets the name attribute.
	 * 
	 * @param name the name to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setName(final String name) throws JspException
	{
		this.contentMap.put("name", evaluateString("parameter", "name", name));
	}

	/**
	 * Sets the repositoryId attribute.
	 * 
	 * @param repositoryId the repositoryId the content will belong to.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setRepositoryId(final String repositoryId) throws JspException
	{
	    this.contentMap.put("repositoryId", evaluateInteger("parameter", "repositoryId", repositoryId));
	}

	/**
	 * Sets the parentContentId attribute.
	 * 
	 * @param repositoryId the parentContentId the content the new content will be placed under.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setParentContentId(final String parentContentId) throws JspException
	{
	    this.contentMap.put("parentContentId", evaluateInteger("parameter", "parentContentId", parentContentId));
	}

	/**
	 * Sets the contentPath attribute. If the parentContentId is set the path is relative to that. Otherwise calculated from  root.
	 * 
	 * @param contentPath the path the new content will be placed under.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setContentPath(final String contentPath) throws JspException
	{
	    this.contentMap.put("contentPath", evaluateString("parameter", "contentPath", contentPath));
	}

	/**
	 * Sets the contentTypeDefinitionId attribute.
	 * 
	 * @param contentTypeDefinitionId the contentTypeDefinitionId the content will be based on.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setContentTypeDefinitionId(final String contentTypeDefinitionId) throws JspException
	{
	    this.contentMap.put("contentTypeDefinitionId", evaluateInteger("parameter", "contentTypeDefinitionId", contentTypeDefinitionId));
	}

	/**
	 * Sets the creatorName attribute.
	 */
	public void setCreatorName(final String creatorName) throws JspException
	{
	    this.contentMap.put("creatorName", evaluateString("parameter", "creatorName", creatorName));
	}

	/**
	 * Sets the isBranch attribute.
	 */
	public void setIsBranch(final String isBranch) throws JspException
	{
	    this.contentMap.put("isBranch", (Boolean)evaluate("parameter", "isBranch", isBranch, Boolean.class));
	}

	/**
	 * Sets the isProtected attribute.
	 */
	public void setIsProtected(final String isProtected) throws JspException
	{
	    this.contentMap.put("isProtected", evaluateInteger("parameter", "isProtected", isProtected));
	}

	/**
	 * Sets the contentTypeDefinitionId attribute.
	 */
	public void setExpireDateTime(final String expireDateTime) throws JspException
	{
	    this.contentMap.put("expireDateTime", (Date)evaluate("parameter", "expireDateTime", expireDateTime, Date.class));
	}

	/**
	 * Sets the contentTypeDefinitionId attribute.
	 */
	public void setPublishDateTime(final String publishDateTime) throws JspException
	{
	    this.contentMap.put("publishDateTime", (Date)evaluate("parameter", "publishDateTime", publishDateTime, Date.class));
	}

	/**
	 * Add the contentVersion the child tag generated to the list of contentVersions that are to be persisted.
	 */
	public void addContentVersion(final Map contentVersion) 
	{
	    List contentVersions = (List)this.contentMap.get("contentVersions");
	    if(contentVersions == null)
	    {
	        contentVersions = new ArrayList();
	        this.contentMap.put("contentVersions", contentVersions);
	    }

	    contentVersions.add(contentVersion);
	}

	/**
	 * Add the accessRight the child tag generated to the list of accessRights that are to be persisted.
	 */
	public void addAccessRight(final Map accessRight) 
	{
	    List accessRights = (List)this.contentMap.get("accessRights");
	    if(accessRights == null)
	    {
	    	accessRights = new ArrayList();
	        this.contentMap.put("accessRights", accessRights);
	    }

	    accessRights.add(accessRight);
	}

	public void clear()
	{
	    contentMap.clear();
	}
}
