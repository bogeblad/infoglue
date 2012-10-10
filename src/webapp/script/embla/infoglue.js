//-----------------------------------------------
// Dirty handling in forms 
//-----------------------------------------------

var dirty = false;

function setDirty()
{
	dirty=true;
}
function getDirty()
{
	return dirty;
}

function getRequestParameter(url, parameterName)
{
	valueIndex = url.indexOf(parameterName + "=");
	endIndex = url.indexOf("&", valueIndex);
	if(endIndex != -1)
		value = url.substring(valueIndex + parameterName.length + 1, endIndex);
	else
		value = url.substring(valueIndex + parameterName.length + 1);
	return value;
}

//returns the scroll left and top for the browser viewport.
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

function resizeInlineTabDivsImproved()
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
  	if(dimensionsWidth != 0)
  	{
		$(".inlineTabDiv").height(dimensionsHeight - 160);
  		$(".inlineTabDiv").width(dimensionsWidth - 16);
		$(".inlineSubTabDiv").height(dimensionsHeight - 220);
		$(".inlineSubTabDiv").width(dimensionsHeight - 40);
		$(".inlineTabDiv > iframe").height(dimensionsHeight - 170).width(dimensionsWidth - 16);
		$(".inlineSubTabDiv > iframe").width(dimensionsWidth - 60);
	}
	else
	{
		setTimeout("resizeInlineTabDivs()", 300);
	}
}

function resizeInlineTabDivs()
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
  	if(dimensionsWidth != 0)
  	{
		$(".inlineTabDiv").css("height", dimensionsHeight - 160);
  		$(".inlineTabDiv").css("width", dimensionsWidth - 30);
		$(".inlineSubTabDiv").css("height", dimensionsHeight - 220);
		$(".inlineSubTabDiv").css("width", dimensionsHeight - 40);
		$(".inlineTabDiv > iframe").css("width", dimensionsWidth - 50);
		$(".inlineSubTabDiv > iframe").css("width", dimensionsWidth - 60);
	}
	else
	{
		setTimeout("resizeInlineTabDivs()", 300);
	}
}

function resizeResizableDiv(heightMinus, widthMinus)
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
	//alert("dimensionsHeight:" + dimensionsHeight);
  	if(dimensionsWidth != 0)
  	{
		$(".resizableDiv").height(dimensionsHeight - heightMinus).width(dimensionsWidth - widthMinus);
	}
	else
	{
		setTimeout("resizeResizableDiv(" + heightMinus + "," + widthMinus + ")", 300);
	}
}

function resizeInlineTabDivsOnOffset(elementId)
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
  	//alert("dimensionsHeight:" + dimensionsHeight);
  	if(dimensionsWidth != 0)
  	{
  		var elemTop = $("#" + elementId).offset().top;
  		//alert("elemTop on " + $(element).attr("id") + ":" + elemTop);
  		
  		$("#" + elementId).height(dimensionsHeight - elemTop).width(dimensionsWidth);
  		$(".inlineTabDiv > iframe").height(dimensionsHeight - elemTop).width(dimensionsWidth);
  		$(".inlineTabDiv > div > iframe").height(dimensionsHeight - elemTop).width(dimensionsWidth);
	}
	else
	{
		setTimeout("resizeInlineTabDivsOnOffset('" + elementId + "')", 500);
	}
}

