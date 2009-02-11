<html>
<head>
<title><g:layoutTitle default="Grails" /></title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}"
	type="image/x-icon" />
<g:layoutHead />
<g:javascript library="application" />
</head>
<body>
<div id="spinner" class="spinner" style="display: none;"><img
	src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="logo">
<table style="border: 0;">
	<tr>
		<td><a href="http://www.transitionaldata.com/" target="new" ><img src="${createLinkTo(dir:'images',file:'tds.jpg')}" border="0" alt="tds" /></a> </td>
		<td style="padding-left: 20%"><jsec:isLoggedIn>
			<strong>Welcome <jsec:principal />&nbsp;! </strong>
			&nbsp;<g:link controller="auth" action="signOut" style="color: #328714">sign out</g:link>
		</jsec:isLoggedIn></td>
	</tr>
</table>

</div>
<div class="logo"></div>

<g:layoutBody />
</body>
</html>