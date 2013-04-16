/*----------------------------------------------------------------------------\
|                       Cross Browser Tree Widget 1.13                        |
|-----------------------------------------------------------------------------|
|                          Created by Emil A Eklund                           |
|                  (http://webfx.eae.net/contact.html#emil)                   |
|                      For WebFX (http://webfx.eae.net/)                      |
|-----------------------------------------------------------------------------|
| An object based tree widget,  emulating the one found in microsoft windows, |
| with persistence using cookies. Works in IE 5+, Mozilla and konqueror 3.    |
|-----------------------------------------------------------------------------|
|                   Copyright (c) 1999 - 2002 Emil A Eklund                   |
|-----------------------------------------------------------------------------|
| This software is provided "as is", without warranty of any kind, express or |
| implied, including  but not limited  to the warranties of  merchantability, |
| fitness for a particular purpose and noninfringement. In no event shall the |
| authors or  copyright  holders be  liable for any claim,  damages or  other |
| liability, whether  in an  action of  contract, tort  or otherwise, arising |
| from,  out of  or in  connection with  the software or  the  use  or  other |
| dealings in the software.                                                   |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| This  software is  available under the  three different licenses  mentioned |
| below.  To use this software you must chose, and qualify, for one of those. |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Non-Commercial License          http://webfx.eae.net/license.html |
| Permits  anyone the right to use the  software in a  non-commercial context |
| free of charge.                                                             |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Commercial license           http://webfx.eae.net/commercial.html |
| Permits the  license holder the right to use  the software in a  commercial |
| context. Such license must be specifically obtained, however it's valid for |
| any number of  implementations of the licensed software.                    |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| GPL - The GNU General Public License    http://www.gnu.org/licenses/gpl.txt |
| Permits anyone the right to use and modify the software without limitations |
| as long as proper  credits are given  and the original  and modified source |
| code are included. Requires  that the final product, software derivate from |
| the original  source or any  software  utilizing a GPL  component, such  as |
| this, is also licensed under the GPL license.                               |
|-----------------------------------------------------------------------------|
| 2001-01-10 | Original Version Posted.                                       |
| 2001-03-18 | Added getSelected and get/setBehavior  that can make it behave |
|            | more like windows explorer, check usage for more information.  |
| 2001-09-23 | Version 1.1 - New features included  keyboard  navigation (ie) |
|            | and the ability  to add and  remove nodes dynamically and some |
|            | other small tweaks and fixes.                                  |
| 2002-01-27 | Version 1.11 - Bug fixes and improved mozilla support.         |
| 2002-06-11 | Version 1.12 - Fixed a bug that prevented the indentation line |
|            | from  updating correctly  under some  circumstances.  This bug |
|            | happened when removing the last item in a subtree and items in |
|            | siblings to the remove subtree where not correctly updated.    |
| 2002-06-13 | Fixed a few minor bugs cased by the 1.12 bug-fix.              |
| 2002-08-20 | Added usePersistence flag to allow disable of cookies.         |
|-----------------------------------------------------------------------------|
| Created 2000-12-11 | All changes are in the log above. | Updated 2002-08-20 |
\----------------------------------------------------------------------------*/
  
// -- Determine browser
var IE  = (document.all)? true: false;
var Mac = (navigator.cpuClass && navigator.cpuClass.match(/PPC/))? true: false;
  


var webFXTreeConfig = {
	rootIcon        : 'css/images/foldericon.png',
	openRootIcon    : 'css/images/openfoldericon.png',
	folderIcon      : 'css/images/foldericon.png',
	openFolderIcon  : 'css/images/openfoldericon.png',
	hiddenFolderIcon      : 'css/images/hiddenFoldericon.png',
	hiddenOpenFolderIcon  : 'css/images/hiddenOpenfoldericon.png',
	hiddenProtectedFolderIcon      : 'css/images/hiddenProtectedFoldericon.png',
	hiddenProtectedOpenFolderIcon  : 'css/images/hiddenProtectedOpenfoldericon.png',
	protectedFolderIcon   	: 'css/images/protectedFolder.png',
	protectedOpenFolderIcon : 'css/images/protectedOpenFolder.png',
	fileIcon        : 'css/images/file.png',
	protectedFileIcon : 'css/images/protectedFile.png',
	iIcon           : 'css/images/I.png',
	lIcon           : 'css/images/L.png',
	lMinusIcon      : 'css/images/Lminus.png',
	lPlusIcon       : 'css/images/Lplus.png',
	tIcon           : 'css/images/T.png',
	tMinusIcon      : 'css/images/Tminus.png',
	tPlusIcon       : 'css/images/Tplus.png',
	blankIcon       : 'css/images/blank.png',
	defaultText     : 'Tree Item',
	defaultAction   : 'javascript:void(0);',
	defaultBehavior : 'classic',
	usePersistence	: false,
	showRoot		: true,
	allowDropOnLeafs: false
};

