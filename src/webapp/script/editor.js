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
			
			plainTextArea.value = document.applets(attributeName + "InfoGlueCodeEditor").getText();
			document.applets(attributeName + "InfoGlueCodeEditor").setText("");
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


function transformAttribute(plainAttribute, document)
{
	//alert("Going to transform:" + plainAttribute);
	var newAttribute = "";
	var startPosition;
	var endPosition;

	startPosition = plainAttribute.toLowerCase().indexOf("<a ");
	index = 0;
	var oldTitle;
	
	//alert("startPosition:" + startPosition);
	while(startPosition > -1 && index < 100)
	{
		newAttribute += plainAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		plainAttribute = plainAttribute.substring(startPosition, plainAttribute.length)
		//alert("Left in plainAttribute:" + plainAttribute)
		startTextPosition = plainAttribute.indexOf(">") + 1;
		//alert("startTextPosition:" + startTextPosition);
		endPosition = plainAttribute.indexOf("</a>");
		text = plainAttribute.substring(startTextPosition, endPosition);
		//alert("text:" + text);
			
		hrefTag = plainAttribute.substring(0, endPosition + 4);
		//alert("hrefTag:" + hrefTag);
		
		if(hrefTag.indexOf("href") > -1 && hrefTag.indexOf("$templateLogic.getPageUrl") > -1)
		{
			hrefStartIndex = hrefTag.toLowerCase().indexOf("href");
			//alert(hrefStartIndex);
			startPart = hrefTag.substring(0, hrefStartIndex + 5);

			//alert("Tranforming templateLogic.getPageUrl...");
			siteNodeIdIndex = hrefTag.indexOf("getPageUrl(") + 11;
			siteNodeIdEndIndex = hrefTag.indexOf(",", siteNodeIdIndex);
			siteNodeId = hrefTag.substring(siteNodeIdIndex, siteNodeIdEndIndex);
			//alert("siteNodeId:" + siteNodeId);
			
			//alert("navigationTitleHash:" + navigationTitleHash)
			//navigationTitle = navigationTitleHash[siteNodeId];
			//alert("navigationTitle:" + navigationTitle);
			
			remainingTag = hrefTag.substring(hrefStartIndex + 5, hrefTag.length);
			//alert("Remaining tag before:" + remainingTag);
			endIndex = remainingTag.indexOf("\" ");
			//alert("endIndex:" + endIndex);
			var jumpSteps = 0;
			if(remainingTag.indexOf("' ") > -1 && (endIndex == -1 || remainingTag.indexOf("' ") < endIndex))
			{
				endIndex = remainingTag.indexOf("' ");
			}
			else if(remainingTag.indexOf("\")") > -1 && (endIndex == -1 || remainingTag.indexOf("\")") < endIndex))
			{
				endIndex = remainingTag.indexOf("\")");
				jumpSteps = 3;
			}
			else if(remainingTag.indexOf("')") > -1 && (endIndex == -1 || remainingTag.indexOf("')") < endIndex))
			{
				endIndex = remainingTag.indexOf("')");
				jumpSteps = 3;
			}
			remainingTag = remainingTag.substring(endIndex + jumpSteps + 1, remainingTag.indexOf(">", endIndex + jumpSteps));
			//alert("remainingTag:" + remainingTag);
			//newAttribute += startPart + remainingTag; 

			//alert("hrefTag:" + hrefTag);
			//alert("unescaped hrefTag:" + unescape(hrefTag));
			newAttribute += "<a href=\"#\" originaltag=\"" + escape(hrefTag) + "\"" + remainingTag + ">" + text + "</a>";
			//newAttribute += "<a href=\"#\" originaltag=\"" + escape(unescape(hrefTag)) + "\"" + remainingTag + ">" + text + "</a>";
			//alert("newAttribute:" + newAttribute);
		}
		else if(hrefTag.indexOf("href") > -1 && hrefTag.indexOf("$templateLogic.getInlineAssetUrl") > -1)
		{
			//alert("Tranforming $templateLogic.getInlineAssetUrl...");
			//alert("hrefTag:" + hrefTag);
			
			hrefStartIndex = hrefTag.toLowerCase().indexOf("href");
			//alert(hrefStartIndex);
			
			remainingTag = hrefTag.substring(hrefStartIndex + 5, hrefTag.length);
			//alert("Remaining tag before:" + remainingTag);
			endIndex = remainingTag.indexOf("\" ");
			//alert("endIndex:" + endIndex);
			var jumpSteps = 0;
			if(remainingTag.indexOf("' ") > -1 && (endIndex == -1 || remainingTag.indexOf("' ") < endIndex))
			{
				endIndex = remainingTag.indexOf("' ");
			}
			else if(remainingTag.indexOf("\")") > -1 && (endIndex == -1 || remainingTag.indexOf("\")") < endIndex))
			{
				endIndex = remainingTag.indexOf("\")");
				jumpSteps = 3;
			}
			else if(remainingTag.indexOf("')") > -1 && (endIndex == -1 || remainingTag.indexOf("')") < endIndex))
			{
				endIndex = remainingTag.indexOf("')");
				jumpSteps = 3;
			}
			remainingTag = remainingTag.substring(endIndex + jumpSteps + 1, remainingTag.indexOf(">", endIndex + jumpSteps));
			//alert("remainingTag:" + remainingTag);
			
			transformedTag = "<a href=\"#\" originaltag=\"" + escape(hrefTag) + "\"" + remainingTag + ">" + text + "</a>";
			//alert("transformedTag:" + transformedTag);
			newAttribute += transformedTag;
			//newAttribute += "<a href=\"#\" originaltag=\"" + escape(hrefTag) + "\"" + remainingTag + ">" + text + "</a>";
			//alert("newAttribute:" + newAttribute);
		}
		else
		{
			newAttribute += hrefTag;
		}
		
		if((endPosition + 4) < plainAttribute.length)
			plainAttribute = plainAttribute.substring(endPosition + 4, plainAttribute.length)		
		else
			plainAttribute = "";		

		//alert("plainAttribute:" + plainAttribute);
		startPosition = plainAttribute.toLowerCase().indexOf("<a ");
		
		index += 1;
	}
	
	
	newAttribute += plainAttribute;
	plainAttribute = newAttribute;
	//alert("plainAttribute:" + plainAttribute);
	//First transformation done... now to the next
	
	newAttribute = "";
	
	startPosition = plainAttribute.toLowerCase().indexOf("<img");
	index = 0;
	while(startPosition > -1 && index < 100)
	{
		newAttribute += plainAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		plainAttribute = plainAttribute.substring(startPosition, plainAttribute.length)
		//alert("Left in plainAttribute:" + plainAttribute)
		endPosition = plainAttribute.indexOf(">");
			
		imgTag = plainAttribute.substring(0, endPosition + 1);
		
		//alert(imgTag);
		if(imgTag.indexOf("$templateLogic") > -1)
		{
			srcStartIndex = imgTag.toLowerCase().indexOf("src");
			//alert(srcStartIndex);
			startPart = imgTag.substring(0, srcStartIndex + 4);
			//alert("StartPart:" + startPart);
			
			//alert("imgTag:" + imgTag);
			imgTag = imgTag.replace(/\$templateLogic.getAssetUrl\(\$templateLogic.contentId,[\s]{0,1}/gi, "$templateLogic.getInlineAssetUrl(");
			imgTag = imgTag.replace(/\'/gi, "\"");
			//alert("imgTag:" + imgTag);
			
			assetUrl = "images/imagePlaceHolder.jpg";
			//alert("imgTag:" + imgTag);
			//alert("indexOf:" + imgTag.indexOf("getInlineAssetUrl"));
			if(imgTag.indexOf("getInlineAssetUrl") > -1)
			{
				if(imgTag.indexOf(",") > -1)
				{
					currentContentId = imgTag.substring(imgTag.indexOf("getInlineAssetUrl") + 18, imgTag.indexOf(","));
					assetKey = imgTag.substring(imgTag.indexOf(", \"") + 3, imgTag.indexOf("\"\)"));
					assetUrl = "DownloadAsset.action?contentId=" + currentContentId + "&languageId=" + document.editForm.languageId.value + "&assetKey=" + assetKey;
				}
				else
				{			
					assetKey = imgTag.substring(imgTag.indexOf("getInlineAssetUrl") + 19, imgTag.indexOf("\"\)"));
					//alert("assetKey:" + assetKey);	
					//alert("Document:" + self.document.location);
					if(document.getElementById("digitalAsset" + assetKey))
						assetUrl =  document.getElementById("digitalAsset" + assetKey).value;
					else if(self.document.getElementById("digitalAsset" + assetKey))
						assetUrl =  self.document.getElementById("digitalAsset" + assetKey).value;
					
					//alert("languageId:" + document.editForm.languageId.value);
				}
			}
				
			startPart += "\"" + assetUrl + "\" originaltag=\"" + escape(imgTag) + "\"";
			//alert("StartPart:" + startPart);
			remainingTag = imgTag.substring(srcStartIndex + 4, imgTag.length);
			//alert("Remaining tag before:" + remainingTag);
			endIndex = remainingTag.indexOf("\" ");
			//alert("endIndex:" + endIndex);
			var jumpSteps = 0;
			if(remainingTag.indexOf("' ") > -1 && (endIndex == -1 || remainingTag.indexOf("' ") < endIndex))
			{
				endIndex = remainingTag.indexOf("' ");
			}
			else if(remainingTag.indexOf("\")") > -1 && (endIndex == -1 || remainingTag.indexOf("\")") < endIndex))
			{
				endIndex = remainingTag.indexOf("\")");
				jumpSteps = 3;
			}
			else if(remainingTag.indexOf("')") > -1 && (endIndex == -1 || remainingTag.indexOf("')") < endIndex))
			{
				endIndex = remainingTag.indexOf("')");
				jumpSteps = 3;
			}
			remainingTag = remainingTag.substring(endIndex + jumpSteps, remainingTag.length);
			newAttribute += startPart + remainingTag; 
			//alert("Remaining tag after:" + remainingTag);
		}
		else
		{
			newAttribute += imgTag;
		}
		
		if((endPosition + 1) < plainAttribute.length)
			plainAttribute = plainAttribute.substring(endPosition + 1, plainAttribute.length)		
		else
			plainAttribute = "";		
			
		startPosition = plainAttribute.toLowerCase().indexOf("<img");
		
		index += 1;
	}
	
	newAttribute += plainAttribute;

	//alert("Done transforming:" + newAttribute);
	return newAttribute;
}

/*
function transformAttribute(plainAttribute, document)
{
	//alert("Going to transform:" + plainAttribute);
	var newAttribute = "";
	var startPosition;
	var endPosition;

	startPosition = plainAttribute.toLowerCase().indexOf("<img");
	index = 0;
	while(startPosition > -1 && index < 100)
	{
		newAttribute += plainAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		plainAttribute = plainAttribute.substring(startPosition, plainAttribute.length)
		//alert("Left in plainAttribute:" + plainAttribute)
		endPosition = plainAttribute.indexOf(">");
			
		imgTag = plainAttribute.substring(0, endPosition + 1);
		
		//alert(imgTag);
		if(imgTag.indexOf("$templateLogic") > -1)
		{
			srcStartIndex = imgTag.toLowerCase().indexOf("src");
			//alert(srcStartIndex);
			startPart = imgTag.substring(0, srcStartIndex + 4);
			//alert("StartPart:" + startPart);
			
			//alert("imgTag:" + imgTag);
			imgTag = imgTag.replace(/\$templateLogic.getAssetUrl\(\$templateLogic.contentId,[\s]{0,1}/gi, "$templateLogic.getInlineAssetUrl(");
			imgTag = imgTag.replace(/\'/gi, "\"");
			//alert("imgTag:" + imgTag);
			
			assetUrl = "images/imagePlaceHolder.jpg";
			//alert("imgTag:" + imgTag);
			//alert("indexOf:" + imgTag.indexOf("getInlineAssetUrl"));
			if(imgTag.indexOf("getInlineAssetUrl") > -1)
			{
				if(imgTag.indexOf(",") > -1)
				{
					currentContentId = imgTag.substring(imgTag.indexOf("getInlineAssetUrl") + 18, imgTag.indexOf(","));
					assetKey = imgTag.substring(imgTag.indexOf(", \"") + 3, imgTag.indexOf("\"\)"));
					assetUrl = "DownloadAsset.action?contentId=" + currentContentId + "&languageId=" + document.editForm.languageId.value + "&assetKey=" + assetKey;
				}
				else
				{			
					assetKey = imgTag.substring(imgTag.indexOf("getInlineAssetUrl") + 19, imgTag.indexOf("\"\)"));
					//alert("assetKey:" + assetKey);	
					//alert("Document:" + self.document.location);
					if(document.getElementById("digitalAsset" + assetKey))
						assetUrl =  document.getElementById("digitalAsset" + assetKey).value;
					else if(self.document.getElementById("digitalAsset" + assetKey))
						assetUrl =  self.document.getElementById("digitalAsset" + assetKey).value;
					
					//alert("languageId:" + document.editForm.languageId.value);
				}
			}
				
			startPart += "\"" + assetUrl + "\" originaltag=\"" + escape(imgTag) + "\"";
			//alert("StartPart:" + startPart);
			remainingTag = imgTag.substring(srcStartIndex + 4, imgTag.length);
			//alert("Remaining tag before:" + remainingTag);
			endIndex = remainingTag.indexOf("\" ");
			//alert("endIndex:" + endIndex);
			var jumpSteps = 0;
			if(remainingTag.indexOf("' ") > -1 && (endIndex == -1 || remainingTag.indexOf("' ") < endIndex))
			{
				endIndex = remainingTag.indexOf("' ");
			}
			else if(remainingTag.indexOf("\")") > -1 && (endIndex == -1 || remainingTag.indexOf("\")") < endIndex))
			{
				endIndex = remainingTag.indexOf("\")");
				jumpSteps = 3;
			}
			else if(remainingTag.indexOf("')") > -1 && (endIndex == -1 || remainingTag.indexOf("')") < endIndex))
			{
				endIndex = remainingTag.indexOf("')");
				jumpSteps = 3;
			}
			remainingTag = remainingTag.substring(endIndex + jumpSteps, remainingTag.length);
			newAttribute += startPart + remainingTag; 
			//alert("Remaining tag after:" + remainingTag);
		}
		else
		{
			newAttribute += imgTag;
		}
		
		if((endPosition + 1) < plainAttribute.length)
			plainAttribute = plainAttribute.substring(endPosition + 1, plainAttribute.length)		
		else
			plainAttribute = "";		
			
		startPosition = plainAttribute.toLowerCase().indexOf("<img");
		
		index += 1;
	}
	
	newAttribute += plainAttribute;
	plainAttribute = newAttribute;
	//alert(plainAttribute);
	//First transformation done... now to the next
	
	newAttribute = "";
	startPosition = plainAttribute.toLowerCase().indexOf("<a ");
	index = 0;
	var oldTitle;
	
	//alert("startPosition:" + startPosition);
	while(startPosition > -1 && index < 100)
	{
		newAttribute += plainAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		plainAttribute = plainAttribute.substring(startPosition, plainAttribute.length)
		//alert("Left in plainAttribute:" + plainAttribute)
		startTextPosition = plainAttribute.indexOf(">") + 1;
		//alert("startTextPosition:" + startTextPosition);
		endPosition = plainAttribute.indexOf("</a>");
		text = plainAttribute.substring(startTextPosition, endPosition);
		//alert("text:" + text);
			
		hrefTag = plainAttribute.substring(0, endPosition + 4);
		//alert("hrefTag:" + hrefTag);
		
		if(hrefTag.indexOf("href") > -1 && hrefTag.indexOf("$templateLogic.getPageUrl") > -1)
		{
			hrefStartIndex = hrefTag.toLowerCase().indexOf("href");
			//alert(hrefStartIndex);
			startPart = hrefTag.substring(0, hrefStartIndex + 5);

			//alert("Tranforming templateLogic.getPageUrl...");
			siteNodeIdIndex = hrefTag.indexOf("getPageUrl(") + 11;
			siteNodeIdEndIndex = hrefTag.indexOf(",", siteNodeIdIndex);
			siteNodeId = hrefTag.substring(siteNodeIdIndex, siteNodeIdEndIndex);
			//alert("siteNodeId:" + siteNodeId);
			
			//alert("navigationTitleHash:" + navigationTitleHash)
			//navigationTitle = navigationTitleHash[siteNodeId];
			//alert("navigationTitle:" + navigationTitle);
			
			remainingTag = hrefTag.substring(hrefStartIndex + 5, hrefTag.length);
			//alert("Remaining tag before:" + remainingTag);
			endIndex = remainingTag.indexOf("\" ");
			//alert("endIndex:" + endIndex);
			var jumpSteps = 0;
			if(remainingTag.indexOf("' ") > -1 && (endIndex == -1 || remainingTag.indexOf("' ") < endIndex))
			{
				endIndex = remainingTag.indexOf("' ");
			}
			else if(remainingTag.indexOf("\")") > -1 && (endIndex == -1 || remainingTag.indexOf("\")") < endIndex))
			{
				endIndex = remainingTag.indexOf("\")");
				jumpSteps = 3;
			}
			else if(remainingTag.indexOf("')") > -1 && (endIndex == -1 || remainingTag.indexOf("')") < endIndex))
			{
				endIndex = remainingTag.indexOf("')");
				jumpSteps = 3;
			}
			remainingTag = remainingTag.substring(endIndex + jumpSteps + 1, remainingTag.indexOf(">", endIndex + jumpSteps));
			//alert("remainingTag:" + remainingTag);
			//newAttribute += startPart + remainingTag; 

			newAttribute += "<a href=\"#\" originaltag=\"" + escape(hrefTag) + "\"" + remainingTag + ">" + text + "</a>";
			//alert("newAttribute:" + newAttribute);
		}
		else if(hrefTag.indexOf("href") > -1 && hrefTag.indexOf("$templateLogic.getInlineAssetUrl") > -1)
		{
			//alert("Tranforming $templateLogic.getInlineAssetUrl...");
			//alert("hrefTag:" + hrefTag);
			
			hrefStartIndex = hrefTag.toLowerCase().indexOf("href");
			//alert(hrefStartIndex);
			
			remainingTag = hrefTag.substring(hrefStartIndex + 5, hrefTag.length);
			//alert("Remaining tag before:" + remainingTag);
			endIndex = remainingTag.indexOf("\" ");
			//alert("endIndex:" + endIndex);
			var jumpSteps = 0;
			if(remainingTag.indexOf("' ") > -1 && (endIndex == -1 || remainingTag.indexOf("' ") < endIndex))
			{
				endIndex = remainingTag.indexOf("' ");
			}
			else if(remainingTag.indexOf("\")") > -1 && (endIndex == -1 || remainingTag.indexOf("\")") < endIndex))
			{
				endIndex = remainingTag.indexOf("\")");
				jumpSteps = 3;
			}
			else if(remainingTag.indexOf("')") > -1 && (endIndex == -1 || remainingTag.indexOf("')") < endIndex))
			{
				endIndex = remainingTag.indexOf("')");
				jumpSteps = 3;
			}
			remainingTag = remainingTag.substring(endIndex + jumpSteps + 1, remainingTag.indexOf(">", endIndex + jumpSteps));
			//alert("remainingTag:" + remainingTag);
			
			transformedTag = "<a href=\"#\" originaltag=\"" + escape(hrefTag) + "\"" + remainingTag + ">" + text + "</a>";
			//alert("transformedTag:" + transformedTag);
			newAttribute += transformedTag;
			//newAttribute += "<a href=\"#\" originaltag=\"" + escape(hrefTag) + "\"" + remainingTag + ">" + text + "</a>";
			//alert("newAttribute:" + newAttribute);
		}
		else
		{
			newAttribute += hrefTag;
		}
		
		if((endPosition + 4) < plainAttribute.length)
			plainAttribute = plainAttribute.substring(endPosition + 4, plainAttribute.length)		
		else
			plainAttribute = "";		

		//alert("plainAttribute:" + plainAttribute);
		startPosition = plainAttribute.toLowerCase().indexOf("<a ");
		
		index += 1;
	}
	
	newAttribute += plainAttribute;

	//alert("Done transforming:" + newAttribute);
	return newAttribute;
}
*/



function untransformAttribute(plainAttribute)
{
	//alert("Going to untransform:" + plainAttribute);
	var newAttribute = "";
	var startPosition;
	var endPosition;

	startPosition = plainAttribute.toLowerCase().indexOf("<img");
	//alert("startPosition:" + startPosition);
	index = 0;
	while(startPosition > -1 && index < 100)
	{
		newAttribute += plainAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		plainAttribute = plainAttribute.substring(startPosition, plainAttribute.length)
		//alert("Left in plainAttribute:" + plainAttribute)
		endPosition = plainAttribute.indexOf(">");
			
		imgTag = plainAttribute.substring(0, endPosition + 1);
		//alert(imgTag);
		originalTagStartIndex = imgTag.indexOf("originaltag");
		//alert(originalTagStartIndex);
		if(originalTagStartIndex > -1)
		{
			imgTag = imgTag.substring(originalTagStartIndex + 13, imgTag.length)
			//alert(imgTag);
			originalTagEndIndex = imgTag.indexOf("\"");
			//alert("originalTagEndIndex:" + originalTagEndIndex);
			//alert("ImageTag med påhäng:" + imgTag + ":" + imgTag.length + ":" + originalTagEndIndex);
			imgTag = imgTag.substring(0, originalTagEndIndex);
			//alert(imgTag);
			imgTag = unescape(imgTag);
			//alert("ImageTag som skall vara ren:" + imgTag);
			newAttribute += imgTag;
		}
		else
		{
			newAttribute += imgTag;
		}

		//alert((endPosition + 1) + "<" + plainAttribute.length + "=" + ((endPosition + 1) < plainAttribute.length));
		if((endPosition + 1) < plainAttribute.length)
			plainAttribute = plainAttribute.substring(endPosition + 1, plainAttribute.length)		
		else
			plainAttribute = "";		
			
		startPosition = plainAttribute.toLowerCase().indexOf("<img");
		
		index += 1;
	}
	newAttribute += plainAttribute;

	plainAttribute = newAttribute;
	//First transformation done... now to the next

	//alert("Going to untransform:" + plainAttribute);
	
	newAttribute = "";
	startPosition = plainAttribute.toLowerCase().indexOf("<a ");
	//alert("startPosition:" + startPosition);
	index = 0;
	while(startPosition > -1 && index < 100)
	{
		newAttribute += plainAttribute.substring(0, startPosition);
		//alert("newAttribute:" + newAttribute);
		plainAttribute = plainAttribute.substring(startPosition, plainAttribute.length)
		//alert("Left in plainAttribute:" + plainAttribute)
		endPosition = plainAttribute.toLowerCase().indexOf("</a>");
			
		hrefTag = plainAttribute.substring(0, endPosition + 4);
		//alert("hrefTag:" + hrefTag);
		
		originalTagStartIndex = hrefTag.indexOf("originaltag");
		//alert("originalTagStartIndex:" + originalTagStartIndex);
		if(originalTagStartIndex > -1)
		{
			//alert("1 hrefTag:" + hrefTag);
			originalTagEndIndex = hrefTag.indexOf("\"", originalTagStartIndex + 13);
			//alert("2 hrefTag:" + originalTagEndIndex);
			//hrefTag = hrefTag.substring(originalTagStartIndex + 13, hrefTag.length);
			hrefTag = hrefTag.substring(originalTagStartIndex + 13, originalTagEndIndex)
			//alert("hrefTag:" + hrefTag);
			originalTagEndIndex = hrefTag.indexOf(">");
			//alert("originalTagEndIndex:" + originalTagEndIndex);
			//hrefTag = hrefTag.substring(0, originalTagEndIndex - 1);
			//alert("hrefTag:" + hrefTag);
			hrefTag = unescape(hrefTag);
			//alert("escaped hrefTag:" + hrefTag);
			newAttribute += hrefTag;
		}
		else
		{
			newAttribute += hrefTag;
		}

		//alert((endPosition + 4) + "<" + plainAttribute.length + "=" + ((endPosition + 4) < plainAttribute.length));
		if((endPosition + 4) < plainAttribute.length)
			plainAttribute = plainAttribute.substring(endPosition + 4, plainAttribute.length)		
		else
			plainAttribute = "";	
			
		startPosition = plainAttribute.toLowerCase().indexOf("<a ");
		
		index += 1;
	}

	newAttribute += plainAttribute;

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