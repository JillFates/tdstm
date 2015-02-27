<%-- 
  -- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<link rel="shortcut icon" type="image/x-icon" href="${resource(dir:'images',file:'favicon.ico')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'main.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'tds.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'ui.core.css')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'ui.dialog.css')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'ui.theme.css')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'ui.datetimepicker.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'jquery-ui-smoothness.css')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'combox.css')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'select2.css')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'combox.css')}" />

<%-- TODO : JPM 10/2014 : Determine why we have jquery ui 1.8.15 css while using 1.9.1-ui --%>
<link id="jquery-ui-theme" media="screen, projection" rel="stylesheet" type="text/css" 
	href="${resource(dir:'plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness',file:'jquery-ui-1.8.15.custom.css')}"/>

<g:javascript src="prototype/prototype.js" />
<g:javascript src="jquery-1.9.1.js"/>	
<g:javascript src="jquery-1.9.1-ui.js"/>
<g:javascript src="datetimepicker.js"/>
<g:javascript src="jquery-migrate-1.0.0.js"/>
<g:javascript src="crawler.js" />
<g:javascript src="select2.js"/>
<g:javascript src="jquery.combox.js"/>	
<g:javascript src="moment.min.js" />
<g:javascript src="daterangepicker.js" />
<g:javascript src="lodash/lodash.min.js" />
<g:javascript src="tds-common.js" />

<script type="text/javascript">
	var currentURL='';
	( function($) {
		currentURL = window.location.pathname;
	})(jQuery);

	// TODO : JPM 10/2014 : Need to refactor this javascript functions to not be global
	var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
	var currentMenuId = "";
	var B1 = []
	var B2 = []
	var taskManagerTimePref = "60"
	var contextPath = "${request.contextPath}"
	var isIE7OrLesser  = jQuery.browser.msie && parseInt(jQuery.browser.version) < 8 ? true : false  
</script>