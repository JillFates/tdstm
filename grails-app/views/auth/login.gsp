
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="layout" content="main" />
<title>Login</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
</head>

<body  onload="document.loginForm.username.focus();" style="margin-top: 40px">
<div class="mainbody" >
<table width="100%" style="border: 0; vertical-align: top;" align="center" cellpadding="0"
	cellspacing="0">
	<tr>
		<td width="292px" valign="top" style="">
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
					<tr>
						<td>Username:</td>
						<td><input type="text" name="username" value="${username}" /></td>
					</tr>
					<tr>
						<td>Password:</td>
						<td><input type="password" name="password" value="" /></td>
					</tr>
					<tr>
						<td />
						<td class="buttonR"><input type="submit" value="Sign in" /></td>
					</tr>
				</tbody>
			</table>
		</g:form></div>
		<div class="left_bcornerlog"></div>
		<div class="border_botlog"></div>
		<div class="right_bcornerlog"></div>
		</div>
		</td>
	</tr>
</table>
</div>
</body>
</html>
