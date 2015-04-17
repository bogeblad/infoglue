/**
 * This method is used to change editor in the content-tool.
 */
 
function changeEditor(editorId, attributeName)
{
	//alert("editor:" + editor);
	//alert("editorId:" + editorId);
	//alert("attributeName:" + attributeName);
	if(currentEditorIdHash && currentEditorIdHash[attributeName + "CurrentEditorId"])
	{
		currentEditorId = currentEditorIdHash[attributeName + "CurrentEditorId"]
		//alert("currentEditorId:" + currentEditorId);
		if(currentEditorId == editorId)
			return;
	}
	
	//alert("editorId:" + editorId);
	//alert("attributeName:" + attributeName);
	var editor0 = attributeName;
	var editor1 = attributeName + "EditorType1";
	var editor2 = attributeName + "EditorType2";
	var editor3 = attributeName + "EditorType3";
	var editor4 = attributeName + "EditorType4";
	var editor5 = attributeName + "EditorType5";
	var editor6 = attributeName + "EditorType6";
	//alert("editor1:" + editor1);
	
	//If the editor choosen is of type 0 it means we use the standard html input field.
	if(editorId == 0)
	{
		var plainEditor = plainEditors[attributeName];
		//var wysiwygEditor = htmlEditors[attributeName + "WYSIWYGEditor"];
		//alert("plainEditor:" + plainEditor);
		//alert("wysiwygEditor:" + wysiwygEditor);
	
		//alert("Switching to plain...: " + attributeName + ":" + wysiwygEditor.getHTML());
		
		//wysiwygEditor.style.visibility = "hidden";
		//wysiwygEditor.style.display = "none";	
		
		hideEditors(editorId, attributeName);
	
		var plainTextArea = document.getElementById(attributeName);
		plainTextArea.style.display = "block";
	}
	else if(editorId == 1)
	{
		var plainEditor = plainEditors[attributeName];
		var wysiwygEditor = htmlEditors[attributeName + "WYSIWYGEditor"];
		
		wysiwygEditor._iframe.style.display = "block";
		wysiwygEditor._toolbar.style.display = "block";
		//alert(wysiwygEditor);
		var plainTextArea = document.getElementById(attributeName);
		//alert("Value:" + plainTextArea.value);
		var tranformedValue = transformAttribute(plainTextArea.value, document);
		//alert("tranformedValue:" + tranformedValue);
		plainTextArea.value = tranformedValue;
		//alert("tranformedValue2:" + plainTextArea.value);

		if (HTMLArea.is_gecko) { try { wysiwygEditor._doc.designMode = "off"; } catch(e) {}; }

		if (!wysiwygEditor.config.fullPage)
			wysiwygEditor._doc.body.innerHTML = transformAttribute(plainTextArea.value);
		else
		  	wysiwygEditor.setFullHTML(plainTextArea.value);

		//wysiwygEditor.setHTML(plainTextArea.value);
		
		if (HTMLArea.is_gecko) { try { wysiwygEditor._doc.designMode = "on"; } catch(e) {}; }
		
		plainTextArea.style.display = "none";
	}
	else if(editorId == 2)
	{
		if(document.all)
		{
			var plainTextArea = document.getElementById(attributeName);
			plainTextArea.style.display = "none";
			//hideEditors(editor, attributeName);
			showdiv = document.getElementById(editor2);
			showdiv.style.visibility = "visible";
			showdiv.style.display = "block";
			value = document.getElementById(attributeName).value;
			
			//alert(document.getElementById(attributeName + "EditorType2IsActive").value);
			if(document.getElementById(attributeName + "EditorType2IsActive").value == "true")
			{
				//alert("Yep, the applet was active - lets set the text");
				document.applets(attributeName + "InfoGlueCodeEditor").setText(value);
			}
		}
		else
		{
			alert("Switching to VLTEditor is not supported for other browsers than IE");
		}
	}
	else if(editorId == 3)
	{
		hideEditors(editor, attributeName);
		showdiv = document.getElementById(editor3);
		showdiv.style.visibility = "visible";
		//openWindow
	}
	else if(editorId == 4)
	{
		hideEditors(editor, attributeName);
		showdiv = document.getElementById(editor4);
		showdiv.style.visibility = "visible";
		//openWindow
	}
	else if(editorId == 5)
	{
		hideEditors(editor, attributeName);
		showdiv = document.getElementById(editor5);
		showdiv.style.visibility = "visible";
		//openWindow
	}
	else if(editorId == 6)
	{
		hideEditors(editor, attributeName);
		showdiv = document.getElementById(editor6);
		showdiv.style.visibility = "visible";
		//openWindow
	}
	currentEditorIdHash[attributeName + "CurrentEditorId"] = editorId;
	//alert("APA:" + currentEditorIdHash[attributeName + "CurrentEditorId"])
}



