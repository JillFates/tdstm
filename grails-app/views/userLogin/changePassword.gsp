<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<title>Change Password</title>
	</head>
	<body>
		<div class="body">
			<h1>Change Password</h1>
			<br />
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<g:form method="post" name="editUserForm" autocomplete="off">
				<input type="hidden" name="id" value="${userLoginInstance?.id}" />
				<input type="hidden" name="companyId" value="${companyId}" />
				<div class="dialog loginView">
					<table>
						<tbody>                        
							<tr class="prop" style="display:none;">
								<td valign="top" class="name">
									<label for="username"></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'username','errors')}">
									<input type="text" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}"/>
									<g:hasErrors bean="${userLoginInstance}" field="username">
										<div class="errors">
											<g:renderErrors bean="${userLoginInstance}" as="list" field="username"/>
										</div>
									</g:hasErrors>
								</td>
							</tr>
							
							<g:render template="setPasswordFields" model="${[changingPassword:false, minPasswordLength:minPasswordLength]}" />
							
						</tbody>
					</table>
				</div>
				<div class="buttons">
					<span class="button"><g:actionSubmit class="save" value="Save Password" action="updatePassword"/></span>
				</div>
			</g:form>
		</div>
	</body>
</html>
