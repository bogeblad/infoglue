try { document.execCommand('BackgroundImageCache', false, true); } catch(e) {}

var isDragActive = false;
var isLocalDragActive = false;
var isDragCompleted = false;
var dragHTML = "";

function getIsIGStandardTools()
{
	return true;
}

function notifyDragHTML(html)
{
	dragHTML = html;
	isDragCompleted = false;
	isDragActive = true;
	//alert("dragHTML:" + dragHTML);
	$("#debug").text("html: " + html);
}

function getDragHTML()
{
	return dragHTML;
}

function emptyDragHTML()
{
	dragHTML = "";
}

function disableDrag()
{
	isDragActive = false;
	isLocalDragActive = false;
	$("#tempDraggable").remove();
}
function enableDrag()
{
	isDragActive = true;
}

function dragCompleted()
{
	//alert("Drag completed");
	isDragCompleted = true;
	$("#tempDraggable").remove();
}

function getIsDragCompleted()
{
	return isDragCompleted;
}

function getIsDragActive()
{
	return isDragActive;
}

function openInlineDiv(url, height, width, modal, iframe, title) 
{
	var windowHeight = getWindowHeight();
	//alert("windowHeight:" + windowHeight);
	if(windowHeight < height)
		height = windowHeight - 60;

	var windowWidth = getWindowWidth();
	//alert("windowWidth:" + windowWidth);
	if(windowWidth < width)
		width = windowWidth - 60;
	//alert("height:" + height + " - width:" + width);
		
  	var separatorSign = "?";
	if(url.indexOf("?") > -1)
  		separatorSign = "&";
	
	var addition = separatorSign + "KeepThis=true&" + (iframe ? "TB_iframe=true&" : "") + "height=" + height + "&width=" + width + (modal ? "&modal=true" : "");
	
	tb_show(title, url + addition, title);
}

function closeInlineDiv()
{
	tb_remove();
}

function search(repositoryId, tabLabelPrefix)
{
	var url = "Search.action?repositoryId=" + repositoryId + "&searchString=" + $("#searchField").val();
	openUrlInWorkArea(url, 'Search', 'search', tabLabelPrefix);
	return false;
}

function htmlTreeItemClick(itemId, repoId, path)
{
	//alert("htmlTreeItemClick:" + itemId + repoId);
	$("#workIframe").attr("src", "ViewContent!V3.action?contentId=" + itemId + "&repositoryId=" + repoId);
}

