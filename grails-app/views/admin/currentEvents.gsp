<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Current Live Events</title>
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
		<h1 style="margin: 0px;"><b>Current Live Events</b></h1>
		<br/>
		<table>
			<thead>
				<tr>
					<th>Name </th>
				</tr>
			</thead>
			<tbody>
				<g:if test="${moveEventsList}">
					<g:each in="${moveEventsList}" status="i"  var="event">			
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td><g:link controller="project" action="show" id="${event.project?.id}">${event.project?.name} - ${event.name}</g:link></td>
					</tr>
					</g:each>
				</g:if>
				<g:else>
					<tr><td class="no_records">No records found</td></tr>
				</g:else>
			</tbody>
		</table>
		</div>
	</div>
</body>
</html>
