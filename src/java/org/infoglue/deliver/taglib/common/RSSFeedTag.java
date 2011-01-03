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
import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.rss.RssHelper;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * This tag will get a cookie value  
 */

public class RSSFeedTag extends TemplateControllerTag 
{
    private final static Logger logger = Logger.getLogger(RSSFeedTag.class.getName());

    /**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	private String feedType 	= null;
	private String title 		= null;
	private String link 		= null;
	private String description 	= null;
	private String encoding 	= "UTF-8";
	
	private List entries 		= new ArrayList(); 
	
	/**
	 * Default constructor.
	 */
	public RSSFeedTag() 
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
		    RssHelper rssHelper = new RssHelper();
		    SyndFeed feed = rssHelper.getFeed(this.feedType, this.title, this.link, this.description, this.encoding);

		    feed.setEntries(entries);
		    
		    String rss = rssHelper.render(feed);
		    setResultAttribute(rss);
	    }
	    catch(Exception e)
	    {
	        logger.error("An error occurred when generating RSS-feed:" + e.getMessage(), e);
	    }
	    finally
	    {
	        entries = new ArrayList();
	    }
	    
        return EVAL_PAGE;
    }

    public void setFeedType(String feedType) throws JspException
    {
        this.feedType = evaluateString("RssFeed", "feedType", feedType);;
    }
    
    public void setDescription(String description) throws JspException
    {
        this.description = evaluateString("RssFeed", "description", description);
    }
    
    public void setLink(String link) throws JspException
    {
        this.link = evaluateString("RssFeed", "link", link);
    }
    
    public void setTitle(String title) throws JspException
    {
        this.title = evaluateString("RssFeed", "title", title);
    }
    
	/**
	 * Add syndentry to the list of entries that are to be rendered.
	 */
	public void addFeedEntry(final SyndEntry entry) 
	{
	    this.entries.add(entry);
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}
}
