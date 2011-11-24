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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightGroupVO;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightRoleVO;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.AccessRightUserVO;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.cms.webservices.elements.CreatedEntityBean;
import org.infoglue.cms.webservices.elements.RemoteAttachment;
import org.infoglue.cms.webservices.elements.StatusBean;
import org.infoglue.deliver.util.webservices.DynamicWebserviceSerializer;


/**
 * This class is responsible for letting an external application call InfoGlue
 * API:s remotely. It handles api:s to manage contents and associated entities.
 * 
 * @author Mattias Bogeblad
 */

public class RemoteContentServiceImpl extends RemoteInfoGlueService
{
    private final static Logger logger = Logger.getLogger(RemoteContentServiceImpl.class.getName());

	/**
	 * The principal executing the workflow.
	 */
	private InfoGluePrincipal principal;

    private static ContentControllerProxy contentControllerProxy = ContentControllerProxy.getController();
    private static ContentVersionControllerProxy contentVersionControllerProxy = ContentVersionControllerProxy.getController();
    
    /**
     * Gets a content version from the cms. Very useful for getting the latest working version.
     */
    
    public ContentVersionVO getContentVersion(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }
        
        if(logger.isInfoEnabled())
        {
	        logger.info("**************************************");
	        logger.info("* Getting content through webservice *");
	        logger.info("**************************************");
        }
	        
        ContentVersionVO contentVersionVO = null;
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
	        Map args = (Map) serializer.deserialize(inputsArray);
	        logger.info("args:" + args);
	        
	        Integer contentId = (Integer)args.get("contentId");
	        Integer languageId = (Integer)args.get("languageId");
	
	        if(logger.isInfoEnabled())
	        {
		        logger.info("principalName:" + principalName);
		        logger.info("contentId:" + contentId);
		        logger.info("languageId:" + languageId);
	        }
	        
