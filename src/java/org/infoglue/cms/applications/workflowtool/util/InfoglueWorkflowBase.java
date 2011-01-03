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
package org.infoglue.cms.applications.workflowtool.util;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.util.workflow.DatabaseSession;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.provider.BeanProvider;
import com.opensymphony.provider.bean.DefaultBeanProvider;
import com.opensymphony.workflow.WorkflowException;
//import com.opensymphony.workflow.util.ScriptVariableParser;

/**
 * Base class containing logic used by both <code>InfoglueFunction</code> and <code>InfoglueCondition</code>.
 * The main purpose of this class is to provide convenience methods for the parameters, arguments, and propertyset objects
 * and to handle the <code>DatabaseSession</code> object.
 */
public abstract class InfoglueWorkflowBase 
{
	/**
	 * The class logger.
	 */
	private final static Logger logger = Logger.getLogger(InfoglueWorkflowBase.class.getName());
	
	/**
	 * The default encoding.
	 */
	protected static final String UTF8_ENCODING = "utf-8";
	
	/**
	 * The prefix for all keys representing workflow specific information in the propertyset.
	 */
	public static final String WORKFLOW_PROPERTYSET_PREFIX = "workflow_";
	
	/**
	 * The key used for storing the function status in the propertyset.
	 */
	public static final String FUNCTION_STATUS_PROPERTYSET_KEY = WORKFLOW_PROPERTYSET_PREFIX + "status";

	/**
	 * The prefix for all keys representing errors in the propertyset.
	 */
	public static final String ERROR_PROPERTYSET_PREFIX = "error_";

	/**
	 * The key used by the <code>DatabaseSession</code> in the <code>parameters</code>.
	 */
	private static final String DB_PARAMETER = "db";
	
	/**
	 * The parameters (transient variables) of the current execution context.
	 */
	private Map parameters;
	
	/**
	 * The arguments passed to the function.
	 */
	private Map arguments;
	
	/**
	 * The propertyset associated with the current workflow.
	 */
	private InfogluePropertySet propertySet;

	/**
	 * The database associated with the current execution.
	 */
	private DatabaseSession workflowDatabase;
	
	/**
	 * If <code>throwException</code> is used inside both <code>try</code> and <code>catch</catch>
	 * then it will be logged twice. To get ride of this, the lastException is tracked.
	 */
	private Exception lastException;
	
	/**
	 * Default constructor.
	 */
	public InfoglueWorkflowBase() 
	{ 
		super(); 
	}

	/**
	 * Method used for initializing the object; will be called before any execution is performed.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException
	{
		workflowDatabase = (DatabaseSession) getParameter(DB_PARAMETER);
	}
	
	/**
	 * Stores the execution context.
	 * 
	 * @param transientVars the transient variables of the current execution context.
	 * @param args the arguments of the function.
	 * @param ps the propertyset associated with the current workflow.
	 */
	protected void storeContext(final Map transientVars, final Map args, final PropertySet ps)
	{
		this.parameters     = transientVars;
		this.arguments      = Collections.unmodifiableMap(args);
		this.propertySet    = new InfogluePropertySet(ps);
	}
	
	/**
	 * The preferred way to throw an exception from a subclass. 
	 * Logs the error, sets the mode of the database to rollback only and throws an exception.
	 * 
	 * @param message the exception message.
	 * @throws WorkflowException always throws an exception with the specified message.
	 */
	protected void throwException(final String message) throws WorkflowException
	{
		throwException(new WorkflowException(message));
	}
	
	/**
	 * The preferred way to throw an exception from a subclass. 
	 * Logs the error, sets the mode of the database to rollback only and throws an exception.
	 * 
	 * @param e the exception to chain. 
	 * @throws WorkflowException always throws an exception. If the specified exception is a
	 * workflow exception, that exception is thrown. Otherwise a chained workflow exception is thrown.
	 */
	protected void throwException(final Exception e) throws WorkflowException
	{
		if(lastException != e)
		{
			logger.info(e.getMessage(), e);
		}
		lastException = e;
		workflowDatabase.setRollbackOnly();
		throw (e instanceof WorkflowException) ? (WorkflowException) e : new WorkflowException(e);
	}
	
