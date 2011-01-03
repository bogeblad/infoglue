package org.infoglue.cms.webservices;

import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.workflowtool.function.InfoglueFunction;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.entities.mydesktop.WorkflowVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.util.webservices.DynamicWebserviceSerializer;

import com.opensymphony.workflow.WorkflowException;

/**
 * This service is used for creating workflows from an external application.
 */

public class RemoteWorkflowServiceImpl extends RemoteInfoGlueService
{
	/**
	 * The class logger.
	 */
	private final static Logger logger = Logger.getLogger(RemoteWorkflowServiceImpl.class.getName());
	
	/**
	 * The principal executing the workflow.
	 */
	private InfoGluePrincipal principal;
    
	/**
	 * The inputs to the workflow.
	 */
	private Map inputs;
    
	/**
	 * Default constructor.
	 */
	public RemoteWorkflowServiceImpl() 
	{
		super();
	}

	/**
	 * Creates the specified workflow running as the specified principal.
	 * To determine if the workflow executed successfully, the state of the workflow is checked. 
	 * A terminated workflow is interpreted as a failure, meaning that all workflows that could
	 * be started from an external application, should terminate directly if an error occurs. 
	 * 
	 * @param principalName the name of the principal that should execute the workflow. Must have permission to create the workflow.
	 * @param languageId the language to use when executing the workflow.
	 * @param workflowName the name of the workflow.
	 * @param inputsArray the inputs to the workflow.
	 * @return true if the workflow executed sucessfully; false otherwise.
	 */
	public Boolean start(final String principalName, final Integer languageId, final String workflowName, final Object[] inputsArray, final Object[] ppp)
	{
		try 
		{
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
			initializePrincipal(principalName, workflowName);
			initializeInputs((Map) serializer.deserialize(inputsArray), languageId);
			
			logger.debug("start(" + principalName + "," + workflowName + "," + languageId + "," + inputs + ")");
			
			final WorkflowVO workflowVO = WorkflowController.getController().initializeWorkflow(principal, workflowName, 0, inputs);
			if(hasTerminated(workflowVO)) 
			{
				logger.debug("The workflow has terminated.");
				return Boolean.FALSE;
			}
		} 
		catch(Throwable t) 
		{
			logger.error("Error:" + t.getMessage(), t);
			return Boolean.FALSE;
		}
		
        updateCaches();

		return Boolean.TRUE;
	}

	/**
	 * Invokes an action on the specified workflow as the specified principal.
	 * 
	 * @param principalName the name of the principal that should execute the workflow. Must have permission to create the workflow.
	 * @param languageId the language to use when executing the workflow.
	 * @param workflowName the name of the workflow.
	 * @param inputsArray the inputs to the workflow.
	 * @return true if the workflow executed sucessfully; false otherwise.
	 */
	public Boolean invokeAction(final String principalName, final Integer languageId, final Long workflowId, final Integer actionId, final Object[] inputsArray, final Object[] ppp)
	{
		try 
		{
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
			initializeInputs((Map) serializer.deserialize(inputsArray), languageId);
			
			logger.debug("start(" + principalName + "," + workflowId + "," + actionId + "," + languageId + "," + inputs + ")");
			
			final WorkflowVO workflowVO = WorkflowController.getController().invokeAction(principal, workflowId.longValue(), actionId.intValue(), inputs);
			if(hasTerminated(workflowVO)) 
			{
				logger.debug("The workflow has terminated.");
				return Boolean.FALSE;
			}
		} 
		catch(Throwable t) 
		{
			logger.error("Error:" + t.getMessage(), t);
			return Boolean.FALSE;
		}
		
        updateCaches();

		return Boolean.TRUE;
	}

	/**
	 * Returns true if the workflow has terminated; false otherwise.
	 * 
	 * @param workflowVO the workflow.
	 * @return true if the workflow has terminated; false otherwise.
	 */
	private boolean hasTerminated(final WorkflowVO workflowVO) throws WorkflowException
	{
		return WorkflowController.getController().hasTerminated(principal, workflowVO.getIdAsPrimitive());
	}

	/**
	 * Initializes the inputs to the workflow.
	 * 
	 * @param callerInputs the inputs sent in from the caller of this service. 
	 * @param languageId the locale to use when running the workflow.
	 */
	private void initializeInputs(final Map callerInputs, final Integer languageId) throws SystemException
	{
		inputs = callerInputs;
		inputs.put(InfoglueFunction.PRINCIPAL_PARAMETER, principal);
		inputs.put(InfoglueFunction.LOCALE_PARAMETER,    LanguageController.getController().getLocaleWithId(languageId));
	}
	
	/**
	 * Checks if the principal exists and if the principal is allowed to create the workflow.
	 * 
	 * @param userName the name of the user.
	 * @param workflowName the name of the workflow to create.
	 * @throws SystemException if the principal doesn't exists or doesn't have permission to create the workflow.
	 */
	private void initializePrincipal(final String userName, final String workflowName) throws SystemException 
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
		if(!WorkflowController.getController().getIsAccessApproved(workflowName, principal))
		{
			throw new SystemException("The principal [" + userName + "] is not allowed to create the [" + workflowName + "] workflow.");
		}
	}
}
