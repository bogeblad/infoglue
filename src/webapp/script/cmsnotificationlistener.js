/* 
 * $id:$
 * CMS Notification listener
 * @author: Stefan Sik
 * 
 * Subscribe for CMS Change notifications.
 *
 * Usage:
 * 1: Include this script: 
 *    <script type="text/javascript" src="script/cmsnotificationlistener.js"></script>
 *
 * 
 * 2: Define callback: function cmsNotify(className, objectId, type) {}
 *    to recieve messages from cms.
 */

var _pollInterval = 10000;

var _xmlHttpRequest;
function _sendRequest(url) 
{
    // Native
    if (window.XMLHttpRequest) {
        _xmlHttpRequest = new XMLHttpRequest();
        _xmlHttpRequest.onreadystatechange = _processRequestChange;
        _xmlHttpRequest.open("POST", url, true);
        _xmlHttpRequest.send(null);
    // IE/Windows ActiveX version
    } else if (window.ActiveXObject) {
        _xmlHttpRequest = new ActiveXObject("Microsoft.XMLHTTP");
        if (_xmlHttpRequest) {
            _xmlHttpRequest.onreadystatechange = _processRequestChange;
            _xmlHttpRequest.open("POST", url, true);
            _xmlHttpRequest.send();
        }
    }
}

function _processRequestChange() {
   if (_xmlHttpRequest.readyState == 4) {
        // only if "OK"
        if (_xmlHttpRequest.status == 200) 
        {
            var response  = _xmlHttpRequest.responseXML.documentElement;
            var messages = response.getElementsByTagName('org.infoglue.cms.util.NotificationMessage');
            for(i=0; i<messages.length;i++)
	    {
               var mess = messages[i];
               _onXmlMessage(mess);
            }
	    setTimeout("_getChangeNotifications()",_pollInterval);
        } 
        else 
        {
            alert("error:\n" + _xmlHttpRequest.statusText);
        }
    }
}
function _getChangeNotifications()
{
  _sendRequest("SimpleContentXml!getChangeNotifications.action");
}
function _onXmlMessage(mess)
{
	try 
	{
 		onXmlMessage(mess);
	}
	catch(e) 
	{
	}
	
	
    if(mess.nodeName=="org.infoglue.cms.util.NotificationMessage")
    {
    	var className="";
        var objectId="";
    	var type="";
        for(i=0;i<mess.childNodes.length;i++)
        {
            n = mess.childNodes[i];
            if(n.nodeName=="className")
		    {
				className=n.firstChild.nodeValue;
            }
            if(n.nodeName=="objectId")
		    {
				objectId=n.firstChild.nodeValue;
            }
            if(n.nodeName=="type")
		    {
				type=n.firstChild.nodeValue;
            }
        }
		try 
		{
	 		cmsNotify(className, objectId, type);
		}
		catch(e) 
		{
		}
    }
}

_getChangeNotifications();