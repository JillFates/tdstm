<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Edit RoleType</title>
</head>
<body>
	<tds:subHeader title="Edit RoleType" crumbs="['Admin','Portal','Role Type','Edit']"/> <br/>
<div class="body">
 <div class="nav" style="border: 1px solid #CCCCCC; height: 24px">
	            <span class="menuButton"><g:link class="list" action="list">RoleType List</g:link></span>
	             <tds:hasPermission permission="${Permission.RoleTypeCreate}">
	            	<span class="menuButton"><g:link class="create" action="create">Create RoleType</g:link></span>
	            </tds:hasPermission>
        	</div>
        	<br/>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:form method="post">
	<input type="hidden" name="id" value="${roleTypeInstance?.id}" />
	<div class="dialog">
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top" class="name"><label for="id">Type:</label></td>
				<td valign="top"
					class="value">${fieldValue(bean:roleTypeInstance,field:'type')}</td>
			</tr>


			<g:if test="${roleTypeInstance.type == 'SECURITY'}">

			<tr class="prop">
				<td valign="top" class="name"><label for="id">Level:</label></td>
				<td valign="top"class="value ${hasErrors(bean:roleTypeInstance,field:'level','errors')}">
					<input type="text" id="level" name="level"
					value="${fieldValue(bean:roleTypeInstance,field:'level')}" />
					<g:hasErrors bean="${roleTypeInstance}" field="level">
                    	<div class="errors">
                    		<g:renderErrors bean="${roleTypeInstance}" as="list" field="level" />
                    	</div>
                	</g:hasErrors>
				</td>
			</tr>
			</g:if>

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
			<tr class="prop">
				<td valign="top" class="name"><label for="help">Help:</label>
				</td>
				<td valign="top"
					class="value ${hasErrors(bean:roleTypeInstance,field:'help','errors')}">
				<input type="text" id="help" name="help"
					value="${fieldValue(bean:roleTypeInstance,field:'help')}" />
				<g:hasErrors bean="${roleTypeInstance}" field="help">
					<div class="errors"><g:renderErrors
						bean="${roleTypeInstance}" as="list" field="help" /></div>
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
