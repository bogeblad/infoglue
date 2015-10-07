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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.util.CacheController;

import webwork.action.ActionContext;

/**
 * Created by IntelliJ IDEA.
 * User: lbj
 * Date: 22-01-2004
 * Time: 16:41:17
 * To change this template use Options | File Templates.
 */
public class BasicURLComposer extends URLComposer
{
    private final static Logger logger = Logger.getLogger(BasicURLComposer.class.getName());

    public BasicURLComposer()
    {
    }
    
    public String composeDigitalAssetUrl(String dnsName, Integer siteNodeId, Integer contentId, Integer languageId, String assetKey, DeliveryContext deliveryContext, Database db) throws Exception
    {
    	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
    	String deriveProtocolWhenUsingProtocolRedirects = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(contentVO.getRepositoryId(), "deriveProtocolWhenUsingProtocolRedirects");
		if(deriveProtocolWhenUsingProtocolRedirects == null || deriveProtocolWhenUsingProtocolRedirects.equals("") || !deriveProtocolWhenUsingProtocolRedirects.equals("true") || !deriveProtocolWhenUsingProtocolRedirects.equals("false"))
			deriveProtocolWhenUsingProtocolRedirects = CmsPropertyHandler.getDeriveProtocolWhenUsingProtocolRedirects();
    	
		String protectedProtocolName = CmsPropertyHandler.getProtectedProtocolName();
		String protectedProtocolPort = CmsPropertyHandler.getProtectedProtocolPort();
		String unprotectedProtocolPort = CmsPropertyHandler.getUnprotectedProtocolPort();

        String disableEmptyUrls = CmsPropertyHandler.getDisableEmptyUrls();
        if(siteNodeId == null || contentId == null || languageId == null || assetKey == null)
            return "";
            
        String assetUrl = "";
        
        String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
        if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
        	enableNiceURI = "false";

        String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
        if(useDNSNameInUrls == null || useDNSNameInUrls.equalsIgnoreCase(""))
            useDNSNameInUrls = "false";

        if(deriveProtocolWhenUsingProtocolRedirects.equalsIgnoreCase("true"))
        {
        	StringBuilder sb = new StringBuilder(256);
	        
	        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
            int indexOfProtocol = originalUrl.indexOf("://");
            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
            String base = protectedProtocolName + originalUrl.substring(indexOfProtocol, indexFirstSlash);
            if(protectedProtocolPort.length() > 0)
            	base = base.replaceFirst(unprotectedProtocolPort, protectedProtocolPort);
            
            sb.append(base);
	        
	        String servletContext = CmsPropertyHandler.getServletContext();
	        
	        sb.append(servletContext);
	        
	        if(!sb.toString().endsWith("/"))
	        	sb.append("/");
	        
	        sb.append("DownloadProtectedAsset.action?siteNodeId=" + siteNodeId + "&contentId=" + contentId + "&languageId=" + languageId + "&assetKey=" + assetKey);
	        
	        assetUrl = sb.toString();    
        }
        else if(enableNiceURI.equalsIgnoreCase("true") || useDNSNameInUrls.equalsIgnoreCase("false"))
        {
        	StringBuilder sb = new StringBuilder(256);
	        
	        if(deliveryContext.getUseFullUrl())
	        {
		        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
	            int indexOfProtocol = originalUrl.indexOf("://");
	            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
	            String base = originalUrl.substring(0, indexFirstSlash);
	            sb.append(base);
	        }
	        
	        String servletContext = CmsPropertyHandler.getServletContext();
	        
        	sb.append(servletContext);
	        
	        if(!sb.toString().endsWith("/"))
	        	sb.append("/");
	        
	        sb.append("DownloadProtectedAsset.action?siteNodeId=" + siteNodeId + "&contentId=" + contentId + "&languageId=" + languageId + "&assetKey=" + assetKey);
	        
	        assetUrl = sb.toString();
        }
        else
        {
        	String operatingMode = CmsPropertyHandler.getOperatingMode();
		    String keyword = "";
		    if(operatingMode.equalsIgnoreCase("0"))
		        keyword = "working=";
		    else if(operatingMode.equalsIgnoreCase("2"))
		        keyword = "preview=";
		    if(operatingMode.equalsIgnoreCase("3"))
		        keyword = "live=";
		    
		    if(dnsName != null)
		    {
    		    int startIndex = dnsName.indexOf(keyword);
    		    if(startIndex != -1)
    		    {
    		        int endIndex = dnsName.indexOf(",", startIndex);
        		    if(endIndex > -1)
    		            dnsName = dnsName.substring(startIndex, endIndex);
    		        else
    		            dnsName = dnsName.substring(startIndex);
    		        
    		        dnsName = dnsName.split("=")[1];
    		    }
    		    else
    		    {
    		        int endIndex = dnsName.indexOf(",");
    		        if(endIndex > -1)
    		            dnsName = dnsName.substring(0, endIndex);
    		        else
    		            dnsName = dnsName.substring(0);
    		        
    		    }
		    }

            String context = CmsPropertyHandler.getServletContext();

            assetUrl = dnsName + context + "/DownloadProtectedAsset.action?siteNodeId=" + siteNodeId + "&contentId=" + contentId + "&languageId=" + languageId + "&assetKey=" + assetKey;
        }
        
        return assetUrl;
    }

