if (!window.console) console = {log: function() {}};

var isDragged = false;

var ns = (navigator.appName.indexOf("Netscape") != -1);
var d = document;
var px = document.layers ? "" : "px";
var elementArray = new Array();
	
function floatDiv(id, sx, sy)
{
	var el=d.getElementById?d.getElementById(id):d.all?d.all[id]:d.layers[id];
	//alert("id:"+id);
	window[id + "_obj"] = el;
	elementArray["" + id + "_obj"] = el;
	//alert("elementArray:" + elementArray.length);
	//alert("elementArray:" + elementArray.toString());
	if(d.layers)el.style=el;
	el.cx = el.sx = sx;el.cy = el.sy = sy;
	el.sP=function(x,y){this.style.left=x+px;this.style.top=y+px;};
	el.flt=function()
	{
		if(isDragged)
		{
			//alert("isDragged:" + isDragged);
			//el.cy = el.sy = 50;
		}
		else
		{
			var pX, pY;
			pX = (this.sx >= 0) ? 0 : ns ? innerWidth : 
			document.documentElement && document.documentElement.clientWidth ? 
			document.documentElement.clientWidth : document.body.clientWidth;
			pY = ns ? pageYOffset : document.documentElement && document.documentElement.scrollTop ? 
			document.documentElement.scrollTop : document.body.scrollTop;
			if(this.sy<0) 
			pY += ns ? innerHeight : document.documentElement && document.documentElement.clientHeight ? 
			document.documentElement.clientHeight : document.body.clientHeight;
			this.cx += (pX + this.sx - this.cx)/2;this.cy += (pY + this.sy - this.cy)/2;
			this.sP(this.cx, this.cy);
			setTimeout(this.id + "_obj.flt()", 50);
		}
	}
	return el;
}

/****************************
 * Called when rezising popup
 ****************************/
 
var toolbarLockPositionCookieName = "toolbarLockPosition";
var pageStructureDivVisibleCookieName = "pageStructureDivVisible";
var pageStructureDivWidthCookieName = "pageStructureDivWidth";
var pageStructureDivHeightCookieName = "pageStructureDivHeight";
var pageStructureDivHeightBodyCookieName = "pageStructureDivHeightBody";
var pageComponentsTopPositionCookieName = "pageStructureTopPosition";
var pageComponentsLeftPositionCookieName = "pageStructureLeftPosition";

var pageStructureDivWidth = "300px";
var pageStructureDivHeight = "380px";
var pageStructureDivHeightBody = "360px";

/**
 * This method sets a cookie in the browser.
 */
 
function setCookie(name, value)
{
	var length = document.cookie.split(';').length;
	var index;
	if(length < 18)
    {
		if(document.cookie != document.cookie)
			index = document.cookie.indexOf(name);
		else
			index = -1;
		
		if (index == -1)
			document.cookie=name+"="+value+"; expires=Monday, 04-Apr-2010 05:00:00 GMT";
	}
}

/**
 * This method gets a cookie
 */
 
function getCookieValue(name)
{ 
	var value = "";
	var index;
	var namestart = "";
	var nameend = "";
 	if(document.cookie)
	{
		index = document.cookie.indexOf(name);
		//alert("index:" + index);
		if (index != -1)
		{
			namestart = (document.cookie.indexOf("=", index) + 1);
			nameend = document.cookie.indexOf(";", index);
			if (nameend == -1) {nameend = document.cookie.length;}
			value = document.cookie.substring(namestart, nameend);
			//alert("defaultMenuSize" + defaultMenuSize);
		}
	}
	
	return value;
} 

function expandWindow()
{
	width = document.getElementById('pageComponents').style.width;
	if(width.indexOf("400") > -1)
	{
		width = "450";
		height = "500";	
	}
	else if(width.indexOf("450") > -1)
	{
		width = "500";
		height = "550";	
	}
	else if(width.indexOf("500") > -1)
	{
		width = "550";
		height = "600";	
	}
	else
	{
		width = "400";
		height = "450";	
	}
	
	width = width+"px";
	heightBody = height-20+"px";
	height = height+"px";
	
	document.getElementById('pageComponents').style.width=width;
	document.getElementById('pageComponents').style.height=height;
	document.getElementById('pageComponentsBody').style.height=heightBody;
	
	setCookie(pageStructureDivWidthCookieName, width);
	setCookie(pageStructureDivHeightCookieName, height);
	setCookie(pageStructureDivHeightBodyCookieName, heightBody);
	
} 
 
 
/****************************
 * Hook method to get informed when a drag starts
 ****************************/

function dragStarted(object)
{
	//alert("dragStarted:" + object.id);
	isDragged = true;
} 

/****************************
 * Hook method to get informed when a drag ends
 ****************************/

var toolbarTopPositionCookieName = "toolbarTopPosition";
var defaultToolbarTopPosition = "0px";
 
function dragEnded(object, left, top)
{
	
	//alert("dragEnded:" + object.id);
	if(object.id == "paletteHandle")
	{
		//el.cy = el.sy = 50;
		topPosition = top;
		setCookie(toolbarLockPositionCookieName, topPosition);
		setCookie(toolbarTopPositionCookieName, topPosition);
	}

	if(object.id == "pageComponentsHandle")
	{
		setCookie(pageComponentsTopPositionCookieName, top);
		setCookie(pageComponentsLeftPositionCookieName, left);
	}
}

var defaultToolbarTopPosition;
var toolbarLockPosition;
var pageComponentsVisibility;	
var pageStructureDivWidth;
var pageStructureDivHeight;
var pageStructureDivHeightBody;

function setToolbarInitialPosition()
{	
	//alert("setToolbarInitialPosition ran");
	defaultToolbarTopPosition = getCookieValue(toolbarTopPositionCookieName);
	toolbarLockPosition = getCookieValue(toolbarLockPositionCookieName);
	pageComponentsVisibility = getCookieValue(pageStructureDivVisibleCookieName);	
	pageStructureDivWidth = getCookieValue(pageStructureDivWidthCookieName);
	pageStructureDivHeight = getCookieValue(pageStructureDivHeightCookieName);
	pageStructureDivHeightBody = getCookieValue(pageStructureDivHeightBodyCookieName);

	var propertiesDiv = document.getElementById("pageComponents");
		
	if(propertiesDiv)
	{
		//alert("window.innerHeight:" + document.height + ":" + window.innerHeight);
		pageComponentsTopPosition = (getScrollY() + ((document.body.clientHeight - propertiesDiv.offsetHeight) / 2));
		pageComponentsLeftPosition = (getScrollX() + ((document.body.clientWidth - propertiesDiv.offsetWidth) / 2));
	
		floatDiv("pageComponents", 200, 50).flt();
	}	
	
	//alert("document:" + document.getElementById("paletteDiv").id);
	var paletteDivElement = document.getElementById("paletteDiv");
	if(paletteDivElement)
	{	
		//alert("defaultToolbarTopPosition" + defaultToolbarTopPosition)
		//alert("toolbarLockPosition" + toolbarLockPosition)
		if(toolbarLockPosition == "up")
			floatDiv("paletteDiv", 0, 0).flt();
		else if(toolbarLockPosition == "down")
			floatDiv("paletteDiv", 0, -80).flt();
		else
			this.document.getElementById('paletteDiv').style.top=defaultToolbarTopPosition;
	}
			
	//alert("getScrollY()" + getScrollY() + ":" + propertiesDiv.offsetHeight + ":" + (document.body.clientHeight));
	//alert("pageComponentsTopPosition" + pageComponentsTopPosition)
	//alert("pageComponentsLeftPosition" + pageComponentsLeftPosition)
	//document.getElementById('pageComponents').style.top=pageComponentsTopPosition + "px";
	//document.getElementById('pageComponents').style.left=pageComponentsLeftPosition + "px";
	var pageComponentsDiv = document.getElementById('pageComponents');
	if(pageComponentsDiv)
	{
		pageComponentsDiv.style.width=pageStructureDivWidth;
		pageComponentsDiv.style.height=pageStructureDivHeight;
	}
	
	var pageComponentsBodyDiv = document.getElementById('pageComponentsBody');
	if(pageComponentsBodyDiv)
		document.getElementById('pageComponentsBody').style.height=pageStructureDivHeightBody;
	
	//alert("pageComponentsVisibility:" + pageComponentsVisibility);
	if(pageComponentsVisibility != "" && propertiesDiv)
	{
		if(pageComponentsVisibility == "visible")
			propertiesDiv.style.display = 'block';

		propertiesDiv.style.visibility = pageComponentsVisibility;
	}
	
}


var activeMenuId = "";
var menuskin = "skin1"; // skin0, or skin1
var display_url = 0; // Show URLs in status bar?
var editUrl = "";
var assignComponentCatched = false;

if (navigator.appName == "Netscape") {
  document.captureEvents(Event.CLICK);
}

// returns the scroll left and top for the browser viewport.
function getScrollX() {
   if (document.body.scrollTop != undefined) {	// IE model
      var ieBox = document.compatMode != "CSS1Compat";
      var cont = ieBox ? document.body : document.documentElement;
      return cont.scrollLeft;
   }
   else {
      return window.pageXOffset;
   }
}

// returns the scroll left and top for the browser viewport.
function getScrollY() {
   if (document.body.scrollTop != undefined) {	// IE model
      var ieBox = document.compatMode != "CSS1Compat";
      var cont = ieBox ? document.body : document.documentElement;
      return cont.scrollTop;
   }
   else {
      return window.pageYOffset;
   }
}

function getEventPositionX(e) 
{
	var mX = 0;
	
	if (navigator.appName == "Microsoft Internet Explorer")
	{
    	mX = event.clientX + getScrollX();
  	}
  	else 
  	{
    	if(e)
	    	mX = e.pageX;
  	}
  	
  	return mX;
}

function getEventPositionY(e) 
{
	var mY = 0;

	if (navigator.appName == "Microsoft Internet Explorer")
	{
    	mY = event.clientY + getScrollY();
  	}
  	else 
  	{
		if(e)
	    	mY = e.pageY;
  	}
  	
  	return mY;
}

function getWindowHeight()
{
	var y;
	if (self.innerHeight) // all except Explorer
	{
		y = self.innerHeight;
	}
	else if (document.documentElement && document.documentElement.clientHeight)
		// Explorer 6 Strict Mode
	{
		y = document.documentElement.clientHeight;
	}
	else if (document.body) // other Explorers
	{
		y = document.body.clientHeight;
	}
	return y;
}

function getElementHeight(element)
{
	var y;
	if (element.innerHeight) // all except Explorer
	{
		y = element.innerHeight;
	}
	else if (document.body) // other Explorers
	{
		y = element.clientHeight;
	}
	return y;
}
function getElementWidth(element)
{
	var x;
	if (element.innerWidth) // all except Explorer
	{
		x = element.innerWidth;
	}
	else if (document.body) // other Explorers
	{
		x = element.clientWidth;
	}
	return x;
}

function getWindowWidth()
{
	var x;
	if (self.innerHeight) // all except Explorer
	{
		x = self.innerWidth;
	}
	else if (document.documentElement && document.documentElement.clientHeight)
		// Explorer 6 Strict Mode
	{
		x = document.documentElement.clientWidth;
	}
	else if (document.body) // other Explorers
	{
		x = document.body.clientWidth;
	}
	return x;
}

function getActiveMenuDiv() 
{
	//alert("activeMenuId:" + activeMenuId);
	return document.getElementById(activeMenuId);
}

var busy = false;
var componentId;
var slotId;
var slotName  = "";
var slotContentId = "";
var editUrl   = "";
var insertUrl = "";
var deleteUrl = "";
var changeUrl = "";

function setEditUrl(anEditUrl) 
{
	//alert("Setting editUrl:" + anEditUrl);
	editUrl = anEditUrl;
}

function setContentItemParameters(repositoryId, contentId, languageId, attributeName)
{
	//alert("Setting contentId:" + contentId);
	//alert("Setting languageId:" + languageId);
	//alert("Setting attributeName:" + attributeName);
	//alert("Setting repositoryId:" + repositoryId);
	selectedRepositoryId = repositoryId;
	selectedContentId = contentId;
	selectedLanguageId = languageId;
	selectedAttributeName = attributeName;
}

