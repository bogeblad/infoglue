/* ===============================================================================
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.mydesktop.WorkflowActionVO;
import org.infoglue.cms.entities.mydesktop.WorkflowVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.cms.util.workflow.WorkflowFacade;
import org.infoglue.deliver.util.CacheController;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.AbstractWorkflow;
import com.opensymphony.workflow.WorkflowException;

/**
 * This controller acts as the api towards the OSWorkflow Workflow-engine.
 * @author Mattias Bogeblad
 * @author <a href="mailto:mattias.bogeblad@modul1.se">Mattias Bogeblad</a>
 */
public class WorkflowController extends BaseController
{
    private final static Logger logger = Logger.getLogger(UserPropertiesController.class.getName());

	private static final WorkflowController controller = new WorkflowController();

	private static SessionFactory hibernateSessionFactory;
	
	static
	{
		try
		{
			hibernateSessionFactory = new Configuration().configure().buildSessionFactory();
		}
		catch (HibernateException e)
		{
			logger.error("An exception occurred when we tried to initialize the hibernateSessionFactory", e);
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * Returns the WorkflowController singleton
	 * @return a reference to a WorkflowController
	 */
	public static WorkflowController getController()
	{
		return controller;
	}

	private WorkflowController() {}

	/**
	 * TODO: move; used by tests + CreateWorkflowInstanceAction 
	 */
	public static Map createWorkflowParameters(final HttpServletRequest request)
	{
		final Map parameters = new HashMap();
		parameters.putAll(request.getParameterMap());
		parameters.put("request", request);
		return parameters;
	}

	/**
	 * @param principal the user principal representing the desired user
	 * @param name the name of the workflow to create.
	 * @param actionId the ID of the initial action
	 * @param inputs the inputs to the workflow
	 * @return a WorkflowVO representing the newly created workflow instance
	 * @throws SystemException if an error occurs while initiaizing the workflow
	 */
	public WorkflowVO initializeWorkflow(InfoGluePrincipal principal, String name, int actionId, Map inputs) throws SystemException
	{
		WorkflowVO workflowVO = null;
		
		try
		{
			Session session = null;
			net.sf.hibernate.Transaction tx = null;

			try
			{
				session = hibernateSessionFactory.openSession();
				tx = session.beginTransaction();

				if(getIsAccessApproved(name, principal))
				{
					WorkflowFacade wf = new WorkflowFacade(principal, name, actionId, inputs, hibernateSessionFactory, session);
					workflowVO = wf.createWorkflowVO();

					session.flush();

					tx.commit();
				}
				else
				{
					throw new Bug("You are not allowed to create " + name + " workflows.");
				}
			}
			catch (Exception e) 
			{
				logger.error("An error occurred when we tries to run initializeWorkflow():" + e.getMessage(), e);
				try
				{
					tx.rollback();
				}
				catch (HibernateException he)
				{
					logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
				}
				restoreSessionFactory(e);
			}
			finally 
			{
				try
				{
					session.close();
			    } 
				catch (HibernateException e)
				{
					logger.error("An error occurred when we tries to close:" + e.getMessage(), e);
				}
			}
		}
		catch (Exception e)
		{
			throw new SystemException(e);
		}			
			
		return workflowVO;
	}

	public WorkflowVO getWorkflow(String workflowName, InfoGluePrincipal principal) throws SystemException, Exception
	{
		WorkflowVO workflow = null;
		
		List<WorkflowVO> workflows = getAvailableWorkflowVOList(principal);
		Iterator<WorkflowVO> workflowsIterator = workflows.iterator();
		while(workflowsIterator.hasNext())
		{
			WorkflowVO workflowVO = workflowsIterator.next();
			
			String fromEncoding = CmsPropertyHandler.getAssetKeyFromEncoding();
			if(fromEncoding == null)
				fromEncoding = "iso-8859-1";
			
			String toEncoding = CmsPropertyHandler.getAssetKeyToEncoding();
			if(toEncoding == null)
				toEncoding = "utf-8";
				
			String encodedName = new String(workflowName.getBytes(fromEncoding), toEncoding);
			System.out.println("" + workflowVO.getName() + "=" + workflowName + "/" + encodedName);
			
			if(workflowVO.getName().equals(workflowName) || workflowVO.getName().equals(encodedName))
			{
				workflow = workflowVO;
				break;
			}
		}
		return workflow;
	}
	
	public WorkflowVO getCurrentWorkflow(Long workflowId, InfoGluePrincipal principal) throws SystemException
	{
		WorkflowVO workflow = null;
		
		List<WorkflowVO> workflows = getCurrentWorkflowVOList(principal);
		System.out.println("workflows:" + workflows);
		if(workflows != null)
		{
			Iterator<WorkflowVO> workflowsIterator = workflows.iterator();
			while(workflowsIterator.hasNext())
			{
				WorkflowVO workflowVO = workflowsIterator.next();
				if(workflowVO.getWorkflowId().longValue() == workflowId.longValue())
				{
					workflow = workflowVO;
					break;
				}
			}
		}
		return workflow;
	}

	/**
	 * Returns a list of all available workflows, i.e., workflows defined in workflows.xml
	 * @param userPrincipal a user principal
	 * @return a list WorkflowVOs representing available workflows
	 */
	public List<WorkflowVO> getAvailableWorkflowVOList(InfoGluePrincipal userPrincipal) throws SystemException
	{
		final List<WorkflowVO> accessibleWorkflows = new ArrayList<WorkflowVO>();

		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();

			tx = session.beginTransaction();
			
			WorkflowFacade wf = new WorkflowFacade(userPrincipal, hibernateSessionFactory, session);
			final List<WorkflowVO> allWorkflows = wf.getDeclaredWorkflows();
			
			for(final Iterator<WorkflowVO> i = allWorkflows.iterator(); i.hasNext(); )
			{
				final WorkflowVO workflowVO = i.next();
				if(getIsAccessApproved(workflowVO.getName(), userPrincipal))
				{
					accessibleWorkflows.add(workflowVO);
				}
			}
			
			session.flush();
			
			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to execute getAvailableWorkflowVOList():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction():" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}
			
		return accessibleWorkflows;
	}

	/**
	 * This method returns true if the user should have access to the contentTypeDefinition sent in.
	 */
    
	public boolean getIsAccessApproved(String workflowName, InfoGluePrincipal infoGluePrincipal) throws SystemException
	{
	    final String protectWorkflows = CmsPropertyHandler.getProtectWorkflows();
	    if(protectWorkflows == null || !protectWorkflows.equalsIgnoreCase("true"))
	    {
	    	return true;
	    }
	    	
		logger.info("getIsAccessApproved for " + workflowName + " AND " + infoGluePrincipal);
		boolean hasAccess = false;
    	
		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);

		try
		{ 
			hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Workflow.Create", workflowName);
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
    
		return hasAccess;
	}

	/**
	 * Returns current workflows, i.e., workflows that are active.
	 * @param userPrincipal a user principal
	 * @return a list of WorkflowVOs representing all active workflows
	 * @throws SystemException if an error occurs while finding the current workflows
	 */
	public List<WorkflowVO> getCurrentWorkflowVOList(InfoGluePrincipal userPrincipal) throws SystemException
	{
		List<WorkflowVO> list = new ArrayList<WorkflowVO>();
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();

			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(userPrincipal, hibernateSessionFactory, session);
			list = wf.getActiveWorkflows();
			
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to execute getCurrentWorkflowVOList():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
	        
		}
		
		return list;
	}


	/**
	 * Returns the workflows owned by the specified principal.
	 * 
	 * @param userPrincipal a user principal.
	 * @return a list of WorkflowVOs owned by the principal.
	 * @throws SystemException if an error occurs while finding the workflows
	 */
	public List getMyCurrentWorkflowVOList(InfoGluePrincipal userPrincipal) throws SystemException
	{
		List list = new ArrayList();
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();

			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(userPrincipal, hibernateSessionFactory, session);
			list = wf.getMyActiveWorkflows(userPrincipal);
			
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to execute getMyCurrentWorkflowVOList():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}
		
		return list;

		//return new WorkflowFacade(userPrincipal, true).getMyActiveWorkflows(userPrincipal);
	}
	

