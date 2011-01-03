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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>
	<title>InfoGlue Error</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta http-equiv="Expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
	<meta http-equiv="Cache-Control" content="no-cache" />
	<meta http-equiv="Pragma" content="no-cache" />

	<style>
		
		body {
			color            : #123456;
			background-color : #FFFFFF;
			font-family      : verdana, arial, sans-serif;
			font-size        : 8pt;
		}
		
		.loginbox {
			background-color : #FFFFFF;
			border-style	 : solid;
			border-width	 : 1px;
			border-color	 : #cecbce;
		}

		.borderedCell {
			background-color : #FFFFFF;
			border-style	 : solid;
			border-width	 : 1px;
			border-color	 : #cecbce;
		}
		
		td {
			font-family      : verdana, arial, sans-serif;
			font-size        : 8pt;
			color		 	 : #333333;
		}
		
		.input {
			font-family      : verdana, arial, sans-serif;
			font-size        : 8pt;
			border-style	 : solid;
			border-width	 : 1px;	
			border-color     : #cecbce;
		}
		
		div.fullymarginalized {
			margin-top		 : 20%;
			margin-bottom	 : 20%;
			width 			 : 100%;	
		}

		td.headline {
			font-size        : 10pt;
			font-weight		 : bold;
		}

	</style> 
	
	<link type="text/css" href="<%= request.getContextPath() %>/script/jqueryplugins/ui/jquery-ui-1.7.2.full.redmond/css/redmond/jquery-ui-1.7.2.custom.css" rel="stylesheet" />	
	<script type="text/javascript" src="<%= request.getContextPath() %>/script/jquery/jquery-1.4.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/script/jqueryplugins/ui/jquery-ui-1.7.2.full.redmond/js/jquery-ui-1.7.2.custom.min.js"></script>

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
	
	<style type="text/css">
		body { font-size: 62.5%; }
		label, input { display:block; }
		input.text { margin-bottom:12px; width:95%; padding: .4em; }
		textarea { margin-bottom:12px; width:95%; height: 120px; padding: .4em; }
		fieldset { padding:0; border:0; margin-top:10px; }
		h1 { font-size: 1.2em; margin: .6em 0; }
		div#users-contain {  width: 350px; margin: 20px 0; }
		div#users-contain table { margin: 1em 0; border-collapse: collapse; width: 100%; }
		div#users-contain table td, div#users-contain table th { border: 1px solid #eee; padding: .6em 10px; text-align: left; }
		.ui-button { outline: 0; margin:0; padding: .4em 1em .5em; text-decoration:none;  !important; cursor:pointer; position: relative; text-align: center; }
		.ui-dialog .ui-state-highlight, .ui-dialog .ui-state-error { padding: .3em;  }
		
		.bug { display: block; float: left; line-height: 24px; width: 100%; background: url(<%= request.getContextPath() %>/images/v3/bug.png) no-repeat 0px 2px; text-indent: 24px; }
		.stack { display: block; float: left; line-height: 24px; width: 100%; background: url(<%= request.getContextPath() %>/images/v3/stack.png) no-repeat 0px 2px; text-indent: 24px; }
		.close { display: block; float: left; line-height: 24px; width: 100%; background:url(<%= request.getContextPath() %>/images/v3/closeWindowIcon.gif) no-repeat 3px 4px; text-indent: 24px; margin-top: 20px; }
		
	</style>
	

</head>

<body>

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

<div class="fullymarginalized">

<table class="loginbox" align="center" border="1" cellspacing="5" cellpadding="0">
<tr>
	<td valign="top" class="borderedCell"><img src="<%= request.getContextPath() %>/images/login.jpg" width="130" height="237"/></td>
	<td valign="top" class="borderedCell">
		<table align="center" border="0" cellspacing="0" cellpadding="0" width="200">
		<tr>
			<td colspan="2" style="background-image: url(<%= request.getContextPath() %>/images/errorHeaderBackground.gif); background-repeat: repeat-x;" align="center"><img src="images/error.jpg"></td>
		</tr>	
		<tr>
			<td colspan="2"><img src="<%= request.getContextPath() %>/images/trans.gif" width="1" height="20"></td>
		</tr>
		<tr>
			<td><img src="<%= request.getContextPath() %>/images/trans.gif" width="20" height="1"></td>
			<td>
				Page not Found or an error occurred. Either way we could not serve your request.<br/><br/>
				System message: <c:out value="${requestScope.error.message}"/><br/><br/>
				
				<p>
					Please help us make Infoglue better by filing a bug report<br/>
					<a href="#" onclick="openReportDialog('reportBugDialog');" class="bug">Report bug</a> 
					<a href="javascript:closeDialog();" class="close">Back / Close</a>
				</p>
								
			</td>
		</tr>
		</table>
	</td>
</tr>	
</table>

</div>

 
</body>
</html>
<%
org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("InfoGlue");
String errorUrl = (String)pageContext.getRequest().getAttribute("javax.servlet.error.request_uri");
logger.warn("Error.jsp called - Possible errorUrl:" + errorUrl);

Exception e = (Exception)pageContext.getRequest().getAttribute("error");
if(e != null)
{
  System.out.println("Error: " + e.getMessage());
  System.out.println(e.getStackTrace()[0].toString());
  System.out.println(e.getStackTrace()[1].toString());
  System.out.println(e.getStackTrace()[2].toString());
  System.out.println(e.getStackTrace()[3].toString());
  System.out.println(e.getStackTrace()[4].toString());
  System.out.println(e.getStackTrace()[5].toString());
}
%>