	/**
	 * Returns true if the specified argument exists; false otherwise.
	 *  
	 * @param name the name of the argument.
	 * @return true if the specified argument exists; false otherwise.
	 */
	protected final boolean argumentExists(final String name) 
	{
		return arguments.containsKey(name);
	}
	
	/**
	 * Returns the specified argument if it exists; otherwise an exception is thrown.
	 * Only use this method if the argument is absolutely required.
	 * 
	 * @param name the name of the argument.
	 * @return the specified argument.
	 * @throws WorkflowException if the specified argument doesn't exists.
	 */
	protected final String getArgument(final String name) throws WorkflowException 
	{
		if(!arguments.containsKey(name)) 
		{
			throwException("Required argument " + name + " is missing.");
		}
		return arguments.get(name).toString();
	}
	
	/**
	 * Returns the specified argument if it exists; otherwise the default value.
	 * 
	 * @param name the name of the argument.
	 * @param defaultValue the default value.
	 * @return the specified argument if it exists; otherwise the default value.
	 */
	protected final String getArgument(final String name, final String defaultValue) throws WorkflowException 
	{
		return arguments.containsKey(name) ? arguments.get(name).toString() : defaultValue;
	}
	
	/**
	 * Returns true if the specified parameter exists; false otherwise.
	 *  
	 * @param name the name of the parameter.
	 * @return true if the specified parameter exists; false otherwise.
	 */
	protected final boolean parameterExists(final String key) 
	{
		return parameters.containsKey(key);
	}
	
	/**
	 * Returns the specified parameter if it exists; otherwise an exception is thrown.
	 * Only use this method if the parameter is absolutely required.
	 * 
	 * @param name the name of the parameter.
	 * @return the specified parameter.
	 * @throws WorkflowException if the specified parameter doesn't exists.
	 */
	protected final Object getParameter(final String key) throws WorkflowException 
	{
		return getParameter(key, true);
	}

	/**
	 * Returns the specified parameter if it exists; otherwise an exception is thrown.
	 * Only use this method if the parameter is absolutely required.
	 * 
	 * @param name the name of the parameter.
	 * @return the specified parameter.
	 * @throws WorkflowException if the specified parameter doesn't exists.
	 */
	protected final String getParameterStringValue(final String key, final boolean required) throws WorkflowException 
	{
		Object o = getParameter(key, required);
		if(o instanceof String[])
			return ((String[])o)[0];
		else
			return (o == null ? null : o.toString());
	}

	/**
	 * Returns the specified parameter if it exists; otherwise the default value.
	 * 
	 * @param name the name of the parameter.
	 * @param defaultValue the default value.
	 * @return the specified parameter if it exists; otherwise the default value.
	 */
	protected final Object getParameter(final String key, final Object defaultValue) throws WorkflowException 
	{
		return parameters.containsKey(key) ? parameters.get(key) : defaultValue;
	}
	
	/**
	 * Returns the specified parameter. If the parameter is required and not found, an exception is thrown.
	 * 
	 * @param key the key.
	 * @param required indicates if the parameter is required.
	 * @return the specified parameter.
	 * @throws WorkflowException if a required parameter is missing.
	 */
	protected final Object getParameter(final String key, final boolean required) throws WorkflowException 
	{
		final Object parameter = parameters.get(key);
		if(required && parameter == null) 
		{
			final WorkflowException e = new WorkflowException("Required parameter " + key + " is missing.");
			logger.error(e.toString());
			throw e;
		}
		return parameter;
	}
	
	/**
	 * Stores the specified parameter.
	 * 
	 * @param key the lookup key.
	 * @param value the value.
	 */
	protected final void setParameter(final String key, final Object value)
	{
		parameters.put(key, value);
	}
	
	/**
	 * Returns the parameters (transient variables) of the current execution context.
	 * 
	 * @return the parameters (transient variables) of the current execution context.
	 */
	protected final Map getParameters()
	{
		return parameters;
	}
	
	/**
	 * Returns true if the specified property exists; false otherwise.
	 * 
	 * @return true if the specified property exists; false otherwise.
	 */
	protected final boolean propertySetContains(final String key)
	{
		return propertySet.exists(key);
	}
	
	/**
	 * Returns the specified data property as a string.
	 * 
	 * @param key the key.
	 */
	protected final String getPropertySetDataString(final String key)
	{
		return propertySet.getDataString(key);
	}
	
