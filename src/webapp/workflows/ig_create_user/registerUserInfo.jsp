<%@ page import="org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController,
				 org.infoglue.cms.security.InfoGluePrincipal,
				 org.infoglue.cms.entities.mydesktop.WorkflowStepVO,
				 java.util.Map,
				 java.util.Iterator,
				 java.util.List"%>
				 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" >

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" >

	<head>
		<title>CssCreator-->XHTML 1.0 Strict standard template </title>
		<meta http-equiv="content-type" content="text/html;charset=utf-8" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
		<link rel="stylesheet" href="css/style.css" type="text/css" />
	</head>

	<body>
		<!-- Generated at www.csscreator.com -->
		<div id="pagewidth">
			<div id="header" >
				<div class="content">
					<div class="header">CREATE USER</div>
				</div>
			</div>
			<div id="outer" >
				<div id="inner">
					<div id="leftcol" >
							<div class="rubrik" >
								<p>THE PROCESS OF CREATING A NEW USER</p>
							</div>
							<div class="content" style="height:400px">
							
							<%
								InfoGluePrincipal infoGluePrincipal = (InfoGluePrincipal)session.getAttribute("org.infoglue.cms.security.user");
							
								WorkflowController workflowController = WorkflowController.getController();
							
								String workflowId = "93";
								if(!request.getParameter("workflowId").equals(""))
								  workflowId = request.getParameter("workflowId");
								
								List allSteps 		= workflowController.getAllSteps(infoGluePrincipal, new Long(workflowId).longValue());
								List currentSteps 	= workflowController.getCurrentSteps(infoGluePrincipal, new Long(workflowId).longValue());
								List historySteps 	= workflowController.getHistorySteps(infoGluePrincipal, new Long(workflowId).longValue());
								
							%>
								<p><b>WORKFLOW STEPS</b></p>
								<div class="event_done">
							<%
								Iterator stepIterator = historySteps.iterator();
								while(stepIterator.hasNext())
								{	
									WorkflowStepVO step = (WorkflowStepVO)stepIterator.next();
							%>
								  <p><b><%= step.getName() %> (#<%= step.getStepId() %>)<br/>
								  </b><i><%= step.getCaller() %></i></p> 
							<%
								}
							%>
								</div>
								
								<div class="event_active">
							
							<%
								stepIterator = currentSteps.iterator();
								while(stepIterator.hasNext())
								{	
									WorkflowStepVO step = (WorkflowStepVO)stepIterator.next();
							%>
								  <p><b><%= step.getName() %> (#<%= step.getStepId() %>)<br/></p> 
							<%
								}
							%>
								</div>
								
								<div class="event_comming">
								
							<%
								stepIterator = allSteps.iterator();
								while(stepIterator.hasNext())
								{	
									WorkflowStepVO step = (WorkflowStepVO)stepIterator.next();

									boolean futureStep = true;
									Iterator historyStepsIterator = historySteps.iterator();
									while(historyStepsIterator.hasNext())
									{
										WorkflowStepVO historyStep = (WorkflowStepVO)historyStepsIterator.next();
										if(step.getName().equals(historyStep.getName()))
								      		futureStep = false;
								    }
								    
								    Iterator currentStepsIterator = currentSteps.iterator();
									while(currentStepsIterator.hasNext())
									{
										WorkflowStepVO currentStep = (WorkflowStepVO)currentStepsIterator.next();
										if(step.getName().equals(currentStep.getName()))
								      		futureStep = false;
								    }
								    
								  	if(futureStep)
								  	{
								  	%>
								  	
								    <p>
								      <b><%= step.getName() %> (#<%= step.getStepId() %>)</b></p>
								    <% 
								    }		
								}
							%>
								</div>
								
							</div>
						</div>
						<div id="maincol" >
							<div class="subject">Create user</div>
						
							<div class="content">
								<div class="info">
									<p>This workflow is a very simple example of a workflow - just one step really.</p>
								</div>
								<div class="manual">
									<p>
										The workflow do actually insert a new user in the system so don't overuse it and make sure to remove the users later.
									</p>
								</div>
								<div class="workarea">
									<div class="form">
										<form name="form1" id="form1" method="GET" action="<%= request.getParameter("returnAddress") %>">
										<input name="workflowId" type="hidden" value="<%= request.getParameter("workflowId") %>" />
										<input name="actionId" type="hidden" value="<%= request.getParameter("actionId") %>" />

											<p>
												First name<br />
											    <input type="text" class="longtextfield" name="firstName" value=""/>
											</p>
											<p>
												Last name<br />
											    <input type="text" class="longtextfield" name="lastName" value=""/>
											</p>
											<p>
											    User name<br />
											    <input type="text" class="longtextfield" name="userName" value=""/>
											</p>
											<p>
												Password<br />
											    <input type="text" class="longtextfield" name="password" value=""/>
											</p>
											<p>
												Email<br />
											    <input type="text" class="longtextfield" name="email" value=""/>
											</p>
											<p>
											    <input type="submit" name="Submit" value="Create User" />
											</p>
											  

										</form>
									</div>
								</div>
								
							</div>
						</div>
						
						<div class="clr"></div>
						<!-- close inner and outer --></div>
				</div>
				<div id="footer" >
					<div class="content"> </div>
				</div>
				<div class="clr"></div>
			</div>
		</div>
	</body>

</html>

<!--
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Untitled Document</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
</head>

<body>
<p><font size="+2" face="Verdana, Arial, Helvetica, sans-serif">Register User</font> </p>
<form name="form1" id="form1" method="GET" action="<%= request.getParameter("returnAddress") %>">
<input name="workflowId" type="hidden" value="<%= request.getParameter("workflowId") %>" />
<input name="actionId" type="hidden" value="<%= request.getParameter("actionId") %>" />
  <p>First name<br />
    <input type="text" name="firstName" value="Mattias"/>
    </font></p>
  <p><font size="-1" face="Verdana, Arial, Helvetica, sans-serif">Last name<br />
    </font><font size="-1" face="Verdana, Arial, Helvetica, sans-serif"> 
    <input type="text" name="lastName" value="Bogeblad"/>
    <br />
    <br />
    </font><font size="-1" face="Verdana, Arial, Helvetica, sans-serif">User name<br />
    <input type="text" name="userName" value="blade"/>
    <br />
    <br />
    Password<br />
    <input type="text" name="password" value="blade"/>
    <br />
    <br />
    Email<br />
    <input type="text" name="email" value="mattias.bogeblad@sprawlsolutions.se"/>
    <br />
    </font><br />
    <input type="submit" name="Submit" value="Submit" />
  </p>
</form>
</body>
</html>
-->