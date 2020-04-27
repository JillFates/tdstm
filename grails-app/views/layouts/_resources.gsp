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

<link rel="stylesheet" type="text/css" href="${resource(dir: 'tds/web-app/css/legacy/', file: 'codemirror.css')}"/>

<asset:javascript src="angular-support" />

<!-- Boosttrap -->
<script src="${resource(dir: 'dist/js/vendors/bootstrap/dist/js', file: 'bootstrap.min.js')}"></script>

<!-- Clarity -->
<script src="${resource(dir: 'dist/js/vendors/clarity', file: 'clr-icons.min.js')}"></script>
<script src="${resource(dir: 'tds/web-app/dist', file: 'custom-elements.min.js')}"></script>

<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>
