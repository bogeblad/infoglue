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

package org.infoglue.deliver.util;

import org.apache.log4j.Logger;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;


public class RequestCentricCachePopulator
{ 
    public final static Logger logger = Logger.getLogger(RequestCentricCachePopulator.class.getName());

	/**
	 * This method simulates a call to a page so all castor caches fills up before we throw the old page cache.
	 * @param db
	 * @param siteNodeId
	 * @param languageId
	 * @param contentId
	 */
	
	public void recache(DatabaseWrapper dbWrapper, Integer siteNodeId) throws SystemException, Exception
	{
        logger.info("recache starting..");
        /*
        String name = "org.infoglue.deliver.applications.actions.ViewPageAction";
        Map parameters = new HashMap();
        parameters.put("siteNodeId", siteNodeId);
        parameters.put("isRecacheCall", "true");

        FakeHttpServletRequest request = new FakeHttpServletRequest();
        request.setServletContext(DeliverContextListener.getServletContext());
        FakeHttpServletResponse response = new FakeHttpServletResponse();
        
        HttpServletRequest portalServletRequest = new PortalServletRequest((HttpServletRequest) request);
        
        ActionContext.setName(name);
        ActionContext.setParameters(Collections.unmodifiableMap(parameters));
        ActionContext.setRequest(portalServletRequest);
        //ActionContext.setRequest(request);
        ActionContext.setResponse(response);
        
        ViewPageAction action = (ViewPageAction)ActionFactory.getAction(name);
        action.setServletRequest(portalServletRequest);
        action.setRecacheCall(true);
        //action.setServletRequest(request);
        action.setServletResponse(response);

        String result = null;
        try
        {
           result = (String)action.execute();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        } 
        */
        HttpHelper helper = new HttpHelper();
        String recacheUrl = CmsPropertyHandler.getRecacheUrl() + "?siteNodeId=" + siteNodeId + "&refresh=true&isRecacheCall=true";
        logger.info("recacheUrl:" + recacheUrl);
        String response = helper.getUrlContent(recacheUrl, 30000);
        
        logger.info("recache stopped..");
	}
	
}