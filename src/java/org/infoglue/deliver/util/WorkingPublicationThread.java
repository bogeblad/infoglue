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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallishContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallContentVersionImpl;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallGroupImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallRoleImpl;
import org.infoglue.cms.entities.management.impl.simple.SmallSystemUserImpl;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;

/**
 * @author mattias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkingPublicationThread extends Thread
{
    public final static Logger logger = Logger.getLogger(WorkingPublicationThread.class.getName());

    private List cacheEvictionBeans = new ArrayList();
	
	public WorkingPublicationThread()
	{
	}
	
	public List getCacheEvictionBeans()
	{
		return cacheEvictionBeans;
	}

	public void run() 
	{
		work();
	}
	
	
	public void work()
	{
		//synchronized (cacheEvictionBeans) 
		//{

		logger.info("cacheEvictionBeans.size:" + cacheEvictionBeans.size() + ":" + RequestAnalyser.getRequestAnalyser().getBlockRequests());
        if(cacheEvictionBeans.size() > 0)
		{
        	//logger.warn("setting block");
            //RequestAnalyser.setBlockRequests(true);
    		
            //Database db = null;
			
			try
			{
			    //db = CastorDatabaseService.getDatabase();
	
				//int publicationDelay = 50;
	
			    //logger.info("\n\n\nSleeping " + publicationDelay + "ms.\n\n\n");
			    //sleep(publicationDelay);
			
				Iterator i = cacheEvictionBeans.iterator();
				while(i.hasNext())
				{
				    CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)i.next();
				    String className = cacheEvictionBean.getClassName();
				    String objectId = cacheEvictionBean.getObjectId();
				    String objectName = cacheEvictionBean.getObjectName();
					String typeId = cacheEvictionBean.getTypeId();
					
				    logger.info("className:" + className);
					logger.info("objectId:" + objectId);
					logger.info("objectName:" + objectName);
					logger.info("typeId:" + typeId);

					try
					{
				        boolean isDependsClass = false;
					    if(className != null && className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
					        isDependsClass = true;
				
					    CacheController.clearCaches(className, objectId, null);
			
					    logger.info("Updating className with id:" + className + ":" + objectId);
					    if(className != null && !typeId.equalsIgnoreCase("" + NotificationMessage.SYSTEM))
						{
						    Class type = Class.forName(className);
			
						    if(!isDependsClass && 
						    		className.equalsIgnoreCase(SystemUserImpl.class.getName()) || 
						    		className.equalsIgnoreCase(RoleImpl.class.getName()) || 
						    		className.equalsIgnoreCase(GroupImpl.class.getName()) || 
						    		className.equalsIgnoreCase(SmallSystemUserImpl.class.getName()) || 
						    		className.equalsIgnoreCase(SmallRoleImpl.class.getName()) || 
						    		className.equalsIgnoreCase(SmallGroupImpl.class.getName()))
						    {
						        Object[] ids = {objectId};
						        CacheController.clearCache(type, ids);
							}
						    else if(!isDependsClass)
						    {
						        Object[] ids = {new Integer(objectId)};
							    CacheController.clearCache(type, ids);
						    }
			
							//If it's an contentVersion we should delete all images it might have generated from attributes.
							if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
							{
							    logger.info("We clear all small contents as well " + objectId);
								Class typesExtra = SmallContentImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
	
							    logger.info("We clear all smallish contents as well " + objectId);
								Class typesExtraSmallish = SmallishContentImpl.class;
								Object[] idsExtraSmallish = {new Integer(objectId)};
								CacheController.clearCache(typesExtraSmallish, idsExtraSmallish);
	
								logger.info("We clear all medium contents as well " + objectId);
								Class typesExtraMedium = MediumContentImpl.class;
								Object[] idsExtraMedium = {new Integer(objectId)};
								CacheController.clearCache(typesExtraMedium, idsExtraMedium);
							}
							if(Class.forName(className).getName().equals(ContentVersionImpl.class.getName()))
							{
							    logger.info("We clear all small contents as well " + objectId);
								Class typesExtra = SmallContentVersionImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
	
								logger.info("We clear all small contents as well " + objectId);
								Class typesExtraSmallest = SmallestContentVersionImpl.class;
								Object[] idsExtraSmallest = {new Integer(objectId)};
								CacheController.clearCache(typesExtraSmallest, idsExtraSmallest);

								//Removing all scriptExtensionBundles to make sure
								CacheController.removeScriptExtensionBundles();							    	
							}
							else if(Class.forName(className).getName().equals(AvailableServiceBindingImpl.class.getName()))
							{
							    Class typesExtra = SmallAvailableServiceBindingImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(SiteNodeImpl.class.getName()))
							{
							    Class typesExtra = SmallSiteNodeImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(SiteNodeVersionImpl.class.getName()))
							{
							    Class typesExtra = SmallSiteNodeVersionImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(DigitalAssetImpl.class.getName()))
							{
								CacheController.clearCache("digitalAssetCache");
								Class typesExtra = SmallDigitalAssetImpl.class;
								Object[] idsExtra = {new Integer(objectId)};
								CacheController.clearCache(typesExtra, idsExtra);
	
								Class typesExtraMedium = MediumDigitalAssetImpl.class;
								Object[] idsExtraMedium = {new Integer(objectId)};
								CacheController.clearCache(typesExtraMedium, idsExtraMedium);
	
								String disableAssetDeletionInWorkThread = CmsPropertyHandler.getDisableAssetDeletionInWorkThread();
								if(disableAssetDeletionInWorkThread != null && !disableAssetDeletionInWorkThread.equals("true"))
								{
									logger.info("We should delete all images with digitalAssetId " + objectId);
									DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
								}

								//Removing all scriptExtensionBundles to make sure
								CacheController.removeScriptExtensionBundles();							    	
							}
							else if(Class.forName(className).getName().equals(SystemUserImpl.class.getName()))
							{
							    Class typesExtra = SmallSystemUserImpl.class;
								Object[] idsExtra = {objectId};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(RoleImpl.class.getName()))
							{
							    Class typesExtra = SmallRoleImpl.class;
								Object[] idsExtra = {objectId};
								CacheController.clearCache(typesExtra, idsExtra);
							}
							else if(Class.forName(className).getName().equals(GroupImpl.class.getName()))
							{
							    Class typesExtra = SmallGroupImpl.class;
								Object[] idsExtra = {objectId};
								CacheController.clearCache(typesExtra, idsExtra);
							}
	
						    logger.info("4");
						}	
					}
					catch (Exception e) 
					{
						logger.error("Error handling cache update message:" + className + ":" + objectId);
					}
				}
			} 
			catch (Exception e)
			{
			    logger.error("An error occurred in the WorkingPublicationThread:" + e.getMessage(), e);
			}
			/*
			finally
			{
				try
				{
					if(db != null)
						db.close();
				}
				catch(Exception e)
				{
				    logger.error("An error occurred in the WorkingPublicationThread:" + e.getMessage(), e);
				}
			}
			*/
		}

        RequestAnalyser.getRequestAnalyser().setBlockRequests(false);
		logger.info("released block");
	}
}