    public String composeDigitalAssetUrl(String dnsName, String filename, DeliveryContext deliveryContext)
    {
    	String folderName = null;
    	if(filename != null && filename.indexOf("_") > -1)
    	{
    		String assetId = filename.substring(0, filename.indexOf("_"));
    		try 
    		{
				folderName = "" + (new Integer(assetId).intValue() / 1000);
    		} 
    		catch (Exception e) 
    		{
		    	logger.error("A problem parsing assetId[" + assetId + "]:" + e.getMessage());
		    	folderName = "0";
		    }
    	}
    	
    	return composeDigitalAssetUrl(dnsName, folderName, filename, deliveryContext);
    }
    
    public String composeDigitalAssetUrl(String dnsName, String folderName, String filename, DeliveryContext deliveryContext)
    {
    	if(logger.isInfoEnabled())
    		logger.info("folderName:" + folderName);
    	
        String disableEmptyUrls = CmsPropertyHandler.getDisableEmptyUrls();
        if((filename == null || filename.equals("")) && (disableEmptyUrls == null || disableEmptyUrls.equalsIgnoreCase("yes")))
            return "";
            
        String assetUrl = "";
        
        String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
        if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
        	enableNiceURI = "false";

        String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
        if(useDNSNameInUrls == null || useDNSNameInUrls.equalsIgnoreCase(""))
            useDNSNameInUrls = "false";

        if(enableNiceURI.equalsIgnoreCase("true") || useDNSNameInUrls.equalsIgnoreCase("false"))
        {
        	StringBuilder sb = new StringBuilder(256);
	        
	        if(deliveryContext.getUseFullUrl())
	        {
		        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
	            int indexOfProtocol = originalUrl.indexOf("://");
	            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
	            String base = originalUrl.substring(0, indexFirstSlash);
	            sb.append(base);
	        }
	        
	        String servletContext = CmsPropertyHandler.getServletContext();
	        String digitalAssetPath = CmsPropertyHandler.getDigitalAssetBaseUrl();
	        if (!digitalAssetPath.startsWith("/"))
	        	digitalAssetPath = "/" + digitalAssetPath;
	        
	        //logger.info("servletContext:" + servletContext);
	        //logger.info("digitalAssetPath:" + digitalAssetPath);
	
	        if(digitalAssetPath.indexOf(servletContext) == -1)
	        	sb.append(servletContext);
	        
	        sb.append(digitalAssetPath);
	     
	        if(!sb.toString().endsWith("/"))
	        	sb.append("/");
	        
	        if(folderName != null)
	        {
		        sb.append(folderName);
			     
		        if(!sb.toString().endsWith("/"))
		        	sb.append("/");
	        }
	        
	        sb.append(filename);
	        
	        //logger.info("sb:" + sb);
	        
	        assetUrl = sb.toString();
        }
        else
        {
        	String operatingMode = CmsPropertyHandler.getOperatingMode();
		    String keyword = "";
		    if(operatingMode.equalsIgnoreCase("0"))
		        keyword = "working=";
		    else if(operatingMode.equalsIgnoreCase("2"))
		        keyword = "preview=";
		    if(operatingMode.equalsIgnoreCase("3"))
		        keyword = "live=";
		    
		    if(dnsName != null)
		    {
    		    int startIndex = dnsName.indexOf(keyword);
    		    if(startIndex != -1)
    		    {
    		        int endIndex = dnsName.indexOf(",", startIndex);
        		    if(endIndex > -1)
    		            dnsName = dnsName.substring(startIndex, endIndex);
    		        else
    		            dnsName = dnsName.substring(startIndex);
    		        
    		        dnsName = dnsName.split("=")[1];
    		    }
    		    else
    		    {
    		        int endIndex = dnsName.indexOf(",");
    		        if(endIndex > -1)
    		            dnsName = dnsName.substring(0, endIndex);
    		        else
    		            dnsName = dnsName.substring(0);
    		        
    		    }
		    }

            String context = CmsPropertyHandler.getServletContext();

            if(folderName != null)
            	assetUrl = dnsName + context + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + filename;
            else
            	assetUrl = dnsName + context + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + filename;	
        }
        
        return assetUrl;
    }

