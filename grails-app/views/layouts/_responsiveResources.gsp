<%--
-- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<asset:link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

<!-- Bootstrap 3.3.5 -->
<link rel="stylesheet" href="${resource(dir: 'dist/js/vendors/bootstrap/dist/css', file: 'bootstrap.min.css')}">
<!-- Font Awesome -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/fontawesome', file: 'font-awesome.min.css')}">
<!-- Ionicons -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/ionicons/2.0.1/css', file: 'ionicons.min.css')}">
<!-- Theme style -->
<link rel="stylesheet" href="${resource(dir: 'dist/css', file: 'TDSTMLayout.min.css')}">
<!-- General Template Style -->
<g:if test="${isLicenseManagerEnabled}">
    <asset:stylesheet href="css/managerStyle.css" />
</g:if>
<g:else>
    <asset:stylesheet href="css/tds-style.css" />
</g:else>
<!-- Blue Skin -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/skins', file: 'skin-blue.min.css')}">

<!-- Kendo UI Material Theme -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/kendo', file: 'kendo.common-material.min.css')}">
<link rel="stylesheet" href="${resource(dir: 'dist/css/kendo', file: 'kendo.material.min.css')}">
<link rel="stylesheet" href="${resource(dir: 'dist/css/kendo', file: 'kendo.material.mobile.min.css')}">

<!-- jQuery -->
<script src="${resource(dir: 'dist/js/vendors/jquery/dist', file: 'jquery.min.js')}"></script>

<!-- LEGACY CODE START -->
<%
def moveEvent = tds.currentMoveEvent() ?: null
%>

<asset:stylesheet src="css/resources.css" />
<asset:javascript src="resources" />

<!-- Migration Scripts -->
<script src="${resource(dir: 'js/migrateScript', file: 'jqueryDeprecated.js')}"></script>

<!-- -->


<%-- What is this for?  --%>
<script type="text/javascript">
    var currentURL = '';
    (function ($) {
        currentURL = window.location.pathname;
    })(jQuery);

    // TODO : JPM 10/2014 : Need to refactor this javascript functions to not be global
    var dateRegExpForExp = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
    var currentMenuId = "";
    var taskManagerTimePref = "60"
    var contextPath = "${request.contextPath}"
    // TODO: This should be removed later, and it-s being created on migrateScript/jqueryDeprecated.js
    var isIE7OrLesser  = jQuery.browser.msie && parseInt(jQuery.browser.version) < 8 ? true : false
</script>

<!-- LEGACY CODE END -->

<!-- Kendo Directives -->
<script src="${resource(dir: 'dist/js/vendors/kendo', file: 'kendo.all.min.js')}"></script>
<!-- Boosttrap -->
<script src="${resource(dir: 'dist/js/vendors/bootstrap/dist/js', file: 'bootstrap.min.js')}"></script>
<!-- ProgressBar -->
<script src="${resource(dir: 'js/progressbar', file: 'progressbar.js')}"></script>

<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>
