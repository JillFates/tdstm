

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Show Person</title>
</head>
<body>
<div class="body">
<h1>Show Person</h1>

	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	<span class="menuButton"><g:link class="list" action="list">Person List</g:link></span>
	<jsec:hasRole name="ADMIN">
	<span class="menuButton"><g:link class="create" action="create">New Person</g:link></span>
	</jsec:hasRole>
	</div>
 <br>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<tbody>


		<tr class="prop">
			<td valign="top" class="name">Id:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'id')}</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Date Created:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'dateCreated')}</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Last Updated:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'lastUpdated')}</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Party Type:</td>

			<td valign="top" class="value"><jsec:hasRole name="ADMIN"><g:link controller="partyType"
				action="show" id="${personInstance?.partyType?.id}">${personInstance?.partyType?.encodeAsHTML()}</g:link>
				</jsec:hasRole>
				<jsec:lacksRole name="ADMIN">${personInstance?.partyType?.encodeAsHTML()}</jsec:lacksRole>
				</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">First Name:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'firstName')}</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Last Name:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'lastName')}</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Nick Name:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'nickName')}</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Active:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'active')}</td>

		</tr>

	</tbody>
</table>
</div>
<jsec:hasRole name="ADMIN">
<div class="buttons"><g:form>
	<input type="hidden" name="id" value="${personInstance?.id}" />
	<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
	<span class="button"><g:actionSubmit class="delete"
		onclick="return confirm('Are you sure?');" value="Delete" /></span>
</g:form></div>
</jsec:hasRole>
</div>
</body>
</html>
