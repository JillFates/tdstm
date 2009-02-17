

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Edit AssetType</title>
</head>
<body>
<div class="nav"><span class="menuButton"><g:link
	class="home" controller="auth" action="home">Home</g:link></span> <span
	class="menuButton"><g:link class="list" action="list">AssetType List</g:link></span>
<span class="menuButton"><g:link class="create" action="create">New AssetType</g:link></span>
</div>
<div class="body">
<h1>Edit AssetType</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:form method="post">
	<input type="hidden" name="assetTypeId" value="${assetTypeInstance?.id}" />
	<div class="dialog">
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top" class="name"><label for="id">Code:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:assetTypeInstance,field:'id','errors')}">
				<input type="text" id="id" name="id"
					value="${fieldValue(bean:assetTypeInstance,field:'id')}" /> <g:hasErrors
					bean="${assetTypeInstance}" field="id">
					<div class="errors"><g:renderErrors
						bean="${assetTypeInstance}" as="list" field="id" /></div>
				</g:hasErrors></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="description">Description:</label>
				</td>
				<td valign="top"
					class="value ${hasErrors(bean:assetTypeInstance,field:'description','errors')}">
				<input type="text" id="description" name="description"
					value="${fieldValue(bean:assetTypeInstance,field:'description')}" />
				<g:hasErrors bean="${assetTypeInstance}" field="description">
					<div class="errors"><g:renderErrors
						bean="${assetTypeInstance}" as="list" field="description" /></div>
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
