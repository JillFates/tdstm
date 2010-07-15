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

			<th colspan="2">List of Party Actions</th>

		</tr>
	</thead>
	<tbody>
		<tr>
			<td><g:link controller="partyGroup" style="color:black">Company</g:link></td>
			<td><g:link controller="roleType" style="color:black">Role Type </g:link></td>
		</tr>
		<tr>
			<td><g:link controller="partyRelationship" style="color:black">Party Relationship</g:link></td>
			<td><g:link controller="partyRelationshipType" style="color:black">Party RelationshipType</g:link></td>
		</tr>
		<tr>
			<td><g:link controller="userLogin" style="color:black">Manage Users</g:link></td>
			<td><g:link controller="refCode" style="color:black">Manage RefCode</g:link></td>
		</tr>
		<tr>
			<td><g:link controller="manufacturer" style="color:black">Manufacturer</g:link></td>
			<td><g:link controller="model" style="color:black">Model</g:link></td>
		</tr>
		<tr>
			<td><g:link controller="admin" action="orphanSummary" style="color:black">Manage Orphan Records</g:link></td>
			<td><g:link controller="admin" action="latestUsers" style="color:black">Last 20 users</g:link></td>
		</tr>
		<tr>
			<td><g:link controller="admin" action="currentLiveEvents" style="color:black">Current live events</g:link></td>
			<td><g:link controller="admin" action="upcomingBundles" style="color:black">Upcoming bundles</g:link></td>
		</tr>
	</tbody>
</table>
</div>
</div>
</body>
</html>
