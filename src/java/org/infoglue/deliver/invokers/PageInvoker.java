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

package org.infoglue.deliver.invokers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.NoBaseTemplateFoundException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.portal.PortalController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.CompressionHelper;
import org.infoglue.deliver.util.JSMin;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

/**
 * @author Mattias Bogeblad
 *
 * This interface defines what a Invoker of a page have to be able to do.
 * The invokers are used to deliver a page to the user in a certain fashion.
 *
 */

public abstract class PageInvoker
{	
    private final static Logger logger = Logger.getLogger(PageInvoker.class.getName());

	private static CompressionHelper compressionHelper = new CompressionHelper();
	private final static VisualFormatter vf = new VisualFormatter();

    private DatabaseWrapper dbWrapper				= null;
	private HttpServletRequest request				= null;
	private HttpServletResponse response 			= null;
	private TemplateController templateController	= null;
	private DeliveryContext deliveryContext 		= null;
	
	private String pageString	 					= null;
	
	/*public PageInvoker()
	{
	}
	*/
	
	/**
	 * The default constructor for PageInvokers. 
	 * @param request
	 * @param response
	 * @param templateController
	 * @param deliveryContext
	 */
	/*
	public PageInvoker(HttpServletRequest request, HttpServletResponse response, TemplateController templateController, DeliveryContext deliveryContext)
	{
		this.request = request;
		this.response = response;
		this.templateController = templateController;
		this.deliveryContext = deliveryContext;
		this.templateController.setDeliveryContext(this.deliveryContext);
	}
	*/

	/**
	 * This method should return an instance of the class that should be used for page editing inside the tools or in working. 
	 * Makes it possible to have an alternative to the ordinary delivery optimized class.
	 */
	
	public abstract PageInvoker getDecoratedPageInvoker(TemplateController templateController) throws SystemException;
	
	/**
	 * The default initializer for PageInvokers. 
	 * @param request
	 * @param response
	 * @param templateController
	 * @param deliveryContext
	 */

	public void setParameters(DatabaseWrapper dbWrapper, HttpServletRequest request, HttpServletResponse response, TemplateController templateController, DeliveryContext deliveryContext)
	{
	    this.dbWrapper = dbWrapper;
		this.request = request;
		this.response = response;
		this.templateController = templateController;
		this.deliveryContext = deliveryContext;
		this.templateController.setDeliveryContext(this.deliveryContext);
	}
	
    public Database getDatabase() throws SystemException
    {
        /*
        if(this.db == null || this.db.isClosed() || !this.db.isActive())
        {
            beginTransaction();
        }
        */
        return dbWrapper.getDatabase();
    }

    
	/**
	 * This is the method that will deliver the page to the user. It can have special
	 * handling of all sorts to enable all sorts of handlers. An example of uses might be to
	 * be to implement a WAP-version of page delivery where you have to set certain headers in the response
	 * or a redirect page which just redirects you to another page.  
	 */
	
	public abstract void invokePage() throws SystemException, Exception;
	

	/**
	 * This method is used to send the page out to the browser or other device.
	 * Override this if you need to set other headers or do other specialized things.
	 */

