<%@page import="com.tdsops.common.security.spring.TdsHttpSessionRequestCache; com.tdsops.common.security.SecurityUtil" %>
<!DOCTYPE>
<html lang="en">
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
		  href="${resource(dir: 'plugins/jquery-ui-1.10.4/jquery-ui/themes/ui-lightness', file: 'jquery-ui-1.10.4.custom.css')}"/>

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
			$("#submitButton").click(function(e){
				<g:if test="${noticeList && noticeList.size() > 0 && false}">
					e.preventDefault();

					$("#postLoginMessages").dialog({
						resizable: false,
						minWidth: 520,
						modal: true,
						position: ['center', 50]
					});

					$('#confirmAccess').click(function() {
						$("#postLoginMessages").dialog('close');
						$("#overlay").css('display', 'inline');
						$("#submitButton").attr('disabled', true);
						var form = $("form")[0];
						form.submit();
					});

					$('#cancelAccess').click(function() {
						$("#postLoginMessages").dialog('close');
					});


					$('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick" style="margin: -8px !important;"></span>');
				</g:if>
				<g:else>
					$("#overlay").css('display', 'inline');
					$("#submitButton").attr('disabled', true);
					var form = $("form")[0];
					form.submit();
				</g:else>
			});

			$('.openSupportedBrowsers').click(function(event){
				event.preventDefault();
				$( "#dialog" ).dialog(
					{
						modal: true,
						minHeight: 360,
						minWidth: 520,
						resizable: false
					});
				$('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick" style="margin: -8px !important;"></span>');
			});

			// This functions search for the real version and detects if is or not in compatiblity Mode for IE
			function detectIE() {
				//Set defaults
				var value = {
					isIE: false,
					trueVersion: 0,
					actingVersion: 0,
					compatibilityMode: false
				};

				//Try to find the Trident version number
				var trident = navigator.userAgent.match(/Trident\/(\d+)/);
				if (trident) {
					value.isIE = true;
					//Convert from the Trident version number to the IE version number
					value.trueVersion = parseInt(trident[1], 10) + 4;
				}

				//Try to find the MSIE number
				var msie = navigator.userAgent.match(/MSIE (\d+)/);
				if (msie) {
					value.isIE = true;
					//Find the IE version number from the user agent string
					value.actingVersion = parseInt(msie[1]);
				} else {
					//Must be IE 11 in "edge" mode
					value.actingVersion = value.trueVersion;
				}

				//If we have both a Trident and MSIE version number, see if they're different
				if (value.isIE && value.trueVersion > 0 && value.actingVersion > 0) {
					//In compatibility mode if the trident number doesn't match up with the MSIE number
					value.compatibilityMode = value.trueVersion != value.actingVersion;
				}
				return value;
			}


			var browserValue = detectIE();
			if(browserValue.isIE && browserValue.compatibilityMode) {
				$('.compatibility-mode').show();
			} else if(browserValue.isIE && browserValue.trueVersion <= 8) {
				$('.unsopported').show();
			}

			if(browserValue.isIE && browserValue.actingVersion <= 7){
				$('#dialog div.col-xs-3').css('width','90px');
				$('#dialog a.get-latest-version').css('margin-top','30px');
			}
		});

	</script>
