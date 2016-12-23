<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>TransitionManager&trade; Error</title>
	    <link rel="stylesheet" href="/tdstm/css/main.css" type="text/css"/>
	    <link rel="stylesheet" href="/tdstm/css/tds.css" type="text/css"/>

	</head>

	<body>
		<div class="tds_header">
			<div class="header_left">
				<a href="/tdstm/" target="new"><img src="${resource(dir:'images',file:'TMMenuLogo.png')}" style="float: left;border: 0px;height: 30px;"/></a>
			</div>
			<div class="title">&nbsp;TransitionManager&trade; Error</div>
		</div>


		<div class="main_body">
			<h2>Damn It! Something apparently didn't go the way that we were expecting...</h2>
			<img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSo3UcXNuQjEyZBTKecFik8nnNj-e5ld33qt-9zmWuJdghkOfGV1Q" width="100" />
			<br/>
			<a href="${continueUrl}">Click Here</a> to get out of this mess
			<br/>
			<g:if test="${showStacktrace}">
				<g:if test="${exception}">
					<hr>
					<h2>The following should ONLY appear when in DEVELOPMENT mode</h2>
					<strong>Exception:</strong> <span> ${exception?.getClass().getName()} </span> <br />
					<strong>Exception Message:</strong> <span> ${exception.message?.encodeAsHTML()} </span> <br />
					<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
					<pre>${com.tdsops.common.lang.ExceptionUtil.stackTraceToString(exception)}</pre>
				</g:if>
			</g:if>
		</div>
	</body>
</html>