/**
 * This method is used to hide all layered editors in the content-tool.
 */
 
function hideEditors(editorId, attributeName)
{
	var editor0 = attributeName;
	var editor1 = attributeName + "EditorType1";
	var editor2 = attributeName + "EditorType2";
	var editor3 = attributeName + "EditorType3";
	var editor4 = attributeName + "EditorType4";
	var editor5 = attributeName + "EditorType5";
	var editor6 = attributeName + "EditorType6";
	
	currentEditorId = currentEditorIdHash[attributeName + "CurrentEditorId"]
	//alert("editorId:" + editorId);
	//alert("currentEditorId:" + currentEditorId);
	
	if(currentEditorId == 0)
	{
	}
	else if(currentEditorId == 1)
	{
		var plainEditor = plainEditors[attributeName];
		var wysiwygEditor = htmlEditors[attributeName + "WYSIWYGEditor"];
		wysiwygEditor._iframe.style.display = "none";
		wysiwygEditor._toolbar.style.display = "none";
		var plainTextArea = document.getElementById(attributeName);
		plainTextArea.value = untransformAttribute(untransformAttribute(wysiwygEditor.getHTML()));
		
		plainTextArea.style.display = "block";
	}
	else if(currentEditorId == 2)
	{
		var plainTextArea = document.getElementById(attributeName);
		
		showdiv = document.getElementById(editor2);
		showdiv.style.display = "none";
		
		//alert("APA:" + document.getElementById(attributeName + "EditorType2IsActive").value);
		if(document.getElementById(attributeName + "EditorType2IsActive").value == "true")
		{
			//alert("Yep, the applet was active - lets set the text and hide");
			
			if(document.all)
			{
				plainTextArea.value = document.applets(attributeName + "InfoGlueCodeEditor").getText();
				document.applets(attributeName + "InfoGlueCodeEditor").setText("");
			}
			else
			{
				alert("Hide is not supported for other browsers than IE");
			}
		}		
	}
	else if(currentEditorId == 3)
	{
		showdiv = document.getElementById(editor3);
		showdiv.style.visibility = "hidden";
	}
	else if(currentEditorId == 4)
	{
		showdiv = document.getElementById(editor4);
		showdiv.style.visibility = "hidden";
	}
	else if(currentEditorId == 5)
	{
		showdiv = document.getElementById(editor5);
		showdiv.style.visibility = "hidden";
	}
	else if(currentEditorId == 6)
	{
		showdiv = document.getElementById(editor6);
		showdiv.style.display = "none";
		//showdiv.style.visibility = "hidden";
	}
	
}


function checkApplet(appletName, handler) 
{
    if (document && 
        document[appletName] && 
        document[appletName].isActive())
      handler(document[appletName]);
    else 
      setTimeout(checkApplet, 100, appletName, handler);
}  

function getAttributeInTagWithDefaultValue(tag, attributeName, defaultValue)
{
	value = defaultValue;
	startPosition = tag.toLowerCase().indexOf(attributeName);
	if(startPosition > -1)
	{
		tag = tag.substring(startPosition, tag.length)
		endPosition = tag.toLowerCase().indexOf("\"");
		endPositionAlternative = tag.toLowerCase().indexOf("'");
		if(endPositionAlternative > -1 && endPositionAlternative < endPosition)
			endPosition = endPositionAlternative;
		if(endPosition > -1)
		{	
			value = tag.substring(startPosition + attributeName.length, endPosition);
			//alert("Value:" + value);
			if(value.charAt(0) == "\"" || value.charAt(0) == "'")
				value = value.substring(1, value.length - 1);
		}
	}
	return value;
}

function Trim(TRIM_VALUE){
if(TRIM_VALUE.length < 1){
return"";
}
TRIM_VALUE = RTrim(TRIM_VALUE);
TRIM_VALUE = LTrim(TRIM_VALUE);
if(TRIM_VALUE==""){
return "";
}
else{
return TRIM_VALUE;
}
} //End Function

function RTrim(VALUE){
var w_space = String.fromCharCode(32);
var v_length = VALUE.length;
var strTemp = "";
if(v_length < 0){
return"";
}
var iTemp = v_length -1;

while(iTemp > -1){
if(VALUE.charAt(iTemp) == w_space){
}
else{
strTemp = VALUE.substring(0,iTemp +1);
break;
}
iTemp = iTemp-1;

} //End While
return strTemp;

} //End Function

function LTrim(VALUE){
var w_space = String.fromCharCode(32);
if(v_length < 1){
return"";
}
var v_length = VALUE.length;
var strTemp = "";

var iTemp = 0;

while(iTemp < v_length){
if(VALUE.charAt(iTemp) == w_space){
}
else{
strTemp = VALUE.substring(iTemp,v_length);
break;
}
iTemp = iTemp + 1;
} //End While
return strTemp;
} //End Function



