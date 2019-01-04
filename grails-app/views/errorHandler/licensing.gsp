<g:set var="topNavClean" value="true" scope="request"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="layout" content="topNav"/>
    <title>TransitionManager&trade; - Licensing Error (500)</title>
    <!-- HTTP_CODE=500 -->

</head>
<body class="hold-transition skin-blue layout-top-nav error-handling">
<div class="wrapper">
    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
        </section>

        <!-- Main content -->
        <section class="content">

            <div class="error-page">
                <h2 class="headline text-red"><span class="glyphicon glyphicon-ban-circle"></span></h2>
                <div class="error-content">
                    <h3><i class="fa fa-warning text-red"></i> ${raw(licenseStateMap.message)}</h3>
                    <p>
                        Please contact the Project Manager to help you resolve this.  Meanwhile, <a href="${continueUrl}">click here</a> to return.
                    </p>
                    <div class="callout callout-danger">
                        <h4>Why am I seeing this?</h4>
                        <p>Your TransitionManager license is missing, expired or is otherwise no longer valid for this project. Your Administrator can manage the license from the Admin Menu, or if you are the Administrator and need assistance, please open a ticket at: <a href="https://support.transitionmanager.com" style="color: white; font-weight: bold;">https://support.transitionmanager.com</a></p>
                    </div>
                </div>
            </div><!-- /.error-page -->

        </section><!-- /.content -->
    </div><!-- /.content-wrapper -->
</div><!-- ./wrapper -->

</body>
</html>