function resizeInlineTabDivsFullOnPosition(elementId)
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
  	//alert("dimensionsHeight:" + dimensionsHeight);
  	if(dimensionsWidth != 0)
  	{
  		var elemTop = $("#" + elementId).offset().top;
  		//alert("elemTop on " + $(element).attr("id") + ":" + elemTop);
		
		var elemHeightFooter = $("#footertoolbar").height();
  		
  		var positionIframeTop = 0;
  		var positionIframeLeft = 0;
  		var iframes = $(".inlineTabDiv > div > iframe");
  		var iframesAlt = $(".inlineTabDiv > iframe");
  		if(iframes.size() > 0)
  		{
  			var marginsHeight = parseInt(iframes.parent().css("margin-top")) + parseInt(iframes.parent().css("margin-bottom"));
  			var marginsWidth = parseInt(iframes.parent().css("margin-left")) + parseInt(iframes.parent().css("margin-right"));
  	  		positionIframeTop = iframes.position().top + marginsHeight;
  	  		positionIframeLeft = iframes.offset().left + marginsWidth;
 		}
  		else if(iframesAlt.size() > 0)
  		{
  			var marginsHeight = parseInt(iframesAlt.parent().css("margin-top")) + parseInt(iframesAlt.parent().css("margin-bottom"));
 			var marginsWidth = parseInt(iframesAlt.parent().css("margin-left")) + parseInt(iframesAlt.parent().css("margin-right"));
  	  		positionIframeTop = iframesAlt.position().top + marginsHeight;
  	  		positionIframeLeft = iframesAlt.offset().left + marginsWidth;
  		}

  		var iframeHeight = dimensionsHeight - elemTop - elemHeightFooter - positionIframeTop;
  		
  		$("#" + elementId).height(dimensionsHeight - elemTop - elemHeightFooter); //.width(dimensionsWidth - positionIframeLeft);
  		$(".inlineTabDiv > iframe").height(iframeHeight).width(dimensionsWidth - positionIframeLeft);
  		$(".inlineTabDiv > div > iframe").height(iframeHeight).width(dimensionsWidth - positionIframeLeft);
  		
  		$(".inlineTabDiv iframe iframe").height(iframeHeight);
	}
	else
	{
		setTimeout("resizeInlineTabDivsFullOnPosition('" + elementId + "')", 500);
	}
}
function resizeInlineTabDivsWithoutSubDivs(yMinus, xMinus)
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
  	//alert("dimensionsHeight:" + dimensionsHeight);
  	if(dimensionsWidth != 0)
  	{
		$(".inlineTabDiv").height(dimensionsHeight - yMinus).width(dimensionsWidth - xMinus);
		//$(".inlineTabDiv > iframe").width($(".inlineTabDiv").width());
		$(".inlineTabDiv > iframe").height(dimensionsHeight - yMinus).width(dimensionsWidth - xMinus);
	}
	else
	{
		setTimeout("resizeInlineTabDivsWithoutSubDivs(" + yMinus + "," + xMinus + ")", 500);
	}
}

function resizeInlineTabDivsWithMinus(yMinus, xMinus)
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
  	if(dimensionsWidth != 0)
  	{
		$(".inlineTabDiv").css("height", dimensionsHeight - (160 + yMinus));
  		$(".inlineTabDiv").css("width", dimensionsWidth - (30 + xMinus));
		$(".inlineSubTabDiv").css("height", dimensionsHeight - (220 + yMinus));
		$(".inlineSubTabDiv").css("width", dimensionsHeight - (40 + xMinus));
		//$(".inlineTabDiv > iframe").width(dimensionsWidth - (20 + xMinus));
		$(".inlineTabDiv > iframe").width(dimensionsWidth - (50 + xMinus));
		$(".inlineTabDiv > iframe").height(dimensionsHeight - (160 + yMinus));
		$(".inlineSubTabDiv > iframe").width(dimensionsWidth - (60 + xMinus));
		//$(".inlineSubTabDiv > iframe").width(dimensionsWidth - (30 + xMinus));
	}
	else
	{
		setTimeout("resizeInlineTabDivsWithMinus(" + yMinus + "," + xMinus + ")", 300);
	}
}

function resizeInlineTabDivsWithAmount(height, width)
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsWidth:" + dimensionsWidth);
	//alert("height:" + height);
  	if(dimensionsWidth != 0)
  	{
		//$(".inlineTabDiv").css("height", dimensionsHeight - height);
  		//$(".inlineTabDiv").css("width", dimensionsWidth - width);
		//$(".inlineSubTabDiv").css("height", dimensionsHeight - (height + 60));
		//$(".inlineSubTabDiv").css("width", dimensionsHeight - (height + 10));
	}
	else
	{
		setTimeout("resizeInlineTabDivsWithAmount(" + height + "," + width + ")", 500);
	}
}

/*
 * This method will have to be the one handling all the one level inlineTabDivs
 */

function resizeScrollArea()
{
}

/*
 * This method will have to be the one handling all the one level inlineTabDivs
 */
