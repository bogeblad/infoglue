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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.TemplateControllerTag;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

/**
 * This tag will generate a SyndEntry item to be used in a syndFeed.  
 */

public class RSSFeedEntryTag extends TemplateControllerTag 
{
    private final static Logger logger = Logger.getLogger(RSSFeedEntryTag.class.getName());

    /**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	private String title 		= null;
	private String link 		= null;
	private Date publishedDate	= null;
	private String description	= null;
	private List categories		= new ArrayList();
	private String descriptionContentType = "text/html";
	private boolean correctDoubleAmpEncoding	= true;
	
	/**
	 * Default constructor.
	 */
	public RSSFeedEntryTag() 
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
	 * Process the end tag. Sets a cookie.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
	    try
	    {		    	
	    	if(correctDoubleAmpEncoding)
	    		link = link.replaceAll("&amp;", "&");
	    	
	        SyndEntry entry = new SyndEntryImpl();
	        entry.setTitle(title);
	        entry.setLink(link);
	        entry.setPublishedDate(publishedDate);

	        SyndContent syndContent = new SyndContentImpl();
	        syndContent.setType(descriptionContentType);
	        syndContent.setValue(description);
	     
	        entry.setDescription(syndContent);
	        
	        entry.setCategories(categories);

		    addEntry(entry);
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
	protected void addEntry(SyndEntry entry) throws JspException
	{
		final RSSFeedTag parent = (RSSFeedTag) findAncestorWithClass(this, RSSFeedTag.class);
		if(parent == null)
		{
			throw new JspTagException("RSSFeedEntryTag must have a RSSFeedTag ancestor.");
		}

		((RSSFeedTag) parent).addFeedEntry(entry);
	}

	
    public void setDescription(String description) throws JspException
    {
        this.description = evaluateString("RssFeedEntry", "description", description);
    }
    
    public void setLink(String link) throws JspException
    {
        this.link = evaluateString("RssFeedEntry", "link", link);
    }
    
    public void setCorrectDoubleAmpEncoding(boolean correctDoubleAmpEncoding) throws JspException
    {
        this.correctDoubleAmpEncoding = correctDoubleAmpEncoding;
    }

    public void setTitle(String title) throws JspException
    {
    	this.title = evaluateString("RssFeedEntry", "title", title);
    }
    
    public void setDescriptionContentType(String descriptionContentType) throws JspException
    {
        this.descriptionContentType = evaluateString("RssFeedEntry", "descriptionContentType", descriptionContentType);
    }
    
    public void setPublishedDate(String publishedDate) throws JspException
    {
        this.publishedDate = (Date)evaluate("RssFeedEntry", "publishedDate", publishedDate, Date.class);
    }
        
    public void addEntryCategory(SyndCategory category)
    {    	
    	this.categories.add(category);
    }
}
