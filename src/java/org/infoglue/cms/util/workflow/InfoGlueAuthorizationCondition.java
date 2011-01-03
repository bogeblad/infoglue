package org.infoglue.cms.util.workflow;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowContext;

/**
 * This action checks if the user has a particular role.
 * 
 * @author Mattias Bogeblad
 */

public class InfoGlueAuthorizationCondition implements Condition 
{
    private final static Logger logger = Logger.getLogger(InfoGlueAuthorizationCondition.class.getName());

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) 
    {
        boolean passesCondition = true;
        
        try 
        {
            WorkflowContext context = (WorkflowContext) transientVars.get("context");
            String roleName = (String)args.get("roleName");
            String userName = (String)args.get("userName");
            
            logger.info("passesCondition.............");
            logger.info("caller:" + context.getCaller());
            logger.info("roleName:" + roleName);
            logger.info("userName:" + userName);
            
            InfoGluePrincipal principal = UserControllerProxy.getController().getUser(context.getCaller());
            
            if(userName != null && userName.length() > 0 && !principal.getName().equals(userName))
                passesCondition = false;
            
            if(roleName != null && roleName.length() > 0)
            {
                boolean hasRole = false;
	            List roles = principal.getRoles();
	            Iterator rolesIterator = roles.iterator();
	            while(rolesIterator.hasNext())
	            {
	                InfoGlueRole role = (InfoGlueRole)rolesIterator.next();
	                if(role.getName().equalsIgnoreCase(roleName))
	                    hasRole = true;
	            }
	            
	            if(!hasRole)
	                passesCondition = false;
            }            
        } 
        catch (Exception e) 
        {
            logger.error("A severe error occurred when checking workflow authorization:" + e.getMessage(), e);
        }
        
        return passesCondition;
    } 
} 