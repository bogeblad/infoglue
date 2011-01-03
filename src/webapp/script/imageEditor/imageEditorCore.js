/*
 * (c) Copyright SXOOP Technologies Ltd. 2005-2006
 * All rights reserved.
 *
 * If you have purchased PXN8 for use on your own servers and want to change the 
 * core functionality we strongly recommend that 
 * You make a copy of this file and rename it to $YOURCOMPANY_pxn8core.js and use that
 * as a working copy.
 */

/*
 * ----------------
 * GLOBAL VARIABLES
 * ----------------
 */

var moz = ((document.all)? false : true); 
var ie = ((document.all)? true : false);

// the start of the selection along the X axis (from left)
var sx = -1;
// the start of the selection along the Y axis (from top)
var sy = -1;
// the end of the selection along the X axis 
var ex = -1;
// the end of the selection along the Y axis 
var ey = -1;

/**
 * PXN8 is a global object referenced throughout the PXN8 javascript code.
 */
var PXN8 = {
    /**
     * CONSTANTS
     */
    LANDSCAPE: 0,
    PORTRAIT: 1,
    
    ON_ZOOM_CHANGE: "ON_ZOOM_CHANGE",
    ON_SELECTION_CHANGE: "ON_SELECTION_CHANGE",
    ON_IMAGE_CHANGE: "ON_IMAGE_CHANGE",
    /**
     * A map of listeners by event type
     */
    listenersByType : {
        ON_ZOOM_CHANGE: [],
        ON_SELECTION_CHANGE: [],
        ON_IMAGE_CHANGE: []
    },
    /**
     * -- function select
     * -- description Selects an area of the image.
     * -- param startX The start position of the selected area along the X axis
     * -- param startY The start position of the selected area along the Y axis (starts at top)
     * -- param width The width of the selected area
     * -- param height The height of the selected area
     */
	 select: function (startX, startY, width, height){
        sx = startX;
        sy = startY;
        ex = sx + width;
        ey = sy + height;

        if (sx < 0) sx = 0;
        if (sy < 0) sy = 0;
        if (ex > PXN8.image.width) ex = PXN8.image.width;
        if (ey > PXN8.image.height) ey = PXN8.image.height;

        selectArea();
    },
    /**
     * select an area using an aspect ratio
     */
    selectByRatio: function(ratio){
        if (typeof ratio == "string"){
            var pair = /^([0-9]+)x([0-9]+)/;
            var match = ratio.match(pair);
            if (match != null){
                if (PXN8.image.width > PXN8.image.height){
                    PXN8.aspectRatio.width = match[2];
                    PXN8.aspectRatio.height = match[1];
                }else{
                    PXN8.aspectRatio.width = match[1];
                    PXN8.aspectRatio.height = match[2];

                }
            }else{
                PXN8.aspectRatio.width = 0;
                PXN8.aspectRatio.height = 0;
                return;
            }

            var topRect = id("pxn8_top_rect");
            topRect.style.borderWidth = "1px";
            
            var leftRect = id("pxn8_left_rect");
            leftRect.style.borderWidth = "0px";
            
            sx = 0;
            sy = 0;
    
            var t1 = PXN8.image.width / PXN8.aspectRatio.width ;
            var t2 = PXN8.image.height / PXN8.aspectRatio.height ;
            if (t2 < t1){
                ey = PXN8.image.height;
                ex = Math.round(ey / PXN8.aspectRatio.height * PXN8.aspectRatio.width);
            }else{
                ex = PXN8.image.width;
                ey = Math.round(ex / PXN8.aspectRatio.width * PXN8.aspectRatio.height);
            }
            sx = Math.round((PXN8.image.width - ex) / 2);
            sy = Math.round((PXN8.image.height - ey) / 2);
            ex += sx;
            ey += sy;
            selectArea();
        }
    },
    
    /**
     * -- function getUncompressedImage
     * -- description Returns the relative URL to the uncompressed 100% quality image
     */
	 getUncompressedImage: function(){
        return PXN8.responses[PXN8.opNumber].uncompressed;
    },

    /**
     * -- function rotateSelection
     * -- description Rotates the selection area by 90 degrees 
     */
    rotateSelection: function(){

        var sel = GetSelection();
        var cx = sel.x + (sel.w / 2);
        var cy = sel.y + (sel.h / 2);
        this.select (cx - sel.h/2, cy - sel.w /2, sel.h, sel.w);
    },
    
    /**
     * -- function addListener
     * -- description Adds a new callback function to the list of functions to be called when a PXN8 event occurs.
     * -- param eventType (ON_ZOOM_CHANGE, ON_IMAGE_CHANGE, ON_SELECTION_CHANGE etc)
     * -- param callback The function to be called when the event occurs
     */
    addListener: function (eventType,callback)
    {
        var callbacks = this.listenersByType[eventType];
        var found = false;
        if (!callbacks){
            callbacks = [];
            this.listenersByType[eventType] = callbacks;
        }
        
        for (var i = 0;i < callbacks.length; i++){
            if (callbacks[i] == callback){
                found = true;
                break;
            }
        }
        if (!found){
            callbacks.push (callback);
        }
        
    },
    /**
     * -- function removeListener
     * -- description Removes a callback function from the list of functions to be called when a PXN8 event occurs
     * -- param eventType (ON_ZOOM_CHANGE, ON_IMAGE_CHANGE, ON_SELECTION_CHANGE etc)
     * -- param callback The function to be removed.
     */
    removeListener: function (eventType, callback)
    {
        var callbacks = this.listenersByType[eventType];
        if (!callbacks) return;
        
        for (var i = 0;i < callbacks.length; i++){
            if (callbacks[i] == callback){
                callbacks.splice(i,1);
            }
        }
    },
    /**
     * -- function onceOnlyListener
     * -- description A special-case of listener that only performs once and once only.
     * -- param eventType (ON_ZOOM_CHANGE, ON_IMAGE_CHANGE, ON_SELECTION_CHANGE etc)
     * -- param callback The function to be called when the event occurs (only called once then removed from list)
     */
    onceOnlyListener: function (eventType,callback){
        var wrappedCallback = null;
        wrappedCallback = function(){
            callback();
            PXN8.removeListener(eventType,wrappedCallback);
        };
        PXN8.addListener(eventType, wrappedCallback);
    },
    
    /**
     * -- function notifyListeners
     * -- description Called by various methods to notify listeners  
     * -- param eventType (ON_ZOOM_CHANGE, ON_IMAGE_CHANGE, ON_SELECTION_CHANGE etc)
     */ 
    notifyListeners: function(eventType)
    {
        var listeners = this.listenersByType[eventType];
        if (listeners){
            for (var i = 0; i < listeners.length; i++){
                var listener = listeners[i];
                if (listener != null){
                    listener(eventType);
                }
                
            }
        }
    },
    
                                  
    /**
     * history stores all session operations
     */
    history: [],

    /**
     * images stores a list of all images indexed by opNumber
     * (used by PXN8.tools.history)
     */
    images: [],

    /**
     * An array of the response images returned from the server
     * This array contains relative file paths. 
     * It is updated in the  ahahDone() function.
     */
    responses: [],
    
    /*
     * The current image - it's width, height and location (URL)
     */
    image:  { 
        width: 0, 
        height: 0, 
        location: ""
    },
    
    /*
     * What is the current operation number ?
     */
    opNumber: 0,

    /**
     * what is the total number of operations performed ?
     */
    maxOpNumber: 0,
    
    /*
     * An object which tells what the current aspect ratio is 
     */
    aspectRatio: {width:0 , height:0},
    
    /**
     * The image orientation (used by crop tool)
     */
    orientation: this.PORTRAIT,
    
    /**
     * The current mouse position
     */
    position: {
        x: "-", 
        y: "-"
    },
    
    /**
     * The JSON response from the last image operation
     */
    response: {
        status: "", 
        image: "", 
        errorCode: 0, 
        errorMessage: "" 
    },

	 /**
     * replaceOnSave specifies how PXN8 handles image URLs
     * if set to true then PXN8 always assumes that the photo at the supplied URL has changed.
     * if set to false then PXN8 will assume that the photo at the supplied url hasn't changed since it was last retrieved.
     */
	 replaceOnSave: false,


    /**
     * If an operation is performed on an image then this is set to true
     * until the image update has completed
     */
    updating: false,
    
    /**
     * Called when the server returns an error code in the JSON response instead of an image
     */
    onImageFailed: function(){},

    /**
     * the log object has a single method 'append'
     * If your edit page has an element with id 'pxn8_log' then ...
     * PXN8.log.append('hello world') 
     * ... will append a new paragraph with 'hello world' as the text to the div.
     */
    log: { 
        append: function(str){
            var log = id("pxn8_log");
            if (log){
                ac(ac(log,ce("p")),tx(str));
            }
        }
    },

    /*
     * Zoom-related stuff
     */
    zooms: [0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2, 3, 4],
    zoomLevel: 3,
    zoomedBy: 1.0,

    magnification: function(){
        return this.zoomedBy;
    },
    canZoomIn: function(){
        return this.zoomedBy < this.zooms[this.zooms.length-1];
    },
    canZoomOut: function(){
        return this.zoomedBy > this.zooms[0];
    },
    zoomIn: function(){
        for (var i = 0; i < this.zooms.length;i++){
            if (this.zooms[i] > this.zoomedBy){
                this.zoomLevel = i;
                this.zoomedBy = this.zooms[i];
                break;
            }
        }
    },
    zoomOut: function(){
        for (var i = this.zooms.length-1; i >= 0; i--){
            if (this.zooms[i] < this.zoomedBy){
                this.zoomLevel = i;
                this.zoomedBy = this.zooms[i];
                break;
            }
        }
    },
    
    /*
     *
     */
    initialize: function(image_src){
    	_pxn8_initialize(image_src);
    },

    
    tools: {
        /* 
         * This is a placeholder for custom image ops
         * Please refer to pxn8_tools.js for details.
         */
    },

    /**
     * The upper bounds on image sizes
     */
    resizelimit: { width: 1600,
                   height: 1200},

    /**
     * Returns the style of an element based on it's external stylesheet,
     * and any inline styles.
     */
    computedStyle: function(elementId)
    {
        var result = null;
        
        if (this.cachedComputedStyles[elementId]){
            result = this.cachedComputedStyles[elementId];
        }else{
            var element = id(elementId);
            if (document.all){

                result = element.currentStyle;

            }else{
                if (window.getComputedStyle){
                    result = window.getComputedStyle(element,null);                
                }else{
                    /**
                     * Safari doesn't support getComputedStyle() 
                     */
                    result = element.style;
                }
            }
            this.cachedComputedStyles[elementId] = result;
        }
        return result;
    },

    /**
     * Computing the style is expensive.
     * cache computation results here.
     */
    cachedComputedStyles: {},

    /**
     * Style-related properties...
     */
    style: {
        /*
         * The style of the canvas area which is not currently selected.
         */
        notSelected: {
            opacity: 0.33,
            color: "black"
        },
        /*
         * The style of the resize grab handles
         */
        resizeHandles: {
            color: "white",
            size: 8
        }
    },
    /*
     * A hashtable of images with the image.src url as the key (value is 'true')
     * Need this for IE to force onload handler for images which 
     * have already been loaded.
     */
    imagesBySrc: {},

    /*
     * Return a list of all the operations which have been performed
     * (doesn't include undone operations)
     */
    getScript: function(){
        var result = new Array();
        for (var i = 0;i <= PXN8.opNumber; i++){
            var operation = {};
            for (var j in PXN8.history[i]){
                operation[j] = PXN8.history[i][j];
            }
            result.push(operation);
        }
        return result;
    },

    elementsByClass: function(className){
        var links = document.getElementsByTagName("*");
        
        var result = new Array();
        for (var i = 0;i < links.length; i++){
            if (links[i].className == className){
                result.push(links[i]);
            }
        }
        return result;
    }
    
};