function showComponentMenu(event, element, compId, anInsertUrl, anDeleteUrl, anChangeUrl) 
{
	hidepreviousmenues();
	
	activeMenuId = "component" + compId + "Menu";

	componentId = compId;
	insertUrl = anInsertUrl;
	deleteUrl = anDeleteUrl;
	changeUrl = anChangeUrl;
	//alert("selectedRepositoryId" + selectedRepositoryId);
	//alert("componentId" + componentId);
	//alert("activeMenuId" + activeMenuId);
	//alert("editUrl" + editUrl);
	//alert("changeUrl:" + changeUrl);
	
    document.body.onclick = hidepreviousmenues;
	getActiveMenuDiv().className = menuskin;
	
	clientX = getEventPositionX(event);
	clientY = getEventPositionY(event);
	
	var rightedge = document.body.clientWidth - clientX;
	//var bottomedge = document.body.clientHeight - clientY;
	var bottomedge = getWindowHeight() - clientY;
	
	menuDiv = getActiveMenuDiv();

	//var offsetYInWindow = clientY - getScrollY();
	//alert("getScrollY():" + getScrollY());
	//alert("e.pageY:" + e.pageY);
	//alert("getWindowHeight;" + getWindowHeight());
	//alert("OffsetInWindow:" + offsetYInWindow);
	//alert("clientY:" + clientY);
	//alert("document.body.scrollTop:" + document.body.scrollTop);
	//alert("bottomedge:" + bottomedge);
	//alert("document.body.clientHeight:" + document.body.clientHeight);
	//alert("menuDiv.offsetWidth:" + menuDiv.offsetWidth);

	var editDivElement = document.getElementById("editDiv" + compId);
	var editInlineDivElement = document.getElementById("editInlineDiv" + compId);
	var subscribeContentDivElement = document.getElementById("subscribeContent" + compId);
	if(!editUrl || editUrl == "")
	{
		if(editDivElement)
			editDivElement.style.display = "none";
		if(editInlineDivElement)
			editInlineDivElement.style.display = "none";
		if(subscribeContentDivElement)
			subscribeContentDivElement.style.display = "none";
	}
	else
	{
		if(editDivElement)
			editDivElement.style.display = "block";
		if(editInlineDivElement)
			editInlineDivElement.style.display = "block";
		if(subscribeContentDivElement)
			subscribeContentDivElement.style.display = "block";
		//alert("Registering click to:" + editUrl);
		var anEditUrl = editUrl;
		$(editDivElement).click(function () { edit(anEditUrl); });
		$(editInlineDivElement).click(function () { editInlineSimple(selectedRepositoryId); });
		
		var subscriptionUrl = componentEditorUrl + "Subscriptions!input.action?interceptionPointCategory=Content&entityName=org.infoglue.cms.entities.content.Content&entityId=" + selectedContentId + "&extraParameters=" + selectedContentId + "&returnAddress=" + componentEditorUrl + "ViewInlineOperationMessages.action";
		$(subscribeContentDivElement).click(function () { openInlineDiv(subscriptionUrl, 700, 750, true); });
	}
		
	var componentEditorInNewWindowDivCompElement = document.getElementById("componentEditorInNewWindowDiv" + compId);
	if(window.parent.name == "PageComponents" && componentEditorInNewWindowDivCompElement)
		componentEditorInNewWindowDivCompElement.style.display = "none";
	
	if (rightedge < menuDiv.offsetWidth)
		clientX = (clientX - menuDiv.offsetWidth + 5);
	
	if (bottomedge < menuDiv.offsetHeight)
		clientY = (clientY - menuDiv.offsetHeight + 5);
		
	menuDiv.style.left 	= clientX + "px";
	menuDiv.style.top 	= clientY + "px";
	//menuDiv.style.left 	= newLeft + "px";
	//menuDiv.style.top 	= newTop + "px";
	
	menuDiv.style.visibility = "visible";
	
	editUrl = "";
	
	return false;
}


function showComponentInTreeMenu(event, element, compId, anInsertUrl, anDeleteUrl, anChangeUrl, slotId, slotContentIdVar, aActiveComponentName) 
{
	activeMenuId 		= "componentInTreeMenu";
	slotName 			= slotId;
	slotContentId 		= slotContentIdVar;
	activeComponentName = aActiveComponentName;
	
	try
	{
		var access = eval("hasAccessToDeleteComponent" + convertName(slotName)); 
	    //alert("access:" + access);
	    if(access) 
	    {
	    	document.getElementById("deleteComponentInTreeMenuItem").style.display = "block";
	    	document.getElementById("componentInTreeMenuTopSeparator").style.display = "block";
		}
		else
		{
	    	document.getElementById("deleteComponentInTreeMenuItem").style.display = "none";
	    	document.getElementById("componentInTreeMenuTopSeparator").style.display = "none";
	    }

		var changeAccess = eval("hasAccessToChangeComponent" + convertName(slotName)); 
	    //alert("changeAccess:" + changeAccess);
	    if(changeAccess) 
	    {
	    	document.getElementById("changeComponentInTreeMenuItem").style.display = "block";
	    }
		else
		{
	    	document.getElementById("changeComponentInTreeMenuItem").style.display = "none";
	    }
	}
	catch(e)
	{
		//alert("Error:" + e);
	}

	componentId = compId;
	insertUrl = anInsertUrl;
	deleteUrl = anDeleteUrl;
	changeUrl = anChangeUrl;
	//alert("componentId" + componentId);
    //alert("changeUrl:" + changeUrl);
    
    document.body.onclick = hidepreviousmenues;
	getActiveMenuDiv().className = menuskin;
	
	clientX = getEventPositionX(event);
	clientY = getEventPositionY(event);
	
	var rightedge = document.body.clientWidth - clientX;
	//var bottomedge = document.body.clientHeight - clientY;
	var bottomedge = getWindowHeight() - clientY;

	menuDiv = getActiveMenuDiv();
	
	/*
	if (rightedge < menuDiv.offsetWidth)
		newLeft = (document.body.scrollLeft + clientX - menuDiv.offsetWidth);
	else
		newLeft = (document.body.scrollLeft + clientX);
	
	if (bottomedge < menuDiv.offsetHeight)
		newTop = (document.body.scrollTop + clientY - menuDiv.offsetHeight);
	else
		newTop = (document.body.scrollTop + clientY);
	*/
	
	if (rightedge < menuDiv.offsetWidth)
		clientX = (clientX - menuDiv.offsetWidth);
	
	if (bottomedge < menuDiv.offsetHeight)
		clientY = (clientY - menuDiv.offsetHeight);
	
	menuDiv.style.left 	= clientX + "px";
	menuDiv.style.top 	= clientY + "px";

	//menuDiv.style.left 	= newLeft + "px";
	//menuDiv.style.top 	= newTop + "px";
	
	menuDiv.style.visibility = "visible";
	
	return false;
}

function convertName(val)
{
  	var regexp = new RegExp("[^0-9,a-z,A-Z]", "g");
  	return val.replace(regexp, "_");
}

function showEmptySlotMenu(slotId, event, compId, anInsertUrl, slotContentIdVar) 
{
	/*
	hidepreviousmenues();
	
	activeMenuId = "emptySlotMenu";
	
	slotName = slotId;
	slotContentId = slotContentIdVar;
		 
	slotId = compId;
	insertUrl = anInsertUrl;
	//alert("slotId:" + slotId);
	//alert("slotName:" + slotName);
	//alert("slotContentId:" + slotContentId);
	//alert("CompId:" + compId);
    //alert(insertUrl);
	
    document.body.onclick = hidepreviousmenues;
	getActiveMenuDiv().className = menuskin;
	
	clientX = getEventPositionX(event);
	clientY = getEventPositionY(event);
	
	var rightedge = document.body.clientWidth - clientX;
	//var bottomedge = document.body.clientHeight - clientY;
	var bottomedge = getWindowHeight() - clientY;

	menuDiv = getActiveMenuDiv();
		
	if (rightedge < menuDiv.offsetWidth)
		clientX = (clientX - menuDiv.offsetWidth + 5);
	
	if (bottomedge < menuDiv.offsetHeight)
		clientY = (clientY - menuDiv.offsetHeight + 5);
	
	menuDiv.style.left 	= clientX + "px";
	menuDiv.style.top 	= clientY + "px";

	menuDiv.style.visibility = "visible";
	*/
	return false;
}


function release()
{
	//alert("Releasing...");
	busy = false;
}

function hidepreviousmenues() 
{
	var layer = getActiveMenuDiv();
	if(layer)
		layer.style.visibility = "hidden";
}

function highlightie5(event) 
{
	var layer = event.srcElement || event.currentTarget || event.target;
	//alert("layer:" + layer.className);
	
	if (layer.className == "igmenuitems") 
	{
		layer.style.backgroundColor = "#B6BDD2";
		layer.style.border = "1px solid black";
		layer.style.padding = "2px 10px 2px 30px";
		
		if (display_url)
			window.status = layer.url;
   	}
}

function lowlightie5(event) 
{
	var layer=event.srcElement || event.currentTarget || event.target;
	
	if (layer.className == "igmenuitems") 
	{
		layer.style.backgroundColor = "";
		layer.style.border = "0px solid white";
		layer.style.padding = "3px 11px 3px 31px";

		window.status = "";
	}
}


// -------------------------------------
// This part takes care of browser-right-click (disables it).
// -------------------------------------


isIE=document.all;
isNN=!document.all&&document.getElementById;
isN4=document.layers;

if (isIE||isNN)
{
	document.oncontextmenu=checkV;
}
else
{
	document.captureEvents(Event.MOUSEDOWN || Event.MOUSEUP);
	document.onmousedown=checkV;
}


function checkV(e)
{
	if (isN4)
	{
		if (e.which==2||e.which==3)
			return false;
		else
			return true;
	}
	else
		return false;
}


function showDiv(id)
{
	//alert("id:" + id)
	if(document.getElementById(id))
	{
		document.getElementById(id).style.visibility = 'visible';
		if(id == "pageComponents")
		{
			document.getElementById(id).style.display = 'block';
			setCookie(pageStructureDivVisibleCookieName, "visible");
		}
	}
}

function hideDiv(id)
{
	if(document.getElementById(id))
	{
		document.getElementById(id).style.visibility = 'hidden';
		if(id == "pageComponents")
		{
			document.getElementById(id).style.display = 'none';
			setCookie(pageStructureDivVisibleCookieName, "hidden");
		}
	}
}

function openDiv(id)
{
	document.getElementById(id).style.display = 'block';
}

function closeDiv(id)
{
	document.getElementById(id).style.display = 'none';
}

function toggleDiv(id)
{
	//alert("toggleDiv div:" + id)
	var div = document.getElementById(id);
	if(div && div.style.visibility == 'visible')
		div.style.visibility = 'hidden';
	else
		div.style.visibility = 'visible';
		
	if(id == "pageComponents")
	{
		if(div.style.visibility == 'hidden')
			document.getElementById(id).style.display = 'none';
		else
			document.getElementById(id).style.display = 'block';
			
		setCookie(pageStructureDivVisibleCookieName, div.style.visibility);
	}
		
}

/**
 * This method submit a form.
 */

function submitForm(id)
{
	isDirty = false;
	document.getElementById(id).submit();
}

function submitFormAndExit(id)
{
	isDirty = false;
	try{document.getElementById(id).hideComponentPropertiesOnLoad.value = "true";}catch(err){alert("Err:" + err);}
	document.getElementById(id).submit();
}

var lastRow = null;
var lastRowOriginalBgColor = null;
var selectedRow = null;
var selectedBgColor = "#FFB62A";

function listRowOn(rowEl)
{
	if (lastRow != null)
	{
		lastRow.style.backgroundColor = lastRowOriginalBgColor;
	}

	lastRowOriginalBgColor = rowEl.style.backgroundColor;
	rowEl.style.backgroundColor = selectedBgColor;
	lastRow = rowEl;
}

function listRowOff()
{
	if (lastRow != null)
	{
		if (lastRow != selectedRow)
		{
			lastRow.style.backgroundColor = lastRowOriginalBgColor;
		}
	}
}


var draggedComponentId;
var movedComponentId = "";
var movedElementId = "";
var skipComponentPropertiesLoad = false;
var clearMovedComponent = false;

