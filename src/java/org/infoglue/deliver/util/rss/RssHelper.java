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
package org.infoglue.deliver.util.rss;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author mattias
 *
 * This is a helper class to interact with ROME - the sun sponsored RSS parser/generator.
 */

public class RssHelper
{
    private final static Logger logger = Logger.getLogger(RssHelper.class.getName());

    //The default error message
    private static final String COULD_NOT_GENERATE_FEED_ERROR = "Could not generate feed";

    //Lets use the iso date format
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private String defaultFeedType = "atom_0.3";

    /**
     * The method that prints the xml and returns it as a string.
     * 
     * @return
     */
    
    public String render(SyndFeed feed)
    {     
        String output = null;
        
        try 
        {
            //res.setContentType(MIME_TYPE);
            SyndFeedOutput out = new SyndFeedOutput();
            output = out.outputString(feed);
        }
        catch (FeedException fe) 
        {
            String msg = COULD_NOT_GENERATE_FEED_ERROR;
            logger.error(msg, fe);
            //res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,msg);
        }
        
        return output;
    }
    /**
     * This method returns a new SyndFeed instansiated with the parameters send in and with an empty entry-list.
     * 
     * @param title
     * @param link
     * @param description
     * @return
     * @throws IOException
     * @throws FeedException
     */
    
    public SyndFeed getFeed(String feedType, String title, String link, String description, String encoding) throws IOException,FeedException 
    {
        
        SyndFeed feed = new SyndFeedImpl();

        feedType = (feedType!=null) ? feedType : defaultFeedType;
        feed.setFeedType(feedType);

        feed.setTitle(title);
        feed.setLink(link);
        feed.setDescription(description);
        feed.setEncoding(encoding);

        List entries = new ArrayList();
        
        feed.setEntries(entries);

        return feed;
    } 

    /**
     * This method adds an entry to a feed. No magic.
     * 
     * @param feed
     * @param title
     * @param link
     * @param publishedDate
     * @param description
     * @param descriptionContentType
     * @throws IOException
     * @throws FeedException
     */
    
    public void addEntry(SyndFeed feed, String title, String link, Date publishedDate, String description, String descriptionContentType) throws IOException,FeedException 
    {
        DateFormat dateParser = new SimpleDateFormat(DATE_FORMAT);

        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(title);
        entry.setLink(link);
        entry.setPublishedDate(publishedDate);
        
        SyndContent syndContent = new SyndContentImpl();
        syndContent.setType(descriptionContentType);
        syndContent.setValue(description);
        
        entry.setDescription(syndContent);
        
        feed.getEntries().add(entry); 
    } 

}
