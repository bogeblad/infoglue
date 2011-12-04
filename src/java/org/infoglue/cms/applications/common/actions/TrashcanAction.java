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

package org.infoglue.cms.applications.common.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;

import webwork.action.Action;


/** 
 * This class contains methods to handle the trashcan and the item's in it.
 */

public class TrashcanAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(TrashcanAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	//private static SubscriptionController subscriptionsController = SubscriptionController.getController();
	private List<RepositoryVO> repositoriesMarkedForDeletion = new ArrayList<RepositoryVO>();
	private List<ContentVO> contentsMarkedForDeletion = new ArrayList<ContentVO>();
	private List<SiteNodeVO> siteNodesMarkedForDeletion = new ArrayList<SiteNodeVO>();
	
	private Integer repositoryFilter = null;
	
	private String entity = "";
	private Integer entityId = -1;
	private Boolean updateParent = false;	
	private Integer updateEntityId = -1;
	
	protected String doExecute() throws Exception
    {
		this.repositoriesMarkedForDeletion = RepositoryController.getController().getRepositoryVOListMarkedForDeletion();
		this.contentsMarkedForDeletion = ContentController.getContentController().getContentVOListMarkedForDeletion(repositoryFilter);
		this.siteNodesMarkedForDeletion = SiteNodeController.getController().getSiteNodeVOListMarkedForDeletion(repositoryFilter);
		
		return Action.SUCCESS;
    }

	public String doRestore() throws Exception
    {
		if(entity != null && !entity.equals("") && entityId != null && !entityId.equals(""))
		{
			updateParent = true;
			if(entity.equalsIgnoreCase("Repository"))
			{
				RepositoryController.getController().restoreRepository(new Integer(entityId), getInfoGluePrincipal());
			}
			else if(entity.equalsIgnoreCase("Content"))
			{
				ContentController.getContentController().restoreContent(new Integer(entityId), getInfoGluePrincipal());
				try 
				{
					updateEntityId = ContentController.getContentController().getContentVOWithId(new Integer(entityId)).getParentContentId();
				}
				catch (Exception e) 
				{
					logger.error("Error getting parent content for update:" + e.getMessage());
				}
			}
			else if(entity.equalsIgnoreCase("SiteNode"))
			{
				SiteNodeController.getController().restoreSiteNode(new Integer(entityId), getInfoGluePrincipal());
				try 
				{
					updateEntityId = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId)).getParentSiteNodeId();
				}
				catch (Exception e) 
				{
					logger.error("Error getting parent page for update:" + e.getMessage());
				}
			}
		}
		
		return doExecute();
    }

	public String doDelete() throws Exception
    {
		validateSecurityCode();
		
		if(entity != null && !entity.equals("") && entityId != null && !entityId.equals(""))
		{
			if(entity.equalsIgnoreCase("Repository"))
				RepositoryController.getController().delete(new Integer(entityId), true, getInfoGluePrincipal());
			else if(entity.equalsIgnoreCase("Content"))
				ContentController.getContentController().delete(new Integer(entityId), true, getInfoGluePrincipal());
			else if(entity.equalsIgnoreCase("SiteNode"))
				SiteNodeController.getController().delete(new Integer(entityId), true, getInfoGluePrincipal());
		}
		
		return doExecute();
    }

	public String doEmpty() throws Exception
    {
		validateSecurityCode();

		this.repositoriesMarkedForDeletion = RepositoryController.getController().getRepositoryVOListMarkedForDeletion();
		this.contentsMarkedForDeletion = ContentController.getContentController().getContentVOListMarkedForDeletion(this.repositoryFilter);
		this.siteNodesMarkedForDeletion = SiteNodeController.getController().getSiteNodeVOListMarkedForDeletion(this.repositoryFilter);

		Iterator<SiteNodeVO> siteNodesMarkedForDeletionIterator = siteNodesMarkedForDeletion.iterator();
		while(siteNodesMarkedForDeletionIterator.hasNext())
		{
			SiteNodeVO siteNodeVO = siteNodesMarkedForDeletionIterator.next();
			try
			{
				SiteNodeControllerProxy.getSiteNodeControllerProxy().acDelete(getInfoGluePrincipal(), siteNodeVO);
			}
			catch (Exception e) 
			{
				logger.error("Could not delete page[" + siteNodeVO.getName() + "]:" + e.getMessage());
				logger.warn("Could not delete page[" + siteNodeVO.getName() + "]:" + e.getMessage(), e);
			}
		}

		Iterator<ContentVO> contentsMarkedForDeletionIterator = contentsMarkedForDeletion.iterator();
		while(contentsMarkedForDeletionIterator.hasNext())
		{
			ContentVO contentVO = contentsMarkedForDeletionIterator.next();
			try
			{
				ContentControllerProxy.getController().acDelete(getInfoGluePrincipal(), contentVO);
			}
			catch (Exception e) 
			{
				logger.error("Could not delete content[" + contentVO.getName() + "]:" + e.getMessage());
				logger.warn("Could not delete content[" + contentVO.getName() + "]:" + e.getMessage(), e);
			}
		}

		Iterator<RepositoryVO> repositoriesMarkedForDeletionIterator = repositoriesMarkedForDeletion.iterator();
		while(repositoriesMarkedForDeletionIterator.hasNext())
		{
			RepositoryVO repositoryVO = repositoriesMarkedForDeletionIterator.next();
			try
			{
				RepositoryController.getController().delete(repositoryVO, true, getInfoGluePrincipal());
			}
			catch (Exception e) 
			{
				logger.error("Could not delete repository[" + repositoryVO.getName() + "]:" + e.getMessage());
				logger.warn("Could not delete repository[" + repositoryVO.getName() + "]:" + e.getMessage(), e);
			}
		}
		
		return doExecute();
    }

	public List<RepositoryVO> getRepositoriesMarkedForDeletion()
	{
		return repositoriesMarkedForDeletion;
	}

	public List<ContentVO> getContentsMarkedForDeletion()
	{
		return contentsMarkedForDeletion;
	}

	public List<SiteNodeVO> getSiteNodesMarkedForDeletion()
	{
		return siteNodesMarkedForDeletion;
	}

	public String getEntity()
	{
		return entity;
	}

	public Integer getEntityId()
	{
		return entityId;
	}

	public void setEntity(String entity)
	{
		this.entity = entity;
	}

	public void setEntityId(Integer entityId)
	{
		this.entityId = entityId;
	}
    
	public Integer getRepositoryFilter()
	{
		return repositoryFilter;
	}

	public void setRepositoryFilter(Integer repositoryFilter)
	{
		this.repositoryFilter = repositoryFilter;
	}

	public Boolean getUpdateParent() 
	{
		return updateParent;
	}

	public Integer getUpdateEntityId() 
	{
		return updateEntityId;
	}

}