function assignComponent(e, siteNodeId, languageId, contentId, parentComponentId, slotId, specifyBaseTemplate, allowedComponentNamesUrlEncodedString, disallowedComponentNamesUrlEncodedString, allowedComponentGroupNamesAsUrlEncodedString, slotPositionComponentId) 
{	
	isDirty = false;
	//alert("slotPositionComponentId:" + slotPositionComponentId);
	//alert("parentComponentId:" + parentComponentId);
	//alert("slotId:" + slotId);
	//alert("draggedComponentId:" + draggedComponentId);
	//alert("movedComponentId:" + movedComponentId);
	if(movedComponentId && movedComponentId != "")
	{
		//alert("isIE:" + isIE);
		if(isIE)
		{
			/*
			var targ;
			if (!e) 
				var e = window.event;
				
			if (e.target) 
				targ = e.target;
			else if (e.srcElement) 
				targ = e.srcElement;
			if (targ.nodeType == 3) // defeat Safari bug
				targ = targ.parentNode;
			
			alert("Target:" + targ + ":" + targ.id);

			parentComponentId = targ.id.substring(0, targ.id.indexOf("_"));
			slotId = targ.id.substring(targ.id.indexOf("_") + 1);
			*/
			
			moveUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponentToSlot.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + parentComponentId + "&componentId=" + movedComponentId + "&slotId=" + slotId + "&slotPositionComponentId=" + slotPositionComponentId + "&specifyBaseTemplate=" + specifyBaseTemplate + "&" + allowedComponentNamesUrlEncodedString + "&" + disallowedComponentNamesUrlEncodedString + "&" + allowedComponentGroupNamesAsUrlEncodedString;
			//alert("moveUrl:" + moveUrl);
			document.location.href = moveUrl;
			window.event.cancelBubble = true;
		}
		else
		{
			moveUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponentToSlot.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + parentComponentId + "&componentId=" + movedComponentId + "&slotId=" + slotId + "&slotPositionComponentId=" + slotPositionComponentId + "&specifyBaseTemplate=" + specifyBaseTemplate + "&" + allowedComponentNamesUrlEncodedString + "&" + disallowedComponentNamesUrlEncodedString + "&" + allowedComponentGroupNamesAsUrlEncodedString;
			//alert("moveUrl:" + moveUrl);
			document.location.href = moveUrl;
		}
		movedComponentId = "";
		movedElementId = "";
	}
	else if(draggedComponentId > 0)
	{
		//alert("isIE:" + isIE);
		if(isIE)
		{
			/*
			var targ;
			if (!e) 
				var e = window.event;
				
			if (e.target) 
				targ = e.target;
			else if (e.srcElement) 
				targ = e.srcElement;
			if (targ.nodeType == 3) // defeat Safari bug
				targ = targ.parentNode;
			//alert("Target:" + targ + ":" + targ.id);

			parentComponentId = targ.id.substring(0, targ.id.indexOf("_"));
			slotId = targ.id.substring(targ.id.indexOf("_") + 1);
			*/
			
			insertUrl = componentEditorUrl + "ViewSiteNodePageComponents!addComponent.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + parentComponentId + "&componentId=" + draggedComponentId + "&slotId=" + slotId + "&slotPositionComponentId=" + slotPositionComponentId + "&specifyBaseTemplate=" + specifyBaseTemplate + "&" + allowedComponentNamesUrlEncodedString + "&" + disallowedComponentNamesUrlEncodedString + "&" + allowedComponentGroupNamesAsUrlEncodedString;
			//alert("insertUrl:" + insertUrl);
			document.location.href = insertUrl;
			window.event.cancelBubble = true;
		}
		else
		{
			insertUrl = componentEditorUrl + "ViewSiteNodePageComponents!addComponent.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + parentComponentId + "&componentId=" + draggedComponentId + "&slotId=" + slotId + "&slotPositionComponentId=" + slotPositionComponentId + "&specifyBaseTemplate=" + specifyBaseTemplate + "&" + allowedComponentNamesUrlEncodedString + "&" + disallowedComponentNamesUrlEncodedString + "&" + allowedComponentGroupNamesAsUrlEncodedString;
			//alert("insertUrl:" + insertUrl);
			document.location.href = insertUrl;
		}
		draggedComponentId = -1;
	}
}

function openWindow(url, windowTitle, options) 
{
	newWin = window.open(url, windowTitle, options);
	if(newWin)
		newWin.focus();
	else
		alert("Could not open in new window - if you have a popup blocker this is most likely the cause.");
}

function saveComponentStructure(url) 
{
	details = "width=500,height=600,left=" + (document.body.clientWidth / 4) + ",top=" + (document.body.clientHeight / 4) + ",toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=no";
	newWin = window.open(url, "Save", details);
	if(newWin)
		newWin.focus();
	else
		alert("Could not save - if you have a popup blocker this is most likely the cause.");
}

function savePartComponentStructure(url, componentId) 
{
	//alert("insertUrl in insertComponent:" + insertUrl.substring(0, 50) + '\n' + insertUrl.substring(50));
	details = "width=500,height=600,left=" + (document.body.clientWidth / 4) + ",top=" + (document.body.clientHeight / 4) + ",toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=no";
	newWin = window.open(url + "&componentId=" + componentId, "Save", details);
	if(newWin)
		newWin.focus();
	else
		alert("Could not save - if you have a popup blocker this is most likely the cause.");
}
	
function openAssignDialog(warningText, assignUrl) 
{
 	if(checkDirty(warningText))
 	{
 		isDirty = false;
 		openInlineDiv(assignUrl, 600, 750, true);
 	}
}

//--------------------------------------------
// Here comes the menu items actions
//--------------------------------------------

function edit(editUrl) 
{
	if(!editUrl || editUrl == "")
	{
		alert("You must right click on a text to be able to use this feature.");
	}
	else
	{
		openInlineDiv(editUrl, 700, 750, true);
	}
	
	return false;
}

var isInInlineEditingMode = new Array();
var savingAttributes = new Array();
var savedAttributes = new Array();

function editInlineSimple(selectedRepositoryId) 
{
	return editInline(selectedRepositoryId, selectedContentId, selectedLanguageId, false);
}

function editInline(selectedRepositoryId, selectedContentId, selectedLanguageId, directEditing) 
{	
	hideIGMenu();
		
	//alert("isInInlineEditingMode: " + selectedContentId + ":" + isInInlineEditingMode["" + selectedContentId]);
	if(isInInlineEditingMode["" + selectedContentId] == "true")
	{
		//alert("Content " + selectedContentId + " is allready in inline editing mode");
		return false;
	}
	
	/*	
	if((!editUrl || editUrl == "") && !directEditing)
	{
		alert("You must right click on a text or double click on a text to be able to use this feature.");
	}
	else
	{
	*/
		var $lastThis;
		var processedIds = new Array();
		var firstElement;
		$(".attribute" + selectedContentId).each(function (i) {
	    	if(processedIds["" + this.id] != "true")
	    	{
		    	var $this = $(this);
		    	if(!firstElement)
		    		firstElement = $(this);
		    	
		    	$lastThis = $this;
		    	//alert("this:" + this.id);
		    	var type = jQuery.trim($this.attr("class"));
				//alert("type:" + type);
				
				if(type.indexOf("textarea") > -1)
				{
					var attributeName = editOnSightAttributeNames[$(this).get(0).id];
					var enableWYSIWYG = editOnSightAttributeNames[$(this).get(0).id + "_enableWYSIWYG"];
					var WYSIWYGToolbar = editOnSightAttributeNames[$(this).get(0).id + "_WYSIWYGToolbar"];
					var WYSIWYGExtraConfig = editOnSightAttributeNames[$(this).get(0).id + "_WYSIWYGExtraConfig"];
					//alert("attributeName:" + attributeName);
	
					var parameterString = "repositoryId=" + selectedRepositoryId + "&contentId=" + selectedContentId + "&languageId=" + selectedLanguageId;
					
					var element = $(this).get(0);
		
					var totalWidth = $(this).parent().width();
					
					var totalHeight = 100;
					$("#attribute" + selectedContentId + attributeName + " > *").each(function(i){
						totalHeight = totalHeight + $(this).outerHeight();
					});
					totalHeight = totalHeight * 1.3;
					
					var windowHeight = $(window).height();
					
					if(totalHeight < 300)
						totalHeight = 300;
					if(windowHeight < totalHeight + 150)
						totalHeight = windowHeight - 150;
					if(totalHeight > 800)
						totalHeight = 800;
					
					var span = $(this).get(0);
						
					var data = "contentId=" + selectedContentId + "&languageId=" + selectedLanguageId + "&attributeName=" + attributeName + "&deliverContext=" + currentContext;
		
					var plainAttribute = span.innerHTML;
					$.ajax({
					   	type: "GET",
					   	url: "" + componentEditorUrl + "UpdateContentVersionAttribute!getAttributeValue.action",
					   	data: data,
					   	success: function(msg){
					   		plainAttribute = msg;
						 
						 	if(enableWYSIWYG == "true")
						 	{
							 	var oFCKeditor = new FCKeditor($this.get(0).id);
							 	oFCKeditor.BasePath = "" + componentEditorUrl + "applications/FCKEditor/" ;
							 	oFCKeditor.Config["CustomConfigurationsPath"] = "" + componentEditorUrl + "WYSIWYGProperties.action?" + parameterString;
							 	oFCKeditor.Config["AutoDetectLanguage"] = false ;
							 	oFCKeditor.ToolbarSet = WYSIWYGToolbar;
							 	if(WYSIWYGExtraConfig && WYSIWYGExtraConfig != '')
							 		eval(WYSIWYGExtraConfig);
							 
							 	oFCKeditor.Height = totalHeight;
							 	if(totalWidth > 100)
									oFCKeditor.Width = totalWidth;
							 	oFCKeditor.Value = plainAttribute;
							 	$this.html(oFCKeditor.CreateHtml());
						 	}
						 	else
						 	{
								var fontFamily 	= $this.parent().css("font-family");
								var fontSize 	= $this.parent().css("font-size");
								var fontWeight 	= $this.parent().css("font-weight");
								var color 		= $this.parent().css("color");
								var textareaHeight = $this.parent().height();
								if(textareaHeight < 50)
									textareaHeight = 50;
										
						 		$this.html("<textarea id='input" + $this.get(0).id + "' ondblclick='if (event && event.stopPropagation) {event.stopPropagation();}else if (window.event) {window.event.cancelBubble = true;}return false;'>" + plainAttribute + "</textarea>");
						 		$("#input" + $this.get(0).id + "").css("display", "block");
						 		$("#input" + $this.get(0).id + "").css("font-family", fontFamily);
								$("#input" + $this.get(0).id + "").css("font-size", fontSize);
								$("#input" + $this.get(0).id + "").css("font-weight", fontWeight);
								$("#input" + $this.get(0).id + "").css("color", color);
								$("#input" + $this.get(0).id + "").css("border", "1px solid #ccc");
								$("#input" + $this.get(0).id + "").css("width", totalWidth);
								$("#input" + $this.get(0).id + "").css("height", textareaHeight);
						 	}
					   },
					   error: function (XMLHttpRequest, textStatus, errorThrown) {
						   if(XMLHttpRequest.status == 403)
						   {
							   alert("You are not logged in properly to the administrative tools - please log in again.");
							   window.open("" + componentEditorUrl + "ViewCMSTool!loginStandalone.action", "Login", "width=400,height=420");
						   }
						   else
						   {
							   alert("You are not allowed to edit this text!");
						   }
					   }
					});
				}
				else if(type.indexOf("textfield") > -1)
				{
					var attributeName = editOnSightAttributeNames[$(this).get(0).id];
					//alert("attributeName:" + attributeName);
					
					var elementObject = $this;
					var text = elementObject.html();
					//alert("text:" + text);
					var fontFamily = elementObject.parent().css("font-family");
					var fontSize = elementObject.parent().css("font-size");
					var color = elementObject.parent().css("color");
					var tagName = elementObject.parent()[0].tagName.toLowerCase();
					
					//This is a hack to fix the fact that IE returns a font-size of over 200px if the font-size on the element is not set.
					if(fontSize.length > 4)
					{
						if(tagName == "h1")
							fontSize = "22px";
						else if(tagName == "h2")
							fontSize = "18px";
						else
							fontSize = "12px";
					}
					
					elementObject.html("<span id='spanInput" + $this.get(0).id + "'><input class='edit' style='width: 80%' ondblclick='if (event && event.stopPropagation) {event.stopPropagation();}else if (window.event) {window.event.cancelBubble = true;}return false;' id='input" + $this.get(0).id + "' type='text' value='" + escapeAmpsAndQuotes(text) + "' /> </span>");
					$(".edit").css("font-family", fontFamily);
					$(".edit").css("font-size", fontSize);
					$(".edit").css("color", color);
					$(".edit").css("border", "1px solid #ccc");
				}
				else
				{
					alert("The type: " + type + " in your content type is not supported yet in inline edit.");
				}
	
		    	processedIds["" + this.id] = "true";
			}
			//else
			//	alert("Attribute:" + this.id + " was allready processed");		
	    });
				
		var topOffset = firstElement.offset().top - 10;
		//alert("topOffset:" + topOffset);
		window.scroll(0, topOffset);
		firstElement = null;

		var saveLabel = "Save";
		var cancelLabel = "Cancel";
		if(userPrefferredLanguageCode == "sv")
		{
			saveLabel = "Spara";
			cancelLabel = "Avbryt";
		}
		
		$lastThis.after("<div id=\"saveButtons" + selectedContentId + "\" class='componentPropertiesFooter'>" +
				"<input type='button' class='save' onclick='saveAttributes(" + selectedContentId + ", " + selectedLanguageId + ");' value='" + saveLabel + "'/>" +
				"<input type='button' class='cancel' onclick='cancelSaveAttributes(" + selectedContentId + ", " + selectedLanguageId + ");' value='" + cancelLabel + "'/>" +
				"</div><div style='clear:both;'></div>");

		//$lastThis.after("<div id=\"saveButtons" + selectedContentId + "\"><a class='igButton' onclick='saveAttributes(" + selectedContentId + ", " + selectedLanguageId + ");'' title='" + saveLabel + "'><span class='igButtonOuterSpan'><span class='linkSave'>" + saveLabel + "</span></span></a><a class='igButton' onclick='cancelSaveAttributes(" + selectedContentId + ", " + selectedLanguageId + ");' title='" + cancelLabel + "'><span class='igButtonOuterSpan'><span class='linkCancel'>" + cancelLabel + "</span></span></a></div><div style='clear:both;'></div>");
		isInInlineEditingMode["" + selectedContentId] = "true"
	//}
}

