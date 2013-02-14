var getUrl = "ViewMessageCenter!getSystemMessages.action";
var lastId = -1; //initial value will be replaced by the latest known id
window.onload = initJavaScript;

function initJavaScript() 
{
	//document.forms['chatForm'].elements['chatbarText'].setAttribute('autocomplete','off'); //this non standard attribute prevents firefox' autofill function to clash with this script
	receiveSystemMessagesText(); //initiates the first data query
}

//initiates the first data query
function receiveSystemMessagesText() 
{
	if (httpReceiveSystemMessages.readyState == 4 || httpReceiveSystemMessages.readyState == 0) 
	{
	  	httpReceiveSystemMessages.open("GET",getUrl + '?lastId=' + lastId + '&rand='+Math.floor(Math.random() * 1000000), true);
	    httpReceiveSystemMessages.onreadystatechange = handlehHttpReceiveSystemMessages; 
	  	httpReceiveSystemMessages.send(null);
	}
}

//deals with the servers' reply to requesting new content
function handlehHttpReceiveSystemMessages() 
{
  	if (httpReceiveSystemMessages.readyState == 4) 
  	{
  		//alert("message:" + httpReceiveSystemMessages.responseText);
  		if(httpReceiveSystemMessages.responseText != "empty")
  		{
	    	results = httpReceiveSystemMessages.responseText.split('---'); //the fields are seperated by ---
	    
		    if(results.length > 2)
			{
			    i = 0;
			    lastId = results[i];
			    type = results[i+1];
			    extradata = results[i+2];
			
				//alert("lastId:" + lastId);
				if(type != "-1")
					handleMessage(type, extradata);
		
				i = i + 3;
			}	
		}	
	    setTimeout('receiveSystemMessagesText();',30000); //executes the next data query in 30 seconds
  	}
}

//inserts the new content into the page
function handleMessage(type, extradata) 
{
	//alert("Got new message:" + extradata + ":" + type);
	setTimeout(extradata,1000);
}


/**
 * This method close layer.
 */
 
function closeDiv(id)
{
	var frame = parent.frames["toolarea"].frames["main"];
	if(frame)
	{
		frame.contentWindow.document.getElementById(id).style.display = 'none';
	}
}

/**
 * This method opens a layer.
 */

function openDiv(id)
{
	var frame = parent.frames["toolarea"].frames["main"];
	if(frame)
	{
		frame.contentWindow.document.getElementById(id).style.display = 'block';
	}
}

function getDocumentHeight()
{
	var frame = parent.frames["toolarea"].frames["main"];
	if(frame)
	{
		var x,y;
		var test1 = frame.contentWindow.document.body.scrollHeight;
		var test2 = frame.contentWindow.document.body.offsetHeight
		if (test1 > test2) // all but Explorer Mac
		{
			x = frame.contentWindow.document.body.scrollWidth;
			y = frame.contentWindow.document.body.scrollHeight;
		}
		else // Explorer Mac;
		     //would also work in Explorer 6 Strict, Mozilla and Safari
		{
			x = frame.contentWindow.document.body.offsetWidth;
			y = frame.contentWindow.document.body.offsetHeight;
		}
		return y + "px";
	}
	else
		return 0 + "px";
}

function openChat(message)
{
	try
	{
		var frame = parent.frames["toolarea"].frames["main"];
		//alert("frame:" + frame);
		if(frame)
		{
			if(frame.contentWindow.document.getElementById("outputList") || (frame.contentWindow.document.getElementById("systemMessages") && frame.contentWindow.document.getElementById("systemMessages").style.display == 'block'))
			{
				//alert("JA");
			}
			else
			{
				var div = frame.contentWindow.document.getElementById("systemMessagesDialog");
				//alert("div:" + div.innerHTML);	
					
				if(div)
				{				
					var divHTML = "<div id=\"systemMessagesMain\">";
					divHTML = divHTML + "<div id=\"systemMessagesMainHandle\" class=\"systemMessagesDivHandle\">";
					divHTML = divHTML + "<div id=\"systemMessagesDivLeftHandle\" class=\"systemMessagesDivLeftHandle\">System message</div>";
					divHTML = divHTML + "<div id=\"systemMessagesDivRightHandle\" class=\"systemMessagesDivRightHandle\"><a href=\"javascript:closeChat();\" class=\"whitelabel\">close</a></div>";
					divHTML = divHTML + "</div>";
					divHTML = divHTML + "<div id=\"systemMessagesDivBody\" class=\"systemMessagesDivBody\">";
					
					divHTML = divHTML + "<iframe frameborder='0' id='chatIFrame' src='ViewMessageCenter!standaloneChat.action' width='400' height='350' align='baseline' style='width:100%; height=100%; margin: 0px 0px 0px 0px; padding: 0px 0px 0px 0px;'></iframe>";
					divHTML = divHTML + "</div>";
					
					div.innerHTML = divHTML;
					//div.innerHTML = "AAAAAAAAAAAAAAAAAAAAAAAAAAA";
					//alert("div:" + div.innerHTML);
					systemMessagesDiv = frame.contentWindow.document.getElementById("systemMessages");
					systemMessagesDiv.style.height = getDocumentHeight();
					//alert("getDocumentHeight():" + getDocumentHeight());
					openDiv("systemMessages");
					openDiv("systemMessagesDialog");
				}
				else
				{
					alert(message);
				}
				
			}
		}
	}
	catch(err)
	{
		alert("System message: " + message);
	}
}



//initiates the XMLHttpRequest object
//as found here: http://www.webpasties.com/xmlHttpRequest
function getHTTPObject() {
  var xmlhttp;
  /*@cc_on
  @if (@_jscript_version >= 5)
    try {
      xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      try {
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (E) {
        xmlhttp = false;
      }
    }
  @else
  xmlhttp = false;
  @end @*/
  if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
    try {
      xmlhttp = new XMLHttpRequest();
    } catch (e) {
      xmlhttp = false;
    }
  }
  return xmlhttp;
}


// initiates the two objects for sending and receiving data
var httpReceiveSystemMessages = getHTTPObject();