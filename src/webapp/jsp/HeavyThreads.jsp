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
        ThreadMXBean t = ManagementFactory.getThreadMXBean();
		%>
		<html>
		<head>
		  <title>JVM Thread Monitor</title>
		</head>
		<body>
		<h2>Heavy Threads</h2>
        <table border="1" width="100%">
        <tr><td>ID</td><td>User Time</td><td>Stack</td></tr>

		<%
        try
        {
            long threads[] = t.getAllThreadIds();
            ThreadInfo[] tinfo = t.getThreadInfo(threads, 40);
			
            for (int i=0; i<tinfo.length; i++)
            {
                ThreadInfo e = tinfo[i];
                StackTraceElement[] el = e.getStackTrace();
				
				if(el.length > 0)
				{
					boolean hasIG = false;
					for(StackTraceElement element : el)
					{
						if(element.toString().contains("infoglue"))
							hasIG = true;
					}
					if(!hasIG)
						continue;
				}
				
				long time = (t.getThreadUserTime(e.getThreadId()) / (1000*1000*100));
				if(time < 10)
					continue;
			%>
				<tr>
				<td><%= e.getThreadId()  %></td>
				<td><%= time %></td>
				<td>
				<% 
					if (el == null || el.length == 0)
	                {
	                        out.print("&nbsp;&nbsp;&nbsp;&nbsp;no stack trace available");
	                        return;
	               	}
	
	                for (int n = 0; n < el.length; n++)
	                {
	                        if (n != 0)
	                                out.print("<br/>");
	
	                        StackTraceElement frame = el[n];
	
	                        if (frame == null) {
	                                out.print("&nbsp;&nbsp;&nbsp;&nbsp;null stack frame");
	                                continue;
	                        }
	
	                        out.print("&nbsp;&nbsp;&nbsp;&nbsp;");
	                        out.print(frame.toString());
	                }
                %>
				</td>
				</tr>
			<%
        	}
        }
		catch(Exception e) {}
	%>
	</table>
	</body>
	</html>
	<%
	}
	%>

                        