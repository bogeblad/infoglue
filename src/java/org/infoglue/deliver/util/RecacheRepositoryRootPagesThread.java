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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.AccessRightUserImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallSystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserRoleImpl;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.services.CacheEvictionBeanListenerService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.applications.filters.URIMapperCache;
import org.infoglue.deliver.cache.PageCacheHelper;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;

/**
 * @author mattias
 *
 * This is a selective publication thread. What that means is that it only throws
 * away objects and pages in the cache which are affected. Experimental for now.
 */
public class RecacheRepositoryRootPagesThread implements Runnable
{
    private final static Logger logger = Logger.getLogger(DeliverContextListener.class.getName());

    private static AtomicBoolean running = new AtomicBoolean(false);
    
	public synchronized void run()
	{
		if(CmsPropertyHandler.getRecacheWorkingUrl().equals("@recacheWorkingUrl@"))
			return;
		
		if(running.compareAndSet(false, true))
		{
			logger.warn("Recaching startpage on each repo\n\n");
			Timer t = new Timer();
	  		HttpHelper helper = new HttpHelper();
	  		
	  		try
	  		{
		  		if(CmsPropertyHandler.getInternalDeliveryUrls().size() > 0)
		        {
		  			List<RepositoryVO> repositories = RepositoryController.getController().getRepositoryVOList();
		  			int i = 0;
		  			for(RepositoryVO repository : repositories)
		  			{
		  				i++;
		  				if(i > 20)
		  					break;
		  				
		  				Thread.sleep(2000);
		  				try
		  				{
		      				SiteNodeVO siteNodeVO = SiteNodeController.getController().getRootSiteNodeVO(repository.getId());
		      				if(siteNodeVO != null)
		      				{
		          				logger.info("siteNodeVO:" + siteNodeVO.getName());
			        			String url = CmsPropertyHandler.getComponentRendererUrl() + "ViewPage.action?siteNodeId=" + siteNodeVO.getId();
			        			
			        			if(!CmsPropertyHandler.getRecacheWorkingUrl().equals(""))
			        			{
			        				url = CmsPropertyHandler.getRecacheWorkingUrl() + "?siteNodeId=" + siteNodeVO.getId();
			        			}
			        			else
			        			{
			        				url = "http://localhost" + url;
			        			}
			        					
			        			logger.info("url:" + url);
			        			helper.getUrlContent(url, 30000);
			        			long time = t.getElapsedTime();
			        			if(time > 20000)
			        				logger.warn("Getting start page for " + repository.getName() + " took:" + time + " ms");
		      				}
		  				}
		  				catch (Exception e) 
		  				{
		  					logger.warn("Could not refresh deliver cache: " + e.getMessage(), e);
		  				}
		  			}
		        }
	  		}
	  		catch (Exception e) 
	  		{
	  			logger.warn("Could not refresh deliver cache: " + e.getMessage(), e);
			}
	  		finally
	  		{
	  			running.set(false);
	  		}
		}
		else
			logger.warn("Running allready...");
	}
}
