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

package org.infoglue.cms.webservices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowDefinitionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.workflow.WorkflowDefinitionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.webservices.DynamicWebserviceSerializer;


/**
 * This class is responsible for all deployment actions.
 * 
 * @author Mattias Bogeblad
 */

public class RemoteDeploymentServiceImpl extends RemoteInfoGlueService
{
    private final static Logger logger = Logger.getLogger(RemoteDeploymentServiceImpl.class.getName());

	/**
	 * The principal executing the workflow.
	 */
	private InfoGluePrincipal principal;

	private static ContentTypeDefinitionController contentTypeDefinitionController = ContentTypeDefinitionController.getController();
	private static WorkflowDefinitionController workflowDefinitionController = WorkflowDefinitionController.getController();
	private static CategoryController categoryController = CategoryController.getController();
    private static ContentControllerProxy contentControllerProxy = ContentControllerProxy.getController();
    private static RepositoryController repositoryController = RepositoryController.getController();
    //private static ContentVersionControllerProxy contentVersionControllerProxy = ContentVersionControllerProxy.getController();
    

	/**
     * Gets all content type definitions from the cms.
     */
    
    public List<ContentTypeDefinitionVO> getContentTypeDefinitions(final String principalName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }
        
        if(logger.isInfoEnabled())
        {
	        logger.info("******************************************************");
	        logger.info("* Getting content type definition through webservice *");
	        logger.info("******************************************************");
        }
	        
        List<ContentTypeDefinitionVO> contentTypeDefinitionVOList = new ArrayList<ContentTypeDefinitionVO>();
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
	        
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
	        
