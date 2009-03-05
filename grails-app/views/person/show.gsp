

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Show Staff</title>
</head>
<body>
<div class="menu2">
<ul>
	<li><g:link class="home" controller="person" id="${companyId}">Staff</g:link></li>
	<li><a href="#">Applications </a></li>
	<li><a href="#">Locations </a></li>
	<li><a href="#">Rooms </a></li>
</ul>
</div>
<div class="body">

<h1>Show Staff</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<tbody>

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
			<td valign="top" class="name">Title:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'title')}</td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Active:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'active')}</td>

		</tr>
		<tr class="prop">
			<td valign="top" class="name">Date Created:</td>

			<td valign="top" class="value"><my:convertDateTime date="${personInstance?.dateCreated}"/></td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Last Updated:</td>

			<td valign="top" class="value"><my:convertDateTime date="${personInstance?.lastUpdated}" /></td>

		</tr>

	</tbody>
</table>
</div>
<jsec:hasRole name="ADMIN">
<div class="buttons"><g:form>
	<input type="hidden" name="id" value="${personInstance?.id}" />
	<input type="hidden" name="companyId" value="${companyId}" />
	
	<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
	<span class="button"><g:actionSubmit class="delete"
		onclick="return confirm('Are you sure?');" value="Delete" /></span>
</g:form></div>
</jsec:hasRole>
</div>
</body>
</html>
