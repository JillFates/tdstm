<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<title>TransitionManager&trade; - Unauthorized (401)</title>
	<!-- HTTP_CODE=401 -->

	<!-- Tell the browser to be responsive to screen width -->
	<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

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

	<!-- jQuery -->
	<script src="${resource(dir: 'dist/js/vendors/jquery/dist', file: 'jquery.min.js')}"></script>

</head>
<body class="hold-transition skin-blue layout-top-nav error-handling">
<div class="wrapper">

	<header class="main-header">
		<nav class="navbar navbar-static-top">
			<div class="container menu-top-container ${((!isLicenseManagerEnabled)? 'menu-top-container-full-menu' : '')}">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse">
						<i class="fa fa-bars"></i>
					</button>
					<g:if test="${isLicenseManagerEnabled}">
						<asset:image id="logo-header" src="images/TMHeaderLogoManager.png" alt="TransitionManager" border="0" />
					</g:if>
					<g:else>
						<asset:image id="logo-header" src="images/TMHeaderLogo.png" alt="TransitionManager" border="0" />
					</g:else>
				</div>
			</div><!-- /.container-fluid -->
		</nav>
	</header>
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
					<h3><i class="fa fa-warning text-red"></i> Unauthorized Access.</h3>
					<p>
						Please contact the Project Manager to help you resolve this.  Meanwhile, <a href="${continueUrl}">click here</a> to return.
					</p>
					<hr>
				</div>
			</div><!-- /.error-page -->

		</section><!-- /.content -->
	</div><!-- /.content-wrapper -->
	<footer class="main-footer">
		<div class="container">
			<div class="pull-right hidden-xs">
				<b></b>
			</div>
			<strong><a href="http://www.transitionaldata.com/service/transitionmanager" target="_blank">&nbsp;TransitionManager&trade;</a> 2015-2016 .</strong> All
		rights reserved.
		</div>
		<!-- /.container -->
	</footer>

</div><!-- ./wrapper -->


<!-- Boosttrap -->
<script src="${resource(dir: 'dist/js/vendors/bootstrap/dist/js', file: 'bootstrap.min.js')}"></script>
<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>
</body>
</html>
