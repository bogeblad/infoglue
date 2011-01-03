<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "DTD/xhtml1-strict.dtd">

<html>
  <head>
    <title>Not authorized to login</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  	
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
			border-color	 : #000000;
			width:           : 130px;
		}

		.borderedCell {
			background-color : #FFFFFF;
			border-style	 : solid;
			border-width	 : 1px;
			border-color	 : #000000;
		}
		
		td {
			font-family      : verdana, arial, sans-serif;
			font-size        : 8pt;
			font-color		 : #333333;
		}
		
		.input {
			font-family      : verdana, arial, sans-serif;
			font-size        : 8pt;
			border-style	 : solid;
			border-width	 : 1px;	
			border-color     : #000000;
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
	
	<script type="text/javascript" language="Javascript">
	<!--
	
		function expandAndFocus()
		{
			
			//alert(parent);
			//if (parent.frames.length != 0)
			//{
			//	url = "ViewCMSTool.action";
			//	details = "toolbar=no,status=yes,scrollbars=no,location=no,menubar=no,directories=no,resizable=yes,width=1000,height=740,left=5,top=5";
			//	newWin=window.open(url, "CMS", details);
			//	newWin.focus();
			//	window.close();
			//} 	
			
			document.inputForm.elements[0].focus();
		}			

		-->
	</script>

</head>

<body onLoad="javascript:expandAndFocus();">

<div class="fullymarginalized">
<!--<form name="inputForm" method="POST" action="collectLogin.jsp">-->
<!--<form name="inputForm" method="POST" action="j_security_check">-->
<form name="inputForm" method="POST" action="$referringUrl">
<table class="loginbox" align="center" border="0" cellspacing="5" cellpadding="0">
<tr>
	<td valign="top" class="borderedCell"><img src="images/login.jpg" width="130" height="235"></td>
	<td valign="top" class="borderedCell">
		<table align="center" border="0" cellspacing="0" cellpadding="0" width="80">
		<tr>
			<td colspan="2" style="border-bottom: 1px solid #000000;"><img src="images/loginHeader.jpg" width="210" height="38"></td>
		</tr>	
		<tr>
			<td colspan="2"><img src="images/trans.gif" width="1" height="20"></td>
		</tr>	
		<tr>
			<td><img src="images/trans.gif" width="20" height="1"></td>
			<td>You are not authorized to use<br> the administrative tools in InfoGlue. <br>
				<br>Please talk to the system<br> administrator for access!<br>
				<!--<%= request.getParameter("errorMessage") %>-->
				<% session.invalidate(); %>
				<br>
				<br>
				<a href="ViewCMSTool.action">Try again</a> | <a href="Login!logout.action">Logout</a>
				
			</td>
		</tr>
		</table>
	</td>
</tr>	
</table>

</form>
</div>
 
</body>
</html>