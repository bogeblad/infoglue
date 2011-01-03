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

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.filters.FilterConstants;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.invokers.PageInvoker;
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
    
    public String composePageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
    	String url = null;
    	
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
	        SiteNodeVO siteNodeVO = SiteNodeController.getController().getSmallSiteNodeVOWithId(siteNodeId, db);
	
	        String deriveProtocolWhenUsingProtocolRedirects = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(siteNodeVO.getRepositoryId(), "deriveProtocolWhenUsingProtocolRedirects");
			if(deriveProtocolWhenUsingProtocolRedirects == null || deriveProtocolWhenUsingProtocolRedirects.equals("") || !deriveProtocolWhenUsingProtocolRedirects.equals("true") || !deriveProtocolWhenUsingProtocolRedirects.equals("false"))
				deriveProtocolWhenUsingProtocolRedirects = CmsPropertyHandler.getDeriveProtocolWhenUsingProtocolRedirects();
			
			if(logger.isInfoEnabled())
				logger.info("Scheme:" + deliveryContext.getHttpServletRequest().getScheme());
			if(deriveProtocolWhenUsingProtocolRedirects.equalsIgnoreCase("true") && CmsPropertyHandler.getOperatingMode().equals("3") && !deliveryContext.getHttpServletRequest().getScheme().equalsIgnoreCase("https"))
			{
				NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		    	Integer protectedSiteNodeVersionId = nodeDeliveryController.getProtectedSiteNodeVersionId(db, siteNodeId);
		    	String originalFullURL = deliveryContext.getOriginalFullURL();
		    	
		    	boolean isAnonymousAccepted = true;
		    	if(protectedSiteNodeVersionId != null)
		    	{
					Principal anonymousPrincipal = getAnonymousPrincipal();
					isAnonymousAccepted = AccessRightController.getController().getIsPrincipalAuthorized(db, (InfoGluePrincipal)anonymousPrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
					//System.out.println("anonymousPrincipal has access:" + isAnonymousAccepted);
		    	}
		    	
		    	if(protectedSiteNodeVersionId != null && !isAnonymousAccepted)
				{
		    		//System.out.println("anonymousPrincipal has no access - switching to secure line");
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
			logger.warn("Error checking up if we should switch protocol:" + e.getMessage());
		}
        
        if(enableNiceURI.equalsIgnoreCase("true") && deliveryContext.getHttpServletRequest().getRequestURI().indexOf("!renderDecoratedPage") == -1 && !deliveryContext.getDisableNiceUri())
        {
            String context = CmsPropertyHandler.getServletContext();
            
            SiteNodeVO siteNode = SiteNodeController.getController().getSmallSiteNodeVOWithId(siteNodeId, db);
            SiteNodeVO currentSiteNode = SiteNodeController.getController().getSmallSiteNodeVOWithId(deliveryContext.getSiteNodeId(), db);
    		if(!siteNode.getRepositoryId().equals(currentSiteNode.getRepositoryId()))
    		{
    			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNode.getRepositoryId(), db);
                String dnsName = repositoryVO.getDnsName();
    		    logger.info("dnsName:" + dnsName + " for siteNode " + siteNode.getName());
    		    
    		    String operatingMode = CmsPropertyHandler.getOperatingMode();
    		    String keyword = "";
    		    if(operatingMode.equalsIgnoreCase("0"))
    		        keyword = "working=";
    		    else if(operatingMode.equalsIgnoreCase("2"))
    		        keyword = "preview=";
    		    if(operatingMode.equalsIgnoreCase("3"))
    		        keyword = "live=";

    		    String repositoryPath = null;
    	    	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
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
    		    	logger.info("A4:" + dnsName);
			    if(deliveryContext.getHttpServletRequest().getScheme().equalsIgnoreCase("https"))
			    	dnsName = dnsName.replaceFirst(unprotectedProtocolName + "://", protectedProtocolName + "://").replaceFirst(unprotectedProtocolPort, protectedProtocolPort);
            	if(logger.isInfoEnabled())
    		    	logger.info("A42:" + dnsName);

    		    if(repositoryPath != null)
    		    {
    		    	if(context.startsWith("/"))
        		    	context = dnsName + context + "/" + repositoryPath;
        		    else
        		     	context = dnsName + "/" + (context.equals("") ? "" : context + "/") + repositoryPath;
    		    }
    		    else
    		    {
        		    if(context.startsWith("/"))
        		    	context = dnsName + context;
        		    else
        		    	context = dnsName + "/" + context;
        		}

            	if(logger.isInfoEnabled())
    		    	logger.info("A5:" + context);

    		}
    		else
    		{
    			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNode.getRepositoryId(), db);
    		    String dnsName = repositoryVO.getDnsName();

    		    String repositoryPath = null;
    	    	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
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
    		    	context = context + "/" + repositoryPath;
    		}

		    String enableNiceURIForLanguage = CmsPropertyHandler.getEnableNiceURIForLanguage();
        	//System.out.println("enableNiceURIForLanguage:" + enableNiceURIForLanguage);
        	if(enableNiceURIForLanguage.equalsIgnoreCase("true"))
        		context = context + "/" + LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(db, languageId).getLanguageCode();

            StringBuffer sb = new StringBuffer(256);

            if((deliveryContext.getUseFullUrl() || makeAccessBasedProtocolAdjustments) && context.indexOf("://") == -1)
	        {
		        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
	            int indexOfProtocol = originalUrl.indexOf("://");
	            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
	            String base = originalUrl.substring(0, indexFirstSlash);
	            sb.append(base);
	        }

	        sb.append(context);

	        try 
			{
	        	String navigationPath = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getPageNavigationPath(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext);
	            if(navigationPath != null && navigationPath.startsWith("/") && sb.toString().endsWith("/"))
	            	sb.append(navigationPath.substring(1));
	            else
	            	sb.append(navigationPath);

	            if(sb.toString().endsWith(context) && !sb.toString().endsWith("/"))
	                sb.append("/");

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

	            if(!enableNiceURIForLanguage.equalsIgnoreCase("true"))
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

	            SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db, true);
	    		String dnsName = CmsPropertyHandler.getWebServerAddress();
	    		if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
	    			dnsName = siteNode.getRepository().getDnsName();

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

			    if(deliveryContext.getHttpServletRequest().getScheme().equalsIgnoreCase("https"))
					url = url.replaceFirst(unprotectedProtocolName + "://", protectedProtocolName + "://").replaceFirst(unprotectedProtocolPort, protectedProtocolPort);
			    
	            String context = CmsPropertyHandler.getServletContext();

	            url = dnsName + context + "/" + CmsPropertyHandler.getApplicationBaseAction() + "?" + arguments;

				if(deliveryContext.getHttpServletRequest().getRequestURI().indexOf("!renderDecoratedPage") > -1)
				{
		            String componentRendererUrl = CmsPropertyHandler.getComponentRendererUrl();
					if(componentRendererUrl.endsWith("/"))
					    componentRendererUrl += "/";
					
					url = componentRendererUrl + CmsPropertyHandler.getComponentRendererAction() + "?" + arguments;
				}
            }
            else
            {
            	StringBuilder sb = new StringBuilder(256);
                if(deliveryContext.getUseFullUrl() || makeAccessBasedProtocolAdjustments)
    	        {
    		        String originalUrl = deliveryContext.getHttpServletRequest().getRequestURL().toString();
    	            int indexOfProtocol = originalUrl.indexOf("://");
    	            int indexFirstSlash = originalUrl.indexOf("/", indexOfProtocol + 3);
    	            String base = originalUrl.substring(0, indexFirstSlash);
    	            sb.append(base);
    	        }
                
                String servletContext = CmsPropertyHandler.getServletContext();
    	        
                if(siteNodeId == null)
	    			siteNodeId = new Integer(-1);
	
	    		if(languageId == null)
	    			languageId = new Integer(-1);
	
	    		if(contentId == null)
	    			contentId = new Integer(-1);
	
	            String arguments = "siteNodeId=" + siteNodeId + getRequestArgumentDelimiter() + "languageId=" + languageId + getRequestArgumentDelimiter() + "contentId=" + contentId;
	            
				if(deliveryContext.getHttpServletRequest().getRequestURI().indexOf("!renderDecoratedPage") > -1)
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

    public String composePageUrlAfterLanguageChange(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
        String pageUrl = composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext);

        String enableNiceURI = CmsPropertyHandler.getEnableNiceURI();
        if(enableNiceURI == null || enableNiceURI.equalsIgnoreCase(""))
        	enableNiceURI = "false";
        
	    String enableNiceURIForLanguage = CmsPropertyHandler.getEnableNiceURIForLanguage();
        if(enableNiceURI.equalsIgnoreCase("true") && !deliveryContext.getDisableNiceUri() && !enableNiceURIForLanguage.equalsIgnoreCase("true"))
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

			    principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
				
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