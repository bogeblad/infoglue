/* ===============================================================================
 *
 * Part of the InfoGlue SiteNode Management Platform (www.infoglue.org)
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

package org.infoglue.cms.applications.structuretool.wizards.actions;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.structuretool.actions.CreateSiteNodeAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * This action represents the last step in the create SiteNode wizard. It creates the SiteNode and does all other neccessairy steps
 * defined by the requestor.
 */

public class CreateSiteNodeWizardFinishAction extends CreateSiteNodeWizardAbstractAction
{
	private static final long serialVersionUID 	= 1L;
	
	private ConstraintExceptionBuffer ceb 		= null;
	private String returnAddress 				= "CreateSiteNodeWizardFinish.action";

	private final static Logger logger = Logger.getLogger(CreateSiteNodeWizardFinishAction.class.getName());

	public CreateSiteNodeWizardFinishAction()
	{
		this.ceb = new ConstraintExceptionBuffer();			
	}
	
	
	public String doExecute() throws Exception
	{
		try
		{
			CreateSiteNodeWizardInfoBean createSiteNodeWizardInfoBean = getCreateSiteNodeWizardInfoBean();
			if(createSiteNodeWizardInfoBean.getParentSiteNodeId() == null)
			{
				return "stateLocation";
			}
	
			createSiteNodeWizardInfoBean.getSiteNodeVO().setCreatorName(this.getInfoGluePrincipal().getName());
			this.ceb = createSiteNodeWizardInfoBean.getSiteNodeVO().validate();
			
			if(!this.ceb.isEmpty())
			{
				return "inputSiteNode";
			}

			Integer repositoryId = createSiteNodeWizardInfoBean.getRepositoryId();
			SiteNodeVO newSiteNodeVO = null;
			
	    	Database db = CastorDatabaseService.getDatabase();
	        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

	        beginTransaction(db);

	        try
	        {
	        	createSiteNodeWizardInfoBean.getSiteNodeVO().setIsBranch(new Boolean(true));
	            SiteNode newSiteNode = SiteNodeControllerProxy.getSiteNodeControllerProxy().acCreate(this.getInfoGluePrincipal(), createSiteNodeWizardInfoBean.getParentSiteNodeId(), createSiteNodeWizardInfoBean.getSiteNodeTypeDefinitionId(), repositoryId, createSiteNodeWizardInfoBean.getSiteNodeVO(), db);            
	            newSiteNodeVO = newSiteNode.getValueObject();
	            SiteNodeController.getController().createSiteNodeMetaInfoContent(db, newSiteNode, repositoryId, this.getInfoGluePrincipal(), createSiteNodeWizardInfoBean.getPageTemplateContentId());
	            
	            commitTransaction(db);
	        }
	        catch(Exception e)
	        {
	            logger.error("An error occurred so we should not completes the transaction:" + e, e);
	            rollbackTransaction(db);
	            throw new SystemException(e.getMessage());
	        }
			    						
			String returnAddress = createSiteNodeWizardInfoBean.getReturnAddress();
			returnAddress = returnAddress.replaceAll("#entityId", createSiteNodeWizardInfoBean.getSiteNodeVO().getId().toString());
			returnAddress = returnAddress.replaceAll("#path", createSiteNodeWizardInfoBean.getSiteNodeVO().getName());

			this.invalidateCreateSiteNodeWizardInfoBean();
			
			this.getResponse().sendRedirect(returnAddress);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return NONE;
	}

	public String doCancel() throws Exception
	{
		try
		{
			CreateSiteNodeWizardInfoBean createSiteNodeWizardInfoBean = getCreateSiteNodeWizardInfoBean();
			
			String cancelAddress = createSiteNodeWizardInfoBean.getCancelAddress();
			
			this.invalidateCreateSiteNodeWizardInfoBean();
		
			this.getResponse().sendRedirect(cancelAddress);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return NONE;
	}

	public void setParentSiteNodeId(Integer parentSiteNodeId)
	{
		getCreateSiteNodeWizardInfoBean().setParentSiteNodeId(parentSiteNodeId);
	}

	public Integer getParentSiteNodeId()
	{
		return getCreateSiteNodeWizardInfoBean().getParentSiteNodeId();
	}

	public void setRepositoryId(Integer repositoryId)
	{
		getCreateSiteNodeWizardInfoBean().setRepositoryId(repositoryId);
	}

	public Integer getRepositoryId() 
	{
		return getCreateSiteNodeWizardInfoBean().getRepositoryId();
	}
	
	public java.lang.String getName()
	{
		return getCreateSiteNodeWizardInfoBean().getSiteNodeVO().getName();
	}

	public void setName(String name)
	{
		getCreateSiteNodeWizardInfoBean().getSiteNodeVO().setName(name);
	}
    	
	public void setPublishDateTime(String publishDateTime)
	{
		getCreateSiteNodeWizardInfoBean().getSiteNodeVO().setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
	}

	public void setExpireDateTime(String expireDateTime)
	{
		getCreateSiteNodeWizardInfoBean().getSiteNodeVO().setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
	}
 
	public SiteNodeVO getSiteNodeVO()
	{
		return getCreateSiteNodeWizardInfoBean().getSiteNodeVO();
	}

	public void setSiteNodeVO(SiteNodeVO siteNodeVO)
	{
		getCreateSiteNodeWizardInfoBean().setSiteNodeVO(siteNodeVO);
	}

	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
	{
		getCreateSiteNodeWizardInfoBean().setSiteNodeTypeDefinitionId(siteNodeTypeDefinitionId);
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}
	
	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setRefreshAddress(String refreshAddress)
	{
		getCreateSiteNodeWizardInfoBean().setReturnAddress(refreshAddress);
	}
	
	public String getRefreshAddress()
	{
		return getCreateSiteNodeWizardInfoBean().getReturnAddress();
	}

}
