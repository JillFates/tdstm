<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<title>Login</title>
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
	<link rel="stylesheet" href="${resource(dir:'css',file:'style.css')}">

	<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'spinner.css')}" />

	<!-- jQuery -->
	<script src="${resource(dir:'dist/js/vendors/jquery/dist',file:'jquery.min.js')}"></script>


	<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
	<!--[if lt IE 9]>
	<script src="${resource(dir:'dist/js/vendors/html5shiv/dist',file:'html5shiv.min.js')}"></script>
	<script src="${resource(dir:'dist/js/vendors/respond/dest',file:'respond.min.js')}"></script>
	<![endif]-->
	<script language="javascript" type="text/javascript">
		// Let's keep this until migrate the login into angularjs.

		function setFieldFocus() {
			<g:if test="${loginConfig.authorityPrompt in ['select', 'prompt']}">
			var field = document.loginForm.authority;
			</g:if>
			<g:else>
			var field = document.loginForm.username;
			</g:else>
			field.focus();
		}

		// break us out of any containing div or iframes
		if (top != self) { top.location.replace(self.location.href); }

		$(document).ready(function() {
			$("#submitButton").click(function(){
				$("#overlay").css('display', 'inline');
				$("#submitButton").attr('disabled', true);
				var form = $("form")[0];
				form.submit();
			});
		});

	</script>
</head>
<body class="hold-transition login-page" onload="setFieldFocus()">
<div class="login-box">

	<div class="login-box-body">
		<div class="login-logo">
			<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
				<img src="${resource(dir:'images',file:'TMLoginLogo.gif')}" border="0" alt="Learn more about TransitionManager" />
			</a>
		</div>
		<p class="login-box-msg">Sign in to start your session</p>
		<div id="spinner" class="spinner" style="display: none;"><img
				src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
		</div>
		<g:form action="signIn" name="loginForm" class="loginPageFormWrapper">
			<input type="hidden" name="targetUri" value="${targetUri}" />
			<g:if test="${loginConfig.authorityPrompt == 'hidden'}">
				<input type="hidden" name="authority" value="${loginConfig.authorityName}">
			</g:if>
			<g:if test="${loginConfig.authorityPrompt in ['prompt', 'select']}">
				<div class="form-group">
					<span>${loginConfig.authorityLabel}:</span>
					<g:if test="${loginConfig.authorityPrompt == 'prompt'}">
						<input type="text"  class="form-control" name="authority" value="${authority}" autocorrect="off" autocapitalize="off">
					</g:if>
					<g:if test="${loginConfig.authorityPrompt == 'select'}">
						<g:select class="form-control" name="authority" from="${loginConfig.authorityList}" value="${authority}" noSelection="['':'Please select']"/>
					</g:if>
				</div>
			</g:if>
			<div class="form-group has-feedback">
				<input type="text" class="form-control" name="username" id="usernameid" placeholder="Enter your username" value="${username}" autocorrect="off" autocapitalize="off" >
				<span class="glyphicon glyphicon-user form-control-feedback"></span>
			</div>
			<div class="form-group has-feedback">
				<input type="password" class="form-control" name="password" autocorrect="off" autocapitalize="off" placeholder="Enter your password"/>
				<span class="glyphicon glyphicon-lock form-control-feedback"></span>
			</div>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="row">
				<div class="col-xs-8">
				</div>
				<!-- /.col -->
				<div class="col-xs-4">
					<button type="submit" id="submitButton" class="btn btn-primary btn-block btn-flat">Sign In</button>
				</div>
				<!-- /.col -->
			</div>
		</g:form>

		<div class="loginErrorMsg">
			<g:if test="${request.getHeader('User-Agent')}">
				<g:if test="${request.getHeader('User-Agent').contains('MSIE 6') || request.getHeader('User-Agent').contains('MSIE 7') || request.getHeader('User-Agent').contains('MSIE 8')}">
					<div class="message" >Warning: This site no longer supports version of Internet Explorer before version 9. We recommend that you use a newer browser for this site.</div>
				</g:if>
			</g:if>
			<g:else>
				<div class="alert alert-warning"><strong>Warning!</strong> Unable to determine your browser type and therefore unable to guarantee the site will work properly.</div>
			</g:else>
		</div>
		<div class="loginIframe">
			<pre><g:link controller="auth" action="forgotMyPassword" style="font-weight: normal;">Forgot your password?</g:link></pre>
		</div>
		<div class="buildInfo">${buildInfo}</div>
	</div>
	<!-- /.login-box-body -->
</div>
<!-- /.login-box -->


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
