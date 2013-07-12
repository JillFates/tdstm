<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Create PartyRelationshipType</title>
</head>
<body>
<div class="body">
	<h1>Create PartyRelationshipType</h1>
	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		<span class="menuButton"><g:link class="list" action="list">PartyRelationshipType List</g:link></span>
	</div>
	<br/>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:form action="save" method="post">
		<div class="dialog">
			<table>
				<tbody>
					<tr>
						<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label for="id"><b>Code:&nbsp;<span style="color: red">*</span></b></label></td>
						<td valign="top"
							class="value ${hasErrors(bean:partyRelationshipTypeInstance,field:'id','errors')}">
						<input type="text" id="id" name="id"
							value="${fieldValue(bean:partyRelationshipTypeInstance,field:'id')}" />
						<g:hasErrors bean="${partyRelationshipTypeInstance}" field="id">
							<div class="errors"><g:renderErrors
								bean="${partyRelationshipTypeInstance}" as="list" field="id" /></div>
						</g:hasErrors></td>
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
		<div class="buttons">
			<span class="button">
				<input class="save" type="submit" value="Save" />
			</span>
		</div>
	</g:form>
</div>
</body>
</html>