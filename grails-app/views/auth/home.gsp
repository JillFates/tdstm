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
<div><g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<table style="border: 0">
	<tr>
		<td>
		<table>
			<thead>
				<tr>
					<th colspan="2">List of Party Actions</th>
				</tr>
			</thead>
			<tbody>
				<tr class="odd">
					<td><g:link controller="partyGroup" style="color:black">Company</g:link></td>
					<td><g:link controller="roleType" style="color:black">Role Type </g:link></td>
				</tr>
				<tr class="even">
					<td><g:link controller="partyRelationship" style="color:black">Party Relationship</g:link></td>
					<td><g:link controller="partyRelationshipType"
						style="color:black">Party RelationshipType</g:link></td>
				</tr>
				<tr class="odd">
					<td><g:link controller="userLogin" style="color:black">Manage Users</g:link></td>
					<td><g:link controller="refCode" style="color:black">Manage RefCode</g:link></td>
				</tr>
				<tr class="even">
					<td><g:link controller="manufacturer" style="color:black">Manufacturer</g:link></td>
					<td><g:link controller="model" style="color:black">Model</g:link></td>
				</tr>
				<tr class="odd">
					<td><g:link controller="admin" action="orphanSummary"
						style="color:black">Manage Orphan Records</g:link></td>
					<td></td>
				</tr>
			</tbody>
		</table>
		</td>
	</tr>
	<tr>
		<td>
			<div>
			<h1 style="margin-right: 0px;"><b>Recent Users</b></h1>
			<table>
				<thead>
					<tr>
						<th>Person</th>
						<th>User Name</th>
						<th>Last logged</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${recentUsers}" status="i"  var="user">			
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td><g:link controller="person" action="show" id="${user.person?.id}">${user.person}</g:link></td>
						<td><g:link controller="userLogin" action="show" id="${user.id}">${user.username}</g:link></td>
						<td><tds:convertDateTime date="${user.lastLogin}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
					</tr>
					</g:each>
				</tbody>
			</table>
			</div>
		</td>
		<td>
			<div>
			<h1 style="margin-right: 0px;"><b>Current Live Events</b></h1>
			<table>
				<thead>
					<tr>
						<th>Current Active Events:</th>
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
			<br/>
			<div>
				<h1 style="margin-right: 0px;"><b>Upcoming Move Bundles</b></h1>
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
		</td>
	</tr>
</table>
</div>
</div>
</body>
</html>