function resizeScrollAreaOld()
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsHeight:" + (dimensionsHeight - 78));
  	if(dimensionsWidth != 0)
  	{
  		var footertoolbarHeight = $("#footertoolbar").height();
  		var menutoolbarHeight = $("#menutoolbar").height();
  		var toolbarHeight = 0 + footertoolbarHeight + menutoolbarHeight;
  		$(".igScrollArea").height(dimensionsHeight - toolbarHeight);
  		
  		var positionIframeTop = 0;
  		var positionIframeLeft = 0;
  		var iframes = $(".igScrollArea > p > iframe");
  		if(iframes.size() > 0)
  		{
  			var marginsHeight = parseInt(iframes.parent().css("margin-top")) + parseInt(iframes.parent().css("margin-bottom"));
  			var marginsWidth = parseInt(iframes.parent().css("margin-left")) + parseInt(iframes.parent().css("margin-right"));
  	  		positionIframeTop = iframes.position().top + marginsHeight;
  	  		positionIframeLeft = iframes.offset().left + marginsWidth;
  	  		
  	  		var iframeHeight = dimensionsHeight - $("#footertoolbar").height() - positionIframeTop;
  	  		$(".igScrollArea > p > iframe").height(iframeHeight); //.width(dimensionsWidth - positionIframeLeft);
 		}
	}
	else
	{
		setTimeout("resizeScrollArea()", 100);
	}
}

function resizeScrollAreaWithAmount(height)
{
	var dimensionsWidth = $(window).width();
	var dimensionsHeight = $(window).height();
  	//alert("dimensionsHeight:" + (dimensionsHeight - height));
  	if(dimensionsWidth != 0)
  	{
		$(".igScrollArea").css("height", dimensionsHeight - height);
		//$(".igScrollArea").css("width", dimensionsWidth);
	}
	else
	{
		setTimeout("resizeScrollAreaWithAmount(" + height + ")", 100);
	}
}

function checkAllBoxes(element)
{
	if(element)
	{
		var length = element.length;
	  	if(length == null)
	  	{
	  		element.checked = true;
	  		rowId = element.getAttribute("rowId");
			listRowMarked(document.getElementById(rowId));
	  	}
	  	else
	  	{	
		 	var field = element;
		 	for (i = 0; i < field.length; i++)
			{
				field[i].checked = true;
				rowId = field[i].getAttribute("rowId");
				listRowMarked(document.getElementById(rowId));
			}
		}
	}
}

function uncheckAllBoxes(element)
{
	if(element)
	{
		var length = element.length;
	  	if(length == null)
	  	{
	  		element.checked = false;
	  		rowId = element.getAttribute("rowId");
			listRowUnMarked(document.getElementById(rowId));
	  	}
	  	else
	  	{	
		 	var field = element;
		 	for (i = 0; i < field.length; i++)
			{
				field[i].checked = false;
				rowId = field[i].getAttribute("rowId");
				listRowUnMarked(document.getElementById(rowId));
			}
		}
	}
}

function listRowMarked(rowEl)
{
	return;
	/*
	if (rowEl.className.slice(0,6) != "marked")
		rowEl.className = "marked"+rowEl.className;
	*/	
	lastRow = rowEl;
}


function listRowUnMarked(rowEl)
{
	var rowClass = rowEl.className;
	if(rowClass.length > 6)
	{
		if (rowClass.slice(0,6) == "marked")
		{
			rowEl.className = rowClass.slice(6, rowClass.length);
		}
	}
			
	lastRow = rowEl;
}

function CheckUncheck(row,chkbox)
{
	var rowEl=document.getElementById(row);
	if (chkbox.checked)
	{
		listRowMarked(rowEl);
	}
	else
	{
		listRowUnMarked(rowEl);		
	}
}

//-----------------------------------------------
//This function opens up a new location in a 
//restriced popup 
//-----------------------------------------------

function openPopup(url, name, details)
{
	newWin=window.open(url, name, details);
	newWin.focus();
}

//-----------------------------------------------
//This function opens up a new location in a 
//restriced popup 
//-----------------------------------------------

function openPopupWithOptionalParameter(url, name, details, question, parameter)
{
	if(confirm(question))
		newWin=window.open(url + "&" + parameter, name, details);
	else
		newWin=window.open(url, name, details);
	
	newWin.focus();
}

//--------------------------------------------------------
//Display a notification div when something has happened
//i.e. a content has been saved or something else has 
//happened that the user needs to be notified about.
//--------------------------------------------------------

function displayNotification(aMessage)
{
	displayNotification(aMessage, "");
}

function displayNotification(aMessage, aClassName)
{
	$("#popupAlertMessageDiv p").html(aMessage);
	$("#popupAlertMessageDiv").attr("class", aClassName);
	$("#popupAlertMessageDiv").css("margin-top", "0");
	setTimeout(function() {
		$("#popupAlertMessageDiv").css("margin-top", "45px");
	}, 4000);
}

function hideNotification()
{
	$("#popupAlertMessageDiv").css("margin-top", "45px");
}
