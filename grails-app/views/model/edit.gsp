<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
		<title>Model - Edit</title>
	</head>
	<body>
		<div class="body">
			<h1>Model - Edit</h1>
			<g:if test="${flash.message}">
				 <div class="message">${flash.message}</div>
			</g:if>
			<g:render template="/model/edit" />
			<input name="id" value="${modelInstance.id}" type="hidden" id="modelId"/>
			<input type="hidden" name="asset" id="modelAsset" value="asset"/>
		</div>
	</body>
</html>
