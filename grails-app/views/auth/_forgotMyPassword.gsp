<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<g:if test="${success}">
	<title>Forgot Password - Step 2</title>
</g:if>
<g:else>
	<title>Forgot Password</title>
</g:else>	
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" type="text/css"/>
	<link rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" type="text/css"/>
	<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	<g:javascript src="jquery-1.9.1.js"/>
	<g:javascript src="jquery-1.9.1-ui.js"/>
	<g:javascript src="tds-common.js" />
	<meta name="viewport" content="height=device-height,width=device-width" />
</head>
<body>

<div id="spinner" class="spinner" style="display: none;"><img
	src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>

<div class="logo">
	<table style="border: 0; width: 450px;">
		<tr>
			<td>
				<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
				<img src="${resource(dir:'images',file:'TMLoginLogo.gif')}" border="0" alt="Learn more about TransitionManager" /></a>
			</td>
		</tr>
	</table>
	<div class="mainbody" style="margin: .8em">
		<table style="width:466px" width="100%" style="border: 0; vertical-align: top;" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
					<div class="xxcolum_login">
						<div class="left_cornerlog"></div>
						<div class="border_toplog"></div>
						<div class="right_cornerlog"></div>
						<div class="xxw_bodylog">
					<g:if test="${success}">
						<h1 style="padding: 8px 0px; margin: 0em !important;">Password Assistant - Check Your Email</h1>
						<p>We sent you an email with a link to reset your password. It may take a few minutes to reach your inbox. 
						If you don’t see the email, be sure to check your spam folder.
						</p>
						<br>
						<div style="text-align: center;"><g:link action="login" class="light">Back to Login</g:link></div>
					</g:if>
					<g:else>
						<h1 style="padding: 8px 0px; margin: 0em !important;">Password Assistant</h1>
						<g:form action="sendResetPassword" name="forgotPasswordForm">
							<g:if test="${flash.message}">
								<div class="message">${flash.message}</div>
							</g:if>
								<p>Please enter the e-mail address associated with your TransitionManager account, then click Send. We will send an e-mail to
								you that contains a link to a page where you can create new password.</p>
								<br/>
								<p>
									<label for="email">Email Address:</label>
									<input type="email" name="email" id="email" value="${email}" size="35"
										required autofocus
										placeholder="Enter your email address" 
										autocorrect="email" autocapitalize="off" />
								</p>
								<br/>
								<p style="text-align:center;" class="buttonR">
									<g:link action="login" class="light" style="margin-right: 16px;">Back to Login</g:link>
									<input type="submit" id="resetPasswordSubmitButton" value="Send" />
								</p>

						</g:form>
					</g:else>	
					</div>
				</td>
			</tr>
		</table>
	</div>
</div>
<div class="logo"></div>
<script type="text/javascript">
	$( document ).ready(function() {
		$("#forgotPasswordForm").on("submit", function(event) {
			event.preventDefault();
			var emailField = $("#email");
			if (!tdsCommon.isValidEmail(emailField.val())) {
				alert("Please enter a valid email address");
				emailField.focus();
				return false;
			} else {
				$(this).off("submit");
				$("#resetPasswordSubmitButton").prop('disabled', true)
				this.submit();
			}
		});
	});
</script>
</body>
</html>