var webFXTreeHandler = {
	idCounter : 0,
	idPrefix  : "webfx-tree-object-",
	all       : {},
	behavior  : null,
	selected  : null,
	onSelect  : function (oItem) {/*alert("onSelect:" + oItem.id);*/ onTreeItemSelect(oItem);}, //    null, /* should be part of tree, not handler */
	getId     : function() { return this.idPrefix + this.idCounter++; },
	toggle    : function (oItem) { this.all[oItem.id.replace('-plus','')].toggle(); },
	select    : function (oItem) { /*alert("Selected:" + oItem.id);*/this.all[oItem.id.replace('-icon','')].select(); },
	focus     : function (oItem) { this.all[oItem.id.replace('-anchor','')].focus(); },
	blur      : function (oItem) { this.all[oItem.id.replace('-anchor','')].blur(); },
	keydown   : function (oItem, e) { return this.all[oItem.id].keydown(e.keyCode); },
	cookies   : new WebFXCookie(),
	insertHTMLBeforeEnd	:	function (oElement, sHTML) {
		if (oElement.insertAdjacentHTML != null) {
			oElement.insertAdjacentHTML("BeforeEnd", sHTML)
			return;
		}
		var df;	// DocumentFragment
		var r = oElement.ownerDocument.createRange();
		r.selectNodeContents(oElement);
		r.collapse(false);
		df = r.createContextualFragment(sHTML);
		oElement.appendChild(df);
	}
};

/*
 * WebFXCookie class
 */

function WebFXCookie() {
	if (document.cookie.length) { this.cookies = ' ' + document.cookie; }
}

var treeCookieName = "treePathCookie";

WebFXCookie.prototype.setCookie = function (key, value) {
	//alert("document.cookie:" + document.cookie);
	var length = document.cookie.split(';').length;
	if(length < 10) //We don't wan't the cookie length to be to long - IE-issues.
	{
		document.cookie = key + "=" + escape(value)+"; expires=Monday, 04-Apr-2020 05:00:00 GMT";
	}
	else
	{
		var startIndex = document.cookie.indexOf(key);
		if(startIndex > -1) //If the key exists we can replace it without affecting the length
		{
			document.cookie = key + "=" + escape(value)+"; expires=Monday, 04-Apr-2020 05:00:00 GMT";
		}
		else
		{
			//alert("To many cookies and no existing cookie for this one...");
		}
	}
		
	/*
	var existingCookie = webFXTreeHandler.cookies.getCookie(treeCookieName);
	if(existingCookie)
	{
		if(existingCookie.length < 400)
		{
			alert("existingCookie:" + existingCookie);
			var allValues = existingCookie + "#" + key + "=" + escape(value);
			
			document.cookie = treeCookieName + "=" + escape(allValues)+"; expires=Monday, 04-Apr-2020 05:00:00 GMT";
		}
	}
	else
	{
		alert("No existingCookie....");
		var allValues = key + "=" + escape(value);
		
		document.cookie = treeCookieName + "=" + escape(allValues)+"; expires=Monday, 04-Apr-2020 05:00:00 GMT";
	}
	*/
}

WebFXCookie.prototype.getCookie = function (key) {
	if (this.cookies) {
		var start = this.cookies.indexOf(' ' + key + '=');
		if (start == -1) { return null; }
		var end = this.cookies.indexOf(";", start);
		if (end == -1) { end = this.cookies.length; }
		end -= start;
		var cookie = this.cookies.substr(start,end);
		return unescape(cookie.substr(cookie.indexOf('=') + 1, cookie.length - cookie.indexOf('=') + 1));
	}
	else { return null; }
}

/*
 * WebFXTreeAbstractNode class
 */

function WebFXTreeAbstractNode(sText, sAction) {
	this.childNodes  = [];
	this.id     = webFXTreeHandler.getId();
	this.text   = sText || webFXTreeConfig.defaultText;
	this.action = sAction || webFXTreeConfig.defaultAction;
	this._last  = false;
	webFXTreeHandler.all[this.id] = this;
}

