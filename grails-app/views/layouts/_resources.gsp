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
<link rel="stylesheet" href="${resource(dir: 'tds/web-app/css', file: 'tds-style.css')}">
<g:if test="${isLicenseManagerEnabled}">
    <link rel="stylesheet" href="${resource(dir: 'tds/web-app/css', file: 'managerStyle.css')}">
</g:if>
<!-- Blue Skin -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/skins', file: 'skin-blue.min.css')}">

<!-- jQuery -->
<!-- LEGACY AND SUPPORT LEGACY CODE START -->
<%-- What is this for?  --%>
<script type="text/javascript">
	var contextPath = "${request.contextPath}";
</script>

<link rel="stylesheet" type="text/css" href="${resource(dir: 'tds/web-app/css/legacy/', file: 'tds-main.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'tds/web-app/css/legacy/', file: 'tds.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'tds/web-app/css/legacy/', file: 'codemirror.css')}"/>

<asset:javascript src="angular-support" />

<script rel="prefetch" src="${resource(dir: 'js/tinymce', file: 'tinymce.min.js')}"></script>
<script rel="prefetch" src="${resource(dir: 'js/tinymce', file: 'theme.min.js')}"></script>

<!-- Boosttrap -->
<script src="${resource(dir: 'dist/js/vendors/bootstrap/dist/js', file: 'bootstrap.min.js')}"></script>
<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>
