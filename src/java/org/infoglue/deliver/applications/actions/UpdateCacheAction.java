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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.ThreadMonitor;

import com.google.gson.Gson;


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

	private static VisualFormatter formatter = new VisualFormatter();

	private ThreadMonitor tk = null;

	private Integer publicationId = null;
	
	public void setPublicationId(Integer publicationId)
	{
		this.publicationId = publicationId;
	}
	
	/**
	 * The constructor for this action - contains nothing right now.
	 */
    
    public UpdateCacheAction() 
    {
	
    }
    
    /**
     * This method will allow for 3:rd party systems to send a publication message
     * through to all deliver instances. This enables the same 3:rd party integrations to 
     * register pages where it's used with a special key which can later be cleared. It also enables 
     * those "publications" to be traced and logged in the same manner as all the others. 
     */
         
    public String doPassThroughPublication() throws Exception
    {
    	String publisherName = getRequest().getParameter("publisherName");
    	if(publisherName == null || publisherName.equalsIgnoreCase(""))
    		publisherName = "SYSTEM";

    	String publicationName = getRequest().getParameter("publicationName");
    	if(publicationName == null || publicationName.equalsIgnoreCase(""))
    		publicationName = "3rd party pass through publication";
    	
    	String publicationDescription = getRequest().getParameter("publicationDescription");
    	if(publicationDescription == null || publicationDescription.equalsIgnoreCase(""))
    		publicationDescription = "No description given.";
    	publicationDescription = publicationDescription + " Originating host: " + getRequest().getRemoteHost();

    	String repositoryId = getRequest().getParameter("repositoryId");
    	if(repositoryId == null || repositoryId.equalsIgnoreCase(""))
    	{
    		repositoryId = "" + RepositoryController.getController().getFirstRepositoryVO().getId();
    	}
    	else
    	{
    		if(repositoryId.equalsIgnoreCase("InfoglueCalendar"))
    		{
    			for(RepositoryVO repoVO : (List<RepositoryVO>)RepositoryController.getController().getRepositoryVOList())
    			{
    				if(repoVO.getName().toLowerCase().indexOf("calendar") > -1)
    				{
    					repositoryId = "" + repoVO.getId();
    					break;
    				}
    			}
    		}
    		else
    		{
    			try
    			{
    				repositoryId = "" + RepositoryController.getController().getRepositoryVOWithId(Integer.parseInt(repositoryId)).getId();
    			}
    			catch (Exception e) {
    				logger.error("Wrong repository was sent from 3:rd party client to pass through publication:" + repositoryId + ". Defaulting to first repo.");
    				repositoryId = "" + RepositoryController.getController().getFirstRepositoryVO().getId();
				}
    		}
    	}
    	
    	String className = getRequest().getParameter("className");
    	String objectId = getRequest().getParameter("objectId");
    	String objectName = getRequest().getParameter("objectName");
    	String objectDescription = getRequest().getParameter("objectDescription");
    	if(objectDescription == null || objectDescription.equalsIgnoreCase(""))
    		objectDescription = "No description given.";
    	
		PublicationDetailVO publicationDetailVO = new PublicationDetailVO();
		publicationDetailVO.setCreationDateTime(DateHelper.getSecondPreciseDate());
		publicationDetailVO.setDescription(objectDescription);
		publicationDetailVO.setEntityClass(className);
		publicationDetailVO.setEntityId(new Integer(objectId));
		publicationDetailVO.setName("" + objectName);	
		publicationDetailVO.setTypeId(PublicationDetailVO.PUBLISH);
		publicationDetailVO.setCreator(publisherName);

		List<PublicationDetailVO> publicationDetailVOList = new ArrayList<PublicationDetailVO>();
		publicationDetailVOList.add(publicationDetailVO);
		
	    PublicationVO publicationVO = new PublicationVO();
	    publicationVO.setName(publicationName);
	    publicationVO.setDescription(publicationDescription);
	    publicationVO.setRepositoryId(new Integer(repositoryId));
	    publicationVO = PublicationController.getController().createAndPublish(publicationVO, publicationDetailVOList, publisherName);
	    
	    /*
    	NotificationMessage notificationMessage = new NotificationMessage("doClearPageCacheOnAllNodes:", className, "SYSTEM", NotificationMessage.PUBLISHING, objectId, ""+objectName);
	    RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
	    RemoteCacheUpdater.pushAndClearSystemNotificationMessages("SYSTEM");
		*/
	    
        this.getResponse().getWriter().println("cache clear instruction ok");
        return NONE;
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
	        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call UpdateCache!test.");

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
     * This method return status information about a certain publication. 
     * It should be able to inform us about if the publication was performed or if it's waiting or ongoing. 
     */
         
    public String doGetPublicationState() throws Exception
    {
        String operatingMode = CmsPropertyHandler.getOperatingMode();
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
        {
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call UpdateCache!getPublicationState.");

	            this.getResponse().setContentType("text/plain");
	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
	            
	            return NONE;
	        }
        }
    
        StringBuffer sb = new StringBuffer();
        List<CacheEvictionBean> latestPublications = RequestAnalyser.getRequestAnalyser().getLatestPublications();
        List<CacheEvictionBean> ongoingPublications = RequestAnalyser.getRequestAnalyser().getOngoingPublications();
        
        CacheEvictionBean foundPublishedBean = null;
        CacheEvictionBean foundOngoingPublicationBean = null;
        
        for(CacheEvictionBean latestPublication : latestPublications)
        {
        	if(latestPublication.getPublicationId().equals(this.publicationId))
        		foundPublishedBean = latestPublication;
        }

        for(CacheEvictionBean ongoingPublication : ongoingPublications)
        {
        	foundOngoingPublicationBean = ongoingPublication;
        }

        if(foundPublishedBean != null)
        	sb.append("" + foundPublishedBean.toQueryString());
        else if(foundOngoingPublicationBean != null)
        	sb.append("" + foundOngoingPublicationBean.toQueryString());
        else
        	sb.append("status=Unknown; serverStartDateTime:" + formatter.formatDate(CmsPropertyHandler.getStartupTime(), "yyyy-MM-dd HH:mm:ss"));
        
        this.getResponse().setContentType("text/plain");
        this.getResponse().getWriter().println("" + sb.toString());
        
        return NONE;
    }

    /**
     * This method return status information about a certain publication. It should be able to inform us about if the publication was performed or if it's waiting or ongoing. 
     */
         
    public String doGetOngoingPublications() throws Exception
    {
        String operatingMode = CmsPropertyHandler.getOperatingMode();
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
        {
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call UpdateCache!getOngoingPublications.");

	            this.getResponse().setContentType("text/plain");
	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
	            
	            return NONE;
	        }
        }
    
        List<CacheEvictionBean> ongoingPublications = RequestAnalyser.getRequestAnalyser().getOngoingPublications();
        
        Gson gson = new Gson();
        String json = gson.toJson(ongoingPublications);
        
        this.getResponse().setContentType("text/plain");
        this.getResponse().getWriter().println(json);
        
        return NONE;
    }

    /**
     * This method return status information about a certain publication. It should be able to inform us about if the publication was performed or if it's waiting or ongoing. 
     */
         
    public String doGetLatestPublications() throws Exception
    {
        String operatingMode = CmsPropertyHandler.getOperatingMode();
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
        {
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call UpdateCache!getLatestPublications.");

	            this.getResponse().setContentType("text/plain");
	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
	            
	            return NONE;
	        }
        }
    
        List<CacheEvictionBean> latestPublications = RequestAnalyser.getRequestAnalyser().getLatestPublications();
        
        Gson gson = new Gson();
        String json = gson.toJson(latestPublications);
        
        this.getResponse().setContentType("text/plain");
        this.getResponse().getWriter().println(json);
        
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
	        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call UpdateCache!testV3.");

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
    	logger.info("A cache update was received");
    	
    	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
    		tk = new ThreadMonitor(2000, this.getRequest(), "Update cache took to long", false);

    	logger.info("Update Cache starts..");
        String operatingMode = CmsPropertyHandler.getOperatingMode();
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
        {
	        long start = System.currentTimeMillis();
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") which is not allowed tried to call UpdateCache.action.");

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
		    
		    String userName 	= this.getRequest().getParameter(i + ".userName");
		    String timestamp 	= this.getRequest().getParameter(i + ".timestamp");
		    String className 	= this.getRequest().getParameter(i + ".className");
		    String typeId 	 	= this.getRequest().getParameter(i + ".typeId");
		    String objectId  	= this.getRequest().getParameter(i + ".objectId");
		    String objectName 	= this.getRequest().getParameter(i + ".objectName");
		    //A very special parameter only used in working environments. Notifies what has changes more precisly
		    Map<String,String> extraInfo 	= new HashMap<String,String>();
		    String changedAttributeNames 	= this.getRequest().getParameter(i + ".changedAttributeNames");
 		    String contentId 				= this.getRequest().getParameter(i + ".contentId");
		    String parentContentId 			= this.getRequest().getParameter(i + ".parentContentId");
		    String contentTypeDefinitionId	= this.getRequest().getParameter(i + ".contentTypeDefinitionId");
		    String contentIsProtected 		= this.getRequest().getParameter(i + ".contentIsProtected");
 		    String siteNodeId 				= this.getRequest().getParameter(i + ".siteNodeId");
		    String parentSiteNodeId		 	= this.getRequest().getParameter(i + ".parentSiteNodeId");
		    String repositoryId 			= this.getRequest().getParameter(i + ".repositoryId");
		    //System.out.println("contentId:" + contentId);
		    //System.out.println("parentContentId:" + parentContentId);
		    //System.out.println("siteNodeId:" + siteNodeId);
		    //System.out.println("parentSiteNodeId:" + parentSiteNodeId);
		    //System.out.println("repositoryId:" + repositoryId);
		    if(changedAttributeNames != null)
		    	extraInfo.put("changedAttributeNames", changedAttributeNames);
		    if(contentId != null)
		    	extraInfo.put("contentId", contentId);
		    if(parentContentId != null)
		    	extraInfo.put("parentContentId", parentContentId);
		    if(contentTypeDefinitionId != null)
		    	extraInfo.put("contentTypeDefinitionId", contentTypeDefinitionId);
		    if(contentIsProtected != null)
		    	extraInfo.put("contentIsProtected", contentIsProtected);
		    if(siteNodeId != null)
		    	extraInfo.put("siteNodeId", siteNodeId);
		    if(parentSiteNodeId != null)
		    	extraInfo.put("parentSiteNodeId", parentSiteNodeId);
		    if(repositoryId != null)
		    	extraInfo.put("repositoryId", repositoryId);
		    
		    while(className != null && !className.equals(""))
		    {
		    	logger.info("Cache update info:" + className + "/" + objectId);
			    Integer publicationId = -1;
			    if(className.indexOf(PublicationImpl.class.getName()) > -1)
			    	publicationId = Integer.parseInt(objectId);
			    
			    boolean skip = false;
			    if(timestamp != null && !timestamp.equals(""))
			    {
			    	try
			    	{
			    		long ts = Long.parseLong(timestamp);
			    		if(ts < CmsPropertyHandler.getStartupTime().getTime())
			    			skip = true;
			    	}
			    	catch (Exception e) 
			    	{
			    		logger.error("Could not read timestamp:" + timestamp);
					}
			    }
			    if(!skip)
			    {
			    	CacheEvictionBean cacheEvictionBean = new CacheEvictionBean(publicationId, userName, timestamp, className, typeId, objectId, objectName, extraInfo);
			    	newNotificationList.add(cacheEvictionBean);
			    	/*
			    	synchronized(CacheController.notifications)
			        {
					    CacheController.notifications.add(cacheEvictionBean);
			        }
			        */
				    logger.info("Added a cacheEvictionBean " + cacheEvictionBean.getClassName() + ":" + cacheEvictionBean.getTypeId() + ":" + cacheEvictionBean.getObjectName() + ":" + cacheEvictionBean.getObjectId());
			    }
			    else
				    logger.warn("Skipped a cacheEvictionBean as it's timestamp was earlier than the server start");
			    	
			    i++;
			    userName 	= this.getRequest().getParameter(i + ".userName");
			    timestamp 	= this.getRequest().getParameter(i + ".timestamp");
			    className 	= this.getRequest().getParameter(i + ".className");
			    typeId 	 	= this.getRequest().getParameter(i + ".typeId");
			    objectId  	= this.getRequest().getParameter(i + ".objectId");
			    objectName 	= this.getRequest().getParameter(i + ".objectName");

			    //A very special parameter only used in working environments. Notifies what has changes more precisly
			    extraInfo 				= new HashMap<String,String>();
			    changedAttributeNames 	= this.getRequest().getParameter(i + ".changedAttributeNames");
	 		    contentId 				= this.getRequest().getParameter(i + ".contentId");
			    parentContentId 		= this.getRequest().getParameter(i + ".parentContentId");
			    contentTypeDefinitionId	= this.getRequest().getParameter(i + ".contentTypeDefinitionId");
			    contentIsProtected 		= this.getRequest().getParameter(i + ".contentIsProtected");
	 		    siteNodeId 				= this.getRequest().getParameter(i + ".siteNodeId");
			    parentSiteNodeId		= this.getRequest().getParameter(i + ".parentSiteNodeId");
			    repositoryId 			= this.getRequest().getParameter(i + ".repositoryId");
//			    System.out.println("contentId:" + contentId);
//			    System.out.println("parentContentId:" + parentContentId);
//			    System.out.println("siteNodeId:" + siteNodeId);
//			    System.out.println("parentSiteNodeId:" + parentSiteNodeId);
//			    System.out.println("repositoryId:" + repositoryId);
			    if(changedAttributeNames != null)
			    	extraInfo.put("changedAttributeNames", changedAttributeNames);
			    if(contentId != null)
			    	extraInfo.put("contentId", contentId);
			    if(parentContentId != null)
			    	extraInfo.put("parentContentId", parentContentId);
			    if(contentTypeDefinitionId != null)
			    	extraInfo.put("contentTypeDefinitionId", contentTypeDefinitionId);
			    if(contentIsProtected != null)
			    	extraInfo.put("contentIsProtected", contentIsProtected);
			    if(siteNodeId != null)
			    	extraInfo.put("siteNodeId", siteNodeId);
			    if(parentSiteNodeId != null)
			    	extraInfo.put("parentSiteNodeId", parentSiteNodeId);
			    if(repositoryId != null)
			    	extraInfo.put("repositoryId", repositoryId);

		    }
		    
		    if(i == 0)
		    {
			    userName 	= this.getRequest().getParameter("userName");
			    timestamp 	= this.getRequest().getParameter("timestamp");
		    	className 	= this.getRequest().getParameter("className");
			    typeId 	 	= this.getRequest().getParameter("typeId");
			    objectId  	= this.getRequest().getParameter("objectId");
			    objectName 	= this.getRequest().getParameter("objectName");
			 
			    //A very special parameter only used in working environments. Notifies what has changes more precisly
			    extraInfo = new HashMap<String,String>();
			    changedAttributeNames 	= this.getRequest().getParameter("changedAttributeNames");
	 		    contentId 				= this.getRequest().getParameter("contentId");
			    parentContentId 		= this.getRequest().getParameter("parentContentId");
			    contentTypeDefinitionId	= this.getRequest().getParameter("contentTypeDefinitionId");
			    contentIsProtected 		= this.getRequest().getParameter("contentIsProtected");
	 		    siteNodeId 				= this.getRequest().getParameter("siteNodeId");
			    parentSiteNodeId 		= this.getRequest().getParameter("parentSiteNodeId");
			    repositoryId 			= this.getRequest().getParameter("repositoryId");
//			    System.out.println("contentId:" + contentId);
//			    System.out.println("parentContentId:" + parentContentId);
//			    System.out.println("siteNodeId:" + siteNodeId);
//			    System.out.println("parentSiteNodeId:" + parentSiteNodeId);
//			    System.out.println("repositoryId:" + repositoryId);
			    if(changedAttributeNames != null)
			    	extraInfo.put("changedAttributeNames", changedAttributeNames);
			    if(contentId != null)
			    	extraInfo.put("contentId", contentId);
			    if(parentContentId != null)
			    	extraInfo.put("parentContentId", parentContentId);
			    if(contentTypeDefinitionId != null)
			    	extraInfo.put("contentTypeDefinitionId", contentTypeDefinitionId);
			    if(contentIsProtected != null)
			    	extraInfo.put("contentIsProtected", contentIsProtected);
			    if(siteNodeId != null)
			    	extraInfo.put("siteNodeId", siteNodeId);
			    if(parentSiteNodeId != null)
			    	extraInfo.put("parentSiteNodeId", parentSiteNodeId);
			    if(repositoryId != null)
			    	extraInfo.put("repositoryId", repositoryId);

			    Integer publicationId = -1;
			    if(className.indexOf(PublicationImpl.class.getName()) > -1)
			    	publicationId = Integer.parseInt(objectId);

			    boolean skip = false;
			    if(timestamp != null && !timestamp.equals(""))
			    {
			    	try
			    	{
			    		long ts = Long.parseLong(timestamp);
			    		if(ts < CmsPropertyHandler.getStartupTime().getTime())
			    			skip = true;
			    	}
			    	catch (Exception e) 
			    	{
			    		logger.error("Could not read timestamp:" + timestamp);
					}
			    }
			    if(!skip)
			    {
				    CacheEvictionBean cacheEvictionBean = new CacheEvictionBean(publicationId, userName, timestamp, className, typeId, objectId, objectName, extraInfo);
				    newNotificationList.add(cacheEvictionBean);
				    /*
				    synchronized(CacheController.notifications)
			        {
				    	CacheController.notifications.add(cacheEvictionBean);
			        }
				    logger.warn("Added an oldSchool cacheEvictionBean " + cacheEvictionBean.getClassName() + ":" + cacheEvictionBean.getTypeId() + ":" + cacheEvictionBean.getObjectName() + ":" + cacheEvictionBean.getObjectId());
			        */
				    logger.info("Added a cacheEvictionBean " + cacheEvictionBean.getClassName() + ":" + cacheEvictionBean.getTypeId() + ":" + cacheEvictionBean.getObjectName() + ":" + cacheEvictionBean.getObjectId());
			    }
			    else
				    logger.warn("Skipped a cacheEvictionBean as it's timestamp was earlier than the server start");
   
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
			//new Thread(new Runnable() { public void run() {try {Thread.sleep(100); CacheController.evictWaitingCache(false);} catch (Exception e) {}}}).start();
			    
			logger.info("UpdateCache finished...");
		}
		catch(Exception e)
		{
			logger.error("Error in UpdateCache: " + e.getMessage(), e);
		}
		catch(Throwable t)
		{
		    logger.error("Error in UpdateCache: " + t.getMessage());
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
