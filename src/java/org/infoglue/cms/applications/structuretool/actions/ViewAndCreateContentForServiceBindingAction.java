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

package org.infoglue.cms.applications.structuretool.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This action first checks if there is a bound content linked - if not one is created in a special folder.
 * The content is then shown to the user for editing.
 */

public class ViewAndCreateContentForServiceBindingAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewAndCreateContentForServiceBindingAction.class.getName());

    private Integer siteNodeVersionId;
    private Integer repositoryId;
    //private Integer availableServiceBindingId;
    private Integer serviceDefinitionId;
    private Integer bindingTypeId;
    private ConstraintExceptionBuffer ceb;
   	private Integer siteNodeId;
   	private ServiceDefinitionVO singleServiceDefinitionVO;
   	//private String qualifyerXML;
	private String tree;	
	private List repositories;
	private ContentVO contentVO = new ContentVO();
	private Integer languageId = null;
	private Integer metaInfoContentTypeDefinitionId = null;
	private String changeStateToWorking = null;
	
   	//private ServiceBindingVO serviceBindingVO = null;
   
  /*
  	public ViewAndCreateContentForServiceBindingAction()
	{
		this(new ServiceBindingVO());
	}
	
	public ViewAndCreateContentForServiceBindingAction(ServiceBindingVO serviceBindingVO)
	{
		this.serviceBindingVO = serviceBindingVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	
	*/
	
	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	/*
	public void setAvailableServiceBindingId(Integer availableServiceBindingId)
	{
		this.availableServiceBindingId = availableServiceBindingId;
	}
	*/

	public void setServiceDefinitionId(Integer serviceDefinitionId)
	{
		this.serviceDefinitionId = serviceDefinitionId;
	}
/*
	public void setBindingTypeId(Integer bindingTypeId)
	{
		this.serviceBindingVO.setBindingTypeId(bindingTypeId);
	}

	public void setPath(String path)
	{
		this.serviceBindingVO.setPath(path);
	}
*/	
	public Integer getSiteNodeVersionId()
	{
		return this.siteNodeVersionId;
	}

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}
	    
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	/*
	public Integer getAvailableServiceBindingId()
	{
		return this.availableServiceBindingId;
	}
	*/
    
	public Integer getServiceDefinitionId()
	{
		return this.singleServiceDefinitionVO.getServiceDefinitionId();
	}
	
	public Integer getBindingTypeId()
	{
		return this.bindingTypeId;
	}

	/*
	public void setServiceBindingId(Integer serviceBindingId)
	{
		this.serviceBindingVO.setServiceBindingId(serviceBindingId);
	}
*/
	public ServiceDefinitionVO getSingleServiceDefinitionVO()
	{
		return this.singleServiceDefinitionVO;
	}
	
	public String getTree()
	{
		return tree;
	}

	public void setTree(String string)
	{
		tree = string;
	}
	
	public String getCurrentAction()
	{
		return "ViewAndCreateContentForServiceBinding.action";
	}
	
	/**
	 * We first checks if there is a bound content linked - if not one is created in a special folder and
	 * a new service binding is created to it. The content is then shown to the user for editing. Most of this method should 
	 * be moved to an controller.
	 */
	
    public String doExecute() throws Exception
    {		
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        beginTransaction(db);

        try
        {
            Language masterLanguage = LanguageController.getController().getMasterLanguage(db, this.repositoryId);
    		this.languageId = masterLanguage.getLanguageId();

    		ContentTypeDefinition metaInfoContentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithName("Meta info", db);
    		this.metaInfoContentTypeDefinitionId = metaInfoContentTypeDefinition.getId();
    		
    		SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(this.siteNodeId, db);

    		if(this.changeStateToWorking != null && this.changeStateToWorking.equalsIgnoreCase("true"))
    		{
    			SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, this.siteNodeId);
    			if(!siteNodeVersion.getStateId().equals(SiteNodeVersionVO.WORKING_STATE))
    			{
	    			List events = new ArrayList();
		    		SiteNodeStateController.getController().changeState(siteNodeVersion.getId(), SiteNodeVersionVO.WORKING_STATE, "Auto", true, this.getInfoGluePrincipal(), this.siteNodeId, db, events);
    			}
    		}
    		
    		Content metaInfoContent = null;
    		if(siteNode.getMetaInfoContentId() != null && siteNode.getMetaInfoContentId().intValue() > -1)
    		    metaInfoContent = ContentController.getContentController().getContentWithId(siteNode.getMetaInfoContentId(), db);
            
            if(metaInfoContent == null)
            {
            	siteNode.setMetaInfoContentId(null);
    		    this.contentVO = SiteNodeController.getController().createSiteNodeMetaInfoContent(db, siteNode, this.repositoryId, this.getInfoGluePrincipal(), null).getValueObject();
            	siteNode.setMetaInfoContentId(this.contentVO.getId());
                logger.error("The site node must have a meta information bound. We tried to recreate it. Old info was lost.");
            } 
            else
            {
                if(!metaInfoContent.getValueObject().getContentTypeDefinitionId().equals(metaInfoContentTypeDefinition.getId()))
                	metaInfoContent.setContentTypeDefinition((ContentTypeDefinitionImpl)metaInfoContentTypeDefinition);
                this.contentVO = metaInfoContent.getValueObject();
            }
            
            this.languageId = getInitialLanguageVO(this.contentVO.getId(), db).getId();
            
    		commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return "success";
    }
       
	/**
	 * We first checks if there is a bound content linked - if not one is created in a special folder and
	 * a new service binding is created to it. The content is then shown to the user for editing. Most of this method should 
	 * be moved to an controller.
	 */
	
    public String doInline() throws Exception
    {		
    	doExecute();
    	
    	return "successInline";
    }
    
	public LanguageVO getInitialLanguageVO(Integer contentId, Database db) throws Exception
	{
		Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

	    String initialLanguageId = ps.getString("content_" + contentId + "_initialLanguageId");
	    ContentVO parentContentVO = ContentController.getContentController().getParentContent(contentId, db); 
	    while((initialLanguageId == null || initialLanguageId.equalsIgnoreCase("-1")) && parentContentVO != null)
	    {
	    	initialLanguageId = ps.getString("content_" + parentContentVO.getId() + "_initialLanguageId");
		    parentContentVO = ContentController.getContentController().getParentContent(parentContentVO.getId(), db); 
	    }
	    
	    if(initialLanguageId != null && !initialLanguageId.equals("") && !initialLanguageId.equals("-1"))
	        return LanguageController.getController().getLanguageVOWithId(new Integer(initialLanguageId), db);
	    else
	        return LanguageController.getController().getMasterLanguage(repositoryId, db);
	}

	public List getRepositories()
	{
		return repositories;
	}
	
	public Integer getContentId()
	{
		return this.contentVO.getId();
	}
	
	public Integer getLanguageId()
	{
		return this.languageId;
	}

	public String getChangeStateToWorking()
	{
		return changeStateToWorking;
	}

	public void setChangeStateToWorking(String changeStateToWorking)
	{
		this.changeStateToWorking = changeStateToWorking;
	}
}