/**
 * Create a pin for placing on top of an image
 */
function createPin(pinId,imgSrc)
{
    var pinElement = document.createElement("img");
    pinElement.id = pinId;
    pinElement.className = "pin";
    pinElement.src = imgSrc;
    pinElement.style.position = "absolute";
    pinElement.style.width = "24px";
    pinElement.style.height = "24px";
    return pinElement;
    
}


/*------------------------------- START DRAG ----------------------------*/
var dx = 0;
var dy = 0;

var beginDragX = 0;
var beginDragY = 0;

/* used when dragging selection */
var osx = 0;
var osy = 0;
var ow = 0;
var oh = 0;

function mousePointToElementPoint(mx,my)
{
    var canvas = id("pxn8_canvas");
    var imageBounds = eb(canvas);
    var scrolledPoint = GetScrolledPoint(mx,my);
    this.x = Math.round((scrolledPoint.x - imageBounds.x)/PXN8.magnification());
    this.y = Math.round((scrolledPoint.y - imageBounds.y)/PXN8.magnification());
    
    if (canvas.style.borderWidth){
        
        var borderWidth = parseInt(canvas.style.borderWidth);
        this.x -= borderWidth;
        this.y -= borderWidth;
        if (this.x < 0){
            this.x = 0;
        }
        if (this.y < 0){
            this.y = 0;
        }
    }
}

function GetScrolledPoint(x,y)
{
    var result = {"x":x,"y":y};

    if (ie){
        //result.x = x + document.body.scrollLeft;
        //result.y = y + document.body.scrollTop;
        
    }else{
        //result.x = x + window.pageXOffset;
        //result.y = y + window.pageYOffset;
    }
    var canvas = id("pxn8_canvas");
    if (canvas.parentNode.id == "pxn8_scroller"){
        var scroller = id("pxn8_scroller");
        result.x += scroller.scrollLeft;
        result.y += scroller.scrollTop;
    }

    return result;
}

