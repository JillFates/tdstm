<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Project Daily Metrics Job</title>
	<style type="text/css">
		th {
			text-align: center
		}
	</style>
</head>
<body style="font-family:'helvetica','arial';">
<tds:subHeader title="Project Daily Metrics Job" crumbs="['Admin','Portal','Project Daily Metrics Job']"/>
<div class="body">
<div>
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
