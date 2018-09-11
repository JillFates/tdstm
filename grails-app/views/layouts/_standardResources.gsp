<%-- 
  -- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<link rel="shortcut icon" type="image/x-icon" href="${assetPath(src: 'images/favicon.ico')}"/>

<!-- LEGACY CODE START -->
<asset:stylesheet src="resources" />


<g:javascript src="prototype/prototype.js" />
<asset:javascript src="resources" />

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
