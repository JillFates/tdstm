<g:set var="licenseCommonService" bean="licenseCommonService"/>
<g:set var="environmentService" bean="environmentService"/>
<!DOCTYPE html>
<html>
<%
    // Only for environments where the License Manager is true Enabled
    def isLicenseManagerEnabled = licenseCommonService.isManagerEnabled()
    def buildInfo = environmentService.getVersionText()
%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><g:layoutTitle default="Grails" /></title>

    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <g:render template="/layouts/resources" model="[isLicenseManagerEnabled:isLicenseManagerEnabled]"  />

    <g:layoutHead />

</head>

<body class="hold-transition skin-blue layout-top-nav">
    <div class="wrapper">
        <!-- / Injects the BODY -->
        <g:layoutBody />
    </div>
    <g:render template="/layouts/chromeAutofillBug" />
</body>
</html>
