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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.PageDeliveryMetaDataController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataEntityVO;
import org.infoglue.cms.entities.management.PageDeliveryMetaDataVO;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.NoBaseTemplateFoundException;
import org.infoglue.cms.exception.PageNotFoundException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.AuthorizationModule;
import org.infoglue.cms.security.InfoGlueBasicAuthorizationModule;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DesEncryptionHelper;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.EditOnSiteBasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.invokers.PageInvoker;
import org.infoglue.deliver.portal.PortalService;
import org.infoglue.deliver.util.BrowserBean;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.HttpHelper;
import org.infoglue.deliver.util.HttpUtilities;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.ThreadMonitor;
import org.infoglue.deliver.util.Timer;

import webwork.action.ActionContext;


/**
 * This is the main delivery action. Gets called when the user clicks on a link that goes inside the site.
 *
 * @author Mattias Bogeblad
 */

public class ViewPageAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;

    public final static Logger logger = Logger.getLogger(ViewPageAction.class.getName());

	private static final SimpleDateFormat HTTP_DATE_FORMAT = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH );

	//These are the standard parameters which uniquely defines which page to show.
	private Integer siteNodeId = null;
	private Integer contentId  = null; 
	private Integer languageId = null;
	private Integer repositoryId = null;
	
	private boolean showSimple = false;
	
	//This parameter are set if you want to access a certain repository startpage
	private String repositoryName = null;
	
	//A cached nodeDeliveryController
	protected NodeDeliveryController nodeDeliveryController					= null;
	protected IntegrationDeliveryController integrationDeliveryController 	= null;
	protected TemplateController templateController 						= null;
		
	private static final boolean USE_LANGUAGE_FALLBACK        			= true;
	private static final boolean DO_NOT_USE_LANGUAGE_FALLBACK 			= false;
	
	//The browserbean
	private BrowserBean browserBean = null;
	private Principal principal = null;
		
	//A possibility to set the referer address
	private String referer = null;

	private boolean isRecacheCall = false;
	
	//For statistics only and debug
	public static long contentVersionTime = 0;
	public static long serviceBindingTime = 0;
	public static long contentAttributeTime = 0;
	public static long boundContentTime = 0;
	public static long inheritedServiceBindingTime = 0;
	public static long selectMatchingEntitiesTime = 0;
	public static long isValidTime = 0;
	public static long qualifyersTime = 0;
	public static long sortQualifyersTime = 0;
	public static long commitTime = 0;
	public static long rollbackTime = 0;
	public static long closeTime = 0;
	public static long lastRequestProcessingTime = 0;
	private static Thread lastThread = null;
	private static boolean memoryWarningSent = false;
	
	private static Random random = new Random();

	
	/**
	 * The constructor for this action - contains nothing right now.
	 */
    
    public ViewPageAction() 
    {
    }
    
    /**
     * This method is the application entry-point. The parameters has been set through the setters
     * and now we just have to render the appropriate output. 
     */
         
    public String doExecute() throws Exception
    {
    	Timer pageTimer = new Timer();
    	
        if(isRecacheCall)
        {
	        //logger.warn("ThreadId:" + Thread.currentThread().getName());
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        }
                    	    	
        //TODO - Can this be removed perhaps
        while(CmsPropertyHandler.getActuallyBlockOnBlockRequests() && RequestAnalyser.getRequestAnalyser().getBlockRequests())
        {
        	//logger.info("Queing up requests as cache eviction are taking place..");
        	Thread.sleep(10);
        }
        
        try
        {
	        Integer maxActiveRequests = new Integer(CmsPropertyHandler.getMaxActiveRequests());
	        Integer maxRequestTime = new Integer(CmsPropertyHandler.getMaxRequestTime());
        	//logger.info("maxActiveRequests:" + maxActiveRequests + "-" + maxRequestTime);

	    	while(CmsPropertyHandler.getUseHighLoadLimiter().equalsIgnoreCase("true") && RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() > maxActiveRequests.intValue() && (lastRequestProcessingTime > maxRequestTime.intValue() || maxRequestTime.intValue() < 1))
	    	{
	        	if(logger.isInfoEnabled())
	        		logger.info("Queing up...:" + RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() + "(" + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + ") - " + lastRequestProcessingTime);
	        	
	            int sleepTime = random.nextInt(300);
	            //logger.info("Queing up...:" + RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests() + "(" + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests() + ") - " + lastRequestProcessingTime + " for " + sleepTime + " ms");
	            
	        	Thread.sleep(sleepTime);
	    	}
        }
        catch(Exception e)
        {
        	logger.error("You have faulty settings in either maxActiveRequests or maxRequestTime - fix this as it affects performance:" + e.getMessage(), e);
        }
        
        if(logger.isInfoEnabled())
        {
	        logger.info("************************************************");
	    	logger.info("* ViewPageAction was called....                *");
	    	logger.info("************************************************");
        }
        
        HttpServletRequest request = getRequest();

        ThreadMonitor tk = null;
    	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
    		tk = new ThreadMonitor(new Long(CmsPropertyHandler.getDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
    	else
    	{
    		if(!CmsPropertyHandler.getKillLiveRequestWhichTimedout())
    			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view seems to take to long!", false);
    		else
    			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
    	}
    	
    	RequestAnalyser.getRequestAnalyser().incNumberOfCurrentRequests(tk);

    	long start 			= System.currentTimeMillis();
    	long elapsedTime 	= 0;
    	float startTotalMemory 	= ((float)Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    	    	
    	DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
    	
		beginTransaction(dbWrapper.getDatabase());

   		try
		{
			validateAndModifyInputParameters(dbWrapper.getDatabase());

	    	this.nodeDeliveryController			= NodeDeliveryController.getNodeDeliveryController(this.siteNodeId, this.languageId, this.contentId);
			this.integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(this.siteNodeId, this.languageId, this.contentId);
				    	
	    	boolean isUserRedirected = false;
			Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionIdForPageCache(dbWrapper.getDatabase(), siteNodeId);
			logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
			Integer forceProtocolChangeSetting = this.nodeDeliveryController.getForceProtocolChangeSettingForPageCache(dbWrapper.getDatabase(), siteNodeId);
			//System.out.println("forceProtocolChangeSetting:" + forceProtocolChangeSetting);
			
			if(logger.isInfoEnabled())
				logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
			
			String protectWorking = CmsPropertyHandler.getProtectDeliverWorking();
			String protectPreview = CmsPropertyHandler.getProtectDeliverPreview();
			boolean protectDeliver = false;

			if(protectWorking.equals("true") && CmsPropertyHandler.getOperatingMode().equals("0"))
				protectDeliver = true;
			else if(protectPreview.equals("true") && CmsPropertyHandler.getOperatingMode().equals("2"))
				protectDeliver = true;

			isUserRedirected = handleAccessBasedProtocolRedirect(protectedSiteNodeVersionId, this.repositoryId, forceProtocolChangeSetting, dbWrapper.getDatabase());

			if(!isUserRedirected)
			{
				if(getRequest().getParameter("authenticateUser") != null && getRequest().getParameter("authenticateUser").equals("true"))
				{
					isUserRedirected = authenticateUser(dbWrapper.getDatabase());
				}
			}

			if(!isUserRedirected)
			{
				if(logger.isInfoEnabled())
					logger.info("RemoteAddress:" + getRequest().getRemoteAddr());
				
				if(CmsPropertyHandler.getAllowInternalCallsBasedOnIP())
				{
					if(getRequest().getRemoteAddr().equals("127.0.0.1") || getRequest().getRemoteAddr().equals("192.168.0.1") || getRequest().getRemoteAddr().equals("0:0:0:0:0:0:0:1%0"))
						protectDeliver = false;
				}
				
				boolean isAnonymousAuthorized = true;
				logger.info("protectedSiteNodeVersionId:" +protectedSiteNodeVersionId);
				if(protectedSiteNodeVersionId != null)
				{
					SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(protectedSiteNodeVersionId, dbWrapper.getDatabase());
					if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
						isAnonymousAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(dbWrapper.getDatabase(), (InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), false);
					else
						isAnonymousAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(dbWrapper.getDatabase(), (InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
				}
				logger.info("isAnonymousAuthorized:" + isAnonymousAuthorized);
				logger.info("URI:" + getOriginalFullURL());
				if((protectedSiteNodeVersionId != null && !isAnonymousAuthorized) || protectDeliver || getOriginalFullURL().indexOf("ticket=") > -1)
				{
					logger.info("Plain login routine which among others looks for ticket....");
					if(logger.isInfoEnabled())
					{
						logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
						logger.info("protectDeliver:" + protectDeliver);
					}
					isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), this.repositoryId, protectedSiteNodeVersionId, protectDeliver, false);
				}
				else
				{
					String forceIdentityCheck = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(this.repositoryId, "forceIdentityCheck");
					if(logger.isInfoEnabled())
						logger.info("forceIdentityCheck:" + forceIdentityCheck);
					if(this.principal == null || CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(this.principal.getName()))
					{
						if(CmsPropertyHandler.getForceIdentityCheck().equalsIgnoreCase("true") || (forceIdentityCheck != null && forceIdentityCheck.equalsIgnoreCase("true")))
						{
							boolean isForcedIdentityCheckDisabled = this.nodeDeliveryController.getIsForcedIdentityCheckDisabled(dbWrapper.getDatabase(), this.siteNodeId);
							if(!isForcedIdentityCheckDisabled && getHttpSession().getAttribute("ssoChecked") == null && (getRequest().getParameter("skipSSOCheck") == null || !getRequest().getParameter("skipSSOCheck").equals("true")))
							{
								Timer t = new Timer();
								logger.info("Let's check for IP:s and user agents to skip redirect for...");
								String userAgentsRegexp = CmsPropertyHandler.getUserAgentsSkippingSSOCheck();
								String ipAddressesRegexp = CmsPropertyHandler.getIpNumbersSkippingSSOCheck();
								
								logger.info("userAgentsRegexp:" + userAgentsRegexp);
								if(userAgentsRegexp != null && !userAgentsRegexp.equals("") && getBrowserBean() != null && getBrowserBean().getUseragent() != null)
								{
									if(getBrowserBean().getUseragent().matches(userAgentsRegexp))
							    	{
										logger.info("Skipping SSO check for this useragent: " + getBrowserBean().getUseragent());
										isForcedIdentityCheckDisabled = true;
							    	}
								}
								
								logger.info("isForcedIdentityCheckDisabled:" + isForcedIdentityCheckDisabled);
								if(!isForcedIdentityCheckDisabled)
								{
									logger.info("ipAddressesRegexp:" + ipAddressesRegexp);
									if(ipAddressesRegexp != null && !ipAddressesRegexp.equals("") && getRequest().getRemoteAddr() != null)
									{
										logger.info("IP:" + getRequest().getRemoteAddr());
										if(getRequest().getRemoteAddr().matches(ipAddressesRegexp))
								    	{
											logger.info("Skipping SSO check for this ip address: " + getRequest().getRemoteAddr());
											isForcedIdentityCheckDisabled = true;
								    	}
									}
								}
								if(logger.isInfoEnabled())
									logger.info("Checking ip and useragent took: " + t.getElapsedTime());
							}

							if(logger.isInfoEnabled())
							{
								logger.info("isForcedIdentityCheckDisabled:" + isForcedIdentityCheckDisabled);
								logger.info("isForcedIdentityCheckDisabled:" + isForcedIdentityCheckDisabled);
								logger.info("getHttpSession().getAttribute(ssoChecked):" + getHttpSession().getAttribute("ssoChecked"));
								logger.info("getRequest().getParameter(skipSSOCheck):" + getRequest().getParameter("skipSSOCheck"));
							}
							if(!isForcedIdentityCheckDisabled && getHttpSession().getAttribute("ssoChecked") == null && (getRequest().getParameter("skipSSOCheck") == null || !getRequest().getParameter("skipSSOCheck").equals("true")))
							{
								isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), true);
								logger.info("isUserRedirected:" + isUserRedirected);
							}
							else if(!isForcedIdentityCheckDisabled && getRequest().getParameter("skipSSOCheck") != null && getRequest().getParameter("skipSSOCheck").equals("true"))
							{
								getHttpSession().setAttribute("ssoChecked", "true");
							}
						}
					}
				}
			}
						
			logger.info("isUserRedirected:" + isUserRedirected);
			logger.info("principal:" + this.principal);
			if(!isUserRedirected)
				isUserRedirected = rewriteUrl();
			
			String pageKey = this.nodeDeliveryController.getPageCacheKey(dbWrapper.getDatabase(), this.getHttpSession(), getRequest(), this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "");

	    	if(logger.isInfoEnabled())
	    		logger.info("pageKey:" + pageKey);

	    	templateController = getTemplateController(dbWrapper, getSiteNodeId(), getLanguageId(), getContentId(), getRequest(), (InfoGluePrincipal)this.principal, false);

			if(logger.isInfoEnabled())
				logger.info("handled extranet users: " + isUserRedirected);
	
			// ----
			// -- portlet
			// ----
			
			// -- check if the portal is active
	        String portalEnabled = CmsPropertyHandler.getEnablePortal();
	        boolean portalActive = ((portalEnabled != null) && portalEnabled.equals("true"));
			
	        if (portalActive && !isRecacheCall) 
	        {
	        	if(logger.isInfoEnabled())
	        		logger.info("---> Checking for portlet action");
	            
	        	PortalService service = new PortalService();
	            //TODO: catch PortalException?
	            boolean actionExecuted = service.service(getRequest(), getResponse());
	            
	            // -- if an action was executed return NONE as a redirect is issued
	            if (actionExecuted) 
	            {
	            	if(logger.isInfoEnabled())
	            		logger.info("---> PortletAction was executed, returning NONE as a redirect has been issued");
	                isUserRedirected = true;
	                return NONE;
	            }
	        }
	
	        if(logger.isInfoEnabled())
	        	logger.info("handled portal action: " + isUserRedirected);
	        
			if(!isUserRedirected)
			{	
				if(logger.isInfoEnabled())
					logger.info("this.templateController.getPrincipal():" + templateController.getPrincipal());
				
				DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(true);
				deliveryContext.setRepositoryName(this.repositoryName);
				deliveryContext.setSiteNodeId(this.siteNodeId);
				deliveryContext.setContentId(this.contentId);
				deliveryContext.setLanguageId(this.languageId);
				deliveryContext.setPageKey(pageKey);
				//deliveryContext.setSession(this.getSession());
				//deliveryContext.setInfoGlueAbstractAction(this);
				deliveryContext.setHttpServletRequest(this.getRequest());
				deliveryContext.setHttpServletResponse(this.getResponse());
				deliveryContext.setUseFullUrl(Boolean.parseBoolean(CmsPropertyHandler.getUseDNSNameInURI()));
				
				boolean skipRender = false;
				PageDeliveryMetaDataVO pdmd = null;
				try
				{
					boolean isIfModifiedLogic = getIsIfModifiedLogicValid(deliveryContext, templateController.getPrincipal(), true); 
					logger.info("isIfModifiedLogic:" + isIfModifiedLogic);
					if(isIfModifiedLogic)
					{
						String ifModifiedSince = this.getRequest().getHeader("If-Modified-Since");
						logger.info("ifModifiedSince:" + ifModifiedSince);
						//System.out.println("pageKey:" + pageKey);
						if(ifModifiedSince != null && !ifModifiedSince.equals(""))
						{
							pdmd = PageDeliveryMetaDataController.getController().getPageDeliveryMetaDataVO(dbWrapper.getDatabase(), this.siteNodeId, this.languageId, this.contentId);
							logger.info("pdmd A:" + (pdmd == null ? "null" : pdmd.getId()));
							if(pdmd != null && pdmd.getLastModifiedDateTime() != null)
							{
								logger.info("pdmd getLastModifiedTimeout:" + pdmd.getLastModifiedTimeout());
								logger.info("System.currentTimeMillis:" + System.currentTimeMillis());
								long diff = System.currentTimeMillis() - (pdmd.getLastModifiedTimeout() != null ? pdmd.getLastModifiedTimeout().getTime() : 0);
								logger.info("diff:" + diff);
								if(diff < 0 || pdmd.getLastModifiedTimeout() == null)
								{
									Date ifModifiedSinceDate = HTTP_DATE_FORMAT.parse( ifModifiedSince );
									logger.info("pdmd B:" + pdmd.getId() + ":" + pdmd.getLastModifiedDateTime());
									logger.info("*************\nCompares:" + pdmd.getLastModifiedDateTime() + "=" + ifModifiedSinceDate);
									logger.info("pdmd.getLastModifiedTimeout():" + pdmd.getLastModifiedTimeout());
									if(ifModifiedSinceDate.getTime() >= pdmd.getLastModifiedDateTime().getTime() - 1000)
									{
										logger.info("Returning NOT_MODIFIED");
										this.getResponse().setStatus( HttpServletResponse.SC_NOT_MODIFIED );
										pageTimer.printElapsedTime("Delivered NOT MODIFIED IN", 50);
										skipRender = true;
										return NONE;
									}
								}
							}
						}
					}
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
				logger.info("skipRender:" + skipRender);
				if(!skipRender)
				{
					SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO = getSiteNodeTypeDefinition(this.siteNodeId, dbWrapper.getDatabase());
							
					try
				    {
				        String invokerClassName = siteNodeTypeDefinitionVO.getInvokerClassName();
				        PageInvoker pageInvoker = (PageInvoker)Class.forName(invokerClassName).newInstance();
				        pageInvoker.setParameters(dbWrapper, this.getRequest(), this.getResponse(), templateController, deliveryContext);
				        pageInvoker.deliverPage();
	
				        boolean isCachedResponse = deliveryContext.getIsCachedResponse();
				        logger.info("isCachedResponse:" + isCachedResponse);
				        
						boolean isIfModifiedLogic = getIsIfModifiedLogicValid(deliveryContext, templateController.getPrincipal(), false); 
						logger.info("isIfModifiedLogic 2:" + isIfModifiedLogic);
						logger.info("deliveryContext.getLastModifiedDateTime():" + deliveryContext.getLastModifiedDateTime());
						if(isCachedResponse && pdmd == null && isIfModifiedLogic)
							pdmd = PageDeliveryMetaDataController.getController().getPageDeliveryMetaDataVO(dbWrapper.getDatabase(), this.siteNodeId, this.languageId, this.contentId);
						
						
						if(pdmd != null)
							logger.info("pdmd():" + pdmd.getLastModifiedDateTime());

						if(isIfModifiedLogic && (!isCachedResponse || pdmd == null/* || deliveryContext.getLastModifiedDateTime().after(pdmd.getLastModifiedDateTime())*/))
						{
							Timer t2 = new Timer();
							logger.info("We should register the last modified date now. Add it to the thread which registers it: " + deliveryContext.getLastModifiedDateTime() + ":" + deliveryContext.hashCode());
							try
							{
								PageDeliveryMetaDataVO pageDeliveryMetaDataVO = new PageDeliveryMetaDataVO();

								List<String> entities = deliveryContext.getAllUsedEntitiesAsSet();
								
								List<String> allUsedEntitiesFilteredCopy = new ArrayList<String>();
								for(String s : entities)
								{
									if(s.startsWith("content_") && s.indexOf("_", 8) == -1)
									{
										allUsedEntitiesFilteredCopy.add(s.replaceAll("content_", "c_"));
										//allUsedEntitiesFilteredCopy.add(s); //getPooledString(s.hashCode()));
										//System.out.println("Added: " + s);
									}
									else if(s.startsWith("siteNode_"))
										allUsedEntitiesFilteredCopy.add(s.replaceAll("siteNode_", "sn_"));
									else if(s.startsWith("selectiveCacheUpdateNonApplicable"))
									{
										allUsedEntitiesFilteredCopy.clear();
										allUsedEntitiesFilteredCopy.add(s);
										pageDeliveryMetaDataVO.setSelectiveCacheUpdateNotApplicable(true);
									}
								}

								Collection<PageDeliveryMetaDataEntityVO> entitiesCollection = new ArrayList<PageDeliveryMetaDataEntityVO>(); 
								for(String s : allUsedEntitiesFilteredCopy)
								{
									if(s.startsWith("c_"))
									{
										PageDeliveryMetaDataEntityVO pageDeliveryMetaDataEntityVO = new PageDeliveryMetaDataEntityVO();
										pageDeliveryMetaDataEntityVO.setContentId(new Integer(s.replaceAll("c_", "")));
										pageDeliveryMetaDataEntityVO.setPageDeliveryMetaDataId(pageDeliveryMetaDataVO.getId());
										entitiesCollection.add(pageDeliveryMetaDataEntityVO);
									}
									else if(s.startsWith("sn_"))
									{
										PageDeliveryMetaDataEntityVO pageDeliveryMetaDataEntityVO = new PageDeliveryMetaDataEntityVO();
										pageDeliveryMetaDataEntityVO.setSiteNodeId(new Integer(s.replaceAll("sn_", "")));
										pageDeliveryMetaDataEntityVO.setPageDeliveryMetaDataId(pageDeliveryMetaDataVO.getId());
										entitiesCollection.add(pageDeliveryMetaDataEntityVO);
									}
								}

								String allUsedEntitiesAsString = StringUtils.join(allUsedEntitiesFilteredCopy, "|");
								logger.info("allUsedEntitiesAsString:" + allUsedEntitiesAsString);
								//System.out.println("allUsedEntitiesAsString:" + allUsedEntitiesAsString.length());
								
								pageDeliveryMetaDataVO.setSiteNodeId(deliveryContext.getSiteNodeId());
								pageDeliveryMetaDataVO.setLanguageId(deliveryContext.getLanguageId());
								pageDeliveryMetaDataVO.setContentId(deliveryContext.getContentId());
								pageDeliveryMetaDataVO.setLastModifiedDateTime(deliveryContext.getLastModifiedDateTime());
								if(deliveryContext.getPageCacheTimeout() != null && deliveryContext.getPageCacheTimeout() > -1)
								{
									logger.info("deliveryContext.getPageCacheTimeout(): " + deliveryContext.getPageCacheTimeout());
									logger.info("Setting page timeout: " + deliveryContext.getPageCacheTimeout()*1000);
									logger.info("Current time: " + System.currentTimeMillis());
									Calendar cal = Calendar.getInstance();
									cal.add(Calendar.SECOND, deliveryContext.getPageCacheTimeout());
									pageDeliveryMetaDataVO.setLastModifiedTimeout(cal.getTime());
								}
								else
									logger.info("AAAAAAAAAAAAAAAAAAAAA: " + deliveryContext.getPageCacheTimeout());

								pageDeliveryMetaDataVO.setUsedEntities(allUsedEntitiesAsString);
								PageDeliveryMetaDataController.getController().deletePageDeliveryMetaData(dbWrapper.getDatabase(), pageDeliveryMetaDataVO.getSiteNodeId(), null);
								PageDeliveryMetaDataController.getController().create(dbWrapper.getDatabase(), pageDeliveryMetaDataVO, entitiesCollection);
								
								String key = "" + pageDeliveryMetaDataVO.getSiteNodeId() + "_" + pageDeliveryMetaDataVO.getLanguageId() + "_" + pageDeliveryMetaDataVO.getContentId();
						    	logger.info("key on store:" + key);
								CacheController.cacheObjectInAdvancedCache("pageDeliveryMetaDataCache", key, pageDeliveryMetaDataVO);
							}
							catch (Exception e) 
							{
								logger.error("Error storing page meta data: " + e.getMessage(), e);
							}
							t2.printElapsedTime("AAAAAAAAAAAAAAAAA",20);
						}
				        request.setAttribute("progress", "after pageInvoker was called");
				    }
				    catch(ClassNotFoundException e)
				    {
				        throw new SystemException("An error was thrown when trying to use the page invoker class assigned to this page type:" + e.getMessage(), e);
					}
				    finally
				    {
				    	deliveryContext.clear();
				    	deliveryContext = null;
				    }
				}
			}
			
	        //StatisticsService.getStatisticsService().registerRequest(getRequest(), getResponse(), pagePath, elapsedTime);
			//logger.info("Registered request in statistics service");
		}
		catch(PageNotFoundException e)
		{
			String extraInformation = "Original URL: " + getOriginalFullURL() + "\n";
			extraInformation += "Referer: " + getRequest().getHeader("Referer") + "\n";
			extraInformation += "UserAgent: " + getRequest().getHeader("User-Agent") + "\n";
			extraInformation += "User IP: " + getRequest().getRemoteAddr();
			
			logger.warn("A user requested a non existing page:" + e.getMessage() + "\n" + extraInformation);
			rollbackTransaction(dbWrapper.getDatabase());

			getResponse().setContentType("text/html; charset=UTF-8");
			getRequest().setAttribute("responseCode", "404");
			getRequest().setAttribute("error", e);
			getRequest().setAttribute("errorUrl", getErrorUrl());
			getRequest().getRequestDispatcher("/ErrorPage.action").forward(getRequest(), getResponse());
		}
		catch(NoBaseTemplateFoundException e)
		{
			String extraInformation = "Original URL: " + getOriginalFullURL() + "\n";
			extraInformation += "Referer: " + getRequest().getHeader("Referer") + "\n";
			extraInformation += "UserAgent: " + getRequest().getHeader("User-Agent") + "\n";
			extraInformation += "User IP: " + getRequest().getRemoteAddr();
			
			logger.error("A user requested a page which had no base template (probably of the old HTMLPageInvoker type - should be changed):" + e.getMessage() + "\n" + extraInformation);
			rollbackTransaction(dbWrapper.getDatabase());

			getResponse().setContentType("text/html; charset=UTF-8");
			getRequest().setAttribute("responseCode", "500");
			getRequest().setAttribute("error", e);
			getRequest().setAttribute("errorUrl", getErrorUrl());
			getRequest().getRequestDispatcher("/ErrorPage.action").forward(getRequest(), getResponse());
		}
		catch(IOException e)
		{
			String extraInformation = "Original URL: " + getOriginalFullURL() + "\n";
			extraInformation += "Referer: " + getRequest().getHeader("Referer") + "\n";
			extraInformation += "UserAgent: " + getRequest().getHeader("User-Agent") + "\n";
			extraInformation += "User IP: " + getRequest().getRemoteAddr();
			
			if(e.getCause() != null)
			{
				if(e.getCause() instanceof SocketException)
					logger.warn("A io exception was thrown returning data to client:" + e.getCause().getMessage() + "\n" + extraInformation);
				else
					logger.error("A io exception was thrown returning data to client:" + e.getCause().getMessage() + "\n" + extraInformation);					
			}
			else
				logger.error("A io exception was thrown returning data to client:" + e.getMessage() + "\n" + extraInformation);		
			rollbackTransaction(dbWrapper.getDatabase());
		}
		catch(Exception e)
		{
			logger.warn("Extra warn:" + e.getMessage(), e);
			
			String extraInformation = "Original URL: " + getOriginalFullURL() + "\n";
			extraInformation += "Referer: " + getRequest().getHeader("Referer") + "\n";
			extraInformation += "UserAgent: " + getRequest().getHeader("User-Agent") + "\n";
			extraInformation += "User IP: " + getRequest().getRemoteAddr();

			logger.error("An error occurred (se warning log for further info):" + e.getMessage());
			logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage() + "\n" + extraInformation, e);
			
			rollbackTransaction(dbWrapper.getDatabase());

			getResponse().setContentType("text/html; charset=UTF-8");
			getRequest().setAttribute("responseCode", "500");
			getRequest().setAttribute("error", e);
			getRequest().setAttribute("errorUrl", getErrorUrl());
			getRequest().getRequestDispatcher("/ErrorPage.action").forward(getRequest(), getResponse());
		}
		finally
		{
			if(logger.isInfoEnabled())
				logger.info("Before closing transaction");
			
			try
			{
				closeTransaction(dbWrapper.getDatabase());
			}
			catch(Exception e) 
			{ 
				logger.error("Problem closing connection:" + e.getMessage(), e);
			}
			
			try
			{
				if(templateController != null)
				{
					templateController.clear();
			    	templateController = null;
				}
			}
			catch (Exception e) 
			{
				logger.error("Problem clearing:" + e.getMessage(), e);
			}
			
			if(logger.isInfoEnabled())
				logger.info("After closing transaction");

			elapsedTime = Math.abs(System.currentTimeMillis() - start);
			RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(elapsedTime);
			lastRequestProcessingTime = elapsedTime;

			//This only states how much memory in general has been allocated more than when the thread started. It can well be other threads allocating but a timeframe is nice.
			float memoryDiff = (((float)Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - startTotalMemory) / 1024f / 1024f;
			logger.info("memoryDiff:" + memoryDiff + "(" + startTotalMemory + "-" + ((float)Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + ")");
			if(memoryDiff > 100)
				logger.warn("During the rendering of: " + this.getOriginalFullURL() + " a large amount (" + (int)memoryDiff + "MB) was allocated.");
			
		    //System.out.println("The page delivery took " + elapsedTime + "ms for request " + this.getRequest().getRequestURL() + "?" + this.getRequest().getQueryString());
			if(!memoryWarningSent)
			{
				float memoryLeft = ((float)Runtime.getRuntime().maxMemory() - (float)Runtime.getRuntime().totalMemory()) / 1024f / 1024f;
				float percentLeft = (memoryLeft / ((float)Runtime.getRuntime().maxMemory() / 1024f / 1024f)) * 100f;
				float percentLeft2 = ((float)Runtime.getRuntime().freeMemory() / (float)Runtime.getRuntime().totalMemory()) * 100f;

				if(percentLeft < 15 && percentLeft2 < 15)
				{
					memoryWarningSent = true;
					String subject = "Memory is getting low on " + CmsPropertyHandler.getServerName();
					String mailBody = "The java maximum heap size is almost used up - only " + (int)memoryLeft + "MB (" + (int)percentLeft + "%) left. Increase the max heap size if possible or trim the cache sizes if they are very large.";
			        String warningEmailReceiver = CmsPropertyHandler.getWarningEmailReceiver();
			        if(warningEmailReceiver != null && !warningEmailReceiver.equals("") && warningEmailReceiver.indexOf("@warningEmailReceiver@") == -1)
			        {
						try
						{
							logger.warn("Sending warning mail:" + (int)percentLeft + ":" + (int)memoryLeft + ":" + Runtime.getRuntime().maxMemory() / 1024 / 1024);
							MailServiceFactory.getService().sendEmail(CmsPropertyHandler.getMailContentType(), warningEmailReceiver, warningEmailReceiver, null, null, null, null, subject, mailBody, "utf-8");
						} 
						catch (Exception e)
						{
							logger.error("Could not send mail:" + e.getMessage(), e);
						}
			        }
				}
			}

			String originalFullUrl = getOriginalFullURL();
			RequestAnalyser.getRequestAnalyser().registerLatestPageStatistics("" + originalFullUrl + " (" + browserBean.getUseragent() + ", IP: " + getRequest().getRemoteAddr() + ") - took " + elapsedTime + " ms.");
			if(elapsedTime > 5000)
				RequestAnalyser.getRequestAnalyser().registerPageStatistics("" + originalFullUrl + " (" + browserBean.getUseragent() + ")", elapsedTime);
		    		    
		    //System.out.println("The page delivery took " + elapsedTime + "ms");
		    if(elapsedTime > 10000)
			{
			    logger.warn("The page delivery took " + elapsedTime + "ms for request " + originalFullUrl);
			    logger.warn("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
			}
			else
			{
				if(logger.isInfoEnabled())
				{
					logger.info("The page delivery took " + elapsedTime + "ms");			
					logger.info("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
				}
			}

	    	if(tk != null && !tk.getIsDoneRunning())
	    		tk.done();
	    	else
	    		logger.warn("Done had allready been run... skipping");
		
			killUnwantedSessions();
		}
		
        return NONE;
    }

    /**
     * The logic is that we only regard the if modified if the page is a standard page.
     * No custom parameters, no login, no special page key etc.
     * @param request
     * @return
     */
    private boolean getIsIfModifiedLogicValid(DeliveryContext deliverContext, InfoGluePrincipal principal, boolean regardHeader) 
    {
    	String ifModifiedSince = this.getRequest().getHeader("If-Modified-Since");
    	if((ifModifiedSince == null || ifModifiedSince.equals("")) && regardHeader)
    		return false;
    		
    	String method = deliverContext.getHttpServletRequest().getMethod().toLowerCase();
    	//int parameterSize = deliverContext.getHttpServletRequest().getParameterMap().size();
    	//System.out.println("Method:" + method);
    	//System.out.println("CmsPropertyHandler.getOperatingMode():" + CmsPropertyHandler.getOperatingMode());
    	
    	if(!method.equals("get") || !CmsPropertyHandler.getOperatingMode().equals("3"))
    	{
    		return false;
    	}
    	else
    	{
    		if(principal != null && !principal.getName().equals(CmsPropertyHandler.getAnonymousUser()))
    			return false;
    	
    		for(Object parameterName : deliverContext.getHttpServletRequest().getParameterMap().keySet())
    		{
    			if(!parameterName.toString().equalsIgnoreCase("siteNodeId") && 
    			   !parameterName.toString().equalsIgnoreCase("amp;siteNodeId") && 
    			   !parameterName.toString().equalsIgnoreCase("languageId") && 
    	    	   !parameterName.toString().equalsIgnoreCase("amp;languageId") && 
    			   !parameterName.toString().equalsIgnoreCase("contentId") &&
    			   !parameterName.toString().equalsIgnoreCase("amp;contentId") &&
    			   !parameterName.toString().equalsIgnoreCase("originalServletPath") && 
    			   !parameterName.toString().equalsIgnoreCase("originalRequestURL") && 
    			   !parameterName.toString().equalsIgnoreCase("originalRequestURI") && 
    			   !parameterName.toString().equalsIgnoreCase("originalQueryString"))
    			{
    				logger.info("parameterName:" + parameterName);
    				return false;
    			}
    		}
    		
    		try
    		{
    			if(browserBean.getUseragent() != null)
    			{
    				Pattern pattern = null;
    				String userAgentsRegexp = null;
    				try
    				{
    					Map cacheSettings = CmsPropertyHandler.getCacheSettings();
    					//System.out.println("cacheSettings:" + cacheSettings);
    			    	if(cacheSettings != null)
    			    	{
    			    		userAgentsRegexp = (String)cacheSettings.get("ENABLE_IF_MODIFIED_FOR_USERAGENTSMATCHING");
    				    	if(userAgentsRegexp != null && !userAgentsRegexp.equals(""))
    							pattern = Pattern.compile(userAgentsRegexp);
    				    }
    				}
    				catch (Exception e) 
    				{
    					logger.warn("cacheSettings was null:" + e.getMessage(), e);
    				}

    				if(logger.isInfoEnabled())
    					logger.info("userAgentsRegexp:" + userAgentsRegexp);
    				
    				if(pattern != null)
    				{
    			        Matcher matcher = pattern.matcher(browserBean.getUseragent());
    			        if(!matcher.find())
    			    	{
    			        	logger.error("Not using if modified logic for:" + browserBean.getUseragent());
    			        	return false;
    			    	}
    			    	matcher.reset();
    				}
    				else
    				{
    					System.out.println("We disable the IF_MODIFIED feature if not stated.");
    					return false;
    				}
    			}
    		}
    		catch (Exception e) 
    		{
    			logger.warn("Problem with if modified since: " + e.getMessage(), e);
    			return false;
    		}
    	}

    	return true;
	}

	/**
     * This method handles redirect of any special cases where we need to redirect the user based on particular situations. 
     * For example to remove a CAS-ticket in the URL or similar.
     * @return true if the user was redirected.
     * @throws Exception
     */
    private boolean rewriteUrl() throws Exception
    {
    	boolean isUserRedirected = false;
    	
    	String URI = getOriginalURI();
		if(logger.isInfoEnabled())
		{
			logger.info("Ticket:" + getRequest().getParameter("ticket"));
			logger.info("URI:" + URI);
		}
		
		if(getRequest().getMethod().equalsIgnoreCase("get") && getRequest().getParameter("ticket") != null && getRequest().getParameter("ticket").length() > 0)
		{
			String queryString = getOriginalQueryString();
			if(logger.isInfoEnabled())
				logger.info("queryString:" + queryString);
			String remainingQueryString = HttpUtilities.removeParameter(queryString, "ticket");
			if(remainingQueryString.indexOf("noAccess=true") == -1)
				remainingQueryString = HttpUtilities.removeParameter(remainingQueryString, "referer");
			
			remainingQueryString = HttpUtilities.removeParameter(remainingQueryString, "recheckSSO");

			if(logger.isInfoEnabled())
				logger.info("remainingQueryString:" + remainingQueryString);

			if(logger.isInfoEnabled())
				logger.info("Redirecting to:" + URI + (remainingQueryString != null && !remainingQueryString.equals("") ? "?" + remainingQueryString : ""));
			getResponse().sendRedirect(URI + (remainingQueryString != null && !remainingQueryString.equals("") ? "?" + remainingQueryString : ""));
			//return true;
			isUserRedirected = true;
		}
		
		return isUserRedirected;
	}

	/**
     * This method checks out for and switches between protocols if set depending on if the page was protected or not.
     * @param protectedSiteNodeVersionId
     * @return
     * @throws IOException
     */
	private boolean handleAccessBasedProtocolRedirect(Integer protectedSiteNodeVersionId, Integer repositoryId, Integer forceProtocolChangeSetting, Database db)
	{
		boolean isUserRedirected = false;
		
		try
		{
			String repositoryUseAccessBasedProtocolRedirects = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(repositoryId, "useAccessBasedProtocolRedirects");
			if(repositoryUseAccessBasedProtocolRedirects == null || repositoryUseAccessBasedProtocolRedirects.equals("") || !repositoryUseAccessBasedProtocolRedirects.equals("true") || !repositoryUseAccessBasedProtocolRedirects.equals("false"))
				repositoryUseAccessBasedProtocolRedirects = CmsPropertyHandler.getUseAccessBasedProtocolRedirects();

			//String useAccessBasedProtocolRedirectsString = CmsPropertyHandler.getUseAccessBasedProtocolRedirects();
			String unprotectedProtocolName = CmsPropertyHandler.getUnprotectedProtocolName();
			String unprotectedProtocolPort = CmsPropertyHandler.getUnprotectedProtocolPort();
			String protectedProtocolName = CmsPropertyHandler.getProtectedProtocolName();
			String protectedProtocolPort = CmsPropertyHandler.getProtectedProtocolPort();
			String accessBasedProtocolRedirectHTTPCode = CmsPropertyHandler.getAccessBasedProtocolRedirectHTTPCode();
			if(logger.isInfoEnabled())
			{
				logger.info("unprotectedProtocolName:" + unprotectedProtocolName);
				logger.info("protectedProtocolName:" + protectedProtocolName);
				logger.info("unprotectedProtocolPort:" + unprotectedProtocolPort);
				logger.info("protectedProtocolPort:" + protectedProtocolPort);
			}
			
			boolean useAccessBasedProtocolRedirects = false;
			if(repositoryUseAccessBasedProtocolRedirects.equals("true") && CmsPropertyHandler.getOperatingMode().equals("3"))
				useAccessBasedProtocolRedirects = true;
			
			if(useAccessBasedProtocolRedirects || forceProtocolChangeSetting.equals(SiteNodeVersionVO.FORCE_SECURE))
			{
				String originalFullURL = getOriginalFullURL();
				//System.out.println("originalFullURL:" + originalFullURL);
		    	boolean isAnonymousAccepted = true;
		    	if(protectedSiteNodeVersionId != null)
		    	{
					Principal anonymousPrincipal = getAnonymousPrincipal();
					isAnonymousAccepted = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)anonymousPrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
		    	}
		    	
		    	if((protectedSiteNodeVersionId != null && !isAnonymousAccepted) || forceProtocolChangeSetting.equals(SiteNodeVersionVO.FORCE_SECURE))
				{
					if(originalFullURL.indexOf(unprotectedProtocolName + "://") > -1)
					{	
						String redirectUrl = originalFullURL.replaceFirst(unprotectedProtocolName + "://", protectedProtocolName + "://").replaceFirst(":" + unprotectedProtocolPort + "/", ":" + protectedProtocolPort + "/");
						getResponse().sendRedirect(redirectUrl);
						logger.info("Redirecting user to:" + redirectUrl);
						isUserRedirected = true;
					}
				}
				else
				{
					if(!forceProtocolChangeSetting.equals(SiteNodeVersionVO.ALLOW_SECURE))
					{
						if(originalFullURL.indexOf(protectedProtocolName + "://") > -1)
						{	
							String redirectUrl = originalFullURL.replaceFirst(protectedProtocolName + "://", unprotectedProtocolName + "://").replaceFirst(":" + protectedProtocolPort + "/", ":" + unprotectedProtocolPort + "/");
							getResponse().setStatus(new Integer(accessBasedProtocolRedirectHTTPCode));
							getResponse().sendRedirect(redirectUrl);
							logger.info("Redirecting user to:" + redirectUrl);
							isUserRedirected = true;
						}
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Error in handleAccessBasedProtocolRedirect:" + e.getMessage(), e);
		}
			
		return isUserRedirected;
	}
    


	/**
	 * This method the renderer for the component editor. 
	 */
         
	public String doRenderDecoratedPage() throws Exception
	{
		if(CmsPropertyHandler.getOperatingMode().equals("3"))
		{
			this.getResponse().setStatus(301);
			this.getResponse().setHeader( "Location", getOriginalFullURL().replaceAll("\\!renderDecoratedPage", ""));
			return NONE; //doExecute();
		}
		
		Timer t = new Timer();
        while(CmsPropertyHandler.getActuallyBlockOnBlockRequests() && RequestAnalyser.getRequestAnalyser().getBlockRequests())
        {
        	//System.out.println("Queing up requests as cache eviction are taking place..");
        	Thread.sleep(10);
        }
        t.printElapsedTime("Waited in render view on blocking threads - correct", 50);		
		if(logger.isInfoEnabled())
		{
	        logger.info("************************************************");
			logger.info("* ViewPageAction was called....                *");
			logger.info("************************************************");
		}
		
        HttpServletRequest request = getRequest();

        ThreadMonitor tk = null;
    	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
    		tk = new ThreadMonitor(new Long(CmsPropertyHandler.getDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
    	else
    	{
    		if(!CmsPropertyHandler.getKillLiveRequestWhichTimedout())
    			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view seems to take to long!", false);
    		else
    			tk = new ThreadMonitor(new Long(CmsPropertyHandler.getLiveDeliverRequestTimeout()).longValue(), request, "Page view took to long!", true);
    	}
    		
    	RequestAnalyser.getRequestAnalyser().incNumberOfCurrentRequests(tk);
    	
   		long start			= System.currentTimeMillis();
		long elapsedTime 	= 0;
		float startTotalMemory 	= ((float)Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    			
		DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
    	//Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(dbWrapper.getDatabase());

		try
		{
			validateAndModifyInputParameters(dbWrapper.getDatabase());
	    	
			this.nodeDeliveryController			= NodeDeliveryController.getNodeDeliveryController(this.siteNodeId, this.languageId, this.contentId);
			this.integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(this.siteNodeId, this.languageId, this.contentId);

			boolean isUserRedirected = false;
			Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(dbWrapper.getDatabase(), siteNodeId, "SiteNodeVersion.Read");
			logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);

			boolean protectDeliver = true;

			if(logger.isInfoEnabled())
				logger.info("RemoteAddress:" + getRequest().getRemoteAddr());
			
			//if(getRequest().getRemoteAddr().equals("127.0.0.1") || getRequest().getRemoteAddr().equals("192.168.0.1"))
			//	protectDeliver = false;
						
			if(protectedSiteNodeVersionId != null || protectDeliver)
				isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), this.repositoryId, protectedSiteNodeVersionId, protectDeliver, true);
			/*
			else
			{
				String forceIdentityCheck = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(this.repositoryId, "forceIdentityCheck");
				if(CmsPropertyHandler.getForceIdentityCheck().equalsIgnoreCase("true") || (forceIdentityCheck != null && forceIdentityCheck.equalsIgnoreCase("true")))
					isUserRedirected = handleExtranetLogic(dbWrapper.getDatabase(), true);
			}
			*/
			
			if(!isUserRedirected)
				isUserRedirected = rewriteUrl();
			
	    	String pageKey = this.nodeDeliveryController.getPageCacheKey(dbWrapper.getDatabase(), this.getHttpSession(), this.getRequest(), this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "_" + this.showSimple + "_pagecomponentDecorated");

			templateController = getTemplateController(dbWrapper, getSiteNodeId(), getLanguageId(), getContentId(), getRequest(), (InfoGluePrincipal)this.principal, true);

	    	InfoGluePrincipal principal = templateController.getPrincipal();
		    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
		    if(cmsUserName != null && !CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(cmsUserName))
			    principal = templateController.getPrincipal(cmsUserName);

		    //As this is the decorated view we need to cache personalized results due to access rights etc.
	    	if(principal != null && pageKey.indexOf(principal.getName()) == -1)
	    		pageKey = pageKey + "_" + principal.getName();

	    	if(logger.isInfoEnabled())
	    		logger.info("A pageKey:" + pageKey);

			if(logger.isInfoEnabled())
				logger.info("handled extranet users");

			// ----
			// -- portlet
			// ----
			
			// -- check if the portal is active
	        String portalEnabled = CmsPropertyHandler.getEnablePortal();
	        boolean portalActive = ((portalEnabled != null) && portalEnabled.equals("true"));
			
	        if (portalActive && !isRecacheCall) 
	        {
	            logger.info("---> Checking for portlet action");
	            PortalService service = new PortalService();
	            //TODO: catch PortalException?
	            boolean actionExecuted = service.service(getRequest(), getResponse());
	            
	            // -- if an action was executed return NONE as a redirect is issued
	            if (actionExecuted) 
	            {
	                logger.info("---> PortletAction was executed, returning NONE as a redirect has been issued");
	                isUserRedirected = true;
	                return NONE;
	            }
	        }
	
	        if(logger.isInfoEnabled())
	        	logger.info("handled portal action");

			if(!isUserRedirected)
			{	
				logger.info("this.templateController.getPrincipal():" + templateController.getPrincipal());
		
				DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(true);
				deliveryContext.setRepositoryName(this.repositoryName);
				deliveryContext.setSiteNodeId(this.siteNodeId);
				deliveryContext.setLanguageId(this.languageId);
				deliveryContext.setContentId(this.contentId);
				deliveryContext.setShowSimple(this.showSimple);
				deliveryContext.setPageKey(pageKey);
				//deliveryContext.setSession(this.getSession());
				//deliveryContext.setInfoGlueAbstractAction(this);
				deliveryContext.setHttpServletRequest(this.getRequest());
				deliveryContext.setHttpServletResponse(this.getResponse());
				deliveryContext.setUseFullUrl(Boolean.parseBoolean(CmsPropertyHandler.getUseDNSNameInURI()));

				//deliveryContext.setDisablePageCache(true);
				
				SiteNodeVO siteNode = nodeDeliveryController.getSiteNodeVO(dbWrapper.getDatabase(), this.siteNodeId);
				if(siteNode == null)
				    throw new SystemException("There was no page with this id.");

				if(siteNode.getSiteNodeTypeDefinitionId() == null)
				    throw new SystemException("There was no SiteNodeTypeDefinition defined for the site node " + siteNode.getName() + "[" + siteNode.getId() + "].");

				SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO = getSiteNodeTypeDefinition(this.siteNodeId, dbWrapper.getDatabase());
				String invokerClassName = siteNodeTypeDefinitionVO.getInvokerClassName();
				
				if(invokerClassName == null || invokerClassName.equals(""))
				{
				    throw new SystemException("There was no page invoker class assigned to this page type.");
				}
				else
				{
				    try
				    {
				        PageInvoker pageInvoker = (PageInvoker)Class.forName(invokerClassName).newInstance();
				        pageInvoker = pageInvoker.getDecoratedPageInvoker(templateController, deliveryContext);
				        pageInvoker.setParameters(dbWrapper, this.getRequest(), this.getResponse(), templateController, deliveryContext);
				        pageInvoker.deliverPage();
				    }
				    catch(ClassNotFoundException e)
				    {
				        throw new SystemException("An error was thrown when trying to use the page invoker class assigned to this page type:" + e.getMessage(), e);
				    }
				    finally
				    {
				    	deliveryContext.clear();
				    	deliveryContext = null;
				    }
				}
			}
			
			//StatisticsService.getStatisticsService().registerRequest(getRequest(), getResponse(), pagePath, elapsedTime);
		}
		catch(PageNotFoundException e)
		{
			String extraInformation = "Original URL: " + getOriginalFullURL() + "\n";
			extraInformation += "Referer: " + getRequest().getHeader("Referer") + "\n";
			extraInformation += "UserAgent: " + getRequest().getHeader("User-Agent") + "\n";
			extraInformation += "User IP: " + getRequest().getRemoteAddr();
			
			logger.warn("A user requested a non existing page:" + e.getMessage() + "\n" + extraInformation);
			rollbackTransaction(dbWrapper.getDatabase());

			getResponse().setContentType("text/html; charset=UTF-8");
			getRequest().setAttribute("responseCode", "404");
			getRequest().setAttribute("error", e);
			getRequest().setAttribute("errorUrl", getErrorUrl());
			getRequest().getRequestDispatcher("/ErrorPage.action").forward(getRequest(), getResponse());
		}
		catch(NoBaseTemplateFoundException e)
		{
			String extraInformation = "Original URL: " + getOriginalFullURL() + "\n";
			extraInformation += "Referer: " + getRequest().getHeader("Referer") + "\n";
			extraInformation += "UserAgent: " + getRequest().getHeader("User-Agent") + "\n";
			extraInformation += "User IP: " + getRequest().getRemoteAddr();
			
			logger.error("A user requested a page which had no base template (probably of the old HTMLPageInvoker type - should be changed):" + e.getMessage() + "\n" + extraInformation);
			rollbackTransaction(dbWrapper.getDatabase());

			getResponse().setContentType("text/html; charset=UTF-8");
			getRequest().setAttribute("responseCode", "500");
			getRequest().setAttribute("error", e);
			getRequest().setAttribute("errorUrl", getErrorUrl());
			getRequest().getRequestDispatcher("/ErrorPage.action").forward(getRequest(), getResponse());
		}
		catch(Exception e)
		{
			String extraInformation = "Original URL: " + getOriginalFullURL() + "\n";
			extraInformation += "Referer: " + getRequest().getHeader("Referer") + "\n";
			extraInformation += "UserAgent: " + getRequest().getHeader("User-Agent") + "\n";
			extraInformation += "User IP: " + getRequest().getRemoteAddr();
			
			if(e instanceof java.net.SocketException)
				logger.warn("An error occurred so we should not complete the transaction:" + e.getMessage() + "\n" + extraInformation);
			else
				logger.error("An error occurred so we should not complete the transaction:" + e.getMessage() + "\n" + extraInformation, e);
				
			rollbackTransaction(dbWrapper.getDatabase());
			
			throw new SystemException(e.getMessage());
		}
		finally
		{
			try
			{
				closeTransaction(dbWrapper.getDatabase());
			}
			catch (Exception e) 
			{
				logger.error("Problem closing connection:" + e.getMessage(), e);
			}
			
			try
			{
				if(templateController != null)
				{
					templateController.clear();
			    	templateController = null;
				}
			}
			catch (Exception e) 
			{
				logger.error("Problem clearing:" + e.getMessage(), e);

			}


			elapsedTime = Math.abs(System.currentTimeMillis() - start);
		    	
		    RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(elapsedTime);

		    //This only states how much memory in general has been allocated more than when the thread started. It can well be other threads allocating but a timeframe is nice.
			float memoryDiff = (((float)Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - startTotalMemory) / 1024f / 1024f;
			logger.info("memoryDiff:" + memoryDiff + "(" + startTotalMemory + "-" + ((float)Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + ")");
			if(memoryDiff > 100)
				logger.warn("During the rendering of: " + this.getOriginalFullURL() + " a large amount (" + (int)memoryDiff + "MB) was allocated.");
			
			if(!memoryWarningSent)
			{
				float memoryLeft = ((float)Runtime.getRuntime().maxMemory() - (float)Runtime.getRuntime().totalMemory()) / 1024f / 1024f;
				float percentLeft = (memoryLeft / ((float)Runtime.getRuntime().maxMemory() / 1024f / 1024f)) * 100f;
				float percentLeft2 = ((float)Runtime.getRuntime().freeMemory() / (float)Runtime.getRuntime().totalMemory()) * 100f;
				
				//System.out.println("memoryLeft:" + memoryLeft);
				//System.out.println("maxMemory:" + (Runtime.getRuntime().maxMemory() / 1024f / 1024f));
				//System.out.println("totalMemory:" + (Runtime.getRuntime().totalMemory() / 1024f / 1024f));
				//System.out.println("freeMemory:" + (Runtime.getRuntime().freeMemory() / 1024f / 1024f));
				//System.out.println("percentLeft:" + percentLeft);
				//System.out.println("percentLeft2:" + percentLeft2);
				if(percentLeft < 15 && percentLeft2 < 15)
				{
					memoryWarningSent = true;
					String subject = "Memory is getting low on " + CmsPropertyHandler.getServerName();
					String mailBody = "The java maximum heap size is almost used up - only " + (int)memoryLeft + "MB (" + (int)percentLeft + "%) left. Increase the max heap size if possible or trim the cache sizes if they are very large.";
			        String warningEmailReceiver = CmsPropertyHandler.getWarningEmailReceiver();
			        if(warningEmailReceiver != null && !warningEmailReceiver.equals("") && warningEmailReceiver.indexOf("@warningEmailReceiver@") == -1)
			        {
						try
						{
							logger.warn("Sending warning mail:" + (int)percentLeft + ":" + (int)memoryLeft + ":" + Runtime.getRuntime().maxMemory() / 1024f / 1024f);
							MailServiceFactory.getService().sendEmail(CmsPropertyHandler.getMailContentType(), warningEmailReceiver, warningEmailReceiver, null, null, null, null, subject, mailBody, "utf-8");
						} 
						catch (Exception e)
						{
							logger.error("Could not send mail:" + e.getMessage(), e);
						}
			        }
				}
			}

			String originalFullUrl = getOriginalFullURL();
			if(elapsedTime > 20000)
			{
			    logger.warn("The page delivery took " + elapsedTime + "ms for request " + originalFullUrl);
			    logger.warn("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
			}
			else
			{
			    logger.info("The page delivery took " + elapsedTime + "ms");			
			    logger.info("The memory consumption was " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "(" + Runtime.getRuntime().totalMemory() + "/" + Runtime.getRuntime().maxMemory() + ") bytes");
			}

	    	if(tk != null && !tk.getIsDoneRunning())
	    		tk.done();
	    	else
	    		logger.warn("Done had allready been run... skipping");
		}
		
		return NONE;
	}
    
  
    
   	/**
	 * This method should be much more sophisticated later and include a check to see if there is a 
	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
	 * The method also consideres wheter or not to invoke the preview-version with administrative functioality or the 
	 * normal site-delivery version.
	 */
	
	public TemplateController getTemplateController(DatabaseWrapper dbWrapper, Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infoGluePrincipal, boolean allowEditOnSightAtAll) throws SystemException, Exception
	{
		TemplateController templateController = new BasicTemplateController(dbWrapper, infoGluePrincipal);
		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
		templateController.setHttpRequest(request);	
		templateController.setBrowserBean(browserBean);
		templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
		
		String operatingMode = CmsPropertyHandler.getOperatingMode();
		
		if(operatingMode != null && (operatingMode.equals("0") || operatingMode.equals("1") || operatingMode.equals("2")))
		{
		    String editOnSite = CmsPropertyHandler.getEditOnSite();
			boolean isEditOnSightDisabled = templateController.getIsEditOnSightDisabled();
			
			if(allowEditOnSightAtAll && !isEditOnSightDisabled && editOnSite != null && editOnSite.equalsIgnoreCase("true"))
			{
				templateController = new EditOnSiteBasicTemplateController(dbWrapper, infoGluePrincipal);
				templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
				templateController.setHttpRequest(request);	
				templateController.setBrowserBean(browserBean);
				templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
			}
		}
		
		return templateController;		
	}


	/**
	 * Here we do all modifications needed on the request. For example we read the startpage if no
	 * siteNodeId is given and stuff like that. Also a good place to put url-rewriting.
	 * Rules so far includes: defaulting to the first repository if not specified and also defaulting to
	 * masterlanguage for that site if not specifying.
	 */
	 
	private void validateAndModifyInputParameters(Database db) throws PageNotFoundException, SystemException, Exception
	{
		this.browserBean = new BrowserBean();
		this.browserBean.setRequest(getRequest());
		
		this.principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
		/*
		boolean enforceJ2EEPrincipal = AuthenticationModule.getAuthenticationModule(null, null).enforceJ2EEContainerPrincipal();
		if(!enforceJ2EEPrincipal || this.getRequest().getUserPrincipal() != null)
			this.principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
		*/
		if(this.principal == null)
		{
			try
			{
				this.principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
				if(this.principal == null)
				{
				    Map arguments = new HashMap();
				    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
				    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
				    
					this.principal = ExtranetController.getController().getAuthenticatedPrincipal(db, arguments);

					if(principal != null)
						CacheController.cacheObject("userCache", "anonymous", this.principal);
				}
				//this.principal = ExtranetController.getController().getAuthenticatedPrincipal("anonymous", "anonymous");
				
			}
			catch(Exception e) 
			{
			    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
			}
		}
		
		if(logger.isDebugEnabled())
			logger.debug("principal in validateAndModifyInputParameters:" + this.principal);
		
		if(getSiteNodeId() == null)
		{
			if(getRepositoryName() == null)
			{
				setRepositoryName(RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getName());
			}
			
			SiteNodeVO rootSiteNodeVO = NodeDeliveryController.getRootSiteNode(db, getRepositoryName());	
			if(rootSiteNodeVO == null)
				throw new SystemException("There was no repository called " + getRepositoryName() + " or no pages were available in that repository");
			
			setSiteNodeId(rootSiteNodeVO.getSiteNodeId());
			repositoryId = rootSiteNodeVO.getRepositoryId();
		} 

		try
		{
			if(getSiteNodeId() != null)
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(getSiteNodeId(), db);
				repositoryId = siteNodeVO.getRepositoryId();
			}	
		}
	    catch(Exception e)
	    {
			throw new PageNotFoundException("There is no page with the requested specification. SiteNodeId:" + getSiteNodeId());
	    }
		
		if(getLanguageId() == null)
		{
		    LanguageVO browserLanguageVO = null;

		    String useBrowserLanguage = CmsPropertyHandler.getUseBrowserLanguage();
		    if(useBrowserLanguage != null && useBrowserLanguage.equals("true"))
		    {
			    String useAlternativeBrowserLanguageCheck = CmsPropertyHandler.getUseAlternativeBrowserLanguageCheck();
			    if(useAlternativeBrowserLanguageCheck == null || !useAlternativeBrowserLanguageCheck.equalsIgnoreCase("true"))
			        browserLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, browserBean.getLanguageCode(), getSiteNodeId(), (InfoGluePrincipal)this.principal);
			    else
			        browserLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfRepositorySupportsIt(db, browserBean.getLanguageCode(), getSiteNodeId());

			    logger.debug("Checking browser language...");
		    }
		    
		    logger.info("browserLanguageVO: " + browserLanguageVO);
		    if(browserLanguageVO != null)
			{
			    logger.info("The system had browserLanguageVO available:" + browserLanguageVO.getName());
			    setLanguageId(browserLanguageVO.getLanguageId());
			}
			else
			{
				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, this.getSiteNodeId());
			    logger.info("masterLanguageVO: " + masterLanguageVO);
				if(masterLanguageVO == null)
					throw new SystemException("There was no master language for the siteNode " + getSiteNodeId());

				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
				boolean isMasterLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, siteNodeId, masterLanguageVO.getId());
				if(!isMasterLanguageValid)
				{
				    logger.info("Master language was not allowed on this sitenode... let's take the next on in order");
				    List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(db, this.getSiteNodeId());
				    Iterator languagesIterator = languages.iterator();
				    while(languagesIterator.hasNext())
				    {
				        LanguageVO currentLanguage = (LanguageVO)languagesIterator.next();
				        boolean isCurrentLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, siteNodeId, currentLanguage.getId());
					    logger.info("currentLanguage validity:" + isCurrentLanguageValid);
				        if(isCurrentLanguageValid)
				        {
				            setLanguageId(currentLanguage.getLanguageId());
				            break;
				        }
				    }
				}
				else
				{
				    logger.info("The system had no browserLanguageVO available - using master language instead:" + masterLanguageVO.getName());
				    setLanguageId(masterLanguageVO.getLanguageId());				
				}


			}
		}
		else
		{
		    LanguageVO languageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, getLanguageId(), getSiteNodeId());
		   
		    if(languageVO != null)
			{
			    logger.info("The system had browserLanguageVO available:" + languageVO.getName());
			    setLanguageId(languageVO.getLanguageId());
			}
			else
			{
				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, this.getSiteNodeId());
				if(masterLanguageVO == null)
					throw new SystemException("There was no master language for the siteNode " + getSiteNodeId());
				
				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
				boolean isMasterLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, siteNodeId, masterLanguageVO.getId());
				if(!isMasterLanguageValid)
				{
				    logger.info("Master language was not allowed on this sitenode... let's take the next on in order");
				    List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(db, this.getSiteNodeId());
				    Iterator languagesIterator = languages.iterator();
				    while(languagesIterator.hasNext())
				    {
				        LanguageVO currentLanguage = (LanguageVO)languagesIterator.next();
				        boolean isCurrentLanguageValid = LanguageDeliveryController.getLanguageDeliveryController().getIsValidLanguage(db, ndc, siteNodeId, currentLanguage.getId());
					    logger.info("currentLanguage validity:" + isCurrentLanguageValid);
				        if(isCurrentLanguageValid)
				        {
				            setLanguageId(currentLanguage.getLanguageId());
				            break;
				        }
				    }
				}
				else
				{
				    logger.info("The system had no browserLanguageVO available - using master language instead:" + masterLanguageVO.getName());
				    setLanguageId(masterLanguageVO.getLanguageId());				
				}
			}
		}
	}

	/**
	 * This method validates that the current page is accessible to the requesting user.
	 * It fetches information from the page metainfo about if the page is protected and if it is 
	 * validates the users credentials against the extranet database,
	 */
	
	public boolean handleExtranetLogic(Database db, Integer repositoryId, Integer protectedSiteNodeVersionId, boolean protectDeliver, boolean forceCmsUser) throws SystemException, Exception
	{
		Timer t = new Timer();
		
		boolean isRedirected = false;
		
		try
		{
		    String referer = this.getRequest().getHeader("Referer");
			logger.info("referer:" + referer);
			
			if(referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
				referer = "/"; 
			
			logger.info("referer after:" + referer);
			
			Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
			logger.info("principal:" + principal);

			if(getRequest().getParameter("recheckSSO") != null)
			{
				principal = null;
				this.getHttpSession().removeAttribute("infogluePrincipal");
			    this.getHttpSession().removeAttribute("infoglueRemoteUser");
			    this.getHttpSession().removeAttribute("cmsUserName");
			}

			if(principal != null && forceCmsUser && CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(principal.getName()))
			{
				if(logger.isInfoEnabled())
					logger.info("Principal in session was:" + principal + " - we clear it as only cms-users are allowed.");
				
				principal = null;
				this.getHttpSession().removeAttribute("infogluePrincipal");
			    this.getHttpSession().removeAttribute("infoglueRemoteUser");
			    this.getHttpSession().removeAttribute("cmsUserName");
			}
							
			//First we check if the user is logged in to the container context
			if(principal == null)
			{
			    if(this.getRequest().getUserPrincipal() != null && !(this.getRequest().getUserPrincipal() instanceof InfoGluePrincipal))
			    {
					Map status = new HashMap();
					status.put("redirected", new Boolean(false));
					principal = AuthenticationModule.getAuthenticationModule(db, this.getOriginalFullURL(), getRequest(), false).loginUser(getRequest(), getResponse(), status);
					Boolean redirected = (Boolean)status.get("redirected");
					if(redirected != null && redirected.booleanValue())
					{
					    this.getHttpSession().removeAttribute("infogluePrincipal");
					    this.principal = null;
					    return true;
					}
					else if(principal != null)
					{
					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
						this.getHttpSession().setAttribute("cmsUserName", principal.getName());
						
					    this.principal = principal;
					}
			    }
			}
			if(principal == null && !protectDeliver)
			{
				Principal anonymousPrincipal = getAnonymousPrincipal();

				boolean isAuthorized = true;
				if(protectedSiteNodeVersionId != null)
				{
					isAuthorized = false;
					SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(protectedSiteNodeVersionId, db);
					if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
						isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)anonymousPrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), false);
					else
						isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)anonymousPrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
				}				
				
				logger.info("isAuthorized:" + isAuthorized);
				
				if(isAuthorized)
				{	
					principal = anonymousPrincipal;
					if(principal != null)
					{
					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
					    this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
					    this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					}
				}
			}
			
			if(principal == null)
			{				
				Map status = new HashMap();
				status.put("redirected", new Boolean(false));
				principal = AuthenticationModule.getAuthenticationModule(db, this.getOriginalFullURL(), getRequest(), false).loginUser(getRequest(), getResponse(), status);
				Boolean redirected = (Boolean)status.get("redirected");
				if(redirected != null && redirected.booleanValue())
				{
				    this.getHttpSession().removeAttribute("infogluePrincipal");
				    this.principal = null;
				    return true;
				}
				else if(principal != null)
				{
				    this.getHttpSession().setAttribute("infogluePrincipal", principal);
					this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
					this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					
				    this.principal = principal;
				}
				
				if(principal == null)
					principal = loginWithCookies();
				
			    if(principal == null)
			        principal = loginWithRequestArguments();
			    
			    if(principal == null)
			    {	
			    	try
					{
			    		if(!forceCmsUser)
			    		{
							principal = getAnonymousPrincipal();
							
							if(principal != null)
							{
								this.getHttpSession().setAttribute("infogluePrincipal", principal);
								this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
								this.getHttpSession().setAttribute("cmsUserName", principal.getName());
								
								boolean isAuthorized = false;
								if(!protectDeliver)
									isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
								
								if(!isAuthorized)
								{	
									this.getHttpSession().removeAttribute("infogluePrincipal");
									logger.info("SiteNode is protected and anonymous user was not allowed - sending him to login page.");
									String redirectUrl = getRedirectUrl(getRequest(), getResponse());								
									//System.out.println("redirectUrl:" + redirectUrl);
									getResponse().sendRedirect(redirectUrl);
									isRedirected = true;
								}
							}
			    		}
			    		else
			    		{
							this.getHttpSession().removeAttribute("infogluePrincipal");
							this.getHttpSession().removeAttribute("infoglueRemoteUser");
							this.getHttpSession().removeAttribute("cmsUserName");
							logger.info("SiteNode is protected and anonymous user was not allowed - sending him to login page.");
							String redirectUrl = getRedirectUrl(getRequest(), getResponse());								
							getResponse().sendRedirect(redirectUrl);
							isRedirected = true;
			    		}
					}
					catch(Exception e) 
					{
					    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
					}
			    }
				else
				{
					boolean isAuthorized = false;
					if(protectDeliver && protectedSiteNodeVersionId == null && !principal.getName().equals(CmsPropertyHandler.getAnonymousUser()))
					{
						isAuthorized = true;
					}
					else if(protectedSiteNodeVersionId != null)
					{
						if(logger.isInfoEnabled())
							logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
						
						isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
						
						if(logger.isInfoEnabled())
							logger.info("Anonymous auth:" + isAuthorized);
						
						if(!isAuthorized)
						{
							isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
						
							if(logger.isInfoEnabled())
								logger.info("" + principal + " auth:" + isAuthorized);
						}
						
						if(logger.isInfoEnabled())
							logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
						
					}
					else if(!protectDeliver)
					{
						isAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
					}
					
					if(logger.isInfoEnabled())
					{
						logger.info("protectDeliver:" + protectDeliver);
						logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
						logger.info("isAuthorized:" + isAuthorized);
						logger.info("principal:" + principal);
					}
					
					if(!isAuthorized)
					{	
						if(this.referer == null)
							this.referer = this.getRequest().getHeader("Referer");
						
						if(this.referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
							this.referer = "/"; 

						if(principal.getName().equals(CmsPropertyHandler.getAnonymousUser()))
						{
							logger.info("SiteNode is protected and user was anonymous - sending him to login page.");
							//String url = "ExtranetLogin!loginForm.action?returnAddress=" + URLEncoder.encode(this.getRequest().getRequestURL().toString() + "?" + this.getRequest().getQueryString() + "&referer=" + URLEncoder.encode(referer, "UTF-8") + "&date=" + System.currentTimeMillis(), "UTF-8");
							String url = getRedirectUrl(getRequest(), getResponse());
							getResponse().sendRedirect(url);
							isRedirected = true;
						}
						else
						{
							logger.info("SiteNode is protected and user has no access - sending him to no access page.");
						    String noAccessURL = getNoAccessURL();
							getResponse().sendRedirect(noAccessURL);
							isRedirected = true;
						}
					}
					else
					{
						this.getHttpSession().setAttribute("infogluePrincipal", principal);
						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
						this.getHttpSession().setAttribute("cmsUserName", principal.getName());
						
						this.principal = principal;
					}
				}

			}
			else
			{
				logger.info("principal:" + principal);
				logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);

				Principal alternativePrincipal = loginWithCookies();
			    if(alternativePrincipal == null)
			        alternativePrincipal = loginWithRequestArguments();

			    SiteNodeVersionVO siteNodeVersionVO = null;
			    boolean isAlternativePrincipalAuthorized = (alternativePrincipal == null);
				boolean isPrincipalAuthorized = true;
				boolean isAnonymousPrincipalAuthorized = true;
				
			    if(protectedSiteNodeVersionId != null)
			    {
			    	siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(protectedSiteNodeVersionId, db);
				
			    	isAlternativePrincipalAuthorized = false;
			    	isPrincipalAuthorized = false;
			    	isAnonymousPrincipalAuthorized = false;
			    }
			    
			    if(siteNodeVersionVO != null && siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
				{
					if(alternativePrincipal != null)
						isAlternativePrincipalAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)alternativePrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), true);
					if(!isAlternativePrincipalAuthorized && !((InfoGluePrincipal)principal).getName().equals(((InfoGluePrincipal)this.getAnonymousPrincipal()).getName()))
						isPrincipalAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), true);
					else if(!isAlternativePrincipalAuthorized)
						isAnonymousPrincipalAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), false);
				}
				else if(siteNodeVersionVO != null)
				{
				    if(alternativePrincipal != null)
						isAlternativePrincipalAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)alternativePrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), true);
					if(!isAlternativePrincipalAuthorized && !((InfoGluePrincipal)principal).getName().equals(((InfoGluePrincipal)this.getAnonymousPrincipal()).getName()))
						isPrincipalAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), true);
					if(!isAlternativePrincipalAuthorized && !isPrincipalAuthorized)
						isAnonymousPrincipalAuthorized = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString(), false);
				}
				
			    logger.info("isAlternativePrincipalAuthorized:" + isAlternativePrincipalAuthorized);
			    logger.info("isPrincipalAuthorized:" + isPrincipalAuthorized);
			    logger.info("isAnonymousPrincipalAuthorized:" + isAnonymousPrincipalAuthorized);
			    logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
			    logger.info("prinicpal:" + principal.getName());
				
			    logger.info("Session ssoChecked:" + getHttpSession().getAttribute("ssoChecked"));
			    String ssoUserName = null;
			    if((getHttpSession().getAttribute("ssoChecked") == null || getHttpSession().getAttribute("ssoChecked").equals(""))  || getRequest().getParameter("recheckSSO") != null)
			    {
					String forceIdentityCheck = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(this.repositoryId, "forceIdentityCheck");
					if(this.principal == null || CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(this.principal.getName()))
					{
						if(CmsPropertyHandler.getForceIdentityCheck().equalsIgnoreCase("true") || (forceIdentityCheck != null && forceIdentityCheck.equalsIgnoreCase("true")))
						{
							boolean isForcedIdentityCheckDisabled = this.nodeDeliveryController.getIsForcedIdentityCheckDisabled(db, this.siteNodeId);
							if(!isForcedIdentityCheckDisabled)
							{
							    ssoUserName = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL(), this.getRequest(), false).getSSOUserName(getRequest());
								if(ssoUserName != null)
									getHttpSession().setAttribute("ssoChecked", "true");
							}
						}
					}
					//t.printElapsedTime("ssoUserName took");
			    }
			    
			    //if(protectedSiteNodeVersionId != null && alternativePrincipal != null && AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)alternativePrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
				if(protectedSiteNodeVersionId != null && isAlternativePrincipalAuthorized)
			    {
			        logger.info("The user " + alternativePrincipal.getName() + " was approved.");
			    }
				//else if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()) &&  !AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
				else if(protectedSiteNodeVersionId != null && !isPrincipalAuthorized && !isAnonymousPrincipalAuthorized || (principal.getName().equals(CmsPropertyHandler.getAnonymousUser()) && ssoUserName != null))
				{
					if(logger.isInfoEnabled())
					{
						logger.info("principal:" + principal);
						logger.info("protectedSiteNodeVersionId:" + protectedSiteNodeVersionId);
						logger.info("this.getAnonymousPrincipal():" + this.getAnonymousPrincipal());
						
						logger.info("Principal access: " + !AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()));
						logger.info("Principal access: " + !AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()));
						logger.info("Anonymous access: " + !AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)this.getAnonymousPrincipal(), "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()));
					}
					
					if(this.referer == null)
						this.referer = this.getRequest().getHeader("Referer");
					
					if(this.referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
						this.referer = "/"; 

					if(principal.getName().equals(CmsPropertyHandler.getAnonymousUser()))
					{
						//String ssoUserName = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL(), this.getRequest(), false).getSSOUserName(getRequest());
						if(ssoUserName != null)
						{
							principal = UserControllerProxy.getController().getUser(ssoUserName);
						    //t.printElapsedTime("principal from ssoUserName took");

							if(principal != null)
							{
								this.principal = principal;
							    this.getHttpSession().setAttribute("infogluePrincipal", principal);
								this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
								this.getHttpSession().setAttribute("cmsUserName", principal.getName());
								
								//---------------------------------------------------------
								// Check if the principal is authorized to view this page.
								// If not, redirect him to the unauthorized.jsp page.
								//---------------------------------------------------------
								if (protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
								{
									String url = getNoAccessURL();
									getResponse().sendRedirect(url);
									isRedirected = true;
								}
							}
						}
						else
						{
							logger.info("SiteNode is protected and user was anonymous - sending him to login page.");
							String url = getRedirectUrl(getRequest(), getResponse());
							logger.info("url:" + url);
							if(url != null)
							{
								getResponse().sendRedirect(url);
								isRedirected = true;
							}
						}
					}
					else
					{
						logger.info("SiteNode is protected and neither " + principal + " or " + this.getAnonymousPrincipal() + " has access - sending him to no access page.");
						String url = getNoAccessURL();
						getResponse().sendRedirect(url);
						isRedirected = true;
					}
				}
				else if(protectedSiteNodeVersionId == null && protectDeliver && !forceCmsUser)
				{
					logger.info("Setting user to anonymous... as this is a protected deliver but not a extranet...");
					Principal anonymousPrincipal = getAnonymousPrincipal();
					
					//this.getHttpSession().setAttribute("infogluePrincipal", anonymousPrincipal);
					//this.getHttpSession().setAttribute("infoglueRemoteUser", anonymousPrincipal.getName());
					//this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					
					this.principal = anonymousPrincipal;
				}
			}
		}
		catch(SystemException se)
		{
			logger.warn("An error occurred:" + se.getMessage(), se);
			throw se;
		}
		catch(Exception e)
		{
			logger.error("An error occurred:" + e.getMessage(), e);
		}
		
		return isRedirected;
	}

	/**
	 * This method gets an no access url and has some logic about it. First it defaults to a standard url. If a different is stated in a repo property called noAccessURL 
	 * it uses that and if the url there is $referer it uses the http referer header and just adds a parameter.
	 * @return
	 * @throws Exception
	 */
	public String getNoAccessURL() throws Exception
	{
		String noAccessURL = "ExtranetLogin!noAccess.action?referer=" + URLEncoder.encode(this.referer, "UTF-8") + "&date=" + System.currentTimeMillis();
	    
		try
		{
		    String repositoryNoAccessURL = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(this.repositoryId, "noAccessURL");
		    logger.info("repositoryNoAccessURL:" + repositoryNoAccessURL);
			if(repositoryNoAccessURL != null && !repositoryNoAccessURL.equals(""))
			{
				if(repositoryNoAccessURL.equalsIgnoreCase("$referer") && this.referer != null && !this.referer.equals(""))
				{
					if(this.referer.indexOf("?") > -1)
						repositoryNoAccessURL = this.referer + "&noAccess=true&date=" + System.currentTimeMillis();
					else
						repositoryNoAccessURL = this.referer + "?noAccess=true&date=" + System.currentTimeMillis();
					logger.info("this.referer:" + this.referer);
					logger.info("repositoryNoAccessURL 2:" + repositoryNoAccessURL);
				}
				else
				{
					if(repositoryNoAccessURL.indexOf("?") > -1)
						repositoryNoAccessURL = repositoryNoAccessURL + "&noAccess=true&referer=" + URLEncoder.encode(this.referer, "UTF-8") + "&date=" + System.currentTimeMillis();
					else
						repositoryNoAccessURL = repositoryNoAccessURL + "?noAccess=true&referer=" + URLEncoder.encode(this.referer, "UTF-8") + "&date=" + System.currentTimeMillis();
					logger.info("2 this.referer:" + this.referer);
					logger.info("2 repositoryNoAccessURL 2:" + repositoryNoAccessURL);
				}
				
				noAccessURL = repositoryNoAccessURL;
				logger.info("noAccessURL:" + noAccessURL);
			}
		}
		catch (Exception e) 
		{
			logger.warn("Problem reading repo specific noAccessURL: " + e.getMessage(), e);
		}
	
		return noAccessURL;
	}
	
	
	private boolean authenticateUser(Database db) throws Exception
	{
		Map status = new HashMap();
		status.put("redirected", new Boolean(false));
		//getRequest().setAttribute("gateway", "" + true);
		String originalFullURL = this.getOriginalFullURL();
		
		int index = originalFullURL.indexOf("authenticateUser=true&");
		if(index > -1)
			originalFullURL = originalFullURL.replaceAll("authenticateUser=true&","");
		else
		{
			int index2 = originalFullURL.indexOf("authenticateUser=true");
			if(index2 > -1)
				originalFullURL = originalFullURL.replaceAll("authenticateUser=true","");
		}
		if(originalFullURL.endsWith("?"))
			originalFullURL = originalFullURL.substring(0, originalFullURL.length() - 1);
		
		//System.out.println("originalFullURL:" + originalFullURL);
		
		principal = AuthenticationModule.getAuthenticationModule(db, originalFullURL, this.getRequest(), false).loginUser(getRequest(), getResponse(), status);
		//System.out.println("principal in AAAAA:" + principal);
		Boolean redirected = (Boolean)status.get("redirected");
		//System.out.println("principal in AAAAA:" + principal);
		if(redirected != null && redirected.booleanValue())
		{
			logger.info("Redirected....");
			this.getHttpSession().removeAttribute("ssoChecked");
			this.getHttpSession().removeAttribute("infogluePrincipal");
		    this.principal = null;
		    return true;
		}
		else if(principal != null)
		{
			logger.info("NOT Redirected....");
		    this.getHttpSession().setAttribute("infogluePrincipal", principal);
			this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
			this.getHttpSession().setAttribute("cmsUserName", principal.getName());
			
		    this.principal = principal;
		}
		else
		{
			String redirectUrl = getRedirectUrl(getRequest(), getResponse());								
			getResponse().sendRedirect(redirectUrl);
			redirected = true;
		}

		return redirected;
	}
	
	
	/**
	 * This method validates that the current page is accessible to the requesting user.
	 * It fetches information from the page metainfo about if the page is protected and if it is 
	 * validates the users credentials against the extranet database,
	 */
	
	public boolean handleExtranetLogic(Database db, boolean gateway) throws SystemException, Exception
	{
		boolean isRedirected = false;
		
		try
		{
			logger.info("principal: " + principal);
			if(principal == null)
			{
				String skipSSOCheck = this.getRequest().getParameter("skipSSOCheck");
				String ticket = this.getRequest().getParameter("ticket");
				if((skipSSOCheck != null && !skipSSOCheck.equals("")) && (ticket == null || ticket.equals("")))
				{
					logger.info("Setting user to anonymous....");
					principal = getAnonymousPrincipal();
					
					if(principal != null)
					{
						this.getHttpSession().setAttribute("infogluePrincipal", principal);
						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
						this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					}
					
					getHttpSession().setAttribute("ssoChecked", "true");
					return isRedirected;
				}
			}			

			
		    String referer = this.getRequest().getHeader("Referer");
			logger.info("referer:" + referer);
			
			if(referer == null || referer.indexOf("ViewStructureToolToolBar.action") != -1)
				referer = "/"; 
			
			Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
			logger.info("principal:" + principal);

			if((principal == null || principal.getName().equals(CmsPropertyHandler.getAnonymousUser())) && getHttpSession().getAttribute("ssoChecked") == null && (getRequest().getParameter("skipSSOCheck") != null && !getRequest().getParameter("skipSSOCheck").equals("true")))	
			{
				logger.info("Nulling user as we want SSO check to be done..");
			    if(this.getRequest().getUserPrincipal() != null && !(this.getRequest().getUserPrincipal() instanceof InfoGluePrincipal))
			    {
					Map status = new HashMap();
					status.put("redirected", new Boolean(false));
					getRequest().setAttribute("gateway", "" + gateway);
					principal = AuthenticationModule.getAuthenticationModule(db, this.getOriginalFullURL(), this.getRequest(), false).loginUser(getRequest(), getResponse(), status);
					logger.info("principal:" + principal + " after gateway test");
					Boolean redirected = (Boolean)status.get("redirected");
					logger.info("redirected:" + redirected);
					if(redirected != null && redirected.booleanValue())
					{
					    this.getHttpSession().removeAttribute("infogluePrincipal");
					    this.principal = null;
					    return true;
					}
					else if(principal != null)
					{
					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
						this.getHttpSession().setAttribute("cmsUserName", principal.getName());
						
					    this.principal = principal;
					}
			    }
			    //getHttpSession().setAttribute("ssoChecked", "true");
			}
			

			//First we check if the user is logged in to the container context
			if(principal == null)
			{
			    if(this.getRequest().getUserPrincipal() != null && !(this.getRequest().getUserPrincipal() instanceof InfoGluePrincipal))
			    {
					Map status = new HashMap();
					status.put("redirected", new Boolean(false));
					getRequest().setAttribute("gateway", "" + gateway);
					principal = AuthenticationModule.getAuthenticationModule(db, this.getOriginalFullURL(), this.getRequest(), false).loginUser(getRequest(), getResponse(), status);
					Boolean redirected = (Boolean)status.get("redirected");
					if(redirected != null && redirected.booleanValue())
					{
					    this.getHttpSession().removeAttribute("infogluePrincipal");
					    this.principal = null;
					    return true;
					}
					else if(principal != null)
					{
					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
						this.getHttpSession().setAttribute("cmsUserName", principal.getName());
						
					    this.principal = principal;
					}
			    }
			}
					
			if(principal == null)
			{				
				Map status = new HashMap();
				status.put("redirected", new Boolean(false));
				getRequest().setAttribute("gateway", "" + gateway);
				principal = AuthenticationModule.getAuthenticationModule(db, this.getOriginalFullURL(), this.getRequest(), false).loginUser(getRequest(), getResponse(), status);
				Boolean redirected = (Boolean)status.get("redirected");
				if(redirected != null && redirected.booleanValue())
				{
				    this.getHttpSession().removeAttribute("infogluePrincipal");
				    this.principal = null;

				    return true;
				}
				else if(principal != null)
				{
				    this.getHttpSession().setAttribute("infogluePrincipal", principal);
					this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
					this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					
				    this.principal = principal;
				}
				
				if(principal == null)
					principal = loginWithCookies();
				
			    if(principal == null)
			        principal = loginWithRequestArguments();
			    
			    if(principal == null)
			    {	
			    	try
					{
						principal = getAnonymousPrincipal();
						
						if(principal != null)
						{
							this.getHttpSession().setAttribute("infogluePrincipal", principal);
							this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
							this.getHttpSession().setAttribute("cmsUserName", principal.getName());
						}
					}
					catch(Exception e) 
					{
					    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
					}
			    }
				else
				{
					this.getHttpSession().setAttribute("infogluePrincipal", principal);
					this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
					this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					
					this.principal = principal;
				}

			}
			else
			{
				logger.info("principal:" + principal);

				Principal alternativePrincipal = loginWithCookies();
			    if(alternativePrincipal == null)
			        alternativePrincipal = loginWithRequestArguments();
			}
		}
		catch(SystemException se)
		{
			logger.warn("An error occurred:" + se.getMessage(), se);
			throw se;
		}
		catch(Exception e)
		{
			logger.error("An error occurred:" + e.getMessage(), e);
		}
				
		return isRedirected;
	}
	
	/**
	 * This method (if enabled in deliver.properties) checks for authentication cookies and 
	 * logs the user in if available.
	 * 
	 * @return Principal
	 * @throws Exception
	 */
	private Principal loginWithCookies() throws Exception
	{
	    Principal principal = null;
	    
	    boolean enableExtranetCookies = false;
	    int extranetCookieTimeout = 43200; //30 days default
	    String enableExtranetCookiesString = CmsPropertyHandler.getEnableExtranetCookies();
	    String extranetCookieTimeoutString = CmsPropertyHandler.getExtranetCookieTimeout();
	    if(enableExtranetCookiesString != null && enableExtranetCookiesString.equalsIgnoreCase("true"))
	    {
	        enableExtranetCookies = true;
	    }
	    if(extranetCookieTimeoutString != null)
	    {
	        try
		    {
	            extranetCookieTimeout = Integer.parseInt(extranetCookieTimeoutString.trim());
		    }
	        catch(Exception e) {}
		}
	
	    if(enableExtranetCookies)
	    {
	        String userName = null;
		    String password = null;
		    Cookie[] cookies = this.getRequest().getCookies();
		    if(cookies != null)
		    {
			    for(int i=0; i<cookies.length; i++)
			    {
			        Cookie cookie = cookies[i];
			        if(cookie.getName().equals("igextranetuserid"))
			            userName = cookie.getValue();
			        else if(cookie.getName().equals("igextranetpassword"))
			            password = cookie.getValue();
			    }
		    }
		    
		    if(userName != null && password != null)
		    {
		    	DesEncryptionHelper encHelper = new DesEncryptionHelper();
		        userName = encHelper.decrypt(userName);
		        password = encHelper.decrypt(password);

		        Map arguments = new HashMap();
			    arguments.put("j_username", userName);
			    arguments.put("j_password", password);
			    
				principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, this.getRequest());
				if(principal != null)
				{
				    this.getHttpSession().setAttribute("infogluePrincipal", principal);
					this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
					this.getHttpSession().setAttribute("cmsUserName", principal.getName());
				}
			    
		    }
	    }

	    if(principal == null)
	    {
	    	HttpHelper httpHelper = new HttpHelper();
			String encodedUserNameCookie = httpHelper.getCookie(this.getRequest(), "iguserid");
			logger.info("encodedUserNameCookie:" + encodedUserNameCookie);
			/*
			if(logger.isInfoEnabled())
			{
				Enumeration attributeNames = ActionContext.getServletContext().getAttributeNames();
				while(attributeNames.hasMoreElements())
				{
					String attribute = (String)attributeNames.nextElement();
					Object value = ActionContext.getServletContext().getAttribute(attribute);
					logger.info("" + attribute + " = " + value);
				}
			}
			*/
			if(encodedUserNameCookie != null && !encodedUserNameCookie.equals(""))
			{
				encodedUserNameCookie = encodedUserNameCookie.replaceAll("IGEQ", "=");
			    logger.info("encodedUserNameCookie2:" + encodedUserNameCookie);
				String servletContextUserName = (String)ActionContext.getServletContext().getAttribute(encodedUserNameCookie);
				logger.info("servletContextUserName:" + servletContextUserName);
				if(servletContextUserName != null && !servletContextUserName.equals(""))
				{
					principal = getAuthenticatedUser(servletContextUserName);
					//System.out.println("principal:" + principal);
					if(principal != null)
					{
					    this.getHttpSession().setAttribute("infogluePrincipal", principal);
						this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
						this.getHttpSession().setAttribute("cmsUserName", principal.getName());
					}
				}
			}
	    }

	    return principal;
	}
	
	/**
	 * This method (if enabled in deliver.properties) checks for arguments in the request
	 * and logs the user in if available.
	 * 
	 * @return Principal
	 * @throws Exception
	 */
	private Principal loginWithRequestArguments() throws Exception
	{
	    Principal principal = null;
	    
        String userName = this.getRequest().getParameter("j_username");
	    String password = this.getRequest().getParameter("j_password");
	    String ticket 	= null; //this.getRequest().getParameter("ticket");
	    //System.out.println("userName:" + userName);
	    //System.out.println("password:" + password);
	    //System.out.println("ticket:" + ticket);
	    
		if(ticket != null)
	    {
			//System.out.println("ticket used in loginWithRequestArguments:" + ticket);
		    Map arguments = new HashMap();
		    arguments.put("ticket", ticket);
		    
			principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, this.getRequest());
			if(principal != null)
			{
			    this.getHttpSession().setAttribute("infogluePrincipal", principal);
				this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
				this.getHttpSession().setAttribute("cmsUserName", principal.getName());
			}
	    }		    
	    else if(userName != null && password != null)
	    {
		    Map arguments = new HashMap();
		    arguments.put("j_username", userName);
		    arguments.put("j_password", password);
		    
			principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, this.getRequest());
			if(principal != null)
			{
			    this.getHttpSession().setAttribute("infogluePrincipal", principal);
				this.getHttpSession().setAttribute("infoglueRemoteUser", principal.getName());
				this.getHttpSession().setAttribute("cmsUserName", principal.getName());
			}
	    }
	    
	    return principal;
	}


	/**
	 * Gets the SiteNodeType definition of this given node
	 * @return
	 */
	private SiteNodeTypeDefinitionVO getSiteNodeTypeDefinition(Integer siteNodeId, Database db) throws SystemException, Exception
	{
	    String key = "" + siteNodeId;
		logger.info("key:" + key);
		//SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO = (SiteNodeTypeDefinitionVO)CacheController.getCachedObjectFromAdvancedCache("pageCacheSiteNodeTypeDefinition", key);
		SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO = (SiteNodeTypeDefinitionVO)CacheController.getCachedObject("pageCacheSiteNodeTypeDefinition", key);
		if(siteNodeTypeDefinitionVO != null)
		{
			logger.info("There was an cached siteNodeTypeDefinitionVO:" + siteNodeTypeDefinitionVO);
		}
		else
		{
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(getSiteNodeId(), db);

			if(siteNodeVO == null)
			    throw new SystemException("There was no page with this id.");
				
			Integer siteNodeTypeDefinitionId = siteNodeVO.getSiteNodeTypeDefinitionId();
			try
			{
				siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(siteNodeTypeDefinitionId, db);
			}
			catch (Exception e) 
			{
			    throw new SystemException("There was no valid page invoker class assigned to the site node " + siteNodeVO.getName() + " - was (" + siteNodeTypeDefinitionId + ")");
			}
			
			/*
		    SiteNode siteNode = nodeDeliveryController.getSiteNode(db, this.siteNodeId);
			if(siteNode == null)
			    throw new SystemException("There was no page with this id.");
			
			if(siteNode.getSiteNodeTypeDefinition() == null || siteNode.getSiteNodeTypeDefinition().getInvokerClassName() == null || siteNode.getSiteNodeTypeDefinition().getInvokerClassName().equals(""))
			{
			    throw new SystemException("There was no page invoker class assigned to the site node " + siteNode.getName());
			}
			
			siteNodeTypeDefinitionVO = siteNode.getSiteNodeTypeDefinition().getValueObject();
			*/
			
			CacheController.cacheObject("pageCacheSiteNodeTypeDefinition", key, siteNodeTypeDefinitionVO);
			//CacheController.cacheObjectInAdvancedCache("pageCacheSiteNodeTypeDefinition", key, siteNodeTypeDefinitionVO);
		}
		
		return siteNodeTypeDefinitionVO;
	}

  	private String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) throws ServletException, Exception 
  	{
		String url = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL(), this.getRequest(), false).getLoginDialogUrl(request, response);

		int index = url.indexOf("authenticateUser%3Dtrue%26");
		if(index > -1)
			url = url.replaceAll("authenticateUser%3Dtrue%26","");
		else
		{
			int index2 = url.indexOf("authenticateUser%3Dtrue");
			if(index2 > -1)
				url = url.replaceAll("authenticateUser%3Dtrue","");
		}
		if(url.endsWith("%3F"))
			url = url.substring(0, url.length() - 3);
		
		String repositoryLoginUrl = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(repositoryId, "loginUrl");
		//System.out.println("repositoryLoginUrl:" + repositoryLoginUrl);
		if(repositoryLoginUrl != null && !repositoryLoginUrl.equals(""))
		{
			String originalFullURL = this.getOriginalFullURL();
			int index2 = originalFullURL.indexOf("authenticateUser=true&");
			if(index2 > -1)
				originalFullURL = originalFullURL.replaceAll("authenticateUser=true&","");
			else
			{
				int index3 = originalFullURL.indexOf("authenticateUser=true");
				if(index3 > -1)
					originalFullURL = originalFullURL.replaceAll("authenticateUser=true","");
			}
			if(originalFullURL.endsWith("?"))
				originalFullURL = originalFullURL.substring(0, url.length() - 1);

			String returnAddress = originalFullURL;
			url = repositoryLoginUrl + (repositoryLoginUrl.indexOf("?") > -1 ? "&" : "?") + "returnAddress=" + URLEncoder.encode(returnAddress, "UTF-8");
		}
		
		return url;
  	}

  	private String getErrorUrl() throws Exception 
  	{
  		String errorUrl = CmsPropertyHandler.getErrorUrl();
  		
		String repositoryErrorUrl = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(repositoryId, "errorUrl");
		if(repositoryErrorUrl != null && !repositoryErrorUrl.equals(""))
		{
			errorUrl = repositoryErrorUrl;
		}
		
		return errorUrl;
  	}

	/**
	 * This method returns the exact full url excluding query string from the original request - not modified
	 * @return
	 */
	
	public String getOriginalURI()
	{
    	String originalRequestURI = this.getRequest().getParameter("originalRequestURI");
    	if(originalRequestURI == null || originalRequestURI.length() == 0)
    		originalRequestURI = this.getRequest().getRequestURI().toString();

    	return originalRequestURI;
	}

	/**
	 * This method returns the exact full url excluding query string from the original request - not modified
	 * @return
	 */
	
	public String getOriginalURL()
	{
    	String originalRequestURL = this.getRequest().getParameter("originalRequestURL");
    	if(originalRequestURL == null || originalRequestURL.length() == 0)
    		originalRequestURL = this.getRequest().getRequestURL().toString();

    	return originalRequestURL;
	}

	/**
	 * This method returns the exact querystring from the original request - not modified
	 * @return
	 */
	
	public String getOriginalQueryString()
	{
    	String originalQueryString = this.getRequest().getParameter("originalQueryString");
    	if(originalQueryString == null || originalQueryString.length() == 0)
    		originalQueryString = this.getRequest().getQueryString();

    	return originalQueryString;
	}

	/**
	 * This method returns the exact full url from the original request - not modified
	 * @return
	 */
	
	public String getOriginalFullURL()
	{
    	String originalRequestURL = getOriginalURL();
    	String originalQueryString = getOriginalQueryString();

    	return originalRequestURL + (originalQueryString == null ? "" : "?" + originalQueryString);
	}

  	/**
  	 * This method fetches the roles and other stuff for the user by invoking the autorizer-module.
  	 */
  	
	private InfoGluePrincipal getAuthenticatedUser(String userName) throws ServletException, Exception 
	{
	    String authenticatorClass = CmsPropertyHandler.getServerNodeProperty("deliver", "authenticatorClass", true, null);
	    String authorizerClass 	= CmsPropertyHandler.getServerNodeProperty("deliver", "authorizerClass", true, null);

	    Properties extraProperties	= null;
	    
	    String extraPropertiesString = CmsPropertyHandler.getServerNodeDataProperty("deliver", "extraSecurityParameters", true, null);
	    if(extraPropertiesString != null)
		{
		    logger.info("Loading extra properties from propertyset. extraPropertiesString:" + extraPropertiesString);
	    	try
			{
	    		extraProperties = new Properties();
				extraProperties.load(new ByteArrayInputStream(extraPropertiesString.getBytes("UTF-8")));
				//extraProperties.list(System.out);
			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}
		}

		AuthorizationModule authorizationModule = null;
		try
		{
			authorizationModule = (AuthorizationModule)Class.forName(authorizerClass).newInstance();
		}
		catch(Exception e)
		{
			logger.error("The authorizationModule-class was wrong:" + e.getMessage() + ": defaulting to infoglue:s own", e);
			authorizationModule = (AuthorizationModule)Class.forName(InfoGlueBasicAuthorizationModule.class.getName()).newInstance();
		}
		
		authorizationModule.setExtraProperties(extraProperties);
		logger.info("authorizerClass:" + authorizerClass + ":" + authorizationModule.getClass().getName());
		
		InfoGluePrincipal infoGluePrincipal = authorizationModule.getAuthorizedInfoGluePrincipal(userName);
		logger.info("infoGluePrincipal:" + infoGluePrincipal);
		if(infoGluePrincipal != null)
		{
			logger.info("roles:" + infoGluePrincipal.getRoles());
			logger.info("groups:" + infoGluePrincipal.getGroups());
		}
		
		return infoGluePrincipal;		
  	}
	/**
	 * Setters and getters for all things sent to the page in the request
	 */
	
    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeId;
    }
        
    public void setSiteNodeId(Integer siteNodeId)
    {
        this.siteNodeId = siteNodeId;
	}

    public Integer getContentId()
    {
        return this.contentId;
    }
        
    public void setContentId(Integer contentId)
    {
    	this.contentId = contentId;
    }

    public Integer getLanguageId()
    {
        return this.languageId;
    }
        
    public void setLanguageId(Integer languageId)
    {
		this.languageId = languageId;   
	}

    public String getRepositoryName()
    {
        return this.repositoryName;
    }
        
    public void setRepositoryName(String repositoryName)
    {
	    this.repositoryName = repositoryName;
    }
    
	public String getReferer()
	{
		return referer;
	}

	public void setReferer(String referer)
	{
		this.referer = referer;
	}

    public void setShowSimple(boolean showSimple)
    {
        this.showSimple = showSimple;
    }
    
    public void setRecacheCall(boolean isRecacheCall)
    {
        this.isRecacheCall = isRecacheCall;
    }
    
    public void setCmsUserName(String userName)
    {
    	if(logger.isInfoEnabled())
    		logger.info("userName:" + userName);
    	
        this.getHttpSession().setAttribute("cmsUserName", userName);
    }
    
	/**
	 * This method removes sessions just created if it was a user agent (bot) matched that we don't think reuses sessions anyway. It would only hog memory.
	 */
	public void killUnwantedSessions() 
	{
		try
		{
			if(browserBean.getUseragent() != null)
			{
				Pattern pattern = null;
				String userAgentsRegexp = null;
				try
				{
					Map cacheSettings = CmsPropertyHandler.getCacheSettings();
					//System.out.println("cacheSettings:" + cacheSettings);
			    	if(cacheSettings != null)
			    	{
			    		userAgentsRegexp = (String)cacheSettings.get("KILL_SESSION_FOR_USERAGENTSMATCHING");
				    	if(userAgentsRegexp != null && !userAgentsRegexp.equals(""))
							pattern = Pattern.compile(userAgentsRegexp);
				    }
				}
				catch (Exception e) 
				{
					logger.warn("cacheSettings was null:" + e.getMessage(), e);
				}

				if(logger.isInfoEnabled())
				{
					logger.info("userAgentsRegexp:" + userAgentsRegexp);
				}
				
				if(pattern != null)
				{
			        Matcher matcher = pattern.matcher(browserBean.getUseragent());
			        if(matcher.find())
			    	{
			        	logger.info("Killing session from:" + browserBean.getUseragent());
			        	this.getHttpSession().invalidate();
			    	}
			    	matcher.reset();
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Problem with session validation check");
		}
	}
}
