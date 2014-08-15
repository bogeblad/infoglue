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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Redirect;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.entities.management.impl.simple.RedirectImpl;
import org.infoglue.cms.entities.structure.SiteNodeVO;
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
 * @author Erik Stenbacka <stenbacka@gmail.com>
 */

public class RedirectController extends BaseController
{
	/** Used to store object on request objects ({@link HttpServletRequest#getAttribute(String)}).
	 *  This attribute defines all URL patterns for a SiteNode that matched the request URL. */
	public static final String SITE_NODE_REDIRECT_URLS = "siteNodeRedirectUrls";
	/** Used to store object on request objects ({@link HttpServletRequest#getAttribute(String)}).
	 *  This attribute defines a URL to a page whose SiteNodeId has a redirect rule for the current request URL. */
	public static final String REDIRECT_SUGGESTION = "redirectSuggestion";
	/** Used to store object on request objects ({@link HttpServletRequest#getAttribute(String)}).
	 *  This attribute defines a RedirectVO object that matched the request URL. */
	public static final String REDIRECT_OBJECT = "redirectSuggestionObject";

	private final static Logger logger = Logger.getLogger(RedirectController.class.getName());

	private static final String PLACEHOLDER_CONTEXT = "{CONTEXT}";

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
		@SuppressWarnings("unchecked")
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

    @SuppressWarnings("rawtypes")
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

//	private boolean endsWith(StringBuilder sb, String end)
//	{
//		int length = sb.length();
//		if (length == 0) return false;
//		return sb.substring(length - 1).equalsIgnoreCase(end);
//	}

//	private void appendPathPart(StringBuilder base, String appendix)
//	{
//		if (!endsWith(base, "/") && !appendix.startsWith("/"))
//		{
//			base.append("/");
//		}
//		base.append(appendix);
//	}

