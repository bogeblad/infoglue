var GetChaturl = "ViewMessageCenter!getMessages.action";
var SendChaturl = "ViewMessageCenter!sendMessage.action";
var lastId = -1; //initial value will be replaced by the latest known id
window.onload = initJavaScript;

function initJavaScript() {
	//document.forms['chatForm'].elements['chatbarText'].setAttribute('autocomplete','off'); //this non standard attribute prevents firefox' autofill function to clash with this script
	receiveChatText(); //initiates the first data query
}

//initiates the first data query
function receiveChatText() {
	if (httpReceiveChat.readyState == 4 || httpReceiveChat.readyState == 0) {
  	httpReceiveChat.open("GET",GetChaturl + '?lastId=' + lastId + '&rand='+Math.floor(Math.random() * 1000000), true);
    httpReceiveChat.onreadystatechange = handlehHttpReceiveChat; 
  	httpReceiveChat.send(null);
	}
}

//deals with the servers' reply to requesting new content
function handlehHttpReceiveChat() 
{
  	if (httpReceiveChat.readyState == 4) 
  	{
   		if(httpReceiveChat.responseText != "empty")
  		{
		    results = httpReceiveChat.responseText.split('---'); //the fields are seperated by ---
		    
		    if(results.length > 2)
			{
			    i = 0;
			    lastId = results[i];
			    userName = results[i+1];
			    message = results[i+2];
			
				//alert("lastId:" + lastId);
				insertNewContent(userName, message); //inserts the new content into the page
				i = i + 3;
			}	
		}
	    setTimeout('receiveChatText();',4000); //executes the next data query in 4 seconds
  	}
}

//inserts the new content into the page
function insertNewContent(liName,liText) {
	liText = liText.replace(/\[/g,"<");
	liText = liText.replace(/\\\//g,"/");
	$("#outputList").prepend("<li class='chatitem'><span class='name'><b>" + liName + ":</b></span><br/>" + liText + "</li>");
}

//stores a new comment on the server
function sendComment() {
	currentChatText = document.getElementById("message").value;
	isSystemMessage = document.getElementById("isSystemMessage").checked;
	//alert("isSystemMessage:" + isSystemMessage);
	if (currentChatText != '' & (httpSendChat.readyState == 4 || httpSendChat.readyState == 0)) 
	{
		param = 'message='+ currentChatText + '&isSystemMessage=' + isSystemMessage;	
		httpSendChat.open("POST", SendChaturl, true);
		httpSendChat.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
  		httpSendChat.onreadystatechange = handlehHttpSendChat;
  		httpSendChat.send(param);
  		document.getElementById("message").value = '';
  		//document.getElementById("isSystemMessage").checked = false;
	} 
	else 
	{
		setTimeout('sendComment();',1000);
	}
}

//deals with the servers' reply to sending a comment
function handlehHttpSendChat() {
  if (httpSendChat.readyState == 4) {
  	receiveChatText(); //refreshes the chat after a new comment has been added (this makes it more responsive)
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
var httpReceiveChat = getHTTPObject();
var httpSendChat = getHTTPObject();