function getCursorPosition(e) {
    e = e || window.event;
    var cursor = {x:0, y:0};
    if (e.pageX || e.pageY) {
        cursor.x = e.pageX;
        cursor.y = e.pageY;
    }
    else {
        cursor.x = e.clientX +
            (document.documentElement.scrollLeft ||
            document.body.scrollLeft) -
            document.documentElement.clientLeft;
        cursor.y = e.clientY +
            (document.documentElement.scrollTop ||
            document.body.scrollTop) -
            document.documentElement.clientTop;
    }
    return cursor;
}
function beginDrag (elementToDrag, event, moveHandler, upHandler)
{
    var elementBounds = eb(elementToDrag);

    var cursorPos = getCursorPosition(event);
    
    var scrolledPoint = GetScrolledPoint(cursorPos.x,cursorPos.y);

    
    beginDragX = scrolledPoint.x;
    beginDragY = scrolledPoint.y;
    
    dx = scrolledPoint.x - elementBounds.x;
    dy = scrolledPoint.y - elementBounds.y;
    
    osx = sx;
    osy = sy;
    ow = ex - sx;
    oh = ey - sy;
    
    if (document.addEventListener){
        document.addEventListener("mousemove", moveHandler, true);
        document.addEventListener("mouseup", upHandler, true);
    }else if (document.attachEvent){
        document.attachEvent("onmousemove",moveHandler);
        document.attachEvent("onmouseup",upHandler);
    }
    if (event.stopPropogation) event.stopPropogation();/* DOM Level 2 */
    else event.cancelBubble = true; /* IE */
   
    if (event.preventDefault) event.preventDefault(); /* DOM Level 2 */
    else event.returnValue = false; /*  IE */
}


function moveCanvasHandler(event)
{
    if (!event) event = window.event; /* IE */

    var canvasBounds = eb(id("pxn8_canvas"));
    
    var theImg = id("pxn8_image");

    var maxX = canvasBounds.x + theImg.width;
    var maxY = canvasBounds.y + theImg.height;
    
    var cursorPos = getCursorPosition(event);
    /*
     * prohibit move outside right and bottom
     */
    var scrolledPoint = GetScrolledPoint(cursorPos.x, cursorPos.y);
    
    var x2 = scrolledPoint.x>maxX?maxX:scrolledPoint.x; 
    x2 = x2 < canvasBounds.x?canvasBounds.x:x2;
    var y2 = scrolledPoint.y>maxY?maxY:scrolledPoint.y;
    y2 = y2 < canvasBounds.y?canvasBounds.y:y2;

    var numerical = function(a,b){
        return a-b;
    };
    var xVals = [beginDragX-canvasBounds.x,x2-canvasBounds.x].sort(numerical);
    var yVals = [beginDragY-canvasBounds.y,y2-canvasBounds.y].sort(numerical);
    
    var pixelWidth = xVals[1] - xVals[0];
    var pixelHeight = yVals[1] - yVals[0];
    
    var width = Math.round(pixelWidth / PXN8.magnification());
    var height = Math.round(pixelHeight / PXN8.magnification());
    
    height = height > PXN8.image.height?PXN8.image.height:height;
    width = width > PXN8.image.width?PXN8.image.width:width;
    if (width > PXN8.aspectRatio.width &&
        height > PXN8.aspectRatio.height &&
        PXN8.aspectRatio.width > 0){
        
        if (PXN8.aspectRatio.width > PXN8.aspectRatio.height){
            height = Math.round(width/PXN8.aspectRatio.width *PXN8.aspectRatio.height);
        }else{
            width = Math.round(height/PXN8.aspectRatio.height *PXN8.aspectRatio.width);
        }
    }
    
    sx = Math.round(xVals[0]/PXN8.magnification());
    ex = sx + width;
    
    sy = Math.round(yVals[0]/PXN8.magnification());
    ey = sy + height;

    selectArea();
    
    if (event.stopPropogation) event.stopPropogation(); /* DOM Level 2 */
    else event.cancelBubble = true; /*  IE */
}
/*
 * Handler passed to beginDrag when the user is dragging on the canvas.
 * This handler will be invoked on a mouseup event
 */
function upCanvasHandler(event)
{

	PXN8.log.append("aspect_ratio: width=" + PXN8.aspectRatio.width + ", height=" + PXN8.aspectRatio.height);
    if (!event) event = window.event ; /* IE */
    
    if (document.removeEventListener){
        document.removeEventListener("mouseup",upCanvasHandler,true);
        document.removeEventListener("mousemove",moveCanvasHandler, true);
    }else if (document.detachEvent){
        document.detachEvent("onmouseup",upCanvasHandler);
        document.detachEvent("onmousemove",moveCanvasHandler);
    }
    if (event.stopPropogation) event.stopPropogation(); /*  DOM Level 2 */
    else event.cancelBubble = true; /* IE */
    
}

function moveSelectionBoxHandler(event)
{
    if (!event) event = window.event; /* IE  */
    
    var canvasBounds = eb(id("pxn8_canvas"));
    var theImg = id("pxn8_image");
    
    var mx = canvasBounds.x + theImg.width;
    var my = canvasBounds.y + theImg.height;

    var cursorPos = getCursorPosition(event);
    var scrolledPoint = GetScrolledPoint(cursorPos.x, cursorPos.y);

    /* how much (in pixels) the cursor has moved */
    var rx = scrolledPoint.x - beginDragX;
    var ry = scrolledPoint.y - beginDragY;
    
    
    /* is it right of left border ? */
    sx = Math.round((osx + (rx/PXN8.magnification()))>0?(osx+(rx/PXN8.magnification())):0);
    
    sx = Math.round((sx+ow)>PXN8.image.width?(PXN8.image.width-ow):sx);
    /*  is it below the top border ? */
    sy = Math.round((osy + (ry/PXN8.magnification()))>0?(osy+(ry/PXN8.magnification())):0);
    
    sy = Math.round((sy+oh)>PXN8.image.height?(PXN8.image.height-oh):sy);
    
    ex = (sx + ow)>0?(sx+ow):0;
    ey = (sy + oh)>0?(sy+oh):0;
    
    if (event.stopPropogation) event.stopPropogation(); /* DOM Level 2 */
    else event.cancelBubble = true; /* IE */

    selectArea();

}

/*
 * Handler passed to beginDrag when the user is dragging the selection rect around.
 * This handler will be invoked on a mouseup event
 */
function upSelectionBoxHandler(event)
{
    if (!event) event = window.event ; /* IE */
    if (document.removeEventListener){
        document.removeEventListener("mouseup",upSelectionBoxHandler,true);
        document.removeEventListener("mousemove",moveSelectionBoxHandler, true);
    }else if (document.detachEvent){
        document.detachEvent("onmouseup",upSelectionBoxHandler);
        document.detachEvent("onmousemove",moveSelectionBoxHandler);
    }
    if (event.stopPropogation) event.stopPropogation(); /* DOM Level 2 */
    else event.cancelBubble = true; /* IE */
}
/* ------------------------------- END DRAG ---------------------------- */

function randomHex()
{
    return (Math.round(Math.random()*65535)).toString(16)
}

