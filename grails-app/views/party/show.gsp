<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Show Party</title>
</head>
<body>
<div class="body">
	<h1>Show Party</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div class="dialog">
		<div class="nav" style="border: 1px solid #CCCCCC; height: 11px"><span
			class="menuButton"><g:link class="list" action="list">Party List</g:link></span>
		<tds:hasPermission permission='PartyCreateView'>
			<span class="menuButton"><g:link class="create" action="create">Create Party</g:link></span>
		</tds:hasPermission></div>
		<br>
		<table>
			<tbody>


				<tr class="prop">
					<td valign="top" class="name">Id:</td>

					<td valign="top" class="value">${fieldValue(bean:partyInstance,
					field:'id')}</td>

				</tr>

				<tr class="prop">
					<td valign="top" class="name">Date Created:</td>

					<td valign="top" class="value">${fieldValue(bean:partyInstance,
					field:'dateCreated')}</td>

				</tr>

				<tr class="prop">
					<td valign="top" class="name">Last Updated:</td>

					<td valign="top" class="value">${fieldValue(bean:partyInstance,
					field:'lastUpdated')}</td>

				</tr>

				<tr class="prop">
					<td valign="top" class="name">Party Type:</td>

					<td valign="top" class="value"><tds:hasPermission permission='PartyTypeShowView'><g:link controller="partyType"
						action="show" id="${partyInstance?.partyType?.id}">${partyInstance?.partyType?.encodeAsHTML()}</g:link></tds:hasPermission>
					<tds:hasPermission permission='PartyShowView'>${partyInstance?.partyType?.encodeAsHTML()}</tds:hasPermission>
					</td>

				</tr>

			</tbody>
		</table>
	</div>
	<tds:hasPermission permission='PartyEditView'>
		<div class="buttons">
			<g:form>
				<input type="hidden" name="id" value="${partyInstance?.id}" />
				<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
				<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
			</g:form>
		</div>
	</tds:hasPermission>
</div>
</body>
</html>