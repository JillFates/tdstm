<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<title>UserLogin</title>
		
		<g:render template="../layouts/angularResources" />
		<script type="text/javascript" src="${resource(dir:'components/admin',file:'adminController.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/admin',file:'adminService.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/admin',file:'unlockAccountDirective.js')}"></script>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<jqgrid:resources />
		<g:javascript src="admin.js" />
		<g:javascript src="projectStaff.js" />
		<g:javascript src="person.js" />
		<g:javascript src="jqgrid-support.js" />
		<g:javascript src="asset.comment.js" />
	</head>
	<body>
		
		<div class="body">
			<h1>UserLogin</h1>
			
			<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
				
				<span class="menuButton"><g:link class="list" action="list" id="${companyId}"  params="[filter:true]">UserLogin List</g:link></span>
				
			</div>
			
			<br/>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="dialog loginView" ng-app="tdsAdmin" ng-controller="tds.admin.controller.MainController as admin">
				<table>
					<tbody>
						
						<tr class="prop">
							<td valign="top" class="name">Company:</td>
							<td nowrap="nowrap" valign="top" class="value">${userLoginInstance?.person?.company}</td>
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name">Person:</td>
							<td nowrap="nowrap" valign="top" class="value"><g:link controller="person" action="show" id="${userLoginInstance?.person?.id}">${userLoginInstance?.person?.encodeAsHTML()}</g:link></td>
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name">Username:</td>
							<td valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'username')}</td>
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name">Email:</td>
							<td valign="top" class="value">${userLoginInstance?.person?.email ?: '<em>Required Email is missing</em>'}</td>
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name">Local account:</td>
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'isLocal','errors')}">
								<input type="checkbox" id="isLocal" name="isLocal" value="${userLoginInstance.isLocal}" disabled="disabled" ${(userLoginInstance.isLocal) ? 'checked="checked"' : ''}/>
							</td>
						</tr>
						
						<tr class="prop passwordsEditFields">
							<td valign="top" class="name">
								<label for="forcePasswordChange">Force password change:</label>
							</td>
							
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'forcePasswordChange','errors')}">
								<input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="${userLoginInstance.forcePasswordChange}" disabled="disabled" ${(userLoginInstance.forcePasswordChange=='Y') ? 'checked="checked"' : ''}/>
							</td>
						</tr>
						
						<tr class="prop passwordsEditFields">
							<td valign="top" class="name">
								<label for="passwordNeverExpiresId">Password never expires:</label>
							</td>
							
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'passwordNeverExpires','errors')}">
								<input type="checkbox" id="passwordNeverExpiresId" name="passwordNeverExpires" value="${userLoginInstance.passwordNeverExpires}" disabled="disabled" ${userLoginInstance.passwordNeverExpires ? 'checked="checked"' : ''}/>
							</td>
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name">Active:</td>
							<td nowrap="nowrap" valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'active')}</td>
						</tr>
						
						<tr class="prop passwordsEditFields">
							<td valign="top" class="name">Failed Logins:</td>
							<td nowrap="nowrap" valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'failedLoginAttempts')}</td>
						</tr>
						
						<tr class="prop passwordsEditFields">
							<td valign="top" class="name">Locked Out Until:</td>
							<td nowrap="nowrap" valign="middle" class="value">
								<g:if test="${userLoginInstance?.lockedOutUntil}">
									<tds:convertDateTime date="${userLoginInstance?.lockedOutUntil}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
									<input tm-unlock-account type="button" id="unlockButtonId" value="Unlock" cellValue='${cellValue}' refreshPage="true"/>
								</g:if>
								<g:else>Not Locked Out</g:else>
							</td>
						</tr>
						
						<tr class="prop passwordsEditFields">
							<td valign="top" class="name">Password Expires:</td>
							<td nowrap="nowrap" valign="top" class="value">
								<g:if test="${userLoginInstance?.passwordExpirationDate}">
									<tds:convertDateTime date="${userLoginInstance?.passwordExpirationDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
								</g:if>
								<g:else>Never</g:else>
							</td>
						</tr>
						
						<g:each in="${roleList}" var="role">
							<tr class="prop">
								<td valign="top" class="name" >
									${role}:
								</td>
								<td valign="top" class="value" >
									<input type="checkbox" id="${role.id}" name="assignedRole"  value="${role.id}" disabled="disabled" ${assignedRoles.id.contains(role.id) ? 'checked="checked"' : ''} />
									&nbsp; ${role.help ? role.help : ''}
								</td>
							</tr>
						</g:each>
						
						<tr class="prop passwordsEditFields">
							<td valign="top" class="name"><g:message code="userLogin.expiryDate.label" default="Expiry Date" />:</td>
							
							<td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.expiryDate}" /></td>
							
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Created Date:</td>
							
							<td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.createdDate}" /></td>
							
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name">Last Modified:</td>
							
							<td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.lastModified}" /></td>
							
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name">Last Login:</td>
							
							<td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.lastLogin}" /></td>
							
						</tr>
						
					</tbody>
				</table>
			</div>
			<tds:hasPermission permission='EditUserLogin'>
				<div class="buttons">
					<g:form>
						<input type="hidden" name="id" value="${userLoginInstance?.id}" />
						<input type="hidden" name="companyId" value="${companyId}" />
						<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
						<tds:hasPermission permission='UserLoginDelete'>
							<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
						</tds:hasPermission>
						<tds:hasPermission permission='EditUserLogin'>
							<span class="button"><input type="button" value="Send Password Reset" id="resetPassword"  <g:if test="${!canResetPasswordByAdmin}"> class="save disableButton" disabled</g:if> <g:else>class="save"</g:else> /></span>
						</tds:hasPermission>
					</g:form>
				</div>
			</tds:hasPermission>
		</div>
		<script>
			currentMenuId = "#adminMenu";
			$("#adminMenuId a").css('background-color','#003366')
			$(document).ready(function(){

				var isChecked = $("#isLocal").is(":checked")
				if (!isChecked) {
					$(".passwordsEditFields").hide();
				} else {
					$(".passwordsEditFields").show();
				}

				$("#resetPassword").click(function(){
					$(this).prop('disabled', true)
					$(this).toggleClass("disableButton")
					var id = $("[name=id]").val()
					$.ajax({
						url: "../sendPasswordReset?id="+id,
						type:'post',
						success: function(data){
							if(data && data.success){
								alert("A password reset email notification has been sent to the user.")
							}else{
								alert("There was an error trying to reset the user's password. Please, contact your System Administrator.")
							}
							$("#resetPassword").removeProp('disabled')
							$("#resetPassword").toggleClass("disableButton")
						},
						error: function(){
							alert("There was an error trying to reset the user's password. Please, contact your System Administrator.")
						}
					})
				})
			});
		</script>
	</body>
</html>
