<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Project Daily Metrics Job</title>
	<style type="text/css">
		th {
			text-align: center
		}
	</style>
</head>
<body style="font-family:'helvetica','arial';">
<div class="body">
<div>
	<h1>Project Daily Metrics Job</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<g:if test="${success}">
		<div class="message">Process launched.</div>
	</g:if>
	<g:else>
		<div class="message">${errorMessage}</div>
	</g:else>
</div>
</div>
</body>

</html>