

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Show RefCode</title>
    </head>
    <body>
        <div class="body">
	         <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list">RefCode List</g:link></span>
	            <span class="menuButton"><g:link class="create" action="create">Create RefCode</g:link></span>
	        </div>
            <h1>Show RefCode</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Domain:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'domain')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Value:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'value')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Abbreviation:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'abbreviation')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Meaning:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'meaning')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Sort Order:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'sortOrder')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Abbreviation Or Value:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'abbreviationOrValue')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Meaning Or Value:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:refCodeInstance, field:'meaningOrValue')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${refCodeInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
