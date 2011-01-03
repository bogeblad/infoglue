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

package org.infoglue.deliver.util;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.FakeHttpServletRequest;
import org.infoglue.cms.util.FakeHttpServletResponse;
import org.infoglue.cms.util.FakeHttpSession;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;


public class RequestAndMetaInfoCentricCachePopulator
{ 
    public final static Logger logger = Logger.getLogger(RequestAndMetaInfoCentricCachePopulator.class.getName());

	/**
	 * This method simulates a call to a page so all castor caches fills up before we throw the old page cache.
	 * @param db
	 * @param siteNodeId
	 * @param languageId
	 * @param contentId
	 */
	
	public void recache(DatabaseWrapper dbWrapper, Integer siteNodeId) throws SystemException, Exception
	{
        logger.info("recache starting..");

        HttpHelper helper = new HttpHelper();
        String recacheUrl = CmsPropertyHandler.getRecacheUrl() + "?siteNodeId=" + siteNodeId + "&refresh=true&isRecacheCall=true";
        String response = helper.getUrlContent(recacheUrl, 30000);
        
        String recacheBaseUrl = CmsPropertyHandler.getRecacheUrl().replaceAll("/ViewPage.action", "");
        String pathsToRecacheOnPublishing = CmsPropertyHandler.getPathsToRecacheOnPublishing();
        if(pathsToRecacheOnPublishing.indexOf("pathsToRecacheOnPublishing") == -1)
        {
	        String[] pathsToRecacheOnPublishingArray = pathsToRecacheOnPublishing.split(",");
	        for(int i = 0; i < pathsToRecacheOnPublishingArray.length; i++)
	        {
	            recacheUrl = recacheBaseUrl + pathsToRecacheOnPublishingArray[i] + "?refresh=true&isRecacheCall=true";
	            logger.info("calling recacheUrl:" + recacheUrl);
	        }
        }
        
		LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(dbWrapper.getDatabase(), siteNodeId);
		if(masterLanguageVO == null)
			throw new SystemException("There was no master language for the siteNode " + siteNodeId);
	
		Integer languageId = masterLanguageVO.getLanguageId();
		if(languageId == null)
		    languageId = masterLanguageVO.getLanguageId();				
				
		Integer contentId = new Integer(-1);
		
		Principal principal = (Principal)CacheController.getCachedObject("userCache", "anonymous");
		if(principal == null)
		{
		    Map arguments = new HashMap();
		    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
		    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
		    
			principal = ExtranetController.getController().getAuthenticatedPrincipal(dbWrapper.getDatabase(), arguments);
			
			if(principal != null)
				CacheController.cacheObject("userCache", "anonymous", principal);
		}

        FakeHttpSession fakeHttpServletSession = new FakeHttpSession();
        FakeHttpServletResponse fakeHttpServletResponse = new FakeHttpServletResponse();
        FakeHttpServletRequest fakeHttpServletRequest = new FakeHttpServletRequest();
        fakeHttpServletRequest.setParameter("siteNodeId", "" + siteNodeId);
        fakeHttpServletRequest.setParameter("languageId", "" + languageId);
        fakeHttpServletRequest.setParameter("contentId", "" + contentId);
        fakeHttpServletRequest.setRequestURI("ViewPage.action");

        fakeHttpServletRequest.setAttribute("siteNodeId", "" + siteNodeId);
        fakeHttpServletRequest.setAttribute("languageId", "" + languageId);
        fakeHttpServletRequest.setAttribute("contentId", "" + contentId);

        fakeHttpServletRequest.setServletContext(DeliverContextListener.getServletContext());
        
        BrowserBean browserBean = new BrowserBean();
	    //this.browserBean.setRequest(getRequest());

		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		IntegrationDeliveryController integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(siteNodeId, languageId, contentId);
		TemplateController templateController = getTemplateController(dbWrapper, siteNodeId, languageId, contentId, new FakeHttpServletRequest(), (InfoGluePrincipal)principal, false, browserBean, nodeDeliveryController, integrationDeliveryController);
		
		DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(/*(InfoGluePrincipal)this.principal*/);
		//deliveryContext.setRepositoryName(repositoryName);
		deliveryContext.setSiteNodeId(siteNodeId);
		deliveryContext.setContentId(contentId);
		deliveryContext.setLanguageId(languageId);
		deliveryContext.setPageKey("" + System.currentTimeMillis());
		//deliveryContext.setSession(new Session(fakeHttpServletSession));
		//deliveryContext.setInfoGlueAbstractAction(null);
		deliveryContext.setHttpServletRequest(fakeHttpServletRequest);
		deliveryContext.setHttpServletResponse(fakeHttpServletResponse);

		templateController.setDeliveryContext(deliveryContext);
		
		//We don't want a page cache entry to be created
		deliveryContext.setDisablePageCache(true);

		SiteNodeVO siteNodeVO = templateController.getSiteNode(siteNodeId);
		SiteNodeVO rootSiteNodeVO = templateController.getRepositoryRootSiteNode(siteNodeVO.getRepositoryId());
		
		recurseSiteNodeTree(rootSiteNodeVO.getId(), languageId, templateController, principal/*, dbWrapper*/, 1, 0);

		List templates = ContentController.getContentController().getContentVOWithContentTypeDefinition("HTMLTemplate", dbWrapper.getDatabase());
		Iterator templatesIterator = templates.iterator();
		{
		    ContentVO template = (ContentVO)templatesIterator.next();

		    String templateString = templateController.getContentAttribute(template.getId(), languageId, "Template", true); 
		}
		
		RepositoryVO repository = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(dbWrapper.getDatabase());
		
		RepositoryDeliveryController.getRepositoryDeliveryController().getRepositoryVOListFromServerName(dbWrapper.getDatabase(), "localhost", "8080", repository.getName());
		
        logger.info("recache stopped..");
	}
	
	
	private void recurseSiteNodeTree(Integer siteNodeId, Integer languageId, TemplateController templateController, Principal principal/*, DatabaseWrapper dbWrapper*/, int maxLevel, int currentLevel) throws Exception
	{		
	    SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, templateController.getDatabase(), true);
	    SiteNodeVO siteNodeVO = templateController.getSiteNode(siteNodeId);