	private String getHost(String dnsName, String operatingMode)
	{
		String host = null;
		String keyword = "";
		if(operatingMode.equalsIgnoreCase("0"))
		{
			keyword = "working=";
		}
		else if(operatingMode.equalsIgnoreCase("2"))
		{
			keyword = "preview=";
		}
		if(operatingMode.equalsIgnoreCase("3"))
		{
			keyword = "live=";
		}
		if(dnsName != null)
		{
			int startIndex = dnsName.indexOf(keyword);
			if(startIndex != -1)
			{
				int endIndex = dnsName.indexOf(",", startIndex);
				if(endIndex > -1)
				{
					host = dnsName.substring(startIndex, endIndex);
				}
				else
				{
					host = dnsName.substring(startIndex);
				}

				host = host.split("=")[1];
			}
			else
			{
				int endIndex = dnsName.indexOf(",");
				if(endIndex > -1)
				{
					host = dnsName.substring(0, endIndex);
				}
				else
				{
					host = dnsName.substring(0);
				}
			}
		}
		return host;
	}
				  
	
	public String composePageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		return composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, true, contentId, CmsPropertyHandler.getServletContext(), deliveryContext);
	}

	/**
	 * If the <em>infoGluePrincipal</em> argument is null the anonymous principal will be used.
	 */
	public String composePageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, String applicationContext, DeliveryContext deliveryContext) throws SystemException, Exception {
		return composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, true, contentId, applicationContext, deliveryContext);
	}
	public String composePageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean includeLanguageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception {
		return 	 composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, includeLanguageId, contentId, CmsPropertyHandler.getServletContext(), deliveryContext);
	}
    public String composePageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean includeLanguageId, Integer contentId, String applicationContext, DeliveryContext deliveryContext) throws SystemException, Exception
    {
    	String url = null;

		if (infoGluePrincipal == null)
		{
			logger.info("No principal was provided for composePageUrl. Will use the anonymous user.");
			infoGluePrincipal = (InfoGluePrincipal)getAnonymousPrincipal();
		}

    	if(siteNodeId == null || siteNodeId.intValue() == -1)
    	{
    		logger.warn("composePageUrl was called with siteNodeId:" + siteNodeId + " from the page with key: " + deliveryContext.getPageKey() + " (siteNodeId=" + deliveryContext.getSiteNodeId() + ")");
    		return "";
    	}
    	
    	if(contentId == null || contentId == 0)
    		contentId = -1;
    	
        /*
        String disableEmptyUrls = CmsPropertyHandler.getDisableEmptyUrls();
        if(filename == null || filename.equals("") && disableEmptyUrls == null || disableEmptyUrls.equalsIgnoreCase("no"))
            return "";
        */
        
    	String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
        if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
        	enableNiceURI = "false";
        
        String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
        if(useDNSNameInUrls == null || useDNSNameInUrls.equalsIgnoreCase(""))
            useDNSNameInUrls = "false";

        boolean makeAccessBasedProtocolAdjustments = false;
        boolean makeAccessBasedProtocolAdjustmentsIntoProtected = false;

		HttpServletRequest request = deliveryContext.getHttpServletRequest();
		String schema = request == null ? null : request.getScheme();
		if(logger.isInfoEnabled())
		{
			logger.info("Scheme:" + schema);
		}
		String operatingMode = CmsPropertyHandler.getOperatingMode();
		if (deliveryContext.getOperatingMode() != null)
		{
			operatingMode = deliveryContext.getOperatingMode();
		}
		if(logger.isInfoEnabled())
		{
			logger.info("operatingMode: " + operatingMode);
		}

		String unprotectedProtocolName = CmsPropertyHandler.getUnprotectedProtocolName();
		String unprotectedProtocolPort = CmsPropertyHandler.getUnprotectedProtocolPort();
		String protectedProtocolName = CmsPropertyHandler.getProtectedProtocolName();
		String protectedProtocolPort = CmsPropertyHandler.getProtectedProtocolPort();
		if(logger.isInfoEnabled())
		{
			logger.info("unprotectedProtocolName:" + unprotectedProtocolName);
			logger.info("protectedProtocolName:" + protectedProtocolName);
			logger.info("unprotectedProtocolPort:" + unprotectedProtocolPort);
			logger.info("protectedProtocolPort:" + protectedProtocolPort);
		}

		try
		{
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
			if(siteNodeVO == null)
			{
				logger.warn("composePageUrl was called with siteNodeId which does not exist:" + siteNodeId + " from the page with key: " + deliveryContext.getPageKey());
				return "";
			}

			String deriveProtocolWhenUsingProtocolRedirects = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(siteNodeVO.getRepositoryId(), "deriveProtocolWhenUsingProtocolRedirects");
			if(deriveProtocolWhenUsingProtocolRedirects == null || deriveProtocolWhenUsingProtocolRedirects.equals("") || !deriveProtocolWhenUsingProtocolRedirects.equals("true") || !deriveProtocolWhenUsingProtocolRedirects.equals("false"))
				deriveProtocolWhenUsingProtocolRedirects = CmsPropertyHandler.getDeriveProtocolWhenUsingProtocolRedirects();

			boolean schemaShouldChange = schema != null && !schema.equalsIgnoreCase("https");
			if(deriveProtocolWhenUsingProtocolRedirects.equalsIgnoreCase("true") && operatingMode.equals("3") && schemaShouldChange)
			{
				NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
				Integer protectedSiteNodeVersionId = nodeDeliveryController.getProtectedSiteNodeVersionId(db, siteNodeId, "SiteNodeVersion.Read");
				String originalFullURL = deliveryContext.getOriginalFullURL();

				boolean isAnonymousAccepted = true;
				if(protectedSiteNodeVersionId != null)
				{
					Principal anonymousPrincipal = getAnonymousPrincipal();
					isAnonymousAccepted = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)anonymousPrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
					//logger.info("anonymousPrincipal has access:" + isAnonymousAccepted);
				}

				if(protectedSiteNodeVersionId != null && !isAnonymousAccepted)
				{
					//logger.info("anonymousPrincipal has no access - switching to secure line");
					if(originalFullURL.indexOf(unprotectedProtocolName + "://") > -1)
					{
						useDNSNameInUrls = "true";
						//deliveryContext.setUseFullUrl(true);
						makeAccessBasedProtocolAdjustments = true;	
						makeAccessBasedProtocolAdjustmentsIntoProtected = true;
					}
				}
				else
				{
					if(originalFullURL.indexOf(protectedProtocolName + "://") > -1)
					{
						useDNSNameInUrls = "true";
						//deliveryContext.setUseFullUrl(true);
						makeAccessBasedProtocolAdjustments = true;	
						makeAccessBasedProtocolAdjustmentsIntoProtected = false;
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Error checking up if we should switch protocol:" + e.getMessage(), e);
		}

		boolean isDecoratedUrl = request == null ? false : request.getRequestURI().indexOf("!renderDecoratedPage") > -1;
		logger.debug("URL is decorated: " + isDecoratedUrl);
		if (enableNiceURI.equalsIgnoreCase("true") && !isDecoratedUrl && !deliveryContext.getDisableNiceUri())
		{
			SiteNodeVO siteNode = SiteNodeController.getController().getSmallSiteNodeVOWithId(siteNodeId, db);
			if(siteNode == null)
			{
				logger.warn("composePageUrl was called with siteNodeId which does not exist:" + siteNodeId + " from the page with key: " + deliveryContext.getPageKey());
				return "";
			}
			String enableNiceURIForLanguage = CmsPropertyHandler.getEnableNiceURIForLanguage();
			/*
		    //logger.info("enableNiceURIForLanguage:" + enableNiceURIForLanguage);
		    if(enableNiceURIForLanguage == null || !enableNiceURIForLanguage.equals("true"))
		    {
		        String enableNiceURIForLanguageForRepo = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(siteNode.getRepositoryId(), "enableNiceURIForLanguage");
				if(enableNiceURIForLanguageForRepo != null && enableNiceURIForLanguageForRepo.equals("true"))
					enableNiceURIForLanguage = enableNiceURIForLanguageForRepo;
		    }

		    if(enableNiceURIForLanguage.equalsIgnoreCase("true"))
        		context = context + "/" + LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(db, languageId).getLanguageCode();
			*/
			SiteNodeVO currentSiteNode = SiteNodeController.getController().getSmallSiteNodeVOWithId(deliveryContext.getSiteNodeId(), db);

			if(!siteNode.getRepositoryId().equals(currentSiteNode.getRepositoryId()))
			{
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNode.getRepositoryId(), db);
				String dnsName = repositoryVO.getDnsName();
				logger.info("dnsName:" + dnsName + " for siteNode " + siteNode.getName());

				//    		    String operatingMode = CmsPropertyHandler.getOperatingMode();
				String keyword = "";
				if (operatingMode.equalsIgnoreCase("0"))
				{
					keyword = "working=";
				}
				else if (operatingMode.equalsIgnoreCase("2"))
				{
					keyword = "preview=";
				}
				if (operatingMode.equalsIgnoreCase("3"))
				{
					keyword = "live=";
				}

				String repositoryPath = null;
				if (!operatingMode.equals("3"))
				{
					int workingPathStartIndex = dnsName.indexOf("workingPath=");
					if(workingPathStartIndex != -1)
					{
						int workingPathEndIndex = dnsName.indexOf(",", workingPathStartIndex);
						if(workingPathEndIndex > -1)
							repositoryPath = dnsName.substring(workingPathStartIndex + 12, workingPathEndIndex);
						else
							repositoryPath = dnsName.substring(workingPathStartIndex + 12);
					}
				}

				if(repositoryPath == null)
				{
					int pathStartIndex = dnsName.indexOf("path=");
					if(pathStartIndex != -1)
					{
						int pathEndIndex = dnsName.indexOf(",", pathStartIndex);
						if(pathEndIndex > -1)
							repositoryPath = dnsName.substring(pathStartIndex + 5, pathEndIndex);
						else
							repositoryPath = dnsName.substring(pathStartIndex + 5);
					}
				}

				if(logger.isInfoEnabled())
				{
					logger.info("repositoryPath in constructing new url:" + repositoryPath);
					logger.info("dnsName:" + dnsName);
					logger.info("repositoryPath:" + repositoryPath);
				}

				if(dnsName != null)
				{
					int startIndex = dnsName.indexOf(keyword);
					if(startIndex != -1)
					{
						int endIndex = dnsName.indexOf(",", startIndex);
						if(endIndex > -1)
							dnsName = dnsName.substring(startIndex, endIndex);
						else
							dnsName = dnsName.substring(startIndex);

						dnsName = dnsName.split("=")[1];
					}
					else
					{
						int endIndex = dnsName.indexOf(",");
						if(endIndex > -1)
							dnsName = dnsName.substring(0, endIndex);
						else
							dnsName = dnsName.substring(0);
					}
				}

				if(logger.isInfoEnabled())
				{
					logger.info("A4:" + dnsName);
				}
				if(schema != null && schema.equalsIgnoreCase("https"))
				{
					dnsName = dnsName.replaceFirst(unprotectedProtocolName + "://", protectedProtocolName + "://").replaceFirst(unprotectedProtocolPort, protectedProtocolPort);
				}
				if(logger.isInfoEnabled())
				{
					logger.info("A42:" + dnsName);
				}

				if(repositoryPath != null)
				{
					if(applicationContext.startsWith("/"))
						applicationContext = dnsName + applicationContext + "/" + repositoryPath;
					else
						applicationContext = dnsName + "/" + (applicationContext.equals("") ? "" : applicationContext + "/") + repositoryPath;
				}
				else
				{
					if(applicationContext.startsWith("/"))
						applicationContext = dnsName + applicationContext;
					else
						applicationContext = dnsName + "/" + applicationContext;
				}

				if(logger.isInfoEnabled())
				{
					logger.info("A5:" + applicationContext);
				}

			}
			else
			{
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNode.getRepositoryId(), db);
				String dnsName = repositoryVO.getDnsName();

				String repositoryPath = null;
				if (!operatingMode.equals("3"))
    	    	{
    		    	int workingPathStartIndex = dnsName.indexOf("workingPath=");
    		    	if(workingPathStartIndex != -1)
    		    	{
    		    		int workingPathEndIndex = dnsName.indexOf(",", workingPathStartIndex);
    		    		if(workingPathEndIndex > -1)
        		    		repositoryPath = dnsName.substring(workingPathStartIndex + 12, workingPathEndIndex);
    		    		else
    		    			repositoryPath = dnsName.substring(workingPathStartIndex + 12);
    		    	}
    	    	}

    	    	if(repositoryPath == null)
    	    	{
        	    	int pathStartIndex = dnsName.indexOf("path=");
        	    	if(pathStartIndex != -1)
        	    	{
    		    		int pathEndIndex = dnsName.indexOf(",", pathStartIndex);
    		    		if(pathEndIndex > -1)
        		    		repositoryPath = dnsName.substring(pathStartIndex + 5, pathEndIndex);
    		    		else
    		    			repositoryPath = dnsName.substring(pathStartIndex + 5);
        	    	}
    	    	}

				logger.info("repositoryPath in constructing new url:" + repositoryPath);

				if(repositoryPath != null)
				{
					applicationContext = applicationContext + "/" + repositoryPath;
				}
			}

			StringBuilder sb = new StringBuilder(256);

			if ((deliveryContext.getUseFullUrl() || makeAccessBasedProtocolAdjustments) && applicationContext.indexOf("://") == -1)
			{
				String originalUrl;
				if (request == null)
				{
					logger.info("Could not derive full base URL since we got no request. Getting value from DNS names instead.");
					RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNode.getRepositoryId(), db);
					originalUrl = getHost(repositoryVO.getDnsName(), operatingMode);
				}
				else
				{
					originalUrl = request.getRequestURL().toString();
				}
				if (originalUrl != null)
				{
					String base = originalUrl;
					int indexOfProtocol = base.indexOf("://");
					if (indexOfProtocol > -1)
					{
						indexOfProtocol += 3;
					}
					else
					{
						indexOfProtocol = 0;
					}
					int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol);
					if (indexFirstSlash > -1)
					{
						base = originalUrl.substring(0, indexFirstSlash);
					}
					sb.append(base);
				}
			}

			sb.append(applicationContext);

			try
			{
				String navigationPath = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getPageNavigationPath(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext);
				if(navigationPath != null && navigationPath.startsWith("/") && sb.toString().endsWith("/"))
					sb.append(navigationPath.substring(1));
				else
					sb.append(navigationPath);

				if(sb.toString().endsWith(applicationContext) && !sb.toString().endsWith("/"))
				{
					sb.append("/");
				}

				boolean addedContent = false;
	            /*
	            if(contentId != null && contentId.intValue() != -1)
	            {
	                sb.append("?contentId=").append(String.valueOf(contentId));
	                addedContent = true;
	            }
	            */
	            
	            if(contentId != null && contentId.intValue() != -1)
	            {
	            	if(!CmsPropertyHandler.getNiceURIDisableNiceURIForContent().equals("true"))
	            	{
		            	String navigationTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, contentId, languageId, "NavigationTitle", siteNodeId, true, deliveryContext, infoGluePrincipal, false, false);
		            	if(navigationTitle == null || navigationTitle.equals(""))
		            		navigationTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, contentId, languageId, "Title", siteNodeId, true, deliveryContext, infoGluePrincipal, false, false);
		            	if(navigationTitle != null && !navigationTitle.equals(""))
		            	{
			            	navigationTitle = new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(navigationTitle, CmsPropertyHandler.getNiceURIDefaultReplacementCharacterForContent());
			            	sb.append("/" + navigationTitle + ".cid").append(String.valueOf(contentId));
			                addedContent = true;
						}
		            	else
		            	{
		    	            if(contentId != null && contentId.intValue() != -1)
		    	            {
		    	                sb.append("?contentId=").append(String.valueOf(contentId));
		    	                addedContent = true;
							}
						}
	            	}
	            	else
	            	{
	    	            if(contentId != null && contentId.intValue() != -1)
	    	            {
	    	                sb.append("?contentId=").append(String.valueOf(contentId));
	    	                addedContent = true;
						}
					}
	            }

	            if(!enableNiceURIForLanguage.equalsIgnoreCase("true") && includeLanguageId)
	            {
		            if (languageId != null && languageId.intValue() != -1 && deliveryContext.getLanguageId().intValue() != languageId.intValue())
		            {
		                if(addedContent)
		                    sb.append(getRequestArgumentDelimiter());
		                else
		                    sb.append("?");
		                    
		                sb.append("languageId=").append(String.valueOf(languageId));
		            }
	            }
	            
	            url = (!sb.toString().equals("") ? sb.toString() : "/");
	        } 
	        catch (Exception e) 
			{
	            logger.warn("Error generating url:" + e.getMessage());
	        }
        }
        else
        {           
            if(!useDNSNameInUrls.equalsIgnoreCase("false"))
            {
	    		if(siteNodeId == null)
	    			siteNodeId = new Integer(-1);
	
	    		if(languageId == null)
	    			languageId = new Integer(-1);
	
	    		if(contentId == null)
	    			contentId = new Integer(-1);
	
	            String arguments = "siteNodeId=" + siteNodeId + getRequestArgumentDelimiter() + "languageId=" + languageId + getRequestArgumentDelimiter() + "contentId=" + contentId;

	            //SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db, true);
	            SiteNodeVO siteNode = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
	    		String dnsName = CmsPropertyHandler.getWebServerAddress();
	    		if(siteNode != null)
	    		{
	    			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNode.getRepositoryId(), db);
		    		if(siteNode != null && repositoryVO.getDnsName() != null && !repositoryVO.getDnsName().equals(""))
		    			dnsName = repositoryVO.getDnsName();
	    		}

//	        	String operatingMode = CmsPropertyHandler.getOperatingMode();
				String keyword = "";
				if(operatingMode.equalsIgnoreCase("0"))
				{
					keyword = "working=";
				}
				else if(operatingMode.equalsIgnoreCase("2"))
				{
					keyword = "preview=";
				}
				if(operatingMode.equalsIgnoreCase("3"))
				{
					keyword = "live=";
				}
			    
			    if(dnsName != null)
			    {
	    		    int startIndex = dnsName.indexOf(keyword);
	    		    if(startIndex != -1)
	    		    {
	    		        int endIndex = dnsName.indexOf(",", startIndex);
	        		    if(endIndex > -1)
	    		            dnsName = dnsName.substring(startIndex, endIndex);
	    		        else
	    		            dnsName = dnsName.substring(startIndex);
	    		        
	    		        dnsName = dnsName.split("=")[1];
	    		    }
	    		    else
	    		    {
	    		        int endIndex = dnsName.indexOf(",");
	    		        if(endIndex > -1)
	    		            dnsName = dnsName.substring(0, endIndex);
	    		        else
	    		            dnsName = dnsName.substring(0);
	    		        
	    		    }
				}

				String context = CmsPropertyHandler.getServletContext();

				url = dnsName + context + "/" + CmsPropertyHandler.getApplicationBaseAction() + "?" + arguments;

				if (isDecoratedUrl)
				{
					String componentRendererUrl = CmsPropertyHandler.getComponentRendererUrl();
					if(componentRendererUrl.endsWith("/"))
					{
						componentRendererUrl += "/";
					}

					url = componentRendererUrl + CmsPropertyHandler.getComponentRendererAction() + "?" + arguments;
				}

				if(request != null && request.getScheme().equalsIgnoreCase("https"))
				{
					url = url.replaceFirst(unprotectedProtocolName + "://", protectedProtocolName + "://").replaceFirst(unprotectedProtocolPort, protectedProtocolPort);
				}
			}
			else
			{
				StringBuilder sb = new StringBuilder(256);
				if(deliveryContext.getUseFullUrl() || makeAccessBasedProtocolAdjustments)
				{
					if (request == null)
					{
						logger.warn("Cannot derive full base URL since there is no request.");
					}
					else
					{
						String originalUrl = request.getRequestURL().toString();
						int indexOfProtocol = originalUrl.indexOf("://");
						int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
						String base = originalUrl.substring(0, indexFirstSlash);
						sb.append(base);
					}
				}

                String servletContext = CmsPropertyHandler.getServletContext();

                if(siteNodeId == null)
	    			siteNodeId = new Integer(-1);

	    		if(languageId == null)
	    			languageId = new Integer(-1);

	    		if(contentId == null)
	    			contentId = new Integer(-1);

	            String arguments = "siteNodeId=" + siteNodeId + getRequestArgumentDelimiter() + "languageId=" + languageId + getRequestArgumentDelimiter() + "contentId=" + contentId;

				if (isDecoratedUrl)
				{
					sb.append(servletContext + "/" + CmsPropertyHandler.getComponentRendererAction() + "?" + arguments);
				}
				else
				{
					sb.append(servletContext + "/" + CmsPropertyHandler.getApplicationBaseAction() + "?" + arguments);
				}

				url = sb.toString();
            }
        }
        
        if(logger.isInfoEnabled())
    	{
    		logger.info("url:" + url);
    		logger.info("makeAccessBasedProtocolAdjustments:" + makeAccessBasedProtocolAdjustments);
        }
        
        if(makeAccessBasedProtocolAdjustments)
        {
        	if(logger.isInfoEnabled())
        	{
        		logger.info("unprotectedProtocolName:" + unprotectedProtocolName);
        		logger.info("protectedProtocolName:" + protectedProtocolName);
        		logger.info("unprotectedProtocolPort:" + unprotectedProtocolPort);
        		logger.info("protectedProtocolPort:" + protectedProtocolPort);
			}
			if(makeAccessBasedProtocolAdjustmentsIntoProtected)
				url = url.replaceFirst(unprotectedProtocolName + "://", protectedProtocolName + "://").replaceFirst(unprotectedProtocolPort, protectedProtocolPort);
			else
				url = url.replaceFirst(protectedProtocolName + "://", unprotectedProtocolName + "://").replaceFirst(protectedProtocolPort, unprotectedProtocolPort);
			logger.info("Adjusted url:" + url);
        }
        
        return url;
    }

    public String composePageUrlForRedirectRegistry(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext, Boolean useNiceURI, Boolean enableNiceURIForLanguage) throws SystemException, Exception
    {
    	if(siteNodeId == null || siteNodeId.intValue() == -1)
    	{
    		logger.warn("composePageUrl was called with siteNodeId:" + siteNodeId);
    		return "";
    	}
    	
    	if(contentId == null || contentId == 0)
    		contentId = -1;
    	
    	StringBuffer sb = new StringBuffer();
    	if(useNiceURI)
    	{
	        try 
			{
	        	String navigationPath = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getPageNavigationPath(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext);
	            if(navigationPath != null && navigationPath.startsWith("/") && sb.toString().endsWith("/"))
	            	sb.append(navigationPath.substring(1));
	            else
	            	sb.append(navigationPath);

            	if(enableNiceURIForLanguage)
            		sb.insert(0, "/" + LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(db, languageId).getLanguageCode());
	        } 
	        catch (Exception e) 
			{
	        	e.printStackTrace();
	            logger.warn("Error generating url:" + e.getMessage());
	        }
        }
        else
        {           
        	if(siteNodeId == null)
    			siteNodeId = new Integer(-1);

    		if(languageId == null)
    			languageId = new Integer(-1);

    		if(contentId == null)
    			contentId = new Integer(-1);

            String arguments = "siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId;

            sb.append("ViewPage.action?" + arguments);
        }
        
        return sb.toString();
    }
    
    public String composePageUrlAfterLanguageChange(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
    	return composePageUrlAfterLanguageChange(db, infoGluePrincipal, siteNodeId, languageId, true, contentId, deliveryContext);
    }
    
    public String composePageUrlAfterLanguageChange(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Boolean includeLangaugeId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
    
        String pageUrl = composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, includeLangaugeId, contentId, deliveryContext);
    	
        String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
        if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
        	enableNiceURI = "false";
        
	    String enableNiceURIForLanguage = CmsPropertyHandler.getEnableNiceURIForLanguage();
	    if(enableNiceURIForLanguage == null || !enableNiceURIForLanguage.equals("true"))
	    {
            SiteNodeVO siteNode = SiteNodeController.getController().getSmallSiteNodeVOWithId(siteNodeId, db);
        	if(siteNode == null)
        	{
	        	logger.warn("composePageUrl was called with siteNodeId which does not exist:" + siteNodeId + " from the page with key: " + deliveryContext.getPageKey());
	    		return "";
        	}
        	
	        String enableNiceURIForLanguageForRepo = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(siteNode.getRepositoryId(), "enableNiceURIForLanguage");
			if(enableNiceURIForLanguageForRepo != null && enableNiceURIForLanguageForRepo.equals("true"))
				enableNiceURIForLanguage = enableNiceURIForLanguageForRepo;
	    }
	    System.out.println("DOOOORAPAGEURL BEFORE:" + pageUrl);

        if(enableNiceURI.equalsIgnoreCase("true") && !deliveryContext.getDisableNiceUri() && !enableNiceURIForLanguage.equalsIgnoreCase("true") && includeLangaugeId)
        {
            if (pageUrl.indexOf("?") == -1) 
	        {
	            pageUrl += "?languageId=" + String.valueOf(languageId);
	        } 
	        else 
	        {
	        	if(pageUrl.indexOf("languageId=") == -1)
	        		pageUrl += getRequestArgumentDelimiter() + "languageId=" + String.valueOf(languageId);
	        }
        }

        System.out.println("DOOOORAPAGEURL AFTER:" + pageUrl);
        return pageUrl;
    }

    private String getRequestArgumentDelimiter()
    {
        String requestArgumentDelimiter = CmsPropertyHandler.getRequestArgumentDelimiter();
        if(requestArgumentDelimiter == null || requestArgumentDelimiter.equalsIgnoreCase("") || (!requestArgumentDelimiter.equalsIgnoreCase("&") && !requestArgumentDelimiter.equalsIgnoreCase("&amp;")))
            requestArgumentDelimiter = "&";

        return requestArgumentDelimiter;
    }

    public String composePageBaseUrl(String dnsName)
    {
        String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
        if(useDNSNameInUrls == null || useDNSNameInUrls.equalsIgnoreCase(""))
            useDNSNameInUrls = "false";

        if(!useDNSNameInUrls.equalsIgnoreCase("false"))
        {
        	String operatingMode = CmsPropertyHandler.getOperatingMode();
		    String keyword = "";
		    if(operatingMode.equalsIgnoreCase("0"))
		        keyword = "working=";
		    else if(operatingMode.equalsIgnoreCase("2"))
		        keyword = "preview=";
		    if(operatingMode.equalsIgnoreCase("3"))
		        keyword = "live=";
		    
		    if(dnsName != null)
		    {
    		    int startIndex = dnsName.indexOf(keyword);
    		    if(startIndex != -1)
    		    {
    		        int endIndex = dnsName.indexOf(",", startIndex);
        		    if(endIndex > -1)
    		            dnsName = dnsName.substring(startIndex, endIndex);
    		        else
    		            dnsName = dnsName.substring(startIndex);
    		        
    		        dnsName = dnsName.split("=")[1];
    		    }
    		    else
    		    {
    		        int endIndex = dnsName.indexOf(",");
    		        if(endIndex > -1)
    		            dnsName = dnsName.substring(0, endIndex);
    		        else
    		            dnsName = dnsName.substring(0);
    		        
    		    }
		    }

            String context = CmsPropertyHandler.getServletContext();
        	
            return dnsName + context + "/" + CmsPropertyHandler.getApplicationBaseAction();
        }
        
        if(ActionContext.getRequest().getRequestURI().indexOf("!renderDecoratedPage") > -1)
		{
            //String componentRendererUrl = CmsPropertyHandler.getComponentRendererUrl();
		    //if(componentRendererUrl.endsWith("/"))
		    //   componentRendererUrl += "/";
			
			return "/" + CmsPropertyHandler.getComponentRendererUrl();
		}
        
        return "/" + CmsPropertyHandler.getApplicationBaseAction();
    }

	public Principal getAnonymousPrincipal() throws SystemException
	{
	    Principal principal = null;
		try
		{
			principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
			if(principal == null)
			{
			    Map arguments = new HashMap();
			    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());

			    principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments, null);
				
				if(principal != null)
					CacheController.cacheObject("userCache", "anonymous", principal);
			}			
		}
		catch(Exception e) 
		{
		    logger.warn("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
		    throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
		}

		return principal;
	}

} 