function openUrlInWorkArea(url, tabLabel, targetTab, tabLabelPrefix, toolName)
{
	//alert("url:" + url + " - " + tabLabel + " - " + targetTab);
	//$("#workIframe").attr("src", url);
	//if(tabLabel != null && tabLabel != '')
	//	$("#singleTabLabel span").text(tabLabel);

	if(tabLabelPrefix == "")
	{
		if(targetTab == "structure")
			tabLabelPrefix = "Page - ";
		else if(targetTab == "content")
			tabLabelPrefix = "Content - ";
		else if(targetTab == "management")
			tabLabelPrefix = "Management - ";
		else if(targetTab == "publishing")
			tabLabelPrefix = "Publishing - ";
		else if(targetTab == "search")
			tabLabelPrefix = "Search - ";
	}
	
	tabLabelPrefix = tabLabelPrefix + " - ";
	
	//alert("targetTab:" + targetTab + ":" + $("#" + targetTab + "TabLabel").size() + " - " + tabLabel);
	if(targetTab && $("#" + targetTab + "TabLabel").size() == 0)
	{
		$("#tabsContainer").tabs("add", "#" + targetTab + "TabDiv", "Loading...");
		//$("#tabsContainer a[href='#" + targetTab + "TabDiv']").attr("id", targetTab + "TabLabel");
		$("#newTabLabel").attr("id", targetTab + "TabLabel");
		$("#newTabLabelMaximize").attr("id", targetTab + "TabLabelMaximize");
		$("#newTabLabelClose").attr("id", targetTab + "TabLabelClose");
		$("#" + targetTab + "TabLabelClose").click(function () { 
                                      					$("#tabsContainer ul li").each(function (i) {
                                                            var tabId = $(this).children("a").attr("id");
															//alert("i:" + i + " - " + tabId);
															if(tabId == targetTab + "TabLabel")
															{
																$("#tabsContainer").tabs('remove', i);
															}
                                                        });
                                    				});

		$("#" + targetTab + "TabLabelMaximize").click(function () { 
			if($("#work").css("position") == "absolute")
			{
				$("#work").css("position", "inherit").css("top", "").css("left", "").css("margin", "4px 4px 4px 0px").css("border-width", "1px").css("zIndex","");
			}
			else
			{
				$("#work").css("position", "absolute").css("top", "0px").css("left", "0px").css("margin", "0px 0px 0px 0px").css("border-width", "0px").css("zIndex","2000");
			}
		});

		//alert("Size:" + $("#newTabDiv").size());
		//alert("Size:" + $(".newTabDiv").size());
		$(".newTabDiv").html("<iframe id='" + targetTab + "WorkIframe" + "' name='" + targetTab + "WorkIframe" + "' src='' width='100%' height='500' frameborder='0'></iframe>");
		
		$(".newTabDiv").attr("id", targetTab + "TabDiv");
		$(".newTabDiv").removeClass("newTabDiv");

		//$("#newWorkIframe").attr("id", targetTab + "WorkIframe");
		//$("#newTabDiv").attr("name", targetTab + "TabDiv").attr("id", targetTab + "TabDiv");
		//$("#newWorkIframe").attr("name", targetTab + "WorkIframe").attr("id", targetTab + "WorkIframe");
		//$("#" + targetTab + "TabDiv").attr("id", targetTab + "TabLabel");
		
		if(targetTab == "structure")
			var justCreated = true;
	}

	$("#" + targetTab + "WorkIframe").attr("src", url);
			
	if(targetTab == "structure" && justCreated)
	{
		$("#structureWorkIframe").load(function() {

			//alert("Just loaded:" + $("#structureWorkIframe").get(0).contentDocument.location.href);
			try
			{
				if(parent.refreshTopToolBar)
				{
					var url = $("#structureWorkIframe").get(0).contentDocument.location.href;
					//alert("url:" + url);
					if(url.indexOf("siteNodeId=") > -1)
					{
						var loadedSiteNodeId = getRequestParameter(url, "siteNodeId");
						//alert("loadedSiteNodeId:" + loadedSiteNodeId);
						parent.refreshTopToolBar('tool.structuretool.siteNodeComponentsHeader', 'tool.structuretool.siteNodeComponentsHeader', 'siteNodeId=' + loadedSiteNodeId, -1, -1, -1);
					}
				}
			}
			catch(e)
			{
				alert("Error:" + e);
			}
		});
	}

	var tabSize = $("#tabsContainer li").size();
	//alert("tabSize:" + tabSize)
	var i=0;
	for (i=0;i<=tabSize;i++)
	{
		var id = $("#tabsContainer li:eq(" + i + ") a").attr("id");
		if(id)
		{
			//alert("id:" + id)
			if(id.indexOf(targetTab) > -1)
				$("#tabsContainer").tabs("select", i);
		}
	}
	//alert("targetTab:" + targetTab);
	if(tabLabel != null && tabLabel != '')
		$("#" + targetTab + "TabLabel span").text(tabLabelPrefix + tabLabel);
}

function refreshWorkArea(targetTab)
{
	//$("#" + targetTab + "WorkIframe").get(0).contentDocument.location.reload();
	$("#" + targetTab + "WorkIframe").get(0).contentWindow.refreshWorkSurface();
}

var currentMenutoolbarLeftUrl = "";
function getCurrentMenutoolbarLeftUrl() { return currentMenutoolbarLeftUrl; }

var currentUrls = new Array()
currentUrls["content"] 		= "";
currentUrls["structure"] 	= "";
currentUrls["management"] 	= "";
currentUrls["publishing"] 	= "";
currentUrls["mydesktop"] 	= "";
currentUrls["formeditor"] 	= "";

function refreshTopToolBar(title, toolbarKey, arguments, unrefreshedContentId, changeTypeId, newContentId)
{
	var newUrl = 'ViewToolbarButtons.action?title=' + title + '&toolbarKey=' + toolbarKey + '&' + arguments;
	//alert("newUrl:" + newUrl);
	if(toolbarKey.indexOf("tool.contenttool") > -1)
		currentUrls["content"] = newUrl;
	else if(toolbarKey.indexOf("tool.structuretool") > -1)
		currentUrls["structure"] = newUrl;
	else if(toolbarKey.indexOf("tool.managementtool") > -1)
		currentUrls["management"] = newUrl;
	else if(toolbarKey.indexOf("tool.publishingtool") > -1)
		currentUrls["publishing"] = newUrl;
	else if(toolbarKey.indexOf("tool.mydesktoptool") > -1)
		currentUrls["mydesktop"] = newUrl;
	else if(toolbarKey.indexOf("tool.formeditortool") > -1)
		currentUrls["formeditor"] = newUrl;
	
	currentMenutoolbarLeftUrl = newUrl;
	
	$("#menutoolbarLeft").empty();
	
	jQuery.get(newUrl, function(data){
      	//alert("Data Loaded: " + data);
		$("#menutoolbarLeft").replaceWith(data);
    });
    
	/*
	if(unrefreshedContentId > 0)
	{
		alert("About to call refresh on menu:" + unrefreshedContentId + ":" + changeTypeId + ":" + newContentId);
		if(parent.frames["menu"])
			parent.frames["menu"].refreshContent(unrefreshedContentId, changeTypeId, newContentId);
	}
	*/
}