/*
 * Return a Rect that represents the current selection
 */
function GetSelection()
{
    var rect = {x: 0, y: 0, h:0, w: 0};
    
    rect.w = ex>sx?ex-sx:sx-ex;
    rect.h = ey>sy?ey-sy:sy-ey;
    rect.x = ex>sx?sx:ex;
    rect.y = ey>sy?sy:ey;
    rect.x = rect.x<0?0:rect.x;
    rect.y = rect.y<0?0:rect.y;     
    return rect;
}
/*
 * Given an element, calculate it's absolute position relative to 
 * the BODY element.
 * Returns an object with attributes x and y
 */
function GetElementPosition(elt)
{
   var tmpElt = elt;
   var posX = parseInt(tmpElt["offsetLeft"]);
   var posY = parseInt(tmpElt["offsetTop"]);
   while(tmpElt.tagName != "BODY") {
      tmpElt = tmpElt.offsetParent;
      posX += parseInt(tmpElt["offsetLeft"]);
      posY += parseInt(tmpElt["offsetTop"]);
   } 
   return {x: posX, y:posY};
} 

/*
 * Calculate the size of the browser window
 */
function GetWindowSize()
{
    if (ie){
        return {width: document.body.clientWidth, 
                height: document.body.clientHeight};
    }else{
        return {width: window.outerWidth,
                height: window.outerHeight};
    } 
}



/* 
 * Select the entire image
 */
function selectAll()
{
    sx = 0;
    sy = 0;
    ex = PXN8.image.width;
    ey = PXN8.image.height;
    selectArea(); 
}

/*
 * when the user clicks somewhere in the canvas
 */
function unselect ()
{
    //    hideResizeHandles();
    
    sx = 0;
    sy = 0;
    ex = 0;
    ey = 0;   
    var selectionDiv = id("pxn8_select_rect");
    selectionDiv.style.display = "none";
    
    
    var topRect = id("pxn8_top_rect");
    topRect.style.borderWidth = "1px";
    
    topRect.style.display = "none";
    
    var bottomRect = id("pxn8_bottom_rect");
    bottomRect.style.borderWidth = "1px";
    
    bottomRect.style.display = "none";
    
    var leftRect = id("pxn8_left_rect");
    leftRect.style.display = "none";
    leftRect.style.borderWidth = "0px";
    
    
    id("pxn8_right_rect").style.display = "none";
    
    /*
     * update the field values
     */  
    displayPositionInfo();

    displaySelectionInfo();
    
    PXN8.notifyListeners(PXN8.ON_SELECTION_CHANGE);
}

/*
 *
 */
function selectArea()
{
    var selectRect = id("pxn8_select_rect");
    var theImg = id("pxn8_image");
    var leftRect = id("pxn8_left_rect");
    var rightRect = id("pxn8_right_rect");
    var topRect = id("pxn8_top_rect");
    var bottomRect = id("pxn8_bottom_rect");
    /*
     * has any selection been made yet ?
     */
    if (sx <=0 && sy <= 0 && ex <= 0 && ey <= 0){
        selectRect.style.display = "none";
        leftRect.style.display = "none";
        rightRect.style.display = "none";
        topRect.style.display = "none";
        bottomRect.style.display = "none";

        PXN8.notifyListeners(PXN8.ON_SELECTION_CHANGE);
        
        return;
    }

    var t = ey > sy?sy:ey;
    var l = ex > sx?sx:ex;
    var w = ex > sx?ex-sx:sx-ex;
    var h = ey > sy?ey-sy:sy-ey;

    if (((ex * PXN8.magnification()) > theImg.width) ||
        ((ey * PXN8.magnification()) > theImg.height)){
        return;
    }
    
    leftRect.style.display = "block";
    leftRect.style.top = "0px";
    leftRect.style.left = "0px";
    leftRect.style.width = (sx * PXN8.magnification())+ "px";
    leftRect.style.height = theImg.height + "px";
    
    rightRect.style.display = "block";
    rightRect.style.top = "0px";
    rightRect.style.left = (ex * PXN8.magnification()) + "px";
    rightRect.style.width = (theImg.width - (ex * PXN8.magnification())) + "px";
    rightRect.style.height = theImg.height + "px";
    
    topRect.style.display = "block";
    topRect.style.top = "0px";
    topRect.style.left = (l* PXN8.magnification()) + "px";
    topRect.style.width = (w* PXN8.magnification()) + "px";
    topRect.style.height = (t* PXN8.magnification()) + "px";

    bottomRect.style.display = "block";
    bottomRect.style.top = ((t+h)* PXN8.magnification()) + "px";
    bottomRect.style.left = (l* PXN8.magnification()) + "px";
    bottomRect.style.width = (w* PXN8.magnification()) + "px";
    bottomRect.style.height = (theImg.height - (ey* PXN8.magnification())) + "px";
    
    
    selectRect.style.top  = (t* PXN8.magnification()) + "px";
    selectRect.style.left = (l* PXN8.magnification()) + "px";
    selectRect.style.width = (w* PXN8.magnification()) + "px";
    selectRect.style.height = (h* PXN8.magnification()) + "px";
    selectRect.style.display = "block";
    
    selectRect.style.zIndex = "100";
    /*
     * update the field values
     */  
    PXN8.position.x = l;
    PXN8.position.y = t;

    displayPositionInfo();
    displaySelectionInfo();
    
    PXN8.notifyListeners(PXN8.ON_SELECTION_CHANGE);
}



/*
 * This is a keypress handler,
 * The supplied function is only invoked if the Enter key is pressed.
 */
function CallOnEnter(evt,func)
{
    evt = (evt) ? evt: event;
    var charCode = (evt.charCode) ? evt.charCode :
        ((evt.which) ? evt.which : evt.keyCode);
    if (charCode == 13 || charCode == 3){
        if (func){
            func();
            return false;
        }
    }
    return true;
}

/*
 * Called when the user clicks 'fetch' or hits enter in the 
 * image url field.
 */
function loadImage(imageLoc)
{
    PXN8.image.location = imageLoc;
    
    PXN8.opNumber = 0;
    PXN8.maxOpNumber = 0;
    
    PXN8.history = new Array();

    var fetchOp = {operation: "fetch",
                   image: escape(escape(PXN8.image.location))};

    PXN8.history.push(fetchOp);
    
    if (PXN8.replaceOnSave){
        fetchOp.random = randomHex();
    }

    unselect();

    replaceImage(PXN8.image.location);
    
    postImageLoad();
}

/*
 * Adjust the current selection to match the current aspect ratio
 * settings. This is called by config redeye 
 */
