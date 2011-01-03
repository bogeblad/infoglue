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
package org.infoglue.deliver.taglib.common;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.TemplateControllerTag;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;

/**
 * This tag will generate a SyndEntryCategory item to be used in a syndFeed.  
 */

public class RSSFeedEntryCategoryTag extends TemplateControllerTag 
{
    private final static Logger logger = Logger.getLogger(RSSFeedEntryTag.class.getName());

    /**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	private String taxonomyUri	= "";
	private String name 		= null;
	
	/**
	 * Default constructor.
	 */
	public RSSFeedEntryCategoryTag() 
	{
		super();
	}
	

	/**
	 * Process the end tag. Sets a cookie.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
	    try
	    {			    	
	    	SyndCategory category = new SyndCategoryImpl();
	    	category.setTaxonomyUri(taxonomyUri);
	    	category.setName(name);		    
		    addCategory(category);
	    }
	    catch(Exception e)
	    {
	        logger.error("An error occurred when generating RSS-feed:" + e.getMessage(), e);
	    }
	    
        return EVAL_PAGE;
    }

	/**
	 * Adds the parameter to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	protected void addCategory(SyndCategory category) throws JspException
	{
		final RSSFeedEntryTag parent = (RSSFeedEntryTag) findAncestorWithClass(this, RSSFeedEntryTag.class);
		if(parent == null)
		{
			throw new JspTagException("RSSFeedEntryCategoryTag must have a RSSFeedEntryTag ancestor.");
		}
		((RSSFeedEntryTag)parent).addEntryCategory(category);
	}

	
    public void setTaxonomyUri(String taxonomyUri) throws JspException
    {
        this.taxonomyUri = evaluateString("RssFeedCategoryEntry", "taxonomyUri", taxonomyUri);
    }
    
    public void setName(String name) throws JspException
    {
        this.name = evaluateString("RssFeedCategoryEntry", "name", name);
    }
}