function transformAttribute(plainAttribute)
{
	//alert("Going to transform:" + plainAttribute);
	var newAttribute = "";
	var remainingAttribute = plainAttribute;
	var startPosition;
	var endPosition;

	startPosition = remainingAttribute.indexOf("$templateLogic.getInlineAssetUrl(");
	while(startPosition > -1)
	{
		newAttribute = newAttribute + remainingAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		remainingAttribute = remainingAttribute.substring(startPosition + 33);
		
		seperatorCharIndex = remainingAttribute.indexOf(",");
		contentId = remainingAttribute.substring(0, seperatorCharIndex);
		assetKey = Trim(remainingAttribute.substring(seperatorCharIndex + 1, remainingAttribute.indexOf(")")));
		//alert("assetKey:" + assetKey);
		assetKey = assetKey.substring(1, assetKey.length - 1);
		//alert("assetKey:" + assetKey);
		
		//if(!contentId || contentId == "" || !assetKey || assetKey == "" || !languageId || languageId == "")
		//	alert("There was a problem with an image which would render the content broken. Please revise and send a screenshot of this to your cms-admin: " + plainAttribute);
		
		//newAttribute = newAttribute + "DownloadAsset.action?contentId=" + contentId +"&languageId=" + languageId + "&assetKey=" + assetKey + "\"";
		newAttribute = newAttribute + "DownloadAsset.action?contentId=" + contentId +"&languageId=" + languageId + "&assetKey=" + assetKey + "";
		
		//alert("remainingAttribute:" + remainingAttribute);
		endIndex = remainingAttribute.indexOf(")");
		//alert("endIndex:" + endIndex);
		
		remainingAttribute = remainingAttribute.substring(endIndex + 1);
		
		//alert("plainAttribute:" + plainAttribute);
		startPosition = remainingAttribute.indexOf("$templateLogic.getInlineAssetUrl(");
	}
	newAttribute = newAttribute + remainingAttribute;

	//alert("Done transforming:" + newAttribute);
	return newAttribute;
}



function untransformAttribute(plainAttribute)
{
	//alert("Going to untransform:" + plainAttribute);
	var newAttribute = "";
	var remainingAttribute = plainAttribute;
	var startPosition;
	var endPosition;
	
	startPosition = remainingAttribute.indexOf("DownloadAsset.action?contentId=");
	while(startPosition > -1)
	{
		newAttribute = newAttribute + remainingAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		remainingAttribute = remainingAttribute.substring(startPosition + 31);
		
		var seperatorCharIndex = remainingAttribute.indexOf("&amp;");
		if(seperatorCharIndex == -1 && remainingAttribute.indexOf("&languageId") > -1)
		{
			seperatorCharIndex = remainingAttribute.indexOf("&");
		}
		var contentId = remainingAttribute.substring(0, seperatorCharIndex);
		var assetKey = remainingAttribute.substring(seperatorCharIndex + 1, remainingAttribute.indexOf("\""));
		//alert("assetKey:" + assetKey);
		var assetStartIndex = assetKey.indexOf("assetKey=") + 9;
		//alert("assetStartIndex:" + assetStartIndex);
		assetKey = assetKey.substring(assetStartIndex);
		//alert("assetKey:" + assetKey);

		if(!contentId || contentId == "" || !assetKey || assetKey == "")
			console.log("There was a problem with an image which would render the content broken. Please revise and send a screenshot of this to your cms-admin: " + plainAttribute);

		newAttribute = newAttribute + "$templateLogic.getInlineAssetUrl(" + contentId + ", \"" + assetKey + "\")\"";
		
		//alert("remainingAttribute:" + remainingAttribute);
		var endIndex = remainingAttribute.indexOf("\"");
		//alert("endIndex:" + endIndex);
		
		remainingAttribute = remainingAttribute.substring(endIndex + 1);
		
		//alert("plainAttribute:" + plainAttribute);
		startPosition = remainingAttribute.indexOf("DownloadAsset.action?contentId=");
	}
	newAttribute = newAttribute + remainingAttribute;

	//alert("Done transforming:" + newAttribute);
	return newAttribute;
}	




/**
 * This method is used to hide all layered editors in the content-tool.
 */
 
function saveValue(attributeName, text)
{
	//alert("Saving " + attributeName + ":" + text);
	document.getElementById(attributeName).value = text;
	validateAndSubmitContentForm();
}

/**
 * This method is used by editors to get the value of the attribute they should work with.
 */
 
function getValue(attributeName)
{
	//alert("Getting " + attributeName);
	//alert("Found " + document.getElementById(attributeName).value);
	return document.getElementById(attributeName).value;
}

/**
 * This method is used by editors to get the value of the attribute they should work with.
 */
 
function setAppletIsActive(attributeName)
{
	//alert("Setting setAppletIsActive on " + attributeName);
	document.getElementById(attributeName + "EditorType2IsActive").value = "true";	
}