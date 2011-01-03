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

package org.infoglue.cms.util.workflow;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.mydesktop.WorkflowActionVO;
import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;
import org.infoglue.cms.entities.mydesktop.WorkflowVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.AbstractWorkflow;
import com.opensymphony.workflow.InvalidActionException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.config.DefaultConfiguration;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.query.Expression;
import com.opensymphony.workflow.query.FieldExpression;
import com.opensymphony.workflow.query.NestedExpression;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;

/**
 * A facade to OSWorkflow that gives us a place to cache workflow data as we need it while interacting with it.
 * This class has kind of a strange interface due to the idiosyncracies of the OSWorkflow, particularly
 * the Workflow interface.  The idea is to encapsulate the interactions with OSWorkflow and eliminate the
 * need to pass a Workflow reference and the workflow ID all over the place when extracting data from OSWorkflow
 * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 * @version $Revision: 1.42 $ $Date: 2010/02/22 08:14:28 $
 */
public class WorkflowFacade
{
	/**
	 * If the following attribute is specified in the workflow meta attributes, 
	 * The title will be fetch from the propertyset associated with the workflow, using the meta value as a key.
	 */
	private static final String WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE = "org.infoglue.title";
	
	/**
	 * If the following attribute is specified in the workflow meta attributes,
	 * then all actions will have access to a DatabaseSession instance controlled by this class. 
	 */
	private static final String WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE = "org.infoglue.database";
	
	
	private final static Logger logger = Logger.getLogger(WorkflowFacade.class.getName());

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
	 * Keep track of the workflows that is currently executing.
	 */
	private static final Collection currentWorkflows = new ArrayList();

	private final AbstractWorkflow workflow;
	private long workflowId;

	private WorkflowDescriptor workflowDescriptor;

	/**
	 * Constructs a WorkflowFacade with the given owner.
	 * 
	 * @param owner the owner of the workflow.
	 */

	public WorkflowFacade(final Owner owner)
	{
		this(owner, true);
	}

	/**
	 * Constructs a WorkflowFacade with the given owner.
	 * 
	 * @param owner the owner of the workflow.
	 */
	
