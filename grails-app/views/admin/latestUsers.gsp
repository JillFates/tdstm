<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Latest users</title>
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
		<h1 style="margin: 0px;"><b>Last 20 users</b></h1>
		<br/>
		<table>
			<thead>
				<tr>
					<th>Person</th>
					<th>User Name</th>
					<th>Last logged</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${latestUsers}" status="i"  var="user">			
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td><g:link controller="person" action="show" id="${user.person?.id}">${user.person}</g:link></td>
					<td><g:link controller="userLogin" action="show" id="${user.id}">${user.username}</g:link></td>
					<td><tds:convertDateTime date="${user.lastLogin}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
				</tr>
				</g:each>
			</tbody>
		</table>
		</div>
	</div>
</body>
</html>
