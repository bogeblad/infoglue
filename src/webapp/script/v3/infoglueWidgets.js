/************************************************
 *  All methods needed to handle checkbox lists *
 ************************************************/
function refreshEvents(attributeName)
{
	$("#leftSelect" + attributeName + " option").unbind("dblclick");
	$("#rightSelect" + attributeName + " option").unbind("dblclick");
    $("#leftSelect" + attributeName + " option").dblclick(function () { moveRight(this, attributeName); });
	$("#rightSelect" + attributeName + " option").dblclick(function () { moveLeft(this, attributeName); });
}

function moveAll(attributeName, fromId, toId)
{
	$("#" + fromId + " option").clone().prependTo("#" + toId);
	$("#" + fromId).empty();
	refreshEvents(attributeName);
}

function moveAllMarked(attributeName, fromId, toId)
{
	$("#" + fromId + " option:selected").clone().prependTo("#" + toId);
	$("#" + fromId + " option:selected").remove();
	refreshEvents(attributeName);
}

function moveRight(element, attributeName) 
{ 
    $(element).clone().prependTo("#rightSelect" + attributeName);
	$(element).remove();
	refreshEvents(attributeName);
}

function moveLeft(element, attributeName)  
{ 
    $(element).clone().prependTo("#leftSelect" + attributeName);
    $(element).remove();
	refreshEvents(attributeName);
}
