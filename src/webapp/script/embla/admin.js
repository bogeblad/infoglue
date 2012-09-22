try { document.execCommand('BackgroundImageCache', false, true); } catch(e) {}

function openInlineDiv(url, height, width, modal, iframe, title) 
{
	var windowHeight = getWindowHeight();
	//alert("windowHeight:" + windowHeight + ":" + $(window).height());
	if(windowHeight < height)
		height = windowHeight - 40;
	else
		height = height - 40;
		
	var windowWidth = getWindowWidth();
	//alert("windowWidth:" + windowWidth);
	if(windowWidth < width)
		width = windowWidth - 60;
	//alert("height:" + height + " - width:" + width);
		
  	var separatorSign = "?";
	if(url.indexOf("?") > -1)
  		separatorSign = "&";
	
	var addition = separatorSign + "KeepThis=true&" + (iframe ? "TB_iframe=true&" : "") + "height=" + height + "&width=" + width + (modal ? "&modal=true" : "");
	//alert("height:" + height);
	
	tb_show(title, url + addition, title);
}

function closeInlineDiv()
{
	tb_remove();
}

function htmlTreeItemClick(itemId, repoId, path)
{
	//alert("htmlTreeItemClick:" + itemId + repoId);
	$("#workIframe").attr("src", "ViewContent!V3.action?contentId=" + itemId + "&repositoryId=" + repoId);
}

function refreshWorkArea(targetTab)
{
	//alert("targetTab:" +targetTab);
	$("#" + targetTab + "WorkIframe").get(0).contentDocument.location.reload();
}

function refreshWorkSurface(toolName)
{
	//alert("refreshWorkSurface:" + toolName);
	$("#" + toolName + "Iframe").get(0).contentWindow.refreshWorkSurface();
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
	var newUrl = 'ViewToolbarButtons!embla.action?title=' + title + '&toolbarKey=' + toolbarKey + '&' + arguments;
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

var activeToolName = "none";
function getActiveToolName() { return activeToolName; }

function openMySettings()
{
	openInlineDiv("ViewMySettings.action", 700, 800, true, true, "MySettings");
}

function closeContextMenu()
{
	$("#contextMenuDiv").hide();
}

function showContextMenu(ajaxUrl, e, aWindow)
{
	//alert("e:" + e);
	//alert("ajaxUrl:" + ajaxUrl);
		
	if(!e)
		e = window.event;

	var xOffset = (aWindow.parent.pageXOffset !== undefined) ? aWindow.parent.pageXOffset : (aWindow.parent.document.documentElement || aWindow.parent.document.body.parentNode || aWindow.parent.document.body).scrollLeft;
	var yOffset = (aWindow.parent.pageYOffset !== undefined) ? aWindow.parent.pageYOffset : (aWindow.parent.document.documentElement || aWindow.parent.document.body.parentNode || aWindow.parent.document.body).scrollTop;
	
	var clientX = getEventPositionX(e) + 16;
	var clientY = getEventPositionY(e) - yOffset + 80;
	
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


function toggleAvailableToolsSize()
{
	if($("#availableTools li").width() == 26)
	{
		$("#availableTools li").css("width", "100%").removeClass("minimized");
		$("#availableTools").removeClass("minimized");
		$("#availableToolsSizeControlBar").removeClass("minimized");
		$("#availableToolsSizeControlBar img").attr("src","images/v3/smallBarDownArrow.png");
	}
	else
	{
		$("#availableTools li").css("width", "26px").css("overflow", "hidden").addClass("minimized");
		$("#availableTools").addClass("minimized");
		$("#availableToolsSizeControlBar").addClass("minimized");
		$("#availableToolsSizeControlBar img").attr("src","images/v3/smallBarUpArrow.png");
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
	document.getElementById(targetFrame).contentWindow.syncWithTree(path, repositoryId);
}