	private String getProcessedURL(String unprocessedUrl)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("URL (un-processed): " + unprocessedUrl);
		}
		String url = unprocessedUrl.replace(PLACEHOLDER_CONTEXT, CmsPropertyHandler.getServletContext());

		if(logger.isInfoEnabled())
		{
			logger.info("URL (processed): " + url);
		}
		return url;
	}

	/**
	 * This method checks if there is a redirect that should be used instead.
	 * @param requestURI
	 * @throws Exception
	 */
	public String getSystemRedirectUrl(final HttpServletRequest request) throws Exception
	{
		// RedirectVO redirect
		String result = findRedirectVO(request, new RedirectCallback()
		{
			@Override
			public String execute(RedirectVO redirect, String remainingURI)
			{
				String redirectUrl = getProcessedURL(redirect.getRedirectUrl());
				if(redirectUrl.startsWith("ViewPage.action"))
				{
					String redirectUrlString = request.getContextPath() + (request.getContextPath().endsWith("/") ? "" : "/") + redirectUrl + (request.getQueryString() != null && request.getQueryString().length() > 0 ? "&" + request.getQueryString() : "");
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
					StringBuilder sb = new StringBuilder();
					sb.append(redirectUrl);
					sb.append(remainingURI);

					if (request.getQueryString() != null && request.getQueryString().length() > 0)
					{
						sb.append(sb.indexOf("?") > -1 ? "&" : "?");
						sb.append(request.getQueryString());
					}

					return sb.toString();
				}
			}
		});
		if (result == null)
		{
			logger.debug("Found no system redirect for Url: " + request.getRequestURL().toString());
		}
		return result;
//		if (redirect == null)
//		{
//			logger.debug("Found no system redirect for Url: " + request.getRequestURL().toString());
//		}
//		else
//		{
//			String requestURL = request.getRequestURL().toString();
//			int indexOfProtocol = requestURL.indexOf("://");
//			int indexFirstSlash = requestURL.indexOf("/", indexOfProtocol + 3);
//			String base = requestURL.substring(0, indexFirstSlash);
//			String requestURI = base + getContextURI(request);
//			requestURI = URLDecoder.decode(requestURI, CmsPropertyHandler.getURIEncoding());
//			String url = getProcessedURL(redirect.getRedirectUrl());
//
//			String redirectUrl = getProcessedURL(redirect.getRedirectUrl());
//			logger.info("redirectUrl:" + redirectUrl);
//			logger.info("url:" + url);
//			logger.info("Regexp: '.*?" + url + "'");
//	
//			String remainingURI = requestURI.replaceAll(".*?" + url, "");
//			logger.info("remainingURI: " + remainingURI);
//			if(remainingURI.equalsIgnoreCase("/"))
//			{
//				remainingURI = "";
//			}
//
//			
//		}
//		return null;
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
            
            @SuppressWarnings("unchecked")
			Collection<RedirectVO> cachedRedirects = (Collection<RedirectVO>)CacheController.getCachedObject("redirectCache", "allUserRedirects");
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
			            
            Iterator<RedirectVO> redirectsIterator = cachedRedirects.iterator();
            while(redirectsIterator.hasNext())
            {
                RedirectVO redirect = redirectsIterator.next();
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
				{
					attributeName = "NavigationTitle";
				}
				if(logger.isInfoEnabled())
				{
					logger.info("attributeName:" + attributeName);
				}
				ViewPageFilter.attributeName = attributeName;
			}

			DeliveryContext dc = DeliveryContext.getDeliveryContext();
			dc.setDisableNiceUri(false);
			dc.setOperatingMode("3");
			dc.setUseFullUrl(true);
			dc.setSiteNodeId(siteNodeId);
			dc.setContentId(-1);

			List<LanguageVO> languageVOList =  LanguageController.getController().getLanguageVOList(repositoryId, db);
			SiteNodeVO siteNodeVO = SiteNodeController.getSiteNodeVOWithId(siteNodeId, db);
			for(LanguageVO languageVO : languageVOList)
			{
				ContentVersionVO currentPublishedMetainfoVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), languageVO.getLanguageId(), ContentVersionVO.PUBLISHED_STATE, db);
				if (logger.isDebugEnabled())
				{
					logger.debug("Getting NiceUri path for SiteNode: " + siteNodeId + ", in language: " + languageVO.getLanguageId() + ". SiteNode has version in current language: " + (currentPublishedMetainfoVersion != null));
				}
				if (currentPublishedMetainfoVersion != null)
				{
					dc.setLanguageId(languageVO.getLanguageId());

					String pageUrlWithLang = URLComposer.getURLComposer().composePageUrl(db, principal, siteNodeId, languageVO.getLanguageId(), -1, PLACEHOLDER_CONTEXT, dc);
					if(logger.isInfoEnabled())
					{
						logger.info("pageUrlWithLang: " + pageUrlWithLang);
					}
					pageUrls.put("" + languageVO.getId() + "_LangInUrl", pageUrlWithLang);
					String pageUrl = URLComposer.getURLComposer().composePageUrl(db, principal, siteNodeId, languageVO.getLanguageId(), -1, PLACEHOLDER_CONTEXT, dc);
					if(logger.isInfoEnabled())
					{
						logger.info("pageUrl: " + pageUrl);
					}
					pageUrls.put("" + languageVO.getId(), pageUrl);
				}
			}

			if (logger.isInfoEnabled())
			{
				logger.info("Generated URLs for system redirects. URLs: " + pageUrls);
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

		@SuppressWarnings("static-access")
		SiteNodeVersionVO snvVO = SiteNodeVersionController.getController().getLatestPublishedSiteNodeVersionVO(siteNodeId, db);
		if(logger.isTraceEnabled())
		{
			logger.trace("snvVO:" + snvVO);
		}
		if(snvVO != null && pageUrls != null && pageUrls.size() > 0)
		{
			if(logger.isInfoEnabled())
			{
				logger.info("There was a published version of this page... let's create a redirection");
			}
			for(LanguageVO languageVO : languageVOList)
			{
				DeliveryContext dc = DeliveryContext.getDeliveryContext();
				dc.setDisableNiceUri(false);
				dc.setOperatingMode("3");
				dc.setUseFullUrl(true);
				dc.setSiteNodeId(siteNodeId);
				dc.setContentId(-1);
				dc.setLanguageId(languageVO.getLanguageId());

				// TODO: Enable NiceLanguageURIs when the feature is implemented.
//				boolean enableNiceURIForLanguage = Boolean.parseBoolean(CmsPropertyHandler.getEnableNiceURIForLanguage());
//				String redirectUrl = URLComposer.getURLComposer().composePageUrlForRedirectRegistry(db, principal, siteNodeId, languageVO.getId(), -1, dc, true, enableNiceURIForLanguage);
				String redirectUrl = URLComposer.getURLComposer().composePageUrl(db, principal, siteNodeId, languageVO.getLanguageId(), -1, PLACEHOLDER_CONTEXT, dc);
				String url = pageUrls.get("" + languageVO.getId());
				String urlWithLangInUrl = pageUrls.get("" + languageVO.getId() + "_LangInUrl");
				logger.info("redirectUrl:" + redirectUrl);
				logger.info("url: " + url);
				logger.info("urlWithLangInUrl: " + urlWithLangInUrl);
				logger.info("siteNodeId:" + siteNodeId);
				logger.info("languageId:" + languageVO.getLanguageId());

				if (url != null)
				{
					RedirectVO redirectVO = new RedirectVO();
					redirectVO.setIsUserManaged(false);
					redirectVO.setModifier(principal.getName());
					redirectVO.setUrl(url);
					redirectVO.setRedirectUrl(redirectUrl);
					redirectVO.setSiteNodeId(siteNodeId);
					redirectVO.setLanguageId(languageVO.getLanguageId());

					Calendar calendar = Calendar.getInstance();
					int months = CmsPropertyHandler.getDefaultNumberOfMonthsBeforeSystemRedirectExpire();
					calendar.add(Calendar.MONTH, months);
					redirectVO.setExpireDateTime(calendar.getTime());
					List<RedirectVO> redirectVOList = RedirectController.getController().getSystemManagedRedirectVOList(url, db);
					if(redirectVOList.isEmpty())
					{
						RedirectController.getController().create(redirectVO);
					}
					else
					{
						logger.info("A redirect rule already exists for the URL. URL: " + url);
					}
				}

				if (urlWithLangInUrl != null)
				{
					RedirectVO redirectVOWithLangInUrl = new RedirectVO();
					redirectVOWithLangInUrl.setIsUserManaged(false);
					redirectVOWithLangInUrl.setModifier(principal.getName());
					redirectVOWithLangInUrl.setUrl(urlWithLangInUrl);
					redirectVOWithLangInUrl.setRedirectUrl(redirectUrl);
					redirectVOWithLangInUrl.setSiteNodeId(siteNodeId);
					redirectVOWithLangInUrl.setLanguageId(languageVO.getLanguageId());

					List<RedirectVO> redirectVOListWithLangInUrl = RedirectController.getController().getSystemManagedRedirectVOList(urlWithLangInUrl, db);
					if(redirectVOListWithLangInUrl.isEmpty())
					{
						RedirectController.getController().create(redirectVOWithLangInUrl);
					}
					else
					{
						logger.info("A redirect rule already exists for the URL (language-in-URL). URL: " + urlWithLangInUrl);
					}
				}
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

//    private String getContextRelativeURI(HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        String contextPath = request.getContextPath();
//        if (contextPath != null && requestURI.length() > 0) {
//            requestURI = requestURI.substring(contextPath.length(), requestURI.length());
//        }
//        if (requestURI.length() == 0)
//            return "/";
//        return requestURI;
//    }

    private String getContextURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.length() == 0)
            return "/";
        return requestURI;
    }

	private String getUrlFromRedirect(RedirectVO redirectVO, InfoGluePrincipal infoGluePrincipal)
	{
		String result = null;
		Database db = null;
		try
		{
			db = beginTransaction();
			result = getUrlFromRedirect(redirectVO, infoGluePrincipal, db);
			commitTransaction(db);
		}
		catch (Exception ex)
		{
			logger.error("Failed to generate URL for redirect suggestions. Message: " + ex.getMessage());
			logger.warn("Failed to generate URL for redirect suggestions. ", ex);
			try
			{
				rollbackTransaction(db);
			}
			catch (SystemException e)
			{
				logger.error("Failed to rollback transaction when generating URL for redirect suggestions. Message: " + ex.getMessage());
				logger.warn("Failed to rollback transaction when generating URL for redirect suggestions. ", ex);
			}
		}
		return result;
	}

	private String getUrlFromRedirect(RedirectVO redirectVO, InfoGluePrincipal infoGluePrincipal, Database db) throws SystemException, Exception
	{
		DeliveryContext dc = DeliveryContext.getDeliveryContext();
		dc.setDisableNiceUri(false);
		dc.setOperatingMode("3");
		dc.setUseFullUrl(true);
		dc.setSiteNodeId(redirectVO.getSiteNodeId());
		dc.setContentId(-1);
		dc.setLanguageId(redirectVO.getLanguageId());

		String result = URLComposer.getURLComposer().composePageUrl(db, infoGluePrincipal, redirectVO.getSiteNodeId(), redirectVO.getLanguageId(), -1, dc);
		if (logger.isDebugEnabled())
		{
			logger.debug("Composed URL for redirect. Redirect.id: " + redirectVO.getRedirectId() + ". SiteNode.id: " + redirectVO.getSiteNodeId() + ". Result: " + result);
		}
		return result;
	}

	public List<RedirectVO> getAllRedirectsForSiteNode(Integer siteNodeId)
	{
		List<RedirectVO> redirectVOList = new ArrayList<RedirectVO>();
		Database db = null;
		try
		{
			db = beginTransaction();
			String sql = "SELECT c FROM org.infoglue.cms.entities.management.impl.simple.RedirectImpl c WHERE c.siteNodeId = $1 ORDER BY c.redirectId";

			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(siteNodeId);

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
		catch (Exception  ex)
		{
			logger.error("Failed to generate URL for redirect suggestions. Message: " + ex.getMessage());
			logger.warn("Failed to generate URL for redirect suggestions. ", ex);
			try
			{
				rollbackTransaction(db);
			}
			catch (SystemException e)
			{
				logger.error("Failed to rollback transaction when generating URL for redirect suggestions. Message: " + ex.getMessage());
				logger.warn("Failed to rollback transaction when generating URL for redirect suggestions. ", ex);
			}
		}
		return redirectVOList;
	}

	private List<String> getRedirectPatternsFromRedirectList(List<RedirectVO> redirects)
	{
		List<String> result = new ArrayList<String>();
		for (RedirectVO redirect : redirects)
		{
			result.add(redirect.getUrl());
		}
		return result;
	}

//	private List<String> getUrlListFromRedirectList(List<RedirectVO> redirects, InfoGluePrincipal infoGluePrincipal)
//	{
//		List<String> urlList = new ArrayList<String>();
//		Database db = null;
//		try
//		{
//			db = beginTransaction();
//			for (RedirectVO redirect : redirects)
//			{
//				urlList.add(getUrlFromRedirect(redirect, infoGluePrincipal, db));
//			}
//			commitTransaction(db);
//		}
//		catch (Exception  ex)
//		{
//			logger.error("Failed to generate URLs from redirect list. Message: " + ex.getMessage());
//			logger.warn("Failed to generate URLs from redirect list. ", ex);
//			try
//			{
//				rollbackTransaction(db);
//			}
//			catch (SystemException e)
//			{
//				logger.error("Failed to rollback transaction when generating URLs from redirect list. Message: " + ex.getMessage());
//				logger.warn("Failed to rollback transaction when generating URLs from redirect list. ", ex);
//			}
//		}
//		return urlList;
//	}

	public void populateRequestWithRedirectSuggestions(final HttpServletRequest request) throws SystemException
	{
		findRedirectVO(request, new RedirectCallback()
		{
			@Override
			public String execute(RedirectVO redirectVO, String remainingUrlPart)
			{
				if (logger.isInfoEnabled())
				{
					logger.info("Found RedirectVO when populating redirect suggestions. SiteNodeId: " + redirectVO.getSiteNodeId() + ", URL: " + request.getRequestURL().toString() + ", RedirectId: " + redirectVO.getRedirectId());
				}
				InfoGluePrincipal infoGluePrincipal = (InfoGluePrincipal) request.getSession().getAttribute("infogluePrincipal");
				String redirectUrl = getUrlFromRedirect(redirectVO, infoGluePrincipal);
				logger.debug("RedirectURL before remaining append: " + redirectUrl);
				redirectUrl = redirectUrl + remainingUrlPart;
				logger.debug("RedirectURL after remaining append: " + redirectUrl);
				request.setAttribute(REDIRECT_SUGGESTION, redirectUrl);

				List<String> siteNodeRedirectUrls = getRedirectPatternsFromRedirectList(getAllRedirectsForSiteNode(redirectVO.getSiteNodeId()));
				request.setAttribute(SITE_NODE_REDIRECT_URLS, siteNodeRedirectUrls);

				request.setAttribute(REDIRECT_OBJECT, redirectVO);

				return null;
			}
		});
	}

	private String findRedirectVO(HttpServletRequest request, RedirectCallback callback) throws SystemException
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

			@SuppressWarnings("unchecked")
			Collection<RedirectVO> cachedSystemRedirects = (Collection<RedirectVO>)CacheController.getCachedObject("redirectCache", "allSystemRedirects");
			if (cachedSystemRedirects == null)
			{
				cachedSystemRedirects = RedirectController.getController().getSystemRedirectVOList();
				CacheController.cacheObject("redirectCache", "allSystemRedirects", cachedSystemRedirects);
			}

			if (logger.isInfoEnabled())
				logger.info("requestURI before decoding:" + requestURI);

			requestURI = URLDecoder.decode(requestURI, CmsPropertyHandler.getURIEncoding());
			if (logger.isInfoEnabled())
				logger.info("requestURI after decoding:" + requestURI);

			String fromEncoding = CmsPropertyHandler.getURIEncoding();
			String toEncoding = "utf-8";
			String testRequestURI = new String(requestURI.getBytes(fromEncoding), toEncoding);
			if (testRequestURI.indexOf((char)65533) == -1)
			{
				requestURI = testRequestURI;
			}

			if (logger.isInfoEnabled())
			{
				logger.info("requestURI after redecoding:" + requestURI);
			}

			Iterator<RedirectVO> redirectsIterator = cachedSystemRedirects.iterator();
			while (redirectsIterator.hasNext())
			{
				RedirectVO redirect = redirectsIterator.next();

				Date now = new Date();
				if(redirect.getExpireDateTime() == null || redirect.getPublishDateTime().before(now) && redirect.getExpireDateTime().after(now))
				{
					if(logger.isInfoEnabled())
					{
						logger.info("Was a valid redirect:" + redirect.getUrl());
					}
				}
				else
				{
					if(logger.isInfoEnabled())
					{
						logger.info("Was NOT a valid redirect:" + redirect.getUrl() + ". Skipping....");
					}
					continue;
				}

				String url = getProcessedURL(redirect.getUrl());

				boolean matches = false;
				if(url.startsWith(".*"))
				{
					if(requestURI.indexOf(url.substring(2)) > -1)
					{
						matches = true;
					}
				}
				else if(requestURI.indexOf(url) > -1)
				{
					matches = true;
				}

				if(matches)
				{
					String remainingURI = requestURI.replaceAll(".*?" + url, "");
					logger.info("remainingURI: " + remainingURI);
					if(remainingURI.equalsIgnoreCase("/"))
					{
						remainingURI = "";
					}
					if(remainingURI != null && remainingURI.length() > 0 && !remainingURI.startsWith("/"))
					{
						continue;
					}

					return callback.execute(redirect, remainingURI);
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
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ContentTypeDefinitionVO();
	}

	private interface RedirectCallback
	{
		String execute(RedirectVO redirectVO, String remainingUrlPart);
	}

}
