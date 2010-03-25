

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>Show UserLogin</title>
    </head>
    <body>
        
        <div class="body">
            <h1>Show UserLogin</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list" id="${companyId}">UserLogin List</g:link></span>
	            <jsec:hasRole name="ADMIN">
	            <span class="menuButton"><g:link class="create" action="create" params="[companyId:companyId]">New UserLogin</g:link></span>
	            </jsec:hasRole>
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
                            <td valign="top" class="name">Active:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'active')}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Created Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'createdDate')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Last Login:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:userLoginInstance, field:'lastLogin')}</td>
                            
                        </tr>
                        
                    
                    </tbody>
                </table>
            </div>
            <jsec:hasRole name="ADMIN">
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${userLoginInstance?.id}" />
                    <input type="hidden" name="companyId" value="${companyId}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
            </jsec:hasRole>
        </div>
    </body>
</html>
