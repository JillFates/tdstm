<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">

	<title>Reset Password</title>
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
	<!-- Clarity CSS -->
	<link rel="stylesheet" href="https://unpkg.com/@clr/ui/clr-ui.min.css" />
    <!--CLARITY ICONS STYLE-->
    <link rel="stylesheet" href="${resource(dir:'dist/css/clarity',file:'clr-icons.min.css')}">
	<link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <!--CLARITY ICONS API & ALL ICON SETS-->
    <script src="${resource(dir:'dist/js/vendors/clarity',file:'clr-icons.min.js')}"></script>

	<!-- General Template Style -->
	<asset:stylesheet href="css/tds-style.css" />
	<asset:link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
	<script src="${resource(dir:'dist/js/vendors/jquery/dist',file:'jquery.min.js')}"></script>
	<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.min.js"></script>

	<asset:stylesheet href="css/spinner.css" />

	<g:javascript src="PasswordValidation.js" />

	</head>
	<body class="hold-transition login-page">
		<div id="passwordModal" class="login-box modal" style="padding: 0; background: none; width: 800px;">
			<div class="modal-content" style="width: 400px; margin: 0 auto 0 auto;">
				<span class="close" style="padding: 5px 5px 0 0;">&times;</span>
				<div class="login-box-body" style="width: 360px; margin: 0 auto 0 auto; border: none !important;">
				<div class="login-logo">
					<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
						<asset:image src="images/TMLoginLogo_v4.7.png" border="0" alt="Learn more about TransitionManager" />
					</a>
				</div>
				<div>
					<h1>Password Assistant - Enter New Password</h1>
				</div>

				<div id="spinner" class="spinner" style="display: none;"><img
						src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
				</div>

				<g:form action="applyNewPassword" name="forgotPasswordForm" method="post" id="forgotPasswordForm">
					<input type="hidden" name="username" id="username" value="${username}" />
					<input type="hidden" name="token" id="token" value="${token}" />
					<p>The final step is to enter your email address associated with your account and a new password.
					</p>
					<div class="clr-row">
                        <form clrForm clrLayout="vertical">
								<clr-input-container class="clr-col-12">
									<input clrInput class="clr-input" type="email"
										   name="email" id="email" placeholder="Enter your email address"
										   value="${email}" autocorrect="off" autocapitalize="off" required autofocus style="width: 100%"/>
								</clr-input-container>
								<clr-password-container class="clr-col-12">
									<input clrPassword type="password"
										   class="clr-input" id="passwordId" name="password" autocorrect="off"
										   autocapitalize="off" placeholder="Enter your <g:if test="${changingPassword}">New </g:if> password" onkeyup="PasswordValidation.checkPassword(this)" style="width: 100%"/>
								</clr-password-container>
                                <div class="requirements-container">
                                    <div class="clr-row">
                                        <div class="clr-col-10">
                                            <span class="label no-border responsive-text">
                                                Password must not contain the username
                                            </span>
                                        </div>
                                        <div class="clr-col-2">
                                            <clr-icon shape="" class="is-success" id="usernameRequirementId"></clr-icon>
                                        </div>
                                    </div>
                                    <div class="clr-row">
                                        <div class="clr-col-10">
                                            <span class="label no-border responsive-text">
                                                Password must be at least ${minPasswordLength} characters long
                                            </span>
                                        </div>
                                        <div class="clr-col-2">
                                            <clr-icon shape="" class="is-success" min-size="${minPasswordLength}" id="lengthRequirementId"></clr-icon>
                                        </div>
                                    </div>
                                    <div class="clr-row">
                                        <div class="clr-col-10">
                                            <span class="label no-border responsive-text" style="text-align: left; white-space: normal;">
                                                Password must contain at least 3 of these requirements:
                                            </span>
                                        </div>
                                        <div class="clr-col-2">
                                            <clr-icon shape="" class="is-success" id="passwordRequirementsId">
                                            </clr-icon>
                                        </div>
                                    </div>
                                    <ul class="list" style="padding: 0 1rem 0 1rem;">
                                        <li>
                                            <div class="clr-row">
                                                <div class="clr-col-11">
                                                    <span class="label no-border responsive-text">
                                                        Uppercase characters
                                                    </span>
                                                </div>
                                                <div class="clr-row-1">
                                                    <clr-icon shape="" class="is-success" id="uppercaseRequirementId"></clr-icon>
                                                </div>
                                            </div>
                                        </li>
                                        <li>
                                            <div class="clr-row">
                                                <div class="clr-col-11">
                                                    <span class="label no-border responsive-text">
                                                        Lowercase characters
                                                    </span>
                                                </div>
                                                <div class="clr-row-1">
                                                    <clr-icon shape="" class="is-success" id="lowercaseRequirementId"></clr-icon>
                                                </div>
                                            </div>
                                        </li>
                                        <li>
                                            <div class="clr-row">
                                                <div class="clr-col-11">
                                                    <span class="label no-border responsive-text">
                                                        Numeric characters
                                                    </span>
                                                </div>
                                                <div class="clr-row-1">
                                                    <clr-icon shape="" class="is-success" id="numericRequirementId"></clr-icon>
                                                </div>
                                            </div>
                                        </li>
                                        <li>
                                            <div class="clr-row">
                                                <div class="clr-col-11">
                                                    <span class="label no-border responsive-text">
                                                        Nonalphanumeric characters
                                                    </span>
                                                </div>
                                                <div class="clr-row-1">
                                                    <clr-icon shape="" class="is-success" id="symbolRequirementId"></clr-icon>
                                                </div>
                                            </div>
                                        </li>
                                    </ul>
                                </div>
								<clr-password-container class="clr-col-12">
									<input clrPassword class="clr-input" type="password" id="confirmPasswordId"
										   name="confirmPassword" autocorrect="off" autocapitalize="off"
										   placeholder="Confirm <g:if test="${changingPassword}">new </g:if>password" onkeyup="PasswordValidation.confirmPassword($('#passwordId')[0], this)" style="width: 100%"
										   required/>
								</clr-password-container>
                                <span id="retypedPasswordMatchRequirementId" class="label no-border" style="color: red;">Passwords should match</span><br>
                                <div class="clr-col-12 buttons-container">
									<div class="clr-row">
										<div class="clr-col-xl-4 clr-col-lg-5 clr-col-md-5 clr-col-sm-12">
											<a href="javascript:void(0)" (click)="backToLogin()"
											   class="light back-to-login">Back to login</a>
										</div>
										<div class="clr-col-xl-8 clr-col-lg-7 clr-col-md-7 clr-col-sm-12">
											<g:if test="${validToken}">
												<g:actionSubmit id="resetPasswordSubmitButton" class="btn btn-primary btn-block btn-flat" value="Update Password" action="applyNewPassword" disabled="true"/>
											</g:if>
										</div>
									</div>
								</div>
							</form>
					</div>
					<g:if test="${flash.message}">
						<div class="message">${flash.message}</div>
					</g:if>

				</g:form>
				</div>
				</div>
			</div>
		</div>
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

			const modal = document.getElementById("passwordModal");
			const span = document.getElementsByClassName("close")[0];

			$(document).ready(function() {
				modal.style.display = "block";
				$(".modal-content").resizable({minWidth: 360, maxWidth: 800});
				$("#passwordModal").draggable();
				$("#submitButton").click(function(){
					$("#overlay").css('display', 'inline');
				});
				$('#email').focus();

				$('.close').click(function() {
					if (confirm("Return to the login page?") === true) {
						window.location.href = "/tdstm";
					}
				});
			});

			$(document).keyup(function(e) {
				if (e.key === "Escape") { // escape key maps to keycode `27`
					if (confirm("Return to the login page?") === true) {
						window.location.href = "/tdstm";
					}
				}
			});

		</script>
	</body>
</html>
