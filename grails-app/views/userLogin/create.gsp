<%@page import="net.transitionmanager.domain.RoleType" %>
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
									<input type="text" maxlength="50" onkeyup="PasswordValidation.checkPassword($('#passwordId')[0])" id="username" name="username" value="${personInstance?.email}" autocomplete="off" />
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
									<input type="text" id="emailInputId" name="email" value="${personInstance?.email}" autocomplete="off" />
									<span id="emailDisplayId" style="display:none;">${personInstance?.email}</span>
								</td>
							</tr>
							<tr>
								<td valign="top" class="name">
									<label for="isLocal">Local Account:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'isLocal','errors')}">
									<input type="checkbox" id="isLocal" name="isLocal" value="true" ${(userLoginInstance?.isLocal) ? 'checked="checked"' : ''}
										onchange="togglePasswordEditFields( $(this) )"/>
								</td>
							</tr>
							<tr class="prop passwordsEditFields">
								<td valign="top" class="name">
									<label for="forcePasswordChange">Force password change:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'forcePasswordChange','errors')}">
									<input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="Y" />
								</td>
							</tr>
							<tr class="prop passwordsEditFields">
								<td valign="top" class="name">
									<label for="passwordNeverExpiresId">Password never expires:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'passwordNeverExpires','errors')}">
									<input type="checkbox" id="passwordNeverExpiresId" name="passwordNeverExpires" value="true" />
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
									<label for="passwordExpirationDateId"><g:message code="userLogin.passwordExpires.label" default="Password Expires" />:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'passwordExpirationDate', 'errors')}">
									<script type="text/javascript">
										jQuery(function($){ $("#passwordExpirationDateId").kendoDateTimePicker({ animation: false, format:tdsCommon.kendoDateTimeFormat()}); });
									</script>
									<input type="text" class="dateRange" id="passwordExpirationDateId" name="passwordExpirationDate"
										value="<tds:convertDateTime date="${userLoginInstance?.passwordExpirationDate}" format="12hrs" />"/>
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
									<g:select id="active" name="active" from="${userLoginInstance.constraints.active.inList}" value="${userLoginInstance?.active}" ></g:select>
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
									<g:select id="projectId" name="projectId" from="${projectList}"
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
										<input type="checkbox" name="assignedRole"  value="${role.id}" id="role_${role.id}" <g:if test="${role.level > maxLevel}">disabled</g:if> />
										<label for="role_${role.id}">&nbsp; ${role.help ? role.help : ''} &nbsp;</label>
									</td>
								</tr>
							</g:each>
                            <%--<tr class="prop">
                              <td valign="top" class="value" >
                              	<table style="border: none;">


                              	</table>
                              </td>
                            </tr>
                            --%><%--<tr class="prop">
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

			                                <g:each in="${roleList}" var="availableRoles">

			                                	<option value="${availableRoles.id}">${availableRoles}</option>

			                                </g:each>

		                                </select>

	                                </td>

	                                <td valign="middle" style="vertical-align:middle" >

		                                <span style="white-space: nowrap;height: 100px;" > <a href="#" id="add">

										<img  src="${resource(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;"/>

										</a></span><br/><br/><br/><br/>

		                                <span style="white-space: nowrap;"> <a href="#" id="remove">

		                                <img  src="${resource(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;"/>

		                                </a></span>

	                                </td>

	                                <td valign="top" class="name">

		                                <select name="assignedRole" id="assignedRoleId" multiple="multiple" size="10" style="width: 250px">

			                                <g:if test="${assignedRole}">

				                                <g:each in="${assignedRole}" var="assignedRole">

				                                	<option value="${assignedRole}" selected="selected">${RoleType.get(assignedRole)}</option>

				                                </g:each>

			                                </g:if>

			                                <g:else>

			                                	<option value="USER" selected="selected">${RoleType.get('USER')}</option>

			                                </g:else>

		                                </select>

	                                </td>

                                </tr>

                                </table>

                                </td>


                            </tr> --%>


						</tbody>
					</table>
				</div>
				<div class="buttons">
					<span class="button"><input class="save" type="submit" value="Save" onclick="selectAllAssigned()"/></span>
				</div>
			</g:form>
		</div>
	<script>


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
					$("#emailFieldId").hide();
					$("#emailDisplayId").show();
				} else {
					$me.val(true)
					$(".passwordsEditFields").show();
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