/*
 * To speed thing up if you're adding multiple nodes at once (after load)
 * use the bNoIdent parameter to prevent automatic re-indentation and call
 * the obj.ident() method manually once all nodes has been added.
 */

WebFXTreeAbstractNode.prototype.add = function (node, bNoIdent) {
	node.parentNode = this;
	this.childNodes[this.childNodes.length] = node;
	var root = this;
	if (this.childNodes.length >=2) {
		this.childNodes[this.childNodes.length -2]._last = false;
	}
	while (root.parentNode) { root = root.parentNode; }
	if (root.rendered) {
		if (this.childNodes.length >= 2) {
			document.getElementById(this.childNodes[this.childNodes.length -2].id + '-plus').src = ((this.childNodes[this.childNodes.length -2].folder)?((this.childNodes[this.childNodes.length -2].open)?webFXTreeConfig.tMinusIcon:webFXTreeConfig.tPlusIcon):webFXTreeConfig.tIcon);
			if (this.childNodes[this.childNodes.length -2].folder) {
				this.childNodes[this.childNodes.length -2].plusIcon = webFXTreeConfig.tPlusIcon;
				this.childNodes[this.childNodes.length -2].minusIcon = webFXTreeConfig.tMinusIcon;
			}
			this.childNodes[this.childNodes.length -2]._last = false;
		}
		this._last = true;
		var foo = this;
		while (foo.parentNode) {
			for (var i = 0; i < foo.parentNode.childNodes.length; i++) {
				if (foo.id == foo.parentNode.childNodes[i].id) { break; }
			}
			if (++i == foo.parentNode.childNodes.length) { foo.parentNode._last = true; }
			else { foo.parentNode._last = false; }
			foo = foo.parentNode;
		}
		webFXTreeHandler.insertHTMLBeforeEnd(document.getElementById(this.id + '-cont'), node.toString());
		//alert("Regging draggable on:" + node.id);
		$("#" + node.id + "").draggable({ helper: 'clone' });
		//$("#" + node.id + " a").draggable({ helper: 'clone' });
		//$("#" + node.id + " img").draggable({ helper: 'clone' });
		//$("#" + node.id + " a").text($("#" + node.id + " a").text() + ":" + node.myType)
		if(webFXTreeConfig.allowDropOnLeafs || node.folder || node.myType == "Folder")
		{
			$("#" + node.id + "").droppable({
				hoverClass: 'ui-state-active',
				tolerance: 'pointer',
			    drop: function(event, ui) { 
					var dragEl = $(ui.draggable); 
					var aTag = dragEl.children("a");
					var movedNodeId = aTag.attr("entityId");
					var parentNodeId = aTag.attr("parentEntityId");
					var newParentNodeId = $("#" + node.id + " a").attr("entityId");		
					
					moveNode(movedNodeId, newParentNodeId, parentNodeId);
				}
			});
		}
		
		if ((!this.folder) && (!this.openIcon)) {
			this.icon = webFXTreeConfig.folderIcon;
			this.openIcon = webFXTreeConfig.openFolderIcon;
		}
		if (!this.folder) { this.folder = true; this.collapse(true); }
		if (!bNoIdent) { this.indent(); }
	}

	if(typeof treeResized == "function")
		treeResized();

	return node;
}

WebFXTreeAbstractNode.prototype.toggle = function() {
	if (this.folder) {
		if (this.open) { this.collapse(); }
		else { this.expand(); }
}	}

WebFXTreeAbstractNode.prototype.select = function() {
	document.getElementById(this.id + '-anchor').focus();
}

WebFXTreeAbstractNode.prototype.deSelect = function() {
	//$("#debug").append("Deselecting" + this.id);
	//alert("ClassNames:" + document.getElementById(this.id + '-anchor').className);
	$("#" + this.id + "-anchor").removeClass("selected");
	//document.getElementById(this.id + '-anchor').className = '';
	webFXTreeHandler.selected = null;
}

