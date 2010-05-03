<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>JsecUser List</title>
<style type="text/css">
a:hover {
	text-decoration: underline;
}
</style>
</head>
<body>
<div class="body">
<div>&nbsp;</div>
	<div>
 	<g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
 	</g:if>
<table>
	<thead>
		<tr>
			<th>Table</th>
			<th>Reference Id</th>
			<th>Type</th>
			<th>Total Orphan Records</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${summaryRecords}" var="summary">
			<tr>
				<td>${summary.mainTable}</td>
				<td>${summary.refId}</td>
				<td>${summary.type}</td>
				<td><a href="#">${summary.totalCount}</a></td>
			</tr>
		</g:each>
	</tbody>
</table>
</div>
</div>
</body>
</html>
