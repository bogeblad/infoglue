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

package org.infoglue.deliver.applications.filters;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.InstallationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.BaseDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

/**
 *
 *
 * @author Lars Borup Jensen (lbj@atira.dk)
 * @author Mattias Bogeblad (bogeblad@yahoo.com)
 * 
 */

public class ViewPageFilter implements Filter 
{
    public final static Logger logger = Logger.getLogger(ViewPageFilter.class.getName());

    private FilterConfig filterConfig = null;
    private URIMatcher uriMatcher = null;
    private URIMapperCache uriCache = null;
    public static String attributeName = null;
    public static boolean caseSensitive = false;

    public void init(FilterConfig filterConfig) throws ServletException 
    {
        this.filterConfig = filterConfig;
        String filterURIs = filterConfig.getInitParameter(FilterConstants.FILTER_URIS_PARAMETER);

        String caseSensitiveString = CmsPropertyHandler.getCaseSensitiveRedirects();
        logger.info("caseSensitiveString:" + caseSensitiveString);
        caseSensitive = Boolean.parseBoolean(caseSensitiveString);
        
        uriMatcher = URIMatcher.compilePatterns(splitString(filterURIs, ","), caseSensitive);

        attributeName = CmsPropertyHandler.getNiceURIAttributeName();
        logger.info("attributeName from properties:" + attributeName);
        
        if(attributeName == null || attributeName.indexOf("@") > -1)
            attributeName = filterConfig.getInitParameter(FilterConstants.ATTRIBUTE_NAME_PARAMETER);
        
        logger.info("attributeName from web.xml, filter parameters:" + attributeName);
        if(attributeName == null || attributeName.equals(""))
            attributeName = "NavigationTitle";

        logger.info("attributeName used:" + attributeName);

        uriCache = new URIMapperCache();
    }

