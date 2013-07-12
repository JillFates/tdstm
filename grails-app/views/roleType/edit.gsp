<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Edit RoleType</title>
</head>
<body>
<div class="body">
<h1>Edit RoleType</h1>
 <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list">RoleType List</g:link></span>
	             <tds:hasPermission permission='CreateRoleType'>
	            	<span class="menuButton"><g:link class="create" action="create">Create RoleType</g:link></span>
	            </tds:hasPermission>
        	</div>
        	<br/>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:form method="post">
	<input type="hidden" name="roleTypeId" value="${roleTypeInstance?.id}" />
	<div class="dialog">
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top" class="name"><label for="id">Code:</label></td>
				<td valign="top"
					class="value">${fieldValue(bean:roleTypeInstance,field:'id')}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="description">Description:</label>
				</td>
				<td valign="top"
					class="value ${hasErrors(bean:roleTypeInstance,field:'description','errors')}">
				<input type="text" id="description" name="description"
					value="${fieldValue(bean:roleTypeInstance,field:'description')}" />
				<g:hasErrors bean="${roleTypeInstance}" field="description">
					<div class="errors"><g:renderErrors
						bean="${roleTypeInstance}" as="list" field="description" /></div>
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
