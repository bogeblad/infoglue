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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.CmsSessionContextListener;
import org.infoglue.cms.util.sorters.AverageInvokingTimeComparator;
import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
import org.infoglue.deliver.portal.ServletConfigContainer;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;

import webwork.action.ActionContext;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.OSCacheUtility;
import com.opensymphony.oscache.web.ServletCache;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

/**
 * This is the action that shows the application state and also can be used to set up surveilence.
 * The idea is to have one command which allways returns a known resultpage if it's ok. Otherwise it prints
 * an error-statement. This action is then called every x minutes by the surveilence and an alarm is raised if something is wrong.
 * We also have a command which can list more status about the application.
 *
 * @author Mattias Bogeblad
 */

public class ViewApplicationStateAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(ViewApplicationStateAction.class.getName());

    private List states 					= new ArrayList();
    private Map applicationMap 				= new HashMap();
    private Object cache					= null;
    
	private boolean databaseConnectionOk 	= false;
	private boolean applicationSettingsOk 	= false;
	private boolean testQueriesOk			= false;
	private boolean diskPermissionOk 		= false;
	
	private String cacheName				= "";
	private boolean clearFileCache			= false;

	private String className				= "";
	private String logLevel					= "";

	private String attributeName			= "";
	private String returnAddress			= null;

	private static VisualFormatter formatter = new VisualFormatter();
	
	/**
	 * The constructor for this action - contains nothing right now.
	 */
    
    public ViewApplicationStateAction() 
    {
	
    }
    
    private Category getDeliverCategory()
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase("org.infoglue.deliver"))
                return category;
        }
        
        return null;
    }

    private Category getCastorJDOCategory()
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase("org.exolab.castor.jdo"))
                return category;
        }
        
        return null;
    }

    private Category getCategory(String className)
    {
        Enumeration enumeration = Logger.getCurrentCategories();
        while(enumeration.hasMoreElements())
        {
            Category category = (Category)enumeration.nextElement();
            if(category.getName().equalsIgnoreCase(className))
                return category;
        }
        
        Category category = Category.getInstance(className);
       
        return category;
    }

	private String handleAccess(HttpServletRequest request) throws Exception
	{
		String returnValue = null;
		
        boolean allowAccess = false;
        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
        {
			Principal principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");
        	logger.info("principal:" + principal);
        	Principal anonymousPrincipal = this.getAnonymousPrincipal();
        	if(principal == null || principal.getName().equals(anonymousPrincipal.getName()))
        	{
            	this.getHttpSession().removeAttribute("infogluePrincipal");
    		    this.getHttpSession().removeAttribute("infoglueRemoteUser");
    		    this.getHttpSession().removeAttribute("cmsUserName");

    		    String redirectUrl = getRedirectUrl(getRequest(), getResponse());								
    			getResponse().sendRedirect(redirectUrl);
    			returnValue = NONE;
        	}
        	else
        	{
				if(AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "ViewApplicationState.Read", false, true))
				{
					allowAccess = true;
				}
				else
				{
		        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") and username [" + principal + "] was denied access to ViewApplicationState.");

		        	this.getHttpSession().removeAttribute("infogluePrincipal");
	    		    this.getHttpSession().removeAttribute("infoglueRemoteUser");
	    		    this.getHttpSession().removeAttribute("cmsUserName");

	    		    this.getResponse().setContentType("text/plain");
		            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
		            this.getResponse().getWriter().println("You have no access to this view as you don't have ViewApplicationState.Read-rights.");				
		            returnValue = NONE;
				}
        	}
        	
		    if(!allowAccess)
	        {
	        	logger.warn("A user from an IP(" + this.getRequest().getRemoteAddr() + ") and username [" + principal + "] was denied access to ViewApplicationState.");
		    	this.getResponse().setContentType("text/plain");
		        this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");				
	            returnValue = NONE;
	        }
        }

		return returnValue;
	}

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearCache(cacheName);
        if(clearFileCache || cacheName.equals("pageCache"))
        	CacheController.clearFileCaches(cacheName);
        
        //this.getHttpSession().invalidate();
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }
 
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doFlushCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.flushCache(cacheName);
        
        //this.getHttpSession().invalidate();
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }
 
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearCacheStartingWith() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        Map caches = getCaches();
        Iterator cachesIterator = caches.keySet().iterator();
        while(cachesIterator.hasNext())
        {
        	String cacheName = (String)cachesIterator.next();
        	if(cacheName.startsWith(getRequest().getParameter("cacheNamePrefix")))
        		CacheController.clearCache(cacheName);
        }
        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }

        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearPageCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearFileCaches("pageCache");
        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }

        
        return "cleared";
    }
	

    
    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearCastorCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        CacheController.clearCache(Class.forName(className));
        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doClearApplicationCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        if(attributeName != null && attributeName.equals("all"))
        {
        	getApplicationAttributes();
        	Iterator applicationAttributesIterator = this.applicationMap.keySet().iterator();
        	while(applicationAttributesIterator.hasNext())
        	{
        		String attributeName = (String)applicationAttributesIterator.next();
        		ActionContext.getServletContext().removeAttribute(attributeName);        	
        	}
        }
        else if(attributeName != null)
        {
        	ActionContext.getServletContext().removeAttribute(attributeName);
        }
        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doRestoreWorkflows() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        WorkflowController.restoreSessionFactory(null);
        
        //this.getHttpSession().invalidate();
        
        return "cleared";
    }

    /**
     * This action allows clearing of the given cache manually.
     */
    public String doDecreaseActiveCount() throws Exception
    {
        if(CmsPropertyHandler.getOperatingMode().equalsIgnoreCase("3"))
        {
            this.getResponse().setContentType("text/plain");
            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
            
            return NONE;
        }
        
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(1000);
        
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on any class.
     */
    public String doSetLogLevel() throws Exception
    {
    	Level newLevel = Level.ERROR;
    	if(this.logLevel.equalsIgnoreCase("debug"))
    		newLevel = Level.DEBUG;
    	if(this.logLevel.equalsIgnoreCase("info"))
    		newLevel = Level.INFO;
    	else if(this.logLevel.equalsIgnoreCase("warn"))
    		newLevel = Level.WARN;
    	else if(this.logLevel.equalsIgnoreCase("error"))
    		newLevel = Level.ERROR;
    	
    	Category category = getCategory(this.className);
    	if(category != null)
    	{
    		category.setLevel(newLevel);
    		
			Enumeration enumeration = Logger.getLogger("org.infoglue.console-debug-dummy").getAllAppenders();
	        while(enumeration.hasMoreElements())
	        {
	        	Appender appender = (Appender)enumeration.nextElement();
	           	category.addAppender(appender);
	            break;
	        }

    	}
    	
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on some basic classes.
     */
    public String doSetLogInfo() throws Exception
    {
        //ViewPageFilter.logger.setLevel(Level.INFO);
        //ViewPageAction.logger.setLevel(Level.INFO);
        //RedirectFilter.logger.setLevel(Level.INFO);
        CastorDatabaseService.logger.setLevel(Level.INFO);
        CacheController.logger.setLevel(Level.INFO);
        getDeliverCategory().setLevel(Level.INFO);
        getCastorJDOCategory().setLevel(Level.INFO);
        
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on some basic classes.
     */
    public String doSetLogWarning() throws Exception
    {
        //ViewPageFilter.logger.setLevel(Level.WARN);
        //ViewPageAction.logger.setLevel(Level.WARN);
        //RedirectFilter.logger.setLevel(Level.WARN);
        CastorDatabaseService.logger.setLevel(Level.WARN);
        CacheController.logger.setLevel(Level.WARN);
        getDeliverCategory().setLevel(Level.WARN);
        getCastorJDOCategory().setLevel(Level.WARN);
        
        return "cleared";
    }

    /**
     * This action allows setting of the loglevel on some basic classes.
     */
    public String doSetLogError() throws Exception
    {
        //ViewPageFilter.logger.setLevel(Level.ERROR);
        //ViewPageAction.logger.setLevel(Level.ERROR);
        //RedirectFilter.logger.setLevel(Level.ERROR);
        CastorDatabaseService.logger.setLevel(Level.ERROR);
        CacheController.logger.setLevel(Level.ERROR);
        getDeliverCategory().setLevel(Level.ERROR);
        getCastorJDOCategory().setLevel(Level.ERROR);

        return "cleared";
    }

    /**
     * This action allows clearing of the caches manually.
     */
    public String doClearCaches() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        CacheController.clearServerNodeProperty(true);
        CacheController.clearCastorCaches();
        CacheController.clearCaches(null, null, null);
        //CacheController.resetSpecial();
        if(clearFileCache)
        	CacheController.clearFileCaches();
        
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
            this.getResponse().sendRedirect(this.returnAddress);
            
            return NONE;
        }
        	
        return "cleared";
    }

    /**
     * This action allows clearing of the castor caches manually.
     */
    public String doClearCastorCaches() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        CacheController.clearCastorCaches();
        
        return "cleared";
    }

    public String doClearOSCaches() throws Exception
	{
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

		ServletCacheAdministrator servletCacheAdministrator = ServletCacheAdministrator.getInstance(ActionContext.getServletContext());
		servletCacheAdministrator.flushAll();
		Cache cache = servletCacheAdministrator.getAppScopeCache(ActionContext.getServletContext());
		
		OSCacheUtility.clear(cache);
		
		return "cleared";
	}
    /**
     * This action allows recaching of some parts of the caches manually.
     */
    public String doClearPortlets() throws Exception
    {
        try 
        {
        	
     		//run registry services to load new portlet info from the registry files
        	String[] svcs = {
     				"org.apache.pluto.portalImpl.services.portletdefinitionregistry.PortletDefinitionRegistryService",
     				"org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistryService"};
     		int len = svcs.length;
     		for (int i = 0; i < len; i++) {				
	 			try {
					ServiceManager.hotInit(ServletConfigContainer.getContainer().getServletConfig(), svcs[i]);
	 			} catch (Throwable e) {
	 				String svc = svcs[i].substring(svcs[i].lastIndexOf('.') + 1);
	 				String msg = "Initialization of " + svc + " service for hot deployment failed!"; 
	 				logger.error(msg);
	 				break;
	 			}
	 	
	 			try {
	 				logger.error("ServletConfigContainer.getContainer().getServletConfig():" + ServletConfigContainer.getContainer().getServletConfig());
	 				logger.error("ServletConfigContainer.getContainer().getServletConfig().getServletContext():" + ServletConfigContainer.getContainer().getServletConfig().getServletContext());
	 				logger.error("svcs[i]:" + svcs[i]);
					ServiceManager.postHotInit(ServletConfigContainer.getContainer().getServletConfig(), svcs[i]);
	 			} catch (Throwable e) {
	 				String svc = svcs[i].substring(svcs[i].lastIndexOf('.') + 1);
	 				String msg = "Post initialization of " + svc + " service for hot deployment failed!"; 
	 				logger.error(msg);
	 				break;
	 			}
			}

        	
            PortletContainerServices.prepare("infoglueAVote");
            ServiceManager.init(ServletConfigContainer.getContainer().getServletConfig());
        
            PortletEntityRegistry.load();

        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return "cleared";
    }
    
    /**
     * This action allows recaching of some parts of the caches manually.
     */
    public String doReCache() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        CacheController.cacheCentralCastorCaches();
        
        return "cleared";        
    }
    
    private List getList(String key, Object value)
    {
        List list = new ArrayList();
        list.add(key);
        list.add(value);

        return list;
    }
    
    public String doGC() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        Runtime.getRuntime().gc();
        
        return doExecute();
    }

    public String doResetAverageResponseTimeStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        RequestAnalyser.resetAverageResponseTimeStatistics();
        
        return "cleared";
    }

    public String doResetComponentStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        RequestAnalyser.resetComponentStatistics();
        
        return "cleared";
    }

    public String doResetPageStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        RequestAnalyser.resetPageStatistics();
        
        return "cleared";
    }

    public String doComponentStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        states.add(getList("Average processing time per request (ms)", "" + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime()));
		
        states.add(getList("<br/><strong>Individual components (in milliseconds)</strong>", "&nbsp;"));
        
        List unsortedComponents = new ArrayList();
        Set componentNames = RequestAnalyser.getAllComponentNames();
        Iterator componentNamesIterator = componentNames.iterator();
        while(componentNamesIterator.hasNext())
        {
        	String componentName = (String)componentNamesIterator.next();
        	long componentAverageElapsedTime = RequestAnalyser.getComponentAverageElapsedTime(componentName);
        	int componentNumberOfHits = RequestAnalyser.getComponentNumberOfHits(componentName);
        	//states.add(getList("" + componentName + " - " + componentNumberOfHits + " hits", "" + componentAverageElapsedTime));
        	unsortedComponents.add(getList("" + componentName + " - " + componentNumberOfHits + " hits - total " + (componentNumberOfHits * componentAverageElapsedTime), new Long(componentAverageElapsedTime)));
        }

        Collections.sort(unsortedComponents, new AverageInvokingTimeComparator());
        
    	states.addAll(unsortedComponents);
    	
        return "successComponentStatistics";
    }

    public String doPageStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        states.add(getList("Average processing time per request (ms)", "" + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime()));
		
        states.add(getList("<br/><strong>Individual pages (in milliseconds)</strong>", "&nbsp;"));
        
        List unsortedPageUrls = new ArrayList();
        Set pageUrls = RequestAnalyser.getAllPageUrls();
        Iterator pageUrlsIterator = pageUrls.iterator();
        while(pageUrlsIterator.hasNext())
        {
        	String pageUrl = (String)pageUrlsIterator.next();
        	long pageAverageElapsedTime = RequestAnalyser.getPageAverageElapsedTime(pageUrl);
        	int pageNumberOfHits = RequestAnalyser.getPageNumberOfHits(pageUrl);
        	unsortedPageUrls.add(getList("" + pageUrl + " - " + pageNumberOfHits + " hits - total " + (pageNumberOfHits * pageAverageElapsedTime), new Long(pageAverageElapsedTime)));
        }

        Collections.sort(unsortedPageUrls, new AverageInvokingTimeComparator());
        
    	states.addAll(unsortedPageUrls);

        return "successPageStatistics";
    }
    
    public String doCacheStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        return "successCacheStatistics";
    }

    public String doCacheDetailsStatistics() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;

        
        if(this.cacheName != null && !this.cacheName.equals(""))
        	this.cache = CacheController.getCaches().get(this.cacheName);
        
        return "successCacheDetailsStatistics";
    }

    private String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) throws ServletException, Exception 
  	{
		String url = AuthenticationModule.getAuthenticationModule(null, this.getOriginalFullURL(), request, false).getLoginDialogUrl(request, response);
		return url;
  	}
    
    /**
     * This method is the application entry-point. The method does a lot of checks to see if infoglue
     * is installed correctly and if all resources needed are available.
     */
         
    public String doExecute() throws Exception
    {
    	String returnValue = handleAccess(this.getRequest());
    	if(returnValue != null)
    		return returnValue;
        
        String sessionTimeout = CmsPropertyHandler.getSessionTimeout();
		if(sessionTimeout == null)
		    sessionTimeout = "1800";
		
        states.add(getList("Application started", "" + formatter.formatDate(CmsPropertyHandler.getStartupTime(), "yyyy-MM-dd HH:mm")));
        states.add(getList("Maximum memory (MB)", "" + Runtime.getRuntime().maxMemory() / 1024 / 1024));
        states.add(getList("Used memory (MB)", "" + ((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory()) / 1024 / 1024)));
        states.add(getList("Free memory (MB)", "" + Runtime.getRuntime().freeMemory() / 1024 / 1024));
        states.add(getList("Total memory (MB)", "" + Runtime.getRuntime().totalMemory() / 1024 / 1024));
        addPermGenStatistics(states);
        states.add(getList("Number of sessions <br/>(remains for " + (Integer.parseInt(sessionTimeout) / 60) + " minutes after last request)", "" + CmsSessionContextListener.getActiveSessions()));
        states.add(getList("Number of request being handled now", "" + RequestAnalyser.getRequestAnalyser().getNumberOfCurrentRequests()));
        states.add(getList("Number of active request being handled now", "" + RequestAnalyser.getRequestAnalyser().getNumberOfActiveRequests()));
        states.add(getList("Total number of requests handled", "" + RequestAnalyser.getRequestAnalyser().getTotalNumberOfRequests()));
        states.add(getList("Average processing time per request (ms)", "" + RequestAnalyser.getRequestAnalyser().getAverageElapsedTime()));
        states.add(getList("Slowest request (ms)", "" + RequestAnalyser.getRequestAnalyser().getMaxElapsedTime()));
        states.add(getList("Number of pages in the statistics", RequestAnalyser.getAllPageUrls().size()));

        states.add(getList("<br/><strong>Latest publications</strong>", "&nbsp;"));
        List publications = RequestAnalyser.getRequestAnalyser().getLatestPublications();
        Iterator publicationsIterator = publications.iterator();
        while(publicationsIterator.hasNext())
        {
        	String publication = (String)publicationsIterator.next();
        	states.add(getList("Date/type:", "" + publication));
        }
		        
    	getApplicationAttributes();
    	
		//this.getHttpSession().invalidate();

        return "success";
    }

    private void addPermGenStatistics(List states)
    {
    	Iterator iter1 = ManagementFactory.getMemoryPoolMXBeans().iterator();
    	while (iter1.hasNext()) 
    	{
    	    MemoryPoolMXBean item = (MemoryPoolMXBean) iter1.next();
    	    long used = item.getUsage().getUsed() / 1024 / 1024;
    	    long max = item.getUsage().getMax() / 1024 / 1024;
    	    long usedDivided = used;
    	    long maxDivided = max;
    	    if(max > 100)
    	    {
    	    	usedDivided = used / 10;
    	    	maxDivided = max / 10;
    	    }
    	    
    	    states.add(getList("" + item.getName(), "" + used + " / " + max + "<div style='border: 1px solid #ccc; background-color: green; height: 10px; width: " + maxDivided + "px;'><div style='margin-top: 2px; background-color: red; height: 6px; width: " + usedDivided + "px;'></div></div>"));
    	}	
    	
	    long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
	    long used = ((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory()) / 1024 / 1024);
	    long usedDivided = used;
	    long maxDivided = max;
	    if(max > 100)
	    {
	    	usedDivided = used / 10;
	    	maxDivided = max / 10;
	    }

        states.add(getList("Heap summary", "" + used + " / " + max + "<div style='border: 1px solid #ccc; background-color: green; height: 10px; width: " + maxDivided + "px;'><div style='margin-top: 2px; background-color: red; height: 6px; width: " + usedDivided + "px;'></div></div>"));

    }

	private void getApplicationAttributes()
	{
		Enumeration attributesEnumeration = ActionContext.getServletContext().getAttributeNames();
    	while(attributesEnumeration.hasMoreElements())
    	{
    		String attributeName = (String)attributesEnumeration.nextElement();
    		if(attributeName.indexOf("org.apache.catalina") == -1 && attributeName.indexOf("__oscache") == -1 && attributeName.indexOf("javax.servlet") == -1)
    		{
    			applicationMap.put(attributeName, ActionContext.getServletContext().getAttribute(attributeName).getClass().getName());
    		}
    	}
	}
        
	public String doAsXML() throws Exception
	{
		doExecute();

		getResponse().setContentType("text/xml; charset=utf-8");
		
		return "successAsXML";
	}

	public boolean getIsApplicationSettingsOk()
	{
		return applicationSettingsOk;
	}

	public boolean getIsDatabaseConnectionOk()
	{
		return databaseConnectionOk;
	}

	public boolean getIsDiskPermissionOk()
	{
		return diskPermissionOk;
	}

	public boolean getIsTestQueriesOk()
	{
		return testQueriesOk;
	}

	public Map getCaches()
	{
		return CacheController.getCaches();
	}

	public List getSortedCaches()
	{
		List cacheNames = new ArrayList();
		cacheNames.addAll(CacheController.getCaches().keySet());
		Collections.sort(cacheNames);
		
		return cacheNames;
	}

	public ServletCache getOSCache()
	{
		ServletCacheAdministrator servletCacheAdministrator = ServletCacheAdministrator.getInstance(ActionContext.getServletContext());
		ServletCache applicationCache = (ServletCache)servletCacheAdministrator.getAppScopeCache(ActionContext.getServletContext());
        
		return applicationCache;
	}

	public Map getEventListeners()
	{
		return CacheController.getEventListeners();
	}

    public List getStates()
    {
        return states;
    }
    
    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }

    public void setClearFileCache(boolean clearFileCache)
    {
        this.clearFileCache = clearFileCache;
    }

    public int getActiveNumberOfSessions() throws Exception
    {
    	return CmsSessionContextListener.getActiveSessions();
    }
    
    public String getServerName()
    {
    	String serverName = "Unknown";

    	try
    	{
		    InetAddress localhost = InetAddress.getLocalHost();
		    serverName = localhost.getHostName(); 
    	}
    	catch(Exception e)
    	{
    		
    	}
    	
	    return serverName;
    }

    public Map parseData(String originalSummary, String originalStats)
    {
    	Map data = new HashMap();
    	try
    	{
	    	data.put("originalSummary", originalSummary);
	    	data.put("originalStats", originalStats);
	    	
	    	if(originalSummary != null && originalStats != null)
	    	{
		    	int startLocation = originalSummary.indexOf("Approximate size");
		    	//logger.info("startLocation:" + startLocation);
		    	String size = originalSummary.substring(startLocation + 17, originalSummary.indexOf("KB") + 2);
		    	//logger.info("size:" + size);
		    	data.put("estimatedSize", "" + size);
		
		    	int startHitLocation = originalStats.indexOf("Hit count");
		    	String hits = originalStats.substring(startHitLocation + 12, originalStats.indexOf(",", startHitLocation));
		    	
		    	//logger.info("originalStats:" + originalStats);
		    	int startMissLocation = originalStats.indexOf("miss count");
		    	//logger.info("startMissLocation:" + startMissLocation);
		    	String miss = originalStats.substring(startMissLocation + 13);
		    	
		    	//logger.info("hits:" + hits);
		    	//logger.info("miss:" + miss);
		    	
		    	try
		    	{
		    		int missInt = Integer.parseInt(miss.trim());
		    		int hitInt = Integer.parseInt(hits.trim());
		    		if(missInt > 0 && hitInt > 0)
		    			data.put("hitMissRatio", "" + (int)((float)missInt / (float)hitInt * 100));
		    		else if(missInt == 0)
		    			data.put("hitMissRatio", "0");
		    		else if(hitInt == 0)
		    			data.put("hitMissRatio", "100");
		    	}
		    	catch (Exception e) 
		    	{
		    		logger.error("Could not parse hits / miss:" + e.getMessage() + " (" + miss.trim() + " / " + hits.trim() + ")");
				}
	    	}
    	}
    	catch (Exception e) 
    	{
    		logger.error("Problem parsing data:" + e.getMessage(), e);
		}
    	return data;
    }
    
	public void setClassName(String className) 
	{
		this.className = className;
	}

	public void setLogLevel(String logLevel) 
	{
		this.logLevel = logLevel;
	}

    public Map getApplicationMap()
    {
    	return this.applicationMap;
    }

    public Object getCache()
    {
    	return this.cache;
    }

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}
	
	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

}
