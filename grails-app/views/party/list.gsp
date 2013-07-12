<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Party List</title>
</head>
<body>
<div class="body">
	<h1>Party List</h1>
	<tds:hasPermission permission='PartyCreateView'>
		<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
			<span class="menuButton"><g:link class="create" action="create">Create Party</g:link></span>
		</div>
	</tds:hasPermission>
	<br>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div class="list">
		<table>
			<thead>
				<tr>

					<g:sortableColumn property="id" title="Id" />

					<g:sortableColumn property="dateCreated" title="Date Created" />

					<g:sortableColumn property="lastUpdated" title="Last Updated" />

					<th>Party Type</th>

				</tr>
			</thead>
			<tbody>
				<g:each in="${partyInstanceList}" status="i" var="partyInstance">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

						<td><g:link action="show" id="${partyInstance.id}">${fieldValue(bean:partyInstance, field:'id')}</g:link></td>

						<td>${fieldValue(bean:partyInstance, field:'dateCreated')}</td>

						<td>${fieldValue(bean:partyInstance, field:'lastUpdated')}</td>

						<td>${fieldValue(bean:partyInstance, field:'partyType')}</td>

					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
	<div class="paginateButtons"><g:paginate total="${Party.count()}" /></div>
</div>
</body>
</html>