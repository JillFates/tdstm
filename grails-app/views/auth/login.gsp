<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
<head>
<title>Login</title>
<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" type="text/css"/>
<link rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" type="text/css"/>
<link rel="shortcut icon"
	href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<meta name="viewport" content="height=device-height,width=device-width" />
	
<g:javascript library="application" />
</head>
<body onload="document.loginForm.username.focus()">
<script language="javascript" type="text/javascript">
	/* break us out of any containing div or iframes */
	if (top != self) { top.location.replace(self.location.href); }
</script>
<div id="spinner" class="spinner" style="display: none;"><img
	src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="logo">
<table style="border: 0; width: 292px;">
	<tr>
		<td style="text-align: center;">
			<a href="http://www.transitionaldata.com/" target="new">
			<img src="${resource(dir:'images',file:'tds3b.png')}" border="0" alt="Visit TDS" /></a>
			<br />
			<h1>TransitionManager&trade;</h1>
		</td>
		
	</tr>
</table>
<div class="mainbody" >
<table width="100%" style="border: 0; vertical-align: top;" align="center" cellpadding="0" cellspacing="0">
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
						<td><input type="text" name="username" id="usernameId" value="${username}" autocorrect="off" autocapitalize="off" /></td>
					</tr>
					<tr>
						<td>Password:</td>
						<td><input type="password" name="password" value="" /></td>
					</tr>
					<tr>
						<td />
						<td class="buttonR"><input type="submit" value="Sign in" /></td>
					</tr>
					<g:if test="${ request.getHeader('User-Agent').contains('MSIE 6') || request.getHeader('User-Agent').contains('MSIE 7') }">
					<tr>
						<td colspan="2" >
							<div class="message" >Warning: This site no longer supports Internet Explorer 6 or 7. We recommend that you use a newer browser for this site.</div>
						</td>
					</tr>
					</g:if>
				</tbody>
			</table>
		</g:form></div>
		<div class="left_bcornerlog"></div>
		<div class="border_botlog"></div>
		<div class="right_bcornerlog"></div>
		<iframe src="/tdstm/build.txt" width="240" height="30" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
		</div>
		</td>
	</tr>
</table>
<script language="javascript" type="text/javascript">
 var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
 var string_length = 4;
 var randomstring = '';
 for (var i=0; i<string_length; i++) {
  var rnum = Math.floor(Math.random() * chars.length);
  randomstring += chars.substring(rnum,rnum+1);
 }
 window.frames[0].location = '../build.txt?'+randomstring;
</script>
</div>
</div>
<div class="logo"></div>
</body>
</html>
