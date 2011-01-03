function notifyDragHTML(html)
{
	if(parent.notifyDragHTML)
		parent.notifyDragHTML(html);
}

function getDragHTML()
{
	if(parent.getDragHTML)
		return parent.getDragHTML();
}

function emptyDragHTML()
{
	if(parent.emptyDragHTML)
		parent.emptyDragHTML();
}

function disableDrag()
{
	if(parent.disableDrag)
		parent.disableDrag();
}

function enableDrag()
{
	if(parent.enableDrag)
		parent.enableDrag();	
}

function dragCompleted()
{
	if(parent.dragCompleted)
		parent.dragCompleted();
}

function getIsDragCompleted()
{
	if(parent.getIsDragCompleted)
		return parent.getIsDragCompleted();
}

function getIsDragActive()
{
	if(parent.getIsDragActive)
		return parent.getIsDragActive();
}