	public void deliverPage() throws NoBaseTemplateFoundException, Exception
	{
		if(logger.isInfoEnabled())
		{
			logger.info("PageKey:" + this.getDeliveryContext().getPageKey());
			logger.info("PageCache:" + this.getDeliveryContext().getDisablePageCache());
		}
		
		LanguageVO languageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(getDatabase(), this.getTemplateController().getLanguageId());
		
		if(logger.isInfoEnabled())
			logger.info("languageVO:" + languageVO);
		
		if(languageVO == null)
			throw new SystemException("There was no such active language for the page with languageId:" + this.getTemplateController().getLanguageId());
		
		String isPageCacheOn = CmsPropertyHandler.getIsPageCacheOn();
		String refresh = this.getRequest().getParameter("refresh");
		
		String pageCacheName = "pageCache"; // + this.getTemplateController().getRepositoryId();
		String pageCacheExtraName = "pageCacheExtra"; // + this.getTemplateController().getRepositoryId();
		
		if(logger.isInfoEnabled())
			logger.info("isPageCacheOn:" + isPageCacheOn);
		
		if(isPageCacheOn.equalsIgnoreCase("true") && (refresh == null || !refresh.equalsIgnoreCase("true")) && getRequest().getMethod().equals("GET"))
		{
			Map cachedExtraData = null;

			Integer pageCacheTimeout = (Integer)CacheController.getCachedObjectFromAdvancedCache("pageCacheExtra", this.getDeliveryContext().getPageKey() + "_pageCacheTimeout");
			if(pageCacheTimeout == null)
				pageCacheTimeout = this.getTemplateController().getPageCacheTimeout();
			
			//if(compressPageCache != null && compressPageCache.equalsIgnoreCase("true"))
			//{
				if(pageCacheTimeout == null)
				{
				    //cachedCompressedData = (byte[])CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey());
					Class[] argsClasses = new Class[2];
					argsClasses[0] = String.class;
					argsClasses[1] = String.class;
					
					Object[] args = new Object[]{pageCacheName, pageCacheExtraName};
					
					this.pageString = (String)CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey(), true, "utf-8", true, this, this.getClass().getMethod("invokeAndDecoratePage", argsClasses), args, this);
				    cachedExtraData = (Map)CacheController.getCachedObjectFromAdvancedCache(pageCacheExtraName, this.getDeliveryContext().getPageKey());
				}
				else
				{
				    //cachedCompressedData = (byte[])CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey(), pageCacheTimeout.intValue());
					this.pageString = (String)CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey(), pageCacheTimeout.intValue(), true, "utf-8", false);
				    cachedExtraData = (Map)CacheController.getCachedObjectFromAdvancedCache(pageCacheExtraName, this.getDeliveryContext().getPageKey(), pageCacheTimeout.intValue());
				}
				
			    if(cachedExtraData != null)
			    	this.getDeliveryContext().populateExtraData(cachedExtraData);
			//}
			/*
			else
			/
			{
				if(pageCacheTimeout == null)
				{
					//this.pageString = (String)CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey());
					this.pageString = (String)CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey(), true, "utf-8", false);
					cachedExtraData = (Map)CacheController.getCachedObjectFromAdvancedCache(pageCacheExtraName, this.getDeliveryContext().getPageKey());
				}
				else
				{
					//this.pageString = (String)CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey(), pageCacheTimeout.intValue());
					this.pageString = (String)CacheController.getCachedObjectFromAdvancedCache(pageCacheName, this.getDeliveryContext().getPageKey(), pageCacheTimeout.intValue(), true, "utf-8", false);
					cachedExtraData = (Map)CacheController.getCachedObjectFromAdvancedCache(pageCacheExtraName, this.getDeliveryContext().getPageKey(), pageCacheTimeout.intValue());
				}
			    if(cachedExtraData != null)
			    	this.getDeliveryContext().populateExtraData(cachedExtraData);					
			}
			*/

