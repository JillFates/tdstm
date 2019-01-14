<%--
-- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<link rel="shortcut icon" type="image/x-icon" href="${resource(dir: 'images', file: 'favicon.ico')}"/>

<!-- Bootstrap 3.3.5 -->
<link rel="stylesheet" href="${resource(dir: 'dist/js/vendors/bootstrap/dist/css', file: 'bootstrap.min.css')}">
<!-- Font Awesome -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/fontawesome', file: 'font-awesome.min.css')}">
<!-- Ionicons -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/ionicons/2.0.1/css', file: 'ionicons.min.css')}">
<!-- Theme style -->
<link rel="stylesheet" href="${resource(dir: 'dist/css', file: 'TDSTMLayout.min.css')}">
<!-- General Template Style -->
<link rel="stylesheet" href="${resource(dir: 'tds/web-app/css', file: 'style.css')}">
<g:if test="${isLicenseManagerEnabled}">
    <link rel="stylesheet" href="${resource(dir: 'tds/web-app/css', file: 'managerStyle.css')}">
</g:if>
<!-- Blue Skin -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/skins', file: 'skin-blue.min.css')}">

<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
<script src="${resource(dir: 'dist/js/vendors/html5shiv/dist', file: 'html5shiv.min.js')}"></script>
<script src="${resource(dir: 'dist/js/vendors/respond/dest', file: 'respond.min.js')}"></script>
<![endif]-->

<!-- jQuery -->
<!-- LEGACY CODE START -->

<link rel="stylesheet" type="text/css" href="${resource(dir: 'tds/web-app/css/legacy/', file: 'main.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'tds/web-app/css/legacy/', file: 'tds.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'tds/web-app/css/legacy/', file: 'codemirror.css')}"/>

<link id="jquery-ui-theme" media="screen, projection" rel="stylesheet" type="text/css" href="${resource(dir: 'plugins/jquery-ui-1.10.4/jquery-ui/themes/ui-lightness', file: 'jquery-ui-1.10.4.custom.css')}"/>

<g:javascript src="jquery-1.9.1.js"/>
<g:javascript src="jquery-1.9.1-ui.js"/>
<%
    def moveEvent = tds.currentMoveEvent() ?: null
%>

<g:if test="${moveEvent?.newsBarMode == 'on' || (moveEvent?.newsBarMode == 'auto' && moveEvent?.estStartTime)}">
    <g:javascript src="crawler.js" />
</g:if>
<g:javascript src="moment.min.js" />
<g:javascript src="moment-timezone-with-data.min.js" />
<g:javascript src="daterangepicker.js" />
<g:javascript src="tds-common.js" />
<g:javascript src="timezone/jquery.maphilight.min.js" />
<g:javascript src="timezone/jquery.timezone-picker.min.js" />
<g:javascript src="person.js"/>
<script src="${resource(dir: 'dist/js/vendors/jquery.browser/dist', file: 'jquery.browser.min.js')}"></script>

<script rel="prefetch" src="${resource(dir: 'dist/js/vendors/tinymce', file: 'tinymce.min.js')}"></script>
<script rel="prefetch" src="${resource(dir: 'dist/js/vendors/tinymce', file: 'theme.min.js')}"></script>

<script type="text/javascript">
    var contextPath = "${request.contextPath}";
    var currentMenuId = '';
</script>

<!-- LEGACY CODE END -->

<!-- Boosttrap -->
<script src="${resource(dir: 'dist/js/vendors/bootstrap/dist/js', file: 'bootstrap.min.js')}"></script>
<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>