	private static Boolean configurationFinished = null;

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException 
    {       
        Timer t = new Timer();

        long end, start = System.currentTimeMillis();
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        
		if(!CmsPropertyHandler.getIsValidSetup() && (httpRequest.getRequestURI().indexOf("Install") == -1 && httpRequest.getRequestURI().indexOf("/script") == -1 && httpRequest.getRequestURI().indexOf("/css") == -1 && httpRequest.getRequestURI().indexOf("/images") == -1))
			httpResponse.sendRedirect("" + httpRequest.getContextPath() + "/Install!input.action");
		        
        String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
        if (enableNiceURI == null)
            enableNiceURI = "false";

        validateCmsProperties(httpRequest);
        String requestURI = URLDecoder.decode(getContextRelativeURI(httpRequest), "UTF-8");
        if(logger.isInfoEnabled())
        	logger.info("requestURI:" + requestURI);

        Timer t2 = new Timer();
        try
        {
        	//logger.info("requestURI:" + requestURI);
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
			//logger.info("requestURI:" + requestURI);
        }
        catch (Exception e) 
        {
        	logger.warn("Error checking for unicode chars:" + e.getMessage());
		}
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("IndexOf in uri-change", t2.getElapsedTime());
        
        if(logger.isInfoEnabled())
        	logger.info("requestURI after encoding check:" + requestURI);

        try
        {
        	if(requestURI.indexOf(CmsPropertyHandler.getDigitalAssetBaseUrl() + "/protected") > -1)
        	{
            	throw new Exception("Not allowed to view protected assets...");
        	}
        	
        	String remainingURI = httpRequest.getParameter("remainingURI");
	        if (enableNiceURI.equalsIgnoreCase("true") && (!uriMatcher.matches(requestURI) || remainingURI != null)) 
	        {
	        	if(logger.isInfoEnabled())
            		logger.info("Entering niceURI logic with:" + remainingURI);
	            while(/*!CmsPropertyHandler.getOperatingMode().equals("3") &&*/ CmsPropertyHandler.getActuallyBlockOnBlockRequests() && RequestAnalyser.getRequestAnalyser().getBlockRequests())
	            {
	            	if(logger.isInfoEnabled())
	            		logger.info("Queing up requests as cache eviction are taking place..");
	            	try { Thread.sleep(10); } catch (Exception e) {}
	            }

	            RequestAnalyser.getRequestAnalyser().incNumberOfCurrentRequests(null);

	            HttpSession httpSession = httpRequest.getSession(true);
	
	            List repositoryVOList = null;
	            Integer languageId = null;
	
	            Database db = CastorDatabaseService.getDatabase();
	    		
	            BaseDeliveryController.beginTransaction(db);
	            
	            try
	            {
	                repositoryVOList = getRepositoryId(httpRequest, db);
	                if(logger.isInfoEnabled())
	                	logger.info("repositoryVOList:" + repositoryVOList.size());
            
	            	languageId = getLanguageId(httpRequest, httpSession, repositoryVOList, requestURI, db);
	            
	                Integer siteNodeId = null;
	                if(languageId != null)
	                {
			            String[] nodeNames = splitString(requestURI, "/");
			            logger.info("nodeNames:" + nodeNames.length);
			            
			            List<String> nodeNameList = new ArrayList<String>();
			            for(int i=0; i<nodeNames.length; i++)
			            {
			            	String nodeName = nodeNames[i];
			            	if(nodeName.indexOf(".cid") == -1)
			            	{
			            		nodeNameList.add(nodeName);
			            	}
			            }

	            		nodeNames = new String[nodeNameList.size()];
	            		nodeNames = nodeNameList.toArray(nodeNames);
			            
	            		//logger.info("RepositoryId.: "+repositoryId);
			            //logger.info("LanguageId...: "+languageId);
			            //logger.info("RequestURI...: "+requestURI);
			
		                InfoGluePrincipal infoGluePrincipal = (InfoGluePrincipal) httpSession.getAttribute("infogluePrincipal");
		                if (infoGluePrincipal == null) 
		                {
		                    try 
		                    {
		                        infoGluePrincipal = (InfoGluePrincipal) CacheController.getCachedObject("userCache", "anonymous");
		                        if (infoGluePrincipal == null) 
		                        {
		                            Map arguments = new HashMap();
		        				    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
		        				    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
		        					infoGluePrincipal = (InfoGluePrincipal)ExtranetController.getController().getAuthenticatedPrincipal(db, arguments);
		        					if(infoGluePrincipal != null)
		        						CacheController.cacheObject("userCache", "anonymous", infoGluePrincipal);
		                        }
		                        //this.principal = ExtranetController.getController().getAuthenticatedPrincipal("anonymous", "anonymous");
		
		                    } 
		                    catch (Exception e) 
		                    {
		    	                BaseDeliveryController.rollbackTransaction(db);
		                        throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
		                    }
		                }
		
		                String siteNodeIdString = httpRequest.getParameter("siteNodeId");
		            	if(siteNodeIdString != null && !siteNodeIdString.equals("") && remainingURI != null && !remainingURI.equals(""))
		                {
		                	nodeNames = splitString(remainingURI, "/");
				            logger.info("nodeNames:" + nodeNames.length);
				            
				            nodeNameList = new ArrayList<String>();
				            for(int i=0; i<nodeNames.length; i++)
				            {
				            	String nodeName = nodeNames[i];
				            	if(nodeName.indexOf(".cid") == -1)
				            	{
				            		nodeNameList.add(nodeName);
				            	}
				            }

		            		nodeNames = new String[nodeNameList.size()];
		            		nodeNames = nodeNameList.toArray(nodeNames);
		            		
		                    DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext();
		                    siteNodeId = NodeDeliveryController.getSiteNodeIdFromBaseSiteNodeIdAndPath(infoGluePrincipal, nodeNames, attributeName, deliveryContext, httpSession, languageId, siteNodeIdString, remainingURI);
		                }
		                else
		                {
			                Iterator repositorVOListIterator = repositoryVOList.iterator();
			                while(repositorVOListIterator.hasNext())
			                {
			                    RepositoryVO repositoryVO = (RepositoryVO)repositorVOListIterator.next();
			                    logger.info("Getting node from:" + repositoryVO.getName());
			                    
			                    //TODO
			                    DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext();
		                    	siteNodeId = NodeDeliveryController.getSiteNodeIdFromPath(infoGluePrincipal, repositoryVO, nodeNames, attributeName, deliveryContext, httpSession, languageId);
			                    
			                    if(deliveryContext.getLanguageId() != null && !deliveryContext.getLanguageId().equals(languageId))
			                    {
			                    	languageId = deliveryContext.getLanguageId();
			                        httpSession.setAttribute(FilterConstants.LANGUAGE_ID, languageId);
			                    }
			                    
			                    if(siteNodeId != null)
			                        break;
			                }
		                }
	                }
	                
	                BaseDeliveryController.rollbackTransaction(db);

	                end = System.currentTimeMillis();
	                
	                if(siteNodeId == null)
	                {
	                    String redirectUrl = RedirectController.getController().getRedirectUrl(httpRequest);
	                    if(redirectUrl != null && redirectUrl.length() > 0)
	                    {
		                    httpResponse.sendRedirect(redirectUrl);
		                    return;
	                    }
	                    
	        			String extraInformation = "Referer: " + httpRequest.getHeader("Referer") + "\n";
	        			extraInformation += "UserAgent: " + httpRequest.getHeader("User-Agent") + "\n";
	        			extraInformation += "User IP: " + httpRequest.getRemoteAddr();
	        			
	        			logger.info("Could not map URI " + requestURI + " against any page on this website." + "\n" + extraInformation);
	                    throw new ServletException("Could not map URI " + requestURI + " against any page on this website.");	                    	
	                }
	                else
	                    logger.info("Mapped URI " + requestURI + " --> " + siteNodeId + " in " + (end - start) + "ms");
	                
	                Integer contentId = getContentId(httpRequest);
	                
	                HttpServletRequest wrappedHttpRequest = prepareRequest(httpRequest, siteNodeId, languageId, contentId);
	                wrappedHttpRequest.getRequestDispatcher("/ViewPage.action").forward(wrappedHttpRequest, httpResponse);
	            } 
	            catch (SystemException e) 
	            {
	                BaseDeliveryController.rollbackTransaction(db);
	                logger.error("Failed to resolve siteNodeId", e);
	                String systemRedirectUrl = RedirectController.getController().getSystemRedirectUrl(httpRequest);
                    if(systemRedirectUrl != null && systemRedirectUrl.length() > 0)
                    {
                    	httpResponse.setStatus(301);
                    	httpResponse.setHeader("Location", systemRedirectUrl);
                    	httpResponse.setHeader("Connection", "close");
	                    return;
                    }
                    else
                    {
	                	throw new ServletException(e);
	            	} 
	            } 
	            catch (Exception e) 
	            {
	                BaseDeliveryController.rollbackTransaction(db);
	                
	                logger.error("Failed to resolve siteNodeId: " + e.getMessage());
	                if(logger.isInfoEnabled())
	                	logger.info("Failed to resolve siteNodeId: " + e.getMessage(), e);
	                String systemRedirectUrl = RedirectController.getController().getSystemRedirectUrl(httpRequest);
                    if(systemRedirectUrl != null && systemRedirectUrl.length() > 0)
                    {
                    	httpResponse.setStatus(301);
                    	httpResponse.setHeader("Location", systemRedirectUrl);
                    	httpResponse.setHeader("Connection", "close");
	                    return;
                    }
                    else
                    {
	                	throw new ServletException(e);
	            	}
	            }
	            finally
	            {
	            	try
	            	{
	            		BaseDeliveryController.closeDatabase(db);
	            	}
	            	catch (Exception e) 
	            	{
	            		e.printStackTrace();
					}
	                RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(-1);
	            }
	            
	        } 
	        else 
	        {
	        	//filterChain.doFilter(httpRequest, httpResponse);
	        	if(!httpResponse.isCommitted())
	        	{
	        		try
		        	{
		        		filterChain.doFilter(httpRequest, httpResponse);
		        	}
		        	catch (Exception e) 
		        	{
		        		logger.error("Response was committed - could not continue filter chains:" + e.getMessage());
		        	}
	        	}
	        	
	        }    
        }
        catch (SystemException se) 
        {
        	if(!httpResponse.isCommitted())
        	{
	            httpRequest.setAttribute("responseCode", "500");
	            httpRequest.setAttribute("error", se);
	            httpRequest.getRequestDispatcher("/ErrorPage.action").forward(httpRequest, httpResponse);
        	}
        	else
        		logger.error("Error and response was committed:" + se.getMessage(), se);
        }
        catch (Exception e) 
        {
        	if(!httpResponse.isCommitted())
        	{
		    	httpRequest.setAttribute("responseCode", "404");
	            httpRequest.setAttribute("error", e);
	            httpRequest.getRequestDispatcher("/ErrorPage.action").forward(httpRequest, httpResponse);
	        }
	        else
	            logger.error("Error and response was committed:" + e.getMessage(), e);
	    }
        
        if(httpRequest.getRequestURL().indexOf("digitalAssets") == -1)
        	RequestAnalyser.getRequestAnalyser().registerComponentStatistics("ViewPageFilter", t.getElapsedTime());
    }

