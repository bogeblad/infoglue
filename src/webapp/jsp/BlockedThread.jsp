<%@ page import="java.lang.management.*" %>
<%@ page import="java.util.*" %>

<%@ page import="org.infoglue.deliver.util.ThreadMonitor" %>
<%@ page import="org.infoglue.deliver.util.RequestAnalyser" %>

<%
	boolean allowAccess = true;
	if(!org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController.getController().getIsIPAllowed(request))
    {
		java.security.Principal principal = (java.security.Principal)session.getAttribute("infogluePrincipal");
		if(principal != null && org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController.getController().getIsPrincipalAuthorized((org.infoglue.cms.security.InfoGluePrincipal)principal, "ViewApplicationState.Read", false, true))
		{
			allowAccess = true;
		}
		else
		{
			allowAccess = false;
	        response.setContentType("text/plain");
	        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        out.println("You have no access to this view - talk to your administrator if you should. Try go through the ViewApplicationState.action if you have an account that have access.");
		}
    }
	
	if(allowAccess)
	{
		try
		{
			String action = request.getParameter("action");
			String threadId = request.getParameter("threadId");
			if(action != null && threadId != null && action.equals("kill"))
			{
		    	ThreadGroup tg = Thread.currentThread().getThreadGroup();
			    int n = tg.activeCount();
		        Thread[] threadArray = new Thread[n];
		        n = tg.enumerate(threadArray, false);
		        for (int i=0; i<n; i++) 
		        {
		           	String currentThreadId = "" + threadArray[i].getId();
		           	if(currentThreadId.equals(threadId))
		           	{
		        	   out.print("Killing " + currentThreadId);
		        	   threadArray[i].stop();
		        	   //RequestAnalyser.getRequestAnalyser().decNumberOfCurrentRequests(30000);
		        	}
		        }  
				response.sendRedirect("BlockedThread.jsp");
				return;
			}
		}
		catch(Exception e)
		{
			out.print("Error:" + e.getMessage());
		}
		
	    ThreadMXBean t = ManagementFactory.getThreadMXBean();
	    
	%>
		<html>
		<head>
		  <title>JVM Blocked Thread Monitor</title>
		</head>
		<body>
	
		
		<table border="0" width="100%">
	    <tr><td align="center"><h3>Long Threads</h3></td></tr>
	    <tr><td align="center"><h4>All threads which has been running for more than 30 seconds</h4></td></tr>
	<%
	   	List threadMonitors = RequestAnalyser.getLongThreadMonitors();
		Iterator threadMonitorsIterator = threadMonitors.iterator();    
		while(threadMonitorsIterator.hasNext())
	    {
			ThreadMonitor tm = (ThreadMonitor)threadMonitorsIterator.next();
			
			long threads[] = {tm.getThreadId()};
		    ThreadInfo[] tinfo = t.getThreadInfo(threads, 20);
			
		    String stackString = "";
	        for (int i=0; i<tinfo.length; i++)
		    {
				ThreadInfo e = tinfo[i];
		
		        StackTraceElement[] el = e.getStackTrace();
		        
		        if (el != null && el.length != 0)
		        {
		            for (int n = 0; n < el.length; n++)
		            {
		            	StackTraceElement frame = el[n];
		            	if (frame == null)
		            		stackString += "&nbsp;&nbsp;&nbsp;&nbsp;null stack frame" + "<br/>";
		            	else	
		                	stackString += "&nbsp;&nbsp;&nbsp;&nbsp;null stack frame" + frame.toString() + "<br/>";
					}                    
		       	}
		    }
			%>
	        <tr><td align="center">
	        <%
	        out.print("<br/>Elapsed time:" + tm.getElapsedTime() + "<br/>" + " " + " Thread id: " + tm.getThreadId() + "<br/> Original url: " + tm.getOriginalFullURL() + ")");
	        out.print("<br/><a href=\"BlockedThread.jsp?action=kill&threadId=" + tm.getThreadId() + "\">Kill thread</a><br/>");
	        out.print(stackString);
			%>
	        </td></tr>
	        <%
	    }
		%>
		</table>
	
		
	    <table border="0" width="100%">
	    <tr><td align="center"><h3>Thread MXBean</h3></td></tr>
	    <tr><td align="center"><h4>All suspicious Threads</h4></td></tr>
		<%
	    long threads[] = t.getAllThreadIds();
	    ThreadInfo[] tinfo = t.getThreadInfo(threads, 15);
	
	    for (int i=0; i<tinfo.length; i++)
	    {
			ThreadInfo e = tinfo[i];
	
	        StackTraceElement[] el = e.getStackTrace();
	        
	        String stackString = "";
	        if (el != null && el.length != 0)
	        {
	            for (int n = 0; n < el.length; n++)
	            {
	            	StackTraceElement frame = el[n];
	            	if (frame == null)
	            		stackString += "&nbsp;&nbsp;&nbsp;&nbsp;null stack frame" + "<br/>";
	            	else	
	                	stackString += "&nbsp;&nbsp;&nbsp;&nbsp;null stack frame" + frame.toString() + "<br/>";
				}                    
	       	}
			
			long threadId = threads[i];
	    	long cpuTime = t.getThreadCpuTime(threadId) / 10000000;
	    	long userTime = t.getThreadUserTime(threadId) / 10000000;
	            
	        long blockedTime = e.getBlockedTime();
	        long waitedTime = e.getWaitedTime();
			
			long lockOwnerId = e.getLockOwnerId();
			String lockOwnerName = e.getLockOwnerName();
			
			//Only list infoglue threads except redirect filter.			
			if(stackString.indexOf("org.infoglue") > -1 && !e.getThreadState().equals(Thread.State.RUNNABLE))
			{
	        %>
	        <tr><td align="center">
	        <%
	        out.print("<br/>" + e.getThreadName() + "<br/>" + " " + " Thread id = " + e.getThreadId() + " " + e.getThreadState() + "(" + cpuTime + ":" + userTime + ":" + blockedTime + ":" + waitedTime + ") - lock ownerId:" + lockOwnerId + ":" + lockOwnerName);
	        out.print("<br/><a href=\"BlockedThread.jsp?action=kill&threadId=" + threadId + "\">Kill thread</a><br/>");
	        out.print(stackString);
	        %>
	        <div style="padding-top: 10px;">
	        	Lock thread:<br/>
	        <%
	        if(lockOwnerId > 0)
	        {
			long lockThreads[] = {lockOwnerId};
		    ThreadInfo[] locktinfo = t.getThreadInfo(lockThreads, 20);
		    String lockStackString = "";
	        for (int iLock=0; iLock<locktinfo.length; iLock++)
		    {
				ThreadInfo eLock = locktinfo[iLock];
		
		        StackTraceElement[] elLock = eLock.getStackTrace();
		        
		        if (elLock != null && elLock.length != 0)
		        {
		            for (int n = 0; n < elLock.length; n++)
		            {
		            	StackTraceElement frame = elLock[n];
		            	if (frame == null)
		            		lockStackString += "&nbsp;&nbsp;&nbsp;&nbsp;null stack frame" + "<br/>";
		            	else	
		            		lockStackString += "&nbsp;&nbsp;&nbsp;&nbsp;null stack frame" + frame.toString() + "<br/>";
					}                    
		       	}
		    }
	        out.print(lockStackString);
	        }
	        %>
	        
	        </div>
	        </td></tr>
	        <%
	        }
	    }
		%>
		</table>
		</body>
		</html>
		<%
	}
%>                        