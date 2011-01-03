var isDragActive = false;
var isLocalDragActive = false;
var isDragCompleted = false;
var dragHTML = "";

function getIsIGStandardTools()
{
	return true;
}

function notifyDragHTML(html)
{
	dragHTML = html;
	isDragCompleted = false;
	isDragActive = true;
	//alert("dragHTML:" + dragHTML);
	$("#debug").text("html: " + html);
}

function getDragHTML()
{
	return dragHTML;
}

function emptyDragHTML()
{
	dragHTML = "";
}

function disableDrag()
{
	isDragActive = false;
	isLocalDragActive = false;
	$("#tempDraggable").remove();
}
function enableDrag()
{
	isDragActive = true;
}

function dragCompleted()
{
	//alert("Drag completed");
	isDragCompleted = true;
	$("#tempDraggable").remove();
}

function getIsDragCompleted()
{
	return isDragCompleted;
}

function getIsDragActive()
{
	return isDragActive;
}
