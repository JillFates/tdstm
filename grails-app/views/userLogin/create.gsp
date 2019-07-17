<%@page import="net.transitionmanager.security.RoleType" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
		<title>Create UserLogin</title>

		<script type="text/javascript">

			$().ready(function() {

				$('#add').click(function() {
					return !$('#availableRoleId option:selected').remove().appendTo('#assignedRoleId');
				});

				$('#remove').click(function() {
					return !$('#assignedRoleId option:selected').remove().appendTo('#availableRoleId');
				});

				$('#username').focus();

			});

			function selectAllAssigned(){

				$('#assignedRoleId').each(function(){
					$("#assignedRoleId option").attr("selected","selected");
				});

			}

			function togglePasswordFields($me) {
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

		<tds:subHeader title="Create UserLogin" crumbs="['Admin','Client','Users', 'Create']"/><br/>
		<div class="body">

			<div class="nav" style="border: 1px solid #CCCCCC; height: 23px">
				<span class="menuButton"><g:link class="list" action="list" id="${companyId}"  params="[filter:true]">UserLogin List</g:link></span>
			</div>

			<br/>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<g:form action="save" method="post" name="createUserForm" autocomplete="off" useToken="true">
				<div class="dialog loginView">
					<table>
						<tbody>
							<tr>
								<td colspan="2">
									<div class="required"> Fields marked ( * ) are mandatory </div>
									<input name="companyId" type="hidden" value="${companyId}" />
									<input name="personId" type="hidden" value="${personInstance?.id}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="person">Company:</label>
								</td>
								<td valign="top" class="value">
									${personInstance?.company}
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="person">Person:</label>
								</td>
								<td valign="top" class="value">
									${personInstance}
								</td>
							</tr>

							<tr class="prop requiredField">
								<td valign="top" class="name">
									<label for="username">Username (use email):</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'username','errors')}">
									<input class="requiredInput" type="text" maxlength="50" onkeyup="PasswordValidation.checkPassword($('#passwordId')[0])" id="username" name="username" value="${username ? username : personInstance?.email}" autocomplete="off" />
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
									<input class="requiredInput" type="text" id="emailInputId" name="email" value="${email ? email : personInstance?.email}" autocomplete="off" />
									<span id="emailDisplayId" style="display:none;">${personInstance?.email ? personInstance.email : email}</span>
								</td>
							</tr>
							<tr>
								<td valign="top" class="name">
									<label for="isLocal">Local Account:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'isLocal','errors')}">
									<input type="checkbox" id="isLocal" name="isLocal" value="true" ${isLocal == "true"? 'checked="checked"' : ''}
										onchange="togglePasswordEditFields( $(this) )"/>
								</td>
							</tr>
							<tr class="prop passwordsEditFields">
								<td valign="top" class="name">
									<label for="forcePasswordChange">Force password change:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'forcePasswordChange','errors')}">
									<input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="true" ${forcePasswordChange == 'true' ? 'checked="checked"' : ''} />
								</td>
							</tr>
							<tr class="prop passwordsEditFields">
								<td valign="top" class="name">
									<label for="passwordNeverExpiresId">Password never expires:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'passwordNeverExpires','errors')}">
									<input type="checkbox" id="passwordNeverExpiresId" name="passwordNeverExpires" value="true" ${passwordNeverExpires == 'true' ? 'checked="checked"' : ''} />
								</td>
							</tr>

							<g:render template="setPasswordFields" model="${[changingPassword:false, minPasswordLength:minPasswordLength, fromDialog:true]}" />

							<tr class="prop">
								<td valign="top" class="name">
									<label for="expiryDate"><g:message code="userLogin.expiryDate.label" default="Expiry Date" />:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'expiryDate', 'errors')}">
									<script type="text/javascript">
										jQuery(function($){ $("#expiryDate").kendoDateTimePicker({ animation: false, format:tdsCommon.kendoDateTimeFormat()}); });
									</script>
									<g:if test="${expiryDate}">
										<input type="text" class="dateRange" id="expiryDate" name="expiryDate"
											   value="${URLDecoder.decode(expiryDate,'UTF-8')}"/>
										<g:hasErrors bean="${params}" field="expiryDate">
											<div class="errors">
												<g:renderErrors bean="${params}" as="list" field="expiryDate"/>
											</div>
										</g:hasErrors>
									</g:if>
									<g:else>
										<input type="text" class="dateRange" id="expiryDate" name="expiryDate"
											   value="<tds:convertDateTime date="${userLoginInstance?.expiryDate}"  formate="12hrs" timeZone="${tds.timeZone()}"/>"/>
										<g:hasErrors bean="${userLoginInstance}" field="expiryDate">
											<div class="errors">
												<g:renderErrors bean="${userLoginInstance}" as="list" field="expiryDate"/>
											</div>
										</g:hasErrors>
									</g:else>
								</td>
							</tr>
							<tr class="prop passwordExpiration">
								<td valign="top" class="name">
									<label for="passwordExpirationDateId"><g:message code="userLogin.passwordExpires.label" default="Password Expires" />:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'passwordExpirationDate', 'errors')}">
									<script type="text/javascript">
										jQuery(function($){ $("#passwordExpirationDateId").kendoDateTimePicker({ animation: false, format:tdsCommon.kendoDateTimeFormat()}); });
									</script>
									<g:if test="${passwordExpirationDate}">
										<input type="text" class="dateRange" id="passwordExpirationDateId" name="passwordExpirationDate"
											   value="${URLDecoder.decode(passwordExpirationDate,'UTF-8')}"/>
									</g:if>
									<g:else>
										<input type="text" class="dateRange" id="passwordExpirationDateId" name="passwordExpirationDate"
											   value="<tds:convertDateTime date="${userLoginInstance?.passwordExpirationDate}"  formate="12hrs" timeZone="${tds.timeZone()}"/>"/>
										<g:hasErrors bean="${userLoginInstance}" field="passwordExpirationDate">
											<div class="errors">
												<g:renderErrors bean="${userLoginInstance}" as="list" field="passwordExpirationDate"/>
											</div>
										</g:hasErrors>
									</g:else>
								</td>
							</tr>

							<tr class="prop requiredField">
								<td valign="top" class="name">
									<label for="active">Active:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'active','errors')}">
									<g:select id="active" name="active" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(userLoginInstance.class).active.inList}" value="${active ? params.active : userLoginInstance?.active}" ></g:select>
									<g:hasErrors bean="${userLoginInstance}" field="active">
										<div class="errors">
											<g:renderErrors bean="${userLoginInstance}" as="list" field="active"/>
										</div>
									</g:hasErrors>
								</td>
							</tr>

							<tr class="prop requiredField">
								<td valign="top" class="name">
									<label for="active">Project:</label>
								</td>
								<td valign="top" class="value">
									<g:select class="requiredInput" id="projectId" name="projectId" from="${projectList}"
										value="${projectId}"
										noSelection="${['':'Select a project...']}"
										optionKey="id" optionValue="name"/>
								</td>
							</tr>
							<g:each in="${roleList}" var="role">
								<tr class="prop">
									<td valign="top" class="name" >
										<label for="role_${role.id}">${role}:</label>
									</td>
									<td valign="top" class="value" >
										<input type="checkbox" name="assignedRole"  value="${role.id}" id="role_${role.id}" <g:if test="${role.level > maxLevel}">disabled</g:if> <g:else>${params[role.id.replaceAll('_','')] == 'true' ? 'checked="checked"' : ''}</g:else> />
										<label for="role_${role.id}">&nbsp; ${role.help ? role.help : ''} &nbsp;</label>
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div>
				<div class="buttons">
					<span class="button"><input class="save disableButton" type="submit" value="Save" disabled="disabled" onclick="selectAllAssigned()"/></span>
				</div>
			</g:form>
		</div>
	<script>

        function checkIfDisableSave() {
            var empty = false;
	        var isLocal = $("#isLocal").is(":checked");
	        //debugger;
            $('.requiredInput').each(function () {
                if ($(this).val().length == 0) {
	                empty = true;
                }
            });

            var passwordsMatch = $('#passwordId').val() == $('#confirmPasswordId').val();

            if ((!isLocal && empty) || (isLocal && (empty || !passwordsMatch))) {
                $('.save').attr('disabled', 'disabled');
                $('.save').addClass('disableButton');
            } else {
                $('.save').removeAttr('disabled');
                $('.save').removeClass('disableButton');
            }
        }

        $(document).ready(function() {
            checkIfDisableSave();
            $('.requiredInput').change(function() {
                checkIfDisableSave();
            });
            $('.requiredInput').on('input', function() {
                checkIfDisableSave();
            });
            $('.passwordField').on('input', function() {
                checkIfDisableSave();
            });
        });

		var form = $("form[name='createUserForm']")[0]
		$(form).submit(function(event){
			var emailValue = $("#emailInputId").val()
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


		currentMenuId = "#adminMenu";
		$('.menu-list-users').addClass('active');
		$('.menu-parent-admin').addClass('active');

		function togglePasswordEditFields($me) {
				var isChecked = $me.is(":checked")
				if (!isChecked) {
					$me.val(false)
					$(".passwordsEditFields").hide();
					$(".passwordExpiration").hide();
					$("#emailFieldId").hide();
					$("#emailDisplayId").show();
				} else {
					$me.val(true)
					$(".passwordsEditFields").show();
					$(".passwordExpiration").show();
					$("#emailFieldId").show();
					$("#emailDisplayId").hide();
				}
		}

		$(document).ready(function(){
			togglePasswordEditFields($("#isLocal"))

		})
	</script>
	</body>
</html>
