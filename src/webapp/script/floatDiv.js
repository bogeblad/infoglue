var isDragged = false;

var ns = (navigator.appName.indexOf("Netscape") != -1);
var d = document;
var px = document.layers ? "" : "px";
var elementArray = new Array();

function floatDiv(id, sx, sy)
{
	var el=d.getElementById?d.getElementById(id):d.all?d.all[id]:d.layers[id];
	//alert("id:"+id + "=" + el);
	window[id + "_obj"] = el;
	elementArray["" + id + "_obj"] = el;
	//alert("elementArray:" + elementArray.length);
	//alert("elementArray:" + elementArray.toString());
	if(d.layers)el.style=el;
	el.cx = el.sx = sx;el.cy = el.sy = sy;
	el.sP=function(x,y){this.style.left=x+px;this.style.top=y+px;};
	el.flt=function()
	{
		if(isDragged)
		{
			//alert("isDragged:" + isDragged);
			//el.cy = el.sy = 50;
		}
		else
		{
			var pX, pY;
			pX = (this.sx >= 0) ? 0 : ns ? innerWidth : 
			document.documentElement && document.documentElement.clientWidth ? 
			document.documentElement.clientWidth : document.body.clientWidth;
			pY = ns ? pageYOffset : document.documentElement && document.documentElement.scrollTop ? 
			document.documentElement.scrollTop : document.body.scrollTop;
			if(this.sy<0) 
			pY += ns ? innerHeight : document.documentElement && document.documentElement.clientHeight ? 
			document.documentElement.clientHeight : document.body.clientHeight;
			this.cx += (pX + this.sx - this.cx)/2;this.cy += (pY + this.sy - this.cy)/2;
			this.sP(this.cx, this.cy);
			setTimeout(this.id + "_obj.flt()", 50);
		}
	}
	return el;
}
