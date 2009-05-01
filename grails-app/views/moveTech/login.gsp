<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Login</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />

</head>
<body onload="document.loginForm.username.focus()">
<div id="spinner" class="spinner" style="display: none;"><img
	src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="mainbody" style="width: auto;">
<div class="colum_techlogin" style="float:left;">
<g:if test="${flash.message}">
	<div style="width: 204px;" class="message">${flash.message}</div>
</g:if>
<div class="w_techlog"><g:form action="signIn" name="loginForm">
	<input type="hidden" name="targetUri" value="${targetUri}" />

	<div
		style="float: left; padding-left: 5px; width: 100%; margin-top: 25px;"><a
		href="http://www.transitionaldata.com/" target="new"><img
		src="${createLinkTo(dir:'images',file:'tds.jpg')}" border="0"
		alt="tds" /></a></div>

	<div
		style="float: left; width: 100%; margin: 50px 0; text-align: center;">
	<table style="border: 0px;">
		<tbody>
			<tr>
				<td height="15px;"></td>
			</tr>
			<tr>
				<td colspan="2" style="text-align: center;"><span
					style="color: #328714; font: bold 15px arial;">Transition
				Manager</span></td>
			</tr>
			<tr>
				<td height="25px;"></td>
			</tr>
			<tr>
				<td><label>Username:</label></td>
				<td><input type="text" name="username" value="${username}" /></td>
			</tr>
			<tr>
				<td height="25px;"></td>
			</tr>
			<tr>
				<td class="buttonR" style="text-align: center;" COLSPAN="2"><input
					type="submit" value="Login" /></td>
			</tr>
		</tbody>
	</table>
	</div>
</g:form></div>
<div class="left_bcornerlog"></div>
<div class="right_bcornerlog"></div>
</div>
</div>
<div class="logo"></div>
</body>
</html>
