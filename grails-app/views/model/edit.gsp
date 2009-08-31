

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Model</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">Model List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Model</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Model</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${modelInstance}">
            <div class="errors">
                <g:renderErrors bean="${modelInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${modelInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:modelInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:modelInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="deviceType">Device Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'deviceType','errors')}">
                                    <g:select optionKey="id" from="${RefCode.list()}" name="deviceType.id" value="${modelInstance?.deviceType?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="manufacturer">Manufacturer:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'manufacturer','errors')}">
                                    <g:select optionKey="id" from="${Manufacturer.list()}" name="manufacturer.id" value="${modelInstance?.manufacturer?.id}" ></g:select>
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