</head>
<body class="hold-transition login-page" onload="setFieldFocus()">
<div class="login-box ${(preLoginList && preLoginList.size > 0)? "login-notice" : ""}">

	<div class="login-box-body">
		<div class="login-logo">
			<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
				<img src="${resource(dir:'images',file:'TMLoginLogo.gif')}" border="0" alt="Learn more about TransitionManager" />
			</a>
		</div>
		<g:if test="${preLoginList && preLoginList.size() > 0}">
			<g:each var="notice" in="${preLoginList}">
				<div class="callout pre-login-message">
					${notice.htmlText}
				</div>
			</g:each>
		</g:if>
		<g:if test="${session[TdsHttpSessionRequestCache.SESSION_EXPIRED] == true}">
			<h1 class="login-box-msg message">Your session has expired. Please log in.</h1>
		</g:if>
		<g:elseif test="${session[SecurityUtil.ACCOUNT_LOCKED_OUT] == true}">
			<h1 class="login-box-msg message"><g:message code="userLogin.inactivityLockout.message"/></h1>
		</g:elseif>
		<g:else>
			<h1 class="login-box-msg">Sign in to start your session</h1>
		</g:else>
		<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
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
				<input type="text" class="form-control" name="username" id="usernameid" placeholder="Enter your username" title="Enter your username" value="${username}" autocorrect="off" autocapitalize="off" >
				<span class="glyphicon glyphicon-user form-control-feedback"></span>
			</div>
			<div class="form-group has-feedback">
				<input type="password" class="form-control" name="password" autocorrect="off" autocapitalize="off" placeholder="Enter your password" title="Enter your password"/>
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
			<div class="message unsopported" style="display: none;">
				<p><span>Warning:</span> Our site has detected that you are using an outdated browser version that will cause errors and limit some functionality in the application.</p>
				<p>It is recommended to upgrade your browser or switch to another supported browser.</p>
				<p><a href="#" class="openSupportedBrowsers">View supported browsers.</a></p>
			</div>

			<div class="message compatibility-mode" style="display: none;">
				<p>Internet Explorer is configured to use Compatibility Mode that may cause erratic behavior in the application therefore it is recommended to disable or use a supported browser.</p>
				<p><a href="#" class="openSupportedBrowsers">View more information.</a></p>
			</div>
		</div>
		<div class="loginIframe">
			<pre><g:link controller="auth" action="forgotMyPassword" style="font-weight: normal;">Forgot your password?</g:link></pre>
		</div>
		<div class="buildInfo">${buildInfo}</div>
	</div>
	<!-- /.login-box-body -->

	<div id="dialog" title="Browser Upgrade Recommended" style="display: none;">
		<p><strong>Warning:</strong> Our site has detected that you are using an outdated browser version that will cause errors and limit some functionality in the application.</p>
		<p>It is recommended to upgrade your browser or switch to another supported browser.</p>
		<div class="row" style="margin-top: 28px; margin-left: 0px; font-size: 14px;">
			<div class="col-xs-3">
				<img src="${resource(dir:'icons/png',file:'internet_explorer.png')}" border="0" alt="Internet Explorer Browser" /><br />
				<span style="font-size: 13px;">Internet Explorer 9+</span>
			</div>
			<div class="col-xs-3">
				<img src="${resource(dir:'icons/png',file:'firefox.png')}" border="0" alt="Fire Fox Browser" /><br />
				<span style="font-size: 13px;">FireFox 42+</span>
			</div>
			<div class="col-xs-3" >
				<img src="${resource(dir:'icons/png',file:'chrome.png')}" border="0" alt="Chrome Browser" /><br />
				<span style="font-size: 13px;">Chrome 44+</span>
			</div>
			<div class="col-xs-3" >
				<img src="${resource(dir:'icons/png',file:'safari.png')}" border="0" alt="Safari Browser" /><br />
				<span style="font-size: 13px;">Safari 8+ <br /> (Only Mac)</span>
			</div>
			<br />
		</div>
		<br />
		<a class="get-latest-version" style="float: right; color: #337ab7; font-weight: 600;" href="http://browsehappy.com/?locale=en" target="_blank">Get Latest Versions</a>
	</div>

	<div id="postLoginMessages" title="Confirmation Required" style="display: none;">
			<g:if test="${postLoginList && postLoginList.size() > 0}">
				<g:each var="notice" in="${postLoginList}">
					<h4>${notice.title}</h4>
					${notice.htmlText}
				</g:each>
			</g:if>
			<div class="modal-footer">
				<button id="confirmAccess" type="submit" class="btn btn-primary pull-left"><span class="glyphicon glyphicon-ok"></span> Confirm</button>
				<button id="cancelAccess" type="button" class="btn btn-default pull-right" data-dismiss="modal"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
			</div>
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
