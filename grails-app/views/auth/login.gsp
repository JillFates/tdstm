<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
<head>
	<title>Login</title>
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" type="text/css"/>
	<link rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" type="text/css"/>
	<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	<meta name="viewport" content="height=device-height,width=device-width" />
	
	<g:javascript library="application" />
</head>
<body onload="setFieldFocus()">
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

	/* break us out of any containing div or iframes */
	if (top != self) { top.location.replace(self.location.href); }

</script>

<div id="spinner" class="spinner" style="display: none;">
	<img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="logo">
	<table style="border: 0; width: 292px;">
		<tr>
			<td style="text-align: center;">
				<a href="http://www.transitionaldata.com/service/transitionmanager" target="new">
					<img src="${resource(dir:'images',file:'TMLoginLogo.gif')}" border="0" alt="Learn more about TransitionManager" />
				</a>
			</td>
			
		</tr>
	</table>
	<div class="mainbody" >
		<table width="100%" style="border: 0; vertical-align: top;" align="center" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top" style="width:292px" >
					<div class="colum_login">
						<div class="left_cornerlog"></div>
						<div class="border_toplog"></div>
						<div class="right_cornerlog"></div>
						<div class="w_bodylog">
							<g:form action="signIn" name="loginForm">
								<input type="hidden" name="targetUri" value="${targetUri}" />
								<g:if test="${flash.message}">
									<div class="message">${flash.message}</div>
								</g:if>
								<table>
									<tbody>
										<g:if test="${loginConfig.authorityPrompt in ['prompt', 'select']}">
											<tr>
												<td>${loginConfig.authorityLabel}:</td>
												<g:if test="${loginConfig.authorityPrompt == 'prompt'}">
													<td><input type="text" name="authority" value="${authority}" autocorrect="off" autocapitalize="off"></td>
												</g:if>
												<g:if test="${loginConfig.authorityPrompt == 'select'}">
													<td>
														<g:select name="authority" from="${loginConfig.authorityList}" value="${authority}"
															noSelection="['':'Please select']"/>
													</td>
												</g:if>
											</tr>
										</g:if>
										<g:if test="${loginConfig.authorityPrompt == 'hidden'}">
											<input type="hidden" name="authority" value="${loginConfig.authorityName}">
										</g:if>
										
										<tr>
											<td>Username:</td>
											<td>
												<input type="text" name="username" id="usernameId" value="${username}" size="25" autocorrect="off" autocapitalize="off" placeholder="${loginConfig.usernamePlaceholder}"/>
											</td>
										</tr>
										<tr>
											<td>Password:</td>
											<td><input type="password" name="password" size="25" value="" autocorrect="off" autocapitalize="off" placeholder="Enter your password"/></td>
										</tr>
										<tr>
											<td />
											<td class="buttonR"><input type="submit" value="Sign in" /></td>
										</tr>
										<g:if test="${ request.getHeader('User-Agent')}">
											<g:if test="${ request.getHeader('User-Agent').contains('MSIE 6') || request.getHeader('User-Agent').contains('MSIE 7') }">
											<tr>
												<td colspan="2" >
													<div class="message" >Warning: This site no longer supports Internet Explorer 6 or 7. We recommend that you use a newer browser for this site.</div>
												</td>
											</tr>
											</g:if>
										</g:if>
										<g:else>
											<div class="message" >Warning: Unable to determine your browser type and therefore unable to guarantee the site will work properly.</div>
										</g:else>
									</tbody>
								</table>
							</g:form>
						</div>
						<div class="left_bcornerlog"></div>
						<div class="border_botlog"></div>
						<div class="right_bcornerlog"></div>
						<div id="buildInfoId">
							<pre style="word-wrap: break-word; white-space: pre-wrap;">${buildInfo}</pre>
						</div>
					</div>
				</td>
			</tr>
		</table>
	</div>
</div>
<div class="logo"></div>
</body>
</html>
