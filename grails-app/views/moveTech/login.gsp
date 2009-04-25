<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Login</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />

</head>
<body onload="document.loginForm.username.focus()">
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: auto;" >
				<div class="colum_techlogin">
				<g:if test="${flash.message}">
						<div style="width: 80%;" class="message">${flash.message}</div>
					</g:if>			
				<div class="w_techlog">
				<g:form action="signIn" name="loginForm">
					<input type="hidden" name="targetUri" value="${targetUri}" />
					
					<div style="float:left; width:100%;text-align:left; height:20px; border:1px solid #5F9FCF;">
								<span style="text-align:left;font-size:10"><b>Login</b></span>
								</div>
								<div style="float:left; padding-left:5px;width:100%;"><a href="http://www.transitionaldata.com/" target="new"><img
								src="${createLinkTo(dir:'images',file:'tds.jpg')}" border="0"
								alt="tds" /></a>
								</div>
								<div style="float:left; width:100%;margin:15px 0;"><div style="text-align:center;margin:5px 0; color:#43ca56;"><h3>Transition Manager</h3></div>
								<div style="float:left; width:100%;margin:25px 0;">Username:
								<input type="text" name="username" value="${username}" /></div>							
								<div style="float:left; width:100%;text-align:center;"class="buttonR">
								<input type="submit" value="Sign in" />
								</div>
			</g:form></div>
			<div class="left_bcornerlog"></div>
			<div class="right_bcornerlog"></div>
			</div>
	</div>
<div class="logo"></div>
</body>
</html>
