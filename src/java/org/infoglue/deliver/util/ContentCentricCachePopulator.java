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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.LanguageVO;
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
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;


public class ContentCentricCachePopulator
{ 
    public final static Logger logger = Logger.getLogger(ContentCentricCachePopulator.class.getName());
		

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

		Integer rootMetaInfoContentId = templateController.getMetaInformationContentId(siteNodeId);
		logger.info("rootMetaInfoContentId:" + rootMetaInfoContentId);
		
		recurseSiteNodeTree(siteNodeId, languageId, templateController);

	    Integer topContentId = null;
	    ContentVO contentVO = templateController.getContent(rootMetaInfoContentId);
	    logger.info("contentVO:" + contentVO.getName());
		ContentVO parentContentVO = templateController.getContent(contentVO.getParentContentId());
		logger.info("parentContentVO:" + parentContentVO.getName());
		while(parentContentVO != null)
	    {
            topContentId = parentContentVO.getContentId();

            if(parentContentVO.getParentContentId() != null)
                parentContentVO = templateController.getContent(parentContentVO.getParentContentId());
            else
                parentContentVO = null;
	    }
	    
		logger.info("topContentId:" + topContentId);
		
		if(topContentId != null)
	        recurseContentTree(topContentId, languageId, templateController);

	}
	
	
	private void recurseContentTree(Integer contentId, Integer languageId, TemplateController templateController) throws Exception
	{
	    Content content = ContentController.getContentController().getReadOnlyContentWithId(contentId, templateController.getDatabase());
	    ContentVO contentVO = templateController.getContent(contentId);
	    
	    Collection childContents = content.getChildren();
		
	    Iterator childContentsIterator = childContents.iterator();
	    while(childContentsIterator.hasNext())
        {
	        Content childContent = (Content)childContentsIterator.next();
	        recurseContentTree(childContent.getId(), languageId, templateController);
	        
	        logger.info("Before read title of content...");
	        templateController.getContentAttribute(childContent.getId(), languageId, "Title", true); 
	        templateController.getContentAttribute(childContent.getId(), languageId, "NavigationTitle", true); 
	        //templateController.getContentAttribute(childContent.getId(), languageId, "LeadIn", true); 
	        //templateController.getContentAttribute(childContent.getId(), languageId, "FullText", true); 
	        logger.info("Read title of content...");
        }
	    //Thread.sleep(100);
	}
	
	private void recurseSiteNodeTree(Integer siteNodeId, Integer languageId, TemplateController templateController) throws Exception
	{
	    SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, templateController.getDatabase(), true);
	    SiteNodeVO siteNodeVO = templateController.getSiteNode(siteNodeId);
	    Collection childSiteNodes = siteNode.getChildSiteNodes();
	    
	    Iterator childSiteNodesIterator = childSiteNodes.iterator();
	    while(childSiteNodesIterator.hasNext())
        {
	        SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
	        recurseSiteNodeTree(childSiteNode.getSiteNodeId(), languageId, templateController);
	        
	        Integer metaInfoContentId = templateController.getMetaInformationContentId(childSiteNode.getSiteNodeId()); 
	        templateController.getContentAttribute(metaInfoContentId, languageId, "ComponentStructure", true); 
        }
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