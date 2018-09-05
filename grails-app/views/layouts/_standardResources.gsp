<%-- 
  -- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<tds:favicon />

<!-- LEGACY CODE START -->
<asset:stylesheet src="resources" />


<g:javascript src="prototype/prototype.js" />
<asset:javascript src="resources" />
%{--<g:javascript src="jquery-1.9.1.js"/>--}%
%{--<g:javascript src="jquery-1.9.1-ui.js"/>--}%
%{--<g:javascript src="datetimepicker.js"/>--}%
%{--<g:javascript src="jquery-migrate-1.0.0.js"/>--}%

<%
def moveEvent = tds.currentMoveEvent()
/*Date date = new Date()
def showCrawler = false
if(moveEvent && moveEvent.estStartTime && moveEvent.estCompletionTime){
	if((moveEvent.newsBarMode != 'off') && (moveEvent.estStartTime <= date) && (moveEvent.estCompletionTime >= date)){
		showCrawler = true
	}	
}
*/
%>

<g:if test="${moveEvent?.newsBarMode == 'on' || (moveEvent?.newsBarMode == 'auto' && moveEvent?.estStartTime)}">
	<g:javascript src="crawler.js" />
</g:if>
%{--<g:javascript src="select2.js"/>--}%
%{--<g:javascript src="jquery.combox.js"/>	--}%
%{--<g:javascript src="moment.min.js" />--}%
%{--<g:javascript src="moment-timezone-with-data.min.js" />--}%
%{--<g:javascript src="daterangepicker.js" />--}%
%{--<g:javascript src="lodash/lodash.min.js" />--}%
%{--<g:javascript src="tds-common.js" />--}%
%{--<g:javascript src="timezone/jquery.maphilight.min.js" />--}%
%{--<g:javascript src="timezone/jquery.timezone-picker.min.js" />--}%

<script type="text/javascript">
	var currentURL='';
	( function($) {
		currentURL = window.location.pathname;
	})(jQuery);

	// TODO : JPM 10/2014 : Need to refactor this javascript functions to not be global
	var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
	var currentMenuId = "";
	var taskManagerTimePref = "60"
	var contextPath = "${request.contextPath}"
	var isIE7OrLesser  = jQuery.browser.msie && parseInt(jQuery.browser.version) < 8 ? true : false  
</script>
