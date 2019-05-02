<%--
  -- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<asset:link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

<!-- LEGACY CODE START -->
<asset:stylesheet src="css/resources.css" />


<g:javascript src="prototype/prototype.js" />
<asset:javascript src="resources" />



<script type="text/javascript">
	var currentURL='';
	( function($) {
		currentURL = window.location.pathname;
	})(jQuery);

	// TODO : JPM 10/2014 : Need to refactor this javascript functions to not be global
	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
	var currentMenuId = "";
	var taskManagerTimePref = "60"
	var contextPath = "${request.contextPath}"
	var isIE7OrLesser  = jQuery.browser.msie && parseInt(jQuery.browser.version) < 8 ? true : false
</script>