	/**
	 * Stores the specified data property.
	 * 
	 * @param key the key.
	 * @param value the value.
	 */
	protected final void setPropertySetDataString(final String key, final String value)
	{
		propertySet.setDataString(key, value);
	}
	
	/**
	 * Returns the specified string property.
	 * 
	 * @param key the key.
	 */
	protected final String getPropertySetString(final String key)
	{
		return propertySet.getString(key);
	}
	
	/**
	 * Stores the specified string property.
	 * 
	 * @param key the key.
	 * @param value the value.
	 */
	protected final void setPropertySetString(final String key, final String value)
	{
		propertySet.setString(key, value);
	}
	
	/**
	 * Removes the property with the specified key from the propertyset.
	 * 
	 * @param key the property key.
	 */
	protected final void removeFromPropertySet(final String key)
	{
		removeFromPropertySet(key, false);
	}
	
	/**
	 * Removes the property with the specified key from the propertyset. 
	 * If key is a prefix, all properties having keys starting with the specified key
	 * will be removed. 
	 * 
	 * @param key the property key.
	 * @param isPrefix indicates if the key should be used as key prefix.
	 */
	protected final void removeFromPropertySet(final String key, final boolean isPrefix)
	{
		propertySet.removeKeys(key, isPrefix);
	}

	/**
	 * 
	 */
	
	protected final String translate(final String s) 
	{
		final Object o = translateVariables(s, parameters, propertySet);
		return (o == null) ? null : o.toString();
	}
	
	/**
	 * Returns the propertyset associated with the current workflow.
	 * 
	 * @return the propertyset associated with the current workflow.
	 */
	protected final InfogluePropertySet getPropertySet()
	{
		return propertySet;
	}
		
	/**
	 * Returns the database associated with the current execution.
	 * 
	 * @return the database associated with the current execution.
	 * @throws WorkflowException if an error occurs when opening/starting the database/transaction.  
	 */
	protected final Database getDatabase() throws WorkflowException 
	{
		return workflowDatabase.getDB();
	}
	
    /**
     * Parses a string for instances of "${foo}" and returns a string with all instances replaced
     * with the string value of the foo object (<b>foo.toString()</b>). If the string being passed
     * in only refers to a single variable and contains no other characters (for example: ${foo}),
     * then the actual object is returned instead of converting it to a string.
     */
  public static Object translateVariables(String s, Map transientVars, PropertySet ps) 
  {
	  Object result = null;
	  
      String temp = s.trim();

      if (temp.startsWith("${") && temp.endsWith("}") && (temp.indexOf('$', 1) == -1)) 
      {
          // the string is just a variable reference, don't convert it to a string
          String var = temp.substring(2, temp.length() - 1);

          result = getVariableFromMaps(var, transientVars, ps);
      } 
      else 
      {
          // the string passed in contains multiple variables (or none!) and should be treated as a string
          while (true) 
          {
              int x = s.indexOf("${");
              int y = s.indexOf("}", x);

              if ((x != -1) && (y != -1)) 
              {
                  String var = s.substring(x + 2, y);
                  String t = null;
                  Object o = getVariableFromMaps(var, transientVars, ps);

                  if (o != null) 
                  {
                      t = o.toString();
                  }

                  if (t != null) 
                  {
                      s = s.substring(0, x) + t + s.substring(y + 1);
                  } 
                  else 
                  {
                      // the variable doesn't exist, so don't display anything
                      s = s.substring(0, x) + s.substring(y + 1);
                  }
              } 
              else 
              {
                  break;
              }
          }

          result = s;
      }
      
      return result;
  } 
  
  private static BeanProvider beanProvider = new DefaultBeanProvider();

  //~ Methods ////////////////////////////////////////////////////////////////

  public static Object getVariableFromMaps(String var, Map transientVars, PropertySet ps) {
      int firstDot = var.indexOf('.');
      String actualVar = var;

      if (firstDot != -1) {
          actualVar = var.substring(0, firstDot);
      }

      Object o = transientVars.get(actualVar);

      if (o == null) {
          o = ps.getAsActualType(actualVar);
      }

      if (firstDot != -1) {
          o = beanProvider.getProperty(o, var.substring(firstDot + 1));
      }

      return o;
  } 
}
