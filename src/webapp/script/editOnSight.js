var menuskin = "skin1"; // skin0, or skin1
var display_url = 0; // Show URLs in status bar?
var editUrl = "";

if (navigator.appName == "Netscape") {
  document.captureEvents(Event.CLICK);
}

//document.body.onclick = hidemenuie5();

function getEventPositionX(e) 
{
	if (navigator.appName == "Microsoft Internet Explorer")
	{
    	mX = event.clientX;
  	}
  	else 
  	{
    	mX = e.pageX;
  	}
  	
  	return mX;
}

function getEventPositionY(e) 
{
	if (navigator.appName == "Microsoft Internet Explorer")
	{
    	mY = event.clientY;
  	}
  	else 
  	{
    	mY = e.pageY;
  	}
  	
  	return mY;
}

function getMenuDiv() 
{
	return document.getElementById("ie5menu");
}

function setEditUrl(anEditUrl) 
{
	//alert("Setting editUrl:" + anEditUrl);
	editUrl = anEditUrl;
}

function setContentItemParameters(contentId, languageId, attributeName)
{
	alert("Setting contentId wrong:" + contentId);
	//alert("Setting languageId:" + languageId);
	//alert("Setting attributeName:" + attributeName);
	selectedContentId = contentId;
	selectedLanguageId = languageId;
	selectedAttributeName = attributeName;
}

function showmenuie5(anEditUrl, event) 
{
	document.body.onclick = hidemenuie5;
	getMenuDiv().className = menuskin;

	editUrl = anEditUrl;
	clientX = getEventPositionX(event);
	clientY = getEventPositionY(event);
	
	var rightedge = document.body.clientWidth - clientX;
	var bottomedge = document.body.clientHeight - clientY;

	menuDiv = getMenuDiv();
	
	if (rightedge < menuDiv.offsetWidth)
		menuDiv.style.left = (document.body.scrollLeft + clientX - menuDiv.offsetWidth);
	else
		menuDiv.style.left = (document.body.scrollLeft + clientX);
	
	if (bottomedge < menuDiv.offsetHeight)
		menuDiv.style.top = (document.body.scrollTop + clientY - menuDiv.offsetHeight);
	else
		menuDiv.style.top = (document.body.scrollTop + clientY);
	
	menuDiv.style.visibility = "visible";
	
	return false;
}

function hidemenuie5() 
{
	//alert("Hiding menu");
	layer = getMenuDiv();
	layer.style.visibility = "hidden";
}

function highlightie5(event) 
{
	var layer = event.srcElement || event.currentTarget || event.target;
	//alert("layer:" + layer.className);
	
	if (layer.className == "igmenuitems") 
	{
		layer.style.backgroundColor = "highlight";
		layer.style.color = "white";
		
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
		layer.style.color = "black";
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
	}
	else
		return false;
}


//--------------------------------------------
// Here comes the menu items actions
//--------------------------------------------

function openEditWindow() 
{
	//alert("editUrl:" + editUrl);
	details = "'width=500,height=700,left=" + (document.body.clientWidth / 4) + ",top=100,toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=yes'";
	newWin=window.open(editUrl, "Edit", details);
	newWin.focus();
}

function viewSource() 
{
	window.location = "view-source:" + window.location.href;
}