        templateController.getContentAttribute(siteNodeVO.getMetaInfoContentId(), languageId, "Title", true); 
        templateController.getContentAttribute(siteNodeVO.getMetaInfoContentId(), languageId, "NavigationTitle", true); 
        templateController.getContentAttribute(siteNodeVO.getMetaInfoContentId(), languageId, "NiceURIName", true); 
        templateController.getContentAttribute(siteNodeVO.getMetaInfoContentId(), languageId, "Description", true); 
        templateController.getContentAttribute(siteNodeVO.getMetaInfoContentId(), languageId, "ComponentStructure", true); 

        List childPages = templateController.getChildPages(siteNodeId);
        
        templateController.getRepositoryRootSiteNode(siteNodeVO.getRepositoryId());
        templateController.getParentSiteNode(siteNodeVO.getId());
        
        Collection childSiteNodes = siteNode.getChildSiteNodes();
	    
        /*
        List childSiteNodeIds = new ArrayList();
        Iterator childSiteNodesIterator = childSiteNodes.iterator();
	    while(childSiteNodesIterator.hasNext())
        {
	        SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
	        childSiteNodeIds.add(childSiteNode.getSiteNodeId());
        }

        templateController.commitDatabase();

	    Iterator childSiteNodeIdsIterator = childSiteNodeIds.iterator();
	    while(childSiteNodeIdsIterator.hasNext())
        {
	        Integer childSiteNodeId = (Integer)childSiteNodeIdsIterator.next();
	        recurseSiteNodeTree(childSiteNodeId, languageId, templateController, principal);
        }

        */

