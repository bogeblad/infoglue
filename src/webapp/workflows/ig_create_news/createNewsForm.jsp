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
							<div class="subject">Create news</div>
						
							<div class="content">
								<div class="info">
									<p>This workflow is an example how you can build a customer specific news creation process where a users follows a wizard to input the data needed and the finished news item ends up in a predefined place.</p>
								</div>
								<div class="manual">
									<p>
										<!--
										<font color="red"><b>
										You cannot run this workflow to the end it you don't modify the workflow definition createNews.xml and set<br>
										<br> 
										propertySet.setString("parentContentId", "195");<br>
										propertySet.setString("contentTypeDefinitionId", "3");<br>
										propertySet.setString("repositoryId", "6");<br>
										<br>
										to values which are correct to your installation. The values could be looked up instead but for this example they are hardcoded.<br></font><br><br>
										-->
										The idea with this small and somewhat silly workflow is just to show some concepts of steps, datahandling and other
										common issues in workflows. We don't want you to think this is a workflow we recommend as there are many
										things we decided to leave out of it. Instead it can act as an inspiration and example.
									</p>
								</div>
								<div class="workarea">
									<div class="form">
										<form name="form1" id="form1" method="GET" action="<%= request.getParameter("returnAddress") %>">
										<input name="workflowId" type="hidden" value="<%= request.getParameter("workflowId") %>" />
										<input name="actionId" type="hidden" value="<%= request.getParameter("actionId") %>" />

										<p>
										  	News name<br />
										    <input type="text" class="longtextfield" name="name" value=""/>
										    </font>
										  </p>
										  <p>
										  	Title<br />
										    <input type="text" class="longtextfield" name="title" value=""/>
										    <br />
										    <br />
										    Navigation Title<br />
										    <input type="text" class="longtextfield" name="navigationTitle" value=""/>
										    <br />
										    <br />
										    LeadIn<br />
										    <input type="text" class="longtextfield" name="leadIn" value=""/>
										    <br />
										    <br />
										    Full text<br />
										    <textarea class="normaltextarea" name="fullText"></textarea>
										    <br />
										    <br />
										    <input type="submit" value="Preview" />
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