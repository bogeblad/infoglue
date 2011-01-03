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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
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
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
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

public class RemoteSiteNodeServiceImpl extends RemoteInfoGlueService
{
    private final static Logger logger = Logger.getLogger(RemoteSiteNodeServiceImpl.class.getName());

	/**
	 * The principal executing the workflow.
	 */
	private InfoGluePrincipal principal;

    private static SiteNodeControllerProxy siteNodeControllerProxy = SiteNodeControllerProxy.getSiteNodeControllerProxy();
    private static ContentVersionControllerProxy contentVersionControllerProxy = ContentVersionControllerProxy.getController();
    
    /**
     * Gets a content version from the cms. Very useful for getting the latest working version.
     */
    /*
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
            logger.error("En error occurred when we tried to get the contentVersionVO:" + t.getMessage(), t);
        }
        
        return contentVersionVO;
    }
	*/
    
    /**
     * Inserts a new SiteNode including versions etc.
     */
    /*
    public int createSiteNode(final String principalName, ContentVO contentVO, int parentContentId, int contentTypeDefinitionId, int repositoryId) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return -1;
        }
        
        int newContentId = 0;
        
        logger.info("***************************************");
        logger.info("Creating sitenode through webservice....");
        logger.info("***************************************");
        
        logger.info("parentContentId:" + parentContentId);
        logger.info("contentTypeDefinitionId:" + contentTypeDefinitionId);
        logger.info("repositoryId:" + repositoryId);
        
        try
        {
            initializePrincipal(principalName);

            SiteNode newSiteNode = SiteNodeControllerProxy.getSiteNodeControllerProxy().acCreate(this.principal, this.parentSiteNodeId, this.siteNodeTypeDefinitionId, this.repositoryId, this.siteNodeVO, db);            
            newSiteNodeVO = newSiteNode.getValueObject();
            
            SiteNodeController.getController().createSiteNodeMetaInfoContent(db, newSiteNode, this.repositoryId, this.getInfoGluePrincipal(), this.pageTemplateContentId);
        }
        catch(Exception e)
        {
            logger.error("En error occurred when we tried to create a new content:" + e.getMessage(), e);
        }
        
        updateCaches();
        
        return newContentId;
    }
    */
    
    /**
     * Inserts a new ContentVersion.
     */
    /*
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
            logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        updateCaches();

        return newContentVersionId;
    }
    */
    
 
    /**
     * Inserts one or many new SiteNode including versions etc.
     */
    
    public StatusBean createSiteNodes(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new StatusBean(false, "You are not allowed to talk to this service");
        }

    	StatusBean statusBean = new StatusBean(true, "ok");

        List newSiteNodeIdList = new ArrayList();
        
        logger.info("****************************************");
        logger.info("Creating sitenodes through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            List siteNodes = (List) serializer.deserialize(inputsArray);
	        logger.info("siteNodes:" + siteNodes);

            initializePrincipal(principalName);
	        Iterator siteNodesIterator = siteNodes.iterator();
	        while(siteNodesIterator.hasNext())
	        {
	            Map siteNode = (Map)siteNodesIterator.next();
	            
	            String name 						= (String)siteNode.get("name");
	            Integer siteNodeTypeDefinitionId 	= (Integer)siteNode.get("siteNodeTypeDefinitionId");
	            Integer repositoryId 				= (Integer)siteNode.get("repositoryId");
	            Integer parentSiteNodeId 			= (Integer)siteNode.get("parentSiteNodeId");
	            //String siteNodePath 				= (String)siteNode.get("siteNodePath");
	            Integer pageTemplateContentId 		= (Integer)siteNode.get("pageTemplateContentId");
	            
	            logger.info("name:" + name);
	            logger.info("siteNodeTypeDefinitionId:" + siteNodeTypeDefinitionId);
	            logger.info("repositoryId:" + repositoryId);
	            logger.info("parentSiteNodeId:" + parentSiteNodeId);
	            //logger.info("contentPath:" + contentPath);
	            
	            /*
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
	            */
	            
	           	SiteNodeVO siteNodeVO = new SiteNodeVO();
	           	siteNodeVO.setName(name);
	           	siteNodeVO.setSiteNodeTypeDefinitionId(siteNodeTypeDefinitionId);
	           	siteNodeVO.setRepositoryId(repositoryId);
	           	siteNodeVO.setParentSiteNodeId(parentSiteNodeId);
	           	siteNodeVO.setIsBranch(true);
	            
	            if(siteNodeVO.getCreatorName() == null)
	            	siteNodeVO.setCreatorName(this.principal.getName());
	            
	            Database db = CastorDatabaseService.getDatabase();
	            ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

	            beginTransaction(db);

	            try
	            {
		            SiteNode newSiteNode = siteNodeControllerProxy.acCreate(this.principal, siteNodeVO.getParentSiteNodeId(), siteNodeVO.getSiteNodeTypeDefinitionId(), siteNodeVO.getRepositoryId(), siteNodeVO, db);
		            SiteNodeVO newSiteNodeVO = newSiteNode.getValueObject();
	            	if(newSiteNode != null)
	            		statusBean.getCreatedBeans().add(new CreatedEntityBean(SiteNodeVO.class.getName(), new Long(newSiteNodeVO.getId())));
	    	        
		            Content newMetaInfoContent = SiteNodeController.getController().createSiteNodeMetaInfoContent(db, newSiteNode, siteNodeVO.getRepositoryId(), this.principal, pageTemplateContentId);
	            	if(newMetaInfoContent != null)
	            		statusBean.getCreatedBeans().add(new CreatedEntityBean(ContentVO.class.getName(), new Long(newMetaInfoContent.getId())));
	                
	    	        //Should we also change state on newly created content version?
		            /*
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
					*/
		            
	                commitTransaction(db);
	            }
	            catch(Exception e)
	            {
	                logger.error("An error occurred so we should not completes the transaction:" + e, e);
	                rollbackTransaction(db);
	                throw new SystemException(e.getMessage());
	            }	            
	        }
	        logger.info("Done with site nodes..");
        }
        catch(Throwable e)
        {
        	statusBean.setStatus(false);
            logger.error("En error occurred when we tried to create a new siteNode:" + e.getMessage(), e);
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