function saveAttributes(selectedContentId, selectedLanguageId) 
{
	//alert("selectedContentId: " + selectedContentId + " - " + selectedLanguageId);
	for (key in editOnSightAttributeNames)
	{
		//alert("Key:" + key);
		if(key.indexOf("attribute" + selectedContentId) > -1 && key.indexOf("_type") == -1 && 
																key.indexOf("_enableWYSIWYG") == -1 && 
																key.indexOf("_originalValue") == -1 && 
																key.indexOf("_WYSIWYGToolbar") == -1 &&
																key.indexOf("_WYSIWYGExtraConfig") == -1)
		{
			var attributeName = editOnSightAttributeNames[key];
			var attributeType = editOnSightAttributeNames[key + "_type"];

			if(!savingAttributes["" + selectedContentId])
				savingAttributes["" + selectedContentId] = new Array();
			
			if(!savedAttributes["" + selectedContentId])
				savedAttributes["" + selectedContentId] = new Array();

			if(!savedAttributes["" + selectedContentId]["" +attributeName])
			{
				savingAttributes["" + selectedContentId]["" + attributeName] = "true";
				var size = 0;
				for (var i in savingAttributes["" + selectedContentId])
					size++;
				//alert("Added:" + attributeName + " to " + "" + selectedContentId + " - size now:" + size);
				//alert("savingAttributes:" + savingAttributes["" + selectedContentId]["" + attributeName]);
				saveAttribute(selectedContentId, selectedLanguageId, attributeName, attributeType, key);
			}
		}
	}
}

function cancelSaveAttributes(selectedContentId, selectedLanguageId) 
{
	//alert("selectedContentId: " + selectedContentId + " - " + selectedLanguageId);
	for (key in editOnSightAttributeNames)
	{
		//alert("Key:" + key);
		if(key.indexOf("attribute" + selectedContentId) > -1 && key.indexOf("_type") == -1 && 
																key.indexOf("_enableWYSIWYG") == -1 && 
																key.indexOf("_originalValue") == -1 && 
																key.indexOf("_WYSIWYGToolbar") == -1 &&
																key.indexOf("_WYSIWYGExtraConfig") == -1)
		{
			var attributeName = editOnSightAttributeNames[key];
			var attributeType = editOnSightAttributeNames[key + "_type"];
			//alert("Saving:" + attributeName + " - " + attributeType);
			cancelSaveAttribute(selectedContentId, selectedLanguageId, attributeName, attributeType, key);
		}
	}
	
	$("#saveButtons" + selectedContentId).remove();
	delete savingAttributes["" + selectedContentId];
	isInInlineEditingMode["" + selectedContentId] = "false"
}

function completeEditInlineSave(selectedContentId, selectedAttributeName)
{
    delete savingAttributes["" + selectedContentId]["" + selectedAttributeName];
    savedAttributes["" + selectedContentId]["" + selectedAttributeName] = "true";
    
	var size = 0;
	for (var i in savingAttributes["" + selectedContentId])
		size++;
	
	//alert("savingAttributes:" + savingAttributes["" + selectedContentId].length + " - " + size);
	if(size == 0)
	{
		$("#saveButtons" + selectedContentId).remove();
		//alert("selectedContentId:" + selectedContentId + " setting to false");
		isInInlineEditingMode["" + selectedContentId] = "false"
		savedAttributes = new Array();
	}
}


function saveAttribute(selectedContentId, selectedLanguageId, selectedAttributeName, type, key)
{
	if(type == "textarea")
	{
		var enableWYSIWYG = editOnSightAttributeNames["attribute" + selectedContentId + selectedAttributeName + "_enableWYSIWYG"];
		
		var value = "";
		if(enableWYSIWYG == "true")
		{
			var oEditor = FCKeditorAPI.GetInstance("attribute" + selectedContentId + selectedAttributeName) ;
			value = oEditor.GetXHTML( true )
			//alert("Value: " + value);
			value = Url.encode(value);
			//alert("Value: " + value);
		}
		else
		{
			value = $("#inputattribute" + selectedContentId + selectedAttributeName).val();
		}
		
		var data = "contentId=" + selectedContentId + "&languageId=" + selectedLanguageId + "&attributeName=" + selectedAttributeName + "&" + selectedAttributeName + "=" + value + "&deliverContext=" + currentContext;

		$.ajax({
		   type: "POST",
		   url: "" + componentEditorUrl + "UpdateContentVersionAttribute!saveAndReturnValue.action",
		   data: data,
		   success: function(msg){
		   	 //alert( "Data Saved: " + msg );
		     if(enableWYSIWYG == "true")
			 {	
			     var oEditor = FCKeditorAPI.GetInstance("attribute" + selectedContentId + selectedAttributeName) ;
				 //$(oEditor.LinkedField.parentNode.parentNode).html(msg);
				 $(oEditor.LinkedField.parentNode).html(msg);
		     }
		     else
		     {
		     	$("#inputattribute" + selectedContentId + selectedAttributeName).replaceWith(msg);
		     }
		     completeEditInlineSave(selectedContentId, selectedAttributeName);
		   },
		   error: function (XMLHttpRequest, textStatus, errorThrown) {
			   if(XMLHttpRequest.status == 403)
			   {
				   alert("You are not logged in properly to the administrative tools - please log in again.");
				   window.open("" + componentEditorUrl + "ViewCMSTool!loginStandalone.action", "Login", "width=400,height=420");
			   }
			   else if(XMLHttpRequest.status == 406)
			   {
				   alert("The value must not be empty - update failed");
			   }
			   else
			   {
				   alert("Update failed!");
			   }
		   }
		 });
	}
	else if(type == "textfield")
	{
		//alert("Saving: " + selectedContentId + " " + selectedLanguageId + " " +  selectedAttributeName);
		var value = $("#inputattribute" + selectedContentId + selectedAttributeName).val();
		//alert("Value: " + value);
		//value = Url.encode(value);
		//alert("Value: " + value);
		var data = "contentId=" + selectedContentId + "&languageId=" + selectedLanguageId + "&attributeName=" + selectedAttributeName + "&" + selectedAttributeName + "=" + value;
	
		$.ajax({
			type: "POST",
		   	url: "" + componentEditorUrl + "UpdateContentVersionAttribute!saveAndReturnValue.action",
		   	data: data,
		   	success: function(msg){
		   		//alert( "Data Saved: " + msg );
				$("#spanInput" + key).parent().html(msg);
		     	//$("#spanInput" + key).replaceWith(msg);
			    completeEditInlineSave(selectedContentId, selectedAttributeName);
		   	},
		   error: function (XMLHttpRequest, textStatus, errorThrown) {
			   if(XMLHttpRequest.status == 403)
			   {
				   alert("You are not logged in properly to the administrative tools - please log in again.");
				   window.open("" + componentEditorUrl + "ViewCMSTool!loginStandalone.action", "Login", "width=400,height=420");
			   }
			   else if(XMLHttpRequest.status == 406)
			   {
				   alert("The value must not be empty - update failed");
			   }
			   else
			   {
				   alert("Update failed!");
			   }
		   }
		});
	}
}

function cancelSaveAttribute(selectedContentId, selectedLanguageId, selectedAttributeName, type, key)
{
	if(type == "textarea")
	{
		var enableWYSIWYG = editOnSightAttributeNames["attribute" + selectedContentId + selectedAttributeName + "_enableWYSIWYG"];
		
		var data = "contentId=" + selectedContentId + "&languageId=" + selectedLanguageId + "&attributeName=" + selectedAttributeName + "&deliverContext=" + currentContext;

		$.ajax({
		   	type: "GET",
		   	url: "" + componentEditorUrl + "UpdateContentVersionAttribute!getAttributeValue.action",
		   	data: data,
		   	success: function(msg){
		   		if(enableWYSIWYG == "true")
				{
		     		var oEditor = FCKeditorAPI.GetInstance("attribute" + selectedContentId + selectedAttributeName) ;
		     		//$(oEditor.LinkedField.parentNode.parentNode).html(msg);
		     		$(oEditor.LinkedField.parentNode).html(msg);
		     	}
		     	else
		     	{
		     		$("#inputattribute" + selectedContentId + selectedAttributeName).replaceWith(msg);
		     	}
		   }
		 });
	}
	else if(type == "textfield")
	{
		//alert("Saving: " + selectedContentId + " " + selectedLanguageId + " " +  selectedAttributeName);
		var value = $("#inputattribute" + selectedContentId + selectedAttributeName).val();
		//alert("Value: " + value);
		var data = "contentId=" + selectedContentId + "&languageId=" + selectedLanguageId + "&attributeName=" + selectedAttributeName + "&deliverContext=" + currentContext;
	
		$.ajax({
		   type: "GET",
		   url: "" + componentEditorUrl + "UpdateContentVersionAttribute!getAttributeValue.action",
		   data: data,
		   success: function(msg){
		     $("#spanInput" + key).replaceWith(msg);
		   }
		 });
	}
	
	$("#saveButtons" + selectedContentId).remove();
}



var Url = {

    // public method for url encoding
    encode : function (string) {
        return escape(this._utf8_encode(string));
    },

    // public method for url decoding
    decode : function (string) {
        return this._utf8_decode(unescape(string));
    },

    // private method for UTF-8 encoding
    _utf8_encode : function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {

            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }

        return utftext;
    },

    // private method for UTF-8 decoding
    _utf8_decode : function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;

        while ( i < utftext.length ) {

            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            }
            else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            }
            else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }

        }

        return string;
    }

}






//--------------------------------------------
// Function submitting the page to publishing
//--------------------------------------------

function submitToPublish(siteNodeId, contentId, languageId, repositoryId, returnAddress) 
{
	var url = componentEditorUrl + "ViewListSiteNodeVersion!v3.action?siteNodeId=" + siteNodeId + "&contentId=" + contentId + "&languageId=" + languageId + "&repositoryId=" + repositoryId + "&recurseSiteNodes=false&returnAddress=" + returnAddress + "&originalAddress=" + currentUrl;
	openInlineDiv(editUrl, 700, 750, true);

}

function unpublishContent(contentId, returnAddress)
{
	var url = componentEditorUrl + "UnpublishContentVersion!inputChooseContentsV3.action?contentId=" + contentId + "&title=tool.contenttool.unpublishAllVersion.header&returnAddress=" + returnAddress + "&originalAddress=" + currentUrl;
	openInlineDiv(url, 700, 750, true);
}

function executeTask(url, openInPopup) 
{
	if(openInPopup)
	{
		//alert("insertUrl in insertComponent:" + insertUrl.substring(0, 50) + '\n' + insertUrl.substring(50));
		details = "width=500,height=600,left=" + (document.body.clientWidth / 4) + ",top=" + (document.body.clientHeight / 4) + ",toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=no";
		newWin=window.open(url, "Edit", details);
		if(newWin)
			newWin.focus();
		else
			alert("Could not start task - if you have a popup blocker this is most likely the cause.");
	}
	else
	{
		$(document.body).append("<div id='dialogDiv' style='border: 1px solid black; position: absolute; top: 100px; left: 200px; width: 600px; height: 500px; background-color: white;'><iframe id='dialogFrame' name='dialogFrame' src='' width='100%' height='100%' border='0'></iframe></div>");
		$("#dialogFrame").attr('src', url);
	}
}

function insertComponent() 
{
	isDirty = false;
	openInlineDiv(insertUrl, 600, 750, true);
}

function insertComponentByUrl(insertUrl) 
{
	isDirty = false;
	openInlineDiv(insertUrl, 600, 750, true);
}

function setAccessRights(slotId, slotContentId) 
{
	isDirty = false;
	//alert("slotId in setAccessRights:" + slotId);
	//alert("currentUrl:" + document.location.href);
	//alert("slotId: " + slotId + " - slotContentId:" + slotContentId);
	openInlineDiv(componentEditorUrl + "ViewAccessRights!V3.action?interceptionPointCategory=ComponentEditor&extraParameters=" + slotContentId + "_" + slotId + "&returnAddress=ViewInlineOperationMessages.action&originalAddress=refreshParent", 800, 600, true);
}