WebFXTreeAbstractNode.prototype.focus = function() {
	//$("#debug").append("Focusing on" + this.id + ":" + (webFXTreeHandler.selected ? webFXTreeHandler.selected.id : "") + "<br/>");
	//alert("Focusing on" + this.id + ":" + (webFXTreeHandler.selected ? webFXTreeHandler.selected.id : ""));
	if ((webFXTreeHandler.selected) && (webFXTreeHandler.selected != this)) { webFXTreeHandler.selected.deSelect(); }
	webFXTreeHandler.selected = this;
	if ((this.openIcon) && (webFXTreeHandler.behavior != 'classic')) { document.getElementById(this.id + '-icon').src = this.openIcon; }
	$("#" + this.id + "-anchor").addClass("selected");
	//document.getElementById(this.id + '-anchor').className = 'selected';
	document.getElementById(this.id + '-anchor').focus();
	if (webFXTreeHandler.onSelect) { webFXTreeHandler.onSelect(this); }
}

WebFXTreeAbstractNode.prototype.blur = function() {
	//alert("Blur");
	if ((this.openIcon) && (webFXTreeHandler.behavior != 'classic')) { document.getElementById(this.id + '-icon').src = this.icon; }
	document.getElementById(this.id + '-anchor').className = 'selected-inactive';
}

var ig_expanded_nodes = null;

WebFXTreeAbstractNode.prototype.doExpand = function() {
	if (webFXTreeHandler.behavior == 'classic') { document.getElementById(this.id + '-icon').src = this.openIcon; }
	if (this.childNodes.length) {  document.getElementById(this.id + '-cont').style.display = 'block'; }
	this.open = true;
	if (webFXTreeConfig.usePersistence)
		webFXTreeHandler.cookies.setCookie(this.id.substr(18,this.id.length - 18), '1');
	
	/****************************
	 * Added by Mattias Bogeblad to support InfoGlue state
	 ****************************/
	if(!ig_expanded_nodes_cookie || ig_expanded_nodes_cookie == null)
		alert("There is no ig_expanded_nodes_cookie variable set in the tree-view template.");
		
	//alert("ig_expanded_nodes:" + ig_expanded_nodes);
	if(ig_expanded_nodes == null)	
		ig_expanded_nodes = webFXTreeHandler.cookies.getCookie(ig_expanded_nodes_cookie);
	//alert("ig_expanded_nodes:" + ig_expanded_nodes);
	
	if(ig_expanded_nodes != null && ig_expanded_nodes != "")
	{
		if(ig_expanded_nodes.substring(0, ig_expanded_nodes.indexOf(",")) != "" + this.myId + "" && ig_expanded_nodes.indexOf("," + this.myId + ",") == -1)
			ig_expanded_nodes += "" + this.myId + ",";
	}
	else
	{ 
		if(this.parentNode)
			ig_expanded_nodes = "" + this.parentNode.myId + "," + this.myId + ",";
	}
	  
	//alert("ig_expanded_nodes:" + ig_expanded_nodes);
	webFXTreeHandler.cookies.setCookie(ig_expanded_nodes_cookie, ig_expanded_nodes);
	/********* END Addition *********/	
	
	if(typeof treeResized == "function")
		setTimeout("treeResized();", 50);
}

WebFXTreeAbstractNode.prototype.doCollapse = function() {
	if (webFXTreeHandler.behavior == 'classic') { document.getElementById(this.id + '-icon').src = this.icon; }
	if (this.childNodes.length) { document.getElementById(this.id + '-cont').style.display = 'none'; }
	this.open = false;
	if (webFXTreeConfig.usePersistence)
		webFXTreeHandler.cookies.setCookie(this.id.substr(18,this.id.length - 18), '0');
	
	/****************************
	 * Added by Mattias Bogeblad to support InfoGlue state
	 ****************************/
	if(!ig_expanded_nodes_cookie || ig_expanded_nodes_cookie == null)
		alert("There is no ig_expanded_nodes_cookie variable set in the tree-view template.");

	//alert("ig_expanded_nodes:" + ig_expanded_nodes);
	if(ig_expanded_nodes == null)	
		ig_expanded_nodes = webFXTreeHandler.cookies.getCookie(ig_expanded_nodes_cookie);
	//alert("ig_expanded_nodes:" + ig_expanded_nodes);
	
	if(ig_expanded_nodes != null && ig_expanded_nodes != "")
	{ 
		index = ig_expanded_nodes.indexOf("," + this.myId + ",");
		//alert("index:" + index);
		searchedString = "" + (this.myId).toString() + ",";
		//alert("searchedString:" + searchedString);
		//alert("APA:" + searchedString.substring(0).length);
		searchedStringLength = searchedString.substring(0).length; 
		//alert("searchedStringLength:" + searchedStringLength);
		
		if(ig_expanded_nodes.substring(0, ig_expanded_nodes.indexOf(",")) == "" + this.myId + "")
			ig_expanded_nodes = "";
		else if(index > -1)
			ig_expanded_nodes = ig_expanded_nodes.substring(0, index) + ig_expanded_nodes.substring(index + searchedStringLength);
	}
	  
	//alert("ig_expanded_nodes:" + ig_expanded_nodes);
	webFXTreeHandler.cookies.setCookie(ig_expanded_nodes_cookie, ig_expanded_nodes);
	/********* END Addition *********/	
	
	if(typeof treeResized == "function")
		setTimeout("treeResized();", 50);

}

