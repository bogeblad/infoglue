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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
* This class handles Deleting a repository - by processing it as a thread in a process bean.
* 
* @author Mattias Bogeblad
*/

public class TrashcanController extends BaseController implements Runnable
{
    public final static Logger logger = Logger.getLogger(TrashcanController.class.getName());

    private InfoGluePrincipal principal;
    private ProcessBean processBean;
    private String entity;
    private Integer entityId;
    private Boolean emptyAll;
    private Integer repositoryFilter;
    private Boolean restore = false;
    
    private RepositoryVO repositoryVO;
    
	private TrashcanController(RepositoryVO repositoryVO, InfoGluePrincipal principal, ProcessBean processBean)
	{
		this.principal = principal;
		this.repositoryVO = repositoryVO;
		this.processBean = processBean;
	}

	private TrashcanController(String entity, Integer entityId, Boolean restore, InfoGluePrincipal principal, ProcessBean processBean)
	{
		this.principal = principal;
		this.entity = entity;
		this.entityId = entityId;
		this.restore = restore;
		this.processBean = processBean;
	}

	private TrashcanController(Boolean emptyAll, Integer repositoryFilter, InfoGluePrincipal principal, ProcessBean processBean)
	{
		this.principal = principal;
		this.emptyAll = emptyAll;
		this.repositoryFilter = repositoryFilter;
		this.processBean = processBean;
	}

	/**
	 * Factory method to get object
	 */
	
	public static void deleteEntity(String entity, Integer entityId, InfoGluePrincipal principal, ProcessBean processBean) throws Exception
	{
		TrashcanController trashcanController = new TrashcanController(entity, entityId, false, principal, processBean);
		Thread thread = new Thread(trashcanController);
		thread.start();
	}

	public static void restoreEntity(String entity, Integer entityId, InfoGluePrincipal principal, ProcessBean processBean) throws Exception
	{
		TrashcanController trashcanController = new TrashcanController(entity, entityId, true, principal, processBean);
		Thread thread = new Thread(trashcanController);
		thread.start();
	}

	public static void emptyTrashcan(Integer repositoryFilter, InfoGluePrincipal principal, ProcessBean processBean) throws Exception
	{
		TrashcanController trashcanController = new TrashcanController(true, repositoryFilter, principal, processBean);
		Thread thread = new Thread(trashcanController);
		thread.start();
	}

	public synchronized void run()
	{
		Integer updateEntityId = -1; 

		try
		{
			if(!restore)
			{
				processBean.updateProcess("Deleting " + (entity == null ? " everything" : entity));
				
				if(repositoryVO != null)
				{
					logger.info("Starting Delete Repo Thread....");
					try
					{
						RepositoryController.getController().delete(this.repositoryVO, true, this.principal, processBean);
			
						processBean.setStatus(ProcessBean.FINISHED);
					}
					catch (Exception e) 
					{
						//TODO: Fix this error message better. Support illegal xml-chars
						processBean.setError("Something went wrong with the import. Please consult the logfiles.");
						logger.error("Error in monitor:" + e.getMessage(), e);
					}
				}
				else if(entity != null && !entity.equals("") && entityId != null && !entityId.equals(""))
				{
					if(entity.equalsIgnoreCase("Repository"))
					{
						RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(new Integer(entityId));
						RepositoryController.getController().delete(repositoryVO, true, this.principal, processBean);
					}
					else if(entity.equalsIgnoreCase("Content"))
					{
						ContentController.getContentController().delete(new Integer(entityId), true, this.principal);
					}
					else if(entity.equalsIgnoreCase("SiteNode"))
					{
						SiteNodeController.getController().delete(new Integer(entityId), true, this.principal);
					}
				}
				else if(emptyAll)
				{
					List<RepositoryVO> repositoriesMarkedForDeletion = RepositoryController.getController().getRepositoryVOListMarkedForDeletion(this.principal);
	
					Iterator<RepositoryVO> repositoriesMarkedForDeletionIterator = repositoriesMarkedForDeletion.iterator();
					while(repositoriesMarkedForDeletionIterator.hasNext())
					{
						RepositoryVO repositoryVO = repositoriesMarkedForDeletionIterator.next();
						try
						{
							RepositoryController.getController().delete(repositoryVO, true, this.principal, this.processBean);
						}
						catch (Exception e) 
						{
							logger.error("Could not delete repository[" + repositoryVO.getName() + "]:" + e.getMessage());
							logger.warn("Could not delete repository[" + repositoryVO.getName() + "]:" + e.getMessage(), e);
						}
					}
	
					List<ContentVO> contentsMarkedForDeletion = ContentController.getContentController().getContentVOListMarkedForDeletion(this.repositoryFilter, this.principal, repositoriesMarkedForDeletion);
					List<SiteNodeVO> siteNodesMarkedForDeletion = SiteNodeController.getController().getSiteNodeVOListMarkedForDeletion(this.repositoryFilter, this.principal, repositoriesMarkedForDeletion);
	
					Iterator<SiteNodeVO> siteNodesMarkedForDeletionIterator = siteNodesMarkedForDeletion.iterator();
					while(siteNodesMarkedForDeletionIterator.hasNext())
					{
						SiteNodeVO siteNodeVO = siteNodesMarkedForDeletionIterator.next();
						try
						{
							SiteNodeControllerProxy.getSiteNodeControllerProxy().acDelete(this.principal, siteNodeVO, true);
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
							ContentControllerProxy.getController().acDelete(this.principal, contentVO, true);
						}
						catch (Exception e) 
						{
							logger.error("Could not delete content[" + contentVO.getName() + "]:" + e.getMessage());
							logger.warn("Could not delete content[" + contentVO.getName() + "]:" + e.getMessage(), e);
						}
					}
				}
			}
			else
			{
				if(entity != null && !entity.equals("") && entityId != null && !entityId.equals(""))
				{
					if(entity.equalsIgnoreCase("Repository"))
					{
						RepositoryController.getController().restoreRepository(new Integer(entityId), this.principal);
					}
					else if(entity.equalsIgnoreCase("Content"))
					{
						ContentController.getContentController().restoreContent(new Integer(entityId), this.principal);
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
						SiteNodeController.getController().restoreSiteNode(new Integer(entityId), this.principal);
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
			}
			
			processBean.setRedirectUrl("Operation complete..", "" + "Trashcan.action?updateEntityId=" + updateEntityId);
			processBean.setStatus(ProcessBean.REDIRECTED);
		}
		catch (Exception e) 
		{
			processBean.setError("Something went wrong with the import. Please consult the logfiles.");
			logger.error("Error in monitor:" + e.getMessage(), e);
		}
	}
	
    public BaseEntityVO getNewVO()
    {
        return null;
    }

}
