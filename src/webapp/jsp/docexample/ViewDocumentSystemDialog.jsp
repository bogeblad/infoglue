<%@ page import="org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController,
				 org.infoglue.cms.security.InfoGluePrincipal,
				 java.util.Map,
				 java.util.Iterator,
				 java.util.List"%>
				 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" >

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" >

	<head>
		<title>Choose file from Docushare</title>
		<meta http-equiv="content-type" content="text/html;charset=utf-8" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
		<link rel="stylesheet" href="css/style.css" type="text/css" />
	
		<script type="text/javascript">
			
			function resize()
			{
				window.resizeTo(1000, 700);
			}
			
			function submitBindning()
			{
			    //alert("Here we commit the binding and close the window...");
				entityId = "1213";
				path = "Styrelsemöte 2004-10-11";
			    var repositoryId = "<%= request.getParameter("repositoryId") %>";
				bindingTypeId = 0; //0 = Content, 1 = SiteNode
				
				qualifyerXML = "<?xml version='1.0' encoding='UTF-8'?>";
				qualifyerXML += "<qualifyer>";
				qualifyerXML += 	"<contentId>" + entityId + "</contentId>";
				qualifyerXML += "</qualifyer>";
	
				var url = "ViewSiteNodePageComponents!addComponentPropertyBinding.action?siteNodeId=<%= request.getParameter("siteNodeId") %>&languageId=3&contentId=-1&entity=Content&entityId=" + entityId + "&componentId=<%= request.getParameter("componentId") %>&propertyName=Documents&path=" + escape(path);
				//alert('Calling:' + url);
			    //alert('qualifyerXML:' + qualifyerXML);
			    self.opener.location.href = url;
			    window.close();
			    //document.inputForm.submit();
			}
			
		</script>
	</head>

	<body onload="resize();">
		<div id="pagewidth">
			<div id="header" >
				<div class="content">
					<div class="header">Choose files from Docushare</div>
				</div>
			</div>
			<div id="outer" >
				<div id="inner">
						<div id="maincol" >
							<div class="subject">Select which file to show on the site</div>
							<div class="content">
								<div class="workarea">
									<div class="form">
										<table border="0" cellpadding="2" cellspacing="0" width="100%">
										<tr>
											<td><img src="images/Contentdocument.gif" width="20" height="20"></td>
											<td><a href="javascript:submitBindning();">Styrelsemöte 2004-10-11.doc</td>
											<td>Per Persson</td>
											<td>2004-10-12</td>
										</tr>
										<tr>
											<td><img src="images/Contentdocument.gif" width="20" height="20"></td>
											<td><a href="javascript:submitBindning();">Protokoll bolagsstämma 2004-11-20.doc</td>
											<td>Anders Andersson</td>
											<td>2004-12-01</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="javascript:submitBindning();">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="javascript:submitBindning();">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="#">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="#">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="#">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="#">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="#">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										<tr>
											<td><img src="images/pdf.gif" width="20" height="20"></td>
											<td><a href="#">Årsredovisning 2004.pdf</td>
											<td>Per Persson</td>
											<td>2004-03-10</td>
										</tr>
										</table>
									</div>
								</div>
								
							</div>
						</div>
						
						<div class="clr"></div>
					</div>
				</div>
				<div id="footer" >
					<div class="content"> </div>
				</div>
				<div class="clr"></div>
			</div>
		</div>
	</body>

</html>