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

	<%
	InfoGluePrincipal infoGluePrincipal = (InfoGluePrincipal)session.getAttribute("org.infoglue.cms.security.user");
	long workflowId = Long.valueOf(request.getParameter("workflowId")).longValue();

	WorkflowController workflowController = WorkflowController.getController();
	Map properties = workflowController.getProperties(infoGluePrincipal, workflowId);
	%>

	<body>
		<!-- Generated at www.csscreator.com -->
		<div id="pagewidth">
			<div id="header" >
				<div class="content">
					<div class="header">CREATE NEWS</div>
				</div>
			</div>
			<div id="outer" >
				<div id="inner">
					<div id="leftcol" >
							<div class="rubrik" >
								<p>THE PROCESS OF CREATING A NEWS ITEM</p>
							</div>
							<div class="content" style="height:400px">

							<%
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
							<div class="subject">Create news</div>

							<div class="content">
								<div class="info">
									<p>Here we have the second step and all the data shown here are fetched from a persistent storage that now has this
									connected to this workflow instance. This has the benefits that we don't have to handle storage between steps ourselves, that
									is done by the workflow engine.</p>
									<p>
									When you publish a news will be created and put under the example repository www.officestand.com under the folder
									"News items". If you don't have this site or that folder it will not work.
									</p>
								</div>

								<div class="workarea">
									<div class="form">
										<form name="form1" id="form1" method="GET" action="<%= request.getParameter("returnAddress") %>">
										<input name="workflowId" type="hidden" value="<%= request.getParameter("workflowId") %>" />
										<input name="actionId" type="hidden" value="<%= request.getParameter("actionId") %>" />

										  <p>
										  	News name<br />
										    <span style="font-decoration:none; font-weight: normal;"><%= properties.get("name") %></span>
										  </p>
										  <p>
										  	Title<br />
										    <span style="font-decoration:none; font-weight: normal;"><%= properties.get("title") %></span>
										    <br />
										    <br />
										    Navigation Title<br />
										    <span style="font-decoration:none; font-weight: normal;"><%= properties.get("navigationTitle") %></span>
										    <br />
										    <br />
										    LeadIn<br />
										    <span style="font-decoration:none; font-weight: normal;"><%= properties.get("leadIn") %></span>
										    <br />
										    <br />
										    Full text<br />
										    <span style="font-decoration:none; font-weight: normal;"><%= properties.get("fullText") %></span>
										    <br />
										    <br />
										    <input type="submit" value="Approve and publish" />
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