    public void destroy() 
    {
        this.filterConfig = null;
    }

    private void validateCmsProperties(HttpServletRequest request) 
    {
        if (CmsPropertyHandler.getServletContext() == null) 
        {
        	CmsPropertyHandler.setServletContext(request.getContextPath());
        }
    }

    private List getRepositoryId(HttpServletRequest request, Database db) throws ServletException, SystemException, Exception 
    {
        /*
        if (session.getAttribute(FilterConstants.REPOSITORY_ID) != null) 
        {
            logger.info("Fetching repositoryId from session");
            return (Integer) session.getAttribute(FilterConstants.REPOSITORY_ID);
        }
        */

        if(logger.isInfoEnabled())
        	logger.info("Trying to lookup repositoryId");
        String serverName = request.getServerName();
        String portNumber = new Integer(request.getServerPort()).toString();
        String repositoryName = request.getParameter("repositoryName");
        if(logger.isInfoEnabled())
        {
	        logger.info("serverName:" + serverName);
	        logger.info("repositoryName:" + repositoryName);
	    }
        
        String repCacheKey = "" + serverName + "_" + portNumber + "_" + repositoryName;
        List repositoryVOList = (List)CacheController.getCachedObject(uriCache.CACHE_NAME, repCacheKey);
        if (repositoryVOList != null) 
        {
            logger.info("Using cached repositoryVOList");
            return repositoryVOList;
        }

        List repositories = RepositoryDeliveryController.getRepositoryDeliveryController().getRepositoryVOListFromServerName(db, serverName, portNumber, repositoryName);
        if(logger.isInfoEnabled())
        	logger.info("repositories:" + repositories);
        
        if (repositories.size() == 0)
        {
            String redirectUrl = RedirectController.getController().getRedirectUrl(request);
            if(logger.isInfoEnabled())
            	logger.info("redirectUrl:" + redirectUrl);
            if(redirectUrl == null || redirectUrl.length() == 0)
            {
                if (repositories.size() == 0) 
                {
                    try 
                    {
                        if(logger.isInfoEnabled())
                        	logger.info("Adding master repository instead - is this correct?");
                        repositories.add(RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db));
                    } 
                    catch (Exception e1) 
                    {
                        logger.error("Failed to lookup master repository");
                    }
                }
                
                if (repositories.size() == 0)
                    throw new ServletException("Unable to find a repository for server-name " + serverName);
            }
        }
        