			//String invokeAndDecoratePage();
			/*
			if(this.pageString == null)
			{
			invokePage();
			this.pageString = getPageString();

			//TEST
			getLastModifiedDateTime();
			//END TEST
			
			pageString = decorateHeadAndPageWithVarsFromComponents(pageString);

			if(!this.getTemplateController().getIsPageCacheDisabled() && !this.getDeliveryContext().getDisablePageCache()) //Caching page if not disabled
			{
				Integer newPageCacheTimeout = getDeliveryContext().getPageCacheTimeout();
				if(newPageCacheTimeout == null)
					newPageCacheTimeout = this.getTemplateController().getPageCacheTimeout();
				
				String pageKey = this.getDeliveryContext().getPageKey();
				String[] allUsedEntitiesCopy = this.getDeliveryContext().getAllUsedEntities().clone();
				Object extraData = this.getDeliveryContext().getExtraData();
				
				String compressPageCache = CmsPropertyHandler.getCompressPageCache();
				//logger.info("compressPageCache:" + compressPageCache);
			    if(compressPageCache != null && compressPageCache.equalsIgnoreCase("true"))
				{
					long startCompression = System.currentTimeMillis();
					byte[] compressedData = compressionHelper.compress(this.pageString);		
				    //logger.info("Compressing page for pageCache took " + (System.currentTimeMillis() - startCompression) + " with a compressionFactor of " + (this.pageString.length() / compressedData.length));
					if(this.getTemplateController().getOperatingMode().intValue() == 3 && !CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
					{
						//CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, compressedData, allUsedEntitiesCopy, false);
					    CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, compressedData, allUsedEntitiesCopy, false, true, false, "utf-8");
					    //CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_usedEntities", allUsedEntitiesCopy, allUsedEntitiesCopy, true);
						CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, false);
			    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, false);    
					}
					else
					{
					    //CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, compressedData, allUsedEntitiesCopy, true);
					    CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, compressedData, allUsedEntitiesCopy, false, true, false, "utf-8");
					    //CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_usedEntities", allUsedEntitiesCopy, allUsedEntitiesCopy, true);
					    CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, true);
			    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, true);    
					}
				}
			    else
			    {
			        if(this.getTemplateController().getOperatingMode().intValue() == 3 && !CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
			        {
			        	//CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, pageString, allUsedEntitiesCopy, false);
			        	CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, pageString, allUsedEntitiesCopy, false, true, false, "utf-8");
			        	//CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_usedEntities", allUsedEntitiesCopy, allUsedEntitiesCopy, true);
			    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, false);
			    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, false);    
			        }
			    	else
			    	{
			    		//CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, pageString, allUsedEntitiesCopy, true);    
					    CacheController.cacheObjectInAdvancedCache(pageCacheName, pageKey, pageString, allUsedEntitiesCopy, false, true, false, "utf-8");
					    //CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_usedEntities", allUsedEntitiesCopy, allUsedEntitiesCopy, true);
			    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey, extraData, allUsedEntitiesCopy, true);
			    		CacheController.cacheObjectInAdvancedCache(pageCacheExtraName, pageKey + "_pageCacheTimeout", newPageCacheTimeout, allUsedEntitiesCopy, true);    
			    	}
			    }
			}
			else
			{
				if(logger.isInfoEnabled())
					logger.info("Page caching was disabled for the page " + this.getDeliveryContext().getSiteNodeId() + " with pageKey " + this.getDeliveryContext().getPageKey() + " - modifying the logic to enable page caching would boast performance.");
			}
		}
		else
		{
			if(logger.isInfoEnabled())
				logger.info("There was a cached copy..."); // + pageString);
		}
		*/
			
			//Caching the pagePath
			this.getDeliveryContext().setPagePath((String)CacheController.getCachedObject("pagePathCache", this.getDeliveryContext().getPageKey()));
			if(this.getDeliveryContext().getPagePath() == null)
			{
				this.getDeliveryContext().setPagePath(this.getTemplateController().getCurrentPagePath());
			
				if(!this.getTemplateController().getIsPageCacheDisabled() && !this.getDeliveryContext().getDisablePageCache()) //Caching page path if not disabled
					CacheController.cacheObject("pagePathCache", this.getDeliveryContext().getPageKey(), this.getDeliveryContext().getPagePath());
			}
			
