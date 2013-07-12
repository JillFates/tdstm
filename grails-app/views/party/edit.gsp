

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Edit Party</title>
</head>
<body>
<div class="body">
<h1>Edit Party</h1>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><g:link class="list" action="list">Party List</g:link></span>
<span class="menuButton"><g:link class="create" action="create">Create Party</g:link></span>
</div>
<br>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:form method="post">
	<input type="hidden" name="id" value="${partyInstance?.id}" />
	<div class="dialog">
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top" class="name"><label for="dateCreated">Date
				Created:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:partyInstance,field:'dateCreated','errors')}">
				<g:datePicker name="dateCreated"
					value="${partyInstance?.dateCreated}"></g:datePicker> <g:hasErrors
					bean="${partyInstance}" field="dateCreated">
					<div class="errors"><g:renderErrors bean="${partyInstance}"
						as="list" field="dateCreated" /></div>
				</g:hasErrors></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="lastUpdated">Last
				Updated:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:partyInstance,field:'lastUpdated','errors')}">
				<g:datePicker name="lastUpdated"
					value="${partyInstance?.lastUpdated}" noSelection="['':'']"></g:datePicker>
				<g:hasErrors bean="${partyInstance}" field="lastUpdated">
					<div class="errors"><g:renderErrors bean="${partyInstance}"
						as="list" field="lastUpdated" /></div>
				</g:hasErrors></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="partyType">Party
				Type:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:partyInstance,field:'partyType','errors')}">
				<g:select optionKey="id" from="${PartyType.list()}"
					name="partyType.id" value="${partyInstance?.partyType?.id}"
					noSelection="['null':'']"></g:select> <g:hasErrors
					bean="${partyInstance}" field="partyType">
					<div class="errors"><g:renderErrors bean="${partyInstance}"
						as="list" field="partyType" /></div>
				</g:hasErrors></td>
			</tr>

		</tbody>
	</table>
	</div>
	<div class="buttons">
		<span class="button"><g:actionSubmit class="save" value="Update" /></span>
		<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
	</div>
</g:form></div>
</body>
</html>
