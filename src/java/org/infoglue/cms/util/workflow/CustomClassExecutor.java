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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import webwork.action.ActionContext;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.WorkflowException;


/**
 * Executes a WebWork function and restores the old ActionContext when finished
 * (but does not provide chaining support yet). The following conversion is done:
 * <ul>
 *  <li>inputs -> ActionContext#parameters</li>
 *  <li>variables -> ActionContext#session</li>
 *  <li>args -> ActionContext#application</li>
 * </ul>
 * <p>
 *
 * <ul>
 *  <li><b>action.name</b> - the actionName to ask from the ActionFactory</li>
 * </ul>
 */
public class CustomClassExecutor implements FunctionProvider 
{
    private final static Logger logger = Logger.getLogger(CustomClassExecutor.class.getName());

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException 
    {
    	logger.info("CustomClassExecutor.execute........");
        final WorkflowContext wfContext = (WorkflowContext) transientVars.get("context");

        String className = (String) args.get("customClass.name");
        HttpServletRequest request = (HttpServletRequest) transientVars.get("request");
        logger.info("className:" + className);
        
        Iterator paramsIterator = transientVars.keySet().iterator();
	    while(paramsIterator.hasNext())
	    {
	        String key = (String)paramsIterator.next();
	        logger.info("transientVars key:" + key);
	        Object value = args.get(key);
	        logger.info("transientVars value:" + value);
	    }
	    
        Map params = new HashMap(transientVars);
        params.putAll(args);
        ActionContext.setParameters(Collections.unmodifiableMap(params));
        
        CustomWorkflowAction customWorkflowAction = getCustomWorkflowActionWithName(className);
        if(customWorkflowAction != null)
            customWorkflowAction.invokeAction(wfContext.getCaller(), request, Collections.unmodifiableMap(params), ps);        
        else
        {
            logger.warn("Could not find custom class " + className + ". Is it in the classpath?");
            throw new WorkflowException("Could not find custom class " + className + ". Is it in the classpath?");
        }
    }
    
    /**
	 * This method instansiate a new object of the given class
	 */
	
	public CustomWorkflowAction getCustomWorkflowActionWithName(String className)
	{
		try 
		{
			Class theClass = null;
			try 
			{
				theClass = Thread.currentThread().getContextClassLoader().loadClass( className );
			}
			catch (ClassNotFoundException e) 
			{
				theClass = getClass().getClassLoader().loadClass( className );
			}
			return (CustomWorkflowAction)theClass.newInstance();
			
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
} 