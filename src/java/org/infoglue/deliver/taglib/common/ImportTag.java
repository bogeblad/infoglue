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

package org.infoglue.deliver.taglib.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.HttpHelper;
import org.infoglue.deliver.util.Timer;
import org.infoglue.deliver.util.ioqueue.CachingIOResultHandler;
import org.infoglue.deliver.util.ioqueue.HttpUniqueRequestQueue;
import org.infoglue.deliver.util.ioqueue.HttpUniqueRequestQueueBean;

public class ImportTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private final static Logger logger = Logger.getLogger(ImportTag.class.getName());

	private String url;
	private String charEncoding;
	private Map requestProperties = new HashMap();
	private Map requestParameters = new HashMap();
	private Integer timeout = new Integer(30000);
	
	private Boolean useCache = false;
	private String cacheName = "importTagResultCache";
	private String cacheKey = null;
	private Boolean useFileCacheFallback = false;
	private String fileCacheCharEncoding = null;
	private Integer cacheTimeout = new Integer(3600); 
	
	private HttpHelper helper = new HttpHelper();
	
    public ImportTag()
    {
        super();
    }

	/**
	 * Initializes the parameters to make it accessible for the children tags (if any).
	 * 
	 * @return indication of whether to evaluate the body or not.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doStartTag() throws JspException 
	{
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Generates the url and either sets the result attribute or writes the url
	 * to the output stream. 
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */

	public int doEndTag() throws JspException
    {
		String forceImportTagFileCaching = CmsPropertyHandler.getProperty("forceImportTagFileCaching");
		if(forceImportTagFileCaching != null && forceImportTagFileCaching.equals("true"))
		{
			useCache = true;
			useFileCacheFallback = true;
			fileCacheCharEncoding = "iso-8859-1";
			if(cacheTimeout != null)
				cacheTimeout = new Integer(3600); 
		}
		
		try
        {
			Timer t = new Timer();
			if(logger.isInfoEnabled())
			{
				logger.info("useCache:" + useCache);
				logger.info("cacheKey:" + cacheKey);
				logger.info("useFileCacheFallback:" + useFileCacheFallback);
				logger.info("cacheTimeout:" + cacheTimeout);
			}

			if(fileCacheCharEncoding == null)
				fileCacheCharEncoding = charEncoding;

			if(!useCache && !useFileCacheFallback)
			{
				if(logger.isInfoEnabled())
					logger.info("Calling url directly - no cache...");
				String result = helper.getUrlContent(url, requestProperties, requestParameters, charEncoding, timeout.intValue());
				produceResult(result);
			}
			else
			{
				String completeUrl = url + "_" + helper.toEncodedString(requestParameters, charEncoding) + "_" + charEncoding + "_" + fileCacheCharEncoding;
				
				String localCacheKey = "result_" + completeUrl;
				if(cacheKey != null && !cacheKey.equals(""))
					localCacheKey = cacheKey;

				if(logger.isInfoEnabled())
					logger.info("localCacheKey:" +localCacheKey);
				
				CachingIOResultHandler resultHandler = new CachingIOResultHandler();
				resultHandler.setCacheKey(localCacheKey);
				resultHandler.setCacheName(cacheName);
				resultHandler.setUseMemoryCache(useCache);
				resultHandler.setUseFileCacheFallback(useFileCacheFallback);
				resultHandler.setFileCacheCharEncoding(fileCacheCharEncoding);

				String cachedResult = null;
				boolean callInBackground = false;
				
				if(logger.isInfoEnabled())
					logger.info("Using some cache (useCache:" + useCache + ", useFileCacheFallback:" + useFileCacheFallback + ", cacheTimeout:" + cacheTimeout.intValue() + ")");

				cachedResult = (String)CacheController.getCachedObjectFromAdvancedCache(cacheName, localCacheKey, cacheTimeout.intValue(), useFileCacheFallback, fileCacheCharEncoding, useCache);
				if(logger.isInfoEnabled())
					t.printElapsedTime("Getting timed cache result:" + cachedResult);
				
				if((cachedResult == null || cachedResult.equals("")))
				{
					logger.info("No cached result either in memory or in filecache - getting old if exists");
					cachedResult = (String)CacheController.getCachedObjectFromAdvancedCache(cacheName, localCacheKey, useFileCacheFallback, fileCacheCharEncoding, useCache);

					callInBackground = true;
				}
				
				if(cachedResult == null || cachedResult.equals(""))
				{
					if(logger.isInfoEnabled())
						logger.info("Calling url directly as last resort...");
					
					cachedResult = helper.getUrlContent(url, requestProperties, requestParameters, charEncoding, timeout.intValue());
					
					if(logger.isInfoEnabled())
						t.printElapsedTime("5 took..");
					resultHandler.handleResult(cachedResult);
					
					if(logger.isInfoEnabled())
						t.printElapsedTime("6 took..");
				}
				else if(callInBackground)
				{
					if(logger.isInfoEnabled())
						logger.info("Adding url to queue...");
					queueBean(resultHandler);
				}

				if(logger.isInfoEnabled())
					logger.info("Sending out the cached result...");
				
				produceResult(cachedResult);
			}
			if(logger.isInfoEnabled())
				t.printElapsedTime("Import took..");
        } 
		catch (Exception e)
        {
            logger.error("An error occurred when we on page '" + getController().getCurrentPageUrl() + "' tried during (" + timeout + " ms) to import the url:" + this.url + ":" + e.getMessage());
            logger.warn("An error occurred when we on page '" + getController().getCurrentPageUrl() + "' tried during (" + timeout + " ms) to import the url:" + this.url + ":" + e.getMessage(), e);
		    produceResult("");
        }
		
		this.useCache = false;
		this.cacheKey = null;
		this.cacheTimeout = new Integer(30000);
		this.useFileCacheFallback = false;
		this.charEncoding = null;
		this.fileCacheCharEncoding = null;
		
        return EVAL_PAGE;
    }

	private void queueBean(CachingIOResultHandler resultHandler)
	{
		if(logger.isInfoEnabled())
			logger.info("Calling url in background...");
		HttpUniqueRequestQueueBean bean = new HttpUniqueRequestQueueBean();
		bean.setEncoding(this.charEncoding);
		bean.setFetcher(helper);
		bean.setHandler(resultHandler);
		bean.setRequestParameters(requestParameters);
		bean.setRequestProperties(requestProperties);
		bean.setTimeout(timeout);
		bean.setUrlAddress(url);
		try
		{
			bean.setSerializedParameters(helper.toEncodedString(requestParameters, this.charEncoding));			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		HttpUniqueRequestQueue.getHttpUniqueRequestQueue().addHttpUniqueRequestQueueBean(bean);
	}

    public void setUrl(String url) throws JspException
    {
        this.url = evaluateString("importTag", "url", url);
    }
    
    public void setCharEncoding(String charEncoding) throws JspException
    {
        this.charEncoding = evaluateString("importTag", "charEncoding", charEncoding);
    }
    
    public void setTimeout(String timeout) throws JspException
    {
        this.timeout = evaluateInteger("importTag", "timeout", timeout);
    }

    public void setUseCache(String useCache) throws JspException
    {
        this.useCache = (Boolean)evaluate("importTag", "useCache", useCache, Boolean.class);
    }

    public void setUseFileCacheFallback(String useFileCacheFallback) throws JspException
    {
        this.useFileCacheFallback = (Boolean)evaluate("importTag", "useFileCacheFallback", useFileCacheFallback, Boolean.class);
    }

    public void setFileCacheCharEncoding(String fileCacheCharEncoding) throws JspException
    {
        this.fileCacheCharEncoding = evaluateString("importTag", "fileCacheCharEncoding", fileCacheCharEncoding);
    }

    public void setCacheName(String cacheName) throws JspException
    {
        this.cacheName = evaluateString("importTag", "cacheName", cacheName);
    }

    public void setCacheKey(String cacheKey) throws JspException
    {
        this.cacheKey = evaluateString("importTag", "cacheKey", cacheKey);
    }

    public void setCacheTimeout(String cacheTimeout) throws JspException
    {
        this.cacheTimeout = evaluateInteger("importTag", "cacheTimeout", cacheTimeout);
    }

    protected final void addProperty(final String name, final String value)
	{
		requestProperties.put(name, value);
	}

    protected final void addParameter(final String name, final String value)
	{
		requestParameters.put(name, value);
	}

}