        CacheController.cacheObject(uriCache.CACHE_NAME, repCacheKey, repositories);
        //session.setAttribute(FilterConstants.REPOSITORY_ID, repository.getRepositoryId());
        return repositories;
    }

    private Integer getLanguageId(HttpServletRequest request, HttpSession session, List repositoryVOList, String requestURI, Database db) throws ServletException, Exception 
    {
    	Integer languageId = null;
        if(request.getParameter("languageId") != null) 
        {
            logger.info("Language is explicitely given in request");
            try 
            {
                languageId = Integer.valueOf(request.getParameter("languageId"));
                session.setAttribute(FilterConstants.LANGUAGE_ID, languageId);
            } 
            catch (NumberFormatException e) {}
        }
        else
        {
        	Timer t = new Timer();
        	Iterator repositoryVOListIterator = repositoryVOList.iterator();
        	outer: while(repositoryVOListIterator.hasNext())
        	{
        		RepositoryVO repositoryVO = (RepositoryVO)repositoryVOListIterator.next();
        		String dnsName = repositoryVO.getDnsName();
        		String serverName = request.getServerName();
        		int startIndex = dnsName.indexOf(serverName);
        		if(startIndex > -1)
        		{
        			while(startIndex > -1)
        			{
        				String domain = null;
        				int endIndex = dnsName.indexOf(",", startIndex);
        				if(endIndex > -1)
        					domain = dnsName.substring(startIndex, endIndex);
        				else
        					domain = dnsName.substring(startIndex);
        					
        				if(domain.indexOf("[") > -1)
        				{
        					String languageCode = domain.substring(domain.indexOf("[") + 1, domain.length() - 1);
        					LanguageVO languageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageWithCode(db, languageCode);
        					if(languageVO != null)
        					{
        						session.setAttribute(FilterConstants.LANGUAGE_ID, languageVO.getId());
        						languageId = languageVO.getId();
            					break outer;
        					}
        				}
        				
        				startIndex = dnsName.indexOf(serverName, startIndex + 1);
        			}
        		}
        	}
        }

        if (languageId != null)
            return languageId;

        if (session.getAttribute(FilterConstants.LANGUAGE_ID) != null) {
            logger.info("Fetching languageId from session");
            return (Integer) session.getAttribute(FilterConstants.LANGUAGE_ID);
        }

        Integer repositoryId = null;
        if(repositoryVOList != null && repositoryVOList.size() > 0)
            repositoryId = ((RepositoryVO)repositoryVOList.get(0)).getId();
        
        logger.info("Looking for languageId for repository " + repositoryId);
        Locale requestLocale = request.getLocale();
       
        if(repositoryId == null)
        	return null;
        
        try 
        {
            List availableLanguagesForRepository = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguagesForRepository(db, repositoryId);

            if (requestLocale != null) 
            {
                for (int i = 0; i < availableLanguagesForRepository.size(); i++) 
                {
                    LanguageVO language = (LanguageVO) availableLanguagesForRepository.get(i);
                    logger.info("language:" + language.getLanguageCode());
                    logger.info("browserLanguage:" + requestLocale.getLanguage());
                    if (language.getLanguageCode().equalsIgnoreCase(requestLocale.getLanguage())) {
                        languageId = language.getLanguageId();
                    }
                }
            }
            if (languageId == null && availableLanguagesForRepository.size() > 0) {
                languageId = ((LanguageVO) availableLanguagesForRepository.get(0)).getLanguageId();
            }
        } 
        catch (Exception e) 
        {
            logger.error("Failed to fetch available languages for repository " + repositoryId);
        }

        if (languageId == null)
            throw new ServletException("Unable to determine language for repository " + repositoryId);

        session.setAttribute(FilterConstants.LANGUAGE_ID, languageId);
        
        return languageId;
    }

    private Integer getContentId(HttpServletRequest request) throws ServletException, Exception 
    {
        Integer contentId = null;
        
    	String contentIdString = null;
        if (request.getParameter("contentId") != null) 
        {
            contentIdString = request.getParameter("contentId");
        }
        else
        {
        	int cidIndex = request.getRequestURL().indexOf(".cid");
        	int cidIndexEnd = request.getRequestURL().indexOf("?", cidIndex);
        	if(cidIndexEnd == -1)
        		cidIndexEnd = request.getRequestURL().indexOf("/", cidIndex);

        	if(cidIndex > -1)
        	{
        		if(cidIndexEnd == -1)
        			contentIdString = request.getRequestURL().substring(cidIndex + 4);
        		else
        			contentIdString = request.getRequestURL().substring(cidIndex + 4, cidIndexEnd);
        	}
        }

        try 
        {
        	contentId = Integer.valueOf(contentIdString);
        } 
        catch (NumberFormatException e) {}

        return contentId;
    }

    
    // @TODO should I URLDecode the strings first? (incl. context path)
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
    
    private String[] splitString(String str, String delimiter) {
        List list = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens()) {
            // Updated to handle portal-url:s
            String t = st.nextToken();
            if (t.startsWith("_")) {
                break;
            } else {
                // Not related to portal - add
                list.add(t.trim());
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private HttpServletRequest prepareRequest(HttpServletRequest request, Integer siteNodeId, Integer languageId, Integer contentId) 
    {
        HttpServletRequest wrappedRequest = new IGHttpServletRequest(request, siteNodeId, languageId, contentId);

        return wrappedRequest;
    }
    

    private class IGHttpServletRequest extends HttpServletRequestWrapper 
    {
        Map requestParameters = new HashMap();
        
        public IGHttpServletRequest(HttpServletRequest httpServletRequest, Integer siteNodeId, Integer languageId, Integer contentId) 
        {
    		super(httpServletRequest);
    		
    		requestParameters.putAll(httpServletRequest.getParameterMap());
            requestParameters.put("siteNodeId", new String[] { String.valueOf(siteNodeId)});
            requestParameters.put("languageId", new String[] { String.valueOf(languageId)});

            if(contentId != null)
            	requestParameters.put("contentId", new String[] { String.valueOf(contentId)});
            else
            {
	            if (requestParameters.get("contentId") == null)
	                requestParameters.put("contentId", new String[] { String.valueOf(-1)});
            }
            
            String originalServletPath = ((HttpServletRequest)httpServletRequest).getServletPath();
            String originalRequestURL = ((HttpServletRequest)httpServletRequest).getRequestURL().toString();
            String originalQueryString = ((HttpServletRequest)httpServletRequest).getQueryString();

            requestParameters.put("originalServletPath", new String[] { originalServletPath });
    		requestParameters.put("originalRequestURL", new String[] { originalRequestURL });
    		if(originalQueryString != null && originalQueryString.length() > 0)
    			requestParameters.put("originalQueryString", new String[] { originalQueryString });
    			
            //logger.info("siteNodeId:" + siteNodeId);
            //logger.info("languageId:" + languageId);
            //logger.info("contentId:" + requestParameters.get("contentId"));
        }

        public String getParameter(String s) 
        {
            String[] array = (String[]) requestParameters.get(s);
            if (array != null && array.length > 0)
                return array[0];
        
            return null;
        }

        public Map getParameterMap() 
        {
            return Collections.unmodifiableMap(requestParameters);
        }

        public Enumeration getParameterNames() 
        {
            return new ParameterNamesEnumeration(requestParameters.keySet().iterator());
        }

        public String[] getParameterValues(String s) 
        {
            String[] array = (String[]) requestParameters.get(s);
            if (array != null && array.length > 0)
                return array;
            
            return null;
        }
        
    }

    private class ParameterNamesEnumeration implements Enumeration {
        Iterator it = null;

        public ParameterNamesEnumeration(Iterator it) {
            this.it = it;
        }

        public boolean hasMoreElements() {
            return it.hasNext();
        }

        public Object nextElement() {
            return it.next();
        }

    }

}