WebFXTreeAbstractNode.prototype.expandAll = function() {
	this.expandChildren();
	if ((this.folder) && (!this.open)) { this.expand(); }
}

WebFXTreeAbstractNode.prototype.expandChildren = function() {
	for (var i = 0; i < this.childNodes.length; i++) {
		this.childNodes[i].expandAll();
} }

WebFXTreeAbstractNode.prototype.collapseAll = function() {
	this.collapseChildren();
	if ((this.folder) && (this.open)) { this.collapse(true); }
}

WebFXTreeAbstractNode.prototype.collapseChildren = function() {
	for (var i = 0; i < this.childNodes.length; i++) {
		this.childNodes[i].collapseAll();
	} 
}

WebFXTreeAbstractNode.prototype.getNodeWithId = function(id) {
	if (id == this.id) return this;
	for (var i = 0; i < this.childNodes.length; i++) {
		var theNode = this.childNodes[i].getNodeWithId(id);
		if(theNode != null) return theNode;
	}
	return null;
}
WebFXTreeAbstractNode.prototype.getNodeWithMyId = function(id) {
	// alert("this.myId=" + this.myId + " searching for: " + id);
	if (id == this.myId) return this;
	for (var i = 0; i < this.childNodes.length; i++) {
		var theNode = this.childNodes[i].getNodeWithMyId(id);
		if(theNode != null) return theNode;
	}
	return null;
}

WebFXTreeAbstractNode.prototype.indent = function(lvl, del, last, level, nodesLeft) {
	/*
	 * Since we only want to modify items one level below ourself,
	 * and since the rightmost indentation position is occupied by
	 * the plus icon we set this to -2
	 */
	if (lvl == null) { lvl = -2; }
	var state = 0;
	for (var i = this.childNodes.length - 1; i >= 0 ; i--) {
		state = this.childNodes[i].indent(lvl + 1, del, last, level);
		if (state) { return; }
	}
	if (del) {
		if ((level >= this._level) && (document.getElementById(this.id + '-plus'))) {
			if (this.folder) {
				document.getElementById(this.id + '-plus').src = (this.open)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.lPlusIcon;
				this.plusIcon = webFXTreeConfig.lPlusIcon;
				this.minusIcon = webFXTreeConfig.lMinusIcon;
			}
			else if (nodesLeft) { document.getElementById(this.id + '-plus').src = webFXTreeConfig.lIcon; }
			return 1;
	}	}
	var foo = document.getElementById(this.id + '-indent-' + lvl);
	if (foo) {
		if ((foo._last) || ((del) && (last))) { foo.src =  webFXTreeConfig.blankIcon; }
		else { foo.src =  webFXTreeConfig.iIcon; }
	}
	return 0;
}

/*
 * WebFXTree class
 */

function WebFXTree(sText, sAction, sBehavior, sIcon, sOpenIcon, isProtected, stateId) {
	this.base = WebFXTreeAbstractNode;
	this.base(sText, sAction);
	this.isProtected = isProtected;
	this.stateId = stateId;
	
	this.icon      = sIcon || webFXTreeConfig.rootIcon;
	this.openIcon  = sOpenIcon || webFXTreeConfig.openRootIcon;
	/* Defaults to open */
	if (webFXTreeConfig.usePersistence)
		this.open  = (webFXTreeHandler.cookies.getCookie(this.id.substr(18,this.id.length - 18)) == '0')?false:true;
	else
		this.open  = true;
	this.folder    = true;
	this.rendered  = false;
	this.onSelect  = null;
	if (!webFXTreeHandler.behavior) {  webFXTreeHandler.behavior = sBehavior || webFXTreeConfig.defaultBehavior; }
}

WebFXTree.prototype = new WebFXTreeAbstractNode;

