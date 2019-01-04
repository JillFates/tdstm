/*
 * Author : Lokanada Reddy
 * Release: 06/03/2009
 */ 
(function($) {	
	/**
	 * Used to display the progress of a process
	 * @param val - the current increment of the progress
	 * @param maxVal - the highest increment to process
	 * @param initializeBar - used to initial what is in the bar, if true clears out the bar text or if String will display the text
	 */
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

				var text = '';				
				if (typeof initializeBar == 'string') {
					text = initializeBar;
				} else if (initializeBar===true) {
					//
				} else if (val) {
					text = val + " of " + max;
				}
				div.find(".text").html(text)

			}
		);
	};
})(jQuery);