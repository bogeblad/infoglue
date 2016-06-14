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

package org.infoglue.cms.applications.publishingtool.actions;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.util.CmsPropertyHandler;

public class PreviewEditionAction extends ViewPublicationsAction
{        
	private List contentVersionsToPublish  = new ArrayList();
	private List siteNodeVersionsToPublish = new ArrayList();
	
    public String doExecute() throws Exception
    {
    	try
    	{
			String[] events = getRequest().getParameterValues("eventId");
	    	
	    	for(int i=0; i<events.length; i++)
	    	{
	    		EventVO event = EventController.getEventVOWithId(new Integer(events[i]));
				if(event.getEntityClass().equalsIgnoreCase(ContentVersion.class.getName()))
					contentVersionsToPublish.add(ContentVersionController.getContentVersionController().getContentVersionVOWithId(event.getEntityId()));
	    		else
					siteNodeVersionsToPublish.add(SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(event.getEntityId()));    			
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();    		
    	}
    	    	
        return "success";
    }
    
    public String getPreviewUrl()
    {
    	return CmsPropertyHandler.getPreviewDeliveryUrl();
    }
    
    public List getContentVersionsToPublish()
    {
    	return contentVersionsToPublish;
    }  

    public List getSiteNodeVersionsToPublish()
    {
    	return siteNodeVersionsToPublish;
    }  
}
