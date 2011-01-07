<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.LanguageController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.ContentController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController"%>
<%@page import="org.infoglue.cms.entities.content.*"%>
<%@page import="org.infoglue.cms.entities.management.*"%>

<html>
<head>
	<title>Small performance test</title>

	<link rel="stylesheet" type="text/css" href="../css/v3/infoglue.css" />
	<style type="text/css">
	<!--
		#perfTestTable {
			font-size: 12px;
			margin-left: 16px;
			margin-right: 16px;
		}
		#perfTestTable td,
		#perfTestTable th {
			padding: 2px;
		}
		#perfTestTable th {
			font-weight: bold;
		}
		#perfTestTable .currentBetter {
			color: green;
		}
		#perfTestTable .currentWorse {
			color: red;
		}
		#perfTestTable .ref {
			color: black;
		}
	-->
	</style>
</head>
<body class="generaltooledit" style="padding-top: 20px;">

<h3>Infoglue small performance test</h3>
<table id="perfTestTable" border="1" style="border: 1px solid #ccc; border-collapse:collapse;" cellpadding="4">
<thead>
<tr>
	<th>Test</th>
	<th>Items</th>
	<th>Elapsed time (ms)</th>
	<th>Average time (ms / item)</th>
	<th class='ref'>Reference run items</th>
	<th class='ref'>Reference run elapsed time (ms)</th>
	<th class='ref'>Reference run average time (ms / item)</th>
</tr>
</thead>
<%
org.infoglue.deliver.util.Timer timer = new org.infoglue.deliver.util.Timer();

java.util.Collection nodes = SiteNodeController.getAllVOObjects(org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl.class, "name", "asc");
long elapsedTime = timer.getElapsedTime();
float thisAvg = (float)elapsedTime/(float)nodes.size();
float refAvg = (float)89/(float)369;
String tdClass = "currentBetter";
if(thisAvg > refAvg)
	tdClass = "currentWorse";
	
out.println("<tr><td>Reading sitenodes</td><td>" + nodes.size() + "</td><td>" + elapsedTime + "</td><td class='" + tdClass + "'>" + thisAvg + "</td><td class='ref'>369</td><td class='ref'>89</td><td class='ref'>" + refAvg + "</td></tr>");

java.util.Collection contents = ContentController.getAllVOObjects(org.infoglue.cms.entities.content.impl.simple.ContentImpl.class, "name", "asc");
elapsedTime = timer.getElapsedTime();
thisAvg = (float)elapsedTime/(float)contents.size();
refAvg = (float)60/(float)969;
tdClass = "currentBetter";
if(thisAvg > refAvg)
	tdClass = "currentWorse";


out.println("<tr><td>Reading contents</td><td>" + contents.size() + "</td><td>" + elapsedTime + "</td><td class='" + tdClass + "'>" + thisAvg + "</td><td class='ref'>969</td><td class='ref'>60</td><td class='ref'>" + refAvg + "</td></tr>");

java.util.Iterator contentsIterator = contents.iterator();
while(contentsIterator.hasNext())
{
	ContentVO contentVO = (ContentVO)contentsIterator.next();
	LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());

	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId());
}

elapsedTime = timer.getElapsedTime();
thisAvg =  (float)elapsedTime/(float)contents.size();
refAvg = (float)1062/(float)969;
tdClass = "currentBetter";
if(thisAvg > refAvg)
	tdClass = "currentWorse";
	
out.println("<tr><td>Reading latest contentVersion</td><td>" + contents.size() + "</td><td>" + elapsedTime + "</td><td class='" + tdClass + "'>" + thisAvg + "</td><td class='ref'>969</td><td class='ref'>1062</td><td class='ref'>" + refAvg + "</td></tr>");
%>
</table>

<p>If you have bad results on these tests there are a couple of things you should do to pinpoint the problem. This is an area where you also can benefit from external help to quickly get results. Contact the community for consultant options.</p>

</body>
</html>