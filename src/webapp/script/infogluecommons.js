/**
 * This method shows a hidden layer.
 */
 
function showDiv(id)
{
	document.getElementById(id).style.visibility = 'visible';
}

/**
 * This method hides a layer.
 */

function hideDiv(id)
{
	document.getElementById(id).style.visibility = 'hidden';
}

/**
 * This method close layer.
 */
 
function closeDiv(id)
{
	document.getElementById(id).style.display = 'none';
}

/**
 * This method opens a layer.
 */

function openDiv(id)
{
	document.getElementById(id).style.display = 'block';
}

function toggleDivVisibility(id)
{
	var div = document.getElementById(id);
	if(div && div.style.visibility == "visible")
		div.style.visibility = "hidden";
	else
		div.style.visibility = "visible";
}

function toggleDivExpansion(id)
{
	var div = document.getElementById(id);
	if(div && div.style.display == "block")
		div.style.display = "none";
	else
		div.style.display = "block";

	var statusImage = document.getElementById(id + "Image");
	if(div && div.style.display == "block")
		statusImage.src = "images/arrowDown.gif";
	else
		statusImage.src = "images/arrowright.gif";		
}


function ddListClick(listFieldId, targetFieldId)
{
	// Show / hide list
	var tt = document.getElementById(targetFieldId);
	var list = document.getElementById(listFieldId);
	var val = list.options[list.selectedIndex].value
	tt.value=val;
	list.style.visibility = "hidden";
	// alert(val);
}


/**
 * This method moves a layer.
 */
 
function moveDiv(id, x, y)
{
	document.getElementById(id).style.left = x;
	document.getElementById(id).style.top = y;
}

/**
 * This method resizes a layer.
 */
 
function resizeDiv(id, width, height)
{
	document.getElementById(id).style.width = width;
	document.getElementById(id).style.height = height;
}

/**
 * This method submit a form.
 */

function submitForm(id)
{
	form = document.getElementById(id);
}

/**
 * This method returns the width of a window/frame
 */
 
function getWindowWidth()
{
	var width = 640;
	if (window.innerWidth || window.innerHeight){ 
		width = window.innerWidth; 
	} 
	else if (document.body.clientWidth || document.body.clientHeight){ 
		width = document.body.clientWidth; 
	} 
	
	return width;
} 

/**
 * This method returns the height of a window/frame
 */

function getWindowHeight()
{
	height = 480;
	if (window.innerWidth || window.innerHeight){ 
		height = window.innerHeight; 
	} 
	else if (document.body.clientWidth || document.body.clientHeight){ 
		height = document.body.clientHeight; 
	} 

	return height;
} 
