<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<g:if test="${success}">
		<title>Forgot Password - Step 2</title>
	</g:if>
	<g:else>
		<title>Forgot Password</title>
	</g:else>
<!-- Tell the browser to be responsive to screen width -->
	<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
	<!-- Bootstrap 3.3.5 -->
	<link rel="stylesheet" href="${resource(dir:'dist/js/vendors/bootstrap/dist/css',file:'bootstrap.min.css')}">
	<!-- Font Awesome -->
	<link rel="stylesheet" href="${resource(dir:'dist/css/fontawesome',file:'font-awesome.min.css')}">
	<!-- Ionicons -->
	<link rel="stylesheet" href="${resource(dir:'dist/css/ionicons/2.0.1/css',file:'ionicons.min.css')}">
	<!-- Theme style -->
	<link rel="stylesheet" href="${resource(dir:'dist/css',file:'TDSTMLayout.min.css')}">
	<!-- General Template Style -->
	<link rel="stylesheet" href="${resource(dir:'css',file:'tds-style.css')}">

	<link rel="shortcut icon" type="image/x-icon" href="${assetPath(src: 'images/favicon.ico')}"/>

	<script src="${resource(dir:'dist/js/vendors/jquery/dist',file:'jquery.min.js')}"></script>

	<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
	<!--[if lt IE 9]>
	<script src="${resource(dir:'dist/js/vendors/html5shiv/dist',file:'html5shiv.min.js')}"></script>
	<script src="${resource(dir:'dist/js/vendors/respond/dest',file:'respond.min.js')}"></script>
	<![endif]-->

	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'spinner.css')}" />

</head>
<body class="hold-transition login-page">
<div class="login-box">

	<div class="login-box-body">
		<div class="login-logo">
			<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
				<img src="${resource(dir:'images',file:'TMLoginLogo.gif')}" border="0" alt="Learn more about TransitionManager" />
			</a>
		</div>

		<g:if test="${success}">
			<h2 class="emailStatus">Check Your Email</h2>
			<p class="instructions">Thank you for requesting a password reset. If your account is currently active, 
			you will receive an email with a reset link. If you do not receive this email, 
			please check your spam filter or contact your system administrator to confirm your account is still active.
			</p>
			<br>
			<div style="text-align: center;"><g:link action="login" class="light">Back to Login</g:link></div>
		</g:if>
		<g:else>
			<h1 class="login-box-msg">Password Assistant</h1>

			<div id="spinner" class="spinner" style="display: none;"><img
					src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
			</div>

			<g:form action="sendResetPassword" name="forgotPasswordForm">
				<p>We will send an e-mail to you that contains a link to a page where you can create new password.</p>
				<div class="form-group has-feedback">
					<input type="email" class="form-control" name="email" id="email" placeholder="Enter your email address" title="Enter your email address" value="${email}" autocorrect="off" autocapitalize="off" required autofocus>
					<span class="glyphicon glyphicon-envelope form-control-feedback"></span>
				</div>
				<g:if test="${flash.message}">
					<div class="message">${flash.message}</div>
				</g:if>
				<div class="row">
					<div class="col-xs-8">
					</div>
					<!-- /.col -->
					<div class="col-xs-4">
						<button type="submit"  id="resetPasswordSubmitButton" class="btn btn-primary btn-block btn-flat">Send</button>
					</div>
					<!-- /.col -->
				</div>
			</g:form>
			<g:link action="login" class="light" style="margin-right: 16px;">Back to Login</g:link>
		</g:else>

	</div>
	<!-- /.login-box-body -->
</div>
<!-- /.login-box -->
<script type="text/javascript">

	$( document ).ready(function() {

		function isValidEmail(email) {
			var emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/ ;
			return emailExp.test(email);
		}

		$("#forgotPasswordForm").on("submit", function(event) {
			event.preventDefault();
			var emailField = $("#email");
			if (!isValidEmail(emailField.val())) {
				alert("Please enter a valid email address");
				emailField.focus();
				return false;
			} else {

				$("#overlay").css('display', 'inline');

				$(this).off("submit");
				$("#resetPasswordSubmitButton").prop('disabled', true);
				this.submit();
			}
		});
	});
</script>

<div id="overlay">
	<div id="overlay-wrapper">
		<div id="floatingBarsG">
			<div class="blockG" id="rotateG_01"></div>
			<div class="blockG" id="rotateG_02"></div>
			<div class="blockG" id="rotateG_03"></div>
			<div class="blockG" id="rotateG_04"></div>
			<div class="blockG" id="rotateG_05"></div>
			<div class="blockG" id="rotateG_06"></div>
			<div class="blockG" id="rotateG_07"></div>
			<div class="blockG" id="rotateG_08"></div>
		</div>
	</div>
</div>
<!-- /.spinenr-box -->

</body>
</html>
