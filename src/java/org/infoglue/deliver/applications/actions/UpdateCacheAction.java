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


package org.infoglue.deliver.applications.actions;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.ThreadMonitor;


/**
 * This is the action that takes care of all incoming update-calls. This action is
 * called by either the system or by replication-program and the class the distibutes the 
 * update-call to all the listeners which have registered earlier.
 *
 * @author Mattias Bogeblad
 */

public class UpdateCacheAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(UpdateCacheAction.class.getName());

	/*
	private String className = null;
	private String objectId = null;
	private String objectName = null;
	private String typeId = null;
	*/
	
	private String repositoryName = null;
	private Integer languageId    = null;
	private Integer siteNodeId    = null;
	
	private static boolean cachingInProgress = false;
	
	private ThreadMonitor tk = null;

	/**
	 * The constructor for this action - contains nothing right now.
	 */
    
    public UpdateCacheAction() 
    {
	
    }
    
    /**
     * This method will just reply to a testcall. 
     */
         
    public String doTest() throws Exception
    {
        String operatingMode = CmsPropertyHandler.getOperatingMode();
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
        {
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	logger.error("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call doReCache.");

	            this.getResponse().setContentType("text/plain");
	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
	            
	            return NONE;
	        }
        }
    
    	this.getResponse().setContentType("text/plain");
        this.getResponse().getWriter().println("test ok - cache action available");
        
        return NONE;
    }

    /**
     * This method will just reply to a testcall. 
     */
         
    public String doTestV3() throws Exception
    {
        String operatingMode = CmsPropertyHandler.getOperatingMode();
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
        {
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	logger.error("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call doReCache.");

	            this.getResponse().setContentType("text/plain");
	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
	            
	            return NONE;
	        }
        }
        
        this.getResponse().setContentType("text/html");
        this.getResponse().getWriter().println("<html><body>test ok - cache action available</body></html>");
        
        return NONE;
    }

    /**
     * This method is the application entry-point. The parameters has been set through the setters
     * and now we just have to render the appropriate output. 
     */
         
    public String doExecute() throws Exception
    {
    	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
    		tk = new ThreadMonitor(2000, this.getRequest(), "Update cache took to long", false);

    	logger.info("Update Cache starts..");
        String operatingMode = CmsPropertyHandler.getOperatingMode();
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
        {
	        long start = System.currentTimeMillis();
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	logger.error("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call doReCache.");

	            this.getResponse().setContentType("text/plain");
	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
	            
	            return NONE;
	        }
	        
        }
        
		try
		{  
			//Iterate through all registered listeners and call them... dont place logic here... have specialized handlers.			

			//logger.info("className:" + className);
			//logger.info("objectId:" + objectId);
			List newNotificationList = new ArrayList();
			
		    int i = 0;
		    
		    String className 	= this.getRequest().getParameter(i + ".className");
		    String typeId 	 	= this.getRequest().getParameter(i + ".typeId");
		    String objectId  	= this.getRequest().getParameter(i + ".objectId");
		    String objectName 	= this.getRequest().getParameter(i + ".objectName");
		    while(className != null && !className.equals(""))
		    {
		    	logger.info("className:" + className);
			    logger.info("objectId:" + objectId);
			    
		    	CacheEvictionBean cacheEvictionBean = new CacheEvictionBean(className, typeId, objectId, objectName);
		    	newNotificationList.add(cacheEvictionBean);
		    	/*
		    	synchronized(CacheController.notifications)
		        {
				    CacheController.notifications.add(cacheEvictionBean);
		        }
		        */
			    logger.info("Added a cacheEvictionBean " + cacheEvictionBean.getClassName() + ":" + cacheEvictionBean.getTypeId() + ":" + cacheEvictionBean.getObjectName() + ":" + cacheEvictionBean.getObjectId());
			    
			    i++;
			    className 	= this.getRequest().getParameter(i + ".className");
			    typeId 	 	= this.getRequest().getParameter(i + ".typeId");
			    objectId  	= this.getRequest().getParameter(i + ".objectId");
			    objectName 	= this.getRequest().getParameter(i + ".objectName");
		    }
		    
		    if(i == 0)
		    {
		    	className 	= this.getRequest().getParameter("className");
			    typeId 	 	= this.getRequest().getParameter("typeId");
			    objectId  	= this.getRequest().getParameter("objectId");
			    objectName 	= this.getRequest().getParameter("objectName");
			    CacheEvictionBean cacheEvictionBean = new CacheEvictionBean(className, typeId, objectId, objectName);
			    newNotificationList.add(cacheEvictionBean);
			    /*
			    synchronized(CacheController.notifications)
		        {
			    	CacheController.notifications.add(cacheEvictionBean);
		        }
			    logger.warn("Added an oldSchool cacheEvictionBean " + cacheEvictionBean.getClassName() + ":" + cacheEvictionBean.getTypeId() + ":" + cacheEvictionBean.getObjectName() + ":" + cacheEvictionBean.getObjectId());
		        */
			    
		    }
		    
		    /*
            //TODO - place check here maybe??
            synchronized(RequestAnalyser.getRequestAnalyser()) 
    	    {
    	       	if(RequestAnalyser.getRequestAnalyser().getBlockRequests())
    		    {
    			    logger.warn("evictWaitingCache allready in progress - returning to avoid conflict");
    		        return;
    		    }

    	       	RequestAnalyser.getRequestAnalyser().setBlockRequests(true);
    		}
    		*/
		    
		    //synchronized(this)
		    //{
			    synchronized(CacheController.notifications)
		        {
			    	CacheController.notifications.addAll(newNotificationList);
		        }
			//}
		    
			logger.info("UpdateCache finished...");
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		catch(Throwable t)
		{
		    logger.error(t.getMessage());
		}
                
		//this.getHttpSession().invalidate();
    	logger.info("Update Cache stops..");

    	if(tk != null)
    		tk.done();

        return NONE;
    }
    
    
	/**
	 * Setters and getters for all things sent to the page in the request
	 */
	/*        
    public void setClassName(String className)
    {
	    this.className = className;
    }
        
    public void setObjectId(String objectId)
    {
	    this.objectId = objectId;
    }

    public void setObjectName(String objectName)
    {
	    this.objectName = objectName;
    }

    public void setTypeId(String typeId)
    {
	    this.typeId = typeId;
    }
    */
}