            initializePrincipal(principalName);
            contentVersionVO = contentVersionControllerProxy.getACLatestActiveContentVersionVO(this.principal, contentId, languageId);
        }
        catch(Throwable t)
        {
            logger.error("En error occurred when we tried to get the contentVersionVO:" + t.getMessage());
            logger.warn("En error occurred when we tried to get the contentVersionVO:" + t.getMessage(), t);
        }
        
        return contentVersionVO;
    }

    /**
     * Inserts a new Content including versions etc.
     */
    
    public int createContent(final String principalName, ContentVO contentVO, int parentContentId, int contentTypeDefinitionId, int repositoryId) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return -1;
        }
        
        int newContentId = 0;
        
        logger.info("***************************************");
        logger.info("Creating content through webservice....");
        logger.info("***************************************");
        
        logger.info("parentContentId:" + parentContentId);
        logger.info("contentTypeDefinitionId:" + contentTypeDefinitionId);
        logger.info("repositoryId:" + repositoryId);
        
        try
        {
            initializePrincipal(principalName);
	        ContentVO newContentVO = contentControllerProxy.acCreate(this.principal, new Integer(parentContentId), new Integer(contentTypeDefinitionId), new Integer(repositoryId), contentVO);
	        newContentId = newContentVO.getId().intValue();
        }
        catch(Exception e)
        {
            logger.error("En error occurred when we tried to create a new content:" + e.getMessage(), e);
        }
        
        updateCaches();
        
        return newContentId;
    }
    
    /**
     * Inserts a new ContentVersion.
     */
    
    public int createContentVersion(final String principalName, ContentVersionVO contentVersionVO, int contentId, int languageId) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return -1;
        }

        int newContentVersionId = 0;
        
        logger.info("***************************************");
        logger.info("Creating content through webservice....");
        logger.info("***************************************");
        
        try
        {
            initializePrincipal(principalName);
	        ContentVersionVO newContentVersionVO = contentVersionControllerProxy.acCreate(this.principal, new Integer(contentId), new Integer(languageId), contentVersionVO);
	        newContentVersionId = newContentVersionVO.getId().intValue();
        }
        catch(Exception e)
        {
            logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage());
            logger.warn("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        updateCaches();

        return newContentVersionId;
    }
    
 
    /**
     * Inserts a new Content including versions etc.
     */
    
    public StatusBean createContents(final String principalName, final Object[] inputsArray/*List contents*/) 
    {    	
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new StatusBean(false, "You are not allowed to talk to this service");
        }

    	StatusBean statusBean = new StatusBean(true, "ok");
    	
        List newContentIdList = new ArrayList();
        
        logger.info("****************************************");
        logger.info("Creating contents through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            List contents = (List) serializer.deserialize(inputsArray);
	        logger.info("contents:" + contents);

            initializePrincipal(principalName);
	        Iterator contentIterator = contents.iterator();
	        while(contentIterator.hasNext())
	        {
	            //String value = (String)contentIterator.next();
	            //logger.info("value:" + value);
	            
	            Map content = (Map)contentIterator.next();
	            
	            String name 					= (String)content.get("name");
	            Integer contentTypeDefinitionId = (Integer)content.get("contentTypeDefinitionId");
	            Integer repositoryId 			= (Integer)content.get("repositoryId");
	            Integer parentContentId 		= (Integer)content.get("parentContentId");
	            String contentPath 				= (String)content.get("contentPath");

	            String creatorName 				= (String)content.get("creatorName");
	            Boolean isBranch 				= (Boolean)content.get("isBranch");
	            Integer isProtected 			= (Integer)content.get("isProtected");

	            Date expireDateTime = null;
	            Object expireDateTimeObject = content.get("expireDateTime");
	            if(expireDateTimeObject != null)
	            {
		            if(expireDateTimeObject instanceof Date)
		            	expireDateTime = (Date)expireDateTimeObject;
		            else if(expireDateTimeObject instanceof Calendar)
		            	expireDateTime = ((Calendar)expireDateTimeObject).getTime();
		        }	            	

	            Date publishDateTime = null;
	            Object publishDateTimeObject = content.get("publishDateTime");
	            if(publishDateTimeObject != null)
	            {
		            if(publishDateTimeObject instanceof Date)
		            	publishDateTime = (Date)publishDateTimeObject;
		            else if(publishDateTimeObject instanceof Calendar)
		            	publishDateTime = ((Calendar)publishDateTimeObject).getTime();
	        	}
	            
	            logger.info("name:" + name);
	            logger.info("contentTypeDefinitionId:" + contentTypeDefinitionId);
	            logger.info("repositoryId:" + repositoryId);
	            logger.info("parentContentId:" + parentContentId);
	            logger.info("contentPath:" + contentPath);
	            
	            if(contentPath != null && !contentPath.equals(""))
	            {
		            Database db = CastorDatabaseService.getDatabase();
		    		beginTransaction(db);
		    		
		    		try
		    		{
		    			if(parentContentId != null)
		    			{
		    				StringBuffer path = new StringBuffer();
		    				
		    				Content parentContent = ContentController.getContentController().getContentWithId(parentContentId, db);
		    				path.insert(0, parentContent.getName() + "/");
		    				while(parentContent.getParentContent() != null)
		    				{
		    					parentContent = parentContent.getParentContent();
		    					if(parentContent != null && parentContent.getParentContent() != null)
		    						path.insert(0, parentContent.getName() + "/");
		    				}

		    				contentPath = path.toString() + contentPath;
		    			}
		    			
			            ContentVO parentContentVO = ContentController.getContentController().getContentVOWithPath(repositoryId, contentPath, true, this.principal, db);
			            parentContentId = parentContentVO.getId();
			            
		    			commitTransaction(db);
		    		}
		    		catch(Exception e)
		    		{
		    			logger.error("An error occurred so we should not complete the transaction:" + e, e);
		    			rollbackTransaction(db);
		    			throw new SystemException(e.getMessage());
		    		}
	            }
	            
	           	ContentVO contentVO = new ContentVO();
	            contentVO.setName(name);
	            contentVO.setContentTypeDefinitionId(contentTypeDefinitionId);
	            contentVO.setRepositoryId(repositoryId);
	            contentVO.setParentContentId(parentContentId);
	            
	            if(expireDateTime != null)
	            	contentVO.setExpireDateTime(expireDateTime);
	            if(isBranch != null)
	            	contentVO.setIsBranch(isBranch);
	            if(isProtected != null)
	                contentVO.setIsProtected(isProtected);
	            if(publishDateTime != null)
	            	contentVO.setPublishDateTime(publishDateTime);
	            
	            if(creatorName != null)
	            	contentVO.setCreatorName(creatorName);
	            else if(contentVO.getCreatorName() == null)
	                contentVO.setCreatorName(this.principal.getName());
	            
	            ContentVO newContentVO = contentControllerProxy.acCreate(this.principal, contentVO.getParentContentId(), contentVO.getContentTypeDefinitionId(), contentVO.getRepositoryId(), contentVO);
	            if(newContentVO != null)
	            	statusBean.getCreatedBeans().add(new CreatedEntityBean(ContentVO.class.getName(), new Long(newContentVO.getId())));
	            
	            List contentVersions = (List)content.get("contentVersions");
	            Iterator contentVersionIterator = contentVersions.iterator();
	            while(contentVersionIterator.hasNext())
	            {
	                Map contentVersion = (Map)contentVersionIterator.next();
	                
	                Integer languageId = (Integer)contentVersion.get("languageId");
	                Integer stateId = (Integer)contentVersion.get("stateId");
	                Boolean allowHTMLContent = (Boolean)contentVersion.get("allowHTMLContent");
	                Boolean allowExternalLinks = (Boolean)contentVersion.get("allowExternalLinks");
	                Boolean allowDollarSigns = (Boolean)contentVersion.get("allowDollarSigns");
	                Boolean allowAnchorSigns = (Boolean)contentVersion.get("allowAnchorSigns");

	                if(allowHTMLContent == null)
	                	allowHTMLContent = new Boolean(false);
	                if(allowExternalLinks == null)
	                	allowExternalLinks = new Boolean(false);
	                if(allowDollarSigns == null)
	                	allowDollarSigns = new Boolean(false);
	                if(allowAnchorSigns == null)
	                	allowAnchorSigns = new Boolean(true);
	                
	    	        logger.info("languageId:" + languageId);
	    	        logger.info("stateId:" + stateId);
	    	        logger.info("allowHTMLContent:" + allowHTMLContent);
	    	        logger.info("allowExternalLinks:" + allowExternalLinks);
	    	        logger.info("allowDollarSigns:" + allowDollarSigns);
	    	        logger.info("allowAnchorSigns:" + allowAnchorSigns);

	                ContentVersionVO contentVersionVO = new ContentVersionVO();
	                contentVersionVO.setLanguageId(languageId);
	                
	                if(contentVersionVO.getVersionModifier() == null)
	                    contentVersionVO.setVersionModifier(this.principal.getName());
	                
	                Map attributes = (Map)contentVersion.get("contentVersionAttributes");
	                
	                DOMBuilder domBuilder = new DOMBuilder();
	                Document document = domBuilder.createDocument();
	                
	                Element rootElement = domBuilder.addElement(document, "root");
	                domBuilder.addAttribute(rootElement, "xmlns", "x-schema:Schema.xml");
	                Element attributesRoot = domBuilder.addElement(rootElement, "attributes");
	                
	                Iterator attributesIterator = attributes.keySet().iterator();
	                while(attributesIterator.hasNext())
	                {
	                    String attributeName  = (String)attributesIterator.next();
	                    String attributeValue = (String)attributes.get(attributeName);
		                
	                    attributeValue = cleanAttributeValue(attributeValue, allowHTMLContent, allowExternalLinks, allowDollarSigns, allowAnchorSigns);
	                    
	                    Element attribute = domBuilder.addElement(attributesRoot, attributeName);
	                    domBuilder.addCDATAElement(attribute, attributeValue);
	                }	                

	                contentVersionVO.setVersionValue(document.asXML());
	                
	    	        ContentVersionVO newContentVersionVO = contentVersionControllerProxy.acCreate(this.principal, newContentVO.getId(), languageId, contentVersionVO);
	    	        Integer newContentVersionId = newContentVersionVO.getId();
	            	statusBean.getCreatedBeans().add(new CreatedEntityBean(ContentVersionVO.class.getName(), new Long(newContentVersionId)));

	    	        List digitalAssets = (List)contentVersion.get("digitalAssets");
	    	        
	    	        logger.info("digitalAssets:" + digitalAssets);
	    	        if(digitalAssets != null)
	    	        {
		    	        Iterator digitalAssetIterator = digitalAssets.iterator();
		    	        while(digitalAssetIterator.hasNext())
		    	        {
		    	            RemoteAttachment remoteAttachment = (RemoteAttachment)digitalAssetIterator.next();
			    	        logger.info("digitalAssets in ws:" + remoteAttachment);
			    	        
			            	DigitalAssetVO newAsset = new DigitalAssetVO();
							newAsset.setAssetContentType(remoteAttachment.getContentType());
							newAsset.setAssetKey(remoteAttachment.getName());
							newAsset.setAssetFileName(remoteAttachment.getFileName());
							newAsset.setAssetFilePath(remoteAttachment.getFilePath());
							newAsset.setAssetFileSize(new Integer(new Long(remoteAttachment.getBytes().length).intValue()));
							//is = new FileInputStream(renamedFile);
							InputStream is = new ByteArrayInputStream(remoteAttachment.getBytes());
		
			    	        DigitalAssetVO newDigitalAssetVO = DigitalAssetController.create(newAsset, is, newContentVersionId, principal);
			    	        if(newDigitalAssetVO != null)
			    	        	statusBean.getCreatedBeans().add(new CreatedEntityBean(DigitalAssetVO.class.getName(), new Long(newDigitalAssetVO.getId())));
			    	    }	 
	    	        }
	    	        
	    	        
	    	        List contentCategories = (List)contentVersion.get("contentCategories");
	    	        
	    	        logger.info("contentCategories:" + contentCategories);
	    	        if(contentCategories != null)
	    	        {
		    	        Iterator contentCategoriesIterator = contentCategories.iterator();
		    	        while(contentCategoriesIterator.hasNext())
		    	        {
		    	        	String contentCategoryString = (String)contentCategoriesIterator.next();
		    	        	String[] split = contentCategoryString.split("=");
		    	        	String categoryKey = split[0];
		    	        	String fullCategoryName = split[1];
		    	        	logger.info("categoryKey:" + categoryKey);
		    	        	logger.info("fullCategoryName:" + fullCategoryName);
		    	        	
		    	        	CategoryVO categoryVO = CategoryController.getController().findByPath(fullCategoryName);
		    	        	logger.info("categoryVO:" + categoryVO);

			    	        List categoryVOList = new ArrayList();
			    	        categoryVOList.add(categoryVO);
			    	        
			    	        Database db = beginTransaction();

			    			try
			    			{
			    				ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(newContentVersionVO.getId(), db);
			    				logger.info("Adding categoryKey:" + categoryKey + " to " + newContentVersionVO.getId() + ":" + categoryVO);
				    	        //ContentCategoryController.getController().create(categoryVOList, newContentVersionVO, categoryKey);
				    			final List categories = categoryVOListToCategoryList(categoryVOList, db);
				    			ContentCategoryController.getController().create(categories, latestContentVersion, categoryKey, db);
				    			
				    			commitTransaction(db);
			    			}
			    			catch (Exception e)
			    			{
			    				logger.warn("Error in contentCategory loop: " + e.getMessage(), e);
			    				rollbackTransaction(db);
			    				throw new SystemException(e.getMessage());
			    			}
			    		}	 
	    	        }
	    	        
	    	        
	    	        //Should we also change state on newly created content version?
	    	        if(stateId != null && !stateId.equals(ContentVersionVO.WORKING_STATE))
	    	        {
	    	        	List events = new ArrayList();
	    	    		ContentStateController.changeState(newContentVersionId, stateId, "Remote update from deliver", false, this.principal, newContentVO.getId(), events);
	    	        
	    	    		if(stateId.equals(ContentVersionVO.PUBLISHED_STATE))
	    	    		{
	    	    		    PublicationVO publicationVO = new PublicationVO();
	    	    		    publicationVO.setName("Direct publication by " + this.principal.getName());
	    	    		    publicationVO.setDescription("Direct publication from deliver");
	    	    		    publicationVO.setRepositoryId(repositoryId);
	    	    		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.principal);
	    	    		}

	    	        }
	            }
	            
	            List accessRights = (List)content.get("accessRights");
	            if(accessRights != null)
	            {
		            Iterator accessRightsIterator = accessRights.iterator();
		            while(accessRightsIterator.hasNext())
		            {
		                Map accessRightMap = (Map)accessRightsIterator.next();
		                
		                String interceptionPointName = (String)accessRightMap.get("interceptionPointName");
		                String parameters = (String)accessRightMap.get("parameters");
		                
		                Database db = CastorDatabaseService.getDatabase();
			    		beginTransaction(db);
			    		
			    		try
			    		{
			                InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(interceptionPointName, db);
			                
			                logger.info("interceptionPointName:" + interceptionPointName);
			    	        logger.info("parameters:" + parameters);
		
			    	        AccessRightVO accessRightVO = new AccessRightVO();
			    	        accessRightVO.setParameters("" + newContentVO.getId());
			    	        
			    	        AccessRight accessRight = AccessRightController.getController().create(accessRightVO, interceptionPoint, db);
			    	        
			    	        List accessRightRoles = (List)accessRightMap.get("accessRightRoles");
			    	        if(accessRightRoles != null)
				            {
			    	        	Iterator accessRightRolesIterator = accessRightRoles.iterator();
					            while(accessRightRolesIterator.hasNext())
					            {
					                //Map accessRightRoleMap = (Map)accessRightRolesIterator.next();
					                
					                String roleName = (String)accessRightRolesIterator.next();
					                
								    AccessRightRoleVO accessRightRoleVO = new AccessRightRoleVO();
								    accessRightRoleVO.setRoleName(roleName);
								    AccessRightRole accessRightRole = AccessRightController.getController().createAccessRightRole(db, accessRightRoleVO, accessRight);
								    accessRight.getRoles().add(accessRightRole);
					            }
				            }
			    	        
			    	        List accessRightGroups = (List)accessRightMap.get("accessRightGroups");
			    	        if(accessRightGroups != null)
				            {
					            Iterator accessRightGroupsIterator = accessRightGroups.iterator();
					            while(accessRightGroupsIterator.hasNext())
					            {
					                String groupName = (String)accessRightGroupsIterator.next();
					                
								    AccessRightGroupVO accessRightGroupVO = new AccessRightGroupVO();
								    accessRightGroupVO.setGroupName(groupName);
								    AccessRightGroup accessRightGroup = AccessRightController.getController().createAccessRightGroup(db, accessRightGroupVO, accessRight);
								    accessRight.getGroups().add(accessRightGroup);
					            }
				            }

			    	        List accessRightUsers = (List)accessRightMap.get("accessRightUsers");
			    	        if(accessRightUsers != null)
				            {
					            Iterator accessRightUsersIterator = accessRightUsers.iterator();
					            while(accessRightUsersIterator.hasNext())
					            {
					                String userName = (String)accessRightUsersIterator.next();
					                
								    AccessRightUserVO accessRightUserVO = new AccessRightUserVO();
								    accessRightUserVO.setUserName(userName);
								    AccessRightUser accessRightUser = AccessRightController.getController().createAccessRightUser(db, accessRightUserVO, accessRight);
								    accessRight.getUsers().add(accessRightUser);
					            }
				            }

							commitTransaction(db);
			    		} 
			    		catch (Exception e) 
			    		{
			    			logger.warn("An error occurred so we should not complete the transaction:" + e);
			    			rollbackTransaction(db);
			    			throw new SystemException(e.getMessage());
			    		}
		            }
	            }
	            
	            newContentIdList.add(newContentVO.getId());
	            
	        }
	        logger.info("Done with contents..");

        }
        catch(Throwable e)
        {
        	statusBean.setStatus(false);
        	statusBean.setMessage("En error occurred when we tried to create a new content:" + e.getMessage());
            logger.error("En error occurred when we tried to create a new content:" + e.getMessage());
            logger.warn("En error occurred when we tried to create a new content:" + e.getMessage(), e);
        }
        
        updateCaches();

        return statusBean;
    }

	/**
	 * 
	 * @param db the database to use in the operation.
	 * @param categoryVOList
	 * @return
	 * @throws Exception
	 */
	private List categoryVOListToCategoryList(final List categoryVOList, Database db) throws Exception 
	{
		final List result = new ArrayList();
		for(Iterator i=categoryVOList.iterator(); i.hasNext(); ) 
		{
			CategoryVO categoryVO = (CategoryVO) i.next();
			result.add(CategoryController.getController().findById(categoryVO.getCategoryId(), db));
		}
		return result;
	}
	/**
     * Updates a content.
     */
    
    public Boolean updateContent(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

    	Boolean status = new Boolean(true);
        
        logger.info("****************************************");
        logger.info("Updating content through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            Map content = (Map) serializer.deserialize(inputsArray);
	        logger.info("content:" + content);

            initializePrincipal(principalName);
            
            Integer contentId 				= (Integer)content.get("contentId");
            String name 					= (String)content.get("name");

            Date expireDateTime = null;
            Object expireDateTimeObject = content.get("expireDateTime");
            if(expireDateTimeObject != null)
            {
	            if(expireDateTimeObject instanceof Date)
	            	expireDateTime = (Date)expireDateTimeObject;
	            else if(expireDateTimeObject instanceof Calendar)
	            	expireDateTime = ((Calendar)expireDateTimeObject).getTime();
	        }	            	

            Date publishDateTime = null;
            Object publishDateTimeObject = content.get("publishDateTime");
            if(publishDateTimeObject != null)
            {
	            if(publishDateTimeObject instanceof Date)
	            	publishDateTime = (Date)publishDateTimeObject;
	            else if(publishDateTimeObject instanceof Calendar)
	            	publishDateTime = ((Calendar)publishDateTimeObject).getTime();
        	}
            
            logger.info("contentId:" + contentId);
            logger.info("name:" + name);
            logger.info("publishDateTime:" + publishDateTime);
            logger.info("expireDateTime:" + expireDateTime);
            
            ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);

            if(name != null)
                contentVO.setName(name);

            if(publishDateTime != null)
                contentVO.setPublishDateTime(publishDateTime);

            if(expireDateTime != null)
                contentVO.setExpireDateTime(expireDateTime);

            ContentVO newContentVO = contentControllerProxy.acUpdate(this.principal, contentVO, null);
	           
	        logger.info("Done with contents..");

        }
        catch(Throwable e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to create a new content:" + e.getMessage());
            logger.warn("En error occurred when we tried to create a new content:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;
    }

    /**
     * Updates a content.
     */
    
    public Boolean updateContentVersion(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

    	Boolean status = new Boolean(true);
        
        logger.info("****************************************");
        logger.info("Updating content versions through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            Map contentVersion = (Map) serializer.deserialize(inputsArray);
	        logger.info("contentVersion:" + contentVersion);

            initializePrincipal(principalName);
            
            Integer contentVersionId = (Integer)contentVersion.get("contentVersionId");
            Integer contentId 		 = (Integer)contentVersion.get("contentId");
            Integer languageId 		 = (Integer)contentVersion.get("languageId");
            Integer stateId			 = (Integer)contentVersion.get("stateId");
            String versionComment 	 = (String)contentVersion.get("versionComment");
            Boolean allowHTMLContent = (Boolean)contentVersion.get("allowHTMLContent");
            Boolean allowExternalLinks 		= (Boolean)contentVersion.get("allowExternalLinks");
            Boolean allowDollarSigns = (Boolean)contentVersion.get("allowDollarSigns");
            Boolean allowAnchorSigns = (Boolean)contentVersion.get("allowAnchorSigns");
            Boolean keepExistingAttributes 	= (Boolean)contentVersion.get("keepExistingAttributes");
            Boolean keepExistingCategories 	= (Boolean)contentVersion.get("keepExistingCategories");

            if(allowHTMLContent == null)
            	allowHTMLContent = new Boolean(false);

            if(allowExternalLinks == null)
            	allowExternalLinks = new Boolean(false);

            if(keepExistingCategories == null)
            	keepExistingCategories = new Boolean(true);

            if(allowDollarSigns == null)
            	allowDollarSigns = new Boolean(false);
            if(allowAnchorSigns == null)
            	allowAnchorSigns = new Boolean(true);
            
            logger.info("contentVersionId:" + contentVersionId);
            logger.info("contentId:" + contentId);
            logger.info("languageId:" + languageId);
            logger.info("stateId:" + stateId);
            logger.info("keepExistingAttributes:" + keepExistingAttributes);
            logger.info("keepExistingCategories:" + keepExistingCategories);
            logger.info("versionComment:" + versionComment);
            logger.info("allowHTMLContent:" + allowHTMLContent);
            logger.info("allowExternalLinks:" + allowExternalLinks);
	        logger.info("allowDollarSigns:" + allowDollarSigns);
	        logger.info("allowAnchorSigns:" + allowAnchorSigns);

            ContentVersionVO contentVersionVO = null;
            if(contentVersionId != null)
                contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId);
            else
                contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
                
            if(contentVersionVO == null)
                return new Boolean(false);
                
            contentVersionVO.setVersionComment(versionComment);
            contentVersionVO.setModifiedDateTime(new Date());
            contentVersionVO.setVersionModifier("" + principalName);
            
            Map attributes = (Map)contentVersion.get("contentVersionAttributes");
            
            if(attributes != null && attributes.size() > 0)
            {
            	DOMBuilder domBuilder = new DOMBuilder();

            	Element attributesRoot = null;
        		Document document = null;
        		
            	if(keepExistingAttributes)
            	{
            		String existingXML = contentVersionVO.getVersionValue();
            		document = domBuilder.getDocument(existingXML);
            		attributesRoot = (Element)document.getRootElement().element("attributes");
            	}
            	else
            	{
		            document = domBuilder.createDocument();
		            Element rootElement = domBuilder.addElement(document, "root");
		            domBuilder.addAttribute(rootElement, "xmlns", "x-schema:Schema.xml");
		            attributesRoot = domBuilder.addElement(rootElement, "attributes");
            	}

            	if(logger.isDebugEnabled())
            		logger.info("attributesRoot:" + attributesRoot);
            	
	            Iterator attributesIterator = attributes.keySet().iterator();
	            while(attributesIterator.hasNext())
	            {
	                String attributeName  = (String)attributesIterator.next();
	                String attributeValue = (String)attributes.get(attributeName);
	                
                    attributeValue = cleanAttributeValue(attributeValue, allowHTMLContent, allowExternalLinks, allowDollarSigns, allowAnchorSigns);
                    
                    if(keepExistingAttributes)
	                {
                    	Element attribute = attributesRoot.element(attributeName);
                    	if(attribute != null)
                    	{
                    		attribute.clearContent();
                        	domBuilder.addCDATAElement(attribute, attributeValue);
                    	}
    	                else
    	                {
                        	attribute = domBuilder.addElement(attributesRoot, attributeName);
        	                domBuilder.addCDATAElement(attribute, attributeValue);
    	                }
	                }
	                else
	                {
                    	Element attribute = domBuilder.addElement(attributesRoot, attributeName);
    	                domBuilder.addCDATAElement(attribute, attributeValue);
	                }
	            }	                

	            contentVersionVO.setVersionValue(document.asXML());
            }
            
            ContentVersionControllerProxy.getController().acUpdate(principal, contentId, languageId, contentVersionVO);
            
            //Assets now
            List digitalAssets = (List)contentVersion.get("digitalAssets");
	        
            if(digitalAssets != null)
            {
		        logger.info("digitalAssets:" + digitalAssets.size());
		        
		        Iterator digitalAssetIterator = digitalAssets.iterator();
		        while(digitalAssetIterator.hasNext())
		        {
		            RemoteAttachment remoteAttachment = (RemoteAttachment)digitalAssetIterator.next();
	    	        logger.info("digitalAssets in ws:" + remoteAttachment);
	    	        
	            	DigitalAssetVO newAsset = new DigitalAssetVO();
					newAsset.setAssetContentType(remoteAttachment.getContentType());
					newAsset.setAssetKey(remoteAttachment.getName());
					newAsset.setAssetFileName(remoteAttachment.getFileName());
					newAsset.setAssetFilePath(remoteAttachment.getFilePath());
					newAsset.setAssetFileSize(new Integer(new Long(remoteAttachment.getBytes().length).intValue()));
					InputStream is = new ByteArrayInputStream(remoteAttachment.getBytes());
	
	    	        DigitalAssetController.create(newAsset, is, contentVersionVO.getContentVersionId(), principal);
	    	    }
            }
            			
			if(!keepExistingCategories)
			{
				ContentCategoryController.getController().deleteByContentVersion(contentVersionVO.getId()); // .deleteByContentVersion(contentVersionVO.getId(), db);
			}

            List contentCategories = (List)contentVersion.get("contentCategories");
	        
	        logger.info("contentCategories:" + contentCategories);
	        if(contentCategories != null)
	        {
    	        Iterator contentCategoriesIterator = contentCategories.iterator();
    	        while(contentCategoriesIterator.hasNext())
    	        {
    	        	String contentCategoryString = (String)contentCategoriesIterator.next();
    	        	String[] split = contentCategoryString.split("=");
    	        	String categoryKey = split[0];
    	        	String fullCategoryName = split[1];
    	        	logger.info("categoryKey:" + categoryKey);
    	        	logger.info("fullCategoryName:" + fullCategoryName);
    	        	
    	        	CategoryVO categoryVO = CategoryController.getController().findByPath(fullCategoryName);
    	        	logger.info("categoryVO:" + categoryVO);

	    	        List categoryVOList = new ArrayList();
	    	        categoryVOList.add(categoryVO);
	    	        
	    	        Database db = beginTransaction();

	    			try
	    			{
	    				ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getId(), db);

	    				logger.info("Adding categoryKey:" + categoryKey + " to " + contentVersionVO.getId() + ":" + categoryVO);
		    	        //ContentCategoryController.getController().create(categoryVOList, newContentVersionVO, categoryKey);
		    			final List categories = categoryVOListToCategoryList(categoryVOList, db);		    			
		    			ContentCategoryController.getController().create(categories, latestContentVersion, categoryKey, db);
		    			
		    			commitTransaction(db);
	    			}
	    			catch (Exception e)
	    			{
		    			logger.warn("An error occurred so we should not complete the transaction:" + e);
	    				rollbackTransaction(db);
	    				throw new SystemException(e.getMessage());
	    			}
	    		}	 
	        }
	        
	        
	        //Should we also change state on newly created content version?
	        if(stateId != null && !stateId.equals(ContentVersionVO.WORKING_STATE))
	        {
	        	List events = new ArrayList();
	    		ContentStateController.changeState(contentVersionVO.getId(), stateId, "Remote update from deliver", false, this.principal, contentVersionVO.getContentId(), events);
	        
	    		if(stateId.equals(ContentVersionVO.PUBLISHED_STATE))
	    		{
	    		    PublicationVO publicationVO = new PublicationVO();
	    		    publicationVO.setName("Direct publication by " + this.principal.getName());
	    		    publicationVO.setDescription("Direct publication from deliver");
	    		    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
	    		    publicationVO.setRepositoryId(contentVO.getRepositoryId());
	    		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.principal);
	    		}

	        }
            
	        logger.info("Done with contentVersion..");

        }
        catch(Throwable e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to create a new content:" + e.getMessage());
            logger.warn("En error occurred when we tried to create a new content:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;
    }

    
    
    /**
     * Deletes a digital asset.
     */
    
    public Boolean deleteDigitalAsset(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

    	Boolean status = new Boolean(true);
        
        logger.info("****************************************");
        logger.info("Updating content through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            Map digitalAsset = (Map) serializer.deserialize(inputsArray);
	        logger.info("digitalAsset:" + digitalAsset);

            initializePrincipal(principalName);
            
            Integer contentVersionId = (Integer)digitalAsset.get("contentVersionId");
            Integer contentId 		 = (Integer)digitalAsset.get("contentId");
            Integer languageId 		 = (Integer)digitalAsset.get("languageId");
            String assetKey 		 = (String)digitalAsset.get("assetKey");
            
            logger.info("contentVersionId:" + contentVersionId);
            logger.info("contentId:" + contentId);
            logger.info("languageId:" + languageId);
            logger.info("assetKey:" + assetKey);
            
            ContentVersionController.getContentVersionController().deleteDigitalAsset(contentId, languageId, assetKey);
               
	        logger.info("Done with contents..");

        }
        catch(Throwable e)
        {
        	status = new Boolean(false);
        	logger.error("En error occurred when we tried to delete a digitalAsset:" + e.getMessage());
        	logger.warn("En error occurred when we tried to delete a digitalAsset:" + e.getMessage(), e);
        }
        
        updateCaches();

        
        return status;
    }

    /**
     * Deletes a content.
     */
    
    public Boolean deleteContent(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

    	Boolean status = new Boolean(true);
    	
        logger.info("****************************************");
        logger.info("Updating content through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            Map content = (Map) serializer.deserialize(inputsArray);
	        logger.info("content:" + content);

            initializePrincipal(principalName);
            
            Integer contentId = (Integer)content.get("contentId");
            //Boolean skipRelationCheck = (Boolean)content.get("skipRelationCheck");
            //Boolean skipServiceBindings = (Boolean)content.get("skipServiceBindings");
            Boolean forceDelete = (Boolean)content.get("forceDelete");
            if(forceDelete == null)
            	forceDelete = new Boolean(false);
                        
            logger.info("contentId:" + contentId);
            
            ContentVO contentVO = new ContentVO();
            contentVO.setContentId(contentId);
            
            if(forceDelete.booleanValue())
            {
            	ContentVO currentContentVO = ContentControllerProxy.getContentController().getContentVOWithId(contentId);
		        List contentVersionsVOList = ContentVersionController.getContentVersionController().getPublishedActiveContentVersionVOList(contentId);
		        
		        List events = new ArrayList();
				Iterator it = contentVersionsVOList.iterator();
				while(it.hasNext())
				{
					ContentVersionVO contentVersionVO = (ContentVersionVO)it.next();
					
					EventVO eventVO = new EventVO();
					eventVO.setDescription("Unpublished before forced deletion");
					eventVO.setEntityClass(ContentVersion.class.getName());
					eventVO.setEntityId(contentVersionVO.getContentVersionId());
					eventVO.setName(contentVersionVO.getContentName() + "(" + contentVersionVO.getLanguageName() + ")");
					eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
					eventVO = EventController.create(eventVO, currentContentVO.getRepositoryId(), principal);
					events.add(eventVO);
				}
			
			    PublicationVO publicationVO = new PublicationVO();
			    publicationVO.setName("Direct publication by " + this.principal.getName());
			    publicationVO.setDescription("Unpublished all versions before forced deletion");
			    //publicationVO.setPublisher(this.getInfoGluePrincipal().getName());
			    publicationVO.setRepositoryId(currentContentVO.getRepositoryId());
			    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, true, this.principal);
            }
			
			ContentController.getContentController().delete(contentVO, principal, forceDelete.booleanValue());
               
	        logger.info("Done with contents..");

        }
        catch(Throwable e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to delete a digitalAsset:" + e.getMessage());
            logger.warn("En error occurred when we tried to delete a digitalAsset:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;
    }

	/**
     * Deletes a content.
     */
    
    public Boolean deleteContentVersion(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

    	Boolean status = new Boolean(true);
        
        logger.info("****************************************");
        logger.info("Updating content through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            Map digitalAsset = (Map) serializer.deserialize(inputsArray);
	        logger.info("digitalAsset:" + digitalAsset);

            initializePrincipal(principalName);
            
            Integer contentVersionId = (Integer)digitalAsset.get("contentVersionId");
            
            logger.info("contentVersionId:" + contentVersionId);
            ContentVersionVO contentVersionVO = new ContentVersionVO(); 
            contentVersionVO.setContentVersionId(contentVersionId);
            
            ContentVersionController.getContentVersionController().delete(contentVersionVO);
               
	        logger.info("Done with contents..");

        }
        catch(Throwable e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to delete a digitalAsset:" + e.getMessage());
            logger.warn("En error occurred when we tried to delete a digitalAsset:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;
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
