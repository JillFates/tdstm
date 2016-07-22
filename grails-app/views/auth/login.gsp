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

	<tds:favicon />

	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'spinner.css')}" />


	<!-- jQuery -->
	<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'ui.dialog.css')}"/>
	<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'jquery-ui-smoothness.css')}"/>
	<link id="jquery-ui-theme" media="screen, projection" rel="stylesheet" type="text/css"
		  href="${resource(dir: 'plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness', file: 'jquery-ui-1.8.15.custom.css')}"/>

	<script src="${resource(dir:'dist/js/vendors/jquery/dist',file:'jquery.min.js')}"></script>
	<g:javascript src="jquery-1.9.1.js"/>
	<g:javascript src="jquery-1.9.1-ui.js"/>


	<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
	<!--[if lt IE 9]>
	<script src="${resource(dir:'dist/js/vendors/html5shiv/dist',file:'html5shiv.min.js')}"></script>
	<script src="${resource(dir:'dist/js/vendors/respond/dest',file:'respond.min.js')}"></script>
	<![endif]-->
	<script language="javascript" type="text/javascript">

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

			$('#openSupportedBrowsers').click(function(event){
				event.preventDefault();
				$( "#dialog" ).dialog(
					{
						modal: true,
						minHeight: 360,
						minWidth: 500,
						resizable: false
					});
				$('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick" style="margin: -8px !important;"></span>');
			});

			/**
			 * detect IE
			 * returns version of IE or false, if browser is not Internet Explorer
			 */
			function detectIE() {
				var ua = window.navigator.userAgent,
					browserElement = {
						version: 0,
						vendor: ''
					};

				var msie = ua.indexOf('MSIE ');
				if (msie > 0) {
					// IE 10 or older => return version number
					browserElement.vendor = 'IE';
					browserElement.version = parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
					return browserElement;
				}

				var trident = ua.indexOf('Trident/');
				if (trident > 0) {
					// IE 11 => return version number
					var rv = ua.indexOf('rv:');
					browserElement.vendor = 'TRIDENT';
					browserElement.version = parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
					return browserElement;
				}

				var edge = ua.indexOf('Edge/');
				if (edge > 0) {
					// Edge (IE 12+) => return version number
					browserElement.vendor = 'EDGE';
					browserElement.version = parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
					return browserElement;
				}
				
				return browserElement;
			}

			var browserDected = detectIE();

			if(browserDected.vendor === 'IE' && browserDected.version <= 9) {
				$('.loginErrorMsg').show();
			}
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

		<div class="loginErrorMsg" style="display: none;">
			<div class="message">
				<p><label>Warning:</label> Our site has detected that you are using an outdated browser version that will cause errors and limit some functionality in the application.</p>
				<p>It is recommended to upgrade your browser or switch to another supported browser.</p>
				<p>Click <a href="#" id="openSupportedBrowsers">here</a> for supported browsers.</p>
			</div>
		</div>
		<div class="loginIframe">
			<pre><g:link controller="auth" action="forgotMyPassword" style="font-weight: normal;">Forgot your password?</g:link></pre>
		</div>
		<div class="buildInfo">${buildInfo}</div>
	</div>
	<!-- /.login-box-body -->

	<div id="dialog" title="Browser Upgrade Recommended" style="display: none;">
		<p><label>Warning:</label> Our site has detected that you are using an outdated browser version that will cause errors and limit some functionality in the application.</p>
		<p>It is recommended to upgrade your browser or switch to another supported browser.</p>
		<div class="row" style="margin-top: 28px; margin-left: 0px; font-size: 14px; text-align: justify;">
			<div class="col-xs-3">
				<img src="${resource(dir:'icons/png',file:'internet_explorer.png')}" border="0" />
				Internet Explorer 10+
			</div>
			<div class="col-xs-3">
				<img src="${resource(dir:'icons/png',file:'firefox.png')}" border="0" />
				Fire Fox 45+
			</div>
			<div class="col-xs-3">
				<img src="${resource(dir:'icons/png',file:'chrome.png')}" border="0" />
				Chrome 50+
			</div>
			<div class="col-xs-3">
				<img src="${resource(dir:'icons/png',file:'safari.png')}" border="0" />
				Safari 8+
			</div>
		</div>
		<br />
		<a style="float: right; color: #337ab7; font-weight: 600;" href="http://browsehappy.com/?locale=en" target="_blank">Get Latest Versions</a>
	</div>

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