WebFXTree.prototype.setBehavior = function (sBehavior) {
	webFXTreeHandler.behavior =  sBehavior;
};

WebFXTree.prototype.getBehavior = function (sBehavior) {
	return webFXTreeHandler.behavior;
};

WebFXTree.prototype.getSelected = function() {
	if (webFXTreeHandler.selected) { return webFXTreeHandler.selected; }
	else { return null; }
}

WebFXTree.prototype.remove = function() { }

WebFXTree.prototype.expand = function() {
	this.doExpand();
}

WebFXTree.prototype.collapse = function(b) {
	if (!b) { this.focus(); }
	this.doCollapse();
}

WebFXTree.prototype.getFirst = function() {
	return null;
}

WebFXTree.prototype.getLast = function() {
	return null;
}

WebFXTree.prototype.getNextSibling = function() {
	return null;
}

WebFXTree.prototype.getPreviousSibling = function() {
	return null;
}

WebFXTree.prototype.keydown = function(key) {
	if (key == 39) {
		if (!this.open) { this.expand(); }
		else if (this.childNodes.length) { this.childNodes[0].select(); }
		return false;
	}
	if (key == 37) { this.collapse(); return false; }
	if ((key == 40) && (this.open) && (this.childNodes.length)) { this.childNodes[0].select(); return false; }
	return true;
}

WebFXTree.prototype.toString = function() {
	
	var str = "";
	if(webFXTreeConfig.showRoot)	
	{
		str += "<div id=\"" + this.id + "\" ondblclick=\"webFXTreeHandler.toggle(this);\" class=\"webfx-tree-item\" onkeydown=\"return webFXTreeHandler.keydown(this, event)\">";
		str += "<img id=\"" + this.id + "-icon\" class=\"webfx-tree-icon\" src=\"" + ((webFXTreeHandler.behavior == 'classic' && this.open)?this.openIcon:this.icon) + "\" onclick=\"onTreeItemSelect('" + this.id + "'); onTreeItemClick(this,'"+ this.myId + "','-1','" + this.text + "');\"><a href=\"" + this.action + "\" id=\"" + this.id + "-anchor\" entityId=\"" + this.myId + "\" parentEntityId=\"-1\" onclick=\"webFXTreeHandler.focus(this); onTreeItemSelect('" + this.id + "');\" onfocus=\"webFXTreeHandler.focus(this);\" onblur=\"webFXTreeHandler.blur(this);\" oncontextmenu=\"return showContextMenu(" + this.myId + ", event, window);\" style=\"-moz-user-select: none; -webkit-user-select: none;\">" + this.text + "</a></div>";
	}
	str += "<div id=\"" + this.id + "-cont\" class=\"webfx-tree-container\" style=\"display: " + ((this.open)?'block':'none') + ";\">";
	for (var i = 0; i < this.childNodes.length; i++) {
		str += this.childNodes[i].toString(i, this.childNodes.length);
	}
	str += "</div>";
	this.rendered = true;
	return str;
};

/*
 * WebFXTreeItem class
 */

function WebFXTreeItem(sText, sAction, eParent, sIcon, sOpenIcon, isProtected, stateId) {
	this.base = WebFXTreeAbstractNode;
	this.base(sText, sAction);
	this.isProtected = isProtected;
	this.stateId = stateId;
	
	/* Defaults to close */
	if (webFXTreeConfig.usePersistence)
		this.open = (webFXTreeHandler.cookies.getCookie(this.id.substr(18,this.id.length - 18)) == '1')?true:false;
	else
		this.open = false;
	if (sIcon) { this.icon = sIcon; }
	if (sOpenIcon) { this.openIcon = sOpenIcon; }
	if (eParent) { eParent.add(this); }
}

WebFXTreeItem.prototype = new WebFXTreeAbstractNode;

