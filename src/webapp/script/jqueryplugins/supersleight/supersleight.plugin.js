jQuery.fn.supersleight = function(settings) {
	settings = jQuery.extend({
		imgs: true,
		backgrounds: true,
		shim: 'x.gif',
		apply_positioning: true
	}, settings);
	
	return this.each(function(){
		if (jQuery.browser.msie && parseInt(jQuery.browser.version) < 7 && parseInt(jQuery.browser.version) > 4) {
			jQuery(this).find('*').each(function(i,obj) {
				var self = jQuery(obj);
				// background pngs
				if (settings.backgrounds && self.css('background-image').match(/\.png/i) !== null) {
					var bg = self.css('background-image');
					var src = bg.substring(5,bg.length-2);
					var mode = (self.css('background-repeat') == 'no-repeat' ? 'crop' : 'scale');
					var styles = {
						'filter': "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + src + "', sizingMethod='" + mode + "')",
						'background-image': 'url('+settings.shim+')'
					};
					self.css(styles);
				};
				// image elements
				if (settings.imgs && self.is('img[src$=png]')){
					var styles = {
						'width': self.width() + 'px',
						'height': self.height() + 'px',
						'filter': "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + self.attr('src') + "', sizingMethod='scale')"
					};
					self.css(styles).attr('src', settings.shim);
				};
				// apply position to 'active' elements
				if (settings.applyPositioning && self.is('a, input') && self.css('position') === ''){
					self.css('position', 'relative');
				};
			});
		};
	});
};