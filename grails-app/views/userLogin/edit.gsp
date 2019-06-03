<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
		<title>Edit UserLogin</title>
		<script type="text/javascript">
			var flag
			$().ready(function() {
				var isLocal = ${userLoginInstance.isLocal}
				if (!isLocal) {
					$(".passwordsEditFields").hide();
				}

				$('#add').click(function() {
					updateRole( 'add',$('#availableRoleId').val() );
					flag = !$('#availableRoleId option:selected').remove().appendTo('#assignedRoleId');
					return flag;
				});
				$('#remove').click(function() {
					updateRole( 'remove',$('#assignedRoleId').val() );
					flag = !$('#assignedRoleId option:selected').remove().appendTo('#availableRoleId');
					return flag;
				});

				$('#username').focus();

			});
			function updateRole( action, values ) {
				var personId = $('#person').val();
				if (values) {
					${remoteFunction(controller:'userLogin', action:'addRoles', params:'\'assignedRoleId=\' + values +\'&person=\'+personId +\'&actionType=\'+action')}
				}
			}
			function togglePasswordEditFields($me) {
				var isChecked = $me.is(":checked")
				if (!isChecked) {
					$me.val(false)
					$(".passwordsEditFields").hide();
					$("#emailFieldId").hide();
					$("#emailDisplayId").show();
				} else {
					$me.val(true)
					$(".passwordsEditFields").show();
					$("#emailFieldId").show();
					$("#emailDisplayId").hide();
				}
			}
		</script>
	</head>
	<body>
	<tds:subHeader title="Edit UserLogin" crumbs="['Admin','Client','Users', 'Edit']"/><br/>
	<div class="body">
			<!-- <h1>Edit UserLogin</h1> -->

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<g:form method="post" name="editUserForm" autocomplete="off" useToken="true">
				<input type="hidden" name="id" value="${userLoginInstance?.id}" />
				<input type="hidden" name="companyId" value="${companyId}" />
				<div class="dialog loginView">
					<table>
						<tbody>
						<tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="person">Company:</label>
							</td>
							<td valign="top" class="value">
								${userLoginInstance.person.company}
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="person">Person:</label>
							</td>
							<td valign="top" class="value">
								${userLoginInstance.person}
							</td>
						</tr>

						<tr class="prop requiredField">
							<td valign="top" class="name">
								<label for="username">Username (use email):</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'username','errors')}">
								<g:if test="${isCurrentUserLogin}">
									<input type="text" onkeyup="PasswordValidation.checkPassword($('#passwordId')[0])" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}" readonly />
								</g:if>
								<g:else>
									<input type="text" onkeyup="PasswordValidation.checkPassword($('#passwordId')[0])" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}" />
								</g:else>

								<g:hasErrors bean="${userLoginInstance}" field="username">
									<div class="errors">
										<g:renderErrors bean="${userLoginInstance}" as="list" field="username"/>
									</div>
								</g:hasErrors>
							</td>
						</tr>

						<tr class="prop requiredField">
							<td valign="top" class="name">
								<label for="email">Email:</label>
							</td>
							<td valign="top" class="value">
								<input type="text" id="emailFieldId" name="email" value="${fieldValue(bean:userLoginInstance,field:'person.email')}"  ${!userLoginInstance.isLocal ? 'style="display:none;"' : ''} />
								<span id="emailDisplayId" ${userLoginInstance.isLocal ? 'style="display:none;"' : ''}>${fieldValue(bean:userLoginInstance,field:'person.email')}</span>
							</td>
						</tr>

						<tr>
							<td valign="top" class="name">
								<label for="isLocal">Local Account:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'isLocal','errors')}">
								<input type="checkbox" id="isLocal" name="isLocal" value="${userLoginInstance.isLocal}" ${(userLoginInstance.isLocal) ? 'checked="checked"' : ''}
									onchange="togglePasswordEditFields( $(this) )" onclick='if(this.checked){this.value = true} else {this.value = false }'/>
							</td>
						</tr>

						<tr class="prop passwordsEditFields">
							<td valign="top" class="name">
								<label for="forcePasswordChange">Force password change:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'forcePasswordChange','errors')}">
								<input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="${userLoginInstance.forcePasswordChange}" ${(userLoginInstance.forcePasswordChange=='Y') ? 'checked="checked"' : ''}/>
							</td>
						</tr>

						<tr class="prop passwordsEditFields">
							<td valign="top" class="name">
								<label for="passwordNeverExpiresId">Password never expires:</label>
							</td>

							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'passwordNeverExpires','errors')}">
								<input type="checkbox" id="passwordNeverExpiresId" name="passwordNeverExpires" value="true" ${userLoginInstance.passwordNeverExpires ? 'checked="checked"' : ''}/>
							</td>
						</tr>

						<g:render template="setPasswordFields" model="${[changingPassword:false, minPasswordLength:minPasswordLength, fromDialog:true]}" />


						<tr class="prop">
							<td valign="top" class="name">
								<label for="expiryDate"><g:message code="userLogin.expiryDate.label" default="Expiry Date" />:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'expiryDate', 'errors')}">
								<script type="text/javascript">
									$(document).ready(function(){

										$("#expiryDate").kendoDateTimePicker({format:tdsCommon.kendoDateTimeFormat()});
									});
								</script>
								<input type="text" class="dateRange" id="expiryDate" name="expiryDate"
									value="<tds:convertDateTime date="${userLoginInstance?.expiryDate}"  formate="12hrs" timeZone="${tds.timeZone()}"/>"/>
								<g:hasErrors bean="${userLoginInstance}" field="expiryDate">
									<div class="errors">
										<g:renderErrors bean="${userLoginInstance}" as="list" field="expiryDate"/>
									</div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">
								<label for="passwordExpirationDate"><g:message code="userLogin.passwordExpires.label" default="Password Expires" />:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'passwordExpirationDate', 'errors')}">
								<script type="text/javascript">
									jQuery(function($){ $("#passwordExpirationDate").kendoDateTimePicker({ animation: false, format:tdsCommon.kendoDateTimeFormat()}); });
								</script>
								<input type="text" class="dateRange" id="passwordExpirationDate" name="passwordExpirationDate"
									value="<tds:convertDateTime date="${userLoginInstance?.passwordExpirationDate}" formate="12hrs" />"/>
								<g:hasErrors bean="${userLoginInstance}" field="passwordExpirationDate">
									<div class="errors">
										<g:renderErrors bean="${userLoginInstance}" as="list" field="passwordExpirationDate"/>
									</div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop requiredField">
							<td valign="top" class="name">
								<label for="active">Active:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'active','errors')}">
								<g:select id="active" name="active" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(userLoginInstance.class).active.inList}" value="${userLoginInstance.active}" ></g:select>
								<g:hasErrors bean="${userLoginInstance}" field="active">
									<div class="errors">
										<g:renderErrors bean="${userLoginInstance}" as="list" field="active"/>
									</div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">
								<label for="lockedOutUntilId">Locked Out Until:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'lockedOutUntil', 'errors')}">
								<input type="text" class="dateRange" id="lockedOutUntilId" name="lockedOutUntil" readonly="true"
									value="<tds:convertDateTime date="${userLoginInstance?.lockedOutUntil}" formate="12hrs" />"/>
								<g:hasErrors bean="${userLoginInstance}" field="lockedOutUntil">
									<div class="errors">
										<g:renderErrors bean="${userLoginInstance}" as="list" field="lockedOutUntil"/>
									</div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop requiredField">
							<td valign="top" class="name">
								<label for="active">Project:</label>
							</td>
							<td valign="top" class="value">
								<g:select id="project" name="projectId" from="${projectList}" value="${projectId}" noSelection="${['':'Select a project...']}" optionKey="id" optionValue="name"/>
							</td>
						</tr>
						<g:each in="${roleList}" var="role">
							<tr class="prop">
								<td valign="top" class="name" >
									${role}:
								</td>
								<td valign="top" class="value" >
									<input type="checkbox" id="${role.id}" name="assignedRole"  value="${role.id}" ${assignedRoles.id.contains(role.id) ? 'checked="checked"' : ''}

									<g:if test="${role.level > maxLevel}">disabled</g:if>

									/>
									&nbsp; ${role.help ? role.help : ''} &nbsp;
								</td>
							</tr>
						</g:each>

						<%--
						<tr class="prop">
							<td valign="top" class="value" colspan="2">
								<table style="border: none;">
									<tr>
										<td valign="top" class="name">
											<label >Available Roles:</label>
										</td>
										<td valign="top" class="name">
											<label >&nbsp;</label>
										</td>
										<td valign="top" class="name">
											<label >Assigned Roles:</label>
										</td>
									</tr>
									<tr>
										<td valign="top" class="name">
											<select name="availableRole" id="availableRoleId" multiple="multiple" size="10" style="width: 250px">
												<g:each in="${availableRoles}" var="availableRoles">
													<option value="${availableRoles.id}">${availableRoles}</option>
												</g:each>
											</select>
										</td>
										<td valign="middle" style="vertical-align:middle" >
											<span style="white-space: nowrap;height: 100px;" > <a href="#" id="add">
												<asset:image src="images/right-arrow.png" style="float: left; border: none;"/>
											</a></span><br/><br/><br/><br/>
											<span style="white-space: nowrap;"> <a href="#" id="remove"><asset:image src="images/left-arrow.png" style="float: left; border: none;"/>
											</a></span>
										</td>
										<td valign="top" class="name">
											<select name="assignedRole" id="assignedRoleId" multiple="multiple" size="10" style="width: 250px">
												<g:each in="${assignedRoles}" var="assignedRole">
													<option value="${assignedRole?.id}" selected="selected">${assignedRole}</option>
												</g:each>
											</select>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						--%>
						</tbody>
					</table>
				</div>
				<div class="buttons">
					<span class="button">
						<g:actionSubmit class="save" value="Update" />
					</span>
					<span class="button">
						<input type="button" class="cancel" value="Cancel" id="cancelButtonId" onclick="window.location = contextPath + '/userLogin/show/${userLoginInstance?.id}'"/>
					</span>
				</div>
			</g:form>
		</div>
	<script>

	var form = $("form[name='editUserForm']")[0]
	$(form).submit(function(event){
		var emailValue = $("#emailFieldId").val()
		var errMsg = ""
		if($("#isLocal").is(":checked")){
			if(emailValue){
				if(!tdsCommon.isValidEmail(emailValue)){
					errMsg = "Email address is invalid."
				}
			}else{
				errMsg = "Email address is required!"
			}
		}

    	if(errMsg.length > 0){
    		event.preventDefault()
    		alert(errMsg)
    	}
	})

		$(document).ready(function(){
			togglePasswordEditFields($("#isLocal"))
		})
//					<input class="cancel" onclick="return confirm('Are you sure?');" value="Delete" />
	currentMenuId = "#adminMenu";
	$('.menu-list-users').addClass('active');
	$('.menu-parent-admin').addClass('active');

	function deleteRole(id){
		var values = $("#"+id).val()
		var personId =  $('#person').val();
		var action = 'remove'
		if(!$("#"+id).is(':checked')){
			${remoteFunction(controller:'userLogin', action:'addRoles', params:'\'assignedRoleId=\' + values +\'&person=\'+personId +\'&actionType=\'+action')}
		}
	}

	</script>
	</body>
</html>
