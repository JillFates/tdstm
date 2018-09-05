<%--
-- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<link rel="shortcut icon" type="image/x-icon" href="${assetPath(src: 'images/favicon.ico')}"/>

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
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'managerStyle.css')}">
</g:if>
<g:else>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'tds-style.css')}">
</g:else>


<!-- Blue Skin -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/skins', file: 'skin-blue.min.css')}">

<!-- Kendo UI Material Theme -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/kendo', file: 'kendo.common-material.min.css')}">
<link rel="stylesheet" href="${resource(dir: 'dist/css/kendo', file: 'kendo.material.min.css')}">
<link rel="stylesheet" href="${resource(dir: 'dist/css/kendo', file: 'kendo.material.mobile.min.css')}">

<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
<script src="${resource(dir: 'dist/js/vendors/html5shiv/dist', file: 'html5shiv.min.js')}"></script>
<script src="${resource(dir: 'dist/js/vendors/respond/dest', file: 'respond.min.js')}"></script>
<![endif]-->

<!-- jQuery -->
<script src="${resource(dir: 'dist/js/vendors/jquery/dist', file: 'jquery.min.js')}"></script>


<!-- LEGACY CODE START -->
<asset:stylesheet src="resources" />


<g:javascript src="prototype/prototype.js" />
<asset:javascript src="resources" />


<!-- Migration Scripts -->
<script src="${resource(dir: 'dist/js/migrateScript', file: 'jqueryDeprecated.js')}"></script>

<!-- -->

<%
def moveEvent = tds.currentMoveEvent() ?: null
%>

        <g:if test="${moveEvent?.newsBarMode == 'on' || (moveEvent?.newsBarMode == 'auto' && moveEvent?.estStartTime)}">
            <g:javascript src="crawler.js" />
        </g:if>

        <% // What is this for?  %>
        <script type="text/javascript">
            var currentURL = '';
            (function ($) {
                currentURL = window.location.pathname;
            })(jQuery);

            // TODO : JPM 10/2014 : Need to refactor this javascript functions to not be global
            var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
            var dateRegExpForExp = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
            var currentMenuId = "";
            var taskManagerTimePref = "60"
            var contextPath = "${request.contextPath}"
            // TODO: This should be removed later, and it-s being created on migrateScript/jqueryDeprecated.js
            var isIE7OrLesser  = jQuery.browser.msie && parseInt(jQuery.browser.version) < 8 ? true : false;
            var isMozillaBrowser = jQuery.browser.mozilla;
        </script>

<!-- LEGACY CODE END -->


<!-- Kendo Directives -->
<script src="${resource(dir: 'dist/js/vendors/kendo', file: 'kendo.all.min.js')}"></script>
<!-- Boosttrap -->
<script src="${resource(dir: 'dist/js/vendors/bootstrap/dist/js', file: 'bootstrap.min.js')}"></script>
<!-- SlimScroll -->
<script src="${resource(dir: 'dist/js/vendors/jquery-slimscroll', file: 'jquery.slimscroll.min.js')}"></script>
<!-- FastClick -->
<script src="${resource(dir: 'dist/js/vendors/fastclick/lib', file: 'fastclick.js')}"></script>
<!-- ProgressBar.js -->
<script src="${resource(dir: 'dist/js/vendors/progressbar.js/dist', file: 'progressbar.js')}"></script>

<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>