	public WorkflowFacade(final Owner owner, boolean createSession)
	{
		workflow = new InfoGlueBasicWorkflow(owner.getIdentifier());
		workflow.getConfiguration().getPersistenceArgs().put("sessionFactory", hibernateSessionFactory);

		if(createSession)
		{
			try
			{
				Session session = hibernateSessionFactory.openSession();
				try
				{
					Map args = new HashMap();
					args.put("sessionFactory", hibernateSessionFactory);
					args.put("session", session);
					workflow.getConfiguration().getWorkflowStore().init(args);
				} 
				catch (StoreException e)
				{
					e.printStackTrace();
				}

				workflow.getConfiguration().getPersistenceArgs().put("session", session);
			} 
			catch (HibernateException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Constructs a WorkflowFacade with the given owner.
	 * 
	 * @param owner the owner of the workflow.
	 */
	public WorkflowFacade(final Owner owner, SessionFactory sessionFactory, Session session)
	{
		if(sessionFactory != null)
			hibernateSessionFactory = sessionFactory;
		
		workflow = new InfoGlueBasicWorkflow(owner.getIdentifier());
		if(session != null)
		{
			com.opensymphony.workflow.config.Configuration config = new /*InfoGlueHibernate*/DefaultConfiguration();
			config.getPersistenceArgs().put("session", session);
			config.getPersistenceArgs().put("sessionFactory", hibernateSessionFactory);
			workflow.setConfiguration(config);
		}
		else
		{
			workflow.getConfiguration().getPersistenceArgs().put("sessionFactory", hibernateSessionFactory);
		}
	}

	/**
	 * Constructs a WorkflowFacade with the given user principal
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 */

	public WorkflowFacade(InfoGluePrincipal userPrincipal)
	{
		this(OwnerFactory.create(userPrincipal));
	}

	/**
	 * Constructs a WorkflowFacade with the given user principal
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 */
	
	public WorkflowFacade(InfoGluePrincipal userPrincipal, boolean createSession)
	{
		this(OwnerFactory.create(userPrincipal), createSession);
	}
	
	
	/**
	 * Constructs a WorkflowFacade with the given user principal
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, SessionFactory sessionFactory, Session session)
	{
		this(OwnerFactory.create(userPrincipal), sessionFactory, session);
	}

	/**
	 * Constructs a WorkflowFacade with the given user principal representing an initialized instance of the workflow
	 * with the given name.  "Initialized" in this context means that the initial action has been executed and we have
	 * the workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param name the name of the workflow to create
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, String name, int initialAction) throws SystemException
	{
		this(userPrincipal, name, initialAction, new HashMap());
	}

	/**
	 * Constructs a WorkflowFacade with the given user principal representing an initialized instance of the workflow
	 * with the given name.  "Initialized" in this context means that the initial action has been executed and we have
	 * the workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param name the name of the workflow to create
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 * @param inputs a map of inputs to use to initialize the workflow.
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, String name, int initialAction, Map inputs) throws SystemException
	{
		this(userPrincipal);
		initialize(name, initialAction, inputs);
	}

	/**
	 * Constructs a WorkflowFacade with the given user principal representing an initialized instance of the workflow
	 * with the given name.  "Initialized" in this context means that the initial action has been executed and we have
	 * the workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param name the name of the workflow to create
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 * @param inputs a map of inputs to use to initialize the workflow.
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, String name, int initialAction, Map inputs, SessionFactory sessionFactory, Session session) throws SystemException
	{
		this(userPrincipal, sessionFactory, session);
		initialize(name, initialAction, inputs);
	}

	/**
	 * Constructs a WorkflowFacade for a user with the given workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param workflowId the ID representing an instance of the desired workflow
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, long workflowId)
	{
		this(userPrincipal);
		setWorkflowIdAndDescriptor(workflowId);
	}

	/**
	 * Constructs a WorkflowFacade for a user with the given workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param workflowId the ID representing an instance of the desired workflow
	 */
	
	public WorkflowFacade(InfoGluePrincipal userPrincipal, long workflowId, boolean createSession)
	{
		this(userPrincipal, createSession);
		setWorkflowIdAndDescriptor(workflowId);
	}
	

	/**
	 * Constructs a WorkflowFacade for a user with the given workflow ID.
	 * @param userPrincipal an InfoGluePrincipal representing a system user
	 * @param workflowId the ID representing an instance of the desired workflow
	 */
	public WorkflowFacade(InfoGluePrincipal userPrincipal, long workflowId, SessionFactory sessionFactory, Session session)
	{
		this(userPrincipal, sessionFactory, session);
		setWorkflowIdAndDescriptor(workflowId);
	}

	/**
	 * Sets the workflow ID to the given value, and caches the associated workflow descriptor
	 * @param workflowId the desired workflow ID
	 */
	private void setWorkflowIdAndDescriptor(long workflowId)
	{
		this.workflowId = workflowId;
		String key = "workflowName_" + workflowId;
		String workflowName = (String)CacheController.getCachedObject("workflowNameCache", key);
		if(workflowName == null)
		{
			workflowName = workflow.getWorkflowName(workflowId);
			CacheController.cacheObject("workflowNameCache", key, workflowName);
		}

		String keyDescriptor = "workflowDescriptor_" + workflowId;
		WorkflowDescriptor workflowDescriptorTemp = (WorkflowDescriptor)CacheController.getCachedObject("workflowNameCache", keyDescriptor);
		if(workflowDescriptorTemp == null)
		{
			workflowDescriptorTemp = workflow.getWorkflowDescriptor(workflowName);
			workflowDescriptor = workflowDescriptorTemp; 
			CacheController.cacheObject("workflowNameCache", keyDescriptor, workflowDescriptorTemp);
		}
		else
			workflowDescriptor = workflowDescriptorTemp; 
		
		//workflowDescriptor = workflow.getWorkflowDescriptor(workflowName);
	}

	/**
	 * Returns the workflow ID
	 * @return the workflow ID
	 */
	public long getWorkflowId()
	{
		return workflowId;
	}

	/**
	 * Initializes the workflow, setting workflowId as a side-effect.
	 * @param name the name of the workflow to initialize
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 * @param inputs a map of inputs to use to initialize the workflow.
	 * @throws SystemException if a workflow error occurs.
	 */
	private void initialize(String name, int initialAction, Map inputs) throws SystemException
	{
		try
		{
			if(useDatabaseExtension(workflow.getWorkflowDescriptor(name)))
			{
				setWorkflowIdAndDescriptor(doExtendedInitialize(name, initialAction, inputs));
			}
			else
			{
				setWorkflowIdAndDescriptor(workflow.initialize(name, initialAction, inputs));
			}
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to get workflow with name:" + name);
			throw new SystemException(e);
		}
	}

	/**
	 * Initializes the workflow.  
	 * A <code>DatabaseSession</code> object whose lifecycle is handled by this method is inserted into the <code>inputs</code>.
	 * 
	 * @param name the name of the workflow to initialize
	 * @param initialAction the ID of the initial action to perform to get the workflow started.
	 * @param inputs a map of inputs to use to initialize the workflow.
	 * @throws SystemException if a workflow error occurs.
	 */

	private long doExtendedInitialize(final String name, final int initialAction, final Map inputs) throws WorkflowException
	{
		long result = 0;
		final DatabaseSession db = new DatabaseSession();
		try
		{
			final Map copy = new HashMap();
			copy.putAll(inputs);
			copy.put(workflow.getWorkflowDescriptor(name).getMetaAttributes().get(WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE), db);
			result = workflow.initialize(name, initialAction, copy);
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			if(db != null)
			{
				db.setRollbackOnly();
			}
	        
			throw (e instanceof WorkflowException) ? (WorkflowException) e : new WorkflowException(e);
		} 
		finally 
		{
			db.releaseDB();
		}
		return result;
	}

	/**
	 * Performs an action using the given inputs
	 * @param actionId the ID of the action to perform
	 * @param inputs a map of inputs to the action
	 * @throws WorkflowException if a workflow error occurs, or if the underlying workflow is not active
	 */
	public synchronized void doAction(int actionId, Map inputs) throws WorkflowException
	{
		if(logger.isInfoEnabled())
			logger.info("doAction with " + actionId + " on " + this.workflowId);

		try
		{
			final Long id = new Long(workflowId);

			if(getEntryState() == WorkflowEntry.CREATED)
				workflow.changeEntryState(workflowId, WorkflowEntry.ACTIVATED);

			if(logger.isInfoEnabled())
			{
				logger.info("workflowId:" + workflowId);
				logger.info("actionId:" + actionId);
			}
			
			synchronized(currentWorkflows)
			{	
				if(!isActive())
				{
					if(getEntryState() == WorkflowEntry.UNKNOWN)
						throw new WorkflowException("The workflow with id " + workflowId + " is in an unknown state - the database could be down or the workflow corrupt");
					else
						throw new InvalidActionException("Workflow " + workflowId + " is no longer active");
				}
				if(currentWorkflows.contains(id))
				{
					throw new WorkflowException("The selected workflow is executing...");
				}
				currentWorkflows.add(id);
			}
			
			try
			{
				if(useDatabaseExtension(workflowDescriptor))
				{
					doExtendedAction(actionId, inputs);
				}
				else
				{
					workflow.doAction(workflowId, actionId, inputs);
				}
			}
			finally
			{
				synchronized(currentWorkflows)
				{
					currentWorkflows.remove(id);
				}
			}
		}
		catch(Exception we)
		{
			logger.error("An error occurred when we tried to invoke an workflow action:" + we.getMessage());
			//restoreSessionFactory(workflow, we);
			throw new WorkflowException("An error occurred when we tried to invoke an workflow action:" + we.getMessage());
		}
	}
	

	/**
	 * Performs an action using the given inputs.
	 * A <code>DatabaseSession</code> object whose lifecycle is handled by this method is inserted into the <code>inputs</code>.
	 * 
	 * @param actionId the ID of the action to perform
	 * @param inputs a map of inputs to the action
	 * @throws WorkflowException if a workflow error occurs, or if the underlying workflow is not active
	 */
	private void doExtendedAction(final int actionId, final Map inputs) throws WorkflowException
	{
		final DatabaseSession db = new DatabaseSession();

		try 
		{
			final Map copy = new HashMap();
			copy.putAll(inputs);
			copy.put(workflowDescriptor.getMetaAttributes().get(WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE), db);

			workflow.doAction(workflowId, actionId, copy);
		} 
		catch(Exception e) 
		{
			logger.error("An error occurred in doExtendedAction:" + e.getMessage(), e);
			//e.printStackTrace();
			if(db != null)
			{
				db.setRollbackOnly();
			}

			throw (e instanceof WorkflowException) ? (WorkflowException) e : new WorkflowException(e);
		} 
		finally 
		{
			db.releaseDB();
		}
	}

	/**
	 * Returns the property set associated with the underlying workflow
	 * @return the property set associated with the underlying workflow
	 */
	public PropertySet getPropertySet()
	{
		//Timer t = new Timer();
    	String key = "psCache_" + workflowId;
    	PropertySet ps = (PropertySet)CacheController.getCachedObject("propertySetCache", key);
    	if(ps == null)
    	{
    		ps = workflow.getPropertySet(workflowId);
    		CacheController.cacheObject("propertySetCache", key, ps);
    	}

    	//t.printElapsedTime("getPropertySet took");
		return ps;
	}

	/**
	 * Returns the state of the underlying workflow entry
	 * @return the state of the underlying workflow entry
	 */
	private int getEntryState() throws WorkflowException
	{
		try
		{
			return workflow.getEntryState(workflowId);
		}
		catch(Throwable we)
		{
			logger.error("An error occurred when we tried to check for entry state:" + we.getMessage());
			//restoreSessionFactory(workflow, we);
			throw new WorkflowException("An error occurred when we tried to check for entry state:" + we.getMessage());
 		}
	}

	/**
	 * Indicates whether the underlying workflow is active.
	 * 
	 * @return true if the underlying workflow's state is WorkflowEntry.ACTIVATED, otherwise returns false.
	 */
	public boolean isActive() throws WorkflowException
	{
		return getEntryState() == WorkflowEntry.ACTIVATED;
	}

	/**
	 * Indicates whether the underlying workflow is finished.
	 * 
	 * @return true if the underlying workflow's state is WorkflowEntry.KILLED or WorkflowEntry.COMPLETED, otherwise returns false.
	 */
	public boolean isFinished() throws WorkflowException
	{
		int state = getEntryState();
		return state == WorkflowEntry.KILLED || state == WorkflowEntry.COMPLETED;
	}
	
	/**
	 * Returns a list of all declared workflows, i.e., workflows defined in workflows.xml
	 * @return a list WorkflowVOs representing all declared workflows
	 */
	public List<WorkflowVO> getDeclaredWorkflows()
	{
		String[] workflowNames = workflow.getWorkflowNames();
		List<WorkflowVO> availableWorkflows = new ArrayList<WorkflowVO>();

		for (int i = 0; i < workflowNames.length; i++)
		{
			try
			{
				availableWorkflows.add(createWorkflowVO(workflowNames[i]));
			}
			catch(Exception e)
			{
				logger.error("The workflow " + workflowNames[i] + " could not be instantiated:" + e.getMessage(), e);
			}
		}
		
		return availableWorkflows;
	}

	/**
	 * Returns a list of all active workflows.
	 * 
	 * @return a list of WorkflowVOs representing all active workflows
	 * @throws SystemException if an error occurs finding the active workflows
	 */
	public List<WorkflowVO> getActiveWorkflows() throws SystemException
	{
		List<WorkflowVO> workflowVOs = new ArrayList<WorkflowVO>();

		List<WorkflowVO> activeWorkflows = findActiveWorkflows();
		Iterator activeWorkflowsIterator = activeWorkflows.iterator();
		while (activeWorkflowsIterator.hasNext())
		{
			setWorkflowIdAndDescriptor(((Long)activeWorkflowsIterator.next()).longValue());
			//logger.info("workflowId:" + workflowId);
			workflowVOs.add(createWorkflowVO());
		}

		return workflowVOs;
	}
	
	/**
	 * Returns a list of workflows owned by the specified principal. If the principal is
	 * an administrator, all active workflows are returned.
	 * 
	 * @param principal the principal.
	 * @return the workflows owned by the specified principal.
	 */
	
	public List getMyActiveWorkflows(final InfoGluePrincipal principal) throws SystemException
	{		
		String key = "myWorkflows_" + principal.getName();
		List workflows = (List)CacheController.getCachedObject("myActiveWorkflows", key);
		
		if(workflows == null)
		{
				if(principal.getIsAdministrator())
				{
					workflows = getActiveWorkflows();
				}
				
				Collection owners = OwnerFactory.createAll(principal);
				Expression[] expressions = new Expression[owners.size()];
				
				Iterator ownersIterator = owners.iterator();
				int i = 0;
				while(ownersIterator.hasNext())
				{
					Owner owner = (Owner)ownersIterator.next();
					Expression expression = new FieldExpression(FieldExpression.OWNER, FieldExpression.CURRENT_STEPS, FieldExpression.EQUALS, owner.getIdentifier());
					expressions[i] = expression;
					i++;
				}				
				
				final Set workflowVOs = new HashSet();
				workflowVOs.addAll(createWorkflowsForOwner(expressions));
	
				/*
				final Set workflowVOs = new HashSet();
				for(final Iterator owners = OwnerFactory.createAll(principal).iterator(); owners.hasNext(); )
				{
					final Owner owner = (Owner) owners.next();
					workflowVOs.addAll(createWorkflowsForOwner(owner));
				}
				*/
				
				workflows = new ArrayList(workflowVOs);
				CacheController.cacheObject("myActiveWorkflows", key, workflows);
		}
		
		return workflows;
	}
	
	/**
	 * Creates value object for all workflows having the specified owner.
	 * 
	 * @param owner the owner.
	 * @return the value objects.
	 * @throws SystemException if an error occurs when creating the value objects.
	 */
	private final Set createWorkflowsForOwner(final Owner owner) throws SystemException
	{
		final Set workflowVOs = new HashSet(); 
		List workflows = findWorkflows(owner);
		Iterator workflowsIterator = workflows.iterator();
		while (workflowsIterator.hasNext())
		{
			setWorkflowIdAndDescriptor(((Long)workflowsIterator.next()).longValue());
			workflowVOs.add(createWorkflowVO());
		}
		return workflowVOs;
	}

	/**
	 * Creates value object for all workflows having the specified owner.
	 * 
	 * @param owner the owner.
	 * @return the value objects.
	 * @throws SystemException if an error occurs when creating the value objects.
	 */
	private final Set createWorkflowsForOwner(final Expression[] expressions) throws SystemException
	{
		try
		{
			final Set workflowVOs = new HashSet(); 
			List workflows = findWorkflows(expressions);
			Iterator workflowsIterator = workflows.iterator();
			while (workflowsIterator.hasNext())
			{
				setWorkflowIdAndDescriptor(((Long)workflowsIterator.next()).longValue());
				workflowVOs.add(createWorkflowVO());
			}

			return workflowVOs;
		}
		catch (WorkflowException e)
		{
			throw new SystemException(e);
		}
	}

	/**
	 * Finds all active workflows
	 * @return A list of workflowIds representing workflows that match the hard-wored query expression.
	 * @throws SystemException if a workflow error occurs during the search
	 */
	private List<WorkflowVO> findActiveWorkflows() throws SystemException
	{
		try
		{
			List<WorkflowVO> workflows = workflow.query(new WorkflowExpressionQuery(new FieldExpression(FieldExpression.STATE, FieldExpression.ENTRY, FieldExpression.EQUALS, new Integer(WorkflowEntry.ACTIVATED))));
			return workflows;
			//return workflow.query(new WorkflowExpressionQuery(new FieldExpression(FieldExpression.STATE, FieldExpression.ENTRY, FieldExpression.EQUALS, new Integer(WorkflowEntry.ACTIVATED))));
		}
		catch (WorkflowException e)
		{
			throw new SystemException(e);
		}
	}

	/**
	 * Finds all workflows for the specified owner.
	 * 
	 * @param owner the owner.
	 * @return The active workflows owned by the specified owner. 
	 * @throws SystemException
	 */
	private List findWorkflows(final Owner owner) throws SystemException
	{
		try
		{
			List workflows = workflow.query(new WorkflowExpressionQuery(new FieldExpression(FieldExpression.OWNER,
					FieldExpression.CURRENT_STEPS, FieldExpression.EQUALS, owner.getIdentifier())));

			return workflows;
		}
		catch (WorkflowException e)
		{
			throw new SystemException(e);
		}
	}

	/**
	 * Finds all workflows for the specified owner.
	 * 
	 * @param owner the owner.
	 * @return The active workflows owned by the specified owner. 
	 * @throws SystemException
	 */
	private List findWorkflows(final Expression[] expressions) throws WorkflowException
	{
		try
		{
			List workflows = workflow.query(new WorkflowExpressionQuery(new NestedExpression(expressions, NestedExpression.OR)));

			//List workflows = workflow.query(new WorkflowExpressionQuery(new FieldExpression(FieldExpression.OWNER, FieldExpression.CURRENT_STEPS, FieldExpression.EQUALS, owner.getIdentifier())));
			return workflows;
		}
		catch (WorkflowException we)
		{
			logger.error("An error occurred when we tried to invoke an workflow action:" + we.getMessage());
			//restoreSessionFactory(workflow, we);
			throw new WorkflowException("An error occurred when we tried to invoke an workflow action:" + we.getMessage());
		}
	}

	/**
	 * Returns all current steps for the workflow, i.e., steps that could be performed in the workflow's current state
	 * Steps are filtered according to ownership; if a step has an owner, it is only included if the ownser matches
	 * the caller or if the current user is an administrator.
	 * @return a list of WorkflowStepVOs representing the current steps of the workflow with workflowId
	 */
	public List getCurrentSteps()
	{
		return getCurrentSteps(null);
	}
	
	public List getCurrentSteps(final WorkflowVO workflowVO)
	{
		return createStepVOs(workflowVO, workflow.getCurrentSteps(workflowId));
	}

	/**
	 * Returns all history steps for the workflow, i.e., all the steps that have already been performed.
	 * @return a list of WorkflowStepVOs representing all history steps for the workflow with workflowId
	 */
	public List getHistorySteps()
	{
		return getHistorySteps(null);
	}
	
	public List getHistorySteps(final WorkflowVO workflowVO)
	{
		return createStepVOs(workflowVO, workflow.getHistorySteps(workflowId));
	}

	/**
	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
	 * no knowledge of current or history steps at this point.
	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
	 */
	public List getDeclaredSteps()
	{
		return getDeclaredSteps(workflowDescriptor);
	}

	/**
	 * Creates a list of WorkflowStepVOs from the given list of steps
	 * @param steps a list of Steps
	 * @return a list of WorkflowStepVOs corresponding to all steps that pass the filter
	 */
	private List createStepVOs(final WorkflowVO workflowVO, final List steps)
	{
		List stepVOs = new ArrayList();
		for (Iterator i = steps.iterator(); i.hasNext();)
		{
			Step step = null;
			step = (Step)i.next();
			try
			{
				stepVOs.add(createStepVO(workflowVO, step));
			}
			catch(Exception e)
			{
				logger.warn("There was an invalid step:" + workflowVO, e);
			}
		}
		
		return stepVOs;
	}

	/**
	 * Returns all steps for a workflow definition.  These are the steps declared in the workfow descriptor; there is
	 * no knowledge of current or history steps at this point.
	 * @param descriptor a workflow descriptor from which to get current steps
	 * @return a list of WorkflowStepVOs representing all steps in the workflow.
	 */
	private List getDeclaredSteps(WorkflowDescriptor descriptor)
	{
		List steps = new ArrayList();
		for (Iterator i = descriptor.getSteps().iterator(); i.hasNext();)
			steps.add(createStepVO((StepDescriptor)i.next()));

		return steps;
	}

	/**
	 * Returns a list of initial actions for the workflow
	 * @return a list of WorkflowActionVOs representing the global actions for the workflow with workflowId
	 */
	private List getInitialActions()
	{
		if(workflowDescriptor != null)
			return createActionVOs(workflowDescriptor.getInitialActions());
		else
			return null;
	}

	/**
	 * Returns a list of global actions for the workflow
	 * @return a list of WorkflowActionVOs representing the global actions for the workflow with workflowId
	 */
	private List getGlobalActions()
	{
		if(workflowDescriptor != null)
			return createActionVOs(workflowDescriptor.getGlobalActions());
		else
			return null;
	}

	/**
	 * Creates a list of WorkflowActionVOs from a list of action descriptors
	 * @param actionDescriptors a list of ActionDescriptors
	 * @return a list of WorkflowActionVOs representing actionDescriptors
	 */
	private List createActionVOs(List actionDescriptors)
	{
		List actions = new ArrayList();
		for (Iterator i = actionDescriptors.iterator(); i.hasNext();)
			actions.add(createActionVO((ActionDescriptor)i.next()));

		return actions;

	}

	/**
	 * Creates a new WorkflowVO.  This represents a pretty complete workflow; you get all the current steps, history
	 * steps, available actions, and global actions.
	 * @return a WorkflowVO representing workflow, with workflowId
	 */
	public WorkflowVO createWorkflowVO()
	{
		WorkflowVO workflowVO = new WorkflowVO(new Long(workflowId), workflow.getWorkflowName(workflowId));
		if(workflowDescriptor != null)
		{
			if(useTitleExtension(workflowDescriptor))
				workflowVO.setTitle(getWorkflowTitle());
		}
		
		workflowVO.setCurrentSteps(getCurrentSteps(workflowVO));
		workflowVO.setHistorySteps(getHistorySteps(workflowVO));
		workflowVO.setInitialActions(getInitialActions());
		workflowVO.setGlobalActions(getGlobalActions());

		return workflowVO;
	}

	/**
	 * Returns the title of the workflow instance.
	 * 
	 * @return the title of the workflow instance.
	 */
	private String getWorkflowTitle() 
	{
		if(!workflowDescriptor.getMetaAttributes().containsKey(WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE))
		{
			return null;
		}
		
		final String key = (String) workflowDescriptor.getMetaAttributes().get(WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE);
		final PropertySet ps = getPropertySet();
		return ps.exists(key) ? ps.getString(key) : null;
	}
	
	/**
	 * Creates a new WorkflowVO from workflow with the given name.  The resulting workflow VO contains only a
	 * minimal amount of data because we don't have the workflow ID.  Basically all you get is all the steps.
	 * @param name the name of the desired workflow
	 * @return a new WorkflowVO representing workflow
	 */
	private WorkflowVO createWorkflowVO(String name)
	{
		WorkflowVO workflowVO = new WorkflowVO(null, name);
		try
		{
			workflowVO.setDeclaredSteps(getDeclaredSteps(workflow.getWorkflowDescriptor(name)));
		}
		catch(Exception e)
		{
			workflowVO.setStatus(WorkflowVO.STATUS_NOT_OK);
			workflowVO.setStatusMessage("Error in workflow:" + e.getMessage());
			
			logger.error("Could not read workflow:" + e.getMessage(), e);
		}
		
		return workflowVO;
	}

	/**
	 * Creates a WorkflowStepVO from the given step
	 * @param step the desired step
	 * @return a new WorkflowStepVO representing step.
	 */
	private WorkflowStepVO createStepVO(final WorkflowVO workflowVO, final Step step) throws Exception
	{
		logger.info("step:" + step + ':' + step.getId());
		logger.info("Owner:" + step.getOwner());

		WorkflowStepVO stepVO = new WorkflowStepVO(workflowVO);
		stepVO.setId(new Integer((int)step.getId()));// Hope it doesn't get too big; we are stuck with int thanks to BaseEntityVO
		stepVO.setStepId(new Integer(step.getStepId()));
		stepVO.setWorkflowId(new Long(workflowId));
		stepVO.setStatus(step.getStatus());
		stepVO.setStartDate(step.getStartDate());
		stepVO.setFinishDate(step.getFinishDate());
		stepVO.setOwner(step.getOwner());
		stepVO.setCaller(step.getCaller());
		
		try
		{
			StepDescriptor stepDescriptor = workflowDescriptor.getStep(step.getStepId());
			if(stepDescriptor != null)
			{
				stepVO.setName(stepDescriptor.getName());
				for (Iterator i = stepDescriptor.getActions().iterator(); i.hasNext();)
					stepVO.addAction(createActionVO((ActionDescriptor)i.next()));
			}
			else
			{
				throw new SystemException("No stepDescriptor found for " + step);
			}
		}
		catch(Exception e)
		{
			
		}
		
		return stepVO;
	}

	/**
	 * Creates a WorkflowStepVO from a step descriptor.  Some of the step data, e.g., status, startDate,
	 * finishDate, etc. cannot be populated here because the step descriptor does not know about these things.
	 * @param stepDescriptor a step descriptor
	 * @return a WorkflowStepVO representing stepDescriptor
	 */
	private WorkflowStepVO createStepVO(StepDescriptor stepDescriptor)
	{
		WorkflowStepVO step = new WorkflowStepVO();
		step.setStepId(new Integer(stepDescriptor.getId()));
		step.setName(stepDescriptor.getName());
		step.setStatus("Not started");

		for (Iterator i = stepDescriptor.getActions().iterator(); i.hasNext();)
			step.addAction(createActionVO((ActionDescriptor)i.next()));

		return step;
	}

	/**
	 * Creates a WorkflowActionVO for the given action descriptor
	 * @param actionDescriptor an action descriptor
	 * @return a WorkflowActionVO representing actionDescriptor
	 */
	private WorkflowActionVO createActionVO(ActionDescriptor actionDescriptor)
	{
		logger.info("Action:" + actionDescriptor.getId() + ':' + actionDescriptor.getName()
					+ ':' + actionDescriptor.getParent().getClass());

		WorkflowActionVO actionVO = new WorkflowActionVO(new Integer(actionDescriptor.getId()));
		actionVO.setWorkflowId(new Long(workflowId));
		actionVO.setName(actionDescriptor.getName());
		actionVO.setView(actionDescriptor.getView());
		actionVO.setAutoExecute(actionDescriptor.getAutoExecute());
		actionVO.setMetaAttributes(actionDescriptor.getMetaAttributes());
		return actionVO;
	}
	
	/**
	 * Checks if the title extension should be used.
	 * 
	 * @return true if the extension should be used, false otherwise.
	 */
	private boolean useTitleExtension(final WorkflowDescriptor descriptor) 
	{
		return descriptor.getMetaAttributes().containsKey(WORKFLOW_TITLE_EXTENSION_META_ATTRIBUTE);
	}

	/**
	 * Checks if the database extension should be used.
	 * 
	 * @return true if the extension should be used, false otherwise.
	 */
	private boolean useDatabaseExtension(final WorkflowDescriptor descriptor) 
	{
		return descriptor.getMetaAttributes().containsKey(WORKFLOW_DATABASE_EXTENSION_META_ATTRIBUTE);
	}	
}
