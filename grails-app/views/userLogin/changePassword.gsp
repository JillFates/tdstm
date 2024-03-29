<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">

	<title>Change Password</title>
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
	<asset:stylesheet href="css/tds-style.css" />
	<asset:link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

	<script src="${resource(dir:'dist/js/vendors/jquery/dist',file:'jquery.min.js')}"></script>

	<asset:stylesheet href="css/spinner.css" />

	<g:javascript src="PasswordValidation.js" />

    <asset:javascript src="resources/stateManagement.js"/>
</head>
<body class="hold-transition login-page">
<div class="login-box">
	<div class="login-box-body">
		<div class="login-logo">
			<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
				<asset:image src="images/TMLoginLogo.gif" border="0" alt="Learn more about TransitionManager" />
			</a>
		</div>
		<p class="login-box-msg">Change Password</p>
		<div id="spinner" class="spinner" style="display: none;"><img
				src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
		</div>

		<g:form method="post" name="editUserForm" autocomplete="off" class="loginPageFormWrapper">
			<input type="hidden" name="id" value="${userLoginInstance?.id}" />
			<input type="hidden" name="companyId" value="${companyId}" />
			<input type="hidden" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}"/>
			<g:render template="setPasswordFields" model="${[changingPassword:false, minPasswordLength:minPasswordLength]}" />
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="row change-password-submit">
				<div class="col-xs-3">
				</div>
				<!-- /.col -->
				<div class="col-xs-9">
					<g:actionSubmit id="submitButton" class="btn btn-primary btn-block btn-flat" value="Save Password" action="updatePassword"/>
				</div>
				<!-- /.col -->
			</div>
		</g:form>
		<a href="/tdstm/module/auth/login" class="light" onclick="clearStorage()">Back to Login</a>
	</div>
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

<script language="javascript" type="text/javascript">
	// Let's keep this until migrate the login into angularjs.

	$(document).ready(function() {
		$("#submitButton").click(function(){
			$("#overlay").css('display', 'inline');
		});
	});

    function clearStorage() {
        stateManagement.destroyState();
    }

</script>

</body>
</html>
