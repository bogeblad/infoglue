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

package org.infoglue.deliver.applications.databeans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;

/**
 * @author Mattias Bogeblad
 *
 * This class is used to store the context of a page and get and set information that is central to it.
 * TODO - write more
 */

public class DeliveryContext implements UsageListener
{
	private static final SimpleDateFormat HTTP_DATE_FORMAT = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH );

    private final static Logger logger = Logger.getLogger(DeliveryContext.class.getName());

	public static final String META_INFO_BINDING_NAME 					= "Meta information";
	public static final String TEMPLATE_ATTRIBUTE_NAME   				= "Template";
	public static final String TITLE_ATTRIBUTE_NAME     		 		= "Title";
	public static final String NAV_TITLE_ATTRIBUTE_NAME 		 		= "NavigationTitle";
	/*
	protected static final String DISABLE_PAGE_CACHE_ATTRIBUTE_NAME		= "DisablePageCache";
	protected static final String PAGE_CONTENT_TYPE_ATTRIBUTE_NAME		= "ContentType";
	protected static final String ENABLE_PAGE_PROTECTION_ATTRIBUTE_NAME = "ProtectPage";
	protected static final String DISABLE_EDIT_ON_SIGHT_ATTRIBUTE_NAME	= "DisableEditOnSight";
	*/
	
	public static final boolean USE_LANGUAGE_FALLBACK        	= true;
	public static final boolean DO_NOT_USE_LANGUAGE_FALLBACK 	= false;
	public static final boolean USE_INHERITANCE 				= true;
	public static final boolean DO_NOT_USE_INHERITANCE 			= false;
	
	//These are the standard parameters which uniquely defines which page to show.
	private Integer siteNodeId = null;
	private Integer contentId  = null; 
	private Integer languageId = null;
	
	//This sets the content type
	private String contentType = null;
	
	//Lets one disable caching of this page if needed for some requests.
	private boolean disablePageCache = false;

	//This decides if to show a minimalistic version of the page structure - not render all compoents etc.
	private boolean showSimple = false;

	//This parameter are set if you want to access a certain repository startpage
	private String repositoryName = null;

	private String pageKey = null;
	private String pagePath = null;
	
	private HttpServletResponse httpServletResponse = null;
	private HttpServletRequest httpServletRequest = null;
	//private Session session = null;
	//private InfoGlueAbstractAction infoglueAbstractAction = null;
	
	//This section has control over what contents and sitenodes are used where so the pagecache can be selectively updated.
	private List usageListeners = new ArrayList();
	
	private Set usedContents = Collections.synchronizedSet(new HashSet());
	private Set usedContentVersions = Collections.synchronizedSet(new HashSet());
	private Set usedSiteNodes = Collections.synchronizedSet(new HashSet());
	private Set usedSiteNodeVersions = Collections.synchronizedSet(new HashSet());
	
	private Set usedPageMetaInfoContentVersionIdSet = Collections.synchronizedSet(new HashSet());
	private Set usedPageComponentsMetaInfoContentVersionIdSet = Collections.synchronizedSet(new HashSet());
	
	private Date lastModifiedDateTime = null;
	private boolean registerLastModifiedDate = false;
	private boolean cachedResponse = false;
	
	//private InfoGluePrincipal infoGluePrincipal = null;
	
	//This variable sets if all urls generated should contain the server name etc.
	private boolean useFullUrl = false;
	
	//The variable sets if url generation should skip niceUris
	private boolean disableNiceUri = false;

	//The variable sets if the response string should be trimmed to avoid problems with xml-responses etc.
	private boolean trimResponse = false;

	//The variable sets if the full page should be rendered once more after all components have been rendered.
	private boolean evaluateFullPage = true;

	//The variable sets if the rendering should consider publish/expire dates when validating contents. Used in preview actions.
	private boolean validateOnDates = false;

	//This variable controls if digitalAssetUrl:s generated are directed to the DownloadAsset.action
	private boolean useDownloadAction = false;

	private Integer pageCacheTimeout = null;

	//This variable controls if tags and logic should consider the logged in editor principal even if the ordinary principal is anonymous when checking for access rights etc.
	private boolean considerEditorInDecoratedMode = true;
	
	//The variable sets if methods should skip using caches if possible
	private boolean defeatCaches = false;

	private String operatingMode = null;
	
	private Map pageAttributes = new HashMap();
	private Set htmlHeadItems = new HashSet();
	private Set<String> htmlBodyEndItems = new HashSet<String>();
	private Map<String, Set<String>> scriptExtensionHeadBundles = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> scriptExtensionBodyBundles = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> cssExtensionBundles = new HashMap<String, Set<String>>();
	private Map httpHeaders = new HashMap();
	
	//Possible place to put debug data for a request. It's limiting and should only be used for compact targeted debug.
	private boolean debugMode = false;
	private String debugInformation = "";

	public static DeliveryContext getDeliveryContext()
	{
		return new DeliveryContext();
	}

	public static DeliveryContext getDeliveryContext(boolean registerLastModifiedDate)
	{
		return new DeliveryContext(registerLastModifiedDate);
	}
	
	private DeliveryContext()
	{
		this(false);
	}

	private DeliveryContext(boolean registerLastModifiedDate)
	{
		this.registerLastModifiedDate = registerLastModifiedDate;
	}
	
	public void clear()
	{
		try
		{
			this.htmlHeadItems.clear();
			this.htmlBodyEndItems.clear();
			this.httpHeaders.clear();
			this.httpServletRequest = null;
			this.httpServletResponse = null;
			this.pageAttributes.clear();
			this.usageListeners.clear();
			this.usedContents.clear();
			this.usedContentVersions.clear();
			this.usedPageMetaInfoContentVersionIdSet.clear();
			this.usedSiteNodes.clear();
			this.usedSiteNodeVersions.clear();
			
			this.usedContents = null;
			this.usedContentVersions = null;
			this.usedPageMetaInfoContentVersionIdSet = null;
			this.usedSiteNodes = null;
			this.usedSiteNodeVersions = null;
			this.debugInformation = "";
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/*
	public static DeliveryContext getDeliveryContext(InfoGluePrincipal infoGluePrincipal)
	{
		return new DeliveryContext(infoGluePrincipal);
	}
	
	private DeliveryContext(InfoGluePrincipal infoGluePrincipal)
	{
		this.infoGluePrincipal = infoGluePrincipal;
	}
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

	public String getPageKey()
	{
		return this.pageKey;
	}

	public String getPagePath()
	{
		return this.pagePath;
	}

	public void setPageKey(String pageKey)
	{
		this.pageKey = pageKey;
	}

	public void setPagePath(String pagePath)
	{
		this.pagePath = pagePath;
	}
	
	/*
	public InfoGluePrincipal getPrincipal()
	{
		return this.infoGluePrincipal;
	}
	*/
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("siteNodeId:" + this.siteNodeId);
		sb.append("languageId:" + this.languageId);
		sb.append("contentId:" + this.contentId);
		//sb.append("InfoGluePrincipal:" + this.infoGluePrincipal);
		
		return sb.toString();
	}

	/*
	public Session getSession()
	{
		return this.session;
	}

	public void setSession(Session session)
	{
		this.session = session;
	}

	public InfoGlueAbstractAction getInfoGlueAbstractAction()
	{
		return infoglueAbstractAction;
	}

	public void setInfoGlueAbstractAction(InfoGlueAbstractAction action)
	{
	    infoglueAbstractAction = action;
	}
	*/

	public HttpServletRequest getHttpServletRequest() 
	{
		return httpServletRequest;
	}
	
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) 
	{
		this.httpServletRequest = httpServletRequest;
	}
	
	public HttpServletResponse getHttpServletResponse() 
	{
		return httpServletResponse;
	}
	
	public void setHttpServletResponse(HttpServletResponse httpServletResponse) 
	{
		this.httpServletResponse = httpServletResponse;
	}
	
    public void addUsedContent(String usedContent)
    {
        this.usedContents.add(usedContent);
        if(usedContent.indexOf("_", 8) > -1)
        	this.usedContents.add(usedContent.substring(0, usedContent.indexOf("_", 8)));
        	
        try
        {
	        Iterator iterator = this.getUsageListeners().iterator();
	        while(iterator.hasNext())
	        {
	            UsageListener usageListener = (UsageListener)iterator.next();
	            usageListener.addUsedContent(usedContent);
	        }
        }
        catch(Exception e)
        {
        	logger.warn("Sync issue when adding usedContent:" + e.getMessage());
        }
    }

    public void addUsedSiteNode(String usedSiteNode)
    {
        this.usedSiteNodes.add(usedSiteNode);
        
        try
        {
	        Iterator iterator = this.getUsageListeners().iterator();
	        while(iterator.hasNext())
	        {
	            UsageListener usageListener = (UsageListener)iterator.next();
	            usageListener.addUsedSiteNode(usedSiteNode);
	        }
        }
        catch(Exception e)
        {
        	logger.warn("Sync issue when adding usedSiteNode:" + e.getMessage());
        }
    }

    public void addUsedContentVersion(String usedContentVersion)
    {
        this.usedContentVersions.add(usedContentVersion);
        
        try
        {
	        Iterator iterator = this.getUsageListeners().iterator();
	        while(iterator.hasNext())
	        {
	            UsageListener usageListener = (UsageListener)iterator.next();
	            usageListener.addUsedContentVersion(usedContentVersion);
	        }
	    }
	    catch(Exception e)
	    {
        	logger.warn("Sync issue when adding usedContentVersion:" + e.getMessage());
	    }
    }

    public void addUsedSiteNodeVersion(String usedSiteNodeVersion)
    {
        this.usedSiteNodeVersions.add(usedSiteNodeVersion);
        
        try
        {
	        Iterator iterator = this.getUsageListeners().iterator();
	        while(iterator.hasNext())
	        {
	            UsageListener usageListener = (UsageListener)iterator.next();
	            usageListener.addUsedSiteNodeVersion(usedSiteNodeVersion);
	        }
	    }
	    catch(Exception e)
	    {
	    	logger.warn("Sync issue when adding usedSiteNodeVersion:" + e.getMessage());
	    }
    }

    public String[] getAllUsedEntities()
    {
        List list = new ArrayList();
        list.addAll(this.usedContents);
        list.addAll(this.usedContentVersions);
        list.addAll(this.usedSiteNodes);
        list.addAll(this.usedSiteNodeVersions);
        Object[] array = list.toArray();
        String[] groups = new String[array.length];
        for(int i=0; i<array.length; i++)
            groups[i] = array[i].toString();
        
        return groups;
    }
    
    public List<String> getAllUsedEntitiesAsSet()
    {
    	List<String> set = new ArrayList<String>();
    	set.addAll(this.usedContents);
    	set.addAll(this.usedContentVersions);
    	set.addAll(this.usedSiteNodes);
    	set.addAll(this.usedSiteNodeVersions);
        
        return set;
    }

    
    public List getUsageListeners()
    {
        return usageListeners;
    }

    public boolean getShowSimple()
    {
        return showSimple;
    }
    
    public void setShowSimple(boolean showSimple)
    {
        this.showSimple = showSimple;
    }
    
    public String getContentType()
    {
        return contentType;
    }
    
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }
    
    public boolean getDisablePageCache()
    {
        return disablePageCache;
    }
    
    public void setDisablePageCache(boolean disablePageCache)
    {
        this.disablePageCache = disablePageCache;
    }
    
    public boolean getUseFullUrl()
    {
        return useFullUrl;
    }
    
    public void setUseFullUrl(boolean useFullUrl)
    {
        this.useFullUrl = useFullUrl;
    }

    public boolean getDisableNiceUri()
    {
        return this.disableNiceUri;
    }
    
    public void setDisableNiceUri(boolean disableNiceUri)
    {
        this.disableNiceUri = disableNiceUri;
    }

    public boolean getTrimResponse()
    {
        return this.trimResponse;
    }

    public void setTrimResponse(boolean trimResponse) 
	{
		this.trimResponse = trimResponse;
	}

	public boolean getEvaluateFullPage() 
	{
		return evaluateFullPage;
	}

	public void setEvaluateFullPage(boolean evaluateFullPage) 
	{
		this.evaluateFullPage = evaluateFullPage;
	}

    public void setDefeatCaches(boolean defeatCaches, Map<Class, List<Object>> entities)
    {
        this.defeatCaches = defeatCaches;
        CacheController.setDefeatCaches(defeatCaches, entities);
    }

    public boolean getDefeatCaches()
    {
        return this.defeatCaches;
    }

	public boolean getValidateOnDates()
	{
		return validateOnDates;
	}

	public void setValidateOnDates(boolean validateOnDates)
	{
		this.validateOnDates = validateOnDates;
	}

    public void setConsiderEditorInDecoratedMode(boolean considerEditorInDecoratedMode)
    {
        this.considerEditorInDecoratedMode = considerEditorInDecoratedMode;
    }

    public boolean getConsiderEditorInDecoratedMode()
    {
        return this.considerEditorInDecoratedMode;
    }

	public Set getUsedPageMetaInfoContentVersionIdSet() 
	{
		return usedPageMetaInfoContentVersionIdSet;
	}

	public void addUsedPageComponentsMetaInfoContentVersionId(Integer contentVersionId) 
	{
		if(contentVersionId == null)
			logger.warn("Null was sent in...");
		else
			this.usedPageComponentsMetaInfoContentVersionIdSet.add(contentVersionId);
	}

	public Set getUsedPageComponentsMetaInfoContentVersionIdSet() 
	{
		return usedPageComponentsMetaInfoContentVersionIdSet;
	}

	public Map getPageAttributes() 
	{
		return pageAttributes;
	}

	public boolean getUseDownloadAction()
	{
		return useDownloadAction;
	}

	public void setUseDownloadAction(boolean useDownloadAction)
	{
		this.useDownloadAction = useDownloadAction;
	}

	public Set<String> getHtmlHeadItems()
	{
		return htmlHeadItems;
	}

	public Set<String> getHtmlBodyEndItems()
	{
		return htmlBodyEndItems;
	}

	public Map getHttpHeaders()
	{
		return httpHeaders;
	}

	public Map<String,Set<String>> getScriptExtensionHeadBundles()
	{
		return scriptExtensionHeadBundles;
	}

	public Map<String,Set<String>> getScriptExtensionBodyBundles()
	{
		return scriptExtensionBodyBundles;
	}

	public Map<String,Set<String>> getCSSExtensionBundles()
	{
		return cssExtensionBundles;
	}

	/*
	 * Adds a file to the named bundle
	 */
	public void addScriptExtensionHeadBundleFile(String bundleName, String filePath)
	{
		Set<String> files = scriptExtensionHeadBundles.get(bundleName);
		if(files == null)
		{
			files = new HashSet<String>();
			scriptExtensionHeadBundles.put(bundleName, files);
		}
		files.add(filePath);
	}

	/*
	 * Adds a file to the named bundle
	 */
	public void addScriptExtensionBodyBundleFile(String bundleName, String filePath)
	{
		Set<String> files = scriptExtensionBodyBundles.get(bundleName);
		if(files == null)
		{
			files = new HashSet<String>();
			scriptExtensionBodyBundles.put(bundleName, files);
		}
		files.add(filePath);
	}

	/*
	 * Adds a file to the named bundle
	 */
	public void addCSSExtensionBundleFile(String bundleName, String filePath)
	{
		Set<String> files = cssExtensionBundles.get(bundleName);
		if(files == null)
		{
			files = new HashSet<String>();
			cssExtensionBundles.put(bundleName, files);
		}
		files.add(filePath);
	}

	public Object getExtraData()
	{
		Map extraData = new HashMap();
		
		Map newHeaders = new HashMap(); 
		newHeaders.putAll(getHttpHeaders());
		
		extraData.put("contentType", this.getContentType());
		extraData.put("headers", newHeaders);
		//extraData.put("pageAttributes", this.getPageAttributes());
		extraData.put("lastModifiedDateTime", this.lastModifiedDateTime);
		extraData.put("pageCacheTimeout", this.pageCacheTimeout);
		
		return extraData;
	}

	public void populateExtraData(Map extraData)
	{
		String contentType = (String)extraData.get("contentType");
		if(contentType != null)
			this.setContentType(contentType);
		
		Map headers = (Map)extraData.get("headers");
		if(headers != null)
			this.getHttpHeaders().putAll(headers);
		
		/*
		Map pageAttributes = (Map)extraData.get("pageAttributes");
		if(pageAttributes != null)
			this.getPageAttributes().putAll(pageAttributes);
		*/
		
		Date lastModifiedDateTime = (Date)extraData.get("lastModifiedDateTime");
		if(lastModifiedDateTime != null && (this.lastModifiedDateTime == null || this.lastModifiedDateTime.before(lastModifiedDateTime)))
		{
			this.lastModifiedDateTime = lastModifiedDateTime;
		}
		if (this.lastModifiedDateTime != null)
		{
			this.getHttpHeaders().put("Last-Modified", HTTP_DATE_FORMAT.format(this.lastModifiedDateTime));
		}
		
		Integer pageCacheTimeout = (Integer)extraData.get("pageCacheTimeout");
		if(pageCacheTimeout != null)
			this.pageCacheTimeout = pageCacheTimeout;
	}

	public Date getLastModifiedDateTime()
	{
		return lastModifiedDateTime;
	}

	public void setLastModifiedDateTime(Date lastModifiedDateTime)
	{
		this.lastModifiedDateTime = lastModifiedDateTime;
		
		getHttpHeaders().put("Last-Modified", HTTP_DATE_FORMAT.format(lastModifiedDateTime));
	}

	public String getOperatingMode()
	{
		if(this.operatingMode == null)
		{
			//logger.info("No local operating mode. Using global:" + CmsPropertyHandler.getOperatingMode());
			this.operatingMode = CmsPropertyHandler.getOperatingMode();
		}
		
		//logger.info("Returning " + this.operatingMode);
		return this.operatingMode;
	}

	public void setOperatingMode(String operatingMode)
	{
		this.operatingMode = operatingMode;
	}

	public Integer getPageCacheTimeout()
	{
		return pageCacheTimeout;
	}

	public void setPageCacheTimeout(Integer pageCacheTimeout)
	{
		this.pageCacheTimeout = pageCacheTimeout;
	}

	public Set getUsedContentVersions()
	{
		return Collections.unmodifiableSet(usedContentVersions);
	}

	public Set getUsedContents()
	{
		return Collections.unmodifiableSet(usedContents);
	}

	/**
	 * This method returns the exact full url excluding query string from the original request - not modified
	 * @return
	 */
	
	public String getOriginalURL()
	{
    	String originalRequestURL = this.getHttpServletRequest().getParameter("originalRequestURL");
    	if(originalRequestURL == null || originalRequestURL.length() == 0)
    		originalRequestURL = this.getHttpServletRequest().getRequestURL().toString();

    	return originalRequestURL;
	}

	/**
	 * This method returns the exact querystring from the original request - not modified
	 * @return
	 */
	
	public String getOriginalQueryString()
	{
    	String originalQueryString = this.getHttpServletRequest().getParameter("originalQueryString");
    	if(originalQueryString == null || originalQueryString.length() == 0)
    		originalQueryString = this.getHttpServletRequest().getQueryString();

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
	 * This method returns the debug information
	 */
	
	public String getDebugInformation()
	{
    	return this.debugInformation;
	}

	/**
	 * This method adds debug information for eventual later use
	 */
	
	public void addDebugInformation(String debug)
	{
		if(this.debugMode)
			this.debugInformation = this.debugInformation + "\n" + debug;
	}

	/**
	 * This method adds debug information for eventual later use
	 */
	
	public void setDebugMode(boolean debugMode)
	{
    	this.debugMode = debugMode;
	}

	/**
	 * This method adds debug information for eventual later use
	 */
	
	public void resetDebugMode()
	{
		this.debugInformation = "";
	}

	/**
	 * @return the cachedResponse
	 */
	public boolean getIsCachedResponse() 
	{
		return cachedResponse;
	}

	/**
	 * @param cachedResponse the cachedResponse to set
	 */
	public void setIsCachedResponse(boolean cachedResponse) 
	{
		this.cachedResponse = cachedResponse;
	}

}
