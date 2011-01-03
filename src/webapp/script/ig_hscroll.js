dw_scrollObj.stopScroll = function(wnId) {
  if ( dw_scrollObjs[wnId] ) dw_scrollObjs[wnId].endScroll();
}

// increase speed onmousedown of scroll links
dw_scrollObj.doubleSpeed = function(wnId) {
  if ( dw_scrollObjs[wnId] ) dw_scrollObjs[wnId].speed *= 2;
}

dw_scrollObj.resetSpeed = function(wnId) {
  if ( dw_scrollObjs[wnId] ) dw_scrollObjs[wnId].speed /= 2;
}

// algorithms for time-based scrolling and scrolling onmouseover at any angle adapted from youngpup.net
dw_scrollObj.initScroll = function(wnId, deg, sp) {
  if ( dw_scrollObjs[wnId] ) {
    var cosine, sine;
    if (typeof deg == "string") {
      switch (deg) {
        case "up"    : deg = 90;  break;
        case "down"  : deg = 270; break;
        case "left"  : deg = 180; break;
        case "right" : deg = 0;   break;
        default: 
          alert("Direction of scroll in mouseover scroll links should be 'up', 'down', 'left', 'right' or number: 0 to 360.");
       }
    } 
    deg = deg % 360;
    if (deg % 90 == 0) {
      cosine = (deg == 0)? -1: (deg == 180)? 1: 0;
      sine = (deg == 90)? 1: (deg == 270)? -1: 0;
    } else {
      var angle = deg * Math.PI/180;
      cosine = -Math.cos(angle); sine = Math.sin(angle);
    }
    dw_scrollObjs[wnId].fx = cosine / ( Math.abs(cosine) + Math.abs(sine) );
    dw_scrollObjs[wnId].fy = sine / ( Math.abs(cosine) + Math.abs(sine) );
    dw_scrollObjs[wnId].endX = (deg == 90 || deg == 270)? dw_scrollObjs[wnId].x:
      (deg < 90 || deg > 270)? -dw_scrollObjs[wnId].maxX: 0; 
    dw_scrollObjs[wnId].endY = (deg == 0 || deg == 180)? dw_scrollObjs[wnId].y: 
      (deg < 180)? 0: -dw_scrollObjs[wnId].maxY;
    dw_scrollObjs[wnId].startScroll(sp);
  }
}

// speed (optional) to override default speed (set in dw_scrollObj.speed)
dw_scrollObj.prototype.startScroll = function(speed) {
  if (!this.ready) return; if (this.timerId) clearInterval(this.timerId);
  this.speed = speed || dw_scrollObj.speed;
  this.lyr = document.getElementById(this.lyrId);
  this.lastTime = ( new Date() ).getTime();
  this.on_scroll_start();  
  this.timerId = setInterval(this.animString + ".scroll()", 10); 
}

dw_scrollObj.prototype.scroll = function() {
  var now = ( new Date() ).getTime();
  var d = (now - this.lastTime)/1000 * this.speed;
  if (d > 0) {
    var x = this.x + this.fx * d; var y = this.y + this.fy * d;
    if (this.fx == 0 || this.fy == 0) { // for horizontal or vertical scrolling
      if ( ( this.fx == -1 && x > -this.maxX ) || ( this.fx == 1 && x < 0 ) || 
        ( this.fy == -1 && y > -this.maxY ) || ( this.fy == 1 && y < 0 ) ) {
        this.lastTime = now;
        this.shiftTo(this.lyr, x, y);
        this.on_scroll(x, y);
      } else {
        clearInterval(this.timerId); this.timerId = 0;
        this.shiftTo(this.lyr, this.endX, this.endY);
        this.on_scroll_end(this.endX, this.endY);
      }
    } else { // for scrolling at an angle (stop when reach end on one axis)
      if ( ( this.fx < 0 && x >= -this.maxX && this.fy < 0 && y >= -this.maxY ) ||
        ( this.fx > 0 && x <= 0 && this.fy > 0 && y <= 0 ) ||
        ( this.fx < 0 && x >= -this.maxX && this.fy > 0 && y <= 0 ) ||
        ( this.fx > 0 && x <= 0 && this.fy < 0 && y >= -this.maxY ) ) {
        this.lastTime = now;
        this.shiftTo(this.lyr, x, y);
        this.on_scroll(x, y);
      } else {
        clearInterval(this.timerId); this.timerId = 0;
        this.on_scroll_end(this.x, this.y);
      }
    }
  }
}

dw_scrollObj.prototype.endScroll = function() {
  if (!this.ready) return;
  if (this.timerId) clearInterval(this.timerId);
  this.timerId = 0;  this.lyr = null;
}

dw_scrollObj.prototype.on_scroll = function() {}
dw_scrollObj.prototype.on_scroll_start = function() {}
dw_scrollObj.prototype.on_scroll_end = function() {}
  
