<%@page defaultCodec="none" %>
<g:set var="title" value="TransitionManager&trade; - Forbidden" />

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
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
			<h2>Stop! You are forbidden from going here!</h2>
			<img width="100" height="100" src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSbFpZy-k6o7YRRNjf1ACHOYEnJio1CaJ9OyN9fqBdYZrrb44Nx">

			<br/>

			<a href="${continueUrl}">Click Here</a> to return to your authorized area.
		</div>

	</body>
</html>