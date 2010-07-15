<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Upcoming bundles</title>
<g:javascript src="orphanData.js" />
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
		<h1 style="margin: 0px;"><b>Upcoming bundles</b></h1>
		<br/>
		<table>
			<thead>
				<tr>
					<th>Name </th>
					<th>Start Time</th>
					<th>Completion Time</th>
				</tr>
			</thead>
			<tbody>
				<g:if test="${moveBundlesList}">
					<g:each in="${moveBundlesList}" status="i"  var="bundle">			
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td><g:link controller="project" action="show" id="${bundle.project?.id}">${bundle.project?.name} - ${bundle.name}</g:link></td>
						<td><tds:convertDateTime date="${bundle.startTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
						<td><tds:convertDateTime date="${bundle.completionTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
					</tr>
					</g:each>
				</g:if>
				<g:else>
					<tr><td colspan="3" class="no_records">No records found</td></tr>
				</g:else>
			</tbody>
		</table>
		</div>
	</div>
</body>
</html>
