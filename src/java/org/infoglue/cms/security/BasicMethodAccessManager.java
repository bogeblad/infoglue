package org.infoglue.cms.security;

import java.security.AccessControlException;

import org.infoglue.cms.jobs.SubscriptionsJob;

public class BasicMethodAccessManager
{
	
	public static final void checkAccessToCall(String[] allowedClassNames, String message)
	{
		Throwable t = new Throwable();
		StackTraceElement[] stackElements = t.getStackTrace();

		StackTraceElement calledElement = stackElements[1];
	    String calledClassName = calledElement.getClassName();
	    String calledMethodName = calledElement.getMethodName();
		
		StackTraceElement callingElement = stackElements[2];
	    String className = callingElement.getClassName();
	    String methodName = callingElement.getMethodName();
    
        boolean acceptedCall = false;
        for(int i=0; i<allowedClassNames.length; i++)
        {
        	String allowedClassName = allowedClassNames[i];
			if(callingElement.getClassName().equals(allowedClassName))
				acceptedCall = true;
        }
        
        if(!acceptedCall)
        	throw new AccessControlException("An access control violation was attempted. Call from " + className + "." + methodName + " was made to " + calledClassName + "." + calledMethodName + "." + message);
	}
	
}
