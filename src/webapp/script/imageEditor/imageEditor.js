//minimized!
function findPosX(obj){
var curleft=0;
if (obj.offsetParent){
while (obj.offsetParent){
curleft += obj.offsetLeft
obj=obj.offsetParent;}
}
else if (obj.x)
curleft += obj.x;
return curleft;}
function startSlide(element,event,inputId,start,size){
var kids=element.getElementsByTagName("*");
var slider=undefined;
for (var i=0; i < kids.length; i++){
if (kids[i].className=="pxn8_slider"){
slider=kids[i];
break;}
}
slider.onmousemove=null;
var inputElement=document.getElementById(inputId);
element.onmousemove=function(evt){ return updateSlider(slider,inputElement,element,evt,start,size);};
element.onmouseup=function(){ element.onmousemove=null; };
updateSlider(slider,inputElement,element,event,start,size); }
function updateSlider(slider,inputElement,element, evt,start,size){ evt=(evt)?evt:window.event;
var px=findPosX(element);
var nx=evt.clientX-px;
if (nx <= 120 && nx >= 3){
slider.style.left=(nx-3)+"px";
var iv=start+(((nx-3) / 117 )*size);
inputElement.value=Math.round(iv,2);}
}
var gNamedColors=[];
gNamedColors.push(new PXN8Color("black",0,0,0,255));
gNamedColors.push(new PXN8Color("white",255,255,255,255));
gNamedColors.push(new PXN8Color("darkgray",169,169,169,255));
gNamedColors.push(new PXN8Color("darkslategray",47,79,79,255));
gNamedColors.push(new PXN8Color("dimgray",105,105,105,255));
gNamedColors.push(new PXN8Color("gainsboro",220,220,220,255));
gNamedColors.push(new PXN8Color("ghostwhite",248,248,255,255));
gNamedColors.push(new PXN8Color("gray",128,128,128,255));
gNamedColors.push(new PXN8Color("lightgrey",211,211,211,255));
gNamedColors.push(new PXN8Color("lightslategrey",119,136,153,255));
gNamedColors.push(new PXN8Color("silver",192,192,192,255));
gNamedColors.push(new PXN8Color("slategray",112,128,144,255));
gNamedColors.push(new PXN8Color("snow",255,250,250,255));
gNamedColors.push(new PXN8Color("whitesmoke",245,245,245,255));
gNamedColors.push(new PXN8Color("antiquewhite",250,235,215,255));
gNamedColors.push(new PXN8Color("cornsilk",255,248,220,255));
gNamedColors.push(new PXN8Color("floralwhite",255,250,240,255));
gNamedColors.push(new PXN8Color("ivory",255,240,240,255));
gNamedColors.push(new PXN8Color("linen",250,240,230,255));
gNamedColors.push(new PXN8Color("oldlace",253,245,230,255));
gNamedColors.push(new PXN8Color("papayawhip",255,239,213,255));
gNamedColors.push(new PXN8Color("seashell",255,245,238,255));
gNamedColors.push(new PXN8Color("honeydew",240,255,240,255));
gNamedColors.push(new PXN8Color("mintcream",245,255,250,255));
gNamedColors.push(new PXN8Color("mistyrose",255,228,225,255));
gNamedColors.push(new PXN8Color("bisque",255,228,196,255));
gNamedColors.push(new PXN8Color("beige",245,245,220,255));
gNamedColors.push(new PXN8Color("blanchedalmond",255,255,205,255));
gNamedColors.push(new PXN8Color("brown",165,42,42,255));
gNamedColors.push(new PXN8Color("burlywood",222,184,135,255));
gNamedColors.push(new PXN8Color("chocolate",210,105,30,255));
gNamedColors.push(new PXN8Color("moccasin",255,228,181,255));
gNamedColors.push(new PXN8Color("navajowhite",255,222,173,255));
gNamedColors.push(new PXN8Color("peru",205,133,63,255));
gNamedColors.push(new PXN8Color("rosybrown",188,143,143,255));
gNamedColors.push(new PXN8Color("sandybrown",244,164,96,255));
gNamedColors.push(new PXN8Color("sienna",160,82,45,255));
gNamedColors.push(new PXN8Color("tan",210,180,140,255));
gNamedColors.push(new PXN8Color("wheat",245,222,179,255));
gNamedColors.push(new PXN8Color("saddlebrown",139,69,19,255));
gNamedColors.push(new PXN8Color("darkorange",255,140,0,255));
gNamedColors.push(new PXN8Color("orange",255,165,0,255));
gNamedColors.push(new PXN8Color("orangered",255,69,0,255));
gNamedColors.push(new PXN8Color("peachpuff",255,218,185,255));
gNamedColors.push(new PXN8Color("darkgoldenrod",184,134,11,255));
gNamedColors.push(new PXN8Color("gold",255,215,0,255));
gNamedColors.push(new PXN8Color("goldenrod",218,165,32,255));
gNamedColors.push(new PXN8Color("lemonchiffon",255,250,205,255));
gNamedColors.push(new PXN8Color("lightgoldenrodyellow",250,250,210,255));
gNamedColors.push(new PXN8Color("lightyellow",255,255,224,255));
gNamedColors.push(new PXN8Color("palegoldenrod",238,232,170,255));
gNamedColors.push(new PXN8Color("yellow",255,255,0,255));
gNamedColors.push(new PXN8Color("greenyellow",173,255,47,255));
gNamedColors.push(new PXN8Color("lightgreen",144,238,144,255));
gNamedColors.push(new PXN8Color("lawngreen",124,252,0,255));
gNamedColors.push(new PXN8Color("teal",0,128,128,255));
gNamedColors.push(new PXN8Color("aquamarine",127,255,212,255));
gNamedColors.push(new PXN8Color("chartreuse",127,255,0,255));
gNamedColors.push(new PXN8Color("darkgreen",0,100,0,255));
gNamedColors.push(new PXN8Color("khaki",240,230,140,255));
gNamedColors.push(new PXN8Color("darkkhaki",189,183,107,255));
gNamedColors.push(new PXN8Color("darkolivegreen",85,107,47,255));
gNamedColors.push(new PXN8Color("darkseagreen",143,188,143,255));
gNamedColors.push(new PXN8Color("forestgreen",34,139,34,255));
gNamedColors.push(new PXN8Color("green",0,128,0,255));
gNamedColors.push(new PXN8Color("limegreen",50,205,50,255));
gNamedColors.push(new PXN8Color("seagreen",46,139,87,255));
gNamedColors.push(new PXN8Color("mediumseagreen",60,179,113,255));
gNamedColors.push(new PXN8Color("mediumspringgreen",0,250,154,255));
gNamedColors.push(new PXN8Color("lime ",0,255,0,255));
gNamedColors.push(new PXN8Color("olive",128,128,0,255));
gNamedColors.push(new PXN8Color("olivedrab",107,142,35,255));
gNamedColors.push(new PXN8Color("palegreen",152,251,152,255));
gNamedColors.push(new PXN8Color("springgreen",0,255,127,255));
gNamedColors.push(new PXN8Color("yellowgreen",154,205,50,255));
gNamedColors.push(new PXN8Color("aqua",0,255,255,255));
gNamedColors.push(new PXN8Color("cyan",0,255,255,255));
gNamedColors.push(new PXN8Color("darkcyan",0,139,139,255));
gNamedColors.push(new PXN8Color("darkturquoise",0,206,209,255));
gNamedColors.push(new PXN8Color("lightcyan",224,255,255,255));
gNamedColors.push(new PXN8Color("lightseagreen",32,178,170,255));
gNamedColors.push(new PXN8Color("mediumaquamarine",102,205,170,255));
gNamedColors.push(new PXN8Color("mediumturquoise",72,209,204,255));
gNamedColors.push(new PXN8Color("paleturquoise",175,238,238,255));
gNamedColors.push(new PXN8Color("turquoise",64,224,208,255));
gNamedColors.push(new PXN8Color("aliceblue",240,248,255,255));
gNamedColors.push(new PXN8Color("azure",240,255,255,255));
gNamedColors.push(new PXN8Color("deepskyblue",0,191,255,255));
gNamedColors.push(new PXN8Color("lightsteelblue",176,196,222,255));
gNamedColors.push(new PXN8Color("dodgerblue",30,144,255,255));
gNamedColors.push(new PXN8Color("powderblue",176,224,230,255));
gNamedColors.push(new PXN8Color("blue",0,0,255,255));
gNamedColors.push(new PXN8Color("cadetblue",95,158,160,255));
gNamedColors.push(new PXN8Color("royalblue",65,105,225,255));
gNamedColors.push(new PXN8Color("cornflowerblue",100,149,237,255));
gNamedColors.push(new PXN8Color("lightblue",173,216,230,255));
gNamedColors.push(new PXN8Color("steelblue",70,130,180,255));
gNamedColors.push(new PXN8Color("mediumblue",0,0,205,255));
gNamedColors.push(new PXN8Color("mediumslateblue",123,104,238,255));
gNamedColors.push(new PXN8Color("skyblue",135,206,235,255));
gNamedColors.push(new PXN8Color("slateblue",106,90,205,255));
gNamedColors.push(new PXN8Color("midnightblue",25,25,112,255));
gNamedColors.push(new PXN8Color("darkblue",0,0,139,255));
gNamedColors.push(new PXN8Color("indigo",75,0,130,255));
gNamedColors.push(new PXN8Color("darkslateblue",72,61,139,255));
gNamedColors.push(new PXN8Color("navy",0,0,128,255));
gNamedColors.push(new PXN8Color("blueviolet",138,43,226,255));
gNamedColors.push(new PXN8Color("mediumpurple",147,112,219,255));
gNamedColors.push(new PXN8Color("darkmagenta",139,0,139,255));
gNamedColors.push(new PXN8Color("violet",238,130,238,255));
gNamedColors.push(new PXN8Color("lavender",230,230,250,255));
gNamedColors.push(new PXN8Color("lavenderblush",255,240,245,255));
gNamedColors.push(new PXN8Color("mediumorchid",186,85,211,255));
gNamedColors.push(new PXN8Color("mediumvioletred",199,21,133,255));
gNamedColors.push(new PXN8Color("thistle",216,191,216,255));
gNamedColors.push(new PXN8Color("orchid",218,112,214,255));
gNamedColors.push(new PXN8Color("plum",221,160,221,255));
gNamedColors.push(new PXN8Color("purple",128,0,128,255));
gNamedColors.push(new PXN8Color("darkorchid",153,50,204,255));
gNamedColors.push(new PXN8Color("pink",255,192,203,255));
gNamedColors.push(new PXN8Color("salmon",250,128,114,255));
gNamedColors.push(new PXN8Color("coral",255,127,80,255));
gNamedColors.push(new PXN8Color("fuchsia",255,0,255,255));
gNamedColors.push(new PXN8Color("magenta",255,0,255,255));
gNamedColors.push(new PXN8Color("hotpink",255,105,180,255));
gNamedColors.push(new PXN8Color("lightcoral",240,128,128,255));
gNamedColors.push(new PXN8Color("darksalmon",233,150,122,255));
gNamedColors.push(new PXN8Color("lightpink",255,182,193,255));
gNamedColors.push(new PXN8Color("darkred",139,0,0,255));
gNamedColors.push(new PXN8Color("firebrick",178,34,34,255));
gNamedColors.push(new PXN8Color("red",225,0,0,255));
gNamedColors.push(new PXN8Color("maroon",128,0,0,255));
gNamedColors.push(new PXN8Color("crimson",220,20,60,255));
gNamedColors.push(new PXN8Color("indianred",205,92,92,255));
gNamedColors.push(new PXN8Color("tomato",253,99,71,255));
function PXN8Color(name, r, g, b, a){
this.name=name;
this.r=r;
this.g=g;
this.b=b;
this.a=a;}
PXN8Color.prototype.hexValue=function(){
var hr=this.r.toString(16);
var hg=this.g.toString(16);
var hb=this.b.toString(16);
return "#"+(this.r<16?("0"+hr):hr)+(this.g<16?("0"+hg):hg)+(this.b<16?("0"+hb):hb);};
function createColorPicker(initialColor,initialOpacity,callback){
var form=document.createElement("form");
form.onsubmit=function(){return false;};
var tBody=ac(ac(form,ce("table",{width: "100%"})),ce("tbody"));
var testCard=ac(ac(ac(tBody,ce("tr")),ce("td")),ce("img",{src: "images/testcard.png"}));
testCard.onmousemove=function(event){
if (!event) event=window.event;
colorTableMove('color_at_point',testCard,event);};
testCard.onclick=function(event){
colorTableClick(testCard,
event,
id('color'),
id('opacity'),
id('pxn8_colorwell'));};
var r1c0=ac(ac(tBody,ce("tr")),ce("td",{id: "color_at_point"}));
ac(r1c0,tx(".::."));
var r2c0=ac(ac(tBody,ce("tr")),ce("td"));
var tbody2=ac(ac(r2c0,ce("table")),ce("tbody"));
if (initialOpacity > -1){
var irow0=ac(tbody2,ce("tr"));
ac(ac(irow0,ce("td")),tx(PXN8STRINGS.OPACITY_LABEL));
ac(ac(irow0,ce("td")),ce("input",{id: "opacity",
name: "opacity",
className: "pxn8_small_field",
type: "text"}));
ac(ac(irow0,ce("td")),tx("%"));}
var irow1=ac(tbody2,ce("tr"));
ac(ac(irow1,ce("td")),tx(PXN8STRINGS.COLOR_LABEL));
ac(ac(irow1,ce("td")),ce("input",{id: "color", name: "color", type: "text",
className: "pxn8_small_field", type: "text"}));
ac(ac(irow1,ce("td", {width: 24})),ce("div",{className: "pxn8_colorwell",
id: "pxn8_colorwell"})); return form;}
function setupColorPickerBehaviour(initialColor,initialOpacity,callback){
if (initialOpacity > -1){
var opacity=id("opacity");
opacity.value=initialOpacity;
opacity.onfocus=function(){ this.select();};
opacity.onkeypress=function(event){ return CallOnEnter(event,callback);}
}
var color=id("color");
color.value=initialColor;
color.onfocus=function(){ this.select(); };
var cwell=id("pxn8_colorwell");
cwell.style.backgroundColor=initialColor;
cwell.style.color=initialColor;
color.onblur=function(){ cwell.style.backgroundColor=color.value;
cwell.style.color=color.value;}
color.onkeypress=function(event){ return CallOnEnter(event,callback);}
}
function colorAtPoint(element, mx,my){
var imageBounds=eb(element);
var scrolledPoint=GetScrolledPoint(mx,my);
var tx=Math.round((scrolledPoint.x-imageBounds.x));
var ty=Math.round((scrolledPoint.y-imageBounds.y));
var row=Math.floor(ty / 15);
var col=Math.floor(tx / 15);
var i=(row*12)+col;
if (row==11 && col > 2){
var alphaOffset=tx-45;
var opacity=Math.round(100-(100*alphaOffset/135));
return opacity;}else{
return gNamedColors[i];}
}
function colorTableMove(infoElementId,element, event){
if (!event) event=window.event;
var info=id(infoElementId);
var colAtPoint=colorAtPoint(element, event.clientX,event.clientY);
if (colAtPoint instanceof PXN8Color){
ac(cl(info),tx(colAtPoint.name+" "+colAtPoint.hexValue()));}else{
ac(cl(info),tx(PXN8STRINGS.OPACITY_LABEL+colAtPoint+"%"));}
return true;}
function colorTableClick(testCard, event, colorInput, opacityInput, colorWell){
if (!event) event=window.event;
var colAtPoint=colorAtPoint(testCard, event.clientX,event.clientY);
if (colAtPoint instanceof PXN8Color){
colorInput.value=colAtPoint.hexValue();
if (colorWell){
colorWell.style.color=""+colAtPoint.hexValue();
colorWell.style.backgroundColor=""+colAtPoint.hexValue();}
}else{
if (opacityInput){
opacityInput.value=colAtPoint;}
}
return true;}
var moz=((document.all)? false:true); var ie=((document.all)? true:false);
var sx=-1;
var sy=-1;
var ex=-1;
var ey=-1;