function adjustCurrentSelection()
{
    if (ex - sx > 0 &&  ey - sy > 0){
        var width = ex - sx;
        var height = ey - sy;
        if (width > PXN8.aspectRatio.width &&
            height > PXN8.aspectRatio.height &&
            PXN8.aspectRatio.width > 0){
            if (PXN8.aspectRatio.width > PXN8.aspectRatio.height){
                height = Math.round(width/PXN8.aspectRatio.width * PXN8.aspectRatio.height);
                if (sy + height > PXN8.image.height){
                    height = PXN8.image.height - sy;
                    width = Math.round(height/PXN8.aspectRatio.height * PXN8.aspectRatio.width);
                }
            }else{
                width = Math.round(height/PXN8.aspectRatio.height * PXN8.aspectRatio.width);
                if (sx + width > PXN8.image.width){
                    width = PXN8.image.width - sx;
                    height = Math.round(width/PXN8.aspectRatio.width * PXN8.aspectRatio.height);
                }
            }
        }
        ex = sx + width;
        ey = sy + height;
        selectArea();
        
    }
}


/*
 * Perform save-to-disk operation
 */
function save()
{
    document.location = "/save.pl?image=" + PXN8.getUncompressedImage();
}

function pointParameter()
{
    var sel = GetSelection();
    return "&point=" + sel.w + "." + sel.h + "." +sel.x + "." + sel.y;
}


function displaySelectionInfo()
{
    var selectionField = id("pxn8_selection_size");
    if (selectionField){
        var text = "N/A";
        if (ex - sx > 0){
            text = (ex-sx) + "," + (ey-sy);
        }
        ac(cl(selectionField),tx(text));
    }
}
function displayPositionInfo()
{
    var posInfo = id("pxn8_mouse_pos");
    if (posInfo){
        var text = PXN8.position.x + "," + PXN8.position.y;
        ac(cl(posInfo),tx(text));
    }
}

function displayZoomInfo()
{
    var zoomInfo = id("pxn8_zoom");
    if (zoomInfo){
        var text = Math.round((PXN8.magnification() * 100)) + "%";
        ac(cl(zoomInfo),tx(text));
    }
}

/*
 * Sets up the mouse handlers for the canvas area
 * Some tools/operations might modify the canvas mouse behaviour
 * If they do so then they should call this method when the tool's
 * work is done or cancelled.
 */
function initializeCanvas ()
{

    var canvas = id("pxn8_canvas");

    canvas.onmousemove = function (event){ 
        if (!event) event = window.event;
	     var cursorPos = getCursorPosition(event);
        var imagePoint = new mousePointToElementPoint(cursorPos.x, cursorPos.y);
        PXN8.position.x = imagePoint.x;
        PXN8.position.y = imagePoint.y;
        displayPositionInfo();
        return true;
    };

    canvas.onmouseout = function (event){ 
        if (!event) event = window.event;
        PXN8.position.x = "-";
        PXN8.position.y = "-";
        displayPositionInfo();
    };
    canvas.onmousedown = function (event){
        if (!event) event = window.event;
        //hideResizeHandles();
        beginDrag(canvas,event,moveCanvasHandler,upCanvasHandler);
    };
    canvas.ondrag = function(){ 
        return false;
    };

    var computedCanvasStyle = PXN8.computedStyle("pxn8_canvas");

    var canvasPosition = null;
    
    if (computedCanvasStyle.getPropertyValue){
        canvasPosition = computedCanvasStyle.getPropertyValue("position");
    }else{
        if (!computedCanvasStyle.position){
            // position may not be available if 
            // computedStyle returns the inline style (on safari).
            //
            canvasPosition = "static";
        }else{
            canvasPosition = computedCanvasStyle.position;
        }
    }
    
    if (!canvasPosition || canvasPosition == "static"){
        // default the canvas position to relative
        canvas.style.position = "relative";
        canvas.style.top = "0px";
        canvas.style.left  = "0px";
    }
    //
    // the canvas should wrap tightly around the image
    // so that the canvas doesn't extend beyond the image,
    // set it's float css property if it hasn't already been set.
    //
    var floatProperty = "cssFloat";
    if (document.all){
        floatProperty = "styleFloat";
    }
    var floatValue = computedCanvasStyle[floatProperty];
    
    if (!floatValue || floatValue == "none"){
        canvas.style[floatProperty] = "left";
    }

    return canvas;
}

/*
 * Should be called on body load !
 */
function _pxn8_initialize(image_src) 
{
    createSelectionRect();

    var canvas = initializeCanvas();
    
    var rects = ["pxn8_top_rect","pxn8_bottom_rect","pxn8_left_rect","pxn8_right_rect"];
    for (var i = 0;i < rects.length; i++){
        var rect = id(rects[i]);
        if (!rect){
            rect = ac(canvas,ce("div",{id: rects[i]}));
        }

        rect.style.fontSize = "0px";
        if (!rect.style.backgroundColor){
            rect.style.backgroundColor = PXN8.style.notSelected.color;
        }
        rect.style.position = "absolute";
        if (!rect.style.opacity){
            fnOpacity(rect,PXN8.style.notSelected.opacity);
        }
        
        rect.style.top = "0px";
        rect.style.left = "0px";
        rect.style.width = "0px";
        rect.style.height = "0px";
        rect.style.display = "none";
        rect.style.zIndex = "1";

    }
    	
    PXN8.image.location = image_src;
    //alert("PXN8.image.location:" + PXN8.image.location);

    PXN8.opNumber = 0;
    PXN8.maxOpNumber = 0;
    
    PXN8.history = new Array();
    
    var fetchOp = {operation: "fetch", 
                   image: escape(escape(PXN8.image.location))
    };

    PXN8.history.push(fetchOp);
    

    if (PXN8.replaceOnSave){
        fetchOp.random = randomHex();
    }
    
    var pxn8image = id("pxn8_image");

    /**
     * Safari doesn't load the image immediately
     * so setting the PXN8.image.width & height variables
     * makes no sense until the image has loaded.
     * the following function gets called directly from within
     * this function but also from within the img.onload function
     * if no <img id="pxn8_image".../> element appears in the body
     * (if pxn8_image is created dynamically as is the case with a 
     * bare-bones html ).
     *
     */
    var onImageLoad = function(pxn8image)
    {
		//alert("pxn8image: " + pxn8image);
        PXN8.image.width =  pxn8image.width;
        PXN8.image.height = pxn8image.height;
		//alert("width: " + PXN8.image.width);
		//alert("height: " + PXN8.image.height);
        addImageToHistory(pxn8image.src);

        displaySizeInfo();
    };
    
    
    /**
     *  Initialize the image
     */
    if (!pxn8image){
        var imgContainer = id("pxn8_image_container");
        if (!imgContainer){
            imgContainer = ac(canvas,ce("div",{id: "pxn8_image_container"}));
        }
        //
        // this won't work for Safari.
        // it is recommended that the <img> tag always appears
        // inside the pxn8_image_container tag.
        //
        pxn8image = ac(imgContainer,ce("img",{id: "pxn8_image", src: PXN8.image.location}));
        pxn8image.onload = function(){
            onImageLoad(pxn8image);
        };

    }else{
        
    }
    

    /**
     * This is for the case where the img src attribute is
     * defined in the html
     */
    onImageLoad(pxn8image);
    
    /* initialize zoom info */
    displayZoomInfo();
    
}      



/**
 *
 */
