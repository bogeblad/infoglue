<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.RedirectController"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%
Exception error = (Exception)pageContext.getRequest().getAttribute("error");
StringBuffer sb = new StringBuffer();
if(error != null)
{
	sb.append("Error:" + error.getMessage() + "<br/>");
	sb.append(error.getStackTrace()[0].toString() + "<br/>");
	sb.append(error.getStackTrace()[1].toString() + "<br/>");
	sb.append(error.getStackTrace()[2].toString() + "<br/>");
	sb.append(error.getStackTrace()[3].toString() + "<br/>");
	sb.append(error.getStackTrace()[4].toString() + "<br/>");
	sb.append(error.getStackTrace()[5].toString() + "<br/>");
	sb.append(error.getStackTrace()[6].toString() + "<br/>");
	sb.append(error.getStackTrace()[7].toString() + "<br/>");
	sb.append(error.getStackTrace()[8].toString() + "<br/>");
	sb.append(error.getStackTrace()[9].toString() + "<br/>");
	sb.append(error.getStackTrace()[10].toString() + "<br/>");
}
pageContext.setAttribute("stacktrace", sb.toString());
%>

<!DOCTYPE HTML>
<html>
  <head>
    <title>InfoGlue Error</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta http-equiv="Expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
	<meta http-equiv="Cache-Control" content="no-cache" />
	<meta http-equiv="Pragma" content="no-cache" />
  	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<style>
		* {
			box-sizing: border-box;
		}
		body {
			color            : #123456;
			background-color : #FAF8F8;
			font-family      : "trebuchet ms", trebuchet;
			font-size        : 14px;
			background		 : url(css/images/v3/body-bg2.jpg) center center fixed no-repeat;
			background-size	 : cover;
			-webkit-background-size	: cover;
			-moz-background-size	: cover;
			-o-background-size		: cover;
		}
		
		.loginBox {
			position: absolute;
		  	top: 0;
		  	right: 0;
		 	bottom: 0;
		 	left: 0;
		 	margin: auto;
		 	width: 410px;
		  	height: 300px;
			min-width: 360px;
			border: 1px solid #eee;
			padding: 30px;
			background-color: white;
		}
		
		.namePart1 {
			font-family: "trebuchet ms", trebuchet;
			font-size: 70px;
			color:#5acbed;
		}
		.namePart2 {
			font-family: "trebuchet ms", trebuchet;
			font-size: 70px;
			color: #FFAD32;
		}
		.loginBody {
			margin-left: 10px;
		}
		
		input[type="text"],input[type="password"] { 
			width: 220px; 
			font-size: 14px; 
			border-radius: 3px;
			font-family: verdana; 
			padding: 8px; 
			background-color: white; 
			border: 1px solid #ccc; 
		}
			
		input:-webkit-autofill {
	        -webkit-box-shadow: 0 0 0px 1000px white inset;
	    }
		.login {
			##margin-top: 20px;
			padding: 4px 10px 4px 4px;
			font-size: 14px;
			font-family: verdana;
			border: 1px solid #ffaa00;
			padding: 10px 10px;
			width: 220px;
			text-align: center;
			border-radius: 3px;
			background: #ffd644; /* Old browsers */
			background: -moz-linear-gradient(top,  #ffd644 0%, #ffad33 100%); /* FF3.6+ */
			background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#ffd644), color-stop(100%,#ffad33)); /* Chrome,Safari4+ */
			background: -webkit-linear-gradient(top,  #ffd644 0%,#ffad33 100%); /* Chrome10+,Safari5.1+ */
			background: -o-linear-gradient(top,  #ffd644 0%,#ffad33 100%); /* Opera 11.10+ */
			background: -ms-linear-gradient(top,  #ffd644 0%,#ffad33 100%); /* IE10+ */
			background: linear-gradient(to bottom,  #ffd644 0%,#ffad33 100%); /* W3C */
			filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ffd644', endColorstr='#ffad33',GradientType=0 ); /* IE6-9 */
		}
		
		label {
			display: none;
		}
	</style> 
	
	<link type="text/css" href="<%= request.getContextPath() %>/script/jqueryplugins-latest/ui/css/jquery-ui.css" rel="stylesheet" />	
	<script type="text/javascript" src="<%= request.getContextPath() %>/script/jquery-latest/jquery.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/script/jqueryplugins-latest/ui/js/jquery-ui.min.js"></script>

	<script type="text/javascript">
	<!--
	
		function expandAndFocus()
		{
			document.inputForm.elements[0].focus();
		}			

		function toggleStacktrace()
		{
			$("#stacktrace").show("slow");
		}

		function closeDialog()
		{
			if(parent && parent.closeInlineDiv) 
				parent.closeInlineDiv(); 
			else if(parent && parent.closeDialog) 
				parent.closeDialog(); 
			else if (window.opener)
				window.close();
			else
				history.back();
		}

		function openReportDialog(dialogId)
		{
			$("#" + dialogId).dialog('open');
		}

		$(function() {

			$("#reportBugDialog").dialog({
				bgiframe: true,
				autoOpen: false,
				height: 500,
				width: 600,
				modal: true,
				buttons: {
					'Submit bug': function() {
						$("#bugForm").submit();
						$(this).dialog('close');
					},
					Cancel: function() {
						$(this).dialog('close');
					}
				},
				close: function() {
				}
			});
		});	
		-->
	</script>
	
</head>

<body onLoad="javascript:expandAndFocus();">

<div id="reportBugDialog" title="Report bug">
	<form id="bugForm" method="post" action="http://www.infoglue.org/bugs/bugreport" target="_blank">
		<input type="hidden" name="currentUrl" id="currentUrl" value="<%= request.getAttribute("javax.servlet.error.request_uri") %>"/>
		<input type="hidden" name="referer" id="referer" value="<c:out value="${header['Referer']}" escapeXml="false"/>"/>
		<input type="hidden" name="principal" id="principal" value="<%= session.getAttribute("org.infoglue.cms.security.user") %>"/>
		<input type="hidden" name="userAgent" id="userAgent" value="<c:out value="${header['User-Agent']}" escapeXml="false"/>" />
		<input type="hidden" name="stacktrace" id="stacktrace" value="<c:out value="${stacktrace}" escapeXml="false"/>"/>
		<input type="hidden" name="infoglueVersion" id="<%= org.infoglue.cms.util.CmsPropertyHandler.getInfoGlueVersion() %> - <%= org.infoglue.cms.util.CmsPropertyHandler.getInfoGlueVersionReleaseDate() %>" />
		<input type="hidden" name="database" id="<%= org.infoglue.cms.util.CmsPropertyHandler.getDatabaseEngine() %>" />
		<fieldset>
			<p>We in the Infoglue community are very interested in getting good bug reports. Please use the form below. The information collected is held to a minimum. No information will be shared with other organisations.</p>
			<label for="name">Name (Optional)</label>
			<input type="text" name="name" id="name" class="text ui-widget-content ui-corner-all" />
			<label for="email">Email (Optional)</label>
			<input type="text" name="email" id="email" value="" class="text ui-widget-content ui-corner-all" />
			<label for="text">Bug description (Optional but please write a meaningful description)</label>
			<textarea name="text" id="text" class="text ui-widget-content ui-corner-all"></textarea>
		</fieldset>
	</form>
</div>

<div id="loginBox" class="loginBox">

	<div class="loginHeader" style="background: url(css/images/v3/infoglueBox.png) no-repeat; text-indent: 90px;"><span class="namePart1">Info</span><span class="namePart2">glue</span></div>
	<div class="loginBody">
		<%
			String redirectSuggestion = (String)request.getAttribute(RedirectController.REDIRECT_SUGGESTION);
			if (redirectSuggestion != null) {
				String redirectName = redirectSuggestion.replaceFirst("http.?://", "");
		%>
			There is no page at this address.<br/>
			There used to be one. It has been moved to:<br/><br/>
			<a href="<%= redirectSuggestion %>" style="word-wrap: break-word;"><%=redirectName %></a>

			<!--
			<%
				java.util.List<String> redirectRules = (java.util.List<String>)request.getAttribute(RedirectController.SITE_NODE_REDIRECT_URLS);
				if (redirectRules != null)
				{
					for (String redirectRule : redirectRules)
					{
						out.print(redirectRule + "\n");
					}
				}
			%>
			 -->
		</p>
		<% } else { %>
			Page not Found or an error occurred. Either way we could not serve your request.<br/><br/>
			System message: <c:out value="${requestScope.error.message}"/><br/><br/>
			<c:if test="${requestScope.error.cause.class.name != 'org.infoglue.cms.exception.PageNotFoundException'}">
			<p>
				Please help us make Infoglue better by filing a bug report<br/>
				<a href="javascript:void(0)" onclick="openReportDialog('reportBugDialog');" class="bug">Report bug</a>
				<a href="javascript:closeDialog();" class="close">Back / Close</a>
			</p>
			</c:if>
		<% } %>
			
	</div>
</div>

</body>
</html>
<%
org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("InfoGlue");
String errorUrl = (String)pageContext.getRequest().getAttribute("javax.servlet.error.request_uri");
Exception e = (Exception)pageContext.getRequest().getAttribute("error");
logger.warn("Error.jsp called - Possible errorUrl:" + errorUrl + ". Message: " + (e == null ? "-none-" : e.getMessage()));
%>

