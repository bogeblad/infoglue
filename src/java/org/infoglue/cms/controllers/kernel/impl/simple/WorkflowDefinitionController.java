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

import java.util.List;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.workflow.WorkflowDefinition;
import org.infoglue.cms.entities.workflow.WorkflowDefinitionVO;
import org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.util.CacheController;

/**
 * @author Mattias Bogeblad
 */

public class WorkflowDefinitionController extends BaseController
{

	/**
	 * Factory method
	 */

	public static WorkflowDefinitionController getController()
	{
		return new WorkflowDefinitionController();
	}

    public WorkflowDefinitionVO getWorkflowDefinitionVOWithId(Integer workflowDefinitionId) throws SystemException, Bug
    {
		return (WorkflowDefinitionVO) getVOWithId(WorkflowDefinitionImpl.class, workflowDefinitionId);
    }

    public WorkflowDefinition getWorkflowDefinitionWithId(Integer workflowDefinitionId, Database db) throws SystemException, Bug
    {
		return (WorkflowDefinition) getObjectWithId(WorkflowDefinitionImpl.class, workflowDefinitionId, db);
    }

	/**
	 * Returns the Workflow Definition with the given name.
	 *
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public WorkflowDefinitionVO getWorkflowDefinitionVOWithName(String name) throws SystemException, Bug
	{
		WorkflowDefinitionVO workflowDefinitionVO = null;

		Database db = CastorDatabaseService.getDatabase();

		try
		{
			beginTransaction(db);

			WorkflowDefinition workflowDefinition = getWorkflowDefinitionWithName(name, db);
			if(workflowDefinition != null)
				workflowDefinitionVO = workflowDefinition.getValueObject();

			commitTransaction(db);
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return workflowDefinitionVO;
	}

	/**
	 * Returns the Workflow Definition with the given name fetched within a given transaction.
	 *
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public WorkflowDefinition getWorkflowDefinitionWithName(String name, Database db) throws SystemException, Bug
	{
		WorkflowDefinition workflowDefinition = null;

		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl f WHERE f.name = $1");
			oql.bind(name);

	    	QueryResults results = oql.execute();
			if (results.hasMore())
			{
				workflowDefinition = (WorkflowDefinition)results.next();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a named WorkflowDefinition. Reason:" + e.getMessage(), e);
		}

		return workflowDefinition;
	}

    public List getWorkflowDefinitionVOList() throws SystemException, Bug
    {
		
        String key = "workflowDefinitionVOList";
		List cachedWorkflowDefinitionVOList = (List)CacheController.getCachedObject("workflowCache", key);
		if(cachedWorkflowDefinitionVOList != null)
		{
			return cachedWorkflowDefinitionVOList;
		}
		
        List workflowDefinitionVOList = getAllVOObjects(WorkflowDefinitionImpl.class, "workflowDefinitionId");

		CacheController.cacheObject("workflowCache", key, workflowDefinitionVOList);

		return workflowDefinitionVOList;
    }
    

    public WorkflowDefinitionVO create(WorkflowDefinitionVO workflowDefinitionVO) throws ConstraintException, SystemException
    {
        WorkflowDefinition workflowDefinition = new WorkflowDefinitionImpl();
        workflowDefinition.setValueObject(workflowDefinitionVO);
        workflowDefinition = (WorkflowDefinition) createEntity(workflowDefinition);
        return workflowDefinition.getValueObject();
    }

    public void delete(WorkflowDefinitionVO workflowDefinitionVO) throws ConstraintException, SystemException
    {
    	deleteEntity(WorkflowDefinitionImpl.class, workflowDefinitionVO.getWorkflowDefinitionId());
    }

    public WorkflowDefinitionVO update(WorkflowDefinitionVO workflowDefinitionVO) throws ConstraintException, SystemException
    {
    	return (WorkflowDefinitionVO) updateEntity(WorkflowDefinitionImpl.class, workflowDefinitionVO);
    }

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new WorkflowDefinitionVO();
	}
}