function deleteComponent(aConfirmText) 
{
	isDirty = false;
	//alert("deleteUrl in deleteComponent:" +  + deleteUrl.substring(0, 50) + '\n' + deleteUrl.substring(50));
	if (confirm(aConfirmText.replace("COMPONENT_NAME", activeComponentName)))
	{
		document.location.href = deleteUrl;
	}
}

function deleteComponentByUrl(url) 
{
	isDirty = false;
	//alert("deleteUrl in deleteComponent:" +  + deleteUrl.substring(0, 50) + '\n' + deleteUrl.substring(50));
	document.location.href = url;
}

function changeComponent() 
{
	isDirty = false;
	openInlineDiv(changeUrl, 600, 750, true);
}

function changeComponentByUrl(url) 
{
	isDirty = false;
	openInlineDiv(url, 600, 750, true);
}

function invokeAddress(url) 
{
	document.location.href = url;
}

function showComponent(e) 
{
	if (!e) 
		e = window.event;
	showComponentProperties("component" + componentId + "Properties", e);
}

function showComponentInDiv(targetDiv, parameterString, skipFloat) 
{
	//alert("targetDiv:" + targetDiv + " - " + parameterString + " - " + skipFloat);
	//if (!e) 
		e = window.event;
	showComponentPropertiesInDiv(targetDiv, parameterString, skipFloat, e);
}

function showComponentTasks(targetDiv, parameterString, skipFloat, e) 
{
	if (!e) 
		e = window.event;
	showComponentTasksInDiv(targetDiv, parameterString, skipFloat, e);
}

function showComponentStructure(targetDiv, parameterString, e) 
{
	if (!e) 
		e = window.event;
	showComponentStructureInDiv(targetDiv, parameterString, e);
}

function showComponentProperties(id, event) 
{
	//alert("id:" + id);
	if (!event) 
		event = window.event;

	showDiv(id);

	var element = $("#" + id);
	
	var scrollTop = $(window).scrollTop();
	var scrollLeft = $(window).scrollLeft();
	
	var newTop = $(window).height()/2-element.height()/2 + scrollTop;
	var newLeft = $(window).width()/2-element.width()/2 + scrollLeft;

	if(newTop < 0)
		newTop = 10;
	if(newLeft < 0)
		newLeft = 10;

	element.css('top', newTop + "px");
	element.css('left', newLeft + "px");
	
	/*
	propertiesDiv = document.getElementById(id);

	clientX = getEventPositionX(event);
	clientY = getEventPositionY(event);
	
	var rightedge = document.body.clientWidth;
	var bottomedge = document.body.clientHeight - clientY;

	menuDiv = propertiesDiv;
	
	if (rightedge < menuDiv.offsetWidth)
		newLeft = (document.body.scrollLeft + clientX - menuDiv.offsetWidth);
	else
		newLeft = (document.body.scrollLeft + clientX);
	
	newTop = (document.body.scrollTop + clientY);
	
	menuDiv.style.left 	= newLeft + "px";
	menuDiv.style.top 	= newTop + "px";	
	*/
}

function clearComponentPropertiesInDiv(targetDivId) 
{
	targetDiv = document.getElementById(targetDivId);
	if(targetDiv)
		$(targetDiv).html("");
	else
		closeDiv("componentPropertiesDiv");
}

function showComponentPropertiesInDiv(targetDivId, parameterString, skipFloat, event) 
{
	hideIGMenu();
	try
	{
	//console.log("skipFloat:" + skipFloat + " - targetDivId:" + targetDivId);
	//alert("parameterString:" + parameterString);

	targetDiv = document.getElementById(targetDivId);
	//console.log("targetDiv:" + targetDiv);

	if(skipFloat && targetDiv)
	{
		$(targetDiv).load("AjaxComponentDeliveryService!getComponentPropertyDiv.action?" + parameterString + "&targetDivId=" + targetDivId + "",{}, function(event)
		{	
			try
			{
				var theRoot = document.getElementById("componentProperties");
				var repositoryId = theRoot.getAttribute("data-repositoryid");
				var languageId = theRoot.getAttribute("data-languageid") || 1;
				var languageCode = theRoot.getAttribute("data-languagecode") || "sv";
				$(targetDiv).find(".wysiwygeditor").each(function(){
			 		if(userPrefferredWYSIWYG  == "ckeditor4" || userPrefferredWYSIWYG  == "" || typeof(userPrefferredWYSIWYG )=="undefined")
			 		{
						var toolbarName = $(this).attr("toolbarName");
						if(!toolbarName || toolbarName == "")
							toolbarName = "Basic";
						var configString = "{'customConfig': '" + componentEditorUrl + "WYSIWYGProperties.action?repositoryId=" + repositoryId + "&contentId=-1&languageId=" + languageId + "&contentVersionId=-1','language': '" + languageCode + "', 'toolbar': '" + toolbarName + "', 'width':'" + $(this).width() + "px', 'height':'200px'}";
						var configObject = eval('(' + configString + ')');
						var editor = CKEDITOR.replace( $(this).attr("id").replace(/ /, "_"), configObject);
			 		}
			 		else
			 		{
						var fck = new FCKeditor("myFCKeditor");
						var id = $(this).attr("id");
						var toolbarName = $(this).attr("toolbarName");
						if(!toolbarName || toolbarName == "")
							toolbarName = "Basic";
							
						var oFCKeditor = new FCKeditor( id ) ;
						oFCKeditor.BasePath = "" + componentEditorUrl + "applications/FCKEditor/" ;
						oFCKeditor.Config["CustomConfigurationsPath"] = "" + componentEditorUrl + "WYSIWYGProperties.action?" + parameterString;
						oFCKeditor.ToolbarSet = toolbarName;
						oFCKeditor.ReplaceTextarea() ;
			 		}
				});

				var theRoot   = document.getElementById("componentProperties");
				if(theRoot)
				{
					var theHandle = document.getElementById("componentPropertiesHandle");
					var windowHeight = $(window).height();
					if($(window).height() > 1000)
						windowHeight = 1000;
					
					$(theRoot).css('top', windowHeight / 2 + $(window).scrollTop() - ($(theRoot).height() / 2));
					$(theRoot).css('left', $(window).width() / 2 - $(theRoot).width() / 2); 
							                        
					$(theHandle).css("cursor", "move");
					$(theRoot).draggable({handle: theHandle});
				}
			}
			catch(err)
			{
				alert("Error 5:" + err);
			}
		});
	}
	else
	{
		if(!targetDiv)
			targetDiv = document.getElementById("componentPropertiesDiv");
		
		$(document.getElementById("componentProperties")).remove();
		
		$(targetDiv).load("AjaxComponentDeliveryService!getComponentPropertyDiv.action?" + parameterString + "&targetDivId=" + targetDivId + "",{}, function(event)
		{	
			try
			{
				var theRoot = document.getElementById("componentProperties");
				var repositoryId = theRoot.getAttribute("data-repositoryid");
				var languageId = theRoot.getAttribute("data-languageid") || 1;
				var languageCode = theRoot.getAttribute("data-languagecode") || "sv";
				$(targetDiv).find(".wysiwygeditor").each(function(){
					if(userPrefferredWYSIWYG  == "ckeditor4" || userPrefferredWYSIWYG  == "" || typeof(userPrefferredWYSIWYG )=="undefined")
					{
						var toolbarName = $(this).attr("toolbarName");
						if(!toolbarName || toolbarName == "")
							toolbarName = "Basic";
						var configString = "{'customConfig': '" + componentEditorUrl + "WYSIWYGProperties.action?repositoryId=" + repositoryId + "&languageId=" + languageId + "&contentVersionId=-1','language': '" + languageCode + "', 'toolbar': '" + toolbarName + "', 'width':'" + $(this).width() + "px', 'height':'200px'}";
						var configObject = eval('(' + configString + ')');
						$(this).attr('id', $(this).attr("id").replace(/ /g, "_"));
						var editor = CKEDITOR.replace( $(this).attr("id"), configObject);
					}
					else
					{
						var fck = new FCKeditor("myFCKeditor");
						var id = $(this).attr("id");
						var toolbarName = $(this).attr("toolbarName");
						if(!toolbarName || toolbarName == "")
							toolbarName = "Basic";
							
						var oFCKeditor = new FCKeditor( id ) ;
						oFCKeditor.BasePath = "" + componentEditorUrl + "applications/FCKEditor/" ;
						oFCKeditor.Config["CustomConfigurationsPath"] = "" + componentEditorUrl + "WYSIWYGProperties.action?" + parameterString;
						oFCKeditor.ToolbarSet = toolbarName;
						oFCKeditor.ReplaceTextarea() ;
					}
				});

				var theHandle = document.getElementById("componentPropertiesHandle");
				//var theRoot   = document.getElementById("componentProperties");
				//console.log($(window).height() / 2 + $(window).scrollTop() - ($(theRoot).height() / 2));
				var windowHeight = $(window).height();
				if($(window).height() > 1000)
					windowHeight = 1000;
				$(theRoot).css('top', windowHeight / 2 + $(window).scrollTop() - ($(theRoot).height() / 2));
				$(theRoot).css('left', $(window).width() / 2 - $(theRoot).width() / 2); 
						                        
				$(theHandle).css("cursor", "move");
				$(theRoot).draggable({handle: theHandle});
			}
			catch(err)
			{
				alert("Error 4:" + err + " parameterString " + parameterString);
				if (typeof console !== "undefined")
				{
					console.error("Error in component properties view", err);
				}
			}
		});		
	}	

	if(targetDiv)
	{		
		$(targetDiv).css("visibility", "visible");
		targetDiv.style.display = "block";
		menuDiv = targetDiv;
	}
	
	}
	catch(e)
	{
		//alert("Error:" + e);
	}
}

var eventXPosition = 0;
var eventYPosition = 0;

function showComponentTasksInDiv(targetDivId, parameterString, skipFloat, event) 
{
	try
	{
	//alert("targetDivId:" + targetDivId);
	//alert("event:" + event);
	//alert("parameterString:" + parameterString);
	//alert("skipFloat:" + skipFloat);
	
	targetDiv = document.getElementById(targetDivId);
	//alert("targetDiv:" + targetDiv);
	
	eventXPosition = getEventPositionX(event);
	eventYPosition = getEventPositionY(event);

	//alert("eventXPosition:" + eventXPosition);
	//alert("eventYPosition:" + eventYPosition);
		
	$(targetDiv).load("AjaxComponentDeliveryService!getComponentTasksDiv.action?" + parameterString + "&targetDivId=" + targetDivId + "",{}, function(event){

		menuDiv = document.getElementById("componentMenu");
		
		clientX = eventXPosition;
		clientY = eventYPosition;
			
		var rightedge = document.body.clientWidth - clientX;
		var bottomedge = getWindowHeight() - clientY;
					
		var editDivElement = document.getElementById("editDiv");
		var editInlineDivElement = document.getElementById("editInlineDiv");
		var subscribeContentDivElement = document.getElementById("subscribeContent");
		if(!editUrl || editUrl == "")
		{
			if(editDivElement)
				editDivElement.style.display = "none";
			if(editInlineDivElement)
				editInlineDivElement.style.display = "none";
			if(subscribeContentDivElement)
				subscribeContentDivElement.style.display = "none";
		}
		else
		{
			editDivElement.style.display = "block";
			editInlineDivElement.style.display = "block";
			subscribeContentDivElement.style.display = "block";
			//alert("Registering click to:" + editUrl);
			var anEditUrl = editUrl;
			$(editDivElement).click(function () { edit(anEditUrl); });
			$(editInlineDivElement).click(function () { editInlineSimple(selectedRepositoryId); });
			var subscriptionUrl = componentEditorUrl + "Subscriptions!input.action?interceptionPointCategory=Content&entityName=org.infoglue.cms.entities.content.Content&entityId=" + selectedContentId + "&extraParameters=" + selectedContentId + "&returnAddress=" + componentEditorUrl + "ViewInlineOperationMessages.action";
			$(subscribeContentDivElement).click(function () { openInlineDiv(subscriptionUrl, 700, 750, true); });
		}
				
		if(window.parent.name == "PageEditor")
			$(menuDiv).find("#componentEditorInNewWindowDiv").css("display", "none");
		
		if (rightedge < menuDiv.offsetWidth)
			clientX = (clientX - menuDiv.offsetWidth);
		
		if (bottomedge < menuDiv.offsetHeight && (clientY - menuDiv.offsetHeight > 0))
			clientY = (clientY - menuDiv.offsetHeight);

		menuDiv.style.left 	= clientX + "px";
		menuDiv.style.top 	= clientY + "px";
		
		menuDiv.style.visibility = "visible";
		
		editUrl = "";
		
		activeMenuId = "componentMenu";
	});

	}
	catch(e)
	{
		//alert("Error:" + e);
	}

	//alert("3");
    if (event && event.stopPropagation) {event.stopPropagation();}
    else if (window.event) {window.event.cancelBubble = true;}	
    return false;
}

