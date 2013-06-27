

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Staff</title>
</head>
<body>

<div class="body">

<h1>Staff</h1>
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
			<td valign="top" class="name">Middle Name:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'middleName')}</td>

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

			<td valign="top" class="value"><tds:convertDateTime date="${personInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>

		</tr>

		<tr class="prop">
			<td valign="top" class="name">Last Updated:</td>

			<td valign="top" class="value"><tds:convertDateTime date="${personInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>

		</tr>
		
		<tr class="prop">
			<td valign="top" class="name">Model Score:</td>

			<td valign="top" class="value">${fieldValue(bean:personInstance,
			field:'modelScore')}</td>

		</tr>

	</tbody>
</table>
</div>
<tds:hasPermission permission='PersonEditView'>
<div class="buttons"><g:form>
	<input type="hidden" name="id" value="${personInstance?.id}" />
	<input type="hidden" name="companyId" value="${companyId}" />
	
	<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
	<span class="button"><g:actionSubmit class="delete"
		onclick="return confirm('Are you sure?');" value="Delete" /></span>
</g:form></div>
</tds:hasPermission>
</div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
</body>
</html>
