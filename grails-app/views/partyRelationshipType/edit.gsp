

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Edit PartyRelationshipType</title>
</head>
<body>
<div class="body">
<h1>Edit PartyRelationshipType</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:form method="post">
	<input type="hidden" name="partyRelTypeId"
		value="${partyRelationshipTypeInstance?.id}" />
	<div class="dialog">
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top" class="name"><label for="id">Code:</label></td>
				<td valign="top" class="value">${fieldValue(bean:partyRelationshipTypeInstance,field:'id')}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="description">Description:</label>
				</td>
				<td valign="top"
					class="value ${hasErrors(bean:partyRelationshipTypeInstance,field:'description','errors')}">
				<input type="text" id="description" name="description"
					value="${fieldValue(bean:partyRelationshipTypeInstance,field:'description')}" />
				<g:hasErrors bean="${partyRelationshipTypeInstance}"
					field="description">
					<div class="errors"><g:renderErrors
						bean="${partyRelationshipTypeInstance}" as="list"
						field="description" /></div>
				</g:hasErrors></td>
			</tr>

		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button"><g:actionSubmit
		class="save" value="Update" /></span> <span class="button"><g:actionSubmit
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span></div>
</g:form></div>
</body>
</html>
