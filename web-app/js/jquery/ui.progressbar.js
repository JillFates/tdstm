/*
 * Author : Lokanada Reddy
 * Release: 06/03/2009
 */ 
(function($) {	
	//Main Method
	$.fn.reportprogress = function(val, maxVal, initializeBar) {			
		var max;
		if (maxVal)
			max=maxVal;

		return this.each(
			function() {		
				var div=$(this);
				var innerdiv=div.find(".progress");
				
				if (innerdiv.length!=1) {						
					innerdiv=$("<div class='progress'></div>");					
					div.append("<div class='text'>&nbsp;</div>");
					$("<span class='text'>&nbsp;</span>").css("width",div.width()).appendTo(innerdiv);					
					div.append(innerdiv);					
				}
				var width=Math.round(val/max*100);
				innerdiv.css("width",width+"%");
				if (initializeBar===true) {
					div.find(".text").html('');
				} else if (val) {	
					div.find(".text").html(val + " of " + max);
				}
			}
		);
	};
})(jQuery);