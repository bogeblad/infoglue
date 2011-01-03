/* ===============================================================================
*
* Part of the InfoGlue SiteNode Management Platform (www.infoglue.org)
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

import java.util.ArrayList;
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
public class SiteNodeParameterTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The siteNodeVO object
	 */
	private Map siteNodeMap = new HashMap();
		
	/**
	 * Default constructor. 
	 */
	public SiteNodeParameterTag()
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
		addSiteNodeMap();
		
		siteNodeMap = new HashMap();
		
		return EVAL_PAGE;
    }
	
	/**
	 * Adds the parameter to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addSiteNodeMap() throws JspException
	{
		final RemoteSiteNodeServiceTag parent = (RemoteSiteNodeServiceTag) findAncestorWithClass(this, RemoteSiteNodeServiceTag.class);
		if(parent == null)
		{
			throw new JspTagException("SiteNodeParameterTag must have a RemoteSiteNodeServiceTag ancestor.");
		}

		((RemoteSiteNodeServiceTag) parent).addSiteNodeMap(siteNodeMap);
	}
	
	/**
	 * Sets the name attribute.
	 * 
	 * @param name the name to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setName(final String name) throws JspException
	{
		this.siteNodeMap.put("name", evaluateString("parameter", "name", name));
	}

	/**
	 * Sets the repositoryId attribute.
	 * 
	 * @param repositoryId the repositoryId the siteNode will belong to.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setRepositoryId(final String repositoryId) throws JspException
	{
	    this.siteNodeMap.put("repositoryId", evaluateInteger("parameter", "repositoryId", repositoryId));
	}

	/**
	 * Sets the parentSiteNodeId attribute.
	 * 
	 * @param repositoryId the parentSiteNodeId the siteNode the new siteNode will be placed under.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setParentSiteNodeId(final String parentSiteNodeId) throws JspException
	{
	    this.siteNodeMap.put("parentSiteNodeId", evaluateInteger("parameter", "parentSiteNodeId", parentSiteNodeId));
	}

	/**
	 * Sets the siteNodePath attribute. If the parentSiteNodeId is set the path is relative to that. Otherwise calculated from  root.
	 * 
	 * @param siteNodePath the path the new siteNode will be placed under.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setSiteNodePath(final String siteNodePath) throws JspException
	{
	    this.siteNodeMap.put("siteNodePath", evaluateString("parameter", "siteNodePath", siteNodePath));
	}

	/**
	 * Sets the siteNodeTypeDefinitionId attribute.
	 * 
	 * @param siteNodeTypeDefinitionId the siteNodeTypeDefinitionId the siteNode will be based on.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setSiteNodeTypeDefinitionId(final String siteNodeTypeDefinitionId) throws JspException
	{
	    this.siteNodeMap.put("siteNodeTypeDefinitionId", evaluateInteger("parameter", "siteNodeTypeDefinitionId", siteNodeTypeDefinitionId));
	}

	/**
	 * Sets the pageTemplateContentId attribute.
	 * 
	 * @param pageTemplateContentId the pageTemplateContentId the siteNode will be based on.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setPageTemplateContentId(final String pageTemplateContentId) throws JspException
	{
	    this.siteNodeMap.put("pageTemplateContentId", evaluateInteger("parameter", "pageTemplateContentId", pageTemplateContentId));
	}

	public void clear()
	{
	    siteNodeMap.clear();
	}
}