        Iterator childSiteNodesIterator = childSiteNodes.iterator();
	    while(childSiteNodesIterator.hasNext())
        {
	        SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
	        if(maxLevel > currentLevel)
	        	recurseSiteNodeTree(childSiteNode.getId(), languageId, templateController, principal, maxLevel, currentLevel + 1);
        }

	 
		Repository repository = RepositoryController.getController().getRepositoryWithId(siteNodeVO.getRepositoryId(), templateController.getDatabase());
		Collection languages = repository.getRepositoryLanguages();
		Iterator languagesIterator = languages.iterator();
		while(languagesIterator.hasNext())
		{
		    RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languagesIterator.next();
		    LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(templateController.getDatabase(), repositoryLanguage.getLanguage().getId(), siteNodeId);
		}
   
	    
	    Integer contentId = new Integer(-1);
	    
        FakeHttpSession fakeHttpServletSession = new FakeHttpSession();
        FakeHttpServletResponse fakeHttpServletResponse = new FakeHttpServletResponse();
        FakeHttpServletRequest fakeHttpServletRequest = new FakeHttpServletRequest();
        fakeHttpServletRequest.setParameter("siteNodeId", "" + siteNodeId);
        fakeHttpServletRequest.setParameter("languageId", "" + languageId);
        fakeHttpServletRequest.setParameter("contentId", "" + contentId);
        fakeHttpServletRequest.setRequestURI("ViewPage.action");

        fakeHttpServletRequest.setAttribute("siteNodeId", "" + siteNodeId);
        fakeHttpServletRequest.setAttribute("languageId", "" + languageId);
        fakeHttpServletRequest.setAttribute("contentId", "" + contentId);

        fakeHttpServletRequest.setServletContext(DeliverContextListener.getServletContext());
        
        BrowserBean browserBean = new BrowserBean();
	    //this.browserBean.setRequest(getRequest());
	    
		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		IntegrationDeliveryController integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(siteNodeId, languageId, contentId);
		TemplateController subTemplateController = getTemplateController(templateController.getDatabaseWrapper(), siteNodeId, languageId, contentId, new FakeHttpServletRequest(), (InfoGluePrincipal)principal, false, browserBean, nodeDeliveryController, integrationDeliveryController);

		DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(/*(InfoGluePrincipal)this.principal*/);
		//deliveryContext.setRepositoryName(repositoryName);
		deliveryContext.setSiteNodeId(siteNodeId);
		deliveryContext.setContentId(contentId);
		deliveryContext.setLanguageId(languageId);
		deliveryContext.setPageKey("" + System.currentTimeMillis());
		//deliveryContext.setSession(new Session(fakeHttpServletSession));
		//deliveryContext.setInfoGlueAbstractAction(null);
		deliveryContext.setHttpServletRequest(fakeHttpServletRequest);
		deliveryContext.setHttpServletResponse(fakeHttpServletResponse);

		subTemplateController.setDeliveryContext(deliveryContext);
		
		//We don't want a page cache entry to be created
		deliveryContext.setDisablePageCache(true);

		SiteNodeVO rootSiteNodeVO = templateController.getRepositoryRootSiteNode(siteNodeVO.getRepositoryId());

		String pagePath = subTemplateController.getCurrentPagePath();
				
		CacheController.cacheObject("newPagePathCache", deliveryContext.getPageKey(), pagePath);
	}
	
   	/**
	 * This method should be much more sophisticated later and include a check to see if there is a 
	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
	 * The method also consideres wheter or not to invoke the preview-version with administrative functioality or the 
	 * normal site-delivery version.
	 */
	
	public TemplateController getTemplateController(DatabaseWrapper dbWrapper, Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infoGluePrincipal, boolean allowEditOnSightAtAll, BrowserBean browserBean, NodeDeliveryController nodeDeliveryController, IntegrationDeliveryController integrationDeliveryController) throws SystemException, Exception
	{
		TemplateController templateController = new BasicTemplateController(dbWrapper, infoGluePrincipal);
		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
		templateController.setHttpRequest(request);	
		templateController.setBrowserBean(browserBean);
		templateController.setDeliveryControllers(nodeDeliveryController, null, integrationDeliveryController);	
		
		return templateController;		
	}


}