			if(logger.isInfoEnabled())
				logger.info("Done caching the pagePath...");	
		}
		else
		{
			invokePage();
			this.pageString = getPageString();
			
			//TEST
			getLastModifiedDateTime();
			//END TEST

			pageString = decorateHeadAndPageWithVarsFromComponents(pageString);

			this.getDeliveryContext().setPagePath(this.templateController.getCurrentPagePath());
		}

		if(this.getRequest().getParameter("includeUsedEntities") != null && this.getRequest().getParameter("includeUsedEntities").equals("true") && (!CmsPropertyHandler.getOperatingMode().equals("3") || CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread")))
		{
			StringBuilder sb = new StringBuilder("<usedEntities>");
			String[] usedEntities = this.getDeliveryContext().getAllUsedEntities();
			for(int i=0; i<usedEntities.length; i++)
				sb.append(usedEntities[i]).append(",");
			sb.append("</usedEntities>");
			
			if(this.getTemplateController().getPageContentType() != null && (this.getTemplateController().getPageContentType().equalsIgnoreCase("text/css") || this.getTemplateController().getPageContentType().equalsIgnoreCase("text/javascript")))
				this.pageString = this.pageString + "/*" + sb.toString() + "*/";
			else
				this.pageString = this.pageString + "<!--" + sb.toString() + "-->";
		}
		
		String contentType = this.getTemplateController().getPageContentType();
		if(this.deliveryContext.getContentType() != null && !contentType.equalsIgnoreCase(this.deliveryContext.getContentType()))
		    contentType = this.deliveryContext.getContentType();

		if(!CmsPropertyHandler.getOperatingMode().equals("3"))
		{
			getResponse().setHeader("Cache-Control","no-cache"); 
	    	getResponse().setHeader("Pragma","no-cache");
	    	getResponse().setDateHeader ("Expires", 0);
		}
			
		//logger.info("pageString before:" + pageString);
		//pageString = decorateHeadAndPageWithVarsFromComponents(pageString);
		//logger.info("pageString after:" + pageString);
		
		try
		{
			//logger.info("ContentType:" + contentType);
			String charSet = languageVO.getCharset();
			if(contentType.indexOf("charset=") > -1)
			{
				try
				{
					int startIndex = contentType.indexOf("charset=");
					int endIndex = contentType.indexOf(";", startIndex + 1);
				
					if(endIndex != -1)
						charSet = contentType.substring(startIndex + "charset=".length(), endIndex).trim();
					else
						charSet = contentType.substring(startIndex + "charset=".length()).trim();
				
					if(logger.isInfoEnabled())
						logger.info("Found a user defined charset: " + charSet);
				}
				catch(Exception e)
				{
					logger.warn("Error parsing charset:" + e.getMessage());
				}
				this.getResponse().setContentType(contentType);
			}
			else
				this.getResponse().setContentType(contentType + "; charset=" + languageVO.getCharset());
			
			if(logger.isInfoEnabled())
				logger.info("Using charset: " + charSet);
			
			Iterator headersIterator = this.getDeliveryContext().getHttpHeaders().keySet().iterator();
			while(headersIterator.hasNext())
			{
				String key = (String)headersIterator.next();
				Object valueObject = this.getDeliveryContext().getHttpHeaders().get(key);
				if(valueObject instanceof Date)
				{
					Date value = (Date)valueObject;
					this.getResponse().setDateHeader(key, value.getTime());
				}
				else
				{
					String value = (String)valueObject;
					this.getResponse().setHeader(key, value);				
				}
			}
			
			if(logger.isInfoEnabled())
				logger.info("contentType:" + contentType + "; charset=" + languageVO.getCharset());
			
			String compressPageResponse = CmsPropertyHandler.getCompressPageResponse();
			if(logger.isInfoEnabled())
				logger.info("compressPageResponse:" + compressPageResponse);
		
			if(compressPageResponse != null && compressPageResponse.equalsIgnoreCase("true"))
			{
				OutputStream out = null;
				
				String encodings = this.getRequest().getHeader("Accept-Encoding");
			    if (encodings != null && encodings.indexOf("gzip") != -1) 
			    {
			    	this.getResponse().setHeader("Content-Encoding", "gzip");
			    	out = new GZIPOutputStream(this.getResponse().getOutputStream());
			    }
			    else if (encodings != null && encodings.indexOf("compress") != -1) 
			    {
			    	this.getResponse().setHeader("Content-Encoding", "x-compress");
			    	out = new ZipOutputStream(this.getResponse().getOutputStream());
			    	((ZipOutputStream)out).putNextEntry(new ZipEntry("dummy name"));
			    }
			    else 
			    {
			    	out = this.getResponse().getOutputStream();
			    }
			   
			    out.write(pageString.getBytes(charSet));
			    //out.write(pageString.getBytes(languageVO.getCharset()));
				out.flush();
				out.close();
			}
			else
			{			
				PrintWriter out = this.getResponse().getWriter();
				out.println(pageString);
				out.flush();
				out.close();
	    	}	    
			
			if(logger.isInfoEnabled())
				logger.info("sent all data to client:" + pageString.length());
    	}
		catch(IllegalStateException e)
		{
			logger.error("There was an IllegalStateException when trying to write output for URL: " + this.getTemplateController().getOriginalFullURL() + "\nMessage: " + e.getMessage());
		}
	}

	public String invokeAndDecoratePage(String pageCacheName, String pageCacheExtraName) throws SystemException, Exception, Bug 
	{
		if(this.pageString == null)
		{
			invokePage();
			this.pageString = getPageString();

			//TEST
			getLastModifiedDateTime();
			//END TEST
			
			this.pageString = decorateHeadAndPageWithVarsFromComponents(pageString);
		}
		else
		{
			if(logger.isInfoEnabled())
				logger.info("There was a cached copy..."); // + pageString);
		}
		return this.pageString;
	}

	private void getLastModifiedDateTime() throws Bug
	{
		if(CmsPropertyHandler.getSetDerivedLastModifiedInLive().equalsIgnoreCase("false"))
			return;
		
		Integer maxNumberOfVersionsForDerivedLastModifiedInLive = CmsPropertyHandler.getMaxNumberOfVersionsForDerivedLastModifiedInLive();
		
		Date lastModifiedDateTime = null;
		//logger.info("UsedContentVersions:" + this.deliveryContext.getUsedContentVersions().size());
		Timer t = new Timer();
		if(this.deliveryContext.getUsedContentVersions().size() > 0)
		{
			Iterator userContentVersionIterator = this.deliveryContext.getUsedContentVersions().iterator();
			int processed = 0;
			while(userContentVersionIterator.hasNext())
			{
				String usedContentVersion = (String)userContentVersionIterator.next();	
				if(usedContentVersion != null && usedContentVersion.startsWith("contentVersion_"))
		    	{
		    		try
		            {
		    			String versionId = usedContentVersion.substring(15);
		    			if(!versionId.equals("null") && !versionId.equals(""))
		    			{
		    				processed++;
		    				Integer contentVersionId = new Integer(versionId);
			    			SmallestContentVersionVO contentVersion = ContentVersionController.getContentVersionController().getSmallestContentVersionVOWithId(contentVersionId, getDatabase());
			    			if(lastModifiedDateTime == null || contentVersion.getModifiedDateTime().after(lastModifiedDateTime))
			    			{
			    				lastModifiedDateTime = contentVersion.getModifiedDateTime();
			    			}
		    			}
		            }
		    		catch (Exception e) 
		    		{
		    			logger.warn("Could not use the versionId:" + usedContentVersion + " - reason:" + e.getMessage());
					}
		    	}
				if(lastModifiedDateTime != null)
				{
					long current = System.currentTimeMillis() - lastModifiedDateTime.getTime();
					if(processed > maxNumberOfVersionsForDerivedLastModifiedInLive || current < (1000 * 60 * 10))
						break;
				}
			}
			if(lastModifiedDateTime != null)
				this.deliveryContext.setLastModifiedDateTime(lastModifiedDateTime);
		}
		
		long elapsedTime = t.getElapsedTime();
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLastModifiedDateTime", elapsedTime);
	}

	protected String decorateHeadAndPageWithVarsFromComponents(String pageString)
	{
		if(pageString.length() < 500000)
		{
			pageString = this.getTemplateController().decoratePage(pageString);
			
			StringBuilder sb = null;
			Timer t = new Timer();
			this.generateExtensionBundles(this.getTemplateController().getDeliveryContext().getScriptExtensionHeadBundles(), "text/javascript", "head");
			this.generateExtensionBundles(this.getTemplateController().getDeliveryContext().getScriptExtensionBodyBundles(), "text/javascript", "body");
			this.generateExtensionBundles(this.getTemplateController().getDeliveryContext().getCSSExtensionBundles(), "text/css", "head");
			
			Set htmlHeadItems = this.getTemplateController().getDeliveryContext().getHtmlHeadItems();
			if(htmlHeadItems != null && htmlHeadItems.size() > 0)
			{
				int indexOfHeadEndTag = pageString.indexOf("</head");
				if(indexOfHeadEndTag == -1)
					indexOfHeadEndTag = pageString.indexOf("</HEAD");
	
				if(indexOfHeadEndTag != -1)
				{
					sb = new StringBuilder(pageString);
					String headerItems = "";
					Iterator htmlHeadItemsIterator = htmlHeadItems.iterator();
					while(htmlHeadItemsIterator.hasNext())
					{
						String value = (String)htmlHeadItemsIterator.next();
						//logger.info("headItem:" + value);
						headerItems = headerItems + value + "\n";
					}
					sb.insert(indexOfHeadEndTag, headerItems);
					//pageString = sb.toString();
				}
			}

			Set htmlBodyEndItems = this.getTemplateController().getDeliveryContext().getHtmlBodyEndItems();
			if(htmlBodyEndItems != null && htmlBodyEndItems.size() > 0)
			{
				if(sb == null)
					sb = new StringBuilder(pageString);

				int indexOfBodyEndTag = sb.indexOf("</body");
				if(indexOfBodyEndTag == -1)
					indexOfBodyEndTag = sb.indexOf("</BODY");
	
				if(indexOfBodyEndTag != -1)
				{
					String bodyItems = "";
					Iterator htmlBodyItemsIterator = htmlBodyEndItems.iterator();
					while(htmlBodyItemsIterator.hasNext())
					{
						String value = (String)htmlBodyItemsIterator.next();
						//logger.info("headItem:" + value);
						bodyItems = bodyItems + value + "\n";
					}
					sb.insert(indexOfBodyEndTag, bodyItems);
					//pageString = sb.toString();
				}
			}
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("pageInvoker", t.getElapsedTime());
			
			try
			{
				int lastModifiedDateTimeIndex;
				if(sb == null)
					lastModifiedDateTimeIndex = pageString.indexOf("<ig:lastModifiedDateTime");
				else
					lastModifiedDateTimeIndex = sb.indexOf("<ig:lastModifiedDateTime");
					
				//logger.info("OOOOOOOOOOOOO lastModifiedDateTimeIndex:" + lastModifiedDateTimeIndex);
				if(lastModifiedDateTimeIndex > -1)
				{
					if(sb == null)
						sb = new StringBuilder(pageString);

					int lastModifiedDateTimeEndIndex = sb.indexOf("</ig:lastModifiedDateTime>", lastModifiedDateTimeIndex);
	
					String tagInfo = sb.substring(lastModifiedDateTimeIndex, lastModifiedDateTimeEndIndex);
					//logger.info("tagInfo:" + tagInfo);
					String dateFormat = "yyyy-MM-dd HH:mm";
					int formatStartIndex = tagInfo.indexOf("format");
					if(formatStartIndex > -1)
					{
						int formatEndIndex = tagInfo.indexOf("\"", formatStartIndex + 8);
						if(formatEndIndex > -1)
							dateFormat = tagInfo.substring(formatStartIndex + 8, formatEndIndex);
					}
					//logger.info("dateFormat:" + dateFormat);
						
					String dateString = vf.formatDate(this.getTemplateController().getDeliveryContext().getLastModifiedDateTime(), this.getTemplateController().getLocale(), dateFormat);
					//logger.info("dateString:" + dateString);
					sb.replace(lastModifiedDateTimeIndex, lastModifiedDateTimeEndIndex + "</ig:lastModifiedDateTime>".length(), dateString);
					//logger.info("Replaced:" + lastModifiedDateTimeIndex + " to " + lastModifiedDateTimeEndIndex + "</ig:lastModifiedDateTime>".length() + " with " + dateString);
				}
			}
			catch (Exception e) 
			{
				logger.error("Problem setting lastModifiedDateTime:" + e.getMessage(), e);
			}
			
			if(sb != null)
				pageString = sb.toString();			
		}
		else
		{
			if(logger.isInfoEnabled())
				logger.info("pageString was to large (" + pageString.length() + ") so the headers was not inserted.");
		}
		
		return pageString;
	}

	private void generateExtensionBundles(Map<String,Set<String>> extensionBundles, String contentType, String targetElement) 
	{
		Timer t = new Timer();
		
		Set<String> bundledSignatures = new HashSet<String>();
		
		Iterator<String> scriptExtensionBundlesIterator = extensionBundles.keySet().iterator();
		while(scriptExtensionBundlesIterator.hasNext())
		{
			String bundleName = scriptExtensionBundlesIterator.next();
			if(logger.isInfoEnabled())
				logger.info("bundleName:" + bundleName);
			
			Set<String> scriptExtensionFileNames = extensionBundles.get(bundleName);
			
			if(scriptExtensionFileNames != null && scriptExtensionFileNames.size() > 0)
			{				
				String scriptBundle = "";

				int i = 0;
				String filePath = CmsPropertyHandler.getDigitalAssetPath0();
				while(filePath != null)
				{
					try
					{
						File extensionsDirectory = new File(filePath + File.separator + "extensions");
						extensionsDirectory.mkdirs();
						
						File extensionsBundleFile = new File(filePath + File.separator + "extensions" + File.separator + bundleName + ".js");
						if(contentType.equalsIgnoreCase("text/css"))
							extensionsBundleFile = new File(filePath + File.separator + "extensions" + File.separator + bundleName + ".css");
						
						if(!extensionsBundleFile.exists())
						{	
							if(logger.isInfoEnabled())
								logger.info("No script - generating:" + bundleName);
							if(scriptBundle.equals(""))
							{
								Iterator<String> scriptExtensionFileNamesIterator = scriptExtensionFileNames.iterator(); 
								while(scriptExtensionFileNamesIterator.hasNext())
								{
									String scriptExtensionFileName = scriptExtensionFileNamesIterator.next();
									if(logger.isInfoEnabled())
										logger.info("scriptExtensionFileName:" + scriptExtensionFileName);
									try
									{
										File file = new File(filePath + File.separator + scriptExtensionFileName);
										String signature = "" + file.getName() + "_" + file.length();
										if(logger.isInfoEnabled())
											logger.info("Checking file:" + filePath + File.separator + scriptExtensionFileName);
										
										if(file.exists() && !bundledSignatures.contains(signature))
										{
											StringBuffer content = new StringBuffer(FileHelper.getFileAsStringOpt(file));
											//Wonder what is the best signature..
											bundledSignatures.add(signature);
											
											//If CSS we should change url:s to point to the original folder
											if(contentType.equalsIgnoreCase("text/css"))
											{
												if(logger.isInfoEnabled())
													logger.info("contentType:" + contentType);
												String extensionPath = file.getPath().substring(extensionsDirectory.getPath().length() + 1, file.getPath().lastIndexOf("/") + 1);
												if(logger.isInfoEnabled())
													logger.info("extensionPath:" + extensionPath);
												
												int urlStartIndex = content.indexOf("url(");
												while(urlStartIndex > -1)
												{
													if(content.charAt(urlStartIndex + 4) == '"' || content.charAt(urlStartIndex + 4) == '\'')
														content.insert(urlStartIndex + 5, extensionPath);
													else
														content.insert(urlStartIndex + 4, extensionPath);
													urlStartIndex = content.indexOf("url(", urlStartIndex + extensionPath.length());
												}
											}	
											//logger.info("transformed content:" + content.substring(0, 500));

											scriptBundle = scriptBundle + "\n\n" + content;
										}
										else
										{
											if(logger.isInfoEnabled())
												logger.info("Not adding:" + signature + " as " + file.exists() + ":" + bundledSignatures.contains(signature));
										}
									}
									catch (Exception e) 
									{
										logger.warn("Error trying to parse file and bundle it (" + scriptExtensionFileName + "):" + e.getMessage());
									}
								}
							}
							
							if(logger.isInfoEnabled())
								logger.info("scriptBundle:" + scriptBundle.length());
							if(scriptBundle != null && !scriptBundle.equals(""))
							{
								if(contentType.equalsIgnoreCase("text/javascript"))
								{
									try 
									{
										JSMin jsmin = new JSMin(new ByteArrayInputStream(scriptBundle.getBytes()), new FileOutputStream(extensionsBundleFile));
										jsmin.jsmin();
									} 
									catch (FileNotFoundException e) 
									{
										e.printStackTrace();
									}
								}
								else
								{
									FileHelper.writeToFile(extensionsBundleFile, scriptBundle, false);
								}
								if(logger.isInfoEnabled())
									logger.info("extensionsBundleFile:" + extensionsBundleFile.length());
							}
						}
						
						i++;
						filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
					}
					catch (Exception e) 
					{
						logger.warn("Error trying to write bundled scripts:" + e.getMessage());
					}
				}
				try
				{
					SiteNode siteNode = NodeDeliveryController.getNodeDeliveryController(deliveryContext).getSiteNode(getDatabase(), getTemplateController().getSiteNodeId());
					String dnsName = CmsPropertyHandler.getWebServerAddress();
					if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
						dnsName = siteNode.getRepository().getDnsName();

					String bundleUrl = "";
					String bundleUrlTag = "";
					
					if(contentType.equalsIgnoreCase("text/javascript"))
					{
						bundleUrl = URLComposer.getURLComposer().composeDigitalAssetUrl(dnsName, "extensions", bundleName + ".js", deliveryContext); 
						bundleUrlTag = "<script type=\"text/javascript\" src=\"" + bundleUrl + "\"></script>";
					}
					else if(contentType.equalsIgnoreCase("text/css"))
					{
						bundleUrl = URLComposer.getURLComposer().composeDigitalAssetUrl(dnsName, "extensions", bundleName + ".css", deliveryContext); 
						bundleUrlTag = "<link href=\"" + bundleUrl + "\" rel=\"stylesheet\" type=\"text/css\" />";
					}

					if(targetElement.equalsIgnoreCase("head"))
						this.getTemplateController().getDeliveryContext().getHtmlHeadItems().add(bundleUrlTag);
					else
						this.getTemplateController().getDeliveryContext().getHtmlBodyEndItems().add(bundleUrlTag);
				}
				catch (Exception e) 
				{
					logger.warn("Error trying  get assetBaseUrl:" + e.getMessage());
				}
			}

		}
		if(logger.isInfoEnabled())
			t.printElapsedTime("Generating bundles took");	
	}
				
	/**
	 * This method is used to allow pagecaching on a general level.
	 */

	public void cachePage()
	{
		
	}
	
	public final DeliveryContext getDeliveryContext()
	{
		return deliveryContext;
	}

	public final HttpServletRequest getRequest()
	{
		return request;
	}

	public final HttpServletResponse getResponse()
	{
		return response;
	}

	public final TemplateController getTemplateController()
	{
		return templateController;
	}

	public String getPageString()
	{
		return pageString;
	}

	public void setPageString(String string)
	{
	    if(string != null && this.deliveryContext.getTrimResponse())
	    	string = string.trim();

		pageString = string;
	}

	
	/**
	 * Creates and returns a defaultContext, currently with the templateLogic 
	 * and if the portal support is enabled the portalLogic object. 
	 * (Added to avoid duplication of context creation in the concrete 
	 * implementations of pageInvokers)
	 * @author robert
	 * @return A default context with the templateLogic and portalLogic object in it.
	 */
	
	public Map getDefaultContext() 
	{
		Map context = new HashMap();
		context.put("templateLogic", getTemplateController());		
		
		// -- check if the portal is active
        String portalEnabled = CmsPropertyHandler.getEnablePortal() ;
        boolean active = ((portalEnabled != null) && portalEnabled.equals("true"));
		if (active) 
		{
		    PortalController pController = new PortalController(getRequest(), getResponse());
		    context.put(PortalController.NAME, pController);
		    if(logger.isInfoEnabled())
		    {
		    	logger.info("PortalController.NAME:" + PortalController.NAME);
		    	logger.info("pController:" + pController);
		    }
		}
		
		return context;
	}
    
}
