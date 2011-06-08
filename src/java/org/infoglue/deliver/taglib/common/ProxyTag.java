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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.common.webappintegrator.WebappIntegrator;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class ProxyTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private final static Logger logger = Logger.getLogger(ProxyTag.class.getName());

	private String url;
	private String charEncoding;
	private String referrer = null;
	private String userAgent = null;
	private String method = "GET";
	private String elementSelector = null;
	private Map<String,String> cookies = new HashMap<String,String>();
	private Map<String,String> requestProperties = new HashMap<String,String>();
	private Map<String,String> requestParameters = new HashMap<String,String>();
	private Integer timeout = new Integer(5000);
	
	private String proxyHost = null;
	private Integer proxyPort = null;

	private String hrefExclusionRegexp = "";
	private String linkExclusionRegexp = "";
	private String srcExclusionRegexp = "";

	/*
	private Boolean useCache = false;
	private String cacheName = "importTagResultCache";
	private String cacheKey = null;
	private Boolean useFileCacheFallback = false;
	private String fileCacheCharEncoding = null;
	private Integer cacheTimeout = new Integer(3600); 
	*/
	
    public ProxyTag()
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
		try
		{
			WebappIntegrator wi = new WebappIntegrator();
			
			if(getController().getHttpServletRequest().getMethod().equalsIgnoreCase("post"))
				wi.setMethod("post");
			
			wi.setReferrer(getController().getCurrentPageUrl());
			wi.setTimeout(timeout);
			wi.setUserAgent("" + getController().getHttpServletRequest().getHeader("user-agent"));

			Map<String,String> proxyCookies = (Map<String,String>)pageContext.getSession().getAttribute("proxyCookies");
			logger.info("proxyCookies:" + proxyCookies);
			if(proxyCookies != null)
			{
				for(Map.Entry<String,String> proxyCookie : proxyCookies.entrySet())
				{
					logger.info("Setting ProxyCookie: " + proxyCookie.getKey() + "=" + proxyCookie.getValue());
					cookies.put(proxyCookie.getKey(), proxyCookie.getValue());
				}
			}

			wi.setCookies(cookies);
			
			logger.info("Adding all params:" + getController().getHttpServletRequest().getParameterMap());
			Enumeration paramsEnumeration = getController().getHttpServletRequest().getParameterNames();
			while(paramsEnumeration.hasMoreElements())
			{
				String parameterName = (String)paramsEnumeration.nextElement();
				if(parameterName.equals("siteNodeId") || parameterName.equals("originalRequestURL") || parameterName.equals("refresh"))
					continue;
				
				String value = getController().getHttpServletRequest().getParameter(parameterName);
				
				if(parameterName.startsWith("igproxy_"))
					parameterName = parameterName.replaceAll("igproxy_", "");
					
				requestParameters.put(parameterName, value);
			}
			wi.setRequestParameters(requestParameters);

			logger.info("RequestHeaders:" + getController().getHttpServletRequest().getHeaderNames());
			Enumeration headersEnumeration = getController().getHttpServletRequest().getHeaderNames();
			while(headersEnumeration.hasMoreElements())
			{
				String headerName = (String)headersEnumeration.nextElement();
				logger.info("headerName:" + headerName + "=" + getController().getHttpServletRequest().getHeader(headerName));
				requestProperties.put(headerName, getController().getHttpServletRequest().getHeader(headerName));
			}
			
			wi.setRequestProperties(requestProperties);

			wi.setCurrentBaseUrl(getController().getCurrentPageUrl());
			wi.setUrlToIntegrate(url);
			logger.info("this.elementSelector:" + this.elementSelector);
			wi.setElementSelector(this.elementSelector);
			
			wi.setProxyHost(proxyHost);
			wi.setProxyPort(proxyPort);
			
			Map<String,String> returnCookies = new HashMap<String,String>();
			Map<String,String> returnHeaders = new HashMap<String,String>();
			Map<String,String> returnStatus = new HashMap<String,String>();
			
			List<String> blockedParameters = new ArrayList<String>();
			blockedParameters.add("siteNodeId");
			blockedParameters.add("contentId");
			blockedParameters.add("languageId");
			blockedParameters.add("proxyUrl");
			produceResult(wi.integrate(returnCookies, returnHeaders, returnStatus, blockedParameters, hrefExclusionRegexp, linkExclusionRegexp, srcExclusionRegexp));
			
			pageContext.setAttribute("returnCookies", returnCookies);
			pageContext.setAttribute("returnHeaders", returnHeaders);
			pageContext.setAttribute("returnStatus", returnStatus);
			
			//Map<String,String> proxyCookies = (Map<String,String>)pageContext.getSession().getAttribute("proxyCookies");
			if(proxyCookies == null)
			{
				proxyCookies = new HashMap<String,String>();
				pageContext.getSession().setAttribute("proxyCookies", proxyCookies);
			}
			proxyCookies.putAll(returnCookies);
			
			/*
			for(Map.Entry<String, String> returnCookie : returnCookies.entrySet())
			{
				logger.info("Cookie returned:" + returnCookie.getKey() + "=" + returnCookie.getValue());
				this.getController().setCookie(returnCookie.getKey(), returnCookie.getValue(), null, null, -1);
			}
			*/
		}
		catch (Exception e)
        {
            logger.error("An error occurred when we tried during (" + timeout + " ms) to import the url:" + this.url + ":" + e.getMessage(), e);
		    produceResult("");
        }
		
		this.cookies.clear();
		this.charEncoding = null;
		this.method = null;
		this.referrer = null;
		this.requestParameters.clear();
		this.requestProperties.clear();
		this.timeout = 5000;
		this.url = null;
		this.userAgent = null;
		this.elementSelector = null;
		this.proxyHost = null;
		this.proxyPort = null;
		this.hrefExclusionRegexp = "";
		this.srcExclusionRegexp = "";
		this.linkExclusionRegexp = "";
		
		/*
		this.useCache = false;
		this.cacheKey = null;
		this.cacheTimeout = new Integer(30000);
		this.useFileCacheFallback = false;
		this.fileCacheCharEncoding = null;
		*/
        return EVAL_PAGE;
    }

    public void setElementSelector(String elementSelector) throws JspException
    {
        this.elementSelector = evaluateString("proxyTag", "elementSelector", elementSelector);
    }
    
    public void setCharEncoding(String charEncoding) throws JspException
    {
        this.charEncoding = evaluateString("proxyTag", "charEncoding", charEncoding);
    }
    
    public void setTimeout(String timeout) throws JspException
    {
        this.timeout = evaluateInteger("proxyTag", "timeout", timeout);
    }

    public void setUrl(String url) throws JspException
    {
        this.url = evaluateString("proxyTag", "url", url);
    }

    public void setProxyHost(String proxyHost) throws JspException
    {
        this.proxyHost = evaluateString("proxyTag", "proxyHost", proxyHost);
    }
    
    public void setProxyPort(String proxyPort) throws JspException
    {
        this.proxyPort = evaluateInteger("proxyTag", "proxyPort", proxyPort);
    }

	public void setHrefExclusionRegexp(String hrefExclusionRegexp) throws JspException
	{
		this.hrefExclusionRegexp = evaluateString("proxyTag", "hrefExclusionRegexp", hrefExclusionRegexp);
	}

	public void setLinkExclusionRegexp(String linkExclusionRegexp) throws JspException 
	{
		this.linkExclusionRegexp = evaluateString("proxyTag", "linkExclusionRegexp", linkExclusionRegexp);
	}

	public void setSrcExclusionRegexp(String srcExclusionRegexp) throws JspException 
	{
		this.srcExclusionRegexp = evaluateString("proxyTag", "srcExclusionRegexp", srcExclusionRegexp);
	}

    /*
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
	*/

    protected final void addCookie(final String name, final String value)
	{
		cookies.put(name, value);
	}

    protected final void addProperty(final String name, final String value)
	{
		requestProperties.put(name, value);
	}

    protected final void addParameter(final String name, final String value)
	{
		requestParameters.put(name, value);
	}

    protected final void addParameter(final String name, final String value, String scope)
	{
		if(scope.equalsIgnoreCase("requestProperty"))
			addProperty(name, value);
		else if(scope.equalsIgnoreCase("cookie"))
			addCookie(name, value);
		else
			requestParameters.put(name, value);
	}

}
