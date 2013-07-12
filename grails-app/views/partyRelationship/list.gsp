

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>PartyRelationship List</title>
</head>
<body>
<div class="body">
	<h1>PartyRelationship List</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div class="list">
		<table>
			<thead>
				<tr>

					<th>Party Relationship Type</th>

					<th>Party Id From</th>

					<th>Role Type Code From</th>

					<th>Party Id To</th>

					<th>Role Type Code To</th>

					<th>Status Code</th>

					<th>Comment</th>

				</tr>
			</thead>
			<tbody>
				<g:each in="${partyRelationshipInstanceList}" status="i" var="partyRelationshipInstance">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

						<td><g:link action="show"
							params="[partyRelationshipTypeId:partyRelationshipInstance?.partyRelationshipType.id, partyIdToId:partyRelationshipInstance?.partyIdTo.id, partyIdFromId:partyRelationshipInstance?.partyIdFrom.id, roleTypeCodeFromId:partyRelationshipInstance?.roleTypeCodeFrom.id, roleTypeCodeToId:partyRelationshipInstance?.roleTypeCodeTo.id ]">${partyRelationshipInstance?.partyRelationshipType}</g:link> </td>

						<td>${partyRelationshipInstance?.partyIdFrom}</td>

						<td>${partyRelationshipInstance?.roleTypeCodeFrom}</td>

						<td>${partyRelationshipInstance?.partyIdTo}</td>

						<td>${partyRelationshipInstance?.roleTypeCodeTo}</td>

						<td>${fieldValue(bean:partyRelationshipInstance,
						field:'statusCode')}</td>

						<td>${fieldValue(bean:partyRelationshipInstance,
						field:'comment')}</td>

					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
	<div class="paginateButtons"><g:paginate total="${PartyRelationship.count()}" /></div>
	<div class="buttons">
		<g:form>
			<span class="button"><g:actionSubmit class="create" value="Create PartyRelationship" action="create" /></span>
		</g:form>
	</div>
</div>

</body>
</html>
