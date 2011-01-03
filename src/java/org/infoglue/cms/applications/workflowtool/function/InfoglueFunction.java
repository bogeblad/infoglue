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
package org.infoglue.cms.applications.workflowtool.function;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.Session;
import org.infoglue.cms.applications.workflowtool.util.InfoglueWorkflowBase;
import org.infoglue.cms.security.InfoGluePrincipal;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

/**
 * Base class for all infoglue workflow functions.
 */
public abstract class InfoglueFunction extends InfoglueWorkflowBase implements FunctionProvider 
{
    private final static Logger logger = Logger.getLogger(InfoglueFunction.class.getName());

	/**
	 * The key used by the <code>request</code> in the <code>parameters</code>.
	 */
	private static final String REQUEST_PARAMETER = "request";
	
	/**
	 * The key used by the <code>principal</code> in the <code>parameters</code>.
	 */
	public static final String PRINCIPAL_PARAMETER = "principal";
	
	/**
	 * The key used by the <code>locale</code> in the <code>parameters</code>.
	 */
	public static final String LOCALE_PARAMETER = "locale";
	
	/**
	 * The locale associated with the current session.
	 */
	private Locale locale;
	
	/**
	 * The caller of the workflow.
	 */
	private InfoGluePrincipal principal;
	
	/**
	 * Default constructor.
	 */
	public InfoglueFunction() 
	{ 
		super(); 
	}

	/**
	 * Executes this function.
	 * 
	 * @param transientVars the transient variables of the current execution context.
	 * @param args the arguments of the function.
	 * @param ps the propertyset associated with the current workflow.
	 * @throws WorkflowException if an error occurs during the execution.
	 */
	public final void execute(final Map transientVars, final Map args, final PropertySet ps) throws WorkflowException 
	{
		try 
		{
			storeContext(transientVars, args, ps);
			logger.debug(getClass().getName() + ".execute()--------- START");
			initialize();
			execute();
			logger.debug(getClass().getName() + ".execute()--------- STOP");
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException
	{
		super.initialize();
		initializeLocale();
		initializePrincipal();
	}
	
	/**
	 * Determine the locale associated with the current session. If a locale is found in the
	 * <code>parameters</code>, that locale will be used. Otherwise the locale will be taken 
	 * from the <code>session</code>.
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	private void initializeLocale() throws WorkflowException
	{
		if(parameterExists(LOCALE_PARAMETER))
		{
			locale = (Locale) getParameter(LOCALE_PARAMETER);
		}
		else
		{
			locale = getSession().getLocale();
		}
	}
	
	/**
	 * Determine the caller of the workflow. If a principal is found in the
	 * <code>parameters</code>, that principal will be used. Otherwise the principal will be taken 
	 * from the <code>session</code>.
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	private void initializePrincipal() throws WorkflowException
	{
		if(parameterExists(PRINCIPAL_PARAMETER))
		{
			principal = (InfoGluePrincipal) getParameter(PRINCIPAL_PARAMETER);
		}
		else
		{
			principal = getSession().getInfoGluePrincipal();
		}
	}
	
	/**
	 * Returns the session associated with the current execution.
	 */
	private Session getSession() throws WorkflowException
	{
		return new Session(((HttpServletRequest) getParameter(REQUEST_PARAMETER)).getSession());
	}
	
	/**
	 * Executes this function.
	 * 
	 * @throws WorkflowException if an error occurs during the execution.
	 */
	protected abstract void execute() throws WorkflowException;
	
	/**
	 * @todo : is this really needed?
	 */
	protected final String getRequestParameter(final String key) 
	{
		Object value = getParameters().get(key);
		if(value != null && value.getClass().isArray()) 
		{
			final String[] values = (String[]) value;
			value = (values.length == 1) ? values[0] : null;
		}
		return (value == null) ? null : value.toString();
	}
	

	/**
	 * Returns the locale associated with the current session.
	 * 
	 * @return the locale associated with the current session.
	 */
	protected final Locale getLocale() throws WorkflowException
	{
		return locale;
	}
	
	/**
	 * Returns the caller of the workflow.
	 * 
	 * @return the caller of the workflow.
	 */
	protected final InfoGluePrincipal getPrincipal()
	{
		return principal;
	}
	
	/**
	 * 
	 * 
	 * @param status
	 */
	protected final void setFunctionStatus(final String status) 
	{
		logger.debug("setFunctionStatus(" + status + ")");
		getPropertySet().setString(FUNCTION_STATUS_PROPERTYSET_KEY, status);
	}
}
