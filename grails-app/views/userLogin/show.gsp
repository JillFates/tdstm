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

	            <tds:hasPermission permission='UserLoginShowView'>

	            <span class="menuButton"><g:link class="create" action="create" params="[companyId:companyId]">New UserLogin</g:link></span>

	            </tds:hasPermission>

        	</div>

        	<br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
						<tr class="prop">

                            <td valign="top" class="name">Username:</td>

                            

                            <td valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'username')}</td>

                            

                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name">Person:</td>
                            
                            <td valign="top" class="value"><g:link controller="person" action="show" id="${userLoginInstance?.person?.id}">${userLoginInstance?.person?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    	<tr class="prop">
                            <td valign="top" class="name"><g:message code="userLogin.expiryDate.label" default="Expiry Date" />:</td>
                            
                            <td valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.expiryDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                            
                        </tr>
                    	<tr class="prop">

                            <td valign="top" class="name">Active:</td>

                            	

                            <td valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'active')}</td>

                            

                        </tr>

                        
                        <tr class="prop">
                            <td valign="top" class="name">Created Date:</td>
                            
                            <td valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.createdDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Last Login:</td>
                            
                            <td valign="top" class="value"><tds:convertDateTime date="${userLoginInstance?.lastLogin}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                            
                        </tr>

                        
                    
                    </tbody>
                </table>
            </div>
            <tds:hasPermission permission='UserLoginShowView'>
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