var PXN8={
	LANDSCAPE: 0,
	PORTRAIT: 1,
	ON_ZOOM_CHANGE: "ON_ZOOM_CHANGE",
	ON_SELECTION_CHANGE: "ON_SELECTION_CHANGE",
	ON_IMAGE_CHANGE: "ON_IMAGE_CHANGE",
	listenersByType:{
	ON_ZOOM_CHANGE: [],
	ON_SELECTION_CHANGE: [],
	ON_IMAGE_CHANGE: []
},
select: function (startX, startY, width, height){
	sx=startX;
	sy=startY;
	ex=sx+width;
	ey=sy+height;
	if (sx < 0) sx=0;
	if (sy < 0) sy=0;
	if (ex > PXN8.image.width) ex=PXN8.image.width;
	if (ey > PXN8.image.height) ey=PXN8.image.height;
	selectArea();},
	selectByRatio: function(ratio){
	if (typeof ratio=="string"){
	var pair=/^([0-9]+)x([0-9]+)/;
	var match=ratio.match(pair);
	if (match != null){
	if (PXN8.image.width > PXN8.image.height){
	PXN8.aspectRatio.width=match[2];
	PXN8.aspectRatio.height=match[1];}else{
	PXN8.aspectRatio.width=match[1];
	PXN8.aspectRatio.height=match[2];}
	}else{
	PXN8.aspectRatio.width=0;
	PXN8.aspectRatio.height=0;
	return;}
	var topRect=id("pxn8_top_rect");
	topRect.style.borderWidth="1px";
	var leftRect=id("pxn8_left_rect");
	leftRect.style.borderWidth="0px";
	sx=0;
	sy=0;
	var t1=PXN8.image.width / PXN8.aspectRatio.width ;
	var t2=PXN8.image.height / PXN8.aspectRatio.height ;
	if (t2 < t1){
	ey=PXN8.image.height;
	ex=Math.round(ey / PXN8.aspectRatio.height*PXN8.aspectRatio.width);}else{
	ex=PXN8.image.width;
	ey=Math.round(ex / PXN8.aspectRatio.width*PXN8.aspectRatio.height);}
	sx=Math.round((PXN8.image.width-ex) / 2);
	sy=Math.round((PXN8.image.height-ey) / 2);
	ex += sx;
	ey += sy;
	selectArea();}
},
getUncompressedImage: function(){
return PXN8.responses[PXN8.opNumber].uncompressed;},
rotateSelection: function(){
var sel=GetSelection();
var cx=sel.x+(sel.w / 2);
var cy=sel.y+(sel.h / 2);
this.select (cx-sel.h/2, cy-sel.w /2, sel.h, sel.w);},
addListener: function (eventType,callback){
var callbacks=this.listenersByType[eventType];
var found=false;
if (!callbacks){
callbacks=[];
this.listenersByType[eventType]=callbacks;}
for (var i=0;i < callbacks.length; i++){
if (callbacks[i]==callback){
found=true;
break;}
}
if (!found){
callbacks.push (callback);}
},
removeListener: function (eventType, callback){
var callbacks=this.listenersByType[eventType];
if (!callbacks) return;
for (var i=0;i < callbacks.length; i++){
if (callbacks[i]==callback){
callbacks.splice(i,1);}
}
},
onceOnlyListener: function (eventType,callback){
var wrappedCallback=null;
wrappedCallback=function(){
callback();
PXN8.removeListener(eventType,wrappedCallback);};
PXN8.addListener(eventType, wrappedCallback);},
 notifyListeners: function(eventType){
var listeners=this.listenersByType[eventType];
if (listeners){
for (var i=0; i < listeners.length; i++){
var listener=listeners[i];
if (listener != null){
listener(eventType);}
}
}
},
history: [],
images: [],
responses: [],
image: { width: 0, height: 0, location: ""
},
opNumber: 0,
maxOpNumber: 0,
aspectRatio: {width:0 , height:0},
orientation: this.PORTRAIT,
position: {
x: "-", y: "-"
},
response: {
status: "", image: "", errorCode: 0, errorMessage: "" },
replaceOnSave: false,
updating: false,
onImageFailed: function(){},
log: { append: function(str){
var log=id("pxn8_log");
if (log){
ac(ac(log,ce("p")),tx(str));}
}
},
zooms: [0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2, 3, 4],
zoomLevel: 3,
zoomedBy: 1.0,
magnification: function(){
return this.zoomedBy;},
canZoomIn: function(){
return this.zoomedBy < this.zooms[this.zooms.length-1];},
canZoomOut: function(){
return this.zoomedBy > this.zooms[0];},
zoomIn: function(){
for (var i=0; i < this.zooms.length;i++){
if (this.zooms[i] > this.zoomedBy){
this.zoomLevel=i;
this.zoomedBy=this.zooms[i];
break;}
}
},
zoomOut: function(){
for (var i=this.zooms.length-1; i >= 0; i--){
if (this.zooms[i] < this.zoomedBy){
this.zoomLevel=i;
this.zoomedBy=this.zooms[i];
break;}
}
},

initialize: function(image_src)
{
_pxn8_initialize(image_src);},
tools: {
},
resizelimit: { width: 1600,
height: 1200},
computedStyle: function(elementId){
var result=null;
if (this.cachedComputedStyles[elementId]){
result=this.cachedComputedStyles[elementId];}else{
var element=id(elementId);
if (document.all){
result=element.currentStyle;}else{
if (window.getComputedStyle){
result=window.getComputedStyle(element,null); }else{
result=element.style;}
}
this.cachedComputedStyles[elementId]=result;}
return result;},
cachedComputedStyles: {},
style: {
notSelected: {
opacity: 0.33,
color: "black"
},
resizeHandles: {
color: "white",
size: 8
}
},
imagesBySrc: {},
getScript: function(){
var result=new Array();
for (var i=0;i <= PXN8.opNumber; i++){
var operation={};
for (var j in PXN8.history[i]){
operation[j]=PXN8.history[i][j];}
result.push(operation);}
return result;},
elementsByClass: function(className){
var links=document.getElementsByTagName("*");
var result=new Array();
for (var i=0;i < links.length; i++){
if (links[i].className==className){
result.push(links[i]);}
}
return result;}
};
function createPin(pinId,imgSrc){
var pinElement=document.createElement("img");
pinElement.id=pinId;
pinElement.className="pin";
pinElement.src=imgSrc;
pinElement.style.position="absolute";
pinElement.style.width="24px";
pinElement.style.height="24px";
return pinElement;}
var dx=0;
var dy=0;
var beginDragX=0;
var beginDragY=0;
var osx=0;
var osy=0;
var ow=0;
var oh=0;
function mousePointToElementPoint(mx,my){
var canvas=id("pxn8_canvas");
var imageBounds=eb(canvas);
var scrolledPoint=GetScrolledPoint(mx,my);
this.x=Math.round((scrolledPoint.x-imageBounds.x)/PXN8.magnification());
this.y=Math.round((scrolledPoint.y-imageBounds.y)/PXN8.magnification());
if (canvas.style.borderWidth){
var borderWidth=parseInt(canvas.style.borderWidth);
this.x -= borderWidth;
this.y -= borderWidth;
if (this.x < 0){
this.x=0;}
if (this.y < 0){
this.y=0;}
}
}
function GetScrolledPoint(x,y){
var result={"x":x,"y":y};
if (ie){
}else{
}
var canvas=id("pxn8_canvas");
if (canvas.parentNode.id=="pxn8_scroller"){
var scroller=id("pxn8_scroller");
result.x += scroller.scrollLeft;
result.y += scroller.scrollTop;}
return result;}
function getCursorPosition(e) {
e=e || window.event;
var cursor={x:0, y:0};
if (e.pageX || e.pageY) {
cursor.x=e.pageX;
cursor.y=e.pageY;}
else {
cursor.x=e.clientX+(document.documentElement.scrollLeft ||
document.body.scrollLeft)-document.documentElement.clientLeft;
cursor.y=e.clientY+(document.documentElement.scrollTop ||
document.body.scrollTop)-document.documentElement.clientTop;}
return cursor;}
function beginDrag (elementToDrag, event, moveHandler, upHandler){
var elementBounds=eb(elementToDrag);
var cursorPos=getCursorPosition(event);
var scrolledPoint=GetScrolledPoint(cursorPos.x,cursorPos.y);
beginDragX=scrolledPoint.x;
beginDragY=scrolledPoint.y;
dx=scrolledPoint.x-elementBounds.x;
dy=scrolledPoint.y-elementBounds.y;
osx=sx;
osy=sy;
ow=ex-sx;
oh=ey-sy;
if (document.addEventListener){
document.addEventListener("mousemove", moveHandler, true);
document.addEventListener("mouseup", upHandler, true);}else if (document.attachEvent){
document.attachEvent("onmousemove",moveHandler);
document.attachEvent("onmouseup",upHandler);}
if (event.stopPropogation) event.stopPropogation();
else event.cancelBubble=true; 
if (event.preventDefault) event.preventDefault(); 
else event.returnValue=false; 
}
function moveCanvasHandler(event){
if (!event) event=window.event; 
var canvasBounds=eb(id("pxn8_canvas"));
var theImg=id("pxn8_image");
var maxX=canvasBounds.x+theImg.width;
var maxY=canvasBounds.y+theImg.height;
var cursorPos=getCursorPosition(event);
var scrolledPoint=GetScrolledPoint(cursorPos.x, cursorPos.y);
var x2=scrolledPoint.x>maxX?maxX:scrolledPoint.x; x2=x2 < canvasBounds.x?canvasBounds.x:x2;
var y2=scrolledPoint.y>maxY?maxY:scrolledPoint.y;
y2=y2 < canvasBounds.y?canvasBounds.y:y2;
var numerical=function(a,b){
return a-b;};
var xVals=[beginDragX-canvasBounds.x,x2-canvasBounds.x].sort(numerical);
var yVals=[beginDragY-canvasBounds.y,y2-canvasBounds.y].sort(numerical);
var pixelWidth=xVals[1]-xVals[0];
var pixelHeight=yVals[1]-yVals[0];
var width=Math.round(pixelWidth / PXN8.magnification());
var height=Math.round(pixelHeight / PXN8.magnification());
height=height > PXN8.image.height?PXN8.image.height:height;
width=width > PXN8.image.width?PXN8.image.width:width;
if (width > PXN8.aspectRatio.width &&
height > PXN8.aspectRatio.height &&
PXN8.aspectRatio.width > 0){
if (PXN8.aspectRatio.width > PXN8.aspectRatio.height){
height=Math.round(width/PXN8.aspectRatio.width *PXN8.aspectRatio.height);}else{
width=Math.round(height/PXN8.aspectRatio.height *PXN8.aspectRatio.width);}
}
sx=Math.round(xVals[0]/PXN8.magnification());
ex=sx+width;
sy=Math.round(yVals[0]/PXN8.magnification());
ey=sy+height;
selectArea();
if (event.stopPropogation) event.stopPropogation(); 
else event.cancelBubble=true; 
}
function upCanvasHandler(event){
PXN8.log.append("aspect_ratio: width="+PXN8.aspectRatio.width+", height="+PXN8.aspectRatio.height);
if (!event) event=window.event ; 
if (document.removeEventListener){
document.removeEventListener("mouseup",upCanvasHandler,true);
document.removeEventListener("mousemove",moveCanvasHandler, true);}else if (document.detachEvent){
document.detachEvent("onmouseup",upCanvasHandler);
document.detachEvent("onmousemove",moveCanvasHandler);}
if (event.stopPropogation) event.stopPropogation(); 
else event.cancelBubble=true; 
}
function moveSelectionBoxHandler(event){
if (!event) event=window.event; 
var canvasBounds=eb(id("pxn8_canvas"));
var theImg=id("pxn8_image");
var mx=canvasBounds.x+theImg.width;
var my=canvasBounds.y+theImg.height;
var cursorPos=getCursorPosition(event);
var scrolledPoint=GetScrolledPoint(cursorPos.x, cursorPos.y);
var rx=scrolledPoint.x-beginDragX;
var ry=scrolledPoint.y-beginDragY;
sx=Math.round((osx+(rx/PXN8.magnification()))>0?(osx+(rx/PXN8.magnification())):0);
sx=Math.round((sx+ow)>PXN8.image.width?(PXN8.image.width-ow):sx);
sy=Math.round((osy+(ry/PXN8.magnification()))>0?(osy+(ry/PXN8.magnification())):0);
sy=Math.round((sy+oh)>PXN8.image.height?(PXN8.image.height-oh):sy);
ex=(sx+ow)>0?(sx+ow):0;
ey=(sy+oh)>0?(sy+oh):0;
if (event.stopPropogation) event.stopPropogation(); 
else event.cancelBubble=true; 
selectArea();}
function upSelectionBoxHandler(event){
if (!event) event=window.event ; 
if (document.removeEventListener){
document.removeEventListener("mouseup",upSelectionBoxHandler,true);
document.removeEventListener("mousemove",moveSelectionBoxHandler, true);}else if (document.detachEvent){
document.detachEvent("onmouseup",upSelectionBoxHandler);
document.detachEvent("onmousemove",moveSelectionBoxHandler);}
if (event.stopPropogation) event.stopPropogation(); 
else event.cancelBubble=true; 
}
function randomHex(){
return (Math.round(Math.random()*65535)).toString(16)
}
function GetSelection(){
var rect={x: 0, y: 0, h:0, w: 0};
rect.w=ex>sx?ex-sx:sx-ex;
rect.h=ey>sy?ey-sy:sy-ey;
rect.x=ex>sx?sx:ex;
rect.y=ey>sy?sy:ey;
rect.x=rect.x<0?0:rect.x;
rect.y=rect.y<0?0:rect.y; return rect;}
function GetElementPosition(elt){
var tmpElt=elt;
var posX=parseInt(tmpElt["offsetLeft"]);
var posY=parseInt(tmpElt["offsetTop"]);
while(tmpElt.tagName != "BODY") {
tmpElt=tmpElt.offsetParent;
posX += parseInt(tmpElt["offsetLeft"]);
posY += parseInt(tmpElt["offsetTop"]);} return {x: posX, y:posY};} 
function GetWindowSize(){
if (ie){
return {width: document.body.clientWidth, height: document.body.clientHeight};}else{
return {width: window.outerWidth,
height: window.outerHeight};} }
function selectAll(){
sx=0;
sy=0;
ex=PXN8.image.width;
ey=PXN8.image.height;
selectArea(); }
function unselect (){
sx=0;
sy=0;
ex=0;
ey=0; var selectionDiv=id("pxn8_select_rect");
selectionDiv.style.display="none";
var topRect=id("pxn8_top_rect");
topRect.style.borderWidth="1px";
topRect.style.display="none";
var bottomRect=id("pxn8_bottom_rect");
bottomRect.style.borderWidth="1px";
bottomRect.style.display="none";
var leftRect=id("pxn8_left_rect");
leftRect.style.display="none";
leftRect.style.borderWidth="0px";
id("pxn8_right_rect").style.display="none";
 displayPositionInfo();
displaySelectionInfo();
PXN8.notifyListeners(PXN8.ON_SELECTION_CHANGE);}
function selectArea(){
var selectRect=id("pxn8_select_rect");
var theImg=id("pxn8_image");
var leftRect=id("pxn8_left_rect");
var rightRect=id("pxn8_right_rect");
var topRect=id("pxn8_top_rect");
var bottomRect=id("pxn8_bottom_rect");
if (sx <=0 && sy <= 0 && ex <= 0 && ey <= 0){
selectRect.style.display="none";
leftRect.style.display="none";
rightRect.style.display="none";
topRect.style.display="none";
bottomRect.style.display="none";
PXN8.notifyListeners(PXN8.ON_SELECTION_CHANGE);
return;}
var t=ey > sy?sy:ey;
var l=ex > sx?sx:ex;
var w=ex > sx?ex-sx:sx-ex;
var h=ey > sy?ey-sy:sy-ey;
if (((ex*PXN8.magnification()) > theImg.width) ||
((ey*PXN8.magnification()) > theImg.height)){
return;}
leftRect.style.display="block";
leftRect.style.top="0px";
leftRect.style.left="0px";
leftRect.style.width=(sx*PXN8.magnification())+ "px";
leftRect.style.height=theImg.height+"px";
rightRect.style.display="block";
rightRect.style.top="0px";
rightRect.style.left=(ex*PXN8.magnification())+"px";
rightRect.style.width=(theImg.width-(ex*PXN8.magnification()))+"px";
rightRect.style.height=theImg.height+"px";
topRect.style.display="block";
topRect.style.top="0px";
topRect.style.left=(l* PXN8.magnification())+"px";
topRect.style.width=(w* PXN8.magnification())+"px";
topRect.style.height=(t* PXN8.magnification())+"px";
bottomRect.style.display="block";
bottomRect.style.top=((t+h)* PXN8.magnification())+"px";
bottomRect.style.left=(l* PXN8.magnification())+"px";
bottomRect.style.width=(w* PXN8.magnification())+"px";
bottomRect.style.height=(theImg.height-(ey* PXN8.magnification()))+"px";
selectRect.style.top=(t* PXN8.magnification())+"px";
selectRect.style.left=(l* PXN8.magnification())+"px";
selectRect.style.width=(w* PXN8.magnification())+"px";
selectRect.style.height=(h* PXN8.magnification())+"px";
selectRect.style.display="block";
selectRect.style.zIndex="100";
 PXN8.position.x=l;
PXN8.position.y=t;
displayPositionInfo();
displaySelectionInfo();
PXN8.notifyListeners(PXN8.ON_SELECTION_CHANGE);}
function CallOnEnter(evt,func){
evt=(evt) ? evt: event;
var charCode=(evt.charCode) ? evt.charCode:((evt.which) ? evt.which:evt.keyCode);
if (charCode==13 || charCode==3){
if (func){
func();
return false;}
}
return true;}
function loadImage(imageLoc){
PXN8.image.location=imageLoc;
PXN8.opNumber=0;
PXN8.maxOpNumber=0;
PXN8.history=new Array();
var fetchOp={operation: "fetch",
image: escape(escape(PXN8.image.location))};
PXN8.history.push(fetchOp);
if (PXN8.replaceOnSave){
fetchOp.random=randomHex();}
unselect();
replaceImage(PXN8.image.location);
postImageLoad();}
function adjustCurrentSelection(){
if (ex-sx > 0 && ey-sy > 0){
var width=ex-sx;
var height=ey-sy;
if (width > PXN8.aspectRatio.width &&
height > PXN8.aspectRatio.height &&
PXN8.aspectRatio.width > 0){
if (PXN8.aspectRatio.width > PXN8.aspectRatio.height){
height=Math.round(width/PXN8.aspectRatio.width*PXN8.aspectRatio.height);
if (sy+height > PXN8.image.height){
height=PXN8.image.height-sy;
width=Math.round(height/PXN8.aspectRatio.height*PXN8.aspectRatio.width);}
}else{
width=Math.round(height/PXN8.aspectRatio.height*PXN8.aspectRatio.width);
if (sx+width > PXN8.image.width){
width=PXN8.image.width-sx;
height=Math.round(width/PXN8.aspectRatio.width*PXN8.aspectRatio.height);}
}
}
ex=sx+width;
ey=sy+height;
selectArea();}
}
function save(){
document.location="/save.pl?image="+PXN8.getUncompressedImage();}
function pointParameter(){
var sel=GetSelection();
return "&point="+sel.w+"."+sel.h+"." +sel.x+"."+sel.y;}
function displaySelectionInfo(){
var selectionField=id("pxn8_selection_size");
if (selectionField){
var text="N/A";
if (ex-sx > 0){
text=(ex-sx)+","+(ey-sy);}
ac(cl(selectionField),tx(text));}
}
function displayPositionInfo(){
var posInfo=id("pxn8_mouse_pos");
if (posInfo){
var text=PXN8.position.x+","+PXN8.position.y;
ac(cl(posInfo),tx(text));}
}
function displayZoomInfo(){
var zoomInfo=id("pxn8_zoom");
if (zoomInfo){
var text=Math.round((PXN8.magnification()*100))+"%";
ac(cl(zoomInfo),tx(text));}
}
function initializeCanvas (){
var canvas=id("pxn8_canvas");
canvas.onmousemove=function (event){ if (!event) event=window.event;
var cursorPos=getCursorPosition(event);
var imagePoint=new mousePointToElementPoint(cursorPos.x, cursorPos.y);
PXN8.position.x=imagePoint.x;
PXN8.position.y=imagePoint.y;
displayPositionInfo();
return true;};
canvas.onmouseout=function (event){ if (!event) event=window.event;
PXN8.position.x="-";
PXN8.position.y="-";
displayPositionInfo();};
canvas.onmousedown=function (event){
if (!event) event=window.event;
beginDrag(canvas,event,moveCanvasHandler,upCanvasHandler);};
canvas.ondrag=function(){ return false;};
var computedCanvasStyle=PXN8.computedStyle("pxn8_canvas");
var canvasPosition=null;
if (computedCanvasStyle.getPropertyValue){
canvasPosition=computedCanvasStyle.getPropertyValue("position");}else{
if (!computedCanvasStyle.position){
canvasPosition="static";}else{
canvasPosition=computedCanvasStyle.position;}
}
if (!canvasPosition || canvasPosition=="static"){
canvas.style.position="relative";
canvas.style.top="0px";
canvas.style.left="0px";}
var floatProperty="cssFloat";
if (document.all){
floatProperty="styleFloat";}
var floatValue=computedCanvasStyle[floatProperty];
if (!floatValue || floatValue=="none"){
canvas.style[floatProperty]="left";}
return canvas;}

