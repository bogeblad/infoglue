/**
 * @license Copyright (c) 2003-2013, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

/**
 * @fileOverview Image plugin based on Widgets API
 */

'use strict';

CKEDITOR.dialog.add( 'figure', function( editor ) {

  	var imageStyles = [ [ '' ], [ 'Fullbredd', 'imagefullwidth' ], [ 'Halvbredd vänster', 'imagelefthalf' ], [ 'Halvbredd höger', 'imagerighthalf' ], [ 'Liten bild vänster', 'imageleftsmall' ], [ 'Liten bild höger', 'imagerightsmall' ] ];
  	console.log(editor.lang);
  	if( editor.lang == 'sv' )
   	 	var imageButtonLabel = "Bildstil";
  	else
    	var imageButtonLabel = "Image style";
  	
	// RegExp: 123, 123px, empty string ""
	var regexGetSizeOrEmpty = /(^\s*(\d+)(px)?\s*$)|^$/i,

		lockButtonId = CKEDITOR.tools.getNextId(),
		resetButtonId = CKEDITOR.tools.getNextId(),

		lang = editor.lang.figure,
		commonLang = editor.lang.common,

		lockResetStyle = 'margin-top:18px;width:40px;height:20px;',
		lockResetHtml = new CKEDITOR.template(
			'<div>' +
				'<a href="javascript:void(0)" tabindex="-1" title="' + lang.lockRatio + '" class="cke_btn_locked" id="{lockButtonId}" role="checkbox">' +
					'<span class="cke_icon"></span>' +
					'<span class="cke_label">' + lang.lockRatio + '</span>' +
				'</a>' +

				'<a href="javascript:void(0)" tabindex="-1" title="' + lang.resetSize + '" class="cke_btn_reset" id="{resetButtonId}" role="button">' +
					'<span class="cke_label">' + lang.resetSize + '</span>' +
				'</a>' +
			'</div>' ).output( {
				lockButtonId: lockButtonId,
				resetButtonId: resetButtonId
			} ),

		helpers = CKEDITOR.plugins.figure,

		// Functions inherited from figure plugin.
		checkHasNaturalRatio = helpers.checkHasNaturalRatio,
		getNatural = helpers.getNatural,

		// Global variables referring to the dialog's context.
		doc, widget, image,

		// Global variable referring to this dialog's image pre-loader.
		preLoader,

		// Global variables holding the original size of the image.
		domWidth, domHeight,

		// Global variables related to image pre-loading.
		preLoadedWidth, preLoadedHeight, srcChanged,

		// Global variables related to size locking.
		lockRatio, userDefinedLock,

		// Global variables referring to dialog fields and elements.
		lockButton, resetButton, widthField, heightField,

		// Global variables related to figure class.
		figureClass,

		natural;

	// Validates dimension. Allowed values are:
	// "123px", "123", "" (empty string)
	function validateDimension() {
		var match = this.getValue().match( regexGetSizeOrEmpty ),
			isValid = !!( match && parseInt( match[ 1 ], 10 ) !== 0 );

		if ( !isValid )
			alert( commonLang[ 'invalid' + CKEDITOR.tools.capitalize( this.id ) ] );

		return isValid;
	}

	// Creates a function that pre-loads images. The callback function passes
	// [image, width, height] or null if loading failed.
	//
	// @returns {Function}
	function createPreLoader() {
		var image = doc.createElement( 'img' ),
			listeners = [];

		function addListener( event, callback ) {
			listeners.push( image.once( event, function( evt ) {
				removeListeners();
				callback( evt );
			} ) );
		}

		function removeListeners() {
			var l;

			while ( ( l = listeners.pop() ) )
				l.removeListener();
		}

		// @param {String} src.
		// @param {Function} callback.
		return function( src, callback, scope ) {
			addListener( 'load', function() {
				callback.call( scope, image, image.$.width, image.$.height );
			} );

			addListener( 'error', function() {
				callback( null );
			} );

			addListener( 'abort', function() {
				callback( null );
			} );

			image.setAttribute( 'src', src + '?' + Math.random().toString( 16 ).substring( 2 ) );
		};
	}

	// This function updates width and height fields once the
	// "src" field is altered. Along with dimensions, also the
	// dimensions lock is adjusted.
	function onChangeSrc() {
		var value = this.getValue();

		// Remember that src is different than default.
		if ( value !== widget.data.src ) {
			// Update dimensions of the image once it's preloaded.
			preLoader( value, function( image, width, height ) {
				
				// There was problem loading the image. Unlock ratio.
				if ( !image )
					return toggleLockRatio( false );

				// Fill width field with the width of the new image.
				widthField.setValue( width );

				// Fill height field with the height of the new image.
				heightField.setValue( height );

				// Cache the new width.
				preLoadedWidth = width;

				// Cache the new height.
				preLoadedHeight = height;

			} );

			srcChanged = true;
		}

		// Value is the same as in widget data but is was
		// modified back in time. Roll back dimensions when restoring
		// default src.
		else if ( srcChanged ) {
			// Src equals default one back again.
			srcChanged = false;
		}
	}


	var ret = {
		title: lang.title,
		minWidth: 250,
		minHeight: 100,
		onLoad: function() {
			// Create a "global" reference to the document for this dialog instance.
			doc = this._.element.getDocument();

			// Create a pre-loader used for determining dimensions of new images.
			preLoader = createPreLoader();
		},
		onShow: function() {
			// Create a "global" reference to edited widget.
			widget = this.widget;

			// Create a "global" reference to widget's image.
			image = widget.parts.image;

			// Reset global variables.
			srcChanged = userDefinedLock = lockRatio = false;

			// Natural dimensions of the image.
			natural = getNatural( image );

			// Get the natural width of the image.
			preLoadedWidth = domWidth = natural.width;

			// Get the natural height of the image.
			preLoadedHeight = domHeight = natural.height;
		},
		contents: [
			{
				id: 'info',
				label: lang.infoTab,
				elements: [
					{
						type: 'vbox',
						padding: 0,
						children: [
							{
								type: 'hbox',
								widths: [ '280px', '110px' ],
								align: 'right',
								children: [
									{
										id: 'src',
										type: 'text',
										label: commonLang.url,
										onKeyup: onChangeSrc,
										onChange: onChangeSrc,
										setup: function( widget ) {
											this.setValue( widget.data.src );
										},
										commit: function( widget ) {
											widget.setData( 'src', this.getValue() );
										},
										validate: CKEDITOR.dialog.validate.notEmpty( lang.urlMissing )
									},
									{
										// Remark: button may be removed at the very bottom of
										// the file, if browser config is not set.
										type: 'button',
										id: 'browse',
										// v-align with the 'txtUrl' field.
										// TODO: We need something better than a fixed size here.
										style: 'display:inline-block;margin-top:16px;',
										align: 'center',
										label: editor.lang.common.browseServer,
										hidden: true,
										filebrowser: 'info:src'
									}
								]
							}
						]
					},
					{
						id: 'alt',
						type: 'text',
						label: lang.alt,
						setup: function( widget ) {
							this.setValue( widget.data.alt );
						},
						commit: function( widget ) {
							widget.setData( 'alt', this.getValue() );
						}
					},
					{
						id: 'hasCaption',
						type: 'checkbox',
						label: lang.captioned,
						setup: function( widget ) {
							this.setValue( widget.data.hasCaption );
						},
						commit: function( widget ) {
							widget.setData( 'hasCaption', this.getValue() );
						}
					},
					{
			  			type: "hbox",
			  			children: [{
							id: "figureClass",
							type: "select",
							label: imageButtonLabel,
							items: imageStyles,
							setup: function( widget ) {
							console.log("widget", widget);
								this.setValue( widget.data.figureClass );
							},
							commit: function( widget ) {
								widget.setData( 'figureClass', this.getValue() );
							}
			  			}]
					}
				]
			},
			{
				id: 'Upload',
				hidden: true,
				filebrowser: 'uploadButton',
				label: lang.uploadTab,
				elements: [
					{
						type: 'file',
						id: 'upload',
						label: lang.btnUpload,
						style: 'height:40px',
						size: 38
					},
					{
						type: 'fileButton',
						id: 'uploadButton',
						filebrowser: 'info:src',
						label: lang.btnUpload,
						'for': [ 'Upload', 'upload' ]
					}
				]
			}
		]
	};
	console.log(editor.config.filebrowserImageBrowseUrl);
	if ( !editor.config.filebrowserFigureBrowseUrl && !editor.config.filebrowserBrowseUrl ) {
		// Replaces hbox (which should contain button#browse but is hidden) with text control.
		ret.contents[ 0 ].elements[ 0 ].children[ 0 ] = ret.contents[ 0 ].elements[ 0 ].children[ 0 ].children[ 0 ];
	}

	return ret;
} );
