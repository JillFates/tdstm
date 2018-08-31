<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<title>TransitionManager&trade; - Not Found (404)</title>
	<!-- HTTP_CODE=404 -->

	<!-- Tell the browser to be responsive to screen width -->
	<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">


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
	<g:if test="${isLicenseManagerEnabled}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'managerStyle.css')}">
	</g:if>
	<g:else>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'tds-style.css')}">
	</g:else>

	<!-- Blue Skin -->
	<link rel="stylesheet" href="${resource(dir: 'dist/css/skins', file: 'skin-blue.min.css')}">

	<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!--[if lt IE 9]>
	<script src="${resource(dir: 'dist/js/vendors/html5shiv/dist', file: 'html5shiv.min.js')}"></script>
	<script src="${resource(dir: 'dist/js/vendors/respond/dest', file: 'respond.min.js')}"></script>
	<![endif]-->

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
						<img id="logo-header" src="${resource(dir:'images',file:'TMHeaderLogoManager.png')}" alt="Transition Manager project" border="0" />
					</g:if>
					<g:else>
						<img id="logo-header" src="${resource(dir:'images',file:'TMHeaderLogo.png')}" alt="Transition Manager project" border="0" />
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
				<h2 class="headline text-red"><i class="fa fa-fw fa-chain-broken"></i></h2>
				<div class="error-content">
					<h3><i class="fa fa-warning text-red"></i> Oops! Nothing Found.</h3>
					<p>
						We cannot find what you are looking for.  Perhaps the page is broken, or has been moved.  Please contact the Project Manager to help you resolve this.  Meanwhile, <a href="${continueUrl}">click here</a> to return.
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
<!-- SlimScroll -->
<script src="${resource(dir: 'dist/js/vendors/jquery-slimscroll', file: 'jquery.slimscroll.min.js')}"></script>
<!-- FastClick -->
<script src="${resource(dir: 'dist/js/vendors/fastclick/lib', file: 'fastclick.js')}"></script>
<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>
</body>
</html>