function resetTopToolBar(toolName)
{
	var newUrl = currentUrls[toolName];
	
	currentMenutoolbarLeftUrl = newUrl;
	
	$("#menutoolbarLeft").empty();

	if(newUrl != "")
	{
		jQuery.get(newUrl, function(data){
	      	//alert("Data Loaded: " + data);
			$("#menutoolbarLeft").replaceWith(data);
	    });
	}
}

function resize()
{
	//alert("Resize");
	var windowHeight = getWindowHeight();
	var windowWidth = getWindowWidth();
	
	var paletteDivHeight = $("#paletteDiv").height();
	if(paletteDivHeight == 0)
		paletteDivHeight = 150;
	
	//alert("paletteDivHeight:" + paletteDivHeight);
	
	$("#tools").height(windowHeight - 88);
	var toolsWidth = $("#tools").width();
	//alert("toolsWidth:" + toolsWidth);
	if($("#work").css("position") != "absolute")
	{
		$("#work").height(windowHeight - 88);
		$("#work").width(windowWidth - (toolsWidth + 16));

		$("#singleTabDiv").height(windowHeight - 115);
		$("#contentTabDiv").height(windowHeight - 115);
		$("#structureTabDiv").height(windowHeight - 115);
		$("#managementTabDiv").height(windowHeight - 115);
		$("#publishingTabDiv").height(windowHeight - 115);
		$("#mydesktopTabDiv").height(windowHeight - 115);
		$("#workIframe").attr("height", windowHeight - 115);
		$("#contentWorkIframe").attr("height", windowHeight - 115);
		$("#structureWorkIframe").attr("height", windowHeight - 115);
		$("#managementWorkIframe").attr("height", windowHeight - 115);
		$("#publishingWorkIframe").attr("height", windowHeight - 115);
		$("#mydesktopWorkIframe").attr("height", windowHeight - 115);
		$("#searchWorkIframe").attr("height", windowHeight - 50);
	}
	else
	{
		$("#work").height(windowHeight);
		$("#work").width(windowWidth);
		$("#tabsContainer").height(windowHeight - 28);
		$("#tabsContainer > div").height(windowHeight - 28);

		$("#singleTabDiv").height(windowHeight - 28);
		$("#contentTabDiv").height(windowHeight - 28);
		$("#structureTabDiv").height(windowHeight - 28);
		$("#managementTabDiv").height(windowHeight - 28);
		$("#publishingTabDiv").height(windowHeight - 28);
		$("#mydesktopTabDiv").height(windowHeight - 28);
		$("#workIframe").attr("height", windowHeight - 28);
		$("#contentWorkIframe").attr("height", windowHeight - 28);
		$("#structureWorkIframe").attr("height", windowHeight - 28);
		$("#managementWorkIframe").attr("height", windowHeight - 28);
		$("#publishingWorkIframe").attr("height", windowHeight - 28);
		$("#mydesktopWorkIframe").attr("height", windowHeight - 28);
		$("#searchWorkIframe").attr("height", windowHeight - 28);
	}
	
	var availableToolsHeight = $("#availableTools").height();
	var activeToolHeaderHeight = $("#activeToolHeader").height();
	//alert("availableToolsHeight:" + availableToolsHeight);
	//alert("activeToolHeaderHeight:" + activeToolHeaderHeight);
	
	$("#structureTreeIframe").attr("height", windowHeight - (activeToolHeaderHeight + availableToolsHeight + 118 + paletteDivHeight));
	$("#contentTreeIframe").height(windowHeight - (activeToolHeaderHeight + availableToolsHeight + $("#contentRepositoryChoiceDiv").height() + 92));
	$("#managementTreeIframe").height(windowHeight - (activeToolHeaderHeight + availableToolsHeight + 90));
	$("#publishingTreeIframe").height(windowHeight - (activeToolHeaderHeight + availableToolsHeight + 90));
	$("#mydesktopTreeIframe").height(windowHeight - (activeToolHeaderHeight + availableToolsHeight + 90));
	
	if(activeToolName == "StructureTool")
		$("#activeTool").height(windowHeight - (activeToolHeaderHeight + availableToolsHeight + 102));
	else
		$("#activeTool").height(windowHeight - (activeToolHeaderHeight + availableToolsHeight + 90));
					
	$("#debug").text("Height: " + $("#managementTreeIframe").height() + ", availableToolsHeight: " + availableToolsHeight + ", activeToolHeaderHeight=" + activeToolHeaderHeight);
	//$("#debug").text("Height: " + $("#contentTreeIframe").attr("height") + ", ContentTool:" + (windowHeight - (activeToolHeaderHeight + availableToolsHeight + 124)) + "minus:" + (activeToolHeaderHeight + availableToolsHeight + 90) + ", availableToolsHeight: " + availableToolsHeight + ", activeToolHeaderHeight=" + activeToolHeaderHeight);
		
	setTimeout("resize();", 1000);
}

