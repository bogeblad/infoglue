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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Redirect;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.filters.ViewPageFilter;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.util.CacheController;


/**
 * @author Mattias Bogeblad
 */

public class RedirectController extends BaseController
{
    private final static Logger logger = Logger.getLogger(RedirectController.class.getName());

	/**
	 * Factory method
	 */

	public static RedirectController getController()
	{
		return new RedirectController();
	}

    public RedirectVO getRedirectVOWithId(Integer redirectId) throws SystemException, Bug
    {
		return (RedirectVO) getVOWithId(RedirectImpl.class, redirectId);
    }

    public Redirect getRedirectWithId(Integer redirectId, Database db) throws SystemException, Bug
    {
		return (Redirect) getObjectWithId(RedirectImpl.class, redirectId, db);
    }

    public List<RedirectVO> getRedirectVOList() throws SystemException, Bug
    {
		List<RedirectVO> redirectVOList = getAllVOObjects(RedirectImpl.class, "redirectId");

		return redirectVOList;
    }

    public List<RedirectVO> getUserRedirectVOList() throws SystemException, Bug
    {
		List<RedirectVO> redirectVOList = new ArrayList<RedirectVO>();

		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);

			String sql = "SELECT c FROM org.infoglue.cms.entities.management.impl.simple.RedirectImpl c WHERE c.isUserManaged = $1 ORDER BY c.redirectId";
			
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(true);