WebFXTreeItem.prototype.remove = function() {
	var iconSrc = document.getElementById(this.id + '-plus').src;
	var parentNode = this.parentNode;
	var prevSibling = this.getPreviousSibling(true);
	var nextSibling = this.getNextSibling(true);
	var folder = this.parentNode.folder;
	var last = ((nextSibling) && (nextSibling.parentNode) && (nextSibling.parentNode.id == parentNode.id))?false:true;
	this.getPreviousSibling().focus();
	this._remove();
	if (parentNode.childNodes.length == 0) {
		document.getElementById(parentNode.id + '-cont').style.display = 'none';
		parentNode.doCollapse();
		//parentNode.folder = false;
		parentNode.open = false;
	}
	if (!nextSibling || last) { parentNode.indent(null, true, last, this._level, parentNode.childNodes.length); }
	if ((prevSibling == parentNode) && !(parentNode.childNodes.length)) {
		// prevSibling.folder = false;
		prevSibling.open = false;
		iconSrc = document.getElementById(prevSibling.id + '-plus').src;
		iconSrc = iconSrc.replace('minus', '').replace('plus', '');
		document.getElementById(prevSibling.id + '-plus').src = iconSrc;
		// document.getElementById(prevSibling.id + '-icon').src = webFXTreeConfig.fileIcon;
	}
	if (document.getElementById(prevSibling.id + '-plus')) {
		if (parentNode == prevSibling.parentNode) {
			iconSrc = iconSrc.replace('minus', '').replace('plus', '');
			document.getElementById(prevSibling.id + '-plus').src = iconSrc;
}	}	}

WebFXTreeItem.prototype._remove = function() {
	for (var i = this.childNodes.length - 1; i >= 0; i--) {
		this.childNodes[i]._remove();
 	}
	for (var i = 0; i < this.parentNode.childNodes.length; i++) {
		if (this == this.parentNode.childNodes[i]) {
			for (var j = i; j < this.parentNode.childNodes.length; j++) {
				this.parentNode.childNodes[j] = this.parentNode.childNodes[j+1];
			}
			this.parentNode.childNodes.length -= 1;
			if (i + 1 == this.parentNode.childNodes.length) { this.parentNode._last = true; }
			break;
	}	}
	webFXTreeHandler.all[this.id] = null;
	var tmp = document.getElementById(this.id);
	if (tmp) { tmp.parentNode.removeChild(tmp); }
	tmp = document.getElementById(this.id + '-cont');
	if (tmp) { tmp.parentNode.removeChild(tmp); }
}

WebFXTreeItem.prototype.expand = function() {
	this.doExpand();
	document.getElementById(this.id + '-plus').src = this.minusIcon;
}

WebFXTreeItem.prototype.collapse = function(b) {
	if (!b) { this.focus(); }
	this.doCollapse();
	document.getElementById(this.id + '-plus').src = this.plusIcon;
}

WebFXTreeItem.prototype.getFirst = function() {
	return this.childNodes[0];
}

WebFXTreeItem.prototype.getLast = function() {
	//alert("We think childNodes are:" + this.childNodes.length + " for " + $("#" + this.id + "-anchor").text());
	if(this.childNodes[this.childNodes.length - 1])
	{	
		if (this.childNodes[this.childNodes.length - 1].open) { return this.childNodes[this.childNodes.length - 1].getLast(); }
		else { return this.childNodes[this.childNodes.length - 1]; }
	}
	else
	{
		//alert("Problem getting last... what happens now..");
		return this;
	}
}

WebFXTreeItem.prototype.getNextSibling = function() {
	for (var i = 0; i < this.parentNode.childNodes.length; i++) {
		if (this == this.parentNode.childNodes[i]) { break; }
	}
	if (++i == this.parentNode.childNodes.length) { return this.parentNode.getNextSibling(); }
	else { return this.parentNode.childNodes[i]; }
}

WebFXTreeItem.prototype.getPreviousSibling = function(b) {
	for (var i = 0; i < this.parentNode.childNodes.length; i++) {
		if (this == this.parentNode.childNodes[i]) { break; }
	}
	if (i == 0) { return this.parentNode; }
	else 
	{
		if ((this.parentNode.childNodes[--i].open) || (b && this.parentNode.childNodes[i].folder)) 
		{ 
			return this.parentNode.childNodes[i].getLast(); 
		}
		else 
		{ 
			return this.parentNode.childNodes[i]; 
		}
} }

WebFXTreeItem.prototype.keydown = function(key) {
	if ((key == 39) && (this.folder)) {
		if (!this.open) { this.expand(); }
		else { this.getFirst().select(); }
		return false;
	}
	else if (key == 37) {
		if (this.open) { this.collapse(); }
		else { this.parentNode.select(); }
		return false;
	}
	else if (key == 40) {
		if (this.open) { this.getFirst().select(); }
		else {
			var sib = this.getNextSibling();
			if (sib) { sib.select(); }
		}
		return false;
	}
	else if (key == 38) { this.getPreviousSibling().select(); return false; }
	return true;
}