var activeToolName = "none";
function getActiveToolName() { return activeToolName; }

function activateTool(toolMarkupDivId, toolName, suffix, checkWorkArea, tabLabelPrefix)
{
	//alert("" + toolMarkupDivName + "=" + activeToolName);
	if(activeToolName == toolName)
		return false;
	
	$("#" + activeToolName).hide();
	$("#" + toolMarkupDivId).show();
	$("#" + toolMarkupDivId + "Link").addClass("active");
	$("#" + activeToolName + "Link").removeClass("active");

	activeToolName = toolName;
	
	resize();

	document.title = "" + toolName + " - " + suffix;
	$("#activeToolHeader h3").html(toolName);
	
	var toolName = toolMarkupDivId.replace("Markup", "");
	if(checkWorkArea)
	{
		var tabSize = $("#tabsContainer li").size();
		var i=0;
		var exists = false;
		for (i=0;i<=tabSize;i++)
		{
			var id = $("#tabsContainer li:eq(" + i + ") a").attr("id");
			if(id)
			{
				//alert("id:" + id)
				if(id.indexOf(toolName) > -1)
					exists = true;
			}
		}
		
		//alert("exists:" + exists)
		if(!exists)
		{
			if(toolName == "content")
				openUrlInWorkArea("ViewContentToolStartPage!V3.action", toolName, "content", tabLabelPrefix);
			if(toolName == "structure")
				openUrlInWorkArea("ViewStructureToolStartPage!V3.action", toolName, "structure", tabLabelPrefix);
			if(toolName == "management")
				openUrlInWorkArea("ViewManagementToolStartPage!V3.action", toolName, "management", tabLabelPrefix);
			if(toolName == "publishing")
				openUrlInWorkArea("ViewPublishingToolStartPage!V3.action", toolName, "publishing", tabLabelPrefix);
			if(toolName == "mydesktop")
				openUrlInWorkArea("ViewMyDesktop.action", toolName, "mydesktop", tabLabelPrefix);	
			if(toolName == "formeditor")
				openUrlInWorkArea("/infoglueDeliverWorking/formeditor", toolName, "formeditor", tabLabelPrefix);	
		}
		else //This activates the tab also
		{
			for (i=0;i<=tabSize;i++)
			{
				var id = $("#tabsContainer li:eq(" + i + ") a").attr("id");
				if(id)
				{
					//alert("id:" + id)
					if(id.indexOf(toolName) > -1)
						$("#tabsContainer").tabs( 'select' , i );
				}
			}
		}
	}
	
	resetTopToolBar(toolName);
	
	return false;
}

function openMySettings()
{
	openInlineDiv("ViewMySettings.action", 700, 800, true, true, "MySettings");
}

function closeContextMenu()
{
	$("#contextMenuDiv").hide();
}

function showContextMenu(ajaxUrl, e)
{
	//alert("e:" + e);
	//alert("ajaxUrl:" + ajaxUrl);
	//alert("Offset:" + document.getElementById("activeTool").offsetTop);
		
	if(!e)
		e = window.event;
 
	var clientX = getEventPositionX(e) + 16;
	var clientY = getEventPositionY(e) + 80;
		
	var rightedge = document.body.clientWidth - clientX;
	var bottomedge = getWindowHeight() - clientY;

	var menuDiv = document.getElementById("contextMenuDiv");
	
	if (rightedge < menuDiv.offsetWidth)
		clientX = (clientX - menuDiv.offsetWidth);
	
	if (bottomedge < menuDiv.offsetHeight && (clientY - menuDiv.offsetHeight > 0))
		clientY = (clientY - menuDiv.offsetHeight);
		
	menuDiv.style.left 	= clientX + "px";
	menuDiv.style.top 	= clientY + "px";
	
	jQuery.get(ajaxUrl,
	  	function(data){
			$("#contextMenuDiv").html(data);
    	});
	$("#contextMenuDiv").show();

	return false;
}