function replaceImage(imageurl)
{
    var imageContainer = cl(id("pxn8_image_container"));
    
    ac(imageContainer,ce("img",{id: "pxn8_image", src: imageurl}));
    displaySizeInfo();
}

var fader = {
	values: [0.99,0.85, 0.70, 0.55, 0.40, 0.25, 0.10, 0],
	times:      [75, 75,  75,  75,  75,  75,  75,  75],
	i: 0,
	stopfadeout: false,

	init: function(){ this.i =0; this.stopfadeout = false;},

	cancelfadeout: function(){ this.stopfadeout = true; },

	fadeout: function(eltid,destroyOnFade){
       if (this.stopfadeout){
           return;
       }
       fnOpacity(id(eltid),this.values[this.i]);
       if (this.i < this.values.length -1 ){
           this.i++;
           setTimeout("fader.fadeout('" + eltid + "'," + destroyOnFade + ");",this.times[this.i]);
       }else{
           if (destroyOnFade){
               var node = id(eltid);
               // it's quite possible that the element has already been destroyed !
               if (!node){
                   return;
               }else{
                   var parent = node.parentNode;
                   parent.removeChild(node);
               }
           }
       }
       
	},
	fadein: function(eltid){
       try{
           if (this.i >= this.values.length){
               this.i = this.values.length - 1;
           }
           fnOpacity(id(eltid),this.values[this.i]);
           if (this.i > 0){
               this.i--;
               setTimeout("fader.fadein('" + eltid + "');",this.times[this.i]);
           }
       }catch(e){
           alert(e);
       }
	}
};
/**
 * Display a soft alert that disappears after a short time
 */
function softalert(message,duration)
{
    if (!duration){
        duration = 1000;
    }
    var warning = id("pxn8_warning");
    if (!warning){
        warning = ce("div",{id: "pxn8_warning",className: "warning"});
    }
    fnOpacity(warning,0.8);
    var imgPos = eb(id("pxn8_image"));
    warning.style.top = imgPos.y + "px";
    warning.style.left = imgPos.x + "px";
    warning.style.width  = imgPos.width + "px";
    
    ac(cl(warning),tx(message));
    ac(document.body,warning);
    
    setTimeout("fader.init();fader.fadeout('pxn8_warning',true);",duration);
}

/*
 * Called when the AJAX request has returned
 */