function _pxn8_initialize(image_src) 
{
createSelectionRect();
var canvas=initializeCanvas();
var rects=["pxn8_top_rect","pxn8_bottom_rect","pxn8_left_rect","pxn8_right_rect"];
for (var i=0;i < rects.length; i++){
var rect=id(rects[i]);
if (!rect){
rect=ac(canvas,ce("div",{id: rects[i]}));}
rect.style.fontSize="0px";
if (!rect.style.backgroundColor){
rect.style.backgroundColor=PXN8.style.notSelected.color;}
rect.style.position="absolute";
if (!rect.style.opacity){
fnOpacity(rect,PXN8.style.notSelected.opacity);}
rect.style.top="0px";
rect.style.left="0px";
rect.style.width="0px";
rect.style.height="0px";
rect.style.display="none";
rect.style.zIndex="1";}
PXN8.image.location=image_src;
PXN8.opNumber=0;
PXN8.maxOpNumber=0;
PXN8.history=new Array();
var fetchOp={operation: "fetch", image: escape(escape(PXN8.image.location))
};
PXN8.history.push(fetchOp);
if (PXN8.replaceOnSave){
fetchOp.random=randomHex();}
var pxn8image=id("pxn8_image");
var onImageLoad=function(pxn8image){
PXN8.image.width=pxn8image.width;
PXN8.image.height=pxn8image.height;
addImageToHistory(pxn8image.src);
displaySizeInfo();};
if (!pxn8image){
var imgContainer=id("pxn8_image_container");
if (!imgContainer){
imgContainer=ac(canvas,ce("div",{id: "pxn8_image_container"}));}
pxn8image=ac(imgContainer,ce("img",{id: "pxn8_image", src: PXN8.image.location}));
pxn8image.onload=function(){
onImageLoad(pxn8image);};}else{
}
onImageLoad(pxn8image);
displayZoomInfo();} 
function replaceImage(imageurl){
var imageContainer=cl(id("pxn8_image_container"));
ac(imageContainer,ce("img",{id: "pxn8_image", src: imageurl}));
displaySizeInfo();}
var fader={
values: [0.99,0.85, 0.70, 0.55, 0.40, 0.25, 0.10, 0],
times: [75, 75, 75, 75, 75, 75, 75, 75],
i: 0,
stopfadeout: false,
init: function(){ this.i =0; this.stopfadeout=false;},
cancelfadeout: function(){ this.stopfadeout=true; },
fadeout: function(eltid,destroyOnFade){
if (this.stopfadeout){
return;}
fnOpacity(id(eltid),this.values[this.i]);
if (this.i < this.values.length -1 ){
this.i++;
setTimeout("fader.fadeout('"+eltid+"',"+destroyOnFade+");",this.times[this.i]);}else{
if (destroyOnFade){
var node=id(eltid);
if (!node){
return;}else{
var parent=node.parentNode;
parent.removeChild(node);}
}
}
},
fadein: function(eltid){
try{
if (this.i >= this.values.length){
this.i=this.values.length-1;}
fnOpacity(id(eltid),this.values[this.i]);
if (this.i > 0){
this.i--;
setTimeout("fader.fadein('"+eltid+"');",this.times[this.i]);}
}catch(e){
alert(e);}
}
};
function softalert(message,duration){
if (!duration){
duration=1000;}
var warning=id("pxn8_warning");
if (!warning){
warning=ce("div",{id: "pxn8_warning",className: "warning"});}
fnOpacity(warning,0.8);
var imgPos=eb(id("pxn8_image"));
warning.style.top=imgPos.y+"px";
warning.style.left=imgPos.x+"px";
warning.style.width=imgPos.width+"px";
ac(cl(warning),tx(message));
ac(document.body,warning);
setTimeout("fader.init();fader.fadeout('pxn8_warning',true);",duration);}
function ahahDone(target) {
if (req.readyState==2) {
}
if (req.readyState==4) {
var targetDiv=id(target);
if (req.status==200) {
PXN8.log.append(req.status);
PXN8.log.append(req.responseText);
var mr=req.responseText.match(/^\{status/);
if (mr){
PXN8.response=eval('('+ req.responseText+')');
if (PXN8.response.status=="OK"){
var newImageSrc=PXN8.response.image;
PXN8.responses[PXN8.opNumber]=PXN8.response;
if (ie){
newImageSrc += "?rnd="+randomHex();}
replaceImage(newImageSrc);}else{
alert(PXN8ERRORS[PXN8.response.errorCode]);
if (PXN8.onImageFailed){
PXN8.onImageFailed();}
}
}else{
alert(req.responseText);}
} else {
alert(PXN8STRINGS.WEB_SERVER_ERROR+req.statusText+":"+req.responseText) ;}
var timer=id("pxn8_timer");
if (timer){
timer.style.display="none";}
PXN8.updating=false;
postImageLoad();}
}
function displaySizeInfo(){
var sizeInfo=id("pxn8_image_size");
if (sizeInfo){
var text=PXN8.image.width+"x"+PXN8.image.height;
ac(cl(sizeInfo),tx(text));}
}
function postImageLoad(){
var theImage=id("pxn8_image");
theImage.onerror=function(){
alert(PXN8STRINGS.IMAGE_ON_ERROR1+theImage.src+PXN8STRINGS.IMAGE_ON_ERROR2);
if (PXN8.onImageFailed){
PXN8.onImageFailed();}
};
var onloadFunc=function(){
PXN8.log.append("image "+theImage.src+" has loaded");	PXN8.image.width=theImage.width;
PXN8.image.height=theImage.height;
displaySizeInfo();
addImageToHistory(theImage.src);
if (sx > PXN8.image.width || ex > PXN8.image.width || sy > PXN8.image.height || ey > PXN8.image.height){
unselect();}else{
selectArea();}
PXN8.imagesBySrc[theImage.src]=true;
PXN8.notifyListeners(PXN8.ON_IMAGE_CHANGE);
displayZoomInfo(); };
if (PXN8.imagesBySrc[theImage.src]){
onloadFunc();}else{
theImage.onload=onloadFunc;}
PXN8.zoomedBy=1.0; 
displayZoomInfo();}
function createSelectionRect(){
var selectRect=id("pxn8_select_rect");
if (!selectRect){
var canvas=id("pxn8_canvas");
selectRect=ac(canvas, ce("div", {id: "pxn8_select_rect"}));
selectRect.style.backgroundColor="white";
fnOpacity(selectRect,0);
selectRect.style.cursor="move";
selectRect.style.borderWidth="1px";
selectRect.style.borderColor="red";
selectRect.style.borderStyle="dotted";
selectRect.style.position="absolute";
selectRect.style.zIndex=1;
selectRect.style.fontSize="0px";
selectRect.style.display="block";
selectRect.style.width="0px";
selectRect.style.height="0px";}
selectRect.onmousedown=function(event){ if (!event) event=window.event;
beginDrag(selectRect,event,moveSelectionBoxHandler,upSelectionBoxHandler);};
return selectRect;}
function addImageToHistory(imageLocation){
PXN8.log.append(" addImageToHistory ("+imageLocation+" "+PXN8.image.width+","+PXN8.image.height+")");
var item={"location": imageLocation,
"width": PXN8.image.width,
"height": PXN8.image.height
};
PXN8.images[PXN8.opNumber]=item;
for (var i=0; i <= PXN8.maxOpNumber; i++){
var item=PXN8.images[i];
if (item){
PXN8.log.append("-- [" +i+ "] "+item.location+" "+item.width+","+item.height);}
}
PXN8.log.append("===========================");}
var resize_dx=0;
var resize_dy=0;
function canResizeNorth(yOffset){
return (sy+yOffset < (ey-PXN8.style.resizeHandles.size)) && (sy+yOffset > 0);}
function canResizeWest(xOffset){
return (sx+xOffset < (ex-PXN8.style.resizeHandles.size)) && (sx+xOffset > 0);}
function canResizeSouth(yOffset){
return (ey+yOffset > (sy+PXN8.style.resizeHandles.size)) && (ey+yOffset < PXN8.image.height);}
function canResizeEast(xOffset){
return (ex+xOffset > (sx+PXN8.style.resizeHandles.size)) && (ex+xOffset < PXN8.image.width);}
var nTest=function(xOffset,yOffset,event){
if (canResizeNorth(yOffset)) {
resize_dy=event.clientY;
sy=Math.round(sy+yOffset);
return true;}
return false;};
var sTest=function(xOffset,yOffset,event){
if (canResizeSouth(yOffset)){
resize_dy=event.clientY;
ey=Math.round(ey+yOffset);
return true;}
return false;};
var wTest=function(xOffset,yOffset,event){
if (canResizeWest(xOffset)) {
resize_dx=event.clientX;
sx=Math.round(sx+xOffset);
return true;}
return false;};
var eTest=function(xOffset,yOffset,event){
if (canResizeEast(xOffset)){
resize_dx=event.clientX;
ex=Math.round(ex+xOffset);
return true;}
return false;};
var nwTest=function(xOffset,yOffset,event){
if (xOffset==0 || yOffset==0){
return false;}
var hr=resize_start_height/resize_start_width;
var wr=1 / hr;
if (wr > hr){
xOffset=yOffset*wr;}else if (wr < hr){
yOffset=xOffset*hr;}else{
yOffset=xOffset;}
if (xOffset > 0){
yOffset=Math.abs(yOffset);}else{
yOffset=0-Math.abs(yOffset);}
if (canResizeWest(xOffset) && canResizeNorth(yOffset)){
resize_dx=event.clientX;
resize_dy=event.clientY;
sx=Math.round(sx+xOffset);
sy=Math.round(sy+yOffset);
return true;}
return false;}
var swTest=function(xOffset,yOffset,event){
if (xOffset==0 || yOffset==0){
return false;}
var hr=resize_start_height/resize_start_width;
var wr=1 / hr;
if (wr > hr){
yOffset=xOffset*wr;}else{
yOffset=xOffset;}
if (xOffset > 0){
yOffset=0-Math.abs(yOffset);}else{
yOffset=Math.abs(yOffset);}
if (canResizeWest(xOffset) && canResizeSouth(yOffset)){
resize_dx=event.clientX;
resize_dy=event.clientY;
sx=Math.round(sx+xOffset);
ey=Math.round(ey+yOffset);
return true;}
return false;}
var neTest=function(xOffset,yOffset,event){
if (xOffset==0 || yOffset==0){
return false;}
var hr=resize_start_height/resize_start_width;
var wr=1 / hr;
if (wr > hr){
xOffset=yOffset*wr;}else{
xOffset=yOffset;}
if (yOffset > 0){
xOffset=0-Math.abs(xOffset);}else{
xOffset=Math.abs(xOffset);}
if (canResizeEast(xOffset) && canResizeNorth(yOffset)){ resize_dx=event.clientX;
resize_dy=event.clientY;
ex=Math.round(ex+xOffset);
sy=Math.round(sy+yOffset);
return true;}
return false;}
var seTest=function(xOffset,yOffset,event){
if (xOffset==0 || yOffset==0){
return false;}
var hr=resize_start_height/resize_start_width;
var wr=1 / hr;
if (wr > hr){
xOffset=yOffset*wr;}else{
yOffset=xOffset;}
if (xOffset > 0){
yOffset=Math.abs(yOffset);}else{
yOffset=0-Math.abs(yOffset);}
if (canResizeEast(xOffset) && canResizeSouth(yOffset)){
resize_dx=event.clientX;
resize_dy=event.clientY;
ex=Math.round(ex+xOffset);
ey=Math.round(ey+yOffset);
return true;}
return false;}
function resizer(testFunc){
var result=function(event){
if (!event) event=window.event;
var rdy=event.clientY-resize_dy;
var rdx=event.clientX-resize_dx;
var prdy=rdy*PXN8.magnification();
var prdx=rdx*PXN8.magnification();
if (prdx==0 && prdy==0){
}else{
if (testFunc(prdx,prdy,event)==true){
selectArea();}
}
if (event.stopPropogation) event.stopPropogation(); 
else event.cancelBubble=true; 
};
return result;}
var handles={
"n": { moveHandler: resizer(nTest),
position: function(handle,sel){
handle.style.left=Math.round((sel.x+(sel.w/2))*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y*PXN8.magnification()))+"px";}
},
"s": { moveHandler: resizer(sTest),
position: function(handle,sel){
handle.style.left=Math.round((sel.x+(sel.w/2))*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y+(sel.h-PXN8.style.resizeHandles.size))*PXN8.magnification())+"px";}
},
"e": { moveHandler: resizer(eTest),
position: function(handle,sel){
handle.style.left=Math.round((sel.x+(sel.w-PXN8.style.resizeHandles.size))*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y+(sel.h/2))*PXN8.magnification())+"px";}
},
"w": { moveHandler: resizer(wTest),
position: function(handle,sel){
handle.style.left=Math.round(sel.x*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y+(sel.h/2))*PXN8.magnification())+"px";}
},
"nw": { moveHandler: resizer(nwTest),
position: function(handle,sel){
handle.style.left=Math.round(sel.x*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y*PXN8.magnification()))+"px";}
},
"sw": { moveHandler: resizer(swTest),
position: function(handle,sel){
handle.style.left=Math.round(sel.x*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y+(sel.h-PXN8.style.resizeHandles.size))*PXN8.magnification())+"px";}
},
"ne": { moveHandler: resizer(neTest),
position: function(handle,sel){
handle.style.left=Math.round((sel.x+(sel.w-PXN8.style.resizeHandles.size))*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y*PXN8.magnification()))+"px";}
},
"se": { moveHandler: resizer(seTest),
position: function(handle,sel){
handle.style.left=Math.round((sel.x+(sel.w-PXN8.style.resizeHandles.size))*PXN8.magnification())+"px";
handle.style.top=Math.round((sel.y+(sel.h-PXN8.style.resizeHandles.size))*PXN8.magnification())+"px";}
}
};
function stopResizing(event){
if (!event) event=window.event ; 
if (document.removeEventListener){
document.removeEventListener("mouseup",stopResizing,true);
for (var i in handles){
document.removeEventListener("mousemove",handles[i].moveHandler, true);}
}else if (document.detachEvent){
document.detachEvent("onmouseup",stopResizing);
for (var i in handles){
document.detachEvent("onmousemove",handles[i].moveHandler);}
}
if (event.stopPropogation) event.stopPropogation(); 
else event.cancelBubble=true; 
}
var resize_start_width=0;
var resize_start_height=0;
function startResizing (hdlr){
var result=function(event){
if (!event) event=window.event;
resize_dx=event.clientX;
resize_dy=event.clientY;
var sel=GetSelection();
resize_start_height=sel.h;
resize_start_width=sel.w;
if (document.addEventListener){
document.addEventListener("mousemove", hdlr, true);
document.addEventListener("mouseup", stopResizing, true);}else if (document.attachEvent){
document.attachEvent("onmousemove",hdlr);
document.attachEvent("onmouseup",stopResizing);}
if (event.stopPropogation) event.stopPropogation();
else event.cancelBubble=true; 
if (event.preventDefault) event.preventDefault(); 
else event.returnValue=false; 
};
return result;}
function createResizeHandle(direction,size,color){
var result=document.createElement("div");
result.id=direction+"_handle";
result.style.backgroundColor=color;
result.style.position="absolute";
result.style.width=size+"px";
result.style.height=size+"px";
result.style.overflow="hidden"; result.style.zIndex=999;
result.style.cursor=direction+"-resize";
result.onmousedown=startResizing(handles[direction].moveHandler);
result.ondrag=function(){return false;};
return result;}
function positionResizeHandles(){
var sel=GetSelection();
if (sel.w==0){
hideResizeHandles();
return;}
var canvas=id("pxn8_canvas");
for (var i in handles){
var handle=id( i+"_handle");
if (!handle){
handle=createResizeHandle(i, PXN8.style.resizeHandles.size,
PXN8.style.resizeHandles.color);
ac(canvas,handle);}
if (handle.style.display=="none"){
handle.style.display="block";}
handles[i].position(handle,sel);}
}
function hideResizeHandles(hdls){
if (hdls){
for (var i =0; i < hdls.length;i++){
var handle=id( i+"_handle");
if (handle){
handle.style.display="none";}
}
}else{
for (var i in handles){
var handle=id( i+"_handle");
if (handle){
handle.style.display="none";}
}
}
}
PXN8.addListener(PXN8.ON_SELECTION_CHANGE, positionResizeHandles);
PXN8.addListener(PXN8.ON_IMAGE_CHANGE, displayZoomInfo);
function cl(elt){
if (elt){
while (elt.firstChild){
elt.removeChild(elt.firstChild);}
}
return elt;}
function tx(str){
return document.createTextNode(str);}
function id(elementId){
return document.getElementById(elementId);}
function ce(nodeType, attributes){
var element=document.createElement(nodeType);
for (var i in attributes){
element[i]=attributes[i];}
return element;}
function ap(parentNode, nodeType, attributes){
return ac(parentNode,ce(nodeType,attributes));}
function fnOpacity(element, value){
if (!element){
return;}
if (document.all){
element.style.filter="alpha(opacity:"+(value*100)+")";}else{
element.style.opacity=value;
element.style._moz_opacity=value;}
}
function eb(elt){
var x=null;
var y=null;
if(elt.style.position=="absolute") {
x=parseInt(elt.style.left);
y=parseInt(elt.style.top);} else {
var pos=GetElementPosition(elt); x=pos.x;
y=pos.y;} return {x: x, y: y, width: elt.offsetWidth, height: elt.offsetHeight};} 
function ac(parent, child){
parent.appendChild(child);
return child;}
function _showTip(tipId){
var tipDiv=null;
if (gTipDisplay[tipId]==false){
return;}
tipDiv=id(tipId);
if (tipDiv==null){
return;}
if (tipDiv.style==null){
return;}
tipDiv.style.display="block";
var imgBounds=eb(id("pxn8_image"));
tipDiv.style.top=imgBounds.y+10+"px";
tipDiv.style.left=imgBounds.x+10+"px";
var shadow=id("tipshadow");
if (!shadow){
shadow=ac(document.body,ce("div"));}
shadow.id="tipshadow";
shadow.style.backgroundColor="black";
var opacity=50;
shadow.style.opacity=opacity/100;
shadow.style._moz_opacity=opacity/100;
shadow.style.filter="alpha(opacity:"+opacity+")";
var tipBounds=eb(tipDiv);
shadow.style.position="absolute";
shadow.style.top=tipBounds.y+3+"px";
shadow.style.left=tipBounds.x+3+"px";
shadow.style.width=tipBounds.width+ "px";
shadow.style.height=tipBounds.height+"px";}
function showTip(element, event,elementId){
var tipId=null;
if (elementId){
tipId=elementId+"_tip";}else{
tipId=element.id+"_tip";}
gTipDisplay[tipId]=true;
setTimeout("_showTip('"+tipId+"');",1200);}
var gTipDisplay=[];
function hideTip(element, event,elementId){
var tipDiv=null;
var tipId=null;
if (elementId){
tipId=elementId+"_tip";}else{
tipId=element.id+"_tip";}
gTipDisplay[tipId]=false;
tipDiv=id(tipId);
if (tipDiv){
tipDiv.style.display="none";}
var shadow=id("tipshadow");
if (shadow){
document.body.removeChild(shadow);}
}
PXN8.tools.history=function (offset){
if (offset==0){
return;}
if (PXN8.updating){
alert (PXN8STRINGS.IMAGE_UPDATING);
return;}
if (!offset) offset=-1;
if (PXN8.opNumber==0 && offset < 0){
softalert(PXN8STRINGS.NO_MORE_UNDO);
return;}
if (PXN8.opNumber==PXN8.maxOpNumber && offset > 0){
softalert(PXN8STRINGS.NO_MORE_REDO);
return;}	if (offset < 0){
softalert("- "+PXN8.history[PXN8.opNumber].operation, 500);}else{
PXN8.log.append("redo: "+PXN8.opNumber);
for (var i=0;i < PXN8.history.length; i++){
PXN8.log.append("redo: "+PXN8.history[i].operation);}
softalert("+ "+PXN8.history[PXN8.opNumber+1].operation,500);}
PXN8.opNumber=PXN8.opNumber+offset;
var currentImageData=PXN8.images[PXN8.opNumber];
if (!currentImageData){
alert("Error! PXN8.images["+PXN8.opNumber+"] is undefined");
return false;}
PXN8.image.location=currentImageData.location;
PXN8.image.width=currentImageData.width;
PXN8.image.height=currentImageData.height;
replaceImage(PXN8.image.location);
PXN8.notifyListeners(PXN8.ON_IMAGE_CHANGE); unselect();
return false;};
PXN8.tools.undo=function(){
PXN8.tools.history(-1);
return false;};
PXN8.tools.redo=function(){
PXN8.tools.history(+1);
return false;};
PXN8.tools.undoall=function(){
PXN8.tools.history(0-PXN8.opNumber);
return false;};
PXN8.tools.redoall=function(){
PXN8.tools.history(PXN8.maxOpNumber-PXN8.opNumber);
return false;};
PXN8.addListener(PXN8.ON_ZOOM_CHANGE,displayZoomInfo);
PXN8.tools.zoomIn=function(){
if (PXN8.canZoomIn()){
PXN8.zoomIn();
var theImg=document.getElementById("pxn8_image");
theImg.width=PXN8.image.width*PXN8.magnification();
theImg.height=PXN8.image.height*PXN8.magnification();
selectArea();
PXN8.notifyListeners(PXN8.ON_ZOOM_CHANGE);}else{
softalert(PXN8STRINGS.NO_MORE_ZOOMIN,500);}
return false;};
PXN8.tools.zoomOut=function(){
if (PXN8.canZoomOut()){
PXN8.zoomOut();
var theImg=document.getElementById("pxn8_image");
theImg.width=PXN8.image.width*PXN8.magnification();
theImg.height=PXN8.image.height*PXN8.magnification();
selectArea();
PXN8.notifyListeners(PXN8.ON_ZOOM_CHANGE);}else{
softalert(PXN8STRINGS.NO_MORE_ZOOMOUT,500);}
return false;};
PXN8.tools.zoomToFit=function(width, height){
var hr=width / PXN8.image.width ;
var vr=height / PXN8.image.height ;
if (vr < hr){
PXN8.zoomedBy=vr;}else{
PXN8.zoomedBy=hr;}
for (var i=0; i < PXN8.zooms.length;i++){
if (PXN8.zooms[i] < PXN8.zoomedBy){
PXN8.zoomLevel=i+1;}else{
break;}
}
if (PXN8.zoomLevel >= PXN8.zooms.length){
PXN8.zoomLevel=PXN8.zooms.length -1;}
var theImg=document.getElementById("pxn8_image");
theImg.width=PXN8.image.width*PXN8.magnification();
theImg.height=PXN8.image.height*PXN8.magnification();
selectArea();
PXN8.notifyListeners(PXN8.ON_ZOOM_CHANGE);
return false;};
PXN8.tools.updateImage=function(op){
var executed=true;
if (PXN8.maxOpNumber > PXN8.opNumber){
var lastUndoneOp=PXN8.history[PXN8.opNumber+1];
for (var i in op){
if (lastUndoneOp[i]==op[i]){
}else{
executed=false;
break;}
}
}else{
executed=false;}
if (!executed){
PXN8.history[++PXN8.opNumber]=op;
PXN8.maxOpNumber=PXN8.opNumber;
PXN8.submitOperation(op ,function(){ahahDone("pxn8_image_container");});}else{
PXN8.tools.redo();}
};
PXN8.tools.enhance=function(){
PXN8.tools.updateImage({operation: "enhance"});};
PXN8.tools.spiritlevel=function(x1,y1,x2,y2){
PXN8.tools.updateImage({operation: "spiritlevel", 'x1': x1, 'x2': x2, 'y1': y1, 'y2':y2});};
PXN8.tools.rotate=function(params){
if (!params.angle){ params.angle=0;}
if (params.fliphz==null){
params.fliphz=false;}
if (params.flipvt==null){
params.flipvt=false;}
params.operation="rotate";
if (params.angle > 0 || params.flipvt || params.fliphz){
PXN8.tools.updateImage(params);}
};
PXN8.tools.blur=function (params){
params.operation="blur";
PXN8.tools.updateImage(params);};
PXN8.tools.colors=function(param){
if (!param.saturation) param.saturation=100;
if (!param.brightness) param.brightness=100;
if (!param.hue) param.hue=100;
if (!param.contrast) param.contrast=0;
param.operation="bsh";
PXN8.tools.updateImage(param);};
PXN8.tools.crop=function (params) {
params.operation="crop";
PXN8.tools.updateImage(params);};
PXN8.tools.filter=function (params){
params.operation="filter";
PXN8.tools.updateImage(params);};
PXN8.tools.interlace=function(params){
params.operation="interlace";
PXN8.tools.updateImage(params);};
PXN8.tools.lomo=function(params){
params.operation="lomo";
PXN8.tools.updateImage(params);};
PXN8.tools.fill_flash=function(brightness, threshold){
PXN8.tools.updateImage({operation: "fill_flash"});};
PXN8.tools.snow=function (){
PXN8.tools.updateImage({operation: "snow"});};
PXN8.tools.whiten=function (params){
params.operation="whiten";
PXN8.tools.updateImage(params);};
PXN8.tools.fixredeye=function(params){
params.operation="redeye";
PXN8.tools.updateImage(params);};
PXN8.tools.resize=function(width, height){
PXN8.tools.updateImage({"operation": "resize", "width": width, "height": height});};
PXN8.tools.roundedcorners=function(color, radius){
PXN8.tools.updateImage({"operation":"roundcorners",
"color":color, "radius":radius});};
PXN8.tools.sepia=function(color){
PXN8.tools.updateImage({"operation":"sepia","color":color});};
PXN8.tools.grayscale=function(){
PXN8.tools.updateImage({"operation":"grayscale"});};
var fnFalse=function(){return false;};
function blurImage(){
var radius=id("blurRadius").value;
if (isNaN(radius) || radius < 1 || radius > 8){
alert(PXN8STRINGSS.BLUR_RANGE);
return false;}
var sel=GetSelection();
PXN8.tools.blur({radius: radius,
top: sel.y,
left: sel.x,
width: sel.w,
height: sel.h
});
return true;}
function configResize(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ resizeImage(); return false;};
ac(cl(id("pxn8_config_title")),tx(PXN8STRINGS.CONFIG_RESIZE_TOOL));
var configContent=id("pxn8_config_content");
if (configContent==null){
alert(PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
cl(id("pxn8_tool_prompt"));
cl(configContent);
var form=ac(configContent,ce("form",{onsubmit: fnFalse}));
var table=ac(form,ce("table"));
var tbody=ac(table,ce("tbody"));
var row1=ac(tbody,ce("tr"));
var r1c1=ac(row1,ce("td"));
ac(r1c1,tx(PXN8STRINGS.ASPECT_LABEL));
var r1c2=ac(row1,ce("td"));
var input=ac(r1c2,ce("input",{type: "checkbox", id: "preserve", value: true, defaultChecked: true}));
input.onclick=function(){ preserveRatio(input);};
var row2=ac(tbody,ce("tr"));
var row3=ac(tbody,ce("tr"));
ac(ac(row2,ce("td")),tx(PXN8STRINGS.WIDTH_LABEL));
var iwidth=ac(ac(row2,ce("td")),ce("input",{className: "pxn8_small_field", type: "text", value: PXN8.image.width,
id: "resizeWidth", name: "resizeWidth"}));
iwidth.onfocus=function (){ iwidth.select();};
iwidth.onblur=function(){ changeDim('width');};
ac(ac(row3,ce("td")),tx(PXN8STRINGS.HEIGHT_LABEL));
var iheight=ac(ac(row3,ce("td")),ce("input",{className: "pxn8_small_field", type: "text", value: PXN8.image.height,
id: "resizeHeight", name: "resizeHeight"}));
iheight.onfocus=function (){ iheight.select();};
iheight.onblur=function(){ changeDim('height');};
iwidth.focus();
var row4=ac(tbody,ce("tr"));
var r4c1=ac(row4,ce("td",{colspan: 2}));
var resizeToSelectedArea=ac(r4c1,ce("a",{href: "javascript:void(0)",
onclick: function(){
var sel=GetSelection();
if (sel.w <= 0 || sel.h <= 0){
softalert(PXN8STRINGS.RESIZE_SELECT_AREA);
return false;}
unselect();
PXN8.tools.resize(sel.w,sel.h);
return false;}
}));
ac(resizeToSelectedArea,tx(PXN8STRINGS.RESIZE_SELECT_LABEL));
sx=0;
sy=0;
ex=PXN8.image.width*0.75;
ey=PXN8.image.height*0.75;
selectArea();
iwidth.setAttribute('value', PXN8.image.width);
iheight.setAttribute('value', PXN8.image.height);}
function configBlur(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ blurImage(); return false;};
cl(id("pxn8_config_title")).appendChild(tx(PXN8STRINGS.CONFIG_BLUR_TOOL));
var configContent=id("pxn8_config_content");
if (configContent==null){
alert (PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
cl(configContent);
var form=ac(configContent,ce("form",{onsubmit: fnFalse}));
var table=ac(form,ce("table", { width: "100%"}));
var tbody=ac(table,ce("tbody"));
var r1=ac(tbody,ce("tr"));
ac(ac(r1,ce("td")),tx(PXN8STRINGS.RADIUS_LABEL));
var input=ac(ac(r1,ce("td")),ce("input", {className: "pxn8_small_field",
type: "text",
id: "blurRadius",
value: 2,
name: "blurRadius"}));
input.onkeypress=function(event){
if (!event) event=window.event;
return CallOnEnter(event,blurImage);};
input.onfocus=function(){
input.select();};
var helpArea=id("pxn8_tool_prompt");
ac(cl(helpArea),tx(PXN8STRINGS.BLUR_PROMPT));
input.focus();}
function bsh(){ var bright=id("brightness");
var sat=id("saturation");
var h=id("hue");
var contrast=id("contrast");
var contrastValue=contrast.options[contrast.selectedIndex];
if (isNaN(bright.value) || bright.value < 0 || bright.value.match(/\S+/)==null){
alert(PXN8STRINGS.BRIGHTNESS_RANGE);
return false;}
if (isNaN(h.value) || h.value < 0 || h.value > 200 || h.value.match(/\S+/)==null){
alert (PXN8STRINGS.HUE_RANGE);
return false;}
if (isNaN(sat.value) || sat.value < 0 || sat.value.match(/\S+/)==null){
alert(PXN8STRINGS.SATURATION_RANGE);
return false;}
if (bright.value==100 && h.value==100 && sat.value==100 && contrastValue.value==0){
return false;}
PXN8.tools.colors ({brightness: bright.value,
hue: h.value,
saturation: sat.value, contrast: contrastValue.value});
return true;}
function configBSH(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ bsh(); return false;};
cl(id("pxn8_tool_prompt"));
cl(id("pxn8_config_title")).appendChild(tx(PXN8STRINGS.CONFIG_COLOR_TOOL));
var configContent=id("pxn8_config_content");
if (configContent==null){
alert (PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
var tbody=ac(ac(cl(configContent),ce("table",{width: "90%"})),ce("tbody"));
var r1=ac(tbody,ce("tr"));
var r2=ac(tbody,ce("tr"));
var r3=ac(tbody,ce("tr"));
var r4=ac(tbody,ce("tr"));
var r1c1=ac(r1,ce("td",{valign: "bottom"}));
var r1c2=ac(r1,ce("td",{valign: "bottom"}));
var r1c3=ac(r1,ce("td",{valign: "bottom"}));
var brdiv=ac(r1c1,ce("div",{className: "pxn8_slide"}));
brdiv.onmousedown=function(event){
if (!event) event=window.event;
startSlide(brdiv,event,'brightness',0,200);};
ac(brdiv,tx(PXN8STRINGS.BRIGHTNESS_LABEL));
ac(brdiv,ce("span",{className: "pxn8_slider"}));
var brdiv2=ac(r1c2,ce("input",{className: "pxn8_slidervalue", type: "text", name: "brightness", id: "brightness", value: 100}));
brdiv2.onkeypress=function(event){
if (!event) event=window.event;
return CallOnEnter(event,bsh);};
ac(r1c3,tx("%"));
var r2c1=ac(r2,ce("td",{valign: "bottom"}));
var r2c2=ac(r2,ce("td",{valign: "bottom"}));
var r2c3=ac(r2,ce("td",{valign: "bottom"}));
var satdiv=ac(r2c1,ce("div",{className: "pxn8_slide"}));
satdiv.onmousedown=function(event){
if (!event) event=window.event;
startSlide(satdiv,event,'saturation',0,200);};
ac(satdiv,tx(PXN8STRINGS.SATURATION_LABEL));
ac(satdiv,ce("span",{className: "pxn8_slider"}));
var sat2=ac(r2c2,ce("input",{className: "pxn8_slidervalue", type: "text", name: "saturation", onkeypress: brdiv2.onkeypress,
id: "saturation", value: 100}));
ac(r2c3,tx("%"));
var r3c1=ac(r3,ce("td",{valign: "bottom"}));
var r3c2=ac(r3,ce("td",{valign: "bottom"}));
var r3c3=ac(r3,ce("td",{valign: "bottom"}));
var huediv=ac(r3c1,ce("div",{className: "pxn8_slide"}));
huediv.onmousedown=function(event){
if (!event) event=window.event;
startSlide(huediv,event,'hue',0,200);};
ac(huediv,tx(PXN8STRINGS.HUE_LABEL));
ac(huediv,ce("span",{className: "pxn8_slider"}));
var hue2=ac(r3c2,ce("input",{className: "pxn8_slidervalue", type: "text", name: "hue", onkeypress: brdiv2.onkeypress,
id: "hue", value: 100}));
ac(r3c3,tx("%"));
var r4c1=ac(r4,ce("td",{colspan: 3, valign: "top"}));
ac(r4c1,tx(PXN8STRINGS.CONTRAST_LABEL));
var sel=ac(r4c1,ce("select",{name: "contrast", id: "contrast",onkeypress: brdiv2.onkeypress }));
var options={"-3": "-3", "-2": "-2", "-1": "-1", "0": PXN8STRINGS.CONTRAST_NORMAL, "1": "+1", "2": "+2", "3": "+3"};
var j=0;
for (var i in options){
sel.options[j++]=new Option(options[i],i);}
sel.selectedIndex=3;
var br=id("brightness");
br.focus();
br.select();}
function crop(){
var sel=GetSelection();
if (sel.w <= 0 || sel.h <= 0){
softalert(PXN8STRINGS.CROP_SELECT_AREA);
return false;}
PXN8.tools.crop({top: sel.y, left: sel.x, width: sel.w, height: sel.h});
unselect();
PXN8.aspectRatio.width=0;
PXN8.aspectRatio.height=0;
return true;}
function configCrop(element,event){ var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ crop(); return false;};
var cancelBtn=id("pxn8_cancel");
var handler=cancelBtn.onclick;
cancelBtn.onclick=function(){ 
PXN8.aspectRatio.width=0;
PXN8.aspectRatio.height=0;
handler();
cancelBtn.onclick=handler;
return false;};
var configTitle=cl(id("pxn8_config_title"));
configTitle.appendChild(tx(PXN8STRINGS.CONFIG_CROP_TOOL));
var configContent=id("pxn8_config_content");
if (configContent==null){
alert(PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
var theImg=id("pxn8_image");
var helpArea=cl(id("pxn8_tool_prompt"));
var form=ac(cl(configContent),ce("form",{onsubmit:fnFalse}));
var tbody=ac(ac(form,ce("table",{width: "100%"})),ce("tbody"));
var row1=ac(tbody,ce("tr"));
var row2=ac(tbody,ce("tr"));
var row3=ac(tbody,ce("tr"));
ac(ac(row1,ce("td")),tx(PXN8STRINGS.ASPECT_CROP_LABEL));
var r1c2=ac(row1,ce("td"));
var sel=ac(r1c2,ce("select",{id: "aspect_ratio", name: "aspect_ratio"}));
sel.onchange=function() {
changeAspectRatio();};
var options={"free": PXN8STRINGS.CROP_FREE,
"1x1":PXN8STRINGS.CROP_SQUARE,
"4x6":"4x6",
"5x7":"5x7",
"8x10": "8x10",
"6x8":"6x8"};
var j=0;
for (var i in options){
sel.options[j++]=new Option(options[i],i);} ac(ac(row2,ce("td")),tx(PXN8STRINGS.ORIENTATION_LABEL));
var r2c2=ac(row2,ce("td"));
var op=ac(r2c2,ce("div",{className: "pxn8_checked", id: "portrait"}));
op.onclick=function(){ changeOrientation(PXN8.PORTRAIT);
op.className="pxn8_checked";
var ol=id("landscape");
ol.className="pxn8_unchecked";};
op.style.cursor="pointer";
ac(op,tx(PXN8STRINGS.ORIENTATION_PORTRAIT));
var ol=ac(r2c2,ce("div",{className: "pxn8_unchecked", id: "landscape"}));
ol.onclick=function(){ changeOrientation(PXN8.LANDSCAPE);
ol.className="pxn8_checked";
var op=id("portrait");
op.className="pxn8_unchecked";};
ol.style.cursor="pointer";
ac(ol,tx(PXN8STRINGS.ORIENTATION_LANDSCAPE));
var portrait=id("portrait");
var landscape=id("landscape");
if (theImg.height > theImg.width){
portrait.className="pxn8_checked";
landscape.className="pxn8_unchecked"
changeOrientation(PXN8.PORTRAIT);}else{
portrait.className="pxn8_unchecked";
landscape.className="pxn8_checked"
changeOrientation(PXN8.LANDSCAPE);} var r3c1=ac(row3,ce("td", {colspan: 2}));
var prevCropLink=ce("a",{href: "javascript:void(0);",
onclick: previewCrop});
ac(prevCropLink,tx("preview"));
ac(r3c1,prevCropLink);
sel.focus();}
function changeOrientation(orientation,width,height){
PXN8.orientation=orientation;
if (width != null){
changeAspectRatio(width,height);}else{
changeAspectRatio();}
}
function changeAspectRatio(width,height){
if (width != null){
PXN8.aspectRatio.width=width;
PXN8.aspectRatio.height=height;}else{
var aspectRatio=id("aspect_ratio");
if (aspectRatio != null){
var selected=aspectRatio.options[aspectRatio.selectedIndex];
var pair=/^([0-9]+)x([0-9]+)/;
var match=selected.value.match(pair);
if (match != null){
if (PXN8.orientation==PXN8.LANDSCAPE){
PXN8.aspectRatio.width=match[2];
PXN8.aspectRatio.height=match[1];}else{
PXN8.aspectRatio.width=match[1];
PXN8.aspectRatio.height=match[2];}
}else{
PXN8.aspectRatio.width=0;
PXN8.aspectRatio.height=0;
return;}
}
}
if (PXN8.aspectRatio.width==0 &&
PXN8.aspectRatio.height==0){
return;}
var topRect=id("pxn8_top_rect");
topRect.style.borderWidth="1px";
var leftRect=id("pxn8_left_rect");
leftRect.style.borderWidth="0px";
sx=0;
sy=0;
var t1=PXN8.image.width / PXN8.aspectRatio.width ;
var t2=PXN8.image.height / PXN8.aspectRatio.height ;
if (t2 < t1){
ey=PXN8.image.height;
ex=Math.round(ey / PXN8.aspectRatio.height*PXN8.aspectRatio.width);}else{
ex=PXN8.image.width;
ey=Math.round(ex / PXN8.aspectRatio.width*PXN8.aspectRatio.height);}
sx=Math.round((PXN8.image.width-ex) / 2);
sy=Math.round((PXN8.image.height-ey) / 2);
ex += sx;
ey += sy;
selectArea();}
function filter(x,y){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
PXN8.tools.filter({"top": y,
"color": id("color").value,
"opacity":id("opacity").value }
);
return true;}
function configFilter(element, event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="none";
var canvas=id("pxn8_canvas");
var oldonmousedown=canvas.onmousedown;
var onImageUpdated=null;
onImageUpdated=function(){
applyBtn.style.display="inline";
var pin1=id("left_pin");
if (pin1){
pin1.style.display="none";}
canvas.onmousedown=oldonmousedown;
PXN8.removeListener(PXN8.ON_IMAGE_CHANGE,onImageUpdated);};
PXN8.addListener(PXN8.ON_IMAGE_CHANGE,onImageUpdated);
var cancelBtn=id("pxn8_cancel");
var handler=cancelBtn.onclick;
cancelBtn.onclick=function(){ onImageUpdated();
handler();
cancelBtn.onclick=handler;
return false;};
var configTitle=cl(id("pxn8_config_title"));
ac(configTitle,tx(PXN8STRINGS.CONFIG_FILTER_TOOL));
unselect();
var configContent=id("pxn8_config_content");
if (configContent==null){
alert(PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
var form=createColorPicker("#FFA500",75,undefined);
ac(cl(configContent),form);
ac(cl(id("pxn8_tool_prompt")),tx(PXN8STRINGS.FILTER_PROMPT));
setupColorPickerBehaviour("#FFA500",75,undefined);
var newonmousedown=function(event){
if (!event) event=window.event;
var cursorPos=getCursorPosition(event);
var imagePoint=new mousePointToElementPoint(cursorPos.x,cursorPos.y);
canvas.onmousedown=oldonmousedown;
var pin1=id("left_pin");
if (pin1==null){
pin1=createPin("left_pin","images/bluepin.gif");
document.body.appendChild(pin1);}
pin1.style.display="block";
pin1.style.left=""+(cursorPos.x -7)+"px";
pin1.style.top=""+(cursorPos.y-24)+"px";
filter(imagePoint.x,imagePoint.y);
return true;};
canvas.onmousedown=newonmousedown;
var cf=id("opacity");
cf.focus();}
function configInterlace(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ addInterlace(); return false;};
var configTitle=cl(id("pxn8_config_title"));
configTitle.appendChild(tx(PXN8STRINGS.CONFIG_INTERLACE_TOOL));
var configContent=id("pxn8_config_content");
if (configContent==null){
alert(PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
var form=createColorPicker("#000000",20,addInterlace);
cl(configContent);
configContent.appendChild(form);
var helpArea=id("pxn8_tool_prompt");
cl(helpArea);
helpArea.appendChild(tx(PXN8STRINGS.INTERLACE_PROMPT));
setupColorPickerBehaviour("#000000",20,addInterlace);
var cf=id("opacity");
cf.focus();
return true;}
function addInterlace(){
var lineColor=id("color").value;
if (lineColor.match(/#[a-fA-F0-9]{6}/)){
}else{
alert(PXN8STRINGS.INVALID_HEX_VALUE);
return false;}
var sel=GetSelection();
var opacityVal=id("opacity").value;
PXN8.tools.interlace({"opacity": opacityVal,
"color": lineColor,
"top": sel.y,
"left": sel.x,
"height": sel.h,
"width": sel.w
});
return true;}
function configLomo(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ lomo(); return false;};
var configTitle=cl(id("pxn8_config_title"));
configTitle.appendChild(tx(PXN8STRINGS.CONFIG_LOMO_TOOL));
var helpArea=cl(id("pxn8_tool_prompt"));
helpArea.appendChild(tx(PXN8STRINGS.OPACITY_PROMPT));
var configContent=id("pxn8_config_content");
if (configContent==null){
alert(PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
var form=ac(cl(configContent),ce("form",{name: "lomoform", onsubmit: fnFalse}));
var tbody=ac(ac(form,ce("table",{width: "100%"})),ce("tbody"));
var row1=ac(tbody,ce("tr"));
var row2=ac(tbody,ce("tr"));
var r1c1=ac(row1,ce("td", {valign: "bottom"}));
var r1c2=ac(row1,ce("td", {valign: "bottom"}));
var r2c1=ac(row2,ce("td", {valign: "bottom"}));
var r2c2=ac(row2,ce("td", {valign: "bottom"}));
var opslide=ac(r1c1,ce("div",{className: "pxn8_slide"}));
opslide.onmousedown=function(event){ if (!event) event=window.event;
startSlide(opslide,event,'opacity',0,100);};
ac(opslide,tx(PXN8STRINGS.OPACITY_LABEL));
ac(opslide,ce("span",{className: "pxn8_slider"}));
var opacityField=ac(r1c2,ce("input",{className: "pxn8_slidervalue", type:"text", name:"opacity", id:"opacity", value:60}));
ac(r1c2,tx("%"));
ac(r2c1,tx(PXN8STRINGS.SATURATE_LABEL));
ac(r2c2,ce("input",{type:"checkbox",name: "saturate", defaultChecked: true, id:"saturate"}));
opacityField.focus();}
function lomo(){
var opacity=id("opacity");
var saturate=id("saturate");
if (isNaN(opacity.value) || opacity.value <0 || opacity.value > 100){
alert(PXN8STRINGS.OPACITY_RANGE);
return false;}
PXN8.tools.lomo({ "opacity": opacity.value, "saturate": saturate.checked });
return true;}
function whiten(){
var sel=GetSelection();
if (sel.w==0 || sel.h==0){
softalert(PXN8STRINGS.WHITEN_SELECT_AREA);
return false;} if (sel.w*sel.h > 16000){
softalert (PXN8STRINGS.SELECT_SMALLER_AREA);
return false;} PXN8.tools.whiten({"top": sel.y, "left":sel.x, "width":sel.w, "height":sel.h});
return true;}
function fixRedEye(){
var sel=GetSelection();
if (sel.w==0 || sel.h==0){
alert(PXN8STRINGS.REDEYE_SELECT_AREA);
return false;} if (sel.w > 100 || sel.h > 100){
alert (PXN8STRINGS.REDEYE_SMALLER_AREA);
return false;} PXN8.tools.fixredeye({"top":sel.y, "left":sel.x, "width":sel.w, "height":sel.h});
PXN8.aspectRatio.width=0;
PXN8.aspectRatio.height=0;
unselect();
return true;}
function configRedEye(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ fixRedEye(); return false;};
ac(cl(id("pxn8_config_title")),tx(PXN8STRINGS.CONFIG_REDEYE_TOOL));
var configContent=id("pxn8_config_content");
if (configContent==null){
alert(PXN8STRINGS.NO_CONFIG_CONTENT);
return false;}
var tbody=ac(cl(configContent),ac(ce("table",{width: "100%"}),ce("tbody")));
ac(ac(tbody,ac(ce("tr"),ce("td"))),tx(PXN8STRINGS.REDEYE_PROMPT));
cl(id("pxn8_tool_prompt"));
PXN8.aspectRatio.width=1;
PXN8.aspectRatio.height=1;
adjustCurrentSelection();}
function resizeImage(){
var newWidth=id("resizeWidth").value;
var newHeight=id("resizeHeight").value;
if (newWidth==PXN8.image.width &&
newHeight==PXN8.image.height){
return false;}
if (isNaN(newWidth) || isNaN(newHeight)){
alert(PXN8STRINGS.NUMERIC_WIDTH_HEIGHT);
return false;}
if (newWidth.match(/[0-9]+/) && newHeight.match(/[0-9]+/)){
}else{
alert(PXN8STRINGS.NUMERIC_WIDTH_HEIGHT);
return false;}
if (newWidth > PXN8.resizelimit.width || newHeight > PXN8.resizelimit.height){
alert(PXN8STRINGS.LIMIT_SIZE+PXN8.resizelimit.width+"x"+PXN8.resizelimit.height);
return false;}
unselect();
PXN8.tools.resize(newWidth,newHeight);
return true;}
function preserveRatio(element){
if (element.checked){
matchHeightToWidth();}
return true;}
function matchHeightToWidth(){
var width=id("resizeWidth").value;
var heightInput=id("resizeHeight");
var expr=/^([0-9]+)(%*)$/;
var match=width.match(expr);
if (match != null){
if (match[2]=='%'){
heightInput.value=Math.round(PXN8.image.height*(match[1] / 100));}else{
heightInput.value=Math.round(PXN8.image.height*(width / PXN8.image.width));}
}
}
function matchWidthToHeight(){
var height=id("resizeHeight").value;
var widthInput=id("resizeWidth");
var expr=/^([0-9]+)(%*)$/;
var match=height.match(expr);
if (match != null){
if (match[2]=='%'){
widthInput.value=Math.round(PXN8.image.width*(match[1] / 100));}else{
widthInput.value=Math.round(PXN8.image.width*(height / PXN8.image.height));}
}
}
function changeDim(axis){
var preserve=id("preserve");
if (preserve.checked){
if (axis=='width'){
matchHeightToWidth();}else{
matchWidthToHeight();}
}
return true;}
function rotateImage(){
var angleCombo=id("angle");
var angle=angleCombo.options[angleCombo.selectedIndex].value;
var flipVt=id("flipvt").checked;
var flipHz=id("fliphz").checked;
if (angle==0 &&
flipVt==false &&
flipHz==false ){
alert(PXN8STRINGS.PROMPT_ROTATE_CHOICE);
return false;}
PXN8.tools.rotate({"angle": angle, "flipvt": flipVt, "fliphz": flipHz});
return false;}
function configRotate(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ rotateImage(); return false;};
var configTitle=cl(id("pxn8_config_title"));
configTitle.appendChild(tx(PXN8STRINGS.CONFIG_ROTATE_TOOL));
unselect();
var configContent=cl(id("pxn8_config_content"));
var helpArea=cl(id("pxn8_tool_prompt"));
unselect();
var form=ac(configContent, ce("form",{onsubmit: fnFalse}));
var tbody=ac(ac(form,ce("table",{width: "100%"})),ce("tbody"));
var row1=ac(tbody,ce("tr"));
var row2=ac(tbody,ce("tr"));
var row3=ac(tbody,ce("tr"));
ac(ac(row1,ce("td")),tx(PXN8STRINGS.FLIPVT_LABEL));
var r1c2=ac(row1, ce("td"));
var flipvt=ac(r1c2,ce("input", { type: "checkbox",name:"flipvt",id:"flipvt"}));
ac(ac(row2,ce("td")),tx(PXN8STRINGS.FLIPHZ_LABEL));
var fliphz=ac(ac(row2,ce("td")),ce("input", {type: "checkbox", name: "fliphz", id: "fliphz"}));
ac(ac(row3,ce("td")),tx(PXN8STRINGS.ANGLE_LABEL));
var sel=ac(ac(row3,ce("td")),ce("select",{name: "angle", id: "angle", className: "pxn8_small_field"}));
var options={"0": " ", "90": "90", "180": "180", "270": "270 "};
var j=0;
for (var i in options){
sel.options[j++]=new Option(options[i], i);}
flipvt.focus();}
function spiritlevelmode (){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="none";
var oldOnImageUpdated=PXN8.onImageUpdated;
var tidyup=function(){
var blackout= id("blackout");
if (blackout){
document.body.removeChild(blackout);}
var prompt=id("prompt");
if (prompt){
document.body.removeChild(prompt);}
initializeCanvas();
var pin1=id("left_pin");
if (pin1){
pin1.style.display="none";}
var pin2=id("right_pin");
if (pin2){
pin2.style.display="none";}
};
var cancelBtn=id("pxn8_cancel");
var handler=cancelBtn.onclick;
cancelBtn.onclick=function(){ tidyup();
handler();
cancelBtn.onclick=handler;
return false;};
var onImageUpdated=null;
onImageUpdated=function(){
tidyup();
PXN8.removeListener(PXN8.ON_IMAGE_CHANGE,onImageUpdated);};
PXN8.addListener(PXN8.ON_IMAGE_CHANGE,onImageUpdated);
var blackout=document.createElement("div");
blackout.id="blackout";
var imgBounds=eb(id("pxn8_image"));
unselect();
blackout.style.position="absolute";
blackout.style.backgroundColor="black";
fnOpacity(blackout,0.7);
blackout.style.top=imgBounds.y+"px";
blackout.style.left=imgBounds.x+(imgBounds.width/2)+"px";
blackout.style.width=(imgBounds.width/2)+"px";
blackout.style.height=imgBounds.height+"px";
document.body.appendChild(blackout);
var prompt=document.createElement("div");
prompt.id="prompt";
prompt.style.position="absolute";
prompt.style.backgroundColor="white";
prompt.style.padding="4px";
prompt.style.top=imgBounds.y+10+"px";
prompt.style.left=imgBounds.x+10+"px";
prompt.style.width=(imgBounds.width/2)- 20+"px";
prompt.style.overflow="auto";
ac(cl(prompt),tx(PXN8STRINGS.SPIRIT_LEVEL_PROMPT1));
document.body.appendChild(prompt);
var configContent=id("pxn8_config_content");
ac(cl(configContent),tx(PXN8STRINGS.SPIRIT_LEVEL_PROMPT1));
ac(cl(id("pxn8_config_title")),tx(PXN8STRINGS.CONFIG_SPIRITLVL_TOOL));
var instructionIndex=0;
var points={ left: {x: 0, y: 0},
right: {x: 0, y: 0}};
cl(id("pxn8_tool_prompt"));
var canvas=id("pxn8_canvas");
canvas.onmousedown=function (event){
event=(event)?event:window.event;
ac(cl(configContent),tx(PXN8STRINGS.SPIRIT_LEVEL_PROMPT2));
var pin1=id("left_pin");
if (pin1==null){
pin1=createPin("left_pin","images/bluepin.gif");
document.body.appendChild(pin1);}
pin1.style.display="block";
var cursorPos=getCursorPosition(event);
pin1.style.left=""+(cursorPos.x -7)+"px";
pin1.style.top=""+(cursorPos.y-24)+"px";
points.left.x=PXN8.position.x;
points.left.y=PXN8.position.y;
blackout.style.left=imgBounds.x+"px";
prompt.style.left=(imgBounds.x+(imgBounds.width/2))+10+"px";
ac(cl(prompt),tx(PXN8STRINGS.SPIRIT_LEVEL_PROMPT2));
canvas.onmousedown=function (event){
event=(event)?event:window.event;
points.right.x=PXN8.position.x;
points.right.y=PXN8.position.y;
var pin2=id("right_pin");
if (pin2==null){
pin2=createPin("right_pin","images/redpin.gif");
document.body.appendChild(pin2);}
pin2.style.display="block";
var cursorPos=getCursorPosition(event); pin2.style.left=""+(cursorPos.x -7)+"px";
pin2.style.top=""+(cursorPos.y-24)+"px";
initializeCanvas();
var blackout= id("blackout");
if (blackout){
document.body.removeChild(blackout);}
var prompt=id("prompt");
if (prompt){
document.body.removeChild(prompt);}
PXN8.tools.spiritlevel(points.left.x,
points.left.y,
points.right.x,
points.right.y);};};}
function configRoundedCorners(element,event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ addRoundedCorners(); return false;};
var configTitle=cl(id("pxn8_config_title"));
configTitle.appendChild(tx(PXN8STRINGS.CONFIG_ROUNDED_TOOL));
var helpArea=id("pxn8_tool_prompt");
cl(helpArea);
var configContent=id("pxn8_config_content");
cl(configContent);
var form=createColorPicker("#FFFFFF",-1,addRoundedCorners);
configContent.appendChild(form);
var table=ac(configContent,ce("table"));
var tbody=ac(table,ce("tbody"));
var row1=ac(tbody,ce("tr"));
var r1c1=ac(row1,ce("td",{width: 48}));
ac(r1c1,tx(PXN8STRINGS.RADIUS_LABEL));
var r1c2=ac(row1,ce("td"));
var input=ac(r1c2,ce("input", {className: "pxn8_small_field", type: "text", name: "radius",
id: "radius",
value: "32"}));
input.onkeypress=function(event){
if (!event) event=window.event;
return CallOnEnter(event,addRoundedCorners);};
setupColorPickerBehaviour("#FFFFFF",-1,addRoundedCorners);
var cf=id("color");
cf.focus();}
function addRoundedCorners(){
var color=id("color");
var radius=id("radius");
PXN8.tools.roundedcorners(color.value, radius.value);
return true;}
function sepiaTone(){
var operation="sepia";
var gs=id("gs");
if (gs.className=="pxn8_checked"){
operation="grayscale";}
if (operation=="sepia"){
PXN8.tools.sepia(id("color").value);}else{
PXN8.tools.grayscale();}
return true;}
function configSepia(element, event){
var applyBtn=id("pxn8_apply");
applyBtn.style.display="inline";
applyBtn.onclick=function(){ sepiaTone(); return false;};
var configTitle=cl(id("pxn8_config_title"));
configTitle.appendChild(tx(PXN8STRINGS.CONFIG_BW_TOOL));
var configContent=cl(id("pxn8_config_content"));
var form=createColorPicker("#A28A65",-1,sepiaTone);
var div=ac(configContent,ce("div",{id: "sepiacolorpicker"}));
ac(div,form);
var form2=ac(configContent,ce("form", {name: "sepia", onsubmit: function(){return false;} }));
var table=ac(form2,ce("table",{width: "100%"}));
var tbody=ac(table,ce("tbody"));
var r1=ac(tbody,ce("tr"));
var r1c1=ac(r1, ce("td"));
var r2=ac(tbody,ce("tr"));
var r2c1=ac(r2, ce("td"));
var d1=ac(r1c1,ce("div",{className:"pxn8_checked", id: "sep"}));
ac(d1,tx(PXN8STRINGS.SEPIA_LABEL));
d1.onclick=function(){
d1.className="pxn8_checked";
var gs=id("gs");
gs.className="pxn8_unchecked";
fnOpacity(id("sepiacolorpicker"),1.0);};
d1.style.cursor="pointer";
var d2=ac(r2c1,ce("div",{className:"pxn8_unchecked", id: "gs"}));
ac(d2,tx(PXN8STRINGS.GRAYSCALE_LABEL));
d2.onclick=function(){
d2.className="pxn8_checked";
var sep=id("sep");
sep.className="pxn8_unchecked";
var spc=id("sepiacolorpicker");
fnOpacity(spc,0.5);
return true;};
d2.style.cursor="pointer";
ac(cl(id("pxn8_tool_prompt")),tx(PXN8STRINGS.BW_PROMPT));
setupColorPickerBehaviour("#A28A65",-1,sepiaTone);
var cf=id("color");
cf.focus();}
function restoreAfterPreview(){
var rects=["left","right","top","bottom"];
for (var i=0;i < rects.length; i++){
var rect=id("pxn8_"+rects[i]+"_rect");
rect.style.backgroundColor=PXN8.style.notSelected.color;
fnOpacity(rect,PXN8.style.notSelected.opacity);}
for (var i in handles){
var handle=id( i+"_handle");
fnOpacity(handle,1.00);}
}
function previewCrop(){
var rects=["left","right","top","bottom"];
for (var i=0;i < rects.length; i++){
var rect=id("pxn8_"+rects[i]+"_rect");
rect.style.backgroundColor="white";
fnOpacity(rect,1.00);}
for (var i in handles){
var handle=id( i+"_handle");
fnOpacity(handle,0);}
setTimeout(restoreAfterPreview,3500);}