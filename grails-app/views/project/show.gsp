

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Project</title>
    </head>
    <body>
        <div class="body">
            <h1>Show Project</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
            <jsec:hasRole name="Administrator">
            <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
            </jsec:hasRole>
        	</div>
            <br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Date Created:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'dateCreated')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Last Updated:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'lastUpdated')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'name')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Comment:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'comment')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Project Code:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'projectCode')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'description')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Track Changes:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'trackChanges')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Start Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'startDate')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Completion Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'completionDate')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <jsec:hasRole name="Administrator">
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${projectInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
            </jsec:hasRole>
        </div>
    </body>
</html>
