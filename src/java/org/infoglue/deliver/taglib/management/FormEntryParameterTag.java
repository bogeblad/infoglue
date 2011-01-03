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
package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.webservices.elements.RemoteAttachment;
import org.infoglue.deliver.taglib.AbstractTag;
import org.infoglue.deliver.taglib.content.ContentVersionParameterInterface;

public class FormEntryParameterTag extends AbstractTag implements ContentVersionParameterInterface
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The formEntryMap object
	 */
	private Map formEntryMap = new HashMap();
		
	/**
	 * Default constructor. 
	 */
	public FormEntryParameterTag()
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
		addFormEntryMap();
		
		formEntryMap = new HashMap();
		
		return EVAL_PAGE;
    }
	
	/**
	 * Adds the parameter to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addFormEntryMap() throws JspException
	{
		final RemoteFormServiceTag parent = (RemoteFormServiceTag) findAncestorWithClass(this, RemoteFormServiceTag.class);
		if(parent == null)
		{
			throw new JspTagException("FormEntryParameterTag must have a RemoteFormServiceTag ancestor.");
		}

		((RemoteFormServiceTag) parent).addFormEntryMap(formEntryMap);
	}

	/**
	 * Adds the parameter to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addFormEntryValue(String name, String value) throws JspException
	{
		Map formEntryValueMap = (Map)formEntryMap.get("formEntryValues");
		if(formEntryValueMap == null)
		{
			formEntryValueMap = new HashMap();
			formEntryMap.put("formEntryValues", formEntryValueMap);
		}
		formEntryValueMap.put(name, value);
	}

	/**
	 * Adds the content version attribute to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addDigitalAsset(RemoteAttachment remoteAttachment) throws JspException
	{
	    List digitalAssets = (List)this.formEntryMap.get("digitalAssets");
	    if(digitalAssets == null)
	    {
	        digitalAssets = new ArrayList();
	        this.formEntryMap.put("digitalAssets", digitalAssets);
	    }

	    digitalAssets.add(remoteAttachment);
	}
	
	public void addContentCategory(String contentCategory) throws JspException
	{
		// TODO Auto-generated method stub
	}

	public void addContentVersionAttribute(String name, String value) throws JspException
	{
	}

	/**
	 * Sets the name attribute.
	 * 
	 * @param name the name to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setFormName(final String formName) throws JspException
	{
		this.formEntryMap.put("formName", evaluateString("parameter", "formName", formName));
	}

	/**
	 * Sets the repositoryId attribute.
	 * 
	 * @param repositoryId the repositoryId the siteNode will belong to.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setFormContentId(final String formContentId) throws JspException
	{
	    this.formEntryMap.put("formContentId", evaluateInteger("parameter", "formContentId", formContentId));
	}

	/**
	 * Sets the parentSiteNodeId attribute.
	 * 
	 * @param repositoryId the parentSiteNodeId the siteNode the new siteNode will be placed under.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setOriginAddress(final String originAddress) throws JspException
	{
	    this.formEntryMap.put("originAddress", evaluateString("parameter", "originAddress", originAddress));
	}

	/**
	 * Sets the siteNodePath attribute. If the parentSiteNodeId is set the path is relative to that. Otherwise calculated from  root.
	 * 
	 * @param siteNodePath the path the new siteNode will be placed under.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setUserIP(final String userIP) throws JspException
	{
	    this.formEntryMap.put("userIP", evaluateString("parameter", "userIP", userIP));
	}

	/**
	 * Sets the siteNodeTypeDefinitionId attribute.
	 * 
	 * @param siteNodeTypeDefinitionId the siteNodeTypeDefinitionId the siteNode will be based on.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setUserAgent(final String userAgent) throws JspException
	{
	    this.formEntryMap.put("userAgent", evaluateString("parameter", "userAgent", userAgent));
	}

	public void clear()
	{
		formEntryMap.clear();
	}
}
