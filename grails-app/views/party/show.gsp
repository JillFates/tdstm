

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Party</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">Party List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Party</g:link></span>
        </div>
        <div class="body">
            <h1>Show Party</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:partyInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Party Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:partyInstance, field:'partyName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Party Created Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:partyInstance, field:'partyCreatedDate')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${partyInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
