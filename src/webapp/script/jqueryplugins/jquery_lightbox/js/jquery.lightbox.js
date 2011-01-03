/*
 * jQuery Lightbox Plugin (balupton edition) - Lightboxes for jQuery
 * Copyright (C) 2008 Benjamin Arthur Lupton
 *
 * This file is part of jQuery Lightbox (balupton edition).
 * 
 * jQuery Lightbox (balupton edition) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * jQuery Lightbox (balupton edition) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with jQuery Lightbox (balupton edition).  If not, see <http://www.gnu.org/licenses/>.
 *
 * @name jquery_lightbox: jquery.lightbox.js
 * @package jQuery Lightbox Plugin (balupton edition)
 * @version 1.0.1-final
 * @date January 09, 2008
 * @category jQuery plugin
 * @author Benjamin "balupton" Lupton {@link http://www.balupton.com}
 * @copyright (c) 2008 Benjamin Arthur Lupton {@link http://www.balupton.com}
 * @license GNU Affero General Public License - {@link http://www.gnu.org/licenses/agpl.html}
 * @example Visit {@link http://jquery.com/plugins/project/jquerylightbox_bal} for more information.
 */

// Start of our jQuery Plugin
(function($)
{	// Create our Plugin function, with $ as the argument (we pass the jQuery object over later)
	// More info: http://docs.jquery.com/Plugins/Authoring#Custom_Alias
	
	// Declare our class
	$.LightboxClass = function ( )
	{	// This is the handler for our constructor
		this.construct();
	};

	// Extend jQuery elements for Lightbox
	$.fn.lightbox = function ( options )
	{	// Init a el for Lightbox
		// Eg. $('#gallery a').lightbox();
		
		// If need be: Instantiate $.LightboxClass to $.Lightbox
		$.Lightbox = $.Lightbox || new $.LightboxClass();
		
		// Establish options
		options = $.extend({start:false,events:true}, options);
		
		// Get group
		var group = $(this);
		
		// Events?
		if ( options.events )
		{	// Add events
			$(group).click(function(){
				// Get obj
				var obj = $(this);
				// Get rel
				// var rel = $(obj).attr('rel');
				// Init group
				if ( !$.Lightbox.init($(obj)[0], group) )
				{	return false;	}
				// Display lightbox
				if ( !$.Lightbox.start() )
				{	return false;	}
				// Cancel href
				return false;
			});
			// Add style
			$(group).addClass('lightbox-enabled');
		}
		
		// Start?
		if ( options.start )
		{	// Start
			// Get obj
			var obj = $(this);
			// Get rel
			// var rel = $(obj).attr('rel');
			// Init group
			if ( !$.Lightbox.init($(obj)[0], group) )
			{	return this;	}
			// Display lightbox
			if ( !$.Lightbox.start() )
			{	return this;	}
		}
		
		// And chain
		return this;
	};
	
	// Define our class
	$.extend($.LightboxClass.prototype,
	{	// Our LightboxClass definition
		
		// -----------------
		// Everyting to do with images
		
		images: {
			
			// -----------------
			// Variables
			
			// Our array of images
			list:[], /* [ {
				src: 'url to image',
				link: 'a link to a page',
				title: 'title of the image',
				description: 'description of the image'
			} ], */
			
			// The current active image
			image: false,
			
			// -----------------
			// Functions
			
			prev: function ( image )
			{	// Get previous image
				
				// Get previous from current?
				if ( typeof image === 'undefined' )
				{	image = this.active();
					if ( !image ) { return image; }
				}
				
				// Is there a previous?
				if ( this.first(image) )
				{	return false;	}
				
				// Get the previous
				return this.get(image.index-1);
			},
			
			next: function ( image )
			{	// Get next image
				
				// Get next from current?
				if ( typeof image === 'undefined' )
				{	image = this.active();
					if ( !image ) { return image; }
				}
				
				// Is there a next?
				if ( this.last(image) )
				{	return false;	}
				
				// Get the next
				return this.get(image.index+1);
			},
			
			first: function ( image )
			{	//
				// Get the first image?
				if ( typeof image === 'undefined' )
				{	return this.get(0);	}
				
				// Are we the first?
				return image.index === 0;
			},
			
			last: function ( image )
			{	//
				// Get the last image?
				if ( typeof image === 'undefined' )
				{	return this.get(this.size()-1);	}
				
				// Are we the last?
				return image.index === this.size()-1;
			},
		
			single: function ( )
			{	// Are we only one
				return this.size() === 1;
			},
			
			size: function ( )
			{	// How many images do we have
				return this.list.length;
			},
			
			empty: function ( )
			{	// Are we empty
				return this.size() === 0;
			},
			
			clear: function ( )
			{	// Clear image arrray
				this.list = [];
				this.image = false;
			},
		
			active: function ( image )
			{	// Set or get the active image
				
				// Get the active image?
				if ( typeof image === 'undefined' )
				{	return this.image;	}
				
				// Set the ative image
				
				// Make sure image exists
				image = this.get(image);
				if ( !image ) { return image; }
				
				// Make it the active
				this.image = image;
				
				// Done
				return true;
			},
		
			add: function ( obj )
			{
				// Do we need to recurse?
				if ( obj[0] )
				{	// We have a lot of images
					for ( var i = 0; i < obj.length; i++ )
					{	this.add(obj[i]);	}
					return true;
				}
				
				// Default image
				
				// Try and create a image
				var image = this.create(obj);
				if ( !image ) { return image; }
				
				// Set image index
				image.index = this.size();
				
				// Push image
				this.list.push(image);
				
				// Success
				return true;
			},
			
			create: function ( obj )
			{	// Create image
				
				// Define
				var image = { // default
					src:	'',
					title:	'Untitled',
					description:	'',
					index:	-1,
					image:	true
				};
				
				// Create
				if ( obj.image )
				{	// Already a image, so copy over values
					image.src = obj.src || image.src;
					image.title = obj.title || image.title;
					image.description = obj.description || image.description;
					image.index = obj.index || image.index;
				}
				else if ( obj.tagName )
				{	// We are an element
					obj = $(obj);
					if ( obj.attr('src') || obj.attr('href') )
					{
						image.src = obj.attr('src') || obj.attr('href');
						image.title = obj.attr('title') || obj.attr('alt') || image.title;
						// Extract description from title
						var s = image.title.indexOf(': ');
						if ( s > 0 )
						{	// Description exists
							image.description = image.title.substring(s+2) || image.description;
							image.title = image.title.substring(0,s) || image.title;
						}
					}
					else
					{	// Unsupported element
						image = false;
					}
				}
				else
				{	// Unknown
					image = false;
				}
				
				if ( !image )
				{	// Error
					this.debug('We dont know what we have:', obj);
					return false;
				}
				
				// Success
				return image;
			},
			
			get: function ( image )
			{	// Get the active, or specified image
				
				// Establish image
				if ( image === undefined || image === null )
				{	// Get the active image
					return this.active();
				}
				else
				if ( typeof image === 'number' )
				{	// We have a index
					
					// Get image
					image = this.list[image] || false;
				}
				else
				{	// Create
					image = this.create(image);
					if ( !image ) { return false; }
					
					// Find
					var f = false;
					for ( var i = 0; i < this.size(); i++ )
					{
						var c = this.list[i];
						if ( c.src === image.src && c.title === image.title && c.description === image.description )
						{	f = c;	}
					}
					
					// Found?
					image = f;
				}
				
				// Determine image
				if ( !image )
				{	// Image doesn't exist
					this.debug('The desired image doesn\'t exist: ', image, this.list);
					return false;
				}
				
				// Return image
				return image;
			},
			
			debug: function ( )
			{
				return $.Lightbox.debug(arguments);
			}
			
		},
		
		// -----------------
		// Locations
		
		baseurl:			'',
		
		files: {
			// If you are doing a repack with packer (http://dean.edwards.name/packer/) then append ".packed" onto the js and css files before you pack it.
			js: {
				lightbox:	'js/jquery.lightbox.js'
			},
			css: {
				lightbox:	'css/jquery.lightbox.css'
			},
			images: {
				prev:		'images/prev.gif',
				next:		'images/next.gif',
				blank:		'images/blank.gif',
				loading:	'images/loading.gif'
			}
		},
		
		text: {
			// For translating
			image:		'Image',
			of:			'of',
			close:		'Close X',
			closeInfo:	'You can also click anywhere outside the image to close',
			help: {
				close:		'Click to close',
				interact:	'Hover to interact'
			}	
		},
		
		keys: {
			close:	'c',
			prev:	'p',
			next:	'n'
		},
		
		opacity:	0.9,
		padding:	null,	// if null - autodetect
		
		speed:		400,	// Duration of effect, milliseconds
		
		rel:		'lightbox',	// What to look for in the rels
		
		// -----------------
		// Functions
		
		construct: function( )
		{	// Construct our Lightbox
			
			// Set baseurl
			// Get the src of the first script tag that includes our js file (with or without an appendix)
			this.baseurl = $('script[src*='+this.files.js.lightbox+']:first').attr('src');
			// The baseurl is the src up until the start of our js file
			this.baseurl = this.baseurl.substring(0, this.baseurl.indexOf(this.files.js.lightbox));
			
			// Apply baseurl to files
			var me = this;
			$.each(this.files, function(group, val){
				$.each(this, function(file, val){
					me.files[group][file] = me.baseurl+val;
				});
			});
			
			// All good
			return true;
		},
		
		domReady: function ( )
		{
			// -------------------
			// Append display
			
			// Include stylesheet
			$('head').append('<link rel="stylesheet" type="text/css" href="'+ this.files.css.lightbox +'" media="screen" />');
			
			// Append markup
			$('body').append('<div id="lightbox-overlay"><div id="lightbox-overlay-text"><p><span id="lightbox-overlay-text-about"><a href="#">Lightbox jQuery Plugin (balupton edition)</a></span></p><p>&nbsp;</p><p><span id="lightbox-overlay-text-close">'+this.text.help.close+'</span><br/>&nbsp;<span id="lightbox-overlay-text-interact">'+this.text.help.interact+'</span></p></div></div><div id="lightbox"><div id="lightbox-imageBox"><div id="lightbox-imageContainer"><img id="lightbox-image" /><div id="lightbox-nav"><a href="#" id="lightbox-nav-btnPrev"></a><a href="#" id="lightbox-nav-btnNext"></a></div><div id="lightbox-loading"><a href="#" id="lightbox-loading-link"><img src="' + this.files.images.loading + '" /></a></div></div></div><div id="lightbox-infoBox"><div id="lightbox-infoContainer"><div id="lightbox-infoHeader"><span id="lightbox-caption"><span id="lightbox-caption-title"></span><span id="lightbox-caption-description"></span></span></div><div id="lightbox-infoFooter"><span id="lightbox-currentNumber"></span><span id="lightbox-close"><a href="#" id="lightbox-close-button" title="'+this.text.closeInfo+'">' + this.text.close + '</a></span></div><div id="lightbox-infoContainer-clear"></div></div></div></div>');
			
			// Update Boxes - for some crazy reason this has to be before the hide in safari and konqueror
			this.resizeBoxes();
			this.repositionBoxes();
			
			// Hide
			$('#lightbox,#lightbox-overlay,#lightbox-overlay-text-interact').hide();
			
			// -------------------
			// Preload Images
			
			// Cycle and preload
			$.each(this.files.images, function()
			{	// Proload the image
				var preloader = new Image();
				preloader.onload = function() {
					preloader.onload = null;
					preloader = null;
				};	preloader.src = this;
			});
			
			// -------------------
			// Apply events
			
			// If the window resizes, act appropriatly
			$(window).resize(function () { $.Lightbox.resizeBoxes(); $.Lightbox.repositionBoxes(); });
			
			// Prev
			$('#lightbox-nav-btnPrev').hover(function() { // over
				$(this).css({ 'background' : 'url(' + $.Lightbox.files.images.prev + ') left 45% no-repeat' });
			},function() { // out
				$(this).css({ 'background' : 'transparent url(' + $.Lightbox.files.images.blank + ') no-repeat' });
			}).click(function() {
				$.Lightbox.showImage($.Lightbox.images.prev());
				return false;
			});
					
			// Next
			$('#lightbox-nav-btnNext').hover(function() { // over
				$(this).css({ 'background' : 'url(' + $.Lightbox.files.images.next + ') right 45% no-repeat' });
			},function() { // out
				$(this).css({ 'background' : 'transparent url(' + $.Lightbox.files.images.blank + ') no-repeat' });
			}).click(function() {
				$.Lightbox.showImage($.Lightbox.images.next());
				return false;
			});
			
			// Help
			$('#lightbox-overlay-text-about a').click(function(){window.open('http://jquery.com/plugins/project/jquerylightbox_bal'); return false;});
			$('#lightbox-overlay-text-close').hover(
				function(){
					$('#lightbox-overlay-text-interact').fadeIn();
				},
				function(){
					$('#lightbox-overlay-text-interact').fadeOut();
				}
			);
			
			// -------------------
			// Finish Up
			
			// Relify
			$.Lightbox.relify();
			
			// All good
			return true;
		},
		
		relify: function ( )
		{	// Create event
		
			//
			var groups = {};
			var groups_n = 0;
			var orig_rel = this.rel;
			// Create the groups
			$.each($('[@rel*='+orig_rel+']'), function(index, obj){
				// Get the group
				var rel = $(obj).attr('rel');
				// Are we really a group
				if ( rel === orig_rel )
				{	// We aren't
					rel = groups_n; // we are individual
				}
				// Does the group exist
				if ( typeof groups[rel] === 'undefined' )
				{	// Make the group
					groups[rel] = [];
					groups_n++;
				}
				// Append the image
				groups[rel].push(obj);
			});
			// Lightbox groups
			$.each(groups, function(index, group){
				$(group).lightbox();
			});
			// Done
			return true;
		},
		
		init: function ( image /*int*/, images /*[]*/ )
		{	// Init a batch of lightboxes
			
			// Establish images
			if ( typeof images === 'undefined' )
			{
				images = image;
				image = 0;
			}
			
			// Clear
			this.images.clear();
			
			// Add images
			if ( !this.images.add(images) )
			{	return false;	}
			
			// Do we need to bother
			if ( this.images.empty() )
			{	// No images
				this.debug('Lightbox started, but no images: ', image, images);
				return false;
			}
			
			// Set active
			if ( !this.images.active(image) )
			{	return false;	}
			
			// Done
			return true;
		},
		
		start: function ( )
		{	// Display the lightbox
			
			// Fix attention seekers
			$('embed, object, select').css({ 'visibility' : 'hidden' });//.hide(); - don't use this, give it a go, find out why!
			
			// Resize the boxes appropriatly
			this.resizeBoxes();
			
			// Hide things
			$('#lightbox-infoFooter').hide(); // we hide this here because it makes the display smoother
			
			// Display the boxes
			$('#lightbox-overlay').css({
				opacity:	this.opacity
			}).fadeIn();
			$('#lightbox').show();
			
			// Assign close clicks
			$('#lightbox-overlay, #lightbox, #lightbox-loading-link, #lightbox-btnClose').click(function() {
				$.Lightbox.finish();
				return false;	
			});
			
			// Display first image
			if ( !this.showImage(this.images.active()) )
			{	this.finish();	return false;	}
			
			// All done
			return true;
		},
		
		finish: function ( )
		{	// Get rid of lightbox
			$('#lightbox').hide();
			$('#lightbox-overlay').fadeOut(function() { $('#lightbox-overlay').hide(); });
			// Fix attention seekers
			$('embed, object, select').css({ 'visibility' : 'visible' });//.show();
		},
		
		resizeBoxes: function ( )
		{
			// Get the page size
			var pageSize = this.getPageSize();
			
			// Style overlay and show it
			$('#lightbox-overlay').css({
				width:		pageSize.largestWidth,
				height:		pageSize.largestHeight
			});
		},
		
		repositionBoxes: function ( options )
		{
			// Options
			options = $.extend({}, options);
			
			// Get the page size
			var pageSize = this.getPageSize();
			
			// Get page scroll
			var pageScroll = this.getPageScroll();
			
			// Figure it out
			var nHeight = options.nHeight || parseInt($('#lightbox').height(),10) || pageSize.largestHeight/3;
			
			// Display lightbox in center
			var nTop = pageScroll.yScroll + (pageSize.windowHeight - nHeight) / 2.5;
			var nLeft = pageScroll.xScroll;
			$('#lightbox').animate({left:nLeft, top:nTop}, 'slow');
		},
		
		showImage: function ( image, options )
		{
			// Establish image
			image = this.images.get(image);
			if ( !image ) { return image; }
			
			// Establish options
			options = $.extend({step:1}, options);
			// Split up below for jsLint compliance
			var skipped_step_1 = options.step > 1 && this.images.active().src !== image.src;
			var skipped_step_2 = options.step > 2 && $('#lightbox-image').attr('src') !== image.src;
			if ( skipped_step_1 || skipped_step_2 )
			{	// Force step 1
				this.debug('We wanted to skip a few steps: ', options, image);
				options.step = 1;
			}
			
			// What do we need to do
			switch ( options.step )
			{
				// ---------------------------------
				// We need to preload
				case 1:
				
					// Disable keyboard nav
					this.KeyboardNav_Disable();
					
					// Show the loading image
					$('#lightbox-loading').show();
					
					// Hide things
					$('#lightbox-image,#lightbox-nav,#lightbox-nav-btnPrev,#lightbox-nav-btnNext,#lightbox-infoBox').hide();
					
					// Remove show info events
					$('#lightbox-imageBox').unbind();
					
					// Preload the image
					var preloader = new Image();
					preloader.onload = function() {
						$.Lightbox.showImage(null, {step:2, width:preloader.width, height:preloader.height});
						preloader.onload = null;
						preloader = null;
					};
					preloader.src = image.src;
					
					// Make active image
					if ( !this.images.active(image) ) { return false; }
					
					// Done
					break;
				
				
				// ---------------------------------
				// Resize the container
				case 2:
					
					// Set image src
					$('#lightbox-image').attr('src', image.src);
					
					// Establish options
					options = $.extend({width:null, height:null}, options);
					
					// Set container border (Moved here for Konqueror fix - Credits to Blueyed)
					if ( this.padding === null || typeof this.padding === 'undefined' || this.padding === NaN )
					{	// Autodetect
						this.padding = parseInt($('#lightbox-imageContainer').css('padding-left'), 10) || parseInt($('#lightbox-imageContainer').css('padding'), 10) || 0;
					}
					
					// Resize image box
					// i:image, c:current, n:new, d:difference
					
					// Get image dimensions
					var iWidth  = options.width;
					var iHeight = options.height;
					
					// Get current width and height
					var cWidth = $('#lightbox-imageBox').width();
					var cHeight = $('#lightbox-imageBox').height();
			
					// Get the width and height of the selected image plus the padding
					var nWidth	= (iWidth  + (this.padding * 2)); // Plus the image´s width and the left and right padding value
					var nHeight	= (iHeight + (this.padding * 2)); // Plus the image´s height and the left and right padding value
					
					// Diferences
					var dWidth  = cWidth  - nWidth;
					var dHeight = cHeight - nHeight;
					
					// Lets do this here because we can - NO because we know the heights here
					$('#lightbox-nav-btnPrev,#lightbox-nav-btnNext').css({ height: iHeight + (this.padding * 2) }); 
					$('#lightbox-infoBox').css({ width: iWidth+this.padding*2 });
					
					// Do we need to wait?
					if ( dWidth === 0 && dHeight === 0 )
					{	// We are the same size
						if ( $.browser.msie ) {
							this.pause(250);
						} else {
							this.pause(100);	
						}
						$.Lightbox.showImage(null, {step:3});
					}
					else
					{	// We are not the same size
						// Animate
						$('#lightbox-imageBox').animate({ width: nWidth, height: nHeight }, this.speed, function ( ) { $.Lightbox.showImage(null, {step:3}); } );
						// Reposition the Boxes
						this.repositionBoxes({'nHeight':nHeight});
					}
					
					// Done
					break;
				
				
				// ---------------------------------
				// Display the image
				case 3:
					
					// Hide loading
					$('#lightbox-loading').hide();
					
					// Animate image
					$('#lightbox-image').fadeIn('normal', function() {$.Lightbox.showImage(null, {step:4}); });
					
					// Start the proloading of other images
					this.preloadNeighbours();
					
					// Done
					break;
				
				
				// ---------------------------------
				// Set image info / Set navigation
				case 4:
					
					// ---------------------------------
					// Set image info
					
					// Hide and set image info
					$('#lightbox-caption-title').html(image.title + (image.description ? ': ' : '') || 'Untitled');
					$('#lightbox-caption-description').html(image.description || '&nbsp;');
					
					// If we have a set, display image position
					if ( this.images.size() > 1 )
					{	// Display
						$('#lightbox-currentNumber').html(this.text.image + '&nbsp;' + ( image.index + 1 ) + '&nbsp;' + this.text.of + '&nbsp;' + this.images.size());
					} else
					{	// Empty
						$('#lightbox-currentNumber').html('&nbsp;');
					}
					
					// Apply event
					$('#lightbox-imageBox').unbind('mouseover').mouseover(function(){
						 $('#lightbox-infoBox').slideDown('fast');
					 });
					
					// Apply event
					$('#lightbox-infoBox').unbind('mouseover').mouseover(function(){
						 $('#lightbox-infoFooter').slideDown('fast');
					 });
					
					// ---------------------------------
					// Set navigation
		
					// Instead to define this configuration in CSS file, we define here. And it´s need to IE. Just.
					$('#lightbox-nav-btnPrev, #lightbox-nav-btnNext').css({ 'background' : 'transparent url(' + this.files.images.blank + ') no-repeat' });
					
					// If not first, show previous button
					if ( !this.images.first(image) ) {
						// Not first, show button
						$('#lightbox-nav-btnPrev').show();
					}
					
					// If not last, show next button
					if ( !this.images.last(image) ) {
						// Not first, show button
						$('#lightbox-nav-btnNext').show();
					}
					
					// Make navigation active / show it
					$('#lightbox-nav').show();
					
					// Enable keyboard navigation
					this.KeyboardNav_Enable();
					
					// Done
					break;
					
					
				// ---------------------------------
				// Error handling
				default:
					this.debug('Don\'t know what to do: ',options);
					return this.showImage(image, {step:1});
					// break;
				
			}
			
			// All done
			return true;
		},
		
		preloadNeighbours: function ( )
		{	// Preload all neighbour images
			
			// Do we need to do this?
			if ( this.images.single() || this.images.empty() )
			{	return true;	}
			
			// Get active image
			var image = this.images.active();
			if ( !image ) { return image; }
			
			// Load previous
			var prev = this.images.prev(image);
			var objNext;
			if ( prev ) {
				objNext = new Image();
				objNext.src = prev.src;
			}
			
			// Load next
			var next = this.images.next(image);
			if ( next ) {
				objNext = new Image();
				objNext.src = next.src;
			}
		},
		
		// --------------------------------------------------
		// Things we don't really care about
		
		debug: function ( options )
		{
			// Can we debug? - Do we have firebug
			var con = null;
			if ( typeof console !== 'undefined' && typeof console.log !== 'undefined' )
			{	con = console;	}
			else if ( typeof window.console !== 'undefined' && typeof window.console.log !== 'undefined')
			{	con = window.console;	}
			
			// Do the log
			if ( con )
			{	// Do we support arguments?
				if ( typeof arguments !== 'undefined' && arguments.length > 1)
				{	con.log(arguments);	return arguments;	}
				else
				{	con.log(options);	return options;		}
			}
		},
		
		KeyboardNav_Enable: function ( ) {
			$(document).keydown(function(objEvent) {
				$.Lightbox.KeyboardNav_Action(objEvent);
			});
		},
		
		KeyboardNav_Disable: function ( ) {
			$(document).unbind();
		},
		
		KeyboardNav_Action: function ( objEvent ) {
			// Get the keycode
			var keycode, escapeKey;
			if ( objEvent === null ) { // ie
				keycode = event.keyCode;
				escapeKey = 27;
			} else { // moz
				keycode = objEvent.keyCode;
				escapeKey = objEvent.DOM_VK_ESCAPE;
			}
			
			// Get key
			var key = String.fromCharCode(keycode).toLowerCase();
			
			// Close?
			if ( key === this.keys.close || keycode === escapeKey )
			{	return $.Lightbox.finish();		}
			
			// Prev?
			if ( key === this.keys.prev || keycode === 37 )
			{	// We want previous
				return $.Lightbox.showImage($.Lightbox.images.prev());
			}
			
			// Next?
			if ( key === this.keys.next || keycode === 39 )
			{	// We want next
				return $.Lightbox.showImage($.Lightbox.images.next());
			}
			
			// Unknown
			return true;
		},
		
		getPageSize: function ( )
		{
			var xScroll, yScroll;
	
			if (window.innerHeight && window.scrollMaxY) {	
				xScroll = window.innerWidth + window.scrollMaxX;
				yScroll = window.innerHeight + window.scrollMaxY;
			} else if (document.body.scrollHeight > document.body.offsetHeight){ // all but Explorer Mac
				xScroll = document.body.scrollWidth;
				yScroll = document.body.scrollHeight;
			} else { // Explorer Mac...would also work in Explorer 6 Strict, Mozilla and Safari
				xScroll = document.body.offsetWidth;
				yScroll = document.body.offsetHeight;
			}
	
			var windowWidth, windowHeight;
	
			if (self.innerHeight) {	// all except Explorer
				if(document.documentElement.clientWidth){
					windowWidth = document.documentElement.clientWidth; 
				} else {
					windowWidth = self.innerWidth;
				}
				windowHeight = self.innerHeight;
			} else if (document.documentElement && document.documentElement.clientHeight) { // Explorer 6 Strict Mode
				windowWidth = document.documentElement.clientWidth;
				windowHeight = document.documentElement.clientHeight;
			} else if (document.body) { // other Explorers
				windowWidth = document.body.clientWidth;
				windowHeight = document.body.clientHeight;
			}	
	
			// for small pages with total height less then height of the viewport
			if(yScroll < windowHeight){
				pageHeight = windowHeight;
			} else { 
				pageHeight = yScroll;
			}
	
	
			// for small pages with total width less then width of the viewport
			if(xScroll < windowWidth){	
				pageWidth = xScroll;		
			} else {
				pageWidth = windowWidth;
			}
			
			//
			var largestWidth;
			var largestHeight;
			var smallestWidth;
			var smallestHeight;
			//
			if ( pageWidth >= windowWidth )
			{	largestWidth = pageWidth; smallestWidth = windowWidth;	}
			else
			{	largestWidth = windowWidth; smallestWidth = pageWidth;	}
			//
			if ( pageHeight >= windowHeight )
			{	largestHeight = pageHeight; smallestHeight = windowHeight;	}
			else
			{	largestHeight = windowHeight; smallestHeight = pageHeight;	}
			
			/*
			//code for a future replacement of all the above rubbish
			//var arrayPageSize = {'pageWidth':w,'pageHeight':h,'windowWidth':w,'windowHeight':h};
			$.Lightbox.debug($(document.body).width());
			$.Lightbox.debug($(document.body).height());
			$.Lightbox.debug('--');
			$.Lightbox.debug($(window).width());
			$.Lightbox.debug($(window).height());
			$.Lightbox.debug('--');
			$.Lightbox.debug(pageWidth);
			$.Lightbox.debug(pageHeight);
			$.Lightbox.debug('--');
			$.Lightbox.debug(windowWidth);
			$.Lightbox.debug(windowHeight);
			$.Lightbox.debug('--');
			$.Lightbox.debug(largestWidth);
			$.Lightbox.debug(largestHeight);
			$.Lightbox.debug('--');
			$.Lightbox.debug(xScroll);
			$.Lightbox.debug(yScroll);
			$.Lightbox.debug('--');
			$.Lightbox.debug(largestWidth-smallestWidth);
			$.Lightbox.debug(largestHeight-smallestHeight);
			$.Lightbox.debug('===');
			//return arrayPageSize;
			*/
			
			// Return
			var arrayPageSize = {'pageWidth':pageWidth,'pageHeight':pageHeight,'windowWidth':windowWidth,'windowHeight':windowHeight,'largestWidth':largestWidth,'largestHeight':largestHeight};
			return arrayPageSize;
		},
		
		getPageScroll: function ( ) {
			var xScroll, yScroll;
			if (self.pageYOffset) {
				yScroll = self.pageYOffset;
				xScroll = self.pageXOffset;
			} else if (document.documentElement && document.documentElement.scrollTop) {	 // Explorer 6 Strict
				yScroll = document.documentElement.scrollTop;
				xScroll = document.documentElement.scrollLeft;
			} else if (document.body) {// all other Explorers
				yScroll = document.body.scrollTop;
				xScroll = document.body.scrollLeft;	
			}
			var arrayPageScroll = {'xScroll':xScroll,'yScroll':yScroll};
			return arrayPageScroll;
		},
		
		
		pause: function ( ms ) {
			var date = new Date();
			var curDate = null;
			do { curDate = new Date(); }
			while ( curDate - date < ms);
		}
	
	}); // We have finished extending/defining our LightboxClass


	// --------------------------------------------------
	// Finish up
	
	// On document load, Instantiate our class
	$(function() {
		// Instantiate
		$.Lightbox = $.Lightbox || new $.LightboxClass();
		
		// domReady
		$.Lightbox.domReady();
	});
	

// Finished definition

})(jQuery); // We are done with our plugin, so lets call it with jQuery as the argument
