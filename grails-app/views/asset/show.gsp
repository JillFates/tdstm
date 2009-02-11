

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Asset</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">Asset List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Asset</g:link></span>
        </div>
        <div class="body">
            <h1>Show Asset</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Project Name:</td>
                            
                            <td valign="top" class="value"><g:link controller="project" action="show" id="${assetInstance?.projectName?.id}">${assetInstance?.projectName?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset Type:</td>
                            
                            <td valign="top" class="value"><g:link controller="assetType" action="show" id="${assetInstance?.assetType?.id}">${assetInstance?.assetType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'assetName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset Tag:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'assetTag')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Serial Number:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'serialNumber')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Device Function:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'deviceFunction')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${assetInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
