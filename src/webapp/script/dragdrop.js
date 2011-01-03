/*
*
*	DRAG & DROP Functionallity
*
*/
    
var srcObj = new Object;
var destObj = null;
var srcObjOrigSrc;
var destObjOrigSrc;
var srcIdIcon;
var srcIdLink;

// string to hold source of object being dragged:
var dummyObj;

function startDrag(){
    // get what is being dragged:
    srcObj = window.event.srcElement;	
    
    var justId = srcObj.id.slice(4,srcObj.id.length);
    
	srcIdIcon = 	document.getElementById("icon" + justId);
	srcIdLink = 	document.getElementById("link" + justId);		
	
	srcObjOrigSrc = srcIdIcon.src;
	srcIdIcon.src = srcIdIcon.src + ".ghost.png";
	srcIdLink.className = "dragging";

	// srcObjOrigSrc = srcObj.src;
	// srcObj.src = 'images/tree/xp/itemghost.png';
	
	

    // store the source of the object into a string acting as a dummy object so we don't ruin the original object:
    dummyObj = srcObj.outerHTML;

    // post the data for Windows:
    var dragData = window.event.dataTransfer;

    // set the type of data for the clipboard:
    dragData.setData('Text', window.event.srcElement.id);

    // allow only dragging that involves moving the object:
    dragData.effectAllowed = 'all';

    // use the special 'move' cursor when dragging:
    dragData.dropEffect = 'move';
}
function endDrag() {
    // when done remove clipboard data
	srcIdIcon.src = srcObjOrigSrc;
	srcIdLink.className = "";
    // srcObj.src = srcObjOrigSrc;
    window.event.dataTransfer.clearData();
}


function enterDrag() {
	// Drop ikon
	
	// Kolla om destObj inte blivit återställt
	if (destObj != null)
	{
		if (destObj.src.indexOf(".drop.png") > -1)
		{
			exitDrag();		
		}
	}
	
    destObj = window.event.srcElement;
    
    if (destObj.id != srcIdIcon.id)
    {
	    var justId = destObj.id.slice(4,destObj.id.length);
		var destLink = document.getElementById("link" + justId);		
		destLink.className = "drop";
		
		destObjOrigSrc = destObj.src;
		destObj.src = destObj.src + ".drop.png";
	}
    
    // allow target object to read clipboard:
    window.event.dataTransfer.getData('Text');
}
function exitDrag() {

	if (destObj != null)
	    if (destObj.id != srcIdIcon.id)
	    {
			// Återställ ikon
		    var justId = destObj.id.slice(4,destObj.id.length);
			var destLink = document.getElementById("link" + justId);		
			destLink.className = "";
			
			destObj.src = destObjOrigSrc;
			destObj=null;
		}

    // allow target object to read clipboard:
    window.event.dataTransfer.getData('Text');
}

function overDrag() {
	// alert("over");
    // tell onOverDrag handler not to do anything:
    destObj = window.event.srcElement;
	if (destObj.src.indexOf(".drop.png") == -1)
	{
		enterDrag();		
	}

    window.event.returnValue = false;
}

function drop() {
    // eliminate default action of ondrop so we can customize:
	if (destObj != null)
	{
		if (destObj.src.indexOf(".drop.png") > -1)
		{
			exitDrag();		
		}
	}
    destObj = window.event.srcElement;
    window.event.returnValue = false;

    // manually add new attributes:
    // dummyObj = addAttribute(dummyObj, 'height="25" width="25" alt="' + srcObj.myLabel + '"');

	var srcId = srcObj.id.slice(4,srcObj.id.length);
	var destId = destObj.id.slice(4,destObj.id.length);
	var srcLabel = srcObj.myLabel;
	var destLabel = destObj.myLabel;

    // alert("do you really want to move " + srcObj.myLabel + " to " + destObj.myLabel + "?");

	MoveRequest(srcId, destId, srcLabel, destLabel);
   
}

// since we aren't working with an actual object, we will add our attributes manually:
function addAttribute(oObj, sVal) {
    var loc = oObj.indexOf(">");
    return oObj.substring(0, loc) + ' ' + sVal + '>';
}
