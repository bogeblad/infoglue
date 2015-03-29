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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.databeans.ComponentPropertyDefinition;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ContentComparator;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class handles all access to components and other actions.
 * 
 * @author Mattias Bogeblad
 */

public class ComponentController extends BaseController
{
    private final static Logger logger = Logger.getLogger(ComponentController.class.getName());

	private static AtomicBoolean preCaching = new AtomicBoolean(false);
	private static List<Integer> componentIdsToRecache = new ArrayList<Integer>();
	
    /**
	 * Factory method
	 */

	public static ComponentController getController()
	{
		return new ComponentController();
	}

	public void preCacheComponentsDelayed() throws Exception
	{
		class PreCacheTask implements Runnable 
		{
	        public void run() 
	        {
	        	try
	        	{
		        	Timer t = new Timer();
		        	preCacheComponents(2000);
		        	t.printElapsedTime("Precached components took");
	        	}
	        	catch (Exception e) 
	        	{
					logger.warn("Failed to precache components: " + e.getMessage(), e);
				}
	        }
	    }
	    Thread thread = new Thread(new PreCacheTask());
	    thread.start();
	}
	
	public void preCacheComponents(long delay) throws Exception
	{
		Database db = CastorDatabaseService.getDatabase();
       
        beginTransaction(db);

        try
        {
        	preCacheComponents(delay, db);
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}
	
	public void preCacheComponents(long delay, Database db) throws Exception
	{
		if(CmsPropertyHandler.getOperatingMode().equals("3"))
			return;
		
		String cacheKey = "allTemplatesAndPagePartMap";
		Map<String,List<ContentVO>> templatesAndPagePartMap = (Map<String,List<ContentVO>>)CacheController.getCachedObject("componentContentsCache", cacheKey);
		logger.info("preCaching value:" + preCaching.get());
		if((templatesAndPagePartMap == null || templatesAndPagePartMap.size() == 0) && preCaching.compareAndSet(false, true))
		{
			logger.info("preCaching sat value:" + preCaching.get());
			if(delay > 0)
			{
				logger.warn("Sleeping before precache: " + delay);
				Thread.sleep(delay);
			}
			
			logger.warn("Precaching all HTMLTemplates and PagePartTemplates");
			try
			{
				ContentTypeDefinitionVO htmlTemplateContentType = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("HTMLTemplate", db);
				ContentTypeDefinitionVO pagePartTemplateContentType = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("PagePartTemplate", db);
				
				Timer t = new Timer();
				List<ContentVersionVO> componentContentVersionVOList = null;
				if(pagePartTemplateContentType != null)
				{
					componentContentVersionVOList = ContentControllerProxy.getController().getLatestContentVersionVOListByContentTypeIdForSmallCollections(new Integer[]{htmlTemplateContentType.getId(), pagePartTemplateContentType.getId()}, db);
					logger.info("Getting componentContentVersionVOList 1 " + componentContentVersionVOList.size() + " took: " + t.getElapsedTime());
					//componentContentVersionVOList = ContentControllerProxy.getController().getLatestContentVersionVOListByContentTypeId(new Integer[]{htmlTemplateContentType.getId(), pagePartTemplateContentType.getId()}, db);
					//logger.error("Getting componentContentVersionVOList 2 " + componentContentVersionVOList.size() + " took: " + t.getElapsedTime());
				}
				else
				{
					componentContentVersionVOList = ContentControllerProxy.getController().getLatestContentVersionVOListByContentTypeIdForSmallCollections(new Integer[]{htmlTemplateContentType.getId()}, db);
					logger.info("Getting componentContentVersionVOList 1 " + componentContentVersionVOList.size() + " took: " + t.getElapsedTime());
					//componentContentVersionVOList = ContentControllerProxy.getController().getLatestContentVersionVOListByContentTypeId(new Integer[]{htmlTemplateContentType.getId()}, db);
				}
				
				logger.warn("Getting componentContentVersionVOList " + componentContentVersionVOList.size() + " took: " + t.getElapsedTime());
				
				templatesAndPagePartMap = new HashMap<String,List<ContentVO>>();
				
			    for(ContentVersionVO incompleteContentVersionVO : componentContentVersionVOList) //The version is not populated with version value
			    {
		        	if(incompleteContentVersionVO != null)
		        	{
			        	ContentVO contentVO = ContentController.getContentController().getSmallContentVOWithId(incompleteContentVersionVO.getContentId(), db, null);
			        	
			        	String groupNameDefault = "Unknown";
						String descriptionDefault = "Unknown"; 
						ContentVersionVO fullContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(incompleteContentVersionVO.getId(), db);
						String groupNameAttribute = ContentVersionController.getContentVersionController().getAttributeValue(fullContentVersionVO, "GroupName", false);
			        	String descriptionAttribute = ContentVersionController.getContentVersionController().getAttributeValue(fullContentVersionVO, "Description", false);
			        	logger.info("GroupInfo ContentVersionId:" + incompleteContentVersionVO.getId() + " - " + groupNameAttribute);
						contentVO.getExtraProperties().put("GroupName", (groupNameAttribute == null ? groupNameDefault : groupNameAttribute));
						contentVO.getExtraProperties().put("Description", (descriptionAttribute == null ? descriptionDefault : descriptionAttribute));
	
			        	List<ContentVO> allComponents = templatesAndPagePartMap.get("all");
			        	if(allComponents == null)
			        	{
			        		allComponents = new ArrayList<ContentVO>();
			        		templatesAndPagePartMap.put("all", allComponents);
			        	}
			        	allComponents.add(contentVO);
			        	logger.info("Adding " + contentVO.getName() + " to group " + contentVO.getName());
			        	
			        	List<ContentVO> nameComponents = templatesAndPagePartMap.get(contentVO.getName());
			        	if(nameComponents == null)
			        	{
			        		nameComponents = new ArrayList<ContentVO>();
			        		templatesAndPagePartMap.put(contentVO.getName(), nameComponents);
			        	}
			        	nameComponents.add(contentVO);
	
			        	logger.info("Adding " + contentVO.getName() + " to group " + contentVO.getName());
			        	if(groupNameAttribute != null && !groupNameAttribute.equals(""))
			        	{
				        	String[] groupNames = groupNameAttribute.split(",");
					        for(String groupName : groupNames)
					        {
					        	List<ContentVO> groupComponents = templatesAndPagePartMap.get(groupName.trim());
					        	if(groupComponents == null)
					        	{
					        		groupComponents = new ArrayList<ContentVO>();
					        		templatesAndPagePartMap.put(groupName.trim(), groupComponents);
					        	}
					        	groupComponents.add(contentVO);
					        	logger.info("Adding " + contentVO.getName() + " to " + groupName.trim());
					        }
			        	}
		        	}
			    }
			    
			    logger.warn("Done indexing took in " + CmsPropertyHandler.getContextRootPath() + ":" + templatesAndPagePartMap.size() + " took: " + t.getElapsedTime());
				
				CacheController.cacheObject("componentContentsCache", cacheKey, templatesAndPagePartMap);
				CacheController.cacheObject("componentContentsCache", cacheKey + "_preCacheDone", new Boolean(true));
			}
			catch (Exception e) 
			{
				logger.error("Error precaching components:" + e.getMessage(), e);
			}
			finally
			{
				preCaching.set(false);			
			}
		}
	}

	public void reIndexComponentContentsDelayed(Integer contentId) throws Exception
	{
		componentIdsToRecache.add(contentId);
		class ReIndexComponentsTask implements Runnable 
		{
	        public void run() 
	        {
	        	try
	        	{
	        		Thread.sleep(1000);
		        	Timer t = new Timer();
		        	Set<Integer> localComponentIdsList = new HashSet<Integer>();
		        	localComponentIdsList.addAll(componentIdsToRecache);
		        	componentIdsToRecache.clear();
		        	for(Integer localComponentContentId : localComponentIdsList)
		        	{
		        		reIndexComponentContent(localComponentContentId);
		        	}
		        	long time = t.getElapsedTime();
		        	if(time > 100)
		        		logger.warn("ReIndexComponentsTask took a bit to long:" + time);
	        	}
	        	catch (Exception e) 
	        	{
					logger.warn("Failed to precache components: " + e.getMessage(), e);
				}
	        }
	    }
	    Thread thread = new Thread(new ReIndexComponentsTask());
	    thread.start();
	}

	private void reIndexComponentContent(Integer contentId) throws Exception
	{
		if(CmsPropertyHandler.getOperatingMode().equals("3"))
			return;
		
		String cacheKey = "allTemplatesAndPagePartMap";
		Map<String,List<ContentVO>> templatesAndPagePartMap = (Map<String,List<ContentVO>>)CacheController.getCachedObject("componentContentsCache", cacheKey);

		if(templatesAndPagePartMap != null)
		{
	    	synchronized (templatesAndPagePartMap) 
	    	{
				for(String key : templatesAndPagePartMap.keySet())
				{
					List<ContentVO> groupList = templatesAndPagePartMap.get(key);
					Iterator<ContentVO> groupListIterator = groupList.iterator();
					while(groupListIterator.hasNext())
					{
						ContentVO cvVO = groupListIterator.next();
						if(cvVO.getId().intValue() == contentId.intValue())
						{
							logger.info("Removing " + cvVO.getName() + " from group:" + key);
							groupListIterator.remove();
						}
					}
				}
			}
		}
		else
		{
			templatesAndPagePartMap = new HashMap<String,List<ContentVO>>();
			CacheController.cacheObject("componentContentsCache", cacheKey, templatesAndPagePartMap);
		}
		
		try
		{
	    	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
	    	if(contentVO != null)
	    	{
		    	LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
		    	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
		
		    	String groupNameDefault = "Unknown";
				String descriptionDefault = "Unknown";
		    	String groupNameAttribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "GroupName", false);
		    	String descriptionAttribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "Description", false);
	        	logger.info("GroupInfo ContentVersionId:" + contentVersionVO.getId() + " - " + groupNameAttribute);
				contentVO.getExtraProperties().put("GroupName", (groupNameAttribute == null ? groupNameDefault : groupNameAttribute));
				contentVO.getExtraProperties().put("Description", (descriptionAttribute == null ? descriptionDefault : descriptionAttribute));
			
		    	List<ContentVO> allComponents = templatesAndPagePartMap.get("all");
		    	if(allComponents == null)
		    	{
		    		allComponents = new ArrayList<ContentVO>();
		    		templatesAndPagePartMap.put("all", allComponents);
		    	}
		    	allComponents.add(contentVO);
		    	logger.info("Adding " + contentVO.getName() + " to group " + contentVO.getName());
		    	
		    	List<ContentVO> nameComponents = templatesAndPagePartMap.get(contentVO.getName());
		    	if(nameComponents == null)
		    	{
		    		nameComponents = new ArrayList<ContentVO>();
		    		templatesAndPagePartMap.put(contentVO.getName(), nameComponents);
		    	}
		    	nameComponents.add(contentVO);
		
		    	logger.info("Adding " + contentVO.getName() + " to group " + contentVO.getName());
		    	if(groupNameAttribute != null && !groupNameAttribute.equals(""))
		    	{
		        	String[] groupNames = groupNameAttribute.split(",");
			        for(String groupName : groupNames)
			        {
			        	logger.info("groupName:" + groupName);
			        	List<ContentVO> groupComponents = templatesAndPagePartMap.get(groupName.trim());
			        	if(groupComponents == null)
			        	{
			        		groupComponents = new ArrayList<ContentVO>();
			        		templatesAndPagePartMap.put(groupName.trim(), groupComponents);
			        	}
			        	groupComponents.add(contentVO);
			        	logger.info("Adding " + contentVO.getName() + " to " + groupName.trim());
			        }
		    	}
	    	}
		}
		catch (Exception e) 
		{
			logger.warn("Component was deleted..");
		}
	}
	
	/**
	 * This method returns a sorted list of components.
	 * @param sortAttribute
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List getComponentVOList(String sortAttribute, String direction, String[] allowedComponentNames, String[] disallowedComponentNames, String[] allowedComponentGroupNames, InfoGluePrincipal principal) throws SystemException, Bug, Exception
	{
		List componentVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			componentVOList = getComponentVOList(sortAttribute, direction, allowedComponentNames, disallowedComponentNames, allowedComponentGroupNames, db, principal);
			    
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
		}		
		
		return componentVOList;
	}

	/**
	 * This method returns a sorted list of components within a transaction.
	 * @param sortAttribute
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public List getComponentVOList(String sortAttribute, String direction, String[] allowedComponentNames, String[] disallowedComponentNames, String[] allowedComponentGroupNames, Database db, InfoGluePrincipal principal) throws SystemException, Bug, Exception
	{
		if(principal == null)
			return null;
			
	    String allowedComponentNamesString = "";
	    if(allowedComponentNames != null)
	    {
	        for(int i=0; i<allowedComponentNames.length; i++)
	            allowedComponentNamesString = allowedComponentNames[i] + ":";
	    }

	    String disallowedComponentNamesString = "";
	    if(disallowedComponentNames != null)
	    {
	        for(int i=0; i<disallowedComponentNames.length; i++)
	        	disallowedComponentNamesString = disallowedComponentNames[i] + ":";
	    }

	    String allowedComponentGroupNamesString = "";
	    if(allowedComponentGroupNames != null)
	    {
	        for(int i=0; i<allowedComponentGroupNames.length; i++)
	        	allowedComponentGroupNamesString = allowedComponentGroupNames[i] + ":";
	    }
	    
	    String componentsKey = "components_" + sortAttribute + "_" + direction + "_" + allowedComponentNamesString + "_" + disallowedComponentNamesString + "_" + allowedComponentGroupNamesString + "_" + principal.getName();
	    List components = (List)CacheController.getCachedObject("componentContentsCache", componentsKey);
		if(components != null)
		{
			logger.info("There was cached components:" + components.size());
		}
		else
		{
			Timer t = new Timer();
		    components = getComponents(allowedComponentNames, disallowedComponentNames, allowedComponentGroupNames, principal, db);
		    if(logger.isInfoEnabled())
		    	t.printElapsedTime("getComponents");
		    /*
		    Iterator componentsIterator = components.iterator();
			while(componentsIterator.hasNext())
			{
			    ContentVO contentVO = (ContentVO)componentsIterator.next();
			    
			    LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), db); //.getMasterLanguage(db, contentVO.getRepositoryId());
				//logger.info("masterLanguage:" + masterLanguage);
			    if(masterLanguage != null)
			    {
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguage.getId(), db);
					
					String groupName = "Unknown";
					String description = "Unknown";
					
					if(contentVersionVO != null)
					{
					    groupName = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "GroupName", false);
					    description = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "Description", false);
					}
		
					contentVO.getExtraProperties().put("GroupName", groupName);
					contentVO.getExtraProperties().put("Description", description);
			    }
			    else
			    {
			    	componentsIterator.remove();
			    }
			}
		    t.printElapsedTime("getComponents 2");
			*/

			//CacheController.cacheObject("componentContentsCache", componentsKey, components);
		}
		
		ContentComparator comparator = new ContentComparator(sortAttribute, direction, null);
		Collections.sort(components, comparator);
		
		return components;
	}

	
	/**
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
	 */
	
	public List getComponents(String[] allowedComponentNames, String[] disallowedComponentNames, String[] allowedComponentGroups/*, String[] disallowedComponentGroups*/, InfoGluePrincipal principal) throws Exception
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");
		
		List argumentList = new ArrayList();
		HashMap argument = new HashMap();
		argument.put("contentTypeDefinitionName", "HTMLTemplate");
		argumentList.add(argument);
		HashMap argument2 = new HashMap();
		argument2.put("contentTypeDefinitionName", "PagePartTemplate");
		argumentList.add(argument2);
		arguments.put("arguments", argumentList);
		
		List results = ContentControllerProxy.getController().getACContentVOList(principal, arguments);

	    Iterator resultsIterator = results.iterator();
	    while(resultsIterator.hasNext())
	    {
	        ContentVO contentVO = (ContentVO)resultsIterator.next();

			boolean isPartOfAllowedComponentNames = false;
			if(allowedComponentNames != null && allowedComponentNames.length > 0)
			{
				for(int i=0; i<allowedComponentNames.length; i++)
		        {
		            if(contentVO.getName().equals(allowedComponentNames[i]))
		            	isPartOfAllowedComponentNames = true;
		        }
			}
			
			boolean isPartOfAllowedComponentGroupNames = false;
			if(allowedComponentGroups != null && allowedComponentGroups.length > 0)
			{
				LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());		

	        	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(contentVO.getContentId(), masterLanguageVO.getId());
	        	String groupName = null;
	        	if(contentVersionVO != null)
	        		groupName = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "GroupName", false);

		        for(int i=0; i<allowedComponentGroups.length; i++)
		        {
		        	String allowedComponentGroup = allowedComponentGroups[i];
		        	if(groupName != null && groupName.indexOf(allowedComponentGroup) > -1)
		        		isPartOfAllowedComponentGroupNames = true;
		        }
			}

			boolean isPartOfDisallowedComponentNames = false;
			if(disallowedComponentNames != null && disallowedComponentNames.length > 0)
			{
		        for(int i=0; i<disallowedComponentNames.length; i++)
		        {
		            if(contentVO.getName().equals(disallowedComponentNames[i]))
		            	isPartOfDisallowedComponentNames = true;
		        }
			}

			if(disallowedComponentNames != null && disallowedComponentNames.length > 0 && isPartOfDisallowedComponentNames)
			{
				resultsIterator.remove();
			}
			else if((allowedComponentNames == null || allowedComponentNames.length == 0) && (allowedComponentGroups == null || allowedComponentGroups.length == 0))
			{
				//logger.info("Was ok as no restrictions was defined");
			}
			else if(isPartOfAllowedComponentNames)
			{
				//logger.info("Was ok as it was part of allowedComponentNames");
			}
			else if(isPartOfAllowedComponentGroupNames)
			{
				//logger.info("Was ok as it was part of allowedComponentGroupNames");
	    	}
			else
			{
				//logger.info("Removing from results:" + contentVO.getName());
				resultsIterator.remove();				
			}
		}
		
		return results;	
	}


	public boolean getIsPagePartTemplate(ContentVO contentVO)
	{
		try
		{
			ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
			if(ctdVO != null && ctdVO.getName().equalsIgnoreCase("PagePartTemplate"))
				return true;
			else 
				return false;			
		}
		catch (Exception e) 
		{
			logger.warn("Error looking up content type:" + e.getMessage());
			return false;
		}
	}
	
	/**
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
	 */
	
	public List getComponents(String[] allowedComponentNames, String[] disallowedComponentNames, String[] allowedComponentGroups/*, String[] disallowedComponentGroups*/, InfoGluePrincipal principal, Database db) throws Exception
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");
		//arguments.put("method", "selectListOnContentSearch");
		//arguments.put("allowedComponentNames", allowedComponentNames);
		//arguments.put("allowedComponentGroups", allowedComponentGroups);
		
		List argumentList = new ArrayList();
		HashMap argument = new HashMap();
		argument.put("contentTypeDefinitionName", "HTMLTemplate");
		argumentList.add(argument);
		HashMap argument2 = new HashMap();
		argument2.put("contentTypeDefinitionName", "PagePartTemplate");
		argumentList.add(argument2);
		arguments.put("arguments", argumentList);
		
		preCacheComponents(-1, db);
		
		String cacheKey = "allTemplatesAndPagePartMap";
		Boolean templatesAndPagePartMapPreCached = (Boolean)CacheController.getCachedObject("componentContentsCache", cacheKey + "_preCacheDone");
		
		logger.info("templatesAndPagePartMapPreCached: " + templatesAndPagePartMapPreCached);
		if(templatesAndPagePartMapPreCached != null && templatesAndPagePartMapPreCached)
		{
			Map<String,List<ContentVO>> templatesAndPagePartMap = (Map<String,List<ContentVO>>)CacheController.getCachedObject("componentContentsCache", cacheKey);
			logger.info("templatesAndPagePartMap: " + templatesAndPagePartMap.size());
			
			Timer t = new Timer();
			logger.info("Returning from cache....:" + allowedComponentNames + ":" + allowedComponentGroups);
			List<ContentVO> results = new ArrayList<ContentVO>();
			
			if(allowedComponentNames == null && allowedComponentGroups == null)
			{
				List<ContentVO> allComponents = templatesAndPagePartMap.get("all");
				if(allComponents != null)
					results.addAll(allComponents);
			}
			else
			{
				if(allowedComponentNames != null && allowedComponentNames.length > 0)
				{
					for(int i=0; i<allowedComponentNames.length; i++)
			        {
						logger.info("Adding all matching:" + allowedComponentNames[i]);
						List<ContentVO> contents = templatesAndPagePartMap.get(allowedComponentNames[i]);
						if(contents != null)
							results.addAll(contents);
			        }
				}
				
				List<List<ContentVO>> unionList = new ArrayList<List<ContentVO>>();
				if(allowedComponentGroups != null && allowedComponentGroups.length > 0)
				{
			        for(int i=0; i<allowedComponentGroups.length; i++)
			        {
			        	String allowedComponentGroup = allowedComponentGroups[i];
			        	logger.info("allowedComponentGroup:" + allowedComponentGroup);
			        	
			        	if(allowedComponentGroup.trim().startsWith("&"))
			        	{
			        		String actualAllowedComponentGroup = allowedComponentGroup.substring(1);
			        		logger.info("actualAllowedComponentGroup:" + actualAllowedComponentGroup);
		        			List<ContentVO> contentsToIncludeIfInBoth = templatesAndPagePartMap.get(actualAllowedComponentGroup);
		        			unionList.add(contentsToIncludeIfInBoth);
			        	}
			        	else
			        	{
			        		logger.info("Adding all matching:" + allowedComponentGroup);
				        	List<ContentVO> contents = templatesAndPagePartMap.get(allowedComponentGroup);
							if(contents != null)
								results.addAll(contents);
			        	}
			        }
				}
				
				if(unionList != null && unionList.size() > 0)
				{
					Iterator<ContentVO> resultsIterator = results.iterator();
					while(resultsIterator.hasNext())
					{
						ContentVO originalListContent = resultsIterator.next();
						boolean isInAllUnions = true; 
						for(List<ContentVO> contentList : unionList)
						{
							if(contentList != null)
							{
								boolean isInUnions = false; 
								for(ContentVO unionContent : contentList)
								{
									if(unionContent.getName().equalsIgnoreCase(originalListContent.getName()))
										isInUnions = true; 
								}
								
								if(!isInUnions)
									isInAllUnions = false;
							}
						}
						
						if(!isInAllUnions)
							resultsIterator.remove();
					}
				}
			}
			
			if(disallowedComponentNames != null && disallowedComponentNames.length > 0)
			{
		        for(int i=0; i<disallowedComponentNames.length; i++)
		        {
		            String disallowedComponentName = disallowedComponentNames[i];
		            for(ContentVO contentVO : results)
		            {
		            	if(contentVO.getName().equals(disallowedComponentName))
		            	{
		            		logger.info("Removing disallowed:" + contentVO.getName());
		            		results.remove(contentVO);
		            		break;
		            	}
		            }
		        }
			}
			if(logger.isInfoEnabled())
				t.printElapsedTime("Collecting from groups took");
			
			List<ContentVO> authorizedComponents = new ArrayList<ContentVO>();
			for(ContentVO contentVO : results)
			{
				boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, principal, "Component.Select", "" + contentVO.getId(), false);
				if(hasAccess)
					authorizedComponents.add(contentVO);
			}
			if(logger.isInfoEnabled())
				t.printElapsedTime("Checking access rights for components took");
			return authorizedComponents;
		}
		else
		{
			List<ContentVO> results = ContentControllerProxy.getController().getACContentVOList(principal, arguments, "Component.Select", db);
	
		    Iterator resultsIterator = results.iterator();
		    while(resultsIterator.hasNext())
		    {
		        ContentVO contentVO = (ContentVO)resultsIterator.next();
	
				boolean isPartOfAllowedComponentNames = false;
				if(allowedComponentNames != null && allowedComponentNames.length > 0)
				{
					for(int i=0; i<allowedComponentNames.length; i++)
			        {
			            if(contentVO.getName().equals(allowedComponentNames[i]))
			            	isPartOfAllowedComponentNames = true;
			        }
				}
				
				boolean isPartOfAllowedComponentGroupNames = false;
				if(allowedComponentGroups != null && allowedComponentGroups.length > 0 && contentVO.getRepositoryId() != null)
				{
					LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), db);
					if(masterLanguageVO != null)
					{
						//logger.info("masterLanguageVO for " + contentVO.getRepositoryId() + " is " + masterLanguageVO);
			        	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestContentVersionVO(contentVO.getContentId(), masterLanguageVO.getId(), db);
			        	String groupName = null;
			        	if(contentVersionVO != null)
			        		groupName = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, "GroupName", false);
		
				        for(int i=0; i<allowedComponentGroups.length; i++)
				        {
				        	String allowedComponentGroup = allowedComponentGroups[i];
				        	if(groupName != null && groupName.indexOf(allowedComponentGroup) > -1)
				        		isPartOfAllowedComponentGroupNames = true;
				        }
					}
				}
	
				boolean isPartOfDisallowedComponentNames = false;
				if(disallowedComponentNames != null && disallowedComponentNames.length > 0)
				{
			        for(int i=0; i<disallowedComponentNames.length; i++)
			        {
			            if(contentVO.getName().equals(disallowedComponentNames[i]))
			            	isPartOfDisallowedComponentNames = true;
			        }
				}
	
				if(disallowedComponentNames != null && disallowedComponentNames.length > 0 && isPartOfDisallowedComponentNames)
				{
					//logger.info("Was not ok as it was part of disallowedComponentNames");
					resultsIterator.remove();
				}
				else if((allowedComponentNames == null || allowedComponentNames.length == 0) && (allowedComponentGroups == null || allowedComponentGroups.length == 0))
				{
					//logger.info("Was ok as no restrictions was defined");
				}
				else if(isPartOfAllowedComponentNames)
				{
					//logger.info("Was ok as it was part of allowedComponentNames");
				}
				else if(isPartOfAllowedComponentGroupNames)
				{
					//logger.info("Was ok as it was part of allowedComponentGroupNames");
				}
				else
				{
					//logger.info("Removing from results:" + contentVO.getName());
					resultsIterator.remove();				
				}
		    }
			return results;	
		}
	}

	public void checkAndAutoCreateContents(Integer siteNodeId, Integer languageId, Integer masterLanguageId, String assetKey, Integer newComponentId, Document document, Integer componentContentId, InfoGluePrincipal principal) throws Exception, SystemException, Bug, ConstraintException
	{
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
        	checkAndAutoCreateContents(db, siteNodeId, languageId, masterLanguageId, assetKey, newComponentId, document, componentContentId, principal);
        	
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}
	
	public void checkAndAutoCreateContents(Database db, Integer siteNodeId, Integer languageId, Integer masterLanguageId, String assetKey, Integer newComponentId, Document document, Integer componentContentId, InfoGluePrincipal principal) throws Exception, SystemException, Bug, ConstraintException
	{
		List componentPropertyDefinitions = ComponentPropertyDefinitionController.getController().getComponentPropertyDefinitions(db, componentContentId, masterLanguageId);
		Iterator componentPropertyDefinitionsIterator = componentPropertyDefinitions.iterator();
		while(componentPropertyDefinitionsIterator.hasNext())
		{
			ComponentPropertyDefinition componentPropertyDefinition = (ComponentPropertyDefinition)componentPropertyDefinitionsIterator.next();
			logger.info("componentPropertyDefinition:" + componentPropertyDefinition);
			if(componentPropertyDefinition.getAutoCreateContent())
			{
				logger.info("componentPropertyDefinition vill ha en auto create:" + componentPropertyDefinition);
				String method = componentPropertyDefinition.getAutoCreateContentMethod();
				String path = componentPropertyDefinition.getAutoCreateContentPath();
				String allowedContentTypeNames = componentPropertyDefinition.getAllowedContentTypeNames();
				String allowedContentTypeName = allowedContentTypeNames.split(",")[0];
				ContentTypeDefinitionVO createContentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(allowedContentTypeName, db);
				logger.info("method:" + method);
				logger.info("path:" + path);
				logger.info("createContentTypeDefinitionVO:" + createContentTypeDefinitionVO);
				
				if(createContentTypeDefinitionVO == null || method == null)
					continue;
					
				if(path.indexOf("/") == 0)
					path = path.substring(1);
				
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
				ContentVO parentContentVO = ContentController.getContentController().getRootContent(db, siteNodeVO.getRepositoryId(), principal.getName(), true).getValueObject();
				if(method.equals("siteStructure"))
				{
					String siteNodePath = SiteNodeController.getController().getSiteNodePath(siteNodeVO.getId(), db);
					logger.info("siteNodePath:" + siteNodePath);
					parentContentVO = ContentController.getContentController().getContentVOWithPath(siteNodeVO.getRepositoryId(), path + siteNodePath, true, principal, db);
				}
				else if(method.equals("fixedPath"))
				{
					parentContentVO = ContentController.getContentController().getContentVOWithPath(siteNodeVO.getRepositoryId(), path, true, principal, db);
				}
				
				ContentVO autoContentVO = new ContentVO();
				autoContentVO.setName(siteNodeVO.getName() + " - " + componentPropertyDefinition.getName());
				autoContentVO.setCreatorName(principal.getName());
				autoContentVO.setIsBranch(false);

				StringBuffer sb = new StringBuffer();
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes>");
				List contentTypeAttributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(createContentTypeDefinitionVO, true);
				Iterator contentTypeAttributesIterator = contentTypeAttributes.iterator();
				while(contentTypeAttributesIterator.hasNext())
				{
					ContentTypeAttribute contentTypeAttribute = (ContentTypeAttribute)contentTypeAttributesIterator.next();
					String initialValue = null;
					if(contentTypeAttribute.getContentTypeAttribute("initialData") != null && contentTypeAttribute.getContentTypeAttribute("initialData").getContentTypeAttributeParameterValue() != null)
					{
						initialValue = contentTypeAttribute.getContentTypeAttribute("initialData").getContentTypeAttributeParameterValue().getValue("label");
					}
					
					boolean skip = false;
					if(contentTypeAttribute.getContentTypeAttribute("enableRelationEditor") != null && contentTypeAttribute.getContentTypeAttribute("enableRelationEditor").getContentTypeAttributeParameterValue() != null && contentTypeAttribute.getContentTypeAttribute("enableRelationEditor").getContentTypeAttributeParameterValue().getValue("label").equals("true"))
					{
						skip = true;
					}
					if(contentTypeAttribute.getContentTypeAttribute("enableContentRelationEditor") != null && contentTypeAttribute.getContentTypeAttribute("enableContentRelationEditor").getContentTypeAttributeParameterValue() != null && contentTypeAttribute.getContentTypeAttribute("enableContentRelationEditor").getContentTypeAttributeParameterValue().getValue("label").equals("true"))
					{
						skip = true;
					}
					if(contentTypeAttribute.getContentTypeAttribute("enableStructureRelationEditor") != null && contentTypeAttribute.getContentTypeAttribute("enableStructureRelationEditor").getContentTypeAttributeParameterValue() != null && contentTypeAttribute.getContentTypeAttribute("enableStructureRelationEditor").getContentTypeAttributeParameterValue().getValue("label").equals("true"))
					{
						skip = true;
					}
					if(!skip)
					{
						if(initialValue == null || initialValue.trim().equals(""))
							initialValue = "State " + contentTypeAttribute.getName();
						sb.append("<" + contentTypeAttribute.getName() + "><![CDATA[" + initialValue + "]]></" + contentTypeAttribute.getName() + ">");
					}
					else
						logger.info("Skipping:" + contentTypeAttribute.getName());
				}
				sb.append("</attributes></article>");
				
				String defaultValue = sb.toString(); 

				ContentVersionVO autoContentVersionVO = new ContentVersionVO();
				autoContentVersionVO.setVersionComment("Automatically created");
				autoContentVersionVO.setVersionModifier(principal.getName());
				autoContentVersionVO.setVersionValue(defaultValue);
				
				autoContentVO = ContentController.getContentController().create(db, parentContentVO.getId(), createContentTypeDefinitionVO.getId(), siteNodeVO.getRepositoryId(), autoContentVO).getValueObject();
				ContentVersionController.getContentVersionController().create(autoContentVO.getId(), languageId, autoContentVersionVO, null, db);
				
				Locale locale = LanguageController.getController().getLocaleWithId(languageId, db);
				LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(siteNodeVO.getRepositoryId(), db);		

				String entity = "Content";
				Integer entityId  = new Integer(autoContentVO.getId());
				String propertyName = "" + componentPropertyDefinition.getName();
				
				ComponentController.getController().addComponentPropertyBinding(document, locale, siteNodeId, languageId, masterLanguageVO.getId(), entity, entityId, propertyName, newComponentId, path, assetKey, principal);
			}
		}
	}

	/**
	 * This method shows the user a list of Components(HTML Templates). 
	 */
    
	public void addComponentPropertyBinding(Document document,
											Locale locale,
											Integer siteNodeId, 
											Integer languageId, 
											Integer masterLanguageId,
											String entity,
											Integer entityId,
											String propertyName,
											Integer componentId,
											String path,
											String assetKey,
											InfoGluePrincipal principal) throws Exception
	{
		//logger.info("************************************************************");
		//logger.info("* doAddComponentPropertyBinding                            *");
		//logger.info("************************************************************");
		//logger.info("siteNodeId:" + this.siteNodeId);
		//logger.info("languageId:" + this.languageId);
		//logger.info("contentId:" + this.contentId);
		//logger.info("componentId:" + this.componentId);
		//logger.info("slotId:" + this.slotId);
		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
		//logger.info("assetKey:" + assetKey);
				
		String componentPropertyXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + propertyName + "']";
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
		if(anl.getLength() == 0)
		{
			String componentXPath = "//component[@id=" + componentId + "]/properties";
			//logger.info("componentXPath:" + componentXPath);
			NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
			if(componentNodeList.getLength() > 0)
			{
				Element componentProperties = (Element)componentNodeList.item(0);
				if(entity.equalsIgnoreCase("SiteNode"))
				    addPropertyElement(componentProperties, propertyName, path, "siteNodeBinding", locale);
				else
				    addPropertyElement(componentProperties, propertyName, path, "contentBinding", locale);
				
				anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
			}
		}
		
		if(anl.getLength() > 0)
		{
			Element component = (Element)anl.item(0);
			component.setAttribute("path", path);
			//component.setAttribute("path_" + locale.getLanguage(), path);
			NamedNodeMap attributes = component.getAttributes();
			logger.debug("NumberOfAttributes:" + attributes.getLength() + ":" + attributes);
						
			List removableAttributes = new ArrayList();
			for(int i=0; i<attributes.getLength(); i++)
			{
				Node node = attributes.item(i);
				logger.debug("Node:" + node.getNodeName());
				if(node.getNodeName().startsWith("path_"))
				{
					removableAttributes.add("" + node.getNodeName());
				}
			}
			
			Iterator removableAttributesIterator = removableAttributes.iterator();
			while(removableAttributesIterator.hasNext())
			{
				String attributeName = (String)removableAttributesIterator.next();
				logger.debug("Removing node:" + attributeName);
				component.removeAttribute(attributeName);
			}
			
			NodeList children = component.getChildNodes();
			for(int i=0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				component.removeChild(node);
			}
			
			addBindingElement(component, entity, entityId, assetKey);
		}
	}

	/**
	 * This method fetches the template-string.
	 */
    
	private String getPageComponentsString(Integer siteNodeId, Integer languageId, InfoGluePrincipal principal) throws SystemException, Exception
	{
		String template = null;
    	
		try
		{
			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, new Integer(-1)).getBoundContent(principal, siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());

			if(contentVO == null)
				throw new SystemException("There was no template bound to this page which makes it impossible to render.");	
			
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
			if(contentVersionVO == null)
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
				LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(siteNodeVO.getRepositoryId());
				contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguage.getLanguageId());
			}
			
			template = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO.getId(), "ComponentStructure", false);
			
			if(template == null)
				throw new SystemException("There was no template bound to this page which makes it impossible to render.");	
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}

		return template;
	}

	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private Element addPropertyElement(Element parent, String name, String path, String type, Locale locale)
	{
		Element element = parent.getOwnerDocument().createElement("property");
		element.setAttribute("name", name);
		
		if(type.equalsIgnoreCase("siteNodeBinding") || type.equalsIgnoreCase("contentBinding"))
		{
			element.setAttribute("path", path);
			element.setAttribute("path_" + locale.getLanguage(), path);
		}
		else
		{
			element.setAttribute("path_" + locale.getLanguage(), path);
		}
		
		element.setAttribute("type", type);
		parent.appendChild(element);
		return element;
	}

	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private Element addBindingElement(Element parent, String entity, Integer entityId, String assetKey)
	{
		Element element = parent.getOwnerDocument().createElement("binding");
		element.setAttribute("entityId", entityId.toString());
		element.setAttribute("entity", entity);
		if(assetKey != null && !assetKey.equals(""))
			element.setAttribute("assetKey", assetKey);
		
		parent.appendChild(element);
		
		return element;
	}

    /* (non-Javadoc)
     * @see org.infoglue.cms.controllers.kernel.impl.simple.BaseController#getNewVO()
     */
    public BaseEntityVO getNewVO()
    {
        return null;
    }
	
	
}
