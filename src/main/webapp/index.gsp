<%@ page import="com.tdsops.common.grails.ApplicationContextHolder" %>
<%@ page import="net.transitionmanager.security.SecurityService" %>
<html>
<head>
<title>TransitionManager</title>
<meta name="layout" content="main" />
</head>
<body>
<%

	/**
	 * This logic is responsible for redirecting the user when going to root URL path (/) to their preferred page
	 * when logged in or to the official login page appropriately
	 */

	//
	SecurityService securityService = ApplicationContextHolder.getService('securityService')
	if (securityService.isLoggedIn()) {
		// TODO : JPM 10/2019 : Change the redirection here to use the logic that currently is in AuthController.redirectToPrefPage
		response.sendRedirect('/tdstm/module/user/dashboard')
	} else {
		// response.sendRedirect("module/auth/login")
		response.sendRedirect(securityService.loginUrl())
	}
%>
</body>
</html>
