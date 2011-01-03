<%@ page import="org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController,
				 org.infoglue.cms.security.InfoGluePrincipal,
				 org.infoglue.cms.entities.mydesktop.WorkflowStepVO,
				 com.opensymphony.module.propertyset.PropertySet,
				 java.util.Map,
				 java.util.Iterator,
				 java.util.List"%>
				 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" >

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" >

	<head>
		<title>CREATE NEWS GU - Step 3 of 3</title>
		<meta http-equiv="content-type" content="text/html;charset=utf-8" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
		<link rel="stylesheet" href="css/style.css" type="text/css" />
	
		<script type="text/javascript">
			
			function resize()
			{
				window.resizeTo(1000, 700);
			}
			
			//*******************************************
			//This method refreshes the caller
			//*******************************************
		
			function refreshCaller()
			{
				document.getElementById('hiddenFrame').src = "<%= request.getParameter("returnAddress") %>?workflowId=<%= request.getParameter("workflowId") %>&actionId=<%= request.getParameter("actionId") %>";
										
				if(window.opener && window.opener.parent && window.opener.parent.frames['main'])
					window.opener.parent.frames['main'].location.reload();
				else
					window.opener.location.reload();	
		
				setTimeout("window.close();",500);
			}
			
		</script>
	</head>

	<body onload="resize();">
		<!-- Generated at www.csscreator.com -->
		<div id="pagewidth">
			<div id="header" >
				<div class="content">
					<div class="header">CREATE NEWS GU</div>
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
									<p>The news was created. Press the button below to close this dialog and return to where you came from.</p>
								</div>
								<div class="manual">
									<p>
										<a href="javascript:refreshCaller();">Close</a>
									</p>
								</div>
								
								<iframe frameborder="1" name="hiddenFrame" id="hiddenFrame" src="" width="0" height="0" align="baseline" style="width:0px; height=0px; display: none;></iframe>
						
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