function showComponentStructureInDiv(targetDivId, parameterString, event) 
{
	//alert("targetDivId:" + targetDivId);
	//alert("parameterString:" + parameterString);
	targetDiv = document.getElementById(targetDivId);
	
	eventXPosition = getEventPositionX(event);
	eventYPosition = getEventPositionY(event);
		
	$(targetDiv).load("AjaxComponentDeliveryService!getComponentStructureDiv.action?" + parameterString + "&targetDivId=" + targetDivId + "",{}, function(event){
		componentStructureDiv = document.getElementById("componentStructure");
		pageComponentsDiv 	  = document.getElementById("pageComponents");
			
		componentStructureDiv.style.visibility = "visible";
		floatDiv("pageComponents", 200, 50).flt();
		
		//activeMenuId = "componentStructure";
	});
}

function invokeAction() 
{
	//alert("editUrl in invokeAction:" + editUrl);
	details = "width=500,height=700,left=" + (document.body.clientWidth / 4) + ",top=" + (document.body.clientHeight / 4) + ",toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=yes";
	//window.open(editUrl, "Edit", details);
}

function viewSource() 
{
	window.location = "view-source:" + window.location.href;
}

      var hit;
	  var draggedComponentId = -1;
	  
      // -- Determine browser
      var IE  = (document.all)? true: false;
      var Mac = (navigator.cpuClass && navigator.cpuClass.match(/PPC/))? true: false;

      //-----------------------------------------------------------------
      // browser-independent routines for determining event position
      //-----------------------------------------------------------------
      function getX(e) {
      	var x = 0;
        if (IE) {
          x = e.clientX;
          if (!Mac) {
            x += document.documentElement.scrollLeft;
            x += document.body.scrollLeft;
		  }
        } else {
          x = e.pageX + window.scrollX;
        }
        return x
      }

      function getY(e) {
        var y = 0;
        if (IE) {
          y = e.clientY;
          if (!Mac) {
            y += document.documentElement.scrollTop;
            y += document.body.scrollTop;
		  }
        } else {
          y = e.pageY + window.scrollY;
        }
        return y;
      }

      //-----------------------------------------------------------------
      // onmousedown handler.  Start drag op
      //-----------------------------------------------------------------
      function grabIt(e) {

        // -- event and target element references are browser-specific.  UGH
        if (!e) 
        	e = window.event;
        var field  = IE? e.srcElement: e.target;   
        var target = getRect(field);	// target element position offsets
		
		//alert("field:" + field)
        //alert("field:" + field.id)
        draggedComponentId = field.id;
        //alert("draggedComponentId" + draggedComponentId)
        
        // -- initialize drag object and store difference between its edges and the mouse
        drag  = document.getElementById("buffer");
        var x = getX(e);
        var y = getY(e);
        //alert("x:" + x);
        //alert("y:" + y);
        
        //alert("target:" + target.left + ":" + target.top);
        
        //alert("drag.dx:" + drag.dx);
        //alert("drag.dy:" + drag.dy);
        
        newXPos = x; // - target.left - 16;
        newYPos = y; // - target.top;
        //alert("newXPos" + newXPos);
        //alert("newYPos" + newYPos);
        //drag.dx =  x - target.left - 16;
        //drag.dy = y - target.top;
        drag.style.left = newXPos + "px";
        drag.style.top = newYPos + "px";
        
        //alert("drag.dx:" + drag.dx);
        //alert("drag.dy:" + drag.dy);
        //alert("drag.style.left:" + drag.style.left);
        //alert("drag.style.top:" + drag.style.top);
        
		//alert("field:" + field)
		
        // -- deactivate cloaking device
        with (drag) 
        {
          //style.top        = target.top;
	      //style.left       = target.left;
	      //style.width      = target.right - target.left;
          style.visibility = "visible";
          //innerHTML        = field.innerHTML;
        }

        // -- Capture mousemove and mouseup events on the page.
        document.onmousemove = dragIt;
        //document.onmouseup   = dropIt;

	// -- block all other events
        if (IE) {
          e.cancelBubble = true;
          e.returnValue  = false;
        } else {
          e.preventDefault();
        }
        return false;
      }

    //-----------------------------------------------------------------
    // onmousemove handler
    //-----------------------------------------------------------------
    function dragIt(e) 
    {
		if (!e) 
        	e = window.event;
	    
        // -- Move drag element by the same amount the cursor has moved.
        with (drag) 
        {
        	style.left = getX(e) + 1 + "px"; //- dx;
          	style.top  = getY(e) + 1 + "px"; //- dy;
        	//style.left = getX(e) - dx;
          	//style.top  = getY(e) - dy;
        }

        return false;
    }

      //-----------------------------------------------------------------
      // onmouseup handler
      //-----------------------------------------------------------------
      function dropIt(e) {

        // -- engage cloaking device
        drag.style.visibility = "hidden";

        // -- get bounding rectangle for drag buffer
        var rd = getRect(drag);

        // -- loop over all form elements
		var divs = document.getElementsByTagName("span"); 
        for(i = 0; i < divs.length; i++) 
        {

          // -- if we aren't a drag target, go to the next element
          var field = divs[i];
          if (! field.className.match(/dragTarget/)) continue;

  		  // -- is drag buffer over this target?
          var rt = getRect(field);
          var boundHorz = (rd.left > rt.left) && (rd.right  < rt.right);
          var boundVert = (rd.top  > rt.top)  && (rd.bottom < rt.bottom);
          //alert(field.className);
          //alert("buffer.left:" + rd.left);
          //alert("span.left:" + rt.left);
          //alert("buffer.right:" + rd.right);
          //alert("span.right:" + rt.right);
          
          //alert(boundHorz);
          //alert(boundVert);
          
          if (boundHorz && boundVert) {
            //alert("Drag is over:" + field.id);
			field.value += drag.innerHTML + ', ';
            break;
          }
        }
        
        // -- IE5/Mac requires this so the drag element doesn't take up full screen width
        drag.innerHTML = '';

        // -- Stop capturing mousemove & mouseup events.
        document.onmousemove = null;
        document.onmouseup   = null;
      }


      function getRect(obj) {
        var rect = new Object();
        rect.top = rect.left = 0;
        var parentObj = obj;
        while (parentObj != null) {
          rect.top  += parentObj.offsetTop;
          rect.left += parentObj.offsetLeft;
          parentObj = parentObj.offsetParent;
        }
        rect.bottom = rect.top  + obj.offsetHeight;
        rect.right  = rect.left + obj.offsetWidth;

		//if()
        return rect;
      }



      //-----------------------------------------------------------------
      // onmouseup handler
      //-----------------------------------------------------------------
      function dropItem(e) {

        // -- engage cloaking device
        drag.style.visibility = "hidden";
		drag.style.left = -50;
        drag.style.top  = -50;
          
        // -- IE5/Mac requires this so the drag element doesn't take up full screen width
        //drag.innerHTML = '';

        // -- Stop capturing mousemove & mouseup events.
        document.onmousemove = null;
        document.onmouseup   = null;
      }
      
      	function xGetElementById(e) 
	{
		if(typeof(e)!='string') return e;
		if(document.getElementById) e=document.getElementById(e);
	  	else if(document.all) e=document.all[e];
	  	else if(document.layers) e=xLayer(e);
	  	else e=null;
	  	return e;
	}
	
	function xName(e) 
	{
	  	if (!e) return e;
	  	else if (e.id && e.id != "") return e.id;
	  	else if (e.nodeName && e.nodeName != "") return e.nodeName;
	  	else if (e.tagName && e.tagName != "") return e.tagName;
	  	else return e;
	}
	
	// Event:
	function xAddEventListener(e, eventType, eventListener, useCapture) 
	{
	  	if(!(e=xGetElementById(e))) return;
	  	eventType=eventType.toLowerCase();
	  	if((!xIE4Up && !xOp7) && e==window) 
	  	{
	    	if(eventType=='resize') { window.xPCW=xClientWidth(); window.xPCH=xClientHeight(); window.xREL=eventListener; xResizeEvent(); return; }
			if(eventType=='scroll') { window.xPSL=xScrollLeft(); window.xPST=xScrollTop(); window.xSEL=eventListener; xScrollEvent(); return; }
	  	}
	  	
	  	var eh='e.on'+eventType+'=eventListener';
	  	if(e.addEventListener) e.addEventListener(eventType,eventListener,useCapture);
	  	else if(e.attachEvent) e.attachEvent('on'+eventType,eventListener);
	  	else if(e.captureEvents) {
	    	if(useCapture||(eventType.indexOf('mousemove')!=-1)) { e.captureEvents(eval('Event.'+eventType.toUpperCase())); }
	    	eval(eh);
	  	}
	  	else eval(eh);
	}
		
		
	function initializeSlotEventHandler(id, slotId, slotContentIdVar, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, originalUrl)
	{
		//alert("initializeSlotEventHandler:" + id + ":" + slotId);
		var object = new emptySlotEventHandler(id, id, slotId, slotContentIdVar, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, originalUrl);
	}

	function emptySlotEventHandler(eleId, objName, slotId, slotContentIdVar, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, originalUrl)
	{
		this.objName = objName;           // objName is a property of myObject4
		this.repositoryId = repositoryId;
		this.siteNodeId = siteNodeId;
		this.languageId = languageId;
		this.contentId = contentId;
		this.componentId = componentId;
		this.componentContentId = componentContentId;
		this.slotId = slotId;
		this.originalUrl = originalUrl;
		//alert("slotId:" + slotId);
		//alert("eleId:" + eleId);
		//alert("this.insertUrl:" + this.insertUrl);
		var ele = xGetElementById(eleId); // ele points to our related Element
		//alert("ele:" + ele);
		ele.thisObj = this;              // Add a property to ele which points
		                                    // to our myObject4 'this'.
		ele.onclick = function(e)         // onclick is a method of ele not myObject4
		{   
		    this.thisObj.onClick(e, this);
		}
		  
		ele.oncontextmenu = function(e)         	// onclick is a method of ele not myObject4
		{ 
		  	this.thisObj.onContextMenu(e, this);
		    return false;
		}
		
		ele.onmouseup = function(e)         	// onclick is a method of ele not myObject4
		{ 
		  	this.thisObj.onMouseUp(e, this);
		    return false;
		}
		
		
		this.onClick = function(evt, ele) // onClick is a method of myObject4
		{
			//alert('emptySlotEventHandler.onClick()\nthis.objName = ' + this.objName + '\nele = ' + xName(ele));
		    hidepreviousmenues();
		    // cancel event bubbling
   			//alert("5");

		    if (evt && evt.stopPropagation) {evt.stopPropagation();}
		    else if (window.event) {window.event.cancelBubble = true;}
		}
		  
		this.onContextMenu = function(evt, ele) // onContextMenu is a method of myObject4
		{
			//alert('emptySlotEventHandler.oncontextmenu()\nthis.objName = ' + this.objName + '\nele = ' + xName(ele));
		    //showEmptySlotMenu(slotId, evt, ele.id, insertUrl, slotContentIdVar);
						
		    showComponentTasks('componentTasks', 'repositoryId=' + repositoryId + '&siteNodeId=' + siteNodeId + '&languageId=' + languageId + '&contentId=' + contentId + '&componentId=' + componentId + '&componentContentId=' + componentContentId + '&slotId=' + slotId + '&showSimple=false&showLegend=false&slotClicked=true&originalUrl=' + this.originalUrl, false, evt);
			
		    // cancel event bubbling
   			//alert("6");

		    if (evt && evt.stopPropagation) {evt.stopPropagation();}
		    else if (window.event) {window.event.cancelBubble = true;}
		}

		this.onMouseUp = function(evt, ele) // onContextMenu is a method of myObject4
		{
			//alert("onMouseUp...:" + eleId + " - " + assignComponentCatched);
			if(assignComponentCatched)
				return false;
			
			//alert("onMouseUp...:" + eleId);
			
			var hasClassSlotPosition = $(ele).hasClass("slotPosition");
			var hasClassDropArea = $(ele).hasClass("dropArea");
    		$("#debugDiv").html("Testing debug:" + eleId + " - " + hasClassSlotPosition + " " + hasClassDropArea);
    		if(!hasClassSlotPosition)
    		{
    			//alert("Not allowed:" + eleId);
    			return;
    		}
    		//alert("Allowed:" + eleId);
    		assignComponentCatched = true;
			
			//assignComponent(e, siteNodeId, languageId, contentId, parentComponentId, slotId, specifyBaseTemplate, allowedComponentNamesUrlEncodedString, disallowedComponentNamesUrlEncodedString, allowedComponentGroupNamesAsUrlEncodedString, slotPositionComponentId) 

			$("#debugDiv").html("Assigning to:" + componentId + " with " + slotId + ":" + ":" + eleId);

			//alert("draggedComponentId:"+ draggedComponentId + " - " + movedElementId + " - " + movedComponentId);
			if(draggedComponentId != '-1' || movedElementId != '' || movedComponentId != '')
			{
				assignComponent(evt, siteNodeId, languageId, contentId, componentId, slotId, false, '', '', '', '');
	
			    // cancel event bubbling
			    if (evt && evt.stopPropagation) {evt.stopPropagation();}
	   			else if (window.event) {window.event.cancelBubble = true;}
			}
		}
	}
	
	function initializeComponentEventHandler(id, compId, slotId, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, originalUrl)
	{
		//alert("initializeComponentEventHandler" + id + " " + deleteUrl);
		var object = new componentEventHandler(id, id, compId, slotId, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, originalUrl);
	}
		
	function componentEventHandler(eleId, objName, objId, slotId, repositoryId, siteNodeId, languageId, contentId, componentId, componentContentId, originalUrl)
	{		
		this.objName = objName;           // objName is a property of myObject4
		this.objId = objId;
		this.repositoryId = repositoryId;
		this.siteNodeId = siteNodeId;
		this.languageId = languageId;
		this.contentId = contentId;
		this.componentId = componentId;
		this.componentContentId = componentContentId;
		this.slotId = slotId;
		this.originalUrl = originalUrl;
		//alert("eleId:" + eleId);
		//alert("objName:" + objName);
		//alert("objId:" + objId);
		var ele = xGetElementById(eleId); // ele points to our related Element
		//alert("ele:" + ele);
		ele.thisObj = this;              // Add a property to ele which points
		                                    // to our myObject4 'this'.
		ele.onclick = function(e)         // onclick is a method of ele not myObject4
		{   
		  	//alert("onclick");                              // so 'this' will point to event.currentTarget.
		    this.thisObj.onClick(e, this);
		}
		  
		ele.oncontextmenu = function(e)         	// onclick is a method of ele not myObject4
		{ 
		  	//alert("oncontextmenu:" + e);           		// so 'this' will point to event.currentTarget.
		    this.thisObj.onContextMenu(e, this);
		    return false;
		}
		
		this.onClick = function(evt, ele) // onClick is a method of myObject4
		{
			var hasClass = $(ele).hasClass("disablePropertyDialogOnClick");
    		$("#debugDiv").html("Onclick:" + ele.id + " - " + hasClass);
    		if(hasClass)
    			return;

			//alert('componentEventHandler.onClick()\nthis.objName = ' + this.objName + '\nele = ' + xName(ele));
		    hidepreviousmenues();
		    
		    /*
		    var alternateComponentPropertiesDiv = document.getElementById("alternateComponentPropertiesDiv");
			//alert("alternateComponentPropertiesDiv:" + alternateComponentPropertiesDiv);
			
			//alert("skipComponentPropertiesLoad:" + skipComponentPropertiesLoad);
			if(alternateComponentPropertiesDiv && !skipComponentPropertiesLoad)
			{
				var parameters = "repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&componentContentId=" + componentContentId + "&showSimple=false&showLegend=true&originalUrl=" + originalUrl;
				//alert("parameters:" + parameters);
				showComponentInDiv('alternateComponentPropertiesDiv', parameters, true);
			}
			*/
			skipComponentPropertiesLoad = false;
			//alert("skipComponentPropertiesLoad:" + skipComponentPropertiesLoad);

		    // cancel event bubbling
   			//alert("8");

			if (evt && evt.stopPropagation) {evt.stopPropagation();}
			else if (window.event) {window.event.cancelBubble = true;}
		}
		  
		this.onContextMenu = function(evt, ele) // onContextMenu is a method of myObject4
		{
			//alert('componentEventHandler.oncontextmenu()\nthis.objName = ' + this.objName + '\nele = ' + xName(ele) + '\ncomponentId = ' + this.componentId);
		    showComponentTasks('componentTasks', 'repositoryId=' + repositoryId + '&siteNodeId=' + siteNodeId + '&languageId=' + languageId + '&contentId=' + contentId + '&componentId=' + componentId + '&componentContentId=' + componentContentId + '&slotId=' + slotId + '&showSimple=false&showLegend=false&slotClicked=false&originalUrl=' + this.originalUrl, false, evt);
		    
		    // cancel event bubbling
   			//alert("9");

		    if (evt && evt.stopPropagation) {evt.stopPropagation();}
		    else if (window.event) {window.event.cancelBubble = true;}
		}
	}
	
	function initializeComponentInTreeEventHandler(id, compId, insertUrl, deleteUrl, changeUrl, slotId, slotContentIdVar)
	{
		//alert("initializeComponentInTreeEventHandler" + id + " " + deleteUrl + " " + slotId);
		var object = new componentInTreeEventHandler(id, id, compId, insertUrl, deleteUrl, changeUrl, slotId, slotContentIdVar);
	}
		
	function componentInTreeEventHandler(eleId, objName, objId, insertUrl, deleteUrl, changeUrl, slotId, slotContentIdVar)
	{
		this.objName = objName;           // objName is a property of myObject4
		this.objId = objId;
		this.insertUrl = insertUrl;
		this.deleteUrl = deleteUrl;
		this.changeUrl = changeUrl;
		//alert("eleId:" + eleId);
		//alert("objName:" + objName);
		//alert("objId:" + objId);
		//alert("this.insertUrl:" + this.insertUrl);
		//alert("this.deleteUrl:" + this.deleteUrl);
		//alert("this.changeUrl:" + this.changeUrl);
		var ele = xGetElementById(eleId); // ele points to our related Element
		//alert("ele:" + ele);
		ele.thisObj = this;              // Add a property to ele which points
		                                    // to our myObject4 'this'.
		ele.onclick = function(e)         // onclick is a method of ele not myObject4
		{   
		  	//alert("onclick");                              // so 'this' will point to event.currentTarget.
		    this.thisObj.onClick(e, this);
		}
		  
		ele.oncontextmenu = function(e)         	// onclick is a method of ele not myObject4
		{ 
		  	//alert("oncontextmenu");           		// so 'this' will point to event.currentTarget.
		    this.thisObj.onContextMenu(e, this);
		    return false;
		}
		
		this.onClick = function(evt, ele) // onClick is a method of myObject4
		{
			//alert('componentEventHandler.onClick()\nthis.objName = ' + this.objName + '\nele = ' + xName(ele));
		    // cancel event bubbling
   			//alert("10");

		    if (evt && evt.stopPropagation) {evt.stopPropagation();}
		    else if (window.event) {window.event.cancelBubble = true;}
		}
		  
		this.onContextMenu = function(evt, ele) // onContextMenu is a method of myObject4
		{
			//alert('componentEventHandler.oncontextmenu()\nthis.objName = ' + this.objName + '\nele = ' + xName(ele));
		    showComponentInTreeMenu(evt, ele.id, this.objId, insertUrl, deleteUrl, changeUrl, slotId, slotContentIdVar);
		    // cancel event bubbling
   			
			if (evt && evt.stopPropagation) {evt.stopPropagation();}
		    else if (window.event) {window.event.cancelBubble = true;}
		}
	}


	//var currentGroup = "Navigation";
	
	function changeTab(group)
	{
		//alert("group" + group);
		//alert("currentGroup" + currentGroup);
		
		document.getElementById(currentGroup + "Tab").style.zIndex = 2;
		document.getElementById(currentGroup + "Tab").className = "tab";
		document.getElementById(currentGroup + "ComponentsBg").style.zIndex = 2;
		
		document.getElementById(group + "Tab").style.zIndex = 3;
		document.getElementById(group + "Tab").className = "thistab";
		document.getElementById(group + "ComponentsBg").style.zIndex = 3;

		currentGroup = group;
		currentComponentsDiv = currentGroup + "Components";
	}
	
	//The code below is to take care of scroll in tabs
	
	function lib_bwcheck(){ //Browsercheck (needed)
		this.ver=navigator.appVersion
		this.agent=navigator.userAgent
		this.dom=document.getElementById?1:0
		this.opera5=this.agent.indexOf("Opera 5")>-1
		this.ie5=(this.ver.indexOf("MSIE 5")>-1 && this.dom && !this.opera5)?1:0; 
		this.ie6=(this.ver.indexOf("MSIE 6")>-1 && this.dom && !this.opera5)?1:0;
		this.ie4=(document.all && !this.dom && !this.opera5)?1:0;
		this.ie=this.ie4||this.ie5||this.ie6
		this.mac=this.agent.indexOf("Mac")>-1
		this.ns6=(this.dom && parseInt(this.ver) >= 5) ?1:0; 
		this.ns4=(document.layers && !this.dom)?1:0;
		this.bw=(this.ie6 || this.ie5 || this.ie4 || this.ns4 || this.ns6 || this.opera5)
		return this
	}
	var bw=new lib_bwcheck()
	
	
	/**************************************************************************
	Variables to set.
	***************************************************************************/
	var sMenuheight = 20  //The height of the menu
	var sArrowwidth = 5  //Width of the arrows
	var sScrollspeed = 20 //Scroll speed: (in milliseconds, change this one and the next variable to change the speed)
	var sScrollPx = 8     //Pixels to scroll per timeout.
	var sScrollExtra = 15 //Extra speed to scroll onmousedown (pixels)
	
	var scrollHash = new Array();
	
	/**************************************************************************
	Scrolling functions
	***************************************************************************/
	var tim = 0
	var noScroll = true
	function mLeft(){
		id = currentComponentsDiv;
		div = document.getElementById(id);
		
		oBg = scrollHash[id + "oBg"]
		oMenu = scrollHash[id + "oMenu"]
	
		pageWidth = (bw.ns4 || bw.ns6 || window.opera)?innerWidth:document.body.clientWidth;
	
		if (!noScroll && oMenu.x<0){
			oMenu.moveBy(sScrollPx,0)
			tim = setTimeout("mLeft()",sScrollspeed)
		}
	}
	function mRight(){
		id = currentComponentsDiv;
		div = document.getElementById(id);
		
		oBg = scrollHash[id + "oBg"]
		oMenu = scrollHash[id + "oMenu"]
		
		pageWidth = (bw.ns4 || bw.ns6 || window.opera)?innerWidth:document.body.clientWidth;
				
		if (!noScroll && oMenu.x>-(oMenu.scrollWidth-(pageWidth))-sArrowwidth){
			oMenu.moveBy(-sScrollPx,0)
			tim = setTimeout("mRight()",sScrollspeed)
		}
	}
	function noMove(){	
		clearTimeout(tim);
		noScroll = true;
		sScrollPx = sScrollPxOriginal;
	}
	/**************************************************************************
	Object part
	***************************************************************************/
	function makeObj(obj,nest,menu){
	    nest = (!nest) ? "":'document.'+nest+'.';
		this.elm = bw.ns4?eval(nest+"document.layers." +obj):bw.ie4?document.all[obj]:document.getElementById(obj);
	   	this.css = bw.ns4?this.elm:this.elm.style;
		this.scrollWidth = bw.ns4?this.css.document.width:this.elm.offsetWidth;
		this.x = bw.ns4?this.css.left:this.elm.offsetLeft;
		this.y = bw.ns4?this.css.top:this.elm.offsetTop;
		this.moveBy = b_moveBy;
		this.moveIt = b_moveIt;
		this.clipTo = b_clipTo;
		return this;
	}
	var px = bw.ns4||window.opera?"":"px";
	function b_moveIt(x,y){
		if (x!=null){this.x=x; this.css.left=this.x+px;}
		if (y!=null){this.y=y; this.css.top=this.y+px;}
	}
	function b_moveBy(x,y){this.x=this.x+x; this.y=this.y+y; this.css.left=this.x+px; this.css.top=this.y+px;}
	function b_clipTo(t,r,b,l){
		if(bw.ns4){this.css.clip.top=t; this.css.clip.right=r; this.css.clip.bottom=b; this.css.clip.left=l;}
		else this.css.clip="rect("+t+"px "+r+"px "+b+"px "+l+"px)";
	}
	/**************************************************************************
	Object part end
	***************************************************************************/
	
	/**************************************************************************
	Init function. Set the placements of the objects here.
	***************************************************************************/
	var sScrollPxOriginal = sScrollPx;
	function tabInit(id)
	{
		div = document.getElementById(id);
		
		//Width of the menu, Currently set to the width of the document.
		//If you want the menu to be 500px wide for instance, just 
		//set the pageWidth=500 in stead.
		pageWidth = (bw.ns4 || bw.ns6 || window.opera)?innerWidth:document.body.clientWidth;
		
		//Making the objects...
		oBg = new makeObj(id + 'Bg')
		oMenu = new makeObj(id,id + 'Bg',1)
		
		//Storing them for later
		scrollHash[id + "oBg"] = oBg;
		scrollHash[id + "oMenu"] = oMenu;
		
		//Placing the menucontainer, the layer with links, and the right arrow.
		oBg.moveIt(0,40) //Main div, holds all the other divs.
		oMenu.moveIt(0,null)
		
		//Setting the width and the visible area of the links.
		if (!bw.ns4) oBg.css.overflow = "hidden";
		if (bw.ns6) oMenu.css.position = "relative";
		//oBg.css.width = pageWidth+px;
		//oBg.clipTo(0,pageWidth,sMenuheight,0)
		//oBg.css.visibility = "visible";
		
	}
	
	/**
	 * This method sets the status text in the list - showing off the full name of the component.
	 */
	 
	function showDetails(name)
	{
		statusDiv = document.getElementById("statusText");
		statusDivText=statusDiv.childNodes.item(0);
		statusDivText.data = name;
	} 
	
	
	//*******************************************
	// This function changes language version
	//*******************************************
	
	function changeLanguage(siteNodeId, languageId, contentId)
	{
		window.location.href = "ViewPage!renderDecoratedPage.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId.value + "&contentId=" + contentId + "";
	}
	
	//*******************************************
	// This function changes language version
	//*******************************************
	
	function refreshComponents(currentLocation)
	{
		var newLocation = currentLocation + "&refresh=true";
		//alert("newLocation" + newLocation);
		document.location.href = newLocation;
	}

	var groupHash = new Array();
	
	//
	// QueryString
	//
	
	function QueryString(key)
	{
		var value = null;
		for (var i=0;i<QueryString.keys.length;i++)
		{
			if (QueryString.keys[i]==key)
			{
				value = QueryString.values[i];
				break;
			}
		}
		return value;
	}
	
	QueryString.keys = new Array();
	QueryString.values = new Array();
	
	function QueryString_Parse()
	{
		var query = window.location.search.substring(1);
		var pairs = query.split("&");
		
		for (var i=0;i<pairs.length;i++)
		{
			var pos = pairs[i].indexOf('=');
			if (pos >= 0)
			{
				var argname = pairs[i].substring(0,pos);
				var value = pairs[i].substring(pos+1);
				QueryString.keys[QueryString.keys.length] = argname;
				QueryString.values[QueryString.values.length] = value;		
			}
		}
	
	}
	
	QueryString_Parse();
	
	var dirty = false;
	function setDirty()
	{
		dirty = true;
	}
	
	function checkDirty(warningText)
	{
		if(dirty)
		{
			var r = confirm(warningText)
			if(r==true)
			{
				return true;
			}
			else
			{
			    return false;
			}
		}
		else
		{
			return true;
		}
	}
	
	var previousIGMenuId;

	function showIGMenu(id, event)
	{
		//alert("event:" + event);
		if(!event)
			event = window.event;
			
		hideIGMenu();
		
		var currentMenuDiv = document.getElementById(id);	    
	    document.body.onclick = hideIGMenu;
		
		clientX = getEventPositionX(event);
		clientY = getEventPositionY(event);
		
		//alert("clientX:" + clientX);
		//alert("clientY:" + clientY);
		
		var rightedge = document.body.clientWidth - clientX;
		var bottomedge = getWindowHeight() - clientY;
	
		//alert("rightedge:" + rightedge);
		//alert("bottomedge:" + bottomedge);
	
		currentMenuDiv.style.display = 'block';
		currentMenuDiv.style.visibility = 'hidden';
	
		//alert("currentMenuDiv.offsetWidth:" + currentMenuDiv.offsetWidth);
		//alert("currentMenuDiv.offsetHeight:" + currentMenuDiv.offsetHeight);
			
		if (rightedge < currentMenuDiv.offsetWidth)
			clientX = (clientX - currentMenuDiv.offsetWidth + 5);
		
		if (bottomedge < currentMenuDiv.offsetHeight && (clientY - currentMenuDiv.offsetHeight > 0))
			clientY = (clientY - currentMenuDiv.offsetHeight + 5);
			
		currentMenuDiv.style.left 	= clientX + "px";
		currentMenuDiv.style.top 	= clientY + "px";
	
		currentMenuDiv.style.visibility = 'visible';
		
		previousIGMenuId = id;
		
		return false;
	}
	
	function hideIGMenu()
	{
		//alert("previousIGMenuId:" + previousIGMenuId);
		if(previousIGMenuId && previousIGMenuId != '')
		{
			var element = document.getElementById(previousIGMenuId);
			if(element)
				element.style.display = 'none';
		}   
		else if(activeMenuId && activeMenuId != '')
		{
			var element = document.getElementById(activeMenuId);
			if(element)
				element.style.display = 'none';
		}
	}
	
	var previousEditOnSightMenuDivId = ''; 
	function closeDialog()
	{
		tb_remove();
		if(previousEditOnSightMenuDivId != '')
			openCloseDiv(previousEditOnSightMenuDivId);
	}
	
	function registerOnMouseUp(elementId, siteNodeId, languageId, contentId, componentId, id, skipFloatDiv, allowedComponentsArrayAsUrlEncodedString, disallowedComponentsArrayAsUrlEncodedString, allowedComponentGroupNamesAsUrlEncodedString, slotPositionComponentId)
	{
		/*
		alert("elementId:" + elementId);
		alert("siteNodeId:" + siteNodeId);
		alert("languageId:" + languageId);
		alert("contentId:" + contentId);
		alert("componentId:" + componentId);
		alert("id:" + id);
		alert("skipFloatDiv:" + skipFloatDiv);
		alert("allowedComponentsArrayAsUrlEncodedString:" + allowedComponentsArrayAsUrlEncodedString);
		alert("disallowedComponentsArrayAsUrlEncodedString:" + disallowedComponentsArrayAsUrlEncodedString);
		*/
		var element = document.getElementById(elementId);
		var insertMarker = "<span id='ghost' style='background-color: #0070da; display: table; clear: both; height: 2px; width: 100%;'></span>";

	   	if (document.addEventListener != null)
	   	{
	   		element.addEventListener("mouseover", function(event) { 
	   		
	    		//$("#debugDiv").html("Testing debug:" + elementId + " - " + draggedComponentId + " - " + movedComponentId + " - " + movedElementId);

    			var hasClass = $(element).hasClass("slotPosition");
	    		$("#debugDiv").html("Testing debug:" + elementId + " - " + hasClass);
	    		if(!hasClass)
	    			return;
	    		
	   			if(clearMovedComponent)
	   			{
	   				draggedComponentId = -1;
					movedComponentId = "";
					movedElementId = "";
					clearMovedComponent = false;
				}
								
				$("#debugDiv").html("draggedComponentId:" + draggedComponentId);

	   			if(movedComponentId && movedComponentId != "") 
	   			{
		   			var draggedElement = document.getElementById(movedElementId);
		   			var height = $(draggedElement).height();
	   				
	   				var targ;
					if (!event) 
						event = window.event;
						
					if (event.target) 
						targ = event.target;
					else if (event.srcElement) 
						targ = event.srcElement;
					if (targ.nodeType == 3) // defeat Safari bug
						targ = targ.parentNode;
			   		
			   		if(targ.id != movedElementId)
			   		{
		   				$("#ghost").remove();
		   				$(element).prepend(insertMarker);
		   			}
	   			}
	   			else if(draggedComponentId && draggedComponentId != "" && draggedComponentId != '-1') 
	   			{
	   				var draggedElement = document.getElementById(draggedComponentId);
		   			var height = $(draggedElement).height();
	   				
	   				var targ;
					if (!event) 
						event = window.event;
						
					if (event.target) 
						targ = event.target;
					else if (event.srcElement) 
						targ = event.srcElement;
					if (targ.nodeType == 3) // defeat Safari bug
						targ = targ.parentNode;
			   		
					$("#debugDiv").html("AAAA:" + targ.id + " - " + movedElementId);
			   		if(targ.id != draggedComponentId)
			   		{
		   				$("#ghost").remove();
		   				$(element).prepend(insertMarker);
		   			}
	   			} 

	   			if (event && event.stopPropagation) {event.stopPropagation();}
    			else if (window.event) {window.event.cancelBubble = true;}	
	   		}, false);
	    	element.addEventListener("mouseup", function (event){ 
	    		
	    		var hasClass = $(element).hasClass("slotPosition");
	    		$("#debugDiv").html("Testing debug:" + elementId + " - " + hasClass);
	    		if(!hasClass /*|| assignComponentCatched*/)
	    		{
	    			//alert("Not allowed:" + elementId + hasClass + assignComponentCatched);
	    			return;
	    		}

	    		assignComponentCatched = true;
				$("#debugDiv").html("Assigning to:" + componentId + " with " + siteNodeId + ":" + languageId + ":" +contentId + ":" + componentId + ":" + id);
				if(draggedComponentId != '-1' || movedElementId != '' || movedComponentId != '')
					assignComponent(event, siteNodeId, languageId, contentId, componentId, id, skipFloatDiv, allowedComponentsArrayAsUrlEncodedString, disallowedComponentsArrayAsUrlEncodedString, allowedComponentGroupNamesAsUrlEncodedString, slotPositionComponentId);
	    	}, false);
	    }
	    else
	    {
	    	element.attachEvent("onmouseover", function (evt) { 
	    	
	    		$("#debugDiv").html("Testing debug:" + draggedComponentId + " - " + movedComponentId + " - " + movedElementId);

    			var hasClass = $(element).hasClass("slotPosition");
	    		$("#debugDiv").html("Testing debug:" + elementId + " - " + hasClass);
	    		if(!hasClass)
	    			return;
	    		
	   			//alert("clearMovedComponent:" + clearMovedComponent);
	   			if(clearMovedComponent)
	   			{
	   				draggedComponentId = -1;
					movedComponentId = "";
					movedElementId = "";
					clearMovedComponent = false;
				}
	    		
	    		if(movedComponentId && movedComponentId != "") 
	   			{
		   			var draggedElement = document.getElementById(movedElementId);
	   				
	   				var targ;
					if (!evt) 
						evt = window.event;
						
					if (evt.target) 
						targ = evt.target;
					else if (evt.srcElement) 
						targ = evt.srcElement;
					if (targ.nodeType == 3) // defeat Safari bug
						targ = targ.parentNode;

		    		//$("#debugDiv").html("<p>Mouse over: " + element.id + "<br/>Event:" + evt + "<br/>Target.id: " + targ.id + "<br/>movedComponentId:" + movedComponentId + "</p>");
		    		
		    		//$("#debugDiv").html("Testing debug:" + draggedComponentId + " - " + movedComponentId + " - " + movedElementId + " - " + evt + ":" + targ.id + ":" + evt.target + ":" + evt.srcElement.id);
			   		
			   		if(targ.id != movedElementId)
			   		{
		   				$("#ghost").remove();
		   				$(element).prepend(insertMarker);
		   			}
	   			} 
	   			else if(draggedComponentId && draggedComponentId != "" && draggedComponentId != '-1') 
	   			{
		   			var draggedElement = document.getElementById(draggedComponentId);
	   				
	   				var targ;
					if (!evt) 
						evt = window.event;
						
					if (evt.target) 
						targ = evt.target;
					else if (evt.srcElement) 
						targ = evt.srcElement;
					if (targ.nodeType == 3) // defeat Safari bug
						targ = targ.parentNode;
			   		
			   		if(targ.id != draggedComponentId)
			   		{
		   				$("#ghost").remove();
		   				$(element).prepend(insertMarker);
		   			}
	   			} 

	   		    if (event && event.stopPropagation) {event.stopPropagation();}
    			else if (window.event) {window.event.cancelBubble = true;}	
	   		});
	    	
	    	element.attachEvent("onmouseup", function (evt) {
	    		
				var hasClass = $(element).hasClass("slotPosition");
	    		$("#debugDiv").html("Testing debug:" + elementId + " - " + hasClass);
	    		if(!hasClass)
	    		{
	    			//alert("Not allowed:" + elementId);
	    			return;
	    		}
	    		
	    		if(draggedComponentId != '-1' || movedElementId != '' || movedComponentId != '')
					assignComponent(evt, siteNodeId, languageId, contentId, componentId, id, skipFloatDiv, allowedComponentsArrayAsUrlEncodedString, disallowedComponentsArrayAsUrlEncodedString, allowedComponentGroupNamesAsUrlEncodedString, slotPositionComponentId);
	   		})
	    }
	}	
	
	function escapeAmpsAndQuotes(original)
	{
		var escapedString = original;
		escapedString = escapedString.replace(/\'/g,"&#39;");
		escapedString = escapedString.replace(/"/g,"&#34;");
		return escapedString;
	}