            contentTypeDefinitionVOList = contentTypeDefinitionController.getContentTypeDefinitionVOList();
        }
        catch(Throwable t)
        {
            logger.error("En error occurred when we tried to get the contentVersionVO:" + t.getMessage(), t);
        }
        
        return contentTypeDefinitionVOList;
    }

	/**
     * Gets all content type definitions from the cms.
     */
    
    public Boolean updateContentTypeDefinitions(final String principalName, final Object[] input) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }
        
    	Boolean status = new Boolean(true);

        if(logger.isInfoEnabled())
        {
	        logger.info("*******************************************************");
	        logger.info("* Updating content type definition through webservice *");
	        logger.info("*******************************************************");
        }
	        
        try
        {
        	final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
        	Map arguments = (Map)serializer.deserialize(input);
        	
            logger.info("arguments:" + arguments);
                    	
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
			
			List remoteContentTypeDefinitionVOList = (List)arguments.get("contentTypeDefinitionVOList");
			List missingContentTypeNameArray = (List)arguments.get("missingContentTypeNameArray");
			List deviatingContentTypeNameArray = (List)arguments.get("deviatingContentTypeNameArray");
			
			logger.info("remoteContentTypeDefinitionVOList:" + remoteContentTypeDefinitionVOList);
			logger.info("missingContentTypeNameArray:" + missingContentTypeNameArray);
			logger.info("deviatingContentTypeNameArray:" + deviatingContentTypeNameArray);
			
			if(missingContentTypeNameArray != null)
	    	{
				Iterator missingContentTypeNameArrayIterator = missingContentTypeNameArray.iterator();
		    	while(missingContentTypeNameArrayIterator.hasNext())
		    	{
		    		String missingContentTypeName = (String)missingContentTypeNameArrayIterator.next();
		    		logger.info("Updating missingContentTypeName:" + missingContentTypeName);
		
		        	Iterator remoteContentTypeDefinitionVOListIterator = remoteContentTypeDefinitionVOList.iterator();
		        	while(remoteContentTypeDefinitionVOListIterator.hasNext())
		        	{
		        		ContentTypeDefinitionVO remoteContentTypeDefinitionVO = (ContentTypeDefinitionVO)remoteContentTypeDefinitionVOListIterator.next();
		        		//logger.info("remoteContentTypeDefinitionVO:" + remoteContentTypeDefinitionVO.getName());
		        		if(remoteContentTypeDefinitionVO.getName().equals(missingContentTypeName))
		        		{
		        			logger.info("Creating remoteContentTypeDefinitionVO:" + remoteContentTypeDefinitionVO.getName());
		        			ContentTypeDefinitionController.getController().create(remoteContentTypeDefinitionVO);
		        		}
		        	}
		    	}
	    	}

	    	if(deviatingContentTypeNameArray != null)
	    	{
	    		Iterator deviatingContentTypeNameArrayIterator = deviatingContentTypeNameArray.iterator();
		    	while(deviatingContentTypeNameArrayIterator.hasNext())
		    	{
		    		String deviatingContentTypeName = (String)deviatingContentTypeNameArrayIterator.next();
		    		//logger.info("Updating deviatingContentTypeName:" + deviatingContentTypeName);
		    		Map deviationArguments = (Map)arguments.get("deviationArguments_" + deviatingContentTypeName);
					logger.info("deviationArguments:" + deviationArguments);

		        	Iterator remoteContentTypeDefinitionVOListIterator = remoteContentTypeDefinitionVOList.iterator();
		        	while(remoteContentTypeDefinitionVOListIterator.hasNext())
		        	{
		        		ContentTypeDefinitionVO remoteContentTypeDefinitionVO = (ContentTypeDefinitionVO)remoteContentTypeDefinitionVOListIterator.next();
		        		//logger.info("remoteContentTypeDefinitionVO:" + remoteContentTypeDefinitionVO.getName());
		        		if(remoteContentTypeDefinitionVO.getName().equals(deviatingContentTypeName))
		        		{
		        			ContentTypeDefinitionVO localContentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(remoteContentTypeDefinitionVO.getName());
		        			String newSchemaValue = localContentTypeDefinitionVO.getSchemaValue();
		        			
		        			//Enkelt - vid push tillåter vi bara push av hela innehållstypen
		        			newSchemaValue = remoteContentTypeDefinitionVO.getSchemaValue();
		        			/*
							logger.info("deviationArguments:" + deviationArguments);
							List attributes 	= (List)deviationArguments.get("attributes");
							List categories 	= (List)deviationArguments.get("categories");
							List assets 		= (List)deviationArguments.get("assets");

		        	    	if(attributes != null)
		        	    	{
		        	    		Iterator attributesIterator = attributes.iterator();
		        	    		while(attributesIterator.hasNext())
		        	    		{
			        	    		String attributeName = (String)attributesIterator.next();
			        	    		logger.info("  * Updating attributeName:" + attributeName);
			        			
				        			newSchemaValue = contentTypeDefinitionController.copyAttribute(remoteContentTypeDefinitionVO.getSchemaValue(), newSchemaValue, attributeName);
				        		}
		        	    	}	        			

		        	    	if(categories != null)
		        	    	{
		        	    		Iterator categoryIterator = categories.iterator();
		        	    		while(categoryIterator.hasNext())
		        	    		{
			        	    		String categoryName = (String)categoryIterator.next();
			        	    		logger.info("  * Updating categoryName:" + categoryName);
			        			
				        			newSchemaValue = contentTypeDefinitionController.copyCategory(remoteContentTypeDefinitionVO.getSchemaValue(), newSchemaValue, categoryName);
				        		}
		        	    	}	

		        	    	if(assets != null)
		        	    	{
		        	    		Iterator assetsIterator = assets.iterator();
		        	    		while(assetsIterator.hasNext())
		        	    		{
			        	    		String assetKey = (String)assetsIterator.next();
			        	    		logger.info("  * Updating assetKey:" + assetKey);
			        			
				        			newSchemaValue = contentTypeDefinitionController.copyAssetKey(remoteContentTypeDefinitionVO.getSchemaValue(), newSchemaValue, assetKey);
				        		}
		        	    	}
		        	    	*/
		        			
		        			localContentTypeDefinitionVO.setSchemaValue(newSchemaValue);
		        			logger.info("Updating localContentTypeDefinitionVO:" + localContentTypeDefinitionVO.getName());
				
		        			ContentTypeDefinitionController.getController().update(localContentTypeDefinitionVO);
		        		}
		        	}
		    	}
	    	}
        }
        catch(Throwable t)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to get the contentVersionVO:" + t.getMessage(), t);
        }
        
        updateCaches();

        return status;
    }

	/**
     * Gets all content type definitions from the cms.
     */
    
    public Boolean updateCategories(final String principalName, final Object[] input) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }
        
    	Boolean status = new Boolean(true);

        if(logger.isInfoEnabled())
        {
	        logger.info("*******************************************************");
	        logger.info("*       Updating categories through webservice        *");
	        logger.info("*******************************************************");
        }
	        
        try
        {
        	logger.info("input:" + input);
        	
        	final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
        	Map arguments = (Map)serializer.deserialize(input);
        	
            logger.info("arguments:" + arguments);
                    	
			List remoteCategoryVOList = (List)arguments.get("categoryVOList");
			Map requestMap = (Map)arguments.get("requestMap");
        	
            logger.info("remoteCategoryVOList:" + remoteCategoryVOList);
                    	
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
						
		    List<CategoryVO> allLocalCategories = CategoryController.getController().findAllActiveCategories();
		    //logger.info("allLocalCategories:" + allLocalCategories.size());
	    	
		    Map handledRemoteCategoryPaths = new HashMap();
		    
		    categoryController.compareAndCompleteCategoryLists(remoteCategoryVOList, allLocalCategories, null, handledRemoteCategoryPaths, requestMap);
        }
        catch(Throwable t)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to update categories:" + t.getMessage(), t);
        }
        
        updateCaches();

        return status;
    }

	/**
     * Gets all content type definitions from the cms.
     */
    
    public Boolean updateWorkflows(final String principalName, final Object[] input) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }
        
    	Boolean status = new Boolean(true);

        if(logger.isInfoEnabled())
        {
	        logger.info("*******************************************************");
	        logger.info("*       Updating categories through webservice        *");
	        logger.info("*******************************************************");
        }
	        
        try
        {
        	logger.info("input:" + input);
        	
        	final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
        	Map arguments = (Map)serializer.deserialize(input);
        	
            logger.info("arguments:" + arguments);
                    	
			List remoteWorkflowDefinitionVOList = (List)arguments.get("workflowDefinitionVOList");
			Map requestMap = (Map)arguments.get("requestMap");
        	
            logger.info("remoteWorkflowDefinitionVOList:" + remoteWorkflowDefinitionVOList);
                    	
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
						
		    Iterator remoteWorkflowDefinitionVOListIterator = remoteWorkflowDefinitionVOList.iterator();
		    while(remoteWorkflowDefinitionVOListIterator.hasNext())
		    {
		    	WorkflowDefinitionVO remoteWorkflowDefinitionVO = (WorkflowDefinitionVO)remoteWorkflowDefinitionVOListIterator.next();
		    	
		    	WorkflowDefinitionVO localWorkflowDefinitionVO = WorkflowDefinitionController.getController().getWorkflowDefinitionVOWithName(remoteWorkflowDefinitionVO.getName());
		    	if(localWorkflowDefinitionVO == null)
		    		WorkflowDefinitionController.getController().create(remoteWorkflowDefinitionVO);
		    	else
		    		WorkflowDefinitionController.getController().update(remoteWorkflowDefinitionVO);
		    }
        }
        catch(Throwable t)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to update categories:" + t.getMessage(), t);
        }
        
        updateCaches();

        return status;
    }

	/**
     * Gets all content type definitions from the cms.
     */
    
    public Boolean updateComponents(final String principalName, final Object[] input) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }
        
    	Boolean status = new Boolean(true);

        if(logger.isInfoEnabled())
        {
	        logger.info("*******************************************************");
	        logger.info("*       Updating components through webservice        *");
	        logger.info("*******************************************************");
        }
	        
        try
        {
        	initializePrincipal(principalName);
        	
        	logger.info("input:" + input);
        	
        	final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
        	Map arguments = (Map)serializer.deserialize(input);
        	
            logger.info("arguments:" + arguments);
                    	
	    	List components = ContentController.getContentController().getContentVOWithContentTypeDefinition("HTMLTemplate");
	    	ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("HTMLTemplate");
			
	    	if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
			InfoGluePrincipal principal = UserControllerProxy.getController().getUser(principalName);

	    	List missingRemoteComponents = (List)arguments.get("missingComponents");
            logger.info("missingRemoteComponents:" + missingRemoteComponents);
                    
			Iterator missingRemoteComponentsIterator = missingRemoteComponents.iterator();
			while(missingRemoteComponentsIterator.hasNext())
			{
				ContentVO missingRemoteContentVO = (ContentVO)missingRemoteComponentsIterator.next();
				if(missingRemoteContentVO != null)
					missingRemoteContentVO.setIsBranch(Boolean.FALSE);
				
				logger.info("missingRemoteContentVO:" + missingRemoteContentVO + ":" + missingRemoteContentVO.getFullPath());
				String fullPath = missingRemoteContentVO.getFullPath();
				logger.info("fullPath:" + fullPath);
				int siteNodeEnd = fullPath.indexOf(" - /");
				String repositoryString = fullPath.substring(0, siteNodeEnd);
				String restString = fullPath.substring(siteNodeEnd + 4);
				restString = restString.substring(0, restString.lastIndexOf("/"));
				
				if(restString.indexOf("/") > -1)
    				restString = restString.substring(restString.indexOf("/") + 1);
				else
					restString = "";
				
				logger.info("repositoryString:" + repositoryString);
				logger.info("restString:" + restString);
				try
				{
					RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithName(repositoryString);
					logger.info("repositoryVO:" + repositoryVO);
					
					if(repositoryVO == null && components != null && components.size() > 0)
					{
						ContentVO contentVO = (ContentVO)components.get(0);
						repositoryVO = RepositoryController.getController().getRepositoryVOWithId(contentVO.getRepositoryId());
						logger.info("repositoryVO:" + repositoryVO);
					}
					
					if(repositoryVO != null)
					{
						LanguageVO languageVO = LanguageController.getController().getMasterLanguage(repositoryVO.getRepositoryId());
	
						ContentVO parentContent = ContentController.getContentController().getContentVOWithPath(repositoryVO.getId(), restString, true, principal);
						logger.info("parentContent:" + parentContent);
						ContentVO newContentVO = ContentController.getContentController().create(parentContent.getId(), contentTypeDefinitionVO.getContentTypeDefinitionId(), parentContent.getRepositoryId(), missingRemoteContentVO);
						logger.info("Now we want to create the version also on:" + newContentVO.getName());
						ContentVersionVO contentVersionVO = new ContentVersionVO();
						contentVersionVO.setVersionComment("deployment");
						contentVersionVO.setVersionModifier(principal.getName());
						logger.info("missingRemoteContentVO.getVersions():" + missingRemoteContentVO.getVersions());
						if(missingRemoteContentVO.getVersions() != null && missingRemoteContentVO.getVersions().length > 0)
						{
							contentVersionVO.setVersionValue(missingRemoteContentVO.getVersions()[0]);
							logger.info("Creating version on content: " + newContentVO.getId() + " in language: " + languageVO.getId());
							ContentVersionController.getContentVersionController().create(newContentVO.getId(), languageVO.getId(), contentVersionVO, null);
						}
					}
					
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			
			//Getting ready to handle deviating ones
	    	List remoteComponents = (List)arguments.get("deviatingComponents");
            logger.info("remoteComponents:" + remoteComponents);
                    
			Iterator remoteComponentsIterator = remoteComponents.iterator();
			while(remoteComponentsIterator.hasNext())
			{
				ContentVO remoteContentVO = (ContentVO)remoteComponentsIterator.next();
				logger.info("remoteContentVO:" + remoteContentVO + ":" + remoteContentVO.getFullPath());

				if(remoteContentVO.getFullPath() != null && remoteContentVO.getFullPath().startsWith("deviatingRemoteVersionId="))
				{
					String remoteVersionId = remoteContentVO.getFullPath().substring(remoteContentVO.getFullPath().indexOf("=") + 1);
					logger.info("Looking for version in remoteVersionId:" + remoteVersionId);
					
		    		ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(principal, new Integer(remoteVersionId));
					LanguageVO languageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageVO.getId());
					if(contentVersionVO != null)
					{
						if(remoteContentVO.getVersions() != null && remoteContentVO.getVersions().length > 0)
						{
							contentVersionVO.setVersionValue(remoteContentVO.getVersions()[0]);
							logger.info("Updating :" + contentVersionVO.getContentName() + " with new latest versionValue:" + remoteContentVO.getVersions()[0].length());
							ContentVersionController.getContentVersionController().update(contentVersionVO.getId(), contentVersionVO);								
						}
					}
				}
				else
				{
					Iterator componentsIterator = components.iterator();
			    	while(componentsIterator.hasNext())
			    	{
			    		ContentVO contentVO = (ContentVO)componentsIterator.next();
			    		String fullPath = ContentController.getContentController().getContentPath(contentVO.getId(), true, true);					
			    		logger.info("fullPath:" + fullPath);
			    		if(fullPath.equalsIgnoreCase(remoteContentVO.getFullPath()))
			    		{
							LanguageVO languageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
							ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageVO.getId());
							if(contentVersionVO != null)
							{
								if(remoteContentVO.getVersions() != null && remoteContentVO.getVersions().length > 0)
								{
									contentVersionVO.setVersionValue(remoteContentVO.getVersions()[0]);
									logger.info("Updating :" + contentVersionVO.getContentName() + " with new latest versionValue:" + remoteContentVO.getVersions()[0].length());
									ContentVersionController.getContentVersionController().update(contentVersionVO.getId(), contentVersionVO);								
								}
							}		
			    		}
			    	}		    	
				}
			}
        }
        catch(Throwable t)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to update categories:" + t.getMessage(), t);
        }
        
        updateCaches();

        return status;
    }

	/**
     * Gets all workflows from the cms.
     */
    
    public List<WorkflowDefinitionVO> getWorkflowDefinitions(final String principalName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }
        
        if(logger.isInfoEnabled())
        {
	        logger.info("******************************************************");
	        logger.info("*   Getting workflow definitions through webservice  *");
	        logger.info("******************************************************");
        }
	        
        List<WorkflowDefinitionVO> workflowDefinitionVOList = new ArrayList<WorkflowDefinitionVO>();
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
	        
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
	        
			workflowDefinitionVOList = workflowDefinitionController.getWorkflowDefinitionVOList();
        }
        catch(Throwable t)
        {
            logger.error("En error occurred when we tried to get the contentVersionVO:" + t.getMessage(), t);
        }
        
        return workflowDefinitionVOList;
    }

	/**
     * Gets all categories from the cms.
     */
    
    public List<CategoryVO> getAllActiveCategories(final String principalName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }
        
        if(logger.isInfoEnabled())
        {
	        logger.info("******************************************");
	        logger.info("*   Getting categies through webservice  *");
	        logger.info("******************************************");
        }
	        
        List<CategoryVO> categoryVOList = new ArrayList<CategoryVO>();
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
	        
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
	        
			categoryVOList = categoryController.findAllActiveCategories(true);
        }
        catch(Throwable t)
        {
            logger.error("En error occurred when we tried to get the contentVersionVO:" + t.getMessage(), t);
        }
        
        return categoryVOList;
    }

	/**
     * Gets all repository names.
     */
    
    public List<RepositoryVO> getAllRepositories(final String principalName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }
        
        if(logger.isInfoEnabled())
        {
	        logger.info("*******************************************");
	        logger.info("* Getting repositories through webservice *");
	        logger.info("*******************************************");
        }
	        
        List<RepositoryVO> repositoryVOList = new ArrayList<RepositoryVO>();
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
	        
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
	        
			repositoryVOList = repositoryController.getRepositoryVOListNotMarkedForDeletion();
        }
        catch(Throwable t)
        {
            logger.error("En error occurred when we tried to get the contentVersionVO:" + t.getMessage(), t);
        }
        
        return repositoryVOList;
    }
    
	/**
     * Gets all component contents from the cms.
     */
    
    public List<ContentVO> getComponents(final String principalName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }
        
        if(logger.isInfoEnabled())
        {
	        logger.info("******************************************************");
	        logger.info("*        Getting components through webservice       *");
	        logger.info("******************************************************");
        }
	        
        List<ContentVO> contentVOList = new ArrayList<ContentVO>();
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
	        
			if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
	        }
	        			
			contentVOList = contentControllerProxy.getContentVOWithContentTypeDefinition("HTMLTemplate");
			Iterator contentVOListIterator = contentVOList.iterator();
			while(contentVOListIterator.hasNext())
			{
				ContentVO contentVO = (ContentVO)contentVOListIterator.next(); 
				
				LanguageVO languageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
				
				String fullPath = ContentController.getContentController().getContentPath(contentVO.getId(), true, true);
				
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageVO.getId());
				if(contentVersionVO != null)
				{
					contentVO.setContentVersion(contentVersionVO);
					contentVO.setVersions(new String[]{contentVersionVO.getVersionValue()});
				}
				else
				{
					contentVOListIterator.remove();
				}
				
				//logger.info("Versions on remote:" + contentVO.getContentVersion());
				contentVO.setFullPath(fullPath);
			}
        }
        catch(Throwable t)
        {
            logger.error("En error occurred when we tried to get the contents:" + t.getMessage(), t);
        }
        
	    return contentVOList;
    }

	/**
	 * Checks if the principal exists and if the principal is allowed to create the workflow.
	 * 
	 * @param userName the name of the user.
	 * @param workflowName the name of the workflow to create.
	 * @throws SystemException if the principal doesn't exists or doesn't have permission to create the workflow.
	 */
	private void initializePrincipal(final String userName) throws SystemException 
	{
		try 
		{
			principal = UserControllerProxy.getController().getUser(userName);
		}
		catch(SystemException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new SystemException(e);
		}
		if(principal == null) 
		{
			throw new SystemException("No such principal [" + userName + "].");
		}
	}


}
