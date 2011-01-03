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
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
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
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
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

public class RemoteManagementServiceImpl extends RemoteInfoGlueService
{
    private final static Logger logger = Logger.getLogger(RemoteManagementServiceImpl.class.getName());

	/**
	 * The principal executing the workflow.
	 */
	private InfoGluePrincipal principal;

    /**
     * Sets access rights for an entity (only content / sitenode) right now.
     */
    
    public StatusBean setAccessRights(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new StatusBean(false, "You are not allowed to talk to this service");
        }

    	StatusBean statusBean = new StatusBean(true, "ok");
    	
        logger.info("****************************************");
        logger.info("Creating contents through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            List accessRights = (List) serializer.deserialize(inputsArray);
	        logger.info("accessRights:" + accessRights);

            initializePrincipal(principalName);

            if(accessRights != null)
            {
	            Iterator accessRightsIterator = accessRights.iterator();
	            while(accessRightsIterator.hasNext())
	            {
	                Map accessRightMap = (Map)accessRightsIterator.next();
	                
	                String interceptionPointName 		= (String)accessRightMap.get("interceptionPointName");
	                String interceptionPointCategory 	= interceptionPointName.substring(0, interceptionPointName.indexOf("."));
	                String parameters 					= (String)accessRightMap.get("parameters");
	                String clearOldAccessRights 		= (String)accessRightMap.get("clearOldAccessRights");
	                
	        		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
	        		
	        		if(interceptionPointCategory.equalsIgnoreCase("Content"))
	        		{	
	        			Integer contentId = new Integer(parameters);
	        			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
	        			if(!contentVO.getCreatorName().equalsIgnoreCase(principal.getName()))
	        			{
	        				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
	        				if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(principal, "Content.ChangeAccessRights", protectedContentId.toString()))
	        					ceb.add(new AccessConstraintException("Content.contentId", "1006"));
	        			}
	        		}
	        		else if(interceptionPointCategory.equalsIgnoreCase("SiteNodeVersion"))
	        		{	
	        			Integer siteNodeVersionId = new Integer(parameters);
	        			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
	        			if(!siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(principal.getName()))
	        			{
	        				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
	        				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(principal, "SiteNodeVersion.ChangeAccessRights", siteNodeVersionId.toString()))
	        					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1006"));
	        			}
	        		}
	        		else
	        		{
	        			ceb.add(new AccessConstraintException("Repository.repositoryId", "1006"));
	        		}
	        		
	        		ceb.throwIfNotEmpty();

	                
	                Database db = CastorDatabaseService.getDatabase();
		    		beginTransaction(db);
		    		
		    		try
		    		{
		                InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(interceptionPointName, db);
		                
		                logger.info("interceptionPointName:" + interceptionPointName);
		    	        logger.info("parameters:" + parameters);
	
		    	        AccessRightVO accessRightVO = new AccessRightVO();
		    	        accessRightVO.setParameters("" + parameters);
		    	        
		    	        if(clearOldAccessRights != null && clearOldAccessRights.equalsIgnoreCase("true"))
		    	        	AccessRightController.getController().delete(interceptionPoint.getId(), parameters, true, db);
		    	        
		    	        AccessRight accessRight = AccessRightController.getController().create(accessRightVO, interceptionPoint, db);
		    	        
		    	        List accessRightRoles = (List)accessRightMap.get("accessRightRoles");
		    	        if(accessRightRoles != null)
			            {
		    	        	Iterator accessRightRolesIterator = accessRightRoles.iterator();
				            while(accessRightRolesIterator.hasNext())
				            {
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
        }
        catch(Throwable e)
        {
        	statusBean.setStatus(false);
        	statusBean.setMessage("En error occurred when we tried to create a new content:" + e.getMessage());
            logger.error("En error occurred when we tried to create a new content:" + e.getMessage(), e);
        }
        
        updateCaches();

        return statusBean;
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
