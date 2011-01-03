/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package org.infoglue.cms.util.workflow;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import webwork.action.ActionContext;
import webwork.dispatcher.GenericDispatcher;

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
public class WebWorkExecutor implements FunctionProvider 
{
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final Log log = LogFactory.getLog(WebWorkExecutor.class);

    //~ Methods ////////////////////////////////////////////////////////////////

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException 
    {
        final WorkflowContext wfContext = (WorkflowContext) transientVars.get("context");

        String actionName = (String) args.get("action.name");
        GenericDispatcher gd = new GenericDispatcher(actionName);
        gd.prepareContext();
        
        ActionContext.setPrincipal(new Principal() {
                public String getName() {
                    return wfContext.getCaller();
                }
            });
        ActionContext.setApplication(args);
        ActionContext.setSession(ps.getProperties(""));
        ActionContext.setLocale(Locale.getDefault());

        Map params = new HashMap(transientVars);
        params.putAll(args);
        ActionContext.setParameters(Collections.unmodifiableMap(params));

        try {
            gd.prepareContext();
            gd.prepareValueStack();
            gd.executeAction();
            gd.finish();
            gd.finalizeContext();
        } catch (Exception e) {
            throw new WorkflowException("Could not execute action " + actionName, e);
        }
    }
} 