function ahahDone(target) 
{
    // only if req is "loaded"
    if (req.readyState == 2) {
        
    }
    if (req.readyState == 4) {

        // only if "OK"
        var targetDiv = id(target);
        if (req.status == 200) {
            PXN8.log.append(req.status);
            PXN8.log.append(req.responseText);
            var mr = req.responseText.match(/^\{status/);
            if (mr){

                PXN8.response = eval('('+ req.responseText + ')');
                if (PXN8.response.status == "OK"){

                    var newImageSrc = PXN8.response.image;
                    PXN8.responses[PXN8.opNumber] = PXN8.response;
                    //
                    // wph 20060513: Workaround for IE's over-aggressive
                    // image caching.
                    // see IE bugs # 4
                    // http://www.sourcelabs.com/blogs/ajb/2006/04/rocky_shoals_of_ajax_developme.html
                    if (ie){
                        newImageSrc += "?rnd=" + randomHex();
                    }

                    replaceImage(newImageSrc);
                }else{
                    alert(PXN8ERRORS[PXN8.response.errorCode]);
                    if (PXN8.onImageFailed){
                        PXN8.onImageFailed();
                    }
                }
            }else{
                //
                // The server returns 200 if the script dies.
                //
                alert(req.responseText);
            }
            
            
            
        } else {
            alert(PXN8STRINGS.WEB_SERVER_ERROR + req.statusText + ":" + req.responseText) ;
        }
        var timer = id("pxn8_timer");
        if (timer){
            timer.style.display = "none";
        }
        PXN8.updating = false;
        postImageLoad();
    }
}

function displaySizeInfo()
{
    var sizeInfo = id("pxn8_image_size");
    if (sizeInfo){
        var text = PXN8.image.width + "x" + PXN8.image.height;
        ac(cl(sizeInfo),tx(text));
    }
}
/*
 * This function is called at the end of ahahDone and at the end of loadImage
 */
function postImageLoad()
{
    var theImage = id("pxn8_image");
    theImage.onerror = function(){
        alert(PXN8STRINGS.IMAGE_ON_ERROR1 + theImage.src + PXN8STRINGS.IMAGE_ON_ERROR2);
        if (PXN8.onImageFailed){
            PXN8.onImageFailed();
        }
    };
    
    var onloadFunc = function()
    {
        
	    PXN8.log.append("image " + theImage.src + " has loaded");	     

        PXN8.image.width = theImage.width;
        PXN8.image.height = theImage.height;
        
        displaySizeInfo();
        
        addImageToHistory(theImage.src);
        
        if (sx > PXN8.image.width || 
            ex > PXN8.image.width || 
            sy > PXN8.image.height || 
            ey > PXN8.image.height)
        {
            unselect();
        }else{
            // the surrounding darkened rects might now 
            // extend beyond the bounds of the image 
            // so ...
            selectArea();
        }
        PXN8.imagesBySrc[theImage.src] = true;
        PXN8.notifyListeners(PXN8.ON_IMAGE_CHANGE);
        displayZoomInfo();        
    };
    //
    // IE Bug: If an image with the same URL has already been loaded
    // then the onload method is never called - need to explicitly call the
    // onloadFunc method so that listeners get notified etc.
    //
    if (PXN8.imagesBySrc[theImage.src]){
        onloadFunc();
    }else{
        theImage.onload = onloadFunc;
    }
    PXN8.zoomedBy = 1.0; /* 100% */
    displayZoomInfo();

}
/**
 * Create the selection area if it's not already defined.
 */
function createSelectionRect()
{
    var selectRect = id("pxn8_select_rect");
    if (!selectRect){
        var canvas = id("pxn8_canvas");
        selectRect = ac(canvas, ce("div", {id: "pxn8_select_rect"}));
        selectRect.style.backgroundColor = "white";
        fnOpacity(selectRect,0);
        selectRect.style.cursor = "move";
        selectRect.style.borderWidth  = "1px";
        selectRect.style.borderColor = "red";
        selectRect.style.borderStyle = "dotted";
        selectRect.style.position = "absolute";
        selectRect.style.zIndex = 1;
        selectRect.style.fontSize = "0px";
        selectRect.style.display = "block";
        selectRect.style.width = "0px";
        selectRect.style.height = "0px";


    }
    selectRect.onmousedown = function(event){ 
        if (!event) event = window.event;
        beginDrag(selectRect,event,moveSelectionBoxHandler,upSelectionBoxHandler);
    };
    return selectRect;
}


function addImageToHistory(imageLocation)
{
    PXN8.log.append(" addImageToHistory (" + imageLocation + " " + PXN8.image.width + "," + PXN8.image.height + ")");

    var item = {"location": imageLocation,
                "width": PXN8.image.width,
                "height": PXN8.image.height
    };
            
    PXN8.images[PXN8.opNumber] = item;

    for (var i = 0; i <= PXN8.maxOpNumber; i++){
        var item = PXN8.images[i];
        if (item){
            PXN8.log.append("-- [" +i+ "] " + item.location + " " + item.width + "," + item.height);
        }
    }
    PXN8.log.append("===========================");
    
}


/**
 * Resizing code makes extensive use of 'currying' (functions that return functions with
 * variables 'baked in'. If not for currying, this code would be way too long and repetitive.
 * walter higgins
 * 3 February 2006
 */
var resize_dx = 0;
var resize_dy = 0;

function canResizeNorth(yOffset){
    return (sy + yOffset < (ey-PXN8.style.resizeHandles.size)) && (sy + yOffset > 0);
}
function canResizeWest(xOffset){
    return (sx + xOffset < (ex-PXN8.style.resizeHandles.size)) && (sx + xOffset > 0);
}
function canResizeSouth(yOffset){
    return (ey + yOffset > (sy+PXN8.style.resizeHandles.size)) && (ey + yOffset < PXN8.image.height);
}
function canResizeEast(xOffset){
    return (ex + xOffset > (sx+PXN8.style.resizeHandles.size)) && (ex + xOffset < PXN8.image.width);
}

var nTest = function(xOffset,yOffset,event){

    if (canResizeNorth(yOffset))        // sy > 0
    {
        resize_dy = event.clientY;
        sy = Math.round(sy + yOffset);
        return true;
        
    }
    return false;
    
};
var sTest = function(xOffset,yOffset,event){

    if (canResizeSouth(yOffset))
    {
        resize_dy = event.clientY;
        ey = Math.round(ey + yOffset);
        return true;
    }
    return false;
};
var wTest = function(xOffset,yOffset,event){

    if (canResizeWest(xOffset))        // sx > 0
    {
        resize_dx = event.clientX;
        sx = Math.round(sx + xOffset);
        return true;

    }
    return false;
};
var eTest = function(xOffset,yOffset,event){

    if (canResizeEast(xOffset))
    {
        resize_dx = event.clientX;
        ex = Math.round(ex + xOffset);
        return true;
    }
    return false;
};

var nwTest = function(xOffset,yOffset,event)
{
    if (xOffset == 0 || yOffset == 0){
        return false;
    }
    var hr = resize_start_height/resize_start_width;
    var wr = 1 / hr;

    if (wr > hr){
        xOffset = yOffset * wr;
    }else if (wr < hr){
        yOffset = xOffset * hr;
    }else{
        yOffset = xOffset;
    }
    //
    // for NW corner
    // ensure both offsets are either negative or positive
    //
    if (xOffset > 0){
        // make Y positive if not already
        yOffset = Math.abs(yOffset);
    }else{
        // make y negative if not already
        yOffset = 0 - Math.abs(yOffset);
    }
    if (canResizeWest(xOffset) && canResizeNorth(yOffset))
    {
        resize_dx = event.clientX;
        resize_dy = event.clientY;
        sx = Math.round(sx + xOffset);
        sy = Math.round(sy + yOffset);
        return true;
    }
    return false;
}

var swTest = function(xOffset,yOffset,event)
{
    if (xOffset == 0 || yOffset == 0){
        return false;
    }
    var hr = resize_start_height/resize_start_width;
    var wr = 1 / hr;

    if (wr > hr){
        yOffset = xOffset * wr;
    }else{
        yOffset = xOffset;
    }

    //
    // for SW corner
    // ensure offset are +/-
    //
    if (xOffset > 0){
        // make Y negative if X is positive
        yOffset = 0 - Math.abs(yOffset);
    }else{
        // make y positive if X is negative
        yOffset = Math.abs(yOffset);
    }
    if (canResizeWest(xOffset) && canResizeSouth(yOffset))
    {
        resize_dx = event.clientX;
        resize_dy = event.clientY;
        sx = Math.round(sx + xOffset);
        ey = Math.round(ey + yOffset);
        return true;
    }
    return false;

}

var neTest = function(xOffset,yOffset,event)
{
    if (xOffset == 0 || yOffset == 0){
        return false;
    }
    var hr = resize_start_height/resize_start_width;
    var wr = 1 / hr;

    if (wr > hr){
        xOffset = yOffset * wr;
    }else{
        xOffset = yOffset;
    }
    //
    // for NE corner
    // ensure offset are +/-
    //
    if (yOffset > 0){
        // make Y negative if X is positive
        xOffset = 0 - Math.abs(xOffset);
    }else{
        // make y positive if X is negative
        xOffset = Math.abs(xOffset);
    }
    if (canResizeEast(xOffset) && canResizeNorth(yOffset))
    {  
        resize_dx = event.clientX;
        resize_dy = event.clientY;
        ex = Math.round(ex + xOffset);
        sy = Math.round(sy + yOffset);
        /*
        if (sy < 0){
            ex = ex - (0-sy);
            sy = 0;
            if (ex > PXN8.image.width){
                
            }
        }
        */
        
        return true;
    }
    return false;
}

var seTest = function(xOffset,yOffset,event)
{
    if (xOffset == 0 || yOffset == 0){
        return false;
    }
    var hr = resize_start_height/resize_start_width;
    var wr = 1 / hr;

    if (wr > hr){
        xOffset = yOffset * wr;
    }else{
        yOffset = xOffset;
    }
    //
    // for SE corner
    // ensure offsets are both + or -
    //
    if (xOffset > 0){
        // make Y positive if X is positive
        yOffset = Math.abs(yOffset);
    }else{
        // make y negative if X is negative
        yOffset = 0 - Math.abs(yOffset);
    }
    if (canResizeEast(xOffset) && canResizeSouth(yOffset))
    {
        resize_dx = event.clientX;
        resize_dy = event.clientY;
        ex = Math.round(ex + xOffset);
        ey = Math.round(ey + yOffset);
        return true;
    }
    return false;
}

function resizer(testFunc)
{
    
    var result = function(event){

        if (!event) event = window.event;
        var rdy = event.clientY - resize_dy;
        var rdx = event.clientX - resize_dx;
        var prdy = rdy * PXN8.magnification();
        var prdx = rdx * PXN8.magnification();
        if (prdx == 0 && prdy == 0){
            // do nothing
        }else{
            if (testFunc(prdx,prdy,event) == true){
                selectArea();
            }
        }
        
        if (event.stopPropogation) event.stopPropogation(); /* DOM Level 2 */
        else event.cancelBubble = true; /* IE */
    };
    
    return result;
}
/**
 * All of the resize handles are defined here
 */
var handles = {
    "n":  { moveHandler: resizer(nTest),
            position: function(handle,sel){
                handle.style.left = Math.round((sel.x + (sel.w/2)) * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y * PXN8.magnification())) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    },
    "s":  { moveHandler: resizer(sTest),
            position: function(handle,sel){
                handle.style.left = Math.round((sel.x + (sel.w/2)) * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y + (sel.h - PXN8.style.resizeHandles.size)) * PXN8.magnification()) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    },
    "e":  { moveHandler: resizer(eTest),
            position: function(handle,sel){
                handle.style.left = Math.round((sel.x + (sel.w - PXN8.style.resizeHandles.size)) * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y + (sel.h/2)) * PXN8.magnification()) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    },
    "w":  { moveHandler: resizer(wTest),
            position: function(handle,sel){
                handle.style.left = Math.round(sel.x * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y + (sel.h/2)) * PXN8.magnification()) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    },
    "nw": { moveHandler: resizer(nwTest),
            position: function(handle,sel){
                handle.style.left = Math.round(sel.x * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y * PXN8.magnification())) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    },
    "sw": { moveHandler: resizer(swTest),
            position: function(handle,sel){
                handle.style.left = Math.round(sel.x * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y + (sel.h - PXN8.style.resizeHandles.size)) * PXN8.magnification()) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    },
    "ne": { moveHandler: resizer(neTest),
            position: function(handle,sel){
                handle.style.left = Math.round((sel.x + (sel.w - PXN8.style.resizeHandles.size)) * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y * PXN8.magnification())) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    },
    "se": { moveHandler: resizer(seTest),
            position: function(handle,sel){
                handle.style.left = Math.round((sel.x + (sel.w - PXN8.style.resizeHandles.size)) * PXN8.magnification()) + "px";
                handle.style.top = Math.round((sel.y + (sel.h - PXN8.style.resizeHandles.size)) * PXN8.magnification()) + "px";
            	document.getElementById("x").value = handle.style.left;
            	document.getElementById("y").value = handle.style.top;
            }
    }
};

function stopResizing(event)
{
    if (!event) event = window.event ; /* IE */
    
    if (document.removeEventListener){
        document.removeEventListener("mouseup",stopResizing,true);
        for (var i in handles){
            document.removeEventListener("mousemove",handles[i].moveHandler, true);
        }
        
    }else if (document.detachEvent){
        document.detachEvent("onmouseup",stopResizing);
        for (var i in handles){
            document.detachEvent("onmousemove",handles[i].moveHandler);
        }
    }
    if (event.stopPropogation) event.stopPropogation(); /*  DOM Level 2 */
    else event.cancelBubble = true; /* IE */
}
var resize_start_width = 0;
var resize_start_height = 0;

/**
 * Returns a handler that get's called when the user 
 * mouses-down on one of the resize handlers
 */
function startResizing (hdlr)
{
    var result = function(event){

        if (!event) event = window.event;
    
        resize_dx = event.clientX;
        resize_dy = event.clientY;

        var sel = GetSelection();
        
        resize_start_height = sel.h;
        resize_start_width = sel.w;
        
        if (document.addEventListener){
            document.addEventListener("mousemove", hdlr, true);
            document.addEventListener("mouseup", stopResizing, true);
        }else if (document.attachEvent){
            document.attachEvent("onmousemove",hdlr);
            document.attachEvent("onmouseup",stopResizing);
        }
        if (event.stopPropogation) event.stopPropogation();/* DOM Level 2 */
        else event.cancelBubble = true; /* IE */
        
        if (event.preventDefault) event.preventDefault(); /* DOM Level 2 */
        else event.returnValue = false; /*  IE */
        
    };
    return result;
}

function createResizeHandle(direction,size,color)
{
    var result = document.createElement("div");
    result.id = direction + "_handle";
    result.style.backgroundColor = color;
    result.style.position = "absolute";
    result.style.width = size + "px";
    result.style.height = size + "px";
    result.style.overflow = "hidden"; // fixes IE
    result.style.zIndex = 999;
    result.style.cursor = direction + "-resize";
    result.onmousedown = startResizing(handles[direction].moveHandler);
    result.ondrag = function(){return false;};
    return result;
}

function positionResizeHandles()
{
    var sel = GetSelection();
	 
    if (sel.w == 0){
        hideResizeHandles();
        return;
    }
    var canvas = id("pxn8_canvas");
    
    for (var i in handles){
        var handle = id( i + "_handle");
        if (!handle){
            handle = createResizeHandle(i, PXN8.style.resizeHandles.size,
                                        PXN8.style.resizeHandles.color);
            ac(canvas,handle);
        }
        if (handle.style.display == "none"){
            handle.style.display = "block";
        }
        handles[i].position(handle,sel);
    }
}

function hideResizeHandles(hdls)
{
    if (hdls){
        for (var i =0; i < hdls.length;i++){
            var handle = id( i + "_handle");
            if (handle){
                handle.style.display = "none";
            }
        }
    }else{
        // hide all
        for (var i in handles){
            var handle = id( i + "_handle");
            if (handle){
                handle.style.display = "none";
            }
        }
    }
}

PXN8.addListener(PXN8.ON_SELECTION_CHANGE, positionResizeHandles);
PXN8.addListener(PXN8.ON_IMAGE_CHANGE, displayZoomInfo);


//
// DOM RELATED FUNCTIONS
//


/**
 * Removes all children from the specified node.
 * returns the passed in element
 * so that it can be called like this...
 *
 * cl(elt).appendChild(tx("hello"));
 * 
 * ... or this ...
 * 
 * var elt = cl(id("nodeid"));
 *
 */
function cl(elt)
{
    if (elt){
        while (elt.firstChild){
            elt.removeChild(elt.firstChild);
        }
    }
    return elt;
}
/**
 * shorthand for document.createTextNode()
 */
function tx(str)
{
    return document.createTextNode(str);
}

/**
 * Shorthand for document.getElementById
 */
function id(elementId)
{
	return document.getElementById(elementId);
}
/**
 * shorthand for document.createElement();
 */
function ce(nodeType, attributes)
{
    var element = document.createElement(nodeType);
    for (var i in attributes){
        element[i] = attributes[i];
    }
    return element;
}

/**
 * Add a new element of type nodeType with attributes to the 
 * parent node.
 */
function ap(parentNode, nodeType, attributes)
{
    //
    // for some strange reason, IE won't allow you set some
    // attributes once the element has been added to the DOM tree
    // 
    return ac(parentNode,ce(nodeType,attributes));
}
/**
 * set the opacity of an element 
 */
function fnOpacity(element, value)
{
    // it's quite possible that the element has been deleted
    // (see fader.fadeout( , destroyOnFade);
    //
    if (!element){
        return;
    }
	 if (document.all){
        element.style.filter = "alpha(opacity:" + (value*100) + ")";
    }else{
        element.style.opacity = value;
        element.style._moz_opacity = value;
    }
}
/*
 * Return the x,y, width & height of an element
 */
function eb(elt)
{
    var x = null;
    var y = null;
    
    if(elt.style.position == "absolute") 
    {
        x = parseInt(elt.style.left);
        y = parseInt(elt.style.top);
    } else {
        var pos = GetElementPosition(elt); 
        x = pos.x;
        y = pos.y;
    } 
    return {x: x, y: y, width: elt.offsetWidth, height: elt.offsetHeight};
} 
/**
 * shorthand for append child
 * returns the child not the parent
 */
function ac(parent, child)
{
    parent.appendChild(child);
    return child;
}
