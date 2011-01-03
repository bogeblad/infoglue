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


package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.ByteArrayInputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.GroupProperties;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RoleProperties;
import org.infoglue.cms.entities.management.UserProperties;
import org.infoglue.cms.entities.management.UserPropertiesVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.jobs.SubscriptionsJob;
import org.infoglue.cms.security.AuthorizationModule;
import org.infoglue.cms.security.BasicMethodAccessManager;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;

/**
 * @author Mattias Bogeblad
 * 
 * This class acts as the proxy for getting a principal from the right source. The source may vary depending
 * on security setup.
 */

public class InfoGluePrincipalControllerProxy extends BaseController 
{
    private final static Logger logger = Logger.getLogger(InfoGluePrincipalControllerProxy.class.getName());

	public static InfoGluePrincipalControllerProxy getController()
	{
		return new InfoGluePrincipalControllerProxy();
	}
	
	/**
	 * This method returns a specific content-object
	 */
	/*
    public InfoGluePrincipal getInfoGluePrincipal(String userName) throws ConstraintException, SystemException
    {
		InfoGluePrincipal infoGluePrincipal = null;
    	
    	try
    	{
			AuthorizationModule authorizationModule = (AuthorizationModule)Class.forName(InfoGlueAuthenticationFilter.authorizerClass).newInstance();
			authorizationModule.setExtraProperties(InfoGlueAuthenticationFilter.extraProperties);
			
			infoGluePrincipal = authorizationModule.getAuthorizedInfoGluePrincipal(userName);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return infoGluePrincipal;
    }
 	*/
	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, boolean escapeSpecialCharacters, boolean findLargestValue) throws Exception
	{
		return getPrincipalPropertyValue(infoGluePrincipal, propertyName, languageId, siteNodeId, useLanguageFallback, escapeSpecialCharacters, findLargestValue, false);
	}
	
	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, boolean escapeSpecialCharacters, boolean findLargestValue, boolean findPrioValue) throws Exception
	{
		String value = "";
		
		if(infoGluePrincipal == null || propertyName == null)
			return null;
		
		Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(db);
		
		try
        {
		    value = getPrincipalPropertyValue(db, infoGluePrincipal, propertyName, languageId, siteNodeId, useLanguageFallback, escapeSpecialCharacters, findLargestValue, findPrioValue);
		    commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }		
        	
		return value;
	}	
	

	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(Database db, InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, boolean escapeSpecialCharacters, boolean findLargestValue) throws Exception
	{
		return getPrincipalPropertyValue(db, infoGluePrincipal, propertyName, languageId, siteNodeId, useLanguageFallback, escapeSpecialCharacters, findLargestValue, false);
	}
	
	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(Database db, InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, boolean escapeSpecialCharacters, boolean findLargestValue, boolean findPrioValue) throws Exception
	{
		String key = "" + infoGluePrincipal.getName() + "_" + propertyName + "_" + languageId + "_" + siteNodeId + "_" + useLanguageFallback + "_" + escapeSpecialCharacters + "_" + findLargestValue + "_" + findPrioValue;
		logger.info("key:" + key);
		Object object = (String)CacheController.getCachedObject("principalPropertyValueCache", key);

	    if(object instanceof NullObject)
		{
			logger.info("There was an cached property but it was null:" + object);
			return null;
		}
		else if(object != null)
		{
			logger.info("There was an cached principalPropertyValue:" + object);
			return (String)object;
		}

		String value = "";
		
		if(infoGluePrincipal == null || propertyName == null)
			return null;
	
	    Collection userPropertiesList = UserPropertiesController.getController().getUserPropertiesList(infoGluePrincipal.getName(), languageId, db, true);
		Iterator userPropertiesListIterator = userPropertiesList.iterator();
		while(userPropertiesListIterator.hasNext())
		{
			UserProperties userProperties = (UserProperties)userPropertiesListIterator.next();

			if(userProperties != null && userProperties.getLanguage().getLanguageId().equals(languageId) && userProperties.getValue() != null && propertyName != null)
			{
				String propertyXML = userProperties.getValue();
				DOMBuilder domBuilder = new DOMBuilder();
				Document document = domBuilder.getDocument(propertyXML);
	
				Node node = document.getRootElement().selectSingleNode("attributes/" + propertyName);
				if(node != null)
				{
					value = node.getStringValue();
					logger.info("Getting value: " + value);
					if(value != null && escapeSpecialCharacters)
						value = new VisualFormatter().escapeHTML(value);
					break;
				}
			}
		}
		
		if(value.equals(""))
		{	
			List roles = infoGluePrincipal.getRoles();
			String largestValue = "-1";
			String prioValue = null;
			int latestPriority = 0;
			Iterator rolesIterator = roles.iterator();
			while(rolesIterator.hasNext())
			{
				InfoGlueRole role = (InfoGlueRole)rolesIterator.next();
				
				Collection rolePropertiesList = RolePropertiesController.getController().getRolePropertiesList(role.getName(), languageId, db, true);

				Iterator rolePropertiesListIterator = rolePropertiesList.iterator();
				while(rolePropertiesListIterator.hasNext())
				{
					RoleProperties roleProperties = (RoleProperties)rolePropertiesListIterator.next();
					
					if(roleProperties != null && roleProperties.getLanguage().getLanguageId().equals(languageId) && roleProperties.getValue() != null && propertyName != null)
					{
						String propertyXML = roleProperties.getValue();
						DOMBuilder domBuilder = new DOMBuilder();
						Document document = domBuilder.getDocument(propertyXML);
						
						Node propertyPriorityNode = document.getRootElement().selectSingleNode("attributes/PropertyPriority");
						int currentPriority = 0;
						if(propertyPriorityNode != null)
						{
							try
							{
								String propertyPriorityValue = propertyPriorityNode.getStringValue();
								logger.info("propertyPriorityValue:" + propertyPriorityValue);

								if(propertyPriorityValue != null && !propertyPriorityValue.equals(""))
									currentPriority = new Integer(propertyPriorityValue);
							}
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}
						
						Node node = document.getRootElement().selectSingleNode("attributes/" + propertyName);
						if(node != null)
						{
							value = node.getStringValue();
							logger.info("Getting value: " + value);
							if(value != null && escapeSpecialCharacters)
								value = new VisualFormatter().escapeHTML(value);
							
							if(value != null && !value.equals("") && findLargestValue && new Integer(largestValue).intValue() < new Integer(value).intValue())
							    largestValue = value;

							logger.info("" + findLargestValue + ":" + findPrioValue + ":" + currentPriority + "=" + latestPriority);
							if(value != null && !value.equals("") && !findLargestValue && findPrioValue && currentPriority > latestPriority)
							{
								logger.info("Using other value..");
								prioValue = value;
								latestPriority = currentPriority;
							}

							break;
						}
					}
				}
			}
			
			if(findLargestValue)
			    value = largestValue;
			
			if(findPrioValue && prioValue != null)
			{
			    value = prioValue;
			    logger.info("Using prio value");
			}
			
			if(value.equals("") && useLanguageFallback)
			{
				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, siteNodeId);
				if(!masterLanguageVO.getLanguageId().equals(languageId))
					value = getPrincipalPropertyValue(infoGluePrincipal, propertyName, masterLanguageVO.getLanguageId(), siteNodeId, useLanguageFallback, escapeSpecialCharacters, findLargestValue);
			}
		}
		
		if(value.equals(""))
		{	
			List groups = infoGluePrincipal.getGroups();
			String largestValue = "-1";
			String prioValue = null;
			int latestPriority = 0;
			Iterator groupsIterator = groups.iterator();
			while(groupsIterator.hasNext())
			{
				InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
				
				Collection groupPropertiesList = GroupPropertiesController.getController().getGroupPropertiesList(group.getName(), languageId, db, true);

				Iterator groupPropertiesListIterator = groupPropertiesList.iterator();
				while(groupPropertiesListIterator.hasNext())
				{
					GroupProperties groupProperties = (GroupProperties)groupPropertiesListIterator.next();
					
					if(groupProperties != null && groupProperties.getLanguage().getLanguageId().equals(languageId) && groupProperties.getValue() != null && propertyName != null)
					{
						String propertyXML = groupProperties.getValue();
						DOMBuilder domBuilder = new DOMBuilder();
						Document document = domBuilder.getDocument(propertyXML);
						
						Node propertyPriorityNode = document.getRootElement().selectSingleNode("attributes/PropertyPriority");
						int currentPriority = 0;
						if(propertyPriorityNode != null)
						{
							try
							{
								String propertyPriorityValue = propertyPriorityNode.getStringValue();
								logger.info("propertyPriorityValue:" + propertyPriorityValue);

								if(propertyPriorityValue != null && !propertyPriorityValue.equals(""))
									currentPriority = new Integer(propertyPriorityValue);
							}
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}

						Node node = document.getRootElement().selectSingleNode("attributes/" + propertyName);
						if(node != null)
						{
							value = node.getStringValue();
							logger.info("Getting value: " + value);
							if(value != null && escapeSpecialCharacters)
								value = new VisualFormatter().escapeHTML(value);
							
							if(value != null && !value.equals("") && findLargestValue && new Integer(largestValue).intValue() < new Integer(value).intValue())
							    largestValue = value;
							
							logger.info("" + findLargestValue + ":" + findPrioValue + ":" + currentPriority + "=" + latestPriority);
							if(value != null && !value.equals("") && !findLargestValue && findPrioValue && currentPriority > latestPriority)
							{
								logger.info("Using other value..");
								prioValue = value;
								latestPriority = currentPriority;
							}
							
							break;
						}
					}
				}
			}
			
			if(findLargestValue)
			    value = largestValue;
			
			if(findPrioValue && prioValue != null)
			{
			    value = prioValue;
			    logger.info("Using prio value");
			}

			if(value.equals("") && useLanguageFallback)
			{
				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, siteNodeId);
				if(!masterLanguageVO.getLanguageId().equals(languageId))
					value = getPrincipalPropertyValue(infoGluePrincipal, propertyName, masterLanguageVO.getLanguageId(), siteNodeId, useLanguageFallback, escapeSpecialCharacters, findLargestValue);
			}
		}
			
		if(value != null)
		    CacheController.getCachedObject("principalPropertyValueCache", key);
		
		if(value != null)
	        CacheController.cacheObject("principalPropertyValueCache", key, value);
	    else
	        CacheController.cacheObject("principalPropertyValueCache", key, new NullObject());
		
		return value;
	}	
	
	
	/**
	 * Getting all assets for a Principal - used for personalisation. 
	 */
	
	public List getPrincipalAssets(Database db, InfoGluePrincipal infoGluePrincipal, Integer languageId) throws Exception
	{
		/*
		String key = "" + infoGluePrincipal.getName() + "_" + languageId;
		logger.info("key:" + key);
		Object object = (String)CacheController.getCachedObject("principalPropertyValueCache", key);

	    if(object instanceof NullObject)
		{
			logger.info("There was an cached property but it was null:" + object);
			return null;
		}
		else if(object != null)
		{
			logger.info("There was an cached principalPropertyValue:" + object);
			return (String)object;
		}
		*/
		
		List digitalAssets = new ArrayList();
		
		if(infoGluePrincipal == null)
			return null;
	
		try
		{
			List userPropertiesVOList = UserPropertiesController.getController().getUserPropertiesVOList(infoGluePrincipal.getName(), languageId);
			if(userPropertiesVOList != null && userPropertiesVOList.size() > 0)
			{
				UserPropertiesVO userPropertiesVO = (UserPropertiesVO)userPropertiesVOList.get(0);
				if(userPropertiesVO != null && userPropertiesVO.getId() != null)
		       	{
		       		digitalAssets = UserPropertiesController.getController().getDigitalAssetVOList(userPropertiesVO.getId());
		       	}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of digitalAssets: " + e.getMessage(), e);
		}
		/*
		if(value != null)
		    CacheController.getCachedObject("principalPropertyValueCache", key);
		
		if(value != null)
	        CacheController.cacheObject("principalPropertyValueCache", key, value);
	    else
	        CacheController.cacheObject("principalPropertyValueCache", key, new NullObject());
		*/
		
		return digitalAssets;
	}	

	/**
	 * Getting all assets for a Principal - used for personalisation. 
	 */
	
	public DigitalAssetVO getPrincipalAsset(Database db, InfoGluePrincipal infoGluePrincipal, Integer languageId, String assetKey) throws Exception
	{
		DigitalAssetVO asset = null;
		
		if(infoGluePrincipal == null)
			return null;
	
		try
		{
			List<DigitalAssetVO> assetList = getPrincipalAssets(db, infoGluePrincipal, languageId);
			Iterator<DigitalAssetVO> assetListIterator = assetList.iterator();
			while(assetListIterator.hasNext())
			{
				DigitalAssetVO currentAsset = (DigitalAssetVO)assetListIterator.next();
				logger.info("" + currentAsset.getAssetKey() + "=" + assetKey);
				if(currentAsset.getAssetKey().equals(assetKey))
				{
					asset = currentAsset;
					break;
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of digitalAssets: " + e.getMessage(), e);
		}
		/*
		if(value != null)
		    CacheController.getCachedObject("principalPropertyValueCache", key);
		
		if(value != null)
	        CacheController.cacheObject("principalPropertyValueCache", key, value);
	    else
	        CacheController.cacheObject("principalPropertyValueCache", key, new NullObject());
		*/
		
		return asset;
	}	

	
	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well. The value in question is a map - name-value.
	 */
	
	public Map getPrincipalPropertyHashValues(InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, boolean escapeSpecialCharacters) throws Exception
	{
		Properties properties = new Properties();
		
		String attributeValue = getPrincipalPropertyValue(infoGluePrincipal, propertyName, languageId, siteNodeId, useLanguageFallback, escapeSpecialCharacters, false);
		
		ByteArrayInputStream is = new ByteArrayInputStream(attributeValue.getBytes("UTF-8"));

		properties.load(is);
        
		return properties;
	}	

	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well. The value in question is a map - name-value.
	 */
	
	public Map getPrincipalPropertyHashValues(Database db, InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, boolean escapeSpecialCharacters) throws Exception
	{
		Properties properties = new Properties();
		
		String attributeValue = getPrincipalPropertyValue(db, infoGluePrincipal, propertyName, languageId, siteNodeId, useLanguageFallback, escapeSpecialCharacters, false);
		
		ByteArrayInputStream is = new ByteArrayInputStream(attributeValue.getBytes("UTF-8"));

		properties.load(is);
        
		return properties;
	}	

	public InfoGluePrincipal getTestPrincipal()
	{
		//BasicMethodAccessManager.checkAccessToCall(new String[]{SubscriptionsJob.class.getName()}, "FUCK OFF - your attempt have been registered.");
		
		return new InfoGluePrincipal("TestUser", "none", "none", "none", new ArrayList(), new ArrayList(), true, null);
	}
	
	
	public BaseEntityVO getNewVO()
	{
		return null;
	}
}
