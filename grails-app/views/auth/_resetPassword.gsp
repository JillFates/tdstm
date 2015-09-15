<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
<head>
<title>Reset Password</title>
<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" type="text/css"/>
<link rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" type="text/css"/>
<link rel="shortcut icon"
	href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

<meta name="viewport" content="height=device-height,width=device-width" />

<g:javascript src="jquery-1.9.1.js"/>
<g:javascript src="PasswordValidation.js" />

<script type="text/javascript">
	$( document ).ready(function() {
		$("#forgotPasswordForm").on("submit", function(event) {
			event.preventDefault();
			var passwordField = $("#password");
			if (!PasswordValidation.checkPassword(passwordField[0])) {
				alert("The password does not adhere to the specified requirements.");
				passwordField.focus();
				return false;
			} else {
				$(this).off("submit");
				$("#resetPasswordSubmitButton").prop('disabled', true)
				this.submit();
			}
		});
	});
</script>
	
</head>
<body>

<div id="spinner" class="spinner" style="display: none;"><img
	src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>

<div class="logo">
	<table style="border: 0; width: 292px;">
		<tr>
			<td style="text-align: center;">
				<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
				<img src="${resource(dir:'images',file:'TMLoginLogo.gif')}" border="0" alt="Learn more about TransitionManager" /></a>
			</td>
			
		</tr>
	</table>
	<div class="mainbody" style="margin: .8em">
		<table style="width:466px" width="100%" style="border: 0; vertical-align: top;" cellpadding="0" cellspacing="0">
			<tr>
				<td  valign="top" style="width:466px" >
				<div class="">
				<div class="left_cornerlog"></div>
				<div class="border_toplog"></div>
				<div class="right_cornerlog"></div>
				<div class="">
				<h1 style="padding: 8px 0px; margin: 0em !important;">Password Assistant - Enter New Password</h1>
				<g:if test="${flash.message}">
					<div class="message">${flash.message}</div>
				</g:if>
				<p>The final step is to enter your email address associated with your account and a new password 
				that adheres to the password requirements listed below.
				</p>

				<g:form action="applyNewPassword" id="forgotPasswordForm" name="forgotPasswordForm">
					<input type="hidden" name="username" id="username" value="${username}" />
					<input type="hidden" name="token" id="token" value="${token}" />
					<table style="border: 0;">
						<tbody>
							<tr>
								<td>Email&nbsp;Address:</td>
								<td width="100%">
									<input type="email" name="email" id="email" value="${email}" size="35"
										required autofocus
										placeholder="Enter your email address" 
										autocorrect="email" autocapitalize="off" />
								</td>
							</tr>
							<tr>
								<td>Password:</td>
								<td>
									<input type="text" id="password" 
										class="passwordField"
										onkeyup="PasswordValidation.checkPassword(this)" 
										name="password"  size="25" value="${password}" 
										required autocapitalize='off' autocorrect='off'
										placeholder="Enter a new password" />
								</td>
							</tr>
							<tr class="passwordsEditFields">
								<td>Requirements:</td>
								<td>
									<ul>
									<li><em id="usernameRequirementId">Password must not contain the username<b class="ok"></b></em><br/>
									<li><em id="lengthRequirementId" size="${minPasswordLength}">Password must be at least ${minPasswordLength} characters long<b class="ok"></b></em><br/>
									<li><em id="passwordRequirementsId">Password must contain at least 3 of these requirements:<b class="ok"></b></em><br/>
									<ul>
										<li><em id="uppercaseRequirementId">Uppercase characters<b class="ok"></b></em></li>
										<li><em id="lowercaseRequirementId">Lowercase characters<b class="ok"></b></em></li>
										<li><em id="numericRequirementId">Numeric characters<b class="ok"></b></em></li>
										<li><em id="symbolRequirementId">Nonalphanumeric characters<b class="ok"></b></em></li>
									</ul>
									</ul>
								</td>
							</tr>
							<tr>
								<td colspan="2" style="text-align:center;" class="buttonR">
									<g:if test="${validToken}">
										<input type="submit" id="resetPasswordSubmitButton" value="Update Password" />
									</g:if>
								</td>
							</tr>
						</tbody>
					</table>
				</g:form>
				</div>
				</td>
			</tr>
		</table>
	</div>
</div>
<div class="logo"></div>
</body>
</html>
