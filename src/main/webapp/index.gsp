<%@ page import="com.tdsops.common.grails.ApplicationContextHolder" %>
<%@ page import="net.transitionmanager.security.SecurityService" %>
<html>
<head>
<title>Welcome to Grails</title>
<meta name="layout" content="main" />
</head>
<body><!-- 
<h1 style="margin-left: 20px;">Welcome to Grails</h1>
<p style="margin-left: 20px; width: 80%">Congratulations, you have
successfully started your first Grails application! At the moment this
is the default page, feel free to modify it to either redirect to a
controller or display whatever content you may choose. Below is a list
of controllers that are currently deployed in this application, click on
each to execute its default action:</p>
<div class="dialog" style="margin-left: 20px; width: 60%;">
<ul>
	<g:each var="c" in="${grailsApplication.controllerClasses}">
		<li class="controller"><g:link
			controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
	</g:each>
</ul>
</div> -->
<%
	SecurityService securityService = ApplicationContextHolder.getService('securityService')
	if (securityService.isLoggedIn()) {
		response.sendRedirect('/tdstm/module/user/dashboard')
	} else {
		response.sendRedirect("module/auth/login?index.gsp=1")
	}
%>
</body>
</html>
