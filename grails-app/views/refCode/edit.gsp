

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit RefCode</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">RefCode List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New RefCode</g:link></span>
        </div>
        <div class="body">
            <h1>Edit RefCode</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${refCodeInstance}">
            <div class="errors">
                <g:renderErrors bean="${refCodeInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${refCodeInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="domain">Domain:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'domain','errors')}">
                                    <input type="text" maxlength="100" id="domain" name="domain" value="${fieldValue(bean:refCodeInstance,field:'domain')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="value">Value:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'value','errors')}">
                                    <input type="text" maxlength="240" id="value" name="value" value="${fieldValue(bean:refCodeInstance,field:'value')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="abbreviation">Abbreviation:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'abbreviation','errors')}">
                                    <input type="text" maxlength="240" id="abbreviation" name="abbreviation" value="${fieldValue(bean:refCodeInstance,field:'abbreviation')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="meaning">Meaning:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'meaning','errors')}">
                                    <input type="text" maxlength="240" id="meaning" name="meaning" value="${fieldValue(bean:refCodeInstance,field:'meaning')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sortOrder">Sort Order:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'sortOrder','errors')}">
                                    <g:select from="${0..99}" id="sortOrder" name="sortOrder" value="${refCodeInstance?.sortOrder}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="abbreviationOrValue">Abbreviation Or Value:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'abbreviationOrValue','errors')}">
                                    
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="meaningOrValue">Meaning Or Value:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'meaningOrValue','errors')}">
                                    
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