function changeRepository(repositoryId, repositoryName, treeDiv, baseAddress, closeDivId)
{
	$("#" + closeDivId + " a").removeClass("current");
	$("#" + closeDivId + " a:contains('" + repositoryName + "')").addClass("current");
	$("#" + closeDivId + "Handle").text(repositoryName);
	$("#" + treeDiv).attr("src", baseAddress + repositoryId);
	$("#" + closeDivId).hide();
	
	return false;
}

function toggleFavourites()
{
	if($("#paletteDiv").height() == 20)
	{
		$("#paletteDiv").height(150);
		$("#paletteIframe").height(150);
		$("#componentPaletteHeader img").attr("src", "css/images/v3/downArrows.png");
	}
	else
	{
		$("#paletteDiv").height(20);
		$("#paletteIframe").height(0);
		$("#componentPaletteHeader img").attr("src", "css/images/v3/upArrows.png");
	}
	resize();
}

function toggleAvailableToolsSize()
{
	if($("#availableTools li").width() == 26)
	{
		$("#availableTools li").css("width", "100%").removeClass("minimized");
		$("#availableTools").removeClass("minimized");
		$("#availableToolsSizeControlBar").removeClass("minimized");
		$("#availableToolsSizeControlBar img").attr("src","css/images/v3/smallBarDownArrow.png");
	}
	else
	{
		$("#availableTools li").css("width", "26px").css("overflow", "hidden").addClass("minimized");
		$("#availableTools").addClass("minimized");
		$("#availableToolsSizeControlBar").addClass("minimized");
		$("#availableToolsSizeControlBar img").attr("src","css/images/v3/smallBarUpArrow.png");
	}
}


var getUrl = "ViewMessageCenter!getSystemMessages.action";
var lastId = -1; //initial value will be replaced by the latest known id

function initMessageSystem() 
{
	receiveSystemMessagesText(true); //initiates the first data query
}

var messageUserName = "";

//initiates the first data query
function receiveSystemMessagesText(loopAfter) 
{
	$.getJSON("ViewMessageCenter!getSystemMessagesV3.action?lastId=" + lastId + "&rand=" + Math.floor(Math.random() * 1000000), function(jsonData) {

			//alert("jsonData:" + jsonData.title + ":" + jsonData.messages);
			if(jsonData.messages)
			{
				$.each(jsonData.messages, function(i,item){
	
					//alert("jsonData:" + item.id + ":" + item.type + ":" + item.text);
					
					if(item.text != "empty")
					{
						i = 0;
						lastId = item.id;
				    	extradata = item.text;
				    	type = item.type;
				    	messageUserName = item.userName;
				    	
				    	//alert("lastId:" + lastId);
				    	//alert("extradata:" + extradata);
				    	//alert("type:" + type);
				    	if(type != "-1")
				    		setTimeout(extradata, "200");
					}	
				});
			}
			
			if(loopAfter)
				setTimeout('receiveSystemMessagesText(true);',15000); //executes the next data query in 30 seconds
		});
}

function openChat(message)
{
	var d = new Date();
	var curr_date = d.getDate();
	var curr_month = d.getMonth();
	curr_month++;
	var curr_year = d.getFullYear();
	var curr_hour = d.getHours();
	if(curr_hour < 10)
		curr_hour = "0" + curr_hour;
	
	var curr_min = d.getMinutes();
	if(curr_min < 10)
		curr_min = "0" + curr_min;

	var curr_sec = d.getSeconds();
	if(curr_sec < 10)
		curr_sec = "0" + curr_sec;

	//var nowDateTime = "" + curr_year + "-" + curr_month + "-" + curr_date + " " + curr_hour + ":" + curr_min;
	var nowDateTime = "" + curr_hour + ":" + curr_min + ":" + curr_sec;
	$("#messages").prepend("<p><span style='font-weight: bold'>" + messageUserName + "(" + nowDateTime + ")" + "" + ":</span> " + message + "</p>")
	$("#messagesDiv").dialog({ title: 'System messages', modal: true, maxHeight: 500, maxWeight: 600, width: 700, minHeight: 200 }).dialog( 'open' );
}

function sendMessage()
{
	var chatMessage = $("#chatMessage").val();
	$.post("ViewMessageCenter!sendMessage.action", { isSystemMessage: "true", message: chatMessage }, function(data){ 
			receiveSystemMessagesText(false);
			$("#chatMessage").val("");
	});
}

function syncWithTree(path, repositoryId, targetFrame)
{
	frames[targetFrame].syncWithTree(path, repositoryId);
}
