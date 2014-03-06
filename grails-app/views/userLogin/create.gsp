<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Create UserLogin</title>

			 <script type="text/javascript">  

			   $().ready(function() {  

			    $('#add').click(function() {  

			     return !$('#availableRoleId option:selected').remove().appendTo('#assignedRoleId');  

			    });  

			    $('#remove').click(function() {  

			     return !$('#assignedRoleId option:selected').remove().appendTo('#availableRoleId');  

			    });  

			   });

			   function selectAllAssigned(){

			   	$('#assignedRoleId').each(function(){  

					$("#assignedRoleId option").attr("selected","selected");  

   				});

			   }
			   
				function togglePasswordFields($me){
					var isChecked = $me.is(":checked")
					if(!isChecked){
						$(".passwordsFields").hide();
					}else{
						$(".passwordsFields").show();
					}
				}
			  </script>          
    </head>
    <body>

    
        <div class="body">
            <h1>Create UserLogin</h1>

            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">

            <span class="menuButton"><g:link class="list" action="list" id="${companyId}"  params="[filter:true]">UserLogin List</g:link></span>

        </div>

        <br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog loginView">
                    <table>
                        <tbody>
	                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> 
							<input name="companyId" type="hidden" value="${companyId}" />
							</td>
							</tr>
                            <tr class="prop">
								<td valign="top" class="name">
        							<label for="person"><b>Person:&nbsp;<span style="color: red">*</span></b></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'person','errors')}">

								<g:if test="${personInstance}">

									<g:select optionKey="id" from="${personInstance}" name="person.id" value="${personInstance?.id}" ></g:select>
									<input type="hidden" name="personId" value="${personInstance?.id}" >

								</g:if>

								<g:else>
	                                <g:select optionKey="id" from="${Person.executeQuery('from Person p where p.id not in (select person.id from UserLogin u) order by p.firstName')}" name="person.id" value="${userLoginInstance?.person?.id}" ></g:select>
                                    
                                </g:else>

                                <g:hasErrors bean="${userLoginInstance}" field="person">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="person"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="username"><b>Username (use email):&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'username','errors')}">
                                    <input type="text" maxlength="50" onkeyup="checkPassword($('#password')[0])" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}"/>
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
                                    <input type="checkbox" id="isLocal" name="isLocal" value="true" ${(userLoginInstance.isLocal)?'checked="checked"':''}  
                                    onchange="togglePasswordFields( $(this) )" onclick='if(this.checked){this.value = true} else {this.value = false }'/>
                                </td>
                            </tr>
                            <tr class="prop passwordsFields">
								<td valign="top" class="name">
									<label for="forcePasswordChange">Force password change:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'forcePasswordChange','errors')}">
									<input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="Y" />
								</td>
							</tr>
                        	<tr class='passwordsFields'>
								<td>
									Hide password:
								</td>
								<td>
									<input type="checkbox" onchange="togglePasswordVisibility(this)" id="showPasswordEditId"/>
								</td>
							</tr>
                            <tr class="prop passwordsFields">
                                <td valign="top" class="name">
                                    <label for="password">Password:&nbsp;</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'password','errors')}">
                                    <input type="text" maxlength="25" onkeyup="checkPassword(this)" id="password" name="password" value=""/>
                                <g:hasErrors bean="${userLoginInstance}" field="password">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr>
                            <tr class='passwordsFields'>
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
                                    <label for="expiryDate"><g:message code="userLogin.expiryDate.label" default="Expiry Date" />:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userLoginInstance, field: 'expiryDate', 'errors')}">
                                    <script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#expiryDate").datetimepicker();
				                    });
				                  </script>
                                    <input type="text" class="dateRange" id="expiryDate" name="expiryDate"
        					value="<tds:convertDateTime date="${userLoginInstance?.expiryDate}"  formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"/>
                                <g:hasErrors bean="${userLoginInstance}" field="expiryDate">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="expiryDate"/>
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
                                    <label for="active">Project:</label>
                                </td>
                                <td valign="top" class="value">
                                    <g:select id="project" name="project" from="${projectList}" 
                                    	noSelection="${['':'Select a project...']}"
                                    	optionKey="id" optionValue="name"/>
                                </td>
                            </tr>
                            <g:each in="${roleList}" var="role">
                            	<tr class="prop">
                            	 <td valign="top" class="name" >
                            	     ${role}:
                            	 </td>
                            	 <td valign="top" class="value" >
                            	     <input type="checkbox" name="assignedRole"  value="${role.id}" />
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

				                                	<option value="${assignedRole}" selected="selected">${RoleType.findById(assignedRole)}</option>

				                                </g:each>

			                                </g:if>

			                                <g:else>

			                                	<option value="USER" selected="selected">${RoleType.findById('USER')}</option>

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
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>