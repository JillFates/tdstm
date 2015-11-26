<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<title>Edit UserLogin</title>
		<script type="text/javascript"> 
			var flag 
			$().ready(function() {
				var isLocal = ${userLoginInstance.isLocal}
				if (isLocal) {
					$(".passwordsEditFields").css('display', 'none');
				} else {
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
				} else {
					$me.val(true)
					$(".passwordsEditFields").show();
				}
			}
		</script>
	</head>
    <body>
        <div class="body">
            <h1>Edit UserLogin</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="create" action="create" params="[companyId:companyId]">Create UserLogin</g:link></span>
        	</div>
        	<br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" name="editUserForm" autocomplete="off">
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
									${userLoginInstance.person.lastNameFirst}
								</td>
							</tr> 
					
							<tr class="prop">
								<td valign="top" class="name">
									<label for="username"><b>Username (use email):&nbsp;<span style="color: red">*</span></b></label>
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
							<tr>
								<td valign="top" class="name">
									<label for="isLocal">Local account:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'isLocal','errors')}">
                                    <input type="checkbox" id="isLocal" name="isLocal" value="${userLoginInstance.isLocal}" ${(userLoginInstance.isLocal)?'checked="checked"':''}  
                                    onchange="togglePasswordEditFields( $(this) )" />
                                </td>
                            </tr>
							<tr class="prop passwordsEditFields">
                                <td valign="top" class="name">
                                    <label for="forcePasswordChange">Force password change:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'forcePasswordChange','errors')}">
                                    <input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="${userLoginInstance.forcePasswordChange}" ${(userLoginInstance.forcePasswordChange=='Y')?'checked="checked"':''}/>
                                </td>
                            </tr> 
                        	<tr class='passwordsEditFields'>
								<td>
									Hide password:
								</td>
								<td>
									<input type="checkbox" onchange="togglePasswordVisibility(this)" id="showPasswordEditId"/>
								</td>
							</tr>
                            <tr class="prop passwordsEditFields">
                                <td valign="top" class="name">
                                    <label for="password">Password:&nbsp;</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'password','errors')}">
                                    <input type="text" id="password" onkeyup="checkPassword(this)" name="password" value=""/>
								
                                <g:hasErrors bean="${userLoginInstance}" field="password">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                            <tr class="passwordsEditFields">
								<td>
									Requirements:
								</td>
								<td>
									<em id="usernameRequirementId">Password must not contain the username</em><br/>
									<em id="lengthRequirementId">Password must be at least 8 characters long</em><br/>
									<b id="passwordRequirementsId">Password must contain at least 3 of these requirements: </b><br/>
									<ul>
										<li><em id="uppercaseRequirementId">Uppercase characters</em></li>
										<li><em id="lowercaseRequirementId">Lowercase characters</em></li>
										<li><em id="numericRequirementId">Numeric characters</em></li>
										<li><em id="symbolRequirementId">Nonalphanumeric characters</em></li>
									</ul>
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="passwordExpirationDate">Password Expires:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'passwordExpirationDate', 'errors')}">
									<script type="text/javascript">
										$(document).ready(function(){
											$("#passwordExpirationDate").datetimepicker();
										});
									</script>
									<input type="text" class="dateRange" id="passwordExpirationDate" name="passwordExpirationDate"
										value="<tds:convertDateTime date="${userLoginInstance?.passwordExpirationDate}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"/>
									<g:hasErrors bean="${userLoginInstance}" field="passwordExpirationDate">
										<div class="errors">
											<g:renderErrors bean="${userLoginInstance}" as="list" field="passwordExpirationDate"/>
										</div>
									</g:hasErrors>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="active"><b>Active:&nbsp;<span style="color: red">*</span></b></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'active','errors')}">
									<g:select id="active" name="active" from="${userLoginInstance.constraints.active.inList}" value="${userLoginInstance.active}" ></g:select>
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
										value="<tds:convertDateTime date="${userLoginInstance?.lockedOutUntil}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"/>
									<g:hasErrors bean="${userLoginInstance}" field="lockedOutUntil">
										<div class="errors">
											<g:renderErrors bean="${userLoginInstance}" as="list" field="lockedOutUntil"/>
										</div>
									</g:hasErrors>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="active"><b>Project:&nbsp;<span style="color: red">*</span></b></label>
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
                           <%-- <tr class="prop">
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
										<img  src="${resource(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;"/>
										</a></span><br/><br/><br/><br/>
		                                <span style="white-space: nowrap;"> <a href="#" id="remove"><img  src="${resource(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;"/>
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
                        --%></tbody>
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
//					<input class="cancel" onclick="return confirm('Are you sure?');" value="Delete" />
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
	
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