			QueryResults results = oql.execute(Database.READONLY);
			while(results.hasMore()) 
            {
				Redirect redirect = (Redirect)results.next();
				redirectVOList.add(redirect.getValueObject());
            }
            
			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of user managed redirects. Reason:" + e.getMessage(), e);			
		}
		
		return redirectVOList;
    }

    public List<RedirectVO> getSystemRedirectVOList() throws SystemException, Bug
    {
    	List<RedirectVO> redirectVOList = new ArrayList<RedirectVO>();

		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);

			redirectVOList = getSystemRedirectVOList(db);
			
			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of user managed redirects. Reason:" + e.getMessage(), e);			
		}
		
		return redirectVOList;
    }

    
    public List<RedirectVO> getSystemRedirectVOList(Database db) throws SystemException, Bug, Exception
    {
    	List<RedirectVO> redirectVOList = new ArrayList<RedirectVO>();

		String sql = "SELECT c FROM org.infoglue.cms.entities.management.impl.simple.RedirectImpl c WHERE c.isUserManaged = $1 ORDER BY c.redirectId";
		
		OQLQuery oql = db.getOQLQuery(sql);
		oql.bind(false);

		QueryResults results = oql.execute(Database.READONLY);
		while(results.hasMore()) 
		{
			Redirect redirect = (Redirect)results.next();
			redirectVOList.add(redirect.getValueObject());
        }
        
		results.close();
		oql.close();

		return redirectVOList;
    }

    public List getRedirectVOList(Database db) throws SystemException, Bug
    {
		List redirectVOList = getAllVOObjects(RedirectImpl.class, "redirectId", db);

		return redirectVOList;
    }

    public RedirectVO create(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
        Redirect redirect = new RedirectImpl();
        redirect.setValueObject(redirectVO);
        redirect = (Redirect) createEntity(redirect);
        return redirect.getValueObject();
    }

    public void delete(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
    	deleteEntity(RedirectImpl.class, redirectVO.getRedirectId());
    }

    public void delete(RedirectVO redirectVO, Database db) throws ConstraintException, SystemException
    {
    	deleteEntity(RedirectImpl.class, redirectVO.getRedirectId(), db);
    }

    public void deleteExpiredSystemRedirects() throws SystemException, Bug
    {
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);

			List<RedirectVO> redirects = getSystemRedirectVOList(db);
			
			commitTransaction(db);
			
			for(RedirectVO redirectVO : redirects)
			{
				if(redirectVO.getExpireDateTime() != null && redirectVO.getExpireDateTime().before(Calendar.getInstance().getTime()))
				{
					delete(redirectVO);
				}
			}
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to fetch a list of user managed redirects. Reason:" + e.getMessage(), e);			
		}
    }

    
    public RedirectVO update(RedirectVO redirectVO) throws ConstraintException, SystemException
    {
    	return (RedirectVO) updateEntity(RedirectImpl.class, redirectVO);
    }
    
    /**
     * This method checks if there is a redirect that should be used instead.
     * @param requestURI
     * @throws Exception
     */
    
    public String getSystemRedirectUrl(HttpServletRequest request) throws Exception
    {
        try
        {
            String requestURL = request.getRequestURL().toString();
            int indexOfProtocol = requestURL.indexOf("://");
            int indexFirstSlash = requestURL.indexOf("/", indexOfProtocol + 3);
            String base = requestURL.substring(0, indexFirstSlash);

            logger.info("base:" + base);

            String requestURI = base + getContextURI(request);
            logger.info("requestURI:" + requestURI);
            logger.info("full requestURI:" + requestURI);
            
            Collection cachedSystemRedirects = (Collection)CacheController.getCachedObject("redirectCache", "allSystemRedirects");
            if(cachedSystemRedirects == null)
            {
            	cachedSystemRedirects = RedirectController.getController().getSystemRedirectVOList();
                CacheController.cacheObject("redirectCache", "allSystemRedirects", cachedSystemRedirects);
            }
            
			if(logger.isInfoEnabled())
	        	logger.info("requestURI before decoding:" + requestURI);
            
			requestURI = URLDecoder.decode(requestURI, CmsPropertyHandler.getURIEncoding());
			if(logger.isInfoEnabled())
	        	logger.info("requestURI after decoding:" + requestURI);
			
			String fromEncoding = CmsPropertyHandler.getURIEncoding();
			String toEncoding = "utf-8";
			String testRequestURI = new String(requestURI.getBytes(fromEncoding), toEncoding);
			if(testRequestURI.indexOf((char)65533) == -1)
				requestURI = testRequestURI;

			if(logger.isInfoEnabled())
	        	logger.info("requestURI after redecoding:" + requestURI);
			
			Iterator redirectsIterator = cachedSystemRedirects.iterator();
            while(redirectsIterator.hasNext())
            {
                RedirectVO redirect = (RedirectVO)redirectsIterator.next(); 
                if(logger.isInfoEnabled())
                	logger.info("url:" + redirect.getUrl());
                
                Date now = new Date();
                if(redirect.getExpireDateTime() == null || redirect.getPublishDateTime().before(now) && redirect.getExpireDateTime().after(now))
                {
                	if(logger.isInfoEnabled())
                    	logger.info("Was a valid redirect:" + redirect.getUrl());
                }
                else
                {
                	if(logger.isInfoEnabled())
                    	logger.info("Was NOT a valid redirect:" + redirect.getUrl() + ". Skipping....");
                	continue;
                }
                
                boolean matches = false;
                if(redirect.getUrl().startsWith(".*"))
                {
                   if(requestURI.indexOf(redirect.getUrl().substring(2)) > -1)
                       matches = true;
                }
                else if(requestURI.indexOf(redirect.getUrl()) > -1)
                {
                    matches = true;
                }
                
                if(matches)
                {
                	logger.info("redirectUrl:" + redirect.getRedirectUrl());
                	logger.info("url:" + redirect.getUrl());
                	logger.info("Regexp: '.*?" + redirect.getUrl() + "'");
                	
                    String remainingURI = requestURI.replaceAll(".*?" + redirect.getUrl(), "");
                    logger.info("remainingURI:" + remainingURI);
                    if(remainingURI.equalsIgnoreCase("/"))
                    	remainingURI = "";
                    
                	if(remainingURI != null && remainingURI.length() > 0 && !remainingURI.startsWith("/"))
                    	continue;
                		
                	if(redirect.getRedirectUrl().startsWith("ViewPage.action"))
                	{
                		String redirectUrlString = request.getContextPath() + (request.getContextPath().endsWith("/") ? "" : "/") + redirect.getRedirectUrl() + (request.getQueryString() != null && request.getQueryString().length() > 0 ? "&" + request.getQueryString() : "");
                		logger.info("redirectUrlString:" + redirectUrlString);
                		if(!remainingURI.equals(""))
                		{
                			redirectUrlString = redirectUrlString + (redirectUrlString.indexOf("?") > -1 ? "&remainingURI=" + remainingURI : "?remainingURI=" + remainingURI);
                			logger.info("redirectUrlString:" + redirectUrlString);
                		}
                		return redirectUrlString;
                	}
                    else
                    {
                        remainingURI = redirect.getRedirectUrl() + remainingURI;
                        logger.info("remainingURI:" + remainingURI + ":" + remainingURI.indexOf("?"));
                        return remainingURI + (request.getQueryString() != null && request.getQueryString().length() > 0 ? (remainingURI.indexOf("?") > -1 ? "&" : "?") + request.getQueryString() : "");
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new SystemException("An error occurred when looking for page:" + e.getMessage());
        }
        
        return null;
    }

    
    /**
     * This method checks if there is a redirect that should be used instead.
     * @param requestURI
     * @throws Exception
     */
    
    public String getRedirectUrl(HttpServletRequest request) throws Exception
    {
        try
        {
            String requestURL = request.getRequestURL().toString();
            int indexOfProtocol = requestURL.indexOf("://");
            int indexFirstSlash = requestURL.indexOf("/", indexOfProtocol + 3);
            String base = requestURL.substring(0, indexFirstSlash);

            logger.info("base:" + base);

            String requestURI = base + getContextURI(request);
            logger.info("requestURI:" + requestURI);
            logger.info("full requestURI:" + requestURI);
            
            Collection cachedRedirects = (Collection)CacheController.getCachedObject("redirectCache", "allUserRedirects");
            if(cachedRedirects == null)
            {
                cachedRedirects = RedirectController.getController().getUserRedirectVOList();
                CacheController.cacheObject("redirectCache", "allUserRedirects", cachedRedirects);
            }
            
			if(logger.isInfoEnabled())
	        	logger.info("requestURI before decoding:" + requestURI);
            
			requestURI = URLDecoder.decode(requestURI, CmsPropertyHandler.getURIEncoding());
			if(logger.isInfoEnabled())
	        	logger.info("requestURI after decoding:" + requestURI);
			
			String fromEncoding = CmsPropertyHandler.getURIEncoding();
			String toEncoding = "utf-8";
			String testRequestURI = new String(requestURI.getBytes(fromEncoding), toEncoding);
			if(testRequestURI.indexOf((char)65533) == -1)
				requestURI = testRequestURI;

			if(logger.isInfoEnabled())
	        	logger.info("requestURI after redecoding:" + requestURI);
			            
            Iterator redirectsIterator = cachedRedirects.iterator();
            while(redirectsIterator.hasNext())
            {
                RedirectVO redirect = (RedirectVO)redirectsIterator.next(); 
                if(logger.isInfoEnabled())
                	logger.info("url:" + redirect.getUrl());
                
                Date now = new Date();
                if(redirect.getExpireDateTime() == null || redirect.getPublishDateTime().before(now) && redirect.getExpireDateTime().after(now))
                {
                	if(logger.isInfoEnabled())
                    	logger.info("Was a valid redirect:" + redirect.getUrl());
                }
                else
                {
                	if(logger.isInfoEnabled())
                    	logger.info("Was NOT a valid redirect:" + redirect.getUrl() + ". Skipping....");
                	continue;
                }
                
                boolean matches = false;
                if(redirect.getUrl().startsWith(".*"))
                {
                   if(requestURI.indexOf(redirect.getUrl().substring(2)) > -1)
                       matches = true;
                }
                else if(requestURI.startsWith(redirect.getUrl()))
                {
                    matches = true;
                }
                
                //if(requestURI.startsWith(redirect.getUrl()))
                if(matches)
                {
                	if(logger.isInfoEnabled())
                    	logger.info("redirectUrl:" + redirect.getRedirectUrl());
                    String remainingURI = requestURI.replaceAll(redirect.getUrl(), "");
                    if(logger.isInfoEnabled())
                    	logger.info("remainingURI:" + remainingURI);
                    return redirect.getRedirectUrl() + remainingURI + (request.getQueryString() != null && request.getQueryString().length() > 0 ? "?" + request.getQueryString() : "");
                    /*
                    remainingURI = redirect.getRedirectUrl() + remainingURI;
                    return remainingURI + (request.getQueryString() != null && request.getQueryString().length() > 0 ? (remainingURI.indexOf("?") > -1 ? "&" : "?") + request.getQueryString() : "");
                	*/
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new SystemException("An error occurred when looking for page:" + e.getMessage());
        }
        
        return null;
    }
    
    public Map<String,String> getNiceURIMapBeforeMove(Database db, Integer repositoryId, Integer siteNodeId, InfoGluePrincipal principal) throws ConstraintException, SystemException, Exception
    {
    	Map<String,String> pageUrls = new HashMap<String,String>();
    	
    	try
    	{
	    	if(ViewPageFilter.attributeName == null)
	        {
	            String attributeName = CmsPropertyHandler.getNiceURIAttributeName();
	            if(attributeName == null || attributeName.equals("") || attributeName.indexOf("@") > -1)
	                attributeName = "NavigationTitle";
	            if(logger.isInfoEnabled())
                	logger.info("attributeName:" + attributeName);
	            ViewPageFilter.attributeName = attributeName;
	        }
	        
	    	DeliveryContext dc = DeliveryContext.getDeliveryContext();
	    	dc.setDisableNiceUri(false);
	    	dc.setOperatingMode("3");
	    	dc.setUseFullUrl(true);
	    	
	    	List<LanguageVO> languageVOList =  LanguageController.getController().getLanguageVOList(repositoryId, db);
	    	for(LanguageVO languageVO : languageVOList)
	    	{
	    		String pageUrlWithLang = URLComposer.getURLComposer().composePageUrlForRedirectRegistry(db, principal, siteNodeId, languageVO.getId(), -1, dc, true, true);
	    		if(logger.isInfoEnabled())
                	logger.info("pageUrlWithLang:" + pageUrlWithLang);
	        	pageUrls.put("" + languageVO.getId() + "_LangInUrl", pageUrlWithLang);
	    		String pageUrl = URLComposer.getURLComposer().composePageUrlForRedirectRegistry(db, principal, siteNodeId, languageVO.getId(), -1, dc, true, false);
	    		if(logger.isInfoEnabled())
                	logger.info("pageUrl:" + pageUrl);
	        	pageUrls.put("" + languageVO.getId(), pageUrl);
	    	} 
    	}
    	catch (Exception e) 
    	{
    		logger.error("Could not get NiceURI for siteNode:" + siteNodeId + ". Reason: " + e.getMessage(), e);
		}
    	
    	return pageUrls;
    }
    
    public void createSystemRedirect(Map<String,String> pageUrls, Integer repositoryId, Integer siteNodeId, InfoGluePrincipal principal) throws ConstraintException, SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);
		
			createSystemRedirect(pageUrls, repositoryId, siteNodeId, principal, db);

			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to create a system redirect for the old location. Reason:" + e.getMessage(), e);			
		}
    }
    
    public void createSystemRedirect(Map<String,String> pageUrls, Integer repositoryId, Integer siteNodeId, InfoGluePrincipal principal, Database db) throws ConstraintException, SystemException, Exception
    {
    	List<LanguageVO> languageVOList =  LanguageController.getController().getLanguageVOList(repositoryId, db);
        
        SiteNodeVersionVO snvVO = SiteNodeVersionController.getController().getLatestPublishedSiteNodeVersionVO(siteNodeId, db);
        if(logger.isInfoEnabled())
        	logger.info("snvVO:" + snvVO);
        if(snvVO != null && pageUrls != null && pageUrls.size() > 0)
        {
        	if(logger.isInfoEnabled())
            	logger.info("There was a published version of this page... let's create a redirection");
        	for(LanguageVO languageVO : languageVOList)
        	{
            	DeliveryContext dc = DeliveryContext.getDeliveryContext();
            	dc.setDisableNiceUri(false);
            	dc.setOperatingMode("3");
            	dc.setUseFullUrl(true);
            	
            	String redirectUrl = URLComposer.getURLComposer().composePageUrlForRedirectRegistry(db, principal, siteNodeId, languageVO.getId(), -1, dc, false, false);
            	String url = pageUrls.get("" + languageVO.getId());
            	String urlWithLangInUrl = pageUrls.get("" + languageVO.getId() + "_LangInUrl");
            	logger.info("redirectUrl:" + redirectUrl);
            	logger.info("url1:" + url);
            	logger.info("url2:" + urlWithLangInUrl);

            	RedirectVO redirectVO = new RedirectVO();
            	redirectVO.setIsUserManaged(false);
            	redirectVO.setModifier(principal.getName());
            	redirectVO.setUrl(url);
            	redirectVO.setRedirectUrl(redirectUrl);
            	
            	List<RedirectVO> redirectVOList = RedirectController.getController().getSystemManagedRedirectVOList(url, db);
            	if(redirectVOList.isEmpty())
            		RedirectController.getController().create(redirectVO);

            	RedirectVO redirectVOWithLangInUrl = new RedirectVO();
            	redirectVOWithLangInUrl.setIsUserManaged(false);
            	redirectVOWithLangInUrl.setModifier(principal.getName());
            	redirectVOWithLangInUrl.setUrl(urlWithLangInUrl);
            	redirectVOWithLangInUrl.setRedirectUrl(redirectUrl);

            	List<RedirectVO> redirectVOListWithLangInUrl = RedirectController.getController().getSystemManagedRedirectVOList(urlWithLangInUrl, db);
            	if(redirectVOListWithLangInUrl.isEmpty())
            		RedirectController.getController().create(redirectVOWithLangInUrl);

        	}
        }
    }
    
    private List<RedirectVO> getSystemManagedRedirectVOList(String url, Database db) throws Exception 
    {
    	List<RedirectVO> redirectVOList = new ArrayList<RedirectVO>();

		String sql = "SELECT c FROM org.infoglue.cms.entities.management.impl.simple.RedirectImpl c WHERE c.isUserManaged = $1 AND c.url = $2 ORDER BY c.redirectId";
		
		OQLQuery oql = db.getOQLQuery(sql);
		oql.bind(false);
		oql.bind(url);

		QueryResults results = oql.execute(Database.READONLY);
		while(results.hasMore()) 
        {
			Redirect redirect = (Redirect)results.next();
			redirectVOList.add(redirect.getValueObject());
        }
        
		results.close();
		oql.close();
		
		return redirectVOList;
	}
    
    public void deleteRelatedRedirects(Integer siteNodeId) throws Exception 
    {
    	Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			beginTransaction(db);

			List<RedirectVO> systemRedirectVOList = getSystemRedirectVOList(db);
	        
			for(RedirectVO redirectVO : systemRedirectVOList)
			{
				if(redirectVO.getRedirectUrl().indexOf("siteNodeId=" + siteNodeId + "&") > -1)
					delete(redirectVO, db);
			}
					
			commitTransaction(db);
		}
		catch ( Exception e)		
		{
			throw new SystemException("An error occurred when we tried to delete all system redirects for pointing to siteNodeId: " + siteNodeId + ". Reason:" + e.getMessage(), e);			
		}
	}

    private String getContextRelativeURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && requestURI.length() > 0) {
            requestURI = requestURI.substring(contextPath.length(), requestURI.length());
        }
        if (requestURI.length() == 0)
            return "/";
        return requestURI;
    }

    private String getContextURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.length() == 0)
            return "/";
        return requestURI;
    }


	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ContentTypeDefinitionVO();
	}
}
