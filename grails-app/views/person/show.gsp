

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Person</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">Person List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Person</g:link></span>
        </div>
        <div class="body">
            <h1>Show Person</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Party Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'partyName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Party Created Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'partyCreatedDate')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">First Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'firstName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Active:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'active')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Person Created Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'personCreatedDate')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Last Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'lastName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Nick Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'nickName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Person Last Updated:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:personInstance, field:'personLastUpdated')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${personInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
