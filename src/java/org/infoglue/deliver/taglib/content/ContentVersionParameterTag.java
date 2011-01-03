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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.webservices.elements.RemoteAttachment;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;common:parameter&gt; tag, which adds a parameter
 * to the parameters of the parent tag.
 *
 *  Note! This tag must have a &lt;common:urlBuilder&gt; ancestor.
 */
public class ContentVersionParameterTag extends AbstractTag implements ContentVersionParameterInterface 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The content version object.
	 */
	private Map contentVersion = new HashMap();
	
	
	/**
	 * Default constructor. 
	 */
	public ContentVersionParameterTag()
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
		addContentVersion();
		
		contentVersion = new HashMap();
		
		return EVAL_PAGE;
    }
	
	/**
	 * Adds the content version to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addContentVersion() throws JspException
	{
		final ContentParameterTag parent = (ContentParameterTag) findAncestorWithClass(this, ContentParameterTag.class);
		if(parent == null)
		{
			throw new JspTagException("ContentVersionParameterTag must have a ContentParameterTag ancestor.");
		}
		((ContentParameterTag) parent).addContentVersion(contentVersion);
	}

	/**
	 * Adds the content version attribute to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addContentVersionAttribute(String name, String value) throws JspException
	{
	    Map contentVersionAttributes = (Map)this.contentVersion.get("contentVersionAttributes");
	    if(contentVersionAttributes == null)
	    {
	        contentVersionAttributes = new HashMap();
	        this.contentVersion.put("contentVersionAttributes", contentVersionAttributes);
	    }

	    contentVersionAttributes.put(name, value);
	}

	/**
	 * Adds the content version attribute to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addDigitalAsset(RemoteAttachment remoteAttachment) throws JspException
	{
	    List digitalAssets = (List)this.contentVersion.get("digitalAssets");
	    if(digitalAssets == null)
	    {
	        digitalAssets = new ArrayList();
	        this.contentVersion.put("digitalAssets", digitalAssets);
	    }

	    digitalAssets.add(remoteAttachment);
	}

	/**
	 * Adds the content category to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addContentCategory(String contentCategory) throws JspException
	{
	    List contentCategories = (List)this.contentVersion.get("contentCategories");
	    if(contentCategories == null)
	    {
	    	contentCategories = new ArrayList();
	        this.contentVersion.put("contentCategories", contentCategories);
	    }

	    contentCategories.add(contentCategory);
	}
	
	/**
	 * 
	 */
	public void setLanguageId(final String languageId) throws JspException
	{
	    this.contentVersion.put("languageId", evaluateInteger("remoteContentService", "languageId", languageId));
	}

	/**
	 * 
	 */
	public void setStateId(final String stateId) throws JspException
	{
	    this.contentVersion.put("stateId", evaluateInteger("remoteContentService", "stateId", stateId));
	}

	/**
	 * 
	 */
	public void setAllowHTMLContent(final String allowHTMLContent) throws JspException
	{
	    this.contentVersion.put("allowHTMLContent", evaluate("remoteContentService", "allowHTMLContent", allowHTMLContent, Boolean.class));
	}

	/**
	 * 
	 */
	public void setAllowExternalLinks(final String allowExternalLinks) throws JspException
	{
	    this.contentVersion.put("allowExternalLinks", evaluate("remoteContentService", "allowExternalLinks", allowExternalLinks, Boolean.class));
	}

	/**
	 * 
	 */
	public void setAllowDollarSigns(final String allowDollarSigns) throws JspException
	{
	    this.contentVersion.put("allowDollarSigns", evaluate("remoteContentService", "allowDollarSigns", allowDollarSigns, Boolean.class));
	}

	/**
	 * 
	 */
	public void setAllowAnchorSigns(final String allowAnchorSigns) throws JspException
	{
	    this.contentVersion.put("allowAnchorSigns", evaluate("remoteContentService", "allowAnchorSigns", allowAnchorSigns, Boolean.class));
	}

}
