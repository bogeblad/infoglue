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

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
* This class handles Deleting a repository - by processing it as a thread in a process bean.
* 
* @author Mattias Bogeblad
*/

public class DeleteRepositoryController extends BaseController implements Runnable
{
    public final static Logger logger = Logger.getLogger(DeleteRepositoryController.class.getName());

    private InfoGluePrincipal principal;
    private ProcessBean processBean;
    private RepositoryVO repositoryVO;
    private Boolean byPassTrashcan;
    private Boolean deleteByForce;
    
	private DeleteRepositoryController(RepositoryVO repositoryVO, InfoGluePrincipal principal, Boolean byPassTrashcan, Boolean deleteByForce, ProcessBean processBean)
	{
		this.principal = principal;
		this.repositoryVO = repositoryVO;
		this.byPassTrashcan = byPassTrashcan;
		this.deleteByForce = deleteByForce;
		this.processBean = processBean;
	}
	
	/**
	 * Factory method to get object
	 */
	
	public static void deleteRepositories(RepositoryVO repositoryVO, InfoGluePrincipal principal, Boolean byPassTrashcan, Boolean deleteByForce, ProcessBean processBean) throws Exception
	{
		DeleteRepositoryController deleteController = new DeleteRepositoryController(repositoryVO, principal, byPassTrashcan, deleteByForce, processBean);
		Thread thread = new Thread(deleteController);
		thread.start();
	}
	   	
	public synchronized void run()
	{
		logger.info("Starting Delete Repo Thread....");
		try
		{
			processBean.setStatus(ProcessBean.RUNNING);

			RepositoryController.getController().markForDelete(this.repositoryVO, this.principal.getName(), this.deleteByForce, this.principal, processBean);
			
			if(CmsPropertyHandler.getDoNotUseTrashcanForRepositories() || this.byPassTrashcan)
				RepositoryController.getController().delete(this.repositoryVO, this.deleteByForce, this.principal, processBean);

			processBean.setRedirectUrl("Deletion complete..", "" + "ViewListRepository.action?title=Repositories");
			processBean.setStatus(ProcessBean.REDIRECTED);
		}
		catch (ConstraintException ce) 
		{
			//processBean.setError("Contraint: " + ce.getMessage());
			if(ce.getErrorCode().equals("3305") || ce.getErrorCode().equals("3405"))
				processBean.setRedirectUrl("Problem deleting repo 2 - redirecting...", "" + "DeleteRepository!input.action?repositoryId=" + this.repositoryVO.getId() + "&byPassTrashcan=" + byPassTrashcan + "&message=" + ce.getExtraInformation());
			else
				processBean.setRedirectUrl("Problem deleting repo 2 - redirecting...", "" + "DeleteRepository!input.action?repositoryId=" + this.repositoryVO.getId() + "&byPassTrashcan=" + byPassTrashcan + "&message=" + ce.getMessage());
				
			processBean.setStatus(ProcessBean.REDIRECTED);
			//ce.printStackTrace();
			logger.warn("Error in monitor:" + ce.getMessage(), ce);
		}
		catch (Exception e) 
		{
			//TODO: Fix this error message better. Support illegal xml-chars
			processBean.setError("Something went wrong. Please consult the logfiles.");
//			processBean.setRedirectUrl("Redirecting...", "" + "DeleteRepository!input.action?repositoryId=" + this.repositoryVO.getId());
//			processBean.setStatus(ProcessBean.REDIRECTED);
			logger.error("Error in monitor:" + e.getMessage(), e);
		}
	}
	
    public BaseEntityVO getNewVO()
    {
        return null;
    }

}
