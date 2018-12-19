
/**
 * Copyright: http://www.baihang-china.com/
 * Author: Dingwei 2013-07-24
 * Description: banger.bgIframe.js
 * Modified by: 
 * Modified contents: 
**/
(function($){
	
	$.fn.bgiframe = ($.browser.msie && /msie 6\.0/i.test(navigator.userAgent) ? function(s){
		
		s = $.extend({
			
			top: 'auto', // auto == .currentStyle.borderTopWidth
			
			left: 'auto', // auto == .currentStyle.borderLeftWidth
			
			width: 'auto', // auto == offsetWidth
			
			height: 'auto', // auto == offsetHeight
			
			opacity: true,
			
			src: 'javascript:false;'
			
		}, s);
		
		var html = '<iframe class="bgiframe" frameborder="0" tabindex="-1" src="' + s.src + '"' +
				   'style="display: block; position: absolute; z-index: -1;' +
				   (s.opacity !== false ? 'filter:Alpha(Opacity=\'0\');' : '') +
				   'top: ' + (s.top == 'auto' ? 'expression(((parseInt(this.parentNode.currentStyle.borderTopWidth)||0)*-1)+\'px\')' : prop(s.top)) + ';' +
				   'left: ' + (s.left == 'auto' ? 'expression(((parseInt(this.parentNode.currentStyle.borderLeftWidth)||0)*-1)+\'px\')' : prop(s.left)) + ';' +
				   'width: ' + (s.width == 'auto' ? 'expression(this.parentNode.offsetWidth+\'px\')' : prop(s.width)) + ';' +
				   'height: ' + (s.height == 'auto' ? 'expression(this.parentNode.offsetHeight-5+\'px\')' : prop(s.height)) + ';' + '" />';
				   
		return this.each(function(){
			
			if ($(this).children('iframe.bgiframe').length === 0){
				
				this.insertBefore(document.createElement(html), this.firstChild);
				
			}
			
		});
		
	} : function(){ return this; });
	
	$.fn.bgIframe = $.fn.bgiframe;
	
	function prop(n){
		
		return n && n.constructor === Number ? n + 'px' : n;
		
	}
	
})(jQuery);
