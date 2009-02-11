

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Create Party</title>
</head>
<body>
<div class="nav"><span class="menuButton"><g:link
	class="home" controller="auth" action="home">Home</g:link></span> <span
	class="menuButton"><g:link class="list" action="list">Party List</g:link></span>
</div>
<div class="body">
<h1>Create Party</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:form action="save" method="post">
	<div class="dialog">
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top" class="name"><label for="partyName">Party
				Name:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:partyInstance,field:'partyName','errors')}">
				<input type="text" id="partyName" name="partyName"
					value="${fieldValue(bean:partyInstance,field:'partyName')}" maxlength="64"/> 
				<g:hasErrors bean="${partyInstance}" field="partyName">
					<div class="errors"><g:renderErrors bean="${partyInstance}"	as="list" field="partyName"/></div>
				</g:hasErrors></td>
			</tr>

		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button"><input
		class="save" type="submit" value="Create" /></span></div>
</g:form></div>
</body>
</html>
