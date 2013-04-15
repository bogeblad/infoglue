<%@page import="org.infoglue.cms.entities.structure.SiteNodeVO"%>
<%@page import="org.infoglue.deliver.util.CacheController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.BaseController"%>
<%@page import="org.infoglue.deliver.applications.actions.InfoGlueComponent"%>
<%@page import="org.infoglue.deliver.controllers.kernel.impl.simple.ComponentLogic"%>
<%@page import="org.infoglue.cms.util.CmsPropertyHandler"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy"%>
<%@page import="org.infoglue.cms.security.InfoGluePrincipal"%>
<%@page import="org.infoglue.deliver.applications.databeans.DeliveryContext"%>
<%@page import="org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController"%>
<%@page import="org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController"%>
<%@page import="org.infoglue.deliver.util.BrowserBean"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService"%>
<%@page import="org.infoglue.deliver.applications.databeans.DatabaseWrapper"%>
<%@page import="org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController"%>
<%@page import="org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.LanguageController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.ContentController"%>
<%@page import="org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController"%>
<%@page import="org.infoglue.cms.entities.content.*"%>
<%@page import="org.infoglue.cms.entities.management.*"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<html>
<head>
	<title>Small performance test</title>

	<link rel="stylesheet" type="text/css" href="../css/infoglue.css" />
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
		#testsList {
			list-style-type: none;
		}
		.commonLink {
			margin: 16px;
		}
		.startButton {
			margin: 16px;
		}
	-->
	</style>
</head>
<body class="generaltooledit" style="padding-top: 20px;">

<h3>Infoglue small performance test</h3>
<p>Select what tests to run.</p>
<%
boolean runSiteNodeListTest;
boolean runContentListTest;
boolean runContentVersionListTest;
boolean runContentAttributeListTest;
runSiteNodeListTest = request.getParameter("test_siteNodeList") != null;
runContentListTest = request.getParameter("test_contentList") != null;
runContentVersionListTest = request.getParameter("test_contentVersionRead") != null;
runContentAttributeListTest = request.getParameter("test_contentAttributeRead") != null;
if (request.getParameter("ISPOSTBACK") == null)
{
	runSiteNodeListTest = true;
	runContentListTest = true;
}
%>
<form method="post">
	<input type="hidden" name="ISPOSTBACK" value="true">
	<ul id="testsList">
		<li>
			<label>
				<input id="test_siteNodeList" type="checkbox" <%= runSiteNodeListTest ? "checked=checked" : "" %> name="test_siteNodeList" />
				SiteNode list
			</label>
		</li>
		<li>
			<label>
				<input id="test_contentList" type="checkbox" <%= runContentListTest ? "checked=checked" : "" %> name="test_contentList" />
				Content list
			</label>
		</li>
		<li id="test_contentVersionReadListItem" <%= runContentListTest ? "" : "style='display:none;'" %>>
			<label>
				<input id="test_contentVersionRead" type="checkbox" <%= runContentVersionListTest ? "checked=checked" : "" %> name="test_contentVersionRead" />
				Content version reads
			</label>
		</li>
		<li id="test_contentAttributeReadListItem" <%= runContentListTest ? "" : "style='display:none;'" %>>
			<label>
				<input id="test_contentAttributeRead" type="checkbox" <%= runContentAttributeListTest ? "checked=checked" : "" %> name="test_contentAttributeRead" />
				Content attribute reads
			</label>
		</li>
	</ul>
	<input type="submit" value="Perform tests" class="startButton" />
</form>
<script type="text/javascript">
	document.getElementById("test_contentList").onchange = function(event) {
		if (event.target.checked)
		{
			document.getElementById("test_contentVersionReadListItem").style.display = 'list-item';
			document.getElementById("test_contentAttributeReadListItem").style.display = 'list-item';
		}
		else
		{
			document.getElementById("test_contentVersionReadListItem").style.display = 'none';
			document.getElementById("test_contentVersionReadListItem").checked = false;
			document.getElementById("test_contentAttributeReadListItem").style.display = 'none';
			document.getElementById("test_contentAttributeReadListItem").checked = false;
		}
	};
