<%@page import="net.transitionmanager.service.ErrorHandler" %>
<%@page defaultCodec="none" %>
<g:set var="title" value="TransitionManager&trade; - Unauthorized" />

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<%--
        <meta name="layout" content="projectHeader" />
--%>
		<title>${title}</title>
	    <link rel="stylesheet" href="/tdstm/css/main.css" type="text/css"/>
	    <link rel="stylesheet" href="/tdstm/css/tds.css" type="text/css"/>

	</head>

	<body>
		<div class="tds_header">
			<div class="header_left">
				<a href="/tdstm/" target="new"><img src="${resource(dir:'images',file:'TMMenuLogo.png')}" style="float: left;border: 0px;height: 30px;"/></a>
			</div>
			<div class="title">${title}</div>
		</div>

		<div class="main_body">
			<h1>You've been denied! Better luck next time!</h1>
			<img width="100" height="100" src="http://previews.123rf.com/images/shadowalice/shadowalice1504/shadowalice150400040/38318006-no-touch-icon-great-for-any-use--Stock-Vector-no.jpg">

		</div>

	</body>
</html>