	/**
	 * Invokes an action on a workflow for a given user and request
	 * @param principal the user principal
	 * @param workflowId the ID of the desired workflow
	 * @param actionId the ID of the desired action
	 * @param inputs the inputs to the workflow 
	 * @return a WorkflowVO representing the current state of the workflow identified by workflowId
	 * @throws WorkflowException if a workflow error occurs
	 */
	public WorkflowVO invokeAction(InfoGluePrincipal principal, long workflowId, int actionId, Map inputs) throws WorkflowException
	{
		WorkflowVO workflowVO = null;

		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();

			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(principal, workflowId, hibernateSessionFactory, session);
			wf.doAction(actionId, inputs);

			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to execute invokeAction():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}
			
		try
		{
			session = hibernateSessionFactory.openSession();

			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(principal, workflowId, hibernateSessionFactory, session);

			workflowVO = wf.createWorkflowVO();
			
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to execute invokeAction():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}

		return workflowVO;
	}


	/**
	 * Returns the workflow property set for a particular user and workflow
	 * @return the workflow property set for the workflow with workflowId and the user represented by userPrincipal
	 */
	/*
	public PropertySet getPropertySet(InfoGluePrincipal userPrincipal, long workflowId)
	{
		PropertySet propertySet = null;
		
		try
		{
			WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, false);
			propertySet = wf.getPropertySet();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
		}

		return propertySet;
		//return new WorkflowFacade(userPrincipal, workflowId, false).getPropertySet();
	}
	*/
	
	public PropertySet getPropertySet(InfoGluePrincipal userPrincipal, long workflowId)
	{
		PropertySet propertySet = null;
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();
			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, hibernateSessionFactory, session);
			propertySet = wf.getPropertySet();
		
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to run getHistorySteps():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}

		return propertySet;
		//return new WorkflowFacade(userPrincipal, workflowId, false).getPropertySet();
	}
	

	/**
	 * Returns the workflow property set for a particular user and workflow
	 * @return the workflow property set for the workflow with workflowId and the user represented by userPrincipal
	 */
	public PropertySet getPropertySet(InfoGluePrincipal userPrincipal, long workflowId, Session session)
	{
		PropertySet propertySet = null;
		
		try
		{
			WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, hibernateSessionFactory, session);
			propertySet = wf.getPropertySet();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
		}

		return propertySet;
		//return new WorkflowFacade(userPrincipal, workflowId, false).getPropertySet();
	}

	/**
	 * Returns the contents of the PropertySet for a particular workflow
	 * @param userPrincipal a user principal
	 * @param workflowId the ID of the desired workflow
	 * @return a map containing the contents of the workflow property set
	 */
	/*
	public Map getProperties(InfoGluePrincipal userPrincipal, long workflowId)
	{
		if(logger.isDebugEnabled())
		{
			logger.info("userPrincipal:" + userPrincipal);
			logger.info("workflowId:" + workflowId);
		}
		
		PropertySet propertySet = getPropertySet(userPrincipal, workflowId);
		Map parameters = new HashMap();
		for (Iterator keys = getPropertySet(userPrincipal, workflowId).getKeys().iterator(); keys.hasNext();)
		{
			String key = (String)keys.next();
			parameters.put(key, propertySet.getString(key));
		}

		return parameters;
	}
	*/

	public Map getProperties(InfoGluePrincipal userPrincipal, long workflowId)
	{
		if(logger.isDebugEnabled())
		{
			logger.info("userPrincipal:" + userPrincipal);
			logger.info("workflowId:" + workflowId);
		}
	
		Map parameters = new HashMap();
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();
			tx = session.beginTransaction();


			PropertySet propertySet = getPropertySet(userPrincipal, workflowId, session);
			for (Iterator keys = propertySet.getKeys().iterator(); keys.hasNext();)
			{
				String key = (String)keys.next();
				parameters.put(key, propertySet.getString(key));
			}

			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to run getHistorySteps():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}
			
		return parameters;
	}
	
	
	/**
	 * Returns all history steps for a workflow, i.e., all the steps that have already been performed.
	 * @param userPrincipal a user principal
	 * @param workflowId the ID of the desired workflow
	 * @return a list of WorkflowStepVOs representing all history steps for the workflow with workflowId
	 */
	public List getHistorySteps(InfoGluePrincipal userPrincipal, long workflowId)
	{
		List historySteps = new ArrayList();
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();
			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, hibernateSessionFactory, session);
			historySteps = wf.getHistorySteps();
			
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to run getHistorySteps():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}

		return historySteps;
		//return new WorkflowFacade(userPrincipal, workflowId, true).getHistorySteps();
	}

	/**
	 * Returns all current steps for a workflow, i.e., steps that could be performed in the workflow's current state
	 * @param userPrincipal a user principal
	 * @param workflowId the Id of the desired workflow
	 * @return a list of WorkflowStepVOs representing the current steps of the workflow with workflowId
	 */
	public List getCurrentSteps(InfoGluePrincipal userPrincipal, long workflowId)
	{
		List currentSteps = new ArrayList();
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();
			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, hibernateSessionFactory, session);
			currentSteps = wf.getCurrentSteps();
			
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to run getCurrentSteps():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}

		//WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, true);
		//List currentSteps = wf.getCurrentSteps();
		
		return currentSteps;
	}

	/**
	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
	 * no knowledge of current or history steps at this point.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param workflowId a workflowId
	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
	 */
	public List getAllSteps(InfoGluePrincipal userPrincipal, long workflowId)
	{
		List declaredSteps = new ArrayList();
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();
			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, hibernateSessionFactory, session);
			declaredSteps = wf.getDeclaredSteps();
			
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to run getAllSteps():" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}

		return declaredSteps;
		//return new WorkflowFacade(userPrincipal, workflowId, true).getDeclaredSteps();
	}

	/**
	 * Returns true if the workflow has terminated; false otherwise.
	 * 
	 * @param workflowVO the workflow.
	 * @return true if the workflow has terminated; false otherwise.
	 */
	public boolean hasTerminated(InfoGluePrincipal userPrincipal, long workflowId) throws WorkflowException
	{
		boolean isFinished = false;
		
		Session session = null;
		net.sf.hibernate.Transaction tx = null;

		try
		{
			session = hibernateSessionFactory.openSession();
			tx = session.beginTransaction();

			WorkflowFacade wf = new WorkflowFacade(userPrincipal, workflowId, hibernateSessionFactory, session);
			isFinished = wf.isFinished();
			
			session.flush();

			tx.commit();
		}
		catch (Exception e) 
		{
			logger.error("An error occurred when we tries to run hasTerminated:" + e.getMessage(), e);
			try
			{
				tx.rollback();
			}
			catch (HibernateException he)
			{
				logger.error("An error occurred when we tries to rollback transaction:" + he.getMessage(), he);
			}
			restoreSessionFactory(e);
		}
		finally 
		{
			try
			{
				session.close();
		    } 
			catch (HibernateException e)
			{
				logger.error("An error occurred when we tries to close session:" + e.getMessage(), e);
			}
		}
		
		return isFinished;
		//return new WorkflowFacade(userPrincipal, workflowId, true).isFinished();
	}

	public static void restoreSessionFactory(Throwable we)
	{
		try
		{
			logger.error("Restoring session factory...");

			String serverName = "Unknown";
	    	try
	    	{
			    InetAddress localhost = InetAddress.getLocalHost();
			    serverName = localhost.getHostName();
	    	}
	    	catch(Exception e) {}

	    	String errorMessage = "";
	    	String stacktrace = "";
	    	StringWriter sw = new StringWriter();
			if(we != null)
			{
				errorMessage = we.getMessage();
		    	we.printStackTrace(new PrintWriter(sw));
				stacktrace = sw.toString().replaceAll("(\r\n|\r|\n|\n\r)", "<br/>");
			}
			
			String subject = "CMS - Restoring session factory on " + serverName;
			String message = "OS Workflow had problems accessing the database or some other problem occurred. Check why the database went away or the error occurred.";
			message = message + "\n\n" + errorMessage + "\n\n" + stacktrace;
			
	        String warningEmailReceiver = CmsPropertyHandler.getWarningEmailReceiver();
	        if(warningEmailReceiver != null && !warningEmailReceiver.equals("") && warningEmailReceiver.indexOf("@warningEmailReceiver@") == -1)
	        {
				try
				{
					MailServiceFactory.getService().sendEmail("text/html", warningEmailReceiver, warningEmailReceiver, null, null, null, null, subject, message, "utf-8");
				} 
				catch (Exception e)
				{
					logger.error("Could not send mail:" + e.getMessage(), e);
				}
	        }
	        try
	        {
	        	logger.info("Closing:" + hibernateSessionFactory);
	        	hibernateSessionFactory.close();
	        	CacheController.clearCache("propertySetCache");
	        }
	        catch (Exception e) 
	        {
				logger.error("An error occurred when we tried to close the hibernate session factory:" + e.getMessage());	        	
			}
	        hibernateSessionFactory = new Configuration().configure().buildSessionFactory();
	        logger.info("Opened:" + hibernateSessionFactory);
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to restore the hibernate session factory:" + e.getMessage(), e);
		}
	}

	/**
	 * Returns a new WorkflowActionVO.  This method is apparently unused, but is required by BaseController.  We don't
	 * use it internally because it requires a cast; it is simpler to just use <code>new</code> to create an instance.
	 * @return a new WorkflowActionVO.
	 */
	public BaseEntityVO getNewVO()
	{
		return new WorkflowActionVO();
	}
}
