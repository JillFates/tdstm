<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>UserLogin</title>
    </head>
    <body>
        
        <div class="body">
            <h1>UserLogin</h1>

            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">

	            <span class="menuButton"><g:link class="list" action="list" id="${companyId}"  params="[filter:true]">UserLogin List</g:link></span>

	            <tds:hasPermission permission='CreateUserLogin'>

	            <span class="menuButton"><g:link class="create" action="create" params="[companyId:companyId]">Create UserLogin</g:link></span>

	            </tds:hasPermission>

        	</div>

        	<br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog loginView">
                <table>
                    <tbody>

                         <tr class="prop">
                            <td valign="top" class="name">Person:</td>
                            
                            <td nowrap="nowrap" valign="top" class="value"><g:link controller="person" action="show" id="${userLoginInstance?.person?.id}">${userLoginInstance?.person?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
						<tr class="prop">

                            <td valign="top" class="name">Username:</td>

                            <td valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'username')}</td>

                        </tr> 
                                           
                        <tr class="prop">

                            <td valign="top" class="name">Local account:</td>

                            <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'isLocal','errors')}">
								<input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="${userLoginInstance.isLocal}" disabled="disabled" ${(userLoginInstance.isLocal)?'checked="checked"':''}/>
							</td>

                        </tr>
                        
                        <tr class="prop">
							<td valign="top" class="name">
								<label for="forcePasswordChange">Force password change:</label>
							</td>
							
							<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'forcePasswordChange','errors')}">
								<input type="checkbox" id="forcePasswordChange" name="forcePasswordChange" value="${userLoginInstance.forcePasswordChange}" disabled="disabled" ${(userLoginInstance.forcePasswordChange=='Y')?'checked="checked"':''}/>
							</td>
						</tr>
						
                    	<tr class="prop">

                            <td valign="top" class="name">Active:</td>

                            	

                            <td nowrap="nowrap" valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'active')}</td>

                        </tr>

						<tr class="prop">
                            <td valign="top" class="name"><g:message code="userLogin.expiryDate.label" default="Expiry Date" />:</td>
                            
                            <td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.expiryDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">Created Date:</td>
                            
                            <td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.createdDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                            
                        </tr>
                        
                     	<tr class="prop">
                            <td valign="top" class="name">Last Modified:</td>
                            
                            <td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.lastModified}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Last Login:</td>
                            
                            <td nowrap="nowrap" valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.lastLogin}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                            
                        </tr>

                        <g:each in="${roleList}" var="role">
                            	<tr class="prop">
                            	 <td valign="top" class="name" >
                            	     ${role}:
                            	 </td>
                            	 <td valign="top" class="value" >
                            	     <input type="checkbox" id="${role.id}" name="assignedRole"  value="${role.id}" disabled="disabled" ${assignedRoles.id.contains(role.id) ? 'checked="checked"' : ''} />
                            	 </td>
                            	</tr>
                         </g:each>
                    
                    </tbody>
                </table>
            </div>
            <tds:hasPermission permission='EditUserLogin'>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${userLoginInstance?.id}" />
                    <input type="hidden" name="companyId" value="${companyId}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
           </tds:hasPermission>
        </div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
