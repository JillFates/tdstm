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
			<td><g:link controller="party" style="color:black">Party </g:link></td>
			<td><g:link controller="partyGroup" style="color:black">Party Group</g:link></td>
		</tr>
		<tr>
			<td><g:link controller="project" style="color:black">Project</g:link></td>
			<td><g:link controller="partyRole" style="color:black">Party Role</g:link></td>

		</tr>
		<tr>
			<td><g:link controller="person" style="color:black">Person </g:link></td>
			<td><g:link controller="userLogin" style="color:black">User Login</g:link></td>

		</tr>
		<tr>
			<td><g:link controller="partyRelationship" style="color:black">Party Relationship</g:link></td>
		</tr>

	</tbody>
</table>
</div>
</div>
</body>
</html>
