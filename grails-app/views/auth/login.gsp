<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
<head>
<title>Login</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css"/>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" type="text/css"/>
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	
<g:javascript library="application" />
</head>
<body onload="document.loginForm.username.focus()">
<div id="spinner" class="spinner" style="display: none;"><img
	src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="logo">
<table style="border: 0;">
	<tr>
		<td>
		<%--<a href="http://www.transitionaldata.com/" target="new"><img
			src="${createLinkTo(dir:'images',file:'tds.jpg')}" border="0"
			alt="tds" /></a>--%>
			<h1>Transition Manager</h1>
			</td>
		
	</tr>
</table>
<div class="mainbody" >
<table width="100%" style="border: 0; vertical-align: top;" align="center" cellpadding="0"
	cellspacing="0">
	<tr>
		<td  valign="top" style="width:292px" >
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
						<td><input type="text" name="username" id="usernameId" value="${username}" /></td>
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
</div>
<div class="logo"></div>
</body>
</html>
		