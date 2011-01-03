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

package org.infoglue.deliver.taglib.page;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.PublicationThread;
import org.infoglue.deliver.util.RequestAnalyser;

/**
 * Tag for programatically clearing the deliver caches. Very useful when working with intranets.
 */

public class ClearCacheTag extends TemplateControllerTag
{
    private final static Logger logger = Logger.getLogger(ClearCacheTag.class.getName());

	private static final long serialVersionUID = 3905242346756059449L;

	private String entity = null;
	private String entityId = null;
	
    public ClearCacheTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		try
		{	
			CacheController.clearCache(Class.forName(entity), new Object[]{entityId}, true);
		    CacheController.clearCaches(entity, entityId, null, true);
		    //CacheController.clearCaches(null, null, null, true);

			//CacheUpdateThread cacheUpdateThread = new CacheUpdateThread();
		    //cacheUpdateThread.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
        return EVAL_PAGE;
    }	
	
	public void setEntity(String entity) throws JspException
	{
		this.entity = evaluateString("clearCache", "entity", entity);
	}

	public void setEntityId(String entityId) throws JspException
	{
	    this.entityId = evaluateString("clearCache", "entityId", entityId);;
	}

	
	class CacheUpdateThread extends Thread
	{
		public synchronized void run() 
		{
			try
			{
				sleep(50);
				CacheController.clearCache(Class.forName(entity), new Object[]{entityId}, true);
			    CacheController.clearCaches(entity, entityId, null, true);
			} 
			catch (Exception e)
			{
			    logger.error("An error occurred in the PublicationThread:" + e.getMessage());
			}
		}
	}
}