</script>
<a class="commonLink" href="http://www.infoglue.org/metrics">View results for other installations</a>
<c:if test="${not empty param.ISPOSTBACK}">
	<%
		String tableName = "cmSiteNode";
		String columnName = "repositoryId";
		if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
			tableName = "cmSiNo";

		TableCount tableCount = BaseController.getTableCount(tableName, columnName);
		int numberOfSiteNodes = -1;
		if(tableCount != null)
			numberOfSiteNodes = tableCount.getCount();

		tableName = "cmContent";

		int numberOfContents = -1;
		tableCount = BaseController.getTableCount(tableName, columnName);
		if(tableCount != null)
			numberOfContents = tableCount.getCount();

		tableName = "cmContentVersion";
    	columnName = "contentId";
    	if(CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
    	{
    		tableName = "cmContVer";
        	columnName = "contId";
    	}

    	int numberOfContentVersions = -1;
    	tableCount = BaseController.getTableCount(tableName, columnName);
    	if(tableCount != null)
    		numberOfContentVersions = tableCount.getCount();

		float sampleSize = 1000f;
	%>

	<p>
		The reference installation have 150 000 site nodes.<br/>
		The reference installation have 344 663 contents.<br/>
		The reference installation have 356 566 content versions.
	</p>

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

	float elapsedTime;
	float thisAvg;
	float refAvg;
	float compareValue;
	String tdClass;

    CacheController.clearCastorCaches();
    CacheController.clearCaches(null, null, null);

	org.infoglue.deliver.util.Timer timer = new org.infoglue.deliver.util.Timer();
	if (request.getParameter("test_siteNodeList") != null)
	{
		java.util.List<SiteNodeVO> nodes = SiteNodeController.getController().getSiteNodeVOList(false, 0, (int)sampleSize);
		//java.util.Collection nodes = SiteNodeController.getAllVOObjects(org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl.class, "name", "asc");
		elapsedTime = timer.getElapsedTime();
		thisAvg = (float)elapsedTime/(float)nodes.size();

		float samleSiteNodeTime = 4500f;
		float sampleTotalSizeSiteNode = 150000f;

		refAvg = samleSiteNodeTime / sampleSize;
		float refCompareValueSiteNode = (sampleTotalSizeSiteNode / sampleSize) * samleSiteNodeTime;
		compareValue = (numberOfSiteNodes / sampleSize) * elapsedTime;

		tdClass = "currentBetter";
		if(compareValue > refCompareValueSiteNode)
			tdClass = "currentWorse";

		out.println("<tr><td>Reading sitenodes</td><td>" + nodes.size() + "</td><td>" + elapsedTime + "</td><td class='" + tdClass + "' title='(" + numberOfSiteNodes + " / " + sampleSize + ") * " + elapsedTime + " = " + compareValue +" (" + refCompareValueSiteNode + ")'>" + thisAvg + "</td><td class='ref'>" + sampleSize + "</td><td class='ref'>" + samleSiteNodeTime + "</td><td class='ref'>" + refAvg + "</td></tr>");
	}

	java.util.List<ContentVO> contents = null;
	if (request.getParameter("test_contentList") != null)
	{
		//java.util.Collection contents = ContentController.getAllVOObjects(org.infoglue.cms.entities.content.impl.simple.ContentImpl.class, "name", "asc");
		contents = ContentController.getContentController().getContentVOList((int)sampleSize);
		elapsedTime = timer.getElapsedTime();
		thisAvg = (float)elapsedTime/(float)contents.size();

		float sampleTotalSizeContent = 344663f;
		float samleContentTime = 50f;

		refAvg = samleContentTime / sampleSize;
		float refCompareValueContent = (sampleTotalSizeContent / sampleSize) * samleContentTime;
		compareValue = (numberOfContents / sampleSize) * elapsedTime;

		tdClass = "currentBetter";
		if(compareValue > refCompareValueContent)
			tdClass = "currentWorse";

		out.println("<tr><td>Reading contents</td><td>" + contents.size() + "</td><td>" + elapsedTime + "</td><td class='" + tdClass + "' title='(" + numberOfContents + " / " + sampleSize + ") * " + elapsedTime + " = " + compareValue +" (" + refCompareValueContent + ")'>" + thisAvg + "</td><td class='ref'>" + sampleSize + "</td><td class='ref'>" + samleContentTime + "</td><td class='ref'>" + refAvg + "</td></tr>");
	}

	if (contents != null && request.getParameter("test_contentVersionRead") != null)
	{
		java.util.Iterator<ContentVO> contentsIterator = contents.iterator();
		while(contentsIterator.hasNext())
		{
			ContentVO contentVO = contentsIterator.next();
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
			ContentVersionVO contentVersionVO = null;
			if(contentVO != null && masterLanguageVO != null)
				contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId());
		}

		elapsedTime = timer.getElapsedTime();
		thisAvg =  (float)elapsedTime/(float)contents.size();

		float sampleTotalSizeContentVersions = 344663f;
		float samleContentVersionTime = 4488f;

		refAvg = samleContentVersionTime / sampleSize;
		float refCompareValueContentVersion = (sampleTotalSizeContentVersions / sampleSize) * samleContentVersionTime;
		compareValue = (numberOfContentVersions / sampleSize) * elapsedTime;

		tdClass = "currentBetter";
		if(compareValue > refCompareValueContentVersion)
			tdClass = "currentWorse";

		out.println("<tr><td>Reading latest contentVersion</td><td>" + contents.size() + "</td><td>" + elapsedTime + "</td><td class='" + tdClass + "' title='(" + numberOfContentVersions + " / " + sampleSize + ") * " + elapsedTime + " = " + compareValue +" (" + refCompareValueContentVersion + ")'>" + thisAvg + "</td><td class='ref'>" + sampleSize + "</td><td class='ref'>" + samleContentVersionTime + "</td><td class='ref'>" + refAvg + "</td></tr>");
	}

	if (contents != null && request.getParameter("test_contentAttributeRead") != null)
	{
		int siteNodeId = 123;
		int languageId = ((LanguageVO)LanguageController.getController().getLanguageVOList().get(0)).getLanguageId();
		int contentId = 123;
		InfoGluePrincipal principal = UserControllerProxy.getController().getUser(CmsPropertyHandler.getAdministratorUserName()); 

		java.util.Iterator<ContentVO> contentsIterator = contents.iterator();
		DatabaseWrapper dbWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
		BasicTemplateController btc = new BasicTemplateController(dbWrapper, principal);
		btc.setStandardRequestParameters(siteNodeId, languageId, contentId);
		btc.setHttpRequest(request);
		btc.setBrowserBean(new BrowserBean());
		btc.setDeliveryControllers(NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId), null, IntegrationDeliveryController.getIntegrationDeliveryController(siteNodeId, languageId, contentId));
		btc.setDeliveryContext(DeliveryContext.getDeliveryContext());
		btc.setComponentLogic(new ComponentLogic(btc, new InfoGlueComponent()));
		while(contentsIterator.hasNext())
		{
			ContentVO contentVO = contentsIterator.next();
			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
			ContentVersionVO contentVersionVO = null;

			if(contentVO != null && masterLanguageVO != null)
			{
// 				contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId());
				String attribute = btc.getContentAttribute(contentVO.getContentId(), masterLanguageVO.getLanguageId(), "Title");
			}
		}

		elapsedTime = timer.getElapsedTime();
		thisAvg =  (float)elapsedTime/(float)contents.size();

		float sampleTotalSizeContentVersions = 344663f;
		float samleContentVersionTime = 1277f;

		refAvg = samleContentVersionTime / sampleSize;
		float refCompareValueContentAttribute = (sampleTotalSizeContentVersions / sampleSize) * samleContentVersionTime;
		compareValue = (numberOfContentVersions / sampleSize) * elapsedTime;

		tdClass = "currentBetter";
		if(thisAvg > refAvg)
			tdClass = "currentWorse";

		out.println("<tr><td>Reading content attributes</td><td>" + contents.size() + "</td><td>" + elapsedTime + "</td><td class='" + tdClass + "' title='(" + numberOfContentVersions + " / " + sampleSize + ") * " + elapsedTime + " = " + compareValue +" (" + refCompareValueContentAttribute + ")'>" + thisAvg + "</td><td class='ref'>" + sampleSize + "</td><td class='ref'>" + samleContentVersionTime + "</td><td class='ref'>" + refAvg + "</td></tr>");
	}

	%>
	</table>

	<p>If you have bad results on these tests there are a couple of things you should do to pinpoint the problem. This is an area where you also can benefit from external help to quickly get results. Contact the community for consultant options.</p>
</c:if>

</body>
</html>