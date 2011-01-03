<% 
	session.putValue("password", request.getParameter("j_password"));
	String sessionId = "";
	
	Cookie[] cookies = request.getCookies();
	if(cookies != null)
	{
		for (int i=0; i < cookies.length; i++)
		{		
			System.out.println("COOKIE NAME IN JSP:" + cookies[i].getName());
			if(cookies[i].getName().equals("JSESSIONID"))
				sessionId = cookies[i].getValue();
		}
	}
%>

<%
	String j_username = request.getParameter("j_username");
  	String j_password = request.getParameter("j_password");
	//String url = "j_security_check;jsessionid=" + sessionId + "?j_username=" + java.net.URLEncoder.encode(j_username) + "&j_password=" + java.net.URLEncoder.encode(j_password);
	String url = "j_security_check?j_username=" + java.net.URLEncoder.encode(j_username) + "&j_password=" + java.net.URLEncoder.encode(j_password) + "&jsessionid=" + sessionId;
	//out.println(url); 
	response.sendRedirect(url);
%>