WebFXTreeItem.prototype.toString = function (nItem, nItemCount) {

	var isContainer = "na";
	/*if(this.myType != null)
		isContainer = (this.myType.indexOf("Folder") > -1)?"true":"false";
	*/
	
	var foo = this.parentNode;
	var indent = '';
	if (nItem + 1 == nItemCount) { this.parentNode._last = true; }
	var i = 0;
	while (foo.parentNode) {
		foo = foo.parentNode;
		indent = "<img id=\"" + this.id + "-indent-" + i + "\" src=\"" + ((foo._last)?webFXTreeConfig.blankIcon:webFXTreeConfig.iIcon) + "\">" + indent;
		i++;
	}
	this._level = i;
	if (this.childNodes.length) { this.folder = 1; }
	else { this.open = false; }
	if ((this.folder) || (webFXTreeHandler.behavior != 'classic')) {
		if (!this.icon) { this.icon = webFXTreeConfig.folderIcon; }
		if (!this.openIcon) { this.openIcon = webFXTreeConfig.openFolderIcon; }
	}
	else if (!this.icon) { 
		this.icon = webFXTreeConfig.fileIcon;
		if(this.isProtected == 'true') {
			this.icon = webFXTreeConfig.protectedFileIcon;
		}
	}
	var styleInfo = "";
	var stateInfo = "";
	var classInfo = "";
	
	if(this.stateId == "0") {
		classInfo = "workingTreeItem";
		stateInfo = " (working)";
		if(languageCode == "sv")
			stateInfo = " (arbetskopia)";
	}
	else if(this.stateId == "1"){ 
		classInfo = "finishedTreeItem";
		stateInfo = " (finished)";
		if(languageCode == "sv")
			stateInfo = " (slutf�rd)";
	}
	else if(this.stateId == "2"){ 
		classInfo = "publishTreeItem";
		stateInfo = " (publish)";
		if(languageCode == "sv")
			stateInfo = " (publicerbar)";
	}
	else
	{
		classInfo = "publishedTreeItem";		
	}
		
	var label = this.text.replace(/</g, '&lt;').replace(/>/g, '&gt;');
	var str = "<div id=\"" + this.id + "\" ondblclick=\"webFXTreeHandler.toggle(this);\" class=\"webfx-tree-item\" onkeydown=\"return webFXTreeHandler.keydown(this, event)\">";
	str += indent;
	str += "<img id=\"" + this.id + "-plus\" src=\"" + ((this.folder)?((this.open)?((this.parentNode._last)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.tMinusIcon):((this.parentNode._last)?webFXTreeConfig.lPlusIcon:webFXTreeConfig.tPlusIcon)):((this.parentNode._last)?webFXTreeConfig.lIcon:webFXTreeConfig.tIcon)) + "\" onclick=\"webFXTreeHandler.toggle(this);\">"
	str += "<img id=\"" + this.id + "-icon\" class=\"webfx-tree-icon\" src=\"" + ((webFXTreeHandler.behavior == 'classic' && this.open)?this.openIcon:this.icon) + "\" onclick=\"onTreeItemSelect('" + this.id + "');" + this.action + "\"><a isContainer=\"" + isContainer + "\" entityId=\"" + this.myId + "\" parentEntityId=\"" + this.parentNode.myId + "\" href=\"javascript:void(" + this.myId + ");\" id=\"" + this.id + "-anchor\" oncontextmenu=\"return showContextMenu(" + this.myId + ", event, window);\" onclick=\"webFXTreeHandler.focus(this); onTreeItemSelect('" + this.id + "');" + this.action + "\" ondblclick=\"if(window.onTreeItemDblClick)onTreeItemDblClick('" + this.myId + "', '" + label + "');\" onfocus=\"webFXTreeHandler.focus(this);\" class=\"" + classInfo + "\" style=\"" + styleInfo + " -moz-user-select: none; -webkit-user-select: none;\">" + label + stateInfo + "</a></div>";
	str += "<div id=\"" + this.id + "-cont\" class=\"webfx-tree-container\" style=\"display: " + ((this.open)?'block':'none') + ";\">";
	for (var i = 0; i < this.childNodes.length; i++) {
		str += this.childNodes[i].toString(i,this.childNodes.length);
	}
	str += "</div>";
	this.plusIcon = ((this.parentNode._last)?webFXTreeConfig.lPlusIcon:webFXTreeConfig.tPlusIcon);
	this.minusIcon = ((this.parentNode._last)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.tMinusIcon);
	return str;
}